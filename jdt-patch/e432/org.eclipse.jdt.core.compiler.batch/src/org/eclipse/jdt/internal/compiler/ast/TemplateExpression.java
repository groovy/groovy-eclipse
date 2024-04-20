/*******************************************************************************
 * Copyright (c) 2023, 2024 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class TemplateExpression extends Expression {
	public Expression processor;
	public StringTemplate template;
	private MessageSend invocation;
	public TemplateExpression(Expression processor, StringTemplate template) {
		this.processor = processor;
		this.template = template;
		this.sourceStart = processor.sourceStart;
		this.sourceEnd = template.sourceEnd;
	}
	@Override
	public StringBuilder printExpression(int indent, StringBuilder output) {
		this.processor.printExpression(0, output);
		output.append("."); //$NON-NLS-1$
		this.template.printExpression(0, output);
		return output;
	}

	@Override
	public TypeBinding resolveType(BlockScope scope) {
		this.constant = Constant.NotAConstant;
		this.template.resolve(scope);
		if (this.processor != null) {
			this.invocation = new MessageSend();
			this.invocation.receiver = this.processor;
			this.invocation.selector = ConstantPool.PROCESS;
			this.invocation.arguments = new Expression[] {this.template};
			this.invocation.resolve(scope);
			if (this.invocation.binding != null) {
				this.resolvedType = this.invocation.binding.returnType;
			}
			ReferenceBinding processorBinding = scope.getJavaLangStringTemplateProcessor();
			if (this.processor.resolvedType == null) {
				// already reported an error. Just return.
				return this.resolvedType;
			}
			if (!this.processor.resolvedType.isCompatibleWith(processorBinding)) {
				scope.problemReporter().typeMismatchError(this.processor.resolvedType, processorBinding, this.processor, null);
				return this.resolvedType;
			}
		}
		return this.resolvedType;
	}
	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		return this.invocation.analyseCode(currentScope, flowContext, flowInfo);
	}
	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		this.invocation.generateCode(currentScope, codeStream, true);
		codeStream.checkcast(this.invocation.binding.returnType);
		if (!valueRequired) {
			codeStream.pop();
		} else {
			codeStream.generateImplicitConversion(this.implicitConversion);
		}
	}
	@Override
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			this.template.traverse(visitor, scope);
			if (this.processor != null)
				this.processor.traverse(visitor, scope);
			if (this.invocation != null)
				this.invocation.traverse(visitor, scope);
		}
	}
}
