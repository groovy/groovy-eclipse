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
import org.codehaus.groovy.eclipse.refactoring.core.GroovyRefactoring;
import org.codehaus.groovy.eclipse.refactoring.core.UserSelection;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.GroovyCompilationUnitDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.ui.GroovyRefactoringMessages;
import org.codehaus.groovy.eclipse.refactoring.ui.GroovyRefactoringWizard;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

/**
 * @author reto kleeb
 * 
 */
public abstract class GroovyRefactoringAction implements IWorkbenchWindowActionDelegate, IEditorActionDelegate {

	protected GroovyEditor editor;
	protected UserSelection selection;
	protected IGroovyDocumentProvider docProvider;

	protected boolean initRefactoring() {

		editor = (GroovyEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		ITextSelection ts = (ITextSelection) editor.getSelectionProvider().getSelection();
		selection = new UserSelection(ts.getOffset(), ts.getLength());
		docProvider = new GroovyCompilationUnitDocumentProvider(editor.getGroovyCompilationUnit());

		if (docProvider.getRootNode() == null) {
			displayErrorDialog(GroovyRefactoringMessages.GroovyRefactoringAction_No_Module_Node);
			return false;
		}
		return PlatformUI.getWorkbench().saveAllEditors(true);
	}

	protected void displayErrorDialog(String message) {
	    ErrorDialog error = new ErrorDialog(
	            editor.getSite().getShell(), "Groovy Refactoring error", message, 
	            new Status(IStatus.ERROR, GroovyPlugin.PLUGIN_ID, message), 
	            IStatus.ERROR | IStatus.WARNING);
		error.open();
	}

	public static void openRefactoringWizard(GroovyRefactoring refactoring) {
		GroovyRefactoringWizard wizard = new GroovyRefactoringWizard(refactoring, getUIFlags());
		RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(wizard);
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		String titleForFailedChecks = "Groovy Refactoring"; //$NON-NLS-1$

		try {
			op.run(shell, titleForFailedChecks);
		} catch (InterruptedException e) {
		}
	}

	public void dispose() {
	    editor = null;
	    selection = null;
	    docProvider = null;
	}

	public void init(IWorkbenchWindow window) {
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
	}

	public static int getUIFlags() {
		return RefactoringWizard.DIALOG_BASED_USER_INTERFACE | RefactoringWizard.PREVIEW_EXPAND_FIRST_NODE;
	}

}
