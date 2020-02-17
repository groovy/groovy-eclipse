/*******************************************************************************
 * Copyright (c) 2018, 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import static org.eclipse.jdt.internal.compiler.ast.ExpressionContext.ASSIGNMENT_CONTEXT;
import static org.eclipse.jdt.internal.compiler.ast.ExpressionContext.INVOCATION_CONTEXT;
import static org.eclipse.jdt.internal.compiler.ast.ExpressionContext.VANILLA_CONTEXT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.PolyTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public class SwitchExpression extends SwitchStatement implements IPolyExpression {

	/* package */ TypeBinding expectedType;
	private ExpressionContext expressionContext = VANILLA_CONTEXT;
	private boolean isPolyExpression = false;
	private TypeBinding[] originalValueResultExpressionTypes;
	private TypeBinding[] finalValueResultExpressionTypes;

	private int nullStatus = FlowInfo.UNKNOWN;
	public List<Expression> resultExpressions;
	public boolean resolveAll;
	/* package */ List<Integer> resultExpressionNullStatus;
	private static Map<TypeBinding, TypeBinding[]> type_map;

	static {
		type_map = new HashMap<TypeBinding, TypeBinding[]>();
		type_map.put(TypeBinding.CHAR, new TypeBinding[] {TypeBinding.CHAR, TypeBinding.BYTE, TypeBinding.INT});
		type_map.put(TypeBinding.SHORT, new TypeBinding[] {TypeBinding.SHORT, TypeBinding.BYTE, TypeBinding.INT});
		type_map.put(TypeBinding.BYTE, new TypeBinding[] {TypeBinding.BYTE, TypeBinding.INT});
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
	protected boolean ignoreMissingDefaultCase(CompilerOptions compilerOptions, boolean isEnumSwitch) {
		return isEnumSwitch; // mandatory error if not enum in switch expressions
	}
	@Override
	protected void reportMissingEnumConstantCase(BlockScope upperScope, FieldBinding enumConstant) {
		upperScope.problemReporter().missingEnumConstantCase(this, enumConstant);
	}
	@Override
	protected int getFallThroughState(Statement stmt, BlockScope blockScope) {
		if ((stmt instanceof Expression && ((Expression) stmt).isTrulyExpression())|| stmt instanceof ThrowStatement)
			return BREAKING;
		if (this.switchLabeledRules // do this check for every block if '->' (Switch Labeled Rules) 
				&& stmt instanceof Block) {
			Block block = (Block) stmt;
			if (block.doesNotCompleteNormally()) {
				return BREAKING;
			}
			//JLS 12 15.28.1 Given a switch expression, if the switch block consists of switch labeled rules,
			//then it is a compile-time error if any switch labeled block can complete normally.
			blockScope.problemReporter().switchExpressionSwitchLabeledBlockCompletesNormally(block);
		}
		return FALLTHROUGH;
	}
	@Override
	public boolean checkNPE(BlockScope skope, FlowContext flowContext, FlowInfo flowInfo, int ttlForFieldCheck) {
		if ((this.nullStatus & FlowInfo.NULL) != 0)
			skope.problemReporter().expressionNullReference(this);
		else if ((this.nullStatus & FlowInfo.POTENTIALLY_NULL) != 0)
			skope.problemReporter().expressionPotentialNullReference(this);
		return true; // all checking done
	}

	private void computeNullStatus(FlowInfo flowInfo, FlowContext flowContext) {
		 boolean precomputed = this.resultExpressionNullStatus.size() > 0;
		 if (!precomputed)
		         this.resultExpressionNullStatus.add(this.resultExpressions.get(0).nullStatus(flowInfo, flowContext));	int status =  this.resultExpressions.get(0).nullStatus(flowInfo, flowContext);
		int combinedStatus = status;
		boolean identicalStatus = true;
		for (int i = 1, l = this.resultExpressions.size(); i < l; ++i) {
		    if (!precomputed)
	             this.resultExpressionNullStatus.add(this.resultExpressions.get(i).nullStatus(flowInfo, flowContext));
		    int tmp = this.resultExpressions.get(i).nullStatus(flowInfo, flowContext);
			identicalStatus &= status == tmp;
			combinedStatus |= tmp;
		}
		if (identicalStatus) {
			this.nullStatus = status;
			return;
		}
		status = Expression.computeNullStatus(0, combinedStatus);
		if (status > 0)
			this.nullStatus = status;
	}

	@Override
	protected void completeNormallyCheck(BlockScope blockScope) {
		if (this.switchLabeledRules) return; // already taken care in getFallThroughState()
		int sz = this.statements != null ? this.statements.length : 0;
		if (sz == 0) return;
		/* JLS 12 15.28.1
		 * If, on the other hand, the switch block consists of switch labeled statement groups, then it is a
		 * compile-time error if either the last statement in the switch block can complete normally, or the
		 * switch block includes one or more switch labels at the end.
		 */
		Statement lastNonCaseStmt = null;
		Statement firstTrailingCaseStmt = null;
		for (int i = sz - 1; i >= 0; i--) {
			Statement stmt = this.statements[sz - 1];
			if (stmt instanceof CaseStatement)
				firstTrailingCaseStmt = stmt;
			else {
				lastNonCaseStmt = stmt;
				break;
			}
		}
		if (lastNonCaseStmt != null) {
			if (!lastNonCaseStmt.doesNotCompleteNormally())
				blockScope.problemReporter().switchExpressionLastStatementCompletesNormally(lastNonCaseStmt);
			else if (lastNonCaseStmt instanceof ContinueStatement || lastNonCaseStmt instanceof ReturnStatement) {
				blockScope.problemReporter().switchExpressionIllegalLastStatement(lastNonCaseStmt);
			}
		}
		if (firstTrailingCaseStmt != null) {
			blockScope.problemReporter().switchExpressionTrailingSwitchLabels(firstTrailingCaseStmt);				
		}
	}
	@Override
	protected boolean needToCheckFlowInAbsenceOfDefaultBranch() { // JLS 12 16.1.8 
		return !this.switchLabeledRules;
	}
	@Override
	public Expression[] getPolyExpressions() {
		List<Expression> polys = new ArrayList<>();
		for (Expression e : this.resultExpressions) {
			Expression[] ea = e.getPolyExpressions();
			if (ea == null || ea.length ==0) continue;
			polys.addAll(Arrays.asList(ea));
		}
		return polys.toArray(new Expression[0]);
	}
	@Override
	public boolean isPertinentToApplicability(TypeBinding targetType, MethodBinding method) {
		for (Expression e : this.resultExpressions) {
			if (!e.isPertinentToApplicability(targetType, method))
				return false;
		}
		return true;
	}
	@Override
	public boolean isPotentiallyCompatibleWith(TypeBinding targetType, Scope scope1) {
		for (Expression e : this.resultExpressions) {
			if (!e.isPotentiallyCompatibleWith(targetType, scope1))
				return false;
		}
		return true;
	}
	@Override
	public boolean isFunctionalType() {
		for (Expression e : this.resultExpressions) {
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
	@Override
	protected void statementGenerateCode(BlockScope currentScope, CodeStream codeStream, Statement statement) {
		if (!(statement instanceof Expression && ((Expression) statement).isTrulyExpression())
				|| statement instanceof Assignment
				|| statement instanceof MessageSend
				|| (statement instanceof SwitchStatement && !(statement instanceof SwitchExpression))) {
			super.statementGenerateCode(currentScope, codeStream, statement);
			return;
		}
		Expression expression1 = (Expression) statement;
		expression1.generateCode(currentScope, codeStream, true /* valueRequired */);
	}
	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		super.generateCode(currentScope, codeStream);
		if (!valueRequired) {
			// switch expression is saved to a variable that is not used. We need to pop the generated value from the stack
			switch(postConversionType(currentScope).id) {
				case TypeIds.T_long :
				case TypeIds.T_double :
					codeStream.pop2();
					break;
				case TypeIds.T_void :
					break;
				default :
					codeStream.pop();
					break;
			}
		}
	}
	protected boolean computeConversions(BlockScope blockScope, TypeBinding targetType) {
		boolean ok = true;
		for (int i = 0, l = this.resultExpressions.size(); i < l; ++i) {
			ok &= computeConversionsResultExpressions(blockScope, targetType, this.originalValueResultExpressionTypes[i], this.resultExpressions.get(i));
		}
		return ok;
	}
	private boolean computeConversionsResultExpressions(BlockScope blockScope, TypeBinding targetType, TypeBinding resultExpressionType,
			Expression resultExpression) {
		if (resultExpressionType != null && resultExpressionType.isValidBinding()) {
			if (resultExpression.isConstantValueOfTypeAssignableToType(resultExpressionType, targetType)
					|| resultExpressionType.isCompatibleWith(targetType)) {

				resultExpression.computeConversion(blockScope, targetType, resultExpressionType);
				if (resultExpressionType.needsUncheckedConversion(targetType)) {
					blockScope.problemReporter().unsafeTypeConversion(resultExpression, resultExpressionType, targetType);
				}
				if (resultExpression instanceof CastExpression
						&& (resultExpression.bits & (ASTNode.UnnecessaryCast|ASTNode.DisableUnnecessaryCastCheck)) == 0) {
					CastExpression.checkNeedForAssignedCast(blockScope, targetType, (CastExpression) resultExpression);
				}
			} else if (isBoxingCompatible(resultExpressionType, targetType, resultExpression, blockScope)) {
				resultExpression.computeConversion(blockScope, targetType, resultExpressionType);
				if (resultExpression instanceof CastExpression
						&& (resultExpression.bits & (ASTNode.UnnecessaryCast|ASTNode.DisableUnnecessaryCastCheck)) == 0) {
					CastExpression.checkNeedForAssignedCast(blockScope, targetType, (CastExpression) resultExpression);
				}
			} else {
				blockScope.problemReporter().typeMismatchError(resultExpressionType, targetType, resultExpression, null);
				return false;
			}
		}
		return true;
	}
	@Override
	public TypeBinding resolveType(BlockScope upperScope) {
		try {
			int resultExpressionsCount;
			if (this.constant != Constant.NotAConstant) {
				this.constant = Constant.NotAConstant;
	
				// A switch expression is a poly expression if it appears in an assignment context or an invocation context (5.2, 5.3). 
				// Otherwise, it is a standalone expression.
				if (this.expressionContext == ASSIGNMENT_CONTEXT || this.expressionContext == INVOCATION_CONTEXT) {
					for (Expression e : this.resultExpressions) {
						//Where a poly switch expression appears in a context of a particular kind with target type T,
						//its result expressions similarly appear in a context of the same kind with target type T.
						e.setExpressionContext(this.expressionContext);
						e.setExpectedType(this.expectedType);
					}
				}
	
				resolve(upperScope);
	
				if (this.statements == null || this.statements.length == 0) {
					//	Report Error JLS 13 15.28.1  The switch block must not be empty.
					upperScope.problemReporter().switchExpressionEmptySwitchBlock(this);
					return null;
				}
				
				resultExpressionsCount = this.resultExpressions != null ? this.resultExpressions.size() : 0;
				if (resultExpressionsCount == 0) {
					//  Report Error JLS 13 15.28.1 
					// It is a compile-time error if a switch expression has no result expressions.
					upperScope.problemReporter().switchExpressionNoResultExpressions(this);
					return null;
				}
	
				if (this.originalValueResultExpressionTypes == null) {
					this.originalValueResultExpressionTypes = new TypeBinding[resultExpressionsCount];
					this.finalValueResultExpressionTypes = new TypeBinding[resultExpressionsCount];
					for (int i = 0; i < resultExpressionsCount; ++i) {
						this.finalValueResultExpressionTypes[i] = this.originalValueResultExpressionTypes[i] =
								this.resultExpressions.get(i).resolvedType;
					}
				}
				if (isPolyExpression()) { //The type of a poly switch expression is the same as its target type.
					if (this.expectedType == null || !this.expectedType.isProperType(true)) {
						return new PolyTypeBinding(this);
					}
					return this.resolvedType = computeConversions(this.scope, this.expectedType) ? this.expectedType : null;
				}
				// fall through
			} else {
				// re-resolving of poly expression:
				resultExpressionsCount = this.resultExpressions != null ? this.resultExpressions.size() : 0;
				for (int i = 0; i < resultExpressionsCount; i++) {
					Expression resultExpr = this.resultExpressions.get(i);
					if (resultExpr.resolvedType == null || resultExpr.resolvedType.kind() == Binding.POLY_TYPE) {
						this.finalValueResultExpressionTypes[i] = this.originalValueResultExpressionTypes[i] = 
							resultExpr.resolveTypeExpecting(upperScope, this.expectedType);
					}
					// This is a kludge and only way completion can tell this node to resolve all
					// resultExpressions. Ideal solution is to remove all other expressions except
					// the one that contain the completion node.
					if (this.resolveAll) continue;
					if (resultExpr.resolvedType == null || !resultExpr.resolvedType.isValidBinding())
						return this.resolvedType = null;
				}
				this.resolvedType = computeConversions(this.scope, this.expectedType) ? this.expectedType : null;
				// fall through
			}

			if (resultExpressionsCount == 1)
				return this.resolvedType = this.originalValueResultExpressionTypes[0];

			boolean typeUniformAcrossAllArms = true;
			TypeBinding tmp = this.originalValueResultExpressionTypes[0];
			for (int i = 1, l = this.originalValueResultExpressionTypes.length; i < l; ++i) {
				TypeBinding originalType = this.originalValueResultExpressionTypes[i];
				if (originalType != null && TypeBinding.notEquals(tmp, originalType)) {
					typeUniformAcrossAllArms = false;
					break;
				}
			}
			// If the result expressions all have the same type (which may be the null type), 
			// then that is the type of the switch expression.
			if (typeUniformAcrossAllArms) {
				tmp = this.originalValueResultExpressionTypes[0];
				for (int i = 1; i < resultExpressionsCount; ++i) {
					if (this.originalValueResultExpressionTypes[i] != null)
						tmp = NullAnnotationMatching.moreDangerousType(tmp, this.originalValueResultExpressionTypes[i]);
				}
				return this.resolvedType = tmp;
			}
			
			boolean typeBbolean = true;
			for (TypeBinding t : this.originalValueResultExpressionTypes) {
				if (t != null)
					typeBbolean &= t.id == T_boolean || t.id == T_JavaLangBoolean;
			}
			LookupEnvironment env = this.scope.environment();
			/*
			 * Otherwise, if the type of each result expression is boolean or Boolean,
			 * an unboxing conversion (5.1.8) is applied to each result expression of type Boolean,
			 * and the switch expression has type boolean.
			 */
			if (typeBbolean) {
				for (int i = 0; i < resultExpressionsCount; ++i) {
					if (this.originalValueResultExpressionTypes[i].id == T_boolean) continue;
					this.finalValueResultExpressionTypes[i] = env.computeBoxingType(this.originalValueResultExpressionTypes[i]);
					this.resultExpressions.get(i).computeConversion(this.scope, this.finalValueResultExpressionTypes[i], this.originalValueResultExpressionTypes[i]);
				}
				return this.resolvedType = TypeBinding.BOOLEAN;
			}

			/*
			 * Otherwise, if the type of each result expression is convertible to a numeric type (5.1.8), the type
			 * of the switch expression is given by numeric promotion (5.6.3) applied to the result expressions.
			 */
			boolean typeNumeric = true;
			TypeBinding resultNumeric = null;
			HashSet<TypeBinding> typeSet = new HashSet<>();
			/*  JLS 13 5.6 Numeric Contexts
			 * An expression appears in a numeric context if it is one of:....
			 * ...8. a result expression of a standalone switch expression (15.28.1),
			 * where all the result expressions are convertible to a numeric type
			 * If any expression is of a reference type, it is subjected to unboxing conversion (5.1.8).
			 */
			for (int i = 0; i < resultExpressionsCount; ++i) {
				TypeBinding originalType = this.originalValueResultExpressionTypes[i];
				if (originalType == null) continue;
				tmp = originalType.isNumericType() ? originalType : env.computeBoxingType(originalType);
				if (!tmp.isNumericType()) {
					typeNumeric = false;
					break;
				}
				typeSet.add(TypeBinding.wellKnownType(this.scope, tmp.id));
			}
			if (typeNumeric) {
				 /* If any result expression is of type double, then other result expressions that are not of type double
				 *  are widened to double.
				 *  Otherwise, if any result expression is of type float, then other result expressions that are not of
				 *  type float are widened to float.
				 *  Otherwise, if any result expression is of type long, then other result expressions that are not of 
				 *  type long are widened to long.
				 */
				TypeBinding[] dfl = new TypeBinding[]{// do not change the order JLS 13 5.6
						TypeBinding.DOUBLE,
						TypeBinding.FLOAT,
						TypeBinding.LONG};
				for (TypeBinding binding : dfl) {
					if (typeSet.contains(binding)) {
						resultNumeric = binding;
						break;
					}
				}

				/* Otherwise, if any expression appears in a numeric array context or a numeric arithmetic context,
				 * rather than a numeric choice context, then the promoted type is int and other expressions that are
				 * not of type int undergo widening primitive conversion to int. - not applicable since numeric choice context.
				 * [Note: A numeric choice context is a numeric context that is either a numeric conditional expression or
				 * a standalone switch expression where all the result expressions are convertible to a numeric type.]
				 */

				 /*  Otherwise, if any result expression is of type int and is not a constant expression, the other 
				 *  result expressions that are not of type int are widened to int.
				 */
				resultNumeric = resultNumeric != null ? resultNumeric : check_nonconstant_int();

				resultNumeric = resultNumeric != null ? resultNumeric : // one among the first few rules applied.
					getResultNumeric(typeSet, this.originalValueResultExpressionTypes); // check the rest
				typeSet = null; // hey gc!
				for (int i = 0; i < resultExpressionsCount; ++i) {
					this.resultExpressions.get(i).computeConversion(this.scope,
							resultNumeric, this.originalValueResultExpressionTypes[i]);
					this.finalValueResultExpressionTypes[i] = resultNumeric;
				}
				// After the conversion(s), if any, value set conversion (5.1.13) is then applied to each result expression.
				return this.resolvedType = resultNumeric;
			}

			/* Otherwise, boxing conversion (5.1.7) is applied to each result expression that has a primitive type,
			 * after which the type of the switch expression is the result of applying capture conversion (5.1.10)
			 * to the least upper bound (4.10.4) of the types of the result expressions.
			 */
			for (int i = 0; i < resultExpressionsCount; ++i) {
				TypeBinding finalType = this.finalValueResultExpressionTypes[i];
				if (finalType != null && finalType.isBaseType())
					this.finalValueResultExpressionTypes[i] = env.computeBoxingType(finalType);
			}
			TypeBinding commonType = this.scope.lowerUpperBound(this.finalValueResultExpressionTypes);
			if (commonType != null) {
				for (int i = 0, l = this.resultExpressions.size(); i < l; ++i) {
					if (this.originalValueResultExpressionTypes[i] == null) continue;
					this.resultExpressions.get(i).computeConversion(this.scope, commonType, this.originalValueResultExpressionTypes[i]);
					this.finalValueResultExpressionTypes[i] = commonType;
				}
				return this.resolvedType = commonType.capture(this.scope, this.sourceStart, this.sourceEnd);
			}
			this.scope.problemReporter().switchExpressionIncompatibleResultExpressions(this);
			return null;
		} finally {
			if (this.scope != null) this.scope.enclosingCase = null; // no longer inside switch case block
		}
	}
	private TypeBinding check_nonconstant_int() {
		for (int i = 0, l = this.resultExpressions.size(); i < l; ++i) {
			Expression e = this.resultExpressions.get(i);
			TypeBinding type = this.originalValueResultExpressionTypes[i];
			if (type != null && type.id == T_int && e.constant == Constant.NotAConstant)
				return TypeBinding.INT;
		}
		return null;
	}
	private boolean areAllIntegerResultExpressionsConvertibleToTargetType(TypeBinding targetType) {
		for (int i = 0, l = this.resultExpressions.size(); i < l; ++i) {
			Expression e = this.resultExpressions.get(i);
			TypeBinding t = this.originalValueResultExpressionTypes[i];
			if (!TypeBinding.equalsEquals(t, TypeBinding.INT)) continue;
			if (!e.isConstantValueOfTypeAssignableToType(t, targetType))
				return false;
		}
		return true;
	}
	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		flowInfo = super.analyseCode(currentScope, flowContext, flowInfo);
		this.resultExpressionNullStatus = new ArrayList<>(0);
		final CompilerOptions compilerOptions = currentScope.compilerOptions();
		if (compilerOptions.enableSyntacticNullAnalysisForFields) {
			for (Expression re : this.resultExpressions) {
				this.resultExpressionNullStatus.add(re.nullStatus(flowInfo, flowContext));
				// wipe information that was meant only for this result expression:
				flowContext.expireNullCheckedFieldInfo();
			}
		}
		computeNullStatus(flowInfo, flowContext);
		return flowInfo;
	}
	private TypeBinding check_csb(Set<TypeBinding> typeSet, TypeBinding candidate) {
		if (!typeSet.contains(candidate))
			return null;

		TypeBinding[] allowedTypes = SwitchExpression.type_map.get(candidate);
		Set<TypeBinding> allowedSet = Arrays.stream(allowedTypes).collect(Collectors.toSet());

		if (!allowedSet.containsAll(typeSet))
			return null;

		return areAllIntegerResultExpressionsConvertibleToTargetType(candidate) ?
				candidate : null;
	}
	private TypeBinding getResultNumeric(Set<TypeBinding> typeSet, TypeBinding[] armTypes) {
		// note: if an expression has a type integer, then it will be a constant 
		// since non-constant integers are already processed before reaching here.

		/* Otherwise, if any expression is of type short, and every other expression is either of type short,
		 * or of type byte, or a constant expression of type int with a value that is representable in the
		 * type short, then T is short, the byte expressions undergo widening primitive conversion to short, 
		 * and the int expressions undergo narrowing primitive conversion to short.\
		 *
		 * Otherwise, if any expression is of type byte, and every other expression is either of type byte or a
		 * constant expression of type int with a value that is representable in the type byte, then T is byte 
		 * and the int expressions undergo narrowing primitive conversion to byte.
		 *
		 * Otherwise, if any expression is of type char, and every other expression is either of type char or a
		 * constant expression of type int with a value that is representable in the type char, then T is char
		 * and the int expressions undergo narrowing primitive conversion to char.
		 *
		 * Otherwise, T is int and all the expressions that are not of type int undergo widening
		 * primitive conversion to int.
		 */

		// DO NOT Change the order below [as per JLS 13 5.6 ].
		TypeBinding[] csb = new TypeBinding[] {TypeBinding.SHORT, TypeBinding.BYTE, TypeBinding.CHAR};
		for (TypeBinding c : csb) {
			TypeBinding result = check_csb(typeSet, c);
			if (result != null)
				return result;
		}
		 /*  Otherwise, all the result expressions that are not of type int are widened to int. */
		return TypeBinding.INT;
	}
	@Override
	public boolean isPolyExpression() {
		if (this.isPolyExpression)
			return true;
		// JLS 13 15.28.1 A switch expression is a poly expression if it appears in an assignment context or
		// an invocation context (5.2, 5.3). Otherwise, it is a standalone expression.
		return this.isPolyExpression = this.expressionContext == ASSIGNMENT_CONTEXT || 
				this.expressionContext == INVOCATION_CONTEXT;
	}
	@Override
	public boolean isTrulyExpression() {
		return true;
	}
	@Override
	public boolean isCompatibleWith(TypeBinding left, Scope skope) {
		if (!isPolyExpression())
			return super.isCompatibleWith(left, skope);

		for (Expression e : this.resultExpressions) {
			if (!e.isCompatibleWith(left, skope))
				return false;
		}
		return true;
	}
	@Override
	public boolean isBoxingCompatibleWith(TypeBinding targetType, Scope skope) {
		if (!isPolyExpression())
			return super.isBoxingCompatibleWith(targetType, skope);

		for (Expression e : this.resultExpressions) {
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
		for (Expression e : this.resultExpressions) {
			if (!e.sIsMoreSpecific(s, t, skope))
				return false;
		}
		return true;
	}
	@Override
	public TypeBinding expectedType() {
		return this.expectedType;
	}
	@Override
	public void traverse(
			ASTVisitor visitor,
			BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			this.expression.traverse(visitor, blockScope);
			if (this.statements != null) {
				int statementsLength = this.statements.length;
				for (int i = 0; i < statementsLength; i++)
					this.statements[i].traverse(visitor, this.scope);
			}
		}
		visitor.endVisit(this, blockScope);
	}
}