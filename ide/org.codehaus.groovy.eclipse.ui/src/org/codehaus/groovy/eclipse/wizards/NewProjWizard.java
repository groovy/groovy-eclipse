/*
 * Copyright 2009-2020 the original author or authors.
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.groovy.core.util.ArrayUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageTwo;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
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
        pageOne = new NewJavaProjectWizardPageOne() {
            @Override
            public IClasspathEntry[] getDefaultClasspathEntries() {
                IClasspathEntry[] entries = super.getDefaultClasspathEntries();
                entries = (IClasspathEntry[]) ArrayUtils.add(entries, GroovyRuntime.newGroovyClasspathContainerEntry(false, false, null));

                String namespace = "org.codehaus.groovy.eclipse.dsl";
                IPreferenceStore prefs = new ScopedPreferenceStore(InstanceScope.INSTANCE, namespace);
                if (!prefs.getBoolean(namespace + ".disabled") /*&& prefs.getBoolean(namespace + ".auto.add.support")*/) {
                    entries = (IClasspathEntry[]) ArrayUtils.add(entries, entries.length - 1, JavaCore.newContainerEntry(new Path(GroovyRuntime.DSLD_CONTAINER_ID)));
                }
                return entries;
            }
        };
        pageOne.setTitle(WizardMessages.NewProjWizard_page1_title);
        pageOne.setDescription(WizardMessages.NewProjWizard_page1_message);
        addPage(pageOne);

        pageTwo = new NewJavaProjectWizardPageTwo(pageOne) {
            @Override
            public void createControl(final Composite parent) {
                super.createControl(parent);

                Object buildPathsBlock = ReflectionUtils.executePrivateMethod(JavaCapabilityConfigurationPage.class, "getBuildPathsBlock", this);
                if (buildPathsBlock != null) {
                    Object sourceFolderPage = ReflectionUtils.executePrivateMethod(buildPathsBlock.getClass(), "getSourceContainerPage", buildPathsBlock);
                    if (sourceFolderPage != null) {
                        Object createModuleInfo = ReflectionUtils.getPrivateField(sourceFolderPage.getClass(), "fCreateModuleInfoFileButton", sourceFolderPage);
                        if (createModuleInfo != null) {
                            ReflectionUtils.executePrivateMethod(createModuleInfo.getClass(), "setSelection", new Class[] {boolean.class}, createModuleInfo, new Object[] {Boolean.FALSE});
                        }
                    }
                }
            }
        };
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
            IProject project = getCreatedElement().getProject();
            try {
                GroovyRuntime.addGroovyNature(project);
            } catch (CoreException e) {
                GroovyPlugin.getDefault().logError("Error adding Groovy nature to project " + project.getName(), e);
            }

            IWorkingSet[] workingSets = pageOne.getWorkingSets();
            if (workingSets.length > 0) {
                PlatformUI.getWorkbench().getWorkingSetManager().addToWorkingSets(project, workingSets);
            }

            BasicNewProjectResourceWizard.updatePerspective(configElement);

            selectAndReveal(project);
        }
        return result;
    }

    @Override
    public IJavaProject getCreatedElement() {
        return pageTwo.getJavaProject();
    }

    //--------------------------------------------------------------------------

    @Override
    protected void finishPage(final IProgressMonitor monitor) throws CoreException, InterruptedException {
        pageTwo.performFinish(monitor);
    }

    @Override
    protected void handleFinishException(final Shell shell, final java.lang.reflect.InvocationTargetException e) {
        ExceptionHandler.handle(e, getShell(), WizardMessages.NewProjWizard_error_title, WizardMessages.NewProjWizard_error_message);
    }

    /*
     * Stores the configuration element for the wizard. The config element is
     * used in {@code performFinish} to set the result perspective.
     */
    @Override
    public void setInitializationData(final IConfigurationElement configElement, final String propertyName, final Object data) {
        this.configElement = configElement;
    }
}
