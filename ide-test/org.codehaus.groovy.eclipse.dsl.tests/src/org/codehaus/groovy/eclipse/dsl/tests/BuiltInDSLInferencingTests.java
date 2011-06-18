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

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.eclipse.jdt.core.JavaCore;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests type inferencing for DSL scripts included with Groovy plugin
 * 
 * @author Andrew Eisenberg
 * @created Jun 16, 2011
 */
public class BuiltInDSLInferencingTests extends AbstractDSLInferencingTest {
    public BuiltInDSLInferencingTests(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(BuiltInDSLInferencingTests.class);
    }
    
    @Override
    protected void setUp() throws Exception {
        doRemoveClasspathContainer = false;
        super.setUp();
    }
    
    public void testSingleton() throws Exception {
        String contents = "@Singleton class Foo { }\nFoo.instance\nFoo.getInstance()";
        int start = contents.lastIndexOf("instance");
        int end = start + "instance".length();
        assertType(contents, start, end, "Foo", "Singleton", true);
        
        start = contents.lastIndexOf("getInstance");
        end = start + "getInstance".length();
        assertType(contents, start, end, "Foo", "Singleton", true);
    }
    // still very
    public void _testDelegate1() throws Exception {
        String contents = "class Foo { @Delegate List<Integer> myList }\nnew Foo().get(0)";
        int start = contents.lastIndexOf("get");
        int end = start + "get".length();
        assertType(contents, start, end, "java.lang.Integer", "Delegate", true);
        assertDeclaringType(contents, start, end, "java.util.List<java.lang.Integer>", true);
    }
    
}
