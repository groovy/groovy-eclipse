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
package org.codehaus.groovy.eclipse.actions;

import static org.eclipse.core.runtime.FileLocator.resolve;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.util.ListUtil;
import org.codehaus.groovy.eclipse.launchers.GroovyLaunchShortcut;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.osgi.framework.Bundle;

/**
 * Provides a project action to execute the Groovy console.
 * @see IObjectActionDelegate
 */
public class RunGroovyShellConsole implements IObjectActionDelegate {
	public static final String GROOVY_SHELL_ACTION_ID = "org.codehaus.groovy.eclipse.groovyshell.action";
	public static final String GROOVY_CONSOLE_ACTION_ID = "org.codehaus.groovy.eclipse.groovyconsole.action"; 

	private ISelection selection;
	
	/**
	 * @see IEditorActionDelegate#run
	 */
	public void run(IAction action) {
	    
//        MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Not implemented", "This action is not implemented yet.");
//
//        if (true) return;
        
		String className ;
		if(GROOVY_CONSOLE_ACTION_ID.equals(action.getId())) {
		    // TODO not done yet
//			className = groovy.ui.Console.class.getName();
		    return;
		} else {
			className = groovy.ui.InteractiveShell.class.getName();
		}
		
        final IStructuredSelection s = (IStructuredSelection) selection;
        final Object selected = s.getFirstElement();
        if (selected instanceof IJavaProject) {
        	IJavaProject project = (IJavaProject) selected;
        	
        	try {
				String launchName = GroovyLaunchShortcut.getLaunchManager().generateUniqueLaunchConfigurationNameFrom(project.getProject().getName());
				ILaunchConfigurationWorkingCopy launchConfig = 
					GroovyLaunchShortcut.getGroovyLaunchConfigType().newInstance(null, launchName);
				
				launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, className);
				launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getElementName());
				List<String> classpath = ListUtil.newList(JavaRuntime.computeDefaultRuntimeClassPath(project));
				classpath.add(getPathToJLine());
				launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpath);
				
				DebugUITools.launch(launchConfig, "run");
				
			} catch (Exception e) {
				GroovyPlugin.getDefault().logException("Exception launching Groovy Console", e);
			}
		}
	}

	/**
	 * @see IEditorActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
        this.selection = selection;
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}
	
	@SuppressWarnings("unchecked")
    private String getPathToJLine() throws CoreException, IOException {
	    Bundle groovyBundle = Platform.getBundle("org.codehaus.groovy");
        Enumeration<URL> enu = groovyBundle.findEntries("", "jline-*.jar", false);
        if (enu.hasMoreElements()) {
            URL jar = resolve(enu.nextElement());
            return jar.getFile();
        } else {
            throw new CoreException(new Status(Status.ERROR, GroovyPlugin.PLUGIN_ID, "Could not find jline jar on the class path.  Please add it manually"));
        }
	}
}