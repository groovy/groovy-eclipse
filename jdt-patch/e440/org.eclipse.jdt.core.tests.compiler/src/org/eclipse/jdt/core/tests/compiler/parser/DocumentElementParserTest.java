/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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

import java.util.Locale;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.internal.compiler.DocumentElementParser;
import org.eclipse.jdt.internal.compiler.IDocumentElementRequestor;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

public class DocumentElementParserTest extends AbstractCompilerTest {
public DocumentElementParserTest(String testName) {
	super(testName);
}
public void reset() {
}
public void test01() {

	String s =
		"public class X {\n" +
		"public String getTexts(int i) [] {\n" +
		"		 String[] texts = new String[1];\n" +
		"		 return texts; \n" +
		"}\n" +
		"}";

	String testName = "test01";

	char[] source = s.toCharArray();
	reset();
	DocumentElementParser parser =
		new DocumentElementParser(new IDocumentElementRequestor() {
			public void acceptImport(int declarationStart, int declarationEnd, int[] javaDocPositions, char[] name, int nameStartPosition, boolean onDemand, int modifiers) {
			}
			public void acceptInitializer(int declarationStart, int declarationEnd, int[] javaDocPositions, int modifiers, int modifiersStart, int bodyStart, int bodyEnd) {
			}
			public void acceptLineSeparatorPositions(int[] positions) {
			}
			public void acceptPackage(int declarationStart, int declarationEnd, int[] javaDocPositions, char[] name, int nameStartPosition) {
			}
			public void acceptProblem(CategorizedProblem problem) {
			}
			public void enterClass(int declarationStart, int[] javaDocPositions, int modifiers, int modifiersStart, int classStart, char[] name, int nameStart, int nameEnd, char[] superclass, int superclassStart, int superclassEnd, char[][] superinterfaces, int[] superinterfaceStarts, int[] superinterfaceEnds, int bodyStart) {
			}
			public void enterCompilationUnit() {
			}
			public void enterConstructor(int declarationStart, int[] javaDocPositions, int modifiers, int modifiersStart, char[] name, int nameStart, int nameEnd, char[][] parameterTypes, int[] parameterTypeStarts, int[] parameterTypeEnds, char[][] parameterNames, int[] parameterNameStarts, int[] parameterNameEnds, int parametersEnd, char[][] exceptionTypes, int[] exceptionTypeStarts, int[] exceptionTypeEnds, int bodyStart) {
			}
			public void enterField(int declarationStart, int[] javaDocPositions, int modifiers, int modifiersStart, char[] type, int typeStart, int typeEnd, int typeDimensionCount, char[] name, int nameStart, int nameEnd, int extendedTypeDimensionCount, int extendedTypeDimensionEnd) {
			}
			public void enterInterface(int declarationStart, int[] javaDocPositions, int modifiers, int modifiersStart, int interfaceStart, char[] name, int nameStart, int nameEnd, char[][] superinterfaces, int[] superinterfaceStarts, int[] superinterfaceEnds, int bodyStart) {
			}
			public void exitClass(int bodyEnd, int declarationEnd) {
			}
			public void exitCompilationUnit(int declarationEnd) {
			}
			public void exitConstructor(int bodyEnd, int declarationEnd) {
			}
			public void exitField(int bodyEnd, int declarationEnd) {
			}
			public void exitInterface(int bodyEnd, int declarationEnd) {
			}
			public void exitMethod(int bodyEnd, int declarationEnd) {
			}
			public void enterMethod(int declarationStart,int[] javaDocPositions,int modifiers,int modifiersStart,char[] returnType,int returnTypeStart,int returnTypeEnd,int returnTypeDimensionCount,char[] name,int nameStart,int nameEnd,char[][] parameterTypes,int[] parameterTypeStarts,int[] parameterTypeEnds,char[][] parameterNames,int[] parameterNameStarts,int[] parameterNameEnds,int parametersEnd,int extendedReturnTypeDimensionCount,int extendedReturnTypeDimensionEnd,char[][] exceptionTypes,int[] exceptionTypeStarts,int[] exceptionTypeEnds,int bodyStart) {
				if (CharOperation.equals(name, "getTexts".toCharArray())) {
					assertEquals("Wrong return type", "String[]", new String(returnType));
				}
			}
		}, new DefaultProblemFactory(Locale.getDefault()), new CompilerOptions(getCompilerOptions()));

	ICompilationUnit sourceUnit = new CompilationUnit(source, testName, null);

	parser.parseCompilationUnit(sourceUnit);
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.IDocumentElementRequestor#acceptImport(int, int, int[], char[], int, boolean, int)
 */
public void acceptImport(int declarationStart, int declarationEnd, int[] javaDocPositions, char[] name, int nameStartPosition, boolean onDemand, int modifiers) {
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.IDocumentElementRequestor#acceptInitializer(int, int, int[], int, int, int, int)
 */
public void acceptInitializer(int declarationStart, int declarationEnd, int[] javaDocPositions, int modifiers, int modifiersStart, int bodyStart, int bodyEnd) {
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.IDocumentElementRequestor#acceptLineSeparatorPositions(int[])
 */
public void acceptLineSeparatorPositions(int[] positions) {
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.IDocumentElementRequestor#acceptPackage(int, int, int[], char[], int)
 */
public void acceptPackage(int declarationStart, int declarationEnd, int[] javaDocPositions, char[] name, int nameStartPosition) {
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.IDocumentElementRequestor#acceptProblem(org.eclipse.jdt.core.compiler.IProblem)
 */
public void acceptProblem(CategorizedProblem problem) {
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.IDocumentElementRequestor#enterClass(int, int[], int, int, int, char[], int, int, char[], int, int, char[][], int[], int[], int)
 */
public void enterClass(int declarationStart, int[] javaDocPositions, int modifiers, int modifiersStart, int classStart, char[] name, int nameStart, int nameEnd, char[] superclass, int superclassStart, int superclassEnd, char[][] superinterfaces, int[] superinterfaceStarts, int[] superinterfaceEnds, int bodyStart) {
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.IDocumentElementRequestor#enterCompilationUnit()
 */
public void enterCompilationUnit() {
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.IDocumentElementRequestor#enterConstructor(int, int[], int, int, char[], int, int, char[][], int[], int[], char[][], int[], int[], int, char[][], int[], int[], int)
 */
public void enterConstructor(int declarationStart, int[] javaDocPositions, int modifiers, int modifiersStart, char[] name, int nameStart, int nameEnd, char[][] parameterTypes, int[] parameterTypeStarts, int[] parameterTypeEnds, char[][] parameterNames, int[] parameterNameStarts, int[] parameterNameEnds, int parametersEnd, char[][] exceptionTypes, int[] exceptionTypeStarts, int[] exceptionTypeEnds, int bodyStart) {
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.IDocumentElementRequestor#enterField(int, int[], int, int, char[], int, int, int, char[], int, int, int, int)
 */
public void enterField(int declarationStart, int[] javaDocPositions, int modifiers, int modifiersStart, char[] type, int typeStart, int typeEnd, int typeDimensionCount, char[] name, int nameStart, int nameEnd, int extendedTypeDimensionCount, int extendedTypeDimensionEnd) {
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.IDocumentElementRequestor#enterInterface(int, int[], int, int, int, char[], int, int, char[][], int[], int[], int)
 */
public void enterInterface(int declarationStart, int[] javaDocPositions, int modifiers, int modifiersStart, int interfaceStart, char[] name, int nameStart, int nameEnd, char[][] superinterfaces, int[] superinterfaceStarts, int[] superinterfaceEnds, int bodyStart) {
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.IDocumentElementRequestor#enterMethod(int, int[], int, int, char[], int, int, int, char[], int, int, char[][], int[], int[], char[][], int[], int[], int, int, int, char[][], int[], int[], int)
 */
public void enterMethod(int declarationStart, int[] javaDocPositions, int modifiers, int modifiersStart, char[] returnType, int returnTypeStart, int returnTypeEnd, int returnTypeDimensionCount, char[] name, int nameStart, int nameEnd, char[][] parameterTypes, int[] parameterTypeStarts, int[] parameterTypeEnds, char[][] parameterNames, int[] parameterNameStarts, int[] parameterNameEnds, int parametersEnd, int extendedReturnTypeDimensionCount, int extendedReturnTypeDimensionEnd, char[][] exceptionTypes, int[] exceptionTypeStarts, int[] exceptionTypeEnds, int bodyStart) {
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.IDocumentElementRequestor#exitClass(int, int)
 */
public void exitClass(int bodyEnd, int declarationEnd) {
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.IDocumentElementRequestor#exitCompilationUnit(int)
 */
public void exitCompilationUnit(int declarationEnd) {
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.IDocumentElementRequestor#exitConstructor(int, int)
 */
public void exitConstructor(int bodyEnd, int declarationEnd) {
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.IDocumentElementRequestor#exitField(int, int)
 */
public void exitField(int bodyEnd, int declarationEnd) {
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.IDocumentElementRequestor#exitInterface(int, int)
 */
public void exitInterface(int bodyEnd, int declarationEnd) {
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.IDocumentElementRequestor#exitMethod(int, int)
 */
public void exitMethod(int bodyEnd, int declarationEnd) {
}
}
