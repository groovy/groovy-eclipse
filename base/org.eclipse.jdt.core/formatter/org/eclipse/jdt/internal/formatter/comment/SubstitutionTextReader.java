/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
		fReader= reader;
		fBuffer= new StringBuffer();
		fIndex= 0;
		fReadFromBuffer= false;
		fCharAfterWhiteSpace= -1;
		fWasWhiteSpace= true;
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
		return fReader;
	}
	 
	/**
	 * Returns the next character.
	 */
	protected int nextChar() throws IOException {
		fReadFromBuffer= (fBuffer.length() > 0);
		if (fReadFromBuffer) {
			char ch= fBuffer.charAt(fIndex++);
			if (fIndex >= fBuffer.length()) {
				fBuffer.setLength(0);
				fIndex= 0;
			}
			return ch;
		} else {
			int ch= fCharAfterWhiteSpace;
			if (ch == -1) {
				ch= fReader.read();
			}
			if (fSkipWhiteSpace && ScannerHelper.isWhitespace((char)ch)) {
				do {
					ch= fReader.read();
				} while (ScannerHelper.isWhitespace((char)ch));
				if (ch != -1) {
					fCharAfterWhiteSpace= ch;
					return ' ';
				}
			} else {
				fCharAfterWhiteSpace= -1;
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
			while (!fReadFromBuffer && c != -1) {
				String s= computeSubstitution(c);
				if (s == null)
					break;
				if (s.length() > 0)
					fBuffer.insert(0, s);
				c= nextChar();
			}
			
		} while (fSkipWhiteSpace && fWasWhiteSpace && (c == ' '));
		fWasWhiteSpace= (c == ' ' || c == '\r' || c == '\n');
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
		return fReader.ready();
	}
		
	/*
	 * @see Reader#close()
	 */		
	public void close() throws IOException {
		fReader.close();
	}
	
	/*
	 * @see Reader#reset()
	 */		
	public void reset() throws IOException {
		fReader.reset();
		fWasWhiteSpace= true;
		fCharAfterWhiteSpace= -1;
		fBuffer.setLength(0);
		fIndex= 0;		
	}

	protected final void setSkipWhitespace(boolean state) {
		fSkipWhiteSpace= state;
	}

	protected final boolean isSkippingWhitespace() {
		return fSkipWhiteSpace;
	}
}
