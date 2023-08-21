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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import groovy.lang.Closure;
import groovy.lang.Tuple;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImmutableClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.runtime.DateGroovyMethods;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.DefaultGroovyStaticMethods;
import org.codehaus.groovy.runtime.EncodingGroovyMethods;
import org.codehaus.groovy.runtime.ProcessGroovyMethods;
import org.codehaus.groovy.runtime.SwingGroovyMethods;
import org.codehaus.groovy.runtime.XmlGroovyMethods;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTMethodNode;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;

/**
 * Maps variable names to types in a hierarchy.
 */
public class VariableScope implements Iterable<VariableScope.VariableInfo> {

    public static final VariableScope[] EMPTY_ARRAY = new VariableScope[0];

    public static final ClassNode NULL_TYPE = new ImmutableClassNode(Object.class);
    public static final ClassNode VOID_CLASS_NODE = ClassHelper.make(void.class);
    public static final ClassNode VOID_WRAPPER_CLASS_NODE = ClassHelper.void_WRAPPER_TYPE;

    public static final ClassNode OBJECT_CLASS_NODE = ClassHelper.OBJECT_TYPE;
    public static final ClassNode GROOVY_OBJECT_CLASS_NODE = ClassHelper.GROOVY_OBJECT_TYPE;
	public static final ClassNode GROOVY_OBJECT_SUPPORT = ClassHelper.GROOVY_OBJECT_SUPPORT_TYPE;
    public static final ClassNode GROOVY_SUPPORT_CLASS_NODE = ClassHelper.GROOVY_OBJECT_SUPPORT_TYPE;
    public static final ClassNode CLOSURE_CLASS_NODE = ClassHelper.CLOSURE_TYPE;
    public static final ClassNode ENUMERATION_CLASS = ClassHelper.make(Enumeration.class);
    public static final ClassNode ITERATOR_CLASS = ClassHelper.Iterator_TYPE;
    public static final ClassNode LIST_CLASS_NODE = ClassHelper.LIST_TYPE;
    public static final ClassNode MAP_CLASS_NODE = ClassHelper.MAP_TYPE;
    public static final ClassNode RANGE_CLASS_NODE = ClassHelper.RANGE_TYPE;
    public static final ClassNode TUPLE_CLASS_NODE = ClassHelper.make(Tuple.class);
    public static final ClassNode STRING_CLASS_NODE = ClassHelper.STRING_TYPE;
    public static final ClassNode GSTRING_CLASS_NODE = ClassHelper.GSTRING_TYPE;
    public static final ClassNode NUMBER_CLASS_NODE = ClassHelper.Number_TYPE;
    public static final ClassNode BIG_DECIMAL_CLASS = ClassHelper.BigDecimal_TYPE;
    public static final ClassNode BIG_INTEGER_CLASS = ClassHelper.BigInteger_TYPE;
    public static final ClassNode PATTERN_CLASS_NODE = ClassHelper.PATTERN_TYPE;
    public static final ClassNode MATCHER_CLASS_NODE = ClassHelper.make(Matcher.class);

    public static final ClassNode FILE_CLASS_NODE = ClassHelper.make(File.class);
    public static final ClassNode INPUT_STREAM_CLASS = ClassHelper.make(InputStream.class);
    public static final ClassNode OUTPUT_STREAM_CLASS = ClassHelper.make(OutputStream.class);
    public static final ClassNode DATA_INPUT_STREAM_CLASS = ClassHelper.make(DataInputStream.class);
    public static final ClassNode DATA_OUTPUT_STREAM_CLASS = ClassHelper.make(DataOutputStream.class);
    public static final ClassNode OBJECT_OUTPUT_STREAM = ClassHelper.make(ObjectOutputStream.class);
    public static final ClassNode OBJECT_INPUT_STREAM = ClassHelper.make(ObjectInputStream.class);
    public static final ClassNode BUFFERED_READER_CLASS_NODE = ClassHelper.make(BufferedReader.class);
    public static final ClassNode BUFFERED_WRITER_CLASS_NODE = ClassHelper.make(BufferedWriter.class);
    public static final ClassNode PRINT_WRITER_CLASS_NODE = ClassHelper.make(PrintWriter.class);

    // standard category classes
    public static final ClassNode DGM_CLASS_NODE = ClassHelper.make(DefaultGroovyMethods.class);
    public static final ClassNode EGM_CLASS_NODE = ClassHelper.make(EncodingGroovyMethods.class);
    public static final ClassNode PGM_CLASS_NODE = ClassHelper.make(ProcessGroovyMethods.class);
    public static final ClassNode SGM_CLASS_NODE = ClassHelper.make(SwingGroovyMethods.class);
    public static final ClassNode XGM_CLASS_NODE = ClassHelper.make(XmlGroovyMethods.class);
    public static final ClassNode DGSM_CLASS_NODE = ClassHelper.make(DefaultGroovyStaticMethods.class);
    public static final ClassNode DATE_GM_CLASS_NODE = ClassHelper.make(DateGroovyMethods.class);

    // only exists on Groovy 2.0
    public static ClassNode RESOURCE_GROOVY_METHODS;
    public static ClassNode STRING_GROOVY_METHODS;
    public static ClassNode IO_GROOVY_METHODS;
    static {
        try {
            RESOURCE_GROOVY_METHODS = ClassHelper.make(Class.forName("org.codehaus.groovy.runtime.ResourceGroovyMethods"));
            STRING_GROOVY_METHODS = ClassHelper.make(Class.forName("org.codehaus.groovy.runtime.StringGroovyMethods"));
            IO_GROOVY_METHODS = ClassHelper.make(Class.forName("org.codehaus.groovy.runtime.IOGroovyMethods"));
        } catch (ClassNotFoundException e) {
            RESOURCE_GROOVY_METHODS = null;
            STRING_GROOVY_METHODS = null;
            IO_GROOVY_METHODS = null;
        }
    }

    // only exists on 2.1 and later
    public static ClassNode DELEGATES_TO;
    static {
        try {
            DELEGATES_TO = ClassHelper.make(Class.forName("groovy.lang.DelegatesTo"));
        } catch (ClassNotFoundException e) {
            DELEGATES_TO = null;
        }
    }

    public static Set<ClassNode> ALL_DEFAULT_CATEGORIES;
    static {
        // add all of the known DGM classes. Order counts since we look up earlier in the list before later and need to
        // ensure we don't accidentally place deprecated elements early in the list
        List<ClassNode> dgm_classes = new ArrayList<ClassNode>(10);
        if (STRING_GROOVY_METHODS != null) {
            dgm_classes.add(STRING_GROOVY_METHODS);
        }
        if (RESOURCE_GROOVY_METHODS != null) {
            dgm_classes.add(RESOURCE_GROOVY_METHODS);
        }
        if (IO_GROOVY_METHODS != null) {
            dgm_classes.add(IO_GROOVY_METHODS);
        }
        dgm_classes.add(EGM_CLASS_NODE);
        dgm_classes.add(PGM_CLASS_NODE);
        dgm_classes.add(SGM_CLASS_NODE);
        dgm_classes.add(XGM_CLASS_NODE);
        dgm_classes.add(DATE_GM_CLASS_NODE);
        dgm_classes.add(DGSM_CLASS_NODE);
        dgm_classes.add(DGM_CLASS_NODE);
        ALL_DEFAULT_CATEGORIES = Collections.unmodifiableSet(new LinkedHashSet<ClassNode>(dgm_classes));
    }

    // don't cache because we have to add properties
    public static final ClassNode CLASS_CLASS_NODE = ClassHelper.makeWithoutCaching(Class.class);
    static {
        initializeProperties(CLASS_CLASS_NODE);
    }

    public static final ClassNode CLASS_ARRAY_CLASS_NODE = CLASS_CLASS_NODE.makeArray();

    // primitive wrapper classes
    public static final ClassNode BOOLEAN_CLASS_NODE = ClassHelper.Boolean_TYPE;
    public static final ClassNode CHARACTER_CLASS_NODE = ClassHelper.Character_TYPE;
    public static final ClassNode BYTE_CLASS_NODE = ClassHelper.Byte_TYPE;
    public static final ClassNode INTEGER_CLASS_NODE = ClassHelper.Integer_TYPE;
    public static final ClassNode SHORT_CLASS_NODE = ClassHelper.Short_TYPE;
    public static final ClassNode LONG_CLASS_NODE = ClassHelper.Long_TYPE;
    public static final ClassNode FLOAT_CLASS_NODE = ClassHelper.Float_TYPE;
    public static final ClassNode DOUBLE_CLASS_NODE = ClassHelper.Double_TYPE;

    //--------------------------------------------------------------------------

    public static class VariableInfo {
        public ASTNode scopeNode;
        public final String name;
        public final ClassNode type;
        public final ClassNode declaringType;

        public VariableInfo(String name, ClassNode type, ClassNode declaringType) {
            this.name = name;
            this.type = type;
            this.declaringType = declaringType;
        }

        private VariableInfo(VariableInfo info, ASTNode node) {
            this(info.name, info.type, info.declaringType);
            this.scopeNode = node;
        }

        public String getTypeSignature() {
            return GroovyUtils.getTypeSignature(type, /*fully-qualified:*/ true, false);
        }
    }

    public static class CallAndType {

        public final ASTNode declaration;
        public final ClassNode declaringType;
        public final MethodCallExpression call;
        private Map<ClosureExpression, Object[]> delegatesTo;

        /**
         * @param declaringType type that declares {@code declaration} in most cases;
         *        if {@code call} is a category method it's likely the calling object
         *        type; if {@code call} is an implicit-this call in a closure, then...
         */
        public CallAndType(MethodCallExpression call, ASTNode declaration, ClassNode declaringType, ModuleNode enclosingModule) {
            this.call = call;
            this.declaration = declaration;
            this.declaringType = declaringType;

            // handle the Groovy 2.1+ @DelegatesTo annotation; see also org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor#checkClosureWithDelegatesTo
            if (DELEGATES_TO != null && declaration instanceof MethodNode) {
                MethodNode methodNode = (MethodNode) declaration;
                Parameter[] parameters = methodNode.getParameters();
                if (parameters != null && parameters.length > 0) {
                    List<Expression> arguments = null;
                    if (call.getArguments() instanceof TupleExpression) {
                        arguments = ((TupleExpression) call.getArguments()).getExpressions();
                    }
                    if (arguments != null && !arguments.isEmpty()) {
                        if (!methodNode.getDeclaringClass().equals(getPerceivedDeclaringType())) {
                            List<Expression> categoryMethodArguments = new ArrayList<Expression>(arguments.size() + 1);
                            categoryMethodArguments.add(new ClassExpression(declaringType));
                            categoryMethodArguments.addAll(arguments);
                            arguments = categoryMethodArguments;
                        }
                        for (int i = 0, n = parameters.length; i < n; i += 1) {
                            List<AnnotationNode> annotations = parameters[i].getAnnotations();
                            if (annotations != null && !annotations.isEmpty()) {
                                for (AnnotationNode annotation : annotations) {
                                    if (annotation.getClassNode().getName().equals(DELEGATES_TO.getName()) &&
                                            i < arguments.size() && arguments.get(i) instanceof ClosureExpression) {
                                        ClosureExpression closure = (ClosureExpression) arguments.get(i);

                                        Expression delegatesToType = annotation.getMember("type");
                                        Expression delegatesToValue = annotation.getMember("value");
                                        Expression delegatesToTarget = annotation.getMember("target");
                                        Expression delegatesToStrategy = annotation.getMember("strategy");
                                        Expression delegatesToGenericTypeIndex = annotation.getMember("genericTypeIndex");

                                        Integer strategy = null, generics = null;
                                        /*if (delegatesToStrategy != null) {
                                            strategy = (Integer) org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.evaluateExpression(org.codehaus.groovy.ast.tools.GeneralUtils.castX(INTEGER_CLASS_NODE, delegatesToStrategy), enclosingModule.getUnit().getConfig());
                                        }
                                        if (delegatesToGenericTypeIndex != null) {
                                            strategy = (Integer) org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.evaluateExpression(org.codehaus.groovy.ast.tools.GeneralUtils.castX(INTEGER_CLASS_NODE, delegatesToGenericTypeIndex), enclosingModule.getUnit().getConfig());
                                        }*/
                                        if (delegatesToStrategy instanceof ConstantExpression) {
                                            strategy = Integer.valueOf(delegatesToStrategy.getText());
                                        }
                                        if (delegatesToGenericTypeIndex instanceof ConstantExpression) {
                                            generics = Integer.valueOf(delegatesToGenericTypeIndex.getText());
                                        }

                                        // handle three modes: @DelegatesTo(Type.class), @DelegatesTo(type="pack.Type"), @DelegatesTo(target="name", genericTypeIndex=i)
                                        if (delegatesToValue instanceof ClassExpression && !delegatesToValue.getType().getName().equals("groovy.lang.DelegatesTo$Target")) {
                                            addDelegatesToClosure(closure, delegatesToValue.getType(), strategy);

                                        } else if (delegatesToType instanceof ConstantExpression && !"".equals(delegatesToType.getText())) {
                                            //ClassNode[] resolved = org.codehaus.groovy.ast.tools.GenericsUtils.parseClassNodesFromString(delegatesToType.getText(), enclosingModule.getContext(), an org.codehaus.groovy.control.CompilationUnit, methodNode, delegatesToType);
                                            //addDelegatesToClosure(closure, resolved[0], strategy);

                                        } else if (delegatesToValue == null || (delegatesToValue instanceof ClassExpression && delegatesToValue.getType().getName().equals("groovy.lang.DelegatesTo$Target"))) {
                                            int j = indexOfDelegatesToTarget(parameters, delegatesToTarget.getText());
                                            if (j >= 0 && j < arguments.size()) {
                                                Expression target = arguments.get(j);
                                                ClassNode targetType = target.getType(); // TODO: lookup expression type (unless j is 0 and it's a category method)
                                                if (generics != null && generics >= 0 && targetType.isUsingGenerics()) {
                                                    targetType.getGenericsTypes()[generics].getType();
                                                }
                                                addDelegatesToClosure(closure, targetType, strategy);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (delegatesTo == null) {
                            if (arguments.get(0) instanceof ClosureExpression &&
                                    methodNode.getName().matches("build|do(Later|Outside)|edt(Builder)?") &&
                                    methodNode.getDeclaringClass().getName().equals("groovy.swing.SwingBuilder")) {
                                addDelegatesToClosure((ClosureExpression) arguments.get(0), methodNode.getDeclaringClass(), Closure.OWNER_FIRST);
                            } else if (arguments.size() > 1 && arguments.get(1) instanceof ClosureExpression &&
                                    methodNode.getName().matches("identity|with") && DGM_CLASS_NODE.equals(methodNode.getDeclaringClass())) {
                                // prior to Groovy 2.3, "with" lacked @DelegatesTo metadata; same goes for "identity" (see GROOVY-8376)
                                addDelegatesToClosure((ClosureExpression) arguments.get(1), declaringType, Closure.DELEGATE_FIRST);
                            }
                        }
                    }
                }
            }
            if (delegatesTo == null) {
                delegatesTo = Collections.emptyMap();
            }
        }

        /**
         * Returns the perceived declaring type of the call, which may differ
         * from the actual declaring class in case of class/category methods.
         */
        public ClassNode getPerceivedDeclaringType() {
            if (declaringType.equals(CLASS_CLASS_NODE)) {
                if (declaringType.isUsingGenerics()) {
                    GenericsType genericsType = declaringType.getGenericsTypes()[0];
                    return genericsType.getType();
                }
                return OBJECT_CLASS_NODE;
            }
            return declaringType;
        }

        public ClassNode getDelegateType(ClosureExpression closure) {
            Object[] tuple = delegatesTo.get(closure);
            return (tuple != null ? (ClassNode) tuple[0] : null);
        }

        public int getResolveStrategy(ClosureExpression closure) {
            Object[] tuple = delegatesTo.get(closure);
            return (tuple != null && tuple[1] != null ? (Integer) tuple[1] : Closure.OWNER_FIRST);
        }

        private void addDelegatesToClosure(ClosureExpression closure, ClassNode delegateType, Integer resolveStrategy) {
            if (delegatesTo == null) {
                delegatesTo = new HashMap<ClosureExpression, Object[]>();
            }
            delegatesTo.put(closure, new Object[] {delegateType, resolveStrategy});
        }

        /**
         * Finds param with DelegatesTo.Target annotation that has matching value string.
         */
        private int indexOfDelegatesToTarget(Parameter[] parameters, String target) {
            for (int i = 0, n = parameters.length; i < n; i += 1) {
                List<AnnotationNode> annotations = parameters[i].getAnnotations();
                if (annotations != null && !annotations.isEmpty()) {
                    for (AnnotationNode annotation : annotations) {
                        if (annotation.getClassNode().getName().equals("groovy.lang.DelegatesTo$Target")) {
                            if (annotation.getMember("value") instanceof ConstantExpression) {
                                String value = annotation.getMember("value").getText();
                                if (value.equals(target)) {
                                    return i;
                                }
                            }
                        }
                    }
                }
            }
            return -1;
        }
    }

    /**
     * Contains state that is shared amongst {@link VariableScope}s
     */
    private static class SharedState {
        /**
         * this field stores values that need to get passed between parts of the file to another
         */
        final Map<String, Object> wormhole = new HashMap<String, Object>();
        /**
         * the enclosing method call is the one where there are the current node is part of an argument list
         */
        final List<CallAndType> enclosingCallStack = new ArrayList<CallAndType>();
        /**
         * Node currently being evaluated, or null if none
         */
        final LinkedList<ASTNode> nodeStack = new LinkedList<ASTNode>();
        /**
         * true iff current scope is implicit run method of script
         */
        boolean isRunMethod;
    }

    /**
     * Null for the top level scope
     */
    private VariableScope parent;

    /**
     * Shared with parent scopes
     */
    private SharedState shared;

    /**
     * AST node for this scope, typically, a block, closure, or body declaration
     */
    /*package*/ ASTNode scopeNode;

    /**
     * number of parameters of current method call or -1 if not a method call
     */
    private boolean isPrimaryNode;

    private final boolean isStaticScope;

    /**
     * Category that will be declared in the next scope
     */
    private ClassNode categoryBeingDeclared;

    private int enclosingCallStackDepth;
    private List<ClassNode> methodCallArgumentTypes;
    private GenericsType[]  methodCallGenericsTypes;
    private final Map<String, VariableInfo> nameVariableMap = new HashMap<String, VariableInfo>();

    //--------------------------------------------------------------------------

    public VariableScope(VariableScope parent, ASTNode enclosingNode, boolean isStatic) {
        this.parent = parent;
        this.scopeNode = enclosingNode;
        this.shared = parent != null ? parent.shared : new SharedState();
        this.enclosingCallStackDepth = shared.enclosingCallStack.size();
        this.isStaticScope = (isStatic || (parent != null && parent.isStaticScope)) &&
            (getEnclosingClosureScope() == null); // if in a closure, items may be found on delegate or owner

        // determine if scope belongs to script body
        if (enclosingNode instanceof ClassNode ||
                enclosingNode instanceof FieldNode) {
            this.shared.isRunMethod = false;
        } else if (enclosingNode instanceof MethodNode) {
            this.shared.isRunMethod = ((MethodNode) enclosingNode).isScriptBody();
        }
        // TODO: When scope is popped, should the flag be restored to its previous value?

        // initialize type of "this" (and by deduction "super"; see lookupName("super"))
        if (enclosingNode instanceof ClassNode) {
            ClassNode type = (ClassNode) enclosingNode;
            addVariable("this", newClassClassNode(type), type);
        } else if (!isStatic && (parent != null && parent.scopeNode instanceof ClassNode)) {
            ClassNode type = (ClassNode) parent.scopeNode;
            addVariable("this", type, type); // switch from Class<T> to T
        }
    }

    /**
     * Back door for storing and retrieving objects between lookup locations.
     */
    public Map<String, Object> getWormhole() {
        return shared.wormhole;
    }

    public ASTNode getEnclosingNode() {
        int n = shared.nodeStack.size();
        if (n > 1) {
            return shared.nodeStack.get(n - 2);
        }
        return null;
    }

    public void setCurrentNode(ASTNode currentNode) {
        shared.nodeStack.add(currentNode);
    }

    public void forgetCurrentNode() {
        if (!shared.nodeStack.isEmpty()) {
            shared.nodeStack.removeLast();
        }
    }

    public ASTNode getCurrentNode() {
        if (!shared.nodeStack.isEmpty()) {
            return shared.nodeStack.getLast();
        } else {
            return null;
        }
    }

    public void setPrimaryNode(boolean isPrimaryNode) {
        this.isPrimaryNode = isPrimaryNode;
    }

    /**
     * @return {@code true} iff the current node is not the RHS of a dotted expression
     */
    public boolean isPrimaryNode() {
        return isPrimaryNode;
    }

    /**
     * The name of all categories in scope.
     */
    public Set<ClassNode> getCategoryNames() {
        if (parent != null) {
            Set<ClassNode> categories = parent.getCategoryNames();
            // don't look at this scope's category, but the parent scope's
            // category. This is because although current scope knows that it
            // is a category scope, the category type is only available from parent
            // scope
            if (parent.isCategoryBeingDeclared()) {
                categories.add(parent.categoryBeingDeclared);
            }
            return categories;
        } else {
            return new LinkedHashSet<ClassNode>(ALL_DEFAULT_CATEGORIES);
        }
    }

    private boolean isCategoryBeingDeclared() {
        return categoryBeingDeclared != null;
    }

    public void setCategoryBeingDeclared(ClassNode categoryBeingDeclared) {
        this.categoryBeingDeclared = categoryBeingDeclared;
    }

    /**
     * Finds the variable in the current scope or parent scopes.
     *
     * @return the variable info or null if not found
     */
    public VariableInfo lookupName(String name) {
        if ("super".equals(name)) {
            ClassNode type = getThis();
            if (type != null) {
                ClassNode superType = type;

                // within a closure in a static scope, "super" and "this" both refer to Class<T>
                VariableScope scope = getEnclosingClosureScope();
                if (scope == null || !scope.isOwnerStatic()) {
                    // outside of a closure "super" may be super of T (non-static scope),
                    // Class<super of T> (static scope) or Object (Is this a Groovy bug?)
                    if (!isStatic()) {
                        superType = type.getSuperClass();
                    } else { // type is Class<T>, so produce Class<super of T>
                        assert type.equals(CLASS_CLASS_NODE) && type.isUsingGenerics();
                        superType = type.getGenericsTypes()[0].getType().getSuperClass(); //super of T
                        if (superType != null && !superType.equals(OBJECT_CLASS_NODE)) {
                            superType = newClassClassNode(superType);
                        }
                    }
                }

                return new VariableInfo(name, superType, superType);
            }
        }

        VariableInfo var = lookupNameInCurrentScope(name);
        if (var == null && parent != null) {
            var = parent.lookupName(name);
        }
        return var;
    }

    /**
     * Finds the name in the current scope. Does not recur up to parent scopes.
     */
    public VariableInfo lookupNameInCurrentScope(String name) {
        VariableInfo info = nameVariableMap.get(name);
        if (info != null) {
            info = new VariableInfo(info, scopeNode);
        }
        return info;
    }

    public ClassNode getThis() {
        VariableInfo info = lookupName("this");
        return info != null ? info.type : null;
    }

    public ClassNode getOwner() {
        VariableInfo info = lookupName("getOwner");
        return info != null ? info.type : null;
    }

    public ClassNode getDelegate() {
        VariableInfo info = lookupName("getDelegate");
        return info != null ? info.type : null;
    }

    /**
     * @return the current delegate type if exists, or this type if exists.
     *       Returns null if in top level scope (i.e. in import statement).
     */
    public ClassNode getDelegateOrThis() {
        ClassNode type = getDelegate();
        if (type == null) {
            type = getThis();
        }
        return type;
    }

    /**
     * @return {@code true} iff this is a static stack frame
     */
    public boolean isStatic() {
        return isStaticScope;
    }

    public boolean isOwnerStatic() {
        FieldNode field; MethodNode method;
        if (isStatic() ||
                ((field = getEnclosingFieldDeclaration()) != null && field.isStatic()) ||
                ((method = getEnclosingMethodDeclaration()) != null && method.isStatic())) {
            return true;
        }
        return false;
    }

    public boolean isFieldAccessDirect() {
        return (!isOwnerStatic() && getEnclosingClosureScope() == null);
    }

    public void addVariable(String name, ClassNode type, ClassNode declaringType) {
        nameVariableMap.put(name, new VariableInfo(name, type, declaringType != null ? declaringType : OBJECT_CLASS_NODE));
    }

    public void addVariable(Variable var) {
        addVariable(var.getName(), var.getType(), var.getOriginType());
    }

    public ModuleNode getEnclosingModuleNode() {
        if (scopeNode instanceof ModuleNode) {
            return (ModuleNode) scopeNode;
        } else if (parent != null) {
            return parent.getEnclosingModuleNode();
        } else {
            return null;
        }
    }

    public ClassNode getEnclosingTypeDeclaration() {
        if (scopeNode instanceof ClassNode) {
            return (ClassNode) scopeNode;
        } else if (parent != null) {
            return parent.getEnclosingTypeDeclaration();
        } else {
            return null;
        }
    }

    public FieldNode getEnclosingFieldDeclaration() {
        if (scopeNode instanceof FieldNode) {
            return (FieldNode) scopeNode;
        } else if (parent != null) {
            return parent.getEnclosingFieldDeclaration();
        } else {
            return null;
        }
    }

    public MethodNode getEnclosingMethodDeclaration() {
        if (scopeNode instanceof MethodNode) {
            return (MethodNode) scopeNode;
        } else if (parent != null) {
            return parent.getEnclosingMethodDeclaration();
        } else {
            return null;
        }
    }

    public ClosureExpression getEnclosingClosure() {
        VariableScope scope = getEnclosingClosureScope();
        return (scope != null ? (ClosureExpression) scope.scopeNode : null);
    }

    public int getEnclosingClosureResolveStrategy() {
        VariableScope scope = getEnclosingClosureScope();
        int resolveStrategy = Closure.OWNER_FIRST;
        if (scope != null) {
            CallAndType cat = scope.getEnclosingMethodCallExpression();
            if (cat != null) {
                resolveStrategy = cat.getResolveStrategy((ClosureExpression) scope.scopeNode);
            }
        }
        return resolveStrategy;
    }

    /*package*/ VariableScope getEnclosingClosureScope() {
        VariableScope scope = this;
        do {
            if (scope.scopeNode instanceof ClosureExpression) {
                return scope;
            }
        } while ((scope = scope.parent) != null);

        return null;
    }

    private static PropertyNode createPropertyNodeForMethodNode(MethodNode methodNode) {
        ClassNode propertyType = methodNode.getReturnType();
        String methodName = methodNode.getName();
        StringBuffer propertyName = new StringBuffer();
        propertyName.append(Character.toLowerCase(methodName.charAt(3)));
        if (methodName.length() > 4) {
            propertyName.append(methodName.substring(4));
        }
        int mods = methodNode.getModifiers();
        ClassNode declaringClass = methodNode.getDeclaringClass();
        PropertyNode property = new PropertyNode(propertyName.toString(), mods, propertyType, declaringClass, null, null, null);
        property.setDeclaringClass(declaringClass);
        property.getField().setDeclaringClass(declaringClass);
        return property;
    }

    private static void initializeProperties(ClassNode node) {
        // getX methods
        for (MethodNode methodNode : node.getMethods()) {
            if (AccessorSupport.isGetter(methodNode)) {
                node.addProperty(createPropertyNodeForMethodNode(methodNode));
            }
        }
    }

    /**
     * Updates the type info of this variable if it already exists in scope, or just adds it if it doesn't
     */
    public void updateOrAddVariable(String name, ClassNode type, ClassNode declaringType) {
        if (!internalUpdateVariable(name, type, declaringType)) {
            addVariable(name, type, declaringType);
        }
    }

    /**
     * Updates the identifier if it exists in this scope or a parent scope. Otherwise does nothing
     *
     * @param name identifier to update
     * @param type type of identifier
     * @param declaringType declaring type of identifier
     * @return true iff the variable exists in scope and was updated
     */
    public boolean updateVariable(String name, ClassNode type, ClassNode declaringType) {
        return internalUpdateVariable(name, type, declaringType);
    }

    /**
     * Return true if the type has been udpated, false otherwise
     */
    private boolean internalUpdateVariable(String name, ClassNode type, ClassNode declaringType) {
        VariableInfo info = lookupNameInCurrentScope(name);
        if (info != null) {
            nameVariableMap.put(name, new VariableInfo(name, type, declaringType == null ? info.declaringType : declaringType));
            return true;
        } else if (parent != null) {
            return parent.internalUpdateVariable(name, type, declaringType);
        } else {
            return false;
        }
    }

    public static ClassNode resolveTypeParameterization(GenericsMapper mapper, ClassNode type) {
        if (mapper.hasGenerics()) {
            GenericsType[] parameterizedTypes = GroovyUtils.getGenericsTypes(type);
            if (parameterizedTypes.length > 0) {
                for (int i = 0, n = parameterizedTypes.length; i < n; i += 1) {
                    GenericsType parameterizedType = parameterizedTypes[i];
                    ClassNode maybe = resolveTypeParameterization(mapper, parameterizedType, type);
                    if (maybe != type) {
                        assert n == 1;
                        type = maybe;
                        break;
                    }
                }
            }
        }
        return type;
    }

    public static ClassNode resolveTypeParameterization(GenericsMapper mapper, GenericsType generic, ClassNode unresolved) {
        if (!generic.isWildcard()) {
            resolveTypeParameterization(mapper, generic.getType()); // TODO: capture return value?

            String toParameterizeName = generic.getName();
            ClassNode resolved = mapper.findParameter(toParameterizeName, generic.getType());

            // there are three known possibilities for resolved:
            // 1. it is the resolution of a type parameter itself (eg- E --> String)
            // 2. it is the resolved type parameter of a generic type (eg- Iterator<E> --> Iterator<String>)
            // 3. it is a substitution of one type parameter for another (eg- List<T> --> List<E>, where T comes from the declaring type)

            if (typeParameterExistsInRedirected(unresolved, toParameterizeName)) {
                Assert.isLegal(unresolved.redirect() != unresolved, "Error: trying to resolve type parameters of a type declaration: " + unresolved);
                // Iterator<E> --> Iterator<String>
                generic.setLowerBound(null);
                generic.setUpperBounds(null);
                generic.setPlaceholder(false); // before setType to prevent mutation of resolved
                generic.setWildcard(false);
                generic.setResolved(true);
                generic.setType(resolved);
                generic.setName(generic.getType().getName());
            } else {
                // E --> String
                // E[] --> String[]
                while (unresolved.isArray()) {
                    unresolved = unresolved.getComponentType();
                    resolved = resolved.makeArray();
                }
                return resolved;
            }

        } else if (generic.getLowerBound() != null) {
            // List<? super E> --> List<? super String>
            ClassNode resolved = resolveTypeParameterization(mapper, generic.getLowerBound());
            generic.setLowerBound(resolved);
            generic.setResolved(true);

        } else if (generic.getUpperBounds() != null) {
            // List<? extends E> --> List<? extends String>
            ClassNode[] parameterizedTypeUpperBounds = generic.getUpperBounds();
            for (int j = 0, k = parameterizedTypeUpperBounds.length; j < k; j += 1) {
                ClassNode resolved = resolveTypeParameterization(mapper, parameterizedTypeUpperBounds[j]);
                parameterizedTypeUpperBounds[j] = resolved;
            }
            generic.setResolved(true);
        }
        return unresolved;
    }

    public static MethodNode resolveTypeParameterization(GenericsMapper mapper, MethodNode method) {
        if (mapper.hasGenerics() && (GroovyUtils.getGenericsTypes(method).length > 0 ||
                GroovyUtils.getGenericsTypes(method.getDeclaringClass()).length > 0)) {

            ClassNode returnType = resolveTypeParameterization(mapper, clone(method.getReturnType()));

            Parameter[] parameters = method.getParameters();
            if (parameters != null && parameters.length > 0) {
                int n = parameters.length;
                parameters = new Parameter[n];
                for (int i = 0; i < n; i += 1) {
                    Parameter original = method.getParameters()[i];
                    ClassNode parameterType = resolveTypeParameterization(mapper, clone(original.getType()));
                    parameters[i] = new Parameter(parameterType, original.getName(), original.getInitialExpression());
                    parameters[i].addAnnotations(original.getAnnotations());
                    parameters[i].setClosureSharedVariable(original.isClosureSharedVariable());
                    parameters[i].setDeclaringClass(original.getDeclaringClass()); // TODO: resolve?
                    parameters[i].setHasNoRealSourcePosition(original.hasNoRealSourcePosition());
                    parameters[i].setInStaticContext(original.isInStaticContext());
                    parameters[i].setModifiers(original.getModifiers());
                    parameters[i].copyNodeMetaData(original);
                    parameters[i].setOriginType(original.getOriginType());
                    parameters[i].setSourcePosition(original);
                    parameters[i].setSynthetic(original.isSynthetic());
                }
            }

            MethodNode resolved;
            if (method instanceof JDTMethodNode) {
                resolved = new JDTMethodNode(
                    ((JDTMethodNode) method).getMethodBinding(), ((JDTMethodNode) method).getResolver(),
                    method.getName(), method.getModifiers(), returnType, parameters, method.getExceptions(), method.getCode());
            } else {
                resolved = new MethodNode(
                    method.getName(), method.getModifiers(), returnType, parameters, method.getExceptions(), method.getCode());
                resolved.addAnnotations(method.getAnnotations());
            }
            resolved.setAnnotationDefault(method.hasAnnotationDefault());
            resolved.setDeclaringClass(resolveTypeParameterization(mapper, clone(method.getDeclaringClass())));
            resolved.setGenericsTypes(method.getGenericsTypes()); // TODO: resolve?
            resolved.setHasNoRealSourcePosition(method.hasNoRealSourcePosition());
            resolved.copyNodeMetaData(method);
            resolved.setOriginal(method.getOriginal());
            resolved.setSourcePosition(method);
            resolved.setSynthetic(method.isSynthetic());
            resolved.setSyntheticPublic(method.isSyntheticPublic());
            resolved.setVariableScope(method.getVariableScope());

            method = resolved;
        }
        return method;
    }

    private static boolean typeParameterExistsInRedirected(ClassNode type, String toParameterizeName) {
        ClassNode redirect = type.redirect();
        GenericsType[] genericsTypes = redirect.getGenericsTypes();
        if (genericsTypes != null) {
            // I don't *think* we need to check here. if any type parameter exists in the redirect, then we are parameterizing
            return true;
        }
        return false;
    }

    /**
     * Create a copy of this class, taking into account generics and redirects
     */
    public static ClassNode clone(ClassNode type) {
        return cloneInternal(type, 0);
    }

    public static ClassNode clonedMap() {
        ClassNode clone = clone(MAP_CLASS_NODE);
        cleanGenerics(clone.getGenericsTypes()[0]);
        cleanGenerics(clone.getGenericsTypes()[1]);
        return clone;
    }

    public static ClassNode clonedList() {
        ClassNode clone = clone(LIST_CLASS_NODE);
        cleanGenerics(clone.getGenericsTypes()[0]);
        return clone;
    }

    public static ClassNode clonedRange() {
        ClassNode clone = clone(RANGE_CLASS_NODE);
        cleanGenerics(clone.getGenericsTypes()[0]);
        return clone;
    }

    private static void cleanGenerics(GenericsType gt) {
        gt.getType().setGenericsTypes(null);
        gt.setName("java.lang.Object");
        gt.setPlaceholder(false);
        gt.setWildcard(false);
        gt.setResolved(true);
        gt.setUpperBounds(null);
        gt.setLowerBound(null);
    }

    /**
     * Internal variant of clone that ensures recursion never gets too deep.
     *
     * @param type class node to clone
     * @param depth stack overflow prevention
     */
    private static ClassNode cloneInternal(ClassNode type, int depth) {
        if (type == null || type.isPrimitive()) {
            return type;
        }
        ClassNode newType = type.getPlainNodeReference();

        newType.setSourcePosition(type);
        newType.setGenericsPlaceHolder(type.isGenericsPlaceHolder());
        ReflectionUtils.setPrivateField(ClassNode.class, "componentType", newType,
            cloneInternal(type.getComponentType(), depth + 1)
        );

        // GRECLIPSE-1024: set an arbitrary depth to return from
        // ensures that improperly set up generics do not lead to infinite recursion
        if (depth < 11) {
            GenericsType[] generics = type.getGenericsTypes();
            if (generics != null) {
                int n = generics.length;
                GenericsType[] clones = new GenericsType[n];
                for (int i = 0; i < n; i += 1) {
                    clones[i] = clone(generics[i], depth);
                }
                newType.setGenericsTypes(clones);
            }
        }

        return newType;
    }

    /**
     * Create a copy of this {@link GenericsType}
     *
     * @param origgt the original {@link GenericsType} to copy
     * @param depth prevent infinite recursion on bad generics
     */
    public static GenericsType clone(GenericsType origgt, int depth) {
        GenericsType newgt = new GenericsType();
        newgt.setType(cloneInternal(origgt.getType(), depth + 1));
        newgt.setLowerBound(cloneInternal(origgt.getLowerBound(), depth + 1));
        ClassNode[] oldUpperBounds = origgt.getUpperBounds();
        if (oldUpperBounds != null) {
            int n = oldUpperBounds.length;
            ClassNode[] newUpperBounds = new ClassNode[n];
            for (int i = 0; i < n; i += 1) {
                newUpperBounds[i] = cloneInternal(oldUpperBounds[i], depth + 1);
            }
            newgt.setUpperBounds(newUpperBounds);
        }
        newgt.setName(origgt.getName());
        newgt.setPlaceholder(origgt.isPlaceholder());
        newgt.setWildcard(origgt.isWildcard());
        newgt.setResolved(origgt.isResolved());
        newgt.setSourcePosition(origgt);
        return newgt;
    }

    public static ClassNode newClassClassNode(ClassNode type) {
        ClassNode classType = ClassHelper.makeWithoutCaching("java.lang.Class");
        classType.setGenericsTypes(new GenericsType[] {new GenericsType(type)});
        classType.setRedirect(CLASS_CLASS_NODE);
        return classType;
    }

    /**
     * @return the enclosing method call expression if one exists, or null otherwise.
     *     For example, when visiting the following closure, the enclosing method call is 'run'
     *     <pre>
     *     def runner = new Runner()
     *     runner.run {
     *       print "hello!"
     *     }
     *     </pre>
     */
    public List<CallAndType> getAllEnclosingMethodCallExpressions() {
        return shared.enclosingCallStack.subList(0, enclosingCallStackDepth);
    }

    public CallAndType getEnclosingMethodCallExpression() {
        List<CallAndType> enclosingCalls = getAllEnclosingMethodCallExpressions();
        return (!enclosingCalls.isEmpty() ? enclosingCalls.get(enclosingCalls.size() - 1) : null);
    }

    public void addEnclosingMethodCall(CallAndType enclosingMethodCall) {
        assert enclosingCallStackDepth == shared.enclosingCallStack.size();
        shared.enclosingCallStack.add(enclosingMethodCall);
        enclosingCallStackDepth += 1;
    }

    public void forgetEnclosingMethodCall() {
        assert enclosingCallStackDepth == shared.enclosingCallStack.size();
        shared.enclosingCallStack.remove(enclosingCallStackDepth - 1);
        enclosingCallStackDepth -= 1;
    }

    public boolean isTopLevel() {
        return parent == null;
    }

    /**
     * Does the following name exist in this scope (does not recur up to parent scopes).
     *
     * @return {@code true} iff in the {@link #nameVariableMap}
     */
    public boolean containsInThisScope(String name) {
        return nameVariableMap.containsKey(name);
    }

    void setMethodCallArgumentTypes(List<ClassNode> methodCallArgumentTypes) {
        this.methodCallArgumentTypes = methodCallArgumentTypes;
    }

    public List<ClassNode> getMethodCallArgumentTypes() {
        return methodCallArgumentTypes;
    }

    void setMethodCallGenericsTypes(GenericsType[] methodCallGenericsTypes) {
        this.methodCallGenericsTypes = methodCallGenericsTypes;
    }

    public GenericsType[] getMethodCallGenericsTypes() {
        return methodCallGenericsTypes;
    }

    /**
     * If visiting the identifier of a method call expression, this field will
     * be equal to the number of arguments to the method call.
     */
    int getMethodCallNumberOfArguments() {
        return isMethodCall() ? methodCallArgumentTypes.size() : 0;
    }

    public boolean isMethodCall() {
        return methodCallArgumentTypes != null;
    }

    public Iterator<VariableInfo> iterator() {
        return new Iterator<VariableInfo>() {
            VariableScope currentScope = VariableScope.this;
            Iterator<VariableInfo> currentIter = currentScope.nameVariableMap.values().iterator();

            public boolean hasNext() {
                if (currentIter == null) {
                    return false;
                }
                if (!currentIter.hasNext()) {
                    currentScope = currentScope.parent;
                    currentIter = currentScope == null ? null : currentScope.nameVariableMap.values().iterator();
                }
                return currentIter != null && currentIter.hasNext();
            }

            public VariableInfo next() {
                return new VariableInfo(currentIter.next(), currentScope.scopeNode);
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Finds all interfaces implemented by {@code type} (including itself, if it
     * is an interface).  The ordering is that the interfaces closest to type are
     * first (in declared order) and then interfaces declared on super interfaces
     * occur (if they are not duplicates).
     *
     * @param allInterfaces an accumulator set that will ensure that each interface exists at most once and in a predictible order
     * @param useResolved whether or not to use the resolved interfaces
     */
    public static void findAllInterfaces(ClassNode type, LinkedHashSet<ClassNode> allInterfaces, boolean useResolved) {
        if (!useResolved) type = type.redirect();
        boolean isInterface = type.isInterface();
        if (!isInterface || !allInterfaces.contains(type)) {
            if (isInterface) {
                allInterfaces.add(type);
            }
            // Urrrgh...I don't like this.
            // Groovy compiler has a different notion of 'resolved' than we do here.
            // Groovy compiler considers a resolved ClassNode one that has no redirect.
            // However, we consider a ClassNode to be resolved if its type parameters are resolved.
            ClassNode[] faces = !useResolved ? type.getInterfaces() : type.getUnresolvedInterfaces();
            if (faces != null) {
                for (ClassNode face : faces) {
                    findAllInterfaces(face, allInterfaces, useResolved);
                }
            }
            if (!isInterface) {
                ClassNode superType = type.getSuperClass();
                if (superType != null && !OBJECT_CLASS_NODE.equals(superType)) {
                    findAllInterfaces(superType, allInterfaces, useResolved);
                }
            }
        }
    }

    /**
     * Creates a type hierarchy for the <code>clazz</code>>, including self.
     * Classes come first and then interfaces.
     * <p>
     * FIXADE: The ordering of super interfaces will not be the same as in {@link VariableScope#findAllInterfaces(ClassNode, LinkedHashSet, boolean)}. Should we make it the same?
     */
    public static void createTypeHierarchy(ClassNode type, LinkedHashSet<ClassNode> allClasses, boolean useResolved) {
        if (!useResolved) {
            type = type.redirect();
        }
        if (!allClasses.contains(type)) {
            if (!type.isInterface()) {
                allClasses.add(type);
                ClassNode superClass;
                // Urrrgh...I don't like this.
                // Groovy compiler has a different notion of 'resolved' than we do here.
                // Groovy compiler considers a resolved ClassNode one that has no redirect.
                // however, we consider a ClassNode to be resolved if its type parameters are resolved.
                // that is why we call getUnresolvedSuperClass if useResolved is true (and vice versa).
                if (useResolved) {
                    superClass = type.getUnresolvedSuperClass();
                } else {
                    superClass = type.getSuperClass();
                }

                if (superClass != null) {
                    createTypeHierarchy(superClass, allClasses, useResolved);
                }
            }
            // interfaces will be added from the top-most type first
            findAllInterfaces(type, allClasses, useResolved);
        }
    }

    /**
     * Extracts an element type from a collection
     *
     * @param collectionType a collection object, or an object that is iterable
     */
    public static ClassNode extractElementType(ClassNode collectionType) {
        // if array, then use the component type
        if (collectionType.isArray()) {
            return collectionType.getComponentType();
        }

        // check to see if this type has an iterator method
        // if so, then resolve the type parameters
        MethodNode iterator = collectionType.getMethod("iterator", Parameter.EMPTY_ARRAY);
        ClassNode typeToResolve = null;
        if (iterator == null && collectionType.isInterface()) {
            // could be a type that implements List
            if (collectionType.implementsInterface(LIST_CLASS_NODE) && collectionType.getGenericsTypes() != null
                    && collectionType.getGenericsTypes().length == 1) {
                typeToResolve = collectionType;
            } else if (collectionType.declaresInterface(ITERATOR_CLASS) || collectionType.equals(ITERATOR_CLASS)
                    || collectionType.declaresInterface(ENUMERATION_CLASS) || collectionType.equals(ENUMERATION_CLASS)) {
                // if the type is an iterator or an enumeration, then resolve the type parameter
                typeToResolve = collectionType;
            } else if (collectionType.declaresInterface(MAP_CLASS_NODE) || collectionType.equals(MAP_CLASS_NODE)) {
                // if the type is a map, then resolve the entrySet
                MethodNode entrySetMethod = collectionType.getMethod("entrySet", Parameter.EMPTY_ARRAY);
                if (entrySetMethod != null) {
                    typeToResolve = entrySetMethod.getReturnType();
                }
            }
        } else if (iterator != null) {
            typeToResolve = iterator.getReturnType();
        }

        if (typeToResolve != null) {
            typeToResolve = clone(typeToResolve);
            ClassNode unresolvedCollectionType = collectionType.redirect();
            GenericsMapper mapper = GenericsMapper.gatherGenerics(collectionType, unresolvedCollectionType);
            ClassNode resolved = resolveTypeParameterization(mapper, typeToResolve);

            // the first type parameter of resolvedReturn should be what we want
            GenericsType[] resolvedReturnGenerics = resolved.getGenericsTypes();
            if (resolvedReturnGenerics != null && resolvedReturnGenerics.length > 0) {
                return resolvedReturnGenerics[0].getType();
            }
        }

        // this is hardcoded from DGM
        if (collectionType.declaresInterface(INPUT_STREAM_CLASS) || collectionType.declaresInterface(DATA_INPUT_STREAM_CLASS)
                || collectionType.equals(INPUT_STREAM_CLASS) || collectionType.equals(DATA_INPUT_STREAM_CLASS)) {
            return BYTE_CLASS_NODE;
        }

        // else assume collection of size 1 (itself)
        return collectionType;
    }

    /**
     * @return true iff the current scope is the implicit run method of a script
     */
    public boolean inScriptRunMethod() {
        return shared.isRunMethod;
    }

    public static boolean isPlainClosure(ClassNode type) {
        return CLOSURE_CLASS_NODE.equals(type) && !type.isUsingGenerics();
    }

    public static boolean isParameterizedClosure(ClassNode type) {
        return CLOSURE_CLASS_NODE.equals(type) &&  type.isUsingGenerics();
    }

    public static boolean isThisOrSuper(Variable var) {
        return var.getName().equals("this") || var.getName().equals("super");
    }

    public static boolean isVoidOrObject(ClassNode type) {
        return type != null && (type.getName().equals(VOID_CLASS_NODE.getName()) ||
                type.getName().equals(VOID_WRAPPER_CLASS_NODE.getName()) ||
                type.getName().equals(OBJECT_CLASS_NODE.getName()));
    }
}
