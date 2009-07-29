/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.ui.pages.rename;

import org.codehaus.groovy.eclipse.refactoring.core.rename.RenameInfo;
import org.codehaus.groovy.eclipse.refactoring.ui.eclipse.RowLayouter;
import org.codehaus.groovy.eclipse.refactoring.ui.eclipse.TextInputWizardPage;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Page for the wizard that is used for rename refactorings
 * @author martin kempf
 *
 */
public class RenamePage extends TextInputWizardPage {
	
	protected RenameInfo info;
	protected Text text;

	public RenamePage(String name, RenameInfo info) {
		super(name,true);
		this.info = info;
		setTitle(name);
		setMessage(name);
	}

	public void createControl(Composite parent) {
		Composite result = new Composite(parent, SWT.NONE);
		setControl(result);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		result.setLayout(layout);
		RowLayouter layouter = new RowLayouter(2);

		Label label = new Label(result, SWT.NONE);
		label.setText("New Name: ");

		text = createTextInputField(result);
		text.selectAll();
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		layouter.perform(label, text, 1);
		
		setText(info.getOldName());
		getTextField().setSelection(0, info.getOldName().length());
	}
	
	@Override
    public void setPageComplete(RefactoringStatus status) {
		info.checkUserInput(status, text.getText());
		super.setPageComplete(status);
	}

	@Override
    protected void textModified(String text) {
		info.setNewName(text);
		super.textModified(text);
	}

	@Override
    public boolean isPageComplete() {
		if(info == null || text == null) 
			return false;
		return !info.getOldName().equals(text.getText()) && super.isPageComplete();
	}
	
	
}
