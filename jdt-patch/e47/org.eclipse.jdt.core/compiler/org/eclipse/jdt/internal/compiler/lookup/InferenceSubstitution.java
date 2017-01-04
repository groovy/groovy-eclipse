/*******************************************************************************
 * Copyright (c) 2013, 2015 GK Software AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

/**
 * A type variable substitution strategy based on inference variables (JLS8 18.1.1)
 */
public class InferenceSubstitution extends Scope.Substitutor implements Substitution {

	private LookupEnvironment environment;
	private InferenceVariable[] variables;
	private InvocationSite site;

	public InferenceSubstitution(LookupEnvironment environment, InferenceVariable[] variables, InvocationSite site) {
		this.environment = environment;
		this.variables = variables;
		this.site = site;
	}

	public InferenceSubstitution(InferenceContext18 context) {
		this(context.environment, context.inferenceVariables, context.currentInvocation);
	}

	/**
	 * Override method {@link Scope.Substitutor#substitute(Substitution, TypeBinding)}, 
	 * to add substitution of types other than type variables.
	 */
	public TypeBinding substitute(Substitution substitution, TypeBinding originalType) {
		for (int i = 0; i < this.variables.length; i++) {
			InferenceVariable variable = this.variables[i];
			if (this.site == variable.site && TypeBinding.equalsEquals(getP(i), originalType)) {
				if (this.environment.globalOptions.isAnnotationBasedNullAnalysisEnabled && originalType.hasNullTypeAnnotations())
					return this.environment.createAnnotatedType(variable.withoutToplevelNullAnnotation(), originalType.getTypeAnnotations());
				return variable;
			}
		}

		return super.substitute(substitution, originalType);
	}

	/**
	 * Get the type corresponding to the ith inference variable.
	 * Default behavior is to answer the inference variable's type parameter.
	 * Sub-class may override to substitute other types.
	 */
	protected TypeBinding getP(int i) {
		return this.variables[i].typeParameter;
	}

	public TypeBinding substitute(TypeVariableBinding typeVariable) {
		ReferenceBinding superclass = typeVariable.superclass;
		ReferenceBinding[] superInterfaces = typeVariable.superInterfaces;
		boolean hasSubstituted = false;
		variableLoop: for (int i = 0; i < this.variables.length; i++) {
			InferenceVariable variable = this.variables[i];
			TypeBinding pi = getP(i);
			if (TypeBinding.equalsEquals(pi, typeVariable))
				return variable;
			if (TypeBinding.equalsEquals(pi, superclass)) {
				superclass = variable;
				hasSubstituted = true;
				continue;
			}
			if (superInterfaces != null) {
				int ifcLen = superInterfaces.length; 
				for (int j = 0; j < ifcLen; j++) {
					if (TypeBinding.equalsEquals(pi, superInterfaces[j])) {
						if (superInterfaces == typeVariable.superInterfaces)
							System.arraycopy(superInterfaces, 0, superInterfaces = new ReferenceBinding[ifcLen], 0, ifcLen);
						superInterfaces[j] = variable;
						hasSubstituted = true;
						continue variableLoop;
					}
				}
			}
		}
		if (hasSubstituted) {
			typeVariable = new TypeVariableBinding(typeVariable.sourceName, typeVariable.declaringElement, typeVariable.rank, this.environment);
			typeVariable.superclass = superclass;
			typeVariable.superInterfaces = superInterfaces;
			typeVariable.firstBound = superclass != null ? superclass : superInterfaces[0];
			if (typeVariable.firstBound.hasNullTypeAnnotations())
				typeVariable.tagBits |= TagBits.HasNullTypeAnnotation;
		}
		return typeVariable;
	}

	public LookupEnvironment environment() {
		return this.environment;
	}

	public boolean isRawSubstitution() {
		// FIXME Auto-generated method stub
		return false;
	}
}
