/**********************************************************************
 * Copyright (c) 2002 - 2009 IBM Corporation, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * 		Adrian Colyer, Andy Clement, Tracy Gardner - initial version
 * 		Ian McGrath - added ability to use existing project structures
 * 		Sian January - updated to look like 3.0 new Java Project wizard
 *      Helen Hawkins - updated for new ajde interface (bug 148190) (no
 *                      longer need to set the current project)
 *      Andrew Eisenberg - Adapted for Groovy
**********************************************************************/

package org.codehaus.groovy.eclipse.wizards;


import java.lang.reflect.InvocationTargetException;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.GroovyPluginImages;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageTwo;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

/**
 * Wizard to create a new Groovy project
 */
public class GroovyProjectWizard extends NewElementWizard implements IExecutableExtension {
    
    protected NewJavaProjectWizardPageOne fFirstPage;
    protected NewJavaProjectWizardPageTwo fSecondPage;
    
    private IConfigurationElement fConfigElement;
    
    public GroovyProjectWizard() {
		setDefaultPageImageDescriptor(GroovyPluginImages.DESC_NEW_GROOVY_PROJECT);
		setDialogSettings(GroovyPlugin.getDefault().getDialogSettings());
		setWindowTitle(NewWizardMessages.GroovyProjectWizard_NewGroovyProject);
   }

    /*
     * @see Wizard#addPages
     */	
    public void addPages() {
        super.addPages();
        fFirstPage= new NewJavaProjectWizardPageOne();
        addPage(fFirstPage);
        fFirstPage.setTitle(NewWizardMessages.GroovyProjectWizard_CreateGroovyProject);
		fFirstPage.setDescription(NewWizardMessages.GroovyProjectWizard_CreateGroovyProjectDesc);
		fSecondPage= new NewJavaProjectWizardPageTwo(fFirstPage);
        fSecondPage.setTitle(NewWizardMessages.GroovyProjectWizard_BuildSettings);
        fSecondPage.setDescription(NewWizardMessages.GroovyProjectWizard_BuildSettingsDesc);
        addPage(fSecondPage);
    }		
    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.ui.wizards.NewElementWizard#finishPage(org.eclipse.core.runtime.IProgressMonitor)
     */
    protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
    	fSecondPage.performFinish(monitor); // use the full progress monitor
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		boolean res= super.performFinish();
		if (res) {
			// Fix for 78263
	        BasicNewProjectResourceWizard.updatePerspective(fConfigElement);
	 		IProject project = fSecondPage.getJavaProject().getProject();
	 		selectAndReveal(project);
			boolean completed = finalizeNewProject(project);
			res = completed;
		}
		return res;
	}
    
    protected void handleFinishException(Shell shell, InvocationTargetException e) {
        String title= NewWizardMessages.GroovyProjectWizard_OpErrorTitle; 
        String message= NewWizardMessages.GroovyProjectWizard_OpErrorCreateMessage; 
        ExceptionHandler.handle(e, getShell(), title, message);
    }	

    
	/**
	 * Builds and adds the necessary properties to the new project and updates the workspace view
	 */
	private boolean finalizeNewProject(IProject project) {
		
        // Bugzilla 46271
        // Force a build of the new Groovy project using the Java builder
        // so that project state can be created. The creation of project 
        // state means that Java projects can reference this project on their
        // build path and successfully continue to build.

		final IProject thisProject = project;
		try {
		    GroovyRuntime.addGroovyRuntime(thisProject);
		    thisProject.build(IncrementalProjectBuilder.FULL_BUILD, null);
        } catch (CoreException e) {
        }

		project = thisProject;
		selectAndReveal(project);
        GroovyCore.trace("New project created: " + thisProject.getName()); //$NON-NLS-1$
		return true;
	}


   
    /*
     * Stores the configuration element for the wizard.  The config element will be used
     * in <code>performFinish</code> to set the result perspective.
     */
    public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
        fConfigElement= cfig;
    }
    
    /* (non-Javadoc)
     * @see IWizard#performCancel()
     */
    public boolean performCancel() {
        fSecondPage.performCancel();
        return super.performCancel();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#canFinish()
     */
    public boolean canFinish() {
        return super.canFinish();
    }

	public IJavaElement getCreatedElement() {
		return fSecondPage.getJavaProject();
	}
}