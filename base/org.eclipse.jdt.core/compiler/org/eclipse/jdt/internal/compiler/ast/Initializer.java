/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;

public class Initializer extends FieldDeclaration {

	public Block block;
	public int lastVisibleFieldID;
	public int bodyStart;
	public int bodyEnd;

	public Initializer(Block block, int modifiers) {
		this.block = block;
		this.modifiers = modifiers;
		
		if (block != null) {
			declarationSourceStart = sourceStart = block.sourceStart;
		}
	}

	public FlowInfo analyseCode(
		MethodScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		if (block != null) {
			return block.analyseCode(currentScope, flowContext, flowInfo);
		}
		return flowInfo;
	}

	/**
	 * Code generation for a non-static initializer: 
	 *    standard block code gen
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {

		if ((bits & IsReachable) == 0) {
			return;
		}
		int pc = codeStream.position;
		if (block != null) block.generateCode(currentScope, codeStream);
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration#getKind()
	 */
	public int getKind() {
		return INITIALIZER;
	}
	
	public boolean isStatic() {

		return (this.modifiers & ClassFileConstants.AccStatic) != 0;
	}
	
	public void parseStatements(
		Parser parser,
		TypeDeclaration typeDeclaration,
		CompilationUnitDeclaration unit) {

		//fill up the method body with statement
		parser.parse(this, typeDeclaration, unit);
	}

	public StringBuffer printStatement(int indent, StringBuffer output) {

		if (modifiers != 0) {
			printIndent(indent, output);
			printModifiers(modifiers, output);
			if (this.annotations != null) printAnnotations(this.annotations, output);
			output.append("{\n"); //$NON-NLS-1$
			if (block != null) {
				block.printBody(indent, output);
			}
			printIndent(indent, output).append('}'); 
			return output;
		} else if (block != null) {
			block.printStatement(indent, output);
		} else {
			printIndent(indent, output).append("{}"); //$NON-NLS-1$
		}
		return output;
	}
	
	public void resolve(MethodScope scope) {

		FieldBinding previousField = scope.initializedField;
		int previousFieldID = scope.lastVisibleFieldID;
		try {
			scope.initializedField = null;
			scope.lastVisibleFieldID = lastVisibleFieldID;
			if (isStatic()) {
				ReferenceBinding declaringType = scope.enclosingSourceType();
				if (declaringType.isNestedType() && !declaringType.isStatic())
					scope.problemReporter().innerTypesCannotDeclareStaticInitializers(
						declaringType,
						this);
			}
			if (block != null) block.resolve(scope);
		} finally {
		    scope.initializedField = previousField;
			scope.lastVisibleFieldID = previousFieldID;
		}
	}

	public void traverse(ASTVisitor visitor, MethodScope scope) {
		if (visitor.visit(this, scope)) {
			if (block != null) block.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}
