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
package org.codehaus.groovy.eclipse.codebrowsing.astfinders;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

/**
 * Exception thrown when the ASTNodeFinder finds an ASTNode.
 * 
 * @author emp
 */
public class ASTNodeFoundException extends RuntimeException {
	private static final long serialVersionUID = -4475120092640994581L;

	ModuleNode moduleNode;

	ClassNode classNode;

	ASTNode astNode;

	private String identifier;

	private IRegion region;
	
	public ASTNodeFoundException(ModuleNode moduleNode, ClassNode classNode,
			ASTNode astNode, String identifier, IRegion region) {
		this.moduleNode = moduleNode;
		this.classNode = classNode;
		this.astNode = astNode;
		this.identifier = identifier;
		this.region = region;
	}

	public ASTNodeFoundException(ModuleNode moduleNode, ClassNode classNode,
			ASTNode astNode, String identifier) {
		this(moduleNode, classNode, astNode, identifier, new Region(astNode.getStart(), astNode.getEnd()-astNode.getStart()));
	}

	public ASTNode getASTNode() {
		return astNode;
	}

	public ClassNode getClassNode() {
		return classNode;
	}

	public ModuleNode getModuleNode() {
		return moduleNode;
	}

	public String getIdentifier() {
		return identifier;
	}
	
	public IRegion getRegion() {
		return region;
	}
	
	public int getOffset() {
		return region.getOffset();
	}
	
	public int getLength() {
		return region.getLength();
	}
}
