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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;

public class AddGroovyNatureAction implements IActionDelegate {

    private List<IProject> selected;

    @Override
    public void run(final IAction action) {
        if (selected != null && !selected.isEmpty()) {
            GroovyCore.trace("AddGroovyNatureAction.run()");
            for (IProject project : selected) {
                GroovyCore.trace("    to " + project.getName());
                GroovyRuntime.addGroovyRuntime(project);
            }
        }
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
}
