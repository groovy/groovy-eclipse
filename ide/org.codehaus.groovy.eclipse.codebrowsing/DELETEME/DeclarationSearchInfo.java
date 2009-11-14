 /*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.codebrowsing;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.core.model.IDocumentFacade;
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
