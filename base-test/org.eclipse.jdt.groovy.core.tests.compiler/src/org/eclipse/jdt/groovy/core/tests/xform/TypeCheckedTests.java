/*
 * Copyright 2009-2022 the original author or authors.
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

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;
import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isParrotParser;
import static org.junit.Assume.assumeTrue;

import java.util.Map;

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
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
            "  List<Integer> ints = new ArrayList<>()\n" +
            "  ints.add(12345)\n" +
            "  ints.add('abc')\n" +
            "  ints << 'def'\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 5)\n" +
            "\tints.add(\'abc\')\n" +
            "\t^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching method java.util.ArrayList#add(java.lang.String)." +
            " Please check if the declared type is correct and if the method exists.\n" +
            "----------\n" +
            "2. ERROR in Main.groovy (at line 6)\n" +
            "\tints << 'def'\n" +
            "\t^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot call <T> org.codehaus.groovy.runtime.DefaultGroovyMethods#leftShift(java.util.List<T>, T) with arguments [java.util.ArrayList<java.lang.Integer>, java.lang.String]\n" +
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
            "  C(String s, @ClosureParams(value=FromString,options='java.util.List<java.lang.Integer>') Closure<Integer> c) {\n" +
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
            "class C<T extends Object> {\n" +
            "  public T f\n" +
            "  C(T p) {\n" +
            "    f = p\n" +
            "  }\n" +
            "}\n" +
            "print new C<String>('works').f\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testTypeChecked12() {
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

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 4)\n" +
            "\tSet<String> keys = args.keySet()\n" +
            "\t                   ^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Incompatible generic argument types. Cannot assign java.util.Set<java.lang.Object> to: java.util.Set<java.lang.String>\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked13() {
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
    public void testTypeChecked14() {
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
    public void testTypeChecked15() {
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
    public void testTypeChecked16() {
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
    public void testTypeChecked17() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  new Order<Pogo, Comparable>({Pogo p -> p.s})\n" + // No such property s for class Object
            "}\n" +
            "test()\n",

            "Order.groovy",
            "class Order<T, U extends Comparable<? super U>> {\n" +
            "  Order(java.util.function.Function<? super T, ? extends U> keyExtractor) {\n" +
            "  }\n" +
            "}\n",

            "Pogo.groovy",
            "@groovy.transform.Canonical\n" +
            "class Pogo {\n" +
            "  Number n\n" +
            "  String s\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked18() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  print(new Pogo().isFlag())\n" +
            "}\n" +
            "test()\n",

            "Pogo.groovy",
            "class Pogo {\n" +
            "  final boolean flag = true\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "true");
    }

    @Test
    public void testTypeChecked19() {
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
    public void testTypeChecked20() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "def <T> T m(java.util.function.Consumer<? super T> c) {\n" +
            "  c.accept(null)\n" +
            "  null\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  this.<Number>m { n ->\n" +
            "    n?.toBigInteger()\n" +
            "  }\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1327
    public void testTypeChecked21() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "def <T> T m(java.util.function.Consumer<? super T> c) {\n" +
            "  c.accept(null)\n" +
            "  null\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  this." + (isAtLeastGroovy(40) ? "<Number>" : "" ) + "m { Number n ->\n" + // TODO: GROOVY-10436
            "    n?.toBigInteger()\n" +
            "  }\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1349
    public void testTypeChecked22() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test(... args) {\n" +
            "  args?.each { item ->\n" +
            "    item.properties.each { key, value ->\n" +
            "      if (value instanceof Iterable) value.each { test(it) }\n" + // NPE
            "    }\n" +
            "  }\n" +
            "}\n" +
            "test([new Object()])\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked23() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  Set<Integer> ints = [1,2,3]\n" +
            "  assert ints == [1,2,3] as Set\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked24() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "import static org.codehaus.groovy.ast.ClassHelper.*\n" +
            "import static org.codehaus.groovy.transform.stc.StaticTypesMarker.*\n" +
            "@TypeChecked void test() {\n" +
            "  @ASTTest(phase=INSTRUCTION_SELECTION, value={\n" +
            "    def type = node.getNodeMetaData(INFERRED_TYPE)\n" +
            "    assert type == LIST_TYPE\n" +
            "    assert type.genericsTypes != null\n" +
            "    assert type.genericsTypes.length == 1\n" +
            "    assert type.genericsTypes[0].type == STRING_TYPE\n" +
            "  })\n" +
            "  Iterable<String> list = (List) null\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked25() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  final type = new Type<String,Integer>('works')\n" +
            "  print type.map { length() }\n" +
            "}\n" +
            "test()\n",

            "Type.groovy",
            "@groovy.transform.TupleConstructor(defaults=false)\n" +
            "class Type<T,U> {\n" +
            "  final T value\n" +
            "  U map(@DelegatesTo(type='T') Closure<U> producer) {\n" +
            "    producer.delegate = value\n" +
            "    producer()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "5");
    }

    @Test
    public void testTypeChecked26() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "void proc(Pogo p, Closure<Boolean> predicate) {\n" +
            "  if (predicate.call(p)) {\n" +
                //...
            "  }\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  proc(new Pogo(name:'Abe', age:55)) {\n" +
            "    it.age >= 18\n" +
            "  }\n" +
            "}\n" +
            "test()\n",

            "Pogo.groovy",
            "class Pogo {\n" +
            "  String name\n" +
            "  int age\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 8)\n" +
            "\tit.age >= 18\n" +
            "\t^^^^^^" + (isParrotParser() ? "" : "^") + "\n" +
            "Groovy:[Static type checking] - No such property: age for class: java.lang.Object\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked27() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "interface A {\n" +
            "  def getX()\n" +
            "}\n" +
            "interface B {\n" +
            "  def getY()\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test(A a) {\n" +
            "  if (a instanceof B) {\n" +
            "    @groovy.transform.ASTTest(phase=INSTRUCTION_SELECTION, value={\n" +
            "      def type = node.rightExpression.objectExpression.getNodeMetaData(\n" +
            "        org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_TYPE)\n" +
            "      assert type.toString(false) == '<UnionType:A+B>'\n" +
            "    })\n" +
            "    def x = a.x\n" +
            "    def y = a.y\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked28() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  Map<String,Number> map = [:].withDefault { 0 }\n" +
            "  map.put('foo', 1)\n" +
            "  map['bar'] = 2\n" +
            "  map.baz = 3.14\n" +
            "  print map\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "[foo:1, bar:2, baz:3.14]");
    }

    @Test
    public void testTypeChecked5450() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class C {\n" +
            "  public final f\n" +
            "  C() { f = 'yes' }\n" +
            "  C(C that) { this.f = that.f }\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "def test(C c) {\n" +
            "  c.f = 'x'\n" +
            "  c.@f = 'x'\n" +
            "  c.setF('x')\n" +
            "  c.with {f  = 'x'}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 9)\n" +
            "\tc.f = 'x'\n" +
            "\t^^^" + (isParrotParser() ? "" : "^") + "\n" +
            "Groovy:[Static type checking] - Cannot set read-only property: f\n" +
            "----------\n" +
            "2. ERROR in Main.groovy (at line 10)\n" +
            "\tc.@f = 'x'\n" +
            "\t^^^^" + (isParrotParser() ? "" : "^") + "\n" +
            "Groovy:[Static type checking] - Cannot set read-only property: f\n" +
            "----------\n" +
            "3. ERROR in Main.groovy (at line 11)\n" +
            "\tc.setF('x')\n" +
            "\t^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching method C#setF(java.lang.String). Please check if the declared type is correct and if the method exists.\n" +
            "----------\n" +
            "4. ERROR in Main.groovy (at line 12)\n" +
            "\tc.with {f  = 'x'}\n" +
            "\t        ^\n" +
            "Groovy:[Static type checking] - Cannot set read-only property: f\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked5502() {
        for (String val : new String[] {"null", "new A()", "new B()", "new C()"/*TODO:, "new Object()"*/}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                "class A {\n" +
                "  void m() {\n" +
                "  }\n" +
                "}\n" +
                "class B extends A {\n" +
                "}\n" +
                "class C extends A {\n" +
                "}\n" +
                "@groovy.transform.TypeChecked\n" +
                "void test(boolean flag) {\n" +
                "  def var = " + val + "\n" +
                "  if (flag) {\n" +
                "    var = new B()\n" +
                "  } else {\n" +
                "    var = new C()\n" +
                "  }\n" +
                "  var.m()\n" + // Cannot find matching method Object#m()
                "}\n" +
                "test(true)\n",
            };
            //@formatter:on

            runConformTest(sources);
        }
    }

    @Test
    public void testTypeChecked5517() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class MyHashMap extends HashMap {\n" +
            "  public static int version = 666\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  def map = new MyHashMap()\n" +
            "  map.foo = 123\n" +
            "  print map.foo\n" +
            "  map['foo'] = 4.5\n" +
            "  print map['foo']\n" +
            "  print MyHashMap.version\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "1234.5666");
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
    public void testTypeChecked6277() {
        //@formatter:off
        String[] sources = {
            "p/Main.groovy",
            "package p\n" +
            "@groovy.transform.TypeChecked\n" +
            "def test(Pogo pogo) {\n" +
            "  def x = pogo.x;\n" +
            "  x.toLowerCase()\n" +
            "}\n" +
            "print test(new Pogo())\n",

            "p/Pogo.groovy",
            "package p\n" +
            "class Pogo {\n" +
            "  protected String getX() { 'Works' }" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testTypeChecked6455() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class IntegerList {\n" +
            "  @Delegate List<Integer> delegate = new ArrayList<Integer>()\n" +
            "}\n" +
            "def list = new IntegerList()\n" +
            "assert list == []\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked6603() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.stc.*\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test(@ClosureParams(value=FromString,options='java.lang.Number') Closure<?> c) {\n" +
            "  c('x')\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 4)\n" +
            "\tc('x')\n" +
            "\t  ^^^\n" +
            "Groovy:[Static type checking] - Cannot call closure that accepts [java.lang.Number] with [java.lang.String]\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked6603a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.stc.*\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test(@ClosureParams(value=FromString,options='java.util.List<java.lang.Number>') Closure<?> c) {\n" +
            "  c(['x'])\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 4)\n" +
            "\tc(['x'])\n" +
            "\t  ^^^^^\n" +
            "Groovy:[Static type checking] - Cannot call closure that accepts [java.util.List<java.lang.Number>] with [java.util.List<java.lang.String>]\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked6603b() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.stc.*\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test(@ClosureParams(value=FromString,options='java.util.Collection<java.lang.String>') Closure<?> c) {\n" +
            "  c(Collections.singletonList('x'))\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testTypeChecked6731() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import java.util.function.Function\n" +
            "def <I, O> void transform(Function<? super I, ? extends O> function) {\n" +
            "  print(function.apply('hello'))\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  transform(new Function<String, String>() {\n" +
            "    @Override\n" +
            "    String apply(String input) {\n" +
            "      input + ' world'\n" +
            "    }\n" +
            "  })\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "hello world");
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
    public void testTypeChecked6787() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "def <T extends List<? extends CharSequence>> void foo(T list) {\n" +
            "}\n" +
            "def <T extends List<? super CharSequence>> void bar(T list) {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "def <U extends List<Object>> void one(U list) {\n" +
            "  foo(list)\n" + // no!
            "  bar(list)\n" + // yes
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "def <U extends List<String>> void two(U list) {\n" +
            "  foo(list)\n" + // yes
            "  bar(list)\n" + // no!
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 7)\n" +
            "\tfoo(list)\n" +
            "\t^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot call <T extends java.util.List<? extends java.lang.CharSequence>> Main#foo(T) with arguments [U]\n" +
            "----------\n" +
            "2. ERROR in Main.groovy (at line 13)\n" +
            "\tbar(list)\n" +
            "\t^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot call <T extends java.util.List<? super java.lang.CharSequence>> Main#bar(T) with arguments [U]\n" +
            "----------\n");
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
    public void testTypeChecked6919() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "interface I1 {\n" +
            "  String getFoo()\n" +
            "}\n" +
            "interface I2 {\n" +
            "  String getBar()\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "def <T extends I1 & I2> void test(T obj) {\n" +
            "  obj?.foo\n" +
            "  obj?.bar\n" +
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
    public void testTypeChecked7003() {
        if (Float.parseFloat(System.getProperty("java.specification.version")) > 8)
            vmArguments = new String[] {"--add-opens", "java.desktop/java.beans=ALL-UNNAMED"};

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import java.beans.*\n" +
            "@groovy.transform.TypeChecked\n" +
            "class C {\n" +
            "  static PropertyChangeListener listener = { PropertyChangeEvent event ->\n" +
            "    print \"${event.oldValue} -> ${event.newValue}\"\n" +
            "  }\n" +
            "}\n" +
            "C.getListener().propertyChange(new PropertyChangeEvent(new Object(), 'foo', 'bar', 'baz'))\n",
        };
        //@formatter:on

        runConformTest(sources, "bar -> baz");
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
    public void testTypeChecked7316() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "def <T> T blank() {\n" +
            "}\n" +
            "def <T extends Iterable<?>> T iter() {\n" +
            "}\n" +
            "def <T extends CharSequence> T seq() {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "List<?> test() {\n" +
            "  blank()\n" +
            "  iter()\n" +
            "  seq()\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 11)\n" +
            "\tseq()\n" +
            "\t^^^^^\n" +
            "Groovy:[Static type checking] - Cannot return value of type java.lang.CharSequence for method returning java.util.List<?>\n" +
            "----------\n");
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
    public void testTypeChecked7582() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "interface A<T> {\n" +
            "  void x(T t)\n" +
            "}\n" +
            "interface B {\n" +
            "  void x()\n" +
            "}\n" +
            "class C {\n" +
            "  void m(A a) { print 'fails' }\n" +
            "  void m(B b) { print 'hello'; b.x() } \n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  def c = new C()\n" +
            "  c.m { -> print ' world' }\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        if (isAtLeastGroovy(40)) {
            runConformTest(sources, "hello world");
        } else {
            runConformTest(sources, "", "groovy.lang.GroovyRuntimeException: Ambiguous method overloading for method C#m");
        }
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
    public void testTypeChecked7890() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class C {\n" +
            "  List<String> replace\n" +
            "  static String m(String s) {\n" + // static seems like an accident
            "    s.collectReplacements {\n" +
            "      (it in replace) ? 'o' : null\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 6)\n" +
            "\t(it in replace) ? 'o' : null\n" +
            "\t       ^^^^^^^\n" +
            "Groovy:[Static type checking] - The variable [replace] is undeclared.\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked7890a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class C {\n" +
            "  List<String> replace\n" +
            "  String m(String s) {\n" +
            "    s.collectReplacements {\n" +
            "      (it in replace) ? 'o' : null\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "print(new C(replace:['a','b','c']).m('foobar'))",
        };
        //@formatter:on

        runConformTest(sources, "foooor");
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
    public void testTypeChecked8693() {
        assumeTrue(isAtLeastGroovy(40));

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class C extends p.A {\n" +
            "  void m() {\n" +
            "    super.m()\n" + // MissingMethodException
            "  }\n" +
            "  void test() {\n" +
            "    m()\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",

            "p/A.java",
            "package p;\n" +
            "public abstract class A implements I {\n" +
            "}\n",

            "p/I.java",
            "package p;\n" +
            "public interface I {\n" +
            "  default void m() {\n" +
            "    System.out.print(\"works\");\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
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
    public void testTypeChecked8917() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "def test() {\n" +
            "  [1, 2, 3].stream().reduce(7) { r, e -> r + e }\n" +
            "}\n" +
            "print test()\n",
        };
        //@formatter:on

        runConformTest(sources, "13");
    }

    @Test
    public void testTypeChecked8917a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "def test() {\n" +
            "  [1, 2, 3].stream().<String>map{i -> null}.limit(1).toList()\n" +
            "}\n" +
            "print test()\n",
        };
        //@formatter:on

        runConformTest(sources, "[null]");
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
    public void testTypeChecked8983d() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class C {\n" +
            "  List<String> list = []\n" +
            "  void setX(String[] array) {\n" +
            "    Collections.addAll(list, array)\n" +
            "  }\n" +
            "}\n" +
            "List<String> m() { ['foo'] }\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test(Set<String> set) {\n" +
            "  def c = new C()\n" +
            "  c.x = m()" + (isAtLeastGroovy(40) ? "\n" : " as String[]\n") +
            "  c.x = set" + (isAtLeastGroovy(40) ? "\n" : " as String[]\n") +
            "  print(c.list)\n" +
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
    public void testTypeChecked9006() {
        if (Float.parseFloat(System.getProperty("java.specification.version")) > 8)
            vmArguments = new String[] {"--add-opens", "java.sql/java.sql=ALL-UNNAMED"};

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import java.sql.Timestamp\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test(Timestamp timestamp) {\n" +
            "  if (timestamp != null) {\n" + // Reference to method is ambiguous
            "    print 'works'\n" +
            "  }\n" +
            "}\n" +
            "test(new Timestamp(new Date().getTime()))\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
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
    public void testTypeChecked9074() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class Main {\n" +
            "  private static Collection<?> c = new ArrayList<String>()\n" +
            "  static main(args) {\n" +
            "    c.add(new Object())\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
        /*
        runNegativeTest(sources, "The method add(capture#1-of ?) in the type Collection<capture#1-of ?> is not applicable for the arguments (Object)");
        */
    }

    @Test
    public void testTypeChecked9074a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import java.awt.Canvas\n" +
            "abstract class Shape {\n" +
            "  abstract void draw(Canvas c)\n" +
            "}\n" +
            "class Circle extends Shape {\n" +
            "  private int x, y, radius\n" +
            "  @Override void draw(Canvas c) {}\n" +
            "}\n" +
            "class Rectangle extends Shape {\n" +
            "  private int x, y, width, height\n" +
            "  @Override void draw(Canvas c) {}\n" +
            "}\n" +
            "\n" +
            "@groovy.transform.TypeChecked\n" +
            "void addRectangle(List<? extends Shape> shapes) {\n" +
            "  shapes.add(0, new Rectangle()) // TODO: compile-time error!\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
        /*
        runNegativeTest(sources, "The method add(capture#1-of ?) in the type List<capture#1-of ?> is not applicable for the arguments (Rectangle)");
        */
    }

    @Test
    public void testTypeChecked9074b() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import java.awt.Canvas\n" +
            "abstract class Shape {\n" +
            "  abstract void draw(Canvas c)\n" +
            "}\n" +
            "class Circle extends Shape {\n" +
            "  private int x, y, radius\n" +
            "  @Override void draw(Canvas c) {}\n" +
            "}\n" +
            "class Rectangle extends Shape {\n" +
            "  private int x, y, width, height\n" +
            "  @Override void draw(Canvas c) {}\n" +
            "}\n" +
            "\n" +
            "@groovy.transform.TypeChecked\n" +
            "void addRectangle(List<? super Shape> shapes) {\n" +
            "  shapes.add(0, new Rectangle())\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testTypeChecked9074c() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Factory {\n" +
            "  public <T> T make(Class<T> type, ... args) {}\n" +
            "}\n" +
            "\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test(Factory fact, Rule rule) {\n" +
            "  Type bean = fact.make(rule.type)\n" +
            "}\n",

            "Rule.java",
            "public class Rule {\n" +
            "  public Class<? extends Type> getType() {\n" +
            "    return null;\n" +
            "  }\n" +
            "}\n",

            "Type.java",
            "public interface Type {}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testTypeChecked9074d() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Factory {\n" +
            "  public <T> T make(Class<T> type, ... args) {}\n" +
            "}\n" +
            "\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test(Factory fact, Rule rule) {\n" +
            "  Type bean = fact.make(rule.type)\n" +
            "}\n",

            "Rule.java",
            "public class Rule {\n" +
            "  public Class<? super Type> getType() {\n" +
            "    return null;\n" +
            "  }\n" +
            "}\n",

            "Type.java",
            "public interface Type {}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 7)\n" +
            "\tType bean = fact.make(rule.type)\n" +
            "\t            ^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot assign value of type java.lang.Object to variable of type Type\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked9127() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Foo {\n" +
            "  private String x = 'foo'\n" +
            "  String getX() { return x }\n" +
            "}\n" +
            "class Bar extends Foo {\n" +
            "  @groovy.transform.TypeChecked\n" +
            "  void writeField() {\n" +
            "    x = 'bar'\n" +
            "  }\n" +
            "  @Override\n" +
            "  String getX() { return 'baz' }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 8)\n" +
            "\tx = 'bar'\n" +
            "\t^\n" +
            "Groovy:[Static type checking] - Cannot set read-only property: x\n" +
            "----------\n");
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
    public void testTypeChecked9769() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "interface A {\n" +
            "  def m()\n" +
            "}\n" +
            "interface B extends A {\n" +
            "  def n()\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test(A a) {\n" +
            "  if (a instanceof B) {\n" +
            "    @groovy.transform.ASTTest(phase=INSTRUCTION_SELECTION, value={\n" +
            "      def type = node.rightExpression.objectExpression.getNodeMetaData(\n" +
            "        org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_TYPE)\n" +
            "      assert type.toString(false) == 'B'\n" + // not <UnionType:A+B>
            "    })\n" +
            "    def x = a.m()\n" +
            "    def y = a.n()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
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

    @Test(expected = AssertionError.class)
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
            "  def <V> C<V> map(F<? super T, ? extends V> func) {\n" +
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
        for (String type : new String[] {"List", "Collection", "Iterable"}) {
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
    public void testTypeChecked9915a() {
        for (String type : new String[] {"Set", "Collection", "Iterable"}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                "@groovy.transform.TypeChecked\n" +
                "class C {\n" +
                  type + "<String> strings = Collections.emptySet()\n" +
                "}\n" +
                "new C()\n",
            };
            //@formatter:on

            runConformTest(sources);
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
    public void testTypeChecked9974a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "@ToString\n" +
            "class Pogo {\n" +
            "  String foo\n" +
            "}\n" +
            "@Newify(Pogo)\n" +
            "List<Pogo> m() {\n" +
            "  [Pogo(foo:'bar'),Pogo(foo:'baz')]\n" +
            "}\n" +
            "@TypeChecked\n" +
            "def test() {\n" +
            "  m().with { list ->\n" +
            "    def other = []\n" + // <-- causes some kind of interference
            "    list.stream().filter{ it.foo.startsWith('ba') }.toList()\n" +
            "  }\n" +
            "}\n" +
            "print test()\n",
        };
        //@formatter:on

        runConformTest(sources, "[Pogo(bar), Pogo(baz)]");
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
            "  C.m(flag ? new A<>(new B()) : new A<>((B)null))\n" +
            "  C.m(flag ? new A<>(new B()) : new A<>((B)new D()))\n" + // Cannot call m(A<B>) with arguments [A<? extends B>]\n" +
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
    public void testTypeChecked10002() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  List<String> list = ['a','b',3]\n" +
            "  Deque<String> deque = ['x','y']\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 3)\n" +
            "\tList<String> list = ['a','b',3]\n" +
            "\t                    ^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Incompatible generic argument types." +
            " Cannot assign java.util.ArrayList<java.io.Serializable<? extends " + (isAtLeastGroovy(40) ? "java.io.Serializable<java.lang.String>" : "java.lang.Object") + ">> to: java.util.List<java.lang.String>\n" +
            "----------\n" +
            "2. ERROR in Main.groovy (at line 4)\n" +
            "\tDeque<String> deque = ['x','y']\n" +
            "\t^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot assign value of type java.util.List<java.lang.String> to variable of type java.util.Deque<java.lang.String>\n" +
            "----------\n");
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
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "def <T extends Number> void printNumbers(Class<T> numberType) {\n" +
            "  Collections.singleton(numberType.newInstance(42)).stream()\n" +
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
    public void testTypeChecked10055() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  new C<>().foo('x').bar('y').baz('z')\n" +
            "}\n",

            "Types.groovy",
            "abstract class A<Self extends A<Self>> {\n" +
            "  Self foo(inputs) {\n" +
            "    this\n" +
            "  }\n" +
            "}\n" +
            "abstract class B<Self extends B<Self>> extends A<Self> {\n" +
            "  Self bar(inputs) {\n" +
            "    this\n" +
            "  }\n" +
            "}\n" +
            "class C<Self extends C<Self>> extends B<Self> {\n" +
            "  Self baz(inputs) {\n" +
            "    this\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
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
            "\t                            ^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot return value of type X for closure expecting A<java.lang.Number>\n" +
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
    public void testTypeChecked10114() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C<T> {\n" +
            "  T p\n" +
            "  C(T p) {\n" +
            "    this.p = p\n" +
            "  }\n" +
            "}\n" +
            "class D {\n" +
            "  Character m() {\n" +
            "    (Character) '!'\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  print((false ? new C<D>(new D()) : new C<>(new D())).p.m())\n" +
            "  print((false ? new C< >(new D()) : new C<>(new D())).p.m())\n" +
            "  def c = (true ? new C<>(new D()) : new C<>(new D()))\n" +
            "  print c.p.m()\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "!!!");
    }

    @Test
    public void testTypeChecked10128() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  java.util.function.Function<String, Number> x = { s ->\n" +
            "    long n = 1\n" +
            "    return n\n" +
            "  }\n" +
            "}\n" +
            "test()\n",
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

        runConformTest(sources);
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

    @Test
    public void testTypeChecked10220() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C<S, T extends Number> {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "class D<T> {\n" +
            "  C<? extends T, Integer> f\n" +
            "  D(C<? extends T, Integer> p) {\n" +
            "    f = p\n" +
            "  }\n" +
            "}\n" +
            "print(new D<String>(null).f)\n",
        };
        //@formatter:on

        runConformTest(sources, "null");
    }

    @Test
    public void testTypeChecked10222() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class C<T> {\n" +
            "  def <X> X m() {\n" +
            "  }\n" +
            "  void test() {\n" +
            "    T x = m()\n" + // Cannot assign value of type #X to variable of type T
            "    print x\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "null");
    }

    @Test
    public void testTypeChecked10222a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Task {\n" +
            "  def <T> T exec(args) {\n" +
            "    args\n" +
            "  }\n" +
            "}\n" +
            "class Test {\n" +
            "  Task task\n" +
            "  @groovy.transform.TypeChecked\n" +
            "  def <T> T exec(args) {\n" +
            "    task.exec(args)\n" + // Cannot return value of type #T on method returning type T
            "  }\n" +
            "}\n" +
            "print(new Test(task: new Task()).exec('works'))\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testTypeChecked10225() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "def <T> T m(T t) {\n" +
            "  print t\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "def <N extends Number, X extends N> void test() {\n" +
            "  X x = (X) null\n" +
            "  m(false ? x : (X) null)\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "null");
    }

    @Test
    public void testTypeChecked10228() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TupleConstructor(defaults=false)\n" +
            "class C<T> {\n" +
            "  T p\n" +
            "}\n" +
            "void m(Number n) {\n" +
            "  print 'works'\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  def x = 12345\n" +
            "  m(new C<>(x).getP())\n" + // Cannot find matching method m(T)
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testTypeChecked10230() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class A {\n" +
            "  def <T extends C<Number,Number>> T m(T t) {\n" +
            "    return t\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "class B extends A {\n" +
            "  @Override\n" +
            "  def <T extends C<Number,Number>> T m(T t) {\n" +
            "    T x = null; super.m(true ? t : x)\n" +
            "  }\n" +
            "}\n" +
            "class C<X,Y> {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  new B().m(new C<>())\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked10234() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "def <T> T getBean(Class<T> beanType) {\n" +
            "  { obj, Class target -> Optional.of(obj.toString()) } as Service\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  print getBean(Service).convert(new ArrayList(), String).get()\n" +
            "}\n" +
            "test()\n",

            "Service.java",
            "import java.util.Optional;\n" +
            "import java.util.function.*;\n" +
            "@SuppressWarnings(\"rawtypes\")" +
            "public interface Service<Impl extends Service> {\n" +
            "  <T> Optional<T> convert(Object object, Class<T> targetType);\n" +
            "  <S, T> Impl addConverter(Class<S> sourceType, Class<T> targetType, Function<S, T> typeConverter);\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[]");
    }

    @Test
    public void testTypeChecked10235() {
        if (Float.parseFloat(System.getProperty("java.specification.version")) > 8)
            vmArguments = new String[] {"--add-opens", "java.base/java.util.concurrent=ALL-UNNAMED"};

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  Set<Integer> integers = java.util.concurrent.ConcurrentHashMap.newKeySet()\n" +
            "  printSet(integers)\n" + // Cannot call printSet(Set<Integer>) with arguments [KeySetView<Object,Object>]
            "}\n" +
            "void printSet(Set<Integer> integers) {\n" +
            "  println(integers)\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "[]");
    }

    @Test
    public void testTypeChecked10239() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test(List<C> list) {\n" +
            "  def result = list.findAll {\n" +
            "    it.e !in [E.X]\n" + // Cannot cast object 'true' with class 'java.lang.Boolean' to class 'Enum10239'
            "  }\n" +
            "  print result.size()\n" +
            "}\n" +
            "test([new C(E.X), new C(E.Y), new C(null)])\n",

            "Types.groovy",
            "@groovy.transform.TupleConstructor(defaults=false)\n" +
            "class C {\n" +
            "  E e\n" +
            "}\n" +
            "enum E {\n" +
            "  X, Y\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "2");
    }

    @Test
    public void testTypeChecked10239a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test(List<C> list) {\n" +
            "  def result = list.findAll {\n" +
            "    !(it.e in [E.X])\n" +
            "  }\n" +
            "  print result.size()\n" +
            "}\n" +
            "test([new C(E.X), new C(E.Y), new C(null)])\n",

            "Types.groovy",
            "@groovy.transform.TupleConstructor(defaults=false)\n" +
            "class C {\n" +
            "  E e\n" +
            "}\n" +
            "enum E {\n" +
            "  X, Y\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "2");
    }

    @Test
    public void testTypeChecked10253() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "def <N extends Number> void test(List<N> list) {\n" +
            "  List<Integer> ints = list\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
        /*
        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 3)\n" +
            "\tList<Integer> ints = list\n" +
            "\t                     ^^^^\n" +
            "Groovy:[Static type checking] - Incompatible generic argument types. Cannot assign java.util.List<N> to: java.util.List<java.lang.Integer>\n" +
            "----------\n");
        */
    }

    @Test
    public void testTypeChecked10254() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "java.util.function.Supplier<Integer> test() {\n" +
            "  { -> 42 }\n" + // should coerce without "as Supplier<Integer>"
            "}\n" +
            "print(test().get())\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testTypeChecked10267() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C<T> {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "C<? extends Object> test() {\n" +
            "  test2()\n" +
            "}\n" +
            "C<? extends Object> test2() {\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked10269() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import java.util.function.Consumer\n" +
            "void foo(Integer y) {\n" +
            "}\n" +
            "void bar(Consumer<Integer> x) {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  def baz = { Consumer<Integer> x -> }\n" +
            "  bar(this::foo)\n" +
            "  baz(this::foo)\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 10)\n" +
            "\tbaz(this::foo)\n" +
            "\t    ^^^^^^^^^\n" +
            "Groovy:The argument is a method reference, but the parameter type is not a functional interface\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked10277() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import java.util.function.*\n" +
            "Long foo(Closure<Long> c) {\n" +
            "  c()\n" +
            "}\n" +
            "Long bar(Supplier<Long> s) {\n" +
            "  s.get()\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  foo { -> false}\n" +
            "  bar { -> false};\n" +
            "  (Supplier<Long>) { -> false};\n" +
            "  { -> false} as Supplier<Long>\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 10)\n" +
            "\tfoo { -> false}\n" +
            "\t         ^^^^^\n" +
            "Groovy:[Static type checking] - Cannot return value of type boolean for closure expecting java.lang.Long\n" +
            "----------\n" +
            "2. ERROR in Main.groovy (at line 11)\n" +
            "\tbar { -> false};\n" +
            "\t         ^^^^^\n" +
            "Groovy:[Static type checking] - Cannot return value of type boolean for closure expecting java.lang.Long\n" +
            "----------\n" +
            "3. ERROR in Main.groovy (at line 12)\n" +
            "\t(Supplier<Long>) { -> false};\n" +
            "\t                      ^^^^^\n" +
            "Groovy:[Static type checking] - Cannot return value of type boolean for closure expecting java.lang.Long\n" +
            "----------\n" +
            "4. ERROR in Main.groovy (at line 13)\n" +
            "\t{ -> false} as Supplier<Long>\n" +
            "\t     ^^^^^\n" +
            "Groovy:[Static type checking] - Cannot return value of type boolean for closure expecting java.lang.Long\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked10280() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Test<T> {\n" +
            "  @groovy.transform.TypeChecked\n" +
            "  T test() {\n" +
            "    @groovy.transform.ASTTest(phase=INSTRUCTION_SELECTION, value={\n" +
            "      def type = node.getNodeMetaData(org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_TYPE)\n" +
            "      assert type.toString(false) == 'T'\n" +
            "    })\n" +
            "    def t = new Foo<T>().x.y.z\n" + // 'T' not propagated
            "  }\n" +
            "}\n" +
            "class Foo<X> {\n" +
            "  Bar<X> x = new Bar<>()\n" +
            "}\n" +
            "class Bar<T> {\n" +
            "  Baz<T> y = new Baz<>()\n" +
            "}\n" +
            "class Baz<Z> {\n" +
            "  Z z\n" +
            "}\n" +
            "new Test().test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked10283() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class A<T1, T2> {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "class B<T1 extends Number, T2 extends A<C, ? extends T1>> {\n" +
            "  protected T2 f\n" +
            "  B(T2 f) {\n" +
            "    this.f  = f\n" +
            "  }\n" +
            "}\n" +
            "class C {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  new B<Integer,A<C,Integer>>(new A<>())\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked10291() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TupleConstructor(defaults=false)\n" +
            "class A<X> {\n" +
            "  X x\n" +
            "}\n" +
            "class B<Y> {\n" +
            "  def method(Y y) { null }\n" +
            "  @groovy.transform.TypeChecked\n" +
            "  void test() {\n" +
            "    def closure = { Y why -> null }\n" +
            "    Y y = null\n" +
            "    method(new A<>(y).x)\n" + // works
            "    closure(new A<>(y).x)\n" + // fails
            "  }\n" +
            "}\n" +
            "new B().test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked10294() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "CharSequence test() {\n" +
            "  def x = 'xx'\n" +
            "  if (false) {\n" +
            "    x = null\n" +
            "  }\n" +
            "  x\n" + // Cannot return value of type Object on method returning type CharSequence
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked10295() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "List<Number> foo() {\n" +
            "  return [1,2,3]\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "Map<String, Object> bar() {\n" +
            "  return [date: new Date(), string: '']\n" +
            "}\n" +
            "foo()\n" +
            "bar()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked10306() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  byte p = 1\n" +
            "  @groovy.transform.TypeChecked\n" +
            "  def test() {\n" +
            "    byte v = 1\n" +
            "    java.util.function.Supplier<Number> s1 = { -> v }\n" +
            "    java.util.function.Supplier<Number> s2 = { -> p }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    def self = this.newInstance()\n" +
            "    print(self.test().get())\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "1");
    }

    @Test
    public void testTypeChecked10309() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TupleConstructor(defaults=false)\n" +
            "class A<T,T2> {\n" +
            "  T f1\n" +
            "  T2 f2\n" +
            "}\n" +
            "class C<T,X> {\n" +
            "  @groovy.transform.TypeChecked\n" +
            "  void test() {\n" +
            "    new A<X,T>((X)null, (T)null)\n" + // Cannot call A#<init>(X, T) with arguments [X, T]
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked10310() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TupleConstructor\n" +
            "class A<T> {\n" +
            "  T t\n" +
            "}\n" +
            "class B<T> {\n" +
            "}\n" +
            "def <T> A<T> m(T t, B<? extends T> b_of_t) {\n" +
            "  new A<>(t)\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  def x = 'x'\n" +
            "  m(x, new B<>())\n" + // Cannot call m(T,B<? extends T>) with arguments...
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked10315() {
        for (String args : new String[] {"m2(), c.m()", "c.m(), m2()"}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                "class C<T> {\n" +
                "  def T m() {\n" +
                "  }\n" +
                "}\n" +
                "def <X> X m2() {\n" +
                "}\n" +
                "def <Y> void m3(Y y1, Y y2) {\n" +
                "}\n" +
                "@groovy.transform.TypeChecked\n" +
                "def <Z> void test(C<Z> c) {\n" +
                "  m3(" + args + ")\n" +
                "}\n",
            };
            //@formatter:on

            runConformTest(sources);
        }
    }

    @Test
    public void testTypeChecked10317() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class A<T1,T2> {\n" +
            "  @groovy.transform.TypeChecked\n" +
            "  void test(T2 t2) {\n" +
            "    def a = new A<T2,T2>()\n" +
            "    a.foo(new B().bar(t2))\n" + // Cannot call A#foo(T2) with arguments [#X]
            "  }\n" +
            "  void foo(T1 t1) {\n" +
            "  }\n" +
            "}\n" +
            "class B {\n" +
            "  def <X,Y> X bar(Y y) {\n" + // X is determined by target
            "  }\n" +
            "}\n" +
            "new A<Object,Number>().test(42)\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked10320() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "interface I<T extends I> {\n" +
            "  T plus(T t_aka_i)\n" +
            "}\n" +
            "@groovy.transform.TupleConstructor(defaults=false)\n" +
            "class C implements I<C> {\n" +
            "  final BigDecimal n\n" +
            "  @Override\n" +
            "  C plus(C c) {\n" +
            "    if (!c) return this\n" +
            "    new C((n ?: 0.0) + (c.n ?: 0.0))\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "def <X extends I> X test(List<X> items) {\n" +
            "  X total = null\n" +
            "  for (it in items) {\n" +
            "    if (!total)\n" +
            "      total = it\n" +
            "    else\n" +
            "      total += it\n" + // Cannot call X#plus(T) with arguments [X]
            "  }\n" +
            "  total\n" +
            "}\n" +
            "print(test([new C(1.1), new C(2.2)]).n)\n",
        };
        //@formatter:on

        runConformTest(sources, "3.3");
    }

    @Test
    public void testTypeChecked10322() {
        assumeTrue(isAtLeastGroovy(40));

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C<T> {\n" +
            "  def <T> T m(T t) {\n" + // this T hides T from C<T>
            "    return t\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  int x = new C<String>().m(1)\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. WARNING in Main.groovy (at line 2)\n" +
            "\tdef <T> T m(T t) {\n" +
            "\t     ^\n" +
            "The type parameter T is hiding the type T\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked10323() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C<T> {\n" +
            "}\n" +
            "def <T,T> T m(C<T> c) {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  Number n = m(new C<>())\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 3)\n" +
            "\tdef <T,T> T m(C<T> c) {\n" +
            "\t       ^\n" +
            "Duplicate type parameter T\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked10324() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C<T> {\n" +
            "}\n" +
            "def <X> X m(C<X> c) {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  Set<String> x = m(new C<>())\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testTypeChecked10325() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test(Map<String,Object> map) {\n" +
            "  @groovy.transform.ASTTest(phase=INSTRUCTION_SELECTION, value={\n" +
            "    def type = node.getNodeMetaData(org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_TYPE)\n" +
            "    assert type.toString(false) == 'java.util.List<java.lang.Object>'\n" + // should not change
            "  })\n" +
            "  def values = map*.value\n" +
            "  map.entrySet()[0].value = 'baz'\n" +
            "  map*.value = 'baz!'\n" + // Cannot assign java.util.List<java.lang.String> to: java.util.List<java.lang.Object>
            "  print map\n" +
            "}\n" +
            "test(foo:'bar')\n",
        };
        //@formatter:on

        runConformTest(sources, "[foo:baz!]");
    }

    @Test
    public void testTypeChecked10326() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "@SuppressWarnings('rawtypes')\n" +
            "void test(Map map) {\n" +
            "  map*.key = null\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 4)\n" +
            "\tmap*.key = null\n" +
            "\t^^^^^^^^" + (isParrotParser() ? "" : "^") + "\n" +
            "Groovy:[Static type checking] - Cannot set read-only property: key\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked10327() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test(Map<String,? super String> map) {\n" +
            "  @groovy.transform.ASTTest(phase=INSTRUCTION_SELECTION, value={\n" +
            "    def type = node.getNodeMetaData(org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_TYPE)\n" +
            "    assert type == org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE\n" +
            "  })\n" +
            "  def foo = map.foo\n" +
            "}\n" +
            "test(foo:'bar')\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked10330() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class C<T> {\n" +
            "  T y\n" +
            "  void m(T x, java.util.function.Function<T, T> f) {\n" +
            "    print f.apply(x)\n" +
            "  }\n" +
            "  void test(T x, java.util.function.Function<T, T> f) {\n" +
            "    m(true ? x : y, f)\n" +
            "  }\n" +
            "}\n" +
            "new C<String>().test('WORKS', { it.toLowerCase() })\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testTypeChecked10336() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import java.util.function.Supplier\n" +
            "class C {\n" +
            "  Integer m() { 1 }\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  Supplier<Long> outer = () -> {\n" +
            "    Closure<Long> inner = (Object o, Supplier<Integer> s) -> 2L\n" +
            "    inner(new Object(), new C()::m)\n" +
            "  }\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 9)\n" +
            "\tinner(new Object(), new C()::m)\n" +
            "\t                    ^^^^^^^^^^\n" +
            "Groovy:The argument is a method reference, but the parameter type is not a functional interface\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked10337() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C<X,Y> {\n" +
            "  C(C<Y,? extends Y> c) {\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "def <T> void test() {\n" +
            "  new C<Number,T>((C<T,T>)null)\n" + // cannot call ctor with argument C<T,T>
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testTypeChecked10339() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "def <T> T bar(T x, T y) {\n" +
            "}\n" +
            "String foo() {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  Integer i = bar(foo(), 1)\n" +
            "}\n",
        };
        //@formatter:on

        String type = "java.io.Serializable";
        if (isAtLeastGroovy(40)) {
            type += " or java.lang.Comparable";
            if (Float.parseFloat(System.getProperty("java.specification.version")) > 11) {
                type += " or java.lang.constant.Constable or java.lang.constant.ConstantDesc";
            }
            type = "(" + type + ")";
        } else {
            type += "<? extends java.io.Serializable<java.lang.String>>";
        }
        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 7)\n" +
            "\tInteger i = bar(foo(), 1)\n" +
            "\t            ^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot assign value of type " + type + " to variable of type java.lang.Integer\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked10341() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "abstract class A {\n" +
            "  abstract def m()\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "class C extends A {\n" +
            "  @Override\n" +
            "  def m() {\n" +
            "    super.m()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 8)\n" +
            "\tsuper.m()\n" +
            "\t^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Abstract method m() cannot be called directly\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked10344() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C<X,Y> {\n" +
            "}\n" +
            "def <T extends C<? extends Number, ? extends Number>> void m(T t1, T t2) {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  m(new C<>(), new C<>())\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked10347() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  String[] one = ['foo','bar'], two = ['baz']\n" +
            "  Map<String,Integer> map = one.collectEntries{[it,1]} + two.collectEntries{[it,2]}\n" +
            "  print map\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "[foo:1, bar:1, baz:2]");
    }

    @Test
    public void testTypeChecked10347a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test(Pogo[] pogos) {\n" +
            "  List<String> strings = pogos.sort(true, new Sorter())*.x\n" + // sort(T[],boolean,Comparator<? super T>)
            "  print strings\n" +
            "}\n" +
            "test(new Pogo(x:'foo'),new Pogo(x:'bar'))\n",

            "Pogo.groovy",
            "class Pogo {\n" +
            "  String x\n" +
            "}\n",

            "Sorter.groovy",
            "class Sorter implements Comparator<Pogo>, Serializable {\n" +
            "  int compare(Pogo p1, Pogo p2) { p1.x <=> p2.x }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[bar, foo]");
    }

    @Test
    public void testTypeChecked10351() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C<T> {\n" +
            "  C(T one, D<T,? extends T> two) {\n" +
            "  }\n" +
            "}\n" +
            "class D<U,V> {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  D<Integer,? extends Integer> x = null\n" +
            "  C<Integer> y = new C<>(1,x)\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked10357() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import java.util.function.Function\n" +
            "@groovy.transform.TypeChecked\n" +
            "abstract class A {\n" +
            "  abstract long m(Function<Boolean,Integer> f = { Boolean b -> b ? +1 : -1 })\n" +
            "}\n" +
            "print new A() {\n" +
            "  @Override\n" +
            "  long m(Function<Boolean,Integer> f) {\n" +
            "    f.apply(true).longValue()\n" +
            "  }\n" +
            "}.m()\n",
        };
        //@formatter:on

        runConformTest(sources, "1");
    }

    @Test
    public void testTypeChecked10367() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TupleConstructor(defaults=false)\n" +
            "class C<X, Y extends X> {\n" + // works without Y
            "  X x\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "def <Z extends Number> void test(Z z) {\n" +
            "  z = new C<>(z).x\n" + // Cannot assign value of type Object to variable of type Z
            "}\n" +
            "test(null)\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked10368() {
        for (String bounds : new String[] {"T", "T extends Number", "T extends Object"}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                "class C<" + bounds + "> {\n" +
                "  C(p) {\n" +
                "  }\n" +
                "}\n" +
                "void m(C<Integer> c) {\n" +
                "}\n" +
                "@groovy.transform.TypeChecked\n" +
                "void test() {\n" +
                "  m(new C<>(null))\n" + // Cannot call m(C<java.lang.Integer>) with arguments [C<# extends java.lang.Number>]
                "}\n" +
                "test()\n",
            };
            //@formatter:on

            runConformTest(sources);
        }
    }

    @Test
    public void testTypeChecked10414() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class Outer {\n" +
            "  class Inner {\n" +
            "    void test() {\n" +
            "      foo = 'bar'\n" +
            "      print(foo);\n" +
            "      setFoo('baz')\n" +
            "      print(getFoo())\n" +
            "    }\n" +
            "  }\n" +
            "  def foo\n" +
            "}\n" +
            "new Outer.Inner(new Outer()).test()\n",
        };
        //@formatter:on

        runConformTest(sources, "barbaz");
    }

    @Test
    public void testTypeChecked10419() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.ToString\n" +
            "class C {\n" +
            "  def p\n" +
            "  void setP(p) {\n" +
            "    this.p = p\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test(C c) {\n" +
            "  c.p ?= 'x'\n" +
            "}\n" +
            "def c = new C()\n" +
            "test(c)\n" +
            "print c\n",
        };
        //@formatter:on

        runConformTest(sources, "C(x)");
    }

    @Test
    public void testTypeChecked10482() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Foo<X> {\n" +
            "  Foo(X x) {\n" +
            "  }\n" +
            "}\n" +
            "def <Y> Y bar() {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "def <Z> void baz() {\n" +
            "  new Foo<Z>(bar())\n" + // Cannot call Foo#<init>(Z) with arguments [#Y]
            "}\n" +
            "this.<String>baz()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked10482a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Foo<X> {\n" +
            "  Foo(X x) {\n" +
            "  }\n" +
            "}\n" +
            "static <Y> Y bar() {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "static <Z> void baz() {\n" +
            "  new Foo<Z>(bar())\n" + // Cannot call Foo#<init>(Z) with arguments [#Y]
            "}\n" +
            "Main.<String>baz()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked10482b() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "def <X> X foo(X x) {\n" +
            "}\n" +
            "def <Y> Y bar() {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "def <Z> void baz() {\n" +
            "  this.<Z>foo(bar())\n" +
            "}\n" +
            "this.<String>baz()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked10494() {
        assumeTrue(isAtLeastGroovy(40) && isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "interface I<T> {\n" +
            "  default void m(T t) {\n" +
            "    println t\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "class C implements I<String> {\n" +
            "  @Override void m(String s) {\n" +
            "    super.m(s)\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 9)\n" +
            "\tsuper.m(s)\n" +
            "\t^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Default method m(T) requires qualified super\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked10499() {
        for (String bounds : new String[] {"?", "Y", "? extends Y"}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                "class C<X> {\n" +
                "  C(X x) {\n" +
                "  }\n" +
                "}\n" +
                "class D<Y> {\n" +
                "  D(C<" + bounds + "> c, Y y) {\n" +
                "  }\n" +
                "  Y m(Y y) {\n" +
                "  }\n" +
                "}\n" +
                "@groovy.transform.TypeChecked\n" +
                "def <Z> void test(Z z = null) {\n" +
                "  new D<>(new C<Z>(z), z).m(z)\n" + // Cannot find matching method D#m(Z)
                "}\n" +
                "test()\n",
            };
            //@formatter:on

            runConformTest(sources);
        }
    }

    @Test
    public void testTypeChecked10525() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test(bean, List<Class<?>> types, Validator validator) {\n" +
            "  validator.validate(bean, types as Class<?>[])\n" +
            "}\n",

            "Validator.java",
            "import java.util.*;\n" +
            "public class Validator {\n" +
            "  @SafeVarargs public final <T> Set<Violation<T>>\n" +
            "  validate(T object, Class<?>... types) {\n" +
            "    return Collections.emptySet();\n" +
            "  }\n" +
            "}\n",

            "Violation.java",
            "public interface Violation<T> { }\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked10556() {
        for (String self : new String[] {"(B) this", "this as B"}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                "@groovy.transform.TypeChecked\n" +
                "abstract class A<B extends A<B,X>,X> {\n" +
                "  B m() {\n" +
                "   " + self + "\n" +
                "  }\n" +
                "}\n" +
                "(new A(){}).m()\n",
            };
            //@formatter:on

            runConformTest(sources);
        }
    }

    @Test
    public void testTypeChecked10576() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class C {\n" +
            "  Map<String,Object> map\n" +
            "  void test(Map<String,?> m) {\n" +
            "    map.putAll(m)\n" + // Cannot call Map#putAll(Map<? extends String, ? extends Object>) with arguments [Map<String, ?>]
            "  }\n" +
            "}\n" +
            "def obj = new C(map:[:])\n" +
            "obj.test([foo:'bar'])\n" +
            "print obj.map\n",
        };
        //@formatter:on

        runConformTest(sources, "[foo:bar]");
    }

    @Test
    public void testTypeChecked10592() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  print Face.getValue()\n" +
            "  print Face.value\n" +
            "}\n" +
            "test()\n",

            "Face.java",
            "interface Face {\n" +
            "  static String getValue() {\n" +
            "    return \"works\";\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works", "groovy.lang.MissingPropertyException: No such property: value for class: Face");
    }

    @Test
    public void testTypeChecked10592a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Impl implements Face {}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test(Impl impl) {\n" +
            "  print impl.getValue()\n" +
            "  print impl.value\n" +
            "}\n" +
            "test(new Impl())\n",

            "Face.java",
            "interface Face {\n" +
            "  default String getValue() {\n" +
            "    return \"works\";\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "worksworks");
    }

    @Test
    public void testTypeChecked10624() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class A<T> {\n" +
            "}\n" +
            "class B<T> {\n" +
            "  B(A<T> a) { }\n" +
            "}\n" +
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  B<Float> x = new B<>(new A<>())\n" + // Cannot assign B<Object> to B<Float>
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTypeChecked10651() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test(TreeNode node) {\n" +
            "  node.each { child ->\n" +
            "    test(child)\n" +
            "  }\n" +
            "}\n" +
            "test()\n",

            "TreeNode.java",
            "public abstract class TreeNode<TN extends TreeNode<?>> implements Iterable<TN> {\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }
}
