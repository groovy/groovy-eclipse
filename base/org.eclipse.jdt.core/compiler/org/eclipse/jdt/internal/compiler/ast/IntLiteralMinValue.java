/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.impl.*;

public class IntLiteralMinValue extends IntLiteral {

	final static char[] CharValue = new char[]{'-','2','1','4','7','4','8','3','6','4','8'};
	final static Constant MIN_VALUE = IntConstant.fromValue(Integer.MIN_VALUE) ; 

public IntLiteralMinValue() {
	super(CharValue,0,0,Integer.MIN_VALUE);
	constant = MIN_VALUE;
}
public void computeConstant(){
	
	/*precomputed at creation time*/ }
}
