/*
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
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
package org.codehaus.groovy.eclipse.refactoring.actions;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.refactoring.core.rename.CandidateCollector;
import org.codehaus.groovy.eclipse.refactoring.core.rename.JavaRefactoringDispatcher;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.refactoring.RenameSupport;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * @author martin
 *         extended by Stefan Reinhard
 *         condensed by Andrew Eisenberg
 */
public class RenameDispatcherAction extends GroovyRefactoringAction {

	public void run(IAction action) {
		if (initRefactoring()) {
			CandidateCollector dispatcher = new CandidateCollector(getUnit(), getSelection());
			try {
			    ISourceReference target = dispatcher.getRefactoringTarget();
                IPreferenceStore store = JavaPlugin.getDefault().getPreferenceStore();
                boolean lightweight = store.getBoolean(PreferenceConstants.REFACTOR_LIGHTWEIGHT);
                if (runViaAdapter(target, lightweight))
                    return;
                if (target instanceof IMember || target instanceof ILocalVariable) {
                    if (lightweight) {
                        new GroovyRenameLinkedMode((IJavaElement) target, getEditor()).start();
                    } else {
                        openJavaRefactoringWizard((IJavaElement) target);
                    }
                } else {
                    displayErrorDialog("Cannot refactor on current selection.  No refactoring candidates found");
                }
            } catch (CoreException e) {
				displayErrorDialog(e.getMessage());
			}
		}
	}

    private boolean runViaAdapter(ISourceReference _target, boolean lightweight) {
        try {
            IRenameTarget target = adapt(_target, IRenameTarget.class);
            if (target != null) {
                return target.performRenameAction(getShell(), getEditor(), lightweight);
            }
        } catch (Exception e) {
            GroovyCore.logException("", e);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static <T> T adapt(Object target, Class<T> clazz) {
        T result;
        if (target instanceof IAdaptable) {
            result = (T) ((IAdaptable) target).getAdapter(clazz);
        } else {
            result = null;
        }
        System.out.println("==> " + result);
        return result;
    }

    private void openJavaRefactoringWizard(IJavaElement element) throws CoreException {
		JavaRefactoringDispatcher dispatcher = new JavaRefactoringDispatcher(element);
		RenameSupport refactoring = dispatcher.dispatchJavaRenameRefactoring();
		Shell shell = getShell();
        refactoring.openDialog(shell);
	}

    private Shell getShell() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    }
}
