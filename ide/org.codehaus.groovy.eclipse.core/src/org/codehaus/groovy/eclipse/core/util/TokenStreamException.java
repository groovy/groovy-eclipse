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

/**
 * Exception thrown by the TokenStream when an unexpected character is encountered.
 * @author empovazan
 */
public class TokenStreamException extends Exception {
	private static final long serialVersionUID = -582979413678809543L;

	TokenStreamException(char ch) {
		super("Unexpected character: " + ch);
	}

	public TokenStreamException(String message) {
		super(message);
	}
}
