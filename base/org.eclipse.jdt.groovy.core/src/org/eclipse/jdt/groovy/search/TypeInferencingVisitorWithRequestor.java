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
package org.eclipse.jdt.groovy.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import groovyjarjarasm.asm.Opcodes;
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
import org.codehaus.groovy.ast.Variable;
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
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
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
import org.codehaus.groovy.ast.tools.WideningCategories;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.sc.ListOfExpressionsExpression;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.ModuleNodeMapper.ModuleNodeInfo;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.Activator;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.ITypeRequestor.VisitStatus;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.groovy.search.VariableScope.CallAndType;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Visits {@link GroovyCompilationUnit" instances to determine the type of expressions they contain.
 */
public class TypeInferencingVisitorWithRequestor extends ClassCodeVisitorSupport {

    public static class VisitCompleted extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public final VisitStatus status;

        public VisitCompleted(VisitStatus status) {
            this.status = status;
        }
    }

    private static class Tuple {
        ClassNode declaringType;
        ASTNode declaration;

        Tuple(ClassNode declaringType, ASTNode declaration) {
            this.declaringType = declaringType;
            this.declaration = declaration;
        }
    }

    private static void log(Throwable t, String form, Object... args) {
        String m = "Groovy-Eclipse Type Inferencing: " + String.format(form, args);
        Status s = new Status(IStatus.ERROR, Activator.PLUGIN_ID, m, t);
        Util.log(s);
    }

    /**
     * Set to true if debug mode is desired. Any exceptions will be spit to syserr. Also, after a visit, there will be a sanity
     * check to ensure that all stacks are empty Only set to true if using a visitor that always visits the entire file
     */
    public boolean DEBUG = false;

    // shared instances for several method checks below
    private static final String[] NO_PARAMS = CharOperation.NO_STRINGS;
    private static final Parameter[] NO_PARAMETERS = Parameter.EMPTY_ARRAY;

    /**
     * We hard code the list of methods that take a closure and expect to iterate over that closure
     */
    private static final Set<String> dgmClosureMethods = new HashSet<String>();
    static {
        dgmClosureMethods.add("find");
        dgmClosureMethods.add("each");
        dgmClosureMethods.add("reverseEach");
        dgmClosureMethods.add("eachWithIndex");
        dgmClosureMethods.add("unique");
        dgmClosureMethods.add("every");
        dgmClosureMethods.add("collect");
        dgmClosureMethods.add("collectEntries");
        dgmClosureMethods.add("collectNested");
        dgmClosureMethods.add("collectMany");
        dgmClosureMethods.add("findAll");
        dgmClosureMethods.add("groupBy");
        dgmClosureMethods.add("groupEntriesBy");

        dgmClosureMethods.add("inject");
        dgmClosureMethods.add("count");
        dgmClosureMethods.add("countBy");
        dgmClosureMethods.add("findResult");
        dgmClosureMethods.add("findResults");
        dgmClosureMethods.add("grep");
        dgmClosureMethods.add("split");
        dgmClosureMethods.add("sum");
        dgmClosureMethods.add("any");
        dgmClosureMethods.add("flatten");
        dgmClosureMethods.add("findIndexOf");
        dgmClosureMethods.add("findIndexValues");
        dgmClosureMethods.add("findLastIndexOf");
        dgmClosureMethods.add("collectAll");
        dgmClosureMethods.add("min");
        dgmClosureMethods.add("max");
        dgmClosureMethods.add("eachPermutation");
        dgmClosureMethods.add("sort");
        dgmClosureMethods.add("withDefault");

        // these don't take collections, but can be handled in the same way
        dgmClosureMethods.add("identity");
        dgmClosureMethods.add("times");
        dgmClosureMethods.add("upto");
        dgmClosureMethods.add("downto");
        dgmClosureMethods.add("step");
        dgmClosureMethods.add("eachFile");
        dgmClosureMethods.add("eachDir");
        dgmClosureMethods.add("eachFileRecurse");
        dgmClosureMethods.add("eachDirRecurse");
        dgmClosureMethods.add("traverse");
    }

    // These methods have a type for the closure argument that is the same as the declaring type
    private static final Set<String> dgmClosureIdentityMethods = new HashSet<String>();
    static {
        dgmClosureIdentityMethods.add("with");
        dgmClosureIdentityMethods.add("addShutdownHook");
    }

    // these methods can be called with a collection or a map.
    // When called with a map and there are 2 closure arguments, then
    // the types are the key/value of the map entry
    private static final Set<String> dgmClosureMaybeMap = new HashSet<String>();
    static {
        dgmClosureMaybeMap.add("any");
        dgmClosureMaybeMap.add("every");
        dgmClosureMaybeMap.add("each");
        dgmClosureMaybeMap.add("collect");
        dgmClosureMaybeMap.add("collectEntries");
        dgmClosureMaybeMap.add("findResult");
        dgmClosureMaybeMap.add("findResults");
        dgmClosureMaybeMap.add("findAll");
        dgmClosureMaybeMap.add("groupBy");
        dgmClosureMaybeMap.add("groupEntriesBy");
        dgmClosureMaybeMap.add("inject");
        dgmClosureMaybeMap.add("withDefault");
    }

    // These methods have a fixed type for the closure argument
    private static final Map<String, ClassNode> dgmClosureMethodsMap = new HashMap<String, ClassNode>();
    static {
        dgmClosureMethodsMap.put("eachLine", VariableScope.STRING_CLASS_NODE);
        dgmClosureMethodsMap.put("splitEachLine", VariableScope.STRING_CLASS_NODE);
        dgmClosureMethodsMap.put("withObjectOutputStream", VariableScope.OBJECT_OUTPUT_STREAM);
        dgmClosureMethodsMap.put("withObjectInputStream", VariableScope.OBJECT_INPUT_STREAM);
        dgmClosureMethodsMap.put("withDataOutputStream", VariableScope.DATA_OUTPUT_STREAM_CLASS);
        dgmClosureMethodsMap.put("withDataInputStream", VariableScope.DATA_INPUT_STREAM_CLASS);
        dgmClosureMethodsMap.put("withOutputStream", VariableScope.OUTPUT_STREAM_CLASS);
        dgmClosureMethodsMap.put("withInputStream", VariableScope.INPUT_STREAM_CLASS);
        dgmClosureMethodsMap.put("withStream", VariableScope.OUTPUT_STREAM_CLASS);
        dgmClosureMethodsMap.put("metaClass", ClassHelper.METACLASS_TYPE);
        dgmClosureMethodsMap.put("eachFileMatch", VariableScope.FILE_CLASS_NODE);
        dgmClosureMethodsMap.put("eachDirMatch", VariableScope.FILE_CLASS_NODE);
        dgmClosureMethodsMap.put("withReader", VariableScope.BUFFERED_READER_CLASS_NODE);
        dgmClosureMethodsMap.put("withWriter", VariableScope.BUFFERED_WRITER_CLASS_NODE);
        dgmClosureMethodsMap.put("withWriterAppend", VariableScope.BUFFERED_WRITER_CLASS_NODE);
        dgmClosureMethodsMap.put("withPrintWriter", VariableScope.PRINT_WRITER_CLASS_NODE);
        dgmClosureMethodsMap.put("transformChar", VariableScope.STRING_CLASS_NODE);
        dgmClosureMethodsMap.put("transformLine", VariableScope.STRING_CLASS_NODE);
        dgmClosureMethodsMap.put("filterLine", VariableScope.STRING_CLASS_NODE);
        dgmClosureMethodsMap.put("eachMatch", VariableScope.STRING_CLASS_NODE);
    }

    private final GroovyCompilationUnit unit;

    private final LinkedList<VariableScope> scopes = new LinkedList<VariableScope>();

    // we are going to have to be very careful about the ordering of lookups
    // Simple type lookup must be last because it always returns an answer
    // Assume that if something returns an answer, then we go with that.
    // Later on, should do some ordering of results
    private final ITypeLookup[] lookups;

    private ITypeRequestor requestor;
    private IJavaElement enclosingElement;
    private ASTNode enclosingDeclarationNode;
    private BinaryExpression enclosingAssignment;
    private ConstructorCallExpression enclosingConstructorCall;

    /**
     * The head of the stack is the current property/attribute/methodcall/binary expression being visited. This stack is used so we
     * can keep track of the type of the object expressions in these property expressions
     */
    private final LinkedList<ASTNode> completeExpressionStack = new LinkedList<ASTNode>();

    /**
     * Keeps track of the type of the object expression corresponding to each frame of the property expression.
     */
    private final LinkedList<ClassNode> primaryTypeStack = new LinkedList<ClassNode>();

    /**
     * Keeps track of the declaring type of the current dependent expression. Dependent expressions are dependent on a primary
     * expression to find type information. this field is only applicable for {@link PropertyExpression}s and
     * {@link MethodCallExpression}s.
     */
    private final LinkedList<Tuple> dependentDeclarationStack = new LinkedList<Tuple>();

    /**
     * Keeps track of the type of the type of the property field corresponding to each frame of the property expression.
     */
    private final LinkedList<ClassNode> dependentTypeStack = new LinkedList<ClassNode>();

    /**
     * Keeps track of closures types.
     */
    private LinkedList<Map<ClosureExpression, ClassNode>> closureTypes = new LinkedList<Map<ClosureExpression, ClassNode>>();

    private final JDTResolver resolver;

    private final AssignmentStorer assignmentStorer = new AssignmentStorer();

    /**
     * Keeps track of local map variables contexts.
     */
    private Map<Variable, Map<String, ClassNode>> localMapProperties = new HashMap<Variable, Map<String, ClassNode>>();
    private Variable currentMapVariable;

    /**
     * Use factory to instantiate
     */
    TypeInferencingVisitorWithRequestor(GroovyCompilationUnit unit, ITypeLookup[] lookups) {
        super();
        this.unit = unit;
        this.lookups = lookups;
        ModuleNodeInfo info = createModuleNode(unit);
        this.resolver = info != null ? info.resolver : null;
        this.enclosingDeclarationNode = info != null ? info.module : null;
    }

    //--------------------------------------------------------------------------

    public void visitCompilationUnit(ITypeRequestor requestor) {
        if (enclosingDeclarationNode == null) {
            // no module node, can't do anything
            return;
        }

        this.requestor = requestor;
        this.enclosingElement = unit;
        VariableScope topLevelScope = new VariableScope(null, enclosingDeclarationNode, false);
        scopes.add(topLevelScope);

        for (ITypeLookup lookup : lookups) {
            if (lookup instanceof ITypeResolver) {
                ((ITypeResolver) lookup).setResolverInformation((ModuleNode) enclosingDeclarationNode, resolver);
            }
            lookup.initialize(unit, topLevelScope);
        }

        try {
            visitPackage(((ModuleNode) enclosingDeclarationNode).getPackage());
            visitImports((ModuleNode) enclosingDeclarationNode);
            for (IType type : unit.getTypes()) {
                visitJDT(type, requestor);
            }
            scopes.removeLast();

        } catch (VisitCompleted vc) {
            // can ignore
        } catch (Exception e) {
            log(e, "Error visiting types for %s", unit.getElementName());
            if (DEBUG) {
                System.err.println("Excpetion thrown from inferencing engine");
                e.printStackTrace();
            }
        }
        if (DEBUG) {
            postVisitSanityCheck();
        }
    }

    public void visitJDT(IType type, ITypeRequestor requestor) {
        IJavaElement oldEnclosing = enclosingElement;
        ASTNode oldEnclosingNode = enclosingDeclarationNode;
        enclosingElement = type;
        ClassNode node = findClassNode(createName(type));
        if (node == null) {
            // probably some sort of AST transformation is making this node invisible
            return;
        }
        try {
            scopes.add(new VariableScope(scopes.getLast(), node, false));
            enclosingDeclarationNode = node;
            visitClassInternal(node);

            try {
                // visitJDT so that we have the proper enclosing element
                for (IJavaElement child : type.getChildren()) {
                    // filter out synthetic members for enums
                    if (type.isEnum() && shouldFilterEnumMember(child)) {
                        continue;
                    }
                    switch (child.getElementType()) {
                        case IJavaElement.METHOD:
                            visitJDT((IMethod) child, requestor);
                            break;
                        case IJavaElement.FIELD:
                            visitJDT((IField) child, requestor);
                            break;
                        case IJavaElement.TYPE:
                            visitJDT((IType) child, requestor);
                            break;
                    }
                }

                if (!type.isEnum()) {
                    if (node.isScript()) {
                        // visit fields that were created by @Field
                        for (FieldNode field : node.getFields()) {
                            if (field.getEnd() > 0) {
                                if (field.getNameEnd() <= 0) {
                                    setNameLocation(field);
                                }
                                visitField(field);
                            }
                        }
                    } else {
                        // visit fields that were relocated by @Trait
                        @SuppressWarnings("unchecked")
                        List<FieldNode> traitFields = (List<FieldNode>) node.getNodeMetaData("trait.fields");
                        if (traitFields != null) {
                            for (FieldNode field : traitFields) {
                                visitField(field);
                            }
                        }
                    }

                    // visit synthetic default constructor; this is where the object initializers are stuffed
                    // this constructor has no JDT counterpart since it doesn't exist in the source code
                    if (!type.getMethod(type.getElementName(), NO_PARAMS).exists()) {
                        ConstructorNode defConstructor = findDefaultConstructor(node);
                        if (defConstructor != null) {
                            visitConstructorOrMethod(defConstructor, true);
                        }
                    }
                }

                // visit relocated @Memoized method bodies
                for (MethodNode method : node.getMethods()) {
                    if (method.getName().startsWith("memoizedMethodPriv$")) {
                        visitClassCodeContainer(method.getCode());
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
            enclosingElement = oldEnclosing;
            enclosingDeclarationNode = oldEnclosingNode;
            scopes.removeLast();
        }
    }

    public void visitJDT(IField field, ITypeRequestor requestor) {
        IJavaElement oldEnclosing = enclosingElement;
        ASTNode oldEnclosingNode = enclosingDeclarationNode;
        enclosingElement = field;
        this.requestor = requestor;
        FieldNode fieldNode = findFieldNode(field);
        if (fieldNode == null) {
            // probably some sort of AST transformation is making this node invisible
            return;
        }

        enclosingDeclarationNode = fieldNode;
        scopes.add(new VariableScope(scopes.getLast(), fieldNode, fieldNode.isStatic()));
        try {
            visitField(fieldNode);
        } catch (VisitCompleted vc) {
            if (vc.status == VisitStatus.STOP_VISIT) {
                throw vc;
            }
        } finally {
            enclosingDeclarationNode = oldEnclosingNode;
            enclosingElement = oldEnclosing;
            scopes.removeLast();
        }

        if (isLazy(fieldNode)) {
            MethodNode lazyMethod = getLazyMethod(field.getElementName());
            if (lazyMethod != null) {
                enclosingDeclarationNode = lazyMethod;
                scopes.add(new VariableScope(scopes.getLast(), lazyMethod, lazyMethod.isStatic()));
                try {
                    visitConstructorOrMethod(lazyMethod, lazyMethod instanceof ConstructorNode);
                } catch (VisitCompleted vc) {
                    if (vc.status == VisitStatus.STOP_VISIT) {
                        throw vc;
                    }
                } finally {
                    scopes.removeLast();
                    enclosingElement = oldEnclosing;
                    enclosingDeclarationNode = oldEnclosingNode;
                }
            }
        }
    }

    public void visitJDT(IMethod method, ITypeRequestor requestor) {
        IJavaElement oldEnclosing = enclosingElement;
        ASTNode oldEnclosingNode = enclosingDeclarationNode;
        enclosingElement = method;
        MethodNode methodNode = findMethodNode(method);
        if (methodNode == null) {
            // probably some sort of AST transformation is making this node invisible
            return;
        }

        enclosingDeclarationNode = methodNode;
        this.requestor = requestor;
        scopes.add(new VariableScope(scopes.getLast(), methodNode, methodNode.isStatic()));
        try {
            visitConstructorOrMethod(methodNode, method.isConstructor());

            // check for anonymous inner types
            IJavaElement[] children = method.getChildren();
            for (IJavaElement child : children) {
                if (child.getElementType() == IJavaElement.TYPE) {
                    visitJDT((IType) child, requestor);
                }
            }
        } catch (VisitCompleted vc) {
            if (vc.status == VisitStatus.STOP_VISIT) {
                throw vc;
            }
        } catch (Exception e) {
            log(e, "Error visiting method %s in class %s", method.getElementName(), method.getParent().getElementName());
        } finally {
            enclosingElement = oldEnclosing;
            enclosingDeclarationNode = oldEnclosingNode;
            scopes.removeLast();
        }
    }

    //

    @Override
    public void visitPackage(PackageNode node) {
        if (node != null) {
            visitAnnotations(node);

            TypeLookupResult result = new TypeLookupResult(null, null, node, TypeConfidence.EXACT, null);
            VisitStatus status = notifyRequestor(node, requestor, result);
            if (status == VisitStatus.STOP_VISIT) {
                throw new VisitCompleted(status);
            }
        }
    }

    @Override
    public void visitImports(ModuleNode node) {
        for (ImportNode imp : GroovyUtils.getAllImportNodes(node)) {
            IJavaElement oldEnclosingElement = enclosingElement;

            visitAnnotations(imp);

            // this will not work for static or * imports, but that's OK because
            // as of now, there is no reason to do that.
            ClassNode type = imp.getType();
            if (type != null) {
                String importName = imp.getClassName().replace('$', '.') +
                        (imp.getFieldName() != null ? "." + imp.getFieldName() : "");
                enclosingElement = unit.getImport(importName);
                if (!enclosingElement.exists()) {
                    enclosingElement = oldEnclosingElement;
                }
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
                        } catch (VisitCompleted e) {
                            if (e.status == VisitStatus.STOP_VISIT) {
                                throw e;
                            }
                        }
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

    private void visitClassInternal(ClassNode node) {
        if (resolver != null) {
            resolver.currentClass = node;
        }
        VariableScope scope = scopes.getLast();
        scope.addVariable("this", node, node);

        visitAnnotations(node);

        TypeLookupResult result = new TypeLookupResult(node, node, node, TypeConfidence.EXACT, scope);
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
            visitClassReference(node.getUnresolvedSuperClass());
        }

        for (ClassNode face : node.getInterfaces()) {
            visitClassReference(face);
        }

        // TODO: Should all methods w/o peer in JDT model have their bodies visited?  Below are two cases in particular.

        if (isMetaAnnotation(node)) {
            // visit relocated @AnnotationCollector annotations
            MethodNode value = node.getMethod("value", NO_PARAMETERS);
            if (value != null && value.getEnd() < 1) {
                visitClassCodeContainer(value.getCode());
            }
        }

        // visit <clinit> body because this is where static field initializers are placed
        // only visit field initializers here.
        // it is important here to get the right variable scope for the initializer.
        // need to ensure that the field is one of the enclosing nodes
        MethodNode clinit = node.getMethod("<clinit>", NO_PARAMETERS);
        if (clinit != null && clinit.getCode() instanceof BlockStatement) {
            for (Statement element : (Iterable<Statement>) ((BlockStatement) clinit.getCode()).getStatements()) {
                // only visit the static initialization of a field
                if (element instanceof ExpressionStatement
                        && ((ExpressionStatement) element).getExpression() instanceof BinaryExpression) {
                    BinaryExpression bexpr = (BinaryExpression) ((ExpressionStatement) element).getExpression();
                    if (bexpr.getLeftExpression() instanceof FieldExpression) {
                        FieldNode f = ((FieldExpression) bexpr.getLeftExpression()).getField();
                        if (f != null && f.isStatic() && bexpr.getRightExpression() != null) {
                            // create the field scope so that it looks like we are visiting within the context of the field
                            VariableScope fieldScope = new VariableScope(scope, f, true);
                            scopes.add(fieldScope);
                            try {
                                bexpr.getRightExpression().visit(this);
                            } finally {
                                scopes.removeLast();
                            }
                        }
                    }
                }
            }
        }

        // I'm not actually sure that there will be anything here. I think these will all be moved to a constructor
        for (Statement element : node.getObjectInitializerStatements()) {
            element.visit(this);
        }

        // visit synthetic no-arg constructors because that's where the non-static initializers are
        for (ConstructorNode constructor : node.getDeclaredConstructors()) {
            if (constructor.isSynthetic() && (constructor.getParameters() == null || constructor.getParameters().length == 0)) {
                visitConstructor(constructor);
            }
        }
        // don't visit contents, the visitJDT methods are used instead
    }

    private void visitClassReference(ClassNode node) {
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
                        attr.setStart(node.getEnd() + 2); attr.setEnd(attr.getStart() + name.length());

                        noLookup = new TypeLookupResult(ClassHelper.VOID_TYPE,
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
    public void visitArgumentlistExpression(ArgumentListExpression node) {
        CallAndType callAndType = scopes.getLast().getEnclosingMethodCallExpression();
        boolean closureFound = false;
        if (callAndType != null && callAndType.declaration instanceof MethodNode) {
            Map<ClosureExpression, ClassNode> map = new HashMap<ClosureExpression, ClassNode>();
            MethodNode methodNode = (MethodNode) callAndType.declaration;
            if (node.getExpressions().size() == methodNode.getParameters().length) {
                for (int i = 0; i < node.getExpressions().size(); i++) {
                    if (node.getExpression(i) instanceof ClosureExpression) {
                        map.put((ClosureExpression) node.getExpression(i), methodNode.getParameters()[i].getType());
                        closureFound = true;
                    }
                }
            }
            if (closureFound) {
                closureTypes.addLast(map);
            }
        }
        visitTupleExpression(node);
        if (closureFound) {
            closureTypes.removeLast();
        }
    }

    @Override
    public void visitArrayExpression(ArrayExpression node) {
        boolean shouldContinue = handleSimpleExpression(node);
        if (shouldContinue) {
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

        // don't visit binary expressions in a constructor that have no source location.
        // the reason is that these were copied from the field initializer.
        // we want to visit them under the field initializer, not the construcor
        if (node instanceof DeclarationExpression && node.getEnd() == 0) {
            return;
        }

        visitAnnotations(node);

        boolean isAssignment = node.getOperation().getType() == Types.EQUALS;
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
                if (associatedMethod.equals("getAt") && result.declaringType.equals(VariableScope.DGM_CLASS_NODE)) {
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
        scopes.add(new VariableScope(scopes.getLast(), block, false));
        boolean shouldContinue = handleStatement(block);
        if (shouldContinue) {
            super.visitBlockStatement(block);
        }
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
            super.visitCastExpression(node);
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
        scopes.removeLast();
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

            ClassNode[] inferredParamTypes = inferClosureParamTypes(scope, node);
            Parameter[] parameters = node.getParameters(); final int n;
            if (parameters != null && (n = parameters.length) > 0) {
                handleParameterList(parameters);
                // TODO: If this is moved above handleParameterList, the inferred type will be available to the param nodes as well.
                for (int i = 0; i < n; i += 1) {
                    Parameter parameter = parameters[i];
                    // only reset the type of the parametrers if it is not explicitly defined
                    if (inferredParamTypes[i] != VariableScope.OBJECT_CLASS_NODE && parameter.isDynamicTyped()) {
                        parameter.setType(inferredParamTypes[i]);
                        scope.addVariable(parameter);
                    }
                }
            } else
            // it variable only exists if there are no explicit parameters
            if (inferredParamTypes[0] != VariableScope.OBJECT_CLASS_NODE && !scope.containsInThisScope("it")) {
                scope.addVariable("it", inferredParamTypes[0], VariableScope.OBJECT_CLASS_NODE);
            }
            if (scope.lookupNameInCurrentScope("it") == null) {
                inferItType(node, scope);
            }

            // Delegate is the declaring type of the enclosing call if one exists, or it is 'this'
            CallAndType cat = scope.getEnclosingMethodCallExpression();
            if (cat != null) {
                ClassNode declaringType = cat.declaringType;
                if (cat.delegatesToClosures.containsKey(node)) {
                    declaringType = cat.delegatesToClosures.get(node);
                }
                scope.addVariable("delegate", declaringType, VariableScope.CLOSURE_CLASS_NODE);
                scope.addVariable("getDelegate", declaringType, VariableScope.CLOSURE_CLASS_NODE);
            } else {
                ClassNode thisType = scope.getThis();
                // GRECLIPSE-1348 someone is silly enough to have a variable named "delegate".
                // don't override that
                if (scope.lookupName("delegate") == null) {
                    scope.addVariable("delegate", thisType, VariableScope.CLOSURE_CLASS_NODE);
                }
                scope.addVariable("getDelegate", thisType, VariableScope.CLOSURE_CLASS_NODE);
            }

            // Owner is 'this' if no enclosing closure, or 'Closure' if there is
            if (parent.getEnclosingClosure() != null) {
                scope.addVariable("getOwner", VariableScope.CLOSURE_CLASS_NODE, VariableScope.CLOSURE_CLASS_NODE);
                scope.addVariable("owner", VariableScope.CLOSURE_CLASS_NODE, VariableScope.CLOSURE_CLASS_NODE);
            } else {
                ClassNode thisType = scope.getThis();
                // GRECLIPSE-1348 someone is silly enough to have a variable named "owner".
                // don't override that
                if (scope.lookupName("owner") == null) {
                    scope.addVariable("owner", thisType, VariableScope.CLOSURE_CLASS_NODE);
                }
                scope.addVariable("getOwner", thisType, VariableScope.CLOSURE_CLASS_NODE);

                // only do this if we are not already in a closure; no need to add twice
                scope.addVariable("thisObject", VariableScope.OBJECT_CLASS_NODE, VariableScope.CLOSURE_CLASS_NODE);
                scope.addVariable("getThisObject", VariableScope.OBJECT_CLASS_NODE, VariableScope.CLOSURE_CLASS_NODE);
                scope.addVariable("resolveStategy", VariableScope.INTEGER_CLASS_NODE, VariableScope.CLOSURE_CLASS_NODE);
                scope.addVariable("getResolveStategy", VariableScope.INTEGER_CLASS_NODE, VariableScope.CLOSURE_CLASS_NODE);
                scope.addVariable("directive", VariableScope.INTEGER_CLASS_NODE, VariableScope.CLOSURE_CLASS_NODE);
                scope.addVariable("getDirective", VariableScope.INTEGER_CLASS_NODE, VariableScope.CLOSURE_CLASS_NODE);
                scope.addVariable("maximumNumberOfParameters", VariableScope.INTEGER_CLASS_NODE, VariableScope.CLOSURE_CLASS_NODE);
                scope.addVariable("getMaximumNumberOfParameters", VariableScope.INTEGER_CLASS_NODE, VariableScope.CLOSURE_CLASS_NODE);
                scope.addVariable("parameterTypes", VariableScope.CLASS_ARRAY_CLASS_NODE, VariableScope.CLOSURE_CLASS_NODE);
                scope.addVariable("getParameterTypes", VariableScope.CLASS_ARRAY_CLASS_NODE, VariableScope.CLOSURE_CLASS_NODE);
            }

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
            visitClassReference(node.getType());
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
                GenericsType[] gens = node.getGenericsTypes();
                if (gens != null) {
                    for (GenericsType gen : gens) {
                        if (gen.getLowerBound() != null) {
                            visitClassReference(gen.getLowerBound());
                        }
                        if (gen.getUpperBounds() != null) {
                            for (ClassNode upper : gen.getUpperBounds()) {
                                visitClassReference(upper);
                            }
                        }
                        if (gen.getType() != null && gen.getType().getName().charAt(0) != '?') {
                            visitClassReference(gen.getType());
                        }
                    }
                }

                visitClassReference(node.getReturnType());
                if (node.getExceptions() != null) {
                    for (ClassNode e : node.getExceptions()) {
                        visitClassReference(e);
                    }
                }

                if (handleParameterList(node.getParameters())) {
                    super.visitConstructorOrMethod(node, isConstructor);
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
    public void visitDeclarationExpression(DeclarationExpression node) {
        // this is ok. the variable expression is visited appropriately
        visitBinaryExpression(node);
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
        TypeLookupResult result = null;
        VariableScope scope = scopes.getLast();
        assignmentStorer.storeField(node, scope);
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
                ClassNode fieldType = node.getType();
                // if two values are == then that means the type
                // is synthetic and doesn't exist in code
                // probably an enum field.
                if (fieldType != node.getDeclaringClass()) {
                    visitClassReference(fieldType);
                }
                visitAnnotations(node);
                Expression init = node.getInitialExpression();
                if (init != null) {
                    init.visit(this);
                }
            case CANCEL_BRANCH:
                return;
            case CANCEL_MEMBER:
            case STOP_VISIT:
                throw new VisitCompleted(status);
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

        scopes.removeLast();
    }

    private void visitGenericTypes(ClassNode node) {
        if (node.isUsingGenerics() && node.getGenericsTypes() != null) {
            for (GenericsType gen : node.getGenericsTypes()) {
                if (gen.getType() != null && gen.getName().charAt(0) != '?') {
                    visitClassReference(gen.getType());
                }
                if (gen.getLowerBound() != null) {
                    visitClassReference(gen.getLowerBound());
                } else if (gen.getUpperBounds() != null) {
                    for (ClassNode upper : gen.getUpperBounds()) {
                        // handle enums where the upper bound is the same as the type
                        if (!upper.getName().equals(node.getName())) {
                            visitClassReference(upper);
                        }
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
        BooleanExpression expr = node.getBooleanExpression();
        expr.visit(this);

        VariableScope s = null; // check for "if (x instanceof y) { ... }" flow typing
        if (!(expr instanceof NotExpression) && expr.getExpression() instanceof BinaryExpression) {
            BinaryExpression b = (BinaryExpression) expr.getExpression();
            if (b.getOperation().getType() == Types.KEYWORD_INSTANCEOF &&
                    b.getLeftExpression() instanceof VariableExpression) {
                VariableExpression v = (VariableExpression) b.getLeftExpression();
                VariableScope.VariableInfo i = scopes.getLast().lookupName(v.getName());
                if (i != null && GroovyUtils.isAssignable(b.getRightExpression().getType(), i.type)) {
                    s = new VariableScope(scopes.getLast(), i.scopeNode /* use the same node */, false);
                    s.addVariable(v.getName(), b.getRightExpression().getType(), v.getDeclaringClass());
                    scopes.add(s);
                }
            }
        }

        node.getIfBlock().visit(this);

        if (s != null) scopes.removeLast();
        // TODO: else, check for "if (!(x instanceof y)) { ... } else { ... }" and adjust scope of else block

        node.getElseBlock().visit(this);
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
        if (node.getExpressions().size() > 0) {
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
                ClassNode mapType = isPrimaryExpression(entry) ?
                    createParameterizedMap(key.getType(), val.getType()) : primaryTypeStack.getLast();
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
        scopes.getLast().setCurrentNode(node);
        if (isDependentExpression(node)) {
            primaryTypeStack.removeLast();
        }
        completeExpressionStack.add(node);
        node.getObjectExpression().visit(this);

        if (node.isSpreadSafe()) {
            // must find the component type of the object expression type
            ClassNode objType = primaryTypeStack.removeLast();
            primaryTypeStack.add(VariableScope.extractElementType(objType));
        }

        node.getMethod().visit(this);

        // this is the inferred return type of this method
        // must pop now before visiting any other nodes
        ClassNode returnType = dependentTypeStack.removeLast();

        // this is the inferred declaring type of this method
        Tuple t = dependentDeclarationStack.removeLast();
        CallAndType call = new CallAndType(node, t.declaringType, t.declaration);

        completeExpressionStack.removeLast();

        ClassNode catNode = isCategoryDeclaration(node);
        if (catNode != null) {
            scopes.getLast().setCategoryBeingDeclared(catNode);
        }
        VariableScope scope = scopes.getLast();

        // remember that we are inside a method call while analyzing the arguments
        scope.addEnclosingMethodCall(call);
        node.getArguments().visit(this);
        scope.forgetEnclosingMethodCall();

        // if this method call is the primary of a larger expression,
        // then pass the inferred type onwards
        if (node.isSpreadSafe()) {
            returnType = createParameterizedList(returnType);
        }

        handleCompleteExpression(node, returnType, t.declaringType);
        scopes.getLast().forgetCurrentNode();
    }

    @Override
    public void visitMethodPointerExpression(MethodPointerExpression node) {
        boolean shouldContinue = handleSimpleExpression(node);
        if (shouldContinue) {
            completeExpressionStack.add(node);
            primaryTypeStack.add(// method src
                node.getExpression().getType());

            super.visitMethodPointerExpression(node);

            // clean up the stacks
            completeExpressionStack.removeLast();
            ClassNode returnType = dependentTypeStack.removeLast();
            Tuple callParamTypes = dependentDeclarationStack.removeLast();

            // try to set Closure generics
            if (!primaryTypeStack.isEmpty() && callParamTypes.declaration != null) {
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
        ClassNode objType;
        if (isDependentExpression(node)) {
            primaryTypeStack.removeLast();
        }
        if (node.isSpreadSafe()) {
            objType = primaryTypeStack.removeLast();
            // must find the component type of the object expression type
            primaryTypeStack.add(objType = VariableScope.extractElementType(objType));
        } else {
            objType = primaryTypeStack.getLast();
        }

        if (VariableScope.MAP_CLASS_NODE.equals(objType) && node.getObjectExpression() instanceof VariableExpression && node.getProperty() instanceof ConstantExpression) {
            currentMapVariable = ((VariableExpression) node.getObjectExpression()).getAccessedVariable();
            Map<String, ClassNode> map = localMapProperties.get(currentMapVariable);
            if (map == null) {
                map = new HashMap<String, ClassNode>();
                localMapProperties.put(currentMapVariable, map);
            }
            if (enclosingAssignment != null) {
                //String key = ((ConstantExpression) node.getProperty()).getConstantName();
                String key = (String) ((ConstantExpression) node.getProperty()).getValue();
                ClassNode val = enclosingAssignment.getRightExpression().getType();
                map.put(key, val);
            }
        }

        node.getProperty().visit(this);
        currentMapVariable = null;

        // this is the type of this property expression
        ClassNode exprType = dependentTypeStack.removeLast();

        // don't care about either of these
        dependentDeclarationStack.removeLast();
        completeExpressionStack.removeLast();

        // if this property expression is the primary of a larger expression,
        // then remember the inferred type
        if (node.isSpreadSafe()) {
            // if we are dealing with a map, then a spread dot will return a list of values,
            // so use the type of the value.
            if (objType.equals(VariableScope.MAP_CLASS_NODE) && objType.getGenericsTypes() != null
                    && objType.getGenericsTypes().length == 2) {
                exprType = objType.getGenericsTypes()[1].getType();
            }
            exprType = createParameterizedList(exprType);
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
        if (isPrimaryExpression(node)) {
            visitMethodCallExpression(new MethodCallExpression(
                    new ClassExpression(node.getOwnerType()), node.getMethod(), node.getArguments()));
        }
        boolean shouldContinue = handleSimpleExpression(node);
        if (shouldContinue) {
            boolean isPresentInSource = (node.getEnd() > 0);
            if (isPresentInSource) visitClassReference(node.getOwnerType());
            if (isPresentInSource || isEnumInit(node)) super.visitStaticMethodCallExpression(node);
        }
    }

    @Override
    public void visitTernaryExpression(TernaryExpression node) {
        if (isDependentExpression(node)) {
            primaryTypeStack.removeLast();
        }
        completeExpressionStack.add(node);

        node.getBooleanExpression().visit(this);

        // arbitrarily, we choose the if clause to be the type of this expression
        node.getTrueExpression().visit(this);

        // arbirtrarily choose the 'true' expression
        // to hold the type of the ternary expression
        ClassNode exprType = primaryTypeStack.removeLast();

        node.getFalseExpression().visit(this);

        completeExpressionStack.removeLast();

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

    private void visitUnaryExpression(Expression node, Expression expression, String operation) {
        scopes.getLast().setCurrentNode(node);
        completeExpressionStack.add(node);
        if (isDependentExpression(node)) {
            primaryTypeStack.removeLast();
        }
        expression.visit(this);

        ClassNode primaryType = primaryTypeStack.removeLast();
        // now infer the type of the operator. It could have been overloaded
        String associatedMethod = findUnaryOperatorName(operation);
        ClassNode completeExprType;
        if (associatedMethod == null && primaryType.equals(VariableScope.NUMBER_CLASS_NODE)
                || ClassHelper.getWrapper(primaryType).isDerivedFrom(VariableScope.NUMBER_CLASS_NODE)) {
            completeExprType = primaryType;
        } else {
            // there is an overloadable method associated with this operation
            // convert to a constant expression and infer type
            TypeLookupResult result = lookupExpressionType(
                new ConstantExpression(associatedMethod), primaryType, false, scopes.getLast());
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
        // don't check the lookups because statements have no type.
        // but individual requestors may choose to end the visit here
        VariableScope scope = scopes.getLast();
        ClassNode declaring = scope.getDelegateOrThis();
        scope.setPrimaryNode(false);

        if (node instanceof BlockStatement) {
            for (ITypeLookup lookup : lookups) {
                if (lookup instanceof ITypeLookupExtension) {
                    // must ensure that declaring type information at the start of the block is invoked
                    ((ITypeLookupExtension) lookup).lookupInBlock((BlockStatement) node, scope);
                }
            }
        }

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

                // visit the parameter type
                visitClassReference(param.getOriginType());

                visitAnnotations(param);

                Expression init = param.getInitialExpression();
                if (init != null) {
                    init.visit(this);
                }
            }
        }
        return true;
    }

    private boolean handleSimpleExpression(Expression node) {
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
            // implicit this expressions do not have a primary type
            if (completeExpressionStack.getLast() instanceof MethodCallExpression &&
                    ((MethodCallExpression) completeExpressionStack.getLast()).isImplicitThis()) {
                primaryType = null;
            }
            isStatic = hasStaticObjectExpression(node);
            scope.setMethodCallArgumentTypes(getMethodCallArgumentTypes(completeExpressionStack.getLast()));
            scope.setMethodCallGenericsTypes(getMethodCallGenericsTypes(completeExpressionStack.getLast()));
        }
        scope.setPrimaryNode(primaryType == null);

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
            rememberedDeclaringType = primaryType != null ? primaryType : scope.getDelegateOrThis();
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

    private ClassNode findClassNode(String name) {
        for (ClassNode clazz : findModuleNode().getClasses()) {
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
            // GRECLIPSE-578 might be @Lazy. Name is changed
            fieldNode = clazz.getField("$" + field.getElementName());
        }
        return fieldNode;
    }

    private MethodNode findMethodNode(IMethod method) {
        // FIXADE TODO pass this in as a parameter
        ClassNode clazz = findClassNode(createName(method.getDeclaringType()));
        try {
            if (method.isConstructor()) {
                List<ConstructorNode> constructors = clazz.getDeclaredConstructors();
                if (constructors == null || constructors.isEmpty()) {
                    return null;
                }
                String[] jdtParamTypes = method.getParameterTypes() == null ? NO_PARAMS : method.getParameterTypes();
                outer: for (ConstructorNode constructorNode : constructors) {
                    Parameter[] groovyParams = constructorNode.getParameters() == null ? NO_PARAMETERS : constructorNode.getParameters();
                    if (groovyParams != null && groovyParams.length > 0) {
                        int implicitParamCount = 0;
                        if (method.getDeclaringType().isEnum()) implicitParamCount = 2;
                        if (groovyParams[0].getName().startsWith("$")) implicitParamCount = 1;
                        // ignore implicit constructor parameters of constructors for enums or inner types
                        if (implicitParamCount > 0) {
                            Parameter[] newGroovyParams = new Parameter[groovyParams.length - implicitParamCount];
                            System.arraycopy(groovyParams, implicitParamCount, newGroovyParams, 0, newGroovyParams.length);
                            groovyParams = newGroovyParams;
                        }
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
                List<MethodNode> methods = clazz.getMethods(method.getElementName());
                if (methods.isEmpty()) {
                    return null;
                }
                String[] jdtParamTypes = method.getParameterTypes() == null ? NO_PARAMS : method.getParameterTypes();
                outer: for (MethodNode methodNode : methods) {
                    Parameter[] groovyParams = methodNode.getParameters() == null ? NO_PARAMETERS : methodNode.getParameters();
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

    private ModuleNode findModuleNode(/*declaration*/) {
        if (enclosingDeclarationNode instanceof ModuleNode) {
            return (ModuleNode) enclosingDeclarationNode;
        } else if (enclosingDeclarationNode instanceof ClassNode) {
            return ((ClassNode) enclosingDeclarationNode).getModule();
        } else if (enclosingDeclarationNode instanceof MethodNode) {
            return ((MethodNode) enclosingDeclarationNode).getDeclaringClass().getModule();
        } else if (enclosingDeclarationNode instanceof FieldNode) {
            return ((FieldNode) enclosingDeclarationNode).getDeclaringClass().getModule();
        } else {
            throw new IllegalArgumentException("Invalid enclosing declaration node: " + enclosingDeclarationNode);
        }
    }

    private MethodNode getLazyMethod(String fieldName) {
        ClassNode classNode = (ClassNode) enclosingDeclarationNode;
        return classNode.getDeclaredMethod("get" + MetaClassHelper.capitalize(fieldName), NO_PARAMETERS);
    }

    private List<ClassNode> getMethodCallArgumentTypes(ASTNode node) {
        // TODO: Check for MethodCall once 2.1 is the minimum supported Groovy runtime
        Expression arguments = null;
        if (node instanceof MethodCallExpression) {
            arguments = ((MethodCallExpression) node).getArguments();
        } else if (node instanceof ConstructorCallExpression) {
            arguments = ((ConstructorCallExpression) node).getArguments();
        } else if (node instanceof StaticMethodCallExpression) {
            arguments = ((StaticMethodCallExpression) node).getArguments();
        }

        if (arguments != null) {
            if (arguments instanceof ArgumentListExpression) {
                List<Expression> expressions = ((ArgumentListExpression) arguments).getExpressions();
                if (!expressions.isEmpty()) {
                    List<ClassNode> types = new ArrayList<ClassNode>(expressions.size());
                    for (Expression expression : expressions) {
                        if (expression instanceof ConstantExpression &&
                            ((ConstantExpression) expression).isNullExpression()) {

                            types.add(VariableScope.NULL_TYPE); // sentinel value
                        } else {
                            scopes.getLast().setMethodCallArgumentTypes(getMethodCallArgumentTypes(expression));
                            TypeLookupResult tlr = lookupExpressionType(expression, null, false, scopes.getLast());

                            types.add(tlr.type);
                        }
                    }
                    return types;
                }
            }
            // TODO: Might be useful to look into TupleExpression
            return Collections.emptyList();
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

    private void inferItType(ClosureExpression node, VariableScope scope) {
        if (closureTypes.isEmpty()) {
            return;
        }
        ClassNode closureType = closureTypes.getLast().get(node);
        if (closureType != null) {
            // Try to find single abstract method with single parameter
            MethodNode method = null;
            for (MethodNode methodNode : closureType.getMethods()) {
                if (methodNode.isAbstract() && methodNode.getParameters().length == 1) {
                    if (method != null) {
                        return;
                    } else {
                        method = methodNode;
                    }
                }
            }
            if (method != null) {
                ClassNode inferredType = method.getParameters()[0].getType();
                scope.addVariable("it", inferredType, VariableScope.OBJECT_CLASS_NODE);
            }
        }
    }

    private ClassNode isCategoryDeclaration(MethodCallExpression node) {
        String methodAsString = node.getMethodAsString();
        if (methodAsString != null && methodAsString.equals("use")) {
            Expression exprs = node.getArguments();
            if (exprs instanceof ArgumentListExpression) {
                ArgumentListExpression args = (ArgumentListExpression) exprs;
                if (args.getExpressions().size() >= 2 && args.getExpressions().get(1) instanceof ClosureExpression) {
                    // really, should be doing inference on the first expression and seeing if it
                    // is a class node, but looking up in scope is good enough for now
                    Expression expr = args.getExpressions().get(0);
                    if (expr instanceof ClassExpression) {
                        return expr.getType();
                    } else if (expr instanceof VariableExpression && expr.getText() != null) {
                        VariableScope.VariableInfo info = scopes.getLast().lookupName(expr.getText());
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
     * Dependent expressions are expressions whose type depends on another expression.
     *
     * Dependent expressions are:
     * <ul>
     * <li>right part of a non-assignment binary expression
     * <li>left part of a assignment expression
     * <li>propery (ie- right part) of a property expression
     * <li>method (ie- right part) of a method call expression
     * <li>property/field (ie- right part) of an attribute expression
     * </ul>
     *
     * Note that for statements and ternary expressions do not have any dependent
     * expression even though they have primary expressions.
     *
     * @param node expression node to check
     * @return true iff the node is the primary expression in an expression pair.
     */
    private boolean isDependentExpression(Expression node) {
        if (!completeExpressionStack.isEmpty()) {
            ASTNode complete = completeExpressionStack.getLast();
            if (complete instanceof PropertyExpression) {
                PropertyExpression prop = (PropertyExpression) complete;
                return prop.getProperty() == node;
            } else if (complete instanceof MethodCallExpression) {
                MethodCallExpression call = (MethodCallExpression) complete;
                return call.getMethod() == node;
            } else if (complete instanceof MethodPointerExpression) {
                MethodPointerExpression ref = (MethodPointerExpression) complete;
                return ref.getMethodName() == node;
            } else if (complete instanceof ImportNode) {
                ImportNode imp = (ImportNode) complete;
                return imp.getAliasExpr() == node || imp.getFieldNameExpr() == node;
            }
        }
        return false;
    }

    /**
     * Primary expressions are:
     * <ul>
     * <li>left part of a non-assignment binary expression
     * <li>right part of a assignment expression
     * <li>object (ie- left part) of a property expression
     * <li>object (ie- left part) of a method call expression
     * <li>object (ie- left part) of an attribute expression
     * <li>collection expression (ie- right part) of a for statement
     * <li>true expression (ie- right part) of a ternary expression
     * <li>first element of a list expression (the first element is assumed to be representative of all list elements)
     * <li>first element of a range expression (the first element is assumed to be representative of the range)
     * <li>Either the key OR the value expression of a {@link MapEntryExpression}
     * <li>The first {@link MapEntryExpression} of a {@link MapExpression}
     * <li>The expression of a {@link PrefixExpression}, a {@link PostfixExpression}, a {@link UnaryMinusExpression}, a
     * {@link UnaryPlusExpression}, or a {@link BitwiseNegationExpression}
     * </ul>
     *
     * @param node expression node to check
     * @return true iff the node is the primary expression in an expression pair.
     */
    private boolean isPrimaryExpression(Expression node) {
        if (!completeExpressionStack.isEmpty()) {
            ASTNode complete = completeExpressionStack.getLast();
            if (complete instanceof PropertyExpression) {
                return ((PropertyExpression) complete).getObjectExpression() == node;

            } else if (complete instanceof MethodCallExpression) {
                return ((MethodCallExpression) complete).getObjectExpression() == node;

            } else if (complete instanceof BinaryExpression) {
                BinaryExpression expr = (BinaryExpression) complete;
                // both sides of the binary expression are primary since we need
                // access to both of them when inferring binary expression types
                if (expr.getLeftExpression() == node || expr.getRightExpression() == node) {
                    return true;
                }
                // statically-compiled assignment chains need a little help
                if (!(node instanceof TupleExpression) &&
                        expr.getRightExpression() instanceof ListOfExpressionsExpression) {
                    @SuppressWarnings("unchecked")
                    List<Expression> list = (List<Expression>) ReflectionUtils.getPrivateField(
                        ListOfExpressionsExpression.class, "expressions", expr.getRightExpression());
                    // list.get(0) should be TemporaryVariableExpression
                    return (node != list.get(1) && list.get(1) instanceof MethodCallExpression);
                }

            } else if (complete instanceof AttributeExpression) {
                return ((AttributeExpression) complete).getObjectExpression() == node;

            } else if (complete instanceof TernaryExpression) {
                return ((TernaryExpression) complete).getTrueExpression() == node;

            } else if (complete instanceof ForStatement) {
                // this check is used to store the type of the collection expression
                // so that it can be assigned to the for loop variable
                return ((ForStatement) complete).getCollectionExpression() == node;

            } else if (complete instanceof ReturnStatement) {
                return ((ReturnStatement) complete).getExpression() == node;

            } else if (complete instanceof ListExpression) {
                return isNotEmpty(((ListExpression) complete).getExpressions()) &&
                        ((ListExpression) complete).getExpression(0) == node;

            } else if (complete instanceof RangeExpression) {
                return ((RangeExpression) complete).getFrom() == node;

            } else if (complete instanceof MapEntryExpression) {
                return ((MapEntryExpression) complete).getKeyExpression() == node ||
                        ((MapEntryExpression) complete).getValueExpression() == node;

            } else if (complete instanceof MapExpression) {
                return isNotEmpty(((MapExpression) complete).getMapEntryExpressions()) &&
                        ((MapExpression) complete).getMapEntryExpressions().get(0) == node;

            } else if (complete instanceof PrefixExpression) {
                return ((PrefixExpression) complete).getExpression() == node;

            } else if (complete instanceof PostfixExpression) {
                return ((PostfixExpression) complete).getExpression() == node;

            } else if (complete instanceof UnaryPlusExpression) {
                return ((UnaryPlusExpression) complete).getExpression() == node;

            } else if (complete instanceof UnaryMinusExpression) {
                return ((UnaryMinusExpression) complete).getExpression() == node;

            } else if (complete instanceof BitwiseNegationExpression) {
                return ((BitwiseNegationExpression) complete).getExpression() == node;
            }
        }
        return false;
    }

    /**
     * @return true iff the object expression associated with node is a static reference to a class declaration
     */
    private boolean hasStaticObjectExpression(Expression node) {
        boolean staticObjectExpression = false;
        if (!completeExpressionStack.isEmpty()) {
            ASTNode maybeProperty = completeExpressionStack.getLast();
            // need to call getObjectExpression and isImplicitThis w/o common interface
            if (maybeProperty instanceof PropertyExpression) {
                PropertyExpression prop = (PropertyExpression) maybeProperty;
                if (prop.getObjectExpression() instanceof ClassExpression) {
                    staticObjectExpression = true;
                } else if (prop.isImplicitThis()) {
                    staticObjectExpression = scopes.getLast().isStatic();
                }
            } else if (maybeProperty instanceof MethodCallExpression) {
                MethodCallExpression prop = (MethodCallExpression) maybeProperty;
                if (prop.getObjectExpression() instanceof ClassExpression) {
                    staticObjectExpression = true;
                } else if (prop.isImplicitThis()) {
                    staticObjectExpression = scopes.getLast().isStatic();
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
        if (TypeConfidence.UNKNOWN == result.confidence && VariableScope.MAP_CLASS_NODE.equals(result.declaringType)) {
            ClassNode inferredType = VariableScope.OBJECT_CLASS_NODE;
            if (currentMapVariable != null && node instanceof ConstantExpression) {
                // recover inferred type from property map (see visitPropertyExpression)
                Map<String, ClassNode> map = localMapProperties.get(currentMapVariable);
                //String key = ((ConstantExpression) node).getConstantName();
                String key = (String) ((ConstantExpression) node).getValue();
                ClassNode val = map.get(key);
                if (val != null)
                    inferredType = val;
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

    private void setNameLocation(FieldNode fieldNode)
            throws JavaModelException {
        fieldNode.setNameEnd(fieldNode.getEnd());
        int nameLength = fieldNode.getName().length();
        Expression init = fieldNode.getInitialExpression();
        if (init != null && !(init instanceof EmptyExpression)) {
            String fieldText = unit.getSource().substring(fieldNode.getStart(), init.getStart());
            int nameStart = fieldNode.getStart() + fieldText.lastIndexOf(fieldNode.getName());
            if (nameStart < fieldNode.getStart()) throw new JavaModelException(null, 980);
            fieldNode.setNameEnd(nameStart + nameLength);
        }
        fieldNode.setNameStart(fieldNode.getNameEnd() - nameLength);
        fieldNode.setNameEnd(fieldNode.getNameEnd() - 1); // name end index is inclusive... not sure why
    }

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
                    // localOccurrenceCount does not exist in 3.7
                    count = type.getOccurrenceCount();
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
     * @return a list parameterized by propType
     */
    private static ClassNode createParameterizedList(ClassNode propType) {
        ClassNode list = VariableScope.clonedList();
        list.getGenericsTypes()[0].setType(propType);
        list.getGenericsTypes()[0].setName(propType.getName());
        return list;
    }

    /**
     * @return a list parameterized by propType
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
     * @return a list parameterized by propType
     */
    private static ClassNode createParameterizedRange(ClassNode propType) {
        ClassNode range = VariableScope.clonedRange();
        range.getGenericsTypes()[0].setType(propType);
        range.getGenericsTypes()[0].setName(propType.getName());
        return range;
    }

    /**
     * Only handle operations that are not handled in {@link #findBinaryOperatorName(String)}
     *
     * @param operation the operation of this binary expression
     * @param lhs the type of the lhs of the binary expression
     * @param rhs the type of the rhs of the binary expression
     * @return the determined type of the binary expression
     */
    private static ClassNode findBinaryExpressionType(String operation, ClassNode lhs, ClassNode rhs) {
        char op = operation.charAt(0);
        switch (op) {
            case '*':
                if (operation.equals("*.") || operation.equals("*.@")) {
                    // can we do better and parameterize the list?
                    return VariableScope.clonedList();
                }
            case '~':
                // regex pattern
                return VariableScope.STRING_CLASS_NODE;

            case '!':
                // includes != and !== and !!
            case '<':
            case '>':
                if (operation.length() > 1) {
                    if (operation.equals("<=>")) {
                        return VariableScope.INTEGER_CLASS_NODE;
                    }
                }
                // all booleans
                return VariableScope.BOOLEAN_CLASS_NODE;

            case 'i':
                if (operation.equals("is") || operation.equals("in")) {
                    return VariableScope.BOOLEAN_CLASS_NODE;
                } else {
                    // unknown
                    return rhs;
                }

            case '.':
                if (operation.equals(".&")) {
                    return VariableScope.CLOSURE_CLASS_NODE;
                } else {
                    // includes ".", "?:", "?.", ".@"
                    return rhs;
                }

            case '=':
                if (operation.length() > 1) {
                    if (operation.charAt(1) == '=') {
                        return VariableScope.BOOLEAN_CLASS_NODE;
                    } else if (operation.charAt(1) == '~') {
                        // consider regex to be string
                        return VariableScope.MATCHER_CLASS_NODE;
                    }
                }
                // drop through

            default:
                // "as"
                // rhs by default
                return rhs;
        }
    }

    /**
     * @return the method name associated with this binary operator
     */
    private static String findBinaryOperatorName(String text) {
        char op = text.charAt(0);
        switch (op) {
            case '+':
                return "plus";
            case '-':
                return "minus";
            case '*':
                if (text.length() > 1 && text.equals("**")) {
                    return "power";
                }
                return "multiply";
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
                if (text.length() > 1 && text.equals(">>")) {
                    return "rightShift";
                }
                break;
            case '<':
                if (text.length() > 1 && text.equals("<<")) {
                    return "leftShift";
                }
                break;
            case '[':
                return "getAt";
        }
        return null;
    }

    private static ConstructorNode findDefaultConstructor(ClassNode node) {
        List<ConstructorNode> constructors = node.getDeclaredConstructors();
        for (ConstructorNode constructor : constructors) {
            if (constructor.getParameters() == null || constructor.getParameters().length == 0) {
                return constructor;
            }
        }
        return null;
    }

    /**
     * @return the method name associated with this unary operator
     */
    private static String findUnaryOperatorName(String text) {
        char op = text.charAt(0);
        switch (op) {
            case '+':
                if (text.length() > 1 && text.equals("++")) {
                    return "next";
                }
                return "positive";
            case '-':
                if (text.length() > 1 && text.equals("--")) {
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
                // lists, numbers or string
                return VariableScope.STRING_CLASS_NODE.equals(lhs) ||
                    lhs.isDerivedFrom(VariableScope.NUMBER_CLASS_NODE) ||
                    VariableScope.NUMBER_CLASS_NODE.equals(lhs) ||
                    VariableScope.LIST_CLASS_NODE.equals(lhs) ||
                    lhs.implementsInterface(VariableScope.LIST_CLASS_NODE);
            case '*':
            case '/':
            case '%':
                // numbers or string
                return VariableScope.STRING_CLASS_NODE.equals(lhs) ||
                    lhs.isDerivedFrom(VariableScope.NUMBER_CLASS_NODE) ||
                    VariableScope.NUMBER_CLASS_NODE.equals(lhs);
            default:
                return false;
        }
    }

    /**
     * Determines if the parameter type can be implicitly determined.  We look for
     * DGM method calls that take closures and see what kind of type they expect.
     *
     * @return array of {@link ClassNode}s specifying the inferred types of the closure's parameters
     */
    private static ClassNode[] inferClosureParamTypes(VariableScope scope, ClosureExpression closure) {
        int paramCount = closure.getParameters() == null ? 0 : closure.getParameters().length;
        if (paramCount == 0) {
            // implicit parameter
            paramCount += 1;
        }

        ClassNode[] inferredTypes = new ClassNode[paramCount];

        // TODO: Could this use the Closure annotations to determine the type?

        CallAndType call = scope.getEnclosingMethodCallExpression();
        if (call != null) {
            ClassNode delegateType = call.declaringType;
            String methodName = call.call.getMethodAsString();

            ClassNode inferredType;
            if (dgmClosureMethods.contains(methodName)) {
                inferredType = VariableScope.extractElementType(delegateType);
            } else if (dgmClosureIdentityMethods.contains(methodName)) {
                inferredType = VariableScope.clone(delegateType);
            } else {
                // inferredType might be null
                inferredType = dgmClosureMethodsMap.get(methodName);
            }

            if (inferredType != null) {
                Arrays.fill(inferredTypes, inferredType);
                // special cases: eachWithIndex has last element an integer
                if (methodName.equals("eachWithIndex") && inferredTypes.length > 1) {
                    inferredTypes[inferredTypes.length - 1] = VariableScope.INTEGER_CLASS_NODE;
                }
                // if declaring type is a map and
                if (delegateType.getName().equals(VariableScope.MAP_CLASS_NODE.getName())) {
                    if ((dgmClosureMaybeMap.contains(methodName) && paramCount == 2) ||
                            (methodName.equals("eachWithIndex") && paramCount == 3)) {
                        GenericsType[] typeParams = inferredType.getGenericsTypes();
                        if (typeParams != null && typeParams.length == 2) {
                            inferredTypes[0] = typeParams[0].getType();
                            inferredTypes[1] = typeParams[1].getType();
                        }
                    }
                }
                return inferredTypes;
            }
        }

        Arrays.fill(inferredTypes, VariableScope.OBJECT_CLASS_NODE);
        return inferredTypes;
    }

    private static boolean isEnumInit(StaticMethodCallExpression node) {
        int typeModifiers = node.getOwnerType().getModifiers();
        return ((typeModifiers & Opcodes.ACC_ENUM) > 0 && node.getMethod().equals("$INIT"));
    }

    private static boolean isLazy(FieldNode fieldNode) {
        for (AnnotationNode annotation : fieldNode.getAnnotations()) {
            if (annotation.getClassNode().getName().equals("groovy.lang.Lazy")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isMetaAnnotation(ClassNode node) {
        if (node.isAnnotated() && node.hasMethod("value", NO_PARAMETERS)) {
            for (AnnotationNode annotation : node.getAnnotations()) {
                if (annotation.getClassNode().getName().equals("groovy.transform.AnnotationCollector")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isNotEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }

    private static boolean shouldFilterEnumMember(IJavaElement child) {
        int type = child.getElementType();
        String name = child.getElementName();
        if (name.indexOf('$') >= 0) {
            return true;
        } else if (type == IJavaElement.METHOD) {
            if ((name.equals("next") || name.equals("previous")) && ((IMethod) child).getNumberOfParameters() == 0) {
                return true;
            }
        } else if (type == IJavaElement.METHOD) {
            if (name.equals("MIN_VALUE") || name.equals("MAX_VALUE")) {
                return true;
            }
        }
        return false;
    }
}
