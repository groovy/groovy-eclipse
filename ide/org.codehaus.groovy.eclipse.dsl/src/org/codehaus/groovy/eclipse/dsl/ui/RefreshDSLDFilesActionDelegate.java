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

import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class RefreshDSLDFilesActionDelegate implements IWorkbenchWindowActionDelegate {

    private IProject[] groovyProjects;

    @Override
    public void run(IAction action) {
        GroovyDSLCoreActivator.getDefault().getContextStoreManager().initialize(groovyProjects, false);
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            Object[] elts = ss.toArray();
            groovyProjects = new IProject[elts.length];
            for (int i = 0; i < elts.length; i++) {
                if (elts[i] instanceof IProject &&
                        GroovyNature.hasGroovyNature((IProject) elts[i])) {
                    groovyProjects[i] = (IProject) elts[i];
                } else {
                    // invalid selection
                    groovyProjects = null;
                    return;
                }
            }
        } else {
            groovyProjects = null;
        }
    }

    @Override
    public void dispose() {
        groovyProjects = null;
    }

    @Override
    public void init(IWorkbenchWindow window) {
    }
}
