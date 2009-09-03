/*******************************************************************************
 * Copyright (c) 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement        - Initial API and implementation
 *     Andrew Eisenberg - Additional work
 *******************************************************************************/
package org.codehaus.jdt.groovy.internal.compiler.ast;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MissingTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

@SuppressWarnings("restriction")
public class GroovyClassScope extends ClassScope {

	public GroovyClassScope(Scope parent, TypeDeclaration typeDecl) {
		super(parent, typeDecl);
	}

	@Override
	protected boolean connectSuperInterfaces() {
		boolean noProblems = super.connectSuperInterfaces();
		return noProblems;
	}

	char[] GROOVY = "groovy".toCharArray(); //$NON-NLS-1$
	char[][] GROOVY_LANG_METACLASS = { GROOVY, TypeConstants.LANG, "MetaClass".toCharArray() }; //$NON-NLS-1$

	public final ReferenceBinding getGroovyLangMetaClassBinding() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(GROOVY_LANG_METACLASS);
		return unitScope.environment.getResolvedType(GROOVY_LANG_METACLASS, this);
	}

	@Override
	protected MethodBinding[] augmentMethodBindings(MethodBinding[] methodBindings) {

		// Don't add these methods to annotations
		if (this.referenceContext.binding != null
				&& (this.referenceContext.binding.isAnnotationType() || this.referenceContext.binding.isInterface())) {
			return methodBindings;
		}

		// Now add the groovy.lang.GroovyObject methods:
		//
		// Object invokeMethod(String name, Object args);
		// Object getProperty(String propertyName);
		// void setProperty(String propertyName, Object newValue);
		// MetaClass getMetaClass();
		// void setMetaClass(MetaClass metaClass);

		TypeBinding bindingJLO = getJavaLangObject();
		TypeBinding bindingJLS = getJavaLangString();
		TypeBinding bindingGLM = getGroovyLangMetaClassBinding();
		// FIXASC (M2) check visibility - which should be synthetic?

		List<MethodBinding> groovyMethods = new ArrayList<MethodBinding>();
		createMethod("invokeMethod", false, "", new TypeBinding[] { bindingJLS, bindingJLO }, bindingJLO, groovyMethods,
				methodBindings);
		createMethod("getProperty", false, "", new TypeBinding[] { bindingJLS }, bindingJLO, groovyMethods, methodBindings);
		createMethod("setProperty", false, "", new TypeBinding[] { bindingJLS, bindingJLO }, TypeBinding.VOID, groovyMethods,
				methodBindings);
		createMethod("getMetaClass", false, "", null, bindingGLM, groovyMethods, methodBindings);
		createMethod("setMetaClass", false, "", new TypeBinding[] { bindingGLM }, TypeBinding.VOID, groovyMethods, methodBindings);

		// FIXASC (M2) decide what difference this makes - should we not be adding anything at all?
		// will not be an instance of GroovyTypeDeclaration if created through
		// SourceTypeConverter
		if (this.referenceContext instanceof GroovyTypeDeclaration) {
			GroovyTypeDeclaration typeDeclaration = (GroovyTypeDeclaration) this.referenceContext;

			// FIXASC (M3) the methods created here need to be a subtype of
			// MethodBinding because they need their source position to be the
			// property
			List<PropertyNode> properties = typeDeclaration.properties;
			for (PropertyNode property : properties) {
				String name = property.getName();
				FieldBinding fBinding = typeDeclaration.binding.getField(name.toCharArray(), false);
				if (!(fBinding.type instanceof MissingTypeBinding)) {
					String getterName = "get" + MetaClassHelper.capitalize(name);
					createMethod(getterName, property.isStatic(), "", /* TypeBinding.NO_TYPES */null, fBinding.type, groovyMethods,
							methodBindings);
					if (!fBinding.isFinal()) {
						String setterName = "set" + MetaClassHelper.capitalize(name);
						createMethod(setterName, property.isStatic(), "", new TypeBinding[] { fBinding.type }, TypeBinding.VOID,
								groovyMethods, methodBindings);
					}
					if (fBinding.type == TypeBinding.BOOLEAN) {
						createMethod("is" + MetaClassHelper.capitalize(name), property.isStatic(), "", /* TypeBinding.NO_TYPES, */
						null, fBinding.type, groovyMethods, methodBindings);
					}
				}
			}
		}

		MethodBinding[] newMethodBindings = groovyMethods.toArray(new MethodBinding[methodBindings.length + groovyMethods.size()]);
		System.arraycopy(methodBindings, 0, newMethodBindings, groovyMethods.size(), methodBindings.length);
		return newMethodBindings;
	}

	private void createMethod(String name, boolean isStatic, String signature, TypeBinding[] parameterTypes,
			TypeBinding returnType, List<MethodBinding> groovyMethods, MethodBinding[] existingMethods) {
		boolean found = false;
		for (MethodBinding existingMethod : existingMethods) {
			if (new String(existingMethod.selector).equals(name)) {
				// FIXASC (M2) safe to do this resolution so early?
				((SourceTypeBinding) existingMethod.declaringClass).resolveTypesFor(existingMethod);
				boolean equalParameters = true;
				if (parameterTypes == null) {
					// not looking for parameters, if this has none, that is OK
					if (existingMethod.parameters.length != 0) {
						equalParameters = false;
					}
				} else if (existingMethod.parameters.length == parameterTypes.length) {
					TypeBinding[] existingParams = existingMethod.parameters;
					for (int p = 0, max = parameterTypes.length; p < max; p++) {
						if (!CharOperation.equals(parameterTypes[p].signature(), existingParams[p].signature())) {
							equalParameters = false;
							break;
						}
					}
				}
				// FIXASC (M2) consider return type?
				if (equalParameters) {
					found = true;
					break;
				}
				// FIXASC (M2) what about inherited methods - what if the supertype
				// provides an implementation, does the subtype get a new method?
			}
		}
		if (!found) {
			int modifiers = ClassFileConstants.AccPublic;
			if (isStatic) {
				modifiers |= ClassFileConstants.AccStatic;
			}
			if (this.referenceContext.binding.isInterface()) {
				modifiers |= ClassFileConstants.AccAbstract;
			}
			MethodBinding mb = new MethodBinding(modifiers, name.toCharArray(), returnType, parameterTypes, null,
					this.referenceContext.binding);
			// FIXASC (M2) parameter names - what value would it have to set them correctly?
			groovyMethods.add(mb);
		}
	}

	@Override
	public boolean shouldReport(int problem) {
		if (problem == IProblem.SuperclassMustBeAClass) {
			return false;
		}
		return true;
	}

	// FIXASC (M3) currently inactive - this enables getSingleton()
	// FIXASC (M3) make this switchable as it is too damn powerful
	// @Override
	@Override
	public MethodBinding[] getAnyExtraMethods(char[] selector) {
		if (true) {
			return null;
		}
		List<MethodNode> mns = ((GroovyTypeDeclaration) referenceContext).getClassNode().getMethods(new String(selector));
		MethodBinding[] newMethods = new MethodBinding[mns.size()];
		int idx = 0;
		for (MethodNode methodNode : mns) {
			TypeBinding[] parameterTypes = null;
			TypeBinding returnType = compilationUnitScope().environment.getResolvedType(CharOperation.splitAndTrimOn('.',
					methodNode.getReturnType().getName().toCharArray()), this);
			newMethods[idx++] = new MethodBinding(methodNode.getModifiers(), selector, returnType, parameterTypes, null,
					this.referenceContext.binding);
		}
		// unitScope.environment.getResolvedType(JAVA_LANG_STRING, this);
		return newMethods;
	}
}
