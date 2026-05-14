/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.ExceptionLabel;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

/**
 * Extra behavior for statements which have a finally block - e.g., try blocks have finally; synchronized statements have hidden finally blocks that call monitorexit etc.
 */
public abstract class StatementWithFinallyBlock extends Statement {

	public static void reenterAllExceptionHandlers(StatementWithFinallyBlock[] statements, int max, CodeStream codeStream) {
		if (statements == null) return;
		if (max < 0) max = statements.length;
		for (int i = 0; i < max; i++) {
			StatementWithFinallyBlock stmt = statements[i];
			stmt.enterAnyExceptionHandler(codeStream);
			stmt.enterDeclaredExceptionHandlers(codeStream);
			stmt.enterResourceExceptionHandlers(codeStream);
		}
	}

	ExceptionLabel anyExceptionLabel;

	public ExceptionLabel enterAnyExceptionHandler(CodeStream codeStream) {

		if (this.anyExceptionLabel == null) {
			this.anyExceptionLabel = new ExceptionLabel(codeStream, null /*any exception*/);
		}
		this.anyExceptionLabel.placeStart();
		return this.anyExceptionLabel;
	}

	public void enterDeclaredExceptionHandlers(CodeStream codeStream) {
		// do nothing by default
	}

	public void enterResourceExceptionHandlers(CodeStream codeStream) {
		// do nothing by default
	}

	public void exitAnyExceptionHandler() {
		if (this.anyExceptionLabel != null) {
			this.anyExceptionLabel.placeEnd();
		}
	}

	public void exitDeclaredExceptionHandlers(CodeStream codeStream) {
		// do nothing by default
	}

	/**
	 * Generate the finally block in current context.
	 * @return boolean, <code>true</code> if the generated code will complete abruptly.
	 */
	public abstract boolean generateFinallyBlock(BlockScope currentScope, CodeStream codeStream, int stateIndex);

	public abstract boolean isFinallyBlockEscaping();

	public void placeAllAnyExceptionHandler() {
		this.anyExceptionLabel.place();
	}
}