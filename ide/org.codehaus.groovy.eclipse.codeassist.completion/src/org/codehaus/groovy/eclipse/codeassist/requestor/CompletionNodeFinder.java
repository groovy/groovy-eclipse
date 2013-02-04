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
import java.util.Map;
import java.util.Stack;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ImportNodeCompatibilityWrapper;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
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
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.eclipse.core.util.VisitCompleteException;
import org.codehaus.groovy.runtime.GeneratedClosure;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;

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

    /**
     * This stack keeps track of the current argument list expressions so that
     * we can know if a completion of a MapExpression is actually an argument
     * and therefore named parameters should be available.
     */
    private Stack<TupleExpression> argsStack;

    public CompletionNodeFinder(int completionOffset, int completionEnd,
            int supportingNodeEnd,
            String completionExpression, String fullCompletionExpression) {
        this.completionOffset = completionOffset;
        this.completionEnd = completionEnd;
        this.supportingNodeEnd = supportingNodeEnd;
        this.completionExpression = completionExpression;
        this.fullCompletionExpression = fullCompletionExpression;
        this.blockStack = new Stack<ASTNode>();
        this.argsStack = new Stack<TupleExpression>();
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
        PackageNode packageNode = node.getPackage();
        if (packageNode != null && doTest(packageNode)) {
            currentDeclaration = packageNode;
            createContext(null, packageNode, ContentAssistLocation.PACKAGE);
        }

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
        if (supr != null && supr.getEnd() > 0 && doTest(supr)) {
            createContext(null, node, EXTENDS);
        }

        Iterator<InnerClassNode> innerClasses = node.getInnerClasses();
        if (innerClasses != null) {
            while (innerClasses.hasNext()) {
                InnerClassNode inner = innerClasses.next();
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
        for (FieldNode fn : node.getFields()) {
            visitField(fn);
        }

        for (ConstructorNode cn : node.getDeclaredConstructors()) {
            visitConstructor(cn);
        }

        for (MethodNode mn : node.getMethods()) {
            visitMethod(mn);
        }

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
            if (run != null && run.getDeclaringClass().equals(node)) {
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

    @Override
    public void visitAnnotations(AnnotatedNode node) {
        List<AnnotationNode> annotations = node.getAnnotations();
        if (annotations.isEmpty()) {
            return;
        }
        for (AnnotationNode an : annotations) {
            // skip built-in properties
            if (an.isBuiltIn()) {
                continue;
            }
            for (Map.Entry<String, Expression> member : an.getMembers().entrySet()) {
                member.getValue().visit(this);
            }
            if (doTest(an.getClassNode())) {
                createContext(node, currentDeclaration, ContentAssistLocation.ANNOTATION);
            }
        }
    }

    private boolean isRunMethod(MethodNode node) {
        // copied from MethodNode.isScriptBody().
        // however, this method does not exist on Groovy 1.7, so can't call it
        return node.getDeclaringClass() != null && node.getDeclaringClass().isScript()
                && node.getName().equals("run")
                && (node.getParameters() == null || node.getParameters().length == 0)
                && (node.getReturnType() != null && node.getReturnType().getName().equals("java.lang.Object"));
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


        if (completionOffset < code.getStart() && !isRunMethod(node)) {
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
        return isRunMethod(node) ?
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

    /**
     * There is a special case here, where there is a completion after a method
     * name with no parens
     *
     * <pre>
     *  obj.myMethodCall _
     * </pre>
     *
     * In this case, the expression of the {@link ExpressionStatement} is
     * either a {@link PropertyExpression} or a {@link VariableExpression}.
     * Perhaps there are other possibilities, too, though.
     */
    @Override
    public void visitExpressionStatement(ExpressionStatement statement) {
        super.visitExpressionStatement(statement);
        if (doTest(statement)) {
            Expression expression = statement.getExpression();
            int exprEnd = expression.getEnd();
            int stateEnd = statement.getEnd();
            // check if either the completionoffset or the supprting node end
            // is in the space between the end of the expression and the end
            // of the statement
            if ((completionOffset <= stateEnd && completionOffset > exprEnd)
                    || (supportingNodeEnd <= stateEnd && supportingNodeEnd > exprEnd)) {
                if (expression instanceof VariableExpression || expression instanceof ConstantExpression
                        || expression instanceof PropertyExpression) {
                    if (expression instanceof PropertyExpression) {
                        expression = getRightMost(expression);
                    }
                    if (expression != null) {
                        createContextForCallContext(expression, expression, expression.getText());
                    }
                }
            }
        }
    }

    /**
     * Returns rightmost expression in s property expression
     *
     * @param expression
     * @return
     */
    private Expression getRightMost(Expression expression) {
        if (expression instanceof VariableExpression || expression instanceof ConstantExpression) {
            return expression;
        } else if (expression instanceof PropertyExpression) {
            return getRightMost(((PropertyExpression) expression).getProperty());
        } else if (expression instanceof BinaryExpression) {
            return getRightMost(((BinaryExpression) expression).getRightExpression());
        }
        return null;
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
        if (!doTest(call)) {
            return;
        }

        // don't check here if the type reference is implicit
        // we know that the type is not implicit if the name
        // location is filled in.
        if (call.getOwnerType().getNameEnd() == 0 &&
                doTest(call.getOwnerType())) {
            createContext(call.getOwnerType(), blockStack.peek(), expressionOrStatement());
        }
        internalVisitCallArguments(call.getArguments());

        // the method itself is not an expression, but only a string

        if (doTest(call)) {
            createContext(call, blockStack.peek(), expressionOrStatement());
        }
    }


    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        blockStack.push(expression);
        internalVisitParameters(expression.getParameters(), expression.getCode());
        super.visitClosureExpression(expression);
        blockStack.pop();

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
    public void visitPropertyExpression(PropertyExpression expression) {
        if (!doTest(expression)) {
            return;
        }
        Expression objectExpression = expression.getObjectExpression();
        Expression propertyExpression = expression.getProperty();
        if (supportingNodeEnd >= 0 && supportingNodeEnd < propertyExpression.getStart()) {
            // GRECLIPSE-1374 probably completion after a parenthesized
            // expression
            createContext(objectExpression, blockStack.peek(), EXPRESSION);
        }

        // check for the special case of groovy command expressions
        checkForCommandExpression(objectExpression, propertyExpression);

        super.visitPropertyExpression(expression);
    }

    void checkForCommandExpression(Expression leftExpression, Expression rightExpression) {
        // see
        // http://docs.codehaus.org/display/GROOVY/Groovy+1.8+release+notes#Groovy18releasenotes-CommandchainsfornicerDomain-SpecificLanguages
        // if all of these are true, then we have a command expression:
        // if objectExpr is a method call with >1 arguments
        // if property is a constant expression
        // if there are no parens (note that we don't actually have to test for
        // this since if there are parens, then the result would still be the
        // same)
        // if doTest(expression.getProperty())

        Expression leftMost = leftMost(rightExpression);
        if (methodCallWithOneArgument(leftExpression) && leftMost instanceof ConstantExpression && doTest(leftMost)) {
            createContext(leftExpression, blockStack.peek(), EXPRESSION);
        }
    }

    boolean methodCallWithOneArgument(Expression objectExpression) {
        if (objectExpression instanceof MethodCallExpression) {
            Expression arguments = ((MethodCallExpression) objectExpression).getArguments();
            return arguments instanceof TupleExpression && ((TupleExpression) arguments).getExpressions() != null
                    && ((TupleExpression) arguments).getExpressions().size() > 0;
        } else {
            return false;
        }
    }

    private Expression leftMost(Expression expr) {
        if (expr instanceof ConstantExpression) {
            return expr;
        } else if (expr instanceof PropertyExpression) {
            return leftMost(((PropertyExpression) expr).getObjectExpression());
        } else if (expr instanceof MethodCallExpression) {
            return leftMost(((MethodCallExpression) expr).getObjectExpression());
        } else if (expr instanceof BinaryExpression) {
            return leftMost(((BinaryExpression) expr).getLeftExpression());
        } else {
            return null;
        }
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        if (!doTest(call)) {
            return;
        }
        Expression objectExpression = call.getObjectExpression();
        Expression methodExpression = call.getMethod();
        Expression arguments = call.getArguments();

        // Could be a groovy 1.8 style command expression
        checkForCommandExpression(objectExpression, methodExpression);

        // here, we check to see if we are after the closing paren or before it.
        // not quite as easy as I would have hoped since the AST doesn't track
        // this information
        checkForAfterClosingParen(methodExpression, arguments);

        objectExpression.visit(this);
        if (supportingNodeEnd >= 0 && supportingNodeEnd < methodExpression.getStart()) {
            // GRECLIPSE-1374 probably completion after a parenthesized
            // expression
            createContext(objectExpression, blockStack.peek(), EXPRESSION);
        }
        methodExpression.visit(this);

        // here do a check if we are inside of a method call, but we are not at
        // any particular expression (ie- this is the METHOD_CONTEXT)
        // there is a special case that when we are at the first character of
        // any expression in an argument list,
        // we want to do the context instead of normal completion.
        // do that check below
        internalVisitCallArguments(arguments);

        // if we get here, then we still want to do the context
        // we are either at a paren, at a comma, or at a start of an expression
        createContextForCallContext(call, methodExpression, // call.isImplicitThis()
                                                            // ?
                                                            // methodExpression
                                                            // :
                                                            // objectExpression,
        // this is not exactly right since it will
        // fail on funky kinds of method calls, like those that are called by a
        // GString
                methodExpression.getText());
    }

    @Override
    public void visitConstructorCallExpression(
            ConstructorCallExpression call) {
        if (!doTest(call)) {
            return;
        }

        Expression arguments = call.getArguments();
        ClassNode constructorType = call.getType();
        checkForAfterClosingParen(call, arguments);

        if (doTest(constructorType)) {
            createContext(constructorType, blockStack.peek(),
                    ContentAssistLocation.CONSTRUCTOR);
        }

        // see comments in visitMethodCallExpression
        internalVisitCallArguments(arguments);

        // GRECLIPSE-1235
        // determine if we are completing on the fully qualified name
        // assume that this is not a partially qualified name
        String constructorName = constructorType.getNameWithoutPackage();
        if (constructorName.length() < constructorType.getLength()) {
            // assume fully qualified name
            constructorName = constructorType.getName();
        }
        fullCompletionExpression = "new " + constructorName;

        createContextForCallContext(call, constructorType, constructorName);
    }

    private void checkForAfterClosingParen(AnnotatedNode contextTarget, Expression arguments) {
        int lastArgEnd = findLastArgumentEnd(arguments);
        int start = arguments.getStart();
        if (start == 0 && arguments instanceof TupleExpression && ((TupleExpression) arguments).getExpressions().size() > 0) {
            // Tuple expressions as argument lists do not always have slocs
            // available
            start = ((TupleExpression) arguments).getExpression(0).getStart();
        }

        if (start == 0 && lastArgEnd == 0) {
            // possibly a malformed constructor call with no parens
            return;
        }

        boolean shouldLookAtArguments = !(lastArgEnd == start && completionOffset == start);
        if (shouldLookAtArguments) {
            if (after(lastArgEnd)) {
                // we are at this situation (completing on 'x'):
                // foo().x
                createContext(contextTarget, blockStack.peek(), expressionOrStatement());
            }
        } else {
            // completion inside of empty argument list:
            // foo()
            // should show context, so do nothing here
        }
    }

    @Override
    public void visitArgumentlistExpression(ArgumentListExpression ale) {
        super.visitArgumentlistExpression(ale);
    }

    @Override
    public void visitListExpression(ListExpression expression) {
        super.visitListExpression(expression);
        if (doTest(expression)) {
            // completion after a list expression: []._ or [10]._
            createContext(expression, currentDeclaration, EXPRESSION);
        }
    }

    @Override
    public void visitMapExpression(MapExpression expression) {
        super.visitMapExpression(expression);
        if (doTest(expression)) {
            // if this map is part of the enclosing argumentlistexpression,
            // then assume that we are completing on named arguments
            if (!isArgument(expression)) {
                // completion after a list expression: [:]._ or [x:10]._
                createContext(expression, currentDeclaration, EXPRESSION);
            } else {
                // do method context
            }
        }
    }

    @Override
    public void visitGStringExpression(GStringExpression expression) {
        visitListOfExpressions(expression.getValues());

        // now check to see if we are in a string
        List<ConstantExpression> strings = expression.getStrings();
        for (ConstantExpression stringExpr : strings) {
            if (doTest(stringExpr)) {
                // inside of a string, no completions available
                context = null;
                throw new VisitCompleteException();
            }
        }
    }

    /**
     * @param expression
     * @return true iff this expression is an argument in the enclosing ALE
     */
    private boolean isArgument(Expression expression) {
        if (argsStack.isEmpty()) {
            return false;
        }
        TupleExpression args = argsStack.peek();
        if (args.getExpressions() != null) {
            for (Expression arg : args.getExpressions()) {
                if (arg == expression) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * finds end of the last argument of an argument list expression
     *
     * @param call
     * @return
     */
    private int findLastArgumentEnd(Expression args) {
        // need to look at the last argument expression as well as the argument
        // list itself.
        // problem is that closure expressions are not included in the end
        // offset
        int listEnd = args.getEnd();
        int lastExpressionEnd = -1;
        if (args instanceof TupleExpression) {
            TupleExpression list = (TupleExpression) args;
            int numExprs = list.getExpressions().size();
            if (numExprs > 0) {
                if (listEnd == 0) {
                    listEnd = list.getExpression(numExprs - 1).getEnd();
                }

                Expression lastExpression = list.getExpression(numExprs - 1);
                lastExpressionEnd = lastExpression.getEnd();
            }
        }
        return Math.max(listEnd, lastExpressionEnd);
    }

    /**
     *
     * @param call
     * @return true iff comlpetion offset is after the last argument of the
     *         argument list
     */
    private boolean after(int callEnd) {
        return (supportingNodeEnd == -1 && callEnd < completionOffset) || callEnd <= supportingNodeEnd;
    }

    /**
     * Visit method/constructor call arguments, but only if
     * we are not at the start of an expression. Otherwise,
     * we don't do normal completion, but only show context information
     *
     * @param arguments
     */
    private void internalVisitCallArguments(Expression arguments) {
        // check to see if we are definitely doing the context
        boolean doContext = false;
        if (arguments instanceof TupleExpression) {
            TupleExpression tuple = (TupleExpression) arguments;
            for (Expression expr : tuple.getExpressions()) {
                if (expr.getStart() == completionOffset) {
                    doContext = true;
                    break;
                }
            }
            argsStack.push(tuple);
        } else if (arguments != null) {
            if (arguments.getStart() == completionOffset) {
                doContext = true;
            }
        }

        if (!doContext) {
            // check to see if we are exactly inside of one of the arguments,
            // ignores in between arguments
            arguments.visit(this);
        }
        if (arguments instanceof TupleExpression) {
            argsStack.pop();
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

        if (p.getNameStart() < completionOffset && p.getNameEnd() >= completionOffset) {
            // completion on the parameter name, but should be treated as a type
            createContext(null, declaringNode, PARAMETER);
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

    /**
     * In this case, we are really completing on the method name and not
     * inside the parens so change the information
     *
     * @param call
     */
    private void createContextForCallContext(Expression origExpression, AnnotatedNode methodExpr, String methodName) {
        context = new MethodInfoContentAssistContext(completionOffset, completionExpression, fullCompletionExpression,
                origExpression, blockStack.peek(), lhsNode, unit, currentDeclaration, completionEnd, methodExpr, methodName,
                methodExpr.getEnd());
        throw new VisitCompleteException();
    }

    private void createContext(ASTNode completionNode, ASTNode declaringNode, ContentAssistLocation location) {
        context = new ContentAssistContext(completionOffset,
                completionExpression, fullCompletionExpression, completionNode,
                declaringNode, lhsNode, location, unit, currentDeclaration,
                completionEnd);
        throw new VisitCompleteException();
    }


    protected boolean doTest(ASTNode node) {
        return node.getEnd() > 0
                && ((supportingNodeEnd > node.getStart() && supportingNodeEnd <= node.getEnd()) || (completionOffset > node
                        .getStart() && completionOffset <= node.getEnd()));
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
