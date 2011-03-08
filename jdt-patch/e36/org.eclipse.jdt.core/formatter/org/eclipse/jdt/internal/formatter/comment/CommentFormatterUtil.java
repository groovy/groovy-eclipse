/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter.comment;

import java.util.Map;

import org.eclipse.text.edits.TextEdit;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Position;

import org.eclipse.jdt.core.ToolFactory;

import org.eclipse.jdt.internal.core.util.Util;

/**
 * Comment formatting utils.
 *
 * @since 3.1
 */
public class CommentFormatterUtil {

	/**
	 * Evaluates the edit on the given string.
	 *
	 * @throws IllegalArgumentException if the positions are not inside the
	 *                 string
	 */
	public static String evaluateFormatterEdit(String string, TextEdit edit, Position[] positions) {
		try {
			Document doc= createDocument(string, positions);
			edit.apply(doc, 0);
			if (positions != null) {
				for (int i= 0; i < positions.length; i++) {
					Assert.isTrue(!positions[i].isDeleted, "Position got deleted"); //$NON-NLS-1$
				}
			}
			return doc.get();
		} catch (BadLocationException e) {
			log(e); // bug in the formatter
			Assert.isTrue(false, "Formatter created edits with wrong positions: " + e.getMessage()); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Creates edits that describe how to format the given string. Returns
	 * <code>null</code> if the code could not be formatted for the given
	 * kind.
	 *
	 * @throws IllegalArgumentException if the offset and length are not
	 *                 inside the string
	 */
	public static TextEdit format2(int kind, String string, int indentationLevel, String lineSeparator, Map options) {
		int length= string.length();
		if (0 < 0 || length < 0 || 0 + length > string.length()) {
			throw new IllegalArgumentException("offset or length outside of string. offset: " + 0 + ", length: " + length + ", string size: " + string.length());   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		}
		return ToolFactory.createCodeFormatter(options).format(kind, string, 0, length, indentationLevel, lineSeparator);
	}

	/**
	 * Returns a document with the given content and the given positions
	 * registered with the {@link DefaultPositionUpdater}.
	 *
	 * @param content the content
	 * @param positions the positions
	 * @return the document
	 * @throws IllegalArgumentException
	 */
	private static Document createDocument(String content, Position[] positions) throws IllegalArgumentException {
		Document doc= new Document(content);
		try {
			if (positions != null) {
				final String POS_CATEGORY= "myCategory"; //$NON-NLS-1$

				doc.addPositionCategory(POS_CATEGORY);
				doc.addPositionUpdater(new DefaultPositionUpdater(POS_CATEGORY) {
					protected boolean notDeleted() {
						if (this.fOffset < this.fPosition.offset && (this.fPosition.offset + this.fPosition.length < this.fOffset + this.fLength)) {
							this.fPosition.offset= this.fOffset + this.fLength; // deleted positions: set to end of remove
							return false;
						}
						return true;
					}
				});
				for (int i= 0; i < positions.length; i++) {
					try {
						doc.addPosition(POS_CATEGORY, positions[i]);
					} catch (BadLocationException e) {
						throw new IllegalArgumentException("Position outside of string. offset: " + positions[i].offset + ", length: " + positions[i].length + ", string size: " + content.length());   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
					}
				}
			}
		} catch (BadPositionCategoryException cannotHappen) {
			// can not happen: category is correctly set up
		}
		return doc;
	}

	/**
	 * Logs the given throwable.
	 *
	 * @param t the throwable
	 * @since 3.1
	 */
	public static void log(Throwable t) {
		Util.log(t, "Exception occured while formatting comments"); //$NON-NLS-1$
	}
}
