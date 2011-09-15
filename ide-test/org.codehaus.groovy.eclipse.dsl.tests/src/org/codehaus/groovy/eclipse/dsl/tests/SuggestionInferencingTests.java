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

import org.eclipse.core.runtime.Path;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests type inferencing that involve the inferencing 
 * 
 * @author Andrew Eisenberg
 * @created Sep 15, 2011
 */
public class SuggestionInferencingTests extends AbstractDSLInferencingTest {
    public SuggestionInferencingTests(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(SuggestionInferencingTests.class);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    
    public void testDoNothing() throws Exception {
       // this test is a stub
    }
    
    
    
    private void doTestForType(String xdslContents, String groovyContents, int exprStart, int exprEnd, String expectedType) throws IOException {
        createXDSL(xdslContents);
        assertType(groovyContents, exprStart, exprEnd, expectedType, true);
    }
    
    private void doTestForDeclaringType(String xdslContents, String groovyContents, int exprStart, int exprEnd, String expectedDeclaringType) throws IOException {
        createXDSL(xdslContents);
        assertDeclaringType(groovyContents, exprStart, exprEnd, expectedDeclaringType, true);
    }
    
    private void createXDSL(String contents) throws IOException {
        defaultFileExtension = "xdsl";
        env.addFile(new Path("Project/.groovy"), "suggestions.xdsl", contents);
    }

}
