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
package org.eclipse.jdt.groovy.core.tests.basic;

import junit.framework.Test;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.eclipse.jdt.core.tests.util.GroovyUtils;

/**
 * Tests error recovery from the parser. All tests in this class are failing, so
 * do not add to build.  When a test starts passing, remove from this class.
 *
 * @author Andrew Eisenberg
 * @created May 17, 2011
 */
public final class ErrorRecoveryTests extends AbstractGroovyRegressionTest {

    public static Test suite() {
        return buildMinimalComplianceTestSuite(ErrorRecoveryTests.class, F_1_5);
    }

    public ErrorRecoveryTests(String name) {
        super(name);
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
        ClassNode cn = mn.getClasses().get(0);
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
        ClassNode cn = mn.getClasses().get(0);
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
        ClassNode cn = mn.getClasses().get(0);
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
        ClassNode cn = mn.getClasses().get(0);
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
        ClassNode cn = mn.getClasses().get(0);
        assertNotNull(cn);
        assertTrue(cn.getName().equals("TextCompletionTest"));
    }

    public void testParsingRecovery_GRE1085_1() {
        runNegativeTest(new String[] {
            "MyDomainClass.groovy",
            "package foo\n"+
            "\n"+
            "class Greclipse1085 {\n"+
            "  public void foo() {\n"+
            "    foo:\n"+
            "  }\n"+
            "}\n"
        },
        "----------\n" +
        "1. ERROR in MyDomainClass.groovy (at line 6)\n" +
        "\t}\n" +
        "\t^\n" +
        "Groovy:unexpected token: } @ line 6, column 3.\n" +
        "----------\n");

        checkGCUDeclaration("MyDomainClass.groovy",
            "package foo;\n"+
            "public class Greclipse1085 {\n"+
            "  public Greclipse1085() {\n"+
            "  }\n"+
            "  public void foo() {\n"+
            "  }\n"+
            "}\n");
    }

    public void testParsingRecovery_GRE1085_2() {
        runNegativeTest(new String[] {
            "MyDomainClass.groovy",
            "package foo\n"+
            "\n"+
            "class Greclipse1085 {\n"+
            "  public void foo() {\n"+
            "    foo:\"abc\",\n"+
            "  }\n"+
            "}\n"
        },
        "----------\n" +
        "1. ERROR in MyDomainClass.groovy (at line 5)\n" +
        "\tfoo:\"abc\",\n" +
        "\t    ^\n" +
        "Groovy:unexpected token: abc @ line 5, column 9.\n" +
        "----------\n");

        checkGCUDeclaration("MyDomainClass.groovy",
            "package foo;\n"+
            "public class Greclipse1085 {\n"+
            "  public Greclipse1085() {\n"+
            "  }\n"+
            "  public void foo() {\n"+
            "  }\n"+
            "}\n");
    }

    public void testParsingRecovery_GRE1085_3() {
        runNegativeTest(new String[] {
            "MyDomainClass.groovy",
            "package foo\n"+
            "\n"+
            "class Greclipse1085 {\n"+
            "  public void foo() {\n"+
            "    foo:,\n"+
            "  }\n"+
            "}\n"},
            "----------\n" +
            "1. ERROR in MyDomainClass.groovy (at line 5)\n" +
            "\tfoo:,\n" +
            "\t    ^\n" +
            "Groovy:unexpected token: , @ line 5, column 9.\n" +
            "----------\n");

        checkGCUDeclaration("MyDomainClass.groovy",
            "package foo;\n"+
            "public class Greclipse1085 {\n"+
            "  public Greclipse1085() {\n"+
            "  }\n"+
            "  public void foo() {\n"+
            "  }\n"+
            "}\n");
    }

    public void testParsingRecovery_GRE1192_1() {
        runNegativeTest(new String[] {
            "MyDomainClass.groovy",
            "class Script {\n"+
            "\tdef m() {\n"+
            "\t}\n"+
            "\tdef x = {\n"+
            "\t  nuthin s,\n"+
            "\t}\n"+
            "}\n"
        },
        "----------\n" +
        "1. ERROR in MyDomainClass.groovy (at line 6)\n" +
        "\t}\n" +
        "\t^\n" +
        "Groovy:unexpected token: } @ line 6, column 2.\n" +
        "----------\n");

        checkGCUDeclaration("MyDomainClass.groovy",
            "public class Script {\n"+
            "  private java.lang.Object x;\n"+
            "  public Script() {\n"+
            "  }\n"+
            "  public java.lang.Object m() {\n"+
            "  }\n"+
            "}\n");
    }

    public void testParsingRecovery_GRE1046_1() {
        // this file is missing the 'then' block.  We should cope and still offer assists
        runNegativeTest(new String[] {
            "MyDomainClass.groovy",
            "File f = new File();\n"+
            "if (f.)\n"
        },
        "----------\n" +
        "1. ERROR in MyDomainClass.groovy (at line 2)\n" +
        "\tif (f.)\n" +
        "\t      ^\n" +
        "Groovy:unexpected token: ) @ line 2, column 7.\n" +
        "----------\n" +
        "2. ERROR in MyDomainClass.groovy (at line 2)\n" +
        "\tif (f.)\n" +
        "\n" +
        "\t       ^\n" +
        "Groovy:unexpected token:  @ line 2, column 8.\n" +
        "----------\n");
    }

    public void testParsingRecovery_GRE1046_2() {
        // trickier than above, this is also missing the closing paren
        runNegativeTest(new String[] {
            "MyDomainClass.groovy",
            "File f = new File();\n"+
            "if (f.\n"
        },
        "----------\n" +
        "1. ERROR in MyDomainClass.groovy (at line 2)\n" +
        "\tif (f.\n" +
        "\n" +
        "\t      ^\n" +
        "Groovy:Expecting an identifier, found a trailing \'.\' instead. @ line 2, column 7.\n" +
        "----------\n");
    }

    public void testParsingRecovery_GRE1213_1() {
        // missing close paren
        runNegativeTest(new String[] {
            "MyDomainClass.groovy",
            "import java.awt.BorderLayout;\n"+
            "panel.add (textField, BorderLayout.\n"
        },
        "----------\n" +
        "1. ERROR in MyDomainClass.groovy (at line 2)\n" +
        "\tpanel.add (textField, BorderLayout.\n" +
        "\n" +
        "\t                                   ^\n" +
        "Groovy:Expecting an identifier, found a trailing \'.\' instead. @ line 2, column 36.\n" +
        "----------\n" +
        "2. ERROR in MyDomainClass.groovy (at line 2)\n" +
        "\tpanel.add (textField, BorderLayout.\n" +
        "\n" +
        "\t                                   ^\n" +
        "Groovy:expecting \')\', found \'\' @ line 2, column 36.\n" +
        "----------\n");
    }

    public void testParsingRecovery_GRE1213_2() {
        // missing close paren
        runNegativeTest(new String[] {
            "MyDomainClass.groovy",
            "import java.awt.BorderLayout;\n"+
            "String s = ('foo' + BorderLayout.\n"
        },
        "----------\n" +
        "1. ERROR in MyDomainClass.groovy (at line 2)\n" +
        "\tString s = (\'foo\' + BorderLayout.\n" +
        "\n" +
        "\t                                 ^\n" +
        "Groovy:Expecting an identifier, found a trailing \'.\' instead. @ line 2, column 34.\n" +
        "----------\n" +
        "2. ERROR in MyDomainClass.groovy (at line 2)\n" +
        "\tString s = (\'foo\' + BorderLayout.\n" +
        "\n" +
        "\t                                 ^\n" +
        "Groovy:expecting \')\', found \'\' @ line 2, column 34.\n" +
        "----------\n");
    }

    public void testParsingRecovery_GRE1107_1() {
        runNegativeTest(new String[] {
            "MyDomainClass.groovy",
            "package foo\n"+
            "\n"+
            "class Greclipse1107 {\n"+
            "  public void foo() {\n"+
            "    Blah blah = this\n"+
            "    do {\n"+
            "    } while (blah != null)\n"+
            "\n"+
            "    return null\n"+
            "  }\n"+
            "}\n"
        },
        "----------\n" +
        "1. ERROR in MyDomainClass.groovy (at line 6)\n" +
        "\tdo {\n" +
        "\t^\n" +
        "Groovy:unexpected token: do @ line 6, column 5.\n" +
        "----------\n" +
        "2. ERROR in MyDomainClass.groovy (at line 7)\n" +
        "\t} while (blah != null)\n" +
        "\t  ^\n" +
        "Groovy:expecting EOF, found \'while\' @ line 7, column 7.\n" +
        "----------\n");

        checkGCUDeclaration("MyDomainClass.groovy",
            "package foo;\n"+
            "public class Greclipse1107 {\n"+
            "  private java.lang.Object public;\n"+
            "  public Greclipse1107() {\n"+
            "  }\n"+
            "}\n");
    }

    public void testParsingRecovery_GRE1107_2() {
        runNegativeTest(new String[] {
            "MyDomainClass.groovy",
            "package foo\n"+
            "import java.io.Serializable;\n"+
            "\n"+
            "class Greclipse1107 {\n"+
            "  public void foo() {\n"+
            "    Blah blah = this\n"+
            "    do {\n"+
            "    } while (blah != null)\n"+
            "\n"+
            "    return null\n"+
            "  }\n"+
            "}\n"
        },
        "----------\n" +
        "1. ERROR in MyDomainClass.groovy (at line 7)\n" +
        "\tdo {\n" +
        "\t^\n" +
        "Groovy:unexpected token: do @ line 7, column 5.\n" +
        "----------\n" +
        "2. ERROR in MyDomainClass.groovy (at line 8)\n" +
        "\t} while (blah != null)\n" +
        "\t  ^\n" +
        "Groovy:expecting EOF, found \'while\' @ line 8, column 7.\n" +
        "----------\n");

        checkGCUDeclaration("MyDomainClass.groovy",
            "package foo;\n"+
            "import java.io.Serializable;\n"+
            "public class Greclipse1107 {\n"+
            "  private java.lang.Object public;\n"+
            "  public Greclipse1107() {\n"+
            "  }\n"+
            "}\n");
    }

    public void testParsingRecovery_GRE468_1() {
        runNegativeTest(new String[] {
            "MyDomainClass.groovy",
            "class T {\n"+
            "\n"+
            " int y()\n"+
            " def m() {\n"+
            " }\n"+
            "}\n"
        },
        "----------\n" +
        "1. ERROR in MyDomainClass.groovy (at line 3)\n" +
        "\tint y()\n" +
        "\t^\n" +
        "Groovy:You defined a method without body. Try adding a body, or declare it abstract. @ line 3, column 2.\n" +
        "----------\n");

        checkGCUDeclaration("MyDomainClass.groovy",
            "public class T {\n"+
            "  public T() {\n"+
            "  }\n"+
            "  public int y() {\n"+
            "  }\n"+
            "  public java.lang.Object m() {\n"+
            "  }\n"+
            "}\n");
    }

    public void testRecovery_GRE766() {
        runNegativeTest(new String[] {
            "A.groovy",
            "public class SwingIt {\n"+
            "   def swingit() {\n"+
            "       swing.actions() {\n"+
            "           echoAction= swing.action(name: 'Echo back',\n"+
            "           enabled: bind(source: model, sourceProperty: 'loggedin'),\n"+
            "           closure: { controller.setEchoBack(it.source.selected) })\n"+
            "       }\n"+
            "   }\n"
        },
        "----------\n" +
        "1. ERROR in A.groovy (at line 8)\n" +
        "\t}\n" +
        "\n" +
        "\t ^\n" +
        "Groovy:unexpected token:  @ line 8, column 5.\n" +
        "----------\n");

        // missing end curly, but that shouldn't cause us to discard what we successfully parsed
        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        ClassNode cn = mn.getClasses().get(0);
        assertNotNull(cn);
        assertTrue(cn.getName().equals("SwingIt")); // should be SwingIt
        MethodNode methodNode = cn.getDeclaredMethod("swingit", Parameter.EMPTY_ARRAY);
        assertNotNull(methodNode);
    }

    public void testParsingRecovery_GRE494() {
        this.runNegativeTest(new String[] {
            "Simple.groovy",
            "class Simple {\n"+
            "   \n"+
            "   def getNumber() {\n"+
            "       return 42\n"+
            "   }\n"+
            "\n"+
            "   asdf\n"+
            "       \n"+
            "   static main(args) {\n"+
            "       print new Simple().getNumber()\n"+
            "   }\n"+
            "}\n"},
            "----------\n" +
            "1. ERROR in Simple.groovy (at line 7)\n" +
            "\tasdf\n" +
            "\t^\n" +
            "Groovy:unexpected token: asdf @ line 7, column 4.\n" +
            "----------\n");
        checkGCUDeclaration("Simple.groovy",
                "public class Simple {\n" +
                "  private java.lang.Object asdf;\n"+
                "  public Simple() {\n" +
                "  }\n" +
                "  public java.lang.Object getNumber() {\n" +
                "  }\n" +
                "  public static void main(public java.lang.String... args) {\n" +
                "  }\n" +
                "}\n");
    }

    public void testParsingRecovery_GRE494_2() {
        this.runNegativeTest(new String[] {
            "MyDomainClass.groovy",
            "class MyDomainClass {\n"+
            "  int intField\n"+
            "\n"+
            "belo\n"+
            "\n"+
            "}\n"},
            "----------\n" +
            "1. ERROR in MyDomainClass.groovy (at line 4)\n" +
            "\tbelo\n" +
            "\t^\n" +
            "Groovy:unexpected token: belo @ line 4, column 1.\n" +
            "----------\n");
        checkGCUDeclaration("MyDomainClass.groovy",
                "public class MyDomainClass {\n" +
                "  private int intField;\n" +
                "  private java.lang.Object belo;\n"+
                "  public MyDomainClass() {\n" +
                "  }\n" +
                "}\n");
    }

    public void _testParsingMissingCurlyRecovery1_GRE468() {
        if (GroovyUtils.isGroovy16()) return; // not valid on 1.6 - doesn't have a fixed parser

        this.runNegativeTest(new String[] {
                "XXX.groovy",
                "class F { int y() }\n"
                },
                "----------\n" +
                "1. ERROR in XXX.groovy (at line 1)\n" +
                "\tclass F { int y() }\n" +
                "\t               ^\n" +
                "Groovy:Method body missing @ line 1, column 15.\n" +
                "----------\n"
                );
        checkGCUDeclaration("XXX.groovy",
                "public class F extends java.lang.Object {\n" +
                "  public F() {\n" +
                "  }\n" +
                "  public int y() {\n" +
                "  }\n" +
                "}\n");
    }

    public void _testParsingMissingCurlyRecovery2_GRE468() {
        if (GroovyUtils.isGroovy16()) return; // not valid on 1.6 - doesn't have a fixed parser

        this.runNegativeTest(new String[] {
                "XXX.groovy",
                "class F { int y() { }\n"
                },
                "----------\n" +
                "1. ERROR in XXX.groovy (at line 1)\n" +
                "\tclass F { int y() }\n" +
                "\t           ^\n" +
                "Groovy:You defined a method without body. Try adding a body, or declare it abstract. at line: 1 column: 11. File: XXX.groovy @ line 1, column 11.\n" +
                "----------\n"
                );
        checkGCUDeclaration("XXX.groovy",
                "public class C extends java.lang.Object {\n" +
                "  public C() {\n" +
                "  }\n" +
                "  public void m() {\n" +
                "  }\n" +
                "}\n");
    }

    /**
     * Simple case of a new reference missing () in a method body
     */
    public void testParsingNewRecovery1_GRE468() {
        this.runNegativeTest(new String[] {
                "XXX.groovy",
                "class C {\n"+
                "  public void m() {\n"+
                "  new Earth\n"+
                "  }\n"+
                "}"
                },
                "----------\n" +
                "1. ERROR in XXX.groovy (at line 3)\n" +
                "\tnew Earth\n" +
                "\t    ^\n" +
                "Groovy:expecting \'(\' or \'[\' after type name to continue new expression @ line 3, column 7.\n" +
                "----------\n" +
                "2. ERROR in XXX.groovy (at line 3)\n" +
                "\tnew Earth\n" +
                "\t    ^^^^^\n" +
                "Groovy:unable to resolve class Earth \n" +
                "----------\n"
                );
        checkGCUDeclaration("XXX.groovy",
                "public class C {\n" +
                "  public C() {\n" +
                "  }\n" +
                "  public void m() {\n" +
                "  }\n" +
                "}\n");
    }

    /**
     * Simple case of a new reference missing () followed by valid code
     */
    public void testParsingNewRecovery2_GRE468() {
        this.runNegativeTest(new String[] {
                "XXX.groovy",
                "class C {\n"+
                "  public void m() {\n"+
                "  new Earth\n"+
                "  def a = 42\n"+
                "  print a\n"+
                "  }\n"+
                "}"
                },
                "----------\n" +
                "1. ERROR in XXX.groovy (at line 3)\n" +
                "\tnew Earth\n" +
                "\t    ^\n" +
                "Groovy:expecting \'(\' or \'[\' after type name to continue new expression @ line 3, column 7.\n" +
                "----------\n" +
                "2. ERROR in XXX.groovy (at line 3)\n" +
                "\tnew Earth\n" +
                "\t    ^^^^^\n" +
                "Groovy:unable to resolve class Earth \n" +
                "----------\n"
                );
        checkGCUDeclaration("XXX.groovy",
                "public class C {\n" +
                "  public C() {\n" +
                "  }\n" +
                "  public void m() {\n" +
                "  }\n" +
                "}\n");
    }

    public void testRecoveryParameterCompletion() {
        this.runNegativeTest(new String[]{
                "XXX.groovy",
                "package com.example.foo\n"+
                "class Foo {\n"+
                "public void foo(XMLConstants\n"+
                "}\n"},
                "----------\n" +
                "1. ERROR in XXX.groovy (at line 3)\n" +
                "\tpublic void foo(XMLConstants\n" +
                "\t                ^\n" +
                "Groovy:unexpected token: XMLConstants @ line 3, column 17.\n" +
                "----------\n" +
                "2. ERROR in XXX.groovy (at line 4)\n" +
                "\t}\n" +
                "\n" +
                "\t ^\n" +
                "Groovy:unexpected token:  @ line 4, column 2.\n" +
                "----------\n");
        checkGCUDeclaration("XXX.groovy",
                "public class Foo {\n" +
                "  public Foo() {\n" +
                "  }\n" +
                "}\n");
        }

    /**
     * Missing type name for new call
     */
    public void testParsingNewRecovery3_GRE468() {
        this.runNegativeTest(new String[] {
                "Foo.groovy",
                "new\n"+
                "def a = 5\n"},
                "----------\n" +
                "1. ERROR in Foo.groovy (at line 1)\n" +
                "\tnew\n" +
                "\t ^\n" +
                "Groovy:missing type for constructor call @ line 1, column 1.\n" +
                "----------\n");
        checkGCUDeclaration("Foo.groovy",
                "public class Foo extends groovy.lang.Script {\n" +
                "  public Foo() {\n" +
                "  }\n" +
                "  public Foo(public groovy.lang.Binding context) {\n" +
                "  }\n" +
                "  public static void main(public java.lang.String... args) {\n" +
                "  }\n" +
                "  public java.lang.Object run() {\n" +
                "  }\n" +
                "}\n");
    }

    public void testParsingNewRecovery4_GRE468() {
//      if (isGroovy16()) return; // not valid on 1.6 - doesn't have a fixed parser
        this.runNegativeTest(new String[] {
                "XXX.groovy",
                "class C {\n"+
                "  public void m() {\n"+
                "  new Earth\n"+
                "     new Air\n"+
                "    new\n"+
                " new Fire\n"+
                " def leppard = 'cool'\n"+
                "  }\n"+
                "}"
                },
                "----------\n" +
                "1. ERROR in XXX.groovy (at line 3)\n" +
                "\tnew Earth\n" +
                "\t    ^\n" +
                "Groovy:expecting \'(\' or \'[\' after type name to continue new expression @ line 3, column 7.\n" +
                "----------\n" +
                "2. ERROR in XXX.groovy (at line 3)\n" +
                "\tnew Earth\n" +
                "\t    ^^^^^\n" +
                "Groovy:unable to resolve class Earth \n" +
                "----------\n" +
                "3. ERROR in XXX.groovy (at line 4)\n" +
                "\tnew Air\n" +
                "\t    ^\n" +
                "Groovy:expecting \'(\' or \'[\' after type name to continue new expression @ line 4, column 10.\n" +
                "----------\n" +
                "4. ERROR in XXX.groovy (at line 4)\n" +
                "\tnew Air\n" +
                "\t    ^^^\n" +
                "Groovy:unable to resolve class Air \n" +
                "----------\n" +
                "5. ERROR in XXX.groovy (at line 5)\n" +
                "\tnew\n" +
                "\t^\n" +
                "Groovy:missing type for constructor call @ line 5, column 5.\n" +
                "----------\n" +
                "6. ERROR in XXX.groovy (at line 6)\n" +
                "\tnew Fire\n" +
                "\t    ^\n" +
                "Groovy:expecting \'(\' or \'[\' after type name to continue new expression @ line 6, column 6.\n" +
                "----------\n" +
                "7. ERROR in XXX.groovy (at line 6)\n" +
                "\tnew Fire\n" +
                "\t    ^^^^\n" +
                "Groovy:unable to resolve class Fire \n" +
                "----------\n"
                );
        checkGCUDeclaration("XXX.groovy",
                "public class C {\n" +
                "  public C() {\n" +
                "  }\n" +
                "  public void m() {\n" +
                "  }\n" +
                "}\n");
    }

    /**
     * Missing type name for new call
     */
    public void testParsingNewRecovery5_GRE468() {
        this.runNegativeTest(new String[] {
                "Foo.groovy",
                "class C { \n"+
                " static {\n"+
                "new\n"+
                "def a = 5\n"+
                "}\n"+
                "}"},
                "----------\n" +
                "1. ERROR in Foo.groovy (at line 3)\n" +
                "\tnew\n" +
                "\t^\n" +
                "Groovy:missing type for constructor call @ line 3, column 1.\n" +
                "----------\n");
        checkGCUDeclaration("Foo.groovy",
                "public class C {\n" +
                "  public C() {\n" +
                "  }\n" +
                "  static void <clinit>() {\n" +
                "  }\n" +
                "}\n");
    }

    // variations: 'import' 'import static' 'import ' 'import static ' 'import com.' 'import static com.'
    public void _testParsingNewRecoveryImports1_GRE538() {
        this.runNegativeTest(new String[] {
            "XXX.groovy",
            "import\n"+
            "\n"+
            "class Wibble {}\n"
        },"----------\n" +
        "1. ERROR in XXX.groovy (at line 1)\n" +
        "\timport\n" +
        "\t ^\n" +
        "Groovy:Invalid import specification @ line 1, column 1.\n" +
        "----------\n");
        // import statement is not mapped from groovy to JDT world so does not appear in the declaration here
        checkGCUDeclaration("XXX.groovy",
                "public class Wibble {\n" +
                "  public Wibble() {\n" +
                "  }\n" +
                "}\n");
        // check it made it through the parse though
        ModuleNode mn = getModuleNode("XXX.groovy");
        assertEquals(1,mn.getImports().size());
        ClassNode cn = mn.getImports().get(0).getType();
        assertNull(cn);
    }

    public void _testParsingNewRecoveryImports2_GRE538() {
        this.runNegativeTest(new String[] {
            "XXX.groovy",
            "import \n"+
            "\n"+
            "class Wibble {}\n"
        },"----------\n" +
        "1. ERROR in XXX.groovy (at line 1)\n" +
        "\timport \n" +
        "\t ^\n" +
        "Groovy:Invalid import specification @ line 1, column 1.\n" +
        "----------\n");
        // import statement is not mapped from groovy to JDT world so does not appear in the declaration here
        checkGCUDeclaration("XXX.groovy",
                "public class Wibble {\n" +
                "  public Wibble() {\n" +
                "  }\n" +
                "}\n");
        // check it made it through the parse though
        ModuleNode mn = getModuleNode("XXX.groovy");
        assertEquals(1,mn.getImports().size());
        ClassNode cn = mn.getImports().get(0).getType();
        assertNull(cn);
    }

    public void _testParsingNewRecoveryImports3_GRE538() {
        this.runNegativeTest(new String[] {
            "XXX.groovy",
            "import static \n"+
            "\n"+
            "class Wibble {}\n"
        },"----------\n" +
        "1. ERROR in XXX.groovy (at line 1)\n" +
        "\timport static \n" +
        "\t ^\n" +
        "Groovy:Invalid import specification @ line 1, column 8.\n" +
        "----------\n");
        // import statement is not mapped from groovy to JDT world so does not appear in the declaration here
        checkGCUDeclaration("XXX.groovy",
                "public class Wibble {\n" +
                "  public Wibble() {\n" +
                "  }\n" +
                "}\n");
        // check it made it through the parse though
        ModuleNode mn = getModuleNode("XXX.groovy");
        assertEquals(1,mn.getImports().size());
        ClassNode cn = mn.getImports().get(0).getType();
        assertNull(cn);
    }

    public void _testParsingNewRecoveryImports4_GRE538() {
        this.runNegativeTest(new String[] {
            "XXX.groovy",
            "import com.\n"+
            "\n"+
            "class Wibble {}\n"
        },"----------\n" +
        "1. ERROR in XXX.groovy (at line 1)\n" +
        "\timport\n" +
        "\t ^\n" +
        "Groovy:Invalid import specification @ line 1, column 1.\n" +
        "----------\n");
        // import statement is not mapped from groovy to JDT world so does not appear in the declaration here
        checkGCUDeclaration("XXX.groovy",
                "public class Wibble {\n" +
                "  public Wibble() {\n" +
                "  }\n" +
                "}\n");
        // check it made it through the parse though
        ModuleNode mn = getModuleNode("XXX.groovy");
        assertEquals(1,mn.getImports().size());
        ClassNode cn = mn.getImports().get(0).getType();
        assertNull(cn);
    }

    public void _testParsingNewRecoveryImports5_GRE538() {
        this.runNegativeTest(new String[] {
            "XXX.groovy",
            "import static com.\n"+
            "\n"+
            "class Wibble {}\n"
        },"----------\n" +
        "1. ERROR in XXX.groovy (at line 1)\n" +
        "\timport\n" +
        "\t ^\n" +
        "Groovy:Invalid import specification @ line 1, column 1.\n" +
        "----------\n");
        // import statement is not mapped from groovy to JDT world so does not appear in the declaration here
        checkGCUDeclaration("XXX.groovy",
                "public class Wibble {\n" +
                "  public Wibble() {\n" +
                "  }\n" +
                "}\n");
        // check it made it through the parse though
        ModuleNode mn = getModuleNode("XXX.groovy");
        assertEquals(1,mn.getImports().size());
        ClassNode cn = mn.getImports().get(0).getType();
        assertNull(cn);
    }

    public void testParsingNewRecovery6_GRE468() {
        this.runNegativeTest(new String[] {
                "XXX.groovy",
                "class Sample {\n"+
                "  def x = new A\n"+
                "}"
        },
        "----------\n" +
        "1. ERROR in XXX.groovy (at line 2)\n" +
        "\tdef x = new A\n" +
        "\t            ^\n" +
        "Groovy:expecting \'(\' or \'[\' after type name to continue new expression @ line 2, column 15.\n" +
        "----------\n" +
        "2. ERROR in XXX.groovy (at line 2)\n" +
        "\tdef x = new A\n" +
        "\t            ^\n" +
        "Groovy:unable to resolve class A \n" +
        "----------\n"
        );
        checkGCUDeclaration("XXX.groovy",
                "public class Sample {\n" +
                "  private java.lang.Object x;\n" +
                "  public Sample() {\n" +
                "  }\n" +
        "}\n");
    }

    public void testParsingNewRecovery7_GRE468() {
        this.runNegativeTest(new String[] {
                "XXX.groovy",
                "import javax.swing.text.html.HTML\n"+
                "HTML h\n"+
                "new Earth"
        },
        "----------\n" +
        "1. ERROR in XXX.groovy (at line 3)\n" +
        "\tnew Earth\n" +
        "\t    ^\n" +
        "Groovy:expecting \'(\' or \'[\' after type name to continue new expression @ line 3, column 5.\n" +
        "----------\n" +
        "2. ERROR in XXX.groovy (at line 3)\n" +
        "\tnew Earth\n" +
        "\t    ^^^^^\n" +
        "Groovy:unable to resolve class Earth \n" +
        "----------\n"
        );
        checkGCUDeclaration("XXX.groovy",null);
    }

    public void testParsingNewRecovery8_GRE468() {
//      if (isGroovy16()) return; // not valid on 1.6 - doesn't have a fixed parser
        this.runNegativeTest(new String[] {
                "XXX.groovy",
                "import javax.swing.text.html.HTML\n"+
                "HTML h\n"+
                "new String()\n"
        },"");
        checkGCUDeclaration("XXX.groovy",
                "import javax.swing.text.html.HTML;\n" +
                "public class XXX extends groovy.lang.Script {\n" +
                "  public XXX() {\n" +
                "  }\n" +
                "  public XXX(public groovy.lang.Binding context) {\n" +
                "  }\n" +
                "  public static void main(public java.lang.String... args) {\n" +
                "  }\n" +
                "  public java.lang.Object run() {\n" +
                "  }\n" +
        "}\n");
    }

    public void _testUnrecoverableErrors_GRE949() {
        this.runNegativeTest(new String[] {
                "Foo.groovy",
                "package a\n" +
                "import javax.swing.text.html.HTML; \n" +
                "   void nuthin() {\n" +
                "         if (! (this instanceof HTML/*_*/) {\n" +
                "            \n" +
                "         }\n" +
                "    } "
        },
        "----------\n" +
        "1. ERROR in Foo.groovy (at line 4)\n" +
        "\tif (! (this instanceof HTML/*_*/) {\n" +
        "\t                       ^\n" +
        "Groovy:unable to resolve class HTML \n" +
        "----------\n" +
        "2. ERROR in Foo.groovy (at line 7)\n" +
        "\t} \n" +
        "\t^\n" +
        "Groovy:expecting \')\', found \'}\' @ line 7, column 5.\n" +
        "----------\n");
        this.runNegativeTest(new String[] {
                "Foo.groovy",
                "package com.foo\n"+
                "\n"+
                "import javax.swing.text.html.HTML\n"+
                "\n"+
                "void nuthin() {\n"+
                "if (! (this instanceof HTMLAccessibleContext/*_*/) {\n"+
                "\n"+
                "}\n"+
                "}"
                },
                "----------\n" +
                "1. ERROR in Foo.groovy (at line 6)\n" +
                "\tif (! (this instanceof HTMLAccessibleContext/_/) {\n" +
                "\t                                            ^\n" +
                "Groovy:unexpected token: / @ line 6, column 45.\n" +
                "----------\n");
        ModuleNode mn = getModuleNode("Foo.groovy");
        assertTrue(mn.encounteredUnrecoverableError());
    }

    public void testUnrecoverableErrors_GRE755_2() {
        this.runNegativeTest(new String[] {
            "Bar.groovy",
            "class Bar {\n"+
            "  def x=\"\n"+
            "}\n"
        },"----------\n" +
        "1. ERROR in Bar.groovy (at line 2)\n" +
        "\tdef x=\"\n" +
        "}\n" +
        "\t       ^\n" +
        "Groovy:expecting anything but \'\'\\n\'\'; got it anyway @ line 2, column 10.\n" +
        "----------\n");
        ModuleNode mn = getModuleNode("Bar.groovy");
        assertTrue(mn.encounteredUnrecoverableError());
    }

    public void testUnrecoverableErrors_GRE755_3() {
        this.runNegativeTest(new String[] {
            "Bar.groovy",
            "package a\n"+
            "\n"+
            "def foo(Nuthin\n"
        },"----------\n" +
        "1. ERROR in Bar.groovy (at line 3)\n" +
        "\tdef foo(Nuthin\n" +
        "\t        ^\n" +
        "Groovy:unexpected token: Nuthin @ line 3, column 9.\n" +
        "----------\n");
        ModuleNode mn = getModuleNode("Bar.groovy");


        if (GroovyUtils.GROOVY_LEVEL<18) {
            assertTrue(mn.encounteredUnrecoverableError());
        } else {
            assertFalse(mn.encounteredUnrecoverableError());
        }
    }

    // Parser should correctly parse this code, but
    // should return with an error
    public void testSafeDereferencingParserRecovery() {
        this.runNegativeTest(new String[] {
                "Run.groovy",
                "public class SomeClass {\n" +
                "  int someProperty\n" +
                "  void someMethod() {\n" +
                "    someProperty?.\n" +
                "  }\n" +
                "}"
        },
        "----------\n" +
        "1. ERROR in Run.groovy (at line 5)\n" +
        "\t}\n" +
        "\t^\n" +
        "Groovy:unexpected token: } @ line 5, column 3.\n" +
        "----------\n");

        String expectedOutput =
            "public class SomeClass {\n" +
            "  private int someProperty;\n" +
            "  public SomeClass() {\n" +
            "  }\n" +
            "  public void someMethod() {\n" +
            "  }\n" +
            "}\n";
        checkGCUDeclaration("Run.groovy",expectedOutput);
    }
}
