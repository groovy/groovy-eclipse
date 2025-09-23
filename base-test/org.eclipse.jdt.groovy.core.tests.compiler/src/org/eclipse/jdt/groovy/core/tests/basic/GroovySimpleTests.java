/*
 * Copyright 2009-2025 the original author or authors.
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
import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isParrotParser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.jdt.groovy.internal.compiler.ast.EventListener;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyClassScope;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.junit.Test;
import org.osgi.framework.Version;

public final class GroovySimpleTests extends GroovyCompilerTestSuite {

    @Test
    public void testArrayBasic1() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "int[] array = new int[0]\n" +
            "assert array.length == 0\n" +
            "print array.class.name\n",
        };
        //@formatter:on

        runConformTest(sources, "[I");
    }

    @Test
    public void testArrayBasic2() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "int[] array = [123]\n" +
            "assert array.length == 1\n" +
            "assert array[0] == 123\n" +
            "print array.class.name\n",
        };
        //@formatter:on

        runConformTest(sources, "[I");
    }

    @Test
    public void testArrayBasic3() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "int[] array = new int[]{123}\n" +
            "assert array.length == 1\n" +
            "assert array[0] == 123\n" +
            "print array.class.name\n",
        };
        //@formatter:on

        runConformTest(sources, "[I");
    }

    @Test
    public void testArrayMulti1() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "int[][] array = new int[1][1]\n" +
            "assert array.length == 1\n" +
            "assert array[0][0] == 0\n" +
            "print array.class.name\n",
        };
        //@formatter:on

        runConformTest(sources, "[[I");
    }

    @Test
    public void testArrayMulti2() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "int[][] array = [ new int[1] ]\n" +
            "assert array.length == 1\n" +
            "assert array[0][0] == 0\n" +
            "print array.class.name\n",
        };
        //@formatter:on

        runConformTest(sources, "[[I");
    }

    @Test
    public void testArrayMulti3() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "int[][] array = new int[][]{ new int[]{1}, new int[]{2} }\n" +
            "assert array.length == 2\n" +
            "assert array[0][0] == 1\n" +
            "assert array[1][0] == 2\n" +
            "print array.class.name\n",
        };
        //@formatter:on

        runConformTest(sources, "[[I");
    }

    @Test
    public void testArrayMulti4() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "int[][] array = [ [1], [2] ]\n" +
            "assert array.length == 2\n" +
            "assert array[0][0] == 1\n" +
            "assert array[1][0] == 2\n" +
            "print array.class.name\n",
        };
        //@formatter:on

        runConformTest(sources, "[[I");
    }

    @Test
    public void testArrayMulti5() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "int[][] array = [ 1, 2 ]\n" + // runtime converts to arrays of length 1
            "assert array.length == 2\n" +
            "assert array[0][0] == 1\n" +
            "assert array[1][0] == 2\n" +
            "print array.class.name\n",
        };
        //@formatter:on

        runConformTest(sources, "[[I");
    }

    @Test
    public void testArrayMulti6() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "String[][] array = [ '1', '23' ]\n" + // runtime converts to arrays of characters
            "assert array.length == 2\n" +
            "assert array[0].length == 1\n" +
            "assert array[0][0] == '1'\n" +
            "assert array[1].length == 2\n" +
            "assert array[1][0] == '2'\n" +
            "print array.class.name\n",
        };
        //@formatter:on

        runConformTest(sources, "[[Ljava.lang.String;");
    }

    @Test
    public void testArrayMulti7() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "int[][] array = new int[1][]\n" +
            "assert array.length == 1\n" +
            "assert array[0] == null\n" +
            "print array.class.name\n",
        };
        //@formatter:on

        runConformTest(sources, "[[I");
    }

    @Test
    public void testClosuresBasic() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "def iterate(n, closure) {\n" +
            "  1.upto(n) {\n" +
            "    closure(it)\n" +
            "  }\n" +
            "}\n" +
            "iterate(3) {\n" +
            "  print it\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "123");
    }

    @Test
    public void testClosureScope1() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class C {\n" +
            "  public String field = 'f'\n" +
            "  \n" +
            "  void test() {\n" +
            "    def c = { thisParameter ->\n" +
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
    public void testClosureScope2() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class C {\n" +
            "  public String field = 'f'\n" +
            "  \n" +
            "  void test() {\n" +
            "    def c1 = { thisParameter1 ->\n" +
            "      def c2 = { thisParameter2 ->\n" +
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

    @Test // GROOVY-4235
    public void testClosureScope3() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class C {\n" +
            "  static prop = 'p'\n" +
            "  static test() {\n" +
            "    return { ->\n" +
            "      this.prop\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "print C.test().call()\n",
        };
        //@formatter:on

        runConformTest(sources, "p");
    }

    @Test
    public void testClosureScope4() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "abstract class A {\n" +
            "  static prop = 'p'\n" +
            "}\n" +
            "class C extends A {\n" +
            "  static test() {\n" +
            "    return { ->\n" +
            "      super.prop\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "print C.test().call()\n",
        };
        //@formatter:on

        runConformTest(sources, "p");
    }

    @Test // GROOVY-2849
    public void testClosureScope5() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  static main(args) {\n" +
            "    newInstance().test()\n" +
            "  }\n" +
            "  void test() {\n" +
            "    print c1()\n" +
            "    print ' '\n" +
            "    print p\n" +
            "  }\n" +
            "  def p = 1\n" +
            "  def c1 = {\n" +
            "    def p = 2\n" +
            "    def c2 = {\n" +
            "      this.p += 10\n" +
            "      p = 3\n" +
            "      assert p == 3\n" +
            "      return this.p\n" +
            "    }\n" +
            "    return c2()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "11 11");
    }

    @Test // GROOVY-7701
    public void testClosureScope6() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Foo {\n" +
            "  List type\n" +
            "}\n" +
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

        runConformTest(sources, "");
    }

    @Test // GROOVY-7973
    public void testClosureScope7() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Test {\n" +
            "  def op1() { this }\n" +
            "  def op2() { ''.with{ this } }\n" +
            "  def op3() { new Object() { def inner() { this } } }\n" +
            "  def op4() { new Object() { def inner() { ''.with{ this } } } }\n" +
            "  def op5() { new Object() { def inner() { Test.this } } }\n" +
            "  def op6() { new Object() { def inner() { ''.with{ Test.this } } } }\n" +
            "  class Inner {\n" +
            "    def inner1() { this }\n" +
            "    def inner2() { ''.with { this } }\n" +
            "    def inner3() { Test.this }\n" +
            "    def inner4() { ''.with { Test.this } }\n" +
            "  }\n" +
            "}\n" +
            "def outer = new Test()\n" +
            "assert outer.op1().class.name == 'Test'\n" +
            "assert outer.op2().class.name == 'Test'\n" +
            "assert outer.op3().inner().class.name == 'Test$1'\n" +
            "assert outer.op4().inner().class.name == 'Test$2'\n" +
            "assert outer.op5().inner().class.name == 'Test'\n" +
            "assert outer.op6().inner().class.name == 'Test'\n" +
            "def inner = new Test.Inner(outer)\n" +
            "assert inner.inner1().class.name == 'Test$Inner'\n" +
            "assert inner.inner2().class.name == 'Test$Inner'\n" +
            "assert inner.inner3().class.name == 'Test'\n" +
            "assert inner.inner4().class.name == 'Test'\n",
        };
        //@formatter:on

        runConformTest(sources, "");
    }

    @Test // GROOVY-8881
    public void testClosureScope8() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Outer implements Runnable {\n" +
            "  class Inner {\n" +
            "    String foo() {\n" +
            "      Closure bar = { ->\n" +
            "        Closure baz = { ->\n" +
            "          return Outer.this\n" +
            "        }\n" +
            "        return baz()\n" +
            "      }\n" +
            "      bar()\n" +
            "    }\n" +
            "    String toString() {\n" +
            "      return 'Inner'\n" +
            "    }\n" +
            "  }\n" +
            "  String toString() {\n" +
            "    return 'Outer'\n" +
            "  }\n" +
            "  void run() {\n" +
            "    print new Inner().foo()\n" +
            "  }\n" +
            "}\n" +
            "new Outer().run()\n",
        };
        //@formatter:on

        runConformTest(sources, "Outer");
    }

    @Test // GROOVY-10188
    public void testClosureScope9() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "List<Integer> numbers = [1,2,3]\n" +
            "numbers.each { Number -> print Number.xxx }\n",
        };
        //@formatter:on

        runConformTest(sources, "", "groovy.lang.MissingPropertyException: No such property: xxx for class: java.lang.Integer");
    }

    @Test
    public void testClosureSyntax() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class A {\n" +
            "  A(Closure<?> closure) {\n" +
            "    closure()\n" +
            "  }\n" +
            "}\n" +
            "abc = {println 'abc'\n" +
            "}\n" +
            "new A({\n" +
            "  abc()\n" +
            "})\n" +
            "new A() {\n" +
            "  abc()\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Script.groovy (at line 12)\n" +
            "\tabc()\n" +
            "\t^" + (!isParrotParser() ? "" : "^^^^") + "\n" +
            "Groovy:" + (!isParrotParser()
                ? "unexpected token: abc\n"
                : "You defined a method[abc] without a body. Try adding a method body, or declare it abstract\n") +
            "----------\n");
    }

    @Test // GROOVY-3831
    public void testClosureConstructorArgument() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "class Foo {\n" +
            "  URI[] uris\n" +
            "  Foo(URI[] uris) {\n" +
            "    this.uris = uris\n" +
            "  }\n" +
            "  Foo(List<String> uris) {\n" +
            "    this(uris.collect { URI.create(it) } as URI[])\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test // GROOVY-10717
    public void testClosureMapAsType() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "abstract class A {\n" +
            "  String b,c\n" +
            "}\n" +
            "def a = [getB:{->'b'}, getX:{->'x'}] as A\n" +
            "print a.b\n" + // overrides accessor
            "print a.c\n" + // generated accessor
            "try {\n" +
            "  print a.x\n" +
            "} catch (MissingPropertyException mpe) {\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "bnull");
    }

    @Test
    public void testLambdaBasic() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import java.util.function.*\n" +
            "\n" +
            "Supplier<String> s = () -> 'hello'\n" +
            "print s.get()\n" +
            "\n" +
            "Consumer<String> c = (x -> print x)\n" + // GROOVY-10236
            "c.accept(' ')\n" +
            "\n" +
            "Function<Integer, String> f = (Integer i) -> { 'world' }\n" +
            "print f(42)\n",
        };
        //@formatter:on

        runConformTest(sources, "hello world");
    }

    @Test // GROOVY-9333
    public void testLambdaScope1() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import java.util.function.*\n" +
            "\n" +
            "class C {\n" +
            "  public String field = 'f'\n" +
            "  \n" +
            "  void test() {\n" +
            "    Consumer<C> c = thisParameter -> {\n" +
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

    @Test // GROOVY-9333
    public void testLambdaScope2() {
        assumeTrue(isParrotParser());

        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import java.util.function.*\n" +
            "\n" +
            "class C {\n" +
            "  public String field = 'f'\n" +
            "  \n" +
            "  void test() {\n" +
            "    Consumer<C> c1 = thisParameter1 -> {\n" +
            "      Consumer<C> c2 = thisParameter2 -> {\n" +
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
    public void testMultiCatch() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "class A {\n" +
            "  static main(args) {\n" +
            "    try {\n" +
            "      foo();\n" +
            "    } catch (IOException | IllegalStateException ex) {\n" +
            "    }\n" +
            "  }\n" +
            "  public static void foo() throws IOException { print 'foo' }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "foo");
    }

    @Test // GROOVY-3421
    public void testSpreadMap() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "int i = 1\n" +
            "print([a:i, *:[b:++i]])\n",
        };
        //@formatter:on

        runConformTest(sources, "[a:1, b:2]");
    }

    @Test
    public void testLogicalImplication1() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "print(true.implies(true))\n" +
            "print(true.implies(false))\n",
        };
        //@formatter:on

        runConformTest(sources, "truefalse");
    }

    @Test // GROOVY-11238
    public void testLogicalImplication2() {
        assumeTrue(isParrotParser() && isAtLeastGroovy(50));

        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "print(true ==> true)\n" +
            "print(true ==> false)\n",
        };
        //@formatter:on

        runConformTest(sources, "truefalse");
    }

    @Test // GROOVY-5245
    public void testBooleanCategoryMethod1() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Cat {\n" +
            "  static boolean isFoo(self) { true }\n" +
            "}\n" +
            "use (Cat) {\n" +
            "  print foo\n" +
            "}\n",
        };
        //@formatter:on

        if (isAtLeastGroovy(40)) {
            runConformTest(sources, "true");
        } else {
            runConformTest(sources, "", "groovy.lang.MissingPropertyException: No such property: foo for class: Script");
        }
    }

    @Test // GROOVY-10133
    public void testBooleanCategoryMethod2() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Cat {\n" +
            "  static boolean isFoo(self) { true }\n" +
            "  static boolean getFoo(self) { false }\n" +
            "}\n" +
            "use (Cat) {\n" +
            "  print foo\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, isAtLeastGroovy(40) ? "true" : "false");
    }

    @Test // GROOVY-5609 (STC of extensions)
    public void testVariadicCategoryMethods() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Cat {\n" +
            "  static <T> void foo(List<T> self, T[] tees) {\n" +
            "    print(self.size() + tees.length)\n" +
            "  }\n" +
            "  static <T> void foo(T[] self, T[] tees) {\n" +
            "    print(self.length + tees.length)\n" +
            "  }\n" +
            "}\n" +
            "use (Cat) {\n" +
            "  Integer[] array = [1,2,3]\n" +
            "  array.foo(array)\n" +
            "  array.foo(4,5,6)\n" +
            "  array.foo(4)\n" +
            "  array.foo()\n" +
            "  List<Integer> list = [1,2,3]\n" +
            "  list.foo(array)\n" +
            "  list.foo(4,5,6)\n" +
            "  list.foo(4)\n" +
            "  list.foo()\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "66436643");
    }

    @Test // GROOVY-10743
    public void testInterfaceCategoryMethods() {
        assumeTrue(isAtLeastGroovy(40) && isAtLeastJava(JDK9));

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "use (java.util.stream.Stream) {\n" +
            "  assert 16.iterate({it < 500}, {it * 2}).toList() == [16, 32, 64, 128, 256]\n" +
            "  assert [1, 1].iterate{f -> [f[1], f.sum()]}.limit(8).toList()*.head() == [1, 1, 2, 3, 5, 8, 13, 21]\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testGreclipse719() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "int anInt = 10\n" +
            "Method[] methodArray = anInt.class.methods\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Script.groovy (at line 2)\n" +
            "\tMethod[] methodArray = anInt.class.methods\n" +
            "\t^^^^^^\n" +
            "Groovy:unable to resolve class Method\n" +
            "----------\n");
    }

    @Test
    public void testGreclipse719_2() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "int anInt = 10\n" +
            "Method[][] methodMethodArray = anInt.class.methods\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Script.groovy (at line 2)\n" +
            "\tMethod[][] methodMethodArray = anInt.class.methods\n" +
            "\t^^^^^^\n" +
            "Groovy:unable to resolve class Method\n" +
            "----------\n");
    }

    @Test
    public void testStaticProperty1() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class A {\n" +
            "  static protected int x\n" +
            "  static void reset() { this.@x = 42 }\n" +
            "}\n" +
            "A.reset()\n" +
            "print A.x\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test // GROOVY-6183
    public void testStaticProperty2() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class A {\n" +
            "  static boolean setterCalled = false\n" +
            "  \n" +
            "  static protected int x\n" +
            "  \n" +
            "  public static void setX(int a) {\n" +
            "    setterCalled = true\n" +
            "    x = a\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "class B extends A {\n" +
            "  static void directAccess() {\n" +
            "    this.@x = 2\n" +
            "  }\n" +
            "}\n" +
            "B.directAccess()\n" +
            "assert B.isSetterCalled() == false\n" +
            "assert B.x == 2\n",
        };
        //@formatter:on

        runConformTest(sources, "");
    }

    @Test
    public void testStaticProperty2a() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  static main(args) {\n" +
            "    this.@x\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "", isAtLeastGroovy(50)
            ? "groovy.lang.MissingFieldException: No such field: x for class: Main"
            : "java.lang.IncompatibleClassChangeError: Class java.lang.Class does not implement the requested interface groovy.lang.GroovyObject");
    }

    @Test // GROOVY-8385
    public void testStaticProperty3() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class A {\n" +
            "  static protected p\n" +
            "  static getP() { -1 }\n" +
            "  static setP(value) { p = 2 }\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def m() { this.@p = 1 }\n" +
            "}\n" +
            "def x = new B()\n" +
            "assert A.@p == null\n" +
            "x.m()\n" +
            "assert A.@p == 1\n",
        };
        //@formatter:on

        runConformTest(sources, "");
    }

    @Test
    public void testStaticProperty4() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "abstract class A {\n" +
            "  static getB() { 'bee' }\n" +
            "}\n" +
            "class C extends A {\n" +
            "  static m() {\n" +
            "    print b.charAt(0)\n" +
            "  }\n" +
            "}\n" +
            "new C().m()\n",
        };
        //@formatter:on

        runConformTest(sources, "b");
    }

    @Test
    public void testStaticProperty4a() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C extends A {\n" +
            "  static main(args) {\n" +
            "    print b.charAt(0)\n" +
            "  }\n" +
            "}\n",

            "A.groovy",
            "abstract class A {\n" +
            "  static getB() { 'bee' }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "b");
    }

    @Test // GRECLIPSE-364
    public void testStaticProperty5() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "public class Foo { static String fubar }\n",

            "Bar.java",
            "public class Bar {\n" +
            "  String fubar = Foo.getFubar();\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test // GRECLIPSE-364
    public void testStaticProperty6() {
        //@formatter:off
        String[] sources = {
            "Bar.java",
            "public class Bar {\n" +
            "  String fubar = Foo.getFubar();\n" +
            "}\n",

            "Foo.groovy",
            "public class Foo { static String fubar }\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testStaticProperty7() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class C {\n" +
            "  static String f = '123'\n" +
            "  static String g() { 'abc' }\n" +
            "  C() {\n" +
            "    this(g() + getF())\n" +
            "  }\n" +
            "  C(String s) {\n" +
            "    print s\n" +
            "  }\n" +
            "}\n" +
            "new C()\n",
        };
        //@formatter:on

        runConformTest(sources, "abc123");
    }

    @Test // GROOVY-8327
    public void testStaticProperty8() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class C {\n" +
            "  static String f = '123'\n" +
            "  static String g() { 'abc' }\n" +
            "  C() {\n" +
            "    this({ -> g() + getF()})\n" +
            "  }\n" +
            "  C(x) {\n" +
            "    print x()\n" +
            "  }\n" +
            "}\n" +
            "new C()\n",
        };
        //@formatter:on

        runConformTest(sources, "abc123");
    }

    @Test // GROOVY-9591: "b" is not static...this variation of "8" should also work
    public void testStaticProperty8a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@groovy.transform.ToString\n" +
            "class A {\n" +
            "  A(a) {}\n" +
            "  def b\n" +
            "}\n" +
            "class C {\n" +
            "  C() {\n" +
            "    this(new A(null).tap { b = 42 })\n" + // A has no default constructor, so properties are initialized in tap
            "  }\n" +
            "  C(x) {\n" +
            "    print x\n" +
            "  }\n" +
            "}\n" +
            "new C()\n",
        };
        //@formatter:on

        runConformTest(sources, "A(42)");
    }

    @Test // GROOVY-9591: "b" is not static...this variation of "8" should also work
    public void testStaticProperty8b() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@groovy.transform.ToString\n" +
            "class A {\n" +
            "  A(a) {}\n" +
            "  def b\n" +
            "}\n" +
            "class C {\n" +
            "  C() {\n" +
            "    this(new A(null).with { b = 42; return it })\n" + // A has no default constructor, so properties are initialized in tap
            "  }\n" +
            "  C(x) {\n" +
            "    print x\n" +
            "  }\n" +
            "}\n" +
            "new C()\n",
        };
        //@formatter:on

        runConformTest(sources, "A(42)");
    }

    @Test // GROOVY-9587
    public void testStaticProperty9() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  static foo\n" +
            "  def getFoo() {\n" +
            "    if (foo == null) {\n" +
            "      getFoo(true)\n" +
            "    } else {\n" +
            "      foo\n" +
            "    }\n" +
            "  }\n" +
            "  def getFoo(flag) {\n" +
            "    return 'foo'\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    println newInstance().getFoo()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "foo");
    }

    @Test // GROOVY-9825
    public void testStaticProperty10() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "interface A {\n" +
            "  int X = 1\n" +
            "}\n" +
            "interface B extends A {\n" +
            "  int Y = 2\n" +
            "}\n" +
            "class Main {\n" +
            "  static class C implements B {\n" +
            "    int getSum() { X + Y }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    print new C().sum\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "3");
    }

    @Test // GROOVY-5364
    public void testStaticProperty11() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "static getStaticProperty() { '!' }\n" +
            "static staticMethod() {\n" +
            "  print getStaticProperty()\n" +
            "  print staticProperty\n" +
            "}\n" +
            "staticMethod()\n",
        };
        //@formatter:on

        runConformTest(sources, "!!");
    }

    @Test
    public void testStaticProperty12() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "print Type.pi\n" +
            "print Type.xx\n",

            "Type.groovy",
            "interface Type {\n" +
            "  BigDecimal pi = 3.14\n" +
            "  BigInteger xx = 1592\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "3.141592");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1289
    public void testStaticProperty13() {
        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.print(Type.pi);\n" +
            "    System.out.print(Type.xx);\n" +
            "  }\n" +
            "}\n",

            "Type.groovy",
            "interface Type {\n" +
            "  BigDecimal pi = 3.14\n" +
            "  BigInteger xx = 1592\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "3.141592");
    }

    @Test
    public void testStaticProperty14() {
        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.print(Pogo.TOO_MANY_DIGITS);\n" +
            "  }\n" +
            "}\n",

            "Pogo.groovy",
            "final class Pogo {\n" +
            "  public static final double TOO_MANY_DIGITS = -3.14159_26535_89793_23846_26433_83279_50288_41971_69399_37510\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Pogo.groovy (at line 2)\n" +
            "\tpublic static final double TOO_MANY_DIGITS = -3.14159_26535_89793_23846_26433_83279_50288_41971_69399_37510\n" +
            "\t                                             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "The literal -3.14159265358979323846264338327950288419716939937510 of type double is out of range \n" +
            "----------\n");
    }

    @Test // GROOVY-10159
    public void testStaticProperty15() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "public class Main {\n" +
            "  static final String foo = 1 + 1\n" +
            "  static main(args) {\n" +
            "    print(foo);\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "2");
    }

    @Test
    public void testClash_GRE1076() {
        //@formatter:off
        String[] sources = {
            "com/foo/Bar.java",
            "package com.foo;\n" +
            "public class Bar {\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.println(\"def\");\n" +
            "  }\n" +
            "}\n",

            "com/foo/Bar/script.groovy",
            "package com.foo.Bar\n" +
            "print 'abc'\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in com\\foo\\Bar\\script.groovy (at line 1)\n" +
            "\tpackage com.foo.Bar\n" +
            "\t        ^^^^^^^^^^^\n" +
            "The package com.foo.Bar collides with a type\n" +
            "----------\n");
    }

    @Test
    public void testClash_GRE1076_2() {
        //@formatter:off
        String[] sources = {
            "com/foo/Bar/script.groovy",
            "package com.foo.Bar\n" +
            "print 'abc'\n",

            "com/foo/Bar.java",
            "package com.foo;\n" +
            "class Bar {}\n",
        };
        //@formatter:on

        boolean conflictIsError = (JavaCore.getPlugin().getBundle().getVersion().compareTo(Version.parseVersion("3.19")) >= 0);

        runNegativeTest(sources,
            "----------\n" +
            "1. " + (conflictIsError ? "ERROR" : "WARNING") + " in com\\foo\\Bar.java (at line 2)\n" +
            "\tclass Bar {}\n" +
            "\t      ^^^\n" +
            "The type Bar collides with a package\n" +
            "----------\n");
    }

    @Test
    public void testCyclicReference1() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C extends C {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in C.groovy (at line 1)\n" +
            "\tclass C extends C {\n" +
            "\t                ^\n" +
            "Groovy:Cycle detected: the type C cannot extend/implement itself or one of its own member types\n" +
            "----------\n");
    }

    @Test
    public void testCyclicReference2() {
        //@formatter:off
        String[] sources = {
            "I.groovy",
            "interface I extends I {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in I.groovy (at line 1)\n" +
            "\tinterface I extends I {\n" +
            "\t                    ^\n" +
            "Groovy:Cycle detected: the type I cannot extend/implement itself or one of its own member types\n" +
            "----------\n");
    }

    @Test
    public void testCyclicReference3() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C extends C.D {\n" +
            "  class D {\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in C.groovy (at line 1)\n" +
            "\tclass C extends C.D {\n" +
            "\t                ^^^\n" +
            "Groovy:Cycle detected: the type C cannot extend/implement itself or one of its own member types\n" +
            "----------\n");
    }

    @Test // GROOVY-10124
    public void testCyclicReference4() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C extends D {\n" +
            "  class D {\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in C.groovy (at line 1)\n" +
            "\tclass C extends D {\n" +
            "\t                ^\n" +
            "Groovy:Cycle detected: the type C cannot extend/implement itself or one of its own member types\n" +
            "----------\n");
    }

    @Test // GROOVY-10113
    public void testCyclicReference5() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C<T extends T> {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in C.groovy (at line 1)\n" +
            "\tclass C<T extends T> {\n" +
            "\t        ^\n" +
            "Groovy:Cycle detected: the type T cannot extend/implement itself or one of its own member types\n" +
            "----------\n");
    }

    @Test
    public void testCyclicReference6() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C<T extends U, U extends T> {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in C.groovy (at line 1)\n" +
            "\tclass C<T extends U, U extends T> {\n" +
            "\t                     ^\n" +
            "Groovy:Cycle detected: the type T cannot extend/implement itself or one of its own member types\n" +
            "----------\n");
    }

    @Test
    public void testCyclicReference7() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C extends C<String> {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in C.groovy (at line 1)\n" +
            "\tclass C extends C<String> {\n" +
            "\t                ^\n" +
            "Groovy:Cycle detected: the type C cannot extend/implement itself or one of its own member types\n" +
            "----------\n");
    }

    @Test
    public void testCyclicReference8() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C extends D {\n" +
            "}\n",

            "D.groovy",
            "class D extends C {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in C.groovy (at line 1)\n" +
            "\tclass C extends D {\n" +
            "\t      ^\n" +
            "The hierarchy of the type C is inconsistent\n" +
            "----------\n" +
            "----------\n" +
            "1. ERROR in D.groovy (at line 1)\n" +
            "\tclass D extends C {\n" +
            "\t                ^\n" +
            "Groovy:Cycle detected: a cycle exists in the type hierarchy between D and C\n" +
            "----------\n");
    }

    @Test
    public void testCyclicReference9() {
        //@formatter:off
        String[] sources = {
            "I.groovy",
            "interface I extends J {\n" +
            "}\n",

            "J.groovy",
            "interface J extends I {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in I.groovy (at line 1)\n" +
            "\tinterface I extends J {\n" +
            "\t          ^\n" +
            "The hierarchy of the type I is inconsistent\n" +
            "----------\n" +
            "----------\n" +
            "1. ERROR in J.groovy (at line 1)\n" +
            "\tinterface J extends I {\n" +
            "\t                    ^\n" +
            "Groovy:Cycle detected: a cycle exists in the type hierarchy between J and I\n" +
            "----------\n");
    }

    @Test
    public void testCyclicReference10() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C extends D {\n" +
            "  interface I {\n" +
            "  }\n" +
            "}\n",

            "D.groovy",
            "class D implements C.I {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in C.groovy (at line 1)\n" +
            "\tclass C extends D {\n" +
            "\t      ^\n" +
            "The hierarchy of the type C is inconsistent\n" +
            "----------\n" +
            "----------\n" +
            "1. ERROR in D.groovy (at line 1)\n" +
            "\tclass D implements C.I {\n" +
            "\t                   ^^^\n" +
            "Groovy:Cycle detected: a cycle exists in the type hierarchy between D and C\n" +
            "----------\n");
    }

    @Test // typo that caused overflow
    public void testCyclicReference11() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "interface A extends B {\n" +
            "}\n",

            "B.groovy",
            "class B extends A {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in A.groovy (at line 1)\n" +
            "\tinterface A extends B {\n" +
            "\t          ^\n" +
            "Groovy:You are not allowed to implement the class 'B', use extends instead.\n" +
            "----------\n" +
            "----------\n" +
            "1. ERROR in B.groovy (at line 1)\n" +
            "\tclass B extends A {\n" +
            "\t                ^\n" +
            "Groovy:Cycle detected: a cycle exists in the type hierarchy between B and A\n" +
            "----------\n");
    }

    @Test
    public void testNonCyclicReference1() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "@SuppressWarnings('rawtypes')\n" +
            "class C<T extends C> {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testNonCyclicReference2() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "@SuppressWarnings('rawtypes')\n" +
            "class C<T extends C.D> {\n" +
            "  class D {\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test // GROOVY-10125
    public void testNonCyclicReference3() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C<T, U extends T> {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testUnreachable_1047() {
        //@formatter:off
        String[] sources = {
            "MyException.java",
            "class MyException extends Exception {\n" +
            "  private static final long serialVersionUID = 1L;\n" +
            "}\n",

            "CanThrowException.groovy",
            "public interface CanThrowException {\n" +
            "  void thisCanThrowException() throws MyException\n" +
            "}\n",

            "ShouldCatchException.java",
            "class ShouldCatchException {\n" +
            "  private CanThrowException thing;\n" +
            "  public void doIt() {\n" +
            "    try {\n" +
            "      thing.thisCanThrowException();\n" +
            "    } catch (MyException e) {\n" +
            "      System.out.println(\"Did we catch it?\");\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testUnreachable_1047_2() {
        //@formatter:off
        String[] sources = {
            "MyException.java",
            "class MyException extends Exception {\n" +
            "  private static final long serialVersionUID = 1L;\n" +
            "}\n",

            "CanThrowException.groovy",
            "public class CanThrowException {\n" +
            "  public CanThrowException() throws MyException {\n" +
            "    throw new MyException();\n" +
            "  }\n" +
            "}\n",

            "ShouldCatchException.java",
            "class ShouldCatchException {\n" +
            "  public void doIt() {\n" +
            "    try {\n" +
            "      new CanThrowException();\n" +
            "    } catch (MyException e) {\n" +
            "      System.out.println(\"Did we catch it?\");\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testDuplicateClasses1() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "class Foo {}\n" +
            "class Foo {}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in A.groovy (at line 2)\n" +
            "\tclass Foo {}\n" +
            "\t^^^^^^^^^^^^\n" +
            "Groovy:Invalid duplicate class definition of class Foo : The source A.groovy contains at least two definitions of the class Foo.\n" +
            "----------\n" +
            "2. ERROR in A.groovy (at line 2)\n" +
            "\tclass Foo {}\n" +
            "\t      ^^^\n" +
            "The type Foo is already defined\n" +
            "----------\n");
    }

    @Test
    public void testDuplicateClasses2() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p\n" +
            "public class X {}\n",

            "p/X.java",
            "package p;\n" +
            "public class X {}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.java (at line 2)\n" +
            "\tpublic class X {}\n" +
            "\t             ^\n" +
            "The type X is already defined\n" +
            "----------\n");
    }

    /**
     * This test is looking at what happens when a valid type is compiled ahead of two problem types (problematic
     * since they both declare the same class).  Although the first file gets through resolution OK, re-resolution
     * is attempted because the SourceUnit isn't tagged as having succeeded that phase - the exception thrown
     * for the real problem jumps over the tagging process.
     */
    @Test
    public void testDuplicateClasses_GRE796() {
        //@formatter:off
        String[] sources = {
            "spring/resources.groovy",
            "foo = {}\n",

            "A.groovy",
            "class Foo {}\n",

            "Foo.groovy",
            "class Foo {}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 1)\n" +
            "\tclass Foo {}\n" +
            "\t^^^^^^^^^^^^\n" +
            "Groovy:Invalid duplicate class definition of class Foo : The sources Foo.groovy and A.groovy each contain a class with the name Foo.\n" +
            "----------\n" +
            "2. ERROR in Foo.groovy (at line 1)\n" +
            "\tclass Foo {}\n" +
            "\t      ^^^\n" +
            "The type Foo is already defined\n" +
            "----------\n");
    }

    @Test
    public void testDuplicateClasses_GRE796_2() {
        //@formatter:off
        String[] sources = {
            "spring/resources.groovy",
            "foo = {}\n",

            "a/Foo.groovy",
            "package a\n" +
            "class Foo {}\n" +
            "class Foo {}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in a\\Foo.groovy (at line 3)\n" +
            "\tclass Foo {}\n" +
            "\t^^^^^^^^^^^^\n" +
            "Groovy:Invalid duplicate class definition of class a.Foo :" +
            " The source a" + File.separator + "Foo.groovy contains at least two definitions of the class a.Foo.\n" +
            "----------\n" +
            "2. ERROR in a\\Foo.groovy (at line 3)\n" +
            "\tclass Foo {}\n" +
            "\t      ^^^\n" +
            "The type Foo is already defined\n" +
            "----------\n");
    }

    @Test
    public void testInvalidAssignment_GRE801() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "httpClientControl.demand.generalConnection(1..1) = {->\n" +
            "  currHttp\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in A.groovy (at line 1)\n" +
            "\thttpClientControl.demand.generalConnection(1..1) = {->\n" +
            "\t" + (isParrotParser() ? "" : "                                                 ") + "^\n" +
            "Groovy:" + (isParrotParser()
                ? "The LHS of an assignment should be a variable or a field accessing expression\n"
                : " \"httpClientControl.demand.generalConnection((1..1))\" is a method call expression, but it should be a variable expression\n") +
            "----------\n");
    }

    @Test
    public void testPrimitiveLikeTypeNames_GRE891() {
        //@formatter:off
        String[] sources = {
            "Foo.java",
            "public class Foo {\n" +
            "  public static void main(String[] args) {\n" +
            "    Z[][] zs = new Z().zzz();\n" +
            "    System.out.println(\"works\");\n" +
            "  }\n" +
            "}\n",

            "Z.groovy",
            "class Z {\n" +
            "  Z[][] zzz() { null }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
        // return type is a single char type (not in package and not primitive)
        assertEquals("[[LZ;", getReturnTypeOfMethod("Z.groovy", "zzz"));
    }

    @Test
    public void testPrimitiveLikeTypeNames_GRE891_2() {
        //@formatter:off
        String[] sources = {
            "Foo.java",
            "public class Foo {\n" +
            "  public static void main(String[] args) {\n" +
            "    int[][] zs = new Z().zzz();\n" +
            "    System.out.println(\"works\");\n" +
            "  }\n" +
            "}\n",

            "Z.groovy",
            "class Z {\n" +
            "  int[][] zzz() { null }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
        // return type is a primitive
        assertEquals("[[I", getReturnTypeOfMethod("Z.groovy", "zzz"));
    }

    @Test
    public void testPrimitiveLikeTypeNames_GRE891_3() {
        //@formatter:off
        String[] sources = {
            "Foo.java",
            "public class Foo {\n" +
            "  public static void main(String[] args) {\n" +
            "    java.lang.String[][] zs = new Z().zzz();\n" +
            "    System.out.println(\"works\");\n" +
            "  }\n" +
            "}\n",

            "Z.groovy",
            "class Z {\n" +
            "  java.lang.String[][] zzz() { null }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
        // return type is a qualified java built in type
        assertEquals("[[Ljava.lang.String;", getReturnTypeOfMethod("Z.groovy", "zzz"));
    }

    @Test
    public void testPrimitiveLikeTypeNames_GRE891_4() {
        //@formatter:off
        String[] sources = {
            "pkg/Foo.java",
            "package pkg;\n" +
            "public class Foo {\n" +
            "  public static void main(String[] args) {\n" +
            "    pkg.H[][] zs = new Z().zzz();\n" +
            "    System.out.println(\"works\");\n" +
            "  }\n" +
            "}\n",

            "Y.groovy",
            "package pkg\n" +
            "class H {}\n",

            "Z.groovy",
            "package pkg\n" +
            "class Z {\n" +
            "  H[][] zzz() { null }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
        // return type is a single char groovy type from a package
        assertEquals("[[Lpkg.H;", getReturnTypeOfMethod("Z.groovy", "zzz"));
    }

    @Test
    public void testPrimitiveLikeTypeNames_GRE891_5() {
        //@formatter:off
        String[] sources = {
            "pkg/Foo.java",
            "package pkg;\n" +
            "public class Foo {\n" +
            "  public static void main(String[] args) {\n" +
            "    H[][] zs = new Z().zzz();\n" +
            "    System.out.println(\"works\");\n" +
            "  }\n" +
            "}\n",

            "Y.java",
            "package pkg;\n" +
            "class H {}\n",

            "Z.groovy",
            "package pkg;\n" +
            "class Z {\n" +
            "  H[][] zzz() { null }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
        // return type is a single char java type from a package
        assertEquals("[[Lpkg.H;", getReturnTypeOfMethod("Z.groovy", "zzz"));
    }

    @Test
    public void testStaticOuter_GRE944() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "static class A {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in A.groovy (at line 1)\n" +
            "\tstatic class A {\n" +
            "\t             ^\n" +
            "Groovy:The class \'A\' has " + (isAtLeastGroovy(50) ? "invalid" : "an incorrect") + " modifier static.\n" +
            "----------\n");
    }

    @Test
    public void testStaticOuter_GRE944_2() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "static class A {\n" +
            "  static class B {\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in A.groovy (at line 1)\n" +
            "\tstatic class A {\n" +
            "\t             ^\n" +
            "Groovy:The class \'A\' has " + (isAtLeastGroovy(50) ? "invalid" : "an incorrect") + " modifier static.\n" +
            "----------\n");
    }

    @Test
    public void testIncompleteCharacterEscape_GRE986() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "hello \\u\n" +
            "class Foo {}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in A.groovy (at line 1)\n" +
            "\thello \\u\n" +
            "\t      ^\n" +
            "Groovy:Did not find four digit hex character code. line: 1 col:7\n" + // Java Editor: "Invalid unicode" with 6 characters underlined
            "----------\n");
    }

    @Test
    public void testInvisibleCharacter1() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "class F\u000Coo {}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 1)\n" +
            "\tclass F\foo {}\n" +
            "\t       ^\n" +
            "Groovy:Unexpected character 0x0C (FORM FEED (FF)) at column 8\n" +
            "----------\n" +
            "2. ERROR in Foo.groovy (at line 1)\n" +
            "\tclass F\foo {}\n" +
            "\t        ^\n" +
            "Groovy:unexpected token: oo\n" +
            "----------\n");
    }

    @Test
    public void testInvisibleCharacter2() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "class F\u200Boo {}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 1)\n" +
            "\tclass F\u200Boo {}\n" +
            "\t       ^\n" +
            "Groovy:Unexpected character 0x200B (ZERO WIDTH SPACE) at column 8\n" +
            "----------\n");
    }

    @Test
    public void testInvisibleCharacter3() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "class F\u2063oo {}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 1)\n" +
            "\tclass F\u2063oo {}\n" +
            "\t       ^\n" +
            "Groovy:Unexpected character 0x2063 (INVISIBLE SEPARATOR) at column 8\n" +
            "----------\n");
    }

    @Test
    public void testInvisibleCharacter4() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "class Foo {\n" +
            "  public static final String SEPARATOR = '\\u2063'\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    /*
     * The groovy object method augmentation (in GroovyClassScope) should only
     * apply to types directly implementing GroovyObject, rather than adding all
     * the way down the hierarchy.  This mirrors what happens in the compiler.
     */

    /**
     * First a class extending another.  The superclass gets augmented but not the subclass.
     */
    @Test
    public void testClassHierarchiesAndGroovyObjectMethods() {
        try {
            GroovyClassScope.debugListener = new EventListener("augment");
            //@formatter:off
            String[] sources = {
                "Foo.groovy",
                "class Foo {\n" +
                "  static main(args) { print 'abc'} \n" +
                "}\n" +
                "class Two extends Foo {\n" +
                "  public void m() {\n" +
                "    Object o = getMetaClass();\n" +
                "  }\n" +
                "}\n",
            };
            //@formatter:on

            runConformTest(sources, "abc");
            assertEventCount(1, GroovyClassScope.debugListener);
            assertEvent("augment: type Foo having GroovyObject methods added", GroovyClassScope.debugListener);
        } finally {
            GroovyClassScope.debugListener = null;
        }
    }

    /**
     * Now a class implementing an interface.  The subclass gets augmented because the superclass did not.
     */
    @Test
    public void testClassHierarchiesAndGroovyObjectMethods2() {
        try {
            GroovyClassScope.debugListener = new EventListener("augment");
            //@formatter:off
            String[] sources = {
                "Foo.groovy",
                "class Foo implements One {\n" +
                "  public void m() {\n" +
                "    Object o = getMetaClass();\n" +
                "  }\n" +
                "  static main(args) { print 'abc'} \n" +
                "}\n" +
                "interface One {\n" +
                "}\n",
            };
            //@formatter:on

            runConformTest(sources, "abc");
            assertEventCount(1, GroovyClassScope.debugListener);
            assertEvent("augment: type Foo having GroovyObject methods added", GroovyClassScope.debugListener);
        } finally {
            GroovyClassScope.debugListener = null;
        }
    }

    /**
     * Now a class extending a java type which extends a base groovy class.  Super groovy type should get them.
     *
     * This looks odd to me, not sure why Foo and One both get the methods when One inherits them through Foo -
     * perhaps the java type in the middle makes a difference.  Anyway by augmenting both of these we
     * are actually doing the same as groovyc, and that is the main thing.
     */
    @Test
    public void testClassHierarchiesAndGroovyObjectMethods3() {
        try {
            GroovyClassScope.debugListener = new EventListener();
            //@formatter:off
            String[] sources = {
                "Foo.groovy",
                "class Foo extends Two {\n" +
                "  public void m() {\n" +
                "    Object o = getMetaClass();\n" +
                "  }\n" +
                "  static main(args) { print 'abc'} \n" +
                "}\n" +
                "class One {\n" +
                "}\n",

                "Two.java",
                "class Two extends One {\n" +
                "}\n",
            };
            //@formatter:on

            runConformTest(sources, "abc");
            assertEventCount(1, GroovyClassScope.debugListener);
            assertEvent("augment: type One having GroovyObject methods added", GroovyClassScope.debugListener);
        } finally {
            GroovyClassScope.debugListener = null;
        }
    }

    @Test
    public void testClassHierarchiesAndGroovyObjectMethods4() {
        try {
            GroovyClassScope.debugListener = new EventListener();
            //@formatter:off
            String[] sources = {
                "Foo.groovy",
                "class Foo {\n" +
                "  static main(args) { print 'works' }\n"+
                "  class Bar {\n" +
                "  }\n" +
                "}\n",
            };
            //@formatter:on

            runConformTest(sources, "works");
            assertEventCount(2, GroovyClassScope.debugListener);
            assertEvent("augment: type Foo having GroovyObject methods added", GroovyClassScope.debugListener);
            assertEvent("augment: type Bar having GroovyObject methods added", GroovyClassScope.debugListener);
        } finally {
            GroovyClassScope.debugListener = null;
        }
    }

    @Test
    public void testParentIsObject_GRE528() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
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
            "  public @groovy.transform.Generated X() {\n" +
            "  }\n" +
            "  public static void main(java.lang.String... args) {\n" +
            "  }\n" +
            "}\n"
        );
    }

    @Test
    public void testOverriding_GRE440() {
        //@formatter:off
        String[] sources = {
            "Bar.groovy",
            "class Bar {\n" +
            "  static void main(args) {}\n" +
            "}\n",

            "Foo.java",
            "class Foo extends Bar { \n" +
            "  public static void main(String[] args) {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testOverriding_GRE440_2() {
        //@formatter:off
        String[] sources = {
            "Bar.java",
            "class Bar {\n" +
            "  void main(String... strings) {}\n" +
            "}\n",

            "Foo.java",
            "class Foo extends Bar {\n" +
            "  void main(String[] strings) {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. WARNING in Foo.java (at line 2)\n" +
            "\tvoid main(String[] strings) {}\n" +
            "\t     ^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Varargs methods should only override or be overridden by other varargs methods unlike Foo.main(String[]) and Bar.main(String...)\n" +
            "----------\n");
    }

    @Test
    public void testOverriding_FinalMethod1() {
        //@formatter:off
        String[] sources = {
            "Bar.groovy",
            "class Bar {\n" +
            "  final getSomething() {}\n" +
            "}\n",

            "Foo.groovy",
            "class Foo extends Bar {\n" +
            "  def getSomething() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 2)\n" +
            "\tdef getSomething() {}\n" +
            "\t    ^^^^^^^^^^^^^^\n" +
            "Groovy:You are not allowed to override the final method getSomething() from class 'Bar'.\n" +
            "----------\n");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-8659
    public void testOverriding_FinalMethod2() {
        //@formatter:off
        String[] sources = {
            "Bar.groovy",
            "class Bar {\n" +
            "  final getSomething() {}\n" +
            "}\n",

            "Foo.groovy",
            "class Foo extends Bar {\n" +
            "  def something = 32\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, isAtLeastGroovy(50) ?
            "----------\n" +
            "1. WARNING in Foo.groovy (at line 2)\n" +
            "\tdef something = 32\n" +
            "\t^\n" +
            "Groovy:Property something cannot override final method getSomething() of class Bar\n" +
            "----------\n" : "");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-11548 and GROOVY-11758
    public void testOverriding_FinalMethod3() {
        //@formatter:off
        String[] sources = {
            "Bar.groovy",
            "class Bar {\n" +
            "  protected final getSomething() {}\n" +
            "}\n",

            "Baz.groovy",
            "trait Baz {\n" +
            "  def something = null\n" +
            "}\n",

            "Foo.groovy",
            "class Foo extends Bar implements Baz {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 1)\n" +
            "\tclass Foo extends Bar implements Baz {\n" +
            "\t      ^^^\n" +
            (isAtLeastGroovy(40)
            ? "Groovy:inherited final method getSomething() from Bar cannot shadow the public method in Baz\n"
            : "Groovy:You are not allowed to override the final method getSomething() from class 'Bar'.\n"
            ) +
            "----------\n");
    }

    @Test
    public void testOverriding_ReducedVisibility1() {
        //@formatter:off
        String[] sources = {
            "Bar.groovy",
            "class Bar {\n" +
            "  public void baz() {}\n" +
            "}\n",

            "Foo.groovy",
            "class Foo extends Bar {\n" +
            "  private void baz() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 2)\n" +
            "\tprivate void baz() {}\n" +
            "\t             ^^^^^\n" +
            "Groovy:baz() in Foo cannot override baz in Bar; attempting to assign weaker access privileges; was public\n" +
            "----------\n");
    }

    @Test
    public void testOverriding_ReducedVisibility1a() {
        //@formatter:off
        String[] sources = {
            "Bar.groovy",
            "class Bar {\n" +
            "  public void baz() {}\n" +
            "}\n",

            "Foo.groovy",
            "class Foo extends Bar {\n" +
            "  protected void baz() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 2)\n" +
            "\tprotected void baz() {}\n" +
            "\t               ^^^^^\n" +
            "Groovy:baz() in Foo cannot override baz in Bar; attempting to assign weaker access privileges; was public\n" +
            "----------\n");
    }

    @Test
    public void testOverriding_ReducedVisibility1b() {
        //@formatter:off
        String[] sources = {
            "Bar.groovy",
            "class Bar {\n" +
            "  public void baz() {}\n" +
            "}\n",

            "Foo.groovy",
            "class Foo extends Bar {\n" +
            "  @groovy.transform.PackageScope void baz() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 2)\n" +
            "\t@groovy.transform.PackageScope void baz() {}\n" +
            "\t                                    ^^^^^\n" +
            "Groovy:baz() in Foo cannot override baz in Bar; attempting to assign weaker access privileges; was public\n" +
            "----------\n");
    }

    @Test
    public void testOverriding_ReducedVisibility2() {
        //@formatter:off
        String[] sources = {
            "Bar.groovy",
            "class Bar {\n" +
            "  protected void baz() {}\n" +
            "}\n",

            "Foo.groovy",
            "class Foo extends Bar {\n" +
            "  private void baz() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 2)\n" +
            "\tprivate void baz() {}\n" +
            "\t             ^^^^^\n" +
            "Groovy:baz() in Foo cannot override baz in Bar; attempting to assign weaker access privileges; was protected\n" +
            "----------\n");
    }

    @Test
    public void testOverriding_ReducedVisibility2a() {
        //@formatter:off
        String[] sources = {
            "Bar.groovy",
            "class Bar {\n" +
            "  protected void baz() {}\n" +
            "}\n",

            "Foo.groovy",
            "class Foo extends Bar {\n" +
            "  @groovy.transform.PackageScope void baz() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 2)\n" +
            "\t@groovy.transform.PackageScope void baz() {}\n" +
            "\t                                    ^^^^^\n" +
            "Groovy:baz() in Foo cannot override baz in Bar; attempting to assign weaker access privileges; was protected\n" +
            "----------\n");
    }

    @Test
    public void testOverriding_ReducedVisibility3() {
        //@formatter:off
        String[] sources = {
            "Bar.groovy",
            "class Bar {\n" +
            "  @groovy.transform.PackageScope void baz() {}\n" +
            "}\n",

            "Foo.groovy",
            "class Foo extends Bar {\n" +
            "  private void baz() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 2)\n" +
            "\tprivate void baz() {}\n" +
            "\t             ^^^^^\n" +
            "Groovy:baz() in Foo cannot override baz in Bar; attempting to assign weaker access privileges; was package-private\n" +
            "----------\n");
    }

    @Test
    public void testOverriding_ReducedVisibility3a() {
        //@formatter:off
        String[] sources = {
            "Bar.java",
            "public class Bar {\n" +
            "  void baz() {}\n" +
            "}\n",

            "Foo.groovy",
            "class Foo extends Bar {\n" +
            "  private void baz() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 2)\n" +
            "\tprivate void baz() {}\n" +
            "\t             ^^^^^\n" +
            "Groovy:baz() in Foo cannot override baz in Bar; attempting to assign weaker access privileges; was package-private\n" +
            "----------\n");
    }

    @Test
    public void testOverriding_MissingAnnotation1() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotation, CompilerOptions.ERROR);

        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "class Foo {\n" +
            "  boolean equals(that) {\n" +
            "    false\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 2)\n" +
            "\tboolean equals(that) {\n" +
            "\t        ^^^^^^^^^^^^\n" +
            "The method equals(Object) of type Foo should be tagged with @Override since it actually overrides a superclass method\n" +
            "----------\n",
            options);
    }

    @Test
    public void testOverriding_MissingAnnotation2() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotation, CompilerOptions.ERROR);

        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "class Foo {\n" +
            "  void bar() {\n" +
            "    def baz = new Object() {\n" +
            "      boolean equals(that) {\n" +
            "        false\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 4)\n" +
            "\tboolean equals(that) {\n" +
            "\t        ^^^^^^^^^^^^\n" +
            "The method equals(Object) of type new Object(){} should be tagged with @Override since it actually overrides a superclass method\n" +
            "----------\n",
            options);
    }

    @Test
    public void testOverriding_MissingAnnotation3() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
        options.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotation, CompilerOptions.ERROR);
        options.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.ENABLED);

        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "class Foo implements Iterable {\n" +
            "  Iterator iterator() {\n" +
            "    null\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 2)\n" +
            "\tIterator iterator() {\n" +
            "\t         ^^^^^^^^^^\n" +
            "The method iterator() of type Foo should be tagged with @Override since it actually overrides a superinterface method\n" +
            "----------\n",
            options);
    }

    @Test
    public void testOverriding_MissingAnnotation4() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
        options.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotation, CompilerOptions.ERROR);
        options.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.ENABLED);

        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "class Foo {\n" +
            "  void bar() {\n" +
            "    def baz = new Iterable() {\n" +
            "      Iterator iterator() {\n" +
            "        null\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 4)\n" +
            "\tIterator iterator() {\n" +
            "\t         ^^^^^^^^^^\n" +
            "The method iterator() of type new Iterable(){} should be tagged with @Override since it actually overrides a superinterface method\n" +
            "----------\n",
            options);
    }

    @Test
    public void testOverriding_StaticMethodHidesInstanceMethod() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class C {\n" +
            "  def m() { 'C' }\n" +
            "}\n" +
            "class D extends C {\n" +
            "  static m() { 'D' }\n" +
            "}\n" +
            "print new D().m()\n",
        };
        //@formatter:on

        runConformTest(sources, "D");
    }

    @Test
    public void testOverriding_InstanceMethodCoversStaticMethod() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class C {\n" +
            "  static m() { 'C' }\n" +
            "}\n" +
            "class D extends C {\n" +
            "  def m() { 'D' }\n" +
            "}\n" +
            "print new D().m()\n",
        };
        //@formatter:on

        runConformTest(sources, "D");
    }

    @Test
    public void testAbstractMethodWithBody1() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "abstract def meth() {\n" +
            "  println 42\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Script.groovy (at line 1)\n" +
            "\tabstract def meth() {\n" +
            "\t^\n" +
            "Groovy:" + (!isParrotParser()
                ? "Abstract methods do not define a body.\n"
                : "You cannot define an abstract method[meth] in the script. Try removing the 'abstract'\n") +
            "----------\n");
    }

    @Test
    public void testAbstractMethodWithBody2() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  abstract def meth() {\n" +
            "    println 42\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        if (!isParrotParser()) {
            runNegativeTest(sources,
                "----------\n" +
                "1. ERROR in Main.groovy (at line 2)\n" +
                "\tabstract def meth() {\n" +
                "\t^\n" +
                "Groovy:Abstract methods do not define a body.\n" +
                "----------\n");
        } else {
            runNegativeTest(sources,
                "----------\n" +
                "1. ERROR in Main.groovy (at line 1)\n" +
                "\tclass Main {\n" +
                "\t      ^^^^\n" +
                "The type Main must be an abstract class to define abstract methods\n" +
                "----------\n" +
                "2. ERROR in Main.groovy (at line 1)\n" +
                "\tclass Main {\n" +
                "\t      ^^^^\n" +
                "Groovy:Can't have an abstract method in a non-abstract class." +
                " The class 'Main' must be declared abstract or the method 'java.lang.Object meth()' must be implemented.\n" +
                "----------\n" +
                "3. ERROR in Main.groovy (at line 2)\n" +
                "\tabstract def meth() {\n" +
                "\t             ^^^^^^\n" +
                "The abstract method meth in type Main can only be defined by an abstract class\n" +
                "----------\n" +
                "4. ERROR in Main.groovy (at line 2)\n" +
                "\tabstract def meth() {\n" +
                "\t             ^^^^^^\n" +
                "Abstract methods do not specify a body\n" +
                "----------\n" +
                "5. ERROR in Main.groovy (at line 2)\n" +
                "\tabstract def meth() {\n" +
                "\t             ^^^^^^\n" +
                "Groovy:Can't have an abstract method in a non-abstract class." +
                " The class 'Main' must be declared abstract or the method 'java.lang.Object meth()' must not be abstract.\n" +
                "----------\n");
        }
    }

    @Test
    public void testAbstractCovariance_GRE272() {
        //@formatter:off
        String[] sources = {
            "A.java",
            "public class A { }",

            "AA.java",
            "public class AA extends A { }",

            "I.java",
            "public interface I { A getA(); }",

            "Impl.java",
            "public class Impl implements I { public AA getA() { return null; } }",

            "GImpl.groovy",
            "class GImpl extends Impl { }",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    // If GroovyFoo is processed *before* FooBase then the MethodVerifier15
    // hasn't had a chance to run on FooBase and create the synthetic bridge method
    @Test
    public void testAbstractCovariance_GRE272_2() {
        //@formatter:off
        String[] sources = {
            "test/Bar.java",
            "package test;\n" +
            "public class Bar extends BarBase { }",

            "test/BarBase.java",
            "package test;\n" +
            "abstract public class BarBase { }",

            "test/GroovyFoo.groovy",
            "package test;\n" +
            "class GroovyFoo extends FooBase { }",

            "test/FooBase.java",
            "package test;\n" +
            "public class FooBase implements IFoo { public Bar foo() { return null; } }",

            "test/IFoo.java",
            "package test;\n" +
            "public interface IFoo { BarBase foo(); }",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testAbstractCovariance_GRE272_3() {
        //@formatter:off
        String[] sources = {
            "test/IFoo.java",
            "package test;\n" +
            "public interface IFoo { BarBase foo(); }",

            "test/GroovyFoo.groovy",
            "package test;\n" +
            "class GroovyFoo extends FooBase { }",

            "test/FooBase.java",
            "package test;\n" +
            "public class FooBase implements IFoo { public Bar foo() { return null; } }",

            "test/BarBase.java",
            "package test;\n" +
            "abstract public class BarBase { }",

            "test/Bar.java",
            "package test;\n" +
            "public class Bar extends BarBase { }",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testAbstractCovariance_GRE272_4() {
        //@formatter:off
        String[] sources = {
            "test/IFoo.java",
            "package test;\n" +
            "public interface IFoo { BarBase foo(); }",

            "test/FooBase.java",
            "package test;\n" +
            "public class FooBase implements IFoo { public Bar foo() { return null; } }",

            "test/BarBase.java",
            "package test;\n" +
            "abstract public class BarBase { }",

            "test/Bar.java",
            "package test;\n" +
            "public class Bar extends BarBase { }",

            "test/GroovyFoo.groovy",
            "package test;\n" +
            "class GroovyFoo extends FooBase { }",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testIncorrectReturnType_GRE292() {
        //@formatter:off
        String[] sources = {
            "Voidy.groovy",
            "public class VoidReturnTestCase {\n" +
            "  void returnSomething() {\n" +
            "    return true && false\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Voidy.groovy (at line 3)\n" +
            "\treturn true && false\n" +
            "\t^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:Cannot use return statement with an expression on a method that returns void\n" +
            "----------\n");
    }

    @Test
    public void testIncorrectReturnType_GRE292_3() {
        //@formatter:off
        String[] sources = {
            "Voidy.groovy",
            "public class VoidReturnTestCase {\n" +
            "  void returnSomething() {\n" +
            "    return true\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Voidy.groovy (at line 3)\n" +
            "\treturn true\n" +
            "\t^^^^^^^^^^^\n" +
            "Groovy:Cannot use return statement with an expression on a method that returns void\n" +
            "----------\n");
    }

    @Test
    public void testIncorrectReturnType_GRE292_2() {
        //@formatter:off
        String[] sources = {
            "Voidy.groovy",
            "public class VoidReturnTestCase {\n" +
            "  static void returnSomething() {\n" +
            "    return true\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Voidy.groovy (at line 3)\n" +
            "\treturn true\n" +
            "\t^^^^^^^^^^^\n" +
            "Groovy:Cannot use return statement with an expression on a method that returns void\n" +
            "----------\n");
    }

    @Test
    public void testIncorrectReturnType_GRE292_4() {
        //@formatter:off
        String[] sources = {
            "Voidy.groovy",
            "public class VoidReturnTestCase {\n" +
            "  void returnSomething() {\n" +
            "    return 375+26\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Voidy.groovy (at line 3)\n" +
            "\treturn 375+26\n" +
            "\t^^^^^^^^^^^^^\n" +
            "Groovy:Cannot use return statement with an expression on a method that returns void\n" +
            "----------\n");
    }

    @Test
    public void testIncorrectReturnType_GRE396() {
        //@formatter:off
        String[] sources = {
            "TrivialBugTest.groovy",
            "package org.sjb.sjblib.cmdline;\n" +
            "public final class TrivialBugTest {\n" +
            "  void func2() {\n" +
            "    tb = new TrivialBug()\n" +
            "  }\n" +
            "}\n",

            "TrivialBug.groovy",
            "package org.sjb.sjblib.cmdline;\n" +
            "public class TrivialBug {\n" +
            "  void func() {\n" +
            "    return 5\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in TrivialBug.groovy (at line 4)\n" +
            "\treturn 5\n" +
            "\t^^^^^^^^\n" +
            "Groovy:Cannot use return statement with an expression on a method that returns void\n" +
            "----------\n");
    }

    @Test
    public void testIncorrectReturnType_GRE396_2() {
        //@formatter:off
        String[] sources = {
            "TrivialBug.groovy",
            "package org.sjb.sjblib.cmdline;\n" +
            "public class TrivialBug {\n" +
            "  void func() {\n" +
            "    return 5\n" +
            "  }\n" +
            "}\n",

            "TrivialBugTest.groovy",
            "package org.sjb.sjblib.cmdline;\n" +
            "public final class TrivialBugTest {\n" +
            "  void func2() {\n" +
            "    tb = new TrivialBug()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in TrivialBug.groovy (at line 4)\n" +
            "\treturn 5\n" +
            "\t^^^^^^^^\n" +
            "Groovy:Cannot use return statement with an expression on a method that returns void\n" +
            "----------\n");
    }

    @Test
    public void testMissingTypesForGeneratedBindingsGivesNPE_GRE273() {
        //@formatter:off
        String[] sources = {
            "GProperty.groovy",
            "import org.andrill.coretools.data.Model\n" +
            "import org.andrill.coretools.data.ModelCollection\n" +
            "import org.andrill.coretools.data.edit.Command\n" +
            "import org.andrill.coretools.data.edit.EditableProperty\n" +
            "import org.andrill.coretools.data.edit.commands.CompositeCommand\n" +
            "\n" +
            "@SuppressWarnings('rawtypes')\n" +
            "class GProperty implements EditableProperty {\n" +
            "  def source\n" +
            "  String name\n" +
            "  String widgetType\n" +
            "  Map widgetProperties = [:]\n" +
            "  Map constraints = [:]\n" +
            "  List validators = []\n" +
            "  Command command\n" +
            "  \n" +
            "  String getValue() {\n" +
            "    if (source instanceof Model) {\n" +
            "      return source.modelData[name]\n" +
            "    } else {\n" +
            "      return (source.\"$name\" as String)\n" +
            "    }\n" +
            "  }\n" +
            "  \n" +
            "  boolean isValid(String newValue) {\n" +
            "    try {\n" +
            "      return validators.inject(true) { valid, validator ->\n" +
            "        valid && validator.call([newValue, source])\n" +
            "      }\n" +
            "    } catch (e) {\n" +
            "      return false\n" +
            "    }\n" +
            "  }\n" +
            "  \n" +
            "  Command getCommand(String newValue) {\n" +
            "    if (constraints?.linkTo && source instanceof Model) {\n" +
            "      def value = source.\"$name\"\n" +
            "      def links = source.collection.models.findAll {\n" +
            "        it.class == source.class && it?.\"${constraints.linkTo}\" == value\n" +
            "      }\n" +
            "      if (links) {\n" +
            "        def commands = []\n" +
            "        commands << new GCommand(source: source, prop: name, value: newValue)\n" +
            "        links.each {\n" +
            "          commands << new GCommand(source: it, prop: constraints.linkTo, value: newValue)\n" +
            "        }\n" +
            "        return new CompositeCommand(\"Change $name\", (commands as Command[]))\n" +
            "      } else {\n" +
            "        return new GCommand(source: source, prop: name, value: newValue)\n" +
            "      }\n" +
            "    } else {\n" +
            "      return new GCommand(source: source, prop: name, value: newValue)\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in GProperty.groovy (at line 1)\n" +
            "\timport org.andrill.coretools.data.Model\n" +
            "\t       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:unable to resolve class org.andrill.coretools.data.Model\n" +
            "----------\n" +
            "2. ERROR in GProperty.groovy (at line 2)\n" +
            "\timport org.andrill.coretools.data.ModelCollection\n" +
            "\t       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:unable to resolve class org.andrill.coretools.data.ModelCollection\n" +
            "----------\n" +
            "3. ERROR in GProperty.groovy (at line 3)\n" +
            "\timport org.andrill.coretools.data.edit.Command\n" +
            "\t       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:unable to resolve class org.andrill.coretools.data.edit.Command\n" +
            "----------\n" +
            "4. ERROR in GProperty.groovy (at line 4)\n" +
            "\timport org.andrill.coretools.data.edit.EditableProperty\n" +
            "\t       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:unable to resolve class org.andrill.coretools.data.edit.EditableProperty\n" +
            "----------\n" +
            "5. ERROR in GProperty.groovy (at line 5)\n" +
            "\timport org.andrill.coretools.data.edit.commands.CompositeCommand\n" +
            "\t       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:unable to resolve class org.andrill.coretools.data.edit.commands.CompositeCommand\n" +
            "----------\n" +
            "6. ERROR in GProperty.groovy (at line 8)\n" +
            "\tclass GProperty implements EditableProperty {\n" +
            "\t      ^^^^^^^^^\n" +
            "Groovy:You are not allowed to implement the class 'org.andrill.coretools.data.edit.EditableProperty', use extends instead.\n" +
            "----------\n" +
            "7. ERROR in GProperty.groovy (at line 43)\n" +
            "\tcommands << new GCommand(source: source, prop: name, value: newValue)\n" +
            "\t                ^^^^^^^^\n" +
            "Groovy:unable to resolve class GCommand\n" +
            "----------\n" +
            "8. ERROR in GProperty.groovy (at line 45)\n" +
            "\tcommands << new GCommand(source: it, prop: constraints.linkTo, value: newValue)\n" +
            "\t                ^^^^^^^^\n" +
            "Groovy:unable to resolve class GCommand\n" +
            "----------\n" +
            "9. ERROR in GProperty.groovy (at line 49)\n" +
            "\treturn new GCommand(source: source, prop: name, value: newValue)\n" +
            "\t           ^^^^^^^^\n" +
            "Groovy:unable to resolve class GCommand\n" +
            "----------\n" +
            "10. ERROR in GProperty.groovy (at line 52)\n" +
            "\treturn new GCommand(source: source, prop: name, value: newValue)\n" +
            "\t           ^^^^^^^^\n" +
            "Groovy:unable to resolve class GCommand\n" +
            "----------\n");
    }

    @Test
    public void testMissingTypesForGeneratedBindingsGivesNPE_GRE273_2() {
        //@formatter:off
        String[] sources = {
            "A.groovy",
            "class A {\n" +
            "  String s;" +
            "  String getS(String foo) { return null; }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testAbstractClass_GRE274() {
        //@formatter:off
        String[] sources = {
            "p/Foo.groovy",
            "class Foo {\n" +
            "  public static void main(String[] argv) {\n" +
            "    new C();\n" +
            "  }\n" +
            "}\n",

            "p/C.java",
            "package p;\n" +
            "public abstract class C {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\Foo.groovy (at line 3)\n" +
            "\tnew C();\n" +
            "\t    ^\n" +
            "Groovy:unable to resolve class C\n" +
            "----------\n");
    }

    @Test
    public void testAbstractClass_GRE274_2() {
        //@formatter:off
        String[] sources = {
            "p/Foo.groovy",
            "class Foo {\n" +
            "  public static void main(String[] argv) {\n" +
            "    new Wibble();\n" +
            "  }\n" +
            "}\n",

            "Wibble.groovy",
            "//@SuppressWarnings(\"cast\")\n" +
            "public class Wibble implements Comparable<String> {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Wibble.groovy (at line 2)\n" +
            "\tpublic class Wibble implements Comparable<String> {\n" +
            "\t             ^^^^^^\n" +
            "Groovy:Can\'t have an abstract method in a non-abstract class. The class \'Wibble\'" +
            " must be declared abstract or the method \'int compareTo(" + (isAtLeastGroovy(50) ? "T" : "java.lang.Object") + ")\' must be implemented.\n" +
            "----------\n");
    }

    @Test
    public void testSwitchCases1() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "def foo(p) {\n" +
            "  switch (p) {\n" +
            "   case 1:\n" +
            "    'a'\n" +
            "    break\n" +
            "   case 2:\n" +
            "    if (false) 'b'\n" +
            "    else 'c'\n" +
            "    break\n" +
            "   case 3:\n" +
            "    'skip'\n" +
            "   default:\n" +
            "    'd'\n" +
            "  }\n" +
            "}\n" +
            "print foo(1)\n" +
            "print foo(2)\n" +
            "print foo(3)\n" +
            "print foo(4)\n",
        };
        //@formatter:on

        runConformTest(sources, "acdd");
    }

    @Test // GROOVY-9896
    public void testSwitchCases2() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "def foo(p) {\n" +
            "  switch (p) {\n" +
            "   case 1:\n" +
            "    'a'\n" +
            "    break\n" +
            "   case 2:\n" +
            "    'b'\n" +
            "    break\n" +
            "   case 3:\n" +
            "    'c'\n" +
            "  }\n" +
            "}\n" +
            "print foo(1)\n" +
            "print foo(2)\n" +
            "print foo(3)\n" +
            "print foo(4)\n",
        };
        //@formatter:on

        runConformTest(sources, "abcnull");
    }

    @Test // GROOVY-4727
    public void testSwitchCases3() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "def foo(x,y) {\n" +
            "  switch (x) {\n" +
            "  case 'x1':\n" +
            "    switch (y) {\n" +
            "    case 'y1':\n" +
            "      'r1'\n" +
            "      break\n" +
            "    case 'y2':\n" +
            "      'r2'\n" +
            "      break\n" +
            "    }\n" +
            // no break
            "  }\n" +
            "}\n" +
            "print foo('x1','y1')\n",
        };
        //@formatter:on

        runConformTest(sources, "r1");
    }

    @Test // GROOVY-9272
    public void testSwitchCases4() {
        assumeTrue(isParrotParser() && isAtLeastGroovy(40));

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "def s = 'Bar'\n" +
            "int yield = switch (s) {\n" +
            "  case 'Foo':\n" +
            "    yield 1\n" +
            "  case 'Bar':\n" +
            "    print 2\n" +
            "  case 'Baz':\n" +
            "    yield 3\n" +
            "  default:\n" +
            "    yield 0\n" +
            "}\n" +
            "print yield\n",
        };
        //@formatter:on

        runConformTest(sources, "23");
    }

    @Test // GROOVY-9272
    public void testSwitchCases5() {
        assumeTrue(isParrotParser() && isAtLeastGroovy(40));

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import java.time.DayOfWeek\n" +
            "import static java.time.DayOfWeek.*\n" +
            "\n" +
            "void test(DayOfWeek day) {\n" +
            "  int letterCount = switch (day) {\n" +
            "    case MONDAY, FRIDAY, SUNDAY -> 6\n" +
            "    case TUESDAY                -> 7\n" +
            "    case THURSDAY, SATURDAY     -> { 8 }\n" +
            "    case WEDNESDAY              -> { yield 9 }\n" +
            "    default                     -> throw new IllegalStateException(\"Invalid day: $day\")\n" +
            "  }\n" +
            "  print letterCount\n" +
            "}\n" +
            "test(WEDNESDAY)\n",
        };
        //@formatter:on

        runConformTest(sources, "9");
    }

    @Test // GROOVY-9272
    public void testSwitchCases6() {
        assumeTrue(isParrotParser() && isAtLeastGroovy(40));

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import java.time.DayOfWeek\n" +
            "import static java.time.DayOfWeek.*\n" +
            "\n" +
            "void test(DayOfWeek day) {\n" +
            "  int letterCount = switch (day) {\n" +
            "    case MONDAY, FRIDAY, SUNDAY: yield 6\n" +
            "    case TUESDAY               : yield 7\n" +
            "    case THURSDAY, SATURDAY    : yield 8\n" +
            "    case WEDNESDAY             : { yield 9 }\n" +
            "    default                    : throw new IllegalStateException(\"Invalid day: $day\")\n" +
            "  }\n" +
            "  print letterCount\n" +
            "}\n" +
            "test(WEDNESDAY)\n",
        };
        //@formatter:on

        runConformTest(sources, "9");
    }

    @Test // GROOVY-9272
    public void testSwitchCases7() {
        assumeTrue(isParrotParser() && isAtLeastGroovy(40));

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import java.time.DayOfWeek\n" +
            "import static java.time.DayOfWeek.*\n" +
            "\n" +
            "void test(DayOfWeek day) {\n" +
            "  print(switch (day) {\n" +
            "    case MONDAY..FRIDAY    : yield 'work day'\n" +
            "    case [SATURDAY, SUNDAY]: yield 'weekend'\n" +
            "  })\n" +
            "}\n" +
            "test(SUNDAY)\n" +
            "print ' '\n" +
            "test(WEDNESDAY)\n",
        };
        //@formatter:on

        runConformTest(sources, "weekend work day");
    }

    @Test // GROOVY-9880
    public void testBreakAfterIf() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "switch ('value') {\n" +
            " case 'value':\n" +
            "  print 'foo'\n" +
            "  if (false) print 'X'\n" +
            "  break\n" +
            " default:\n" +
            "  print 'bar'\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "foo"); // not "foobar"
    }

    @Test
    public void testBreak_GRE290() {
        //@formatter:off
        String[] sources = {
            "X.groovy",
            "words: [].each { final item ->\n" +
            "  break words\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in X.groovy (at line 2)\n" +
            "\tbreak words\n" +
            "\t^^^^^^^^^^^\n" +
            "Groovy:" + (!isParrotParser()
                ? "the break statement with named label is only allowed inside loops\n"
                : "break statement is only allowed inside loops or switches\n") +
            "----------\n");
    }

    @Test
    public void testContinue_GRE291() {
        //@formatter:off
        String[] sources = {
            "ContinueTestCase.groovy",
            "public class ContinueTestCase {\n" +
            "  public ContinueTestCase() {\n" +
            "    continue;\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in ContinueTestCase.groovy (at line 3)\n" +
            "\tcontinue;\n" +
            "\t^^^^^^^^\n" +
            "Groovy:" + (!isParrotParser()
                ? "the continue statement is only allowed inside loops\n"
                : "continue statement is only allowed inside loops\n") +
            "----------\n");
    }

    @Test
    public void testMissingContext_GRE308() {
        //@formatter:off
        String[] sources = {
            "DibDabs.groovy",
            "\tdef run(n) {\n\n" +
            "\t\t  OtherGroovy.iterate (3) {\n" +
            "\t\t  print it*2\n" +
            "\t  }  \n" +
            "//\t\t  \t\tNOT RECORDED AGAINST THIS FILE??\n" +
            "\t\t  int i        ",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in DibDabs.groovy (at line 7)\n" +
            "\tint i        \n" +
            "\t            ^\n" +
            "Groovy:expecting \'}\', found \'\'\n" +
            "----------\n");
    }

    @Test // FIXASC less than ideal underlining for error location
    public void testMissingContext_GRE308_2() {
        //@formatter:off
        String[] sources = {
            "DibDabs.groovy",
            "\tdef run(n) {\n\n" +
            "\t\t  OtherGroovy.iterate (3) {\n" +
            "\t\t  print it*2\n" +
            "\t  }  \n" +
            "//\t\t  \t\tNOT RECORDED AGAINST THIS FILE??\n" +
            "\t\t  int i        \n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in DibDabs.groovy (at line 7)\n" +
            "\tint i        \n" +
            "\n" +
            "\t             ^\n" +
            "Groovy:expecting \'}\', found \'\'\n" +
            "----------\n");
    }

    @Test
    public void testSloppyScript_GRE323_1() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "def foo(closure) {\n" +
            "  closure()\n" +
            "}\n" +
            "\n" +
            "foo {\n" +
            "  final session2 = null\n" +
            "  \n" +
            "  // Define scenarios\n" +
            "  def secBoardRep = session2\n" +
            "  def x\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testSloppyScript_GRE323_2() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "def foo(closure) {\n" +
            "  closure()\n" +
            "}\n" +
            "\n" +
            "foo {\n" +
            "  final session2 = null\n" +
            "  \n" +
            "  // Define scenarios\n" +
            "  def secBoardRep = session2.\n" + // trailing dot
            "  def x\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 10)\n" +
            "\tdef x\n" +
            "\t^\n" +
            "Groovy:unexpected token: def\n" +
            "----------\n");
    }

    @Test // removed surrounding method
    public void testSloppyScript_GRE323_3() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "final session2 = null\n" +
            "\n" +
            "// Define scenarios\n" +
            "def secBoardRep = session2\n" +
            "def x\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testSloppyScript_GRE323_4() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "def foo(closure) {\n" +
            "  closure()\n" +
            "}\n" +
            "\n" +
            "foo {\n" +
            "  final session2 = null\n" +
            "  \n" +
            "  // Define scenarios\n" +
            "  session2.\n" + // trailing dot
            "  def x\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 10)\n" +
            "\tdef x\n" +
            "\t^\n" +
            "Groovy:unexpected token: def\n" +
            "----------\n");
    }

    @Test
    public void testSloppyScript_GRE323_4b() {
        //@formatter:off
        String[] sources = {
            "Run.java",
            "public class Run {\n" +
            "  public static void main(String[]argv) {\n" +
            "    try {\n" +
            "      Foo.main();\n" +
            "    } catch (Throwable t) {\n" +
            "      System.out.println(t.getMessage());\n" +
            "    }\n" +
            "  }\n" +
            "}\n",

            "Foo.groovy",
            "def foo(closure) {\n" +
            "  closure()\n" +
            "}\n" +
            "\n" +
            "foo {\n" +
            "  final session2 = null\n" +
            "  \n" +
            "  // Define scenarios\n" +
            "  session2.\n" + // trailing dot
            "  def x\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 10)\n" +
            "\tdef x\n" +
            "\t^\n" +
            "Groovy:unexpected token: def\n" +
            "----------\n");
    }

    @Test
    public void testSloppyScript_GRE323_5() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "def foo(closure) {\n" +
            "  closure()\n" +
            "}\n" +
            "\n" +
            "foo {\n" +
            "  final session2 = [\"def\": { println \"DEF\" }]\n" +
            "  \n" +
            "  final x = 1\n" +
            "  // Define scenarios\n" +
            "  session2.\n" + // trailing dot
            "  def x\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 11)\n" +
            "\tdef x\n" +
            "\t^\n" +
            "Groovy:unexpected token: def\n" +
            "----------\n" +
            "2. ERROR in Foo.groovy (at line 11)\n" +
            "\tdef x\n" +
            "\t    ^\n" +
            "Groovy:The current scope already contains a variable of the name x\n" +
            "----------\n");
    }

    @Test
    public void testSloppyScript_GRE323_5b() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "def foo(closure) {\n" +
            "  closure()\n" +
            "}\n" +
            "\n" +
            "foo {\n" +
            "  final session2 = [\"def\": { println \"DEF\" }]\n" +
            "  \n" +
            "  final x = 1\n" +
            "  // Define scenarios\n" +
            "  session2.\n" + // trailing dot
            "  def x\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 11)\n" +
            "\tdef x\n" +
            "\t^\n" +
            "Groovy:unexpected token: def\n" +
            "----------\n" +
            "2. ERROR in Foo.groovy (at line 11)\n" +
            "\tdef x\n" +
            "\t    ^\n" +
            "Groovy:The current scope already contains a variable of the name x\n" +
            "----------\n");
    }

    @Test
    public void testSloppyScript_GRE323_6() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "def foo(closure) {\n" +
            "  closure()\n" +
            "}\n" +
            "\n" +
            "foo {\n" +
            "  final session2 = [\"def\": { println \"DEF\" }]\n" +
            "  \n" +
            "  final x = 1\n" +
            "  // Define scenarios\n" +
            "  final y = session2.def x\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "DEF");
    }

    @Test
    public void testBridgeMethods_GRE336() {
        //@formatter:off
        String[] sources = {
            "my/example/EnumBooleanMap.java",
            "package my.example;\n" +
            "\n" +
            "import java.util.EnumMap;\n" +
            "\n" +
            "@SuppressWarnings(\"serial\")\n" +
            "public class EnumBooleanMap<E extends Enum<E>> extends EnumMap<E, Boolean> {\n" +
            "  \n" +
            "  public EnumBooleanMap(Class<E> keyType) {\n" +
            "    super(keyType);\n" +
            "  }\n" +
            "\n" +
            "  public EnumBooleanMap(EnumBooleanMap<E> m) {\n" +
            "    super(m);\n" +
            "  }\n" +
            "\n" +
            "  @Override\n" +
            "  public Boolean get(Object key) {\n" +
            "    Boolean value = super.get(key);\n" +
            "    return value != null ? value : false;\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testTransientMethod_GRE370() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "public class Foo {\n" +
            "  public transient void foo() {}\n" +
            "}\n",
        };
        //@formatter:on

        if (isAtLeastGroovy(40)) {
            runNegativeTest(sources,
                "----------\n" +
                "1. ERROR in Foo.groovy (at line 2)\n" +
                "\tpublic transient void foo() {}\n" +
                "\t                      ^^^^^\n" +
                "Groovy:The method 'void foo()' has " + (isAtLeastGroovy(50) ? "invalid" : "an incorrect") + " modifier transient.\n" +
                "----------\n");
        } else {
            runNegativeTest(sources, "");
        }
    }

    @Test // The getter for 'description' implements the interface
    public void testImplementingAnInterfaceViaProperty() {
        //@formatter:off
        String[] sources = {
            "a/b/c/C.groovy",
            "package a.b.c;\n" +
            "import p.q.r.I;\n" +
            "public class C implements I {\n" +
            "  String description;\n" +
            "  public static void main(String[] argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "p/q/r/I.groovy",
            "package p.q.r;\n" +
            "public interface I {\n" +
            "  String getDescription();\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test // Referencing from a groovy to a java type where the reference is through a member, not the hierarchy
    public void testReferencingOtherTypesInSamePackage() {
        //@formatter:off
        String[] sources = {
            "a/b/c/C.groovy",
            "package a.b.c;\n" +
            "public class C {\n" +
            "  D description;\n" +
            "  public static void main(String[] argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",

            "a/b/c/D.java",
            "package a.b.c;\n" +
            "public class D {\n" +
            "  String getDescription() { return null; }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testProtectedType() {
        //@formatter:off
        String[] sources = {
            "p/Y.groovy",
            "package p;\n" +
            "class Y {\n" +
            "  public static void main(String[]argv) {\n" +
            "    new X().main(argv);\n" +
            "  }\n" +
            "}\n",

            "p/X.groovy",
            "package p;\n" +
            "protected class X {\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        if (!isAtLeastGroovy(50)) {
            runConformTest(sources, "success");
        } else {
            runNegativeTest(sources,
                "----------\n" +
                "1. ERROR in p\\X.groovy (at line 2)\n" +
                "\tprotected class X {\n" +
                "\t                ^\n" +
                "Groovy:The class 'p.X' has invalid modifier protected.\n" +
                "----------\n");
        }
    }

    @Test
    public void testStandaloneGroovySource1() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  public static void main(String[] argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testStandaloneGroovySource2() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
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
            "  public @groovy.transform.Generated X() {\n" +
            "  }\n" +
            "  public static void main(java.lang.String... args) {\n" +
            "  }\n" +
            "}\n"
        );
    }

    @Test
    public void testStandaloneGroovySource3() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "print 'success'\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testReadStaticFieldFromGtoJ() {
        //@formatter:off
        String[] sources = {
            "Foo.groovy",
            "print SomeJava.constant\n",
            "SomeJava.java",
            "class SomeJava { static String constant = \"abc\";}",
        };
        //@formatter:on

        runConformTest(sources, "abc");
    }

    @Test
    public void testCallStaticMethodFromGtoJ() {
        //@formatter:off
        String[] sources = {
            "p/Foo.groovy",
            "package p;\n" +
            "public class Foo {\n" +
            "  public static void main(String[]argv) {\n" +
            "    X.run()\n" +
            "  }\n" +
            "}\n",

            "p/X.java",
            "package p;\n" +
            "public class X {\n" +
            "  public static void run() {\n" +
            "    System.out.println(\"success\");\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testCallStaticMethodFromJtoG() {
        //@formatter:off
        String[] sources = {
            "p/Foo.java",
            "package p;\n" +
            "public class Foo {\n" +
            "  public static void main(String[]argv) {\n" +
            "    X.run();\n" +
            "  }\n" +
            "}\n",

            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  public static void run() {\n" +
            "    System.out.println(\"success\");\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testExtendingInterface1() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X extends I {\n" +
            "}\n",

            "p/I.groovy",
            "package p\n" +
            "interface I {}",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\tpublic class X extends I {\n" +
            "\t             ^\n" +
            "Groovy:You are not allowed to extend the interface \'p.I\', use implements instead.\n" +
            "----------\n");
    }

    @Test
    public void testExtendingInterface2() {
        //@formatter:off
        String[] sources = {
            "p/X.groovy",
            "package p;\n" +
            "public class X extends List<String> {\n" +
            "  public static void main(String[] argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\X.groovy (at line 2)\n" +
            "\tpublic class X extends List<String> {\n" +
            "\t             ^\n" +
            "Groovy:You are not allowed to extend the interface \'java.util.List\', use implements instead.\n" +
            "----------\n");
    }

    // WMTW: the type declaration building code creates the correct representation of A and adds the default constructor
    @Test
    public void testExtendingGroovyWithJava1() {
        //@formatter:off
        String[] sources = {
            "p/B.java",
            "package p;\n" +
            "public class B extends A {\n" +
            "  public static void main(String[] argv) {\n" +
            "    new B();\n" +
            "    System.out.println(\"success\");\n" +
            "  }\n" +
            "}\n",

            "p/A.groovy",
            "package p;\n" +
            "public class A {\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    // WMTW: a JDT resolver is plugged into groovy so it can see Java types
    // details:
    // 1. needed the lookupenvironment to flow down to the groovyparser (through initializeParser) as groovy will need it
    // for resolution of JDT types
    // 2. needed to subclass ResolveVisitor - trying just to override resolve(ClassNode) right now
    // 3. needed to build JDTClassNode and needed it to initialize the superclass field
    @Test
    public void testExtendingJavaWithGroovy1() {
        //@formatter:off
        String[] sources = {
            "p/B.groovy",
            "package p;\n" +
            "public class B extends A {\n" +
            "  public static void main(String[] argv) {\n" +
            "    new B();\n" +
            "    System.out.println(\"success\");\n" +
            "  }\n" +
            "}\n",

            "p/A.java",
            "package p;\n" +
            "public class A {\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testExtendingJavaWithGroovyAndThenJava() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C extends B {\n" +
            "  public static void main(String[] argv) {\n" +
            "    new C();\n" +
            "    System.out.println(\"success\");\n" +
            "  }\n" +
            "}\n",

            "p/B.groovy",
            "package p;\n" +
            "public class B extends A {\n" +
            "}\n",

            "p/A.java",
            "package p;\n" +
            "public class A {\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    // Groovy is allowed to have a public class like this in a file with a different name
    @Test
    public void testPublicClassInWrongFile() {
        //@formatter:off
        String[] sources = {
            "pkg/One.groovy",
            "package pkg;\n" +
            "public class One {" +
            "  public static void main(String[]argv) { print \"success\";}\n" +
            "}\n" +
            "public class Two {" +
            "  public static void main(String[]argv) { print \"success\";}\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    /**
     * WMTW: having a callback registered with groovy class generation that tracks which class file is created for which module node
     * details:
     * the groovy compilationunit provides a way to ask for the generated classes but it doesnt give a way to tell why they arose
     * (which sourceunit caused them to come into existence).  I am using the callback mechanism to track this information, but I worry
     * that we are causing groovy to perhaps do things too many times.  It also feels a little wierd that driving any single file through
     * to CLASSGEN drives them all through.  It isn't necessarily a problem, but it conflicts with the model of dealing with one file at
     * a time...
     */
    @Test
    public void testBuildingTwoGroovyFiles() {
        //@formatter:off
        String[] sources = {
            "pkg/One.groovy",
            "package pkg;\n" +
            "class One {" +
            "  public static void main(String[]argv) { print \"success\";}\n" +
            "}\n",

            "pkg/Two.groovy",
            "package pkg;\n" +
            "class Two {}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testExtendingGroovyInterfaceWithJava() {
        boolean interfaceDefaultMethod = isParrotParser() && isAtLeastGroovy(50);
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C implements I {" +
            "  public static void main(String[] args) {\n" +
            "    I i = new C();\n" +
            "    System.out.println(i.foo());\n" +
            "  }\n" +
            (!interfaceDefaultMethod ? "@Override public String foo() { return \"foobar\"; }\n" : "") +
            "}\n",

            "p/I.groovy",
            "package p\n" +
            "interface I {\n" + (!interfaceDefaultMethod
            ?
            "  String foo()\n"
            :
            "  default String foo() {\n" +
            "    'foo' + I.this.bar()\n" +
            "  }\n" +
            "  private String bar() {\n" +
            "    'bar'\n" +
            "  }\n") +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "foobar");
    }

    @Test
    public void testExtendingJavaInterfaceWithGroovy() {
        //@formatter:off
        String[] sources = {
            "p/C.groovy",
            "package p\n" +
            "class C implements I {" +
            "  static main(args) {\n" +
            "    I i = new C()\n" +
            "    print i.foo()\n" +
            "  }\n" +
            "}\n",

            "p/I.java",
            "package p;\n" +
            "interface I {\n" +
            "  default String foo() {\n" +
            "    return \"foo\" + bar();\n" +
            "  }\n" +
            "  private String bar() {\n" +
            "    return \"bar\";\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "foobar");
    }

    @Test
    public void testExtendingJavaWithGroovyAndThenJavaAndThenGroovy() {
        //@formatter:off
        String[] sources = {
            "p/D.groovy",
            "package p\n" +
            "class D extends C {\n" +
            "  static main(args) {\n" +
            "    new C()\n" +
            "    print 'success'\n" +
            "  }\n" +
            "}\n",

            "p/C.java",
            "package p;\n" +
            "public class C extends B {}\n",

            "p/B.groovy",
            "package p\n" +
            "class B extends A {}\n",

            "p/A.java",
            "package p;\n" +
            "public class A {}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testImplementingInterface1() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C implements I {\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.println(\"success\");\n" +
            "  }\n" +
            "}\n",

            "p/I.groovy",
            "package p;\n" +
            "public interface I {\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testImplementingInterface2() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C extends groovy.lang.GroovyObjectSupport implements I {\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.println(\"success\");\n" +
            "  }\n" +
            "}\n",

            "p/I.groovy",
            "package p;\n" +
            "public interface I {\n" +
            "  void m();\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\C.java (at line 2)\n" +
            "\tpublic class C extends groovy.lang.GroovyObjectSupport implements I {\n" +
            "\t             ^\n" +
            "The type C must implement the inherited abstract method I.m()\n" +
            "----------\n");
    }

    @Test
    public void testImplementingInterface3() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C extends groovy.lang.GroovyObjectSupport implements I {\n" +
            "  public void m() {}\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.println(\"success\");\n" +
            "  }\n" +
            "}\n",

            "p/I.groovy",
            "package p;\n" +
            "public interface I {\n" +
            "  void m();\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testImplementingInterface4() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C extends groovy.lang.GroovyObjectSupport implements I {\n" +
            "  void m() {}\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.println(\"success\");\n" +
            "  }\n" +
            "}\n",

            "p/I.groovy",
            "package p;\n" +
            "public interface I {\n" +
            "  void m();\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\C.java (at line 3)\n" +
            "\tvoid m() {}\n" +
            "\t     ^^^\n" +
            "Cannot reduce the visibility of the inherited method from I\n" +
            "----------\n");
    }

    @Test
    public void testImplementingInterface5() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C extends groovy.lang.GroovyObjectSupport implements I {\n" +
            "  public String m() { return \"\";}\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.println(\"success\");\n" +
            "  }\n" +
            "}\n",

            "p/I.groovy",
            "package p;\n" +
            "public interface I {\n" +
            "  String m();\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testImplementingInterface6() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C implements Comparator<Foo> {\n" +
            "  int compare(Foo one, Foo two) {\n" +
            "    one.bar <=> two.bar\n" +
            "  }\n" +
            "}\n",

            "Foo.groovy",
            "class Foo { String bar }\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test // GROOVY-5106
    public void testImplementingInterface7() {
        //@formatter:off
        String[] sources = {
            "I.groovy",
            "interface I<T> {\n" +
            "}\n",

            "J.groovy",
            "interface J<T> extends I<T> {\n" +
            "}\n",

            "X.groovy",
            "class X implements I<String>, J<Number> {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. " + (isAtLeastGroovy(40) ? "ERROR" : "WARNING") + " in X.groovy (at line 1)\n" +
            "\tclass X implements I<String>, J<Number> {\n" +
            "\t^\n" +
            "Groovy:The interface I " + (isAtLeastGroovy(40) ? "cannot be" : "is") +
            " implemented more than once with different arguments: I<java.lang.String>" + (isAtLeastGroovy(50) ? " (via X)" : "") +
            " and I<java.lang.Number>" + (isAtLeastGroovy(50) ? " (via J)" : "") + "\n" +
            "----------\n");
    }

    @Test // GROOVY-5106
    public void testImplementingInterface8() {
        //@formatter:off
        String[] sources = {
            "I.groovy",
            "interface I<T> {\n" +
            "}\n",

            "X.groovy",
            "class X implements I<String> {\n" +
            "}\n",

            "Y.groovy",
            "class Y extends X implements I<Number> {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. " + (isAtLeastGroovy(40) ? "ERROR" : "WARNING") + " in Y.groovy (at line 1)\n" +
            "\tclass Y extends X implements I<Number> {\n" +
            "\t^\n" +
            "Groovy:The interface I " + (isAtLeastGroovy(40) ? "cannot be" : "is") +
            " implemented more than once with different arguments: I<java.lang.Number>" + (isAtLeastGroovy(50) ? " (via Y)" : "") +
            " and I<java.lang.String>" + (isAtLeastGroovy(50) ? " (via X)" : "") + "\n" +
            "----------\n");
    }

    @Test // GROOVY-5687
    public void testImplementingInterface9() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import java.text.*\n" +
            "interface DateTimeFormatConstants {\n" +
            "  SimpleDateFormat AM_PM_TIME_FORMAT = new SimpleDateFormat('h:mma', new Locale('en_US'))\n" +
            "  SimpleDateFormat MILITARY_TIME_FORMAT = new SimpleDateFormat('HH:mm')\n" +
            "}\n" +
            "interface DateTimeFormatConstants2 extends DateTimeFormatConstants {\n" +
            "}\n" +
            "class DateTimeUtils implements DateTimeFormatConstants2 {\n" +
            "  static String convertMilitaryTimeToAmPm(String militaryTime) {\n" +
            "    Date date = MILITARY_TIME_FORMAT.parse(militaryTime)\n" +
            "    return AM_PM_TIME_FORMAT.format(date).toLowerCase()\n" +
            "  }\n" +
            "}\n" +
            "print DateTimeUtils.convertMilitaryTimeToAmPm('20:30')\n",
        };
        //@formatter:on

        runConformTest(sources, "8:30pm");
    }

    @Test // GROOVY-8164
    public void testImplementingInterface10() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class C implements p.I, p.J {\n" +
            "}\n" +
            "C.m()\n",

            "p/I.java",
            "package p;\n" +
            "public interface I {\n" +
            "  static String m() {\n" +
            "    return \"I\";\n" +
            "  }\n" +
            "}\n",

            "p/J.java",
            "package p;\n" +
            "public interface J {\n" +
            "  static String m() {\n" +
            "    return \"J\";\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "", "groovy.lang.MissingMethodException: No signature of " + (isAtLeastGroovy(50) ? "static method: m for class: C" : "method: static C.m()") + " is applicable for argument types: () values: []");
    }

    @Test // GROOVY-9259
    public void testImplementingInterface11() {
        assumeTrue(isParrotParser()); // TODO: support default in antlr2 parser?

        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "print new C().m()\n",

            "A.java",
            "public interface A {\n" +
            "  String m();\n" +
            "}\n",

            "B.groovy",
            "interface B extends A {\n" +
            "  default String m() {\n" +
            "    'B'\n" +
            "  }\n" +
            "}\n",

            "C.groovy",
            "class C implements B {\n" +
            "  @Override String m() {\n" +
            "    'C' + " + (isAtLeastGroovy(50) ? "" : "B.") + "super.m()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "CB");
    }

    @Test // GROOVY-8299
    public void testImplementingInterface12() {
        assumeTrue(isParrotParser() && isAtLeastGroovy(50));

        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "print new C().m()\n",

            "A.java",
            "public interface A {\n" +
            "  String m();\n" +
            "}\n",

            "B.groovy",
            "interface B extends A {\n" +
            "  default String m() {\n" +
            "    B.this.pm()\n" +
            "  }\n" +
            "  private String pm() {\n" +
            "    sm()\n" +
            "  }\n" +
            "  static  String sm() {\n" +
            "    'B'\n" +
            "  }\n" +
            "}\n",

            "C.groovy",
            "class C implements B {\n" +
            "  @Override String m() {\n" +
            "    'C' + super.m()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "CB");
    }

    @Test
    public void testImplementingInterface_JavaExtendingGroovyAndImplementingMethod() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C implements I {\n" +
            "  public String m() { return \"\";}\n" +
            "}\n",

            "p/I.groovy",
            "package p;\n" +
            "public interface I {\n" +
            "  List<?> m();\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\C.java (at line 3)\n" +
            "\tpublic String m() { return \"\";}\n" +
            "\t       ^^^^^^\n" +
            "The return type is incompatible with I.m()\n" +
            "----------\n");
    }

    @Test
    public void testImplementingInterface_JavaExtendingGroovyAndImplementingMethod_ArrayReferenceReturnType() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "import java.util.List;\n" +
            "public class C extends groovy.lang.GroovyObjectSupport implements I {\n" +
            "  public String m() { return \"\";}\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.println( \"success\");\n" +
            "  }\n" +
            "}\n",

            "p/I.groovy",
            "package p;\n" +
            "public interface I {\n" +
            "  List[] m();\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\C.java (at line 4)\n" +
            "\tpublic String m() { return \"\";}\n" +
            "\t       ^^^^^^\n" +
            "The return type is incompatible with I.m()\n" +
            "----------\n" +
            // this verifies the position report for the error against the return value of the method
            "----------\n" +
            "1. WARNING in p\\I.groovy (at line 3)\n" +
            "\tList[] m();\n" +
            "\t^^^^\n" +
            "List is a raw type. References to generic type List<E> should be parameterized\n" +
            "----------\n");
    }

    @Test
    public void testImplementingInterface_JavaExtendingGroovyAndImplementingMethod_QualifiedArrayReferenceReturnType() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "import java.util.List;\n" +
            "public class C extends groovy.lang.GroovyObjectSupport implements I {\n" +
            "  public String m() { return \"\";}\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.println( \"success\");\n" +
            "  }\n" +
            "}\n",

            "p/I.groovy",
            "package p;\n" +
            "public interface I {\n" +
            "  java.util.List[] m();\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\C.java (at line 4)\n" +
            "\tpublic String m() { return \"\";}\n" +
            "\t       ^^^^^^\n" +
            "The return type is incompatible with I.m()\n" +
            "----------\n" +
            // this verifies the position report for the error against the return value of the method
            "----------\n" +
            "1. WARNING in p\\I.groovy (at line 3)\n" +
            "\tjava.util.List[] m();\n" +
            "\t^^^^^^^^^^^^^^\n" +
            "List is a raw type. References to generic type List<E> should be parameterized\n" +
            "----------\n");
    }

    @Test
    public void testImplementingInterface_JavaExtendingGroovyAndImplementingMethod_ParamPosition() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "import java.util.List;\n" +
            "public class C extends groovy.lang.GroovyObjectSupport implements I {\n" +
            "  public void m(String s) { }\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.println( \"success\");\n" +
            "  }\n" +
            "}\n",

            "p/I.groovy",
            "package p;\n" +
            "public interface I {\n" +
            "  void m(List l);\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\C.java (at line 3)\n" +
            "\tpublic class C extends groovy.lang.GroovyObjectSupport implements I {\n" +
            "\t             ^\n" +
            "The type C must implement the inherited abstract method I.m(List)\n" +
            "----------\n" +
            // this verifies the position report for the error against the method parameter
            "----------\n" +
            "1. WARNING in p\\I.groovy (at line 3)\n" +
            "\tvoid m(List l);\n" +
            "\t       ^^^^\n" +
            "List is a raw type. References to generic type List<E> should be parameterized\n" +
            "----------\n");
    }

    @Test
    public void testImplementingInterface_JavaExtendingGroovyAndImplementingMethod_QualifiedParamPosition() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "import java.util.List;\n" +
            "public class C extends groovy.lang.GroovyObjectSupport implements I {\n" +
            "  public void m(String s) { }\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.println( \"success\");\n" +
            "  }\n" +
            "}\n",

            "p/I.groovy",
            "package p;\n" +
            "public interface I {\n" +
            "  void m(java.util.List l);\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\C.java (at line 3)\n" +
            "\tpublic class C extends groovy.lang.GroovyObjectSupport implements I {\n" +
            "\t             ^\n" +
            "The type C must implement the inherited abstract method I.m(List)\n" +
            "----------\n" +
            // this verifies the position report for the error against the method parameter+
            "----------\n" +
            "1. WARNING in p\\I.groovy (at line 3)\n" +
            "\tvoid m(java.util.List l);\n" +
            "\t       ^^^^^^^^^^^^^^\n" +
            "List is a raw type. References to generic type List<E> should be parameterized\n" +
            "----------\n");
    }

    @Test
    public void testImplementingInterface_MethodWithParameters_GextendsJ() {
        //@formatter:off
        String[] sources = {
            "p/C.groovy",
            "package p;\n" +
            "public class C implements I<Integer> {\n" +
            "  public void m(String s) { }\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.println( \"success\");\n" +
            "  }\n" +
            "}\n",

            "p/I.java",
            "package p;\n" +
            "public interface I<T extends Number> {\n" +
            "  void m(String s);\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testImplementingInterface_MethodWithParameters_JextendsG() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C extends groovy.lang.GroovyObjectSupport implements I<Integer> {\n" +
            "  public void m(String s) { }\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.println( \"success\");\n" +
            "  }\n" +
            "}\n",

            "p/I.groovy",
            "package p;\n" +
            "public interface I<T extends Number> {\n" +
            "  void m(String s);\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testImplementingInterface_MethodWithParameters2_JextendsG() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C extends groovy.lang.GroovyObjectSupport implements I<Integer> {\n" +
            "  public void m(String s, Integer i) { }\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.println( \"success\");\n" +
            "  }\n" +
            "}\n",

            "p/I.groovy",
            "package p;\n" +
            "public interface I<T extends Number> {\n" +
            "  void m(String s, Integer i);\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testImplementingInterface_MethodWithParameters2_GextendsJ() {
        //@formatter:off
        String[] sources = {
            "p/C.groovy",
            "package p;\n" +
            "public class C implements I<Integer> {\n" +
            "  public void m(String s, Integer i) { return null;}\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.println( \"success\");\n" +
            "  }\n" +
            "}\n",

            "p/I.java",
            "package p;\n" +
            "public interface I<T extends Number> {\n" +
            "  void m(String s, Integer i);\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testImplementingInterface_MethodWithParameters3_GextendsJ() {
        //@formatter:off
        String[] sources = {
            "p/C.groovy",
            "package p;\n" +
            "public class C implements I<Integer> {\n" +
            "  public void m(String s, Integer i) { return null;}\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.println( \"success\");\n" +
            "  }\n" +
            "}\n",

            "p/I.java",
            "package p;\n" +
            "public interface I<T extends Number> {\n" +
            "  void m(String s, T t);\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testImplementingInterface_MethodWithParameters3_JextendsG() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C extends groovy.lang.GroovyObjectSupport implements I<Integer> {\n" +
            "  public void m(String s, Integer i) { }\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.println( \"success\");\n" +
            "  }\n" +
            "}\n",

            "p/I.groovy",
            "package p;\n" +
            "public interface I<T extends Number> {\n" +
            "  void m(String s, T t);\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testCallingJavaFromGroovy1() {
        //@formatter:off
        String[] sources = {
            "p/Main.groovy",
            "package p\n" +
            "class Main {\n" +
            "  static main(args) {\n" +
            "    new J().run()\n" +
            "    print new J().name\n" +
            "  }\n" +
            "}\n",

            "p/J.java",
            "package p;\n" +
            "public class J {\n" +
            "  public String name = \"name\";\n" +
            "  public void run() { System.out.print(\"success\"); }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "successname");
    }

    @Test
    public void testCallingJavaFromGroovy2() {
        //@formatter:off
        String[] sources = {
            "p/Main.groovy",
            "package p\n" +
            "@Tag(value=4)\n" +
            "class Main {\n" +
            "  static main(args) {\n" +
            "    new J().run()\n" +
            "  }\n" +
            "}\n",

            "p/J.java",
            "package p;\n" +
            "public class J {\n" +
            "  public String name = \"name\";\n" +
            "  public void run() { System.out.print(\"success\"); }\n" +
            "}\n",

            "p/Tag.java",
            "package p;\n" +
            "public @interface Tag {\n" +
            "  int value() default 3;\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testCallingMethods_JcallingG() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    new GClass().run();\n" +
            "  }\n" +
            "}\n",

            "p/GClass.groovy",
            "package p;\n" +
            "public class GClass {\n" +
            "  void run() {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testCallingMethods_GcallingJ() {
        //@formatter:off
        String[] sources = {
            "p/C.groovy",
            "package p;\n" +
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    new OtherClass().run();\n" +
            "  }\n" +
            "}\n",

            "p/OtherClass.java",
            "package p;\n" +
            "public class OtherClass {\n" +
            "  void run() {\n" +
            "    System.out.println(\"success\");\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testCallingConstructors_JcallingG() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    OtherClass oClass = new OtherClass();\n" +
            "    System.out.println(\"success\");\n" +
            "  }\n" +
            "}\n",

            "p/OtherClass.groovy",
            "package p;\n" +
            "public class OtherClass {\n" +
            "  public OtherClass() {\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testCallingConstructors_GcallingJ() {
        //@formatter:off
        String[] sources = {
            "p/C.groovy",
            "package p;\n" +
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    OtherClass oClass = new OtherClass();\n" +
            "    System.out.println(\"success\");\n" +
            "  }\n" +
            "}\n",

            "p/OtherClass.java",
            "package p;\n" +
            "public class OtherClass {\n" +
            "  public OtherClass() {\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testReferencingFields_JreferingToG() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    OtherClass oClass = new OtherClass();\n" +
            "    System.out.println(oClass.message);\n" +
            "  }\n" +
            "}\n",

            "p/OtherClass.groovy",
            "package p;\n" +
            "public class OtherClass {\n" +
            "  public String message =\"success\";\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testReferencingFields_GreferingToJ() {
        //@formatter:off
        String[] sources = {
            "p/C.groovy",
            "package p;\n" +
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    OtherClass oClass = new OtherClass();\n" +
            "    System.out.println(oClass.message);\n" +
            "  }\n" +
            "}\n",

            "p/OtherClass.java",
            "package p;\n" +
            "public class OtherClass {\n" +
            "  public String message =\"success\";\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testReferencingFields_DirectAccess1() {
        for (String modifier : new String[] {"public", "protected", "@groovy.transform.PackageScope", "private"}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                "print new C().@field\n",

                "C.groovy",
                "class C {\n" +
                "  " + modifier + " String field = 'value'\n" +
                "}\n",
            };
            //@formatter:on

            runConformTest(sources, "value");
        }
    }

    @Test
    public void testReferencingFields_DirectAccess2() {
        for (String modifier : new String[] {"public", "protected", "@groovy.transform.PackageScope", /*GROOVY-8167: "private"*/}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                "print new C().@field\n",

                "A.groovy",
                "abstract class A {\n" +
                "  " + modifier + " String field = 'value'\n" +
                "}\n",

                "C.groovy",
                "class C extends A {\n" +
                "}\n",
            };
            //@formatter:on

            runConformTest(sources, "value");
        }
    }

    @Test
    public void testReferencingFields_DirectAccess3() {
        for (String modifier : new String[] {"public", "protected", "@groovy.transform.PackageScope", "private"}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                "print new C().@field\n",

                "A.groovy",
                "abstract class A {\n" +
                "  " + modifier + " String field = 'A'\n" +
                "}\n",

                "C.groovy",
                "class C extends A {\n" +
                "  " + modifier + " String field = 'C'\n" +
                "}\n",
            };
            //@formatter:on

            runConformTest(sources, "C");
        }
    }

    @Test
    public void testReferencingFields_DirectAccess4() {
        for (String modifier : new String[] {"public", "protected", "@groovy.transform.PackageScope", /*GROOVY-8167: "private"*/}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                "new C().test()\n",

                "A.groovy",
                "abstract class A {\n" +
                "  " + modifier + " String field = 'A'\n" +
                "}\n",

                "C.groovy",
                "class C extends A {\n" +
                "  void test() {\n" +
                "    print this.@field\n" +
                "  }\n" +
                "}\n",
            };
            //@formatter:on

            runConformTest(sources, "A");
        }
    }

    @Test
    public void testReferencingFields_DirectAccess5() {
        for (String modifier : new String[] {"public", "protected", "@groovy.transform.PackageScope", /*GROOVY-8167: "private"*/}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                "new C().test()\n",

                "A.groovy",
                "abstract class A {\n" +
                "  " + modifier + " String field = 'A'\n" +
                "}\n",

                "C.groovy",
                "class C extends A {\n" +
                "  " + modifier + " String field = 'C'\n" +
                "  void test() {\n" +
                "    print this.@field\n" +
                "  }\n" +
                "}\n",
            };
            //@formatter:on

            runConformTest(sources, "C");
        }
    }

    @Test
    public void testReferencingFields_DirectAccess6() {
        for (String modifier : new String[] {"public", "protected", "@groovy.transform.PackageScope", "private"}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                "new C().test()\n",

                "A.groovy",
                "abstract class A {\n" +
                "  " + modifier + " String field = 'A'\n" +
                "}\n",

                "C.groovy",
                "class C extends A {\n" +
                "  public String field = 'C'\n" +
                "  void test() {\n" +
                "    print super.@field\n" +
                "  }\n" +
                "}\n",
            };
            //@formatter:on

            if (!"private".equals(modifier)) {
                runConformTest(sources, "A");
            } else {
                runConformTest(sources, "", "groovy.lang.MissingFieldException: No such field: field for class: A");
            }
        }
    }

    @Test
    public void testReferencingFields_DirectAccess7() {
        for (String modifier : new String[] {"public", "protected", "@groovy.transform.PackageScope", "private"}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                "new C().with { test(); print result }\n",

                "A.groovy",
                "abstract class A {\n" +
                "  " + modifier + " String field = 'A'\n" +
                "  String getResult() { return field }\n" +
                "}\n",

                "C.groovy",
                "class C extends A {\n" +
                "  public String field = 'C'\n" +
                "  void test() {\n" +
                "    super.@field = 'x'\n" +
                "  }\n" +
                "}\n",
            };
            //@formatter:on

            if (!"private".equals(modifier)) {
                runConformTest(sources, "x");
            } else {
                runConformTest(sources, "", "groovy.lang.MissingFieldException: No such field: field for class: A");
            }
        }
    }

    @Test // GROOVY-8648
    public void testReferencingFields_DirectAccess8() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "new Account().with {\n" +
            "  deposit(42)\n" +
            "  println balance\n" +
            "}\n",

            "Account.groovy",
            "class Account {\n" +
            "  private int balance = 0\n" +
            "  int getBalance() {\n" +
            "    return balance\n" +
            "  }\n" +
            "  void deposit(int amount) {\n" +
            "    assert amount > 0\n" +
            "    this.@balance += amount\n" + // ASM error for LHS attribute expression
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test // GROOVY-6183
    public void testReferencingFields_DirectAccess9() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "C.directAccess()\n" +
            "assert C.x == 2\n" +
            "assert !C.isSetterCalled()\n",

            "A.groovy",
            "abstract class A {\n" +
            "  static boolean setterCalled\n" +
            "  static protected int x\n" +
            "  static void setX(int a) {\n" +
            "    setterCalled = true\n" +
            "    x = a\n" +
            "  }\n" +
            "}\n",

            "C.groovy",
            "class C extends A {\n" +
            "  static void directAccess() {\n" +
            "    this.@x = 2\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test // GROOVY-9982
    public void testReferencingFields_ThisClassPropertyVsSuperClassMethod() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "abstract class A {\n" +
            "  def foo() { 'foo' }\n" +
            "}\n" +
            "class C extends A {\n" +
            "  def foo\n" +
            "  def test() {\n" +
            "    foo = foo()\n" + // not ScriptBytecodeAdapter#invokeClosure
            "    foo\n" +
            "  }\n" +
            "}\n" +
            "print new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "foo");
    }

    @Test
    public void testGroovyObjectsAreGroovyAtCompileTime() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    groovy.lang.GroovyObject oClass = new OtherClass();\n" +
            "    System.out.println(\"success\");\n" +
            "  }\n" +
            "}\n",

            "p/OtherClass.groovy",
            "package p;\n" +
            "class OtherClass {\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testCallGroovyObjectMethods_invokeMethod() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    groovy.lang.GroovyObject oClass = new OtherClass();\n" +
            "    String s = (String)oClass.invokeMethod(\"toString\",null);\n" +
            "    System.out.println(s);\n" +
            "  }\n" +
            "}\n",

            "p/OtherClass.groovy",
            "package p;\n" +
            "class OtherClass {\n" +
            "  String toString() { return \"success\";}\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testSuperCallWithPrivateMethod() {
        //@formatter:off
        String[] sources = {
            "AandC.groovy",
            "abstract class A {\n" +
            "  private x() {\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class C extends A {\n" +
            "  private y() {\n" +
            "    x()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in AandC.groovy (at line 8)\n" +
            "\tx()\n" +
            "\t^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching method C#x()\n" +
            "----------\n");
    }

    @Test
    public void testSuperCallWithStaticMethod() {
        //@formatter:off
        String[] sources = {
            "AandC.groovy",
            "abstract class A {\n" +
            "  protected A(String foo) {\n" +
            "    this.foo = foo\n" +
            "  }\n" +
            "  String foo\n" +
            "}\n" +
            "class C extends A {\n" +
            "  C() {\n" +
            "    super(bar('baz'))\n" +
            "  }\n" +
            "  private static String bar(baz) {\n" +
            "    return 'foobar'\n" +
            "  }\n" +
            "  private static String bar() {\n" +
            "  }\n" +
            "}\n" +
            "print new C().foo\n",
        };
        //@formatter:on

        runConformTest(sources, "foobar");
    }

    @Test
    public void testThisCallInMethod() {
        //@formatter:off
        String[] sources = {
            "T.groovy",
            "public class T {\n" +
            "  def x () {\n" +
            "    this \"\"\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            isParrotParser() ? "" : "----------\n" +
            "1. ERROR in T.groovy (at line 3)\n" +
            "\tthis \"\"\n" +
            "\t     ^^\n" +
            "Groovy:Constructor call must be the first statement in a constructor.\n" +
            "----------\n");
    }

    @Test
    public void testGroovyObjectsAreGroovyAtRunTime() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    OtherClass oClass = new OtherClass();\n" +
            "    System.out.println(oClass instanceof groovy.lang.GroovyObject);\n" +
            "  }\n" +
            "}\n",

            "p/OtherClass.groovy",
            "package p;\n" +
            "class OtherClass {\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "true");
    }

    @Test
    public void testGroovyBug1() {
        //@formatter:off
        String[] sources = {
            "p/A.groovy",
            "package p\n" +
            "public class A<T> { static main(args) { print 'a' } }\n",

            "p/B.groovy",
            "package p\n" +
            "public class B extends A<String> {}",
        };
        //@formatter:on

        runConformTest(sources, "a");
    }

    @Test
    public void testGroovyBug2() {
        //@formatter:off
        String[] sources = {
            "p/B.groovy",
            "package p\n" +
            "public class B extends A<String> { static main(args) { print 'a' } }",

            "p/A.groovy",
            "package p\n" +
            "public class A<T> { }\n",
        };
        //@formatter:on

        runConformTest(sources, "a");
    }

    @Test
    public void testGroovyBug3() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "static void test() {\n" +
            "  print 'A'.with { \"${'toLowerCase'}\"() }\n" +
            "}\n" +
            "test()\n",
        };
        //@formatter:on

        runConformTest(sources, "a");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-1736
    public void testGroovy1736() {
        for (String vis : new String[] {"", "public", "protected", "@groovy.transform.PackageScope"}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                "abstract class A {\n" +
                vis + " def getX() { 'A' }\n" +
                "}\n" +
                "class C extends A {\n" +
                "  def getX() { super.x + 'C' }\n" + // no stack overflow
                "  void m() {\n" +
                "    print x\n" +
                "    print this.x\n" +
                "    print super.x\n" + // TODO: test safe and spread
                "  }\n" +
                "}\n" +
                "new C().m()\n",
            };
            //@formatter:on

            runConformTest(sources, "ACACA");
        }
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-3311
    public void testGroovy3311() {
        //@formatter:off
        String[] sources = {
            "Day.groovy",
            "class Day extends Date {\n" +
            "  static main(args) {\n" +
            "    print period\n" +
            "  }\n" +
            "  static Day get(_date) {\n" +
            "    return new Day(new java.text.SimpleDateFormat('MM.dd.yyyy').parse(_date))\n" +
            "  }\n" +
            "  Day(Date _date) {\n" +
            "    super(_date.time)\n" +
//            "    def time = getTime()\n" +
//            "    24.times { hour ->\n" +
//            "      hoursOfTheDay << new Date(time + hour*1000*60*60)\n" +
//            "    }\n" +
            "  }\n" +
//            "  List<Date> hoursOfTheDay = []\n" +
            "  \n" +
            "  @Override String toString() {\n" +
            "    new java.text.SimpleDateFormat('MM.dd.yyyy').format(this)\n" +
            "  }\n" +
            "  \n" +
            "  static def period = (1..3).collect { get \"12.3${it}.1999\" }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "[12.31.1999, 01.01.2000, 01.02.2000]");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-6045
    public void testGroovy6045() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  static main(args) { new Main() }\n" +
            "  String toString() { super?.toString() }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-6097
    public void testGroovy6097() {
        for (String vis : new String[] {"", "public", "protected", "@groovy.transform.PackageScope"}) {
            //@formatter:off
            String[] sources = {
                "Main.groovy",
                "abstract class A {\n" +
                vis + " boolean isX() { true }\n" +
                vis + " boolean getX() { false }\n" +
                "}\n" +
                "class C extends A {\n" +
                "  void m() {\n" +
                "    print x\n" +
                "    print this.x\n" +
                "    print super.x\n" + // hardwired to "super.getX()"
                "  }\n" +
                "}\n" +
                "new C().m()\n",
            };
            //@formatter:on

            runConformTest(sources, "truetrue" + (isAtLeastGroovy(40) ? "true" : "false"));
        }
    }

    @Test(expected = AssertionError.class) // https://issues.apache.org/jira/browse/GROOVY-7844
    public void testGroovy7844() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "print(Pojo.m('foo'))\n",

            "Pojo.java",
            "class Pojo {\n" +
            "  public  static String m(Object o) {\n" +
            "    return o.toString();\n" +
            "  }\n" +
            "  private static String m(String s) {\n" +
            "    return org.codehaus.groovy.runtime.StringGroovyMethods.reverse(s);\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "foo");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-7924
    public void testGroovy7924() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "abstract class A {\n" +
            " def getFoo() { 'works' }\n" +
            "}\n" +
            "class C extends A {\n" +
            "  void m(String name) {\n" +
            "    print super.\"$name\"\n" +
            "  }\n" +
            "}\n" +
            "new C().m('foo')\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-8164
    public void testGroovy8164() {
        assumeTrue(isAtLeastGroovy(40));

        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C implements I, J {\n" +
            "}\n" +
            "C.m()\n", // not inherited -- see JLS 8.4.8

            "I.java",
            "public interface I {\n" +
            "  static void m() {\n" +
            "    System.out.print(\"I\");\n" +
            "  }\n" +
            "}\n",

            "J.java",
            "public interface J {\n" +
            "  static void m() {\n" +
            "    System.out.print(\"J\");\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "", "groovy.lang.MissingMethodException: No signature of " + (isAtLeastGroovy(50) ? "static method: m for class: C" : "method: static C.m()") + " is applicable for argument types: () values: []");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-8311
    public void testGroovy8311() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "def greet(args) {\n" +
            "  [args.name, args.age]\n" +
            "}\n" +
            "def name = 'age'\n" +
            "assert greet(name: 'Frank Grimes', (name): 42) == ['Frank Grimes', 42]\n",
        };
        //@formatter:on

        runConformTest(sources, "");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-9336
    public void testGroovy9336() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  public static final double CONST = 2 << 16 - 1\n" +
            "  static main(args) {\n" +
            "    print CONST\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "65536.0");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-9391
    public void testGroovy9391() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class A { def m() {} }\n" +
            "class B extends A {  }\n" +
            "class C extends B {\n" +
            "  def m() {\n" +
            "    ((A) super).m()\n" + // makes no sense
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 5)\n" +
            "\t((A) super).m()\n" +
            "\t ^^^^^^^^^\n" +
            "Groovy:Cannot cast or coerce `super`\n" +
            "----------\n");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-9857
    public void testGroovy9857() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "abstract class A {\n" +
            "  A(boolean b) { }\n" +
            "}\n" +
            "class C extends A {}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 4)\n" +
            "\tclass C extends A {}\n" +
            "\t      ^\n" +
            "Groovy:Implicit super constructor A() is undefined for generated constructor. Must define an explicit constructor.\n" +
            "----------\n");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-9906
    public void testGroovy9906() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "abstract class A {\n" +
            "  void m(String a, String b, String c) {}\n" +
            "  void m(String a, String... zeroPlus) {}\n" +
            "}\n" +
            "class C extends A {\n" +
            "  void test() {\n" +
            "    m('x', 'y', 'z')\n" + // ArrayIndexOutOfBoundsException: Index 2 out of bounds for length 2
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-9966
    public void testGroovy9966() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "import static java.util.Arrays.*\n" +
            "def m(x) { return x }\n" +
            "final value = 123.456\n" +
            "m(\"$value\").missing\n",
        };
        //@formatter:on

        runConformTest(sources, "", "groovy.lang.MissingPropertyException: No such property: missing for class: org.codehaus.groovy.runtime.GStringImpl");
    }

    // was worried <clinit> would surface in list of methods used to build the type declaration, but that doesn't appear to be the case
    @Test
    public void testExtendingGroovyObjects_clinit() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    OtherClass oClass = new OtherClass();\n" +
            "    System.out.println(\"success\");\n" +
            "  }\n" +
            "}\n",

            "p/OtherClass.groovy",
            "package p;\n" +
            "public class OtherClass {\n" +
            "  { int i = 5; }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testGroovyPropertyAccessors1() {
        //@formatter:off
        String[] sources = {
            "C.java",
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    G pogo = new G();\n" +
            "    System.out.print(pogo.isB());\n" +
            "    System.out.print(pogo.getB());\n" +
            "  }\n" +
            "}\n",

            "G.groovy",
            "class G {\n" +
            "  boolean b\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "falsefalse");
    }

    @Test
    public void testGroovyPropertyAccessors2() {
        //@formatter:off
        String[] sources = {
            "C.java",
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    G pogo = new G();\n" +
            "    System.out.print(pogo.getB());\n" +
            "    pogo.setB(true);\n" +
            "    System.out.print(pogo.getB());\n" +
            "  }\n" +
            "}\n",

            "G.groovy",
            "class G {\n" +
            "  boolean b\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "falsetrue");
    }

    @Test
    public void testGroovyPropertyAccessors3() {
        //@formatter:off
        String[] sources = {
            "C.java",
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    G pogo = new G();\n" +
            "    System.out.print(pogo.isB());\n" +
            "    System.out.print(pogo.getB());\n" +
            "  }\n" +
            "}\n",

            "G.groovy",
            "class G {\n" +
            "  boolean b\n" +
            "  boolean isB() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in C.java (at line 5)\n" +
            "\tSystem.out.print(pogo.getB());\n" +
            "\t                      ^^^^\n" +
            "The method getB() is undefined for the type G\n" +
            "----------\n");
    }

    @Test
    public void testGroovyPropertyAccessors4() {
        //@formatter:off
        String[] sources = {
            "C.java",
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    G pogo = new G();\n" +
            "    System.out.print(pogo.isB());\n" +
            "    System.out.print(pogo.getB());\n" +
            "  }\n" +
            "}\n",

            "G.groovy",
            "class G {\n" +
            "  boolean b\n" +
            "  boolean getB() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in C.java (at line 4)\n" +
            "\tSystem.out.print(pogo.isB());\n" +
            "\t                      ^^^\n" +
            "The method isB() is undefined for the type G\n" +
            "----------\n");
    }

    @Test // @Deprecated should be propagated to accessors
    public void testGroovyPropertyAccessors5() {
        //@formatter:off
        String[] sources = {
            "G.groovy",
            "class G {\n" +
            "  @Deprecated\n" +
            "  boolean flag\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        checkDisassemblyFor("G.class",
            "  @java.lang.Deprecated\n" +
            "  private boolean flag;\n");

        checkDisassemblyFor("G.class",
            "  @java.lang.Deprecated\n  @groovy.transform.Generated\n" +
            "  public boolean isFlag();\n");

        checkDisassemblyFor("G.class",
            "  @java.lang.Deprecated\n  @groovy.transform.Generated\n" +
            "  public boolean getFlag();\n");

        checkDisassemblyFor("G.class",
            "  @java.lang.Deprecated\n  @groovy.transform.Generated\n" +
            "  public void setFlag(boolean arg0);\n");
    }

    @Test
    public void testGroovyPropertyAccessors6() {
        //@formatter:off
        String[] sources = {
            "G.groovy",
            "class G {\n" +
            "  def p = 'G'\n" +
            "  void test() {\n" +
            "    [p:'map'].with {\n" +
            "      print getP()\n" + // resolves to enclosing class not map
            "    }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    new G().test()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "G");
    }

    @Test
    public void testGroovyPropertyAccessors7() {
        //@formatter:off
        String[] sources = {
            "G.groovy",
            "class G {\n" +
            "  def p = 'G'\n" +
            "  void test() {\n" +
            "    [p:'map'].with {\n" +
            "      print p\n" + // resolves to map not enclosing class
            "    }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    new G().test()\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "map");
    }

    @Test // check no duplicate created for 'String getProp'
    public void testGroovyPropertyAccessors_ErrorCases1() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    G o = new G();\n" +
            "    System.out.print(o.getProp());\n" +
            "  }\n" +
            "}\n",

            "p/G.groovy",
            "package p;\n" +
            "public class G {\n" +
            "  String prop = 'foo'\n" +
            "  String getProp() { return prop; }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "foo");
    }

    @Test // check no duplicate created for 'boolean isProp'
    public void testGroovyPropertyAccessors_ErrorCases2() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    G o = new G();\n" +
            "    System.out.print(o.isProp());\n" +
            "  }\n" +
            "}\n",

            "p/G.groovy",
            "package p;\n" +
            "public class G {\n" +
            "  boolean prop = false\n" +
            "  boolean isProp() { return prop; }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "false");
    }

    @Test // although there is a getProp already defined, it takes a parameter so a new one should still be generated
    public void testGroovyPropertyAccessors_ErrorCases3() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    G o = new G();\n" +
            "    System.out.print(o.getProp());\n" +
            "  }\n" +
            "}\n",

            "p/G.groovy",
            "package p;\n" +
            "public class G {\n" +
            "  String prop = 'foo'\n" +
            "  String getProp(String s) { return prop; }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "foo");
    }

    @Test // although there is a setProp already defined, it takes no parameters so a new one should still be generated
    public void testGroovyPropertyAccessors_ErrorCases4() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    G o = new G();\n" +
            "    o.setProp(\"abc\");\n" +
            "    System.out.print(\"abc\");\n" +
            "  }\n" +
            "}\n",

            "p/G.groovy",
            "package p;\n" +
            "public class G {\n" +
            "  String prop = 'foo'\n" +
            "  void setProp() { }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "abc");
    }

    @Test
    public void testGroovyPropertyAccessors_ErrorCases5() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    G o = new G();\n" +
            "    o.setProp(new H());\n" +
            "    System.out.print(\"abc\");\n" +
            "  }\n" +
            "}\n",

            "p/H.java",
            "package p;\n" +
            "class H{}\n",

            "p/J.java",
            "package p;\n" +
            "class J{}\n",

            "p/G.groovy",
            "package p;\n" +
            "public class G {\n" +
            "  H prop\n" +
            "  void setProp(J b) { }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\C.java (at line 5)\n" +
            "\to.setProp(new H());\n" +
            "\t  ^^^^^^^\n" +
            "The method setProp(J) in the type G is not applicable for the arguments (H)\n" +
            "----------\n");
    }

    @Test
    public void testGroovyPropertyAccessors_ErrorCases6() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    G o = new G();\n" +
            "    o.setProp(\"abc\");\n" +
            "    System.out.print(\"abc\");\n" +
            "  }\n" +
            "}\n",

            "p/G.groovy",
            "package p;\n" +
            "public class G {\n" +
            "  String prop = 'foo'\n" +
            "  void setProp(boolean b) { }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\C.java (at line 5)\n" +
            "\to.setProp(\"abc\");\n" +
            "\t  ^^^^^^^\n" +
            "The method setProp(boolean) in the type G is not applicable for the arguments (String)\n" +
            "----------\n");
    }

    @Test
    public void testDefaultValueMethods1() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    G o = new G();\n" +
            "    o.m(\"abc\",3);\n" +
            "    o.m(\"abc\");\n" +
            "  }\n" +
            "}\n",

            "p/G.groovy",
            "package p;\n" +
            "public class G {\n" +
            "  public void m(String s,Integer i=3) { print s }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "abcabc");

        checkGCUDeclaration("G.groovy",
            "package p;\n" +
            "public class G {\n" +
            "  public @groovy.transform.Generated G() {\n" +
            "  }\n" +
            "  public void m(String s, Integer i) {\n" +
            "  }\n" +
            "  public @groovy.transform.Generated void m(String s) {\n" +
            "  }\n" +
            "}\n");

        checkDisassemblyFor("p/G.class",
            "  \n" +
            "  public void m(String s, Integer i);\n" +
            "  \n",
            ClassFileBytesDisassembler.COMPACT);

        checkDisassemblyFor("p/G.class",
            "  \n" +
            "  public void m(String s);\n" +
            "  \n",
            ClassFileBytesDisassembler.COMPACT);
    }

    @Test
    public void testDefaultValueMethods2() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    G o = new G();\n" +
            "    String str=\"xyz\";\n" +
            "    o.m(str,1,str,str,4.0f,str);\n" +
            "    o.m(str,1,str,str,str);\n" +
            "    o.m(str,1,str,str);\n" +
            "    o.m(str,str,str);\n" +
            "  }\n" +
            "}\n",

            "p/G.groovy",
            "package p;\n" +
            "public class G {\n" +
            "  public void m(String s, Integer i=3, String j=\"abc\", String k, float f = 3.0f, String l) { print s+f }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "xyz4.0xyz3.0xyz3.0xyz3.0");

        checkGCUDeclaration("G.groovy",
            "package p;\n" +
            "public class G {\n" +
            "  public @groovy.transform.Generated G() {\n" +
            "  }\n" +
            "  public void m(String s, Integer i, String j, String k, float f, String l) {\n" +
            "  }\n" +
            "  public @groovy.transform.Generated void m(String s, Integer i, String j, String k, String l) {\n" +
            "  }\n" +
            "  public @groovy.transform.Generated void m(String s, Integer i, String k, String l) {\n" +
            "  }\n" +
            "  public @groovy.transform.Generated void m(String s, String k, String l) {\n" +
            "  }\n" +
            "}\n");

        checkDisassemblyFor("p/G.class",
            "  \n" +
            "  public void m(String s, Integer i, String j, String k, float f, String l);\n" +
            "  \n",
            ClassFileBytesDisassembler.COMPACT);

        checkDisassemblyFor("p/G.class",
            "  \n" +
            "  public void m(String s, Integer i, String j, String k, String l);\n" +
            "  \n",
            ClassFileBytesDisassembler.COMPACT);

        checkDisassemblyFor("p/G.class",
            "  \n" +
            "  public void m(String s, Integer i, String k, String l);\n" +
            "  \n",
            ClassFileBytesDisassembler.COMPACT);

        checkDisassemblyFor("p/G.class",
            "  \n" +
            "  public void m(String s, String k, String l);\n" +
            "  \n",
            ClassFileBytesDisassembler.COMPACT);
    }

    @Test
    public void testDefaultValueConstructors1() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    G o = new G(2,\"abc\");\n" +
            "    o.print();\n" +
            "    o = new G(3);\n" +
            "    o.print();\n" +
            "  }\n" +
            "}\n",

            "p/G.groovy",
            "package p;\n" +
            "public class G {\n" +
            "  def msg\n" +
            "  public G(Integer i, String m=\"abc\") {this.msg = m;}\n" +
            "  public void print(int i=3) { print msg }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "abcabc");

        checkGCUDeclaration("G.groovy",
            "package p;\n" +
            "public class G {\n" +
            "  private java.lang.Object msg;\n" +
            "  public G(Integer i, String m) {\n" +
            "  }\n" +
            "  public @groovy.transform.Generated G(Integer i) {\n" +
            "  }\n" +
            "  public void print(int i) {\n" +
            "  }\n" +
            "  public @groovy.transform.Generated void print() {\n" +
            "  }\n" +
            "}\n");

        checkDisassemblyFor("p/G.class",
            "  \n" +
            "  public G(Integer i, String m);\n" +
            "  \n",
            ClassFileBytesDisassembler.COMPACT);

        checkDisassemblyFor("p/G.class",
            "  \n" +
            "  public G(Integer i);\n" +
            "  \n",
            ClassFileBytesDisassembler.COMPACT);
    }

    @Test
    public void testDefaultValueConstructors2() {
        //@formatter:off
        String[] sources = {
            "p/C.java",
            "package p;\n" +
            "public class C {\n" +
            "  public static void main(String[] argv) {\n" +
            "    G o = new G(2,\"abc\");\n" +
            "    o.print();\n" +
            "    o = new G(3);\n" +
            "    o.print();\n" +
            "  }\n" +
            "}\n",

            "p/G.groovy",
            "package p\n" +
            "class G {\n" +
            "  def foo\n" +
            "  G(Integer i) { foo = 'bar' }\n" +
            "  G(Integer i, String s='baz') { foo = s }\n" +
            "  void print(int i=3) { print(foo) }\n" +
            "}\n",
        };
        //@formatter:on

        if (isAtLeastGroovy(40)) {
            runConformTest(sources, "abcbar");
        } else {
            var descriptor = "public void <init>(java.lang.Integer i)  { ... " + "}"; // TODO
            runNegativeTest(sources,
                "----------\n" +
                "1. ERROR in p\\G.groovy (at line 2)\n" +
                "\tclass G {\n" +
                "\t^\n" +
                "Groovy:The constructor " + descriptor + " duplicates another constructor of the same signature\n" +
                "----------\n");
        }
    }

    @Test
    public void testClashingMethodsWithDefaultParams() {
        //@formatter:off
        String[] sources = {
            "p/Code.groovy",
            "package p;\n" +
            "\n" +
            "class Code {\n" +
            "  public void m(String s) {}\n" +
            "  public void m(String s, Integer i=3) {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\Code.groovy (at line 5)\n" +
            "\tpublic void m(String s, Integer i=3) {}\n" +
            "\t^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:The method with default parameters \"void m(java.lang.String, java.lang.Integer)\"" +
            " defines a method \"void m(java.lang.String)\" that is already defined.\n" +
            "----------\n");
    }

    @Test
    public void testTypeVariableBoundIsRawType() {
        //@formatter:off
        String[] sources = {
            "p/Foo.groovy",
            "package p;\n" +
            "public class Foo extends Supertype {\n" +
            "  public static void main(String[] argv) {\n" +
            "    System.out.print(\"success\");\n" +
            "  }\n" +
            "}\n",

            "p/Supertype.java",
            "package p;\n" +
            "class Supertype<T extends Supertype2> { }",

            "p/Supertype2.java",
            "package p;\n" +
            "class Supertype2<T> { }",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testDuplicateLocalVariables1() {
        //@formatter:off
        String[] sources = {
            "p/Foo.groovy",
            "package p\n" +
            "class Foo {\n" +
            "  def bar() {\n" +
            "    if (condition()) {\n" +
            "      def baz = 1\n" +
            "    } else {\n" +
            "      def baz = 2\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testDuplicateLocalVariables2() {
        //@formatter:off
        String[] sources = {
            "p/Foo.groovy",
            "package p\n" +
            "class Foo {\n" +
            "  def bar() {\n" +
            "    if (condition()) {\n" +
            "      def baz = 1\n" +
            "    }\n" +
            "    def block = { ->\n" +
            "      def baz = 2\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testDuplicateLocalVariables3() {
        //@formatter:off
        String[] sources = {
            "p/Foo.groovy",
            "package p\n" +
            "class Foo {\n" +
            "  def bar() {\n" +
            "    switch (something()) {\n" +
            "    case 'A':\n" +
            "      def baz = 1\n" +
            "    case 'B':\n" +
            "      def baz = 2\n" +
            "    }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    // Type already implements invokeMethod(String,Object) - should not be an error, just don't add the method
    @Test
    public void testDuplicateGroovyObjectMethods1() {
        //@formatter:off
        String[] sources = {
            "p/Foo.groovy",
            "package p;\n" +
            "public class Foo /*extends Supertype<Goo>*/ {\n" +
            " public Object invokeMethod(String s, Object o) {\n" +
            " return o;}\n" +
            "  public static void main(String[] argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testDuplicateGroovyObjectMethods2() {
        //@formatter:off
        String[] sources = {
            "p/Foo.groovy",
            "package p;\n" +
            "public class Foo /*extends Supertype<Goo>*/ {\n" +
            "  public MetaClass getMetaClass() {return null;}\n" +
            "  public void setMetaClass(MetaClass mc) {}\n" +
            "  public Object getProperty(String propertyName) {return null;}\n" +
            "  public void setProperty(String propertyName,Object newValue) {}\n" +
            "  public static void main(String[] argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testTwoTopLevelTypesInAFile() {
        //@formatter:off
        String[] sources = {
            "p/First.groovy",
            "package p;\n" +
            "public class First {\n" +
            "  public static void main(String[] argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n" +
            "class Second {\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    @Test
    public void testMultipleTypesInOneFile1() {
        //@formatter:off
        String[] sources = {
            "p/Foo.groovy",
            "package p;\n" +
            "class Foo {\n" +
            "  public static void main(String[] argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n" +
            "class Goo {}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    // Refering to the secondary type from the primary (but internally to a method)
    @Test
    public void testMultipleTypesInOneFile2() {
        //@formatter:off
        String[] sources = {
            "p/Foo.groovy",
            "package p;\n" +
            "class Foo {\n" +
            "  public static void main(String[] argv) {\n" +
            "    new Goo();\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n" +
            "class Goo {}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    // Refering to the secondary type from the primary - from a method param
    @Test
    public void testMultipleTypesInOneFile3() {
        //@formatter:off
        String[] sources = {
            "p/Foo.groovy",
            "package p;\n" +
            "class Foo {\n" +
            "  public static void main(String[] argv) {\n" +
            "    new Foo().runnit(new Goo());\n" +
            "  }\n" +
            "  public void runnit(Goo g) {" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n" +
            "class Goo {}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    // Refering to the secondary type from the primary - from a method
    @Test
    public void testJDKClasses() {
        //@formatter:off
        String[] sources = {
            "p/Foo.groovy",
            "package p;\n" +
            "class Foo {\n" +
            "  public static void main(String[] argv) {\n" +
            "    new Foo().runnit(new Goo());\n" +
            "  }\n" +
            "  public void runnit(Goo g) {" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n" +
            "class Goo {}\n",
        };
        //@formatter:on

        runConformTest(sources, "success");
    }

    // Test the visibility of a package-private source type from another package
    @Test
    public void testVisibility() {
        //@formatter:off
        String[] sources = {
            "p/Foo.groovy",
            "package p\n" +
            "import q.Bar\n" +
            "class Foo {\n" +
            "  static main(args) {\n" +
            "    new Foo().getBar()\n" +
            "    print 'success'\n" +
            "  }\n" +
            "  Bar getBar() {new Bar()}\n" +
            "}\n",

            "q/Bar.java",
            "package q;\n" +
            "class Bar {}\n",
        };
        //@formatter:on

        String old = System.getProperty("groovy.target.indy");
        try {
            System.setProperty("groovy.target.indy", "false");

            String fail = Runtime.version().feature() > 8 ? "failed" : "tried";
            runConformTest(sources, "", "java.lang.IllegalAccessError: " + fail + " to access class q.Bar from class p.Foo");
        } finally {
            if (old == null) {
                System.clearProperty("groovy.target.indy");
            } else {
                System.setProperty("groovy.target.indy", old);
            }
        }
    }

    @Test
    public void testConfigScriptWithError() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "withConfig(configuration) {\n" +
            "  X\n" +
            "}\n"
        ).getAbsolutePath());

        //@formatter:off
        String[] sources = {
            "hello.groovy",
            "println 'hello'",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. WARNING in hello.groovy (at line 1)\n" +
            "\tprintln 'hello'\n" +
            "\t^\n" +
            "Cannot read the source from ##" + File.separator + "config.groovy due to internal exception" +
            " groovy.lang.MissingPropertyException: No such property: X for class: config\n" +
            "----------\n",
            options);
    }

    @Test
    public void testConfigScriptPrecedence() {
        Map<String, String> options = getCompilerOptions();
        options.put(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, createScript("config.groovy",
            "configuration.optimizationOptions.indy = false\n"
        ).getAbsolutePath());
        options.put(CompilerOptions.OPTIONG_GroovyFlags, Integer.toString(CompilerOptions.InvokeDynamic));

        //@formatter:off
        String[] sources = {
            "A.groovy",
            "class A {\n" +
            "  def x\n" +
            "}\n",

            "B.groovy",
            "class B extends A {\n" +
            "  void test() {\n" +
            "    x" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "", options);

        checkDisassemblyFor("B.class",
            "  invokeinterface org.codehaus.groovy.runtime.callsite.CallSite.callGroovyObjectGetProperty");
    }

    @Test // Variable arguments
    public void testInvokingVarargs01_JtoG() {
        //@formatter:off
        String[] sources = {
            "p/Run.java",
            "package p;\n" +
            "public class Run {\n" +
            "  public static void main(String[] argv) {\n" +
            "    X x = new X();\n" +
            "    x.callit();\n" +
            "    x.callit(1);\n" +
            "    x.callit(1,2);\n" +
            "    x.callit2();\n" +
            "    x.callit2(1);\n" +
            "    x.callit2(1,2);\n" +
            "    x.callit3();\n" +
            "    x.callit3(\"abc\");\n" +
            "    x.callit3(\"abc\",\"abc\");\n" +
            "  }\n" +
            "}\n",

            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  public void callit(int... is) { print is.length; }\n" +
            "  public void callit2(Integer... is) { print is.length; }\n" +
            "  public void callit3(String... ss) { print ss.length; }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "012012012");
    }

    @Test
    public void testInvokingVarargs01_GtoJ() {
        //@formatter:off
        String[] sources = {
            "p/Run.groovy",
            "package p;\n" +
            "public class Run {\n" +
            "  public static void main(String[] argv) {\n" +
            "    X x = new X();\n" +
            "    x.callit('abc');\n" +
            "    x.callit('abc',1);\n" +
            "    x.callit('abc',1,2);\n" +
            "    x.callit2(3);\n" +
            "    x.callit2(4,1);\n" +
            "    x.callit2(1,1,2);\n" +
            "    x.callit3('abc');\n" +
            "    x.callit3('abc',\"abc\");\n" +
            "    x.callit3('abc',\"abc\",\"abc\");\n" +
            "  }\n" +
            "}\n",

            "p/X.java",
            "package p;\n" +
            "public class X {\n" +
            "  public void callit(String a, int... is) { System.out.print(is.length); }\n" +
            "  public void callit2(int a, Integer... is) { System.out.print(is.length); }\n" +
            "  public void callit3(String s, String... ss) { System.out.print(ss.length); }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "012012012");
    }

    @Test // In these two cases the methods also take other values
    public void testInvokingVarargs02_JtoG() {
        //@formatter:off
        String[] sources = {
            "p/Run.java",
            "package p;\n" +
            "public class Run {\n" +
            "  public static void main(String[] argv) {\n" +
            "    X x = new X();\n" +
            "    x.callit(\"abc\");\n" +
            "    x.callit(\"abc\",1);\n" +
            "    x.callit(\"abc\",1,2);\n" +
            "    x.callit2(3);\n" +
            "    x.callit2(4,1);\n" +
            "    x.callit2(1,1,2);\n" +
            "    x.callit3(\"abc\");\n" +
            "    x.callit3(\"abc\",\"abc\");\n" +
            "    x.callit3(\"abc\",\"abc\",\"abc\");\n" +
            "  }\n" +
            "}\n",

            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  public void callit(String a, int... is) { print is.length; }\n" +
            "  public void callit2(int a, Integer... is) { print is.length; }\n" +
            "  public void callit3(String s, String... ss) { print ss.length; }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "012012012");
    }

    @Test // Groovy doesn't care about '...' and will consider [] as varargs
    public void testInvokingVarargs03_JtoG() {
        //@formatter:off
        String[] sources = {
            "p/Run.java",
            "package p;\n" +
            "public class Run {\n" +
            "  public static void main(String[] argv) {\n" +
            "    X x = new X();\n" +
            "    x.callit(\"abc\");\n" +
            "    x.callit(\"abc\",1);\n" +
            "    x.callit(\"abc\",1,2);\n" +
            "    x.callit2(3);\n" +
            "    x.callit2(4,1);\n" +
            "    x.callit2(1,1,2);\n" +
            "    x.callit3(\"abc\");\n" +
            "    x.callit3(\"abc\",\"abc\");\n" +
            "    x.callit3(\"abc\",\"abc\",\"abc\");\n" +
            "  }\n" +
            "}\n",

            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  public void callit(String a, int[] is) { print is.length; }\n" +
            "  public void callit2(int a, Integer[] is) { print is.length; }\n" +
            "  public void callit3(String s, String[] ss) { print ss.length; }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "012012012");
    }

    @Test
    public void testInvokingVarargs01_GtoJ_GRE925() {
        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String[] args) {}\n" +
            "  protected void method(String[] x) {}\n" +
            "}\n",

            "Sub.groovy",
            "class Sub extends Main {\n" +
            "  protected void method(String[] x) { super.method(x) }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testInvokingVarargs02_GtoJ() {
        //@formatter:off
        String[] sources = {
            "p/Run.groovy",
            "package p;\n" +
            "public class Run {\n" +
            "  public static void main(String[] argv) {\n" +
            "    X x = new X();\n" +
            "    x.callit();\n" +
            "    x.callit(1);\n" +
            "    x.callit(1,2);\n" +
            "    x.callit2();\n" +
            "    x.callit2(1);\n" +
            "    x.callit2(1,2);\n" +
            "    x.callit3();\n" +
            "    x.callit3(\"abc\");\n" +
            "    x.callit3(\"abc\",\"abc\");\n" +
            "  }\n" +
            "}\n",

            "p/X.java",
            "package p;\n" +
            "public class X {\n" +
            "  public void callit(int... is) { System.out.print(is.length); }\n" +
            "  public void callit2(Integer... is) { System.out.print(is.length); }\n" +
            "  public void callit3(String... ss) { System.out.print(ss.length); }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "012012012");
    }

    @Test
    public void testInvokingVarargsCtors01_JtoG() {
        //@formatter:off
        String[] sources = {
            "p/Run.java",
            "package p;\n" +
            "public class Run {\n" +
            "  public static void main(String[] argv) {\n" +
            "    X x = null;\n" +
            "    x = new X();\n" +
            "    x = new X(1);\n" +
            "    x = new X(1,2);\n" +
            "    x = new X(\"abc\");\n" +
            "    x = new X(\"abc\",1);\n" +
            "    x = new X(\"abc\",1,2);\n" +
            "  }\n" +
            "}\n",

            "p/X.groovy",
            "package p;\n" +
            "public class X {\n" +
            "  public X(int... is) { print is.length; }\n" +
            "  public X(String s, int... is) { print is.length; }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "012012");
    }

    @Test
    public void testInvokingVarargsCtors01_GtoJ() {
        //@formatter:off
        String[] sources = {
            "p/Run.groovy",
            "package p;\n" +
            "public class Run {\n" +
            "  public static void main(String[] argv) {\n" +
            "    X x = null;\n" +
            "    x = new X();\n" +
            "    x = new X(1);\n" +
            "    x = new X(1,2);\n" +
            "    x = new X(\"abc\");\n" +
            "    x = new X(\"abc\",1);\n" +
            "    x = new X(\"abc\",1,2);\n" +
            "  }\n" +
            "}\n",

            "p/X.java",
            "package p;\n" +
            "public class X {\n" +
            "  public X(int... is) { System.out.print(is.length); }\n" +
            "  public X(String s, int... is) { System.out.print(is.length); }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "012012");
    }

    @Test // GROOVY-9851
    public void testSuperDotPrivateMethod() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "abstract class A {\n" +
            "  private void m() {\n" +
            "    print 'works'\n" +
            "  }\n" +
            "}\n" +
            "class C extends A {\n" +
            "  void test() {\n" +
            "    super.m()\n" + // private method!
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        if (!isAtLeastGroovy(40)) {
            runConformTest(sources, "works");
        } else {
            String signature = isAtLeastGroovy(50) ? "m for class: A" : "A.m()";
            runConformTest(sources, "", "groovy.lang.MissingMethodException: No signature of method: " + signature + " is applicable for argument types: () values: []");
        }
    }

    @Test // GROOVY-10747
    public void testSuperDotClone() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class C implements Cloneable {\n" +
            "  C clone() {\n" +
            "    super.clone()\n" +
            "  }\n" +
            "}\n" +
            "def pogo = new C()\n" +
            "def copy = pogo.clone()\n" +
            "assert !copy.is( pogo )\n" +
            "assert copy instanceof C\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test // GROOVY-10733
    public void testArrayDotClone() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "int[] numbers = [1,2,3]\n" +
            "assert numbers.clone() == [1,2,3]\n" +
            "assert numbers.clone() instanceof int[]\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testPositions1() {
        //@formatter:off
        String[] sources = {
            "One.groovy",
            "class One {\n" +
            "\t\t/*a*/\t\t\tStack plates;\n" +
            "  /*b*/ Stack plates2;\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. WARNING in One.groovy (at line 2)\n" +
            "\t/*a*/\t\t\tStack plates;\n" +
            "\t     \t\t\t^^^^^\n" +
            "Stack is a raw type. References to generic type Stack<E> should be parameterized\n" +
            "----------\n" +
            "2. WARNING in One.groovy (at line 3)\n" +
            "\t/*b*/ Stack plates2;\n" +
            "\t      ^^^^^\n" +
            "Stack is a raw type. References to generic type Stack<E> should be parameterized\n" +
            "----------\n");
    }

    @Test
    public void testPositions2() {
        //@formatter:off
        String[] sources = {
            "One.groovy",
            "class One {\n" +
            "\t\t/*a*/\t\t\tStack plates;\n" +
            "  /*b*/ Stack plates2;\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. WARNING in One.groovy (at line 2)\n" +
            "\t/*a*/\t\t\tStack plates;\n" +
            "\t     \t\t\t^^^^^\n" +
            "Stack is a raw type. References to generic type Stack<E> should be parameterized\n" +
            "----------\n" +
            "2. WARNING in One.groovy (at line 3)\n" +
            "\t/*b*/ Stack plates2;\n" +
            "\t      ^^^^^\n" +
            "Stack is a raw type. References to generic type Stack<E> should be parameterized\n" +
            "----------\n");
    }

    @Test
    public void testFieldPositions1() {
        //@formatter:off
        String[] sources = {
            "p/C.groovy",
            "package p;\n" +
            "public class C {\n" +
            "  List aList;\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. WARNING in p\\C.groovy (at line 3)\n" +
            "\tList aList;\n" +
            "\t^^^^\n" +
            "List is a raw type. References to generic type List<E> should be parameterized\n" +
            "----------\n");
    }

    @Test // TODO: poor positional error for invalid field name
    public void testFieldPositions2() {
        //@formatter:off
        String[] sources = {
            "p/C.groovy",
            "package p;\n" +
            "public class C {\n" +
            "  List<String> class;\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in p\\C.groovy (at line 3)\n" +
            "\tList<String> class;\n" +
            "\t^\n" +
            "Groovy:unexpected token: List\n" +
            "----------\n");
    }

    @Test
    public void testFieldPositions3() {
        //@formatter:off
        String[] sources = {
            "I.groovy",
            "interface I {\n" +
            "  boolean b\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testFieldPositions4() {
        //@formatter:off
        String[] sources = {
            "I.groovy",
            "interface I {\n" +
            "  byte b\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testFieldPositions5() {
        //@formatter:off
        String[] sources = {
            "I.groovy",
            "interface I {\n" +
            "  char c\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testFieldPositions6() {
        //@formatter:off
        String[] sources = {
            "I.groovy",
            "interface I {\n" +
            "  float f\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testFieldPositions7() {
        //@formatter:off
        String[] sources = {
            "I.groovy",
            "interface I {\n" +
            "  double d\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testFieldPositions8() {
        //@formatter:off
        String[] sources = {
            "I.groovy",
            "interface I {\n" +
            "  int i\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testFieldPositions9() {
        //@formatter:off
        String[] sources = {
            "I.groovy",
            "interface I {\n" +
            "  long l\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testFieldPositions10() {
        //@formatter:off
        String[] sources = {
            "I.groovy",
            "interface I {\n" +
            "  short s\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testFieldPositions11() {
        //@formatter:off
        String[] sources = {
            "I.groovy",
            "interface I {\n" +
            "  BigDecimal big = 0.0\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testFieldPositions12() {
        //@formatter:off
        String[] sources = {
            "I.groovy",
            "interface I {\n" +
            "  BigInteger big = 42g\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testFieldPositions13() {
        //@formatter:off
        String[] sources = {
            "I.groovy",
            "interface I {\n" +
            "  Class<?> type = Object\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testFieldPositions14() {
        //@formatter:off
        String[] sources = {
            "I.groovy",
            "interface I {\n" +
            "  String typeName = Object.name\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testSecondaryTypeTagging() {
        //@formatter:off
        String[] sources = {
            "Run.groovy",
            "class Run { public static void main(String[]argv) {print '1.0';} }\n" +
            "class B {}\n" +
            "class C {}\n" +
            "class D {}\n",
        };
        //@formatter:on

        runConformTest(sources, "1.0");

        GroovyCompilationUnitDeclaration gcud = getCUDeclFor("Run.groovy");
        TypeDeclaration[] tds = gcud.types;
        assertTrue((tds[0].bits & ASTNode.IsSecondaryType) == 0);
        assertTrue((tds[1].bits & ASTNode.IsSecondaryType) != 0);
        assertTrue((tds[2].bits & ASTNode.IsSecondaryType) != 0);
        assertTrue((tds[3].bits & ASTNode.IsSecondaryType) != 0);

        //@formatter:off
        sources = new String[] {
            "Run2.groovy",
            "class B {}\n" +
            "class Run2 { public static void main(String[]argv) {print '1.0';} }\n" +
            "class C {}\n" +
            "class D {}\n",
        };
        //@formatter:on

        runConformTest(sources, "1.0");

        gcud = getCUDeclFor("Run2.groovy");
        tds = gcud.types;
        assertTrue((tds[0].bits & ASTNode.IsSecondaryType) != 0);
        assertTrue((tds[1].bits & ASTNode.IsSecondaryType) == 0);
        assertTrue((tds[2].bits & ASTNode.IsSecondaryType) != 0);
        assertTrue((tds[3].bits & ASTNode.IsSecondaryType) != 0);
    }

    @Test
    public void testShellScriptComment1() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "#! blah blah\n" +
            "println 'hello world'\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testShellScriptComment2() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "#! /usr/bin/env nix-shell\n" +
            "#! nix-shell -i groovy -p groovy\n" +
            "/* copyright notice */\n" +
            "println 'hello world'\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Script.groovy (at line 2)\n" +
            "\t#! nix-shell -i groovy -p groovy\n" +
            "\t^\n" +
            "Groovy:unexpected char: '#'\n" +
            "----------\n");
    }

    @Test
    public void testVoidMethodParameter() {
        //@formatter:off
        String[] sources = {
            "Pogo.groovy",
            "class Pogo {\n" +
            "  void m(void p) {}\n" +
            "}\n",

            "Pojo.java",
            "class Pojo {\n" +
            "  void m(void p) {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Pogo.groovy (at line 2)\n" +
            "\tvoid m(void p) {}\n" +
            "\t     ^^^^^^^^^\n" +
            "void is an invalid type for the parameter p of a method\n" +
            "----------\n" +
            "2. ERROR in Pogo.groovy (at line 2)\n" +
            "\tvoid m(void p) {}\n" +
            "\t            ^\n" +
            "Groovy:The parameter 'p' in method 'void m(void)' has invalid type void\n" +
            "----------\n" +
            "----------\n" +
            "1. ERROR in Pojo.java (at line 2)\n" +
            "\tvoid m(void p) {}\n" +
            "\t     ^^^^^^^^^\n" +
            "void is an invalid type for the parameter p of a method\n" +
            "----------\n");
    }

    //--------------------------------------------------------------------------

    private void assertEventCount(int expectedCount, EventListener listener) {
        if (listener.eventCount() != expectedCount) {
            fail("Expected " + expectedCount + " events but found " + listener.eventCount() + "\nEvents:\n" + listener.toString());
        }
    }

    private void assertEvent(String eventText, EventListener listener) {
        boolean found = false;
        Iterator<String> eventIter = listener.getEvents().iterator();
        while (eventIter.hasNext()) {
            String s = eventIter.next();
            if (s.equals(eventText)) {
                found = true;
                break;
            }
        }
        if (!found) {
            fail("Expected event '" + eventText + "'\nEvents:\n" + listener.toString());
        }
    }
}
