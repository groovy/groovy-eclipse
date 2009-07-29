/* 
 * Copyright (C) 2008, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.predicates;

import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTVisitorDecorator;

public class IncludesClosurePredicate extends ASTVisitorDecorator<Boolean> {

	private final int line;
	public IncludesClosurePredicate(Boolean container,int line) {
		super(container);
		this.line = line;
	}

	@Override
    public void visitClosureExpression(ClosureExpression expression) {
		if(expression.getLineNumber() == line)
			container = true;
	}
	
}
