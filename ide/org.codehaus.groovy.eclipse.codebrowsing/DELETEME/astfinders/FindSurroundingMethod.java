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