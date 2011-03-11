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
public void acceptAnnotationTypeReference(char[][] typeName, int sourceStart, int sourceEnd) {}
/**
 * acceptAnnotationTypeReference method comment.
 */
public void acceptAnnotationTypeReference(char[] typeName, int sourcePosition) {}
/**
 * acceptConstructorReference method comment.
 */
public void acceptConstructorReference(char[] typeName, int argCount, int sourcePosition) {}
/**
 * acceptFieldReference method comment.
 */
public void acceptFieldReference(char[] fieldName, int sourcePosition) {}
/**
 * acceptImport method comment. 3.7 variant
 */
public void acceptImport(int declarationStart, int declarationEnd, int nameStart, int nameEnd, char[][] tokens, boolean onDemand, int modifiers) {}
/**
 * acceptImport method comment. 3.6 variant
 */
public void acceptImport(int declarationStart, int declarationEnd, char[][] tokens, boolean onDemand, int modifiers) {}
/**
 * acceptLineSeparatorPositions method comment.
 */
public void acceptLineSeparatorPositions(int[] positions) {}
/**
 * acceptMethodReference method comment.
 */
public void acceptMethodReference(char[] methodName, int argCount, int sourcePosition) {}
/**
 * acceptPackage method comment.
 */
public void acceptPackage(ImportReference importReference) {}
/**
 * acceptProblem method comment.
 */
public void acceptProblem(CategorizedProblem problem) {}
/**
 * acceptTypeReference method comment.
 */
public void acceptTypeReference(char[][] typeName, int sourceStart, int sourceEnd) {}
/**
 * acceptTypeReference method comment.
 */
public void acceptTypeReference(char[] typeName, int sourcePosition) {}
/**
 * acceptUnknownReference method comment.
 */
public void acceptUnknownReference(char[][] name, int sourceStart, int sourceEnd) {}
/**
 * acceptUnknownReference method comment.
 */
public void acceptUnknownReference(char[] name, int sourcePosition) {}
/**
 * enterCompilationUnit method comment.
 */
public void enterCompilationUnit() {}
/**
 * enterConstructor method comment.
 */
public void enterConstructor(MethodInfo methodInfo) {}
/**
 * enterField method comment.
 */
public void enterField(FieldInfo fieldInfo) {}
/**
 * enterMethod method comment.
 */
public void enterMethod(MethodInfo methodInfo) {}
/**
 * enterType method comment.
 */
public void enterType(TypeInfo typeInfo) {}
/**
 * exitCompilationUnit method comment.
 */
public void exitCompilationUnit(int declarationEnd) {}
/**
 * exitConstructor method comment.
 */
public void exitConstructor(int declarationEnd) {}
/**
 * exitField method comment.
 */
public void exitField(int initializationStart, int declarationEnd, int declarationSourceEnd) {}
/**
 * exitMethod method comment.
 */
public void exitMethod(int declarationEnd, Expression defaultValue) {}

/**
 * enterInitializer method comment.
 */
public void enterInitializer(int sourceStart, int sourceEnd) {
}

/**
 * exitInitializer method comment.
 */
public void exitInitializer(int sourceEnd) {
}
/**
 * exitType method comment.
 */
public void exitType(int declarationEnd) {}

}
