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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * RemoveGroovyNatureAction is responsible for removing the Groovy nature to a java
 * project.
 * 
 * @author Andrew
 */
public class RemoveGroovyNatureAction implements IObjectActionDelegate {
    private List<IProject> currSelected = new LinkedList<IProject>();

    public void run(final IAction action) {
        
        if (currSelected != null && currSelected.size() > 0) {
            GroovyCore.trace("AddGroovySupportAction.run()");
            
            for (IProject project : currSelected) {
                GroovyCore.trace("   to " + project.getName());
                try {
					GroovyRuntime.removeGroovyNature(project);
				} catch (CoreException e) {
					GroovyCore.logException("Error removing Groovy nature", e);
				}
            }
        }
    }

    /**
     * @see IEditorActionDelegate#selectionChanged
     */
    public void selectionChanged(final IAction action, final ISelection selection) {
        currSelected.clear();
        List<IProject> newSelected = new LinkedList<IProject>();
        boolean enabled = true;
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection newSelection = (IStructuredSelection) selection;
            for (Iterator<?> iter = newSelection.iterator(); iter.hasNext();) {
                Object object = iter.next();
                if (object instanceof IAdaptable) {
                    IProject project = (IProject) ((IAdaptable)object).getAdapter(IProject.class);  
                    if(project != null) {
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
            this.currSelected = newSelected;
        }
    }

    /**
     * @see IEditorActionDelegate#setActivePart
     */
    public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
    }
}
