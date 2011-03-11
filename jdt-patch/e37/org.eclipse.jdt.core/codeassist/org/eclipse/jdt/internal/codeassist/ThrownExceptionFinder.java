/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

import java.util.Stack;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;

public class ThrownExceptionFinder extends ASTVisitor {

	private SimpleSet thrownExceptions;
	private Stack exceptionsStack;

	public ReferenceBinding[] find(TryStatement tryStatement, BlockScope scope) {
		this.thrownExceptions = new SimpleSet();
		this.exceptionsStack = new Stack();
		tryStatement.traverse(this, scope);
		removeCaughtExceptions(tryStatement);

		ReferenceBinding[] result = new ReferenceBinding[this.thrownExceptions.elementSize];
		this.thrownExceptions.asArray(result);
		return result;
	}

	private void acceptException(ReferenceBinding binding) {
		if (binding != null && binding.isValidBinding()) {
			this.thrownExceptions.add(binding);
		}
	}

	public void endVisit(MessageSend messageSend, BlockScope scope) {
		if (messageSend.binding != null) {
			endVisitMethodInvocation(messageSend.binding);
		}
		super.endVisit(messageSend, scope);
	}

	public void endVisit(AllocationExpression allocationExpression, BlockScope scope) {
		if (allocationExpression.binding != null) {
			endVisitMethodInvocation(allocationExpression.binding);
		}
		super.endVisit(allocationExpression, scope);
	}

	public void endVisit(ThrowStatement throwStatement, BlockScope scope) {
		acceptException((ReferenceBinding)throwStatement.exception.resolvedType);
		super.endVisit(throwStatement, scope);
	}


	private void endVisitMethodInvocation(MethodBinding methodBinding) {
		ReferenceBinding[] thrownExceptionBindings = methodBinding.thrownExceptions;
		int length = thrownExceptionBindings == null ? 0 : thrownExceptionBindings.length;
		for (int i = 0; i < length; i++) {
			acceptException(thrownExceptionBindings[i]);
		}
	}

	public boolean visit(TypeDeclaration typeDeclaration, CompilationUnitScope scope) {
		return visitType(typeDeclaration);
	}

	public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope scope) {
		return visitType(memberTypeDeclaration);
	}

	public boolean visit(TypeDeclaration localTypeDeclaration, BlockScope scope) {
		return visitType(localTypeDeclaration);
	}

	private boolean visitType(TypeDeclaration typeDeclaration) {
		return false;
	}

	public boolean visit(TryStatement tryStatement, BlockScope scope) {
		this.exceptionsStack.push(this.thrownExceptions);
		SimpleSet exceptionSet = new SimpleSet();
		this.thrownExceptions = exceptionSet;
		tryStatement.tryBlock.traverse(this, scope);

		removeCaughtExceptions(tryStatement);

		this.thrownExceptions = (SimpleSet)this.exceptionsStack.pop();

		Object[] values = exceptionSet.values;
		for (int i = 0; i < values.length; i++) {
			if (values[i] != null) {
				this.thrownExceptions.add(values[i]);
			}
		}

		Block[] catchBlocks = tryStatement.catchBlocks;
		int length = catchBlocks == null ? 0 : catchBlocks.length;
		for (int i = 0; i < length; i++) {
			catchBlocks[i].traverse(this, scope);
		}
		return false;
	}

	private void removeCaughtExceptions(TryStatement tryStatement) {
		Argument[] catchArguments = tryStatement.catchArguments;
		int length = catchArguments == null ? 0 : catchArguments.length;
		for (int i = 0; i < length; i++) {
			TypeBinding exception = catchArguments[i].type.resolvedType;
			if (exception != null && exception.isValidBinding()) {
				removeCaughtException((ReferenceBinding)exception);

			}
		}
	}

	private void removeCaughtException(ReferenceBinding caughtException) {
		Object[] exceptions = this.thrownExceptions.values;
		for (int i = 0; i < exceptions.length; i++) {
			ReferenceBinding exception = (ReferenceBinding)exceptions[i];
			if (exception != null) {
				if (exception == caughtException || caughtException.isSuperclassOf(exception)) {
					this.thrownExceptions.remove(exception);
				}
			}
		}
	}
}
