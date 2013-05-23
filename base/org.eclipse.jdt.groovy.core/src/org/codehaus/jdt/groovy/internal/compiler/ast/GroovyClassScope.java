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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LazilyResolvedMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MissingTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

@SuppressWarnings("restriction")
public class GroovyClassScope extends ClassScope {

	// SET FOR TESTING ONLY, enables tests to listen for interesting events
	public static EventListener debugListener = null;

	public GroovyClassScope(Scope parent, TypeDeclaration typeDecl) {
		super(parent, typeDecl);
	}

	@Override
	protected boolean connectSuperInterfaces() {
		boolean noProblems = super.connectSuperInterfaces();
		return noProblems;
	}

	// FIXASC pull out into common util area (see GCUScope too)
	char[] GROOVY = "groovy".toCharArray(); //$NON-NLS-1$
	char[][] GROOVY_LANG_METACLASS = { GROOVY, TypeConstants.LANG, "MetaClass".toCharArray() }; //$NON-NLS-1$
	char[][] GROOVY_LANG_GROOVYOBJECT = { GROOVY, TypeConstants.LANG, "GroovyObject".toCharArray() }; // $NON-NLS-1$

	public final ReferenceBinding getGroovyLangMetaClassBinding() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(GROOVY_LANG_METACLASS);
		return unitScope.environment.getResolvedType(GROOVY_LANG_METACLASS, this);
	}

	/**
	 * Add any groovy specific method bindings to the set determined by the compiler. These
	 */
	@Override
	protected MethodBinding[] augmentMethodBindings(MethodBinding[] methodBindings) {
		// Don't add these methods to annotations
		SourceTypeBinding binding = this.referenceContext.binding;
		if (binding != null && (binding.isAnnotationType() || binding.isInterface())) {
			return methodBindings;
		}
		boolean implementsGroovyLangObject = false;

		ReferenceBinding[] superInterfaces = binding.superInterfaces;
		if (superInterfaces != null) {
			for (int i = 0, max = superInterfaces.length; i < max; i++) {
				char[][] interfaceName = superInterfaces[i].compoundName;
				if (CharOperation.equals(interfaceName, GROOVY_LANG_GROOVYOBJECT)) {
					implementsGroovyLangObject = true;
					break;
				}
			}
		}

		List<MethodBinding> groovyMethods = new ArrayList<MethodBinding>();

		// If we don't then a supertype did and these methods do not have to be added here
		if (implementsGroovyLangObject) {
			if (debugListener != null) {
				debugListener.record("augment: type " + new String(this.referenceContext.name)
						+ " having GroovyObject methods added");
			}
			TypeBinding bindingJLO = getJavaLangObject();
			TypeBinding bindingJLS = getJavaLangString();
			TypeBinding bindingGLM = getGroovyLangMetaClassBinding();

			// Now add the groovy.lang.GroovyObject methods:
			//
			// Object invokeMethod(String name, Object args);
			// Object getProperty(String propertyName);
			// void setProperty(String propertyName, Object newValue);
			// MetaClass getMetaClass();
			// void setMetaClass(MetaClass metaClass);

			// Note on synthetic
			// javac/ecj don't see synthetic methods when considering if a type implements an interface. So don't make these
			// synthetic

			// Visibility is public and possibly static/abstract depending on the containing type
			createMethod("invokeMethod", false, "", new TypeBinding[] { bindingJLS, bindingJLO }, bindingJLO, groovyMethods,
					methodBindings, null);
			createMethod("getProperty", false, "", new TypeBinding[] { bindingJLS }, bindingJLO, groovyMethods, methodBindings,
					null);
			createMethod("setProperty", false, "", new TypeBinding[] { bindingJLS, bindingJLO }, TypeBinding.VOID, groovyMethods,
					methodBindings, null);
			createMethod("getMetaClass", false, "", null, bindingGLM, groovyMethods, methodBindings, null);
			createMethod("setMetaClass", false, "", new TypeBinding[] { bindingGLM }, TypeBinding.VOID, groovyMethods,
					methodBindings, null);
		}
		// FIXASC decide what difference this makes - should we not be adding anything at all?
		// will not be an instance of GroovyTypeDeclaration if created through SourceTypeConverter
		if (this.referenceContext instanceof GroovyTypeDeclaration) {
			GroovyTypeDeclaration typeDeclaration = (GroovyTypeDeclaration) this.referenceContext;

			boolean useOldWay = false;
			if (useOldWay) {
				// FIXASC the methods created here need to be a subtype of
				// MethodBinding because they need their source position to be the
				// property
				List<PropertyNode> properties = typeDeclaration.properties;
				for (PropertyNode property : properties) {
					String name = property.getName();
					FieldBinding fBinding = typeDeclaration.binding.getField(name.toCharArray(), false);
					// null binding indicates there was a problem resolving its type
					if (fBinding != null && !(fBinding.type instanceof MissingTypeBinding)) {
						String getterName = "get" + MetaClassHelper.capitalize(name);
						createMethod(getterName, property.isStatic(), "", /* TypeBinding.NO_TYPES */null, fBinding.type,
								groovyMethods, methodBindings, typeDeclaration);
						if (!fBinding.isFinal()) {
							String setterName = "set" + MetaClassHelper.capitalize(name);
							createMethod(setterName, property.isStatic(), "", new TypeBinding[] { fBinding.type },
									TypeBinding.VOID, groovyMethods, methodBindings, typeDeclaration);
						}
						if (fBinding.type == TypeBinding.BOOLEAN) {
							createMethod("is" + MetaClassHelper.capitalize(name), property.isStatic(), "", /* TypeBinding.NO_TYPES, */
									null, fBinding.type, groovyMethods, methodBindings, typeDeclaration);
						}
					}
				}
			} else {
				// Create getters/setters without resolving the types.
				List<PropertyNode> properties = typeDeclaration.properties;
				for (PropertyNode property : properties) {
					String name = property.getName();
					String capitalizedName = MetaClassHelper.capitalize(name);
					// Create getter
					createGetterMethod(name, "get" + capitalizedName, property.isStatic(), groovyMethods, methodBindings,
							typeDeclaration);
					// Create setter if non-final property
					if (!Modifier.isFinal(property.getModifiers())) {
						createSetterMethod(name, "set" + capitalizedName, property.isStatic(), groovyMethods, methodBindings,
								typeDeclaration, property.getType().getName());
					}
					// Create isA if type is boolean
					String propertyType = property.getType().getName();
					if (propertyType.equals("boolean")) {
						createGetterMethod(name, "is" + capitalizedName, property.isStatic(), groovyMethods, methodBindings,
								typeDeclaration);
					}
				}
			}
		}

		MethodBinding[] newMethodBindings = groovyMethods.toArray(new MethodBinding[methodBindings.length + groovyMethods.size()]);
		System.arraycopy(methodBindings, 0, newMethodBindings, groovyMethods.size(), methodBindings.length);
		return newMethodBindings;
	}

	private void createMethod(String name, boolean isStatic, String signature, TypeBinding[] parameterTypes,
			TypeBinding returnType, List<MethodBinding> groovyMethods, MethodBinding[] existingMethods,
			GroovyTypeDeclaration typeDeclaration) {
		boolean found = false;
		for (MethodBinding existingMethod : existingMethods) {
			if (new String(existingMethod.selector).equals(name)) {
				// FIXASC safe to do this resolution so early?
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
				// FIXASC consider return type?
				if (equalParameters) {
					found = true;
					break;
				}
				// FIXASC what about inherited methods - what if the supertype
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
			char[] methodName = name.toCharArray();
			/*
			 * if (typeDeclaration != null) { // check we are not attempting to override a final method MethodBinding[]
			 * existingBindings = typeDeclaration.binding.getMethods(name.toCharArray()); int stop = 1; }
			 */
			MethodBinding mb = new MethodBinding(modifiers, methodName, returnType, parameterTypes, null,
					this.referenceContext.binding);
			// FIXASC parameter names - what value would it have to set them correctly?
			groovyMethods.add(mb);
		}
	}

	private void createGetterMethod(String propertyName, String name, boolean isStatic, List<MethodBinding> groovyMethods,
			MethodBinding[] existingMethods, GroovyTypeDeclaration typeDeclaration) {
		boolean found = false;

		char[] nameAsCharArray = name.toCharArray();
		for (MethodBinding existingMethod : existingMethods) {
			if (CharOperation.equals(nameAsCharArray, existingMethod.selector)) {
				// check if this possible candidate has parameters (if it does, it can't be our getter)
				if ((existingMethod.modifiers & ExtraCompilerModifiers.AccUnresolved) != 0) {
					// need some intelligence here
					AbstractMethodDeclaration methodDecl = existingMethod.sourceMethod();
					if (methodDecl == null) {
						// FIXASC decide what we can do here
					} else {
						Argument[] arguments = methodDecl.arguments;
						if (arguments == null || arguments.length == 0) {
							found = true;
						}
					}
				} else {
					TypeBinding[] existingParams = existingMethod.parameters;
					if (existingParams == null || existingParams.length == 0) {
						found = true;
					}
				}
			}
		}

		// FIXASC what about inherited methods - what if the supertype
		// provides an implementation, does the subtype get a new method?
		if (!found) {
			int modifiers = ClassFileConstants.AccPublic;
			if (isStatic) {
				modifiers |= ClassFileConstants.AccStatic;
			}
			if (this.referenceContext.binding.isInterface()) {
				modifiers |= ClassFileConstants.AccAbstract;
			}
			/*
			 * if (typeDeclaration != null) { // check we are not attempting to override a final method MethodBinding[]
			 * existingBindings = typeDeclaration.binding.getMethods(name.toCharArray()); int stop = 1; }
			 */
			MethodBinding mb = new LazilyResolvedMethodBinding(true, propertyName, modifiers, nameAsCharArray, null,
					this.referenceContext.binding);
			// FIXASC parameter names - what value would it have to set them correctly?
			groovyMethods.add(mb);
		}
	}

	private void createSetterMethod(String propertyName, String name, boolean isStatic, List<MethodBinding> groovyMethods,
			MethodBinding[] existingMethods, GroovyTypeDeclaration typeDeclaration, String propertyType) {
		boolean found = false;

		char[] nameAsCharArray = name.toCharArray();
		for (MethodBinding existingMethod : existingMethods) {
			if (CharOperation.equals(nameAsCharArray, existingMethod.selector)) {
				// check if this possible candidate has parameters (if it does, it can't be our getter)
				if ((existingMethod.modifiers & ExtraCompilerModifiers.AccUnresolved) != 0) {
					// lets look at the declaration
					AbstractMethodDeclaration methodDecl = existingMethod.sourceMethod();
					if (methodDecl == null) {
						// FIXASC decide what we can do here
					} else {
						Argument[] arguments = methodDecl.arguments;
						if (arguments != null && arguments.length == 1) {
							// might be a candidate, it takes one parameter
							// TypeReference tr = arguments[0].type;
							// String typename = new String(CharOperation.concatWith(tr.getTypeName(), '.'));
							// // not really an exact comparison here...
							// if (typename.endsWith(propertyName)) {
							found = true;
							// }
						}
					}
				} else {
					TypeBinding[] existingParams = existingMethod.parameters;
					if (existingParams != null && existingParams.length == 1) {
						// if (CharOperation.equals(existingParams[0].signature(),)) {
						// might be a candidate, it takes one parameter
						found = true;
						// }
					}
				}
			}
		}

		// FIXASC what about inherited methods - what if the supertype
		// provides an implementation, does the subtype get a new method?
		if (!found) {
			int modifiers = ClassFileConstants.AccPublic;
			if (isStatic) {
				modifiers |= ClassFileConstants.AccStatic;
			}
			if (this.referenceContext.binding.isInterface()) {
				modifiers |= ClassFileConstants.AccAbstract;
			}
			char[] methodName = name.toCharArray();
			/*
			 * if (typeDeclaration != null) { // check we are not attempting to override a final method MethodBinding[]
			 * existingBindings = typeDeclaration.binding.getMethods(name.toCharArray()); int stop = 1; }
			 */
			MethodBinding mb = new LazilyResolvedMethodBinding(false, propertyName, modifiers, methodName, null,
					this.referenceContext.binding);
			// FIXASC parameter names - what value would it have to set them correctly?
			groovyMethods.add(mb);
		}
	}

	@Override
	public boolean shouldReport(int problem) {
		if (problem == IProblem.SuperclassMustBeAClass) {
			return false;
		}
		if (problem == IProblem.IncompatibleReturnType) {
			return false;
		}
		if (problem == IProblem.AbstractMethodMustBeImplemented) {
			return false;
		}
		if (problem == IProblem.MethodNameClash) {
			return false;
		}
		if (problem == IProblem.VarargsConflict) {
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
			TypeBinding returnType = compilationUnitScope().environment.getResolvedType(
					CharOperation.splitAndTrimOn('.', methodNode.getReturnType().getName().toCharArray()), this);
			newMethods[idx++] = new MethodBinding(methodNode.getModifiers(), selector, returnType, parameterTypes, null,
					this.referenceContext.binding);
		}
		// unitScope.environment.getResolvedType(JAVA_LANG_STRING, this);
		return newMethods;
	}

	@Override
	protected ClassScope buildClassScope(Scope parent, TypeDeclaration typeDecl) {
		return new GroovyClassScope(parent, typeDecl);
	}

	@Override
	public void buildFieldsAndMethods() {
		super.buildFieldsAndMethods();
		GroovyTypeDeclaration context = (GroovyTypeDeclaration) referenceContext;
		GroovyTypeDeclaration[] anonymousTypes = context.getAnonymousTypes();
		if (anonymousTypes != null) {
			for (GroovyTypeDeclaration anonType : anonymousTypes) {
				GroovyClassScope anonScope = new GroovyClassScope(this, anonType);
				anonType.scope = anonScope;
				anonType.resolve(anonType.enclosingMethod.scope);
			}
		}
	}
}
