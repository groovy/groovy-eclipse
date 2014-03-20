/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jesper S Moller - Contributions for
 *								Bug 378674 - "The method can be declared as static" is wrong
 *     Stephan Herrmann - Contribution for
 *							Bug 400874 - [1.8][compiler] Inference infrastructure should evolve to meet JLS8 18.x (Part G of JSR335 spec)
 *******************************************************************************/
package org.eclipse.jdt.internal.eval;

import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.InferenceContext18;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * A this reference inside a code snippet denotes a remote
 * receiver object (that is, the receiver of the context in the stack frame)
 */
public class CodeSnippetThisReference extends ThisReference implements EvaluationConstants, InvocationSite {

	EvaluationContext evaluationContext;
	FieldBinding delegateThis;
	boolean isImplicit;

	/**
	 * CodeSnippetThisReference constructor comment.
	 * @param s int
	 * @param sourceEnd int
	 */
	public CodeSnippetThisReference(int s, int sourceEnd, EvaluationContext evaluationContext, boolean isImplicit) {
		super(s, sourceEnd);
		this.evaluationContext = evaluationContext;
		this.isImplicit = isImplicit;
	}
	
	public boolean checkAccess(BlockScope scope, ReferenceBinding thisType) {
		// this/super cannot be used in constructor call
		MethodScope methodScope = scope.methodScope();
		if (this.evaluationContext.isConstructorCall) {
			methodScope.problemReporter().fieldsOrThisBeforeConstructorInvocation(this);
			return false;
		}

		// static may not refer to this/super
		if (this.evaluationContext.declaringTypeName == null || this.evaluationContext.isStatic) {
			methodScope.problemReporter().errorThisSuperInStatic(this);
			return false;
		}
		scope.tagAsAccessingEnclosingInstanceStateOf(thisType, false /* type variable access */);
		return true;
	}
	
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		int pc = codeStream.position;
		if (valueRequired) {
			codeStream.aload_0();
			codeStream.fieldAccess(Opcodes.OPC_getfield, this.delegateThis, null /* default declaringClass */); // delegate field access
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.InvocationSite#genericTypeArguments()
	 */
	public TypeBinding[] genericTypeArguments() {
		return null;
	}
	
	public InferenceContext18 freshInferenceContext(Scope scope) {
		return null;
	}

	public boolean isSuperAccess(){
		return false;
	}
	
	public boolean isTypeAccess(){
		return false;
	}
	
	public StringBuffer printExpression(int indent, StringBuffer output){

		char[] declaringType = this.evaluationContext.declaringTypeName;
		output.append('(');
		if (declaringType == null)
			output.append("<NO DECLARING TYPE>"); //$NON-NLS-1$
		else
			output.append(declaringType);
		return output.append(")this"); //$NON-NLS-1$
	}
	
	public TypeBinding resolveType(BlockScope scope) {
		// implicit this
		this.constant = Constant.NotAConstant;
		ReferenceBinding snippetType = scope.enclosingSourceType();
		MethodScope methodScope = scope.methodScope();
		if (!this.isImplicit && !checkAccess(scope, snippetType)) {
			return null;
		}

		this.delegateThis = scope.getField(snippetType, DELEGATE_THIS, this);
		if (this.delegateThis == null || !this.delegateThis.isValidBinding()) {
			// should not happen
			// if this happen we should report illegal access to this in a static context
			methodScope.problemReporter().errorThisSuperInStatic(this);
			return null;
		}
		return this.resolvedType = this.delegateThis.type;
	}
	
	public void setActualReceiverType(ReferenceBinding receiverType) {
		// ignored
	}
	
	public void setDepth(int depth){
		// ignored
	}
	
	public void setFieldIndex(int index){
		// ignored
	}
}
