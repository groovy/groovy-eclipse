/*
 * Copyright 2009-2019 the original author or authors.
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

import static org.eclipse.jdt.ui.PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_COLOR_SUFFIX;
import static org.eclipse.jdt.ui.PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlightings;
import org.eclipse.jdt.ui.text.IJavaColorConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

public interface PreferenceConstants {

    String GROOVY_EDITOR_HIGHLIGHT_PREFIX = "groovy.editor.highlight.";

    String GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR = GROOVY_EDITOR_HIGHLIGHT_PREFIX + "gjdk" + EDITOR_SEMANTIC_HIGHLIGHTING_COLOR_SUFFIX;

    String GROOVY_EDITOR_HIGHLIGHT_KEYWORDS_COLOR = GROOVY_EDITOR_HIGHLIGHT_PREFIX + "javakeywords" + EDITOR_SEMANTIC_HIGHLIGHTING_COLOR_SUFFIX;

    String GROOVY_EDITOR_HIGHLIGHT_PRIMITIVES_COLOR = GROOVY_EDITOR_HIGHLIGHT_PREFIX + "javatypes" + EDITOR_SEMANTIC_HIGHLIGHTING_COLOR_SUFFIX;

    String GROOVY_EDITOR_HIGHLIGHT_ASSERT_COLOR = GROOVY_EDITOR_HIGHLIGHT_PREFIX + "assert" + EDITOR_SEMANTIC_HIGHLIGHTING_COLOR_SUFFIX;

    String GROOVY_EDITOR_HIGHLIGHT_RETURN_COLOR = GROOVY_EDITOR_HIGHLIGHT_PREFIX + "return" + EDITOR_SEMANTIC_HIGHLIGHTING_COLOR_SUFFIX;

    String GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR = IJavaColorConstants.JAVA_STRING;

    String GROOVY_EDITOR_HIGHLIGHT_SLASHY_STRINGS = GROOVY_EDITOR_HIGHLIGHT_PREFIX + "slashy";

    String GROOVY_EDITOR_HIGHLIGHT_ANNOTATION_COLOR = EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + SemanticHighlightings.ANNOTATION + EDITOR_SEMANTIC_HIGHLIGHTING_COLOR_SUFFIX;

    String GROOVY_EDITOR_HIGHLIGHT_BRACKET_COLOR = IJavaColorConstants.JAVA_BRACKET;

    String GROOVY_EDITOR_HIGHLIGHT_OPERATOR_COLOR = IJavaColorConstants.JAVA_OPERATOR;

    String GROOVY_EDITOR_DEFAULT_COLOR = IJavaColorConstants.JAVA_DEFAULT;

    String GROOVY_EDITOR_BOLD_SUFFIX = "_bold";

    String GROOVY_SEMANTIC_HIGHLIGHTING = "groovy.semantic.highlighting";

    static IPreferenceStore getPreferenceStore() {
        return new ChainedPreferenceStore(new IPreferenceStore[] {GroovyPlugin.getDefault().getPreferenceStore(), JavaPlugin.getDefault().getPreferenceStore()});
    }
}
