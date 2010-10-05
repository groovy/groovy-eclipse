package org.eclipse.jdt.groovy.core.util;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.Activator;

public class ScriptFolderSelector {

	public static enum FileKind {
		SOURCE, SCRIPT, SCRIPT_NO_COPY
	}

	private char[][] scriptPatterns;
	private boolean[] doCopy;
	private final boolean disabled;

	public ScriptFolderSelector() {
		// boolean isDisabled = Activator.getDefault().getBooleanPreference(Activator.GROOVY_SCRIPT_FILTER_ENABLED, false);
		// ALWAYS DISABLED
		boolean isDisabled = false;
		if (isDisabled) {
			this.disabled = isDisabled;
			this.scriptPatterns = null;
		} else {
			init(Activator.getDefault().getListStringPreference(Activator.GROOVY_SCRIPT_FILTERS,
					Activator.DEFAULT_GROOVY_SCRIPT_FILTER));
			this.disabled = false;
		}
	}

	private void init(List<String> listStringPreference) {
		if (listStringPreference == null) {
			scriptPatterns = CharOperation.NO_CHAR_CHAR;
			doCopy = new boolean[0];
		}
		scriptPatterns = new char[listStringPreference.size() / 2][];
		doCopy = new boolean[listStringPreference.size() / 2];
		int count = 0;
		int index = 0;

		for (String patternStr : listStringPreference) {
			if (count++ % 2 == 0) {
				scriptPatterns[index] = patternStr.toCharArray();
			} else {
				doCopy[index++] = patternStr.toCharArray()[0] == 'y';
			}
		}
	}

	/**
	 * Determines if the file name passed in should be treated as a script. Uses workspace script patterns to determine if the file
	 * name is a script or not
	 * 
	 * @param fileName project-relative file name to a groovy or groovy-like file
	 * @return true if file name matches the patterns or false otherwise
	 */
	public FileKind getFileKind(char[] filepath) {
		if (!disabled) {
			if (filepath != null) {
				for (int i = 0; i < scriptPatterns.length; i++) {
					char[] pattern = scriptPatterns[i];
					if (CharOperation.pathMatch(pattern, filepath, true, '/')) {
						return doCopy[i] ? FileKind.SCRIPT : FileKind.SCRIPT_NO_COPY;
					}
				}
			}
		}
		return FileKind.SOURCE;
	}

	/**
	 * Determines if the file name passed in should be treated as a script. Uses workspace script patterns to determine if the file
	 * name is a script or not
	 * 
	 * @param file groovy-like file
	 * @return true if file name matches the patterns or false otherwise
	 */
	public FileKind getFileKind(IResource file) {
		if (file == null) {
			return FileKind.SOURCE;
		}
		return getFileKind(file.getProjectRelativePath().toPortableString().toCharArray());
	}

	/**
	 * Convenience method for {@link ScriptFolderSelector#getFileKind(char[])}. Returns true for {@link FileKind#SCRIPT} and
	 * {@link FileKind#SCRIPT_NO_COPY} false for {@link FileKind#SOURCE}.
	 */
	public boolean isScript(char[] filepath) {
		FileKind kind = getFileKind(filepath);
		return kind == FileKind.SCRIPT || kind == FileKind.SCRIPT_NO_COPY;
	}

	/**
	 * Convenience method for {@link ScriptFolderSelector#getFileKind(char[])}. Returns true for {@link FileKind#SCRIPT} and
	 * {@link FileKind#SCRIPT_NO_COPY} false for {@link FileKind#SOURCE}.
	 */
	public boolean isScript(IResource file) {
		if (file == null) {
			return false;
		}
		return isScript(file.getProjectRelativePath().toPortableString().toCharArray());
	}

}
