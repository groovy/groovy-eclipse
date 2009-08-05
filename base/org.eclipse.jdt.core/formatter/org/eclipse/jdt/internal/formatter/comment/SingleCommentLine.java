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

/**
 * Single-line comment line in a comment region.
 *
 * @since 3.0
 */
public class SingleCommentLine extends CommentLine {

	/** Line prefix for single line comments */
	public static final String SINGLE_COMMENT_PREFIX= "// "; //$NON-NLS-1$

	/** NLS tag prefix */
	private static final String NLS_TAG_PREFIX= "//$NON-NLS-"; //$NON-NLS-1$

	/** Is the comment a NLS locale tag sequence? */
	private boolean fLocaleSequence= false;

	/**
	 * Creates a new single-line comment line.
	 *
	 * @param region comment region to create the line for
	 */
	protected SingleCommentLine(final CommentRegion region) {
		super(region);
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.text.comment.CommentLine#adapt(org.eclipse.jdt.internal.corext.text.comment.CommentLine)
	 */
	protected void adapt(final CommentLine previous) {
		// Do nothing
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.text.comment.CommentLine#formatLowerBorder(org.eclipse.jdt.internal.corext.text.comment.CommentRange, java.lang.String, int)
	 */
	protected void formatLowerBorder(final CommentRange range, final String indentation, final int length) {

		final int offset= range.getOffset() + range.getLength();
		final CommentRegion parent= getParent();

		parent.logEdit(parent.getDelimiter(), offset, parent.getLength() - offset);
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.text.comment.CommentLine#formatUpperBorder(org.eclipse.jdt.internal.corext.text.comment.CommentRange, java.lang.String, int)
	 */
	protected void formatUpperBorder(final CommentRange range, final String indentation, final int length) {

		final CommentRegion parent= getParent();

		parent.logEdit(getContentPrefix(), 0, range.getOffset());
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.text.comment.CommentLine#getContentPrefix()
	 */
	protected String getContentPrefix() {
		return SINGLE_COMMENT_PREFIX;
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.text.comment.CommentLine#getEndingPrefix()
	 */
	protected String getEndingPrefix() {
		return SINGLE_COMMENT_PREFIX;
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.text.comment.CommentLine#getStartingPrefix()
	 */
	protected String getStartingPrefix() {
		return SINGLE_COMMENT_PREFIX;
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.text.comment.CommentLine#scanLine(int)
	 */
	protected void scanLine(final int line) {

		final CommentRange range= getFirst();
		final String content= getParent().getText(range.getOffset(), range.getLength());
		final String prefix= getContentPrefix().trim();

		final int offset= content.indexOf(prefix);
		if (offset >= 0) {

			if (content.startsWith(NLS_TAG_PREFIX))
				this.fLocaleSequence= true;

			range.trimBegin(offset + prefix.length());
		}
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.text.comment.CommentLine#tokenizeLine(int)
	 */
	protected void tokenizeLine(final int line) {

		if (!this.fLocaleSequence)
			super.tokenizeLine(line);
	}
}
