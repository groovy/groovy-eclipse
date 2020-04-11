/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;

public class CompactConstructorDeclaration extends ConstructorDeclaration {

	public RecordDeclaration recordDeclaration;

	public CompactConstructorDeclaration(CompilationResult compilationResult) {
		super(compilationResult);
	}
	@Override
	public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
		parser.parse(this, unit, false);
		this.containsSwitchWithTry = parser.switchWithTry;
	}
	@Override
	protected void checkAndGenerateFieldAssignment(FlowContext flowContext, FlowInfo flowInfo, FieldBinding field) {
		if (field.isStatic() ||
				flowInfo.isDefinitelyAssigned(field) || flowInfo.isPotentiallyAssigned(field))
			return;
		assert field.isFinal();
		/* JLS 14 8.10.5 Compact Record Constructor Declarations
		 * In addition, at the end of the body of the compact constructor, all the fields
		 * corresponding to the record components of R that are definitely unassigned
		 * (16 (Definite Assignment)) are implicitly initialized to the value of the
		 * corresponding formal parameter. These fields are implicitly initialized in the
		 * order that they are declared in the record component list.
		 */
		FieldReference lhs = new FieldReference(field.name,0);
		lhs.receiver = new ThisReference(0, 0);
		//TODO: Check whether anything has to be done for null analysis.
		Assignment assignment = new Assignment(lhs, new SingleNameReference(field.name, 0), 0);
		assignment.resolveType(this.scope);
		assignment.analyseCode(this.scope, flowContext, flowInfo);
		assignment.bits |= ASTNode.IsImplicit;
		assert flowInfo.isDefinitelyAssigned(field);
		Statement[] stmts = this.statements;
		if (this.statements == null) {
			this.statements = new Statement[] { assignment };
		} else {
			int len = this.statements.length;
			System.arraycopy(
					this.statements,
					0,
					stmts = new Statement[len + 1],
					0,
					len);
			stmts[len] = assignment;
			this.statements = stmts;
		}
	}
}