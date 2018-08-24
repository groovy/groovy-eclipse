/*
 * Copyright 2009-2018 the original author or authors.
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

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isParrotParser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.jdt.groovy.internal.compiler.ast.AliasImportReference;
import org.codehaus.jdt.groovy.internal.compiler.ast.EventListener;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyClassScope;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTClassNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.junit.Ignore;
import org.junit.Test;

public final class GroovySimpleTests extends GroovyCompilerTestSuite {

    @Test
    public void testClosureSyntax() {
        runNegativeTest(new String[] {
            "Foo.groovy",
            "class A {\n"+
            "    A(closure) {\n"+
            "        closure()\n"+
            "    }\n"+
            "}\n"+
            "abc = {println 'abc'\n"+
            "}\n"+
            "new A({\n"+
            "    abc()\n"+
            "}) //works properly\n"+
            "new A() {   \n"+
            "    abc()\n"+
            "} // throw error: unexpected token: abc at line: 13, column: 3\n",
        },
        "----------\n" +
        "1. ERROR in Foo.groovy (at line 12)\n" +
        "\tabc()\n" +
        "\t^\n" +
        "Groovy:unexpected token: abc @ line 12, column 5.\n" +
        "----------\n");
    }

    @Test
    public void testGreclipse1521_pre() {
        runConformTest(new String[] {
            "Color.groovy",
            "enum Color { R,G,B }\n",
        });
     }

    @Test
    public void testGreclipse719() {
        runNegativeTest(new String[] {
            "MyDomainClass.groovy",
            "int anInt = 10;\n"+
            "def Method[] methodArray = anInt.class.methods;\n"+
            "println methodArray.name;",
        },
        "----------\n" +
        "1. ERROR in MyDomainClass.groovy (at line 2)\n" +
        "\tdef Method[] methodArray = anInt.class.methods;\n" +
        "\t    ^^^^^^^^\n" +
        "Groovy:unable to resolve class Method[]\n" +
        "----------\n");
    }

    @Test
    public void testGreclipse719_2() {
        runNegativeTest(new String[] {
            "MyDomainClass.groovy",
            "int anInt = 10;\n"+
            "def Method[][] methodMethodArray = anInt.class.methods;\n"+
            "println methodArray.name;",
        },
        "----------\n" +
        "1. ERROR in MyDomainClass.groovy (at line 2)\n" +
        "\tdef Method[][] methodMethodArray = anInt.class.methods;\n" +
        "\t    ^^^^^^^^^^\n" +
        "Groovy:unable to resolve class Method[][]\n" +
        "----------\n");
    }

    @Test
    public void testDuplicateClassesUnnecessaryExceptions() {
        runNegativeTest(new String[] {
            "A.groovy",
            "class Foo {}\n"+
            "class Foo {}",
        },
        "----------\n" +
        "1. ERROR in A.groovy (at line 2)\n" +
        "\tclass Foo {}\n" +
        "\t^^^^^^^^^^^^\n" +
        "Groovy:Invalid duplicate class definition of class Foo : The source A.groovy contains at least two definitions of the class Foo.\n" +
        "----------\n" +
        "2. ERROR in A.groovy (at line 2)\n" +
        "\tclass Foo {}\n" +
        "\t      ^^^\n" +
        "The type Foo is already defined\n" +
        "----------\n");
    }

    @Test
    public void testStaticProperty() {
        runConformTest(new String[] {
            "Super.groovy",
            "class Super {" +
            "  def static getSql() { return 'abc'; }\n" +
            "}\n"+
            "class Sub extends Super {\n" +
            "  def static m() {\n" +
            "    sql.charAt(0)\n"+
            "  }" +
            "}\n",
        });
    }

    @Test @Ignore
    public void testClash_GRE1076() {
        runConformTest(new String[] {
            "com/foo/Bar/config.groovy",
            "package com.foo.Bar\n"+
            "print 'abc'\n",

            "com/foo/Bar.java",
            "package com.foo;\n"+
            "class Bar {}\n",
        },
        "abc");
    }

    @Test @Ignore
    public void testClash_GRE1076_2() {
        runConformTest(new String[] {
            "com/foo/Bar.java",
            "package com.foo;\n"+
            "public class Bar { \n"+
            "  public static void main(String[] argv) {\n"+
            "    System.out.println(\"def\");\n"+
            "  }\n"+
            "}\n",

            "com/foo/Bar/config.groovy",
            "package com.foo.Bar\n"+
            "print 'abc'\n",
        },
        "def");
    }

    @Test
    public void testUnreachable_1047() {
        runConformTest(new String[] {
            "MyException.java",
            "class MyException extends Exception {\n"+
            "  private static final long serialVersionUID = 1L;\n"+
            "}",

            "CanThrowException.groovy",
            "\n"+
            "public interface CanThrowException {\n"+
            "  void thisCanThrowException() throws MyException\n"+
            "}\n",

            "ShouldCatchException.java",
            "\n"+
            "class ShouldCatchException {\n"+
            "  private CanThrowException thing;\n"+
            "  \n"+
            "  public void doIt() {\n"+
            "    try {\n"+
            "      thing.thisCanThrowException();\n"+
            "    } catch( MyException e ) {\n"+
            "      System.out.println(\"Did we catch it?\");\n"+
            "    }\n"+
            "  }\n"+
            "}\n",
        });
    }

    @Test
    public void testUnreachable_1047_2() {
        runConformTest(new String[] {
            "MyException.java",
            "class MyException extends Exception {\n"+
            "  private static final long serialVersionUID = 1L;\n"+
            "}",

            "CanThrowException.groovy",
            "\n"+
            "public class CanThrowException {\n"+
            "  public CanThrowException() throws MyException {\n"+
            "    throw new MyException();\n"+
            "  }\n"+
            "}\n",

            "ShouldCatchException.java",
            "\n"+
            "class ShouldCatchException {\n"+
            "  \n"+
            "  public void doIt() {\n"+
            "    try {\n"+
            "      new CanThrowException();\n"+
            "    } catch( MyException e ) {\n"+
            "      System.out.println(\"Did we catch it?\");\n"+
            "    }\n"+
            "  }\n"+
            "}\n",
        });
    }

    /**
     * This test is looking at what happens when a valid type is compiled ahead of two problem types (problematic
     * since they both declare the same class).  Although the first file gets through resolution OK, re-resolution
     * is attempted because the SourceUnit isn't tagged as having succeeded that phase - the exception thrown
     * for the real problem jumps over the tagging process.
     */
    @Test
    public void testDuplicateClassesUnnecessaryExceptions_GRE796() {
        runNegativeTest(new String[] {
            "spring/resources.groovy",
            "foo = {}\n",

            "A.groovy",
            "class Foo {}\n",

            "Foo.groovy",
            "class Foo {}",
        },
        "----------\n" +
        "1. ERROR in Foo.groovy (at line 1)\n" +
        "\tclass Foo {}\n" +
        "\t ^^^^^^^^^^^\n" +
        "Groovy:Invalid duplicate class definition of class Foo : The sources Foo.groovy and A.groovy each contain a class with the name Foo.\n" +
        "----------\n" +
        "2. ERROR in Foo.groovy (at line 1)\n" +
        "\tclass Foo {}\n" +
        "\t      ^^^\n" +
        "The type Foo is already defined\n" +
        "----------\n");
    }

    @Test
    public void testDuplicateClassesUnnecessaryExceptions_GRE796_2() {
        runNegativeTest(new String[] {
            "spring/resources.groovy",
            "foo = {}\n",

            "a/Foo.groovy",
            "package a\n"+
            "class Foo {}\n" +
            "class Foo {}",
        },
        "----------\n" +
        "1. ERROR in a\\Foo.groovy (at line 3)\n" +
        "\tclass Foo {}\n" +
        "\t^^^^^^^^^^^^\n" +
        "Groovy:Invalid duplicate class definition of class a.Foo : The source a"+File.separator+"Foo.groovy contains at least two definitions of the class a.Foo.\n" +
        "----------\n" +
        "2. ERROR in a\\Foo.groovy (at line 3)\n" +
        "\tclass Foo {}\n" +
        "\t      ^^^\n" +
        "The type Foo is already defined\n" +
        "----------\n");
    }

    @Test
    public void testGroovyColon_GRE801() {
        runNegativeTest(new String[] {
            "A.groovy",
            "httpClientControl.demand.generalConnection(1..1) = {->\n"+
            "currHttp\n"+
            "}\n",
        },
        "----------\n" +
        "1. ERROR in A.groovy (at line 1)\n" +
        "\thttpClientControl.demand.generalConnection(1..1) = {->\n" +
        "\t                                                  ^\n" +
        "Groovy:\"httpClientControl.demand.generalConnection((1..1))\" is a method call expression, but it should be a variable expression at line: 1 column: 50. File: A.groovy @ line 1, column 50.\n" +
        "----------\n");
    }

    @Test
    public void testStaticOuter_GRE944() {
        runNegativeTest(new String[] {
            "A.groovy",
            "static class A {\n"+
            "  public static void main(String[]argv) {print 'abc';}\n"+
            "}\n",
        },
        "----------\n" +
        "1. ERROR in A.groovy (at line 1)\n" +
        "\tstatic class A {\n" +
        "\t             ^\n" +
        "Groovy:The class \'A\' has an incorrect modifier static.\n" +
        "----------\n");
    }

    @Test
    public void testCrashRatherThanError_GRE986() {
        runNegativeTest(new String[] {
            "A.groovy",
            "hello \\u\n"+
            "class Foo {}\n",
        },
        "----------\n" +
        "1. ERROR in A.groovy (at line 1)\n" +
        "\thello \\u\n" +
        "\t       ^\n" +
        "Groovy:Did not find four digit hex character code. line: 1 col:7 @ line 1, column 7.\n" +
        "----------\n");
    }

    @Test
    public void testAmbiguous_GRE945_gu() {
        runConformTest(new String[] {
            "Code.groovy",
            "import bug.factory.*\n"+
            "class Code {\n"+
            "  static Factory fact = new Factory()\n"+
            "  public static void main(String[]argv) {\n"+
            "    fact.foo()\n"+
            "  }\n"+
            "}\n",

            "Factory.groovy",
            "package bug.factory\n"+
            "class Factory { static foo() { print 'abc'}}\n",
        },
        "abc");
    }

    @Test
    public void testAmbiguous_GRE945_jl() {
        runConformTest(new String[] {
            "Code.groovy",
            "import bug.factory.*\n"+
            "class Code {\n"+
            "  static StringBuffer fact = new StringBuffer()\n"+
            "  public static void main(String[]argv) {\n"+
            "    print fact.foo()\n"+
            "  }\n"+
            "}\n",

            "StringBuffer.groovy",
            "package bug.factory\n"+
            "class StringBuffer { static String foo() { return 'abc'}}\n",
        },
        "abc");
    }

    @Test
    public void testAmbiguous_GRE945_bothFromSource() {
        runConformTest(new String[] {
            "Code.groovy",
            "import a.*\n"+
            "import b.*\n"+
            "class Code {\n"+
            "  static A fact = new A()\n"+
            "  public static void main(String[]argv) {\n"+
            "    print fact.foo()\n"+
            "  }\n"+
            "}\n",

            "a/A.groovy",
            "package a\n"+
            "class A { static String foo() { return 'abc'}}\n",

            "b/A.groovy",
            "package b\n"+
            "class A { static String foo() { return 'def'}}\n",
        },
        "abc");
    }

    @Test
    public void testAmbiguous_GRE945_bothFromSource_2() {
        runConformTest(new String[] {
            "Code.groovy",
            "import b.*\n"+
            "import a.*\n"+
            "class Code {\n"+
            "  static A fact = new A()\n"+
            "  public static void main(String[]argv) {\n"+
            "    print fact.foo()\n"+
            "  }\n"+
            "}\n",

            "a/A.groovy",
            "package a\n"+
            "class A { static String foo() { return 'abc'}}\n",

            "b/A.groovy",
            "package b\n"+
            "class A { static String foo() { return 'def'}}\n",
        },
        "def");
    }

    @Test
    public void testAmbiguous_GRE945_bothFromSource_3() {
        runConformTest(new String[] {
            "Code.groovy",
            "import b.*\n"+
            "import a.*\n"+
            "class Code {\n"+
            "  static Process fact = new Process()\n"+
            "  public static void main(String[]argv) {\n"+
            "    print fact.foo()\n"+
            "  }\n"+
            "}\n",

            "a/Process.groovy",
            "package a\n"+
            "class Process { static String foo() { return 'abc'}}\n",

            "b/Process.groovy",
            "package b\n"+
            "class Process { static String foo() { return 'def'}}\n",
        },
        "def");
    }

    @Test
    public void testAmbiguous_GRE945_ju() {
        runConformTest(new String[] {
            "Code.groovy",
            "import bug.factory.*\n"+
            "class Code {\n"+
            "  static List fact = new List()\n"+
            "  public static void main(String[]argv) {\n"+
            "    fact.foo()\n"+
            "  }\n"+
            "}\n",

            "List.groovy",
            "package bug.factory\n"+
            "class List { static foo() { print 'abc'}}\n",
        },
        "abc");
    }

    @Test
    public void testAmbiguous_GRE945_jn() {
        runConformTest(new String[] {
            "Code.groovy",
            "import bug.factory.*\n"+
            "class Code {\n"+
            "  static Socket fact = new Socket()\n"+
            "  public static void main(String[]argv) {\n"+
            "    fact.foo()\n"+
            "  }\n"+
            "}\n",

            "Socket.groovy",
            "package bug.factory\n"+
            "class Socket { static foo() { print 'abc'}}\n",
        },
        "abc");
    }

    @Test
    public void testAmbiguous_GRE945_gl() {
        runConformTest(new String[] {
            "Code.groovy",
            "import bug.factory.*\n"+
            "class Code {\n"+
            "  static Tuple fact = new Tuple()\n"+
            "  public static void main(String[]argv) {\n"+
            "    fact.foo()\n"+
            "  }\n"+
            "}\n",

            "Tuple.groovy",
            "package bug.factory\n"+
            "class Tuple { static foo() { print 'abc'}}\n",
        },
        "abc");
    }

    @Test
    public void testAmbiguous_GRE945_ji() {
        runConformTest(new String[] {
            "Code.groovy",
            "import bug.factory.*\n"+
            "class Code {\n"+
            "  static Serializable fact = new Serializable()\n"+
            "  public static void main(String[]argv) {\n"+
            "    fact.foo()\n"+
            "  }\n"+
            "}\n",

            "Serializable.groovy",
            "package bug.factory\n"+
            "class Serializable { static foo() { print 'abc'}}\n",
        },
        "abc");
    }

    @Test
    public void testStaticOuter_GRE944_2() {
        runNegativeTest(new String[] {
            "B.java",
            "public class B {\n"+
            "  public static void main(String[] argv) {\n" +
            "    new A.C().foo();\n" +
            "  }\n" +
            "}\n",

            "A.groovy",
            "static class A {\n"+
            "  static class C {\n"+
            "  public void foo() {print 'abcd';}\n"+
            "  }\n"+
            "}\n",
        },
        "----------\n" +
        "1. ERROR in A.groovy (at line 1)\n" +
        "\tstatic class A {\n" +
        "\t             ^\n" +
        "Groovy:The class \'A\' has an incorrect modifier static.\n" +
        "----------\n");
    }

    @Test
    public void testEnumStatic_GRE974() {
        runConformTest(new String[] {
            "be/flow/A.groovy",
            "package be.flow\n"+
            "\n"+
            "enum C1{\n"+
            "	TEST_C1\n"+
            "}	\n"+
            "\n"+
            "class A {\n"+
            "	public enum C2{\n"+
            "		TEST_C2\n"+
            "	}\n"+
            "}",

            "be/flow/B.groovy",
            "package be.flow\n"+
            "\n"+
            "import static be.flow.C1.TEST_C1;\n"+
            "import static be.flow.A.C2.*;\n"+
            "\n"+
            "class B {\n"+
            "	\n"+
            "	B(){\n"+
            "		super(TEST_C2)\n"+
            "	}\n"+
            "	\n"+
            "	void doIt(){\n"+
            "		println(be.flow.C1.TEST_C1);\n"+
            "		println(be.flow.A.C2.TEST_C2);\n"+
            "		println(TEST_C2);\n"+
            "	}\n"+
            "	\n"+
            "}",

            "be/flow/D.groovy",
            "package be.flow\n"+
            "\n"+
            "import static be.flow.C1.TEST_C1;\n"+
            "import static be.flow.A.C2.*;\n"+
            "\n"+
            "class D {\n"+
            "	\n"+
            "	static void doIt(){\n"+
            "		println(be.flow.C1.TEST_C1);\n"+
            "		println(be.flow.A.C2.TEST_C2);\n"+
            "		println(TEST_C2);\n"+
            "	}\n"+
            "	\n"+
            "}",
        });
    }

    @Test
    public void testVarargs_GRE925() {
        runConformTest(new String[] {
            "Test.java",
            "public class Test {\n"+
            "  protected void method(String[] x) {}\n"+
            "  public static void main(String []argv) {}\n"+
            "}",

            "SubTest.groovy",
            "class SubTest extends Test {\n"+
            " protected void method(String[] x) { super.method(x) }\n"+
            "}",
        });
    }

    @Test
    public void testPrimitiveLikeTypeNames_GRE891() {
        runConformTest(new String[] {
            "Foo.java",
            "public class Foo {\n"+
            "public static void main(String[] args) {\n"+
            "  Z[][] zs = new Z().zzz();\n"+
            "  System.out.println(\"works\");\n"+
            "  }\n"+
            "}",

            "Z.groovy",
            "class Z {\n"+
            "   Z[][] zzz() { null }\n"+
            "}\n",
        },
        "works");
        // return type is a single char type (not in package and not primitive)
        assertEquals("[[LZ;", getReturnTypeOfMethod("Z.groovy", "zzz"));
    }

    @Test
    public void testPrimitiveLikeTypeNames_GRE891_2() {
        runConformTest(new String[] {
            "Foo.java",
            "public class Foo {\n"+
            "public static void main(String[] args) {\n"+
            "  int[][] zs = new Z().zzz();\n"+
            "  System.out.println(\"works\");\n"+
            "  }\n"+
            "}",

            "Z.groovy",
            "class Z {\n"+
            "   int[][] zzz() { null }\n"+
            "}\n",
        },
        "works");
        // return type is a primitive
        assertEquals("[[I", getReturnTypeOfMethod("Z.groovy", "zzz"));
    }

    @Test
    public void testPrimitiveLikeTypeNames_GRE891_3() {
        runConformTest(new String[] {
            "Foo.java",
            "public class Foo {\n"+
            "public static void main(String[] args) {\n"+
            "  java.lang.String[][] zs = new Z().zzz();\n"+
            "  System.out.println(\"works\");\n"+
            "  }\n"+
            "}",

            "Z.groovy",
            "class Z {\n"+
            "   java.lang.String[][] zzz() { null }\n"+
            "}\n",
        },
        "works");
        // return type is a qualified java built in type
        assertEquals("[[Ljava.lang.String;", getReturnTypeOfMethod("Z.groovy", "zzz"));
    }

    @Test
    public void testPrimitiveLikeTypeNames_GRE891_4() {
        runConformTest(new String[] {
            "pkg/Foo.java",
            "package pkg;\n"+
            "public class Foo {\n"+
            "public static void main(String[] args) {\n"+
            "  pkg.H[][] zs = new Z().zzz();\n"+
            "  System.out.println(\"works\");\n"+
            "  }\n"+
            "}\n",

            "Y.groovy",
            "package pkg\n"+
            "class H {}\n",
            "Z.groovy",
            "package pkg\n"+
            "class Z {\n"+
            "  H[][] zzz() { null }\n"+
            "}\n",
        },
        "works");
        // return type is a single char groovy type from a package
        assertEquals("[[Lpkg.H;", getReturnTypeOfMethod("Z.groovy", "zzz"));
    }

    @Test
    public void testPrimitiveLikeTypeNames_GRE891_5() {
        runConformTest(new String[] {
            "pkg/Foo.java",
            "package pkg;\n"+
            "public class Foo {\n"+
            "public static void main(String[] args) {\n"+
            "  H[][] zs = new Z().zzz();\n"+
            "  System.out.println(\"works\");\n"+
            "  }\n"+
            "}",

            "Y.java",
            "package pkg;\n"+
            "class H {}\n",

            "Z.groovy",
            "package pkg;\n"+
            "class Z {\n"+
            "   H[][] zzz() { null }\n"+
            "}\n",
        },
        "works");
        // return type is a single char java type from a package
        assertEquals("[[Lpkg.H;", getReturnTypeOfMethod("Z.groovy", "zzz"));
    }

    @Test
    public void testMixedModeInnerProperties_GRE597() {
        runConformTest(new String[] {
            "gr8/JointGroovy.groovy",
            "package gr8\n"+
            "\n"+
            "class JointGroovy {\n"+
            "StaticInner property\n"+
            "\n"+
            " static class StaticInner {\n"+
            "  NonStaticInner property2\n"+
            "\n"+
            "  class NonStaticInner {\n"+
            "    Closure property3 = {}\n"+
            "  }\n"+
            " }\n"+
            "}",

            "gr8/JointJava.java",
            "package gr8;\n"+
            "\n"+
            "import groovy.lang.Closure;\n"+
            "\n"+
            "public class JointJava {\n"+
            "    public void method() {\n"+
            "        Closure closure = new JointGroovy().getProperty().getProperty2().getProperty3();\n"+
            "    }\n"+
            "}",
        });
    }

    @Test
    public void testMixedModeInnerProperties2_GRE597() {
        runConformTest(new String[] {
            "gr8/JointGroovy.groovy",
            "package gr8\n"+
            "\n"+
            "class JointGroovy {\n"+
            "StaticInner property\n"+
            "\n"+
            " }\n"+
            // now the inner is not an inner (like the previous test) but the property3 still is
            " class StaticInner {\n"+
            "  NonStaticInner property2\n"+
            "\n"+
            "  class NonStaticInner {\n"+
            "    Closure property3 = {}\n"+
            "  }\n"+
            "}",

            "gr8/JointJava.java",
            "package gr8;\n"+
            "\n"+
            "import groovy.lang.Closure;\n"+
            "\n"+
            "public class JointJava {\n"+
            "    public void method() {\n"+
            "        Closure closure = new JointGroovy().getProperty().getProperty2().getProperty3();\n"+
            "    }\n"+
            "}",
        });
    }

    @Test
    public void testStaticVariableInScript() {
        runNegativeTest(new String[] {
            "Script.groovy",
            "enum Move { ROCK, PAPER, SCISSORS }\n"+
            "\n"+
            "final static BEATS = [\n"+
            "   [Move.ROCK,     Move.SCISSORS],\n"+
            "   [Move.PAPER,    Move.ROCK],\n"+
            "   [Move.SCISSORS, Move.PAPER]\n"+
            "].asImmutable()\n",
        },
        "----------\n" +
        "1. ERROR in Script.groovy (at line 3)\n" +
        "\tfinal static BEATS = [\n" +
        "\t             ^^^^^\n" +
        "Groovy:Modifier 'static' not allowed here.\n" +
        "----------\n");
    }

    // WMTW: What makes this work: the groovy compiler is delegated to for .groovy files
    @Test
    public void testStandaloneGroovyFile() {
        runConformTest(new String[] {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  public static void main(String[] argv) {\n"+
            "    print \"success\"\n" +
            "  }\n"+
            "}\n",
        },
        "success");
    }

    @Test
    public void testRecursion_GR531() {
        runNegativeTest(new String[] {
            "XXX.groovy",
            "class XXX extends XXX {\n" +
            "}\n",
        },
        "----------\n" +
        "1. ERROR in XXX.groovy (at line 1)\n" +
        "\tclass XXX extends XXX {\n" +
        "\t      ^^^\n" +
        "Groovy:Cyclic inheritance involving XXX in class XXX\n" +
        "----------\n" +
        "2. ERROR in XXX.groovy (at line 1)\n" +
        "\tclass XXX extends XXX {\n" +
        "\t                  ^^^\n" +
        "Cycle detected: the type XXX cannot extend/implement itself or one of its own member types\n" +
        "----------\n");
    }

    @Test
    public void testRecursion_GR531_2() {
        runNegativeTest(new String[] {
            "XXX.groovy",
            "class XXX extends XXX {\n" +
            "  public static void main(String[] argv) {\n"+
            "    print \"success\"\n" +
            "  }\n"+
            "}\n",
        },
        "----------\n" +
        "1. ERROR in XXX.groovy (at line 1)\n" +
        "\tclass XXX extends XXX {\n" +
        "\t      ^^^\n" +
        "Groovy:Cyclic inheritance involving XXX in class XXX\n" +
        "----------\n" +
        "2. ERROR in XXX.groovy (at line 1)\n" +
        "\tclass XXX extends XXX {\n" +
        "\t                  ^^^\n" +
        "Cycle detected: the type XXX cannot extend/implement itself or one of its own member types\n" +
        "----------\n");
    }

    @Test
    public void testRecursion_GR531_3() {
        runNegativeTest(new String[] {
            "XXX.groovy",
            "class XXX extends YYY {\n" +
            "  public static void main(String[] argv) {\n"+
            "    print \"success\"\n" +
            "  }\n"+
            "}\n",
            "YYY.groovy",
            "class YYY extends XXX {\n" +
            "  public static void main(String[] argv) {\n"+
            "    print \"success\"\n" +
            "  }\n"+
            "}\n",
        },
        "----------\n" +
        "1. ERROR in XXX.groovy (at line 1)\n" +
        "\tclass XXX extends YYY {\n" +
        "\t      ^^^\n" +
        "The hierarchy of the type XXX is inconsistent\n" +
        "----------\n" +
        "----------\n" +
        "1. ERROR in YYY.groovy (at line 1)\n" +
        "\tclass YYY extends XXX {\n" +
        "\t      ^^^\n" +
        "Groovy:Cyclic inheritance involving YYY in class YYY\n" +
        "----------\n" +
        "2. ERROR in YYY.groovy (at line 1)\n" +
        "\tclass YYY extends XXX {\n" +
        "\t                  ^^^\n" +
        "Cycle detected: a cycle exists in the type hierarchy between YYY and XXX\n" +
        "----------\n");
    }

    @Test
    public void testRecursion_GR531_4() {
        runNegativeTest(new String[] {
            "XXX.groovy",
            "interface XXX extends XXX {\n" +
            "}\n",
        },
        "----------\n" +
        "1. ERROR in XXX.groovy (at line 1)\n" +
        "\tinterface XXX extends XXX {\n" +
        "\t          ^^^\n" +
        "Groovy:Cyclic inheritance involving XXX in interface XXX\n" +
        "----------\n" +
        "2. ERROR in XXX.groovy (at line 1)\n" +
        "\tinterface XXX extends XXX {\n" +
        "\t                      ^^^\n" +
        "Cycle detected: the type XXX cannot extend/implement itself or one of its own member types\n" +
        "----------\n");
    }

    /**
     * The groovy object method augmentation (in GroovyClassScope) should only apply to types directly implementing GroovyObject, rather than
     * adding them all the way down the hierarchy.  This mirrors what happens in the compiler.
     */
    /**
     * First a class extending another.  The superclass gets augmented but not the subclass.
     */
    @Test
    public void testClassHierarchiesAndGroovyObjectMethods() {
        try {
            GroovyClassScope.debugListener = new EventListener("augment");
            runConformTest(new String[] {
                "Foo.groovy",
                "class Foo {\n"+
                "  static main(args) { print 'abc'} \n"+
                "}\n" +
                "class Two extends Foo {\n" +
                "  public void m() {\n"+
                "    Object o = getMetaClass();\n"+
                "  }\n"+
                "}\n",
            },
            "abc");
            assertEventCount(1,GroovyClassScope.debugListener);
            assertEvent("augment: type Foo having GroovyObject methods added", GroovyClassScope.debugListener);
            System.err.println(GroovyClassScope.debugListener.toString());
        } finally {
            GroovyClassScope.debugListener = null;
        }
    }

    /**
     * Now a class implementing an interface.  The subclass gets augmented because the superclass did not.
     */
    @Test
    public void testClassHierarchiesAndGroovyObjectMethods2() {
        try {
            GroovyClassScope.debugListener = new EventListener("augment");
            runConformTest(new String[] {
                "Foo.groovy",
                "class Foo implements One {\n" +
                "  public void m() {\n"+
                "    Object o = getMetaClass();\n"+
                "  }\n"+
                "  static main(args) { print 'abc'} \n"+
                "}\n"+
                "interface One {\n"+
                "}\n",
            },
            "abc");
            assertEventCount(1,GroovyClassScope.debugListener);
            assertEvent("augment: type Foo having GroovyObject methods added", GroovyClassScope.debugListener);
            System.err.println(GroovyClassScope.debugListener.toString());
        } finally {
            GroovyClassScope.debugListener = null;
        }
    }

    /**
     * Now a class extending a java type which extends a base groovy class.  Super groovy type should get them.
     *
     * This looks odd to me, not sure why Foo and One both get the methods when One inherits them through Foo -
     * perhaps the java type in the middle makes a difference.  Anyway by augmenting both of these we
     * are actually doing the same as groovyc, and that is the main thing.
     */
    @Test
    public void testClassHierarchiesAndGroovyObjectMethods3() {
        try {
            GroovyClassScope.debugListener = new EventListener();
            runConformTest(new String[] {
                "Foo.groovy",
                "class Foo extends Two {\n" +
                "  public void m() {\n"+
                "    Object o = getMetaClass();\n"+
                "  }\n"+
                "  static main(args) { print 'abc'} \n"+
                "}\n"+
                "class One {\n"+
                "}\n",
                "Two.java",
                "class Two extends One {}\n",
            },
            "abc");
            assertEventCount(2,GroovyClassScope.debugListener);
            assertEvent("augment: type One having GroovyObject methods added", GroovyClassScope.debugListener);
            assertEvent("augment: type Foo having GroovyObject methods added", GroovyClassScope.debugListener);
        } finally {
            GroovyClassScope.debugListener = null;
        }
    }

    @Test
    public void testStandaloneGroovyFile2() {
        runConformTest(new String[] {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
//			"  public static void main(String[] args) {\n"+
            "  static main(args) {\n"+
            "    print \"success\"\n" +
            "  }\n"+
            "}\n",
        },
        "success");
        checkGCUDeclaration("X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  public X() {\n" +
            "  }\n" +
            "  public static void main(java.lang.String... args) {\n" +
            "  }\n" +
            "}\n"
        );
    }

    @Test
    public void testParentIsObject_GRE528() {
        runConformTest(new String[] {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  static main(args) {\n"+
            "    print \"success\"\n" +
            "  }\n"+
            "}\n",
        },
        "success");
        checkGCUDeclaration("X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  public X() {\n" +
            "  }\n" +
            "  public static void main(java.lang.String... args) {\n" +
            "  }\n" +
            "}\n"
        );
    }

    @Test
    public void testInnerTypes() {
        runConformTest(new String[] {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            " class Inner {}\n"+
            "  static main(args) {\n"+
            "    print \"success\"\n" +
            "  }\n"+
            "}\n",
        },
        "success");

        checkGCUDeclaration("X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  public class Inner {\n" +
            "    public Inner() {\n"+
            "    }\n"+
            "  }\n"+
            "  public X() {\n" +
            "  }\n" +
            "  public static void main(java.lang.String... args) {\n" +
            "  }\n" +
            "}\n"
        );
    }

    @Test // GROOVY-4219
    public void testGRE637() {
        runConformTest(new String[] {
            "de/brazzy/nikki/Texts.java",
            "package de.brazzy.nikki;\n"+
            "\n"+
            "public final class Texts { \n"+
            "	public static class Image {\n"+
            "		public static final String ORDERED_BY_FILENAME = \"image.sortedby.filename\";\n"+
            "		public static final String ORDERED_BY_TIME = \"image.sortedby.time\";\n"+
            "}}\n",
            "de/brazzy/nikki/model/Image.groovy",
            "package de.brazzy.nikki.model;\n"+
            "\n"+
            "class Image implements Serializable{\n"+
            "    def fileName\n"+
            "    def time\n"+
            "}\n",
            "de/brazzy/nikki/model/ImageSortField.groovy",
            "package de.brazzy.nikki.model\n"+
            "\n"+
            "import de.brazzy.nikki.Texts\n"+
            "import de.brazzy.nikki.model.Image\n"+
            "\n"+
            "enum ImageSortField {\n"+
            "    FILENAME(field: Image.metaClass.fileName, name: Texts.Image.ORDERED_BY_FILENAME),\n"+
            "    TIME(field: Image.metaClass.time, name: Texts.Image.ORDERED_BY_TIME)\n"+
            "\n"+
            "    def field\n"+
            "    def name\n"+
            "\n"+
            "    public String toString(){\n"+
            "        name\n"+
            "    }\n"+
            "}\n",
        });
    }

    @Test
    public void testOverriding_GRE440() {
        runNegativeTest(new String[] {
            "Bar.groovy",
            "class Bar {\n" +
            "  static void main(args) {}\n" +
            "}\n",

            "Foo.java",
            "class Foo extends Bar { \n" +
            "  public static void main(String[] args) {}\n" +
            "}\n",
        },
        "");
    }

    @Test
    public void testOverriding_GRE440_2() {
        runNegativeTest(new String[] {
            "Bar.java",
            "class Bar {\n" +
            "  void main(String... strings) {}\n" +
            "}\n",

            "Foo.java",
            "class Foo extends Bar {\n" +
            "  void main(String[] strings) {}\n" +
            "}\n",
        },
        "----------\n" +
        "1. WARNING in Foo.java (at line 2)\n" +
        "\tvoid main(String[] strings) {}\n" +
        "\t     ^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Varargs methods should only override or be overridden by other varargs methods unlike Foo.main(String[]) and Bar.main(String...)\n" +
        "----------\n");
    }

    @Test
    public void testOverriding_FinalMethod1() {
        runNegativeTest(new String[] {
            "Bar.groovy",
            "class Bar {\n" +
            "  final def getFinalProperty() {}\n" +
            "}\n",

            "Foo.groovy",
            "class Foo extends Bar {\n" +
            "  def getFinalProperty() {}\n" +
            "}\n",
        },
        "----------\n" +
        "1. ERROR in Foo.groovy (at line 2)\n" +
        "\tdef getFinalProperty() {}\n" +
        "\t    ^^^^^^^^^^^^^^^^\n" +
        "Groovy:You are not allowed to override the final method getFinalProperty() from class 'Bar'.\n" +
        "----------\n");
    }

    @Test
    public void testOverriding_FinalMethod2() {
        runNegativeTest(new String[] {
            "Bar.groovy",
            "class Bar {\n" +
            "  final def getFinalProperty() {}\n" +
            "}\n",

            "Foo.groovy",
            "class Foo extends Bar {\n" +
            "  def finalProperty = 32\n" +
            "}\n",
        },
        "");
    }

    @Test
    public void testOverriding_ReducedVisibility1() {
        runNegativeTest(new String[] {
            "Bar.groovy",
            "class Bar { public void baz() {} }\n",

            "Foo.groovy",
            "class Foo extends Bar { private void baz() {}\n }\n",
        },
        "----------\n" +
        "1. ERROR in Foo.groovy (at line 1)\n" +
        "\tclass Foo extends Bar { private void baz() {}\n" +
        "\t                                     ^^^\n" +
        "Groovy:baz() in Foo cannot override baz in Bar; attempting to assign weaker access privileges; was public\n" +
        "----------\n");
    }

    @Test
    public void testOverriding_ReducedVisibility1a() {
        runNegativeTest(new String[] {
            "Bar.groovy",
            "class Bar { public void baz() {} }\n",

            "Foo.groovy",
            "class Foo extends Bar { protected void baz() {}\n }\n",
        },
        "----------\n" +
        "1. ERROR in Foo.groovy (at line 1)\n" +
        "\tclass Foo extends Bar { protected void baz() {}\n" +
        "\t                                       ^^^\n" +
        "Groovy:baz() in Foo cannot override baz in Bar; attempting to assign weaker access privileges; was public\n" +
        "----------\n");
    }

    @Test
    public void testOverriding_ReducedVisibility1b() {
        runNegativeTest(new String[] {
            "Bar.groovy",
            "class Bar { public void baz() {} }\n",

            "Foo.groovy",
            "class Foo extends Bar { @groovy.transform.PackageScope void baz() {}\n }\n",
        },
        "----------\n" +
        "1. ERROR in Foo.groovy (at line 1)\n" +
        "\tclass Foo extends Bar { @groovy.transform.PackageScope void baz() {}\n" +
        "\t                                                            ^^^\n" +
        "Groovy:baz() in Foo cannot override baz in Bar; attempting to assign weaker access privileges; was public\n" +
        "----------\n");
    }

    @Test
    public void testOverriding_ReducedVisibility2() {
        runNegativeTest(new String[] {
            "Bar.groovy",
            "class Bar { protected void baz() {} }\n",

            "Foo.groovy",
            "class Foo extends Bar { private void baz() {}\n }\n",
        },
        "----------\n" +
        "1. ERROR in Foo.groovy (at line 1)\n" +
        "\tclass Foo extends Bar { private void baz() {}\n" +
        "\t                                     ^^^\n" +
        "Groovy:baz() in Foo cannot override baz in Bar; attempting to assign weaker access privileges; was protected\n" +
        "----------\n");
    }

    @Test
    public void testOverriding_ReducedVisibility2a() {
        runNegativeTest(new String[] {
            "Bar.groovy",
            "class Bar { protected void baz() {} }\n",

            "Foo.groovy",
            "class Foo extends Bar { @groovy.transform.PackageScope void baz() {}\n }\n",
        },
        "----------\n" +
        "1. ERROR in Foo.groovy (at line 1)\n" +
        "\tclass Foo extends Bar { @groovy.transform.PackageScope void baz() {}\n" +
        "\t                                                            ^^^\n" +
        "Groovy:baz() in Foo cannot override baz in Bar; attempting to assign weaker access privileges; was protected\n" +
        "----------\n");
    }

    @Test
    public void testOverriding_ReducedVisibility3() {
        runNegativeTest(new String[] {
            "Bar.groovy",
            "class Bar { @groovy.transform.PackageScope void baz() {} }\n",

            "Foo.groovy",
            "class Foo extends Bar { private void baz() {}\n }\n",
        },
        "----------\n" +
        "1. ERROR in Foo.groovy (at line 1)\n" +
        "\tclass Foo extends Bar { private void baz() {}\n" +
        "\t                                     ^^^\n" +
        "Groovy:baz() in Foo cannot override baz in Bar; attempting to assign weaker access privileges; was package-private\n" +
        "----------\n");
    }

    @Test
    public void testOverriding_ReducedVisibility3a() {
        runNegativeTest(new String[] {
            "Bar.java",
            "public class Bar { void baz() {} }\n",

            "Foo.groovy",
            "class Foo extends Bar { private void baz() {}\n }\n",
        },
        "----------\n" +
        "1. ERROR in Foo.groovy (at line 1)\n" +
        "\tclass Foo extends Bar { private void baz() {}\n" +
        "\t                                     ^^^\n" +
        "Groovy:baz() in Foo cannot override baz in Bar; attempting to assign weaker access privileges; was package-private\n" +
        "----------\n");
    }

    @Test
    public void testAliasing_GRE473() {
        runConformTest(new String[] {
            "Foo.groovy",
            "import java.util.regex.Pattern as JavaPattern\n"+
            "class Pattern {JavaPattern javaPattern}\n"+
            "def p = new Pattern(javaPattern:~/\\d+/)\n"+
            "assert \"123\" ==~ p.javaPattern\n"+
            "print 'success '\n"+
            "print '['+p.class.name+']['+JavaPattern.class.name+']'\n",
        },
        "success [Pattern][java.util.regex.Pattern]");
    }

    @Test
    public void testAliasing_GRE473_2() {
        runNegativeTest(new String[] {
            "Foo.groovy",
            "import java.util.regex.Pattern\n"+
            "class Pattern {Pattern javaPattern}\n"+
            "def p = new Pattern(javaPattern:~/\\d+/)\n"+
            "assert \"123\" ==~ p.javaPattern\n"+
            "print 'success'\n",
        },
        "----------\n" +
        "1. ERROR in Foo.groovy (at line 1)\n" +
        "\timport java.util.regex.Pattern\n" +
        "\t       ^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "The import java.util.regex.Pattern conflicts with a type defined in the same file\n" +
        "----------\n");
    }

    @Test
    public void testEnumPositions_GRE1072() {
        runConformTest(new String[] {
            "Color.groovy",
            "enum Color {\n" +
            "  /** hello */\n"+
            "  RED,\n"+
            "  GREEN,\n"+
            "  BLUE\n"+
            "}\n",
        });

        GroovyCompilationUnitDeclaration decl = getCUDeclFor("Color.groovy");

        FieldDeclaration fDecl = findField(decl, "RED");
        assertEquals("RED sourceStart>sourceEnd:30>32 declSourceStart>declSourceEnd:15>32 modifiersSourceStart=30 endPart1Position:30", stringifyFieldDecl(fDecl));

        fDecl = findField(decl, "GREEN");
        assertEquals("GREEN sourceStart>sourceEnd:37>41 declSourceStart>declSourceEnd:37>41 modifiersSourceStart=37 endPart1Position:37", stringifyFieldDecl(fDecl));

        fDecl = findField(decl, "BLUE");
        assertEquals("BLUE sourceStart>sourceEnd:46>49 declSourceStart>declSourceEnd:46>49 modifiersSourceStart=46 endPart1Position:46", stringifyFieldDecl(fDecl));
    }

    @Test
    public void testEnumValues_GRE1071() {
        runConformTest(new String[] {
            "H.groovy",
            "enum H {\n"+
            "  RED,\n"+
            "  BLUE\n"+
            "}"
        });

        assertEquals("[LH;", getReturnTypeOfMethod("H.groovy", "values"));
    }

    @Test
    public void testAbstractCovariance_GRE272() {
        runNegativeTest(new String[] {
            "A.java",
            "public class A {}",

            "AA.java",
            "public class AA extends A{}",

            "I.java",
            "public interface I { A getA();}",

            "Impl.java",
            "public class Impl implements I { public AA getA() {return null;}}",

            "GImpl.groovy",
            "class GImpl extends Impl {}",
        },
        "");
    }

    // If GroovyFoo is processed *before* FooBase then the MethodVerifier15
    // hasn't had a chance to run on FooBase and create the synthetic bridge method
    @Test
    public void testAbstractCovariance_GRE272_2() {
        runNegativeTest(new String[] {
            "test/Bar.java",
            "package test;\n"+
            "public class Bar extends BarBase {}",

            "test/BarBase.java",
            "package test;\n"+
            "abstract public class BarBase {}",

            "test/GroovyFoo.groovy",
            "package test;\n"+
            "class GroovyFoo extends FooBase {}",

            "test/FooBase.java",
            "package test;\n"+
            "public class FooBase implements IFoo { public Bar foo() {return null;}}",


            "test/IFoo.java",
            "package test;\n"+
            "public interface IFoo { BarBase foo();}",
        },
        "");
    }

    @Test
    public void testAbstractCovariance_GRE272_3() {
        runNegativeTest(new String[] {
            "test/IFoo.java",
            "package test;\n"+
            "public interface IFoo { BarBase foo();}",

            "test/GroovyFoo.groovy",
            "package test;\n"+
            "class GroovyFoo extends FooBase {}",

            "test/FooBase.java",
            "package test;\n"+
            "public class FooBase implements IFoo { public Bar foo() {return null;}}",


            "test/BarBase.java",
            "package test;\n"+
            "abstract public class BarBase {}",

            "test/Bar.java",
            "package test;\n"+
            "public class Bar extends BarBase {}",
        },
        "");
    }

    @Test
    public void testAbstractCovariance_GRE272_4() {
        runNegativeTest(new String[] {
            "test/IFoo.java",
            "package test;\n"+
            "public interface IFoo { BarBase foo();}",

            "test/FooBase.java",
            "package test;\n"+
            "public class FooBase implements IFoo { public Bar foo() {return null;}}",

            "test/BarBase.java",
            "package test;\n"+
            "abstract public class BarBase {}",

            "test/Bar.java",
            "package test;\n"+
            "public class Bar extends BarBase {}",

            "test/GroovyFoo.groovy",
            "package test;\n"+
            "class GroovyFoo extends FooBase {}",
        },
        "");
    }

    @Test
    public void testIncorrectReturnType_GRE292() {
        runNegativeTest(new String[] {
            "Voidy.groovy",
            "public class VoidReturnTestCase {\n"+
            "\n"+
            "  void returnSomething() { \n"+
            "    return true && false   \n" +
            "  }\n"+
            "\n"+
            "}\n",
        },
        "----------\n" +
        "1. ERROR in Voidy.groovy (at line 4)\n" +
        "\treturn true && false   \n" +
        "\t^^^^^^^^^^^^^^^^^^^^\n" +
        "Groovy:Cannot use return statement with an expression on a method that returns void\n" +
        "----------\n");
    }

    @Test
    public void testIncorrectReturnType_GRE292_3() {
        runNegativeTest(new String[] {
            "Voidy.groovy",
            "public class VoidReturnTestCase {\n"+
            "\n"+
            "  void returnSomething() { \n"+
            "    return true\n" +
            "  }\n"+
            "\n"+
            "}\n",
        },
        "----------\n" +
        "1. ERROR in Voidy.groovy (at line 4)\n" +
        "\treturn true\n" +
        "\t^^^^^^^^^^^\n" +
        "Groovy:Cannot use return statement with an expression on a method that returns void\n" +
        "----------\n");
    }

    @Test
    public void testIncorrectReturnType_GRE292_2() {
        runNegativeTest(new String[] {
            "Voidy.groovy",
            "public class VoidReturnTestCase {\n"+
            "\n"+
            "  void returnSomething() { return true }\n"+
            "\n"+
            "}\n",
        },
        "----------\n"+
        "1. ERROR in Voidy.groovy (at line 3)\n" +
        "\tvoid returnSomething() { return true }\n" +
        "\t                         ^^^^^^^^^^^\n" +
        "Groovy:Cannot use return statement with an expression on a method that returns void\n" +
        "----------\n");
    }

    @Test
    public void testIncorrectReturnType_GRE292_4() {
        runNegativeTest(new String[] {
            "Voidy.groovy",
            "public class VoidReturnTestCase {\n"+
            "\n"+
            " void returnSomething() { return 375+26 }\n"+
            "\n"+
            "}\n",
        },
        "----------\n" +
        "1. ERROR in Voidy.groovy (at line 3)\n" +
        "\tvoid returnSomething() { return 375+26 }\n" +
        "\t                         ^^^^^^^^^^^^^\n" +
        "Groovy:Cannot use return statement with an expression on a method that returns void\n" +
        "----------\n");
    }

    @Test
    public void testMissingTypesForGeneratedBindingsGivesNPE_GRE273() {
        runNegativeTest(new String[] {
            "X.groovy",
            "import java.util.Map\n"+
            "import org.andrill.coretools.data.edit.Command\n"+
            "import org.andrill.coretools.data.edit.EditableProperty\n"+
            "import org.andrill.coretools.data.Model\n"+
            "import org.andrill.coretools.data.ModelCollection\n"+
            "import org.andrill.coretools.data.edit.commands.CompositeCommand\n"+
            "\n"+
            "class GProperty implements EditableProperty {\n"+
            "def source\n"+
            "String name\n"+
            "String widgetType\n"+
            "Map widgetProperties = [:]\n"+
            "Map constraints = [:]\n"+
            "def validators = []\n"+
            "Command command\n"+
            "\n"+
            "String getValue() {\n"+
            "if (source instanceof Model) { return source.modelData[name] } else { return (source.\"$name\" as String) }\n"+
            "}\n"+
            "\n"+
            "boolean isValid(String newValue) {\n"+
            "try {\n"+
            "return validators.inject(true) { prev, cur -> prev && cur.call([newValue, source]) }\n"+
            "} catch (e) { return false }\n"+
            "}\n"+
            "\n"+
            "Command getCommand(String newValue) {\n"+
            "if (constraints?.linkTo && source instanceof Model) {\n"+
            "def value = source.\"$name\"\n"+
            "def links = source.collection.models.findAll { it.class == source.class && it?.\"${constraints.linkTo}\" == value }\n"+
            "if (links) {\n"+
            "def commands = []\n"+
            "commands << new GCommand(source: source, prop: name, value: newValue)\n"+
            "links.each { commands << new GCommand(source: it, prop: constraints.linkTo, value: newValue) }\n"+
            "return new CompositeCommand(\"Change $name\", (commands as Command[]))\n"+
            "} else { return new GCommand(source: source, prop: name, value: newValue) }\n"+
            "} else { return new GCommand(source: source, prop: name, value: newValue) }\n"+
            "}\n"+
            "}\n",
        },
        "----------\n" +
        "1. ERROR in X.groovy (at line 2)\n" +
        "\timport org.andrill.coretools.data.edit.Command\n" +
        "\t       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Groovy:unable to resolve class org.andrill.coretools.data.edit.Command\n" +
        "----------\n" +
        "2. ERROR in X.groovy (at line 3)\n" +
        "\timport org.andrill.coretools.data.edit.EditableProperty\n" +
        "\t       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Groovy:unable to resolve class org.andrill.coretools.data.edit.EditableProperty\n" +
        "----------\n" +
        "3. ERROR in X.groovy (at line 4)\n" +
        "\timport org.andrill.coretools.data.Model\n" +
        "\t       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Groovy:unable to resolve class org.andrill.coretools.data.Model\n" +
        "----------\n" +
        "4. ERROR in X.groovy (at line 5)\n" +
        "\timport org.andrill.coretools.data.ModelCollection\n" +
        "\t       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Groovy:unable to resolve class org.andrill.coretools.data.ModelCollection\n" +
        "----------\n" +
        "5. ERROR in X.groovy (at line 6)\n" +
        "\timport org.andrill.coretools.data.edit.commands.CompositeCommand\n" +
        "\t       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Groovy:unable to resolve class org.andrill.coretools.data.edit.commands.CompositeCommand\n" +
        "----------\n" +
        "6. ERROR in X.groovy (at line 8)\n" +
        "\tclass GProperty implements EditableProperty {\n" +
        "\t      ^^^^^^^^^\n" +
        "Groovy:You are not allowed to implement the class \'org.andrill.coretools.data.edit.EditableProperty\', use extends instead.\n" +
        "----------\n" +
        "7. WARNING in X.groovy (at line 12)\n" +
        "\tMap widgetProperties = [:]\n" +
        "\t^^^\n" +
        "Map is a raw type. References to generic type Map<K,V> should be parameterized\n" +
        "----------\n" +
        "8. WARNING in X.groovy (at line 13)\n" +
        "\tMap constraints = [:]\n" +
        "\t^^^\n" +
        "Map is a raw type. References to generic type Map<K,V> should be parameterized\n" +
        "----------\n" +
        "9. ERROR in X.groovy (at line 33)\n" +
        "\tcommands << new GCommand(source: source, prop: name, value: newValue)\n" +
        "\t                ^^^^^^^^\n" +
        "Groovy:unable to resolve class GCommand\n" +
        "----------\n" +
        "10. ERROR in X.groovy (at line 34)\n" +
        "\tlinks.each { commands << new GCommand(source: it, prop: constraints.linkTo, value: newValue) }\n" +
        "\t                             ^^^^^^^^\n" +
        "Groovy:unable to resolve class GCommand\n" +
        "----------\n" +
        "11. ERROR in X.groovy (at line 36)\n" +
        "\t} else { return new GCommand(source: source, prop: name, value: newValue) }\n" +
        "\t                    ^^^^^^^^\n" +
        "Groovy:unable to resolve class GCommand\n" +
        "----------\n" +
        "12. ERROR in X.groovy (at line 37)\n" +
        "\t} else { return new GCommand(source: source, prop: name, value: newValue) }\n" +
        "\t                    ^^^^^^^^\n" +
        "Groovy:unable to resolve class GCommand\n" +
        "----------\n");
    }

    @Test
    public void testMissingTypesForGeneratedBindingsGivesNPE_GRE273_2() {
        runNegativeTest(new String[] {
            "A.groovy",
            "class A {\n"+
            "  String s;"+
            "  String getS(String foo) { return null;}\n"+
            "}"
        },
        "");
    }

    @Test
    public void testAbstractClass_GRE274() {
        runNegativeTest(new String[] {
            "p/Foo.groovy",
            "class Foo {\n" +
            "  public static void main(String[] argv) {\n"+
            "    new C();\n"+
            "  }\n"+
            "}\n",

            "p/C.java",
            "package p;\n" +
            "public abstract class C {\n" +
            "}\n",
        },
        "----------\n" +
        "1. ERROR in p\\Foo.groovy (at line 3)\n" +
        "\tnew C();\n" +
        "\t    ^\n" +
        "Groovy:unable to resolve class C\n" +
        "----------\n");
    }

    @Test
    public void testAbstractClass_GRE274_2() {
        runNegativeTest(new String[] {
            "p/Foo.groovy",
            "class Foo {\n" +
            "  public static void main(String[] argv) {\n"+
            "    new Wibble();\n"+
            "  }\n"+
            "}\n",

            "Wibble.groovy",
            "//@SuppressWarnings(\"cast\")\n"+
            "public class Wibble implements Comparable<String> {\n" +
            "}\n",
        },
        "----------\n" +
        "1. ERROR in Wibble.groovy (at line 2)\n" +
        "\tpublic class Wibble implements Comparable<String> {\n" +
        "\t             ^^^^^^\n" +
        "Groovy:Can\'t have an abstract method in a non-abstract class. The class \'Wibble\' must be declared abstract or the method \'int compareTo(java.lang.Object)\' must be implemented.\n" +
        "----------\n");
    }

    @Test
    public void testConstructorsForEnumWrong_GRE285() {
        runNegativeTest(new String[] {
            "TestEnum.groovy",
            "enum TestEnum {\n" +
            "\n" +
            "VALUE1(1, 'foo'),\n" +
            "VALUE2(2)\n" +
            "\n" +
            "private final int _value\n" +
            "private final String _description\n" +
            "\n" +
            "private TestEnum(int value, String description = null) {\n" +
            "   _value = value\n" +
            "   _description = description\n" +
            "}\n" +
            "\n" +
            "String getDescription() { _description }\n" +
            "\n" +
            "int getValue() { _value }\n" +
            "}\n",
        },
        "");
    }

    @Test @Ignore
    public void testCrashingOnBadCode_GRE290() {
        runNegativeTest(new String[] {
            "Moo.groovy",
            "package com.omxgroup.scripting;\n" +
            "\n" +
            "public class Moo {\n" +
            "public static def moo() { println this.class }\n" +
            "}\n",
        },
        "----------\n" +
        "1. ERROR in Moo.groovy (at line 4)\n" +
        "   public static def moo() { println this.class }\n" +
        "                                     ^\n" +
        "Groovy:class is declared in a dynamic context, but you tried to access it from a static context.\n" +
        "----------\n" +
        "2. ERROR in Moo.groovy (at line 4)\n" +
        "   public static def moo() { println this.class }\n" +
        "                                     ^\n" +
        "Groovy:Non-static variable \'this\' cannot be referenced from the static method moo.\n" +
        "----------\n");
    }

    @Test
    public void testCrashingOnBadCode_GRE290_2() {
        runNegativeTest(new String[] {
            "Moo.groovy",
            "public class Moo {\n" +
            "\n" +
            "public Moo processMoo(final moo) {\n" +
            "final moo = processMoo(moo)\n" +
            "return moo\n" +
            "}\n" +
            "}\n",
        },
        "----------\n" +
        "1. ERROR in Moo.groovy (at line 4)\n" +
        "\tfinal moo = processMoo(moo)\n" +
        "\t      ^^^\n" +
        "Groovy:The current scope already contains a variable of the name moo\n" +
        "----------\n");
    }

    // 'this' by itself isn't an error
    @Test
    public void testCrashingOnBadCode_GRE290_3() {
        runNegativeTest(new String[] {
            "Moo.groovy",
            "public class Moo {\n" +
            "public static def moo() { println this }\n" +
            "}\n"
        },
        "");
    }

    @Test
    public void testCrashingOnBadCode_GRE290_4() {
        runNegativeTest(new String[] {
            "p/X.groovy",
            "words: [].each { final item ->\n" +
            "  break words\n" +
            "}\n",
        },
        "----------\n" +
        "1. ERROR in p\\X.groovy (at line 2)\n" +
        "\tbreak words\n" +
        "\t^^^^^^^^^^^\n" +
        "Groovy:the break statement with named label is only allowed inside loops\n" +
        "----------\n");
    }

    @Test
    public void testNPE_GRE291() {
        runNegativeTest(new String[] {
            "ContinueTestCase.groovy",
            "public class ContinueTestCase {\n" +
            "\n" +
            "	public ContinueTestCase() {\n" +
            "		continue;\n" +
            "	}\n" +
            "}\n",
        },
        "----------\n" +
        "1. ERROR in ContinueTestCase.groovy (at line 4)\n" +
        "\tcontinue;\n" +
        "\t^^^^^^^^\n" +
        "Groovy:the continue statement is only allowed inside loops\n" +
        "----------\n");
    }

    @Test
    public void testMissingContext_GRE308() {
        runNegativeTest(new String[] {
            "DibDabs.groovy",
            "	def run(n) {\n\n" +
            "		  OtherGroovy.iterate (3) {\n" +
            "		  print it*2\n" +
            "	  }  \n" +
            "//		  		NOT RECORDED AGAINST THIS FILE??\n" +
            "		  int i        ",
        },
        "----------\n" +
        "1. ERROR in DibDabs.groovy (at line 7)\n" +
        "\tint i        \n" +
        "\t            ^\n" +
        "Groovy:expecting \'}\', found \'\' @ line 7, column 17.\n" +
        "----------\n");
    }

    // FIXASC less than ideal underlining for error location
    @Test
    public void testMissingContext_GRE308_2() {
        runNegativeTest(new String[] {
            "DibDabs.groovy",
            "	def run(n) {\n\n" +
            "		  OtherGroovy.iterate (3) {\n" +
            "		  print it*2\n" +
            "	  }  \n" +
            "//		  		NOT RECORDED AGAINST THIS FILE??\n" +
            "		  int i        \n",
        },
        "----------\n" +
        "1. ERROR in DibDabs.groovy (at line 7)\n" +
        "\tint i        \n" +
        "\n" +
        "\t             ^\n" +
        "Groovy:expecting \'}\', found \'\' @ line 7, column 18.\n" +
        "----------\n");
    }

    // a valid script, no '.' after session2
    @Test
    public void testInvalidScripts_GRE323_1() {
        runNegativeTest(new String[] {
            "Two.groovy",
            "def moo(closure) {\n" +
            "  closure();\n" +
            "}\n" +
            "\n" +
            "moo {\n" +
            "  final session2 = null\n" +
            "  \n" +
            "  // Define scenarios\n" +
            "  def secBoardRep = session2\n" +
            "  def x\n" +
            "}\n",
        },
        "");
    }

    @Test
    public void testInvalidScripts_GRE323_1b() {
        runConformTest(new String[] {
            "Two.groovy",
            "def moo(closure) {\n" +
            "  closure();\n" +
            "}\n" +
            "\n" +
            "moo {\n" +
            "  final session2 = null\n" +
            "  \n" +
            "  // Define scenarios\n" +
            "  def secBoardRep = session2\n" +
            "  def x\n" +
            "}\n",
        });
    }

    // '.' added, command line gives:
    //	One.groovy: 10: expecting '}', found 'x' @ line 10, column 7.
    //    def x
    //        ^
    @Test
    public void testInvalidScripts_GRE323_2() {
        // command expression syntax now allows this but it looks weird
        runConformTest(new String[] {
            "One.groovy",
            "def moo(closure) {\n" +
            "  closure();\n" +
            "}\n" +
            "\n" +
            "moo {\n" +
            "  final session2 = null\n" +
            "  \n" +
            "  // Define scenarios\n" +
            "  def secBoardRep = session2.\n" +
            "  def x\n" +
            "}\n",
        },
        "",
        "groovy.lang.MissingPropertyException: No such property: x for class: One");
    }

    // removed surrounding method
    @Test
    public void testInvalidScripts_GRE323_3() {
        runNegativeTest(new String[] {
            "Three.groovy",
            "def moo(closure) {\n" +
            "  closure();\n" +
            "}\n" +
            "\n" +
            "  final session2 = null\n" +
            "  \n" +
            "  // Define scenarios\n" +
            "  def secBoardRep = session2\n" +
            "  def x\n",
        },
        "");
    }

    @Test
    public void testInvalidScripts_GRE323_3b() {
        runConformTest(new String[] {
            "Three.groovy",
            "def moo(closure) {\n" +
            "  closure();\n" +
            "}\n" +
            "\n" +
            "  final session2 = null\n" +
            "  \n" +
            "  // Define scenarios\n" +
            "  def secBoardRep = session2\n" +
            "  def x\n",
        });
    }

    // no assignment for session2
    @Test
    public void testInvalidScripts_GRE323_4() {
        runConformTest(new String[] {
            "Four.groovy",
            "def moo(closure) {\n" +
            "  closure();\n" +
            "}\n" +
            "\n" +
            "moo {\n" +
            "  final session2 = null\n" +
            "  \n" +
            "  // Define scenarios\n" +
            "  session2.\n" +
            "  def x\n"+
            "}\n",
        },
        "",
        "groovy.lang.MissingPropertyException: No such property: x for class: Four");
    }

    @Test
    public void testInvalidScripts_GRE323_4b() {
        runConformTest(new String[] {
            "Run.java",
            "public class Run {\n"+
            "  public static void main(String[]argv) {\n"+
            "   try {\n"+
            "    Four.main(null);\n"+
            "   } catch (Throwable t) {\n"+
            "    System.out.println(t.getMessage());\n"+
            "   }\n"+
            "}\n"+
            "}",

            "Four.groovy",
            "def moo(closure) {\n" +
            "  closure();\n" +
            "}\n" +
            "\n" +
            "moo {\n" +
            "  final session2 = null\n" +
            "  \n" +
            "  // Define scenarios\n" +
            "  session2.\n" +
            "  def x\n"+
            "}\n",
        },
        "No such property: x for class: Four");
// actual exception:
//		"[ERR]:groovy.lang.MissingPropertyException: No such property: x for class: Four\n"+
//		"\tat org.codehaus.groovy.runtime.ScriptBytecodeAdapter.unwrap(ScriptBytecodeAdapter.java:49)\n"+
//		"\tat org.codehaus.groovy.runtime.callsite.PogoGetPropertySite.getProperty(PogoGetPropertySite.java:49)\n"+
//		"\tat org.codehaus.groovy.runtime.callsite.AbstractCallSite.callGroovyObjectGetProperty(AbstractCallSite.java:241)\n"+
//		"\tat Four$_run_closure1.doCall(Four.groovy:9)\n"+
//		"\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"+
//		"\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n"+
//		"\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n"+
//		"\tat java.lang.reflect.Method.invoke(Method.java:585)\n"+
//		"\tat org.codehaus.groovy.reflection.CachedMethod.invoke(CachedMethod.java:86)\n"+
//		"\tat groovy.lang.MetaMethod.doMethodInvoke(MetaMethod.java:234)\n"+
//		"\tat org.codehaus.groovy.runtime.metaclass.ClosureMetaClass.invokeMethod(ClosureMetaClass.java:272)\n"+
//		"\tat groovy.lang.MetaClassImpl.invokeMethod(MetaClassImpl.java:880)\n"+
//		"\tat org.codehaus.groovy.runtime.callsite.PogoMetaClassSite.callCurrent(PogoMetaClassSite.java:66)\n"+
//		"\tat org.codehaus.groovy.runtime.callsite.CallSiteArray.defaultCallCurrent(CallSiteArray.java:44)\n"+
//		"\tat org.codehaus.groovy.runtime.callsite.AbstractCallSite.callCurrent(AbstractCallSite.java:143)\n"+
//		"\tat org.codehaus.groovy.runtime.callsite.AbstractCallSite.callCurrent(AbstractCallSite.java:151)\n"+
//		"\tat Four$_run_closure1.doCall(Four.groovy)\n"+
//		"\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"+
//		"\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n"+
//		"\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n"+
//		"\tat java.lang.reflect.Method.invoke(Method.java:585)\n"+
//		"\tat org.codehaus.groovy.reflection.CachedMethod.invoke(CachedMethod.java:86)\n"+
//		"\tat groovy.lang.MetaMethod.doMethodInvoke(MetaMethod.java:234)\n"+
//		"\tat org.codehaus.groovy.runtime.metaclass.ClosureMetaClass.invokeMethod(ClosureMetaClass.java:272)\n"+
//		"\tat groovy.lang.MetaClassImpl.invokeMethod(MetaClassImpl.java:880)\n"+
//		"\tat org.codehaus.groovy.runtime.callsite.PogoMetaClassSite.call(PogoMetaClassSite.java:39)\n"+
//		"\tat org.codehaus.groovy.runtime.callsite.CallSiteArray.defaultCall(CallSiteArray.java:40)\n"+
//		"\tat org.codehaus.groovy.runtime.callsite.AbstractCallSite.call(AbstractCallSite.java:117)\n"+
//		"\tat org.codehaus.groovy.runtime.callsite.AbstractCallSite.call(AbstractCallSite.java:121)\n"+
//		"\tat Four.moo(Four.groovy:2)\n"+
//		"\tat Four$moo.callCurrent(Unknown Source)\n"+
//		"\tat org.codehaus.groovy.runtime.callsite.CallSiteArray.defaultCallCurrent(CallSiteArray.java:44)\n"+
//		"\tat org.codehaus.groovy.runtime.callsite.AbstractCallSite.callCurrent(AbstractCallSite.java:143)\n"+
//		"\tat org.codehaus.groovy.runtime.callsite.AbstractCallSite.callCurrent(AbstractCallSite.java:151)\n"+
//		"\tat Four.run(Four.groovy:5)\n"+
//		"\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"+
//		"\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n"+
//		"\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n"+
//		"\tat java.lang.reflect.Method.invoke(Method.java:585)\n"+
//		"\tat org.codehaus.groovy.reflection.CachedMethod.invoke(CachedMethod.java:86)\n"+
//		"\tat groovy.lang.MetaMethod.doMethodInvoke(MetaMethod.java:234)\n"+
//		"\tat groovy.lang.MetaClassImpl.invokeMethod(MetaClassImpl.java:1049)\n"+
//		"\tat groovy.lang.MetaClassImpl.invokeMethod(MetaClassImpl.java:880)\n"+
//		"\tat org.codehaus.groovy.runtime.InvokerHelper.invokePogoMethod(InvokerHelper.java:745)\n"+
//		"\tat org.codehaus.groovy.runtime.InvokerHelper.invokeMethod(InvokerHelper.java:728)\n"+
//		"\tat org.codehaus.groovy.runtime.InvokerHelper.runScript(InvokerHelper.java:383)\n"+
//		"\tat org.codehaus.groovy.runtime.InvokerHelper$runScript.call(Unknown Source)\n"+
//		"\tat org.codehaus.groovy.runtime.callsite.CallSiteArray.defaultCall(CallSiteArray.java:40)\n"+
//		"\tat org.codehaus.groovy.runtime.callsite.AbstractCallSite.call(AbstractCallSite.java:117)\n"+
//		"\tat org.codehaus.groovy.runtime.callsite.AbstractCallSite.call(AbstractCallSite.java:129)\n"+
//		"\tat Four.main(Four.groovy)\n"+
//		"\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"+
//		"\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n"+
//		"\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n"+
//		"\tat java.lang.reflect.Method.invoke(Method.java:585)\n"+
    }

    @Test
    public void testInvalidScripts_GRE323_5() {
        runNegativeTest(new String[] {
            "Five.groovy",
            "def moo(closure) {\n" +
            "  closure();\n" +
            "}\n" +
            "\n" +
            "moo {\n" +
            "  final session2 = [\"def\": { println \"DEF\" }]\n" +
            "  \n" +
            "  final x = 1\n"+
            "  // Define scenarios\n" +
            "  session2.\n" +
            "  def x\n"+
            "}\n",
        },
        "");
    }

    @Test
    public void testInvalidScripts_GRE323_5b() {
        runConformTest(new String[] {
            "Five.groovy",
            "def moo(closure) {\n" +
            "  closure();\n" +
            "}\n" +
            "\n" +
            "moo {\n" +
            "  final session2 = [\"def\": { println \"DEF\" }]\n" +
            "  \n" +
            "  final x = 1\n"+
            "  // Define scenarios\n" +
            "  session2.\n" +
            "  def x\n"+
            "}\n",
        },
        "DEF");
    }

    @Test
    public void testInvalidScripts_GRE323_6() {
        runConformTest(new String[] {
            "Six.groovy",
            "def moo(closure) {\n" +
            "  closure();\n" +
            "}\n" +
            "\n" +
            "moo {\n" +
            "  final session2 = [\"def\": { println \"DEF\" }]\n" +
            "  \n" +
            "  final x = 1\n"+
            "  // Define scenarios\n" +
            "  final y = session2.def x\n" +
            "}\n",
        },
        "DEF");
    }

    @Test
    public void testBridgeMethods_GRE336() {
        runNegativeTest(new String[] {
            "my/example/EnumBooleanMap.java",
            "package my.example;\n"+
            "\n"+
            "import java.util.EnumMap;\n"+
            "\n"+
            "@SuppressWarnings(\"serial\")\n"+
            "public class EnumBooleanMap<E extends Enum<E>> extends EnumMap<E, Boolean> {\n"+
            "	\n"+
            "	public EnumBooleanMap(Class<E> keyType) {\n"+
            "		super(keyType);\n"+
            "	}\n"+
            "\n"+
            "	public EnumBooleanMap(EnumBooleanMap<E> m) {\n"+
            "		super(m);\n"+
            "	}\n"+
            "\n"+
            "	@Override\n"+
            "	public Boolean get(Object key) {\n"+
            "		Boolean value = super.get(key);\n"+
            "		return value != null ? value : false;\n"+
            "	}\n"+
            "}\n",
        },
        "");
    }

    @Test
    public void testInnerTypeReferencing_GRE339() {
        runConformTest(new String[] {
            "Script.groovy",
            "class Script {\n"+
            "  public static void main(String[] argv) {\n"+
            "    print Outer.Inner.VAR\n"+
            "  }\n"+
            "}",

            "Outer.java",
            "public class Outer {\n"+
            "	  static class Inner {\n"+
            "	    static String VAR = \"value\";\n"+
            "	  }\n"+
            "	}\n",
        },
        "value");
    }

    // interface
    @Test
    public void testInnerTypeReferencing_GRE339_2() {
        runConformTest(new String[] {
            "Script.groovy",
            "class Script {\n"+
            "  public static void main(String[] argv) {\n"+
            "    print Outer.Inner.VAR\n"+
            "  }\n"+
            "}",

            "Outer.java",
            "public interface Outer {\n"+
            "	  interface Inner {\n"+
            "	    static String VAR = \"value\";\n"+
            "	  }\n"+
            "	}\n",
        },
        "value");
    }

    // pure script
    @Test
    public void testInnerTypeReferencing_GRE339_3() {
        runConformTest(new String[] {
            "script.groovy",
            "print Outer.Inner.VAR\n",

            "Outer.java",
            "public interface Outer {\n"+
            "	  interface Inner {\n"+
            "	    static String VAR = \"value\";\n"+
            "	  }\n"+
            "	}\n",
        },
        "value");
    }

    @Test
    public void testStaticProperties_GRE364() {
        runNegativeTest(new String[] {
            "Foo.groovy",
            "public class Foo { static String fubar }\n",

            "Bar.java",
            "public class Bar {\n"+
            "	  String fubar = Foo.getFubar();\n"+
            "	}\n",
        },
        "");
    }

    @Test
    public void testStaticProperties_GRE364_2() {
        runNegativeTest(new String[] {
            "Bar.java",
            "public class Bar {\n"+
            "  String fubar = Foo.getFubar();\n"+
            "}\n",

            "Foo.groovy",
            "public class Foo { static String fubar }\n",
        },
        "");
    }

    @Test
    public void testTransientMethod_GRE370() {
        runNegativeTest(new String[] {
            "Foo.groovy",
            "public class Foo {\n"+
            "  public transient void foo() {}\n"+
            "}\n",
        },
        "");
    }

    @Test
    public void testNotSeriousEnough_GRE396() {
        runNegativeTest(new String[] {
            "TrivialBugTest.groovy",
            "package org.sjb.sjblib.cmdline;\n"+
            "public class TrivialBugTest {\n"+
            "	void func2() {\n"+
            "		tb = new TrivialBug()\n"+
            "	}\n"+
            "}\n",

            "TrivialBug.groovy",
            "package org.sjb.sjblib.cmdline;\n"+
            "public class TrivialBug {\n"+
            "	void func() {\n"+
            "		return 5\n"+
            "	}\n"+
            "}\n",
        },
        "----------\n" +
        "1. ERROR in TrivialBug.groovy (at line 4)\n" +
        "\treturn 5\n" +
        "\t^^^^^^^^\n" +
        "Groovy:Cannot use return statement with an expression on a method that returns void\n" +
        "----------\n");
    }

    @Test
    public void testNotSeriousEnough_GRE396_2() {
        runNegativeTest(new String[] {
            "TrivialBug.groovy",
            "package org.sjb.sjblib.cmdline;\n"+
            "public class TrivialBug {\n"+
            "	void func() {\n"+
            "		return 5\n"+
            "	}\n"+
            "}\n",

            "TrivialBugTest.groovy",
            "package org.sjb.sjblib.cmdline;\n"+
            "public class TrivialBugTest {\n"+
            "	void func2() {\n"+
            "		tb = new TrivialBug()\n"+
            "	}\n"+
            "}\n",
        },
        "----------\n" +
        "1. ERROR in TrivialBug.groovy (at line 4)\n" +
        "\treturn 5\n" +
        "\t^^^^^^^^\n" +
        "Groovy:Cannot use return statement with an expression on a method that returns void\n" +
        "----------\n");
    }

    @Test
    public void testStarImports_GRE421() {
        runConformTest(new String[] {
            "Wibble.groovy",
            "import a.b.c.*;\n"+
            "class Wibble {\n"+
            "	 Process process = new Process()\n"+
            "  public static void main(String[] argv) { print new Wibble().process.class}\n"+
            "}\n",
            "a/b/c/Process.java",
            "package a.b.c;\n"+
            "public class Process {}\n",
        },
        "class a.b.c.Process");
    }

    // The getter for 'description' implements the interface
    @Test
    public void testImplementingAnInterfaceViaProperty() {
        runConformTest(new String[] {
            "a/b/c/C.groovy",
            "package a.b.c;\n" +
            "import p.q.r.I;\n"+
            "public class C implements I {\n" +
            "  String description;\n"+
            "  public static void main(String[] argv) {\n"+
            "    print \"success\"\n" +
            "  }\n"+
            "}\n",

            "p/q/r/I.groovy",
            "package p.q.r;\n" +
            "public interface I {\n" +
            "  String getDescription();\n"+
            "}\n",
        },
        "success");
    }

    // Referencing from a groovy to a java type where the reference is through a member, not the hierarchy
    @Test
    public void testReferencingOtherTypesInSamePackage() {
        runConformTest(new String[] {
            "a/b/c/C.groovy",
            "package a.b.c;\n" +
            "public class C {\n" +
            "  D description;\n"+
            "  public static void main(String[] argv) {\n"+
            "    print \"success\"\n" +
            "  }\n"+
            "}\n",

            "a/b/c/D.java",
            "package a.b.c;\n" +
            "public class D {\n" +
            "  String getDescription() { return null;}\n"+
            "}\n",
        },
        "success");
    }

    // Ensures that the Point2D.Double reference is resolved in the context of X and not Y (if Y is used then the import isn't found)
    @Test
    public void testMemberTypeResolution() {
        runConformTest(new String[] {
            "p/X.groovy",
            "package p;\n" +
            "import java.awt.geom.Point2D;\n"+
            "public class X {\n" +
            "  public void foo() {\n"+
            "    Object o = new Point2D.Double(p.x(),p.y());\n"+
            "  }\n"+
            "  public static void main(String[] argv) {\n"+
            "    print \"success\"\n" +
            "  }\n"+
            "}\n",
            "p/Y.groovy",
            "package p;\n" +
            "public class Y {\n" +
            "  public void foo() {\n"+
            "  }\n"+
            "  public static void main(String[] argv) {\n"+
            "    print \"success\"\n" +
            "  }\n"+
            "}\n",
        },
        "success");
    }

    @Test
    public void testFailureWhilstAttemptingToReportError() {
        runNegativeTest(new String[] {
            "T.groovy",
            "public class T {\n" +
            "	def x () {\n" +
            "		this \"\"\n" + // not a ctor call
            "	}\n"+
            "}\n",
        },
        isParrotParser() ? "" :
        "----------\n" +
        "1. ERROR in T.groovy (at line 3)\n" +
        "\tthis \"\"\n" +
        "\t     ^^\n" +
        "Groovy:Constructor call must be the first statement in a constructor. at line: 3 column: 8. File: T.groovy @ line 3, column 8.\n" +
        "----------\n");
    }

    @Test
    public void testProtectedType() {
        runConformTest(new String[] {
            "p/Y.groovy",
            "package p;\n" +
            "class Y {\n" +
            "  public static void main(String[]argv) {\n"+
            "    new X().main(argv);\n"+
            "  }\n"+
            "}\n",

            "p/X.groovy",
            "package p;\n" +
            "protected class X {\n" +
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",
        },
        "success");
    }

    @Test
    public void testEnums() {
        runNegativeTest(new String[] {
            "p/X.groovy",
            "package p;\n" +
            "public enum X {\n" +
            "}\n",
        },
        "");
    }

    @Test
    public void testEnums2() {
        try {
            JDTResolver.recordInstances = true;
            runConformTest(new String[] {
                "EE.groovy",
                "enum EE {A,B,C;}\n",

                "Foo.java",
                "public class Foo<E extends Foo<E>> implements Comparable<E> {" +
                "  public int compareTo(E b) { return 0;}\n"+
                "}\n",

                "Goo.java",
                "public class Goo<X extends Goo<X>> extends Foo<X> {}\n",
                "Bar.groovy",
                "abstract class Bar extends Goo<Bar> {" +
                "  int compareTo(Bar b) { return 0;}\n"+
                "  EE getEnum() { return null; }\n"+
                "}\n",
            });

            // Check on the state of Comparable
            JDTClassNode classnode = JDTResolver.getCachedNode("java.lang.Comparable<E>");
            assertNotNull(classnode);
            // Should have one method
            List<MethodNode> methods = classnode.getMethods();
            assertEquals(1,methods.size());
            assertEquals("int compareTo(java.lang.Object)",methods.get(0).getTypeDescriptor());

            classnode.lazyClassInit();
        } finally {
            JDTResolver.instances.clear();
            JDTResolver.recordInstances=false;
        }

        runConformTest(new String[] {
            "Foo.groovy",
            "class Foo<E extends Foo<E>> implements Comparable<E> {" +
            "  int compareTo(Object b) { return 0;}\n"+
            "}\n" +
            "\n",

            "Bar.groovy",
            "abstract class Bar extends Foo<Bar> {" +
            "  int compareTo(Bar b) { return 0;}\n"+
            "}\n",
        });
    }

    @Test
    public void testNonTerminalMissingImport() {
        runNegativeTest(new String[] {
            "p/X.groovy",
            "package p;\n" +
            "import a.b.c.D;\n"+
            "public class X {\n" +
            "  public static void main(String[]argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",
        },
        "----------\n" +
        "1. ERROR in p\\X.groovy (at line 2)\n" +
        "\timport a.b.c.D;\n" +
        "\t       ^^^^^^^\n" +
        "Groovy:unable to resolve class a.b.c.D\n" +
        "----------\n");
    }

    @Test
    public void testTypeClash() {
        runNegativeTest(new String[] {
            "p/X.groovy",
            "package p\n" +
            "public class X {}",

            "p/X.java",
            "package p;\n" +
            "public class X {}",
        },
        "----------\n" +
        "1. ERROR in p\\X.java (at line 2)\n" +
        "\tpublic class X {}\n" +
        "\t             ^\n" +
        "The type X is already defined\n" +
        "----------\n");
    }

    @Test
    public void testCallStaticMethodFromGtoJ() {
        runConformTest(new String[] {
            "p/Foo.groovy",
            "package p;\n" +
            "public class Foo {\n" +
            "  public static void main(String[]argv) {\n"+
            "    X.run()\n"+
            "  }\n"+
            "}\n",

            "p/X.java",
            "package p;\n"+
            "public class X {\n"+
            "  public static void run() {\n"+
            "    System.out.println(\"success\");\n"+
            "  }\n"+
            "}\n",
        },
        "success");
    }

    @Test
    public void testCallStaticMethodFromJtoG() {
        runConformTest(new String[] {
            "p/Foo.java",
            "package p;\n" +
            "public class Foo {\n" +
            "  public static void main(String[]argv) {\n"+
            "    X.run();\n"+
            "  }\n"+
            "}\n",

            "p/X.groovy",
            "package p;\n"+
            "public class X {\n"+
            "  public static void run() {\n"+
            "    System.out.println(\"success\");\n"+
            "  }\n"+
            "}\n",
        },
        "success");
    }

    @Test
    public void testNotMakingInterfacesImplementGroovyObject() {
        runConformTest(new String[] {
            "p/X.java",
            "package p;\n"+
            "public class X implements I {\n"+
            "  public static void main(String[] argv) {\n"+
            "    System.out.println(\"success\");\n"+
            "  }\n"+
            "}\n",

            "p/I.groovy",
            "package p;\n" +
            "public interface I {\n" +
            "}\n",
        },
        "success");
    }

    @Test
    public void testExtendingInterface1() {
        runNegativeTest(new String[] {
            "p/X.groovy",
            "package p;\n" +
            "public class X extends I {\n" +
            "}\n",

            "p/I.groovy",
            "package p\n"+
            "interface I {}",
        },
        "----------\n" +
        "1. ERROR in p\\X.groovy (at line 2)\n" +
        "\tpublic class X extends I {\n" +
        "\t             ^\n" +
        "Groovy:You are not allowed to extend the interface \'p.I\', use implements instead.\n" +
        "----------\n");
    }

    @Test
    public void testExtendingInterface2() {
        runNegativeTest(new String[] {
            "p/X.groovy",
            "package p;\n" +
            "public class X extends List<String> {\n" +
            "  public static void main(String[] argv) {\n"+
            "    print \"success\"\n" +
            "  }\n"+
            "}\n",
        },
        "----------\n" +
        "1. ERROR in p\\X.groovy (at line 2)\n" +
        "\tpublic class X extends List<String> {\n" +
        "\t             ^\n" +
        "Groovy:You are not allowed to extend the interface \'java.util.List\', use implements instead.\n" +
        "----------\n");
    }

    // WMTW: the type declaration building code creates the correct representation of A and adds the default constructor
    @Test
    public void testExtendingGroovyWithJava1() {
        runConformTest(new String[] {
            "p/B.java",
            "package p;\n"+
            "public class B extends A {\n"+
            "  public static void main(String[] argv) {\n"+
            "    new B();\n"+
            "    System.out.println(\"success\");\n"+
            "  }\n"+
            "}\n",

            "p/A.groovy",
            "package p;\n" +
            "public class A {\n" +
            "}\n",
        },
        "success");
    }

    // WMTW: a JDT resolver is plugged into groovy so it can see Java types
    // details:
    // 1. needed the lookupenvironment to flow down to the groovyparser (through initializeParser) as groovy will need it
    // for resolution of JDT types
    // 2. needed to subclass ResolveVisitor - trying just to override resolve(ClassNode) right now
    // 3. needed to build JDTClassNode and needed it to initialize the superclass field
    @Test
    public void testExtendingJavaWithGroovy1() {
        runConformTest(new String[] {
            "p/B.groovy",
            "package p;\n" +
            "public class B extends A {\n" +
            "  public static void main(String[] argv) {\n"+
            "    new B();\n"+
            "    System.out.println(\"success\");\n"+
            "  }\n"+
            "}\n",

            "p/A.java",
            "package p;\n"+
            "public class A {\n"+
            "}\n",
        },
        "success");
    }

    @Test
    public void testExtendingJavaWithGroovyAndThenJava() {
        runConformTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C extends B {\n" +
            "  public static void main(String[] argv) {\n"+
            "    new C();\n"+
            "    System.out.println(\"success\");\n"+
            "  }\n"+
            "}\n",

            "p/B.groovy",
            "package p;\n" +
            "public class B extends A {\n" +
            "}\n",

            "p/A.java",
            "package p;\n"+
            "public class A {\n"+
            "}\n",
        },
        "success");
    }

    // Groovy is allowed to have a public class like this in a file with a different name
    @Test
    public void testPublicClassInWrongFile() {
        runConformTest(new String[] {
            "pkg/One.groovy",
            "package pkg;\n" +
            "public class One {" +
            "  public static void main(String[]argv) { print \"success\";}\n" +
            "}\n"+
            "public class Two {" +
            "  public static void main(String[]argv) { print \"success\";}\n" +
            "}\n",
        },
        "success");
    }

    /**
     * WMTW: having a callback registered with groovy class generation that tracks which class file is created for which module node
     * details:
     * the groovy compilationunit provides a way to ask for the generated classes but it doesnt give a way to tell why they arose
     * (which sourceunit caused them to come into existence).  I am using the callback mechanism to track this information, but I worry
     * that we are causing groovy to perhaps do things too many times.  It also feels a little wierd that driving any single file through
     * to CLASSGEN drives them all through.  It isn't necessarily a problem, but it conflicts with the model of dealing with one file at
     * a time...
     */
    @Test
    public void testBuildingTwoGroovyFiles() {
        runConformTest(new String[] {
            "pkg/One.groovy",
            "package pkg;\n" +
            "class One {" +
            "  public static void main(String[]argv) { print \"success\";}\n" +
            "}\n",

            "pkg/Two.groovy",
            "package pkg;\n" +
            "class Two {}\n",
        },
        "success");
    }

    @Test
    public void testExtendingGroovyInterfaceWithJava() {
        runConformTest(new String[] {
            "pkg/C.java",
            "package pkg;\n" +
            "public class C extends groovy.lang.GroovyObjectSupport implements I {" +
            "  public static void main(String[]argv) {\n"+
            "    I i = new C();\n"+
            "    System.out.println( \"success\");" +
            "  }\n" +
            "}\n",

            "pkg/I.groovy",
            "package pkg;\n" +
            "interface I {}\n",
        },
        "success");
    }

    @Test
    public void testExtendingJavaInterfaceWithGroovy() {
        runConformTest(new String[] {
            "pkg/C.groovy",
            "package pkg;\n" +
            "public class C implements I {" +
            "  public static void main(String[]argv) {\n"+
            "    I i = new C();\n"+
            "    System.out.println( \"success\");" +
            "  }\n" +
            "}\n",

            "pkg/I.java",
            "package pkg;\n" +
            "interface I {}\n",
        },
        "success");
    }

    // WMTM: the fix for the previous code that tracks why classes are generated
    @Test
    public void testExtendingJavaWithGroovyAndThenJavaAndThenGroovy() {
        runConformTest(new String[] {
            "p/D.groovy",
            "package p;\n" +
            "class D extends C {\n" +
            "  public static void main(String[] argv) {\n"+
            "    new C();\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/C.java",
            "package p;\n" +
            "public class C extends B {}\n",

            "p/B.groovy",
            "package p;\n" +
            "public class B extends A {}\n",

            "p/A.java",
            "package p;\n"+
            "public class A {}\n",
        },
        "success");
    }

    @Test
    public void testImplementingInterface1() {
        runNegativeTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C extends groovy.lang.GroovyObjectSupport implements I {\n"+
            "  public static void main(String[] argv) {\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/I.groovy",
            "package p;\n"+
            "public interface I {\n" +
            "  void m();\n"+
            "}\n",
        },
        "----------\n" +
        "1. ERROR in p\\C.java (at line 2)\n" +
        "\tpublic class C extends groovy.lang.GroovyObjectSupport implements I {\n" +
        "\t             ^\n" +
        "The type C must implement the inherited abstract method I.m()\n" +
        "----------\n");
    }

    @Test
    public void testImplementingInterface2() {
        runConformTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C extends groovy.lang.GroovyObjectSupport implements I {\n"+
            "  public void m() {}\n"+
            "  public static void main(String[] argv) {\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/I.groovy",
            "package p;\n"+
            "public interface I {\n" +
            "  void m();\n"+
            "}\n",
        },
        "success");
    }

    @Test
    public void testImplementingInterface3() {
        runNegativeTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C extends groovy.lang.GroovyObjectSupport implements I {\n"+
            "  void m() {}\n"+
            "  public static void main(String[] argv) {\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/I.groovy",
            "package p;\n"+
            "public interface I {\n" +
            "  void m();\n"+
            "}\n",
        },
        "----------\n" +
        "1. ERROR in p\\C.java (at line 3)\n" +
        "\tvoid m() {}\n" +
        "\t     ^^^\n" +
        "Cannot reduce the visibility of the inherited method from I\n" +
        "----------\n");
    }

    @Test
    public void testImplementingInterface4() {
        runConformTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C extends groovy.lang.GroovyObjectSupport implements I {\n"+
            "  public String m() { return \"\";}\n"+
            "  public static void main(String[] argv) {\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/I.groovy",
            "package p;\n"+
            "public interface I {\n" +
            "  String m();\n"+
            "}\n",
        },
        "success");
    }

    @Test
    public void testImplementingInterface5() {
        runConformTest(new String[] {
            "C.groovy",
            "class C implements Comparator<Foo> {\n"+
            "  int compare(Foo one, Foo two) {\n"+
            "    one.bar <=> two.bar\n"+
            "  }\n"+
            "}\n",

            "Foo.groovy",
            "class Foo { String bar }\n",
        });
    }

    // WMTW: Groovy compilation unit scope adds the extra default import for java.util so List can be seen
    @Test
    public void testImplementingInterface_JavaExtendingGroovyAndImplementingMethod() {
        runNegativeTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "import java.util.List;\n"+
            "public class C extends groovy.lang.GroovyObjectSupport implements I {\n"+
            "  public String m() { return \"\";}\n"+
            "  public static void main(String[] argv) {\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/I.groovy",
            "package p;\n"+
            "public interface I {\n" +
            "  List m();\n"+
            "}\n",
        },
        "----------\n" +
        "1. ERROR in p\\C.java (at line 4)\n" +
        "\tpublic String m() { return \"\";}\n" +
        "\t       ^^^^^^\n" +
        "The return type is incompatible with I.m()\n" +
        "----------\n"+
        // this verifies the position report for the error against the return value of the method
        "----------\n" +
        "1. WARNING in p\\I.groovy (at line 3)\n" +
        "\tList m();\n" +
        "\t^^^^\n" +
        "List is a raw type. References to generic type List<E> should be parameterized\n" +
        "----------\n");
    }

    @Test
    public void testFieldPositioning01() {
        runNegativeTest(new String[] {
            "p/C.groovy",
            "package p;\n" +
            "public class C {\n"+
            "  List aList;\n"+
            "}\n",
        },
        "----------\n" +
        "1. WARNING in p\\C.groovy (at line 3)\n" +
        "\tList aList;\n" +
        "\t^^^^\n" +
        "List is a raw type. References to generic type List<E> should be parameterized\n" +
        "----------\n");
    }

    // FIXASC poor positional error for invalid field name - this test needs sorting out
    @Test
    public void testFieldPositioning02() {
        runNegativeTest(new String[] {
            "p/C.groovy",
            "package p;\n" +
            "public class C {\n"+
            "  List<String> class;\n"+
            "}\n",
        },
        "----------\n" +
        "1. ERROR in p\\C.groovy (at line 3)\n" +
        "\tList<String> class;\n" +
        "\t^\n" +
        "Groovy:unexpected token: List @ line 3, column 3.\n" +
        "----------\n");
    }

    @Test
    public void testImplementingInterface_JavaExtendingGroovyAndImplementingMethod_ArrayReferenceReturnType() {
        runNegativeTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "import java.util.List;\n"+
            "public class C extends groovy.lang.GroovyObjectSupport implements I {\n"+
            "  public String m() { return \"\";}\n"+
            "  public static void main(String[] argv) {\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/I.groovy",
            "package p;\n"+
            "public interface I {\n" +
            "  List[] m();\n"+
            "}\n",
        },
        "----------\n" +
        "1. ERROR in p\\C.java (at line 4)\n" +
        "\tpublic String m() { return \"\";}\n" +
        "\t       ^^^^^^\n" +
        "The return type is incompatible with I.m()\n" +
        "----------\n"+
        // this verifies the position report for the error against the return value of the method
        "----------\n" +
        "1. WARNING in p\\I.groovy (at line 3)\n" +
        "\tList[] m();\n" +
        "\t^^^^\n" +
        "List is a raw type. References to generic type List<E> should be parameterized\n" +
        "----------\n");
    }

    @Test
    public void testImplementingInterface_JavaExtendingGroovyAndImplementingMethod_QualifiedArrayReferenceReturnType() {
        runNegativeTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "import java.util.List;\n"+
            "public class C extends groovy.lang.GroovyObjectSupport implements I {\n"+
            "  public String m() { return \"\";}\n"+
            "  public static void main(String[] argv) {\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/I.groovy",
            "package p;\n"+
            "public interface I {\n" +
            "  java.util.List[] m();\n"+
            "}\n",
        },
        "----------\n" +
        "1. ERROR in p\\C.java (at line 4)\n" +
        "\tpublic String m() { return \"\";}\n" +
        "\t       ^^^^^^\n" +
        "The return type is incompatible with I.m()\n" +
        "----------\n"+
        // this verifies the position report for the error against the return value of the method
        "----------\n" +
        "1. WARNING in p\\I.groovy (at line 3)\n" +
        "\tjava.util.List[] m();\n" +
        "\t^^^^^^^^^^^^^^\n" +
        "List is a raw type. References to generic type List<E> should be parameterized\n" +
        "----------\n");
    }

    @Test
    public void testImplementingInterface_JavaExtendingGroovyAndImplementingMethod_ParamPosition() {
        runNegativeTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "import java.util.List;\n"+
            "public class C extends groovy.lang.GroovyObjectSupport implements I {\n"+
            "  public void m(String s) { }\n"+
            "  public static void main(String[] argv) {\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/I.groovy",
            "package p;\n"+
            "public interface I {\n" +
            "  void m(List l);\n"+
            "}\n",
        },
        "----------\n" +
        "1. ERROR in p\\C.java (at line 3)\n" +
        "\tpublic class C extends groovy.lang.GroovyObjectSupport implements I {\n" +
        "\t             ^\n" +
        "The type C must implement the inherited abstract method I.m(List)\n" +
        "----------\n" +
        // this verifies the position report for the error against the method parameter
        "----------\n" +
        "1. WARNING in p\\I.groovy (at line 3)\n" +
        "\tvoid m(List l);\n" +
        "\t       ^^^^\n" +
        "List is a raw type. References to generic type List<E> should be parameterized\n" +
        "----------\n");
    }

    @Test
    public void testImplementingInterface_JavaExtendingGroovyAndImplementingMethod_QualifiedParamPosition() {
        runNegativeTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "import java.util.List;\n"+
            "public class C extends groovy.lang.GroovyObjectSupport implements I {\n"+
            "  public void m(String s) { }\n"+
            "  public static void main(String[] argv) {\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/I.groovy",
            "package p;\n"+
            "public interface I {\n" +
            "  void m(java.util.List l);\n"+
            "}\n",
        },
        "----------\n" +
        "1. ERROR in p\\C.java (at line 3)\n" +
        "\tpublic class C extends groovy.lang.GroovyObjectSupport implements I {\n" +
        "\t             ^\n" +
        "The type C must implement the inherited abstract method I.m(List)\n" +
        "----------\n" +
        // this verifies the position report for the error against the method parameter+
        "----------\n" +
        "1. WARNING in p\\I.groovy (at line 3)\n" +
        "\tvoid m(java.util.List l);\n" +
        "\t       ^^^^^^^^^^^^^^\n" +
        "List is a raw type. References to generic type List<E> should be parameterized\n" +
        "----------\n");
    }

    @Test
    public void testImplementingInterface_MethodWithParameters_GextendsJ() {
        runConformTest(new String[] {
            "p/C.groovy",
            "package p;\n" +
            "public class C implements I<Integer> {\n"+
            "  public void m(String s) { }\n"+
            "  public static void main(String[] argv) {\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/I.java",
            "package p;\n"+
            "public interface I<T extends Number> {\n" +
            "  void m(String s);\n"+
            "}\n",
        },
        "success");
    }

    // Test that the alias is recognized when referenced as superclass
    // WMTW: the code Scope.getShortNameFor()
    @Test
    public void testImportAliasingGoober() {
        runConformTest(new String[] {
            "p/C.groovy",
            "package p;\n" +
            "import java.util.HashMap as Goober;\n"+
            "public class C extends Goober {\n"+
            "  public static void main(String[] argv) {\n"+
            "    print 'q.A.run'\n"+
            "  }\n"+
            "}\n",
        },
        "q.A.run");
    }

    @Test
    public void testImportAliasing() {
        runConformTest(new String[] {
            "p/C.groovy",
            "package p;\n" +
            "import q.A as AA;\n"+
            "import r.A as AB;\n"+
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    callitOne(new AA());\n"+
            "    callitTwo(new AB());\n"+
            "  }\n"+
            "  public static void callitOne(AA a) { a.run();}\n"+
            "  public static void callitTwo(AB a) { a.run();}\n"+
            "}\n",

            "q/A.java",
            "package q;\n"+
            "public class A {\n" +
            "  public static void run() { System.out.print(\"q.A.run \");}\n"+
            "}\n",

            "r/A.java",
            "package r;\n"+
            "public class A {\n" +
            "  public static void run() { System.out.print(\"r.A.run\");}\n"+
            "}\n",
        },
        "q.A.run r.A.run");
    }

    @Test
    public void testImportAliasingAndOldReference() {
        runNegativeTest(new String[] {
            "p/C.groovy",
            "package p;\n" +
            "import q.A as AA;\n"+
            "import r.A as AB;\n"+
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    callitOne(new AA());\n"+
            "  }\n"+
            "  public static void callitOne(A a) { }\n"+ // no A imported!
            "}\n",

            "q/A.java",
            "package q;\n"+
            "public class A {\n" +
            "  public static void run() { System.out.print(\"q.A.run \");}\n"+
            "}\n",

            "r/A.java",
            "package r;\n"+
            "public class A {\n" +
            "  public static void run() { System.out.print(\"r.A.run\");}\n"+
            "}\n",
        },
        "----------\n" +
        "1. ERROR in p\\C.groovy (at line 8)\n" +
        "\tpublic static void callitOne(A a) { }\n" +
        "\t                             ^\n" +
        "Groovy:unable to resolve class A\n" +
        "----------\n");
    }

    @Test
    public void testImportInnerInner01() {
        runConformTest(new String[] {
            "p/C.groovy",
            "package p;\n" +
            "public class C {\n"+
            "  private static Wibble.Inner.Inner2 wibbleInner = new G();\n"+
            "  public static void main(String[] argv) {\n"+
            "    wibbleInner.run();\n"+
            "  }\n"+
            "}\n"+
            "class G extends Wibble.Inner.Inner2  {}",

            "p/Wibble.java",
            "package p;\n"+
            "public class Wibble {\n" +
            "  public static class Inner {\n"+
            "    public static class Inner2 {\n"+
            "      public static void run() { System.out.print(\"p.Wibble.Inner.Inner2.run \");}\n"+
            "    }\n"+
            "  }\n"+
            "}\n",
        },
        "p.Wibble.Inner.Inner2.run");
    }

    @Test
    public void testImportInnerClass01_JavaCase() {
        runConformTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "import x.y.z.Wibble.Inner;\n"+
            "\n"+
            "public class C {\n"+
            "  private static Inner wibbleInner = new Inner();\n"+
            "  public static void main(String[] argv) {\n"+
            "    wibbleInner.run();\n"+
            "  }\n"+
            "}\n",

            "x/y/z/Wibble.java",
            "package x.y.z;\n"+
            "public class Wibble {\n" +
            "  public static class Inner {\n"+
            "    public static void run() { System.out.print(\"q.A.run \");}\n"+
            "  }\n"+
            "}\n",
        },
        "q.A.run");
    }

    // FIXASC need to look at all other kinds of import - statics/double nested static classes/etc
    @Test
    public void testImportInnerClass01_GroovyCase() {
        runConformTest(new String[] {
            "p/C.groovy",
            "package p;\n" +
            "import x.y.z.Wibble.Inner\n"+
            "\n"+
            "public class C {\n"+
            "  private static Inner wibbleInner = new Inner();\n"+
            "  public static void main(String[] argv) {\n"+
            "    wibbleInner.run();\n"+
            "  }\n"+
            "}\n",

            "x/y/z/Wibble.java",
            "package x.y.z;\n"+
            "public class Wibble {\n" +
            "  public static class Inner {\n"+
            "    public static void run() { System.out.print(\"q.A.run \");}\n"+
            "  }\n"+
            "}\n",
        },
        "q.A.run");
    }

    @Test
    public void testImportInnerClass() {
        runConformTest(new String[] {
            "p/C.groovy",
            "package p;\n" +
            "import x.y.z.Wibble.Inner /*as WibbleInner*/;\n"+
            "public class C {\n"+
            "  private static Inner wibbleInner = new Inner();\n"+
            "  public static void main(String[] argv) {\n"+
            "    wibbleInner.run();\n"+
            "  }\n"+
            //"  public static void callitOne(WibbleInner a) { a.run();}\n"+
            "}\n",

            "x/y/z/Wibble.java",
            "package x.y.z;\n"+
            "public class Wibble {\n" +
            "  public static class Inner {\n"+
            "    public void run() { System.out.print(\"run\");}\n"+
            "  }\n"+
            "}\n",
        },
        "run");
    }

    @Test
    public void testImportAliasingInnerClass() {
        runConformTest(new String[] {
            "p/C.groovy",
            "package p;\n" +
            "import x.y.z.Wibble.Inner as WibbleInner;\n"+
            "public class C {\n"+
            "  private static WibbleInner wibbleInner = new WibbleInner();\n"+
            "  public static void main(String[] argv) {\n"+
            "   wibbleInner.run();\n"+
            "  }\n"+
            "}\n",

            "x/y/z/Wibble.java",
            "package x.y.z;\n"+
            "public class Wibble {\n" +
            "  public static class Inner {\n"+
            "    public void run() { System.out.print(\"run \");}\n"+
            "  }\n"+
            "}\n",
        },
        "run");
    }

    @Test
    public void testImplementingInterface_MethodWithParameters_JextendsG() {
        runConformTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C extends groovy.lang.GroovyObjectSupport implements I<Integer> {\n"+
            "  public void m(String s) { }\n"+
            "  public static void main(String[] argv) {\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/I.groovy",
            "package p;\n"+
            "public interface I<T extends Number> {\n" +
            "  void m(String s);\n"+
            "}\n",
        },
        "success");
    }

    @Test
    public void testImplementingInterface_MethodWithParameters2_JextendsG() {
        runConformTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C extends groovy.lang.GroovyObjectSupport implements I<Integer> {\n"+
            "  public void m(String s, Integer i) { }\n"+
            "  public static void main(String[] argv) {\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/I.groovy",
            "package p;\n"+
            "public interface I<T extends Number> {\n" +
            "  void m(String s, Integer i);\n"+
            "}\n",
        },
        "success");
    }

    @Test
    public void testImplementingInterface_MethodWithParameters2_GextendsJ() {
        runConformTest(new String[] {
            "p/C.groovy",
            "package p;\n" +
            "public class C implements I<Integer> {\n"+
            "  public void m(String s, Integer i) { return null;}\n"+
            "  public static void main(String[] argv) {\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/I.java",
            "package p;\n"+
            "public interface I<T extends Number> {\n" +
            "  void m(String s, Integer i);\n"+
            "}\n",
        },
        "success");
    }

    @Test
    public void testImplementingInterface_MethodWithParameters3_GextendsJ() {
        runConformTest(new String[] {
            "p/C.groovy",
            "package p;\n" +
            "public class C implements I<Integer> {\n"+
            "  public void m(String s, Integer i) { return null;}\n"+
            "  public static void main(String[] argv) {\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/I.java",
            "package p;\n"+
            "public interface I<T extends Number> {\n" +
            "  void m(String s, T t);\n"+
            "}\n",
        },
        "success");
    }

    @Test
    public void testImplementingInterface_MethodWithParameters3_JextendsG() {
        runConformTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C extends groovy.lang.GroovyObjectSupport implements I<Integer> {\n"+
            "  public void m(String s, Integer i) { }\n"+
            "  public static void main(String[] argv) {\n"+
            "    System.out.println( \"success\");\n"+
            "  }\n"+
            "}\n",

            "p/I.groovy",
            "package p;\n"+
            "public interface I<T extends Number> {\n" +
            "  void m(String s, T t);\n"+
            "}\n",
        },
        "success");
    }

    @Test
    public void testCallingMethods_JcallingG() {
        runConformTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    new GClass().run();\n"+
            "  }\n"+
            "}\n",

            "p/GClass.groovy",
            "package p;\n"+
            "public class GClass {\n" +
            "  void run() {\n" +
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",
        },
        "success");
    }

    @Test
    public void testCallingMethods_GcallingJ() {
        runConformTest(new String[] {
            "p/C.groovy",
            "package p;\n" +
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    new OtherClass().run();\n"+
            "  }\n"+
            "}\n",

            "p/OtherClass.java",
            "package p;\n"+
            "public class OtherClass {\n" +
            "  void run() {\n" +
            "    System.out.println(\"success\");\n"+
            "  }\n"+
            "}\n",
        },
        "success");
    }

    @Test
    public void testReferencingFields_JreferingToG() {
        runConformTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    OtherClass oClass = new OtherClass();\n"+
            "    System.out.println(oClass.message);\n"+
            "  }\n"+
            "}\n",

            "p/OtherClass.groovy",
            "package p;\n"+
            "public class OtherClass {\n" +
            "  public String message =\"success\";\n"+
            "}\n",
        },
        "success");
    }

    @Test
    public void testReferencingFields_GreferingToJ() {
        runConformTest(new String[] {
            "p/C.groovy",
            "package p;\n" +
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    OtherClass oClass = new OtherClass();\n"+
            "    System.out.println(oClass.message);\n"+
            "  }\n"+
            "}\n",

            "p/OtherClass.java",
            "package p;\n"+
            "public class OtherClass {\n" +
            "  public String message =\"success\";\n"+
            "}\n",
        },
        "success");
    }

    @Test
    public void testCallingConstructors_JcallingG() {
        runConformTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    OtherClass oClass = new OtherClass();\n"+
            "    System.out.println(\"success\");\n"+
            "  }\n"+
            "}\n",

            "p/OtherClass.groovy",
            "package p;\n"+
            "public class OtherClass {\n" +
            "  public OtherClass() {\n"+
            "  }\n"+
            "}\n",
        },
        "success");
    }

    @Test
    public void testCallingConstructors_GcallingJ() {
        runConformTest(new String[] {
            "p/C.groovy",
            "package p;\n" +
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    OtherClass oClass = new OtherClass();\n"+
            "    System.out.println(\"success\");\n"+
            "  }\n"+
            "}\n",

            "p/OtherClass.java",
            "package p;\n"+
            "public class OtherClass {\n" +
            "  public OtherClass() {\n"+
            "  }\n"+
            "}\n",
        },
        "success");
    }

    @Test
    public void testGroovyObjectsAreGroovyAtCompileTime() {
        runConformTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    groovy.lang.GroovyObject oClass = new OtherClass();\n"+
            "    System.out.println(\"success\");\n"+
            "  }\n"+
            "}\n",

            "p/OtherClass.groovy",
            "package p;\n"+
            "import java.util.*;\n"+
            "public class OtherClass {\n" +
            "}\n",
        },
        "success");
    }

    @Test
    public void testCallGroovyObjectMethods_invokeMethod() {
        runConformTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    groovy.lang.GroovyObject oClass = new OtherClass();\n"+
            "    String s = (String)oClass.invokeMethod(\"toString\",null);\n"+
            "    System.out.println(s);\n"+
            "  }\n"+
            "}\n",

            "p/OtherClass.groovy",
            "package p;\n"+
            "import java.util.*;\n"+
            "public class OtherClass {\n" +
            "  String toString() { return \"success\";}\n"+
            "}\n",
        },
        "success");
    }

    @Test
    public void testGroovyObjectsAreGroovyAtRunTime() {
        runConformTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    OtherClass oClass = new OtherClass();\n"+
            "    System.out.println(oClass instanceof groovy.lang.GroovyObject);\n"+
            "  }\n"+
            "}\n",

            "p/OtherClass.groovy",
            "package p;\n"+
            "import java.util.*;\n"+
            "public class OtherClass {\n" +
            "}\n",
        },
        "true");
    }

    @Test
    public void testGroovyBug() {
        runConformTest(new String[] {
            "p/A.groovy",
            "package p;\n" +
            "public class A<T> { public static void main(String[]argv) { print \"a\";}}\n",

            "p/B.groovy",
            "package p;\n" +
            "public class B extends A<String> {}",
        },
        "a");
    }

    @Test
    public void testGroovyBug2() {
        runConformTest(new String[] {
            "p/B.groovy",
            "package p;\n" +
            "public class B extends A<String> {public static void main(String[]argv) { print \"a\";}}",

            "p/A.groovy",
            "package p;\n" +
            "public class A<T> { }\n",
        },
        "a");
    }

    // was worried <clinit> would surface in list of methods used to build the type declaration, but that doesn't appear to be the case
    @Test
    public void testExtendingGroovyObjects_clinit() {
        runConformTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    OtherClass oClass = new OtherClass();\n"+
            "    System.out.println(\"success\");\n"+
            "  }\n"+
            "}\n",

            "p/OtherClass.groovy",
            "package p;\n"+
            "public class OtherClass {\n" +
            "  { int i = 5; }\n" +
            "}\n",
        },
        "success");
    }

    @Test
    public void testGroovyPropertyAccessors_ErrorCases1() {
        // check no duplicate created for 'String getProp'
        runConformTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    G o = new G();\n"+
            "    System.out.print(o.getProp());\n"+
            "  }\n"+
            "}\n",

            "p/G.groovy",
            "package p;\n"+
            "public class G {\n" +
            "  String prop = 'foo'\n"+
            "  String getProp() { return prop; }\n"+
            "}\n",
        },
        "foo");
    }

    @Test
    public void testGroovyPropertyAccessors_ErrorCases2() {
        // check no duplicate created for 'boolean isProp'
        runConformTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    G o = new G();\n"+
            "    System.out.print(o.isProp());\n"+
            "  }\n"+
            "}\n",

            "p/G.groovy",
            "package p;\n"+
            "public class G {\n" +
            "  boolean prop = false\n"+
            "  boolean isProp() { return prop; }\n"+
            "}\n",
        },
        "false");
    }

    @Test
    public void testGroovyPropertyAccessors_ErrorCases3() {
        // although there is a getProp already defined, it takes a parameter
        // so a new one should still be generated
        runConformTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    G o = new G();\n"+
            "    System.out.print(o.getProp());\n"+
            "  }\n"+
            "}\n",

            "p/G.groovy",
            "package p;\n"+
            "public class G {\n" +
            "  String prop = 'foo'\n"+
            "  String getProp(String s) { return prop; }\n"+
            "}\n",
        },
        "foo");
    }

    @Test
    public void testGroovyPropertyAccessors_ErrorCases4() {
        // although there is a setProp already defined, it takes no parameters
        // so a new one should still be generated
        runConformTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    G o = new G();\n"+
            "    o.setProp(\"abc\");\n"+
            "    System.out.print(\"abc\");\n"+
            "  }\n"+
            "}\n",

            "p/G.groovy",
            "package p;\n"+
            "public class G {\n" +
            "  String prop = 'foo'\n"+
            "  void setProp() { }\n"+
            "}\n",
        },
        "abc");
    }

    @Test
    public void testGroovyPropertyAccessors_ErrorCases5() {
        runNegativeTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    G o = new G();\n"+
            "    o.setProp(new H());\n"+
            "    System.out.print(\"abc\");\n"+
            "  }\n"+
            "}\n",

            "p/H.java",
            "package p;\n"+
            "class H{}\n",

            "p/J.java",
            "package p;\n"+
            "class J{}\n",

            "p/G.groovy",
            "package p;\n"+
            "public class G {\n" +
            "  H prop\n"+
            "  void setProp(J b) { }\n"+
            "}\n",
        },
        "----------\n" +
        "1. ERROR in p\\C.java (at line 5)\n" +
        "\to.setProp(new H());\n" +
        "\t  ^^^^^^^\n" +
        "The method setProp(J) in the type G is not applicable for the arguments (H)\n" +
        "----------\n");
    }

    @Test
    public void testGroovyPropertyAccessors_ErrorCases6() {
        runNegativeTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    G o = new G();\n"+
            "    o.setProp(\"abc\");\n"+
            "    System.out.print(\"abc\");\n"+
            "  }\n"+
            "}\n",

            "p/G.groovy",
            "package p;\n"+
            "public class G {\n" +
            "  String prop = 'foo'\n"+
            "  void setProp(boolean b) { }\n"+
            "}\n",
        },
        "----------\n" +
        "1. ERROR in p\\C.java (at line 5)\n" +
        "\to.setProp(\"abc\");\n" +
        "\t  ^^^^^^^\n" +
        "The method setProp(boolean) in the type G is not applicable for the arguments (String)\n" +
        "----------\n");
    }

    @Test
    public void testGroovyPropertyAccessors() {
        runConformTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    G o = new G();\n"+
            "    System.out.print(o.isB());\n"+
            "    System.out.print(o.getB());\n"+
            "  }\n"+
            "}\n",

            "p/G.groovy",
            "package p;\n"+
            "public class G {\n" +
            "  boolean b\n"+
            "}\n",
        },
        "falsefalse");
    }

    @Test
    public void testGroovyPropertyAccessors_Set() {
        runConformTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    G o = new G();\n"+
            "    System.out.print(o.getB());\n"+
            "    o.setB(true);\n"+
            "    System.out.print(o.getB());\n"+
            "  }\n"+
            "}\n",

            "p/G.groovy",
            "package p;\n"+
            "public class G {\n" +
            "  boolean b\n"+
            "}\n",
        },
        "falsetrue");
    }

    @Test
    public void testDefaultValueMethods() {
        runConformTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    G o = new G();\n"+
            "    o.m(\"abc\",3);\n"+
            "    o.m(\"abc\");\n"+
            "  }\n"+
            "}\n",

            "p/G.groovy",
            "package p;\n"+
            "public class G {\n" +
            "  public void m(String s,Integer i=3) { print s }\n"+
            "}\n",
        },
        "abcabc");

        String expectedOutput =
            "package p;\n" +
            "public class G {\n" +
            "  public G() {\n" +
            "  }\n" +
            "  public void m(String s, Integer i) {\n" +
            "  }\n" +
            "  public void m(String s) {\n" +
            "  }\n" +
            "}\n";
        checkGCUDeclaration("G.groovy",expectedOutput);
        expectedOutput =
            "  \n" +
            "  public void m(String s, Integer i);\n" +
            "  \n";
        checkDisassemblyFor("p/G.class", expectedOutput, ClassFileBytesDisassembler.COMPACT);
        expectedOutput =
            "  \n" +
            "  public void m(String s);\n" +
            "  \n";
        checkDisassemblyFor("p/G.class", expectedOutput, ClassFileBytesDisassembler.COMPACT);
    }

    @Test
    public void testDefaultValueMethods02() {
        runConformTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    G o = new G();\n"+
            "    String str=\"xyz\";\n"+
            "    o.m(str,1,str,str,4.0f,str);\n"+
            "    o.m(str,1,str,str,str);\n"+
            "    o.m(str,1,str,str);\n"+
            "    o.m(str,str,str);\n"+
            "  }\n"+
            "}\n",

            "p/G.groovy",
            "package p;\n"+
            "public class G {\n" +
            "  public void m(String s, Integer i=3, String j=\"abc\", String k, float f = 3.0f, String l) { print s+f }\n"+
            "}\n",
        },
        "xyz4.0xyz3.0xyz3.0xyz3.0");

        String expectedOutput =
            "package p;\n" +
            "public class G {\n" +
            "  public G() {\n" +
            "  }\n" +
            "  public void m(String s, Integer i, String j, String k, float f, String l) {\n" +
            "  }\n" +
            "  public void m(String s, Integer i, String j, String k, String l) {\n" +
            "  }\n" +
            "  public void m(String s, Integer i, String k, String l) {\n" +
            "  }\n" +
            "  public void m(String s, String k, String l) {\n" +
            "  }\n" +
            "}\n";
        checkGCUDeclaration("G.groovy", expectedOutput);

        expectedOutput =
            "  \n" +
            "  public void m(String s, Integer i, String j, String k, float f, String l);\n" +
            "  \n";
        checkDisassemblyFor("p/G.class", expectedOutput, ClassFileBytesDisassembler.COMPACT);
        expectedOutput =
            "  \n" +
            "  public void m(String s, Integer i, String j, String k, String l);\n" +
            "  \n";
        checkDisassemblyFor("p/G.class", expectedOutput, ClassFileBytesDisassembler.COMPACT);
        expectedOutput =
            "  \n" +
            "  public void m(String s, Integer i, String k, String l);\n" +
            "  \n";
        checkDisassemblyFor("p/G.class", expectedOutput, ClassFileBytesDisassembler.COMPACT);
        expectedOutput =
            "  \n" +
            "  public void m(String s, String k, String l);\n" +
            "  \n";
        checkDisassemblyFor("p/G.class", expectedOutput, ClassFileBytesDisassembler.COMPACT);
    }

    @Test
    public void testDefaultValueConstructors() {
        runConformTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    G o = new G(2,\"abc\");\n"+
            "    o.print();\n"+
            "    o = new G(3);\n"+
            "    o.print();\n"+
            "  }\n"+
            "}\n",

            "p/G.groovy",
            "package p;\n"+
            "public class G {\n" +
            "  def msg\n"+
            "  public G(Integer i, String m=\"abc\") {this.msg = m;}\n"+
            "  public void print(int i=3) { print msg }\n"+
            "}\n",
        },
        "abcabc");

        String expectedOutput=
            "package p;\n" +
            "public class G {\n" +
            "  private java.lang.Object msg;\n" +
            "  public G(Integer i, String m) {\n" +
            "  }\n" +
            "  public G(Integer i) {\n" +
            "  }\n" +
            "  public void print(int i) {\n" +
            "  }\n" +
            "  public void print() {\n" +
            "  }\n" +
            "}\n";
        checkGCUDeclaration("G.groovy", expectedOutput);
        expectedOutput =
            "  \n" +
            "  public G(Integer i, String m);\n" +
            "  \n";
        checkDisassemblyFor("p/G.class", expectedOutput, ClassFileBytesDisassembler.COMPACT);
        expectedOutput =
            "  \n" +
            "  public G(Integer i);\n" +
            "  \n";
        checkDisassemblyFor("p/G.class", expectedOutput, ClassFileBytesDisassembler.COMPACT);
    }

    @Test
    public void testDefaultValueConstructors02() {
        runConformTest(new String[] {
            "p/C.java",
            "package p;\n" +
            "public class C {\n"+
            "  public static void main(String[] argv) {\n"+
            "    G o = new G(2,\"abc\");\n"+
            "    o.print();\n"+
            "    o = new G(3);\n"+
            "    o.print();\n"+
            "  }\n"+
            "}\n",

            "p/G.groovy",
            "package p;\n"+
            "public class G {\n" +
            "  def msg\n"+
            "  public G(Integer i) {}\n"+
            "  public G(Integer i, String m=\"abc\") {this.msg = m;}\n"+
            "  public void print(int i=3) { print msg }\n"+
            "}\n",
        },
        "",
        "java.lang.ClassFormatError: Duplicate method name");
    }

    @Test
    public void testClashingMethodsWithDefaultParams() {
        runNegativeTest(new String[] {
            "p/Code.groovy",
            "package p;\n"+
            "\n"+
            "class Code {\n"+
            "  public void m(String s) {}\n"+
            "  public void m(String s, Integer i =3) {}\n"+
            "}\n",
        },
        "----------\n" +
        "1. ERROR in p\\Code.groovy (at line 5)\n" +
        "\tpublic void m(String s, Integer i =3) {}\n" +
        "\t^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Groovy:The method with default parameters \"void m(java.lang.String, java.lang.Integer)\" defines a method \"void m(java.lang.String)\" that is already defined.\n"+
        "----------\n"
        );
    }

    @Test
    public void testCallingJavaFromGroovy1() throws Exception {
        runConformTest(new String[] {
            "p/Code.groovy",
            "package p;\n"+
            "class Code {\n"+
            "  public static void main(String[] argv) {\n"+
            "    new J().run();\n"+
            "    print new J().name;\n"+
            "  }\n"+
            "}\n",

            "p/J.java",
            "package p;\n"+
            "public class J {\n"+
            "  public String name = \"name\";\n"+
            "  public void run() { System.out.print(\"success\"); }\n"+
            "}\n",
        },
        "successname");
        //checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p/Code.class", "Code", "");
    }

    @Test
    public void testCallingJavaFromGroovy2() throws Exception {
        runConformTest(new String[] {
            "p/Code.groovy",
            "package p;\n"+
            "@Wibble(value=4)\n"+
            "class Code {\n"+
            "  public static void main(String[] argv) {\n"+
            "    new J().run();\n"+
            "  }\n"+
            "}\n",

            "p/J.java",
            "package p;\n"+
            "public class J {\n"+
            "  public String name = \"name\";\n"+
            "  public void run() { System.out.print(\"success\"); }\n"+
            "}\n",

            "p/Wibble.java",
            "package p;\n"+
            "public @interface Wibble {\n"+
            "  int value() default 3;\n"+
            "}\n",
        },
        "success");
    }

    @Test
    public void testExtendingRawJavaType() {
        runConformTest(new String[] {
            "p/Foo.groovy",
            "package p;\n"+
            "public class Foo extends Supertype {\n"+
            "  public static void main(String[] argv) {\n"+
            "    System.out.print(\"success\");\n"+
            "  }\n"+
            "}\n",

            "p/Supertype.java",
            "package p;\n"+
            "class Supertype<T> extends Supertype2 { }",

            "p/Supertype2.java",
            "package p;\n"+
            "class Supertype2<T> { }"
        },
        "success");
    }

    @Test
    public void testTypeVariableBoundIsRawType() {
        runConformTest(new String[] {
            "p/Foo.groovy",
            "package p;\n"+
            "public class Foo extends Supertype {\n"+
            "  public static void main(String[] argv) {\n"+
            "    System.out.print(\"success\");\n"+
            "  }\n"+
            "}\n",

            "p/Supertype.java",
            "package p;\n"+
            "class Supertype<T extends Supertype2> { }",

            "p/Supertype2.java",
            "package p;\n"+
            "class Supertype2<T> { }"
        },
        "success");
    }

    @Test
    public void testEnum() {
        runConformTest(new String[] {
            "p/Foo.groovy",
            "package p;\n"+
            "public class Foo /*extends Supertype<Goo>*/ {\n"+
            "  public static void main(String[] argv) {\n"+
            "    print Goo.R\n"+
            "  }\n"+
            "}\n",

            "p/Goo.java",
            "package p;\n"+
            "enum Goo { R,G,B; }",
        },
        "R");
    }

    // Type already implements invokeMethod(String,Object) - should not be an error, just don't add the method
    @Test
    public void testDuplicateGroovyObjectMethods() {
        runConformTest(new String[] {
            "p/Foo.groovy",
            "package p;\n"+
            "public class Foo /*extends Supertype<Goo>*/ {\n"+
            " public Object invokeMethod(String s, Object o) {\n" +
            " return o;}\n"+
            "  public static void main(String[] argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",
        },
        "success");
    }

    @Test
    public void testDuplicateGroovyObjectMethods2() {
        runConformTest(new String[] {
            "p/Foo.groovy",
            "package p;\n"+
            "public class Foo /*extends Supertype<Goo>*/ {\n"+
            "  public MetaClass getMetaClass() {return null;}\n"+
            "  public void setMetaClass(MetaClass mc) {}\n"+
            "  public Object getProperty(String propertyName) {return null;}\n"+
            "  public void setProperty(String propertyName,Object newValue) {}\n"+
            "  public static void main(String[] argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",
        },
        "success");
    }

    @Test
    public void testTwoTopLevelTypesInAFile() {
        runConformTest(new String[] {
            "p/First.groovy",
            "package p;\n"+
            "public class First {\n"+
            "  public static void main(String[] argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n"+
            "class Second {\n"+
            "}\n",
        },
        "success");
    }

    @Test
    public void testImports1() {
        runConformTest(new String[] {
            "p/First.groovy",
            "package p;\n"+
            "import java.util.regex.Pattern\n"+
            "public class First {\n"+
            "  public static void main(String[] argv) {\n"+
            "    Pattern p = Pattern.compile(\".\")\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "  public Pattern getPattern() { return null;}\n"+
            "}\n",
        },
        "success");
    }

    @Test
    public void testImports2() {
        runConformTest(new String[] {
            "p/First.groovy",
            "package p;\n"+
            "import java.util.regex.Pattern\n"+
            "public class First {\n"+
            "  public static void main(String[] argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "  public File getFile() { return null;}\n"+ // java.io.File should be picked up magically
            "}\n",
        },
        "success");
    }

    @Test
    public void testImportsBigDecimal1() {
        runConformTest(new String[] {
            "p/Main.java",
            "package p;\n" +
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.print(new Big().getAmount().toString());\n" + // https://github.com/groovy/groovy-eclipse/issues/268
            "  }\n" +
            "}\n",

            "p/Big.groovy",
            "package p\n" +
            "class Big {\n" +
            "  BigDecimal amount = 3.14\n" +
            "}\n",
        },
        "3.14");
    }

    @Test
    public void testImportsBigDecimal2() {
        runConformTest(new String[] {
            "p/Big.groovy",
            "package p\n" +
            "class Big {\n" +
            "  static void main(String[] args) {\n" +
            "    print 'success'\n" +
            "  }\n" +
            "  BigDecimal getAmount() { return 0 }\n" +
            "}\n",
        },
        "success");
    }

    @Test
    public void testImportsBigDecimal3() {
        // this version has an import; that can make a difference...
        runConformTest(new String[] {
            "p/Big.groovy",
            "package p\n" +
            "import java.util.regex.Pattern\n" +
            "class Big {\n" +
            "  static void main(String[] args) {\n" +
            "    print 'success'\n" +
            "  }\n" +
            "  BigDecimal getAmount() { return 0 }\n" +
            "}\n",
        },
        "success");
    }

    @Test
    public void testImportsBigDecimal4() {
        runConformTest(new String[] {
            "p/Big.groovy",
            "package p\n" +
            "class Big {\n" +
            "  static void main(String[] args) {\n" +
            "    print 'success'\n" +
            "  }\n" +
            "  private static final BigDecimal FIXED_AMOUNT = BigDecimal.TEN\n" +
            "}\n",
        },
        "success");
    }

    @Test
    public void testImportsBigInteger1() {
        runConformTest(new String[] {
            "p/Main.java",
            "package p;\n" +
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.print(new Big().getAmount().toString());\n" +
            "  }\n" +
            "}\n",

            "p/Big.groovy",
            "package p\n" +
            "class Big {\n" +
            "  BigInteger amount = 10\n" +
            "}\n",
        },
        "10");
    }

    @Test
    public void testImportsBigInteger2() {
        runConformTest(new String[] {
            "p/Big.groovy",
            "package p\n" +
            "class Big {\n" +
            "  static void main(String[] args) {\n" +
            "    print 'success'\n" +
            "  }\n" +
            "  BigInteger getAmount() { return 0 }\n" +
            "}\n",
        },
        "success");
    }

    @Test
    public void testImportsBigInteger3() {
        runConformTest(new String[] {
            "p/Big.groovy",
            "package p\n" +
            "class Big {\n" +
            "  static void main(String[] args) {\n" +
            "    print 'success'\n" +
            "  }\n" +
            "  private static final BigInteger FIXED_AMOUNT = BigInteger.TEN\n" +
            "}\n",
        },
        "success");
    }

    @Test
    public void testMultipleTypesInOneFile01() {
        runConformTest(new String[] {
            "p/Foo.groovy",
            "package p;\n"+
            "class Foo {\n"+
            "  public static void main(String[] argv) {\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n"+
            "class Goo {}\n",
        },
        "success");
    }

    // Refering to the secondary type from the primary (but internally to a method)
    @Test
    public void testMultipleTypesInOneFile02() {
        runConformTest(new String[] {
            "p/Foo.groovy",
            "package p;\n"+
            "class Foo {\n"+
            "  public static void main(String[] argv) {\n"+
            "    new Goo();\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n"+
            "class Goo {}\n",
        },
        "success");
    }

    // Refering to the secondary type from the primary - from a method param
    @Test
    public void testMultipleTypesInOneFile03() {
        runConformTest(new String[] {
            "p/Foo.groovy",
            "package p;\n"+
            "class Foo {\n"+
            "  public static void main(String[] argv) {\n"+
            "    new Foo().runnit(new Goo());\n"+
            "  }\n"+
            "  public void runnit(Goo g) {"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n"+
            "class Goo {}\n",
        },
        "success");
    }

    // Refering to the secondary type from the primary - from a method
    @Test
    public void testJDKClasses() {
        runConformTest(new String[] {
            "p/Foo.groovy",
            "package p;\n"+
            "class Foo {\n"+
            "  public static void main(String[] argv) {\n"+
            "    new Foo().runnit(new Goo());\n"+
            "  }\n"+
            "  public void runnit(Goo g) {"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n"+
            "class Goo {}\n",
        },
        "success");
    }

    // Test the visibility of a package-private source type from another package
    @Test
    public void testVisibility() {
        runConformTest(new String[] {
            "p/First.groovy",
            "package p;\n" +
            "import q.Second;\n" +
            "public class First {\n" +
            "  public static void main(String[] argv) {\n" +
            "    new First().getIt();\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "  public Second getIt() { return null;}\n" +
            "}\n",

            "q/Second.java",
            "package q;\n" +
            "class Second {}\n",
        },
        "success");
    }

    @Test
    public void testClosuresBasic() {
        runConformTest(new String[] {
            "Coroutine.groovy",
            "def iterate(n, closure) {\n"+
            "  1.upto(n) {\n" +
            "    closure(it);\n" +
            "  }\n"+
            "}\n"+
            "iterate (3) {\n"+
            "  print it\n"+
            "}\n"
        },
        "123");
    }

    @Test
    public void testScript() {
        runConformTest(new String[] {
            "Foo.groovy",
            "print 'Coolio'\n",
        },
        "Coolio");
    }

    @Test
    public void testScriptCallJava() {
        runConformTest(new String[] {
            "Foo.groovy",
            "print SomeJava.constant\n",
            "SomeJava.java",
            "class SomeJava { static String constant = \"abc\";}",
        },
        "abc");
    }

    @Test
    public void testScriptWithError() {
        runNegativeTest(new String[] {
            "Foo.groovy",
            "print Coolio!",
        },
        "----------\n" +
        "1. ERROR in Foo.groovy (at line 1)\n" +
        "\tprint Coolio!\n" +
        "\t            ^\n" +
        "Groovy:expecting EOF, found \'!\' @ line 1, column 13.\n" +
        "----------\n"
        );
    }

    @Test
    public void testConfigScriptWithError() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  X\n" +
            "}\n"
        ).getAbsolutePath());

        runNegativeTest(new String[] {
            "hello.groovy",
            "println 'hello'",
        },
        "----------\n" +
        "1. WARNING in hello.groovy (at line 1)\n" +
        "\tprintln 'hello'\n" +
        "\t^\n" +
        "Cannot read the source from ##" + File.separator + "config.groovy due to internal exception groovy.lang.MissingPropertyException: No such property: X for class: config\n" +
        "----------\n",
        options);
    }

    @Test
    public void testExtraImports1() {
        Map<String, String> options = getCompilerOptions();
        // use the pre-2.1 verbose syntax for one test case to ensure it works as a fallback
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "import org.codehaus.groovy.control.customizers.ImportCustomizer\n" +
            "def ic = new ImportCustomizer()\n" +
            "ic.addStarImports 'com.foo'\n" +
            "configuration.addCompilationCustomizers(ic)\n"
        ).getAbsolutePath());

        runConformTest(new String[] {
            "com/bar/Runner.groovy",
            "package com.bar\n" +
            "class Runner {\n" +
            "  public static void main(args) {\n" +
            "    Type.m()\n" + // requires extra import
            "    print 'done'\n" +
            "  }\n" +
            "}\n",

            "com/foo/Type.groovy",
            "package com.foo\n" +
            "class Type {\n" +
            "  public static void m() {}\n" +
            "}\n",
        },
        "done", options);
    }

    @Test
    public void testExtraImports2() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  imports {\n" +
            "    star 'com.foo'\n" +
            "  }\n" +
            "}\n"
        ).getAbsolutePath());

        runConformTest(new String[] {
            "com/bar/Runner.groovy",
            "package com.bar\n" +
            "class Runner {\n" +
            "  public static void main(args) {\n" +
            "    Type.m()\n" + // requires extra import
            "    print 'done'\n" +
            "  }\n" +
            "}\n",

            "com/foo/Type.groovy",
            "package com.foo\n" +
            "class Type {\n" +
            "  public static void m() {}\n" +
            "}\n",
        },
        "done", options);
    }

    @Test
    public void testExtraImports3() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  imports {\n" +
            "    normal 'com.foo.Type'\n" +
            "  }\n" +
            "}\n"
        ).getAbsolutePath());

        runConformTest(new String[] {
            "com/bar/Runner.groovy",
            "package com.bar\n" +
            "class Runner {\n" +
            "  public static void main(args) {\n" +
            "    Type.m()\n" + // requires extra import
            "    print 'done'\n" +
            "  }\n" +
            "}\n",

            "com/foo/Type.groovy",
            "package com.foo\n" +
            "class Type {\n" +
            "  public static void m() {}\n" +
            "}\n",
        },
        "done", options);
    }

    @Test
    public void testExtraImports4() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  imports {\n" +
            "    normal 'com.foo.Type'\n" +
            "    normal 'com.foo.Type2'\n" +
            "  }\n" +
            "}\n"
        ).getAbsolutePath());

        runConformTest(new String[] {
            "com/bar/Runner.groovy",
            "package com.bar\n" +
            "class Runner {\n" +
            "  public static void main(args) {\n" +
            "    Type.m()\n" + // requires extra import
            "    Type2.m()\n" + // requires extra import
            "    print 'done'\n" +
            "  }\n" +
            "}\n",

            "com/foo/Type.groovy",
            "package com.foo\n" +
            "class Type {\n" +
                "  public static void m() {}\n" +
            "}\n",

            "com/foo/Type2.groovy",
            "package com.foo\n" +
            "class Type2 {\n" +
            "  public static void m() {}\n" +
            "}\n",
        },
        "done", options);
    }

    @Test
    public void testExtraImports_extensionFilter1() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  source(extension: 'groovy') {\n" +
            "    imports {\n" +
            "      star 'com.foo'\n" +
            "    }\n" +
            "  }\n" +
            "}\n"
        ).getAbsolutePath());

        runConformTest(new String[] {
            "com/bar/Runner.groovy",
            "package com.bar\n" +
            "class Runner {\n" +
            "  public static void main(args) {\n" +
            "    Type.m()\n" + // requires extra import
            "    print 'done'\n" +
            "  }\n" +
            "}\n",

            "com/foo/Type.groovy",
            "package com.foo\n" + "class Type {\n" +
            "  public static void m() {}\n" +
            "}\n",
        },
        "done", options);
    }

    @Test
    public void testExtraImports_extensionFilter2() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  source(extension: 'groovy') {\n" +
            "    imports {\n" +
            "      normal 'com.foo.Type'\n" +
            "    }\n" +
            "  }\n" +
            "}\n"
        ).getAbsolutePath());

        runConformTest(new String[] {
            "com/bar/Runner.groovy",
            "package com.bar\n" +
            "class Runner {\n" +
            "  public static void main(args) {\n" +
            "    Type.m()\n" + // requires extra import
            "       print 'done'\n" +
            "  }\n" +
            "}\n",

            "com/foo/Type.groovy",
            "package com.foo\n" + "class Type {\n" +
            "  public static void m() {}\n" +
            "}\n",
        },
        "done", options);
    }

    @Test
    public void testExtraImports_extensionFilter3() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  source(extension: 'groovy') {\n" +
            "    imports {\n" +
            "      normal 'com.foo.Type'\n" +
            "      normal 'com.foo.TypeB'\n" +
            "    }\n" +
            "  }\n" +
            "}\n"
        ).getAbsolutePath());

        runConformTest(new String[] {
            "com/bar/Runner.groovy",
            "package com.bar\n" +
            "class Runner {\n" +
            "  public static void main(args) {\n" +
            "    Type.m()\n" + // requires extra import
            "    TypeB.m()\n" + // requires extra import
            "    print 'done'\n" +
            "  }\n" +
            "}\n",

            "com/foo/Type.groovy",
            "package com.foo\n" + "class Type {\n" +
            "  public static void m() {}\n" +
            "}\n",

            "com/foo/TypeB.groovy",
            "package com.foo\n" +
            "class TypeB {\n" +
            "  public static void m() {}\n" +
            "}\n",
        },
        "done", options);
    }

    @Test
    public void testExtraImports_nonMatchingSuffix() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  source(extension: 'gradle') {\n" +
            "    imports {\n" +
            "      normal 'com.foo.Type'\n" +
            "    }\n" +
            "  }\n" +
            "}\n"
        ).getAbsolutePath());

        runNegativeTest(new String[] {
            "com/bar/Runner.groovy",
            "package com.bar\n" +
            "class Runner {\n" +
            "  public static void main(args) {\n" +
            "    Type.m()\n" + // requires extra import
            "    print 'done'\n" +
            "  }\n" +
            "}\n",

            "com/foo/Type.groovy",
            "package com.foo\n" + "class Type {\n" +
            "  public static void m() {}\n" +
            "}\n",
        },
        "----------\n" +
        "1. ERROR in com\\bar\\Runner.groovy (at line 4)\n" +
        "\tType.m()\n" +
        "\t^^^^\n" +
        "Groovy:Apparent variable 'Type' was found in a static scope but doesn't refer to a local variable, static field or class. Possible causes:\n" +
        "----------\n",
        options);
    }

    @Test
    public void testExtraImports_typeDoesNotExist() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  imports {\n" +
            "    normal 'com.foo.Type2'\n" +
            "  }\n" +
            "}\n"
        ).getAbsolutePath());

        runNegativeTest(new String[] {
            "com/bar/Runner.groovy",
            "package com.bar\n" +
            "class Runner {\n" +
            "  public static void main(args) {\n" +
            "    Type.m()\n" + // requires extra import
            "    print 'done'\n" +
            "  }\n" +
            "}\n",

            "com/foo/Type.groovy",
            "package com.foo\n" +
            "class Type {\n" +
            "  public static void m() {}\n" +
            "}\n",
        },
        "----------\n" +
        "1. ERROR in com\\bar\\Runner.groovy (at line 1)\n" +
        "\tpackage com.bar\n" +
        "\t^\n" +
        "Groovy:unable to resolve class com.foo.Type2\n" +
        "----------\n" +
        "2. ERROR in com\\bar\\Runner.groovy (at line 4)\n" +
        "\tType.m()\n" +
        "\t^^^^\n" +
        "Groovy:Apparent variable \'Type\' was found in a static scope but doesn\'t refer to a local variable, static field or class. Possible causes:\n" +
        "----------\n" +
        "----------\n" +
        "1. ERROR in com\\foo\\Type.groovy (at line 1)\n" +
        "\tpackage com.foo\n" +
        "\t^\n" +
        "Groovy:unable to resolve class com.foo.Type2\n" +
        "----------\n",
        options);
    }

    @Test
    public void testExtraImports_packageDoesNotExist() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  imports {\n" +
            "    star 'com.whatever'\n" +
            "  }\n" +
            "}\n"
        ).getAbsolutePath());

        runNegativeTest(new String[] {
            "com/bar/Runner.groovy",
            "package com.bar\n" +
            "class Runner {\n" +
            "  public static void main(args) {\n" +
            "    Type.m()\n" + // requires extra import
            "    print 'done'\n" +
            "  }\n" +
            "}\n",

            "com/foo/Type.groovy",
            "package com.foo\n" +
            "class Type {\n" +
            "  public static void m() {}\n" +
            "}\n",
        },
        "----------\n" +
        "1. ERROR in com\\bar\\Runner.groovy (at line 4)\n" +
        "\tType.m()\n" +
        "\t^^^^\n" +
        "Groovy:Apparent variable \'Type\' was found in a static scope but doesn\'t refer to a local variable, static field or class. Possible causes:\n" +
        "----------\n",
        options);
    }

    @Test
    public void testExtraImports_mixedAdditions() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  imports {\n" +
            "    star 'com.whatever'\n" +
            "  }\n" +
            "  source(extension: 'groovy') {\n" +
            "    imports {\n" +
            "      normal 'com.foo.Type'\n" +
            "    }\n" +
            "  }\n" +
            "}\n"
        ).getAbsolutePath());

        runConformTest(new String[] {
            "com/bar/Runner.groovy",
            "package com.bar\n" +
            "class Runner {\n" +
            "  public static void main(args) {\n" +
            "    Type.m()\n" + // requires extra import
            "    print 'done'\n" +
            "  }\n" +
            "}\n",

            "com/foo/Type.groovy",
            "package com.foo\n" +
            "class Type {\n" +
                "  public static void m() {}\n" +
            "}\n",
        },
        "done", options);
    }

    @Test // Variable arguments
    public void testInvokingVarargs01_JtoG() {
        runConformTest(new String[] {
            "p/Run.java",
            "package p;\n"+
            "public class Run {\n" +
            "  public static void main(String[] argv) {\n"+
            "    X x = new X();\n"+
            "    x.callit();\n"+
            "    x.callit(1);\n"+
            "    x.callit(1,2);\n"+
            "    x.callit2();\n"+
            "    x.callit2(1);\n"+
            "    x.callit2(1,2);\n"+
            "    x.callit3();\n"+
            "    x.callit3(\"abc\");\n"+
            "    x.callit3(\"abc\",\"abc\");\n"+
            "  }\n"+
            "}\n",

            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  public void callit(int... is) { print is.length; }\n"+
            "  public void callit2(Integer... is) { print is.length; }\n"+
            "  public void callit3(String... ss) { print ss.length; }\n"+
            "}\n",
        },"012012012");
    }

    @Test
    public void testInvokingVarargs01_GtoJ() {
        runConformTest(new String[] {
            "p/Run.groovy",
            "package p;\n"+
            "public class Run {\n" +
            "  public static void main(String[] argv) {\n"+
            "    X x = new X();\n"+
            "    x.callit('abc');\n"+
            "    x.callit('abc',1);\n"+
            "    x.callit('abc',1,2);\n"+
            "    x.callit2(3);\n"+
            "    x.callit2(4,1);\n"+
            "    x.callit2(1,1,2);\n"+
            "    x.callit3('abc');\n"+
            "    x.callit3('abc',\"abc\");\n"+
            "    x.callit3('abc',\"abc\",\"abc\");\n"+
            "  }\n"+
            "}\n",

            "p/X.java",
            "package p;\n" +
            "public class X {\n" +
            "  public void callit(String a, int... is) { System.out.print(is.length); }\n"+
            "  public void callit2(int a, Integer... is) { System.out.print(is.length); }\n"+
            "  public void callit3(String s, String... ss) { System.out.print(ss.length); }\n"+
            "}\n",
        },"012012012");
    }

    @Test // In these two cases the methods also take other values
    public void testInvokingVarargs02_JtoG() {
        runConformTest(new String[] {
            "p/Run.java",
            "package p;\n"+
            "public class Run {\n" +
            "  public static void main(String[] argv) {\n"+
            "    X x = new X();\n"+
            "    x.callit(\"abc\");\n"+
            "    x.callit(\"abc\",1);\n"+
            "    x.callit(\"abc\",1,2);\n"+
            "    x.callit2(3);\n"+
            "    x.callit2(4,1);\n"+
            "    x.callit2(1,1,2);\n"+
            "    x.callit3(\"abc\");\n"+
            "    x.callit3(\"abc\",\"abc\");\n"+
            "    x.callit3(\"abc\",\"abc\",\"abc\");\n"+
            "  }\n"+
            "}\n",

            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  public void callit(String a, int... is) { print is.length; }\n"+
            "  public void callit2(int a, Integer... is) { print is.length; }\n"+
            "  public void callit3(String s, String... ss) { print ss.length; }\n"+
            "}\n",
        },"012012012");
    }

    @Test // Groovy doesn't care about '...' and will consider [] as varargs
    public void testInvokingVarargs03_JtoG() {
        runConformTest(new String[] {
            "p/Run.java",
            "package p;\n"+
            "public class Run {\n" +
            "  public static void main(String[] argv) {\n"+
            "    X x = new X();\n"+
            "    x.callit(\"abc\");\n"+
            "    x.callit(\"abc\",1);\n"+
            "    x.callit(\"abc\",1,2);\n"+
            "    x.callit2(3);\n"+
            "    x.callit2(4,1);\n"+
            "    x.callit2(1,1,2);\n"+
            "    x.callit3(\"abc\");\n"+
            "    x.callit3(\"abc\",\"abc\");\n"+
            "    x.callit3(\"abc\",\"abc\",\"abc\");\n"+
            "  }\n"+
            "}\n",

            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  public void callit(String a, int[] is) { print is.length; }\n"+
            "  public void callit2(int a, Integer[] is) { print is.length; }\n"+
            "  public void callit3(String s, String[] ss) { print ss.length; }\n"+
            "}\n",
        },"012012012");
    }

    @Test
    public void testInners_1185() {
        runConformTest(new String[] {
            "WithInnerClass.groovy",
            "class WithInnerClass {\n"+
            "\n"+
            "  interface InnerInterface {\n"+
            "	 void foo()\n"+
            "  }\n"+
            "\n"+
            "  private final InnerInterface foo = new InnerInterface() {\n"+
            "	  void foo() {\n" +
            "\n" +
            "	  }\n" +
            "  }\n"+
            "}"
        });
    }

    @Test
    public void testInvokingVarargs02_GtoJ() {
        runConformTest(new String[] {
            "p/Run.groovy",
            "package p;\n"+
            "public class Run {\n" +
            "  public static void main(String[] argv) {\n"+
            "    X x = new X();\n"+
            "    x.callit();\n"+
            "    x.callit(1);\n"+
            "    x.callit(1,2);\n"+
            "    x.callit2();\n"+
            "    x.callit2(1);\n"+
            "    x.callit2(1,2);\n"+
            "    x.callit3();\n"+
            "    x.callit3(\"abc\");\n"+
            "    x.callit3(\"abc\",\"abc\");\n"+
            "  }\n"+
            "}\n",

            "p/X.java",
            "package p;\n" +
            "public class X {\n" +
            "  public void callit(int... is) { System.out.print(is.length); }\n"+
            "  public void callit2(Integer... is) { System.out.print(is.length); }\n"+
            "  public void callit3(String... ss) { System.out.print(ss.length); }\n"+
            "}\n",
        },"012012012");
    }

    @Test
    public void testInvokingVarargsCtors01_JtoG() {
        runConformTest(new String[] {
            "p/Run.java",
            "package p;\n"+
            "public class Run {\n" +
            "  public static void main(String[] argv) {\n"+
            "    X x = null;\n"+
            "    x = new X();\n"+
            "    x = new X(1);\n"+
            "    x = new X(1,2);\n"+
            "    x = new X(\"abc\");\n"+
            "    x = new X(\"abc\",1);\n"+
            "    x = new X(\"abc\",1,2);\n"+
            "  }\n"+
            "}\n",

            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  public X(int... is) { print is.length; }\n"+
            "  public X(String s, int... is) { print is.length; }\n"+
            "}\n",
        },"012012");
    }

    @Test
    public void testInvokingVarargsCtors01_GtoJ() {
        runConformTest(new String[] {
            "p/Run.groovy",
            "package p;\n"+
            "public class Run {\n" +
            "  public static void main(String[] argv) {\n"+
            "    X x = null;\n"+
            "    x = new X();\n"+
            "    x = new X(1);\n"+
            "    x = new X(1,2);\n"+
            "    x = new X(\"abc\");\n"+
            "    x = new X(\"abc\",1);\n"+
            "    x = new X(\"abc\",1,2);\n"+
            "  }\n"+
            "}\n",

            "p/X.java",
            "package p;\n" +
            "public class X {\n" +
            "  public X(int... is) { System.out.print(is.length); }\n"+
            "  public X(String s, int... is) { System.out.print(is.length); }\n"+
            "}\n",
        },"012012");
    }

    @Test
    public void testPositions() {
        runNegativeTest(new String[] {
            "One.groovy",
            "class One {\n" +
            "		/*a*/			Stack plates;\n"+
            "  /*b*/ Stack plates2;\n"+
            "}\n",
        },"----------\n" +
        "1. WARNING in One.groovy (at line 2)\n" +
        "\t/*a*/			Stack plates;\n" +
        "\t     			^^^^^\n" +
        "Stack is a raw type. References to generic type Stack<E> should be parameterized\n" +
        "----------\n" +
        "2. WARNING in One.groovy (at line 3)\n" +
        "\t/*b*/ Stack plates2;\n" +
        "\t      ^^^^^\n" +
        "Stack is a raw type. References to generic type Stack<E> should be parameterized\n" +
        "----------\n");

    }

    @Test
    public void testPositions_2() {
        runNegativeTest(new String[] {
            "One.groovy",
            "class One {\n" +
            "		/*a*/			Stack plates;\n"+
            "  /*b*/ Stack plates2;\n"+
            "}\n",
        },"----------\n" +
        "1. WARNING in One.groovy (at line 2)\n" +
        "\t/*a*/			Stack plates;\n" +
        "\t     			^^^^^\n" +
        "Stack is a raw type. References to generic type Stack<E> should be parameterized\n" +
        "----------\n" +
        "2. WARNING in One.groovy (at line 3)\n" +
        "\t/*b*/ Stack plates2;\n" +
        "\t      ^^^^^\n" +
        "Stack is a raw type. References to generic type Stack<E> should be parameterized\n" +
        "----------\n");
    }

    @Test
    public void testJDTClassNode_633() {
        try {
            JDTResolver.recordInstances = true;
            runConformTest(new String[] {
                "p/Run.groovy",
                "package p;\n"+
                "import static p.q.r.Colour.*;\n"+
                "import p.q.r.Colour2;\n"+
                "public class Run {\n" +
                "  public static void main(String[] argv) {\n"+
                "    System.out.print(Red);\n"+
                "    System.out.print(Green);\n"+
                "    System.out.print(Blue);\n"+
                "   Colour2 c2 = new Colour2();\n"+
                "   int i = c2.compareTo('abc');\n"+
                "  }\n"+
                "}\n",

                "p/q/r/Colour.java",
                "package p.q.r;\n" +
                "enum Colour { Red,Green,Blue; }\n",

                "p/q/r/Colour3.java",
                "package p.q.r;\n"+
                "@SuppressWarnings(\"rawtypes\")\n"+
                "class Colour3 implements Comparable { public int compareTo(Object o) { return 0;}}\n",

                "p/q/r/Colour2.java",
                "package p.q.r;\n" +
                "public class Colour2 implements Comparable<String> { \n"+
                "  public int compareTo(String s) { return 0; } \n"+
                    "}\n",
                },"RedGreenBlue");

            // Check on the state of Comparable
            JDTClassNode classnode = JDTResolver.getCachedNode("java.lang.Comparable<E>");
            assertNotNull(classnode);
            // Should have one method
            List<MethodNode> methods = classnode.getMethods();
            assertEquals(1,methods.size());
            assertEquals("int compareTo(java.lang.Object)", methods.get(0).getTypeDescriptor());
        } finally {
            JDTResolver.instances.clear();
            JDTResolver.recordInstances=false;
        }
    }

    @Test
    public void testSecondaryTypeTagging() {
        runConformTest(new String[] {
            "Run.groovy",
            "class Run { public static void main(String[]argv) {print '1.0';} }\n"+
            "class B {}\n"+
            "class C {}\n"+
            "class D {}\n"
        },"1.0");
        GroovyCompilationUnitDeclaration gcud = getCUDeclFor("Run.groovy");
        TypeDeclaration[] tds = gcud.types;
        assertFalse((tds[0].bits&ASTNode.IsSecondaryType)!=0);
        assertTrue((tds[1].bits&ASTNode.IsSecondaryType)!=0);
        assertTrue((tds[2].bits&ASTNode.IsSecondaryType)!=0);
        assertTrue((tds[3].bits&ASTNode.IsSecondaryType)!=0);

        runConformTest(new String[] {
                "Run2.groovy",
                "class B {}\n"+
                "class Run2 { public static void main(String[]argv) {print '1.0';} }\n"+
                "class C {}\n"+
                "class D {}\n"
            },"1.0");
            gcud = getCUDeclFor("Run2.groovy");
            tds = gcud.types;
            assertTrue((tds[0].bits&ASTNode.IsSecondaryType)!=0);
            assertFalse((tds[1].bits&ASTNode.IsSecondaryType)!=0);
            assertTrue((tds[2].bits&ASTNode.IsSecondaryType)!=0);
            assertTrue((tds[3].bits&ASTNode.IsSecondaryType)!=0);
    }

    @Test // static imports
    public void testStaticImports_JtoG() {
        runConformTest(new String[] {
            "p/Run.java",
            "package p;\n"+
            "import static p.q.r.Colour.*;\n"+
            "public class Run {\n" +
            "  public static void main(String[] argv) {\n"+
            "    System.out.print(Red);\n"+
            "    System.out.print(Green);\n"+
            "    System.out.print(Blue);\n"+
            "  }\n"+
            "}\n",

            "p/q/r/Colour.groovy",
            "package p.q.r;\n" +
            "enum Colour { Red,Green,Blue; }\n",
        },"RedGreenBlue");
    }

    @Test
    public void testStaticImports_GtoJ() {
        runConformTest(new String[] {
            "p/Run.groovy",
            "package p;\n"+
            "import static p.q.r.Colour.*;\n"+
            "public class Run {\n" +
            "  public static void main(String[] argv) {\n"+
            "    System.out.print(Red);\n"+
            "    System.out.print(Green);\n"+
            "    System.out.print(Blue);\n"+
            "  }\n"+
            "}\n",

            "p/q/r/Colour.java",
            "package p.q.r;\n" +
            "enum Colour { Red,Green,Blue; }\n",
        },"RedGreenBlue");
    }

    @Test
    public void testStaticImports2_GtoJ() {
        runConformTest(new String[] {
            "p/Run.java",
            "package p;\n"+
            "import static p.q.r.Colour.*;\n"+
            "public class Run {\n" +
            "  public static void main(String[] argv) {\n"+
            "    Red.printme();\n"+
            "  }\n"+
            "}\n",

            "p/q/r/Colour.groovy",
            "package p.q.r;\n" +
            "enum Colour { Red,Green,Blue; \n" +
            "  void printme() {\n"+
            "    println \"${name()}\";\n" +
             "  }\n"+
             "}\n",
        },"Red");
    }

    @Test
    public void testStaticImportsAliasing_G() {
        runConformTest(new String[] {
            "p/Run.groovy",
            "package p\n" +
            "import static java.lang.Math.PI as pi\n" +
            "import static java.lang.Math.sin as sine\n" +
            "import static java.lang.Math.cos as cosine\n" +
            "\n"+
            "print sine(pi / 6) + cosine(pi / 3)\n",
        }, "1.0");
    }

    @Test // 'import static a.B.FOO'
    public void testImportStatic1() {
        runConformTest(new String[] {
            "b/Run.groovy",
            "package b\n" +
            "import static a.B.FOO\n" +
            "class Run { public static void main(String[]argv) { print FOO;} }\n",

            "a/B.groovy",
            "package a\n" +
            "class B { public static String FOO='abc';}\n",
        }, "abc");

        ImportReference ref = getCUDeclFor("Run.groovy").imports[0];
        assertTrue(ref.isStatic());
        assertEquals("a.B.FOO", ref.toString());
        assertFalse(ref instanceof AliasImportReference);
        assertEquals("FOO", String.valueOf(ref.getSimpleName()));
    }

    @Test // 'import static a.B.*'
    public void testImportStatic2() {
        runConformTest(new String[] {
            "b/Run.groovy",
            "package b\n" +
            "import static a.B.*\n" +
            "class Run { public static void main(String[]argv) { print FOO;} }\n",

            "a/B.groovy",
            "package a\n" +
            "class B { public static String FOO='abc';}\n",
        }, "abc");

        ImportReference ref = getCUDeclFor("Run.groovy").imports[0];
        assertTrue(ref.isStatic());
        assertEquals("a.B.*", ref.toString());
    }

    @Test // 'import static a.B.FOO as Wibble'
    public void testImportStatic3() {
        runConformTest(new String[] {
            "b/Run.groovy",
            "package b\n" +
            "import static a.B.FOO as Wibble\n" +
            "class Run { public static void main(String[]argv) { print Wibble;} }\n",

            "a/B.groovy",
            "package a\n" +
            "class B { public static String FOO='abc';}\n",
        }, "abc");

        ImportReference ref = getCUDeclFor("Run.groovy").imports[0];
        assertTrue(ref.isStatic());
        assertTrue(ref instanceof AliasImportReference);
        assertEquals("a.B.FOO as Wibble", ref.toString());
        assertEquals("Wibble", String.valueOf(ref.getSimpleName()));
    }

    @Test
    public void testImportStatic4() {
        runConformTest(new String[] {
            "a/B.groovy",
            "package a\n" +
            "interface B {\n" +
            "  String C = 'nls'\n" +
            "}",

            "x/Y.groovy",
            "package x\n" +
            "import static a.B.C\n" +
            "class Y {\n" +
            "  @SuppressWarnings(C) def one() {}\n" +
            "  @SuppressWarnings(C) def two() {}\n" +
            "}",
        });
    }

    @Test
    public void testParsingBlankPackage() {
        runNegativeTest(new String[] {
            "Foo.groovy",
            "package \n"+
            "class Name { }\n",
        },
        "----------\n" +
        "1. ERROR in Foo.groovy (at line 1)\n" +
        "\tpackage \n" +
        "\t^\n" +
        "Groovy:Invalid package specification @ line 1, column 0.\n" +
        "----------\n");
    }

    @Test
    public void testParsingBlankPackage2() {
        runNegativeTest(new String[] {
            "Foo.groovy",
            "package ;\n"+
            "class Name { }\n",
        },
        "----------\n" +
        "1. ERROR in Foo.groovy (at line 1)\n" +
        "\tpackage ;\n" +
        "\t^\n" +
        "Groovy:Invalid package specification @ line 1, column 0.\n" +
        "----------\n");
    }

    @Test // does the second error now get reported after the package problem
    public void testParsingBlankPackage3() {
        runNegativeTest(new String[] {
            "Foo.groovy",
            "package ;\n"+
            "class Name { \n" +
            "  asdf\n"+
            "}\n",
        },
        "----------\n" +
        "1. ERROR in Foo.groovy (at line 1)\n" +
        "\tpackage ;\n" +
        "\t^\n" +
        "Groovy:Invalid package specification @ line 1, column 0.\n" +
        "----------\n" +
        "2. ERROR in Foo.groovy (at line 3)\n" +
        "\tasdf\n" +
        "\t^\n" +
        "Groovy:unexpected token: asdf @ line 3, column 3.\n" +
        "----------\n");
    }

    @Test
    public void testParsingBlankImport_538() {
        runNegativeTest(new String[] {
            "A.groovy",
            "import "
        },
        "----------\n" +
        "1. ERROR in A.groovy (at line 1)\n" +
        "\timport \n" +
        "\t^^^^^^^\n" +
        "Groovy:unable to resolve class ?\n" +
        "----------\n");

        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());

        List<ImportNode> imports = mn.getImports();
        ImportNode recoveredImport = imports.get(0);
        assertEquals(0, recoveredImport.getStart());
        assertEquals(7, recoveredImport.getEnd());
        assertEquals("?", recoveredImport.getType().getName());

        ClassNode cn = mn.getClasses().get(0);
        assertNotNull(cn);
        assertTrue(cn.getName().equals("A"));
    }

    @Test
    public void testParsingBlankImport2_538() {
        runNegativeTest(new String[] {
            "A.groovy",
            "import ;"
        },
        "----------\n" +
        "1. ERROR in A.groovy (at line 1)\n" +
        "\timport ;\n" +
        "\t^^^^^^^\n" +
        "Groovy:unable to resolve class ?\n" +
        "----------\n");

        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());

        List<ImportNode> imports = mn.getImports();
        ImportNode recoveredImport = imports.get(0);
        assertEquals(0, recoveredImport.getStart());
        assertEquals(7, recoveredImport.getEnd());
        assertEquals("?", recoveredImport.getType().getName());

        ClassNode cn = mn.getClasses().get(0);
        assertNotNull(cn);
        assertTrue(cn.getName().equals("A"));
    }

    @Test
    public void testParsingDotTerminatedImport_538() {
        runNegativeTest(new String[] {
            "A.groovy",
            "import foo."
        },
        "----------\n" +
        "1. ERROR in A.groovy (at line 1)\n" +
        "\timport foo.\n" +
        "\t        ^\n" +
        "Groovy:Invalid import @ line 1, column 8.\n" +
        "----------\n");

        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());

        List<ImportNode> imports = mn.getStarImports();
        ImportNode recoveredImport = imports.get(0);
        assertEquals("foo.", recoveredImport.getPackageName());

        ClassNode cn = mn.getClasses().get(0);
        assertNotNull(cn);
        assertTrue(cn.getName().equals("A"));
    }

    @Test
    public void testParsingBlankImportStatic_538() {
        runNegativeTest(new String[] {
            "A.groovy",
            "import static \n"
        },
        "----------\n" +
        "1. ERROR in A.groovy (at line 1)\n" +
        "\timport static \n" +
        "\t^^^^^^^^^^^^^^\n" +
        "Groovy:unable to resolve class ?\n" +
        "----------\n");

        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());

        List<ImportNode> imports = mn.getImports();
        ImportNode recoveredImport = imports.get(0);
        assertEquals(0, recoveredImport.getStart());
        assertEquals(14, recoveredImport.getEnd());
        assertEquals("?", recoveredImport.getType().getName());
        assertTrue(mn.getStaticImports().isEmpty());

        ClassNode cn = mn.getClasses().get(0);
        assertNotNull(cn);
        assertTrue(cn.getName().equals("A"));
    }

    @Test
    public void testParsingBlankImportStatic2_538() {
        runNegativeTest(new String[] {
            "A.groovy",
            "import static ;\n"
        },
        "----------\n" +
        "1. ERROR in A.groovy (at line 1)\n" +
        "\timport static ;\n" +
        "\t^^^^^^^^^^^^^^\n" +
        "Groovy:unable to resolve class ?\n" +
        "----------\n");

        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());

        List<ImportNode> imports = mn.getImports();
        ImportNode recoveredImport = imports.get(0);
        assertEquals(0, recoveredImport.getStart());
        assertEquals(14, recoveredImport.getEnd());
        assertEquals("?", recoveredImport.getType().getName());
        assertTrue(mn.getStaticImports().isEmpty());

        ClassNode cn = mn.getClasses().get(0);
        assertNotNull(cn);
        assertTrue(cn.getName().equals("A"));
    }

    @Test
    public void testParsingDotTerminatedImportStatic_538() {
        runNegativeTest(new String[] {
            "A.groovy",
            "import static foo.Bar."
        },
        "----------\n" +
        "1. ERROR in A.groovy (at line 1)\n" +
        "\timport static foo.Bar.\n" +
        "\t              ^^^^^^^\n" +
        "Groovy:unable to resolve class foo.Bar\n" +
        "----------\n" +
        "2. ERROR in A.groovy (at line 1)\n" +
        "\timport static foo.Bar.\n" +
        "\t               ^\n" +
        "Groovy:Invalid import @ line 1, column 15.\n" +
        "----------\n");

        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());

        Map<String, ImportNode> imports = mn.getStaticStarImports();
        ImportNode recoveredImport = imports.get("foo.Bar");
        assertEquals("foo.Bar", recoveredImport.getType().getName());

        ClassNode cn = mn.getClasses().get(0);
        assertNotNull(cn);
        assertEquals("A", cn.getName());
    }

    @Test
    public void testParsingDotTerminatedImportFollowedByClassDeclaration_538() {
        runNegativeTest(new String[] {
            "A.groovy",
            "import foo.\n"+
            "\n"+
            "class Wibble {}\n"
        },
        "----------\n" +
        "1. ERROR in A.groovy (at line 1)\n" +
        "\timport foo.\n" +
        "\t        ^\n" +
        "Groovy:Invalid import @ line 1, column 8.\n" +
        "----------\n");

        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());

        List<ImportNode> imports = mn.getStarImports();
        ImportNode recoveredImport = imports.get(0);
        assertEquals("foo.", recoveredImport.getPackageName());

        ClassNode cn = mn.getClasses().get(0);
        assertNotNull(cn);
        assertEquals("Wibble", cn.getName());
    }

    @Test
    public void testParsingDotTerminatedImportFollowedByModifierAndClassDeclaration_538() {
        runNegativeTest(new String[] {
            "A.groovy",
            "import foo.\n"+
            "\n"+
            "public class Wibble {}\n"
        },
        "----------\n" +
        "1. ERROR in A.groovy (at line 1)\n" +
        "\timport foo.\n" +
        "\t        ^\n" +
        "Groovy:Invalid import @ line 1, column 8.\n" +
        "----------\n");

        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());

        List<ImportNode> imports = mn.getStarImports();
        ImportNode recoveredImport = imports.get(0);
        assertEquals("foo.", recoveredImport.getPackageName());

        ClassNode cn = mn.getClasses().get(0);
        assertNotNull(cn);
        assertEquals("Wibble", cn.getName());
    }

    @Test
    public void testParsingBlankImportFollowedByClassDeclaration_538() {
        runNegativeTest(new String[] {
            "A.groovy",
            "import\n"+
            "\n"+
            "public class Wibble {}\n"
        },
        "----------\n" +
        "1. ERROR in A.groovy (at line 1)\n" +
        "\timport\n" +
        "\t^^^^^^\n" +
        "Groovy:unable to resolve class ?\n" +
        "----------\n");

        ModuleNode mn = getModuleNode("A.groovy");
        assertNotNull(mn);
        assertFalse(mn.encounteredUnrecoverableError());

        List<ImportNode> imports = mn.getImports();
        ImportNode recoveredImport = imports.get(0);
        assertEquals("?", recoveredImport.getType().getName());

        ClassNode cn = mn.getClasses().get(0);
        assertNotNull(cn);
        assertEquals("Wibble", cn.getName());
    }

    @Test
    public void testParsingIncompleteClassDeclaration_495() {
        runNegativeTest(new String[] {
            "A.groovy",
            "class Bar {}\n"+
            "class FooTest extends Bar { }\n" +
            "class BBB extends FooTes"
        },
        "----------\n" +
        "1. ERROR in A.groovy (at line 3)\n" +
        "\tclass BBB extends FooTes\n" +
        "\t                  ^^^^^^\n" +
        "Groovy:unable to resolve class FooTes\n" +
        "----------\n" +
        "2. ERROR in A.groovy (at line 3)\n" +
        "\tclass BBB extends FooTes\n" +
        "\t                       ^\n" +
        "Groovy:Malformed class declaration @ line 3, column 24.\n" +
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
        assertEquals("FooTest", cn.getName());
        cn = mn.getClasses().get(1);
        assertNotNull(cn);
        assertEquals("BBB", cn.getName());
    }

    @Test @Ignore
    public void testSts3930() {
        runConformTest(new String[] {
            "GroovyDemo.groovy",
            "package demo\n"+
            "class GroovyDemo {\n" +
            "    static <T> List someMethod(Class<T> factoryClass, ClassLoader classLoader = GroovyDemo.class.classLoader) {}\n" +
            "}",

            "JavaDemo.java",
            "package demo;\n"+
            "public class JavaDemo {\n" +
            "    public static void someMethod() {\n" +
            "        GroovyDemo.someMethod(JavaDemo.class);\n" +
            "    }\n" +
            "}\n",
        },
        "");
    }

    @Test @Ignore("FIXASC testcase for this. infinite loops (B extends B<String>")
    public void testCyclicReference() {
        runConformTest(new String[] {
            "p/B.groovy",
            "package p;\n" +
            "class B extends B<String> {\n" +
            "  public static void main(String[] argv) {\n"+
            "    new B();\n"+
            "    print \"success\"\n"+
            "  }\n"+
            "}\n",

            "p/A.java",
            "package p;\n" +
            "public class A<T> {}\n",
        },
        "");
    }

    @Test
    public void testInnerClass1() {
        runConformTest(new String[] {
            "A.groovy",
            "def foo = new Runnable() {\n" +
            "  void run() {\n" +
            "    println 'hi!'\n" +
            "  }\n" +
            "}\n" +
            "foo.run()\n",
        },
        "hi!");

        checkGCUDeclaration("A.groovy",
            "public class A extends groovy.lang.Script {\n" +
            "  public A() {\n" +
            "  }\n" +
            "  public A(groovy.lang.Binding context) {\n" +
            "  }\n" +
            "  public static void main(java.lang.String... args) {\n" +
            "  }\n" +
            "  public java.lang.Object run() {\n" +
            "    new Runnable() {\n" +
            "      x() {\n" +
            "        super();\n" +
            "      }\n" +
            "      public void run() {\n" +
            "      }\n" +
            "    };\n" +
            "  }\n" +
            "}");
    }

    @Test
    public void testInnerClass1a() {
        runConformTest(new String[] {
            "A.groovy",
            "class A {" +
            "  def foo = new Runnable() {\n" +
            "    void run() {\n" +
            "      println 'hi!'\n" +
            "    }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    new A().foo.run()\n" +
            "  }\n" +
            "}\n",
        },
        "hi!");

        checkGCUDeclaration("A.groovy",
            "public class A {\n" +
            "  private java.lang.Object foo = new Runnable() {\n" +
            "    x() {\n" +
            "      super();\n" +
            "    }\n" +
            "    public void run() {\n" +
            "    }\n" +
            "  };\n" +
            "  public A() {\n" +
            "  }\n" +
            "  public static void main(java.lang.String... args) {\n" +
            "  }\n" +
            "}");
    }

    @Test
    public void testInnerClass2() {
        runConformTest(new String[] {
            "A.groovy",
            "def foo = new Runnable() {\n" +
            "  void run() {\n" +
            "    println 'bye!'\n" +
            "  }\n" +
            "}\n" +
            "foo = new Runnable() {\n" +
            "  void run() {\n" +
            "    println 'hi!'\n" +
            "  }\n" +
            "}\n" +
            "foo.run()",
        },
        "hi!");
    }

    @Test
    public void testInnerClass3() {
        runConformTest(new String[] {
            "A.groovy",
            "def foo() {\n" +
            "  new Runnable() {\n" +
            "    void run() {\n" +
            "      println 'hi!'\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "foo().run()",
        },
        "hi!");
    }

    @Test
    public void testInnerClass4() {
        runConformTest(new String[] {
            "A.groovy",
            "class Foo {\n" +
            "  def foo = new Runnable() {\n" +
            "    void run() {\n" +
            "      println 'hi!'\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "new Foo().foo.run()\n",
        },
        "hi!");
    }

    @Test
    public void testInnerClass5() {
        runNegativeTest(new String[] {
            "A.groovy",
            "def foo = new Runnable() {\n" +
            "  void bad() {\n" +
            "    println 'hi!'\n" +
            "  }\n" +
            "}\n",
        },
        "----------\n" +
        "1. ERROR in A.groovy (at line 1)\n" +
        "\tdef foo = new Runnable() {\n" +
        "\t              ^^^^^^^^\n" +
        "Groovy:Can't have an abstract method in a non-abstract class. The class 'A$1' must be declared abstract or the method 'void run()' must be implemented.\n" +
        "----------\n");
    }

    @Test
    public void testAbstractMethodWithinEnum1() {
        runNegativeTest(new String[] {
            "Good.groovy",
            "enum Good {\n" +
            "  A() {\n" +
            "    @Override\n" +
            "    int foo() {\n" +
            "      1\n" +
            "    }\n" +
            "  }\n" +
            "  abstract int foo()\n" +
            "}"
        },
        "");

        checkGCUDeclaration("Good.groovy",
            "public enum Good {\n" +
            "  A,\n" +
            "  private Good() {\n" +
            "  }\n" +
            "  public abstract int foo();\n" +
            "}");
    }

    @Test
    public void testAbstractMethodWithinEnum2() {
        runNegativeTest(new String[] {
            "Bad.groovy",
            "enum Bad {\n" +
            "  A() {\n" +
            "  }\n" +
            "  abstract int foo()\n" +
            "}"
        },
        "----------\n" +
        "1. ERROR in Bad.groovy (at line 2)\n" +
        "\tA() {\n" +
        "\t^\n" +
        "Groovy:Can't have an abstract method in enum constant A. Implement method 'int foo()'.\n" +
        "----------\n");
    }

    //--------------------------------------------------------------------------

    private void assertEventCount(int expectedCount, EventListener listener) {
        if (listener.eventCount() != expectedCount) {
            fail("Expected " + expectedCount + " events but found " + listener.eventCount() + "\nEvents:\n" + listener.toString());
        }
    }

    private void assertEvent(String eventText, EventListener listener) {
        boolean found = false;
        Iterator<String> eventIter = listener.getEvents().iterator();
        while (eventIter.hasNext()) {
            String s = eventIter.next();
            if (s.equals(eventText)) {
                found = true;
                break;
            }
        }
        if (!found) {
            fail("Expected event '" + eventText + "'\nEvents:\n" + listener.toString());
        }
    }

    /**
     * Find the named file (which should have just been compiled) and for the named method determine
     * the ClassNode for the return type and return the name of the classnode.
     */
    public String getReturnTypeOfMethod(String filename, String methodname) {
        ModuleNode mn = getModuleNode(filename);
        ClassNode cn = mn.getClasses().get(0);
        assertNotNull(cn);
        MethodNode methodNode = cn.getMethod(methodname, Parameter.EMPTY_ARRAY);
        assertNotNull(methodNode);
        ClassNode returnType = methodNode.getReturnType();
        return returnType.getName();
    }

    private String stringifyFieldDecl(FieldDeclaration fDecl) {
        StringBuffer sb = new StringBuffer();
        sb.append(fDecl.name);
        sb.append(" sourceStart>sourceEnd:" + fDecl.sourceStart + ">" + fDecl.sourceEnd);
        sb.append(" declSourceStart>declSourceEnd:" + fDecl.declarationSourceStart + ">" + fDecl.declarationSourceEnd);
        sb.append(" modifiersSourceStart=" + fDecl.modifiersSourceStart); // first char of decls modifiers
        sb.append(" endPart1Position:" + fDecl.endPart1Position); // char after type decl ('int x,y' is space)
        return sb.toString();
    }
}
