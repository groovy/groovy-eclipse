/*
 * Copyright 2009-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.preferences;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.builder.ConvertLegacyProject;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class GroovyPreferencePage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage {

    private final class MonospaceFieldEditor extends BooleanFieldEditor {
        Label myLabel;

        private MonospaceFieldEditor() {
            super(PreferenceConstants.GROOVY_JUNIT_MONOSPACE_FONT, "&Use monospace font for JUnit (deprecated)", getFieldEditorParent());
            setEnabled(true, getFieldEditorParent());
            setPreferenceStore(getPreferenceStore());
        }

        // override so we can set line wrap
        @Override
        public Label getLabelControl(Composite parent) {
            if (myLabel == null) {
                myLabel = new Label(parent, SWT.LEFT | SWT.WRAP);
                myLabel.setFont(parent.getFont());
                String text = getLabelText();
                if (text != null) {
                    myLabel.setText(text);
                }
                myLabel.addDisposeListener(event -> { myLabel = null; });
            } else {
                checkParent(myLabel, parent);
            }
            return myLabel;
        }

        @Override
        protected Label getLabelControl() {
            return myLabel;
        }
    }

    public GroovyPreferencePage() {
        super(GRID);
        setPreferenceStore(GroovyPlugin.getDefault().getPreferenceStore());
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected String getPageId() {
        return "org.codehaus.groovy.eclipse.preferences";
    }

    @Override
    protected void createFieldEditors() {
        // JUnit Monospace
        addField(new MonospaceFieldEditor());
        new Label(getFieldEditorParent(), SWT.LEFT | SWT.WRAP).setText(
            "  This is particularly useful for testing with the assert keyword");

        // default launch location for scripts
        addField(new RadioGroupFieldEditor(PreferenceConstants.GROOVY_SCRIPT_DEFAULT_WORKING_DIRECTORY,
            "\nDefault working directory for running Groovy scripts\n  (will not change the working directory of existing scripts)",
            1,
            new String[][] {
                { "Project home", PreferenceConstants.GROOVY_SCRIPT_PROJECT_HOME  },
                { "Script location", PreferenceConstants.GROOVY_SCRIPT_SCRIPT_LOC },
                { "Eclipse home", PreferenceConstants.GROOVY_SCRIPT_ECLIPSE_HOME  }
            },
            getFieldEditorParent()));

        // legacy projects
        ConvertLegacyProject convert = new ConvertLegacyProject();
        final IProject[] oldProjects = convert.getAllOldProjects();
        if (oldProjects.length > 0) {
            Label l = new Label(getFieldEditorParent(), SWT.LEFT | SWT.WRAP);
            l.setText("The following legacy groovy projects exist in the workspace:\n");
            final List oldProjectsList = new List(getFieldEditorParent(), SWT.MULTI | SWT.V_SCROLL);
            populateProjectsList(oldProjectsList, oldProjects);

            l = new Label(getFieldEditorParent(), SWT.LEFT | SWT.WRAP);
            l.setText("Select the projects to convert.");

            Button convertButton = new Button(getFieldEditorParent(), SWT.PUSH);
            convertButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
            convertButton.setText("Convert");
            convertButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    convertSelectedProjects(oldProjectsList.getSelection());
                    populateProjectsList(oldProjectsList, new ConvertLegacyProject().getAllOldProjects());
                }
            });
        }
    }

    //--------------------------------------------------------------------------

    private void populateProjectsList(final List oldProjectsList, final IProject[] oldProjects) {
        final String[] projNames = new String[oldProjects.length];
        for (int i = 0; i < oldProjects.length; i++) {
            projNames[i] = oldProjects[i].getName();
        }
        oldProjectsList.setItems(projNames);
    }

    private void convertSelectedProjects(String[] selection) {
        if (selection.length == 0) {
            return;
        }
        IProject[] toConvert = new IProject[selection.length];
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        for (int i = 0; i < toConvert.length; i++) {
            toConvert[i] = root.getProject(selection[i]);
        }
        try {
            new ConvertLegacyProject().convertProjects(toConvert);
            StringBuffer sb = new StringBuffer();
            sb.append("The following projects have been converted:\n");
            for (String projName : selection) {
                sb.append(projName + "\n");
            }
            MessageDialog.openInformation(getShell(), "Successful conversion", sb.toString());
        } catch (Exception e) {
            // some kind of failure
            GroovyCore.logException("Failure when converting legacy projects", e);
            MessageDialog.openError(getShell(), "Error converting projects", "There has been an error converting the projects.  See the error log.");
        }
    }
}
