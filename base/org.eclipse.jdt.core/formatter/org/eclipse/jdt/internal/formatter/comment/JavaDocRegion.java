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

package org.eclipse.jdt.internal.formatter.comment;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.text.edits.TextEdit;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextUtilities;

import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;
import org.eclipse.jdt.internal.formatter.CodeFormatterVisitor;


/**
 * Javadoc region in a source code document.
 *
 * @since 3.0
 */
public class JavaDocRegion extends MultiCommentRegion {

	/** The positions of code ranges */
	private final ArrayList fCodePositions= new ArrayList();

	/** Should HTML tags be formatted? */
	private final boolean fFormatHtml;

	/** Should source code regions be formatted? */
	private final boolean fFormatSource;

 	/**
	 * Creates a new Javadoc region.
	 *
	 * @param document the document which contains the comment region
	 * @param position the position of this comment region in the document
	 * @param formatter the given formatter
	 */
	public JavaDocRegion(final IDocument document, final Position position, final CodeFormatterVisitor formatter) {
		super(document, position, formatter);

		this.fFormatSource = this.preferences.comment_format_source;
		this.fFormatHtml = this.preferences.comment_format_html;
		this.fClear = this.preferences.comment_clear_blank_lines_in_javadoc_comment;
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.text.comment.CommentRegion#canFormat(org.eclipse.jdt.internal.corext.text.comment.CommentRange, org.eclipse.jdt.internal.corext.text.comment.CommentRange)
	 */
	protected boolean canFormat(final CommentRange previous, final CommentRange next) {

		if (previous != null) {

			final boolean isCurrentCode= next.hasAttribute(COMMENT_CODE);
			final boolean isLastCode= previous.hasAttribute(COMMENT_CODE);

			final int base= getOffset();

			if (!isLastCode && isCurrentCode)
				this.fCodePositions.add(new Position(base + previous.getOffset()));
			else if (isLastCode && !isCurrentCode)
				this.fCodePositions.add(new Position(base + next.getOffset() + next.getLength()));

			if (previous.hasAttribute(COMMENT_IMMUTABLE) && next.hasAttribute(COMMENT_IMMUTABLE))
				return false;

			return true;
		}
		return false;
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.text.comment.CommentRegion#formatRegion(java.lang.String, int)
	 */
	protected final void formatRegion(final String indentation, final int width) {

		super.formatRegion(indentation, width);

		if (this.fFormatSource) {

			try {

				if (this.fCodePositions.size() > 0) {

					int begin= 0;
					int end= 0;

					Position position= null;

					final IDocument document= getDocument();

					for (int index= this.fCodePositions.size() - 1; index >= 0;) {

						position= (Position)this.fCodePositions.get(index--);
						begin= position.getOffset();

						if (index >= 0) {
							position= (Position)this.fCodePositions.get(index--);
							end= position.getOffset();
						} else {
							/*
							 * Handle missing closing tag
							 * see: https://bugs.eclipse.org/bugs/show_bug.cgi?id=57011
							 */
							position= null;
							end= getOffset() + getLength() - MultiCommentLine.MULTI_COMMENT_END_PREFIX.trim().length();
							while (end > begin && ScannerHelper.isWhitespace(document.getChar(end - 1)))
								end--;
						}

						String snippet= document.get(begin, end - begin);
						snippet= preprocessCodeSnippet(snippet);
						snippet= formatCodeSnippet(snippet);
						snippet= postprocessCodeSnippet(snippet, indentation);

						logEdit(snippet, begin - getOffset(), end - begin);
					}
				}
			} catch (BadLocationException e) {
				// Can not happen
				CommentFormatterUtil.log(e);
			}
		}
	}

	/**
	 * Preprocess a given code snippet.
	 *
	 * @param snippet the code snippet
	 * @return the preprocessed code snippet
	 */
	private String preprocessCodeSnippet(String snippet) {
		// strip content prefix
		StringBuffer buffer= new StringBuffer();
		ILineTracker tracker= new DefaultLineTracker();
		String contentPrefix= MultiCommentLine.MULTI_COMMENT_CONTENT_PREFIX.trim();

		buffer.setLength(0);
		buffer.append(snippet);
		tracker.set(snippet);
		for (int line= tracker.getNumberOfLines() - 1; line > 0; line--) {
			int lineOffset;
			try {
				lineOffset= tracker.getLineOffset(line);
			} catch (BadLocationException e) {
				// Can not happen
				CommentFormatterUtil.log(e);
				return snippet;
			}
			int prefixOffset= buffer.indexOf(contentPrefix, lineOffset);
			if (prefixOffset >= 0 && buffer.substring(lineOffset, prefixOffset).trim().length() == 0)
				buffer.delete(lineOffset, prefixOffset + contentPrefix.length() + 1);
		}

		return convertHtml2Java(buffer.toString());
	}

	/**
	 * Format the given code snippet
	 *
	 * @param snippet the code snippet
	 * @return the formatted code snippet
	 */
	private String formatCodeSnippet(String snippet) {
		String lineDelimiter= TextUtilities.getDefaultLineDelimiter(getDocument());
		TextEdit edit= CommentFormatterUtil.format2(CodeFormatter.K_UNKNOWN, snippet, 0, lineDelimiter, this.preferences.getMap());
		if (edit != null)
			snippet= CommentFormatterUtil.evaluateFormatterEdit(snippet, edit, null);
		return snippet;
	}

	/**
	 * Postprocesses the given code snippet with the given indentation.
	 *
	 * @param snippet the code snippet
	 * @param indentation the indentation
	 * @return the postprocessed code snippet
	 */
	private String postprocessCodeSnippet(String snippet, String indentation) {
		// patch content prefix
		StringBuffer buffer= new StringBuffer();
		ILineTracker tracker= new DefaultLineTracker();
		String patch= indentation + MultiCommentLine.MULTI_COMMENT_CONTENT_PREFIX;

		// remove trailing spaces
		int i= snippet.length();
		while (i > 0 && ' ' == snippet.charAt(i-1))
			i--;
		snippet= snippet.substring(0, i);

		buffer.setLength(0);
		String lineDelimiter= getDelimiter();
		if (lineDelimiter != null && snippet.indexOf(lineDelimiter) != 0)
			buffer.append(lineDelimiter);
		buffer.append(convertJava2Html(snippet));
		if (lineDelimiter != null && snippet.lastIndexOf(lineDelimiter) != snippet.length() - lineDelimiter.length())
			buffer.append(lineDelimiter);
		tracker.set(buffer.toString());

		for (int line= tracker.getNumberOfLines() - 1; line > 0; line--)
			try {
				buffer.insert(tracker.getLineOffset(line), patch);
			} catch (BadLocationException e) {
				// Can not happen
				CommentFormatterUtil.log(e);
				return snippet;
			}

		return buffer.toString();
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.text.comment.MultiCommentRegion#markHtmlRanges()
	 */
	protected final void markHtmlRanges() {

		markTagRanges(JAVADOC_IMMUTABLE_TAGS, COMMENT_IMMUTABLE, true);

		if (this.fFormatSource)
			markTagRanges(JAVADOC_CODE_TAGS, COMMENT_CODE, false);
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.text.comment.MultiCommentRegion#markHtmlTag(org.eclipse.jdt.internal.corext.text.comment.CommentRange, java.lang.String)
	 */
	protected final void markHtmlTag(final CommentRange range, final char[] token) {

		if (range.hasAttribute(COMMENT_HTML)) {

			range.markHtmlTag(JAVADOC_IMMUTABLE_TAGS, token, COMMENT_IMMUTABLE, true, true);
			if (this.fFormatHtml) {

				range.markHtmlTag(JAVADOC_SEPARATOR_TAGS, token, COMMENT_SEPARATOR, true, true);
				range.markHtmlTag(JAVADOC_BREAK_TAGS, token, COMMENT_BREAK, false, true);
				range.markHtmlTag(JAVADOC_SINGLE_BREAK_TAG, token, COMMENT_BREAK, true, false);
				range.markHtmlTag(JAVADOC_NEWLINE_TAGS, token, COMMENT_NEWLINE, true, false);

			} else
				range.markHtmlTag(JAVADOC_CODE_TAGS, token, COMMENT_SEPARATOR, true, true);
		}
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.text.comment.MultiCommentRegion#markJavadocTag(org.eclipse.jdt.internal.corext.text.comment.CommentRange, java.lang.String)
	 */
	protected final void markJavadocTag(final CommentRange range, final char[] token) {

		range.markPrefixTag(JAVADOC_PARAM_TAGS, COMMENT_TAG_PREFIX, token, COMMENT_PARAMETER);

		if (token[0] == JAVADOC_TAG_PREFIX && !range.hasAttribute(COMMENT_PARAMETER))
			range.setAttribute(COMMENT_ROOT);
	}

	/**
	 * Marks the comment region with the HTML range tag.
	 *
	 * @param tags the HTML tag which confines the HTML range
	 * @param attribute the attribute to set if the comment range is in the
	 *                HTML range
	 * @param html <code>true</code> iff the HTML tags in this HTML range
	 *                should be marked too, <code>false</code> otherwise
	 */
	protected final void markTagRanges(final char[][] tags, final int attribute, final boolean html) {

		int level= 0;
		int count= 0;
		char[] token= null;
		CommentRange current= null;

		for (int index= 0; index < tags.length; index++) {

			level= 0;
			for (final Iterator iterator= getRanges().iterator(); iterator.hasNext();) {

				current= (CommentRange)iterator.next();
				count= current.getLength();

				if (count > 0 || level > 0) { // PR44035: when inside a tag, mark blank lines as well to get proper snippet formatting

					token= getText(current.getOffset(), current.getLength()).toCharArray();
					level= current.markTagRange(token, tags[index], level, attribute, html);
				}
			}
		}
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.text.comment.CommentRegion#canAppend(org.eclipse.jdt.internal.corext.text.comment.CommentLine, org.eclipse.jdt.internal.corext.text.comment.CommentRange, org.eclipse.jdt.internal.corext.text.comment.CommentRange, int, int)
	 */
	protected boolean canAppend(CommentLine line, CommentRange previous, CommentRange next, int index, int count) {
		// don't append code sections
		if (next.hasAttribute(COMMENT_CODE | COMMENT_FIRST_TOKEN) && line.getSize() != 0)
			return false;
		return super.canAppend(line, previous, next, index, count);
	}

	/**
	 * Converts <code>formatted</code> into valid html code suitable to be
	 * put inside &lt;pre&gt;&lt;/pre&gt; tags by replacing any html symbols
	 * by the relevant entities.
	 *
	 * @param formatted the formatted java code
	 * @return html version of the formatted code
	 */
	private String convertJava2Html(String formatted) {
		Java2HTMLEntityReader reader= new Java2HTMLEntityReader(new StringReader(formatted));
		char[] buf= new char[256];
		StringBuffer buffer= new StringBuffer();
		int l;
		try {
			do {
				l= reader.read(buf);
				if (l != -1)
					buffer.append(buf, 0, l);
			} while (l > 0);
			return buffer.toString();
		} catch (IOException e) {
			return formatted;
		}
	}

	/**
	 * Converts <code>html</code> into java code suitable for formatting
	 * by replacing any html entities by their plain text representation.
	 *
	 * @param html html code, may contain html entities
	 * @return plain textified version of <code>html</code>
	 */
	private String convertHtml2Java(String html) {
		HTMLEntity2JavaReader reader= new HTMLEntity2JavaReader(new StringReader(html));
		char[] buf= new char[html.length()]; // html2text never gets longer, only shorter!

		try {
			int read= reader.read(buf);
			return new String(buf, 0, read);
		} catch (IOException e) {
			return html;
		}
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.text.comment.CommentRegion#createLine()
	 * @since 3.1
	 */
	protected CommentLine createLine() {
		return new JavaDocLine(this);
	}
}
