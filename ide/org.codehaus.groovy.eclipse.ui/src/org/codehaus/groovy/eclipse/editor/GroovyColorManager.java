/*
 * Copyright 2009-2020 the original author or authors.
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
package org.codehaus.groovy.eclipse.editor;

import org.codehaus.groovy.eclipse.preferences.PreferenceConstants;
import org.eclipse.jdt.internal.ui.text.JavaColorManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

public class GroovyColorManager extends JavaColorManager {

    private IPropertyChangeListener propertyChangeListener = (PropertyChangeEvent event) -> {
        if (event.getNewValue() != event.getOldValue() && fKeyTable.containsKey(event.getProperty())) {
            unbindColor(event.getProperty());
        }
    };

    public GroovyColorManager() {
        initialize();
        PreferenceConstants.getPreferenceStore().addPropertyChangeListener(propertyChangeListener);
    }

    @Override
    public void dispose() {
        super.dispose();
        PreferenceConstants.getPreferenceStore().removePropertyChangeListener(propertyChangeListener);
    }

    public void initialize() {
        IPreferenceStore preferenceStore = PreferenceConstants.getPreferenceStore();
        bindColor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR,
            PreferenceConverter.getColor(preferenceStore, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR));
        bindColor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_PRIMITIVES_COLOR,
            PreferenceConverter.getColor(preferenceStore, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_PRIMITIVES_COLOR));
        bindColor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_KEYWORDS_COLOR,
            PreferenceConverter.getColor(preferenceStore, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_KEYWORDS_COLOR));
        bindColor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_ASSERT_COLOR,
            PreferenceConverter.getColor(preferenceStore, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_ASSERT_COLOR));
        bindColor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_RETURN_COLOR,
            PreferenceConverter.getColor(preferenceStore, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_RETURN_COLOR));
        bindColor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR,
            PreferenceConverter.getColor(preferenceStore, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR));
        bindColor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_ANNOTATION_COLOR,
            PreferenceConverter.getColor(preferenceStore, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_ANNOTATION_COLOR));
        bindColor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_BRACKET_COLOR,
            PreferenceConverter.getColor(preferenceStore, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_BRACKET_COLOR));
        bindColor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_OPERATOR_COLOR,
            PreferenceConverter.getColor(preferenceStore, PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_OPERATOR_COLOR));
        bindColor(PreferenceConstants.GROOVY_EDITOR_DEFAULT_COLOR,
            PreferenceConverter.getColor(preferenceStore, PreferenceConstants.GROOVY_EDITOR_DEFAULT_COLOR));
    }

    public void uninitialize() {
        unbindColor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR);
        unbindColor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_PRIMITIVES_COLOR);
        unbindColor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_KEYWORDS_COLOR);
        unbindColor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_ASSERT_COLOR);
        unbindColor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_RETURN_COLOR);
        unbindColor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR);
        unbindColor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_ANNOTATION_COLOR);
        unbindColor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_BRACKET_COLOR);
        unbindColor(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_OPERATOR_COLOR);
        unbindColor(PreferenceConstants.GROOVY_EDITOR_DEFAULT_COLOR);
    }
}
