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
package org.codehaus.groovy.eclipse.actions;

import org.codehaus.groovy.eclipse.wizards.NewProjWizard;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.ui.actions.AbstractOpenWizardAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class OpenNewProjectWizardAction extends AbstractOpenWizardAction implements IWorkbenchWindowActionDelegate {

    public OpenNewProjectWizardAction() {
    }

    @Override
    protected final INewWizard createWizard() throws CoreException {
        return new NewProjWizard();
    }

    @Override
    protected boolean doCreateProjectFirstOnEmptyWorkspace(Shell shell) {
        return true; // can work on an empty workspace
    }

    @Override
    public void dispose() {
    }

    @Override
    public void init(IWorkbenchWindow window) {
        setShell(window.getShell());
    }

    @Override
    public void run(IAction action) {
        super.run();
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            setSelection((IStructuredSelection) selection);
        } else {
            setSelection(StructuredSelection.EMPTY);
        }
    }
}
