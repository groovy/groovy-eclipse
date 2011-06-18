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
    
    
    /**
     * @throws IOException
     */
    private void createDSL() throws IOException {
        defaultFileExtension = "dsld";
        createUnit("SomeInterestingExamples", GroovyDSLDTestsActivator.getDefault().getTestResourceContents("SomeInterestingExamples.dsld"));
        defaultFileExtension = "groovy";
        env.fullBuild();
        expectingNoProblems();
    }

}
