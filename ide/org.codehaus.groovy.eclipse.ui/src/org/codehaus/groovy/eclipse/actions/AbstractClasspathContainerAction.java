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

import java.text.MessageFormat;
import java.util.Optional;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;

public abstract class AbstractClasspathContainerAction implements IActionDelegate {

    protected final String containerName;
    protected final IPath  containerPath;
    protected IJavaProject targetProject;

    protected AbstractClasspathContainerAction(final String containerName, final IPath containerPath) {
        this.containerName = containerName;
        this.containerPath = containerPath;
    }

    @Override
    public void run(final IAction action) {
        try {
            Optional<IClasspathEntry> optEntry = GroovyRuntime.findClasspathEntry(targetProject, cpe -> cpe.getPath().matchingFirstSegments(containerPath) > 0);
            if (!optEntry.isPresent()) {
                IClasspathEntry newEntry = JavaCore.newContainerEntry(containerPath, ClasspathEntry.NO_ACCESS_RULES, JavaRuntime.isModularProject(targetProject)
                    ? new IClasspathAttribute[] {JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true")} : ClasspathEntry.NO_EXTRA_ATTRIBUTES, false);
                GroovyRuntime.appendClasspathEntry(targetProject, newEntry);
            } else {
                GroovyRuntime.removeClasspathEntry(targetProject, optEntry.get());
            }
        } catch (JavaModelException e) {
            GroovyCore.logException(MessageFormat.format("Failed to add or remove {0} classpath container", containerName), e);
        }
    }

    @Override
    public void selectionChanged(final IAction action, final ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            Object selected = ((IStructuredSelection) selection).getFirstElement();
            if (selected instanceof IProject) {
                IProject projSelected = (IProject) selected;
                if (GroovyNature.hasGroovyNature(projSelected)) {
                    targetProject = JavaCore.create(projSelected);
                }
            } else if (selected instanceof IJavaProject) {
                IJavaProject selectedProject = (IJavaProject) selected;
                if (GroovyNature.hasGroovyNature(selectedProject.getProject())) {
                    targetProject = selectedProject;
                }
            }
        }

        if (targetProject != null) {
            action.setEnabled(true);
            try {
                if (GroovyRuntime.findClasspathEntry(targetProject, cpe -> cpe.getPath().matchingFirstSegments(containerPath) > 0).isPresent()) {
                    action.setText(MessageFormat.format("Remove {0} from project", containerName));
                } else {
                    boolean isModular = (targetProject.getModuleDescription() != null);
                    action.setText(MessageFormat.format("Add {0} to {1,choice,0#classpath|1#modulepath}", containerName, isModular ? 1 : 0));
                }

                return;

            } catch (JavaModelException e) {
                GroovyCore.logException(MessageFormat.format("Failed to update {0} classpath container action", containerName), e);
            }
        }

        action.setEnabled(false);
        action.setText(MessageFormat.format("Cannot add or remove {0} classpath container", containerName));
    }
}
