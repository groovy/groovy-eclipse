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

import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.groovy.eclipse.refactoring.core.extract.ExtractGroovyConstantRefactoring;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.refactoring.ExtractConstantWizard;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.jdt.internal.ui.refactoring.actions.RefactoringStarter;
import org.eclipse.jdt.ui.actions.ExtractConstantAction;
import org.eclipse.jdt.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.jface.text.ITextSelection;

/**
 * 
 * @author Andrew Eisenberg
 * @created May 10, 2010
 */
public class GroovyExtractConstantAction extends ExtractConstantAction {

    private final GroovyEditor fEditor;

    public GroovyExtractConstantAction(GroovyEditor editor) {
        super(editor);
        this.fEditor = editor;
    }

    public void run(ITextSelection selection) {
        if (!ActionUtil.isEditable(fEditor))
            return;
        ExtractGroovyConstantRefactoring refactoring = new ExtractGroovyConstantRefactoring(
                fEditor.getGroovyCompilationUnit(),
                selection.getOffset(), selection.getLength());
        new RefactoringStarter().activate(
                new ExtractConstantWizard(refactoring), getShell(),
                RefactoringMessages.ExtractConstantAction_extract_constant,
                RefactoringSaveHelper.SAVE_NOTHING);
    }
}
