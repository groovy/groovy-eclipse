/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package core;

import static core.RefactoringTestActivator.getPathOfPlugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

/**
 * class to simplify the access to certain ressources during testexecution
 * @author reto kleeb
 *
 */
public class FilePathHelper {
	
	/*
	 * If this ENV var is set to true on a system, the class returns paths that will work
	 * if the tests run as eclipse plugin unittests ("buildserver").
	 * 
	 * If the ENV var is not set the class will return paths that make sense
	 * for local, common junit tests
	 */
	public final static String ENV_VAR = "RUN_PLUGIN_UNIT_TESTS"; 
	
	public static String getPathToCoreJar() {
		final String jarFileNameServer = "groovy-all-1.6.3";
		final String jarFileNameLocal = "groovy-all-1.6.3.jar";
		if (isLocalUnitTest()) {
			return "D:\\Builds\\Build-Result\\automatedTests\\eclipse\\plugins\\org.codehaus.groovy_1.6.3.200809031117NGT" + 
			jarFileNameLocal;
		}
        return getPathOfPlugin("org.codehaus.groovy") + jarFileNameServer;
	}
	
	
    protected static String getPluginDirectoryPath() {
        try {
            URL platformURL = Platform.getBundle("org.codehaus.groovy.eclipse.refactoring.test").getEntry("/"); //$NON-NLS-1$ //$NON-NLS-2$
            return new File(FileLocator.toFileURL(platformURL).getFile()).getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

	
	public static String getPathToTestFiles() {
		final String systemSeparator = String.valueOf(IPath.SEPARATOR);
		final String folders = "/src/tests/TestCodeFiles/";
		folders.replaceAll("/", systemSeparator);
		
		return getPluginDirectoryPath() + folders;
		
//		if(isLocalUnitTest()){ 
//			return folders;
//		}
//        return getPathOfPlugin("org.codehaus.groovy.eclipse.refactoring.test")+folders;
	}
	
	private static boolean isLocalUnitTest() {
		final String readEnvVal = System.getProperty(ENV_VAR);
		if(readEnvVal != null){
			if(readEnvVal.equals("true")){
				return false;
			}
		}
		return true;
	}

}
