/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 */

package org.eclipse.jdt.internal.codeassist.select;

import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class SelectionOnLambdaExpression extends LambdaExpression {
	public SelectionOnLambdaExpression(LambdaExpression expression) {
		// Where is object derivation when I need it ???
		super(expression.compilationResult(), true);
		// copy all state created by the parser.
		this.sourceStart = expression.sourceStart;
		this.sourceEnd = expression.sourceEnd;
		this.hasParentheses = expression.hasParentheses;
		this.statementEnd = expression.statementEnd;
		this.setBody(expression.body());
		this.setArguments(expression.arguments());
		this.setArrowPosition(expression.arrowPosition());
	}
	@Override
	public TypeBinding resolveType(BlockScope blockScope) {
		TypeBinding resolveType = super.resolveType(blockScope);
		if (this.expectedType != null && this.original == this) {  // final resolution.
			throw new SelectionNodeFound(this.descriptor);
		}
		return resolveType;
	}
}