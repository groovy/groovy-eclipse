/*
 * Copyright 2009-2019 the original author or authors.
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
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
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
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
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
    private ASTNode lhsNode;

    private final LinkedList<ASTNode> blockStack = new LinkedList<>();

    private final LinkedList<AnnotatedNode> declarationStack = new LinkedList<>();

    /**
     * Tracks the current argument list expressions so that we can know if a
     * completion of a MapExpression is actually an argument and therefore
     * named parameters should be available.
     */
    private final LinkedList<TupleExpression> argumentListStack = new LinkedList<>();

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

        if (node.hasAnnotationDefault()) {
            // provide some context for the visitation of the annotation attribute default value expression
            lhsNode = new MemberValueExpression(node.getName(), ((ReturnStatement) node.getCode()).getExpression(), new AnnotationNode(node.getDeclaringClass()));
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
            if (completionOffset > Math.max(node.getStart(), node.getNameEnd() + 1) && completionOffset < body.getStart() && !node.isScriptBody()) {
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
        blockStack.add(node);
        super.visitParameter(node);
        blockStack.removeLast();

        // check parameter type
        if (check(node.getType()) || (check(node) && completionOffset < node.getNameStart())) {
            boolean isCatchParam = (blockStack.getLast() instanceof CatchStatement);
            createContext(node, blockStack.getLast(), isCatchParam ? ContentAssistLocation.EXCEPTIONS : ContentAssistLocation.PARAMETER);
        }

        // check parameter name
        if (node.getNameStart() <= completionOffset && completionOffset <= node.getNameEnd()) {
            boolean isCatchParam = (blockStack.getLast() instanceof CatchStatement &&
                (node.getStart() == node.getNameStart() || node.getName().equals("?")));
            createContext(node, blockStack.getLast(), isCatchParam ? ContentAssistLocation.EXCEPTIONS : ContentAssistLocation.PARAMETER);
        }

        // check for default value expression (that was moved during verification)
        if (check(node)) {
            createContext(node, blockStack.getLast(), expressionOrStatement());
        }
    }

    /**
     * Saves LHS node for possible initialization expression of field, parameter
     * or annotation attribute.
     * <p>
     * <b>Note</b>: DynamicVariable and VariableExpression are skipped by design.
     * @see #visitVariableExpression
     * @see #visitBinaryExpression
     */
    @Override
    protected void visitVariable(Variable var) {
        assert !(var instanceof DynamicVariable ||
                 var instanceof VariableExpression);

        if (var instanceof ASTNode) {
            lhsNode = (ASTNode) var;
        }
        super.visitVariable(var);
        lhsNode = null;
    }

    /**
     * Visits method/constructor call arguments, but only if we are not at the
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
            // outer receiver is irrelevant within argument list
            final ASTNode lhs = lhsNode; lhsNode = null;

            // check the arguments; ignores in-between locations
            args.visit(this);

            lhsNode = lhs;
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
            // if we get here, then we know that we are in this block statement, but not inside any expression
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
        if (Types.ofType(expression.getOperation().getType(), Types.ASSIGNMENT_OPERATOR)) {
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

        // sometimes the code block does not end at the closing '}', but at the
        // end of the last statement the closure itself ends at the last '}'
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
        if (completionOffset > expression.getStart() && completionOffset <= expression.getEnd()) {
            if (isStringLiteral(expression)) throw new VisitCompleteException();
            createContext(expression, blockStack.getLast(), expressionOrStatement());
        }
        super.visitConstantExpression(expression);
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression expression) {
        if (!check(expression)) {
            return;
        }

        visitAnnotations(expression.getAnnotations());
        ClassNode constructorType = expression.getType();

        if (completionOffset < expression.getNameStart()) {
            createContext(expression, blockStack.getLast(), expressionOrStatement());
        }
        if (completionOffset >= expression.getNameStart() && completionOffset <= expression.getNameEnd() + 1) {
            createContext(constructorType, blockStack.getLast(), ContentAssistLocation.CONSTRUCTOR);
        }
        // TODO: Does name range include type parameters?

        if (expression.isSpecialCall()) {
            AnnotatedNode enclosing = declarationStack.getLast();
            constructorType = enclosing.getDeclaringClass(); // "this" type
            if (expression.isSuperCall())
                constructorType = constructorType.getUnresolvedSuperClass(false);
        } else if (expression.isUsingAnonymousInnerClass()) {
            constructorType = expression.getType().getUnresolvedSuperClass(false);
            if (constructorType == ClassHelper.OBJECT_TYPE)
                constructorType = expression.getType().getUnresolvedInterfaces(false)[0];

            visitClass(expression.getType()); // see https://github.com/groovy/groovy-eclipse/issues/395
        }

        Expression arguments = expression.getArguments();
        checkForAfterClosingParen(expression, arguments);

        visitArguments(arguments, expression);

        // at a paren, at a comma, or at the start of an argument expression; do constructor context
        createContextForCallContext(expression, constructorType, constructorType.getNameWithoutPackage(), expression.getNameEnd() + 1);
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

        // Could be a groovy 1.8 style command expression
        checkForCommandExpression(objectExpression, methodExpression);

        Expression arguments = expression.getArguments();
        checkForAfterClosingParen(methodExpression, arguments);

        objectExpression.visit(this);

        if (completionOffset > objectExpression.getEnd() && completionOffset < methodExpression.getStart()) {
            // probably a completion after dot in 'foo.\nbar()' or 'foo.\n"$bar"()' or '(foo).\ndef bar'
            createContext(objectExpression, blockStack.getLast(), ContentAssistLocation.EXPRESSION);
        }

        methodExpression.visit(this);

        visitArguments(arguments, expression);
        // at a paren, at a comma, or at the start of an argument expression; do method context
        createContextForCallContext(expression, methodExpression, methodExpression.getText());
    }

    @Override
    public void visitPropertyExpression(PropertyExpression expression) {
        if (!check(expression)) {
            return;
        }

        Expression objectExpression = expression.getObjectExpression();
        Expression propertyExpression = expression.getProperty();

        // check for the special case of groovy command expressions
        checkForCommandExpression(objectExpression, propertyExpression);

        if (completionOffset > objectExpression.getEnd() && completionOffset < propertyExpression.getStart()) {
            // probably a completion after dot in 'foo.\nbar' or 'foo.\n"bar"' or 'foo.\n"$bar"', etc.
            createContext(objectExpression, blockStack.getLast(), ContentAssistLocation.EXPRESSION);
        }

        super.visitPropertyExpression(expression);

        createContext(expression, blockStack.getLast(), ContentAssistLocation.EXPRESSION);
    }

    @Override
    public void visitRangeExpression(RangeExpression expression) {
        super.visitRangeExpression(expression);

        if (completionOffset > expression.getStart() && completionOffset <= expression.getEnd()) {
            if (completionOffset <= expression.getFrom().getEnd() || completionOffset >= expression.getTo().getStart()) {
                createContext(expression, blockStack.getLast(), ContentAssistLocation.STATEMENT);
            }
            // no completions within the operator
            throw new VisitCompleteException();
        }
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
        Expression methodName = new ConstantExpression(expression.getMethod());
        methodName.setStart(expression.getNameStart());
        methodName.setEnd(expression.getNameEnd());
        if (check(methodName)) {
            createContext(expression, blockStack.getLast(), expressionOrStatement());
        }
        createContextForCallContext(expression, expression, expression.getMethod(), expression.getNameEnd());
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
        visitAnnotations(expression.getAnnotations());
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
        visitExpression(expression);
        // no call to super.visitVariableExpression
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

    private void createContextForCallContext(Expression expression, AnnotatedNode methodExpr, String methodName) {
        createContextForCallContext(expression, methodExpr, methodName, methodExpr.getEnd());
    }

    private void createContextForCallContext(Expression expression, AnnotatedNode methodExpr, String methodName, int methodNameEnd) {
        context = new MethodInfoContentAssistContext(
            completionOffset,
            completionExpression,
            fullCompletionExpression,
            expression,
            blockStack.getLast(),
            lhsNode,
            unit,
            declarationStack.getLast(),
            completionEnd,
            methodExpr,
            methodName,
            methodNameEnd);
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

    private static boolean isStringLiteral(ConstantExpression expr) {
        if (ClassHelper.STRING_TYPE.equals(expr.getType())) {
            return (expr.getLength() > expr.getText().length());
        }
        return false;
    }
}
