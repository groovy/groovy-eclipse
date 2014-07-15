/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter;

/**
 * Represents a reference in a javadoc comment block (see
 * {@link FormatJavadocBlock}.
 * <p>
 * A specific class is used as intermediate positions need to be stored for further
 * formatting improvements (typically for qualified references).
 * </p>
 */
public class FormatJavadocReference extends FormatJavadocNode {

public FormatJavadocReference(int start, int end, int line) {
	super(start, end, line);
}

public FormatJavadocReference(long position, int line) {
	super((int) (position >>> 32), (int) position, line);
}

void clean() {
	// Clean positions when used
}

protected void toString(StringBuffer buffer) {
	buffer.append("ref");	//$NON-NLS-1$
	super.toString(buffer);
}
}
