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
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.ui;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * Creates either a combo for multiple project selections, or a label control
 * with just one project selection.
 * If a combo is create, the first project in the input list will be set as the
 * default selection. New selections
 * can be set via setProject(..). Selection changes trigger a selection change
 * event that can be handled by registered listeners.
 * 
 * @author Nieraj Singh
 * @created 2011-05-13
 */
public class ProjectDropDownControl extends ProjectDisplayControl {

    private List<IProject> projects;

    private Combo dropDown;

    private ISelectionHandler handler;

    /**
     * None of the arguments, except the handler can be null
     */
    protected ProjectDropDownControl(List<IProject> projects, Shell shell, Composite parent, ISelectionHandler handler) {
        super(shell, parent);
        this.projects = projects;
        this.handler = handler;
    }

    protected List<IProject> getProjects() {
        return projects;
    }

    public static IProjectUIControl getProjectSelectionControl(List<IProject> projects, Shell shell, Composite parent,
            ISelectionHandler handler) {
        // If zero or one projects exit, delegate to super class to create
        // a single label to display the project
        IProjectUIControl control = null;
        if (projects == null || projects.size() <= 1) {
            control = new ProjectDisplayControl(shell, parent);
            if (projects.size() == 1) {
                control.setProject(projects.get(0));
            }

        } else {
            control = new ProjectDropDownControl(projects, shell, parent, handler);
        }
        return control;
    }

    public void createProjectDisplayControl(Composite parent) {

        // Otherwise create the drop down
        String[] projectNames = new String[projects.size()];

        int i = 0;
        while (i < projectNames.length && i < projects.size()) {
            projectNames[i] = projects.get(i).getName();
            i++;
        }

        dropDown = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        dropDown.setItems(projectNames);

        dropDown.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                String newSelection = dropDown.getItem(dropDown.getSelectionIndex());
                IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(newSelection);
                ProjectDropDownControl.super.setProject(project);
                handleProjectChange(project);
            }
        });

        // Set the first project as the displayed selection
        setProject(projects.get(0));

    }

    /**
     * True iff both the current selection and the project to select are the
     * same. False any other case.
     * 
     * @param projectToSelect
     * @return
     */
    protected boolean isSelectionSame(IProject projectToSelect) {

        int selectionIndex = dropDown.getSelectionIndex();
        if (selectionIndex >= 0) {
            String currentSelection = dropDown.getItem(selectionIndex);
            return projectToSelect.getName().equals(currentSelection);
        }
        return projectToSelect == getProject();
    }

    /**
     * Return the selected project, or null if the project is not available in
     * the selection and cannot be selected.
     * 
     * @param projectToSelect
     * @return
     */
    public IProject setProject(IProject projectToSelect) {
        if (projectToSelect == null || isSelectionSame(projectToSelect)) {
            return projectToSelect;
        }

        int selectedIndex = -1;
        String[] allProjects = dropDown.getItems();
        for (int i = 0; i < allProjects.length; i++) {
            if (projectToSelect.getName().equals(allProjects[i])) {
                selectedIndex = i;
                break;
            }
        }

        if (selectedIndex >= 0) {

            dropDown.select(selectedIndex);
            super.setProject(projectToSelect);
            handleProjectChange(projectToSelect);

            return projectToSelect;
        } else {
            return null;
        }

    }

    protected void handleProjectChange(IProject selectedProject) {

        if (handler != null) {
            handler.selectionChanged(selectedProject);
        }
    }

}
