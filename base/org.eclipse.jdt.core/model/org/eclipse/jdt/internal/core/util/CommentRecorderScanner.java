/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.util;

import org.eclipse.jdt.internal.compiler.parser.Scanner;

/**
 * Internal scanner used for DOM AST nodes.
 * 
 * @since 3.0
 */
public class CommentRecorderScanner extends Scanner {

	public CommentRecorderScanner(
		boolean tokenizeComments,
		boolean tokenizeWhiteSpace,
		boolean checkNonExternalizedStringLiterals,
		long sourceLevel,
		char[][] taskTags,
		char[][] taskPriorities,
		boolean isTaskCaseSensitive) {
		super(tokenizeComments, tokenizeWhiteSpace, checkNonExternalizedStringLiterals, sourceLevel, taskTags, taskPriorities, isTaskCaseSensitive);
	}
	
	/**
	 * Set start position negative for line comments.
	 * @see org.eclipse.jdt.internal.compiler.parser.Scanner#recordComment(int)
	 */
	public void recordComment(int token) {
		super.recordComment(token);
		if (token == TokenNameCOMMENT_LINE) {
			// for comment line both positions are negative
			this.commentStarts[this.commentPtr] = -this.commentStarts[this.commentPtr];
		}
	}
}
