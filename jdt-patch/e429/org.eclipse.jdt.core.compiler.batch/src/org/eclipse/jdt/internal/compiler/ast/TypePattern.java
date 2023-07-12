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

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.RecordComponentBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBindingVisitor;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

public class TypePattern extends Pattern {

	public LocalDeclaration local;
	Expression expression;
	public int index = -1; // denoting position

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
	public void collectPatternVariablesToScope(LocalVariableBinding[] variables, BlockScope scope) {
		if (this.resolvedType == null) {
			this.resolveType(scope);
		}
		if (this.local != null && this.local.binding != null) {
			LocalVariableBinding binding = this.local.binding;
			if (variables != null) {
				for (LocalVariableBinding variable : variables) {
					if (variable == binding) continue; // Shouldn't happen
					if (CharOperation.equals(binding.name, variable.name)) {
						scope.problemReporter().redefineLocal(this.local);
					}
				}
			}
			if (this.patternVarsWhenTrue == null) {
				this.patternVarsWhenTrue = new LocalVariableBinding[1];
				this.patternVarsWhenTrue[0] = binding;
			} else {
				LocalVariableBinding[] vars = new LocalVariableBinding[1];
				vars[0] = binding;
				this.addPatternVariablesWhenTrue(vars);
			}
		}
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
		if (this.local != null) {
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
		return flowInfo;
	}
	@Override
	public void generateOptimizedBoolean(BlockScope currentScope, CodeStream codeStream, BranchLabel trueLabel, BranchLabel falseLabel) {
		if (this.local != null) {
			LocalVariableBinding localBinding = this.local.binding;
			if (!this.isTotalTypeNode) {
				codeStream.checkcast(localBinding.type);
			}
			this.local.generateCode(currentScope, codeStream);
			codeStream.store(localBinding, false);
			localBinding.recordInitializationStartPC(codeStream.position);
		}
	}
	public void initializePatternVariables(BlockScope currentScope, CodeStream codeStream) {
		codeStream.addVariable(this.secretPatternVariable);
		codeStream.store(this.secretPatternVariable, false);
	}
	@Override
	protected void generatePatternVariable(BlockScope currentScope, CodeStream codeStream, BranchLabel trueLabel, BranchLabel falseLabel) {
		if (this.local != null) {
			codeStream.load(this.secretPatternVariable);
			LocalVariableBinding localBinding = this.local.binding;
			if (!this.isTotalTypeNode)
				codeStream.checkcast(localBinding.type);
			this.local.generateCode(currentScope, codeStream);
			codeStream.store(localBinding, false);
			localBinding.recordInitializationStartPC(codeStream.position);
		}
	}
	@Override
	public void wrapupGeneration(CodeStream codeStream) {
		codeStream.removeVariable(this.secretPatternVariable);
	}
	@Override
	public LocalDeclaration getPatternVariable() {
		return this.local;
	}
	@Override
	public void resolveWithExpression(BlockScope scope, Expression exp) {
		this.expression = exp;
	}
	@Override
	public void resolve(BlockScope scope) {
		this.resolveType(scope);
	}
	@Override
	public boolean isTotalForType(TypeBinding type) {
		if (type == null || this.resolvedType == null)
			return false;
		return (type.isSubtypeOf(this.resolvedType, false));
	}
	@Override
	protected boolean isPatternTypeCompatible(TypeBinding other, BlockScope scope) {
		TypeBinding patternType = this.resolvedType;
		if (patternType.isBaseType()) {
			if (!TypeBinding.equalsEquals(other, patternType)) {
				scope.problemReporter().incompatiblePatternType(this, other, patternType);
				return false;
			}
		} else if (!checkCastTypesCompatibility(scope, other, patternType, this.expression, true)) {
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

	/*
	 * A type pattern, p, declaring a pattern variable x of type T, that is total for U,
	 * is resolved to an any pattern that declares x of type T;
	 * otherwise it is resolved to p.
	 */
	@Override
	public TypeBinding resolveAtType(BlockScope scope, TypeBinding u) {
		if (this.resolvedType == null) {
			this.resolvedType = this.local.binding.type;
		}
		return this.resolvedType;
	}
	@Override
	public TypeBinding resolveType(BlockScope scope, boolean isPatternVariable) {
		if (this.resolvedType != null)
			return this.resolvedType;
		if (this.local != null) {
			this.local.modifiers |= ExtraCompilerModifiers.AccPatternVariable;
//			this.local.resolve(scope, isPatternVariable);

			if (this.local.isTypeNameVar(scope) ) {
				/* If the LocalVariableType is var then the pattern variable must appear in
				 *  a pattern list of a record pattern with type R. Let T be the type of the
				 *  corresponding component field in R. The type of the pattern variable is
				 *  the upward projection of T with respect to all synthetic type variables
				 *  mentioned by T.*/
				Pattern enclosingPattern = this.getEnclosingPattern();
				if (enclosingPattern instanceof RecordPattern) {
					ReferenceBinding recType = (ReferenceBinding) enclosingPattern.resolvedType;
					if (recType != null) {
						RecordComponentBinding[] components = recType.components();
						if (components.length > this.index) {
							RecordComponentBinding rcb = components[this.index];
							TypeVariableBinding[] mentionedTypeVariables = findSyntheticTypeVariables(rcb.type);
							if  (mentionedTypeVariables != null && mentionedTypeVariables.length > 0) {
								this.local.type.resolvedType = recType.upwardsProjection(scope, mentionedTypeVariables);
							} else {
								this.local.type.resolvedType = rcb.type;
							}
						}
					}
				}
			}
			this.local.resolve(scope, isPatternVariable);
			if (this.local.binding != null) {
				this.local.binding.modifiers |= ExtraCompilerModifiers.AccPatternVariable;
				this.local.binding.useFlag = LocalVariableBinding.USED;
				this.resolvedType = this.local.binding.type;
			}
			initSecretPatternVariable(scope);
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
	protected void initSecretPatternVariable(BlockScope scope) {
		LocalVariableBinding l =
				this.secretPatternVariable =
						new LocalVariableBinding(
							SECRET_PATTERN_VARIABLE_NAME,
							this.resolvedType,
							ClassFileConstants.AccDefault,
							false);
				l.setConstant(Constant.NotAConstant);
				l.useFlag = LocalVariableBinding.USED;
				scope.addLocalVariable(l);
	}

	@Override
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.local != null)
				this.local.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}

	@Override
	public StringBuffer printExpression(int indent, StringBuffer output) {
		return this.local != null ? this.local.printAsExpression(indent, output) : output;
	}
}
