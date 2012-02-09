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
package org.codehaus.groovy.eclipse.actions;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public abstract class AbstractAddClasspathContainerAction implements IObjectActionDelegate {
    protected IJavaProject targetProject;

    public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {}

    public void run(final IAction action) {
        try {
            if (GroovyRuntime.hasClasspathContainer(targetProject, getClasspathContainerPath())) {
                GroovyRuntime.removeLibraryFromClasspath(targetProject, getClasspathContainerPath());
            } else {
                GroovyRuntime.addLibraryToClasspath(targetProject, getClasspathContainerPath(), exportClasspath());
            }
        } catch (final CoreException e) {
            GroovyCore.logException(errorMessage(), e);
        }
    }

    public void selectionChanged(final IAction action, final ISelection selection) {
        targetProject = getTargetProject(selection);
        if (targetProject == null) {
            action.setEnabled(false);
            action.setText(disabledText());
        } else {
            action.setEnabled(true);
            try {
                if (GroovyRuntime.hasClasspathContainer(targetProject, getClasspathContainerPath())) {
                    action.setText(removeText());
                } else {
                    action.setText(addText());
                }
            } catch (CoreException e) {
                GroovyCore.logException(errorMessage(), e);
                action.setEnabled(false);
                action.setText(disabledText());
            }
        }
    }

    private IJavaProject getTargetProject(ISelection selection) {
        IJavaProject candidate = null;
        if (selection instanceof IStructuredSelection) {
            final Object selected = ((IStructuredSelection) selection).getFirstElement();
            if (selected instanceof IProject) {
                IProject projSelected = (IProject) selected;
                if (GroovyNature.hasGroovyNature(projSelected)) {
                    candidate = JavaCore.create(projSelected);
                }
            } else if (selected instanceof IJavaProject) {
                IJavaProject projSelected = (IJavaProject) selected;
                if (GroovyNature.hasGroovyNature(projSelected.getProject())) {
                    candidate = projSelected;
                }
            }
        }
        return candidate;
    }

    protected abstract IPath getClasspathContainerPath();

    protected abstract boolean exportClasspath();

    protected abstract String errorMessage();

    protected abstract String disabledText();

    protected abstract String addText();

    protected abstract String removeText();

}
