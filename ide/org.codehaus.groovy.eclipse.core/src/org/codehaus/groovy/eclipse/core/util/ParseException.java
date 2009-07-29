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
 * Exception thrown by the ExpressionFinder if the expression cannot be parsed.
 * @author empovazan
 */
public class ParseException extends Exception {
	private static final long serialVersionUID = -3475185632147480495L;
	private Token token;
	
	public ParseException(Token token) {
		super("Unexpected token: " + token.toString());
		this.token = token;
	}

	public Token getToken() {
		return token;
	}
}
