/*
 * Copyright 2003-2012 the original author or authors.
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

import org.codehaus.groovy.eclipse.refactoring.core.convert.AssignStatementToNewLocalRefactoring;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Point;

/**
 * Assign a statement to a new local variable.
 * 
 * Example:
 * 
 * foo()
 * becomes
 * def foo = foo()
 * 
 * @author Stephanie Van Dyk sevandyk@gmail.com
 * @created April 15, 2012
 */
public class AssignStatementToNewLocalAction extends GroovyRefactoringAction {

    private AssignStatementToNewLocalRefactoring assignStatementRefactoring;

    public void run(IAction action) {
        if (initRefactoring() && assignStatementRefactoring.isApplicable()) {
            assignStatementRefactoring.applyRefactoring(getDocument());

            // update the selection
            Point p =  assignStatementRefactoring.getNewSelection();
            getEditor().getViewer().setSelectedRange(p.x, p.y);
        }
    }

    private IDocument getDocument() {
        return getEditor().getDocumentProvider().getDocument(getEditor().getEditorInput());
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        if (action.isEnabled() && getSelection() != null && getUnit() != null) {
            assignStatementRefactoring = new AssignStatementToNewLocalRefactoring(getUnit(), getSelection().getOffset());
            if (!assignStatementRefactoring.isApplicable()) {
                action.setEnabled(false);
            }
        }
    }
}
