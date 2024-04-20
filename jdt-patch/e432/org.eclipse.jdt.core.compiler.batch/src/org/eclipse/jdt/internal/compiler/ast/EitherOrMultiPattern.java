/*******************************************************************************
* Copyright (c) 2024 Advantest Europe GmbH and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Srikanth Sankaran - initial implementation
*******************************************************************************/

package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class EitherOrMultiPattern extends Pattern {

	private Pattern [] patterns;
	private int patternsCount;

	public EitherOrMultiPattern(Pattern [] patterns) {
		this.patterns = patterns;
		this.patternsCount = patterns.length;
		this.sourceStart = patterns[0].sourceStart;
		this.sourceEnd = patterns[this.patternsCount - 1].sourceEnd;
		setIsEitherOrPattern();
	}

	@Override
	public Pattern[] getAlternatives() {
		return this.patterns;
	}

	@Override
	public void setIsEitherOrPattern() {
		for (int i = 0; i < this.patternsCount; i++)
			this.patterns[i].setIsEitherOrPattern();
	}

	@Override
	public void setIsGuarded() {
		for (int i = 0; i < this.patternsCount; i++)
			this.patterns[i].setIsGuarded();
	}

	@Override
	public TypeBinding resolveType(BlockScope scope) {
		boolean hasError = false;
		for (int i = 0; i < this.patternsCount; i++) {
			TypeBinding t = this.patterns[i].resolveType(scope);
			if (t == null || !t.isValidBinding())
				hasError = true;
		}
		return this.resolvedType = hasError ? null : this.patterns[0].resolvedType; // for now, we don't have a union type abstraction
	}

	@Override
	public boolean matchFailurePossible() {
		if (!isUnguarded())
			return true;
		for (Pattern p : this.patterns) {
			if (p.matchFailurePossible())
				return true;
		}
		return false;
	}

	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream, BranchLabel patternMatchLabel, BranchLabel matchFailLabel) {
		/* JVM Stack on entry - [expression] // && expression instanceof _one of the_ EitherOrMultiPattern, we don't know which one
		   JVM stack on exit with successful pattern match or failed match -> []
		 */
		BranchLabel [] checkNextLabel = new BranchLabel [this.patternsCount];
		for (int i = 0; i < this.patternsCount; i++) {
			checkNextLabel[i] = new BranchLabel(codeStream);
			Pattern p = this.patterns[i];
			codeStream.dup();
			codeStream.instance_of(p.resolvedType); // mandatory since we don't know which alternative instance we have on stack
			codeStream.ifeq(checkNextLabel[i]);
			codeStream.dup();
			this.patterns[i].generateCode(currentScope, codeStream, patternMatchLabel, checkNextLabel[i]);
			codeStream.pop();
			codeStream.goto_(patternMatchLabel);
			checkNextLabel[i].place();
		}
		codeStream.pop();
		codeStream.goto_(matchFailLabel);
	}

	@Override
	public boolean dominates(Pattern p) {
		if (!isUnguarded())
			return false;
		for (Pattern thiz : this.patterns) {
			if (thiz.dominates(p))
				return true;
		}
		return false;
	}

	@Override
	public boolean coversType(TypeBinding type) {
		if (!isUnguarded())
			return false;
		for (Pattern p : this.patterns) {
			if (p.coversType(type))
				return true;
		}
		return false;
	}

	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		return flowInfo;
	}

	@Override
	public LocalVariableBinding[] bindingsWhenTrue() {
		return NO_VARIABLES; // emphatically.
	}

	@Override
	public StringBuilder printExpression(int indent, StringBuilder output) {
		for (int i = 0; i < this.patternsCount; i++) {
			if (i > 0) output.append(", "); //$NON-NLS-1$
			this.patterns[i].print(0, output);
		}
		return output;
	}

	@Override
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			for (Pattern p : this.patterns) {
				p.traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}
}