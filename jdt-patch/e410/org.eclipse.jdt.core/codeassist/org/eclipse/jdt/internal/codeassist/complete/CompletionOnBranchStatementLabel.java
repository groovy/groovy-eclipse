/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
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
package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.internal.compiler.ast.BranchStatement;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

public class CompletionOnBranchStatementLabel extends BranchStatement {
	public static final int BREAK = 1;
	public static final int CONTINUE = 2;

	private int kind;
	public char[][] possibleLabels;

	public CompletionOnBranchStatementLabel(int kind, char[] l, int s, int e, char[][] possibleLabels) {
		super(l, s, e);
		this.kind = kind;
		this.possibleLabels = possibleLabels;
	}

	@Override
	public FlowInfo analyseCode(BlockScope currentScope,
			FlowContext flowContext, FlowInfo flowInfo) {
		// Is never called
		return null;
	}

	@Override
	public void resolve(BlockScope scope) {
		throw new CompletionNodeFound(this, scope);
	}
	@Override
	public StringBuffer printStatement(int indent, StringBuffer output) {
		printIndent(indent, output);
		if(this.kind == CONTINUE) {
			output.append("continue "); //$NON-NLS-1$
		} else {
			output.append("break "); //$NON-NLS-1$
		}
		output.append("<CompleteOnLabel:"); //$NON-NLS-1$
		output.append(this.label);
		return output.append(">;"); //$NON-NLS-1$
	}

}
