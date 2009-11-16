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
package org.codehaus.groovy.eclipse.editor;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.JavaColorManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;

import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_GROOVYDOC_KEYWORD_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_GROOVYDOC_TAG_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_GROOVYDOC_LINK_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_MULTILINECOMMENTS_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_NUMBERS_COLOR;
import static org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants.GROOVY_EDITOR_DEFAULT_COLOR;

/**
 * @author Andrew Eisenberg
 * @created Jul 22, 2009
 *
 */
public class GroovyColorManager extends JavaColorManager {
    
    
    public GroovyColorManager() {
        super();
        
        bindColor(GROOVY_EDITOR_GROOVYDOC_KEYWORD_COLOR, PreferenceConverter.getColor(getPreferenceStore(), GROOVY_EDITOR_GROOVYDOC_KEYWORD_COLOR));
        bindColor(GROOVY_EDITOR_GROOVYDOC_TAG_COLOR, PreferenceConverter.getColor(getPreferenceStore(), GROOVY_EDITOR_GROOVYDOC_TAG_COLOR));
        bindColor(GROOVY_EDITOR_GROOVYDOC_LINK_COLOR, PreferenceConverter.getColor(getPreferenceStore(), GROOVY_EDITOR_GROOVYDOC_LINK_COLOR));
        bindColor(GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR, PreferenceConverter.getColor(getPreferenceStore(), GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR));
        bindColor(GROOVY_EDITOR_HIGHLIGHT_MULTILINECOMMENTS_COLOR, PreferenceConverter.getColor(getPreferenceStore(), GROOVY_EDITOR_HIGHLIGHT_MULTILINECOMMENTS_COLOR));
        bindColor(GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_COLOR, PreferenceConverter.getColor(getPreferenceStore(), GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_COLOR));
        bindColor(GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_COLOR, PreferenceConverter.getColor(getPreferenceStore(), GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_COLOR));
        bindColor(GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR, PreferenceConverter.getColor(getPreferenceStore(), GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR));
        bindColor(GROOVY_EDITOR_HIGHLIGHT_NUMBERS_COLOR, PreferenceConverter.getColor(getPreferenceStore(), GROOVY_EDITOR_HIGHLIGHT_NUMBERS_COLOR));
        bindColor(GROOVY_EDITOR_DEFAULT_COLOR, PreferenceConverter.getColor(getPreferenceStore(), GROOVY_EDITOR_DEFAULT_COLOR));
    }

    private IPreferenceStore getPreferenceStore() {
        return JavaPlugin.getDefault().getPreferenceStore();
    }

}
