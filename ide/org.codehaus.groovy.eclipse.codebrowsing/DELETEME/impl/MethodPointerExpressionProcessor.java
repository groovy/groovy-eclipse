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
