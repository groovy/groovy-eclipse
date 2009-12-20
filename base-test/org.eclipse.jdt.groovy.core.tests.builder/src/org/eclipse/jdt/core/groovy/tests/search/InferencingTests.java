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
 * Lots of tests to see that expressions have the proper type associated with them
 * @author Andrew Eisenberg
 * @created Nov 4, 2009
 *
 */
public class InferencingTests extends AbstractInferencingTest {
 
    public static Test suite() {
        return buildTestSuite(InferencingTests.class);
    }

    public InferencingTests(String name) {
        super(name);
    }

    public void testInferNumber1() throws Exception {
        assertType("10", "java.lang.Integer");
    }

    public void testInferNumber2() throws Exception {
        assertType("1+2", "java.lang.Integer");
    }

    public void testInferNumber3() throws Exception {
        assertType("10L", "java.lang.Long");
    }
    
    public void testInferNumber4() throws Exception {
        assertType("10++", "java.lang.Number");
    }
    
    public void testInferNumber5() throws Exception {
        assertType("++10", "java.lang.Number");
    }
    
    public void testInferString1() throws Exception {
        assertType("\"10\"", "java.lang.String");
    }
    
    public void testInferString2() throws Exception {
        assertType("'10'", "java.lang.String");
    }
    
    public void testInferString3() throws Exception {
        String contents = "def x = '10'";
        assertType(contents, contents.indexOf('\''), contents.lastIndexOf('\'')+1, "java.lang.String");
    }
    
    public void testInferString4() throws Exception {
        String contents = "false ? '' : ''";
        assertType(contents, "java.lang.String");
    }
    
    public void testInferList1() throws Exception {
        assertType("[]", "java.util.List");
    }
    
    public void testInferList2() throws Exception {
        assertType("[] << \"\"", "java.util.List");
    }
    
    public void testInferMap1() throws Exception {
        assertType("[:]", "java.util.Map");
    }
    
    public void testInferBoolean1() throws Exception {
        assertType("!x", "java.lang.Boolean");
    }
    
    public void testStaticMethodCall() throws Exception {
        String contents = "Two.x()\n class Two {\n static String x() {\n \"\" } } ";
        String expr = "x";
        assertType(contents, contents.indexOf(expr), contents.indexOf(expr)+expr.length(), "java.lang.String");
    }
    public void testStaticMethodCall2() throws Exception {
        String contents = "Two.x\n class Two {\n static String x() {\n \"\" } } ";
        String expr = "x";
        assertType(contents, contents.indexOf(expr), contents.indexOf(expr)+expr.length(), "java.lang.String");
    }
    public void testStaticMethodCall3() throws Exception {
        String contents = "class Two {\n def other() { \n x() } \n static String x() {\n \"\" } } ";
        String expr = "x() ";  // extra space b/c static method call expression end offset is wrong
        assertType(contents, contents.indexOf(expr), contents.indexOf(expr)+expr.length(), "java.lang.String");
    }
    public void testStaticMethodCall4() throws Exception {
        String contents = "class Two {\n def other() { \n x } \n static String x() {\n \"\" } } ";
        String expr = "x ";  // extra space b/c variable expression end offset is wrong
        assertType(contents, contents.indexOf(expr), contents.indexOf(expr)+expr.length(), "java.lang.String");
    }
    public void testSuperFieldReference() throws Exception {
        String contents = "class B extends A {\n def other() { \n myOther } } \n class A { String myOther } ";
        String expr = "myOther "; // extra space b/c variable expression end offset is wrong
        assertType(contents, contents.indexOf(expr), contents.indexOf(expr)+expr.length(), "java.lang.String");
    }
    
    public void testTernaryExpression() throws Exception {
        String contents = "true ? '' : ''";
        assertType(contents, "java.lang.String");
    }
    
    public void testElvisExpression() throws Exception {
        String contents = "'' ?: ''";
        assertType(contents, "java.lang.String");
    }
    
    public void testRangeExpression1() throws Exception {
        String contents = "0 .. 5";
        assertType(contents, "java.util.List");
    }
    
    public void testRangeExpression2() throws Exception {
        String contents = "0 ..< 5";
        assertType(contents, "java.util.List");
    }
    
    public void testClassReference1() throws Exception {
        String contents = "String";
        assertType(contents, "java.lang.String");
    }
    public void testClassReference2() throws Exception {
        String contents = "String.class";
        assertType(contents, "java.lang.Class");
    }
    public void testClassReference3() throws Exception {
        String contents = "String.getClass()";
        int start = contents.indexOf("getClass");
        int end = start + "getClass".length();
        assertType(contents, start, end, "java.lang.Class");
    }
    public void testClassReference4() throws Exception {
        String contents = "String.class.getCanonicalName()";
        int start = contents.indexOf("getCanonicalName");
        int end = start + "getCanonicalName".length();
        assertType(contents, start, end, "java.lang.String");
    }
    public void testClassReference5() throws Exception {
        String contents = "String.class.canonicalName";
        int start = contents.indexOf("canonicalName");
        int end = start + "canonicalName".length();
        assertType(contents, start, end, "java.lang.String");
    }
    
    public void testInnerClass1() throws Exception {
        String contents = "class Outer { class Inner { } \nInner x }\nnew Outer().x";
        int start = contents.lastIndexOf("x");
        int end = start + 1;
        assertType(contents, start, end, "Outer$Inner");
    }
    
    public void testInnerClass2() throws Exception {
        String contents = "class Outer { class Inner { class InnerInner{ } }\n Outer.Inner.InnerInner x }\nnew Outer().x";
        int start = contents.lastIndexOf("x");
        int end = start + 1;
        assertType(contents, start, end, "Outer$Inner$InnerInner");
    }
    
    public void testInnerClass3() throws Exception {
        String contents = "class Outer { class Inner { def z() { \nnew Outer().x \n } } \nInner x }";
        int start = contents.indexOf("x");
        int end = start + 1;
        assertType(contents, start, end, "Outer$Inner");
    }

    public void testInnerClass4() throws Exception {
        String contents = "class Outer { class Inner { class InnerInner { def z() { \nnew Outer().x \n } } } \nInner x }";
        int start = contents.indexOf("x");
        int end = start + 1;
        assertType(contents, start, end, "Outer$Inner");
    }
    
    public void testInnerClass5() throws Exception {
        String contents = "class Outer { class Inner extends Outer { } }";
        int start = contents.lastIndexOf("Outer");
        int end = start + "Outer".length();
        assertType(contents, start, end, "Outer");
    }
    
    public void testInnerClass6() throws Exception {
        String contents = "class Outer extends RuntimeException { class Inner { def foo() throws Outer { } } }";
        int start = contents.lastIndexOf("Outer");
        int end = start + "Outer".length();
        assertType(contents, start, end, "Outer");
    }
    
}
