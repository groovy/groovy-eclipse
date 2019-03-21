/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.preferences;

import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.*;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

public class PreferenceInitializer extends AbstractPreferenceInitializer {
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore prefs = GroovyPlugin.getDefault().getPreferenceStore();

        // Debug
        prefs.setDefault(GROOVY_DEBUG_FILTER_STACK, true);
        prefs.setDefault(GROOVY_DEBUG_FORCE_DEBUG_OPTIONS_ON_STARTUP, true);
        prefs.setDefault(GROOVY_DEBUG_FILTER_LIST, "groovy.lang,groovy.ui,java.lang.reflect,org.codehaus.groovy,sun.misc,sun.reflect");

        // General
        prefs.setDefault(GROOVY_JUNIT_MONOSPACE_FONT, false);
        prefs.setDefault(GROOVY_LOG_TRACE_MESSAGES_ENABLED, false);
        prefs.setDefault(GROOVY_ASK_TO_CONVERT_LEGACY_PROJECTS, true);
        prefs.setDefault(GROOVY_SCRIPT_DEFAULT_WORKING_DIRECTORY, GROOVY_SCRIPT_PROJECT_HOME);

        // Semantic highlighting
        prefs.setDefault(GROOVY_SEMANTIC_HIGHLIGHTING, true);
        prefs.setDefault(GROOVY_EDITOR_HIGHLIGHT_SLASHY_STRINGS, true);

        // Syntax coloring
        setSyntaxElementDefault(prefs, GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_COLOR, 127, 0, 85, true);
        setSyntaxElementDefault(prefs, GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_COLOR, 127, 0, 85, true);
        setSyntaxElementDefault(prefs, GROOVY_EDITOR_HIGHLIGHT_GROOVYKEYWORDS_COLOR, 127, 0, 85, true);
        setSyntaxElementDefault(prefs, GROOVY_EDITOR_HIGHLIGHT_RETURN_COLOR, 127, 0, 85, true); //keyword
        setSyntaxElementDefault(prefs, GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR, 102, 204, 255, false); //method
        setSyntaxElementDefault(prefs, GROOVY_EDITOR_HIGHLIGHT_ANNOTATION_COLOR, 100, 100, 100, false);
        setSyntaxElementDefault(prefs, GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR, 255, 0, 204, false);
        setSyntaxElementDefault(prefs, GROOVY_EDITOR_HIGHLIGHT_BRACKET_COLOR, 0, 0, 0, false);
        setSyntaxElementDefault(prefs, GROOVY_EDITOR_HIGHLIGHT_OPERATOR_COLOR, 0, 0, 0, false);
        setSyntaxElementDefault(prefs, GROOVY_EDITOR_DEFAULT_COLOR, 0, 0, 0, false);
    }

    private void setSyntaxElementDefault(IPreferenceStore prefs, String pref, int r, int g, int b, boolean bold) {
        PreferenceConverter.setDefault(prefs, pref, new RGB(r, g, b));
        prefs.setDefault(pref + GROOVY_EDITOR_BOLD_SUFFIX, bold);
    }
}
