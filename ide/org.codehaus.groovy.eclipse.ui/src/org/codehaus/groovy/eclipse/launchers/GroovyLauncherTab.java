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
/*
 * Created on 17-Dec-2003
 */
package org.codehaus.groovy.eclipse.launchers;

import java.util.List;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;
import org.eclipse.jdt.internal.ui.viewsupport.JavaUILabelProvider;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.dialogs.ListDialog;

/**
 * Dialog for selecting the groovy class to run.
 *
 * @author MelamedZ
 */
public class GroovyLauncherTab extends JavaMainTab {

	/**
	 * Dialog for selecting the groovy class to run.
	 */
	protected void handleSearchButtonSelected() {
		IJavaProject javaProject = getJavaProject();
		/*
		 * Note that the set of available classes may be zero and hence the
         * dialog will obviously not display any classes; in which case the
		 * project needs to be compiled.
		 */
		try {
            final List<IType> availableClasses = new GroovyProjectFacade(javaProject).findAllRunnableTypes();
            if (availableClasses.size() == 0) {
            	MessageDialog.openWarning(getShell(), "No Groovy classes to run",
            			"There are no compiled groovy classes to run in this project");
            	return;
            }
            ListDialog dialog = new ListDialog(getShell());
            dialog.setBlockOnOpen(true);
            dialog.setMessage("Select a Groovy class to run");
            dialog.setTitle("Choose Groovy Class");
            dialog.setContentProvider(new ArrayContentProvider());
            dialog.setLabelProvider(new JavaUILabelProvider());
            dialog.setInput(availableClasses.toArray(new IType[availableClasses.size()]));
            if (dialog.open() == Window.CANCEL) {
            	return;
            }
            
            Object[] results = dialog.getResult();
            if (results == null || results.length == 0){
            	return;
            }
            if (results[0] instanceof IType) {
                fMainText.setText(((IType) results[0]).getFullyQualifiedName());
            }
            
        } catch (JavaModelException e) {
            GroovyCore.logException("Exception when launching " + javaProject, e);
        }
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return "Groovy Main"; //$NON-NLS-1$
	}	
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		super.activated(workingCopy);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 * The buttons are private in Eclipse 3.2
	public void createControl(Composite parent) {
		super.createControl(parent);
		fSearchExternalJarsCheckButton.setVisible(false);
		fStopInMainCheckButton.setVisible(false);
	}*/

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
     */
    public void setDefaults(ILaunchConfigurationWorkingCopy config) {
        super.setDefaults(config);
        config.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, GroovySourceLocator.ID);
    }
}
