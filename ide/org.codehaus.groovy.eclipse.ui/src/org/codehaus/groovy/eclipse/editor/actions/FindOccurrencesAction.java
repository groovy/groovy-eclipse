/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.editor.actions;

import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.groovy.eclipse.search.GroovyOccurrencesFinder;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IEditorStatusLine;

public class FindOccurrencesAction implements IEditorActionDelegate {

    private GroovyEditor editor;

    private TextSelection sel;

    @Override
    public void run(IAction action) {
        if (editor == null || sel == null) {
            return;
        }

        // ensure that selection is up to date. For some reason,
        // selection changed is not being called when the selection changes
        // from a 0-length selection to another 0-length selection
        ISelection editorSel = editor.getSelectionProvider().getSelection();
        if (editorSel instanceof TextSelection) {
            sel = (TextSelection) editorSel;
        }
        try {
            String result = GroovyOccurrencesFinder.newFinderEngine().run(
                editor.getGroovyCompilationUnit(), sel.getOffset(), sel.getLength());
            if (result != null)
                showMessage(getShell(), result);
        } catch (JavaModelException e) {
            JavaPlugin.log(e);
        }
    }

    private Shell getShell() {
        if (editor == null) {
            return null;
        }
        return editor.getSite().getShell();
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof TextSelection) {
            sel = (TextSelection) selection;
        } else {
            sel = null;
        }
    }

    @Override
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        if (targetEditor instanceof GroovyEditor) {
            editor = (GroovyEditor) targetEditor;
        } else {
            editor = null;
        }
    }

    private void showMessage(Shell shell, String msg) {
        IEditorStatusLine statusLine = Adapters.adapt(editor, IEditorStatusLine.class);
        if (statusLine != null) {
            statusLine.setMessage(true, msg, null);
        }
        if (shell != null) {
            shell.getDisplay().beep();
        }
    }
}
