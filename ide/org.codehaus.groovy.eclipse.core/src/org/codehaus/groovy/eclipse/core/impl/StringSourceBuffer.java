 /*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
