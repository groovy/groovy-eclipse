/*
 * Copyright 2003-2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.editor;

import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_BOLD_SUFFIX;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_DEFAULT_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_ANNOTATION_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_BRACKET_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_NUMBERS_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_OPERATOR_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_RETURN_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.eclipse.jdt.internal.ui.text.JavaColorManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * @author Andrew Eisenberg
 * @created Jul 22, 2009
 *
 */
public class GroovyColorManager extends JavaColorManager {

    private ColorPreferencesChangeListener changeListener;

    private class ColorPreferencesChangeListener implements IPropertyChangeListener {

        public void propertyChange(PropertyChangeEvent event) {
            if (event.getProperty().startsWith(GROOVY_EDITOR_HIGHLIGHT)
                    && !(event.getProperty().endsWith(GROOVY_EDITOR_BOLD_SUFFIX))) {
                if (event.getNewValue() != event.getOldValue()) {
                    unbindColor(event.getProperty());
                }
            }
        }
    }

    public GroovyColorManager() {
        super();
        changeListener = new ColorPreferencesChangeListener();
        GroovyPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(changeListener);
        initialize();
    }

    @Override
    public void dispose() {
        super.dispose();
        GroovyPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(changeListener);
    }
    public void initialize() {
        bindColor(GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR,
                PreferenceConverter.getColor(getGroovyPreferenceStore(), GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR));
        bindColor(GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_COLOR,
                PreferenceConverter.getColor(getGroovyPreferenceStore(), GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_COLOR));
        bindColor(GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_COLOR,
                PreferenceConverter.getColor(getGroovyPreferenceStore(), GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_COLOR));
        bindColor(GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR,
                PreferenceConverter.getColor(getGroovyPreferenceStore(), GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR));
        bindColor(GROOVY_EDITOR_HIGHLIGHT_NUMBERS_COLOR,
                PreferenceConverter.getColor(getGroovyPreferenceStore(), GROOVY_EDITOR_HIGHLIGHT_NUMBERS_COLOR));
        bindColor(GROOVY_EDITOR_DEFAULT_COLOR,
                PreferenceConverter.getColor(getGroovyPreferenceStore(), GROOVY_EDITOR_DEFAULT_COLOR));
        bindColor(GROOVY_EDITOR_HIGHLIGHT_ANNOTATION_COLOR,
                PreferenceConverter.getColor(getGroovyPreferenceStore(), GROOVY_EDITOR_HIGHLIGHT_ANNOTATION_COLOR));
        bindColor(GROOVY_EDITOR_HIGHLIGHT_BRACKET_COLOR,
                PreferenceConverter.getColor(getGroovyPreferenceStore(), GROOVY_EDITOR_HIGHLIGHT_BRACKET_COLOR));
        bindColor(GROOVY_EDITOR_HIGHLIGHT_RETURN_COLOR,
                PreferenceConverter.getColor(getGroovyPreferenceStore(), GROOVY_EDITOR_HIGHLIGHT_RETURN_COLOR));
        bindColor(GROOVY_EDITOR_HIGHLIGHT_OPERATOR_COLOR,
                PreferenceConverter.getColor(getGroovyPreferenceStore(), GROOVY_EDITOR_HIGHLIGHT_OPERATOR_COLOR));
    }

    public void uninitialize() {
        unbindColor(GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR);
        unbindColor(GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_COLOR);
        unbindColor(GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_COLOR);
        unbindColor(GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR);
        unbindColor(GROOVY_EDITOR_HIGHLIGHT_NUMBERS_COLOR);
        unbindColor(GROOVY_EDITOR_DEFAULT_COLOR);
        unbindColor(GROOVY_EDITOR_HIGHLIGHT_ANNOTATION_COLOR);
        unbindColor(GROOVY_EDITOR_HIGHLIGHT_BRACKET_COLOR);
        unbindColor(GROOVY_EDITOR_HIGHLIGHT_RETURN_COLOR);
        unbindColor(GROOVY_EDITOR_HIGHLIGHT_OPERATOR_COLOR);
    }

    private IPreferenceStore getGroovyPreferenceStore() {
        return GroovyPlugin.getDefault().getPreferenceStore();
    }

}
