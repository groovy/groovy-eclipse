/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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
 *								bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
 *								bug 402993 - [null] Follow up of bug 401088: Missing warning about redundant null check
 *								Bug 453483 - [compiler][null][loop] Improve null analysis for loops
 *								Bug 421035 - [resource] False alarm of resource leak warning when casting a closeable in its assignment
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.flow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.StatementWithFinallyBlock;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.UnionTypeReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.CatchParameterBinding;
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
	// WARNING: This is an array that maps to catch blocks, not caught exceptions (which could be more than catch blocks in a multi-catch block)
	UnconditionalFlowInfo[] initsOnExceptions;
	IdentityHashMap<ReferenceBinding, Integer> indexes = new IdentityHashMap<>();
	boolean isMethodContext;

	public UnconditionalFlowInfo initsOnReturn;
	public FlowContext initializationParent; // special parent relationship only for initialization purpose

	// for dealing with anonymous constructor thrown exceptions
	public List<TypeBinding> extendedExceptions;

	private static final Argument[] NO_ARGUMENTS = new Argument[0];
	public  Argument [] catchArguments;

	private final int[] exceptionToCatchBlockMap;

public ExceptionHandlingFlowContext(
			FlowContext parent,
			ASTNode associatedNode,
			ReferenceBinding[] handledExceptions,
			FlowContext initializationParent,
			BlockScope scope,
			UnconditionalFlowInfo flowInfo) {
	this(parent, associatedNode, handledExceptions, null, NO_ARGUMENTS, initializationParent, scope, flowInfo);
}
public ExceptionHandlingFlowContext(
		FlowContext parent,
		TryStatement tryStatement,
		ReferenceBinding[] handledExceptions,
		int [] exceptionToCatchBlockMap,
		FlowContext initializationParent,
		BlockScope scope,
		FlowInfo flowInfo) {
	this(parent, tryStatement, handledExceptions, exceptionToCatchBlockMap,
			tryStatement.catchArguments, initializationParent, scope, flowInfo.unconditionalInits());
	UnconditionalFlowInfo unconditionalCopy = flowInfo.unconditionalCopy();
	unconditionalCopy.acceptAllIncomingNullness();
	unconditionalCopy.tagBits |= FlowInfo.UNROOTED;
	this.initsOnFinally = unconditionalCopy;
}
ExceptionHandlingFlowContext(
		FlowContext parent,
		ASTNode associatedNode,
		ReferenceBinding[] handledExceptions,
		int [] exceptionToCatchBlockMap,
		Argument [] catchArguments,
		FlowContext initializationParent,
		BlockScope scope,
		UnconditionalFlowInfo flowInfo) {

	super(parent, associatedNode, true);
	this.isMethodContext = scope == scope.methodScope();
	this.handledExceptions = handledExceptions;
	this.catchArguments = catchArguments;
	this.exceptionToCatchBlockMap = exceptionToCatchBlockMap;
	int count = handledExceptions.length, cacheSize = (count / ExceptionHandlingFlowContext.BitCacheSize) + 1;
	this.isReached = new int[cacheSize]; // none is reached by default
	this.isNeeded = new int[cacheSize]; // none is needed by default
	this.initsOnExceptions = new UnconditionalFlowInfo[count];
	boolean markExceptionsAndThrowableAsReached =
		!this.isMethodContext || scope.compilerOptions().reportUnusedDeclaredThrownExceptionExemptExceptionAndThrowable;
	for (int i = 0; i < count; i++) {
		ReferenceBinding handledException = handledExceptions[i];
		int catchBlock = this.exceptionToCatchBlockMap != null? this.exceptionToCatchBlockMap[i] : i;
		this.indexes.put(handledException, i); // key type  -> value index
		if (handledException.isUncheckedException(true)) {
			if (markExceptionsAndThrowableAsReached ||
					handledException.id != TypeIds.T_JavaLangThrowable &&
					handledException.id != TypeIds.T_JavaLangException) {
				this.isReached[i / ExceptionHandlingFlowContext.BitCacheSize] |= 1 << (i % ExceptionHandlingFlowContext.BitCacheSize);
			}
			this.initsOnExceptions[catchBlock] = flowInfo.unconditionalCopy();
		} else {
			this.initsOnExceptions[catchBlock] = FlowInfo.DEAD_END;
		}
	}
	if (!this.isMethodContext) {
		System.arraycopy(this.isReached, 0, this.isNeeded, 0, cacheSize);
	}
	this.initsOnReturn = FlowInfo.DEAD_END;
	this.initializationParent = initializationParent;
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
	nextHandledException: for (ReferenceBinding handledException : this.handledExceptions) {
		int index = this.indexes.get(handledException);
		if ((this.isReached[index / ExceptionHandlingFlowContext.BitCacheSize] & 1 << (index % ExceptionHandlingFlowContext.BitCacheSize)) == 0) {
			for (int j = 0; j < docCommentReferencesLength; j++) {
				if (TypeBinding.equalsEquals(docCommentReferences[j], handledException)) {
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
	for (int index = 0, count = this.handledExceptions.length; index < count; index++) {
		int cacheIndex = index / ExceptionHandlingFlowContext.BitCacheSize;
		int bitMask = 1 << (index % ExceptionHandlingFlowContext.BitCacheSize);
		if ((this.isReached[cacheIndex] & bitMask) == 0) {
			scope.problemReporter().unreachableCatchBlock(
				this.handledExceptions[index],
				getExceptionType(index));
		} else {
			if ((this.isNeeded[cacheIndex] & bitMask) == 0) {
				scope.problemReporter().hiddenCatchBlock(
					this.handledExceptions[index],
					getExceptionType(index));
			}
		}
	}
}

private ASTNode getExceptionType(int index) {
	if (this.exceptionToCatchBlockMap == null) {
		return this.catchArguments[index].type;
	}
	int catchBlock = this.exceptionToCatchBlockMap[index];
	ASTNode node = this.catchArguments[catchBlock].type;
	if (node instanceof UnionTypeReference) {
		TypeReference[] typeRefs = ((UnionTypeReference)node).typeReferences;
		for (TypeReference typeRef : typeRefs) {
			if (TypeBinding.equalsEquals(typeRef.resolvedType, this.handledExceptions[index])) return typeRef;
		}
	}
	return node;
}

@Override
public FlowContext getInitializationContext() {
	return this.initializationParent;
}

@Override
public String individualToString() {
	StringBuilder buffer = new StringBuilder("Exception flow context"); //$NON-NLS-1$
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
		int catchBlock = this.exceptionToCatchBlockMap != null? this.exceptionToCatchBlockMap[i] : i;
		buffer.append('-').append(this.initsOnExceptions[catchBlock].toString()).append(']');
	}
	buffer.append("[initsOnReturn -").append(this.initsOnReturn.toString()).append(']'); //$NON-NLS-1$
	return buffer.toString();
}

// WARNING: index is the catch block index as in the program order, before any normalization is
// applied for multi catch
public UnconditionalFlowInfo initsOnException(int index) {
	return this.initsOnExceptions[index];
}

@Override
public UnconditionalFlowInfo initsOnReturn(){
	return this.initsOnReturn;
}

/*
 * Compute a merged list of unhandled exception types (keeping only the most generic ones).
 * This is necessary to add synthetic thrown exceptions for anonymous type constructors (JLS 8.6).
 */
public void mergeUnhandledException(TypeBinding newException){
	if (this.extendedExceptions == null){
		this.extendedExceptions = new ArrayList<>(Arrays.asList(this.handledExceptions));
	}
	boolean isRedundant = false;

	for(int i = this.extendedExceptions.size()-1; i >= 0; i--){
		switch(Scope.compareTypes(newException, this.extendedExceptions.get(i))){
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
		TypeBinding caughtException,
		ASTNode invocationSite,
		boolean wasAlreadyDefinitelyCaught) {

	int index = this.indexes.get(exceptionType);
	int cacheIndex = index / ExceptionHandlingFlowContext.BitCacheSize;
	int bitMask = 1 << (index % ExceptionHandlingFlowContext.BitCacheSize);
	if (!wasAlreadyDefinitelyCaught) {
		this.isNeeded[cacheIndex] |= bitMask;
	}
	this.isReached[cacheIndex] |= bitMask;
	int catchBlock = this.exceptionToCatchBlockMap != null? this.exceptionToCatchBlockMap[index] : index;
	if (caughtException != null && this.catchArguments != null && this.catchArguments.length > 0 && !wasAlreadyDefinitelyCaught) {
		CatchParameterBinding catchParameter = (CatchParameterBinding) this.catchArguments[catchBlock].binding;
		catchParameter.setPreciseType(caughtException);
	}
	this.initsOnExceptions[catchBlock] =
		(this.initsOnExceptions[catchBlock].tagBits & FlowInfo.UNREACHABLE) == 0 ?
			this.initsOnExceptions[catchBlock].mergedWith(flowInfo):
			flowInfo.unconditionalCopy();
}

@Override
public void recordReturnFrom(UnconditionalFlowInfo flowInfo) {
	if ((flowInfo.tagBits & FlowInfo.UNREACHABLE_OR_DEAD) == 0) {
		if ((this.initsOnReturn.tagBits & FlowInfo.UNREACHABLE_OR_DEAD) == 0) {
			this.initsOnReturn = this.initsOnReturn.mergedWith(flowInfo);
		}
		else {
			this.initsOnReturn = (UnconditionalFlowInfo) flowInfo.copy();
		}
	}
}

/**
 * Exception handlers (with no finally block) are also included with statement with finally block
 * only once (in case parented with true InsideStatementWithFinallyBlockFlowContext).
 * Standard management of statements with finally blocks need to also operate on intermediate
 * exception handlers.
 * @see org.eclipse.jdt.internal.compiler.flow.FlowContext#statementWithFinallyBlock()
 */
@Override
public StatementWithFinallyBlock statementWithFinallyBlock() {
	if (this.associatedNode instanceof StatementWithFinallyBlock) {
		// exception handler context may be child of InsideStatementWithFinallyBlockFlowContext, which maps to same handler
		if (this.parent.statementWithFinallyBlock() == this.associatedNode)
			return null;
		return (StatementWithFinallyBlock) this.associatedNode;
	}
	return null;
}
}
