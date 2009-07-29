/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.ui.pages.inlineMethod;

import org.codehaus.groovy.eclipse.refactoring.core.inlineMethod.InlineMethodInfo;
import org.codehaus.groovy.eclipse.refactoring.ui.GroovyRefactoringMessages;
import org.codehaus.groovy.eclipse.refactoring.ui.pages.extractMethod.ExtractMethodPageContent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * @author M.Klenk mklenk@hsr.ch
 * 
 */
public class InlineMethodWizard extends Composite {

	private InlineMethodInfo inlineMethodInfo;
	private Button btnInvocation, btnDelete, btnSelected;

	public InlineMethodWizard(Composite control, InlineMethodInfo info) {
		super(control, SWT.NONE);
		this.inlineMethodInfo = info;
		setLayout(new GridLayout());
		GridData compositeLData = new GridData();
		compositeLData.horizontalAlignment = GridData.FILL;
		compositeLData.grabExcessHorizontalSpace = true;
		setLayoutData(compositeLData);
		
		initialize(control);
		
		layout();
	}

	private void initialize(Composite shell) {
		
		Group groupInline = new Group(shell,SWT.NONE);
		groupInline.setText(GroovyRefactoringMessages.InlineMethodWizard_Inline);
		
		GridLayout gridLayout = new GridLayout ();
		groupInline.setLayout (gridLayout);
				
		GridData groupData = new GridData();
		groupData.horizontalAlignment = GridData.FILL;
		groupData.grabExcessHorizontalSpace = true;
		groupInline.setLayoutData(groupData);
		
		btnInvocation = new Button (groupInline, SWT.RADIO);
		btnInvocation.setText (GroovyRefactoringMessages.InlineMethodWizard_All_Invocations);
		btnInvocation.addListener(SWT.Selection,new Listener() {
			public void handleEvent(Event event) {
				updateValues();
			}
		});
		
		btnDelete = new Button (groupInline, SWT.CHECK);
		btnDelete.setText (GroovyRefactoringMessages.InlineMethodWizard_Delete_Method);
		GridData data = new GridData ();
		data.horizontalIndent = 20;
		btnDelete.setLayoutData (data);
		btnDelete.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				updateValues();
			}
		});

		btnSelected = new Button (groupInline, SWT.RADIO);
		btnSelected.setText (GroovyRefactoringMessages.InlineMethodWizard_Only_Selected_Invocation);
		
		updateValues();
	}

	void updateValues() {
		inlineMethodInfo.setInlineAllInvocations(btnInvocation.getSelection());
		inlineMethodInfo.setDeleteMethod(btnDelete.getSelection());
		
		btnDelete.setEnabled(inlineMethodInfo.isInlineAllInvocations());
		btnSelected.setEnabled(!inlineMethodInfo.isMethodDeclarationSelected());
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		GridLayout gridLayout = new GridLayout();
		shell.setLayout(gridLayout);
		
		new ExtractMethodPageContent(shell, null, null);
		
		shell.pack();
		shell.open();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
	
}
