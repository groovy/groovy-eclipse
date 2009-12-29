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
package org.codehaus.groovy.eclipse.launchers;

import java.util.List;

import org.codehaus.groovy.eclipse.core.GroovyCore;
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
public abstract class AbstractGroovyLauncherTab extends JavaMainTab {

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
            final List<IType> availableClasses = findAllRunnableTypes(javaProject);
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
     * @param javaProject
     * @return
     * @throws JavaModelException
     */
    protected abstract List<IType> findAllRunnableTypes(IJavaProject javaProject)
            throws JavaModelException;
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

}
