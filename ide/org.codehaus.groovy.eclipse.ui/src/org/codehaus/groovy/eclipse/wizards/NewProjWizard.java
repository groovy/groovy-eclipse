/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.wizards;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.ui.decorators.GroovyPluginImages;
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
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

public class NewProjWizard extends NewElementWizard implements IExecutableExtension {

    private IConfigurationElement configElement;
    private NewJavaProjectWizardPageOne pageOne;
    private NewJavaProjectWizardPageTwo pageTwo;

    public NewProjWizard() {
        setDefaultPageImageDescriptor(GroovyPluginImages.DESC_NEW_GROOVY_PROJECT);
        setDialogSettings(GroovyPlugin.getDefault().getDialogSettings());
        setWindowTitle(WizardMessages.NewProjWizard_title);
    }

    @Override
    public void addPages() {
        pageOne = new NewJavaProjectWizardPageOne();
        pageOne.setTitle(WizardMessages.NewProjWizard_page1_title);
        pageOne.setDescription(WizardMessages.NewProjWizard_page1_message);
        addPage(pageOne);

        pageTwo = new NewJavaProjectWizardPageTwo(pageOne);
        pageTwo.setTitle(WizardMessages.NewProjWizard_page2_title);
        pageTwo.setDescription(WizardMessages.NewProjWizard_page2_message);
        addPage(pageTwo);
    }

    @Override
    public boolean performCancel() {
        pageTwo.performCancel();
        return super.performCancel();
    }

    @Override
    public boolean performFinish() {
        boolean result = super.performFinish();
        if (result) {
            // Fix for 78263
            BasicNewProjectResourceWizard.updatePerspective(configElement);
            IProject project = pageTwo.getJavaProject().getProject();
            selectAndReveal(project);

            IWorkingSet[] workingSets = pageOne.getWorkingSets();
            if (workingSets.length > 0) {
                PlatformUI.getWorkbench().getWorkingSetManager().addToWorkingSets(project, workingSets);
            }

            // Force build of the new Groovy project using the Java builder so
            // that project state can be created. The creation of project state
            // means that Java projects can reference this project on their
            // build path and successfully continue to build.
            try {
                GroovyRuntime.addGroovyRuntime(project);
                project.build(IncrementalProjectBuilder.FULL_BUILD, null);
            } catch (CoreException e) {
                GroovyPlugin.getDefault().logError("Error adding Groovy runtime to project " + pageTwo.getJavaProject().getElementName(), e);
            }
        }
        return result;
    }

    @Override
    public IJavaElement getCreatedElement() {
        return pageTwo.getJavaProject();
    }

    //--------------------------------------------------------------------------

    @Override
    protected void finishPage(IProgressMonitor monitor) throws CoreException, InterruptedException {
        pageTwo.performFinish(monitor);
    }

    @Override
    protected void handleFinishException(Shell shell, java.lang.reflect.InvocationTargetException e) {
        ExceptionHandler.handle(e, getShell(), WizardMessages.NewProjWizard_error_title, WizardMessages.NewProjWizard_error_message);
    }

    /*
     * Stores the configuration element for the wizard. The config element is
     * used in {@code performFinish} to set the result perspective.
     */
    @Override
    public void setInitializationData(IConfigurationElement configElement, String propertyName, Object data) {
        this.configElement = configElement;
    }
}
