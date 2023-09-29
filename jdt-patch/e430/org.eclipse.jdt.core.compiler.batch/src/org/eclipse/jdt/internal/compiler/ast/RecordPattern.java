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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.ExceptionLabel;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.InferenceContext18;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.RecordComponentBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public class RecordPattern extends TypePattern {

	public static final String SECRET_RECORD_PATTERN_THROWABLE_VARIABLE_NAME = " secretRecordPatternThrowableVariable"; //$NON-NLS-1$;

	public Pattern[] patterns;
	public TypeReference type;
	int thenInitStateIndex1 = -1;
	int thenInitStateIndex2 = -1;
	public LocalVariableBinding secretCaughtThrowableVariable = null;
	/* package */ BranchLabel guardedElseTarget;

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
	public boolean coversType(TypeBinding t) {
		if (TypeBinding.equalsEquals(t, this.resolvedType)) {
			// return the already computed value
			return this.isTotalTypeNode;
		}
		if (!t.isRecord())
			return false;
		RecordComponentBinding[] components = t.components();
		if (components == null || components.length != this.patterns.length) {
			return false;
		}
		for (int i = 0; i < components.length; i++) {
			Pattern p = this.patterns[i];
			RecordComponentBinding componentBinding = components[i];
			if (!p.coversType(componentBinding.type)) {
				return false;
			}
		}
		return true;
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

		getSecretVariable(scope, this.resolvedType);

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
		this.isTotalTypeNode = super.coversType(this.resolvedType);
		RecordComponentBinding[] components = this.resolvedType.capture(scope, this.sourceStart, this.sourceEnd).components();
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
					p.isTotalTypeNode = p.coversType(componentBinding.type);
					MethodBinding[] methods = this.resolvedType.getMethods(componentBinding.name);
					if (methods != null && methods.length > 0) {
						p.accessorMethod = methods[0];
					}
				}
				this.isTotalTypeNode &= p.isTotalTypeNode;
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
		return this.resolvedType != null && this.resolvedType.isRawType();
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
		if (!super.coversType(p.resolvedType)) {
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
	public static LocalVariableBinding getRecPatternCatchVar(int level, BlockScope parentScope) {
		if (level < 0)
			return null;
		String secret_name = RecordPattern.SECRET_RECORD_PATTERN_THROWABLE_VARIABLE_NAME + level;
		LocalVariableBinding l =
				new LocalVariableBinding(
					secret_name.toCharArray(),
					TypeBinding.wellKnownType(parentScope, TypeIds.T_JavaLangThrowable),
					ClassFileConstants.AccDefault,
					false);
		l.setConstant(Constant.NotAConstant);
		l.useFlag = LocalVariableBinding.USED;
		new BlockScope(parentScope).addLocalVariable(l);
		return l;
	}
	@Override
	public void generateOptimizedBoolean(BlockScope currentScope, CodeStream codeStream, BranchLabel trueLabel, BranchLabel falseLabel) {
//		codeStream.checkcast(this.resolvedType);
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
		List<ExceptionLabel> labels = new ArrayList<>();
		for (Pattern p : this.patterns) {
			if (p.accessorMethod != null) {
				codeStream.load(this.secretPatternVariable);
				codeStream.checkcast(this.resolvedType);

				ExceptionLabel exceptionLabel = new ExceptionLabel(codeStream,TypeBinding.wellKnownType(currentScope, T_JavaLangThrowable));
				exceptionLabel.placeStart();
				generateArguments(p.accessorMethod, null, currentScope, codeStream);
				codeStream.invoke(Opcodes.OPC_invokevirtual, p.accessorMethod.original(), this.resolvedType, null);
				exceptionLabel.placeEnd();
				labels.add(exceptionLabel);

				if (TypeBinding.notEquals(p.accessorMethod.original().returnType.erasure(),
						p.accessorMethod.returnType.erasure()))
					codeStream.checkcast(p.accessorMethod.returnType);
				if (p instanceof RecordPattern || !p.isTotalTypeNode) {
					((TypePattern)p).getSecretVariable(currentScope, p.resolvedType);
					((TypePattern)p).initializePatternVariables(currentScope, codeStream);
					codeStream.load(p.secretPatternVariable);
					codeStream.instance_of(p.resolvedType);
					BranchLabel target = falseLabel != null ? falseLabel : new BranchLabel(codeStream);
					List<LocalVariableBinding> deepPatternVars = getDeepPatternVariables(currentScope);
					recordEndPCDeepPatternVars(deepPatternVars, codeStream.position);
					codeStream.ifeq(target);
					recordStartPCDeepPatternVars(deepPatternVars, codeStream.position);
					p.secretPatternVariable.recordInitializationStartPC(codeStream.position);
					codeStream.load(p.secretPatternVariable);
					codeStream.removeVariable(p.secretPatternVariable);
				}
				p.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel);
			}
		}
		addExceptionToBlockScope(currentScope, codeStream, labels);
		super.generatePatternVariable(currentScope, codeStream, trueLabel, falseLabel);
	}

	List<LocalVariableBinding> getDeepPatternVariables(BlockScope blockScope) {
		class PatternVariableCollector extends ASTVisitor {
			Set<LocalVariableBinding> deepPatternVariables = new HashSet<>();
			@Override
			public boolean visit(Pattern pattern, BlockScope blockScope1) {
				if (pattern.secretPatternVariable != null)
					this.deepPatternVariables.add(pattern.secretPatternVariable);
				return true;
			}
			@Override
			public boolean visit(TypePattern typePattern, BlockScope blockScope1) {
				if (typePattern.secretPatternVariable != null)
					this.deepPatternVariables.add(typePattern.secretPatternVariable);
				LocalVariableBinding local1 = typePattern.local != null ? typePattern.local.binding : null;
				if (local1 != null && local1.initializationCount > 0)
					this.deepPatternVariables.add(typePattern.local.binding);
				return true;
			}
		}
		PatternVariableCollector pvc = new PatternVariableCollector();
		traverse(pvc, blockScope);
		pvc.deepPatternVariables.add(this.secretPatternVariable);
		return new ArrayList<>(pvc.deepPatternVariables);
	}
	private void recordEndPCDeepPatternVars(List<LocalVariableBinding> vars, int position) {
		if (vars == null)
			return;
		for (LocalVariableBinding v : vars) {
			if (v.initializationCount > 0)
				v.recordInitializationEndPC(position);
		}
	}
	private void recordStartPCDeepPatternVars(List<LocalVariableBinding> vars, int position) {
		if (vars == null)
			return;
		for (LocalVariableBinding v : vars) {
			v.recordInitializationStartPC(position);
		}
	}
	private void addExceptionToBlockScope(BlockScope currentScope, CodeStream codeStream, List<ExceptionLabel> labels) {
		if (currentScope == null || labels == null || labels.isEmpty())
			return;
		Predicate<Scope> pred = codeStream.patternCatchStack.isEmpty() ?
			 s -> s instanceof MethodScope :
				 s -> s == codeStream.patternCatchStack.firstElement();

		Scope scope = currentScope;
		while (scope != null) {
			if (pred.test(scope)) {
				List<ExceptionLabel> eLabels = codeStream.patternAccessorMap.get(scope);
				if (eLabels == null || eLabels.isEmpty()) {
					eLabels = labels;
				} else {
					eLabels.addAll(labels);
				}
				codeStream.patternAccessorMap.put((BlockScope) scope, eLabels);
				break;
			}
			scope = scope.parent;
		}
	}
	@Override
	public void initializePatternVariables(BlockScope currentScope, CodeStream codeStream) {
		super.initializePatternVariables(currentScope, codeStream);
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
