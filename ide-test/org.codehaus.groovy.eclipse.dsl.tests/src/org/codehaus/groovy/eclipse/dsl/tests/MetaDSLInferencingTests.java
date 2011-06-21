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

import org.eclipse.jdt.core.tests.util.GroovyUtils;

/**
 * Tests type inferencing that involve dsls
 * 
 * @author Andrew Eisenberg
 * @created Feb 18, 2011
 */
public class MetaDSLInferencingTests extends AbstractDSLInferencingTest {
    public MetaDSLInferencingTests(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(MetaDSLInferencingTests.class);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createMetaDSL();
    }
    
    public void testSimpleDSL() throws Exception {
        assertType("foo", "java.lang.Object", true);
        createDsls("currentType().accept { property ( name: \"foo\", type: Date ) }");
        assertType("foo", "java.util.Date", true);
        deleteDslFile(0);
        assertType("foo", "java.lang.Object", true);
    }
    
    public void testMetaDSL1() throws Exception {
        assertType("currentType", "p.IPointcut", true);
    }
    public void testMetaDSL2() throws Exception {
        assertType("registerPointcut", "java.lang.Void", true);
    }
    public void testMetaDSL3() throws Exception {
        assertType("supportsVersion", "java.lang.Void", true);
    }


    public void testMetaDSL4() throws Exception {
        String contents = 
            "currentType().accept {\n" +
            " method\n" +
            // ignore since conflicts with Script.getProperty
//            " property\n" +
            " wormhole\n" +
            " delegatesTo\n" +
            " delegatesToUseNamedArgs\n" +
            "}";
        assertDSLType(contents, "method");
//        assertDSLType(contents, "property");
        assertDSLType(contents, "wormhole");
        assertDSLType(contents, "delegatesTo");
        assertDSLType(contents, "delegatesToUseNamedArgs");
    }

    public void testMetaDSL5() throws Exception {
        String contents = 
            " method\n" +
            // ignore since conflicts with Script.getProperty
//            " property\n" +
            " wormhole\n" +
            " delegatesTo\n" +
            " delegatesToUseNamedArgs\n";
        assertUnknownDSLType(contents, "method");
//        assertUnknownDSLType(contents, "property");
        assertUnknownDSLType(contents, "wormhole");
        assertUnknownDSLType(contents, "delegatesTo");
        assertUnknownDSLType(contents, "delegatesToUseNamedArgs");
    }

    public void testMetaDSL6() throws Exception {
        defaultFileExtension = "groovy";
        String contents = 
            "currentType().accept {\n" +
            " method\n" +
            // ignore since conflicts with Script.getProperty
//            " property\n" +
            " wormhole\n" +
            " delegatesTo\n" +
            " delegatesToUseNamedArgs\n" +
            "}";
        assertUnknownDSLType(contents, "method");
//        assertUnknownDSLType(contents, "property");
        assertUnknownDSLType(contents, "wormhole");
        assertUnknownDSLType(contents, "delegatesTo");
        assertUnknownDSLType(contents, "delegatesToUseNamedArgs");
    }
    
    public void testMetaDSL7() throws Exception {
        defaultFileExtension = "groovy";
        String contents = 
            "currentType().accept {\n" +
            "}";
        assertUnknownDSLType(contents, "currentType");
        assertUnknownDSLType(contents, "accept");
    }
    
    public void testBindings() throws Exception {
        String contents = 
            "bind( b : currentType()).accept {\n" +
            "b\n" +
            "}";
        String name = "b";
        assertDeclaringType(contents, contents.lastIndexOf(name), contents.lastIndexOf(name) + name.length(), "p.IPointcut", true);
    }
    
    /**
     * @throws IOException
     */
    private void createMetaDSL() throws IOException {
        // Closure uses a type parameter in Groovy 1.8, but not in 17
        if (GroovyUtils.GROOVY_LEVEL > 17) {
            createJavaUnit("p", "IPointcut", "package p;\npublic interface IPointcut { \n Object accept(groovy.lang.Closure<?> c);\n }");
        } else {
            createJavaUnit("p", "IPointcut", "package p;\npublic interface IPointcut { \n Object accept(groovy.lang.Closure c);\n }");
        }
        defaultFileExtension = "dsld";
        createUnit("DSLD_meta_script", GroovyDSLDTestsActivator.getDefault().getTestResourceContents("DSLD_meta_script.dsld"));
        env.fullBuild();
        expectingNoProblems();
    }

}
