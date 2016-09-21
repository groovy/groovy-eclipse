/*
 * Copyright 2003-2014 the original author or authors.
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
package org.eclipse.jdt.groovy.core.tests.basic;

import junit.framework.Test;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyParser;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

/**
 * Tests error recovery from the parser. All tests in this class are failing, so
 * do not add to build.  When a test starts passing, remove from this class.
 *
 * @author Andrew Eisenberg
 * @created May 17, 2011
 */
public class ErrorRecoveryTests extends AbstractGroovyRegressionTest {

    public static Test suite() {
        return buildUniqueComplianceTestSuite(testClass(), ClassFileConstants.JDK1_5);
    }

    public static Class testClass() {
        return ErrorRecoveryTests.class;
    }

    public ErrorRecoveryTests(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        complianceLevel = ClassFileConstants.JDK1_5;
    }

    private ModuleNode getModuleNode(String filename) {
        GroovyCompilationUnitDeclaration decl = (GroovyCompilationUnitDeclaration) ((DebugRequestor) GroovyParser.debugRequestor).declarations.get(filename);
        if (decl != null) {
            return decl.getModuleNode();
        } else {
            return null;
        }
    }

    //

    public void testParsingIncompleteIfCondition_1046() throws Exception {
        this.runNegativeTest(new String[]{
                "A.groovy",
                "File f = new File('c:\\test')\n" +
                "if (f.isD"
        },
        //"----------\n" +
        //"1. ERROR in A.groovy (at line 2)\n" +
        //"\tif (f.isD\n" +
        //"\t        ^\n" +
        //"Groovy:expecting ')', found '' @ line 2, column 9.\n" +
        //"----------\n" +
        "");
        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());
        ClassNode cn = (ClassNode) mn.getClasses().get(0);
        assertNotNull(cn);
        assertTrue(cn.getName().equals("A"));
    }

    public void testParsingDotTerminatingIncompleteIfCondition_1046() throws Exception {
        this.runNegativeTest(new String[]{
                "A.groovy",
                "File f = new File('c:\\test')\n" +
                "if (f."
        },
        "----------\n" +
        "1. ERROR in A.groovy (at line 2)\n" +
        "\tif (f.\n" +
        "\t     ^\n" +
        "Groovy:Expecting an identifier, found a trailing '.' instead. @ line 2, column 6.\n" +
        //"----------\n" +
        //"2. ERROR in A.groovy (at line 2)\n" +
        //"\tif (f.\n" +
        //"\t     ^\n" +
        //"Groovy:expecting ')', found '' @ line 2, column 6.\n" +
        "----------\n");
        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());
        ClassNode cn = (ClassNode) mn.getClasses().get(0);
        assertNotNull(cn);
        assertTrue(cn.getName().equals("A"));
    }

    public void testGRE941() throws Exception {
        this.runNegativeTest(new String[]{
                "A.groovy",
                "class Foo {\n" +
                "  def myMethod() {\n" +
                "    def x = \"\"\n" +
                "    println x.;\n" + // if a ';' is inserted in this situation, you can get back what you want...
                "    println x\n" +
                "  }\n" +
                "}"
        },
        "----------\n" +
        "1. ERROR in A.groovy (at line 4)\n" +
        "\tprintln x.;\n" +
        "\t          ^\n" +
        "Groovy:unexpected token: ; @ line 4, column 15.\n" +
        "----------\n");
        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());
        ClassNode cn = (ClassNode) mn.getClasses().get(0);
        assertNotNull(cn);
        assertEquals("Foo",cn.getName());
    }

    public void testGRE644() throws Exception {
        this.runNegativeTest(new String[]{
                "Other.groovy",
                "class Test {\n" +
                "    static String CONST = \"hello\";\n" +
                "    static double addOne(double a) { return a + 1.0; }\n" +
                "}",
                "A.groovy",
                "Test."
        },
        "----------\n" +
        "1. ERROR in A.groovy (at line 1)\n" +
        "\tTest.\n" +
        "\t    ^\n" +
        "Groovy:Expecting an identifier, found a trailing '.' instead. @ line 1, column 5.\n" +
        "----------\n");
        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());
        ClassNode cn = (ClassNode) mn.getClasses().get(0);
        assertNotNull(cn);
        assertTrue(cn.getName().equals("A"));
    }

    public void testGRE1048() throws Exception {
        this.runNegativeTest(new String[]{
                "A.groovy",
                "class TextCompletionTest { \n" +
                "    String getBla(){\n" +
                "        return \"bla\"\n" +
                "    }\n" +
                "    \n" +
                "    static void foo(){\n" +
                "        TextCompletionTest variable = new TextCompletionTest()\n" +
                "        println variable.bla //works\n" +
                "        println(variable.)\n" +
                "    }\n" +
                "}",
        },
        "----------\n" +
        "1. ERROR in A.groovy (at line 9)\n" +
        "\tprintln(variable.)\n" +
        "\t                 ^\n" +
        "Groovy:unexpected token: ) @ line 9, column 26.\n" +
        "----------\n");
        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());
        ClassNode cn = (ClassNode) mn.getClasses().get(0);
        assertNotNull(cn);
        assertTrue(cn.getName().equals("TextCompletionTest"));
    }
}
