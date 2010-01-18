/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package jdtIntegration.renameField;

import java.io.File;
import java.util.List;

import junit.framework.TestSuite;
import tests.BaseTestSuite;

/**
 * @author Stefan Sidler
 * 
 */
public class RenameField_EclipseTestSuite extends BaseTestSuite {

	public static TestSuite suite() {
		
		TestSuite ts = new TestSuite(RenameField_EclipseTestSuite.class.getCanonicalName());
		
		List<File> files;

		files = getFileList("/jdtIntegration/startedFromGroovy/renameField","" + "RenameJavaField_Test_");
		
		for (File file : files) {
//            if (file.getName().endsWith("RenameJavaField_Test_Typed_SamePackage.txt"))
			ts.addTest(new RenameField_EclipseTestCase("started from Groovy: "+file.getName(), file));
		}
		
		files = getFileList("/jdtIntegration/startedFromGroovy/renameField","RenameGroovyField_Test_");
		
		for (File file : files) {
//            if (file.getName().endsWith("RenameJavaField_Test_Typed_SamePackage.txt"))
			ts.addTest(new RenameField_EclipseTestCase("started from Groovy: "+file.getName(), file));
		} 
		
		files = getFileList("/jdtIntegration/startedFromJava/renameField","RenameJavaField_Test_");
		
		for (File file : files) {	
//            if (file.getName().endsWith("RenameJavaField_Test_Typed_SamePackage.txt"))
			ts.addTest(new RenameField_EclipseTestCase("started from Java: "+file.getName(), file));
		}
		
		files = getFileList("/jdtIntegration/startedFromJava/renameField","RenameGroovyField_Test_");
		
		for (File file : files) {	
//            if (file.getName().endsWith("RenameJavaField_Test_Typed_SamePackage.txt"))
			ts.addTest(new RenameField_EclipseTestCase("started from Java: "+file.getName(), file));
		}
		
		return ts;
	}
}
