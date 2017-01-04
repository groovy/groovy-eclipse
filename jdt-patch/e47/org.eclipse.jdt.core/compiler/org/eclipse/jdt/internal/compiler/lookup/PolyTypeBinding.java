/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								Bug 428811 - [1.8][compiler] Type witness unnecessarily required
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;

public class PolyTypeBinding extends TypeBinding {

	Expression expression;
	boolean vanillaCompatibilty = true;
	
	public PolyTypeBinding(Expression expression) {
		this.expression = expression;
	}
	
	public char[] constantPoolName() {
		throw new UnsupportedOperationException();  // should never reach code generation
	}

	public PackageBinding getPackage() {
		throw new UnsupportedOperationException();  // nobody should be asking this question.
	}

	public boolean isCompatibleWith(TypeBinding left, Scope scope) {
		return this.vanillaCompatibilty ? this.expression.isCompatibleWith(left, scope) : this.expression.isBoxingCompatibleWith(left, scope);
	}
	
	@Override
	public boolean isPotentiallyCompatibleWith(TypeBinding targetType, Scope scope) {
		return this.expression.isPotentiallyCompatibleWith(targetType, scope);
	}

	@Override
	public boolean isPolyType() {
		return true;
	}
	
	@Override
	public boolean isFunctionalType() {
		return this.expression.isFunctionalType();
	}

	public char[] qualifiedSourceName() {
		return readableName();
	}

	public char[] sourceName() {
		return readableName();
	}

	public char[] readableName() {
		return this.expression.printExpression(0,  new StringBuffer()).toString().toCharArray();
	}

	public char[] shortReadableName() {
		return this.expression instanceof LambdaExpression ?
				((LambdaExpression) this.expression).printExpression(0, new StringBuffer(), true).toString().toCharArray() : readableName();
	}

	public boolean sIsMoreSpecific(TypeBinding s, TypeBinding t, Scope scope) {
		return this.expression.sIsMoreSpecific(s, t, scope);
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer("PolyTypeBinding for: "); //$NON-NLS-1$
		return this.expression.printExpression(0,  buffer).toString();
	}
	
	public int kind() {
		return Binding.POLY_TYPE;
	}

	public TypeBinding computeBoxingType() {
		PolyTypeBinding type = new PolyTypeBinding(this.expression);
		type.vanillaCompatibilty = !this.vanillaCompatibilty;
		return type;
	}
}