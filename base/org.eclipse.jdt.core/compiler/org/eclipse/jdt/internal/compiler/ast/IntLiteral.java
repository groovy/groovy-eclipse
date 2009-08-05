/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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

public class IntLiteral extends NumberLiteral {
	
	public int value;

	public static final IntLiteral One = new IntLiteral(new char[]{'1'},0,0,1);//used for ++ and --
	
public IntLiteral(char[] token, int s, int e) {
	super(token, s,e);
}

public IntLiteral(char[] token, int s,int e, int value) {
	this(token, s,e);
	this.value = value;
}

public IntLiteral(int intValue) {
	//special optimized constructor : the cst is the argument
	//value that should not be used
	//	tokens = null ;
	//	sourceStart = 0;
	//	sourceEnd = 0;
	super(null,0,0);
	this.constant = IntConstant.fromValue(intValue);
	this.value = intValue;
}

public void computeConstant() {
	//a special constant is use for the potential Integer.MAX_VALUE+1
	//which is legal if used with a - as prefix....cool....
	//notice that Integer.MIN_VALUE  == -2147483648
	long MAX = Integer.MAX_VALUE;
	if (this == One) {	
		this.constant = IntConstant.fromValue(1); 
		return ;
	}
	int length = this.source.length;
	long computedValue = 0L;
	if (this.source[0] == '0') {	
		MAX = 0xFFFFFFFFL ; //a long in order to be positive !
		if (length == 1) {
			this.constant = IntConstant.fromValue(0); return ;
		}
		final int shift,radix;
		int j ;
		if ((this.source[1] == 'x') || (this.source[1] == 'X')) {	
			shift = 4 ; j = 2; radix = 16;
		} else {	
			shift = 3 ; j = 1; radix = 8;
		}
		while (this.source[j]=='0')	 {	
			j++; //jump over redondant zero
			if (j == length) {
				//watch for 000000000000000000
				this.constant = IntConstant.fromValue(this.value = (int)computedValue);
				return;
			}
		}
		while (j<length) {	
			int digitValue ;
			if ((digitValue = ScannerHelper.digit(this.source[j++],radix))	< 0 ) {
				return; /*constant stays null*/
			}
			computedValue = (computedValue<<shift) | digitValue ;
			if (computedValue > MAX) return; /*constant stays null*/
		}
	} else {	
		//-----------regular case : radix = 10-----------
		for (int i = 0 ; i < length;i++) {	
			int digitValue ;
			if ((digitValue = ScannerHelper.digit(this.source[i],10))	< 0 ) {
				return; /*constant stays null*/
			}
			computedValue = 10*computedValue + digitValue;
			if (computedValue > MAX) return /*constant stays null*/ ;
		}
	}
	this.constant = IntConstant.fromValue(this.value = (int)computedValue);

}

/**
 * Code generation for int literal
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 * @param valueRequired boolean
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	int pc = codeStream.position;
	if (valueRequired) {
		codeStream.generateConstant(this.constant, this.implicitConversion);
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}

public TypeBinding literalType(BlockScope scope) {
	return TypeBinding.INT;
}

public final boolean mayRepresentMIN_VALUE(){
	//a special autorized int literral is 2147483648
	//which is ONE over the limit. This special case
	//only is used in combinaison with - to denote
	//the minimal value of int -2147483648
	return ((this.source.length == 10) &&
			(this.source[0] == '2') &&
			(this.source[1] == '1') &&
			(this.source[2] == '4') &&
			(this.source[3] == '7') &&
			(this.source[4] == '4') &&
			(this.source[5] == '8') &&
			(this.source[6] == '3') &&
			(this.source[7] == '6') &&
			(this.source[8] == '4') &&
			(this.source[9] == '8') &&
			(((this.bits & ASTNode.ParenthesizedMASK) >> ASTNode.ParenthesizedSHIFT) == 0));
}

public StringBuffer printExpression(int indent, StringBuffer output){
	if (this.source == null) {
	/* special optimized IntLiteral that are created by the compiler */
		return output.append(String.valueOf(this.value));
	}
	return super.printExpression(indent, output);
}

public void traverse(ASTVisitor visitor, BlockScope scope) {
	visitor.visit(this, scope);
	visitor.endVisit(this, scope);
}
}
