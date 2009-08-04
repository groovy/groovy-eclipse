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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.eclipse.codebrowsing.IDeclarationSearchInfo;
import org.codehaus.groovy.eclipse.codebrowsing.IDeclarationSearchProcessor;
import org.codehaus.groovy.eclipse.core.model.IDocumentFacade;
import org.eclipse.jdt.core.IJavaElement;

/**
 * <pre>
 *   a.&amp;b
 *      &circ;
 * </pre>
 * 
 * @author emp
 */
public class MethodPointerExpressionProcessor implements
		IDeclarationSearchProcessor {
	public IJavaElement[] getProposals(IDeclarationSearchInfo info) {
		MethodPointerExpression expr = (MethodPointerExpression) info
				.getASTNode();
		String methodName = expr.getMethodName().toString();
		List<MethodNode> nodes = info.getClassNode().getMethods();
		List<IJavaElement> results = new ArrayList<IJavaElement>();
		for (MethodNode methodNode : nodes) {
			if (methodNode.getName().equals(methodName)) {
				IDocumentFacade facade = info.getEditorFacade();
				IJavaElement method = 
				    facade.getProjectFacade().groovyNodeToJavaElement(methodNode, facade.getFile());
				results.add(method);
			}
		}

		return results
				.toArray(new IJavaElement[results.size()]);
	}
}
