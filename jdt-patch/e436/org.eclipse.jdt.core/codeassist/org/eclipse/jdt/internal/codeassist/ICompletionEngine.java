/*******************************************************************************
 * Copyright (c) 2025 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;

/**
 * Represents a class that can provide completion for a compilation unit.
 */
public interface ICompletionEngine {

	/**
	 * Provide completion items based on the given source unit and completion position to the configured requestor.
	 *
	 * @param sourceUnit the compilation unit for which completion was triggered
	 * @param completionPosition the position that completion was triggered at
	 * @param adjustment the amount to subtract from all positions in completion proposals passed to the requestor
	 * @param root the type root for which completion was triggered
	 */
	public void complete(ICompilationUnit sourceUnit, int completionPosition, int adjustment, ITypeRoot root);

}
