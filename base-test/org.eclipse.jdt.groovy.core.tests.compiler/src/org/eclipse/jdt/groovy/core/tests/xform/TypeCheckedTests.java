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
package org.eclipse.jdt.groovy.core.tests.xform;

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.junit.Test;

/**
 * Test cases for {@link groovy.transform.TypeChecked}.
 */
public final class TypeCheckedTests extends GroovyCompilerTestSuite {

    @Test
    public void testTypeChecked1() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "import groovy.transform.TypeChecked\n"+
            "@TypeChecked\n"+
            "void method(String message) {\n"+
            "  if (rareCondition) {\n"+
            "    println \"Did you spot the error in this ${message.toUppercase()}?\"\n"+
            "  }\n"+
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 4)\n" +
            "\tif (rareCondition) {\n" +
            "\t    ^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - The variable [rareCondition] is undeclared.\n" +
            "----------\n" +
            "2. ERROR in Foo.groovy (at line 5)\n" +
            "\tprintln \"Did you spot the error in this ${message.toUppercase()}?\"\n" +
            "\t                                          ^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching method java.lang.String#toUppercase()." +
            " Please check if the declared type is correct and if the method exists.\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked2() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "import groovy.transform.TypeChecked\n" +
            "@TypeChecked\n" +
            "void method(String message) {\n" +
            "  List<Integer> ls = new ArrayList<Integer>()\n" +
            "  ls.add(123)\n" +
            "  ls.add('abc')\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 6)\n" +
            "\tls.add(\'abc\')\n" +
            "\t^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot call java.util.ArrayList <Integer>#add(java.lang.Integer) with arguments [java.lang.String] \n" +
            "----------\n");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-9412
    public void testTypeChecked3() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "interface I {\n" +
            "}\n" +
            "enum E implements I {\n" +
            "  X\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  List<I> list = []\n" +
            "  list.add(E.X)\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "");
    }

    @Test
    public void testTypeChecked4() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class Foo {" +
            "  def method() {\n" +
            "    Set<java.beans.BeanInfo> defs = []\n" +
            "    defs*.additionalBeanInfo\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testTypeChecked5() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class Foo {" +
            "  static def method() {\n" + // static method alters type checking
            "    Set<java.beans.BeanInfo> defs = []\n" +
            "    defs*.additionalBeanInfo\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testTypeChecked6() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class Foo {\n" +
            "  private Closure<String> normalizer\n" +
            "  String normalize(String s) {\n" +
            "    normalizer(s)" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testTypeChecked7() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "class C {\n" +
            "  C(String s, Comparable<List<Integer>> c) {\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  new C('blah', { list -> list.get(0) })\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testTypeChecked8() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "import groovy.transform.stc.*\n" +
            "class C {\n" +
            "  C(String s, @ClosureParams(value=SimpleType, options='java.util.List') Closure<Integer> c) {\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  new C('blah', { list -> list.get(0) })\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testTypeChecked9() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "class C {\n" +
            "  static m(String s, Comparable<List<Integer>> c) {\n" +
            "  }\n" +
            "  @groovy.transform.TypeChecked\n" +
            "  static test() {\n" +
            "    m('blah', { list -> list.get(0) })\n" +
            "  }\n" +
            "}\n" +
            "C.test()\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test // GROOVY-7363: don't match bridge method
    public void testTypeChecked10() {
        //@formatter:off
        String[] sources = {
            "Face.java",
            "public interface Face<T> {\n" +
            "  T getItem();\n" +
            "}\n",

            "Impl.groovy",
            "class Impl implements Face<Pogo> {\n" +
            "  Pogo getItem() { new Pogo() }\n" +
            "}\n",

            "Pogo.groovy",
            "class Pogo {\n" +
            "  def prop\n" +
            "}\n",

            "Test.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test(Impl impl) {\n" +
            "  impl.item.prop\n" + // typeof(impl.item) is Pogo not T
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test // GROOVY-8103
    public void testTypeChecked11() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import Util.Ours\n" +
            "import static Fluent.*\n" +
            "@groovy.transform.TypeChecked\n" +
            "def method() {\n" +
            "  fluent('string').isEqualTo('x')\n" + // fine
            "  fluent(new Ours()).isSimilarTo('')\n" + // fine
            "  fluent(Util.factory('{}')).isSimilarTo('{\"key\":\"val\"}')\n" + // STC error
            "}\n",

            "API.groovy",
            "class Fluent {\n" +
            "  static FluentAPI  fluent(String s) { return new FluentAPI() }\n" +
            "  static <T extends FluentExtension> T fluent(T t) { return t }\n" +
            "}\n" +
            "class FluentAPI {\n" +
            "  FluentAPI isEqualTo(String s) { return this }\n" +
            "}\n" +
            "interface FluentExtension {\n" +
            "}\n",

            "Util.groovy",
            "class Util {\n" +
            "  static class Ours implements FluentExtension {\n" +
            "      Ours isSimilarTo(String json) { return this }\n" +
            "  }\n" +
            "  static Ours factory(String json) { new Ours() }\n" +
            "}",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test // GROOVY-9460
    public void testTypeChecked12() {
        //@formatter:off
        String[] sources = {
            "G.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class G<D> {\n" +
            "  void test(Class<D> c) {\n" +
            "    J.m(c)\n" +
            "  }\n" +
            "}\n",

            "J.java",
            "public class J {\n" +
            "  public static void m(Class<?> target) {\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test // GROOVY-9570
    public void testTypeChecked13() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class C<I extends Item> {\n" +
            "  Queue<I> queue\n" +
            "  def c = { ->\n" +
            "    queue.each { I item ->\n" +
            "      println item\n" +
            "    }\n" +
            "  }\n" +
            "  def m() {\n" +
            "    queue.each { I item ->\n" +
            "      println item\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "interface Item {}\n" +
            "new C()\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test // GROOVY-9735
    public void testTypeChecked14() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.stc.*\n" +
            "@groovy.transform.TypeChecked\n" +
            "class C<I extends Item> {\n" +
            "  Queue<I> queue\n" +
            "  def c = { ->\n" +
            "    x(queue) { I item ->\n" +
            "      println item\n" +
            "    }\n" +
            "  }\n" +
            "  def m() {\n" +
            "    x(queue) { I item ->\n" +
            "      println item\n" +
            "    }\n" +
            "  }\n" +
            "  def <T> T x(Collection<T> y, @ClosureParams(FirstParam.FirstGenericType) Closure<?> z) {\n" +
            "  }\n" +
            "}\n" +
            "interface Item {}\n" +
            "new C()\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test // GROOVY-9735
    public void testTypeChecked15() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.stc.*\n" +
            "@groovy.transform.TypeChecked\n" +
            "class C<I extends Item> {\n" +
            "  Queue<I> queue\n" +
            "  def c = { ->\n" +
            "    x(queue) { I item ->\n" +
            "      println item\n" +
            "    }\n" +
            "  }\n" +
            "  def m() {\n" +
            "    x(queue) { I item ->\n" +
            "      println item\n" +
            "    }\n" +
            "  }\n" + // method is static:
            "  static <T> T x(Collection<T> y, @ClosureParams(FirstParam.FirstGenericType) Closure<?> z) {\n" +
            "  }\n" +
            "}\n" +
            "interface Item {}\n" +
            "new C()\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }
}
