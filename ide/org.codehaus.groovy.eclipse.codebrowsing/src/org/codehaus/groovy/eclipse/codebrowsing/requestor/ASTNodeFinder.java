/*
 * Copyright 2009-2016 the original author or authors.
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

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ImportNodeCompatibilityWrapper;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
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
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.eclipse.core.util.ArrayUtils;
import org.codehaus.groovy.eclipse.core.util.VisitCompleteException;
import org.codehaus.groovy.runtime.GeneratedClosure;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;

public class ASTNodeFinder extends ClassCodeVisitorSupport {

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
        try {
            visitPackage(node.getPackage());
            visitImports(node);
            for (ClassNode clazz : node.getClasses()) {
                visitClass(clazz);
            }
        } catch (VisitCompleteException done) {
        }
        return result;
    }

    @Override
    public void visitPackage(PackageNode node) {
        if (node != null) {
            visitAnnotations(node);
            check(node);
        }
    }

    @Override
    public void visitImports(ModuleNode node) {
        for (ImportNode importNode : new ImportNodeCompatibilityWrapper(node).getAllImportNodes()) {
            visitImport(importNode);
        }
    }

    protected void visitImport(ImportNode node) {
        if (node.getType() != null) {
            visitAnnotations(node);
            check(node.getType());
            if (node.getFieldNameExpr() != null) {
                check(node.getFieldNameExpr());
            }
            if (node.getAliasExpr() != null) {
                check(node.getAliasExpr());
            }
        }
        check(node); // for package qualifier
    }

    @Override
    public void visitClass(ClassNode node) {
        visitAnnotations(node);
        if (node.getNameEnd() > 0) {
            checkNameRange(node); // also checks generics

            // supers can only appear after the name
            if (sloc.getEnd() > node.getNameEnd()) {
                // set offset beyond the class name and any generics
                GenericsType type = ArrayUtils.lastElement(node.getGenericsTypes());
                int offset = (type != null ? type.getEnd() : node.getNameEnd()) + 1;
                String source = readClassDeclaration(node).substring(offset - node.getStart());

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
                if (superTypes != null && superTypes.length > 0) {
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
                                    while ((c = source.charAt(a)) == ',' ||
                                            Character.isWhitespace(c)) a += 1;
                                    break;
                                }
                            }
                            for (int j = i + 1; j < n; j += 1) {
                                if (superTypes[j].getStart() > 0) {
                                    b = (superTypes[j].getStart() - 1) - offset;
                                    while ((c = source.charAt(b - 1)) == ',' ||
                                            Character.isWhitespace(c)) b -= 1;
                                    break;
                                }
                            }

                            check(superTypes[i], offset + a, offset + b);
                        }
                    }
                }
            }
        }

        if (node.getObjectInitializerStatements() != null) {
            for (Statement statement : node.getObjectInitializerStatements()) {
                statement.visit(this);
            }
        }

        // visit <clinit> body because this is where static field initializers are placed
        // However, there is a problem in that constant fields are seen here as well.
        // If a match is found here, keep it for later because there may be a more appropriate match in the class body
        VisitCompleteException vce = null;
        try {
            MethodNode clinit = node.getMethod("<clinit>", Parameter.EMPTY_ARRAY);
            if (clinit != null && clinit.getCode() instanceof BlockStatement) {
                for (Statement statement : ((BlockStatement) clinit.getCode()).getStatements()) {
                    statement.visit(this);
                }
            }
        } catch (VisitCompleteException e) {
            vce = e;
        }

        node.visitContents(this);

        // visit inner classes
        Iterator<InnerClassNode> innerClasses = node.getInnerClasses();
        if (innerClasses != null) {
            while (innerClasses.hasNext()) {
                ClassNode inner = innerClasses.next();
                // do not look into closure classes.  A closure class
                // looks like ParentClass$_name_closure#, where
                // ParentClass is the name of the containing class.
                // name is a name for the closure, and # is a number
                if (!inner.isSynthetic() || inner instanceof GeneratedClosure) {
                    visitClass(inner);
                }
            }
        }

        // if we have gotten here, then we have not found a more appropriate candidate
        if (vce != null) {
            throw vce;
        }
    }

    @Override
    public void visitField(FieldNode node) {
//        if (node.getName().contains("$")) {
//            // synthetic field, probably 'this$0' for an inner class reference to the outer class
//            return;
//        }
        if (node.getNameEnd() > 0) {
            checkNameRange(node);
        }
        // visit annotations and init expression
        super.visitField(node);
        // visit type and generics
        check(node.getType(), node.getStart(), node.getEnd() - node.getName().length());
    }

    @Override
    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        if (node.getEnd() > 0) {
            ClassNode returnType = node.getReturnType();
            if (returnType != null /*&& !returnType.isPrimitive()*/) { // allow primitives to be found to stop the visit
                int n, offset = node.getStart();

                // constrain the return type's start offset using generics or annotations
                ASTNode last = ArrayUtils.lastElement(node.getGenericsTypes());
                if (last != null) {
                    offset = last.getEnd() + 1;
                } else if ((n = node.getAnnotations().size()) > 0) {
                    last = GroovyUtils.lastElement(node.getAnnotations().get(n - 1));
                    offset = last.getEnd() + 1;
                } else if (returnType.getEnd() < 1) {
                    // TODO: select on modifiers shows as return type
                }

                check(returnType, offset, node.getNameStart() - 1);
            }

            if (node.getNameEnd() > 0) {
                checkNameRange(node);
            }

            checkParameters(node.getParameters());

            if (node.getExceptions() != null) {
                for (ClassNode e : node.getExceptions()) {
                    check(e);
                }
            }
        }
        // visit annotations, param annotations, and statements
        super.visitConstructorOrMethod(node, isConstructor);
    }

    @Override
    protected void visitAnnotation(AnnotationNode node) {
        check(node.getClassNode());
        int start = node.getEnd() + 2;
        for (Map.Entry<String, Expression> pair : node.getMembers().entrySet()) {
            String name = pair.getKey(); Expression expr = pair.getValue();
            check(node.getClassNode().getMethod(name, Parameter.EMPTY_ARRAY),
                start/*expr.getStart() - name.length() - 1*/, expr.getStart() - 1);
//            expr.visit(this);
            start = expr.getEnd() + 1;
        }
        super.visitAnnotation(node);
    }

    @Override
    public void visitArrayExpression(ArrayExpression expression) {
        ClassNode arrayClass = expression.getElementType();
        if (arrayClass != arrayClass.redirect()) {
            check(arrayClass);
        } else {
            // this is a synthetic ArrayExpression used for when
            // referencing enum fields
        }
        super.visitArrayExpression(expression);
    }

    @Override
    public void visitBinaryExpression(BinaryExpression expression) {
        super.visitBinaryExpression(expression);
        check(expression);
    }

    @Override
    public void visitCastExpression(CastExpression expression) {
        check(expression.getType());
        super.visitCastExpression(expression);
    }

    @Override
    public void visitClassExpression(ClassExpression expression) {
        // NOTE: expression.getType() may refer to ClassNode behind "this" or "super", so it may cast a very wide net
        if (expression.getEnd() > 0 && expression.getStart() == expression.getType().getStart()) {
            check(expression.getType());
        } else {
            check(expression);
        }
        super.visitClassExpression(expression);
    }

    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        checkParameters(expression.getParameters());
        super.visitClosureExpression(expression);
    }

    @Override
    public void visitFieldExpression(FieldExpression expression) {
        check(expression);
        super.visitFieldExpression(expression);
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
        // check the annotations, generics, and type of variable declarations -- including @Lazy fields
        if (expression == expression.getAccessedVariable() || expression.getName().charAt(0) == '$') {
            visitAnnotations(expression);

            // expression start and end bound the variable name; guestimate the type positions
            int until = expression.getStart() - 1, // assume at least one space on either side
                start = Math.max(0, until - expression.getOriginType().getName().length() - 1);

            check(expression.getOriginType(), start, until);
        }
        check(expression);
    }

    @Override
    public void visitConstantExpression(ConstantExpression expression) {
        if (expression == ConstantExpression.NULL) {
            // the sloc of this global variable is inexplicably set to something
            // so, we may erroneously find matches here
            return;
        }
        if (expression.getText().length() == 0 && expression.getLength() != 0) {
            // GRECLIPSE-1330 This is probably an empty expression in a gstring...can ignore.
            return;
        }
        if (expression instanceof AnnotationConstantExpression) {
            // example: @interface X { Y default @Y(...) }; expression is "@Y(...)"
            // example: @X(@Y(...)); expression is "@Y(...)"
            check(expression.getType());
            // values have been visited
        }
        check(expression);
        super.visitConstantExpression(expression);
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        if (call.getEnd() > 0) {
            int start, until;

            if (call.getNameStart() > 0) {
                checkNameRange(call);
                checkGenerics(call.getType());

                start = call.getStart();
                until = call.getNameStart() - 1;
            } else try {
                start = call.getStart() + "new ".length();
                until = call.getArguments().getStart() - 1;

                // check call name and generics
                check(call.getType(), start, until);

                until = start;
                start = call.getStart();
            } catch (VisitCompleteException e) {
                result = call;
                throw e;
            }

            // check the new keyword
            check(null, start, until);
        }

        // visit argument list
        super.visitConstructorCallExpression(call);
        // visit anonymous body
        if (call.isUsingAnonymousInnerClass()) {
            call.getType().visitContents(this);
        }
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

        // the method itself is not an expression, but only a string
        // so this check call will test for open declaration on the method
        check(call);
    }

    @Override
    public void visitCatchStatement(CatchStatement statement) {
        checkParameter(statement.getVariable());
        super.visitCatchStatement(statement);
    }

    @Override
    public void visitForLoop(ForStatement statement) {
        checkParameter(statement.getVariable());
        super.visitForLoop(statement);
    }

    //--------------------------------------------------------------------------

    /**
     * Checks if the node covers the selection.
     */
    protected void check(ASTNode node) {
        //if (node == null) return;
        if (node instanceof ClassNode) {
            checkGenerics((ClassNode) node);
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
                node.getNameStart(), node.getNameEnd() - node.getNameStart()));
        }
        if (node instanceof ClassNode) {
            checkGenerics((ClassNode) node);
        }
    }

    private void checkGenerics(ClassNode node) {
        if (node.isUsingGenerics() && node.getGenericsTypes() != null) {
            for (GenericsType generics : node.getGenericsTypes()) {
                int start = generics.getStart(),
                    until = start + generics.getName().length();

                if (generics.getType() != null && generics.getType().getName().charAt(0) != '?') {
                    check(generics.getType(), start, until);
                }

                start = until + 1;
                until = generics.getEnd();

                if (generics.getLowerBound() != null) {
                    start += "super ".length(); // assume 1 space
                    check(generics.getLowerBound(), start, until);
                } else if (generics.getUpperBounds() != null) {
                    start += "extends ".length(); // assume 1 space
                    for (ClassNode upper : generics.getUpperBounds()) {
                        String name = upper.getName();
                        // handle enums where the upper bound is the same as the type
                        if (!name.equals(node.getName())) {
                            check(upper, start, Math.min(start + name.length(), until));
                            if (upper.getEnd() > 0)
                                start = upper.getEnd() + 1;
                        }
                    }
                }
            }
        }
    }

    private void checkParameter(Parameter param) {
        if (param != null && param.getEnd() > 0) {
            checkNameRange(param);
            if (param.getInitialExpression() != null) {
                param.getInitialExpression().visit(this);
            }
            check(param.getType(), param.getStart(), param.getEnd());
            //check(param); // what's left?
        }
    }

    private void checkParameters(Parameter[] params) {
        if (params != null) {
           for (Parameter p : params) {
               checkParameter(p);
           }
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
        String code = (String) node.getNodeMetaData("groovy.source");
        if (code == null) {
            code = String.valueOf(GroovyUtils.readSourceRange(module.getContext(), node.getStart(), node.getLength()));
            node.setNodeMetaData("groovy.source", code);
        }
        return code.substring(0, code.indexOf('{'));
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
