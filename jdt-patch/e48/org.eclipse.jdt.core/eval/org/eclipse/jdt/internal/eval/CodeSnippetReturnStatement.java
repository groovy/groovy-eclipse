/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *							Bug 400874 - [1.8][compiler] Inference infrastructure should evolve to meet JLS8 18.x (Part G of JSR335 spec)
 *******************************************************************************/
package org.eclipse.jdt.internal.eval;

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.InferenceContext18;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * A return statement inside a code snippet. During the code gen,
 * it uses a macro to set the result of the code snippet instead
 * of returning it.
 */
public class CodeSnippetReturnStatement extends ReturnStatement implements InvocationSite, EvaluationConstants {
	MethodBinding setResultMethod;
public CodeSnippetReturnStatement(Expression expr, int s, int e) {
	super(expr, s, e);
}

public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	FlowInfo info = super.analyseCode(currentScope, flowContext, flowInfo);
	// we need to remove this optimization in order to prevent the inlining of the return bytecode
	// 1GH0AU7: ITPJCORE:ALL - Eval - VerifyError in scrapbook page
	this.expression.bits &= ~IsReturnedValue;
	return info;
}

/**
 * Dump the suitable return bytecode for a return statement
 *
 */
public void generateReturnBytecode(CodeStream codeStream) {

	// output the return bytecode
	codeStream.return_();
}
public void generateStoreSaveValueIfNecessary(CodeStream codeStream){

	// push receiver
	codeStream.aload_0();

	// push the 2 parameters of "setResult(Object, Class)"
	if (this.expression == null || this.expression.resolvedType == TypeBinding.VOID) { // expressionType == VoidBinding if code snippet is the expression "System.out.println()"
		// push null
		codeStream.aconst_null();

		// void.class
		codeStream.generateClassLiteralAccessForType(TypeBinding.VOID, null);
	} else {
		// swap with expression
		int valueTypeID = this.expression.resolvedType.id;
		if (valueTypeID == T_long || valueTypeID == T_double) {
			codeStream.dup_x2();
			codeStream.pop();
		} else {
			codeStream.swap();
		}

		// generate wrapper if needed
		if (this.expression.resolvedType.isBaseType() && this.expression.resolvedType != TypeBinding.NULL) {
			codeStream.generateBoxingConversion(this.expression.resolvedType.id);
		}

		// generate the expression type
		codeStream.generateClassLiteralAccessForType(this.expression.resolvedType, null);
	}

	// generate the invoke virtual to "setResult(Object,Class)"
	codeStream.invoke(Opcodes.OPC_invokevirtual, this.setResultMethod, null /* default declaringClass */);
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
public boolean isSuperAccess() {
	return false;
}
public boolean isTypeAccess() {
	return false;
}
public boolean needValue(){
	return true;
}
public void prepareSaveValueLocation(TryStatement targetTryStatement){

	// do nothing: no storage is necessary for snippets
}
public void resolve(BlockScope scope) {
	if (this.expression != null) {
		if (this.expression.resolveType(scope) != null) {
			TypeBinding javaLangClass = scope.getJavaLangClass();
			if (!javaLangClass.isValidBinding()) {
				scope.problemReporter().codeSnippetMissingClass("java.lang.Class", this.sourceStart, this.sourceEnd); //$NON-NLS-1$
				return;
			}
			TypeBinding javaLangObject = scope.getJavaLangObject();
			if (!javaLangObject.isValidBinding()) {
				scope.problemReporter().codeSnippetMissingClass("java.lang.Object", this.sourceStart, this.sourceEnd); //$NON-NLS-1$
				return;
			}
			TypeBinding[] argumentTypes = new TypeBinding[] {javaLangObject, javaLangClass};
			this.setResultMethod = scope.getImplicitMethod(SETRESULT_SELECTOR, argumentTypes, this);
			if (!this.setResultMethod.isValidBinding()) {
				scope.problemReporter().codeSnippetMissingMethod(ROOT_FULL_CLASS_NAME, new String(SETRESULT_SELECTOR), new String(SETRESULT_ARGUMENTS), this.sourceStart, this.sourceEnd);
				return;
			}
			// in constant case, the implicit conversion cannot be left uninitialized
			if (this.expression.constant != Constant.NotAConstant) {
				// fake 'no implicit conversion' (the return type is always void)
				this.expression.implicitConversion = this.expression.constant.typeID() << 4;
			}
		}
	}
}
public void setActualReceiverType(ReferenceBinding receiverType) {
	// ignored
}
public void setDepth(int depth) {
	// ignored
}
public void setFieldIndex(int depth) {
	// ignored
}
}
