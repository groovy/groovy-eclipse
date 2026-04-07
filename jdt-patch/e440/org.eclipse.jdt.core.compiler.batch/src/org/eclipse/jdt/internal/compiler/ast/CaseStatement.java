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
 *     Advantest R & D - Enhanced Switches v2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import java.util.Arrays;
import java.util.stream.Stream;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.Pattern.PrimitiveConversionRoute;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.BooleanConstant;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.IntConstant;
import org.eclipse.jdt.internal.compiler.impl.JavaFeature;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public class CaseStatement extends Statement {

	public BranchLabel targetLabel;
	public Expression[] constantExpressions; // case with multiple expressions - if you want a under-the-hood view, use peeledLabelExpressions()
	public boolean isSwitchRule = false;

	public SwitchStatement swich; // owning switch
	public int labelExpressionOrdinal;   // for the first pattern among this.constantExpressions

public CaseStatement(Expression[] constantExpressions, int sourceStart, int sourceEnd) {
	this.constantExpressions = constantExpressions;
	this.sourceStart = sourceStart;
	this.sourceEnd = sourceEnd;
}

public static class LabelExpression {
	public Constant constant;
	public Expression expression;
	public TypeBinding type; // For ease of access. This.e contains the type binding anyway.
	public int index;
	private int intValue;
	private final boolean isPattern;
	private final boolean isQualifiedEnum;
	public int enumDescIdx;
	public int classDescIdx;
	public int primitivesBootstrapIdx; // index for a bootstrap method to args to indy typeSwitch for primitives

	LabelExpression(Constant c, Expression e, TypeBinding t, int index, boolean isQualifiedEnum) {
		this.constant = c;
		this.expression = e;
		this.type = t;
		this.index = index;
		this.intValue = c.typeID() == TypeIds.T_JavaLangString ? c.stringValue().hashCode() : c.intValue();
		this.isPattern = e instanceof Pattern;
		this.isQualifiedEnum = isQualifiedEnum;
	}

	public int intValue() { return this.intValue; }
	public boolean isPattern() { return this.isPattern; }
	public boolean isQualifiedEnum() { return this.isQualifiedEnum; }

	@Override
	public String toString() {
		return "case " + this.expression + " [CONSTANT=" + this.constant + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}

/** Provide an under-the-hood view of label expressions, peeling away any abstractions that package many expressions as one
 *  @return flattened array of label expressions
 */
public Expression [] peeledLabelExpressions() {
	Expression [] constants = Expression.NO_EXPRESSIONS;
	for (Expression e : this.constantExpressions) {
		if (e instanceof Pattern p)
			constants = Stream.concat(Arrays.stream(constants), Arrays.stream(p.getAlternatives())).toArray(Expression[]::new);
		else
			constants = Stream.concat(Arrays.stream(constants), Stream.of(e)).toArray(Expression[]::new);
	}
	return constants;
}

private boolean essentiallyQualifiedEnumerator(Expression e, TypeBinding selectorType) { // "Essentially" as in not "superfluously" qualified.
	return e instanceof NameReference reference && reference.binding instanceof FieldBinding field
				&& (field.modifiers & ClassFileConstants.AccEnum) != 0 && !TypeBinding.equalsEquals(e.resolvedType, selectorType); // <<-- essential qualification
}

private void checkDuplicateDefault(BlockScope scope, ASTNode node) {
	if (this.swich.defaultCase != null)
		scope.problemReporter().duplicateDefaultCase(node);
	else if (this.swich.unconditionalPatternCase != null)
		scope.problemReporter().illegalTotalPatternWithDefault(this);
	this.swich.defaultCase = this;
}

private Constant resolveConstantLabel(BlockScope scope, TypeBinding caseType, TypeBinding selectorType, Expression expression) {

	if (this.swich.expression.resolvedType != null && this.swich.expression.resolvedType.id == TypeIds.T_void)
		return Constant.NotAConstant;

	if (expression instanceof NullLiteral) {
		if (!caseType.isCompatibleWith(selectorType, scope))
			scope.problemReporter().caseConstantIncompatible(TypeBinding.NULL, selectorType, expression);
		return IntConstant.fromValue(-1);
	}

	if (expression instanceof StringLiteral) {
		if (selectorType.id == T_JavaLangString)
			return expression.constant;
		scope.problemReporter().caseConstantIncompatible(expression.resolvedType, selectorType, expression);
		return Constant.NotAConstant;
	}

	CompilerOptions options = scope.compilerOptions();
	if (caseType.isEnum() && caseType.isCompatibleWith(selectorType)) {
		if (((expression.bits & ASTNode.ParenthesizedMASK) >> ASTNode.ParenthesizedSHIFT) != 0)
			scope.problemReporter().enumConstantsCannotBeSurroundedByParenthesis(expression);

		if (expression instanceof NameReference reference && reference.binding instanceof FieldBinding field) {
			if ((field.modifiers & ClassFileConstants.AccEnum) == 0)
				 scope.problemReporter().enumSwitchCannotTargetField(reference, field);
			else if (reference instanceof QualifiedNameReference && options.complianceLevel < ClassFileConstants.JDK21)
				scope.problemReporter().cannotUseQualifiedEnumConstantInCaseLabel(reference, field);

			if (!TypeBinding.equalsEquals(caseType, selectorType)) {
				this.swich.switchBits |= SwitchStatement.QualifiedEnum;
				return StringConstant.fromValue(CharOperation.toString(reference.getName()));
			}
			return IntConstant.fromValue(field.original().id + 1); // (ordinal value + 1) zero should not be returned see bug 141810
		}
		scope.problemReporter().caseExpressionMustBeConstant(expression);
		return Constant.NotAConstant;
	}

	if (this.swich.isNonTraditional && selectorType.isBaseType() && !expression.isConstantValueOfTypeAssignableToType(caseType, selectorType)) {
		scope.problemReporter().caseConstantIncompatible(caseType, selectorType, expression);
		return Constant.NotAConstant;
	}

	if (expression.isConstantValueOfTypeAssignableToType(caseType, selectorType) || caseType.isCompatibleWith(selectorType)) {
		if (expression.constant == Constant.NotAConstant)
			scope.problemReporter().caseExpressionMustBeConstant(expression);
		return expression.constant;
	}

	boolean boxing = !JavaFeature.PATTERN_MATCHING_IN_SWITCH.isSupported(options) || this.swich.integralType(selectorType);
	if (boxing && isBoxingCompatible(caseType, selectorType, expression, scope)) {
		if (expression.constant == Constant.NotAConstant)
			scope.problemReporter().caseExpressionMustBeConstant(expression);
		return expression.constant;
	}
	scope.problemReporter().caseConstantIncompatible(expression.resolvedType, selectorType, expression);
	return Constant.NotAConstant;
}

private Constant resolvePatternLabel(BlockScope scope, TypeBinding caseType, TypeBinding selectorType, Pattern pattern, boolean isUnguarded) {

	Constant constant = IntConstant.fromValue(this.swich.labelExpressionIndex);

	if (pattern instanceof RecordPattern)
		this.swich.containsRecordPatterns = true;

	if (isUnguarded) {
		this.swich.caseLabelElementTypes.add(caseType);
		this.swich.caseLabelElements.add(pattern);
	}

	if (!caseType.isReifiable()) {
		if (!pattern.isApplicable(selectorType, scope, pattern))
			return Constant.NotAConstant;
	} else if (caseType.isValidBinding()) { // already complained if invalid
		if (pattern.findPrimitiveConversionRoute(caseType, selectorType, scope) == PrimitiveConversionRoute.NO_CONVERSION_ROUTE) {
			if (caseType.isPrimitiveType() && !JavaFeature.PRIMITIVES_IN_PATTERNS.isSupported(scope.compilerOptions())) {
				scope.problemReporter().unexpectedTypeinSwitchPattern(caseType, pattern);
				return Constant.NotAConstant;
			} else if (!pattern.checkCastTypesCompatibility(scope, caseType, selectorType, null, false)) {
				scope.problemReporter().typeMismatchError(selectorType, caseType, pattern, null);
				return Constant.NotAConstant;
			}
		} else {
			this.swich.isPrimitiveSwitch = true;
		}
	}
	if (pattern.coversType(selectorType, scope)) {
		this.swich.switchBits |= SwitchStatement.Exhaustive;
		pattern.isTotalTypeNode = true;
		if (pattern.isUnconditional(selectorType, scope)) // unguarded is implied from 'coversType()' above
			this.swich.unconditionalPatternCase = this;
	}
 	return constant;
}

@Override
public void resolve(BlockScope scope) {

	if (this.swich == null)
		return;

	TypeBinding selectorType = (this.swich.switchBits & SwitchStatement.InvalidSelector) != 0 ? null : this.swich.expression.resolvedType; // to inhibit secondary errors.
	this.labelExpressionOrdinal = this.swich.labelExpressionIndex;
	this.swich.cases[this.swich.caseCount++] = this;

	this.swich.switchBits |= this.isSwitchRule ? SwitchStatement.LabeledRules : SwitchStatement.LabeledBlockStatementGroup;
	if ((this.swich.switchBits & (SwitchStatement.LabeledRules | SwitchStatement.LabeledBlockStatementGroup)) == (SwitchStatement.LabeledRules | SwitchStatement.LabeledBlockStatementGroup))
		scope.problemReporter().arrowColonMixup(this);

	scope.enclosingCase = this; // record entering in a switch case block
	if (this.constantExpressions == Expression.NO_EXPRESSIONS) {
		checkDuplicateDefault(scope, this);
		return;
	}

	this.swich.switchBits |= SwitchStatement.HasNondefaultCase;
	int count = 0;
	int nullCaseCount = 0;
	for (Expression e : this.constantExpressions) {
		count++;
		if (e instanceof FakeDefaultLiteral) {
			this.swich.containsPatterns = this.swich.isNonTraditional = true;
			 checkDuplicateDefault(scope, this.constantExpressions.length > 1 ? e : this);
			 if (count != 2 || nullCaseCount < 1)
				 scope.problemReporter().patternSwitchCaseDefaultOnlyAsSecond(e);
			 continue;
		}
		if (e instanceof NullLiteral) {
			this.swich.containsNull = this.swich.isNonTraditional = true;
			if (this.swich.nullCase == null)
				this.swich.nullCase = this;
			nullCaseCount++;
		}

		// tag constant name with enum type for privileged access to its members
		if (selectorType != null && selectorType.isEnum() && (e instanceof SingleNameReference))
			((SingleNameReference) e).setActualReceiverType((ReferenceBinding)selectorType);

		e.setExpressionContext(ExpressionContext.TESTING_CONTEXT);
		if (e instanceof Pattern p) {
			this.swich.containsPatterns = this.swich.isNonTraditional =  true;
			p.setOuterExpressionType(selectorType);
		} else if (count > 1 && nullCaseCount == 1) {
			// Under if (!pattern) because we anyway issue ConstantWithPatternIncompatible for mixing patterns & null
			// Also multiple nulls get reported as duplicates and we don't want to complain again.
			scope.problemReporter().patternSwitchNullOnlyOrFirstWithDefault(e);
		}

		TypeBinding	caseType = e.resolveType(scope);
		if (caseType == null || selectorType == null)
			continue;

		if (caseType.isValidBinding()) {
			if (e instanceof Pattern) {
				for (Pattern p : ((Pattern) e).getAlternatives()) {
					Constant constant =  resolvePatternLabel(scope, p.resolvedType, selectorType, p, ((Pattern) e).isUnguarded());
					if (constant != Constant.NotAConstant)
						this.swich.gatherLabelExpression(new LabelExpression(constant, p, p.resolvedType, this.swich.labelExpressionIndex, false));
				}
			} else {
				// check from §14.11.1 (JEP 455):
				// For each case constant associated with the switch block that is a constant expression, one of the following is true:
				//  - [...]
				//  - if T is one of long, float, double, or boolean, the type of the case constant is T.
				//  - if T is one of Long, Float, Double, or Boolean, the type of the case constant is, respectively, long, float, double, or boolean.
				if (caseType.id != T_null) {
					TypeBinding expectedCaseType = selectorType.isBoxedPrimitiveType() && JavaFeature.PRIMITIVES_IN_PATTERNS.isSupported(scope.compilerOptions()) ? selectorType.unboxedType() : selectorType;
					switch (expectedCaseType.id) {
						case TypeIds.T_long, TypeIds.T_float, TypeIds.T_double, TypeIds.T_boolean -> {
							if (caseType.id != expectedCaseType.id) {
								scope.problemReporter().caseExpressionWrongType(e, selectorType, expectedCaseType);
								continue;
							}
							if (Pattern.findPrimitiveConversionRoute(caseType, selectorType, scope, this) != Pattern.PrimitiveConversionRoute.NO_CONVERSION_ROUTE)
								selectorType = expectedCaseType;
						}
					}
				}
				Constant constant = resolveConstantLabel(scope, caseType, selectorType, e);
				if (constant != Constant.NotAConstant) {
					int index = e instanceof NullLiteral ? -1 : this.swich.labelExpressionIndex;
					boolean isQualifiedEnum = essentiallyQualifiedEnumerator(e, selectorType);
					this.swich.gatherLabelExpression(new LabelExpression(constant, e, caseType, index, isQualifiedEnum));
				}
			}
		}
	}
}

@Override
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	if (!JavaFeature.UNNAMMED_PATTERNS_AND_VARS.isSupported(currentScope.compilerOptions()))
		for (LocalVariableBinding local : bindingsWhenTrue())
			local.useFlag = LocalVariableBinding.USED; // these are structurally required even if not touched

	for (Expression e : this.constantExpressions) {
		if (e instanceof NullLiteral && flowContext.associatedNode instanceof SwitchStatement swichStatement) {
			Expression switchValue = swichStatement.expression;
			if (switchValue != null && switchValue.nullStatus(flowInfo, flowContext) == FlowInfo.NON_NULL)
				currentScope.problemReporter().unnecessaryNullCaseInSwitchOverNonNull(this);
		}
		flowInfo = e.analyseCode(currentScope, flowContext, flowInfo);
	}
	return flowInfo;
}

@Override
public void generateCode(BlockScope currentScope, CodeStream codeStream) {
	if ((this.bits & ASTNode.IsReachable) == 0)
		return;

	int pc = codeStream.position;
	this.targetLabel.place();

	if (containsPatternVariable(true)) {

		BranchLabel patternMatchLabel = new BranchLabel(codeStream);
		BranchLabel matchFailLabel = new BranchLabel(codeStream);

		Pattern pattern = (Pattern) this.constantExpressions[0];
		codeStream.load(this.swich.selector);
		pattern.generateCode(currentScope, codeStream, patternMatchLabel, matchFailLabel);
		codeStream.goto_(patternMatchLabel);
		if (matchFailLabel.forwardReferenceCount() > 0) { // bother with generation of restart trampoline IFF match fail is possible
			matchFailLabel.place();
			/* We are generating a "thunk"/"trampoline" of sorts now, that flow analysis has no clue about.
			   We need to manage the live variables manually. Pattern bindings are not definitely
			   assigned here as we are in the else region.
		    */
			final LocalVariableBinding[] bindingsWhenTrue = pattern.bindingsWhenTrue();
			Stream.of(bindingsWhenTrue).forEach(v -> v.recordInitializationEndPC(codeStream.position));
			codeStream.load(this.swich.selector);
			int caseIndex = this.labelExpressionOrdinal + pattern.getAlternatives().length;
			codeStream.loadInt(this.swich.nullProcessed ? caseIndex - 1 : caseIndex);
			codeStream.goto_(this.swich.switchPatternRestartTarget);
			Stream.of(bindingsWhenTrue).forEach(v -> v.recordInitializationStartPC(codeStream.position));
		}
		patternMatchLabel.place();
	} else {
		if (this.swich.nullCase == this)
			this.swich.nullProcessed = true;
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}

@Override
public LocalVariableBinding[] bindingsWhenTrue() {
	LocalVariableBinding [] variables = NO_VARIABLES;
	for (Expression e : this.constantExpressions)
		variables = LocalVariableBinding.merge(variables, e.bindingsWhenTrue());
	return variables;
}

@Override
public StringBuilder printStatement(int tab, StringBuilder output) {
	printIndent(tab, output);
	if (this.constantExpressions == Expression.NO_EXPRESSIONS)
		output.append("default"); //$NON-NLS-1$
	else {
		output.append("case "); //$NON-NLS-1$
		for (int i = 0, length = this.constantExpressions.length; i < length; ++i) {
			this.constantExpressions[i].printExpression(0, output);
			if (i < length -1) output.append(',');
		}
	}
	return output.append(this.isSwitchRule ? " ->" : " :"); //$NON-NLS-1$ //$NON-NLS-2$
}

@Override
public void traverse(ASTVisitor visitor, 	BlockScope blockScope) {
	if (visitor.visit(this, blockScope)) {
		for (Expression e : this.constantExpressions)
			e.traverse(visitor, blockScope);
	}
	visitor.endVisit(this, blockScope);
}

public Boolean getBooleanConstantValue() {
	if (this.constantExpressions != null) {
		for (Expression expression : this.constantExpressions) {
			if (expression.constant instanceof BooleanConstant bc)
				return bc.booleanValue();
		}
	}
	return null;
}
}