/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrew Eisenberg - Adapted for Groovy-Eclipse
 *******************************************************************************/
package org.codehaus.groovy.eclipse.editor.actions;

import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.groovy.eclipse.search.GroovyOccurrencesFinder;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.search.FindOccurrencesEngine;
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
        FindOccurrencesEngine engine = FindOccurrencesEngine.create(new GroovyOccurrencesFinder());
        try {
            String result = engine.run(editor.getGroovyCompilationUnit(), sel.getOffset(), sel.getLength());
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

    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof TextSelection) {
            sel = (TextSelection) selection;
        } else {
            sel = null;
        }
    }

    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        if (targetEditor instanceof GroovyEditor) {
            editor = (GroovyEditor) targetEditor;
        } else {
            editor = null;
        }
    }

    private void showMessage(Shell shell, String msg) {
        IEditorStatusLine statusLine = (IEditorStatusLine) editor.getAdapter(IEditorStatusLine.class);
        if (statusLine != null) {
            statusLine.setMessage(true, msg, null);
        }
        if (shell != null) {
            shell.getDisplay().beep();
        }
    }
}
