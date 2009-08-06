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
import org.codehaus.groovy.eclipse.core.builder.GroovyClasspathContainer;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class ImportLibs implements IObjectActionDelegate {
    private ISelection selection;

    public ImportLibs() {}

    public void setActivePart(final IAction action,
            final IWorkbenchPart targetPart) {}

    public void run(final IAction action) {
        GroovyCore.trace("AddGroovySupportAction.run()");
        final Object selected = ((IStructuredSelection) selection)
                .getFirstElement();
        if (!(selected instanceof IProject)
                && !(selected instanceof IJavaProject))
            return;
        final IProject targetProject = selected instanceof IProject ? (IProject) selected
                : ((IJavaProject) selected).getProject();
        try {
            // If the Groovy Nature has yet to be added... no use doing this...
            if (!targetProject.hasNature(GroovyNature.GROOVY_NATURE))
                return;
            IClasspathEntry containerEntry = JavaCore
                    .newContainerEntry(GroovyClasspathContainer.CONTAINER_ID);
            GroovyRuntime.addClassPathEntry(JavaCore.create(targetProject),
                    containerEntry);
        } catch (final CoreException e) {
            GroovyCore.logException("failed to import groovy library jars", e);
        }
    }

    public void selectionChanged(final IAction action,
            final ISelection selection) {
        // StructuredSelection.elements[0] should be a JavaProject
        this.selection = selection;
    }
}
