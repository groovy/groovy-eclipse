/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;

public class ModuleReference extends ASTNode {
	public char[][] tokens;
	public long[] sourcePositions; //each entry is using the code : (start<<32) + end
	public char[] moduleName;
	public ModuleBinding binding = null;

	public ModuleReference(char[][] tokens, long[] sourcePositions) {
		this.tokens = tokens;
		this.sourcePositions = sourcePositions;
		this.sourceEnd = (int) (sourcePositions[sourcePositions.length - 1] & 0x00000000FFFFFFFF);
		this.sourceStart = (int) (sourcePositions[0] >>> 32);
		this.moduleName = CharOperation.concatWith(tokens, '.');
	}

	@Override
	public StringBuffer print(int indent, StringBuffer output) {
		for (int i = 0; i < this.tokens.length; i++) {
			if (i > 0) output.append('.');
			output.append(this.tokens[i]);
		}
		return output;
	}

	public ModuleBinding resolve(Scope scope) {
		if (scope == null || this.binding != null)
			return this.binding;
		return this.binding = scope.environment().getModule(this.moduleName);
	}
}
