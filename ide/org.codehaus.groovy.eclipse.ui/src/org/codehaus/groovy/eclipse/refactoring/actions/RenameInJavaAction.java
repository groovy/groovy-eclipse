/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
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

import org.codehaus.groovy.eclipse.refactoring.core.GroovyRefactoring;
import org.codehaus.groovy.eclipse.refactoring.core.jdtIntegration.groovyRefactorings.RenameRefactoringConverter;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * @author Stefan Sidler
 */
public class RenameInJavaAction implements IEditorActionDelegate,
		IViewActionDelegate {

	private static RenameInJavaAction activeAction = null;
	private IJavaElement[] elements;

	public RenameInJavaAction() {
		activeAction = this;
	}

	public static RenameInJavaAction getActiveAction() {
		return activeAction;
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {

		GroovyRefactoring refactoring = getRefactoringForSelectedElement();
		if (refactoring != null) {
			GroovyRefactoringAction.openRefactoringWizard(refactoring);
		}
	}

	public GroovyRefactoring getRefactoringForSelectedElement() {
		if (elements != null && elements.length == 1) {
			return RenameRefactoringConverter.createRefactoring(elements[0]);
		}
		return null;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		action.setEnabled(true);
		elements = getSelectedElements(selection);

		// Fixme: disaling checking of groovy elements
		if (elements != null && elements.length == 1) {
			action.setEnabled(isGroovyElement(elements[0]));
		} else {
			action.setEnabled(false);
		}
	}

	private boolean isGroovyElement(IJavaElement element) {
        if (element instanceof IMember) {
            IMember type = (IMember) element;
            ITypeRoot root = type.getTypeRoot();
            return root instanceof GroovyCompilationUnit;
        }
        return element instanceof GroovyCompilationUnit;
	}

	private IJavaElement[] getSelectedElements(ISelection selection) {
		if (selection instanceof ITextSelection) {
			IWorkbenchPage activePage = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			if (activePage != null
					&& activePage.getActiveEditor() instanceof JavaEditor) {
				JavaEditor editor = (JavaEditor) activePage.getActiveEditor();
				try {
					return SelectionConverter.codeResolve(editor);
				} catch (JavaModelException e) { /* intentional */
				}
			}
		}
		return null;
	}

	public void init(IViewPart view) {

	}
}
