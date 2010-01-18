/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package jdtIntegration;

import org.codehaus.groovy.eclipse.test.TestProject;
import org.eclipse.core.runtime.CoreException;

import jdtIntegration.renameClass.RenameClass_EclipseTestSuite;
import jdtIntegration.renameField.RenameField_EclipseTestSuite;
import jdtIntegration.renameMethod.RenameMethod_EclipseTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Stefan Sidler
 */
public class JointRenameRefactoringTestSuite {

    public static TestProject testProject;
    
    
	public static Test suite() {
	    
	    
		TestSuite suite = new TestSuite(JointRenameRefactoringTestSuite.class.getCanonicalName());

		suite.addTest(RenameMethod_EclipseTestSuite.suite());
		suite.addTest(RenameClass_EclipseTestSuite.suite());
		suite.addTest(RenameField_EclipseTestSuite.suite());
		return suite;
	}


    public static TestProject getTestProject() throws CoreException {
        if (testProject == null) {
            testProject = new TestProject();
        }
        return testProject;
    }

    public static void cleanTestProject() throws CoreException {
        testProject.deleteContents();
    }
}
