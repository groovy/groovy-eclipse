/*
 * Copyright 2009-2018 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Brings up a dialog to fix workspace compiler mismatches.
 */
public class FixCompilerMismatches implements IObjectActionDelegate {

    Shell activeShell;

    @Override
    public void run(IAction action) {
        IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        List<IProject> mismatchedProjects = new ArrayList<>(allProjects.length);
        for (IProject project : allProjects) {
            if (hasMismatch(project)) {
                mismatchedProjects.add(project);
            }
        }

        if (!mismatchedProjects.isEmpty()) {
            IProject[] toConvert = ListMessageDialog.openViewer(activeShell, mismatchedProjects.toArray(new IProject[0]));
            if (toConvert == null) {
                return;
            }
            for (IProject project : toConvert) {
                CompilerUtils.setCompilerLevel(project, CompilerUtils.getActiveGroovyVersion(), false);
            }
        } else {
            MessageDialog.openInformation(activeShell, "No mismatches found", "All projects have correct compiler settings.");
        }
    }

    private boolean hasMismatch(IProject project) {
        if (!project.isAccessible()) {
            return false;
        }
        try {
            IMarker[] markers = project.findMarkers(GroovyPlugin.COMPILER_MISMATCH_MARKER, true, IResource.DEPTH_ZERO);
            return markers != null && markers.length > 0;
        } catch (CoreException e) {
            GroovyCore.logException("Error finding markers", e);
            return false;
        }
    }

    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        if (targetPart != null) {
            activeShell = targetPart.getSite().getShell();
        } else {
            activeShell = null;
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
    }
}
