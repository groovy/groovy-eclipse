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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

/**
 * Multi-line comment line in a comment region.
 * 
 * @since 3.0
 */
public class MultiCommentLine extends CommentLine implements ICommentAttributes, IHtmlTagDelimiters, IJavaDocTagConstants {

	/** Line prefix of multi-line comment content lines */
	public static final String MULTI_COMMENT_CONTENT_PREFIX= " * "; //$NON-NLS-1$

	/** Line prefix of multi-line comment end lines */
	public static final String MULTI_COMMENT_END_PREFIX= " */"; //$NON-NLS-1$

	/** Line prefix of multi-line comment content lines */
	public static final String MULTI_COMMENT_START_PREFIX= "/* "; //$NON-NLS-1$

	/** The indentation reference of this line */
	private String fReferenceIndentation= ""; //$NON-NLS-1$
	
	/** The javadoc tag lookup. */
	private static final Set fgTagLookup;
	
	static {
		fgTagLookup= new HashSet();
		for (int i= 0; i < JAVADOC_BREAK_TAGS.length; i++) {
			fgTagLookup.add(new String(JAVADOC_BREAK_TAGS[i]));
		}
		for (int i= 0; i < JAVADOC_SINGLE_BREAK_TAG.length; i++) {
			fgTagLookup.add(new String(JAVADOC_SINGLE_BREAK_TAG[i]));
		}
		for (int i= 0; i < JAVADOC_CODE_TAGS.length; i++) {
			fgTagLookup.add(new String(JAVADOC_CODE_TAGS[i]));
		}
		for (int i= 0; i < JAVADOC_IMMUTABLE_TAGS.length; i++) {
			fgTagLookup.add(new String(JAVADOC_IMMUTABLE_TAGS[i]));
		}
		for (int i= 0; i < JAVADOC_NEWLINE_TAGS.length; i++) {
			fgTagLookup.add(new String(JAVADOC_NEWLINE_TAGS[i]));
		}
		for (int i= 0; i < JAVADOC_SEPARATOR_TAGS.length; i++) {
			fgTagLookup.add(new String(JAVADOC_SEPARATOR_TAGS[i]));
		}
	}

	/**
	 * Creates a new multi-line comment line.
	 * 
	 * @param region comment region to create the line for
	 */
	protected MultiCommentLine(final CommentRegion region) {
		super(region);
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.text.comment.CommentLine#adapt(org.eclipse.jdt.internal.corext.text.comment.CommentLine)
	 */
	protected void adapt(final CommentLine previous) {

		if (!hasAttribute(COMMENT_ROOT) && !hasAttribute(COMMENT_PARAMETER) && !previous.hasAttribute(COMMENT_BLANKLINE))
			fReferenceIndentation= previous.getIndentationReference();
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.text.comment.CommentLine#append(org.eclipse.jdt.internal.corext.text.comment.CommentRange)
	 */
	protected void append(final CommentRange range) {

		final MultiCommentRegion parent= (MultiCommentRegion)getParent();

		if (range.hasAttribute(COMMENT_PARAMETER))
			setAttribute(COMMENT_PARAMETER);
		else if (range.hasAttribute(COMMENT_ROOT))
			setAttribute(COMMENT_ROOT);
		else if (range.hasAttribute(COMMENT_BLANKLINE))
			setAttribute(COMMENT_BLANKLINE);

		final int ranges= getSize();
		if (ranges == 1) {

			if (parent.isIndentRoots()) {

				final CommentRange first= getFirst();
				final String common= parent.getText(first.getOffset(), first.getLength()) + CommentRegion.COMMENT_RANGE_DELIMITER;

				if (hasAttribute(COMMENT_ROOT))
					fReferenceIndentation= common;
				else if (hasAttribute(COMMENT_PARAMETER)) {
					if (parent.isIndentDescriptions())
						fReferenceIndentation= "\t" + common; //$NON-NLS-1$
					else
						fReferenceIndentation= common;
				}
			}
		}
		super.append(range);
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.text.comment.CommentLine#getContentLinePrefix()
	 */
	protected String getContentPrefix() {
		return MULTI_COMMENT_CONTENT_PREFIX;
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.text.comment.CommentLine#getEndLinePrefix()
	 */
	protected String getEndingPrefix() {
		return MULTI_COMMENT_END_PREFIX;
	}

	/**
	 * Returns the reference indentation to use for this line.
	 * 
	 * @return the reference indentation for this line
	 */
	protected final String getIndentationReference() {
		return fReferenceIndentation;
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.text.comment.CommentLine#getStartLinePrefix()
	 */
	protected String getStartingPrefix() {
		return MULTI_COMMENT_START_PREFIX;
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.text.comment.CommentLine#scanLine(int)
	 */
	protected void scanLine(final int line) {

		final CommentRegion parent= getParent();
		final String start= getStartingPrefix().trim();
		final String end= getEndingPrefix().trim();
		final String content= getContentPrefix().trim();

		final int lines= parent.getSize();
		final CommentRange range= getFirst();

		int offset= 0;
		int postfix= 0;

		String text= parent.getText(range.getOffset(), range.getLength());
		if (line == 0) {

			offset= text.indexOf(start);
			if (offset >= 0 && text.substring(0, offset).trim().length() != 0)
				offset= -1;
			
			if (offset >= 0) {

				offset += start.length();
				range.trimBegin(offset);

				postfix= text.lastIndexOf(end);
				if (postfix >= 0 && text.substring(postfix + end.length()).trim().length() != 0)
					postfix= -1;
				
				if (postfix >= offset)
					// comment ends on same line
					range.setLength(postfix - offset);
				else {
					postfix= text.lastIndexOf(content);
					if (postfix >= 0 && text.substring(postfix + content.length()).trim().length() != 0)
						postfix= -1;
					
					if (postfix >= offset) {

						range.setLength(postfix - offset);
						parent.setBorder(BORDER_UPPER);

						if (postfix > offset) {

							text= parent.getText(range.getOffset(), range.getLength());
							final IRegion region= trimLine(text, content);

							range.move(region.getOffset());
							range.setLength(region.getLength());
						}
					}
				}
			}
		} else if (line == lines - 1) {

			offset= text.indexOf(content);
			if (offset >= 0 && text.substring(0, offset).trim().length() != 0)
				offset= -1;
			postfix= text.lastIndexOf(end);
			if (postfix >= 0 && text.substring(postfix + end.length()).trim().length() != 0)
				postfix= -1;
			
			if (offset >= 0 && offset == postfix)
				// no content on line, only the comment postfix
				range.setLength(0);
			else {
				if (offset >= 0)
					// omit the content prefix
					range.trimBegin(offset + content.length());
				
				if (postfix >= 0)
					// omit the comment postfix
					range.trimEnd(-end.length());
				
				text= parent.getText(range.getOffset(), range.getLength());
				final IRegion region= trimLine(text, content);
				if (region.getOffset() != 0 || region.getLength() != text.length()) {

					range.move(region.getOffset());
					range.setLength(region.getLength());

					parent.setBorder(BORDER_UPPER);
					parent.setBorder(BORDER_LOWER);
				}
			}
		} else {

			offset= text.indexOf(content);
			if (offset >= 0 && text.substring(0, offset).trim().length() != 0)
				offset= -1;
			
			if (offset >= 0) {

				offset += content.length();
				range.trimBegin(offset);
			}
		}
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.text.comment.CommentLine#tokenizeLine(int)
	 */
	protected void tokenizeLine(int line) {

		int offset= 0;
		int index= offset;

		final CommentRegion parent= getParent();
		final CommentRange range= getFirst();
		final int begin= range.getOffset();

		final String content= parent.getText(begin, range.getLength());
		final int length= content.length();

		while (offset < length && ScannerHelper.isWhitespace(content.charAt(offset)))
			offset++;

		CommentRange result= null;
		if (offset >= length && !parent.isClearLines() && (line > 0 && line < parent.getSize() - 1)) {

			result= new CommentRange(begin, 0);
			result.setAttribute(COMMENT_BLANKLINE);
			result.setAttribute(COMMENT_FIRST_TOKEN);

			parent.append(result);
		}

		int attribute= COMMENT_FIRST_TOKEN | COMMENT_STARTS_WITH_RANGE_DELIMITER;
		while (offset < length) {

			while (offset < length && ScannerHelper.isWhitespace(content.charAt(offset))) {
				offset++;
				attribute |= COMMENT_STARTS_WITH_RANGE_DELIMITER;
			}

			index= offset;

			if (index < length) {

				if (content.charAt(index) == HTML_TAG_PREFIX) {

					// in order to avoid recognizing any < in a comment, even those which are part of e.g.
					// java source code, we validate the tag content to be one of the recognized
					// tags (structural, breaks, pre, code).
					int tag= ++index;
					while (index < length && content.charAt(index) != HTML_TAG_POSTFIX && content.charAt(index) != HTML_TAG_PREFIX)
						index++;

					if (index < length && content.charAt(index) == HTML_TAG_POSTFIX && isValidTag(content.substring(tag, index))) {
						index++;
						attribute |= COMMENT_HTML; // only set html attribute if postfix found
					} else {
						// no tag - do the usual thing from the original offset
						index= tag;
						while (index < length
								&& !ScannerHelper.isWhitespace(content.charAt(index))
								&& content.charAt(index) != HTML_TAG_PREFIX 
								&& !content.startsWith(LINK_TAG_PREFIX_STRING, index))
							index++;
					}


				} else if (content.startsWith(LINK_TAG_PREFIX_STRING, index)) {

					while (index < length && content.charAt(index) != LINK_TAG_POSTFIX)
						index++;

					if (index < length && content.charAt(index) == LINK_TAG_POSTFIX)
						index++;

					attribute |= COMMENT_OPEN | COMMENT_CLOSE;

				} else {

					while (index < length
							&& !ScannerHelper.isWhitespace(content.charAt(index))
							&& content.charAt(index) != HTML_TAG_PREFIX
							&& !content.startsWith(LINK_TAG_PREFIX_STRING, index))
						index++;
				}
			}

			if (index - offset > 0) {

				result= new CommentRange(begin + offset, index - offset);
				result.setAttribute(attribute);

				parent.append(result);
				offset= index;
			}
			
			attribute= 0;
		}
	}

	/**
	 * Checks whether <code>tag</code> is a valid tag content (text inside
	 * the angular brackets &lt;, &gt;).
	 * <p>
	 * The algorithm is to see if the tag trimmed of whitespace and an
	 * optional slash starts with one of our recognized tags.
	 * 
	 * @param tag the tag to check
	 * @return <code>true</code> if <code>tag</code> is a valid tag
	 *         content
	 */
	private boolean isValidTag(String tag) {
		// strip the slash
		if (tag.startsWith("/")) //$NON-NLS-1$
			tag= tag.substring(1, tag.length());
		
		// strip ws
		tag= tag.trim();
		
		// extract first token
		int i= 0;
		while (i < tag.length() && !ScannerHelper.isWhitespace(tag.charAt(i)))
			i++;
		tag= tag.substring(0, i);
		
		// see if it's a tag
		return isTagName(tag.toLowerCase());
	}

	/**
	 * Checks whether <code>tag</code> is one of the configured tags.
	 * 
	 * @param tag the tag to check
	 * @return <code>true</code> if <code>tag</code> is a configured tag
	 *         name
	 */
	private boolean isTagName(String tag) {
		return fgTagLookup.contains(tag);
	}

	/**
	 * Removes all leading and trailing occurrences from <code>line</code>.
	 * 
	 * @param line the string to remove the occurrences of
	 *                <code>trimmable</code>
	 * @param trimmable the string to remove from <code>line</code>
	 * @return the region of the trimmed substring within <code>line</code>
	 */
	protected final IRegion trimLine(final String line, final String trimmable) {

		final int trim= trimmable.length();

		int offset= 0;
		int length= line.length() - trim;

		while (line.startsWith(trimmable, offset))
			offset += trim;

		while (line.startsWith(trimmable, length))
			length -= trim;

		return new Region(offset, length + trim);
	}
}
