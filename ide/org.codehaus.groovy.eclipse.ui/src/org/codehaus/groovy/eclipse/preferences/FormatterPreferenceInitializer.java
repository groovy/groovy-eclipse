/*
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
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
import org.codehaus.groovy.eclipse.refactoring.PreferenceConstants;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Class used to initialize default preference values.
 */
public class FormatterPreferenceInitializer extends AbstractPreferenceInitializer {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = GroovyPlugin.getDefault().getPreferenceStore();

        store.setDefault(PreferenceConstants.GROOVY_FORMATTER_MULTILINE_INDENTATION, PreferenceConstants.DEFAULT_INDENT_MULTILINE);

        store.setDefault(PreferenceConstants.GROOVY_FORMATTER_BRACES_START, PreferenceConstants.SAME);
        store.setDefault(PreferenceConstants.GROOVY_FORMATTER_BRACES_END, PreferenceConstants.NEXT);

        store.setDefault(PreferenceConstants.GROOVY_FORMATTER_MAX_LINELENGTH, PreferenceConstants.DEFAULT_MAX_LINE_LEN);
        store.setDefault(PreferenceConstants.GROOVY_FORMATTER_LONG_LIST_LENGTH, PreferenceConstants.DEFAULT_LONG_LIST_LENGTH);

        store.setDefault(PreferenceConstants.GROOVY_FORMATTER_REMOVE_UNNECESSARY_SEMICOLONS, false);
    }
}
