/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.codebrowsing.selection;

import java.util.Stack;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ImportNodeCompatibilityWrapper;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.SpreadMapExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.ast.expr.UnaryPlusExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.ASTFragmentFactory;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.IASTFragment;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.ASTNodeFinder;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.groovy.eclipse.core.util.VisitCompleteException;
import org.eclipse.core.runtime.Assert;

/**
 * Finds the AST node that completely encloses the selection that is passed in.
 *
 * @author Andrew Eisenberg
 * @created May 7, 2010
 */
public class FindSurroundingNode extends ASTNodeFinder {

    public static enum VisitKind {
        SURROUNDING_NODE, // Find the surrounding node
        EXTRA_EXPAND, // Do an extra expand after finding the surrounding node
                      // if the region is exactly the same as the surrounding
        PARENT_STACK
        // do not filter out co-located or empty nodes. Keep the entire parent
        // stack as-is
    }

    private final VisitKind visitKind;

    private Stack<IASTFragment> nodeStack;

    private ASTFragmentFactory factory;

    // GRECLIPSE-781 must visit the run method of a script last
    private MethodNode runMethod;

    public FindSurroundingNode(Region r) {
        this(r, VisitKind.EXTRA_EXPAND);
    }

    public FindSurroundingNode(Region r, VisitKind visitKind) {
        super(r);
        nodeStack = new Stack<IASTFragment>();
        this.visitKind = visitKind;
        factory = new ASTFragmentFactory();
    }

    public IASTFragment doVisitSurroundingNode(ModuleNode module) {
        nodeStack.push(factory.createFragment(module));

        ASTNode node = super.doVisit(module);
        if (node == null && runMethod != null) {
            // GRECLIPSE-781 visit run method if it exists
            try {
                internalVisitConstructorOrMethod(runMethod, false);
            } catch (VisitCompleteException e) {
                // do nothing
            }
        }

        IASTFragment maybeNode = factory.createFragment(node);

        if (visitKind != VisitKind.PARENT_STACK) {
            // remove nodes that are colocated with the region as well as nodes
            // that have no source position
            while (!nodeStack.isEmpty()) {
                maybeNode = nodeStack.peek();
                if (maybeNode.getEnd() == 0 || (visitKind == VisitKind.EXTRA_EXPAND && super.getRegion().isSame(maybeNode))) {
                    nodeStack.pop();
                } else {
                    break;
                }
            }
        }
        return nodeStack.isEmpty() ? maybeNode : nodeStack.peek();
    }

	@Override
	public ASTNode doVisit(ModuleNode module) {
		Assert.isLegal(false, "Use doVisitSurroundingNode() instead");
		return super.doVisit(module);
	}

    public Stack<IASTFragment> getParentStack() {
        return nodeStack;
    }

    protected void reset() {
        nodeStack.clear();
        runMethod = null;
    }

    @Override
    public void visitAnnotations(AnnotatedNode node) {
        nodeStack.push(factory.createFragment(node));
        super.visitAnnotations(node);
        nodeStack.pop();
    }


    @Override
    public void visitAssertStatement(AssertStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitAssertStatement(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitBlockStatement(BlockStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitBlockStatement(node);
        // check(node);
        nodeStack.pop();
    }

    @Override
    public void visitBreakStatement(BreakStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitBreakStatement(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitCaseStatement(CaseStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitCaseStatement(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitContinueStatement(ContinueStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitContinueStatement(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitDoWhileLoop(DoWhileStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitDoWhileLoop(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitExpressionStatement(ExpressionStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitExpressionStatement(node);
        nodeStack.pop();
    }

    @Override
    public void visitIfElse(IfStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitIfElse(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitPackage(PackageNode node) {
        nodeStack.push(factory.createFragment(node));
        super.visitPackage(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitImports(ModuleNode module) {
        for (ImportNode importNode : new ImportNodeCompatibilityWrapper(module).getAllImportNodes()) {
            if (importNode.getType() != null) {
                nodeStack.push(factory.createFragment(importNode));
                check(importNode.getType());
                nodeStack.pop();
            }
        }
    }

    @Override
    public void visitSwitch(SwitchStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitSwitch(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitSynchronizedStatement(SynchronizedStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitSynchronizedStatement(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitThrowStatement(ThrowStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitThrowStatement(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitTryCatchFinally(TryCatchStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitTryCatchFinally(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitWhileLoop(WhileStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitWhileLoop(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitArgumentlistExpression(ArgumentListExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitArgumentlistExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitAttributeExpression(AttributeExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitAttributeExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitBinaryExpression(BinaryExpression node) {
        nodeStack.push(factory.createFragment(node));
        if (getRegion().regionIsCoveredByNode(node)) {
            // create an extra stack frame to precisely describe the region
            // being covered
            nodeStack.push(factory.createFragment(node, getRegion().getOffset(), getRegion().getEnd()));
        }
        super.visitBinaryExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitBitwiseNegationExpression(
            BitwiseNegationExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitBitwiseNegationExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitBooleanExpression(BooleanExpression node) {
        nodeStack.push(factory.createFragment(node));

        // we want to check the inner node
        // if the source locations are the same
        if (!isColocated(node.getExpression(), node)) {
            check(node);
        }
        super.visitBooleanExpression(node);
        nodeStack.pop();
    }

    /**
     * Checks to see if the two nodes have the same source location
     *
     * @param node1
     * @param node2
     * @return
     */
    private boolean isColocated(ASTNode node1, ASTNode node2) {
        return node1.getStart() == node2.getStart() && node1.getEnd() == node2.getEnd();
    }

    @Override
    public void visitClosureListExpression(ClosureListExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitClosureListExpression(node);
        check(node);
        nodeStack.pop();
    }

    // Method doesn't exist in 1.6 stream
    // @Override
    protected void visitEmptyStatement(EmptyStatement node) {
        nodeStack.push(factory.createFragment(node));
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitListExpression(ListExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitListExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitMapEntryExpression(MapEntryExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitMapEntryExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitMapExpression(MapExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitMapExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression node) {
        nodeStack.push(factory.createFragment(node));
        if (getRegion().regionIsCoveredByNode(node)) {
            // create an extra stack frame to precisely describe the region
            // being covered
            nodeStack.push(factory.createFragment(node, getRegion().getOffset(), getRegion().getEnd()));
        }
        super.visitMethodCallExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitMethodPointerExpression(MethodPointerExpression node) {
        nodeStack.push(factory.createFragment(node));
        if (getRegion().regionIsCoveredByNode(node)) {
            // create an extra stack frame to precisely describe the region
            // being covered
            nodeStack.push(factory.createFragment(node, getRegion().getOffset(), getRegion().getEnd()));
        }
        super.visitMethodPointerExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitNotExpression(NotExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitNotExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitPostfixExpression(PostfixExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitPostfixExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitPrefixExpression(PrefixExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitPrefixExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitPropertyExpression(PropertyExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitPropertyExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitRangeExpression(RangeExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitRangeExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitShortTernaryExpression(ElvisOperatorExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitShortTernaryExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitSpreadExpression(SpreadExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitSpreadExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitSpreadMapExpression(SpreadMapExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitSpreadMapExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitTernaryExpression(TernaryExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitTernaryExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitTupleExpression(TupleExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitTupleExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitUnaryMinusExpression(UnaryMinusExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitUnaryMinusExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitUnaryPlusExpression(UnaryPlusExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitUnaryPlusExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitArrayExpression(ArrayExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitArrayExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitCastExpression(CastExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitCastExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitCatchStatement(CatchStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitCatchStatement(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitClass(ClassNode node) {
        nodeStack.push(factory.createFragment(node));
        super.visitClass(node);

        // explicitly check the class itself since super only
        // checks the class name
        // but only check if the node is not a script because
        // the script's run method will be checked separately
        if (!node.isScript()) {
            check(node);
        }
        nodeStack.pop();
    }

    @Override
    public void visitClassExpression(ClassExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitClassExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitClosureExpression(ClosureExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitClosureExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitConstantExpression(ConstantExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitConstantExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitConstructorCallExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    protected void visitConstructorOrMethod(MethodNode node,
            boolean isConstructor) {

        // GRECLIPSE-781 must visit run method last.
        if (isRunMethod(node)) {
            runMethod = node;
            return;
        }

        internalVisitConstructorOrMethod(node, isConstructor);
    }

    /**
     * @param node
     * @param isConstructor
     */
    private void internalVisitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        nodeStack.push(factory.createFragment(node));
        super.visitConstructorOrMethod(node, isConstructor);
        // explicitly check the method itself since super only
        // checks the method name
        check(node);
        nodeStack.pop();
    }

    /**
     * @param node
     * @return
     */
    private boolean isRunMethod(MethodNode node) {
        return node.getDeclaringClass().isScript() && node.getName().equals("run")
                && (node.getParameters() == null || node.getParameters().length == 0);
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression node) {
        nodeStack.push(factory.createFragment(node));
        if (getRegion().regionIsCoveredByNode(node)) {
            // create an extra stack frame to precisely describe the region
            // being covered
            nodeStack.push(factory.createFragment(node, getRegion().getOffset(), getRegion().getEnd()));
        }
        super.visitDeclarationExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitField(FieldNode node) {
        if (node.getName().startsWith("$")) {
            // synthetic
            return;
        }
        nodeStack.push(factory.createFragment(node));
        super.visitField(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitProperty(PropertyNode node) {
    // don't visit properties
    }

    @Override
    public void visitFieldExpression(FieldExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitFieldExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitForLoop(ForStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitForLoop(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitGStringExpression(GStringExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitGStringExpression(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitReturnStatement(ReturnStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitReturnStatement(node);
        check(node);
        nodeStack.pop();
    }

    @Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitStaticMethodCallExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitVariableExpression(VariableExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitVariableExpression(node);
        nodeStack.pop();
    }
}