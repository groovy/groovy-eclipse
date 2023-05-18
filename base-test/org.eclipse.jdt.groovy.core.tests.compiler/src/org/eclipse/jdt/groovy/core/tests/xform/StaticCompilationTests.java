/*
 * Copyright 2009-2023 the original author or authors.
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
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
            "Groovy:[Static type checking] - Cannot call java.util.ArrayList#add(java.lang.Integer) with arguments [java.lang.String]\n" +
            "----------\n");
    }

    /**
     * Test case for {@link org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport#checkCompatibleAssignmentTypes(ClassNode,ClassNode,Expression,boolean) checkCompatibleAssignmentTypes}.
     * <p>
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
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  def list = new LinkedList<String>([1,2,3])\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 3)\n" +
            "\tdef list = new LinkedList<String>([1,2,3])\n" +
            "\t           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot call java.util.LinkedList#<init>(java.util.Collection<? extends java.lang.String>) with arguments [java.util.ArrayList<java.lang.Integer>]\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic13() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
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
            "Main.groovy",
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
    public void testCompileStatic15() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  static class One {\n" +
            "    private static int foo = 333\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    print One.foo\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "333");
    }

    @Test
    public void testCompileStatic16() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  static class One {\n" +
            "    private static int foo = 333\n" +
            "  }\n" +
            "  static class Two {\n" +
            "    private static int bar = One.foo\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    print One.foo + Two.bar\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "666");
    }

    @Test
    public void testCompileStatic17() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  static main(args) {\n" +
            "    print One.foo + Two.bar\n" +
            "  }\n" +
            "}\n" +
            "class One {\n" +
            "  private static int foo = 333\n" +
            "}\n" +
            "@groovy.transform.PackageScope\n" +
            "class Two {\n" +
            "  private static int bar = One.foo\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 4)\n" +
            "\tprint One.foo + Two.bar\n" +
            "\t      ^^^\n" +
            "Groovy:Access to One#foo is forbidden\n" +
            "----------\n" +
            "2. ERROR in Main.groovy (at line 4)\n" +
            "\tprint One.foo + Two.bar\n" +
            "\t                ^^^\n" +
            "Groovy:Access to Two#bar is forbidden\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic18() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main<T extends List<X>, X extends Number> {\n" +
            "  X getFirstElement(T t) {\n" +
            "    X x = t.get(0)\n" +
            "    return x\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    def f = new Main<ArrayList<Integer>, Integer>()\n" +
            "    def list = new ArrayList<Integer>()\n" +
            "    list.add(123)\n" +
            "    print f.getFirstElement(list)\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "123");
    }

    /**
     * Test case for {@link org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor#performSecondPass() performSecondPass()}.
     */
    @Test
    public void testCompileStatic19() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class A {\n" +
            "  void foo() {}\n" +
            "}\n" +
            "class B {\n" +
            "  void bar() {}\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  def x = new A();\n" +
            "  { -> x = new B() }\n" +
            "  x.foo()\n" +
            "}\n",
        };
        //@formatter:on

        String lub = isAtLeastGroovy(40) ? "groovy.lang.GroovyObject" : "java.lang.Object";
        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 11)\n" +
            "\tx.foo()\n" +
            "\t^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching method " + lub + "#foo()." +
            " Please check if the declared type is correct and if the method exists.\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic20() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "import static org.codehaus.groovy.transform.stc.StaticTypesMarker.*\n" +
            "\n" +
            "@CompileStatic\n" +
            "class C {\n" +
            "  void m() {\n" +
            "    C that = this;\n" +
            "    { ->\n" +
            "      @ASTTest(phase=INSTRUCTION_SELECTION, value={\n" +
            "        assert node.getNodeMetaData(INFERRED_TYPE)?.name == 'C'\n" +
            "      })\n" +
            "      def ref = getThisObject()\n" +
            "      assert ref == that\n" +
            "    }();\n" +
            "    { ->\n" +
            "      @ASTTest(phase=INSTRUCTION_SELECTION, value={\n" +
            "        assert node.getNodeMetaData(INFERRED_TYPE)?.name == 'C'\n" +
            "      })\n" +
            "      def ref = getDelegate()\n" +
            "      assert ref == that\n" +
            "    }();\n" +
            "    { ->\n" +
            "      @ASTTest(phase=INSTRUCTION_SELECTION, value={\n" +
            "        assert node.getNodeMetaData(INFERRED_TYPE)?.name == 'C'\n" +
            "      })\n" +
            "      def ref = getOwner()\n" +
            "      assert ref == that\n" +
            "    }();\n" +
            "  }\n" +
            "}\n" +
            "new C().m()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic21() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class C {\n" +
            "  int which\n" +
            "  C() {\n" +
            "    contentView = 42L\n" +
            "    print which\n" +
            "  }\n" +
            "  void setContentView(Date value) { which = 1 }\n" +
            "  void setContentView(Long value) { which = 2 }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class D extends C {\n" +
            "  D() {\n" +
            "    contentView = 42L\n" +
            "    print which\n" +
            "    contentView = new Date()\n" +
            "    print which\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  new D().with {\n" +
            "    contentView = 42L\n" +
            "    print which\n" +
            "    contentView = new Date()\n" +
            "    print which\n" +
            "  }\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "22121");
    }

    @Test
    public void testCompileStatic22() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  def x = '123';\n" +
            "  { -> x = new StringBuffer() }\n" +
            "  print x.charAt(0) // available in String and StringBuffer\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "1");
    }

    @Test // StaticTypeCheckingSupport#typeCheckMethodsWithGenerics checks receiver derivation
    public void testCompileStatic23() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "abstract class A {\n" +
            "  public    void foo() { print 'hello' }\n" +
            "  protected void bar(ArrayList list) { print(' '); baz(list) }\n" +
            "  private   void baz(List list, String s = 'abc') { print 'world' }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class Outer extends A {\n" +
            "  class Inner {\n" +
            "    void m() {\n" +
            "      foo()\n" + // receiver is Outer$Inner
            "      bar(new ArrayList())\n" + // same here
            "    }\n" +
            "  }\n" +
            "}\n" +
            "new Outer.Inner(new Outer()).m()\n",
        };
        //@formatter:on

        runConformTest(sources, "hello world");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1191
    public void testCompileStatic24() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "void m(java.util.function.Function<String, Integer> f) {\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  m(Object::sleep)\n" + // NPE while processing Main.groovy
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 5)\n" +
            "\tm(Object::sleep)\n" +
            "\t  ^^^^^^^^^^^^^\n" +
            "Groovy:Failed to find class method 'sleep(java.lang.String)' or instance method 'sleep()' for the type: java.lang.Object\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic25() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C<T> {\n" +
            "  T t\n" +
            "  T getT() {\n" +
            "    this.t\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test(C<Map<String,Object>> p) {\n" +
            "  print p.t.u\n" + // Access to T#x is forbidden
            "}\n" +
            "test(new C<>(t:[u:'v']))\n",
        };
        //@formatter:on

        runConformTest(sources, "v");
    }

    @Test
    public void testCompileStatic26() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C<T> {\n" +
            "  T t\n" +
            "  T getT() {\n" +
            "    this.t\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test(C<Map<String,Object>> p) {\n" +
            "  print p.with { t.u }\n" +
            "}\n" +
            "test(new C<>(t:[u:'v']))\n",
        };
        //@formatter:on

        runConformTest(sources, "v");
    }

    @Test
    public void testCompileStatic27() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C<T> {\n" +
            "  T t\n" +
            "  T getT() {\n" +
            "    this.t\n" +
            "  }\n" +
            "}\n" +
            "class D extends C<Map<String,Object>> {\n" +
            "  @groovy.transform.CompileStatic\n" +
            "  void test() {\n" +
            "    print t.u\n" +
            "  }\n" +
            "}\n" +
            "new D(t:[u:'v']).test()\n",
        };
        //@formatter:on

        runConformTest(sources, "v");
    }

    @Test
    public void testCompileStatic28() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C {\n" +
            "  private String getString() {''}\n" +
            "  @groovy.transform.CompileStatic\n" +
            "  void test() {\n" +
            "    print new Object() {\n" +
            "      String toString() {\n" +
            "        string.toLowerCase()\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1411
    public void testCompileStatic29() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test(Pogo pogo) {\n" +
            "  def key = 'aaa'\n" +
            "  pogo.map[key] = 1\n" +
            "}\n" +
            "test(new Pogo())\n",

            "Pogo.groovy",
            "class Pogo {\n" +
            "  Map<String,?> getMap() {\n" +
            "    return [:]\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic30() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import java.util.function.Function\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  Function<String, String> lower = String::toLowerCase\n" +
            "  Function<String, String> upper = String::toUpperCase\n" +
            "  Function<String, String> lu = lower.andThen(upper)\n" +
            "  Function<? super String, String> ul = upper.andThen(lower)\n" +
            "  assert lower('Hi') == ul('Hi')\n" +
            "  assert upper('Hi') == lu('Hi')\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic1505() {
        //@formatter:off
        String[] sources = {
            "DynamicQuery.groovy",
            "import groovy.transform.CompileStatic\n" +
            "@CompileStatic\n" +
            "class DynamicQuery {\n" +
            "  static main(args) {\n" +
            "    new DynamicQuery().foo(null)\n" +
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
            "Main.groovy",
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

    @Test // see GROOVY-5001, GROOVY-5491, GROOVY-6144, GROOVY-8788
    public void testCompileStatic5517() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class M extends HashMap<String,Number> {\n" +
            "  public static int version = 666\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  def map = new M()\n" +
            "  map['foo'] = 123\n" +
            "  map.foo = 123\n" +
            "  def value = map.foo\n" +
            "  assert value == 123\n" +
            "  map['foo'] = 4.5\n" +
            "  value = map['foo']\n" +
            "  assert value == 4.5\n" +
            "  value = map.version\n" +
            "  assert value == " + (isAtLeastGroovy(50) ? "666" : "null") + "\n" +
            "  assert M.version == 666\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic5746() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +

            "@Field int i = 0\n" +
            "int getIndex() { i++ }\n" +
            "@CompileStatic void test() {\n" +
            "  def list = ['x','y','z']\n" +
            "  assert (list[index] += '!') == 'x!'\n" +
            "  assert (list[index] += '!') == 'y!'\n" +
            "  print list\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "[x!, y!, z]");
    }

    @Test
    public void testCompileStatic6095() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
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
    public void testCompileStatic6097() {
        for (String mod : new String[] {"", "public", "protected", "@groovy.transform.PackageScope"}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                "abstract class A {\n" +
                "  " + mod + " boolean isX() { true }\n" +
                "}\n" +
                "class C extends A {\n" +
                "  @groovy.transform.CompileStatic\n" +
                "  def m() {\n" +
                "    '' + x + this.x + super.x\n" + // hardwired to "super.getX()"
                "  }\n" +
                "}\n" +
                "print(new C().m())\n",
            };
            //@formatter:on

            runConformTest(sources, "truetruetrue");
        }
    }

    @Test
    public void testCompileStatic6137() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  print(null in null)\n" +
            "  print(null in 'xx')\n" +
            "  print('xx' in null)\n" +
            "  print('xx' in 'xx')\n" +
            "  print('xx' in ['xx'])\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "truefalsefalsetruetrue");
    }

    @Test
    public void testCompileStatic6137a() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  print(null !in null)\n" +
            "  print(null !in 'xx')\n" +
            "  print('xx' !in null)\n" +
            "  print('xx' !in 'xx')\n" +
            "  print('xx' !in [''])\n" +
            "  print('xx' !in ['xx'])\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "falsetruetruefalsetruefalse");
    }

    @Test
    public void testCompileStatic6276() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Outer {\n" +
            "  private int outerField = 1\n" +
            "  private int outerMethod() { 2 }\n" +
            "  int outerProperty = 3\n" +
            "  class Inner {\n" +
            "    void test() {\n" +
            "      assert outerField == 1\n" +
            "      assert outerMethod() == 2\n" +
            "      assert outerProperty == 3\n" +
            "      assert getOuterProperty() == 3\n" +
            "    }\n" +
            "  }\n" +
            "  void test() {\n" +
            "    new Inner().test()\n" +
            "  }\n" +
            "}\n" +
            "new Outer().test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic6277() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class One {\n" +
            "  private getX() { 'One' }\n" +
            "}\n" +
            "class Two extends One {\n" +
            "  public x = 'Two'\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  print(new Two().x)\n" + // Cannot call private method One#getX from class Main
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "Two");
    }

    @Test
    public void testCompileStatic6504() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "import static org.codehaus.groovy.transform.stc.StaticTypesMarker.*\n" +
            "\n" +
            "@CompileStatic void test() {\n" +
            "  @ASTTest(phase=INSTRUCTION_SELECTION, value={\n" +
            "    def target = node.rightExpression.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)\n" +
            "    assert target.declaringClass.name == 'java.util.Collection'\n" + // not java.lang.Object
            "  })\n" +
            "  int sum = ['a','bb','ccc'].inject(0) { int acc, String str -> acc += str.length(); acc }\n" +
            "  print sum" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "6");
    }

    @Test
    public void testCompileStatic6610() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Outer {\n" +
            "  private static Integer VALUE = 42\n" +
            "  static class Inner {\n" +
            "    public final String value\n" +
            "    Inner(String string) {\n" +
            "      value = string\n" +
            "    }\n" +
            "    Inner() {\n" +
            "      this(VALUE.toString())\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "print new Outer.Inner().value\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testCompileStatic6782() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  String[] array = [123]\n" +
            "  def temp = array\n" +
            "  def x = temp[0]\n" +
            "  temp = [:]\n" + // works if this line is removed
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic6802() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  Boolean b = [false]\n" +
            "  print b\n" +
            "  b = [true];\n" +
            "  print b\n" +
            "  b = [];\n" +
            "  print b\n" +
            "  b = [:]\n" +
            "  print b\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "truetruefalsefalse");
    }

    @Test
    public void testCompileStatic6802a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  Class<?> c = []\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 3)\n" +
            "\tClass<?> c = []\n" +
            "\t^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching constructor java.lang.Class()\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic6803() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  String s = ['foo']\n" +
            "  print s\n" +
            "  s = [];\n" +
            "  print s\n" +
            "  s = [:]\n" +
            "  print s\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "[foo][]" + (isAtLeastGroovy(40) ? "[:]" : "{}"));
    }

    @Test
    public void testCompileStatic6849() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "interface ObservableList<E> extends List<E> {\n" +
            "  boolean addAll(E... elements)\n" +
            "}\n" +
            "def <E> ObservableList<E> wrap(List<E> list) {\n" +
            "  list as ObservableList\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test(ObservableList<String> tags) {\n" +
            "  tags.addAll('bug')\n" + // Collection#addAll(T[]) (dgm) vs ObservableList#addAll(E[])
            "  print tags\n" +
            "}\n" +
            "test(wrap(['add']))\n",
        };
        //@formatter:on

        runConformTest(sources, "[add, bug]");
    }

    @Test
    public void testCompileStatic6851() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  static main(args) {\n" +
            "    new Main().test()\n" +
            "  }\n" +
            "  void test(Map<String, Object> m = new HashMap<>(Collections.emptyMap())) {\n" +
            "    print m\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[:]");
    }

    @Test
    public void testCompileStatic6904() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "interface I {\n" +
            "  def m()\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class C {\n" +
            "  def foo() {\n" +
            "    bar { ->\n" +
            "      return new I() {\n" +
            "        def m() { baz }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "  def bar(Closure<? extends I> x) {\n" +
            "    x().m()\n" +
            "  }\n" +
            "  def baz = 'works'\n" +
            "}\n" +
            "print new C().foo()\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic6921() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "def test(List<List<?>> list) {\n" +
            "  list.collectMany { pair ->\n" +
            "    (1..pair[1]).collect {\n" +
            "      \"${pair[0]} supports $it\".toString()\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "print test([\n" +
            "  ['x', 1],\n" +
            "  ['y', 2],\n" +
            "])\n",
        };
        //@formatter:on

        runConformTest(sources, "[x supports 1, y supports 1, y supports 2]");
    }

    @Test
    public void testCompileStatic6970() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "interface A { void m() }\n" +
            "interface B { void m() }\n" +
            "interface C extends A, B { }\n" +
            "class D {\n" +
            "  @groovy.transform.CompileStatic\n" +
            "  D(C c) {\n" +
            "    c.m()\n" +
            "  }\n" +
            "}\n" +
            "class CImpl implements C {\n" +
            "  void m() { println 'works' }\n" +
            "}\n" +
            "new D(new CImpl())\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic7204() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Repository<T, S extends Serializable> {\n" +
            "  void delete(T arg) { assert true }\n" +
            "  void delete(S arg) { assert false: 'wrong method invoked' }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  Repository<String, Long> r = new Repository<String, Long>()\n" +
            "  r.delete('foo')\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 8)\n" +
            "\tr.delete('foo')\n" +
            "\t^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot call Repository#delete(java.lang.Long) with arguments [java.lang.String]\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic7300() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
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
            "Main.groovy",
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
            "    return super.getField()\n" +
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
            "Main.groovy",
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
            "    return super.getField()\n" +
            "  }\n" +
            "}\n" +
            "print new B().field\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 11)\n" +
            "\tsuper.@field = 'reset'\n" +
            "\t       ^^^^^\n" +
            "Groovy:[Static type checking] - The field A.field is not accessible\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic7300c() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
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
    public void testCompileStatic7304() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class A {\n" +
            "  private int i = 1\n" +
            "  @groovy.transform.CompileStatic\n" +
            "  int m() {\n" +
            "    \"\".with {\n" +
            "      i++\n" + // should use private access bridge method
            "    }\n" +
            "  }\n" +
            "}\n" +
            "class B extends A {\n" +
            "}\n" +
            "print new B().m()\n",
        };
        //@formatter:on

        runConformTest(sources, "1");
    }

    @Test
    public void testCompileStatic7304a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class A {\n" +
            "  private int i = 1\n" +
            "  int m() {\n" +
            "    \"\".with {\n" +
            "      ++i\n" + // should use private access bridge method
            "    }\n" +
            "  }\n" +
            "}\n" +
            "class B extends A {\n" +
            "}\n" +
            "print new B().m()\n",
        };
        //@formatter:on

        runConformTest(sources, "2");
    }

    @Test
    public void testCompileStatic7304b() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class A {\n" +
            "  private int i = 1\n" +
            "  int m() {\n" +
            "    \"\".with {\n" +
            "      i--\n" + // should use private access bridge method
            "    }\n" +
            "  }\n" +
            "}\n" +
            "class B extends A {\n" +
            "}\n" +
            "print new B().m()\n",
        };
        //@formatter:on

        runConformTest(sources, "1");
    }

    @Test
    public void testCompileStatic7361() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class A {\n" +
            "  private final Map<Long, String> map = [1L:'x', 2L:'y']\n" +
            "  void m() {\n" +
            "    def list = [1L]\n" +
            "    list.each {\n" +
            "      synchronized (map) {\n" +
            "        map.remove(it)\n" +
            "      }\n" +
            "    }\n" +
            "    print map\n" +
            "  }\n" +
            "}\n" +
            "new A().m()\n",
        };
        //@formatter:on

        runConformTest(sources, "[2:y]");
    }

    @Test
    public void testCompileStatic7361a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class A {\n" +
            "  private final Map map = [:]\n" +
            "  void m() {\n" +
            "    new Runnable() {\n" +
            "      @groovy.transform.CompileStatic\n" +
            "      void run() {\n" +
            "        { -> map['x'] = 'y' }.call()\n" +
            "      }\n" +
            "    }.run()\n" +
            "    print map\n" +
            "  }\n" +
            "}\n" +
            "new A().m()\n",
        };
        //@formatter:on

        runConformTest(sources, "[x:y]");
    }

    @Test
    public void testCompileStatic7363() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  def abc = new ABC()\n" +
            "  print('' + abc.b.object.value)\n" +
            "}\n" +
            "test()\n",

            "Types.java",
            "interface A<T, U extends B<T>> {\n" +
            "  U getB();\n" +
            "}\n" +
            "class ABC implements A<C, BC> {\n" +
            "  void setB(BC b) {}\n" +
            "  @Override\n" +
            "  public BC getB() {\n" +
            "    return new BC();\n" +
            "  }\n" +
            "}\n" +
            "interface B<T> {\n" +
            "  T getObject();\n" +
            "}\n" +
            "class BC implements B<C> {\n" +
            "  @Override\n" +
            "  public C getObject() {\n" +
            "      return new C();\n" +
            "  }\n" +
            "}\n" +
            "class C {\n" +
            "  long getValue() {\n" +
            "      return 42;\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testCompileStatic7473() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Foo { String bar }\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test(Foo foo) {\n" +
            "  if (foo.bar[0] in ['a','b','c']) {\n" +
            "    print 'abc'\n" +
            "  }\n" +
            "}\n" +
            "test(new Foo(bar:'baz'))\n",
        };
        //@formatter:on

        runConformTest(sources, "abc");

        String result = disassemble(getOutputFile("Main.class"), 1);
        int pos = result.indexOf("createList");
        assumeTrue(pos > 0);

        // the operand should be processed only once
        pos = result.indexOf("createList", pos + 1);
        assertTrue(pos < 0);
    }

    @Test
    public void testCompileStatic7473a() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Foo { String bar }\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test(Foo foo) {\n" +
            "  if (foo.bar[0] !in ['x','y','z']) {\n" +
            "    print 'not xyz'\n" +
            "  }\n" +
            "}\n" +
            "test(new Foo(bar:'baz'))\n",
        };
        //@formatter:on

        runConformTest(sources, "not xyz");

        String result = disassemble(getOutputFile("Main.class"), 1);
        int pos = result.indexOf("ScriptBytecodeAdapter.isNotCase");
        if (isAtLeastGroovy(40)) assertTrue(pos < 0); //GROOVY-10383

        pos = result.indexOf("createList");
        assumeTrue(pos > 0);

        // the operand should be processed only once
        pos = result.indexOf("createList", pos + 1);
        assertTrue(pos < 0);
    }

    @Test
    public void testCompileStatic7473b() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  int[] accept = [1,2]\n" +
            "  def   result = ['x','yy','zzz'].findAll { it.size() in accept }\n" +
            "  print result\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "[x, yy]");
    }

    @Test
    public void testCompileStatic7473c() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class C {\n" +
            "  int i, j\n" +
            "  int getA() { i++ }\n" +
            "  int getB() { j++ }\n" +
            "  void test() {\n" +
            "    assert a in b\n" +
            "    assert i == 1\n" +
            "    assert j == 1\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic7473d() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class C {\n" +
            "  int i, j\n" +
            "  def getA() { i++ }\n" +
            "  def getB() { j++; null }\n" +
            "  void test() {\n" +
            "    assert !(a in b)\n" +
            "    assert i == 1\n" +
            "    assert j == 1\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic7490() {
        for (String imports : new String[] {"import static Pogo.callable_property; import static Pogo.closure_property", "import static Pogo.*"}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                imports + "\n" +
                "@groovy.transform.CompileStatic\n" +
                "void test() {\n" +
                "  print callable_property('hello')\n" +
                "  print closure_property(' world')\n" +
                "}\n" +
                "test()\n",

                "Pogo.groovy",
                "class Pogo {\n" +
                "  static final WithCall callable_property = new WithCall()\n" +
                "  static final Closure closure_property = { return it }\n" +
                "}\n",

                "WithCall.groovy",
                "class WithCall {\n" +
                "  String call(String input) {\n" +
                "    return input\n" +
                "  }\n" +
                "}\n",
            };
            //@formatter:on

            runConformTest(sources, "hello world");
        }
    }

    @Test
    public void testCompileStatic7490a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  print Pogo.callable_property.call('works')\n" + // TODO:5881,6324
            "}\n" +
            "test()\n",

            "Pogo.groovy",
            "class Pogo {\n" +
            "  static final WithCall callable_property = new WithCall()\n" +
            "}\n",

            "WithCall.groovy",
            "class WithCall {\n" +
            "  String call(String input) {\n" +
            "    return input\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic7506() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C {\n" +
            "  public String[] strings\n" +
            "  void setP(String[] strings) {\n" +
            "    this.strings = strings\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  def c = new C()\n" +
            "  c.p = ['foo', 123 ]\n" +
            "  assert c.strings == ['foo','123']\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic7526() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "boolean check(String s) { true }\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test(Pogo pogo) {\n" +
            "  if (check(pogo?.field)) {\n" + // VerifyError: Bad type on operand stack
            "    print 'works'\n" +
            "  }\n" +
            "}\n" +
            "test(new Pogo())\n",

            "Pogo.groovy",
            "class Pogo {\n" +
            "  public String field\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic7549() {
        //@formatter:off
        String[] sources = {
            "a/Main.groovy",
            "package a\n" +
            "import b.Maker\n" +
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  static main(args) {\n" +
            "    Face f = Maker.make()\n" +
            // the following only fails when CompileStatic is enabled with the following error:
            // Exception in thread "main" java.lang.IllegalAccessError: tried to access class b.Impl from class a.Main
            "    f.meth()\n" +
            "  }\n" +
            "}\n",

            "a/Face.groovy",
            "package a\n" +
            "interface Face {\n" +
            "  void meth()\n" +
            "}\n",

            "b/Impl.groovy",
            "package b\n" +
            "import a.Face\n" +
            "@groovy.transform.CompileStatic\n" +
            "@groovy.transform.PackageScope\n" +
            "class Impl implements Face {\n" +
            "  void meth() {\n" +
            "    println 'did stuff'\n" +
            "  }\n" +
            "}\n",

            "b/Maker.groovy",
            "package b\n" +
            "class Maker {\n" +
            "  static Impl make() {\n" + // probably should return a.Face
            "    new Impl()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "did stuff");
    }

    @Test
    public void testCompileStatic7595() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "interface I {\n" +
            "  void setP(value)\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class C {\n" +
            "  def p\n" +
            "  void setP(value) {\n" +
            "    { ->\n" +
            "      this.@p = value\n" +
            "    }()\n" +
            "  }\n" +
            "}\n" +
            "def obj = new C()\n" +
            "obj.setP('works')\n" +
            "print(obj.p)\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic7687() {
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
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "abstract class A<N extends Number> {\n" +
            "  protected final N number\n" +
            "  A(N number) {\n" +
            "    this.number = number\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class C<I extends BigInteger> extends A<I> {\n" +
            "  C(I integer) {\n" +
            "    super(integer)\n" +
            "  }\n" +
            "  I getValue() {\n" +
            "    return number\n" + // field of type I
            "  }\n" +
            "}\n" +
            "print new C<BigInteger>(42G).value\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testCompileStatic7691a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "abstract class A<N extends Number> {\n" +
            "  protected final N number\n" +
            "  A(N number) {\n" +
            "    this.number = number\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class C<I extends BigInteger> extends A<I> {\n" +
            "  C(I integer) {\n" +
            "    super(integer)\n" +
            "  }\n" +
            "  I getValue() {\n" +
            "    return { ->\n" +
            "      return number\n" + // field of type I from closure
            "    }()\n" +
            "  }\n" +
            "}\n" +
            "print new C<BigInteger>(42G).value\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testCompileStatic7691b() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "abstract class A<N extends Number> {\n" +
            "  final N number\n" +
            "  A(N number) {\n" +
            "    this.number = number\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class C<I extends BigInteger> extends A<I> {\n" +
            "  C(I integer) {\n" +
            "    super(integer)\n" +
            "  }\n" +
            "  I getValue() {\n" +
            "    return number\n" + // property of type I
            "  }\n" +
            "}\n" +
            "print new C<BigInteger>(42G).value\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test // GROOVY-9580
    public void testCompileStatic7691c() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "abstract class A<N extends Number> {\n" +
            "  final N number\n" +
            "  A(N number) {\n" +
            "    this.number = number\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class C<I extends BigInteger> extends A<I> {\n" +
            "  C(I integer) {\n" +
            "    super(integer)\n" +
            "  }\n" +
            "  I getValue() {\n" +
            "    return getNumber()\n" + // method call of type I
            "  }\n" +
            "}\n" +
            "print new C<BigInteger>(42G).value\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testCompileStatic7701() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Foo {\n" +
            "  List type\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class Bar {\n" +
            "  int type = 10\n" +
            "  @Lazy List<Foo> something = { ->\n" +
            "    List<Foo> tmp = []\n" +
            "    def foo = new Foo()\n" +
            "    foo.with {\n" +
            "      type = ['String']\n" +
            //     ^^^^ should be Foo.type, not Bar.type
            "    }\n" +
            "    tmp.add(foo)\n" +
            "    tmp\n" +
            "  }()\n" +
            "}\n" +
            "def bar = new Bar()\n" +
            "assert bar.type == 10\n" +
            "assert bar.something*.type == [['String']]\n" +
            "assert bar.type == 10\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic7741() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class C {\n" +
            "  Closure doSomething = { -> }\n" +
            "  void m() {\n" +
            "    List items = ['x']\n" +
            "    items.each {\n" +
            "      doSomething()\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "new C().m()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic7848() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "def test() {\n" +
            "  def pairs = [[1,2], [3,4]]\n" +
            "  pairs.collect { pair -> pair[0] + pair[1] }\n" +
            "}\n" +
            "print test()\n",
        };
        //@formatter:on

        runConformTest(sources, "[3, 7]");
    }

    @Test
    public void testCompileStatic7848a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "def test() {\n" +
            "  List<List<Integer>> pairs = [[1,3], [1,2]].transpose()\n" +
            "  pairs.inject(true) { flag, pair -> flag && pair[0] == pair[1] }\n" +
            "}\n" +
            "print test()\n",
        };
        //@formatter:on

        runConformTest(sources, "false");
    }

    @Test
    public void testCompileStatic7970() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Foo {\n" +
            "  @groovy.transform.CompileStatic\n" +
            "  String renderTemplate(String arg) { print \":$arg\" }\n" +
            "  def bar() {\n" +
            "    'A'.with { renderTemplate(it) }\n" +
            "    new Object() {\n" +
            "      String toString() {\n" +
            "       renderTemplate('B')\n" +
            "       'C'.with { renderTemplate(it) }\n" + // ERROR
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "new Foo().bar().toString()\n",
        };
        //@formatter:on

        runConformTest(sources, ":A:B:C");
    }

    @Test
    public void testCompileStatic7985() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
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

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic7996() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "print new Bar().doStuff()\n",

            "Foo.groovy",
            "@groovy.transform.CompileStatic\n" +
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
            "      //return bars.isEmpty()\n" + // ClassCastException: java.lang.String cannot be cast to java.util.List
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        String strategyOne = "OWNER_FIRST";
        String strategyTwo = "DELEGATE_FIRST";

        runNegativeTest(sources,
            "----------\n" +
            "1. WARNING in Foo.groovy (at line 4)\n" +
            "\treturn this.with(block)\n" +
            "\t                 ^^^^^\n" +
            "Groovy:[Static type checking] - Closure parameter with resolve strategy " +
                strategyOne + " passed to method with resolve strategy " + strategyTwo + "\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic7996a() {
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
    public void testCompileStatic7996e() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "import org.codehaus.groovy.ast.DynamicVariable\n" +
            "import org.codehaus.groovy.ast.expr.VariableExpression\n" +
            "import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE\n" +
            "import static org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_TYPE\n" +
            "import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.PROPERTY_OWNER\n" +

            "class JSON {\n" +
            "  def get(String name) {\n" +
            "  }\n" +
            "}\n" +
            "class POGO {\n" +
            "  Number getAnswer() {\n" +
            "  }\n" +
            "  @CompileStatic\n" +
            "  void usage() {\n" +
            "    new JSON().with {\n" +
            "      @ASTTest(phase=CLASS_GENERATION, value={\n" +
            "        def vexp = node.rightExpression\n" +
            "        assert vexp instanceof VariableExpression\n" +
            "        assert vexp.accessedVariable instanceof DynamicVariable\n" +
            "        assert vexp.getNodeMetaData(INFERRED_TYPE) == OBJECT_TYPE\n" +
            "        assert vexp.getNodeMetaData(PROPERTY_OWNER).name == 'JSON'\n" +
            "      })\n" +
            "      def result = answer\n" + // "answer" accessed from JSON; "getAnswer()" invoked from POGO
            "    }\n" +
            "  }\n" +
            "}\n" +
            "new POGO().usage()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic8050() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Outer {\n" +
            "  class Inner {\n" +
            "  }\n" +
            "  def foo = 1\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test(Outer.Inner inner) {\n" +
            "  print inner.getFoo()\n" +
            "}\n" +
            "test(new Outer.Inner(new Outer()))\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 9)\n" +
            "\tprint inner.getFoo()\n" +
            "\t      ^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching method Outer$Inner#getFoo(). Please check if the declared type is correct and if the method exists.\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic8050a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Outer {\n" +
            "  class Inner {\n" +
            "  }\n" +
            "  def foo = 1\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test(Outer.Inner inner) {\n" +
            "  print inner.foo\n" +
            "}\n" +
            "test(new Outer.Inner(new Outer()))\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 9)\n" +
            "\tprint inner.foo\n" +
            "\t      ^^^^^^^^^\n" +
            "Groovy:[Static type checking] - No such property: foo for class: Outer$Inner\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic8051() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Outer {\n" +
            "  class Inner {\n" +
            "    Closure createClosure() {\n" +
            "      return { foo }\n" +
            "    }\n" +
            "  }\n" +
            "  def foo = 1\n" +
            "}\n" +
            "def i = new Outer.Inner(new Outer())\n" +
            "def c = i.createClosure()\n" +
            "print c()\n",
        };
        //@formatter:on

        runConformTest(sources, "1");
    }

    @Test
    public void testCompileStatic8074() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class M extends HashMap<String,Number> {\n" +
            "  def foo = 1\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  def map = new M()\n" +
            "  map.put('foo',42)\n" +
            "  print map.foo\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, isAtLeastGroovy(50) ? "1" : "42");
    }

    @Test
    public void testCompileStatic8133() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "import static org.codehaus.groovy.transform.stc.StaticTypesMarker.*\n" +
            "\n" +
            "@CompileStatic void test() {\n" +
            "  @ASTTest(phase=INSTRUCTION_SELECTION, value={\n" +
            "    def list_type = node.getNodeMetaData(INFERRED_TYPE)\n" +
            "    assert list_type?.toString(false) == 'java.util.List<java.lang.String>'\n" +
            "  })\n" +
            "  def list = ['foo','bar','baz'].stream()*.toUpperCase()\n" +
            "  print list\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "[FOO, BAR, BAZ]");
    }

    @Test
    public void testCompileStatic8176() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "static <P extends Pogo> P merge(P pogo, Map spec) {\n" +
            "  !spec ? pogo : pogo.tap {\n" +
            "    one = spec['one']\n" +
            "    two = spec['two']\n" +
            "  }\n" +
            "}\n" +
            "def pogo = new Pogo()\n" +
            "def result = merge(pogo, [one: 1, two: 2.0])\n" +
            "assert result.one == 1 && result.two == 2.0\n" +
            "assert result.is(pogo)\n",

            "Pogo.groovy",
            "class Pogo {\n" +
            "  def one, two\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic8310() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "def bar(Closure<Collection<Integer>> baz) {\n" +
            "  baz()\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "def foo() {\n" +
            "  bar {\n" +
            "    [1]\n" +
            "  }\n" +
            "}\n" +
            "print foo()\n",
        };
        //@formatter:on

        runConformTest(sources, "[1]");
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

    @Test
    public void testCompileStatic8389() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import static Foo.bar\n" +
            "class Foo {\n" +
            "  static bar = 'property'\n" +
            "}\n" +
            "def bar() {\n" +
            "  'method'\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "def test() {\n" +
            "  bar()\n" +
            "}\n" +
            "print test()\n",
        };
        //@formatter:on

        runConformTest(sources, "method");
    }

    @Test
    public void testCompileStatic8389a() {
        for (String imports : new String[] {"import static Foo.bar; import static Foo.baz", "import static Foo.*"}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                imports + "\n" +
                "class Foo {\n" +
                "  static Closure<String> bar = { -> 'property' }\n" +
                "  static Closure<String> baz = { -> 'property' }\n" +
                "}\n" +
                "String bar() {\n" +
                "  'method'\n" +
                "}\n" +
                "@groovy.transform.CompileStatic\n" +
                "def test() {\n" +
                "  bar() + ':' + baz()\n" +
                "}\n" +
                "print test()\n",
            };
            //@formatter:on

            runConformTest(sources, "method:property");
        }
    }

    @Test
    public void testCompileStatic8409() {
        for (char t : new char[] {'R', 'S', 'T', 'U'}) { // BiFunction uses R, T and U
            //@formatter:off
            String[] sources = {
                "Main.groovy",
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
    public void testCompileStatic8487() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  final list = ['foo','bar']\n" +
            "  for (item in list.iterator()) print item.toUpperCase()\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        if (isAtLeastGroovy(40)) {
            runConformTest(sources, "FOOBAR");
        } else {
            runNegativeTest(sources,
                "----------\n" +
                "1. ERROR in Main.groovy (at line 4)\n" +
                "\tfor (item in list.iterator()) print item.toUpperCase()\n" +
                "\t                                    ^^^^^^^^^^^^^^^^^^\n" +
                "Groovy:[Static type checking] - Cannot find matching method java.lang.Object#toUpperCase(). Please check if the declared type is correct and if the method exists.\n" +
                "----------\n");
        }
    }

    @Test
    public void testCompileStatic8499() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  [].stream().map{item,xxxx ->}\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 3)\n" +
            "\t[].stream().map{item,xxxx ->}\n" +
            "\t               ^^^^^^^^^^^^^^\n" +
            "Groovy:Incorrect number of parameters. Expected 1 but found 2\n" +
            "----------\n");
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
        //@formatter:off
        String[] sources = {
            "Main.groovy",
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
            "Groovy:[Static type checking] - Cannot call A#getFirstRecord(java.util.ArrayList<java.util.HashMap<java.lang.String, java.lang.Integer>>) with arguments [java.util.ArrayList<java.util.TreeMap<java.lang.String, java.lang.Integer>>]\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic8609d() {
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
            "Groovy:[Static type checking] - Cannot call A#getFirstRecord(java.util.ArrayList<java.util.HashMap<java.lang.String, java.lang.Integer>>) with arguments [java.util.ArrayList<java.util.HashMap<java.lang.String, java.lang.Long>>]\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic8609e() {
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
            "Groovy:[Static type checking] - Cannot call A#getFirstRecord(java.util.ArrayList<java.util.HashMap<java.lang.String, java.lang.Integer>>) with arguments [java.util.ArrayList<java.util.HashMap<java.lang.StringBuffer, java.lang.Integer>>]\n" +
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
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "def meth(obj) {\n" +
            "  boolean isA = (obj instanceof String && obj.equalsIgnoreCase('a'))\n" +
            "  obj.toLowerCase()\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in Main.groovy (at line 4)\n" +
            "\tobj.toLowerCase()\n" +
            "\t^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching method java.lang.Object#toLowerCase(). Please check if the declared type is correct and if the method exists.\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic8686a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "def meth(obj) {\n" +
            "  def str = obj instanceof String ? obj : obj.toString()\n" +
            "  obj.toLowerCase()\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "----------\n" +
            "1. ERROR in Main.groovy (at line 4)\n" +
            "\tobj.toLowerCase()\n" +
            "\t^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching method java.lang.Object#toLowerCase(). Please check if the declared type is correct and if the method exists.\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic8686b() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
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

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 9)\n" +
            "\tobj.toLowerCase()\n" +
            "\t^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching method java.lang.Object#toLowerCase(). Please check if the declared type is correct and if the method exists.\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic8693() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class C extends p.A {\n" +
            "  void m() {\n" +
            "    super.m()\n" + // StackOverflowError
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
    public void testCompileStatic8737() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "String m(String key, Object[] args) {\n" +
            "  \"key=$key, args=$args\"\n" +
            "}\n" +
            "String m(String key, Object[] args, Object[] parts) {\n" +
            "  \"key=$key, args=$args, parts=$parts\"\n" +
            "}\n" +
            "String m(String key, Object[] args, String[] names) {\n" +
            "  \"key=$key, args=$args, names=$names\"\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  print m('hello', ['world'] as Object[])\n" + // exact match for m(String,Object[])
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "key=hello, args=[world]");
    }

    @Test
    public void testCompileStatic8788() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.*\n" +
            "import static org.codehaus.groovy.transform.stc.StaticTypesMarker.*\n" +
            "\n" +
            "@CompileStatic void test(Set one, Set two) {\n" +
            "  @ASTTest(phase=INSTRUCTION_SELECTION, value={\n" +
            "    def target = node.rightExpression.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)\n" +
            "    assert target.declaringClass.name == 'java.util." + (isAtLeastGroovy(50) ? "Collection" : "Set") + "'\n" +
            "  })\n" +
            "  def three = one.intersect(two)\n" + // Set#intersect(Iterable) vs Collection#intersect(Collection)
            "  print three\n" +
            "}\n" +
            "test(Collections.emptySet(), Collections.emptySet())\n",
        };
        //@formatter:on

        runConformTest(sources, "[]");
    }

    @Test
    public void testCompileStatic8816() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  [0].each { -> }\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        if (isAtLeastGroovy(50)) {
            runNegativeTest(sources,
                "----------\n" +
                "1. ERROR in Main.groovy (at line 3)\n" +
                "\t[0].each { -> }\n" +
                "\t         ^^^^^^\n" +
                "Groovy:Incorrect number of parameters. Expected 1 but found 0\n" +
                "----------\n");
        } else {
            runConformTest(sources, "", "groovy.lang.MissingMethodException: No signature of method: Main$_test_closure1.doCall() is applicable for argument types: (Integer) values: [0]");
        }
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
    public void testCompileStatic8840() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "def test() {\n" +
            "  def list = [0, 1, 2, 3]\n" +
            "  for (int i in 1..2) {\n" +
            "    list[i - 1]++\n" +
            "  }\n" +
            "  list\n" +
            "}\n" +
            "print test()\n",
        };
        //@formatter:on

        runConformTest(sources, "[1, 2, 2, 3]");
    }

    @Test
    public void testCompileStatic8840a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "def test() {\n" +
            "  def list = [0, 1, 2, 3]\n" +
            "  List<Integer> other = [1]\n" +
            "  list[other[0]]++\n" +
            // ^^^^^^^^^^^^^^^^ works with casting to int/Integer: list[(int)other[0]]++
            // and up to 2.4.12 works if part of an expression like list[other[0] - 0]++
            "  return list\n" +
            "}\n" +
            "print test()\n",
        };
        //@formatter:on

        runConformTest(sources, "[0, 2, 2, 3]");
    }

    @Test
    public void testCompileStatic8840b() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "def test() {\n" +
            "  def list = [0, 1, 2, 3]\n" +
            "  List<Integer> other = [1]\n" +
            "  list[(int)other[0]]++\n" +
            "  return list\n" +
            "}\n" +
            "print test()\n",
        };
        //@formatter:on

        runConformTest(sources, "[0, 2, 2, 3]");
    }

    @Test
    public void testCompileStatic8840c() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "def test() {\n" +
            "  def list = [0, 1, 2, 3]\n" +
            "  List<Integer> other = [1]\n" +
            "  list[other.first()]++\n" +
            "  return list\n" +
            "}\n" +
            "print test()\n",
        };
        //@formatter:on

        runConformTest(sources, "[0, 2, 2, 3]");
    }

    @Test
    public void testCompileStatic8873() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
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

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic8946() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            /*
            "@GrabResolver(name='grails', root='https://repo.grails.org/grails/core')\n" +
            "@Grapes([\n" +
            "  @Grab('javax.servlet:javax.servlet-api:3.0.1'),\n" +
            "  @Grab('org.grails.plugins:converters:3.3.+'),\n" +
            "  @Grab('org.grails:grails-web:3.3.+')\n" +
            "])\n" +
            "@GrabExclude('org.codehaus.groovy:*')\n" +
            "import static grails.converters.JSON.parse\n" +
            */
            "class JSONElement {\n" +
            "  def getProperty(String name) {\n" +
            "    if (name == 'k') return [1,2]\n" +
            "  }\n" +
            "}\n" +
            "JSONElement parse(String json) {\n" +
            "  new JSONElement()\n" +
            "}\n" +

            "@groovy.transform.CompileStatic\n" +
            "def test() {\n" +
            "  def json = parse('[{\"k\":1},{\"k\":2}]')\n" +
            "  def vals = json['k']\n" +
            "  assert vals == [1,2]\n" +
            "  boolean result = 'k'.tokenize('.').every { token ->\n" + // 'k' represents a path like 'a.b.c.d'
            "    json = json[token]\n" +
            "  }\n" +
            "  assert result\n" +
            "  return json\n" + // ClassCastException: java.util.ArrayList cannot cast to org.grails.web.json.JSONElement
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic8955() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
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

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic8961() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "void setM(List<String> strings) {}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  m = Collections.emptyList()\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic8978() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
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
            "def tc = new TaskConfig().clone()\n" +
            "assert (tc instanceof TaskConfig)\n",
        };
        //@formatter:on

        runConformTest(sources);
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

        if (!isAtLeastGroovy(40)) {
            runConformTest(sources, "", "groovy.lang.MissingPropertyException: No such property: name for class: E");
        } else {
            runNegativeTest(sources,
                "----------\n" +
                "1. ERROR in Main.groovy (at line 6)\n" +
                "\tprint E.ONE.name\n" +
                "\t      ^^^^^\n" +
                "Groovy:Access to E#name is forbidden\n" +
                "----------\n");
        }
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

        if (!isAtLeastGroovy(40)) {
            runConformTest(sources, "", "groovy.lang.MissingPropertyException: No such property: ordinal for class: E");
        } else {
            runNegativeTest(sources,
                "----------\n" +
                "1. ERROR in Main.groovy (at line 6)\n" +
                "\tprint E.ONE.ordinal\n" +
                "\t      ^^^^^\n" +
                "Groovy:Access to E#ordinal is forbidden\n" +
                "----------\n");
        }
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

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 6)\n" +
            "\tprint value\n" +
            "\t      ^^^^^\n" +
            "Groovy:[Static type checking] - The variable [value] is undeclared.\n" +
            "----------\n");
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

        if (!isAtLeastGroovy(40)) {
            runConformTest(sources);
        } else {
            runNegativeTest(sources,
                "----------\n" +
                "1. ERROR in q\\More.groovy (at line 5)\n" +
                "\tprint VALUE\n" +
                "\t      ^^^^^\n" +
                "Groovy:Access to q.More#VALUE is forbidden\n" +
                "----------\n");
        }
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

        if (!isAtLeastGroovy(40)) {
            runConformTest(sources);
        } else {
            runNegativeTest(sources,
                "----------\n" +
                "1. ERROR in q\\More.groovy (at line 5)\n" +
                "\tprint VALUE\n" +
                "\t      ^^^^^\n" +
                "Groovy:Access to q.More#VALUE is forbidden\n" +
                "----------\n");
        }
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

    @Test
    public void testCompileStatic9086() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
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
            "Main.groovy",
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
            "Main.groovy",
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
            "Main.groovy",
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
            "Main.groovy",
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
            "Main.groovy",
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
            "Main.groovy",
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
            "Main.groovy",
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
            "  new C1().with(block)\n" +
            "}\n" +
            "void inner(@DelegatesTo(value = C2, strategy = Closure.DELEGATE_FIRST) Closure block) {\n" +
            "  new C2().with(block)\n" +
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
            "Main.groovy",
            "class C1 {\n" +
            "  String getP() {\n" +
            "    'outer delegate'\n" +
            "  }\n" +
            "}\n" +
            "class C2 {\n" +
            "  String getP() {\n" +
            "    'inner delegate'\n" +
            "  }\n" +
            "}\n" +
            "void outer(@DelegatesTo(value = C1, strategy = Closure.DELEGATE_FIRST) Closure block) {\n" +
            "  new C1().with(block)\n" +
            "}\n" +
            "void inner(@DelegatesTo(value = C2, strategy = Closure.DELEGATE_FIRST) Closure block) {\n" +
            "  new C2().with(block)\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  outer {\n" +
            "    inner {\n" +
            "      this.print(owner.p)\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "outer delegate");
    }

    @Test
    public void testCompileStatic9089b() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
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
            "void outer(@DelegatesTo(value = C1) Closure block) {\n" +
            "  block.delegate = new C1()\n" +
            "  block.call()\n" +
            "}\n" +
            "void inner(@DelegatesTo(value = C2) Closure block) {\n" +
            "  block.delegate = new C2()\n" +
            "  block.call()\n" +
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
            "Main.groovy",
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
    public void testCompileStatic9132() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import java.util.function.Function\n" +
            "def <R> R transform(Function<? super String, ? extends R> f) {\n" +
            "  f.apply('foo')\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  print(transform(String::length) * 3)\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "9");
    }

    @Test
    public void testCompileStatic9136() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
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
            "Main.groovy",
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
            "Main.groovy",
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
            "Main.groovy",
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

    @Test
    public void testCompileStatic9151() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void greet(Object o = 'world', String s = o) {\n" +
            "  print \"hello $s\"\n" +
            "}\n" +
            "greet()\n",
        };
        //@formatter:on

        runConformTest(sources, "hello world");
    }

    @Test
    public void testCompileStatic9151and10104() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void greet(Object o = 'world', String s = o.toString()) {\n" +
            "  print \"hello $s\"\n" +
            "}\n" +
            /*void greet() {
              Object o = 'world'
              greet(o, (String)o.toString()) // IncompatibleClassChangeError: Expected static method java.lang.Object.toString()Ljava/lang/String;
            }*/
            "greet()\n",
        };
        //@formatter:on

        runConformTest(sources, "hello world");
    }

    @Test
    public void testCompileStatic9151b() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Thing {\n" +
            "  Thing(Object o = 'foo', String s = o) {\n" +
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
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void meth() {\n" +
            "  File temp = File.createTempDir()\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic9204() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main extends Three {\n" +
            "  static main(args) {\n" +
            "    print newInstance().test()\n" +
            "  }\n" +
            "  def test() {\n" +
            "    field.meth() // typeof(field) should be A\n" +
            "    //    ^^^^ Cannot find matching method java.lang.Object#meth()\n" +
            "  }\n" +
            "}\n",

            "J.java",
            "public class J {\n" +
            "  public String meth() {\n" +
            "    return \"works\";\n" +
            "  }\n" +
            "}\n" +
            "abstract class One<T extends J> {\n" +
            "  protected T field;\n" +
            "}\n" +
            "abstract class Two<T extends J> extends One<T> {\n" +
            "}\n" +
            "abstract class Three extends Two<J> {\n" +
            "  {\n" +
            "    field = new J();\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic9265() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
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
            "Main.groovy",
            "class Main {\n" +
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
    public void testCompileStatic9288() {
        for (String p : new String[] {"a", "b"}) {
            for (String q : new String[] {"", "this.", "thisObject.", "owner."}) {
                for (String mod : new String[] {"public", "protected", "@groovy.transform.PackageScope"}) {
                    //@formatter:off
                    String[] sources = {
                        "Main.groovy",
                        "print new " + p + ".B().m()\n",

                        "a/A.groovy",
                        "package a\n" +
                        "@groovy.transform.CompileStatic\n" +
                        "abstract class A {\n" +
                        "  " + mod + " String f = 'value'\n" +
                        "  abstract String m()\n" +
                        "}\n",

                        p + "/B.groovy",
                        "package " + p + "\n" +
                        "@groovy.transform.CompileStatic\n" +
                        "class B extends a.A {\n" +
                        "  @Override\n" +
                        "  String m() {\n" +
                        "    'whatever'.with {\n" +
                        "      return " + q + "f\n" + // field from super class
                        "    }\n" +
                        "  }\n" +
                        "}\n",
                    };
                    //@formatter:on

                    String stderr = "", stdout = "value";
                    if ("b".equals(p) && mod.endsWith("PackageScope") && Float.parseFloat(System.getProperty("java.specification.version")) > 8) {
                        stderr = "groovy.lang.MissingPropertyException: No such property: f for class: " + (!q.isEmpty() ? "b.B" : "java.lang.String");
                        stdout = "";
                    }
                    runConformTest(sources, stdout, stderr);
                }
            }
        }
    }

    @Test
    public void testCompileStatic9292() {
        for (String p : new String[] {"a", "b"}) {
            for (String q : new String[] {"", "it.", "owner.", "delegate."}) {
                for (String mod : new String[] {"public", "protected", "@groovy.transform.PackageScope"}) {
                    //@formatter:off
                    String[] sources = {
                        "Main.groovy",
                        "print new " + p + ".B().m()\n",

                        "a/A.groovy",
                        "package a\n" +
                        "@groovy.transform.CompileStatic\n" +
                        "abstract class A {\n" +
                        "  " + mod + " String f = 'value'\n" +
                        "  abstract String m()\n" +
                        "}\n",

                        p + "/B.groovy",
                        "package " + p + "\n" +
                        "@groovy.transform.CompileStatic\n" +
                        "class B extends a.A {\n" +
                        "  @Override\n" +
                        "  String m() {\n" +
                        "    this.with {\n" +
                        "      return " + q + "f\n" + // field from super class
                        "    }\n" +
                        "  }\n" +
                        "}\n",
                    };
                    //@formatter:on

                    String stderr = "", stdout = "value";
                    if ("b".equals(p) && mod.endsWith("PackageScope") && Float.parseFloat(System.getProperty("java.specification.version")) > 8) {
                        stderr = "groovy.lang.MissingPropertyException: No such property: f for class: b.B";
                        stdout = "";
                    }
                    runConformTest(sources, stdout, stderr);
                }
            }
        }
    }

    @Test
    public void testCompileStatic9327() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  def runner = new Runnable() {\n" +
            "    @Override void run() {\n" +
            "      unknown\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 5)\n" +
            "\tunknown\n" +
            "\t^^^^^^^\n" +
            "Groovy:[Static type checking] - The variable [unknown] is undeclared.\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic9327a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.TypeChecked\n" +
            "void test() {\n" +
            "  def runner = new Runnable() {\n" +
            "    @Override void run() {\n" +
            "      unknown\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 5)\n" +
            "\tunknown\n" +
            "\t^^^^^^^\n" +
            "Groovy:[Static type checking] - The variable [unknown] is undeclared.\n" +
            "----------\n");
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
        assumeTrue(isParrotParser());

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
        assumeTrue(isParrotParser());

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
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
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
            "Main.groovy",
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
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
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
            "Main.groovy",
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
            "Main.groovy",
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
            "1. ERROR in Main.groovy (at line 7)\n" +
            "\tmeth(c)\n" +
            "\t^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot call Main#meth(java.lang.Class<? extends java.lang.CharSequence>) with arguments [java.lang.Class<?>]\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic9338a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
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
            "1. ERROR in Main.groovy (at line 7)\n" +
            "\tmeth(c)\n" +
            "\t^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot call Main#meth(java.lang.Class<? super java.lang.CharSequence>) with arguments [java.lang.Class<?>]\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic9340() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  static main(args) {\n" +
            "    this.newInstance().test()\n" +
            "  }\n" +
            "  @groovy.transform.CompileStatic\n" +
            "  void test() {\n" +
            "    java.util.function.Consumer<Main> consumer = main -> print('works')\n" + // A transform used a generics containing ClassNode Main for the method ...
            "    consumer.accept(this)\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic9342() {
        assumeTrue(isParrotParser());

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
    public void testCompileStatic9344() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class A {}\n" +
            "class B {}\n" +
            "@groovy.transform.CompileStatic\n" +
            "def test() {\n" +
            "  def var\n" +
            "  var = new A()\n" +
            "  def c = { ->\n" +
            "    var = new B()\n" + // Cannot cast object 'B@4e234c52' with class 'B' to class 'A'
            "    print var.class.simpleName\n" +
            "  }\n" +
            "  c.call()\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "B");
    }

    @Test
    public void testCompileStatic9344a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class A {}\n" +
            "class B {}\n" +
            "@groovy.transform.CompileStatic\n" +
            "def test() {\n" +
            "  def var\n" +
            "  var = new A()\n" +
            "  def c = { ->\n" +
            "    var = new B()\n" + // Cannot cast object 'B@4e234c52' with class 'B' to class 'A'
            "  }\n" +
            "  c.call()\n" +
            "  print var.class.simpleName\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "B");
    }

    @Test
    public void testCompileStatic9347() {
        assumeTrue(isParrotParser());

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

    @Test
    public void testCompileStatic9385() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  private int i\n" +
            "  int test() {\n" +
            "    { ->\n" +
            "      i += 1\n" +
            "    }.call()\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    print new Main().test()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "1");
    }

    @Test
    public void testCompileStatic9385a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  private int i\n" +
            "  int test() {\n" +
            "    { ->\n" +
            "      ++i\n" +
            "    }.call()\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    print new Main().test()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "1");
    }

    @Test
    public void testCompileStatic9385b() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  private int i\n" +
            "  int test() {\n" +
            "    { ->\n" +
            "      i++\n" +
            "    }.call()\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    print new Main().test()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "0");
    }

    @Test
    public void testCompileStatic9389() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  static main(args) {\n" +
            "    print new Pogo().integer++\n" +
            "  }\n" +
            "}\n",

            "Pogo.groovy",
            "class Pogo {\n" +
            "  Integer integer = 0\n" +
            "  Integer getInteger() { return integer }\n" +
            "  void setInteger(Integer i) { integer = i }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "0");
    }

    @Test
    public void testCompileStatic9389a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  static main(args) {\n" +
            "    print(++(new Pogo().integer))\n" +
            "  }\n" +
            "}\n",

            "Pogo.groovy",
            "class Pogo {\n" +
            "  Integer integer = 0\n" +
            "  Integer getInteger() { return integer }\n" +
            "  void setInteger(Integer i) { integer = i }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "1");
    }

    @Test
    public void testCompileStatic9389b() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  static main(args) {\n" +
            "    print new Pogo().integer++\n" +
            "  }\n" +
            "}\n",

            "Pogo.groovy",
            "class Pogo {\n" +
            "  Integer integer = 0\n" +
            "  Integer getInteger() { return integer }\n" +
            "  void setInteger(Character c) { integer = c as int }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 4)\n" +
            "\tprint new Pogo().integer++\n" +
            "\t      ^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot assign value of type java.lang.Integer to variable of type java.lang.Character\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic9420() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test(Map<String,String> map, Object key) {\n" +
            "  String str = map[key]\n" + // Map#getAt(Object) vs Object#getAt(String)
            "}\n" +
            "test([:],'')\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic9422() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class A {\n" +
            "  List<String> test() {\n" +
            "    ['x'].collect { String s ->\n" +
            "      new B(s).prop\n" +
            "    }\n" +
            "  }\n" +
            "  class B {\n" +
            "    B(param) {}\n" +
            "    String prop = 'works'\n" +
            "  }\n" +
            "}\n" +
            "print new A().test()[0]\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic9454() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "interface Face {\n" +
            "}\n" +
            "class Impl implements Face {\n" +
            "  String something\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class Task<R extends Face> implements java.util.concurrent.Callable<String> {\n" +
            "  R request\n" +
            "  \n" +
            "  @Override\n" +
            "  String call() {\n" +
            "    if (request instanceof Impl) {\n" +
            "      request.something.toLowerCase()\n" +
            "    } else {\n" +
            "      null\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "def task = new Task<Impl>(request: new Impl(something: 'WORKS'))\n" +
            "print task.call()",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic9500() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "trait Entity<D> {\n" +
            "}\n" +
            "@groovy.transform.CompileStatic @SuppressWarnings('rawtypes')\n" +
            "abstract class Path<F extends Entity, T extends Entity> implements Iterable<Path.Segment<F,T>> {\n" +
            "  interface Segment<F, T> {\n" +
            "    F start()\n" +
            "    T end()\n" +
            "  }\n" +
            "  abstract F start()\n" +
            "  T end\n" +
            "  T end() {\n" +
            "    end\n" + // Cannot return value of type Path$Segment<F,T> on method returning type T
            "  }\n" +
            "  @Override\n" +
            "  void forEach(java.util.function.Consumer<? super Segment<F, T>> action) {\n" +
            "  }\n" +
            "  @Override\n" +
            "  Spliterator<Segment<F, T>> spliterator() {\n" +
            "  }\n" +
            "  @Override\n" +
            "  Iterator<Segment<F, T>> iterator() {\n" +
            "  }\n" +
            "}\n" +
            "null\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic9517() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void listSortedFiles(File directory) {\n" +
            "  File[] files = directory.listFiles()\n" +
            "  files = files?.sort { it.name }\n" +
            "}\n" +
            "println 'works'\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic9524() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class W {\n" +
            "  enum X {\n" +
            "        Y {\n" +
            "      def z() {\n" +
            "        truncate('123', 2)\n" +
            "      }\n" +
            "    }\n" +
            "    abstract def z()\n" +
            "    private String truncate(String input, int maxLength) {\n" +
            "      input.substring(0, maxLength)\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "print W.X.Y.z()\n",
        };
        //@formatter:on

        runConformTest(sources, "12");
    }

    @Test
    public void testCompileStatic9555() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "interface I<T> {\n" +
            "}\n" +
            "class C implements I<Object> {\n" +
            "}\n" +
            "interface Factory {\n" +
            "  def <T extends I<?>> T getInstance(Class<T> clazz)\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test(Factory f) {\n" +
            "  C c = f.getInstance(C)\n" +
            "  assert c instanceof C\n" +
            "  assert c instanceof I\n" +
            "}\n" +
            "test { Class clazz -> clazz.newInstance() }\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic9558() {
        for (String dgm : new String[] {"tap", "with"}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                "@groovy.transform.CompileStatic\n" +
                "void test() {\n" +
                "  def config = new org.codehaus.groovy.control.CompilerConfiguration()\n" +
                "  config." + dgm + " {\n" +
                "    optimizationOptions['indy'] = true\n" + // Cannot cast object '...' with class 'CompilerConfiguration' to class 'Map'
                "    optimizationOptions.indy = true\n" +
                "  }\n" +
                "}\n" +
                "test()\n",
            };
            //@formatter:on

            runConformTest(sources);
        }
    }

    @Test
    public void testCompileStatic9562() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "abstract class One {\n" +
            "  int prop = 1\n" +
            "}\n" +
            "abstract class Two {\n" +
            "  int prop = 2\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class Foo extends One {\n" +
            "  def bar() {\n" +
            "    new Two() {\n" +
            "      def baz() {\n" +
            "        prop\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "print new Foo().bar().baz()\n",
        };
        //@formatter:on

        runConformTest(sources, "2");
    }

    @Test
    public void testCompileStatic9581() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class One {\n" +
            "  static boolean foo = true\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class Two extends One {\n" +
            "  def bar() {\n" +
            "    def baz = { ->\n" +
            "      foo\n" +
            "    }\n" +
            "    baz()\n" +
            "  }\n" +
            "}\n" +
            "print new Two().bar()\n",
        };
        //@formatter:on

        runConformTest(sources, "true");
    }

    @Test
    public void testCompileStatic9597() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import groovy.transform.stc.*\n" +
            "class A {\n" +
            "  def <T> void proc(Collection<T> values, @ClosureParams(FirstParam.FirstGenericType) Closure<String> block) {\n" +
            "    print block(values.first())\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class B {\n" +
            "  List<Integer> list = [1,2,3]\n" +
            "  void test(A a) {\n" +
            "    a.proc(this.list) { it.toBigDecimal().toString() }\n" + // works
            "    a.with {\n" +
            "      proc(this.list) { it.toBigDecimal().toString() }\n" + // error
            "    }\n" +
            "  }\n" +
            "}\n" +
            "new B().test(new A())\n",
        };
        //@formatter:on

        runConformTest(sources, "11");
    }

    @Test
    public void testCompileStatic9603() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test(Map<String, Object> map) {\n" +
            "  map.put('proper', [key: 'abc'])\n" +
            "  assert map.proper['key'] == 'abc'\n" +
            "  map['proper'] = [key: 'def']\n" +
            "  assert map.proper['key'] == 'def'\n" +
            "  map.proper = [key: 'ghi']\n" +
            "  assert map.proper['key'] == 'ghi'" +
            "}\n" +
            "test([:])\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic9604() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class C {\n" +
            "  void m() {\n" +
            "    { ->\n" +
            "      print resolveStrategy\n" +
            "      print getResolveStrategy()\n" +
            "    }();\n" +
            "  }\n" +
            "}\n" +
            "new C().m()\n",
        };
        //@formatter:on

        runConformTest(sources, "00");
    }

    @Test
    public void testCompileStatic9607() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "void helper(Runnable runner) {}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test(item, MetaProperty prop) {\n" +
            "  def name = prop.name\n" +
            "  helper(new Runnable() {\n" +
            "    void run() {\n" +
            "      if (item[name] != null) {\n" +
            //       ...
            "      }\n" +
            "    }\n" +
            "  })\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic9607a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "void helper(Runnable runner) {}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test(item, name, MetaProperty prop) {\n" +
            "  name = prop.name\n" +
            "  helper(new Runnable() {\n" +
            "    void run() {\n" +
            "      if (item[name] != null) {\n" +
            //       ...
            "      }\n" +
            "    }\n" +
            "  })\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testCompileStatic9609() {
        for (String mod : new String[] {"", "public", "protected", "@groovy.transform.PackageScope"}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                "abstract class A {\n" +
                "  " + mod + " def getX() { 'A' }\n" +
                "}\n" +
                "@groovy.transform.CompileStatic\n" +
                "class C extends A {\n" +
                "  def getX() {\n" +
                "    '' + super.x + 'C' \n" + // no stack overflow
                "  }\n" +
                "  def m() {\n" +
                "    '' + x + this.x + super.x\n" +
                "  }\n" +
                "}\n" +
                "print(new C().m())\n",
            };
            //@formatter:on

            runConformTest(sources, "ACACA");
        }
    }

    @Test
    public void testCompileStatic9635() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import java.util.function.Function\n" +
            "@groovy.transform.CompileStatic\n" +
            "class C<R extends Number> {\n" +
            "  def <V> V m(Function<C<?>, V> f) {\n" + // R from C is confused with R from Function
            "    V result = f.apply(this)\n" +
            "    return result\n" +
            "  }\n" +
            "}\n" +
            "print new C<Integer>().m(new Function<C<?>, String>() {\n" +
            "  @Override\n" +
            "  String apply(C<?> that) {\n" +
            "    return 'works'\n" +
            "  }\n" +
            "})",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic9652() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Node {\n" +
            "  String name, text\n" +
            "}\n" +
            "class Root implements Iterable<Node> {\n" +
            "  @Override\n" +
            "  Iterator<Node> iterator() {\n" +
            "    return [\n" +
            "      new Node(name: 'term', text: 'foo'),\n" +
            "      new Node(name: 'dash', text: '-'  ),\n" +
            "      new Node(name: 'term', text: 'bar') \n" +
            "    ].iterator()\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  Root root = new Root()\n" +
            "  root[0].with {\n" +
            "    assert name == 'term'\n" +
            "    assert text == 'foo'\n" +
            "  }\n" +
            "  root[1].with {\n" +
            "    assert name == 'dash'\n" +
            "    assert text == '-'\n" + // GroovyCastException: Cannot cast object 'Main@b91d8c4' with class 'Main' to class 'Node'
            "  }\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic9653() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class C {\n" +
            "  C() {\n" +
            "    print new D().with {\n" +
            "      something = 'value'\n" + // ClassCastException: D cannot be cast to C
            "      return object\n" +
            "    }\n" +
            "  }\n" +
            "  void setSomething(value) { }\n" +
            "}\n" +
            "class D {\n" +
            "  void setSomething(value) { }\n" +
            "  Object getObject() { 'works' }\n" +
            "}\n" +
            "new C()\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic9699() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class A {\n" +
            "  private static final java.util.regex.Pattern PATTERN = ~/.*/\n" +
            "  void checkList() {\n" +
            "    def list = []\n" +
            "    def closure = { ->\n" +
            "      list << PATTERN.pattern()\n" +
            "    }\n" +
            "    closure()\n" +
            "  }\n" +
            "  void checkMap() {\n" +
            "    def map = [:]\n" +
            "    def closure = { ->\n" +
            "      map[PATTERN.pattern()] = 1\n" +
            "    }\n" +
            "    closure()\n" +
            "  }\n" +
            "}\n" +
            "class B extends A {\n" +
            "}\n" +
            "new A().checkList()\n" +
            "new B().checkList()\n" +
            "new A().checkMap()\n" +
            "new B().checkMap()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic9704() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "def test() {\n" +
            "  long x = 42L\n" +
            "  x = ~x\n" +
            "}\n" +
            "print test()\n",
        };
        //@formatter:on

        runConformTest(sources, "-43");
    }

    @Test
    public void testCompileStatic9734() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "void m(List<String> strings) {}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  m(Collections.emptyList())\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic9737() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class C extends p.A {\n" +
            "  void test() {\n" +
            "    m('')\n" + // VerifyError: Bad access to protected data in invokevirtual
            "  }\n" +
            "}\n" +
            "new C().test()\n",

            "p/A.groovy",
            "package p\n" +
            "@groovy.transform.CompileStatic\n" +
            "abstract class A {\n" +
            "  static void m(Integer i) { print 'int' }\n" +
            "  protected void m(String s) { print 'str' }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "str");
    }

    @Test
    public void testCompileStatic9737a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "abstract class A {\n" +
            "  static void m(Integer i) { print 'int' }\n" +
            "  protected void m(String s) { print 'str' }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class C extends A {\n" +
            "  void test() {\n" +
            "    m('')\n" + // ClassCastException: class java.lang.Class cannot be cast to class A
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "str");
    }

    @Test
    public void testCompileStatic9762() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "static <T> List<T> list(T item) {\n" +
            "  return [item]\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  Optional<Integer> opt = Optional.ofNullable(123)\n" +
            "  List<Integer> result = opt.map(this::list).get()\n" +
            "  print result\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "[123]");
    }

    @Test
    public void testCompileStatic9771() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  private final Map<String, Boolean> map = [:]\n" +
            "  void test() {\n" +
            "    { ->\n" +
            "      map['key'] = true\n" +
            "    }.call()\n" +
            "    print map\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    newInstance().test()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[key:true]");
    }

    @Test
    public void testCompileStatic9786() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "interface I {\n" +
            "  void m()\n" +
            "}\n" +
            "class A implements I {\n" +
            "  void m() { print 'A' }\n" +
            "}\n" +
            "class B implements I {\n" +
            "  void m() { print 'B' }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  I x\n" +
            "  def y = false\n" +
            "  def z = true \n" +
            "  if (y) {\n" +
            "    x = new A()\n" +
            "  } else if (z) {\n" +
            "    x = new B()\n" +
            "  }\n" +
            "  x.m()\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "B");
    }

    @Test
    public void testCompileStatic9790() {
        assumeTrue(isParrotParser());

        for (Object sig : new String[] {"i", "(int i)", "(Integer i)"}) {
            //@formatter:off
            String[] sources = {
                "Script.groovy",
                "@groovy.transform.CompileStatic\n" +
                "void test() {\n" +
                "  java.util.stream.IntStream.range(0, 2).forEach(\n" +
                "    " + sig + " -> { assert i >= 0 && i < 2 }\n" +
                "  )\n" +
                "}\n" +
                "test()\n",
            };
            //@formatter:on

            runConformTest(sources);
        }
    }

    @Test
    public void testCompileStatic9799() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C {\n" +
            "  String x\n" +
            "}\n" +
            "class D {\n" +
            "  String x\n" +
            "  static D from(C c) {\n" +
            "    new D(x: c.x)\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test(C c) {\n" +
            "  print Optional.of(c).map(D::from).map(D::getX).get()\n" +
            "}\n" +
            "test(new C(x: 'works'))\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic9853() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import static java.util.stream.Collectors.toMap\n" +
            "import java.util.function.Function\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  print(['a','bc','def'].stream().collect(toMap(Function.<String>identity(), CharSequence::length)))\n" +
            "}\n" + // <T,K,U> Collector<T,?,Map<K,U>> toMap(Function<? super T,? extends K>,Function<? super T,? extends U>)
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "[a:1, bc:2, def:3]");
    }

    @Test
    public void testCompileStatic9855() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "@SuppressWarnings(C.PREFIX + 'checked')\n" + // not 'un'.plus('checked')
            "class C {\n" +
            "  public static final String PREFIX = 'un'\n" +
            "}\n" +
            "new C()\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. WARNING in Main.groovy (at line 2)\n" +
            "\t@SuppressWarnings(C.PREFIX + 'checked')\n" +
            "\t                  ^^^^^^^^^^^^^^^^^^^^\n" +
            "Unnecessary @SuppressWarnings(\"unchecked\")\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic9860() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class C {\n" +
            "  static <T> void test() {\n" +
            "    def bind = { T a, T b ->\n" +
            "      return new Tuple2<T, T>(a, b)\n" +
            "    }\n" +
            "    print bind('foo', 'bar')\n" +
            "  }\n" +
            "}\n" +
            "C.test()\n",
        };
        //@formatter:on

        runConformTest(sources, "[foo, bar]");
    }

    @Test
    public void testCompileStatic9863() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class C {\n" +
            "  double getSomeValue() {\n" +
            "    0.0d\n" +
            "  }\n" +
            "  double test() {\n" +
            "    1.0d + someValue\n" +
            "  }\n" +
            "}\n" +
            "print new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "1.0");
    }

    @Test
    public void testCompileStatic9872() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class C {\n" +
            "  private Map<String, String> map = [:]\n" +
            "  void test() {\n" +
            "    ['kv'].each {\n" +
            "      map[it] = it\n" +
            "    }\n" +
            "    print map\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "[kv:kv]");
    }

    @Test
    public void testCompileStatic9881() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  print new Value<>(123).replace { -> 'foo' }\n" +
            "  print new Value<>(123).replace { Integer v -> 'bar' }\n" +
            "}\n" +
            "test()\n",

            "Value.groovy",
            "import java.util.function.*\n" +
            "class Value<V> {\n" +
            "  final V val\n" +
            "  Value(V v) {\n" +
            "    this.val = v\n" +
            "  }\n" +
            "  String toString() {\n" +
            "    val as String\n" +
            "  }\n" +
            "  def <T> Value<T> replace(Supplier<T> supplier) {\n" +
            "    new Value<>(supplier.get())\n" +
            "  }\n" +
            "  def <T> Value<T> replace(Function<? super V, ? extends T> function) {\n" +
            "    new Value(function.apply(val))\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "foobar");
    }

    @Test
    public void testCompileStatic9881a() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  print new Value<>(123).replace(() -> 'foo')\n" +
            "  print new Value<>(123).replace((Integer v) -> 'bar')\n" +
            "}\n" +
            "test()\n",

            "Value.groovy",
            "import java.util.function.*\n" +
            "class Value<V> {\n" +
            "  final V val\n" +
            "  Value(V v) {\n" +
            "    this.val = v\n" +
            "  }\n" +
            "  String toString() {\n" +
            "    val as String\n" +
            "  }\n" +
            "  def <T> Value<T> replace(Supplier<T> supplier) {\n" +
            "    new Value<>(supplier.get())\n" +
            "  }\n" +
            "  def <T> Value<T> replace(Function<? super V, ? extends T> function) {\n" +
            "    new Value(function.apply(val))\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "foobar");
    }

    @Test
    public void testCompileStatic9882() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import java.util.function.Supplier\n" +
            "@groovy.transform.CompileStatic\n" +
            "class C {\n" +
            "  Supplier<String> p = { 'foo' }\n" +
            "  void test() {\n" +
            "    Supplier<String> v = { 'bar' }\n" +
            "    print(p.get() + v.get())\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "foobar");
    }

    @Test
    public void testCompileStatic9883() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class C {\n" +
            "  java.util.function.Supplier<String> p = {\n" +
            "    return java.util.UUID.randomUUID()\n" +
            "  }\n" +
            "}\n" +
            "print new C().p.get().class\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 4)\n" +
            "\treturn java.util.UUID.randomUUID()\n" +
            "\t       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot return value of type java.util.UUID for closure expecting java.lang.String\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic9885() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.ToString\n" +
            "class C {\n" +
            "  String p\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test(String string, whatever) {\n" +
            "  print new C(p: string.trim() ?: \"$whatever\")\n" +
            "}\n" +
            "test('x','y')\n" +
            "test(' ','y')\n" +
            "test(' ',123)\n",
        };
        //@formatter:on

        runConformTest(sources, "C(x)C(y)C(123)");
    }

    @Test
    public void testCompileStatic9890() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C implements I {\n" +
            "  void m(String s) {\n" +
            "    throw new Exception()\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  new C().m(42L)\n" +
            "}\n" +
            "test()\n",

            "I.java",
            "public interface I {\n" +
            "  default void m(long n) {\n" +
            "    System.out.print(n);\n" +
            "  }\n" +
            "  void m(String s);\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testCompileStatic9892() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class C {\n" +
            "  int prefix\n" +
            "  int postfix\n" +
            "  def test() {\n" +
            "    { ->\n" +
            "      print \"X${++prefix}Y${postfix++}\"\n" +
            "    }.call()\n" +
            "    true\n" +
            "  }\n" +
            "}\n" +
            "def c = new C()\n" +
            "assert c.test()\n" +
            "assert c.prefix == 1\n" +
            "assert c.postfix == 1\n",
        };
        //@formatter:on

        runConformTest(sources, "X1Y0");
    }

    @Test
    public void testCompileStatic9893() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "abstract class A {\n" +
            "  void setX(String s) { print 'String' }\n" +
            "}\n" +
            "class C extends A {\n" +
            "  void setX(boolean b) { print 'boolean' }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  def c = new C()\n" +
            "  c.x = 'value'\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "String");
    }

    @Test
    public void testCompileStatic9893a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "interface I {\n" +
            "  void setX(String s)\n" +
            "}\n" +
            "abstract class A implements I {\n" +
            "  void setX(boolean b) { print 'boolean' }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test(A a) {\n" +
            "  a.x = 'value'\n" +
            "}\n" +
            "test(new A() { void setX(String s) { print 'String' } })\n",
        };
        //@formatter:on

        runConformTest(sources, "String");
    }

    @Test(expected = AssertionError.class)
    public void testCompileStatic9909() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import p.*\n" +
            "@groovy.transform.CompileStatic\n" +
            "class C implements A, B {\n" +
            "  void m() {\n" +
            "    A.super.m()\n" +
            "  }\n" +
            "  void test() {\n" +
            "    m()\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",

            "p/A.java",
            "package p;\n" +
            "public interface A {\n" +
            "  default void m() {\n" +
            "    System.out.print(\"A\");\n" +
            "  }\n" +
            "}\n",

            "p/B.java",
            "package p;\n" +
            "public interface B {\n" +
            "  default void m() {\n" +
            "    System.out.print(\"B\");\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "A");
    }

    @Test
    public void testCompileStatic9918() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "def m(one, ... zeroOrMore) {  }\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  Object[] array = ['a', 'b']\n" +
            "  m(array)\n" + // ouch!
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic9938() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  interface I {\n" +
            "    void m(@DelegatesTo(value=D, strategy=Closure.DELEGATE_FIRST) Closure<?> c)\n" +
            "  }\n" +
            "  static class C implements I {\n" +
            "    void m(@DelegatesTo(value=D, strategy=Closure.DELEGATE_FIRST) Closure<?> c) {\n" +
            "      new D().with(c)\n" +
            "    }\n" +
            "  }\n" +
            "  static class D {\n" +
            "    void f() {\n" +
            "      print 'works'\n" +
            "    }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    new C().m { f() }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic9938a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  interface I {\n" +
            "    void m(@DelegatesTo(value=D, strategy=Closure.DELEGATE_FIRST) Closure<?> c)\n" +
            "  }\n" +
            "  static class X implements I {\n" +
            "    void m(@DelegatesTo(value=D, strategy=Closure.DELEGATE_FIRST) Closure<?> c) {\n" +
            "      new D().with(c)\n" +
            "    }\n" +
            "  }\n" +
            "  static class C implements I {\n" +
            "    @Delegate(parameterAnnotations=true) X x = new X()\n" + // generates m(Closure) that delegates to X#m(Closure)
            "  }\n" +
            "  static class D {\n" +
            "    void f() {\n" +
            "      print 'works'\n" +
            "    }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    new C().m { f() }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic9955() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import p.Types.Public\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  assert Public.answer == 42\n" +
            "  assert Public.CONST == 'XX'\n" +
            "  assert Public.VALUE == null\n" +
            "  Public.VALUE = 'YY'\n" +
            "  assert Public.VALUE == 'YY'\n" +
            "  Public.@VALUE = 'ZZ'\n" +
            "  assert Public.@VALUE == 'ZZ'\n" +
            "}\n" +
            "test()\n",

            "p/Types.groovy",
            "package p\n" +
            "class Types {\n" +
            "  @groovy.transform.PackageScope static class PackagePrivate {\n" +
            "    public static Number getAnswer() { 42 }\n" +
            "    public static final String CONST = 'XX'\n" +
            "    public static String VALUE\n" +
            "  }\n" +
            "  public static class Public extends PackagePrivate {\n" +
            "  }\n" +
            "}",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic9967() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class A {\n" +
            "}\n" +
            "class B extends A {\n" +
            "  String p = 'foo'\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class C {\n" +
            "  String scenario1(x) {\n" +
            "    (x instanceof String) ? x.toLowerCase() : 'bar'\n" +
            "  }\n" +
            "  String scenario2(B x) {\n" +
            "    x.p\n" +
            "  }\n" +
            "  String scenario2a(B x) {\n" +
            "    x.getP()\n" +
            "  }\n" +
            "  String scenario3(B x) {\n" +
            "    (x instanceof B) ? x.p : 'bar'\n" +
            "  }\n" +
            "  String scenario3a(B x) {\n" +
            "    (x instanceof B) ? x.getP() : 'bar'\n" +
            "  }\n" +
            "  String scenario4(A x) {\n" +
            "    (x instanceof B) ? x.p : 'bar'\n" + // Access to A#p is forbidden
            "  }\n" +
            "  String scenario4a(A x) {\n" +
            "    (x instanceof B) ? x.getP() : 'bar'\n" +
            "  }\n" +
            "}\n" +
            "new C().with {\n" +
            "  assert scenario1(null) == 'bar'\n" +
            "  assert scenario1('Foo') == 'foo'\n" +
            "  assert scenario2(new B()) == 'foo'\n" +
            "  assert scenario2a(new B()) == 'foo'\n" +
            "  assert scenario3(new B()) == 'foo'\n" +
            "  assert scenario3a(new B()) == 'foo'\n" +

            "  assert scenario4(new A()) == 'bar'\n" +
            "  assert scenario4(new B()) == 'foo'\n" +
            "  assert scenario4a(new A()) == 'bar'\n" +
            "  assert scenario4a(new B()) == 'foo'\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic9973() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class C {\n" +
            "  private int f\n" +
            "  int getP() { f }\n" +
            "  Integer calc() { 1 - this.p }\n" +
            "  Integer calc(int i) { i - p }\n" +
            "}\n" +
            "def c = new C()\n" +
            "print c.calc()\n" +
            "print c.calc(2)\n",
        };
        //@formatter:on

        runConformTest(sources, "12");
    }

    @Test
    public void testCompileStatic10033() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  Main(java.util.function.Function<Main,String> f) {\n" +
            "    print f.apply(this)\n" +
            "  }\n" +
            "  String m() { 'works' }\n" +
            "  static main(args) {\n" +
            "    new Main(Main::m)\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic10033a() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  Main() {\n" +
            "    print 'works'\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    test(Main::new)\n" +
            "  }\n" +
            "  static test(java.util.function.Supplier<Main> f) {\n" +
            "    f.get()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic10047() {
        assumeTrue(isParrotParser());

        for (String value : new String[] {"String::length", "String.&length", "(String s) -> s.length()", "{String s -> s.length()}"}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                "import static java.util.stream.Collectors.toMap\n" +
                "import java.util.function.Function\n" +
                "@groovy.transform.CompileStatic\n" +
                "void test() {\n" +
                "  print(['a','bc','def'].stream().collect(toMap(Function.<String>identity(), " + value + ")))\n" +
                "}\n" + // <T,K,U> Collector<T,?,Map<K,U>> toMap(Function<? super T,? extends K>,Function<? super T,? extends U>)
                "test()\n",
            };
            //@formatter:on

            runConformTest(sources, "[a:1, bc:2, def:3]");
        }
    }

    @Test
    public void testCompileStatic10047a() {
        assumeTrue(isParrotParser());

        for (String value : new String[] {"s -> s.length()", "{s -> s.length()}"}) { // no type for "s" -- should this work?
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                "import static java.util.stream.Collectors.toMap\n" +
                "import java.util.function.Function\n" +
                "@groovy.transform.CompileStatic\n" +
                "void test() {\n" +
                "  print(['a','bc','def'].stream().collect(toMap(Function.<String>identity(), " + value + ")))\n" +
                "}\n" + // <T,K,U> Collector<T,?,Map<K,U>> toMap(Function<? super T,? extends K>,Function<? super T,? extends U>)
                "test()\n",
            };
            //@formatter:on

            runConformTest(sources, "[a:1, bc:2, def:3]");
        }
    }

    @Test
    public void testCompileStatic10047b() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import static java.util.stream.Collectors.toMap\n" +
            "import java.util.function.Function\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  print(['a','bc','def'].stream().collect(toMap(Function.<String>identity(), List::size)))\n" +
            "}\n" + // <T,K,U> Collector<T,?,Map<K,U>> toMap(Function<? super T,? extends K>,Function<? super T,? extends U>)
            "test()\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 5)\n" +
              "\tprint(['a','bc','def'].stream().collect(toMap(Function.<String>identity(), List::size)))\n" + (isAtLeastGroovy(50)
            ? "\t      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
              "Groovy:[Static type checking] - Cannot call <R,A> java.util.stream.Stream#collect(java.util.stream.Collector<? super java.lang.String, A, R>) with arguments [java.util.stream.Collector<java.util.List, ?, java.util.Map<java.lang.String, java.lang.Integer>>]\n"
            : "\t                                                                           ^^^^^^^^^^\n" +
              "Groovy:Failed to find class method 'size(java.lang.String)' or instance method 'size()' for the type: java.util.List\n"
            ) +
            "----------\n");
    }

    @Test
    public void testCompileStatic10071() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  def c = { ... zeroOrMore -> 'foo' + zeroOrMore }\n" +
            "  assert c('bar', 'baz') == 'foo[bar, baz]'\n" +
            "  assert c('bar') == 'foo[bar]'\n" +
            "  assert c() == 'foo[]'\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic10072() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  def c = { p = 'foo' -> return p }\n" +
            "  assert c('bar') == 'bar'\n" +
            "  assert c() == 'foo'\n" +
            "  c = { p, q = 'baz' -> '' + p + q }\n" +
            "  assert c('foo', 'bar') == 'foobar'\n" +
            "  assert c('foo') == 'foobaz'\n" +
            "  c = { p, q = p.toString() -> '' + p + q }\n" +
            "  assert c('foo', 'bar') == 'foobar'\n" +
            "  assert c('foo') == 'foofoo'\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic10089() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test(... attributes) {\n" +
            "  List one = [\n" +
            "    [id:'x', options:[count:1]]\n" +
            "  ]\n" +
            "  List two = attributes.collect {\n" +
            "    def node = Collections.singletonMap('children', one)\n" +
            "    if (node) {\n" +
            "      node = node.get('children').find { child -> child['id'] == 'x' }\n" +
            "    }\n" +
            "    [id: it['id'], name: node['name'], count: node['options']['count']]\n" +
            //                                             ^^^^^^^^^^^^^^^ GroovyCastException (map ctor for Collection)
            "  }\n" +
            "  print two\n" +
            "}\n" +
            "test( [id:'x'] )\n",
        };
        //@formatter:on

        runConformTest(sources, "[[id:x, name:null, count:1]]");
    }

    @Test
    public void testCompileStatic10109() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "String test(String string) {\n" +
            "  new StringBuilder().with {\n" +
            "    int len = length()\n" + // IllegalAccessError
            "    append(string)\n" +
            "    toString()\n" +
            "  }\n" +
            "}\n" +
            "print test('works')\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic10197() {
        for (String override : new String[] {"int getBaz() {1}", "final int baz = 1"}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                "@groovy.transform.CompileStatic\n" +
                "enum Foo {\n" +
                "  BAR {\n" +
                "    " + override + "\n" +
                "  }\n" +
                "  int getBaz() { -1 }\n" +
                "}\n" +
                "print Foo.BAR.baz\n",
            };
            //@formatter:on

            runConformTest(sources, "1");
        }
    }

    @Test
    public void testCompileStatic10229() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class C {\n" +
            "  Map<String,?> a() {\n" +
            "  }\n" +
            "  Map<String,List<?>> b() {\n" +
            "    def c = { ->\n" +
            "      [\n" +
            "        a(), a()\n" +
            "      ]\n" +
            "    }\n" +
            "    c()\n" +
            "    null\n" +
            "  }\n" +
            "}\n" +
            "print new C().b()\n",
        };
        //@formatter:on

        runConformTest(sources, "null");

        checkDisassemblyFor("C$_b_closure1.class",
            "  // Signature: ()Ljava/util/" + (isAtLeastGroovy(50) ? "Array" : "") + "List<Ljava/util/Map<Ljava/lang/String;+Ljava/lang/Object;>;>;\n"); // not L?;
    }

    @Test // BiFunction and BinaryOperator with same type parameter
    public void testCompileStatic10282() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "String f() {\n" +
            "  def integers = java.util.stream.IntStream.range(0, 10).boxed()\n" +
            "  integers.reduce('', (s, i) -> s + '-', String::concat)\n" +
            "}\n" +
            "print f()\n",
        };
        //@formatter:on

        runConformTest(sources, "----------");
    }

    @Test
    public void testCompileStatic10308() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C<T> {\n" +
            "  T p\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  def x = { -> new C<String>() }\n" +
            "  def y = x()\n" +
            "  def z = y.p\n" +
            "  y = null\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testCompileStatic10319() {
        //@formatter:off
        String[] sources = {
            "p/Main.groovy",
            "package p\n" +
            "@groovy.transform.ToString(includeFields=true)\n" +
            "@groovy.transform.CompileStatic\n" +
            "class C implements Cloneable {\n" +
            "  private int[] array = [1]\n" +
            "  @Override\n" +
            "  C clone() {\n" +
            "    C c = (C) super.clone()\n" +
            "    c.array = array.clone()\n" +
            "    return c\n" +
            "  }\n" +
            "}\n" +
            "print new C().clone()\n",
        };
        //@formatter:on

        runConformTest(sources, "p.C([1])");
    }

    @Test
    public void testCompileStatic10375() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import java.util.function.Supplier\n" +
            "@groovy.transform.CompileStatic\n" +
            "class C {\n" +
            "  private String getX() {\n" +
            "    return 'works'\n" +
            "  }\n" +
            "  void test() {\n" +
            "    Supplier<String> s = () -> x\n" + // GroovyCastException
            "    print s.get()\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic10377() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test(a, b = a) {\n" +
            "  print(a === b)\n" +
            "  print(a !== b)\n" +
            "}\n" +
            "test(new Object())\n",
        };
        //@formatter:on

        runConformTest(sources, "truefalse");
        checkDisassemblyFor("Main.class", "if_acmpne"); // ===
        checkDisassemblyFor("Main.class", "if_acmpeq"); // !==
    }

    @Test
    public void testCompileStatic10379() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class C extends p.A {\n" +
            "  void test() {\n" +
            "    m('')\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",

            "p/A.groovy",
            "package p\n" +
            "abstract class A implements I {\n" +
            "  static void m(Number n) {\n" +
            "    print 'number'\n" +
            "  }\n" +
            "}\n",

            "p/I.java",
            "package p;\n" +
            "public interface I {\n" +
            "  default void m(String s) {\n" +
            "    System.out.print(\"string\");\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "string");
    }

    @Test
    public void testCompileStatic10380() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class C extends p.A {\n" +
            "  void test() {\n" +
            "    m()\n" + // IncompatibleClassChangeError: Found class C, but interface was expected
            "  }\n" +
            "}\n" +
            "new C().test()\n",

            "p/A.groovy",
            "package p\n" +
            "abstract class A implements I {\n" +
            "}\n",

            "p/I.java",
            "package p;\n" +
            "interface I {\n" +
            "  default void m() {\n" +
            "    System.out.print(\"works\");\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic10381() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class C implements p.A, p.B {\n" +
            "  void test() {\n" +
            "    m()\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",

            "p/A.java",
            "package p;\n" +
            "public interface A {\n" +
            "  default void m() {\n" +
            "  }\n" +
            "}\n",

            "p/B.java",
            "package p;\n" +
            "public interface B {\n" +
            "  default void m() {\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 2)\n" +
            "\tclass C implements p.A, p.B {\n" +
            "\t      ^\n" +
            "Duplicate default methods named m with the parameters () and () are inherited from the types A and B\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic10394() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class C {\n" +
            "  int i = 0, j = 1\n" +
            "  Integer getA() { i++ }\n" +
            "  Integer getB() { j++ }\n" +
            "  void test() {\n" +
            "    assert (a <=> b) == -1\n" +
            "    print i\n" +
            "    print j\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "12");
    }

    @Test
    public void testCompileStatic10395() {
        for (String type : new String[] {"int", "long", "short", "byte", "char", "double", "float"}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                "@groovy.transform.CompileStatic\n" +
                "int test(" + type + " a, " + type + " b) {\n" +
                "  a <=> b\n" +
                "}\n" +
                "assert test((" + type + ")0,(" + type + ")0) == 0\n" +
                "assert test((" + type + ")0,(" + type + ")1) < 0\n" +
                "assert test((" + type + ")1,(" + type + ")0) > 0\n",
            };
            //@formatter:on

            runConformTest(sources);

            String result = disassemble(getOutputFile("Main.class"), 1);
            int pos = result.indexOf("ScriptBytecodeAdapter.compareTo");
            assertTrue(pos < 0);
        }
    }

    @Test
    public void testCompileStatic10395b() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "int test(boolean a, boolean b) {\n" +
            "  a <=> b\n" +
            "}\n" +
            "assert test(false,false) == 0\n" +
            "assert test(false,true) < 0\n" +
            "assert test(true,false) > 0\n" +
            "assert test(true,true) == 0\n",
        };
        //@formatter:on

        runConformTest(sources);

        String result = disassemble(getOutputFile("Main.class"), 1);
        int pos = result.indexOf("ScriptBytecodeAdapter.compareTo");
        assertTrue(pos < 0);
    }

    @Test
    public void testCompileStatic10424() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Outer {\n" +
            "  private static class Inner {\n" +
            "    static class Three {}\n" +
            "  }\n" +
            "  void test() {\n" +
            "    def inner = new Inner()\n" +
            "    if (inner) {\n" + // optimized boolean expression; StackOverflowError
            "      print 'works'\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "new Outer().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic10457() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class C {\n" +
            "  @groovy.transform.CompileDynamic\n" +
            "  C() {\n" +
            "    print(new StringReader('works').text)\n" +
            "  }\n" +
            "}\n" +
            "new C()\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic10476() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  def list = []\n" +
            "  for (e in ['foo','bar','baz'].stream()) {\n" +
            "    list.add(e.toUpperCase())\n" +
            "  }\n" +
            "  print list\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "[FOO, BAR, BAZ]");
    }

    @Test
    public void testCompileStatic10557() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C {\n" +
            "  def <T> T m(java.util.function.Function<Reader,T> f) {\n" +
            "    new StringReader('').withCloseable { reader ->\n" +
            "      f.apply(reader)\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  print(new C().m { it.text.empty })\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "true");
    }

    @Test
    public void testCompileStatic10579() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  int[] numbers = [1,2,3,4,5]\n" +
            "  int sum = 0\n" +
            "  for (i in numbers) sum += i\n" +
            "  print sum\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "15");
    }

    @Test
    public void testCompileStatic10592() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  print(Face.getValue())\n" +
            "  print(Face.value)\n" +
            "}\n" +
            "test()\n",

            "Face.java",
            "public interface Face {\n" +
            "  static String getValue() {\n" +
            "    return \"works\";\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "worksworks");
    }

    @Test
    public void testCompileStatic10592a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Impl implements Face {  }\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test(Impl impl) {\n" +
            "  print(impl.getValue())\n" +
            "  print(impl.value)\n" +
            "}\n" +
            "test(new Impl())\n",

            "Face.java",
            "public interface Face {\n" +
            "  default String getValue() {\n" +
            "    return \"works\";\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "worksworks");
    }

    @Test
    public void testCompileStatic10714() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import java.util.function.*\n" +
            "class C {\n" +
            "  String which\n" +
            "  void m(int i) { which = 'int' }\n" +
            "  void m(Number n) { which = 'Number' }\n" +
            "}\n" +
            "interface I {\n" +
            "  I andThen(Consumer<? super Number> c)\n" +
            "  I andThen(BiConsumer<? super Number, ?> bc)\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test(I i, C c) {\n" +
            "  i = i.andThen(c::m)\n" + // "andThen" is ambiguous unless parameters of "m" overloads are taken into account
            "}\n" +
            "C x= new C()\n" +
            "test(new I() {\n" +
            "  I andThen(Consumer<? super Number> c) {\n" +
            "    c.accept(42)\n" +
            "    return this\n" +
            "  }\n" +
            "  I andThen(BiConsumer<? super Number, ?> bc) {\n" +
            "    bc.accept(1234, null)\n" +
            "    return this\n" +
            "  }\n" +
            "}, x)\n" +
            "print x.which\n",
        };
        //@formatter:on

        if (isAtLeastGroovy(40)) {
            runConformTest(sources, "Number");
        } else {
            runNegativeTest(sources,
                "----------\n" +
                "1. ERROR in Main.groovy (at line 13)\n" +
                "\ti = i.andThen(c::m)\n" +
                "\t    ^^^^^^^^^^^^^^^\n" +
                "Groovy:[Static type checking] - Reference to method is ambiguous. Cannot choose between [I I#andThen(java.util.function.Consumer<? super java.lang.Number>), I I#andThen(java.util.function.BiConsumer<? super java.lang.Number, ?>)]\n" +
                "----------\n");
        }
    }

    @Test
    public void testCompileStatic10725() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  List<String> list = ['foo','bar']\n" +
            "  Set<Map<String,String>> set_of_maps = []\n" +
            "  set_of_maps.addAll(list.collectEntries { [it, it.toUpperCase()] })\n" +
            "  print set_of_maps.first()\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "[foo:FOO, bar:BAR]");
    }

    @Test
    public void testCompileStatic10742() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "void foo(bar) { }\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  java.util.function.Function<?,String> f = this::foo\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 4)\n" +
            "\tjava.util.function.Function<?,String> f = this::foo\n" +
            "\t                                          ^^^^^^^^^\n" +
            "Groovy:Invalid return type: void is not convertible to java.lang.String\n" +
            "----------\n");
    }

    @Test
    public void testCompileStatic10791() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import java.util.function.*\n" +
            "@SuppressWarnings('rawtypes')\n" +
            "@groovy.transform.CompileStatic\n" +
            "void test(List list) {\n" +
            "  BiConsumer<List,Consumer> bc = List::forEach\n" + // default method of Iterator
            "  Consumer printer = { print(it) }\n" +
            "  bc.accept(list, printer)\n" +
            "}\n" +
            "test(['works'])\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testCompileStatic10807() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class C {\n" +
            "  public static final Comparator<String> BY_DISPLAY_NAME = Comparator.<String,String>comparing(C::getDisplayName)\n" +
            "  static String getDisplayName(String component) {\n" +
            "    return component\n" +
            "  }\n" +
            "}\n" +
            "def list = ['foo','bar','baz']\n" +
            "list.sort(C.BY_DISPLAY_NAME)\n" +
            "print list\n",
        };
        //@formatter:on

        runConformTest(sources, "[bar, baz, foo]");
    }

    @Test
    public void testCompileStatic10815() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  Pogo pogo = new Pogo(name:'Frank Grimes')\n" +
            "  def props = pogo.properties\n" +
            "  print props.keySet().sort()\n" +
            "}\n" +
            "test()\n",

            "Pogo.groovy",
            "class Pogo {\n" +
            "  String name\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[class, name]");
    }

    @Test
    public void testCompileStatic10819() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  def cmc = Class.getMetaClass()\n" +
            "  def smc = String.getMetaClass()\n" +
            "  assert cmc != smc\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "", isAtLeastGroovy(40) ? "" : "Assertion failed");
    }

    @Test
    public void testCompileStatic10820() {
        assumeTrue(isAtLeastGroovy(40));

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  Pogo.with {\n" +
            "    print name\n" +
            "    print getName()\n" +
            "    print it.name\n" +
            "    print it.getName()\n" +
            "    print delegate.name\n" +
            "    print delegate.getName()\n" +
            "    print number\n" +
            "    print getNumber()\n" +
            "    print it.number\n" +
            "    print it.getNumber()\n" +
            "    print delegate.number\n" +
            "    print delegate.getNumber()\n" +
            "  }\n" +
            "}\n" +
            "test()\n",

            "Pogo.groovy",
            "class Pogo {\n" +
            "  static getNumber() { 1 }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "PogoPogoPogoPogoPogoPogo111111");
    }

    @Test
    public void testCompileStatic10904() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import java.util.function.Function\n" +
            "import java.util.stream.Collectors\n" +
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  static class Profile {\n" +
            "    String foo, bar\n" +
            "  }\n" +
            "  Map<String, Profile> profiles = [new Profile()].stream()\n" +
            "    .collect(Collectors.toMap(Profile::getFoo, Function.identity()))\n" +
            "  static main(args) {\n" +
            "    print this.newInstance().getProfiles().size()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "1");
    }

    @Test
    public void testCompileStatic10933() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  List<String> strings = []\n" +
            "  void run() {\n" +
            "    Optional.of('works').ifPresent(strings::add)\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    def obj = this.newInstance()\n" +
            "    obj.run(); print obj.strings\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[works]");
    }

    @Test
    public void testCompileStatic11029() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Foo {\n" +
            "  Object myThing\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class Bar extends Foo {\n" +
            "  @Override\n" +
            "  Object getMyThing() {\n" +
            "    super.myThing\n" +
            "  }\n" +
            "  @Override\n" +
            "  void setMyThing(Object object) {\n" +
            "    super.myThing = object\n" +
            "  }\n" +
            "}\n" +
            "def bar = new Bar()\n" +
            "def value = 'works'\n" +
            "bar.myThing = value\n" +
            "print(bar.myThing);\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }
}
