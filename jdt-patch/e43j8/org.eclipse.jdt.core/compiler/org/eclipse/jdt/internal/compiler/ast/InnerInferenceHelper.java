/*******************************************************************************
 * Copyright (c) 2014 GK Software AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class InnerInferenceHelper {

	/** For each candidate method store here the array of argument types if inner inference has improved any during Invocation Type Inference. */
	private Map<MethodBinding,TypeBinding[]> argTypesPerCandidate = new HashMap<MethodBinding,TypeBinding[]>();

	public void registerInnerResult(MethodBinding method, TypeBinding resolvedType, int argCount, int argIdx) {
		TypeBinding[] argTypes = this.argTypesPerCandidate.get(method);
		if (argTypes == null)
			this.argTypesPerCandidate.put(method, argTypes = new TypeBinding[argCount]);
		argTypes[argIdx] = resolvedType;
	}
	
	public TypeBinding[] getArgumentTypesForCandidate(MethodBinding candidate, TypeBinding[] plainArgTypes) {
		TypeBinding[] argTypes = this.argTypesPerCandidate.get(candidate);
		if (argTypes == null)
			return plainArgTypes;
		// fill in any blanks now:
		for (int i = 0; i < argTypes.length; i++) {
			if (argTypes[i] == null)
				argTypes[i] = plainArgTypes[i];
		}
		return argTypes;
	}
}
