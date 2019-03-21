/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.select;

import org.eclipse.jdt.internal.compiler.ast.ModuleReference;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;

public class SelectionOnModuleReference extends ModuleReference {

	public SelectionOnModuleReference(char[][] tokens, long[] sourcePositions) {
		super(tokens, sourcePositions);
	}

	public ModuleBinding resolve(Scope scope) {
		ModuleBinding resolvedBinding = super.resolve(scope);
		if (resolvedBinding != null) {
			throw new SelectionNodeFound(resolvedBinding);
		} else {
			throw new SelectionNodeFound();
		}
	}

	public StringBuffer print(int tab, StringBuffer output) {
		printIndent(tab, output).append("<SelectOnModuleReference:"); //$NON-NLS-1$
		for (int i = 0; i < this.tokens.length; i++) {
			if (i > 0) output.append('.');
			output.append(this.tokens[i]);
		}
		return output.append('>');
	}

}
