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

import org.codehaus.groovy.eclipse.core.ISourceBuffer;

/**
 * A buffer useful for reverse regex.
 * 
 * @author empovazan
 */
public class ReverseSourceBuffer implements ISourceBuffer {
	private ISourceBuffer buffer;

	private int origin;

	public ReverseSourceBuffer(ISourceBuffer buffer, int origin) {
		this.buffer = buffer;
		this.origin = origin;
	}

	public char charAt(int offset) {
		char ch = buffer.charAt(origin - offset);
		return ch;
	}

	public int length() {
		return origin + 1;
	}

	public CharSequence subSequence(int start, int end) {
		return buffer.subSequence(origin - end + 1, origin - start + 1);
	}

	public int[] toLineColumn(int offset) {
		throw new UnsupportedOperationException();
	}

	public int toOffset(int line, int column) {
		throw new UnsupportedOperationException();
	}
}
