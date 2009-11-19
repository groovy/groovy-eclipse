/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package jdtIntegration.renameMethod;

import java.io.File;
import java.util.List;

import junit.framework.TestSuite;
import tests.BaseTestSuite;

/**
 * @author Stefan Sidler
 * 
 */
public class RenameMethod_EclipseTestSuite extends BaseTestSuite {

	public static TestSuite suite() {
		
		TestSuite ts = new TestSuite("Rename Method Suite");
		
		List<File> files;

		files = getFileList("/jdtIntegration/startedFromGroovy/renameMethod","RenameJavaMethod_Test_");
		
		for (File file : files) {
			ts.addTest(new RenameMethod_EclipseTestCase("started from Groovy: "+file.getName(), file));
		}
		
		files = getFileList("/jdtIntegration/startedFromGroovy/renameMethod","RenameGroovyMethod_Test_");
		
		for (File file: files) {
			ts.addTest(new RenameMethod_EclipseTestCase("started from Groovy: "+file.getName(), file));
		}
		
		files = getFileList("/jdtIntegration/startedFromJava/renameMethod","RenameJavaMethod_Test_");
		
		for (File file : files) {
			ts.addTest(new RenameMethod_EclipseTestCase("started from Java: "+file.getName(), file));
		}
		
		files = getFileList("/jdtIntegration/startedFromJava/renameMethod","RenameGroovyMethod_Test_");
		
		for (File file : files) {
			ts.addTest(new RenameMethod_EclipseTestCase("started from Java: "+file.getName(), file));
		}

		return ts;
	}
}
