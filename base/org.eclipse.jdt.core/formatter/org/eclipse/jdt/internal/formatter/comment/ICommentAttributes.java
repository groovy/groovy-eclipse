/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.formatter.comment;

/**
 * General comment range attributes.
 *
 * @since 3.0
 */
public interface ICommentAttributes {

	/** Range has blank line attribute */
	public static final int COMMENT_BLANKLINE= 1 << 1;

	/** Range has line break attribute */
	public static final int COMMENT_BREAK= 1 << 2;

	/** Range has close tag attribute */
	public static final int COMMENT_CLOSE= 1 << 3;

	/** Range has source code attribute */
	public static final int COMMENT_CODE= 1 << 4;

	/** Range has html tag attribute */
	public static final int COMMENT_HTML= 1 << 5;

	/** Range has the immutable region attribute */
	public static final int COMMENT_IMMUTABLE= 1 << 6;

	/** Range has new line attribute */
	public static final int COMMENT_NEWLINE= 1 << 7;

	/** Range has open tag attribute */
	public static final int COMMENT_OPEN= 1 << 8;

	/** Range has paragraph attribute */
	public static final int COMMENT_PARAGRAPH= 1 << 9;

	/** Range has parameter tag attribute */
	public static final int COMMENT_PARAMETER= 1 << 10;

	/** Range has root tag attribute */
	public static final int COMMENT_ROOT= 1 << 11;

	/** Range has paragraph separator attribute */
	public static final int COMMENT_SEPARATOR= 1 << 12;

	/** Range is the first token on the line in the original source */
	public static final int COMMENT_FIRST_TOKEN= 1 << 13;

	/**
	 * Range was preceded by whitespace / line delimiters
	 * @since 3.1
	 */
	public static final int COMMENT_STARTS_WITH_RANGE_DELIMITER= 1 << 14;
}
