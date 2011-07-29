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
 * 
 * @author Nieraj Singh
 * @created 2011-05-13
 */
public class ProjectDropDownControl extends ProjectDisplayControl {

    private List<IProject> projects;

    private Combo dropDown;

    private ISelectionHandler handler;

    public ProjectDropDownControl(List<IProject> projects, Shell shell, Composite parent, ISelectionHandler handler) {
        super(shell, parent);
        this.projects = projects;
        this.handler = handler;
    }

    protected List<IProject> getProjects() {
        return projects;
    }

    @Override
    public void createProjectDisplayControl(Composite parent) {

        // If zero or one projects exit, delegate to super class to create
        // a single label to display the project
        if (projects == null || projects.isEmpty() || projects.size() == 1) {
            if (projects.size() == 1) {
                super.setProject(projects.get(0));
            }
            super.createProjectDisplayControl(parent);
            return;
        }

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

            @Override
            public void widgetSelected(SelectionEvent e) {
                String newSelection = dropDown.getItem(dropDown.getSelectionIndex());
                changeProject(newSelection);
            }
        });

        // Set the first project as the displayed selection
        changeProject(projects.get(0));

    }

    protected IProject changeProject(String projectName) {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        return changeProject(project);
    }

    protected IProject changeProject(IProject selectedProject) {
        if (selectedProject == null) {
            return null;
        }

        int selectedIndex = -1;
        String[] allProjects = dropDown.getItems();
        for (int i = 0; i < allProjects.length; i++) {
            if (selectedProject.getName().equals(allProjects[i])) {
                selectedIndex = i;
                break;
            }
        }

        if (selectedIndex >= 0) {

            setProject(selectedProject);

            dropDown.select(selectedIndex);

            handleProjectChange(selectedProject);
            return selectedProject;
        }

        return null;

    }

    protected int getIndex(String name) {
        if (dropDown == null || name == null) {
            return -1;
        }

        String[] items = dropDown.getItems();

        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                if (name.equals(items[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    protected boolean selectProject(IProject project) {
        if (dropDown == null || dropDown.isDisposed() || project == null) {
            return false;
        }

        IProject currentProject = getProject();

        if (currentProject == null || currentProject.getName().equals(project.getName())) {
            return false;
        }
        return changeProject(project) != null;
    }

    @Override
    public void setProject(IProject project) {
        super.setProject(project);
        selectProject(project);
    }

    protected void handleProjectChange(IProject selectedProject) {
        if (handler != null) {
            handler.selectionChanged(selectedProject);
        }
    }

}
