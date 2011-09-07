/*******************************************************************************
 * Copyright (c) 2011 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Andrew Eisenberg - Initial implemenation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.dsl.tests;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests type inferencing that involve dsls
 * 
 * @author Andrew Eisenberg
 * @created Feb 18, 2011
 */
public class DSLInferencingTests extends AbstractDSLInferencingTest {
    public DSLInferencingTests(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(DSLInferencingTests.class);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createDSL();
    }
    
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
        //        assertType(contents, start, end, "java.util.Map<java.lang.Integer,java.lang.Long>", true);
                        
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
                if (num %3 == 2) {
                    assertUnknownConfidence(contents, loc, loc+len, "java.util.List<E>", true);
                } else {
                    assertType(contents, loc, loc+len, "java.lang.Integer", true);
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
        createDsls("contribute(enclosingMethod(annotatedBy(\n" + 
        		"    name(\"MyAnno\") &   \n" + 
        		"    hasAttribute(\n" + 
        		"        name(\"name\") & \n" + 
        		"        bind(vals : value()))))) {\n" + 
        		"    vals.each { property name:it, type: Double }\n" + 
        		"}");
        String contents = "@interface MyAnno {\n" + 
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
        createDsls("contribute(enclosingMethod(annotatedBy(\n" + 
        		"    name(\"MyAnno\") &   \n" + 
        		"    hasAttribute(\n" + 
        		"        name(\"name\") & \n" + 
        		"        bind(names : value())) &\n" + 
        		"    hasAttribute(\n" + 
        		"        name(\"type\") & \n" + 
        		"        bind(types : value()))))) {\n" + 
        		"    property name : names.iterator().next(), type: types.iterator().next()\n" + 
        		"}");
        String contents = "@interface MyAnno {\n" + 
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
        
        String contents = "class Flart {\n" +
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
        
        String contents = "class Flart { }\n" +
                "class Other {\n" +
                "  Flart foo(arg) { arg } }";
        
        int start = contents.lastIndexOf("arg");
        int end = start + "arg".length();
        assertType(contents, start, end, "Flart", true);
    }
    
    private void createDSL() throws IOException {
        defaultFileExtension = "dsld";
        createUnit("SomeInterestingExamples", GroovyDSLDTestsActivator.getDefault().getTestResourceContents("SomeInterestingExamples.dsld"));
        defaultFileExtension = "groovy";
        env.fullBuild();
        expectingNoProblems();
    }

}
