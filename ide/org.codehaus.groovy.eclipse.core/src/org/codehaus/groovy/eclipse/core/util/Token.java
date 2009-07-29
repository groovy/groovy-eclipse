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
package org.codehaus.groovy.eclipse.core.util;

import java.io.Serializable;

/**
 * Tokens recognized for an expression.
 * 
 * @author empovazan
 */
public class Token implements Serializable {
	private static final long serialVersionUID = 384520235046067298L;

	private static final String[] names = new String[] { "EOF", "IDENT", "DOT", "SEMI", "QUOTED_STRING", "PAREN_BLOCK",
			"BRACE_BLOCK", "BRACK_BLOCK", "LINE_COMMENT", "BLOCK_COMMENT" , "LINE_BREAK", "DOUBLE_DOT" };

	public static final int EOF = 0;

	/**
	 * [0-9a-zA-Z_'"]* Note, can start with [0-9]
	 */
	public static final int IDENT = 1;

	public static final int DOT = 2;

	public static final int SEMI = 3;

	/** Any quoted string, single and triple */
	public static final int QUOTED_STRING = 4;

	/** '(' anything ')' */
	public static final int PAREN_BLOCK = 5;

	/** '{' anything '}' */
	public static final int BRACE_BLOCK = 6;

	/** '[' anything ']' */
	public static final int BRACK_BLOCK = 7;
	
	/** '//' */
	public static final int LINE_COMMENT = 8;
	
	/** '/_* ... *_/' */
	public static final int BLOCK_COMMENT = 9;
	
	/** '\n', '\r', '\n\r', '\r\n' */   
	public static final int LINE_BREAK = 10;
	
	/** range definition 10..23, 10..<23 */
	public static final int DOUBLE_DOT = 11;

	public int type;

	public int startOffset;

	public int endOffset;

	public String text;

//	private String desc;

	public Token(int type, int startOffset, int endOffset, String text) {
		this.type = type;
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.text = text;
	}

	public boolean equals(Object obj) {
		try {
			if (obj != null) {
				return type == ((Token) obj).type;
			}
		} catch (ClassCastException e) {
		}
		return false;
	}

	public int hashCode() {
		return names[type].hashCode() + type;
	}

	public String toString() {
		return names[type] + "[" + startOffset + ":" + endOffset + "] - " + text;
	}
	
}
