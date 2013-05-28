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

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.launchers.GroovyShellLaunchDelegate;
import org.codehaus.groovy.eclipse.core.util.ListUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

/**
 * This class is reponsible for creating a launching the Groovy shell.  If an
 * existing launch configuration exists it will use that, if not it will
 * create a new launch configuration and launch it.
 *
 * @see ILaunchShortcut
 */
public class GroovyShellLaunchShortcut implements ILaunchShortcut {

    /**
     * The ID of this groovy launch configuration
     */
    public static final String GROOVY_SHELL_LAUNCH_CONFIG_ID = "org.codehaus.groovy.eclipse.groovyShellLaunchConfiguration" ;

    /**
     * Used for dialog presentation if the used needs to choose from
     * matching Launch configurations
     */
    public static final String SELECT_CONFIG_DIALOG_TITLE = "Select Groovy Shell Launch" ;

    /**
     * Used for dialog presentation if the used needs to choose from
     * matching Launch configurations
     */
    public static final String SELECT_CONFIG_DIALOG_TEXT = "Please select the Groovy Shell run configuration to Launch" ;

    /**
     * This is the string that will show if the groovy file the user is trying to run
     * doesn't meet the criteria to be run.
     */
    public static final String GROOVY_FILE_NOT_RUNNABLE_MESSAGE = "The groovy shell could not be run.";

    /**
     * Launches from the package explorer.
     *
     * @see ILaunchShortcut#launch
     */
    public void launch(ISelection selection, String mode)  {
        if (selection instanceof IStructuredSelection && ((IStructuredSelection) selection).getFirstElement() instanceof IJavaElement) {
            IStructuredSelection structredSelection = (IStructuredSelection) selection;
            IJavaElement elt = (IJavaElement) structredSelection.getFirstElement();
            launchGroovy(elt.getJavaProject(), mode);
        }
    }

    /**
     * Launches from the source file.
     *
     * @see ILaunchShortcut#launch
     */
    public void launch(IEditorPart editor, String mode)  {
        // make sure we are saved as we run groovy from the file
        editor.getEditorSite().getPage().saveEditor(editor,false);
        IEditorInput input = editor.getEditorInput();
        IFile file = (IFile) input.getAdapter(IFile.class);
        ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
        if (unit.getJavaProject() != null) {
            launchGroovy(unit.getJavaProject(), mode);
        }

    }

    private void launchGroovy(IJavaProject project, String mode) {
        String className = org.codehaus.groovy.tools.shell.Main.class.getName();

        try {
            String launchName = getLaunchManager().generateLaunchConfigurationName(project.getProject().getName());
            ILaunchConfigurationWorkingCopy launchConfig =
                    getGroovyLaunchConfigType().newInstance(null, launchName);

            launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, className);
            launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getElementName());
            launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "-Djline.terminal=jline.UnsupportedTerminal");
            List<String> classpath = ListUtil.newList(JavaRuntime.computeDefaultRuntimeClassPath(project));
            try {
                classpath.addAll(0, GroovyShellLaunchDelegate.getExtraClasspathElements());
            } catch (Exception e) {
                GroovyCore.logException("Error getting extra classpath elements to launch", e);
            }
            launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpath);

            DebugUITools.launch(launchConfig, mode);

        } catch (Exception e) {
            GroovyPlugin.getDefault().logException("Exception launching Groovy Console", e);
        }
    }

    /**
     * This is a convenience method for getting the Groovy launch configuration
     * type from the Launch Manager.
     *
     * @return Returns the ILaunchConfigurationType for running Groovy classes.
     */
    public static ILaunchConfigurationType getGroovyLaunchConfigType() {
        return getLaunchManager().getLaunchConfigurationType(GROOVY_SHELL_LAUNCH_CONFIG_ID);
    }

    /**
     * This is a convenince method for getting the Launch Manager from
     * the Debug plugin.
     *
     * @return Returns the default Eclipse launch manager.
     */
    public static ILaunchManager getLaunchManager() {
        return DebugPlugin.getDefault().getLaunchManager();
    }

}
