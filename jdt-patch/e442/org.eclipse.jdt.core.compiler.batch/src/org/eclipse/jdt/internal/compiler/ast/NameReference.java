/*******************************************************************************
 * Copyright (c) 2000, 2026 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for
 *								bug 331649 - [compiler][null] consider null annotations for fields
 *								Bug 400874 - [1.8][compiler] Inference infrastructure should evolve to meet JLS8 18.x (Part G of JSR335 spec)
 *								Bug 426996 - [1.8][inference] try to avoid method Expression.unresolve()?
 *     Jesper S Moller - Contributions for
 *							bug 382721 - [1.8][compiler] Effectively final variables needs special treatment
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;

public abstract class NameReference extends Reference implements InvocationSite {

	public Binding binding; //may be aTypeBinding-aFieldBinding-aLocalVariableBinding

	public TypeBinding actualReceiverType;	// modified receiver type - actual one according to namelookup

	//the error printing
	//some name reference are build as name reference but
	//only used as type reference. When it happens, instead of
	//creating a new object (aTypeReference) we just flag a boolean
	//This concesion is valuable while there are cases when the NameReference
	//will be a TypeReference (static message sends.....) and there is
	//no changeClass in java.
public NameReference() {
	this.bits |= Binding.TYPE | Binding.VARIABLE; // restrictiveFlag
}

protected void checkLocalStaticClassVariables(BlockScope scope, VariableBinding variable) {
	// Only check for static methods accessing outer locals in Java 16+ (when static methods in local classes are allowed)
	if (scope.compilerOptions().sourceLevel < ClassFileConstants.JDK16)
		return;

	// Compile-time constant variables are inlined and can be legally accessed from a static
	// context (e.g. an annotation on a local class, or a static method of a local class), so
	// they are not subject to the outer-local reference restriction of JLS 8.1.3.
	if (variable.constant(scope) != Constant.NotAConstant)
		return;
	// Check if we're in a local type (either static local class OR non-static local class with static method)
	if (this.actualReceiverType.isLocalType()) {
		MethodScope currentMethodScope = scope instanceof MethodScope ? (MethodScope) scope : scope.enclosingMethodScope();
		// Check if either the local class is static OR the current method is static
		boolean inStaticContext = this.actualReceiverType.isStatic() || (currentMethodScope != null && currentMethodScope.isStatic);

		if (inStaticContext &&
				(variable.modifiers & ClassFileConstants.AccStatic) == 0 &&
				(this.bits & ASTNode.IsCapturedOuterLocal) != 0) {
			BlockScope declaringScope = ((LocalVariableBinding) this.binding).declaringScope;
			MethodScope declaringMethodScope = declaringScope instanceof MethodScope ? (MethodScope)declaringScope :
				declaringScope.enclosingMethodScope();
			ClassScope declaringClassScope = declaringMethodScope != null ? declaringMethodScope.classScope() : null;
			ClassScope currentClassScope = currentMethodScope != null ? currentMethodScope.classScope() : null;
			if (declaringClassScope != currentClassScope)
				scope.problemReporter().recordStaticReferenceToOuterLocalVariable((LocalVariableBinding)variable, this);
		}
	}
}

/**
 * Creates a constant pool entry which is not needed by the VM but might help tools.
 * See https://bugs.openjdk.org/browse/JDK-7153958
 */
public void emitDeclaringClassOfConstant(CodeStream codeStream) {
	if (this.constant != Constant.NotAConstant && this.binding instanceof FieldBinding f) {
		codeStream.constantPool.literalIndexForType(f.declaringClass);
	}
}
/**
 * Creates a constant pool entry for each constant reference within expr.
 * This is not needed by the VM but might help tools.
 * See https://bugs.openjdk.org/browse/JDK-7153958
 */
public static void emitDeclaringClassOfConstant(Expression expr, CodeStream codeStream) {
	if (expr instanceof Literal)
		return;
	expr.traverse(
		new ASTVisitor() {
			@Override
			public boolean visit(SingleNameReference nameReference, BlockScope scope) {
				nameReference.emitDeclaringClassOfConstant(codeStream);
				return false;
			}
			@Override
			public boolean visit(QualifiedNameReference nameReference, BlockScope scope) {
				nameReference.emitDeclaringClassOfConstant(codeStream);
				return false;
			}
		},
		(BlockScope) null
	);
}

/**
 * Use this method only when sure that the current reference is <strong>not</strong>
 * a chain of several fields (QualifiedNameReference with more than one field).
 * Otherwise use {@link #lastFieldBinding()}.
 */
@Override
public FieldBinding fieldBinding() {
	//this method should be sent ONLY after a check against isFieldReference()
	//check its use doing senders.........
	return (FieldBinding) this.binding ;
}

@Override
public FieldBinding lastFieldBinding() {
	if ((this.bits & ASTNode.RestrictiveFlagMASK) == Binding.FIELD)
		return fieldBinding(); // most subclasses only refer to one field anyway
	return null;
}

@Override
public InferenceContext18 freshInferenceContext(Scope scope) {
	return null;
}

@Override
public boolean isSuperAccess() {
	return false;
}

@Override
public boolean isTypeAccess() {
	// null is acceptable when we are resolving the first part of a reference
	return this.binding == null || (this.binding.kind() & Binding.TYPE) != 0;
}

@Override
public boolean isTypeReference() {
	return this.binding instanceof ReferenceBinding;
}

@Override
public void setActualReceiverType(ReferenceBinding receiverType) {
	if (receiverType == null) return; // error scenario only
	this.actualReceiverType = receiverType;
}

@Override
public void setDepth(int depth) {
	this.bits &= ~DepthMASK; // flush previous depth if any
	if (depth > 0) {
		this.bits |= (depth & 0xFF) << DepthSHIFT; // encoded on 8 bits
	}
}

@Override
public void setFieldIndex(int index){
	// ignored
}

public abstract String unboundReferenceErrorName();

public abstract char[][] getName();

@Override
public boolean isType() {
	return (this.bits & Binding.TYPE) != 0;
}
}
