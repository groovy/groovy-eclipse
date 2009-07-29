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

/**
 * An interface representing a buffer for some source code. It is a read only buffer.
 * The buffer must never change while this interface is in use.
 * <p>
 * This interface is used by various packages, to detemine the context in some code, for evaluating types in expressions
 * and so on.
 * <p>
 * Specific implementations are used in the following ways: In editors, it wraps an editor document. In tests, it wraps
 * a String. In an expression evaluator, it wraps a text fields buffer. And so on.
 * 
 * @author empovazan
 */
public interface ISourceBuffer extends CharSequence {
	/**
	 * @see CharSequence#charAt(int)
	 */
	public char charAt(int offset);

	/**
	 * @see CharSequence#length()
	 */
	public int length();

	/**
	 * @see CharSequence#subSequence(int, int)
	 */
	public CharSequence subSequence(int start, int end);

	/**
	 * Convert from offset coordinates to line/column coordinates.
	 * 
	 * @param offset
	 * @throws IndexOutOfBoundsException
	 * @return An integer array with values [line, column].
	 */
	public int[] toLineColumn(int offset);

	/**
	 * Convert from line/column coordinates to offset coordinates.
	 * 
	 * @param line
	 * @param column
	 * @throws IndexOutOfBoundsException
	 * @return The offset represented by the line/column.
	 */
	public int toOffset(int line, int column);
}
