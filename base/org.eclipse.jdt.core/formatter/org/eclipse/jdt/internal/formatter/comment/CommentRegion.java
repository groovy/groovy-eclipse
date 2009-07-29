/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.formatter.comment;

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.core.runtime.Assert;

import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;

import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;
import org.eclipse.jdt.internal.formatter.CodeFormatterVisitor;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;
import org.eclipse.jdt.internal.formatter.Scribe;

/**
 * Comment region in a source code document.
 * 
 * @since 3.0
 */
public class CommentRegion extends Position implements IHtmlTagDelimiters, IBorderAttributes, ICommentAttributes {

	/** Default comment range delimiter */
	protected static final String COMMENT_RANGE_DELIMITER= " "; //$NON-NLS-1$

	/** Default line prefix length */
	private static final int COMMENT_PREFIX_LENGTH= 3;

	/** The borders of this region */
	private int fBorders= 0;

	/** Should all blank lines be cleared during formatting? */
	protected boolean fClear;

	/** The line delimiter used in this comment region */
	private final String fDelimiter;

	/** The document to format */
	private final IDocument fDocument;

	/** The lines in this comment region */
	private final LinkedList fLines= new LinkedList();
	
	/** The formatting preferences */
	protected final DefaultCodeFormatterOptions preferences;
	
	/** The comment ranges in this comment region */
	private final LinkedList fRanges= new LinkedList();

	/** Is this comment region a single line region? */
	private final boolean fSingleLine;

	/** Number of spaces representing tabulator */
	private int fTabSize;

	/** the scribe used to create edits */
	protected Scribe scribe;

	/**
	 * Creates a new comment region.
	 * 
	 * @param document the document which contains the comment region
	 * @param position the position of this comment region in the document
	 * @param formatter the given code formatter
	 */
	public CommentRegion(final IDocument document, final Position position, final CodeFormatterVisitor formatter) {
		super(position.getOffset(), position.getLength());

		this.preferences = formatter.preferences;
		fDelimiter = this.preferences.line_separator;
		fDocument= document;
		
		fTabSize= DefaultCodeFormatterOptions.SPACE == this.preferences.tab_char ? this.preferences.indentation_size : this.preferences.tab_size;

		this.scribe = formatter.scribe;

		final ILineTracker tracker= new DefaultLineTracker();

		IRegion range= null;
		CommentLine line= null;

		tracker.set(getText(0, getLength()));
		final int lines= tracker.getNumberOfLines();

		fSingleLine= lines == 1;

		try {

			for (int index= 0; index < lines; index++) {

				range= tracker.getLineInformation(index);
				line= createLine();
				line.append(new CommentRange(range.getOffset(), range.getLength()));

				fLines.add(line);
			}

		} catch (BadLocationException exception) {
			// Should not happen
		}
	}

	/**
	 * Appends the comment range to this comment region.
	 * 
	 * @param range comment range to append to this comment region
	 */
	protected final void append(final CommentRange range) {
		fRanges.addLast(range);
	}

	/**
	 * Can the comment range be appended to the comment line?
	 * 
	 * @param line comment line where to append the comment range
	 * @param previous comment range which is the predecessor of the current
	 *                comment range
	 * @param next comment range to test whether it can be appended to the
	 *                comment line
	 * @param index amount of space in the comment line used by already
	 *                inserted comment ranges
	 * @param width the maximal width of text in this comment region
	 *                measured in average character widths
	 * @return <code>true</code> iff the comment range can be added to the
	 *         line, <code>false</code> otherwise
	 */
	protected boolean canAppend(final CommentLine line, final CommentRange previous, final CommentRange next, final int index, final int width) {
		return index == 0 || index + next.getLength() <= width;
	}

	/**
	 * Can the whitespace between the two comment ranges be formatted?
	 * 
	 * @param previous previous comment range which was already formatted,
	 *                can be <code>null</code>
	 * @param next next comment range to be formatted
	 * @return <code>true</code> iff the next comment range can be
	 *         formatted, <code>false</code> otherwise.
	 */
	protected boolean canFormat(final CommentRange previous, final CommentRange next) {
		return previous != null;
	}

	/**
	 * Formats the comment region with the given indentation level.
	 * 
	 * @param indentationLevel the indentation level
	 * @return the resulting text edit of the formatting process
	 * @since 3.1
	 */
	public final TextEdit format(int indentationLevel, boolean returnEdit) {
		final String probe= getText(0, CommentLine.NON_FORMAT_START_PREFIX.length());
		if (!probe.startsWith(CommentLine.NON_FORMAT_START_PREFIX)) {

			int margin= this.preferences.comment_line_length;
			String indentation= computeIndentation(indentationLevel);
			margin= Math.max(COMMENT_PREFIX_LENGTH + 1, margin - stringToLength(indentation) - COMMENT_PREFIX_LENGTH);

			tokenizeRegion();
			markRegion();
			wrapRegion(margin);
			formatRegion(indentation, margin);

		}
		if (returnEdit) {
			return this.scribe.getRootEdit();
		}
		return null;
	}

	/**
	 * Formats this comment region.
	 * 
	 * @param indentation the indentation of this comment region
	 * @param width the maximal width of text in this comment region
	 *                measured in average character widths
	 */
	protected void formatRegion(final String indentation, final int width) {

		final int last= fLines.size() - 1;
		if (last >= 0) {

			CommentLine lastLine= (CommentLine)fLines.get(last);
			CommentRange lastRange= lastLine.getLast();
			lastLine.formatLowerBorder(lastRange, indentation, width);

			CommentLine previous;
			CommentLine next= null;
			CommentRange range= null;
			for (int line= last; line >= 0; line--) {

				previous= next;
				next= (CommentLine)fLines.get(line);

				range= next.formatLine(previous, range, indentation, line);
			}
			next.formatUpperBorder(range, indentation, width);
		}
	}

	/**
	 * Returns the line delimiter used in this comment region.
	 * 
	 * @return the line delimiter for this comment region
	 */
	protected final String getDelimiter() {
		return fDelimiter;
	}

	/**
	 * Returns the line delimiter used in this comment line break.
	 * 
	 * @param predecessor the predecessor comment line after the line break
	 * @param successor the successor comment line before the line break
	 * @param previous the comment range after the line break
	 * @param next the comment range before the line break
	 * @param indentation indentation of the formatted line break
	 * @return the line delimiter for this comment line break
	 */
	protected String getDelimiter(final CommentLine predecessor, final CommentLine successor, final CommentRange previous, final CommentRange next, final String indentation) {
		return fDelimiter + indentation + successor.getContentPrefix();
	}

	/**
	 * Returns the range delimiter for this comment range break.
	 * 
	 * @param previous the previous comment range to the right of the range
	 *                delimiter
	 * @param next the next comment range to the left of the range delimiter
	 * @return the delimiter for this comment range break
	 */
	protected String getDelimiter(final CommentRange previous, final CommentRange next) {
		return COMMENT_RANGE_DELIMITER;
	}

	/**
	 * Returns the document of this comment region.
	 * 
	 * @return the document of this region
	 */
	protected final IDocument getDocument() {
		return fDocument;
	}

	/**
	 * Returns the comment ranges in this comment region
	 * 
	 * @return the comment ranges in this region
	 */
	protected final LinkedList getRanges() {
		return fRanges;
	}

	/**
	 * Returns the number of comment lines in this comment region.
	 * 
	 * @return the number of lines in this comment region
	 */
	protected final int getSize() {
		return fLines.size();
	}

	/**
	 * Returns the text of this comment region in the indicated range.
	 * 
	 * @param position the offset of the comment range to retrieve in
	 *                comment region coordinates
	 * @param count the length of the comment range to retrieve
	 * @return the content of this comment region in the indicated range
	 */
	protected final String getText(final int position, final int count) {

		String content= ""; //$NON-NLS-1$
		try {
			content= fDocument.get(getOffset() + position, count);
		} catch (BadLocationException exception) {
			// Should not happen
		}
		return content;
	}

	/**
	 * Does the border <code>border</code> exist?
	 * 
	 * @param border the type of the border, must be a border attribute of
	 *                <code>CommentRegion</code>
	 * @return <code>true</code> iff this border exists,
	 *         <code>false</code> otherwise
	 */
	protected final boolean hasBorder(final int border) {
		return (fBorders & border) == border;
	}

	/**
	 * Does the comment range consist of letters and digits only?
	 * 
	 * @param range the comment range to text
	 * @return <code>true</code> iff the comment range consists of letters
	 *         and digits only, <code>false</code> otherwise
	 */
	protected final boolean isAlphaNumeric(final CommentRange range) {

		final String token= getText(range.getOffset(), range.getLength());

		for (int index= 0; index < token.length(); index++) {
			if (!ScannerHelper.isLetterOrDigit(token.charAt(index)))
				return false;
		}
		return true;
	}

	/**
	 * Does the comment range contain no letters and digits?
	 * 
	 * @param range the comment range to text
	 * @return <code>true</code> iff the comment range contains no letters
	 *         and digits, <code>false</code> otherwise
	 */
	protected final boolean isNonAlphaNumeric(final CommentRange range) {

		final String token= getText(range.getOffset(), range.getLength());

		for (int index= 0; index < token.length(); index++) {
			if (ScannerHelper.isLetterOrDigit(token.charAt(index)))
				return false;
		}
		return true;
	}

	/**
	 * Should blank lines be cleared during formatting?
	 * 
	 * @return <code>true</code> iff blank lines should be cleared,
	 *         <code>false</code> otherwise
	 */
	protected final boolean isClearLines() {
		return fClear;
	}

	/**
	 * Is this comment region a single line region?
	 * 
	 * @return <code>true</code> iff this region is single line,
	 *         <code>false</code> otherwise
	 */
	protected final boolean isSingleLine() {
		return fSingleLine;
	}

	/**
	 * Logs a text edit operation occurred during the formatting process
	 * 
	 * @param change the changed text
	 * @param position offset measured in comment region coordinates where
	 *                to apply the changed text
	 * @param count length of the range where to apply the changed text
	 */
	protected final void logEdit(final String change, final int position, final int count) {
		try {
			final int base= getOffset() + position;
			final String content= fDocument.get(base, count);

			if (!change.equals(content)) {
				if (count > 0) {
					this.scribe.addReplaceEdit(base, base + count - 1, change);
				} else {
					this.scribe.addInsertEdit(base, change);
				}
			}
		} catch (BadLocationException exception) {
			// Should not happen
			CommentFormatterUtil.log(exception);
		} catch (MalformedTreeException exception) {
			// Do nothing
			CommentFormatterUtil.log(exception);
		}
	}

	/**
	 * Marks the comment ranges in this comment region.
	 */
	protected void markRegion() {
		// Do nothing
	}

	/**
	 * Set the border type <code>border</code> to true.
	 * 
	 * @param border the type of the border. Must be a border attribute of
	 *                <code>CommentRegion</code>
	 */
	protected final void setBorder(final int border) {
		fBorders |= border;
	}

	/**
	 * Returns the indentation of the given indentation level.
	 * 
	 * @param indentationLevel the indentation level
	 * @return the indentation of the given indentation level
	 * @since 3.1
	 */
	private String computeIndentation(int indentationLevel) {
		if (DefaultCodeFormatterOptions.TAB == this.preferences.tab_char)
			return replicate("\t", indentationLevel); //$NON-NLS-1$

		if (DefaultCodeFormatterOptions.SPACE == this.preferences.tab_char)
			return replicate(" ", indentationLevel * this.preferences.tab_size); //$NON-NLS-1$
		
		if (DefaultCodeFormatterOptions.MIXED == this.preferences.tab_char) {
			int tabSize= this.preferences.tab_size;
			int indentSize= this.preferences.indentation_size;
			int spaceEquivalents= indentationLevel * indentSize;
			return replicate("\t", spaceEquivalents / tabSize) + replicate(" ", spaceEquivalents % tabSize); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		Assert.isTrue(false);
		return null;
	}
	
	/**
	 * Returns the given string n-times replicated.
	 * 
	 * @param string the string
	 * @param n n
	 * @return the given string n-times replicated
	 * @since 3.1
	 */
	private String replicate(String string, int n) {
		StringBuffer buffer= new StringBuffer(n*string.length());
		for (int i= 0; i < n; i++)
			buffer.append(string);
		return buffer.toString();
	}

	/**
	 * Computes the equivalent indentation for a string
	 * 
	 * @param reference the string to compute the indentation for
	 * @return the indentation string
	 */
	protected final String stringToIndent(final String reference) {
		return replicate(" ", stringToLength(reference)); //$NON-NLS-1$
	}

	/**
	 * Returns the length of the string in expanded characters.
	 * 
	 * @param reference the string to get the length for
	 * @return the length of the string in expanded characters
	 */
	protected final int stringToLength(final String reference) {
		return expandTabs(reference).length();
	}

	/**
	 * Expands the given string's tabs according to the given tab size.
	 * 
	 * @param string the string
	 * @return the expanded string
	 * @since 3.1
	 */
	private String expandTabs(String string) {
		StringBuffer expanded= new StringBuffer();
		for (int i= 0, n= string.length(), chars= 0; i < n; i++) {
			char ch= string.charAt(i);
			if (ch == '\t') {
				for (; chars < fTabSize; chars++)
					expanded.append(' ');
				chars= 0;
			} else {
				expanded.append(ch);
				chars++;
				if (chars >= fTabSize)
					chars= 0;
			}
		
		}
		return expanded.toString();
	}

	/**
	 * Tokenizes the comment region.
	 */
	protected void tokenizeRegion() {

		int index= 0;
		CommentLine line= null;

		for (final Iterator iterator= fLines.iterator(); iterator.hasNext(); index++) {

			line= (CommentLine)iterator.next();

			line.scanLine(index);
			line.tokenizeLine(index);
		}
	}

	/**
	 * Wraps the comment ranges in this comment region into comment lines.
	 * 
	 * @param width the maximal width of text in this comment region
	 *                measured in average character widths
	 */
	protected void wrapRegion(final int width) {

		fLines.clear();

		int index= 0;
		boolean adapted= false;

		CommentLine successor= null;
		CommentLine predecessor= null;

		CommentRange previous= null;
		CommentRange next= null;

		while (!fRanges.isEmpty()) {

			index= 0;
			adapted= false;

			predecessor= successor;
			successor= createLine();
			fLines.add(successor);

			while (!fRanges.isEmpty()) {
				next= (CommentRange)fRanges.getFirst();

				if (canAppend(successor, previous, next, index, width)) {

					if (!adapted && predecessor != null) {

						successor.adapt(predecessor);
						adapted= true;
					}

					fRanges.removeFirst();
					successor.append(next);

					index += (next.getLength() + 1);
					previous= next;
				} else
					break;
			}
		}
	}

	/**
	 * Creates a new line for this region.
	 * 
	 * @return a new line for this region
	 * @since 3.1
	 */
	protected CommentLine createLine() {
		return new SingleCommentLine(this);
	}
}
