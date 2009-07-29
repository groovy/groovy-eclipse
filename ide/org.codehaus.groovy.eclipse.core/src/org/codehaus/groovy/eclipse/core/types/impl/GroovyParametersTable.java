/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.core.types.impl;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContextAware;
import org.codehaus.groovy.eclipse.core.types.ISymbolTable;
import org.codehaus.groovy.eclipse.core.types.Type;
import org.codehaus.groovy.eclipse.core.types.TypeUtil;

public class GroovyParametersTable implements ISymbolTable, ISourceCodeContextAware {
	private ISourceCodeContext context;

	private VariableScope scope;

	public Type lookup(String name) {
		if (context == null || name.equals("this")) {
			return null;
		}

		Variable var = (Variable) scope.getReferencedLocalVariable(name);
		if (var != null) {
			return TypeUtil.newLocalVariable(var);
		}

		return null;
	}

	public void setSourceCodeContext(ISourceCodeContext context) {
		this.context = context;
		// setup access to symbol table here from context.
		ASTNode[] path = context.getASTPath();
		ASTNode astNode = path[path.length - 1];
		if (astNode instanceof MethodNode) {
			astNode = ((MethodNode) astNode).getCode();
		} else if (astNode instanceof ClosureExpression) {
			astNode = ((ClosureExpression)astNode).getCode();
		} else if (astNode instanceof MethodCallExpression) {
			ArgumentListExpression ale = (ArgumentListExpression) ((MethodCallExpression)astNode).getArguments();
			if (ale.getExpression(0) instanceof ClosureExpression) {
				ClosureExpression cl = (ClosureExpression) ale.getExpression(0);
				astNode = cl.getCode();
			}
		}
		scope = ((BlockStatement) astNode).getVariableScope();
	}
}
