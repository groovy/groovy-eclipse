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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.GroovyCoreActivator;
import org.codehaus.groovy.eclipse.core.builder.ConvertLegacyProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * This Action converts a Project that uses the old Groovy 1.x Nature to a project that uses the 2.x nature. 
 * 
 * @author Andrew
 */
public class ConvertLegacyProjectAction implements IObjectActionDelegate {
    private IProject[] projects;
    private Shell currentShell;
    

    public void run(final IAction action) {
        if (projects != null && projects.length > 0) {
            ConvertLegacyProject convert = new ConvertLegacyProject();
            try {
                convert.convertProjects(projects);
                
                // success if no exception thrown
                MessageDialog.openInformation(
                        (currentShell != null ? currentShell : Display.getCurrent().getActiveShell()), 
                        "Successful conversion",
                        "Conversion to new groovy plugin successful!\n" +
                        "It is recommended to delete the \"bin-groovy\" folder.");
                
            } catch (Exception e) {
                StringBuffer sb = new StringBuffer();
                for (IProject project : projects) {
                    sb.append(project.getName() + " ");
                    
                }
                String message = "Failed to convert projects: " + sb;
                GroovyCore.logException(message, e);
                IStatus reason = new Status(IStatus.ERROR, GroovyCoreActivator.PLUGIN_ID, message, e);
                ErrorDialog.openError(
                        (currentShell != null ? currentShell : Display.getCurrent().getActiveShell()), 
                        "Could not convert projects", "Could not convert projects.  Reason:", reason);
            }
        }
    }

    /**
     * @see IEditorActionDelegate#selectionChanged
     */
    public void selectionChanged(final IAction action, final ISelection selection) {
        List<IProject> newSelected = new LinkedList<IProject>();
        boolean enabled = true;
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection newSelection = (IStructuredSelection) selection;
            for (Iterator<?> iter = newSelection.iterator(); iter.hasNext();) {
                Object object = iter.next();
                if (object instanceof IAdaptable) {
                    IProject project = (IProject) ((IAdaptable)object).getAdapter(IProject.class);  
                    try {
                        if(project != null  && project.hasNature(ConvertLegacyProject.OLD_NATURE)) {
                            newSelected.add(project);
                        } else {
                            enabled = false;
                            break;
                        }
                    } catch (CoreException e) {
                        GroovyCore.logException("Error finding project nature for " + project.getName(), e);
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
            this.projects = newSelected.toArray(new IProject[0]);
        } else {
            this.projects = null;
        }
    }

    /**
     * @see IEditorActionDelegate#setActivePart
     */
    public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
        try { 
            currentShell = targetPart.getSite().getShell();
        } catch (Exception e) {
            currentShell = null;
        }
    }
}
