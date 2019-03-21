/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.requestor;

import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
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
import org.codehaus.groovy.syntax.Types;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.groovy.core.util.DepthFirstVisitor;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;

/**
 * Finds the completion node for an offset and calculates the content assist context.
 */
public class CompletionNodeFinder extends DepthFirstVisitor {

    private int completionEnd;
    private int completionOffset;
    private int supportingNodeEnd;
    private String completionExpression;
    private String fullCompletionExpression;

    private GroovyCompilationUnit unit;
    private ContentAssistContext context;

    /**
     * Left hand side of any assignment statement or {@code null} if there is none.
     */
    private Expression lhsNode;

    private final LinkedList<ASTNode> blockStack = new LinkedList<ASTNode>();

    private final LinkedList<AnnotatedNode> declarationStack = new LinkedList<AnnotatedNode>();

    /**
     * Tracks the current argument list expressions so that we can know if a
     * completion of a MapExpression is actually an argument and therefore
     * named parameters should be available.
     */
    private final LinkedList<TupleExpression> argumentListStack = new LinkedList<TupleExpression>();

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

    //--------------------------------------------------------------------------

    public ContentAssistContext findContentAssistContext(GroovyCompilationUnit unit) {
        try {
            this.unit = unit;
            visitModule(unit.getModuleNode());
        } catch (VisitCompleteException e) {
            // successful visitation
        } finally {
            this.unit = null;
        }
        return context;
    }

    public ContentAssistContext getContext() {
        return context;
    }

    @Override
    public void visitPackage(PackageNode node) {
        declarationStack.add(node);

        visitAnnotations(node.getAnnotations());
        if (check(node)) {
            createContext(null, node, ContentAssistLocation.PACKAGE);
        }

        declarationStack.removeLast();
    }

    @Override
    public void visitImport(ImportNode node) {
        declarationStack.add(node);

        visitAnnotations(node.getAnnotations());
        if (check(node)) {
            createContext(null, node, ContentAssistLocation.IMPORT);
        }

        declarationStack.removeLast();
    }

    @Override
    public void visitClass(ClassNode node) {
        if (!check(node)) {
            return;
        }

        declarationStack.add(node);

        ClassNode ext = node.getUnresolvedSuperClass();
        if (check(ext)) {
            createContext(null, node, ContentAssistLocation.EXTENDS);
        }

        for (ClassNode imp : node.getUnresolvedInterfaces()) {
            if (check(imp)) {
                createContext(null, node, ContentAssistLocation.IMPLEMENTS);
            }
        }

        blockStack.add(node);
        super.visitClass(node);
        blockStack.removeLast();

        if (!node.isScript()) { // script body handled by visitMethod
            createContext(null, node, ContentAssistLocation.CLASS_BODY);
        }

        declarationStack.removeLast();
    }

    @Override
    public void visitField(FieldNode node) {
        if (!check(node)) {
            return;
        }

        declarationStack.add(node);

        if (check(node.getType()) && !node.isEnum()) {
            createContext(null, node.getDeclaringClass(), ContentAssistLocation.CLASS_BODY);
        }

        blockStack.add(node);
        super.visitField(node);
        blockStack.removeLast();

        declarationStack.removeLast();

        // enum bodies and static initializers are included in the field node's source range but are rooted elsewhere
        if (!node.isEnum() && !(node.isStatic() && node.getEnd() > node.getNameEnd() + 1)) {
            createContext(node, node.getDeclaringClass(), ContentAssistLocation.CLASS_BODY);
        }
    }

    @Override
    public void visitProperty(PropertyNode node) {
        if (!check(node)) {
            return;
        }

        declarationStack.add(node);

        ClassNode type = node.getType();
        if (check(type)) {
            createContext(null, node.getDeclaringClass(), ContentAssistLocation.CLASS_BODY);
        }

        blockStack.add(node);
        super.visitProperty(node);
        blockStack.removeLast();

        declarationStack.removeLast();
    }

    @Override
    public void visitMethod(MethodNode node) {
        if (node == runMethod) {
            return;
        }

        declarationStack.add(node);

        blockStack.add(node);
        super.visitMethod(node);
        blockStack.removeLast();

        if (isNotEmpty(node.getExceptions()) && !node.isScriptBody()) {
            for (ClassNode type : node.getExceptions()) {
                if (check(type)) {
                    createContext(null, node, ContentAssistLocation.EXCEPTIONS);
                }
            }
        }

        Statement body = node.getCode();
        if (body != null) {
            if (completionOffset > node.getStart() && completionOffset < body.getStart() && !node.isScriptBody()) {
                // probably inside an empty parameters list
                createContext(null, node, ContentAssistLocation.PARAMETER);
            }
            if (check(node) || node.isScriptBody()) {
                if (body.getEnd() < 1) body.setSourcePosition(node);
                createContext(body, body, expressionScriptOrStatement(node));
            }
        }

        declarationStack.removeLast();
    }

    @Override
    protected void visitAnnotation(AnnotationNode node) {
        if (node.getStart() <= completionOffset && completionOffset <= node.getClassNode().getEnd()) {
            createContext(node, declarationStack.getLast(), ContentAssistLocation.ANNOTATION);
        }
        blockStack.add(node);
        super.visitAnnotation(node);
        int annoEnd = GroovyUtils.endOffset(node),
            nameEnd = node.getClassNode().getEnd();
        if (annoEnd > nameEnd) { // annotation has a body
            if (completionOffset > nameEnd && completionOffset < annoEnd) {
                // TODO: Is it possible to determine method node when completing a value?
                createContext(node, node, ContentAssistLocation.ANNOTATION_BODY);
            }
        }
        blockStack.removeLast();
    }

    @Override
    protected void visitParameter(Parameter node) {
        super.visitParameter(node);

        // check parameter type
        if (check(node.getType()) || (check(node) && completionOffset < node.getNameStart())) {
            createContext(node, blockStack.getLast(), ContentAssistLocation.PARAMETER);
        }

        // check parameter name
        if (node.getNameStart() <= completionOffset && completionOffset <= node.getNameEnd()) {
            createContext(node, blockStack.getLast(), ContentAssistLocation.PARAMETER);
        }

        // check for default value expression (that was moved during verification)
        if (check(node)) {
            createContext(node, blockStack.getLast(), expressionOrStatement());
        }
    }

    /**
     * Visit method/constructor call arguments, but only if we are not at the
     * start of an expression. Otherwise, we don't do normal completion, but
     * only show context information.
     */
    private void visitArguments(Expression args, Expression call) {
        // check to see if we are definitely doing context
        boolean doContext = false;
        if (args instanceof TupleExpression) {
            TupleExpression tuple = (TupleExpression) args;
            for (Expression expr : tuple.getExpressions()) {
                if (expr.getStart() == completionOffset) {
                    doContext = true;
                    break;
                }
            }
            argumentListStack.add(tuple);
        } else if (args != null && args.getStart() == completionOffset) {
            doContext = true;
        }

        if (!doContext) {
            blockStack.add(call);
            // outer receiver is irrelevant within argument list
            final Expression lhs = lhsNode; lhsNode = null;

            // check the arguments; ignores in-between locations
            args.visit(this);

            lhsNode = lhs;
            blockStack.removeLast();
        }
        if (args instanceof TupleExpression) {
            argumentListStack.removeLast();
        }
    }

    // statements:

    @Override
    public void visitBlockStatement(BlockStatement statement) {
        blockStack.add(statement);
        super.visitBlockStatement(statement);
        if (check(statement)) {
            // if we get here, then we know that we are in this block statement,
            // but not inside any expression.  Use this to complete on
            createContext(blockStack.getLast(), statement, expressionOrStatement());
        }
        blockStack.removeLast();
    }

    @Override
    public void visitCatchStatement(CatchStatement statement) {
        blockStack.add(statement); // for param check
        super.visitCatchStatement(statement);
        blockStack.removeLast();
    }

    /**
     * In this case, the expression of the {@link ExpressionStatement} is
     * either a {@link PropertyExpression} or a {@link VariableExpression}.
     * Perhaps there are other possibilities...
     * <p>
     * There is a special case here, where there is a completion after a method
     * name with no parens:
     * <pre>
     *  obj.myMethodCall _
     * </pre>
     */
    @Override
    public void visitExpressionStatement(ExpressionStatement statement) {
        super.visitExpressionStatement(statement);
        if (check(statement)) {
            Expression expression = statement.getExpression();
            int exprEnd = expression.getEnd();
            int stateEnd = statement.getEnd();
            // check if either the completionOffset or the supprting node end
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
                        if (supportingNodeEnd > 0 && fullCompletionExpression.endsWith(".") && completionExpression.equals("")) {
                            createContext(expression, blockStack.getLast(), ContentAssistLocation.EXPRESSION);
                        } else {
                            createContextForCallContext(expression, expression, expression.getText());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void visitForLoop(ForStatement statement) {
        blockStack.add(statement); // for param check
        super.visitForLoop(statement);
        blockStack.removeLast();
    }

    // expressions:

    @Override
    public void visitArrayExpression(ArrayExpression expression) {
        if (check(expression)) {
            createContext(expression, blockStack.getLast(), expressionOrStatement());
        }
        super.visitArrayExpression(expression);
    }

    @Override
    public void visitBinaryExpression(BinaryExpression expression) {
        if (expression.getOperation().getType() == Types.ASSIGN) {
            lhsNode = expression.getLeftExpression();
            super.visitBinaryExpression(expression);
            lhsNode = null;
        } else {
            super.visitBinaryExpression(expression);
        }

        // check for an array access expression
        if (expression.getOperation().getType() == Types.LEFT_SQUARE_BRACKET && check(expression)) {
            createContext(expression, blockStack.getLast(), expressionOrStatement());
        }
    }

    @Override
    public void visitCastExpression(CastExpression expression) {
        if (check(expression.getType())) {
            createContext(expression.getType(), blockStack.getLast(), expressionOrStatement());
        }
        super.visitCastExpression(expression);
    }

    @Override
    public void visitClassExpression(ClassExpression expression) {
        if (check(expression)) {
            createContext(expression, blockStack.getLast(), expressionOrStatement());
        }
        super.visitClassExpression(expression);
    }

    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        blockStack.add(expression);
        super.visitClosureExpression(expression);
        blockStack.removeLast();

        // sometimes the code block does not end at the closing '}', but at the end of the last statement
        // the closure itself ends at the last '}'.  So, do that test here.
        if (check(expression)) {
            createContext(expression, expression.getCode(), expressionOrStatement());
        }
    }

    @Override
    public void visitConstantExpression(ConstantExpression expression) {
        if (expression instanceof AnnotationConstantExpression && check(expression.getType())) {
            // ex: @interface X { Y default @Y(...) } -- expression is "@Y(...)"
            createContext(expression, blockStack.getLast(), ContentAssistLocation.ANNOTATION);
        }
        if (check(expression)) {
            createContext(expression, blockStack.getLast(), expressionOrStatement());
        }
        super.visitConstantExpression(expression);
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression expression) {
        if (!check(expression)) {
            return;
        }

        Expression arguments = expression.getArguments();
        checkForAfterClosingParen(expression, arguments);
        ClassNode constructorType = expression.getType();

        if (check(constructorType)) {
            createContext(constructorType, blockStack.getLast(), ContentAssistLocation.CONSTRUCTOR);
        }

        try {
            // see comments in visitMethodCallExpression
            visitArguments(arguments, expression);
        } catch (VisitCompleteException e) {
            if (context.location != ContentAssistLocation.STATEMENT) {
                throw e;
            }
            // completing constructor argument (https://github.com/groovy/groovy-eclipse/issues/331)
        }

        // GRECLIPSE-1235: completion invocation offset is outside of type name and argument expressions; it is probably after opening paren or separating comma

        int offset = expression.getNameStart(), length = expression.getNameEnd() - offset + 1;
        String constructorText = constructorType.getNameWithoutPackage();
        if (constructorText.length() < length) {
            constructorText = constructorType.getName();
        }

        createContextForCallContext(expression, constructorType, constructorText);
    }

    @Override
    public void visitFieldExpression(FieldExpression expression) {
        if (check(expression)) {
            createContext(expression, blockStack.getLast(), expressionOrStatement());
        }
        super.visitFieldExpression(expression);
    }

    @Override
    public void visitGStringExpression(GStringExpression expression) {
        visitExpressions(expression.getValues());
        for (ConstantExpression stringExpr : expression.getStrings()) {
            if (check(stringExpr)) {
                // no completions available within string literals
                context = null;
                throw new VisitCompleteException();
            }
        }
    }

    @Override
    public void visitListExpression(ListExpression expression) {
        super.visitListExpression(expression);
        if (check(expression)) {
            // completion after a list expression: []._ or [10]._
            createContext(expression, declarationStack.getLast(), ContentAssistLocation.EXPRESSION);
        }
    }

    @Override
    public void visitMapExpression(MapExpression expression) {
        super.visitMapExpression(expression);
        if (check(expression)) {
            // if this map is part of the enclosing ArgumentListExpression,
            // then assume that we are completing on named arguments
            if (!isArgument(expression)) {
                // completion after a list expression: [:]._ or [x:10]._
                createContext(expression, declarationStack.getLast(), ContentAssistLocation.EXPRESSION);
            } else {
                // do method context
            }
        }
    }

    @Override
    public void visitMapEntryExpression(MapEntryExpression expression) {
        // Is the completion located within the RHS of a named argument pair?
        if (check(expression.getValueExpression()) && isArgument(expression)) {
            lhsNode = expression.getKeyExpression();
        }
        super.visitMapEntryExpression(expression);
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression expression) {
        if (!check(expression)) { super.visitMethodCallExpression(expression);
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

        // check for special case "(obj).\ndef var", which is seen by parser as "obj.def(var)"
        if (supportingNodeEnd > objectExpression.getEnd() && supportingNodeEnd < methodExpression.getStart()) {
            // GRECLIPSE-1374: probably a completion after a parenthesized expression
            createContext(objectExpression, blockStack.getLast(), ContentAssistLocation.EXPRESSION);
        }

        methodExpression.visit(this);

        // here do a check if we are inside of a method call, but we are not at
        // any particular expression (ie- this is the METHOD_CONTEXT)
        // there is a special case that when we are at the first character of
        // any expression in an argument list,
        // we want to do the context instead of normal completion.
        // do that check below
        visitArguments(arguments, expression);

        // if we get here, then we still want to do the context
        // we are either at a paren, at a comma, or at a start of an expression
        createContextForCallContext(expression, methodExpression,
            /*call.isImplicitThis() ? methodExpression : objectExpression,*/
            // this is not exactly right since it will fail on funky kinds of method calls, like those that are called by a GString
            methodExpression.getText());
    }

    @Override
    public void visitPropertyExpression(PropertyExpression expression) {
        if (!check(expression) && !check(expression.getProperty())) {
            return;
        }

        Expression objectExpression = expression.getObjectExpression();
        Expression propertyExpression = expression.getProperty();

        // check for the special case of groovy command expressions
        checkForCommandExpression(objectExpression, propertyExpression);

        // expression contains completion node or supporting node; test for loose match
        if (objectExpression.getEnd() > 0 && supportingNodeEnd > objectExpression.getEnd() && supportingNodeEnd < propertyExpression.getStart()) {
            // GRECLIPSE-1374: probably a completion after a parenthesized expression
            createContext(objectExpression, blockStack.getLast(), ContentAssistLocation.EXPRESSION);
        }

        super.visitPropertyExpression(expression);
    }

    @Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression expression) {
        if (!check(expression)) { super.visitStaticMethodCallExpression(expression);
            return;
        }

        // don't check here if the type reference is implicit
        // we know that the type is not implicit if the name
        // location is filled in.
        if (expression.getOwnerType().getNameEnd() == 0 && check(expression.getOwnerType())) {
            createContext(expression.getOwnerType(), blockStack.getLast(), expressionOrStatement());
        }
        visitArguments(expression.getArguments(), expression);

        // the method itself is not an expression, but only a string

        if (check(expression)) {
            createContext(expression, blockStack.getLast(), expressionOrStatement());
        }
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
        if (expression.getAccessedVariable() == expression) {
            ClassNode type = expression.getOriginType();
            if (check(type)) {
                createContext(type, blockStack.getLast(), expressionOrStatement());
            }
        }
        if (check(expression)) {
            ContentAssistLocation loc = ContentAssistLocation.EXPRESSION;
            if (blockStack.getLast() instanceof AnnotationNode) {
                loc = ContentAssistLocation.ANNOTATION_BODY;
            } else if (supportingNodeEnd == -1) {
                loc = ContentAssistLocation.STATEMENT;
            }
            createContext(expression, blockStack.getLast(), loc);
        }
        super.visitVariableExpression(expression);
    }

    //--------------------------------------------------------------------------

    private boolean check(ASTNode node) {
        if (node.getEnd() > 0) {
            boolean containsCompletionOffset = (completionOffset > node.getStart() && completionOffset <= node.getEnd());
            boolean containsSupportingOffset = (supportingNodeEnd > node.getStart() && supportingNodeEnd <= node.getEnd());

            return containsCompletionOffset || containsSupportingOffset;
        }
        return false;
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
            if ((supportingNodeEnd == -1 && lastArgEnd < completionOffset) || lastArgEnd <= supportingNodeEnd) {
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

    private void checkForCommandExpression(Expression leftExpression, Expression rightExpression) {
        // if all of these are true, then we have a command expression:
        // if objectExpr is a method call with >1 arguments
        // if property is a constant expression
        // if there are no parens (note that we don't actually have to test for
        // this since if there are parens, then the result would still be the same)

        Expression leftMost = getLeftMost(rightExpression);
        if (isMethodCallWithOneArgument(leftExpression) && leftMost instanceof ConstantExpression && check(leftMost)) {
            createContext(leftExpression, blockStack.getLast(), ContentAssistLocation.EXPRESSION);
        }
    }

    private void createContext(ASTNode completionNode, ASTNode containingNode, ContentAssistLocation location) {
        context = new ContentAssistContext(
            completionOffset,
            completionExpression,
            fullCompletionExpression,
            completionNode,
            containingNode,
            lhsNode,
            location,
            unit,
            declarationStack.getLast(),
            completionEnd);
        throw new VisitCompleteException();
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
            declarationStack.getLast(),
            completionEnd,
            methodExpr,
            methodName,
            methodExpr.getEnd());
        throw new VisitCompleteException();
    }

    private ContentAssistLocation expressionScriptOrStatement(MethodNode node) {
        return node.isScriptBody() ? expressionOrScript() : expressionOrStatement();
    }

    private ContentAssistLocation expressionOrScript() {
        return supportingNodeEnd == -1 ? ContentAssistLocation.SCRIPT : ContentAssistLocation.EXPRESSION;
    }

    private ContentAssistLocation expressionOrStatement() {
        return supportingNodeEnd == -1 ? ContentAssistLocation.STATEMENT : ContentAssistLocation.EXPRESSION;
    }

    private boolean isArgument(Expression expr) {
        if (argumentListStack.isEmpty()) {
            return false;
        }
        TupleExpression tuple = argumentListStack.getLast();
        return isArgument(expr, tuple.getExpressions());
    }

    private boolean isArgument(Expression expr, List<? extends Expression> args) {
        if (args != null && !args.isEmpty()) {
            for (Expression arg : args) {
                if (arg == expr) {
                    return true;
                }
                if (arg instanceof NamedArgumentListExpression) {
                    return isArgument(expr, ((NamedArgumentListExpression) arg).getMapEntryExpressions());
                }
            }
        }
        return false;
    }

    //--------------------------------------------------------------------------

    /**
     * finds end of the last argument of an argument list expression
     */
    private static int findLastArgumentEnd(Expression args) {
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

    private static Expression getLeftMost(Expression expr) {
        if (expr instanceof ConstantExpression) {
            return expr;
        } else if (expr instanceof PropertyExpression) {
            return getLeftMost(((PropertyExpression) expr).getObjectExpression());
        } else if (expr instanceof MethodCallExpression) {
            return getLeftMost(((MethodCallExpression) expr).getObjectExpression());
        } else if (expr instanceof BinaryExpression) {
            return getLeftMost(((BinaryExpression) expr).getLeftExpression());
        }
        return null;
    }

    private static Expression getRightMost(Expression expr) {
        if (expr instanceof VariableExpression || expr instanceof ConstantExpression) {
            return expr;
        } else if (expr instanceof PropertyExpression) {
            return getRightMost(((PropertyExpression) expr).getProperty());
        } else if (expr instanceof BinaryExpression) {
            return getRightMost(((BinaryExpression) expr).getRightExpression());
        }
        return null;
    }

    private static boolean isMethodCallWithOneArgument(Expression expr) {
        if (expr instanceof MethodCallExpression) {
            Expression arguments = ((MethodCallExpression) expr).getArguments();
            return arguments instanceof TupleExpression &&
                ((TupleExpression) arguments).getExpressions() != null &&
                !((TupleExpression) arguments).getExpressions().isEmpty();
        } else {
            return false;
        }
    }
}
