/*******************************************************************************
 * Copyright (c) 2013, 2014 GK Software AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
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

	public InferenceSubstitution(LookupEnvironment environment, InferenceVariable[] variables) {
		this.environment = environment;
		this.variables = variables;
	}
	
	/**
	 * Override method {@link Scope.Substitutor#substitute(Substitution, TypeBinding)}, 
	 * to add substitution of types other than type variables.
	 */
	public TypeBinding substitute(Substitution substitution, TypeBinding originalType) {
		for (int i = 0; i < this.variables.length; i++) {
			InferenceVariable variable = this.variables[i];
			if (TypeBinding.equalsEquals(variable.typeParameter, originalType)) {
				variable.nullHints |= originalType.tagBits & TagBits.AnnotationNullMASK;
				return variable;
			}
		}

		return super.substitute(substitution, originalType);
	}

	public TypeBinding substitute(TypeVariableBinding typeVariable) {
		ReferenceBinding superclass = typeVariable.superclass;
		ReferenceBinding[] superInterfaces = typeVariable.superInterfaces;
		boolean hasSubstituted = false;
		variableLoop: for (int i = 0; i < this.variables.length; i++) {
			InferenceVariable variable = this.variables[i];
			if (TypeBinding.equalsEquals(variable.typeParameter, typeVariable))
				return variable;
			if (TypeBinding.equalsEquals(variable.typeParameter, superclass)) {
				superclass = variable;
				hasSubstituted = true;
				continue;
			}
			if (superInterfaces != null) {
				int ifcLen = superInterfaces.length; 
				for (int j = 0; j < ifcLen; j++) {
					if (TypeBinding.equalsEquals(variable.typeParameter, superInterfaces[j])) {
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
