/*******************************************************************************
 * Copyright (c) 2021, 2022 IBM Corporation and others.
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

import java.util.function.Supplier;

import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public abstract class Pattern extends Expression {

	/* package */ boolean isTotalTypeNode = false;
	static final char[] SECRET_PATTERN_VARIABLE_NAME = "secretPatternVariable".toCharArray(); //$NON-NLS-1$

	public LocalVariableBinding secretPatternVariable = null;

	protected MethodBinding accessorMethod;
	/* package */ BranchLabel elseTarget;
	/* package */ BranchLabel thenTarget;


	public boolean isTotalForType(TypeBinding type) {
		return false;
	}
	public TypeBinding resolveAtType(BlockScope scope, TypeBinding type) {
		return null;
	}
	@Override
	public TypeBinding resolveType(BlockScope scope) {
		return resolveType(scope, true);
	}
	public TypeBinding resolveType(BlockScope scope, boolean isPatternVariable) {
		return null;
	}
	public boolean isAlwaysTrue() {
		return true;
	}
	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
		if (this.elseTarget == null)
			this.elseTarget = new BranchLabel(codeStream);
		if (this.thenTarget == null)
			this.thenTarget = new BranchLabel(codeStream);
		generateOptimizedBoolean(currentScope, codeStream, this.thenTarget, this.elseTarget);
	}
	public void suspendVariables(CodeStream codeStream, BlockScope scope) {
		// nothing by default
	}
	public void resumeVariables(CodeStream codeStream, BlockScope scope) {
		// nothing by default
	}
	public abstract void generateOptimizedBoolean(BlockScope currentScope, CodeStream codeStream, BranchLabel trueLabel, BranchLabel falseLabel);
	protected abstract void generatePatternVariable(BlockScope currentScope, CodeStream codeStream, BranchLabel trueLabel, BranchLabel falseLabel);
	protected abstract void wrapupGeneration(CodeStream codeStream);

	public TypeReference getType() {
		return null;
	}
	public abstract void resolveWithExpression(BlockScope scope, Expression expression);

	public void setTargetSupplier(Supplier<BranchLabel> targetSupplier) {
		// default implementation does nothing
	}
	protected abstract boolean isPatternTypeCompatible(TypeBinding other, BlockScope scope);

	public abstract boolean dominates(Pattern p);

	public Pattern primary() {
		return this;
	}

	@Override
	public StringBuffer print(int indent, StringBuffer output) {
		return this.printExpression(indent, output);
	}
}
