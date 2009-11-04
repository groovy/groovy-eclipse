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
package org.codehaus.groovy.eclipse.core.preferences;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {
    
    private PreferenceConstants() {
        // uninstantiable
    }

	public static final String P_PATH = "pathPreference"; //$NON-NLS-1$

	public static final String GROOVY_EDITOR_HIGHLIGHT_GJDK_ENABLED = "groovy.editor.highlight.gjdk.enabled"; //$NON-NLS-1$
	public static final String GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR = "groovy.editor.highlight.gjdk.color"; //$NON-NLS-1$

	public static final String GROOVY_EDITOR_HIGHLIGHT_MULTILINECOMMENTS_ENABLED = "groovy.editor.highlight.multilinecomments.enabled"; //$NON-NLS-1$
	public static final String GROOVY_EDITOR_HIGHLIGHT_MULTILINECOMMENTS_COLOR = "groovy.editor.highlight.multilinecomments.color"; //$NON-NLS-1$

	public static final String GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_ENABLED = "groovy.editor.highlight.javakeywords.enabled"; //$NON-NLS-1$
	public static final String GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_COLOR = "groovy.editor.highlight.javakeywords.color"; //$NON-NLS-1$

	public static final String GROOVY_EDITOR_HIGHLIGHT_GROOVYKEYWORDS_ENABLED = "groovy.editor.highlight.groovykeywords.enabled"; //$NON-NLS-1$
	public static final String GROOVY_EDITOR_HIGHLIGHT_GROOVYKEYWORDS_COLOR = "groovy.editor.highlight.groovykeywords.color"; //$NON-NLS-1$

	public static final String GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_ENABLED = "groovy.editor.highlight.javatypes.enabled"; //$NON-NLS-1$
	public static final String GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_COLOR = "groovy.editor.highlight.javatypes.color"; //$NON-NLS-1$

	public static final String GROOVY_EDITOR_HIGHLIGHT_STRINGS_ENABLED = "groovy.editor.highlight.strings.enabled"; //$NON-NLS-1$
	public static final String GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR = "groovy.editor.highlight.strings.color"; //$NON-NLS-1$

	public static final String GROOVY_EDITOR_HIGHLIGHT_NUMBERS_ENABLED = "groovy.editor.highlight.numbers.enabled"; //$NON-NLS-1$
	public static final String GROOVY_EDITOR_HIGHLIGHT_NUMBERS_COLOR = "groovy.editor.highlight.numbers.color"; //$NON-NLS-1$

	// this preference will add the plugin trace statements to the error in order
	// to make it easier to track down problems at runtime
	public static final String GROOVY_LOG_TRACE_MESSAGES_ENABLED = "groovy.log.trace.messages.enabled"; //$NON-NLS-1$

	public static final String GROOVY_EDITOR_GROOVYDOC_KEYWORD_ENABLED = "groovy.editor.groovyDoc.keyword.enabled"; //$NON-NLS-1$
	public static final String GROOVY_EDITOR_GROOVYDOC_KEYWORD_COLOR = "groovy.editor.groovyDoc.keyword.color"; //$NON-NLS-1$

	public static final String GROOVY_EDITOR_GROOVYDOC_TAG_ENABLED = "groovy.editor.groovyDoc.tag.enabled"; //$NON-NLS-1$
	public static final String GROOVY_EDITOR_GROOVYDOC_TAG_COLOR = "groovy.editor.groovyDoc.tag.color"; //$NON-NLS-1$

	public static final String GROOVY_EDITOR_GROOVYDOC_LINK_ENABLED = "groovy.editor.groovyDoc.link.enabled"; //$NON-NLS-1$
	public static final String GROOVY_EDITOR_GROOVYDOC_LINK_COLOR = "groovy.editor.groovyDoc.link.color"; //$NON-NLS-1$

	public static final String GROOVY_EDITOR_FOLDING_ENABLED = "groovy.editor.folding.enabled"; //$NON-NLS-1$
	
	public static final String GROOVY_RUNTIME_SOURCE = "groovy.runtime.lib.source";

	public static final String GROOVY_JUNIT_MONOSPACE_FONT = "groovy.junit.monospace.font";
	
	public static final String GROOVY_COMPILER_VERSION_IS_16 = "groovy.compiler.version.is.16";
	
	// Store the last version number so that we can check to see if the number has been upgraded since last startup
	public static final String GROOVY_PLUGIN_LAST_VERSION_NUMBER = "groovy.plugin.version";
	
	public static final String GROOVY_ASK_TO_CONVERT_LEGACY_PROJECTS = "groovy.plugin.ask.to.convert";
	
	public static final String GROOVY_SEMANTIC_HIGHLIGHTING = "groovy.semantic.highlighting";
}
