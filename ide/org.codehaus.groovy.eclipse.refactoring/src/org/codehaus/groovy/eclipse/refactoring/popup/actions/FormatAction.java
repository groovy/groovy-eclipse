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
package org.codehaus.groovy.eclipse.refactoring.popup.actions;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.editor.actions.EditingAction;
import org.codehaus.groovy.eclipse.refactoring.formatter.DefaultGroovyFormatter;
import org.codehaus.groovy.eclipse.refactoring.ui.GroovyRefactoringMessages;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;

/**
 * @author Mike Klenk mklenk@hsr.ch
 * 
 */
public class FormatAction extends EditingAction {

	@Override
    public void doAction(IAction action) throws BadLocationException {

		if (initFormatting()) {
			IPreferenceStore preferences = GroovyPlugin.getDefault()
					.getPreferenceStore();
			DefaultGroovyFormatter formatter = new DefaultGroovyFormatter(
					getTextSelection(), getDocument(), preferences, false);
			try {
				formatter.format().apply(getDocument());
			} catch (RuntimeException e) {
				displayErrorDialog(e.getMessage());
			}
		}
	}

	protected boolean initFormatting() {

		try {
			IFile sourceFile = ((IFileEditorInput) editor.getEditorInput())
					.getFile();
			IMarker[] markers = sourceFile.findMarkers(IMarker.PROBLEM, true,
					IResource.DEPTH_ONE);
			if (markers.length > 0) {
				displayErrorDialog(GroovyRefactoringMessages.FormattingAction_Syntax_Errors);
				return false;
			}
			if (!hasModuleNode(sourceFile)) {
				displayErrorDialog(GroovyRefactoringMessages.GroovyRefactoringAction_No_Module_Node);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

    /**
     * @param sourceFile
     */
    private boolean hasModuleNode(IFile sourceFile) {
        ICompilationUnit unit = JavaCore.createCompilationUnitFrom(sourceFile);
        if (unit instanceof GroovyCompilationUnit) {
            return ((GroovyCompilationUnit) unit).getModuleNode() != null;
        }
        return false;
    }

	protected void displayErrorDialog(String message) {
		MessageBox error = new MessageBox(PlatformUI.getWorkbench()
				.getDisplay().getActiveShell(), SWT.OK);
		error.setText("Formatting"); //$NON-NLS-1$
		error.setMessage(message);
		error.open();
	}
}
