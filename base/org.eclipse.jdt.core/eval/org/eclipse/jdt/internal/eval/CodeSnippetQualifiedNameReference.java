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
import org.eclipse.jdt.internal.compiler.ast.CompoundAssignment;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemFieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;

public class CodeSnippetQualifiedNameReference extends QualifiedNameReference implements EvaluationConstants, ProblemReasons {

	EvaluationContext evaluationContext;
	FieldBinding delegateThis;
/**
 * CodeSnippetQualifiedNameReference constructor comment.
 * @param sources char[][]
 * @param sourceStart int
 * @param sourceEnd int
 */
public CodeSnippetQualifiedNameReference(char[][] sources, long[] positions, int sourceStart, int sourceEnd, EvaluationContext evaluationContext) {
	super(sources, positions, sourceStart, sourceEnd);
	this.evaluationContext = evaluationContext;	
}
/**
 * Check and/or redirect the field access to the delegate receiver if any
 */
public TypeBinding checkFieldAccess(BlockScope scope) {
	// check for forward references
	this.bits &= ~RestrictiveFlagMASK; // clear bits
	this.bits |= Binding.FIELD;
	return getOtherFieldBindings(scope);
}
public void generateAssignment(BlockScope currentScope, CodeStream codeStream, Assignment assignment, boolean valueRequired) {

	FieldBinding lastFieldBinding = generateReadSequence(currentScope, codeStream);
	if (lastFieldBinding.canBeSeenBy(getReceiverType(currentScope), this, currentScope)) {
		// the last field access is a write access
		assignment.expression.generateCode(currentScope, codeStream, true);
		fieldStore(codeStream, lastFieldBinding, null, valueRequired);
	} else {
		codeStream.generateEmulationForField(lastFieldBinding);
		codeStream.swap();
		assignment.expression.generateCode(currentScope, codeStream, true);
		if (valueRequired) {
			if ((lastFieldBinding.type == TypeBinding.LONG) || (lastFieldBinding.type == TypeBinding.DOUBLE)) {
				codeStream.dup2_x2();
			} else {
				codeStream.dup_x2();
			}
		}
		codeStream.generateEmulatedWriteAccessForField(lastFieldBinding);
	}
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
		if (valueRequired) {
			if (lastFieldBinding.declaringClass == null) { // array length
				codeStream.arraylength();
				codeStream.generateImplicitConversion(this.implicitConversion);
			} else {
				Constant fieldConstant = lastFieldBinding.constant();
				if (fieldConstant != Constant.NotAConstant) {
					if (!lastFieldBinding.isStatic()){
						codeStream.invokeObjectGetClass();
						codeStream.pop();
					}
					// inline the last field constant
					codeStream.generateConstant(fieldConstant, this.implicitConversion);
				} else {	
					if (lastFieldBinding.canBeSeenBy(getReceiverType(currentScope), this, currentScope)) {
						if (lastFieldBinding.isStatic()) {
							codeStream.getstatic(lastFieldBinding);
						} else {
							codeStream.getfield(lastFieldBinding);
						}
					} else {
						codeStream.generateEmulatedReadAccessForField(lastFieldBinding);
					}	
					codeStream.generateImplicitConversion(this.implicitConversion);
				}
			}
		} else {
			if (lastFieldBinding != null && !lastFieldBinding.isStatic()){
				codeStream.invokeObjectGetClass(); // perform null check
				codeStream.pop();
			}
		}		
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}
public void generateCompoundAssignment(BlockScope currentScope, CodeStream codeStream, Expression expression, int operator, int assignmentImplicitConversion, boolean valueRequired) {
	
	FieldBinding lastFieldBinding = generateReadSequence(currentScope, codeStream);
	if (lastFieldBinding.canBeSeenBy(getReceiverType(currentScope), this, currentScope)) {
		if (lastFieldBinding.isStatic()){
			codeStream.getstatic(lastFieldBinding);
		} else {
			codeStream.dup();
			codeStream.getfield(lastFieldBinding);
		}
		// the last field access is a write access
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
		// actual assignment
		fieldStore(codeStream, lastFieldBinding, null, valueRequired);
	} else {
		if (lastFieldBinding.isStatic()){
			codeStream.generateEmulationForField(lastFieldBinding);
			codeStream.swap();
			codeStream.aconst_null();
			codeStream.swap();

			codeStream.generateEmulatedReadAccessForField(lastFieldBinding);
		} else {
			codeStream.generateEmulationForField(lastFieldBinding);
			codeStream.swap();
			codeStream.dup();

			codeStream.generateEmulatedReadAccessForField(lastFieldBinding);
		}
		// the last field access is a write access
		// perform the actual compound operation
		int operationTypeID;
		if ((operationTypeID = (this.implicitConversion & IMPLICIT_CONVERSION_MASK) >> 4) == T_JavaLangString) {
			codeStream.generateStringConcatenationAppend(currentScope, null, expression);
		} else {
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
		// actual assignment

		// current stack is:
		// field receiver value
		if (valueRequired) {
			if ((lastFieldBinding.type == TypeBinding.LONG) || (lastFieldBinding.type == TypeBinding.DOUBLE)) {
				codeStream.dup2_x2();
			} else {
				codeStream.dup_x2();
			}
		}
		// current stack is:
		// value field receiver value				
		codeStream.generateEmulatedWriteAccessForField(lastFieldBinding);
	}
}
public void generatePostIncrement(BlockScope currentScope, CodeStream codeStream, CompoundAssignment postIncrement, boolean valueRequired) {

    FieldBinding lastFieldBinding = generateReadSequence(currentScope, codeStream);
	if (lastFieldBinding.canBeSeenBy(getReceiverType(currentScope), this, currentScope)) {
		if (lastFieldBinding.isStatic()){
			codeStream.getstatic(lastFieldBinding);
		} else {
			codeStream.dup();
			codeStream.getfield(lastFieldBinding);
		}	
		// duplicate the old field value
		if (valueRequired) {
			if (lastFieldBinding.isStatic()) {
				if ((lastFieldBinding.type == TypeBinding.LONG) || (lastFieldBinding.type == TypeBinding.DOUBLE)) {
					codeStream.dup2();
				} else {
					codeStream.dup();
				}
			} else { // Stack:  [owner][old field value]  ---> [old field value][owner][old field value]
				if ((lastFieldBinding.type == TypeBinding.LONG) || (lastFieldBinding.type == TypeBinding.DOUBLE)) {
					codeStream.dup2_x1();
				} else {
					codeStream.dup_x1();
				}
			}
		}
		codeStream.generateConstant(postIncrement.expression.constant, this.implicitConversion);
		codeStream.sendOperator(postIncrement.operator, lastFieldBinding.type.id);
		codeStream.generateImplicitConversion(postIncrement.preAssignImplicitConversion);
		
		fieldStore(codeStream, lastFieldBinding, null, false);
	} else {
		codeStream.generateEmulatedReadAccessForField(lastFieldBinding);
		if (valueRequired) {
			if ((lastFieldBinding.type == TypeBinding.LONG) || (lastFieldBinding.type == TypeBinding.DOUBLE)) {
				codeStream.dup2();
			} else {
				codeStream.dup();
			}
		}
		codeStream.generateEmulationForField(lastFieldBinding);
		if ((lastFieldBinding.type == TypeBinding.LONG) || (lastFieldBinding.type == TypeBinding.DOUBLE)) {
			codeStream.dup_x2();
			codeStream.pop();
			if (lastFieldBinding.isStatic()) {
				codeStream.aconst_null();
			} else {
				generateReadSequence(currentScope, codeStream);
			}
			codeStream.dup_x2();
			codeStream.pop();					
		} else {
			codeStream.dup_x1();
			codeStream.pop();
			if (lastFieldBinding.isStatic()) {
				codeStream.aconst_null();
			} else {
				generateReadSequence(currentScope, codeStream);
			}
			codeStream.dup_x1();
			codeStream.pop();					
		}
		codeStream.generateConstant(postIncrement.expression.constant, this.implicitConversion);
		codeStream.sendOperator(postIncrement.operator, lastFieldBinding.type.id);
		codeStream.generateImplicitConversion(postIncrement.preAssignImplicitConversion);
		codeStream.generateEmulatedWriteAccessForField(lastFieldBinding);
	}
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
	
	switch (this.bits & RestrictiveFlagMASK) {
		case Binding.FIELD :
			lastFieldBinding = (FieldBinding) this.codegenBinding;
			lastGenericCast = this.genericCast;
			// if first field is actually constant, we can inline it
			if (lastFieldBinding.constant() != Constant.NotAConstant) {
				break;
			}
			if (needValue) {
				if (lastFieldBinding.canBeSeenBy(getReceiverType(currentScope), this, currentScope)) {
					if (!lastFieldBinding.isStatic()) {
						if ((this.bits & DepthMASK) != 0) {
							ReferenceBinding targetType = currentScope.enclosingSourceType().enclosingTypeAt((this.bits & DepthMASK) >> DepthSHIFT);
							Object[] emulationPath = currentScope.getEmulationPath(targetType, true /*only exact match*/, false/*consider enclosing arg*/);
							codeStream.generateOuterAccess(emulationPath, this, targetType, currentScope);
						} else {
							generateReceiver(codeStream);
						}
					}
				} else {
					if (!lastFieldBinding.isStatic()) {
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
				}				
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
				if ((bits & DepthMASK) != 0) {
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
	if (this.otherCodegenBindings != null) {
		for (int i = 0; i < otherBindingsCount; i++) {
			FieldBinding nextField = this.otherCodegenBindings[i];
			TypeBinding nextGenericCast = this.otherGenericCasts == null ? null : this.otherGenericCasts[i];
			if (lastFieldBinding != null) {
				needValue = !nextField.isStatic();
				if (needValue) {
					if (lastFieldBinding.canBeSeenBy(getReceiverType(currentScope), this, currentScope)) {
						Constant fieldConstant = lastFieldBinding.constant();
						if (fieldConstant != Constant.NotAConstant) {
							if (lastFieldBinding != this.codegenBinding && !lastFieldBinding.isStatic()) {
								codeStream.invokeObjectGetClass(); // perform null check
								codeStream.pop();
							}
							codeStream.generateConstant(fieldConstant, 0);
						} else if (lastFieldBinding.isStatic()) {
							codeStream.getstatic(lastFieldBinding);
						} else {
							codeStream.getfield(lastFieldBinding);
						}
					} else {
						codeStream.generateEmulatedReadAccessForField(lastFieldBinding);
					}
					if (lastGenericCast != null) codeStream.checkcast(lastGenericCast);
				} else {
					if (this.codegenBinding != lastFieldBinding && !lastFieldBinding.isStatic()){
						codeStream.invokeObjectGetClass(); // perform null check
						codeStream.pop();
					}						
				}
			}
			lastFieldBinding = nextField;
			lastGenericCast = nextGenericCast;
			if (lastFieldBinding != null && !lastFieldBinding.canBeSeenBy(getReceiverType(currentScope), this, currentScope)) {
				if (lastFieldBinding.isStatic()) {
					codeStream.aconst_null();
				}
			}
		}			
	}
	return lastFieldBinding;
}

public void generateReceiver(CodeStream codeStream) {
	codeStream.aload_0();
	if (this.delegateThis != null) {
		codeStream.getfield(this.delegateThis); // delegated field access
	}
}
public TypeBinding getOtherFieldBindings(BlockScope scope) {
	// At this point restrictiveFlag may ONLY have two potential value : FIELD LOCAL (i.e cast <<(VariableBinding) binding>> is valid)

	int length = this.tokens.length;
	if ((this.bits & Binding.FIELD) != 0) {
		if (!((FieldBinding) this.binding).isStatic()) { //must check for the static status....
			if (this.indexOfFirstFieldBinding == 1) {
				//the field is the first token of the qualified reference....
				if (scope.methodScope().isStatic) {
					scope.problemReporter().staticFieldAccessToNonStaticVariable(this, (FieldBinding) this.binding);
					return null;
				}
			} else { //accessing to a field using a type as "receiver" is allowed only with static field	
				scope.problemReporter().staticFieldAccessToNonStaticVariable(this, (FieldBinding) this.binding);
				return null;
			}
		}
		// only last field is actually a write access if any
		if (isFieldUseDeprecated((FieldBinding) this.binding, scope, (this.bits & IsStrictlyAssigned) !=0 && this.indexOfFirstFieldBinding == length)) {
			scope.problemReporter().deprecatedField((FieldBinding) this.binding, this);
		}
	}

	TypeBinding type = ((VariableBinding) this.binding).type;
	int index = this.indexOfFirstFieldBinding;
	if (index == length) { //	restrictiveFlag == FIELD
		this.constant = ((FieldBinding) this.binding).constant();
		return type;
	}

	// allocation of the fieldBindings array	and its respective constants
	int otherBindingsLength = length - index;
	this.otherCodegenBindings = this.otherBindings = new FieldBinding[otherBindingsLength];
	
	// fill the first constant (the one of the binding)
	this.constant =((VariableBinding) this.binding).constant();

	// iteration on each field	
	while (index < length) {
		char[] token = this.tokens[index];
		if (type == null) return null; // could not resolve type prior to this point
		FieldBinding field = scope.getField(type, token, this);
		int place = index - this.indexOfFirstFieldBinding;
		this.otherBindings[place] = field;
		if (!field.isValidBinding()) {
			// try to retrieve the field as private field
			CodeSnippetScope localScope = new CodeSnippetScope(scope);
			if (this.delegateThis == null) {
				if (this.evaluationContext.declaringTypeName != null) {
					this.delegateThis = scope.getField(scope.enclosingSourceType(), DELEGATE_THIS, this);
					if (this.delegateThis == null){  // if not found then internal error, field should have been found
						return super.reportError(scope);
					}
				} else {
					this.constant = Constant.NotAConstant; //don't fill other constants slots...
					scope.problemReporter().invalidField(this, field, index, type);
					return null;
				}
			}
			field = localScope.getFieldForCodeSnippet(this.delegateThis.type, token, this);
			this.otherBindings[place] = field;
		}
		if (field.isValidBinding()) {
			// only last field is actually a write access if any
			if (isFieldUseDeprecated(field, scope, (this.bits & IsStrictlyAssigned) !=0 && index+1 == length)) {
				scope.problemReporter().deprecatedField(field, this);
			}
			// constant propagation can only be performed as long as the previous one is a constant too.
			if (this.constant != Constant.NotAConstant){
				this.constant = field.constant();
			}
			type = field.type;
			index++;
		} else {
			this.constant = Constant.NotAConstant; //don't fill other constants slots...
			scope.problemReporter().invalidField(this, field, index, type);
			return null;
		}
	}
	return (this.otherBindings[otherBindingsLength - 1]).type;
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
	/**
	 * index is <0 to denote write access emulation
	 */		
	public void manageSyntheticAccessIfNecessary(
		BlockScope currentScope,
		FieldBinding fieldBinding,
		TypeBinding lastReceiverType,
		int index,
		FlowInfo flowInfo) {

		if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) != 0) return;
	
		// if the binding declaring class is not visible, need special action
		// for runtime compatibility on 1.2 VMs : change the declaring class of the binding
		// NOTE: from target 1.2 on, field's declaring class is touched if any different from receiver type
		boolean useDelegate;
		if (index < 0) { // write-access?
		    useDelegate = fieldBinding == this.binding && this.delegateThis != null;
		} else {
			useDelegate = index == 0 && this.delegateThis != null;
		}
		
		if (useDelegate) {
			lastReceiverType = this.delegateThis.type;
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
					&& (options.complianceLevel >= ClassFileConstants.JDK1_4 || (index < 0 ? fieldBinding != binding : index > 0) || this.indexOfFirstFieldBinding > 1 || !fieldBinding.isStatic())
					&& fieldBinding.declaringClass.id != T_JavaLangObject) // no change for Object fields
				|| !(useDelegate
						? new CodeSnippetScope(currentScope).canBeSeenByForCodeSnippet(fieldBinding.declaringClass, (ReferenceBinding) this.delegateThis.type)
						: fieldBinding.declaringClass.canBeSeenBy(currentScope))) {
	
			    if (index < 0) { // write-access?
					if (fieldBinding == this.binding){
						this.codegenBinding = currentScope.enclosingSourceType().getUpdatedFieldBinding(fieldBinding, (ReferenceBinding)lastReceiverType.erasure());
					} else {
						if (this.otherCodegenBindings == this.otherBindings){
							int l = this.otherBindings.length;
							System.arraycopy(this.otherBindings, 0, this.otherCodegenBindings = new FieldBinding[l], 0, l);
						}
						this.otherCodegenBindings[this.otherCodegenBindings.length-1] = currentScope.enclosingSourceType().getUpdatedFieldBinding(fieldBinding, (ReferenceBinding)lastReceiverType.erasure());
					}
			    } if (index == 0){
					this.codegenBinding = currentScope.enclosingSourceType().getUpdatedFieldBinding(fieldBinding, (ReferenceBinding)lastReceiverType.erasure());
				} else {
					if (this.otherCodegenBindings == this.otherBindings){
						int l = this.otherBindings.length;
						System.arraycopy(this.otherBindings, 0, this.otherCodegenBindings = new FieldBinding[l], 0, l);
					}
					this.otherCodegenBindings[index-1] = currentScope.enclosingSourceType().getUpdatedFieldBinding(fieldBinding, (ReferenceBinding)lastReceiverType.erasure());
				}
			}		
		}
	}
/**
 * Normal field binding did not work, try to bind to a field of the delegate receiver.
 */
public TypeBinding reportError(BlockScope scope) {

	if (this.evaluationContext.declaringTypeName != null) {
		this.delegateThis = scope.getField(scope.enclosingSourceType(), DELEGATE_THIS, this);
		if (this.delegateThis == null){  // if not found then internal error, field should have been found
			return super.reportError(scope);
		}
	} else {
		return super.reportError(scope);
	}

	if ((this.binding instanceof ProblemFieldBinding && ((ProblemFieldBinding) this.binding).problemId() == NotFound)
		|| (this.binding instanceof ProblemBinding && ((ProblemBinding) this.binding).problemId() == NotFound)){
		// will not support innerclass emulation inside delegate
		FieldBinding fieldBinding = scope.getField(this.delegateThis.type, this.tokens[0], this);
		if (!fieldBinding.isValidBinding()) {
			if (((ProblemFieldBinding) fieldBinding).problemId() == NotVisible) {
				// manage the access to a private field of the enclosing type
				CodeSnippetScope localScope = new CodeSnippetScope(scope);
				this.codegenBinding = this.binding = localScope.getFieldForCodeSnippet(this.delegateThis.type, this.tokens[0], this);
				if (this.binding.isValidBinding()) {
					return checkFieldAccess(scope);						
				} else {
					return super.reportError(scope);
				}
			} else {
				return super.reportError(scope);
			}
		}
		this.codegenBinding = this.binding = fieldBinding;
		return checkFieldAccess(scope);
	}

	TypeBinding result;
	if (this.binding instanceof ProblemFieldBinding
		&& ((ProblemFieldBinding) this.binding).problemId() == NotVisible) {
		result = resolveTypeVisibility(scope);
		if (result == null) {
			return super.reportError(scope);
		}
		if (result.isValidBinding()) {
			return result;
		}
	}

	return super.reportError(scope);
}
public TypeBinding resolveTypeVisibility(BlockScope scope) {
	// field and/or local are done before type lookups

	// the only available value for the restrictiveFlag BEFORE
	// the TC is Flag_Type Flag_LocalField and Flag_TypeLocalField 

	CodeSnippetScope localScope = new CodeSnippetScope(scope);
	if ((this.codegenBinding = this.binding = localScope.getBinding(this.tokens, this.bits & RestrictiveFlagMASK, this, (ReferenceBinding) this.delegateThis.type)).isValidBinding()) {
		this.bits &= ~RestrictiveFlagMASK; // clear bits
		this.bits |= Binding.FIELD;
		return getOtherFieldBindings(scope);
	}
	//========error cases===============
	return super.reportError(scope);
}
}
