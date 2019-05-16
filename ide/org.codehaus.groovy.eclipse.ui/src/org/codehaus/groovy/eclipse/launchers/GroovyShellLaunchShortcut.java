/*
 * Copyright 2009-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.launchers;

import java.util.Arrays;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Adapters;
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
import org.eclipse.ui.IEditorPart;

public class GroovyShellLaunchShortcut implements ILaunchShortcut {

    @Override
    public void launch(IEditorPart editor, String mode) {
        // make sure we are saved as we run groovy from the file
        editor.getEditorSite().getPage().saveEditor(editor, false);
        IFile file = Adapters.adapt(editor.getEditorInput(), IFile.class);
        ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
        if (unit.getJavaProject() != null) {
            launchGroovy(unit.getJavaProject(), mode);
        }
    }

    @Override
    public void launch(ISelection selection, String mode)  {
        if (selection instanceof IStructuredSelection && ((IStructuredSelection) selection).getFirstElement() instanceof IJavaElement) {
            IStructuredSelection structredSelection = (IStructuredSelection) selection;
            Object elem = structredSelection.getFirstElement();
            if (elem instanceof IJavaElement) {
                launchGroovy(((IJavaElement) elem).getJavaProject(), mode);
            }
        }
    }

    private void launchGroovy(IJavaProject project, String mode) {
        try {
            ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
            String launchName = launchManager.generateLaunchConfigurationName(project.getProject().getName());
            ILaunchConfigurationType launchType = launchManager.getLaunchConfigurationType("org.codehaus.groovy.eclipse.groovyShellLaunchConfiguration");

            ILaunchConfigurationWorkingCopy launchConfig = launchType.newInstance(null, launchName);
            launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getElementName());
            launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "org.codehaus.groovy.tools.shell.Main");
            launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "-Djline.terminal=jline.UnsupportedTerminal");
            launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, Arrays.asList(JavaRuntime.computeDefaultRuntimeClassPath(project)));

            DebugUITools.launch(launchConfig, mode);
        } catch (Exception e) {
            GroovyPlugin.getDefault().logError("Exception launching Groovy Shell", e);
        }
    }
}
