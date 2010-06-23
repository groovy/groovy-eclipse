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
package org.codehaus.groovy.eclipse.editor.actions;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.groovy.eclipse.refactoring.core.extract.ExtractGroovyLocalRefactoring;
import org.codehaus.groovy.eclipse.refactoring.ui.extract.ExtractLocalWizard;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.refactoring.UserInterfaceStarter;
import org.eclipse.jdt.ui.actions.ExtractTempAction;
import org.eclipse.jdt.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 *
 * @author Andrew Eisenberg
 * @created May 10, 2010
 */
public class GroovyExtractLocalAction extends ExtractTempAction {

    private final GroovyEditor fEditor;

    public GroovyExtractLocalAction(GroovyEditor editor) {
        super(editor);
        this.fEditor = editor;
    }

    @Override
    public void run(ITextSelection selection) {
        if (!ActionUtil.isEditable(fEditor)) {
            return;
        }

        ExtractGroovyLocalRefactoring refactoring = new ExtractGroovyLocalRefactoring(fEditor.getGroovyCompilationUnit(), selection
                .getOffset(), selection.getLength());
        ExtractLocalWizard wizard = new ExtractLocalWizard(refactoring);
        UserInterfaceStarter starter = new UserInterfaceStarter();
        starter.initialize(wizard);
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        try {
            starter.activate(refactoring, shell, RefactoringSaveHelper.SAVE_REFACTORING);
        } catch (CoreException e) {
            GroovyCore.logException("Exception ", e);
        }
    }
}
