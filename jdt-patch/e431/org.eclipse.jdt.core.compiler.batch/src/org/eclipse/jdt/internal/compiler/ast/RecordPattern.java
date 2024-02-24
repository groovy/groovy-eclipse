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
import java.util.List;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.ExceptionLabel;
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

	/* package */ BranchLabel guardedElseTarget;

	private TypeBinding expectedType; // for record pattern type inference

	public RecordPattern(TypeReference type, int sourceStart, int sourceEnd) {
		this.type = type;
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
	}
	@Override
	public TypeReference getType() {
		return this.type;
	}
	@Override
	public boolean checkUnsafeCast(Scope scope, TypeBinding castType, TypeBinding expressionType, TypeBinding match, boolean isNarrowing) {
		if (!castType.isReifiable())
			return CastExpression.checkUnsafeCast(this, scope, castType, expressionType, match, isNarrowing);
		else
			return super.checkUnsafeCast(scope, castType, expressionType, match, isNarrowing);
	}
	@Override
	public LocalVariableBinding[] bindingsWhenTrue() {
		LocalVariableBinding [] variables = NO_VARIABLES;
		for (Pattern p : this.patterns) {
			variables = LocalVariableBinding.merge(variables, p.bindingsWhenTrue());
		}
		return variables;
	}
	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		this.thenInitStateIndex1 = currentScope.methodScope().recordInitializationStates(flowInfo);
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
	public void setExpectedType(TypeBinding expectedType) {
		this.expectedType = expectedType;
	}

	@Override
	public TypeBinding expectedType() {
		return this.expectedType;
	}

	@Override
	public TypeBinding resolveType(BlockScope scope) {

		if (this.resolvedType != null)
			return this.resolvedType;

		this.type.bits |= ASTNode.IgnoreRawTypeCheck;
		this.resolvedType = this.type.resolveType(scope);

		if (!this.resolvedType.isRecord()) {
			scope.problemReporter().unexpectedTypeinRecordPattern(this.resolvedType, this.type);
			return this.resolvedType;
		}
		if (this.resolvedType.components().length != this.patterns.length) {
			scope.problemReporter().recordPatternSignatureMismatch(this.resolvedType, this);
			return this.resolvedType = null;
		}

		LocalVariableBinding [] bindings = NO_VARIABLES;
		for (Pattern p : this.patterns) {
			p.resolveTypeWithBindings(bindings, scope);
			bindings = LocalVariableBinding.merge(bindings, p.bindingsWhenTrue());
		}
		for (LocalVariableBinding binding : bindings)
			binding.useFlag = LocalVariableBinding.USED; // syntactically required even if untouched

		if (this.resolvedType.isRawType()) {
			TypeBinding expressionType = expectedType();
			if (expressionType instanceof ReferenceBinding) {
				ReferenceBinding binding = inferRecordParameterization(scope, (ReferenceBinding) expressionType);
				if (binding == null || !binding.isValidBinding()) {
					scope.problemReporter().cannotInferRecordPatternTypes(this);
				    return this.resolvedType = null;
				}
				this.resolvedType = binding;
			}
		}

		if (this.resolvedType == null || !this.resolvedType.isValidBinding()) {
			return this.resolvedType;
		}

		this.isTotalTypeNode = super.coversType(this.resolvedType);
		RecordComponentBinding[] components = this.resolvedType.capture(scope, this.sourceStart, this.sourceEnd).components();
		for (int i = 0; i < components.length; i++) {
			Pattern p1 = this.patterns[i];
			if (p1 instanceof TypePattern tp) {
				RecordComponentBinding componentBinding = components[i];
				if (p1.getType() == null || p1.getType().isTypeNameVar(scope)) {
					if (tp.local.binding != null) // rewrite with the inferred type
						tp.local.binding.type = componentBinding.type;
				}
				TypeBinding expressionType = componentBinding.type;
				if (p1.isPatternTypeCompatible(expressionType, scope)) {
					p1.isTotalTypeNode = p1.coversType(componentBinding.type);
					MethodBinding[] methods = this.resolvedType.getMethods(componentBinding.name);
					if (methods != null && methods.length > 0) {
						p1.accessorMethod = methods[0];
					}
				}
				this.isTotalTypeNode &= p1.isTotalTypeNode;
			}
		}
		return this.resolvedType;
	}
	private ReferenceBinding inferRecordParameterization(BlockScope scope, ReferenceBinding proposedMatchingType) {
		InferenceContext18 freshInferenceContext = new InferenceContext18(scope);
		try {
			return freshInferenceContext.inferRecordPatternParameterization(this, scope, proposedMatchingType);
		} finally {
			freshInferenceContext.cleanUp();
		}
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

	@Override
	public void generateOptimizedBoolean(BlockScope currentScope, CodeStream codeStream, BranchLabel trueLabel, BranchLabel falseLabel) {

		/* JVM Stack on entry - [expression] // && expression instanceof this.resolvedType
		   JVM stack on exit with successful pattern match or failed match -> []
		   Notation: 'R' : record pattern, 'C': component
		 */
		codeStream.checkcast(this.resolvedType); // [R]
		List<ExceptionLabel> labels = new ArrayList<>();
		for (int i = 0, length = this.patterns.length; i < length; i++) {
			Pattern p = this.patterns[i];
			/* For all but the last component, dup the record instance to use
			   as receiver for accessor invocation. The last component uses the
			   original record instance as receiver - leaving the stack drained.
			 */
			boolean lastComponent = i == length - 1;
			if (!lastComponent)
				codeStream.dup(); //  lastComponent ? [R] : [R, R]
			ExceptionLabel exceptionLabel = new ExceptionLabel(codeStream,
					TypeBinding.wellKnownType(currentScope, T_JavaLangThrowable));
			exceptionLabel.placeStart();
			codeStream.invoke(Opcodes.OPC_invokevirtual, p.accessorMethod.original(), this.resolvedType, null);
			// lastComponent ? [C] : [R, C]
			exceptionLabel.placeEnd();
			labels.add(exceptionLabel);

			if (TypeBinding.notEquals(p.accessorMethod.original().returnType.erasure(),
					p.accessorMethod.returnType.erasure()))
				codeStream.checkcast(p.accessorMethod.returnType); // lastComponent ? [C] : [R, C]
			if (p instanceof RecordPattern || !p.isTotalTypeNode) {
				codeStream.dup(); // lastComponent ? [C, C] : [R, C, C]
				codeStream.instance_of(p.resolvedType); // lastComponent ? [C, boolean] : [R, C, boolean]
				BranchLabel target = falseLabel != null ? falseLabel : new BranchLabel(codeStream);
				BranchLabel innerTruthLabel = new BranchLabel(codeStream);
				codeStream.ifne(innerTruthLabel); // lastComponent ? [C] : [R, C]
				int pops = 1; // Not going to store into the component pattern binding, so need to pop, the duped value.
				Pattern current = p;
				RecordPattern outer = this;
				while (outer != null) {
					if (current.index != outer.patterns.length - 1)
						pops++;
					current = outer;
					outer = outer.getEnclosingPattern() instanceof RecordPattern rp ? rp : null;
				}
				while (pops > 1) {
					codeStream.pop2();
					pops -= 2;
				}
				if (pops > 0)
					codeStream.pop();

				codeStream.goto_(target);
				innerTruthLabel.place();
			}
			p.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel);
		}

		if (labels.size() > 0) {
			BlockScope trapScope = codeStream.accessorExceptionTrapScopes.peek();
			List<ExceptionLabel> eLabels = codeStream.patternAccessorMap.get(trapScope);
			if (eLabels == null || eLabels.isEmpty()) {
				eLabels = labels;
			} else {
				eLabels.addAll(labels);
			}
			codeStream.patternAccessorMap.put(trapScope, eLabels);
		}
		if (this.thenInitStateIndex2 != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.thenInitStateIndex2);
			codeStream.addDefinitelyAssignedVariables(currentScope, this.thenInitStateIndex2);
		}
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
		if (visitor.visit(this, scope)) {
			if (this.type != null) {
				this.type.traverse(visitor, scope);
			}
			for (Pattern p : this.patterns) {
				p.traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}

	@Override
	public StringBuilder printExpression(int indent, StringBuilder output) {
		output.append(this.type).append('(');
		if (this.patterns != null) {
			for (int i = 0; i < this.patterns.length; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				this.patterns[i].print(0, output);
			}
		}
		output.append(')');
		return output;
	}
}
