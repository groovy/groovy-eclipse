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
package org.eclipse.jdt.internal.codeassist.select;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ModuleDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceModuleBinding;

public class SelectionOnModuleDeclaration extends ModuleDeclaration {

	public SelectionOnModuleDeclaration(CompilationResult compilationResult, char[][] tokens, long[] positions) {
		super(compilationResult, tokens, positions);
	}

	@Override
	public ModuleBinding setBinding(SourceModuleBinding sourceModuleBinding) {
		super.setBinding(sourceModuleBinding);
		throw new SelectionNodeFound(this.binding);
	}

}
