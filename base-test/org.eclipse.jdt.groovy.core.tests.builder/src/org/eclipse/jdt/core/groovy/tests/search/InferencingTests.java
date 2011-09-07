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

import org.eclipse.jdt.core.tests.util.GroovyUtils;

import junit.framework.Test;

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

    public void testInferNumber1() throws Exception {
        assertType("10", "java.lang.Integer");
    }
    
    // same as above, but test that whitespace is not included
    public void testInferNumber1a() throws Exception {
        assertType("10 ", 0, 2, "java.lang.Integer");
    }

    public void testInferNumber2() throws Exception {
        assertType("1+2", "java.lang.Integer");
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
        String contents = "false ? '' : ''";
        assertType(contents, "java.lang.String");
    }
    
    public void testMatcher1() throws Exception {
        assertType("\"\" =~ /pattern/", "java.util.regex.Matcher");
    }
    
    public void testMatcher2() throws Exception {
        String contents = "(\"\" =~ /pattern/).hasGroup()";
        int start = contents.indexOf("hasGroup");
        int end = start + "hasGroup".length();
        assertType(contents, start, end, "java.lang.Boolean");
    }
    
    public void testPattern() throws Exception {
        assertType("\"\" ==~ /pattern/", "java.lang.Boolean");
    }
    
    public void testInferList1() throws Exception {
        assertType("[]", "java.util.List<java.lang.Object>");
    }
    
    // Should be java.util.List<java.lang.String>
    public void testInferList2() throws Exception {
        assertType("[] << \"\"", "java.util.List<java.lang.Object>");
    }
    
    
    public void testInferClosure1() throws Exception {
        if (GroovyUtils.GROOVY_LEVEL >= 18) {
            assertType("x.&y", "groovy.lang.Closure<java.lang.Object<V>>");
        } else {
            // closure is not parameterized in groovy 1.7 and earlier
            assertType("x.&y", "groovy.lang.Closure");
        }
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
        String contents = "true ? '' : ''";
        assertType(contents, "java.lang.String");
    }
    
    public void testElvisExpression() throws Exception {
        String contents = "'' ?: ''";
        assertType(contents, "java.lang.String");
    }
    
    public void testRangeExpression1() throws Exception {
        String contents = "0 .. 5";
        assertType(contents, "groovy.lang.Range<java.lang.Integer>");
    }
    
    public void testRangeExpression2() throws Exception {
        String contents = "0 ..< 5";
        assertType(contents, "groovy.lang.Range<java.lang.Integer>");
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
    
    private final static String XXX = "xxx";

    public void testAssignementInInnerBlock() throws Exception {
        String contents = "def xxx\n if (true) { xxx = \"\" \n xxx} ";
        int start = contents.lastIndexOf(XXX);
        int end = start + XXX.length();
        assertType(contents, start, end, "java.lang.String");
    }

    public void testAssignementInInnerBlock2() throws Exception {
        String contents = "def xxx\n if (true) { xxx = \"\" \n }\n xxx";
        int start = contents.lastIndexOf(XXX);
        int end = start + XXX.length();
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
        int start = contents.lastIndexOf(XXX);
        int end = start + XXX.length();
        assertType(contents, start, end, "java.lang.String");
    }
    public void testGRECLIPSE731b() throws Exception {
        String contents = "def foo() { } \ndef xxx = foo()\nxxx";
        int start = contents.lastIndexOf(XXX);
        int end = start + XXX.length();
        assertType(contents, start, end, "java.lang.Object");
    }
    public void testGRECLIPSE731c() throws Exception {
        String contents = "String foo() { } \ndef xxx = foo()\nxxx";
        int start = contents.lastIndexOf(XXX);
        int end = start + XXX.length();
        assertType(contents, start, end, "java.lang.String");
    }
    public void testGRECLIPSE731d() throws Exception {
        String contents = "int foo() { } \ndef xxx = foo()\nxxx";
        int start = contents.lastIndexOf(XXX);
        int end = start + XXX.length();
        assertType(contents, start, end, "java.lang.Integer");
    }
    // ignore assignments to object expressions
    public void testGRECLIPSE731e() throws Exception {
        String contents = "def foo() { } \nString xxx\nxxx = foo()\nxxx";
        int start = contents.lastIndexOf(XXX);
        int end = start + XXX.length();
        assertType(contents, start, end, "java.lang.String");
    }
    // ignore assignments to object expressions
    public void testGRECLIPSE731f() throws Exception {
        String contents = "class X { String xxx\ndef foo() { }\ndef meth() { xxx = foo()\nxxx } }";
        int start = contents.lastIndexOf(XXX);
        int end = start + XXX.length();
        assertType(contents, start, end, "java.lang.String");
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
        String contents = "class Bar {\ndef meth() { } }\n new Bar().meth {\n this }";
        int start = contents.lastIndexOf("this");
        int end = start + "this".length();
        assertType(contents, start, end, "Bar");
    }
    public void testInClosure2() throws Exception {
        String contents = "class Bar {\ndef meth(x) { } }\n new Bar().meth(6) {\n this }";
        int start = contents.lastIndexOf("this");
        int end = start + "this".length();
        assertType(contents, start, end, "Bar");
    }
    public void testInClosure3() throws Exception {
        String contents = "class Baz { }\nclass Bar {\ndef meth(x) { } }\n new Bar().meth(6) {\n new Baz() }";
        int start = contents.lastIndexOf("Baz");
        int end = start + "Baz".length();
        assertType(contents, start, end, "Baz");
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
        assertDeclaringType(contents, start, end, "Baz", false, true);
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
        String contents = "'$print'";
        int start = 0;
        int end = contents.length();
        assertDeclaringType(contents, start, end, "groovy.lang.Script");
    }
    public void testDGM2() throws Exception {
        String contents = "'${print}'";
        int start = 0;
        int end = contents.length();
        assertDeclaringType(contents, start, end, "groovy.lang.Script");
    }
    public void testDGM3() throws Exception {
        String contents = "class Foo {\n def m() {\n '${print}'\n } }";
        int start = contents.indexOf("'${print}'");
        int end = start + "'${print}'".length();
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
        assertDeclaringType(contents, textStart, textEnd, "org.codehaus.groovy.runtime.DefaultGroovyMethods");
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
}