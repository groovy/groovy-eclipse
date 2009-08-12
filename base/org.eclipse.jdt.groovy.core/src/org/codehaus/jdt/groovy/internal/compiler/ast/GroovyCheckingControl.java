/*******************************************************************************
 * Copyright (c) 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement     - Initial API and implementation
 *******************************************************************************/
package org.codehaus.jdt.groovy.internal.compiler.ast;

/**
 * Set of constants that control how much debugging is done.
 * 
 * @author Andy Clement
 */
public class GroovyCheckingControl {

	public final static boolean globalControl = true;

	// if true then type references will be verified when they are built
	public final static boolean checkTypeReferences = globalControl && true;

}
