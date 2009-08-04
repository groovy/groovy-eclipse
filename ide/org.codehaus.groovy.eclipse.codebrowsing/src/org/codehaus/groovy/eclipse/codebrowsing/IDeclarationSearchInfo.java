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
import org.codehaus.groovy.eclipse.core.model.IDocumentFacade;
import org.eclipse.jface.text.IRegion;

/**
 * Interface containing information required to start a declaration search.
 * 
 * @author emp
 */
public interface IDeclarationSearchInfo {
	/**
	 * @return The identifier. Required.
	 */
	public String getIdentifier();

	/**
	 * @return The editor which contains the identifier. Required.
	 */
	public IDocumentFacade getEditorFacade();

	/**
	 * @return The region the identifier occupies. Required.
	 */
	public IRegion getRegion();

	/**
	 * @return The ModuleNode, which may be null, or out of date.
	 * @see #getASTNode()
	 */
	public ModuleNode getModuleNode();

	/**
	 * @return The ClassNode, which may be null if an ASTNode cannot be found.
	 * @see #getASTNode()
	 */
	public ClassNode getClassNode();

	/**
	 * @return The ASTNode may be null if one cannot not be found. This occurs
	 *         when a file could not be compiled.
	 */
	public ASTNode getASTNode();
}
