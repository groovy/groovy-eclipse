/*
 * Copyright 2009-2017 the original author or authors.
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
package org.eclipse.jdt.groovy.core.util;

import static org.codehaus.groovy.ast.ClassCodeVisitorSupport.ORIGINAL_EXPRESSION;
import static org.eclipse.jdt.groovy.core.util.GroovyUtils.getAllImportNodes;
import static org.eclipse.jdt.groovy.core.util.GroovyUtils.getTraitFieldExpression;
import static org.eclipse.jdt.groovy.core.util.GroovyUtils.getTransformNodes;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GroovyClassVisitor;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PackageNode;
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
import org.codehaus.groovy.ast.expr.EmptyExpression;
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
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.runtime.GeneratedClosure;
import org.codehaus.groovy.transform.FieldASTTransformation;
import org.eclipse.core.runtime.Assert;

/**
 * Groovy AST visitor that performs a depth-first traversal.
 *
 * @see org.codehaus.groovy.ast.ClassCodeVisitorSupport
 * @see org.codehaus.groovy.ast.CodeVisitorSupport
 */
public abstract class DepthFirstVisitor implements GroovyClassVisitor, GroovyCodeVisitor {

    // TODO: Move into visitModule so overrides of visitMethod don't require "if (method == runMethod) return;".
    protected MethodNode runMethod;

    public void visitModule(ModuleNode node) {
        runMethod = null; // start fresh
        if (node.getPackage() != null) {
            visitPackage(node.getPackage());
        }
        for (ImportNode importNode : getAllImportNodes(node)) {
            visitImport(importNode);
        }
        for (ClassNode classNode : node.getClasses()) {
            // GRECLIPSE-781: visit script's run method last
            if (classNode.isScript()) {
                runMethod = classNode.getMethod("run", Parameter.EMPTY_ARRAY);
                Assert.isNotNull(runMethod);
            }
            visitClass(classNode);
        }
        if (runMethod != null) {
            // allow visit method to pass guard condition
            MethodNode run = runMethod; runMethod = null;
            visitMethod(run);
        }
    }

    public void visitPackage(PackageNode node) {
        visitAnnotations(node.getAnnotations());
    }

    public void visitImport(ImportNode node) {
        visitAnnotations(node.getAnnotations());
        if (node.getType() != null) {
            visitIfPresent(node.getFieldNameExpr());
            visitIfPresent(node.getAliasExpr());
        }
    }

    public void visitClass(ClassNode node) {
        visitAnnotations(node.getAnnotations());

        for (Statement stmt : node.getObjectInitializerStatements()) {
            stmt.visit(this);
        }

        node.visitContents(this);

        // visit trait members
        @SuppressWarnings("unchecked")
        List<FieldNode> traitFields = (List<FieldNode>) node.getNodeMetaData("trait.fields");
        if (traitFields != null) {
            for (FieldNode field : traitFields) {
                visitField(field);
            }
        }
        @SuppressWarnings("unchecked")
        List<MethodNode> traitMethods = (List<MethodNode>) node.getNodeMetaData("trait.methods");
        if (traitMethods != null) {
            for (MethodNode method : traitMethods) {
                visitMethod(method);
            }
        }

        // visit inner classes
        for (Iterator<InnerClassNode> it = node.getInnerClasses(); it.hasNext();) {
            InnerClassNode inner = it.next();
            // closures are represented as a class like Outer$_name_closure#, where # is a number
            if (!inner.isSynthetic() && !(inner instanceof GeneratedClosure)) {
                visitClass(inner);
            }
        }
    }

    public void visitProperty(PropertyNode node) {
        visitAnnotations(node.getAnnotations());
        visitIfPresent(node.getInitialExpression());
        visitIfPresent(node.getGetterBlock());
        visitIfPresent(node.getSetterBlock());
    }

    public void visitField(FieldNode node) {
        visitAnnotations(node.getAnnotations());
        // script field annotations are saved in script transforms map
        if (node.getEnd() > 0 && node.getDeclaringClass().isScript()) {
            for (ASTNode anno : getTransformNodes(node.getDeclaringClass(), FieldASTTransformation.class)) {
                if (anno.getStart() >= node.getStart() && anno.getEnd() < node.getEnd()) {
                    visitAnnotation((AnnotationNode) anno);
                }
            }
        }
        visitIfPresent(node.getInitialExpression());
    }

    public void visitConstructor(ConstructorNode node) {
        visitMethod(node);
    }

    public void visitMethod(MethodNode node) {
        if (node == runMethod) return;
        visitAnnotations(node.getAnnotations());
        visitParameters(node.getParameters());
        visitIfPresent(node.getCode());
    }

    // statements:

    public void visitAssertStatement(AssertStatement statement) {
        visitIfPresent(statement.getBooleanExpression());
        visitIfPresent(statement.getMessageExpression());
        visitStatement(statement);
    }

    public void visitBlockStatement(BlockStatement statement) {
        for (Statement stmt : statement.getStatements()) {
            visitIfPresent(stmt);
        }
        visitStatement(statement);
    }

    public void visitBreakStatement(BreakStatement statement) {
        visitStatement(statement);
    }

    public void visitCaseStatement(CaseStatement statement) {
        visitIfPresent(statement.getExpression());
        visitIfPresent(statement.getCode());
        visitStatement(statement);
    }

    public void visitCatchStatement(CatchStatement statement) {
        visitParameter(statement.getVariable());
        visitIfPresent(statement.getCode());
        visitStatement(statement);
    }

    public void visitContinueStatement(ContinueStatement statement) {
        visitStatement(statement);
    }

    public void visitDoWhileLoop(DoWhileStatement statement) {
        visitIfPresent(statement.getLoopBlock());
        visitIfPresent(statement.getBooleanExpression());
        visitStatement(statement);
    }

    public void visitEmptyStatement(EmptyStatement statement) {
        visitStatement(statement);
    }

    public void visitExpressionStatement(ExpressionStatement statement) {
        visitIfPresent(statement.getExpression());
        visitStatement(statement);
    }

    public void visitForLoop(ForStatement statement) {
        visitParameter(statement.getVariable());
        visitIfPresent(statement.getCollectionExpression());
        visitIfPresent(statement.getLoopBlock());
        visitStatement(statement);
    }

    public void visitIfElse(IfStatement statement) {
        visitIfPresent(statement.getBooleanExpression());
        visitIfPresent(statement.getIfBlock());
        if (statement.getElseBlock() instanceof EmptyStatement) {
            visitEmptyStatement((EmptyStatement) statement.getElseBlock());
        } else {
            visitIfPresent(statement.getElseBlock());
        }
        visitStatement(statement);
    }

    public void visitReturnStatement(ReturnStatement statement) {
        visitIfPresent(statement.getExpression());
        visitStatement(statement);
    }

    public void visitSwitch(SwitchStatement statement) {
        visitIfPresent(statement.getExpression());
        for (Statement stmt : statement.getCaseStatements()) {
            stmt.visit(this);
        }
        visitIfPresent(statement.getDefaultStatement());
        visitStatement(statement);
    }

    public void visitSynchronizedStatement(SynchronizedStatement statement) {
        visitIfPresent(statement.getExpression());
        visitIfPresent(statement.getCode());
        visitStatement(statement);
    }

    public void visitThrowStatement(ThrowStatement statement) {
        visitIfPresent(statement.getExpression());
        visitStatement(statement);
    }

    public void visitTryCatchFinally(TryCatchStatement statement) {
        visitIfPresent(statement.getTryStatement());
        for (Statement stmt : statement.getCatchStatements()) {
            stmt.visit(this);
        }
        if (statement.getFinallyStatement() instanceof EmptyStatement) {
            visitEmptyStatement((EmptyStatement) statement.getFinallyStatement());
        } else {
            visitIfPresent(statement.getFinallyStatement());
        }
        visitStatement(statement);
    }

    public void visitWhileLoop(WhileStatement statement) {
        statement.getBooleanExpression().visit(this);
        statement.getLoopBlock().visit(this);
        visitStatement(statement);
    }

    // expressions:

    public void visitArgumentlistExpression(ArgumentListExpression expression) {
        visitTupleExpression(expression);
    }

    public void visitArrayExpression(ArrayExpression expression) {
        visitAnnotations(expression.getAnnotations());
        visitExpressions(expression.getSizeExpression());
        visitExpressions(expression.getExpressions());
        visitExpression(expression);
    }

    public void visitAttributeExpression(AttributeExpression expression) {
        visitPropertyExpression(expression);
    }

    public void visitBinaryExpression(BinaryExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getLeftExpression().visit(this);
        expression.getRightExpression().visit(this);
        visitExpression(expression);
    }

    public void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getExpression().visit(this);
        visitExpression(expression);
    }

    public void visitBooleanExpression(BooleanExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getExpression().visit(this);
        visitExpression(expression);
    }

    public void visitBytecodeExpression(BytecodeExpression expression) {
        visitExpression(expression);
    }

    public void visitCastExpression(CastExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getExpression().visit(this);
        visitExpression(expression);
    }

    public void visitClassExpression(ClassExpression expression) {
        visitAnnotations(expression.getAnnotations());
        visitExpression(expression);
    }

    public void visitClosureExpression(ClosureExpression expression) {
        visitAnnotations(expression.getAnnotations());
        visitParameters(expression.getParameters());
        expression.getCode().visit(this);
        visitExpression(expression);
    }

    public void visitClosureListExpression(ClosureListExpression expression) {
        visitAnnotations(expression.getAnnotations());
        visitExpressions(expression.getExpressions());
        visitExpression(expression);
    }

    public void visitConstantExpression(ConstantExpression expression) {
        // check for an inlined constant (see ResolveVisitor.cloneConstantExpression)
        visitIfPresent((Expression) expression.getNodeMetaData(ORIGINAL_EXPRESSION));
        visitExpression(expression);
    }

    public void visitConstructorCallExpression(ConstructorCallExpression expression) {
        visitAnnotations(expression.getAnnotations());
        if (expression.isUsingAnonymousInnerClass()) {
            visitClass(expression.getType());
        }
        expression.getArguments().visit(this);
        visitExpression(expression);
    }

    public void visitDeclarationExpression(DeclarationExpression expression) {
        visitBinaryExpression(expression);
    }

    public void visitEmptyExpression(EmptyExpression expression) {
        visitExpression(expression);
    }

    public void visitFieldExpression(FieldExpression expression) {
        visitAnnotations(expression.getAnnotations());
        visitExpression(expression);
    }

    public void visitGStringExpression(GStringExpression expression) {
        visitAnnotations(expression.getAnnotations());
        visitExpressions(expression.getStrings());
        visitExpressions(expression.getValues());
        visitExpression(expression);
    }

    public void visitListExpression(ListExpression expression) {
        visitAnnotations(expression.getAnnotations());
        visitExpressions(expression.getExpressions());
        visitExpression(expression);
    }

    public void visitMapExpression(MapExpression expression) {
        visitAnnotations(expression.getAnnotations());
        visitExpressions(expression.getMapEntryExpressions());
        visitExpression(expression);
    }

    public void visitMapEntryExpression(MapEntryExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getKeyExpression().visit(this);
        expression.getValueExpression().visit(this);
        visitExpression(expression);
    }

    public void visitMethodCallExpression(MethodCallExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getObjectExpression().visit(this);
        expression.getMethod().visit(this);
        expression.getArguments().visit(this);

        // check for trait field re-written as call to helper method
        visitIfPresent(getTraitFieldExpression(expression));

        visitExpression(expression);
    }

    public void visitMethodPointerExpression(MethodPointerExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getExpression().visit(this);
        expression.getMethodName().visit(this);
        visitExpression(expression);
    }

    public void visitNotExpression(NotExpression expression) {
        visitBooleanExpression(expression);
    }

    public void visitPostfixExpression(PostfixExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getExpression().visit(this);
        visitExpression(expression);
    }

    public void visitPrefixExpression(PrefixExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getExpression().visit(this);
        visitExpression(expression);
    }

    public void visitPropertyExpression(PropertyExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getObjectExpression().visit(this);
        expression.getProperty().visit(this);
        visitExpression(expression);
    }

    public void visitRangeExpression(RangeExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getFrom().visit(this);
        expression.getTo().visit(this);
        visitExpression(expression);
    }

    public void visitShortTernaryExpression(ElvisOperatorExpression expression) {
        visitTernaryExpression(expression);
    }

    public void visitSpreadExpression(SpreadExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getExpression().visit(this);
        visitExpression(expression);
    }

    public void visitSpreadMapExpression(SpreadMapExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getExpression().visit(this);
        visitExpression(expression);
    }

    public void visitStaticMethodCallExpression(StaticMethodCallExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getArguments().visit(this);
        visitExpression(expression);
    }

    public void visitTernaryExpression(TernaryExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getBooleanExpression().visit(this);
        expression.getTrueExpression().visit(this);
        expression.getFalseExpression().visit(this);
        visitExpression(expression);
    }

    public void visitTupleExpression(TupleExpression expression) {
        visitAnnotations(expression.getAnnotations());
        visitExpressions(expression.getExpressions());
        visitExpression(expression);
    }

    public void visitVariableExpression(VariableExpression expression) {
        visitAnnotations(expression.getAnnotations());
        if (expression.getAccessedVariable() == expression) {
            visitIfPresent(expression.getInitialExpression());
        }
        visitExpression(expression);
    }

    public void visitUnaryMinusExpression(UnaryMinusExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getExpression().visit(this);
        visitExpression(expression);
    }

    public void visitUnaryPlusExpression(UnaryPlusExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getExpression().visit(this);
        visitExpression(expression);
    }

    //--------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    protected void visitAnnotations(Collection<? extends AnnotationNode> nodes) {
        if (isNotEmpty(nodes)) {
            for (AnnotationNode node : nodes) {
                if (node.isBuiltIn()) continue;

                visitAnnotations((Collection<? extends AnnotationNode>) node.getNodeMetaData("AnnotationCollector"));

                visitAnnotation(node);
            }
        }
    }

    protected void visitAnnotation(AnnotationNode node) {
        visitExpressions(node.getMembers().values());
    }

    protected void visitExpressions(Collection<? extends Expression> nodes) {
        if (isNotEmpty(nodes)) {
            for (Expression node : nodes) {
                visitIfPresent(node);
            }
        }
    }

    protected void visitExpression(Expression expression) {
    }

    protected void visitParameters(Parameter[] nodes) {
        if (isNotEmpty(nodes)) {
            for (Parameter node : nodes) {
                visitParameter(node);
            }
        }
    }

    protected void visitParameter(Parameter parameter) {
        if (parameter != null) {
            visitAnnotations(parameter.getAnnotations());
            visitIfPresent(parameter.getInitialExpression());
        }
    }

    protected void visitStatement(Statement statement) {
    }

    protected final void visitIfPresent(ASTNode node) {
        if (node != null) node.visit(this);
    }

    protected static boolean isNotEmpty(Object[] array) {
        return array != null && array.length > 0;
    }

    protected static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }
}
