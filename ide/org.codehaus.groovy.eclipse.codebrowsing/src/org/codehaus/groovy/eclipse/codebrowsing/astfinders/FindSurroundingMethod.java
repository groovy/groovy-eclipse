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
package org.codehaus.groovy.eclipse.codebrowsing.astfinders;

import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.eclipse.core.util.ASTUtils;

/**
 * Given some lineNumber, columnNumber, find the MethodNode surrounding the
 * lineNumber, columnNumber.
 * 
 * @author emp
 */
public class FindSurroundingMethod extends ClassCodeVisitorSupport {
	private ModuleNode moduleNode;

	private ClassNode classNode;

	private int lineNumber;

	private int columnNumber;

	@Override
    protected SourceUnit getSourceUnit() {
		// Nothing to do.
		return null;
	}

	/**
	 * Find a MethodNode surrounding the given lineNumber, columnNumber. If a
	 * MethodNode is found, the identifier field is set to 'method node', and
	 * the lineNumber, columnNumber is set to the found MethodNode AST
	 * lineNumber, columnNumber.
	 * 
	 * @param moduleNode
	 * @param lineNumber
	 * @param columnNumber
	 * @throws ASTNodeFoundException
	 */
	public void doFind(ModuleNode moduleNode, int lineNumber, int columnNumber) {
		this.moduleNode = moduleNode;
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;

		List<ClassNode> classes = moduleNode.getClasses();
		for (Iterator<ClassNode> iter = classes.iterator(); iter.hasNext();) {
			classNode = iter.next();
			classNode.visitContents(this);
		}
	}

	@Override
    public void visitMethod(MethodNode node) {
		if (ASTUtils.isInsideNode(node, lineNumber, columnNumber)) {
			throw new ASTNodeFoundException(moduleNode, classNode, node,
					"method node");
		}
		super.visitMethod(node);
	}
}