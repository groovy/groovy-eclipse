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
package org.eclipse.jdt.core.groovy.tests.search;

import org.junit.Test;

/**
 * Tests of operator overloading inferencing.
 */
public final class OperatorOverloadingInferencingTests extends InferencingTestSuite {

    @Test
    public void testPlus1() {
        String contents =
            "class Foo {}\n" +
            "class Bar {\n" +
            "  Foo plus() {}\n" +
            "}\n" +
            "def xxx = new Bar() + nuthin\n" +
            "xxx";

        assertType(contents, "xxx", "Foo");
    }

    @Test
    public void testPlus2() {
        String contents =
            "class Foo {}\n" +
            "class Bar {\n" +
            "  Foo plus() {}\n" +
            "}\n" +
            "class Sub extends Bar {}\n" +
            "def xxx = new Sub() + nuthin\n" +
            "xxx";

        assertType(contents, "xxx", "Foo");
    }

    @Test
    public void testPlus3() {
        String contents =
            "def xxx = [2]+[2]\n" +
            "xxx";

        assertType(contents, "xxx", "java.util.List<java.lang.Integer>");
    }

    @Test
    public void testMinus1() {
        String contents =
            "class Foo {}\n" +
            "class Bar {\n" +
            "  Foo minus() {}\n" +
            "}\n" +
            "def xxx = new Bar() - nuthin\n" +
            "xxx";

        assertType(contents, "xxx", "Foo");
    }

    @Test
    public void testMinus2() {
        String contents =
            "def xxx = [2]-[2]\n" +
            "xxx";

        assertType(contents, "xxx", "java.util.List<java.lang.Integer>");
    }

    @Test
    public void testMultiply() {
        String contents =
            "class Foo {}\n" +
            "class Bar {\n" +
            "  Foo multiply() {}\n" +
            "}\n" +
            "def xxx = new Bar() * nuthin\n" +
            "xxx";

        assertType(contents, "xxx", "Foo");
    }

    @Test
    public void testDivide() {
        String contents =
            "class Foo {}\n" +
            "class Bar {\n" +
            "  Foo div() {}\n" +
            "}\n" +
            "def xxx = new Bar() / nuthin\n" +
            "xxx";

        assertType(contents, "xxx", "Foo");
    }

    @Test
    public void testMod() {
        String contents =
            "class Foo {}\n" +
            "class Bar {\n" +
            "  Foo mod() {}\n" +
            "}\n" +
            "def xxx = new Bar() % nuthin\n" +
            "xxx";

        assertType(contents, "xxx", "Foo");
    }

    @Test
    public void testAnd() {
        String contents =
            "class Foo {}\n" +
            "class Bar {\n" +
            "  Foo and() {}\n" +
            "}\n" +
            "def xxx = new Bar() & nuthin\n" +
            "xxx";

        assertType(contents, "xxx", "Foo");
    }

    @Test
    public void testOr() {
        String contents =
            "class Foo {}\n" +
            "class Bar {\n" +
            "  Foo or() {}\n" +
            "}\n" +
            "def xxx = new Bar() | nuthin\n" +
            "xxx";

        assertType(contents, "xxx", "Foo");
    }

    @Test
    public void testXor() {
        String contents =
            "class Foo {}\n" +
            "class Bar {\n" +
            "  Foo xor() {}\n" +
            "}\n" +
            "def xxx = new Bar() ^ nuthin\n" +
            "xxx";

        assertType(contents, "xxx", "Foo");
    }

    @Test
    public void testRightShift() {
        String contents =
            "class Foo {}\n" +
            "class Bar {\n" +
            "  Foo rightShift(a) {}\n" +
            "}\n" +
            "def xxx = new Bar() >> nuthin\n" +
            "xxx";

        assertType(contents, "xxx", "Foo");
    }

    @Test
    public void testLeftShift() {
        String contents =
            "class Foo {}\n" +
            "class Bar {\n" +
            "  Foo leftShift(a) {}\n" +
            "}\n" +
            "def xxx = new Bar() << nuthin\n" +
            "xxx";

        assertType(contents, "xxx", "Foo");
    }

    @Test
    public void testGetAt1() {
        String contents =
            "class Foo {}\n" +
            "class Bar {\n" +
            "  Foo getAt() {}\n" +
            "}\n" +
            "def xxx = new Bar()[nuthin]\n" + // should be DGM.getAt(Object, String): Object
            "xxx";

        assertType(contents, "xxx", "java.lang.Object");
    }

    @Test
    public void testGetAt2() {
        String contents =
            "class Foo {}\n" +
            "Foo[] yyy\n" +
            "def xxx = yyy[0]\n" +
            "xxx";

        assertType(contents, "xxx", "Foo");
    }

    @Test
    public void testGetAt3() {
        String contents =
            "class Foo {}\n" +
            "Foo[] yyy\n" +
            "def xxx = yyy[0,1,2]\n" +
            "xxx";

        assertType(contents, "xxx", "java.util.List<Foo>");
    }

    @Test
    public void testGetAt4() {
        String contents =
            "class Foo {}\n" +
            "Foo[] yyy\n" +
            "def xxx = yyy[0..2]\n" +
            "xxx";

        assertType(contents, "xxx", "java.util.List<Foo>");
    }

    @Test
    public void testGetAt5() {
        String contents =
            "class Foo {}\n" +
            "List<Foo> yyy\n" +
            "def xxx = yyy[0]\n" +
            "xxx";

        assertType(contents, "xxx", "Foo");
    }

    @Test
    public void testGetAt6() {
        String contents =
            "class Foo {}\n" +
            "List<Foo> yyy\n" +
            "def xxx = yyy[0,1,2]\n" +
            "xxx";

        assertType(contents, "xxx", "java.util.List<Foo>");
    }

    @Test
    public void testGetAt7() {
        String contents =
            "class Foo {}\n" +
            "List<Foo> yyy\n" +
            "def xxx = yyy[0..2]\n" +
            "xxx";

        assertType(contents, "xxx", "java.util.List<Foo>");
    }

    @Test
    public void testGetAt8() {
        String contents =
            "class Foo {}\n" +
            "Map<Integer,Foo> yyy\n" +
            "def xxx = yyy[0]\n" +
            "xxx";

        assertType(contents, "xxx", "Foo");
    }

    @Test
    public void testGetAt9() {
        String contents =
            "class Foo {}\n" +
            "BitSet yyy\n" +
            "def xxx = yyy[0]\n" +
            "xxx";

        assertType(contents, "xxx", "java.lang.Boolean");
    }

    @Test
    public void testAttributeExpr1() throws Exception {
        String contents =
            "class Foo { boolean str\n}\n" +
            "def xxx = new Foo().@str\n" +
            "xxx";

        assertType(contents, "xxx", "java.lang.Boolean");
    }

    @Test
    public void testAttributeExpr2() throws Exception {
        String contents =
            "class Foo { String str\n}\n" +
            "def xxx = new Foo().@str.startsWith('1')\n" +
            "xxx";

        assertType(contents, "xxx", "java.lang.Boolean");
    }

    @Test
    public void testLongExpr1() throws Exception {
        String contents =
            "class Foo { String str\n}\n" +
            "def xxx = ([ new Foo() ].str.length() + 4 - 9) % 7\n" +
            "xxx";

        assertType(contents, "xxx", "java.lang.Integer");
    }

    @Test
    public void testLongExpr2() throws Exception {
        String contents =
            "class Foo { String str\n}\n" +
            "def xxx = ([ new Foo() ])[(new Foo().str.length() + 4 - 9) % 7]\n" +
            "xxx";

        assertType(contents, "xxx", "Foo");
    }

    @Test
    public void testLongExpr3() throws Exception {
        String contents =
            "class Foo { Foo next() {}\n int previous() {}\n}\n" +
            "def xxx = ([new Foo()++][0]--) + 8\n" +
            "xxx";

        assertType(contents, "xxx", "java.lang.Integer");
    }

    @Test
    public void testNumberPlusString1() throws Exception {
        String contents =
            "def xxx = 1 + ''\n" +
            "xxx";

        assertType(contents, "xxx", "java.lang.String");
    }

    @Test
    public void testNumberPlusString2() throws Exception {
        String contents =
            "def xxx = 1 + \"${this}\"\n" +
            "xxx";

        assertType(contents, "xxx", "java.lang.String");
    }

    @Test
    public void testCompleteExpr1() throws Exception {
        assertType("['']", "java.util.List<java.lang.String>");
    }

    @Test
    public void testCompleteExpr2() throws Exception {
        assertType("this.class.name", "java.lang.String");
    }

    @Test
    public void testCompleteExpr3() throws Exception {
        assertType("this.getClass().getName()", "java.lang.String");
    }

    @Test
    public void testCompleteExpr4() throws Exception {
        assertType("this.getClass().getName() + 3", "java.lang.String");
    }

    @Test
    public void testCompleteExpr5() throws Exception {
        assertType("4 + this.getClass().getName()", "java.lang.String");
    }

    @Test
    public void testCompleteExpr6() throws Exception {
        assertType("new LinkedList<String>()[0]", "java.lang.String");
    }

    @Test
    public void testCompleteExpr7() throws Exception {
        assertType("[1:3]", "java.util.Map<java.lang.Integer,java.lang.Integer>");
    }

    @Test
    public void testCompleteExpr8() throws Exception {
        assertType("1..3", "groovy.lang.Range<java.lang.Integer>");
    }

    @Test
    public void testPrefix1() throws Exception {
        String contents = "def x = 1\ndef xxx = -x\nxxx";

        assertType(contents, "xxx", "java.lang.Integer");
    }

    @Test
    public void testPrefix2() throws Exception {
        String contents =
            "class Foo { double positive() {}}\n" +
            "def xxx = +(new Foo())\n" +
            "xxx";

        assertType(contents, "xxx", "java.lang.Double");
    }

    @Test
    public void testPrefix3() throws Exception {
        String contents =
            "class Foo { double negative() {}}\n" +
            "def xxx = -(new Foo())\n" +
            "xxx";

        assertType(contents, "xxx", "java.lang.Double");
    }

    @Test
    public void testPrefix4() throws Exception {
        String contents =
            "class Foo { double next() {}}\n" +
            "def xxx = ++(new Foo())\n" +
            "xxx";

        assertType(contents, "xxx", "java.lang.Double");
    }

    @Test
    public void testPrefix5() throws Exception {
        String contents =
            "class Foo { double previous() {}}\n" +
            "def xxx = --(new Foo())\n" +
            "xxx";

        assertType(contents, "xxx", "java.lang.Double");
    }

    @Test
    public void testPrefix6() throws Exception {
        String contents =
            "class Foo { double bitwiseNegate() {}}\n" +
            "def xxx = ~(new Foo())\n" +
            "xxx";

        assertType(contents, "xxx", "java.lang.Double");
    }

    @Test
    public void testPostfix1() throws Exception {
        String contents =
            "class Foo { double next() {}}\n" +
            "def xxx = (new Foo())++\n" +
            "xxx";

        assertType(contents, "xxx", "java.lang.Double");
    }

    @Test
    public void testPostfix2() throws Exception {
        String contents =
            "class Foo { double previous() {}}\n" +
            "def xxx = (new Foo())--\n" +
            "xxx";

        assertType(contents, "xxx", "java.lang.Double");
    }
}
