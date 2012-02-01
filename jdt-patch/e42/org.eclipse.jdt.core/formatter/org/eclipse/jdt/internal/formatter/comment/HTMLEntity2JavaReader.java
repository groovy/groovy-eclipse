/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt McCutchen - fix for bug 198153
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter.comment;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;

/**
 * <code>SubstitutionTextReader</code> that will substitute plain text values
 * for html entities encountered in the original text. Line breaks and
 * whitespace are preserved.
 *
 * @since 3.0
 */
public class HTMLEntity2JavaReader extends SubstitutionTextReader {

	/** The hard-coded entity map. */
	private static final Map fgEntityLookup;

	static {
		fgEntityLookup= new HashMap(7);
		fgEntityLookup.put("lt", "<"); //$NON-NLS-1$ //$NON-NLS-2$
		fgEntityLookup.put("gt", ">"); //$NON-NLS-1$ //$NON-NLS-2$
		fgEntityLookup.put("nbsp", " "); //$NON-NLS-1$ //$NON-NLS-2$
		fgEntityLookup.put("amp", "&"); //$NON-NLS-1$ //$NON-NLS-2$
		fgEntityLookup.put("circ", "^"); //$NON-NLS-1$ //$NON-NLS-2$
		fgEntityLookup.put("tilde", "~"); //$NON-NLS-2$ //$NON-NLS-1$
		fgEntityLookup.put("quot", "\""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Creates a new instance that will read from <code>reader</code>
	 *
	 * @param reader the source reader
	 */
	public HTMLEntity2JavaReader(Reader reader) {
		super(reader);
		setSkipWhitespace(false);
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.text.SubstitutionTextReader#computeSubstitution(int)
	 */
	protected String computeSubstitution(int c) throws IOException {
		if (c == '&')
			return processEntity();
		return null;
	}

	/**
	 * Replaces an HTML entity body (without &amp; and ;) with its
	 * plain/text (or plain/java) counterpart.
	 *
	 * @param symbol the entity body to resolve
	 * @return the plain/text counterpart of <code>symbol</code>
	 */
	protected String entity2Text(String symbol) {
		if (symbol.length() > 1 && symbol.charAt(0) == '#') {
			int ch;
			try {
				if (symbol.charAt(1) == 'x') {
					ch= Integer.parseInt(symbol.substring(2), 16);
				} else {
					ch= Integer.parseInt(symbol.substring(1), 10);
				}
				return String.valueOf((char) ch);
			} catch (NumberFormatException e) {
				// ignore
			}
		} else {
			String str= (String) fgEntityLookup.get(symbol);
			if (str != null) {
				return str;
			}
		}
		return "&" + symbol; // not found //$NON-NLS-1$
	}

	/**
	 * Reads an HTML entity from the stream and returns its plain/text
	 * counterpart.
	 *
	 * @return an entity read from the stream, or the stream content.
	 * @throws IOException if the underlying reader throws one
	 */
	private String processEntity() throws IOException {
		StringBuffer buf= new StringBuffer();
		int ch= nextChar();
		while (ScannerHelper.isLetterOrDigit((char) ch) || ch == '#') {
			buf.append((char) ch);
			ch= nextChar();
		}
		if (ch == ';')
			return entity2Text(buf.toString());
		buf.insert(0, '&');
		if (ch != -1)
			buf.append((char) ch);
		return buf.toString();
	}
}
