/*******************************************************************************
 * Copyright (c) 2022, 2025 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.InferenceContext18;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.RecordComponentBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public class RecordPattern extends Pattern {

	public Pattern[] patterns;
	public TypeReference type;

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
	public void setIsEitherOrPattern() {
		for (Pattern p : this.patterns) {
			p.setIsEitherOrPattern();
		}
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
		for (Pattern p : this.patterns) {
			 flowInfo = p.analyseCode(currentScope, flowContext, flowInfo);
		}
		return flowInfo;
	}

	@Override
	public boolean coversType(TypeBinding t, Scope scope) {

		if (!isUnguarded())
			return false;

		if (TypeBinding.equalsEquals(t, this.resolvedType)) {
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
			if (!p.coversType(componentBinding.type, scope)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public TypeBinding resolveType(BlockScope scope) {

		this.constant = Constant.NotAConstant;
		if (this.resolvedType != null)
			return this.resolvedType;

		this.type.bits |= ASTNode.IgnoreRawTypeCheck;
		this.resolvedType = this.type.resolveType(scope);

		if (this.resolvedType == null || !this.resolvedType.isRecord()) {
			scope.problemReporter().unexpectedTypeinRecordPattern(this.type);
			return this.resolvedType;
		}
		if (this.resolvedType.components().length != this.patterns.length) {
			scope.problemReporter().recordPatternSignatureMismatch(this.resolvedType, this);
			return this.resolvedType = null;
		}

		if (this.resolvedType.isRawType()) {
			if (this.outerExpressionType instanceof ReferenceBinding) {
				ReferenceBinding binding = inferRecordParameterization(scope, (ReferenceBinding) this.outerExpressionType);
				if (binding == null || !binding.isValidBinding()) {
					scope.problemReporter().cannotInferRecordPatternTypes(this);
				    return this.resolvedType = null;
				}
				this.resolvedType = binding.capture(scope, this.sourceStart, this.sourceEnd);
			}
		}

		RecordComponentBinding[] componentBindings = this.resolvedType.components();
		LocalVariableBinding [] bindings = NO_VARIABLES;
		for (int i = 0, l = this.patterns.length; i < l; ++i) {
			Pattern p = this.patterns[i];
			p.setOuterExpressionType(componentBindings[i].type);
			p.resolveTypeWithBindings(bindings, scope);
			bindings = LocalVariableBinding.merge(bindings, p.bindingsWhenTrue());
		}

		if (this.resolvedType == null || !this.resolvedType.isValidBinding()) {
			return this.resolvedType;
		}

		this.isTotalTypeNode = super.coversType(this.resolvedType, scope);
		RecordComponentBinding[] components = this.resolvedType.capture(scope, this.sourceStart, this.sourceEnd).components();
		for (int i = 0; i < components.length; i++) {
			Pattern p1 = this.patterns[i];
			RecordComponentBinding componentBinding = components[i];
			if (p1 instanceof TypePattern tp) {
				if (tp.getType() == null || tp.getType().isTypeNameVar(scope)) {
					if (tp.local.binding != null) // rewrite with the inferred type
						tp.local.binding.type = componentBinding.type;
				}
			}
			TypeBinding componentType = componentBinding.type;
			if (p1.isApplicable(componentType, scope, p1)) {
				p1.isTotalTypeNode = p1.coversType(componentType, scope);
				MethodBinding[] methods = this.resolvedType.getMethods(componentBinding.name);
				if (methods != null && methods.length > 0) {
					p1.accessorMethod = methods[0];
				}
			}
			this.isTotalTypeNode &= p1.isTotalTypeNode;
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
	public boolean dominates(Pattern p) {
		/* 14.30.3: A record pattern with type R and pattern list L dominates another record pattern
		   with type S and pattern list M if (i) R and S name the same record class, and (ii)
		   every component pattern, if any, in L dominates the corresponding component
		   pattern in M.
		*/
		if (!isUnguarded())
			return false;

		if (this.resolvedType == null || !this.resolvedType.isValidBinding() || p.resolvedType == null || !p.resolvedType.isValidBinding())
			return false;

		if (TypeBinding.notEquals(this.resolvedType.erasure(), p.resolvedType.erasure()))
			return false;

		if (!this.resolvedType.erasure().isRecord())
			return false;

		if (p instanceof RecordPattern) {
			RecordPattern rp = (RecordPattern) p;
			if (this.patterns.length != rp.patterns.length)
				return false;
			for (int i = 0, length = this.patterns.length; i < length; i++) {
				if (!this.patterns[i].dominates(rp.patterns[i])) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream, BranchLabel patternMatchLabel, BranchLabel matchFailLabel) {

		int length = this.patterns.length;
		/* JVM Stack on entry - [expression] // && expression instanceof this.resolvedType
		   JVM stack on exit with successful pattern match or failed match -> []
		   Notation: 'R' : record pattern, 'C': component
		 */
		if (length == 0) {
			codeStream.pop();
			return;
		}
		codeStream.checkcast(this.resolvedType); // [R]
		List<ExceptionLabel> labels = new ArrayList<>();
		for (int i = 0; i < length; i++) {
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

			TypeBinding componentType = p.accessorMethod.returnType;
			checkForPrimitiveType(currentScope, p, componentType);
			if (TypeBinding.notEquals(p.accessorMethod.original().returnType.erasure(),
					componentType.erasure()))
				codeStream.checkcast(componentType); // lastComponent ? [C] : [R, C]
			if (p instanceof RecordPattern || !p.isTotalTypeNode) {
				if (!p.isUnnamed())
					codeStream.dup(componentType); // lastComponent ? named ? ([C, C] : [R, C, C]) : ([C] : [R, C])
				if (p instanceof TypePattern) {
					((TypePattern) p).generateTypeCheck(currentScope, codeStream);
				} else {
					codeStream.instance_of(p.resolvedType); // lastComponent ? named ? ([C, boolean] : [R, C, boolean]) : ([boolean] : [R, boolean])
				}
				BranchLabel innerTruthLabel = new BranchLabel(codeStream);
				codeStream.ifne(innerTruthLabel); // lastComponent ? named ? ([C] : [R, C]) : ([] : [R])
				int pops = p.isUnnamed() ? 0 : TypeIds.getCategory(componentType.id); // Not going to store into the component pattern binding, so need to pop, the duped value.
				Pattern current = p;
				RecordPattern outer = this;
				while (outer != null) {
					if (current.index != outer.patterns.length - 1)
						pops++;
					current = outer;
					outer = outer.getEnclosingPattern();
				}
				while (pops > 1) {
					codeStream.pop2();
					pops -= 2;
				}
				if (pops > 0)
					codeStream.pop();

				codeStream.goto_(matchFailLabel);
				innerTruthLabel.place();
			}
			p.generateCode(currentScope, codeStream, patternMatchLabel, matchFailLabel);
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
	}

	private void checkForPrimitiveType(BlockScope currentScope, Pattern p, TypeBinding componentType) {
		if (p.isTotalTypeNode && !componentType.isPrimitiveType() &&  p instanceof TypePattern tp) {
			TypeBinding providedType = tp.resolvedType;
			if (providedType != null && providedType.isPrimitiveType()) {
				PrimitiveConversionRoute route = findPrimitiveConversionRoute(componentType, providedType, currentScope);
				if (route != PrimitiveConversionRoute.NO_CONVERSION_ROUTE
						|| !componentType.isPrimitiveType()) {
					p.isTotalTypeNode = false;
				}
			}
		}
	}

	@Override
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			this.type.traverse(visitor, scope);
			for (Pattern p : this.patterns) {
				p.traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}

	@Override
	public StringBuilder printExpression(int indent, StringBuilder output) {
		output.append(this.type).append('(');
		for (int i = 0; i < this.patterns.length; i++) {
			if (i > 0) output.append(", "); //$NON-NLS-1$
			this.patterns[i].print(0, output);
		}
		output.append(')');
		return output;
	}
}