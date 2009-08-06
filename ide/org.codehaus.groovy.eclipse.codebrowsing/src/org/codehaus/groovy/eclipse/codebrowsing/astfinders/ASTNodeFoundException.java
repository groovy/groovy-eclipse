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
