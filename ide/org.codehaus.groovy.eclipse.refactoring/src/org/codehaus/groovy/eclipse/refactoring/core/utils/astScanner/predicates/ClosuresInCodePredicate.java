/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */

package org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.predicates;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTTools;

public class ClosuresInCodePredicate implements IASTNodePredicate {

	public ASTNode evaluate(ASTNode input) {
		if (input instanceof ClosureExpression) {
			ClosureExpression cl = (ClosureExpression) input;
			if (ASTTools.hasValidPosition(cl))
				return cl;
		}
		return null;
	}
}
