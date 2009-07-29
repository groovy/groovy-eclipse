/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.core;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

public class DocumentSourceBuffer implements ISourceBuffer {
	private IDocument document;

	public DocumentSourceBuffer(IDocument document) {
		this.document = document;
	}

	public char charAt(int offset) {
		try {
			return document.getChar(offset);
		} catch (BadLocationException e) {
			throw new IndexOutOfBoundsException(e.getMessage());
		}
	}

	public int length() {
		return document.getLength();
	}

	public CharSequence subSequence(int start, int end) {
		try {
			return document.get(start, end - start);
		} catch (BadLocationException e) {
			throw new IndexOutOfBoundsException(e.getMessage());
		}
	}

	public int[] toLineColumn(int offset) {
		int line;
		try {
			line = document.getLineOfOffset(offset);
			int lineOffset = document.getLineOffset(line);
			return new int[] { line + 1, offset - lineOffset + 1 };
		} catch (BadLocationException e) {
			throw new IndexOutOfBoundsException(e.getMessage());
		}
	}

	public int toOffset(int line, int column) {
		int offset;
		try {
			offset = document.getLineOffset(line - 1);
			return offset + column - 1;
		} catch (BadLocationException e) {
			try {
				return document.getLineOffset(document.getNumberOfLines()-1);
			} catch (BadLocationException e1) {
				throw new IndexOutOfBoundsException(e.getMessage());
			}
		}
	}
}
