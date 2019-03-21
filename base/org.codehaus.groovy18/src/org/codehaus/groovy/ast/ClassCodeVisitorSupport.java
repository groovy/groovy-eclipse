/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.ast;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
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
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.PreciseSyntaxException;

public abstract class ClassCodeVisitorSupport extends CodeVisitorSupport implements GroovyClassVisitor {

    public void visitClass(ClassNode node) {
        visitAnnotations(node);
        visitPackage(node.getPackage());
        visitImports(node.getModule());
        node.visitContents(this);
        visitObjectInitializerStatements(node);
    }
    
    protected void visitObjectInitializerStatements(ClassNode node) {
        for (Statement element : node.getObjectInitializerStatements()) {
            element.visit(this);
        }
    }

    public void visitPackage(PackageNode node) {
        if (node != null) {
            visitAnnotations(node);
            node.visit(this);
        }
    }

    public void visitImports(ModuleNode node) {
        if (node != null) {
            for (ImportNode importNode : node.getImports()) {
                visitAnnotations(importNode);
                importNode.visit(this);
            }
            for (ImportNode importStarNode : node.getStarImports()) {
                visitAnnotations(importStarNode);
                importStarNode.visit(this);
            }
            for (ImportNode importStaticNode : node.getStaticImports().values()) {
                visitAnnotations(importStaticNode);
                importStaticNode.visit(this);
            }
            for (ImportNode importStaticStarNode : node.getStaticStarImports().values()) {
                visitAnnotations(importStaticStarNode);
                importStaticStarNode.visit(this);
            }
        }
    }

    public void visitAnnotations(AnnotatedNode node) {
        List<AnnotationNode> annotations = node.getAnnotations();
        // GRECLIPSE edit
        if (!annotations.isEmpty())
            visitAnnotations(annotations);
        // GRECLIPSE end
    }

    // GRECLIPSE add
    protected void visitAnnotations(Iterable<AnnotationNode> nodes) {
        for (AnnotationNode node : nodes) {
            // skip built-in properties
            if (node.isBuiltIn()) continue;

            visitAnnotation(node);
        }
    }

    protected void visitAnnotation(AnnotationNode node) {
        for (Expression expr : node.getMembers().values()) {
            expr.visit(this);
        }
    }

    @Override
    public void visitConstantExpression(ConstantExpression expression) {
        // check for inlined constant (see ResolveVisitor.transformInlineConstants)
        Expression original = (Expression) expression.getNodeMetaData(ORIGINAL_EXPRESSION);
        if (original != null) {
            original.visit(this);
        }
    }

    /**
     * Returns the original source expression (in case of constant inlining) or
     * the input expression.
     *
     * @see org.codehaus.groovy.control.ResolveVisitor#transformInlineConstants
     * @see org.codehaus.groovy.control.ResolveVisitor#cloneConstantExpression
     */
    public static final Expression getNonInlinedExpression(Expression expression) {
        Expression original = (Expression) expression.getNodeMetaData(ORIGINAL_EXPRESSION);
        return original != null ? original : expression;
    }

    public static final String ORIGINAL_EXPRESSION = "OriginalExpression";
    // GRECLIPSE end

    protected void visitClassCodeContainer(Statement code) {
        if (code != null) code.visit(this);
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        visitAnnotations(expression);
        super.visitDeclarationExpression(expression);
    }

    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        visitAnnotations(node);
        visitClassCodeContainer(node.getCode());
        for (Parameter param : node.getParameters()) {
            visitAnnotations(param);
        }
    }

    public void visitConstructor(ConstructorNode node) {
        visitConstructorOrMethod(node, true);
    }

    public void visitMethod(MethodNode node) {
        visitConstructorOrMethod(node, false);
    }

    public void visitField(FieldNode node) {
        visitAnnotations(node);
        Expression init = node.getInitialExpression();
        if (init != null) init.visit(this);
    }

    public void visitProperty(PropertyNode node) {
        visitAnnotations(node);
        Statement statement = node.getGetterBlock();
        visitClassCodeContainer(statement);

        statement = node.getSetterBlock();
        visitClassCodeContainer(statement);

        Expression init = node.getInitialExpression();
        if (init != null) init.visit(this);
    }

    protected void addError(String msg, ASTNode expr) {
        int line = expr.getLineNumber();
        int col = expr.getColumnNumber();
        // GRECLIPSE
        int start = expr.getStart();
        int end = expr.getEnd()-1;
        if (expr instanceof ClassNode) {
        	// assume we have a class declaration
        	ClassNode cn = (ClassNode) expr;
        	if (cn.getNameEnd() > 0) {
        		start = cn.getNameStart();
        		end = cn.getNameEnd();
        	} else if (cn.getComponentType() != null) {
        		// avoid extra whitespace after closing ]
        		end --;
        	}
        	
        } else if (expr instanceof DeclarationExpression) {
        	// assume that we just want to underline the variable declaration
        	DeclarationExpression decl = (DeclarationExpression) expr;
        	Expression lhs = decl.getLeftExpression();
			start = lhs.getStart();
        	// avoid extra space before = if a variable
        	end = lhs instanceof VariableExpression ? start + lhs.getText().length() -1: lhs.getEnd() -1;
        }
        // end
        
        SourceUnit source = getSourceUnit();
        source.getErrorCollector().addErrorAndContinue(
                // GRECLIPSE: start
                new SyntaxErrorMessage(new PreciseSyntaxException(msg + '\n', line, col, start, end), source)
                // end
        );
    }

    // GRECLIPSE start
    protected void addTypeError(String msg, ClassNode expr ) {
        int line = expr.getLineNumber();
        int col = expr.getColumnNumber();
        SourceUnit source = getSourceUnit();
        source.getErrorCollector().addErrorAndContinue(
        		// GRECLIPSE: start
                new SyntaxErrorMessage(new PreciseSyntaxException(msg + '\n', line, col, expr.getNameStart(), expr.getNameEnd()), source)
                // end
        );
    }
    // end

    // GRECLIPSE edit
    //protected abstract SourceUnit getSourceUnit();
    protected SourceUnit getSourceUnit() {
        return null;
    }
    // GRECLIPSE end

    protected void visitStatement(Statement statement) {
    }

    public void visitAssertStatement(AssertStatement statement) {
        visitStatement(statement);
        super.visitAssertStatement(statement);
    }

    public void visitBlockStatement(BlockStatement block) {
        visitStatement(block);
        super.visitBlockStatement(block);
    }

    public void visitBreakStatement(BreakStatement statement) {
        visitStatement(statement);
        super.visitBreakStatement(statement);
    }

    public void visitCaseStatement(CaseStatement statement) {
        visitStatement(statement);
        super.visitCaseStatement(statement);
    }

    public void visitCatchStatement(CatchStatement statement) {
        visitStatement(statement);
        super.visitCatchStatement(statement);
    }

    public void visitContinueStatement(ContinueStatement statement) {
        visitStatement(statement);
        super.visitContinueStatement(statement);
    }

    public void visitDoWhileLoop(DoWhileStatement loop) {
        visitStatement(loop);
        super.visitDoWhileLoop(loop);
    }

    public void visitExpressionStatement(ExpressionStatement statement) {
        visitStatement(statement);
        super.visitExpressionStatement(statement);
    }

    public void visitForLoop(ForStatement forLoop) {
        visitStatement(forLoop);
        super.visitForLoop(forLoop);
    }

    public void visitIfElse(IfStatement ifElse) {
        visitStatement(ifElse);
        super.visitIfElse(ifElse);
    }

    public void visitReturnStatement(ReturnStatement statement) {
        visitStatement(statement);
        super.visitReturnStatement(statement);
    }

    public void visitSwitch(SwitchStatement statement) {
        visitStatement(statement);
        super.visitSwitch(statement);
    }

    public void visitSynchronizedStatement(SynchronizedStatement statement) {
        visitStatement(statement);
        super.visitSynchronizedStatement(statement);
    }

    public void visitThrowStatement(ThrowStatement statement) {
        visitStatement(statement);
        super.visitThrowStatement(statement);
    }

    public void visitTryCatchFinally(TryCatchStatement statement) {
        visitStatement(statement);
        super.visitTryCatchFinally(statement);
    }

    public void visitWhileLoop(WhileStatement loop) {
        visitStatement(loop);
        super.visitWhileLoop(loop);
    }
}
