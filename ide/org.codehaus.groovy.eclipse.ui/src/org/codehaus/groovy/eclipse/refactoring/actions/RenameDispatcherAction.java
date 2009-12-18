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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.groovy.eclipse.refactoring.core.GroovyRefactoring;
import org.codehaus.groovy.eclipse.refactoring.core.rename.CandidateCollector;
import org.codehaus.groovy.eclipse.refactoring.core.rename.GroovyRefactoringDispatcher;
import org.codehaus.groovy.eclipse.refactoring.core.rename.JavaRefactoringDispatcher;
import org.codehaus.groovy.eclipse.refactoring.core.rename.NoRefactoringForASTNodeException;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameCandidates;
import org.codehaus.groovy.eclipse.refactoring.ui.selection.ElementSelectionDialog;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.refactoring.RenameSupport;
import org.eclipse.jface.action.IAction;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * @author martin
 * extended by Stefan Reinhard
 */
public class RenameDispatcherAction extends GroovyRefactoringAction {
	
	private final JavaEditor editor;

	public RenameDispatcherAction(JavaEditor editor) {
		this.editor = editor;
	}
	
	public RenameDispatcherAction() {
	    editor = null;
    }

	public void run(IAction action) {
		if (initRefactoring()) {
			CandidateCollector dispatcher = new CandidateCollector(docProvider, selection);
			try {
				IJavaElement[] javaCandidates = dispatcher.getJavaCandidates();	
				ASTNode[] groovyCandidates = dispatcher.getGroovyCandidates();
				int totalNumberOfCandidates = javaCandidates.length + groovyCandidates.length;
				if (totalNumberOfCandidates > 1) {		
					dispatchFromSelectionDialog(javaCandidates, groovyCandidates);
				} else if (javaCandidates.length == 1) {
					openJavaRefactoringWizard((IJavaElement)javaCandidates[0]);
				} else if (groovyCandidates.length == 1){
					openGroovyRefactoringWizard((ASTNode)groovyCandidates[0]);
				} else {
					displayErrorDialog("No candidates for Refactoring found!");
				}
			} catch (CoreException e) {
				displayErrorDialog(e.getMessage());
			}
		}
	}

	private void dispatchFromSelectionDialog(IJavaElement[] javaCandidates, ASTNode[] groovyCandidates)
	throws CoreException {
		Object selected = openSelectionDialog(javaCandidates, groovyCandidates);
		if (selected instanceof IJavaElement) {
			openJavaRefactoringWizard((IJavaElement)selected);
		} else if (selected instanceof ASTNode) {
			openGroovyRefactoringWizard((ASTNode)selected);
		}
	}

	private Object openSelectionDialog(IJavaElement[] javaCandidates, ASTNode[] groovyCandidates) throws CoreException {
		Shell s = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		RenameCandidates c = new RenameCandidates(groovyCandidates, javaCandidates);
		ElementSelectionDialog dialog = new ElementSelectionDialog(s, c);
		dialog.setInput(c);
		dialog.open();
		return dialog.getFirstResult();
	}
	
	private void openGroovyRefactoringWizard(ASTNode node) {
		ICompilationUnit unit = ((GroovyEditor) editor).getGroovyCompilationUnit();
		if (unit == null) {
			unit = JavaPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editor.getEditorInput(), false);
		}
		GroovyRefactoringDispatcher dispatcher = new GroovyRefactoringDispatcher(node, selection, docProvider, unit);
		try {
			GroovyRefactoring refactoring = dispatcher.dispatchGroovyRenameRefactoring();
			openRefactoringWizard(refactoring);
		} catch (NoRefactoringForASTNodeException e) {
			displayErrorDialog(e.getMessage());
		}
	}

	private void openJavaRefactoringWizard(IJavaElement element) throws CoreException {
		JavaRefactoringDispatcher dispatcher = new JavaRefactoringDispatcher(element);
		RenameSupport refactoring = dispatcher.dispatchJavaRenameRefactoring();
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		refactoring.openDialog(shell);
	}
	
	public static int getUIFlags() {
		return RefactoringWizard.DIALOG_BASED_USER_INTERFACE
				| RefactoringWizard.PREVIEW_EXPAND_FIRST_NODE;
	}

}
