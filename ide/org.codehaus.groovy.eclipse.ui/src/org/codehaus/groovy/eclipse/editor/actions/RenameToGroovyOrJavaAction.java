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


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ltk.core.refactoring.resource.RenameResourceChange;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.UIJob;

/**
 * Rename the file extension of a file to groovy or to java
 */
public abstract class RenameToGroovyOrJavaAction implements IWorkbenchWindowActionDelegate {

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
		    UIJob renameTo = new UIJob("Rename to " + javaOrGroovy) {
                
                @Override
                public IStatus runInUIThread(IProgressMonitor monitor) {
                    Set<IProject> affectedProjects = new HashSet<IProject>();
                    final Set<IResource> filesAlreadyOpened = new HashSet<IResource>();
                    StructuredSelection sel = (StructuredSelection) selection;
                    for (Iterator iter = sel.iterator(); iter.hasNext();) {
                        Object object = iter.next();
                        if (object instanceof IAdaptable) {

                            IResource file = (IResource) ((IAdaptable) object)
                                    .getAdapter(IResource.class);
                            
                            if (file != null) {
                                if (file.getType() != IResource.FILE) {
                                    continue;
                                }
                                IDE.saveAllEditors(new IFile[] { (IFile) file }, true);
                                String name = convertName(file);
                                RenameResourceChange change = new RenameResourceChange(
                                        file.getFullPath(), name); //$NON-NLS-1$
                                try {
                                    if (isOpenInEditor(file)) {
                                        filesAlreadyOpened.add(file);
                                    }
                                        
                                    change.perform(monitor);
                                    
                                    IProject project = file.getProject();
                                    if (!GroovyNature.hasGroovyNature(project)) {
                                        affectedProjects.add(project);
                                    }
                                } catch (CoreException e) {
                                    String message = "Error converting file extension to " + 
                                            javaOrGroovy + " for file " + file.getName();
                                    GroovyCore.logException(message, e);
                                    return new Status(IStatus.ERROR, GroovyPlugin.PLUGIN_ID, message, e);
                                }
                            }
                        }
                    }
                    
                    if (! affectedProjects.isEmpty() && javaOrGroovy.equals(GROOVY)) {
                        askToConvert(affectedProjects, this.getDisplay().getActiveShell());
                    }
                    
                    reopenFiles(filesAlreadyOpened);
                    return Status.OK_STATUS;
                }
            };
            renameTo.schedule();
		}
	}

    /**
     * @param file
     * @return
     */
    protected boolean isOpenInEditor(IResource file) {
        try {
            IEditorReference[] refs = getWorkbenchPage().getEditorReferences();
            for (IEditorReference ref : refs) {
                try {
                    if (ref.getEditorInput() instanceof IFileEditorInput) {
                        IFileEditorInput input = (IFileEditorInput) ref.getEditorInput();
                        if (input.getFile().equals(file)) {
                            return true;
                        }
                    }
                } catch (PartInitException e) {
                    GroovyCore.logException("Exception looking through opened editors", e);
                }
            }
        } catch (NullPointerException npe) {
            // workbench is shutting down, editors cannot be reached
            // ok to ignore.
        } catch (IndexOutOfBoundsException e) {
            // no open workbench windows.  OK to ignore
        }
        return false;
    }

    /**
     * @return
     */
    private IWorkbenchPage getWorkbenchPage() {
        return PlatformUI.getWorkbench().getWorkbenchWindows()[0].getActivePage();
    }

    /**
     * @param file
     * @return
     */
    protected String convertName(IResource file) {
        String name = file.getName();
        name = name.substring(0, name.lastIndexOf('.'));
        name = name + javaOrGroovy;
        return name;
    }

    /**
	 * Reopen files that were closed
     * @param filesAlreadyOpened
     */
    protected void reopenFiles(Set<IResource> filesAlreadyOpened) {
        for (IResource origResource : filesAlreadyOpened) {
            String name = convertName(origResource);
            IFile newFile = origResource.getParent().getFile(new Path(name));
            try {
                IDE.openEditor(getWorkbenchPage(), newFile);
            } catch (PartInitException e) {
                GroovyCore.logException("Exception thrown when opening " + name + " in an editor", e);
            }
        }
    }

    /**
     * @param affectedProjects
     * @param shell 
     */
    protected void askToConvert(Set<IProject> affectedProjects, Shell shell) {
        // no op unless converting to groovy
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

    public void dispose() {
        selection = null;
    }

    public void init(IWorkbenchWindow window) {
    }

}