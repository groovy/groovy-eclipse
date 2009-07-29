/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

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
    }

    private IPreferenceStore getPreferenceStore() {
        return JavaPlugin.getDefault().getPreferenceStore();
    }

}
