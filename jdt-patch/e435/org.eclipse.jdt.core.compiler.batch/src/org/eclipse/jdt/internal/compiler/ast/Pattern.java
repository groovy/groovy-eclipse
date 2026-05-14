/*******************************************************************************
 * Copyright (c) 2021, 2024 IBM Corporation and others.
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

import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.impl.JavaFeature;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.NullTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.VoidTypeBinding;

public abstract class Pattern extends Expression {

	boolean isTotalTypeNode = false;

	private Pattern enclosingPattern;

	protected MethodBinding accessorMethod;

	public int index = -1; // index of this in enclosing record pattern, or -1 for top level patterns

	public boolean isUnguarded = true; // no guard or guard is compile time constant true.

	public enum PrimitiveConversionRoute {
		IDENTITY_CONVERSION,
		WIDENING_PRIMITIVE_CONVERSION,
		NARROWING_PRIMITVE_CONVERSION,
		WIDENING_AND_NARROWING_PRIMITIVE_CONVERSION,
		BOXING_CONVERSION,
		BOXING_CONVERSION_AND_WIDENING_REFERENCE_CONVERSION,
		// following for reference
		WIDENING_REFERENCE_AND_UNBOXING_COVERSION,
		WIDENING_REFERENCE_AND_UNBOXING_COVERSION_AND_WIDENING_PRIMITIVE_CONVERSION,
		NARROWING_AND_UNBOXING_CONVERSION,
		UNBOXING_CONVERSION,
		UNBOXING_AND_WIDENING_PRIMITIVE_CONVERSION,
		NO_CONVERSION_ROUTE
	}

	protected TypeBinding outerExpressionType; // the expression type of the enclosing instanceof, switch or outer record pattern

	record TestContextRecord(TypeBinding left, TypeBinding right, PrimitiveConversionRoute route) {}

	public Pattern getEnclosingPattern() {
		return this.enclosingPattern;
	}

	public void setEnclosingPattern(RecordPattern enclosingPattern) {
		this.enclosingPattern = enclosingPattern;
	}

	public boolean isUnnamed() {
		return false;
	}

	/**
	 * Implement the rules in the spec under 14.11.1.1 Exhaustive Switch Blocks
	 *
	 * @return whether pattern covers the given type or not
	 */
	public boolean coversType(TypeBinding type, Scope scope) {
		if (!isUnguarded())
			return false;
		if (type == null || this.resolvedType == null)
			return false;
		if (type instanceof TypeVariableBinding && type.superclass().isBoxedPrimitiveType())
			type = type.superclass(); // when a boxing type is in supers it must be superclass, because all boxing types are classes
		if (type.isPrimitiveOrBoxedPrimitiveType()) {
			PrimitiveConversionRoute route = Pattern.findPrimitiveConversionRoute(this.resolvedType, type, scope);
			switch (route) {
				// JLS §5.7.2:
				case IDENTITY_CONVERSION:
				case BOXING_CONVERSION:
				case BOXING_CONVERSION_AND_WIDENING_REFERENCE_CONVERSION:
					return true;
				case WIDENING_PRIMITIVE_CONVERSION:
					return BaseTypeBinding.isExactWidening(this.resolvedType.id, type.id);
				case WIDENING_AND_NARROWING_PRIMITIVE_CONVERSION:
					return false; // char->byte
				/* §14.11.1.1 "CE contains a type pattern with a primitive type P,
				 * 		 	and T is the wrapper class for the primitive type W,
				 *			and the conversion from type W to type P is unconditionally exact (5.7.2). */
				case UNBOXING_CONVERSION:
					return true; // W -> P is identity
				case UNBOXING_AND_WIDENING_PRIMITIVE_CONVERSION:
					return BaseTypeBinding.isExactWidening(this.resolvedType.id, TypeIds.box2primitive(type.id));
				default:
					break;
			}
		}
		if (type.isSubtypeOf(this.resolvedType, false))
			return true;
		return false;
	}

	// Given a non-null instance of same type, would the pattern always match ?
	public boolean matchFailurePossible() {
		return false;
	}

	public boolean isUnconditional(TypeBinding t, Scope scope) {
		return false;
	}

	public abstract void generateCode(BlockScope currentScope, CodeStream codeStream, BranchLabel patternMatchLabel, BranchLabel matchFailLabel);

	public void generateTestingConversion(BlockScope scope, CodeStream codeStream) {
		// TODO: MAKE THIS abstract
	}


	@Override
	public boolean checkUnsafeCast(Scope scope, TypeBinding castType, TypeBinding expressionType, TypeBinding match, boolean isNarrowing) {
		if (!castType.isReifiable())
			return CastExpression.checkUnsafeCast(this, scope, castType, expressionType, match, isNarrowing);
		else
			return super.checkUnsafeCast(scope, castType, expressionType, match, isNarrowing);
	}

	public TypeReference getType() {
		return null;
	}

	// 14.30.3 Properties of Patterns: A pattern p is said to be applicable at a type T if ...
	protected boolean isApplicable(TypeBinding expressionType, BlockScope scope, ASTNode location) {
		if (expressionType == TypeBinding.NULL)
			return true;
		TypeReference typeRef = getType();
		if (typeRef == null)
			return true; // nothing to be checked for wildcard '_'
		TypeBinding patternType = typeRef.resolvedType;
		if (patternType == null || !patternType.isValidBinding() || !expressionType.isValidBinding())
			return false; // problem already reported

		// 14.30.3 Properties of Patterns doesn't allow boxing nor unboxing, primitive widening/narrowing (< JLS23)
		if (patternType.isBaseType() != expressionType.isBaseType() && !JavaFeature.PRIMITIVES_IN_PATTERNS.isSupported(scope.compilerOptions())) {
			scope.problemReporter().notCompatibleTypesError(location, expressionType, patternType);
			return false;
		}
		if (patternType.isBaseType()) {
			PrimitiveConversionRoute route = Pattern.findPrimitiveConversionRoute(this.resolvedType, this.outerExpressionType, scope);
			if (!TypeBinding.equalsEquals(expressionType, patternType)
					&& route == PrimitiveConversionRoute.NO_CONVERSION_ROUTE) {
				scope.problemReporter().notCompatibleTypesError(location, expressionType, patternType);
				return false;
			}
		} else {
			if (!checkCastTypesCompatibility(scope, patternType, expressionType, null, true)) {
				scope.problemReporter().notCompatibleTypesError(location, expressionType, patternType);
				return false;
			}
			if ((this.bits & ASTNode.UnsafeCast) != 0) {
				scope.problemReporter().unsafeCastInTestingContext(location, patternType, this.outerExpressionType);
				return false;
			}
		}
		return true;
	}

	public abstract boolean dominates(Pattern p);

	@Override
	public StringBuilder print(int indent, StringBuilder output) {
		return this.printExpression(indent, output);
	}

	public Pattern[] getAlternatives() {
		return new Pattern [] { this };
	}

	public abstract void setIsEitherOrPattern(); // if set, is one of multiple (case label) patterns and so pattern variables can't be named.

	public void setOuterExpressionType(TypeBinding expressionType) {
		this.outerExpressionType = expressionType;
	}

	public boolean isUnguarded() {
		return this.isUnguarded;
	}

	public void setIsGuarded() {
		this.isUnguarded = false;
	}
	public static boolean isBoxing(TypeBinding provided, TypeBinding expected) {

		if (expected.isBaseType() && !provided.isBaseType()) {
			int expectedId = switch(expected.id) {
				case T_char     -> T_JavaLangCharacter;
				case T_byte     -> T_JavaLangByte;
				case T_short    -> T_JavaLangShort;
				case T_boolean  -> T_JavaLangBoolean;
				case T_long     -> T_JavaLangLong;
				case T_double   -> T_JavaLangDouble;
				case T_float    -> T_JavaLangFloat;
				case T_int      -> T_JavaLangInteger;
				default -> -1;
			};
			return provided.id == expectedId;
		}
		return false;
	}
	public static PrimitiveConversionRoute findPrimitiveConversionRoute(TypeBinding destinationType, TypeBinding expressionType, Scope scope) {
		if (!JavaFeature.PRIMITIVES_IN_PATTERNS.isSupported(scope.compilerOptions()))
			return PrimitiveConversionRoute.NO_CONVERSION_ROUTE;
		if (destinationType == null || expressionType == null)
			return PrimitiveConversionRoute.NO_CONVERSION_ROUTE;
		boolean destinationIsBaseType = destinationType.isBaseType();
		boolean expressionIsBaseType = expressionType.isBaseType();
		if (destinationIsBaseType && expressionIsBaseType) {
			if (TypeBinding.equalsEquals(destinationType, expressionType)) {
				return PrimitiveConversionRoute.IDENTITY_CONVERSION;
			}
			if (BaseTypeBinding.isWideningAndNarrowing(destinationType.id, expressionType.id))
				return PrimitiveConversionRoute.WIDENING_AND_NARROWING_PRIMITIVE_CONVERSION;
			if (BaseTypeBinding.isWidening(destinationType.id, expressionType.id))
				return PrimitiveConversionRoute.WIDENING_PRIMITIVE_CONVERSION;
			if (BaseTypeBinding.isNarrowing(destinationType.id, expressionType.id))
				return PrimitiveConversionRoute.NARROWING_PRIMITVE_CONVERSION;
		} else {
			if (expressionIsBaseType) {
				if (expressionType instanceof NullTypeBinding
						|| expressionType instanceof VoidTypeBinding)
					return PrimitiveConversionRoute.NO_CONVERSION_ROUTE;

				if (isBoxing(destinationType, expressionType))
					return PrimitiveConversionRoute.BOXING_CONVERSION;
				if (scope.environment().computeBoxingType(expressionType).isCompatibleWith(destinationType))
					return PrimitiveConversionRoute.BOXING_CONVERSION_AND_WIDENING_REFERENCE_CONVERSION;

			} else if (expressionType.isBoxedPrimitiveType() && destinationIsBaseType) {
				TypeBinding unboxedExpressionType = scope.environment().computeBoxingType(expressionType);
				 //an unboxing conversion (5.1.8)
				if (TypeBinding.equalsEquals(destinationType, unboxedExpressionType))
					return PrimitiveConversionRoute.UNBOXING_CONVERSION;
				 //an unboxing conversion followed by a widening primitive conversion
				if (BaseTypeBinding.isWidening(destinationType.id, unboxedExpressionType.id))
					return PrimitiveConversionRoute.UNBOXING_AND_WIDENING_PRIMITIVE_CONVERSION;
			} else if (destinationIsBaseType) {
				if (expressionType instanceof TypeVariableBinding && expressionType.superclass().isBoxedPrimitiveType()) { // <T extends Integer> / <? extends Short> ...
					int boxId = expressionType.superclass().id;
					int exprPrimId = TypeIds.box2primitive(boxId);
					if (exprPrimId == destinationType.id)
						return PrimitiveConversionRoute.WIDENING_REFERENCE_AND_UNBOXING_COVERSION;
					if (BaseTypeBinding.isWidening(destinationType.id, exprPrimId))
						return PrimitiveConversionRoute.WIDENING_REFERENCE_AND_UNBOXING_COVERSION_AND_WIDENING_PRIMITIVE_CONVERSION;
				}
				TypeBinding boxedDestinationType = scope.environment().computeBoxingType(destinationType);
				// a narrowing reference conversion that is checked followed by an unboxing conversion
				// TODO: check relevance of 'checked', as well as use of erasure() below
				if (boxedDestinationType.isCompatibleWith(expressionType.erasure()))
					return PrimitiveConversionRoute.NARROWING_AND_UNBOXING_CONVERSION;
			}
		}
		return PrimitiveConversionRoute.NO_CONVERSION_ROUTE;
	}
}