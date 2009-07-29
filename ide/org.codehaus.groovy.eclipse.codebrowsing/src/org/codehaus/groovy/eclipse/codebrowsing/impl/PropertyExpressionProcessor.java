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
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.codebrowsing.IDeclarationSearchInfo;
import org.codehaus.groovy.eclipse.codebrowsing.IDeclarationSearchProcessor;
import org.codehaus.groovy.eclipse.editor.actions.IDocumentFacade;
import org.eclipse.jdt.core.IJavaElement;

/**
 * @author emp
 */
public class PropertyExpressionProcessor implements
		IDeclarationSearchProcessor {
	public IJavaElement[] getProposals(IDeclarationSearchInfo info) {
		PropertyExpression expr = (PropertyExpression) info.getASTNode();
		if (expr.getObjectExpression() instanceof VariableExpression) {
			VariableExpression vexpr = (VariableExpression) expr
					.getObjectExpression();
			if (!vexpr.getName().equals("this"))
				return NONE;

			FieldNode fieldNode = info.getClassNode().getField(
					expr.getPropertyAsString());
			if (fieldNode == null)
				return NONE;
			IDocumentFacade facade = info.getEditorFacade();
			IJavaElement elt = 
			    facade.getProjectFacade().groovyNodeToJavaElement(fieldNode, facade.getFile());
			return new IJavaElement[] { elt };
		}
		return NONE;
	}
}
