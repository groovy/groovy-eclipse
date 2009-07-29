/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Michael Klenk and others        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
/**
 * 
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;

/**
 * @author Mike Klenk mklenk@hsr.ch
 * 
 */
public class IndentationAction extends EditingAction {

	@Override
    public void doAction(IAction action) throws BadLocationException {
		

		if(initFormatting()) {			
			Preferences preferences = GroovyPlugin.getDefault().getPluginPreferences();
			DefaultGroovyFormatter formatter = new DefaultGroovyFormatter(getTextSelection(),getDocument(), preferences, true);
			formatter.format().apply(getDocument());
		}
	}

	protected boolean initFormatting() {
		IFile sourceFile = ((IFileEditorInput) editor.getEditorInput()).getFile();
		try {
			IMarker[] markers = sourceFile.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ONE);
			if (markers.length > 0) {
				displayErrorDialog(GroovyRefactoringMessages.FormattingAction_Syntax_Errors);
				return false;
			}
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}
		
		if (!hasModuleNode(sourceFile)) {
			displayErrorDialog(GroovyRefactoringMessages.GroovyRefactoringAction_No_Module_Node);
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
		MessageBox error = new MessageBox(PlatformUI.getWorkbench().getDisplay().getActiveShell(), SWT.OK);
		error.setText("Formatting"); //$NON-NLS-1$
		error.setMessage(message);
		error.open();
	}
}
