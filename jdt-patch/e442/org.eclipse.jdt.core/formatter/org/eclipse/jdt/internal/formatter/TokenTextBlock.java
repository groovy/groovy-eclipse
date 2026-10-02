/*******************************************************************************
 * Copyright (c) 20265 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter;

import org.eclipse.jdt.internal.compiler.parser.TerminalToken;

public class TokenTextBlock extends Token {

	private boolean hasReplace = false;

	public TokenTextBlock(int sourceStart, int sourceEnd, TerminalToken tokenType) {
		super(sourceStart, sourceEnd, tokenType);
	}

	public boolean hasReplace() {
		return this.hasReplace;
	}

	public void setReplace(boolean value) {
		this.hasReplace = value;
	}
}
