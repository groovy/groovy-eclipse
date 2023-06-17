/*
 * Copyright 2009-2023 the original author or authors.
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
package org.codehaus.groovy.eclipse.codebrowsing.requestor;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.util.VisitCompleteException;
import org.eclipse.jdt.groovy.core.util.ArrayUtils;
import org.eclipse.jdt.groovy.core.util.DepthFirstVisitor;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;

public class ASTNodeFinder extends DepthFirstVisitor {

    protected ModuleNode module;
    protected ASTNode result;
    protected Region sloc;

    public ASTNodeFinder(Region sloc) {
        this.sloc = sloc;
    }

    /**
     * The main entry point.
     */
    public ASTNode doVisit(ModuleNode node) {
        module = node;
        result = null;
        try {
            visitModule(node);
        } catch (VisitCompleteException e) {
            // finished
        }
        return result;
    }

    @Override
    public void visitPackage(PackageNode node) {
        super.visitPackage(node);
        check(node);
    }

    @Override
    public void visitImport(ImportNode node) {
        if (node.getType() != null) {
            check(node.getType());
        }
        super.visitImport(node);
        check(node);
    }

    @Override
    public void visitClass(ClassNode node) {
        if (node.getNameEnd() > 0) {
            checkNameRange(node); // also checks generics
            checkHeader(node); // extends, implements and permits
        }

        super.visitClass(node);
    }

    @Override
    public void visitField(FieldNode node) {
        if (node.getNameEnd() > 0) {
            checkNameRange(node);
        }

        // visit annotations and init expression
        super.visitField(node);

        // compute supporting type start position
        int start = node.getStart(),
            annos = node.getAnnotations().size();
        if (annos > 0)
            start = GroovyUtils.lastElement(node.getAnnotations().get(annos - 1)).getEnd() + 1;

        // don't check enum constant declarations, which have an implicit type (not visible in the source)
        if (start > 0 && !node.isEnum()) {
            // visit type and generics
            check(node.getType(), start, node.getEnd() - node.getName().length());
        }
    }

    @Override
    public void visitMethod(MethodNode node) {
        if (node == runMethod) return;

        if (node.getEnd() > 0) {
            if (!(node instanceof ConstructorNode) && isNotEmpty(node.getGenericsTypes())) {
                checkGenerics(node.getGenericsTypes());
            }

            ClassNode returnType = node.getReturnType();
            if (returnType != null /*&& !returnType.isPrimitive()*/) { // allow primitives to be found to stop the visit
                int offset = -1;

                if (returnType.getEnd() < 1) {
                    // constrain the return type's start offset using generics or annotations
                    ASTNode last = ArrayUtils.lastElement(node.getGenericsTypes());
                    if (last != null) {
                        offset = last.getEnd() + 1;
                    } else if (!node.getAnnotations().isEmpty()) {
                        for (int i = (node.getAnnotations().size() - 1); i >= 0; i -= 1) {
                            // find the rightmost annotation with end source position info
                            int end = GroovyUtils.lastElement(node.getAnnotations().get(i)).getEnd() + 1;
                            if (end > 0) {
                                offset = end;
                                break;
                            }
                        }
                    }
                    // TODO: if offset is still -1, select on modifiers shows as return type
                }

                check(returnType, Math.max(offset, node.getStart()), node.getNameStart() - 1);
            }

            if (node.getNameEnd() > 0) {
                checkNameRange(node);
            }

            checkTypes(node.getExceptions());
        }

        // visit annotations, parameters, and statements
        super.visitMethod(node);
    }

    //

    @Override
    public void visitArrayExpression(ArrayExpression expression) {
        if (expression.getEnd() > 0) {
            check(GroovyUtils.getBaseType(expression.getType()), expression.getNameStart(), expression.getNameEnd() + 1);
        }
        super.visitArrayExpression(expression);
    }

    @Override
    public void visitCastExpression(CastExpression expression) {
        if (expression.getEnd() > 0) {
            check(expression.getType(), expression.getNameStart(), expression.getNameEnd());
        }
        super.visitCastExpression(expression);
    }

    @Override
    public void visitClassExpression(ClassExpression expression) {
        // NOTE: expression.getType() may refer to ClassNode behind "this" or "super", so it may cast a very wide net
        if (expression.getEnd() > 0 && expression.getStart() == expression.getType().getStart()) {
            check(expression.getType());
        }
        super.visitClassExpression(expression);
    }

    @Override
    public void visitConstantExpression(ConstantExpression expression) {
        if (expression instanceof AnnotationConstantExpression) {
            // example: @interface X { Y default @Y(...) }; expression is "@Y(...)"
            // example: @X(@Y(...)); expression is "@Y(...)"
            check(expression.getType());
            // values have been visited
        }
        super.visitConstantExpression(expression);
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
        // check the annotations, generics, and type of variable declarations (including @Lazy fields)
        if (expression == expression.getAccessedVariable() || expression.getName().charAt(0) == '$') {
            // expression start and end bound the variable name; guestimate the type positions
            int until = expression.getStart() - 1, // assume at least one space on either side
                start = Math.max(0, until - expression.getOriginType().getName().length() - 1);

            if (until > 0) check(expression.getOriginType(), start, until);
        }
        super.visitVariableExpression(expression);
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        if (expression.isMultipleAssignmentDeclaration() && // check before "(...)"
                expression.getStart() < expression.getTupleExpression().getStart()) {
            visitAnnotations(expression.getAnnotations()); // possibly interleaved with "def", "final", ...
            check(expression.getType(), expression.getStart(), expression.getTupleExpression().getStart());
        }
        super.visitDeclarationExpression(expression);
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        if (call.getEnd() > 0) {
            if (call.getNameStart() > 0) {
                if (call.isUsingAnonymousInnerClass()) {
                    check(call.getType().getUnresolvedSuperClass(false));
                    checkTypes(call.getType().getUnresolvedInterfaces(false));
                } else {
                    checkNameRange(call);
                    checkGenerics(call.getType());
                }
            } else {
                try {
                    int start = call.getStart() + "new ".length();
                    int until = call.getArguments().getStart() - 1;

                    // check call name and generics
                    check(call.getType(), start, until);
                } catch (VisitCompleteException e) {
                    result = call;
                    throw e;
                }
            }
            // in case of @Newify, "new" keyword is not present
            if (call.getStart() == call.getType().getStart() && !call.isUsingAnonymousInnerClass()) {
                check(call.getType());
            }
        }
        // visit argument list and anonymous body
        super.visitConstructorCallExpression(call);
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        if (call.isUsingGenerics()) {
            checkGenerics(call.getGenericsTypes());
        }
        super.visitMethodCallExpression(call);
    }

    @Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
        // don't check here if the type reference is implicit
        // we know that the type is not implicit if the name
        // location is filled in.
        if (call.getOwnerType() != call.getOwnerType().redirect()) {
            check(call.getOwnerType());
        }
        super.visitStaticMethodCallExpression(call);
    }

    @Override
    public void visitArgumentlistExpression(ArgumentListExpression expression) {
        visitAnnotations(expression.getAnnotations());

        Iterator<Expression> arguments = expression.iterator();
        if (arguments.hasNext()) {
            Expression first = arguments.next();
            // named and positional arguments may be interleaved, so visit named arguments expression last
            if (!(first instanceof MapExpression) || first.getEnd() != expression.getEnd()) {
                first.visit(this);
                first = null;
            }
            while (arguments.hasNext()) {
                arguments.next().visit(this);
            }
            if (first != null) {
                first.visit(this);
            }
        }

        visitExpression(expression);
    }

    @Override
    protected void visitAnnotation(AnnotationNode annotation) {
        if (sloc.regionIsCoveredByNode(annotation)) {
            check(annotation.getClassNode());

            int start = annotation.getClassNode().getEnd() + 1;
            for (Map.Entry<String, Expression> pair : annotation.getMembers().entrySet()) {
                String name = pair.getKey();
                Expression expr = pair.getValue();
                check(GroovyUtils.getAnnotationMethod(annotation, name),
                    start/*expr.getStart() - name.length() - 1*/, expr.getStart() - 1);
                /*expr.visit(this);*/
                start = expr.getEnd() + 1;
            }

            super.visitAnnotation(annotation);
        }
    }

    @Override
    protected void visitExpression(Expression expression) {
        super.visitExpression(expression);
        check(expression);
    }

    @Override
    protected void visitParameter(Parameter parameter) {
        super.visitParameter(parameter);
        checkParameter(parameter);
    }

    @Override
    protected void visitStatement(Statement statement) {
        super.visitStatement(statement);
        if (!(statement instanceof BlockStatement) &&
                !(statement instanceof ExpressionStatement)) {
            check(statement);
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Checks if the node covers the selection.
     */
    protected void check(ASTNode node) {
        if (node instanceof ClassNode) {
            ClassNode type = (ClassNode) node;
            if (type.isArray()) {
                check(type.getComponentType(), node.getStart(), node.getEnd() - 2);
            } else {
                checkGenerics(type);
            }
        }
        if (node.getEnd() > 0 && sloc.regionIsCoveredByNode(node)) {
            completeVisitation(node, null);
        }
    }

    /**
     * Checks if the node covers the selection.  If source location isn't set
     * for specified node, the supplied offsets will constrain it's location.
     */
    protected void check(ASTNode node, int start, int until) {
        if (node != null && node.getEnd() > 0) {
            check(node);
        } else {
            if (node instanceof ClassNode) {
                checkGenerics((ClassNode) node);
            }
            if (sloc.getOffset() >= start && sloc.getEnd() <= until) {
                completeVisitation(node, new Region(start, until - start));
            }
        }
    }

    /**
     * Checks if the name of the node covers the selection.
     */
    protected void checkNameRange(AnnotatedNode node) {
        if (sloc.regionIsCoveredByNameRange(node)) {
            completeVisitation(node, new Region(
                node.getNameStart(), (node.getNameEnd() + 1) - node.getNameStart()));
        }
        if (node instanceof ClassNode) {
            checkGenerics((ClassNode) node);
        }
    }

    /**
     * Checks if the extends, implements or permits clause covers the selection.
     */
    private void checkHeader(ClassNode node) {
        // supers can only appear after the name
        if (!(sloc.getOffset() > node.getNameEnd())) {
            return;
        }
        // anonymous inner classes cannot have extends or implements clauses
        if (GroovyUtils.isAnonymous(node)) {
            return;
        }
        // set offset beyond the class name and any generics
        GenericsType type = ArrayUtils.lastElement(node.getGenericsTypes());
        int offset = (type != null ? type.getEnd() : node.getNameEnd() + 1);
        String source, src = null;
        try {
            src = readClassDeclaration(node);
            source = src.substring(offset - node.getStart());
        } catch (Exception err) {
            GroovyCore.logException(String.format(
                "Error checking super-types at offset %d in file / index %d of:%n%s%n%s", offset, offset - node.getStart(), src, node), err);
            return;
        }

        ClassNode superClass = node.getUnresolvedSuperClass();
        if (superClass != null) {
            // only check types that appear in the source code
            String name = GroovyUtils.splitName(superClass)[1];
            if (source.indexOf(name) > 0) {
                int a = endIndexOf(source, EXTENDS_),
                    b = indexOf(source, _IMPLEMENTS);
                if (b < 0) b = source.length();
                check(superClass, offset + a, offset + b);
            }
        }

        ClassNode[] superTypes = node.getUnresolvedInterfaces();
        if (isNotEmpty(superTypes)) {
            for (int i = 0, n = superTypes.length; i < n; i += 1) {
                // only check types that appear in the source code
                String name = GroovyUtils.splitName(superTypes[i])[1];
                if (source.indexOf(name) > 0) {
                    int a = endIndexOf(source, IMPLEMENTS_),
                        b = source.length(); // TODO: Set to source.indexOf(name) + name.length()?
                    char c;
                    // use prev and next to further constrain
                    for (int j = i - 1; j >= 0; j -= 1) {
                        if (superTypes[j].getEnd() > 0) {
                            a = (superTypes[j].getEnd()) - offset;
                            while ((c = source.charAt(a)) == ',' || Character.isWhitespace(c)) {
                                a += 1;
                            }
                            break;
                        }
                    }
                    for (int j = i + 1; j < n; j += 1) {
                        if (superTypes[j].getStart() > 0) {
                            b = (superTypes[j].getStart() - 1) - offset;
                            while ((c = source.charAt(b - 1)) == ',' || Character.isWhitespace(c)) {
                                b -= 1;
                            }
                            break;
                        }
                    }

                    check(superTypes[i], offset + a, offset + b);
                }
            }
        }

        List<ClassNode> subTypes = node.getPermittedSubclasses();
        for (int i = 0, n = subTypes.size(); i < n; i += 1) {
            // only check types that appear in the source code
            String name = GroovyUtils.splitName(subTypes.get(i))[1];
            if (source.indexOf(name) > 0) {
                int a = endIndexOf(source, Pattern.compile("\\bpermits\\s+")),
                    b = source.length(); // TODO: Set to source.indexOf(name) + name.length()?
                char c;
                // use prev and next to further constrain
                for (int j = i - 1; j >= 0; j -= 1) {
                    if (subTypes.get(j).getEnd() > 0) {
                        a = (subTypes.get(j).getEnd()) - offset;
                        while ((c = source.charAt(a)) == ',' || Character.isWhitespace(c)) {
                            a += 1;
                        }
                        break;
                    }
                }
                for (int j = i + 1; j < n; j += 1) {
                    if (subTypes.get(j).getStart() > 0) {
                        b = (subTypes.get(j).getStart() - 1) - offset;
                        while ((c = source.charAt(b - 1)) == ',' || Character.isWhitespace(c)) {
                            b -= 1;
                        }
                        break;
                    }
                }

                check(subTypes.get(i), offset + a, offset + b);
            }
        }
    }

    private void checkTypes(ClassNode[] nodes) {
        if (isNotEmpty(nodes)) {
            for (ClassNode node : nodes) {
                check(node);
            }
        }
    }

    private void checkGenerics(ClassNode node) {
        if (isNotEmpty(node.getGenericsTypes()) && !node.isEnum()) {
            checkGenerics(node.getGenericsTypes());
        }
    }

    private void checkGenerics(GenericsType[] generics) {
        for (GenericsType generic : generics) {
            int start = generic.getStart(),
                until = start + generic.getName().length();

            if (generic.getType() != null && generic.getType().getName().charAt(0) != '?') {
                check(generic.getType(), start, until);
            }

            start = until + 1;
            until = generic.getEnd();

            if (generic.getLowerBound() != null) {
                start += "super ".length(); // assume 1 space
                check(generic.getLowerBound(), start, until);
            } else if (generic.getUpperBounds() != null) {
                start += "extends ".length(); // assume 1 space
                for (ClassNode upper : generic.getUpperBounds()) {
                    String name = upper.getName();
                    check(upper, start, Math.min(start + name.length(), until));
                    if (upper.getEnd() > 0)
                        start = upper.getEnd() + 1;
                }
            }
        }
    }

    private void checkParameter(Parameter param) {
        if (param != null && param.getEnd() > 0) {
            checkNameRange(param);
            check(param.getOriginType(), param.getStart(), param.getNameStart() - 1);
        }
    }

    /**
     * Provides a single exit point for the various check methods.
     */
    protected final void completeVisitation(ASTNode node, Region sloc) throws VisitCompleteException {
        result = node;
        if (sloc != null)
            this.sloc = sloc;
        throw new VisitCompleteException();
    }

    //--------------------------------------------------------------------------

    private String readClassDeclaration(ClassNode node) {
        String code = node.getNodeMetaData("groovy.source", x -> String.valueOf(module.getContext().readSourceRange(node.getStart(), node.getLength())));
        return code.substring(0, code.indexOf('{', node.getNameEnd() - node.getStart()));
    }

    private static int endIndexOf(String s, Pattern p) {
        Matcher m = p.matcher(s);
        if (m.find()) {
            return m.start() + m.group().length();
        }
        return -1;
    }

    private static int indexOf(String s, Pattern p) {
        Matcher m = p.matcher(s);
        if (m.find()) {
            return m.start();
        }
        return -1;
    }

    private static final Pattern EXTENDS_ = Pattern.compile("\\bextends\\s+");
    private static final Pattern IMPLEMENTS_ = Pattern.compile("\\bimplements\\s+");
    private static final Pattern _IMPLEMENTS = Pattern.compile("\\s+implements\\b");
}
