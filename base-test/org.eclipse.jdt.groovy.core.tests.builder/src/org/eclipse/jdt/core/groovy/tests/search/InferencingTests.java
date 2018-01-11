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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Comparator;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.junit.Test;

public final class InferencingTests extends InferencingTestSuite {

    private void assertExprType(String source, String target, String type) {
        final int offset = source.lastIndexOf(target);
        assertType(source, offset, offset + target.length(), type);
    }

    private void assertNoUnknowns(String source) {
        GroovyCompilationUnit unit = createUnit("Search", source);

        TypeInferencingVisitorWithRequestor visitor = factory.createVisitor(unit);
        visitor.DEBUG = true;
        UnknownTypeRequestor requestor = new UnknownTypeRequestor();
        visitor.visitCompilationUnit(requestor);
        List<ASTNode> unknownNodes = requestor.getUnknownNodes();
        assertTrue("Should not have found any AST nodes with unknown confidence, but instead found:\n" + unknownNodes, unknownNodes.isEmpty());
    }

    //--------------------------------------------------------------------------

    @Test
    public void testLocalVar1() {
        String contents ="def x\nthis.x";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertUnknownConfidence(contents, start, end, "Search", false);
    }

    @Test
    public void testLocalVar2() {
        String contents ="def x\ndef y = { this.x }";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertUnknownConfidence(contents, start, end, "Search", false);
    }

    @Test
    public void testLocalVar2a() {
        String contents ="def x\ndef y = { this.x() }";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertUnknownConfidence(contents, start, end, "Search", false);
    }

    @Test
    public void testLocalVar3() {
        String contents ="int x\ndef y = { x }";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testLocalVar4() {
        String contents ="int x\ndef y = { x() }";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testLocalMethod1() {
        String contents ="int x() { }\ndef y = { x() }";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testLocalMethod2() {
        String contents ="int x() { }\ndef y = { x }";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testLocalMethod3() {
        String contents ="int x() { }\ndef y = { def z = { x } }";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testLocalMethod4() {
        String contents ="int x() { }\ndef y = { def z = { x() } }";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testLocalMethod5() {
        String contents ="int x() { }\ndef y = { def z = { this.x() } }";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testLocalMethod6() {
        String contents ="def x\ndef y = { delegate.x() }";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertUnknownConfidence(contents, start, end, "Search", false);
    }

    @Test
    public void testLocalMethod7() {
        String contents ="def x\ndef y = { delegate.x }";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertUnknownConfidence(contents, start, end, "Search", false);
    }

    @Test
    public void testNumber1() {
        assertType("10", "java.lang.Integer");
    }

    @Test
    public void testNumber1a() {
        // same as above, but test that whitespace is not included
        assertType("10 ", 0, 2, "java.lang.Integer");
    }

    @Test
    public void testNumber2() {
        String contents ="def x = 1+2\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testNumber3() {
        assertType("10L", "java.lang.Long");
    }

    @Test
    public void testNumber4() {
        assertType("10++", "java.lang.Integer");
    }

    @Test
    public void testNumber5() {
        assertType("++10", "java.lang.Integer");
    }

    @Test
    public void testNumber6() {
        String contents = "(x <=> y).intValue()";
        int start = contents.indexOf("intValue");
        int end = start + "intValue".length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testString1() {
        assertType("\"10\"", "java.lang.String");
    }

    @Test
    public void testString2() {
        assertType("'10'", "java.lang.String");
    }

    @Test
    public void testString3() {
        String contents = "def x = '10'";
        assertType(contents, contents.indexOf('\''), contents.lastIndexOf('\'')+1, "java.lang.String");
    }

    @Test
    public void testString4() {
        String contents = "def x = false ? '' : ''\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testMatcher1() {
        String contents = "def x = \"\" =~ /pattern/\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.util.regex.Matcher");
    }

    @Test
    public void testMatcher2() {
        String contents = "(\"\" =~ /pattern/).hasGroup()";
        int start = contents.indexOf("hasGroup");
        int end = start + "hasGroup".length();
        assertType(contents, start, end, "java.lang.Boolean");
    }

    @Test
    public void testPattern1() {
        String contents ="def x = ~/pattern/\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.util.regex.Pattern");
    }

    @Test
    public void testPattern2() {
        String contents ="def x = \"\" ==~ /pattern/\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Boolean");
    }

    @Test
    public void testSpread1() {
        String contents = "def z = [1,2]*.value";
        int start = contents.lastIndexOf("value");
        assertType(contents, start, start + "value".length(), "java.lang.Integer");
    }

    @Test
    public void testSpread2() {
        String contents = "[1,2,3]*.intValue()";
        int start = contents.lastIndexOf("intValue");
        assertType(contents, start, start + "intValue".length(), "java.lang.Integer");
    }

    @Test
    public void testSpread3() {
        String contents = "[1,2,3]*.intValue()[0].value";
        int start = contents.lastIndexOf("value");
        assertType(contents, start, start + "value".length(), "java.lang.Integer");
    }

    @Test
    public void testSpread4() {
        String contents = "[x:1,y:2,z:3]*.getKey()";
        int start = contents.lastIndexOf("getKey");
        assertType(contents, start, start + "getKey".length(), "java.lang.String");
    }

    @Test
    public void testSpread5() {
        String contents = "[x:1,y:2,z:3]*.getValue()";
        int start = contents.lastIndexOf("getValue");
        assertType(contents, start, start + "getValue".length(), "java.lang.Integer");
    }

    @Test
    public void testSpread6() {
        String contents = "[x:1,y:2,z:3]*.key";
        int start = contents.lastIndexOf("key");
        assertType(contents, start, start + "key".length(), "java.lang.String");
    }

    @Test
    public void testSpread7() {
        String contents = "[x:1,y:2,z:3]*.value";
        int start = contents.lastIndexOf("value");
        assertType(contents, start, start + "value".length(), "java.lang.Integer");
    }

    @Test
    public void testSpread8() {
        String contents = "[x:1,y:2,z:3]*.key[0].toLowerCase()";
        int start = contents.lastIndexOf("toLowerCase");
        assertType(contents, start, start + "toLowerCase".length(), "java.lang.String");
    }

    @Test
    public void testSpread9() {
        String contents = "[x:1,y:2,z:3]*.value[0].intValue()";
        int start = contents.lastIndexOf("intValue");
        assertType(contents, start, start + "intValue".length(), "java.lang.Integer");
    }

    @Test
    public void testSpread10() {
        String contents = "[1,2,3]*.value[0].value";
        int start = contents.lastIndexOf("value");
        assertType(contents, start, start + "value".length(), "java.lang.Integer");
    }

    @Test
    public void testSpread11() {
        String contents = "Set<String> strings = ['1','2','3'] as Set\n" +
            "strings*.bytes";
        int start = contents.lastIndexOf("bytes");
        assertType(contents, start, start + "bytes".length(), "byte[]");
    }

    @Test
    public void testSpread12() {
        String contents = "Set<String> strings = ['1','2','3'] as Set\n" +
            "strings*.length()";
        int start = contents.lastIndexOf("length");
        assertType(contents, start, start + "length".length(), "java.lang.Integer");
    }

    @Test
    public void testSpread13() {
        String contents = "@groovy.transform.TypeChecked class Foo {\n" +
            "  static def meth() {\n" +
            "    Set<java.beans.BeanInfo> beans = []\n" +
            "    beans*.additionalBeanInfo\n" +
            "  }\n" +
            "}";
        int start = contents.lastIndexOf("beans");
        assertType(contents, start, start + "beans".length(), "java.util.Set<java.beans.BeanInfo>");
        start = contents.lastIndexOf("additionalBeanInfo");
        assertType(contents, start, start + "additionalBeanInfo".length(), "java.beans.BeanInfo[]");
    }

    @Test
    public void testMapLiteral() {
        assertType("[:]", "java.util.Map<java.lang.Object,java.lang.Object>");
    }

    @Test
    public void testBoolean1() {
        assertType("!x", "java.lang.Boolean");
    }

    @Test
    public void testBoolean2() {
        String contents = "(x < y).booleanValue()";
        int start = contents.indexOf("booleanValue");
        int end = start + "booleanValue".length();
        assertType(contents, start, end, "java.lang.Boolean");
    }

    @Test
    public void testBoolean3() {
        String contents = "(x <= y).booleanValue()";
        int start = contents.indexOf("booleanValue");
        int end = start + "booleanValue".length();
        assertType(contents, start, end, "java.lang.Boolean");
    }

    @Test
    public void testBoolean4() {
        String contents = "(x >= y).booleanValue()";
        int start = contents.indexOf("booleanValue");
        int end = start + "booleanValue".length();
        assertType(contents, start, end, "java.lang.Boolean");
    }

    @Test
    public void testBoolean5() {
        String contents = "(x != y).booleanValue()";
        int start = contents.indexOf("booleanValue");
        int end = start + "booleanValue".length();
        assertType(contents, start, end, "java.lang.Boolean");
    }

    @Test
    public void testBoolean6() {
        String contents = "(x == y).booleanValue()";
        int start = contents.indexOf("booleanValue");
        int end = start + "booleanValue".length();
        assertType(contents, start, end, "java.lang.Boolean");
    }

    @Test
    public void testBoolean7() {
        String contents = "(x in y).booleanValue()";
        int start = contents.indexOf("booleanValue");
        int end = start + "booleanValue".length();
        assertType(contents, start, end, "java.lang.Boolean");
    }

    @Test
    public void testClassLiteral1() {
        String contents = "def foo = Number.class";
        int start = contents.indexOf("foo"), end = start + 3;
        assertType(contents, start, end, "java.lang.Class<java.lang.Number>");
    }

    @Test
    public void testClassLiteral2() {
        String contents = "def foo = java.lang.Number.class";
        int start = contents.indexOf("foo"), end = start + 3;
        assertType(contents, start, end, "java.lang.Class<java.lang.Number>");
    }

    @Test
    public void testClassLiteral3() {
        String contents = "def foo = Number";
        int start = contents.indexOf("foo"), end = start + 3;
        assertType(contents, start, end, "java.lang.Class<java.lang.Number>");
    }

    @Test
    public void testClassLiteral4() {
        String contents = "def foo = java.lang.Number";
        int start = contents.indexOf("foo"), end = start + 3;
        assertType(contents, start, end, "java.lang.Class<java.lang.Number>");
    }

    @Test
    public void testClassLiteral5() {
        String contents = "def foo = Map.Entry.class";
        int start = contents.indexOf("foo"), end = start + 3;
        assertType(contents, start, end, "java.lang.Class<java.util.Map$Entry>");
    }

    @Test // GRECLIPSE-1229: constructors with map parameters
    public void testConstructor1() {
        String contents =
            "class O {\n" +
            "  boolean aa\n" +
            "  int bb\n" +
            "}\n" +
            "new O(aa: 1, bb:8)";

        int start = contents.lastIndexOf("aa");
        int end = start + "aa".length();
        assertType(contents, start, end, "java.lang.Boolean");
        assertDeclaration(contents, start, end, "O", "aa", DeclarationKind.PROPERTY);

        start = contents.lastIndexOf("bb");
        end = start + "bb".length();
        assertType(contents, start, end, "java.lang.Integer");
        assertDeclaration(contents, start, end, "O", "bb", DeclarationKind.PROPERTY);
    }

    @Test
    public void testConstructor2() {
        String contents =
            "class O {\n" +
            "  boolean aa\n" +
            "  int bb\n" +
            "}\n" +
            "new O([aa: 1, bb:8])";

        int start = contents.lastIndexOf("aa");
        int end = start + "aa".length();
        assertType(contents, start, end, "java.lang.Boolean");
        assertDeclaration(contents, start, end, "O", "aa", DeclarationKind.PROPERTY);

        start = contents.lastIndexOf("bb");
        end = start + "bb".length();
        assertType(contents, start, end, "java.lang.Integer");
        assertDeclaration(contents, start, end, "O", "bb", DeclarationKind.PROPERTY);
    }

    @Test
    public void testConstructor3() {
        String contents =
            "class O {\n" +
            "  boolean aa\n" +
            "  int bb\n" +
            "}\n" +
            "new O([8: 1, bb:8])";

        int start = contents.lastIndexOf("bb");
        int end = start + "bb".length();
        assertType(contents, start, end, "java.lang.Integer");
        assertDeclaration(contents, start, end, "O", "bb", DeclarationKind.PROPERTY);
    }

    @Test
    public void testConstructor4() {
        String contents =
            "class O {\n" +
            "  boolean aa\n" +
            "  int bb\n" +
            "}\n" +
            "new O([aa: 1, bb:8], 9)";

        int start = contents.lastIndexOf("aa");
        int end = start + "aa".length();
        assertType(contents, start, end, "java.lang.String");
        assertDeclaringType(contents, start, end, "null");

        start = contents.lastIndexOf("bb");
        end = start + "bb".length();
        assertType(contents, start, end, "java.lang.String");
        assertDeclaringType(contents, start, end, "null");
    }

    @Test
    public void testConstructor5() {
        String contents =
            "class O {\n" +
            "  boolean aa\n" +
            "  int bb\n" +
            "}\n" +
            "new O(9, [aa: 1, bb:8])";

        int start = contents.lastIndexOf("aa");
        int end = start + "aa".length();
        assertType(contents, start, end, "java.lang.String");
        assertDeclaringType(contents, start, end, "null");

        start = contents.lastIndexOf("bb");
        end = start + "bb".length();
        assertType(contents, start, end, "java.lang.String");
        assertDeclaringType(contents, start, end, "null");
    }

    @Test
    public void testConstructor6() {
        String contents =
            "class O {\n" +
            "  boolean aa\n" +
            "  int bb\n" +
            "}\n" +
            "def g = [aa: 1, bb:8]";

        int start = contents.lastIndexOf("aa");
        int end = start + "aa".length();
        assertType(contents, start, end, "java.lang.String");
        assertDeclaringType(contents, start, end, "null");

        start = contents.lastIndexOf("bb");
        end = start + "bb".length();
        assertType(contents, start, end, "java.lang.String");
        assertDeclaringType(contents, start, end, "null");
    }

    @Test
    public void testSpecialConstructor1() {
        String contents =
            "class C {\n" +
            "  C() {\n" +
            "    this()\n" +
            "  }\n" +
            "}";

        int start = contents.indexOf("this()");
        int end = start + "this(".length();
        assertType(contents, start, end, "java.lang.Object");
        assertDeclaringType(contents, start, end, "C");
    }

    @Test
    public void testSpecialConstructor2() {
        String contents =
            "class C extends HashMap {\n" +
            "  C() {\n" +
            "    super()\n" +
            "  }\n" +
            "}";

        int start = contents.indexOf("super()");
        int end = start + "super(".length();
        assertType(contents, start, end, "java.lang.Object");
        assertDeclaringType(contents, start, end, "java.util.HashMap");
    }

    @Test
    public void testStaticMethodCall() {
        String contents = "Two.x()\n class Two {\n static String x() {\n \"\" } } ";
        String expr = "x";
        assertType(contents, contents.indexOf(expr), contents.indexOf(expr)+expr.length(), "java.lang.String");
    }

    @Test
    public void testStaticMethodCall2() {
        String contents = "Two.x\n class Two {\n static String x() {\n \"\" } } ";
        String expr = "x";
        assertType(contents, contents.indexOf(expr), contents.indexOf(expr)+expr.length(), "java.lang.String");
    }

    @Test
    public void testStaticMethodCall3() {
        String contents = "class Two {\n def other() { \n x() } \n static String x() {\n \"\" } } ";
        String expr = "x() ";  // extra space b/c static method call expression end offset is wrong
        assertType(contents, contents.indexOf(expr), contents.indexOf(expr)+expr.length(), "java.lang.String");
    }

    @Test
    public void testStaticMethodCall4() {
        String contents = "class Two {\n def other() { \n x } \n static String x() {\n \"\" } } ";
        String expr = "x";
        assertType(contents, contents.indexOf(expr), contents.indexOf(expr)+expr.length(), "java.lang.String");
    }

    @Test // GRECLISPE-1244
    public void testStaticMethodCall5() {
        String contents =
            "class Parent {\n" +
            "    static p() {}\n" +
            "}\n" +
            "class Child extends Parent {\n" +
            "    def c() {\n" +
            "        p()\n" +
            "    }\n" +
            "}";
        String expr = "p()";
        assertDeclaringType(contents, contents.lastIndexOf(expr), contents.lastIndexOf(expr)+expr.length(), "Parent");
    }

    @Test // GRECLISPE-1244
    public void testStaticMethodCall6() {
        createUnit("Parent",
            "class Parent {\n" +
            "    static p() {}\n" +
            "}");
        String contents =
            "class Child extends Parent {\n" +
            "    def c() {\n" +
            "        p()\n" +
            "    }\n" +
            "}";
        String expr = "p()";
        assertDeclaringType(contents, contents.lastIndexOf(expr), contents.lastIndexOf(expr)+expr.length(), "Parent");
    }

    @Test
    public void testStaticMethodCall7() throws Exception {
        createUnit("foo", "Bar", "package foo\n" +
            "import java.util.regex.*\n" +
            "class Bar {\n" +
            "  static Object meth(Object o) { return o }\n" +
            "  static Pattern meth(Pattern p) { return p }\n" +
            "  static Collection meth(Collection c) { return c }\n" +
            "}");

        String staticCall = "meth([])";
        String contents = "import static foo.Bar.*; " + staticCall;
        assertType(contents, contents.indexOf(staticCall), contents.indexOf(staticCall) + staticCall.length(), "java.util.Collection");
    }

    @Test
    public void testStaticMethodCall8() throws Exception {
        createUnit("foo", "Bar", "package foo\n" +
            "import java.util.regex.*\n" +
            "class Bar {\n" +
            "  static Object meth(Object o) { return o }\n" +
            "  static Pattern meth(Pattern p) { return p }\n" +
            "  static Collection meth(Collection c) { return c }\n" +
            "}");

        String staticCall = "meth(~/abc/)";
        String contents = "import static foo.Bar.*; " + staticCall;
        assertType(contents, contents.indexOf(staticCall), contents.indexOf(staticCall) + staticCall.length(), "java.util.regex.Pattern");
    }

    @Test
    public void testStaticMethodCall9() throws Exception {
        createUnit("foo", "Bar", "package foo\n" +
            "import java.util.regex.*\n" +
            "class Bar {\n" +
            "  static Object meth(Object o) { return o }\n" +
            "  static Pattern meth(Pattern p) { return p }\n" +
            "  static Collection meth(Collection c) { return c }\n" +
            "}");

        String staticCall = "meth(compile('abc'))";
        String contents = "import static foo.Bar.*; import static java.util.regex.Pattern.*; " + staticCall;
        assertType(contents, contents.indexOf(staticCall), contents.indexOf(staticCall) + staticCall.length(), "java.util.regex.Pattern");
    }

    @Test
    public void testStaticThisAndSuper() {
        String contents =
            "class A {\n" +
            "  static main(args) {\n" +
            "    this\n" +
            "    super\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "this", "java.lang.Class<A>");
        assertExprType(contents, "super", "java.lang.Object");
    }

    @Test
    public void testStaticThisAndSuper2() {
        String contents =
            "class A { }\n" +
            "class B extends A {\n" +
            "  static main(args) {\n" +
            "    this\n" +
            "    super\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "this", "java.lang.Class<B>");
        assertExprType(contents, "super", "java.lang.Class<A>");
    }

    @Test
    public void testSuperFieldReference() {
        String contents = "class B extends A {\n def other() { \n myOther } } \n class A { String myOther } ";
        String expr = "myOther";
        assertType(contents, contents.indexOf(expr), contents.indexOf(expr)+expr.length(), "java.lang.String");
    }

    @Test
    public void testSuperClassMethod() {
        String contents = "class A { void m() { } }\n" + "class B extends A { }\n" + "new B().m()";
        int offset = contents.lastIndexOf('m');
        assertDeclaringType(contents, offset, offset + 1, "A");
    }

    @Test
    public void testSuperClassMethod2() {
        String contents = "class A { void m(Runnable r) { } }\n" + "class B extends A { }\n" + "new B().m() { -> }";
        int offset = contents.lastIndexOf('m');
        assertDeclaringType(contents, offset, offset + 1, "A");
    }

    @Test
    public void testFieldWithInitializer1() {
        String contents = "class A {\ndef x = 9\n}\n new A().x";
        int offset = contents.lastIndexOf('x');
        assertType(contents, offset, offset + 1, "java.lang.Integer");
    }

    @Test
    public void testFieldWithInitializer2() {
        createUnit("A", "class A {\ndef x = 9\n}");
        String contents = "new A().x";
        int start = contents.lastIndexOf('x');
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testTernaryExpression() {
        String contents = "def x = true ? 2 : 1\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testElvisOperator() {
        String contents = "def x = 2 ?: 1\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testRangeExpression1() {
        String contents = "def x = 0 .. 5\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "groovy.lang.Range<java.lang.Integer>");
    }

    @Test
    public void testRangeExpression2() {
        String contents = "def x = 0 ..< 5\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "groovy.lang.Range<java.lang.Integer>");
    }

    @Test
    public void testRangeExpression3() {
        String contents = "(1..10).getFrom()";
        int start = contents.lastIndexOf("getFrom");
        int end = start + "getFrom".length();
        assertType(contents, start, end, "java.lang.Comparable<java.lang.Integer>");
    }

    @Test
    public void testRangeExpression4() {
        String contents = "(1..10).getTo()";
        int start = contents.lastIndexOf("getTo");
        int end = start + "getTo".length();
        assertType(contents, start, end, "java.lang.Comparable<java.lang.Integer>");
    }

    @Test
    public void testRangeExpression5() {
        String contents = "(1..10).step(0)";
        int start = contents.lastIndexOf("step");
        int end = start + "step".length();
        assertType(contents, start, end, "java.util.List<java.lang.Integer>");
    }

    @Test
    public void testRangeExpression6() {
        String contents = "(1..10).step(0, { })";
        int start = contents.lastIndexOf("step");
        int end = start + "step".length();
        assertType(contents, start, end, "java.lang.Void");
    }

    @Test
    public void testInnerClass1() {
        String contents = "class Outer { class Inner { } \nInner x }\nnew Outer().x ";
        int start = contents.lastIndexOf("x");
        int end = start + 1;
        assertType(contents, start, end, "Outer$Inner");
    }

    @Test
    public void testInnerClass2() {
        String contents = "class Outer { class Inner { class InnerInner{ } }\n Outer.Inner.InnerInner x }\nnew Outer().x ";
        int start = contents.lastIndexOf("x");
        int end = start + 1;
        assertType(contents, start, end, "Outer$Inner$InnerInner");
    }

    @Test
    public void testInnerClass3() {
        String contents = "class Outer { class Inner { def z() { \nnew Outer().x \n } } \nInner x }";
        int start = contents.indexOf("x");
        int end = start + 1;
        assertType(contents, start, end, "Outer$Inner");
    }

    @Test
    public void testInnerClass4() {
        String contents = "class Outer { class Inner { class InnerInner { def z() { \nnew Outer().x \n } } } \nInner x }";
        int start = contents.indexOf("x");
        int end = start + 1;
        assertType(contents, start, end, "Outer$Inner");
    }

    @Test
    public void testInnerClass5() {
        String contents = "class Outer { class Inner extends Outer { } }";
        int start = contents.lastIndexOf("Outer");
        int end = start + "Outer".length();
        assertType(contents, start, end, "Outer");
    }

    @Test
    public void testInnerClass6() {
        String contents = "class Outer extends RuntimeException { class Inner { def foo() throws Outer { } } }";
        int start = contents.lastIndexOf("Outer");
        int end = start + "Outer".length();
        assertType(contents, start, end, "Outer");
    }

    @Test
    public void testInnerClass7() {
        String contents = "class A {\n" +
            "  protected Number f\n" +
            "  protected Number m() { }\n" +
            "}\n" +
            "class AA extends A {\n" +
            "  class AAA {\n" +
            "    def x() {\n" +
            "      f  \n" +
            "      m()\n" +
            "    }\n" +
            "  }\n" +
            "}";
        int offset = contents.lastIndexOf('f');
        assertDeclaringType(contents, offset, offset + 1, "A");
        assertType(contents, offset, offset + 1, "java.lang.Number");
            offset = contents.lastIndexOf('m');
        assertDeclaringType(contents, offset, offset + 1, "A");
        assertType(contents, offset, offset + 1, "java.lang.Number");
    }

    @Test
    public void testInnerClass8() {
        String contents = "class A {\n" +
            "  protected Number f\n" +
            "  protected Number m() { }\n" +
            "}\n" +
            "class AA extends A {\n" +
            "  static class AAA {\n" +
            "    def x() {\n" +
            "      f  \n" +
            "      m()\n" +
            "    }\n" +
            "  }\n" +
            "}";
        int offset = contents.lastIndexOf('f');
        assertType(contents, offset, offset + 1, "java.lang.Object");
        assertUnknownConfidence(contents, offset, offset + 1, "A", false);
            offset = contents.lastIndexOf('m');
        assertType(contents, offset, offset + 1, "java.lang.Object");
        assertUnknownConfidence(contents, offset, offset + 1, "A", false);
    }

    @Test
    public void testInnerClass9() {
        String contents = "class A {\n" +
            "  public static final Number N = 1\n" +
            "}\n" +
            "class AA extends A {\n" +
            "  static class AAA {\n" +
            "    def x() {\n" +
            "      N\n" +
            "    }\n" +
            "  }\n" +
            "}";
        int offset = contents.lastIndexOf('N');
        assertDeclaringType(contents, offset, offset + 1, "A");
        assertType(contents, offset, offset + 1, "java.lang.Number");
    }

    @Test
    public void testInnerClass10() {
        String contents = "class A {\n" +
            "  public static final Number N = 1\n" +
            "}\n" +
            "class AA extends A {\n" +
            "  static class AAA {\n" +
            "    def x() {\n" +
            "      def cl = { ->\n" +
            "        N\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
        int offset = contents.lastIndexOf('N');
        assertDeclaringType(contents, offset, offset + 1, "A");
        assertType(contents, offset, offset + 1, "java.lang.Number");
    }

    @Test
    public void testInnerClass11() {
        String contents = "class A {\n" +
            "  public static final Number N = 1\n" +
            "}\n" +
            "class AA extends A {\n" +
            "  static class AAA {\n" +
            "    static def x() {\n" +
            "      def cl = { ->\n" +
            "        N\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
        int offset = contents.lastIndexOf('N');
        assertDeclaringType(contents, offset, offset + 1, "A");
        assertType(contents, offset, offset + 1, "java.lang.Number");
    }

    @Test
    public void testAnonInner1() {
        String contents = "def foo = new Runnable() { void run() {} }";
        int start = contents.lastIndexOf("Runnable");
        int end = start + "Runnable".length();
        assertType(contents, start, end, "java.lang.Runnable");
    }

    @Test
    public void testAnonInner2() {
        String contents = "def foo = new Comparable<String>() { int compareTo(String a, String b) {} }";
        int start = contents.lastIndexOf("Comparable");
        int end = start + "Comparable".length();
        assertType(contents, start, end, "java.lang.Comparable<java.lang.String>");
    }

    @Test
    public void testAnonInner3() {
        String contents = "def foo = new Comparable<String>() { int compareTo(String a, String b) { compareTo()} }";
        int start = contents.lastIndexOf("compareTo");
        int end = start + "compareTo".length();
        assertDeclaringType(contents, start, end, "Search$1");
    }

    @Test
    public void testAnonInner4() {
        String contents = "def foo = new Comparable<String>() { int compareTo(String a, String b) {} }\n" +
            "foo.compareTo";
        int start = contents.lastIndexOf("compareTo");
        int end = start + "compareTo".length();
        assertDeclaringType(contents, start, end, "Search$1");
    }

    @Test
    public void testAnonInner5() {
        String contents = "def foo = new Comparable<String>() { int compareTo(String a, String b) {} }\n" +
            "foo = new Comparable<String>() { int compareTo(String a, String b) {} }\n" +
            "foo.compareTo";
        int start = contents.lastIndexOf("compareTo");
        int end = start + "compareTo".length();
        assertDeclaringType(contents, start, end, "Search$2");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/378
    public void testAnonInner6() {
        String contents =
            "class A {\n" +
            "  protected def f\n" +
            "  protected def m() { }\n" +
            "}\n" +
            "class AA extends A {\n" +
            "  void init() {\n" +
            "    def whatever = new Object() {\n" +
            "      def something() {\n" +
            "        f  \n" +
            "        m()\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
        int offset = contents.lastIndexOf('f');
        assertDeclaringType(contents, offset, offset + 1, "A");
            offset = contents.lastIndexOf('m');
        assertDeclaringType(contents, offset, offset + 1, "A");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/383
    public void testAnonInner7() {
        String contents =
            "class A {\n" +
            "  protected def m() { }\n" +
            "  def p = new Object() {\n" +
            "    def meth() {\n" +
            "      m();\n" +
            "      p  ;\n" +
            "    }\n" +
            "  }\n" +
            "  void init() {\n" +
            "    def whatever = new Object() {\n" +
            "      def something() {\n" +
            "        m()\n" +
            "        p  \n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
        int offset = contents.indexOf("m();");
        assertDeclaringType(contents, offset, offset + 1, "A");
            offset = contents.indexOf("p  ;");
        assertDeclaringType(contents, offset, offset + 1, "A");
            offset = contents.lastIndexOf("m");
        assertDeclaringType(contents, offset, offset + 1, "A");
            offset = contents.lastIndexOf("p");
        assertDeclaringType(contents, offset, offset + 1, "A");
    }

    @Test
    public void testConstantFromSuper() {
        String contents =
            "public interface Constants {\n" +
            "  int FIRST = 9;\n" +
            "}\n" +
            "class UsesConstants implements Constants {\n" +
            "  def x() {\n" +
            "    FIRST\n" +
            "  }\n" +
            "}";
        int start = contents.lastIndexOf("FIRST");
        int end = start + "FIRST".length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testAssignementInInnerBlock() {
        String contents = "def xxx\n if (true) { xxx = \"\" \n xxx} ";
        int start = contents.lastIndexOf("xxx");
        int end = start + "xxx".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testAssignementInInnerBlock2() {
        String contents = "def xxx\n if (true) { xxx = \"\" \n }\n xxx";
        int start = contents.lastIndexOf("xxx");
        int end = start + "xxx".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testGRECLIPSE731a() {
        String contents = "def foo() { } \nString xxx = foo()\nxxx";
        int start = contents.lastIndexOf("xxx");
        int end = start + "xxx".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testGRECLIPSE731b() {
        String contents = "def foo() { } \ndef xxx = foo()\nxxx";
        int start = contents.lastIndexOf("xxx");
        int end = start + "xxx".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test
    public void testGRECLIPSE731c() {
        String contents = "String foo() { } \ndef xxx = foo()\nxxx";
        int start = contents.lastIndexOf("xxx");
        int end = start + "xxx".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testGRECLIPSE731d() {
        String contents = "int foo() { } \ndef xxx = foo()\nxxx";
        int start = contents.lastIndexOf("xxx");
        int end = start + "xxx".length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testGRECLIPSE731e() {
        // ignore assignments to object expressions
        String contents = "def foo() { } \nString xxx\nxxx = foo()\nxxx";
        int start = contents.lastIndexOf("xxx");
        int end = start + "xxx".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testGRECLIPSE731f() {
        // ignore assignments to object expressions
        String contents = "class X { String xxx\ndef foo() { }\ndef meth() { xxx = foo()\nxxx } }";
        int start = contents.lastIndexOf("xxx");
        int end = start + "xxx".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testCatchBlock1() {
        String catchString = "try {     } catch (NullPointerException e) { e }";
        int start = catchString.lastIndexOf("NullPointerException");
        int end = start + "NullPointerException".length();
        assertType(catchString, start, end, "java.lang.NullPointerException");
    }

    @Test
    public void testCatchBlock2() {
        String catchString = "try {     } catch (NullPointerException e) { e }";
        int start = catchString.lastIndexOf("e");
        int end = start + 1;
        assertType(catchString, start, end, "java.lang.NullPointerException");
    }

    @Test
    public void testCatchBlock3() {
        String catchString = "try {     } catch (NullPointerException e) { e }";
        int start = catchString.indexOf("NullPointerException e");
        int end = start + ("NullPointerException e").length();
        assertType(catchString, start, end, "java.lang.NullPointerException");
    }

    @Test
    public void testCatchBlock4() {
        String catchString2 = "try {     } catch (e) { e }";
        int start = catchString2.indexOf("e");
        int end = start + 1;
        assertType(catchString2, start, end, "java.lang.Exception");
    }

    @Test
    public void testCatchBlock5() {
        String catchString2 = "try {     } catch (e) { e }";
        int start = catchString2.lastIndexOf("e");
        int end = start + 1;
        assertType(catchString2, start, end, "java.lang.Exception");
    }

    @Test
    public void testAssignment1() {
        String contents = "String x = 7\nx";
        int start = contents.lastIndexOf("x");
        int end = start + 1;
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testAssignment2() {
        String contents = "String x\nx";
        int start = contents.lastIndexOf("x");
        int end = start + 1;
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testAssignment3() {
        String contents = "String x\nx = 7\nx"; // will be a GroovyCastException at runtime
        int start = contents.lastIndexOf("x");
        int end = start + 1;
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testAssignment4() {
        String contents = "String x() { \ndef x = 9\n x}";
        int start = contents.lastIndexOf("x");
        int end = start + 1;
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testAssignment5() {
        String contents = "String x() { \ndef x\nx = 9\n x}";
        int start = contents.lastIndexOf("x");
        int end = start + 1;
        assertType(contents, start, end, "java.lang.Integer");
    }

    @Test
    public void testScriptDeclaringType() {
        String contents = "other\n";
        int start = contents.lastIndexOf("other");
        int end = start + "other".length();
        assertDeclaringType(contents, start, end, "Search", false, true);
    }

    @Test
    public void testStaticImports1() {
        String contents = "import static javax.swing.text.html.HTML.NULL_ATTRIBUTE_VALUE\n" +
                          "NULL_ATTRIBUTE_VALUE";
        int start = contents.lastIndexOf("NULL_ATTRIBUTE_VALUE");
        int end = start + "NULL_ATTRIBUTE_VALUE".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testStaticImports2() {
        String contents = "import static javax.swing.text.html.HTML.getAttributeKey\n" +
                          "getAttributeKey('')";
        int start = contents.lastIndexOf("getAttributeKey");
        int end = start + "getAttributeKey('')".length();
        assertType(contents, start, end, "javax.swing.text.html.HTML$Attribute");
    }

    @Test
    public void testStaticImports3() {
        String contents = "import static javax.swing.text.html.HTML.*\n" +
                          "NULL_ATTRIBUTE_VALUE";
        int start = contents.lastIndexOf("NULL_ATTRIBUTE_VALUE");
        int end = start + "NULL_ATTRIBUTE_VALUE".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testStaticImports4() {
        String contents = "import static javax.swing.text.html.HTML.*\n" +
                          "getAttributeKey('')";
        int start = contents.lastIndexOf("getAttributeKey");
        int end = start + "getAttributeKey('')".length();
        assertType(contents, start, end, "javax.swing.text.html.HTML$Attribute");
    }

    @Test
    public void testDGM1() {
        String contents = "\"$print\"";
        String lookFor = "print";
        int start = contents.indexOf(lookFor);
        int end = start + lookFor.length();
        assertDeclaringType(contents, start, end, "groovy.lang.Script");
    }

    @Test
    public void testDGM2() {
        String contents = "\"${print}\"";
        String lookFor = "print";
        int start = contents.indexOf(lookFor);
        int end = start + lookFor.length();
        assertDeclaringType(contents, start, end, "groovy.lang.Script");
    }

    @Test
    public void testDGM3() {
        String contents = "class Foo {\n def m() {\n \"${print()}\"\n } }";
        String lookFor = "print";
        int start = contents.indexOf(lookFor);
        int end = start + lookFor.length();
        assertDeclaringType(contents, start, end, "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    private static final String CONTENTS_GETAT1 =
        "class GetAt {\n" +
        "  String getAt(foo) { }\n" +
        "}\n" +
        "\n" +
        "new GetAt()[0].startsWith()\n" +
        "GetAt g\n" +
        "g[0].startsWith()";

    private static final String CONTENTS_GETAT2 =
        "class GetAt {\n" +
        "}\n" +
        "\n" +
        "new GetAt()[0].startsWith()\n" +
        "GetAt g\n" +
        "g[0].startsWith()";

    @Test
    public void testGetAt1() {
        int start = CONTENTS_GETAT1.indexOf("startsWith");
        int end = start + "startsWith".length();
        assertDeclaringType(CONTENTS_GETAT1, start, end, "java.lang.String");
    }

    @Test
    public void testGetAt2() {
        int start = CONTENTS_GETAT1.lastIndexOf("startsWith");
        int end = start + "startsWith".length();
        assertDeclaringType(CONTENTS_GETAT1, start, end, "java.lang.String");
    }

    @Test
    public void testGetAt3() {
        int start = CONTENTS_GETAT2.indexOf("startsWith");
        int end = start + "startsWith".length();
        // expecting unknown confidence because getAt not explicitly defined
        assertDeclaringType(CONTENTS_GETAT2, start, end, "GetAt", false, true);
    }

    @Test
    public void testGetAt4() {
        int start = CONTENTS_GETAT2.lastIndexOf("startsWith");
        int end = start + "startsWith".length();
        // expecting unknown confidence because getAt not explicitly defined
        assertDeclaringType(CONTENTS_GETAT2, start, end, "GetAt", false, true);
    }

    @Test // GRECLIPSE-743
    public void testGetAt5() {
        String contents = "class A { }\n new A().getAt() ";
        int start = contents.lastIndexOf("getAt");
        int end = start + "getAt".length();
        assertType(contents, start, end, "java.lang.Object");
        assertDeclaringType(contents, start, end, "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test
    public void testGetAt6() {
        String contents = "class A {\n A getAt(prop) { \n new A() \n } }\n new A().getAt('x')";
        int start = contents.lastIndexOf("getAt");
        int end = start + "getAt".length();
        assertType(contents, start, end, "A");
        assertDeclaringType(contents, start, end, "A");
    }

    @Test
    public void testGetAt7() {
        String contents = "class A {\n A getAt(prop) { \n new A() \n } }\n class B extends A { }\n new B().getAt('x')";
        int start = contents.lastIndexOf("getAt");
        int end = start + "getAt".length();
        assertType(contents, start, end, "A");
        assertDeclaringType(contents, start, end, "A");
    }

    @Test
    public void testListSort1() {
        String contents = "def list = []; list.sort()";
        int offset = contents.lastIndexOf("sort");
        assertType(contents, offset, offset + 4, "java.util.List<java.lang.Object>");
        assertDeclaringType(contents, offset, offset + 4, "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/387
    public void testListSort2() {
        String contents = "def list = []; list.sort { a, b -> a <=> b }";
        int offset = contents.lastIndexOf("sort");
        assertType(contents, offset, offset + 4, "java.util.List<java.lang.Object>");
        MethodNode m = assertDeclaration(contents, offset, offset + 4, "org.codehaus.groovy.runtime.DefaultGroovyMethods", "sort", DeclarationKind.METHOD);
        assertEquals("Should resolve to sort(Iterable,Closure) since Collection version is deprecated", "java.lang.Iterable<java.lang.Object>", printTypeName(m.getParameters()[0].getType()));
    }

    @Test
    public void testListSort3() {
        // Java 8 adds default method sort(Comparator) to the List interface
        boolean jdkListSort;
        try {
            List.class.getDeclaredMethod("sort", Comparator.class);
            jdkListSort = true;
        } catch (Exception e) {
            jdkListSort = false;
        }

        String contents = "def list = []; list.sort({ a, b -> a <=> b } as Comparator)";
        int offset = contents.lastIndexOf("sort");
        assertType(contents, offset, offset + 4, jdkListSort ? "java.lang.Void" : "java.util.List<java.lang.Object>");
        assertDeclaringType(contents, offset, offset + 4, jdkListSort ? "java.util.List<java.lang.Object>" : "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/368
    public void testListRemove() {
        String contents =
            "class Util {\n" +
            "  static int remove(List<?> list, Object item) {\n" +
            "    //...\n" +
            "  }\n" +
            "  void doSomething() {\n" +
            "    List a = []\n" +
            "    List b = []\n" +
            "    // List.remove(int) or List.remove(Object)\n" +
            "    a.remove(Util.remove(b, ''))\n" +
            "  }\n" +
            "}";
        int offset = contents.indexOf("a.remove") + 2;
        MethodNode m = assertDeclaration(contents, offset, offset + "remove".length(), "java.util.List", "remove", DeclarationKind.METHOD);
        assertEquals("Should resolve to remove(int) due to return type of inner call", "int", printTypeName(m.getParameters()[0].getType()));
    }

    @Test // GRECLIPSE-1013
    public void testCategoryMethodAsProperty() {
        String contents = "''.toURL().text";
        int start = contents.indexOf("text");
        assertDeclaringType(contents, start, start + 4, "org.codehaus.groovy.runtime.ResourceGroovyMethods");
    }

    @Test
    public void testInterfaceMethodsAsProperty() throws Exception {
        createUnit("foo", "Bar", "package foo; interface Bar { def getOne() }");
        createUnit("foo", "Baz", "package foo; interface Baz extends Bar { def getTwo() }");

        String contents = "def meth(foo.Baz b) { b.one + b.two }";

        int start = contents.indexOf("one");
        assertDeclaringType(contents, start, start + 3, "foo.Bar");
        start = contents.indexOf("two");
        assertDeclaringType(contents, start, start + 3, "foo.Baz");
    }

    @Test
    public void testInterfaceMethodAsProperty2() throws Exception {
        createUnit("foo", "Bar", "package foo; interface Bar { def getOne() }");
        createUnit("foo", "Baz", "package foo; abstract class Baz implements Bar { abstract def getTwo() }");

        String contents = "def meth(foo.Baz b) { b.one + b.two }";

        int start = contents.indexOf("one");
        assertDeclaringType(contents, start, start + 3, "foo.Bar");
        start = contents.indexOf("two");
        assertDeclaringType(contents, start, start + 3, "foo.Baz");
    }

    @Test
    public void testInterfaceMethodAsProperty3() throws Exception {
        createUnit("foo", "Bar", "package foo; interface Bar { def getOne() }");
        createUnit("foo", "Baz", "package foo; abstract class Baz implements Bar { abstract def getTwo() }");

        String contents = "abstract class C extends foo.Baz { }\n def meth(C c) { c.one + c.two }";

        int start = contents.indexOf("one");
        assertDeclaringType(contents, start, start + 3, "foo.Bar");
        start = contents.indexOf("two");
        assertDeclaringType(contents, start, start + 3, "foo.Baz");
    }

    @Test
    public void testIndirectInterfaceMethod() throws Exception {
        createUnit("foo", "Bar", "package foo; interface Bar { def getOne() }");
        createUnit("foo", "Baz", "package foo; abstract class Baz implements Bar { abstract def getTwo() }");

        String contents = "abstract class C extends foo.Baz { }\n def meth(C c) { c.getOne() + c.getTwo() }";

        int start = contents.indexOf("getOne");
        assertDeclaringType(contents, start, start + 6, "foo.Bar");
        start = contents.indexOf("getTwo");
        assertDeclaringType(contents, start, start + 6, "foo.Baz");
    }

    @Test
    public void testIndirectInterfaceConstant() throws Exception {
        createUnit("I", "interface I { Number ONE = 1 }");
        createUnit("A", "abstract class A implements I { Number TWO = 2 }");

        String contents = "abstract class B extends A { }\n B b; b.ONE; b.TWO";

        int start = contents.indexOf("ONE");
        assertDeclaringType(contents, start, start + 3, "I");
        start = contents.indexOf("TWO");
        assertDeclaringType(contents, start, start + 3, "A");
    }

    @Test
    public void testObjectMethodOnInterface() {
        // Object is not in explicit type hierarchy of List
        String contents = "def meth(List list) { list.getClass() }";

        String target = "getClass", source = "java.lang.Object";
        assertDeclaringType(contents, contents.indexOf(target), contents.indexOf(target) + target.length(), source);
    }

    @Test
    public void testObjectMethodOnInterfaceAsProperty() {
        // Object is not in explicit type hierarchy of List
        String contents = "def meth(List list) { list.class }";

        String target = "class", source = "java.lang.Object";
        assertDeclaringType(contents, contents.indexOf(target), contents.indexOf(target) + target.length(), source);
    }

    @Test
    public void testClassReference1() {
        String contents = "String.substring";
        int textStart = contents.indexOf("substring");
        int textEnd = textStart + "substring".length();
        assertDeclaringType(contents, textStart, textEnd, "java.lang.String", false, true);
    }

    @Test
    public void testClassReference2() {
        String contents = "String.getPackage()";
        int textStart = contents.indexOf("getPackage");
        int textEnd = textStart + "getPackage".length();
        assertType(contents, textStart, textEnd, "java.lang.Package");
    }

    @Test
    public void testClassReference3() {
        String contents = "String.class.getPackage()";
        int textStart = contents.indexOf("getPackage");
        int textEnd = textStart + "getPackage".length();
        assertType(contents, textStart, textEnd, "java.lang.Package");
    }

    @Test
    public void testClassReference4() {
        String contents = "String.class.package";
        int textStart = contents.indexOf("package");
        int textEnd = textStart + "package".length();
        assertType(contents, textStart, textEnd, "java.lang.Package");
    }

    @Test
    public void testMultiDecl1() {
        String contents = "def (x, y) = []\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart + 1, "java.lang.Object");
        assertType(contents, yStart, yStart + 1, "java.lang.Object");
    }

    @Test
    public void testMultiDecl2() {
        String contents = "def (x, y) = [1]\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Integer");
        assertType(contents, yStart, yStart+1, "java.lang.Integer");
    }

    @Test
    public void testMultiDecl3() {
        String contents = "def (x, y) = [1,1]\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Integer");
        assertType(contents, yStart, yStart+1, "java.lang.Integer");
    }

    @Test
    public void testMultiDecl4() {
        String contents = "def (x, y) = [1,'']\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Integer");
        assertType(contents, yStart, yStart+1, "java.lang.String");
    }

    @Test
    public void testMultiDecl6() {
        String contents = "def (x, y) = new ArrayList()\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Object");
        assertType(contents, yStart, yStart+1, "java.lang.Object");
    }

    @Test
    public void testMultiDecl7() {
        String contents = "def (x, y) = new ArrayList<Double>()\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Double");
        assertType(contents, yStart, yStart+1, "java.lang.Double");
    }

    @Test
    public void testMultiDecl8() {
        String contents = "Double[] meth() { }\ndef (x, y) = meth()\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Double");
        assertType(contents, yStart, yStart+1, "java.lang.Double");
    }

    @Test
    public void testMultiDecl9() {
        String contents = "List<Double> meth() { }\ndef (x, y) = meth()\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Double");
        assertType(contents, yStart, yStart+1, "java.lang.Double");
    }

    @Test
    public void testMultiDecl10() {
        String contents = "List<Double> field\ndef (x, y) = field\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Double");
        assertType(contents, yStart, yStart+1, "java.lang.Double");
    }

    @Test
    public void testMultiDecl11() {
        String contents = "List<Double> field\ndef x\ndef y\n (x, y)= field\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Double");
        assertType(contents, yStart, yStart+1, "java.lang.Double");
    }

    @Test
    public void testMultiDecl12() {
        String contents = "def (x, y) = 1d\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Double");
        assertType(contents, yStart, yStart+1, "java.lang.Double");
    }

    @Test
    public void testMultiDecl13() {
        String contents = "def (int x, float y) = [1,2]\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Integer");
        assertType(contents, yStart, yStart+1, "java.lang.Float");
    }

    @Test // GRECLIPSE-1174 groovy casting
    public void testAsExpression1() {
        String contents = "(1 as int).intValue()";
        int start = contents.lastIndexOf("intValue");
        assertType(contents, start, start+"intValue".length(), "java.lang.Integer");
    }

    @Test // GRECLIPSE-1174 groovy casting
    public void testAsExpression2() {
        String contents = "class Flar { int x\n }\n(null as Flar).x";
        int start = contents.lastIndexOf("x");
        assertType(contents, start, start+"x".length(), "java.lang.Integer");
    }

    @Test // GRECLIPSE-1264
    public void testImplicitVar1() {
        String contents = "class SettingUndeclaredProperty {\n" +
            "    public void mymethod() {\n" +
            "        doesNotExist = \"abc\"\n" +
            "    }\n" +
            "}";
        int start = contents.lastIndexOf("doesNotExist");
        assertUnknownConfidence(contents, start, start+"doesNotExist".length(), "SettingUndeclaredProperty", false);
    }

    @Test // GRECLIPSE-1264
    public void testImplicitVar2() {
        String contents = "class SettingUndeclaredProperty {\n" +
            "     def r = {\n" +
            "        doesNotExist = 0\n" +
            "    }\n" +
            "}";
        int start = contents.lastIndexOf("doesNotExist");
        assertUnknownConfidence(contents, start, start+"doesNotExist".length(), "SettingUndeclaredProperty", false);
    }

    @Test // GRECLIPSE-1264
    public void testImplicitVar3() {
        String contents =
            "doesNotExist";
        int start = contents.lastIndexOf("doesNotExist");
        assertUnknownConfidence(contents, start, start+"doesNotExist".length(), "Search", false);
    }

    @Test // GRECLIPSE-1264
    public void testImplicitVar4() {
        String contents = "doesNotExist = 9";
        int start = contents.lastIndexOf("doesNotExist");
        assertDeclaringType(contents, start, start+"doesNotExist".length(), "Search", false);
    }

    @Test // GRECLIPSE-1264
    public void testImplicitVar5() {
        String contents =
            "doesNotExist = 9\n" +
            "def x = {doesNotExist }";
        int start = contents.lastIndexOf("doesNotExist");
        assertDeclaringType(contents, start, start+"doesNotExist".length(), "Search", false);
    }

    @Test // GRECLIPSE-1264
    public void testImplicitVar6() {
        String contents =
            "def x = {\n" +
            "doesNotExist = 9\n" +
            "doesNotExist }";
        int start = contents.lastIndexOf("doesNotExist");
        assertDeclaringType(contents, start, start+"doesNotExist".length(), "Search", false);
    }

    @Test // GRECLIPSE-1264
    public void testImplicitVar7() {
        String contents =
            "def z() {\n" +
            "    doesNotExist = 9\n" +
            "}\n";
        int start = contents.lastIndexOf("doesNotExist");
        assertUnknownConfidence(contents, start, start+"doesNotExist".length(), "Search", false);
    }

    @Test // nested expressions of various forms
    public void testNested1() {
        String contents =
            "(true ? 2 : 7) + 9";
        assertType(contents, "java.lang.Integer");
    }

    @Test // nested expressions of various forms
    public void testNested2() {
        String contents =
            "(true ? 2 : 7) + (true ? 2 : 7)";
        assertType(contents, "java.lang.Integer");
    }

    @Test // nested expressions of various forms
    public void testNested3() {
        String contents =
            "(8 ?: 7) + (8 ?: 7)";
        assertType(contents, "java.lang.Integer");
    }

    @Test // nested expressions of various forms
    public void testNested4() {
        createUnit("Foo", "class Foo { int prop }");
        String contents = "(new Foo().@prop) + (8 ?: 7)";
        assertType(contents, "java.lang.Integer");
    }

    @Test
    public void testPostfix() {
        String contents =
            "int i = 0\n" +
            "def list = [0]\n" +
            "list[i]++";
        int start = contents.lastIndexOf('i');
        assertType(contents, start, start +1, "java.lang.Integer");
    }

    @Test // GRECLIPSE-1302
    public void testNothingIsUnknown() {
        assertNoUnknowns(
            "1 > 4\n" +
            "1 < 1\n" +
            "1 >= 1\n" +
            "1 <= 1\n" +
            "1 <=> 1\n" +
            "1 == 1\n" +
            "[1,9][0]");
    }

    @Test
    public void testNothingIsUnknownWithCategories() {
        assertNoUnknowns(
            "class Me {\n" +
            "    def meth() {\n" +
            "        use (MeCat) {\n" +
            "            println getVal()\n" +
            "            println val\n" +
            "        }\n" +
            "    }\n" +
            "} \n" +
            "\n" +
            "class MeCat { \n" +
            "    static String getVal(Me self) {\n" +
            "        \"val\"\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "use (MeCat) {\n" +
            "    println new Me().getVal()\n" +
            "    println new Me().val\n" +
            "}\n" +
            "new Me().meth()");
    }

    @Test // GRECLIPSE-1304
    public void testNoGString() {
        assertNoUnknowns("'$'\n'${}\n'${a}'\n'$a'");
    }

    @Test // GRECLIPSE-1341
    public void testDeclarationAtBeginningOfMethod() {
        String contents =
            "class Problem2 {\n" +
            "    String action() { }\n" +
            "    def meth() {\n" +
            "        def x = action()\n" +
            "        x.substring()\n" +
            "    }\n" +
            "}";
        int start = contents.lastIndexOf("substring");
        int end = start + "substring".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test // GRECLIPSE-1638
    public void testInstanceOf1() {
        String contents =
            "def m(Object obj) {\n" +
            "  def val = obj\n" +
            "  if (val instanceof String) {\n" +
            "    println val.trim()\n" +
            "  }\n" +
            "  val\n" +
            "}\n";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test // GRECLIPSE-1638
    public void testInstanceOf1a() {
        String contents =
            "def m(Object obj) {\n" +
            "  def val = obj\n" +
            "  if (val == null || val instanceof String) {\n" +
            "    println val.trim()\n" +
            "  }\n" +
            "  val\n" +
            "}\n";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test // GRECLIPSE-1638
    public void testInstanceOf1b() {
        String contents =
            "def m(Object obj) {\n" +
            "  def val = obj\n" +
            "  if (val != null && val instanceof String) {\n" +
            "    println val.trim()\n" +
            "  }\n" +
            "  val\n" +
            "}\n";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test
    public void testInstanceOf2() {
        String contents =
            "def m(Object obj) {\n" +
            "  def val = obj\n" +
            "  if (!(val instanceof String)) {\n" +
            "    println val\n" +
            "  }\n" +
            "  val\n" +
            "}\n";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test
    public void testInstanceOf3() {
        String contents =
            "def m(Object obj) {\n" +
            "  def val = obj\n" +
            "  if (!(val instanceof String)) {\n" +
            "    println val\n" +
            "  } else {\n" +
            "    val\n" +
            "  }\n" +
            "  val\n" +
            "}\n";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object"); //TODO: "java.lang.String"?

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test
    public void testInstanceOf4() {
        String contents =
            "def m(Object obj) {\n" +
            "  def val = obj\n" +
            "  if (val instanceof String) {\n" +
            "    println val.trim()\n" +
            "  }\n" +
            "  val\n" +
            "  def var = obj\n" +
            "  if (var instanceof Integer) {\n" +
            "    println var.intValue()\n" +
            "  }\n" +
            "  var\n" +
            "}\n";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("var");
        end = start + "var".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("var", end + 1);
        end = start + "var".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("var", end + 1);
        end = start + "var".length();
        assertType(contents, start, end, "java.lang.Integer");

        start = contents.indexOf("var", end + 1);
        end = start + "var".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test
    public void testInstanceOf5() {
        String contents =
            "def val = new Object()\n" +
            "if (val instanceof Number) {\n" +
            "  val\n" +
            "} else if (val instanceof CharSequence) {\n" +
            "  val\n" +
            "}\n" +
            "val";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Number");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.CharSequence");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test
    public void testInstanceOf6() {
        String contents =
            "def val = new Object()\n" +
            "if (val instanceof Number || val instanceof CharSequence) {\n" +
            "  println val\n" +
            "}\n" +
            "val";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test
    public void testInstanceOf7() {
        String contents =
            "def val = new Object()\n" +
            "if (val instanceof Number) {\n" +
            "  if (val instanceof Double) {\n" +
            "    val\n" +
            "}}\n" +
            "val";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Number");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Double");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test
    public void testInstanceOf8() {
        String contents =
            "def val = new Object()\n" +
            "if (val instanceof String) {\n" +
            "  if (val instanceof CharSequence) {\n" +
            "    val\n" +
            "}}\n" +
            "val";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test
    public void testInstanceOf9() {
        String contents =
            "def val\n" +
            "def str = val instanceof String ? val : val.toString()\n" +
            "val";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.String");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test
    public void testInstanceOf10() {
        String contents =
            "def val\n" +
            "def str = !(val instanceof String) ? val.toString : val\n" +
            "val";

        int start = contents.indexOf("val");
        int end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object"); //TODO: "java.lang.String"?

        start = contents.indexOf("val", end + 1);
        end = start + "val".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    @Test // GRECLIPSE-554
    public void testMapEntries1() {
        String contents =
            "def map = [:]\n" +
            "map.foo = 1\n" +
            "print map.foo\n" +
            "map.foo = 'text'\n" +
            "print map.foo\n";

        int start = contents.lastIndexOf("map");
        int end = start + "map".length();
        assertType(contents, start, end, "java.util.Map<java.lang.Object,java.lang.Object>");

        start = contents.indexOf("foo");
        start = contents.indexOf("foo", start + 1);
        end = start + "foo".length();
        assertType(contents, start, end, "java.lang.Integer");

        start = contents.indexOf("foo", start + 1);
        start = contents.indexOf("foo", start + 1);
        end = start + "foo".length();
        assertType(contents, start, end, "java.lang.String");
    }

    @Test
    public void testThisInInnerClass1() {
        String contents =
            "class A {\n" +
            "    String source = null\n" +
            "    def user = new Object() {\n" +
            "        def field = A.this.source\n" +
            "    }\n" +
            "}\n";
        int start = contents.lastIndexOf("source");
        int end = start + "source".length();
        assertType(contents, start, end, "java.lang.String");

        start = contents.indexOf("this");
        end = start + "this".length();
        assertType(contents, start, end, "A");
    }

    @Test // GRECLIPSE-1798
    public void testFieldAndPropertyWithSameName() {
        createJavaUnit("Wrapper",
            "public class Wrapper<T> {\n" +
            "  private final T wrapped;\n" +
            "  public Wrapper(T wrapped) { this.wrapped = wrapped; }\n" +
            "  public T getWrapped() { return wrapped; }\n" +
            "}");
        createJavaUnit("MyBean",
            "public class MyBean {\n" +
            "  private Wrapper<String> foo = new Wrapper<>(\"foo\");\n" +
            "  public String getFoo() { return foo.getWrapped(); }\n" +
            "}");

        String contents =
            "class GroovyTest {\n" +
            "  static void main(String[] args) {\n" +
            "    def b = new MyBean()\n" +
            "    println b.foo.toUpperCase()\n" +
            "  }\n" +
            "}";
        assertExprType(contents, "foo", "java.lang.String");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/355
    public void testLocalTypeAndDefaultImportCollision() {
        createJavaUnit("domain", "Calendar",
            "public class Calendar { public static Calendar instance() { return null; } }");

        String contents = "def cal = domain.Calendar.instance()";
        assertExprType(contents, "instance", "domain.Calendar");
        assertExprType(contents, "cal", "domain.Calendar");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/405
    public void testMethodOverloadsAndImperfectArgumentMatching() {
        createJavaUnit("MyEnum", "enum MyEnum { A, B }");

        String contents = "class Issue405 {\n" +
            "  void meth(String s, MyEnum e) {\n" +
            "    def d1, d2\n" +
            "    switch (e) {\n" +
            "    case MyEnum.A:\n" +
            "      d1 = new Date()\n" +
            "      d2 = new Date()\n" +
            "      break\n" +
            "    case MyEnum.B:\n" +
            "      d1 = null\n" +
            "      d2 = null\n" +
            "      break\n" +
            "    }\n" +
            "    meth(s, d1, d2)\n" +
            "  }\n" +
            "  void meth(String s, Date d1, Date d2) {\n" +
            "  }\n" +
            "}";
        int offset = contents.indexOf("meth(s");
        MethodNode m = assertDeclaration(contents, offset, offset + 4, "Issue405", "meth", DeclarationKind.METHOD);
        assertEquals("Expected 'meth(String, Date, Date)' but was 'meth(String, MyEnum)'", 3, m.getParameters().length);
    }

    @Test
    public void testMethodOverloadsAndPerfectArgumentMatching() {
        createJavaUnit("MyEnum", "enum MyEnum { A, B }");

        String contents = "class Issue405 {\n" +
            "  void meth(String s, MyEnum e) {\n" +
            "    def d1, d2\n" +
            "    switch (e) {\n" +
            "    case MyEnum.A:\n" +
            "      d1 = new Date()\n" +
            "      d2 = new Date()\n" +
            "      break\n" +
            "    case MyEnum.B:\n" +
            "      d1 = null\n" +
            "      d2 = null\n" +
            "      break\n" +
            "    }\n" +
            "    meth(s, d1, d2)\n" +
            "  }\n" +
            "  void meth(String s, Date d1, Date d2) {\n" +
            "  }\n" +
            "  void meth(String s, Object o1, Object o2) {\n" +
            "  }\n" +
            "}";
        int offset = contents.indexOf("meth(s");
        MethodNode m = assertDeclaration(contents, offset, offset + 4, "Issue405", "meth", DeclarationKind.METHOD);
        assertTrue("Expected 'meth(String, Object, Object)' but was 'meth(String, MyEnum)' or 'meth(String, Date, Date)'",
            m.getParameters().length == 3 && m.getParameters()[2].getType().getNameWithoutPackage().equals("Object"));
    }
}
