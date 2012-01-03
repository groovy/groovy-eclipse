/*
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
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

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * @author reto kleeb
 * @author andrew@eisenberg.as
 */
public abstract class GroovyRefactoringAction implements IWorkbenchWindowActionDelegate, IEditorActionDelegate {

    private GroovyEditor editor;
    private ITextSelection selection;
    private GroovyCompilationUnit gcu;

    protected boolean initRefactoring() {
        if (editor == null || selection == null || gcu == null) {
            return false;
        }
        if (gcu != null) {
            if (gcu.getModuleNode() == null) {
                displayErrorDialog("Cannot find ModuleNode for " + gcu.getElementName());
                return false;
            }
            return true;
            // return PlatformUI.getWorkbench().saveAllEditors(true);
        }
        return false;
    }

    protected void displayErrorDialog(String message) {
        ErrorDialog error = new ErrorDialog(
                editor.getSite().getShell(), "Groovy Refactoring error", message,
                new Status(IStatus.ERROR, GroovyPlugin.PLUGIN_ID, message),
                IStatus.ERROR | IStatus.WARNING);
        error.open();
    }

    public void dispose() {
        editor = null;
        gcu = null;
        selection = null;
    }

    protected GroovyEditor getEditor() {
        return editor;
    }

    protected ITextSelection getSelection() {
        return selection;
    }

    protected GroovyCompilationUnit getUnit() {
        return gcu;
    }

    public void init(IWorkbenchWindow window) {
    }

    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof ITextSelection) {
            this.selection = (ITextSelection) selection;
            action.setEnabled(true);
        } else {
            this.selection = null;
            action.setEnabled(false);
        }
    }

    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        if (targetEditor instanceof GroovyEditor) {
            this.editor = (GroovyEditor) targetEditor;
            this.gcu = editor.getGroovyCompilationUnit();
            action.setEnabled(true);
        } else {
            this.editor = null;
            this.gcu = null;
            action.setEnabled(false);
        }
    }

    public static int getUIFlags() {
        return RefactoringWizard.DIALOG_BASED_USER_INTERFACE | RefactoringWizard.PREVIEW_EXPAND_FIRST_NODE;
    }
}
