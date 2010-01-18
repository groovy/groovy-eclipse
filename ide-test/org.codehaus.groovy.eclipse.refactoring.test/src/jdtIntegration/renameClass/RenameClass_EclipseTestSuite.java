/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package jdtIntegration.renameClass;

import java.io.File;
import java.util.List;

import junit.framework.TestSuite;
import tests.BaseTestSuite;

/**
 * @author Stefan Sidler
 * 
 */
public class RenameClass_EclipseTestSuite extends BaseTestSuite {

    public static TestSuite suite() {

        TestSuite ts = new TestSuite(RenameClass_EclipseTestSuite.class.getCanonicalName());

        List<File> files;

        files = getFileList("/jdtIntegration/startedFromJava/renameClass","RenameJavaClass_Test_");

        for (File file : files) {
//            if (file.getName().contains("ExtendsClass"))
//                ts.addTest(new RenameClass_EclipseTestCase("started from Java: "+file.getName(), file));
//            if (file.getName().contains("Test_Expression"))
                ts.addTest(new RenameClass_EclipseTestCase("started from Java: "+file.getName(), file));
        }

        files = getFileList("/jdtIntegration/startedFromJava/renameClass","RenameGroovyClass_Test_");

        for (File file : files) {
//            if (file.getName().contains("ExtendsClass"))
//                ts.addTest(new RenameClass_EclipseTestCase("started from Java: "+file.getName(), file));
//            if (file.getName().contains("Test_Expression"))
                ts.addTest(new RenameClass_EclipseTestCase("started from Java: "+file.getName(), file));
        }

        files = getFileList("/jdtIntegration/startedFromGroovy/renameClass","RenameJavaClass_Test_");

        for (File file : files) {
//            if (file.getName().contains("ExtendsClass"))
//                ts.addTest(new RenameClass_EclipseTestCase("started from Java: "+file.getName(), file));
//            if (file.getName().contains("Test_Expression"))
                ts.addTest(new RenameClass_EclipseTestCase("started from Java: "+file.getName(), file));
        }

        files = getFileList("/jdtIntegration/startedFromGroovy/renameClass","RenameGroovyClass_Test_");

        for (File file : files) {
//            if (file.getName().contains("ExtendsClass"))
//                ts.addTest(new RenameClass_EclipseTestCase("started from Java: "+file.getName(), file));
//            if (file.getName().contains("Test_Expression"))
                ts.addTest(new RenameClass_EclipseTestCase("started from Java: "+file.getName(), file));
        } 

        return ts;
    }
}
