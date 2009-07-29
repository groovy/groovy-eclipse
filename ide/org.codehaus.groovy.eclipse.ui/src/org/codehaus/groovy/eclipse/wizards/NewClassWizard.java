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
package org.codehaus.groovy.eclipse.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.jface.wizard.Wizard;

/**
 * @see Wizard
 */
public class NewClassWizard extends NewElementWizard  {
	private NewClassWizardPage fPage;
	
	public NewClassWizard() {
		super();
		setDefaultPageImageDescriptor(JavaPluginImages.DESC_WIZBAN_NEWCLASS);
		setDialogSettings(JavaPlugin.getDefault().getDialogSettings());
		
		setWindowTitle("Create a new Groovy class"); 
	}

	/*
	 * @see Wizard#createPages
	 */	
	public void addPages() {
		super.addPages();
		fPage= new NewClassWizardPage();
		addPage(fPage);
		fPage.init(getSelection());
	}	


    /*(non-Javadoc)
     * @see org.eclipse.jdt.internal.ui.wizards.NewElementWizard#canRunForked()
     */
	protected boolean canRunForked() {
        return !fPage.isEnclosingTypeSelected();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.ui.wizards.NewElementWizard#finishPage(org.eclipse.core.runtime.IProgressMonitor)
     */
    protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
        fPage.createType(monitor); // use the full progress monitor
    }
        
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#performFinish()
     */
    public boolean performFinish() {
        warnAboutTypeCommentDeprecation();
        boolean res= super.performFinish();
        if (res) {
            IResource resource= fPage.getModifiedResource();
            if (resource != null) {
                selectAndReveal(resource);
                openResource((IFile) resource);
            }   
        }
        return res;
    }


	public IJavaElement getCreatedElement() {
		return fPage.getCreatedType();
	}
}
