/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software Inc.
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Kris De Volder - initial API and implementation
 *******************************************************************************/

package org.codehaus.groovy.alltests;

import org.eclipse.jdt.groovy.core.tests.basic.GroovySimpleTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * A subset of the tests that we want to run with older groovy compilers.
 * 
 * @author Kris De Volder
 */
public class SmokeTests {
    public static Test suite() throws Exception {
        // ensure that the compiler chooser starts up
    	GroovyTestSuiteSupport.initializeCompilerChooser();
    	
        TestSuite suite = new TestSuite(SmokeTests.class.getName()); //$NON-NLS-1$
        suite.addTest(GroovySimpleTest.suite());
        //Can add more tests here.
        
        return suite;
    }
}
