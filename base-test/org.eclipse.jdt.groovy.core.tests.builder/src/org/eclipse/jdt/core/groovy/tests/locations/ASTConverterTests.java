/*
 * Copyright 2009-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.jdt.core.groovy.tests.locations;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.groovy.core.util.JavaConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.junit.Test;

/**
 * Tests that our changes to the ASTConverter do not break Java code.
 */
public final class ASTConverterTests {

    private static void checkJavaName(String contents, String expectedName) {
        int start = contents.lastIndexOf(expectedName);
        int length = expectedName.length();
        SimpleName name = (SimpleName) findJavaNodeAt(contents, start, length);
        assertEquals(expectedName, name.getIdentifier());
        assertEquals("Invalid start position", start, name.getStartPosition());
        assertEquals("Invalid length position", length, name.getLength());
    }

    private static ASTNode findJavaNodeAt(String contents, int start, int length) {
        ASTParser parser = ASTParser.newParser(JavaConstants.AST_LEVEL);
        Map<String, String> options = JavaCore.getOptions();
        options.put(CompilerOptions.OPTION_Source, "1.5");
        parser.setCompilerOptions(options);
        parser.setSource(contents.toCharArray());
        parser.setStatementsRecovery(true);
        CompilationUnit unit = (CompilationUnit) parser.createAST(null);
        assertEquals("Compilation unit should not have any problems:\n" + printProblems(unit), 0, unit.getProblems().length);
        return NodeFinder.perform(unit, start, length);
    }

    private static String printProblems(CompilationUnit unit) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < unit.getProblems().length; i++) {
            sb.append(unit.getProblems()[i]);
            sb.append("\n");
        }
        return sb.toString();
    }

    //--------------------------------------------------------------------------

    @Test
    public void testJavaASTConversionEnum() throws Exception {
        checkJavaName("enum MyNames {\n NAME1(0), NAME2(0);\n private MyNames(int val){}\n}", "MyNames");
    }

    @Test
    public void testJavaASTConversionClass() throws Exception {
        checkJavaName("class MyNames {\n private MyNames(int val){}\n}", "MyNames");
    }

    @Test
    public void testJavaASTConversionInterface() throws Exception {
        checkJavaName("interface MyNames {\n int myMethod(int val);\n}", "MyNames");
    }
}
