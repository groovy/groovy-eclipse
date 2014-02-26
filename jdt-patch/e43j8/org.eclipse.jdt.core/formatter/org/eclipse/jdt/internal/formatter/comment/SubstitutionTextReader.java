/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt McCutchen - add check for EOF handling
 ******************************************************************************/

package org.eclipse.jdt.internal.formatter.comment;


import java.io.IOException;
import java.io.Reader;

import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;

/**
 * Reads the text contents from a reader and computes for each character
 * a potential substitution. The substitution may eat more characters than
 * only the one passed into the computation routine.
 */
public abstract class SubstitutionTextReader extends Reader {

	private Reader fReader;
	private boolean fWasWhiteSpace;
	private int fCharAfterWhiteSpace;

	/**
	 * Tells whether white space characters are skipped.
	 */
	private boolean fSkipWhiteSpace= true;

	private boolean fReadFromBuffer;
	private StringBuffer fBuffer;
	private int fIndex;


	protected SubstitutionTextReader(Reader reader) {
		this.fReader= reader;
		this.fBuffer= new StringBuffer();
		this.fIndex= 0;
		this.fReadFromBuffer= false;
		this.fCharAfterWhiteSpace= -1;
		this.fWasWhiteSpace= true;
	}

	/**
	 * Gets the content as a String
	 */
	public String getString() throws IOException {
		StringBuffer buf= new StringBuffer();
		int ch;
		while ((ch= read()) != -1) {
			buf.append((char)ch);
		}
		return buf.toString();
	}

	/**
	 * Implement to compute the substitution for the given character and
	 * if necessary subsequent characters. Use <code>nextChar</code>
	 * to read subsequent characters.
	 */
	protected abstract String computeSubstitution(int c) throws IOException;

	/**
	 * Returns the internal reader.
	 */
	protected Reader getReader() {
		return this.fReader;
	}

	/**
	 * Returns the next character.
	 */
	protected int nextChar() throws IOException {
		this.fReadFromBuffer= (this.fBuffer.length() > 0);
		if (this.fReadFromBuffer) {
			char ch= this.fBuffer.charAt(this.fIndex++);
			if (this.fIndex >= this.fBuffer.length()) {
				this.fBuffer.setLength(0);
				this.fIndex= 0;
			}
			return ch;
		} else {
			int ch= this.fCharAfterWhiteSpace;
			if (ch == -1) {
				ch= this.fReader.read();
			}
			if (this.fSkipWhiteSpace && ScannerHelper.isWhitespace((char)ch)) {
				do {
					ch= this.fReader.read();
				} while (ScannerHelper.isWhitespace((char)ch));
				if (ch != -1) {
					this.fCharAfterWhiteSpace= ch;
					return ' ';
				}
			} else {
				this.fCharAfterWhiteSpace= -1;
			}
			return ch;
		}
	}

	/*
	 * @see Reader#read()
	 */
	public int read() throws IOException {
		int c;
		do {

			c= nextChar();
			while (!this.fReadFromBuffer && c != -1) {
				String s= computeSubstitution(c);
				if (s == null)
					break;
				if (s.length() > 0)
					this.fBuffer.insert(0, s);
				c= nextChar();
			}

		} while (this.fSkipWhiteSpace && this.fWasWhiteSpace && (c == ' '));
		this.fWasWhiteSpace= (c == ' ' || c == '\r' || c == '\n');
		return c;
	}

	/*
	 * @see Reader#read(char[],int,int)
	 */
	public int read(char cbuf[], int off, int len) throws IOException {
		int end= off + len;
		for (int i= off; i < end; i++) {
			int ch= read();
			if (ch == -1) {
				if (i == off) {
					return -1;
				} else {
					return i - off;
				}
			}
			cbuf[i]= (char)ch;
		}
		return len;
	}

	/*
	 * @see java.io.Reader#ready()
	 */
	public boolean ready() throws IOException {
		return this.fReader.ready();
	}

	/*
	 * @see Reader#close()
	 */
	public void close() throws IOException {
		this.fReader.close();
	}

	/*
	 * @see Reader#reset()
	 */
	public void reset() throws IOException {
		this.fReader.reset();
		this.fWasWhiteSpace= true;
		this.fCharAfterWhiteSpace= -1;
		this.fBuffer.setLength(0);
		this.fIndex= 0;
	}

	protected final void setSkipWhitespace(boolean state) {
		this.fSkipWhiteSpace= state;
	}

	protected final boolean isSkippingWhitespace() {
		return this.fSkipWhiteSpace;
	}
}
