/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
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
