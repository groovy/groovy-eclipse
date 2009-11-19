/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
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

package org.codehaus.groovy.eclipse.refactoring.core.rename.renameLocal;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.RefactoringCodeVisitorSupport;

public class LocalVariableCollector extends RefactoringCodeVisitorSupport {
	
	private final MethodNode renameLocalMethod;
	private BlockStatement script;
	private final List<String> usedNames = new ArrayList<String>();

	public LocalVariableCollector(ModuleNode rootNode, MethodNode renameLocalMethod) {
		super(rootNode);
		this.renameLocalMethod = renameLocalMethod;
		//Variable to rename is in a script
		if (renameLocalMethod == null) {
			script = rootNode.getStatementBlock();
		}
	}
	
	@Override
    public void scanAST() {
		if (renameLocalMethod != null) {
			visitMethod(renameLocalMethod);
		} else {
			//the variable is defined somewhere in: script, Closure, ForLoop
			script.visit(this);
		}
	}
	
	@Override
    public void analyzeParameter(Parameter parameter) {
		usedNames.add(parameter.getName());
		super.analyzeParameter(parameter);
	}
	
	@Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
		usedNames.add(expression.getVariableExpression().getName());
		super.visitDeclarationExpression(expression);
	}

	public List<String> getUsedNames() {
		return usedNames;
	}
}
