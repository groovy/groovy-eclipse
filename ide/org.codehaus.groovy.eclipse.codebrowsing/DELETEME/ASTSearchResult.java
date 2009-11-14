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