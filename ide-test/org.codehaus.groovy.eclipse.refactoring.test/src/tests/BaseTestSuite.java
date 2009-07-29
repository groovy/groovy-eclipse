/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package tests;

import core.FilePathHelper;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestSuite;

public class BaseTestSuite extends TestSuite {
	
	private static class PatternFilter implements FilenameFilter {
		
		public PatternFilter(final String findPattern) {
			this.findPattern = "^" + findPattern + ".*";
		}
		
		private String findPattern;

		public boolean accept(final File dir, final String name) {
			return name.matches(findPattern);
		}	
	}
	
	public static List<File> getFileList(final String search) {
		return createFileList(search, "");
	}
	
	public static List<File> getFileList(final String subFolder,final String search) {
		return createFileList(search, subFolder);
	}
	
	private static List<File> createFileList(final String search, final String subFolder){
		final String TEST_FILES = FilePathHelper.getPathToTestFiles();
		final File dir = new File(TEST_FILES + subFolder);
		if(!dir.isDirectory()) {
			throw new RuntimeException("The path: " + dir.getAbsolutePath() + " is invalid");
		}
		final ArrayList<File> fl = new ArrayList<File>();
		
		for (final File f : dir.listFiles(new PatternFilter(search))) {
			fl.add(f);
		}
		
		return fl;
	} 
	
}
