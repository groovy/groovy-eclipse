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



import java.util.ArrayList;


/*
 * This test pass verifies the initial focus of a dialog
 * when it is given focus.
 */
public class FocusTestPass implements IDialogTestPass {
	private static final int CHECKLIST_SIZE = 1;


	/**
	 * @see IDialogTestPass#title()
	 */
	public String title() {
		return "Test Pass: Initial Focus";
	}
	/**
	 * @see IDialogTestPass#description()
	 */
	public String description() {
		return "Verify the initial focus of the dialogs.";
	}
	/**
	 * @see IDialogTestPass#label()
	 */
	public String label() {
		return "&Initial Focus";
	}
	/**
	 * @see IDialogTestPass#checkListTexts()
	 */
	public ArrayList checkListTexts() {
		ArrayList list = new ArrayList(CHECKLIST_SIZE);
		list.add("&1) the initial focus is appropriate.");
		return list;
	}
	/**
	 * @see IDialogTestPass#failureTexts()
	 * Size of the return array must be the same size as the checkListTexts'
	 * ArrayList.
	 */
	public String[] failureTexts() {
		String[] failureText = new String[CHECKLIST_SIZE];
		failureText[0] = "The initial focus is inappropriate.";
		return failureText;
	}
	/**
	 * @see IDialogTestPass#queryText()
	 */
	public String queryText() {
		return "Is the initial focus of the dialog correct?";
	}
	/**
	 * @see IDialogTestPass#getID()
	 */
	public int getID() {
		return VerifyDialog.TEST_FOCUS;
	}
}
