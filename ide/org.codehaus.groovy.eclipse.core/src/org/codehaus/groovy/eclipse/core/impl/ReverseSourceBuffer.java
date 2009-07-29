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
