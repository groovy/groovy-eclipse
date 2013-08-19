/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt McCutchen - fix for bug 197169 and complementary fix for 109636
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter.comment;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.parser.*;

/**
 * <code>SubstitutionTextReader</code> that will substitute html entities for
 * html symbols encountered in the original text. Line breaks and whitespaces
 * are preserved.
 *
 * @since 3.0
 */
public class Java2HTMLEntityReader extends SubstitutionTextReader {

	private static final int BEGIN_LINE = 0x01;

	/** The hardcoded entity map. */
	private static final Map fgEntityLookup;

	/**
	 * True if we have not yet seen a non-whitespace character on the current
	 * line.
	 */
	private int bits = BEGIN_LINE;

	static {
		fgEntityLookup= new HashMap(7);
		fgEntityLookup.put("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
		fgEntityLookup.put(">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
		fgEntityLookup.put("&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
		fgEntityLookup.put("^", "&circ;"); //$NON-NLS-1$ //$NON-NLS-2$
		fgEntityLookup.put("~", "&tilde;"); //$NON-NLS-2$ //$NON-NLS-1$
		fgEntityLookup.put("\"", "&quot;"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Creates a new instance that will read from <code>reader</code>
	 *
	 * @param reader the source reader
	 */
	public Java2HTMLEntityReader(Reader reader) {
		super(reader);
		setSkipWhitespace(false);
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.text.SubstitutionTextReader#computeSubstitution(int)
	 */
	protected String computeSubstitution(int c) throws IOException {
		StringBuffer buf = new StringBuffer();
		// Accumulate *s into the buffer until we see something other than *.
		while (c == '*') {
			this.bits &= ~BEGIN_LINE;
			c = nextChar();
			buf.append('*');
		}
		if (c == -1)
			// Snippet must have ended with *s.  Just return them.
			return buf.toString();
		if (c == '/' && buf.length() > 0) {
			/*
			 * Translate a * that precedes a / to &#42; so it isn't
			 * misinterpreted as the end of the Javadoc comment that contains
			 * the code we are formatting.
			 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=109636
			 */
			buf.setLength(buf.length() - 1);
			buf.append("&#42;/"); //$NON-NLS-1$
		} else if (c == '@' && (this.bits & BEGIN_LINE) != 0) {
			/*
			 * When @ is first on a line, translate it to &#064; so it isn't
			 * misinterpreted as a Javadoc tag.
			 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=197169
			 */
			buf.append("&#064;"); //$NON-NLS-1$
		} else {
			/*
			 * Ordinary processing.  If the character needs an entity in HTML,
			 * add the entity, otherwise add the character.
			 */
			String entity = (String) fgEntityLookup.get(String.valueOf((char) c));
			if (entity != null)
				buf.append(entity);
			else
				buf.append((char) c);
		}
		// Update bits for the benefit of the next character.
		if (c == '\n' || c == '\r') {
			this.bits |= BEGIN_LINE;
		} else if (!ScannerHelper.isWhitespace((char) c)) {
			this.bits &= ~BEGIN_LINE;
		}
		return buf.toString();
	}
}
