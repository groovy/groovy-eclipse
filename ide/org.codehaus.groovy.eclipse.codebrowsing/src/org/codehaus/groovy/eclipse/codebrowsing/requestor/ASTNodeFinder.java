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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ImportNodeCompatibilityWrapper;
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
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.eclipse.core.util.VisitCompleteException;
import org.codehaus.groovy.runtime.GeneratedClosure;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;

public class ASTNodeFinder extends ClassCodeVisitorSupport {

    protected ASTNode nodeFound;
    private Region r;

    public ASTNodeFinder(Region r) {
        this.r = r;
    }

    public Region getRegion() {
        return r;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return null;
    }

    @Override
    public void visitReturnStatement(ReturnStatement ret) {
        // special case: AnnotationConstantExpressions do not visit their type.
        // this means that annotations in default expressions are not visited.
        // check that here
        if (ret.getExpression() instanceof AnnotationConstantExpression) {
            check(((AnnotationConstantExpression) ret.getExpression()).getType());
        }

        super.visitReturnStatement(ret);
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
        // VariableExpressions not an AnnotatedNode in groovy 1.6, but they are in 1.7+
        Object maybeAnnotatedNode = expression;
        if (maybeAnnotatedNode instanceof AnnotatedNode) {
            visitAnnotations((AnnotatedNode) maybeAnnotatedNode);
        }
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
        // don't do this stuff for implicit methods
        if (node.getEnd() > 0) {
            ClassNode expression = node.getReturnType();
            if (expression != null) {
                visitClassReference(expression);
            }
            if (node.getExceptions() != null) {
                for (ClassNode e : node.getExceptions()) {
                    visitClassReference(e);
                }
            }
            checkParameters(node.getParameters());
        }

        super.visitConstructorOrMethod(node, isConstructor);

        // maybe selecting the method name itself
        if (node.getNameEnd() > 0) {
            checkNameRange(node);
        }
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
        if (p != null && p.getEnd() > 0) {
            check(p.getType());
           if (p.getInitialExpression() != null) {
               p.getInitialExpression().visit(this);
           }
           check(p);
        }
    }

    @Override
    public void visitField(FieldNode node) {
        if (node.getName().contains("$")) {
            // synthetic field, probably 'this$0' for an inner class reference to the outer class
            return;
        }
        visitClassReference(node.getType());
        super.visitField(node);
        // maybe selecting the field name itself
        if (node.getNameEnd() > 0) {
            checkNameRange(node);
        }
    }

    @Override
    public void visitCastExpression(CastExpression node) {
        check(node.getType());
        super.visitCastExpression(node);
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
        check(expression);
        super.visitConstantExpression(expression);
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        // DeclarationExpressions not an AnnotatedNode in groovy 1.6, but they are in 1.7+
        Object maybeAnnotatedNode = expression;
        if (maybeAnnotatedNode instanceof AnnotatedNode) {
            visitAnnotations((AnnotatedNode) maybeAnnotatedNode);
        }
        check(expression.getLeftExpression().getType());
        super.visitDeclarationExpression(expression);
    }

    @Override
    public void visitConstructorCallExpression(
            ConstructorCallExpression call) {
        check(call.getType(), call);
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
    public void visitStaticMethodCallExpression(
            StaticMethodCallExpression call) {
    	// don't check here if the type reference is implicit
    	// we know that the type is not implicit if the name
    	// location is filled in.
        if(call.getOwnerType() != call.getOwnerType().redirect()) {
    	    check(call.getOwnerType());
    	}


    	super.visitStaticMethodCallExpression(call);

    	// the method itself is not an expression, but only a string
        // so this check call will test for open declaration on the method
        check(call);
    }

    @Override
    public void visitClass(ClassNode node) {
        // special case...could be selecting the class name itself
        if (node.getNameEnd() > 0) {
            checkNameRange(node);
        }

        ClassNode unresolvedSuperClass = node.getUnresolvedSuperClass();
        if (unresolvedSuperClass != null && unresolvedSuperClass.getEnd() > 0) {
            visitClassReference(unresolvedSuperClass); // use unresolved to maintain source
                                         // locations
        }
        if (node.getInterfaces() != null) {
            for (ClassNode inter : node.getInterfaces()) {
                if (inter.getEnd() > 0) {
                    visitClassReference(inter);
                }
            }
        }
        if (node.getObjectInitializerStatements() != null) {
            for (Statement element : (Iterable<Statement>) node.getObjectInitializerStatements()) {
                element.visit(this);
            }
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
                // do not look into closure classes.  A closure class
                // looks like ParentClass$_name_closure#, where
                // ParentClass is the name of the containing class.
                // name is a name for the closure, and # is a number
                if (!inner.isSynthetic() || inner instanceof GeneratedClosure) {
                    this.visitClass(inner);
                }
            }
        }

        // visit <clinit> body because this is where static field initializers are placed
        // However, there is a problem in that Constants are defined here as
        // well.
        // If a match is found here, keep it for later because there may be a
        // more appropriate
        // match in the class body
        VisitCompleteException candidate = null;
        try {
            MethodNode clinit = node.getMethod("<clinit>", new Parameter[0]);
            if (clinit != null && clinit.getCode() instanceof BlockStatement) {
                for (Statement element : (Iterable<Statement>) ((BlockStatement) clinit.getCode()).getStatements()) {
                    element.visit(this);
                }
            }
        } catch (VisitCompleteException e) {
            candidate = e;
        }
        visitAnnotations(node);
        node.visitContents(this);

        // if we have gotten here, then we have not found a more appropriate
        // candidate
        if (candidate != null) {
            throw candidate;
        }
    }
    
    /**
     * Visits a class node with potential type parameters
     * @param node
     */
    private void visitClassReference(ClassNode node) {
        if (node.isUsingGenerics() && node.getGenericsTypes() != null) {
            for (GenericsType gen : node.getGenericsTypes()) {
                if (gen.getLowerBound() != null) {
                    visitClassReference(gen.getLowerBound());
                }
                if (gen.getUpperBounds() != null) {
                    for (ClassNode upper : gen.getUpperBounds()) {
                        // handle enums where the upper bound is the same as the type
                        if (!upper.getName().equals(node.getName())) {
                            visitClassReference(upper);
                        }
                    }
                }
                if (gen.getType() != null && gen.getName().charAt(0) != '?') {
                    visitClassReference(gen.getType());
                }
            }
        }
        check(node);
    }

    /**
     * Super implementation doesn't visit the annotation type itself
     */
    @Override
    public void visitAnnotations(AnnotatedNode node) {
        List<AnnotationNode> annotations = node.getAnnotations();
        if (annotations.isEmpty()) return;
        for (AnnotationNode an : annotations) {
            // skip built-in properties
            if (an.isBuiltIn()) continue;

            check(an.getClassNode());

            for (Map.Entry<String, Expression> member : (Iterable<Map.Entry<String, Expression>>)an.getMembers().entrySet()) {
                Expression value = member.getValue();
                if (value instanceof AnnotationConstantExpression) {
                    check(((AnnotationConstantExpression) value).getType());
                }
                value.visit(this);
            }
        }
    }

    @Override
    public void visitGStringExpression(GStringExpression expression) {
        super.visitGStringExpression(expression);
    }

    // method does not exist in 1.6 stream
    // @Override
    public void visitPackage(PackageNode node) {
        visitAnnotations(node);
        if (node != null) {
            check(node);
        }
    }

    @Override
    public void visitImports(ModuleNode module) {
        for (ImportNode importNode : new ImportNodeCompatibilityWrapper(module).getAllImportNodes()) {
            if (importNode.getType() != null) {
                visitAnnotations(importNode);
                check(importNode.getType());
                if (importNode.getFieldNameExpr() != null) {
                    check(importNode.getFieldNameExpr());
                }
                if (importNode.getAliasExpr() != null) {
                    check(importNode.getAliasExpr());
                }
            }
        }
    }
    
    @Override
    public void visitBinaryExpression(BinaryExpression expression) {
        super.visitBinaryExpression(expression);
        check(expression);
    }

    /**
     * Check if the body of the node covers the selection
     *
     * @throw {@link VisitCompleteException} if a match is found
     */
    protected void check(ASTNode node) {
        if (doTest(node)) {
            nodeFound = node;
            throw new VisitCompleteException();
        }
        if (node instanceof ClassNode) {
            checkGenerics((ClassNode) node);
        }
    }

    /**
     * Check if the body of the node covers the selection
     * 
     * This variant of the method allowsyou to check for one node
     * but target another one
     *
     * @throw {@link VisitCompleteException} if a match is found
     */
    protected void check(ASTNode checkNode, ASTNode targetNode) {
        if (doTest(checkNode)) {
            nodeFound = targetNode;
            throw new VisitCompleteException();
        }
        if (checkNode instanceof ClassNode) {
            checkGenerics((ClassNode) checkNode);
        }
    }
    
    /**
     * Check if the name of the node covers the selection
     *
     * @throw {@link VisitCompleteException} if a match is found
     */
    protected void checkNameRange(AnnotatedNode node) {
        if (doNameRangeTest(node)) {
            nodeFound = node;
            throw new VisitCompleteException();
        }
        if (node instanceof ClassNode) {
            checkGenerics((ClassNode) node);
        }
    }

    /**
     * forces the checking of generics for class nodes
     *
     * @param node
     */
    private void checkGenerics(ClassNode node) {
        if (node.isUsingGenerics() && node.getGenericsTypes() != null) {
            for (GenericsType gen : node.getGenericsTypes()) {
                if (gen.getLowerBound() != null) {
                    check(gen.getLowerBound());
                }
                if (gen.getUpperBounds() != null) {
                    for (ClassNode upper : gen.getUpperBounds()) {
                        // handle enums where the upper bound is the same as the type
                        if (! upper.getName().equals(node.getName())) {
                            check(upper);
                        }
                    }
                }
                if (gen.getType() != null && gen.getType().getName().charAt(0) != '?') {
                    check(gen.getType());
                }
            }
        }
    }

    /**
     * Return true if the visit should be ended and the node is found.
     *
     * @param node node to compare against the interesting region
     * @return true iff this node covers the interesting region
     */
    protected boolean doTest(ASTNode node) {
        return node.getEnd() > 0 && r.regionIsCoveredByNode(node);
    }

    /**
     * Return true if the visit should be ended and the node is found.
     * Tests the name range of the node (assumes that this is a
     * declaration node).
     *
     * @param node node to compare against the interesting region
     * @return true iff this node covers the interesting region
     */
    protected boolean doNameRangeTest(AnnotatedNode node) {
        return r.regionIsCoveredByNameRange(node);
    }

    public ASTNode doVisit(ModuleNode module) {
        try {
            PackageNode pack = module.getPackage();
            if (pack != null) {
                visitPackage(pack);
            }

            visitImports(module);
            for (ClassNode clazz : (Iterable<ClassNode>) module.getClasses()) {
                this.visitClass(clazz);
            }
        } catch (VisitCompleteException e) {
        }
        return nodeFound;
    }

}