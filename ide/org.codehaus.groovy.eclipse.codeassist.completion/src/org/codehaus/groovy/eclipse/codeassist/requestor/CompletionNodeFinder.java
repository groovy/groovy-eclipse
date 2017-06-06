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
package org.codehaus.groovy.eclipse.codeassist.requestor;

import static org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation.ANNOTATION_BODY;
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
import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
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
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.eclipse.core.util.VisitCompleteException;
import org.codehaus.groovy.runtime.GeneratedClosure;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;

/**
 * Finds the completion node for an offset and calculates the content assist context.
 */
public class CompletionNodeFinder extends ClassCodeVisitorSupport {

    private int completionEnd;
    private int completionOffset;
    private int supportingNodeEnd;
    private String completionExpression;
    private String fullCompletionExpression;
    private AnnotatedNode currentDeclaration;

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
    private final LinkedList<TupleExpression> argsStack = new LinkedList<TupleExpression>();

    private final LinkedList<ASTNode> blockStack = new LinkedList<ASTNode>();

    public CompletionNodeFinder(
        int completionOffset,
        int completionEnd,
        int supportingNodeEnd,
        String completionExpression,
        String fullCompletionExpression) {

        this.completionOffset = completionOffset;
        this.completionEnd = completionEnd;
        this.supportingNodeEnd = supportingNodeEnd;
        this.completionExpression = completionExpression;
        this.fullCompletionExpression = fullCompletionExpression;
    }

    public ContentAssistContext findContentAssistContext(GroovyCompilationUnit unit) {
        try {
            this.unit = unit;
            ModuleNode node = unit.getModuleNode();

            visitImports(node);
            // visit script last because sometimes its source locations wrap around the other classes
            ClassNode script = null;
            for (ClassNode clazz : (Iterable<ClassNode>) node.getClasses()) {
                if (clazz.isScript()) {
                    script = clazz;
                } else {
                    visitClass(clazz);
                }
            }
            if (script != null) {
                visitClass(script);
            }
        } catch (VisitCompleteException e) {
        }
        return context;
    }

    @Override
    public void visitImports(ModuleNode node) {
        PackageNode packageNode = node.getPackage();
        if (packageNode != null && doTest(packageNode)) {
            currentDeclaration = packageNode;
            createContext(null, packageNode, ContentAssistLocation.PACKAGE);
        }

        for (ImportNode importNode : GroovyUtils.getAllImportNodes(node)) {
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
        blockStack.add(node);
        visitAnnotations(node);
        blockStack.removeLast();
        ClassNode supr = node.getUnresolvedSuperClass();
        if (supr != null && supr.getEnd() > 0 && doTest(supr)) {
            createContext(null, node, EXTENDS);
        }

        Iterator<InnerClassNode> innerClasses = node.getInnerClasses();
        if (innerClasses != null) {
            while (innerClasses.hasNext()) {
                InnerClassNode inner = innerClasses.next();
                if (!inner.isSynthetic() || inner instanceof GeneratedClosure) {
                    visitClass(inner);
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
        MethodNode clinit = node.getMethod("<clinit>", Parameter.EMPTY_ARRAY);
        if (clinit != null && clinit.getCode() instanceof BlockStatement) {
            blockStack.add(clinit.getCode());
            for (Statement element : (Iterable<Statement>) ((BlockStatement) clinit.getCode()).getStatements()) {
                element.visit(this);
            }
            blockStack.removeLast();
        }

        // visit default constructors that have been added by the verifier; this is where initializers lie
        ConstructorNode init = findDefaultConstructor(node);
        if (init != null) {
            Statement statement = init.getCode();
            if (statement instanceof ExpressionStatement) {
                ((ExpressionStatement) statement).visit(this);
            } else {
                blockStack.add(init.getCode());
                for (Statement element : (Iterable<Statement>) ((BlockStatement) init.getCode()).getStatements()) {
                    element.visit(this);
                }
                blockStack.removeLast();
            }
        }

        currentDeclaration = node;
        for (Statement element : (Iterable<Statement>) node.getObjectInitializerStatements()) {
            element.visit(this);
        }

        // do the run method last since it can wrap around other methods
        if (node.isScript()) {
            MethodNode run = node.getMethod("run", Parameter.EMPTY_ARRAY);
            if (run != null && run.getDeclaringClass().equals(node)) {
                internalVisitConstructorOrMethod(run);
            }
        }

        // if exception has not been thrown, we are inside this class body
        createContext(null, node, node.isScript() ? SCRIPT : CLASS_BODY);
    }

    @Override
    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        // script body must be visited last
        if (!node.isScriptBody()) {
            internalVisitConstructorOrMethod(node);
        }
    }

    @Override
    protected void visitAnnotation(AnnotationNode annotation) {
        if (doTest(annotation.getClassNode())) {
            createContext(annotation, currentDeclaration, ContentAssistLocation.ANNOTATION);
        }
        blockStack.add(annotation);
        super.visitAnnotation(annotation);
        int annoEnd = GroovyUtils.endOffset(annotation),
            nameEnd = annotation.getClassNode().getEnd();
        if (annoEnd > nameEnd) { // annotation has a body
            if (completionOffset > nameEnd && completionOffset < annoEnd) {
                // TODO: Is it possible to determine method node when completing a value?
                createContext(annotation, annotation, ContentAssistLocation.ANNOTATION_BODY);
            }
        }
        blockStack.removeLast();
    }

    @Override
    public void visitBlockStatement(BlockStatement statement) {
        blockStack.add(statement);
        super.visitBlockStatement(statement);

        if (doTest(statement)) {
            // if we get here, then we know that we are in this block statement,
            // but not inside any expression.  Use this to complete on
            createContext(blockStack.getLast(), statement, expressionOrStatement());
        }
        blockStack.removeLast();
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
            if ((completionOffset <= stateEnd && completionOffset > exprEnd) ||
                    (supportingNodeEnd <= stateEnd && supportingNodeEnd > exprEnd)) {
                if (expression instanceof VariableExpression ||
                    expression instanceof ConstantExpression ||
                    expression instanceof PropertyExpression) {

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

        blockStack.add(node);
        super.visitField(node);
        blockStack.removeLast();

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
            ContentAssistLocation loc = EXPRESSION;
            if (blockStack.getLast() instanceof AnnotationNode) {
                loc = ANNOTATION_BODY;
            } else if (supportingNodeEnd == -1) {
                loc = STATEMENT;
            }
            createContext(expression, blockStack.getLast(), loc);
        }

        super.visitVariableExpression(expression);
    }

    @Override
    public void visitFieldExpression(FieldExpression expression) {
        if (doTest(expression)) {
            createContext(expression, blockStack.getLast(), expressionOrStatement());
        }
        super.visitFieldExpression(expression);
    }

    @Override
    public void visitClassExpression(ClassExpression expression) {
        if (doTest(expression)) {
            createContext(expression, blockStack.getLast(), expressionOrStatement());
        }
        super.visitClassExpression(expression);
    }

    @Override
    public void visitConstantExpression(ConstantExpression expression) {
        if (doTest(expression)) {
            createContext(expression, blockStack.getLast(), expressionOrStatement());
        }
        super.visitConstantExpression(expression);
    }

    @Override
    public void visitCastExpression(CastExpression node) {
        if (doTest(node.getType())) {
            createContext(node.getType(), blockStack.getLast(), expressionOrStatement());
        }
        super.visitCastExpression(node);
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        ClassNode type = expression.getLeftExpression().getType();
        if (doTest(type)) {
            createContext(type, blockStack.getLast(), expressionOrStatement());
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
            createContext(expression, blockStack.getLast(), expressionOrStatement());
        }
        super.visitArrayExpression(expression);
    }

    @Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression expression) {
        if (!doTest(expression)) {
            return;
        }

        // don't check here if the type reference is implicit
        // we know that the type is not implicit if the name
        // location is filled in.
        if (expression.getOwnerType().getNameEnd() == 0 && doTest(expression.getOwnerType())) {
            createContext(expression.getOwnerType(), blockStack.getLast(), expressionOrStatement());
        }
        internalVisitCallArguments(expression.getArguments(), expression);

        // the method itself is not an expression, but only a string

        if (doTest(expression)) {
            createContext(expression, blockStack.getLast(), expressionOrStatement());
        }
    }

    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        blockStack.add(expression);
        internalVisitParameters(expression.getParameters(), expression.getCode());
        super.visitClosureExpression(expression);
        blockStack.removeLast();

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
        if (expression.getOperation().getText().equals("[") && doTest(expression)) {
            // after an array access
            createContext(expression, blockStack.getLast(), expressionOrStatement());
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
            createContext(objectExpression, blockStack.getLast(), EXPRESSION);
        }

        // check for the special case of groovy command expressions
        checkForCommandExpression(objectExpression, propertyExpression);

        super.visitPropertyExpression(expression);
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression expression) {
        if (!doTest(expression)) {
            return;
        }
        Expression objectExpression = expression.getObjectExpression();
        Expression methodExpression = expression.getMethod();
        Expression arguments = expression.getArguments();

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
            createContext(objectExpression, blockStack.getLast(), EXPRESSION);
        }
        methodExpression.visit(this);

        // here do a check if we are inside of a method call, but we are not at
        // any particular expression (ie- this is the METHOD_CONTEXT)
        // there is a special case that when we are at the first character of
        // any expression in an argument list,
        // we want to do the context instead of normal completion.
        // do that check below
        internalVisitCallArguments(arguments, expression);

        // if we get here, then we still want to do the context
        // we are either at a paren, at a comma, or at a start of an expression
        createContextForCallContext(expression, methodExpression,
            /*call.isImplicitThis() ? methodExpression : objectExpression,*/
            // this is not exactly right since it will fail on funky kinds of method calls, like those that are called by a GString
            methodExpression.getText());
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression expression) {
        if (!doTest(expression)) {
            return;
        }

        Expression arguments = expression.getArguments();
        ClassNode constructorType = expression.getType();
        checkForAfterClosingParen(expression, arguments);

        if (doTest(constructorType)) {
            createContext(constructorType, blockStack.getLast(), ContentAssistLocation.CONSTRUCTOR);
        }

        // see comments in visitMethodCallExpression
        internalVisitCallArguments(arguments, expression);

        // GRECLIPSE-1235
        // determine if we are completing on the fully qualified name
        // assume that this is not a partially qualified name
        String constructorName = constructorType.getNameWithoutPackage();
        if (constructorName.length() < constructorType.getLength()) {
            // assume fully qualified name
            constructorName = constructorType.getName();
        }
        fullCompletionExpression = "new " + constructorName;

        createContextForCallContext(expression, constructorType, constructorName);
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
            // if this map is part of the enclosing ArgumentListExpression,
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
    public void visitMapEntryExpression(MapEntryExpression expression) {
        // Is the completion located within the RHS of a named argument pair?
        if (doTest(expression.getValueExpression()) && isArgument(expression)) {
            lhsNode = expression.getKeyExpression();
        }
        super.visitMapEntryExpression(expression);
    }

    @Override
    public void visitGStringExpression(GStringExpression expression) {
        visitListOfExpressions(expression.getValues());
        for (ConstantExpression stringExpr : expression.getStrings()) {
            if (doTest(stringExpr)) {
                // no completions available within string literals
                context = null;
                throw new VisitCompleteException();
            }
        }
    }

    //--------------------------------------------------------------------------

    private boolean isArgument(Expression expr) {
        if (argsStack.isEmpty()) {
            return false;
        }
        TupleExpression tuple = argsStack.getLast();
        return isArgument(expr, tuple.getExpressions());
    }

    private static boolean isArgument(Expression expr, List<? extends Expression> args) {
        if (args != null && !args.isEmpty()) {
            for (Expression arg : args) {
                if (arg instanceof NamedArgumentListExpression) {
                    return isArgument(expr, ((NamedArgumentListExpression) arg).getMapEntryExpressions());
                }
                if (arg == expr) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * finds end of the last argument of an argument list expression
     */
    private int findLastArgumentEnd(Expression args) {
        // need to look at the last argument expression as well as the argument list itself
        // problem is that closure expressions are not included in the end offset
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
     * @return {@code true} iff comlpetion offset is after the last argument of the argument list
     */
    private boolean after(int callEnd) {
        return (supportingNodeEnd == -1 && callEnd < completionOffset) || callEnd <= supportingNodeEnd;
    }

    /**
     * Visit method/constructor call arguments, but only if we are not at the
     * start of an expression. Otherwise, we don't do normal completion, but
     * only show context information.
     */
    private void internalVisitCallArguments(Expression args, Expression call) {
        // check to see if we are definitely doing the context
        boolean doContext = false;
        if (args instanceof TupleExpression) {
            TupleExpression tuple = (TupleExpression) args;
            for (Expression expr : tuple.getExpressions()) {
                if (expr.getStart() == completionOffset) {
                    doContext = true;
                    break;
                }
            }
            argsStack.add(tuple);
        } else if (args != null) {
            if (args.getStart() == completionOffset) {
                doContext = true;
            }
        }

        if (!doContext) {
            // check to see if we are exactly inside of one of the arguments, ignores in between arguments
            args.visit(this);
        }
        if (args instanceof TupleExpression) {
            argsStack.removeLast();
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

    private void internalVisitConstructorOrMethod(MethodNode node) {
        if (!node.isScriptBody()) {
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

        blockStack.add(node);
        visitAnnotations(node);
        blockStack.removeLast();

        Statement code = node.getCode();

        /*
         * Traits have code assigned to null by the TraitASTTransformation. The
         * transformation creates a helper inner class node that has the
         * necessary data. Hence skip the rest if code is null
         */
        if (code != null) {
            visitClassCodeContainer(code);

            if (completionOffset < code.getStart() && !node.isScriptBody()) {
                // probably inside an empty parameters list
                createContext(null, node, PARAMETER);
            }

            // if we get here, then it is probably because the block statement
            // has been swapped with a new one that has not had
            // its locations set properly
            code.setStart(node.getStart());
            code.setEnd(node.getEnd());
            createContext(code, code, expressionScriptOrStatement(node));
        }
    }

    private ContentAssistLocation expressionScriptOrStatement(MethodNode node) {
        return node.isScriptBody() ? expressionOrScript() : expressionOrStatement();
    }

    private ContentAssistLocation expressionOrScript() {
        return supportingNodeEnd == -1 ? SCRIPT : EXPRESSION;
    }

    private ContentAssistLocation expressionOrStatement() {
        return supportingNodeEnd == -1 ? STATEMENT : EXPRESSION;
    }

    /**
     * Returns rightmost expression in s property expression
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

    void checkForCommandExpression(Expression leftExpression, Expression rightExpression) {
        // if all of these are true, then we have a command expression:
        // if objectExpr is a method call with >1 arguments
        // if property is a constant expression
        // if there are no parens (note that we don't actually have to test for
        // this since if there are parens, then the result would still be the same)

        Expression leftMost = leftMost(rightExpression);
        if (methodCallWithOneArgument(leftExpression) && leftMost instanceof ConstantExpression && doTest(leftMost)) {
            createContext(leftExpression, blockStack.getLast(), EXPRESSION);
        }
    }

    boolean methodCallWithOneArgument(Expression objectExpression) {
        if (objectExpression instanceof MethodCallExpression) {
            Expression arguments = ((MethodCallExpression) objectExpression).getArguments();
            return arguments instanceof TupleExpression &&
                ((TupleExpression) arguments).getExpressions() != null &&
                !((TupleExpression) arguments).getExpressions().isEmpty();
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

    private void checkForAfterClosingParen(AnnotatedNode contextTarget, Expression arguments) {
        int lastArgEnd = findLastArgumentEnd(arguments);
        int start = arguments.getStart();
        if (start == 0 && arguments instanceof TupleExpression && !((TupleExpression) arguments).getExpressions().isEmpty()) {
            // TupleExpressions as argument lists do not always have slocs available
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
                createContext(contextTarget, blockStack.getLast(), expressionOrStatement());
            }
        } else {
            // completion inside of empty argument list:
            // foo()
            // should show context, so do nothing here
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
     * In this case, we are really completing on the method name and not inside the parens so change the information.
     */
    private void createContextForCallContext(Expression origExpression, AnnotatedNode methodExpr, String methodName) {
        context = new MethodInfoContentAssistContext(
            completionOffset,
            completionExpression,
            fullCompletionExpression,
            origExpression,
            blockStack.getLast(),
            lhsNode,
            unit,
            currentDeclaration,
            completionEnd,
            methodExpr,
            methodName,
            methodExpr.getEnd());
        throw new VisitCompleteException();
    }

    private void createContext(ASTNode completionNode, ASTNode declaringNode, ContentAssistLocation location) {
        context = new ContentAssistContext(
            completionOffset,
            completionExpression,
            fullCompletionExpression,
            completionNode,
            declaringNode,
            lhsNode,
            location,
            unit,
            currentDeclaration,
            completionEnd);
        throw new VisitCompleteException();
    }

    protected boolean doTest(ASTNode node) {
        if (node.getEnd() > 0) {
            boolean containsCompletionOffset = (completionOffset > node.getStart() && completionOffset <= node.getEnd());
            boolean containsSupportingOffset = (supportingNodeEnd > node.getStart() && supportingNodeEnd <= node.getEnd());

            return containsCompletionOffset || containsSupportingOffset;
        }
        return false;
    }

    /**
     * will be null if no completions are available at the location
     */
    public ContentAssistContext getContext() {
        return context;
    }
}
