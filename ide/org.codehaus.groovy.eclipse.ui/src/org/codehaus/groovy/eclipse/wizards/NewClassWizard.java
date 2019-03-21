/*
 * Copyright 2009-2017 the original author or authors.
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

import org.codehaus.groovy.eclipse.ui.decorators.GroovyPluginImages;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;

public class NewClassWizard extends NewElementWizard {

    private NewClassWizardPage fPage;

    public NewClassWizard() {
        super();
        setWindowTitle("Create a new Groovy class.");
        setDialogSettings(JavaPlugin.getDefault().getDialogSettings());
        setDefaultPageImageDescriptor(GroovyPluginImages.DESC_NEW_GROOVY_ELEMENT);
    }

    @Override
    public void addPages() {
        super.addPages();
        fPage = new NewClassWizardPage();
        addPage(fPage);
        fPage.init(getSelection());
    }

    @Override
    protected boolean canRunForked() {
        return !fPage.isEnclosingTypeSelected();
    }

    @Override
    protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
        fPage.createType(monitor); // use the full progress monitor
    }

    @Override
    public boolean performFinish() {
        warnAboutTypeCommentDeprecation();
        boolean res = super.performFinish();
        if (res) {
            IResource resource = fPage.getModifiedResource();
            if (resource != null) {
                selectAndReveal(resource);
                openResource((IFile) resource);
            }
        }
        return res;
    }

    @Override
    public IJavaElement getCreatedElement() {
        return fPage.getCreatedType();
    }
}
