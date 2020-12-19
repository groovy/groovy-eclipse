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
            "Main.groovy",
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
            "1. ERROR in Main.groovy (at line 4)\n" +
            "\tif (rareCondition) {\n" +
            "\t    ^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - The variable [rareCondition] is undeclared.\n" +
            "----------\n" +
            "2. ERROR in Main.groovy (at line 5)\n" +
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
            "Main.groovy",
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
            "1. ERROR in Main.groovy (at line 6)\n" +
            "\tls.add(\'abc\')\n" +
            "\t^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching method java.util.ArrayList#add(java.lang.String). Please check if the declared type is correct and if the method exists.\n" +
            "----------\n");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-9412
    public void testTypeChecked3() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
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
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class Main {" +
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
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class Main {" +
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
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class Main {\n" +
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
            "Main.groovy",
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
            "Main.groovy",
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
            "Main.groovy",
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

    @Test
    public void testTypeChecked6882() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class B {\n" +
            "  void m() {\n" +
            "    print 'B'\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "class C extends B {\n" +
            "  @Override\n" +
            "  void m() {\n" +
            "    print 'C'\n" +
            "  }\n" +
            "  void test() {\n" +
            "    def x = new Runnable() {\n" +
            "      @Override\n" +
            "      void run() {\n" +
            "        m()\n" + // Reference to method is ambiguous. Cannot choose between [void C#m(), void B#m()]
            "      }\n" +
            "    }\n" +
            "    x.run()\n" +
            "    m()\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "CC");
    }

    @Test
    public void testTypeChecked7333() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "int len(byte[] bytes) { bytes.length }\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test(arg) {\n" +
            "  if (arg instanceof byte[]) {\n" +
            "    print(len(arg))\n" +
            "  }\n" +
            "}\n" +
            "test(new byte[3])\n",
        };
        //@formatter:on

        runConformTest(sources, "3");
    }

    @Test // don't match bridge method
    public void testTypeChecked7363() {
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

    @Test
    public void testTypeChecked8103() {
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

    @Test
    public void testTypeChecked9460() {
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

    @Test
    public void testTypeChecked9570() {
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

    @Test
    public void testTypeChecked9735() {
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

    @Test
    public void testTypeChecked9735a() {
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

    @Test
    public void testTypeChecked9751() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "interface Service {\n" +
            "  Number transform(String s)\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test(Service service) {\n" +
            "  Set<Number> numbers = []\n" +
            "  List<String> strings = ['x','y','z']\n" +
            "  numbers.addAll(strings.collect(service.&transform))\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testTypeChecked9762() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "static <T> List<T> list(T item) {\n" +
            "  Collections.singletonList(item)\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  Optional<Integer> opt = Optional.ofNullable(123)\n" +
            "  List<Integer> result = opt.map(this.&list).get()\n" +
            "  print result\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "[123]");
    }

    @Test // see GROOVY-9783 for Groovy 4
    public void testTypeChecked9803() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  def c = C.of(123)\n" +
            "  def d = c.map(D.&wrap)\n" +
            "  def e = d.map{x -> x.first().intValue()}\n" +
            "  print e.t\n" +
            "}\n" +
            "test()\n",

            "Types.groovy",
            "class C<T> {\n" +
            "  private T t\n" +
            "  C(T item) {\n" +
            "    t = item\n" +
            "  }\n" +
            "  static <U> C<U> of(U item) {\n" +
            "    new C<U>(item)\n" +
            "  }\n" +
            "  def <V> C<V> map(F<? super T, ? super V> func) {\n" +
            "    new C<V>(func.apply(t))\n" +
            "  }\n" +
            "}\n" +
            "class D {\n" +
            "  static <W> Set<W> wrap(W o) {\n" +
            "    Collections.singleton(o)\n" +
            "  }\n" +
            "}\n" +
            "interface F<X,Y> {\n" +
            "  Y apply(X x)\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "123");
    }

    @Test
    public void testTypeChecked9821() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "def test(A a) {\n" +
            "  a.bees*.c\n" +
            "}\n",

            "Types.java",
            "interface A {\n" +
            "  java.util.Collection<? extends B> getBees();\n" +
            "}\n" +
            "interface B {\n" +
            "  Object getC();\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testTypeChecked9821a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "def test(A a) {\n" +
            "  a.bees.c\n" +
            "}\n",

            "Types.java",
            "interface A {\n" +
            "  java.util.List<? extends B> getBees();\n" +
            "}\n" +
            "interface B {\n" +
            "  Object getC();\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testTypeChecked9822() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "GraphTraversalSource test(Graph graph) {\n" +
            "  def strategy = ReadOnlyStrategy.instance()\n" +
            "  graph.traversal().withStrategies(strategy)\n" +
            "}\n",

            "Types.java", // from org.apache.tinkerpop:gremlin-core:3.4.8
            "@SuppressWarnings(\"rawtypes\")\n" +
            "interface TraversalStrategy<S extends TraversalStrategy> extends Comparable<Class<? extends TraversalStrategy>> {\n" +
            "  interface VerificationStrategy extends TraversalStrategy<VerificationStrategy> {\n" +
            "  }\n" +
            "}\n" +
            "@SuppressWarnings(\"rawtypes\")\n" +
            "abstract class AbstractTraversalStrategy<S extends TraversalStrategy> implements TraversalStrategy<S> {\n" +
            "}\n" +
            "abstract\n" + // don't want to implement Comparable
            "class ReadOnlyStrategy extends AbstractTraversalStrategy<TraversalStrategy.VerificationStrategy>\n" +
            "    implements TraversalStrategy.VerificationStrategy {\n" +
            "  static ReadOnlyStrategy instance() { return null; }\n" +
            "}\n" +
            "interface TraversalSource extends Cloneable, AutoCloseable {\n" +
            "  @SuppressWarnings(\"rawtypes\")\n" +
            "  default TraversalSource withStrategies(TraversalStrategy... strategies) {\n" +
            "    return null;\n" +
            "  }\n" +
            "}\n" +
            "abstract\n" + // don't want to implement AutoCloseable
            "class GraphTraversalSource implements TraversalSource {\n" +
            "  @Override\n" +
            "  @SuppressWarnings(\"rawtypes\")\n" +
            "  public GraphTraversalSource withStrategies(TraversalStrategy... strategies) {\n" +
            "    return (GraphTraversalSource) TraversalSource.super.withStrategies(strategies);\n" +
            "  }\n" +
            "}\n" +
            "class Graph {\n" +
            "  public <C extends TraversalSource> C traversal(Class<C> c) {\n" +
            "    return null;\n" +
            "  }\n" +
            "  public GraphTraversalSource traversal() {\n" +
            "    return null;\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }
}
