/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Michael Klenk and others        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner;

public class NodeNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 4065684597255206462L;

	public NodeNotFoundException(String message) {
		super(message);
	}
}
