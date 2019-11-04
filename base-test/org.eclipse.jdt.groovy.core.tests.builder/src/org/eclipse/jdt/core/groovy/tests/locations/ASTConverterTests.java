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
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.JavaCore;
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
        ASTParser parser = ASTParser.newParser(JavaConstants.AST_LEVEL);
        Map<String, String> options = JavaCore.getOptions();
        options.put(CompilerOptions.OPTION_Source, "1.5");
        parser.setCompilerOptions(options);
        parser.setSource(contents.toCharArray());
        parser.setStatementsRecovery(true);
        CompilationUnit unit = (CompilationUnit) parser.createAST(null);

        if (unit.getProblems().length > 0) {
            fail("Compilation unit should not have any problems:\n" +
                Arrays.stream(unit.getProblems()).map(Object::toString).collect(Collectors.joining("\n")));
        }

        int start = contents.lastIndexOf(expectedName), length = expectedName.length();
        SimpleName name = (SimpleName) NodeFinder.perform(unit, start, length);
        assertEquals(expectedName, name.getIdentifier());
        assertEquals("Invalid start position", start, name.getStartPosition());
        assertEquals("Invalid length position", length, name.getLength());
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
