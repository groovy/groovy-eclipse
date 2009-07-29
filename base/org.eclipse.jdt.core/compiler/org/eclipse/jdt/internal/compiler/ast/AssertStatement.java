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

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.ASTVisitor;

public class AssertStatement extends Statement {
	
	public Expression assertExpression, exceptionArgument;

	// for local variable attribute
	int preAssertInitStateIndex = -1;
	private FieldBinding assertionSyntheticFieldBinding;
	
	public AssertStatement(
		Expression exceptionArgument,
		Expression assertExpression,
		int startPosition) {
			
		this.assertExpression = assertExpression;
		this.exceptionArgument = exceptionArgument;
		sourceStart = startPosition;
		sourceEnd = exceptionArgument.sourceEnd;
	}

	public AssertStatement(Expression assertExpression, int startPosition) {

		this.assertExpression = assertExpression;
		sourceStart = startPosition;
		sourceEnd = assertExpression.sourceEnd;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {
			
		preAssertInitStateIndex = currentScope.methodScope().recordInitializationStates(flowInfo);

		Constant cst = this.assertExpression.optimizedBooleanConstant();		
		boolean isOptimizedTrueAssertion = cst != Constant.NotAConstant && cst.booleanValue() == true;
		boolean isOptimizedFalseAssertion = cst != Constant.NotAConstant && cst.booleanValue() == false;

		FlowInfo assertRawInfo = assertExpression.
			analyseCode(currentScope, flowContext, flowInfo.copy());
		UnconditionalFlowInfo assertWhenTrueInfo = assertRawInfo.initsWhenTrue().
			unconditionalInits();
		UnconditionalFlowInfo assertInfo = assertRawInfo.unconditionalCopy();
		if (isOptimizedTrueAssertion) {
			assertInfo.setReachMode(FlowInfo.UNREACHABLE);
		}
		
		if (exceptionArgument != null) {
			// only gets evaluated when escaping - results are not taken into account
			FlowInfo exceptionInfo = exceptionArgument.analyseCode(currentScope, flowContext, assertInfo.copy()); 
			
			if (!isOptimizedTrueAssertion){
				flowContext.checkExceptionHandlers(
					currentScope.getJavaLangAssertionError(),
					this,
					exceptionInfo,
					currentScope);
			}
		}
		
		if (!isOptimizedTrueAssertion){
			// add the assert support in the clinit
			manageSyntheticAccessIfNecessary(currentScope, flowInfo);
		}
		if (isOptimizedFalseAssertion) {
			return flowInfo; // if assertions are enabled, the following code will be unreachable
			// change this if we need to carry null analysis results of the assert
			// expression downstream
		} else {
			return flowInfo.mergedWith(assertInfo.nullInfoLessUnconditionalCopy()).
				addInitializationsFrom(assertWhenTrueInfo.discardInitializationInfo());
			// keep the merge from the initial code for the definite assignment 
			// analysis, tweak the null part to influence nulls downstream
		}
	}

	public void generateCode(BlockScope currentScope, CodeStream codeStream) {

		if ((bits & IsReachable) == 0) {
			return;
		}
		int pc = codeStream.position;
	
		if (this.assertionSyntheticFieldBinding != null) {
			BranchLabel assertionActivationLabel = new BranchLabel(codeStream);
			codeStream.getstatic(this.assertionSyntheticFieldBinding);
			codeStream.ifne(assertionActivationLabel);
			
			BranchLabel falseLabel;
			this.assertExpression.generateOptimizedBoolean(currentScope, codeStream, (falseLabel = new BranchLabel(codeStream)), null , true);
			codeStream.newJavaLangAssertionError();
			codeStream.dup();
			if (exceptionArgument != null) {
				exceptionArgument.generateCode(currentScope, codeStream, true);
				codeStream.invokeJavaLangAssertionErrorConstructor(exceptionArgument.implicitConversion & 0xF);
			} else {
				codeStream.invokeJavaLangAssertionErrorDefaultConstructor();
			}
			codeStream.athrow();
			
			// May loose some local variable initializations : affecting the local variable attributes
			if (preAssertInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(currentScope, preAssertInitStateIndex);
			}	
			falseLabel.place();
			assertionActivationLabel.place();
		} else {			
			// May loose some local variable initializations : affecting the local variable attributes
			if (preAssertInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(currentScope, preAssertInitStateIndex);
			}			
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public void resolve(BlockScope scope) {

		assertExpression.resolveTypeExpecting(scope, TypeBinding.BOOLEAN);
		if (exceptionArgument != null) {
			TypeBinding exceptionArgumentType = exceptionArgument.resolveType(scope);
			if (exceptionArgumentType != null){
			    int id = exceptionArgumentType.id;
			    switch(id) {
					case T_void :
						scope.problemReporter().illegalVoidExpression(exceptionArgument);
					default:
					    id = T_JavaLangObject;
					case T_boolean :
					case T_byte :
					case T_char :
					case T_short :
					case T_double :
					case T_float :
					case T_int :
					case T_long :
					case T_JavaLangString :
						exceptionArgument.implicitConversion = (id << 4) + id;
				}
			}
		}
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			assertExpression.traverse(visitor, scope);
			if (exceptionArgument != null) {
				exceptionArgument.traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}	
	
	public void manageSyntheticAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {

		if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) == 0) {
    		
    		// need assertion flag: $assertionsDisabled on outer most source clas
    		// (in case of static member of interface, will use the outermost static member - bug 22334)
    		SourceTypeBinding outerMostClass = currentScope.enclosingSourceType();
			while (outerMostClass.isLocalType()) {
    			ReferenceBinding enclosing = outerMostClass.enclosingType();
    			if (enclosing == null || enclosing.isInterface()) break;
    			outerMostClass = (SourceTypeBinding) enclosing;
    		}
    
    		this.assertionSyntheticFieldBinding = outerMostClass.addSyntheticFieldForAssert(currentScope);
    
    		// find <clinit> and enable assertion support
    		TypeDeclaration typeDeclaration = outerMostClass.scope.referenceType();
    		AbstractMethodDeclaration[] methods = typeDeclaration.methods;
    		for (int i = 0, max = methods.length; i < max; i++) {
    			AbstractMethodDeclaration method = methods[i];
    			if (method.isClinit()) {
    				((Clinit) method).setAssertionSupport(assertionSyntheticFieldBinding, currentScope.compilerOptions().sourceLevel < ClassFileConstants.JDK1_5);
    				break;
    			}
    		}
		}
	}

	public StringBuffer printStatement(int tab, StringBuffer output) {

		printIndent(tab, output);
		output.append("assert "); //$NON-NLS-1$
		this.assertExpression.printExpression(0, output);
		if (this.exceptionArgument != null) {
			output.append(": "); //$NON-NLS-1$
			this.exceptionArgument.printExpression(0, output);
		}
		return output.append(';');
	}
	
}
