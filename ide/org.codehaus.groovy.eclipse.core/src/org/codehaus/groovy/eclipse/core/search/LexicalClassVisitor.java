/*
 * Copyright 2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.search;

import java.util.PriorityQueue;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PackageNode;
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
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.control.SourceUnit;

/**
 * Iterates through {@link ModuleNode} members in lexical order.
 *
 * The order is not computed lazily, rather, when the first node is asked for,
 * the entire module node is walked.
 *
 * {@link PackageNode}s and {@link ImportNode}s are ignored.
 *
 * @author andrew
 * @created Jan 21, 2011
 */
public class LexicalClassVisitor {

    private class LexicalPrevisitor extends ClassCodeVisitorSupport {

        @Override
        public void visitClass(ClassNode node) {
            maybeAddNode(node);
            visitObjectInitializerStatements(node);
            node.visitContents(this);
        }

        private void maybeAddNode(ASTNode node) {
            if (node.getEnd() > 0) {
                nodeList.add(new ComparableNode(node));
            }
        }

        @Override
        protected void visitObjectInitializerStatements(ClassNode node) {
            maybeAddNode(node);
            super.visitObjectInitializerStatements(node);
        }

        @Override
        public void visitVariableExpression(VariableExpression node) {
            maybeAddNode(node);
            super.visitVariableExpression(node);
        }

        @Override
        public void visitConstructor(ConstructorNode node) {
            maybeAddNode(node);
            super.visitConstructor(node);
        }

        @Override
        public void visitMethod(MethodNode node) {
            maybeAddNode(node);
            super.visitMethod(node);
        }

        @Override
        public void visitField(FieldNode node) {
            super.visitField(node);
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }

        @Override
        public void visitAssertStatement(AssertStatement node) {
            maybeAddNode(node);
            super.visitAssertStatement(node);
        }

        @Override
        public void visitBlockStatement(BlockStatement node) {
            maybeAddNode(node);
            super.visitBlockStatement(node);
        }

        @Override
        public void visitBreakStatement(BreakStatement node) {
            maybeAddNode(node);
            super.visitBreakStatement(node);
        }

        @Override
        public void visitCaseStatement(CaseStatement node) {
            maybeAddNode(node);
            super.visitCaseStatement(node);
        }

        @Override
        public void visitCatchStatement(CatchStatement node) {
            maybeAddNode(node);
            super.visitCatchStatement(node);
        }

        @Override
        public void visitContinueStatement(ContinueStatement node) {
            maybeAddNode(node);
            super.visitContinueStatement(node);
        }

        @Override
        public void visitDoWhileLoop(DoWhileStatement node) {
            maybeAddNode(node);
            super.visitDoWhileLoop(node);
        }

        @Override
        public void visitExpressionStatement(ExpressionStatement node) {
            maybeAddNode(node);
            super.visitExpressionStatement(node);
        }

        @Override
        public void visitForLoop(ForStatement node) {
            maybeAddNode(node);
            super.visitForLoop(node);
        }

        @Override
        public void visitIfElse(IfStatement node) {
            maybeAddNode(node);
            super.visitIfElse(node);
        }

        @Override
        public void visitReturnStatement(ReturnStatement node) {
            maybeAddNode(node);
            super.visitReturnStatement(node);
        }

        @Override
        public void visitSwitch(SwitchStatement node) {
            maybeAddNode(node);
            super.visitSwitch(node);
        }

        @Override
        public void visitSynchronizedStatement(SynchronizedStatement node) {
            maybeAddNode(node);
            super.visitSynchronizedStatement(node);
        }

        @Override
        public void visitThrowStatement(ThrowStatement node) {
            maybeAddNode(node);
            super.visitThrowStatement(node);
        }

        @Override
        public void visitTryCatchFinally(TryCatchStatement node) {
            maybeAddNode(node);
            super.visitTryCatchFinally(node);
        }

        @Override
        public void visitWhileLoop(WhileStatement node) {
            maybeAddNode(node);
            super.visitWhileLoop(node);
        }

        @Override
        protected void visitEmptyStatement(EmptyStatement node) {
            maybeAddNode(node);
            super.visitEmptyStatement(node);
        }

        @Override
        public void visitMethodCallExpression(MethodCallExpression node) {
            maybeAddNode(node);
            super.visitMethodCallExpression(node);
        }

        @Override
        public void visitStaticMethodCallExpression(StaticMethodCallExpression node) {
            maybeAddNode(node);
            super.visitStaticMethodCallExpression(node);
        }

        @Override
        public void visitConstructorCallExpression(ConstructorCallExpression node) {
            maybeAddNode(node);
            super.visitConstructorCallExpression(node);
        }

        @Override
        public void visitBinaryExpression(BinaryExpression node) {
            maybeAddNode(node);
            super.visitBinaryExpression(node);
        }

        @Override
        public void visitTernaryExpression(TernaryExpression node) {
            maybeAddNode(node);
            super.visitTernaryExpression(node);
        }

        @Override
        public void visitShortTernaryExpression(ElvisOperatorExpression node) {
            maybeAddNode(node);
            super.visitShortTernaryExpression(node);
        }

        @Override
        public void visitPostfixExpression(PostfixExpression node) {
            maybeAddNode(node);
            super.visitPostfixExpression(node);
        }

        @Override
        public void visitPrefixExpression(PrefixExpression node) {
            maybeAddNode(node);
            super.visitPrefixExpression(node);
        }

        @Override
        public void visitBooleanExpression(BooleanExpression node) {
            maybeAddNode(node);
            super.visitBooleanExpression(node);
        }

        @Override
        public void visitNotExpression(NotExpression node) {
            maybeAddNode(node);
            super.visitNotExpression(node);
        }

        @Override
        public void visitClosureExpression(ClosureExpression node) {
            maybeAddNode(node);
            super.visitClosureExpression(node);
        }

        @Override
        public void visitTupleExpression(TupleExpression node) {
            maybeAddNode(node);
            super.visitTupleExpression(node);
        }

        @Override
        public void visitListExpression(ListExpression node) {
            maybeAddNode(node);
            super.visitListExpression(node);
        }

        @Override
        public void visitArrayExpression(ArrayExpression node) {
            maybeAddNode(node);
            super.visitArrayExpression(node);
        }

        @Override
        public void visitMapExpression(MapExpression node) {
            maybeAddNode(node);
            super.visitMapExpression(node);
        }

        @Override
        public void visitMapEntryExpression(MapEntryExpression node) {
            maybeAddNode(node);
            super.visitMapEntryExpression(node);
        }

        @Override
        public void visitRangeExpression(RangeExpression node) {
            maybeAddNode(node);
            super.visitRangeExpression(node);
        }

        @Override
        public void visitSpreadExpression(SpreadExpression node) {
            maybeAddNode(node);
            super.visitSpreadExpression(node);
        }

        @Override
        public void visitSpreadMapExpression(SpreadMapExpression node) {
            maybeAddNode(node);
            super.visitSpreadMapExpression(node);
        }

        @Override
        public void visitMethodPointerExpression(MethodPointerExpression node) {
            maybeAddNode(node);
            super.visitMethodPointerExpression(node);
        }

        @Override
        public void visitUnaryMinusExpression(UnaryMinusExpression node) {
            maybeAddNode(node);
            super.visitUnaryMinusExpression(node);
        }

        @Override
        public void visitUnaryPlusExpression(UnaryPlusExpression node) {
            maybeAddNode(node);
            super.visitUnaryPlusExpression(node);
        }

        @Override
        public void visitBitwiseNegationExpression(BitwiseNegationExpression node) {
            maybeAddNode(node);
            super.visitBitwiseNegationExpression(node);
        }

        @Override
        public void visitCastExpression(CastExpression node) {
            maybeAddNode(node);
            super.visitCastExpression(node);
        }

        @Override
        public void visitConstantExpression(ConstantExpression node) {
            maybeAddNode(node);
            super.visitConstantExpression(node);
        }

        @Override
        public void visitClassExpression(ClassExpression node) {
            maybeAddNode(node);
            super.visitClassExpression(node);
        }

        @Override
        public void visitDeclarationExpression(DeclarationExpression node) {
            maybeAddNode(node);
            super.visitDeclarationExpression(node);
        }

        @Override
        public void visitPropertyExpression(PropertyExpression node) {
            maybeAddNode(node);
            super.visitPropertyExpression(node);
        }

        @Override
        public void visitAttributeExpression(AttributeExpression node) {
            maybeAddNode(node);
            super.visitAttributeExpression(node);
        }

        @Override
        public void visitFieldExpression(FieldExpression node) {
            maybeAddNode(node);
            super.visitFieldExpression(node);
        }

        @Override
        public void visitGStringExpression(GStringExpression node) {
            maybeAddNode(node);
            super.visitGStringExpression(node);
        }

        @Override
        public void visitArgumentlistExpression(ArgumentListExpression node) {
            maybeAddNode(node);
            super.visitArgumentlistExpression(node);
        }

        @Override
        public void visitClosureListExpression(ClosureListExpression node) {
            maybeAddNode(node);
            super.visitClosureListExpression(node);
        }

        @Override
        public void visitBytecodeExpression(BytecodeExpression node) {
            maybeAddNode(node);
            super.visitBytecodeExpression(node);
        }

        /**
         * @param module
         */
        void doVisit(ModuleNode module) {
            for (ClassNode node : (Iterable<ClassNode>) module.getClasses()) {
                visitClass(node);
            }
        }

    }

    private class ComparableNode implements Comparable<ComparableNode> {

        final ASTNode thisNode;

        ComparableNode(ASTNode thisNode) {
            this.thisNode = thisNode;
        }

        public int compareTo(ComparableNode o) {
            if (thisNode.getStart() != o.thisNode.getStart()) {
                return thisNode.getStart() - o.thisNode.getStart();
            } else {
                // if two nodes start at the same position, but end at different
                // ones, assume that the one that ends later should be visited
                // first
                return o.thisNode.getEnd() - thisNode.getEnd();
            }
        }

    }

    private final ModuleNode module;

    private PriorityQueue<ComparableNode> nodeList;

    public LexicalClassVisitor(ModuleNode module) {
        super();
        this.module = module;
    }

    /**
     * @return the next lexical ASTNode to visit, or null if the visit is
     *         complete
     */
    public ASTNode getNextNode() {
        if (!hasNextNode()) {
            return null;
        }
        return nodeList.remove().thisNode;
    }

    private void initialize() {
        nodeList = new PriorityQueue<ComparableNode>();
        LexicalPrevisitor visitor = new LexicalPrevisitor();
        visitor.doVisit(module);
    }

    public boolean hasNextNode() {
        if (nodeList == null) {
            initialize();
        }
        return !nodeList.isEmpty();
    }

    public void reset() {
        nodeList = null;
    }
}