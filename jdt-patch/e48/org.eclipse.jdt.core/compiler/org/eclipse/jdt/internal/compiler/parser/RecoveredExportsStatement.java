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
package org.eclipse.jdt.internal.compiler.parser;

import org.eclipse.jdt.internal.compiler.ast.ExportsStatement;

public class RecoveredExportsStatement extends RecoveredPackageVisibilityStatement {

	public RecoveredExportsStatement(ExportsStatement exportsStatement, RecoveredElement parent, int bracketBalance) {
		super(exportsStatement, parent, bracketBalance);
	}
	public String toString(int tab) {
		return tabString(tab) + "Recovered exports stmt: " + super.toString(); //$NON-NLS-1$
	}
}
