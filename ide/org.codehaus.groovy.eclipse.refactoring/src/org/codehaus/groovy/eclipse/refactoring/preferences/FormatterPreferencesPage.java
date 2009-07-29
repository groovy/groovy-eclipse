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
import org.codehaus.groovy.eclipse.preferences.FieldEditorOverlayPage;
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
		new PreferenceInitializer().initializeDefaultPreferences();
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

//		IntegerFieldEditor maxLineLength = new IntegerFieldEditor(PreferenceConstants.GROOVY_FORMATTER_MAX_LINELENGTH,"Maximum line width: ",getFieldEditorParent(),2);
//		maxLineLength.setValidRange(10, 400);
//		addField(maxLineLength);
		
		IntegerFieldEditor multiInd = new IntegerFieldEditor(PreferenceConstants.GROOVY_FORMATTER_MULTILINE_INDENTATION,"Default indentation for wrapped lines: ",getFieldEditorParent(),2);
		multiInd.setValidRange(0, 10);
		addField(multiInd);
		
	}

	@Override
	protected String getPageId() {
		return "Formatter";
	}

	public void init(IWorkbench workbench) {
		
	}




}