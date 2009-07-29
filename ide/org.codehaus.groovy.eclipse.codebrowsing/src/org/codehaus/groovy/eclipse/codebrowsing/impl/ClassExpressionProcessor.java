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
