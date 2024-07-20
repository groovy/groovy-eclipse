/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *    Jesper Steen Moller - Bug 412150 [1.8] [compiler] Enable reflected parameter names during annotation processing
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.apt.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationHolder;
import org.eclipse.jdt.internal.compiler.lookup.AptBinaryLocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodVerifier;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

public class ExecutableElementImpl extends ElementImpl implements
		ExecutableElement {

	private Name _name = null;

	/* package */ ExecutableElementImpl(BaseProcessingEnvImpl env, MethodBinding binding) {
		super(env, binding);
	}

	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p)
	{
		return v.visitExecutable(this, p);
	}

	@Override
	protected AnnotationBinding[] getAnnotationBindings()
	{
		return ((MethodBinding)this._binding).getAnnotations();
	}

	@Override
	public AnnotationValue getDefaultValue() {
		MethodBinding binding = (MethodBinding)this._binding;
		Object defaultValue = binding.getDefaultValue();
		if (defaultValue != null) return new AnnotationMemberValue(this._env, defaultValue, binding);
		return null;
	}

	@Override
	public List<? extends Element> getEnclosedElements() {
		return Collections.emptyList();
	}

	@Override
	public Element getEnclosingElement() {
		MethodBinding binding = (MethodBinding)this._binding;
		if (null == binding.declaringClass) {
			return null;
		}
		return this._env.getFactory().newElement(binding.declaringClass);
	}

	@Override
	public String getFileName() {
		ReferenceBinding dc = ((MethodBinding)this._binding).declaringClass;
		char[] name = dc.getFileName();
		if (name == null)
			return null;
		return new String(name);
	}

	@Override
	public ElementKind getKind() {
		MethodBinding binding = (MethodBinding)this._binding;
		if (binding.isConstructor()) {
			return ElementKind.CONSTRUCTOR;
		}
		else if (CharOperation.equals(binding.selector, TypeConstants.CLINIT)) {
			return ElementKind.STATIC_INIT;
		}
		else if (CharOperation.equals(binding.selector, TypeConstants.INIT)) {
			return ElementKind.INSTANCE_INIT;
		}
		else {
			return ElementKind.METHOD;
		}
	}

	@Override
	public Set<Modifier> getModifiers() {
		MethodBinding binding = (MethodBinding)this._binding;
		return Factory.getModifiers(binding.modifiers, getKind());
	}

	@Override
	PackageElement getPackage()
	{
		MethodBinding binding = (MethodBinding)this._binding;
		if (null == binding.declaringClass) {
			return null;
		}
		return this._env.getFactory().newPackageElement(binding.declaringClass.fPackage);
	}

	@Override
	public List<? extends VariableElement> getParameters() {
		MethodBinding binding = (MethodBinding)this._binding;
		int length = binding.parameters == null ? 0 : binding.parameters.length;
		if (0 != length) {
			AbstractMethodDeclaration methodDeclaration = binding.sourceMethod();
			List<VariableElement> params = new ArrayList<>(length);
			if (methodDeclaration != null) {
				for (Argument argument : methodDeclaration.arguments) {
					VariableElement param = new VariableElementImpl(this._env, argument.binding);
					params.add(param);
				}
			} else {
				// binary method
				AnnotationBinding[][] parameterAnnotationBindings = null;
				AnnotationHolder annotationHolder = binding.declaringClass.retrieveAnnotationHolder(binding, false);
				if (annotationHolder != null) {
					parameterAnnotationBindings = annotationHolder.getParameterAnnotations();
				}
				// we need to filter the synthetic arguments
				int i = 0;
				for (TypeBinding typeBinding : binding.parameters) {
					char name[] = binding.parameterNames.length > i ? binding.parameterNames[i] : null;
					if (name == null) {
 						StringBuilder builder = new StringBuilder("arg");//$NON-NLS-1$
						builder.append(i);
						name = String.valueOf(builder).toCharArray();
					}
					VariableElement param = new VariableElementImpl(this._env,
							new AptBinaryLocalVariableBinding(
									name,
									typeBinding,
									0,
									parameterAnnotationBindings != null ? parameterAnnotationBindings[i] : null,
									binding));
					params.add(param);
					i++;
				}
			}
			return Collections.unmodifiableList(params);
		}
		return Collections.emptyList();
	}

	@Override
	public TypeMirror getReturnType() {
		MethodBinding binding = (MethodBinding)this._binding;
		if (binding.returnType == null) {
			return null;
		}
		else return this._env.getFactory().newTypeMirror(binding.returnType);
	}

	@Override
	public Name getSimpleName() {
		MethodBinding binding = (MethodBinding)this._binding;
		if (this._name == null) {
			this._name = new NameImpl(binding.selector);
		}
		return this._name;
	}

	@Override
	public List<? extends TypeMirror> getThrownTypes() {
		MethodBinding binding = (MethodBinding)this._binding;
		if (binding.thrownExceptions.length == 0) {
			return Collections.emptyList();
		}
		List<TypeMirror> list = new ArrayList<>(binding.thrownExceptions.length);
		for (ReferenceBinding exception : binding.thrownExceptions) {
			list.add(this._env.getFactory().newTypeMirror(exception));
		}
		return list;
	}

	@Override
	public List<? extends TypeParameterElement> getTypeParameters() {
		MethodBinding binding = (MethodBinding)this._binding;
		TypeVariableBinding[] variables = binding.typeVariables();
		if (variables.length == 0) {
			return Collections.emptyList();
		}
		List<TypeParameterElement> params = new ArrayList<>(variables.length);
		for (TypeVariableBinding variable : variables) {
			params.add(this._env.getFactory().newTypeParameterElement(variable, this));
		}
		return Collections.unmodifiableList(params);
	}

	@Override
	public boolean hides(Element hidden)
	{
		if (!(hidden instanceof ExecutableElementImpl)) {
			return false;
		}
		MethodBinding hiderBinding = (MethodBinding)this._binding;
		MethodBinding hiddenBinding = (MethodBinding)((ExecutableElementImpl)hidden)._binding;
		if (hiderBinding == hiddenBinding) {
			return false;
		}
		if (hiddenBinding.isPrivate()) {
			return false;
		}
		// See JLS 8.4.8: hiding only applies to static methods
		if (!hiderBinding.isStatic() || !hiddenBinding.isStatic()) {
			return false;
		}
		// check names
		if (!CharOperation.equals(hiddenBinding.selector, hiderBinding.selector)) {
			return false;
		}
		// check parameters
		if (!this._env.getLookupEnvironment().methodVerifier().isMethodSubsignature(hiderBinding, hiddenBinding)) {
			return false;
		}
		return null != hiderBinding.declaringClass.findSuperTypeOriginatingFrom(hiddenBinding.declaringClass);
	}

	@Override
	public boolean isVarArgs() {
		return ((MethodBinding) this._binding).isVarargs();
	}

	/**
	 * Return true if this method overrides {@code overridden} in the context of {@code type}.  For
	 * instance, consider
	 * <pre>
	 *   interface A { void f(); }
	 *   class B { void f() {} }
	 *   class C extends B implements I { }
	 * </pre>
	 * In the context of B, B.f() does not override A.f(); they are unrelated.  But in the context of
	 * C, B.f() does override A.f().  That is, the copy of B.f() that C inherits overrides A.f().
	 * This is equivalent to considering two questions: first, does C inherit B.f(); if so, does
	 * the inherited C.f() override A.f().  If B.f() were private, for instance, then in the context
	 * of C it would still not override A.f().
	 *
     * <p>see jls3 8.4.8 Inheritance, Overriding, and Hiding
     * <p>see jls3 9.4.1 Inheritance and Overriding
	 * @see javax.lang.model.util.Elements#overrides(ExecutableElement, ExecutableElement, TypeElement)
	 */
	public boolean overrides(ExecutableElement overridden, TypeElement type)
	{
		MethodBinding overriddenBinding = (MethodBinding)((ExecutableElementImpl) overridden)._binding;
		ReferenceBinding overriderContext = (ReferenceBinding)((TypeElementImpl)type)._binding;
		if ((MethodBinding)this._binding == overriddenBinding
				|| overriddenBinding.isStatic()
				|| overriddenBinding.isPrivate()
				|| ((MethodBinding)this._binding).isStatic()) {
			return false;
		}
		char[] selector = ((MethodBinding)this._binding).selector;
		if (!CharOperation.equals(selector, overriddenBinding.selector))
			return false;

		// Construct a binding to the equivalent of this (the overrider) as it would be inherited by 'type'.
		// Can only do this if 'type' is descended from the overrider.
		// Second clause of the AND is required to match a peculiar javac behavior.
		if (null == overriderContext.findSuperTypeOriginatingFrom(((MethodBinding)this._binding).declaringClass) &&
				null == ((MethodBinding)this._binding).declaringClass.findSuperTypeOriginatingFrom(overriderContext)) {
			return false;
		}
		MethodBinding overriderBinding = new MethodBinding((MethodBinding)this._binding, overriderContext);
		if (overriderBinding.isPrivate()) {
			// a private method can never override another method.  The other method would either be
			// private itself, in which case it would not be visible; or this would be a restriction
			// of access, which is a compile-time error.
			return false;
		}

		TypeBinding match = overriderBinding.declaringClass.findSuperTypeOriginatingFrom(overriddenBinding.declaringClass);
		if (!(match instanceof ReferenceBinding)) return false;

		org.eclipse.jdt.internal.compiler.lookup.MethodBinding[] superMethods = ((ReferenceBinding)match).getMethods(selector);
		LookupEnvironment lookupEnvironment = this._env.getLookupEnvironment();
		if (lookupEnvironment == null) return false;
		MethodVerifier methodVerifier = lookupEnvironment.methodVerifier();
		for (MethodBinding superMethod : superMethods) {
			if (superMethod.original() == overriddenBinding) {
				return methodVerifier.doesMethodOverride(overriderBinding, superMethod);
			}
		}
		return false;
	}

	@Override
	public TypeMirror getReceiverType() {
		return this._env.getFactory().getReceiverType((MethodBinding) this._binding);
	}

	@Override
	public boolean isDefault() {
		if (this._binding != null) {
			return ((MethodBinding)this._binding).isDefaultMethod();
		}
		return false;
	}

}
