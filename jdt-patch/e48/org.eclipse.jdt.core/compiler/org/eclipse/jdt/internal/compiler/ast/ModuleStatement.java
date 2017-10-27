/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

/**
 * Just a marker class to represent statements that can occur in a module declaration
 *
 */
public abstract class ModuleStatement extends ASTNode {

	public int declarationEnd;
	public int declarationSourceStart;
	public int declarationSourceEnd;
}
