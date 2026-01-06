/*******************************************************************************
 * Copyright (c) 2018, 2024 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Advantest R & D - Switch Expressions 2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import static org.eclipse.jdt.internal.compiler.ast.ExpressionContext.ASSIGNMENT_CONTEXT;
import static org.eclipse.jdt.internal.compiler.ast.ExpressionContext.INVOCATION_CONTEXT;
import static org.eclipse.jdt.internal.compiler.ast.ExpressionContext.VANILLA_CONTEXT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.PolyTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public class SwitchExpression extends SwitchStatement implements IPolyExpression {

	/* package */ TypeBinding expectedType;
	ExpressionContext expressionContext = VANILLA_CONTEXT;
	private int nullStatus = FlowInfo.UNKNOWN;
	public boolean jvmStackVolatile = false;
	static final char[] SECRET_YIELD_VALUE_NAME = " yieldValue".toCharArray(); //$NON-NLS-1$
	private List<LocalVariableBinding> typesOnStack;

	public Results results = new Results();

	class Results { // Abstraction to help with 15.28.1 determination of the type of a switch expression.

		private Set<Expression> rExpressions = new LinkedHashSet<>(4);
		private Set<TypeBinding> rTypes = new HashSet<>();

		// Result expressions aggregate classification - will be negated as and when the picture changes
		private boolean allUniform = true;
		private boolean allBoolean = true;
		private boolean allNumeric = true;
		private boolean allWellFormed = true; // true as long as result expression completely fail to resolve (resolvedType == null)

		private TypeBinding resultType() {

			if (!this.allWellFormed)
				return null;

			if (SwitchExpression.this.isPolyExpression())
				return resolveAsType(SwitchExpression.this.expectedType);

			if (this.allUniform) {
				TypeBinding uniformType = null;
				for (Expression rExpression : this.rExpressions)
					uniformType = uniformType == null ? rExpression.resolvedType : NullAnnotationMatching.moreDangerousType(uniformType, rExpression.resolvedType);
				return resolveAsType(uniformType);
			}

			if (this.allBoolean)
				return resolveAsType(TypeBinding.BOOLEAN);

			if (this.allNumeric) {
				for (TypeBinding type : TypeBinding.NUMERIC_TYPES) {
					TypeBinding result = switch (type.id) {
						case T_double, T_float, T_long, T_int -> this.rTypes.contains(type) ? type : null;
						case T_short, T_byte, T_char -> {
							if (this.rTypes.contains(type)) {
								if (type.id != T_char && this.rTypes.contains(TypeBinding.CHAR))
									yield TypeBinding.INT;
								for (Expression rExpression : this.rExpressions) {
									if (rExpression.resolvedType.id == T_int && rExpression.constant != Constant.NotAConstant && !rExpression.isConstantValueOfTypeAssignableToType(rExpression.resolvedType, type))
										yield TypeBinding.INT;
								}
								yield type;
							}
							yield null;
						}
						default -> throw new IllegalStateException("Unexpected control flow!"); //$NON-NLS-1$;
					};
					if (result != null)
						return resolveAsType(result);
				}
				throw new IllegalStateException("Unexpected control flow!"); //$NON-NLS-1$
			}
			// Non-uniform, non-boolean, non-numeric: Force to reference versions, compute lub and apply capture and we are done!
			LookupEnvironment env = SwitchExpression.this.scope.environment();
			TypeBinding [] resultReferenceTypes = new TypeBinding[this.rExpressions.size()];
			int i = 0;
			for (Expression rExpression : this.rExpressions)
				resultReferenceTypes[i++] = rExpression.resolvedType.isBaseType() ? env.computeBoxingType(rExpression.resolvedType) : rExpression.resolvedType;

			TypeBinding lub = SwitchExpression.this.scope.lowerUpperBound(resultReferenceTypes);
			if (lub != null)
				return resolveAsType(lub);

			SwitchExpression.this.scope.problemReporter().incompatibleSwitchExpressionResults(SwitchExpression.this); // Is this unreachable ? can lub be null with only reference types ??!
			return null;
		}

		/** Add an expression to known result expressions, gather some aggregate characteristics if in standalone context.
		 *  @return a flag indicating the overall well-formedness of result expression set.
		 */
		public boolean add(/*@NonNull*/ Expression rxpression, TypeBinding rxpressionType) {

			this.rExpressions.add(rxpression);
			if (rxpressionType == null) { // tolerate poly-expression resolving to null in the absence of target type.
				if (!rxpression.isPolyExpression() || ((IPolyExpression) rxpression).expectedType() != null || SwitchExpression.this.expressionContext == VANILLA_CONTEXT)
					this.allWellFormed = false;
			} else if (!rxpressionType.isValidBinding()) {
				this.allWellFormed = false;
			}

			// Classify result expressions on an aggregate basis - needed only for well formed, non-poly switches.
			if (!this.allWellFormed || SwitchExpression.this.isPolyExpression())
				return this.allWellFormed;

			rxpressionType = rxpressionType.unboxedType();
			this.allUniform &= TypeBinding.equalsEquals(this.rExpressions.iterator().next().resolvedType, rxpression.resolvedType);
			this.allBoolean &= rxpressionType.id == T_boolean;
			this.allNumeric &= rxpressionType.isNumericType();

			if (this.allNumeric) {
				boolean definiteType = true;
				if (rxpressionType.id == T_int && rxpression.constant != Constant.NotAConstant) {
					int i = rxpression.constant.intValue();
					if (i <= Character.MAX_VALUE && i >= Short.MIN_VALUE)
						definiteType = false;
					//  else int constants may get reclassified as they undergo narrowing conversions - can't pin them yet.
				}
				if (definiteType)
					this.rTypes.add(rxpressionType);
			}
			return true;
		}

		@Override
		public String toString() {
			return this.rExpressions.toString();
		}
	}

	@Override
	public void setExpressionContext(ExpressionContext context) {
		this.expressionContext = context;
	}

	@Override
	public void setExpectedType(TypeBinding expectedType) {
		this.expectedType = expectedType;
	}

	@Override
	public ExpressionContext getExpressionContext() {
		return this.expressionContext;
	}
	@Override
	protected void reportMissingEnumConstantCase(BlockScope upperScope, FieldBinding enumConstant) {
		// seeming identical body with super method causes varied overload selection
		upperScope.problemReporter().missingEnumConstantCase(this, enumConstant);
	}
	@Override
	public boolean checkNPE(BlockScope skope, FlowContext flowContext, FlowInfo flowInfo, int ttlForFieldCheck) {
		if ((this.nullStatus & FlowInfo.NULL) != 0)
			skope.problemReporter().expressionNullReference(this);
		else if ((this.nullStatus & FlowInfo.POTENTIALLY_NULL) != 0)
			skope.problemReporter().expressionPotentialNullReference(this);
		return true; // all checking done
	}

	@Override
	protected boolean needToCheckFlowInAbsenceOfDefaultBranch() { // JLS 12 16.1.8
		return (this.switchBits & LabeledRules) == 0;
	}

	@Override
	public Expression[] getPolyExpressions() {
		List<Expression> polys = new ArrayList<>();
		for (Expression e : this.results.rExpressions) {
			Expression[] ea = e.getPolyExpressions();
			if (ea == null || ea.length == 0) continue;
			polys.addAll(Arrays.asList(ea));
		}
		return polys.toArray(new Expression[0]);
	}
	@Override
	public boolean isPertinentToApplicability(TypeBinding targetType, MethodBinding method) {
		for (Expression e : this.results.rExpressions) {
			if (!e.isPertinentToApplicability(targetType, method))
				return false;
		}
		return true;
	}
	@Override
	public boolean isPotentiallyCompatibleWith(TypeBinding targetType, Scope scope1) {
		for (Expression e : this.results.rExpressions) {
			if (!e.isPotentiallyCompatibleWith(targetType, scope1))
				return false;
		}
		return true;
	}
	@Override
	public boolean isFunctionalType() {
		for (Expression e : this.results.rExpressions) {
			if (e.isFunctionalType()) // return true even for one functional type
				return true;
		}
		return false;
	}
	@Override
	public int nullStatus(FlowInfo flowInfo, FlowContext flowContext) {
		if ((this.implicitConversion & TypeIds.BOXING) != 0)
			return FlowInfo.NON_NULL;
		return this.nullStatus;
	}
	private LocalVariableBinding createStackSpillSlot(CodeStream codeStream, TypeBinding type, int typeId, int index, int resolvedPosition) {
		char[] name = CharOperation.concat(SECRET_YIELD_VALUE_NAME, String.valueOf(index).toCharArray());
		if (type == null) {
			type = TypeBinding.wellKnownType(this.scope, typeId);
			if (type == null)
				type = this.scope.getJavaLangObject();
		}
		LocalVariableBinding lvb =
				new LocalVariableBinding(
					name,
					type,
					ClassFileConstants.AccDefault,
					false);
		lvb.setConstant(Constant.NotAConstant);
		lvb.useFlag = LocalVariableBinding.USED;
		lvb.resolvedPosition = resolvedPosition;
		this.scope.addLocalVariable(lvb);
		return lvb;
	}

	private void spillOperandStackIfNeeded(CodeStream codeStream) {
		int operandStackSize = codeStream.operandStack.size();
		if (!this.jvmStackVolatile || (codeStream.stackDepth == 0 && operandStackSize == 0)) // nothing to spill and nothing to compain
			return;
		int nextResolvedPosition = this.scope.offset;
		this.typesOnStack = new ArrayList<>();
		int index = 0;
		while (operandStackSize > 0) {
			TypeBinding type = codeStream.operandStack.peek();
			LocalVariableBinding lvb = createStackSpillSlot(codeStream, type, TypeIds.T_undefined, index++, nextResolvedPosition);
			nextResolvedPosition += switch (lvb.type.id) {
				case TypeIds.T_long, TypeIds.T_double -> 2;
				default -> 1;
			};
			this.typesOnStack.add(lvb);
			codeStream.store(lvb, false);
			codeStream.addVariable(lvb);
			operandStackSize = codeStream.operandStack.size();
		}
		if (codeStream.stackDepth != 0 || codeStream.operandStack.size() != 0)
			codeStream.classFile.referenceBinding.scope.problemReporter().operandStackSizeInappropriate(codeStream.classFile.referenceBinding.scope.referenceContext);

		int delta = nextResolvedPosition - this.scope.offset;
		if (delta > 0)
			this.scope.adjustLocalVariablePositions(delta);
	}

	public void refillOperandStackIfNeeded(CodeStream codeStream, YieldStatement yield) {
		if (!this.jvmStackVolatile || this.typesOnStack == null || this.typesOnStack.isEmpty() || (yield.bits & ASTNode.IsAnyFinallyBlockEscaping) != 0) // nothing spilled or no point restoring.
			 return;

		if (codeStream.stackDepth != 0 || codeStream.operandStack.size() != 0)
			codeStream.classFile.referenceBinding.scope.problemReporter().operandStackSizeInappropriate(codeStream.classFile.referenceBinding.scope.referenceContext);

		List<LocalVariableBinding> tos = this.typesOnStack;
		int index = tos != null ? tos.size() - 1 : -1;
		while (index >= 0) {
			LocalVariableBinding lvb = tos.get(index--);
			codeStream.load(lvb);
		}
	}

	private void releaseStackSpillSlots(CodeStream codeStream) {
		List<LocalVariableBinding> tos = this.typesOnStack;
		int index = tos != null ? tos.size() -1 : - 1;
		while (index >= 0) {
			LocalVariableBinding lvb = tos.get(index--);
			codeStream.removeVariable(lvb);
		}
	}

	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		spillOperandStackIfNeeded(codeStream);
		super.generateCode(currentScope, codeStream);
		if (this.jvmStackVolatile)
			releaseStackSpillSlots(codeStream);
		if (!valueRequired) { // switch expression result discarded (saved to a variable that is not used.)
			switch(postConversionType(currentScope).id) {
				case TypeIds.T_long, TypeIds.T_double -> codeStream.pop2();
				default -> codeStream.pop();
			}
		} else if (!this.isPolyExpression())
			codeStream.generateImplicitConversion(this.implicitConversion);
	}

	@Override
	public void resolve(BlockScope upperScope) {
		resolveType(upperScope);
	}

	@Override
	public TypeBinding resolveType(BlockScope upperScope) {
		try {
			if (this.constant != Constant.NotAConstant) {
				this.constant = Constant.NotAConstant;
				super.resolve(upperScope); // drills down into switch block, which will cause yield expressions to be discovered and added to `this.results`
				if (this.results.rExpressions.size() == 0) {
					upperScope.problemReporter().unyieldingSwitchExpression(this);
					return this.resolvedType = null;
				}
				if (isPolyExpression() && (this.expectedType == null || !this.expectedType.isProperType(true)))
					return new PolyTypeBinding(this);
			} else { // re-resolve poly-expression against the eventual target type.
				for (Expression rExpression : this.results.rExpressions)
					if (rExpression.isPolyExpression())
						rExpression.resolveTypeExpecting(upperScope, this.expectedType);
			}
			return this.resolvedType = this.results.resultType();
		} finally {
			if (this.scope != null) this.scope.enclosingCase = null; // no longer inside switch case block
		}
	}

	// We have resolved the switch expression to be if type `switchType'. Allow result expressions to adapt.
	// Return the type after applying capture conversion.
	private TypeBinding resolveAsType(TypeBinding switchType) {
		for (Expression rExpression : this.results.rExpressions) {
			if (rExpression.resolvedType != null && rExpression.resolvedType.isValidBinding()) {
				if (rExpression.isConstantValueOfTypeAssignableToType(rExpression.resolvedType, switchType) || rExpression.resolvedType.isCompatibleWith(switchType)) {
					rExpression.computeConversion(this.scope, switchType, rExpression.resolvedType);
					if (rExpression.resolvedType.needsUncheckedConversion(switchType))
						this.scope.problemReporter().unsafeTypeConversion(rExpression, rExpression.resolvedType, switchType);
					if (rExpression instanceof CastExpression && (rExpression.bits & (ASTNode.UnnecessaryCast | ASTNode.DisableUnnecessaryCastCheck)) == 0)
						CastExpression.checkNeedForAssignedCast(this.scope, switchType, (CastExpression) rExpression);
				} else if (isBoxingCompatible(rExpression.resolvedType, switchType, rExpression, this.scope)) {
					rExpression.computeConversion(this.scope, switchType, rExpression.resolvedType);
					if (rExpression instanceof CastExpression && (rExpression.bits & (ASTNode.UnnecessaryCast | ASTNode.DisableUnnecessaryCastCheck)) == 0)
						CastExpression.checkNeedForAssignedCast(this.scope, switchType, (CastExpression) rExpression);
				} else {
					this.scope.problemReporter().typeMismatchError(rExpression.resolvedType, switchType, rExpression, null);
					return null;
				}
			}
		}
		return switchType.capture(this.scope, this.sourceStart, this.sourceEnd);
	}


	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		flowInfo = super.analyseCode(currentScope, flowContext, flowInfo);

		if (currentScope.compilerOptions().enableSyntacticNullAnalysisForFields)
			flowContext.expireNullCheckedFieldInfo(); // wipe information that was meant only for this result expression:

		int status =  this.results.rExpressions.iterator().next().nullStatus(flowInfo, flowContext);
		int combinedStatus = status;
		boolean identicalStatus = true;
		for (Expression rExpression : this.results.rExpressions) {
		    status = rExpression.nullStatus(flowInfo, flowContext);
			identicalStatus &= combinedStatus == status;
			combinedStatus |= status;
		}
		if (identicalStatus) {
			this.nullStatus = status;
			return flowInfo;
		}
		status = Expression.computeNullStatus(0, combinedStatus);
		if (status > 0)
			this.nullStatus = status;
		return flowInfo;
	}

	@Override
	public boolean isPolyExpression() {
		return this.expressionContext == ASSIGNMENT_CONTEXT || this.expressionContext == INVOCATION_CONTEXT;
	}
	@Override
	public boolean isTrulyExpression() {
		return true;
	}
	@Override
	public boolean isCompatibleWith(TypeBinding left, Scope skope) {
		if (!isPolyExpression())
			return super.isCompatibleWith(left, skope);

		for (Expression e : this.results.rExpressions) {
			if (!e.isCompatibleWith(left, skope))
				return false;
		}
		return true;
	}
	@Override
	public boolean isBoxingCompatibleWith(TypeBinding targetType, Scope skope) {
		if (!isPolyExpression())
			return super.isBoxingCompatibleWith(targetType, skope);

		for (Expression e : this.results.rExpressions) {
			if (!(e.isCompatibleWith(targetType, skope) || e.isBoxingCompatibleWith(targetType, skope)))
				return false;
		}
		return true;
	}
	@Override
	public boolean sIsMoreSpecific(TypeBinding s, TypeBinding t, Scope skope) {
		if (super.sIsMoreSpecific(s, t, skope))
			return true;
		if (!isPolyExpression())
			return false;
		for (Expression e : this.results.rExpressions) {
			if (!e.sIsMoreSpecific(s, t, skope))
				return false;
		}
		return true;
	}

	@Override
	public StringBuilder printExpression(int tab, StringBuilder output, boolean makeShort) {
		if (!makeShort) {
			return super.printExpression(tab, output);
		} else {
			printIndent(tab, output).append("switch ("); //$NON-NLS-1$
			return this.expression.printExpression(0, output).append(") { ... }"); //$NON-NLS-1$
		}
	}

	@Override
	public TypeBinding expectedType() {
		return this.expectedType;
	}

	public Set<Expression> resultExpressions() {
		return this.results.rExpressions;
	}
}