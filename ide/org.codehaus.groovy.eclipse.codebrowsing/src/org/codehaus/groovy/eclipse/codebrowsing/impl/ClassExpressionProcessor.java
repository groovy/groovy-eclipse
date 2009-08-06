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

import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.eclipse.codebrowsing.IDeclarationSearchInfo;
import org.codehaus.groovy.eclipse.codebrowsing.IDeclarationSearchProcessor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;

/**
 * @author emp
 */
public class ClassExpressionProcessor implements
		IDeclarationSearchProcessor {
    
	@SuppressWarnings("unchecked")
    public IJavaElement[] getProposals(IDeclarationSearchInfo info) {
		ClassExpression expr = (ClassExpression) info.getASTNode();
		ClassNode classNode = null;
		List classes = info.getModuleNode().getClasses();
		for (Iterator iter = classes.iterator(); iter.hasNext();) {
			ClassNode test = (ClassNode) iter.next();
			if (test.getName().equals(expr.getType().getName())) {
				classNode = test;
				break;
			}
		}
		
		// Either metaclass, or base class - add tests laster.
		if (classNode == null) {
			return NONE;
		}
		IType type = info.getEditorFacade().getProjectFacade().groovyClassToJavaType(classNode);
		return type != null ? new IJavaElement[] { type } : NONE;

	}
}
