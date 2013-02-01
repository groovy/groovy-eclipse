/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter;

import org.eclipse.jdt.internal.compiler.parser.JavadocTagConstants;

/**
 * Abstract class for all {@link FormatJavadoc} nodes.
 * <p>
 * The basic information for these nodes are the start and end positions in the
 * source.
 * </p>
 */
public abstract class FormatJavadocNode implements JavadocTagConstants {

	// default size used for array
	final static int DEFAULT_ARRAY_SIZE = 10;
	final static int INCREMENT_ARRAY_SIZE = 10;
	protected int sourceStart, sourceEnd;
	protected int lineStart;
	protected int linesBefore = 0;

public FormatJavadocNode(int start, int end, int line) {
	this.sourceStart = start;
	this.sourceEnd = end;
	this.lineStart = line;
}

abstract void clean();

FormatJavadocNode getLastNode() {
	return null;
}

public int getLength() {
	return this.sourceEnd - this.sourceStart + 1;
}

/**
 * Returns whether the node is a text (see {@link FormatJavadocText} or not.
 * In case not, that means that the node is an block (see
 * {@link FormatJavadocBlock}).
 *
 * @return <code>true</code> if the node is a text <code>false</code>
 * 	otherwise.
 */
public boolean isText() {
	return false;
}

/**
 * Returns whether the node is immutable or not. If <code>true</code>, then
 * the formatter will leave it contents unchanged.
 *
 * @return <code>true</code> if the node is immutable, <code>false</code>
 * 	otherwise.
 */
public boolean isImmutable() {
	return false;
}

public String toString() {
	StringBuffer buffer = new StringBuffer();
	toString(buffer);
	return buffer.toString();
}
protected void toString(StringBuffer buffer) {
	buffer.append(": "); //$NON-NLS-1$
	buffer.append(this.sourceStart);
	buffer.append(" -> ");	//$NON-NLS-1$
	buffer.append(this.sourceEnd);
}

public String toStringDebug(char[] source) {
	StringBuffer buffer = new StringBuffer();
	toStringDebug(buffer, source);
	return buffer.toString();
}

public void toStringDebug(StringBuffer buffer, char[] source) {
	buffer.append(source, this.sourceStart, this.sourceEnd-this.sourceStart+1);
	buffer.append(' ');
}

void setHeaderLine(int javadocLineStart) {
	// do nothing
}

}
