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

import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.util.Util;

public class StringTemplate extends Expression {
	private StringLiteral[] fragments;
	private Expression[] values;
	public boolean isMultiline;
	public StringTemplate(StringLiteral[] fragments, Expression[] values, int start, int end, boolean isMultiline) {
		this.fragments = fragments;
		this.values = values;
		this.sourceStart = start;
		this.sourceEnd = end;
		this.isMultiline = isMultiline;
	}

    public StringLiteral[] fragments() {
		return this.fragments;
    }
    public Expression[] values() {
		return this.values;
    }
	@Override
	public void resolve(BlockScope scope) {
		for (StringLiteral frag : this.fragments) {
			frag.resolveType(scope);
		}
		for (Expression exp : this.values) {
			if (exp == null) {
				continue;
			}
			exp.resolveType(scope);
		}
	}

	@Override
	public TypeBinding resolveType(BlockScope scope) {
		this.constant = Constant.NotAConstant;
		return this.resolvedType = scope.getJavaLangStringTemplate();
	}

	private void generateNewTemplateBootstrap(CodeStream codeStream) {
		int index = codeStream.classFile.recordBootstrapMethod(this);
		// Kludge, see if this can be moved to CodeStream
		codeStream.stackDepth ++;
		if (codeStream.stackDepth > codeStream.stackMax) {
			codeStream.stackMax = codeStream.stackDepth;
		}
		StringBuilder signature = new StringBuilder("("); //$NON-NLS-1$
		for (Expression exp : this.values) {
			TypeBinding type = exp.resolvedType;
			if (type == TypeBinding.NULL)
				signature.append(ConstantPool.JavaLangObjectSignature);
			else
				signature.append(type.signature());
		}
		signature.append(")Ljava/lang/StringTemplate;"); //$NON-NLS-1$
		codeStream.invokeDynamic(index,
				1, //
				1, // int
				ConstantPool.PROCESS,
				signature.toString().toCharArray(),
				TypeIds.T_int,
				TypeBinding.INT);
	}
	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		for (Expression exp : this.values) {
			flowInfo = exp.analyseCode(currentScope, flowContext, flowInfo).unconditionalInits();
		}
		return flowInfo;
	}
	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		for (Expression exp : this.values) {
			exp.generateCode(currentScope, codeStream, valueRequired);
		}

		generateNewTemplateBootstrap(codeStream);
		int pc = codeStream.position;
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}
	@Override
	public StringBuilder printExpression(int indent, StringBuilder output) {
		int length = this.fragments.length;
		output.append('\"');
		if (this.isMultiline)
			output.append("\"\"\n"); //$NON-NLS-1$
		for (int i = 0; i < length; i++) {
			char[] source = this.fragments[i].source();
			for (int j = 0; j < source.length; j++) {
				Util.appendEscapedChar(output, source[j], true);
			}
			if (i + 1 < length) {
				output.append("\\{"); //$NON-NLS-1$
				if (this.values[i] != null) {
					this.values[i].printExpression(0, output);
				}
				output.append("}"); //$NON-NLS-1$
			}
		}
		output.append('\"');
		if (this.isMultiline)
			output.append("\"\""); //$NON-NLS-1$
		return output;
	}
}
