 /*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.codehaus.groovy.eclipse.core.types.GroovyDeclaration;
import org.codehaus.groovy.eclipse.core.types.TypeUtil;

public class GroovyParametersTable implements ISymbolTable, ISourceCodeContextAware {
	private ISourceCodeContext context;

	private VariableScope scope;

	public GroovyDeclaration lookup(String name) {
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
