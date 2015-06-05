/*******************************************************************************
 * Copyright (c) 2014, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

public class PolyParameterizedGenericMethodBinding extends ParameterizedGenericMethodBinding { // confused citizen.

	private ParameterizedGenericMethodBinding wrappedBinding; 
	public PolyParameterizedGenericMethodBinding(ParameterizedGenericMethodBinding applicableMethod) {
		super(applicableMethod.originalMethod, applicableMethod.typeArguments, applicableMethod.environment);
		this.wrappedBinding = applicableMethod;
	}
	
	public boolean equals(Object other) {
		if (other instanceof PolyParameterizedGenericMethodBinding) {
			PolyParameterizedGenericMethodBinding ppgmb = (PolyParameterizedGenericMethodBinding)other;
			return this.wrappedBinding.equals(ppgmb.wrappedBinding);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.wrappedBinding.hashCode();
	}
}
