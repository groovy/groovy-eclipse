/*
 * Copyright 2009-2020 the original author or authors.
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
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
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
import org.codehaus.groovy.transform.sc.TemporaryVariableExpression;
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
import org.eclipse.core.runtime.Platform;
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
import org.eclipse.jdt.groovy.core.util.GroovyCodeVisitorAdapter;
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

    private static void log(final Throwable t, final String form, final Object... args) {
        String m = "Groovy-Eclipse Type Inferencing: " + String.format(form, args);
        Status s = new Status(IStatus.ERROR, Activator.PLUGIN_ID, m, t);
        Util.log(s);
    }

    private static final String[] NO_PARAMS = CharOperation.NO_STRINGS;
    private static final Parameter[] NO_PARAMETERS = Parameter.EMPTY_ARRAY;

    /**
     * Set to true if debug mode is desired. Any exceptions will be spit to syserr. Also, after a visit, there will be a sanity
     * check to ensure that all stacks are empty Only set to true if using a visitor that always visits the entire file
     */
    public boolean debug = Boolean.parseBoolean(Platform.getDebugOption("org.codehaus.groovy.eclipse.core/debug/typeinfo"));

    private final GroovyCompilationUnit unit;

    private final Deque<VariableScope> scopes = new LinkedList<>();

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
    private final Deque<ASTNode> completeExpressionStack = new LinkedList<>();

    /**
     * Tracks the declaring type of the current dependent expression. Dependent
     * expressions are dependent on a primary expression to find type information.
     * This field is only applicable for {@link PropertyExpression}s and {@link MethodCallExpression}s.
     */
    private final Deque<Tuple> dependentDeclarationStack = new LinkedList<>();

    /**
     * Tracks the type of the type of the property field corresponding to each
     * frame of the property expression.
     */
    private final Deque<ClassNode> dependentTypeStack = new LinkedList<>();

    /**
     * Tracks the type of the object expression corresponding to each frame of
     * the property expression.
     */
    private final Deque<ClassNode> primaryTypeStack = new LinkedList<>();

    private final AssignmentStorer assignmentStorer = new AssignmentStorer();

    /**
     * Use factory to instantiate
     */
    TypeInferencingVisitorWithRequestor(final GroovyCompilationUnit unit, final ITypeLookup[] lookups) {
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

    public void visitCompilationUnit(final ITypeRequestor requestor) {
        if (enclosingModule == null) {
            // no module node, can't do anything
            return;
        }

        this.requestor = requestor;
        this.enclosingElement = unit;
        VariableScope topLevelScope = new VariableScope(null, enclosingModule, true);
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
            if (debug) {
                System.err.println("Excpetion thrown from inferencing engine");
                e.printStackTrace();
            }
        } finally {
            scopes.removeLast();
        }
        if (debug) {
            postVisitSanityCheck();
        }
    }

    public void visitJDT(final IType type, final ITypeRequestor requestor) {
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

    public void visitJDT(final IField field, final ITypeRequestor requestor) {
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

    public void visitJDT(final IMethod method, final ITypeRequestor requestor) {
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
    public void visitPackage(final PackageNode node) {
        if (node != null) {
            visitAnnotations(node);

            IJavaElement oldEnclosing = enclosingElement;
            enclosingElement = unit.getPackageDeclaration(node.getName().substring(0, node.getName().length() - 1));
            try {
                TypeLookupResult noLookup = new TypeLookupResult(null, null, node, TypeConfidence.EXACT, null);
                VisitStatus status = notifyRequestor(node, requestor, noLookup);
                if (status == VisitStatus.STOP_VISIT) {
                    throw new VisitCompleted(status);
                }
            } finally {
                enclosingElement = oldEnclosing;
            }
        }
    }

    @Override
    public void visitImports(final ModuleNode node) {
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
            }

            enclosingElement = unit.getImport(importName);
            if (!enclosingElement.exists()) {
                // GRECLIPSE-1363, GRECLIPSE-1371, et al. -- handle imports like "import static Boolean.TRUE"
                if (imp.isStatic()) {
                    if (imp.isStar()) {
                        importName = imp.getType().getNameWithoutPackage().replace('$', '.') + ".*";
                    } else {
                        importName = imp.getType().getNameWithoutPackage().replace('$', '.') + "." + imp.getFieldName();
                    }
                    enclosingElement = unit.getImport(importName);
                }
                if (!enclosingElement.exists()) {
                    enclosingElement = oldEnclosingElement;
                }
            }

            VariableScope scope = scopes.getLast();
            assignmentStorer.storeImport(imp, scope);
            try {
                TypeLookupResult noLookup = new TypeLookupResult(null, null, imp, TypeConfidence.EXACT, scope);
                VisitStatus status = notifyRequestor(imp, requestor, noLookup);
                switch (status) {
                case CONTINUE:
                    try {
                        ClassNode type = imp.getType();
                        if (type != null) {
                            visitClassReference(type);
                            if (imp.getFieldNameExpr() != null) {
                                completeExpressionStack.add(imp);
                                try {
                                    scope.setCurrentNode(imp);
                                    primaryTypeStack.add(type);
                                    visitConstantExpression(imp.getFieldNameExpr());

                                    scope.forgetCurrentNode();
                                    dependentTypeStack.removeLast();
                                    dependentDeclarationStack.removeLast();
                                } finally {
                                    completeExpressionStack.removeLast();
                                }
                            }
                        }
                    } catch (VisitCompleted vc) {
                        if (vc.status == VisitStatus.STOP_VISIT) {
                            throw vc;
                        }
                    }
                    // fall through
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

    private void visitClassReference(final ClassNode node) {
        TypeLookupResult result = null;
        VariableScope scope = scopes.getLast();
        ClassNode type = GroovyUtils.getBaseType(node);
        for (ITypeLookup lookup : lookups) {
            TypeLookupResult candidate = lookup.lookupType(type, scope);
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

        VisitStatus status = notifyRequestor(type, requestor, result);
        switch (status) {
        case CONTINUE:
            if (!type.isEnum()) {
                visitGenericTypes(type);
            }
            // fall through
        case CANCEL_BRANCH:
            return;
        case CANCEL_MEMBER:
        case STOP_VISIT:
            throw new VisitCompleted(status);
        }
    }

    private void visitFieldInternal(final FieldNode node) {
        try {
            visitField(node);
        } catch (VisitCompleted vc) {
            if (vc.status == VisitStatus.STOP_VISIT) {
                throw vc;
            }
        }
    }

    private void visitMethodInternal(final MethodNode node) {
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
    private void visitMethodOverrides(final ClassNode node) {
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
    public void visitAnnotation(final AnnotationNode node) {
        ClassNode type = node.getClassNode();
        VariableScope scope = scopes.getLast();
        TypeLookupResult noLookup = new TypeLookupResult(type, type, node, TypeConfidence.EXACT, scope);

        VisitStatus status = notifyRequestor(node, requestor, noLookup);
        switch (status) {
        case CONTINUE:
            // visit annotation label
            visitClassReference(type);
            // visit attribute labels
            visitAnnotationKeys(node);
            // visit attribute values
            super.visitAnnotation(node);
            break;
        case CANCEL_BRANCH:
            return;
        case CANCEL_MEMBER:
        case STOP_VISIT:
            throw new VisitCompleted(status);
        }
    }

    private void visitAnnotationKeys(final AnnotationNode node) {
        VariableScope scope = scopes.getLast();
        for (String name : node.getMembers().keySet()) {
            ASTNode attr;
            TypeLookupResult noLookup;
            MethodNode meth = GroovyUtils.getAnnotationMethod(node, name);
            if (meth != null) {
                attr = meth; // no Groovy AST node exists for name
                noLookup = new TypeLookupResult(meth.getReturnType(), meth.getDeclaringClass(), meth, TypeConfidence.EXACT, scope);
            } else {
                attr = new ConstantExpression(name);
                ClassNode type = node.getClassNode();
                // this is very rough; it only works for an attribute that directly follows '('
                attr.setStart(type.getEnd() + 1);
                attr.setEnd(attr.getStart() + name.length());
                noLookup = new TypeLookupResult(VariableScope.VOID_CLASS_NODE, type, null, TypeConfidence.UNKNOWN, scope);
            }
            noLookup.enclosingAnnotation = node; // set context for requestor
            VisitStatus status = notifyRequestor(attr, requestor, noLookup);
            if (status != VisitStatus.CONTINUE) break;
        }
    }

    @Override
    public void visitArrayExpression(final ArrayExpression node) {
        handleSimpleExpression(node, () -> {
            visitClassReference(node.getType());
            super.visitArrayExpression(node);
        });
    }

    @Override
    public void visitAttributeExpression(final AttributeExpression node) {
        visitPropertyExpression(node);
    }

    @Override
    public void visitBinaryExpression(final BinaryExpression node) {
        if (isDependentExpression(node)) {
            primaryTypeStack.removeLast();
        }

        boolean isAssignment = node.getOperation().isA(Types.ASSIGNMENT_OPERATOR);
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

        ClassNode primaryExprType;
        VariableScope trueScope = null;
        try {
            toVisitPrimary.visit(this);

            primaryExprType = primaryTypeStack.removeLast();
            if (isAssignment) {
                assignmentStorer.storeAssignment(node, scopes.getLast(), primaryExprType);

            } else if (node.getOperation().getType() == Types.LOGICAL_AND) { // check for instanceof guard
                Map<String, ClassNode[]> types = inferInstanceOfType(toVisitPrimary, scopes.getLast());
                if (!types.isEmpty()) {
                    trueScope = new VariableScope(scopes.getLast(), toVisitPrimary, false);
                    for (Map.Entry<String, ClassNode[]> entry : types.entrySet()) {
                        if (entry.getValue().length > 0 && entry.getValue()[0] != null) {
                            trueScope.updateVariableSoft(entry.getKey(), entry.getValue()[0]);
                        }
                    }
                    scopes.add(trueScope);
                }
            }

            toVisitDependent.visit(this);
        } finally {
            completeExpressionStack.removeLast();
            if (trueScope != null)
                scopes.removeLast().bubbleUpdates();
        }
        ClassNode completeExprType = primaryExprType;
        ClassNode dependentExprType = primaryTypeStack.removeLast();

// TODO: Is it an illegal state to have either as null?
assert primaryExprType != null && dependentExprType != null;

        if (!isAssignment && primaryExprType != null && dependentExprType != null) {
            // type of RHS of binary expression
            // find the type of the complete expression
            String associatedMethod = findBinaryOperatorName(node.getOperation().getText());
            if (isArithmeticOperationOnListOrNumberOrString(node.getOperation().getText(), primaryExprType, dependentExprType)) {
                // in 1.8 and later, Groovy will not go through the MOP for standard arithmetic operations on numbers
                completeExprType = dependentExprType.equals(VariableScope.STRING_CLASS_NODE) || dependentExprType.equals(VariableScope.GSTRING_CLASS_NODE) ? VariableScope.STRING_CLASS_NODE : primaryExprType;
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
    public void visitBitwiseNegationExpression(final BitwiseNegationExpression node) {
        visitUnaryExpression(node, node.getExpression(), "~");
    }

    @Override
    public void visitBlockStatement(final BlockStatement block) {
        VariableScope scope = new VariableScope(scopes.getLast(), block, false);
        scope.setCurrentNode(block);
        scopes.add(scope);
        try {
            boolean shouldContinue = handleStatement(block);
            if (shouldContinue) {
                super.visitBlockStatement(block);
            }
            scope.forgetCurrentNode();
        } finally {
            scope.bubbleUpdates();
            scopes.removeLast();
        }
    }

    @Override
    public void visitBooleanExpression(final BooleanExpression node) {
        visitUnaryExpression(node, node.getExpression(), "!!");
    }

    @Override
    public void visitBytecodeExpression(final BytecodeExpression node) {
        handleSimpleExpression(node, () -> {
            super.visitBytecodeExpression(node);
        });
    }

    @Override
    public void visitCastExpression(final CastExpression node) {
        visitClassReference(node.getType());
        visitUnaryExpression(node, node.getExpression(), "as");
    }

    @Override
    public void visitCatchStatement(final CatchStatement node) {
        scopes.add(new VariableScope(scopes.getLast(), node, false));
        try {
            Parameter param = node.getVariable();
            if (param != null) {
                handleParameterList(new Parameter[] {param});
            }
            super.visitCatchStatement(node);
        } finally {
            scopes.removeLast().bubbleUpdates();
        }
    }

    @Override
    public void visitClassExpression(final ClassExpression node) {
        handleSimpleExpression(node, () -> {
            visitClassReference(node.getType());
            super.visitClassExpression(node);
        });
    }

    @Override
    public void visitClosureExpression(final ClosureExpression node) {
        VariableScope parent = scopes.getLast();
        VariableScope scope = new VariableScope(parent, node, false);
        scopes.add(scope);
        try {
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
                if (info == null || info.type == null || info.scopeNode instanceof ClosureExpression) {
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
                VariableScope.VariableInfo info = scope.lookupName("delegate");
                if (info == null || info.type == null || info.scopeNode instanceof ClosureExpression) {
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

            // GRECLIPSE-598: make sure enclosingAssignment is set before visitClosureExpression() to make the
            // assignedVariable pointcut work immediately inside assigned closure block like "def foo = { | }"
            scope.getWormhole().put("enclosingAssignment", enclosingAssignment);

            super.visitClosureExpression(node);
            handleSimpleExpression(node);
        } finally {
            scopes.removeLast();
        }
    }

    @Override
    public void visitClosureListExpression(final ClosureListExpression node) {
        handleSimpleExpression(node, () -> {
            super.visitClosureListExpression(node);
        });
    }

    @Override
    public void visitConstantExpression(final ConstantExpression node) {
        if (node instanceof AnnotationConstantExpression) {
            // visit annotation label
            visitClassReference(node.getType());
            // visit attribute labels
            visitAnnotationKeys((AnnotationNode) node.getValue());
            // visit attribute values (in AnnotationConstantExpression#visit)
        }
        scopes.getLast().setCurrentNode(node);
        handleSimpleExpression(node, () -> {
            super.visitConstantExpression(node);
        });
        scopes.getLast().forgetCurrentNode();
    }

    @Override
    public void visitConstructorCallExpression(final ConstructorCallExpression node) {
        handleSimpleExpression(node, () -> {
            final ClassNode type = node.getType();
            if (node.isUsingAnonymousInnerClass()) {
                // in "new Type() { ... }", Type is super class or interface
                ClassNode superClass = type.getUnresolvedSuperClass(false);
                if (superClass != VariableScope.OBJECT_CLASS_NODE) {
                    visitClassReference(superClass); // incl. Object
                } else {
                    visitClassReference(type.getInterfaces()[0]);
                }
            } else if (!node.isSpecialCall()) {
                visitClassReference(type);
            }
            if (node.getArguments() instanceof TupleExpression) {
                TupleExpression tuple = (TupleExpression) node.getArguments();
                if (isNotEmpty(tuple.getExpressions())) {
                    if ((tuple.getExpressions().size() == 1 && tuple.getExpression(0) instanceof MapExpression) ||
                            DefaultGroovyMethods.last(tuple.getExpressions()) instanceof NamedArgumentListExpression) {
                        // remember this is a map ctor call, so that field names can be inferred when visiting the map
                        enclosingConstructorCall = node;
                    }
                }
            }
            super.visitConstructorCallExpression(node);

            // visit anonymous inner class body
            if (node.isUsingAnonymousInnerClass()) {
                scopes.add(new VariableScope(scopes.getLast(), type, false));
                // in case of an enclosing closure, disable access to Closure's implicit variables (note: must keep in sync with visitClosureExpression)
                Stream.of("owner", "getOwner", "delegate", "getDelegate", "thisObject", "getThisObject").map(name -> scopes.getLast().lookupName(name))
                    .filter(info -> info != null && info.type != null && VariableScope.CLOSURE_CLASS_NODE.equals(info.declaringType))
                    .forEach(info -> scopes.getLast().addVariable(info.name, null, VariableScope.CLOSURE_CLASS_NODE));
                ASTNode  enclosingDeclaration0 = enclosingDeclarationNode;
                IJavaElement enclosingElement0 = enclosingElement;
                enclosingDeclarationNode = type;
                try {
                    IType anon = findAnonType(type, enclosingElement).orElseThrow(
                        () -> new GroovyEclipseBug("Failed to locate anon. type " + type.getName()));

                    enclosingElement = anon; // visit inlined object initializers
                    type.getDeclaredConstructors().forEach(this::visitMethodInternal);

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
        });
    }

    @Override
    public void visitConstructorOrMethod(final MethodNode node, final boolean isConstructor) {
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
    public void visitEmptyExpression(final EmptyExpression node) {
        handleSimpleExpression(node, () -> {
            super.visitEmptyExpression(node);
        });
    }

    @Override
    public void visitFieldExpression(final FieldExpression node) {
        handleSimpleExpression(node, () -> {
            super.visitFieldExpression(node);
        });
    }

    @Override
    public void visitField(final FieldNode node) {
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
                // fall through
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
    public void visitForLoop(final ForStatement node) {
        completeExpressionStack.add(node);
        try {
            node.getCollectionExpression().visit(this);
        } finally {
            completeExpressionStack.removeLast();
        }
        ClassNode collectionType = primaryTypeStack.removeLast();

        // the loop has its own scope
        scopes.add(new VariableScope(scopes.getLast(), node, false));
        try {
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
        } finally {
            scopes.removeLast().bubbleUpdates();
        }
    }

    private void visitGenericTypes(final ClassNode node) {
        visitGenericTypes(node.getGenericsTypes(), node.getName());
    }

    private void visitGenericTypes(final GenericsType[] generics, final String typeName) {
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
    public void visitGStringExpression(final GStringExpression node) {
        scopes.getLast().setCurrentNode(node);
        handleSimpleExpression(node, () -> {
            super.visitGStringExpression(node);
        });
        scopes.getLast().forgetCurrentNode();
    }

    @Override
    public void visitIfElse(final IfStatement node) {
        scopes.add(new VariableScope(scopes.getLast(), node, false) {
            @Override
            public void updateVariable(String name, ClassNode type, ClassNode declaringType) {
                type = WideningCategories.lowestUpperBound(type, lookupName(name).type);
                super.updateVariable(name, type, declaringType);
            }
        });
        VariableScope trueScope = null, falseScope = null;
        try {
            // TODO: Assignment within expression may be conditional or unconditional due to short-circuit evaluation.
            node.getBooleanExpression().visit(this);

            Map<String, ClassNode[]> types = inferInstanceOfType(node.getBooleanExpression(), scopes.getLast());
            trueScope = new VariableScope(scopes.getLast(), node.getIfBlock(), false);
            scopes.add(trueScope);
            try {
                for (Map.Entry<String, ClassNode[]> entry : types.entrySet()) {
                    if (entry.getValue().length > 0 && entry.getValue()[0] != null) {
                        trueScope.updateVariableSoft(entry.getKey(), entry.getValue()[0]);
                    }
                }
                node.getIfBlock().visit(this);
            } finally {
                scopes.removeLast();
            }

            falseScope = new VariableScope(scopes.getLast(), node.getElseBlock(), false);
            scopes.add(falseScope);
            try {
                for (Map.Entry<String, ClassNode[]> entry : types.entrySet()) {
                    if (entry.getValue().length > 1 && entry.getValue()[1] != null) {
                        falseScope.updateVariableSoft(entry.getKey(), entry.getValue()[1]);
                    }
                }
                // TODO: If the else block sets unconditionally, exclude current type from the LUB computation.
                node.getElseBlock().visit(this);
            } finally {
                scopes.removeLast();
            }
        } finally {
            // apply variable updates
            if (trueScope != null)
                trueScope.bubbleUpdates();
            if (falseScope != null)
                falseScope.bubbleUpdates();
            scopes.removeLast().bubbleUpdates();
        }
    }

    @Override
    public void visitListExpression(final ListExpression node) {
        scopes.getLast().setCurrentNode(node);
        if (isDependentExpression(node)) {
            primaryTypeStack.removeLast();
        }
        completeExpressionStack.add(node);
        ClassNode itemType;
        try {
            if (isNotEmpty(node.getExpressions())) {
                Iterator<Expression> items = node.getExpressions().iterator();

                items.next().visit(this);
                itemType = primaryTypeStack.removeLast();

                while (items.hasNext()) {
                    items.next().visit(this);
                }
            } else {
                itemType = VariableScope.OBJECT_CLASS_NODE;
            }
        } finally {
            completeExpressionStack.removeLast();
        }
        ClassNode exprType = createParameterizedList(itemType);
        handleCompleteExpression(node, exprType, null);
        scopes.getLast().forgetCurrentNode();
    }

    @Override
    public void visitMapEntryExpression(final MapEntryExpression node) {
        scopes.getLast().setCurrentNode(node);
        if (isDependentExpression(node)) {
            primaryTypeStack.removeLast();
        }
        completeExpressionStack.add(node);
        ClassNode keyType, valType;
        try {
            node.getKeyExpression().visit(this);
            keyType = primaryTypeStack.removeLast();

            node.getValueExpression().visit(this);
            valType = primaryTypeStack.removeLast();
        } finally {
            completeExpressionStack.removeLast();
        }
        ClassNode exprType = createParameterizedMap(keyType, valType);
        handleCompleteExpression(node, exprType, null);
        scopes.getLast().forgetCurrentNode();
    }

    @Override
    public void visitMapExpression(final MapExpression node) {
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
        ClassNode exprType = null;
        try {
            for (MapEntryExpression entry : node.getMapEntryExpressions()) {
                Expression key = entry.getKeyExpression(), value = entry.getValueExpression();
                if (ctorType != null && key instanceof ConstantExpression && !"*".equals(key.getText())) {

                    VariableScope scope = scopes.getLast();
                    // look for a non-synthetic setter followed by a property or field
                    scope.setMethodCallArgumentTypes(Collections.singletonList(value.getType()));
                    String setterName = AccessorSupport.SETTER.createAccessorName(key.getText());
                    TypeLookupResult result = lookupExpressionType(new ConstantExpression(setterName), ctorType, false, scope);
                    if (result.confidence == TypeConfidence.UNKNOWN || !(result.declaration instanceof MethodNode) ||
                            ((MethodNode) result.declaration).isSynthetic()) {
                        scope.getWormhole().put("lhs", key);
                        scope.setMethodCallArgumentTypes(null);
                        result = lookupExpressionType(key, ctorType, false, scope);
                    }

                    // pre-visit entry so keys are highlighted as keys, not fields/methods/properties
                    ClassNode mapType = Optional.ofNullable(exprType).orElseGet(() -> createParameterizedMap(key.getType(), value.getType()));
                    scope.setCurrentNode(entry);
                    handleCompleteExpression(entry, mapType, null);
                    if (exprType == null) {
                        exprType = primaryTypeStack.removeLast();
                    }
                    scope.forgetCurrentNode();

                    handleRequestor(key, ctorType, result);
                    value.visit(this);
                } else {
                    entry.visit(this);
                    if (exprType == null) {
                        exprType = primaryTypeStack.removeLast();
                    }
                }
            }
        } finally {
            completeExpressionStack.removeLast();
        }

        if (exprType == null) {
            exprType = createParameterizedMap(VariableScope.OBJECT_CLASS_NODE, VariableScope.OBJECT_CLASS_NODE);
        }
        handleCompleteExpression(node, exprType, null);
        scopes.getLast().forgetCurrentNode();
    }

    @Override
    public void visitMethodCallExpression(final MethodCallExpression node) {
        VariableScope scope = scopes.getLast();
        scope.setCurrentNode(node);

        if (isDependentExpression(node)) {
            primaryTypeStack.removeLast();
        }
        completeExpressionStack.add(node);

        ClassNode objExprType, preSpreadType = null;
        try {
            node.getObjectExpression().visit(this);

            if (node.isSpreadSafe()) {
                preSpreadType = primaryTypeStack.removeLast();
                // method call targets the element type of the object expression
                primaryTypeStack.add(VariableScope.extractSpreadType(preSpreadType));
            }

            if (node.isUsingGenerics()) {
                objExprType = primaryTypeStack.removeLast();
                visitGenericTypes(node.getGenericsTypes(), null);
                primaryTypeStack.add(objExprType);
            } else {
                objExprType = primaryTypeStack.getLast();
            }

            // visit method before arguments to provide @ClosureParams, @DeleagtesTo, etc. to closures
            // NOTE: this makes choosing from overloads imprecise since argument types aren't complete
            node.getMethod().visit(this);
        } finally {
            completeExpressionStack.removeLast();
        }
        ClassNode returnType = dependentTypeStack.removeLast();

        // the inferred declaring type of this method
        Tuple t = dependentDeclarationStack.removeLast();
        VariableScope.CallAndType call = new VariableScope.CallAndType(node, t.declaration, t.declaringType, enclosingModule);

        ClassNode catNode = isCategoryDeclaration(node, scope);
        if (catNode != null) {
            scope.setCategoryBeingDeclared(catNode);
        }

        // remember that we are inside a method call while analyzing the arguments
        scope.addEnclosingMethodCall(call);
        node.getArguments().visit(this);
        scope.forgetEnclosingMethodCall();

        ClassNode type = node.getType().redirect();
        if (isEnumInit(node) && GroovyUtils.isAnonymous(type)) {
            visitMethodOverrides(type);
        }

        if (t.declaration instanceof MethodNode) {
            MethodNode meth = (MethodNode) t.declaration;
            // if return type depends on any Closure argument return types, deal with that
            if (meth.getGenericsTypes() != null && Arrays.stream(meth.getParameters())
                    .anyMatch(p -> p.getType().equals(VariableScope.CLOSURE_CLASS_NODE))) {
                scope.setMethodCallArgumentTypes(getMethodCallArgumentTypes(node));
                scope.setMethodCallGenericsTypes(getMethodCallGenericsTypes(node));
                try {
                    boolean isStatic = (node.getObjectExpression() instanceof ClassExpression);
                    returnType = lookupExpressionType(node.getMethod(), objExprType, isStatic, scope).type;
                } finally {
                    scope.setMethodCallArgumentTypes(null);
                    scope.setMethodCallGenericsTypes(null);
                }
            } else if (t.declaringType.getName().equals("org.spockframework.runtime.ValueRecorder")) {
                switch (meth.getName()) {
                case "record":
                case "realizeNas":
                    returnType = primaryTypeStack.removeLast();
                }
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
    public void visitMethodPointerExpression(final MethodPointerExpression node) {
        scopes.getLast().setCurrentNode(node);

        if (isDependentExpression(node)) {
            primaryTypeStack.removeLast();
        }
        completeExpressionStack.add(node);
        try {
            super.visitMethodPointerExpression(node);
        } finally {
            completeExpressionStack.removeLast();
        }

        Tuple t = dependentDeclarationStack.removeLast();
        if (!(t.declaration instanceof MethodNode)) {
            dependentTypeStack.removeLast(); // unused type
            handleCompleteExpression(node, node.getType(), null);
        } else {
            ClassNode returnType = dependentTypeStack.removeLast();
            handleCompleteExpression(node, createParameterizedClosure(returnType), t.declaringType);
        }

        scopes.getLast().forgetCurrentNode();
    }

    @Override
    public void visitNotExpression(final NotExpression node) {
        visitUnaryExpression(node, node.getExpression(), "!");
    }

    @Override
    public void visitPostfixExpression(final PostfixExpression node) {
        visitUnaryExpression(node, node.getExpression(), node.getOperation().getText());
    }

    @Override
    public void visitPrefixExpression(final PrefixExpression node) {
        visitUnaryExpression(node, node.getExpression(), node.getOperation().getText());
    }

    @Override
    public void visitPropertyExpression(final PropertyExpression node) {
        scopes.getLast().setCurrentNode(node);
        completeExpressionStack.add(node);
        ClassNode preSpreadType = null;
        try {
            node.getObjectExpression().visit(this);

            if (node.isSpreadSafe()) {
                preSpreadType = primaryTypeStack.removeLast();
                // property access targets the element type of the object expression
                primaryTypeStack.add(VariableScope.extractSpreadType(preSpreadType));
            }

            node.getProperty().visit(this);
        } finally {
            completeExpressionStack.removeLast();
        }
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
    public void visitRangeExpression(final RangeExpression node) {
        scopes.getLast().setCurrentNode(node);
        if (isDependentExpression(node)) {
            primaryTypeStack.removeLast();
        }
        completeExpressionStack.add(node);
        try {
            super.visitRangeExpression(node);
        } finally {
            completeExpressionStack.removeLast();
        }
        ClassNode exprType = createParameterizedRange(primaryTypeStack.removeLast());
        handleCompleteExpression(node, exprType, null);
        scopes.getLast().forgetCurrentNode();
    }

    @Override
    public void visitReturnStatement(final ReturnStatement node) {
        boolean shouldContinue = handleStatement(node);
        if (shouldContinue) {
            ClosureExpression closure = scopes.getLast().getEnclosingClosure();
            if (closure == null || closure.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE) != null) {
                super.visitReturnStatement(node);
            } else { // capture return type
                completeExpressionStack.add(node);
                try {
                    super.visitReturnStatement(node);
                } finally {
                    completeExpressionStack.removeLast();
                }
                ClassNode returnType = primaryTypeStack.removeLast();
                ClassNode closureType = (ClassNode) closure.putNodeMetaData("returnType", returnType);
                if (closureType != null && !closureType.equals(returnType)) {
                    closure.putNodeMetaData("returnType", WideningCategories.lowestUpperBound(closureType, returnType));
                }
            }
        }
    }

    @Override
    public void visitShortTernaryExpression(final ElvisOperatorExpression node) {
        if (isDependentExpression(node)) {
            primaryTypeStack.removeLast();
        }
        // arbitrarily, we choose the if clause to be the type of this expression
        completeExpressionStack.add(node);
        try {
            node.getTrueExpression().visit(this);
        } finally {
            completeExpressionStack.removeLast();
        }
        // the declaration itself is the property node
        ClassNode exprType = primaryTypeStack.removeLast();

        node.getFalseExpression().visit(this);
        handleCompleteExpression(node, exprType, null);
    }

    @Override
    public void visitSpreadExpression(final SpreadExpression node) {
        visitUnaryExpression(node, node.getExpression(), "*");
    }

    @Override
    public void visitSpreadMapExpression(final SpreadMapExpression node) {
        visitUnaryExpression(node, node.getExpression(), "*");
    }

    @Override
    public void visitStaticMethodCallExpression(final StaticMethodCallExpression node) {
        ClassNode type = node.getOwnerType();
        if (isPrimaryExpression(node)) {
            visitMethodCallExpression(new MethodCallExpression(new ClassExpression(type), node.getMethod(), node.getArguments()));
        }
        handleSimpleExpression(node, () -> {
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
        });
    }

    @Override
    public void visitTernaryExpression(final TernaryExpression node) {
        if (isDependentExpression(node)) {
            primaryTypeStack.removeLast();
        }
        completeExpressionStack.add(node);
        ClassNode exprType;
        try {
            node.getBooleanExpression().visit(this);

            Map<String, ClassNode[]> types = inferInstanceOfType(node.getBooleanExpression(), scopes.getLast());
            scopes.add(new VariableScope(scopes.getLast(), node.getTrueExpression(), false));
            try {
                for (Map.Entry<String, ClassNode[]> entry : types.entrySet()) {
                    if (entry.getValue().length > 0 && entry.getValue()[0] != null) {
                        scopes.getLast().updateVariableSoft(entry.getKey(), entry.getValue()[0]);
                    }
                }
                node.getTrueExpression().visit(this);
                // arbitrarily choose the 'true' expression
                // to hold the type of the ternary expression
                exprType = primaryTypeStack.removeLast();
            } finally {
                scopes.removeLast().bubbleUpdates();
            }

            scopes.add(new VariableScope(scopes.getLast(), node.getFalseExpression(), false));
            try {
                for (Map.Entry<String, ClassNode[]> entry : types.entrySet()) {
                    if (entry.getValue().length > 1 && entry.getValue()[1] != null) {
                        scopes.getLast().updateVariableSoft(entry.getKey(), entry.getValue()[1]);
                    }
                }
                node.getFalseExpression().visit(this);
            } finally {
                scopes.removeLast().bubbleUpdates();
            }
        } finally {
            completeExpressionStack.removeLast();
        }

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
    public void visitTupleExpression(final TupleExpression node) {
        completeExpressionStack.add(node);
        try {
            if (isNotEmpty(node.getExpressions())) {
                for (Expression expr : node) {
                    // prevent revisit of statically-compiled chained assignment nodes
                    if (!(expr instanceof TemporaryVariableExpression)) {
                        expr.visit(this);
                    }
                }
            }
        } finally {
            completeExpressionStack.removeLast();
        }
        handleCompleteExpression(node, VariableScope.VOID_CLASS_NODE, null);
    }

    private void visitUnaryExpression(final Expression node, final Expression operand, final String operation) {
        VariableScope scope = scopes.getLast();
        scope.setCurrentNode(node);

        if (isDependentExpression(node)) {
            primaryTypeStack.removeLast();
        }
        completeExpressionStack.add(node);
        ClassNode exprType;
        try {
            operand.visit(this);

            ClassNode operandType = primaryTypeStack.removeLast();
            // infer the type of the (possibly overloaded) operator
            String associatedMethod = findUnaryOperatorName(operation);
            if (associatedMethod != null && !operandType.isDerivedFrom(VariableScope.NUMBER_CLASS_NODE)) {
                scope.setMethodCallArgumentTypes(Collections.emptyList());
                TypeLookupResult result = lookupExpressionType(new ConstantExpression(associatedMethod), operandType, false, scope);

                exprType = result.confidence.isAtLeast(TypeConfidence.LOOSELY_INFERRED) ? result.type : operandType;
            } else if (node instanceof BooleanExpression) {
                exprType = VariableScope.BOOLEAN_CLASS_NODE;
            } else if (node instanceof CastExpression) {
                exprType = node.getType();
            } else {
                exprType = operandType;
            }
        } finally {
            completeExpressionStack.removeLast();
        }
        handleCompleteExpression(node, exprType, null);
        scope.forgetCurrentNode();
    }

    @Override
    public void visitUnaryMinusExpression(final UnaryMinusExpression node) {
        visitUnaryExpression(node, node.getExpression(), "-");
    }

    @Override
    public void visitUnaryPlusExpression(final UnaryPlusExpression node) {
        visitUnaryExpression(node, node.getExpression(), "+");
    }

    @Override
    public void visitVariableExpression(final VariableExpression node) {
        // check for transformed expression (see MethodCallExpressionTransformer.transformToMopSuperCall)
        Expression orig = node.getNodeMetaData(ORIGINAL_EXPRESSION);
        if (orig != null) {
            orig.visit(this);
        }

        scopes.getLast().setCurrentNode(node);
        visitAnnotations(node);
        if (node.getAccessedVariable() == node) {
            // this is a local variable declaration
            visitClassReference(node.getOriginType());
        }
        handleSimpleExpression(node);
        scopes.getLast().forgetCurrentNode();
    }

    //--------------------------------------------------------------------------

    private boolean handleStatement(final Statement node) {
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
        default:
            throw new VisitCompleted(status);
        }
    }

    private boolean handleParameterList(final Parameter[] params) {
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

    private boolean handleSimpleExpression(final Expression node) {
        ClassNode primaryType;
        boolean isStatic;
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

    private void    handleSimpleExpression(final Expression node, final Runnable continuation) {
        if (handleSimpleExpression(node)) {
            try {
                continuation.run();
            } catch (RuntimeException e) {
                if (isPrimaryExpression(node)) {
                    primaryTypeStack.removeLast();
                } else if (isDependentExpression(node)) {
                    dependentTypeStack.removeLast();
                    dependentDeclarationStack.removeLast();
                }
                throw e;
            }
        }
    }

    private void    handleCompleteExpression(final Expression node, final ClassNode exprType, final ClassNode declaringType) {
        VariableScope scope = scopes.getLast();
        scope.setPrimaryNode(false);
        handleRequestor(node, declaringType, new TypeLookupResult(exprType, declaringType, node, TypeConfidence.EXACT, scope));
    }

    private boolean handleRequestor(final Expression node, final ClassNode primaryType, final TypeLookupResult result) {
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
        case CANCEL_BRANCH:
            postVisit(node, result.type, rememberedDeclaringType, result.declaration);
            return (status == VisitStatus.CONTINUE);
        case STOP_VISIT:
        case CANCEL_MEMBER:
            throw new VisitCompleted(status);
        }
        // won't get here
        return false;
    }

    //

    private Optional<IType> findAnonType(final ClassNode type, final IJavaElement enclosing) {
        try {
            for (IJavaElement child : ((IMember) enclosing).getChildren()) {
                if (child instanceof IType && ((IType) child).isAnonymous() &&
                        type.getName().endsWith("$" + ((SourceType) child).localOccurrenceCount)) {
                    return Optional.of((IType) child);
                }
            }

            if (enclosing instanceof IType) {
                for (IJavaElement child : ((IType) enclosing).getChildren()) {
                    if (child instanceof org.eclipse.jdt.internal.core.Initializer) {
                        Optional<IType> result = findAnonType(type, child);
                        if (result.isPresent()) return result;
                    }
                }
            } else if (enclosing instanceof IMethod) {
                // check for method with AIC in default argument expression
                MethodNode methodNode = findMethodNode((IMethod) enclosing);
                if (methodNode != null && methodNode.getOriginal() != methodNode) {
                    for (IMethod child : ((IMethod) enclosing).getDeclaringType().getMethods()) {
                        if (child != enclosing && child.getElementName().equals(methodNode.getName())) {
                            Optional<IType> result = findAnonType(type, child);
                            if (result.isPresent()) return result;
                        }
                    }
                }
            }
        } catch (JavaModelException e) {
            log(e, "Error visiting children of %s", type.getName());
        }

        return Optional.empty();
    }

    private ClassNode findClassNode(final String name) {
        for (ClassNode clazz : enclosingModule.getClasses()) {
            if (clazz.getNameWithoutPackage().equals(name)) {
                return clazz;
            }
        }
        return null;
    }

    private FieldNode findFieldNode(final IField field) {
        ClassNode clazz = findClassNode(createName(field.getDeclaringType()));
        FieldNode fieldNode = clazz.getField(field.getElementName());
        if (fieldNode == null) {
            // GRECLIPSE-578: might be @Lazy; name is changed
            fieldNode = clazz.getField("$" + field.getElementName());
        }
        return fieldNode;
    }

    private MethodNode findMethodNode(final IMethod method) {
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
                        if (simpleGroovyClassType.equals(jdtParamTypes[i]) || (simpleGroovyClassType.indexOf('.') != -1 &&
                                simpleGroovyClassType.replaceAll("(?<=Q)(\\w+\\.)+", "").equals(jdtParamTypes[i]))) {
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
                if (!Flags.isStatic(method.getFlags()) && (targetExpr = findCategoryTarget(clazz)) != null) {
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
                        if (simpleGroovyClassType.equals(jdtParamTypes[i]) || (simpleGroovyClassType.indexOf('.') != -1 &&
                                simpleGroovyClassType.replaceAll("(?<=Q)(\\w+\\.)+", "").equals(jdtParamTypes[i]))) {
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

    private List<MethodNode> findMethods(final ClassNode classNode, final String methodName) {
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

    private MethodNode findLazyMethod(final String fieldName) {
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
                        /*if (expression instanceof MapExpression) {
                            types.add(VariableScope.MAP_CLASS_NODE);
                        } else if (expression instanceof ListExpression) {
                            types.add(VariableScope.LIST_CLASS_NODE);
                        } else if (expression instanceof ClosureExpression ||
                                expression instanceof MethodPointerExpression) {
                            types.add(VariableScope.CLOSURE_CLASS_NODE);
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
                            if (expression instanceof PropertyExpression) {
                                PropertyExpression path = (PropertyExpression) expression;
                                tlr = lookupExpressionType(path.getObjectExpression(), null, false, scopes.getLast());
                                tlr = lookupExpressionType(path.getProperty(), tlr.type, path.getObjectExpression() instanceof ClassExpression || VariableScope.CLASS_CLASS_NODE.equals(tlr.type), scopes.getLast());
                            } else if (expression instanceof MethodCallExpression) {
                                MethodCallExpression call = (MethodCallExpression) expression;
                                tlr = lookupExpressionType(call.getObjectExpression(), null, false, scopes.getLast());
                                tlr = lookupExpressionType(call.getMethod(), tlr.type, call.getObjectExpression() instanceof ClassExpression || VariableScope.CLASS_CLASS_NODE.equals(tlr.type), scopes.getLast());
                            } else {
                                tlr = lookupExpressionType(expression, null, false, scopes.getLast());
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
            return (info == null || info.type == null ? null : Collections.singletonList(info.type));
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

    private GenericsType[] getMethodCallGenericsTypes(final ASTNode node) {
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

    private ClassNode[] inferClosureParamTypes(final ClosureExpression node, final VariableScope scope) {
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
            int i = 0;
            MethodNode sam;
            // check for SAM-type coercion of closure/lambda expression
            if (primaryType != null && (sam = ClassHelper.findSAM(primaryType)) != null) {
                GenericsMapper m = GenericsMapper.gatherGenerics(primaryType, primaryType.redirect());
                for (ClassNode t : GroovyUtils.getParameterTypes(sam.getParameters())) {
                    if (i == inferredTypes.length) break;
                    inferredTypes[i++] = VariableScope.resolveTypeParameterization(m, t);
                }
            }
            Arrays.fill(inferredTypes, i, inferredTypes.length, VariableScope.OBJECT_CLASS_NODE);
        }

        return inferredTypes;
    }

    private ClassNode isCategoryDeclaration(final MethodCallExpression node, final VariableScope scope) {
        if ("use".equals(node.getMethodAsString())) {
            if (node.getArguments() instanceof ArgumentListExpression) {
                ArgumentListExpression args = (ArgumentListExpression) node.getArguments();
                if (args.getExpressions().size() >= 2 && args.getExpression(1) instanceof ClosureExpression) {
                    // really, should be doing inference on the first expression and seeing if it
                    // is a class node, but looking up in scope is good enough for now
                    Expression expr = args.getExpression(0);
                    if (expr instanceof ClassExpression) {
                        return expr.getType();
                    } else if (expr instanceof VariableExpression && expr.getText() != null) {
                        VariableScope.VariableInfo info = scope.lookupName(expr.getText());
                        if (info != null && info.type != null) {
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
    private boolean isDependentExpression(final Expression expr) {
        if (!completeExpressionStack.isEmpty()) {
            ASTNode node = completeExpressionStack.getLast();
            if (node instanceof PropertyExpression) {
                return expr == ((PropertyExpression) node).getProperty();
            } else if (node instanceof MethodCallExpression) {
                return expr == ((MethodCallExpression) node).getMethod();
            } else if (node instanceof MethodPointerExpression) {
                return expr == ((MethodPointerExpression) node).getMethodName();
            } else if (node instanceof ImportNode) {
                return expr == ((ImportNode) node).getAliasExpr() || expr == ((ImportNode) node).getFieldNameExpr();
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
     * <li>true expression of a ternary expression
     * <li>first element of a map expression (the first element is assumed to be representative of all map elements)
     * <li>first element of a list expression (the first element is assumed to be representative of all list elements)
     * <li>first element of a range expression (the first element is assumed to be representative of the range components)
     * <li>either the key or the value expression of a {@link MapEntryExpression}
     * <li>expression of a {@link PrefixExpression}, {@link PostfixExpression},
     *     {@link UnaryMinusExpression}, {@link UnaryPlusExpression}, or {@link BitwiseNegationExpression}
     * <li>collection expression of a for statement
     * </ul>
     *
     * @return true iff the expression is a primary expression in an expression group
     */
    private boolean isPrimaryExpression(final Expression expr) {
        if (!completeExpressionStack.isEmpty()) {
            boolean[] result = new boolean[1];
            completeExpressionStack.getLast().visit(new GroovyCodeVisitorAdapter() {

                @Override public void visitBinaryExpression(BinaryExpression expression) {
                    // both sides of the binary expression are primary since we need
                    // access to both of them when inferring binary expression types
                    if (expr == expression.getLeftExpression() || expr == expression.getRightExpression()) {
                        result[0] = true;
                    } else if (!(expr instanceof TupleExpression) && expression.getRightExpression() instanceof ListOfExpressionsExpression) {
                        // statically-compiled assignment chains need a little help
                        List<Expression> list = ReflectionUtils.getPrivateField(
                            ListOfExpressionsExpression.class, "expressions", expression.getRightExpression());
                        // list.get(0) should be TemporaryVariableExpression
                        result[0] = (expr != list.get(1) && list.get(1) instanceof MethodCallExpression);
                    }
                }

                @Override public void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
                    result[0] = (expr == expression.getExpression());
                }

                @Override public void visitBooleanExpression(BooleanExpression expression) {
                    result[0] = (expr == expression.getExpression());
                }

                @Override public void visitCastExpression(CastExpression expression) {
                    result[0] = (expr == expression.getExpression());
                }

                @Override public void visitListExpression(ListExpression expression) {
                    result[0] = (isNotEmpty(expression.getExpressions()) && expr == expression.getExpression(0));
                }

                @Override public void visitMapEntryExpression(MapEntryExpression expression) {
                    result[0] = (expr == expression.getKeyExpression() || expr == expression.getValueExpression());
                }

                @Override public void visitMapExpression(MapExpression expression) {
                    result[0] = (isNotEmpty(expression.getMapEntryExpressions()) && expr == expression.getMapEntryExpressions().get(0));
                }

                @Override public void visitMethodCallExpression(MethodCallExpression expression) {
                    result[0] = (expr == expression.getObjectExpression());
                }

                @Override public void visitMethodPointerExpression(MethodPointerExpression expression) {
                    result[0] = (expr == expression.getExpression());
                }

                @Override public void visitPrefixExpression(PrefixExpression expression) {
                    result[0] = (expr == expression.getExpression());
                }

                @Override public void visitPostfixExpression(PostfixExpression expression) {
                    result[0] = (expr == expression.getExpression());
                }

                @Override public void visitPropertyExpression(PropertyExpression expression) {
                    result[0] = (expr == expression.getObjectExpression());
                }

                @Override public void visitRangeExpression(RangeExpression expression) {
                    result[0] = (expr == expression.getFrom());
                }

                @Override public void visitSpreadExpression(SpreadExpression expression) {
                    result[0] = (expr == expression.getExpression());
                }

                @Override public void visitSpreadMapExpression(SpreadMapExpression expression) {
                    result[0] = (expr == expression.getExpression());
                }

                @Override public void visitTernaryExpression(TernaryExpression expression) {
                    result[0] = (expr == expression.getTrueExpression());
                }

                @Override public void visitUnaryMinusExpression(UnaryMinusExpression expression) {
                    result[0] = (expr == expression.getExpression());
                }

                @Override public void visitUnaryPlusExpression(UnaryPlusExpression expression) {
                    result[0] = (expr == expression.getExpression());
                }

                // statements:

                @Override public void visitForLoop(ForStatement statement) {
                    // used to capture the type of the collection expression
                    // so that it can be assigned to the for loop variable
                    result[0] = (expr == statement.getCollectionExpression());
                }

                @Override public void visitReturnStatement(ReturnStatement statement) {
                    // used to capture the return type of a closure expression
                    result[0] = (expr == statement.getExpression());
                }
            });
            if (result[0]) return true;
        }
        return isSpockValueRecorderArgument(expr);
    }

    /**
     * Spock rewrites the "expect" statements from <code>foo.bar == baz</code> to:
     * <pre>
     * org.spockframework.runtime.SpockRuntime.verifyCondition(
     *   $spock_errorCollector,
     *   $spock_valueRecorder.reset(),
     *   foo == bar.baz, 14, 5, null,
     *   $spock_valueRecorder.record(
     *     $spock_valueRecorder.startRecordingValue(3),
     *     (
     *       $spock_valueRecorder.record(
     *         $spock_valueRecorder.startRecordingValue(1),
     *         $spock_valueRecorder.record(
     *           $spock_valueRecorder.startRecordingValue(0), foo).bar
     *       ) == $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(2), baz)
     *     )
     *   )
     * )
     * </pre>
     *
     * ValueRecorder.record is defined as: <code>public Object record(int index, Object value)</code>.
     * So, we want to save the inferred type of the last argument so it can be used as the return type.
     */
    private boolean isSpockValueRecorderArgument(final Expression expr) {
        VariableScope.CallAndType cat = scopes.getLast().getEnclosingMethodCallExpression();
        if (cat != null && cat.declaration instanceof MethodNode && cat.declaringType.getName().equals("org.spockframework.runtime.ValueRecorder")) {
            ArgumentListExpression args = (ArgumentListExpression) cat.call.getArguments();
            MethodNode meth = (MethodNode) cat.declaration;
            switch (meth.getName()) {
            case "record":
            case "realizeNas":
                return expr == DefaultGroovyMethods.last(args.getExpressions());
            }
        }
        return false;
    }

    /**
     * @return true iff the object expression associated with node is a static reference to a class declaration
     */
    private boolean hasStaticObjectExpression(final Expression node, final ClassNode primaryType) {
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
            } else if (complete instanceof ImportNode) {
                staticObjectExpression = true;
            }
        }
        return staticObjectExpression;
    }

    private TypeLookupResult lookupExpressionType(final Expression node, final ClassNode objExprType, final boolean isStatic, final VariableScope scope) {
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
                for (ClassNode face : result.declaringType.getAllInterfaces()) {
                    if (face.equals(VariableScope.MAP_CLASS_NODE)) { // Map<K,V>
                        GenericsType[] generics = GroovyUtils.getGenericsTypes(face);
                        if (generics.length == 2) inferredType = generics[1].getType();
                        break;
                    }
                }
            }
            TypeLookupResult tlr = new TypeLookupResult(inferredType, result.declaringType, result.declaration, TypeConfidence.INFERRED, result.scope);
            tlr.enclosingAnnotation = result.enclosingAnnotation;
            tlr.enclosingAssignment = result.enclosingAssignment;
            result = tlr;
        }
        return result.resolveTypeParameterization(objExprType, isStatic);
    }

    private VisitStatus notifyRequestor(final ASTNode node, final ITypeRequestor requestor, final TypeLookupResult result) {
        // result is never null because SimpleTypeLookup always returns non-null
        return requestor.acceptASTNode(node, result, enclosingElement);
    }

    private void postVisit(final Expression node, final ClassNode type, final ClassNode declaringType, final ASTNode declaration) {
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
        Assert.isTrue(scopes.isEmpty(), String.format(SANITY_CHECK_MESSAGE, "Variable scope"));
        Assert.isTrue(primaryTypeStack.isEmpty(), String.format(SANITY_CHECK_MESSAGE, "Primary type"));
        Assert.isTrue(dependentTypeStack.isEmpty(), String.format(SANITY_CHECK_MESSAGE, "Dependent type"));
        Assert.isTrue(completeExpressionStack.isEmpty(), String.format(SANITY_CHECK_MESSAGE, "Expression"));
        Assert.isTrue(dependentDeclarationStack.isEmpty(), String.format(SANITY_CHECK_MESSAGE, "Declaration"));
    }

    private static final String SANITY_CHECK_MESSAGE =
        "Inferencing engine in invalid state after visitor completed.  %s stack should be empty after visit completed.";

    //--------------------------------------------------------------------------

    /**
     * Get the module node. Potentially forces creation of a new module node if the working copy owner is non-default. This is
     * necessary because a non-default working copy owner implies that this may be a search related to refactoring and therefore,
     * the ModuleNode must be based on the most recent working copies.
     */
    private static ModuleNodeInfo createModuleNode(final GroovyCompilationUnit unit) {
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
     * @return a closure type parameterized by {@code t}
     */
    private static ClassNode createParameterizedClosure(final ClassNode t) {
        ClassNode closure = VariableScope.clonedClosure();
        resetType(closure.getGenericsTypes()[0], t);
        return closure;
    }

    /**
     * @return a list type parameterized by {@code t}
     */
    private static ClassNode createParameterizedList(final ClassNode t) {
        ClassNode list = VariableScope.clonedList();
        resetType(list.getGenericsTypes()[0], t);
        return list;
    }

    /**
     * @return a map type parameterized by {@code k} and {@code v}
     */
    private static ClassNode createParameterizedMap(final ClassNode k, final ClassNode v) {
        ClassNode map = VariableScope.clonedMap();
        resetType(map.getGenericsTypes()[0], k);
        resetType(map.getGenericsTypes()[1], v);
        return map;
    }

    /**
     * @return a range type parameterized by {@code t}
     */
    private static ClassNode createParameterizedRange(final ClassNode t) {
        ClassNode range = VariableScope.clonedRange();
        resetType(range.getGenericsTypes()[0], t);
        return range;
    }

    /**
     * @see org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor#adjustTypeForSpreading
     */
    private static ClassNode createSpreadResult(ClassNode t, final ClassNode objExprType) {
        ClassNode elementType = VariableScope.extractElementType(objExprType);
        if (GeneralUtils.isOrImplements(elementType, VariableScope.COLLECTION_CLASS_NODE)) {
            // TODO: clone elementType and replace deepest Collection's generic type with t
            t = GenericsUtils.nonGeneric(elementType);
        }
        return createParameterizedList(t);
    }

    /**
     * Mutually exclusive to {@link #findBinaryOperatorName(String)}.
     *
     * @param operator the operation of this binary expression
     * @param lhs the type of the lhs of the binary expression
     * @param rhs the type of the rhs of the binary expression
     * @return the determined type of the binary expression
     */
    private static ClassNode findBinaryExpressionType(final String operator, final ClassNode lhs, final ClassNode rhs) {
        switch (operator.charAt(0)) {
        case '<':
            if (operator.length() == 3 && operator.charAt(1) == '=' && operator.charAt(2) == '>') {
                return VariableScope.INTEGER_CLASS_NODE;
            }
            // fall through
        case '>':
        case '!':
            // includes "!=" and "!=="
        case 'i':
            // includes "in" and "instanceof"
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
    private static String findBinaryOperatorName(final String text) {
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
            if (text.length() == 2 && text.charAt(1) == 'n') {
                return "isCase";
            }
        }
        return null;
    }

    public static Expression findCategoryTarget(final ClassNode node) {
        return GroovyUtils.getAnnotations(node, "groovy.lang.Category")
            .findFirst().map(an -> an.getMember("value")).orElse(null);
    }

    private static Parameter findTargetParameter(final Expression arg, final MethodCallExpression call, final MethodNode declaration, final boolean isGroovyMethod) {
        // see ExpressionCompletionRequestor.getParameterPosition(ASTNode, MethodCallExpression)
        // see CompletionNodeFinder.isArgument(Expression, List<? extends Expression>)
        String key = null;
        int pos = -1;
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
    private static String findUnaryOperatorName(final String text) {
        switch (text.charAt(0)) {
        case '+':
            if (text.length() == 2 && text.charAt(1) == '+') {
                return "next";
            }
            return "positive";
        case '-':
            if (text.length() == 2 && text.charAt(1) == '-') {
                return "previous";
            }
            return "negative";
        case '~':
            return "bitwiseNegate";
        case ']':
            return "putAt";
        }
        return null;
    }

    private static Parameter[] getPositionalParameters(final MethodNode methodNode) {
        if (Arrays.stream(methodNode.getClass().getInterfaces()).anyMatch(face -> face.getSimpleName().equals("MethodNodeWithNamedParams"))) {
            Parameter[] parameters = ReflectionUtils.executePrivateMethod(methodNode.getClass(), "getPositionalParams", methodNode);
            return parameters;
        }
        return methodNode.getParameters();
    }

    private static Map<String, ClassNode[]> inferInstanceOfType(final Expression expression, final VariableScope scope) {
        // check for "if (x instanceof y) { ... }" flow typing
        if (expression instanceof BinaryExpression) {
            BinaryExpression be = (BinaryExpression) expression;
            // check for "if (x == null || x instanceof y) { .. }" or
            //  "if (x != null && x instanceof y) { .. }" flow typing
            BinaryExpression nsbe = nullSafeBinaryExpression(be);
            if (nsbe != null) {
                be = nsbe;
            }
            if (isInstanceOf(be) && be.getLeftExpression() instanceof VariableExpression) {
                VariableExpression ve = (VariableExpression) be.getLeftExpression();
                VariableScope.VariableInfo vi = scope.lookupName(ve.getName());
                if (vi != null && vi.type != null && GroovyUtils.isAssignable(be.getRightExpression().getType(), vi.type)) {
                    return Collections.singletonMap(vi.name, be.getOperation().getType() == Types.KEYWORD_INSTANCEOF
                        ? new ClassNode[] {be.getRightExpression().getType(), null} // instanceof
                        : new ClassNode[] {null, be.getRightExpression().getType()} // !instanceof
                    );
                }
            }
        } else if (expression instanceof BooleanExpression) {
            Map<String, ClassNode[]> types = inferInstanceOfType(((BooleanExpression) expression).getExpression(), scope);
            if (!types.isEmpty()) {
                // check for "if (!(x instanceof y)) { ... } else { ... }"
                if (expression instanceof NotExpression) {
                    for (ClassNode[] value : types.values()) {
                        ClassNode type = value[0];
                        value[0] = value[1];
                        value[1] = type;
                    }
                }
                return types;
            }
        }
        return Collections.emptyMap();
    }

    /**
     * Makes assumption that no one has overloaded the basic arithmetic operations on numbers.
     * These operations will bypass the mop in most situations anyway.
     */
    private static boolean isArithmeticOperationOnListOrNumberOrString(final String text, final ClassNode lhs, final ClassNode rhs) {
        if (text.length() != 1) {
            return false;
        }

        switch (text.charAt(0)) {
        case '+':
        case '-':
            if (GeneralUtils.isOrImplements(lhs, VariableScope.LIST_CLASS_NODE)) {
                return true;
            }
            // fall through
        case '*':
        case '/':
        case '%':
            return ClassHelper.getWrapper(lhs).isDerivedFrom(VariableScope.NUMBER_CLASS_NODE) || lhs.equals(VariableScope.STRING_CLASS_NODE) || lhs.equals(VariableScope.GSTRING_CLASS_NODE);
        default:
            return false;
        }
    }

    private static boolean isEnumInit(final MethodCallExpression node) {
        return (node.getType().isEnum() && node.getMethodAsString().equals("$INIT"));
    }

    private static boolean isEnumInit(final StaticMethodCallExpression node) {
        return (node.getOwnerType().isEnum() && node.getMethodAsString().equals("$INIT"));
    }

    private static boolean isInstanceOf(final BinaryExpression node) {
        // Groovy 3+: return node.getOperation().isA(Types.INSTANCEOF_OPERATOR);
        switch (node.getOperation().getType()) {
        case Types.KEYWORD_INSTANCEOF:
        case 130/*NOT_INSTANCEOF*/:
            return true;
        default:
            return false;
        }
    }

    private static boolean isLazy(final FieldNode fieldNode) {
        return isNotEmpty(GroovyUtils.getAnnotations(fieldNode, "groovy.lang.Lazy"));
    }

    private static boolean isMetaAnnotation(final ClassNode classNode) {
        return isNotEmpty(GroovyUtils.getAnnotations(classNode, "groovy.transform.AnnotationCollector"));
    }

    private static boolean isNotEmpty(final List<?> list) {
        return (list != null && !list.isEmpty());
    }

    private static boolean isNotEmpty(final Stream<?> stream) {
        return (stream != null && stream.anyMatch(x -> true));
    }

    private static VariableExpression isNotNullTest(final Expression node) {
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

    private static VariableExpression isNullTest(final Expression node) {
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
    private static BinaryExpression nullSafeBinaryExpression(final BinaryExpression node) {
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

    private static List<IMember> membersOf(final IType type, final boolean isScript) throws JavaModelException {
        boolean isEnum = type.isEnum();
        List<IMember> members = new ArrayList<>();

        for (IJavaElement child : type.getChildren()) {
            String name = child.getElementName();
            switch (child.getElementType()) {
            case IJavaElement.METHOD: // exclude synthetic members for enums
                if (!isEnum || !(name.indexOf('$') > -1 || (("next".equals(name) || "previous".equals(name)) && ((IMethod) child).getNumberOfParameters() == 0))) {
                    members.add((IMethod) child);
                }
                break;
            case IJavaElement.FIELD: // exclude synthetic members for enums
                if (!isEnum || !(name.indexOf('$') > -1 || "MIN_VALUE".equals(name) || "MAX_VALUE".equals(name))) {
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

    private static void resetType(final GenericsType gt, final ClassNode t) {
        ClassNode type = GroovyUtils.getWrapperTypeIfPrimitive(t);
        gt.setName(type.getName());
        gt.setType(type);
    }

    //--------------------------------------------------------------------------

    private static class Tuple {
        ClassNode declaringType;
        ASTNode declaration;

        Tuple(final ClassNode declaringType, final ASTNode declaration) {
            this.declaringType = declaringType;
            this.declaration = declaration;
        }
    }

    public static class VisitCompleted extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public final VisitStatus status;

        public VisitCompleted(final VisitStatus status) {
            this.status = status;
        }
    }
}
