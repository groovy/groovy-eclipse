/*
 * Copyright 2009-2021 the original author or authors.
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
package org.eclipse.jdt.groovy.core.util;

import static org.codehaus.groovy.ast.ClassCodeVisitorSupport.ORIGINAL_EXPRESSION;
import static org.eclipse.jdt.groovy.core.util.GroovyUtils.getAllImportNodes;
import static org.eclipse.jdt.groovy.core.util.GroovyUtils.getTraitFieldExpression;
import static org.eclipse.jdt.groovy.core.util.GroovyUtils.getTransformNodes;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.codehaus.groovy.ast.Variable;
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
import org.codehaus.groovy.ast.expr.ExpressionTransformer;
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
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.runtime.GeneratedClosure;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.transform.ASTTestTransformation;
import org.codehaus.groovy.transform.FieldASTTransformation;
import org.codehaus.groovy.transform.LazyASTTransformation;
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
            if (GroovyUtils.isAnonymous(classNode)) {
                continue; // visited under ctor call expr
            }
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

    @Override
    public void visitClass(ClassNode node) {
        visitAnnotations(node.getAnnotations());

        // visit "<clinit>" statements before visitContents
        MethodNode clinit = node.getMethod("<clinit>", Parameter.EMPTY_ARRAY);
        if (clinit != null && !node.isEnum()) {
            visitIfPresent(clinit.getCode());
        }
        for (Statement stmt : node.getObjectInitializerStatements()) {
            stmt.visit(this);
        }

        node.visitContents(this);

        // visit trait members
        List<FieldNode> traitFields = node.redirect().getNodeMetaData("trait.fields");
        if (traitFields != null) {
            for (FieldNode field : traitFields) {
                visitField(field);
            }
        }
        List<MethodNode> traitMethods = node.redirect().getNodeMetaData("trait.methods");
        if (traitMethods != null) {
            for (MethodNode method : traitMethods) {
                visitMethod(method);
            }
        }

        // visit (non-synthetic, non-anonymous) inner classes
        for (Iterator<InnerClassNode> it = node.getInnerClasses(); it.hasNext();) {
            InnerClassNode inner = it.next();
            // closures are represented as a class like Outer$_name_closure#, where # is a number
            if (!inner.isSynthetic() && !(inner instanceof GeneratedClosure) && !GroovyUtils.isAnonymous(inner)) {
                visitClass(inner);
            }
        }
    }

    @Override
    public void visitProperty(PropertyNode node) {
        visitAnnotations(node.getAnnotations());
        visitIfPresent(node.getGetterBlock());
        visitIfPresent(node.getSetterBlock());
    }

    @Override
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
        visitVariable(node);

        // visit lazy field initializer inline with field
        for (ASTNode anno : getTransformNodes(node.getDeclaringClass(), LazyASTTransformation.class)) {
            if (node.getAnnotations().contains(anno)) {
                MethodNode init = node.getDeclaringClass().getDeclaredMethod(
                    "get" + MetaClassHelper.capitalize(node.getName().substring(1)), Parameter.EMPTY_ARRAY);
                if (init != null && init.getEnd() < 1) {
                    visitMethod(init);
                }
                break;
            }
        }

        // visit enum field initializer inline with field
        if (node.isEnum() && node.isStatic() && !node.getName().matches("(MAX|MIN)_VALUE|\\$VALUES")) {
            MethodNode clinit = node.getDeclaringClass().getMethod("<clinit>", Parameter.EMPTY_ARRAY);
            for (Statement stmt : ((BlockStatement) clinit.getCode()).getStatements()) {
                if (stmt instanceof ExpressionStatement && ((ExpressionStatement) stmt).getExpression() instanceof BinaryExpression) {
                    Expression lhs = ((BinaryExpression) ((ExpressionStatement) stmt).getExpression()).getLeftExpression();
                    Expression rhs = ((BinaryExpression) ((ExpressionStatement) stmt).getExpression()).getRightExpression();
                    if (lhs instanceof FieldExpression && ((FieldExpression) lhs).getField() == node) {
                        rhs.visit(this);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void visitConstructor(ConstructorNode node) {
        visitMethod(node);
    }

    @Override
    public void visitMethod(MethodNode node) {
        if (node == runMethod || "<clinit>".equals(node.getName())) return;
        visitAnnotations(node.getAnnotations());
        visitParameters(node.getParameters());
        visitIfPresent(node.getCode());
    }

    // statements:

    @Override
    public void visitAssertStatement(AssertStatement statement) {
        visitIfPresent(statement.getBooleanExpression());
        visitIfPresent(statement.getMessageExpression());
        visitStatement(statement);
    }

    @Override
    public void visitBlockStatement(BlockStatement statement) {
        for (Statement stmt : statement.getStatements()) {
            stmt.visit(this);
        }
        visitStatement(statement);
    }

    @Override
    public void visitBreakStatement(BreakStatement statement) {
        visitStatement(statement);
    }

    @Override
    public void visitCaseStatement(CaseStatement statement) {
        visitIfPresent(statement.getExpression());
        visitIfPresent(statement.getCode());
        visitStatement(statement);
    }

    @Override
    public void visitCatchStatement(CatchStatement statement) {
        visitParameter(statement.getVariable());
        visitIfPresent(statement.getCode());
        visitStatement(statement);
    }

    @Override
    public void visitContinueStatement(ContinueStatement statement) {
        visitStatement(statement);
    }

    @Override
    public void visitDoWhileLoop(DoWhileStatement statement) {
        visitIfPresent(statement.getLoopBlock());
        visitIfPresent(statement.getBooleanExpression());
        visitStatement(statement);
    }

    public void visitEmptyStatement(EmptyStatement statement) {
        visitStatement(statement);
    }

    @Override
    public void visitExpressionStatement(ExpressionStatement statement) {
        statement.getExpression().visit(this);
        visitStatement(statement);
    }

    @Override
    public void visitForLoop(ForStatement statement) {
        visitParameter(statement.getVariable());
        visitIfPresent(statement.getCollectionExpression());
        visitIfPresent(statement.getLoopBlock());
        visitStatement(statement);
    }

    @Override
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

    @Override
    public void visitReturnStatement(ReturnStatement statement) {
        visitIfPresent(statement.getExpression());
        visitStatement(statement);
    }

    @Override
    public void visitSwitch(SwitchStatement statement) {
        visitIfPresent(statement.getExpression());
        for (Statement stmt : statement.getCaseStatements()) {
            stmt.visit(this);
        }
        visitIfPresent(statement.getDefaultStatement());
        visitStatement(statement);
    }

    @Override
    public void visitSynchronizedStatement(SynchronizedStatement statement) {
        visitIfPresent(statement.getExpression());
        visitIfPresent(statement.getCode());
        visitStatement(statement);
    }

    @Override
    public void visitThrowStatement(ThrowStatement statement) {
        visitIfPresent(statement.getExpression());
        visitStatement(statement);
    }

    @Override
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

    @Override
    public void visitWhileLoop(WhileStatement statement) {
        statement.getBooleanExpression().visit(this);
        statement.getLoopBlock().visit(this);
        visitStatement(statement);
    }

    // expressions:

    @Override
    public void visitArgumentlistExpression(ArgumentListExpression expression) {
        visitTupleExpression(expression);
    }

    @Override
    public void visitArrayExpression(ArrayExpression expression) {
        visitAnnotations(expression.getAnnotations());
        visitExpressions(expression.getSizeExpression());
        visitExpressions(expression.getExpressions());
        visitExpression(expression);
    }

    @Override
    public void visitAttributeExpression(AttributeExpression expression) {
        visitPropertyExpression(expression);
    }

    @Override
    public void visitBinaryExpression(BinaryExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getLeftExpression().visit(this);
        expression.getRightExpression().visit(this);
        visitExpression(expression);
    }

    @Override
    public void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getExpression().visit(this);
        visitExpression(expression);
    }

    @Override
    public void visitBooleanExpression(BooleanExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getExpression().visit(this);
        visitExpression(expression);
    }

    @Override
    public void visitBytecodeExpression(BytecodeExpression expression) {
        visitExpression(expression);
    }

    @Override
    public void visitCastExpression(CastExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getExpression().visit(this);
        visitExpression(expression);
    }

    @Override
    public void visitClassExpression(ClassExpression expression) {
        visitAnnotations(expression.getAnnotations());
        visitExpression(expression);
    }

    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        visitAnnotations(expression.getAnnotations());
        visitParameters(expression.getParameters());
        expression.getCode().visit(this);
        visitExpression(expression);
    }

    @Override
    public void visitClosureListExpression(ClosureListExpression expression) {
        visitAnnotations(expression.getAnnotations());
        visitExpressions(expression.getExpressions());
        visitExpression(expression);
    }

    @Override
    public void visitConstantExpression(ConstantExpression expression) {
        visitExpression(expression);
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression expression) {
        visitAnnotations(expression.getAnnotations());
        if (expression.isUsingAnonymousInnerClass()) {
            visitClass(expression.getType());
        }
        expression.getArguments().visit(this);
        visitExpression(expression);
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        visitBinaryExpression(expression);
    }

    @Override
    public void visitEmptyExpression(EmptyExpression expression) {
        visitExpression(expression);
    }

    @Override
    public void visitFieldExpression(FieldExpression expression) {
        visitAnnotations(expression.getAnnotations());
        visitExpression(expression);
    }

    @Override
    public void visitGStringExpression(GStringExpression expression) {
        visitAnnotations(expression.getAnnotations());
        visitExpressions(expression.getStrings());
        visitExpressions(expression.getValues());
        visitExpression(expression);
    }

    @Override
    public void visitListExpression(ListExpression expression) {
        visitAnnotations(expression.getAnnotations());
        visitExpressions(expression.getExpressions());
        visitExpression(expression);
    }

    @Override
    public void visitMapExpression(MapExpression expression) {
        visitAnnotations(expression.getAnnotations());
        visitExpressions(expression.getMapEntryExpressions());
        visitExpression(expression);
    }

    @Override
    public void visitMapEntryExpression(MapEntryExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getKeyExpression().visit(this);
        expression.getValueExpression().visit(this);
        visitExpression(expression);
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getObjectExpression().visit(this);
        expression.getMethod().visit(this);
        expression.getArguments().visit(this);

        // check for trait field re-written as call to helper method
        visitIfPresent(getTraitFieldExpression(expression));

        // check for enum init body
        ClassNode type = expression.getType().redirect();
        if (type.isEnum() && GroovyUtils.isAnonymous(type) &&
                expression.getMethodAsString().equals("$INIT")) {
            visitClass(type); // visit enum constant methods
        }

        visitExpression(expression);
    }

    @Override
    public void visitMethodPointerExpression(MethodPointerExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getExpression().visit(this);
        expression.getMethodName().visit(this);
        visitExpression(expression);
    }

    @Override
    public void visitNotExpression(NotExpression expression) {
        visitBooleanExpression(expression);
    }

    @Override
    public void visitPostfixExpression(PostfixExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getExpression().visit(this);
        visitExpression(expression);
    }

    @Override
    public void visitPrefixExpression(PrefixExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getExpression().visit(this);
        visitExpression(expression);
    }

    @Override
    public void visitPropertyExpression(PropertyExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getObjectExpression().visit(this);
        expression.getProperty().visit(this);
        visitExpression(expression);
    }

    @Override
    public void visitRangeExpression(RangeExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getFrom().visit(this);
        expression.getTo().visit(this);
        visitExpression(expression);
    }

    @Override
    public void visitShortTernaryExpression(ElvisOperatorExpression expression) {
        visitTernaryExpression(expression);
    }

    @Override
    public void visitSpreadExpression(SpreadExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getExpression().visit(this);
        visitExpression(expression);
    }

    @Override
    public void visitSpreadMapExpression(SpreadMapExpression expression) {
        visitAnnotations(expression.getAnnotations());
        visitExpression(expression);
    }

    @Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getArguments().visit(this);

        // check for enum init body
        ClassNode type = expression.getOwnerType();
        if (type.isEnum() && GroovyUtils.isAnonymous(type) &&
                expression.getMethodAsString().equals("$INIT")) {
            visitClass(type); // visit enum constant methods
        }

        visitExpression(expression);
    }

    @Override
    public void visitTernaryExpression(TernaryExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getBooleanExpression().visit(this);
        expression.getTrueExpression().visit(this);
        expression.getFalseExpression().visit(this);
        visitExpression(expression);
    }

    @Override
    public void visitTupleExpression(TupleExpression expression) {
        visitAnnotations(expression.getAnnotations());
        visitExpressions(expression.getExpressions());
        visitExpression(expression);
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
        visitAnnotations(expression.getAnnotations());
        if (expression.getAccessedVariable() == expression) {
            visitVariable(expression);
        }
        visitExpression(expression);
    }

    @Override
    public void visitUnaryMinusExpression(UnaryMinusExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getExpression().visit(this);
        visitExpression(expression);
    }

    @Override
    public void visitUnaryPlusExpression(UnaryPlusExpression expression) {
        visitAnnotations(expression.getAnnotations());
        expression.getExpression().visit(this);
        visitExpression(expression);
    }

    //--------------------------------------------------------------------------

    protected void visitAnnotations(Collection<? extends AnnotationNode> nodes) {
        if (isNotEmpty(nodes)) {
            for (AnnotationNode node : nodes) {
                if (node.isBuiltIn()) continue;

                visitAnnotations(node.getNodeMetaData("AnnotationCollector"));

                visitAnnotation(node);
            }
        }
    }

    protected void visitAnnotation(AnnotationNode node) {
        visitIfPresent(node.getNodeMetaData(ASTTestTransformation.class));
        for (Map.Entry<String, Expression> pair : node.getMembers().entrySet()) {
            // provide some context for the visitation of the initial value expression
            visitVariable(new MemberValueExpression(pair.getKey(), pair.getValue(), node));
        }
    }

    protected void visitExpressions(Collection<? extends Expression> nodes) {
        if (isNotEmpty(nodes)) {
            for (Expression node : nodes) {
                node.visit(this);
            }
        }
    }

    protected void visitExpression(Expression expression) {
        // check for an inlined constant (see ResolveVisitor.cloneConstantExpression)
        // or super ref (see MethodCallExpressionTransformer.transformToMopSuperCall)
        visitIfPresent(expression.getNodeMetaData(ORIGINAL_EXPRESSION));
    }

    protected void visitParameters(Parameter[] nodes) {
        if (isNotEmpty(nodes)) {
            for (Parameter node : nodes) {
                visitParameter(node);
            }
        }
    }

    protected void visitParameter(Parameter parameter) {
        visitAnnotations(parameter.getAnnotations());
        visitVariable(parameter);
    }

    protected void visitStatement(Statement statement) {
    }

    protected void visitVariable(Variable variable) {
        visitIfPresent(variable.getInitialExpression());
        if (variable instanceof Parameter) { // special case; see Verifier.addDefaultParameters
            visitIfPresent(((Parameter) variable).getNodeMetaData(Verifier.INITIAL_EXPRESSION));
        }
    }

    protected final void visitIfPresent(ASTNode node) {
        if (node != null) node.visit(this);
    }

    protected static boolean isNotEmpty(Object[] array) {
        return (array != null && array.length > 0);
    }

    protected static boolean isNotEmpty(Collection<?> collection) {
        return (collection != null && !collection.isEmpty());
    }

    //--------------------------------------------------------------------------

    protected static class MemberValueExpression extends Expression implements Variable {

        private String name;
        private Expression value;
        private AnnotationNode parent;

        public MemberValueExpression(String name, Expression value, AnnotationNode parent) {
            this.name = name;
            this.value = value;
            this.parent = parent;
        }

        @Override
        public Expression getInitialExpression() {
            return value;
        }

        @Override
        public boolean hasInitialExpression() {
            return (value != null);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getModifiers() {
            return 0;
        }

        @Override
        public ClassNode getType() {
            MethodNode member = parent.getClassNode().getMethod(getName(), Parameter.EMPTY_ARRAY);
            return (member != null ? member.getReturnType() : null);
        }

        @Override
        public ClassNode getOriginType() {
            return getType();
        }

        @Override
        public boolean isDynamicTyped() {
            return false;
        }

        @Override
        public boolean isInStaticContext() {
            return true;
        }

        @Override
        public boolean isClosureSharedVariable() {
            return false;
        }

        @Override
        public void setClosureSharedVariable(boolean value) {
        }

        @Override
        public Expression transformExpression(ExpressionTransformer xformer) {
            return null;
        }
    }
}
