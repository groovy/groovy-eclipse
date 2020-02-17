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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

import java.util.List;

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTClassNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.junit.Test;

public final class EnumerationTests extends GroovyCompilerTestSuite {

    @Test
    public void testEnum0() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import p.E\n" +
            "println E.values()\n",

            "p/E.groovy",
            "package p\n" +
            "enum E {\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[]");
    }

    @Test
    public void testEnum1() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "println Color.values()\n",

            "Color.groovy",
            "enum Color { R, G, B }\n",
        };
        //@formatter:on

        runConformTest(sources, "[R, G, B]");
    }

    @Test
    public void testEnum1a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "println Color.values()\n",

            "Color.groovy",
            "enum Color { R, G, B; }\n", // semicolon
        };
        //@formatter:on

        runConformTest(sources, "[R, G, B]");
    }

    @Test
    public void testEnum1b() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "println Color.values()\n",

            "Color.groovy",
            "enum Color { R, G, B, }\n", // trailing comma
        };
        //@formatter:on

        runConformTest(sources, "[R, G, B]");
    }

    @Test
    public void testEnum1c() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "println Color.values()\n",

            "Color.groovy",
            "enum Color {\n" +
            "  R,\n" +
            "  G,\n" +
            "  B,\n" +
            "  ;" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[R, G, B]");
    }

    @Test
    public void testEnum2() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "println Orientation.values()\n",

            "Orientation.groovy",
            "enum Orientation {\n" +
            "  /** comment */" +
            "  LANDSCAPE,\n" +
            "  /** comment */" +
            "  PORTRAIT\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[LANDSCAPE, PORTRAIT]");
    }

    @Test
    public void testEnum2a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "println Orientation.values()\n",

            "Orientation.groovy",
            "enum Orientation {\n" +
            "  /** comment */" +
            "  LANDSCAPE,\n" +
            "  /** comment */" +
            "  PORTRAIT;\n" + // semicolon
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[LANDSCAPE, PORTRAIT]");
    }

    @Test
    public void testEnum2b() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "println Orientation.values()\n",

            "Orientation.groovy",
            "enum Orientation {\n" +
            "  /** comment */" +
            "  LANDSCAPE,\n" +
            "  /** comment */" +
            "  PORTRAIT,\n" + // trailing comma
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[LANDSCAPE, PORTRAIT]");
    }

    @Test
    public void testEnum3() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "println Orientation.values()\n",

            "Orientation.groovy",
            "enum Orientation {\n" +
            "  LANDSCAPE, PORTRAIT\n" +
            "  \n" +
            "  @Override\n" +
            "  String toString() {\n" +
            "    name().toLowerCase().capitalize()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[Landscape, Portrait]");
    }

    @Test
    public void testEnum3a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "println Orientation.values()\n",

            "Orientation.groovy",
            "enum Orientation {\n" +
            "  LANDSCAPE, PORTRAIT;\n" + // semicolon
            "  \n" +
            "  @Override\n" +
            "  String toString() {\n" +
            "    name().toLowerCase().capitalize()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[Landscape, Portrait]");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-9301
    public void testEnum3b() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "println Orientation.values()\n",

            "Orientation.groovy",
            "enum Orientation {\n" +
            "  LANDSCAPE, PORTRAIT,\n" + // trailing comma
            "  \n" +
            "  @Override\n" +
            "  String toString() {\n" +
            "    name().toLowerCase().capitalize()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[Landscape, Portrait]");
    }

    @Test
    public void testEnum4() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "println Orientation.values()\n",

            "Orientation.groovy",
            "enum Orientation {\n" +
            "  LANDSCAPE('Landscape'), PORTRAIT('Portrait')\n" +
            "  \n" +
            "  Orientation(String string) {\n" +
            "    this.string = string\n" +
            "  }\n" +
            "  private String string\n" +
            "  \n" +
            "  @Override\n" +
            "  String toString() {\n" +
            "    return string\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[Landscape, Portrait]");
    }

    @Test
    public void testEnum4a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "println Orientation.values()\n",

            "Orientation.groovy",
            "enum Orientation {\n" +
            "  LANDSCAPE('Landscape'), PORTRAIT('Portrait');\n" + // semicolon
            "  \n" +
            "  Orientation(String string) {\n" +
            "    this.string = string\n" +
            "  }\n" +
            "  private String string\n" +
            "  \n" +
            "  @Override\n" +
            "  String toString() {\n" +
            "    return string\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[Landscape, Portrait]");
    }

    @Test
    public void testEnum4b() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "println Orientation.values()\n",

            "Orientation.groovy",
            "enum Orientation {\n" +
            "  LANDSCAPE('Landscape'), PORTRAIT('Portrait'),\n" + // trailing comma
            "  \n" +
            "  Orientation(String string) {\n" +
            "    this.string = string\n" +
            "  }\n" +
            "  private String string\n" +
            "  \n" +
            "  @Override\n" +
            "  String toString() {\n" +
            "    string\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[Landscape, Portrait]");
    }

    @Test
    public void testEnum5() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "println Orientation.values()\n",

            "Orientation.groovy",
            "enum Orientation {\n" +
            "  LANDSCAPE('Landscape'), PORTRAIT('Portrait')\n" +
            "  \n" +
            "  private String string\n" +
            "  \n" +
            "  Orientation(String string) {\n" +
            "    this.string = string\n" +
            "  }\n" +
            "  \n" +
            "  @Override\n" +
            "  String toString() {\n" +
            "    return string\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[Landscape, Portrait]");
    }

    @Test
    public void testEnum5a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "println Orientation.values()\n",

            "Orientation.groovy",
            "enum Orientation {\n" +
            "  LANDSCAPE('Landscape'), PORTRAIT('Portrait');\n" + // semicolon
            "  \n" +
            "  private String string\n" +
            "  \n" +
            "  Orientation(String string) {\n" +
            "    this.string = string\n" +
            "  }\n" +
            "  \n" +
            "  @Override\n" +
            "  String toString() {\n" +
            "    return string\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[Landscape, Portrait]");
    }

    @Test
    public void testEnum5b() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "println Orientation.values()\n",

            "Orientation.groovy",
            "enum Orientation {\n" +
            "  LANDSCAPE('Landscape'), PORTRAIT('Portrait'),\n" + // trailing comma
            "  \n" +
            "  private String string\n" +
            "  \n" +
            "  Orientation(String string) {\n" +
            "    this.string = string\n" +
            "  }\n" +
            "  \n" +
            "  @Override\n" +
            "  String toString() {\n" +
            "    string\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[Landscape, Portrait]");
    }

    @Test
    public void testEnum6() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "println NonFinal.values()\n",

            "NonFinal.groovy",
            "enum NonFinal {\n" +
            "  One(1), Two(2)\n" +
            "  Object value\n" + // different parsing without leading keyword
            "  NonFinal(value) {\n" +
            "    this.value = value\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[One, Two]");
    }

    @Test
    public void testEnum6a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "println NonFinal.values()\n",

            "NonFinal.groovy",
            "enum NonFinal {\n" +
            "  One(1), Two(2);\n" +
            "  Object value\n" +
            "  NonFinal(value) {\n" +
            "    this.value = value\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[One, Two]");
    }

    @Test
    public void testEnum6b() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "println NonFinal.values()\n",

            "NonFinal.groovy",
            "enum NonFinal {\n" +
            "  One(1), Two(2),\n" +
            "  Object value\n" +
            "  NonFinal(value) {\n" +
            "    this.value = value\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[One, Two]");
    }

    @Test
    public void testEnum7() {
        //@formatter:off
        String[] sources = {
            "Cards.groovy",
            "enum Color {\n" +
            "  RED,\n" +
            "  BLACK\n" +
            "}\n" +
            "enum Suit {\n" +
            "  CLUBS(Color.BLACK),\n" +
            "  DIAMONDS(Color.RED),\n" +
            "  HEARTS(Color.RED),\n" +
            "  SPADES(Color.BLACK),\n" +
            "  \n" +
            "  final Color color\n" +
            "  Suit(Color color) {\n" +
            "    this.color = color\n" +
            "  }\n" +
            "}\n" +
            "print \"${Suit.SPADES.name()} are ${Suit.SPADES.color}\"\n",
        };
        //@formatter:on

        runConformTest(sources, "SPADES are BLACK");
    }

    @Test
    public void testEnum8() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "enum Whatever {\n" +
            "  ONE(1), TWO(2)\n\n" +
            "  Whatever(value) {\n" +
            "    _value = value\n" +
            "  }\n\n" +
            "  private final int _value\n" +
            "}\n" +
            "print Whatever.ONE.@_value\n",
        };
        //@formatter:on

        runConformTest(sources, "1");
    }

    @Test
    public void testEnum9() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "enum Whatever {\n" +
            "  @Deprecated ONE(1), TWO(2)\n\n" +
            "  Whatever(value) {\n" +
            "    _value = value\n" +
            "  }\n\n" +
            "  private final int _value\n" +
            "}\n" +
            "print Whatever.TWO.ordinal()\n",
        };
        //@formatter:on

        runConformTest(sources, "1");
    }

    @Test
    public void testEnum10() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "enum Whatever {\n" +
            "  ONE(1), @Deprecated TWO(2)\n\n" +
            "  Whatever(value) {\n" +
            "    _value = value\n" +
            "  }\n\n" +
            "  private final int _value\n" +
            "}\n" +
            "print Whatever.TWO.ordinal()\n",
        };
        //@formatter:on

        runConformTest(sources, "1");
    }

    @Test
    public void testEnum11() {
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
    public void testEnum12() {
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
    public void testEnum13() {
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

    @Test // https://issues.apache.org/jira/browse/GROOVY-4219
    public void testEnum4219() {
        //@formatter:off
        String[] sources = {
            "de/brazzy/nikki/Texts.java",
            "package de.brazzy.nikki;\n" +
            "\n" +
            "public final class Texts {\n" +
            "  public static class Image {\n" +
            "    public static final String ORDERED_BY_FILENAME = \"image.sortedby.filename\";\n" +
            "    public static final String ORDERED_BY_TIME = \"image.sortedby.time\";\n" +
            "  }\n" +
            "}\n",

            "de/brazzy/nikki/model/Image.groovy",
            "package de.brazzy.nikki.model\n" +
            "\n" +
            "class Image implements Serializable {\n" +
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
            "  FILENAME(field: Image.metaClass.fileName, name: Texts.Image.ORDERED_BY_FILENAME),\n" +
            "  TIME(field: Image.metaClass.time, name: Texts.Image.ORDERED_BY_TIME)\n" +
            "  \n" +
            "  def field\n" +
            "  def name\n" +
            "  \n" +
            "  public String toString(){\n" +
            "    name\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test(timeout = 1500) // https://issues.apache.org/jira/browse/GROOVY-4438
    public void testEnum4438() {
        //@formatter:off
        String[] sources = {
            "Outer.groovy",
            "enum Outer {\n" +
            "  A,\n" +
            "  B\n" +
            "  enum Inner {\n" +
            "    X,\n" +
            "    Y\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test(timeout = 1500) // https://issues.apache.org/jira/browse/GROOVY-8507
    public void testEnum8507() {
        //@formatter:off
        String[] sources = {
            "Outer.groovy",
            "enum Outer {\n" +
            "  A,\n" +
            "  enum Inner {\n" +
            "    X,\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
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
        assertEquals("RED declarationSourceStart:15 modifiersSourceStart:30 endPart1Position:0 sourceStart:30" +
                " sourceEnd:32 endPart2Position:0 declarationEnd:32 declarationSourceEnd:32", stringify(fDecl));

        fDecl = findField(decl, "GREEN");
        assertEquals("GREEN declarationSourceStart:37 modifiersSourceStart:37 endPart1Position:0 sourceStart:37" +
                " sourceEnd:41 endPart2Position:0 declarationEnd:41 declarationSourceEnd:41", stringify(fDecl));

        fDecl = findField(decl, "BLUE");
        assertEquals("BLUE declarationSourceStart:46 modifiersSourceStart:46 endPart1Position:0 sourceStart:46" +
                " sourceEnd:49 endPart2Position:0 declarationEnd:49 declarationSourceEnd:49", stringify(fDecl));
    }

    @Test
    public void testConstructorsForEnumWrong_GRE285() {
        //@formatter:off
        String[] sources = {
            "TestEnum.groovy",
            "enum TestEnum {\n" +
            "  VALUE1(1, 'foo'),\n" +
            "  VALUE2(2)\n" +
            "  \n" +
            "  private final int _value\n" +
            "  private final String _description\n" +
            "  \n" +
            "  private TestEnum(int value, String description = null) {\n" +
            "    _value = value\n" +
            "    _description = description\n" +
            "  }\n" +
            "  \n" +
            "  String getDescription() { _description }\n" +
            "  \n" +
            "  int getValue() { _value }\n" +
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
            "  private " + (isAtLeastGroovy(25) ? "@groovy.transform.Generated " : "") + "Good() {\n" +
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

    @Test // GROOVY-8444
    public void testSwitchCasesWithoutQualifier() {
        assumeTrue(isAtLeastGroovy(30));

        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "enum Move { ROCK, PAPER, SCISSORS }\n" +
            "\n" +
            "@groovy.transform.CompileStatic\n" +
            "void make(Move move) {\n" +
            "  switch (move) {\n" +
            "   case ROCK:\n" +
            "    print 'rock'; break\n" +
            "   case PAPER:\n" +
            "    print 'paper'; break\n" +
            "   case SCISSORS:\n" +
            "    print 'scissors'; break\n" +
            "   default:\n" +
            "    throw new AssertionError(move);\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "make(Move.ROCK)\n",
        };
        //@formatter:on

        runConformTest(sources, "rock");
    }
}
