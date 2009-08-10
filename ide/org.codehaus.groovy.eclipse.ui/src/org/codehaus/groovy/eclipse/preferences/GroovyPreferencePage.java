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
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class GroovyPreferencePage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage {

    public GroovyPreferencePage() {
        super(GRID);
        setPreferenceStore(GroovyPlugin.getDefault().getPreferenceStore());
    }
    


    public void init(IWorkbench workbench) {}

    protected String getPageId() {
        return this.getClass().getPackage().getName();
    }



    @Override
    protected void createFieldEditors() {
        // JUnit Monospace
        final BooleanFieldEditor classFilePrefEditor = new BooleanFieldEditor(
                PreferenceConstants.GROOVY_JUNIT_MONOSPACE_FONT,
                "&Use monospace font in the JUnit results pane.\n" +
                "This is particularly useful for testing frameworks\n" +
                "that use a formatted output such as Spock",
                getFieldEditorParent());
        classFilePrefEditor.setPreferenceStore(getPreferenceStore());
        addField(classFilePrefEditor);
        
    }
    
    

}