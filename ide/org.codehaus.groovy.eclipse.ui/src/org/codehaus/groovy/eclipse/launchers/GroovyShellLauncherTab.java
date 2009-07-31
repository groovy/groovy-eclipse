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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.ui.viewsupport.JavaUILabelProvider;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ListDialog;

/**
 * Dialog for creating a groovy shell launch configuration.
 *
 * @author aeisenberg
 */
public class GroovyShellLauncherTab extends JavaMainTab {

	/**
	 * Dialog for selecting the groovy class to run.
	 */
	protected void handleSearchButtonSelected() {
	}
	
	@Override
	protected void createMainTypeEditor(Composite parent, String text) {
	    fMainText = new Text(parent, 0);
	    fMainText.setVisible(false);
        String className = groovy.ui.InteractiveShell.class.getName();
	    fMainText.setText(className);
	    createMainTypeExtensions(parent);
	}
	
	@Override
	protected void updateMainTypeFromConfig(ILaunchConfiguration config) {
	}
	
	@Override
	public void initializeFrom(ILaunchConfiguration config) {
        String projectName = EMPTY_STRING;
        try {
            projectName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);   
        }
        catch (CoreException ce) {
            setErrorMessage(ce.getStatus().getMessage());
        }
        fProjText.setText(projectName);
        
        ReflectionUtils.executePrivateMethod(JavaLaunchTab.class, "setCurrentLaunchConfiguration", 
                new Class[] { ILaunchConfiguration.class }, this, new Object[] { config });
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return "Groovy Shell"; //$NON-NLS-1$
	}	
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
	}	
	
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
     */
    public void setDefaults(ILaunchConfigurationWorkingCopy config) {
        super.setDefaults(config);
        config.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, GroovySourceLocator.ID);
    }
}
