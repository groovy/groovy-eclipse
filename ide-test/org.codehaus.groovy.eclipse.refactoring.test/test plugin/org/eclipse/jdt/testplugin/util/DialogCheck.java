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




import junit.framework.Assert;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;

import org.eclipse.ui.PlatformUI;


/**
 * A <code>DialogCheck</code> is used test a dialog in
 * various ways.
 * <p>
 * For interactive tests use <code>assertDialog</code>.
 * For automated tests use <code>assert DialogTexts</code>.
 * </p>
 */
public class DialogCheck {
	private DialogCheck() {
	}
	private static VerifyDialog _verifyDialog;


	/**
	 * Asserts that a given dialog is not null and that it passes
	 * certain visual tests.  These tests will be verified manually
	 * by the tester using an input dialog.  Use this assert method
	 * to verify a dialog's sizing, initial focus, or accessiblity.
	 * To ensure that both the input dialog and the test dialog are
	 * accessible by the tester, the getShell() method should be used
	 * when creating the test dialog.
	 *
	 * Example usage:
	 * <code>Dialog dialog = new AboutDialog( DialogCheck.getShell() );
	 * DialogCheck.assertDialog(dialog, this);</code>
	 *
	 * @param dialog the test dialog to be verified.
	 */
	public static void assertDialog(Dialog dialog) {
		Assert.assertNotNull(dialog);
		if (_verifyDialog.getShell() == null) {
			//force the creation of the verify dialog
			getShell();
		}
		if (_verifyDialog.open(dialog) == IDialogConstants.NO_ID) {
			Assert.assertTrue(_verifyDialog.getFailureText(), false);
		}
	}


	/**
	 * Automated test that checks all the labels and buttons of a dialog
	 * to make sure there is enough room to display all the text.  Any
	 * text that wraps is only approximated and is currently not accurate.
	 *
	 * @param dialog the test dialog to be verified.
	 */
	public static void assertDialogTexts(Dialog dialog) {
		Assert.assertNotNull(dialog);
		dialog.setBlockOnOpen(false);
		dialog.open();
		Shell shell = dialog.getShell();
		verifyCompositeText(shell);
		dialog.close();
	}


	/**
	 * This method should be called when creating dialogs to test.  This
	 * ensures that the dialog's parent shell will be that of the
	 * verification dialog.
	 *
	 * @return Shell The shell of the verification dialog to be used as
	 * the parent shell of the test dialog.
	 */
	public static Shell getShell() {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		_verifyDialog = new VerifyDialog(shell);
		_verifyDialog.create();
		return _verifyDialog.getShell();
	}


	/*
	 * Looks at all the child widgets of a given composite and
	 * verifies the text on all labels and widgets.
	 * @param composite The composite to look through
	 */
	private static void verifyCompositeText(Composite composite) {
		Control children[] = composite.getChildren();
		for (int i = 0; i < children.length; i++) {
			try {
				//verify the text if the child is a button
				verifyButtonText((Button) children[i]);
			} catch (ClassCastException exNotButton) {
				try {
					//child is not a button, maybe a label
					verifyLabelText((Label) children[i]);
				} catch (ClassCastException exNotLabel) {
					try {
						//child is not a label, make a recursive call if it is a composite
						verifyCompositeText((Composite) children[i]);
					} catch (ClassCastException exNotComposite) {
						//the child is not a button, label, or composite - ignore it.
					}
				}
			}
		}
	}

	/*
	 * Verifies that a given button is large enough to display its text.
	 * @param button The button to verify,
	 */
	private static void verifyButtonText(Button button) {
		String widget = button.toString();
		Point size = button.getSize();


		//compute the size with no line wrapping
		Point preferred = button.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		//if (size.y/preferred.y) == X, then label spans X lines, so divide
		//the calculated value of preferred.x by X
		if (preferred.y * size.y > 0) {
			preferred.y /= countLines(button.getText()); //check for '\n\'
			if (size.y / preferred.y > 1) {
				preferred.x /= (size.y / preferred.y);
			}
		}


		String message =
			new StringBuffer("Warning: ")
				.append(widget)
				.append("\n\tActual Width -> ")
				.append(size.x)
				.append("\n\tRecommended Width -> ")
				.append(preferred.x)
				.toString();
		if (preferred.x > size.x) {
			//close the dialog
			button.getShell().dispose();
			Assert.assertTrue(message.toString(), false);
		}
	}

	/*
	 * Verifies that a given label is large enough to display its text.
	 * @param label The label to verify,
	 */
	private static void verifyLabelText(Label label) {
		String widget = label.toString();
		Point size = label.getSize();


		//compute the size with no line wrapping
		Point preferred = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		//if (size.y/preferred.y) == X, then label spans X lines, so divide
		//the calculated value of preferred.x by X
		if (preferred.y * size.y > 0) {
			preferred.y /= countLines(label.getText());
			if (size.y / preferred.y > 1) {
				preferred.x /= (size.y / preferred.y);
			}
		}
		String message =
			new StringBuffer("Warning: ")
				.append(widget)
				.append("\n\tActual Width -> ")
				.append(size.x)
				.append("\n\tRecommended Width -> ")
				.append(preferred.x)
				.toString();
		if (preferred.x > size.x) {
			//close the dialog
			label.getShell().dispose();
			Assert.assertTrue(message.toString(), false);
		}
	}

	/*
	 * Counts the number of lines in a given String.
	 * For example, if a string contains one (1) newline character,
	 * a value of two (2) would be returned.
	 * @param text The string to look through.
	 * @return int the number of lines in text.
	 */
	private static int countLines(String text) {
		int newLines = 1;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '\n') {
				newLines++;
			}
		}
		return newLines;
	}
}
