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
            "@groovy.transform.TypeChecked\n"+
            "void method(String message) {\n"+
            "  if (rareCondition) {\n"+
            "    println \"Did you spot the error in this ${message.toUppercase()}?\"\n"+
            "  }\n"+
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 3)\n" +
            "\tif (rareCondition) {\n" +
            "\t    ^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - The variable [rareCondition] is undeclared.\n" +
            "----------\n" +
            "2. ERROR in Main.groovy (at line 4)\n" +
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
            "@groovy.transform.TypeChecked\n" +
            "void method(String message) {\n" +
            "  List<Integer> ls = new ArrayList<Integer>()\n" +
            "  ls.add(123)\n" +
            "  ls.add('abc')\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 5)\n" +
            "\tls.add(\'abc\')\n" +
            "\t^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching method java.util.ArrayList#add(java.lang.String). Please check if the declared type is correct and if the method exists.\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked3() {
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
    public void testTypeChecked4() {
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
    public void testTypeChecked5() {
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
    public void testTypeChecked6() {
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
    public void testTypeChecked7() {
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
    public void testTypeChecked8() {
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
    public void testTypeChecked9() {
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

    @Test // https://github.com/groovy/groovy-eclipse/issues/1281
    public void testTypeChecked10() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  Object v1 = 'a'\n" +
            "  Object v2 = 'b'\n" +
            "  [v1, v2].each { v ->\n" +
            "    if (v instanceof Map) {\n" +
            "      v.entrySet().each { e ->\n" +
            "        def s = e.value\n" + // No such property "value" for Object
            "        if (s instanceof String)\n" +
            "          e.value = s.toUpperCase()\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testTypeChecked11() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "@SuppressWarnings('rawtypes')\n" +
            "void test(Map args) {\n" +
            "  Set<String> keys = args.keySet()\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testTypeChecked12() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import static java.util.stream.Collectors.toList\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  List<CharSequence> xxx = ['x'].collect()\n" +
            "  List<CharSequence> yyy = ['y'].stream().toList()\n" +
            "  List<CharSequence> zzz = ['z'].stream().collect(toList())\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 4)\n" +
            "\tList<CharSequence> xxx = ['x'].collect()\n" +
            "\t                         ^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Incompatible generic argument types. Cannot assign java.util.List<java.lang.String> to: java.util.List<java.lang.CharSequence>\n" +
            "----------\n" +
            "2. ERROR in Main.groovy (at line 5)\n" +
            "\tList<CharSequence> yyy = ['y'].stream().toList()\n" +
            "\t                         ^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Incompatible generic argument types. Cannot assign java.util.List<java.lang.String> to: java.util.List<java.lang.CharSequence>\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked13() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import org.codehaus.groovy.runtime.DefaultGroovyMethods as DGM\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  def strings = ['x','yy','zzz']\n" +
            "  print(strings.inject(0) { result, string -> result += string.length() })\n" +
            "  print(strings.inject { result, string -> result += string.toUpperCase() })\n" +
            "  print(DGM.inject(strings) { result, string -> result += string.toUpperCase() })\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "6xYYZZZxYYZZZ");
    }

    @Test
    public void testTypeChecked14() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "interface OngoingStubbing<T> /*extends IOngoingStubbing*/ {\n" +
            "  OngoingStubbing<T> thenReturn(T value)\n" +
            "}\n" +
            "static <T> OngoingStubbing<T> when(T methodCall) {\n" +
            "  [thenReturn: { T value -> null }] as OngoingStubbing<T>\n" +
            "}\n" +
            "Optional<String> foo() {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  when(foo()).thenReturn(Optional.empty())\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked15() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test(Pojo pojo) {\n" +
            "  Foo raw = pojo.getFoo('')\n" +
            "  raw.bar = raw.baz\n" +
            "}\n" +
            "test(new Pojo())\n",

            "Pojo.java",
            "public class Pojo {\n" +
            "  public <R extends I> Foo<R> getFoo(String key) {\n" +
            "    return new Foo<>();\n" +
            "  }\n" +
            "}\n",

            "Types.groovy",
            "interface I {\n" +
            "}\n" +
            "class Foo<T extends I> {\n" +
            "  T bar\n" +
            "  T baz\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked16() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "def <T,U extends Configurable<T>> U configure(Class<U> type, @DelegatesTo(type='T',strategy=Closure.DELEGATE_FIRST) Closure<?> spec) {\n" +
            "  Configurable<T> obj = (Configurable<T>) type.newInstance()\n" +
            "  obj.configure(spec)\n" +
            "  obj\n" +
            "}\n" +
            "trait Configurable<X> { X configObject\n" +
            "  void configure(Closure<Void> spec) {\n" +
            "    configObject.with(spec)\n" +
            "  }\n" +
            "}\n" +
            "class Item implements Configurable<ItemConfig> {\n" +
            "  Item() {\n" +
            "    configObject = new ItemConfig()\n" +
            "  }\n" +
            "}\n" +
            "class ItemConfig {\n" +
            "  String name, version\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "def test() {\n" +
            "  configure(Item) {\n" +
            "    name = 'test'\n" +
            "    version = '1'\n" +
            "  }\n" +
            "}\n" +
            "print test().configObject.name\n",
        };
        //@formatter:on

        runConformTest(sources, "test");
    }

    @Test
    public void testTypeChecked5523() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import static org.codehaus.groovy.ast.ClassHelper.*\n" +
            "import static org.codehaus.groovy.transform.stc.StaticTypesMarker.*\n" +
            "@groovy.transform.TypeChecked\n" +
            "def findFile(String path) {\n" +
            "  @groovy.transform.ASTTest(phase=INSTRUCTION_SELECTION, value={\n" +
            "    assert node.getNodeMetaData(INFERRED_TYPE) == make(File)\n" +
            "  })\n" +
            "  File file = path ? null : null\n" + // edge case
            "}",
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
    public void testTypeChecked6240() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test(Map<String,Number> map) {\n" +
            "  for (e in map) {\n" +
            "    print \"${e.key.toUpperCase()}${e.value.intValue()}\"\n" +
            "  }\n" +
            "}\n" +
            "test(a:1,b:2,c:3.14)\n",
        };
        //@formatter:on

        runConformTest(sources, "A1B2C3");
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
    public void testTypeChecked7753() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.Field\n" +
            "String x = 'X'\n" +
            "@groovy.transform.TypeChecked\n" +
            "public List<String> getStrings() {\n" +
            "  x ? [x] : Collections.emptyList()\n" +
            "}\n" +
            "print strings\n",
        };
        //@formatter:on

        runConformTest(sources, "[X]");
    }

    @Test
    public void testTypeChecked7804() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" + // Supplier also uses "T"
            "def <T> T test(java.util.function.Supplier<T> supplier) {\n" +
            "  supplier.get()\n" +
            "}\n" +
            "print(test { -> 'foo' })\n",
        };
        //@formatter:on

        runConformTest(sources, "foo");
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
            "Groovy:[Static type checking] - Cannot call A#<init>(java.lang.Class<java.lang.String>, java.lang.Class<java.lang.Integer>) with arguments [java.lang.Class<java.lang.Integer>, java.lang.Class<java.lang.String>]\n" +
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
    public void testTypeChecked8111() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "def test(thing) {\n" +
            "  thing != null ?: Pair.of('k','v')\n" + // StackOverflowError
            "}\n",

            "Pair.groovy",
            "class Pair<L,R> implements Map.Entry<L,R>, Comparable<Pair<L,R>>, Serializable {\n" +
            "  public final L left\n" +
            "  public final R right\n" +
            "  private Pair(final L left, final R right) {\n" +
            "    this.left = left\n" +
            "    this.right = right\n" +
            "  }\n" +
            "  static <L, R> Pair<L, R> of(final L left, final R right) {\n" +
            "    return new Pair<>(left, right)\n" +
            "  }\n" +
            "  L getKey() { left }\n" +
            "  R getValue() { right }\n" +
            "  R setValue(R value) { right }\n" +
            "  int compareTo(Pair<L,R> that) { 0 }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked8202() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "void proc() {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "String test0(flag) {\n" +
            "  if (flag) {\n" +
            "    'foo'\n" +
            "  } else {\n" +
            "    proc()\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "String test1(flag) {\n" +
            "  Closure<String> c = { ->\n" +
            "    if (flag) {\n" +
            "      'bar'\n" +
            "    } else {\n" +
            "      proc()\n" +
            "      null\n" +
            "    }\n" +
            "  }\n" +
            "  c.call()\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "String test2(flag) {\n" +
            "  Closure<String> c = { ->\n" + // Cannot assign Closure<Object> to Closure<String>
            "    if (flag) {\n" +
            "      'baz'\n" +
            "    } else {\n" +
            "      proc()\n" +
            "    }\n" +
            "  }\n" +
            "  c.call()\n" +
            "}\n" +
            "print test0(true)\n" +
            "print test1(true)\n" +
            "print test2(true)\n" +
            "print test0(false)\n" +
            "print test1(false)\n" +
            "print test2(false)\n",
        };
        //@formatter:on

        runConformTest(sources, "foobarbaznullnullnull");
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
    public void testTypeChecked8974() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "def <T> T identity(T t) { t }\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  List<String> list = identity(new ArrayList<>())\n" +
            "  list.add('foo'); print list.get(0).toUpperCase()\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "FOO");
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
    public void testTypeChecked9033() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "List<String> test() {\n" +
            "  def list = []\n" +
            "  list << null\n" +
            "  return list\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 5)\n" +
            "\treturn list\n" +
            "\t       ^^^^\n" +
            "Groovy:[Static type checking] - Incompatible generic argument types. Cannot assign java.util.List<java.lang.Object> to: java.util.List<java.lang.String>\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked9033a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  def map = [key: []]\n" +
            "  map.add('foo','bar')\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 4)\n" +
            "\tmap.add('foo','bar')\n" +
            "\t^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching method java.util.LinkedHashMap#add(java.lang.String, java.lang.String). Please check if the declared type is correct and if the method exists.\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked9033b() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  @groovy.transform.ASTTest(phase=INSTRUCTION_SELECTION, value={\n" +
            "    def type = node.getNodeMetaData(org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_TYPE)\n" +
            "    assert type.toString(false) == 'java.util.LinkedList<java.lang.String>'\n" +
            "  })\n" +
            "  Iterable<String> list = new LinkedList()\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked9412() {
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
            "  def <U> U of(@DelegatesTo(value=Holder, strategy=Closure.DELEGATE_FIRST) Closure<U> c) {\n" +
            //                                 ^^^^^^ type argument cannot be supplied using value attribute
            "    this.with(c)\n" +
            "  }\n" +
            "}\n" +
            "class TypedProperty<V, Unused> {\n" +
            "  Class<V> clazz\n" +
            "  void eq(V that) {\n" +
            "    assert that.class == this.lclazz : \"that.class is ${that.class} not ${this.clazz}\"\n" +
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
            "Groovy:[Static type checking] - Cannot call TypedProperty#eq(java.lang.String) with arguments [groovy.lang.GString]\n" +
            "----------\n" +
            "2. ERROR in Main.groovy (at line 21)\n" +
            "\tstringProperty.eq(1234)\n" +
            "\t^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching method TypedProperty#eq(int). Please check if the declared type is correct and if the method exists.\n" +
            "----------\n" +
            "3. ERROR in Main.groovy (at line 22)\n" +
            "\tnumberProperty.eq('xx')\n" +
            "\t^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching method TypedProperty#eq(java.lang.String). Please check if the declared type is correct and if the method exists.\n" +
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
        for (String type : new String[] {"List", "Iterable", "Collection"}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                "@groovy.transform.TypeChecked\n" +
                "class C {\n" +
                "  void m() {\n" +
                "    init(Collections.emptyList())\n" + // Cannot call C#init(List<String>) with arguments [List<T>]
                "  }\n" +
                "  private static void init(" + type + "<String> strings) {\n" +
                "    print strings\n" +
                "  }\n" +
                "}\n" +
                "new C().m()\n",
            };
            //@formatter:on

            runConformTest(sources, "[]");
        }
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
    public void testTypeChecked9983() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TupleConstructor(defaults=false)\n" +
            "class A<T> {\n" +
            "  T p\n" +
            "}\n" +
            "class B {\n" +
            "}\n" +
            "class C {\n" +
            "  static m(A<B> a_of_b) {\n" +
            "  }\n" +
            "}\n" +
            "class D extends B {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  boolean flag = true\n" +
            "  A<B> v = new A<>(null)\n" + // Cannot call C#m(A<B>) with arguments [A<#>]
            "  A<B> w = new A<>(new B())\n" +
            "  A<B> x = new A<>(new D())\n" +
            "  A<B> y = flag ? new A<>(new B()) : new A<>(new B())\n" +
            "  A<B> z = flag ? new A<>(new B()) : new A<>(new D())\n" +
            "  C.m(new A<>(null))\n" +
            "  C.m(new A<>(new B()))\n" +
            "  C.m(new A<>(new D()))\n" +
            "  C.m(flag ? new A<>(new B()) : new A<>(new B()))\n" +
            "  C.m(flag ? new A<>(new B()) : new A<>(new D()))\n" + // Cannot call m(A<B>) with arguments [A<? extends B>]\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
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
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  new char[] {'a','b','c'}\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked9985b() {
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

    @Test
    public void testTypeChecked10049() {
        if (Float.parseFloat(System.getProperty("java.specification.version")) > 8)
            vmArguments = new String[] {"--add-opens", "java.base/java.util.stream=ALL-UNNAMED"};

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "def <X /*extends Number*/> Set<X> generateNumbers(Class<X> type) {\n" +
            "  return Collections.singleton(type.newInstance(42))\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "def <Y extends Number> void printNumbers(Class<Y> numberType) {\n" +
            "  generateNumbers(numberType).stream()\n" +
            "    .filter { n -> n.intValue() > 0 }\n" +
            "    .forEach { n -> print n }\n" +
            "}\n" +
            "printNumbers(Integer)\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testTypeChecked10051() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "abstract class State/*<H extends Handle>*/ {\n" +
            "  def <T extends Handle> HandleContainer<T> getHandleContainer(key) {\n" +
            "  }\n" +
            "}\n" +
            "class HandleContainer<H extends Handle> {\n" +
            "  H handle\n" +
            "}\n" +
            "interface Handle {\n" +
            "  Result getResult()\n" +
            "}\n" +
            "class Result {\n" +
            "  int itemCount\n" +
            "  String[] items\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "List<String> getStrings(State state, List<?> keys) {\n" +
            "  keys.collectMany { key ->\n" +
            "    List<String> strings = Collections.emptyList()\n" +
            "    \n" +
            "    def container = state.getHandleContainer(key)\n" + // returns HandleContainer<Object> not HandleContainer<Handle>
            "    if (container != null) {\n" +
            "      def result = container.handle.result\n" +
            "      if (result != null && result.itemCount > 0) {\n" +
            "        strings = Arrays.asList(result.items)\n" +
            "      }\n" +
            "    }\n" +
            "    \n" +
            "    strings\n" +
            "  }\n" +
            "}\n" +
            "print getStrings(null,[])\n",
        };
        //@formatter:on

        runConformTest(sources, "[]");
    }

    @Test
    public void testTypeChecked10052() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  String x = Optional.of('x').orElseThrow({ new Exception() })\n" +
            "  def f = { ->\n" +
            "    String y = Optional.of('y').orElseThrow({ new Exception() })\n" +
            "  }\n" +
            "  print x\n" +
            "  print f()\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "xy");
    }

    @Test
    public void testTypeChecked10053() {
        if (Float.parseFloat(System.getProperty("java.specification.version")) > 8)
            vmArguments = new String[] {"--add-opens", "java.base/java.util.stream=ALL-UNNAMED"};

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "Set<Number> f() {\n" +
            "  Collections.<Number>singleton(42)\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "def <N extends Number> Set<N> g(Class<N> t) {\n" +
            "  Set<N> result = new HashSet<>()\n" +
            "  f().stream().filter{n -> t.isInstance(n)}\n" +
            "    .map{n -> t.cast(n)}.forEach{n -> result.add(n)}\n" +
            "  return result\n" +
            "}\n" +
            "print g(Integer)\n",
        };
        //@formatter:on

        runConformTest(sources, "[42]");
    }

    @Test
    public void testTypeChecked10056() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test(String[][] arrayArray) {\n" +
            "  print Arrays.asList(arrayArray).get(0).length\n" +
            "}\n" +
            "String[][] arrayArray = [\n" +
            "  ['a','b','c'],\n" +
            "  ['d','e','f']\n" +
            "]\n" +
            "test(arrayArray)\n",
        };
        //@formatter:on

        runConformTest(sources, "3");
    }

    @Test
    public void testTypeChecked10062() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "def <T> T m(T t, ... zeroOrMore) { t }\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  def obj = m(42)\n" +
            "  print obj.intValue()\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testTypeChecked10063() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "static Tuple2<String,Integer> m() {\n" +
            "  new Tuple2<>('answer', 42)\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  def (String string, Integer number) = m()\n" +
            "  print number\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testTypeChecked10067() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "def <N extends Number> N getNumber() {\n" +
            "  return (N) 42\n" +
            "}\n" +
            "def f(Integer i) {\n" +
            "}\n" +
            "def g(int i) {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  Integer i = this.<Integer>getNumber()\n" +
            "  f(this.<Integer>getNumber())\n" +
            "  g(this.<Integer>getNumber())\n" +
            "  i = (Integer) getNumber()\n" +
            "  f((Integer) getNumber())\n" +
            "  g((Integer) getNumber())\n" +
            "  i = getNumber()\n" +
            "  f(getNumber())\n" +
            "  g(getNumber())\n" +
            "  i = number\n" +
            "  f(number)\n" +
            "  g(number)\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked10080() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TupleConstructor(defaults=false)\n" +
            "class C<T> {\n" +
            "  T p\n" +
            "}\n" +
            "class D {\n" +
            "  int m(Object[] objects) {\n" +
            "    42\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  def closure = { ->\n" +
            "    new C<>(new D())\n" +
            "  }\n" +
            "  print(closure().p.m(new BigDecimal[0]))\n" + // Cannot find matching method Object#m(...)
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testTypeChecked10082() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class A {}\n" +
            "class B extends A {}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  Closure<A> c = { -> new B() }\n" +
            "  print(c() instanceof A)\n" +
            "  print(c() instanceof B)\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "truetrue");
    }

    @Test
    public void testTypeChecked10082a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  Closure<String> c = {-> 42}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 3)\n" +
            "\tClosure<String> c = {-> 42}\n" +
            "\t                    ^^^^^^^\n" +
            "Groovy:[Static type checking] - Incompatible generic argument types. Cannot assign groovy.lang.Closure<java.lang.Integer> to: groovy.lang.Closure<java.lang.String>\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked10086() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "def m(C<D> c_of_d) {c_of_d.p}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  m(new C<>(0))\n" +
            "}\n",

            "Types.groovy",
            "@groovy.transform.TupleConstructor(defaults=false)\n" +
            "class C<T> {\n" +
            "  T p\n" +
            "}\n" +
            "class D {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 4)\n" +
            "\tm(new C<>(0))\n" +
            "\t^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot call Main#m(C<D>) with arguments [C<java.lang.Integer>]\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked10088() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  new D<Number>().p = 'x'\n" +
            "}\n",

            "Types.groovy",
            "class C<T> {\n" +
            "  void setP(T t) { }\n" +
            "}\n" +
            "class D<X> extends C<X> {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 3)\n" +
            "\tnew D<Number>().p = 'x'\n" +
            "\t^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot assign value of type java.lang.String to variable of type java.lang.Number\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked10089() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "@groovy.transform.TypeChecked\n" +
            "def test(... attributes) {\n" +
            "  List one = [\n" +
            "    [id:'x', options:[count:42]]\n" +
            "  ]\n" +
            "  List two = attributes.collect {\n" +
            "    def node = Collections.singletonMap('children', one)\n" +
            "    if (node) {\n" +
            "      node = node.get('children').find { child -> child['id'] == 'x' }\n" +
            "    }\n" +
            "    [id: it['id'], name: node['name'], count: node['options']['count']]\n" +
            "  }\n" + //                                   ^^^^^^^^^^^^^^^ GroovyCastException (map ctor for Collection)
            "}\n" +
            "print test( [id:'x'] ).first().count\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testTypeChecked10091() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class A<T> {}\n" +
            "class B extends A<Number> {}\n" +
            "class X extends A<String> {}\n" +
            "class Y<Z> extends A<Number> {}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  Closure<A<Number>> b = { -> new B()}\n" +
            "  Closure<A<Number>> x = { -> new X()}\n" +
            "  Closure<A<Number>> y = { -> new Y<String>()}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 8)\n" +
            "\tClosure<A<Number>> x = { -> new X()}\n" +
            "\t                       ^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Incompatible generic argument types. Cannot assign groovy.lang.Closure<X> to: groovy.lang.Closure<A<java.lang.Number>>\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked10094() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test(int i = 'error') {}\n" +
            "test()\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 2)\n" +
            "\tvoid test(int i = 'error') {}\n" +
            "\t                  ^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot assign value of type java.lang.String to variable of type int\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked10098() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "@groovy.transform.TupleConstructor(defaults=false)\n" +
            "class C<T extends Number> {\n" +
            "  T p\n" +
            "  @groovy.transform.TypeChecked\n" +
            "  T test() {\n" +
            "    Closure<T> x = { -> p }\n" +
            "    x()\n" + // Cannot return value of type Object on method returning type T
            "  }\n" +
            "  static main(args) {\n" +
            "    print new C<>(42).test()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testTypeChecked10107() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class C<T extends Number> {\n" +
            "  void m() {\n" +
            "    T n = null\n" +
            "  }\n" +
            "}\n" +
            "new C<Long>().m()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked10111() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C<X, Y> { }\n" +
            "@groovy.transform.TypeChecked\n" +
            "def <X extends C<Number, String>> X[] m() {\n" +
            "  new X[]{ new C<Number, String>() }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked10111a() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C<X, Y> { }\n" +
            "@groovy.transform.TypeChecked\n" +
            "def String[] test() {\n" +
            "  new String[]{ 1, (long)2, (short)3 }\n" +
            "}\n" +
            "def result = test()\n" +
            "assert result.toString() == '[1, 2, 3]'\n" +
            "assert result.every { it.class == String }\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked10166() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "@SuppressWarnings('rawtypes')\n" +
            "abstract class A<T extends C> {\n" +
            "  T getC() {\n" +
            "  }\n" +
            "  Map toMap() {\n" +
            "    c.getMap(this)\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "@SuppressWarnings('rawtypes')\n" +
            "class C<T extends A> {\n" +
            "  Map getMap(T a) {\n" +
            "  }\n" +
            "  T getObj(Map m) {\n" +
            "    A a = null\n" +
            "    a.c.get(1)\n" +
            "  }\n" +
            "  T get(int i) {\n" +
            "  }\n" +
            "}\n" +
            "new C()\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 17)\n" +
            "\ta.c.get(1)\n" +
            "\t^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching method A#get(int). Please check if the declared type is correct and if the method exists.\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked10179() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test(args) {\n" +
            "  if (args instanceof Map) {\n" +
            "    for (e in args) {\n" +
            "      print \"${e.key}${e.value}\"\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "test(a:1,b:2,c:3.14)\n",
        };
        //@formatter:on

        runConformTest(sources, "a1b2c3.14");
    }

    @Test
    public void testTypeChecked10180() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test(args) {\n" +
            "  if (args instanceof Map) {\n" +
            "    args.each { e ->\n" +
            "      print \"${e.key}${e.value}\"\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "test(a:1,b:2,c:3.14)\n",
        };
        //@formatter:on

        runConformTest(sources, "a1b2c3.14");
    }

    @Test
    public void testTypeChecked10217() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test(Object o) {\n" +
            "  if (o instanceof List) {\n" +
            "    print o[0]\n" +
            "    def x = (List) o\n" +
            "    print x[0]\n" +
            "  }\n" +
            "}\n" +
            "test([1])\n",
        };
        //@formatter:on

        runConformTest(sources, "11");
    }
}
