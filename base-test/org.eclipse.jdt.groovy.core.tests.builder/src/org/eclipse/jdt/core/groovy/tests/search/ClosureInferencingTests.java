/*
 * Copyright 2009-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.jdt.core.groovy.tests.search;

import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.junit.Test;
import org.osgi.framework.Version;

public final class ClosureInferencingTests extends InferencingTestSuite {

    private void assertExprType(String source, String target, String type) {
        final int offset = source.lastIndexOf(target);
        assertType(source, offset, offset + target.length(), type);
    }

    // As of Groovy 2.4.6, 'bar.foo = X' is seen as 'bar.setFoo(X)' for some cases.
    // See StaticTypeCheckingVisitor.existsProperty(), circa 'checkGetterOrSetter'.
    private static boolean isAccessorPreferredForSTCProperty() {
        Version version = CompilerUtils.getActiveGroovyBundle().getVersion();
        return (version.compareTo(new Version(2, 4, 6)) >= 0);
    }

    //--------------------------------------------------------------------------

    @Test
    public void testClosure1() {
        String contents = "def fn = { a, b -> a + b }";
        assertExprType(contents, "fn", "groovy.lang.Closure");
    }

    @Test
    public void testClosure2() {
        String contents = "def fn = x.&y";
        assertExprType(contents, "fn", "groovy.lang.Closure");
    }

    @Test
    public void testClosure3() {
        String contents =
            "class Foo {\n" +
            "  URL other\n" +
            "  def method(Number param) {\n" +
            "    def fn = { -> param }\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "param", "java.lang.Number");
    }

    @Test
    public void testClosure4() {
        String contents =
            "class Foo {\n" +
            "  URL other\n" +
            "  def method() {\n" +
            "    Number local\n" +
            "    def fn = { -> local }\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "local", "java.lang.Number");
    }

    @Test
    public void testClosure5() {
        String contents =
            "class Foo {\n" +
            "  URL proper\n" +
            "  def method() {\n" +
            "    def fn = { -> proper }\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "proper", "java.net.URL");
    }

    @Test
    public void testClosure6() {
        String contents =
            "class Foo {\n" +
            "  static URL proper\n" +
            "  def method() {\n" +
            "    def fn = { -> proper }\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "proper", "java.net.URL");
    }

    @Test
    public void testClosure7() {
        String contents =
            "class Foo {}\n" +
            "class Bar extends Foo {\n" +
            "  def method() {\n" +
            "    def fn = {\n" +
            "      this\n" +
            "      super\n" +
            "      owner\n" +
            "      delegate\n" +
            "    }\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "this",     "Bar");
        assertExprType(contents, "super",    "Foo");
        assertExprType(contents, "owner",    "Bar");
        assertExprType(contents, "delegate", "Bar");
    }

    @Test // closure with non-default resolve strategy
    public void testClosure8() {
        String contents =
            "class Foo {}\n" +
            "class Bar {\n" +
            "  def method() {\n" +
            "    new Foo().with {\n" +
            "      this\n" +
            "      super\n" +
            "      owner\n" +
            "      delegate\n" +
            "    }\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "this",     "Bar");
        assertExprType(contents, "super",    "java.lang.Object");
        assertExprType(contents, "owner",    "Bar");
        assertExprType(contents, "delegate", "Foo");
    }

    @Test // closure within static scope wrt owner
    public void testClosure9() {
        String contents =
            "class Foo {}\n" +
            "class Bar extends Foo {\n" +
            "  static void main(args) {\n" +
            "    def fn = {\n" +
            "      owner\n" +
            "      delegate\n" +
            "    }\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "owner",    "java.lang.Class<Bar>");
        assertExprType(contents, "delegate", "java.lang.Class<Bar>");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/502
    public void testClosure9a() {
        String contents =
            "class Foo {\n" +
            "  static long bar(String arg) {\n" +
            "  }\n" +
            "  static void baz() {\n" +
            "    String a = 'bc'\n" +
            "    def fn = {\n" +
            "      bar(a)\n" + // call static method from closure within static scope
            "    }\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "bar", "java.lang.Long");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/502
    public void testClosure9b() {
        String contents =
            "class Foo {\n" +
            "  static long bar(String arg) {\n" +
            "  }\n" +
            "  static void baz() {\n" +
            "    String a = 'bc'\n" +
            "    def fn = {\n" +
            "      [].each {\n" +
            "        bar(a)\n" + // call static method from closure within closure within static scope
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "bar", "java.lang.Long");
    }

    @Test
    public void testClosure10() {
        String contents =
            "class Foo {}\n" +
            "class Bar extends Foo {\n" +
            "  static void main(args) {\n" +
            "    def fn = {\n" +
            "      this\n" +
            "      super\n" +
            "    }\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "this",  "java.lang.Class<Bar>");
        assertExprType(contents, "super", "java.lang.Class<Bar>");
    }

    @Test // non-static delegate is same type as static owner
    public void testClosure11() {
        String contents =
            "class Foo {\n" +
            "  Number bar\n" +
            "  static main(args) {\n" +
            "    new Foo().with {\n" +
            "      delegate.bar\n" +
            "      owner.bar\n" +
            "      bar\n" +
            "    }\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "delegate", "Foo"); // obj exp of with
        assertExprType(contents, "owner", "java.lang.Class<Foo>");
        assertExprType(contents, "bar", "java.lang.Number");

        int offset = contents.indexOf("delegate.bar") + "delegate.".length();
        assertType(contents, offset, offset + 3, "java.lang.Number");

        offset = contents.indexOf("owner.bar") + "owner.".length();
        assertUnknownConfidence(contents, offset, offset + 3, "Foo", false);
    }

    @Test // static object expression for delegate
    public void testClosure11a() {
        String contents =
            "class Boo {}\n" +
            "class Foo {\n" +
            "  Number bar\n" +
            "  static main(args) {\n" +
            "    Boo.with {\n" +
            "      delegate.bar\n" +
            "      owner.bar\n" +
            "      bar\n" +
            "    }\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "delegate", "java.lang.Class<Boo>");
        assertExprType(contents, "owner", "java.lang.Class<Foo>");

        int offset = contents.indexOf("delegate.bar") + "delegate.".length();
        assertUnknownConfidence(contents, offset, offset + 3, "Foo", false);

        offset = contents.indexOf("owner.bar") + "owner.".length();
        assertUnknownConfidence(contents, offset, offset + 3, "Foo", false);

        offset = contents.lastIndexOf("bar");
        assertUnknownConfidence(contents, offset, offset + 3, "Foo", false);
    }

    @Test // other (invariant) members of Closure
    public void testClosure12() {
        String contents =
            "def fn = {\n" +
            "  thisObject\n" +
            "  getThisObject()\n" +
            "  directive\n" +
            "  getDirective()\n" +
            "  resolveStrategy\n" +
            "  getResolveStrategy()\n" +
            "  parameterTypes\n" +
            "  getParameterTypes()\n" +
            "  maximumNumberOfParameters\n" +
            "  getMaximumNumberOfParameters()\n" +
            "}";
        assertExprType(contents, "thisObject", "java.lang.Object");
        assertExprType(contents, "getThisObject", "java.lang.Object");
        assertExprType(contents, "directive", "java.lang.Integer");
        assertExprType(contents, "getDirective", "java.lang.Integer");
        assertExprType(contents, "resolveStrategy", "java.lang.Integer");
        assertExprType(contents, "getResolveStrategy", "java.lang.Integer");
        assertExprType(contents, "parameterTypes", "java.lang.Class<T extends java.lang.Object>[]");
        assertExprType(contents, "getParameterTypes", "java.lang.Class<T extends java.lang.Object>[]");
        assertExprType(contents, "maximumNumberOfParameters", "java.lang.Integer");
        assertExprType(contents, "getMaximumNumberOfParameters", "java.lang.Integer");
    }

    @Test // other members of Closure (within static scope wrt owner)
    public void testClosure13() {
        String contents =
            "class A { static void main(args) { def fn = {\n" +
            "  thisObject\n" +
            "  getThisObject()\n" +
            "  directive\n" +
            "  getDirective()\n" +
            "  resolveStrategy\n" +
            "  getResolveStrategy()\n" +
            "  parameterTypes\n" +
            "  getParameterTypes()\n" +
            "  maximumNumberOfParameters\n" +
            "  getMaximumNumberOfParameters()\n" +
            "}}}";
        assertExprType(contents, "thisObject", "java.lang.Object");
        assertExprType(contents, "getThisObject", "java.lang.Object");
        assertExprType(contents, "directive", "java.lang.Integer");
        assertExprType(contents, "getDirective", "java.lang.Integer");
        assertExprType(contents, "resolveStrategy", "java.lang.Integer");
        assertExprType(contents, "getResolveStrategy", "java.lang.Integer");
        assertExprType(contents, "parameterTypes", "java.lang.Class<T extends java.lang.Object>[]");
        assertExprType(contents, "getParameterTypes", "java.lang.Class<T extends java.lang.Object>[]");
        assertExprType(contents, "maximumNumberOfParameters", "java.lang.Integer");
        assertExprType(contents, "getMaximumNumberOfParameters", "java.lang.Integer");
    }

    @Test
    public void testClosure14() {
        String contents =
            "class A {\n" +
            "  Number b\n" +
            "  static void main(args) {\n" +
            "    def fn = {\n" +
            "      b\n" + // unknown because enclosing declaration is static
            "    }\n" +
            "  }\n" +
            "}";
        int offset = contents.lastIndexOf('b');
        assertUnknownConfidence(contents, offset, offset + 1, "A", false);
    }

    @Test
    public void testClosure15() {
        String contents =
            "class A {\n" +
            "  Number b\n" +
            "  static void main(args) {\n" +
            "    with {\n" + // <-- changes the resolve strategy
            "      b\n" + // unknown because enclosing declaration is static
            "    }\n" +
            "  }\n" +
            "}";
        int offset = contents.lastIndexOf('b');
        assertUnknownConfidence(contents, offset, offset + 1, "A", false);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/360
    public void testClosure16() {
        String contents =
            "class A {\n" +
            "  public C xxx\n" +
            "}\n" +
            "class B {\n" +
            "  public C xyz\n" +
            "  static void meth(A a) {\n" +
            "    a.with {\n" +
            "      xxx\n" + // from delegate
            "      xyz\n" + // not available
            "    }\n" +
            "  }\n" +
            "}\n" +
            "class C {}";
        assertExprType(contents, "xxx", "C");
        int offset = contents.lastIndexOf("xyz");
        assertUnknownConfidence(contents, offset, offset + 3, "B", false);
    }

    @Test // closure is part of method call expression
    public void testClosure17() {
        String contents =
            "class A {\n" +
            "  def m() {}\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def m() {\n" +
            "    [''].collect {\n" +
            "      this\n" +
            "      super\n" +
            "      owner\n" +
            "      delegate\n" +
            "    }\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "this",     "B");
        assertExprType(contents, "super",    "A");
        assertExprType(contents, "owner",    "B");
        assertExprType(contents, "delegate", "B");
    }

    @Test
    public void testClosure18() {
        String contents =
            "class A {\n" +
            "  def m() {}\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class B extends A {\n" +
            "  def m() {\n" +
            "    def fn = {\n" +
            "      getThisObject()\n" +
            "      thisObject\n" +
            "      super.m()\n" +
            "      this\n" +
            "    }\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "getThisObject", "java.lang.Object");
        assertExprType(contents, "thisObject", "java.lang.Object");
        assertExprType(contents, "super", "A");
        assertExprType(contents, "this", "B");

        // @CompileStatic 2.3+ alters calls to super methods
        int start = contents.lastIndexOf("m()"), end = start + 1;
        assertDeclaration(contents, start, end, "A", "m", DeclarationKind.METHOD);
    }

    @Test
    public void testClosure19() {
        String contents =
            "class A {\n" +
            "  def m() {}\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class B extends A {\n" +
            "  def m() {\n" +
            "    def fn = {\n" +
            "      super.equals(null)\n" +
            "    }\n" +
            "  }\n" +
            "}";

        // @CompileStatic 2.3+ alters calls to super methods
        int start = contents.indexOf("equals("), end = start + "equals".length();
        assertDeclaration(contents, start, end, "java.lang.Object", "equals", DeclarationKind.METHOD);
    }

    @Test
    public void testClosure20() {
        String contents =
            "''.foo {\n" +
            "  substring" +
            "}";
        int offset = contents.indexOf("substring");
        assertUnknownConfidence(contents, offset, offset + "substring".length(), "", false);
    }

    @Test
    public void testClosure21() {
        String contents =
            "''.foo {\n" +
            "  delegate.substring" +
            "}";
        int offset = contents.indexOf("substring");
        assertUnknownConfidence(contents, offset, offset + "substring".length(), "", false);
    }

    @Test
    public void testClosure22() {
        String contents =
            "''.foo {\n" +
            "  this.substring" +
            "}";
        int offset = contents.indexOf("substring");
        assertUnknownConfidence(contents, offset, offset + "substring".length(), "", false);
    }

    @Test
    public void testClosure23() {
        String contents =
            "''.with {\n" +
            "  substring" +
            "}";
        int offset = contents.indexOf("substring");
        assertType(contents, offset, offset + "substring".length(), "java.lang.String");
        assertDeclaringType(contents, offset, offset + "substring".length(), "java.lang.String");
    }

    @Test
    public void testClosure24() {
        String contents =
            "new Date().with {\n" +
            "  def t = time\n" +
            "}";
        int start = contents.lastIndexOf("time");
        int end = start + "time".length();
        assertType(contents, start, end, "java.lang.Long");
    }

    @Test
    public void testClosure25() {
        String contents =
            "new Date().with {\n" +
            "  time = 0L\n" +
            "}";
        int start = contents.lastIndexOf("time");
        int end = start + "time".length();
        assertType(contents, start, end, "java.lang.Void");
    }

    @Test
    public void testClosure26() {
        String contents =
            "new Date().with {\n" +
            "  time = 0L\n" +
            "  def t = time\n" + // this 'time' property should not be seen as setTime()
            "}";
        int start = contents.lastIndexOf("time");
        int end = start + "time".length();
        assertType(contents, start, end, "java.lang.Long");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/660
    public void testClosure27() {
        String contents =
            "new Date().with { one, two = delegate ->\n" +
            "}";
        int offset = contents.indexOf("delegate");
        assertType(contents, offset, offset + "delegate".length(), "java.util.Date");
    }

    @Test
    public void testClosure28() {
        String contents =
            "def closure = { int i = 2 ->\n" +
            "}";
        int offset = contents.indexOf("2");
        assertType(contents, offset, offset + 1, "java.lang.Integer");
    }

    @Test
    public void testClosure29() {
        String contents =
            "class Foo {\n" +
            "  private Number n = 0\n" +
            "  Number getBar() { return n }\n" +
            "  void setBar(Number n) { this.n = n }\n" +
            "}\n" +
            "new Foo().with {\n" +
            "  bar += 42\n" + // property-style reference to getter and setter via '+='
            "}";
        int offset = contents.indexOf("bar");
        assertType(contents, offset, offset + "bar".length(), "java.lang.Void");
        assertDeclaration(contents, offset, offset + "bar".length(), "Foo", "setBar", DeclarationKind.METHOD);
    }

    @Test // closure within closure
    public void testDoubleClosure1() {
        String contents =
            "def x = { def y = {\n" +
            "  owner\n" +
            "  delegate\n" +
            "  thisObject\n" +
            "  getThisObject()\n" +
            "  resolveStrategy\n" +
            "  getResolveStrategy()\n" +
            "}}";
        assertExprType(contents, "owner", "groovy.lang.Closure");
        assertExprType(contents, "delegate", "groovy.lang.Closure");
        assertExprType(contents, "thisObject", "java.lang.Object");
        assertExprType(contents, "getThisObject", "java.lang.Object");
        assertExprType(contents, "resolveStrategy", "java.lang.Integer");
        assertExprType(contents, "getResolveStrategy", "java.lang.Integer");
    }

    @Test
    public void testDoubleClosure2() {
        String contents =
            "''.with {\n" +
            "  1.with {\n" +
            "    intValue\n" +
            "  }" +
            "}";
        assertExprType(contents, "intValue", "java.lang.Integer");
    }

    @Test
    public void testDoubleClosure3() {
        String contents =
            "''.with {\n" +
            "  1.with {\n" +
            "    intValue()\n" +
            "  }" +
            "}";
        assertExprType(contents, "intValue", "java.lang.Integer");
    }

    @Test
    public void testDoubleClosure4() {
        String contents =
            "''.with {\n" +
            "  1.with {\n" +
            "    delegate.intValue()\n" +
            "  }" +
            "}";
        assertExprType(contents, "intValue", "java.lang.Integer");
    }

    @Test // DGM
    public void testDoubleClosure5() {
        String contents =
            "''.with {\n" +
            "  1.with {\n" +
            "    abs\n" +
            "  }" +
            "}";
        assertExprType(contents, "abs", "java.lang.Integer");
    }

    @Test
    public void testDoubleClosure6() {
        String contents =
            "''.with {\n" +
            "  1.with {\n" +
            "    abs()\n" +
            "  }" +
            "}";
        assertExprType(contents, "abs", "java.lang.Integer");
    }

    @Test
    public void testDoubleClosure7() {
        String contents =
            "''.with {\n" +
            "  1.with {\n" +
            "    delegate.abs()\n" +
            "  }" +
            "}";
        assertExprType(contents, "abs", "java.lang.Integer");
    }

    @Test
    public void testDoubleClosure8() {
        String contents =
            "''.with {\n" +
            "  1.with {\n" +
            "    this.abs()\n" +
            "  }" +
            "}";
        int start = contents.lastIndexOf("abs");
        int end = start + "abs".length();
        assertUnknownConfidence(contents, start, end, "Search", false);
    }

    @Test
    public void testDoubleClosure9() {
        String contents =
            "''.with {\n" +
            "  1.with {\n" +
            "    this\n" +
            "  }" +
            "}";
        assertExprType(contents, "this", "Search");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/377
    public void testDoubleClosure10() {
        String contents =
            "import java.beans.*\n" +
            "class B extends PropertyChangeSupport {\n" +
            "  Number info\n" +
            "}\n" +
            "class C {\n" +
            "  B bean\n" +
            "  void init() {\n" +
            "    bean.with {\n" +
            "      addPropertyChangeListener('name') { PropertyChangeEvent event ->\n" +
            "        info\n" + // from outer delegate
            "      }" +
            "    }\n" +
            "  }\n" +
            "}";
        int offset = contents.lastIndexOf("info");
        assertType(contents, offset, offset + 4, "java.lang.Number");
        assertDeclaringType(contents, offset, offset + 4, "B"); // outer delegate
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/377
    public void testDoubleClosure11() {
        String contents =
            "import java.beans.*\n" +
            "class B extends PropertyChangeSupport {\n" +
            "  boolean meth(args) {}\n" +
            "}\n" +
            "class C {\n" +
            "  B bean\n" +
            "  void init() {\n" +
            "    bean.with {\n" +
            "      addPropertyChangeListener('name') { PropertyChangeEvent event ->\n" +
            "        meth(1, '2', ~/3/)\n" + // from outer delegate
            "      }" +
            "    }\n" +
            "  }\n" +
            "}";
        int offset = contents.lastIndexOf("meth");
        assertType(contents, offset, offset + 4, "java.lang.Boolean");
        assertDeclaringType(contents, offset, offset + 4, "B"); // outer delegate
    }

    @Test
    public void testCoercedClosure1() {
        createUnit("Face",
            "interface Face {\n" +
            "  void meth(int a, BigInteger b, java.util.regex.Pattern c)\n" +
            "}");

        String contents = "Face f = { x, y, z-> }";

        assertExprType(contents, "x", "java.lang.Integer");
        assertExprType(contents, "y", "java.math.BigInteger");
        assertExprType(contents, "z", "java.util.regex.Pattern");
    }

    @Test
    public void testCoercedClosure2() {
        createUnit("Face",
            "interface Face {\n" +
            "  void meth(int a, BigInteger b, java.util.regex.Pattern c)\n" +
            "}");

        String contents = "def f = { x, y, z-> } as Face";

        assertExprType(contents, "x", "java.lang.Integer");
        assertExprType(contents, "y", "java.math.BigInteger");
        assertExprType(contents, "z", "java.util.regex.Pattern");
    }

    @Test // Closure type inference without @CompileStatic
    public void testCompileStaticClosure0() {
        String contents =
            "import groovy.beans.Bindable\n" +
            "class A {\n" +
            "  @Bindable\n" +
            "  String foo\n" +
            "  static void main(String[] args) {\n" +
            "    A a = new A()\n" +
            "    a.foo = 'old'\n" +
            "    a.addPropertyChangeListener('foo') {\n" +
            "      println 'foo changed: ' + it.oldValue + ' -> ' + it.newValue\n" +
            "    }\n" +
            "    a.foo = 'new'\n" +
            "  }\n" +
            "}";

        int offset = contents.lastIndexOf("it");
        assertType(contents, offset, offset + 2, "java.beans.PropertyChangeEvent");
    }

    @Test // GRECLIPSE-1748: Closure type inference with @CompileStatic
    public void testCompileStaticClosure1() {
        String contents =
            "import groovy.beans.Bindable\n" +
            "import groovy.transform.CompileStatic\n" +
            "class A {\n" +
            "  @Bindable\n" +
            "  String foo\n" +
            "  @CompileStatic" +
            "  static void main(String[] args) {\n" +
            "    A a = new A()\n" +
            "    a.foo = 'old'\n" +
            "    a.addPropertyChangeListener('foo') {\n" +
            "      println 'foo changed: ' + it.oldValue + ' -> ' + it.newValue\n" +
            "    }\n" +
            "    a.foo = 'new'\n" +
            "  }\n" +
            "}";

        int start = contents.lastIndexOf("it");
        int end = start + "it".length();
        assertType(contents, start, end, "java.beans.PropertyChangeEvent");
    }

    @Test // GRECLIPSE-1751
    public void testWithAndClosure1() throws Exception {
        createUnit("p", "D",
            "package p\n" +
            "class D {\n" +
            "  String foo\n" +
            "  D bar\n" +
            "}");
        String contents =
            "package p\n" +
            "class E {\n" +
            "  D d = new D()\n" +
            "  void doSomething() {\n" +
            "    d.with {\n" +
            "      foo = 'foo'\n" +
            "      bar = new D()\n" +
            "      bar.foo = 'bar'\n" +
            "    }\n" +
            "  }\n" +
            "}";

        int start = contents.indexOf("foo");
        int end = start + "foo".length();
        assertType(contents, start, end, "java.lang.String");

        start = contents.indexOf("bar", end);
        end = start + "bar".length();
        assertType(contents, start, end, "p.D");

        start = contents.indexOf("bar", end);
        end = start + "bar".length();
        assertType(contents, start, end, "p.D");

        start = contents.indexOf("foo", end);
        end = start + "foo".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testWithAndClosure2() throws Exception {
        createUnit("p", "D",
            "package p\n" +
            "class D {\n" +
            "  String foo\n" +
            "  D bar\n" +
            "}");
        String contents =
            "package p\n" +
            "@groovy.transform.TypeChecked\n" +
            "class E {\n" +
            "  D d = new D()\n" +
            "  void doSomething() {\n" +
            "    d.with {\n" +
            "      foo = 'foo'\n" +
            "      bar = new D()\n" +
            "      bar.foo = 'bar'\n" +
            "    }\n" +
            "  }\n" +
            "}";

        int start = contents.indexOf("foo");
        int end = start + "foo".length();
        assertType(contents, start, end, "java.lang.String");

        start = contents.indexOf("bar", end);
        end = start + "bar".length();
        assertType(contents, start, end, "p.D");

        start = contents.indexOf("bar", end);
        end = start + "bar".length();
        assertType(contents, start, end, "p.D");

        start = contents.indexOf("foo", end);
        end = start + "foo".length();
        assertType(contents, start, end, isAccessorPreferredForSTCProperty() ? "java.lang.Void" : "java.lang.String");
    }

    @Test
    public void testWithAndClosure3() throws Exception {
        createUnit("p", "D",
            "package p\n" +
            "class D {\n" +
            "  String foo\n" +
            "  D bar\n" +
            "}");
        String contents =
            "package p\n" +
            "@groovy.transform.CompileStatic\n" +
            "class E {\n" +
            "  D d = new D()\n" +
            "  void doSomething() {\n" +
            "    d.with {\n" +
            "      foo = 'foo'\n" +
            "      bar = new D()\n" +
            "      bar.foo = 'bar'\n" +
            "    }\n" +
            "  }\n" +
            "}";

        int start = contents.indexOf("foo");
        int end = start + "foo".length();
        assertType(contents, start, end, isAccessorPreferredForSTCProperty() ? "java.lang.Void" : "java.lang.String");

        start = contents.indexOf("bar", end);
        end = start + "bar".length();
        assertType(contents, start, end, isAccessorPreferredForSTCProperty() ? "java.lang.Void" : "p.D");

        start = contents.indexOf("bar", end);
        end = start + "bar".length();
        assertType(contents, start, end, "p.D");

        start = contents.indexOf("foo", end);
        end = start + "foo".length();
        assertType(contents, start, end, isAccessorPreferredForSTCProperty() ? "java.lang.Void" : "java.lang.String");
    }

    @Test
    public void testWithAndClosure4() throws Exception {
        createUnit("p", "D",
            "package p\n" +
            "class D {\n" +
            "  String foo\n" +
            "  D bar = new D()\n" +
            "}");
        String contents =
            "package p\n" +
            "@groovy.transform.CompileStatic\n" +
            "class E {\n" +
            "  D d = new D()\n" +
            "  void doSomething() {\n" +
            "    d.with {\n" +
            "      foo = 'foo'\n" +
            "      bar.foo = 'bar'\n" +
            "    }\n" +
            "  }\n" +
            "}";

        int start = contents.indexOf("foo");
        int end = start + "foo".length();
        assertType(contents, start, end, isAccessorPreferredForSTCProperty() ? "java.lang.Void" : "java.lang.String");

        start = contents.indexOf("bar", end);
        end = start + "bar".length();
        assertType(contents, start, end, "p.D");

        start = contents.indexOf("foo", end);
        end = start + "foo".length();
        assertType(contents, start, end, isAccessorPreferredForSTCProperty() ? "java.lang.Void" : "java.lang.String");
    }

    @Test
    public void testClosureReturnType1() {
        String contents =
            "class Foo {\n" +
            "  Number bar\n" +
            "  String baz\n" +
            "}\n" +
            "def foo = { -> \n" +
            "  return new Foo()\n" +
            "}()\n" +
            "foo.bar\n" +
            "foo.baz\n" +
            "";

        int offset = contents.indexOf("foo");
        assertType(contents, offset, offset + "foo".length(), "Foo");

        offset = contents.lastIndexOf("bar");
        assertType(contents, offset, offset + "bar".length(), "java.lang.Number");

        offset = contents.lastIndexOf("baz");
        assertType(contents, offset, offset + "bar".length(), "java.lang.String");
    }

    @Test
    public void testClosureReturnType2() {
        String contents =
            "class Foo {\n" +
            "  Number bar\n" +
            "  String baz\n" +
            "}\n" +
            "def arr = { -> \n" +
            "  return new Foo[0]\n" +
            "}()\n" +
            "arr.length\n" +
            "arr[0].bar\n" +
            "arr[0].baz\n" +
            "";

        int offset = contents.indexOf("arr");
        assertType(contents, offset, offset + "arr".length(), "Foo[]");

        offset = contents.lastIndexOf("bar");
        assertType(contents, offset, offset + "bar".length(), "java.lang.Number");

        offset = contents.lastIndexOf("baz");
        assertType(contents, offset, offset + "bar".length(), "java.lang.String");

        offset = contents.lastIndexOf("length");
        assertType(contents, offset, offset + "length".length(), "java.lang.Integer");
    }

    @Test
    public void testClosureReturnType3() {
        String contents =
            "class Foo {\n" +
            "  Number bar\n" +
            "  String baz\n" +
            "}\n" +
            "def foo = this.with {\n" +
            "  return new Foo()\n" +
            "}\n" +
            "foo.bar\n" +
            "foo.baz\n" +
            "";

        int offset = contents.indexOf("foo");
        assertType(contents, offset, offset + "foo".length(), "Foo");

        offset = contents.lastIndexOf("bar");
        assertType(contents, offset, offset + "bar".length(), "java.lang.Number");

        offset = contents.lastIndexOf("baz");
        assertType(contents, offset, offset + "bar".length(), "java.lang.String");
    }

    @Test
    public void testClosureReturnType4() {
        String contents =
            "class Foo {\n" +
            "  Number bar\n" +
            "  String baz\n" +
            "}\n" +
            "def arr = this.with {\n" +
            "  return new Foo[0]\n" +
            "}\n" +
            "arr.length\n" +
            "arr[0].bar\n" +
            "arr[0].baz\n" +
            "";

        int offset = contents.indexOf("arr");
        assertType(contents, offset, offset + "arr".length(), "Foo[]");

        offset = contents.lastIndexOf("bar");
        assertType(contents, offset, offset + "bar".length(), "java.lang.Number");

        offset = contents.lastIndexOf("baz");
        assertType(contents, offset, offset + "bar".length(), "java.lang.String");

        offset = contents.lastIndexOf("length");
        assertType(contents, offset, offset + "length".length(), "java.lang.Integer");
    }

    @Test
    public void testClosureParamsAnnotation1() {
        String contents =
            "import groovy.transform.stc.*\n" +
            "def match(@ClosureParams(value=SimpleType, options=['java.util.regex.Pattern']) Closure block) {\n" +
            "  block(item)\n" +
            "}\n" +
            "\n" +
            "match { it }";

        String target = "it";
        int offset = contents.lastIndexOf(target);
        assertType(contents, offset, offset + target.length(), "java.util.regex.Pattern");
    }

    @Test
    public void testClosureParamsAnnotation2() {
        String contents =
            "import java.util.regex.*\n" +
            "import groovy.transform.stc.*\n" +
            "def doItUp(List<Pattern> list, @ClosureParams(FirstParam.FirstGenericType) Closure code) {\n" +
            "  for (item in list) {\n" +
            "    code(item)\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "doItUp([]) { it }";

        String target = "it";
        int offset = contents.lastIndexOf(target);
        assertType(contents, offset, offset + target.length(), "java.util.regex.Pattern");
    }

    @Test
    public void testClosureReferencesSuperClass() {
        String contents =
            "class MySuper {\n" +
            "  public void insuper() {}\n" +
            "}\n" +
            "class MySub extends MySuper {\n" +
            "  public void foo() {\n" +
            "    [1].each {\n" +
            "      insuper('3')\n" +
            "    }\n" +
            "  }\n" +
            "}";
        int offset = contents.lastIndexOf("insuper");
        assertDeclaringType(contents, offset, offset + "insuper".length(), "MySuper");
    }

    @Test
    public void testGRECLIPSE1348() {
        String contents =
            "class A {\n" +
            "  def myMethod(String owner) {\n" +
            "    return { return owner }\n" +
            "  }\n" +
            "}";
        int start = contents.lastIndexOf("owner");
        int end = start + "owner".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testGRECLIPSE1348a() {
        String contents =
            "class A {\n" +
            "  def myMethod(String notOwner) {\n" +
            "    return { return owner }\n" +
            "  }\n" +
            "}";
        int start = contents.lastIndexOf("owner");
        int end = start + "owner".length();
        assertType(contents, start, end, "A");
    }
}
