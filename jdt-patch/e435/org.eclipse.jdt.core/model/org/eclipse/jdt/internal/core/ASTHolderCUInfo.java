// GROOVY PATCHED
/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.Map;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class ASTHolderCUInfo extends CompilationUnitElementInfo {
	public int astLevel;
	public boolean resolveBindings;
	public int reconcileFlags;
	public Map<String, CategorizedProblem[]> problems;
	public CompilationUnit ast;
}
