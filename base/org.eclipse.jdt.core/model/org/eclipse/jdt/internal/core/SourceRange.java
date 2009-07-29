/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.ISourceRange;

/**
 * @see ISourceRange
 */
public class SourceRange implements ISourceRange {

protected int offset, length;

public SourceRange(int offset, int length) {
	this.offset = offset;
	this.length = length;
}
/*
 * @see Object#equals(Object)
 */
public boolean equals(Object obj) {
	if (!(obj instanceof ISourceRange))
        return false;
	ISourceRange sourceRange = (ISourceRange) obj;
    return sourceRange.getOffset() == this.offset && sourceRange.getLength() == this.length;
}
/**
 * @see ISourceRange
 */
public int getLength() {
	return this.length;
}
/**
 * @see ISourceRange
 */
public int getOffset() {
	return this.offset;
}
/*
 * @see Object#hashCode()
 */
public int hashCode() {
    return this.length ^ this.offset;
}
public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("[offset="); //$NON-NLS-1$
	buffer.append(this.offset);
	buffer.append(", length="); //$NON-NLS-1$
	buffer.append(this.length);
	buffer.append("]"); //$NON-NLS-1$
	return buffer.toString();
}
}
