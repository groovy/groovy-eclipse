 /*
 * Copyright 2003-2009 the original author or authors.
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

import junit.framework.Test;

/**
 * tests of operator overloading inferencing
 * @author Andrew Eisenberg
 * @created Dec 15, 2011
 */
public class OperatorOverloadingInferencingTests extends AbstractInferencingTest {
 
    public static Test suite() {
        return buildTestSuite(OperatorOverloadingInferencingTests.class);
    }

    public OperatorOverloadingInferencingTests(String name) {
        super(name);
    }

    public void testPlus1() throws Exception {
        String contents = 
                "class Foo { }\n" +
                "class Bar {\n" +
                "  Foo plus() { }\n" +
                "}\n" +
                "def xxx = new Bar() + nuthin\n" +
                "xxx";
        String expr = "xxx";
        assertType(contents, contents.lastIndexOf(expr), contents.lastIndexOf(expr)+expr.length(), "Foo");
    }

    public void testPlus2() throws Exception {
        String contents = 
                "class Foo { }\n" +
                "class Bar {\n" +
                "  Foo plus() { }\n" +
                "}\n" +
                "class Sub extends Bar { }\n" +
                "def xxx = new Sub() + nuthin\n" +
                "xxx";
        String expr = "xxx";
        assertType(contents, contents.lastIndexOf(expr), contents.lastIndexOf(expr)+expr.length(), "Foo");
    }
    
    public void testMinus() throws Exception {
        String contents = 
                "class Foo { }\n" +
                "class Bar {\n" +
                "  Foo minus() { }\n" +
                "}\n" +
                "def xxx = new Bar() - nuthin\n" +
                "xxx";
        String expr = "xxx";
        assertType(contents, contents.lastIndexOf(expr), contents.lastIndexOf(expr)+expr.length(), "Foo");
    }

    // FIXADE We are losing the generic information here, but that is because it is encoded in a type parameter on a method
    public void testMinus2() throws Exception {
        String contents = 
                "def xxx = [2]-[2]\n" +
                "xxx";
        String expr = "xxx";
        assertType(contents, contents.lastIndexOf(expr), contents.lastIndexOf(expr)+expr.length(), "java.util.List<java.lang.Object<T>>");
    }
    
    public void testMultiply() throws Exception {
        String contents = 
                "class Foo { }\n" +
                "class Bar {\n" +
                "  Foo multiply() { }\n" +
                "}\n" +
                "def xxx = new Bar() * nuthin\n" +
                "xxx";
        String expr = "xxx";
        assertType(contents, contents.lastIndexOf(expr), contents.lastIndexOf(expr)+expr.length(), "Foo");
    }

    public void testDivide() throws Exception {
        String contents = 
                "class Foo { }\n" +
                "class Bar {\n" +
                "  Foo divide() { }\n" +
                "}\n" +
                "def xxx = new Bar() / nuthin\n" +
                "xxx";
        String expr = "xxx";
        assertType(contents, contents.lastIndexOf(expr), contents.lastIndexOf(expr)+expr.length(), "Foo");
    }

    public void testMod() throws Exception {
        String contents = 
                "class Foo { }\n" +
                "class Bar {\n" +
                "  Foo mod() { }\n" +
                "}\n" +
                "def xxx = new Bar() % nuthin\n" +
                "xxx";
        String expr = "xxx";
        assertType(contents, contents.lastIndexOf(expr), contents.lastIndexOf(expr)+expr.length(), "Foo");
    }

    public void testAnd() throws Exception {
        String contents = 
                "class Foo { }\n" +
                "class Bar {\n" +
                "  Foo and() { }\n" +
                "}\n" +
                "def xxx = new Bar() & nuthin\n" +
                "xxx";
        String expr = "xxx";
        assertType(contents, contents.lastIndexOf(expr), contents.lastIndexOf(expr)+expr.length(), "Foo");
    }

    public void testOr() throws Exception {
        String contents = 
                "class Foo { }\n" +
                "class Bar {\n" +
                "  Foo or() { }\n" +
                "}\n" +
                "def xxx = new Bar() | nuthin\n" +
                "xxx";
        String expr = "xxx";
        assertType(contents, contents.lastIndexOf(expr), contents.lastIndexOf(expr)+expr.length(), "Foo");
    }

    public void testXor() throws Exception {
        String contents = 
                "class Foo { }\n" +
                "class Bar {\n" +
                "  Foo xor() { }\n" +
                "}\n" +
                "def xxx = new Bar() ^ nuthin\n" +
                "xxx";
        String expr = "xxx";
        assertType(contents, contents.lastIndexOf(expr), contents.lastIndexOf(expr)+expr.length(), "Foo");
    }

    public void testRightShift() throws Exception {
        String contents = 
                "class Foo { }\n" +
                "class Bar {\n" +
                "  Foo rightShift(a) { }\n" +
                "}\n" +
                "def xxx = new Bar() >> nuthin\n" +
                "xxx";
        String expr = "xxx";
        assertType(contents, contents.lastIndexOf(expr), contents.lastIndexOf(expr)+expr.length(), "Foo");
    }

    public void testLeftShift() throws Exception {
        String contents = 
                "class Foo { }\n" +
                "class Bar {\n" +
                "  Foo leftShift(a) { }\n" +
                "}\n" +
                "def xxx = new Bar() << nuthin\n" +
                "xxx";
        String expr = "xxx";
        assertType(contents, contents.lastIndexOf(expr), contents.lastIndexOf(expr)+expr.length(), "Foo");
    }

    // lots of variants with getAt
    
    public void testGetAt1() throws Exception {
        String contents = 
                "class Foo { }\n" +
                "class Bar {\n" +
                "  Foo getAt() { }\n" +
                "}\n" +
                "def xxx = new Bar()[nuthin]\n" +
                "xxx";
        String expr = "xxx";
        assertType(contents, contents.lastIndexOf(expr), contents.lastIndexOf(expr)+expr.length(), "Foo");
    }
    
    public void testGetAt2() throws Exception {
        String contents = 
                "class Foo{ }\n" +
                "Foo[] yyy\n" +
                "def xxx = yyy[0]\n" +
                "xxx";
        String expr = "xxx";
        assertType(contents, contents.lastIndexOf(expr), contents.lastIndexOf(expr)+expr.length(), "Foo");
    }
    
    public void testGetAt3() throws Exception {
        String contents = 
                "class Foo{ }\n" +
                "Foo[] yyy\n" +
                "def xxx = yyy[0,1,2]\n" +
                "xxx";
        String expr = "xxx";
        assertType(contents, contents.lastIndexOf(expr), contents.lastIndexOf(expr)+expr.length(), "java.util.List<Foo>");
    }
    
    public void testGetAt4() throws Exception {
        String contents = 
                "class Foo{ }\n" +
                "Foo[] yyy\n" +
                "def xxx = yyy[0..2]\n" +
                "xxx";
        String expr = "xxx";
        assertType(contents, contents.lastIndexOf(expr), contents.lastIndexOf(expr)+expr.length(), "java.util.List<Foo>");
    }
    public void testGetAt5() throws Exception {
        String contents = 
                "class Foo{ }\n" +
                "List<Foo> yyy\n" +
                "def xxx = yyy[0]\n" +
                "xxx";
        String expr = "xxx";
        assertType(contents, contents.lastIndexOf(expr), contents.lastIndexOf(expr)+expr.length(), "Foo");
    }
    
    public void testGetAt6() throws Exception {
        String contents = 
                "class Foo{ }\n" +
                "List<Foo> yyy\n" +
                "def xxx = yyy[0,1,2]\n" +
                "xxx";
        String expr = "xxx";
        assertType(contents, contents.lastIndexOf(expr), contents.lastIndexOf(expr)+expr.length(), "java.util.List<Foo>");
    }
    
    public void testGetAt7() throws Exception {
        String contents = 
                "class Foo{ }\n" +
                "List<Foo> yyy\n" +
                "def xxx = yyy[0..2]\n" +
                "xxx";
        String expr = "xxx";
        assertType(contents, contents.lastIndexOf(expr), contents.lastIndexOf(expr)+expr.length(), "java.util.List<Foo>");
    }
    public void testGetAt8() throws Exception {
        String contents = 
                "class Foo{ }\n" +
                "Map<Integer,Foo> yyy\n" +
                "def xxx = yyy[0]\n" +
                "xxx";
        String expr = "xxx";
        assertType(contents, contents.lastIndexOf(expr), contents.lastIndexOf(expr)+expr.length(), "Foo");
    }
    
    public void testGetAt9() throws Exception {
        String contents = 
                "class Foo{ }\n" +
                "BitSet yyy\n" +
                "def xxx = yyy[0]\n" +
                "xxx";
        String expr = "xxx";
        assertType(contents, contents.lastIndexOf(expr), contents.lastIndexOf(expr)+expr.length(), "java.lang.Boolean");
    }

}