/*******************************************************************************
 * Copyright (c) 2000, 2024 IBM Corporation and others.
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.Pattern.PrimitiveConversionRoute;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.IntConstant;
import org.eclipse.jdt.internal.compiler.impl.JavaFeature;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public class CaseStatement extends Statement {


	public BranchLabel targetLabel;
	public Expression[] constantExpressions; // case with multiple expressions - if you want a under-the-hood view, use peeledLabelExpressions()
	public BranchLabel[] targetLabels; // for multiple expressions
	public boolean isExpr = false;

	public SwitchStatement swich; // owning switch
	public int typeSwitchIndex;   // for the first pattern among this.constantExpressions

public CaseStatement(Expression[] constantExpressions, int sourceStart, int sourceEnd) {
	this.constantExpressions = constantExpressions;
	this.sourceStart = sourceStart;
	this.sourceEnd = sourceEnd;
}

/** Provide an under-the-hood view of label expressions, peeling away any abstractions that package many expressions as one
 *  @return flattened array of label expressions
 */
public Expression [] peeledLabelExpressions() {
	Expression [] constants = Expression.NO_EXPRESSIONS;
	for (Expression e : this.constantExpressions) {
		if (e instanceof Pattern p1) {
			constants = Stream.concat(Arrays.stream(constants), Arrays.stream(p1.getAlternatives())).toArray(Expression[]::new);
		} else {
			constants = Stream.concat(Arrays.stream(constants), Stream.of(e)).toArray(Expression[]::new);
		}
	}
	return constants;
}
@Override
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {

	int nullPatternCount = 0;
	for (int i = 0, length = this.constantExpressions.length; i < length; i++) {
		Expression e = this.constantExpressions[i];
		for (LocalVariableBinding local : e.bindingsWhenTrue()) {
			local.useFlag = LocalVariableBinding.USED; // these are structurally required even if not touched
		}
		nullPatternCount +=  e instanceof NullLiteral ? 1 : 0;
		if (i > 0 && (e instanceof Pattern) && !JavaFeature.UNNAMMED_PATTERNS_AND_VARS.isSupported(currentScope.compilerOptions().sourceLevel, currentScope.compilerOptions().enablePreviewFeatures)) {
			if (!(i == nullPatternCount && e instanceof TypePattern))
				currentScope.problemReporter().IllegalFallThroughToPattern(e);
		}
		flowInfo = analyseConstantExpression(currentScope, flowContext, flowInfo, e);
		if (nullPatternCount > 0 && e instanceof TypePattern) {
			LocalVariableBinding binding = ((TypePattern) e).local.binding;
			if (binding != null)
				flowInfo.markNullStatus(binding, FlowInfo.POTENTIALLY_NULL);
		}
	}

	return flowInfo;
}
private FlowInfo analyseConstantExpression(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo,
		Expression e) {
	if (e.constant == Constant.NotAConstant
			&& !e.resolvedType.isEnum()) {
		boolean caseNullorDefaultAllowed =
				JavaFeature.PATTERN_MATCHING_IN_SWITCH.isSupported(currentScope.compilerOptions())
				&& (e instanceof NullLiteral || e instanceof FakeDefaultLiteral || e instanceof Pattern);
		if (!caseNullorDefaultAllowed)
			currentScope.problemReporter().caseExpressionMustBeConstant(e);
		if (e instanceof NullLiteral && flowContext.associatedNode instanceof SwitchStatement) {
			Expression switchValue = ((SwitchStatement) flowContext.associatedNode).expression;
			if (switchValue != null && switchValue.nullStatus(flowInfo, flowContext) == FlowInfo.NON_NULL) {
				currentScope.problemReporter().unnecessaryNullCaseInSwitchOverNonNull(this);
			}
		}
	}
	return e.analyseCode(currentScope, flowContext, flowInfo);
}

/**
 * No-op : should use resolveCase(...) instead.
 */
@Override
public void resolve(BlockScope scope) {
	// no-op : should use resolveCase(...) instead.
}
public static class ResolvedCase {
	static final ResolvedCase[] UnresolvedCase = new ResolvedCase[0];
	public Constant c;
	public Expression e;
	public TypeBinding t; // For ease of access. This.e contains the type binding anyway.
	public int index;
	private int intValue;
	private final boolean isPattern;
	private final boolean isQualifiedEnum;
	public int enumDescIdx;
	public int classDescIdx;
	public int primitivesBootstrapIdx; // index for a bootstrap method to args to indy typeSwitch for primitives
	ResolvedCase(Constant c, Expression e, TypeBinding t, int index, boolean isQualifiedEnum) {
		this.c = c;
		this.e = e;
		this.t= t;
		this.index = index;
		if (c.typeID() == TypeIds.T_JavaLangString) {
			this.intValue = c.stringValue().hashCode();
		} else {
			this.intValue = c.intValue();
		}
		this.isPattern = e instanceof Pattern;
		this.isQualifiedEnum = isQualifiedEnum;
	}
	public int intValue() {
		return this.intValue;
	}
	public boolean isPattern() {
		return this.isPattern;
	}
	public boolean isQualifiedEnum() {
		return this.isQualifiedEnum;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("case "); //$NON-NLS-1$
		builder.append(this.e);
		builder.append(" [CONSTANT="); //$NON-NLS-1$
		builder.append(this.c);
		builder.append("]"); //$NON-NLS-1$
		return builder.toString();
	}
}

/**
 * Returns the constant intValue or ordinal for enum constants. If constant is NotAConstant, then answers Float.MIN_VALUE
 */
public ResolvedCase[] resolveCase(BlockScope scope, TypeBinding switchExpressionType, SwitchStatement switchStatement) {
	this.swich = switchStatement;
	scope.enclosingCase = this; // record entering in a switch case block
	if (this.constantExpressions == Expression.NO_EXPRESSIONS) {
		checkDuplicateDefault(scope, switchStatement, this);
		return ResolvedCase.UnresolvedCase;
	}

	switchStatement.cases[switchStatement.caseCount++] = this;

	List<ResolvedCase> cases = new ArrayList<>();
	int count = 0;
	int nullCaseCount = 0;
	boolean hasResolveErrors = false;
	for (Expression e : this.constantExpressions) {
		count++;
		if (e instanceof FakeDefaultLiteral) {
			 checkDuplicateDefault(scope, switchStatement, this.constantExpressions.length > 1 ? e : this);
			 if (count != 2 || nullCaseCount < 1) {
				 scope.problemReporter().patternSwitchCaseDefaultOnlyAsSecond(e);
			 }
			 continue;
		}
		if (e instanceof NullLiteral) {
			if (switchStatement.nullCase == null) {
				switchStatement.nullCase = this;
			}
			nullCaseCount++;
			if (count > 1 && nullCaseCount < 2) {
				scope.problemReporter().patternSwitchNullOnlyOrFirstWithDefault(e);
			}
		}

		// tag constant name with enum type for privileged access to its members
		if (switchExpressionType != null && switchExpressionType.isEnum() && (e instanceof SingleNameReference)) {
			((SingleNameReference) e).setActualReceiverType((ReferenceBinding)switchExpressionType);
		} else if (e instanceof FakeDefaultLiteral) {
			continue; // already processed
		}
		e.setExpressionContext(ExpressionContext.TESTING_CONTEXT);
		if (e instanceof Pattern p)
			p.setOuterExpressionType(switchExpressionType);

		TypeBinding	caseType = e.resolveType(scope);

		if (caseType == null || switchExpressionType == null) {
			hasResolveErrors = true;
			continue;
		}


		if (caseType.isValidBinding()) {
			if (e instanceof Pattern) {
				for (Pattern p : ((Pattern) e).getAlternatives()) {
					Constant con =  resolveCasePattern(scope, p.resolvedType, switchExpressionType, switchStatement, p, ((Pattern) e).isUnguarded());
					if (con != Constant.NotAConstant) {
						int index = switchStatement.constantIndex++;
						cases.add(new ResolvedCase(con, p, p.resolvedType, index, false));
					}
				}
			} else {
				// check from §14.11.1 (JEP 455):
				// For each case constant associated with the switch block that is a constant expression, one of the following is true:
				//  - [...]
				//  - if T is one of long, float, double, or boolean, the type of the case constant is T.
				//  - if T is one of Long, Float, Double, or Boolean, the type of the case constant is, respectively, long, float, double, or boolean.
				TypeBinding expectedCaseType = switchExpressionType;
				if (switchExpressionType.isBoxedPrimitiveType()) {
					expectedCaseType = scope.environment().computeBoxingType(switchExpressionType); // in this case it's actually 'computeUnboxingType()'
				}
				switch (expectedCaseType.id) {
					case TypeIds.T_long, TypeIds.T_float, TypeIds.T_double, TypeIds.T_boolean -> {
						if (caseType.id != expectedCaseType.id) {
							scope.problemReporter().caseExpressionWrongType(e, switchExpressionType, expectedCaseType);
							continue;
						}
						switchExpressionType = expectedCaseType;
					}
				}
				//
				Constant con = resolveConstantExpression(scope, caseType, switchExpressionType, switchStatement, e, cases);
				if (con != Constant.NotAConstant) {
					int index = this == switchStatement.nullCase && e instanceof NullLiteral ?
							-1 : switchStatement.constantIndex++;
					cases.add(new ResolvedCase(con, e, caseType, index, false));
				}
			}
		}
	}
	return hasResolveErrors ? ResolvedCase.UnresolvedCase : cases.toArray(new ResolvedCase[cases.size()]);
}

/**
 * Precondition: this is a default case.
 * Check if (a) another default or (b) a total type pattern has been recorded already
 */
private void checkDuplicateDefault(BlockScope scope, SwitchStatement switchStatement, ASTNode node) {
	if (switchStatement.defaultCase != null) {
		scope.problemReporter().duplicateDefaultCase(node);
	} else if ((switchStatement.switchBits & SwitchStatement.TotalPattern) != 0) {
		scope.problemReporter().illegalTotalPatternWithDefault(this);
	}

	// remember the default case into the associated switch statement
	// on error the last default will be the selected one ...
	switchStatement.defaultCase = this;
}

@Override
public LocalVariableBinding[] bindingsWhenTrue() {
	LocalVariableBinding [] variables = NO_VARIABLES;
	for (Expression e : this.constantExpressions) {
		variables = LocalVariableBinding.merge(variables, e.bindingsWhenTrue());
	}
	return variables;
}

public Constant resolveConstantExpression(BlockScope scope,
											TypeBinding caseType,
											TypeBinding switchType,
											SwitchStatement switchStatement,
											Expression expression,
											List<ResolvedCase> cases) {

	CompilerOptions options = scope.compilerOptions();
	boolean patternSwitchAllowed = JavaFeature.PATTERN_MATCHING_IN_SWITCH.isSupported(options);
	if (patternSwitchAllowed) {
		if (expression instanceof Pattern) {
			throw new AssertionError("Unexpected control flow"); //$NON-NLS-1$
		} else if (expression instanceof NullLiteral) {
			if (!caseType.isCompatibleWith(switchType, scope)) {
				scope.problemReporter().caseConstantIncompatible(TypeBinding.NULL, switchType, expression);
			}
			switchStatement.switchBits |= SwitchStatement.NullCase;
			return IntConstant.fromValue(-1);
		} else if (expression instanceof FakeDefaultLiteral) {
			// do nothing
		} else {
			if (switchStatement.isNonTraditional) {
				if (switchType.isBaseType() && !expression.isConstantValueOfTypeAssignableToType(caseType, switchType)) {
					scope.problemReporter().caseConstantIncompatible(caseType, switchType, expression);
					return Constant.NotAConstant;
				}
			}

	}
	}
	boolean boxing = !patternSwitchAllowed ||
			switchStatement.isAllowedType(switchType);

	if (expression.isConstantValueOfTypeAssignableToType(caseType, switchType)
			||(caseType.isCompatibleWith(switchType)
				&& !(expression instanceof StringLiteral))) {
		if (caseType.isEnum()) {
			if (((expression.bits & ASTNode.ParenthesizedMASK) >> ASTNode.ParenthesizedSHIFT) != 0) {
				scope.problemReporter().enumConstantsCannotBeSurroundedByParenthesis(expression);
			}

			if (expression instanceof NameReference
					&& (expression.bits & ASTNode.RestrictiveFlagMASK) == Binding.FIELD) {
				NameReference reference = (NameReference) expression;
				FieldBinding field = reference.fieldBinding();
				if ((field.modifiers & ClassFileConstants.AccEnum) == 0) {
					 scope.problemReporter().enumSwitchCannotTargetField(reference, field);
				} else 	if (reference instanceof QualifiedNameReference) {
					if (options.complianceLevel < ClassFileConstants.JDK21) {
						scope.problemReporter().cannotUseQualifiedEnumConstantInCaseLabel(reference, field);
					} else if (!TypeBinding.equalsEquals(caseType, switchType)) {
						switchStatement.switchBits |= SwitchStatement.QualifiedEnum;
						StringConstant constant = (StringConstant) StringConstant.fromValue(new String(field.name));
						cases.add(new ResolvedCase(constant, expression, caseType, -1, true));
						return Constant.NotAConstant;
					}
				}
				return IntConstant.fromValue(field.original().id + 1); // (ordinal value + 1) zero should not be returned see bug 141810
			}
		} else {
			return expression.constant;
		}
	} else if (boxing && isBoxingCompatible(caseType, switchType, expression, scope)) {
		// constantExpression.computeConversion(scope, caseType, switchExpressionType); - do not report boxing/unboxing conversion
		return expression.constant;
	}
	scope.problemReporter().caseConstantIncompatible(expression.resolvedType, switchType, expression);
	return Constant.NotAConstant;
}

private Constant resolveCasePattern(BlockScope scope, TypeBinding caseType, TypeBinding switchExpressionType, SwitchStatement switchStatement, Pattern e, boolean isUnguarded) {

	Constant constant = Constant.NotAConstant;

	TypeBinding type = e.resolvedType;

	if (type != null) {
		constant = IntConstant.fromValue(switchStatement.constantIndex);
		switchStatement.caseLabelElements.add(e);

		if (isUnguarded)
			switchStatement.caseLabelElementTypes.add(type);

		TypeBinding expressionType = switchStatement.expression.resolvedType;
		// The following code is copied from InstanceOfExpression#resolve()
		// But there are enough differences to warrant a copy
		if (!type.isReifiable()) {
			if (expressionType != TypeBinding.NULL && !(e instanceof RecordPattern)) {
				boolean isLegal = e.checkCastTypesCompatibility(scope, type, expressionType, e, false);
				if (!isLegal || (e.bits & ASTNode.UnsafeCast) != 0) {
					scope.problemReporter().unsafeCastInInstanceof(e, type, expressionType);
				}
			}
		} else if (type.isValidBinding()) {
			// if not a valid binding, an error has already been reported for unresolved type
			if (Pattern.findPrimitiveConversionRoute(type, expressionType, scope) == PrimitiveConversionRoute.NO_CONVERSION_ROUTE) {
				if (type.isPrimitiveType() && !JavaFeature.PRIMITIVES_IN_PATTERNS.isSupported(scope.compilerOptions())) {
					scope.problemReporter().unexpectedTypeinSwitchPattern(type, e);
					return Constant.NotAConstant;
				} else if (!e.checkCastTypesCompatibility(scope, type, expressionType, null, false)) {
					scope.problemReporter().typeMismatchError(expressionType, type, e, null);
					return Constant.NotAConstant;
				}
			} else {
				this.swich.isPrimitiveSwitch = true;
			}
		}
		if (e.coversType(expressionType, scope)) {
			switchStatement.switchBits |= SwitchStatement.Exhaustive;
			e.isTotalTypeNode = true;
			if (e.isUnconditional(expressionType, scope)) // unguarded is implied from 'coversType()' above
				switchStatement.switchBits |= SwitchStatement.TotalPattern;
			if (switchStatement.nullCase == null)
				constant = IntConstant.fromValue(-1);
		}
	}
 	return constant;
}

@Override
public void generateCode(BlockScope currentScope, CodeStream codeStream) {
	if ((this.bits & ASTNode.IsReachable) == 0) {
		return;
	}
	int pc = codeStream.position;
	if (this.targetLabels != null) {
		for (BranchLabel label : this.targetLabels) {
			label.place();
		}
	}
	if (this.targetLabel != null)
		this.targetLabel.place();

	if (containsPatternVariable(true)) {

		BranchLabel patternMatchLabel = new BranchLabel(codeStream);
		BranchLabel matchFailLabel = new BranchLabel(codeStream);

		Pattern pattern = (Pattern) this.constantExpressions[0];
		codeStream.load(this.swich.dispatchPatternCopy);
		pattern.generateCode(currentScope, codeStream, patternMatchLabel, matchFailLabel);
		codeStream.goto_(patternMatchLabel);
		matchFailLabel.place();

		if (pattern.matchFailurePossible()) {
			/* We are generating a "thunk"/"trampoline" of sorts now, that flow analysis has no clue about.
			   We need to manage the live variables manually. Pattern bindings are not definitely
			   assigned here as we are in the else region.
		    */
			final LocalVariableBinding[] bindingsWhenTrue = pattern.bindingsWhenTrue();
			Stream.of(bindingsWhenTrue).forEach(v->v.recordInitializationEndPC(codeStream.position));
			int caseIndex = this.typeSwitchIndex + pattern.getAlternatives().length;
			codeStream.loadInt(this.swich.nullProcessed ? caseIndex - 1 : caseIndex);
			codeStream.store(this.swich.restartIndexLocal, false);
			codeStream.goto_(this.swich.switchPatternRestartTarget);
			Stream.of(bindingsWhenTrue).forEach(v->v.recordInitializationStartPC(codeStream.position));
		}
		patternMatchLabel.place();
	} else {
		if (this.swich.containsNull) {
			this.swich.nullProcessed |= true;
		}
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}

@Override
public StringBuilder printStatement(int tab, StringBuilder output) {
	printIndent(tab, output);
	if (this.constantExpressions == Expression.NO_EXPRESSIONS) {
		output.append("default "); //$NON-NLS-1$
		output.append(this.isExpr ? "->" : ":"); //$NON-NLS-1$ //$NON-NLS-2$
	} else {
		output.append("case "); //$NON-NLS-1$
		for (int i = 0, l = this.constantExpressions.length; i < l; ++i) {
			this.constantExpressions[i].printExpression(0, output);
			if (i < l -1) output.append(',');
		}
		output.append(this.isExpr ? " ->" : " :"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	return output;
}

@Override
public void traverse(ASTVisitor visitor, 	BlockScope blockScope) {
	if (visitor.visit(this, blockScope)) {
		for (Expression e : this.constantExpressions) {
			e.traverse(visitor, blockScope);
		}
	}
	visitor.endVisit(this, blockScope);
}
}
