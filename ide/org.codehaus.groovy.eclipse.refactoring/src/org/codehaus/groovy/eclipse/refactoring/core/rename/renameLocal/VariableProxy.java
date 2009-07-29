/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
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
