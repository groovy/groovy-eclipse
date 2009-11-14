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
package org.codehaus.groovy.eclipse.codebrowsing.impl;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.codebrowsing.IDeclarationSearchInfo;
import org.codehaus.groovy.eclipse.codebrowsing.IDeclarationSearchProcessor;
import org.codehaus.groovy.eclipse.codebrowsing.SourceCodeFinder;
import org.eclipse.jdt.core.IJavaElement;

/**
 * <pre>
 *   class A extends B implement C, D 
 *         &circ;         &circ;           &circ;  &circ; 
 * </pre>
 * 
 * @author emp
 */
public class ClassNodeProcessor implements IDeclarationSearchProcessor {
	public IJavaElement[] getProposals(IDeclarationSearchInfo info) {
		ClassNode classNode = (ClassNode) info.getASTNode();
		
		// Clicked on the actual node, nothing to do.
		if (classNode == info.getClassNode()) {
			return NONE;
		}

		IJavaElement sourceCode = SourceCodeFinder.find(classNode, info.getEditorFacade().getFile());
		if (sourceCode != null) {
			return new IJavaElement[] { sourceCode };
		}

		return NONE;
	}
}