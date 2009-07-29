/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.formatter.comment;

/**
 * Javadoc comment line in a comment region.
 * 
 * @since 3.0
 */
public class JavaDocLine extends MultiCommentLine {

	/** Line prefix of javadoc start lines */
	public static final String JAVADOC_START_PREFIX= "/**"; //$NON-NLS-1$

	/**
	 * Creates a new javadoc line.
	 * 
	 * @param region comment region to create the line for
	 */
	protected JavaDocLine(final CommentRegion region) {
		super(region);
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.text.comment.CommentLine#formatUpperBorder(org.eclipse.jdt.internal.corext.text.comment.CommentRange, java.lang.String, int)
	 */
	protected void formatUpperBorder(final CommentRange range, final String indentation, final int length) {

		final CommentRegion parent= getParent();

		if (parent.isSingleLine() && parent.getSize() == 1) {
			parent.logEdit(getStartingPrefix() + CommentRegion.COMMENT_RANGE_DELIMITER, 0, range.getOffset());
		} else
			super.formatUpperBorder(range, indentation, length);
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.text.comment.CommentLine#getStartingPrefix()
	 */
	protected String getStartingPrefix() {
		return JAVADOC_START_PREFIX;
	}
}
