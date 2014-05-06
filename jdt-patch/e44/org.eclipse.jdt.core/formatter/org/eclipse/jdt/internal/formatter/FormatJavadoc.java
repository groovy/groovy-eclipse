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

import org.eclipse.jdt.internal.compiler.ast.Javadoc;

/**
 * Represents a full Javadoc comment for the {@link FormatterCommentParser}.
 * <p>
 * It might have one or several blocks ( see {@link FormatJavadocBlock}). The
 * javadoc comment might starts with a <b>description</b> which means that its
 * first block has no tag.
 * </p>
 */
public class FormatJavadoc extends Javadoc {

	FormatJavadocBlock[] blocks;
	int textStart, textEnd;
	int lineStart, lineEnd;

public FormatJavadoc(int sourceStart, int sourceEnd, int length) {
	super(sourceStart, sourceEnd);
	if (length > 0) {
		this.blocks = new FormatJavadocBlock[length];
	}
}

/**
 * Return the first block of the javadoc or <code>null</code> if has no block
 * at all.
 *
 * @return a {@link FormatJavadocBlock} or <code>null</code>.
 */
public FormatJavadocBlock getFirstBlock() {
	if (this.blocks != null) {
		return this.blocks[0];
	}
	return null;
}

/**
 * Returns whether it has several lines or not.
 *
 * @return <code>true</code> if the javadoc comment has several lines
 * 	<code>false</code> otherwise (e.g. header and footer are on the same
 * 	line).
 */
public boolean isMultiLine() {
	return this.lineStart < this.lineEnd;
}

public String toDebugString(char[] source) {
	if (this.blocks == null) {
		return "No block in current Javadoc comment"; //$NON-NLS-1$
	}
	StringBuffer buffer = new StringBuffer();
	int length = this.blocks.length;
	for (int i=0; i<length; i++) {
		this.blocks[i].toStringDebug(buffer, source);
		buffer.append('\n');
	}
	return buffer.toString();
}

}
