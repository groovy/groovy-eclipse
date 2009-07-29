/* 
 * Copyright (C) 2008, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.predicates;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.VariableExpression;

public class Variables implements IASTNodePredicate {

	public ASTNode evaluate(ASTNode input) {
		if (input instanceof VariableExpression) {
			return input;
		}
		return null;
	}

}
