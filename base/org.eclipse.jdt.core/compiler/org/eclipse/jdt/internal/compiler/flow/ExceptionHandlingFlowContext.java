/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.flow;

import java.util.ArrayList;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.SubRoutineStatement;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.codegen.ObjectCache;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

/**
 * Reflects the context of code analysis, keeping track of enclosing
 *	try statements, exception handlers, etc...
 */
public class ExceptionHandlingFlowContext extends FlowContext {

	public final static int BitCacheSize = 32; // 32 bits per int

	public ReferenceBinding[] handledExceptions;
	int[] isReached;
	int[] isNeeded;
	UnconditionalFlowInfo[] initsOnExceptions;
	ObjectCache indexes = new ObjectCache();
	boolean isMethodContext;

	public UnconditionalFlowInfo initsOnReturn;
	public FlowContext initializationParent; // special parent relationship only for initialization purpose
	
	// for dealing with anonymous constructor thrown exceptions
	public ArrayList extendedExceptions;

public ExceptionHandlingFlowContext(
		FlowContext parent,
		ASTNode associatedNode,
		ReferenceBinding[] handledExceptions,
		FlowContext initializationParent,
		BlockScope scope,
		UnconditionalFlowInfo flowInfo) {

	super(parent, associatedNode);
	this.isMethodContext = scope == scope.methodScope();
	this.handledExceptions = handledExceptions;
	int count = handledExceptions.length, cacheSize = (count / ExceptionHandlingFlowContext.BitCacheSize) + 1;
	this.isReached = new int[cacheSize]; // none is reached by default
	this.isNeeded = new int[cacheSize]; // none is needed by default
	this.initsOnExceptions = new UnconditionalFlowInfo[count];
	boolean markExceptionsAndThrowableAsReached =
		!this.isMethodContext || scope.compilerOptions().reportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable;
	for (int i = 0; i < count; i++) {
		ReferenceBinding handledException = handledExceptions[i];
		this.indexes.put(handledException, i); // key type  -> value index
		if (handledException.isUncheckedException(true)) {
			if (markExceptionsAndThrowableAsReached ||
					handledException.id != TypeIds.T_JavaLangThrowable &&
					handledException.id != TypeIds.T_JavaLangException) {
				this.isReached[i / ExceptionHandlingFlowContext.BitCacheSize] |= 1 << (i % ExceptionHandlingFlowContext.BitCacheSize);
			}
			this.initsOnExceptions[i] = flowInfo.unconditionalCopy();
		} else {
			this.initsOnExceptions[i] = FlowInfo.DEAD_END;
		}
	}
	if (!this.isMethodContext) {
		System.arraycopy(this.isReached, 0, this.isNeeded, 0, cacheSize);
	}
	this.initsOnReturn = FlowInfo.DEAD_END;
	this.	initializationParent = initializationParent;
}

public void complainIfUnusedExceptionHandlers(AbstractMethodDeclaration method) {
	MethodScope scope = method.scope;
	// can optionally skip overriding methods
	if ((method.binding.modifiers & (ExtraCompilerModifiers.AccOverriding | ExtraCompilerModifiers.AccImplementing)) != 0
	        && !scope.compilerOptions().reportUnusedDeclaredThrownExceptionWhenOverriding) {
	    return;
	}

	// report errors for unreachable exception handlers
	TypeBinding[] docCommentReferences = null;
	int docCommentReferencesLength = 0;
	if (scope.compilerOptions().
				reportUnusedDeclaredThrownExceptionIncludeDocCommentReference &&
			method.javadoc != null &&
			method.javadoc.exceptionReferences != null &&
			(docCommentReferencesLength = method.javadoc.exceptionReferences.length) > 0) {
		docCommentReferences = new TypeBinding[docCommentReferencesLength];
		for (int i = 0; i < docCommentReferencesLength; i++) {
			docCommentReferences[i] = method.javadoc.exceptionReferences[i].resolvedType;
		}
	}
	nextHandledException: for (int i = 0, count = this.handledExceptions.length; i < count; i++) {
		int index = this.indexes.get(this.handledExceptions[i]);
		if ((this.isReached[index / ExceptionHandlingFlowContext.BitCacheSize] & 1 << (index % ExceptionHandlingFlowContext.BitCacheSize)) == 0) {
			for (int j = 0; j < docCommentReferencesLength; j++) {
				if (docCommentReferences[j] == this.handledExceptions[i]) {
					continue nextHandledException;
				}
			}
			scope.problemReporter().unusedDeclaredThrownException(
				this.handledExceptions[index],
				method,
				method.thrownExceptions[index]);
		}
	}
}

public void complainIfUnusedExceptionHandlers(BlockScope scope,TryStatement tryStatement) {
	// report errors for unreachable exception handlers
	for (int i = 0, count = this.handledExceptions.length; i < count; i++) {
		int index = this.indexes.get(this.handledExceptions[i]);
		int cacheIndex = index / ExceptionHandlingFlowContext.BitCacheSize;
		int bitMask = 1 << (index % ExceptionHandlingFlowContext.BitCacheSize);
		if ((this.isReached[cacheIndex] & bitMask) == 0) {
			scope.problemReporter().unreachableCatchBlock(
				this.handledExceptions[index],
				tryStatement.catchArguments[index].type);
		} else {
			if ((this.isNeeded[cacheIndex] & bitMask) == 0) {
				scope.problemReporter().hiddenCatchBlock(
					this.handledExceptions[index],
					tryStatement.catchArguments[index].type);
			}
		}
	}
}

public String individualToString() {
	StringBuffer buffer = new StringBuffer("Exception flow context"); //$NON-NLS-1$
	int length = this.handledExceptions.length;
	for (int i = 0; i < length; i++) {
		int cacheIndex = i / ExceptionHandlingFlowContext.BitCacheSize;
		int bitMask = 1 << (i % ExceptionHandlingFlowContext.BitCacheSize);
		buffer.append('[').append(this.handledExceptions[i].readableName());
		if ((this.isReached[cacheIndex] & bitMask) != 0) {
			if ((this.isNeeded[cacheIndex] & bitMask) == 0) {
				buffer.append("-masked"); //$NON-NLS-1$
			} else {
				buffer.append("-reached"); //$NON-NLS-1$
			}
		} else {
			buffer.append("-not reached"); //$NON-NLS-1$
		}
		buffer.append('-').append(this.initsOnExceptions[i].toString()).append(']');
	}
	buffer.append("[initsOnReturn -").append(this.initsOnReturn.toString()).append(']'); //$NON-NLS-1$
	return buffer.toString();
}

public UnconditionalFlowInfo initsOnException(ReferenceBinding exceptionType) {
	int index;
	if ((index = this.indexes.get(exceptionType)) < 0) {
		return FlowInfo.DEAD_END;
	}
	return this.initsOnExceptions[index];
}

public UnconditionalFlowInfo initsOnReturn(){
	return this.initsOnReturn;
}

/*
 * Compute a merged list of unhandled exception types (keeping only the most generic ones).
 * This is necessary to add synthetic thrown exceptions for anonymous type constructors (JLS 8.6).
 */
public void mergeUnhandledException(TypeBinding newException){
	if (this.extendedExceptions == null){
		this.extendedExceptions = new ArrayList(5);
		for (int i = 0; i < this.handledExceptions.length; i++){
			this.extendedExceptions.add(this.handledExceptions[i]);
		}
	}
	boolean isRedundant = false;

	for(int i = this.extendedExceptions.size()-1; i >= 0; i--){
		switch(Scope.compareTypes(newException, (TypeBinding)this.extendedExceptions.get(i))){
			case Scope.MORE_GENERIC :
				this.extendedExceptions.remove(i);
				break;
			case Scope.EQUAL_OR_MORE_SPECIFIC :
				isRedundant = true;
				break;
			case Scope.NOT_RELATED :
				break;
		}
	}
	if (!isRedundant){
		this.extendedExceptions.add(newException);
	}
}

public void recordHandlingException(
		ReferenceBinding exceptionType,
		UnconditionalFlowInfo flowInfo,
		TypeBinding raisedException,
		ASTNode invocationSite,
		boolean wasAlreadyDefinitelyCaught) {

	int index = this.indexes.get(exceptionType);
	int cacheIndex = index / ExceptionHandlingFlowContext.BitCacheSize;
	int bitMask = 1 << (index % ExceptionHandlingFlowContext.BitCacheSize);
	if (!wasAlreadyDefinitelyCaught) {
		this.isNeeded[cacheIndex] |= bitMask;
	}
	this.isReached[cacheIndex] |= bitMask;

	this.initsOnExceptions[index] =
		(this.initsOnExceptions[index].tagBits & FlowInfo.UNREACHABLE) == 0 ?
			this.initsOnExceptions[index].mergedWith(flowInfo):
			flowInfo.unconditionalCopy();
}

public void recordReturnFrom(UnconditionalFlowInfo flowInfo) {
	if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) == 0) {
		if ((this.initsOnReturn.tagBits & FlowInfo.UNREACHABLE) == 0) {
			this.initsOnReturn = this.initsOnReturn.mergedWith(flowInfo);
		}
		else {
			this.initsOnReturn = (UnconditionalFlowInfo) flowInfo.copy();
		}
	}
}

/**
 * Exception handlers (with no finally block) are also included with subroutine
 * only once (in case parented with true InsideSubRoutineFlowContext).
 * Standard management of subroutines need to also operate on intermediate
 * exception handlers.
 * @see org.eclipse.jdt.internal.compiler.flow.FlowContext#subroutine()
 */
public SubRoutineStatement subroutine() {
	if (this.associatedNode instanceof SubRoutineStatement) {
		// exception handler context may be child of InsideSubRoutineFlowContext, which maps to same handler
		if (this.parent.subroutine() == this.associatedNode)
			return null;
		return (SubRoutineStatement) this.associatedNode;
	}
	return null;
}
}
