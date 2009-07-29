/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Edward Povazan   - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.codebrowsing;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.codebrowsing.astfinders.ASTNodeFoundException;
import org.eclipse.jface.text.IRegion;

/**
 * The result of searching an AST.
 * 
 * @author emp
 */
public class ASTSearchResult {
	private ASTNodeFoundException exception;

	public ASTSearchResult(ASTNodeFoundException e) {
		this.exception = e;
	}

	public ASTNode getASTNode() {
		return exception.getASTNode();
	}

	public ClassNode getClassNode() {
		return exception.getClassNode();
	}

	public String getIdentifier() {
		return exception.getIdentifier();
	}

	public IRegion getRegion() {
		return exception.getRegion();
	}
	
	public ModuleNode getModuleNode() {
		return exception.getModuleNode();
	}
}