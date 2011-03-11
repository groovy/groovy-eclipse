/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contributions for 
 *     						bug 236385 - [compiler] Warn for potential programming problem if an object is created but not used
 *     						bug 319201 - [null] no warning when unboxing SingleNameReference causes NPE
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class AllocationExpression extends Expression implements InvocationSite {

	public TypeReference type;
	public Expression[] arguments;
	public MethodBinding binding;							// exact binding resulting from lookup
	MethodBinding syntheticAccessor;						// synthetic accessor for inner-emulation
	public TypeReference[] typeArguments;
	public TypeBinding[] genericTypeArguments;
	public FieldDeclaration enumConstant; // for enum constant initializations

public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	// check captured variables are initialized in current context (26134)
	checkCapturedLocalInitializationIfNecessary((ReferenceBinding)this.binding.declaringClass.erasure(), currentScope, flowInfo);

	// process arguments
	if (this.arguments != null) {
		for (int i = 0, count = this.arguments.length; i < count; i++) {
			flowInfo =
				this.arguments[i]
					.analyseCode(currentScope, flowContext, flowInfo)
					.unconditionalInits();
			if ((this.arguments[i].implicitConversion & TypeIds.UNBOXING) != 0) {
				this.arguments[i].checkNPE(currentScope, flowContext, flowInfo);
			}
		}
	}
	// record some dependency information for exception types
	ReferenceBinding[] thrownExceptions;
	if (((thrownExceptions = this.binding.thrownExceptions).length) != 0) {
		if ((this.bits & ASTNode.Unchecked) != 0 && this.genericTypeArguments == null) {
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=277643, align with javac on JLS 15.12.2.6
			thrownExceptions = currentScope.environment().convertToRawTypes(this.binding.thrownExceptions, true, true);
		}		
		// check exception handling
		flowContext.checkExceptionHandlers(
			thrownExceptions,
			this,
			flowInfo.unconditionalCopy(),
			currentScope);
	}
	manageEnclosingInstanceAccessIfNecessary(currentScope, flowInfo);
	manageSyntheticAccessIfNecessary(currentScope, flowInfo);

	return flowInfo;
}

public void checkCapturedLocalInitializationIfNecessary(ReferenceBinding checkedType, BlockScope currentScope, FlowInfo flowInfo) {
	if (((checkedType.tagBits & ( TagBits.AnonymousTypeMask|TagBits.LocalTypeMask)) == TagBits.LocalTypeMask)
			&& !currentScope.isDefinedInType(checkedType)) { // only check external allocations
		NestedTypeBinding nestedType = (NestedTypeBinding) checkedType;
		SyntheticArgumentBinding[] syntheticArguments = nestedType.syntheticOuterLocalVariables();
		if (syntheticArguments != null)
			for (int i = 0, count = syntheticArguments.length; i < count; i++){
				SyntheticArgumentBinding syntheticArgument = syntheticArguments[i];
				LocalVariableBinding targetLocal;
				if ((targetLocal = syntheticArgument.actualOuterLocalVariable) == null) continue;
				if (targetLocal.declaration != null && !flowInfo.isDefinitelyAssigned(targetLocal)){
					currentScope.problemReporter().uninitializedLocalVariable(targetLocal, this);
				}
			}
	}
}

public Expression enclosingInstance() {
	return null;
}

public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	if (!valueRequired)
		currentScope.problemReporter().unusedObjectAllocation(this);

	int pc = codeStream.position;
	MethodBinding codegenBinding = this.binding.original();
	ReferenceBinding allocatedType = codegenBinding.declaringClass;

	codeStream.new_(allocatedType);
	boolean isUnboxing = (this.implicitConversion & TypeIds.UNBOXING) != 0;
	if (valueRequired || isUnboxing) {
		codeStream.dup();
	}
	// better highlight for allocation: display the type individually
	if (this.type != null) { // null for enum constant body
		codeStream.recordPositionsFrom(pc, this.type.sourceStart);
	} else {
		// push enum constant name and ordinal
		codeStream.ldc(String.valueOf(this.enumConstant.name));
		codeStream.generateInlinedValue(this.enumConstant.binding.id);
	}

	// handling innerclass instance allocation - enclosing instance arguments
	if (allocatedType.isNestedType()) {
		codeStream.generateSyntheticEnclosingInstanceValues(
			currentScope,
			allocatedType,
			enclosingInstance(),
			this);
	}
	// generate the arguments for constructor
	generateArguments(this.binding, this.arguments, currentScope, codeStream);
	// handling innerclass instance allocation - outer local arguments
	if (allocatedType.isNestedType()) {
		codeStream.generateSyntheticOuterArgumentValues(
			currentScope,
			allocatedType,
			this);
	}
	// invoke constructor
	if (this.syntheticAccessor == null) {
		codeStream.invoke(Opcodes.OPC_invokespecial, codegenBinding, null /* default declaringClass */);
	} else {
		// synthetic accessor got some extra arguments appended to its signature, which need values
		for (int i = 0,
			max = this.syntheticAccessor.parameters.length - codegenBinding.parameters.length;
			i < max;
			i++) {
			codeStream.aconst_null();
		}
		codeStream.invoke(Opcodes.OPC_invokespecial, this.syntheticAccessor, null /* default declaringClass */);
	}
	if (valueRequired) {
		codeStream.generateImplicitConversion(this.implicitConversion);
	} else if (isUnboxing) {
		// conversion only generated if unboxing
		codeStream.generateImplicitConversion(this.implicitConversion);
		switch (postConversionType(currentScope).id) {
			case T_long :
			case T_double :
				codeStream.pop2();
				break;
			default :
				codeStream.pop();
		}
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}

/**
 * @see org.eclipse.jdt.internal.compiler.lookup.InvocationSite#genericTypeArguments()
 */
public TypeBinding[] genericTypeArguments() {
	return this.genericTypeArguments;
}

public boolean isSuperAccess() {
	return false;
}

public boolean isTypeAccess() {
	return true;
}

/* Inner emulation consists in either recording a dependency
 * link only, or performing one level of propagation.
 *
 * Dependency mechanism is used whenever dealing with source target
 * types, since by the time we reach them, we might not yet know their
 * exact need.
 */
public void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {
	if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) != 0) return;
	ReferenceBinding allocatedTypeErasure = (ReferenceBinding) this.binding.declaringClass.erasure();

	// perform some emulation work in case there is some and we are inside a local type only
	if (allocatedTypeErasure.isNestedType()
		&& currentScope.enclosingSourceType().isLocalType()) {

		if (allocatedTypeErasure.isLocalType()) {
			((LocalTypeBinding) allocatedTypeErasure).addInnerEmulationDependent(currentScope, false);
			// request cascade of accesses
		} else {
			// locally propagate, since we already now the desired shape for sure
			currentScope.propagateInnerEmulation(allocatedTypeErasure, false);
			// request cascade of accesses
		}
	}
}

public void manageSyntheticAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {
	if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) != 0) return;
	// if constructor from parameterized type got found, use the original constructor at codegen time
	MethodBinding codegenBinding = this.binding.original();

	ReferenceBinding declaringClass;
	if (codegenBinding.isPrivate() && currentScope.enclosingSourceType() != (declaringClass = codegenBinding.declaringClass)) {

		// from 1.4 on, local type constructor can lose their private flag to ease emulation
		if ((declaringClass.tagBits & TagBits.IsLocalType) != 0 && currentScope.compilerOptions().complianceLevel >= ClassFileConstants.JDK1_4) {
			// constructor will not be dumped as private, no emulation required thus
			codegenBinding.tagBits |= TagBits.ClearPrivateModifier;
		} else {
			this.syntheticAccessor = ((SourceTypeBinding) declaringClass).addSyntheticMethod(codegenBinding, isSuperAccess());
			currentScope.problemReporter().needToEmulateMethodAccess(codegenBinding, this);
		}
	}
}

public StringBuffer printExpression(int indent, StringBuffer output) {
	if (this.type != null) { // type null for enum constant initializations
		output.append("new "); //$NON-NLS-1$
	}
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
	if (this.type != null) { // type null for enum constant initializations
		this.type.printExpression(0, output);
	}
	output.append('(');
	if (this.arguments != null) {
		for (int i = 0; i < this.arguments.length; i++) {
			if (i > 0) output.append(", "); //$NON-NLS-1$
			this.arguments[i].printExpression(0, output);
		}
	}
	return output.append(')');
}

public TypeBinding resolveType(BlockScope scope) {
	// Propagate the type checking to the arguments, and check if the constructor is defined.
	this.constant = Constant.NotAConstant;
	if (this.type == null) {
		// initialization of an enum constant
		this.resolvedType = scope.enclosingReceiverType();
	} else {
		this.resolvedType = this.type.resolveType(scope, true /* check bounds*/);
		checkParameterizedAllocation: {
			if (this.type instanceof ParameterizedQualifiedTypeReference) { // disallow new X<String>.Y<Integer>()
				ReferenceBinding currentType = (ReferenceBinding)this.resolvedType;
				if (currentType == null) return currentType;
				do {
					// isStatic() is answering true for toplevel types
					if ((currentType.modifiers & ClassFileConstants.AccStatic) != 0) break checkParameterizedAllocation;
					if (currentType.isRawType()) break checkParameterizedAllocation;
				} while ((currentType = currentType.enclosingType())!= null);
				ParameterizedQualifiedTypeReference qRef = (ParameterizedQualifiedTypeReference) this.type;
				for (int i = qRef.typeArguments.length - 2; i >= 0; i--) {
					if (qRef.typeArguments[i] != null) {
						scope.problemReporter().illegalQualifiedParameterizedTypeAllocation(this.type, this.resolvedType);
						break;
					}
				}
			}
		}
	}
	// will check for null after args are resolved

	// resolve type arguments (for generic constructor call)
	if (this.typeArguments != null) {
		int length = this.typeArguments.length;
		boolean argHasError = scope.compilerOptions().sourceLevel < ClassFileConstants.JDK1_5;
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

	// buffering the arguments' types
	boolean argsContainCast = false;
	TypeBinding[] argumentTypes = Binding.NO_PARAMETERS;
	if (this.arguments != null) {
		boolean argHasError = false;
		int length = this.arguments.length;
		argumentTypes = new TypeBinding[length];
		for (int i = 0; i < length; i++) {
			Expression argument = this.arguments[i];
			if (argument instanceof CastExpression) {
				argument.bits |= DisableUnnecessaryCastCheck; // will check later on
				argsContainCast = true;
			}
			if ((argumentTypes[i] = argument.resolveType(scope)) == null) {
				argHasError = true;
			}
		}
		if (argHasError) {
			if (this.resolvedType instanceof ReferenceBinding) {
				// record a best guess, for clients who need hint about possible contructor match
				TypeBinding[] pseudoArgs = new TypeBinding[length];
				for (int i = length; --i >= 0;) {
					pseudoArgs[i] = argumentTypes[i] == null ? TypeBinding.NULL : argumentTypes[i]; // replace args with errors with null type
				}
				this.binding = scope.findMethod((ReferenceBinding) this.resolvedType, TypeConstants.INIT, pseudoArgs, this);
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
						if (closestMatchOriginal.isOrEnclosedByPrivateType() && !scope.isDefinedInMethod(closestMatchOriginal)) {
							// ignore cases where method is used from within inside itself (e.g. direct recursions)
							closestMatchOriginal.modifiers |= ExtraCompilerModifiers.AccLocallyUsed;
						}
					}
				}
			}
			return this.resolvedType;
		}
	}
	if (this.resolvedType == null || !this.resolvedType.isValidBinding()) {
		return null;
	}

	// null type denotes fake allocation for enum constant inits
	if (this.type != null && !this.resolvedType.canBeInstantiated()) {
		scope.problemReporter().cannotInstantiate(this.type, this.resolvedType);
		return this.resolvedType;
	}
	ReferenceBinding allocationType = (ReferenceBinding) this.resolvedType;
	if (!(this.binding = scope.getConstructor(allocationType, argumentTypes, this)).isValidBinding()) {
		if (this.binding.declaringClass == null) {
			this.binding.declaringClass = allocationType;
		}
		if (this.type != null && !this.type.resolvedType.isValidBinding()) {
			return null;
		}
		scope.problemReporter().invalidConstructor(this, this.binding);
		return this.resolvedType;
	}
	if ((this.binding.tagBits & TagBits.HasMissingType) != 0) {
		scope.problemReporter().missingTypeInConstructor(this, this.binding);
	}
	if (isMethodUseDeprecated(this.binding, scope, true))
		scope.problemReporter().deprecatedMethod(this.binding, this);
	if (checkInvocationArguments(scope, null, allocationType, this.binding, this.arguments, argumentTypes, argsContainCast, this)) {
		this.bits |= ASTNode.Unchecked;
	}
	if (this.typeArguments != null && this.binding.original().typeVariables == Binding.NO_TYPE_VARIABLES) {
		scope.problemReporter().unnecessaryTypeArgumentsForMethodInvocation(this.binding, this.genericTypeArguments, this.typeArguments);
	}
	return allocationType;
}

public void setActualReceiverType(ReferenceBinding receiverType) {
	// ignored
}

public void setDepth(int i) {
	// ignored
}

public void setFieldIndex(int i) {
	// ignored
}

public void traverse(ASTVisitor visitor, BlockScope scope) {
	if (visitor.visit(this, scope)) {
		if (this.typeArguments != null) {
			for (int i = 0, typeArgumentsLength = this.typeArguments.length; i < typeArgumentsLength; i++) {
				this.typeArguments[i].traverse(visitor, scope);
			}
		}
		if (this.type != null) { // enum constant scenario
			this.type.traverse(visitor, scope);
		}
		if (this.arguments != null) {
			for (int i = 0, argumentsLength = this.arguments.length; i < argumentsLength; i++)
				this.arguments[i].traverse(visitor, scope);
		}
	}
	visitor.endVisit(this, scope);
}
}
