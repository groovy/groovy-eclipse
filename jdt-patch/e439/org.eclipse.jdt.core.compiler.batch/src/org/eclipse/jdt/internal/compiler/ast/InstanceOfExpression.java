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
 *     Stephan Herrmann - Contribution for
 *								bug 383368 - [compiler][null] syntactic null analysis for field references
 *								Bug 392238 - [1.8][compiler][null] Detect semantically invalid null type annotations
 *								Bug 416307 - [1.8][compiler][null] subclass with type parameter substitution confuses null checking
 *								Bug 392099 - [1.8][compiler][null] Apply null annotation on types for null analysis
 *								Bug 417295 - [1.8[[null] Massage type annotated null analysis to gel well with deep encoded type bindings.
 *								Bug 392238 - [1.8][compiler][null] Detect semantically invalid null type annotations
 *								Bug 435570 - [1.8][null] @NonNullByDefault illegally tries to affect "throws E"
 *								Bug 466713 - Null Annotations: NullPointerException using <int @Nullable []> as Type Param
 *        Andy Clement - Contributions for
 *                          Bug 383624 - [1.8][compiler] Revive code generation support for type annotations (from Olivier's work)
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import java.util.stream.Stream;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.Pattern.PrimitiveConversionRoute;
import org.eclipse.jdt.internal.compiler.ast.Pattern.TestContextRecord;
import org.eclipse.jdt.internal.compiler.ast.TypeReference.AnnotationPosition;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.JavaFeature;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class InstanceOfExpression extends OperatorExpression implements IGenerateTypeCheck {

	public Expression expression;
	public TypeReference type;
	public Pattern pattern;

	private static final char[] SECRET_EXPRESSION_VALUE = " secretExpressionValue".toCharArray(); //$NON-NLS-1$
	private LocalVariableBinding secretExpressionValue = null;

	private TestContextRecord testContextRecord;

public InstanceOfExpression(Expression expression, TypeReference type) {
	this.expression = expression;
	this.type = type;
	type.bits |= IgnoreRawTypeCheck; // https://bugs.eclipse.org/bugs/show_bug.cgi?id=282141
	this.bits |= INSTANCEOF << OperatorSHIFT;
	this.sourceStart = expression.sourceStart;
	this.sourceEnd = type.sourceEnd;
}

public InstanceOfExpression(Expression expression, Pattern pattern) {
	this.expression = expression;
	this.pattern = pattern;
	this.type = pattern.getType();
	this.type.bits |= IgnoreRawTypeCheck;
	this.bits |= INSTANCEOF << OperatorSHIFT;
	this.sourceStart = expression.sourceStart;
	this.sourceEnd = this.pattern.sourceEnd;
}

@Override
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	LocalVariableBinding local = this.expression.localVariableBinding();
	FlowInfo initsWhenTrue = null;
	if (local != null && (local.type.tagBits & TagBits.IsBaseType) == 0) {
		flowInfo = this.expression.analyseCode(currentScope, flowContext, flowInfo).
			unconditionalInits();
		initsWhenTrue = flowInfo.copy();
		initsWhenTrue.markAsComparedEqualToNonNull(local);
		flowContext.recordUsingNullReference(currentScope, local,
				this.expression, FlowContext.CAN_ONLY_NULL | FlowContext.IN_INSTANCEOF, flowInfo);
		// no impact upon enclosing try context
		flowInfo =  FlowInfo.conditional(initsWhenTrue.copy(), flowInfo.copy());
	} else if (this.expression instanceof Reference) {
		if (currentScope.compilerOptions().enableSyntacticNullAnalysisForFields) {
			FieldBinding field = ((Reference)this.expression).lastFieldBinding();
			if (field != null && (field.type.tagBits & TagBits.IsBaseType) == 0) {
				flowContext.recordNullCheckedFieldReference((Reference) this.expression, 1, FlowInfo.NON_NULL);
			}
		}
	}
	if (initsWhenTrue == null) {
		flowInfo = this.expression.analyseCode(currentScope, flowContext, flowInfo).
				unconditionalInits();
	}
	if (this.pattern != null) {
		FlowInfo patternFlow = this.pattern.analyseCode(currentScope, flowContext, (initsWhenTrue == null) ? flowInfo : initsWhenTrue);
		initsWhenTrue = initsWhenTrue == null ? patternFlow : initsWhenTrue.addInitializationsFrom(patternFlow);
	}
	return (initsWhenTrue == null) ? flowInfo :
			FlowInfo.conditional(initsWhenTrue, flowInfo.copy());
}

@Override
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {

	BranchLabel falseLabel, continueLabel;

	if (this.pattern != null) {
		falseLabel = new BranchLabel(codeStream);
		continueLabel = new BranchLabel(codeStream);
	} else {
		falseLabel = null;
		continueLabel = null;
	}

	generateOptimizedBoolean(currentScope, codeStream, null, falseLabel, true);

	if (this.pattern != null) {

		if (valueRequired) {
			codeStream.iconst_1();
			codeStream.goto_(continueLabel);
		}
		falseLabel.place();
		/* We are generating a "thunk" of sorts now, that flow analysis has no clue about.
		   so, we need to manage the live variables manually. Pattern bindings are not definitely
		   assigned here as we are in instanceof false region.
	    */
		Stream.of(bindingsWhenTrue()).forEach(v->v.recordInitializationEndPC(codeStream.position));

		if (valueRequired)
			codeStream.iconst_0();

		continueLabel.place();
	}

	if (valueRequired) {
		codeStream.generateImplicitConversion(this.implicitConversion);
	} else if (this.pattern == null) {
		codeStream.pop(); // in the pattern case, if (!valueRequired), we have nothing on the stack to pop
	}

	codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd);
}

@Override
public void generateOptimizedBoolean(BlockScope currentScope, CodeStream codeStream, BranchLabel trueLabel, BranchLabel falseLabel, boolean valueRequired) {

	/* A label valued to null is supposed to mean: by default we fall through the case ...
	   Both null means no "optimization" and we leave the value on the stack
	   But we have trouble when
	        this.pattern != null && trueLabel != null && falseLabel == null

	   In this case, since we have no control over placement of the trueLabel, we won't know where to emit the pattern binding code.
	   So what we do is always emit ifeq even when optimization would call for ifne and treat the whole "blob" of pattern matching
	   code as part of the predicate. if you think about long and hard you can convince yourself this is semantically correct.
	*/

	int pc = codeStream.position;

	this.expression.generateCode(currentScope, codeStream, true);
	if (this.secretExpressionValue != null) {
		codeStream.store(this.secretExpressionValue, true);
		codeStream.addVariable(this.secretExpressionValue);
	}

	BranchLabel internalFalseLabel = falseLabel != null ? falseLabel : this.pattern != null ? new BranchLabel(codeStream) : null;
	PrimitiveConversionRoute route = PrimitiveConversionRoute.NO_CONVERSION_ROUTE;
	TypeBinding providedType = null;
	if (this.testContextRecord != null) {
		route = this.testContextRecord.route();
		providedType = this.testContextRecord.right();
	}
	generateTypeCheck(providedType, this.type, currentScope, codeStream, route);

	if (this.pattern != null) {
		codeStream.ifeq(internalFalseLabel);
		if (this.secretExpressionValue != null) {
			codeStream.load(this.secretExpressionValue);
			codeStream.removeVariable(this.secretExpressionValue);
		} else {
			this.expression.generateCode(currentScope, codeStream, true);
		}
		this.pattern.generateCode(currentScope, codeStream, trueLabel, internalFalseLabel);
	} else if (!valueRequired) {
		codeStream.pop();
	}

	codeStream.recordPositionsFrom(pc, this.sourceStart);

	int position = codeStream.position;

	if (valueRequired) {
		if (falseLabel == null) {
			if (trueLabel != null) {
				if (this.pattern != null) {
					// Implicit falling through the FALSE case, any bindings defined when true cease to be live
					Stream.of(bindingsWhenTrue()).forEach(v->v.recordInitializationEndPC(codeStream.position));
					codeStream.goto_(trueLabel);
				} else {
					codeStream.ifne(trueLabel);
				}
			}
		} else {
			if (trueLabel == null) {
				// Implicit falling through the TRUE case
				if (this.pattern == null)
					codeStream.ifeq(falseLabel);
			} else {
				// No implicit fall through TRUE/FALSE --> classic instanceof code generation called from generateCode above
			}
		}
	}
	codeStream.recordPositionsFrom(position, this.sourceEnd);

	if (internalFalseLabel != falseLabel)
		internalFalseLabel.place();
}

@Override
public void setPatternIsTotalType() {
	if (this.pattern != null) {
		this.pattern.isTotalTypeNode = true;
	}
}

@Override
public void consumeProvidedValue(TypeBinding provided, CodeStream codeStream) {
	LocalVariableBinding local = this.expression.localVariableBinding();
	local = local != null ? local : this.secretExpressionValue;
	if (local != null)
		codeStream.store(local, false);
}

@Override
public StringBuilder printExpressionNoParenthesis(int indent, StringBuilder output) {
	this.expression.printExpression(indent, output).append(" instanceof "); //$NON-NLS-1$
	return this.pattern == null ? this.type.print(0, output) : this.pattern.printExpression(0, output);
}

@Override
public LocalVariableBinding[] bindingsWhenTrue() {
	return this.pattern != null ? this.pattern.bindingsWhenTrue() : NO_VARIABLES;
}

@Override
public TypeBinding resolveType(BlockScope scope) {
	this.constant = Constant.NotAConstant;

	TypeBinding checkedType = this.type.resolveType(scope, true /* check bounds*/);
	if (this.expression instanceof CastExpression) {
		((CastExpression) this.expression).setInstanceofType(checkedType); // for cast expression we need to know instanceof type to not tag unnecessary when needed
	}
	TypeBinding expressionType = this.expression.resolveType(scope);
	if (this.pattern != null) {
		this.expression.computeConversion(scope, expressionType, expressionType); // avoid that a total pattern would skip a checkCast, needed due to generics
		this.pattern.setExpressionContext(ExpressionContext.TESTING_CONTEXT);
		this.pattern.setOuterExpressionType(this.expression.resolvedType);
		this.pattern.resolveType(scope);

		addSecretExpressionValue(scope, expressionType);
	}
	if (expressionType != null && checkedType != null && this.type.hasNullTypeAnnotation(AnnotationPosition.ANY)) {
		// don't complain if the entire operation is redundant anyway
		if (!expressionType.isCompatibleWith(checkedType) || NullAnnotationMatching.analyse(checkedType, expressionType, -1).isAnyMismatch())
			scope.problemReporter().nullAnnotationUnsupportedLocation(this.type);
	}

	if (expressionType == null || checkedType == null)
		return null;

	CompilerOptions options = scope.compilerOptions();
	if (this.pattern != null) {
		if (this.pattern.isApplicable(expressionType, scope, this))
			checkForPrimitives(scope, checkedType, expressionType);
		if (options.complianceLevel < ClassFileConstants.JDK21 && expressionType.isSubtypeOf(checkedType, false))
			scope.problemReporter().expressionTypeCannotBeSubtypeOfPatternType(this.expression);
		return this.resolvedType = TypeBinding.BOOLEAN;
	}

	if (!checkedType.isReifiable()) {
		// Report same as before for older compliances
		if (options.complianceLevel < ClassFileConstants.JDK16) {
			scope.problemReporter().illegalInstanceOfGenericType(checkedType, this);
		} else {
			if (expressionType != TypeBinding.NULL) {
				boolean isLegal = checkCastTypesCompatibility(scope, checkedType, expressionType, this.expression, true);
				if (!isLegal || (this.bits & ASTNode.UnsafeCast) != 0) {
					scope.problemReporter().unsafeCastInTestingContext(this.expression, checkedType, expressionType);
				} else  {
					checkRefForPrimitivesAndAddSecretVariable(scope, checkedType, expressionType);
				}
			}
		}
	} else if (checkedType.isValidBinding()) {
		// if not a valid binding, an error has already been reported for unresolved type
		checkForPrimitives(scope, checkedType, expressionType);
	}

	return this.resolvedType = TypeBinding.BOOLEAN;
}

private void checkForPrimitives(BlockScope scope, TypeBinding checkedType, TypeBinding expressionType) {
	boolean needToCheck = (expressionType != TypeBinding.NULL && expressionType.isBaseType()) // disallow autoboxing
				|| checkedType.isBaseType()
				|| !checkCastTypesCompatibility(scope, checkedType, expressionType, null, true);
	if (!needToCheck)
		return;
	PrimitiveConversionRoute route = Pattern.findPrimitiveConversionRoute(checkedType, expressionType, scope, this);
	this.testContextRecord = new TestContextRecord(checkedType, expressionType, route);

	if (route == PrimitiveConversionRoute.WIDENING_PRIMITIVE_CONVERSION
			|| route == PrimitiveConversionRoute.NARROWING_PRIMITVE_CONVERSION
			|| route == PrimitiveConversionRoute.WIDENING_AND_NARROWING_PRIMITIVE_CONVERSION) {

		// Do Nothing - no additional steps required for conversion later.

	} else if (route == PrimitiveConversionRoute.BOXING_CONVERSION
			|| route == PrimitiveConversionRoute.BOXING_CONVERSION_AND_WIDENING_REFERENCE_CONVERSION) {
		addSecretExpressionValue(scope, expressionType);
	} else if (route == PrimitiveConversionRoute.NO_CONVERSION_ROUTE) {
		scope.problemReporter().notCompatibleTypesError(this, expressionType, checkedType);
	}
}

private void checkRefForPrimitivesAndAddSecretVariable(BlockScope scope, TypeBinding checkedType, TypeBinding expressionType) {
	if (!JavaFeature.PRIMITIVES_IN_PATTERNS.isSupported(scope.compilerOptions()))
		return;
	PrimitiveConversionRoute route = Pattern.findPrimitiveConversionRoute(checkedType, expressionType, scope, this);
	this.testContextRecord = new TestContextRecord(checkedType, expressionType, route);
}

private void addSecretExpressionValue(BlockScope scope, TypeBinding expressionType) {
	if ((this.expression.bits & ASTNode.RestrictiveFlagMASK) != Binding.LOCAL) {

		TypeBinding type1 = (this.expression.resolvedType != null
				&& this.expression.resolvedType.isBaseType()) ?
				this.expression.resolvedType : TypeBinding.wellKnownType(scope, T_JavaLangObject);
		// reevaluation may double jeopardize as side effects may recur, compute once and cache
		LocalVariableBinding local =
				new LocalVariableBinding(
					InstanceOfExpression.SECRET_EXPRESSION_VALUE,
					type1,
					ClassFileConstants.AccDefault,
					false);
		local.setConstant(Constant.NotAConstant);
		local.useFlag = LocalVariableBinding.USED;
		scope.addLocalVariable(local);
		this.secretExpressionValue = local;
		if (expressionType != TypeBinding.NULL)
			this.secretExpressionValue.type = expressionType;
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
public void tagAsUnnecessaryCast(Scope scope, TypeBinding castType) {
	// record deconstruction and binding type pattern variable are legitimate operations.
	if (this.pattern != null)
		return;
	// null is not instanceof Type, recognize direct scenario
	if (this.expression.resolvedType != TypeBinding.NULL)
		scope.problemReporter().unnecessaryInstanceof(this, castType);
}

@Override
public void traverse(ASTVisitor visitor, BlockScope scope) {
	if (visitor.visit(this, scope)) {
		this.expression.traverse(visitor, scope);
		if (this.pattern != null) {
			this.pattern.traverse(visitor, scope);
		} else {
			this.type.traverse(visitor, scope);
		}
	}
	visitor.endVisit(this, scope);
}
}