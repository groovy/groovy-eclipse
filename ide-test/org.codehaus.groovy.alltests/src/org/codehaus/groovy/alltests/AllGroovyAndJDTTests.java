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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Kris De Volder
 *
 * Groovy plugin tests
 */
public class AllGroovyAndJDTTests {
    public static Test suite() throws Exception {
        // ensure that the compiler chooser starts up
    	GroovyTestSuiteSupport.initializeCompilerChooser();
    	
        TestSuite suite = new TestSuite("All Groovy and Groovy JDT Tests"); //$NON-NLS-1$
        
        suite.addTest(AllGroovyTests.suite()); //This must be first because of test that check startup log contents!
        suite.addTest(GroovyJDTTests.suite());
        
        return suite;
    }
}
