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
package org.codehaus.groovy.eclipse.codeassist;

import org.codehaus.groovy.eclipse.core.ISourceBuffer;
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
