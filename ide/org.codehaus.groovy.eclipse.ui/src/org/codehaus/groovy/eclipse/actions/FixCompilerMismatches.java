/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.actions;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.codehaus.groovy.eclipse.wizards.ListMessageDialog;
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
 * Brings up a dialog to fix workspace compiler mismatches
 * @author Andrew Eisenberg
 * @created 2012-12-31
 */
public class FixCompilerMismatches implements IObjectActionDelegate {
    Shell activeShell = null;

    public void run(IAction action) {
        IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        List<IProject> mismatchedProjects = new ArrayList<IProject>(allProjects.length);
        for (IProject project : allProjects) {
            if (hasMismatch(project)) {
                mismatchedProjects.add(project);
            }
        }

        if (mismatchedProjects.size() > 0) {
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

    public void selectionChanged(IAction action, ISelection selection) {
        // noop
    }

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        if (targetPart != null) {
            activeShell = targetPart.getSite().getShell();
        } else {
            activeShell = null;
        }
    }

}
