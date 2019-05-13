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
package org.eclipse.jdt.groovy.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.ClosureSignatureHint;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
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
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCall;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.SpreadMapExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.ast.expr.UnaryPlusExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.ast.tools.WideningCategories;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.AnnotationCollectorTransform;
import org.codehaus.groovy.transform.FieldASTTransformation;
import org.codehaus.groovy.transform.sc.ListOfExpressionsExpression;
import org.codehaus.groovy.transform.sc.transformers.CompareToNullExpression;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.codehaus.groovy.transform.trait.Traits;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyEclipseBug;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.JavaCoreUtil;
import org.codehaus.jdt.groovy.model.ModuleNodeMapper.ModuleNodeInfo;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.Activator;
import org.eclipse.jdt.groovy.core.util.ArrayUtils;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.ITypeRequestor.VisitStatus;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Visits {@link GroovyCompilationUnit} instances to determine the type of expressions they contain.
 */
public class TypeInferencingVisitorWithRequestor extends ClassCodeVisitorSupport {

    private static void log(Throwable t, String form, Object... args) {
        String m = "Groovy-Eclipse Type Inferencing: " + String.format(form, args);
        Status s = new Status(IStatus.ERROR, Activator.PLUGIN_ID, m, t);
        Util.log(s);
    }

    // shared instances for several method checks below
    private static final String[] NO_PARAMS = CharOperation.NO_STRINGS;
    private static final Parameter[] NO_PARAMETERS = Parameter.EMPTY_ARRAY;

    /**
     * Set to true if debug mode is desired. Any exceptions will be spit to syserr. Also, after a visit, there will be a sanity
     * check to ensure that all stacks are empty Only set to true if using a visitor that always visits the entire file
     */
    public boolean DEBUG = false;

    private final GroovyCompilationUnit unit;

    private final LinkedList<VariableScope> scopes = new LinkedList<>();

    // we are going to have to be very careful about the ordering of lookups
    // Simple type lookup must be last because it always returns an answer
    // Assume that if something returns an answer, then we go with that.
    // Later on, should do some ordering of results
    private final ITypeLookup[] lookups;

    private ITypeRequestor requestor;
    private final JDTResolver resolver;
    private IJavaElement enclosingElement;
    private ASTNode enclosingDeclarationNode;
    private final ModuleNode enclosingModule;
    private BinaryExpression enclosingAssignment;
    private ConstructorCallExpression enclosingConstructorCall;

    /**
     * The head of the stack is the current property/attribute/methodcall/binary
     * expression being visited. This stack is used so we can keep track of the
     * type of the object expressions in these property expressions.
     */
    private final LinkedList<ASTNode> completeExpressionStack = new LinkedList<>();

    /**
     * Tracks the declaring type of the current dependent expression. Dependent
     * expressions are dependent on a primary expression to find type information.
     * This field is only applicable for {@link PropertyExpression}s and {@link MethodCallExpression}s.
     */
    private final LinkedList<Tuple> dependentDeclarationStack = new LinkedList<>();

    /**
     * Tracks the type of the type of the property field corresponding to each
     * frame of the property expression.
     */
    private final LinkedList<ClassNode> dependentTypeStack = new LinkedList<>();

    /**
     * Tracks the type of the object expression corresponding to each frame of
     * the property expression.
     */
    private final LinkedList<ClassNode> primaryTypeStack = new LinkedList<>();

    /**
     * Tracks the anonymous inner class counts for type members.  Required for
     * calls to {@code IMember.getType("", occurrenceCount);}
     */
    private Map<IJavaElement, AtomicInteger> occurrenceCounts = new HashMap<>();

    private final AssignmentStorer assignmentStorer = new AssignmentStorer();

    /**
     * Use factory to instantiate
     */
    TypeInferencingVisitorWithRequestor(GroovyCompilationUnit unit, ITypeLookup[] lookups) {
        this.unit = unit;
        this.lookups = lookups;

        ModuleNodeInfo info = createModuleNode(unit);
        if (info == null) {
            this.resolver = null;
            this.enclosingModule = null;
        } else {
            this.resolver = info.resolver;
            this.enclosingModule = info.module;
            this.enclosingDeclarationNode = info.module;
        }
    }

    //--------------------------------------------------------------------------

    public void visitCompilationUnit(ITypeRequestor requestor) {
        if (enclosingModule == null) {
            // no module node, can't do anything
            return;
        }

        this.requestor = requestor;
        this.enclosingElement = unit;
        VariableScope topLevelScope = new VariableScope(null, enclosingModule, false);
        scopes.add(topLevelScope);

        for (ITypeLookup lookup : lookups) {
            if (lookup instanceof ITypeResolver) {
                ((ITypeResolver) lookup).setResolverInformation(enclosingModule, resolver);
            }
            lookup.initialize(unit, topLevelScope);
        }

        try {
            visitPackage(enclosingModule.getPackage());
            visitImports(enclosingModule);
            for (IType type : unit.getTypes()) {
                visitJDT(type, requestor);
            }
        } catch (CancellationException e) {
            throw e; // propagate
        } catch (VisitCompleted vc) {
            // can ignore
        } catch (Exception e) {
            log(e, "Error visiting types for %s", unit.getElementName());
            if (DEBUG) {
                System.err.println("Excpetion thrown from inferencing engine");
                e.printStackTrace();
            }
        } finally {
            occurrenceCounts.clear();
            scopes.removeLast();
        }
        if (DEBUG) {
            postVisitSanityCheck();
        }
    }

    public void visitJDT(IType type, ITypeRequestor requestor) {
        ClassNode node = findClassNode(createName(type));
        if (node == null) {
            // probably some AST transformation is making this node invisible
            return;
        }

        scopes.add(new VariableScope(scopes.getLast(), node, false));
        ASTNode  enclosingDeclaration0 = enclosingDeclarationNode;
        IJavaElement enclosingElement0 = enclosingElement;
        enclosingDeclarationNode = node;
        enclosingElement = type;
        try {
            visitAnnotations(node);
            if (isMetaAnnotation(node)) {
                // visit relocated @AnnotationCollector annotations
                visitAnnotations(AnnotationCollectorTransform.getMeta(node));
            }

            // visit name "node"
            TypeLookupResult result = new TypeLookupResult(node, node, node, TypeConfidence.EXACT, scopes.getLast());
            VisitStatus status = notifyRequestor(node, requestor, result);
            switch (status) {
            case CONTINUE:
                break;
            case CANCEL_BRANCH:
                return;
            case CANCEL_MEMBER:
            case STOP_VISIT:
                throw new VisitCompleted(status);
            }

            if (!node.isEnum()) {
                visitGenericTypes(node);
                for (ClassNode face : node.getInterfaces()) {
                    visitClassReference(face);
                }
                visitClassReference(node.getUnresolvedSuperClass());
            }

            MethodNode clinit = node.getMethod("<clinit>", NO_PARAMETERS);
            if (clinit != null && clinit.getCode() instanceof BlockStatement) {
                // visit <clinit> body because this is where static field initializers
                // are placed; only visit field initializers here -- it's important to
                // get the right variable scope for the initializer to ensure that the
                // field is one of the enclosing nodes
                for (Statement element : ((BlockStatement) clinit.getCode()).getStatements()) {
                    // only visit the static initialization of a field
                    if (element instanceof ExpressionStatement && ((ExpressionStatement) element).getExpression() instanceof BinaryExpression) {
                        BinaryExpression expr = (BinaryExpression) ((ExpressionStatement) element).getExpression();
                        if (expr.getLeftExpression() instanceof FieldExpression) {
                            FieldNode fieldNode = ((FieldExpression) expr.getLeftExpression()).getField();
                            if (fieldNode != null && fieldNode.isStatic() && !fieldNode.getName().matches("(MAX|MIN)_VALUE|\\$VALUES") && expr.getRightExpression() != null) {
                                // create the field scope so that it looks like we are visiting within the context of the field
                                scopes.add(new VariableScope(scopes.getLast(), fieldNode, true));
                                try {
                                    expr.getRightExpression().visit(this);
                                } finally {
                                    scopes.removeLast().bubbleUpdates();
                                }
                            }
                        }
                    }
                }
                if (!node.isEnum()) {
                    visitMethodInternal(clinit);
                }
            }

            try {
                for (IMember member : membersOf(type, node.isScript())) {
                    switch (member.getElementType()) {
                    case IJavaElement.METHOD:
                        visitJDT((IMethod) member, requestor);
                        break;
                    case IJavaElement.FIELD:
                        visitJDT((IField) member, requestor);
                        break;
                    case IJavaElement.TYPE:
                        visitJDT((IType) member, requestor);
                        break;
                    }
                }

                // TODO: Should all methods w/o peer in JDT model have their bodies visited?  Below are two cases in particular.

                if (!type.isEnum()) {
                    if (node.isScript()) {
                        // visit fields created by @Field
                        for (FieldNode field : node.getFields()) {
                            if (field.getEnd() > 0) {
                                visitFieldInternal(field);
                            }
                        }
                    } else {
                        // visit fields and methods relocated by @Trait
                        List<FieldNode> traitFields = node.redirect().getNodeMetaData("trait.fields");
                        if (traitFields != null) {
                            for (FieldNode field : traitFields) {
                                visitFieldInternal(field);
                            }
                        }
                        List<MethodNode> traitMethods = node.redirect().getNodeMetaData("trait.methods");
                        if (traitMethods != null) {
                            for (MethodNode method : traitMethods) {
                                visitMethodInternal(method);
                            }
                        }
                    }
                }

                // visit relocated @Memoized method bodies
                for (MethodNode method : node.getMethods()) {
                    if (method.getName().startsWith("memoizedMethodPriv$")) {
                        scopes.add(new VariableScope(scopes.getLast(), method, method.isStatic()));
                        enclosingDeclarationNode = method;
                        try {
                            visitClassCodeContainer(method.getCode());
                        } finally {
                            enclosingDeclarationNode = node;
                            scopes.removeLast().bubbleUpdates();
                        }
                    }
                }
            } catch (JavaModelException e) {
                log(e, "Error visiting children of %s", type.getFullyQualifiedName());
            }
        } catch (VisitCompleted vc) {
            if (vc.status == VisitStatus.STOP_VISIT) {
                throw vc;
            }
        } finally {
            scopes.removeLast().bubbleUpdates();
            enclosingElement = enclosingElement0;
            enclosingDeclarationNode = enclosingDeclaration0;
        }
    }

    public void visitJDT(IField field, ITypeRequestor requestor) {
        FieldNode fieldNode = findFieldNode(field);
        if (fieldNode == null) {
            // probably some sort of AST transformation is making this node invisible
            return;
        }
        this.requestor = requestor;

        IJavaElement enclosingElement0 = enclosingElement;
        enclosingElement = field;
        try {
            visitField(fieldNode);
        } catch (VisitCompleted vc) {
            if (vc.status == VisitStatus.STOP_VISIT) {
                throw vc;
            }
        } finally {
            enclosingElement = enclosingElement0;
        }

        if (isLazy(fieldNode)) {
            MethodNode fieldInit = findLazyMethod(field.getElementName());
            if (fieldInit != null && fieldInit.getEnd() < 1) {
                enclosingElement = field;
                try {
                    visitMethodInternal(fieldInit);
                } finally {
                    enclosingElement = enclosingElement0;
                }
            }
        }
    }

    public void visitJDT(IMethod method, ITypeRequestor requestor) {
        MethodNode methodNode = findMethodNode(method);
        if (methodNode == null) {
            // probably some sort of AST transformation is making this node invisible
            return;
        }
        this.requestor = requestor;

        IJavaElement enclosingElement0 = enclosingElement;
        enclosingElement = method;
        try {
            visitMethodInternal(methodNode);
            // visit relocated @Transactional, et al. method bodies
            Optional<List<MethodNode>> decorated = Optional.ofNullable(methodNode.getNodeMetaData("$DECORATED"));
            decorated.ifPresent(decoratedMethodNodes -> decoratedMethodNodes.forEach(this::visitMethodInternal));
        } catch (VisitCompleted | CancellationException e) {
            throw e; // propagate
        } catch (Exception e) {
            log(e, "Error visiting method %s in class %s", method.getElementName(), method.getParent().getElementName());
        } finally {
            enclosingElement = enclosingElement0;
        }
    }

    //

    @Override
    public void visitPackage(PackageNode node) {
        if (node != null) {
            visitAnnotations(node);

            IJavaElement oldEnclosing = enclosingElement;
            enclosingElement = unit.getPackageDeclaration(node.getName().substring(0, node.getName().length() - 1));
            try {
                TypeLookupResult result = new TypeLookupResult(null, null, node, TypeConfidence.EXACT, null);
                VisitStatus status = notifyRequestor(node, requestor, result);
                if (status == VisitStatus.STOP_VISIT) {
                    throw new VisitCompleted(status);
                }
            } finally {
                enclosingElement = oldEnclosing;
            }
        }
    }

    @Override
    public void visitImports(ModuleNode node) {
        for (ImportNode imp : GroovyUtils.getAllImportNodes(node)) {
            IJavaElement oldEnclosingElement = enclosingElement;

            visitAnnotations(imp);

            String importName;
            if (imp.isStar()) {
                if (!imp.isStatic()) {
                    importName = imp.getPackageName() + "*";
                } else {
                    importName = imp.getClassName().replace('$', '.') + ".*";
                }
            } else {
                if (!imp.isStatic()) {
                    importName = imp.getClassName().replace('$', '.');
                } else {
                    importName = imp.getClassName().replace('$', '.') + "." + imp.getFieldName();
                }
                // TODO: concatenate import alias?
            }

            enclosingElement = unit.getImport(importName);
            if (!enclosingElement.exists()) {
                enclosingElement = oldEnclosingElement;
            }

            try {
                TypeLookupResult result = null;
                VariableScope scope = scopes.getLast();
                scope.setPrimaryNode(false);
                assignmentStorer.storeImport(imp, scope);
                for (ITypeLookup lookup : lookups) {
                    TypeLookupResult candidate = lookup.lookupType(imp, scope);
                    if (candidate != null) {
                        if (result == null || result.confidence.isLessThan(candidate.confidence)) {
                            result = candidate;
                        }
                        if (result.confidence.isAtLeast(TypeConfidence.INFERRED)) {
                            break;
                        }
                    }
                }
                VisitStatus status = notifyRequestor(imp, requestor, result);

                switch (status) {
                case CONTINUE:
                    try {
                        ClassNode type = imp.getType();
                        if (type != null) {
                            visitClassReference(type);
                            completeExpressionStack.add(imp);
                            if (imp.getFieldNameExpr() != null) {
                                primaryTypeStack.add(type);
                                imp.getFieldNameExpr().visit(this);
                                dependentDeclarationStack.removeLast();
                                dependentTypeStack.removeLast();
                            }
                            completeExpressionStack.removeLast();
                        }
                    } catch (VisitCompleted vc) {
                        if (vc.status == VisitStatus.STOP_VISIT) {
                            throw vc;
                        }
                    }
                    //fallthrough
                case CANCEL_MEMBER:
                    continue;
                case CANCEL_BRANCH:
                    // assume that import statements are not interesting
                    return;
                case STOP_VISIT:
                    throw new VisitCompleted(status);
                }
            } finally {
                enclosingElement = oldEnclosingElement;
            }
        }
    }

    private void visitClassReference(ClassNode node) {
        TypeLookupResult result = null;
        node = GroovyUtils.getBaseType(node);
        VariableScope scope = scopes.getLast();
        for (ITypeLookup lookup : lookups) {
            TypeLookupResult candidate = lookup.lookupType(node, scope);
            if (candidate != null) {
                if (result == null || result.confidence.isLessThan(candidate.confidence)) {
                    result = candidate;
                }
                if (result.confidence.isAtLeast(TypeConfidence.INFERRED)) {
                    break;
                }
            }
        }
        scope.setPrimaryNode(false);

        VisitStatus status = notifyRequestor(node, requestor, result);
        switch (status) {
        case CONTINUE:
            if (!node.isEnum()) {
                visitGenericTypes(node);
            }
            // fall through
        case CANCEL_BRANCH:
            return;
        case CANCEL_MEMBER:
        case STOP_VISIT:
            throw new VisitCompleted(status);
        }
    }

    private void visitFieldInternal(FieldNode node) {
        try {
            visitField(node);
        } catch (VisitCompleted vc) {
            if (vc.status == VisitStatus.STOP_VISIT) {
                throw vc;
            }
        }
    }

    private void visitMethodInternal(MethodNode node) {
        scopes.add(new VariableScope(scopes.getLast(), node, node.isStatic()));
        ASTNode enclosingDeclaration0 = enclosingDeclarationNode;
        enclosingDeclarationNode = node;
        try {
            visitConstructorOrMethod(node, node instanceof ConstructorNode);
        } catch (VisitCompleted vc) {
            if (vc.status == VisitStatus.STOP_VISIT) {
                throw vc;
            }
        } finally {
            scopes.removeLast().bubbleUpdates();
            enclosingDeclarationNode = enclosingDeclaration0;
        }
    }

    /**
     * @param node anonymous inner class for enum constant
     */
    private void visitMethodOverrides(ClassNode node) {
        assert node.isEnum() && node.getEnclosingMethod() == null;
        ASTNode  enclosingDeclaration0 = enclosingDeclarationNode;
        IJavaElement enclosingElement0 = enclosingElement; // enum
        enclosingDeclarationNode = scopes.getLast().getEnclosingFieldDeclaration();
        try {
            IField enumConstant = ((IType) enclosingElement).getField(((FieldNode) enclosingDeclarationNode).getName());
            assert enumConstant != null && enumConstant.exists();
            for (MethodNode method : node.getMethods()) {
                if (method.getEnd() > 0) {
                    enclosingElement = JavaCoreUtil.findMethod(method, (IType) enumConstant.getChildren()[0]);
                    visitMethodInternal(method);
                }
            }
        } catch (JavaModelException e) {
            log(e, "Error visiting children of %s", enclosingDeclarationNode);
        } finally {
            enclosingElement = enclosingElement0;
            enclosingDeclarationNode = enclosingDeclaration0;
        }
    }

    //

    @Override
    public void visitAnnotation(AnnotationNode node) {
        TypeLookupResult result = null;
        VariableScope scope = scopes.getLast();
        for (ITypeLookup lookup : lookups) {
            TypeLookupResult candidate = lookup.lookupType(node, scope);
            if (candidate != null) {
                if (result == null || result.confidence.isLessThan(candidate.confidence)) {
                    result = candidate;
                }
                if (result.confidence.isAtLeast(TypeConfidence.INFERRED)) {
                    break;
                }
            }
        }
        VisitStatus status = notifyRequestor(node, requestor, result);

        switch (status) {
        case CONTINUE:
            // visit annotation label
            visitClassReference(node.getClassNode());
            // visit attribute values
            super.visitAnnotation(node);
            // visit attribute labels
            for (String name : node.getMembers().keySet()) {
                MethodNode meth = node.getClassNode().getMethod(name, NO_PARAMETERS);
                ASTNode attr; TypeLookupResult noLookup;
                if (meth != null) {
                    attr = meth; // no Groovy AST node exists for name
                    noLookup = new TypeLookupResult(meth.getReturnType(),
                        node.getClassNode().redirect(), meth, TypeConfidence.EXACT, scope);
                } else {
                    attr = new ConstantExpression(name);
                    // this is very rough; it only works for an attribute that directly follows '('
                    attr.setStart(node.getClassNode().getEnd() + 1); attr.setEnd(attr.getStart() + name.length());

                    noLookup = new TypeLookupResult(VariableScope.VOID_CLASS_NODE,
                        node.getClassNode().redirect(), null, TypeConfidence.UNKNOWN, scope);
                }
                noLookup.enclosingAnnotation = node; // set context for requestor
                status = notifyRequestor(attr, requestor, noLookup);
                if (status != VisitStatus.CONTINUE) break;
            }
            break;
        case CANCEL_BRANCH:
            return;
        case CANCEL_MEMBER:
        case STOP_VISIT:
            throw new VisitCompleted(status);
        }
    }

    @Override
    public void visitArrayExpression(ArrayExpression node) {
        boolean shouldContinue = handleSimpleExpression(node);
        if (shouldContinue) {
            visitClassReference(node.getType());
            super.visitArrayExpression(node);
        }
    }

    @Override
    public void visitAttributeExpression(AttributeExpression node) {
        visitPropertyExpression(node);
    }

    @Override
    public void visitBinaryExpression(BinaryExpression node) {
        if (isDependentExpression(node)) {
            primaryTypeStack.removeLast();
        }

        boolean isAssignment = Types.ofType(node.getOperation().getType(), Types.ASSIGNMENT_OPERATOR);
        BinaryExpression oldEnclosingAssignment = enclosingAssignment;
        if (isAssignment) {
            enclosingAssignment = node;
        }

        completeExpressionStack.add(node);

        // visit order is dependent on whether or not assignment statement
        Expression toVisitPrimary;
        Expression toVisitDependent;
        if (isAssignment) {
            toVisitPrimary = node.getRightExpression();
            toVisitDependent = node.getLeftExpression();
        } else {
            toVisitPrimary = node.getLeftExpression();
            toVisitDependent = node.getRightExpression();
        }

        toVisitPrimary.visit(this);

        ClassNode primaryExprType = primaryTypeStack.removeLast();
        if (isAssignment) {
            assignmentStorer.storeAssignment(node, scopes.getLast(), primaryExprType);
        }

        toVisitDependent.visit(this);

        completeExpressionStack.removeLast();
        // type of the entire expression
        ClassNode completeExprType = primaryExprType;
        ClassNode dependentExprType = primaryTypeStack.removeLast();

// TODO: Is it an illegal state to have either as null?
assert primaryExprType != null && dependentExprType != null;

        if (!isAssignment && primaryExprType != null && dependentExprType != null) {
            // type of RHS of binary expression
            // find the type of the complete expression
            String associatedMethod = findBinaryOperatorName(node.getOperation().getText());
            if (isArithmeticOperationOnNumberOrStringOrList(node.getOperation().getText(), primaryExprType, dependentExprType)) {
                // in 1.8 and later, Groovy will not go through the MOP for standard arithmetic operations on numbers
                completeExprType = dependentExprType.equals(VariableScope.STRING_CLASS_NODE) ? VariableScope.STRING_CLASS_NODE : primaryExprType;
            } else if (associatedMethod != null) {
                scopes.getLast().setMethodCallArgumentTypes(Collections.singletonList(dependentExprType));
                // there is an overloadable method associated with this operation; convert to a constant expression and look it up
                TypeLookupResult result = lookupExpressionType(new ConstantExpression(associatedMethod), primaryExprType, false, scopes.getLast());
                if (result.confidence != TypeConfidence.UNKNOWN) completeExprType = result.type;
                // special case DefaultGroovyMethods.getAt -- the problem is that DGM has too many variants of getAt
                if ("getAt".equals(associatedMethod) && VariableScope.DGM_CLASS_NODE.equals(result.declaringType)) {
                    if (primaryExprType.getName().equals("java.util.BitSet")) {
                        completeExprType = VariableScope.BOOLEAN_CLASS_NODE;
                    } else {
                        GenericsType[] lhsGenericsTypes = primaryExprType.getGenericsTypes();
                        ClassNode elementType;
                        if (VariableScope.MAP_CLASS_NODE.equals(primaryExprType) && lhsGenericsTypes != null && lhsGenericsTypes.length == 2) {
                            // for maps use the value type
                            elementType = lhsGenericsTypes[1].getType();
                        } else {
                            elementType = VariableScope.extractElementType(primaryExprType);
                        }
                        if (dependentExprType.isArray() ||
                                dependentExprType.implementsInterface(VariableScope.LIST_CLASS_NODE) ||
                                dependentExprType.getName().equals(VariableScope.LIST_CLASS_NODE.getName())) {
                            // if rhs is a range or list type, then result is a list of elements
                            completeExprType = createParameterizedList(elementType);
                        } else if (ClassHelper.isNumberType(dependentExprType)) {
                            // if rhs is a number type, then result is a single element
                            completeExprType = elementType;
                        }
                    }
                }
            } else {
                // no overloadable associated method
                completeExprType = findBinaryExpressionType(node.getOperation().getText(), primaryExprType, dependentExprType);
            }
        }
        handleCompleteExpression(node, completeExprType, null);
        enclosingAssignment = oldEnclosingAssignment;
    }

    @Override
    public void visitBitwiseNegationExpression(BitwiseNegationExpression node) {
        visitUnaryExpression(node, node.getExpression(), "~");
    }

    @Override
    public void visitBlockStatement(BlockStatement block) {
        VariableScope scope = new VariableScope(scopes.getLast(), block, false);
        scope.setCurrentNode(block);
        scopes.add(scope);

        boolean shouldContinue = handleStatement(block);
        if (shouldContinue) {
            super.visitBlockStatement(block);
        }

        scope.forgetCurrentNode();
        scope.bubbleUpdates();
        scopes.removeLast();
    }

    @Override
    public void visitBooleanExpression(BooleanExpression node) {
        boolean shouldContinue = handleSimpleExpression(node);
        if (shouldContinue) {
            super.visitBooleanExpression(node);
        }
    }

    @Override
    public void visitBytecodeExpression(BytecodeExpression node) {
        boolean shouldContinue = handleSimpleExpression(node);
        if (shouldContinue) {
            super.visitBytecodeExpression(node);
        }
    }

    @Override
    public void visitCastExpression(CastExpression node) {
        boolean shouldContinue = handleSimpleExpression(node);
        if (shouldContinue) {
            visitClassReference(node.getType());

            completeExpressionStack.add(node);
              super.visitCastExpression(node);
            completeExpressionStack.removeLast();
        }
    }

    @Override
    public void visitCatchStatement(CatchStatement node) {
        scopes.add(new VariableScope(scopes.getLast(), node, false));
        Parameter param = node.getVariable();
        if (param != null) {
            handleParameterList(new Parameter[] {param});
        }
        super.visitCatchStatement(node);
        scopes.removeLast().bubbleUpdates();
    }

    @Override
    public void visitClassExpression(ClassExpression node) {
        boolean shouldContinue = handleSimpleExpression(node);
        if (shouldContinue) {
            visitClassReference(node.getType());
            super.visitClassExpression(node);
        }
    }

    @Override
    public void visitClosureExpression(ClosureExpression node) {
        VariableScope parent = scopes.getLast();
        VariableScope scope = new VariableScope(parent, node, false);
        scopes.add(scope);

        // if enclosing closure, owner type is 'Closure', otherwise it's 'typeof(this)'
        if (parent.getEnclosingClosure() != null) {
            ClassNode closureType = GenericsUtils.nonGeneric(VariableScope.CLOSURE_CLASS_NODE);
            closureType.putNodeMetaData("outer.scope", parent.getEnclosingClosureScope());

            scope.addVariable("owner", closureType, VariableScope.CLOSURE_CLASS_NODE);
            scope.addVariable("getOwner", closureType, VariableScope.CLOSURE_CLASS_NODE);
        } else {
            ClassNode ownerType = scope.getThis();
            // GRECLIPSE-1348: if someone is silly enough to have a variable named "owner"; don't override it
            VariableScope.VariableInfo info = scope.lookupName("owner");
            if (info == null || info.scopeNode instanceof ClosureExpression) {
                scope.addVariable("owner", ownerType, VariableScope.CLOSURE_CLASS_NODE);
            }
            scope.addVariable("getOwner", ownerType, VariableScope.CLOSURE_CLASS_NODE);

            // only set this if not already in a closure; type doesn't vary with nesting
            scope.addVariable("thisObject", ownerType, VariableScope.CLOSURE_CLASS_NODE);
            scope.addVariable("getThisObject", ownerType, VariableScope.CLOSURE_CLASS_NODE);
        }

        // if enclosing method call, delegate type can be specified by the method, otherwise it's 'typeof(owner)'
        VariableScope.CallAndType cat = scope.getEnclosingMethodCallExpression();
        if (cat != null && cat.getDelegateType(node) != null) {
            ClassNode delegateType = cat.getDelegateType(node);
            scope.addVariable("delegate", delegateType, VariableScope.CLOSURE_CLASS_NODE);
            scope.addVariable("getDelegate", delegateType, VariableScope.CLOSURE_CLASS_NODE);
        } else {
            ClassNode delegateType = scope.getOwner();
            // GRECLIPSE-1348: if someone is silly enough to have a variable named "delegate"; don't override it
            VariableScope.VariableInfo inf = scope.lookupName("delegate");
            if (inf == null || inf.scopeNode instanceof ClosureExpression) {
                scope.addVariable("delegate", delegateType, VariableScope.CLOSURE_CLASS_NODE);
            }
            scope.addVariable("getDelegate", delegateType, VariableScope.CLOSURE_CLASS_NODE);
        }

        ClassNode[] inferredParamTypes = inferClosureParamTypes(node, scope);
        if (node.isParameterSpecified()) {
            Parameter[] parameters = node.getParameters();
            for (int i = 0, n = parameters.length; i < n; i += 1) {
                // only change the type of the parameter if it's not explicitly defined
                if (parameters[i].isDynamicTyped() && !VariableScope.OBJECT_CLASS_NODE.equals(inferredParamTypes[i])) {
                    parameters[i].setType(inferredParamTypes[i]);
                }
            }
            handleParameterList(parameters);
        } else if (node.getParameters() != null && !scope.containsInThisScope("it")) {
            scope.addVariable("it", inferredParamTypes[0], VariableScope.CLOSURE_CLASS_NODE);
        }

        //GRECLIPSE-598 make sure enclosingAssignment is set before visitClosureExpression() to make assignedVariable pointcut work
        //immediately inside assigned closure block: def foo = { | }
        scope.getWormhole().put("enclosingAssignment", enclosingAssignment);

        super.visitClosureExpression(node);
        handleSimpleExpression(node);
        scopes.removeLast();
    }

    @Override
    public void visitClosureListExpression(ClosureListExpression node) {
        boolean shouldContinue = handleSimpleExpression(node);
        if (shouldContinue) {
            super.visitClosureListExpression(node);
        }
    }

    @Override
    public void visitConstantExpression(ConstantExpression node) {
        if (node instanceof AnnotationConstantExpression) {
            visitClassReference(node.getType());
        }
        scopes.getLast().setCurrentNode(node);
        handleSimpleExpression(node);
        scopes.getLast().forgetCurrentNode();
        super.visitConstantExpression(node);
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression node) {
        boolean shouldContinue = handleSimpleExpression(node);
        if (shouldContinue) {
            final ClassNode type = node.getType();
            if (node.isUsingAnonymousInnerClass()) {
                // in "new Type() { ... }", Type is super class or interface
                if (type.getSuperClass() != VariableScope.OBJECT_CLASS_NODE) {
                    visitClassReference(type.getUnresolvedSuperClass(false));
                } else {
                    visitClassReference(type.getInterfaces()[0]);
                }
            } else if (!node.isSpecialCall()) {
                visitClassReference(type);
            }
            if (node.getArguments() instanceof TupleExpression) {
                TupleExpression tuple = (TupleExpression) node.getArguments();
                if (isNotEmpty(tuple.getExpressions())) {
                    if ((tuple.getExpressions().size() == 1 && tuple.getExpressions().get(0) instanceof MapExpression) ||
                            tuple.getExpression(tuple.getExpressions().size() - 1) instanceof NamedArgumentListExpression) {
                        // remember this is a map ctor call, so that field names can be inferred when visiting the map
                        enclosingConstructorCall = node;
                    }
                }
            }
            super.visitConstructorCallExpression(node);

            // visit anonymous inner class body
            if (node.isUsingAnonymousInnerClass()) {
                scopes.add(new VariableScope(scopes.getLast(), type, false));
                ASTNode  enclosingDeclaration0 = enclosingDeclarationNode;
                IJavaElement enclosingElement0 = enclosingElement;
                enclosingDeclarationNode = type;
                try {
                    IType anon = findAnonType(type);

                    for (Statement stmt : type.getObjectInitializerStatements()) {
                        stmt.visit(this);
                    }
                    for (FieldNode field : type.getFields()) {
                        if (field.getEnd() > 0) {
                            enclosingElement = anon.getField(field.getName());
                            visitFieldInternal(field);
                        }
                    }
                    for (MethodNode method : type.getMethods()) {
                        if (method.getEnd() > 0) {
                            enclosingElement = JavaCoreUtil.findMethod(method, anon);
                            visitMethodInternal(method);
                        }
                    }
                } finally {
                    scopes.removeLast().bubbleUpdates();
                    enclosingElement = enclosingElement0;
                    enclosingDeclarationNode = enclosingDeclaration0;
                }
            }
        }
    }

    @Override
    public void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        TypeLookupResult result = null;
        VariableScope scope = scopes.getLast();
        for (ITypeLookup lookup : lookups) {
            TypeLookupResult candidate = lookup.lookupType(node, scope);
            if (candidate != null) {
                if (result == null || result.confidence.isLessThan(candidate.confidence)) {
                    result = candidate;
                }
                if (result.confidence.isAtLeast(TypeConfidence.INFERRED)) {
                    break;
                }
            }
        }
        scope.setPrimaryNode(false);
        VisitStatus status = notifyRequestor(node, requestor, result);

        switch (status) {
        case CONTINUE:
            visitGenericTypes(node.getGenericsTypes(), null);
            visitClassReference(node.getReturnType());
            if (node.getExceptions() != null) {
                for (ClassNode e : node.getExceptions()) {
                    visitClassReference(e);
                }
            }
            if (handleParameterList(node.getParameters())) {
                visitAnnotations(node);
                if (!isConstructor || !(node.getCode() instanceof BlockStatement)) {
                    visitClassCodeContainer(node.getCode());
                } else {
                    for (Statement stmt : ((BlockStatement) node.getCode()).getStatements()) {
                        if (stmt.getEnd() > 0) { // skip inlined initialization expressions
                            stmt.visit(this);
                        }
                    }
                }
            }
            // fall through
        case CANCEL_BRANCH:
            return;
        case CANCEL_MEMBER:
        case STOP_VISIT:
            throw new VisitCompleted(status);
        }
    }

    @Override
    public void visitEmptyExpression(EmptyExpression node) {
        handleSimpleExpression(node);
    }

    @Override
    public void visitFieldExpression(FieldExpression node) {
        boolean shouldContinue = handleSimpleExpression(node);
        if (shouldContinue) {
            super.visitFieldExpression(node);
        }
    }

    @Override
    public void visitField(FieldNode node) {
        VariableScope scope = new VariableScope(scopes.getLast(), node, node.isStatic());
        ASTNode enclosingDeclaration0 = enclosingDeclarationNode;
        assignmentStorer.storeField(node, scope);
        enclosingDeclarationNode = node;
        scopes.add(scope);
        try {
            TypeLookupResult result = null;
            for (ITypeLookup lookup : lookups) {
                TypeLookupResult candidate = lookup.lookupType(node, scope);
                if (candidate != null) {
                    if (result == null || result.confidence.isLessThan(candidate.confidence)) {
                        result = candidate;
                    }
                    if (result.confidence.isAtLeast(TypeConfidence.INFERRED)) {
                        break;
                    }
                }
            }
            scope.setPrimaryNode(false);

            VisitStatus status = notifyRequestor(node, requestor, result);
            switch (status) {
            case CONTINUE:
                visitAnnotations(node);
                if (node.getEnd() > 0 && node.getDeclaringClass().isScript()) {
                    for (ASTNode anno : GroovyUtils.getTransformNodes(node.getDeclaringClass(), FieldASTTransformation.class)) {
                        if (anno.getStart() >= node.getStart() && anno.getEnd() < node.getEnd()) {
                            visitAnnotation((AnnotationNode) anno);
                        }
                    }
                }
                // if two values are == then that means the type is synthetic and doesn't exist in code...probably an enum field
                if (node.getType() != node.getDeclaringClass()) {
                    visitClassReference(node.getType());
                }
                Expression init = node.getInitialExpression();
                if (init != null) {
                    init.visit(this);
                }
                //fallthrough
            case CANCEL_BRANCH:
                return;
            case CANCEL_MEMBER:
            case STOP_VISIT:
                throw new VisitCompleted(status);
            }
        } finally {
            scopes.removeLast().bubbleUpdates();
            enclosingDeclarationNode = enclosingDeclaration0;
        }
    }

    @Override
    public void visitForLoop(ForStatement node) {
        completeExpressionStack.add(node);
        node.getCollectionExpression().visit(this);
        completeExpressionStack.removeLast();

        // the type of the collection
        ClassNode collectionType = primaryTypeStack.removeLast();

        // the loop has its own scope
        scopes.add(new VariableScope(scopes.getLast(), node, false));

        // a three-part for loop, i.e. "for (_; _; _)", uses a dummy variable; skip it
        if (!(node.getCollectionExpression() instanceof ClosureListExpression)) {
            Parameter param = node.getVariable();
            if (param.isDynamicTyped()) {
                // update the type of the parameter from the collection type
                scopes.getLast().addVariable(param.getName(), VariableScope.extractElementType(collectionType), null);
            }
            handleParameterList(new Parameter[] {param});
        }

        node.getLoopBlock().visit(this);

        scopes.removeLast().bubbleUpdates();
    }

    private void visitGenericTypes(ClassNode node) {
        if (node.isUsingGenerics()) {
            visitGenericTypes(node.getGenericsTypes(), node.getName());
        }
    }

    private void visitGenericTypes(GenericsType[] generics, String typeName) {
        if (generics == null) return;
        for (GenericsType gt : generics) {
            if (gt.getType() != null && gt.getName().charAt(0) != '?') {
                visitClassReference(gt.getType());
            }
            if (gt.getLowerBound() != null) {
                visitClassReference(gt.getLowerBound());
            }
            if (gt.getUpperBounds() != null) {
                for (ClassNode upper : gt.getUpperBounds()) {
                    // handle enums where the upper bound is the same as the type
                    if (!upper.getName().equals(typeName)) {
                        visitClassReference(upper);
                    }
                }
            }
        }
    }

    @Override
    public void visitGStringExpression(GStringExpression node) {
        scopes.getLast().setCurrentNode(node);
        boolean shouldContinue = handleSimpleExpression(node);
        if (shouldContinue) {
            super.visitGStringExpression(node);
        }
        scopes.getLast().forgetCurrentNode();
    }

    @Override
    public void visitIfElse(IfStatement node) {
            scopes.add(new VariableScope(scopes.getLast(), node, false) {
                @Override
                public void updateVariable(String name, ClassNode type, ClassNode declaringType) {
                    type = WideningCategories.lowestUpperBound(type, lookupName(name).type);
                    super.updateVariable(name, type, declaringType);
                }
            });

        // TODO: Assignment within expression may be conditional or unconditional due to short-circuit evaluation.
        node.getBooleanExpression().visit(this);

            Map<String, ClassNode[]> types = inferInstanceOfType(node.getBooleanExpression(), scopes.getLast());
            scopes.add(new VariableScope(scopes.getLast(), node.getIfBlock(), false));
            for (Map.Entry<String, ClassNode[]> entry : types.entrySet()) {
                if (entry.getValue().length > 0 && entry.getValue()[0] != null) {
                    scopes.getLast().updateVariableSoft(entry.getKey(), entry.getValue()[0]);
                }
            }

        node.getIfBlock().visit(this);

            VariableScope trueScope = scopes.removeLast();

            scopes.add(new VariableScope(scopes.getLast(), node.getElseBlock(), false));
            for (Map.Entry<String, ClassNode[]> entry : types.entrySet()) {
                if (entry.getValue().length > 1 && entry.getValue()[1] != null) {
                    scopes.getLast().updateVariableSoft(entry.getKey(), entry.getValue()[1]);
                }
            }

        // TODO: If the else block sets unconditionally, exclude current type from the LUB computation.
        node.getElseBlock().visit(this);

            VariableScope falseScope = scopes.removeLast();

            // apply variable updates
            trueScope.bubbleUpdates();
            falseScope.bubbleUpdates();
            scopes.removeLast().bubbleUpdates();
    }

    @Override
    public void visitListExpression(ListExpression node) {
        if (isDependentExpression(node)) {
            primaryTypeStack.removeLast();
        }
        scopes.getLast().setCurrentNode(node);
        completeExpressionStack.add(node);
        super.visitListExpression(node);
        ClassNode eltType;
        if (isNotEmpty(node.getExpressions())) {
            eltType = primaryTypeStack.removeLast();
        } else {
            eltType = VariableScope.OBJECT_CLASS_NODE;
        }
        completeExpressionStack.removeLast();
        ClassNode exprType = createParameterizedList(eltType);
        handleCompleteExpression(node, exprType, null);
        scopes.getLast().forgetCurrentNode();
    }

    @Override
    public void visitMapEntryExpression(MapEntryExpression node) {
        if (isDependentExpression(node)) {
            primaryTypeStack.removeLast();
        }
        scopes.getLast().setCurrentNode(node);
        completeExpressionStack.add(node);
        node.getKeyExpression().visit(this);
        ClassNode k = primaryTypeStack.removeLast();
        node.getValueExpression().visit(this);
        ClassNode v = primaryTypeStack.removeLast();
        completeExpressionStack.removeLast();

        ClassNode exprType;
        if (isPrimaryExpression(node)) {
            exprType = createParameterizedMap(k, v);
        } else {
            exprType = VariableScope.OBJECT_CLASS_NODE;
        }
        handleCompleteExpression(node, exprType, null);
        scopes.getLast().forgetCurrentNode();
    }

    @Override
    public void visitMapExpression(MapExpression node) {
        if (isDependentExpression(node)) {
            primaryTypeStack.removeLast();
        }
        ClassNode ctorType = null;
        if (enclosingConstructorCall != null) {
            // map expr within ctor call indicates use of map constructor or default constructor + property setters
            ctorType = enclosingConstructorCall.getType();
            enclosingConstructorCall = null;

            for (ConstructorNode ctor : ctorType.getDeclaredConstructors()) {
                Parameter[] ctorParams = ctor.getParameters();
                // TODO: What about ctorParams[0].getType().declaresInterface(VariableScope.MAP_CLASS_NODE)?
                // TODO: Do the generics of the Map matter?  Probably should be String (or Object?) for key.
                if (ctorParams.length == 1 && ctorParams[0].getType().equals(VariableScope.MAP_CLASS_NODE)) {
                    ctorType = null; // a map constructor exists; shut down key type lookups
                }
            }
        }
        scopes.getLast().setCurrentNode(node);
        completeExpressionStack.add(node);

        for (MapEntryExpression entry : node.getMapEntryExpressions()) {
            Expression key = entry.getKeyExpression(), val = entry.getValueExpression();
            if (ctorType != null && key instanceof ConstantExpression && !"*".equals(key.getText())) {

                VariableScope scope = scopes.getLast();
                // look for a non-synthetic setter followed by a property or field
                scope.setMethodCallArgumentTypes(Collections.singletonList(val.getType()));
                String setterName = AccessorSupport.SETTER.createAccessorName(key.getText());
                TypeLookupResult result = lookupExpressionType(new ConstantExpression(setterName), ctorType, false, scope);
                if (result.confidence == TypeConfidence.UNKNOWN || !(result.declaration instanceof MethodNode) ||
                        ((MethodNode) result.declaration).isSynthetic()) {
                    scope.getWormhole().put("lhs", key);
                    scope.setMethodCallArgumentTypes(null);
                    result = lookupExpressionType(key, ctorType, false, scope);
                }

                // pre-visit entry so keys are highlighted as keys, not fields/methods/properties
                ClassNode mapType = isPrimaryExpression(entry) ? createParameterizedMap(key.getType(), val.getType()) : primaryTypeStack.getLast();
                scope.setCurrentNode(entry);
                handleCompleteExpression(entry, mapType, null);
                scope.forgetCurrentNode();

                handleRequestor(key, ctorType, result);
                val.visit(this);
            } else {
                entry.visit(this);
            }
        }

        completeExpressionStack.removeLast();

        ClassNode exprType;
        if (isNotEmpty(node.getMapEntryExpressions())) {
            exprType = primaryTypeStack.removeLast();
        } else {
            exprType = createParameterizedMap(VariableScope.OBJECT_CLASS_NODE, VariableScope.OBJECT_CLASS_NODE);
        }
        handleCompleteExpression(node, exprType, null);
        scopes.getLast().forgetCurrentNode();
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression node) {
        VariableScope scope = scopes.getLast();
        scope.setCurrentNode(node);
        if (isDependentExpression(node)) {
            primaryTypeStack.removeLast();
        }
        completeExpressionStack.add(node);
        node.getObjectExpression().visit(this);

        ClassNode preSpreadType = null;
        if (node.isSpreadSafe()) {
            preSpreadType = primaryTypeStack.removeLast();
            // method call targets the element type of the object expression
            primaryTypeStack.add(VariableScope.extractSpreadType(preSpreadType));
        }
        ClassNode objExprType = primaryTypeStack.getLast();

        if (node.isUsingGenerics()) {
            visitGenericTypes(node.getGenericsTypes(), null);
        }

        // visit method before arguments to provide @ClosureParams, @DeleagtesTo, etc. to closures
        // NOTE: this makes choosing from overloads imprecise since argument types aren't complete
        node.getMethod().visit(this);

        // this is the inferred return type of this method
        // must pop now before visiting any other nodes
        ClassNode returnType = dependentTypeStack.removeLast();

        // this is the inferred declaring type of this method
        Tuple t = dependentDeclarationStack.removeLast();
        VariableScope.CallAndType call = new VariableScope.CallAndType(node, t.declaration, t.declaringType, enclosingModule);

        completeExpressionStack.removeLast();

        ClassNode catNode = isCategoryDeclaration(node, scope);
        if (catNode != null) {
            scope.setCategoryBeingDeclared(catNode);
        }

        // remember that we are inside a method call while analyzing the arguments
        scope.addEnclosingMethodCall(call);
        node.getArguments().visit(this);
        scope.forgetEnclosingMethodCall();

        ClassNode type;
        if (isEnumInit(node) && GroovyUtils.isAnonymous(type = node.getType().redirect())) {
            visitMethodOverrides(type);
        }

        MethodNode meth; // if return type depends on any Closure argument return types, deal with that now
        if (t.declaration instanceof MethodNode && (meth = (MethodNode) t.declaration).getGenericsTypes() != null &&
                Arrays.stream(meth.getParameters()).anyMatch(p -> p.getType().equals(VariableScope.CLOSURE_CLASS_NODE))) {
            scope.setMethodCallArgumentTypes(getMethodCallArgumentTypes(node));
            scope.setMethodCallGenericsTypes(getMethodCallGenericsTypes(node));
            try {
                boolean isStatic = (node.getObjectExpression() instanceof ClassExpression);
                returnType = lookupExpressionType(node.getMethod(), objExprType, isStatic, scope).type;
            } finally {
                scope.setMethodCallArgumentTypes(null);
                scope.setMethodCallGenericsTypes(null);
            }
        }

        if (node.isSpreadSafe()) {
            returnType = createSpreadResult(returnType, preSpreadType);
        }

        // check for trait field re-written as call to helper method
        Expression expr = GroovyUtils.getTraitFieldExpression(node);
        if (expr != null) {
            handleSimpleExpression(expr);
            postVisit(node, returnType, t.declaringType, node);
        } else {
            handleCompleteExpression(node, returnType, t.declaringType);
        }
        scope.forgetCurrentNode();
    }

    @Override
    public void visitMethodPointerExpression(MethodPointerExpression node) {
        boolean shouldContinue = handleSimpleExpression(node);
        if (shouldContinue) {
            completeExpressionStack.add(node);
            scopes.getLast().setCurrentNode(node);
            super.visitMethodPointerExpression(node);

            // clean up the stacks
            scopes.getLast().forgetCurrentNode();
            completeExpressionStack.removeLast();
            ClassNode returnType = dependentTypeStack.removeLast();
            Tuple callParamTypes = dependentDeclarationStack.removeLast();

            // try to set Closure generics
            if (!primaryTypeStack.isEmpty() && callParamTypes.declaration instanceof MethodNode) {
                GroovyUtils.updateClosureWithInferredTypes(primaryTypeStack.getLast(),
                    returnType, ((MethodNode) callParamTypes.declaration).getParameters());
            }
        }
    }

    @Override
    public void visitNotExpression(NotExpression node) {
        boolean shouldContinue = handleSimpleExpression(node);
        if (shouldContinue) {
            super.visitNotExpression(node);
        }
    }

    @Override
    public void visitPostfixExpression(PostfixExpression node) {
        visitUnaryExpression(node, node.getExpression(), node.getOperation().getText());
    }

    @Override
    public void visitPrefixExpression(PrefixExpression node) {
        visitUnaryExpression(node, node.getExpression(), node.getOperation().getText());
    }

    @Override
    public void visitPropertyExpression(PropertyExpression node) {
        scopes.getLast().setCurrentNode(node);

        completeExpressionStack.add(node);
        node.getObjectExpression().visit(this);

        ClassNode preSpreadType = null;
        if (node.isSpreadSafe()) {
            preSpreadType = primaryTypeStack.removeLast();
            // property access targets the element type of the object expression
            primaryTypeStack.add(VariableScope.extractSpreadType(preSpreadType));
        }
        node.getProperty().visit(this);

        // don't care about either of these
        completeExpressionStack.removeLast();
        dependentDeclarationStack.removeLast();
        // this is the type of the property expression
        ClassNode exprType = dependentTypeStack.removeLast();
        if (node.isSpreadSafe()) {
            exprType = createSpreadResult(exprType, preSpreadType);
        }
        handleCompleteExpression(node, exprType, null);

        scopes.getLast().forgetCurrentNode();
    }

    @Override
    public void visitRangeExpression(RangeExpression node) {
        if (isDependentExpression(node)) {
            primaryTypeStack.removeLast();
        }
        scopes.getLast().setCurrentNode(node);
        completeExpressionStack.add(node);
        super.visitRangeExpression(node);
        ClassNode eltType = primaryTypeStack.removeLast();
        completeExpressionStack.removeLast();
        ClassNode rangeType = createParameterizedRange(eltType);
        handleCompleteExpression(node, rangeType, null);
        scopes.getLast().forgetCurrentNode();
    }

    @Override
    public void visitReturnStatement(ReturnStatement node) {
        boolean shouldContinue = handleStatement(node);
        if (shouldContinue) {
            ClosureExpression closure = scopes.getLast().getEnclosingClosure();
            if (closure == null || closure.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE) != null) {
                super.visitReturnStatement(node);
            } else { // capture return type
                completeExpressionStack.add(node);
                super.visitReturnStatement(node);
                completeExpressionStack.removeLast();
                ClassNode returnType = primaryTypeStack.removeLast();
                ClassNode closureType = (ClassNode) closure.putNodeMetaData("returnType", returnType);
                if (closureType != null && !closureType.equals(returnType)) {
                    closure.putNodeMetaData("returnType", WideningCategories.lowestUpperBound(closureType, returnType));
                }
            }
        }
    }

    @Override
    public void visitShortTernaryExpression(ElvisOperatorExpression node) {
        if (isDependentExpression(node)) {
            primaryTypeStack.removeLast();
        }
        // arbitrarily, we choose the if clause to be the type of this expression
        completeExpressionStack.add(node);
        node.getTrueExpression().visit(this);

        // the declaration itself is the property node
        ClassNode exprType = primaryTypeStack.removeLast();
        completeExpressionStack.removeLast();
        node.getFalseExpression().visit(this);
        handleCompleteExpression(node, exprType, null);
    }

    @Override
    public void visitSpreadExpression(SpreadExpression node) {
        boolean shouldContinue = handleSimpleExpression(node);
        if (shouldContinue) {
            super.visitSpreadExpression(node);
        }
    }

    @Override
    public void visitSpreadMapExpression(SpreadMapExpression node) {
        boolean shouldContinue = handleSimpleExpression(node);
        if (shouldContinue) {
            super.visitSpreadMapExpression(node);
        }
    }

    @Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression node) {
        ClassNode type = node.getOwnerType();
        if (isPrimaryExpression(node)) {
            visitMethodCallExpression(new MethodCallExpression(new ClassExpression(type), node.getMethod(), node.getArguments()));
        }
        boolean shouldContinue = handleSimpleExpression(node);
        if (shouldContinue) {
            boolean isPresentInSource = (node.getEnd() > 0);
            if (isPresentInSource) {
                visitClassReference(type);
            }
            if (isPresentInSource || isEnumInit(node)) {
                // visit static method call arguments
                super.visitStaticMethodCallExpression(node);
                // visit anonymous inner class members
                if (GroovyUtils.isAnonymous(type)) {
                    visitMethodOverrides(type);
                }
            }
        }
    }

    @Override
    public void visitTernaryExpression(TernaryExpression node) {
        if (isDependentExpression(node)) {
            primaryTypeStack.removeLast();
        }
        completeExpressionStack.add(node);

        node.getBooleanExpression().visit(this);

            Map<String, ClassNode[]> types = inferInstanceOfType(node.getBooleanExpression(), scopes.getLast());
            scopes.add(new VariableScope(scopes.getLast(), node.getTrueExpression(), false));
            for (Map.Entry<String, ClassNode[]> entry : types.entrySet()) {
                if (entry.getValue().length > 0 && entry.getValue()[0] != null) {
                    scopes.getLast().updateVariableSoft(entry.getKey(), entry.getValue()[0]);
                }
            }

        node.getTrueExpression().visit(this);
        // arbitrarily choose the 'true' expression
        // to hold the type of the ternary expression
        ClassNode exprType = primaryTypeStack.removeLast();

            scopes.removeLast().bubbleUpdates();

            scopes.add(new VariableScope(scopes.getLast(), node.getFalseExpression(), false));
            for (Map.Entry<String, ClassNode[]> entry : types.entrySet()) {
                if (entry.getValue().length > 1 && entry.getValue()[1] != null) {
                    scopes.getLast().updateVariableSoft(entry.getKey(), entry.getValue()[1]);
                }
            }

        node.getFalseExpression().visit(this);
        completeExpressionStack.removeLast();

            scopes.removeLast().bubbleUpdates();

        // if the ternary expression is a primary expression
        // of a larger expression, use the exprType as the
        // primary of the next expression
        handleCompleteExpression(node, exprType, null);
    }

    /**
     * <ul>
     * <li> argument list (named or positional)
     * <li> chained assignment, as in: {@code a = b = 1}
     * <li> multi-assignment, as in: {@code def (a, b) = [1, 2]}
     * </ul>
     */
    @Override
    public void visitTupleExpression(TupleExpression node) {
        boolean shouldContinue = handleSimpleExpression(node);
        if (shouldContinue && isNotEmpty(node.getExpressions())) {
            // prevent revisit of statically-compiled chained assignment nodes
            if (node instanceof ArgumentListExpression || node.getExpression(node.getExpressions().size() - 1) instanceof NamedArgumentListExpression) {
                super.visitTupleExpression(node);
            }
        }
    }

    private void visitUnaryExpression(Expression node, Expression operand, String operation) {
        scopes.getLast().setCurrentNode(node);
        completeExpressionStack.add(node);
        if (isDependentExpression(node)) {
            primaryTypeStack.removeLast();
        }
        operand.visit(this);

        ClassNode primaryType = primaryTypeStack.removeLast();
        // now infer the type of the (possibly overloaded) operator
        String associatedMethod = findUnaryOperatorName(operation);
        ClassNode completeExprType;
        if (associatedMethod == null && primaryType.equals(VariableScope.NUMBER_CLASS_NODE) ||
                ClassHelper.getWrapper(primaryType).isDerivedFrom(VariableScope.NUMBER_CLASS_NODE)) {
            completeExprType = primaryType;
        } else {
            // there is an overloadable method associated with this operation
            // convert to a constant expression and infer type
            VariableScope scope = scopes.getLast();
            scope.setMethodCallArgumentTypes(Collections.emptyList());
            TypeLookupResult result = lookupExpressionType(new ConstantExpression(associatedMethod), primaryType, false, scope);

            completeExprType = result.type;
        }
        completeExpressionStack.removeLast();
        handleCompleteExpression(node, completeExprType, null);
    }

    @Override
    public void visitUnaryMinusExpression(UnaryMinusExpression node) {
        visitUnaryExpression(node, node.getExpression(), "-");
    }

    @Override
    public void visitUnaryPlusExpression(UnaryPlusExpression node) {
        visitUnaryExpression(node, node.getExpression(), "+");
    }

    @Override
    public void visitVariableExpression(VariableExpression node) {
        // check for transformed expression (see MethodCallExpressionTransformer.transformToMopSuperCall)
        Expression orig = node.getNodeMetaData(ORIGINAL_EXPRESSION);
        if (orig != null) {
            orig.visit(this);
        }

        scopes.getLast().setCurrentNode(node);
        visitAnnotations(node);
        if (node.getAccessedVariable() == node) {
            // this is a declaration
            visitClassReference(node.getOriginType());
        }
        handleSimpleExpression(node);
        scopes.getLast().forgetCurrentNode();
    }

    //--------------------------------------------------------------------------

    private boolean handleStatement(Statement node) {
        VariableScope scope = scopes.getLast();
        ClassNode declaring = scope.getDelegateOrThis();

        if (node instanceof BlockStatement) {
            for (ITypeLookup lookup : lookups) {
                if (lookup instanceof ITypeLookupExtension) {
                    // ensure that declaring type information is invoked at start of the block
                    ((ITypeLookupExtension) lookup).lookupInBlock((BlockStatement) node, scope);
                }
            }
        }

        // don't check the lookups because statements have no type;
        // but individual requestors may choose to end the visit here
        TypeLookupResult noLookup = new TypeLookupResult(declaring, declaring, declaring, TypeConfidence.EXACT, scope);
        VisitStatus status = notifyRequestor(node, requestor, noLookup);
        switch (status) {
        case CONTINUE:
            return true;
        case CANCEL_BRANCH:
            return false;
        case CANCEL_MEMBER:
        case STOP_VISIT:
        default:
            throw new VisitCompleted(status);
        }
    }

    private boolean handleParameterList(Parameter[] params) {
        if (params != null) {
            VariableScope scope = scopes.getLast();
            scope.setPrimaryNode(false);
            for (Parameter param : params) {
                if (!scope.containsInThisScope(param.getName())) {
                    scope.addVariable(param);
                }

                TypeLookupResult result = null;
                for (ITypeLookup lookup : lookups) {
                    TypeLookupResult candidate = lookup.lookupType(param, scope);
                    if (candidate != null) {
                        if (result == null || result.confidence.isLessThan(candidate.confidence)) {
                            result = candidate;
                        }
                        if (result.confidence.isAtLeast(TypeConfidence.INFERRED)) {
                            break;
                        }
                    }
                }

                TypeLookupResult parameterResult = new TypeLookupResult(result.type, result.declaringType, param, TypeConfidence.EXACT, scope);
                VisitStatus status = notifyRequestor(param, requestor, parameterResult);
                switch (status) {
                case CONTINUE:
                    break;
                case CANCEL_BRANCH:
                    return false;
                case CANCEL_MEMBER:
                case STOP_VISIT:
                    throw new VisitCompleted(status);
                }

                visitAnnotations(param);
                visitClassReference(param.getOriginType());
                Expression init = Optional.ofNullable(param.getInitialExpression()).orElse(param.getNodeMetaData(Verifier.INITIAL_EXPRESSION));
                if (init != null) {
                    init.visit(this);
                }
            }
        }
        return true;
    }

    private boolean handleSimpleExpression(Expression node) {
        ClassNode primaryType; boolean isStatic;
        VariableScope scope = scopes.getLast();
        if (!isDependentExpression(node)) {
            primaryType = null;
            isStatic = false;
            scope.setMethodCallArgumentTypes(getMethodCallArgumentTypes(node));
            scope.setMethodCallGenericsTypes(getMethodCallGenericsTypes(node));
        } else {
            primaryType = primaryTypeStack.removeLast();
            // implicit this (no obj expr) method calls do not have a primary type
            if (completeExpressionStack.getLast() instanceof MethodCallExpression &&
                    ((MethodCallExpression) completeExpressionStack.getLast()).isImplicitThis()) {
                primaryType = null;
            }
            isStatic = hasStaticObjectExpression(node, primaryType);
            scope.setMethodCallArgumentTypes(getMethodCallArgumentTypes(completeExpressionStack.getLast()));
            scope.setMethodCallGenericsTypes(getMethodCallGenericsTypes(completeExpressionStack.getLast()));
        }
        scope.setPrimaryNode(primaryType == null);
        scope.getWormhole().put("enclosingAssignment", enclosingAssignment);

        TypeLookupResult result = lookupExpressionType(node, primaryType, isStatic, scope);
        return handleRequestor(node, primaryType, result);
    }

    private void    handleCompleteExpression(Expression node, ClassNode exprType, ClassNode declaringType) {
        VariableScope scope = scopes.getLast();
        scope.setPrimaryNode(false);
        handleRequestor(node, declaringType, new TypeLookupResult(exprType, declaringType, node, TypeConfidence.EXACT, scope));
    }

    private boolean handleRequestor(Expression node, ClassNode primaryType, TypeLookupResult result) {
        result.enclosingAssignment = enclosingAssignment;
        VisitStatus status = requestor.acceptASTNode(node, result, enclosingElement);
        VariableScope scope = scopes.getLast();
        scope.setMethodCallArgumentTypes(null);
        scope.setMethodCallGenericsTypes(null);

        // when there is a category method, we don't want to store it
        // as the declaring type since this will mess things up inside closures
        ClassNode rememberedDeclaringType = result.declaringType;
        if (scope.getCategoryNames().contains(rememberedDeclaringType)) {
            rememberedDeclaringType = (primaryType != null ? primaryType : scope.getDelegateOrThis());
        }
        if (rememberedDeclaringType == null) {
            rememberedDeclaringType = VariableScope.OBJECT_CLASS_NODE;
        }
        switch (status) {
        case CONTINUE:
            postVisit(node, result.type, rememberedDeclaringType, result.declaration);
            return true;
        case CANCEL_BRANCH:
            postVisit(node, result.type, rememberedDeclaringType, result.declaration);
            return false;
        case CANCEL_MEMBER:
        case STOP_VISIT:
            throw new VisitCompleted(status);
        }
        // won't get here
        return false;
    }

    //

    private IType findAnonType(ClassNode type) {
        int occurrenceCount = occurrenceCounts.computeIfAbsent(
            enclosingElement, x -> new AtomicInteger()).incrementAndGet();
        try {
            for (IJavaElement child : ((IMember) enclosingElement).getChildren()) {
                if (child instanceof IType && ((IType) child).isAnonymous() &&
                          ((IType) child).getOccurrenceCount() == occurrenceCount) {
                    return (IType) child;
                }
            }
        } catch (JavaModelException e) {
            log(e, "Error visiting children of %s", type.getName());
        }
        throw new GroovyEclipseBug("Failed to locate anon. type " + type.getName());
    }

    private ClassNode findClassNode(String name) {
        for (ClassNode clazz : enclosingModule.getClasses()) {
            if (clazz.getNameWithoutPackage().equals(name)) {
                return clazz;
            }
        }
        return null;
    }

    private FieldNode findFieldNode(IField field) {
        ClassNode clazz = findClassNode(createName(field.getDeclaringType()));
        FieldNode fieldNode = clazz.getField(field.getElementName());
        if (fieldNode == null) {
            // GRECLIPSE-578: might be @Lazy; name is changed
            fieldNode = clazz.getField("$" + field.getElementName());
        }
        return fieldNode;
    }

    private MethodNode findMethodNode(IMethod method) {
        ClassNode clazz = findClassNode(createName(method.getDeclaringType()));
        try {
            if (method.isConstructor()) {
                List<ConstructorNode> constructors = clazz.getDeclaredConstructors();
                if (constructors == null || constructors.isEmpty()) {
                    return null;
                }
                String[] jdtParamTypes = method.getParameterTypes();
                if (jdtParamTypes == null) jdtParamTypes = NO_PARAMS;
                outer: for (ConstructorNode constructorNode : constructors) {
                    Parameter[] groovyParams = constructorNode.getParameters();
                    if (groovyParams == null) groovyParams = NO_PARAMETERS;

                    // ignore implicit parameters of constructors for enums or inner types
                    int implicitParamCount = 0;
                    if (method.getDeclaringType().isEnum()) implicitParamCount = 2;
                    if (groovyParams.length > 0 && groovyParams[0].getName().startsWith("$")) implicitParamCount = 1;
                    if (implicitParamCount > 0) {
                        Parameter[] newGroovyParams = new Parameter[groovyParams.length - implicitParamCount];
                        System.arraycopy(groovyParams, implicitParamCount, newGroovyParams, 0, newGroovyParams.length);
                        groovyParams = newGroovyParams;
                    }

                    if (groovyParams.length != jdtParamTypes.length) {
                        continue;
                    }
                    inner: for (int i = 0, n = groovyParams.length; i < n; i += 1) {
                        String simpleGroovyClassType = GroovyUtils.getTypeSignature(groovyParams[i].getType(), false, false);
                        if (simpleGroovyClassType.equals(jdtParamTypes[i])) {
                            continue inner;
                        }
                        String groovyClassType = GroovyUtils.getTypeSignature(groovyParams[i].getType(), true, false);
                        if (!groovyClassType.equals(jdtParamTypes[i])) {
                            continue outer;
                        }
                    }
                    return constructorNode;
                }
                String params = "";
                if (jdtParamTypes.length > 0) {
                    params = Arrays.toString(jdtParamTypes);
                    params = params.substring(1, params.length() - 1);
                }
                System.err.printf("%s.findMethodNode: no match found for %s(%s)%n",
                    getClass().getSimpleName(), clazz.getName(), params);
                // no match found, just return the first
                return constructors.get(0);
            } else {
                List<MethodNode> methods = findMethods(clazz, method.getElementName());
                if (methods.isEmpty()) {
                    return null;
                }
                String[] jdtParamTypes = method.getParameterTypes();
                if (jdtParamTypes == null) jdtParamTypes = NO_PARAMS;

                Expression targetExpr; // append implicit parameter for @Category method
                if ((method.getFlags() & Flags.AccStatic) == 0 && (targetExpr = findCategoryTarget(clazz)) != null) {
                    TypeLookupResult result = lookupExpressionType(targetExpr, null, true, scopes.getLast());
                    ClassNode targetType = result.type.getGenericsTypes()[0].getType();

                    String typeSignature = GroovyUtils.getTypeSignature(targetType, false, false);
                    jdtParamTypes = (String[]) ArrayUtils.add(jdtParamTypes, 0, typeSignature);
                }

                if (methods.size() > 1) {
                    methods.sort(Comparator.comparingInt(m -> m.getParameters().length));
                }

                outer: for (MethodNode methodNode : methods) {
                    Parameter[] groovyParams = methodNode.getParameters();
                    if (groovyParams == null) groovyParams = NO_PARAMETERS;
                    if (groovyParams.length < jdtParamTypes.length) {
                        continue;
                    }
                    inner: for (int i = 0, n = jdtParamTypes.length; i < n; i += 1) {
                        String simpleGroovyClassType = GroovyUtils.getTypeSignature(groovyParams[i].getType(), false, false);
                        if (simpleGroovyClassType.equals(jdtParamTypes[i])) {
                            continue inner;
                        }
                        String groovyClassType = GroovyUtils.getTypeSignature(groovyParams[i].getType(), true, false);
                        if (!groovyClassType.equals(jdtParamTypes[i])) {
                            continue outer;
                        }
                    }
                    return methodNode;
                }
                String params = "";
                if (jdtParamTypes.length > 0) {
                    params = Arrays.toString(jdtParamTypes);
                    params = params.substring(1, params.length() - 1);
                }
                System.err.printf("%s.findMethodNode: no match found for %s.%s(%s)%n",
                    getClass().getSimpleName(), clazz.getName(), method.getElementName(), params);
                // no match found, just return the first
                return methods.get(0);
            }
        } catch (JavaModelException e) {
            log(e, "Error finding method %s in class %s", method.getElementName(), clazz.getName());
        }
        // probably happened due to a syntax error in the code or an AST transformation
        return null;
    }

    private List<MethodNode> findMethods(ClassNode classNode, String methodName) {
        List<MethodNode> methods = classNode.getMethods(methodName);
        if (methods.isEmpty()) {
            if (isNotEmpty(GroovyUtils.getAnnotations(classNode, "org.spockframework.runtime.model.SpecMetadata"))) {
                methods = classNode.getMethods().stream().filter(mn -> {
                    Optional<AnnotationNode> opt = GroovyUtils.getAnnotations(mn, "org.spockframework.runtime.model.FeatureMetadata").findFirst();
                    return methodName.equals(opt.map(an -> an.getMember("name")).map(Expression::getText).orElse(null));
                }).collect(Collectors.toList());
            }
        }
        return methods;
    }

    private MethodNode findLazyMethod(String fieldName) {
        ClassNode classNode = (ClassNode) enclosingDeclarationNode;
        return classNode.getDeclaredMethod("get" + MetaClassHelper.capitalize(fieldName), NO_PARAMETERS);
    }

    private List<ClassNode> getMethodCallArgumentTypes(ASTNode node) {
        if (node instanceof MethodCall) {
            Expression arguments = ((MethodCall) node).getArguments();
            if (arguments instanceof ArgumentListExpression) {
                List<Expression> expressions = ((ArgumentListExpression) arguments).getExpressions();
                if (isNotEmpty(expressions)) {
                    List<ClassNode> types = new ArrayList<>(expressions.size());
                    for (Expression expression : expressions) {
                        ClassNode exprType = expression.getType();
                        /*if (expression instanceof ClosureExpression) {
                            types.add(VariableScope.CLOSURE_CLASS_NODE);
                        } else if (expression instanceof MapExpression) {
                            types.add(VariableScope.MAP_CLASS_NODE);
                        } else if (expression instanceof ListExpression) {
                            types.add(VariableScope.LIST_CLASS_NODE);
                        } else*/ if (expression instanceof ClassExpression) {
                            types.add(VariableScope.newClassClassNode(exprType));
                        } else if (expression instanceof CastExpression || expression instanceof ConstructorCallExpression) {
                            types.add(exprType);
                        } else if (expression instanceof ConstantExpression && ((ConstantExpression) expression).isNullExpression()) {
                            types.add(VariableScope.NULL_TYPE); // sentinel for wildcard matching
                        } else if (expression instanceof VariableExpression && expression.getText().endsWith(Traits.THIS_OBJECT) && Traits.isTrait(expression.getType())) {
                            continue; // skip synthetic this argument expression
                        } else if (ClassHelper.isNumberType(exprType) || VariableScope.BIG_DECIMAL_CLASS.equals(exprType) || VariableScope.BIG_INTEGER_CLASS.equals(exprType)) {
                            types.add(GroovyUtils.getWrapperTypeIfPrimitive(exprType));
                        } else if (expression instanceof GStringExpression || (expression instanceof ConstantExpression && ((ConstantExpression) expression).isEmptyStringExpression())) {
                            types.add(VariableScope.STRING_CLASS_NODE);
                        } else if (expression instanceof BooleanExpression || (expression instanceof ConstantExpression && (((ConstantExpression) expression).isTrueExpression() || ((ConstantExpression) expression).isFalseExpression()))) {
                            types.add(VariableScope.BOOLEAN_CLASS_NODE);
                        } else {
                            scopes.getLast().setMethodCallArgumentTypes(getMethodCallArgumentTypes(expression));

                            TypeLookupResult tlr;
                            if (!(expression instanceof MethodCallExpression)) {
                                tlr = lookupExpressionType(expression, null, false, scopes.getLast());
                            } else {
                                MethodCallExpression call = (MethodCallExpression) expression;
                                tlr = lookupExpressionType(call.getObjectExpression(), null, false, scopes.getLast());
                                tlr = lookupExpressionType(call.getMethod(), tlr.type, call.getObjectExpression() instanceof ClassExpression, scopes.getLast());
                            }

                            types.add(tlr.type);
                        }
                    }
                    return types;
                }
            }
            // TODO: Might be useful to look into TupleExpression
            return Collections.emptyList();
        }

        // "bar = 123" may refer to "setBar(x)"
        if (node instanceof VariableExpression && node == scopes.getLast().getWormhole().get("lhs")) {
            VariableScope.VariableInfo info = scopes.getLast().lookupName(node.getText());
            return (info != null ? Collections.singletonList(info.type) : null);
        }

        // "foo.bar = 123" may refer to "setBar(x)"
        while (node instanceof PropertyExpression) {
            node = ((PropertyExpression) node).getProperty();
        }
        if (node == scopes.getLast().getWormhole().get("lhs")) {
            ClassNode rhsType = node.getNodeMetaData("rhsType");
            if (rhsType != null) return Collections.singletonList(rhsType);
        }

        return null;
    }

    private GenericsType[] getMethodCallGenericsTypes(ASTNode node) {
        GenericsType[] generics = null;
        if (node instanceof MethodCallExpression) {
            generics = ((MethodCallExpression) node).getGenericsTypes();
        }/* else if (node instanceof ConstructorCallExpression) {
            generics = ((ConstructorCallExpression) node).getGenericsTypes();
        } else if (node instanceof StaticMethodCallExpression) {
            generics = ((StaticMethodCallExpression) node).getGenericsTypes();
        }*/
        return generics;
    }

    private ClassNode[] inferClosureParamTypes(ClosureExpression node, VariableScope scope) {
        if (node.getParameters() == null) { // i.e. "{ -> ... }"
            return ClassNode.EMPTY_ARRAY;
        }
        ClassNode primaryType = null;

        ClassNode[] inferredTypes = new ClassNode[node.isParameterSpecified() ? node.getParameters().length : 1];

        VariableScope.CallAndType cat = scope.getEnclosingMethodCallExpression();
        if (cat != null && cat.declaration instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) cat.declaration;
            Parameter methodParam = findTargetParameter(node, cat.call, methodNode,
                !methodNode.getDeclaringClass().equals(cat.getPerceivedDeclaringType()));
            if (methodParam != null) {
                if (VariableScope.CLOSURE_CLASS_NODE.equals(methodParam.getType())) {
                    GroovyUtils.getAnnotations(methodParam, VariableScope.CLOSURE_PARAMS.getName()).findFirst().ifPresent(cp -> {
                        SourceUnit sourceUnit = enclosingModule.getContext();
                        try {
                            @SuppressWarnings("unchecked")
                            Class<? extends ClosureSignatureHint> hint = (Class<? extends ClosureSignatureHint>) StaticTypeCheckingSupport.evaluateExpression(GeneralUtils.castX(VariableScope.CLASS_CLASS_NODE, cp.getMember("value")), sourceUnit.getConfiguration());
                            String[] opts = (String[]) (cp.getMember("options") == null ? ClosureParams.class.getMethod("options").getDefaultValue() : StaticTypeCheckingSupport.evaluateExpression(GeneralUtils.castX(VariableScope.STRING_CLASS_NODE.makeArray(), cp.getMember("options")), sourceUnit.getConfiguration()));

                            // determine closure param types from ClosureSignatureHint
                            List<ClassNode[]> sigs = hint.newInstance().getClosureSignatures(methodNode, sourceUnit, resolver.compilationUnit, opts, cat.call);
                            if (isNotEmpty(sigs)) {
                                for (ClassNode[] sig : sigs) {
                                    if (sig.length == inferredTypes.length) {
                                        GenericsType[] generics = getMethodCallGenericsTypes(cat.call);
                                        List<ClassNode> arguments = GroovyUtils.getParameterTypes(methodNode.getParameters());
                                        GenericsMapper map = GenericsMapper.gatherGenerics(arguments, cat.declaringType, methodNode.getOriginal(), generics);

                                        for (int i = 0, n = sig.length; i < n; i += 1) {
                                            // TODO: If result still has generics, use Object or ???
                                            inferredTypes[i] = VariableScope.resolveTypeParameterization(map, VariableScope.clone(sig[i]));
                                        }

                                        break; // TODO: What if more than one signature matches parameter count?
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log(e, "Error processing @ClosureParams of %s", methodNode.getTypeDescriptor());
                        }
                    });
                }

                if (inferredTypes[0] == null) {
                    primaryType = methodParam.getType();
                    if (GenericsMapper.isVargs(methodNode.getParameters()) &&
                            DefaultGroovyMethods.last(methodNode.getParameters()) == methodParam) {
                        primaryType = primaryType.getComponentType();
                    }
                }
            }
        } else if (!completeExpressionStack.isEmpty() && completeExpressionStack.getLast() instanceof CastExpression &&
                          ((CastExpression) completeExpressionStack.getLast()).getExpression() == node) {
            primaryType = ((CastExpression) completeExpressionStack.getLast()).getType();
        } else if (enclosingAssignment != null && enclosingAssignment.getRightExpression() == node) {
            primaryType = enclosingAssignment.getLeftExpression().getType();
        }

        if (inferredTypes[0] == null) {
            int i = 0; MethodNode sam; // check for SAM-type coercion of closure expression
            if (primaryType != null && (sam = ClassHelper.findSAM(primaryType)) != null) {
                for (ClassNode t : GroovyUtils.getParameterTypes(sam.getParameters())) {
                    if (i == inferredTypes.length) break;
                    inferredTypes[i++] = t;
                }
            }
            Arrays.fill(inferredTypes, i, inferredTypes.length, VariableScope.OBJECT_CLASS_NODE);
        }

        return inferredTypes;
    }

    private ClassNode isCategoryDeclaration(MethodCallExpression node, VariableScope scope) {
        if ("use".equals(node.getMethodAsString())) {
            if (node.getArguments() instanceof ArgumentListExpression) {
                ArgumentListExpression args = (ArgumentListExpression) node.getArguments();
                if (args.getExpressions().size() >= 2 && args.getExpressions().get(1) instanceof ClosureExpression) {
                    // really, should be doing inference on the first expression and seeing if it
                    // is a class node, but looking up in scope is good enough for now
                    Expression expr = args.getExpressions().get(0);
                    if (expr instanceof ClassExpression) {
                        return expr.getType();
                    } else if (expr instanceof VariableExpression && expr.getText() != null) {
                        VariableScope.VariableInfo info = scope.lookupName(expr.getText());
                        if (info != null) {
                            // info.type should be Class<Category>
                            return info.type.getGenericsTypes()[0].getType();
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Dependent (type depends on another expression) expressions are:
     * <ul>
     * <li>field (ie- right side) of an attribute expression
     * <li>property (ie- right side) of a property expression
     * <li>method (ie- right side) of a method call expression
     * <li>method (ie- right side) of a method pointer expression
     * <li>alias expression of an import statement or field expression of a static import
     * </ul>
     *
     * Note that for statements and ternary expressions do not have any dependent
     * expression even though they have primary expressions.
     *
     * @return true iff the expression is a dependent expression in an expression group
     */
    private boolean isDependentExpression(Expression expr) {
        if (!completeExpressionStack.isEmpty()) {
            ASTNode node = completeExpressionStack.getLast();

            if (node instanceof PropertyExpression) {
                return ((PropertyExpression) node).getProperty() == expr;

            } else if (node instanceof MethodCallExpression) {
                return ((MethodCallExpression) node).getMethod() == expr;

            } else if (node instanceof MethodPointerExpression) {
                return ((MethodPointerExpression) node).getMethodName() == expr;

            } else if (node instanceof ImportNode) {
                return ((ImportNode) node).getAliasExpr() == expr ||
                        ((ImportNode) node).getFieldNameExpr() == expr;
            }
        }
        return false;
    }

    /**
     * Primary expressions are:
     * <ul>
     * <li>right side of an assignment expression
     * <li>either side of a non-assignment binary expression
     * <li>object (ie- left side) of a property expression
     * <li>object (ie- left side) of a method call expression
     * <li>object (ie- left side) of an attribute expression
     * <li>collection expression of a for statement
     * <li>true expression of a ternary expression
     * <li>first element of a list expression (the first element is assumed to be representative of all list elements)
     * <li>first element of a range expression (the first element is assumed to be representative of the range)
     * <li>either the key or the value expression of a {@link MapEntryExpression}
     * <li>first {@link MapEntryExpression} of a {@link MapExpression}
     * <li>expression of a {@link PrefixExpression}, {@link PostfixExpression},
     *     {@link UnaryMinusExpression}, {@link UnaryPlusExpression}, or {@link BitwiseNegationExpression}
     * </ul>
     *
     * @return true iff the expression is a primary expression in an expression group
     */
    private boolean isPrimaryExpression(Expression expr) {
        if (!completeExpressionStack.isEmpty()) {
            ASTNode node = completeExpressionStack.getLast();

            if (node instanceof PropertyExpression) {
                return ((PropertyExpression) node).getObjectExpression() == expr;

            } else if (node instanceof MethodCallExpression) {
                return ((MethodCallExpression) node).getObjectExpression() == expr;

            } else if (node instanceof MethodPointerExpression) {
                return ((MethodPointerExpression) node).getExpression() == expr;

            } else if (node instanceof BinaryExpression) {
                BinaryExpression bexp = (BinaryExpression) node;
                // both sides of the binary expression are primary since we need
                // access to both of them when inferring binary expression types
                if (bexp.getLeftExpression() == expr || bexp.getRightExpression() == expr) {
                    return true;
                }
                // statically-compiled assignment chains need a little help
                if (!(expr instanceof TupleExpression) &&
                        bexp.getRightExpression() instanceof ListOfExpressionsExpression) {
                    List<Expression> list = ReflectionUtils.getPrivateField(
                        ListOfExpressionsExpression.class, "expressions", bexp.getRightExpression());
                    // list.get(0) should be TemporaryVariableExpression
                    return (expr != list.get(1) && list.get(1) instanceof MethodCallExpression);
                }

            } else if (node instanceof AttributeExpression) {
                return ((AttributeExpression) node).getObjectExpression() == expr;

            } else if (node instanceof TernaryExpression) {
                return ((TernaryExpression) node).getTrueExpression() == expr;

            } else if (node instanceof ForStatement) {
                // this check is used to store the type of the collection expression
                // so that it can be assigned to the for loop variable
                return ((ForStatement) node).getCollectionExpression() == expr;

            } else if (node instanceof ReturnStatement) {
                return ((ReturnStatement) node).getExpression() == expr;

            } else if (node instanceof ListExpression) {
                return isNotEmpty(((ListExpression) node).getExpressions()) &&
                        ((ListExpression) node).getExpression(0) == expr;

            } else if (node instanceof RangeExpression) {
                return ((RangeExpression) node).getFrom() == expr;

            } else if (node instanceof MapEntryExpression) {
                return ((MapEntryExpression) node).getKeyExpression() == expr ||
                        ((MapEntryExpression) node).getValueExpression() == expr;

            } else if (node instanceof MapExpression) {
                return isNotEmpty(((MapExpression) node).getMapEntryExpressions()) &&
                        ((MapExpression) node).getMapEntryExpressions().get(0) == expr;

            } else if (node instanceof PrefixExpression) {
                return ((PrefixExpression) node).getExpression() == expr;

            } else if (node instanceof PostfixExpression) {
                return ((PostfixExpression) node).getExpression() == expr;

            } else if (node instanceof UnaryPlusExpression) {
                return ((UnaryPlusExpression) node).getExpression() == expr;

            } else if (node instanceof UnaryMinusExpression) {
                return ((UnaryMinusExpression) node).getExpression() == expr;

            } else if (node instanceof BitwiseNegationExpression) {
                return ((BitwiseNegationExpression) node).getExpression() == expr;
            }
        }
        return false;
    }

    /**
     * @return true iff the object expression associated with node is a static reference to a class declaration
     */
    private boolean hasStaticObjectExpression(Expression node, ClassNode primaryType) {
        boolean staticObjectExpression = false;
        if (!completeExpressionStack.isEmpty()) {
            ASTNode complete = completeExpressionStack.getLast();
            if (complete instanceof PropertyExpression || complete instanceof MethodCallExpression || complete instanceof MethodPointerExpression) {
                // call getObjectExpression and isImplicitThis w/o common interface
                Expression objectExpression = null;
                boolean isImplicitThis = false;

                if (complete instanceof PropertyExpression) {
                    PropertyExpression prop = (PropertyExpression) complete;
                    objectExpression = prop.getObjectExpression();
                    isImplicitThis = prop.isImplicitThis();
                } else if (complete instanceof MethodCallExpression) {
                    MethodCallExpression call = (MethodCallExpression) complete;
                    objectExpression = call.getObjectExpression();
                    isImplicitThis = call.isImplicitThis();
                } else if (complete instanceof MethodPointerExpression) {
                    MethodPointerExpression expr = (MethodPointerExpression) complete;
                    objectExpression = expr.getExpression();
                }

                if (objectExpression == null && isImplicitThis) {
                    staticObjectExpression = scopes.getLast().isStatic();
                } else if (objectExpression instanceof ClassExpression || VariableScope.CLASS_CLASS_NODE.equals(primaryType)) {
                    staticObjectExpression = true; // separate lookup exists for non-static members of Class, Object, or GroovyObject
                }
            }
        }
        return staticObjectExpression;
    }

    private TypeLookupResult lookupExpressionType(Expression node, ClassNode objExprType, boolean isStatic, VariableScope scope) {
        TypeLookupResult result = null;
        for (ITypeLookup lookup : lookups) {
            TypeLookupResult candidate;
            if (lookup instanceof ITypeLookupExtension) {
                candidate = ((ITypeLookupExtension) lookup).lookupType(node, scope, objExprType, isStatic);
            } else {
                candidate = lookup.lookupType(node, scope, objExprType);
            }
            if (candidate != null) {
                if (result == null || result.confidence.isLessThan(candidate.confidence)) {
                    result = candidate;
                }
                if (result.confidence.isAtLeast(TypeConfidence.INFERRED)) {
                    break;
                }
            }
        }
        if (result.confidence == TypeConfidence.UNKNOWN && result.declaringType != null &&
                GeneralUtils.isOrImplements(result.declaringType, VariableScope.MAP_CLASS_NODE)) {
            ClassNode inferredType = VariableScope.OBJECT_CLASS_NODE;
            if (node instanceof ConstantExpression && node.getType().equals(VariableScope.STRING_CLASS_NODE)) {
                List<MethodNode> putMethods = result.declaringType.getMethods("put"); // returns the value type
                GenericsMapper mapper = GenericsMapper.gatherGenerics(result.declaringType, result.declaringType.redirect());
                inferredType = VariableScope.resolveTypeParameterization(mapper, VariableScope.clone(putMethods.get(0).getReturnType()));
            }
            TypeLookupResult tlr = new TypeLookupResult(inferredType, result.declaringType, result.declaration, TypeConfidence.INFERRED, result.scope, result.extraDoc);
            tlr.enclosingAnnotation = result.enclosingAnnotation;
            tlr.enclosingAssignment = result.enclosingAssignment;
            tlr.isGroovy = result.isGroovy;
            result = tlr;
        }
        return result.resolveTypeParameterization(objExprType, isStatic);
    }

    private VisitStatus notifyRequestor(ASTNode node, ITypeRequestor requestor, TypeLookupResult result) {
        // result is never null because SimpleTypeLookup always returns non-null
        return requestor.acceptASTNode(node, result, enclosingElement);
    }

    private void postVisit(Expression node, ClassNode type, ClassNode declaringType, ASTNode declaration) {
        if (isPrimaryExpression(node)) {
            assert type != null;
            primaryTypeStack.add(type);
        } else if (isDependentExpression(node)) {
            // TODO: null has been seen here for type; is that okay?
            dependentTypeStack.add(type);
            dependentDeclarationStack.add(new Tuple(declaringType, declaration));
        }
    }

    /**
     * For testing only, ensures that after a visit is complete,
     */
    private void postVisitSanityCheck() {
        Assert.isTrue(completeExpressionStack.isEmpty(), String.format(SANITY_CHECK_MESSAGE, "Expression"));
        Assert.isTrue(primaryTypeStack.isEmpty(), String.format(SANITY_CHECK_MESSAGE, "Primary type"));
        Assert.isTrue(dependentDeclarationStack.isEmpty(), String.format(SANITY_CHECK_MESSAGE, "Declaration"));
        Assert.isTrue(dependentTypeStack.isEmpty(), String.format(SANITY_CHECK_MESSAGE, "Dependent type"));
        Assert.isTrue(scopes.isEmpty(), String.format(SANITY_CHECK_MESSAGE, "Variable scope"));
    }
    private static final String SANITY_CHECK_MESSAGE =
        "Inferencing engine in invalid state after visitor completed.  %s stack should be empty after visit completed.";

    //--------------------------------------------------------------------------

    /**
     * Get the module node. Potentially forces creation of a new module node if the working copy owner is non-default. This is
     * necessary because a non-default working copy owner implies that this may be a search related to refactoring and therefore,
     * the ModuleNode must be based on the most recent working copies.
     */
    private static ModuleNodeInfo createModuleNode(GroovyCompilationUnit unit) {
        if (unit.getOwner() == null || unit.owner == DefaultWorkingCopyOwner.PRIMARY) {
            return unit.getModuleInfo(true);
        } else {
            return unit.getNewModuleInfo();
        }
    }

    /**
     * Creates type name taking into account inner types.
     */
    private static String createName(IType type) {
        StringBuilder sb = new StringBuilder();
        while (type != null) {
            if (sb.length() > 0) {
                sb.insert(0, '$');
            }
            if (type instanceof SourceType && type.getElementName().length() < 1) {
                int count;
                try {
                    count = (Integer) ReflectionUtils.throwableGetPrivateField(SourceType.class, "localOccurrenceCount", (SourceType) type);
                } catch (Exception e) {
                    throw new GroovyEclipseBug(e);
                }
                sb.insert(0, count);
            } else {
                sb.insert(0, type.getElementName());
            }
            type = (IType) type.getParent().getAncestor(IJavaElement.TYPE);
        }
        return sb.toString();
    }

    /**
     * @return a list type parameterized by {@code t}
     */
    private static ClassNode createParameterizedList(ClassNode t) {
        ClassNode list = VariableScope.clonedList();
        list.getGenericsTypes()[0].setType(t);
        list.getGenericsTypes()[0].setName(t.getName());
        return list;
    }

    /**
     * @return a map type parameterized by {@code k} and {@code v}
     */
    private static ClassNode createParameterizedMap(ClassNode k, ClassNode v) {
        ClassNode map = VariableScope.clonedMap();
        map.getGenericsTypes()[0].setType(k);
        map.getGenericsTypes()[0].setName(k.getName());
        map.getGenericsTypes()[1].setType(v);
        map.getGenericsTypes()[1].setName(v.getName());
        return map;
    }

    /**
     * @return a range type parameterized by {@code t}
     */
    private static ClassNode createParameterizedRange(ClassNode t) {
        ClassNode range = VariableScope.clonedRange();
        range.getGenericsTypes()[0].setType(t);
        range.getGenericsTypes()[0].setName(t.getName());
        return range;
    }

    /**
     * @see org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor#adjustTypeForSpreading
     */
    private static ClassNode createSpreadResult(ClassNode t, ClassNode objExprType) {
        ClassNode elementType = VariableScope.extractElementType(objExprType);
        if (GeneralUtils.isOrImplements(elementType, VariableScope.COLLECTION_CLASS_NODE)) {
            // TODO: clone elementType and replace deepest Collection's generic type with t
            t = GenericsUtils.nonGeneric(elementType);
        }
        return createParameterizedList(ClassHelper.getWrapper(t));
    }

    /**
     * Mutually exclusive to {@link #findBinaryOperatorName(String)}.
     *
     * @param operator the operation of this binary expression
     * @param lhs the type of the lhs of the binary expression
     * @param rhs the type of the rhs of the binary expression
     * @return the determined type of the binary expression
     */
    private static ClassNode findBinaryExpressionType(String operator, ClassNode lhs, ClassNode rhs) {
        switch (operator.charAt(0)) {
        case '<':
            if (operator.equals("<=>")) {
                return VariableScope.INTEGER_CLASS_NODE;
            }
        case '>':
        case '!':
            // includes "!=" and "!=="
        case 'i':
            // includes "instanceof"
            return VariableScope.BOOLEAN_CLASS_NODE;
        case '=':
            if (operator.length() > 1) {
                if (operator.charAt(1) == '=') {
                    // includes "==", "===" and "==~"
                    return VariableScope.BOOLEAN_CLASS_NODE;
                } else if (operator.charAt(1) == '~') {
                    return VariableScope.MATCHER_CLASS_NODE;
                }
            }
        }
        return rhs;
    }

    /**
     * @return the method name associated with this binary operator
     */
    private static String findBinaryOperatorName(String text) {
        switch (text.charAt(0)) {
        case '+':
            return "plus";
        case '-':
            return "minus";
        case '*':
            if (text.length() == 1 || text.charAt(1) == '=') {
                return "multiply";
            }
            if (text.length() > 1 && text.charAt(1) == '*') {
                return "power";
            }
            break;
        case '/':
            return "div";
        case '%':
            return "mod";
        case '&':
            return "and";
        case '|':
            return "or";
        case '^':
            return "xor";
        case '>':
            if (text.length() > 1 && text.charAt(1) == '>') {
                return "rightShift"; // or "rightShiftUnsigned"
            }
            break;
        case '<':
            if (text.length() > 1 && text.charAt(1) == '<') {
                return "leftShift";
            }
            break;
        case '[':
            return "getAt"; // or "putAt"
        case 'i':
            if (text.equals("in")) {
                return "isCase";
            }
        }
        return null;
    }

    public static Expression findCategoryTarget(ClassNode node) {
        return GroovyUtils.getAnnotations(node, "groovy.lang.Category")
            .findFirst().map(an -> an.getMember("value")).orElse(null);
    }

    private static Parameter findTargetParameter(Expression arg, MethodCallExpression call, MethodNode declaration, boolean isGroovyMethod) {
        // see ExpressionCompletionRequestor.getParameterPosition(ASTNode, MethodCallExpression)
        // see CompletionNodeFinder.isArgument(Expression, List<? extends Expression>)
        String key = null; int pos = -1;
        if (call.getArguments() instanceof ArgumentListExpression) {
            int idx = -1;
            out: for (Expression exp : (ArgumentListExpression) call.getArguments()) {
                if (exp instanceof MapExpression) {
                    for (MapEntryExpression ent : ((MapExpression) exp).getMapEntryExpressions()) {
                        if (arg == ent.getValueExpression()) {
                            key = ent.getKeyExpression().getText();
                            break out;
                        }
                    }
                } else {
                    idx += 1;
                    if (arg == exp) {
                        pos = idx;
                        break;
                    }
                }
            }
        }
        if (key != null) {
            for (Parameter parameter : declaration.getParameters()) {
                if (parameter.getName().equals(key)) {
                    return parameter;
                }
            }
        } else if (pos != -1) {
            if (isGroovyMethod) pos += 1; // skip self parameter
            Parameter[] parameters = getPositionalParameters(declaration);
            if (pos < parameters.length || GenericsMapper.isVargs(parameters)) {
                return parameters[Math.min(pos, parameters.length - 1)];
            }
        }
        return null;
    }

    /**
     * @return the method name associated with this unary operator
     */
    private static String findUnaryOperatorName(String text) {
        switch (text.charAt(0)) {
        case '+':
            if (text.equals("++")) {
                return "next";
            }
            return "positive";
        case '-':
            if (text.equals("--")) {
                return "previous";
            }
            return "negative";
        case ']':
            return "putAt";
        case '~':
            return "bitwiseNegate";
        }
        return null;
    }

    private static Parameter[] getPositionalParameters(MethodNode methodNode) {
        if (Arrays.stream(methodNode.getClass().getInterfaces()).anyMatch(face -> face.getSimpleName().equals("MethodNodeWithNamedParams"))) {
            Parameter[] parameters = ReflectionUtils.executePrivateMethod(methodNode.getClass(), "getPositionalParams", methodNode);
            return parameters;
        }
        return methodNode.getParameters();
    }

    private static Map<String, ClassNode[]> inferInstanceOfType(BooleanExpression condition, VariableScope scope) {
        // check for "if (x instanceof y) { ... }" flow typing
        if (condition.getExpression() instanceof BinaryExpression) {
            BinaryExpression be = (BinaryExpression) condition.getExpression();
            // check for "if (x == null || x instanceof y) { .. }" or
            //  "if (x != null && x instanceof y) { .. }" flow typing
            BinaryExpression nsbe = nullSafeBinaryExpression(be);
            if (nsbe != null) {
                be = nsbe;
            }
            if (be.getOperation().getType() == Types.KEYWORD_INSTANCEOF &&
                    be.getLeftExpression() instanceof VariableExpression) {
                VariableExpression ve = (VariableExpression) be.getLeftExpression();
                VariableScope.VariableInfo vi = scope.lookupName(ve.getName());
                if (vi != null && GroovyUtils.isAssignable(be.getRightExpression().getType(), vi.type)) {
                    return Collections.singletonMap(vi.name, new ClassNode[] {be.getRightExpression().getType(), null});
                }
            }
        }/* else if (condition.getExpression() instanceof NotExpression) {
            // check for "if (!(x instanceof y)) { ... } else { ... }"
            Map<String, ClassNode[]> types = inferInstanceOfType((NotExpression) condition.getExpression(), scope);
            if (!types.isEmpty()) {
                for (Map.Entry<String, ClassNode[]>entry : types.entrySet()) {
                    entry.setValue(new ClassNode[] {entry.getValue()[1], entry.getValue()[0]});
                }
                return types;
            }
        }*/
        return Collections.EMPTY_MAP;
    }

    /**
     * Makes assumption that no one has overloaded the basic arithmetic operations on numbers.
     * These operations will bypass the mop in most situations anyway.
     */
    private static boolean isArithmeticOperationOnNumberOrStringOrList(String text, ClassNode lhs, ClassNode rhs) {
        if (text.length() != 1) {
            return false;
        }

        lhs = ClassHelper.getWrapper(lhs);

        switch (text.charAt(0)) {
        case '+':
        case '-':
            if (GeneralUtils.isOrImplements(lhs, VariableScope.LIST_CLASS_NODE)) {
                return true;
            }
            // falls through
        case '*':
        case '/':
        case '%':
            return VariableScope.STRING_CLASS_NODE.equals(lhs) ||
                   VariableScope.NUMBER_CLASS_NODE.equals(lhs) || lhs.isDerivedFrom(VariableScope.NUMBER_CLASS_NODE);
        default:
            return false;
        }
    }

    private static boolean isEnumInit(MethodCallExpression node) {
        return (node.getType().isEnum() && node.getMethodAsString().equals("$INIT"));
    }

    private static boolean isEnumInit(StaticMethodCallExpression node) {
        return (node.getOwnerType().isEnum() && node.getMethodAsString().equals("$INIT"));
    }

    private static boolean isLazy(FieldNode fieldNode) {
        return isNotEmpty(GroovyUtils.getAnnotations(fieldNode, "groovy.lang.Lazy"));
    }

    private static boolean isMetaAnnotation(ClassNode classNode) {
        return isNotEmpty(GroovyUtils.getAnnotations(classNode, "groovy.transform.AnnotationCollector"));
    }

    private static boolean isNotEmpty(List<?> list) {
        return (list != null && !list.isEmpty());
    }

    private static boolean isNotEmpty(Stream<?> stream) {
        return (stream != null && stream.anyMatch(x -> true));
    }

    private static VariableExpression isNotNullTest(Expression node) {
        if (node instanceof CompareToNullExpression) {
            CompareToNullExpression expr = (CompareToNullExpression) node;
            if (expr.getOperation().getText().equals("!=") &&
                    expr.getObjectExpression() instanceof VariableExpression) {
                return (VariableExpression) expr.getObjectExpression();
            }
        } else if (node instanceof BinaryExpression) {
            BinaryExpression expr = (BinaryExpression) node;
            if (expr.getOperation().getType() == Types.COMPARE_NOT_EQUAL) {
                if (expr.getLeftExpression() instanceof VariableExpression &&
                        expr.getRightExpression() instanceof ConstantExpression &&
                        ((ConstantExpression) expr.getRightExpression()).isNullExpression()) {
                    return (VariableExpression) expr.getLeftExpression();
                } else if (expr.getRightExpression() instanceof VariableExpression &&
                        expr.getLeftExpression() instanceof ConstantExpression &&
                        ((ConstantExpression) expr.getLeftExpression()).isNullExpression()) {
                    return (VariableExpression) expr.getRightExpression();
                }
            }
        }
        return null;
    }

    private static VariableExpression isNullTest(Expression node) {
        if (node instanceof CompareToNullExpression) {
            CompareToNullExpression expr = (CompareToNullExpression) node;
            if (expr.getOperation().getText().equals("==") &&
                    expr.getObjectExpression() instanceof VariableExpression) {
                return (VariableExpression) expr.getObjectExpression();
            }
        } else if (node instanceof BinaryExpression) {
            BinaryExpression expr = (BinaryExpression) node;
            if (expr.getOperation().getType() == Types.COMPARE_EQUAL) {
                if (expr.getLeftExpression() instanceof VariableExpression &&
                        expr.getRightExpression() instanceof ConstantExpression &&
                        ((ConstantExpression) expr.getRightExpression()).isNullExpression()) {
                    return (VariableExpression) expr.getLeftExpression();
                } else if (expr.getRightExpression() instanceof VariableExpression &&
                        expr.getLeftExpression() instanceof ConstantExpression &&
                        ((ConstantExpression) expr.getLeftExpression()).isNullExpression()) {
                    return (VariableExpression) expr.getRightExpression();
                }
            }
        }
        return null;
    }

    /**
     * Checks for "x == null || x ..." or "x != null && x ..."
     */
    private static BinaryExpression nullSafeBinaryExpression(BinaryExpression node) {
        Expression left = node.getLeftExpression();
        Expression rght = node.getRightExpression();
        if (node.getOperation().getType() == Types.LOGICAL_OR) {
            VariableExpression ve;
            if ((ve = isNullTest(left)) != null && rght instanceof BinaryExpression) {
                BinaryExpression be = (BinaryExpression) rght;
                if (be.getLeftExpression() instanceof VariableExpression &&
                        ve.getName().equals(((VariableExpression) be.getLeftExpression()).getName())) {
                    return be;
                }
            } else if (ve == null && (ve = isNullTest(rght)) != null && left instanceof BinaryExpression) {
                BinaryExpression be = (BinaryExpression) left;
                if (be.getLeftExpression() instanceof VariableExpression &&
                        ve.getName().equals(((VariableExpression) be.getLeftExpression()).getName())) {
                    return be;
                }
            }
        } else if (node.getOperation().getType() == Types.LOGICAL_AND) {
            VariableExpression ve;
            if ((ve = isNotNullTest(left)) != null && rght instanceof BinaryExpression) {
                BinaryExpression be = (BinaryExpression) rght;
                if (be.getLeftExpression() instanceof VariableExpression &&
                        ve.getName().equals(((VariableExpression) be.getLeftExpression()).getName())) {
                    return be;
                }
            } else if (ve == null && (ve = isNotNullTest(rght)) != null && left instanceof BinaryExpression) {
                BinaryExpression be = (BinaryExpression) left;
                if (be.getLeftExpression() instanceof VariableExpression &&
                        ve.getName().equals(((VariableExpression) be.getLeftExpression()).getName())) {
                    return be;
                }
            }
        }
        return null;
    }

    private static List<IMember> membersOf(IType type, boolean isScript) throws JavaModelException {
        boolean isEnum = type.isEnum();
        List<IMember> members = new ArrayList<>();

        for (IJavaElement child : type.getChildren()) {
            String name = child.getElementName();
            switch (child.getElementType()) {
            case IJavaElement.METHOD: // exclude synthetic members for enums
                if (!isEnum || !(name.indexOf('$') > -1 || ((name.equals("next") || name.equals("previous")) && ((IMethod) child).getNumberOfParameters() == 0))) {
                    members.add((IMethod) child);
                }
                break;
            case IJavaElement.FIELD: // exclude synthetic members for enums
                if (!isEnum || !(name.indexOf('$') > -1 || name.equals("MIN_VALUE") || name.equals("MAX_VALUE"))) {
                    members.add((IField) child);
                }
                break;
            case IJavaElement.TYPE:
                members.add((IType) child);
            }
        }

        if (isScript) {
            // move 'run' method to the end since it covers other members
            for (Iterator<IMember> it = members.iterator(); it.hasNext();) {
                IMember member = it.next();
                if (member.getElementType() == IJavaElement.METHOD && member.getElementName().equals("run") && ((IMethod) member).getNumberOfParameters() == 0) {
                    it.remove(); members.add(member);
                    break;
                }
            }
        }

        return members;
    }

    //--------------------------------------------------------------------------

    private static class Tuple {
        ClassNode declaringType;
        ASTNode declaration;

        Tuple(ClassNode declaringType, ASTNode declaration) {
            this.declaringType = declaringType;
            this.declaration = declaration;
        }
    }

    public static class VisitCompleted extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public final VisitStatus status;

        public VisitCompleted(VisitStatus status) {
            this.status = status;
        }
    }
}
