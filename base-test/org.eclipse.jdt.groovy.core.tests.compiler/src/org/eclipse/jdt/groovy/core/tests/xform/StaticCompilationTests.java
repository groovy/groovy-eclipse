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
package org.eclipse.jdt.groovy.core.tests.xform;

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;
import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isParrotParser;
import static org.junit.Assume.assumeTrue;

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test cases for {@link groovy.transform.CompileStatic} and {@link groovy.transform.CompileDynamic}.
 */
public final class StaticCompilationTests extends GroovyCompilerTestSuite {

    @Test
    public void testCompileDynamic() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  int prop\n" +
            "  int computeStatic(int input) {\n" +
            "    prop + input\n" +
            "  }\n" +
            "  @groovy.transform.CompileDynamic\n" +
            "  int computeDynamic(int input) {\n" +
            "    missing(prop, input)\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic1() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  List<Integer> ls = new ArrayList<Integer>()\n" +
            "  ls.add(123)\n" +
            "  ls.add('abc')\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 5)\n" +
            "\tls.add('abc')\n" +
            "\t^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot call java.util.ArrayList <Integer>#add(java.lang.Integer) with arguments [java.lang.String] \n" +
            "----------\n");
    }

    /**
     * Testing the code in the StaticTypeCheckingSupport.checkCompatibleAssignmentTypes.
     *
     * That method does a lot of == testing against ClassNode constants, which may not work so well for us.
     */
    @Test
    public void testCompileStatic2() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main { \n" +
            "  String getPropertyValue(String propertyName, Properties props, String defaultValue) {\n" +
            "    // First check whether we have a system property with the given name.\n" +
            "    def value = getValueFromSystemOrBuild(propertyName, props)\n" +
            "    \n" +
            "    // Return the BuildSettings value if there is one, otherwise use the default.\n" +
            "    return value != null ? value : defaultValue \n" +
            "  }\n" +
            "  \n" +
            "  def getValueFromSystemOrBuild(String propertyName, Properties props) {\n" +
            "    def value = System.getProperty(propertyName)\n" +
            "    if (value != null) return value\n" +
            "    \n" +
            "    // Now try the BuildSettings config.\n" +
            "    value = props[propertyName]\n" +
            "    return value\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic3() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test(int primitive) {\n" +
            "  Integer wrapper = primitive\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic4() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test(Number n) {\n" +
            "  Integer i = n\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 3)\n" +
            "\tInteger i = n\n" +
            "\t            ^\n" +
            "Groovy:[Static type checking] - Cannot assign value of type java.lang.Number to variable of type java.lang.Integer\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic4a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test(Number n) {\n" +
            "  Object o\n" +
            "  Integer i = (o = n)\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 4)\n" +
            "\tInteger i = (o = n)\n" +
            "\t            ^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot assign value of type java.lang.Number to variable of type java.lang.Integer\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic5() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test(Number n) {\n" +
            "  Object o = n\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic6() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test(Number n) {\n" +
            "  String s = n\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic7() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test(Number n) {\n" +
            "  boolean b = n\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic7a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test(Number n) {\n" +
            "  Boolean b = n\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test // verify generics are correct for the 'Closure<?>' as CompileStatic will attempt an exact match
    public void testCompileStatic8() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "class A {\n" +
            "  public void profile(String name, groovy.lang.Closure<?> callable) { }\n" +
            "}\n",

            "B.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class B extends A {\n" +
            "  def foo() {\n" +
            "    profile('creating plugin manager with classes') {\n" +
            "      println 'abc'\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic9() {
        //@formatter:off
        String[] sources = {
            "FlowTyping.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class FlowTyping {\n" +
            "  private Number number\n" +
            "  BigDecimal method() {\n" +
            "    return (number == null || number instanceof BigDecimal) \\\n" +
            "      ? (BigDecimal) number : new BigDecimal(number.toString())\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic10() {
        //@formatter:off
        String[] sources = {
            "BridgeMethod.groovy",
            "@groovy.transform.CompileStatic\n" +
            "int compare(Integer integer) {\n" +
            "  if (integer.compareTo(0) == 0)\n" +
            "    return 0\n" +
            "  return 1\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic11() {
        assumeTrue(isAtLeastGroovy(25));

        //@formatter:off
        String[] sources = {
            "q/Foo.groovy",
            "package q\n" +
            "class Foo {\n" +
            "  protected void m() {}\n" +
            "}\n",

            "r/Bar.groovy",
            "package r\n" +
            "@groovy.transform.CompileStatic\n" +
            "class Bar {\n" +
            "  void testM(q.Foo f) {\n" +
            "    f.m()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in r\\Bar.groovy (at line 5)\n" +
            "\tf.m()\n" +
            "\t^^^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching method q.Foo#m(). Please check if the declared type is correct and if the method exists.\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic12() {
        //@formatter:off
        String[] sources = {
            "Generics.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  def list = new LinkedList<String>([1,2,3])\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Generics.groovy (at line 3)\n" +
            "\tdef list = new LinkedList<String>([1,2,3])\n" +
            "\t           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot call java.util.LinkedList <String>#<init>(java.util.Collection <? extends java.lang.String>) with arguments [java.util.List <java.lang.Integer>] \n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic13() {
        //@formatter:off
        String[] sources = {
            "Generics.groovy",
            "void meth(Class<?> c) {\n" +
            "  print c.simpleName\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  meth(String.class)" +
            "}\n" +
            "test()",
        };
        //@formatter:on

        runConformTest(sources, "String");
    }

    @Test
    public void testCompileStatic14() {
        //@formatter:off
        String[] sources = {
            "Generics.groovy",
            "void meth(Class<? extends CharSequence> c) {\n" +
            "  print c.simpleName\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  meth(String.class)" +
            "}\n" +
            "test()",
        };
        //@formatter:on

        runConformTest(sources, "String");
    }

    @Test
    public void testCompileStatic1505() {
        //@formatter:off
        String[] sources = {
            "DynamicQuery.groovy",
            "import groovy.transform.CompileStatic\n" +
            "@CompileStatic\n" +
            "class DynamicQuery {\n" +
            "  public static void main(String[]argv) {\n" +
            "    new DynamicQuery().foo(null);\n" +
            "  }\n" +
            "  private foo(Map sumpin) {\n" +
            "    Map foo = [:]\n" +
            "    foo.collect{ Map.Entry it -> it.key }\n" +
            "    print 'abc';\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "abc");
    }

    @Test
    public void testCompileStatic1511() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "@groovy.transform.CompileStatic\n" +
            "def meth() {\n" +
            "   List<String> one = []\n" +
            "   List<String> two = []\n" +
            "   one.addAll(two)\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic1514() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "@SuppressWarnings('rawtypes')\n" +
            "@groovy.transform.CompileStatic\n" +
            "class C {\n" +
            "  def xxx(List list) {\n" +
            "    list.unique().each { }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic1515() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "import groovy.transform.CompileStatic;\n" +
            "import java.util.regex.Pattern\n" +
            "@CompileStatic\n" +
            "class C {\n" +
            "  void validate() {\n" +
            "    for (String validationKey : [:].keySet()) {\n" +
            "      String regex\n" +
            "      Pattern pattern = ~regex\n" + // NPE on this bitwise negation
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic1521() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Foo {\n" +
            "  enum Status { ON, OFF }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic6095() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@groovy.transform.CompileStatic\n" +
            "Map<String, ? extends Number> numbers() {\n" +
            "  [a: 1, b: 2, c: 3d]\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  numbers().each { String key, Number val ->\n" +
            "    print val\n" +
            "  }\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "123.0");
    }

    @Test
    public void testCompileStatic7300() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class A {\n" +
            "  private String field = 'value'\n" +
            "  String getField() { return field }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class B extends A {\n" +
            "  @Override\n" +
            "  String getField() {\n" +
            "    return super.field\n" +
            "  }\n" +
            "}\n" +
            "print new B().field\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testCompileStatic7300a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class A {\n" +
            "  private String field = 'value'\n" +
            "  String getField() { return field }\n" +
            "  void setField(String value) { field = value }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class B extends A {\n" +
            "  @Override\n" +
            "  String getField() {\n" +
            "    super.field = 'reset'\n" +
            "    return super.field\n" +
            "  }\n" +
            "}\n" +
            "print new B().field\n",
        };
        //@formatter:on

        runConformTest(sources, "reset");
    }

    @Test
    public void testCompileStatic7300b() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class A {\n" +
            "  private String field = 'value'\n" +
            "  String getField() { return field }\n" +
            "  void setField(String value) { field = value }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class B extends A {\n" +
            "  @Override\n" +
            "  String getField() {\n" +
            "    super.@field = 'reset'\n" +
            "    return super.field\n" +
            "  }\n" +
            "}\n" +
            "print new B().field\n",
        };
        //@formatter:on

        runConformTest(sources, "reset");
    }

    @Test
    public void testCompileStatic7300c() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class A {\n" +
            "  private String field = 'value'\n" +
            "  String getField() { return field }\n" +
            "  void setField(String value) { field = value }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class B extends A {\n" +
            "  @Override\n" +
            "  String getField() {\n" +
            "    super.setField('reset')\n" +
            "    return super.field\n" +
            "  }\n" +
            "}\n" +
            "print new B().field\n",
        };
        //@formatter:on

        runConformTest(sources, "reset");
    }

    @Test
    public void testCompileStatic7687() {
        assumeTrue(isAtLeastGroovy(25));

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  static class Foo {\n" +
            "    public List<Bar> bars = [new Bar()]\n" +
            "  }\n" +
            "  static class Bar {\n" +
            "    String message\n" +
            "  }\n" +
            "  void interactions(@DelegatesTo.Target Foo foo, @DelegatesTo(strategy=Closure.DELEGATE_FIRST) Closure block) {\n" +
            "    block.resolveStrategy = Closure.DELEGATE_FIRST\n" +
            "    block.delegate = foo\n" +
            "    block()\n" +
            "  }\n" +
            "  void execute(Foo foo) {\n" +
            "    interactions(foo) {\n" +
            "      bars.each { bar ->\n" + // ClassCastException
            "        bar.message = 'hello world'\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    def foo = new Foo()\n" +
            "    new Main().execute(foo)\n" +
            "    print foo.bars*.message\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[hello world]");
    }

    @Test
    public void testCompileStatic7687a() {
        assumeTrue(isAtLeastGroovy(25));

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  static class Foo {\n" +
            "    public List<Bar> bars = [new Bar()]\n" +
            "  }\n" +
            "  static class Bar {\n" +
            "    String message\n" +
            "  }\n" +
            "  void interactions(@DelegatesTo.Target Foo foo, @DelegatesTo(strategy=Closure.OWNER_FIRST) Closure block) {\n" +
            "    block.resolveStrategy = Closure.OWNER_FIRST\n" +
            "    block.delegate = foo\n" +
            "    block()\n" +
            "  }\n" +
            "  void execute(Foo foo) {\n" +
            "    interactions(foo) {\n" +
            "      bars.each { bar ->\n" + // ClassCastException
            "        bar.message = 'hello world'\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    def foo = new Foo()\n" +
            "    new Main().execute(foo)\n" +
            "    print foo.bars*.message\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[hello world]");
    }

    @Test
    public void testCompileStatic7687b() {
        assumeTrue(isAtLeastGroovy(25));

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  static class Foo {\n" +
            "    public List<String> messages = ['hello world']\n" +
            "  }\n" +
            "  void interactions(@DelegatesTo.Target Foo foo, @DelegatesTo Closure block) {\n" +
            "    block.delegate = foo\n" +
            "    block()\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    new Main().interactions(new Foo()) {\n" +
            "      messages.each { message ->\n" + // ClassCastException
            "        print message\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "hello world");
    }

    @Test
    public void testCompileStatic7691() {
        assumeTrue(isAtLeastGroovy(25));

        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@groovy.transform.CompileStatic\n" +
            "abstract class AbstractNumberWrapper<S extends Number> {\n" +
            "  protected final S number;\n" +
            "  \n" +
            "  AbstractNumberWrapper(S number) {\n" +
            "    this.number = number\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class LongWrapper<S extends Long> extends AbstractNumberWrapper<S> {\n" +
            "  LongWrapper(S longNumber) {\n" +
            "    super(longNumber)\n" +
            "  }\n" +
            "  \n" +
            "  S getValue() {\n" +
            "    return number\n" + // field of type S
            "  }\n" +
            "}\n" +
            "print new LongWrapper<Long>(42L).value\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testCompileStatic7691a() {
        assumeTrue(isAtLeastGroovy(25));

        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@groovy.transform.CompileStatic\n" +
            "abstract class AbstractNumberWrapper<S extends Number> {\n" +
            "  protected final S number;\n" +
            "  \n" +
            "  AbstractNumberWrapper(S number) {\n" +
            "    this.number = number\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class LongWrapper<S extends Long> extends AbstractNumberWrapper<S> {\n" +
            "  LongWrapper(S longNumber) {\n" +
            "    super(longNumber)\n" +
            "  }\n" +
            "  \n" +
            "  S getValue() {\n" +
            "    return { ->" +
            "      return number\n" + // field of type S from closure
            "    }()" +
            "  }\n" +
            "}\n" +
            "print new LongWrapper<Long>(42L).value\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testCompileStatic7691b() {
        assumeTrue(isAtLeastGroovy(25));

        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@groovy.transform.CompileStatic\n" +
            "abstract class AbstractNumberWrapper<S extends Number> {\n" +
            "  final S number;\n" +
            "  \n" +
            "  AbstractNumberWrapper(S number) {\n" +
            "    this.number = number\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class LongWrapper<S extends Long> extends AbstractNumberWrapper<S> {\n" +
            "  LongWrapper(S longNumber) {\n" +
            "    super(longNumber)\n" +
            "  }\n" +
            "  \n" +
            "  S getValue() {\n" +
            "    return number\n" + // property of type S
            "  }\n" +
            "}\n" +
            "print new LongWrapper<Long>(42L).value\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testCompileStatic7985() {
        //@formatter:off
        String[] sources = {
            "Pairs.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Pair<L, R> implements Serializable {\n" +
            "  public final L left\n" +
            "  public final R right\n" +
            "  \n" +
            "  private Pair(final L left, final R right) {\n" +
            "    this.left = left\n" +
            "    this.right = right\n" +
            "  }\n" +
            "  \n" +
            "  static <L, R> Pair<L, R> of(final L left, final R right) {\n" +
            "    return new Pair<>(left, right)\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "@groovy.transform.CompileStatic\n" +
            "Pair<Pair<String, Integer>, Pair<String, Integer>> doSmething() {\n" +
            "  def one = (Pair<String, Integer>) Pair.of('a', 1)\n" +
            "  def two = (Pair<String, Integer>) Pair.of('b', 2)\n" +
            "  return Pair.of(one, two)\n" +
            "}\n" +
            "\n" +
            "assert doSmething().left.left == 'a'\n" +
            "assert doSmething().left.right == 1\n" +
            "assert doSmething().right.left == 'b'\n" +
            "assert doSmething().right.right == 2\n",
        };
        //@formatter:on

        runConformTest(sources, "");
    }

    @Test @Ignore("https://issues.apache.org/jira/browse/GROOVY-7996")
    public void testCompileStatic7996() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "print new Bar().doStuff()\n",

            "Foo.groovy",
            "class Foo {\n" +
            "  def build(Closure<?> block) {\n" +
            "    return this.with(block)\n" +
            "  }\n" +
            "  def propertyMissing(String name) {\n" +
            "    return 'stuff'\n" +
            "  }\n" +
            "}\n",

            "Bar.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Bar {\n" +
            "  protected List<?> bars = []\n" +
            "  def doStuff() {\n" +
            "    new Foo().build {\n" +
            "      return bars.isEmpty()\n" + // ClassCastException: java.lang.String cannot be cast to java.util.List
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "true");
    }

    @Test
    public void testCompileStatic7996a() {
        assumeTrue(isAtLeastGroovy(30));

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "print new Bar().doStuff()\n",

            "Foo.groovy",
            "class Foo {\n" +
            "  def build(@DelegatesTo(value=Foo, strategy=Closure.DELEGATE_FIRST) Closure<?> block) {\n" +
            "    return this.with(block)\n" +
            "  }\n" +
            "  def propertyMissing(String name) {\n" +
            "    return 'stuff'\n" +
            "  }\n" +
            "}\n",

            "Bar.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Bar {\n" +
            "  protected List<?> bars = []\n" +
            "  def doStuff() {\n" +
            "    new Foo().build {\n" +
            "      return bars.toString()\n" + // ClassCastException: java.lang.String cannot be cast to java.util.List
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "stuff");
    }

    @Test
    public void testCompileStatic7996b() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "print new Bar().doStuff()\n",

            "Foo.groovy",
            "class Foo {" +
            "  def build(@DelegatesTo(value=Foo, strategy=Closure.OWNER_FIRST) Closure<?> block) {\n" +
            "    block.delegate = this\n" +
            "    return block.call()\n" +
            "  }\n" +
            "  def propertyMissing(String name) {\n" +
            "    return 'stuff'\n" +
            "  }\n" +
            "}\n",

            "Bar.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Bar {\n" +
            "  protected List<?> bars = []\n" +
            "  def doStuff() {\n" +
            "    new Foo().build {\n" +
            "      return bars.isEmpty()\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "true");
    }

    @Test
    public void testCompileStatic7996c() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "print new Bar().doStuff()\n",

            "Foo.groovy",
            "class Foo {\n" +
            "  def build(@DelegatesTo(value=Foo, strategy=Closure.DELEGATE_FIRST) Closure<?> block) {\n" +
            "    return this.with(block)\n" +
            "  }\n" +
            "  def propertyMissing(String name) {\n" +
            "    return 'stuff'\n" +
            "  }\n" +
            "}\n",

            "Bar.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Bar {\n" +
            "  protected List<?> bars = []\n" +
            "  def doStuff() {\n" +
            "    new Foo().build {\n" +
            "      return owner.bars.isEmpty()\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "true");
    }

    @Test
    public void testCompileStatic7996d() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "print new Bar().doStuff()\n",

            "Foo.groovy",
            "class Foo {\n" +
            "  def build(@DelegatesTo(value=Foo, strategy=Closure.DELEGATE_FIRST) Closure<?> block) {\n" +
            "    return this.with(block)\n" +
            "  }\n" +
            "  def propertyMissing(String name) {\n" +
            "    return 'stuff'\n" +
            "  }\n" +
            "}\n",

            "Bar.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Bar {\n" +
            "  protected List<?> bars = []\n" +
            "  def doStuff() {\n" +
            "    new Foo().build {\n" +
            "      return thisObject.bars.isEmpty()\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "true");
    }

    @Test
    public void testCompileStatic8337() {
        //@formatter:off
        String[] sources = {
            "FlowTyping.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class FlowTyping {\n" +
            "  private Number number;\n" +
            "  private BigDecimal method() {\n" +
            "    return (number == null || number instanceof BigDecimal) ? number : new BigDecimal(number.toString());\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic8342() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Foo {\n" +
            "  protected <T> List<T[]> bar(T thing) {\n" +
            "    return Collections.emptyList()\n" +
            "  }\n" +
            "  protected void baz() {\n" +
            "    List<Integer[]> list = bar(1)\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test @Ignore("https://issues.apache.org/jira/browse/GROOVY-8409")
    public void testCompileStatic8409() {
        for (char t : new char[] {'R', 'S', 'T', 'U'}) { // BiFunction uses R, T and U
            //@formatter:off
            String[] sources = {
                "Script.groovy",
                "@groovy.transform.CompileStatic\n" +
                "static <" + t + "> " + t + " meth(java.util.function.BiFunction<Date, URL, " + t + "> func) {\n" +
                "  " + t + " result = func.apply(new Date(), new URL('http://www.example.com'))\n" +
                "  return result\n" +
                "}\n" +
                "meth({ Date d, URL u -> 'result' })",
            };
            //@formatter:on

            runNegativeTest(sources, "");
        }
    }

    @Test
    public void testCompileStatic8509() {
        //@formatter:off
        String[] sources = {
            "p/Foo.groovy",
            "package p\n" +
            "class Foo {\n" +
            "  protected void m() {}\n" +
            "}\n",

            "p/Bar.groovy",
            "package p\n" +
            "@groovy.transform.CompileStatic\n" +
            "class Bar {\n" +
            "  void testM(Foo f) {\n" +
            "    f.m()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic8562() {
        assumeTrue(isAtLeastGroovy(25));

        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class C {\n" +
            "  void test() {\n" +
            "    print exec(new D()) {\n" +
            "      return x\n" +
            "    }\n" +
            "  }\n" +
            "  public <T> T exec(D d, @DelegatesTo(value=D, strategy=Closure.DELEGATE_ONLY) Closure<T> block) {\n" +
            "    block.resolveStrategy = Closure.DELEGATE_ONLY\n" +
            "    block.delegate = d\n" +
            "    block()\n" +
            "  }\n" +
            "  def x = 'owner'\n" +
            "}\n" +
            "class D {\n" +
            "  def x = 'delegate'\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "delegate");
    }

    @Test
    public void testCompileStatic8609() {
        assumeTrue(isAtLeastGroovy(25));

        //@formatter:off
        String[] sources = {
            "A.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class A<T extends List<E>, E extends Map<String, Integer>> {\n" + // upper bound with generics
            "  E getFirstRecord(T recordList) {\n" +
            "    return recordList.get(0)\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    def list = new ArrayList<HashMap<String, Integer>>()\n" +
            "    def record = new HashMap<String, Integer>()\n" +
            "    list.add(record)\n" +
            "    def a = new A<ArrayList<HashMap<String, Integer>>, HashMap<String, Integer>>()\n" +
            "    assert record.is(a.getFirstRecord(list))\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic8609a() {
        assumeTrue(isAtLeastGroovy(25));

        //@formatter:off
        String[] sources = {
            "A.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class A<T extends List<E>, E extends Map> {\n" + // upper bound without generics
            "  E getFirstRecord(T recordList) {\n" +
            "    return recordList.get(0)\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    def list = new ArrayList<HashMap<String, Integer>>()\n" +
            "    def record = new HashMap<String, Integer>()\n" +
            "    list.add(record)\n" +
            "    def a = new A<ArrayList<HashMap<String, Integer>>, HashMap<String, Integer>>()\n" +
            "    assert record.is(a.getFirstRecord(list))\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic8609b() {
        assumeTrue(isAtLeastGroovy(25));

        //@formatter:off
        String[] sources = {
            "A.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class A<T extends List<E>, E> {\n" + // no upper bound
            "  E getFirstRecord(T recordList) {\n" +
            "    return recordList.get(0)\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    def list = new ArrayList<HashMap<String, Integer>>()\n" +
            "    def record = new HashMap<String, Integer>()\n" +
            "    list.add(record)\n" +
            "    def a = new A<ArrayList<HashMap<String, Integer>>, HashMap<String, Integer>>()\n" +
            "    assert record.is(a.getFirstRecord(list))\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic8609c() {
        assumeTrue(isAtLeastGroovy(25));

        //@formatter:off
        String[] sources = {
            "A.groovy",
            "@groovy.transform.CompileStatic\n" +
            "public class A<T extends List<E>, E extends Map<String, Integer>> {\n" + // upper bound with generics
            "  E getFirstRecord(T recordList) {\n" +
            "    return recordList.get(0)\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    def list = new ArrayList<TreeMap<String, Integer>>()\n" +
            "    def record = new TreeMap<String, Integer>()\n" +
            "    list.add(record)\n" +
            "    def a = new A<ArrayList<HashMap<String, Integer>>, HashMap<String, Integer>>()\n" +
            "    assert record.is(a.getFirstRecord(list))\n" + // TreeMap argument, HashMap parameter
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in A.groovy (at line 11)\n" +
            "\tassert record.is(a.getFirstRecord(list))\n" +
            "\t                 ^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching method A#getFirstRecord(java.util.ArrayList <TreeMap>). Please check if the declared type is correct and if the method exists.\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic8609d() {
        assumeTrue(isAtLeastGroovy(25));

        //@formatter:off
        String[] sources = {
            "A.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class A<T extends List<E>, E extends Map<String, Integer>> {\n" +
            "  E getFirstRecord(T recordList) {\n" +
            "    return recordList.get(0)\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    def list = new ArrayList<HashMap<String, Long>>()\n" +
            "    def record = new HashMap<String, Long>()\n" +
            "    list.add(record)\n" +
            "    def a = new A<ArrayList<HashMap<String, Integer>>, HashMap<String, Integer>>()\n" +
            "    assert record.is(a.getFirstRecord(list))\n" + // Long argument, Integer parameter
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in A.groovy (at line 11)\n" +
            "\tassert record.is(a.getFirstRecord(list))\n" +
            "\t                 ^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching method A#getFirstRecord(java.util.ArrayList <HashMap>). Please check if the declared type is correct and if the method exists.\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic8609e() {
        assumeTrue(isAtLeastGroovy(25));

        //@formatter:off
        String[] sources = {
            "A.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class A<T extends List<E>, E extends Map<String, Integer>> {\n" +
            "  E getFirstRecord(T recordList) {\n" +
            "    return recordList.get(0)\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    def list = new ArrayList<HashMap<StringBuffer, Integer>>()\n" +
            "    def record = new HashMap<StringBuffer, Integer>()\n" +
            "    list.add(record)\n" +
            "    def a = new A<ArrayList<HashMap<String, Integer>>, HashMap<String, Integer>>()\n" +
            "    assert record.is(a.getFirstRecord(list))\n" + // StringBuffer argument, String parameter
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in A.groovy (at line 11)\n" +
            "\tassert record.is(a.getFirstRecord(list))\n" +
            "\t                 ^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching method A#getFirstRecord(java.util.ArrayList <HashMap>). Please check if the declared type is correct and if the method exists.\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic8638() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Foo {\n" +
            "  protected void bar(Multimap<String, Integer> mmap) {\n" +
            "    Map<String, Collection<Integer>> map = mmap.asMap()\n" +
            "    Set<Map.Entry<String, Collection<Integer>>> entrySet = map.entrySet()\n" +
            "    Iterator<Map.Entry<String, Collection<Integer>>> iter = entrySet.iterator()\n" +
            "    while (iter.hasNext()) {\n" +
            "      Map.Entry<String, Collection<Integer>> group = iter.next()\n" +
            "      Collection<Integer> values = group.value\n" +
            "    }\n" +
            "  }\n" +
            "}\n",

            "Multimap.java",
            "import java.util.*;\n" +
            "interface Multimap<K, V> {\n" +
            "  Map<K, Collection<V>> asMap();\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic8686() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@groovy.transform.TypeChecked\n" +
            "def meth(obj) {\n" +
            "  boolean isA = (obj instanceof String && obj.equalsIgnoreCase('a'))\n" +
            "  obj.toLowerCase()\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in Script.groovy (at line 4)\n" +
            "\tobj.toLowerCase()\n" +
            "\t^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching method java.lang.Object#toLowerCase(). Please check if the declared type is correct and if the method exists.\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic8686a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@groovy.transform.TypeChecked\n" +
            "def meth(obj) {\n" +
            "  def str = obj instanceof String ? obj : obj.toString()\n" +
            "  obj.toLowerCase()\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in Script.groovy (at line 4)\n" +
            "\tobj.toLowerCase()\n" +
            "\t^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching method java.lang.Object#toLowerCase(). Please check if the declared type is correct and if the method exists.\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic8686b() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@groovy.transform.TypeChecked\n" +
            "def meth(obj) {\n" +
            "  def str\n" +
            "  if (obj instanceof String) {\n" +
            "    str = obj\n" +
            "  } else {\n" +
            "    str = obj.toString()\n" +
            "  }\n" +
            "  obj.toLowerCase()\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in Script.groovy (at line 9)\n" +
            "\tobj.toLowerCase()\n" +
            "\t^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching method java.lang.Object#toLowerCase(). Please check if the declared type is correct and if the method exists.\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic8839() {
        //@formatter:off
        String[] sources = {
            "p/Main.groovy",
            "package p\n" +
            "import q.ResultHandle\n" +
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  protected Map<String, ResultHandle[]> getResultsByType() {\n" +
            "    Map<String, ResultHandle[]> resultsByType = [:]\n" +
            "    // populate resultsByType\n" +
            "    return resultsByType\n" +
            "  }\n" +
            "}\n",

            "q/ResultHandle.java",
            "package q;\n" +
            "public class ResultHandle {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic8873() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Foo {\n" +
            "  String foo = 'foo'\n" +
            "  String foom() { 'foom' }\n" +
            "}\n" +
            "class Bar {\n" +
            "  String bar = 'bar'\n" +
            "  String barm() { 'barm' }\n" +
            "}\n" +
            "class Baz {\n" +
            "  String baz = 'baz'\n" +
            "  String bazm() { 'bazm' }\n" +
            "}\n" +
            "String other() { 'other' }\n" +
            "\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  new Foo().with {\n" +
            "    assert foo == 'foo'\n" +
            "    assert foom() == 'foom'\n" +
            "    assert other() == 'other'\n" +
            "    new Bar().with {\n" +
            "      assert foo == 'foo'\n" +
            "      assert bar == 'bar'\n" +
            "      assert foom() == 'foom'\n" +
            "      assert barm() == 'barm'\n" +
            "      assert other() == 'other'\n" +
            "      new Baz().with {\n" +
            "        assert foo == 'foo'\n" +
            "        assert bar == 'bar'\n" +
            "        assert baz == 'baz'\n" +
            "        assert foom() == 'foom'\n" +
            "        assert barm() == 'barm'\n" +
            "        assert bazm() == 'bazm'\n" +
            "        assert other() == 'other'\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "");
    }

    @Test @Ignore("https://issues.apache.org/jira/browse/GROOVY-8946")
    public void testCompileStatic8946() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@GrabResolver(name='grails', root='https://repo.grails.org/grails/core')\n" +
            "@Grapes([\n" +
            "  @Grab('javax.servlet:javax.servlet-api:3.0.1'),\n" +
            "  @Grab('org.grails.plugins:converters:3.3.+'),\n" +
            "  @Grab('org.grails:grails-web:3.3.+')\n" +
            "])\n" +
            "@GrabExclude('org.codehaus.groovy:*')\n" +
            "import static grails.converters.JSON.parse\n" +
            "\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  def json = parse('[{\"k\":1},{\"k\":2}]')\n" + // returns org.grails.web.json.JSONElement
            "  def vals = json['k']\n" +
            "  assert vals == [1,2]\n" +
            "  boolean result = 'k'.tokenize('.').every { token ->\n" + // 'k' represents a path like 'a.b.c.d'
            "    json = json[token]\n" + // GroovyCastException: Cannot cast object '[1, 2]' with class 'java.util.ArrayList' to class 'org.grails.web.json.JSONElement'
            "  }\n" +
            "  assert result\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "");
    }

    @Test
    public void testCompileStatic8955() {
        assumeTrue(isAtLeastGroovy(25));

        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Property {\n" +
            "  String generator\n" +
            "}\n" +
            "\n" +
            "interface PMapping<T extends Property> {\n" +
            "  T getMappedForm()\n" +
            "}\n" +
            "\n" +
            "interface PProperty {\n" +
            "  PMapping getMapping()\n" +
            "}\n" +
            "\n" +
            "@groovy.transform.CompileStatic\n" +
            "class GPEntity {\n" +
            "  def method() {\n" +
            "    PProperty identity = getIdentity()\n" +
            "    String generatorType = identity.getMapping().getMappedForm().getGenerator()\n" +
            "  }\n" +
            "  \n" +
            "  PProperty getIdentity() {\n" +
            "    new PProperty() {\n" +
            "      PMapping getMapping() {\n" +
            "        new PMapping() {\n" +
            "          def getMappedForm() {\n" + // replace "def" with "Property"
            "            new Property() {\n" +
            "              String getGenerator() { 'foo' }\n" +
            "            }\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "new GPEntity().method()\n",
        };
        //@formatter:on

        runConformTest(sources, "");
    }

    @Test
    public void testCompileStatic8978() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class DelegatesToMap {\n" +
            "  @Delegate protected Map<String, Object> target = new HashMap<>()\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class TaskConfig extends DelegatesToMap implements Cloneable {\n" +
            "  @Override\n" +
            "  TaskConfig clone() {\n" +
            "    def copy = (TaskConfig) super.clone()\n" +
            "    copy.target = new HashMap<>(this.target)\n" + // NPE
            "    return copy\n" +
            "  }\n" +
            "}\n" +
            "new TaskConfig().clone()\n",
        };
        //@formatter:on

        runConformTest(sources, "");
    }

    @Test
    public void testCompileStatic9005() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import java.util.concurrent.Callable\n" +
            "@groovy.transform.CompileStatic\n" +
            "class Main extends A1 {" +
            "  Map<String, Object> getCommands() {\n" +
            "    Map<String, Object> commands = getMap()\n" +
            "    commands.put('greet', new Callable<String>() {\n" +
            "      @Override\n" +
            "      String call() {\n" +
            "        def string = getObj().toString()\n" +
            "        return string\n" +
            "      }\n" +
            "    })\n" +
            "    return commands\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    def greeter = new Main().commands['greet']\n" +
            "    if (greeter instanceof Callable) {\n" +
            "      print greeter.call()\n" +
            "    }\n" +
            "  }\n" +
            "}\n",

            "A1.java",
            "abstract class A1 extends A2 {\n" +
            "  public Object getObj() {\n" +
            "    return \"hi!\";\n" +
            "  }\n" +
            "}\n",

            "A2.groovy",
            "abstract class A2 {\n" +
            "  Map<String, Object> getMap() {\n" +
            "    [:]\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "hi!");
    }

    @Test
    public void testCompileStatic9007() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  private enum E {\n" +
            "    ONE(1), TWO(2)\n" +
            "    public final int n\n" +
            "    E(int n) { this.n = n }\n" +
            "    static E valueOf(int n) {\n" +
            "      values().find { it.n == n }\n" + // "it" must not be converted to "owner.it"
            "    }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    print E.valueOf(2)\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "TWO");
    }

    @Test
    public void testCompileStatic9007a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  private enum E {\n" +
            "    ONE(1), TWO(2)\n" +
            "    private final int n\n" +
            "    E(int n) { this.n = n }\n" +
            "    static E valueOf(int n) {\n" +
            "      values().find { it.n == n }\n" +
            "    }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    print E.valueOf(2)\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "TWO");
    }

    @Test
    public void testCompileStatic9007b() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  private enum E {\n" +
            "    ONE(1), TWO(2)\n" +
            "    private final int n\n" +
            "    E(int n) { this.n = n }\n" +
            "    static E valueOf(int n) {\n" +
            "      values().find { it.@n == n }\n" +
            "    }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    print E.valueOf(2)\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "TWO");
    }

    @Test
    public void testCompileStatic9007or9043_enumConstToPrivate1() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "enum E {\n" +
            "  ONE, TWO\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  print E.ONE.name\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "", "groovy.lang.MissingPropertyException: No such property: name for class: E");
        /* TODO: https://issues.apache.org/jira/browse/GROOVY-9093
        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 6)\n" +
            "\tE.ONE.name\n" +
            "\t^^^^^\n" +
            "Groovy:Access to E#name is forbidden @ line 6, column 3.\n" +
            "----------\n");
        */
    }

    @Test
    public void testCompileStatic9007or9043_enumConstToPrivate2() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "enum E {\n" +
            "  ONE, TWO\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  print E.ONE.ordinal\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "", "groovy.lang.MissingPropertyException: No such property: ordinal for class: E");
        /* TODO: https://issues.apache.org/jira/browse/GROOVY-9093
        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 6)\n" +
            "\tE.ONE.ordinal\n" +
            "\t^^^^^\n" +
            "Groovy:Access to E#ordinal is forbidden @ line 6, column 3.\n" +
            "----------\n");
        */
    }

    @Test
    public void testCompileStatic9043_nonStaticInnerToPackage() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "@CompileStatic class Main {\n" +
            "  @PackageScope static final String VALUE = 'value'\n" +
            "  class Inner {\n" +
            "    void meth() { print VALUE }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    new Inner(new Main()).meth()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testCompileStatic9043_nonStaticInnerToPackage2() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "@CompileStatic class Main {\n" +
            "  @PackageScope String value = 'value'\n" + // instance field
            "  class Inner {\n" +
            "    void meth() { print value }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    new Inner(new Main()).meth()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testCompileStatic9043_nonStaticInnerToProtected() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "@CompileStatic class Main {\n" +
            "  protected static final String VALUE = 'value'\n" +
            "  class Inner {\n" +
            "    void meth() { print VALUE }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    new Inner(new Main()).meth()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testCompileStatic9043_nonStaticInnerToPublic() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "@CompileStatic class Main {\n" +
            "  public static final String VALUE = 'value'\n" +
            "  class Inner {\n" +
            "    void meth() { print VALUE }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    new Inner(new Main()).meth()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testCompileStatic9043_nonStaticInnerToPrivate() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "@CompileStatic class Main {\n" +
            "  private static final String VALUE = 'value'\n" +
            "  class Inner {\n" +
            "    void meth() { print VALUE }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    new Inner(new Main()).meth()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testCompileStatic9043_staticInnerToPackage() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "@CompileStatic class Main {\n" +
            "  @PackageScope static final String VALUE = 'value'\n" +
            "  static class Inner {\n" +
            "    void meth() { print VALUE }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    new Inner().meth()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testCompileStatic9043_staticInnerToPackage2() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "@CompileStatic class Main {\n" +
            "  @PackageScope String value = 'value'\n" + // instance field
            "  static class Inner {\n" +
            "    void meth() {\n" +
            "      print value\n" +
            "    }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    new Inner().meth()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "", "groovy.lang.MissingPropertyException: No such property: value for class: Main");
        /* TODO: GROOVY-9093
        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 6)\n" +
            "\tprint value\n" +
            "\t      ^^^^^\n" +
            "Groovy:...\n" +
            "----------\n");
        */
    }

    @Test
    public void testCompileStatic9043_staticInnerToProtected() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "@CompileStatic class Main {\n" +
            "  protected static final String VALUE = 'value'\n" +
            "  static class Inner {\n" +
            "    void meth() { print VALUE }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    new Inner().meth()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testCompileStatic9043_staticInnerToPublic() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "@CompileStatic class Main {\n" +
            "  public static final String VALUE = 'value'\n" +
            "  static class Inner {\n" +
            "    void meth() { print VALUE }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    new Inner().meth()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testCompileStatic9043_staticInnerToPrivate() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "@CompileStatic class Main {\n" +
            "  private static final String VALUE = 'value'\n" +
            "  static class Inner {\n" +
            "    void meth() { print VALUE }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    new Inner().meth()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testCompileStatic9043_selfToPackage() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "@CompileStatic class Main {\n" +
            "  @PackageScope static final String VALUE = 'value'\n" +
            "  static main(args) {\n" +
            "    print VALUE\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testCompileStatic9043_selfToPackage2() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "@CompileStatic class Main {\n" +
            "  @PackageScope String value = 'value'\n" + // instance field
            "  static main(args) {\n" +
            "    print new Main().value\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testCompileStatic9043_selfToProtected() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "@CompileStatic class Main {\n" +
            "  protected static final String VALUE = 'value'\n" +
            "  static main(args) {\n" +
            "    print VALUE\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testCompileStatic9043_selfToPublic() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "@CompileStatic class Main {\n" +
            "  public static final String VALUE = 'value'\n" +
            "  static main(args) {\n" +
            "    print VALUE\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testCompileStatic9043_selfToPrivate() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "@CompileStatic class Main {\n" +
            "  private static final String VALUE = 'value'\n" +
            "  static main(args) {\n" +
            "    print VALUE\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testCompileStatic9043_peerToPackage() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "@CompileStatic class Main {\n" +
            "  @PackageScope static final String VALUE = 'value'\n" +
            "  static main(args) {\n" +
            "    new Peer().meth()\n" +
            "  }\n" +
            "}\n" +
            "@CompileStatic class Peer {\n" +
            "  void meth() {\n" +
            "    print Main.VALUE\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testCompileStatic9043_peerToPackage2() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "@CompileStatic class Main {\n" +
            "  @PackageScope String value = 'value'\n" + // instance field
            "  static main(args) {\n" +
            "    new Peer().meth()\n" +
            "  }\n" +
            "}\n" +
            "@CompileStatic class Peer {\n" +
            "  void meth() {\n" +
            "    print new Main().value\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testCompileStatic9043_peerToPackageX() {
        //@formatter:off
        String[] sources = {
            "p/Main.groovy",
            "package p\n" +
            "import groovy.transform.*\n" +
            "class Main {\n" +
            "  @PackageScope static final String VALUE = 'value'\n" +
            "  static main(args) {\n" +
            "    new Peer().meth()\n" +
            "  }\n" +
            "}\n",

            "q/More.groovy",
            "package q\n" +
            "class More extends p.Main {}\n",

            "p/Peer.groovy",
            "package p\n" +
            "import groovy.transform.*\n" +
            "@CompileStatic class Peer {\n" +
            "  void meth() {\n" +
            "    print q.More.VALUE\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testCompileStatic9043_peerToProtected() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "@CompileStatic class Main {\n" +
            "  protected static final String VALUE = 'value'\n" +
            "  static main(args) {\n" +
            "    new Peer().meth()\n" +
            "  }\n" +
            "}\n" +
            "@CompileStatic class Peer {\n" +
            "  void meth() {\n" +
            "    print Main.VALUE\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testCompileStatic9043_peerToPublic() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "@CompileStatic class Main {\n" +
            "  public static final String VALUE = 'value'\n" +
            "  static main(args) {\n" +
            "    new Peer().meth()\n" +
            "  }\n" +
            "}\n" +
            "@CompileStatic class Peer {\n" +
            "  void meth() {\n" +
            "    print Main.VALUE\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testCompileStatic9043_peerToPrivate() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "@CompileStatic class Main {\n" +
            "  private static final String VALUE = 'value'\n" +
            "}\n" +
            "@CompileStatic class Peer {\n" +
            "  void meth() {\n" +
            "    print Main.VALUE\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 7)\n" +
            "\tprint Main.VALUE\n" +
            "\t      ^^^^\n" +
            "Groovy:Access to Main#VALUE is forbidden\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic9043_subToPackage() {
        //@formatter:off
        String[] sources = {
            "p/Main.groovy",
            "package p\n" +
            "class Main {\n" +
            "  @groovy.transform.PackageScope static final String VALUE = 'value'\n" +
            "}\n",

            "q/More.groovy",
            "package q\n" +
            "@groovy.transform.CompileStatic\n" +
            "class More extends p.Main {\n" +
            "  static void meth() {\n" +
            "    print VALUE\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in q\\More.groovy (at line 5)\n" +
            "\tprint VALUE\n" +
            "\t      ^^^^^\n" +
            "Groovy:Access to q.More#VALUE is forbidden\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic9043_subToPackage2() {
        //@formatter:off
        String[] sources = {
            "p/Main.groovy",
            "package p\n" +
            "class Main {\n" +
            "  @groovy.transform.PackageScope static final String VALUE = 'value'\n" +
            "}\n",

            "q/More.groovy",
            "package q\n" +
            "@groovy.transform.CompileStatic\n" +
            "class More extends p.Main {\n" +
            "  void meth() {\n" + // non-static
            "    print VALUE\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "");
        /* TODO: https://issues.apache.org/jira/browse/GROOVY-9093
        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in q\\More.groovy (at line 5)\n" +
            "\tprint VALUE\n" +
            "\t      ^^^^^\n" +
            "Groovy:Access to q.More#VALUE is forbidden @ line 5, column 11.\n" +
            "----------\n");
        */
    }

    @Test
    public void testCompileStatic9043_subToPackageX() {
        //@formatter:off
        String[] sources = {
            "p/Main.groovy",
            "package p\n" +
            "class Main {\n" +
            "  @groovy.transform.PackageScope static final String VALUE = 'value'\n" +
            "}\n",

            "p/More.groovy",
            "package p\n" +
            "class More extends Main {}\n",

            "q/Test.groovy",
            "package q\n" +
            "@groovy.transform.CompileStatic\n" +
            "class Test {\n" +
            "  void meth() {\n" +
            "    p.More.VALUE\n" + // Main and More are in same package, Test is not
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in q\\Test.groovy (at line 5)\n" +
            "\tp.More.VALUE\n" +
            "\t^^^^^^\n" +
            "Groovy:Access to p.More#VALUE is forbidden\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic9043_subToProtected() {
        //@formatter:off
        String[] sources = {
            "p/Main.groovy",
            "package p\n" +
            "class Main {\n" +
            "  protected static final String VALUE = 'value'\n" +
            "  static main(args) {\n" +
            "    new q.More().meth()\n" +
            "  }\n" +
            "}\n",

            "q/More.groovy",
            "package q\n" +
            "@groovy.transform.CompileStatic\n" +
            "class More extends p.Main {\n" +
            "  void meth() {\n" +
            "    print VALUE\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testCompileStatic9043_subToPublic() {
        //@formatter:off
        String[] sources = {
            "p/Main.groovy",
            "package p\n" +
            "class Main {\n" +
            "  public static final String VALUE = 'value'\n" +
            "  static main(args) {\n" +
            "    new q.More().meth()\n" +
            "  }\n" +
            "}\n",

            "q/More.groovy",
            "package q\n" +
            "@groovy.transform.CompileStatic\n" +
            "class More extends p.Main {\n" +
            "  void meth() {\n" +
            "    print VALUE\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testCompileStatic9043_subToPrivate() {
        //@formatter:off
        String[] sources = {
            "p/Main.groovy",
            "package p\n" +
            "class Main {\n" +
            "  private static final String VALUE = 'value'\n" +
            "}\n",

            "q/More.groovy",
            "package q\n" +
            "@groovy.transform.CompileStatic\n" +
            "class More extends p.Main {\n" +
            "  static void meth() {\n" +
            "    print VALUE\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in q\\More.groovy (at line 5)\n" +
            "\tprint VALUE\n" +
            "\t      ^^^^^\n" +
            "Groovy:Access to q.More#VALUE is forbidden\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic9043_subToPrivate2() {
        //@formatter:off
        String[] sources = {
            "p/Main.groovy",
            "package p\n" +
            "class Main {\n" +
            "  private static final String VALUE = 'value'\n" +
            "}\n",

            "q/More.groovy",
            "package q\n" +
            "@groovy.transform.CompileStatic\n" +
            "class More extends p.Main {\n" +
            "  void meth() {\n" + // non-static
            "    print VALUE\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "");
        /* TODO: https://issues.apache.org/jira/browse/GROOVY-9093
        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in q\\More.groovy (at line 5)\n" +
            "\tprint VALUE\n" +
            "\t      ^^^^^\n" +
            "Groovy:Access to q.More#VALUE is forbidden @ line 5, column 11.\n" +
            "----------\n");
        */
    }

    @Test
    public void testCompileStatic9058() {
        //@formatter:off
        String[] sources = {
            "p/Main.groovy",
            "package p\n" +
            "class Main {\n" +
            "  @groovy.transform.CompileStatic\n" +
            "  void meth() {\n" +
            "    List<Object[]> rows = new Foo().bar()\n" +
            "    rows.each { row ->\n" + // should be Object[]
            "      def col = row[0]\n" +
            "    }\n" +
            "  }\n" +
            "}\n",

            "p/Foo.java",
            "package p;\n" +
            "public class Foo {\n" +
            "  @SuppressWarnings(\"rawtypes\")\n" +
            "  public java.util.List bar() { return null; }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic9063() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  protected String message = 'hello'\n" +
            "  \n" +
            "  void meth() {\n" +
            "    { ->\n" +
            "      { ->\n" +
            "        print message.length()\n" +
            "      }.call()\n" +
            "    }.call()\n" +
            "  }\n" +
            "  \n" +
            "  static main(args) {\n" +
            "    new Main().meth()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "5");
    }

    @Test @Ignore("https://issues.apache.org/jira/browse/GROOVY-9074")
    public void testCompileStatic9074() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  private static Collection<?> c = new ArrayList<String>()\n" +
            "  static main(args) {\n" +
            "    c.add(new Object())\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "The method add(capture#1-of ?) in the type Collection<capture#1-of ?> is not applicable for the arguments (Object)");
    }

    @Test @Ignore("https://issues.apache.org/jira/browse/GROOVY-9074")
    public void testCompileStatic9074a() {
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
            "@groovy.transform.CompileStatic\n" +
            "void addRectangle(List<? extends Shape> shapes) {\n" +
            "  shapes.add(0, new Rectangle()) // TODO: compile-time error!\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "The method add(capture#1-of ?) in the type List<capture#1-of ?> is not applicable for the arguments (Rectangle)");
    }

    @Test
    public void testCompileStatic9074b() {
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
            "@groovy.transform.CompileStatic\n" +
            "void addRectangle(List<? super Shape> shapes) {\n" +
            "  shapes.add(0, new Rectangle())\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic9074c() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Factory {\n" +
            "  public <T> T make(Class<T> type, ... args) {}\n" +
            "}\n" +
            "\n" +
            "@groovy.transform.CompileStatic\n" +
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

    @Test @Ignore("https://issues.apache.org/jira/browse/GROOVY-9074")
    public void testCompileStatic9074d() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Factory {\n" +
            "  public <T> T make(Class<T> type, ... args) {}\n" +
            "}\n" +
            "\n" +
            "@groovy.transform.CompileStatic\n" +
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

        runNegativeTest(sources, "cannot convert from capture#1-of ? super Type to Type");
    }

    @Test
    public void testCompileStatic9086() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class C1 {\n" +
            "  void m1() {\n" +
            "    print 'outer delegate'\n" +
            "  }\n" +
            "}\n" +
            "class C2 {\n" +
            "  void m2() {\n" +
            "    print 'inner delegate'\n" +
            "  }\n" +
            "}\n" +
            "void outer(@DelegatesTo(value = C1, strategy = Closure.DELEGATE_FIRST) Closure block) {\n" +
            "  block.delegate = new C1()\n" +
            "  block.call()\n" +
            "}\n" +
            "void inner(@DelegatesTo(value = C2, strategy = Closure.DELEGATE_FIRST) Closure block) {\n" +
            "  block.delegate = new C2()\n" +
            "  block.call()\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  outer {\n" +
            "    inner {\n" +
            "      m1()\n" +
            "      print ' '\n" +
            "      m2()\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "outer delegate inner delegate");
    }

    @Test
    public void testCompileStatic9086a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class C1 {\n" +
            "  void m1() {\n" +
            "    print 'outer delegate'\n" +
            "  }\n" +
            "}\n" +
            "class C2 {\n" +
            "  void m2() {\n" +
            "    print 'inner delegate'\n" +
            "  }\n" +
            "}\n" +
            "void outer(@DelegatesTo(value = C1, strategy = Closure.DELEGATE_FIRST) Closure block) {\n" +
            "  block.delegate = new C1()\n" +
            "  block.call()\n" +
            "}\n" +
            "void inner(@DelegatesTo(value = C2, strategy = Closure.OWNER_FIRST) Closure block) {\n" +
            "  block.delegate = new C2()\n" +
            "  block.call()\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  outer {\n" +
            "    inner {\n" +
            "      m1()\n" +
            "      print ' '\n" +
            "      m2()\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "outer delegate inner delegate");
    }

    @Test
    public void testCompileStatic9086b() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class C1 {\n" +
            "  void m1() {\n" +
            "    print 'outer delegate'\n" +
            "  }\n" +
            "}\n" +
            "class C2 {\n" +
            "  void m2() {\n" +
            "    print 'inner delegate'\n" +
            "  }\n" +
            "}\n" +
            "void outer(@DelegatesTo(value = C1, strategy = Closure.OWNER_FIRST) Closure block) {\n" +
            "  block.delegate = new C1()\n" +
            "  block.call()\n" +
            "}\n" +
            "void inner(@DelegatesTo(value = C2, strategy = Closure.DELEGATE_FIRST) Closure block) {\n" +
            "  block.delegate = new C2()\n" +
            "  block.call()\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  outer {\n" +
            "    inner {\n" +
            "      m1()\n" +
            "      print ' '\n" +
            "      m2()\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "outer delegate inner delegate");
    }

    @Test
    public void testCompileStatic9086c() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class C1 {\n" +
            "  void m1() {\n" +
            "    print 'outer delegate'\n" +
            "  }\n" +
            "}\n" +
            "class C2 {\n" +
            "  void m2() {\n" +
            "    print 'inner delegate'\n" +
            "  }\n" +
            "}\n" +
            "void outer(@DelegatesTo(value = C1, strategy = Closure.OWNER_FIRST) Closure block) {\n" +
            "  block.delegate = new C1()\n" +
            "  block.call()\n" +
            "}\n" +
            "void inner(@DelegatesTo(value = C2, strategy = Closure.OWNER_FIRST) Closure block) {\n" +
            "  block.delegate = new C2()\n" +
            "  block.call()\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  outer {\n" +
            "    inner {\n" +
            "      m1()\n" +
            "      print ' '\n" +
            "      m2()\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "outer delegate inner delegate");
    }

    @Test
    public void testCompileStatic9086d() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class C1 {\n" +
            "  void m() {\n" +
            "    print 'outer delegate'\n" +
            "  }\n" +
            "}\n" +
            "class C2 {\n" +
            "  void m() {\n" +
            "    print 'inner delegate'\n" +
            "  }\n" +
            "}\n" +
            "void outer(@DelegatesTo(value = C1, strategy = Closure.DELEGATE_FIRST) Closure block) {\n" +
            "  block.delegate = new C1()\n" +
            "  block.call()\n" +
            "}\n" +
            "void inner(@DelegatesTo(value = C2, strategy = Closure.DELEGATE_FIRST) Closure block) {\n" +
            "  block.delegate = new C2()\n" +
            "  block.call()\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  outer {\n" +
            "    m()\n" +
            "    print ' '\n" +
            "    inner {\n" +
            "      m()\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "outer delegate inner delegate");
    }

    @Test
    public void testCompileStatic9086e() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class C1 {\n" +
            "  void m() {\n" +
            "    print 'outer delegate'\n" +
            "  }\n" +
            "}\n" +
            "class C2 {\n" +
            "  void m() {\n" +
            "    print 'inner delegate'\n" +
            "  }\n" +
            "}\n" +
            "void outer(@DelegatesTo(value = C1, strategy = Closure.DELEGATE_FIRST) Closure block) {\n" +
            "  block.delegate = new C1()\n" +
            "  block.call()\n" +
            "}\n" +
            "void inner(@DelegatesTo(value = C2, strategy = Closure.OWNER_FIRST) Closure block) {\n" +
            "  block.delegate = new C2()\n" +
            "  block.call()\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  outer {\n" +
            "    m()\n" +
            "    print ' '\n" +
            "    inner {\n" +
            "      m()\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "outer delegate outer delegate");
    }

    @Test
    public void testCompileStatic9086f() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class C1 {\n" +
            "  void m() {\n" +
            "    print 'outer delegate'\n" +
            "  }\n" +
            "}\n" +
            "class C2 {\n" +
            "  void m() {\n" +
            "    print 'inner delegate'\n" +
            "  }\n" +
            "}\n" +
            "void outer(@DelegatesTo(value = C1, strategy = Closure.OWNER_FIRST) Closure block) {\n" +
            "  block.delegate = new C1()\n" +
            "  block.call()\n" +
            "}\n" +
            "void inner(@DelegatesTo(value = C2, strategy = Closure.OWNER_FIRST) Closure block) {\n" +
            "  block.delegate = new C2()\n" +
            "  block.call()\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  outer {\n" +
            "    m()\n" +
            "    print ' '\n" +
            "    inner {\n" +
            "      m()\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "outer delegate outer delegate");
    }

    @Test
    public void testCompileStatic9089() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class C1 {\n" +
            "  void m() {\n" +
            "    print 'outer delegate'\n" +
            "  }\n" +
            "}\n" +
            "class C2 {\n" +
            "  void m() {\n" +
            "    print 'inner delegate'\n" +
            "  }\n" +
            "}\n" +
            "@SuppressWarnings('rawtypes')\n" +
            "void outer(@DelegatesTo(value = C1) Closure block) {\n" +
            "  block.delegate = new C1()\n" +
            "  block()\n" +
            "}\n" +
            "@SuppressWarnings('rawtypes')\n" +
            "void inner(@DelegatesTo(value = C2, strategy = Closure.DELEGATE_FIRST) Closure block) {\n" +
            "  block.delegate = new C2()\n" +
            "  block()\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  outer {\n" +
            "    inner {\n" +
            "      owner.m()\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "outer delegate");
    }

    @Test
    public void testCompileStatic9089a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class C1 {\n" +
            "  void m() {\n" +
            "    print 'outer delegate'\n" +
            "  }\n" +
            "}\n" +
            "class C2 {\n" +
            "  void m() {\n" +
            "    print 'inner delegate'\n" +
            "  }\n" +
            "}\n" +
            "@SuppressWarnings('rawtypes')\n" +
            "void outer(@DelegatesTo(value = C1) Closure block) {\n" +
            "  block.delegate = new C1()\n" +
            "  block()\n" +
            "}\n" +
            "@SuppressWarnings('rawtypes')\n" +
            "void inner(@DelegatesTo(value = C2, strategy = Closure.OWNER_FIRST) Closure block) {\n" +
            "  block.delegate = new C2()\n" +
            "  block()\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  outer {\n" +
            "    inner {\n" +
            "      delegate.m()\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "inner delegate");
    }

    @Test
    public void testCompileStatic9127() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Foo {\n" +
            "  protected String field = 'foo'\n" +
            "  String getField() {\n" +
            "    return field\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class Bar extends Foo {\n" +
            "  void changeField() {\n" +
            "    this.field = 'baz'\n" + // [Static type checking] - Cannot set read-only property: field
            "  }\n" +
            "  @Override\n" +
            "  String getField() {\n" +
            "    return 'bar'\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "def bar = new Bar()\n" +
            "bar.changeField()\n" +
            "println bar.field\n",
        };
        //@formatter:on

        runConformTest(sources, "bar");
    }

    @Test
    public void testCompileStatic9136() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Foo {\n" +
            "  public String field = 'foo'\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class Bar {\n" +
            "  def doIt(Foo foo) {\n" +
            "    'baz'.with {\n" +
            "      print foo.field\n" + // Access to Foo#foo is forbidden
            "      return 'bar'\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "def bar = new Bar()\n" +
            "print bar.doIt(new Foo())\n",
        };
        //@formatter:on

        runConformTest(sources, "foobar");
    }

    @Test
    public void testCompileStatic9136a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Foo {\n" +
            "  public String field = 'foo'\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class Bar {\n" +
            "  def doIt(Foo foo) {\n" +
            "    print foo.field\n" + // Access to Foo#foo is forbidden
            "    return 'bar'\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "def bar = new Bar()\n" +
            "print bar.doIt(new Foo())\n",
        };
        //@formatter:on

        runConformTest(sources, "foobar");
    }

    @Test
    public void testCompileStatic9136b() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Foo {\n" +
            "  private String field = 'foo'\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class Bar {\n" +
            "  def doIt(Foo foo) {\n" +
            "    foo.with {\n" +
            "      field\n" + // Access to Foo#field is forbidden
            "    }\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "def bar = new Bar()\n" +
            "print bar.doIt(new Foo())\n",
        };
        //@formatter:on

        runConformTest(sources, "foo");
    }

    @Test
    public void testCompileStatic9136c() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Foo {\n" +
            "  private String field = 'foo'\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class Bar {\n" +
            "  def doIt(Foo foo) {\n" +
            "    foo.with {\n" +
            "      delegate.field\n" + // Access to Foo#field is forbidden
            "    }\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "def bar = new Bar()\n" +
            "print bar.doIt(new Foo())\n",
        };
        //@formatter:on

        runConformTest(sources, "foo");
    }

    @Test @Ignore("https://issues.apache.org/jira/browse/GROOVY-9151")
    public void testCompileStatic9151() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void greet(Object o = 'world', String s = o.toString()) {\n" +
            "  print \"hello $s\"\n" +
            "}\n" +
            "greet()\n",
        };
        //@formatter:on

        runConformTest(sources, "hello world");
    }

    @Test @Ignore("https://issues.apache.org/jira/browse/GROOVY-9151")
    public void testCompileStatic9151a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Thing {\n" +
            "  Thing(Object o = 'foo', String s = o.toString()) {\n" +
            "    print s\n" +
            "  }\n" +
            "}\n" +
            "new Thing()\n" +
            "new Thing('bar')\n",
        };
        //@formatter:on

        runConformTest(sources, "foobar");
    }

    @Test
    public void testCompileStatic9153() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void meth() {\n" +
            "  File temp = File.createTempDir()\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic9265() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Outer {\n" +
            "  static class Inner {\n" +
            "    public String field = 'works'\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  { ->\n" + // <-- could be each, with, or whatever
            "    def inner = new Outer.Inner()\n" +
            "    print inner.field\n" +
            "  }()\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic9283() {
        //@formatter:off
        String[] sources = {
            "Groovy9283.groovy",
            "class Groovy9283 {\n" +
            "  @groovy.transform.CompileStatic\n" +
            "  static main(args) {\n" +
            "    this.newInstance().enter {\n" +
            "      nest {\n" +
            "        nest {\n" +
            "          nest {\n" +
            "            print \"${delegate.class.simpleName}${delegate.index}\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "  \n" +
            "  void enter(@DelegatesTo(value=Outer, strategy=Closure.DELEGATE_FIRST) Closure closure) {\n" +
            "    new Outer().with(closure)\n" +
            "  }\n" +
            "  \n" +
            "  abstract static class Node {\n" +
            "    private static int count\n" +
            "    final int index\n" +
            "    \n" +
            "    Node() {\n" +
            "      index = count++\n" +
            "    }\n" +
            "    \n" +
            "    void nest(@DelegatesTo(value=Inner, strategy=Closure.DELEGATE_FIRST) Closure closure) {\n" +
            "      print \"${getClass().simpleName}${index} > \"\n" +
            "      new Inner().with(closure)\n" +
            "    }\n" +
            "  }\n" +
            "  \n" +
            "  static class Outer extends Node {\n" +
            "  }\n" +
            "  \n" +
            "  static class Inner extends Node {\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Outer0 > Inner1 > Inner2 > Inner3"); // OWNER_FIRST results in "Outer0 > Outer0 > Outer0 > Inner3"
    }

    @Test
    public void testCompileStatic9328() {
        //@formatter:off
        String[] sources = {
            "Outer.groovy",
            "class Outer {\n" +
            "  static main(args) {\n" +
            "    new Outer().test()\n" +
            "  }\n" +
            "  void test() {\n" +
            "    def inner = new Inner()\n" +
            "    print inner.innerMethod()\n" +
            "  }\n" +
            "  class Inner {\n" +
            "    @groovy.transform.CompileStatic\n" +
            "    String innerMethod() { outerMethod() }\n" +
            "  }\n" +
            "  private String outerMethod() { 'works' }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic9328a() {
        //@formatter:off
        String[] sources = {
            "Outer.groovy",
            "class Outer {\n" +
            "  static main(args) {\n" +
            "    new Outer().test()\n" +
            "  }\n" +
            "  void test() {\n" +
            "    def callable = new java.util.concurrent.Callable<String>() {\n" +
            "      @groovy.transform.CompileStatic\n" +
            "      @Override String call() { outerMethod() }\n" +
            "    }\n" +
            "    print callable.call()\n" +
            "  }\n" +
            "  private String outerMethod() { 'works' }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic9332() {
        assumeTrue(isAtLeastJava(JDK8) && isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  static main(args) {\n" +
            "    print list\n" +
            "  }\n" +
            "  static list\n" +
            "  static final int one = 1\n" +
            "  static {\n" +
            "    list = [1, 2, 3].stream().map(i -> i + one).toList()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[2, 3, 4]");
    }

    @Test
    public void testCompileStatic9332a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  static main(args) {\n" +
            "    print list\n" +
            "  }\n" +
            "  static list\n" +
            "  static final int one = 1\n" +
            "  static {\n" +
            "    list = [1, 2, 3].collect { i -> i + one }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[2, 3, 4]");
    }

    @Test
    public void testCompileStatic9332b() {
        assumeTrue(isAtLeastJava(JDK8) && isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  static main(args) {\n" +
            "    print last\n" +
            "  }\n" +
            "  static int last = 0\n" +
            "  static {\n" +
            "    [1, 2, 3].forEach((Integer i) -> last = i)\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "3");
    }

    @Test
    public void testCompileStatic9333() {
        assumeTrue(isAtLeastJava(JDK8) && isParrotParser());

        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import java.util.function.*\n" +
            "\n" +
            "class C {\n" +
            "  public String field = 'f'\n" +
            "  \n" +
            "  @groovy.transform.CompileStatic\n" +
            "  void test() {\n" +
            "    Consumer<C> c = (C thisParameter) -> {\n" +
            "      print '1' + thisParameter.field\n" +
            "      print '2' + thisObject.field\n" +
            "      print '3' + this.field\n" +
            "      print '4' + field\n" +
            "    }\n" +
            "    c.accept(this)\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "1f2f3f4f");
    }

    @Test
    public void testCompileStatic9333a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class C {\n" +
            "  public String field = 'f'\n" +
            "  \n" +
            "  @groovy.transform.CompileStatic\n" +
            "  void test() {\n" +
            "    def c = { C thisParameter ->\n" +
            "      print '1' + thisParameter.field\n" +
            "      print '2' + thisObject.field\n" +
            "      print '3' + this.field\n" +
            "      print '4' + field\n" +
            "    }\n" +
            "    c(this)\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "1f2f3f4f");
    }

    @Test
    public void testCompileStatic9333and9341() {
        assumeTrue(isAtLeastJava(JDK8) && isParrotParser());

        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import java.util.function.*\n" +
            "\n" +
            "class C {\n" +
            "  public String field = 'f'\n" +
            "  \n" +
            "  @groovy.transform.CompileStatic\n" +
            "  void test() {\n" +
            "    Consumer<C> c1 = (C thisParameter1) -> {\n" +
            "      Consumer<C> c2 = (C thisParameter2) -> {\n" +
            "        print '0' + thisParameter2.field\n" +
            "        print '1' + thisParameter1.field\n" +
            "        print '2' + thisObject.field\n" +
            "        print '3' + this.field\n" +
            "        print '4' + field\n" +
            "      }\n" +
            "      c2.accept(thisParameter1)\n" +
            "    }\n" +
            "    c1.accept(this)\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "0f1f2f3f4f");
    }

    @Test
    public void testCompileStatic9333c() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class C {\n" +
            "  public String field = 'f'\n" +
            "  \n" +
            "  @groovy.transform.CompileStatic\n" +
            "  void test() {\n" +
            "    def c1 = { C thisParameter1 ->\n" +
            "      def c2 = { C thisParameter2 ->\n" +
            "        print '0' + thisParameter2.field\n" +
            "        print '1' + thisParameter1.field\n" +
            "        print '2' + thisObject.field\n" +
            "        print '3' + this.field\n" +
            "        print '4' + field\n" +
            "      }\n" +
            "      c2(thisParameter1)\n" +
            "    }\n" +
            "    c1(this)\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "0f1f2f3f4f");
    }

    @Test
    public void testCompileStatic9338() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "void meth(Class<? extends CharSequence> c) {\n" +
            "  print c.simpleName\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  def c = (Class<?>) String.class\n" +
            "  meth(c)\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Script.groovy (at line 7)\n" +
            "\tmeth(c)\n" +
            "\t^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot call Script#meth(java.lang.Class <? extends java.lang.CharSequence>) with arguments [java.lang.Class <?>] \n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic9338a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "void meth(Class<? super CharSequence> c) {\n" +
            "  print c.simpleName\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  def c = (Class<?>) String.class\n" +
            "  meth(c)\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Script.groovy (at line 7)\n" +
            "\tmeth(c)\n" +
            "\t^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot call Script#meth(java.lang.Class <? super java.lang.CharSequence>) with arguments [java.lang.Class <?>] \n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic9340() {
        assumeTrue(isAtLeastJava(JDK8) && isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  static main(args) {\n" +
            "    this.newInstance().test()\n" +
            "  }\n" +
            "  @groovy.transform.CompileStatic\n" +
            "  void test() {\n" +
            "    java.util.function.Consumer<Main> consumer = main -> print 'works'\n" + // A transform used a generics containing ClassNode Main for the method ...
            "    consumer.accept(this)\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic9342() {
        assumeTrue(isAtLeastJava(JDK8) && isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  static main(args) {\n" +
            "    print acc\n" +
            "  }\n" +
            "  static int acc = 0\n" +
            "  static {\n" +
            "    [1, 2, 3].forEach((Integer i) -> acc += i)\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "6");
    }

    @Test
    public void testCompileStatic9347() {
        assumeTrue(isAtLeastJava(JDK8) && isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  static main(args) {\n" +
            "    print acc\n" +
            "  }\n" +
            "  static int acc = 0\n" +
            "  static {\n" +
            "    [1, 2, 3].forEach(i -> acc += i)\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "6");
    }

    @Test
    public void testCompileStatic9347a() {
        assumeTrue(isAtLeastJava(JDK8));

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  static main(args) {\n" +
            "    print acc\n" +
            "  }\n" +
            "  static int acc = 0\n" +
            "  static {\n" +
            "    [1, 2, 3].forEach { i -> acc += i }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "6");
    }
}
