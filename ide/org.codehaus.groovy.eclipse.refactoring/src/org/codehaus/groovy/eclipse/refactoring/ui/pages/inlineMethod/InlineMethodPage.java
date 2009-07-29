/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.ui.pages.inlineMethod;

import org.codehaus.groovy.eclipse.refactoring.core.inlineMethod.InlineMethodInfo;
import org.codehaus.groovy.eclipse.refactoring.ui.eclipse.TextInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Michael Klenk mklenk@hsr.ch
 *
 */
public class InlineMethodPage extends TextInputWizardPage{
	
	private InlineMethodInfo info;

	public InlineMethodPage(String name,InlineMethodInfo info) {
		super(name,true);
		this.info = info;
		setTitle(name);
		setMessage("Inline a Method");
	}

	public void createControl(Composite parent) {

		Composite control = new Composite(parent, SWT.NONE);
		GridLayout baseLayout = new GridLayout();
		baseLayout.numColumns = 1;
		
		new InlineMethodWizard(control,info);
		
		control.setLayout(baseLayout);
		setControl(control);
	}

}
