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

package org.codehaus.groovy.eclipse.codeassist.requestor;

import static org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation.CLASS_BODY;
import static org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation.EXCEPTIONS;
import static org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation.EXPRESSION;
import static org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation.EXTENDS;
import static org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation.IMPLEMENTS;
import static org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation.IMPORT;
import static org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation.PARAMETER;
import static org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation.SCRIPT;
import static org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation.STATEMENT;

import java.util.Collections;
import java.util.Map;
import java.util.Stack;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.eclipse.core.util.VisitCompleteException;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;

/**
 * @author Andrew Eisenberg
 * @created Nov 9, 2009
 *
 * Find the completion node for the offset.  Also calculate the content assist context. 
 */
public class CompletionNodeFinder extends ClassCodeVisitorSupport {
    
    private Stack<ASTNode> blockStack;
    private AnnotatedNode currentDeclaration;
    
    private int completionOffset;
    private int supportingNodeEnd;
    private String completionExpression;
    private GroovyCompilationUnit unit;    
    private ContentAssistContext context;
    
    public CompletionNodeFinder(int completionOffset, int supportingNodeEnd,
            String completionExpression) {
        this.completionOffset = completionOffset;
        this.supportingNodeEnd = supportingNodeEnd;
        this.completionExpression = completionExpression;
        this.blockStack = new Stack<ASTNode>();
    }

    public ContentAssistContext findContentAssistContext(GroovyCompilationUnit unit) {
        try {
            this.unit = unit;
            internalVisitModuleNode(unit.getModuleNode());
        } catch (VisitCompleteException e) { }
        return context;
    }
    
    
    private void internalVisitModuleNode(ModuleNode module) {
        visitImports(module);
        // visit script last because sometimes its
        // source locations wrap around the other classes
        ClassNode script = null;
        for (ClassNode clazz : (Iterable<ClassNode>) module.getClasses()) {
            if (clazz.isScript()) {
                script = clazz;
            } else {
                visitClass(clazz);
            }
        }
        if (script != null) {
            visitClass(script);
        }
    }
    
    
    public void visitImports(ModuleNode node) {
        for (ImportNode importNode : (Iterable<ImportNode>) node.getImports()) {
            visitAnnotations(importNode);
            if (doTest(importNode.getType())) {
                currentDeclaration = importNode;
                createContext(null, importNode, IMPORT);
            }
        }
        // for 1.7 stream only
        for (ImportNode importStarNode : getImports(node, "getStarImports")) {
            visitAnnotations(importStarNode);
            if (doTest(importStarNode.getType())) {
                currentDeclaration = importStarNode;
                createContext(null, importStarNode, IMPORT);
            }
        }
        for (ImportNode importStaticNode : getImports(node, "getStaticImports")) {
            visitAnnotations(importStaticNode);
            if (doTest(importStaticNode.getType())) {
                currentDeclaration = importStaticNode;
                createContext(null, importStaticNode, IMPORT);
            }
        }
        for (ImportNode importStaticStarNode : getImports(node, "getStaticStarImports")) {
            visitAnnotations(importStaticStarNode);
            if (doTest(importStaticStarNode.getType())) {
                currentDeclaration = importStaticStarNode;
                createContext(null, importStaticStarNode, IMPORT);
            }
        }
    }


    /**
     * Use reflection because this is a 1.7 only method
     * @return
     */
    private Iterable<ImportNode> getImports(ModuleNode node, String methodName) {
        try {
            Object obj = ReflectionUtils.executePrivateMethod(ModuleNode.class, 
                    methodName, new Class<?>[0], node, new Object[0]);
            if (obj instanceof Iterable<?>) {
                return (Iterable<ImportNode>) obj;
            } else if (obj instanceof Map<?,?>) {
                return (Iterable<ImportNode>) ((Map<?,ImportNode>) obj).values();
            }
        } catch (Exception e) {
            // using 1.6.5
        }
        return Collections.emptyList();
    }

    @Override
    public void visitClass(ClassNode node) {
        if (!doTest(node) && !node.isScript()) {
            return;
        }
        currentDeclaration = node;
        visitAnnotations(node);
        ClassNode supr = node.getUnresolvedSuperClass();
        if (supr != null && doTest(supr)) {
            createContext(null, node, EXTENDS);
        }
        
        
        if (node.getInterfaces() != null) {
            for (ClassNode interf : node.getInterfaces()) {
                if (doTest(interf)) {
                    createContext(null, node, IMPLEMENTS);
                }
            }
        }
        node.visitContents(this);

        currentDeclaration = node;
        for (Statement element : (Iterable<Statement>) node.getObjectInitializerStatements()) {
            element.visit(this);
        }
        
        // if exception has not been thrown, we are inside this class body
        createContext(null, node, node.isScript() ? SCRIPT : CLASS_BODY);
    }
    
    @Override
    protected void visitConstructorOrMethod(MethodNode node,
            boolean isConstructor) {
        if (!doTest(node)) {
            return;
        }
        currentDeclaration = node;
        internalVisitParameters(node.getParameters(), node);
    
        if (node.getExceptions() != null) {
            for (ClassNode excep : node.getExceptions()) {
                if (doTest(excep)) {
                    createContext(null, node, EXCEPTIONS);
                }
            }
        }
        
        
        super.visitConstructorOrMethod(node, isConstructor);
        
        if (completionOffset < node.getCode().getStart()) {
            // probably inside an empty parameters list
            createContext(null, node, PARAMETER);
        }
        
        // if we get here, then it is because the block statement
        // has been swapped with a new one that has not had
        // its locaitons set properly
        createContext(node.getCode(), node.getCode(), expressionScriptOrStatement(node));
    }

    /**
     * @param node
     * @return
     */
    private ContentAssistLocation expressionScriptOrStatement(MethodNode node) {
        return node.getDeclaringClass().isScript() ? 
                expressionOrScript() : expressionOrStatement();
    }

    /**
     * @return
     */
    private ContentAssistLocation expressionOrScript() {
        return (supportingNodeEnd == -1 ? SCRIPT : EXPRESSION);
    }
    
    @Override
    public void visitBlockStatement(BlockStatement node) {
        blockStack.push(node);
        super.visitBlockStatement(node);
        
        if (doTest(node)) {
            // if we get here, then we know that we are in this block statement, 
            // but not inside any expression.  Use this to complete on
            createContext(blockStack.peek(), node, expressionOrStatement());
        }
        blockStack.pop();
    }
    
    // only visit the statements that may contain what we are looking for
    @Override
    protected void visitStatement(Statement statement) {
        if (doTest(statement)) {
            super.visitStatement(statement);
        }
    }
    
    @Override
    public void visitField(FieldNode node) {
        if (!doTest(node)) {
            return;
        }
        
        currentDeclaration = node;
        ClassNode type = node.getType();
        if (type != null && doTest(type)) {
            createContext(null, node.getDeclaringClass(), CLASS_BODY);
        }
        blockStack.push(node);
        super.visitField(node);
        blockStack.pop();
        createNullContext();
    }
    
    @Override
    public void visitProperty(PropertyNode node) {
        if (!doTest(node)) {
            return;
        }
        
        currentDeclaration = node;
        ClassNode type = node.getType();
        if (type != null && doTest(type)) {
            createContext(null, node.getDeclaringClass(), CLASS_BODY);
        }
        blockStack.push(node);
        super.visitProperty(node);
        blockStack.pop();
        createNullContext();
    }



    @Override
    public void visitVariableExpression(VariableExpression expression) {
        if (doTest(expression)) {
            createContext(expression, blockStack.peek(), expressionOrStatement());
        }
            
        super.visitVariableExpression(expression);
    }
    
    /**
     * @return
     */
    private ContentAssistLocation expressionOrStatement() {
        return supportingNodeEnd == -1 ? STATEMENT : EXPRESSION;
    }


    @Override
    public void visitFieldExpression(FieldExpression expression) {
        if (doTest(expression)) {
            createContext(expression, blockStack.peek(), expressionOrStatement());
        }
        super.visitFieldExpression(expression);
    }
    
    @Override
    public void visitClassExpression(ClassExpression expression) {
        if (doTest(expression)) {
            createContext(expression, blockStack.peek(), expressionOrStatement());
        }
        super.visitClassExpression(expression);
    }
    
    @Override
    public void visitConstantExpression(ConstantExpression expression) {
        if (doTest(expression)) {
            createContext(expression, blockStack.peek(), expressionOrStatement());
        }
        super.visitConstantExpression(expression);
    }
    
    @Override
    public void visitCastExpression(CastExpression node) {
        if (doTest(node.getType())) {
            createContext(node.getType(), blockStack.peek(), expressionOrStatement());
        }
        super.visitCastExpression(node);
    }
    
    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        ClassNode type = expression.getLeftExpression().getType();
        if (doTest(type)) {
            createContext(type, blockStack.peek(), expressionOrStatement());
        }
        super.visitDeclarationExpression(expression);
    }
    
    @Override
    public void visitConstructorCallExpression(
            ConstructorCallExpression call) {
        if (doTest(call.getType())) {
            createContext(call.getType(), blockStack.peek(), expressionOrStatement());
        }
        super.visitConstructorCallExpression(call);
        // might be a completion after the parens
        if (doTest(call)) {
            createContext(call, blockStack.peek(), expressionOrStatement());
        }
    }
    
    @Override
    public void visitCatchStatement(CatchStatement statement) {
        internalVisitParameter(statement.getVariable(), statement);
        super.visitCatchStatement(statement);
    }

    @Override
    public void visitForLoop(ForStatement forLoop) {
        internalVisitParameter(forLoop.getVariable(), forLoop);
        super.visitForLoop(forLoop);
    }

    public void visitArrayExpression(ArrayExpression expression) {
        if (doTest(expression)) {
            createContext(expression, blockStack.peek(), expressionOrStatement());
        }
        super.visitArrayExpression(expression);
    }


    @Override
    public void visitStaticMethodCallExpression(
            StaticMethodCallExpression call) {
        // don't check here if the type reference is implicit
        // we know that the type is not implicit if the name
        // location is filled in.
        if (call.getOwnerType().getNameEnd() == 0 && 
                doTest(call.getOwnerType())) {
            createContext(call.getOwnerType(), blockStack.peek(), expressionOrStatement());
        }
        // the method itself is not an expression, but only a string
        // so this check call will test for open declaration on the method
        if (doTest(call)) {
            createContext(call, blockStack.peek(), expressionOrStatement());
        }
        super.visitStaticMethodCallExpression(call);
    }

    
    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        internalVisitParameters(expression.getParameters(), blockStack.peek());
        super.visitClosureExpression(expression);
        
        // sometimes the code block does not end at the closing '}', but at the end of the last statement
        // the closure itself ends at the last '}'.  So, do that test here.
        if (doTest(expression)) {
            createContext(expression, expression.getCode(), expressionOrStatement());
        }
    }
    
    
    private void internalVisitParameters(Parameter[] ps, ASTNode declaringNode) {
        if (ps != null) {
            for (Parameter p : ps) {
                internalVisitParameter(p, declaringNode);
            }
        }
    }
    
    private void internalVisitParameter(Parameter p, ASTNode declaringNode) {
        if (doTest(p.getType())) {
            createContext(null, declaringNode, PARAMETER);
        }
        // This is for varargs
        ClassNode componentType = p.getType().getComponentType();
        if (componentType != null && doTest(componentType)) {
            createContext(null, declaringNode, PARAMETER);
        }
        Expression initialExpression = p.getInitialExpression();
        if (initialExpression != null && doTest(initialExpression)) {
            initialExpression.visit(this);
        }
        if (doTest(p)) {
            // mighe have an initial expression, but was moved out during part of the verification phase.
            // or might be a param without any type
            createContext(p, declaringNode, expressionOrStatement());
        }
    }
    
    private void createContext(ASTNode completionNode, ASTNode declaringNode, ContentAssistLocation location) {
        context = new ContentAssistContext(completionOffset, completionExpression, 
                completionNode, declaringNode, location, unit, currentDeclaration);
        throw new VisitCompleteException();
    }


    // called when completion is not possible
    private void createNullContext() {
        throw new VisitCompleteException();
    }
    
    protected boolean doTest(ASTNode node) {
        return ((supportingNodeEnd > node.getStart() && supportingNodeEnd <= node.getEnd()) ||
                (completionOffset >= node.getStart() && completionOffset <= node.getEnd()));
    }
    
    /**
     * will be null if no completions are available at the location
     * @return
     */
    public ContentAssistContext getContext() {
        return context;
    }


    @Override
    protected SourceUnit getSourceUnit() {
        return null;
    }
    
}
