/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.testplugin.util;


import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;


/*
 * This dialog is intended to verify a dialogs in a testing
 * environment.  The tester can test for sizing, initial focus,
 * or accessibility.
 */
public class VerifyDialog extends TitleAreaDialog {
	private int SIZING_WIDTH = 400;

	private static int      TEST_TYPE;
	public static final int TEST_SIZING = 0;
	public static final int TEST_FOCUS  = 1;
	public static final int TEST_ACCESS = 2;
	private IDialogTestPass _dialogTests[] = new IDialogTestPass[3];


	private Dialog _testDialog; //the dialog to test
	private Point  _testDialogSize;

	private Label  _queryLabel;
	private Button _yesButton;
	private Button _checkList[];
	private String _failureText;

	/*
	 * Create an instance of the verification dialog.
	 */
	public VerifyDialog(Shell parent) {
		super(parent);
		if ( !(TEST_TYPE <= 2) && !(TEST_TYPE >= 0) ) {
			TEST_TYPE = TEST_SIZING;
		}
		_failureText = "";
		_dialogTests[0] = new SizingTestPass();
		_dialogTests[1] = new FocusTestPass();
		_dialogTests[2] = new AccessibilityTestPass();
	}

	/* (non-Javadoc)
	 * Method declared on Window.
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Dialog Verification");
		setShellStyle(SWT.NONE);
	}
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		_yesButton = createButton(parent, IDialogConstants.YES_ID, IDialogConstants.YES_LABEL, true);
		createButton(parent, IDialogConstants.NO_ID, IDialogConstants.NO_LABEL, false);
	}
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.YES_ID == buttonId) {
			setReturnCode(IDialogConstants.YES_ID);
			if (_testDialog.getShell() != null) {
				_testDialog.close();
			}
			close();
		} else if (IDialogConstants.NO_ID == buttonId) {
			handleFailure();
		}
	}
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		// top level composite
		Composite parentComposite = (Composite)super.createDialogArea(parent);


		// create a composite with standard margins and spacing
		Composite composite = new Composite(parentComposite, SWT.NONE);
		composite.setSize(SIZING_WIDTH, SWT.DEFAULT);
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));


		createTestSelectionGroup(composite);
		createCheckListGroup(composite);


		_queryLabel = new Label(composite, SWT.NONE);
		_queryLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		initializeTest();
		return composite;
	}
	/*
	 * Group for selecting type of test.
	 */
	private void createTestSelectionGroup(Composite parent) {
		Group group = new Group(parent, SWT.SHADOW_NONE);
		group.setText("Testing:");
		group.setLayout( new GridLayout() );
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(data);

		for (int i = 0; i < _dialogTests.length; i++) {
			Button radio = new Button(group, SWT.RADIO);
			radio.setText( _dialogTests[i].label() );
			final int testID = _dialogTests[i].getID();
			radio.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					TEST_TYPE = testID;
					initializeTest();
					_yesButton.setEnabled(true);
				}
			});
			if ( TEST_TYPE == _dialogTests[i].getID() ) {
				radio.setSelection(true);
			}
		}
	}
	/*
	 * Initializes the checklist with empty checks.
	 */
	private void createCheckListGroup(Composite parent) {
		Group group = new Group(parent, SWT.SHADOW_NONE);
		group.setText("Verify that:");
		group.setLayout( new GridLayout() );
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(data);

		int checkListSize = 0;
		for (int i = 0; i < _dialogTests.length; i++) {
			int size = _dialogTests[i].checkListTexts().size();
			if (size > checkListSize) {
				checkListSize = size;
			}
		}
		_checkList = new Button[checkListSize];
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				checkYesEnable();
			}
		};
		for (int i = 0; i < checkListSize; i++) {
			_checkList[i] = new Button(group, SWT.CHECK);
			_checkList[i].addSelectionListener(selectionAdapter);
			data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			data.grabExcessHorizontalSpace = true;
			_checkList[i].setLayoutData(data);
		}
	}
	/*
	 * Disables the yes button if any of the items in the checklist
	 * are unchecked.  Enables the yes button otherwise.
	 */
	private void checkYesEnable() {
		boolean enable = true;
		for (int i = 0; i < _checkList.length; i++) {
			if ( !_checkList[i].getSelection() ) {
				enable = false;
			}
		}
		_yesButton.setEnabled(enable);
	}
	/*
	 * Initializes the checklist, banner texts, and query label
	 */
	private void initializeTest() {
		IDialogTestPass test = _dialogTests[TEST_TYPE];
		setTitle( test.title() );
		setMessage( test.description() );
		Iterator iterator = test.checkListTexts().iterator();
		for (int i = 0; i < _checkList.length; i++) {
			if ( iterator.hasNext() ) {
				_checkList[i].setText( iterator.next().toString() );
				_checkList[i].setVisible(true);
				_checkList[i].update();
			} else {
				_checkList[i].setVisible(false);
				_checkList[i].update();
			}
			_checkList[i].setSelection(true);
		}
		_queryLabel.setText( test.queryText() );
	}
	public String getFailureText() {
		return _failureText;
	}
	/*
	 * Can't open the verification dialog without a specified
	 * test dialog, this simply returns a failure and prevents
	 * opening.  Should use open(Dialog) instead.
	 *
	 */
	public int open() {
		_failureText = "Testing dialog is required, use VerifyDialog::open(Dialog)";
		return IDialogConstants.NO_ID;
	}
	/*
	 * Opens the verification dialog to test the specified dialog.
	 */
	public int open(Dialog testDialog) {
		if (getShell() == null) {
			create();
		}
		getShell().setLocation(0, 0);
		getShell().setSize(Math.max(SIZING_WIDTH, getShell().getSize().x), getShell().getSize().y);
		_testDialog = testDialog;
		if (_testDialog.getShell() == null) {
			_testDialog.create();
		}
		_testDialogSize = _testDialog.getShell().getSize();
		openNewTestDialog();

		return super.open();
	}
	/*
	 * Opens the dialog to be verified.
	 */
	private void openNewTestDialog() {
		if (_testDialog.getShell() == null) {
			_testDialog.create();
		}
		_testDialog.setBlockOnOpen(false);
		_testDialog.getShell().setLocation(getShell().getSize().x + 1, 0);
		_testDialog.getShell().setSize(_testDialogSize);
		_testDialog.getShell().addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				e.doit = false;
			}

		});
		_testDialog.open();
	}
	/*
	 * The test dialog failed, open the failure dialog.
	 */
	private void handleFailure() {
		IDialogTestPass test = _dialogTests[TEST_TYPE];
		StringBuffer text = new StringBuffer();
		String label = test.label();
		label = label.substring(0, label.indexOf("&")) +
		        label.substring(label.indexOf("&") + 1);
		text.append(label).
		     append(" failed on the ").
		     append(SWT.getPlatform()).
		     append(" platform:\n");

		String failureMessages[] = test.failureTexts();
		for (int i = 0; i < test.checkListTexts().size(); i++) {
			if ( !_checkList[i].getSelection() ) {
				text.append("- ").append(failureMessages[i]).append("\n");
			}
		}
		FailureDialog dialog = new FailureDialog( getShell() );
		dialog.create();
		dialog.setText( text.toString() );
		if (dialog.open() == IDialogConstants.OK_ID) {
			_failureText = dialog.toString();
			setReturnCode(IDialogConstants.NO_ID);
			if (_testDialog.getShell() != null) {
				_testDialog.close();
			}
			close();
		}
	}
	/*
	 * In case the shell was closed by a means other than
	 * the NO button.
	 */
	protected void handleShellCloseEvent() {
		handleFailure();
	}
}


