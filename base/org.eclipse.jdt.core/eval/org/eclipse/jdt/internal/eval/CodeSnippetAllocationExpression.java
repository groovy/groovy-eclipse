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

import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

public class CodeSnippetAllocationExpression extends AllocationExpression implements ProblemReasons, EvaluationConstants {
	EvaluationContext evaluationContext;
	FieldBinding delegateThis;
/**
 * CodeSnippetAllocationExpression constructor comment.
 */
public CodeSnippetAllocationExpression(EvaluationContext evaluationContext) {
	this.evaluationContext = evaluationContext;
}
public void generateCode(
	BlockScope currentScope, 
	CodeStream codeStream, 
	boolean valueRequired) {

	int pc = codeStream.position;
	ReferenceBinding allocatedType = this.codegenBinding.declaringClass;

	if (this.codegenBinding.canBeSeenBy(allocatedType, this, currentScope)) {
		codeStream.new_(allocatedType);
		if (valueRequired) {
			codeStream.dup();
		}
		// better highlight for allocation: display the type individually
		codeStream.recordPositionsFrom(pc, this.type.sourceStart);

		// handling innerclass instance allocation - enclosing instance arguments
		if (allocatedType.isNestedType()) {
			codeStream.generateSyntheticEnclosingInstanceValues(
				currentScope,
				allocatedType,
				enclosingInstance(),
				this);
		}
		// generate the arguments for constructor
		if (this.arguments != null) {
			for (int i = 0, count = this.arguments.length; i < count; i++) {
				this.arguments[i].generateCode(currentScope, codeStream, true);
			}
		}
		// handling innerclass instance allocation - outer local arguments
		if (allocatedType.isNestedType()) {
			codeStream.generateSyntheticOuterArgumentValues(
				currentScope,
				allocatedType,
				this);
		}
		// invoke constructor
		codeStream.invokespecial(this.codegenBinding);
	} else {
		// private emulation using reflect
		codeStream.generateEmulationForConstructor(currentScope, this.codegenBinding);
		// generate arguments
		if (this.arguments != null) {
			int argsLength = this.arguments.length;
			codeStream.generateInlinedValue(argsLength);
			codeStream.newArray(currentScope.createArrayType(currentScope.getType(TypeConstants.JAVA_LANG_OBJECT, 3), 1));
			codeStream.dup();
			for (int i = 0; i < argsLength; i++) {
				codeStream.generateInlinedValue(i);
				this.arguments[i].generateCode(currentScope, codeStream, true);
				TypeBinding parameterBinding = this.codegenBinding.parameters[i];
				if (parameterBinding.isBaseType() && parameterBinding != TypeBinding.NULL) {
					codeStream.generateBoxingConversion(this.codegenBinding.parameters[i].id);
				}
				codeStream.aastore();
				if (i < argsLength - 1) {
					codeStream.dup();
				}	
			}
		} else {
			codeStream.generateInlinedValue(0);
			codeStream.newArray(currentScope.createArrayType(currentScope.getType(TypeConstants.JAVA_LANG_OBJECT, 3), 1));			
		}
		codeStream.invokeJavaLangReflectConstructorNewInstance();
		codeStream.checkcast(allocatedType);
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}
/* Inner emulation consists in either recording a dependency 
 * link only, or performing one level of propagation.
 *
 * Dependency mechanism is used whenever dealing with source target
 * types, since by the time we reach them, we might not yet know their
 * exact need.
 */
public void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {
	// not supported yet
}
public void manageSyntheticAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {
		if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) == 0) {

		// if constructor from parameterized type got found, use the original constructor at codegen time
		this.codegenBinding = this.binding.original();
		}
}
public TypeBinding resolveType(BlockScope scope) {
	// Propagate the type checking to the arguments, and check if the constructor is defined.
	this.constant = Constant.NotAConstant;
	this.resolvedType = this.type.resolveType(scope, true /* check bounds*/); // will check for null after args are resolved

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
			return this.resolvedType;
		}
	}
	if (this.resolvedType == null) {
		return null;
	}
	if (!this.resolvedType.canBeInstantiated()) {
		scope.problemReporter().cannotInstantiate(this.type, this.resolvedType);
		return this.resolvedType;
	}
	ReferenceBinding allocatedType = (ReferenceBinding) this.resolvedType;
	if (!(this.binding = scope.getConstructor(allocatedType, argumentTypes, this)).isValidBinding()) {
		if (this.binding instanceof ProblemMethodBinding
			&& ((ProblemMethodBinding) this.binding).problemId() == NotVisible) {
			if (this.evaluationContext.declaringTypeName != null) {
				this.delegateThis = scope.getField(scope.enclosingSourceType(), DELEGATE_THIS, this);
				if (this.delegateThis == null) {
					if (this.binding.declaringClass == null) {
						this.binding.declaringClass = allocatedType;
					}
					if (this.type != null && !this.type.resolvedType.isValidBinding()) {
						return null;
					}					
					scope.problemReporter().invalidConstructor(this, this.binding);
					return this.resolvedType;
				}
			} else {
				if (this.binding.declaringClass == null) {
					this.binding.declaringClass = allocatedType;
				}
				if (this.type != null && !this.type.resolvedType.isValidBinding()) {
					return null;
				}				
				scope.problemReporter().invalidConstructor(this, this.binding);
				return this.resolvedType;
			}
			CodeSnippetScope localScope = new CodeSnippetScope(scope);			
			MethodBinding privateBinding = localScope.getConstructor((ReferenceBinding)this.delegateThis.type, argumentTypes, this);
			if (!privateBinding.isValidBinding()) {
				if (this.binding.declaringClass == null) {
					this.binding.declaringClass = allocatedType;
				}
				if (this.type != null && !this.type.resolvedType.isValidBinding()) {
					return null;
				}				
				scope.problemReporter().invalidConstructor(this, this.binding);
				return this.resolvedType;
			} else {
				this.binding = privateBinding;
			}				
		} else {
			if (this.binding.declaringClass == null) {
				this.binding.declaringClass = allocatedType;
			}
			if (this.type != null && !this.type.resolvedType.isValidBinding()) {
				return null;
			}			
			scope.problemReporter().invalidConstructor(this, this.binding);
			return this.resolvedType;
		}
	}
	if (isMethodUseDeprecated(this.binding, scope, true)) {
		scope.problemReporter().deprecatedMethod(this.binding, this);
	}
	if (arguments != null) {
		for (int i = 0; i < arguments.length; i++) {
		    TypeBinding parameterType = binding.parameters[i];
		    TypeBinding argumentType = argumentTypes[i];
			arguments[i].computeConversion(scope, parameterType, argumentType);
			if (argumentType.needsUncheckedConversion(parameterType)) {
				scope.problemReporter().unsafeTypeConversion(arguments[i], argumentType, parameterType);
			}
		}
		if (argsContainCast) {
			CastExpression.checkNeedForArgumentCasts(scope, null, allocatedType, binding, this.arguments, argumentTypes, this);
		}
	}
	if (allocatedType.isRawType() && this.binding.hasSubstitutedParameters()) {
	    scope.problemReporter().unsafeRawInvocation(this, this.binding);
	}
	return allocatedType;
}
}
