/* 
 * Copyright (C) 2009 Stefan Reinhard, Stefan Sidler
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.ui.selection;

import static org.codehaus.groovy.eclipse.refactoring.ui.GroovyRefactoringMessages.CandidateSelection_Message;
import static org.codehaus.groovy.eclipse.refactoring.ui.GroovyRefactoringMessages.CandidateSelection_SelectionNotOK;
import static org.codehaus.groovy.eclipse.refactoring.ui.GroovyRefactoringMessages.CandidateSelection_SelectionOK;
import static org.codehaus.groovy.eclipse.refactoring.ui.GroovyRefactoringMessages.CandidateSelection_Title;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameCandidates;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

/**
 * @author Stefan Reinhard
 */
public class ElementSelectionDialog extends ElementTreeSelectionDialog implements ISelectionStatusValidator {
	
	/**
	 * @param parent
	 * @param labelProvider
	 * @param contentProvider
	 */
	public ElementSelectionDialog(Shell parent, RenameCandidates candidates) {
		super(parent, new LabelDispatcher(), new SelectionElementProvider(candidates));
		setValidator(this);
		setAllowMultiple(false);
		setTitle(CandidateSelection_Title);
		setMessage(CandidateSelection_Message);
	}
	
	public void create() {
        BusyIndicator.showWhile(null, new Runnable() {
            public void run() {
            	superCreate();
                getTreeViewer().expandAll();
            	updateOKStatus();
            }
        });
    }
	
	public void superCreate() {
		super.create();
	}

	public IStatus validate(Object[] selection) {
		if (selection.length > 0) {
			Object element = selection[0];
			if (element instanceof IJavaElement || element instanceof ASTNode) {
				return new Status(IStatus.OK, " ", CandidateSelection_SelectionOK);
			}
		}
		return new Status(IStatus.ERROR, " ", CandidateSelection_SelectionNotOK);
	}

}
