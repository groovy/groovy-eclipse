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
package org.codehaus.groovy.eclipse.debug.ui;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
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
import org.codehaus.groovy.ast.expr.Expression;
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
import org.codehaus.groovy.ast.expr.RegexExpression;
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
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.control.SourceUnit;

/**
 * @author Andrew Eisenberg
 * @created Aug 4, 2009
 *
 * Compute a valid location where to put a breakpoint from a ModuleNode.
 * The result is the first valid location with a line number greater or equals than the given position.
 * A valid location is considered to be the last expression or statement on a given line
 *
 */
public class ValidBreakpointLocationFinder extends ClassCodeVisitorSupport {

    private class VisitCompleted extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private class NodeNotFound extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private ASTNode lastValid = null;

    private int startLine;

    public ValidBreakpointLocationFinder(int startLine) {
        this.startLine = startLine;
    }

    private void validateNode(ASTNode node) throws VisitCompleted {

        if (node.getLineNumber() == -1 || node instanceof BlockStatement || node instanceof ClosureExpression) {
            // line number hasn't been set, but child line numbers might have
            // continue to search through children
            // also block statements seem to have end locations wrong
            // we can safely ignore them since breakpoints should not be set on block statements
            return;
        }

        if (node.getLineNumber() == startLine) {
            lastValid = node;
            // keep on searching until the line is over
        } else if (node.getLineNumber() > startLine) {
            if (lastValid == null) {
                lastValid = node;
            }
            throw new VisitCompleted();
        } else if (node.getLastLineNumber() < startLine && node instanceof AnnotatedNode && !(node instanceof Expression)) {
            // end the visit because we have gotten to the next declaration continuing on this path is a waste of time
            throw new NodeNotFound();
        }

    }

    public ASTNode findValidBreakpointLocation(ModuleNode module) {
        ASTNode candidate = null;
        for (ClassNode classNode : (Iterable<ClassNode>) module.getClasses()) {

            try {
                for (Statement initializer : (Iterable<Statement>) classNode.getObjectInitializerStatements()) {
                    this.visitStatement(initializer);
                }
            } catch (VisitCompleted vc) { }
            candidate = returnBetterCandiate(candidate, lastValid);
            lastValid = null;

            try {
                for (PropertyNode pn : (Iterable<PropertyNode>) classNode.getProperties()) {
                    this.visitProperty(pn);
                }
            } catch (VisitCompleted vc) { }
            candidate = returnBetterCandiate(candidate, lastValid);
            lastValid = null;

            try {
                for (FieldNode fn : classNode.getFields()) {
                    this.visitField(fn);
                }
            } catch (VisitCompleted vc) { }
            candidate = returnBetterCandiate(candidate, lastValid);
            lastValid = null;

            try {
                for (ConstructorNode cn : (Iterable<ConstructorNode>) classNode.getDeclaredConstructors()) {
                    this.visitConstructor(cn);
                }
            } catch (VisitCompleted vc) { }
            candidate = returnBetterCandiate(candidate, lastValid);
            lastValid = null;

            // if a class is a script, then it can possibly
            // have methods declared within the script
            // so, move the catch block inside the
            // loop to ensure all methods are explored
            if (classNode.isScript()) {
                for (MethodNode mn : classNode.getMethods()) {
                    try {
                        this.visitMethod(mn);
                    } catch (VisitCompleted vc) { }
                    candidate = returnBetterCandiate(candidate, lastValid);
                    lastValid = null;
                }
            } else {
                try {
                    for (MethodNode mn : classNode.getMethods()) {
                        this.visitMethod(mn);
                    }
                } catch (VisitCompleted vc) { }
                candidate = returnBetterCandiate(candidate, lastValid);
                lastValid = null;

                // check the <clinit> method to catch static initializers
                // and initializations of static methods
                MethodNode clinit = classNode.getMethod("<clinit>", new Parameter[0]);
                if (clinit != null) {
                    // visit the body only.
                    try {
                        clinit.getCode().visit(this);
                    } catch (VisitCompleted vc) { }
                    candidate = returnBetterCandiate(candidate, lastValid);
                    lastValid = null;
                }
            }
        }
        return candidate;
    }


    /**
     * compare two ASTNodes and determine which one is closer to the startLine
     *
     */
    private ASTNode returnBetterCandiate(ASTNode first, ASTNode second) {
        if (first != null && second != null) {
            int firstStartLine = first.getLineNumber();
            int secondStartLine = second.getLineNumber();

            int firstDiff = Math.abs(firstStartLine - startLine);
            int secondDiff = Math.abs(secondStartLine - startLine);
            return firstDiff < secondDiff ? first : second;
        } else if (first != null) {
            return first;
        } else {
            return second;
        }
    }

    /**
     * no breakpoints allowed here
     */
    @Override
    public void visitAnnotations(AnnotatedNode node) {
    }

    @Override
    protected void visitConstructorOrMethod(MethodNode node,
            boolean isConstructor) {
        try {

            // FIXADE must do a special ordering of statements if
            // in a <clinit> or <init>
            // See GRECLIPSE-888 (not implemented yet).
            super.visitConstructorOrMethod(node, isConstructor);
        } catch (NodeNotFound nnf) { }
    }

    @Override
    public void visitClass(ClassNode node) {
        try {
            super.visitClass(node);
        } catch (NodeNotFound nnf) { }
    }

    @Override
    public void visitField(FieldNode node) {
        try {
            super.visitField(node);
        } catch (NodeNotFound nnf) { }
    }

    @Override
    public void visitProperty(PropertyNode node) {
        try {
            super.visitProperty(node);
        } catch (NodeNotFound nnf) { }
    }

    @Override
    public void visitArgumentlistExpression(ArgumentListExpression ale) {
        validateNode(ale);
        super.visitArgumentlistExpression(ale);
    }

    @Override
    public void visitArrayExpression(ArrayExpression expression) {
        validateNode(expression);
        super.visitArrayExpression(expression);
    }

    @Override
    public void visitAssertStatement(AssertStatement statement) {
        validateNode(statement);
        super.visitAssertStatement(statement);
    }

    @Override
    public void visitAttributeExpression(AttributeExpression expression) {
        validateNode(expression);
        super.visitAttributeExpression(expression);
    }

    @Override
    public void visitBinaryExpression(BinaryExpression expression) {
        if (expression.getEnd() <= 0) {
            // avoid synthetic assignment statements,
            // specifically, initial value expressions that have been moved to
            // constructors, unless the RHS is a closure expression, in that
            // case,
            // visit the closure expression only.
            if (expression.getRightExpression() instanceof ClosureExpression) {
                expression.getRightExpression().visit(this);
            }
            return;
        }
        validateNode(expression);
        super.visitBinaryExpression(expression);
    }

    @Override
    public void visitBitwiseNegationExpression(
            BitwiseNegationExpression expression) {
        validateNode(expression);
        super.visitBitwiseNegationExpression(expression);
    }

    @Override
    public void visitBlockStatement(BlockStatement block) {
        validateNode(block);
        super.visitBlockStatement(block);
    }

    @Override
    public void visitBooleanExpression(BooleanExpression expression) {
        validateNode(expression);
        super.visitBooleanExpression(expression);
    }

    @Override
    public void visitBreakStatement(BreakStatement statement) {
        validateNode(statement);
        super.visitBreakStatement(statement);
    }

    @Override
    public void visitBytecodeExpression(BytecodeExpression cle) {
        validateNode(cle);
        super.visitBytecodeExpression(cle);
    }

    @Override
    public void visitCaseStatement(CaseStatement statement) {
        validateNode(statement);
        super.visitCaseStatement(statement);
    }

    @Override
    public void visitCastExpression(CastExpression expression) {
        validateNode(expression);
        super.visitCastExpression(expression);
    }

    @Override
    public void visitCatchStatement(CatchStatement statement) {
        validateNode(statement);
        super.visitCatchStatement(statement);
    }

    @Override
    public void visitClassExpression(ClassExpression expression) {
        validateNode(expression);
        super.visitClassExpression(expression);
    }

    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        validateNode(expression);
        super.visitClosureExpression(expression);
    }

    @Override
    public void visitClosureListExpression(ClosureListExpression cle) {
        validateNode(cle);
        super.visitClosureListExpression(cle);
    }

    @Override
    public void visitConstantExpression(ConstantExpression expression) {
        validateNode(expression);
        super.visitConstantExpression(expression);
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        validateNode(call);
        super.visitConstructorCallExpression(call);
    }

    @Override
    public void visitContinueStatement(ContinueStatement statement) {
        validateNode(statement);
        super.visitContinueStatement(statement);
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        validateNode(expression);
        super.visitDeclarationExpression(expression);
    }

    @Override
    public void visitDoWhileLoop(DoWhileStatement loop) {
        validateNode(loop);
        super.visitDoWhileLoop(loop);
    }

    @Override
    public void visitExpressionStatement(ExpressionStatement statement) {
        validateNode(statement);
        super.visitExpressionStatement(statement);
    }

    @Override
    public void visitFieldExpression(FieldExpression expression) {
        validateNode(expression);
        super.visitFieldExpression(expression);
    }

    @Override
    public void visitForLoop(ForStatement forLoop) {
        validateNode(forLoop);
        super.visitForLoop(forLoop);
    }

    @Override
    public void visitGStringExpression(GStringExpression expression) {
        validateNode(expression);
        super.visitGStringExpression(expression);
    }

    @Override
    public void visitIfElse(IfStatement ifElse) {
        validateNode(ifElse);
        super.visitIfElse(ifElse);
    }

    @Override
    public void visitListExpression(ListExpression expression) {
        validateNode(expression);
        super.visitListExpression(expression);
    }

    @Override
    public void visitMapEntryExpression(MapEntryExpression expression) {
        validateNode(expression);
        super.visitMapEntryExpression(expression);
    }

    @Override
    public void visitMapExpression(MapExpression expression) {
        validateNode(expression);
        super.visitMapExpression(expression);
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        validateNode(call);
        super.visitMethodCallExpression(call);
    }

    @Override
    public void visitMethodPointerExpression(MethodPointerExpression expression) {
        validateNode(expression);
        super.visitMethodPointerExpression(expression);
    }

    @Override
    public void visitNotExpression(NotExpression expression) {
        validateNode(expression);
        super.visitNotExpression(expression);
    }

    @Override
    public void visitPostfixExpression(PostfixExpression expression) {
        validateNode(expression);
        super.visitPostfixExpression(expression);
    }

    @Override
    public void visitPrefixExpression(PrefixExpression expression) {
        validateNode(expression);
        super.visitPrefixExpression(expression);
    }

    @Override
    public void visitPropertyExpression(PropertyExpression expression) {
        validateNode(expression);
        super.visitPropertyExpression(expression);
    }

    @Override
    public void visitRangeExpression(RangeExpression expression) {
        validateNode(expression);
        super.visitRangeExpression(expression);
    }

    @Override
    public void visitRegexExpression(RegexExpression expression) {
        validateNode(expression);
        super.visitRegexExpression(expression);
    }

    @Override
    public void visitReturnStatement(ReturnStatement statement) {
        validateNode(statement);
        super.visitReturnStatement(statement);
    }

    @Override
    public void visitShortTernaryExpression(ElvisOperatorExpression expression) {
        validateNode(expression);
        super.visitShortTernaryExpression(expression);
    }

    @Override
    public void visitSpreadExpression(SpreadExpression expression) {
        validateNode(expression);
        super.visitSpreadExpression(expression);
    }

    @Override
    public void visitSpreadMapExpression(SpreadMapExpression expression) {
        validateNode(expression);
        super.visitSpreadMapExpression(expression);
    }

    @Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
        validateNode(call);
        super.visitStaticMethodCallExpression(call);
    }

    @Override
    public void visitSwitch(SwitchStatement statement) {
        validateNode(statement);
        super.visitSwitch(statement);
    }

    @Override
    public void visitSynchronizedStatement(SynchronizedStatement statement) {
        validateNode(statement);
        super.visitSynchronizedStatement(statement);
    }

    @Override
    public void visitTernaryExpression(TernaryExpression expression) {
        validateNode(expression);
        super.visitTernaryExpression(expression);
    }

    @Override
    public void visitThrowStatement(ThrowStatement statement) {
        validateNode(statement);
        super.visitThrowStatement(statement);
    }

    @Override
    public void visitTryCatchFinally(TryCatchStatement statement) {
        validateNode(statement);
        super.visitTryCatchFinally(statement);
    }

    @Override
    public void visitTupleExpression(TupleExpression expression) {
        validateNode(expression);
        super.visitTupleExpression(expression);
    }

    @Override
    public void visitUnaryMinusExpression(UnaryMinusExpression expression) {
        validateNode(expression);
        super.visitUnaryMinusExpression(expression);
    }

    @Override
    public void visitUnaryPlusExpression(UnaryPlusExpression expression) {
        validateNode(expression);
        super.visitUnaryPlusExpression(expression);
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
        validateNode(expression);
        super.visitVariableExpression(expression);
    }

    @Override
    public void visitWhileLoop(WhileStatement loop) {
        validateNode(loop);
        super.visitWhileLoop(loop);
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return null;
    }

}
