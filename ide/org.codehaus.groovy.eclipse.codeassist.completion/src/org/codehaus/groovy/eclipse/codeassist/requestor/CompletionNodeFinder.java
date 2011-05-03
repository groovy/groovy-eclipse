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

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ImportNodeCompatibilityWrapper;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.eclipse.core.util.VisitCompleteException;
import org.codehaus.groovy.runtime.GeneratedClosure;
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

    private int completionEnd;
    private int supportingNodeEnd;
    private String completionExpression;
    private String fullCompletionExpression;

    private GroovyCompilationUnit unit;
    private ContentAssistContext context;

    /**
     * Left hand side of any assignment statement or null if there is none
     */
    private Expression lhsNode;

    public CompletionNodeFinder(int completionOffset, int completionEnd,
            int supportingNodeEnd,
            String completionExpression, String fullCompletionExpression) {
        this.completionOffset = completionOffset;
        this.completionEnd = completionEnd;
        this.supportingNodeEnd = supportingNodeEnd;
        this.completionExpression = completionExpression;
        this.fullCompletionExpression = fullCompletionExpression;
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


// @Override
    @Override
    public void visitImports(ModuleNode node) {
        ImportNodeCompatibilityWrapper wrapper = new ImportNodeCompatibilityWrapper(node);
        for (ImportNode importNode : wrapper.getAllImportNodes()) {
            visitAnnotations(importNode);
            if (importNode.getType() != null && doTest(importNode.getType())) {
                currentDeclaration = importNode;
                createContext(null, importNode, IMPORT);
            }
        }
    }


    @Override
    public void visitClass(ClassNode node) {
        if (!doTest(node) && !node.isScript()) {
            return;
        }
        currentDeclaration = node;
        blockStack.push(node);
        visitAnnotations(node);
        blockStack.pop();
        ClassNode supr = node.getUnresolvedSuperClass();
        if (supr != null && doTest(supr)) {
            createContext(null, node, EXTENDS);
        }

        // visit inner classes
        // getInnerClasses() does not exist in the 1.6 stream, so must access reflectively
        Iterator<ClassNode> innerClasses;
        try {
            innerClasses = (Iterator<ClassNode>)
                    ReflectionUtils.throwableExecutePrivateMethod(ClassNode.class, "getInnerClasses", new Class<?>[0], node, new Object[0]);
        } catch (Exception e) {
            // can ignore.
            innerClasses = null;
        }
        if (innerClasses != null) {
            while (innerClasses.hasNext()) {
                ClassNode inner = innerClasses.next();
                if (!inner.isSynthetic() || inner instanceof GeneratedClosure) {
                    this.visitClass(inner);
                }
            }
        }



        if (node.getInterfaces() != null) {
            for (ClassNode interf : node.getInterfaces()) {
                if (doTest(interf)) {
                    createContext(null, node, IMPLEMENTS);
                }
            }
        }
        node.visitContents(this);

        // visit <clinit> body because this is where static field initializers are placed
        MethodNode clinit = node.getMethod("<clinit>", new Parameter[0]);
        if (clinit != null && clinit.getCode() instanceof BlockStatement) {
            blockStack.push(clinit.getCode());
            for (Statement element : (Iterable<Statement>) ((BlockStatement) clinit.getCode()).getStatements()) {
                element.visit(this);
            }
            blockStack.pop();
        }

        // visit default constructors that have been added by the verifier.  This is
        // where initializers lie
        ConstructorNode init = findDefaultConstructor(node);
        if (init != null) {
            blockStack.push(init.getCode());
            for (Statement element : (Iterable<Statement>) ((BlockStatement) init.getCode()).getStatements()) {
                element.visit(this);
            }
            blockStack.pop();
        }



        currentDeclaration = node;
        for (Statement element : (Iterable<Statement>) node.getObjectInitializerStatements()) {
            element.visit(this);
        }

        // do the run method last since it can wrap around other methods
        if (node.isScript()) {
            MethodNode run = node.getMethod("run", new Parameter[0]);
            if (run != null) {
                internalVisitConstructorOrMethod(run);
            }
        }

        // if exception has not been thrown, we are inside this class body
        createContext(null, node, node.isScript() ? SCRIPT : CLASS_BODY);
    }

    @Override
    protected void visitConstructorOrMethod(MethodNode node,
            boolean isConstructor) {
        // run method must be visited last
        if (!isRunMethod(node)) {
            internalVisitConstructorOrMethod(node);
        }
    }

    private boolean isRunMethod(MethodNode node) {
        if (node.getName().equals("run") && node.getParameters().length == 0) {
            ClassNode declaring = node.getDeclaringClass();
            return (node.getStart() == declaring.getStart() && node.getEnd() == declaring.getEnd());
        }
        return false;
    }


    /**
     * @param node
     */
    private void internalVisitConstructorOrMethod(MethodNode node) {
        if (!isRunMethod(node)) {
            if (!doTest(node)) {
                return;
            }
            currentDeclaration = node;
        }
        internalVisitParameters(node.getParameters(), node);

        if (node.getExceptions() != null) {
            for (ClassNode excep : node.getExceptions()) {
                if (doTest(excep)) {
                    createContext(null, node, EXCEPTIONS);
                }
            }
        }

        blockStack.push(node);
        visitAnnotations(node);
        blockStack.pop();

        Statement code = node.getCode();
        visitClassCodeContainer(code);


        if (completionOffset < code.getStart()) {
            // probably inside an empty parameters list
            createContext(null, node, PARAMETER);
        }

        // if we get here, then it is probably because the block statement
        // has been swapped with a new one that has not had
        // its locations set properly
        createContext(code, code, expressionScriptOrStatement(node));
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
        return supportingNodeEnd == -1 ? SCRIPT : EXPRESSION;
    }

    /**
     * @return
     */
    private ContentAssistLocation expressionOrStatement() {
        return supportingNodeEnd == -1 ? STATEMENT : EXPRESSION;
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
    // or visit ones with unknown source locations.
    @Override
    protected void visitStatement(Statement statement) {
        if (doTest(statement) || (statement.getStart() <= 0 && statement.getEnd() <= 0)) {
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

        // do not create a null context here.
        // in this case, the static initializer has moved to the <clinit> method
        // the end and name end comparison checks to see if there is extra
        // text after the name that constitutes an initializdr
        if (node.isStatic() && node.getEnd() > node.getNameEnd() + 1) {
            return;
        }
        currentDeclaration = node.getDeclaringClass();
        createContext(node, node.getDeclaringClass(), CLASS_BODY);
    }

    @Override
    public void visitProperty(PropertyNode node) {
        // FIXADE should not visit properties. Consider deleting
        // if (!doTest(node)) {
        // return;
        // }
        //
        // currentDeclaration = node;
        // ClassNode type = node.getType();
        // if (type != null && doTest(type)) {
        // createContext(null, node.getDeclaringClass(), CLASS_BODY);
        // }
        // blockStack.push(node);
        // super.visitProperty(node);
        // blockStack.pop();
        //
        // // do not create a null context here.
        // // in this case, the static initializer has moved to the <clinit> method
        // if (node.isStatic() && !node.hasInitialExpression()) {
        // return;
        // }
        //
        // createNullContext();
    }



    @Override
    public void visitVariableExpression(VariableExpression expression) {
        if (doTest(expression)) {
            createContext(expression, blockStack.peek(), expressionOrStatement());
        }

        super.visitVariableExpression(expression);
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
            createContext(call.getType(), blockStack.peek(),
                    ContentAssistLocation.CONSTRUCTOR);
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

    @Override
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
        super.visitStaticMethodCallExpression(call);

        // the method itself is not an expression, but only a string
        // so this check call will test for open declaration on the method
        if (doTest(call)) {
            createContext(call, blockStack.peek(), expressionOrStatement());
        }
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

    @Override
    public void visitBinaryExpression(BinaryExpression expression) {
        if (expression.getOperation().getText().equals("=")) {
            // keep track of the LHS expression
            lhsNode = expression.getLeftExpression();
        }
        super.visitBinaryExpression(expression);
        lhsNode = null;
        if (expression.getOperation().getText().equals("[")
                && doTest(expression)) {
            // after an array access
            createContext(expression, blockStack.peek(),
                    expressionOrStatement());
        }
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        if (!doTest(call)) {
            return;
        }
        super.visitMethodCallExpression(call);
        // if we are still here, this means that the location is part of
        // the open/close paren
        createContext(call.getMethod(), blockStack.peek(), expressionOrStatement());
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

    private ConstructorNode findDefaultConstructor(ClassNode node) {
        List<ConstructorNode> constructors = node.getDeclaredConstructors();
        for (ConstructorNode constructor : constructors) {
            if (constructor.getParameters() == null || constructor.getParameters().length == 0) {
                // only return automatically generated constructors
                if (constructor.getEnd() <= 0) {
                    return constructor;
                }
            }
        }
        return null;
    }

    private void createContext(ASTNode completionNode, ASTNode declaringNode, ContentAssistLocation location) {
        context = new ContentAssistContext(completionOffset,
                completionExpression, fullCompletionExpression, completionNode,
                declaringNode, lhsNode, location, unit, currentDeclaration,
                completionEnd);
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
