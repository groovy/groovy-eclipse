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
package org.codehaus.groovy.eclipse.core.impl;

import static org.codehaus.groovy.eclipse.core.util.ListUtil.newList;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.core.ISourceBuffer;

/**
 * Implementation of ISourceBuffer for String instances.
 * 
 * @author empovazan
 */
public class StringSourceBuffer implements ISourceBuffer {
	private final char[] sourceCode;

	/** List of Integer to line offsets. */
	private final List lineOffsets;

	public StringSourceBuffer(String sourceCode) {
		this.sourceCode = new char[sourceCode.length()];
		sourceCode.getChars(0, sourceCode.length(), this.sourceCode, 0);
		this.lineOffsets = createLineLookup(this.sourceCode);
	}

	private List createLineLookup(char[] sourceCode) {
		if (sourceCode.length == 0) {
			return new ArrayList();
		}

		List< Integer > offsets = newList();
		offsets.add(new Integer(0));

		int ch;
		for (int i = 0; i < sourceCode.length; ++i) {
			ch = sourceCode[i];
			if (ch == '\r') {
				if (i + 1 < sourceCode.length) {
					ch = sourceCode[i + 1];
					if (ch == '\n') {
						offsets.add(new Integer(++i + 1));
					} else {
						offsets.add(new Integer(i + 1));
					}
				} else {
					offsets.add(new Integer(i + 1));
				}
			} else if (ch == '\n') {
				offsets.add(new Integer(i + 1));
			}
		}

		return offsets;
	}

	public char charAt(int offset) {
		try {
			return sourceCode[offset];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IndexOutOfBoundsException("Offset: " + offset + ", Range: [0.." + (sourceCode.length - 1) + "]");
		}
	}

	public int length() {
		return sourceCode.length;
	}

	public CharSequence subSequence(int start, int end) {
		return new String(sourceCode, start, end - start);
	}

	public int[] toLineColumn(int offset) {
		try {
			for (int i = 0; i < lineOffsets.size(); ++i) {
				int lineOffset = ((Integer) lineOffsets.get(i)).intValue();
				if (offset < lineOffset) {
					lineOffset = ((Integer) lineOffsets.get(i - 1)).intValue();
					return new int[] { i, offset - lineOffset + 1 };
				}
			}
			int line = lineOffsets.size();
			int lineOffset = ((Integer) lineOffsets.get(line - 1)).intValue();
			return new int[] { line, offset - lineOffset + 1 };
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IndexOutOfBoundsException("Offset: " + offset + ", Range: [0.." + (sourceCode.length - 1) + "]");
		}
	}

	public int toOffset(int line, int column) {
		int offset = ((Integer) lineOffsets.get(line - 1)).intValue();
		return offset + column - 1;
	}
}
