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

/**
 * Flatten string literal
 */
public class StringLiteralConcatenation extends StringLiteral {

	/**
	 * Build a two-strings literal
	 */
	public StringLiteralConcatenation(StringLiteral str1, StringLiteral lit) {
		super(str1, lit, str1.sourceStart, lit.sourceEnd, str1.getLineNumber());
	}

	@Override
	public StringBuilder printExpression(int indent, StringBuilder output) {
		output.append("StringLiteralConcatenation{"); //$NON-NLS-1$
		for (StringLiteral lit : getLiterals()) {
			lit.printExpression(indent, output);
			output.append("+\n");//$NON-NLS-1$
		}
		return output.append('}');
	}

	@Override
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			for (StringLiteral lit : getLiterals()) {
				lit.traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}

	public StringLiteral[] getLiterals() {
		int size = append(null, 0, this);
		StringLiteral[] result = new StringLiteral[size];
		append(result, 0, this);
		return result;
	}

	private static int append(StringLiteral[] result, int length, StringLiteral o) {
		do {
			if (o.tail instanceof StringLiteral l) {
				if (result != null) {
					result[result.length - 1 - length] = l;
				}
				length += 1;
			} else {
				if (result != null) {
					result[result.length - 1 - length] = o;
				}
				length += 1;
			}
			o = o.optionalHead;
		} while (o != null);
		return length;
	}

	@Override
	protected void flatten(char[] result) {
		// don't store flattened representation: getLiterals() still needs the linked list
	}

	@Override
	public void setSource(char[] source) {
		throw new UnsupportedOperationException();
	}

}