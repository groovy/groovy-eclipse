/*******************************************************************************
 * Copyright (c) 2000, 2024 IBM Corporation and others.
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

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

public class ExtendedStringLiteral extends StringLiteral {

	protected ExtendedStringLiteral(StringLiteral optionalHead, Object sourcesTail, int start, int end, int lineNumber) {
		super(optionalHead, sourcesTail, start,  end,  lineNumber);
	}

	@Override
	public StringBuilder printExpression(int indent, StringBuilder output) {
		return output.append("ExtendedStringLiteral{").append(this.source()).append('}'); //$NON-NLS-1$
	}

	@Override
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}
}
