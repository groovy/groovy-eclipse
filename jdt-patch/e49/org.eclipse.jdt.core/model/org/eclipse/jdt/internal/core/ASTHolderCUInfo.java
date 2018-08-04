/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.CompilationUnit;

@SuppressWarnings("rawtypes")
public class ASTHolderCUInfo extends CompilationUnitElementInfo {
	int astLevel;
	boolean resolveBindings;
	int reconcileFlags;
	HashMap problems = null;
	CompilationUnit ast;
}
