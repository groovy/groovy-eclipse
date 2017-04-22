/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.tests;

import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.eclipse.core.runtime.IPath;

/**
 * Tests type inferencing that involve DSLs.
 */
public final class DSLInferencingTests extends DSLInferencingTestCase {

    private static final String SET_DELEGATE_TYPE_SCRIPT =
        "public interface Obj {\n" +
        "    String getFoo();\n" +
        "    int FOO1 = 9;\n" +
        "    int FOO2 = 9;\n" +
        "    int OTHER = 9;\n" +
        "    int BAR = 9;\n" +
        "    int BAZ1 = 9;\n" +
        "    int BAZ2 = 9;\n" +
        "    int BAZ3 = 9;\n" +
        " }\n" +
        "\"\".l { delegate }\n" +
        "\"\".l { this }\n" +
        "\"\".l { getFoo() }\n" +
        "\"\".l { FOO1 }\n" +
        "\"\".l { delegate.FOO2 }\n" +
        "\"\".l { ''.OTHER }\n" +
        "\"\".l { delegate.l { BAR } }\n" +
        "\"\".l { 1.BAZ1 }\n" +
        "\"\".l { 1.l { BAZ2 } }\n" +
        "\"\".l { this.BAZ3 }\n" +
        "";
    private static final String SET_DELEGATE_TYPE_DSLD =
        "contribute(inClosure() & currentType(String)) {\n" +
        "  setDelegateType 'Obj'\n" +
        "}";

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(DSLInferencingTests.class);
    }

    public DSLInferencingTests(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createDSL();
    }

    private void createDSL() throws Exception {
        defaultFileExtension = "dsld";
        createUnit("SomeInterestingExamples", getTestResourceContents("SomeInterestingExamples.dsld"));
        defaultFileExtension = "groovy";
        env.fullBuild();
        expectingNoProblems();
    }

    //

    public void testRegisteredPointcut1() throws Exception {
        String contents = "2.phat";
        String name = "phat";

        assertDeclaringType(contents, contents.indexOf(name), contents.indexOf(name) + name.length(), "java.lang.Integer");
    }

    public void testRegisteredPointcut2() throws Exception {
        String contents = "2.valueInteger";
        String name = "valueInteger";

        assertDeclaringType(contents, contents.indexOf(name), contents.indexOf(name) + name.length(), "java.lang.Integer");
    }

    public void testContiribution1() throws Exception {
        createDsls("contribute(currentType('Foo')) { delegatesTo 'Other' }");
        String contents =
            "class Foo { }\n" +
            "class Other { Class<String> blar() { } }\n" +
            "new Foo().blar()";
        int start = contents.lastIndexOf("blar");
        int end = start + "blar".length();
        assertType(contents, start, end, "java.lang.Class<java.lang.String>", true);
        assertDeclaringType(contents, start, end, "Other", true);
    }

    public void testDelegatesTo1() throws Exception {
        createDsls("currentType('Foo').accept { delegatesTo 'Other' }");
        String contents =
            "class Foo { }\n" +
            "class Other { Class<String> blar() { } }\n" +
            "new Foo().blar()";
        int start = contents.lastIndexOf("blar");
        int end = start + "blar".length();
        assertType(contents, start, end, "java.lang.Class<java.lang.String>", true);
        assertDeclaringType(contents, start, end, "Other", true);
    }

    public void testDelegatesTo2() throws Exception {
        createDsls("currentType('Foo').accept { delegatesTo type:'Other' }");
        String contents =
            "class Foo { }\n" +
            "class Other { Class<String> blar() { } }\n" +
            "new Foo().blar()";
        int start = contents.lastIndexOf("blar");
        int end = start + "blar".length();
        assertType(contents, start, end, "java.lang.Class<java.lang.String>", true);
        assertDeclaringType(contents, start, end, "Other", true);
    }

    // this one is not working since we don't filter out contributions when static context is mismatched
    public void _testDelegatesTo3() throws Exception {
        createDsls("currentType('Foo').accept { delegatesTo type:'Other' }");
        String contents =
            "class Foo { }\n" +
            "class Other { Class<String> blar() { } }\n" +
            "Foo.blar()";
        int start = contents.lastIndexOf("blar");
        int end = start + "blar".length();
        // unknown confidence because accessing in static context
        assertUnknownConfidence(contents, start, end, "Other", true);
    }

    public void testDelegatesTo4() throws Exception {
        createDsls("currentType('Foo').accept { delegatesTo type:'Other', isStatic:true }");
        String contents =
            "class Foo { }\n" +
            "class Other { Class<String> blar() { } }\n" +
            "Foo.blar()";
        int start = contents.lastIndexOf("blar");
        int end = start + "blar".length();
        assertType(contents, start, end, "java.lang.Class<java.lang.String>", true);
        assertDeclaringType(contents, start, end, "Other", true);
    }

    public void testDelegatesTo5() throws Exception {
        createDsls("currentType('Foo').accept { delegatesTo type:'Other', asCategory:true }");
        String contents =
            "class Foo { }\n" +
            "class Other { Class<String> blar(Foo x) { }\n" +
            "Class<String> flar() { } }\n" +
            "new Foo().blar()\n" +
            "new Foo().flar()";
        int start = contents.lastIndexOf("blar");
        int end = start + "blar".length();
        assertType(contents, start, end, "java.lang.Class<java.lang.String>", true);
        assertDeclaringType(contents, start, end, "Other", true);

        start = contents.lastIndexOf("flar");
        end = start + "flar".length();
        assertUnknownConfidence(contents, start, end, "Foo", true);
    }

    public void testDelegatesTo6() throws Exception {
        createDsls("currentType('Foo').accept { delegatesTo type:'Other', except: ['glar']}");
        String contents =
            "class Foo {\n" +
            "  Class<String> glar() { }\n" +
            "}\n" +
            "class Other {\n" +
            "  Class<String> blar() { }\n" +
            "  Class<String> flar() { }\n" +
            "  Class<String> glar() { }\n" +
            "}\n" +
            "new Foo().flar()\n" +
            "new Foo().glar()";
        int start = contents.lastIndexOf("glar");
        int end = start + "glar".length();
        assertType(contents, start, end, "java.lang.Class<java.lang.String>", true);
        assertDeclaringType(contents, start, end, "Foo");

        start = contents.lastIndexOf("flar");
        end = start + "flar".length();
        assertType(contents, start, end, "java.lang.Class<java.lang.String>", true);
        assertDeclaringType(contents, start, end, "Other", true);
    }

    public void testGenerics1() throws Exception {
        createDsls("currentType('Foo').accept { property name: 'fooProp', type: 'List<Class<Foo>>' }");
        String contents =
            "class Foo {\n" +
            "}\n" +
            "new Foo().fooProp";
        int start = contents.lastIndexOf("fooProp");
        int end = start + "fooProp".length();
        assertType(contents, start, end, "java.util.List<java.lang.Class<Foo>>", true);
    }

    public void testGenerics2() throws Exception {
        createDsls("currentType('Foo').accept { property name: 'fooProp', type: 'List<Class<Foo>>' }");
        String contents =
            "class Foo {\n" +
            "}\n" +
            "def x = new Foo().fooProp[0]\n" +
            "x";
        int start = contents.lastIndexOf("x");
        int end = start + "x".length();
        assertType(contents, start, end, "java.lang.Class<Foo>", true);
    }

    public void testGenerics3() throws Exception {
        createDsls("currentType('Foo').accept { property name: 'fooProp', type: 'Map< Integer, Long>' }");
        String contents =
            "class Foo {\n" +
            "}\n" +
            "new Foo().fooProp";
        int start = contents.lastIndexOf("fooProp");
        int end = start + "fooProp".length();
        assertType(contents, start, end, "java.util.Map<java.lang.Integer,java.lang.Long>", true);
    }

    public void testDeprecated1() throws Exception {
        createDsls("currentType('Foo').accept { property name: 'fooProp', type: 'Map< Integer, Long>', isDeprecated:true }");
        String contents =
            "class Foo {\n" +
            "}\n" +
            "new Foo().fooProp";
        int start = contents.lastIndexOf("fooProp");
        int end = start + "fooProp".length();
        assertType(contents, start, end, "java.util.Map<java.lang.Integer,java.lang.Long>", true);
        assertDeprecated(contents, start, end);
    }

    public void testDeprecated2() throws Exception {
        createDsls("currentType('Foo').accept { method name: 'fooProp', type: 'Map< Integer, Long>', isDeprecated:true }");
        String contents =
            "class Foo {\n" +
            "}\n" +
            "new Foo().fooProp";
        int start = contents.lastIndexOf("fooProp");
        int end = start + "fooProp".length();
        assertType(contents, start, end, "java.util.Map<java.lang.Integer,java.lang.Long>", true);
        assertDeprecated(contents, start, end);
    }

    public void testAssertVersion1() throws Exception {
        createDsls("assertVersion(groovyEclipse:\"9.5.9\")\n" +
            "contribute(currentType('Foo')) { property name: 'fooProp', type: 'Map< Integer, Long>' }");
        String contents =
            "class Foo {\n" +
            "}\n" +
            "new Foo().fooProp";
        int start = contents.lastIndexOf("fooProp");
        int end = start + "fooProp".length();
        // script should not be executed
        assertUnknownConfidence(contents, start, end, "Foo", false);
    }

    public void testAssertVersion2() throws Exception {
        createDsls("assertVersion(groovyEclipse:\"1.5.9\")\n" +
            "contribute(currentType('Foo')) { property name: 'fooProp', type: 'Map< Integer, Long>' }");
        String contents =
            "class Foo {\n" +
            "}\n" +
            "new Foo().fooProp";
        int start = contents.lastIndexOf("fooProp");
        int end = start + "fooProp".length();
        // script should be executed
        assertType(contents, start, end, "java.util.Map<java.lang.Integer,java.lang.Long>", true);
    }

    public void testSupportsVersion3() throws Exception {
        createDsls("if (supportsVersion(groovyEclipse:\"9.5.9\"))\n" +
            "  contribute(currentType('Foo')) { property name: 'fooProp', type: 'Map< Integer, Long>' }");
        String contents =
            "class Foo {\n" +
            "}\n" +
            "new Foo().fooProp";
        int start = contents.lastIndexOf("fooProp");
        int end = start + "fooProp".length();
        // script should not be executed
        assertUnknownConfidence(contents, start, end, "Foo", false);
    }

    public void testSupportsVersion2() throws Exception {
        createDsls("if (supportsVersion(groovyEclipse:\"1.5.9\"))\n" +
                "  contribute(currentType('Foo')) { property name: 'fooProp', type: 'Map< Integer, Long>' }");
        String contents =
                "class Foo {\n" +
                        "}\n" +
                        "new Foo().fooProp";
        int start = contents.lastIndexOf("fooProp");
        int end = start + "fooProp".length();
        // script should be executed
        assertType(contents, start, end, "java.util.Map<java.lang.Integer,java.lang.Long>", true);
    }

    public void testIsThisType1() throws Exception {
        createDsls("contribute(isThisType()) { property name: 'thisType', type:Integer }");
        String contents =
            "class Foo { \n" +
            "def k() { \n" +
            "  thisType\n" +
            "  new Foo().thisType\n" +
            "  [].thisType }\n" +
            "def l = { \n" +
            "  thisType\n" +
            "  new Foo().thisType\n" +
            "  [].thisType }\n" +
            "}";

        int loc = 0;
        int len = "thisType".length();
        int num = 0;
        do {
            loc = contents.indexOf("thisType", loc +1);
            if (loc > 0) {
                if (num % 3 == 0) {
                    assertType(contents, loc, loc+len, "java.lang.Integer", true);
                } else if (num % 3 == 1) {
                    assertUnknownConfidence(contents, loc, loc+len, "Foo", true);
                } else if (num % 3 == 2) {
                    assertUnknownConfidence(contents, loc, loc+len, "java.util.List<E>", true);
                }
            }
            num++;
        } while (loc > 0);
    }

    public void testEnclosingCall1() throws Exception {
        createDsls("contribute(enclosingCall(name('foo')) & isThisType()) {  " +
            "property name: 'yes', type: Double } ");

        String contents = "foo( yes )";
        int start = contents.lastIndexOf("yes");
        int end = start + "yes".length();
        assertType(contents, start, end, "java.lang.Double", true);
    }

    public void testEnclosingCall2() throws Exception {
        createDsls("contribute(enclosingCall('foo') & isThisType()) {  " +
            "property name: 'yes', type: Double } ");

        String contents = "foo( yes )";
        int start = contents.lastIndexOf("yes");
        int end = start + "yes".length();
        assertType(contents, start, end, "java.lang.Double", true);
    }

    public void testEnclosingCall3() throws Exception {
        createDsls("contribute(enclosingCall(name('foo') & hasArgument(name('arg') & bind(value : value()))) & isThisType()) {  " +
            "value.each { property name: \"${it}Prop\", type: Double } }");

        String contents = "foo(arg:'yes', arg2:yesProp)";
        int start = contents.lastIndexOf("yesProp");
        int end = start + "yesProp".length();
        assertType(contents, start, end, "java.lang.Double", true);
    }

    public void testEnclosingCall4() throws Exception {
        createDsls("contribute(enclosingCall(name('foo') & hasArgument(value : name('arg'))) & isThisType()) {  " +
            "value.each { property name: \"${it}Prop\", type: Double } }");

        String contents = "foo(arg:argProp)";
        int start = contents.lastIndexOf("argProp");
        int end = start + "argProp".length();
        assertType(contents, start, end, "java.lang.Double", true);
    }

    public void testEnclosingCall5() throws Exception {
        createDsls("contribute(enclosingCall(name('foo') & hasArgument(bind(value : name('arg')) | bind(value : name('arg2')))) & isThisType()) {  " +
            "value.each { property name: \"${it}Prop\", type: Double } }");

        String contents = "foo(arg:argProp)";
        int start = contents.lastIndexOf("argProp");
        int end = start + "argProp".length();
        assertType(contents, start, end, "java.lang.Double", true);
    }

    public void testEnclosingCall6() throws Exception {
        createDsls("contribute(enclosingCall(name('foo') & hasArgument(bind(value : name('arg'))) & hasArgument(name('arg2'))) & isThisType()) {  " +
            "value.each { property name: \"${it}Prop\", type: Double } }");

        String contents = "foo(arg:argProp, arg2: nuthin)";
        int start = contents.lastIndexOf("argProp");
        int end = start + "argProp".length();
        assertType(contents, start, end, "java.lang.Double", true);
    }

    public void testEnclosingCall7() throws Exception {
        createDsls("contribute(enclosingCall(name('foo') & hasArgument(bind(value : value()) & name('arg'))) & isThisType()) {  " +
            "value.each { property name: \"${it}Prop\", type: Double } }");

        String contents = "foo(arg:'arg', arg2:argProp)";
        int start = contents.lastIndexOf("argProp");
        int end = start + "argProp".length();
        assertType(contents, start, end, "java.lang.Double", true);
    }

    public void testAnnotatedBy1() throws Exception {
        createDsls(
            "contribute(enclosingMethod(annotatedBy(\n" +
            "    name(\"MyAnno\") &   \n" +
            "    hasAttribute(\n" +
            "        name(\"name\") & \n" +
            "        bind(vals : value()))))) {\n" +
            "    vals.each { property name:it, type: Double }\n" +
            "}");
        String contents =
            "@interface MyAnno {\n" +
            "    String name() \n" +
            "}  \n" +
            "@MyAnno(name = \"name\")\n" +
            "def method() {\n" +
            "    name\n" +
            "}";
        int start = contents.lastIndexOf("name");
        int end = start + "name".length();
        assertType(contents, start, end, "java.lang.Double", true);
    }

    public void testAnnotatedBy2() throws Exception {
        createDsls(
            "contribute(enclosingMethod(annotatedBy(\n" +
            "    name(\"MyAnno\") &   \n" +
            "    hasAttribute(\n" +
            "        name(\"name\") & \n" +
            "        bind(names : value())) &\n" +
            "    hasAttribute(\n" +
            "        name(\"type\") & \n" +
            "        bind(types : value()))))) {\n" +
            "    property name : names.iterator().next(), type: types.iterator().next()\n" +
            "}");
        String contents =
            "@interface MyAnno {\n" +
            "    String name() \n" +
            "    Class type() \n" +
            "}\n" +
            "@MyAnno(name = \"name\", type = Double)\n" +
            "def method() {\n" +
            "    name  \n" +
            "}";
        int start = contents.lastIndexOf("name");
        int end = start + "name".length();
        assertType(contents, start, end, "java.lang.Double", true);
    }

    // GRECLIPSE-1190
    public void testHasArgument1() throws Exception {
        createDsls(
            "enclosingMethod(name(\"foo\") & declaringType(\"Flart\") & hasArgument(\"arg\")).accept {\n" +
            "  property name:\"arg\", type:\"Flart\"\n" +
            "}");

        String contents =
            "class Flart {\n" +
            "  def foo(arg) { arg } }";

        int start = contents.lastIndexOf("arg");
        int end = start + "arg".length();
        assertType(contents, start, end, "Flart", true);
    }

    // GRECLIPSE-1190
    public void testHasArgument2() throws Exception {
        createDsls(
            "enclosingMethod(name(\"foo\") & type(\"Flart\") & hasArgument(\"arg\")).accept {\n" +
                    "  property name:\"arg\", type:\"Flart\"\n" +
            "}");

        String contents =
            "class Flart { }\n" +
            "class Other {\n" +
            "  Flart foo(arg) { arg } }";

        int start = contents.lastIndexOf("arg");
        int end = start + "arg".length();
        assertType(contents, start, end, "Flart", true);
    }

    // GRECLIPSE-1261
    public void testStaticContext1() throws Exception {
        createDsls("contribute(currentType('Flart')) { method name: \"testme\", type: boolean }");
        String contents =
            "class Flart { }\n" +
            "static ahem() {\n" +
            "  new Flart().testme" +
            "}";
        int start = contents.lastIndexOf("testme");
        int end = start + "testme".length();
        assertType(contents, start, end, "java.lang.Boolean", true);
    }

    // GRECLIPSE-1261
    public void testStaticContext2() throws Exception {
        createDsls("contribute(currentType('Flart')) { method name: \"testme\", type: boolean }");
        String contents =
            "class Flart { }\n" +
            "static ahem() {\n" +
            "  Flart.testme" +
            "}";
        int start = contents.lastIndexOf("testme");
        int end = start + "testme".length();
        assertUnknownConfidence(contents, start, end, "Flart", true);
    }
    // GRECLIPSE-1261
    public void testStaticContext3() throws Exception {
        createDsls("contribute(currentType('Flart')) { method name: \"testme\", type: boolean, isStatic:true }");
        String contents =
            "class Flart {\n" +
            "static ahem() {\n" +
            "  testme" +
            "} }";
        int start = contents.lastIndexOf("testme");
        int end = start + "testme".length();
        assertType(contents, start, end, "java.lang.Boolean", true);
    }
    // GRECLIPSE-1261
    public void testStaticContext4() throws Exception {
        createDsls("contribute(currentType('Flart')) { method name: \"testme\", type: boolean }");
        String contents =
            "class Flart { \n" +
            "static ahem() {\n" +
            "  Flart.testme" +
            "} }";
        int start = contents.lastIndexOf("testme");
        int end = start + "testme".length();
        assertUnknownConfidence(contents, start, end, "Flart", true);
    }

    // GRECLIPSE-1261
    public void testStaticContext5() throws Exception {
        createDsls("contribute(currentType('Flart')) { method name: \"testme\", type: boolean, isStatic:true }");
        String contents =
            "class Flart { \n" +
            "static ahem() {\n" +
            "  new Flart().testme" +
            "} }";
        int start = contents.lastIndexOf("testme");
        int end = start + "testme".length();
        assertType(contents, start, end, "java.lang.Boolean", true);
    }

    // GRECLIPSE-1290
    public void testOperatorOverloading1() throws Exception {
        createDsls("contribute(currentType('Flart')) { method name: \"plus\", params: [a:Object], type: boolean }");
        String contents =
            "class Flart { }\n" +
            "def xxx = new Flart() + nuthin\n" +
            "xxx";
        int start = contents.lastIndexOf("xxx");
        int end = start + "xxx".length();
        assertType(contents, start, end, "java.lang.Boolean", true);
    }
    // GRECLIPSE-1290
    public void testOperatorOverloading2() throws Exception {
        createDsls("contribute(currentType('Flart')) { method name: \"getAt\", params: [a:Object], type: boolean }");
        String contents =
            "class Flart { }\n" +
            "def xxx = new Flart()[nuthin]\n" +
            "xxx";
        int start = contents.lastIndexOf("xxx");
        int end = start + "xxx".length();
        assertType(contents, start, end, "java.lang.Boolean", true);
    }

    // GRECLIPSE-1291
    public void testOperatorOverloading3() throws Exception {
        createDsls("contribute(currentType('Flart')) { method name: \"getAt\", params: [a:Object], type: boolean }");
        String contents =
            "class Flart { }\n" +
            "def xxx = new Flart()[nuthin]\n" +
            "xxx";
        int start = contents.lastIndexOf("xxx");
        int end = start + "xxx".length();
        assertType(contents, start, end, "java.lang.Boolean", true);
    }

    // GRECLIPSE-1291
    public void testOperatorOverloading4() throws Exception {
        createDsls("contribute(currentType('Flart')) { method name: \"positive\", type: boolean }");
        String contents =
            "class Flart { }\n" +
            "def xxx = +(new Flart())\n" +
            "xxx";
        int start = contents.lastIndexOf("xxx");
        int end = start + "xxx".length();
        assertType(contents, start, end, "java.lang.Boolean", true);
    }

    // GRECLIPSE-1291
    public void testOperatorOverloading5() throws Exception {
        createDsls("contribute(currentType('Flart')) { method name: \"negative\", type: boolean }");
        String contents =
            "class Flart { }\n" +
            "def xxx = -(new Flart())\n" +
            "xxx";
        int start = contents.lastIndexOf("xxx");
        int end = start + "xxx".length();
        assertType(contents, start, end, "java.lang.Boolean", true);
    }
    // GRECLIPSE-1291
    public void testOperatorOverloading6() throws Exception {
        createDsls("contribute(currentType('Flart')) { method name: \"bitwiseNegate\", type: boolean }");
        String contents =
            "class Flart { }\n" +
            "def xxx = ~(new Flart())\n" +
            "xxx";
        int start = contents.lastIndexOf("xxx");
        int end = start + "xxx".length();
        assertType(contents, start, end, "java.lang.Boolean", true);
    }

    // GRECLIPSE-1295
    public void testIsThisType2() throws Exception {
        createDsls("contribute(isThisType()) { property name:'hi', type:int }");
        String contents =
            "class Foo {\n" +
            "  def meth(Closure c) { }\n" +
            "}\n" +
            "new Foo().meth { hi }";
        int start = contents.lastIndexOf("hi");
        int end = start + "hi".length();
        assertType(contents, start, end, "java.lang.Integer", true);
    }

    // GRECLIPSE-1295
    public void testIsThisType3() throws Exception {
        createDsls("contribute(currentTypeIsEnclosingType()) { property name:'hi', type:int }");
        String contents =
            "class Foo {\n" +
            "  def meth(Closure c) { }\n" +
            "}\n" +
            "new Foo().meth { hi }";
        int start = contents.lastIndexOf("hi");
        int end = start + "hi".length();
        assertUnknownConfidence(contents, start, end, "Foo", true);
    }

    // GRECLIPSE-1301
    public void testEnclosingCallName1() throws Exception {
        createDsls(
            "contribute(~ enclosingCallName(\"foo\")) {\n" +
            "    property name:\"hi\"\n" +
            "}");
        String contents =
            "foo {\n" +
            "    bar {\n" +
            "        hi\n" +
            "    }\n" +
            "}";
        int start = contents.lastIndexOf("hi");
        int end = start + "hi".length();
        assertUnknownConfidence(contents, start, end, "Search", true);
    }

    // GRECLIPSE-1301
    public void testEnclosingCallName2() throws Exception {
        createDsls(
            "contribute(enclosingCall(~name('foo'))) {\n" +
            "    property name:'hi', type:int\n" +
            "}");

        String contents =
            "foo {\n" +
            "    bar {\n" +
            "        hi\n" +
            "    }\n" +
            "}";
        int start = contents.lastIndexOf("hi");
        int end = start + "hi".length();
        assertType(contents, start, end, "java.lang.Integer", true);
    }

    // GRECLIPSE-1321
    public void testDelegatesTo7() throws Exception {
        createDsls(
            "contribute(currentType(String)) {\n" +
            "  delegatesTo 'Obj'\n" +
            "}");
        String contents =
            "public interface Obj {\n" +
            "    String getFoo();\n" +
            "    int foo(arg);\n" +
            " }\n" +
            "\"\".getFoo()" +
            "\"\".foo()";
        int start = contents.lastIndexOf("foo");
        int end = start + "foo".length();
        assertType(contents, start, end, "java.lang.Integer", true);
        start = contents.lastIndexOf("getFoo");
        end = start + "getFoo".length();
        assertType(contents, start, end, "java.lang.String", true);
    }

    // GRECLIPSE-1442
    public void testDelegatesTo8() throws Exception {
        createDsls(
            "contribute(currentType(\"Delegatee\")) {\n" +
            "    delegatesTo type: \"MyCategory\", asCategory: true\n" +
            "}");
        String contents =
            "class MyCategory {\n" +
            "    static int getSomething(Delegatee d) { }\n" +
            "}\n" +
            "class Delegatee { }\n" +
            "new Delegatee().something \n" +
            "new Delegatee().getSomething()";
        int start = contents.lastIndexOf("getSomething");
        int end = start + "getSomething".length();
        assertType(contents, start, end, "java.lang.Integer", true);
        start = contents.lastIndexOf("something");
        end = start + "something".length();
        assertType(contents, start, end, "java.lang.Integer", true);
    }

    // GRECLIPSE-1442
    public void testDelegatesTo9() throws Exception {
        createDsls(
            "contribute(currentType(\"Delegatee\")) {\n" +
            "    delegatesTo type: \"MyCategory\", asCategory: true\n" +
            "}");
        String contents =
            "class MyCategory {\n" +
            "    static boolean isSomething(Delegatee d) { }\n" +
            "}\n" +
            "class Delegatee { }\n" +
            "new Delegatee().something \n" +
            "new Delegatee().isSomething()";
        int start = contents.lastIndexOf("isSomething");
        int end = start + "isSomething".length();
        assertType(contents, start, end, "java.lang.Boolean", true);
        start = contents.lastIndexOf("something");
        end = start + "something".length();
        assertType(contents, start, end, "java.lang.Boolean", true);
    }

    public void testSetDelegateType1() throws Exception {
        createDsls(SET_DELEGATE_TYPE_DSLD);
        String contents = SET_DELEGATE_TYPE_SCRIPT;
        int start = contents.lastIndexOf("delegate");
        int end = start + "delegate".length();
        assertType(contents, start, end, "Obj", true);
    }

    public void testSetDelegateType1a() throws Exception {
        createDsls(SET_DELEGATE_TYPE_DSLD);
        String contents = SET_DELEGATE_TYPE_SCRIPT;
        int start = contents.lastIndexOf("this");
        int end = start + "this".length();
        assertType(contents, start, end, "Search", true);
    }

    public void testSetDelegateType2() throws Exception {
        createDsls(SET_DELEGATE_TYPE_DSLD);
        String contents = SET_DELEGATE_TYPE_SCRIPT;
        int start = contents.lastIndexOf("getFoo");
        int end = start + "getFoo".length();
        assertType(contents, start, end, "java.lang.String", true);
    }

    public void testSetDelegateType3() throws Exception {
        createDsls(SET_DELEGATE_TYPE_DSLD);
        String contents = SET_DELEGATE_TYPE_SCRIPT;
        int start = contents.lastIndexOf("FOO1");
        int end = start + "FOO1".length();
        assertType(contents, start, end, "java.lang.Integer", true);
    }

    public void testSetDelegateType4() throws Exception {
        createDsls(SET_DELEGATE_TYPE_DSLD);
        String contents = SET_DELEGATE_TYPE_SCRIPT;
        int start = contents.lastIndexOf("FOO2");
        int end = start + "FOO2".length();
        assertType(contents, start, end, "java.lang.Integer", true);
    }

    public void testSetDelegateType5() throws Exception {
        createDsls(SET_DELEGATE_TYPE_DSLD);
        String contents = SET_DELEGATE_TYPE_SCRIPT;
        int start = contents.lastIndexOf("OTHER");
        int end = start + "OTHER".length();
        assertUnknownConfidence(contents, start, end, "java.lang.String", true);
    }

    public void testSetDelegateType6() throws Exception {
        createDsls(SET_DELEGATE_TYPE_DSLD);
        String contents = SET_DELEGATE_TYPE_SCRIPT;
        int start = contents.lastIndexOf("BAR");
        int end = start + "BAR".length();
        assertType(contents, start, end, "java.lang.Integer", true);
    }

    public void testSetDelegateType7() throws Exception {
        createDsls(SET_DELEGATE_TYPE_DSLD);
        String contents = SET_DELEGATE_TYPE_SCRIPT;
        int start = contents.lastIndexOf("BAZ1");
        int end = start + "BAZ1".length();
        assertUnknownConfidence(contents, start, end, "java.lang.Integer", true);
    }

    public void testSetDelegateType8() throws Exception {
        createDsls(SET_DELEGATE_TYPE_DSLD);
        String contents = SET_DELEGATE_TYPE_SCRIPT;
        int start = contents.lastIndexOf("BAZ2");
        int end = start + "BAZ2".length();
        assertUnknownConfidence(contents, start, end, "java.lang.Integer", true);
    }

    public void testSetDelegateType9() throws Exception {
        createDsls(SET_DELEGATE_TYPE_DSLD);
        String contents = SET_DELEGATE_TYPE_SCRIPT;
        int start = contents.lastIndexOf("BAZ3");
        int end = start + "BAZ3".length();
        assertUnknownConfidence(contents, start, end, "Search", true);
    }

    // GRECLIPSE-1458
    public void testMultiProject() throws Exception {
        IPath otherPath = env.addProject("Other", "1.5");
        env.removePackageFragmentRoot(otherPath, "");
        IPath root = env.addPackageFragmentRoot(otherPath, "src", null, null, "bin");
        env.addFile(env.addFolder(root, "dsld"), "otherdsld.dsld", "contribute(currentType(String)) { property name: 'other', type: Integer }");
        env.fullBuild("Other");
        env.addRequiredProject(project.getFullPath(), otherPath);

        GroovyDSLCoreActivator.getDefault().getContextStoreManager().initialize(project, true);

        String contents = "''.other";
        int start = contents.lastIndexOf("other");
        int end = start + "other".length();

        assertType(contents, start, end, "java.lang.Integer", true);
    }

    // GRECLIPSE-1459
    public void testNullType() throws Exception {
        createDsls("contribute(enclosingCall(hasArgument(type()))) {\n" +
                "    property name:'foo', type:Integer\n" +
                "}");
        String contents = "String flart(val, closure) { }\n" +
                "\n" +
                "flart '', {\n" +
                "    foo\n" +
                "}";
        int start = contents.lastIndexOf("fo");
        int end = start + "foo".length();
        assertType(contents, start, end, "java.lang.Integer");
    }

    private final static String ARRAY_TYPE_DSLD =
        "contribute(currentType()) {\n" +
        "    property name:'foot1', type:'java.lang.String[]'\n" +
        "    property name:'foot2', type:'java.lang.String[][]'\n" +
        "    property name:'foot3', type:'java.util.List<java.lang.String[][]>'\n" +
        "    property name:'foot4', type:'java.util.List<java.lang.String>[]'\n" +
        "    property name:'foot5', type:'java.util.List<java.lang.String[]>[]'\n" +
        "    property name:'foot6', type:'java.util.Map<java.lang.String[],java.lang.Integer[]>'\n" +
        "}";

    // GRECLIPSE-1555
    public void testArrayType1() throws Exception {
        createDsls(ARRAY_TYPE_DSLD);
        String contents = "foot1";
        assertType(contents, "java.lang.String[]", true);
    }

    public void testArrayType2() throws Exception {
        createDsls(ARRAY_TYPE_DSLD);
        String contents = "foot2";
        assertType(contents, "java.lang.String[][]", true);
    }

    public void testArrayType3() throws Exception {
        createDsls(ARRAY_TYPE_DSLD);
        String contents = "foot3";
        assertType(contents, "java.util.List<java.lang.String[][]>", true);
    }

    // TODO expected to fail
    public void _testArrayType4() throws Exception {
        createDsls(ARRAY_TYPE_DSLD);
        String contents = "foot4";
        assertType(contents, "java.util.List<java.lang.String>[]", true);
    }

    // TODO expected to fail
    public void _testArrayType5() throws Exception {
        createDsls(ARRAY_TYPE_DSLD);
        String contents = "foot5";
        assertType(contents, "java.util.List<java.lang.String[]>[]", true);
    }

    public void testArrayType6() throws Exception {
        createDsls(ARRAY_TYPE_DSLD);
        String contents = "foot6";
        assertType(contents, "java.util.Map<java.lang.String[],java.lang.Integer[]>", true);
    }

    public void testNestedCalls() throws Exception {
        createDsls(
            "contribute(bind( x: enclosingCall())) {\n" +
            "	x.each { \n" +
            "		property name: it.methodAsString + \"XXX\", type: Long\n" +
            "	}\n" +
            "}");

        String contents =
            "bar {\n" +
            "	foo {\n" +
            "		 fooXXX\n" +
            "		 barXXX      \n" +
            "	}\n" +
            "}";

        int start = contents.indexOf("fooXXX");
        int end = start + "fooXXX".length();
        assertType(contents, start, end, "java.lang.Long");

        start = contents.indexOf("barXXX");
        end = start + "barXXX".length();
        assertType(contents, start, end, "java.lang.Long");
    }
}
