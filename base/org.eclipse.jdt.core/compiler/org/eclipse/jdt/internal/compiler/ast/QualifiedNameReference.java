/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.MissingTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemFieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

public class QualifiedNameReference extends NameReference {
	
	public char[][] tokens;
	public long[] sourcePositions;	
	public FieldBinding[] otherBindings, otherCodegenBindings;
	int[] otherDepths;
	public int indexOfFirstFieldBinding;//points (into tokens) for the first token that corresponds to first FieldBinding
	public SyntheticMethodBinding syntheticWriteAccessor;
	public SyntheticMethodBinding[] syntheticReadAccessors;
	public TypeBinding genericCast;
	public TypeBinding[] otherGenericCasts;
	
public QualifiedNameReference(	char[][] tokens, long[] positions, int sourceStart, int sourceEnd) {
	this.tokens = tokens;
	this.sourcePositions = positions;
	this.sourceStart = sourceStart;
	this.sourceEnd = sourceEnd;
}

public FlowInfo analyseAssignment(BlockScope currentScope, 	FlowContext flowContext, FlowInfo flowInfo, Assignment assignment, boolean isCompound) {
	// determine the rank until which we now we do not need any actual value for the field access
	int otherBindingsCount = this.otherBindings == null ? 0 : this.otherBindings.length;
	boolean needValue = otherBindingsCount == 0 || !this.otherBindings[0].isStatic();
	boolean complyTo14 = currentScope.compilerOptions().complianceLevel >= ClassFileConstants.JDK1_4;
	FieldBinding lastFieldBinding = null;
	switch (this.bits & ASTNode.RestrictiveFlagMASK) {
		case Binding.FIELD : // reading a field
			lastFieldBinding = (FieldBinding) this.binding;
			if (needValue || complyTo14) {
				manageSyntheticAccessIfNecessary(currentScope, lastFieldBinding, this.actualReceiverType, 0, flowInfo);
			}
			// check if final blank field
			if (lastFieldBinding.isBlankFinal()
				    && this.otherBindings != null // the last field binding is only assigned
	 				&& currentScope.needBlankFinalFieldInitializationCheck(lastFieldBinding)) {
				if (!flowInfo.isDefinitelyAssigned(lastFieldBinding)) {
					currentScope.problemReporter().uninitializedBlankFinalField(
						lastFieldBinding,
						this);
				}
			}
			break;
		case Binding.LOCAL :
			// first binding is a local variable
			LocalVariableBinding localBinding;
			if (!flowInfo
				.isDefinitelyAssigned(localBinding = (LocalVariableBinding) this.binding)) {
				currentScope.problemReporter().uninitializedLocalVariable(localBinding, this);
			}
			if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) == 0)	{
				localBinding.useFlag = LocalVariableBinding.USED;
			} else if (localBinding.useFlag == LocalVariableBinding.UNUSED) {
				localBinding.useFlag = LocalVariableBinding.FAKE_USED;
			}
			checkNPE(currentScope, flowContext, flowInfo, true);
	}
	
	if (needValue) {
		manageEnclosingInstanceAccessIfNecessary(currentScope, flowInfo);
		// only for first binding
	}
	// all intermediate field accesses are read accesses
	if (this.otherBindings != null) {
		for (int i = 0; i < otherBindingsCount-1; i++) {
			lastFieldBinding = this.otherBindings[i];
			needValue = !this.otherBindings[i+1].isStatic();
			if (needValue || complyTo14) {
				manageSyntheticAccessIfNecessary(
					currentScope, 
					lastFieldBinding, 
					i == 0 
						? ((VariableBinding)this.binding).type
						: this.otherBindings[i-1].type,
					i + 1, 
					flowInfo);
			}
		}
		lastFieldBinding = this.otherBindings[otherBindingsCount-1];
	}

	if (isCompound) {
		if (otherBindingsCount == 0
				&& lastFieldBinding.isBlankFinal()
				&& currentScope.needBlankFinalFieldInitializationCheck(lastFieldBinding)
				&& (!flowInfo.isDefinitelyAssigned(lastFieldBinding))) {
			currentScope.problemReporter().uninitializedBlankFinalField(lastFieldBinding, this);
		}
		TypeBinding lastReceiverType;
		switch (otherBindingsCount) {
			case 0 :
				lastReceiverType = this.actualReceiverType;
				break;
			case 1 :
				lastReceiverType = ((VariableBinding)this.binding).type;
				break;
			default:
				lastReceiverType = this.otherBindings[otherBindingsCount-2].type;
				break;
		}
		manageSyntheticAccessIfNecessary(
			currentScope,
			lastFieldBinding,
			lastReceiverType,
			otherBindingsCount, 
			flowInfo);
	}
	
	if (assignment.expression != null) {
		flowInfo =
			assignment
				.expression
				.analyseCode(currentScope, flowContext, flowInfo)
				.unconditionalInits();
	}
	
	// the last field access is a write access
	if (lastFieldBinding.isFinal()) {
		// in a context where it can be assigned?
		if (otherBindingsCount == 0
				&& this.indexOfFirstFieldBinding == 1
				&& lastFieldBinding.isBlankFinal()
				&& !isCompound
				&& currentScope.allowBlankFinalFieldAssignment(lastFieldBinding)) {
			if (flowInfo.isPotentiallyAssigned(lastFieldBinding)) {
				currentScope.problemReporter().duplicateInitializationOfBlankFinalField(lastFieldBinding, this);
			} else {
				flowContext.recordSettingFinal(lastFieldBinding, this, flowInfo);
			}
			flowInfo.markAsDefinitelyAssigned(lastFieldBinding);
		} else {
			currentScope.problemReporter().cannotAssignToFinalField(lastFieldBinding, this);
			if (otherBindingsCount == 0 && currentScope.allowBlankFinalFieldAssignment(lastFieldBinding)) { // pretend it got assigned
				flowInfo.markAsDefinitelyAssigned(lastFieldBinding);
			}
		}
	}
	// equivalent to valuesRequired[maxOtherBindings]
	TypeBinding lastReceiverType;
	switch (otherBindingsCount) {
		case 0 :
			lastReceiverType = this.actualReceiverType;
			break;
		case 1 :
			lastReceiverType = ((VariableBinding)this.binding).type;
			break;
		default :
			lastReceiverType = this.otherBindings[otherBindingsCount-2].type;
			break;
	}
	manageSyntheticAccessIfNecessary(currentScope, lastFieldBinding, lastReceiverType, -1 /*write-access*/, flowInfo);

	return flowInfo;
}

public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	return analyseCode(currentScope, flowContext, flowInfo, true);
}

public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, boolean valueRequired) {
	// determine the rank until which we now we do not need any actual value for the field access
	int otherBindingsCount = this.otherBindings == null ? 0 : this.otherBindings.length;

	boolean needValue = otherBindingsCount == 0 ? valueRequired : !this.otherBindings[0].isStatic();
	boolean complyTo14 = currentScope.compilerOptions().complianceLevel >= ClassFileConstants.JDK1_4;
	switch (this.bits & ASTNode.RestrictiveFlagMASK) {
		case Binding.FIELD : // reading a field
			if (needValue || complyTo14) {
				manageSyntheticAccessIfNecessary(currentScope, (FieldBinding) this.binding, this.actualReceiverType, 0, flowInfo);
			}
			if (this.indexOfFirstFieldBinding == 1) { // was an implicit reference to the first field binding
				FieldBinding fieldBinding = (FieldBinding) this.binding;
				// check if reading a final blank field
				if (fieldBinding.isBlankFinal()
						&& currentScope.needBlankFinalFieldInitializationCheck(fieldBinding)
						&& !flowInfo.isDefinitelyAssigned(fieldBinding)) {
					currentScope.problemReporter().uninitializedBlankFinalField(fieldBinding, this);
				}
			}
			break;
		case Binding.LOCAL : // reading a local variable
			LocalVariableBinding localBinding;
			if (!flowInfo
				.isDefinitelyAssigned(localBinding = (LocalVariableBinding) this.binding)) {
				currentScope.problemReporter().uninitializedLocalVariable(localBinding, this);
			}
			if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) == 0)	{
				localBinding.useFlag = LocalVariableBinding.USED;
			} else if (localBinding.useFlag == LocalVariableBinding.UNUSED) {
				localBinding.useFlag = LocalVariableBinding.FAKE_USED;
			}
			checkNPE(currentScope, flowContext, flowInfo, true);
	}
	if (needValue) {
		manageEnclosingInstanceAccessIfNecessary(currentScope, flowInfo);
		// only for first binding (if value needed only)
	}
	if (this.otherBindings != null) {
		for (int i = 0; i < otherBindingsCount; i++) {
			needValue = i < otherBindingsCount-1 ? !this.otherBindings[i+1].isStatic() : valueRequired;
			if (needValue || complyTo14) {
				TypeBinding lastReceiverType = getGenericCast(i);
				if (lastReceiverType == null) {
					if (i == 0) {
						 lastReceiverType = ((VariableBinding)this.binding).type;
					} else {
						lastReceiverType = this.otherBindings[i-1].type;
					}
				}
				manageSyntheticAccessIfNecessary(
					currentScope, 
					this.otherBindings[i], 
					lastReceiverType,
					i + 1,
					flowInfo);
			}
		}
	}
	return flowInfo;
}

/**
 * Check and/or redirect the field access to the delegate receiver if any
 */
public TypeBinding checkFieldAccess(BlockScope scope) {
	FieldBinding fieldBinding = (FieldBinding) this.binding;
	MethodScope methodScope = scope.methodScope();
	TypeBinding declaringClass = fieldBinding.original().declaringClass;
	// check for forward references
	if ((this.indexOfFirstFieldBinding == 1 || declaringClass.isEnum())
			&& methodScope.enclosingSourceType() == declaringClass
			&& methodScope.lastVisibleFieldID >= 0
			&& fieldBinding.id >= methodScope.lastVisibleFieldID
			&& (!fieldBinding.isStatic() || methodScope.isStatic)) {
		scope.problemReporter().forwardReference(this, 0, fieldBinding);
	}
	this.bits &= ~ASTNode.RestrictiveFlagMASK; // clear bits
	this.bits |= Binding.FIELD;
	return getOtherFieldBindings(scope);
}

public void checkNPE(BlockScope scope, FlowContext flowContext, FlowInfo flowInfo, boolean checkString) {
	// cannot override localVariableBinding because this would project o.m onto o when
	// analysing assignments
	if ((this.bits & ASTNode.RestrictiveFlagMASK) == Binding.LOCAL) {
		LocalVariableBinding local = (LocalVariableBinding) this.binding;
		if (local != null && 
			(local.type.tagBits & TagBits.IsBaseType) == 0 &&
			(checkString || local.type.id != TypeIds.T_JavaLangString)) {
			if ((this.bits & ASTNode.IsNonNull) == 0) {
				flowContext.recordUsingNullReference(scope, local, this, 
					FlowContext.MAY_NULL, flowInfo);
			}
			flowInfo.markAsComparedEqualToNonNull(local); 
				// from thereon it is set
			if (flowContext.initsOnFinally != null) {
				flowContext.initsOnFinally.markAsComparedEqualToNonNull(local); 
			}			
		}
	}
}

/**
 * @see org.eclipse.jdt.internal.compiler.ast.Expression#computeConversion(org.eclipse.jdt.internal.compiler.lookup.Scope, org.eclipse.jdt.internal.compiler.lookup.TypeBinding, org.eclipse.jdt.internal.compiler.lookup.TypeBinding)
 */
public void computeConversion(Scope scope, TypeBinding runtimeTimeType, TypeBinding compileTimeType) {
	if (runtimeTimeType == null || compileTimeType == null)
		return;		
	// set the generic cast after the fact, once the type expectation is fully known (no need for strict cast)
	FieldBinding field = null;
	int length = this.otherBindings == null ? 0 : this.otherBindings.length;
	if (length == 0) {
		if ((this.bits & Binding.FIELD) != 0 && this.binding != null && this.binding.isValidBinding()) {
			field = (FieldBinding) this.binding;
		}
	} else {
		field  = this.otherBindings[length-1];
	}
	if (field != null) {
		FieldBinding originalBinding = field.original();
		TypeBinding originalType = originalBinding.type;
	    // extra cast needed if field type is type variable
		if (originalType.leafComponentType().isTypeVariable()) {
	    	TypeBinding targetType = (!compileTimeType.isBaseType() && runtimeTimeType.isBaseType()) 
	    		? compileTimeType  // unboxing: checkcast before conversion
	    		: runtimeTimeType;
	    	TypeBinding typeCast = originalType.genericCast(targetType);
	    	setGenericCast(length, typeCast);
	        if (typeCast instanceof ReferenceBinding) {
				ReferenceBinding referenceCast = (ReferenceBinding) typeCast;
				if (!referenceCast.canBeSeenBy(scope)) {
		        	scope.problemReporter().invalidType(this, 
		        			new ProblemReferenceBinding(
								CharOperation.splitOn('.', referenceCast.shortReadableName()),
								referenceCast,
								ProblemReasons.NotVisible));
				}
	        }			    	
		} 	
	}
	super.computeConversion(scope, runtimeTimeType, compileTimeType);
}

public void generateAssignment(BlockScope currentScope, CodeStream codeStream, Assignment assignment, boolean valueRequired) {
	int pc = codeStream.position;
	FieldBinding lastFieldBinding = generateReadSequence(currentScope, codeStream);
	codeStream.recordPositionsFrom(pc , this.sourceStart);
	assignment.expression.generateCode(currentScope, codeStream, true);
	fieldStore(codeStream, lastFieldBinding, this.syntheticWriteAccessor, valueRequired);
	// equivalent to valuesRequired[maxOtherBindings]
	if (valueRequired) {
		codeStream.generateImplicitConversion(assignment.implicitConversion);
	}
}

public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {		
	int pc = codeStream.position;
	if (this.constant != Constant.NotAConstant) {
		if (valueRequired) {
			codeStream.generateConstant(this.constant, this.implicitConversion);
		}
	} else {
		FieldBinding lastFieldBinding = generateReadSequence(currentScope, codeStream);
		if (lastFieldBinding != null) {
			boolean isStatic = lastFieldBinding.isStatic();
			Constant fieldConstant = lastFieldBinding.constant();
			if (fieldConstant != Constant.NotAConstant) {
				if (!isStatic){
					codeStream.invokeObjectGetClass();
					codeStream.pop();
				}
				if (valueRequired) { // inline the last field constant
					codeStream.generateConstant(fieldConstant, this.implicitConversion);
				}
			} else {
				boolean isFirst = lastFieldBinding == this.binding 
												&& (this.indexOfFirstFieldBinding == 1 || lastFieldBinding.declaringClass == currentScope.enclosingReceiverType())
												&& this.otherBindings == null; // could be dup: next.next.next
				TypeBinding requiredGenericCast = getGenericCast(this.otherCodegenBindings == null ? 0 : this.otherCodegenBindings.length);
				if (valueRequired  
						|| (!isFirst && currentScope.compilerOptions().complianceLevel >= ClassFileConstants.JDK1_4)
						|| ((this.implicitConversion & TypeIds.UNBOXING) != 0)
						|| requiredGenericCast != null) {
					int lastFieldPc = codeStream.position;
					if (lastFieldBinding.declaringClass == null) { // array length
						codeStream.arraylength();
						if (valueRequired) {
							codeStream.generateImplicitConversion(this.implicitConversion);
						} else {
							// could occur if !valueRequired but compliance >= 1.4
							codeStream.pop();
						}
					} else {
						SyntheticMethodBinding accessor =
							this.syntheticReadAccessors == null
								? null
								: this.syntheticReadAccessors[this.syntheticReadAccessors.length - 1];
						if (accessor == null) {
							if (isStatic) {
								codeStream.getstatic(lastFieldBinding);
							} else {
								codeStream.getfield(lastFieldBinding);
							}
						} else {
							codeStream.invokestatic(accessor);
						}
						if (requiredGenericCast != null) codeStream.checkcast(requiredGenericCast);
						if (valueRequired) {
							codeStream.generateImplicitConversion(this.implicitConversion);
						} else {
							boolean isUnboxing = (this.implicitConversion & TypeIds.UNBOXING) != 0;
							// conversion only generated if unboxing
							if (isUnboxing) codeStream.generateImplicitConversion(this.implicitConversion);
							switch (isUnboxing ? postConversionType(currentScope).id : lastFieldBinding.type.id) {
								case T_long :
								case T_double :
									codeStream.pop2();
									break;
								default :
									codeStream.pop();
							}
						}
					}
					
					int fieldPosition = (int) (this.sourcePositions[this.sourcePositions.length - 1] >>> 32);
					codeStream.recordPositionsFrom(lastFieldPc, fieldPosition);
				} else {
					if (!isStatic){
						codeStream.invokeObjectGetClass(); // perform null check
						codeStream.pop();
					}
				}									
			}
		}
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}

public void generateCompoundAssignment(
	BlockScope currentScope,
	CodeStream codeStream,
	Expression expression,
	int operator,
	int assignmentImplicitConversion,
	boolean valueRequired) {
		
	FieldBinding lastFieldBinding = generateReadSequence(currentScope, codeStream);
	SyntheticMethodBinding accessor =
		this.syntheticReadAccessors == null
			? null
			: this.syntheticReadAccessors[this.syntheticReadAccessors.length - 1];
	if (lastFieldBinding.isStatic()) {
		if (accessor == null) {
			codeStream.getstatic(lastFieldBinding);
		} else {
			codeStream.invokestatic(accessor);
		}
	} else {
		codeStream.dup();
		if (accessor == null) {
			codeStream.getfield(lastFieldBinding);
		} else {
			codeStream.invokestatic(accessor);
		}
	}
	// the last field access is a write access
	// perform the actual compound operation
	int operationTypeID;
	switch(operationTypeID = (this.implicitConversion & TypeIds.IMPLICIT_CONVERSION_MASK) >> 4) {
		case T_JavaLangString :
		case T_JavaLangObject :
		case T_undefined :
			codeStream.generateStringConcatenationAppend(currentScope, null, expression);
			break;
		default :
			TypeBinding requiredGenericCast = getGenericCast(this.otherCodegenBindings == null ? 0 : this.otherCodegenBindings.length);
			if (requiredGenericCast != null) codeStream.checkcast(requiredGenericCast);				
			// promote the array reference to the suitable operation type
			codeStream.generateImplicitConversion(this.implicitConversion);
			// generate the increment value (will by itself  be promoted to the operation value)
			if (expression == IntLiteral.One) { // prefix operation
				codeStream.generateConstant(expression.constant, this.implicitConversion);
			} else {
				expression.generateCode(currentScope, codeStream, true);
			}
			// perform the operation
			codeStream.sendOperator(operator, operationTypeID);
			// cast the value back to the array reference type
			codeStream.generateImplicitConversion(assignmentImplicitConversion);
	}
	// actual assignment
	fieldStore(codeStream, lastFieldBinding, this.syntheticWriteAccessor, valueRequired);
	// equivalent to valuesRequired[maxOtherBindings]
}

public void generatePostIncrement(
	BlockScope currentScope,
	CodeStream codeStream,
	CompoundAssignment postIncrement,
	boolean valueRequired) {
    
	FieldBinding lastFieldBinding = generateReadSequence(currentScope, codeStream);
	SyntheticMethodBinding accessor =
		this.syntheticReadAccessors == null
			? null
			: this.syntheticReadAccessors[this.syntheticReadAccessors.length - 1];
	if (lastFieldBinding.isStatic()) {
		if (accessor == null) {
			codeStream.getstatic(lastFieldBinding);
		} else {
			codeStream.invokestatic(accessor);
		}
	} else {
		codeStream.dup();
		if (accessor == null) {
			codeStream.getfield(lastFieldBinding);
		} else {
			codeStream.invokestatic(accessor);
		}
	}
	TypeBinding requiredGenericCast = getGenericCast(this.otherCodegenBindings == null ? 0 : this.otherCodegenBindings.length);
	TypeBinding operandType;
	if (requiredGenericCast != null) {
		codeStream.checkcast(requiredGenericCast);
		operandType = requiredGenericCast;
	} else {
		operandType = lastFieldBinding.type;
	}		
	// duplicate the old field value
	if (valueRequired) {
		if (lastFieldBinding.isStatic()) {
			if ((operandType == TypeBinding.LONG)
				|| (operandType == TypeBinding.DOUBLE)) {
				codeStream.dup2();
			} else {
				codeStream.dup();
			}
		} else { // Stack:  [owner][old field value]  ---> [old field value][owner][old field value]
			if ((operandType == TypeBinding.LONG)
				|| (operandType == TypeBinding.DOUBLE)) {
				codeStream.dup2_x1();
			} else {
				codeStream.dup_x1();
			}
		}
	}
	codeStream.generateImplicitConversion(this.implicitConversion);		
	codeStream.generateConstant(
		postIncrement.expression.constant,
		this.implicitConversion);
	codeStream.sendOperator(postIncrement.operator, this.implicitConversion & TypeIds.COMPILE_TYPE_MASK);
	codeStream.generateImplicitConversion(
		postIncrement.preAssignImplicitConversion);
	fieldStore(codeStream, lastFieldBinding, this.syntheticWriteAccessor, false);
}	

/*
 * Generate code for all bindings (local and fields) excluding the last one, which may then be generated code
 * for a read or write access.
 */
public FieldBinding generateReadSequence(BlockScope currentScope, CodeStream codeStream) {
	// determine the rank until which we now we do not need any actual value for the field access
	int otherBindingsCount = this.otherCodegenBindings == null ? 0 : this.otherCodegenBindings.length;
	boolean needValue = otherBindingsCount == 0 || !this.otherBindings[0].isStatic();
	FieldBinding lastFieldBinding = null;
	TypeBinding lastGenericCast = null;
	boolean complyTo14 = currentScope.compilerOptions().complianceLevel >= ClassFileConstants.JDK1_4;

	switch (this.bits & ASTNode.RestrictiveFlagMASK) {
		case Binding.FIELD :
			lastFieldBinding = (FieldBinding) this.codegenBinding;
			lastGenericCast = this.genericCast;
			// if first field is actually constant, we can inline it
			if (lastFieldBinding.constant() != Constant.NotAConstant) {
				break;
			}
			if ((needValue && !lastFieldBinding.isStatic()) || lastGenericCast != null) {
				int pc = codeStream.position;
				if ((this.bits & ASTNode.DepthMASK) != 0) {
					ReferenceBinding targetType = currentScope.enclosingSourceType().enclosingTypeAt((this.bits & ASTNode.DepthMASK) >> ASTNode.DepthSHIFT);
					Object[] emulationPath = currentScope.getEmulationPath(targetType, true /*only exact match*/, false/*consider enclosing arg*/);
					codeStream.generateOuterAccess(emulationPath, this, targetType, currentScope);
				} else {
					generateReceiver(codeStream);
				}
				codeStream.recordPositionsFrom(pc, this.sourceStart);
			}
			break;
		case Binding.LOCAL : // reading the first local variable
			if (!needValue) break; // no value needed
			LocalVariableBinding localBinding = (LocalVariableBinding) this.codegenBinding;
			// regular local variable read
			Constant localConstant = localBinding.constant();
			if (localConstant != Constant.NotAConstant) {
				codeStream.generateConstant(localConstant, 0);
				// no implicit conversion
			} else {
				// outer local?
				if ((this.bits & ASTNode.DepthMASK) != 0) {
					// outer local can be reached either through a synthetic arg or a synthetic field
					VariableBinding[] path = currentScope.getEmulationPath(localBinding);
					codeStream.generateOuterAccess(path, this, localBinding, currentScope);
				} else {
					codeStream.load(localBinding);
				}
			}
	}
					
	// all intermediate field accesses are read accesses
	// only the last field binding is a write access
	int positionsLength = this.sourcePositions.length;
	if (this.otherCodegenBindings != null) {
		for (int i = 0; i < otherBindingsCount; i++) {
			int pc = codeStream.position;
			FieldBinding nextField = this.otherCodegenBindings[i];
			TypeBinding nextGenericCast = this.otherGenericCasts == null ? null : this.otherGenericCasts[i];
			if (lastFieldBinding != null) {
				needValue = !nextField.isStatic();
				Constant fieldConstant = lastFieldBinding.constant();
				if (fieldConstant != Constant.NotAConstant) {
					if (i > 0 && !lastFieldBinding.isStatic()) {
						codeStream.invokeObjectGetClass(); // perform null check
						codeStream.pop();
					}
					if (needValue) {
						codeStream.generateConstant(fieldConstant, 0);
					}
				} else {
					if (needValue || (i > 0 && complyTo14) || lastGenericCast != null) {
						MethodBinding accessor = this.syntheticReadAccessors == null ? null : this.syntheticReadAccessors[i]; 
						if (accessor == null) {
							if (lastFieldBinding.isStatic()) {
								codeStream.getstatic(lastFieldBinding);
							} else {
								codeStream.getfield(lastFieldBinding);
							}
						} else {
							codeStream.invokestatic(accessor);
						}
						if (lastGenericCast != null) codeStream.checkcast(lastGenericCast);
						if (!needValue) codeStream.pop();
					} else {
						if (this.codegenBinding == lastFieldBinding) {
							if (lastFieldBinding.isStatic()){
								// if no valueRequired, still need possible side-effects of <clinit> invocation, if field belongs to different class
								if (((FieldBinding)this.binding).original().declaringClass != this.actualReceiverType.erasure()) {
									MethodBinding accessor = this.syntheticReadAccessors == null ? null : this.syntheticReadAccessors[i]; 
									if (accessor == null) {
										codeStream.getstatic(lastFieldBinding);
									} else {
										codeStream.invokestatic(accessor);
									}
									codeStream.pop();
								}				
							}
						} else if (!lastFieldBinding.isStatic()){
							codeStream.invokeObjectGetClass(); // perform null check
							codeStream.pop();
						}						
					}
					if ((positionsLength - otherBindingsCount + i - 1) >= 0) {
						int fieldPosition = (int) (this.sourcePositions[positionsLength - otherBindingsCount + i - 1] >>>32);
						codeStream.recordPositionsFrom(pc, fieldPosition);
					}
				}
			}
			lastFieldBinding = nextField;
			lastGenericCast = nextGenericCast;
		}
	}
	return lastFieldBinding;
}

public void generateReceiver(CodeStream codeStream) {
	codeStream.aload_0();
}

/**
 * @see org.eclipse.jdt.internal.compiler.lookup.InvocationSite#genericTypeArguments()
 */
public TypeBinding[] genericTypeArguments() {
	return null;
}

// get the matching codegenBinding
protected FieldBinding getCodegenBinding(int index) {
  if (index == 0){
		return (FieldBinding)this.codegenBinding;
	} else {
		return this.otherCodegenBindings[index-1];
	}
}

// get the matching generic cast
protected TypeBinding getGenericCast(int index) {
   if (index == 0){
		return this.genericCast;
	} else {
	    if (this.otherGenericCasts == null) return null;
		return this.otherGenericCasts[index-1];
	}
}

public TypeBinding getOtherFieldBindings(BlockScope scope) {
	// At this point restrictiveFlag may ONLY have two potential value : FIELD LOCAL (i.e cast <<(VariableBinding) binding>> is valid)
	int length = this.tokens.length;
	FieldBinding field;
	if ((this.bits & Binding.FIELD) != 0) {
		field = (FieldBinding) this.binding;
		if (!field.isStatic()) {
			//must check for the static status....
			if (this.indexOfFirstFieldBinding > 1  //accessing to a field using a type as "receiver" is allowed only with static field
					 || scope.methodScope().isStatic) { 	// the field is the first token of the qualified reference....
				scope.problemReporter().staticFieldAccessToNonStaticVariable(this, field);
				return null;
			 }
		} else if (this.indexOfFirstFieldBinding > 1 
					&& field.declaringClass != this.actualReceiverType
					&& field.declaringClass.canBeSeenBy(scope)) {
			scope.problemReporter().indirectAccessToStaticField(this, field);
		}
		// only last field is actually a write access if any
		if (isFieldUseDeprecated(field, scope, (this.bits & ASTNode.IsStrictlyAssigned) != 0 && this.indexOfFirstFieldBinding == length))
			scope.problemReporter().deprecatedField(field, this);
	} else {
		field = null;
	}
	TypeBinding type = ((VariableBinding) this.binding).type;
	int index = this.indexOfFirstFieldBinding;
	if (index == length) { //	restrictiveFlag == FIELD
		this.constant = ((FieldBinding) this.binding).constant();
		// perform capture conversion if read access
		return (type != null && (this.bits & ASTNode.IsStrictlyAssigned) == 0)
				? type.capture(scope, this.sourceEnd)
				: type;
	}
	// allocation of the fieldBindings array	and its respective constants
	int otherBindingsLength = length - index;
	this.otherCodegenBindings = this.otherBindings = new FieldBinding[otherBindingsLength];
	this.otherDepths = new int[otherBindingsLength];
	
	// fill the first constant (the one of the binding)
	this.constant = ((VariableBinding) this.binding).constant();
	// save first depth, since will be updated by visibility checks of other bindings
	int firstDepth = (this.bits & ASTNode.DepthMASK) >> ASTNode.DepthSHIFT;
	// iteration on each field	
	while (index < length) {
		char[] token = this.tokens[index];
		if (type == null)
			return null; // could not resolve type prior to this point

		this.bits &= ~ASTNode.DepthMASK; // flush previous depth if any		
		FieldBinding previousField = field;
		field = scope.getField(type.capture(scope, (int)this.sourcePositions[index]), token, this);
		int place = index - this.indexOfFirstFieldBinding;
		this.otherBindings[place] = field;
		this.otherDepths[place] = (this.bits & ASTNode.DepthMASK) >> ASTNode.DepthSHIFT;
		if (field.isValidBinding()) {
			// set generic cast of for previous field (if any)
			if (previousField != null) {
				TypeBinding fieldReceiverType = type;
				TypeBinding receiverErasure = type.erasure();
				if (receiverErasure instanceof ReferenceBinding) {
					if (receiverErasure.findSuperTypeOriginatingFrom(field.declaringClass) == null) {
						fieldReceiverType = field.declaringClass; // handle indirect inheritance thru variable secondary bound
					}
				}				
				FieldBinding originalBinding = previousField.original();
			    if (originalBinding.type.leafComponentType().isTypeVariable()) {
			    	setGenericCast(index-1,originalBinding.type.genericCast(fieldReceiverType)); // type cannot be base-type even in boxing case
			    }
		    }
			// only last field is actually a write access if any
			if (isFieldUseDeprecated(field, scope, (this.bits & ASTNode.IsStrictlyAssigned) !=0 && index+1 == length)) {
				scope.problemReporter().deprecatedField(field, this);
			}
			// constant propagation can only be performed as long as the previous one is a constant too.
			if (this.constant != Constant.NotAConstant) {
				this.constant = field.constant();
			}

			if (field.isStatic()) {
				ReferenceBinding declaringClass = field.original().declaringClass;
				if (declaringClass.isEnum()) {
					MethodScope methodScope = scope.methodScope();
					SourceTypeBinding sourceType = methodScope.enclosingSourceType();
					if ((this.bits & ASTNode.IsStrictlyAssigned) == 0
							&& sourceType == declaringClass
							&& methodScope.lastVisibleFieldID >= 0
							&& field.id >= methodScope.lastVisibleFieldID
							&& (!field.isStatic() || methodScope.isStatic)) {
						scope.problemReporter().forwardReference(this, index, field);
					}					
					// check if accessing enum static field in initializer
					if ((sourceType == declaringClass || sourceType.superclass == declaringClass) // enum constant body
							&& field.constant() == Constant.NotAConstant
							&& !methodScope.isStatic
							&& methodScope.isInsideInitializerOrConstructor()) {
						scope.problemReporter().enumStaticFieldUsedDuringInitialization(field, this);
					}
				}
				// static field accessed through receiver? legal but unoptimal (optional warning)
				scope.problemReporter().nonStaticAccessToStaticField(this, field, index);
				// indirect static reference ?
				if (field.declaringClass != type) {
					scope.problemReporter().indirectAccessToStaticField(this, field);
				}
			}
			type = field.type;
			index++;
		} else {
			this.constant = Constant.NotAConstant; //don't fill other constants slots...
			scope.problemReporter().invalidField(this, field, index, type);
			setDepth(firstDepth);
			return null;
		}
	}
	setDepth(firstDepth);
	type = (this.otherBindings[otherBindingsLength - 1]).type;
	// perform capture conversion if read access
	return (type != null && (this.bits & ASTNode.IsStrictlyAssigned) == 0)
			? type.capture(scope, this.sourceEnd)
			: type;		
}

public void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {
	if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) == 0)	{
	//If inlinable field, forget the access emulation, the code gen will directly target it
	if (((this.bits & ASTNode.DepthMASK) == 0) || (this.constant != Constant.NotAConstant)) {
		return;
	}
	if ((this.bits & ASTNode.RestrictiveFlagMASK) == Binding.LOCAL) {
		currentScope.emulateOuterAccess((LocalVariableBinding) this.binding);
	}
	}
}

/**
 * index is <0 to denote write access emulation
 */
public void manageSyntheticAccessIfNecessary(BlockScope currentScope, FieldBinding fieldBinding, TypeBinding lastReceiverType, 	int index, FlowInfo flowInfo) {
	if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) != 0)	return;
	// index == 0 denotes the first fieldBinding, index > 0 denotes one of the 'otherBindings', index < 0 denotes a write access (to last binding)
	if (fieldBinding.constant() != Constant.NotAConstant)
		return;

	// if field from parameterized type got found, use the original field at codegen time
	FieldBinding originalField = fieldBinding.original();
	if (originalField != fieldBinding) {
		setCodegenBinding(index < 0 ? (this.otherBindings == null ? 0 : this.otherBindings.length) : index, originalField);
	}
	
	if (fieldBinding.isPrivate()) { // private access
	    FieldBinding someCodegenBinding = getCodegenBinding(index < 0 ? (this.otherBindings == null ? 0 : this.otherBindings.length) : index);
		if (someCodegenBinding.declaringClass != currentScope.enclosingSourceType()) {
		    setSyntheticAccessor(fieldBinding, index, 
		            ((SourceTypeBinding) someCodegenBinding.declaringClass).addSyntheticMethod(someCodegenBinding, index >= 0 /*read-access?*/));
			currentScope.problemReporter().needToEmulateFieldAccess(someCodegenBinding, this, index >= 0 /*read-access?*/);
			return;
		}
	} else if (fieldBinding.isProtected()){
	    int depth = (index == 0 || (index < 0 && this.otherDepths == null))
	    		? (this.bits & ASTNode.DepthMASK) >> ASTNode.DepthSHIFT 
	    		 : this.otherDepths[index < 0 ? this.otherDepths.length-1 : index-1];
		
		// implicit protected access 
		if (depth > 0 && (fieldBinding.declaringClass.getPackage() != currentScope.enclosingSourceType().getPackage())) {
		    FieldBinding someCodegenBinding = getCodegenBinding(index < 0 ? (this.otherBindings == null ? 0 : this.otherBindings.length) : index);
		    setSyntheticAccessor(fieldBinding, index, 
		            ((SourceTypeBinding) currentScope.enclosingSourceType().enclosingTypeAt(depth)).addSyntheticMethod(someCodegenBinding, index >= 0 /*read-access?*/));
			currentScope.problemReporter().needToEmulateFieldAccess(someCodegenBinding, this, index >= 0 /*read-access?*/);
			return;
		}
	}
	// if the binding declaring class is not visible, need special action
	// for runtime compatibility on 1.2 VMs : change the declaring class of the binding
	// NOTE: from target 1.2 on, field's declaring class is touched if any different from receiver type
	// and not from Object or implicit static field access.	
	if (fieldBinding.declaringClass != lastReceiverType
			&& !lastReceiverType.isArrayType()
			&& fieldBinding.declaringClass != null // array.length
			&& fieldBinding.constant() == Constant.NotAConstant) {
		CompilerOptions options = currentScope.compilerOptions();
		if ((options.targetJDK >= ClassFileConstants.JDK1_2
				&& (options.complianceLevel >= ClassFileConstants.JDK1_4 || !(index <= 1 &&  this.indexOfFirstFieldBinding == 1 && fieldBinding.isStatic()))
				&& fieldBinding.declaringClass.id != TypeIds.T_JavaLangObject) // no change for Object fields
				|| !fieldBinding.declaringClass.canBeSeenBy(currentScope)) {
	
		    setCodegenBinding(
		            index < 0 ? (this.otherBindings == null ? 0 : this.otherBindings.length) : index, 
		            currentScope.enclosingSourceType().getUpdatedFieldBinding(
		                    getCodegenBinding(index < 0 ? (this.otherBindings == null ? 0 : this.otherBindings.length) : index), 
		                    (ReferenceBinding)lastReceiverType.erasure()));
		}
	}			
}

public int nullStatus(FlowInfo flowInfo) {
	return FlowInfo.UNKNOWN;
}

public Constant optimizedBooleanConstant() {
	switch (this.resolvedType.id) {
		case T_boolean :
		case T_JavaLangBoolean :
			if (this.constant != Constant.NotAConstant) return this.constant;
			switch (this.bits & ASTNode.RestrictiveFlagMASK) {
				case Binding.FIELD : // reading a field
				if (this.otherBindings == null)
					return ((FieldBinding)this.binding).constant();
				// fall thru
			case Binding.LOCAL : // reading a local variable
				return this.otherBindings[this.otherBindings.length-1].constant();
		}
	}
	return Constant.NotAConstant;
}

/**
 * @see org.eclipse.jdt.internal.compiler.ast.Expression#postConversionType(Scope)
 */
public TypeBinding postConversionType(Scope scope) {
	TypeBinding convertedType = this.resolvedType;
	TypeBinding requiredGenericCast = getGenericCast(this.otherCodegenBindings == null ? 0 : this.otherCodegenBindings.length);
	if (requiredGenericCast != null) 
		convertedType = requiredGenericCast;
	int runtimeType = (this.implicitConversion & TypeIds.IMPLICIT_CONVERSION_MASK) >> 4;
	switch (runtimeType) {
		case T_boolean :
			convertedType = TypeBinding.BOOLEAN;
			break;
		case T_byte :
			convertedType = TypeBinding.BYTE;
			break;
		case T_short :
			convertedType = TypeBinding.SHORT;
			break;
		case T_char :
			convertedType = TypeBinding.CHAR;
			break;
		case T_int :
			convertedType = TypeBinding.INT;
			break;
		case T_float :
			convertedType = TypeBinding.FLOAT;
			break;
		case T_long :
			convertedType = TypeBinding.LONG;
			break;
		case T_double :
			convertedType = TypeBinding.DOUBLE;
			break;
		default :
	}		
	if ((this.implicitConversion & TypeIds.BOXING) != 0) {
		convertedType = scope.environment().computeBoxingType(convertedType);
	}
	return convertedType;
}

public StringBuffer printExpression(int indent, StringBuffer output) {
	for (int i = 0; i < this.tokens.length; i++) {
		if (i > 0) output.append('.');
		output.append(this.tokens[i]);
	}
	return output;
}
	
/**
 * Normal field binding did not work, try to bind to a field of the delegate receiver.
 */
public TypeBinding reportError(BlockScope scope) {
	if (this.binding instanceof ProblemFieldBinding) {
		scope.problemReporter().invalidField(this, (FieldBinding) this.binding);
	} else if (this.binding instanceof ProblemReferenceBinding || this.binding instanceof MissingTypeBinding) {
		scope.problemReporter().invalidType(this, (TypeBinding) this.binding);
	} else {
		scope.problemReporter().unresolvableReference(this, this.binding);
	}
	return null;
}

public TypeBinding resolveType(BlockScope scope) {
	// field and/or local are done before type lookups
	// the only available value for the restrictiveFlag BEFORE
	// the TC is Flag_Type Flag_LocalField and Flag_TypeLocalField 
	this.actualReceiverType = scope.enclosingReceiverType();
	this.constant = Constant.NotAConstant;
	if ((this.codegenBinding = this.binding = scope.getBinding(this.tokens, this.bits & ASTNode.RestrictiveFlagMASK, this, true /*resolve*/)).isValidBinding()) {
		switch (this.bits & ASTNode.RestrictiveFlagMASK) {
			case Binding.VARIABLE : //============only variable===========
			case Binding.TYPE | Binding.VARIABLE :
				if (this.binding instanceof LocalVariableBinding) {
					LocalVariableBinding local = (LocalVariableBinding) this.binding;
					if (!local.isFinal() && ((this.bits & ASTNode.DepthMASK) != 0))
						scope.problemReporter().cannotReferToNonFinalOuterLocal((LocalVariableBinding) this.binding, this);
					this.bits &= ~ASTNode.RestrictiveFlagMASK; // clear bits
					this.bits |= Binding.LOCAL;
					if (local.type != null && (local.type.tagBits & TagBits.HasMissingType) != 0) {
						// only complain if field reference (for local, its type got flagged already)
						return null;
					}
					this.resolvedType = getOtherFieldBindings(scope);
					if (this.resolvedType != null 
							&& (this.resolvedType.tagBits & TagBits.HasMissingType) != 0) {
						FieldBinding lastField = this.otherBindings[this.otherBindings.length - 1];
						scope.problemReporter().invalidField(this, new ProblemFieldBinding(lastField.declaringClass, lastField.name, ProblemReasons.NotFound), this.tokens.length, this.resolvedType.leafComponentType());
						return null;
					}
					return this.resolvedType;					
				}
				if (this.binding instanceof FieldBinding) {
					FieldBinding fieldBinding = (FieldBinding) this.binding;
					MethodScope methodScope = scope.methodScope();
					TypeBinding declaringClass = fieldBinding.original().declaringClass;
					// check for forward references
					if ((this.indexOfFirstFieldBinding == 1 || declaringClass.isEnum())
							&& methodScope.enclosingSourceType() == declaringClass
							&& methodScope.lastVisibleFieldID >= 0
							&& fieldBinding.id >= methodScope.lastVisibleFieldID
							&& (!fieldBinding.isStatic() || methodScope.isStatic)) {
						scope.problemReporter().forwardReference(this, 0, fieldBinding);
					}
					if (fieldBinding.isStatic()) {
						// check if accessing enum static field in initializer					
						if (declaringClass.isEnum()) {
							SourceTypeBinding sourceType = methodScope.enclosingSourceType();
							if ((sourceType == declaringClass || sourceType.superclass == declaringClass) // enum constant body
									&& fieldBinding.constant() == Constant.NotAConstant
									&& !methodScope.isStatic
									&& methodScope.isInsideInitializerOrConstructor()) {
								scope.problemReporter().enumStaticFieldUsedDuringInitialization(fieldBinding, this);
							}
						}						
					} else if (this.indexOfFirstFieldBinding == 1 && scope.compilerOptions().getSeverity(CompilerOptions.UnqualifiedFieldAccess) != ProblemSeverities.Ignore) {
						scope.problemReporter().unqualifiedFieldAccess(this, fieldBinding);
					}
					this.bits &= ~ASTNode.RestrictiveFlagMASK; // clear bits
					this.bits |= Binding.FIELD;
					
//						// check for deprecated receiver type
//						// deprecation check for receiver type if not first token
//						if (indexOfFirstFieldBinding > 1) {
//							if (isTypeUseDeprecated(this.actualReceiverType, scope))
//								scope.problemReporter().deprecatedType(this.actualReceiverType, this);
//						}
					this.resolvedType = getOtherFieldBindings(scope);
					if (this.resolvedType != null 
							&& (this.resolvedType.tagBits & TagBits.HasMissingType) != 0) {
						FieldBinding lastField = this.indexOfFirstFieldBinding == this.tokens.length ? (FieldBinding)this.binding : this.otherBindings[this.otherBindings.length - 1];
						scope.problemReporter().invalidField(this, new ProblemFieldBinding(lastField.declaringClass, lastField.name, ProblemReasons.NotFound), this.tokens.length, this.resolvedType.leafComponentType());
						return null;
					}
					return this.resolvedType;
				}
				// thus it was a type
				this.bits &= ~ASTNode.RestrictiveFlagMASK; // clear bits
				this.bits |= Binding.TYPE;
			case Binding.TYPE : //=============only type ==============
			    TypeBinding type = (TypeBinding) this.binding;
//					if (isTypeUseDeprecated(type, scope))
//						scope.problemReporter().deprecatedType(type, this);
				type = scope.environment().convertToRawType(type, false /*do not force conversion of enclosing types*/);
				return this.resolvedType = type;
		}
	}
	//========error cases===============
	return this.resolvedType = this.reportError(scope);
}

// set the matching codegenBinding and generic cast
protected void setCodegenBinding(int index, FieldBinding someCodegenBinding) {
	if (index == 0){
		this.codegenBinding = someCodegenBinding;
	} else {
	    int length = this.otherBindings.length;
		if (this.otherCodegenBindings == this.otherBindings){
			System.arraycopy(this.otherBindings, 0, this.otherCodegenBindings = new FieldBinding[length], 0, length);
		}
		this.otherCodegenBindings[index-1] = someCodegenBinding;
	}	    
}

public void setFieldIndex(int index) {
	this.indexOfFirstFieldBinding = index;
}

// set the matching codegenBinding and generic cast
protected void setGenericCast(int index, TypeBinding someGenericCast) {
	if (someGenericCast == null) return;
	if (index == 0){
		this.genericCast = someGenericCast;
	} else {
	    if (this.otherGenericCasts == null) {
	        this.otherGenericCasts = new TypeBinding[this.otherBindings.length];
	    }
	    this.otherGenericCasts[index-1] = someGenericCast;
	}	    
}

// set the matching synthetic accessor
protected void setSyntheticAccessor(FieldBinding fieldBinding, int index, SyntheticMethodBinding syntheticAccessor) {
	if (index < 0) { // write-access ?
		this.syntheticWriteAccessor = syntheticAccessor;
    } else {
		if (this.syntheticReadAccessors == null) {
			this.syntheticReadAccessors = new SyntheticMethodBinding[this.otherBindings == null ? 1 : this.otherBindings.length + 1];
		}
		this.syntheticReadAccessors[index] = syntheticAccessor;
    }
}

public void traverse(ASTVisitor visitor, BlockScope scope) {
	visitor.visit(this, scope);
	visitor.endVisit(this, scope);
}

public void traverse(ASTVisitor visitor, ClassScope scope) {
	visitor.visit(this, scope);
	visitor.endVisit(this, scope);
}

public String unboundReferenceErrorName() {
	return new String(this.tokens[0]);
}
}
