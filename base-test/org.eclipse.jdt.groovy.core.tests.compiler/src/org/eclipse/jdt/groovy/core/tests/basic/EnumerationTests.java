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
package org.eclipse.jdt.groovy.core.tests.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTClassNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.junit.Test;

public final class EnumerationTests extends GroovyCompilerTestSuite {

    @Test
    public void testEnums1() {
        //@formatter:off
        String[] sources = {
            "p/Foo.groovy",
            "package p;\n" +
            "class Foo {\n" +
            "  static main(args) {\n" +
            "    print E.F\n" +
            "  }\n" +
            "}\n",

            "p/E.java",
            "package p;\n" +
            "enum E { F, G; }\n",
        };
        //@formatter:on

        runConformTest(sources, "F");
    }

    @Test
    public void testEnums2() {
        //@formatter:off
        String[] sources = {
            "p/E.groovy",
            "package p\n" +
            "enum E {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testEnums3() {
        //@formatter:off
        String[] sources = {
            "Color.groovy",
            "enum Color { R, G, B }\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testEnums4() {
        try {
            JDTResolver.recordInstances = true;
            //@formatter:off
            String[] sources = {
                "EE.groovy",
                "enum EE { A, B, C; }\n",

                "Foo.java",
                "public class Foo<E extends Foo<E>> implements Comparable<E> {" +
                "  public int compareTo(E b) { return 0; }\n" +
                "}\n",

                "Goo.java",
                "public class Goo<X extends Goo<X>> extends Foo<X> { }\n",

                "Bar.groovy",
                "abstract class Bar extends Goo<Bar> {" +
                "  int compareTo(Bar b) { return 0; }\n" +
                "  EE getEnum() { return null; }\n" +
                "}\n",
            };
            //@formatter:on

            runConformTest(sources);

            // Check on the state of Comparable
            JDTClassNode classnode = JDTResolver.getCachedNode("java.lang.Comparable<E>");
            assertNotNull(classnode);
            // Should have one method
            List<MethodNode> methods = classnode.getMethods();
            assertEquals(1, methods.size());
            assertEquals("int compareTo(java.lang.Object)", methods.get(0).getTypeDescriptor());

            classnode.lazyClassInit();
        } finally {
            JDTResolver.instances.clear();
            JDTResolver.recordInstances = false;
        }
    }

    @Test
    public void testEnums5() {
        try {
            JDTResolver.recordInstances = true;
            //@formatter:off
            String[] sources = {
                "p/Run.groovy",
                "package p;\n" +
                "import static p.q.r.Colour.*;\n" +
                "import p.q.r.Colour2;\n" +
                "public class Run {\n" +
                "  public static void main(String[] argv) {\n" +
                "    System.out.print(Red);\n" +
                "    System.out.print(Green);\n" +
                "    System.out.print(Blue);\n" +
                "   Colour2 c2 = new Colour2();\n" +
                "   int i = c2.compareTo('abc');\n" +
                "  }\n" +
                "}\n",

                "p/q/r/Colour.java",
                "package p.q.r;\n" +
                "enum Colour { Red,Green,Blue; }\n",

                "p/q/r/Colour3.java",
                "package p.q.r;\n" +
                "@SuppressWarnings(\"rawtypes\")\n" +
                "class Colour3 implements Comparable { public int compareTo(Object o) { return 0;}}\n",

                "p/q/r/Colour2.java",
                "package p.q.r;\n" +
                "public class Colour2 implements Comparable<String> { \n" +
                "  public int compareTo(String s) { return 0; } \n" +
                "}\n",
            };
            //@formatter:on

            runConformTest(sources, "RedGreenBlue");

            // Check on the state of Comparable
            JDTClassNode classnode = JDTResolver.getCachedNode("java.lang.Comparable<E>");
            assertNotNull(classnode);
            // Should have one method
            List<MethodNode> methods = classnode.getMethods();
            assertEquals(1, methods.size());
            assertEquals("int compareTo(java.lang.Object)", methods.get(0).getTypeDescriptor());
        } finally {
            JDTResolver.instances.clear();
            JDTResolver.recordInstances = false;
        }
    }

    @Test
    public void testEnumStatic_GRE974() {
        //@formatter:off
        String[] sources = {
            "be/flow/A.groovy",
            "package be.flow\n" +
            "\n" +
            "enum C1{\n" +
            "  TEST_C1\n" +
            "}\n" +
            "\n" +
            "class A {\n" +
            "  public enum C2{\n" +
            "    TEST_C2\n" +
            "  }\n" +
            "}\n",

            "be/flow/B.groovy",
            "package be.flow\n" +
            "\n" +
            "import static be.flow.C1.TEST_C1;\n" +
            "import static be.flow.A.C2.*;\n" +
            "\n" +
            "class B {\n" +
            "  B(){\n" +
            "    super(TEST_C2)\n" +
            "  }\n" +
            "  \n" +
            "  void doIt(){\n" +
            "    println(be.flow.C1.TEST_C1);\n" +
            "    println(be.flow.A.C2.TEST_C2);\n" +
            "    println(TEST_C2);\n" +
            "  }\n" +
            "}\n",

            "be/flow/D.groovy",
            "package be.flow\n" +
            "\n" +
            "import static be.flow.C1.TEST_C1;\n" +
            "import static be.flow.A.C2.*;\n" +
            "\n" +
            "class D {\n" +
            "  \n" +
            "  static void doIt(){\n" +
            "    println(be.flow.C1.TEST_C1);\n" +
            "    println(be.flow.A.C2.TEST_C2);\n" +
            "    println(TEST_C2);\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test // GROOVY-4219
    public void testGRE637() {
        //@formatter:off
        String[] sources = {
            "de/brazzy/nikki/Texts.java",
            "package de.brazzy.nikki;\n" +
            "\n" +
            "public final class Texts { \n" +
            "  public static class Image {\n" +
            "    public static final String ORDERED_BY_FILENAME = \"image.sortedby.filename\";\n" +
            "    public static final String ORDERED_BY_TIME = \"image.sortedby.time\";\n" +
            "  }\n" +
            "}\n",

            "de/brazzy/nikki/model/Image.groovy",
            "package de.brazzy.nikki.model\n" +
            "\n" +
            "class Image implements Serializable{\n" +
            "  def fileName\n" +
            "  def time\n" +
            "}\n",

            "de/brazzy/nikki/model/ImageSortField.groovy",
            "package de.brazzy.nikki.model\n" +
            "\n" +
            "import de.brazzy.nikki.Texts\n" +
            "import de.brazzy.nikki.model.Image\n" +
            "\n" +
            "enum ImageSortField {\n" +
            "    FILENAME(field: Image.metaClass.fileName, name: Texts.Image.ORDERED_BY_FILENAME),\n" +
            "    TIME(field: Image.metaClass.time, name: Texts.Image.ORDERED_BY_TIME)\n" +
            "\n" +
            "    def field\n" +
            "    def name\n" +
            "\n" +
            "    public String toString(){\n" +
            "        name\n" +
            "    }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testEnumValues_GRE1071() {
        //@formatter:off
        String[] sources = {
            "H.groovy",
            "enum H {\n" +
            "  RED,\n" +
            "  BLUE\n" +
            "}",
        };
        //@formatter:on

        runConformTest(sources);

        assertEquals("[LH;", getReturnTypeOfMethod("H.groovy", "values"));
    }

    @Test
    public void testEnumPositions_GRE1072() {
        //@formatter:off
        String[] sources = {
            "Color.groovy",
            "enum Color {\n" +
            "  /** hello */\n" +
            "  RED,\n" +
            "  GREEN,\n" +
            "  BLUE\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);

        GroovyCompilationUnitDeclaration decl = getCUDeclFor("Color.groovy");

        FieldDeclaration fDecl = findField(decl, "RED");
        assertEquals("RED sourceStart>sourceEnd:30>32 declSourceStart>declSourceEnd:15>32 modifiersSourceStart=30 endPart1Position:30", stringify(fDecl));

        fDecl = findField(decl, "GREEN");
        assertEquals("GREEN sourceStart>sourceEnd:37>41 declSourceStart>declSourceEnd:37>41 modifiersSourceStart=37 endPart1Position:37", stringify(fDecl));

        fDecl = findField(decl, "BLUE");
        assertEquals("BLUE sourceStart>sourceEnd:46>49 declSourceStart>declSourceEnd:46>49 modifiersSourceStart=46 endPart1Position:46", stringify(fDecl));
    }

    @Test
    public void testConstructorsForEnumWrong_GRE285() {
        //@formatter:off
        String[] sources = {
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
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testAbstractMethodWithinEnum1() {
        //@formatter:off
        String[] sources = {
            "Good.groovy",
            "enum Good {\n" +
            "  A() {\n" +
            "    @Override\n" +
            "    int foo() {\n" +
            "      1\n" +
            "    }\n" +
            "  }\n" +
            "  abstract int foo()\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        checkGCUDeclaration("Good.groovy",
            "public enum Good {\n" +
            "  A() {\n" +
            "    x() {\n" +
            "      super();\n" +
            "    }\n" +
            "    public @Override int foo() {\n" +
            "    }\n" +
            "  },\n" +
            "  private Good() {\n" +
            "  }\n" +
            "  public abstract int foo();\n" +
            "}");
    }

    @Test
    public void testAbstractMethodWithinEnum2() {
        //@formatter:off
        String[] sources = {
            "Bad.groovy",
            "enum Bad {\n" +
            "  A() {\n" +
            "  }\n" +
            "  abstract int foo()\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Bad.groovy (at line 2)\n" +
            "\tA() {\n" +
            "\t^^^\n" +
            "Groovy:Can't have an abstract method in enum constant A. Implement method 'int foo()'.\n" +
            "----------\n");
    }

    @Test
    public void testStaticVariableInScript() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "enum Move { ROCK, PAPER, SCISSORS }\n" +
            "\n" +
            "static final BEATS = [\n" +
            "   [Move.ROCK,     Move.SCISSORS],\n" +
            "   [Move.PAPER,    Move.ROCK    ],\n" +
            "   [Move.SCISSORS, Move.PAPER   ]\n" +
            "].asImmutable()\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Script.groovy (at line 3)\n" +
            "\tstatic final BEATS = [\n" +
            "\t             ^^^^^\n" +
            "Groovy:Modifier 'static' not allowed here.\n" +
            "----------\n");
    }
}
