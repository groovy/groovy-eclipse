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
import org.codehaus.groovy.eclipse.editor.actions.IDocumentFacade;
import org.eclipse.jface.text.IRegion;

/**
 * Default implemenatation of IDeclarationSearchInfo.
 * 
 * @author emp
 */
public class DeclarationSearchInfo implements IDeclarationSearchInfo {
	private String identifier;

	private IDocumentFacade facade;

	private IRegion region;

	private ModuleNode moduleNode;

	private ClassNode classNode;

	private ASTNode astNode;

	public DeclarationSearchInfo(ASTSearchResult result,
	        IDocumentFacade facade, IRegion region) {
		this(result.getIdentifier(), facade, region, result.getModuleNode(),
				result.getClassNode(), result.getASTNode());
	}

	public DeclarationSearchInfo(String identifier, IDocumentFacade facade,
			IRegion region, ModuleNode moduleNode, ClassNode classNode,
			ASTNode astNode) {
		this.identifier = identifier;
		this.facade = facade;
		this.region = region;
		this.moduleNode = moduleNode;
		this.classNode = classNode;
		this.astNode = astNode;
	}

	public String getIdentifier() {
		return identifier;
	}

	public IRegion getRegion() {
		return region;
	}

	public ModuleNode getModuleNode() {
		return moduleNode;
	}

	public ClassNode getClassNode() {
		return classNode;
	}

	public ASTNode getASTNode() {
		return astNode;
	}

    public IDocumentFacade getEditorFacade() {
        return facade;
    }
}
