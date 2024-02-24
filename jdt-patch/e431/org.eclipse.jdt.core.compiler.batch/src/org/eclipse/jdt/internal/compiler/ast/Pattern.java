/*******************************************************************************
 * Copyright (c) 2021, 2023 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public abstract class Pattern extends Expression {

	/* package */ boolean isTotalTypeNode = false;

	private Pattern enclosingPattern;
	protected MethodBinding accessorMethod;
	/* package */ BranchLabel elseTarget;
	/* package */ BranchLabel thenTarget;

	public int index = -1; // index of this in enclosing record pattern, or -1 for top level patterns

	@Override
	public boolean containsPatternVariable() {
		class PatternVariablesVisitor extends ASTVisitor {
			public boolean hasPatternVar = false;
			public boolean typeElidedVar =  false;

			@Override
			public boolean visit(TypePattern typePattern, BlockScope blockScope) {
				 this.hasPatternVar = typePattern.local != null;
				 this.typeElidedVar |= typePattern.getType() == null || typePattern.getType().isTypeNameVar(blockScope);
				 return !(this.hasPatternVar && this.typeElidedVar);
			}
 		}

		PatternVariablesVisitor pvv = new PatternVariablesVisitor();
		this.traverse(pvv, (BlockScope) null);
		return pvv.hasPatternVar;
	}

	/**
	 * @return the enclosingPattern
	 */
	public Pattern getEnclosingPattern() {
		return this.enclosingPattern;
	}

	/**
	 * @param enclosingPattern the enclosingPattern to set
	 */
	public void setEnclosingPattern(RecordPattern enclosingPattern) {
		this.enclosingPattern = enclosingPattern;
	}
	/**
	 * Implement the rules in the spec under 14.11.1.1 Exhaustive Switch Blocks
	 *
	 * @return whether pattern covers the given type or not
	 */
	public boolean coversType(TypeBinding type) {
		return false;
	}
	public boolean isAlwaysTrue() {
		return true;
	}
	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
		setTargets(codeStream);
		generateOptimizedBoolean(currentScope, codeStream, this.thenTarget, this.elseTarget);
	}
	/* package */ void setTargets(CodeStream codeStream) {
		if (this.elseTarget == null)
			this.elseTarget = new BranchLabel(codeStream);
		if (this.thenTarget == null)
			this.thenTarget = new BranchLabel(codeStream);
	}
	public void suspendVariables(CodeStream codeStream, BlockScope scope) {
		// nothing by default
	}
	public void resumeVariables(CodeStream codeStream, BlockScope scope) {
		// nothing by default
	}
	public abstract void generateOptimizedBoolean(BlockScope currentScope, CodeStream codeStream, BranchLabel trueLabel, BranchLabel falseLabel);

	public TypeReference getType() {
		return null;
	}

	protected abstract boolean isPatternTypeCompatible(TypeBinding other, BlockScope scope);

	public abstract boolean dominates(Pattern p);

	@Override
	public StringBuilder print(int indent, StringBuilder output) {
		return this.printExpression(indent, output);
	}
}
