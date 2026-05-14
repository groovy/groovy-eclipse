/*
 * Copyright 2009-2020 the original author or authors.
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
package org.codehaus.groovy.eclipse.actions;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class RemoveGroovyNatureAction implements IObjectActionDelegate {

    private List<IProject> selected;

    private IWorkbenchPart targetPart;

    private boolean shouldAskToRemoveJars = true;

    @Override
    public void run(final IAction action) {
        if (selected != null && !selected.isEmpty()) {
            GroovyCore.trace("RemoveGroovyNatureAction.run()");
            for (IProject project : selected) {
                GroovyCore.trace("    from " + project.getName());
                try {
                    GroovyRuntime.removeGroovyNature(project);
                    IJavaProject javaProject = JavaCore.create(project);
                    if (GroovyRuntime.hasGroovyClasspathContainer(javaProject)) {
                        boolean shouldRemove;
                        if (shouldAskToRemoveJars) {
                            shouldRemove = MessageDialog.openQuestion(getShell(), "Remove Groovy jars?", "Do you want to also remove the groovy runtime jars from project " + project.getName() + "?");
                        } else {
                            shouldRemove = true; // do automatically during testing
                        }
                        if (shouldRemove) {
                            GroovyRuntime.removeGroovyClasspathContainer(javaProject);
                        }
                    }
                    GroovyRuntime.findClasspathEntry(javaProject, cpe -> GroovyRuntime.DSLD_CONTAINER_ID.equals(cpe.getPath().segment(0))).ifPresent(cpe -> GroovyRuntime.removeClasspathEntry(javaProject, cpe));
                } catch (CoreException e) {
                    GroovyCore.logException("Error removing Groovy nature", e);
                }
            }
        }
    }

    private Shell getShell() {
        return targetPart != null ? targetPart.getSite().getShell() : Display.getDefault().getActiveShell();
    }

    @Override
    public void selectionChanged(final IAction action, final ISelection selection) {
        selected = null;

        List<IProject> newSelected = new LinkedList<>();
        boolean enabled = true;
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection newSelection = (IStructuredSelection) selection;
            for (Iterator<?> iter = newSelection.iterator(); iter.hasNext();) {
                Object object = iter.next();
                if (object instanceof IAdaptable) {
                    IProject project = Adapters.adapt(object, IProject.class);
                    if (project != null) {
                        newSelected.add(project);
                    } else {
                        enabled = false;
                        break;
                    }
                } else {
                    enabled = false;
                    break;
                }
            }
            if (action != null) {
                action.setEnabled(enabled);
            }
        }

        if (enabled) {
            this.selected = newSelected;
        }
    }

    @Override
    public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
        this.targetPart = targetPart;
    }

    //@VisibleForTesting
    public void doNotAskToRemoveJars() {
        this.shouldAskToRemoveJars = false;
    }
}
