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