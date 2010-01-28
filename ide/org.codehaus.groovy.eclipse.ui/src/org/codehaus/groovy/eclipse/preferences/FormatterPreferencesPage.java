/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
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
import org.codehaus.groovy.eclipse.refactoring.PreferenceConstants;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


public class FormatterPreferencesPage 
extends FieldEditorOverlayPage 
implements IWorkbenchPreferencePage 
{

	public FormatterPreferencesPage() {
		super(GRID);
		setPreferenceStore(GroovyPlugin.getDefault().getPreferenceStore());
		new FormatterPreferenceInitializer().initializeDefaultPreferences();
	}

	@Override
    public void createFieldEditors() {

		addField(new RadioGroupFieldEditor(
				PreferenceConstants.GROOVY_FORMATTER_INDENTATION,
				"&Use Tabs or Spaces for indentation: ",2,new String[][] {{"Tab","tab"},{"Spaces","space"}}, getFieldEditorParent()));
		
		IntegerFieldEditor indsize = new IntegerFieldEditor(PreferenceConstants.GROOVY_FORMATTER_INDENTATION_SIZE,"&Tab size: ",getFieldEditorParent(),2);
		indsize.setValidRange(0, 10);
		addField(indsize);
		
		addField(new RadioGroupFieldEditor(PreferenceConstants.GROOVY_FORMATTER_BRACES_START,
				"Position of the opening braces {: ",2,
				new String[][] {{"On the same line: ","same"},{"On the next line: ","next"}},
				getFieldEditorParent()));

		addField(new RadioGroupFieldEditor(PreferenceConstants.GROOVY_FORMATTER_BRACES_END,
				"Position of the closing braces }: ",2,
				new String[][] {{"On the same line: ","same"},{"On the next line: ","next"}},
				getFieldEditorParent()));

		IntegerFieldEditor multiInd = new IntegerFieldEditor(PreferenceConstants.GROOVY_FORMATTER_MULTILINE_INDENTATION,"Default indentation for wrapped lines: ",getFieldEditorParent(),2);
		multiInd.setValidRange(0, 10);
		addField(multiInd);
		
	}

	@Override
	protected String getPageId() {
		return "org.codehaus.groovy.eclipse.preferences.formatter";
	}

	public void init(IWorkbench workbench) {
		
	}




}