/*
 * Copyright 2009-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.jdt.groovy.core.util;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.Activator;

public class ScriptFolderSelector implements IEclipsePreferences.IPreferenceChangeListener {

    public static boolean isEnabled(IProject project) {
        IEclipsePreferences preferences = getGroovyPreferences(project);
        if (preferences != null) {
            return preferences.getBoolean(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, Activator.DEFAULT_SCRIPT_FILTERS_ENABLED);
        }
        return false;
    }

    private static IEclipsePreferences getGroovyPreferences(IProject project) {
        IEclipsePreferences preferences = Activator.getProjectPreferences(project);
        if (preferences == null || !preferences.getBoolean(Activator.USING_PROJECT_PROPERTIES, false)) {
            preferences = Activator.getInstancePreferences();
        }
        return preferences;
    }

    public enum FileKind {
        SOURCE, SCRIPT, SCRIPT_NO_COPY
    }

    //--------------------------------------------------------------------------

    private boolean enabled;
    private boolean[] doCopy;
    private char[][] scriptPatterns;
    private IEclipsePreferences preferences;

    public ScriptFolderSelector(IProject project) {
        preferences = getGroovyPreferences(project);
        if (preferences == null) {
            enabled = false;
        } else {
            preferenceChange(null);
        }
    }

    /** Do not use! For testing only. */
    protected ScriptFolderSelector(List<String> preferences, boolean enabled) {
        this.enabled = enabled;
        initFilters(preferences);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (preferences != null) {
            try {
                preferences.removePreferenceChangeListener(this);
            } finally {
                preferences = null;
                enabled = false;
            }
        }
    }

    @Override
    public void preferenceChange(IEclipsePreferences.PreferenceChangeEvent event) {
        doCopy = null;
        scriptPatterns = null;
        enabled = preferences.getBoolean(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, Activator.DEFAULT_SCRIPT_FILTERS_ENABLED);
        if (enabled) {
            String filters = preferences.get(Activator.GROOVY_SCRIPT_FILTERS, Activator.DEFAULT_GROOVY_SCRIPT_FILTER);
            initFilters(Arrays.asList(filters.split(",")));
        }
    }

    private void initFilters(List<String> listStringPreference) {
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
