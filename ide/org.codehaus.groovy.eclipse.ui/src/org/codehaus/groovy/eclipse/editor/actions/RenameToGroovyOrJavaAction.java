/**********************************************************************
 * Copyright (c) 2004, 2009  IBM Corporation, SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Sian January - initial version
 *               Andrew Eisenberg - conversion to groovy
 **********************************************************************/
package org.codehaus.groovy.eclipse.editor.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ltk.core.refactoring.resource.RenameResourceChange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;

/**
 * Rename the file extension of a file to groovy or to java
 */
public abstract class RenameToGroovyOrJavaAction implements IActionDelegate {

	public static final String GROOVY = ".groovy";
	public static final String JAVA = ".java";
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
		if (selection instanceof StructuredSelection) {
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				@SuppressWarnings("unchecked")
                public void run(IProgressMonitor monitor) {
					StructuredSelection sel = (StructuredSelection) selection;
					for (Iterator iter = sel.iterator(); iter.hasNext();) {
						Object object = iter.next();
						if (object instanceof IAdaptable) {

							IResource file = (IResource) ((IAdaptable) object)
									.getAdapter(IResource.class);
							if (file != null) {
								String name = file.getName();
								name = name.substring(0, name.lastIndexOf('.')); 
								RenameResourceChange change = new RenameResourceChange(
										file.getFullPath(), name + javaOrGroovy); //$NON-NLS-1$
								try {
									change.perform(monitor);
								} catch (CoreException e) {
									GroovyCore.logException("Error converting file extension to " + 
									        javaOrGroovy + " for file " + file.getName(), e);
								}
							}
						}
					}
				}
			};

			IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(
					runnable);
			try {
				new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op);
			} catch (InvocationTargetException e) {
			} catch (InterruptedException e) {
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}