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

package org.codehaus.groovy.eclipse.codebrowsing.requestor;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.eclipse.core.util.VisitCompleteException;
import org.eclipse.jface.text.IRegion;

public class ASTNodeFinder extends ClassCodeVisitorSupport {
    
    protected ASTNode nodeFound;
    private IRegion r;
    
    public ASTNodeFinder(IRegion r) {
        this.r = r;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return null;
    }
    
    
    @Override
    public void visitVariableExpression(VariableExpression expression) {
        check(expression);
        super.visitVariableExpression(expression);
    }
    
    @Override
    public void visitFieldExpression(FieldExpression expression) {
        check(expression);
        super.visitFieldExpression(expression);
    }
    
    @Override
    public void visitClassExpression(ClassExpression expression) {
        check(expression);
        super.visitClassExpression(expression);
    }
    
    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        checkParameters(expression.getParameters());
        super.visitClosureExpression(expression);
    }

    @Override
    protected void visitConstructorOrMethod(MethodNode node,
            boolean isConstructor) {
        ClassNode expression = node.getReturnType();
        if (expression != null) {
            check(expression);
        }
        if (node.getExceptions() != null) {
            for (ClassNode e : node.getExceptions()) {
                check(e);
            }
        }
        checkParameters(node.getParameters());
        super.visitConstructorOrMethod(node, isConstructor);
    }

    /**
     * @param node
     */
    private void checkParameters(Parameter[] params) {
        if (params != null) {
           for (Parameter p : params) {
               checkParameter(p);
           }
        }
    }

    /**
     * @param p
     */
    private void checkParameter(Parameter p) {
        if (p != null) {
            check(p.getType());
           if (p.getInitialExpression() != null) {
               p.getInitialExpression().visit(this);
           }
        }
    }
    
    @Override
    public void visitField(FieldNode node) {
        check(node.getType());
        super.visitField(node);
    }
    
    @Override
    public void visitCastExpression(CastExpression node) {
        check(node.getType());
        super.visitCastExpression(node);
    }
    
    @Override
    public void visitConstantExpression(ConstantExpression expression) {
        check(expression);
        super.visitConstantExpression(expression);
    }
    
    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        check(expression.getLeftExpression().getType());
        super.visitDeclarationExpression(expression);
    }
    
    @Override
    public void visitConstructorCallExpression(
            ConstructorCallExpression call) {
        check(call.getType());
        super.visitConstructorCallExpression(call);
    }
    
    @Override
    public void visitCatchStatement(CatchStatement statement) {
        checkParameter(statement.getVariable());
        super.visitCatchStatement(statement);
    }

    @Override
    public void visitForLoop(ForStatement forLoop) {
        checkParameter(forLoop.getVariable());
        super.visitForLoop(forLoop);
    }

    public void visitArrayExpression(ArrayExpression expression) {
        check(expression.getElementType());
        super.visitArrayExpression(expression);
    }


    @Override
    public void visitStaticMethodCallExpression(
            StaticMethodCallExpression call) {
        check(call.getOwnerType());
        // the method itself is not an expression, but only a string
        // so this check call will test for open declaration on the method
        check(call);
        super.visitStaticMethodCallExpression(call);
    }

    @Override
    public void visitClass(ClassNode node) {
        if (node.getUnresolvedSuperClass() != null) {
            check(node.getUnresolvedSuperClass());  // use unresolved to maintain source locations 
        }
        if (node.getInterfaces() != null) {
            for (ClassNode inter : node.getInterfaces()) {
                check(inter);
            }
        }
        if (node.getObjectInitializerStatements() != null) {
            for (Statement s : node.getObjectInitializerStatements()) {
                super.visitStatement(s);
            }
        }
        super.visitClass(node);
    }

    /**
     * @param node
     */
    protected void check(ASTNode node) {
        if (doTest(node)) {
            nodeFound = node;
            throw new VisitCompleteException();
        }
    }

    /**
     * @param node
     * @return
     */
    protected boolean doTest(ASTNode node) {
        return node.getStart() <= r.getOffset() && node.getEnd() >= r.getOffset()+r.getLength();
    }

    /**
     * @param module
     * @return
     */
    public ASTNode doVisit(ModuleNode module) {
        try {
            for (ClassNode clazz : (Iterable<ClassNode>) module.getClasses()) {
                this.visitClass(clazz);
            }
        } catch (VisitCompleteException e) {
        }
        return nodeFound;
    }
    
}