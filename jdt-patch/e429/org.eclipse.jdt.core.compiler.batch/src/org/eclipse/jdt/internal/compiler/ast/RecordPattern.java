/*******************************************************************************
 * Copyright (c) 2022, 2023 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.InferenceContext18;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.RecordComponentBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class RecordPattern extends TypePattern {

	public Pattern[] patterns;
	public TypeReference type;
	int thenInitStateIndex1 = -1;
	int thenInitStateIndex2 = -1;

	public RecordPattern(LocalDeclaration local) {
		super(local);
		this.type = local.type;
		this.sourceStart = local.sourceStart;
		this.sourceEnd = local.sourceEnd;
	}
	public RecordPattern(TypeReference type, int sourceStart, int sourceEnd) {
		super();
		this.type = type;
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
	}
	@Override
	public TypeReference getType() {
		return this.type;
	}
 	@Override
	public void collectPatternVariablesToScope(LocalVariableBinding[] variables, BlockScope scope) {
		if (this.resolvedType == null) {
			this.resolveType(scope);
		}
		this.addPatternVariablesWhenTrue(variables);
		super.collectPatternVariablesToScope(variables, scope);
		for (Pattern p : this.patterns) {
			p.collectPatternVariablesToScope(this.patternVarsWhenTrue, scope);
			this.addPatternVariablesWhenTrue(p.patternVarsWhenTrue);
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
	public LocalDeclaration getPatternVariable() {
		return super.getPatternVariable();
	}
	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		this.thenInitStateIndex1 = currentScope.methodScope().recordInitializationStates(flowInfo);
		flowInfo = super.analyseCode(currentScope, flowContext, flowInfo);
		for (Pattern p : this.patterns) {
			 flowInfo = p.analyseCode(currentScope, flowContext, flowInfo);
		}
		flowInfo = flowInfo.safeInitsWhenTrue(); // TODO: is this really needed?
		this.thenInitStateIndex2 = currentScope.methodScope().recordInitializationStates(flowInfo);
		return flowInfo;
	}
	@Override
	public boolean isTotalForType(TypeBinding t) {
		return false;
	}
	@Override
	public void resolveWithExpression(BlockScope scope, Expression exp) {
		this.expression = exp;
		if (shouldInitiateRecordTypeInference()) {
			LocalVariableBinding localVariableBinding = exp.localVariableBinding();
			TypeBinding type1 = localVariableBinding != null && localVariableBinding.type != null ?
					localVariableBinding.type : exp.resolvedType;
			if (type1 instanceof ReferenceBinding) {
				ReferenceBinding binding = inferRecordParameterization(scope, (ReferenceBinding) type1);
				if (binding == null || !binding.isValidBinding()) {
					scope.problemReporter().cannotInferRecordPatternTypes(this);
				    this.resolvedType = null;
				    return;
				}
				this.resolvedType = binding;
				setAccessorsPlusInfuseInferredType(scope);
			} else {
				// TODO: which scenarios? eg? if found add the code here from resolveType();
			}
		}
	}
	@Override
	public TypeBinding resolveAtType(BlockScope scope, TypeBinding u) {
		for (Pattern p : this.patterns) {
			p.resolveAtType(scope, u);
		}
		if (this.local != null) {
			this.resolvedType = super.resolveAtType(scope, u);
		}
		return this.resolvedType;
	}
	@Override
	public TypeBinding resolveType(BlockScope scope, boolean isPatternVariable) {
		if (this.resolvedType != null)
			return this.resolvedType;
		super.resolveType(scope, isPatternVariable);

		if (this.local != null) {
			this.resolvedType = super.resolveType(scope);
		} else {
			this.type.bits |= ASTNode.IgnoreRawTypeCheck;
			this.resolvedType = this.type.resolveType(scope);
		}
		if (this.resolvedType == null) {
			// Probably called during collectPatternVariablesToScope()
			// and probably due to an error, this is unresolved.
			return null;
		}
		if (!this.resolvedType.isValidBinding())
			return this.resolvedType;

		initSecretPatternVariable(scope);

		// check whether the give type reference is a record
		// check whether a raw type is being used in pattern types
		// check whether the pattern signature matches that of the record declaration
		if (!this.resolvedType.isRecord()) {
			scope.problemReporter().unexpectedTypeinRecordPattern(this.resolvedType, this.type);
			return this.resolvedType;
		}
		setAccessorsPlusInfuseInferredType(scope);
		return this.resolvedType;
	}
	private void setAccessorsPlusInfuseInferredType(BlockScope scope) {
		this.isTotalTypeNode = isTotalForType(this.resolvedType);
		RecordComponentBinding[] components = this.resolvedType.components();
		if (components.length != this.patterns.length) {
			scope.problemReporter().recordPatternSignatureMismatch(this.resolvedType, this);
		} else {
			for (int i = 0; i < components.length; i++) {
				Pattern p = this.patterns[i];
				if (!(p instanceof TypePattern))
					continue;
				TypePattern tp = (TypePattern) p;
				RecordComponentBinding componentBinding = components[i];
				if (p.getType().isTypeNameVar(scope)) {
					infuseInferredType(tp, componentBinding);
					if (tp.local.binding != null) // rewrite with the inferred type
						tp.local.binding.type = componentBinding.type;
				}
				p.resolveType(scope, true);
				TypeBinding expressionType = componentBinding.type;
				if (p.isPatternTypeCompatible(expressionType, scope)) {
					p.isTotalTypeNode = p.isTotalForType(componentBinding.type);
					MethodBinding[] methods = this.resolvedType.getMethods(componentBinding.name);
					if (methods != null && methods.length > 0) {
						p.accessorMethod = methods[0];
					}
				}
			}
		}
	}
	private ReferenceBinding inferRecordParameterization(BlockScope scope, ReferenceBinding proposedMatchingType) {
		InferenceContext18 freshInferenceContext = new InferenceContext18(scope);
		try {
			return freshInferenceContext.inferRecordPatternParameterization(this, scope, proposedMatchingType);
		} finally {
			freshInferenceContext.cleanUp();
		}
	}
	private boolean shouldInitiateRecordTypeInference() {
		ReferenceBinding binding = this.resolvedType != null ? this.resolvedType.actualType() : null;
		if (binding == null || !binding.isGenericType())
			return false;

		if (this.resolvedType.isParameterizedType())
			return false;
		if (this.containsTypeElidedPatternVar == null) {
			this.containsPatternVariable();
		}
		return this.containsTypeElidedPatternVar;
	}
	private void infuseInferredType(TypePattern tp, RecordComponentBinding componentBinding) {
		SingleTypeReference ref = new SingleTypeReference(tp.local.type.getTypeName()[0],
				tp.local.type.sourceStart,
				tp.local.type.sourceEnd) {
			@Override
			public TypeBinding resolveType(BlockScope scope, boolean checkBounds) {
				return componentBinding.type;
			}
		};
		tp.local.type = ref;
	}
	@Override
	public boolean isAlwaysTrue() {
		return false;
	}
	@Override
	public boolean dominates(Pattern p) {
		if (!this.resolvedType.isValidBinding())
			return false;
		if (!super.isTotalForType(p.resolvedType)) {
			return false;
		}
		if (p instanceof RecordPattern) {
			RecordPattern rp = (RecordPattern) p;
			if (this.patterns.length != rp.patterns.length)
				return false;
			for(int i = 0; i < this.patterns.length; i++) {
				if (!this.patterns[i].dominates(rp.patterns[i])) {
					return false;
				}
			}
		}
		return true;
	}
	@Override
	public void generateOptimizedBoolean(BlockScope currentScope, CodeStream codeStream, BranchLabel trueLabel, BranchLabel falseLabel) {
		codeStream.checkcast(this.resolvedType);
		initializePatternVariables(currentScope, codeStream);
		generatePatternVariable(currentScope, codeStream, trueLabel, falseLabel);
		wrapupGeneration(codeStream);
		if (this.thenInitStateIndex2 != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.thenInitStateIndex2);
			codeStream.addDefinitelyAssignedVariables(currentScope, this.thenInitStateIndex2);
		}
	}
	@Override
	protected void generatePatternVariable(BlockScope currentScope, CodeStream codeStream, BranchLabel trueLabel, BranchLabel falseLabel) {
		if (!this.isTotalTypeNode) {
//			codeStream.load(this.secretPatternVariable);
//			codeStream.instance_of(this.resolvedType);
//			BranchLabel target = falseLabel != null ? falseLabel : new BranchLabel(codeStream);
//			codeStream.ifeq(target);
		}
		for (Pattern p : this.patterns) {
			if (p.accessorMethod != null) {
				codeStream.load(this.secretPatternVariable);
				if (!this.isTotalTypeNode)
					codeStream.checkcast(this.resolvedType);
				generateArguments(p.accessorMethod, null, currentScope, codeStream);
				codeStream.invoke(Opcodes.OPC_invokevirtual, p.accessorMethod.original(), this.resolvedType, null);
				if (!p.accessorMethod.original().equals(p.accessorMethod))
					codeStream.checkcast(p.accessorMethod.returnType);
				if (!p.isTotalTypeNode) {
					if (p instanceof TypePattern) {
						((TypePattern)p).initializePatternVariables(currentScope, codeStream);
						codeStream.load(p.secretPatternVariable);
						codeStream.instance_of(p.resolvedType);
						BranchLabel target = falseLabel != null ? falseLabel : new BranchLabel(codeStream);
						codeStream.ifeq(target);
						codeStream.load(p.secretPatternVariable);
					}
				}
				p.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel);
			}
		}
		super.generatePatternVariable(currentScope, codeStream, trueLabel, falseLabel);
	}
	@Override
	public void wrapupGeneration(CodeStream codeStream) {
		for (Pattern p : this.patterns) {
			p.wrapupGeneration(codeStream);
		}
		super.wrapupGeneration(codeStream);
	}
	@Override
	public void suspendVariables(CodeStream codeStream, BlockScope scope) {
		codeStream.removeNotDefinitelyAssignedVariables(scope, this.thenInitStateIndex1);
	}
	@Override
	public void resumeVariables(CodeStream codeStream, BlockScope scope) {
		codeStream.addDefinitelyAssignedVariables(scope, this.thenInitStateIndex2);
	}
	@Override
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		for (Pattern p : this.patterns) {
			visitor.visit(p, scope);
		}
		if (visitor.visit(this, scope)) {
			if (this.local != null)
				this.local.traverse(visitor, scope);
			else if (this.type != null) {
				this.type.traverse(visitor, scope);
			}
			for (Pattern p : this.patterns) {
				p.traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}

	@Override
	public StringBuffer printExpression(int indent, StringBuffer output) {
		output.append(this.type).append('(');
		if (this.patterns != null) {
			for (int i = 0; i < this.patterns.length; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				this.patterns[i].print(0, output);
			}
		}
		output.append(')');
		if (this.local != null)
			output.append(' ').append(this.local.name);
		return output;
	}
}
