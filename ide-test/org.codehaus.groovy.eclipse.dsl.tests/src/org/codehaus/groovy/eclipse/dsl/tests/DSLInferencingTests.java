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
    
    public void testSimpleDSL() throws Exception {
        assertType("foo", "java.lang.Object", true);
        createDsls("currentType().accept { property ( name: \"foo\", type: Date ) }");
        assertType("foo", "java.util.Date", true);
        deleteDslFile(0);
        assertType("foo", "java.lang.Object", true);
    }

}
