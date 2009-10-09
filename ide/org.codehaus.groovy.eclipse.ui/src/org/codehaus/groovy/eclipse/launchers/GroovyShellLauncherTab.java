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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

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
//	    createMainTypeExtensions(parent);
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
	
}
