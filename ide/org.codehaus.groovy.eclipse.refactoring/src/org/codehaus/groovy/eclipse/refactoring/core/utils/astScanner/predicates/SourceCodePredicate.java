/* 
 * Copyright (C) 2008, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.predicates;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.stmt.BlockStatement;

public class SourceCodePredicate implements IASTNodePredicate {

	int line, col;
	
	public SourceCodePredicate(int line, int col) {
		this.line = line;
		this.col = col;
	}

	public ASTNode evaluate(ASTNode input) {
		if (/*!(input instanceof BlockStatement) &&*/ // ignore block statements because we are really interested in the statements that it contains.
				input.getLineNumber() == line && input.getColumnNumber() == col)
			return input;
		return null;
	}

}
