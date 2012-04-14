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
import org.codehaus.groovy.eclipse.refactoring.core.extract.ConvertGroovyLocalToFieldRefactoring;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.refactoring.PromoteTempWizard;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.jdt.internal.ui.refactoring.actions.RefactoringStarter;
import org.eclipse.jdt.ui.actions.ConvertLocalToFieldAction;
import org.eclipse.jdt.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.jface.text.ITextSelection;

/**
 * @author Daniel Phan
 * @created 2012-01-26
 */
public class GroovyConvertLocalToFieldAction extends ConvertLocalToFieldAction {

    private final GroovyEditor fEditor;

    public GroovyConvertLocalToFieldAction(GroovyEditor editor) {
        super(editor);
        this.fEditor = editor;
    }

    @Override
    public void run(ITextSelection selection) {
        if (!ActionUtil.isEditable(fEditor))
            return;
        ConvertGroovyLocalToFieldRefactoring refactoring = new ConvertGroovyLocalToFieldRefactoring(
                fEditor.getGroovyCompilationUnit(),
                selection.getOffset(), selection.getLength());
        new RefactoringStarter().activate(
                new PromoteTempWizard(refactoring), getShell(),
                RefactoringMessages.ConvertLocalToField_title,
                RefactoringSaveHelper.SAVE_NOTHING);
    }
}