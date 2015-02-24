 /*
 * Copyright 2003-2014 the original author or authors.
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

import java.util.List;

import junit.framework.Test;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.tests.util.GroovyUtils;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;

/**
 * Lots of tests to see that expressions have the proper type associated with them
 * @author Andrew Eisenberg
 * @created Nov 4, 2009
 *
 */
public class InferencingTests extends AbstractInferencingTest {
 
    private static final String GET_AT = "getAt";

    public static Test suite() {
        return buildTestSuite(InferencingTests.class);
    }

    public InferencingTests(String name) {
        super(name);
    }
    
    
    public void testLocalVar1() throws Exception {
        String contents ="def x\nthis.x";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertUnknownConfidence(contents, start, end, "Search", false);
    }

    public void testLocalVar2() throws Exception {
        String contents ="def x\ndef y = { this.x }";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertUnknownConfidence(contents, start, end, "Search", false);
    }
    
    public void testLocalVar2a() throws Exception {
        String contents ="def x\ndef y = { this.x() }";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertUnknownConfidence(contents, start, end, "Search", false);
    }
    
    public void testLocalVar3() throws Exception {
        String contents ="int x\ndef y = { x }";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    
    public void testLocalVar4() throws Exception {
        String contents ="int x\ndef y = { x() }";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    
    public void testLocalMethod1() throws Exception {
        String contents ="int x() { }\ndef y = { x() }";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    
    public void testLocalMethod2() throws Exception {
        String contents ="int x() { }\ndef y = { x }";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    
    public void testLocalMethod3() throws Exception {
        String contents ="int x() { }\ndef y = { def z = { x } }";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    
    public void testLocalMethod4() throws Exception {
        String contents ="int x() { }\ndef y = { def z = { x() } }";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    
    public void testLocalMethod5() throws Exception {
        String contents ="int x() { }\ndef y = { def z = { this.x() } }";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    
    public void testLocalMethod6() throws Exception {
        String contents ="def x\ndef y = { delegate.x() }";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertUnknownConfidence(contents, start, end, "Search", false);
    }

    public void testLocalMethod7() throws Exception {
        String contents ="def x\ndef y = { delegate.x }";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertUnknownConfidence(contents, start, end, "Search", false);
    }
    
    
    public void testInferNumber1() throws Exception {
        assertType("10", "java.lang.Integer");
    }
    
    // same as above, but test that whitespace is not included
    public void testInferNumber1a() throws Exception {
        assertType("10 ", 0, 2, "java.lang.Integer");
    }

    public void testInferNumber2() throws Exception {
        String contents ="def x = 1+2\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testInferNumber3() throws Exception {
        assertType("10L", "java.lang.Long");
    }
    
    public void testInferNumber4() throws Exception {
        assertType("10++", "java.lang.Integer");
    }
    
    public void testInferNumber5() throws Exception {
        assertType("++10", "java.lang.Integer");
    }
    
    public void testInferNumber6() throws Exception {
        String contents = "(x <=> y).intValue()";
        int start = contents.indexOf("intValue");
        int end = start + "intValue".length();
        assertType(contents, start, end, "java.lang.Integer");
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
        String contents = "def x = false ? '' : ''\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.String");
    }
    
    public void testMatcher1() throws Exception {
        String contents = "def x = \"\" =~ /pattern/\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.util.regex.Matcher");
    }
    
    public void testMatcher2() throws Exception {
        String contents = "(\"\" =~ /pattern/).hasGroup()";
        int start = contents.indexOf("hasGroup");
        int end = start + "hasGroup".length();
        assertType(contents, start, end, "java.lang.Boolean");
    }
    
    public void testPattern() throws Exception {
        String contents ="def x = \"\" ==~ /pattern/\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Boolean");
    }
    
    public void testInferList1() throws Exception {
        String contents ="def x = []\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.util.List<java.lang.Object>");
    }
    
    // Should be java.util.List<java.lang.String>
    public void testInferList2() throws Exception {
        String contents = "def x = [] << \"\"\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertTypeOneOf(contents, start, end, 
        		"java.util.Collection<java.lang.Object>",
        		"java.util.List<java.lang.Object>"
        );
    }
    
    
	public void testInferClosure1() throws Exception {
        assertType("x.&y", "groovy.lang.Closure");
    }
    
    public void testSpread1() throws Exception {
        String contents = "def z = [1,2]*.value\nz";
        int start = contents.lastIndexOf("value");
        assertType(contents, start, start + "value".length(), "java.lang.Integer");
    }
    
    
    public void testSpread2() throws Exception {
        String contents = "[1,2,3]*.intValue()";
        int start = contents.lastIndexOf("intValue");
        assertType(contents, start, start + "intValue".length(), "java.lang.Integer");
    }

    public void testSpread3() throws Exception {
        String contents = "[1,2,3]*.intValue()[0].value";
        int start = contents.lastIndexOf("value");
        assertType(contents, start, start + "value".length(), "java.lang.Integer");
    }
    
    public void testSpread4() throws Exception {
        String contents = "[x:1,y:2,z:3]*.getKey()";
        int start = contents.lastIndexOf("getKey");
        assertType(contents, start, start + "getKey".length(), "java.lang.String");
    }
    
    public void testSpread5() throws Exception {
        String contents = "[x:1,y:2,z:3]*.getValue()";
        int start = contents.lastIndexOf("getValue");
        assertType(contents, start, start + "getValue".length(), "java.lang.Integer");
    }
    
    public void testSpread6() throws Exception {
        String contents = "[x:1,y:2,z:3]*.key()";
        int start = contents.lastIndexOf("key");
        assertType(contents, start, start + "key".length(), "java.lang.String");
    }
    
    public void testSpread7() throws Exception {
        String contents = "[x:1,y:2,z:3]*.value";
        int start = contents.lastIndexOf("value");
        assertType(contents, start, start + "value".length(), "java.lang.Integer");
    }
    
    public void testSpread8() throws Exception {
        String contents = "[x:1,y:2,z:3]*.key[0].toLowerCase()";
        int start = contents.lastIndexOf("toLowerCase");
        assertType(contents, start, start + "toLowerCase".length(), "java.lang.String");
    }
    
    public void testSpread9() throws Exception {
        String contents = "[x:1,y:2,z:3]*.value[0].intValue()";
        int start = contents.lastIndexOf("intValue");
        assertType(contents, start, start + "intValue".length(), "java.lang.Integer");
    }
    
    public void testSpread10() throws Exception {
        String contents = "[1,2,3]*.value[0].value";
        int start = contents.lastIndexOf("value");
        assertType(contents, start, start + "value".length(), "java.lang.Integer");
    }
    
    public void testInferMap1() throws Exception {
        assertType("[:]", "java.util.Map<java.lang.Object,java.lang.Object>");
    }
    
    public void testInferBoolean1() throws Exception {
        assertType("!x", "java.lang.Boolean");
    }
    
    public void testInferBoolean2() throws Exception {
        String contents = "(x < y).booleanValue()";
        int start = contents.indexOf("booleanValue");
        int end = start + "booleanValue".length();
        assertType(contents, start, end, "java.lang.Boolean");
    }
    
    public void testInferBoolean3() throws Exception {
        String contents = "(x <= y).booleanValue()";
        int start = contents.indexOf("booleanValue");
        int end = start + "booleanValue".length();
        assertType(contents, start, end, "java.lang.Boolean");
    }
    
    public void testInferBoolean4() throws Exception {
        String contents = "(x >= y).booleanValue()";
        int start = contents.indexOf("booleanValue");
        int end = start + "booleanValue".length();
        assertType(contents, start, end, "java.lang.Boolean");
    }
    
    public void testInferBoolean5() throws Exception {
        String contents = "(x != y).booleanValue()";
        int start = contents.indexOf("booleanValue");
        int end = start + "booleanValue".length();
        assertType(contents, start, end, "java.lang.Boolean");
    }
    
    public void testInferBoolean6() throws Exception {
        String contents = "(x == y).booleanValue()";
        int start = contents.indexOf("booleanValue");
        int end = start + "booleanValue".length();
        assertType(contents, start, end, "java.lang.Boolean");
    }
    
    public void testInferBoolean7() throws Exception {
        String contents = "(x in y).booleanValue()";
        int start = contents.indexOf("booleanValue");
        int end = start + "booleanValue".length();
        assertType(contents, start, end, "java.lang.Boolean");
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
        String expr = "x";
        assertType(contents, contents.indexOf(expr), contents.indexOf(expr)+expr.length(), "java.lang.String");
    }
    // GRECLISPE-1244
    public void testStaticMethodCall5() throws Exception {
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
    // GRECLISPE-1244
    public void testStaticMethodCall6() throws Exception {
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
    public void testSuperFieldReference() throws Exception {
        String contents = "class B extends A {\n def other() { \n myOther } } \n class A { String myOther } ";
        String expr = "myOther"; 
        assertType(contents, contents.indexOf(expr), contents.indexOf(expr)+expr.length(), "java.lang.String");
    }
    
    public void testInferFieldWithInitializer1() throws Exception {
        String contents = "class A {\ndef x = 9\n}\n new A().x";
        int start = contents.lastIndexOf('x');
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    
    public void testInferFieldWithInitializer2() throws Exception {
        createUnit("A", "class A {\ndef x = 9\n} ");
        String contents = "new A().x";
        int start = contents.lastIndexOf('x');
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    
    public void testTernaryExpression() throws Exception {
        String contents = "def x = true ? 2 : 1\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    
    public void testElvisOperator() throws Exception {
        String contents = "def x = 2 ?: 1\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    
    public void testRangeExpression1() throws Exception {
        String contents = "def x = 0 .. 5\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "groovy.lang.Range<java.lang.Integer>");
    }
    
    public void testRangeExpression2() throws Exception {
        String contents = "def x = 0 ..< 5\nx";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "groovy.lang.Range<java.lang.Integer>");
    }
    
    public void testRangeExpression3() throws Exception {
        String contents = "(1..10).getFrom()";
        int start = contents.lastIndexOf("getFrom");
        int end = start + "getFrom".length();
        assertType(contents, start, end, "java.lang.Comparable<java.lang.Integer>");
    }
    
    public void testRangeExpression4() throws Exception {
        String contents = "(1..10).getTo()";
        int start = contents.lastIndexOf("getTo");
        int end = start + "getTo".length();
        assertType(contents, start, end, "java.lang.Comparable<java.lang.Integer>");
    }
    
    public void testRangeExpression5() throws Exception {
        String contents = "(1..10).step(0)";
        int start = contents.lastIndexOf("step");
        int end = start + "step".length();
        assertType(contents, start, end, "java.util.List<java.lang.Integer>");
    }
    
    public void testRangeExpression6() throws Exception {
        String contents = "(1..10).step(0, { })";
        int start = contents.lastIndexOf("step");
        int end = start + "step".length();
        assertType(contents, start, end, "java.lang.Void");
    }
    
    public void testInnerClass1() throws Exception {
        String contents = "class Outer { class Inner { } \nInner x }\nnew Outer().x ";
        int start = contents.lastIndexOf("x");
        int end = start + 1;
        assertType(contents, start, end, "Outer$Inner");
    }
    
    public void testInnerClass2() throws Exception {
        String contents = "class Outer { class Inner { class InnerInner{ } }\n Outer.Inner.InnerInner x }\nnew Outer().x ";
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
    
    public void testConstantFromSuper() throws Exception {
        String contents = "public interface Constants {\n" +
                          "int FIRST = 9;\n" +
                          "}\n" +
                          "class UsesConstants implements Constants {\n" +
                          "def x() {\n" +
                          "FIRST\n" +
                          "}\n" +
                          "}";
        int start = contents.lastIndexOf("FIRST");
        int end = start + "FIRST".length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    
    public void testAssignementInInnerBlock() throws Exception {
        String contents = "def xxx\n if (true) { xxx = \"\" \n xxx} ";
        int start = contents.lastIndexOf("xxx");
        int end = start + "xxx".length();
        assertType(contents, start, end, "java.lang.String");
    }

    public void testAssignementInInnerBlock2() throws Exception {
        String contents = "def xxx\n if (true) { xxx = \"\" \n }\n xxx";
        int start = contents.lastIndexOf("xxx");
        int end = start + "xxx".length();
        assertType(contents, start, end, "java.lang.String");
    }
    // GRECLIPSE-743
    public void testOverrideCategory1() throws Exception {
        String contents = "class A { }\n new A().getAt() ";
        int start = contents.lastIndexOf(GET_AT);
        int end = start + GET_AT.length();
        assertType(contents, start, end, "java.lang.Object");
        assertDeclaringType(contents, start, end, "org.codehaus.groovy.runtime.DefaultGroovyMethods");
    }
    
    public void testOverrideCategory2() throws Exception {
        String contents = "class A {\n A getAt(prop) { \n new A() \n } }\n new A().getAt()";
        int start = contents.lastIndexOf(GET_AT);
        int end = start + GET_AT.length();
        assertType(contents, start, end, "A");
        assertDeclaringType(contents, start, end, "A");
    }
    
    public void testOverrideCategory3() throws Exception {
        String contents = "class A {\n A getAt(prop) { \n new A() \n } }\n class B extends A { }\n new B().getAt()";
        int start = contents.lastIndexOf(GET_AT);
        int end = start + GET_AT.length();
        assertType(contents, start, end, "A");
        assertDeclaringType(contents, start, end, "A");
    }
    
    public void testGRECLIPSE731a() throws Exception {
        String contents = "def foo() { } \nString xxx = foo()\nxxx";
        int start = contents.lastIndexOf("xxx");
        int end = start + "xxx".length();
        assertType(contents, start, end, "java.lang.String");
    }
    public void testGRECLIPSE731b() throws Exception {
        String contents = "def foo() { } \ndef xxx = foo()\nxxx";
        int start = contents.lastIndexOf("xxx");
        int end = start + "xxx".length();
        assertType(contents, start, end, "java.lang.Object");
    }
    public void testGRECLIPSE731c() throws Exception {
        String contents = "String foo() { } \ndef xxx = foo()\nxxx";
        int start = contents.lastIndexOf("xxx");
        int end = start + "xxx".length();
        assertType(contents, start, end, "java.lang.String");
    }
    public void testGRECLIPSE731d() throws Exception {
        String contents = "int foo() { } \ndef xxx = foo()\nxxx";
        int start = contents.lastIndexOf("xxx");
        int end = start + "xxx".length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    // ignore assignments to object expressions
    public void testGRECLIPSE731e() throws Exception {
        String contents = "def foo() { } \nString xxx\nxxx = foo()\nxxx";
        int start = contents.lastIndexOf("xxx");
        int end = start + "xxx".length();
        assertType(contents, start, end, "java.lang.String");
    }
    // ignore assignments to object expressions
    public void testGRECLIPSE731f() throws Exception {
        String contents = "class X { String xxx\ndef foo() { }\ndef meth() { xxx = foo()\nxxx } }";
        int start = contents.lastIndexOf("xxx");
        int end = start + "xxx".length();
        assertType(contents, start, end, "java.lang.String");
    }
    public void testGRECLIPSE1720() throws Exception {
        String contents = "import groovy.transform.CompileStatic\n" +
        			"@CompileStatic\n" +
        			"public class Bug {\n" +
        			"enum Letter { A,B,C }\n" +  
        			"boolean bug(Letter l) {\n" +
        			"boolean isEarly = l in [Letter.A,Letter.B]\n" +
        			"isEarly\n" +
    				"}\n" +
    				"}";
        int start = contents.lastIndexOf("isEarly");
        int end = start + "isEarly".length();
        assertType(contents, start, end, "java.lang.Boolean");
    }    
    
    private final static String catchString = "try {     } catch (NullPointerException e) { e }";
    private final static String catchString2 = "try {     } catch (e) { e }";
    private final static String npe = "NullPointerException";
    public void testCatchBlock1() throws Exception {
        int start = catchString.lastIndexOf(npe);
        int end = start + npe.length();
        assertType(catchString, start, end, "java.lang.NullPointerException");
    }
    public void testCatchBlock2() throws Exception {
        int start = catchString.lastIndexOf("e");
        int end = start + 1;
        assertType(catchString, start, end, "java.lang.NullPointerException");
    }
    public void testCatchBlock3() throws Exception {
        int start = catchString.indexOf(npe + " e");
        int end = start + (npe + " e").length();
        assertType(catchString, start, end, "java.lang.NullPointerException");
    }
    public void testCatchBlock4() throws Exception {
        int start = catchString2.indexOf("e");
        int end = start + 1;
        assertType(catchString2, start, end, "java.lang.Exception");
    }
    public void testCatchBlock5() throws Exception {
        int start = catchString2.lastIndexOf("e");
        int end = start + 1;
        assertType(catchString2, start, end, "java.lang.Exception");
    }
    
    public void testAssignment1() throws Exception {
        String contents = "String x = 7\nx";
        int start = contents.lastIndexOf("x");
        int end = start + 1;
        assertType(contents, start, end, "java.lang.String");
    }
    public void testAssignment2() throws Exception {
        String contents = "String x\nx";
        int start = contents.lastIndexOf("x");
        int end = start + 1;
        assertType(contents, start, end, "java.lang.String");
    }
    public void testAssignment3() throws Exception {
        String contents = "String x\nx = 7\nx";
        int start = contents.lastIndexOf("x");
        int end = start + 1;
        assertType(contents, start, end, "java.lang.Integer");
    }
    public void testAssignment4() throws Exception {
        String contents = "String x() { \ndef x = 9\n x}";
        int start = contents.lastIndexOf("x");
        int end = start + 1;
        assertType(contents, start, end, "java.lang.Integer");
    }
    public void testAssignment5() throws Exception {
        String contents = "String x() { \ndef x\nx = 9\n x}";
        int start = contents.lastIndexOf("x");
        int end = start + 1;
        assertType(contents, start, end, "java.lang.Integer");
    }
    public void testInClosure1() throws Exception {
        String contents = 
                "class Bar {\n" +
        		"  def meth() { } }\n" +
        		"new Bar().meth {\n delegate }";
        int start = contents.lastIndexOf("delegate");
        int end = start + "delegate".length();
        assertType(contents, start, end, "Bar");
    }
    public void testInClosure2() throws Exception {
        String contents = 
                "class Bar {\n" +
        		"  def meth(x) { } }\n" +
        		"new Bar().meth(6) {\n this }";
        int start = contents.lastIndexOf("this");
        int end = start + "this".length();
        assertType(contents, start, end, "Search");
    }
    public void testInClosure2a() throws Exception {
        String contents = 
                "class Bar {\n" +
                "  def meth(x) { } }\n" +
                "new Bar().meth(6) {\n delegate }";
        int start = contents.lastIndexOf("delegate");
        int end = start + "delegate".length();
        assertType(contents, start, end, "Bar");
    }
    public void testInClosure2b() throws Exception {
        String contents = 
                "class Bar {\n" +
                "  def meth(x) { } }\n" +
                "new Bar().meth(6) {\n owner }";
        int start = contents.lastIndexOf("owner");
        int end = start + "owner".length();
        assertType(contents, start, end, "Search");
    }
    // closure in a closure and owner is outer closure
    public void testInClosure2c() throws Exception {
        String contents = 
                "first {\n second {\n owner } }";
        int start = contents.lastIndexOf("owner");
        int end = start + "owner".length();
        if (GroovyUtils.GROOVY_LEVEL > 17) {
            assertType(contents, start, end, "groovy.lang.Closure<java.lang.Object<V>>");
        } else {
            assertType(contents, start, end, "groovy.lang.Closure");
        }
    }
    public void testInClosure3() throws Exception {
        String contents = "class Baz { }\n" +
        		"class Bar {\n" +
        		"  def meth(x) { }\n" +
        		"}\n" +
        		"new Bar().meth(6) {\n" +
        		"  new Baz()" +
        		"}";
        int start = contents.lastIndexOf("Baz");
        int end = start + "Baz".length();
        assertType(contents, start, end, "Baz");
    }
    
    public void testInClosure4() throws Exception {
        String contents = 
                "''.foo {\n" +
                "  substring" +
                "}";
        
        int start = contents.lastIndexOf("substring");
        int end = start + "substring".length();
        assertType(contents, start, end, "java.lang.String");
    }
    
    public void testInClosure5() throws Exception {
        String contents = 
                "''.foo {\n" +
                "  delegate.substring()" +
                "}";
        
        int start = contents.lastIndexOf("substring");
        int end = start + "substring".length();
        assertType(contents, start, end, "java.lang.String");
    }
    
    public void testInClosure6() throws Exception {
        String contents = 
                "''.foo {\n" +
                "  this.substring" +
                "}";
        
        int start = contents.lastIndexOf("substring");
        int end = start + "substring".length();
        assertUnknownConfidence(contents, start, end, "Search", false);
    }
    
    public void testInClosure7() throws Exception {
        String contents = 
                "''.foo {\n" +
                "  this\n" +
                "}";
        
        int start = contents.lastIndexOf("this");
        int end = start + "this".length();
        assertType(contents, start, end, "Search", false);
    }

    
    public void testClosure4() throws Exception {
        String contents = 
            "class Baz {\n" +
            "  URL other\n" +
            "  def method() {\n" +
            "    sumthin { other }\n" +
            "  }\n" +
            "}";
        int start = contents.lastIndexOf("other");
        int end = start + "other".length();
        assertType(contents, start, end, "java.net.URL");
    }
    

    public void testClosure5() throws Exception {
        String contents = 
                "class Baz {\n" +
                        "  URL other\n" +
                        "  def method() {\n" +
                        "    sumthin {\n" +
                        "      delegate\n" +
                        "      owner\n" +
                        "      getDelegate()\n" +
                        "      getOwner()\n" +
                        "    }\n" +
                        "  }\n" +
                        "}";
        int start = contents.lastIndexOf("delegate");
        int end = start + "delegate".length();
        assertType(contents, start, end, "Baz");
        start = contents.lastIndexOf("owner");
        end = start + "owner".length();
        assertType(contents, start, end, "Baz");
        start = contents.lastIndexOf("getDelegate");
        end = start + "getDelegate".length();
        assertType(contents, start, end, "Baz");
        start = contents.lastIndexOf("getOwner");
        end = start + "getOwner".length();
        assertType(contents, start, end, "Baz");
    }

    
    public void testClosure6() throws Exception {
        String contents = 
                "def x = {\n" +
                "maximumNumberOfParameters\n" +
                "getMaximumNumberOfParameters()\n" +
                "thisObject\n" +
                "getThisObject()\n" +
                "}";
        int start = contents.lastIndexOf("maximumNumberOfParameters");
        int end = start + "maximumNumberOfParameters".length();
        assertType(contents, start, end, "java.lang.Integer");
        start = contents.lastIndexOf("getMaximumNumberOfParameters");
        end = start + "getMaximumNumberOfParameters".length();
        assertType(contents, start, end, "java.lang.Integer");
        start = contents.lastIndexOf("thisObject");
        end = start + "thisObject".length();
        assertType(contents, start, end, "java.lang.Object");
        start = contents.lastIndexOf("getThisObject");
        end = start + "getThisObject".length();
        assertType(contents, start, end, "java.lang.Object");
    }
    public void testClosure7() throws Exception {
        String contents = 
                "def x = { def y = {\n" +
                "maximumNumberOfParameters\n" +
                "getMaximumNumberOfParameters()\n" +
                "thisObject\n" +
                "getThisObject()\n" +
                "}}";
        int start = contents.lastIndexOf("maximumNumberOfParameters");
        int end = start + "maximumNumberOfParameters".length();
        assertType(contents, start, end, "java.lang.Integer");
        start = contents.lastIndexOf("getMaximumNumberOfParameters");
        end = start + "getMaximumNumberOfParameters".length();
        assertType(contents, start, end, "java.lang.Integer");
        start = contents.lastIndexOf("thisObject");
        end = start + "thisObject".length();
        assertType(contents, start, end, "java.lang.Object");
        start = contents.lastIndexOf("getThisObject");
        end = start + "getThisObject".length();
        assertType(contents, start, end, "java.lang.Object");
    }

    
    // the declaring type of things inside of a closure should be the declaring 
    // type of the method that calls the closure
    public void testInClosureDeclaringType1() throws Exception {
        String contents = 
                "class Baz {\n" +
        		"  def method() { }\n" +
        		"  Integer other() { }\n" +
        		"}\n" +
        		"class Bar extends Baz {\n" +
                "  def other(x) { }\n" +
                "}\n" +
        		"new Bar().method {\n " +
        		"  other\n" +
        		"}";
        int start = contents.lastIndexOf("other");
        int end = start + "other".length();
        assertType(contents, start, end, "java.lang.Integer");
        assertDeclaringType(contents, start, end, "Baz");
    }
    // Unknown references should have the declaring type of the closure
    public void testInClosureDeclaringType2() throws Exception {
        String contents = 
                "class Baz {\n" +
                "  def method() { }\n" +
                "}\n" +
                "class Bar extends Baz {\n" +
                "}\n" +
                "new Bar().method {\n " +
                "  other\n" +
                "}";
        int start = contents.lastIndexOf("other");
        int end = start + "other".length();
        assertDeclaringType(contents, start, end, "Search", false, true);
    }
    
    // Unknown references should have the delegate type of the closure
    public void testInClosureDeclaringType3() throws Exception {
        String contents = 
                "class Baz {\n" +
                "  def method() { }\n" +
                "}\n" +
                "class Bar extends Baz {\n" +
                "  def sumthin() {\n" +
                "    new Bar().method {\n " +
                "      other\n" +
                "    }\n" +
                "  }\n" +
                "}";
        int start = contents.lastIndexOf("other");
        int end = start + "other".length();
        assertDeclaringType(contents, start, end, "Bar", false, true);
    }

    // 'this' is always the enclosing type
    public void testInClosureDeclaringType4() throws Exception {
        String contents = 
                "class Bar {\n" +
                "  def method() { }\n" +
                "}\n" +
                "new Bar().method {\n " +
                "  this\n" +
                "}";
        int start = contents.lastIndexOf("this");
        int end = start + "this".length();
        assertDeclaringType(contents, start, end, "Search", false);
    }
    
    // 'delegate' always has declaring type of closure
    public void testInClosureDeclaringType5() throws Exception {
        // failing on 1.7
        if (GroovyUtils.GROOVY_LEVEL > 17) {
            String contents = 
                    "class Bar {\n" +
                    "  def method() { }\n" +
                    "}\n" +
                    "new Bar().method {\n " +
                    "  delegate\n" +
                    "}";
            int start = contents.lastIndexOf("delegate");
            int end = start + "delegate".length();
            assertDeclaringType(contents, start, end, "groovy.lang.Closure<java.lang.Object<V>>", false);
        }
    }
    
    // Unknown references should have the declaring type of the enclosing method
    public void testInClassDeclaringType1() throws Exception {
        String contents = 
                "class Baz {\n" +
                "  def method() {\n" +
                "    other\n" +
                "  }\n" +
                "}";
        int start = contents.lastIndexOf("other");
        int end = start + "other".length();
        assertDeclaringType(contents, start, end, "Baz", false, true);
    }
    // Unknown references should have the declaring type of the enclosing closure
    public void testInClassDeclaringType2() throws Exception {
        String contents = 
            "class Baz {\n" +
            "  def method = {\n" +
            "    other\n" +
            "  }\n" +
            "}";
        int start = contents.lastIndexOf("other");
        int end = start + "other".length();
        assertDeclaringType(contents, start, end, "Baz", false, true);
    }
    
    public void testDoubleClosure1() throws Exception {
        String contents = 
                "''.foo {\n" +
                "  1.foo {\n" +
                "    intValue\n" +
                "  }" +
                "}";
        
        int start = contents.lastIndexOf("intValue");
        int end = start + "intValue".length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    
    public void testDoubleClosure2() throws Exception {
        String contents = 
                "''.foo {\n" +
                "  1.foo {\n" +
                "    intValue()\n" +
                "  }" +
                "}";
        
        int start = contents.lastIndexOf("intValue");
        int end = start + "intValue".length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    
    public void testDoubleClosure2a() throws Exception {
        String contents = 
                "''.foo {\n" +
                "  1.foo {\n" +
                "    delegate.intValue()\n" +
                "  }" +
                "}";
        
        int start = contents.lastIndexOf("intValue");
        int end = start + "intValue".length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    
    // test DGM
    public void testDoubleClosure3() throws Exception {
        String contents = 
                "''.foo {\n" +
                "  1.foo {\n" +
                "    abs\n" +
                "  }" +
                "}";
        
        int start = contents.lastIndexOf("abs");
        int end = start + "abs".length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    
    public void testDoubleClosure4() throws Exception {
        String contents = 
                "''.foo {\n" +
                "  1.foo {\n" +
                "    abs()\n" +
                "  }" +
                "}";
        
        int start = contents.lastIndexOf("abs");
        int end = start + "abs".length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testDoubleClosure5() throws Exception {
        String contents = 
                "''.foo {\n" +
                "  1.foo {\n" +
                "    delegate.abs()\n" +
                "  }" +
                "}";
        
        int start = contents.lastIndexOf("abs");
        int end = start + "abs".length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    public void testDoubleClosure6() throws Exception {
        String contents = 
                "''.foo {\n" +
                "  1.foo {\n" +
                "    this.abs()\n" +
                "  }" +
                "}";
        
        int start = contents.lastIndexOf("abs");
        int end = start + "abs".length();
        assertUnknownConfidence(contents, start, end, "Search", false);
    }

    public void testDoubleClosure7() throws Exception {
        String contents = 
                "''.foo {\n" +
                "  1.foo {\n" +
                "    this\n" +
                "  }" +
                "}";
        
        int start = contents.lastIndexOf("this");
        int end = start + "this".length();
        assertType(contents, start, end, "Search");
    }

    // GRECLIPSE-1748
    // Closure type inference with @CompileStatic  
    public void testClosureTypeInference1() throws Exception {
        if (GroovyUtils.GROOVY_LEVEL < 21) {
            return;
        }
        String contents =
                "import groovy.beans.Bindable\n" +
                "import groovy.transform.CompileStatic\n" +
                "class A {\n" +
                "    @Bindable\n" +
                "    String foo\n" +
                "    @CompileStatic" +
                "    static void main(String[] args) {\n" +
                "        A a = new A()\n" +
                "        a.foo = 'old'\n" +
                "        a.addPropertyChangeListener('foo') {\n" +
                "            println 'foo changed: ' + it.oldValue + ' -> ' + it.newValue\n" +
                "        }\n" +
                "        a.foo = 'new'\n" +
                "    }\n" +
                "}";
        
        int start = contents.lastIndexOf("it");
        int end = start + "it".length();
        assertType(contents, start, end, "java.beans.PropertyChangeEvent");
    }

    // Closure type inference without @CompileStatic
    public void testClosureTypeInference2() throws Exception {
        if (GroovyUtils.GROOVY_LEVEL < 21) {
            return;
        }
        String contents =
                "import groovy.beans.Bindable\n" +
                "class A {\n" +
                "    @Bindable\n" +
                "    String foo\n" +
                "    static void main(String[] args) {\n" +
                "        A a = new A()\n" +
                "        a.foo = 'old'\n" +
                "        a.addPropertyChangeListener('foo') {\n" +
                "            println 'foo changed: ' + it.oldValue + ' -> ' + it.newValue\n" +
                "        }\n" +
                "        a.foo = 'new'\n" +
                "    }\n" +
                "}";
        
        int start = contents.lastIndexOf("it");
        int end = start + "it".length();
        assertType(contents, start, end, "java.beans.PropertyChangeEvent");
    }

    // GRECLIPSE-1751
    // Test 'with' operator. No annotations.
    public void testWithAndClosure1() throws Exception {
        createUnit("p", "D",
                "package p\n" +
                "class D {\n" +
                "    String foo\n" +
                "    D bar\n" +
                "}");
        String contents =
                "package p\n" +
                "class E {\n" +
                "    D d = new D()\n" +
                "    void doSomething() {\n" +
                "        d.with {\n" +
                "            foo = 'foo'\n" +
                "            bar = new D()\n" +
                "            bar.foo = 'bar'\n" +
                "        }\n" +
                "    }\n" +
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

    // Test 'with' operator. @TypeChecked annotation.
    public void testWithAndClosure2() throws Exception {
        createUnit("p", "D",
                "package p\n" +
                "class D {\n" +
                "    String foo\n" +
                "    D bar\n" +
                "}");
        String contents =
                "package p\n" +
                "@groovy.transform.TypeChecked\n" +
                "class E {\n" +
                "    D d = new D()\n" +
                "    void doSomething() {\n" +
                "        d.with {\n" +
                "            foo = 'foo'\n" +
                "            bar = new D()\n" +
                "            bar.foo = 'bar'\n" +
                "        }\n" +
                "    }\n" +
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

    // Test 'with' operator. @CompileStatic annotation.
    public void testWithAndClosure3() throws Exception {
        createUnit("p", "D",
                "package p\n" +
                "class D {\n" +
                "    String foo\n" +
                "    D bar\n" +
                "}");
        String contents =
                "package p\n" +
                "@groovy.transform.CompileStatic\n" +
                "class E {\n" +
                "    D d = new D()\n" +
                "    void doSomething() {\n" +
                "        d.with {\n" +
                "            foo = 'foo'\n" +
                "            bar = new D()\n" +
                "            bar.foo = 'bar'\n" +
                "        }\n" +
                "    }\n" +
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

    // Another test 'with' operator. @CompileStatic annotation.
    public void testWithAndClosure4() throws Exception {
        createUnit("p", "D",
                "package p\n" +
                "class D {\n" +
                "    String foo\n" +
                "    D bar = new D()\n" +
                "}");
        String contents =
                "package p\n" +
                "@groovy.transform.CompileStatic\n" +
                "class E {\n" +
                "    D d = new D()\n" +
                "    void doSomething() {\n" +
                "        d.with {\n" +
                "            foo = 'foo'\n" +
                "            bar.foo = 'bar'\n" +
                "        }\n" +
                "    }\n" +
                "}";

        int start = contents.indexOf("foo");
        int end = start + "foo".length();
        assertType(contents, start, end, "java.lang.String");

        start = contents.indexOf("bar", end);
        end = start + "bar".length();
        assertType(contents, start, end, "p.D");

        start = contents.indexOf("foo", end);
        end = start + "foo".length();
        assertType(contents, start, end, "java.lang.String");
    }

    // Unknown references should have the declaring type of the enclosing closure
    public void testInScriptDeclaringType() throws Exception {
        String contents = 
            "other\n";
        int start = contents.lastIndexOf("other");
        int end = start + "other".length();
        assertDeclaringType(contents, start, end, "Search", false, true);
    }
    public void testStaticImports1() throws Exception {
        String contents = "import static javax.swing.text.html.HTML.NULL_ATTRIBUTE_VALUE\n" + 
                          "NULL_ATTRIBUTE_VALUE";
        int start = contents.lastIndexOf("NULL_ATTRIBUTE_VALUE");
        int end = start + "NULL_ATTRIBUTE_VALUE".length();
        assertType(contents, start, end, "java.lang.String");
    }
    public void testStaticImports2() throws Exception {
        String contents = "import static javax.swing.text.html.HTML.getAttributeKey\n" + 
                          "getAttributeKey('')";
        int start = contents.lastIndexOf("getAttributeKey");
        int end = start + "getAttributeKey('')".length();
        assertType(contents, start, end, "javax.swing.text.html.HTML$Attribute");
    }
    public void testStaticImports3() throws Exception {
        String contents = "import static javax.swing.text.html.HTML.*\n" + 
                          "NULL_ATTRIBUTE_VALUE";
        int start = contents.lastIndexOf("NULL_ATTRIBUTE_VALUE");
        int end = start + "NULL_ATTRIBUTE_VALUE".length();
        assertType(contents, start, end, "java.lang.String");
    }
    public void testStaticImports4() throws Exception {
        String contents = "import static javax.swing.text.html.HTML.*\n" + 
                          "getAttributeKey('')";
        int start = contents.lastIndexOf("getAttributeKey");
        int end = start + "getAttributeKey('')".length();
        assertType(contents, start, end, "javax.swing.text.html.HTML$Attribute");
    }
    
    public void testDGM1() throws Exception {
        String contents = "\"$print\"";
        String lookFor = "print";
        int start = contents.indexOf(lookFor);
        int end = start + lookFor.length();
        assertDeclaringType(contents, start, end, "groovy.lang.Script");
    }
    public void testDGM2() throws Exception {
        String contents = "\"${print}\"";
        String lookFor = "print";
        int start = contents.indexOf(lookFor);
        int end = start + lookFor.length();
        assertDeclaringType(contents, start, end, "groovy.lang.Script");
    }
    public void testDGM3() throws Exception {
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

    
    public void testGetAt1() throws Exception {
        int start = CONTENTS_GETAT1.indexOf("startsWith");
        int end = start + "startsWith".length();
        assertDeclaringType(CONTENTS_GETAT1, start, end, "java.lang.String");
    }
    public void testGetAt2() throws Exception {
        int start = CONTENTS_GETAT1.lastIndexOf("startsWith");
        int end = start + "startsWith".length();
        assertDeclaringType(CONTENTS_GETAT1, start, end, "java.lang.String");
    }
    public void testGetAt3() throws Exception {
        int start = CONTENTS_GETAT2.indexOf("startsWith");
        int end = start + "startsWith".length();
        // expecting unknown confidence because getAt not explicitly defined
        assertDeclaringType(CONTENTS_GETAT2, start, end, "GetAt", false, true);
    }
    public void testGetAt4() throws Exception {
        int start = CONTENTS_GETAT2.lastIndexOf("startsWith");
        int end = start + "startsWith".length();
        // expecting unknown confidence because getAt not explicitly defined
        assertDeclaringType(CONTENTS_GETAT2, start, end, "GetAt", false, true);
    }
    
    // GRECLIPSE-1013
    public void testCategoryMethodAsField() throws Exception {
        String contents = "''.toURL().text";
        
        int textStart = contents.indexOf("text");
        int textEnd = textStart + "text".length();
        if (GroovyUtils.GROOVY_LEVEL >= 20) {
            assertDeclaringType(contents, textStart, textEnd, "org.codehaus.groovy.runtime.ResourceGroovyMethods");
        } else {
            assertDeclaringType(contents, textStart, textEnd, "org.codehaus.groovy.runtime.DefaultGroovyMethods");
        }
    }
    
    public void testClassReference1() throws Exception {
        String contents = "String";
        assertDeclaringType(contents, 0, contents.length(), "java.lang.String");
    }
    
    public void testClassReference2() throws Exception {
        String contents = "String.substring";
        int textStart = contents.indexOf("substring");
        int textEnd = textStart + "substring".length();
        assertDeclaringType(contents, textStart, textEnd, "java.lang.String", false, true);
    }
    
    public void testClassReference3() throws Exception {
        String contents = "String.getPackage()";
        int textStart = contents.indexOf("getPackage");
        int textEnd = textStart + "getPackage".length();
        assertType(contents, textStart, textEnd, "java.lang.Package");
    }
    
    public void testClassReference4() throws Exception {
        String contents = "String.class.getPackage()";
        int textStart = contents.indexOf("getPackage");
        int textEnd = textStart + "getPackage".length();
        assertType(contents, textStart, textEnd, "java.lang.Package");
    }
    
    public void testClassReference5() throws Exception {
        String contents = "String.class.package";
        int textStart = contents.indexOf("package");
        int textEnd = textStart + "package".length();
        assertType(contents, textStart, textEnd, "java.lang.Package");
    }
    
    public void testClassReference6() throws Exception {
        String contents = "String.class";
        // in the groovy AST, this is all one ast expression node (a class expression)
        int textStart = contents.indexOf("String.class");
        int textEnd = textStart + "String.class".length();
        assertDeclaringType(contents, textStart, textEnd, "java.lang.Class<java.lang.Object<T>>", false, false);
    }
 
    public void testMultiDecl1() throws Exception {
        String contents = "def (x, y) = []\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Object");
        assertType(contents, yStart, yStart+1, "java.lang.Object");
    }
    public void testMultiDecl2() throws Exception {
        String contents = "def (x, y) = [1]\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Integer");
        assertType(contents, yStart, yStart+1, "java.lang.Integer");
    }
    public void testMultiDecl3() throws Exception {
        String contents = "def (x, y) = [1,1]\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Integer");
        assertType(contents, yStart, yStart+1, "java.lang.Integer");
    }
    public void testMultiDecl4() throws Exception {
        String contents = "def (x, y) = [1,'']\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Integer");
        assertType(contents, yStart, yStart+1, "java.lang.String");
    }
    public void testMultiDecl6() throws Exception {
        String contents = "def (x, y) = new ArrayList()\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Object<E>");
        assertType(contents, yStart, yStart+1, "java.lang.Object<E>");
    }
    public void testMultiDecl7() throws Exception {
        String contents = "def (x, y) = new ArrayList<Double>()\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Double");
        assertType(contents, yStart, yStart+1, "java.lang.Double");
    }
    public void testMultiDecl8() throws Exception {
        String contents = "Double[] meth() { }\ndef (x, y) = meth()\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Double");
        assertType(contents, yStart, yStart+1, "java.lang.Double");
    }
    public void testMultiDecl9() throws Exception {
        String contents = "List<Double> meth() { }\ndef (x, y) = meth()\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Double");
        assertType(contents, yStart, yStart+1, "java.lang.Double");
    }
    public void testMultiDecl10() throws Exception {
        String contents = "List<Double> field\ndef (x, y) = field\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Double");
        assertType(contents, yStart, yStart+1, "java.lang.Double");
    }
    public void testMultiDecl11() throws Exception {
        String contents = "List<Double> field\ndef x\ndef y\n (x, y)= field\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Double");
        assertType(contents, yStart, yStart+1, "java.lang.Double");
    }
    public void testMultiDecl12() throws Exception {
        String contents = "def (x, y) = 1d\nx\ny";
        int xStart = contents.lastIndexOf("x");
        int yStart = contents.lastIndexOf("y");
        assertType(contents, xStart, xStart+1, "java.lang.Double");
        assertType(contents, yStart, yStart+1, "java.lang.Double");
    }
    
    
    // GRECLIPSE-1174 groovy casting
    public void testAsExpression1() throws Exception {
        String contents = "(1 as int).intValue()";
        int start = contents.lastIndexOf("intValue");
        assertType(contents, start, start+"intValue".length(), "java.lang.Integer");
    }
    // GRECLIPSE-1174 groovy casting
    public void testAsExpression2() throws Exception {
        String contents = "class Flar { int x\n }\n(null as Flar).x";
        int start = contents.lastIndexOf("x");
        assertType(contents, start, start+"x".length(), "java.lang.Integer");
    }
    
    // GRECLIPSE-1264
    public void testImplicitVar1() throws Exception {
        String contents = "class SettingUndeclaredProperty {\n" + 
        		"    public void mymethod() {\n" + 
        		"        doesNotExist = \"abc\"\n" + 
        		"    }\n" + 
        		"}";
        int start = contents.lastIndexOf("doesNotExist");
        assertUnknownConfidence(contents, start, start+"doesNotExist".length(), "SettingUndeclaredProperty", false);
    }
    // GRECLIPSE-1264
    public void testImplicitVar2() throws Exception {
        String contents = "class SettingUndeclaredProperty {\n" + 
                "     def r = {\n" + 
                "        doesNotExist = 0\n" + 
                "    }\n" + 
                "}";
        int start = contents.lastIndexOf("doesNotExist");
        assertUnknownConfidence(contents, start, start+"doesNotExist".length(), "SettingUndeclaredProperty", false);
    }
    // GRECLIPSE-1264
    public void testImplicitVar3() throws Exception {
        String contents = 
                "doesNotExist";
        int start = contents.lastIndexOf("doesNotExist");
        assertUnknownConfidence(contents, start, start+"doesNotExist".length(), "Search", false);
    }
    // GRECLIPSE-1264
    public void testImplicitVar4() throws Exception {
        String contents = "doesNotExist = 9";
        int start = contents.lastIndexOf("doesNotExist");
        assertDeclaringType(contents, start, start+"doesNotExist".length(), "Search", false);
    }
    // GRECLIPSE-1264
    public void testImplicitVar5() throws Exception {
        String contents = 
                "doesNotExist = 9\n" +
                "def x = {doesNotExist }";
        int start = contents.lastIndexOf("doesNotExist");
        assertDeclaringType(contents, start, start+"doesNotExist".length(), "Search", false);
    }
    // GRECLIPSE-1264
    public void testImplicitVar6() throws Exception {
        String contents = 
                "" +
                "def x = {\n" +
                "doesNotExist = 9\n" +
                "doesNotExist }";
        int start = contents.lastIndexOf("doesNotExist");
        assertDeclaringType(contents, start, start+"doesNotExist".length(), "Search", false);
    }
    // GRECLIPSE-1264
    public void testImplicitVar7() throws Exception {
        String contents = 
                "def z() {\n" + 
                "    doesNotExist = 9\n" + 
                "}\n";
        int start = contents.lastIndexOf("doesNotExist");
        assertUnknownConfidence(contents, start, start+"doesNotExist".length(), "Search", false);
    }
    
    
    // nested expressions of various forms
    public void testNested1() throws Exception {
        String contents = 
                "(true ? 2 : 7) + 9";
        assertType(contents, "java.lang.Integer");
    }
    
    // nested expressions of various forms
    public void testNested2() throws Exception {
        String contents = 
                "(true ? 2 : 7) + (true ? 2 : 7)";
        assertType(contents, "java.lang.Integer");
    }
    
    // nested expressions of various forms
    public void testNested3() throws Exception {
        String contents = 
                "(8 ?: 7) + (8 ?: 7)";
        assertType(contents, "java.lang.Integer");
    }
    
    // nested expressions of various forms
    public void testNested4() throws Exception {
        createUnit("", "Foo", "class Foo { int prop }");
        String contents = 
                "(new Foo().@prop) + (8 ?: 7)";
        assertType(contents, "java.lang.Integer");
    }
    
    
    
    public void testPostfix() throws Exception {
        // failing for groovy 1.7
        if (GroovyUtils.GROOVY_LEVEL > 17) { 
            String contents = 
                    "int i = 0\n" + 
                    "def list = [0]\n" + 
                    "list[i]++";
            int start = contents.lastIndexOf('i');
            assertType(contents, start, start +1, "java.lang.Integer");
        }
    }

    // GRECLIPSE-1302
    public void testNothingIsUnknown() throws Exception {
        assertNoUnknowns("1 > 4\n" + 
                "1 < 1\n" + 
                "1 >= 1\n" + 
                "1 <= 1\n" + 
                "1 <=> 1\n" + 
                "1 == 1\n" +
                "[1,9][0]");
    }
    public void testNothingIsUnknownWithCategories() throws Exception {
        assertNoUnknowns("class Me {\n" + 
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

    // GRECLIPSE-1304
    public void testNoGString1() throws Exception {
        assertNoUnknowns("'$'\n'${}\n'${a}'\n'$a'");
    }
    public void testClosureReferencesSuperClass() throws Exception {
        assertNoUnknowns("class MySuper {\n" + 
        		"    public void insuper() {  }\n" + 
        		"}\n" + 
        		"\n" + 
        		"class MySub extends MySuper {\n" + 
        		"    public void foo() {\n" + 
        		"        [1].each {\n" + 
        		// this line is problematic --- references super class
        		"            insuper(\"3\")\n" + 
        		"        }\n" + 
        		"    }\n" + 
        		"}");
    }
    
    // GRECLIPSE-1341
    public void testDeclarationAtBeginningOfMethod() throws Exception {
        String contents = "class Problem2 {\n" + 
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
    
    public void testGRECLIPSE1348() throws Exception {
        String contents = "class A {\n" + 
        		"    def myMethod(String owner) {\n" + 
        		"        return { return owner }\n" + 
        		"    }\n" + 
        		"}";
        int start = contents.lastIndexOf("owner");
        int end = start + "owner".length();
        assertType(contents, start, end, "java.lang.String");
    } 

    public void testGRECLIPSE1348a() throws Exception {
        String contents = "class A {\n" + 
                "    def myMethod(String notOwner) {\n" + 
                "        return { return owner }\n" + 
                "    }\n" + 
                "}";
        int start = contents.lastIndexOf("owner");
        int end = start + "owner".length();
        assertType(contents, start, end, "A");
    }
    
    public void testAnonInner1() throws Exception {
		String contents = "def foo = new Runnable() { void run() {} }";
        int start = contents.lastIndexOf("Runnable");
        int end = start + "Runnable".length();
        assertType(contents, start, end, "java.lang.Runnable");
	}
    public void testAnonInner2() throws Exception {
    	String contents = "def foo = new Comparable<String>() { int compareTo(String a, String b) {} }";
    	int start = contents.lastIndexOf("Comparable");
    	int end = start + "Comparable".length();
    	assertType(contents, start, end, "java.lang.Comparable<java.lang.String>");
    }
    public void testAnonInner3() throws Exception {
    	String contents = "def foo = new Comparable<String>() { int compareTo(String a, String b) { compareTo()} }";
    	int start = contents.lastIndexOf("compareTo");
    	int end = start + "compareTo".length();
    	assertDeclaringType(contents, start, end, "Search$1");
    }
    public void testAnonInner4() throws Exception {
    	String contents = "def foo = new Comparable<String>() { int compareTo(String a, String b) {} }\n" +
    			"foo.compareTo";
    	int start = contents.lastIndexOf("compareTo");
    	int end = start + "compareTo".length();
    	assertDeclaringType(contents, start, end, "Search$1");
    }
    public void testAnonInner5() throws Exception {
    	String contents = "def foo = new Comparable<String>() { int compareTo(String a, String b) {} }\n" +
    			"foo = new Comparable<String>() { int compareTo(String a, String b) {} }\n" +
    			"foo.compareTo";
    	int start = contents.lastIndexOf("compareTo");
    	int end = start + "compareTo".length();
    	assertDeclaringType(contents, start, end, "Search$2");
    }

	// GRECLIPSE-1638
	public void testInstanceOf1() throws Exception {
		String contents =
				"def m(Object obj) {\n" +
				"    def val = obj\n" +
				"    if (val instanceof String) {\n" +
				"        println val.trim()\n" +
				"    }\n" +
				"}\n";
		int start = contents.indexOf("val");
		int end = start + "val".length();
		assertType(contents, start, end, "java.lang.Object");

		start = contents.lastIndexOf("val");
		end = start + "val".length();
		assertType(contents, start, end, "java.lang.String");
	}

	public void testInstanceOf2() throws Exception {
		String contents =
				"@groovy.transform.TypeChecked\n" +
				"def m(Object obj) {\n" +
				"    def val = obj\n" +
				"    if (val instanceof String) {\n" +
				"        println val.trim()\n" +
				"    }\n" +
				"}\n";
		int start = contents.indexOf("val");
		int end = start + "val".length();
		assertType(contents, start, end, "java.lang.Object");

		start = contents.lastIndexOf("val");
		end = start + "val".length();
		assertType(contents, start, end, "java.lang.String");
	}

	public void testInstanceOf3() throws Exception {
		String contents =
				"@groovy.transform.CompileStatic\n" +
				"def m(Object obj) {\n" +
				"    def val = obj\n" +
				"    if (val instanceof String) {\n" +
				"        println val.trim()\n" +
				"    }\n" +
				"}\n";
		int start = contents.indexOf("val");
		int end = start + "val".length();
		assertType(contents, start, end, "java.lang.Object");

		start = contents.lastIndexOf("val");
		end = start + "val".length();
		assertType(contents, start, end, "java.lang.String");
	}

	public void testInstanceOf4() throws Exception {
		String contents =
				"def m(Object obj) {\n" +
				"    def val = obj\n" +
				"    if (val instanceof String) {\n" +
				"        println val.trim()\n" +
				"    }\n" +
				"    def var = obj\n" +
				"    if (var instanceof Integer) {\n" +
				"        println var.intValue()\n" +
				"    }\n" +
				"}\n";
		int start = contents.indexOf("val");
		int end = start + "val".length();
		assertType(contents, start, end, "java.lang.Object");

		start = contents.lastIndexOf("val");
		end = start + "val".length();
		assertType(contents, start, end, "java.lang.String");

		start = contents.indexOf("var");
		end = start + "var".length();
		assertType(contents, start, end, "java.lang.Object");

		start = contents.lastIndexOf("var");
		end = start + "var".length();
		assertType(contents, start, end, "java.lang.Integer");
	}

    // GRECLIPSE-554
    public void testMapEntries1() throws Exception {
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

    protected void assertNoUnknowns(String contents) {
        GroovyCompilationUnit unit = createUnit("Search", contents);
        
        TypeInferencingVisitorWithRequestor visitor = factory.createVisitor(unit);
        visitor.DEBUG = true;
        UnknownTypeRequestor requestor = new UnknownTypeRequestor();
        visitor.visitCompilationUnit(requestor);
        List<ASTNode> unknownNodes = requestor.getUnknownNodes();
        assertTrue("Should not have found any AST nodes with unknown confidence, but instead found:\n" + unknownNodes, unknownNodes.isEmpty());
    }
}