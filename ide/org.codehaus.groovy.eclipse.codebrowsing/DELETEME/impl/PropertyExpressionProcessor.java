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

import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.codebrowsing.IDeclarationSearchInfo;
import org.codehaus.groovy.eclipse.codebrowsing.IDeclarationSearchProcessor;
import org.codehaus.groovy.eclipse.core.model.IDocumentFacade;
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
