/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
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
package org.codehaus.groovy.eclipse.refactoring.core.extractMethod;

import java.util.ArrayList;
import java.util.List;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.eclipse.refactoring.core.UserSelection;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTTools;
import org.codehaus.groovy.eclipse.refactoring.core.utils.SourceCodePoint;
import org.eclipse.jface.text.IDocument;

/**
 * Class to scan a document and extract all statements which are covered by a
 * given Selection. A valid selection must meet this criteria: - Selection in a
 * Method - Covers in minimum a whole Statement - is in a BlockStatement or
 * contains a whole BlockStatement
 * 
 * @author Michael Klenk mklenk@hsr.ch
 * 
 */
public class StatementFinder extends CodeVisitorSupport {

	private List<Statement> preSelection, inSelection, postSelection;
	private boolean isInClosure;
	private MethodNode methodNode,actualMethod;
	private ClassNode classNode,actualClass;
	private final UserSelection selection;
	private final IDocument document;
	private final ModuleNode rootNode;
	boolean preCode = true;
	boolean selectionIsInLoopOrClosure = false;
	boolean inLoop = false;

	/**
	 * @param selection
	 * @param document
	 * @param rootNode
	 */
	public StatementFinder(UserSelection selection, IDocument document,
			ModuleNode rootNode) {
		this.selection = selection;
		this.document = document;
		this.rootNode = rootNode;
		scanDocument();
	}

	/**
	 * Returns true if the selection is in a static Method
	 * 
	 * @return
	 */
	public boolean isStatic() {
		return methodNode.isStatic();
	}
	
	/**
	 * Returns true if the selection is in the constructor
	 * @return
	 */
	public boolean isInConstructor() {
		return (methodNode != null && methodNode.getName().equals("<init>"));
	}

	/**
	 * Returns True if the Selection is in a Closure
	 * 
	 * @return
	 */
	public boolean isInClosure() {
		return isInClosure;
	}

	/**
	 * Return true if the selection is in a loop or a closure
	 * @return
	 */
	public boolean selectionIsInLoopOrClosure() {
		return selectionIsInLoopOrClosure;
	}

	/**
	 * Return all Statements in front of the Selection
	 * 
	 * @return
	 */
	public List<Statement> getPreSelection() {
		return preSelection;
	}

	/**
	 * Return all Statements in the given Selection
	 * 
	 * @return
	 */
	public List<Statement> getInSelection() {
		return inSelection;
	}

	/**
	 * Return all Statements after the Selection
	 * 
	 * @return
	 */
	public List<Statement> getPostSelection() {
		return postSelection;
	}

	/**
	 * Returns the MethodNode in which contains the Selection
	 * 
	 * @return
	 */
	public MethodNode getMethodNode() {
		return methodNode;
	}
	
	/**
	 * Return a list of all method names, declared in the class which contains the selection
	 * @return List of method names
	 */
	public List<String> getMethodNames() {
		List<String> methods = new ArrayList<String>();
		if(classNode != null) {
			for(MethodNode method : (List<MethodNode>)classNode.getMethods()) {
				methods.add(method.getName());
			}
		}
		return methods;
	}

	/**
	 * Return the name of the class which contains the current selection
	 * @return
	 */
	public String getClassName() {
		if(classNode != null)
			return classNode.getNameWithoutPackage();
        return "";
	}
	
	/**
	 * Return the class which contains the selection
	 * @return
	 */
	public ClassNode getClassNode() {
		return classNode;
	}

	/**
	 * Finds all satements in the given editor and the given Selection
	 * 
	 * @param offset
	 * @param length
	 * @param editor
	 * @return
	 */
	public void scanDocument() {

		inSelection = new ArrayList<Statement>();
		preSelection = new ArrayList<Statement>();
		postSelection = new ArrayList<Statement>();

		if (rootNode != null) {
			for (ClassNode cl : (List<ClassNode>) rootNode.getClasses()) {
				actualClass = cl;
				for (ConstructorNode method : (List<ConstructorNode>) cl.getDeclaredConstructors()) {
					scanMethod(cl, method);			
				}
				for (MethodNode method : (List<MethodNode>) cl.getMethods()) {
					scanMethod(cl, method);
				}
			}
		}
	}

	private void scanMethod(ClassNode cl, MethodNode method) {
		if (testSelection(selection, method, document, false)
				|| (cl.isScript() && method.getName() != "main")) {
			if(method.getCode() instanceof BlockStatement) {
				actualMethod = method;
				visitBlockStatement(((BlockStatement) method.getCode()));
			}
		}
	}


	@Override
    public void visitBlockStatement(BlockStatement block) {
		for (Statement statement : (List<Statement>) block.getStatements()) {
			if (testStatementSelection(statement, true)) {
				inSelection.add(statement);
				if (inLoop) {
					selectionIsInLoopOrClosure = true;
				}
				preCode = false;
				methodNode = actualMethod;
				classNode = actualClass;
				
			} else {
				if (preCode)
					if (testStatementSelection(statement, false)) {
						statement.visit(this);
					} else {
						preSelection.add(statement);
					}
				else {
					postSelection.add(0, statement);
				}
			}
		}
	}

	@Override
	public void visitForLoop(ForStatement forLoop) {
		boolean oldInLoop = inLoop;
		inLoop = true;
		super.visitForLoop(forLoop);
		inLoop = oldInLoop;
	}

	@Override
	public void visitWhileLoop(WhileStatement loop) {
		boolean oldInLoop = inLoop;
		inLoop = true;
		super.visitWhileLoop(loop);
		inLoop = oldInLoop;
	}

	@Override
	public void visitDoWhileLoop(DoWhileStatement loop) {
		boolean oldInLoop = inLoop;
		inLoop = true;
		super.visitDoWhileLoop(loop);
		inLoop = oldInLoop;
	}

	@Override
	public void visitClosureExpression(ClosureExpression expression) {
		boolean oldInLoop = inLoop;
		inLoop = true;
		super.visitClosureExpression(expression);
		inLoop = oldInLoop;
	}

	/**
	 * Get the offset of the last position in the class which inchludes the
	 * current selection
	 * 
	 * @param offset
	 * @param length
	 * @param editor
	 * @param root
	 * @return the offset in the file before the closing } of the class
	 */
	public SourceCodePoint getPositionAfterMethodWithSelection() {
		if (rootNode != null) {
			if(methodNode.getLastColumnNumber() != -1 && methodNode.getLastLineNumber() != -1)
				return new SourceCodePoint(methodNode.getLastLineNumber(),methodNode.getLastColumnNumber());
            return new SourceCodePoint(document.getLength(),document);
		}
		return null;
	}

	/**
	 * Test if a Statement is in a Selection or contains a Selection
	 * 
	 * @param statement
	 * @param inSelection
	 * @return
	 */
	private boolean testStatementSelection(Statement statement,
			boolean inSelection) {

		return testSelection(selection, statement, document, inSelection);
	}

	/**
	 * Test if the given node is in the Selection or if the Selction contains
	 * the Node
	 * 
	 * @param offset
	 * @param length
	 * @param node
	 * @param editor
	 * @param inSelection
	 *            if true the node must be in the seletion
	 * @return
	 */
	public static boolean testSelection(UserSelection sel, ASTNode astNode,
			IDocument doc, boolean inSelection) {
	    ASTNode node = astNode;
		if(!ASTTools.hasValidPosition(node) && node instanceof ReturnStatement) {
			node = ((ReturnStatement) node).getExpression();
		}

		SourceCodePoint selectionStartPoint = new SourceCodePoint(sel
				.getOffset(), doc);
		SourceCodePoint selectionEndPoint = new SourceCodePoint(sel.getOffset()
				+ sel.getLength(), doc);

		SourceCodePoint nodeStartPoint = new SourceCodePoint(node
				.getLineNumber(), node.getColumnNumber());
		SourceCodePoint nodeEndPoint = new SourceCodePoint(node
				.getLastLineNumber(), node.getLastColumnNumber());

		if (inSelection)
			return isInSelection(selectionStartPoint, selectionEndPoint,
					nodeStartPoint, nodeEndPoint);
        return containsSelection(selectionStartPoint, selectionEndPoint,
        		nodeStartPoint, nodeEndPoint);
	}

	/**
	 * Test if the Node contains the Selection
	 * 
	 * @param selectionStartPoint
	 * @param selectionEndPoint
	 * @param nodeStartPoint
	 * @param nodeEndPoint
	 * @return
	 */
	private static boolean containsSelection(
			SourceCodePoint selectionStartPoint,
			SourceCodePoint selectionEndPoint, SourceCodePoint nodeStartPoint,
			SourceCodePoint nodeEndPoint) {

		return (nodeStartPoint.isBefore(selectionStartPoint) && nodeEndPoint
				.isAfter(selectionEndPoint) && nodeStartPoint.isBefore(nodeEndPoint));
	}

	/**
	 * Test if the Node is in the current Selection
	 * 
	 * @param selectionStartPoint
	 * @param selectionEndPoint
	 * @param nodeStartPoint
	 * @param nodeEndPoint
	 * @return
	 */
	private static boolean isInSelection(SourceCodePoint selectionStartPoint,
			SourceCodePoint selectionEndPoint, SourceCodePoint nodeStartPoint,
			SourceCodePoint nodeEndPoint) {

		return (selectionStartPoint.isBefore(nodeStartPoint) && selectionEndPoint
				.isAfter(nodeEndPoint) && nodeStartPoint.isBefore(nodeEndPoint));
	}

}
