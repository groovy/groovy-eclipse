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

import java.util.LinkedList;

import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;

/**
 * General comment line in a comment region.
 *
 * @since 3.0
 */
public abstract class CommentLine implements IBorderAttributes {

	/** Prefix of non-formattable comment lines */
	protected static final String NON_FORMAT_START_PREFIX= "/*-"; //$NON-NLS-1$

	/** The attributes of this line */
	private int fAttributes= 0;

	/** The parent region of this line */
	private final CommentRegion fParent;

	/** The comment ranges in this line */
	private final LinkedList fRanges= new LinkedList();

	/**
	 * Creates a new comment line.
	 *
	 * @param parent comment region to create the comment line for
	 */
	protected CommentLine(final CommentRegion parent) {
		this.fParent= parent;
	}

	/**
	 * Adapts the line attributes from the previous line in the comment
	 * region.
	 *
	 * @param previous the previous comment line in the comment region
	 */
	protected abstract void adapt(final CommentLine previous);

	/**
	 * Appends the specified comment range to this comment line.
	 *
	 * @param range comment range to append to this line
	 */
	protected void append(final CommentRange range) {
		this.fRanges.add(range);
	}

	/**
	 * Formats this comment line as content line.
	 *
	 * @param predecessor the predecessor comment line in the comment region
	 * @param last the most recently processed comment range
	 * @param indentation the indentation of the comment region
	 * @param line the index of this comment line in the comment region
	 * @return the first comment range in this comment line
	 */
	protected CommentRange formatLine(final CommentLine predecessor, final CommentRange last, final String indentation, final int line) {

		int offset= 0;
		int length= 0;

		CommentRange next= last;
		CommentRange previous= null;

		final int stop= this.fRanges.size() - 1;
		final int end= this.fParent.getSize() - 1;

		for (int index= stop; index >= 0; index--) {

			previous= next;
			next= (CommentRange)this.fRanges.get(index);

			if (this.fParent.canFormat(previous, next)) {

				offset= next.getOffset() + next.getLength();
				length= previous.getOffset() - offset;

				if (index == stop && line != end)
					this.fParent.logEdit(this.fParent.getDelimiter(predecessor, this, previous, next, indentation), offset, length);
				else
					this.fParent.logEdit(this.fParent.getDelimiter(previous, next), offset, length);
			}
		}
		return next;
	}

	/**
	 * Formats this comment line as end line having a lower border
	 * consisting of content line prefixes.
	 *
	 * @param range last comment range of the last comment line in the
	 *                comment region
	 * @param indentation the indentation of the comment region
	 * @param length the maximal length of text in this comment region
	 *                measured in average character widths
	 */
	protected void formatLowerBorder(final CommentRange range, final String indentation, final int length) {

		final int offset= range.getOffset() + range.getLength();

		final StringBuffer buffer= new StringBuffer(length);
		final String end= getEndingPrefix();
		final String delimiter= this.fParent.getDelimiter();

		if (this.fParent.isSingleLine() && this.fParent.getSize() == 1)
			buffer.append(end);
		else {

			final String filler= getContentPrefix().trim();

			buffer.append(delimiter);
			buffer.append(indentation);

			if (this.fParent.hasBorder(BORDER_LOWER)) {

				buffer.append(' ');
				for (int character= 0; character < length; character++)
					buffer.append(filler);

				buffer.append(end.trim());

			} else
				buffer.append(end);
		}
		this.fParent.logEdit(buffer.toString(), offset, this.fParent.getLength() - offset);
	}

	/**
	 * Formats this comment line as start line having an upper border
	 * consisting of content line prefixes.
	 *
	 * @param range the first comment range in the comment region
	 * @param indentation the indentation of the comment region
	 * @param length the maximal length of text in this comment region
	 *                measured in average character widths
	 */
	protected void formatUpperBorder(final CommentRange range, final String indentation, final int length) {

		final StringBuffer buffer= new StringBuffer(length);
		final String start= getStartingPrefix();
		final String content= getContentPrefix();

		if (this.fParent.isSingleLine() && this.fParent.getSize() == 1)
			buffer.append(start);
		else {

			final String trimmed= start.trim();
			final String filler= content.trim();

			buffer.append(trimmed);

			if (this.fParent.hasBorder(BORDER_UPPER)) {

				for (int character= 0; character < length - trimmed.length() + start.length(); character++)
					buffer.append(filler);
			}

			buffer.append(this.fParent.getDelimiter());
			buffer.append(indentation);
			buffer.append(content);
		}
		this.fParent.logEdit(buffer.toString(), 0, range.getOffset());
	}

	/**
	 * Returns the line prefix of content lines.
	 *
	 * @return line prefix of content lines
	 */
	protected abstract String getContentPrefix();

	/**
	 * Returns the line prefix of end lines.
	 *
	 * @return line prefix of end lines
	 */
	protected abstract String getEndingPrefix();

	/**
	 * Returns the first comment range in this comment line.
	 *
	 * @return the first comment range
	 */
	protected final CommentRange getFirst() {
		return (CommentRange)this.fRanges.getFirst();
	}

	/**
	 * Returns the indentation reference string for this line.
	 *
	 * @return the indentation reference string for this line
	 */
	protected String getIndentationReference() {
		return ""; //$NON-NLS-1$
	}

	/**
	 * Returns the last comment range in this comment line.
	 *
	 * @return the last comment range
	 */
	protected final CommentRange getLast() {
		return (CommentRange)this.fRanges.getLast();
	}

	/**
	 * Returns the parent comment region of this comment line.
	 *
	 * @return the parent comment region
	 */
	protected final CommentRegion getParent() {
		return this.fParent;
	}

	/**
	 * Returns the number of comment ranges in this comment line.
	 *
	 * @return the number of ranges in this line
	 */
	protected final int getSize() {
		return this.fRanges.size();
	}

	/**
	 * Returns the line prefix of start lines.
	 *
	 * @return line prefix of start lines
	 */
	protected abstract String getStartingPrefix();

	/**
	 * Is the attribute <code>attribute</code> true?
	 *
	 * @param attribute the attribute to get.
	 * @return <code>true</code> iff this attribute is <code>true</code>,
	 *         <code>false</code> otherwise.
	 */
	protected final boolean hasAttribute(final int attribute) {
		return (this.fAttributes & attribute) == attribute;
	}

	/**
	 * Scans this comment line for comment range boundaries.
	 *
	 * @param line the index of this line in the comment region
	 */
	protected abstract void scanLine(final int line);

	/**
	 * Set the attribute <code>attribute</code> to true.
	 *
	 * @param attribute the attribute to set.
	 */
	protected final void setAttribute(final int attribute) {
		this.fAttributes |= attribute;
	}

	/**
	 * Tokenizes this comment line into comment ranges
	 *
	 * @param line the index of this line in the comment region
	 */
	protected void tokenizeLine(final int line) {

		int offset= 0;
		int index= offset;

		final CommentRange range= (CommentRange)this.fRanges.get(0);
		final int begin= range.getOffset();

		final String content= this.fParent.getText(begin, range.getLength());
		final int length= content.length();

		while (offset < length) {

			while (offset < length && ScannerHelper.isWhitespace(content.charAt(offset)))
				offset++;

			index= offset;

			while (index < length && !ScannerHelper.isWhitespace(content.charAt(index)))
				index++;

			if (index - offset > 0) {
				this.fParent.append(new CommentRange(begin + offset, index - offset));

				offset= index;
			}
		}
	}

	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		final int size = this.fRanges.size();
		for (int i = 0; i < size; i++) {
			buffer.append(this.fRanges.get(i)).append("\n"); //$NON-NLS-1$
		}
		return String.valueOf(buffer);
	}
}
