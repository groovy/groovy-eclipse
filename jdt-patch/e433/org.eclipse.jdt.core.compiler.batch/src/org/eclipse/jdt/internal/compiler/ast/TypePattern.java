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
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.RecordComponentBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

public class TypePattern extends Pattern {

	public LocalDeclaration local;

	private boolean isEitherOrPattern = false;

	public TypePattern(LocalDeclaration local) {
		this.local = local;
	}

	public static TypePattern createTypePattern(LocalDeclaration lokal) {
		if (lokal.name.length == 1 && lokal.name[0] == '_') {
			return new TypePattern(lokal) {
				@Override
				public boolean isUnnamed() {
					return true;
				}
			};
		}
		return new TypePattern(lokal);
	}

	@Override
	public TypeReference getType() {
		return this.local.type;
	}

	@Override
	public void setIsEitherOrPattern() {
		this.isEitherOrPattern = true;
	}

	@Override
	public LocalVariableBinding[] bindingsWhenTrue() {
		return this.isUnnamed() || this.local.binding == null ? NO_VARIABLES : new LocalVariableBinding[] { this.local.binding };
	}

	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		flowInfo = this.local.analyseCode(currentScope, flowContext, flowInfo);
		FlowInfo patternInfo = flowInfo.copy();

		if (this.isUnnamed())
			return patternInfo; // exclude anonymous blokes from flow analysis.

		patternInfo.markAsDefinitelyAssigned(this.local.binding);
		if (!this.isTotalTypeNode) {
			// non-total type patterns create a nonnull local:
			patternInfo.markAsDefinitelyNonNull(this.local.binding);
		} else {
			// total type patterns inherit the nullness of the value being switched over, unless ...
			if (flowContext.associatedNode instanceof SwitchStatement swStmt) {
				int nullStatus = swStmt.containsNull
						? FlowInfo.NON_NULL // ... null is handled in a separate case
						: swStmt.expression.nullStatus(patternInfo, flowContext);
				patternInfo.markNullStatus(this.local.binding, nullStatus);
			}
		}
		return patternInfo;
	}

	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream, BranchLabel patternMatchLabel, BranchLabel matchFailLabel) {
		if (isUnnamed()) {
			if (this.getEnclosingPattern() == null || this.isTotalTypeNode) {
				switch (this.local.binding.type.id) {
					case T_long :
					case T_double :
						codeStream.pop2();
						break;
					default :
						codeStream.pop();
				}
			} // else we don't value on stack.
		} else {
			if (!this.isTotalTypeNode) {
				codeStream.checkcast(this.local.binding.type);
			}
			this.local.generateCode(currentScope, codeStream);
		}
	}

	@Override
	public boolean dominates(Pattern p) {
		if (!isUnguarded())
			return false;
		if (p.resolvedType == null || this.resolvedType == null)
			return false;
		return p.resolvedType.erasure().isSubtypeOf(this.resolvedType.erasure(), false);
	}

	@Override
	public TypeBinding resolveType(BlockScope scope) {
		if (this.resolvedType != null)
			return this.resolvedType;

		Pattern enclosingPattern = this.getEnclosingPattern();
		if (this.local.type == null || this.local.type.isTypeNameVar(scope)) {
			if (enclosingPattern instanceof RecordPattern) {
				// 14.30.1: The type of a pattern variable declared in a nested type pattern is determined as follows ...
				ReferenceBinding recType = (ReferenceBinding) enclosingPattern.resolvedType;
				if (recType != null) {
					RecordComponentBinding[] components = recType.components();
					if (components.length > this.index) {
						RecordComponentBinding rcb = components[this.index];
						if (rcb.type != null && (rcb.tagBits & TagBits.HasMissingType) != 0) {
							scope.problemReporter().invalidType(this, rcb.type);
						}
						TypeVariableBinding[] mentionedTypeVariables = rcb.type != null ? rcb.type.syntheticTypeVariablesMentioned() : Binding.NO_TYPE_VARIABLES;
						this.resolvedType = mentionedTypeVariables.length > 0 ? rcb.type.upwardsProjection(scope, mentionedTypeVariables) : rcb.type;
						if (this.local.type != null)
							this.local.type.resolvedType = this.resolvedType;
					}
				}
			}
		}
		this.local.resolve(scope, true);
		if (this.local.binding != null) {
			this.local.binding.modifiers |= ExtraCompilerModifiers.AccOutOfFlowScope; // start out this way, will be BlockScope.include'd when definitely assigned
			if (enclosingPattern != null)
				this.local.binding.useFlag = LocalVariableBinding.USED; // syntactically required even if untouched
			if (this.local.type != null)
				this.resolvedType = this.local.binding.type;
		}

		if (this.isEitherOrPattern && !this.isUnnamed()) {
			scope.problemReporter().namedPatternVariablesDisallowedHere(this.local);
		}

		return this.resolvedType;
	}

	@Override
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			this.local.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}

	@Override
	public StringBuilder printExpression(int indent, StringBuilder output) {
		return this.local.printAsExpression(indent, output);
	}
}
