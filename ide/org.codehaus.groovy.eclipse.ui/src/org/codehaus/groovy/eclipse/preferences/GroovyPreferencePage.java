/*
 * Copyright 2009-2019 the original author or authors.
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

import java.util.Arrays;
import java.util.stream.Collectors;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class GroovyPreferencePage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage {

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
        // default launch location for scripts
        addField(new RadioGroupFieldEditor(PreferenceConstants.GROOVY_SCRIPT_DEFAULT_WORKING_DIRECTORY,
            "Default working directory for running Groovy scripts\n  (will not change the working directory of existing scripts)",
            1,
            new String[][] {
                {"Eclipse home", PreferenceConstants.GROOVY_SCRIPT_ECLIPSE_HOME},
                {"Project home", PreferenceConstants.GROOVY_SCRIPT_PROJECT_HOME},
                {"Script location", PreferenceConstants.GROOVY_SCRIPT_SCRIPT_LOC},
            },
            getFieldEditorParent()));

        new Label(getFieldEditorParent(), SWT.NONE).setText("");

        // JUnit monospace typeface
        addField(new BooleanFieldEditor(PreferenceConstants.GROOVY_JUNIT_MONOSPACE_FONT,
            "&Use monospace font for JUnit (deprecated)", getFieldEditorParent()));
        new Label(getFieldEditorParent(), SWT.LEFT | SWT.WRAP).setText(
            "  This is particularly useful for testing with the assert keyword");

        // legacy projects
        IProject[] oldProjects = new ConvertLegacyProject().getAllOldProjects();
        if (oldProjects.length > 0) {
            new Label(getFieldEditorParent(), SWT.NONE).setText("");

            Label label = new Label(getFieldEditorParent(), SWT.LEFT | SWT.WRAP);
            label.setText("The following legacy groovy projects exist in the workspace:\n");
            List oldProjectsList = new List(getFieldEditorParent(), SWT.MULTI | SWT.V_SCROLL);
            oldProjectsList.setItems(Arrays.stream(oldProjects).map(IProject::getName).toArray(String[]::new));

            label = new Label(getFieldEditorParent(), SWT.LEFT | SWT.WRAP);
            label.setText("Select the projects to convert.");

            Button button = new Button(getFieldEditorParent(), SWT.PUSH);
            button.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
            button.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    convertSelectedProjects(oldProjectsList.getSelection());
                    IProject[] oldProjects = new ConvertLegacyProject().getAllOldProjects();
                    oldProjectsList.setItems(Arrays.stream(oldProjects).map(IProject::getName).toArray(String[]::new));
                }
            });
            button.setText("Convert");
        }
    }

    private void convertSelectedProjects(String[] selection) {
        if (selection.length == 0) {
            return;
        }
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject[] projects = Arrays.stream(selection).map(s -> root.getProject(s)).toArray(IProject[]::new);
        try {
            new ConvertLegacyProject().convertProjects(projects);
            MessageDialog.openInformation(getShell(), "Successful conversion",
                "The following projects have been converted:\n" + Arrays.stream(selection).collect(Collectors.joining("\n")));
        } catch (Exception e) {
            GroovyCore.logException("Failure when converting legacy projects", e);
            MessageDialog.openError(getShell(), "Error converting projects", "There has been an error converting the projects.  See the error log.");
        }
    }
}
