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
package org.eclipse.jdt.groovy.core.tests.xform;

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isParrotParser;
import static org.junit.Assume.assumeTrue;

import java.util.Map;

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.junit.Ignore;
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

        runConformTest(sources);
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
    public void testTypeChecked10() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  def str = \"${}\"\n" +
            "  assert str != 'x'\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked6232() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C<T> {\n" +
            "  C(T x, T y) {\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  C<Object> c = new C<>('a', new Object())\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked6786() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C<X> {\n" +
            "  Container<X> container\n" +
            "  @groovy.transform.TypeChecked\n" +
            "  void refresh() {\n" +
            "    def items = findAllItems()\n" +
            "    container.addAll(items)\n" + // Cannot call Container#addAll(java.util.Collection<? extends X>) with arguments [java.util.Collection<X>]
            "  }\n" +
            "  Collection<X> findAllItems() {\n" +
            "  }\n" +
            "}\n" +
            "interface Container<Y> {\n" +
            "  void addAll(Collection<? extends Y> collection)\n" +
            "}\n" +
            "new C()\n",
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
    public void testTypeChecked6912() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  def a = []\n" +
            "  assert a instanceof List\n" +
            "  List b = []\n" +
            "  assert b instanceof List\n" +
            "  Object c = []\n" +
            "  assert c instanceof List\n" +
            "  Iterable d = []\n" +
            "  assert d instanceof Iterable\n" +
            "  ArrayList e = []\n" +
            "  assert e instanceof ArrayList\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked6912a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  Set a = []\n" +
            "  assert a instanceof Set\n" +
            "  HashSet b = []\n" +
            "  assert b instanceof HashSet\n" +
            "  LinkedHashSet c = []\n" +
            "  assert c instanceof LinkedHashSet\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked6938() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.ASTTest\n" +
            "import groovy.transform.TypeChecked\n" +
            "import org.codehaus.groovy.ast.expr.MethodCallExpression\n" +
            "import static org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_TYPE\n" +

            "@TypeChecked\n" +
            "class G extends J<Integer> {\n" +
            "  Integer doSomething() {\n" +
            "    @ASTTest(phase=INSTRUCTION_SELECTION, value={\n" +
            "      def expr = node.rightExpression\n" +
            "      assert expr instanceof MethodCallExpression\n" +
            "      assert expr.objectExpression.text == 'super'\n" +

            "      def type = expr.objectExpression.getNodeMetaData(INFERRED_TYPE)\n" +
            "      assert type.toString(false) == 'J<java.lang.Integer>'\n" + // was "J<T>"
            "      type = node.leftExpression.getNodeMetaData(INFERRED_TYPE)\n" +
            "      assert type.toString(false) == 'java.lang.Integer'\n" +
            "    })\n" +
            "    def result = super.doSomething()\n" +
            "    return result\n" +
            "  }\n" +
            "}\n" +
            "print new G().doSomething()\n",

            "J.java",
            "public class J <T extends Number> {\n" +
            "  public T doSomething() {\n" +
            "    return null;\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "null");
    }

    @Test
    public void testTypeChecked7106() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "void m(Map<String,Object> map) {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  ['x'].each {\n" +
            "    m([(it): it.toLowerCase()])\n" +
            "  }\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked7128() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  List<Number> list = [1,2,3]\n" +
            "  Set<Number> set = [1,2,3,3]\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked7128a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  def a = [:]\n" +
            "  assert a instanceof Map\n" +
            "  Map b = [:]\n" +
            "  assert b instanceof Map\n" +
            "  Object c = [:]\n" +
            "  assert c instanceof Map\n" +
            "  HashMap d = [:]\n" +
            "  assert d instanceof HashMap\n" +
            "  LinkedHashMap e = [:]\n" +
            "  assert e instanceof LinkedHashMap\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked7128b() {
        for (String spec : new String[] {"CharSequence,Integer", "String,Number", "CharSequence,Number"}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                "@groovy.transform.TypeChecked\n" +
                "void test() {\n" +
                "  Map<" + spec + "> map = [a:1,b:2,c:3]\n" +
                "  assert map.size() == 3\n" +
                "  assert map['c'] == 3\n" +
                "  assert !('x' in map)\n" +
                "}\n" +
                "test()\n",
            };
            //@formatter:on

            runConformTest(sources);
        }
    }

    @Test
    public void testTypeChecked7128c() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  Map<String,Integer> map = [1:2]\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 3)\n" +
            "\tMap<String,Integer> map = [1:2]\n" +
            "\t                          ^^^^^\n" +
            "Groovy:[Static type checking] - Incompatible generic argument types. Cannot assign java.util.LinkedHashMap<java.lang.Integer, java.lang.Integer> to: java.util.Map<java.lang.String, java.lang.Integer>\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked7128d() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class A<B,C> {\n" +
            "  int x\n" +
            "  int y\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  A<Number,String> a = [x:100, y:200]\n" +
            "  assert a.x == 100\n" +
            "  assert a.y == 200\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked7274() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "void m(Map<String,Object> map) {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  m([d:new Date(), i:1, s:''])\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
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
    public void testTypeChecked7945() {
        //@formatter:off
        String[] sources = {
            "Test.groovy",
            "abstract class A<X, Y> {\n" +
            "  private final Class<X> x\n" +
            "  private final Class<Y> y\n" +
            "  A(Class<X> x, Class<Y> y) {\n" +
            "    this.x = x\n" +
            "    this.y = y\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "class C extends A<String, Integer> {\n" +
            "  C() {\n" +
            "    super(Integer, String)\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Test.groovy (at line 12)\n" +
            "\tsuper(Integer, String)\n" +
            "\t^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot call A#<init>(java.lang.Class<java.lang.String>, java.lang.Class<java.lang.Integer>) with arguments [java.lang.Class<java.lang.Integer>, java.lang.Class<java.lang.String>] \n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked8001() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C {\n" +
            "  Map<String,Object> map\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  int value = 42\n" +
            "  def c = new C()\n" +
            "  c.map = [key:\"$value\"]\n" +
            "  print c.map\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "[key:42]");
    }

    @Test
    public void testTypeChecked8034() {
        //@formatter:off
        String[] sources = {
            "Test.groovy",
            "class A<I, O> {\n" +
            "  def <IO extends A<? super O, ?>> IO andThen(IO next) {\n" +
            "    next\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  def a1 = new A<String , Integer>()\n" +
            "  def a2 = new A<Integer, Double >()\n" +
            "  def a3 = new A<Double , String >()\n" +
            "  def a4 = new A<String , Double >()\n" +
            "  def a5 = new A<Number , Object >()\n" +
            "  \n" +
            "  a1.andThen(a2)\n" +
            "  a2.andThen(a3)\n" +
            "  a3.andThen(a4)\n" +
            "  a4.andThen(a5)\n" +
            "  \n" +
            "  a1.andThen(a2)\n" +
            "    .andThen(a3)\n" +
            "    .andThen(a4)\n" +
            "    .andThen(a5)\n" +
            "}\n" +
            "test()\n",
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

            "Types.groovy",
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
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testTypeChecked8909() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "void m(List<Object> list) {\n" +
            "  assert list.size() == 3\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  m([1,2,3])\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked8909a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "void m(Set<Integer> set) {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  m([1,2,3])\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 5)\n" +
            "\tm([1,2,3])\n" +
            "\t^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching method Main#m(java.util.List<java.lang.Integer>). Please check if the declared type is correct and if the method exists.\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked8983() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "List<String> m() { ['foo'] }\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test(Set<String> set) {\n" +
            "  String[] one = m()\n" +
            "  String[] two = set\n" +
            "  print(one + two)\n" +
            "}\n" +
            "test(['bar'].toSet())\n",
        };
        //@formatter:on

        runConformTest(sources, "[foo, bar]");
    }

    @Test
    public void testTypeChecked8983a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "List<String> m() { ['foo'] }\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test(Set<String> set) {\n" +
            "  CharSequence[] one = m()\n" +
            "  CharSequence[] two = set\n" +
            "  print(one + two)\n" +
            "}\n" +
            "test(['bar'].toSet())\n",
        };
        //@formatter:on

        runConformTest(sources, "[foo, bar]");
    }

    @Test
    public void testTypeChecked8983b() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "List<String> m() { ['foo'] }\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test(Set<String> set) {\n" +
            "  Object[] one = m()\n" +
            "  Object[] two = set\n" +
            "  print(one + two)\n" +
            "}\n" +
            "test(['bar'].toSet())\n",
        };
        //@formatter:on

        runConformTest(sources, "[foo, bar]");
    }

    @Test
    public void testTypeChecked8983c() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "List<? extends CharSequence> m() { ['foo'] }\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test(Set<? extends CharSequence> set) {\n" +
            "  CharSequence[] one = m()\n" +
            "  CharSequence[] two = set\n" +
            "  print(one + two)\n" +
            "}\n" +
            "test(['bar'].toSet())\n",
        };
        //@formatter:on

        runConformTest(sources, "[foo, bar]");
    }

    @Test
    public void testTypeChecked8984() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "List<? super CharSequence> m() { [null] }\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test(Set<? super CharSequence> set) {\n" +
            "  CharSequence[] one = m()\n" +
            "  CharSequence[] two = set\n" +
            "}\n" +
            "test([null].toSet())\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 4)\n" +
            "\tCharSequence[] one = m()\n" +
            "\t                     ^^^\n" +
            "Groovy:[Static type checking] - Cannot assign value of type java.util.List<? super java.lang.CharSequence> to variable of type java.lang.CharSequence[]\n" +
            "----------\n" +
            "2. ERROR in Main.groovy (at line 5)\n" +
            "\tCharSequence[] two = set\n" +
            "\t                     ^^^\n" +
            "Groovy:[Static type checking] - Cannot assign value of type java.util.Set<? super java.lang.CharSequence> to variable of type java.lang.CharSequence[]\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked8984a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "List<String> m() { ['foo'] }\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test(Set<String> set) {\n" +
            "  Number[] one = m()\n" +
            "  Number[] two = set\n" +
            "}\n" +
            "test(['bar'].toSet())\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 4)\n" +
            "\tNumber[] one = m()\n" +
            "\t               ^^^\n" +
            "Groovy:[Static type checking] - Cannot assign value of type java.util.List<java.lang.String> to variable of type java.lang.Number[]\n" +
            "----------\n" +
            "2. ERROR in Main.groovy (at line 5)\n" +
            "\tNumber[] two = set\n" +
            "\t               ^^^\n" +
            "Groovy:[Static type checking] - Cannot assign value of type java.util.Set<java.lang.String> to variable of type java.lang.Number[]\n" +
            "----------\n");
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
    public void testTypeChecked9707() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  ast(groovy.transform.CompileStatic)\n" +
            "}\n"
        ).getAbsolutePath());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class C {\n" +
            "  def m() {\n" +
            "    'a' + 'b'\n" +
            "  }\n" +
            "}\n" +
            "print new C().m()\n",
        };
        //@formatter:on

        runConformTest(sources, "ab", options);
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

    @Test
    public void testTypeChecked9844() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "void m(Map<String, Object> map) {\n" +
            "  print map\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  Map<String, Object> map = [key: 'val']\n" +
            "  m([key: 'val'])\n" +
            "  m(key: 'val')\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "[key:val][key:val]");
    }

    @Ignore @Test
    public void testTypeChecked9873() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyFlags, Integer.toString(CompilerOptions.InvokeDynamic));

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

        runConformTest(sources, "123", options);
    }

    @Test
    public void testTypeChecked9891() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test(Pojo pojo) {\n" +
            "  Collection<? extends Number> c = pojo.map.values()\n" +
            "  Iterable<? extends Number> i = pojo.map.values()\n" + // Cannot assign Collection<? extends Number> to Iterable<? extends Number>
            "  print i.iterator().next()\n" +
            "}\n" +
            "test(new Pojo(map: [x:1,y:2,z:3.4]))\n",

            "Pojo.java",
            "import java.util.Map;\n" +
            "class Pojo {\n" +
            "  Map<String, ? extends Number> getMap() {\n" +
            "    return map;\n" +
            "  }\n" +
            "  void setMap(Map<String, ? extends Number> map) {\n" +
            "    this.map = map;\n" +
            "  }\n" +
            "  private Map<String, ? extends Number> map = null;\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "1");
    }

    @Test
    public void testTypeChecked9902() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Holder<Unknown> {\n" +
            "  TypedProperty<Number, Unknown> numberProperty = prop(Number)\n" +
            "  TypedProperty<String, Unknown> stringProperty = prop(String)\n" +
            "  def <T> TypedProperty<T, Unknown> prop(Class<T> clazz) {\n" +
            "    new TypedProperty<T, Unknown>(clazz: clazz)\n" +
            "  }\n" +
            // Note: type argument of Holder cannot be supplied to value attribute of @DelegatesTo
            "  def <T> T of(@DelegatesTo(value=Holder, strategy=Closure.DELEGATE_FIRST) Closure<T> c) {\n" +
            "    this.with(c)\n" +
            "  }\n" +
            "}\n" +
            "class TypedProperty<X, Y> {\n" +
            "  Class<X> clazz\n" +
            "  void eq(X x) {\n" +
            "    assert x.class == clazz : \"x.class is ${x.class} not ${clazz}\"\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test(Holder<Object> h) {\n" +
            "  h.stringProperty.eq(\"${0}\")\n" + // STC error
            "  h.of {\n" + // <-- 2nd type parameter discarded
            "    stringProperty.eq(1234)\n" + // expect STC error
            "    numberProperty.eq('xx')\n" + // expect STC error
            "  }\n" +
            "}\n" +
            "test(new Holder<Object>())\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 19)\n" +
            "\th.stringProperty.eq(\"${0}\")\n" +
            "\t^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot call TypedProperty#eq(java.lang.String) with arguments [groovy.lang.GString] \n" +
            "----------\n" +
            "2. ERROR in Main.groovy (at line 21)\n" +
            "\tstringProperty.eq(1234)\n" +
            "\t^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot call TypedProperty#eq(java.lang.String) with arguments [int] \n" +
            "----------\n" +
            "3. ERROR in Main.groovy (at line 22)\n" +
            "\tnumberProperty.eq('xx')\n" +
            "\t^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot call TypedProperty#eq(java.lang.Number) with arguments [java.lang.String] \n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked9903() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "def m(@DelegatesTo(strategy=Closure.TO_SELF) Closure<Object> c) {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "def x() {\n" +
            "  m {" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 1)\n" +
            "\tdef m(@DelegatesTo(strategy=Closure.TO_SELF) Closure<Object> c) {\n" +
            "\t      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:Not enough arguments found for a @DelegatesTo method call. Please check " +
            "that you either use an explicit class or @DelegatesTo.Target with a correct id\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked9907() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "Integer foo(x) {\n" +
            "  if (x instanceof Integer) {\n" +
            "    def bar = { -> return x }\n" +
            "    return bar.call()\n" +
            "  }\n" +
            "  return 0\n" +
            "}\n" +
            "println(foo(1))\n",
        };
        //@formatter:on

        runConformTest(sources, "1");
    }

    @Test
    public void testTypeChecked9915() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class C {\n" +
            "  void m() {\n" +
            "    init(Collections.emptyList())\n" + // Cannot call C#init(List<String>) with arguments [List<T>]
            "  }\n" +
            "  private static void init(List<String> strings) {\n" +
            "    print strings\n" +
            "  }\n" +
            "}\n" +
            "new C().m()\n",
        };
        //@formatter:on

        runConformTest(sources, "[]");
    }

    @Test
    public void testTypeChecked9935() {
        for (String type : new String[] {"def", "int", "Integer", "BigInteger", "BigDecimal"}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                "@groovy.transform.TypeChecked\n" +
                "Number f() {\n" +
                "  " + type + " n = 42\n" +
                "  return n\n" +
                "}\n" +
                "print f()\n",
            };
            //@formatter:on

            runConformTest(sources, "42");
        }
    }

    @Test
    public void testTypeChecked9945() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "interface I<T> {\n" +
            "}\n" +
            "class A<T> implements I<Character> {\n" +
            "  void m(T t) {\n" +
            "    print t\n" +
            "  }\n" +
            "}\n" +
            "class B<T> extends A<T> {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  new B<Integer>().m(42)\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testTypeChecked9948() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TupleConstructor\n" +
            "class C<T> {\n" +
            "  T p\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  C<Integer> c = new C<>(1)\n" +
            "  print(c.p < 10)\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "true");
    }

    @Test
    public void testTypeChecked9953() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "C test(Object x) {\n" +
            "  if (x instanceof C) {\n" +
            "    def y = x\n" +
            "    return y\n" +
            "  } else {\n" +
            "    new C()\n" +
            "  }\n" +
            "}\n" +
            "new C().with { assert test(it).is(it) }\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked9956() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TupleConstructor\n" +
            "class C<Y> {\n" +
            "  Y p\n" +
            "}\n" +
            "interface I { }\n" +
            "class D implements I { }\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  C<I> ci = new C<>(new D())\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked9956a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "abstract class A<X> {\n" +
            "}\n" +
            "@groovy.transform.TupleConstructor\n" +
            "class C<Y> extends A<Y> {\n" +
            "  Y p\n" +
            "}\n" +
            "interface I { }\n" +
            "class D implements I { }\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  A<I> ai = new C<>(new D())\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked9956b() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "abstract class A<X> {\n" +
            "}\n" +
            "class C<Y> extends A<Y> {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  A<String> ax = new C<Number>()\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 7)\n" +
            "\tA<String> ax = new C<Number>()\n" +
            "\t               ^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Incompatible generic argument types. Cannot assign C<java.lang.Number> to: A<java.lang.String>\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked9963() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TupleConstructor\n" +
            "class C<T> {\n" +
            "  T p\n" +
            "}\n" +
            "static m(String s) {\n" +
            "  print s\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "static test() {\n" +
            "  m(new C<>('x').p)\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "x");
    }

    @Test
    public void testTypeChecked9968() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "@Canonical class Pogo { String prop }\n" +
            "@Canonical class IterableType<T extends Pogo> implements Iterable<T> {\n" +
            "  Iterator<T> iterator() {\n" +
            "    list.iterator()\n" +
            "  }\n" +
            "  List<T> list\n" +
            "}\n" +
            "@TypeChecked void test() {\n" +
            "  def iterable = new IterableType([new Pogo('x'), new Pogo('y'), new Pogo('z')])\n" +
            "  print iterable.collect { Pogo p -> p.prop }\n" +
            "  print iterable.collect { it.prop }\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "[x, y, z][x, y, z]");
    }

    @Test
    public void testTypeChecked9970() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TupleConstructor\n" +
            "class A<T extends B> {\n" +
            "  T p\n" +
            "}\n" +
            "class B {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "class C<T extends Number> {\n" +
            "  void test(T n) {\n" +
            "    A<B> x = new A<>(new B())\n" +
            "    def closure = { ->\n" +
            "      A<B> y = new A<>(new B())\n" +
            "    }\n" +
            "    closure.call()\n" +
            "  }\n" +
            "}\n" +
            "new C<Long>().test(42L)\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked9971() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "def m(Closure<String> block) {\n" +
            "  block.call()\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  final x = 123\n" +
            "  Closure<String> c = { \"x=$x\" }\n" +
            "  print c.call().class.name\n" +
            "  print((m { \"x=$x\" }).class.name)\n" +
            "  assert m { -> \"x=$x\" } == 'x=123'\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "java.lang.Stringjava.lang.String");
    }

    @Test
    public void testTypeChecked9972() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TupleConstructor\n" +
            "class A<T> {\n" +
            "  T p\n" +
            "}\n" +
            "class B {\n" +
            "  public String f = 'B#f'\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  A<B> x = true ? new A<>(new B()) : new A<>(new B())\n" +
            "  print x.p.f.toLowerCase()\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "b#f");
    }

    @Test
    public void testTypeChecked9972a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TupleConstructor\n" +
            "class A<T> {\n" +
            "  T p\n" +
            "}\n" +
            "class B {\n" +
            "  public String f = 'B#f'\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test(flag) {\n" +
            "  A<B> x = flag ? new A<>(new B()) : (flag ? new A<>(new B()) : new A<>(new B()))\n" +
            "  print x.p.f.toLowerCase()\n" +
            "}\n" +
            "test(true)\n" +
            "test(false)\n",
        };
        //@formatter:on

        runConformTest(sources, "b#fb#f");
    }

    @Test
    public void testTypeChecked9972b() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TupleConstructor\n" +
            "class A<T> {\n" +
            "  T p\n" +
            "}\n" +
            "class B {\n" +
            "  public String f = 'B#f'\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  def x\n" +
            "  if (true) {\n" +
            "    x = new A<>(new B())\n" +
            "  } else {\n" +
            "    x = new A<>(new B())\n" +
            "  }\n" +
            "  print x.p.f.toLowerCase()\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "b#f");
    }

    @Test
    public void testTypeChecked9972c() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TupleConstructor\n" +
            "class A {\n" +
            "  List<B> bees\n" +
            "}\n" +
            "class B {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test(A... args) {\n" +
            "  List<B> bees = args.collectMany { it.bees ?: [] }\n" +
            "}\n" +
            "test(new A(), new A(bees: [new B()]))\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked9974() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "boolean isBlank(String s) {\n" +
            "  s.isAllWhitespace()\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  print([''].removeIf(this.&isBlank))\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "true");
    }

    @Test
    public void testTypeChecked9977() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class C {\n" +
            "  public final Comparator<Integer> f = { a, b -> Integer.compare(a, b) }\n" +
            "  \n" +
            "  final Comparator<Integer> p = { a, b -> Integer.compare(a, b) }\n" +
            "  def m() {\n" +
            "    Comparator<Integer> v = { a, b -> Integer.compare(a, b) }\n" +
            "  }\n" +
            "}\n" +
            "print new C().getP().compare(0, 1)\n",
        };
        //@formatter:on

        runConformTest(sources, "-1");
    }

    @Test
    public void testTypeChecked9984() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TupleConstructor(defaults=false)\n" +
            "class C<T> {\n" +
            "  T p\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  C<Integer> c = new C<>(null)\n" +
            "  print c.p\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "null");
    }

    @Test
    public void testTypeChecked9985() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  new Integer[] {123, 'x'}\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 3)\n" +
            "\tnew Integer[] {123, 'x'}\n" +
            "\t                    ^^^\n" +
            "Groovy:[Static type checking] - Cannot convert from java.lang.String to java.lang.Integer\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked9985a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  new Integer[123]['x']\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 3)\n" +
            "\tnew Integer[123]['x']\n" +
            "\t                 ^^^\n" +
            "Groovy:[Static type checking] - Cannot convert from java.lang.String to int\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked9991() {
        if (Float.parseFloat(System.getProperty("java.specification.version")) > 8)
            vmArguments = new String[] {"--add-opens", "java.base/java.util.function=ALL-UNNAMED"};

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  java.util.function.Predicate<?> p = { false }\n" +
            "  print p.test(null)\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "false");
    }

    @Test
    public void testTypeChecked9995() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TupleConstructor(defaults=false)\n" +
            "class A<T extends Number> {\n" +
            "  T p\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  Closure<A<Long>> c = { ->\n" +
            "    long x = 1\n" +
            "    new A<>(x)\n" +
            "  }\n" +
            "  Long y = c().p\n" +
            "  print y\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "1");
    }

    @Test
    public void testTypeChecked9996() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TupleConstructor(defaults=false)\n" +
            "class A<T> {\n" +
            "  T p\n" +
            "}\n" +
            "class B { }\n" +
            "class C extends B { }\n" +
            "static m(A<B> ab) { }\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  B b = new C()\n" + // "b" infers as C
            "  def a = new A<>(b)\n" + // "a" infers as A<C>
            "  m(a)\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked9997() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  def cast = (Comparator<Integer>) { a, b -> Integer.compare(a, b) }\n" +
            "  def coerce = { a, b -> Integer.compare(a, b) } as Comparator<Integer>\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked9998() {
        if (Float.parseFloat(System.getProperty("java.specification.version")) > 8)
            vmArguments = new String[] {"--add-opens", "java.base/java.util.stream=ALL-UNNAMED"};

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "@TupleConstructor(defaults=false)\n" +
            "class A {\n" +
            "  final int order\n" +
            "}\n" +
            "@InheritConstructors @ToString(includeSuperProperties=true)\n" +
            "class B extends A {\n" +
            "}\n" +
            "@TypeChecked\n" +
            "def test() {\n" +
            "  Comparator<A> comparator = { a1, a2 -> Integer.compare(a1.order, a2.order) }\n" +
            "  [new B(2), new B(3), new B(1), new B(0)].stream().sorted(comparator).toList()\n" +
            "}\n" +
            "print test()\n",
        };
        //@formatter:on

        runConformTest(sources, "[B(0), B(1), B(2), B(3)]");
    }

    @Test
    public void testTypeChecked9998a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  List<String> strings = [1].collectMany {\n" +
            "    Collections.emptyList()\n" +
            "  }\n" +
            "  print strings\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "[]");
    }

    @Test
    public void testTypeChecked9998b() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import static Type.*\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  CharSequence cs = make(type)\n" +
            "  print cs\n" +
            "}\n" +
            "test()\n",

            "Type.java",
            "class Type {\n" +
            "  static <T> T make(Class<T> c) { return null; }\n" +
            "  static Class<? extends CharSequence> getType() { return String.class; }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "null");
    }

    @Test
    public void testTypeChecked10006() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "def <T> void a(T one, T two) { }\n" +
            "def <T> void b(T one, List<T> many) { }\n" +
            "def <T> void c(T one, T two, T three) { }\n" +
            "def <T extends Number> void d(T one, T two) { }\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  a(1,'II')\n" +
            "  b(1,['II','III'])\n" +
            "  c(1,'II',Class)\n" +
            "  d(1L,2G)\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked10006a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "def <T extends Number> void d(T one, T two) { }\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  d(1,'II')\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 4)\n" +
            "\td(1,'II')\n" +
            "\t^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching method Main#d(int, java.lang.String). Please check if the declared type is correct and if the method exists.\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked10010() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "void m(List<String> list) { }\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  def bar = 123\n" +
            "  m([\"foo\",\"$bar\"])\n" +
            "  List<String> list = [\"foo\",\"$bar\"]\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 5)\n" +
            "\tm([\"foo\",\"$bar\"])\n" +
            "\t^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - You are trying to use a GString in place of a String in a type which explicitly declares accepting String. Make sure to call toString() on all GString values.\n" +
            "----------\n" +
            "2. ERROR in Main.groovy (at line 6)\n" +
            "\tList<String> list = [\"foo\",\"$bar\"]\n" +
            "\t                    ^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - You are trying to use a GString in place of a String in a type which explicitly declares accepting String. Make sure to call toString() on all GString values.\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked10010a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "void m(Map<?,String> map) { }\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  def bar = 123\n" +
            "  m([x:\"foo\",y:\"$bar\"])\n" +
            "  Map<String,String> map = [x:\"foo\",y:\"$bar\"]\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 5)\n" +
            "\tm([x:\"foo\",y:\"$bar\"])\n" +
            "\t^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - You are trying to use a GString in place of a String in a type which explicitly declares accepting String. Make sure to call toString() on all GString values.\n" +
            "----------\n" +
            "2. ERROR in Main.groovy (at line 6)\n" +
            "\tMap<String,String> map = [x:\"foo\",y:\"$bar\"]\n" +
            "\t                         ^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - You are trying to use a GString in place of a String in a type which explicitly declares accepting String. Make sure to call toString() on all GString values.\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked10011() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TupleConstructor(defaults=false)\n" +
            "class C<T> {\n" +
            "  T p\n" +
            "}\n" +
            "interface I { }\n" +
            "class D implements I { }\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test(I i) {\n" +
            "  if (i instanceof D)\n" +
            "    C<D> cd = new C<>(i)\n" +
            "}\n" +
            "test(new D())\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked10027() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "int getAnswer() { return 42 }\n" +
            "void m(@NamedParams([\n" +
            "  @NamedParam(value='n', type=Number)\n" +
            "]) Map<String,?> map) { print map.n }\n" +
            "@TypeChecked\n" +
            "void test() {\n" +
            "  m(n: answer)\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }
}
