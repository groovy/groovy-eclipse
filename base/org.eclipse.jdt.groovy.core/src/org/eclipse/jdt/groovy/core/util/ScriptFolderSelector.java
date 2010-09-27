package org.eclipse.jdt.groovy.core.util;

import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.Activator;

public class ScriptFolderSelector {

	private final static char[][] NO_PATTERNS = new char[0][];
	private final char[][] scriptPatterns;

	public ScriptFolderSelector() {
		super();
		this.scriptPatterns = toCharChar(Activator.getDefault().getListStringPreference(Activator.GROOVY_SCRIPT_FILTER,
				Activator.DEFAULT_GROOVY_SCRIPT_FILTER));
	}

	private char[][] toCharChar(List<String> listStringPreference) {
		if (listStringPreference == null) {
			return NO_PATTERNS;
		}
		char[][] arr = new char[listStringPreference.size()][];
		int index = 0;
		for (String patternStr : listStringPreference) {
			arr[index++] = patternStr.toCharArray();
		}
		return arr;
	}

	/**
	 * Determines if the file name passed in should be treated as a script. Uses workspace script patterns to determine if the file
	 * name is a script or not
	 * 
	 * @param fileName project-relative file name to a groovy or groovy-like file
	 * @return true if file name matches the patterns or false otherwise
	 */
	public boolean isScript(char[] filepath) {
		for (char[] pattern : scriptPatterns) {
			if (CharOperation.pathMatch(pattern, filepath, true, '/')) {
				return true;
			}
		}
		return false;
	}

}
