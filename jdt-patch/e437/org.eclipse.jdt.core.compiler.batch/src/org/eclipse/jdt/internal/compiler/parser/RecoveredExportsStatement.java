/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
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
 *
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

import org.eclipse.jdt.internal.compiler.ast.ExportsStatement;

public class RecoveredExportsStatement extends RecoveredPackageVisibilityStatement {

	public RecoveredExportsStatement(ExportsStatement exportsStatement, RecoveredElement parent, int bracketBalance) {
		super(exportsStatement, parent, bracketBalance);
	}
	@Override
	public String toString(int tab) {
		return tabString(tab) + "Recovered exports stmt: " + super.toString(); //$NON-NLS-1$
	}
}
