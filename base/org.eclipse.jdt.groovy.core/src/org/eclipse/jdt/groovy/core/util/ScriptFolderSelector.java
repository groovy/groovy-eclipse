/*******************************************************************************
 * Copyright (c) 2009, 2010 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.groovy.core.util;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.Activator;

public class ScriptFolderSelector {

	public static enum FileKind {
		SOURCE, SCRIPT, SCRIPT_NO_COPY
	}

	private char[][] scriptPatterns;
	private boolean[] doCopy;
	private final boolean enabled;
	private IEclipsePreferences preferences;

	public static boolean isEnabled(IProject project) {
		// disabled by default
		Activator activator = Activator.getDefault();
		// perform null check since this is occasionally being called during shutdown after the plugin has been closed
		if (activator != null) {
			IEclipsePreferences preferences = activator.getProjectOrWorkspacePreferences(project);
			return activator.getBooleanPreference(preferences, Activator.GROOVY_SCRIPT_FILTERS_ENABLED, false);
		} else {
			return false;
		}
	}

	public ScriptFolderSelector(IProject project) {
		Activator activator = Activator.getDefault();
		if (activator == null) {
			// either in the middle of startup or shutdown
			this.enabled = false;
		} else {
			preferences = activator.getProjectOrWorkspacePreferences(project);
			// disabled by default
			boolean isEnabled = activator.getBooleanPreference(preferences, Activator.GROOVY_SCRIPT_FILTERS_ENABLED, false);
			if (!isEnabled) {
				this.enabled = false;
				this.scriptPatterns = null;
			} else {
				init(activator.getListStringPreference(preferences, Activator.GROOVY_SCRIPT_FILTERS,
						Activator.DEFAULT_GROOVY_SCRIPT_FILTER));
				this.enabled = true;
			}
		}
	}

	/**
	 * do not use! For testing only
	 */
	protected ScriptFolderSelector(List<String> preferences, boolean isEnabled) {
		this.enabled = isEnabled;
		init(preferences);
	}

	private void init(List<String> listStringPreference) {
		if (listStringPreference == null) {
			scriptPatterns = CharOperation.NO_CHAR_CHAR;
			doCopy = new boolean[0];
		}
		int size = listStringPreference.size();
		if (size % 2 == 0) {
			scriptPatterns = new char[size / 2][];
			doCopy = new boolean[size / 2];
		} else {
			// preferences are a little bit wacky
			scriptPatterns = new char[1 + size / 2][];
			doCopy = new boolean[1 + size / 2];
		}
		int count = 0;
		int index = 0;

		for (String patternStr : listStringPreference) {
			if (count++ % 2 == 0) {
				scriptPatterns[index] = patternStr.toCharArray();
			} else {
				char[] pattern = patternStr.toCharArray();
				doCopy[index++] = pattern.length > 0 && pattern[0] == 'y';
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
		if (enabled) {
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
		if (file == null || !enabled) {
			return FileKind.SOURCE;
		}
		return getFileKind(file.getProjectRelativePath().toPortableString().toCharArray());
	}

	/**
	 * Convenience method for {@link ScriptFolderSelector#getFileKind(char[])}. Returns true for {@link FileKind#SCRIPT} and
	 * {@link FileKind#SCRIPT_NO_COPY} false for {@link FileKind#SOURCE}.
	 */
	public boolean isScript(char[] filepath) {
		if (filepath == null || !enabled) {
			return false;
		}
		FileKind kind = getFileKind(filepath);
		return kind == FileKind.SCRIPT || kind == FileKind.SCRIPT_NO_COPY;
	}

	/**
	 * Convenience method for {@link ScriptFolderSelector#getFileKind(char[])}. Returns true for {@link FileKind#SCRIPT} and
	 * {@link FileKind#SCRIPT_NO_COPY} false for {@link FileKind#SOURCE}.
	 */
	public boolean isScript(IResource file) {
		if (file == null || !enabled) {
			return false;
		}
		return isScript(file.getProjectRelativePath().toPortableString().toCharArray());
	}

}
