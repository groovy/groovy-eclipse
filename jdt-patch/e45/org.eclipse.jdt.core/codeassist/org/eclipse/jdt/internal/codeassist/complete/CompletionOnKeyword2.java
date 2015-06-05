/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public class CompletionOnKeyword2 extends ImportReference implements CompletionOnKeyword {
	private char[] token;
	private char[][] possibleKeywords;
	public CompletionOnKeyword2(char[] token, long pos, char[][] possibleKeywords) {
		super(new char[][]{token}, new long[]{pos}, false, ClassFileConstants.AccDefault);
		this.token = token;
		this.possibleKeywords = possibleKeywords;
	}
	public char[] getToken() {
		return this.token;
	}
	public char[][] getPossibleKeywords() {
		return this.possibleKeywords;
	}
	public StringBuffer print(int indent, StringBuffer output, boolean withOnDemand) {

		return printIndent(indent, output).append("<CompleteOnKeyword:").append(this.token).append('>'); //$NON-NLS-1$
	}
}
