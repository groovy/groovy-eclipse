/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.preferences;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

/**
 * @see org.eclipse.jface.preference.PreferencePage
 */
public class TemplatesPreferencePage extends TemplatePreferencePage implements IWorkbenchPreferencePage {
	
	public TemplatesPreferencePage() {
		setPreferenceStore(GroovyPlugin.getDefault().getPreferenceStore());
		setTemplateStore(GroovyPlugin.getDefault().getTemplateStore());
		setContextTypeRegistry(GroovyPlugin.getDefault().getContextTypeRegistry());
	}

	protected boolean isShowFormatterSetting() {
		return false;
	}
	
	
	public boolean performOk() {
		boolean ok= super.performOk();
		
		GroovyPlugin.getDefault().savePluginPreferences();		
		return ok;
	}
}
