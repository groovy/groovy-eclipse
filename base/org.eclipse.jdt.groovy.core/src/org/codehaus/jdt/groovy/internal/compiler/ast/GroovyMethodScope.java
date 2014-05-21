package org.codehaus.jdt.groovy.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;

/**
 * {@code GroovyMethodScope} class contains fixes specific for Groovy.
 *
 * @author denis_murashev
 */
public class GroovyMethodScope extends MethodScope {

	public GroovyMethodScope(Scope parent, ReferenceContext context, boolean isStatic, int lastVisibleFieldID) {
		super(parent, context, isStatic, lastVisibleFieldID);
	}

	public GroovyMethodScope(Scope parent, ReferenceContext context, boolean isStatic) {
		super(parent, context, isStatic);
	}

	@Override
	protected void checkAndSetModifiersForMethod(MethodBinding methodBinding) {
		if (isTrait()) {
			int modifiers = methodBinding.modifiers;
			final ReferenceBinding declaringClass = methodBinding.declaringClass;

			int realModifiers = modifiers & ExtraCompilerModifiers.AccJustFlag;
			int expectedModifiers = ClassFileConstants.AccPublic | ClassFileConstants.AccAbstract | ClassFileConstants.AccPrivate;
			boolean isDefaultMethod = (modifiers & ExtraCompilerModifiers.AccDefaultMethod) != 0;
			boolean reportIllegalModifierCombination = false;
			boolean isJDK18orGreater = false;
			if (compilerOptions().sourceLevel >= ClassFileConstants.JDK1_8 && !declaringClass.isAnnotationType()) {
				expectedModifiers |= ClassFileConstants.AccStrictfp | ExtraCompilerModifiers.AccDefaultMethod
						| ClassFileConstants.AccStatic;
				isJDK18orGreater = true;
				if (!methodBinding.isAbstract()) {
					reportIllegalModifierCombination = isDefaultMethod && methodBinding.isStatic();
				} else {
					reportIllegalModifierCombination = isDefaultMethod || methodBinding.isStatic();
					if (methodBinding.isStrictfp()) {
						problemReporter().illegalAbstractModifierCombinationForMethod(
								(AbstractMethodDeclaration) this.referenceContext);
					}
				}
				if (reportIllegalModifierCombination) {
					problemReporter().illegalModifierCombinationForInterfaceMethod(
							(AbstractMethodDeclaration) this.referenceContext);
				}
				if (isDefaultMethod) {
					realModifiers |= ExtraCompilerModifiers.AccDefaultMethod;
				}
			}
			if ((realModifiers & ~expectedModifiers) != 0) {
				if ((declaringClass.modifiers & ClassFileConstants.AccAnnotation) != 0) {
					problemReporter().illegalModifierForAnnotationMember((AbstractMethodDeclaration) this.referenceContext);
				} else {
					problemReporter().illegalModifierForInterfaceMethod((AbstractMethodDeclaration) this.referenceContext,
							isJDK18orGreater);
				}
			}
		} else {
			super.checkAndSetModifiersForMethod(methodBinding);
		}
	}

	private boolean isTrait() {
		if (this.parent instanceof GroovyClassScope) {
			TypeDeclaration parentContext = ((GroovyClassScope) this.parent).referenceContext;
			if (parentContext.annotations == null) {
				return false;
			}
			for (Annotation annotation : parentContext.annotations) {
				if ("@groovy.transform.Trait".equals(annotation.toString())) { //$NON-NLS-1$
					return true;
				}
			}
		}
		return false;
	}
}
