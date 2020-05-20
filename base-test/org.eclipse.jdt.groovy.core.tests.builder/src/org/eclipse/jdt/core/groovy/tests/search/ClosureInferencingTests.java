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
package org.eclipse.jdt.core.groovy.tests.search;

import org.junit.Test;

public final class ClosureInferencingTests extends InferencingTestSuite {

    @Test
    public void testClosure1() {
        String contents =
            //@formatter:off
            "def fn = { a, b ->\n" +
            "  return a + b\n" +
            "}";
            //@formatter:on
        assertType(contents, "fn", "groovy.lang.Closure");
    }

    @Test
    public void testClosure2() {
        String contents = "def fn = x.&y";
        assertType(contents, "fn", "groovy.lang.Closure");
    }

    @Test
    public void testClosure3() {
        String contents =
            //@formatter:off
            "class Foo {\n" +
            "  URL other\n" +
            "  def method(Number param) {\n" +
            "    def fn = { -> param }\n" +
            "  }\n" +
            "}";
            //@formatter:on

        assertType(contents, "param", "java.lang.Number");
    }

    @Test
    public void testClosure4() {
        String contents =
            //@formatter:off
            "class Foo {\n" +
            "  URL other\n" +
            "  def method() {\n" +
            "    Number local\n" +
            "    def fn = { -> local }\n" +
            "  }\n" +
            "}";
            //@formatter:on

        assertType(contents, "local", "java.lang.Number");
    }

    @Test
    public void testClosure5() {
        String contents =
            //@formatter:off
            "class Foo {\n" +
            "  URL proper\n" +
            "  def method() {\n" +
            "    def fn = { -> proper }\n" +
            "  }\n" +
            "}";
            //@formatter:on

        assertType(contents, "proper", "java.net.URL");
    }

    @Test
    public void testClosure6() {
        String contents =
            //@formatter:off
            "class Foo {\n" +
            "  static URL proper\n" +
            "  def method() {\n" +
            "    def fn = { -> proper }\n" +
            "  }\n" +
            "}";
            //@formatter:on

        assertType(contents, "proper", "java.net.URL");
    }

    @Test
    public void testClosure7() {
        String contents =
            //@formatter:off
            "class Foo {}\n" +
            "class Bar extends Foo {\n" +
            "  def method() {\n" +
            "    def fn = {\n" +
            "      this\n" +
            "      super\n" +
            "      owner\n" +
            "      getOwner()\n" +
            "      delegate\n" +
            "      getDelegate()\n" +
            "    }\n" +
            "  }\n" +
            "}";
            //@formatter:on

        assertType(contents, "this",          "Bar");
        assertType(contents, "super",         "Foo");
        assertType(contents, "owner",         "Bar");
        assertType(contents, "getOwner()",    "Bar");
        assertType(contents, "delegate",      "Bar");
        assertType(contents, "getDelegate()", "Bar");
    }

    @Test // closure with non-default resolve strategy
    public void testClosure8() {
        String contents =
            //@formatter:off
            "class Foo {}\n" +
            "class Bar {\n" +
            "  def method() {\n" +
            "    new Foo().with {\n" +
            "      this\n" +
            "      super\n" +
            "      owner\n" +
            "      getOwner()\n" +
            "      delegate\n" +
            "      getDelegate()\n" +
            "    }\n" +
            "  }\n" +
            "}";
            //@formatter:on

        assertType(contents, "this",          "Bar");
        assertType(contents, "super",         "java.lang.Object");
        assertType(contents, "owner",         "Bar");
        assertType(contents, "getOwner()",    "Bar");
        assertType(contents, "delegate",      "Foo");
        assertType(contents, "getDelegate()", "Foo");
    }

    @Test // closure in static scope wrt owner
    public void testClosure9() {
        String contents =
            //@formatter:off
            "class Foo {}\n" +
            "class Bar extends Foo {\n" +
            "  static void main(args) {\n" +
            "    def fn = {\n" +
            "      owner\n" +
            "      getOwner()\n" +
            "      delegate\n" +
            "      getDelegate()\n" +
            "      thisObject\n" +
            "      getThisObject()\n" +
            "    }\n" +
            "  }\n" +
            "}";
            //@formatter:on

        assertType(contents, "owner",           "java.lang.Class<Bar>");
        assertType(contents, "getOwner()",      "java.lang.Class<Bar>");
        assertType(contents, "delegate",        "java.lang.Class<Bar>");
        assertType(contents, "getDelegate()",   "java.lang.Class<Bar>");
        assertType(contents, "thisObject",      "java.lang.Class<Bar>");
        assertType(contents, "getThisObject()", "java.lang.Class<Bar>");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/502
    public void testClosure9a() {
        String contents =
            //@formatter:off
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
            //@formatter:on

        assertType(contents, "bar", "java.lang.Long");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/502
    public void testClosure9b() {
        String contents =
            //@formatter:off
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
            //@formatter:on

        assertType(contents, "bar", "java.lang.Long");
    }

    @Test
    public void testClosure10() {
        String contents =
            //@formatter:off
            "class Foo {}\n" +
            "class Bar extends Foo {\n" +
            "  static void main(args) {\n" +
            "    def fn = {\n" +
            "      this\n" +
            "      super\n" +
            "    }\n" +
            "  }\n" +
            "}";
            //@formatter:on

        assertType(contents, "this",  "java.lang.Class<Bar>");
        assertType(contents, "super", "java.lang.Class<Bar>");
    }

    @Test // non-static delegate is same type as static owner
    public void testClosure11() {
        String contents =
            //@formatter:off
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
            //@formatter:on

        assertType(contents, "delegate", "Foo"); // obj exp of with
        assertType(contents, "owner", "java.lang.Class<Foo>");
        assertType(contents, "bar", "java.lang.Number");

        int offset = contents.indexOf("delegate.bar") + "delegate.".length();
        assertType(contents, offset, offset + 3, "java.lang.Number");

        offset = contents.indexOf("owner.bar") + "owner.".length();
        assertUnknownConfidence(contents, offset, offset + 3);
    }

    @Test // static object expression for delegate
    public void testClosure11a() {
        String contents =
            //@formatter:off
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
            //@formatter:on

        assertType(contents, "delegate", "java.lang.Class<Boo>");
        assertType(contents, "owner", "java.lang.Class<Foo>");

        int offset = contents.indexOf("delegate.bar") + "delegate.".length();
        assertUnknownConfidence(contents, offset, offset + 3);

        offset = contents.indexOf("owner.bar") + "owner.".length();
        assertUnknownConfidence(contents, offset, offset + 3);

        offset = contents.lastIndexOf("bar");
        assertUnknownConfidence(contents, offset, offset + 3);
    }

    @Test // other (invariant) members of Closure
    public void testClosure12() {
        String contents =
            //@formatter:off
            "def fn = {\n" +
            "  directive\n" +
            "  getDirective()\n" +
            "  resolveStrategy\n" +
            "  getResolveStrategy()\n" +
            "  parameterTypes\n" +
            "  getParameterTypes()\n" +
            "  maximumNumberOfParameters\n" +
            "  getMaximumNumberOfParameters()\n" +
            "}";
            //@formatter:on

        assertType(contents, "directive", "java.lang.Integer");
        assertType(contents, "getDirective", "java.lang.Integer");
        assertType(contents, "resolveStrategy", "java.lang.Integer");
        assertType(contents, "getResolveStrategy", "java.lang.Integer");
        assertType(contents, "parameterTypes", "java.lang.Class<T extends java.lang.Object>[]");
        assertType(contents, "getParameterTypes", "java.lang.Class<T extends java.lang.Object>[]");
        assertType(contents, "maximumNumberOfParameters", "java.lang.Integer");
        assertType(contents, "getMaximumNumberOfParameters", "java.lang.Integer");
    }

    @Test // other members of Closure (in static scope wrt owner)
    public void testClosure13() {
        String contents =
            //@formatter:off
            "class A { static void main(args) { def fn = {\n" +
            "  directive\n" +
            "  getDirective()\n" +
            "  resolveStrategy\n" +
            "  getResolveStrategy()\n" +
            "  parameterTypes\n" +
            "  getParameterTypes()\n" +
            "  maximumNumberOfParameters\n" +
            "  getMaximumNumberOfParameters()\n" +
            "}}}";
            //@formatter:on

        assertType(contents, "directive", "java.lang.Integer");
        assertType(contents, "getDirective", "java.lang.Integer");
        assertType(contents, "resolveStrategy", "java.lang.Integer");
        assertType(contents, "getResolveStrategy", "java.lang.Integer");
        assertType(contents, "parameterTypes", "java.lang.Class<T extends java.lang.Object>[]");
        assertType(contents, "getParameterTypes", "java.lang.Class<T extends java.lang.Object>[]");
        assertType(contents, "maximumNumberOfParameters", "java.lang.Integer");
        assertType(contents, "getMaximumNumberOfParameters", "java.lang.Integer");
    }

    @Test
    public void testClosure14() {
        String contents =
            //@formatter:off
            "class A {\n" +
            "  Number b\n" +
            "  static void main(args) {\n" +
            "    def fn = {\n" +
            "      b\n" + // unknown because enclosing declaration is static
            "    }\n" +
            "  }\n" +
            "}";
            //@formatter:on

        int offset = contents.lastIndexOf('b');
        assertUnknownConfidence(contents, offset, offset + 1);
    }

    @Test
    public void testClosure15() {
        String contents =
            //@formatter:off
            "class A {\n" +
            "  Number b\n" +
            "  static void main(args) {\n" +
            "    with {\n" + // <-- changes the resolve strategy
            "      b\n" + // unknown because enclosing declaration is static
            "    }\n" +
            "  }\n" +
            "}";
            //@formatter:on

        int offset = contents.lastIndexOf('b');
        assertUnknownConfidence(contents, offset, offset + 1);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/360
    public void testClosure16() {
        String contents =
            //@formatter:off
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
            //@formatter:on

        assertType(contents, "xxx", "C");
        int offset = contents.lastIndexOf("xyz");
        assertUnknownConfidence(contents, offset, offset + 3);
    }

    @Test // closure is part of method call expression
    public void testClosure17() {
        String contents =
            //@formatter:off
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
            //@formatter:on

        assertType(contents, "this",     "B");
        assertType(contents, "super",    "A");
        assertType(contents, "owner",    "B");
        assertType(contents, "delegate", "B");
    }

    @Test
    public void testClosure18() {
        String contents =
            //@formatter:off
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
            //@formatter:on

        assertType(contents, "getThisObject", "B");
        assertType(contents, "thisObject", "B");
        assertType(contents, "super", "A");
        assertType(contents, "this", "B");

        // @CompileStatic 2.3+ alters calls to super methods
        int start = contents.lastIndexOf("m()"), end = start + 1;
        assertDeclaration(contents, start, end, "A", "m", DeclarationKind.METHOD);
    }

    @Test
    public void testClosure19() {
        String contents =
            //@formatter:off
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
            //@formatter:on

        // @CompileStatic 2.3+ alters calls to super methods
        int start = contents.indexOf("equals("), end = start + "equals".length();
        assertDeclaration(contents, start, end, "java.lang.Object", "equals", DeclarationKind.METHOD);
    }

    @Test
    public void testClosure20() {
        String contents =
            //@formatter:off
            "''.foo {\n" +
            "  substring(0)\n" +
            "}";
            //@formatter:on

        int offset = contents.indexOf("substring");
        assertUnknownConfidence(contents, offset, offset + "substring".length());
    }

    @Test
    public void testClosure21() {
        String contents =
            //@formatter:off
            "''.foo {\n" +
            "  delegate.substring(0)\n" +
            "}";
            //@formatter:on

        int offset = contents.indexOf("substring");
        assertUnknownConfidence(contents, offset, offset + "substring".length());
    }

    @Test
    public void testClosure22() {
        String contents =
            //@formatter:off
            "''.foo {\n" +
            "  this.substring(0)\n" +
            "}";
            //@formatter:on

        int offset = contents.indexOf("substring");
        assertUnknownConfidence(contents, offset, offset + "substring".length());
    }

    @Test
    public void testClosure23() {
        String contents =
            //@formatter:off
            "''.with {\n" +
            "  substring(0)\n" +
            "}";
            //@formatter:on

        int offset = contents.indexOf("substring");
        assertType(contents, offset, offset + "substring".length(), "java.lang.String");
        assertDeclaringType(contents, offset, offset + "substring".length(), "java.lang.String");
    }

    @Test
    public void testClosure24() {
        String contents =
            //@formatter:off
            "new Date().with {\n" +
            "  def t = time\n" +
            "}";
            //@formatter:on

        int start = contents.lastIndexOf("time");
        int end = start + "time".length();
        assertType(contents, start, end, "java.lang.Long");
    }

    @Test
    public void testClosure25() {
        String contents =
            //@formatter:off
            "new Date().with {\n" +
            "  time = 0L\n" +
            "}";
            //@formatter:on

        int start = contents.lastIndexOf("time");
        int end = start + "time".length();
        assertType(contents, start, end, "java.lang.Void");
    }

    @Test
    public void testClosure26() {
        String contents =
            //@formatter:off
            "new Date().with {\n" +
            "  time = 0L\n" +
            "  def t = time\n" + // this 'time' property should not be seen as setTime()
            "}";
            //@formatter:on

        int start = contents.lastIndexOf("time");
        int end = start + "time".length();
        assertType(contents, start, end, "java.lang.Long");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/660
    public void testClosure27() {
        String contents =
            //@formatter:off
            "new Date().with { one, two = delegate ->\n" +
            "}";
            //@formatter:on

        int offset = contents.indexOf("delegate");
        assertType(contents, offset, offset + "delegate".length(), "java.util.Date");
    }

    @Test
    public void testClosure28() {
        String contents =
            //@formatter:off
            "def closure = { int i = 2 ->\n" +
            "}";
            //@formatter:on

        int offset = contents.indexOf("2");
        assertType(contents, offset, offset + 1, "java.lang.Integer");
    }

    @Test
    public void testClosure29() {
        String contents =
            //@formatter:off
            "class Foo {\n" +
            "  private Number n = 0\n" +
            "  Number getBar() { return n }\n" +
            "  void setBar(Number n) { this.n = n }\n" +
            "}\n" +
            "new Foo().with {\n" +
            "  bar += 42\n" + // property-style reference to getter and setter via '+='
            "}";
            //@formatter:on

        int offset = contents.indexOf("bar");
        assertType(contents, offset, offset + "bar".length(), "java.lang.Void");
        assertDeclaration(contents, offset, offset + "bar".length(), "Foo", "setBar", DeclarationKind.METHOD);
    }

    @Test // closure within closure
    public void testNestedClosure1() {
        String contents =
            //@formatter:off
            "def x = { def y = {\n" +
            "  owner\n" +
            "  getOwner()\n" +
            "  delegate\n" +
            "  getDelegate()\n" +
            "  thisObject\n" +
            "  getThisObject()\n" +
            "  resolveStrategy\n" +
            "  getResolveStrategy()\n" +
            "}}";
            //@formatter:on

        assertType(contents, "owner", "groovy.lang.Closure");
        assertType(contents, "getOwner", "groovy.lang.Closure");
        assertType(contents, "delegate", "groovy.lang.Closure");
        assertType(contents, "getDelegate", "groovy.lang.Closure");
        assertType(contents, "thisObject", DEFAULT_UNIT_NAME);
        assertType(contents, "getThisObject", DEFAULT_UNIT_NAME);
        assertType(contents, "resolveStrategy", "java.lang.Integer");
        assertType(contents, "getResolveStrategy", "java.lang.Integer");
    }

    @Test
    public void testNestedClosure2() {
        String contents =
            //@formatter:off
            "''.with {\n" +
            "  1.with {\n" +
            "    intValue\n" +
            "  }\n" +
            "}";
            //@formatter:on

        int offset = contents.indexOf("intValue");
        assertUnknownConfidence(contents, offset, offset + "intValue".length());
    }

    @Test
    public void testNestedClosure3() {
        String contents =
            //@formatter:off
            "''.with {\n" +
            "  1.with {\n" +
            "    intValue()\n" +
            "  }\n" +
            "}";
            //@formatter:on

        assertType(contents, "intValue", "java.lang.Integer");
    }

    @Test
    public void testNestedClosure4() {
        String contents =
            //@formatter:off
            "''.with {\n" +
            "  1.with {\n" +
            "    delegate.intValue()\n" +
            "  }\n" +
            "}";
            //@formatter:on

        assertType(contents, "intValue", "java.lang.Integer");
    }

    @Test // DGM
    public void testNestedClosure5() {
        String contents =
            //@formatter:off
            "''.with {\n" +
            "  1L.with {\n" +
            "    abs\n" +
            "  }\n" +
            "}";
            //@formatter:on

        int offset = contents.indexOf("abs");
        assertUnknownConfidence(contents, offset, offset + "abs".length());
    }

    @Test // DGM
    public void testNestedClosure6() {
        String contents =
            //@formatter:off
            "''.with {\n" +
            "  1L.with {\n" +
            "    abs()\n" +
            "  }\n" +
            "}";
            //@formatter:on

        assertType(contents, "abs", "java.lang.Long");
    }

    @Test
    public void testNestedClosure7() {
        String contents =
            //@formatter:off
            "''.with {\n" +
            "  1L.with {\n" +
            "    delegate.abs()\n" +
            "  }\n" +
            "}";
            //@formatter:on

        assertType(contents, "abs", "java.lang.Long");
    }

    @Test
    public void testNestedClosure8() {
        String contents =
            //@formatter:off
            "''.with {\n" +
            "  1L.with {\n" +
            "    this.abs()\n" +
            "  }\n" +
            "}";
            //@formatter:on

        int offset = contents.indexOf("abs");
        assertUnknownConfidence(contents, offset, offset + "abs".length());
    }

    @Test
    public void testNestedClosure9() {
        String contents =
            //@formatter:off
            "''.with {\n" +
            "  42.with {\n" +
            "    this\n" +
            "  }\n" +
            "}";
            //@formatter:on

        assertType(contents, "this", DEFAULT_UNIT_NAME);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/809
    public void testNestedClosure10() {
        String contents =
            //@formatter:off
            "''.with {\n" +
            "  42.with {\n" +
            "    owner.thisObject\n" +
            "    owner.getThisObject()\n" +
            "  }\n" +
            "}";
            //@formatter:on

        assertType(contents, "thisObject", DEFAULT_UNIT_NAME);
        assertType(contents, "getThisObject", DEFAULT_UNIT_NAME);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/809
    public void testNestedClosure11() {
        String contents =
            //@formatter:off
            "42.with {\n" +
            "  ''.with {\n" +
            "    owner.delegate\n" +
            "    owner.getDelegate()\n" +
            "  }\n" +
            "}";
            //@formatter:on

        assertType(contents, "delegate", "java.lang.Integer");
        assertType(contents, "getDelegate", "java.lang.Integer");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/809
    public void testNestedClosure12() {
        String contents =
            //@formatter:off
            "42.with {\n" +
            "  ''.with {\n" +
            "    def x = owner.owner\n" +
            "    def y = owner.getOwner()\n" +
            "  }\n" +
            "}";
            //@formatter:on

        assertType(contents, "x", DEFAULT_UNIT_NAME);
        assertType(contents, "y", DEFAULT_UNIT_NAME);
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/809
    public void testNestedClosure13() {
        String contents =
            //@formatter:off
            "(~/.../).with {\n" +
            "  ''.with {\n" +
            "    42.with {\n" +
            "      owner.owner.delegate\n" +
            "      owner.owner.getDelegate()\n" +
            "    }\n" +
            "  }\n" +
            "}";
            //@formatter:on

        assertType(contents, "delegate", "java.util.regex.Pattern");
        assertType(contents, "getDelegate", "java.util.regex.Pattern");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/809
    public void testNestedClosure14() {
        String contents =
            //@formatter:off
            "class Foo {\n" +
            "  Number nnn\n" +
            "  static class Bar {\n" +
            "    Number nnn\n" +
            "  }\n" +
            "  static class Baz {\n" +
            "    Number nnn\n" +
            "  }\n" +

            "  def bar(@DelegatesTo(value=Bar, strategy=Closure.OWNER_FIRST) Closure c) {}\n" +
            "  def baz(@DelegatesTo(value=Baz, strategy=Closure.OWNER_FIRST) Closure c) {}\n" +

            "  void meth() {\n" +
            "    bar {\n" +
            "      baz {\n" +
            "        def v = nnn\n" + // refers to Foo.nnn
            "        def x = owner.nnn\n" + // refers to Foo.nnn
            "        def y = delegate.nnn\n" + // refers to Baz.nnn
            "        def z = owner.delegate.nnn\n" + // refers to Bar.nnn
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
            //@formatter:on

        assertType(contents, "v", "java.lang.Number");
        assertType(contents, "x", "java.lang.Number");
        assertType(contents, "y", "java.lang.Number");
        assertType(contents, "z", "java.lang.Number");

        int offset = contents.indexOf("= nnn") + 2;
        assertDeclaringType(contents, offset, offset + "nnn".length(), "Foo");
        offset = contents.indexOf("nnn", offset + 3);
        assertDeclaringType(contents, offset, offset + "nnn".length(), "Foo");
        offset = contents.indexOf("nnn", offset + 3);
        assertDeclaringType(contents, offset, offset + "nnn".length(), "Foo$Baz");
        offset = contents.indexOf("nnn", offset + 3);
        assertDeclaringType(contents, offset, offset + "nnn".length(), "Foo$Bar");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/377
    public void testNestedClosure15() {
        String contents =
            //@formatter:off
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
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
            //@formatter:on

        int offset = contents.lastIndexOf("info");
        assertType(contents, offset, offset + "info".length(), "java.lang.Number");
        assertDeclaringType(contents, offset, offset + "info".length(), "B"); // outer delegate
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/377
    public void testNestedClosure16() {
        String contents =
            //@formatter:off
            "import java.beans.*\n" +
            "class B extends PropertyChangeSupport {\n" +
            "  boolean meth(... args) {}\n" +
            "}\n" +
            "class C {\n" +
            "  B bean\n" +
            "  void init() {\n" +
            "    bean.with {\n" +
            "      addPropertyChangeListener('name') { PropertyChangeEvent event ->\n" +
            "        meth(1, '2', ~/3/)\n" + // from outer delegate
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
            //@formatter:on

        int offset = contents.lastIndexOf("meth");
        assertType(contents, offset, offset + "info".length(), "java.lang.Boolean");
        assertDeclaringType(contents, offset, offset + "info".length(), "B"); // outer delegate
    }

    @Test
    public void testCoercedClosure1() {
        createUnit("Face",
            //@formatter:off
            "interface Face {\n" +
            "  void meth(int a, BigInteger b, java.util.regex.Pattern c)\n" +
            "}");
            //@formatter:on

        //@formatter:off
        String contents = "Face f = { x, y, z-> }";
        //@formatter:on

        assertType(contents, "x", "java.lang.Integer");
        assertType(contents, "y", "java.math.BigInteger");
        assertType(contents, "z", "java.util.regex.Pattern");
    }

    @Test
    public void testCoercedClosure2() {
        createUnit("Face",
            //@formatter:off
            "interface Face {\n" +
            "  void meth(int a, BigInteger b, java.util.regex.Pattern c)\n" +
            "}");
            //@formatter:on

        //@formatter:off
        String contents = "def f = { x, y, z-> } as Face";
        //@formatter:on

        assertType(contents, "x", "java.lang.Integer");
        assertType(contents, "y", "java.math.BigInteger");
        assertType(contents, "z", "java.util.regex.Pattern");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1000
    public void testCoercedClosure3() {
        createUnit("Face",
            //@formatter:off
            "interface Face<T> {\n" +
            "  boolean test(T t)\n" +
            "}");
            //@formatter:on

        //@formatter:off
        String contents =
            "class C<E> {\n" + // like Collection
            "  boolean meth(Face<? super E> f) {\n" + // like removeIf
            "    f.test(null)\n" +
            "  }\n" +
            "}\n" +
            "def c = new C<Integer>()\n" +
            "def result = c.meth { e -> e }\n";
        //@formatter:on

        assertType(contents, "e", "java.lang.Integer");
    }

    @Test // Closure type inference without @CompileStatic
    public void testCompileStaticClosure0() {
        String contents =
            //@formatter:off
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
            //@formatter:on

        int offset = contents.lastIndexOf("it");
        assertType(contents, offset, offset + 2, "java.beans.PropertyChangeEvent");
    }

    @Test // GRECLIPSE-1748: Closure type inference with @CompileStatic
    public void testCompileStaticClosure1() {
        String contents =
            //@formatter:off
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
            //@formatter:on

        int start = contents.lastIndexOf("it");
        int end = start + "it".length();
        assertType(contents, start, end, "java.beans.PropertyChangeEvent");
    }

    @Test // GRECLIPSE-1751
    public void testWithAndClosure1() throws Exception {
        createUnit("p", "D",
            //@formatter:off
            "package p\n" +
            "class D {\n" +
            "  String foo\n" +
            "  D bar\n" +
            "}");
            //@formatter:on

        String contents =
            //@formatter:off
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
            //@formatter:on

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
            //@formatter:off
            "package p\n" +
            "class D {\n" +
            "  String foo\n" +
            "  D bar\n" +
            "}");
            //@formatter:on

        String contents =
            //@formatter:off
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
            //@formatter:on

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
    public void testWithAndClosure3() throws Exception {
        createUnit("p", "D",
            //@formatter:off
            "package p\n" +
            "class D {\n" +
            "  String foo\n" +
            "  D bar\n" +
            "}");
            //@formatter:on

        String contents =
            //@formatter:off
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
            //@formatter:on

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
        assertType(contents, start, end, "java.lang.Void");
    }

    @Test
    public void testWithAndClosure4() throws Exception {
        createUnit("p", "D",
            //@formatter:off
            "package p\n" +
            "class D {\n" +
            "  String foo\n" +
            "  D bar = new D()\n" +
            "}");
            //@formatter:on

        String contents =
            //@formatter:off
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
            //@formatter:on

        int start = contents.indexOf("foo");
        int end = start + "foo".length();
        assertType(contents, start, end, "java.lang.String");

        start = contents.indexOf("bar", end);
        end = start + "bar".length();
        assertType(contents, start, end, "p.D");

        start = contents.indexOf("foo", end);
        end = start + "foo".length();
        assertType(contents, start, end, "java.lang.Void");
    }

    @Test
    public void testWithAndClosure5() throws Exception {
        createUnit("p", "A",
            //@formatter:off
            "package p\n" +
            "class A {\n" +
            "  String foo\n" +
            "}");
            //@formatter:on

        String contents =
            //@formatter:off
            "package p\n" +
            "class B {\n" +
            "  void meth() {\n" +
            "    new A().with {\n" +
            "      def c = new Object() {\n" +
            "        String toString() {foo}\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
            //@formatter:on

        int offset = contents.lastIndexOf("foo");
        assertUnknownConfidence(contents, offset, offset + 3);
    }

    @Test
    public void testWithAndClosure6() throws Exception {
        createUnit("p", "A",
            //@formatter:off
            "package p\n" +
            "class A {\n" +
            "  String foo\n" +
            "}");
            //@formatter:on

        String contents =
            //@formatter:off
            "package p\n" +
            "class B {\n" +
            "  void meth() {\n" +
            "    new A().with {\n" +
            "      def c = new Object() {\n" +
            "        String toString() {it.foo}\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
            //@formatter:on

        int offset = contents.lastIndexOf("foo");
        assertDeclaringType(contents, offset, offset + 3, "p.A");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/927
    public void testWithAndClosure7() throws Exception {
        createUnit("p", "A",
            //@formatter:off
            "package p\n" +
            "class A {\n" +
            "  String foo\n" +
            "}");
            //@formatter:on

        String contents =
            //@formatter:off
            "package p\n" +
            "class B {\n" +
            "  void meth() {\n" +
            "    new A().with {\n" +
            "      def c = new Object() {\n" +
            "        String foo = 'bar'\n" +
            "        String toString() {foo}\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
            //@formatter:on

        int offset = contents.lastIndexOf("foo");
        assertDeclaringType(contents, offset, offset + 3, "p.B$1");
    }

    @Test
    public void testClosureReturnType1() {
        String contents =
            //@formatter:off
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
            //@formatter:on

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
            //@formatter:off
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
            //@formatter:on

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
            //@formatter:off
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
            //@formatter:on

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
            //@formatter:off
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
            //@formatter:on

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
            //@formatter:off
            "import groovy.transform.stc.*\n" +
            "def match(@ClosureParams(value=SimpleType, options=['java.util.regex.Pattern']) Closure block) {\n" +
            "  block(item)\n" +
            "}\n" +
            "\n" +
            "match { it }";
            //@formatter:on

        String target = "it";
        int offset = contents.lastIndexOf(target);
        assertType(contents, offset, offset + target.length(), "java.util.regex.Pattern");
    }

    @Test
    public void testClosureParamsAnnotation2() {
        String contents =
            //@formatter:off
            "import java.util.regex.*\n" +
            "import groovy.transform.stc.*\n" +
            "def doItUp(List<Pattern> list, @ClosureParams(FirstParam.FirstGenericType) Closure code) {\n" +
            "  for (item in list) {\n" +
            "    code(item)\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "doItUp([]) { it }";
            //@formatter:on

        String target = "it";
        int offset = contents.lastIndexOf(target);
        assertType(contents, offset, offset + target.length(), "java.util.regex.Pattern");
    }

    @Test
    public void testClosureParamsAnnotation3() {
        String contents =
            //@formatter:off
            "import groovy.transform.stc.*\n" +
            "class C {\n" +
            "  C(String s, @ClosureParams(value=SimpleType, options='java.util.List<java.lang.Integer>') Closure c) {\n" +
            "  }\n" +
            "}\n" +
            "new C('str', { list -> null })\n";
            //@formatter:on

        assertType(contents, "list", "java.util.List<java.lang.Integer>");
    }

    @Test
    public void testClosureParamsAnnotation4() {
        String contents =
            //@formatter:off
            "import groovy.transform.stc.*\n" +
            "class C {\n" +
            "  static m(String s, @ClosureParams(value=SimpleType, options='java.util.List<java.lang.Integer>') Closure c) {\n" +
            "  }\n" +
            "  static test() {\n" +
            "    m('str', { list -> null })\n" +
            "  }\n" +
            "}\n";
            //@formatter:on

        assertType(contents, "list", "java.util.List<java.lang.Integer>");
    }

    @Test
    public void testClosureReferencesSuperClass() {
        String contents =
            //@formatter:off
            "class A {\n" +
            "  void insuper(x) {}\n" +
            "}\n" +
            "class B extends A {\n" +
            "  def m() {\n" +
            "    [1].each {\n" +
            "      insuper('3')\n" +
            "    }\n" +
            "  }\n" +
            "}";
            //@formatter:on

        assertDeclaringType(contents, "insuper", "A");
    }

    @Test
    public void testGRECLIPSE1348() {
        String contents =
            //@formatter:off
            "class C {\n" +
            "  def m(String owner) {\n" +
            "    return { return owner }\n" +
            "  }\n" +
            "}";
            //@formatter:on

        assertType(contents, "owner", "java.lang.String");
    }

    @Test
    public void testGRECLIPSE1348a() {
        String contents =
            //@formatter:off
            "class C {\n" +
            "  def m(String notOwner) {\n" +
            "    return { return owner }\n" +
            "  }\n" +
            "}";
            //@formatter:on

        assertType(contents, "owner", "C");
    }
}
