/**********************************************************************
 * Copyright (c) 2004, 2010  IBM Corporation, SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Sian January - initial version
 *               Andrew Eisenberg - conversion to groovy
 **********************************************************************/
package org.codehaus.groovy.eclipse.editor.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.eclipse.ui.utils.GroovyResourceUtil;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Rename the file extension of a file to groovy or to java
 */
public abstract class RenameToGroovyOrJavaAction implements IWorkbenchWindowActionDelegate {

    private ISelection selection;

    private String javaOrGroovy;

    public RenameToGroovyOrJavaAction(String javaOrGroovy) {
        this.javaOrGroovy = javaOrGroovy;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        if (selection instanceof IStructuredSelection) {
            GroovyResourceUtil.renameFile(javaOrGroovy, getResources((IStructuredSelection) selection));
        }
    }

    /**
     *
     * @return non-null list of resources in the context selection. May be empty
     */
    protected List<IResource> getResources(IStructuredSelection selection) {
        List<IResource> resources = new ArrayList<IResource>();
        if (selection != null) {
            for (Iterator<?> iter = selection.iterator(); iter.hasNext();) {
                Object object = iter.next();
                if (object instanceof IAdaptable) {

                    IResource file = (IResource) ((IAdaptable) object).getAdapter(IResource.class);

                    if (file != null) {
                        if (file.getType() != IResource.FILE) {
                            continue;
                        }
                        resources.add(file);
                    }
                }
            }
        }
        return resources;
    }


    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action
     * .IAction,
     * org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = selection;
    }

    public void dispose() {
        selection = null;
    }

    public void init(IWorkbenchWindow window) {}

}