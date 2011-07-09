/*
 * Copyright 2011 the original author or authors.
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
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class SaveActionsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public SaveActionsPreferencePage() {
        super(GRID);
        setPreferenceStore(GroovyPlugin.getDefault().getPreferenceStore());
    }

    @Override
    public void createFieldEditors() {
        BooleanFieldEditor semicolonOption = new BooleanFieldEditor(PreferenceConstants.GROOVY_SAVE_ACTION_REMOVE_UNNECESSARY_SEMICOLONS, "Remove unnecessary semicolons", getFieldEditorParent());
        addField(semicolonOption);
    }

    public void init(IWorkbench workbench) {}
}
