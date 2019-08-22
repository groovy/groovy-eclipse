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

import org.codehaus.groovy.eclipse.refactoring.core.convert.ConvertToClosureRefactoring;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;

/**
 * Action to convert a method to a closure.
 */
public class ConvertToClosureAction extends GroovyRefactoringAction {

    private ConvertToClosureRefactoring convertToClosureRefactoring;

    @Override
    public void run(IAction action) {
        if (checkPreconditions() && convertToClosureRefactoring.isApplicable()) {
            convertToClosureRefactoring.applyRefactoring(getDocument());

            // force updating of the action's state after running
            int offset = getSelection().getOffset(), length = getSelection().getLength();
            getEditor().getViewer().setSelectedRange(offset, length > 1 ? length - 1 : length + 1);
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);

        if (action.isEnabled()) {
            Display.getDefault().asyncExec(() -> {
                if (getSelection() != null && getUnit() != null) {
                    convertToClosureRefactoring = new ConvertToClosureRefactoring(getUnit(), getSelection().getOffset());
                    action.setEnabled(convertToClosureRefactoring.isApplicable());
                }
            });
        }
    }
}
