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

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-05-13
 */
public class ProjectDisplayControl implements IProjectUIControl {

    private Shell shell;

    private Composite parent;

    private IProject project;

    protected static final String NO_PROJECT = "No Groovy project available.";

    protected ProjectDisplayControl(Shell shell, Composite parent) {
        this.shell = shell;
        this.parent = parent;
    }

    protected Shell getShell() {
        return shell;
    }

    public IProject getProject() {
        return project;
    }

    public Control createControls() {

        Composite projectComposite = new Composite(parent, SWT.NONE);

        GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(projectComposite);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(projectComposite);

        Label projectLabel = new Label(projectComposite, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).applyTo(projectLabel);
        projectLabel.setText("Project: ");

        createProjectDisplayControl(projectComposite);
        return projectComposite;
    }

    protected void createProjectDisplayControl(Composite parent) {
        Label projectLabel = new Label(parent, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).applyTo(projectLabel);
        String labelVal = project != null ? project.getName() : NO_PROJECT;
        projectLabel.setText(labelVal);

    }

    public IProject setProject(IProject project) {
        this.project = project;
        return project;
    }

}
