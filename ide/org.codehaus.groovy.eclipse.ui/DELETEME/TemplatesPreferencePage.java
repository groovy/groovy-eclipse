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
