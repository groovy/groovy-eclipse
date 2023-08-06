/*
 * Copyright 2009-2023 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.ui;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;

public class AbstractCheckerAction {

    private ISelection selection;
    protected Shell shell;

    protected List<IResource> findSelection() {
        List<IResource> currentSelection;
        if (!(selection instanceof IStructuredSelection)) {
            currentSelection = null;
        }
        List<?> elts = ((IStructuredSelection) selection).toList();
        currentSelection = new ArrayList<>(elts.size());
        for (Object elt : elts) {
            IResource candidate = null;
            if (elt instanceof IResource) {
                candidate = (IResource) elt;
            } else if (elt instanceof IAdaptable) {
                candidate = Adapters.adapt(elt, IResource.class);
            }

            if (candidate != null && GroovyNature.hasGroovyNature(candidate.getProject())) {
                currentSelection.add(candidate);
            }
        }
        if (currentSelection.isEmpty()) {
            currentSelection = null;
        }
        return currentSelection;
    }

    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = selection;
    }

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        try {
            IWorkbenchPartSite site = targetPart.getSite();
            if (site != null) {
                shell = site.getShell();
                return;
            }
        } catch (Exception ignore) {
            // must be initializing or de-initializing
        }
        shell = null;
    }
}
