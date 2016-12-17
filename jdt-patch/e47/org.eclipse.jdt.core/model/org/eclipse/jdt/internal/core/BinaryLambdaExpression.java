/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IJavaElement;


public class BinaryLambdaExpression extends LambdaExpression {

	BinaryLambdaExpression(JavaElement parent, org.eclipse.jdt.internal.compiler.ast.LambdaExpression lambdaExpression) {
		super(parent, lambdaExpression);
	}

	BinaryLambdaExpression(JavaElement parent, String interphase, int sourceStart, int sourceEnd, int arrowPosition) {
		super(parent, interphase, sourceStart, sourceEnd, arrowPosition);
	}

	BinaryLambdaExpression(JavaElement parent, String interphase, int sourceStart, int sourceEnd, int arrowPosition, LambdaMethod lambdaMethod) {
		super(parent, interphase, sourceStart, sourceEnd, arrowPosition, lambdaMethod);
	}

	/*
	 * @see JavaElement#getPrimaryElement(boolean)
	 */
	public IJavaElement getPrimaryElement(boolean checkOwner) {
		return this;
	}

	/*
	 * @see IMember#isBinary()
	 */
	public boolean isBinary() {
		return true;
	}
}
