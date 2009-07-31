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
package org.codehaus.groovy.eclipse.launchers;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;


/**
 * @see AbstractLaunchConfigurationTabGroup
 */
public class GroovyShellLaunchConfigurationTabGroup
        extends AbstractLaunchConfigurationTabGroup {


	/**
	 * @see AbstractLaunchConfigurationTabGroup#createTabs
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
				new GroovyShellLauncherTab(),
				new JavaArgumentsTab(),
				new JavaJRETab(),
				new JavaClasspathTab(),
				new EnvironmentTab(),
				new CommonTab()
				}; 
		setTabs(tabs);
	}
}
