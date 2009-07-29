/* 
 * Copyright (C) 2008, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.inlineMethod;

import java.util.List;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTVisitorDecorator;

/**
 * @author mklenk
 *
 */
public class FindVariables extends ASTVisitorDecorator<List<Variable>> {

	public FindVariables(List<Variable> container) {
		super(container);
	}
		
	@Override
    public void visitVariableExpression(VariableExpression expression) {
		container.add(expression);		
		super.visitVariableExpression(expression);
	}
	
	@Override
    public void visitConstantExpression(ConstantExpression expression) {
		Object val = expression.getValue();
		if (val instanceof String) {
			val = "\"" + val + "\"";
		}
		
		container.add(new VariableExpression(val.toString()));
		super.visitConstantExpression(expression);
	}

}
