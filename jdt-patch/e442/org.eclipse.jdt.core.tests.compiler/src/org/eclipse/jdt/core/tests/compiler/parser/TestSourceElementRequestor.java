/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;

public class TestSourceElementRequestor implements ISourceElementRequestor {
/**
 * DummySourceElementRequestor constructor comment.
 */
public TestSourceElementRequestor() {
	super();
}
/**
 * acceptAnnotationTypeReference method comment.
 */
@Override
public void acceptAnnotationTypeReference(char[][] typeName, int sourceStart, int sourceEnd) {}
/**
 * acceptAnnotationTypeReference method comment.
 */
@Override
public void acceptAnnotationTypeReference(char[] typeName, int sourcePosition) {}
/**
 * acceptConstructorReference method comment.
 */
@Override
public void acceptConstructorReference(char[] typeName, int argCount, int sourcePosition) {}
/**
 * acceptFieldReference method comment.
 */
@Override
public void acceptFieldReference(char[] fieldName, int sourcePosition) {}
/**
 * acceptImport method comment.
 */
@Override
public void acceptImport(int declarationStart, int declarationEnd, int nameStart, int nameEnd, char[][] tokens, boolean onDemand, int modifiers) {}
/**
 * acceptLineSeparatorPositions method comment.
 */
@Override
public void acceptLineSeparatorPositions(int[] positions) {}
/**
 * acceptMethodReference method comment.
 */
@Override
public void acceptMethodReference(char[] methodName, int argCount, int sourcePosition) {}
/**
 * acceptPackage method comment.
 */
@Override
public void acceptPackage(ImportReference importReference) {}
/**
 * acceptProblem method comment.
 */
@Override
public void acceptProblem(CategorizedProblem problem) {}
/**
 * acceptTypeReference method comment.
 */
@Override
public void acceptTypeReference(char[][] typeName, int sourceStart, int sourceEnd) {}
/**
 * acceptTypeReference method comment.
 */
@Override
public void acceptTypeReference(char[] typeName, int sourcePosition) {}
/**
 * acceptUnknownReference method comment.
 */
@Override
public void acceptUnknownReference(char[][] name, int sourceStart, int sourceEnd) {}
/**
 * acceptUnknownReference method comment.
 */
@Override
public void acceptUnknownReference(char[] name, int sourcePosition) {}
/**
 * enterCompilationUnit method comment.
 */
@Override
public void enterCompilationUnit() {}
/**
 * enterConstructor method comment.
 */
@Override
public void enterConstructor(MethodInfo methodInfo) {}
/**
 * enterField method comment.
 */
@Override
public void enterField(FieldInfo fieldInfo) {}
/**
 * enterMethod method comment.
 */
@Override
public void enterMethod(MethodInfo methodInfo) {}
/**
 * enterType method comment.
 */
@Override
public void enterType(TypeInfo typeInfo) {}
/**
 * exitCompilationUnit method comment.
 */
@Override
public void exitCompilationUnit(int declarationEnd) {}
/**
 * exitConstructor method comment.
 */
@Override
public void exitConstructor(int declarationEnd) {}
/**
 * exitField method comment.
 */
@Override
public void exitField(int initializationStart, int declarationEnd, int declarationSourceEnd) {}
/**
 * exitMethod method comment.
 */
@Override
public void exitMethod(int declarationEnd, Expression defaultValue) {}

/**
 * enterInitializer method comment.
 */
@Override
public void enterInitializer(int sourceStart, int sourceEnd) {
}

/**
 * exitInitializer method comment.
 */
@Override
public void exitInitializer(int sourceEnd) {
}
/**
 * exitType method comment.
 */
@Override
public void exitType(int declarationEnd) {}

}
