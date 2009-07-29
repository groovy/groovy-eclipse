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

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;

public class CharLiteral extends NumberLiteral {
	char value;
public CharLiteral(char[] token, int s, int e) {
	super(token, s, e);
	computeValue();
}
public void computeConstant() {
	//The source is a  char[3] first and last char are '
	//This is true for both regular char AND unicode char
	//BUT not for escape char like '\b' which are char[4]....

	constant = CharConstant.fromValue(value);
}
private void computeValue() {
	//The source is a  char[3] first and last char are '
	//This is true for both regular char AND unicode char
	//BUT not for escape char like '\b' which are char[4]....

	if ((value = source[1]) != '\\')
		return;
	char digit;
	switch (digit = source[2]) {
		case 'b' :
			value = '\b';
			break;
		case 't' :
			value = '\t';
			break;
		case 'n' :
			value = '\n';
			break;
		case 'f' :
			value = '\f';
			break;
		case 'r' :
			value = '\r';
			break;
		case '\"' :
			value = '\"';
			break;
		case '\'' :
			value = '\'';
			break;
		case '\\' :
			value = '\\';
			break;
		default : //octal (well-formed: ended by a ' )
			int number = ScannerHelper.getNumericValue(digit);
			if ((digit = source[3]) != '\'')
				number = (number * 8) + ScannerHelper.getNumericValue(digit);
			else {
				constant = CharConstant.fromValue(value = (char) number);
				break;
			}
			if ((digit = source[4]) != '\'')
				number = (number * 8) + ScannerHelper.getNumericValue(digit);
			value = (char) number;
			break;
	}
}
/**
 * CharLiteral code generation
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 * @param valueRequired boolean
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	int pc = codeStream.position;
	if (valueRequired) {
		codeStream.generateConstant(constant, implicitConversion);
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}
public TypeBinding literalType(BlockScope scope) {
	return TypeBinding.CHAR;
}
public void traverse(ASTVisitor visitor, BlockScope blockScope) {
	visitor.visit(this, blockScope);
	visitor.endVisit(this, blockScope);
}
}
