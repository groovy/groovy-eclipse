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

import org.eclipse.jdt.internal.compiler.ast.OpensStatement;

public class RecoveredOpensStatement extends RecoveredPackageVisibilityStatement {

	public RecoveredOpensStatement(OpensStatement opensStatement, RecoveredElement parent, int bracketBalance) {
		super(opensStatement, parent, bracketBalance);
	}
	@Override
	public String toString(int tab) {
		return tabString(tab) + "Recovered opens stmt: " + super.toString(); //$NON-NLS-1$
	}
}
