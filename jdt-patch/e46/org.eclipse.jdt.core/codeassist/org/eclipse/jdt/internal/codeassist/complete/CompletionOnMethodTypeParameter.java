/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;

public class CompletionOnMethodTypeParameter extends MethodDeclaration {
	public CompletionOnMethodTypeParameter(TypeParameter[] typeParameters, CompilationResult compilationResult){
		super(compilationResult);
		this.selector = CharOperation.NO_CHAR;
		this.typeParameters = typeParameters;
		this.sourceStart = typeParameters[0].sourceStart;
		this.sourceEnd = typeParameters[typeParameters.length - 1].sourceEnd;
	}

	public void resolveStatements() {
			throw new CompletionNodeFound(this, this.scope);
	}

	public StringBuffer print(int tab, StringBuffer output) {
		printIndent(tab, output);
		output.append('<');
		int max = this.typeParameters.length - 1;
		for (int j = 0; j < max; j++) {
			this.typeParameters[j].print(0, output);
			output.append(", ");//$NON-NLS-1$
		}
		this.typeParameters[max].print(0, output);
		output.append('>');
		return output;
	}

}
