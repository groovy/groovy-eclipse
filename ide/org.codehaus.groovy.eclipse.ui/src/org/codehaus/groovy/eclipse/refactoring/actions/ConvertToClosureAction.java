/*
 * Copyright 2003-2010 the original author or authors.
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

import org.codehaus.groovy.eclipse.refactoring.core.convert.ConvertToClosureRefactoring;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;

/**
 * Action to convert a method to a closure.
 * 
 * @author Geoff Denning
 * @created Nov 15, 2011
 */
public class ConvertToClosureAction extends GroovyRefactoringAction {

    private ConvertToClosureRefactoring convertToClosureRefactoring;

    public void run(IAction action) {
        if (initRefactoring() && convertToClosureRefactoring.isApplicable()) {
            convertToClosureRefactoring.applyRefactoring(getDocument());
            // force updating of the action's state after running
            int length = getSelection().getLength();
            getEditor().getViewer().setSelectedRange(getSelection().getOffset(), length > 1 ? length -1 : length + 1);
        }
    }

    private IDocument getDocument() {
        return getEditor().getDocumentProvider().getDocument(getEditor().getEditorInput());
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        if (action.isEnabled() && getSelection() != null && getUnit() != null) {
            convertToClosureRefactoring = new ConvertToClosureRefactoring(getUnit(), getSelection().getOffset());
            if (!convertToClosureRefactoring.isApplicable()) {
                action.setEnabled(false);
            }
        }
    }
}
