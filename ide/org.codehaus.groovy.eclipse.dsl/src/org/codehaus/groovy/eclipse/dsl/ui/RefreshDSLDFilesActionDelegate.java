/*******************************************************************************
 * Copyright (c) 2011 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Andrew Eisenberg - Initial implemenation
 *******************************************************************************/
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
    
    public void run(IAction action) {
        GroovyDSLCoreActivator.getDefault().getContextStoreManager().initialize(groovyProjects, false);
    }

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

    public void dispose() { 
        groovyProjects = null;
    }

    public void init(IWorkbenchWindow window) {
    }

}
