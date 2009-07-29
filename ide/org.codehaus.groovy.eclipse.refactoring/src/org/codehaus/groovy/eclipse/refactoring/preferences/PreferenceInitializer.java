/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Michael Klenk and others        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.refactoring.preferences;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
    public void initializeDefaultPreferences() {
		IPreferenceStore store = GroovyPlugin.getDefault().getPreferenceStore();	
		
		// Formatter Prefs
		store.setDefault(PreferenceConstants.GROOVY_FORMATTER_INDENTATION, "tab");
		store.setDefault(PreferenceConstants.GROOVY_FORMATTER_INDENTATION_SIZE, 4);
		store.setDefault(PreferenceConstants.GROOVY_FORMATTER_MULTILINE_INDENTATION, 2);

		store.setDefault(PreferenceConstants.GROOVY_FORMATTER_BRACES_START, "same");
		store.setDefault(PreferenceConstants.GROOVY_FORMATTER_BRACES_END, "next");
		
		store.setDefault(PreferenceConstants.GROOVY_FORMATTER_MAX_LINELENGTH, 80);

	}


}
