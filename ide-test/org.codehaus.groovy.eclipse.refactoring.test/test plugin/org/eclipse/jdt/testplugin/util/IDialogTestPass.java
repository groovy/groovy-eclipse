/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
 * Interface to describe a visual test pass for a dialog test.
 */
public interface IDialogTestPass {
	/*
	 * @return String The title of the test pass.
	 */
	public String title();
	/*
	 * @return String The description of the test pass.
	 */
	public String description();
	/*
	 * @return String The label of the test pass to be used
	 * in a selection list.  The return includes an '&'
	 * if a mnemonic is desired.
	 */
	public String label();
	/*
	 * @return ArrayList A list of items to appear in a checklist.
	 * The items in the list must be Strings and should include an
	 * '&' if a mnemonic is desired.
	 */
	public ArrayList checkListTexts();
	/*
	 * @return String[] Associated failure messages that correspond
	 * to the checklist items.  The size of this array should be the
	 * same size as the checklist.
	 */
	public String[] failureTexts();
	/*
	 * @return String The test that corresponds to the test pass to
	 * which the tester will respond with a 'yes' or 'no'.
	 */
	public String queryText();
	/*
	 * @return int A unique number that identifies the test pass.
	 */
	public int getID();
}


