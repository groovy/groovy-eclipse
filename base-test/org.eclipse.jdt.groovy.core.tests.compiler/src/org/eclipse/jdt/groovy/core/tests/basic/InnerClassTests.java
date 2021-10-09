/*
 * Copyright 2009-2021 the original author or authors.
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

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isParrotParser;
import static org.junit.Assume.assumeTrue;

import org.junit.Test;

public final class InnerClassTests extends GroovyCompilerTestSuite {

    @Test
    public void testInnerTypeReferencing1() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  static main(args) {\n" +
            "    print Outer.Inner.VALUE\n" +
            "  }\n" +
            "}\n",

            "Outer.java",
            "class Outer {\n" +
            "  static class Inner {\n" +
            "    static final String VALUE = \"value\";\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testInnerTypeReferencing2() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main extends Outer {\n" +
            "  static main(args) {\n" +
            "    print Inner.VALUE\n" +
            "  }\n" +
            "}\n",

            "Outer.java",
            "class Outer {\n" +
            "  static class Inner {\n" +
            "    static final String VALUE = \"value\";\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testInnerTypeReferencing3() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  static main(args) {\n" +
            "    print Outer.Inner.VALUE\n" +
            "  }\n" +
            "}\n",

            "Outer.java",
            "interface Outer {\n" +
            "  interface Inner {\n" +
            "    String VALUE = \"value\";\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testInnerTypeReferencing4() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main implements Outer {\n" +
            "  static main(args) {\n" +
            "    print Inner.VALUE\n" +
            "  }\n" +
            "}\n",

            "Outer.java",
            "interface Outer {\n" +
            "  interface Inner {\n" +
            "    String VALUE = \"value\";\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testInnerTypeReferencing5() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "print Outer.Inner.VALUE\n",

            "Outer.java",
            "interface Outer {\n" +
            "  interface Inner {\n" +
            "    String VALUE = \"value\";\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testInnerTypeReferencing6() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main extends Outer {\n" +
            "  static main(args) {\n" +
            "    new Main(Inner.VALUE)\n" +
            "  }\n" +
            "  Main(Inner inner) {\n" +
            "    print inner.name()\n" +
            "  }\n" +
            "}\n",

            "Outer.java",
            "class Outer {\n" +
            "  enum Inner {\n" +
            "    VALUE;\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "VALUE");
    }

    @Test
    public void testInnerClass1() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            " class Inner {}\n" +
            "  static main(args) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");

        checkGCUDeclaration("X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  public class Inner {\n" +
            "    public @groovy.transform.Generated Inner() {\n" +
            "    }\n" +
            "  }\n" +
            "  public @groovy.transform.Generated X() {\n" +
            "  }\n" +
            "  public static void main(java.lang.String... args) {\n" +
            "  }\n" +
            "}\n"
        );
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/718
    public void testInnerClass2() {
        //@formatter:off
        String[] sources = {
            "Outer.groovy",
            "class Outer {\n" +
            "  class Inner {\n" +
            "    static {\n" +
            "      println '<clinit>'\n" +
            "    }\n" +
            "  }\n" +
            "  def method() {\n" +
            "    new Inner()\n" +
            "  }\n" +
            "  static void main(args) {\n" +
            "    new Outer().method()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "<clinit>");
    }

    @Test
    public void testInnerClass3() {
        //@formatter:off
        String[] sources = {
            "Outer.groovy",
            "class Outer {\n" +
            "  static class Inner {\n" +
            "    static {\n" +
            "      println '<clinit>'\n" +
            "    }\n" +
            "  }\n" +
            "  static void main(args) {\n" +
            "    new Inner()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "<clinit>");
    }

    @Test
    public void testInnerClass4() {
        //@formatter:off
        String[] sources = {
            "WithInnerClass.groovy",
            "class WithInnerClass {\n" +
            "  interface InnerInterface {\n" +
            "    void foo()\n" +
            "  }\n" +
            "  private final InnerInterface foo = new InnerInterface() {\n" +
            "     void foo() {\n" +
            "     }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test // GROOVY-4287
    public void testInnerClass5() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import static p.Outer.Inner\n" +
            "new Inner()\n",

            "p/Outer.groovy",
            "package p\n" +
            "class Outer {\n" +
            "  static class Inner {\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/708
    public void testInnerClass6() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import p.Sideways.Inner\n",

            "p/Outer.groovy",
            "package p\n" +
            "class Outer {\n" +
            "  static class Inner {\n" +
            "  }\n" +
            "}\n" +
            "class Sideways extends Outer {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Script.groovy (at line 1)\n" +
            "\timport p.Sideways.Inner\n" +
            "\t       ^^^^^^^^^^^^^^^^\n" +
            "Groovy:unable to resolve class p.Sideways.Inner\n" +
            "----------\n");
    }

    @Test
    public void testInnerClass7() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Outer {\n" +
            "  @groovy.transform.TupleConstructor(defaults=false)\n" +
            "  class Inner {\n" +
            "    int p\n" +
            "  }\n" +
            "  static m(int n) {\n" +
            "    new Inner(new Outer(), n)\n" +
            "  }\n" +
            "}\n" +
            "print Outer.m(4).p\n" +
            "print new Outer.Inner(new Outer(), 2).p\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test // GROOVY-8947
    public void testInnerClass8() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Outer {\n" +
            "  @groovy.transform.TupleConstructor(defaults=false)\n" +
            "  class Inner {\n" +
            "    int p\n" +
            "  }\n" +
            "  static m(int n) {\n" +
            "    new Outer().new Inner(n)\n" +
            "  }\n" +
            "}\n" +
            "print Outer.m(4).p\n" +
            "print new Outer().new Inner(2).p\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testInnerClass9() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Outer {\n" +
            "  static class Inner {\n" + // TODO: GROOVY-9781
            "    String p\n" +
            "  }\n" +
            "  Inner m() {\n" +
            "    [p:'x']\n" + // calls ScriptBytecodeAdapter.castToType([p:'x'], Outer$Inner.class)
            "  }\n" +
            "  static String q = 'y'\n" +
            "}\n" +
            "o = new Outer().m()\n" +
            "print o.p\n" +
            "print o.q\n",
        };
        //@formatter:on

        runConformTest(sources, "xy");
    }

    @Test // GROOVY-10289
    public void testInnerClass10() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Outer {\n" +
            "  static class StaticInner {\n" +
            "    void test() {\n" +
            "      throw new NonStaticInner()\n" +
            "    }\n" +
            "  }\n" +
            "  class NonStaticInner extends RuntimeException {\n" +
            "  }\n" +
            "}\n" +
            "Outer.StaticInner.test()\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Script.groovy (at line 4)\n" +
            "\tthrow new NonStaticInner()\n" +
            "\t          ^^^^^^^^^^^^^^\n" +
            "Groovy:No enclosing instance passed in constructor call of a non-static inner class\n" +
            "----------\n");
    }

    @Test
    public void testAnonymousInnerClass1() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "def foo = new Runnable() {\n" +
            "  void run() {\n" +
            "    println 'hi!'\n" +
            "  }\n" +
            "}\n" +
            "foo.run()\n",
        };
        //@formatter:on

        runConformTest(sources, "hi!");

        checkGCUDeclaration("A.groovy",
            "public class A extends groovy.lang.Script {\n" +
            "  public A() {\n" +
            "  }\n" +
            "  public A(final groovy.lang.Binding context) {\n" +
            "  }\n" +
            "  public static void main(final java.lang.String... args) {\n" +
            "  }\n" +
            "  public @java.lang.Override java.lang.Object run() {\n" +
            "    java.lang.Object foo;\n" +
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
    public void testAnonymousInnerClass2() {
        //@formatter:off
        String[] sources = {
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
        };
        //@formatter:on

        runConformTest(sources, "hi!");

        checkGCUDeclaration("A.groovy",
            "public class A {\n" +
            "  private java.lang.Object foo = new Runnable() {\n" +
            "    x() {\n" +
            "      super();\n" +
            "    }\n" +
            "    public void run() {\n" +
            "    }\n" +
            "  };\n" +
            "  public @groovy.transform.Generated A() {\n" +
            "  }\n" +
            "  public static void main(java.lang.String... args) {\n" +
            "  }\n" +
            "}");
    }

    @Test
    public void testAnonymousInnerClass2a() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "class A {" +
            "  @Lazy def foo = new Runnable() {\n" +
            "    void run() {\n" +
            "      println 'hi!'\n" +
            "    }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    new A().foo.run()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "hi!");

        checkGCUDeclaration("A.groovy",
            "public class A {\n" +
            "  private @Lazy java.lang.Object foo = new Runnable() {\n" +
            "    x() {\n" +
            "      super();\n" +
            "    }\n" +
            "    public void run() {\n" +
            "    }\n" +
            "  };\n" +
            "  public @groovy.transform.Generated A() {\n" +
            "  }\n" +
            "  public static void main(java.lang.String... args) {\n" +
            "  }\n" +
            "}");
    }

    @Test
    public void testAnonymousInnerClass3() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "class A {" +
            "  def foo(int bar) {\n" +
            "    new Runnable() {\n" +
            "      void run() {\n" +
            "        println 'hi!'\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    new A().foo(0).run()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "hi!");

        checkGCUDeclaration("A.groovy",
            "public class A {\n" +
            "  public @groovy.transform.Generated A() {\n" +
            "  }\n" +
            "  public java.lang.Object foo(int bar) {\n" +
            "    new Runnable() {\n" +
            "      x() {\n" +
            "        super();\n" +
            "      }\n" +
            "      public void run() {\n" +
            "      }\n" +
            "    };\n" +
            "  }\n" +
            "  public static void main(java.lang.String... args) {\n" +
            "  }\n" +
            "}");
    }

    @Test
    public void testAnonymousInnerClass4() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "class A {" +
            "  def foo(int bar, int baz = 0) {\n" +
            "    new Runnable() {\n" +
            "      void run() {\n" +
            "        println 'hi!'\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    new A().foo(0, 1).run()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "hi!");

        checkGCUDeclaration("A.groovy",
            "public class A {\n" +
            "  public @groovy.transform.Generated A() {\n" +
            "  }\n" +
            "  public java.lang.Object foo(int bar, int baz) {\n" +
            "    new Runnable() {\n" +
            "      x() {\n" +
            "        super();\n" +
            "      }\n" +
            "      public void run() {\n" +
            "      }\n" +
            "    };\n" +
            "  }\n" +
            "  public java.lang.Object foo(int bar) {\n" +
            "  }\n" +
            "  public static void main(java.lang.String... args) {\n" +
            "  }\n" +
            "}");
    }

    @Test
    public void testAnonymousInnerClass5() {
        //@formatter:off
        String[] sources = {
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
        };
        //@formatter:on

        runConformTest(sources, "hi!");
    }

    @Test
    public void testAnonymousInnerClass6() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "def foo() {\n" +
            "  new Runnable() {\n" +
            "    void run() {\n" +
            "      println 'hi!'\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "foo().run()",
        };
        //@formatter:on

        runConformTest(sources, "hi!");
    }

    @Test
    public void testAnonymousInnerClass7() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "class Foo {\n" +
            "  def foo = new Runnable() {\n" +
            "    void run() {\n" +
            "      println 'hi!'\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "new Foo().foo.run()\n",
        };
        //@formatter:on

        runConformTest(sources, "hi!");
    }

    @Test
    public void testAnonymousInnerClass8() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "def foo = new Runnable() {\n" +
            "  void bad() {\n" +
            "    println 'hi!'\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in A.groovy (at line 1)\n" +
            "\tdef foo = new Runnable() {\n" +
            "\t              ^^^^^^^^^^\n" +
            "Groovy:Can't have an abstract method in a non-abstract class." +
            " The class 'A$1' must be declared abstract or the method 'void run()' must be implemented.\n" +
            "----------\n");
    }

    @Test
    public void testAnonymousInnerClass9() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "class A {\n" +
            "  static {\n" +
            "    def foo = new Runnable() {\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in A.groovy (at line 3)\n" +
            "\tdef foo = new Runnable() {\n" +
            "\t              ^^^^^^^^^^\n" +
            "Groovy:Can't have an abstract method in a non-abstract class." +
            " The class 'A$1' must be declared abstract or the method 'void run()' must be implemented.\n" +
            "----------\n");

        checkGCUDeclaration("A.groovy",
            "public class A {\n" +
            "  public @groovy.transform.Generated A() {\n" +
            "  }\n" +
            "  <clinit>() {\n" +
            "    java.lang.Object foo;\n" +
            "    new Runnable() {\n" +
            "      x() {\n" +
            "        super();\n" +
            "      }\n" +
            "    };\n" +
            "  }\n" +
            "}");
    }

    @Test
    public void testAnonymousInnerClass9a() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "class A {\n" +
            "  {\n" +
            "    def foo = new Runnable() {\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in A.groovy (at line 3)\n" +
            "\tdef foo = new Runnable() {\n" +
            "\t              ^^^^^^^^^^\n" +
            "Groovy:Can't have an abstract method in a non-abstract class." +
            " The class 'A$1' must be declared abstract or the method 'void run()' must be implemented.\n" +
            "----------\n");

        checkGCUDeclaration("A.groovy",
            "public class A {\n" +
            "  {\n" +
            "  }\n" +
            "  public @groovy.transform.Generated A() {\n" +
            "    new Runnable() {\n" +
            "      x() {\n" +
            "        super();\n" +
            "      }\n" +
            "    };\n" +
            "  }\n" +
            "}");
    }

    @Test
    public void testAnonymousInnerClass9b() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class A {}\n" +
            "new A() {\n" +
            "  {\n" +
            "    def foo = new Runnable() {\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Script.groovy (at line 4)\n" +
            "\tdef foo = new Runnable() {\n" +
            "\t              ^^^^^^^^^^\n" +
            "Groovy:Can't have an abstract method in a non-abstract class." +
            " The class 'Script$1$1' must be declared abstract or the method 'void run()' must be implemented.\n" +
            "----------\n");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/715
    public void testAnonymousInnerClass10() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "class A {" +
            "  def foo = new I<String>() {\n" +
            "    private static final long serialVersionUID = 1L\n" +
            "    String bar() {\n" +
            "      println 'hi!'\n" +
            "    }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    new A().foo.bar()\n" +
            "  }\n" +
            "}\n",

            "I.groovy",
            "interface I<T> extends Serializable {\n" +
            "  T bar()\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "hi!");

        checkGCUDeclaration("A.groovy",
            "public class A {\n" +
            "  private java.lang.Object foo = new I<String>() {\n" +
            "    private static final long serialVersionUID = 1L;\n" +
            "    x() {\n" +
            "      super();\n" +
            "    }\n" +
            "    public String bar() {\n" +
            "    }\n" +
            "  };\n" +
            "  public @groovy.transform.Generated A() {\n" +
            "  }\n" +
            "  public static void main(java.lang.String... args) {\n" +
            "  }\n" +
            "}\n");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/800
    public void testAnonymousInnerClass11() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "class A {" +
            // field initializer with anon. inner argument:
            "  C cee = new C(1, '2', new Runnable() {\n" +
            "    void run() {\n" +
            "      println 'hi!'\n" +
            "    }\n" +
            "  })\n" +
            "  static main(args) {\n" +
            "    new A()\n" +
            "  }\n" +
            "}\n",

            "C.groovy",
            "class C {\n" +
            "  C(int one, String two, Runnable three) {\n" +
            "    three.run()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "hi!");

        checkGCUDeclaration("A.groovy",
            "public class A {\n" +
            "  private C cee = (C) (java.lang.Object) new Runnable() {\n" +
            "  x() {\n" +
            "    super();\n" +
            "  }\n" +
            "  public void run() {\n" +
            "  }\n" +
            "};\n" +
            "  public @groovy.transform.Generated A() {\n" +
            "  }\n" +
            "  public static void main(java.lang.String... args) {\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testAnonymousInnerClass11a() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "class A {" +
            // field initializer with anon. inner argument:
            "  C cee = newC(1, '2', new Runnable() {\n" +
            "    void run() {\n" +
            "    }\n" +
            "  })\n" +
            "  static C newC(int one, String two, Runnable three) {\n" +
            "    new C()\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    new A()\n" +
            "  }\n" +
            "}\n",

            "C.groovy",
            "class C {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        checkGCUDeclaration("A.groovy",
            "public class A {\n" +
            "  private C cee = (C) (java.lang.Object) new Runnable() {\n" +
            "  x() {\n" +
            "    super();\n" +
            "  }\n" +
            "  public void run() {\n" +
            "  }\n" +
            "};\n" +
            "  public @groovy.transform.Generated A() {\n" +
            "  }\n" +
            "  public static C newC(int one, String two, Runnable three) {\n" +
            "  }\n" +
            "  public static void main(java.lang.String... args) {\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testAnonymousInnerClass11b() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "class A {" +
            // field initializer with anon. inner argument:
            "  C cee = newC().one(1).two('2').three(new Runnable() {\n" +
            "    void run() {\n" +
            "    }\n" +
            "  })\n" +
            "  static C newC() {\n" +
            "    new C()\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    new A()\n" +
            "  }\n" +
            "}\n",

            "C.groovy",
            "class C {\n" +
            "  C one(int i) {\n" +
            "    this\n" +
            "  }\n" +
            "  C two(String s) {\n" +
            "    this\n" +
            "  }\n" +
            "  C three(Runnable r) {\n" +
            "    this\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        checkGCUDeclaration("A.groovy",
            "public class A {\n" +
            "  private C cee = (C) (java.lang.Object) new Runnable() {\n" +
            "  x() {\n" +
            "    super();\n" +
            "  }\n" +
            "  public void run() {\n" +
            "  }\n" +
            "};\n" +
            "  public @groovy.transform.Generated A() {\n" +
            "  }\n" +
            "  public static C newC() {\n" +
            "  }\n" +
            "  public static void main(java.lang.String... args) {\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testAnonymousInnerClass12() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "class A {" +
            "  static main(args) {\n" +
            // local initializer with anon. inner argument:
            "    C cee = new C(1, '2', new Runnable() {\n" +
            "      void run() {\n" +
            "        println 'hi!'\n" +
            "      }\n" +
            "    })\n" +
            "  }\n" +
            "}\n",

            "C.groovy",
            "class C {\n" +
            "  C(int one, String two, Runnable three) {\n" +
            "    three.run()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "hi!");

        checkGCUDeclaration("A.groovy",
            "public class A {\n" +
            "  public @groovy.transform.Generated A() {\n" +
            "  }\n" +
            "  public static void main(java.lang.String... args) {\n" +
            "    C cee;\n" +
            "    new Runnable() {\n" +
            "      x() {\n" +
            "        super();\n" +
            "      }\n" +
            "      public void run() {\n" +
            "      }\n" +
            "    };\n" +
            "  }\n" +
            "}\n");
    }

    @Test
    public void testAnonymousInnerClass13() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C {\n" +
            "  int count\n" +
            "  @SuppressWarnings('rawtypes')\n" +
            "  static def m() {\n" +
            "    new LinkedList() {\n" +
            "      @Override\n" +
            "      def get(int i) {\n" +
            "        count += 1\n" +
            "        super.get(i)\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in C.groovy (at line 8)\n" +
            "\tcount += 1\n" +
            "\t^^^^^\n" +
            "Groovy:Apparent variable 'count' was found in a static scope but doesn't refer to a local variable, static field or class.\n" +
            "----------\n");
    }

    @Test
    public void testAnonymousInnerClass14() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C {\n" +
            "  static int count\n" +
            "  @SuppressWarnings('rawtypes')\n" +
            "  static def m() {\n" +
            "    new LinkedList() {\n" +
            "      @Override\n" +
            "      def get(int i) {\n" +
            "        count += 1\n" +
            "        super.get(i)\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testAnonymousInnerClass14a() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C {\n" +
            "  static int count\n" +
            "  @SuppressWarnings('rawtypes')\n" +
            "  static def m() {\n" +
            "    new LinkedList() {\n" +
            "      @Override\n" +
            "      def get(int i) {\n" +
            "        setCount(getCount() + 1)\n" +
            "        super.get(i)\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testAnonymousInnerClass15() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C {\n" +
            "  @SuppressWarnings('rawtypes')\n" +
            "  static def m() {\n" +
            "    int count = 0\n" +
            "    new LinkedList() {\n" +
            "      @Override\n" +
            "      def get(int i) {\n" +
            "        count += 1\n" +
            "        super.get(i)\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testAnonymousInnerClass16() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C {\n" +
            "  @SuppressWarnings('rawtypes')\n" +
            "  static def m(int count) {\n" +
            "    new LinkedList() {\n" +
            "      @Override\n" +
            "      def get(int i) {\n" +
            "        count += 1\n" +
            "        super.get(i)\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-5961
    public void testAnonymousInnerClass17() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@SuppressWarnings('rawtypes')\n" +
            "static def m() {\n" +
            "  new LinkedList() {\n" +
            "    int count\n" +
            "    @Override\n" +
            "    def get(int i) {\n" +
            "      count += 1\n" + // Apparent variable 'count' was found in a static scope but doesn't refer to a local variable, static field or class.
            "      super.get(i)\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-5961
    public void testAnonymousInnerClass17a() {
        //@formatter:off
        String[] sources = {
            "Abstract.groovy",
            "abstract class Abstract {\n" +
            "  abstract def find(key)\n" +
            "  @SuppressWarnings('rawtypes')\n" +
            "  protected Map map = [:]\n" +
            "}\n",

            "Script.groovy",
            "static def m() {\n" +
            "  def anon = new Abstract() {\n" +
            "    @Override\n" +
            "    def find(key) {\n" +
            "      map.get(key)\n" + // Apparent variable 'map' was found in a static scope but doesn't refer to a local variable, static field or class.
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-4035
    public void testAnonymousInnerClass18() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class C {\n" +
            "  def m(p) {\n" +
            "    p\n" +
            "  }\n" +
            "}\n" +
            "def aic = new C() {\n" +
            "  def m(Object p) {\n" +
            "    super.m(p)\n" +
            "  }\n" +
            "}\n" +
            "print aic.m('x')\n",
        };
        //@formatter:on

        runConformTest(sources, "x");
    }

    @Test
    public void testAnonymousInnerClass19() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "print Main.bar()\n" +
            "print ' '\n" +
            "Main.foo = 2\n" +
            "print Main.bar()\n",

            "Main.groovy",
            "class Main {\n" +
            "  static foo = 1\n" +
            "  static bar() {\n" +
            "    def impl = new java.util.function.Supplier() {\n" +
            "      @Override def get() {\n" +
            "        return foo\n" +
            "      }\n" +
            "    }\n" +
            "    return impl.get()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "1 2");
    }

    @Test
    public void testAnonymousInnerClass20() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "def bar = new Bar()\n" +
            "print bar.baz()\n",

            "Types.groovy",
            "class Foo {\n" +
            "  static baz() {\n" +
            "    def impl = new java.util.function.Supplier() {\n" +
            "      @Override def get() {\n" +
            "        return x()\n" +
            "      }\n" +
            "    }\n" +
            "    return impl.get()\n" +
            "  }\n" +
            "  private static def x() { 'foo' }\n" +
            "}\n" +
            "class Bar extends Foo {\n" +
            "  private static def x() { 'bar' }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "foo");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-6809
    public void testAnonymousInnerClass21() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  static main(args) {\n" +
            "    this.newInstance()\n" +
            "  }\n" +
            "  Main() {\n" +
            "    this(new Runnable() {\n" +
            "      @Override void run() {\n" +
            "        print 'works'\n" +
            "      }\n" +
            "    })\n" +
            "  }\n" +
            "  Main(Runnable action) {\n" +
            "    action.run()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-9168
    public void testAnonymousInnerClass22() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  static main(args) {\n" +
            "    this.newInstance()\n" +
            "  }\n" +
            // default argument with anon. inner initializer:
            "  Main(Runnable action = new Runnable() {\n" +
            "      @Override void run() {\n" +
            "        print 'works'\n" +
            "      }\n" +
            "    }\n" +
            "  ) {\n" +
            "    action.run()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-6809
    public void testAnonymousInnerClass23() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  static main(args) {\n" +
            "    this.newInstance()\n" +
            "  }\n" +
            "  static String getResult() {\n" +
            "    'works'\n" +
            "  }\n" +
            "  Main() {\n" +
            "    this(new Runnable() {\n" +
            "      @Override void run() {\n" +
            "        print getResult()\n" + // should be able to access static member of enclosing type
            "      }\n" +
            "    })\n" +
            "  }\n" +
            "  Main(Runnable action) {\n" +
            "    action.run()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-6809
    public void testAnonymousInnerClass23a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  static main(args) {\n" +
            "    this.newInstance()\n" +
            "  }\n" +
            "  static String getResult() {\n" +
            "    'works'\n" +
            "  }\n" +
            "  Main() {\n" +
            "    this(new Runnable() {\n" +
            "      @Override void run() {\n" +
            "        print result\n" + // should be able to access static member of enclosing type
            "      }\n" +
            "    })\n" +
            "  }\n" +
            "  Main(Runnable action) {\n" +
            "    action.run()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/924
    public void testAnonymousInnerClass24() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            // default argument with anon. inner initializer:
            "void meth(Runnable action = new Runnable() {\n" +
            "  @Override void run() {\n" +
            "    print 'works'\n" +
            "  }\n" +
            "}) {\n" +
            "  action.run()\n" +
            "}\n" +
            "meth()\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-9168
    public void testAnonymousInnerClass24a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  static main(args) {\n" +
            "    this.meth()\n" +
            "  }\n" +
            // default argument with anon. inner initializer:
            "  static void meth(Runnable action = new Runnable() {\n" +
            "    @Override void run() {\n" +
            "      print 'works'\n" +
            "    }\n" +
            "  }) {\n" +
            "    action.run()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-9168
    public void testAnonymousInnerClass24b() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            // default argument with anon. inner initializer:
            "static void meth(Runnable action = new Runnable() {\n" +
            "  @Override void run() {\n" +
            "    print 'works'\n" +
            "  }\n" +
            "}) {\n" +
            "  action.run()\n" +
            "}\n" +
            "meth()\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testAnonymousInnerClass25() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Foo {\n" +
            "  String string = 'foo'\n" +
            "}\n" +
            "new Foo().with {\n" +
            // anon. inner has static scoping
            "  def bar = new Object() {\n" +
            "    String string = 'bar'\n" +
            "    @Override\n" +
            "    String toString() { return string }\n" +
            "  }\n" +
            "  print bar\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "bar");
    }

    @Test
    public void testAnonymousInnerClass25a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Foo {\n" +
            "  String string = 'foo'\n" +
            "}\n" +
            "new Foo().with {\n" +
            // anon. inner has static scoping
            "  def bar = new Object() {\n" +
            "    @Override\n" +
            "    String toString() { return string }\n" +
            "  }\n" +
            "  print bar\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "", "groovy.lang.MissingPropertyException: No such property: string for class: Script");
    }

    @Test
    public void testAnonymousInnerClass26() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C {\n" +
            "  interface I {\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    def var = args\n" +
            "    new I() {\n" +
            "      def prop = var\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "");
    }

    @Test
    public void testAnonymousInnerClass26a() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C {\n" +
            "  interface I {\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    new I() {\n" +
            "      def prop = args\n" + // MissingPropertyException: No such property: args for class: C
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-9120
    public void testAnonymousInnerClass27() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import java.util.concurrent.Callable\n" +
            "\n" +
            "interface Face {\n" +
            "  Runnable runnable()\n" +
            "  Callable<Long> callable()\n" +
            "}\n" +
            "\n" +
            "static Face make() {\n" +
            "  final long number = 42\n" +
            "  return new Face() {\n" +
            "    @Override\n" +
            "    Runnable runnable() {\n" +
            "      return { ->\n" +
            "        print \"${number}\"\n" +
            "      }\n" +
            "    }\n" +
            "    @Override\n" +
            "    Callable<Long> callable() {\n" +
            "      return { -> number }\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "def face = make()\n" +
            "face.runnable().run()\n" +
            "print \"${face.callable().call()}\"\n",
        };
        //@formatter:on

        runConformTest(sources, "4242");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-9642
    public void testAnonymousInnerClass28() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class C {\n" +
            "  interface I {}\n" +
            "  static class T {}\n" +
            "}\n" +
            "class D extends C {\n" +
            "  static I one() {\n" +
            "    new I() {}\n" +
            "  }\n" +
            "  static T two() {\n" +
            "    new T() {}\n" +
            "  }\n" +
            "}\n" +
            "print D.one().getClass()\n" +
            "print ';'\n" +
            "print D.two().getClass()\n",
        };
        //@formatter:on

        runConformTest(sources, "class D$1;class D$2");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-5728
    public void testAnonymousInnerClass29() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "abstract class A {\n" +
            "  private A() { }\n" +
            "  abstract answer()\n" +
            "  static A create() {\n" +
            "    return new A() {\n" + // IllegalAccessError when A$1 calls private constructor
            "      def answer() { 42 }\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "print A.create().answer()\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-7686
    public void testAnonymousInnerClass30() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "abstract class A {\n" +
            "  A() {\n" +
            "    m()\n" +
            "  }\n" +
            "  abstract void m()\n" +
            "}\n" +
            "void test() {\n" +
            "  def v = false\n" +
            "  def a = new A() {\n" +
            "    // run by super ctor\n" +
            "    @Override void m() {\n" +
            "      assert v != null\n" +
            "    }\n" +
            "  }\n" +
            "  v = true\n" +
            "  a.m()\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-8104
    public void testAnonymousInnerClass31() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class A {\n" +
            "  void foo() {\n" +
            "    C c = new C()\n" +
            "    ['1','2','3'].each {\n" +
            "      c.baz(it, new I() {\n" +
            "        void bar(Object o) {\n" +
            "           B b = new B()\n" + // Could not find matching constructor for: A$B(A$_foo_closure1)
            "           print \"$o:$b;\"\n" +
            "        }\n" +
            "      })\n" +
            "    }\n" +
            "  }\n" +
            "  class B {\n" +
            "    String toString() { getClass().getSimpleName() }\n" +
            "  }\n" +
            "}\n" +
            "class C {\n" +
            "  void baz(Object o, I i) {\n" +
            "    i.bar(o)\n" +
            "  }\n" +
            "}\n" +
            "interface I {\n" +
            "  void bar(Object o)\n" +
            "}\n" +
            "A a = new A()\n" +
            "a.foo()\n",
        };
        //@formatter:on

        runConformTest(sources, "1:B;2:B;3:B;");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1268
    public void testAnonymousInnerClass32() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class C {\n" +
            "  class D {\n" +
            "  }\n" +
            "  def obj = new Object() {\n" +
            "    @Override\n" +
            "    String toString() {\n" +
            "      return new Object() {\n" +
            "        @Override\n" +
            "        String toString() {\n" +
            "          new D()\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "new C().obj\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test // GROOVY-6977
    public void testAnonymousInnerClass33() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class C {\n" +
            "  def <T> List<T> foo() {\n" +
            "    new ArrayList<T>() {}\n" +
            "  }\n" +
            "}\n" +
            "def longList = new C().<Long>foo()\n" +
            "assert longList != null\n" +
            "assert longList.empty\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testMixedModeInnerProperties_GRE597() {
        //@formatter:off
        String[] sources = {
            "gr8/JointGroovy.groovy",
            "package gr8\n" +
            "\n" +
            "class JointGroovy {\n" +
            "StaticInner property\n" +
            "\n" +
            " static class StaticInner {\n" +
            "  NonStaticInner property2\n" +
            "\n" +
            "  class NonStaticInner {\n" +
            "    Closure property3 = {}\n" +
            "  }\n" +
            " }\n" +
            "}",

            "gr8/JointJava.java",
            "package gr8;\n" +
            "\n" +
            "import groovy.lang.Closure;\n" +
            "\n" +
            "public class JointJava {\n" +
            "    public void method() {\n" +
            "        Closure closure = new JointGroovy().getProperty().getProperty2().getProperty3();\n" +
            "    }\n" +
            "}",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testMixedModeInnerProperties2_GRE597() {
        //@formatter:off
        String[] sources = {
            "gr8/JointGroovy.groovy",
            "package gr8\n" +
            "\n" +
            "class JointGroovy {\n" +
            "StaticInner property\n" +
            "\n" +
            " }\n" +
            // now the inner is not an inner (like the previous test) but the property3 still is
            " class StaticInner {\n" +
            "  NonStaticInner property2\n" +
            "\n" +
            "  class NonStaticInner {\n" +
            "    Closure property3 = {}\n" +
            "  }\n" +
            "}",

            "gr8/JointJava.java",
            "package gr8;\n" +
            "\n" +
            "import groovy.lang.Closure;\n" +
            "\n" +
            "public class JointJava {\n" +
            "    public void method() {\n" +
            "        Closure closure = new JointGroovy().getProperty().getProperty2().getProperty3();\n" +
            "    }\n" +
            "}",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test // Ensures that the Point2D.Double reference is resolved in the context of X and not Y (if Y is used then the import isn't found)
    public void testMemberTypeResolution() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "import java.awt.geom.Point2D;\n" +
            "public class X {\n" +
            "  public void foo() {\n" +
            "    Object o = new Point2D.Double(p.x(),p.y());\n" +
            "  }\n" +
            "  public static void main(String[] argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/Y.groovy",
            "package p;\n" +
            "public class Y {\n" +
            "  public void foo() {\n" +
            "  }\n" +
            "  public static void main(String[] argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-9168
    public void testReferenceToUninitializedThis1() {
        //@formatter:off
        String[] sources = {
            "Outer.groovy",
            "class Outer {\n" +
            "  class Inner {\n" +
            "  }\n" +
            "  Outer(Inner inner) {\n" +
            "  }\n" +
            "  Outer() {\n" +
            "    this(new Inner())\n" + // "new Inner()" has implicit 'this' parameter
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Outer.groovy (at line 7)\n" +
            "\tthis(new Inner())\n" +
            "\t               ^\n" +
            "Groovy:Cannot reference 'this' before supertype constructor has been called.\n" +
            "----------\n");
    }

    @Test
    public void testReferenceToUninitializedThis1a() {
        //@formatter:off
        String[] sources = {
            "Outer.groovy",
            "class Outer {\n" +
            "  class Inner {\n" +
            "  }\n" +
            "  Outer(Inner inner = new Inner()) {\n" + // "new Inner()" has implicit 'this' parameter
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Outer.groovy (at line 4)\n" +
            "\tOuter(Inner inner = new Inner()) {\n" +
            "\t                              ^\n" +
            "Groovy:Cannot reference 'this' before supertype constructor has been called.\n" +
            "----------\n");
    }

    @Test
    public void testReferenceToUninitializedThis2() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C {\n" +
            "  C(foo = bar()) {\n" +
            "  }\n" +
            "  def bar() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in C.groovy (at line 2)\n" +
            "\tC(foo = bar()) {\n" +
            "\t        ^^^\n" +
            "Groovy:Cannot reference 'bar' before supertype constructor has been called.\n" +
            "----------\n");
    }

    @Test
    public void testReferenceToUninitializedThis2a() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C {\n" +
            "  C(foo = this.bar()) {\n" +
            "  }\n" +
            "  def bar() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in C.groovy (at line 2)\n" +
            "\tC(foo = this.bar()) {\n" +
            "\t        ^^^^\n" +
            "Groovy:Cannot reference 'this' before supertype constructor has been called.\n" +
            "----------\n");
    }

    @Test
    public void testReferenceToUninitializedThis2b() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C {\n" +
            "  C(foo = this.&bar) {\n" +
            "  }\n" +
            "  def bar() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in C.groovy (at line 2)\n" +
            "\tC(foo = this.&bar) {\n" +
            "\t        ^^^^\n" +
            "Groovy:Cannot reference 'this' before supertype constructor has been called.\n" +
            "----------\n");
    }

    @Test
    public void testReferenceToUninitializedThis3() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C {\n" +
            "  C(foo = bar) {\n" +
            "  }\n" +
            "  def bar\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in C.groovy (at line 2)\n" +
            "\tC(foo = bar) {\n" +
            "\t        ^^^\n" +
            "Groovy:Cannot reference 'bar' before supertype constructor has been called.\n" +
            "----------\n");
    }

    @Test
    public void testReferenceToUninitializedThis3a() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C {\n" +
            "  C(foo = this.bar) {\n" +
            "  }\n" +
            "  def bar\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in C.groovy (at line 2)\n" +
            "\tC(foo = this.bar) {\n" +
            "\t        ^^^^\n" +
            "Groovy:Cannot reference 'this' before supertype constructor has been called.\n" +
            "----------\n");
    }

    @Test
    public void testReferenceToUninitializedThis3b() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C {\n" +
            "  C(foo = this.@bar) {\n" +
            "  }\n" +
            "  private def bar\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in C.groovy (at line 2)\n" +
            "\tC(foo = this.@bar) {\n" +
            "\t        ^^^^\n" +
            "Groovy:Cannot reference 'this' before supertype constructor has been called.\n" +
            "----------\n");
    }

    @Test
    public void testAccessOuterClassMemberFromInnerClassConstructor1() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Super {\n" +
            "  String str\n" +
            "  Super(String s) { str = s }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" + // required?
            "class Outer {\n" +
            "  String a\n" +
            "  private class Inner extends Super {\n" +
            "    Inner() {\n" +
            "      super(getA())\n" + // here
            "    }\n" +
            "  }\n" +
            "  String test() { new Inner().str }\n" +
            "}\n" +
            "def o = new Outer(a:'ok')\n" +
            "print o.test()\n",
        };
        //@formatter:on

        runConformTest(sources, "ok");
    }

    @Test
    public void testAccessOuterClassMemberFromInnerClassConstructor1a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Super {\n" +
            "  String str\n" +
            "  Super(String s) { str = s }\n" +
            "}\n" +
            "class Outer {\n" +
            "  String a\n" +
            "  private class Inner extends Super {\n" +
            "    Inner() {\n" +
            "      super(Outer.this.getA())\n" + // here
            "    }\n" +
            "  }\n" +
            "  String test() { new Inner().str }\n" +
            "}\n" +
            "def o = new Outer(a:'ok')\n" +
            "print o.test()\n",
        };
        //@formatter:on

        runConformTest(sources, "ok");
    }

    @Test
    public void testAccessOuterClassMemberFromInnerClassConstructor2() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Super {\n" +
            "  String str\n" +
            "  Super(String s) { str = s }\n" +
            "}\n" +
            "class Outer {\n" +
            "  String a\n" +
            "  private class Inner extends Super {\n" +
            "    Inner() {\n" +
            "      super(a)\n" + // here
            "    }\n" +
            "  }\n" +
            "  String test() { new Inner().str }\n" +
            "}\n" +
            "def o = new Outer(a:'ok')\n" +
            "print o.test()\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Script.groovy (at line 9)\n" +
            "\tsuper(a)\n" +
            "\t      ^\n" +
            "Groovy:Apparent variable 'a' was found in a static scope but doesn't refer to a local variable, static field or class.\n" +
            "----------\n");
    }

    @Test
    public void testAccessOuterClassMemberFromInnerClassConstructor2a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Super {\n" +
            "  String str\n" +
            "  Super(String s) { str = s }\n" +
            "}\n" +
            "class Outer {\n" +
            "  String a\n" +
            "  private class Inner extends Super {\n" +
            "    Inner() {\n" +
            "      super(Outer.this.a)\n" + // here
            "    }\n" +
            "  }\n" +
            "  String test() { new Inner().str }\n" +
            "}\n" +
            "def o = new Outer(a:'ok')\n" +
            "print o.test()\n",
        };
        //@formatter:on

        runConformTest(sources, "ok");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-5259
    public void testAccessOuterClassMemberFromInnerClassConstructor3() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Super {\n" +
            "  String str\n" +
            "  Super(String s) { str = s }\n" +
            "}\n" +
            "class Outer {\n" +
            "  static final String OUTER_CONSTANT = 'ok'\n" +
            "  private class Inner extends Super {\n" +
            "    Inner() {\n" +
            "      super(OUTER_CONSTANT)\n" + // here
            "    }\n" +
            "  }\n" +
            "  String test() { new Inner().str }\n" +
            "}\n" +
            "print new Outer().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "ok");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-9501
    public void testAccessOuterClassMemberFromInnerClassMethod1() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main extends Outer {\n" +
            "    static main(args) {\n" +
            "        newInstance().newThread()\n" +
            "        assert Outer.Inner.error == null\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "abstract class Outer {\n" +
            "    private static volatile boolean flag\n" +
            "\n" +
            "    void newThread() {\n" +
            "        Thread thread = new Inner()\n" +
            "        thread.start()\n" +
            "        thread.join()\n" +
            "    }\n" +
            "\n" +
            "    private final class Inner extends Thread {\n" +
            "        @Override\n" +
            "        void run() {\n" +
            "            try {\n" +
            "                if (!flag) {\n" +
            "                    print 'works'\n" +
            "                }\n" +
            "            } catch (e) {\n" +
            "                error = e\n" +
            "            }\n" +
            "        }\n" +
            "        public static error\n" +
            "    }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test // inner class is static instead of final
    public void testAccessOuterClassMemberFromInnerClassMethod2() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main extends Outer {\n" +
            "    static main(args) {\n" +
            "        newInstance().newThread()\n" +
            "        assert Outer.Inner.error == null\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "abstract class Outer {\n" +
            "    private static volatile boolean flag\n" +
            "\n" +
            "    void newThread() {\n" +
            "        Thread thread = new Inner()\n" +
            "        thread.start()\n" +
            "        thread.join()\n" +
            "    }\n" +
            "\n" +
            "    private static class Inner extends Thread {\n" +
            "        @Override\n" +
            "        void run() {\n" +
            "            try {\n" +
            "                if (!flag) {\n" +
            "                    print 'works'\n" +
            "                }\n" +
            "            } catch (e) {\n" +
            "                error = e\n" +
            "            }\n" +
            "        }\n" +
            "        public static error\n" +
            "    }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-9569
    public void testAccessOuterClassMemberFromInnerClassMethod3() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main extends Outer {\n" +
            "    static main(args) {\n" +
            "        newInstance().newThread()\n" +
            "        assert Outer.Inner.error == null\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "abstract class Outer {\n" +
            "    private static volatile boolean flag\n" +
            "\n" +
            "    void newThread() {\n" +
            "        Thread thread = new Inner()\n" +
            "        thread.start()\n" +
            "        thread.join()\n" +
            "    }\n" +
            "\n" +
            "    @groovy.transform.CompileStatic\n" +
            "    private static class Inner extends Thread {\n" +
            "        @Override\n" +
            "        void run() {\n" +
            "            try {\n" +
            "                if (!flag) {\n" +
            "                    print 'works'\n" +
            "                }\n" +
            "            } catch (e) {\n" +
            "                error = e\n" +
            "            }\n" +
            "        }\n" +
            "        public static error\n" +
            "    }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testAccessOuterClassMemberFromInnerClassMethod4() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "interface I {\n" +
            "  String CONST = 'value'\n" +
            "}\n" +
            "class C implements I {\n" +
            "  static class D {\n" +
            "    void test() {\n" +
            "      print CONST\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "new C.D().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test // // https://issues.apache.org/jira/browse/GROOVY-9905
    public void testAccessOuterClassMemberFromInnerClassMethod5() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "abstract class A {\n" +
            "  protected final f = 'x'\n" +
            "  abstract static class B {}\n" +
            "}\n" +
            "class C extends A {\n" +
            "  private class D extends A.B {\n" + // B is static inner
            "    String toString() {\n" +
            "      println(f)\n" + // No such property: f for class: A
            "      return 'y'\n" +
            "    }\n" +
            "  }\n" +
            "  def m() {\n" +
            "    new D().toString()\n" +
            "  }\n" +
            "}\n" +
            "new C().m()\n",
        };
        //@formatter:on

        runConformTest(sources, "x");
    }
}
