/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.ui.pages.extractMethod;

import org.codehaus.groovy.eclipse.refactoring.core.extractMethod.ExtractMethodInfo;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Michael Klenk mklenk@hsr.ch
 *
 */
public class ExtractMethodPage extends UserInputWizardPage{
	
	private ExtractMethodInfo extractMethodInfo;

	public ExtractMethodPage(String name,ExtractMethodInfo info) {
		super(name);
		this.extractMethodInfo = info;
		setTitle(name);
		setMessage("Extract a Method");
	}
	
	public void createControl(Composite parent) {

		Composite control = new Composite(parent, SWT.NONE);
		GridLayout baseLayout = new GridLayout();
		baseLayout.numColumns = 1;
		
		new ExtractMethodPageContent(control,extractMethodInfo,this);
		
		control.setLayout(baseLayout);
		setControl(control);
	}

}
