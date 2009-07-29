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
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public class FieldReference extends Reference implements InvocationSite {

	public static final int READ = 0;
	public static final int WRITE = 1;
	public Expression receiver;
	public char[] token;
	public FieldBinding binding;															// exact binding resulting from lookup
	protected FieldBinding codegenBinding;									// actual binding used for code generation (if no synthetic accessor)
	public MethodBinding[] syntheticAccessors; // [0]=read accessor [1]=write accessor
	
	public long nameSourcePosition; //(start<<32)+end
	public TypeBinding receiverType;
	public TypeBinding genericCast;
	
public FieldReference(char[] source, long pos) {
	this.token = source;
	this.nameSourcePosition = pos;
	//by default the position are the one of the field (not true for super access)
	this.sourceStart = (int) (pos >>> 32);
	this.sourceEnd = (int) (pos & 0x00000000FFFFFFFFL);
	this.bits |= Binding.FIELD;

}

public FlowInfo analyseAssignment(BlockScope currentScope, 	FlowContext flowContext, 	FlowInfo flowInfo, Assignment assignment, boolean isCompound) {
	// compound assignment extra work
	if (isCompound) { // check the variable part is initialized if blank final
		if (this.binding.isBlankFinal()
			&& this.receiver.isThis()
			&& currentScope.needBlankFinalFieldInitializationCheck(this.binding)
			&& (!flowInfo.isDefinitelyAssigned(this.binding))) {
			currentScope.problemReporter().uninitializedBlankFinalField(this.binding, this);
			// we could improve error msg here telling "cannot use compound assignment on final blank field"
		}
		manageSyntheticAccessIfNecessary(currentScope, flowInfo, true /*read-access*/);
	}
	flowInfo =
		this.receiver
			.analyseCode(currentScope, flowContext, flowInfo, !this.binding.isStatic())
			.unconditionalInits();
	if (assignment.expression != null) {
		flowInfo =
			assignment
				.expression
				.analyseCode(currentScope, flowContext, flowInfo)
				.unconditionalInits();
	}
	manageSyntheticAccessIfNecessary(currentScope, flowInfo, false /*write-access*/);

	// check if assigning a final field 
	if (this.binding.isFinal()) {
		// in a context where it can be assigned?
		if (this.binding.isBlankFinal()
			&& !isCompound
			&& this.receiver.isThis()
			&& !(this.receiver instanceof QualifiedThisReference)
			&& ((this.receiver.bits & ASTNode.ParenthesizedMASK) == 0) // (this).x is forbidden
			&& currentScope.allowBlankFinalFieldAssignment(this.binding)) {
			if (flowInfo.isPotentiallyAssigned(this.binding)) {
				currentScope.problemReporter().duplicateInitializationOfBlankFinalField(
					this.binding,
					this);
			} else {
				flowContext.recordSettingFinal(this.binding, this, flowInfo);
			}
			flowInfo.markAsDefinitelyAssigned(this.binding);
		} else {
			// assigning a final field outside an initializer or constructor or wrong reference
			currentScope.problemReporter().cannotAssignToFinalField(this.binding, this);
		}
	}
	return flowInfo;
}

public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	return analyseCode(currentScope, flowContext, flowInfo, true);
}

public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, boolean valueRequired) {
	boolean nonStatic = !this.binding.isStatic();
	this.receiver.analyseCode(currentScope, flowContext, flowInfo, nonStatic);
	if (nonStatic) {
		this.receiver.checkNPE(currentScope, flowContext, flowInfo);
	}
	
	if (valueRequired || currentScope.compilerOptions().complianceLevel >= ClassFileConstants.JDK1_4) {
		manageSyntheticAccessIfNecessary(currentScope, flowInfo, true /*read-access*/);
	}
	return flowInfo;
}

/**
 * @see org.eclipse.jdt.internal.compiler.ast.Expression#computeConversion(org.eclipse.jdt.internal.compiler.lookup.Scope, org.eclipse.jdt.internal.compiler.lookup.TypeBinding, org.eclipse.jdt.internal.compiler.lookup.TypeBinding)
 */
public void computeConversion(Scope scope, TypeBinding runtimeTimeType, TypeBinding compileTimeType) {
	if (runtimeTimeType == null || compileTimeType == null)
		return;		
	// set the generic cast after the fact, once the type expectation is fully known (no need for strict cast)
	if (this.binding != null && this.binding.isValidBinding()) {
		FieldBinding originalBinding = this.binding.original();
		TypeBinding originalType = originalBinding.type;
	    // extra cast needed if field type is type variable
		if (originalType.leafComponentType().isTypeVariable()) {
	    	TypeBinding targetType = (!compileTimeType.isBaseType() && runtimeTimeType.isBaseType()) 
	    		? compileTimeType  // unboxing: checkcast before conversion
	    		: runtimeTimeType;
	        this.genericCast = originalBinding.type.genericCast(targetType);
	        if (this.genericCast instanceof ReferenceBinding) {
				ReferenceBinding referenceCast = (ReferenceBinding) this.genericCast;
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

public FieldBinding fieldBinding() {
	return this.binding;
}

public void generateAssignment(BlockScope currentScope, CodeStream codeStream, Assignment assignment, boolean valueRequired) {
	int pc = codeStream.position;
	this.receiver.generateCode(
		currentScope,
		codeStream,
		!this.codegenBinding.isStatic());
	codeStream.recordPositionsFrom(pc, this.sourceStart);
	assignment.expression.generateCode(currentScope, codeStream, true);
	fieldStore(
		codeStream,
		this.codegenBinding,
		this.syntheticAccessors == null ? null : this.syntheticAccessors[FieldReference.WRITE],
		valueRequired);
	if (valueRequired) {
		codeStream.generateImplicitConversion(assignment.implicitConversion);
	}
	// no need for generic cast as value got dupped
}

/**
 * Field reference code generation
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 * @param valueRequired boolean
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	int pc = codeStream.position;
	if (this.constant != Constant.NotAConstant) {
		if (valueRequired) {
			codeStream.generateConstant(this.constant, this.implicitConversion);
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
		return;
	}
	boolean isStatic = this.codegenBinding.isStatic();
	boolean isThisReceiver = this.receiver instanceof ThisReference;
	Constant fieldConstant = this.codegenBinding.constant();
	if (fieldConstant != Constant.NotAConstant) {
		if (!isThisReceiver) {
			this.receiver.generateCode(currentScope, codeStream, !isStatic);
			if (!isStatic){
				codeStream.invokeObjectGetClass();
				codeStream.pop();
			}
		}
		if (valueRequired) {
			codeStream.generateConstant(fieldConstant, this.implicitConversion);
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
		return;
	}
	if (valueRequired 
			|| (!isThisReceiver && currentScope.compilerOptions().complianceLevel >= ClassFileConstants.JDK1_4)
			|| ((this.implicitConversion & TypeIds.UNBOXING) != 0)
			|| (this.genericCast != null)) {
		this.receiver.generateCode(currentScope, codeStream, !isStatic);
		pc = codeStream.position;
		if (this.codegenBinding.declaringClass == null) { // array length
			codeStream.arraylength();
			if (valueRequired) {
				codeStream.generateImplicitConversion(this.implicitConversion);
			} else {
				// could occur if !valueRequired but compliance >= 1.4
				codeStream.pop();
			}
		} else {
			if (this.syntheticAccessors == null || this.syntheticAccessors[FieldReference.READ] == null) {
				if (isStatic) {
					codeStream.getstatic(this.codegenBinding);
				} else {
					codeStream.getfield(this.codegenBinding);
				}
			} else {
				codeStream.invokestatic(this.syntheticAccessors[FieldReference.READ]);
			}
			// required cast must occur even if no value is required
			if (this.genericCast != null) codeStream.checkcast(this.genericCast);
			if (valueRequired) {
				codeStream.generateImplicitConversion(this.implicitConversion);
			} else {
				boolean isUnboxing = (this.implicitConversion & TypeIds.UNBOXING) != 0;
				// conversion only generated if unboxing
				if (isUnboxing) codeStream.generateImplicitConversion(this.implicitConversion);
				switch (isUnboxing ? postConversionType(currentScope).id : this.codegenBinding.type.id) {
					case T_long :
					case T_double :
						codeStream.pop2();
						break;
					default :
						codeStream.pop();
				}
			}
		}
	} else {
		if (isThisReceiver) {
			if (isStatic){
				// if no valueRequired, still need possible side-effects of <clinit> invocation, if field belongs to different class
				if (this.binding.original().declaringClass != this.receiverType.erasure()) {
					MethodBinding accessor = this.syntheticAccessors == null ? null : this.syntheticAccessors[FieldReference.READ]; 
					if (accessor == null) {
						codeStream.getstatic(this.codegenBinding);
					} else {
						codeStream.invokestatic(accessor);
					}
					switch (this.codegenBinding.type.id) {
						case T_long :
						case T_double :
							codeStream.pop2();
							break;
						default :
							codeStream.pop();
					}
				}
			}
		} else {
			this.receiver.generateCode(currentScope, codeStream, !isStatic);				
			if (!isStatic){
				codeStream.invokeObjectGetClass(); // perform null check
				codeStream.pop();
			}
		}						
	}
	codeStream.recordPositionsFrom(pc, this.sourceEnd);
}

public void generateCompoundAssignment(BlockScope currentScope, CodeStream codeStream, Expression expression, int operator, int assignmentImplicitConversion, boolean valueRequired) {
	boolean isStatic;
	this.receiver.generateCode(
		currentScope,
		codeStream,
		!(isStatic = this.codegenBinding.isStatic()));
	if (isStatic) {
		if (this.syntheticAccessors == null || this.syntheticAccessors[FieldReference.READ] == null) {
			codeStream.getstatic(this.codegenBinding);
		} else {
			codeStream.invokestatic(this.syntheticAccessors[FieldReference.READ]);
		}
	} else {
		codeStream.dup();
		if (this.syntheticAccessors == null || this.syntheticAccessors[FieldReference.READ] == null) {
			codeStream.getfield(this.codegenBinding);
		} else {
			codeStream.invokestatic(this.syntheticAccessors[FieldReference.READ]);
		}
	}
	int operationTypeID;
	switch(operationTypeID = (this.implicitConversion & TypeIds.IMPLICIT_CONVERSION_MASK) >> 4) {
		case T_JavaLangString :
		case T_JavaLangObject :
		case T_undefined :
			codeStream.generateStringConcatenationAppend(currentScope, null, expression);
			break;
		default :
			if (this.genericCast != null)
				codeStream.checkcast(this.genericCast);				
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
	fieldStore(
		codeStream,
		this.codegenBinding,
		this.syntheticAccessors == null ? null : this.syntheticAccessors[FieldReference.WRITE],
		valueRequired);
	// no need for generic cast as value got dupped
}

public void generatePostIncrement(BlockScope currentScope, CodeStream codeStream, CompoundAssignment postIncrement, boolean valueRequired) {
	boolean isStatic;
	this.receiver.generateCode(
		currentScope,
		codeStream,
		!(isStatic = this.codegenBinding.isStatic()));
	if (isStatic) {
		if (this.syntheticAccessors == null || this.syntheticAccessors[FieldReference.READ] == null) {
			codeStream.getstatic(this.codegenBinding);
		} else {
			codeStream.invokestatic(this.syntheticAccessors[FieldReference.READ]);
		}
	} else {
		codeStream.dup();
		if (this.syntheticAccessors == null || this.syntheticAccessors[FieldReference.READ] == null) {
			codeStream.getfield(this.codegenBinding);
		} else {
			codeStream.invokestatic(this.syntheticAccessors[FieldReference.READ]);
		}
	}
	TypeBinding operandType;
	if (this.genericCast != null) {
		codeStream.checkcast(this.genericCast);
		operandType = this.genericCast;
	} else {
		operandType = this.codegenBinding.type;
	}	
	if (valueRequired) {
		if (isStatic) {
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
	fieldStore(codeStream, this.codegenBinding, this.syntheticAccessors == null ? null : this.syntheticAccessors[FieldReference.WRITE], false);
}	

/**
 * @see org.eclipse.jdt.internal.compiler.lookup.InvocationSite#genericTypeArguments()
 */
public TypeBinding[] genericTypeArguments() {
	return null;
}
public boolean isSuperAccess() {
	return this.receiver.isSuper();
}

public boolean isTypeAccess() {
	return this.receiver != null && this.receiver.isTypeReference();
}

/*
 * No need to emulate access to protected fields since not implicitly accessed
 */
public void manageSyntheticAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo, boolean isReadAccess) {
	if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) != 0)	return;

	// if field from parameterized type got found, use the original field at codegen time
	this.codegenBinding = this.binding.original();
	
	if (this.binding.isPrivate()) {
		if ((currentScope.enclosingSourceType() != this.codegenBinding.declaringClass) 
				&& this.binding.constant() == Constant.NotAConstant) {
			if (this.syntheticAccessors == null)
				this.syntheticAccessors = new MethodBinding[2];
			this.syntheticAccessors[isReadAccess ? FieldReference.READ : FieldReference.WRITE] = 
				((SourceTypeBinding) this.codegenBinding.declaringClass).addSyntheticMethod(this.codegenBinding, isReadAccess);
			currentScope.problemReporter().needToEmulateFieldAccess(this.codegenBinding, this, isReadAccess);
			return;
		}

	} else if (this.receiver instanceof QualifiedSuperReference) { // qualified super

		// qualified super need emulation always
		SourceTypeBinding destinationType =
			(SourceTypeBinding) (((QualifiedSuperReference) this.receiver)
				.currentCompatibleType);
		if (this.syntheticAccessors == null)
			this.syntheticAccessors = new MethodBinding[2];
		this.syntheticAccessors[isReadAccess ? FieldReference.READ : FieldReference.WRITE] = destinationType.addSyntheticMethod(this.codegenBinding, isReadAccess);
		currentScope.problemReporter().needToEmulateFieldAccess(this.codegenBinding, this, isReadAccess);
		return;

	} else if (this.binding.isProtected()) {

		SourceTypeBinding enclosingSourceType;
		if (((this.bits & ASTNode.DepthMASK) != 0)
			&& this.binding.declaringClass.getPackage()
				!= (enclosingSourceType = currentScope.enclosingSourceType()).getPackage()) {

			SourceTypeBinding currentCompatibleType =
				(SourceTypeBinding) enclosingSourceType.enclosingTypeAt(
					(this.bits & ASTNode.DepthMASK) >> ASTNode.DepthSHIFT);
			if (this.syntheticAccessors == null)
				this.syntheticAccessors = new MethodBinding[2];
			this.syntheticAccessors[isReadAccess ? FieldReference.READ : FieldReference.WRITE] = currentCompatibleType.addSyntheticMethod(this.codegenBinding, isReadAccess);
			currentScope.problemReporter().needToEmulateFieldAccess(this.codegenBinding, this, isReadAccess);
			return;
		}
	}
	// if the binding declaring class is not visible, need special action
	// for runtime compatibility on 1.2 VMs : change the declaring class of the binding
	// NOTE: from target 1.2 on, field's declaring class is touched if any different from receiver type
	// and not from Object or implicit static field access.	
	if (this.binding.declaringClass != this.receiverType
			&& !this.receiverType.isArrayType()
			&& this.binding.declaringClass != null // array.length
			&& this.binding.constant() == Constant.NotAConstant) {
		CompilerOptions options = currentScope.compilerOptions();
		if ((options.targetJDK >= ClassFileConstants.JDK1_2
				&& (options.complianceLevel >= ClassFileConstants.JDK1_4 || !(this.receiver.isImplicitThis() && this.codegenBinding.isStatic()))
				&& this.binding.declaringClass.id != TypeIds.T_JavaLangObject) // no change for Object fields
			|| !this.binding.declaringClass.canBeSeenBy(currentScope)) {

			this.codegenBinding =
				currentScope.enclosingSourceType().getUpdatedFieldBinding(
					this.codegenBinding,
					(ReferenceBinding) this.receiverType.erasure());
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
			return this.constant != Constant.NotAConstant ? this.constant : this.binding.constant();
		default :
			return Constant.NotAConstant;
	}
}

/**
 * @see org.eclipse.jdt.internal.compiler.ast.Expression#postConversionType(Scope)
 */
public TypeBinding postConversionType(Scope scope) {
	TypeBinding convertedType = this.resolvedType;
	if (this.genericCast != null) 
		convertedType = this.genericCast;
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
	return this.receiver.printExpression(0, output).append('.').append(this.token);
}

public TypeBinding resolveType(BlockScope scope) {
	// Answer the signature type of the field.
	// constants are propaged when the field is final
	// and initialized with a (compile time) constant 

	//always ignore receiver cast, since may affect constant pool reference
	boolean receiverCast = false;
	if (this.receiver instanceof CastExpression) {
		this.receiver.bits |= ASTNode.DisableUnnecessaryCastCheck; // will check later on
		receiverCast = true;
	}
	this.receiverType = this.receiver.resolveType(scope);
	if (this.receiverType == null) {
		this.constant = Constant.NotAConstant;
		return null;
	}
	if (receiverCast) {
		 // due to change of declaring class with receiver type, only identity cast should be notified
		if (((CastExpression)this.receiver).expression.resolvedType == this.receiverType) { 
				scope.problemReporter().unnecessaryCast((CastExpression)this.receiver);		
		}
	}		
	// the case receiverType.isArrayType and token = 'length' is handled by the scope API
	FieldBinding fieldBinding = this.codegenBinding = this.binding = scope.getField(this.receiverType, this.token, this);
	if (!fieldBinding.isValidBinding()) {
		this.constant = Constant.NotAConstant;
		if (this.receiver.resolvedType instanceof ProblemReferenceBinding) {
			// problem already got signaled on receiver, do not report secondary problem
			return null;
		}
		scope.problemReporter().invalidField(this, this.receiverType);
		return null;
	}
	TypeBinding receiverErasure = this.receiverType.erasure();
	if (receiverErasure instanceof ReferenceBinding) {
		if (receiverErasure.findSuperTypeOriginatingFrom(fieldBinding.declaringClass) == null) {
			this.receiverType = fieldBinding.declaringClass; // handle indirect inheritance thru variable secondary bound
		}
	}
	this.receiver.computeConversion(scope, this.receiverType, this.receiverType);
	if (isFieldUseDeprecated(fieldBinding, scope, (this.bits & ASTNode.IsStrictlyAssigned) !=0)) {
		scope.problemReporter().deprecatedField(fieldBinding, this);
	}
	boolean isImplicitThisRcv = this.receiver.isImplicitThis();
	this.constant = isImplicitThisRcv ? fieldBinding.constant() : Constant.NotAConstant;
	if (fieldBinding.isStatic()) {
		// static field accessed through receiver? legal but unoptimal (optional warning)
		if (!(isImplicitThisRcv
				|| (this.receiver instanceof NameReference 
					&& (((NameReference) this.receiver).bits & Binding.TYPE) != 0))) {
			scope.problemReporter().nonStaticAccessToStaticField(this, fieldBinding);
		}
		ReferenceBinding declaringClass = this.binding.declaringClass;
		if (!isImplicitThisRcv
				&& declaringClass != this.receiverType
				&& declaringClass.canBeSeenBy(scope)) {
			scope.problemReporter().indirectAccessToStaticField(this, fieldBinding);
		}
		// check if accessing enum static field in initializer
		if (declaringClass.isEnum()) {
			MethodScope methodScope = scope.methodScope();
			SourceTypeBinding sourceType = scope.enclosingSourceType();
			if (this.constant == Constant.NotAConstant
					&& !methodScope.isStatic
					&& (sourceType == declaringClass || sourceType.superclass == declaringClass) // enum constant body
					&& methodScope.isInsideInitializerOrConstructor()) {
				scope.problemReporter().enumStaticFieldUsedDuringInitialization(this.binding, this);
			}
		}		
	}
	TypeBinding fieldType = fieldBinding.type;
	if (fieldType != null) {
		if ((this.bits & ASTNode.IsStrictlyAssigned) == 0) {
			fieldType = fieldType.capture(scope, this.sourceEnd);	// perform capture conversion if read access
		}
		this.resolvedType = fieldType;
		if ((fieldType.tagBits & TagBits.HasMissingType) != 0) {
			scope.problemReporter().invalidType(this, fieldType);
			return null;
		}	
	}
	return fieldType;
}

public void setActualReceiverType(ReferenceBinding receiverType) {
	// ignored
}

public void setDepth(int depth) {
	this.bits &= ~ASTNode.DepthMASK; // flush previous depth if any			
	if (depth > 0) {
		this.bits |= (depth & 0xFF) << ASTNode.DepthSHIFT; // encoded on 8 bits
	}
}

public void setFieldIndex(int index) {
	// ignored
}

public void traverse(ASTVisitor visitor, BlockScope scope) {
	if (visitor.visit(this, scope)) {
		this.receiver.traverse(visitor, scope);
	}
	visitor.endVisit(this, scope);
}
}
