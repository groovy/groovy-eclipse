/*
 * Copyright 2009-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.jdt.core.groovy.tests.locations;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * Tests that our changes to the ASTConverter do not break Java code.
 *
 * @author Andrew Eisenberg
 * @created Jul 30, 2010
 */
public class ASTConverterTests extends TestCase {

    public static Test suite() {
        return buildTestSuite(ASTConverterTests.class);
    }

    public ASTConverterTests(String name) {
        super(name);
    }

    public void testJavaASTConversionEnum() throws Exception {
        checkJavaName("enum MyNames {\n NAME1(0), NAME2(0);\n private MyNames(int val) { } }", "MyNames");
    }

    public void testJavaASTConversionClass() throws Exception {
        checkJavaName("class MyNames {\n \n private MyNames(int val) { } }", "MyNames");
    }

    public void testJavaASTConversionInterface() throws Exception {
        checkJavaName("interface MyNames {\n \n int myMethod(int val); }", "MyNames");
    }


    private void checkJavaName(String contents, String expectedName) {
        int start = contents.lastIndexOf(expectedName);
        int length = expectedName.length();
        SimpleName name = (SimpleName) findJavaNodeAt(contents, start, length);
        assertEquals(expectedName, name.getIdentifier());
        assertEquals("Invalid start position", start, name.getStartPosition());
        assertEquals("Invalid length position", length, name.getLength());
    }

    public static int astlevel = -1;

    public static int getAstLevel() {
        if (astlevel == -1) {
            astlevel = AST.JLS3;
            try {
                AST.class.getDeclaredField("JLS8");
                astlevel = 8;
            } catch (NoSuchFieldException nsfe) {
                // pre-java8
            }
        }
        return astlevel;
    }

    private ASTNode findJavaNodeAt(String contents, int start, int length) {
        ASTParser parser = ASTParser.newParser(getAstLevel());
        Map<String, String> options = JavaCore.getOptions();
        options.put(CompilerOptions.OPTION_Source, "1.5");
        parser.setCompilerOptions(options);
        parser.setSource(contents.toCharArray());
        parser.setStatementsRecovery(true);
        CompilationUnit unit = (CompilationUnit) parser.createAST(null);
        assertEquals("Compilation unit should not have any problems:\n" + printProblems(unit), 0, unit.getProblems().length);

        return NodeFinder.perform(unit, start, length);
    }

    private String printProblems(CompilationUnit unit) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < unit.getProblems().length; i++) {
            sb.append(unit.getProblems()[i]);
            sb.append("\n");
        }
        return sb.toString();
    }
}
