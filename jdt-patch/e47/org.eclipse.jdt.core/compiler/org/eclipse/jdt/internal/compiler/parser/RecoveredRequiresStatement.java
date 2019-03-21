/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

import org.eclipse.jdt.internal.compiler.ast.RequiresStatement;

public class RecoveredRequiresStatement extends RecoveredModuleStatement {

	public RecoveredRequiresStatement(RequiresStatement requiresStatement, RecoveredElement parent, int bracketBalance) {
		super(requiresStatement, parent, bracketBalance);
	}
	public String toString(int tab) {
		return tabString(tab) + "Recovered requires: " + super.toString(); //$NON-NLS-1$
	}
	public RequiresStatement updatedRequiresStatement(){
		return (RequiresStatement)this.moduleStatement;
	}
}
