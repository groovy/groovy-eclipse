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

import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.eclipse.codebrowsing.IDeclarationSearchInfo;
import org.codehaus.groovy.eclipse.codebrowsing.IDeclarationSearchProcessor;
import org.codehaus.groovy.eclipse.codebrowsing.SourceCodeFinder;
import org.eclipse.jdt.core.IJavaElement;

/**
 * @author emp
 */
public class FieldNodeProcessor implements
		IDeclarationSearchProcessor {
	public IJavaElement[] getProposals(IDeclarationSearchInfo info) {
		FieldNode fieldNode = (FieldNode) info.getASTNode();

		if (fieldNode.isDynamicTyped()) {
			return NONE;
		}
		
		IJavaElement sourceCode = SourceCodeFinder.find(fieldNode.getType(), info.getEditorFacade().getFile());
		if (sourceCode != null) {
			return new IJavaElement[] { sourceCode };
		}

		return NONE;
	}
}