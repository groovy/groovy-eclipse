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

import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public abstract class Pattern extends Expression {

	boolean isTotalTypeNode = false;

	private Pattern enclosingPattern;

	protected MethodBinding accessorMethod;

	public int index = -1; // index of this in enclosing record pattern, or -1 for top level patterns

	public boolean isUnguarded = true; // no guard or guard is compile time constant true.

	public Pattern getEnclosingPattern() {
		return this.enclosingPattern;
	}

	public void setEnclosingPattern(RecordPattern enclosingPattern) {
		this.enclosingPattern = enclosingPattern;
	}

	public boolean isUnnamed() {
		return false;
	}

	/**
	 * Implement the rules in the spec under 14.11.1.1 Exhaustive Switch Blocks
	 *
	 * @return whether pattern covers the given type or not
	 */
	public boolean coversType(TypeBinding type) {
		if (!isUnguarded())
			return false;
		if (type == null || this.resolvedType == null)
			return false;
		return (type.isSubtypeOf(this.resolvedType, false));
	}

	// Given a non-null instance of same type, would the pattern always match ?
	public boolean matchFailurePossible() {
		return false;
	}

	public boolean isUnconditional(TypeBinding t) {
		return isUnguarded() && coversType(t);
	}

	public abstract void generateCode(BlockScope currentScope, CodeStream codeStream, BranchLabel patternMatchLabel, BranchLabel matchFailLabel);

	@Override
	public boolean checkUnsafeCast(Scope scope, TypeBinding castType, TypeBinding expressionType, TypeBinding match, boolean isNarrowing) {
		if (!castType.isReifiable())
			return CastExpression.checkUnsafeCast(this, scope, castType, expressionType, match, isNarrowing);
		else
			return super.checkUnsafeCast(scope, castType, expressionType, match, isNarrowing);
	}

	public TypeReference getType() {
		return null;
	}

	// 14.30.3 Properties of Patterns: A pattern p is said to be applicable at a type T if ...
	protected boolean isApplicable(TypeBinding other, BlockScope scope) {
		TypeBinding patternType = this.resolvedType;
		if (patternType == null) // ill resolved pattern
			return false;
		// 14.30.3 Properties of Patterns doesn't allow boxing nor unboxing, primitive widening/narrowing.
		if (patternType.isBaseType() != other.isBaseType()) {
			scope.problemReporter().incompatiblePatternType(this, other, patternType);
			return false;
		}
		if (patternType.isBaseType()) {
			if (!TypeBinding.equalsEquals(other, patternType)) {
				scope.problemReporter().incompatiblePatternType(this, other, patternType);
				return false;
			}
		} else if (!checkCastTypesCompatibility(scope, other, patternType, null, true)) {
			scope.problemReporter().incompatiblePatternType(this, other, patternType);
			return false;
		}
		return true;
	}

	public abstract boolean dominates(Pattern p);

	@Override
	public StringBuilder print(int indent, StringBuilder output) {
		return this.printExpression(indent, output);
	}

	public Pattern[] getAlternatives() {
		return new Pattern [] { this };
	}

	public abstract void setIsEitherOrPattern(); // if set, is one of multiple (case label) patterns and so pattern variables can't be named.

	public boolean isUnguarded() {
		return this.isUnguarded;
	}

	public void setIsGuarded() {
		this.isUnguarded = false;
	}
}