/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 */
package org.codehaus.groovy.eclipse.refactoring.core.rename;

import org.codehaus.groovy.ast.ASTNode;

/**
 * 
 * @author martin, reto
 *
 */
public class NoRefactoringForASTNodeException extends Exception {

	private static final long serialVersionUID = -1815292409874984372L;
	
	private final ASTNode node;
	private final static String MESSAGE = "No Refactoring for this node implemented";
	
	public NoRefactoringForASTNodeException(ASTNode node) {
		this.node = node;
	}

	public ASTNode getNode() {
		return node;
	}

	@Override
    public String getMessage() {
		return MESSAGE;
	}

}
