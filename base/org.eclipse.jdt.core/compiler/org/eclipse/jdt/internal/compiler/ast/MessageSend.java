/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Nick Teryaev - fix for bug (https://bugs.eclipse.org/bugs/show_bug.cgi?id=40752)
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
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.RawTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

public class MessageSend extends Expression implements InvocationSite {
    
	public Expression receiver;
	public char[] selector;
	public Expression[] arguments;
	public MethodBinding binding;							// exact binding resulting from lookup
	public MethodBinding codegenBinding;		// actual binding used for code generation (if no synthetic accessor)
	public MethodBinding syntheticAccessor;						// synthetic accessor for inner-emulation
	public TypeBinding expectedType;					// for generic method invocation (return type inference)

	public long nameSourcePosition ; //(start<<32)+end

	public TypeBinding actualReceiverType;
	public TypeBinding receiverGenericCast; // extra reference type cast to perform on generic receiver
	public TypeBinding valueCast; // extra reference type cast to perform on method returned value
	public TypeReference[] typeArguments;
	public TypeBinding[] genericTypeArguments;
	
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	boolean nonStatic = !this.binding.isStatic();
	flowInfo = this.receiver.analyseCode(currentScope, flowContext, flowInfo, nonStatic).unconditionalInits();
	if (nonStatic) {
		this.receiver.checkNPE(currentScope, flowContext, flowInfo);
	}

	if (this.arguments != null) {
		int length = this.arguments.length;
		for (int i = 0; i < length; i++) {
			flowInfo = this.arguments[i].analyseCode(currentScope, flowContext, flowInfo).unconditionalInits();
		}
	}
	ReferenceBinding[] thrownExceptions;
	if ((thrownExceptions = this.binding.thrownExceptions) != Binding.NO_EXCEPTIONS) {
		// must verify that exceptions potentially thrown by this expression are caught in the method
		flowContext.checkExceptionHandlers(thrownExceptions, this, flowInfo.copy(), currentScope);
		// TODO (maxime) the copy above is needed because of a side effect into 
		//               checkExceptionHandlers; consider protecting there instead of here;
		//               NullReferenceTest#test0510
	}
	manageSyntheticAccessIfNecessary(currentScope, flowInfo);	
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
		MethodBinding originalBinding = this.binding.original();
		TypeBinding originalType = originalBinding.returnType;
	    // extra cast needed if method return type is type variable
		if (originalType.leafComponentType().isTypeVariable()) {
	    	TypeBinding targetType = (!compileTimeType.isBaseType() && runtimeTimeType.isBaseType()) 
	    		? compileTimeType  // unboxing: checkcast before conversion
	    		: runtimeTimeType;
	        this.valueCast = originalType.genericCast(targetType);
		} 	else if (this.actualReceiverType.isArrayType() 
						&& runtimeTimeType.id != TypeIds.T_JavaLangObject
						&& this.binding.parameters == Binding.NO_PARAMETERS 
						&& scope.compilerOptions().complianceLevel >= ClassFileConstants.JDK1_5 
						&& CharOperation.equals(this.binding.selector, TypeConstants.CLONE)) {
					// from 1.5 compliant mode on, array#clone() resolves to array type, but codegen to #clone()Object - thus require extra inserted cast
			this.valueCast = runtimeTimeType;			
		}
        if (this.valueCast instanceof ReferenceBinding) {
			ReferenceBinding referenceCast = (ReferenceBinding) this.valueCast;
			if (!referenceCast.canBeSeenBy(scope)) {
	        	scope.problemReporter().invalidType(this, 
	        			new ProblemReferenceBinding(
							CharOperation.splitOn('.', referenceCast.shortReadableName()),
							referenceCast,
							ProblemReasons.NotVisible));
			}
        }		
	}
	super.computeConversion(scope, runtimeTimeType, compileTimeType);
}

/**
 * MessageSend code generation
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 * @param valueRequired boolean
 */ 
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {

	int pc = codeStream.position;

	// generate receiver/enclosing instance access
	boolean isStatic = this.codegenBinding.isStatic();
	if (isStatic) {
		this.receiver.generateCode(currentScope, codeStream, false);
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	} else if ((this.bits & ASTNode.DepthMASK) != 0 && this.receiver.isImplicitThis()) { // outer access ?
		// outer method can be reached through emulation if implicit access
		ReferenceBinding targetType = currentScope.enclosingSourceType().enclosingTypeAt((this.bits & ASTNode.DepthMASK) >> ASTNode.DepthSHIFT);		
		Object[] path = currentScope.getEmulationPath(targetType, true /*only exact match*/, false/*consider enclosing arg*/);
		codeStream.generateOuterAccess(path, this, targetType, currentScope);
	} else {
		this.receiver.generateCode(currentScope, codeStream, true);
		if (this.receiverGenericCast != null) 
			codeStream.checkcast(this.receiverGenericCast);
		codeStream.recordPositionsFrom(pc, this.sourceStart);
		
	}
	// generate arguments
	generateArguments(this.binding, this.arguments, currentScope, codeStream);
	// actual message invocation
	if (this.syntheticAccessor == null){
		if (isStatic){
			codeStream.invokestatic(this.codegenBinding);
		} else {
			if( (this.receiver.isSuper()) || this.codegenBinding.isPrivate()){
				codeStream.invokespecial(this.codegenBinding);
			} else {
				if (this.codegenBinding.declaringClass.isInterface()) { // interface or annotation type
					codeStream.invokeinterface(this.codegenBinding);
				} else {
					codeStream.invokevirtual(this.codegenBinding);
				}
			}
		}
	} else {
		codeStream.invokestatic(this.syntheticAccessor);
	}
	// required cast must occur even if no value is required
	if (this.valueCast != null) codeStream.checkcast(this.valueCast);
	if (valueRequired){
		// implicit conversion if necessary
		codeStream.generateImplicitConversion(this.implicitConversion);
	} else {
		boolean isUnboxing = (this.implicitConversion & TypeIds.UNBOXING) != 0;
		// conversion only generated if unboxing
		if (isUnboxing) codeStream.generateImplicitConversion(this.implicitConversion);
		switch (isUnboxing ? postConversionType(currentScope).id : this.codegenBinding.returnType.id) {
			case T_long :
			case T_double :
				codeStream.pop2();
				break;
			case T_void :
				break;
			default :
				codeStream.pop();
		}
	}
	codeStream.recordPositionsFrom(pc, (int)(this.nameSourcePosition >>> 32)); // highlight selector
}

/**
 * @see org.eclipse.jdt.internal.compiler.lookup.InvocationSite#genericTypeArguments()
 */
public TypeBinding[] genericTypeArguments() {
	return this.genericTypeArguments;
}	

public boolean isSuperAccess() {	
	return this.receiver.isSuper();
}
public boolean isTypeAccess() {	
	return this.receiver != null && this.receiver.isTypeReference();
}
public void manageSyntheticAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo){

	if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) != 0)	return;

	// if method from parameterized type got found, use the original method at codegen time
	this.codegenBinding = this.binding.original();
	if (this.binding.isPrivate()){

		// depth is set for both implicit and explicit access (see MethodBinding#canBeSeenBy)		
		if (currentScope.enclosingSourceType() != this.codegenBinding.declaringClass){
		
			this.syntheticAccessor = ((SourceTypeBinding)this.codegenBinding.declaringClass).addSyntheticMethod(this.codegenBinding, isSuperAccess());
			currentScope.problemReporter().needToEmulateMethodAccess(this.codegenBinding, this);
			return;
		}

	} else if (this.receiver instanceof QualifiedSuperReference){ // qualified super

		// qualified super need emulation always
		SourceTypeBinding destinationType = (SourceTypeBinding)(((QualifiedSuperReference)this.receiver).currentCompatibleType);
		this.syntheticAccessor = destinationType.addSyntheticMethod(this.codegenBinding, isSuperAccess());
		currentScope.problemReporter().needToEmulateMethodAccess(this.codegenBinding, this);
		return;

	} else if (this.binding.isProtected()){

		SourceTypeBinding enclosingSourceType;
		if (((this.bits & ASTNode.DepthMASK) != 0) 
				&& this.codegenBinding.declaringClass.getPackage() 
					!= (enclosingSourceType = currentScope.enclosingSourceType()).getPackage()){

			SourceTypeBinding currentCompatibleType = (SourceTypeBinding)enclosingSourceType.enclosingTypeAt((this.bits & ASTNode.DepthMASK) >> ASTNode.DepthSHIFT);
			this.syntheticAccessor = currentCompatibleType.addSyntheticMethod(this.codegenBinding, isSuperAccess());
			currentScope.problemReporter().needToEmulateMethodAccess(this.codegenBinding, this);
			return;
		}
	}
	
	// if the binding declaring class is not visible, need special action
	// for runtime compatibility on 1.2 VMs : change the declaring class of the binding
	// NOTE: from target 1.2 on, method's declaring class is touched if any different from receiver type
	// and not from Object or implicit static method call.	
	if (this.binding.declaringClass != this.actualReceiverType
			&& this.receiverGenericCast == null
			&& !this.actualReceiverType.isArrayType()) {
		CompilerOptions options = currentScope.compilerOptions();
		if ((options.targetJDK >= ClassFileConstants.JDK1_2
				&& (options.complianceLevel >= ClassFileConstants.JDK1_4 || !(this.receiver.isImplicitThis() && this.codegenBinding.isStatic()))
				&& this.binding.declaringClass.id != TypeIds.T_JavaLangObject) // no change for Object methods
			|| !this.binding.declaringClass.canBeSeenBy(currentScope)) {

			this.codegenBinding = currentScope.enclosingSourceType().getUpdatedMethodBinding(
			        										this.codegenBinding, (ReferenceBinding) this.actualReceiverType.erasure());
		}
		// Post 1.4.0 target, array clone() invocations are qualified with array type 
		// This is handled in array type #clone method binding resolution (see Scope and UpdatedMethodBinding)
	}
}
public int nullStatus(FlowInfo flowInfo) {
	return FlowInfo.UNKNOWN;
}

/**
 * @see org.eclipse.jdt.internal.compiler.ast.Expression#postConversionType(Scope)
 */
public TypeBinding postConversionType(Scope scope) {
	TypeBinding convertedType = this.resolvedType;
	if (this.valueCast != null) 
		convertedType = this.valueCast;
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
	
public StringBuffer printExpression(int indent, StringBuffer output){
	
	if (!this.receiver.isImplicitThis()) this.receiver.printExpression(0, output).append('.');
	if (this.typeArguments != null) {
		output.append('<');
		int max = this.typeArguments.length - 1;
		for (int j = 0; j < max; j++) {
			this.typeArguments[j].print(0, output);
			output.append(", ");//$NON-NLS-1$
		}
		this.typeArguments[max].print(0, output);
		output.append('>');
	}
	output.append(this.selector).append('(') ;
	if (this.arguments != null) {
		for (int i = 0; i < this.arguments.length ; i ++) {	
			if (i > 0) output.append(", "); //$NON-NLS-1$
			this.arguments[i].printExpression(0, output);
		}
	}
	return output.append(')');
}

public TypeBinding resolveType(BlockScope scope) {
	// Answer the signature return type
	// Base type promotion

	this.constant = Constant.NotAConstant;
	boolean receiverCast = false, argsContainCast = false; 
	if (this.receiver instanceof CastExpression) {
		this.receiver.bits |= ASTNode.DisableUnnecessaryCastCheck; // will check later on
		receiverCast = true;
	}
	this.actualReceiverType = this.receiver.resolveType(scope); 
	boolean receiverIsType = this.receiver instanceof NameReference && (((NameReference) this.receiver).bits & Binding.TYPE) != 0;
	if (receiverCast && this.actualReceiverType != null) {
		 // due to change of declaring class with receiver type, only identity cast should be notified
		if (((CastExpression)this.receiver).expression.resolvedType == this.actualReceiverType) { 
			scope.problemReporter().unnecessaryCast((CastExpression)this.receiver);		
		}
	}
	// resolve type arguments (for generic constructor call)
	if (this.typeArguments != null) {
		int length = this.typeArguments.length;
		boolean argHasError = scope.compilerOptions().sourceLevel < ClassFileConstants.JDK1_5; // typeChecks all arguments
		this.genericTypeArguments = new TypeBinding[length];
		for (int i = 0; i < length; i++) {
			TypeReference typeReference = this.typeArguments[i];
			if ((this.genericTypeArguments[i] = typeReference.resolveType(scope, true /* check bounds*/)) == null) {
				argHasError = true;
			}
			if (argHasError && typeReference instanceof Wildcard) {
				scope.problemReporter().illegalUsageOfWildcard(typeReference);
			}
		}
		if (argHasError) {
			if (this.arguments != null) { // still attempt to resolve arguments
				for (int i = 0, max = this.arguments.length; i < max; i++) {
					this.arguments[i].resolveType(scope);
				}
			}					
			return null;
		}
	}	
	// will check for null after args are resolved
	TypeBinding[] argumentTypes = Binding.NO_PARAMETERS;
	if (this.arguments != null) {
		boolean argHasError = false; // typeChecks all arguments 
		int length = this.arguments.length;
		argumentTypes = new TypeBinding[length];
		for (int i = 0; i < length; i++){
			Expression argument = this.arguments[i];
			if (argument instanceof CastExpression) {
				argument.bits |= ASTNode.DisableUnnecessaryCastCheck; // will check later on
				argsContainCast = true;
			}
			if ((argumentTypes[i] = argument.resolveType(scope)) == null){
				argHasError = true;
			}
		}
		if (argHasError) {
			if (this.actualReceiverType instanceof ReferenceBinding) {
				//  record a best guess, for clients who need hint about possible method match
				TypeBinding[] pseudoArgs = new TypeBinding[length];
				for (int i = length; --i >= 0;)
					pseudoArgs[i] = argumentTypes[i] == null ? TypeBinding.NULL : argumentTypes[i]; // replace args with errors with null type
				this.binding = 
					this.receiver.isImplicitThis()
						? scope.getImplicitMethod(this.selector, pseudoArgs, this)
						: scope.findMethod((ReferenceBinding) this.actualReceiverType, this.selector, pseudoArgs, this);
				if (this.binding != null && !this.binding.isValidBinding()) {
					MethodBinding closestMatch = ((ProblemMethodBinding)this.binding).closestMatch;
					// record the closest match, for clients who may still need hint about possible method match
					if (closestMatch != null) {
						if (closestMatch.original().typeVariables != Binding.NO_TYPE_VARIABLES) { // generic method
							// shouldn't return generic method outside its context, rather convert it to raw method (175409)
							closestMatch = scope.environment().createParameterizedGenericMethod(closestMatch.original(), (RawTypeBinding)null);
						}
						this.binding = closestMatch;
						MethodBinding closestMatchOriginal = closestMatch.original();
						if ((closestMatchOriginal.isPrivate() || closestMatchOriginal.declaringClass.isLocalType()) && !scope.isDefinedInMethod(closestMatchOriginal)) {
							// ignore cases where method is used from within inside itself (e.g. direct recursions)
							closestMatchOriginal.modifiers |= ExtraCompilerModifiers.AccLocallyUsed;
						}
					}
				}
			}
			return null;
		}
	}
	if (this.actualReceiverType == null) {
		return null;
	}
	// base type cannot receive any message
	if (this.actualReceiverType.isBaseType()) {
		scope.problemReporter().errorNoMethodFor(this, this.actualReceiverType, argumentTypes);
		return null;
	}
	this.binding = 
		this.receiver.isImplicitThis()
			? scope.getImplicitMethod(this.selector, argumentTypes, this)
			: scope.getMethod(this.actualReceiverType, this.selector, argumentTypes, this);
	if (!this.binding.isValidBinding()) {
		if (this.binding.declaringClass == null) {
			if (this.actualReceiverType instanceof ReferenceBinding) {
				this.binding.declaringClass = (ReferenceBinding) this.actualReceiverType;
			} else { 
				scope.problemReporter().errorNoMethodFor(this, this.actualReceiverType, argumentTypes);
				return null;
			}
		}
		scope.problemReporter().invalidMethod(this, this.binding);
		MethodBinding closestMatch = ((ProblemMethodBinding)this.binding).closestMatch;
		switch (this.binding.problemId()) {
			case ProblemReasons.Ambiguous :
				break; // no resilience on ambiguous
			case ProblemReasons.NotVisible :
			case ProblemReasons.NonStaticReferenceInConstructorInvocation :
			case ProblemReasons.NonStaticReferenceInStaticContext :
			case ProblemReasons.ReceiverTypeNotVisible :
			case ProblemReasons.ParameterBoundMismatch :
				// only steal returnType in cases listed above
				if (closestMatch != null) this.resolvedType = closestMatch.returnType;
			default :
		}
		// record the closest match, for clients who may still need hint about possible method match
		if (closestMatch != null) {
			this.binding = closestMatch;
			MethodBinding closestMatchOriginal = closestMatch.original();			
			if ((closestMatchOriginal.isPrivate() || closestMatchOriginal.declaringClass.isLocalType()) && !scope.isDefinedInMethod(closestMatchOriginal)) {
				// ignore cases where method is used from within inside itself (e.g. direct recursions)
				closestMatchOriginal.modifiers |= ExtraCompilerModifiers.AccLocallyUsed;
			}
		}
		return (this.resolvedType != null && (this.resolvedType.tagBits & TagBits.HasMissingType) == 0)
						? this.resolvedType 
						: null;
	}
	if ((this.binding.tagBits & TagBits.HasMissingType) != 0) {
		scope.problemReporter().missingTypeInMethod(this, this.binding);
	}
	final CompilerOptions compilerOptions = scope.compilerOptions();
	if (!this.binding.isStatic()) {
		// the "receiver" must not be a type
		if (receiverIsType) {
			scope.problemReporter().mustUseAStaticMethod(this, this.binding);
			if (this.actualReceiverType.isRawType() 
					&& (this.receiver.bits & ASTNode.IgnoreRawTypeCheck) == 0 
					&& compilerOptions.getSeverity(CompilerOptions.RawTypeReference) != ProblemSeverities.Ignore) {
				scope.problemReporter().rawTypeReference(this.receiver, this.actualReceiverType);
			}
		} else {
			this.receiver.computeConversion(scope, this.actualReceiverType, this.actualReceiverType);
			// compute generic cast if necessary
			TypeBinding receiverErasure = this.actualReceiverType.erasure();
			if (receiverErasure instanceof ReferenceBinding) {
				if (receiverErasure.findSuperTypeOriginatingFrom(this.binding.declaringClass) == null) {
					this.receiverGenericCast = this.binding.declaringClass; // handle indirect inheritance thru variable secondary bound
				}
			}
		}
	} else {
		// static message invoked through receiver? legal but unoptimal (optional warning).
		if (!(this.receiver.isImplicitThis() || this.receiver.isSuper() || receiverIsType)) {
			scope.problemReporter().nonStaticAccessToStaticMethod(this, this.binding);
		}
		if (!this.receiver.isImplicitThis() && this.binding.declaringClass != this.actualReceiverType) {
			scope.problemReporter().indirectAccessToStaticMethod(this, this.binding);
		}		
	}
	checkInvocationArguments(scope, this.receiver, this.actualReceiverType, this.binding, this.arguments, argumentTypes, argsContainCast, this);

	//-------message send that are known to fail at compile time-----------
	if (this.binding.isAbstract()) {
		if (this.receiver.isSuper()) {
			scope.problemReporter().cannotDireclyInvokeAbstractMethod(this, this.binding);
		}
		// abstract private methods cannot occur nor abstract static............
	}
	if (isMethodUseDeprecated(this.binding, scope, true))
		scope.problemReporter().deprecatedMethod(this.binding, this);

	// from 1.5 compliance on, array#clone() returns the array type (but binding still shows Object)
	if (this.actualReceiverType.isArrayType() 
			&& this.binding.parameters == Binding.NO_PARAMETERS 
			&& compilerOptions.complianceLevel >= ClassFileConstants.JDK1_5 
			&& CharOperation.equals(this.binding.selector, TypeConstants.CLONE)) {
		this.resolvedType = this.actualReceiverType;
	} else {
		TypeBinding returnType = this.binding.returnType;
		if (returnType != null) returnType = returnType.capture(scope, this.sourceEnd);
		this.resolvedType = returnType;
	}
	if (this.receiver.isSuper() && compilerOptions.getSeverity(CompilerOptions.OverridingMethodWithoutSuperInvocation) != ProblemSeverities.Ignore) {
		final ReferenceContext referenceContext = scope.methodScope().referenceContext;
		if (referenceContext instanceof AbstractMethodDeclaration) {
			final AbstractMethodDeclaration abstractMethodDeclaration = (AbstractMethodDeclaration) referenceContext;
			MethodBinding enclosingMethodBinding = abstractMethodDeclaration.binding;
			if (enclosingMethodBinding.isOverriding()
					&& CharOperation.equals(this.binding.selector, enclosingMethodBinding.selector)
					&& this.binding.areParametersEqual(enclosingMethodBinding)) {
				abstractMethodDeclaration.bits |= ASTNode.OverridingMethodWithSupercall;
			}
		}
	}
	if (this.typeArguments != null && this.binding.original().typeVariables == Binding.NO_TYPE_VARIABLES) {
		scope.problemReporter().unnecessaryTypeArgumentsForMethodInvocation(this.binding, this.genericTypeArguments, this.typeArguments);
	}
	return (this.resolvedType.tagBits & TagBits.HasMissingType) == 0
				? this.resolvedType 
				: null;
}

public void setActualReceiverType(ReferenceBinding receiverType) {
	if (receiverType == null) return; // error scenario only
	this.actualReceiverType = receiverType;
}
public void setDepth(int depth) {
	this.bits &= ~ASTNode.DepthMASK; // flush previous depth if any
	if (depth > 0) {
		this.bits |= (depth & 0xFF) << ASTNode.DepthSHIFT; // encoded on 8 bits
	}
}

/**
 * @see org.eclipse.jdt.internal.compiler.ast.Expression#setExpectedType(org.eclipse.jdt.internal.compiler.lookup.TypeBinding)
 */
public void setExpectedType(TypeBinding expectedType) {
    this.expectedType = expectedType;
}
public void setFieldIndex(int depth) {
	// ignore for here
}

public void traverse(ASTVisitor visitor, BlockScope blockScope) {
	if (visitor.visit(this, blockScope)) {
		this.receiver.traverse(visitor, blockScope);
		if (this.typeArguments != null) {
			for (int i = 0, typeArgumentsLength = this.typeArguments.length; i < typeArgumentsLength; i++) {
				this.typeArguments[i].traverse(visitor, blockScope);
			}		
		}
		if (this.arguments != null) {
			int argumentsLength = this.arguments.length;
			for (int i = 0; i < argumentsLength; i++)
				this.arguments[i].traverse(visitor, blockScope);
		}
	}
	visitor.endVisit(this, blockScope);
}
}
