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
 * This exception can be thrown by setters in some subtype that should never be called.
 * 
 * @author Andy Clement
 */
public class ImmutableException extends IllegalStateException {

	private static final long serialVersionUID = 1L;

}
