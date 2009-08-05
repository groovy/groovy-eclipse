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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jface.text.Position;

/**
 * Range in a comment region in comment region coordinates.
 *
 * @since 3.0
 */
public class CommentRange extends Position implements ICommentAttributes, IHtmlTagDelimiters {

	/** The attributes of this range */
	private int fAttributes= 0;

	/**
	 * Creates a new comment range.
	 *
	 * @param position offset of the range
	 * @param count length of the range
	 */
	public CommentRange(final int position, final int count) {
		super(position, count);
	}

	/**
	 * Is the attribute <code>attribute</code> true?
	 *
	 * @param attribute the attribute to get
	 * @return <code>true</code> iff this attribute is <code>true</code>,
	 *         <code>false</code> otherwise
	 */
	protected final boolean hasAttribute(final int attribute) {
		return (this.fAttributes & attribute) == attribute;
	}

	/**
	 * Does this comment range contain a closing HTML tag?
	 *
	 * @param token token belonging to the comment range
	 * @param tag the HTML tag to check
	 * @return <code>true</code> iff this comment range contains a closing
	 *         html tag, <code>false</code> otherwise
	 */
	protected final boolean isClosingTag(final char[] token, final char[] tag) {

		boolean result= (CharOperation.indexOf(HTML_CLOSE_PREFIX, token, false) == 0)
				&& token[token.length - 1] == HTML_TAG_POSTFIX;
		if (result) {

			setAttribute(COMMENT_CLOSE);
			result= CharOperation.equals(tag, token, HTML_CLOSE_PREFIX.length, token.length - 1, false);
		}
		return result;
	}

	/**
	 * Does this comment range contain an opening HTML tag?
	 *
	 * @param token token belonging to the comment range
	 * @param tag the HTML tag to check
	 * @return <code>true</code> iff this comment range contains an
	 *         opening html tag, <code>false</code> otherwise
	 */
	protected final boolean isOpeningTag(final char[] token, final char[] tag) {

		boolean result= token.length > 0
				&& token[0] == HTML_TAG_PREFIX
				&& (CharOperation.indexOf(HTML_CLOSE_PREFIX, token, false) != 0)
				&& token[token.length - 1] == HTML_TAG_POSTFIX;
		if (result) {

			setAttribute(COMMENT_OPEN);
			result= CharOperation.indexOf(tag, token, false) == 1;
		}
		return result;
	}

	/**
	 * Mark the comment range with the occurred HTML tags.
	 *
	 * @param tags the HTML tags to test for their occurrence
	 * @param token token belonging to the comment range
	 * @param attribute attribute to set if a HTML tag is present
	 * @param open <code>true</code> iff opening tags should be marked,
	 *                <code>false</code> otherwise
	 * @param close <code>true</code> iff closing tags should be marked,
	 *                <code>false</code> otherwise
	 */
	protected final void markHtmlTag(final char[][] tags, final char[] token, final int attribute, final boolean open, final boolean close) {
		if (token[0] == HTML_TAG_PREFIX && token[token.length - 1] == HTML_TAG_POSTFIX) {

			char[] tag= null;
			boolean isOpen= false;
			boolean isClose= false;

			for (int index= 0; index < tags.length; index++) {

				tag= tags[index];

				isOpen= isOpeningTag(token, tag);
				isClose= isClosingTag(token, tag);

				if ((open && isOpen) || (close && isClose)) {

					setAttribute(attribute);
					break;
				}
			}
		}
	}

	/**
	 * Mark the comment range with the occurred tags.
	 *
	 * @param tags the tags to test for their occurrence
	 * @param prefix the prefix which is common to all the tags to test
	 * @param token the token belonging to the comment range
	 * @param attribute attribute to set if a tag is present
	 */
	protected final void markPrefixTag(final char[][] tags, final char prefix, final char[] token, final int attribute) {

		if (token[0] == prefix) {

			char[] tag= null;
			for (int index= 0; index < tags.length; index++) {

				tag= tags[index];
				if (CharOperation.equals(token, tag)) {

					setAttribute(attribute);
					break;
				}
			}
		}
	}

	/**
	 * Marks the comment range with the HTML range tag.
	 *
	 * @param token the token belonging to the comment range
	 * @param tag the HTML tag which confines the HTML range
	 * @param level the nesting level of the current HTML range
	 * @param key the key of the attribute to set if the comment range is in
	 *                the HTML range
	 * @param html <code>true</code> iff the HTML tags in this HTML range
	 *                should be marked too, <code>false</code> otherwise
	 * @return the new nesting level of the HTML range
	 */
	protected final int markTagRange(final char[] token, final char[] tag, int level, final int key, final boolean html) {

		if (isOpeningTag(token, tag)) {
			if (level++ > 0)
				setAttribute(key);
		} else if (isClosingTag(token, tag)) {
			if (--level > 0)
				setAttribute(key);
		} else if (level > 0) {
			if (html || !hasAttribute(COMMENT_HTML))
				setAttribute(key);
		}
		return level;
	}

	/**
	 * Moves this comment range.
	 *
	 * @param delta the delta to move the range
	 */
	public final void move(final int delta) {
		this.offset += delta;
	}

	/**
	 * Set the attribute <code>attribute</code> to true.
	 *
	 * @param attribute the attribute to set.
	 */
	protected final void setAttribute(final int attribute) {
		this.fAttributes |= attribute;
	}

	/**
	 * Trims this comment range at the beginning.
	 *
	 * @param delta amount to trim the range
	 */
	public final void trimBegin(final int delta) {
		this.offset += delta;
		this.length -= delta;
	}

	/**
	 * Trims this comment range at the end.
	 *
	 * @param delta amount to trim the range
	 */
	public final void trimEnd(final int delta) {
		this.length += delta;
	}

	/*
	 * @see java.lang.Object#toString()
	 * @since 3.1
	 */
	public String toString() {
		List attributes= new ArrayList();
		if (hasAttribute(COMMENT_BLANKLINE))
			attributes.add("COMMENT_BLANKLINE"); //$NON-NLS-1$
		if (hasAttribute(COMMENT_BREAK))
			attributes.add("COMMENT_BREAK"); //$NON-NLS-1$
		if (hasAttribute(COMMENT_CLOSE))
			attributes.add("COMMENT_CLOSE"); //$NON-NLS-1$
		if (hasAttribute(COMMENT_CODE))
			attributes.add("COMMENT_CODE"); //$NON-NLS-1$
		if (hasAttribute(COMMENT_HTML))
			attributes.add("COMMENT_HTML"); //$NON-NLS-1$
		if (hasAttribute(COMMENT_IMMUTABLE))
			attributes.add("COMMENT_IMMUTABLE"); //$NON-NLS-1$
		if (hasAttribute(COMMENT_NEWLINE))
			attributes.add("COMMENT_NEWLINE"); //$NON-NLS-1$
		if (hasAttribute(COMMENT_OPEN))
			attributes.add("COMMENT_OPEN"); //$NON-NLS-1$
		if (hasAttribute(COMMENT_PARAGRAPH))
			attributes.add("COMMENT_PARAGRAPH"); //$NON-NLS-1$
		if (hasAttribute(COMMENT_PARAMETER))
			attributes.add("COMMENT_PARAMETER"); //$NON-NLS-1$
		if (hasAttribute(COMMENT_ROOT))
			attributes.add("COMMENT_ROOT"); //$NON-NLS-1$
		if (hasAttribute(COMMENT_SEPARATOR))
			attributes.add("COMMENT_SEPARATOR"); //$NON-NLS-1$
		if (hasAttribute(COMMENT_FIRST_TOKEN))
			attributes.add("COMMENT_FIRST_TOKEN"); //$NON-NLS-1$
		if (hasAttribute(COMMENT_STARTS_WITH_RANGE_DELIMITER))
			attributes.add("COMMENT_STARTS_WITH_RANGE_DELIMITER"); //$NON-NLS-1$

		StringBuffer buf= new StringBuffer("CommentRange [" + this.offset + "+" + this.length + "] {"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		for (Iterator it= attributes.iterator(); it.hasNext();) {
			String string= (String) it.next();
			buf.append(string);
			if (it.hasNext())
				buf.append(", "); //$NON-NLS-1$
		}

		return buf.toString() + "}"; //$NON-NLS-1$
	}
}
