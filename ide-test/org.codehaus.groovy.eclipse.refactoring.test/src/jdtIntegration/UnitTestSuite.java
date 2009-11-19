/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package jdtIntegration;

import jdtIntegration.renameClass.RenameClass_EclipseTestSuite;
import jdtIntegration.renameField.RenameField_EclipseTestSuite;
import jdtIntegration.renameMethod.RenameMethod_EclipseTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Stefan Sidler
 */
public class UnitTestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for jdtIntegration");

		suite.addTest(RenameClass_EclipseTestSuite.suite());
		suite.addTest(RenameMethod_EclipseTestSuite.suite());
		suite.addTest(RenameField_EclipseTestSuite.suite());
		return suite;
	}

}
