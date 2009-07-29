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
package org.codehaus.groovy.eclipse.editor;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class GroovyStringScanner extends RuleBasedScanner {
	public GroovyStringScanner() {
		// get color
		Preferences prefs = GroovyPlugin.getDefault().getPluginPreferences();
		if (prefs.getBoolean(PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_ENABLED)) {
			IPreferenceStore store = GroovyPlugin.getDefault().getPreferenceStore();
			RGB rgb = PreferenceConverter.getColor(store,PreferenceConstants.GROOVY_EDITOR_HIGHLIGHT_STRINGS_COLOR);
			// create tokens
			IToken token = new Token(new TextAttribute(new Color(null,rgb), null, SWT.NONE));
			setDefaultReturnToken(token);
		}
	}

}
