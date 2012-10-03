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
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

public class FormatterPreferencesPage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage {

    public FormatterPreferencesPage() {
        super(GRID);
        setPreferenceStore(GroovyPlugin.getDefault().getPreferenceStore());
        new FormatterPreferenceInitializer().initializeDefaultPreferences();
    }

    @Override
    public void createFieldEditors() {
        addField(new RadioGroupFieldEditor(PreferenceConstants.GROOVY_FORMATTER_BRACES_START,
                "Position of the opening braces {: ",
                2,
                new String[][] { { "On the same line: ", PreferenceConstants.SAME }, { "On the next line: ", PreferenceConstants.NEXT } },
                getFieldEditorParent()));

        addField(new RadioGroupFieldEditor(PreferenceConstants.GROOVY_FORMATTER_BRACES_END,
                "Position of the closing braces }: ",
                2,
                new String[][] { { "On the same line: ", PreferenceConstants.SAME }, { "On the next line: ", PreferenceConstants.NEXT } },
                getFieldEditorParent()));

        IntegerFieldEditor multiInd = new IntegerFieldEditor(PreferenceConstants.GROOVY_FORMATTER_MULTILINE_INDENTATION,
                "Default indentation for wrapped lines: ",
                getFieldEditorParent(),
                2);
        multiInd.setValidRange(0, 10);
        addField(multiInd);

        IntegerFieldEditor listLenInt = new IntegerFieldEditor(PreferenceConstants.GROOVY_FORMATTER_LONG_LIST_LENGTH,
                "Length after which list are 'long' (long lists are wrapped): ",
                getFieldEditorParent(),
                PreferenceConstants.DEFAULT_LONG_LIST_LENGTH);
        listLenInt.setValidRange(0, 200);
        listLenInt.getLabelControl(getFieldEditorParent()).setToolTipText(
                "This value corresponds to the number of characters inside of the [ ], " +
                        "excluding leading/trailing whitespace.  All lists larger than this value " +
                "will be wrapped.");
        addField(listLenInt);



        addField(new BooleanFieldEditor(PreferenceConstants.GROOVY_FORMATTER_REMOVE_UNNECESSARY_SEMICOLONS,
                "Remove unnecessary semicolons", getFieldEditorParent()));

        PreferenceLinkArea area = new PreferenceLinkArea(getFieldEditorParent(),
                SWT.WRAP,
                "org.eclipse.jdt.ui.preferences.CodeFormatterPreferencePage",
                "\n\nTab and space related preferences \nare inherited from the <a>Java Formatter</a>", //$NON-NLS-1$
                (IWorkbenchPreferenceContainer) getContainer(),
                null);
        GridData data = new GridData(SWT.FILL, SWT.TOP, false, false);
        data.horizontalSpan = 2;
        area.getControl().setLayoutData(data);
    }

    @Override
    protected String getPageId() {
        return "org.codehaus.groovy.eclipse.preferences.formatter";
    }

    public void init(IWorkbench workbench) {}
}
