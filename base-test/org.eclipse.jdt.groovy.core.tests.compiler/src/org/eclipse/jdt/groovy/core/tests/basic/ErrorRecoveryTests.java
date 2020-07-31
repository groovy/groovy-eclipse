/*
 * Copyright 2009-2020 the original author or authors.
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
package org.eclipse.jdt.groovy.core.tests.basic;

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;
import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isParrotParser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.junit.Test;

/**
 * Tests groovy parser error recovery enhancements.
 */
public final class ErrorRecoveryTests extends GroovyCompilerTestSuite {

    @Test // syntax error in one method should not impact outer class structure
    public void testParsingRecovery_BasicBlock() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "public class X {\n" +
            "  int foo\n" +
            "  void bar() {\n" +
            "    def err {\n" +
            "  }\n" +
            "  def baz() {\n" +
            "    def good = { ->\n" +
            "    }\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 4)\n" +
            "\tdef err {\n" +
            "\t    ^\n" +
            "Groovy:unexpected token: err\n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  private int foo;\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "  public void bar() {\n" +
            "  }\n" +
            "  public java.lang.Object baz() {\n" +
            "    java.lang.Object good;\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_IncompleteAssignment1() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "int err = ",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 1)\n" +
            "\tint err = \n" +
            "\t         ^\n" +
            "Groovy:unexpected token: \n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "public class X extends groovy.lang.Script {\n" +
            "  public X() {\n" +
            "  }\n" +
            "  public X(final groovy.lang.Binding context) {\n" +
            "  }\n" +
            "  public static void main(final java.lang.String... args) {\n" +
            "  }\n" +
            "  public @java.lang.Override java.lang.Object run() {\n" +
            "    int err;\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_IncompleteAssignment1a() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "int err\n" +
            "err = ",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 2)\n" +
            "\terr = \n" +
            "\t     ^\n" +
            "Groovy:unexpected token: \n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "public class X extends groovy.lang.Script {\n" +
            "  public X() {\n" +
            "  }\n" +
            "  public X(final groovy.lang.Binding context) {\n" +
            "  }\n" +
            "  public static void main(final java.lang.String... args) {\n" +
            "  }\n" +
            "  public @java.lang.Override java.lang.Object run() {\n" +
            "    int err;\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_IncompleteAssignment1b() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "int err\n" +
            "err *= ",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 2)\n" +
            "\terr *= \n" +
            "\t      ^\n" +
            "Groovy:unexpected token: \n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "public class X extends groovy.lang.Script {\n" +
            "  public X() {\n" +
            "  }\n" +
            "  public X(final groovy.lang.Binding context) {\n" +
            "  }\n" +
            "  public static void main(final java.lang.String... args) {\n" +
            "  }\n" +
            "  public @java.lang.Override java.lang.Object run() {\n" +
            "    int err;\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_IncompleteAssignment1c() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "int err\n" +
            "err **= ",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 2)\n" +
            "\terr **= \n" +
            "\t       ^\n" +
            "Groovy:unexpected token: \n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "public class X extends groovy.lang.Script {\n" +
            "  public X() {\n" +
            "  }\n" +
            "  public X(final groovy.lang.Binding context) {\n" +
            "  }\n" +
            "  public static void main(final java.lang.String... args) {\n" +
            "  }\n" +
            "  public @java.lang.Override java.lang.Object run() {\n" +
            "    int err;\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_IncompleteAssignment2() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "public class X {\n" +
            "  int err = \n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 3)\n" +
            "\t}\n" +
            "\t^\n" +
            "Groovy:unexpected token: " + "}\n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  private int err;\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_IncompleteAssignment2a() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "public class X {\n" +
            "  int err\n" +
            "  X() {\n" +
            "    err = \n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 5)\n" +
            "\t}\n" +
            "\t^\n" +
            "Groovy:unexpected token: " + "}\n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  private int err;\n" +
            "  public X() {\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_IncompleteAssignment3() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "public class X {\n" +
            "  void bar() {\n" +
            "    int err = \n" +
            "  }\n" +
            "  def baz() {\n" +
            "    def good = { ->\n" +
            "    }\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 4)\n" +
            "\t}\n" +
            "\t^\n" +
            "Groovy:unexpected token: " + "}\n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "  public void bar() {\n" +
            "    int err;\n" +
            "  }\n" +
            "  public java.lang.Object baz() {\n" +
            "    java.lang.Object good;\n" +
            "  }\n" +
            "}\n");
    }

    @Test // previous version of assignment recovery was trying to add missing identifier for error within init expression
    public void testParsingRecovery_IncompleteAssignment4() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "public class X {\n" +
            "  void bar() {\n" +
            "    int good = {\n" +
            "      \"\n" + // err
            "    }\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 4)\n" +
            "\t\"\n" +
            "    }\n" +
            "\t ^\n" +
            "Groovy:expecting anything but ''\\n''; got it anyway\n" +
            "----------\n");

        /*checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  public X() {\n" +
            "  }\n" +
            "  public void bar() {\n" +
            "  }\n" +
            "}\n");*/
    }

    @Test
    public void testParsingRecovery_IncompleteIfCondition1() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "File f = new File('c:\\test')\n" +
            "if (f.isD",
        };
        //@formatter:off

        runConformTest(sources, "", "groovy.lang.MissingPropertyException: No such property: isD for class: java.io.File\n");

        ModuleNode mn = getModuleNode("X.groovy");
        assertFalse(mn.encounteredUnrecoverableError());
        assertEquals("X", mn.getClasses().get(0).getName());
    }

    @Test
    public void testParsingRecovery_IncompleteIfCondition2() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "File f = new File('c:\\test')\n" +
            "if (f.",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 2)\n" +
            "\tif (f.\n" +
            "\t     ^\n" +
            "Groovy:unexpected token: \n" +
            "----------\n");

        ModuleNode mn = getModuleNode("X.groovy");
        assertFalse(mn.encounteredUnrecoverableError());
        assertEquals("X", mn.getClasses().get(0).getName());
    }

    @Test
    public void testParsingRecovery_IncompleteRangeExpression1() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "0..\n" +
            "",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 1)\n" +
            "\t0..\n\n" +
            "\t   ^\n" +
            "Groovy:unexpected token: \n" +
            "----------\n");

        assertFalse(getModuleNode("X.groovy").encounteredUnrecoverableError());
        checkGCUDeclaration("X.groovy",
            "public class X extends groovy.lang.Script {\n" +
            "  public X() {\n" +
            "  }\n" +
            "  public X(final groovy.lang.Binding context) {\n" +
            "  }\n" +
            "  public static void main(final java.lang.String... args) {\n" +
            "  }\n" +
            "  public @java.lang.Override java.lang.Object run() {\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_IncompleteRangeExpression2() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "0..<\n" +
            "",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 1)\n" +
            "\t0..<\n\n" +
            "\t    ^\n" +
            "Groovy:unexpected token: \n" +
            "----------\n");

        assertFalse(getModuleNode("X.groovy").encounteredUnrecoverableError());
        checkGCUDeclaration("X.groovy",
            "public class X extends groovy.lang.Script {\n" +
            "  public X() {\n" +
            "  }\n" +
            "  public X(final groovy.lang.Binding context) {\n" +
            "  }\n" +
            "  public static void main(final java.lang.String... args) {\n" +
            "  }\n" +
            "  public @java.lang.Override java.lang.Object run() {\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_IncompleteRangeExpression3() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "for (i in 0..) ;\n" +
            "",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 1)\n" +
            "\tfor (i in 0..) ;\n" +
            "\t             ^\n" +
            "Groovy:unexpected token: )\n" +
            "----------\n");

        assertFalse(getModuleNode("X.groovy").encounteredUnrecoverableError());
        checkGCUDeclaration("X.groovy",
            "public class X extends groovy.lang.Script {\n" +
            "  public X() {\n" +
            "  }\n" +
            "  public X(final groovy.lang.Binding context) {\n" +
            "  }\n" +
            "  public static void main(final java.lang.String... args) {\n" +
            "  }\n" +
            "  public @java.lang.Override java.lang.Object run() {\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_IncompleteRangeExpression4() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "for (i in 0..<) ;\n" +
            "",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 1)\n" +
            "\tfor (i in 0..<) ;\n" +
            "\t              ^\n" +
            "Groovy:unexpected token: )\n" +
            "----------\n");

        assertFalse(getModuleNode("X.groovy").encounteredUnrecoverableError());
        checkGCUDeclaration("X.groovy",
            "public class X extends groovy.lang.Script {\n" +
            "  public X() {\n" +
            "  }\n" +
            "  public X(final groovy.lang.Binding context) {\n" +
            "  }\n" +
            "  public static void main(final java.lang.String... args) {\n" +
            "  }\n" +
            "  public @java.lang.Override java.lang.Object run() {\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_IncompleteRangeExpression5() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X {\n" +
            "  def y() {\n" +
            "    0..\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 4)\n" +
            "\t}\n" +
            "\t^\n" +
            "Groovy:unexpected token: " + "}\n" +
            "----------\n");

        assertFalse(getModuleNode("X.groovy").encounteredUnrecoverableError());
        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "  public java.lang.Object y() {\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_IncompleteRangeExpression6() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X {\n" +
            "  def y() {\n" +
            "    0..<\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 4)\n" +
            "\t}\n" +
            "\t^\n" +
            "Groovy:unexpected token: " + "}\n" +
            "----------\n");

        assertFalse(getModuleNode("X.groovy").encounteredUnrecoverableError());
        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "  public java.lang.Object y() {\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_IncompleteRangeExpression7() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X {\n" +
            "  def y() {\n" +
            "    for (i in 0..) ;\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 3)\n" +
            "\tfor (i in 0..) ;\n" +
            "\t             ^\n" +
            "Groovy:unexpected token: )\n" +
            "----------\n");

        assertFalse(getModuleNode("X.groovy").encounteredUnrecoverableError());
        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "  public java.lang.Object y() {\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_IncompleteRangeExpression8() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X {\n" +
            "  def y() {\n" +
            "    for (i in 0..<) ;\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 3)\n" +
            "\tfor (i in 0..<) ;\n" +
            "\t              ^\n" +
            "Groovy:unexpected token: )\n" +
            "----------\n");

        assertFalse(getModuleNode("X.groovy").encounteredUnrecoverableError());
        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "  public java.lang.Object y() {\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_IncompleteRangeExpression9() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X {\n" +
            "  def y() {\n" +
            "    def range = 0g..\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 4)\n" +
            "\t}\n" +
            "\t^\n" +
            "Groovy:unexpected token: " + "}\n" +
            "----------\n");

        assertFalse(getModuleNode("X.groovy").encounteredUnrecoverableError());
        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "  public java.lang.Object y() {\n" +
            "    java.lang.Object range;\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_GRE941() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X {\n" +
            "  def myMethod() {\n" +
            "    def x = \"\"\n" +
            "    println x.;\n" + // if a ';' is inserted in this situation, you can get back what you want...
            "    println x\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 4)\n" +
            "\tprintln x.;\n" +
            "\t          ^\n" +
            "Groovy:unexpected token: ;\n" +
            "----------\n");

        ModuleNode mn = getModuleNode("X.groovy");
        assertFalse(mn.encounteredUnrecoverableError());
        assertEquals("X", mn.getClasses().get(0).getName());
    }

    @Test
    public void testParsingRecovery_GRE644() {
        //@formatter:off
        String[] sources = {
            "Y.groovy",
            "class Test {\n" +
            "    static String CONST = \"hello\";\n" +
            "    static double addOne(double a) { return a + 1.0; }\n" +
            "}",
            "X.groovy",
            "Test.",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 1)\n" +
            "\tTest.\n" +
            "\t    ^\n" +
            "Groovy:unexpected token: \n" +
            "----------\n");

        ModuleNode mn = getModuleNode("X.groovy");
        assertFalse(mn.encounteredUnrecoverableError());
        assertEquals("X", mn.getClasses().get(0).getName());
    }

    @Test
    public void testParsingRecovery_GRE1048() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X { \n" +
            "    String getBla(){\n" +
            "        return \"bla\"\n" +
            "    }\n" +
            "    \n" +
            "    static void foo(){\n" +
            "        X variable = new X()\n" +
            "        println variable.bla //works\n" +
            "        println(variable.)\n" +
            "    }\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 9)\n" +
            "\tprintln(variable.)\n" +
            "\t                 ^\n" +
            "Groovy:unexpected token: )\n" +
            "----------\n");

        ModuleNode mn = getModuleNode("X.groovy");
        assertFalse(mn.encounteredUnrecoverableError());
        assertEquals("X", mn.getClasses().get(0).getName());
    }

    @Test
    public void testParsingRecovery_GRE1085_1() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "package foo\n" +
            "\n" +
            "class X {\n" +
            "  public void foo() {\n" +
            "    foo:\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 6)\n" +
            "\t}\n" +
            "\t^\n" +
            "Groovy:unexpected token: " + "}\n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "package foo;\n" +
            "public class X {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "  public void foo() {\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_GRE1085_2() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "package foo\n" +
            "\n" +
            "class X {\n" +
            "  public void foo() {\n" +
            "    foo:\"abc\",\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 5)\n" +
            "\tfoo:\"abc\",\n" +
            "\t    ^\n" +
            "Groovy:unexpected token: abc\n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "package foo;\n" +
            "public class X {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "  public void foo() {\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_GRE1085_3() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "package foo\n" +
            "\n" +
            "class X {\n" +
            "  public void foo() {\n" +
            "    foo:,\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 5)\n" +
            "\tfoo:,\n" +
            "\t    ^\n" +
            "Groovy:unexpected token: ,\n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "package foo;\n" +
            "public class X {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "  public void foo() {\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_GRE1192_1() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X {\n" +
            "\tdef m() {\n" +
            "\t}\n" +
            "\tdef x = {\n" +
            "\t  nuthin s,\n" +
            "\t}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 6)\n" +
            "\t}\n" +
            "\t^\n" +
            "Groovy:unexpected token: " + "}\n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  private java.lang.Object x;\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "  public java.lang.Object m() {\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_GRE1046_1() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "File f = new File();\n" +
            "if (f.)\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 2)\n" +
            "\tif (f.)\n" +
            "\t      ^\n" +
            "Groovy:unexpected token: )\n" +
            "----------\n" +
            "2. ERROR in X.groovy (at line 2)\n" +
            "\tif (f.)\n" +
            "\n" +
            "\t       ^\n" +
            "Groovy:unexpected token: \n" +
            "----------\n");
    }

    @Test
    public void testParsingRecovery_GRE1046_2() {
        // trickier than above, this is also missing the closing paren
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "File f = new File();\n" +
            "if (f.\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 2)\n" +
            "\tif (f.\n" +
            "\n" +
            "\t      ^\n" +
            "Groovy:unexpected token: \n" +
            "----------\n");
    }

    @Test
    public void testParsingRecovery_GRE1213_1() {
        // missing close paren
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "import java.awt.BorderLayout;\n" +
            "panel.add (textField, BorderLayout.\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 2)\n" +
            "\tpanel.add (textField, BorderLayout.\n" +
            "\n" +
            "\t                                   ^\n" +
            "Groovy:unexpected token: \n" +
            "----------\n" +
            "2. ERROR in X.groovy (at line 2)\n" +
            "\tpanel.add (textField, BorderLayout.\n" +
            "\n" +
            "\t                                   ^\n" +
            "Groovy:expecting \')\', found \'\'\n" +
            "----------\n");
    }

    @Test
    public void testParsingRecovery_GRE1213_2() {
        // missing close paren
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "import java.awt.BorderLayout;\n" +
            "String s = ('foo' + BorderLayout.\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 2)\n" +
            "\tString s = (\'foo\' + BorderLayout.\n" +
            "\n" +
            "\t                                 ^\n" +
            "Groovy:unexpected token: \n" +
            "----------\n" +
            "2. ERROR in X.groovy (at line 2)\n" +
            "\tString s = (\'foo\' + BorderLayout.\n" +
            "\n" +
            "\t                                 ^\n" +
            "Groovy:expecting \')\', found \'\'\n" +
            "----------\n");
    }

    @Test
    public void testParsingRecovery_GRE1107_1() {
        //@formatter:off
        String[] sources = {
            "foo/X.groovy",
            "package foo\n" +
            "\n" +
            "class X {\n" +
            "  public void foo() {\n" +
            "    def blah = this\n" +
            "    do {\n" +
            "    } while (blah != null)\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            isParrotParser() ? "" : "----------\n" +
            "1. ERROR in foo\\X.groovy (at line 6)\n" +
            "\tdo {\n" +
            "\t^\n" +
            "Groovy:unexpected token: do\n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "package foo;\n" +
            "public class X {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "  public void foo() {\n" +
            "    java.lang.Object blah;\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_GRE1107_2() {
        //@formatter:off
        String[] sources = {
            "foo/X.groovy",
            "package foo\n" +
            "import java.io.Serializable;\n" +
            "\n" +
            "class X {\n" +
            "  public void foo() {\n" +
            "    def blah = this\n" +
            "    do {\n" +
            "    } while (blah != null)\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            isParrotParser() ? "" : "----------\n" +
            "1. ERROR in foo\\X.groovy (at line 7)\n" +
            "\tdo {\n" +
            "\t^\n" +
            "Groovy:unexpected token: do\n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "package foo;\n" +
            "import java.io.Serializable;\n" +
            "public class X {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "  public void foo() {\n" +
            "    java.lang.Object blah;\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_GRE468_1() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X {\n" +
            " int y()\n" +
            " def m() {\n" +
            " }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 2)\n" +
            "\tint y()\n" +
            "\t^\n" +
            "Groovy:You defined a method without a body. Try adding a body, or declare it abstract.\n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "  public int y() {\n" +
            "  }\n" +
            "  public java.lang.Object m() {\n" +
            "  }\n" +
            "}\n");
    }

    @Test // new reference missing () in a method body
    public void testParsingRecovery_GRE468_2() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X {\n" +
            "  public void m() {\n" +
            "  new Earth\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 3)\n" +
            "\tnew Earth\n" +
            "\t    ^\n" +
            "Groovy:expecting \'(\' or \'[\' after type name to continue new expression\n" +
            "----------\n" +
            "2. ERROR in X.groovy (at line 3)\n" +
            "\tnew Earth\n" +
            "\t    ^^^^^\n" +
            "Groovy:unable to resolve class Earth\n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "  public void m() {\n" +
            "  }\n" +
            "}\n");
    }

    @Test // new reference missing () followed by valid code
    public void testParsingRecovery_GRE468_3() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X {\n" +
            "  public void m() {\n" +
            "  new Earth\n" +
            "  def a = 42\n" +
            "  print a\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 3)\n" +
            "\tnew Earth\n" +
            "\t    ^\n" +
            "Groovy:expecting \'(\' or \'[\' after type name to continue new expression\n" +
            "----------\n" +
            "2. ERROR in X.groovy (at line 3)\n" +
            "\tnew Earth\n" +
            "\t    ^^^^^\n" +
            "Groovy:unable to resolve class Earth\n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "  public void m() {\n" +
            "    java.lang.Object a;\n" +
            "  }\n" +
            "}\n");
    }

    @Test // missing type name for new call
    public void testParsingRecovery_GRE468_4() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "new\n" +
            "def a = 5\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 1)\n" +
            "\tnew\n" +
            "\t^\n" +
            "Groovy:missing type for constructor call\n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "public class X extends groovy.lang.Script {\n" +
            "  public X() {\n" +
            "  }\n" +
            "  public X(final groovy.lang.Binding context) {\n" +
            "  }\n" +
            "  public static void main(final java.lang.String... args) {\n" +
            "  }\n" +
            "  public @java.lang.Override java.lang.Object run() {\n" +
            "    java.lang.Object a;\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_GRE468_5() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X {\n" +
            "  public void m() {\n" +
            "  new Earth\n" +
            "     new Air\n" +
            "    new\n" +
            " new Fire\n" +
            " def leppard = 'cool'\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 3)\n" +
            "\tnew Earth\n" +
            "\t    ^\n" +
            "Groovy:expecting \'(\' or \'[\' after type name to continue new expression\n" +
            "----------\n" +
            "2. ERROR in X.groovy (at line 3)\n" +
            "\tnew Earth\n" +
            "\t    ^^^^^\n" +
            "Groovy:unable to resolve class Earth\n" +
            "----------\n" +
            "3. ERROR in X.groovy (at line 4)\n" +
            "\tnew Air\n" +
            "\t    ^\n" +
            "Groovy:expecting \'(\' or \'[\' after type name to continue new expression\n" +
            "----------\n" +
            "4. ERROR in X.groovy (at line 4)\n" +
            "\tnew Air\n" +
            "\t    ^^^\n" +
            "Groovy:unable to resolve class Air\n" +
            "----------\n" +
            "5. ERROR in X.groovy (at line 5)\n" +
            "\tnew\n" +
            "\t^\n" +
            "Groovy:missing type for constructor call\n" +
            "----------\n" +
            "6. ERROR in X.groovy (at line 6)\n" +
            "\tnew Fire\n" +
            "\t    ^\n" +
            "Groovy:expecting \'(\' or \'[\' after type name to continue new expression\n" +
            "----------\n" +
            "7. ERROR in X.groovy (at line 6)\n" +
            "\tnew Fire\n" +
            "\t    ^^^^\n" +
            "Groovy:unable to resolve class Fire\n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "  public void m() {\n" +
            "    java.lang.Object leppard;\n" +
            "  }\n" +
            "}\n");
    }

    @Test // missing type name for new call
    public void testParsingRecovery_GRE468_6() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X { \n" +
            " static {\n" +
            "new\n" +
            "def a = 5\n" +
            "}\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 3)\n" +
            "\tnew\n" +
            "\t^\n" +
            "Groovy:missing type for constructor call\n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "  <clinit>() {\n" +
            "    java.lang.Object a;\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_GRE468_7() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X {\n" +
            "  def x = new A\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 2)\n" +
            "\tdef x = new A\n" +
            "\t            ^\n" +
            "Groovy:expecting \'(\' or \'[\' after type name to continue new expression\n" +
            "----------\n" +
            "2. ERROR in X.groovy (at line 2)\n" +
            "\tdef x = new A\n" +
            "\t            ^\n" +
            "Groovy:unable to resolve class A\n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  private java.lang.Object x;\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_GRE468_8() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "import javax.swing.text.html.HTML\n" +
            "HTML h\n" +
            "new Earth",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 3)\n" +
            "\tnew Earth\n" +
            "\t    ^\n" +
            "Groovy:expecting \'(\' or \'[\' after type name to continue new expression\n" +
            "----------\n" +
            "2. ERROR in X.groovy (at line 3)\n" +
            "\tnew Earth\n" +
            "\t    ^^^^^\n" +
            "Groovy:unable to resolve class Earth\n" +
            "----------\n");

        checkGCUDeclaration("X.groovy", null);
    }

    @Test
    public void testParsingRecovery_GRE468_9() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "import javax.swing.text.html.HTML\n" +
            "HTML h\n" +
            "new String()\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        checkGCUDeclaration("X.groovy",
            "import javax.swing.text.html.HTML;\n" +
            "public class X extends groovy.lang.Script {\n" +
            "  public X() {\n" +
            "  }\n" +
            "  public X(final groovy.lang.Binding context) {\n" +
            "  }\n" +
            "  public static void main(final java.lang.String... args) {\n" +
            "  }\n" +
            "  public @java.lang.Override java.lang.Object run() {\n" +
            "    HTML h;\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_GRE766() {
        // missing end curly, but that shouldn't cause us to discard what we successfully parsed
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "public class X {\n" +
            "   def swingit() {\n" +
            "       swing.actions() {\n" +
            "           echoAction= swing.action(name: 'Echo back',\n" +
            "           enabled: bind(source: model, sourceProperty: 'loggedin'),\n" +
            "           closure: { controller.setEchoBack(it.source.selected) })\n" +
            "       }\n" +
            "   }\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 8)\n" +
            "\t}\n" +
            "\n" +
            "\t ^\n" +
            "Groovy:unexpected token: \n" +
            "----------\n");

        ModuleNode mn = getModuleNode("X.groovy");
        ClassNode cn = mn.getClasses().get(0);
        assertEquals("X", cn.getName());
        assertNotNull(cn.getDeclaredMethod("swingit", Parameter.EMPTY_ARRAY));
    }

    @Test
    public void testParsingRecovery_GRE494_1() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X {\n" +
            "   \n" +
            "   def getNumber() {\n" +
            "       return 42\n" +
            "   }\n" +
            "\n" +
            "   asdf\n" +
            "       \n" +
            "   static main(args) {\n" +
            "       print new X().getNumber()\n" +
            "   }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 7)\n" +
            "\tasdf\n" +
            "\t^\n" +
            "Groovy:unexpected token: asdf\n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "  public java.lang.Object getNumber() {\n" +
            "  }\n" +
            "  public static void main(java.lang.String... args) {\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_GRE494_2() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X {\n" +
            "  int intField\n" +
            "\n" +
            "belo\n" +
            "\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 4)\n" +
            "\tbelo\n" +
            "\t^\n" +
            "Groovy:unexpected token: belo\n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  private int intField;\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_GRE495() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "class Bar {}\n" +
            "class Foo extends Bar { }\n" +
            "class BBB extends Fo",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in A.groovy (at line 3)\n" +
            "\tclass BBB extends Fo\n" +
            "\t                  ^^\n" +
            "Groovy:unable to resolve class Fo\n" +
            "----------\n" +
            "2. ERROR in A.groovy (at line 3)\n" +
            "\tclass BBB extends Fo\n" +
            "\t                   ^\n" +
            "Groovy:Malformed class declaration\n" +
            "----------\n");

        // missing end curly, but that shouldn't cause us to discard what we successfully parsed
        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        List<ClassNode> l = mn.getClasses();
        for (int i = 0; i < l.size(); i++) {
            System.out.println(l.get(i));
        }
        assertFalse(mn.encounteredUnrecoverableError());
        ClassNode cn = mn.getClasses().get(2);
        assertNotNull(cn);
        assertEquals("Foo", cn.getName());
        cn = mn.getClasses().get(1);
        assertNotNull(cn);
        assertEquals("BBB", cn.getName());
    }

    @Test // variations: 'import' 'import static' 'import ' 'import static ' 'import com.' 'import static com.'
    public void testParsingRecovery_Imports1() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "import\n" +
            "\n" +
            "class X {}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 1)\n" +
            "\timport\n" +
            "\t^^^^^^\n" +
            "Groovy:unable to resolve class ?\n" +
            "----------\n");

        // import statement is not mapped from groovy to JDT world so does not appear in the declaration here
        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "}\n");
        // check it made it through the parse though
        ModuleNode mn = getModuleNode("X.groovy");
        assertEquals(1, mn.getImports().size());
        assertNotNull(mn.getImports().get(0).getType());
    }

    @Test
    public void testParsingRecovery_Imports2() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "import \n" +
            "\n" +
            "class X {}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 1)\n" +
            "\timport \n" +
            "\t^^^^^^^\n" +
            "Groovy:unable to resolve class ?\n" +
            "----------\n");

        // import statement is not mapped from groovy to JDT world so does not appear in the declaration here
        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "}\n");
        // check it made it through the parse though
        ModuleNode mn = getModuleNode("X.groovy");
        assertEquals(1, mn.getImports().size());
        assertNotNull(mn.getImports().get(0).getType());
    }

    @Test
    public void testParsingRecovery_Imports3() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "import static \n" +
            "\n" +
            "class X {}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 1)\n" +
            "\timport static \n" +
            "\t^^^^^^^^^^^^^^\n" +
            "Groovy:unable to resolve class ?\n" +
            "----------\n");

        // import statement is not mapped from groovy to JDT world so does not appear in the declaration here
        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "}\n");
        // check it made it through the parse though
        ModuleNode mn = getModuleNode("X.groovy");
        assertEquals(1, mn.getImports().size());
        assertNotNull(mn.getImports().get(0).getType());
    }

    @Test
    public void testParsingRecovery_Imports4() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "import com.\n" +
            "\n" +
            "class X {}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 1)\n" +
            "\timport com.\n" +
            "\t       ^\n" +
            "Groovy:Invalid import\n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "}\n");
        ModuleNode mn = getModuleNode("X.groovy");
        assertEquals(0, mn.getImports().size());
    }

    @Test
    public void testParsingRecovery_Imports5() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "import static com.\n" +
            "\n" +
            "class X {}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 1)\n" +
            "\timport static com.\n" +
            "\t              ^\n" +
            "Groovy:Invalid import\n" +
            "----------\n" +
            "2. ERROR in X.groovy (at line 1)\n" +
            "\timport static com.\n" +
            "\t              ^^^^\n" +
            "Only a type can be imported. com resolves to a package\n" +
            "----------\n" +
            "3. ERROR in X.groovy (at line 1)\n" +
            "\timport static com.\n" +
            "\t              ^^^\n" +
            "Groovy:unable to resolve class com\n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "}\n");
        ModuleNode mn = getModuleNode("X.groovy");
        assertEquals(0, mn.getImports().size());
    }

    @Test
    public void testParsingRecovery_InstrusivePathExpr1() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "package a\n" +
            "\n" +
            "import java.text.NumberFormat\n" +
            "\n" +
            "class X {\n" +
            "  @SuppressWarnings('rawtypes')\n" +
            "  static void main(args) {\n" +
            "    NumberFormat.\n" + // caret
            "    Set s = []\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 9)\n" +
            "\tSet s = []\n" +
            "\t^\n" +
            "Groovy:unexpected token: Set\n" +
            "----------\n");

        ModuleNode mn = getModuleNode("X.groovy");
        assertFalse(mn.encounteredUnrecoverableError());
        MethodNode main = mn.getClasses().get(0).getMethod("main",
            new Parameter[] {new Parameter(ClassHelper.STRING_TYPE.makeArray(), "args")});
        assertFalse("Expected at least path expression 'NumberFormat.' in block", ((BlockStatement) main.getCode()).getStatements().isEmpty());
    }

    @Test
    public void testParsingRecovery_InstrusivePathExpr2() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "package a\n" +
            "\n" +
            "import java.text.NumberFormat\n" +
            "\n" +
            "class X {\n" +
            "  static void main(args) {\n" +
            "    NumberFormat.\n" + // caret
            "    Set<Integer> s = []\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 8)\n" +
            "\tSet<Integer> s = []\n" +
            "\t^\n" +
            "Groovy:unexpected token: Set\n" +
            "----------\n");

        ModuleNode mn = getModuleNode("X.groovy");
        assertFalse(mn.encounteredUnrecoverableError());
        MethodNode main = mn.getClasses().get(0).getMethod("main",
            new Parameter[] {new Parameter(ClassHelper.STRING_TYPE.makeArray(), "args")});
        assertFalse("Expected at least path expression 'NumberFormat.' in block", ((BlockStatement) main.getCode()).getStatements().isEmpty());
    }

    @Test
    public void testParsingRecovery_MissingCurly1() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X {int y()}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 1)\n" +
            "\tclass X {int y()}\n" +
            "\t         ^\n" +
            "Groovy:You defined a method without a body. Try adding a body, or declare it abstract.\n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "  public int y() {\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_MissingCurly2() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X { int y() {}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 1)\n" +
            "\tclass X { int y() {}\n\n" +
            "\t                    ^\n" +
            "Groovy:unexpected token: \n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "  public int y() {\n" +
            "  }\n" +
            "}\n");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/895
    public void testParsingRecovery_MissingCurly3() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "do\n" +
            "println 123\n" +
            "println 456\n" +
            "while (false)\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Script.groovy (at line 1)\n" +
            "\tdo\nprintln 123\n" +
            "\t  ^\n" +
            "Groovy:expecting '{', found '<newline>'\n" +
            "----------\n");
    }

    @Test
    public void testParsingRecovery_ParameterCompletion() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "package com.example.foo\n" +
            "class X {\n" +
            "public void foo(XMLConstants\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 3)\n" +
            "\tpublic void foo(XMLConstants\n" +
            "\t                ^\n" +
            "Groovy:unexpected token: XMLConstants\n" +
            "----------\n" +
            "2. ERROR in X.groovy (at line 4)\n" +
            "\t}\n" +
            "\n" +
            "\t ^\n" +
            "Groovy:unexpected token: \n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "}\n");
    }

    @Test // parser should correctly parse this code, but should return with an error
    public void testParsingRecovery_SafeDereferencing() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "public class X {\n" +
            "  int someProperty\n" +
            "  void someMethod() {\n" +
            "    someProperty?.\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 5)\n" +
            "\t}\n" +
            "\t^\n" +
            "Groovy:unexpected token: " + "}\n" +
            "----------\n");

        checkGCUDeclaration("X.groovy",
            "public class X {\n" +
            "  private int someProperty;\n" +
            "  public " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "X() {\n" +
            "  }\n" +
            "  public void someMethod() {\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testParsingRecovery_ScriptWithError() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "print Coolio!",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 1)\n" +
            "\tprint Coolio!\n" +
            "\t            ^\n" +
            "Groovy:expecting EOF, found \'!\'\n" +
            "----------\n");
    }

    @Test
    public void testUnrecoverableErrors_GRE755_1() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "class X {\n" +
            "  def x=\"\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 2)\n" +
            "\tdef x=\"\n" +
            "}\n" +
            "\t       ^\n" +
            "Groovy:expecting anything but \'\'\\n\'\'; got it anyway\n" +
            "----------\n");

        ModuleNode mn = getModuleNode("X.groovy");
        assertTrue(mn.encounteredUnrecoverableError());
    }

    @Test
    public void testUnrecoverableErrors_GRE755_2() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "package a\n" +
            "\n" +
            "def foo(Nuthin\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 3)\n" +
            "\tdef foo(Nuthin\n" +
            "\t        ^\n" +
            "Groovy:unexpected token: Nuthin\n" +
            "----------\n");

        ModuleNode mn = getModuleNode("X.groovy");
        assertFalse(mn.encounteredUnrecoverableError());
    }
}
