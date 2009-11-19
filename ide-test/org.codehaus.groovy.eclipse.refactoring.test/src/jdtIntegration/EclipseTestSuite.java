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
import junit.framework.TestSuite;
import tests.BaseTestSuite;

/**
 * @author Stefan Sidler
 * 
 */
public class EclipseTestSuite extends BaseTestSuite {

	public static TestSuite suite() {
		TestSuite suite = new TestSuite("Run of all Cross Language Refactoring Test");
        suite.addTest(RenameClass_EclipseTestSuite.suite());
        suite.addTest(RenameField_EclipseTestSuite.suite());
        suite.addTest(RenameMethod_EclipseTestSuite.suite());

        return suite;
	}
}
