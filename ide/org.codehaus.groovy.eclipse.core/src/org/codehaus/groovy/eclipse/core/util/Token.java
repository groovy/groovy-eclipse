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
			"BRACE_BLOCK", "BRACK_BLOCK", "LINE_COMMENT", "BLOCK_COMMENT" , "LINE_BREAK", "DOUBLE_DOT", "SAFE_DEREF" };

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

	/** safe navigation operator ?. */
	public static final int SAFE_DEREF = 12;
	
	/** spread operator *. */
	public static final int SPREAD = 13;
	
	

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
