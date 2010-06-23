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
package org.codehaus.groovy.eclipse.refactoring.core.extract;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTTools;

/**
 * Scans a document to extract all statements which are covered by a
 * given Selection. A valid selection must meet this criteria:
 * - Selection is in a Method
 * - Covers at minimum a whole Statement
 * - is in a BlockStatement or contains a whole BlockStatement
 *
 * @author Michael Klenk mklenk@hsr.ch
 *
 */
public class StatementFinder extends CodeVisitorSupport {

    private List<Statement> inSelection, postSelection;

    /**
     * The declaration that contains the selection
     */
    private AnnotatedNode actualSelectedDeclaration;

    /**
     * The declaration currently being scanned
     */
    private AnnotatedNode currentDeclaration;

    private final Region selection;
	private final ModuleNode rootNode;

    /**
     * True when we have not found the selection yet
     */
	boolean preCode = true;
	boolean isInLoopOrClosure = false;
	boolean internalInLoopOrClosure = false;

    public StatementFinder(Region selection, ModuleNode rootNode) {
		this.selection = selection;
		this.rootNode = rootNode;
		scanDocument();
	}

    /**
     * @return true if the selection is in a static Method
     */
	public boolean isStatic() {
		return actualSelectedDeclaration instanceof MethodNode ? ((MethodNode) actualSelectedDeclaration).isStatic() : ((FieldNode) actualSelectedDeclaration).isStatic();
	}

    /**
     * @return true if the selection is in the constructor
     */
	public boolean isInConstructor() {
		return (actualSelectedDeclaration instanceof MethodNode && ((MethodNode) actualSelectedDeclaration).getName().equals("<init>"));
	}

	/**
	 * Return true if the selection is in a loop or a closure
	 * @return
	 */
	public boolean isInLoopOrClosure() {
		return isInLoopOrClosure;
	}

    /**
     * @return all Statements in the given Selection
     */
	public List<Statement> getInSelection() {
		return inSelection;
	}

    /**
     * @return all Statements after the Selection
     */
	public List<Statement> getPostSelection() {
		return postSelection;
	}

    /**
     * @return the declaration node in which contains the Selection (can be
     *         method or field)
     */
	public AnnotatedNode getSelectedDeclaration() {
		return actualSelectedDeclaration;
	}

	/**
	 * Return a list of all method names, declared in the class which contains the selection
	 * @return List of method names
	 */
	public List<String> getMethodNames() {
		List<String> methods = new ArrayList<String>();
        if (actualSelectedDeclaration != null) {
            ClassNode declaringClass = actualSelectedDeclaration.getDeclaringClass();
            for (MethodNode method : (List<MethodNode>) declaringClass.getMethods()) {
				methods.add(method.getName());
			}
		}
		return methods;
	}

    /**
     * @return the name of the class that contains the current selection
     */
	public String getClassName() {
        ClassNode declaringClass = actualSelectedDeclaration.getDeclaringClass();
        if (declaringClass != null)
            return declaringClass.getNameWithoutPackage();
        return "";
	}

    /**
     * @return the class which contains the selection
     */
	public ClassNode getClassNode() {
        return actualSelectedDeclaration.getDeclaringClass();
	}

	/**
	 * Finds all satements in the given editor and the given Selection
	 */
	public void scanDocument() {

		inSelection = new ArrayList<Statement>();
		postSelection = new ArrayList<Statement>();

		if (rootNode != null) {
			for (ClassNode cl : (List<ClassNode>) rootNode.getClasses()) {
				for (ConstructorNode method : (List<ConstructorNode>) cl.getDeclaredConstructors()) {
					scanMethod(cl, method);
				}
				for (MethodNode method : (List<MethodNode>) cl.getMethods()) {
					scanMethod(cl, method);
				}
				for (FieldNode field : (List<FieldNode>) cl.getFields()) {
				    scanField(cl, field);
				}

			}
		}
	}

    private void scanField(ClassNode cl, FieldNode field) {
        if (testSelection(selection, field, SelectionTestKind.SELECTION_IS_COVERED_BY)) {
            if (field.getInitialExpression() instanceof ClosureExpression) {
                Statement closureBlock = ((ClosureExpression) field.getInitialExpression()).getCode();
                if (closureBlock instanceof BlockStatement) {
                    currentDeclaration = field;
                    visitBlockStatement((BlockStatement) closureBlock);
                }
            }
        }
    }

    private void scanMethod(ClassNode cl, MethodNode method) {
        if (testSelection(selection, method, SelectionTestKind.SELECTION_IS_COVERED_BY)) {
			if(method.getCode() instanceof BlockStatement) {
                currentDeclaration = method;
				visitBlockStatement(((BlockStatement) method.getCode()));
			}
		}
	}


	@Override
    public void visitBlockStatement(BlockStatement block) {
		for (Statement statement : (List<Statement>) block.getStatements()) {
            if (testSelection(selection, statement, SelectionTestKind.SELECTION_COVERS)) {
				inSelection.add(statement);
				if (internalInLoopOrClosure) {
					isInLoopOrClosure = true;
				}
				preCode = false;
                actualSelectedDeclaration = currentDeclaration;

			} else {
                if (preCode) {
                    if (testSelection(selection, statement, SelectionTestKind.SELECTION_IS_COVERED_BY)) {
						statement.visit(this);
					}
                } else {
					postSelection.add(0, statement);
				}
			}
		}
	}

	@Override
	public void visitForLoop(ForStatement forLoop) {
		boolean oldInLoop = internalInLoopOrClosure;
		internalInLoopOrClosure = true;
		super.visitForLoop(forLoop);
		internalInLoopOrClosure = oldInLoop;
	}

	@Override
	public void visitWhileLoop(WhileStatement loop) {
		boolean oldInLoop = internalInLoopOrClosure;
		internalInLoopOrClosure = true;
		super.visitWhileLoop(loop);
		internalInLoopOrClosure = oldInLoop;
	}

	@Override
	public void visitDoWhileLoop(DoWhileStatement loop) {
		boolean oldInLoop = internalInLoopOrClosure;
		internalInLoopOrClosure = true;
		super.visitDoWhileLoop(loop);
		internalInLoopOrClosure = oldInLoop;
	}

	@Override
	public void visitClosureExpression(ClosureExpression expression) {
		boolean oldInLoop = internalInLoopOrClosure;
		internalInLoopOrClosure = true;
		super.visitClosureExpression(expression);
		internalInLoopOrClosure = oldInLoop;
	}

    private enum SelectionTestKind {
        SELECTION_COVERS, SELECTION_IS_COVERED_BY
    }

    /**
     * Test if the given node is in the Selection or if the Selection contains
     * the Node
     *
     * if true the node must be in the selection
     *
     * @return
     */
    private boolean testSelection(Region sel, ASTNode astNode, SelectionTestKind inSelection) {
        if (sel.isEmpty()) {
            return false;
        }
	    ASTNode node = astNode;
		if(!ASTTools.hasValidPosition(node) && node instanceof ReturnStatement) {
			node = ((ReturnStatement) node).getExpression();
		}
        if (inSelection == SelectionTestKind.SELECTION_COVERS) {
            return sel.regionCoversNode(node);
        } else {
            return sel.regionIsCoveredByNode(node);
        }
	}
}
