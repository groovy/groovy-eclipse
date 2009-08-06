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
