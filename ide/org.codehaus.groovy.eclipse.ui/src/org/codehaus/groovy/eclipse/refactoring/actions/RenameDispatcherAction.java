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
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.refactoring.RenameSupport;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextSelection;
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
            ITextSelection selection = getSelection();
            GroovyCompilationUnit unit = getUnit();
            CandidateCollector dispatcher = new CandidateCollector(unit, selection);
            try {
                ISourceReference target = dispatcher.getRefactoringTarget();
                IPreferenceStore store = JavaPlugin.getDefault().getPreferenceStore();
                boolean lightweight = store.getBoolean(PreferenceConstants.REFACTOR_LIGHTWEIGHT);
                if (runViaAdapter(target, lightweight)) {
                    return;
                }
                if (target instanceof IMember || target instanceof ILocalVariable) {
                    if (lightweight && nameMatches(((IJavaElement) target).getElementName(), unit, selection)) {
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

    /**
     * @param elementName
     * @param unit
     * @param selection
     * @return true iff the selected name matches the element name
     */
    private boolean nameMatches(String elementName, GroovyCompilationUnit unit, ITextSelection selection) {
        char[] contents = unit.getContents();
        // need to expand the selection so that it covers an entire word
        int start = selection.getOffset();
        int end = start + selection.getLength();
        while (start >= contents.length || (start >= 0 && Character.isJavaIdentifierPart(contents[start]))) {
            start --;
        }
        if (start != 0 || !Character.isJavaIdentifierPart(contents[start])) {
            start ++;
        }
        while (end < contents.length && Character.isJavaIdentifierPart(contents[end])) {
            end ++;
        }
        if (end > contents.length) {
            end --;
        }
        char[] selectedText = CharOperation.subarray(contents, start, end);
        return selectedText != null && elementName.equals(String.valueOf(selectedText));
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
