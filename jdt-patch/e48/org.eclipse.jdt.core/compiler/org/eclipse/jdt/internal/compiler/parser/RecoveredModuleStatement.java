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

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.ModuleStatement;

public abstract class RecoveredModuleStatement extends RecoveredElement {

	public ModuleStatement moduleStatement;

	public RecoveredModuleStatement(ModuleStatement moduleStmt, RecoveredElement parent, int bracketBalance) {
		super(parent, bracketBalance);
		this.moduleStatement = moduleStmt;
	}
	/*
	 * Answer the associated parsed structure
	 */
	@Override
	public ASTNode parseTree(){
		return this.moduleStatement;
	}
	/*
	 * Answer the very source end of the corresponding parse node
	 */
	@Override
	public int sourceEnd(){
		return this.moduleStatement.declarationSourceEnd;
	}
	@Override
	public String toString(int tab) {
		return this.moduleStatement.toString();
	}
	protected ModuleStatement updatedModuleStatement(){
		return this.moduleStatement;
	}
	@Override
	public void updateParseTree(){
		updatedModuleStatement();
	}
	/*
	 * Update the declarationSourceEnd of the corresponding parse node
	 */
	@Override
	public void updateSourceEndIfNecessary(int bodyStart, int bodyEnd){
		if (this.moduleStatement.declarationSourceEnd == 0) {
			this.moduleStatement.declarationSourceEnd = bodyEnd;
			this.moduleStatement.declarationEnd = bodyEnd;
		}
	}
}
