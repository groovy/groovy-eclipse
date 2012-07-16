/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.preferences;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.GroovyCoreActivator;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jdt.groovy.core.Activator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    /*
     * (non-Javadoc)
     *
     * @seeorg.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
     * initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = GroovyPlugin.getDefault().getPreferenceStore();

        store.setDefault(PreferenceConstants.GROOVY_LOG_TRACE_MESSAGES_ENABLED, false);

        // Syntax coloring
        PreferenceConverter.setDefault(store, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR, new RGB(102, 204, 255));
        PreferenceConverter.setDefault(store, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_COLOR, new RGB(151, 44, 120));
        PreferenceConverter.setDefault(store, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GROOVYKEYWORDS_COLOR, new RGB(151, 44, 120));
        PreferenceConverter.setDefault(store, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_COLOR, new RGB(151, 44, 120));
        PreferenceConverter.setDefault(store, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR, new RGB(255, 0, 204));
        PreferenceConverter.setDefault(store, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_NUMBERS_COLOR, new RGB(205, 50, 0));
        PreferenceConverter.setDefault(store, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_ANNOTATION_COLOR, new RGB(70, 70, 70));
        PreferenceConverter.setDefault(store, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_RETURN_COLOR, new RGB(151, 44, 120));
        PreferenceConverter.setDefault(store, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_BRACKET_COLOR, new RGB(0, 0, 0));
        PreferenceConverter.setDefault(store, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_OPERATOR_COLOR, new RGB(0, 0, 0));
        PreferenceConverter.setDefault(store, PreferenceConstants.GROOVY_EDITOR_DEFAULT_COLOR, new RGB(0, 0, 0));

        // bold syntax
        store.setDefault(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_COLOR + PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX, true);
        store.setDefault(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GROOVYKEYWORDS_COLOR + PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX, true);
        store.setDefault(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_COLOR + PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX, true);
        store.setDefault(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_RETURN_COLOR + PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX, true);
        store.setDefault(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR + PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX, false);
        store.setDefault(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR + PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX, false);
        store.setDefault(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_NUMBERS_COLOR + PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX, false);
        store.setDefault(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_ANNOTATION_COLOR + PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX, false);
        store.setDefault(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_BRACKET_COLOR + PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX, false);
        store.setDefault(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_OPERATOR_COLOR + PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX, false);
        store.setDefault(PreferenceConstants.GROOVY_EDITOR_DEFAULT_COLOR + PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX, false);

        // Dollar slashy string highlighting
        store.setDefault(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_SLASHY_STRINGS, true);

        // JUnit Monospace font
        store.setDefault(PreferenceConstants.GROOVY_JUNIT_MONOSPACE_FONT, false);

        // Ask to convert Legacy Projects at startup
        store.setDefault(PreferenceConstants.GROOVY_ASK_TO_CONVERT_LEGACY_PROJECTS, true);

        // Semantic highlighting
        store.setDefault(PreferenceConstants.GROOVY_SEMANTIC_HIGHLIGHTING, true);

        // Groovier Content assist
        store.setDefault(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, true);
        store.setDefault(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, true);
        store.setDefault(PreferenceConstants.GROOVY_CONTENT_NAMED_ARGUMENTS, false);
        store.setDefault(PreferenceConstants.GROOVY_CONTENT_PARAMETER_GUESSING, true);

        store.setDefault(PreferenceConstants.GROOVY_SCRIPT_DEFAULT_WORKING_DIRECTORY, PreferenceConstants.GROOVY_SCRIPT_PROJECT_HOME);

        // Debug
        store.setDefault(PreferenceConstants.GROOVY_DEBUG_FILTER_STACK, true);
        store.setDefault(PreferenceConstants.GROOVY_DEBUG_FILTER_LIST, "org.codehaus.groovy,groovy.lang,java.lang.reflect,sun.reflect,groovy.ui,sun.misc");

        store.setDefault(PreferenceConstants.GROOVY_DEBUG_FORCE_DEBUG_OPTIONS_ON_STARTUP, true);

        // Compile
        store.setDefault(Activator.GROOVY_SCRIPT_FILTERS, Activator.DEFAULT_GROOVY_SCRIPT_FILTER);
        store.setDefault(Activator.GROOVY_SCRIPT_FILTERS_ENABLED, false);
        store.setDefault(Activator.GROOVY_CHECK_FOR_COMPILER_MISMATCH, true);
    }

    public void reset() {
        IPreferenceStore store = GroovyPlugin.getDefault().getPreferenceStore();

        store.setValue(PreferenceConstants.GROOVY_LOG_TRACE_MESSAGES_ENABLED, false);

        // Syntax coloring
        PreferenceConverter.setValue(store, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR, new RGB(102, 204, 255));
        PreferenceConverter.setValue(store, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_COLOR, new RGB(151, 44, 120));
        PreferenceConverter.setValue(store, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GROOVYKEYWORDS_COLOR, new RGB(151, 44, 120));
        PreferenceConverter.setValue(store, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_COLOR, new RGB(151, 44, 120));
        PreferenceConverter.setValue(store, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR, new RGB(255, 0, 204));
        PreferenceConverter.setValue(store, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_NUMBERS_COLOR, new RGB(205, 50, 0));
        PreferenceConverter.setValue(store, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_ANNOTATION_COLOR, new RGB(70, 70, 70));
        PreferenceConverter.setValue(store, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_RETURN_COLOR, new RGB(151, 44, 120));
        PreferenceConverter.setValue(store, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_BRACKET_COLOR, new RGB(0, 0, 0));
        PreferenceConverter.setValue(store, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_OPERATOR_COLOR, new RGB(0, 0, 0));
        PreferenceConverter.setValue(store, PreferenceConstants.GROOVY_EDITOR_DEFAULT_COLOR, new RGB(0, 0, 0));

        // bold syntax
        store.setValue(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_COLOR + PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX, true);
        store.setValue(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GROOVYKEYWORDS_COLOR + PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX, true);
        store.setValue(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_COLOR + PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX, true);
        store.setValue(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_RETURN_COLOR + PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX, true);
        store.setValue(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR + PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX, false);
        store.setValue(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR + PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX, false);
        store.setValue(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_NUMBERS_COLOR + PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX, false);
        store.setValue(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_ANNOTATION_COLOR + PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX, false);
        store.setValue(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_BRACKET_COLOR + PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX, false);
        store.setValue(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_OPERATOR_COLOR + PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX, false);
        store.setValue(PreferenceConstants.GROOVY_EDITOR_DEFAULT_COLOR + PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX, false);

        // Dollar slashy string highlighting
        store.setValue(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_SLASHY_STRINGS, true);

        // JUnit Monospace font
        store.setValue(PreferenceConstants.GROOVY_JUNIT_MONOSPACE_FONT, false);

        // Ask to convert Legacy Projects at startup
        store.setValue(PreferenceConstants.GROOVY_ASK_TO_CONVERT_LEGACY_PROJECTS, true);

        // Semantic highlighting
        store.setValue(PreferenceConstants.GROOVY_SEMANTIC_HIGHLIGHTING, true);

        // Groovier Content assist
        store.setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, true);
        store.setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS, true);
        store.setValue(PreferenceConstants.GROOVY_CONTENT_NAMED_ARGUMENTS, false);
        store.setValue(PreferenceConstants.GROOVY_CONTENT_PARAMETER_GUESSING, true);

        store.setValue(PreferenceConstants.GROOVY_SCRIPT_DEFAULT_WORKING_DIRECTORY, "proj_home");

        // Compile
        Activator.getDefault().setPreference(null, Activator.GROOVY_SCRIPT_FILTERS, Activator.DEFAULT_GROOVY_SCRIPT_FILTER);
        Activator.getDefault().setPreference(null, Activator.GROOVY_SCRIPT_FILTERS_ENABLED, "true");

        GroovyCoreActivator.getDefault().setPreference(PreferenceConstants.GROOVY_CLASSPATH_USE_GROOVY_LIB_GLOBAL, true);
    }
}
