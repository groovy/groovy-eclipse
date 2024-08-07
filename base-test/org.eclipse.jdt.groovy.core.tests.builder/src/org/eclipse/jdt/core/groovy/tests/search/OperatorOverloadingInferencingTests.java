/*
 * Copyright 2009-2024 the original author or authors.
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

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;
import static org.junit.Assume.assumeTrue;

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
            "  Foo plus(that) {}\n" +
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
            "  Foo plus(that) {}\n" +
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
            "  Foo minus(that) {}\n" +
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
            "  Foo multiply(that) {}\n" +
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
            "  Foo div(that) {}\n" +
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
            "  Foo mod(that) {}\n" +
            "}\n" +
            (isAtLeastGroovy(50) ? "@groovy.transform.OperatorRename(remainder='mod')\n" : "") +
            "void test() {\n" +
            "  def xxx = new Bar() % nuthin\n" +
            "}\n";

        assertType(contents, "xxx", "Foo");
    }

    @Test
    public void testRemainder() {
        assumeTrue(isAtLeastGroovy(50));

        String contents =
            "class Foo {}\n" +
            "class Bar {\n" +
            "  Foo remainder(that) {}\n" +
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
            "  Foo and(that) {}\n" +
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
            "  Foo or(that) {}\n" +
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
            "  Foo xor(that) {}\n" +
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
            "  Foo rightShift(that) {}\n" +
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
            "  Foo leftShift(that) {}\n" +
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
            "  Foo getAt(that) {}\n" +
            "}\n" +
            "def xxx = new Bar()[nuthin]\n" +
            "xxx";

        assertType(contents, "xxx", "Foo");
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
    public void testAttributeExpr1() {
        String contents =
            "class Foo { boolean flag\n}\n" +
            "def xxx = new Foo().@flag\n" +
            "xxx";

        assertType(contents, "xxx", "java.lang.Boolean");
    }

    @Test
    public void testAttributeExpr2() {
        String contents =
            "class Foo { String str\n}\n" +
            "def xxx = new Foo().@str.startsWith('1')\n" +
            "xxx";

        assertType(contents, "xxx", "java.lang.Boolean");
    }

    @Test
    public void testNumberPlusString() {
        String contents =
            "def xxx = 1 + ''\n" +
            "xxx";

        assertType(contents, "xxx", "java.lang.String");
    }

    @Test
    public void testNumberPlusGString() {
        String contents =
            "def xxx = 1 + \"${this}\"\n" +
            "xxx";

        assertType(contents, "xxx", "java.lang.String");
    }

    @Test
    public void testCompleteExpr1() {
        assertType("['']", "java.util.List<java.lang.String>");
    }

    @Test
    public void testCompleteExpr2() {
        assertType("this.class.name", "java.lang.String");
    }

    @Test
    public void testCompleteExpr3() {
        assertType("this.getClass().getName()", "java.lang.String");
    }

    @Test
    public void testCompleteExpr4() {
        assertType("this.getClass().getName() + 3", "java.lang.String");
    }

    @Test
    public void testCompleteExpr5() {
        assertType("4 + this.getClass().getName()", "java.lang.String");
    }

    @Test
    public void testCompleteExpr6() {
        assertType("new LinkedList<String>()[0]", "java.lang.String");
    }

    @Test
    public void testCompleteExpr7() {
        assertType("[1:3]", "java.util.Map<java.lang.Integer,java.lang.Integer>");
    }

    @Test
    public void testPrefix1() {
        String contents =
            "class Foo { double positive() {}}\n" +
            "def xxx = +(new Foo())";

        assertType(contents, "xxx", "java.lang.Double");
    }

    @Test
    public void testPrefix2() {
        String contents =
            "class Foo { double negative() {}}\n" +
            "def xxx = -(new Foo())";

        assertType(contents, "xxx", "java.lang.Double");
    }

    @Test
    public void testPrefix3() {
        String contents =
            "class Foo { double next() {}}\n" +
            "def xxx = ++(new Foo())";

        assertType(contents, "xxx", "java.lang.Double");
    }

    @Test
    public void testPrefix4() {
        String contents =
            "class Foo { double previous() {}}\n" +
            "def xxx = --(new Foo())";

        assertType(contents, "xxx", "java.lang.Double");
    }

    @Test
    public void testPrefix5() {
        String contents =
            "class Foo { double bitwiseNegate() {}}\n" +
            "def xxx = ~(new Foo())";

        assertType(contents, "xxx", "java.lang.Double");
    }

    @Test
    public void testPostfix1() {
        String contents =
            "class Foo { double next() {}}\n" +
            "def xxx = (new Foo())++";

        assertType(contents, "xxx", "java.lang.Double");
    }

    @Test
    public void testPostfix2() {
        String contents =
            "class Foo { double previous() {}}\n" +
            "def xxx = (new Foo())--";

        assertType(contents, "xxx", "java.lang.Double");
    }

    @Test
    public void testPostfix3() {
        String contents =
            "class Foo { Foo next() {}\n int previous() {}}\n" +
            "def xxx = ([new Foo()++][0]--) + 8";

        assertType(contents, "xxx", "java.lang.Integer");
    }
}
