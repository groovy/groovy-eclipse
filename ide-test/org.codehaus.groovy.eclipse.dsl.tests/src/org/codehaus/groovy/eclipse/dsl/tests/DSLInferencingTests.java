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
    
    public void testSingleton() throws Exception {
        
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
