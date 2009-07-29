/*******************************************************************************
 * Copyright (c) 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement        - Initial API and implementation
 *******************************************************************************/
package org.codehaus.jdt.groovy.internal.compiler.ast;

/**
 * Indicates something unexpected has happened. Should never occur when code is running as designed.
 * 
 * @author Andy Clement
 */
public class GroovyEclipseBug extends IllegalStateException {

	private static final long serialVersionUID = 1L;

	public GroovyEclipseBug(String message) {
		super(message);
	}

	public GroovyEclipseBug() {
	}

}
