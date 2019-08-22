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

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public abstract class GroovyRefactoringAction implements IWorkbenchWindowActionDelegate, IEditorActionDelegate {

    private GroovyEditor editor;
    private ITextSelection selection;
    private GroovyCompilationUnit groovyUnit;

    protected boolean checkPreconditions() {
        if (editor != null && selection != null && groovyUnit != null) {
            if (groovyUnit.getModuleNode() != null) {
                return true;
            }
            displayErrorDialog("Cannot find ModuleNode for " + groovyUnit.getElementName());
        }
        return false;
    }

    protected void displayErrorDialog(String message) {
        ErrorDialog error = new ErrorDialog(
            editor.getSite().getShell(), "Groovy Refactoring error", message,
            new Status(IStatus.ERROR, GroovyPlugin.PLUGIN_ID, message), IStatus.ERROR | IStatus.WARNING);
        error.open();
    }

    @Override
    public void dispose() {
        editor = null;
        selection = null;
        groovyUnit = null;
    }

    protected final IDocument getDocument() {
        return editor.getDocumentProvider().getDocument(editor.getEditorInput());
    }

    protected final GroovyEditor getEditor() {
        return editor;
    }

    protected final ITextSelection getSelection() {
        return selection;
    }

    protected final GroovyCompilationUnit getUnit() {
        return groovyUnit;
    }

    @Override
    public void init(IWorkbenchWindow window) {
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof ITextSelection) {
            this.selection = (ITextSelection) selection;
        } else {
            this.selection = null;
        }
        updateEnabled(action);
    }

    @Override
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        if (targetEditor instanceof GroovyEditor) {
            this.editor = (GroovyEditor) targetEditor;
            this.groovyUnit = editor.getGroovyCompilationUnit();
        } else {
            this.editor = null;
            this.groovyUnit = null;
        }
        updateEnabled(action);
    }

    private void updateEnabled(IAction action) {
        action.setEnabled(editor != null && selection != null && groovyUnit != null);
    }
}
