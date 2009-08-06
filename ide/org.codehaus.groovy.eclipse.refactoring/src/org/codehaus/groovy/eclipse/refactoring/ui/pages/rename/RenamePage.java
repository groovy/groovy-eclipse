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
