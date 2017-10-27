/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.internal.compiler.ast.ModuleReference;

public class CompletionOnKeywordModule2 extends ModuleReference implements CompletionOnKeyword {
	private char[] token;
	private char[][] possibleKeywords;

	public CompletionOnKeywordModule2(char[] token, long pos, char[][] possibleKeywords) {
		super(new char[][] {token}, new long[] {pos}); // dummy
		this.token = token;
		this.possibleKeywords = possibleKeywords;
		this.sourceStart = (int) (pos>>>32)  ;
		this.sourceEnd = (int) (pos & 0x00000000FFFFFFFFL);
	}

	@Override
	public char[] getToken() {
		return this.token;
	}

	@Override
	public char[][] getPossibleKeywords() {
		return this.possibleKeywords;
	}
}
