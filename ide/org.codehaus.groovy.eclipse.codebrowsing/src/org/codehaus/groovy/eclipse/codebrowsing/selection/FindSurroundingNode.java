/*
 * Copyright 2009-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
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
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;

/**
 * Finds the AST node that completely encloses the selection that is passed in.
 */
public class FindSurroundingNode extends ASTNodeFinder {

    public enum VisitKind {
        /** find the surrounding node */
        SURROUNDING_NODE,
        /** do an extra expand if the region is exactly the same as the surrounding node */
        EXTRA_EXPAND,
        /** do not filter out co-located or empty nodes; keep the entire parent stack as-is */
        PARENT_STACK,
    }

    private final VisitKind visitKind;

    private final ASTFragmentFactory factory = new ASTFragmentFactory();

    private final Stack<IASTFragment> nodeStack = new Stack<>();

    public FindSurroundingNode(Region r) {
        this(r, VisitKind.EXTRA_EXPAND);
    }

    public FindSurroundingNode(Region r, VisitKind visitKind) {
        super(r);
        this.visitKind = visitKind;
    }

    public IASTFragment doVisitSurroundingNode(ModuleNode module) {
        nodeStack.push(factory.createFragment(module));

        Region sloc = this.sloc;
        ASTNode node = super.doVisit(module);
        IASTFragment maybeNode = factory.createFragment(node);

        if (visitKind != VisitKind.PARENT_STACK) {
            // remove nodes that are colocated with the region as well as nodes that have no source position
            while (!nodeStack.isEmpty()) {
                maybeNode = nodeStack.peek();
                if (maybeNode.getEnd() == 0 || (visitKind == VisitKind.EXTRA_EXPAND && sloc.isSame(maybeNode))) {
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
        return null;
    }

    public Stack<IASTFragment> getParentStack() {
        return nodeStack;
    }

    //--------------------------------------------------------------------------

    @Override
    public void visitPackage(PackageNode node) {
        nodeStack.push(factory.createFragment(node));
        super.visitPackage(node);
        //check(node);
        nodeStack.pop();
    }

    @Override
    public void visitImport(ImportNode node) {
        nodeStack.push(factory.createFragment(node));
        super.visitImport(node);
        //check(node);
        nodeStack.pop();
    }

    @Override
    public void visitClass(ClassNode node) {
        nodeStack.push(factory.createFragment(node));
        super.visitClass(node);
        if (!GroovyUtils.isScript(node)) {
            check(node);
        }
        nodeStack.pop();
    }

    @Override
    public void visitProperty(PropertyNode node) {
        // don't visit properties
    }

    @Override
    public void visitField(FieldNode node) {
        nodeStack.push(factory.createFragment(node));
        super.visitField(node);
        if (!node.isEnum()) { // enum bodies are included in the source range of the field node but are rooted elsewhere
            check(node);
        }
        nodeStack.pop();
    }

    @Override
    public void visitMethod(MethodNode node) {
        if (node == runMethod) return; // GRECLIPSE-781
        nodeStack.push(factory.createFragment(node));
        super.visitMethod(node);
        check(node);
        nodeStack.pop();
    }

    // statements:

    @Override
    public void visitAssertStatement(AssertStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitAssertStatement(node);
        nodeStack.pop();
    }

    @Override
    public void visitBlockStatement(BlockStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitBlockStatement(node);
        nodeStack.pop();
    }

    @Override
    public void visitBreakStatement(BreakStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitBreakStatement(node);
        nodeStack.pop();
    }

    @Override
    public void visitCaseStatement(CaseStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitCaseStatement(node);
        nodeStack.pop();
    }

    @Override
    public void visitCatchStatement(CatchStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitCatchStatement(node);
        nodeStack.pop();
    }

    @Override
    public void visitContinueStatement(ContinueStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitContinueStatement(node);
        nodeStack.pop();
    }

    @Override
    public void visitDoWhileLoop(DoWhileStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitDoWhileLoop(node);
        nodeStack.pop();
    }

    @Override
    public void visitEmptyStatement(EmptyStatement node) {
        nodeStack.push(factory.createFragment(node));
        nodeStack.pop();
    }

    @Override
    public void visitExpressionStatement(ExpressionStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitExpressionStatement(node);
        nodeStack.pop();
    }

    @Override
    public void visitForLoop(ForStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitForLoop(node);
        nodeStack.pop();
    }

    @Override
    public void visitIfElse(IfStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitIfElse(node);
        nodeStack.pop();
    }

    @Override
    public void visitReturnStatement(ReturnStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitReturnStatement(node);
        nodeStack.pop();
    }

    @Override
    public void visitSwitch(SwitchStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitSwitch(node);
        nodeStack.pop();
    }

    @Override
    public void visitSynchronizedStatement(SynchronizedStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitSynchronizedStatement(node);
        nodeStack.pop();
    }

    @Override
    public void visitThrowStatement(ThrowStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitThrowStatement(node);
        nodeStack.pop();
    }

    @Override
    public void visitTryCatchFinally(TryCatchStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitTryCatchFinally(node);
        nodeStack.pop();
    }

    @Override
    public void visitWhileLoop(WhileStatement node) {
        nodeStack.push(factory.createFragment(node));
        super.visitWhileLoop(node);
        nodeStack.pop();
    }

    // expressions:

    @Override
    public void visitArrayExpression(ArrayExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitArrayExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitBinaryExpression(BinaryExpression node) {
        nodeStack.push(factory.createFragment(node));
        if (sloc.regionIsCoveredByNode(node)) {
            // create an extra stack frame to precisely describe the region being covered
            nodeStack.push(factory.createFragment(node, sloc.getOffset(), sloc.getEnd()));
        }
        super.visitBinaryExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitBitwiseNegationExpression(BitwiseNegationExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitBitwiseNegationExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitBooleanExpression(BooleanExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitBooleanExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitCastExpression(CastExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitCastExpression(node);
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
        nodeStack.pop();
    }

    @Override
    public void visitClosureListExpression(ClosureListExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitClosureListExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitConstantExpression(ConstantExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitConstantExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitConstructorCallExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitFieldExpression(FieldExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitFieldExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitGStringExpression(GStringExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitGStringExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitListExpression(ListExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitListExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitMapExpression(MapExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitMapExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitMapEntryExpression(MapEntryExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitMapEntryExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression node) {
        nodeStack.push(factory.createFragment(node));
        if (sloc.regionIsCoveredByNode(node)) {
            // create an extra stack frame to precisely describe the region being covered
            nodeStack.push(factory.createFragment(node, sloc.getOffset(), sloc.getEnd()));
        }
        super.visitMethodCallExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitMethodPointerExpression(MethodPointerExpression node) {
        nodeStack.push(factory.createFragment(node));
        if (sloc.regionIsCoveredByNode(node)) {
            // create an extra stack frame to precisely describe the region being covered
            nodeStack.push(factory.createFragment(node, sloc.getOffset(), sloc.getEnd()));
        }
        super.visitMethodPointerExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitPostfixExpression(PostfixExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitPostfixExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitPrefixExpression(PrefixExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitPrefixExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitPropertyExpression(PropertyExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitPropertyExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitRangeExpression(RangeExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitRangeExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitSpreadExpression(SpreadExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitSpreadExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitSpreadMapExpression(SpreadMapExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitSpreadMapExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitStaticMethodCallExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitTernaryExpression(TernaryExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitTernaryExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitTupleExpression(TupleExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitTupleExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitVariableExpression(VariableExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitVariableExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitUnaryMinusExpression(UnaryMinusExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitUnaryMinusExpression(node);
        nodeStack.pop();
    }

    @Override
    public void visitUnaryPlusExpression(UnaryPlusExpression node) {
        nodeStack.push(factory.createFragment(node));
        super.visitUnaryPlusExpression(node);
        nodeStack.pop();
    }
}
