/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.preferences;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = GroovyPlugin.getDefault().getPreferenceStore();

		store.setDefault(PreferenceConstants.GROOVY_LOG_TRACE_MESSAGES_ENABLED,
				false);

		// GJDK Prefs
		store.setDefault(
				PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GJDK_ENABLED, true);
		PreferenceConverter.setDefault(store,
				PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GJDK_COLOR,
				new RGB(102, 204, 255));

		// Multiline Comment Prefs
		store
				.setDefault(
						PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_MULTILINECOMMENTS_ENABLED,
						true);
		PreferenceConverter
				.setDefault(
						store,
						PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_MULTILINECOMMENTS_COLOR,
						new RGB(204, 0, 0));

		// Java Keyword Prefs
		store
				.setDefault(
						PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_ENABLED,
						true);
		PreferenceConverter.setDefault(store,
				PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVAKEYWORDS_COLOR,
				new RGB(0, 153, 255));

		// Groovy Keyword Prefs
		store
				.setDefault(
						PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GROOVYKEYWORDS_ENABLED,
						true);
		PreferenceConverter
				.setDefault(
						store,
						PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_GROOVYKEYWORDS_COLOR,
						new RGB(0, 153, 102));

		// Java Types Prefs
		store.setDefault(
				PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_ENABLED,
				true);
		PreferenceConverter.setDefault(store,
				PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_JAVATYPES_COLOR,
				new RGB(0, 153, 255));

		// String Prefs
		store.setDefault(
				PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_ENABLED,
				true);
		PreferenceConverter.setDefault(store,
				PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR,
				new RGB(255, 0, 204));

		// Number Prefs
		store.setDefault(
				PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_NUMBERS_ENABLED,
				true);
		PreferenceConverter.setDefault(store,
				PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_NUMBERS_COLOR,
				new RGB(255, 0, 0));

		// Enable Folding
		store.setDefault(PreferenceConstants.GROOVY_EDITOR_FOLDING_ENABLED,
				true);
	}

}
