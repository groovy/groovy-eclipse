package org.codehaus.groovy.alltests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.groovy.core.tests.basic.GroovySimpleTest;

/**
 * Tycho refuses to run tests directly from another bundle, so we will just have to
 * create a class here that runs them.
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
