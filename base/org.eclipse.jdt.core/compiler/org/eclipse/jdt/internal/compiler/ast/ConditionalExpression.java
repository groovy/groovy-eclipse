/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class ConditionalExpression extends OperatorExpression {

	public Expression condition, valueIfTrue, valueIfFalse;
	public Constant optimizedBooleanConstant;
	public Constant optimizedIfTrueConstant;
	public Constant optimizedIfFalseConstant;
	
	// for local variables table attributes
	int trueInitStateIndex = -1;
	int falseInitStateIndex = -1;
	int mergedInitStateIndex = -1;
	
	public ConditionalExpression(
		Expression condition,
		Expression valueIfTrue,
		Expression valueIfFalse) {
		this.condition = condition;
		this.valueIfTrue = valueIfTrue;
		this.valueIfFalse = valueIfFalse;
		sourceStart = condition.sourceStart;
		sourceEnd = valueIfFalse.sourceEnd;
	}

public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext,
		FlowInfo flowInfo) {
		Constant cst = this.condition.optimizedBooleanConstant();
		boolean isConditionOptimizedTrue = cst != Constant.NotAConstant && cst.booleanValue() == true;
		boolean isConditionOptimizedFalse = cst != Constant.NotAConstant && cst.booleanValue() == false;

		int mode = flowInfo.reachMode();
		flowInfo = condition.analyseCode(currentScope, flowContext, flowInfo, cst == Constant.NotAConstant);
		
		// process the if-true part
		FlowInfo trueFlowInfo = flowInfo.initsWhenTrue().copy();
		if (isConditionOptimizedFalse) {
			trueFlowInfo.setReachMode(FlowInfo.UNREACHABLE); 
		}
		trueInitStateIndex = currentScope.methodScope().recordInitializationStates(trueFlowInfo);
		trueFlowInfo = valueIfTrue.analyseCode(currentScope, flowContext, trueFlowInfo);

		// process the if-false part
		FlowInfo falseFlowInfo = flowInfo.initsWhenFalse().copy();
		if (isConditionOptimizedTrue) {
			falseFlowInfo.setReachMode(FlowInfo.UNREACHABLE); 
		}
		falseInitStateIndex = currentScope.methodScope().recordInitializationStates(falseFlowInfo);
		falseFlowInfo = valueIfFalse.analyseCode(currentScope, flowContext, falseFlowInfo);

		// merge if-true & if-false initializations
		FlowInfo mergedInfo;
		if (isConditionOptimizedTrue){
			mergedInfo = trueFlowInfo.addPotentialInitializationsFrom(falseFlowInfo);
		} else if (isConditionOptimizedFalse) {
			mergedInfo = falseFlowInfo.addPotentialInitializationsFrom(trueFlowInfo);
		} else {
			// if ((t && (v = t)) ? t : t && (v = f)) r = v;  -- ok
			cst = this.optimizedIfTrueConstant;
			boolean isValueIfTrueOptimizedTrue = cst != null && cst != Constant.NotAConstant && cst.booleanValue() == true;
			boolean isValueIfTrueOptimizedFalse = cst != null && cst != Constant.NotAConstant && cst.booleanValue() == false;
			
			cst = this.optimizedIfFalseConstant;
			boolean isValueIfFalseOptimizedTrue = cst != null && cst != Constant.NotAConstant && cst.booleanValue() == true;
			boolean isValueIfFalseOptimizedFalse = cst != null && cst != Constant.NotAConstant && cst.booleanValue() == false;

			UnconditionalFlowInfo trueInfoWhenTrue = trueFlowInfo.initsWhenTrue().unconditionalCopy();
			UnconditionalFlowInfo falseInfoWhenTrue = falseFlowInfo.initsWhenTrue().unconditionalCopy();
			UnconditionalFlowInfo trueInfoWhenFalse = trueFlowInfo.initsWhenFalse().unconditionalInits();
			UnconditionalFlowInfo falseInfoWhenFalse = falseFlowInfo.initsWhenFalse().unconditionalInits();
			if (isValueIfTrueOptimizedFalse) trueInfoWhenTrue.setReachMode(FlowInfo.UNREACHABLE); 
			if (isValueIfFalseOptimizedFalse) falseInfoWhenTrue.setReachMode(FlowInfo.UNREACHABLE); 
			if (isValueIfTrueOptimizedTrue) trueInfoWhenFalse.setReachMode(FlowInfo.UNREACHABLE); 
			if (isValueIfFalseOptimizedTrue) falseInfoWhenFalse.setReachMode(FlowInfo.UNREACHABLE); 

			mergedInfo =
				FlowInfo.conditional(
					trueInfoWhenTrue.mergedWith(falseInfoWhenTrue),
					trueInfoWhenFalse.mergedWith(falseInfoWhenFalse));
		}
		mergedInitStateIndex =
			currentScope.methodScope().recordInitializationStates(mergedInfo);
		mergedInfo.setReachMode(mode);
		return mergedInfo;
	}

	/**
	 * Code generation for the conditional operator ?:
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 * @param valueRequired boolean
	*/
	public void generateCode(
		BlockScope currentScope,
		CodeStream codeStream,
		boolean valueRequired) {

		int pc = codeStream.position;
		BranchLabel endifLabel, falseLabel;
		if (constant != Constant.NotAConstant) {
			if (valueRequired)
				codeStream.generateConstant(constant, implicitConversion);
			codeStream.recordPositionsFrom(pc, this.sourceStart);
			return;
		}
		Constant cst = condition.optimizedBooleanConstant();
		boolean needTruePart = !(cst != Constant.NotAConstant && cst.booleanValue() == false);
		boolean needFalsePart = 	!(cst != Constant.NotAConstant && cst.booleanValue() == true);
		endifLabel = new BranchLabel(codeStream);

		// Generate code for the condition
		falseLabel = new BranchLabel(codeStream);
		falseLabel.tagBits |= BranchLabel.USED;
		condition.generateOptimizedBoolean(
			currentScope,
			codeStream,
			null,
			falseLabel,
			cst == Constant.NotAConstant);

		if (trueInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(
				currentScope,
				trueInitStateIndex);
			codeStream.addDefinitelyAssignedVariables(currentScope, trueInitStateIndex);
		}
		// Then code generation
		if (needTruePart) {
			valueIfTrue.generateCode(currentScope, codeStream, valueRequired);
			if (needFalsePart) {
				// Jump over the else part
				int position = codeStream.position;
				codeStream.goto_(endifLabel);
				codeStream.updateLastRecordedEndPC(currentScope, position);
				// Tune codestream stack size
				if (valueRequired) {
					codeStream.decrStackSize(this.resolvedType == TypeBinding.LONG || this.resolvedType == TypeBinding.DOUBLE ? 2 : 1);
				}
			}
		}
		if (needFalsePart) {
			if (falseInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(
					currentScope,
					falseInitStateIndex);
				codeStream.addDefinitelyAssignedVariables(currentScope, falseInitStateIndex);
			}
			if (falseLabel.forwardReferenceCount() > 0) {
				falseLabel.place();
			}
			valueIfFalse.generateCode(currentScope, codeStream, valueRequired);
			if (valueRequired) {
				codeStream.recordExpressionType(this.resolvedType);
			}
			if (needTruePart) {
				// End of if statement
				endifLabel.place();
			}
		}
		// May loose some local variable initializations : affecting the local variable attributes
		if (mergedInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(
				currentScope,
				mergedInitStateIndex);
		}
		// implicit conversion
		if (valueRequired)
			codeStream.generateImplicitConversion(implicitConversion);
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	/**
	 * Optimized boolean code generation for the conditional operator ?:
	*/
	public void generateOptimizedBoolean(
		BlockScope currentScope,
		CodeStream codeStream,
		BranchLabel trueLabel,
		BranchLabel falseLabel,
		boolean valueRequired) {

		if ((constant != Constant.NotAConstant) && (constant.typeID() == T_boolean) // constant
			|| ((valueIfTrue.implicitConversion & IMPLICIT_CONVERSION_MASK) >> 4) != T_boolean) { // non boolean values
			super.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel, valueRequired);
			return;
		}
		Constant cst = condition.constant;
		Constant condCst = condition.optimizedBooleanConstant();
		boolean needTruePart =
			!(((cst != Constant.NotAConstant) && (cst.booleanValue() == false))
				|| ((condCst != Constant.NotAConstant) && (condCst.booleanValue() == false)));
		boolean needFalsePart =
			!(((cst != Constant.NotAConstant) && (cst.booleanValue() == true))
				|| ((condCst != Constant.NotAConstant) && (condCst.booleanValue() == true)));

		BranchLabel internalFalseLabel, endifLabel = new BranchLabel(codeStream);

		// Generate code for the condition
		boolean needConditionValue = (cst == Constant.NotAConstant) && (condCst == Constant.NotAConstant);
		condition.generateOptimizedBoolean(
				currentScope,
				codeStream,
				null,
				internalFalseLabel = new BranchLabel(codeStream),
				needConditionValue);

		if (trueInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(
				currentScope,
				trueInitStateIndex);
			codeStream.addDefinitelyAssignedVariables(currentScope, trueInitStateIndex);
		}
		// Then code generation
		if (needTruePart) {
			valueIfTrue.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel, valueRequired);
			
			if (needFalsePart) {
				// Jump over the else part
				JumpEndif: {
					if (falseLabel == null) {
						if (trueLabel != null) {
							// implicit falling through the FALSE case
							cst = this.optimizedIfTrueConstant;
							boolean isValueIfTrueOptimizedTrue = cst != null && cst != Constant.NotAConstant && cst.booleanValue() == true;
							if (isValueIfTrueOptimizedTrue) break JumpEndif; // no need to jump over, since branched to true already
						}
					} else {
						// implicit falling through the TRUE case
						if (trueLabel == null) {
							cst = this.optimizedIfTrueConstant;
							boolean isValueIfTrueOptimizedFalse = cst != null && cst != Constant.NotAConstant && cst.booleanValue() == false;
							if (isValueIfTrueOptimizedFalse) break JumpEndif; // no need to jump over, since branched to false already
						} else {
							// no implicit fall through TRUE/FALSE --> should never occur
						}
					}
					int position = codeStream.position;
					codeStream.goto_(endifLabel);
					codeStream.updateLastRecordedEndPC(currentScope, position);
				}
				// No need to decrement codestream stack size
				// since valueIfTrue was already consumed by branch bytecode
			}
		}
		if (needFalsePart) {
			internalFalseLabel.place();
			if (falseInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(currentScope, falseInitStateIndex);
				codeStream.addDefinitelyAssignedVariables(currentScope, falseInitStateIndex);
			}
			valueIfFalse.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel, valueRequired);

			// End of if statement
			endifLabel.place();
		}
		// May loose some local variable initializations : affecting the local variable attributes
		if (mergedInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
		}
		// no implicit conversion for boolean values
		codeStream.updateLastRecordedEndPC(currentScope, codeStream.position);
	}
	
public int nullStatus(FlowInfo flowInfo) {
	Constant cst = this.condition.optimizedBooleanConstant();
	if (cst != Constant.NotAConstant) {
		if (cst.booleanValue()) {
			return valueIfTrue.nullStatus(flowInfo);
		}
		return valueIfFalse.nullStatus(flowInfo);
	}
	int ifTrueNullStatus = valueIfTrue.nullStatus(flowInfo),
	    ifFalseNullStatus = valueIfFalse.nullStatus(flowInfo);
	if (ifTrueNullStatus == ifFalseNullStatus) {
		return ifTrueNullStatus;
	}
	return FlowInfo.UNKNOWN;
	// cannot decide which branch to take, and they disagree
}

	public Constant optimizedBooleanConstant() {

		return this.optimizedBooleanConstant == null ? this.constant : this.optimizedBooleanConstant;
	}
	
	public StringBuffer printExpressionNoParenthesis(int indent, StringBuffer output) {
		
		condition.printExpression(indent, output).append(" ? "); //$NON-NLS-1$
		valueIfTrue.printExpression(0, output).append(" : "); //$NON-NLS-1$
		return valueIfFalse.printExpression(0, output);
	}

	public TypeBinding resolveType(BlockScope scope) {
		// JLS3 15.25
		constant = Constant.NotAConstant;
		LookupEnvironment env = scope.environment();
		boolean use15specifics = scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5;
		TypeBinding conditionType = condition.resolveTypeExpecting(scope, TypeBinding.BOOLEAN);
		condition.computeConversion(scope, TypeBinding.BOOLEAN, conditionType);
		
		if (valueIfTrue instanceof CastExpression) valueIfTrue.bits |= DisableUnnecessaryCastCheck; // will check later on
		TypeBinding originalValueIfTrueType = valueIfTrue.resolveType(scope);

		if (valueIfFalse instanceof CastExpression) valueIfFalse.bits |= DisableUnnecessaryCastCheck; // will check later on
		TypeBinding originalValueIfFalseType = valueIfFalse.resolveType(scope);

		if (conditionType == null || originalValueIfTrueType == null || originalValueIfFalseType == null)
			return null;

		TypeBinding valueIfTrueType = originalValueIfTrueType;
		TypeBinding valueIfFalseType = originalValueIfFalseType;
		if (use15specifics && valueIfTrueType != valueIfFalseType) {
			if (valueIfTrueType.isBaseType()) {
				if (valueIfFalseType.isBaseType()) {
					// bool ? baseType : baseType
					if (valueIfTrueType == TypeBinding.NULL) {  // bool ? null : 12 --> Integer
						valueIfFalseType = env.computeBoxingType(valueIfFalseType); // boxing
					} else if (valueIfFalseType == TypeBinding.NULL) {  // bool ? 12 : null --> Integer
						valueIfTrueType = env.computeBoxingType(valueIfTrueType); // boxing
					}
				} else {
					// bool ? baseType : nonBaseType
					TypeBinding unboxedIfFalseType = valueIfFalseType.isBaseType() ? valueIfFalseType : env.computeBoxingType(valueIfFalseType);
					if (valueIfTrueType.isNumericType() && unboxedIfFalseType.isNumericType()) {
						valueIfFalseType = unboxedIfFalseType; // unboxing
					} else if (valueIfTrueType != TypeBinding.NULL) {  // bool ? 12 : new Integer(12) --> int
						valueIfFalseType = env.computeBoxingType(valueIfFalseType); // unboxing
					}
				}
			} else if (valueIfFalseType.isBaseType()) {
					// bool ? nonBaseType : baseType
					TypeBinding unboxedIfTrueType = valueIfTrueType.isBaseType() ? valueIfTrueType : env.computeBoxingType(valueIfTrueType);
					if (unboxedIfTrueType.isNumericType() && valueIfFalseType.isNumericType()) {
						valueIfTrueType = unboxedIfTrueType; // unboxing
					} else if (valueIfFalseType != TypeBinding.NULL) {  // bool ? new Integer(12) : 12 --> int
						valueIfTrueType = env.computeBoxingType(valueIfTrueType); // unboxing
					}					
			} else {
					// bool ? nonBaseType : nonBaseType
					TypeBinding unboxedIfTrueType = env.computeBoxingType(valueIfTrueType);
					TypeBinding unboxedIfFalseType = env.computeBoxingType(valueIfFalseType);
					if (unboxedIfTrueType.isNumericType() && unboxedIfFalseType.isNumericType()) {
						valueIfTrueType = unboxedIfTrueType;
						valueIfFalseType = unboxedIfFalseType;
					}
			} 
		}
		// Propagate the constant value from the valueIfTrue and valueIFFalse expression if it is possible
		Constant condConstant, trueConstant, falseConstant;
		if ((condConstant = condition.constant) != Constant.NotAConstant
			&& (trueConstant = valueIfTrue.constant) != Constant.NotAConstant
			&& (falseConstant = valueIfFalse.constant) != Constant.NotAConstant) {
			// all terms are constant expression so we can propagate the constant
			// from valueIFTrue or valueIfFalse to the receiver constant
			constant = condConstant.booleanValue() ? trueConstant : falseConstant;
		}
		if (valueIfTrueType == valueIfFalseType) { // harmed the implicit conversion 
			valueIfTrue.computeConversion(scope, valueIfTrueType, originalValueIfTrueType);
			valueIfFalse.computeConversion(scope, valueIfFalseType, originalValueIfFalseType);
			if (valueIfTrueType == TypeBinding.BOOLEAN) {
				this.optimizedIfTrueConstant = valueIfTrue.optimizedBooleanConstant();
				this.optimizedIfFalseConstant = valueIfFalse.optimizedBooleanConstant();
				if (this.optimizedIfTrueConstant != Constant.NotAConstant 
						&& this.optimizedIfFalseConstant != Constant.NotAConstant
						&& this.optimizedIfTrueConstant.booleanValue() == this.optimizedIfFalseConstant.booleanValue()) {
					// a ? true : true  /   a ? false : false
					this.optimizedBooleanConstant = optimizedIfTrueConstant;
				} else if ((condConstant = condition.optimizedBooleanConstant()) != Constant.NotAConstant) { // Propagate the optimized boolean constant if possible
					this.optimizedBooleanConstant = condConstant.booleanValue()
						? this.optimizedIfTrueConstant
						: this.optimizedIfFalseConstant;
				}
			}
			return this.resolvedType = valueIfTrueType;
		}
		// Determine the return type depending on argument types
		// Numeric types
		if (valueIfTrueType.isNumericType() && valueIfFalseType.isNumericType()) {
			// (Short x Byte) or (Byte x Short)"
			if ((valueIfTrueType == TypeBinding.BYTE && valueIfFalseType == TypeBinding.SHORT)
				|| (valueIfTrueType == TypeBinding.SHORT && valueIfFalseType == TypeBinding.BYTE)) {
				valueIfTrue.computeConversion(scope, TypeBinding.SHORT, originalValueIfTrueType);
				valueIfFalse.computeConversion(scope, TypeBinding.SHORT, originalValueIfFalseType);
				return this.resolvedType = TypeBinding.SHORT;
			}
			// <Byte|Short|Char> x constant(Int)  ---> <Byte|Short|Char>   and reciprocally
			if ((valueIfTrueType == TypeBinding.BYTE || valueIfTrueType == TypeBinding.SHORT || valueIfTrueType == TypeBinding.CHAR)
					&& (valueIfFalseType == TypeBinding.INT
						&& valueIfFalse.isConstantValueOfTypeAssignableToType(valueIfFalseType, valueIfTrueType))) {
				valueIfTrue.computeConversion(scope, valueIfTrueType, originalValueIfTrueType);
				valueIfFalse.computeConversion(scope, valueIfTrueType, originalValueIfFalseType);
				return this.resolvedType = valueIfTrueType;
			}
			if ((valueIfFalseType == TypeBinding.BYTE
					|| valueIfFalseType == TypeBinding.SHORT
					|| valueIfFalseType == TypeBinding.CHAR)
					&& (valueIfTrueType == TypeBinding.INT
						&& valueIfTrue.isConstantValueOfTypeAssignableToType(valueIfTrueType, valueIfFalseType))) {
				valueIfTrue.computeConversion(scope, valueIfFalseType, originalValueIfTrueType);
				valueIfFalse.computeConversion(scope, valueIfFalseType, originalValueIfFalseType);
				return this.resolvedType = valueIfFalseType;
			}
			// Manual binary numeric promotion
			// int
			if (BaseTypeBinding.isNarrowing(valueIfTrueType.id, T_int)
					&& BaseTypeBinding.isNarrowing(valueIfFalseType.id, T_int)) {
				valueIfTrue.computeConversion(scope, TypeBinding.INT, originalValueIfTrueType);
				valueIfFalse.computeConversion(scope, TypeBinding.INT, originalValueIfFalseType);
				return this.resolvedType = TypeBinding.INT;
			}
			// long
			if (BaseTypeBinding.isNarrowing(valueIfTrueType.id, T_long)
					&& BaseTypeBinding.isNarrowing(valueIfFalseType.id, T_long)) {
				valueIfTrue.computeConversion(scope, TypeBinding.LONG, originalValueIfTrueType);
				valueIfFalse.computeConversion(scope, TypeBinding.LONG, originalValueIfFalseType);
				return this.resolvedType = TypeBinding.LONG;
			}
			// float
			if (BaseTypeBinding.isNarrowing(valueIfTrueType.id, T_float)
					&& BaseTypeBinding.isNarrowing(valueIfFalseType.id, T_float)) {
				valueIfTrue.computeConversion(scope, TypeBinding.FLOAT, originalValueIfTrueType);
				valueIfFalse.computeConversion(scope, TypeBinding.FLOAT, originalValueIfFalseType);
				return this.resolvedType = TypeBinding.FLOAT;
			}
			// double
			valueIfTrue.computeConversion(scope, TypeBinding.DOUBLE, originalValueIfTrueType);
			valueIfFalse.computeConversion(scope, TypeBinding.DOUBLE, originalValueIfFalseType);
			return this.resolvedType = TypeBinding.DOUBLE;
		}
		// Type references (null null is already tested)
		if (valueIfTrueType.isBaseType() && valueIfTrueType != TypeBinding.NULL) {
			if (use15specifics) {
				valueIfTrueType = env.computeBoxingType(valueIfTrueType);
			} else {
				scope.problemReporter().conditionalArgumentsIncompatibleTypes(this, valueIfTrueType, valueIfFalseType);
				return null;
			}
		} 
		if (valueIfFalseType.isBaseType() && valueIfFalseType != TypeBinding.NULL) {
			if (use15specifics) {
				valueIfFalseType = env.computeBoxingType(valueIfFalseType);
			} else {
				scope.problemReporter().conditionalArgumentsIncompatibleTypes(this, valueIfTrueType, valueIfFalseType);
				return null;
			}
		}
		if (use15specifics) {
			// >= 1.5 : LUB(operand types) must exist
			TypeBinding commonType = null;
			if (valueIfTrueType == TypeBinding.NULL) {
				commonType = valueIfFalseType;
			} else if (valueIfFalseType == TypeBinding.NULL) {
				commonType = valueIfTrueType;
			} else {
				commonType = scope.lowerUpperBound(new TypeBinding[] { valueIfTrueType, valueIfFalseType });
			}
			if (commonType != null) {
				valueIfTrue.computeConversion(scope, commonType, originalValueIfTrueType);
				valueIfFalse.computeConversion(scope, commonType, originalValueIfFalseType);
				return this.resolvedType = commonType.capture(scope, this.sourceEnd);
			}
		} else {
			// < 1.5 : one operand must be convertible to the other
			if (valueIfFalseType.isCompatibleWith(valueIfTrueType)) {
				valueIfTrue.computeConversion(scope, valueIfTrueType, originalValueIfTrueType);
				valueIfFalse.computeConversion(scope, valueIfTrueType, originalValueIfFalseType);
				return this.resolvedType = valueIfTrueType;
			} else if (valueIfTrueType.isCompatibleWith(valueIfFalseType)) {
				valueIfTrue.computeConversion(scope, valueIfFalseType, originalValueIfTrueType);
				valueIfFalse.computeConversion(scope, valueIfFalseType, originalValueIfFalseType);
				return this.resolvedType = valueIfFalseType;
			}
		}
		scope.problemReporter().conditionalArgumentsIncompatibleTypes(
			this,
			valueIfTrueType,
			valueIfFalseType);
		return null;
	}
	
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			condition.traverse(visitor, scope);
			valueIfTrue.traverse(visitor, scope);
			valueIfFalse.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}
