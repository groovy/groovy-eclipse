/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.predicates;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.*;

public class AssignementAndCall implements IASTNodePredicate {

	public ASTNode evaluate(ASTNode input) {

		if (input instanceof ClosureExpression) {
			return input;
		} else if (isAssignement(input) || isMethodCall(input)) {
			return input;
		}
		return null;
	}

	public static boolean isAssignement(ASTNode node) {
		if (node instanceof DeclarationExpression) {
			return true;
		} else if (node instanceof BinaryExpression) {
			return true;
		} else if (node instanceof PostfixExpression) {
			return true;
		} else if (node instanceof PrefixExpression) {
			return true;
		}
		return false;
	}
	
	public static boolean isMethodCall(ASTNode node) {
		if (node instanceof StaticMethodCallExpression) {
			return true;
		}
		if (node instanceof MethodCallExpression) {
			return true;
		}
		return false;	
	}
}
