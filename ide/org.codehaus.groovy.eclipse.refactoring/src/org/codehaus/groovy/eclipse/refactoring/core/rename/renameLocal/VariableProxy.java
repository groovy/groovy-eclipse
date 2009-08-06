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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.VariableExpression;

/**
 * class to simplify the access to different kinds of ast nodes
 * that are candidates for a rename refactoring
 * @author reto kleeb
 *
 */
public class VariableProxy extends ASTNode {
	
	private Variable variable;

	public VariableProxy() {
		
	}
	
	public VariableProxy(Variable variable) {
		this.variable = variable;
	}
	
	public Variable getVariable() {
		return variable;
	}

	public void setVariable(Variable variable) {
		this.variable = variable;
	}
	
	public String getName() {
		return variable.getName();
	}
	
	public boolean isClosureSharedVariable(){
		return variable.isClosureSharedVariable();
	}
	


	@Override
	public String toString() {
		return 	"(" + getLineNumber() + "/" + getColumnNumber() + ") - (" 
				+ getLastLineNumber() + "/" + getLastColumnNumber() + "): "
				+ getName() + "\t(" + variable.getClass() + ")";
	}
	
	public Variable getAccessedVariable() {
		if(variable instanceof VariableExpression){
			VariableExpression v = (VariableExpression) variable;
			return v.getAccessedVariable();
		}
		return variable;
	}

}
