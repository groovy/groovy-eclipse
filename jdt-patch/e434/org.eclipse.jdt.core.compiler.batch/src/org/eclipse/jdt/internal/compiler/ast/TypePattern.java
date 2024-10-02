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

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.JavaFeature;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.RecordComponentBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

public class TypePattern extends Pattern implements IGenerateTypeCheck {

	public LocalDeclaration local;

	private boolean isEitherOrPattern = false;

	public TypePattern(LocalDeclaration local) {
		this.local = local;
	}

	public static TypePattern createTypePattern(LocalDeclaration lokal) {
		if (lokal.name.length == 1 && lokal.name[0] == '_') {
			return new TypePattern(lokal) {
				@Override
				public boolean isUnnamed() {
					return true;
				}
			};
		}
		return new TypePattern(lokal);
	}

	@Override
	public TypeReference getType() {
		return this.local.type;
	}

	@Override
	public void setIsEitherOrPattern() {
		this.isEitherOrPattern = true;
	}

	@Override
	public LocalVariableBinding[] bindingsWhenTrue() {
		return this.isUnnamed() || this.local.binding == null ? NO_VARIABLES : new LocalVariableBinding[] { this.local.binding };
	}

	@Override
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		flowInfo = this.local.analyseCode(currentScope, flowContext, flowInfo);
		FlowInfo patternInfo = flowInfo.copy();

		if (this.isUnnamed())
			return patternInfo; // exclude anonymous blokes from flow analysis.

		patternInfo.markAsDefinitelyAssigned(this.local.binding);
		if (!this.isTotalTypeNode) {
			// non-total type patterns create a nonnull local:
			patternInfo.markAsDefinitelyNonNull(this.local.binding);
		} else {
			// total type patterns inherit the nullness of the value being switched over, unless ...
			if (flowContext.associatedNode instanceof SwitchStatement swStmt) {
				int nullStatus = swStmt.containsNull
						? FlowInfo.NON_NULL // ... null is handled in a separate case
						: swStmt.expression.nullStatus(patternInfo, flowContext);
				patternInfo.markNullStatus(this.local.binding, nullStatus);
			}
		}
		return patternInfo;
	}

	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream, BranchLabel patternMatchLabel, BranchLabel matchFailLabel) {
		generateTestingConversion(currentScope, codeStream);
		if (isUnnamed()) {
			if (this.getEnclosingPattern() == null || this.isTotalTypeNode) {
				switch (this.local.binding.type.id) {
					case T_long :
					case T_double :
						codeStream.pop2();
						break;
					default :
						codeStream.pop();
				}
			} // else we don't value on stack.
		} else {

			if (!this.isTotalTypeNode) {
				boolean checkCast = JavaFeature.PRIMITIVES_IN_PATTERNS.isSupported(currentScope.compilerOptions()) ?
								!this.local.binding.type.isBaseType() : true;
				if (checkCast)
					codeStream.checkcast(this.local.binding.type);
			}
			this.local.generateCode(currentScope, codeStream);
		}
	}

	public void generateTypeCheck(BlockScope scope, CodeStream codeStream, BranchLabel internalFalseLabel) {
		generateTypeCheck(this.outerExpressionType, getType(), scope, codeStream, internalFalseLabel,
				Pattern.findPrimitiveConversionRoute(this.resolvedType, this.accessorMethod.returnType, scope));
	}

	@Override
	public void setPatternIsTotalType() {
		this.isTotalTypeNode = true;
	}

	@Override
	public void generateTestingConversion(BlockScope scope, CodeStream codeStream) {
		TypeBinding provided = this.outerExpressionType;
		TypeBinding expected = this.resolvedType;
		PrimitiveConversionRoute route = Pattern.findPrimitiveConversionRoute(expected, provided, scope);
		switch (route) {
			case IDENTITY_CONVERSION:
				// Do nothing
				break;
			case WIDENING_PRIMITIVE_CONVERSION:
			case NARROWING_PRIMITVE_CONVERSION:
			case WIDENING_AND_NARROWING_PRIMITIVE_CONVERSION:
				this.computeConversion(scope, expected, provided);
				codeStream.generateImplicitConversion(this.implicitConversion);
				break;
			case BOXING_CONVERSION:
			case BOXING_CONVERSION_AND_WIDENING_REFERENCE_CONVERSION: // widening needs no conversion :)
				codeStream.generateBoxingConversion(provided.id);
				break;
			case WIDENING_REFERENCE_AND_UNBOXING_COVERSION:
				codeStream.generateUnboxingConversion(expected.id);
				break;
			case WIDENING_REFERENCE_AND_UNBOXING_COVERSION_AND_WIDENING_PRIMITIVE_CONVERSION:
				int rhsUnboxed = TypeIds.box2primitive(provided.superclass().id);
				codeStream.generateUnboxingConversion(rhsUnboxed);
				this.computeConversion(scope, TypeBinding.wellKnownBaseType(rhsUnboxed), expected);
				codeStream.generateImplicitConversion(this.implicitConversion);
				break;
			case NARROWING_AND_UNBOXING_CONVERSION:
				TypeBinding boxType = scope.environment().computeBoxingType(expected);
				codeStream.checkcast(boxType);
				codeStream.generateUnboxingConversion(expected.id);
				break;
			case UNBOXING_CONVERSION:
				codeStream.generateUnboxingConversion(expected.id);
				break;
			case UNBOXING_AND_WIDENING_PRIMITIVE_CONVERSION:
				this.computeConversion(scope, expected, provided);
				codeStream.generateImplicitConversion(this.implicitConversion);
				break;
			case NO_CONVERSION_ROUTE:
			default:
				break;
		}
	}

	@Override
	public boolean isUnconditional(TypeBinding t, Scope scope) {
		// §14.30.3: A type pattern that declares a pattern variable of a type S is unconditional for a type T
		// 			 if there is a testing conversion that is unconditionally exact (5.7.2) from |T| to |S|.
		// §5.7.2 lists:
		// * an identity conversion
		// * an exact widening primitive conversion
		// * a widening reference conversion
		// * a boxing conversion
		// * a boxing conversion followed by a widening reference conversion
		if (TypeBinding.equalsEquals(t, this.resolvedType))
			return true;
		PrimitiveConversionRoute route = findPrimitiveConversionRoute(this.resolvedType, t, scope);
		return switch(route) {
			case IDENTITY_CONVERSION,
				BOXING_CONVERSION,
				BOXING_CONVERSION_AND_WIDENING_REFERENCE_CONVERSION
				-> true;
			case WIDENING_PRIMITIVE_CONVERSION -> BaseTypeBinding.isExactWidening(this.resolvedType.id, t.id);
			case NO_CONVERSION_ROUTE -> { // a widening reference conversion?
				if (!this.resolvedType.isPrimitiveOrBoxedPrimitiveType() || !t.isPrimitiveOrBoxedPrimitiveType()) {
					yield t.isCompatibleWith(this.resolvedType);
				} else {
					yield false;
				}
			}
			default -> false;
		};
	}

	@Override
	public boolean dominates(Pattern p) {
		if (!isUnguarded())
			return false;
		if (p.resolvedType == null || this.resolvedType == null)
			return false;
		return p.resolvedType.erasure().isSubtypeOf(this.resolvedType.erasure(), false);
	}

	@Override
	public TypeBinding resolveType(BlockScope scope) {
		this.constant = Constant.NotAConstant;
		if (this.resolvedType != null)
			return this.resolvedType;

		Pattern enclosingPattern = this.getEnclosingPattern();
		if (this.local.type == null || this.local.type.isTypeNameVar(scope)) {
			if (enclosingPattern instanceof RecordPattern) {
				// 14.30.1: The type of a pattern variable declared in a nested type pattern is determined as follows ...
				ReferenceBinding recType = (ReferenceBinding) enclosingPattern.resolvedType;
				if (recType != null) {
					RecordComponentBinding[] components = recType.components();
					if (components.length > this.index) {
						RecordComponentBinding rcb = components[this.index];
						if (rcb.type != null && (rcb.tagBits & TagBits.HasMissingType) != 0) {
							scope.problemReporter().invalidType(this, rcb.type);
						}
						TypeVariableBinding[] mentionedTypeVariables = rcb.type != null ? rcb.type.syntheticTypeVariablesMentioned() : Binding.NO_TYPE_VARIABLES;
						this.resolvedType = mentionedTypeVariables.length > 0 ? rcb.type.upwardsProjection(scope, mentionedTypeVariables) : rcb.type;
						if (this.local.type != null)
							this.local.type.resolvedType = this.resolvedType;
					}
				}
			}
		}
		this.local.resolve(scope, true);
		if (this.local.binding != null) {
			this.local.binding.modifiers |= ExtraCompilerModifiers.AccOutOfFlowScope; // start out this way, will be BlockScope.include'd when definitely assigned
			if (enclosingPattern != null)
				this.local.binding.useFlag = LocalVariableBinding.USED; // syntactically required even if untouched
			if (this.local.type != null)
				this.resolvedType = this.local.binding.type;
		}

		if (this.isEitherOrPattern && !this.isUnnamed()) {
			scope.problemReporter().namedPatternVariablesDisallowedHere(this.local);
		}

		return this.resolvedType;
	}

	@Override
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			this.local.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}

	@Override
	public StringBuilder printExpression(int indent, StringBuilder output) {
		return this.local.printAsExpression(indent, output);
	}
}
