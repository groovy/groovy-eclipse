/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.util.Util;

public class StringLiteral extends Literal {

	char[] source;
	int lineNumber;

	public StringLiteral(char[] token, int start, int end, int lineNumber) {

		this(start,end);
		this.source = token;
		this.lineNumber = lineNumber - 1; // line number is 1 based
	}

	public StringLiteral(int s, int e) {

		super(s,e);
	}

	public void computeConstant() {

		this.constant = StringConstant.fromValue(String.valueOf(this.source));
	}

	public ExtendedStringLiteral extendWith(CharLiteral lit){

		//add the lit source to mine, just as if it was mine
		return new ExtendedStringLiteral(this,lit);
	}

	public ExtendedStringLiteral extendWith(StringLiteral lit){

		//add the lit source to mine, just as if it was mine
		return new ExtendedStringLiteral(this,lit);
	}

	/**
	 *  Add the lit source to mine, just as if it was mine
	 */
	public StringLiteralConcatenation extendsWith(StringLiteral lit) {
		return new StringLiteralConcatenation(this, lit);
	}
	/**
	 * Code generation for string literal
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {

		int pc = codeStream.position;
		if (valueRequired)
			codeStream.ldc(this.constant.stringValue());
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public TypeBinding literalType(BlockScope scope) {

		return scope.getJavaLangString();
	}

	public StringBuffer printExpression(int indent, StringBuffer output) {

		// handle some special char.....
		output.append('\"');
		for (int i = 0; i < this.source.length; i++) {
			Util.appendEscapedChar(output, this.source[i], true);
		}
		output.append('\"');
		return output;
	}

	public char[] source() {

		return this.source;
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}
}
