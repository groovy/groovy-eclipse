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
package org.codehaus.groovy.eclipse.refactoring.actions;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.refactoring.core.rename.CandidateCollector;
import org.codehaus.groovy.eclipse.refactoring.core.rename.JavaRefactoringDispatcher;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.Adapters;
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
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.widgets.Shell;

public class RenameDispatcherAction extends GroovyRefactoringAction {

    @Override
    public void run(IAction action) {
        if (checkPreconditions()) {
            GroovyCompilationUnit unit = getUnit();
            ITextSelection selection = getSelection();
            CandidateCollector dispatcher = new CandidateCollector(unit, selection);
            try {
                ISourceReference target = dispatcher.getRefactoringTarget();
                boolean lightweight = JavaPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.REFACTOR_LIGHTWEIGHT);
                if (!runViaAdapter(target, lightweight)) {
                    if (target instanceof IMember || target instanceof ILocalVariable) {
                        if (lightweight && nameMatches(((IJavaElement) target).getElementName().toCharArray(), unit, selection)) {
                            new GroovyRenameLinkedMode((IJavaElement) target, getEditor()).start();
                        } else {
                            openJavaRefactoringWizard((IJavaElement) target);
                        }
                    } else {
                        displayErrorDialog("Cannot refactor on current selection. No refactoring candidates found.");
                    }
                }
            } catch (CoreException e) {
                displayErrorDialog(e.getMessage());
            }
        }
    }

    /**
     * @return {@code true} iff the selected name matches the element name
     */
    private boolean nameMatches(char[] elementName, GroovyCompilationUnit unit, ITextSelection selection) {
        char[] contents = unit.getContents();
        // need to expand the selection so that it covers an entire word
        int start = Math.min(selection.getOffset(), contents.length - 1);
        int end = Math.min(start + selection.getLength(), contents.length);
        while (start > 0 && Character.isJavaIdentifierPart(contents[start - 1])) {
            start -= 1;
        }
        while (end  < contents.length && Character.isJavaIdentifierPart(contents[end])) {
            end += 1;
        }
        char[] selectedName = CharOperation.subarray(contents, start, end);
        return (selectedName != null && CharOperation.equals(elementName, selectedName));
    }

    private boolean runViaAdapter(ISourceReference targetParam, boolean lightweight) {
        try {
            if (targetParam instanceof IAdaptable) {
                IRenameTarget target = Adapters.adapt(targetParam, IRenameTarget.class);
                if (target != null) {
                    return target.performRenameAction(getShell(), getEditor(), lightweight);
                }
            }
        } catch (Exception e) {
            GroovyCore.logException("", e);
        }
        return false;
    }

    private void openJavaRefactoringWizard(IJavaElement element) throws CoreException {
        JavaRefactoringDispatcher dispatcher = new JavaRefactoringDispatcher(element);
        RenameSupport refactoring = dispatcher.dispatchJavaRenameRefactoring();
        refactoring.openDialog(getShell());
    }

    private Shell getShell() {
        return getEditor().getSite().getShell();
    }
}
