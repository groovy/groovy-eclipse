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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.RecordComponentBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBindingVisitor;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

public class TypePattern extends Pattern {

	public LocalDeclaration local;

	public TypePattern(LocalDeclaration local) {
		this.local = local;
	}
	protected TypePattern() {
	}
	@Override
	public TypeReference getType() {
		return this.local.type;
	}
	@Override
	public LocalVariableBinding[] bindingsWhenTrue() {
		return this.local.binding == null ? NO_VARIABLES : new LocalVariableBinding[] { this.local.binding };
	}
	@Override
	public boolean checkUnsafeCast(Scope scope, TypeBinding castType, TypeBinding expressionType, TypeBinding match, boolean isNarrowing) {
		if (!castType.isReifiable())
			return CastExpression.checkUnsafeCast(this, scope, castType, expressionType, match, isNarrowing);
		else
			return super.checkUnsafeCast(scope, castType, expressionType, match, isNarrowing);
	}

	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		flowInfo = this.local.analyseCode(currentScope, flowContext, flowInfo);
		FlowInfo patternInfo = flowInfo.copy();
		patternInfo.markAsDefinitelyAssigned(this.local.binding);
		if (!this.isTotalTypeNode) {
			// non-total type patterns create a nonnull local:
			patternInfo.markAsDefinitelyNonNull(this.local.binding);
		} else {
			// total type patterns inherit the nullness of the value being switched over, unless ...
			if (flowContext.associatedNode instanceof SwitchStatement) {
				SwitchStatement swStmt = (SwitchStatement) flowContext.associatedNode;
				int nullStatus = swStmt.containsNull
						? FlowInfo.NON_NULL // ... null is handled in a separate case
						: swStmt.expression.nullStatus(patternInfo, flowContext);
				patternInfo.markNullStatus(this.local.binding, nullStatus);
			}
		}
		return patternInfo;
	}
	@Override
	public void generateOptimizedBoolean(BlockScope currentScope, CodeStream codeStream, BranchLabel trueLabel, BranchLabel falseLabel) {
		LocalVariableBinding localBinding = this.local.binding;
		if (!this.isTotalTypeNode) {
			codeStream.checkcast(localBinding.type);
		}
		this.local.generateCode(currentScope, codeStream);
		codeStream.store(localBinding, false);
		localBinding.recordInitializationStartPC(codeStream.position);
	}
	@Override
	public LocalDeclaration getPatternVariable() {
		return this.local;
	}

	@Override
	public boolean coversType(TypeBinding type) {
		if (type == null || this.resolvedType == null)
			return false;
		return (type.isSubtypeOf(this.resolvedType, false));
	}
	@Override
	protected boolean isPatternTypeCompatible(TypeBinding other, BlockScope scope) {
		TypeBinding patternType = this.resolvedType;
		if (patternType == null) // ill resolved pattern
			return false;
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
	@Override
	public boolean dominates(Pattern p) {
		if (p.resolvedType == null || this.resolvedType == null)
			return false;
		return p.resolvedType.erasure().isSubtypeOf(this.resolvedType.erasure(), false);
	}

	@Override
	public TypeBinding resolveType(BlockScope scope) {
		if (this.resolvedType != null)
			return this.resolvedType; // Srikanth, fix reentry

		this.local.modifiers |= ExtraCompilerModifiers.AccOutOfFlowScope;
		if (this.local.type == null || this.local.type.isTypeNameVar(scope)) {
			/*
			 * If the LocalVariableType is var then the pattern variable must appear in a pattern list of a
			 * record pattern with type R. Let T be the type of the corresponding component field in R. The type
			 * of the pattern variable is the upward projection of T with respect to all synthetic type
			 * variables mentioned by T.
			 */
			Pattern enclosingPattern = this.getEnclosingPattern();
			if (enclosingPattern instanceof RecordPattern) {
				ReferenceBinding recType = (ReferenceBinding) enclosingPattern.resolvedType;
				if (recType != null) {
					RecordComponentBinding[] components = recType.components();
					if (components.length > this.index) {
						RecordComponentBinding rcb = components[this.index];
						if (rcb.type != null && (rcb.tagBits & TagBits.HasMissingType) != 0) {
							scope.problemReporter().invalidType(this, rcb.type);
						}
						TypeVariableBinding[] mentionedTypeVariables = findSyntheticTypeVariables(rcb.type);
						if (mentionedTypeVariables != null && mentionedTypeVariables.length > 0) {
							this.local.type.resolvedType = recType.upwardsProjection(scope,
									mentionedTypeVariables);
						} else {
							if (this.local.type != null)
								this.local.type.resolvedType = rcb.type;
							this.resolvedType = rcb.type;
						}
					}
				}
			}
		}
		this.local.resolve(scope, true);
		if (this.local.binding != null) {
			this.local.binding.modifiers |= ExtraCompilerModifiers.AccOutOfFlowScope; // start out this way, will be BlockScope.include'd when definitely assigned
			this.local.binding.tagBits |= TagBits.IsPatternBinding;
			if (this.local.type != null)
				this.resolvedType = this.local.binding.type;
		}

		return this.resolvedType;
	}
	// Synthetics? Ref 4.10.5 also watch out for spec changes in rec pattern..
	private TypeVariableBinding[] findSyntheticTypeVariables(TypeBinding typeBinding) {
		final Set<TypeVariableBinding> mentioned = new HashSet<>();
		TypeBindingVisitor.visit(new TypeBindingVisitor() {
			@Override
			public boolean visit(TypeVariableBinding typeVariable) {
				if (typeVariable.isCapture())
					mentioned.add(typeVariable);
				return super.visit(typeVariable);
			}
		}, typeBinding);
		if (mentioned.isEmpty()) return null;
		return mentioned.toArray(new TypeVariableBinding[mentioned.size()]);
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
