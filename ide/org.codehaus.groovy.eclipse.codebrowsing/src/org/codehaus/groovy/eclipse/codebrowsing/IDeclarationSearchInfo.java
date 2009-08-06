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
