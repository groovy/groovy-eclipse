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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * 
 * @author martin
 *
 */
public class RenameClassPage extends RenamePage {
	
	private final String[] oldName = new String[2];
	
	public RenameClassPage(String name, RenameInfo info) {
		super(name, info);
		separateClassName(info.getOldName());
	}
	
	@Override
    public void createControl(Composite parent) {
		Composite result = new Composite(parent, SWT.NONE);
		setControl(result);

		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		result.setLayout(layout);
		RowLayouter layouter = new RowLayouter(3);
		
		Label newName = new Label(result, SWT.NONE);
		Label packageName = new Label(result, SWT.None);
		newName.setText("New Name: ");
		packageName.setText(oldName[0]);

		text = createTextInputField(result);
		text.selectAll();
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		layouter.perform(newName, packageName, text, 3);
		
		setText(oldName[1]);
		getTextField().setSelection(0, oldName[1].length());
	}
	
	/**
	 * Separates a full qualified class name into the package name and the class name
	 * with the the package name in index 0 and class in index 1
	 * @param className full qualified class name
	 * @return 
	 */
	public void separateClassName(String className) {
		int lastDotIndex = className.lastIndexOf('.');
		oldName[0] = className.substring(0,lastDotIndex+1);
		oldName[1] = className.substring(lastDotIndex+1,className.length());
	}
	
	@Override
    public boolean isPageComplete() {
		if(info == null || text == null) 
			return false;
		return !info.getOldName().equals(oldName[0]+text.getText()) && super.isPageComplete();
	}
}
