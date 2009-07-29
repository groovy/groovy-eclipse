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
package org.eclipse.jdt.internal.eval;

import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.CompoundAssignment;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;

/**
 * A single name reference inside a code snippet can denote a field of a remote
 * receiver object (that is, the receiver of the context in the stack frame).
 */
public class CodeSnippetSingleNameReference extends SingleNameReference implements EvaluationConstants, ProblemReasons {

	EvaluationContext evaluationContext;
	FieldBinding delegateThis;
	
public CodeSnippetSingleNameReference(char[] source, long pos, EvaluationContext evaluationContext) {
	super(source, pos);
	this.evaluationContext = evaluationContext;
}
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, boolean valueRequired) {

	switch (this.bits & RestrictiveFlagMASK) {
		case Binding.FIELD : // reading a field
			// check if reading a final blank field
			FieldBinding fieldBinding;
			if ((fieldBinding = (FieldBinding) this.binding).isBlankFinal() 
					&& currentScope.needBlankFinalFieldInitializationCheck(fieldBinding)) {
				if (!flowInfo.isDefinitelyAssigned(fieldBinding)) {
					currentScope.problemReporter().uninitializedBlankFinalField(fieldBinding, this);
				}
			}
			break;
		case Binding.LOCAL : // reading a local variable
			LocalVariableBinding localBinding;
			if (!flowInfo.isDefinitelyAssigned(localBinding = (LocalVariableBinding) this.binding)) {
				currentScope.problemReporter().uninitializedLocalVariable(localBinding, this);
			}
			if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) == 0) {
				localBinding.useFlag = LocalVariableBinding.USED;
			} else if (localBinding.useFlag == LocalVariableBinding.UNUSED) {
				localBinding.useFlag = LocalVariableBinding.FAKE_USED;
			}
	}
	return flowInfo;
}
/**
 * Check and/or redirect the field access to the delegate receiver if any
 */
public TypeBinding checkFieldAccess(BlockScope scope) {

	if (this.delegateThis == null) {
		return super.checkFieldAccess(scope);
	}
	FieldBinding fieldBinding = (FieldBinding) this.binding;
	this.bits &= ~RestrictiveFlagMASK; // clear bits
	this.bits |= Binding.FIELD;
	if (!fieldBinding.isStatic()) {
		// must check for the static status....
		if (this.evaluationContext.isStatic) {
			scope.problemReporter().staticFieldAccessToNonStaticVariable(
				this,
				fieldBinding);
			this.constant = Constant.NotAConstant;
			return null;
		}
	}
	this.constant = fieldBinding.constant();

	if (isFieldUseDeprecated(fieldBinding, scope, (this.bits & IsStrictlyAssigned) !=0)) {
		scope.problemReporter().deprecatedField(fieldBinding, this);
	}
	return fieldBinding.type;

}
public void generateAssignment(BlockScope currentScope, CodeStream codeStream, Assignment assignment, boolean valueRequired) {

	// optimizing assignment like: i = i + 1 or i = 1 + i
	if (assignment.expression.isCompactableOperation()) {
		BinaryExpression operation = (BinaryExpression) assignment.expression;
		int operator = (operation.bits & OperatorMASK) >> OperatorSHIFT;
		SingleNameReference variableReference;
		if ((operation.left instanceof SingleNameReference) && ((variableReference = (SingleNameReference) operation.left).binding == this.binding)) {
			// i = i + value, then use the variable on the right hand side, since it has the correct implicit conversion
			variableReference.generateCompoundAssignment(currentScope, codeStream, this.syntheticAccessors == null ? null : this.syntheticAccessors[WRITE], operation.right, operator, operation.implicitConversion, valueRequired);
			if (valueRequired) {
				codeStream.generateImplicitConversion(assignment.implicitConversion);
			}
			return;
		}
		if ((operation.right instanceof SingleNameReference)
			&& ((operator == PLUS) || (operator == MULTIPLY)) // only commutative operations
			&& ((variableReference = (SingleNameReference) operation.right).binding == this.binding)
			&& (operation.left.constant != Constant.NotAConstant) // exclude non constant expressions, since could have side-effect
			&& (((operation.left.implicitConversion & IMPLICIT_CONVERSION_MASK) >> 4) != T_JavaLangString) // exclude string concatenation which would occur backwards
			&& (((operation.right.implicitConversion & IMPLICIT_CONVERSION_MASK) >> 4) != T_JavaLangString)) { // exclude string concatenation which would occur backwards
			// i = value + i, then use the variable on the right hand side, since it has the correct implicit conversion
			variableReference.generateCompoundAssignment(currentScope, codeStream, this.syntheticAccessors == null ? null : this.syntheticAccessors[WRITE], operation.left, operator, operation.implicitConversion, valueRequired);
			if (valueRequired) {
				codeStream.generateImplicitConversion(assignment.implicitConversion);
			}
			return;
		}
	}
	switch (this.bits & RestrictiveFlagMASK) {
		case Binding.FIELD : // assigning to a field
			FieldBinding fieldBinding = (FieldBinding) this.codegenBinding;
			if (fieldBinding.canBeSeenBy(getReceiverType(currentScope), this, currentScope)) {
				if (!fieldBinding.isStatic()) { // need a receiver?
					if ((this.bits & DepthMASK) != 0) {
						ReferenceBinding targetType = currentScope.enclosingSourceType().enclosingTypeAt((this.bits & DepthMASK) >> DepthSHIFT);
						Object[] emulationPath = currentScope.getEmulationPath(targetType, true /*only exact match*/, false/*consider enclosing arg*/);
						codeStream.generateOuterAccess(emulationPath, this, targetType, currentScope);
					} else {
						this.generateReceiver(codeStream);
					}
				}
				assignment.expression.generateCode(currentScope, codeStream, true);
				fieldStore(codeStream, fieldBinding, null, valueRequired);
				if (valueRequired) {
					codeStream.generateImplicitConversion(assignment.implicitConversion);
				}
			} else {
				codeStream.generateEmulationForField(fieldBinding);
				if (!fieldBinding.isStatic()) { // need a receiver?
					if ((this.bits & DepthMASK) != 0) {
						// internal error, per construction we should have found it
						// not yet supported
						currentScope.problemReporter().needImplementation(this);
					} else {
						this.generateReceiver(codeStream);
					}
				} else {
					codeStream.aconst_null();
				}
				assignment.expression.generateCode(currentScope, codeStream, true);
				if (valueRequired) {
					if ((fieldBinding.type == TypeBinding.LONG) || (fieldBinding.type == TypeBinding.DOUBLE)) {
						codeStream.dup2_x2();
					} else {
						codeStream.dup_x2();
					}
				}
				codeStream.generateEmulatedWriteAccessForField(fieldBinding);
				if (valueRequired) {
					codeStream.generateImplicitConversion(assignment.implicitConversion);
				}
			}
			return;
		case Binding.LOCAL : // assigning to a local variable
			LocalVariableBinding localBinding = (LocalVariableBinding) this.codegenBinding;
			if (localBinding.resolvedPosition != -1) {
				assignment.expression.generateCode(currentScope, codeStream, true);
			} else {
				if (assignment.expression.constant != Constant.NotAConstant) {
					// assigning an unused local to a constant value = no actual assignment is necessary
					if (valueRequired) {
						codeStream.generateConstant(assignment.expression.constant, assignment.implicitConversion);
					}
				} else {
					assignment.expression.generateCode(currentScope, codeStream, true);
					/* Even though the value may not be required, we force it to be produced, and discard it later
					on if it was actually not necessary, so as to provide the same behavior as JDK1.2beta3.	*/
					if (valueRequired) {
						codeStream.generateImplicitConversion(assignment.implicitConversion); // implicit conversion
					} else {
						if ((localBinding.type == TypeBinding.LONG) || (localBinding.type == TypeBinding.DOUBLE)) {
							codeStream.pop2();
						} else {
							codeStream.pop();
						}
					}
				}
				return;
			}
			// normal local assignment (since cannot store in outer local which are final locations)
			codeStream.store(localBinding, valueRequired);
			if ((this.bits & FirstAssignmentToLocal) != 0) { // for local variable debug attributes
				localBinding.recordInitializationStartPC(codeStream.position);
			}
			// implicit conversion
			if (valueRequired) {
				codeStream.generateImplicitConversion(assignment.implicitConversion);
			}
	}
}
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	int pc = codeStream.position;
	if (this.constant != Constant.NotAConstant) {
		if (valueRequired) {
			codeStream.generateConstant(this.constant, this.implicitConversion);
		}
	} else {
		switch (this.bits & RestrictiveFlagMASK) {
			case Binding.FIELD : // reading a field
				if (!valueRequired) 
					break;
				FieldBinding fieldBinding = (FieldBinding) this.codegenBinding;
				Constant fieldConstant = fieldBinding.constant();
				if (fieldConstant == Constant.NotAConstant) { // directly use inlined value for constant fields
					if (fieldBinding.canBeSeenBy(getReceiverType(currentScope), this, currentScope)) {
						 // directly use inlined value for constant fields
						boolean isStatic;
						if (!(isStatic = fieldBinding.isStatic())) {
							if ((this.bits & DepthMASK) != 0) {
								ReferenceBinding targetType = currentScope.enclosingSourceType().enclosingTypeAt((this.bits & DepthMASK) >> DepthSHIFT);
								Object[] emulationPath = currentScope.getEmulationPath(targetType, true /*only exact match*/, false/*consider enclosing arg*/);
								codeStream.generateOuterAccess(emulationPath, this, targetType, currentScope);
							} else {
								generateReceiver(codeStream);
							}
						}
						// managing private access							
						if (isStatic) {
							codeStream.getstatic(fieldBinding);
						} else {
							codeStream.getfield(fieldBinding);
						}
					} else {
						// managing private access
						if (!fieldBinding.isStatic()) {
							if ((this.bits & DepthMASK) != 0) {
								// internal error, per construction we should have found it
								// not yet supported
								currentScope.problemReporter().needImplementation(this);
							} else {
								generateReceiver(codeStream);
							}
						} else {
							codeStream.aconst_null();
						}
						codeStream.generateEmulatedReadAccessForField(fieldBinding);
					}
					if (this.genericCast != null) codeStream.checkcast(this.genericCast);		
					codeStream.generateImplicitConversion(this.implicitConversion);
				} else { // directly use the inlined value
					codeStream.generateConstant(fieldConstant, this.implicitConversion);
				}
				break;
			case Binding.LOCAL : // reading a local
				LocalVariableBinding localBinding = (LocalVariableBinding) this.codegenBinding;
				if (!valueRequired)
					break;
				// outer local?
				if ((this.bits & DepthMASK) != 0) {
					// outer local can be reached either through a synthetic arg or a synthetic field
					VariableBinding[] path = currentScope.getEmulationPath(localBinding);
					codeStream.generateOuterAccess(path, this, localBinding, currentScope);
				} else {
					// regular local variable read
					codeStream.load(localBinding);
				}
				codeStream.generateImplicitConversion(this.implicitConversion);
				break;
		}
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}
/*
 * The APIs with an extra argument is used whenever there are two references to the same variable which
 * are optimized in one access: e.g "a = a + 1" optimized into "a++".
 */
public void generateCompoundAssignment(BlockScope currentScope, CodeStream codeStream, MethodBinding writeAccessor, Expression expression, int operator, int assignmentImplicitConversion, boolean valueRequired) {
	switch (this.bits & RestrictiveFlagMASK) {
		case Binding.FIELD : // assigning to a field
			FieldBinding fieldBinding = (FieldBinding) this.codegenBinding;
			if (fieldBinding.isStatic()) {
				if (fieldBinding.canBeSeenBy(getReceiverType(currentScope), this, currentScope)) {
					codeStream.getstatic(fieldBinding);
				} else {
					// used to store the value
					codeStream.generateEmulationForField(fieldBinding);
					codeStream.aconst_null();

					// used to retrieve the actual value
					codeStream.aconst_null();
					codeStream.generateEmulatedReadAccessForField(fieldBinding);
				}
			} else {
				if (fieldBinding.canBeSeenBy(getReceiverType(currentScope), this, currentScope)) {
					if ((this.bits & DepthMASK) != 0) {
						ReferenceBinding targetType = currentScope.enclosingSourceType().enclosingTypeAt((this.bits & DepthMASK) >> DepthSHIFT);
						Object[] emulationPath = currentScope.getEmulationPath(targetType, true /*only exact match*/, false/*consider enclosing arg*/);
						codeStream.generateOuterAccess(emulationPath, this, targetType, currentScope);
					} else {
						generateReceiver(codeStream);
					}
					codeStream.dup();
					codeStream.getfield(fieldBinding);
				} else {
					if ((this.bits & DepthMASK) != 0) {
						// internal error, per construction we should have found it
						// not yet supported
						currentScope.problemReporter().needImplementation(this);
					}
					// used to store the value
					codeStream.generateEmulationForField(fieldBinding);
					generateReceiver(codeStream);

					// used to retrieve the actual value
					codeStream.dup();
					codeStream.generateEmulatedReadAccessForField(fieldBinding);
				}
			}
			break;
		case Binding.LOCAL : // assigning to a local variable (cannot assign to outer local)
			LocalVariableBinding localBinding = (LocalVariableBinding) this.codegenBinding;
			// using incr bytecode if possible
			switch (localBinding.type.id) {
				case T_JavaLangString :
					codeStream.generateStringConcatenationAppend(currentScope, this, expression);
					if (valueRequired) {
						codeStream.dup();
					}
					codeStream.store(localBinding, false);
					return;
				case T_int :
					Constant assignConstant;
					if (((assignConstant = expression.constant) != Constant.NotAConstant) 
							&& (assignConstant.typeID() != TypeIds.T_float) // only for integral types
							&& (assignConstant.typeID() != TypeIds.T_double)) {
						switch (operator) {
							case PLUS :
								int increment  = assignConstant.intValue();
								if (increment != (short) increment) break; // not representable as a 16-bits value
								codeStream.iinc(localBinding.resolvedPosition, increment);
								if (valueRequired) {
									codeStream.load(localBinding);
								}
								return;
							case MINUS :
								increment  = -assignConstant.intValue();
								if (increment != (short) increment) break; // not representable as a 16-bits value
								codeStream.iinc(localBinding.resolvedPosition, increment);
								if (valueRequired) {
									codeStream.load(localBinding);
								}
								return;
						}
					}
				default :
					codeStream.load(localBinding);
			}
	}
	// perform the actual compound operation
	int operationTypeID;
	switch(operationTypeID = (this.implicitConversion & IMPLICIT_CONVERSION_MASK) >> 4) {
		case T_JavaLangString :
		case T_JavaLangObject :
		case T_undefined :
			codeStream.generateStringConcatenationAppend(currentScope, null, expression);
			break;
		default :
			// promote the array reference to the suitable operation type
			codeStream.generateImplicitConversion(this.implicitConversion);
			// generate the increment value (will by itself  be promoted to the operation value)
			if (expression == IntLiteral.One){ // prefix operation
				codeStream.generateConstant(expression.constant, this.implicitConversion);			
			} else {
				expression.generateCode(currentScope, codeStream, true);
			}		
			// perform the operation
			codeStream.sendOperator(operator, operationTypeID);
			// cast the value back to the array reference type
			codeStream.generateImplicitConversion(assignmentImplicitConversion);
	}
	// store the result back into the variable
	switch (this.bits & RestrictiveFlagMASK) {
		case Binding.FIELD : // assigning to a field
			FieldBinding fieldBinding = (FieldBinding) this.codegenBinding;
			if (fieldBinding.canBeSeenBy(getReceiverType(currentScope), this, currentScope)) {
				fieldStore(codeStream, fieldBinding, writeAccessor, valueRequired);
			} else {
				// current stack is:
				// field receiver value
				if (valueRequired) {
					if ((fieldBinding.type == TypeBinding.LONG) || (fieldBinding.type == TypeBinding.DOUBLE)) {
						codeStream.dup2_x2();
					} else {
						codeStream.dup_x2();
					}
				}
				// current stack is:
				// value field receiver value				
				codeStream.generateEmulatedWriteAccessForField(fieldBinding);
			}
			return;
		case Binding.LOCAL : // assigning to a local variable
			LocalVariableBinding localBinding = (LocalVariableBinding) this.codegenBinding;
			if (valueRequired) {
				if ((localBinding.type == TypeBinding.LONG) || (localBinding.type == TypeBinding.DOUBLE)) {
					codeStream.dup2();
				} else {
					codeStream.dup();
				}
			}
			codeStream.store(localBinding, false);
	}
}
public void generatePostIncrement(BlockScope currentScope, CodeStream codeStream, CompoundAssignment postIncrement, boolean valueRequired) {
	switch (this.bits & RestrictiveFlagMASK) {
		case Binding.FIELD : // assigning to a field
			FieldBinding fieldBinding = (FieldBinding) this.codegenBinding;
			if (fieldBinding.canBeSeenBy(getReceiverType(currentScope), this, currentScope)) {
				if (fieldBinding.isStatic()) {
					codeStream.getstatic(fieldBinding);
				} else {
					if ((this.bits & DepthMASK) != 0) {
						ReferenceBinding targetType = currentScope.enclosingSourceType().enclosingTypeAt((this.bits & DepthMASK) >> DepthSHIFT);
						Object[] emulationPath = currentScope.getEmulationPath(targetType, true /*only exact match*/, false/*consider enclosing arg*/);
						codeStream.generateOuterAccess(emulationPath, this, targetType, currentScope);
					} else {
						generateReceiver(codeStream);
					}
					codeStream.dup();
					codeStream.getfield(fieldBinding);
				}
				if (valueRequired) {
					if (fieldBinding.isStatic()) {
						if ((fieldBinding.type == TypeBinding.LONG) || (fieldBinding.type == TypeBinding.DOUBLE)) {
							codeStream.dup2();
						} else {
							codeStream.dup();
						}
					} else { // Stack:  [owner][old field value]  ---> [old field value][owner][old field value]
						if ((fieldBinding.type == TypeBinding.LONG) || (fieldBinding.type == TypeBinding.DOUBLE)) {
							codeStream.dup2_x1();
						} else {
							codeStream.dup_x1();
						}
					}
				}
				codeStream.generateConstant(postIncrement.expression.constant, this.implicitConversion);
				codeStream.sendOperator(postIncrement.operator, fieldBinding.type.id);
				codeStream.generateImplicitConversion(postIncrement.preAssignImplicitConversion);
				fieldStore(codeStream, fieldBinding, null, false);
			} else {
				if (fieldBinding.isStatic()) {
					codeStream.aconst_null();
				} else {
					if ((this.bits & DepthMASK) != 0) {
						// internal error, per construction we should have found it
						// not yet supported
						currentScope.problemReporter().needImplementation(this);
					} else {
						generateReceiver(codeStream);
					}
				}
				codeStream.generateEmulatedReadAccessForField(fieldBinding);
				if (valueRequired) {
					if ((fieldBinding.type == TypeBinding.LONG) || (fieldBinding.type == TypeBinding.DOUBLE)) {
						codeStream.dup2();
					} else {
						codeStream.dup();
					}
				}
				codeStream.generateEmulationForField(fieldBinding);
				if ((fieldBinding.type == TypeBinding.LONG) || (fieldBinding.type == TypeBinding.DOUBLE)) {
					codeStream.dup_x2();
					codeStream.pop();
					if (fieldBinding.isStatic()) {
						codeStream.aconst_null();
					} else {
						generateReceiver(codeStream);
					}
					codeStream.dup_x2();
					codeStream.pop();					
				} else {
					codeStream.dup_x1();
					codeStream.pop();
					if (fieldBinding.isStatic()) {
						codeStream.aconst_null();
					} else {
						generateReceiver(codeStream);
					}
					codeStream.dup_x1();
					codeStream.pop();					
				}
				codeStream.generateConstant(postIncrement.expression.constant, this.implicitConversion);
				codeStream.sendOperator(postIncrement.operator, fieldBinding.type.id);
				codeStream.generateImplicitConversion(postIncrement.preAssignImplicitConversion);
				codeStream.generateEmulatedWriteAccessForField(fieldBinding);
			}
			return;
		case Binding.LOCAL : // assigning to a local variable
			LocalVariableBinding localBinding = (LocalVariableBinding) this.codegenBinding;
			// using incr bytecode if possible
			if (localBinding.type == TypeBinding.INT) {
				if (valueRequired) {
					codeStream.load(localBinding);
				}
				if (postIncrement.operator == PLUS) {
					codeStream.iinc(localBinding.resolvedPosition, 1);
				} else {
					codeStream.iinc(localBinding.resolvedPosition, -1);
				}
			} else {
				codeStream.load(localBinding);
				if (valueRequired){
					if ((localBinding.type == TypeBinding.LONG) || (localBinding.type == TypeBinding.DOUBLE)) {
						codeStream.dup2();
					} else {
						codeStream.dup();
					}
				}
				codeStream.generateConstant(postIncrement.expression.constant, this.implicitConversion);
				codeStream.sendOperator(postIncrement.operator, localBinding.type.id);
				codeStream.generateImplicitConversion(postIncrement.preAssignImplicitConversion);

				codeStream.store(localBinding, false);
			}
	}
}
public void generateReceiver(CodeStream codeStream) {
	codeStream.aload_0();
	if (this.delegateThis != null) {
		codeStream.getfield(this.delegateThis); // delegated field access
	}
}
/**
 * Check and/or redirect the field access to the delegate receiver if any
 */
public TypeBinding getReceiverType(BlockScope currentScope) {
	Scope scope = currentScope.parent;
	while (true) {
			switch (scope.kind) {
				case Scope.CLASS_SCOPE :
					return ((ClassScope) scope).referenceContext.binding;
				default:
					scope = scope.parent;
			}
	}
}
public void manageSyntheticAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo, boolean isReadAccess) {

	if (this.delegateThis == null) {
		super.manageSyntheticAccessIfNecessary(currentScope, flowInfo, isReadAccess);
		return;
	}

	if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) != 0) return;
	//If inlinable field, forget the access emulation, the code gen will directly target it
	if (this.constant != Constant.NotAConstant)
		return;	
	// if field from parameterized type got found, use the original field at codegen time
	if (this.binding instanceof ParameterizedFieldBinding) {
	    ParameterizedFieldBinding parameterizedField = (ParameterizedFieldBinding) this.binding;
	    this.codegenBinding = parameterizedField.originalField;
	    FieldBinding fieldCodegenBinding = (FieldBinding)this.codegenBinding;
	    // extra cast needed if field type was type variable
	    if ((fieldCodegenBinding.type.tagBits & TagBits.HasTypeVariable) != 0) {
	        this.genericCast = fieldCodegenBinding.type.genericCast(currentScope.boxing(parameterizedField.type)); // runtimeType could be base type in boxing case
	    }		    
	}		
	if ((this.bits & Binding.FIELD) != 0) {
		FieldBinding fieldBinding = (FieldBinding) this.binding;
		
		// if the binding declaring class is not visible, need special action
		// for runtime compatibility on 1.2 VMs : change the declaring class of the binding
		// NOTE: from target 1.2 on, field's declaring class is touched if any different from receiver type
		// and not from Object or implicit static field access.	
		if (fieldBinding.declaringClass != this.delegateThis.type
				&& fieldBinding.declaringClass != null // array.length
				&& fieldBinding.constant() == Constant.NotAConstant) {
			CompilerOptions options = currentScope.compilerOptions();
			if ((options.targetJDK >= ClassFileConstants.JDK1_2
					&& (options.complianceLevel >= ClassFileConstants.JDK1_4 || !fieldBinding.isStatic())
					&& fieldBinding.declaringClass.id != T_JavaLangObject) // no change for Object fields
				|| !fieldBinding.declaringClass.canBeSeenBy(currentScope)) {
	
				this.codegenBinding = 
				    currentScope.enclosingSourceType().getUpdatedFieldBinding(
					       (FieldBinding)this.codegenBinding, 
					        (ReferenceBinding)this.delegateThis.type.erasure());
			}
		}				
	}
}
/**
 * Normal field binding did not work, try to bind to a field of the delegate receiver.
 */
public TypeBinding reportError(BlockScope scope) {

	this.constant = Constant.NotAConstant;
	if (this.binding instanceof ProblemFieldBinding && ((ProblemFieldBinding) this.binding).problemId() == NotFound){
		if (this.evaluationContext.declaringTypeName != null) {
			this.delegateThis = scope.getField(scope.enclosingSourceType(), DELEGATE_THIS, this);
			if (this.delegateThis != null){  // if not found then internal error, field should have been found
				// will not support innerclass emulation inside delegate
				this.codegenBinding = this.binding = scope.getField(this.delegateThis.type, this.token, this);
				if (!this.binding.isValidBinding()) {
					return super.reportError(scope);
				}
				return checkFieldAccess(scope);
			}
		}
	}
	if (this.binding instanceof ProblemBinding && ((ProblemBinding) this.binding).problemId() == NotFound){
		if (this.evaluationContext.declaringTypeName != null) {
			this.delegateThis = scope.getField(scope.enclosingSourceType(), DELEGATE_THIS, this);
			if (this.delegateThis != null){  // if not found then internal error, field should have been found
				// will not support innerclass emulation inside delegate
				FieldBinding fieldBinding = scope.getField(this.delegateThis.type, this.token, this);
				if (!fieldBinding.isValidBinding()) {
					if (((ProblemFieldBinding) fieldBinding).problemId() == NotVisible) {
						// manage the access to a private field of the enclosing type
						CodeSnippetScope localScope = new CodeSnippetScope(scope);
						this.codegenBinding = this.binding = localScope.getFieldForCodeSnippet(this.delegateThis.type, this.token, this);
						return checkFieldAccess(scope);						
					} else {
						return super.reportError(scope);
					}
				}
				this.codegenBinding = this.binding = fieldBinding;
				return checkFieldAccess(scope);
			}
		}
	}
	return super.reportError(scope);
}
}
