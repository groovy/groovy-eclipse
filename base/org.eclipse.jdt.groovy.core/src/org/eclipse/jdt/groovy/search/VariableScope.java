/*
 * Copyright 2009-2025 the original author or authors.
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

import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GenericsUtils.parseClassNodesFromString;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.evaluateExpression;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.lang.Tuple;
import groovy.transform.stc.ClosureParams;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImmutableClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCall;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.ast.tools.WideningCategories;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.DefaultGroovyStaticMethods;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.transform.trait.Traits;
import org.codehaus.jdt.groovy.internal.compiler.GroovyClassLoaderFactory.GrapeAwareGroovyClassLoader;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTMethodNode;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;

/**
 * Maps variable names to types in a hierarchy.
 */
public class VariableScope implements Iterable<VariableScope.VariableInfo> {

    public static final ClassNode NULL_TYPE = new ImmutableClassNode(Object.class);
    public static final ClassNode VOID_CLASS_NODE = ClassHelper.VOID_TYPE; // void.class
    public static final ClassNode VOID_WRAPPER_CLASS_NODE = ClassHelper.void_WRAPPER_TYPE; // Void.class

    public static final ClassNode CLASS_CLASS_NODE = ClassHelper.CLASS_Type;
    public static final ClassNode OBJECT_CLASS_NODE = ClassHelper.OBJECT_TYPE;
    public static final ClassNode GROOVY_OBJECT_CLASS_NODE = ClassHelper.GROOVY_OBJECT_TYPE;
    public static final ClassNode GROOVY_SUPPORT_CLASS_NODE = ClassHelper.GROOVY_OBJECT_SUPPORT_TYPE;
    public static final ClassNode ENUMERATION_CLASS_NODE = ClassHelper.make(Enumeration.class);
    public static final ClassNode COLLECTION_CLASS_NODE = ClassHelper.make(Collection.class);
    public static final ClassNode ITERABLE_CLASS_NODE = ClassHelper.make(Iterable.class);
    public static final ClassNode ITERATOR_CLASS_NODE = ClassHelper.Iterator_TYPE;
    public static final ClassNode LIST_CLASS_NODE = ClassHelper.LIST_TYPE;
    public static final ClassNode MAP_CLASS_NODE = ClassHelper.MAP_TYPE;
    public static final ClassNode ENTRY_CLASS_NODE = ClassHelper.make(Map.Entry.class);
    public static final ClassNode RANGE_CLASS_NODE = ClassHelper.RANGE_TYPE;
    public static final ClassNode TUPLE_CLASS_NODE = ClassHelper.make(Tuple.class);
    public static final ClassNode BIG_DECIMAL_CLASS = ClassHelper.BigDecimal_TYPE;
    public static final ClassNode BIG_INTEGER_CLASS = ClassHelper.BigInteger_TYPE;
    public static final ClassNode NUMBER_CLASS_NODE = ClassHelper.Number_TYPE;
    public static final ClassNode STRING_CLASS_NODE = ClassHelper.STRING_TYPE;
    public static final ClassNode GSTRING_CLASS_NODE = ClassHelper.GSTRING_TYPE;
    public static final ClassNode CLOSURE_CLASS_NODE = ClassHelper.CLOSURE_TYPE;
    public static final ClassNode PATTERN_CLASS_NODE = ClassHelper.PATTERN_TYPE;
    public static final ClassNode MATCHER_CLASS_NODE = ClassHelper.make(Matcher.class);

    public static final ClassNode FILE_CLASS_NODE = ClassHelper.make(File.class);
    public static final ClassNode READER_CLASS_NODE = ClassHelper.make(Reader.class);
    public static final ClassNode INPUT_STREAM_CLASS_NODE = ClassHelper.make(InputStream.class);
    public static final ClassNode OUTPUT_STREAM_CLASS_NODE = ClassHelper.make(OutputStream.class);
    public static final ClassNode DATA_INPUT_STREAM_CLASS_NODE = ClassHelper.make(DataInputStream.class);
    public static final ClassNode DATA_OUTPUT_STREAM_CLASS_NODE = ClassHelper.make(DataOutputStream.class);

    // present in Groovy 2.1+
    public static final ClassNode DELEGATES_TO = ClassHelper.make(DelegatesTo.class);
    // present in Groovy 2.3+
    public static final ClassNode CLOSURE_PARAMS = ClassHelper.make(ClosureParams.class);

    // standard category classes
    public static final ClassNode DGM_CLASS_NODE = ClassHelper.make(DefaultGroovyMethods.class);
    public static final ClassNode DGSM_CLASS_NODE = ClassHelper.make(DefaultGroovyStaticMethods.class);

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

    /**
     * Null for the top level scope.
     */
    private VariableScope parent;

    /**
     * State shared with all scopes.
     */
    private SharedState shared;

    /**
     * AST node for this scope, typically, a block, closure, or body declaration.
     */
    private ASTNode scopeNode;

    /**
     * Is the current node not the RHS of a dotted expression?
     */
    private boolean isPrimaryNode;

    private final boolean isStaticScope;

    /**
     * Category that will be declared in the next scope.
     */
    private ClassNode categoryBeingDeclared;

    /**
     * Variables from parent scopes that have been updated in this or a child scope.
     */
    private Set<String> dirtyNames;

    private int enclosingCallStackDepth;
    private List<ClassNode> methodCallArgumentTypes;
    private GenericsType[]  methodCallGenericsTypes;
    private final Map<String, VariableInfo> nameVariableMap = new HashMap<>();

    //--------------------------------------------------------------------------

    public VariableScope(VariableScope parent, ASTNode enclosingNode, boolean isStatic) {
        this.parent = parent;
        this.scopeNode = enclosingNode;
        this.shared = (parent != null ? parent.shared : new SharedState());
        this.enclosingCallStackDepth = this.shared.enclosingCallStack.size();
        this.isStaticScope = (isStatic || (parent != null && parent.isStaticScope && !(enclosingNode instanceof ClassNode))) &&
                              (getEnclosingClosureScope() == null); // if in a closure, items may be found on delegate or owner

        // determine if scope belongs to script body
        if (enclosingNode instanceof ClassNode || enclosingNode instanceof FieldNode) {
            this.shared.isRunMethod = false;
        } else if (enclosingNode instanceof MethodNode) {
            this.shared.isRunMethod = ((MethodNode) enclosingNode).isScriptBody();
        }
        // TODO: Should the flag be restored to its previous value when scope is popped?

        // initialize type of "this" (and by extension "super"; see lookupName("super"))
        if (enclosingNode instanceof ClassNode) {
            ClassNode type = (ClassNode) enclosingNode;
            addVariable("this", newClassClassNode(type), type);
        } else if (!isStaticScope && !(enclosingNode instanceof ClosureExpression) && (parent != null && parent.scopeNode instanceof ClassNode)) {
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
     * @return all categories active in this scope
     */
    public Set<ClassNode> getCategoryNames() {
        Set<ClassNode> categories;

        if (parent != null) {
            categories = parent.getCategoryNames();
            // look at the parent scope's category, not this scope's category
            // if this scope represents a "use" block, category is not active
            // until child scopes
            if (parent.isCategoryBeingDeclared()) {
                categories = new LinkedHashSet<>(categories);
                categories.add(parent.categoryBeingDeclared);
                for (ClassNode superClass = parent.categoryBeingDeclared.getSuperClass();
                        superClass != OBJECT_CLASS_NODE && superClass != null;
                        superClass = superClass.getSuperClass()) {
                    categories.add(superClass);
                }
            }
        } else {
            categories = scopeNode.getNodeMetaData(DefaultGroovyMethods.class);
            if (categories == null) {
                GrapeAwareGroovyClassLoader gcl = (GrapeAwareGroovyClassLoader) ((ModuleNode) scopeNode).getUnit().getClassLoader();
                categories = gcl.getDefaultCategories().stream().map(ClassNode::new).collect(Collectors.toCollection(LinkedHashSet::new));
                synchronized (scopeNode) {
                    Set<ClassNode> value = categories;
                    scopeNode.getNodeMetaData(DefaultGroovyMethods.class, key -> value);
                }
            }
        }

        return categories;
    }

    private boolean isCategoryBeingDeclared() {
        return (categoryBeingDeclared != null);
    }

    public void setCategoryBeingDeclared(ClassNode categoryBeingDeclared) {
        this.categoryBeingDeclared = categoryBeingDeclared;
    }

    public boolean isDefaultCategory(ClassNode category) {
        ModuleNode module = getEnclosingModuleNode();
        Set<ClassNode> defaultCategories = module.getNodeMetaData(DefaultGroovyMethods.class);

        return defaultCategories.contains(category);
    }

    public boolean isDefaultStaticCategory(ClassNode category) {
        ModuleNode module = getEnclosingModuleNode();
        GrapeAwareGroovyClassLoader loader = (GrapeAwareGroovyClassLoader) module.getUnit().getClassLoader();

        return loader.isDefaultStaticCategory(category.getName());
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
                        ClassNode[] superInterfaces = type.getInterfaces();
                        if (superInterfaces.length > 0 && GroovyUtils.getGroovyVersion().getMajor() > 3) {
                            String union = Stream.concat(Stream.of(superType), Stream.of(superInterfaces))
                                .map(t -> t.toString(false)).collect(Collectors.joining(" | ", "(", ")"));
                            superType = new ClassNode(union, 0, superType, superInterfaces.clone(), null);
                        }
                    } else { // type is Class<T>, so produce Class<"super of T">
                        assert (type.equals(CLASS_CLASS_NODE) && type.getGenericsTypes() != null);
                        superType = type.getGenericsTypes()[0].getType().getSuperClass();
                        if (superType != null && !superType.equals(OBJECT_CLASS_NODE)) {
                            superType = newClassClassNode(superType);
                        }
                    }
                }

                return new VariableInfo(name, superType, superType);
            }
        }

        VariableInfo info = lookupNameInCurrentScope(name);
        if (info == null && parent != null) {
            info = parent.lookupName(name);
        }
        return info;
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
        for (VariableScope scope = this; scope != null; scope = scope.parent) {
            if (scope.isStatic()) {
                return true;
            }
            if (scope.scopeNode instanceof ClassNode ||
                scope.scopeNode instanceof FieldNode ||
                scope.scopeNode instanceof MethodNode) {
                break;
            }
        }
        return CLASS_CLASS_NODE.equals(getOwner());
    }

    public boolean isFieldAccessDirect() {
        return (getEnclosingClosureScope() == null);
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
        for (VariableScope scope = this; scope != null; scope = scope.parent) {
            if (scope.scopeNode instanceof ClosureExpression) {
                return scope;
            }
            if (scope.scopeNode instanceof ClassNode) {
                break;
            }
        }
        return null;
    }

    public BinaryExpression getEnclosingAssignment() {
        Object node = getWormhole().get("enclosingAssignment");
        if (node instanceof BinaryExpression) {
            return (BinaryExpression) node;
        } else {
            return null;
        }
    }

    public Optional<Token> getEnclosingAssignmentOperator() {
        return Optional.ofNullable(getEnclosingAssignment()).map(expr -> Optional.ofNullable((Token) expr.getNodeMetaData("original.operator")).orElse(expr.getOperation()));
    }

    /**
     * Adds specified variable declaration to this scope.
     */
    public void addVariable(Variable var) {
        addVariable(var.getName(), var.getType(), ((AnnotatedNode) var).getDeclaringClass());
    }

    /**
     * Adds specified variable declaration to this scope.
     *
     * @param name name of variable
     * @param type type of variable
     * @param declaringType enclosing type declaration of variable
     */
    public void addVariable(String name, ClassNode type, ClassNode declaringType) {
        if (declaringType == null) declaringType = getEnclosingTypeDeclaration();
        nameVariableMap.put(name, new VariableInfo(name, type, declaringType));
    }

    /**
     * Updates the type info of the given variable if it already exists in this
     * or an enclosing scope, or adds it if it doesn't.
     *
     * @param name name of variable
     * @param type type of variable
     * @param declaringType enclosing type declaration of variable
     */
    public void updateOrAddVariable(String name, ClassNode type, ClassNode declaringType) {
        if (!updateVariableImpl(name, type, declaringType)) {
            addVariable(name, type, declaringType);
        }
    }

    /**
     * Updates the type info of the given variable if it already exists in this
     * or an enclosing scope.
     *
     * @param name name of variable
     * @param type type of variable
     * @param declaringType enclosing type declaration of variable
     */
    public void updateVariable(String name, ClassNode type, ClassNode declaringType) {
        updateVariableImpl(name, type, declaringType);
    }

    /**
     * @param name name of variable
     * @param type type of variable
     * @param declaringType enclosing type declaration of variable
     * @return {@code true} if the type has been udpated, {@code false} otherwise
     */
    private boolean updateVariableImpl(String name, ClassNode type, ClassNode declaringType) {
        VariableInfo info = nameVariableMap.get(name);
        if (info == null && parent != null) {
            info = parent.lookupName(name);
        }
        if (info != null) {
            nameVariableMap.put(name, merge(info, type, declaringType));
            // if variable is declared in a parent scope, mark it dirty
            if (info.scopeNode != this.scopeNode) {
                if (dirtyNames == null)
                    dirtyNames = new HashSet<>();
                dirtyNames.add(name);
            }
            return true;
        }
        return false;
    }

    /**
     * Updates the type info of the given variable in this scope but not in any
     * enclosing scopes.  This is useful for exposing an inferred type, like an
     * instanceof expression might yield.
     *
     * @param name name of variable
     * @param type type of variable
     */
    /*package*/ void updateVariableSoft(String name, ClassNode type) {
        nameVariableMap.put(name, merge(parent.lookupName(name), type, null));
    }

    private static VariableInfo merge(VariableInfo base, ClassNode type, ClassNode declaringType) {
        if (declaringType == null) declaringType = base.declaringType;
        VariableInfo info = new VariableInfo(base.name, type, declaringType);
        info.scopeNode = base.scopeNode; // preserve declaring scope
        return info;
    }

    void bubbleUpdates() {
        if (dirtyNames != null && !dirtyNames.isEmpty() && !isTerminal() && !isTopLevel()) {
            for (String name : dirtyNames) {
                VariableInfo info = nameVariableMap.get(name);
                parent.updateVariable(name, info.type, info.declaringType);
            }
        }
        dirtyNames = null;
    }

    void bubbleUpdates(final VariableScope defaultBranch, final VariableScope... conditionalBranches) {

        VariableScope[] nonTerminalBranches = Stream.concat(Stream.of(conditionalBranches), Stream.of(defaultBranch)).filter(it -> !it.isTerminal()).toArray(VariableScope[]::new);

        Stream.of(nonTerminalBranches).flatMap(it -> it.dirtyNames != null ? it.dirtyNames.stream() : Stream.empty()).distinct().forEach(name -> {
            ClassNode type = null;

            // if name is not set in all branches, mix with current type (if non-null), otherwise replace current type
            if (!Stream.of(nonTerminalBranches).allMatch(it -> it.dirtyNames != null && it.dirtyNames.contains(name))) {
                type = lookupName(name).type;
            }

            type = Stream.of(nonTerminalBranches).filter(it -> it.dirtyNames != null && it.dirtyNames.contains(name))
                .map(it -> it.nameVariableMap.get(name).type).reduce(type, (t0, t1) -> t0 == null ? t1 : WideningCategories.lowestUpperBound(t0, t1));

            parent.updateVariable(name, type, lookupName(name).declaringType);
            if (dirtyNames != null) dirtyNames.remove(name);
        });

        bubbleUpdates();
    }

    public static ClassNode resolveTypeParameterization(GenericsMapper mapper, ClassNode type) {
        if (mapper.hasGenerics()) {
            GenericsType[] genericsTypes = GroovyUtils.getGenericsTypes(type);
            int n = genericsTypes.length;
            if (n > 0) {
                for (int i = 0; i < n; i += 1) {
                    ClassNode maybe = resolveTypeParameterization(mapper, genericsTypes[i], type);
                    if (maybe != type) {
                        assert n == 1;
                        type = maybe;
                        break;
                    }
                }
            } else if (GroovyUtils.getBaseType(type).isGenericsPlaceHolder()) {
                int dims = 0;
                while (type.isArray()) {
                    type = type.getComponentType();
                    dims += 1;
                }
                type = mapper.findParameter(type.getUnresolvedName(), type);
                while (dims > 0) {
                    type = type.makeArray();
                    dims -= 1;
                }
            }
        }
        return type;
    }

    public static ClassNode resolveTypeParameterization(GenericsMapper mapper, GenericsType generic, ClassNode unresolved) {
        if (!generic.isWildcard()) {
            int dims = 0;
            ClassNode type = generic.getType();
            while (type.isArray()) {
                dims += 1;
                type = type.getComponentType();
            }
            if (!type.isGenericsPlaceHolder()) {
                if (GenericsUtils.hasUnresolvedGenerics(type))
                    resolveTypeParameterization(mapper, type); // drill down
                return unresolved;
            }
            String typeParameterName = type.getUnresolvedName();
            type = mapper.findParameter(typeParameterName, type);
            while (dims > 0) {
                dims -= 1;
                type = type.makeArray();
            }

            if (unresolved.toString(false).equals(typeParameterName)) {
                // E --> String
                return type;
            } else {
                Assert.isLegal(unresolved.redirect() != unresolved, "Error: trying to mutate type parameters of a type declaration: " + unresolved);
                Assert.isLegal(unresolved.redirect().getGenericsTypes() != null, "Error: trying to add type arguments to non-generic type: " + unresolved);
                // List<E> --> List<String>
                // List<E[]> --> List<String[]>
                if (Boolean.TRUE.equals(type.getNodeMetaData("?"))) {
                    type = ClassHelper.makeWithoutCaching("?");
                    generic.setWildcard(true);
                } else {
                    generic.setWildcard(false);
                    generic.setPlaceHolder(type.isGenericsPlaceHolder());
                    generic.setName(type.isGenericsPlaceHolder() ? type.getUnresolvedName() : type.getName());
                }
                generic.setType(type);
                generic.setResolved(true);
                generic.setLowerBound(null);
                generic.setUpperBounds(null);
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
                // simplify "? extends FinalClass" to just "FinalClass"
                if (k == 1 && Flags.isFinal(resolved.getModifiers())) {
                    generic.setUpperBounds(null);
                    generic.setWildcard(false);
                    generic.setType(resolved);
                }
            }
            generic.setResolved(true);
        }
        return unresolved;
    }

    public static MethodNode resolveTypeParameterization(GenericsMapper mapper, MethodNode method) {
        if (mapper.hasGenerics() && (GenericsUtils.hasUnresolvedGenerics(method.getReturnType()) ||
                GroovyUtils.getParameterTypes(method.getParameters()).stream().anyMatch(GenericsUtils::hasUnresolvedGenerics))) {

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

    /**
     * Create a copy of this class, taking into account generics and redirects
     */
    public static ClassNode clone(ClassNode type) {
        return cloneInternal(type, 0);
    }

    /**
     * Create a copy of this {@link GenericsType}
     *
     * @param gt the {@link GenericsType} to copy
     * @param depth prevent infinite recursion on bad generics
     */
    public static GenericsType clone(GenericsType gt, int depth) {
        GenericsType newgt = new GenericsType();
        newgt.setType(cloneInternal(gt.getType(), depth + 1));
        newgt.setLowerBound(cloneInternal(gt.getLowerBound(), depth + 1));
        ClassNode[] oldUpperBounds = gt.getUpperBounds();
        if (oldUpperBounds != null) {
            int n = oldUpperBounds.length;
            ClassNode[] newUpperBounds = new ClassNode[n];
            for (int i = 0; i < n; i += 1) {
                newUpperBounds[i] = cloneInternal(oldUpperBounds[i], depth + 1);
            }
            newgt.setUpperBounds(newUpperBounds);
        }
        newgt.setName(gt.getName());
        newgt.setPlaceholder(gt.isPlaceholder());
        newgt.setWildcard(gt.isWildcard());
        newgt.setResolved(gt.isResolved());
        newgt.setSourcePosition(gt);
        return newgt;
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

    public static ClassNode clonedClosure() {
        ClassNode clone = clone(CLOSURE_CLASS_NODE);
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
        if (type == null || ClassHelper.isPrimitiveType(type)) {
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

    /**
     * Does the following name exist in this scope (does not recur up to parent scopes).
     *
     * @return {@code true} iff in the {@link #nameVariableMap}
     */
    public boolean containsInThisScope(String name) {
        return nameVariableMap.containsKey(name);
    }

    /*package*/ boolean isCasePredicate(ClosureExpression closure) {
        return (parent != null && parent.scopeNode instanceof SwitchStatement &&
            ((SwitchStatement) parent.scopeNode).getCaseStatements().stream().anyMatch(cs -> cs.getExpression() == closure));
    }

    /*package*/ void setMethodCallArgumentTypes(List<ClassNode> methodCallArgumentTypes) {
        this.methodCallArgumentTypes = methodCallArgumentTypes;
    }

    public List<ClassNode> getMethodCallArgumentTypes() {
        return methodCallArgumentTypes;
    }

    /*package*/ void setMethodCallGenericsTypes(GenericsType[] methodCallGenericsTypes) {
        this.methodCallGenericsTypes = methodCallGenericsTypes;
    }

    public GenericsType[] getMethodCallGenericsTypes() {
        return methodCallGenericsTypes;
    }

    /**
     * If visiting the identifier of a method call expression, this field will
     * be equal to the number of arguments to the method call.
     */
    public int getMethodCallNumberOfArguments() {
        return (isMethodCall() ? methodCallArgumentTypes.size() : 0);
    }

    public boolean isMethodCall() {
        return (methodCallArgumentTypes != null);
    }

    /**
     * @return {@code true} if scope is enclosed by the implicit run method of a script
     */
    public boolean inScriptRunMethod() {
        return shared.isRunMethod;
    }

    /**
     * @return {@code true} if scope contains an unconditional return
     */
    public boolean isTerminal() {
        if (scopeNode instanceof BlockStatement) {
            return ((BlockStatement) scopeNode).getStatements()
                .stream().anyMatch(s -> s instanceof ReturnStatement);
        }
        if (scopeNode instanceof ReturnStatement) {
            return true;
        }
        if (scopeNode instanceof ThrowStatement) {
            // TODO: What about throw? Could be caught by outer scope...
        }
        if (scopeNode instanceof MethodNode && !(scopeNode instanceof ConstructorNode ||
                (((MethodNode) scopeNode).isStatic() && ((MethodNode) scopeNode).getName().equals("<clinit>")) ||
                (!((MethodNode) scopeNode).isStatic() && GroovyUtils.getAnnotations((MethodNode) scopeNode, "javax.annotation.PostConstruct").anyMatch(x -> true)))) {
            return true;
        }
        return false;
    }

    public boolean isTopLevel() {
        return (parent == null);
    }

    @Override
    public Iterator<VariableInfo> iterator() {
        return new Iterator<VariableInfo>() {
            VariableScope currentScope = VariableScope.this;
            Iterator<VariableInfo> currentIter = currentScope.nameVariableMap.values().iterator();

            @Override
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

            @Override
            public VariableInfo next() {
                return new VariableInfo(currentIter.next(), currentScope.scopeNode);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Finds all interfaces implemented by {@code type} (including itself, if it
     * is an interface). The ordering is that the interfaces closest to type are
     * first (in declared order with traits reversed) and then any non-duplicate
     * interfaces declared on super interfaces.
     *
     * @param accumulator ensures that each interface exists at most once and in a predictible order
     * @param useResolved whether or not to use the resolved interfaces
     */
    public static void findAllInterfaces(ClassNode type, final Set<ClassNode> accumulator, final boolean useResolved) {
        if (!useResolved) type = type.redirect();
        boolean isInterface = type.isInterface();
        if (!isInterface || accumulator.add(type)) {
            ClassNode[] interfaces = type.getInterfaces();
            if (interfaces != null && interfaces.length > 0) {
                // put traits first, in reverse declared order
                Deque<ClassNode> todo = new LinkedList<>();
                for (ClassNode face : interfaces) {
                    if (Traits.isTrait(face)) {
                        todo.addFirst(face);
                    } else {
                        todo.addLast(face);
                    }
                }
                for (ClassNode face : todo) {
                    findAllInterfaces(face, accumulator, useResolved);
                }
            }
            if (!isInterface) {
                ClassNode sc = type.getUnresolvedSuperClass();
                if (sc != null && !sc.equals(OBJECT_CLASS_NODE)) {
                    findAllInterfaces(sc, accumulator, useResolved);
                }
            }
        }
    }

    /**
     * Creates a type hierarchy for {@code type}, including itself. Classes come
     * first, followed by interfaces.
     * <p>
     * TODO: The order of super interfaces will not be the same as in {@link #findAllInterfaces}.  Should it be the same?
     */
    public static void createTypeHierarchy(ClassNode type, final Set<ClassNode> accumulator, final boolean useResolved) {
        if (!useResolved) type = type.redirect();
        if (!accumulator.contains(type)) {
            ClassNode[] bounds = null;
            if (!type.isInterface()) {
                if (type.isGenericsPlaceHolder() && type.getGenericsTypes() != null) {
                    bounds = type.getGenericsTypes()[0].getUpperBounds();
                    if (bounds != null && bounds.length > 0) {
                        type = bounds[0]; // "T extends X ..."
                    }
                }
                accumulator.add(type);
                // Groovy compiler has a different notion of 'resolved' than we do here.
                // It considers a ClassNode resolved if it is primary or has type class.
                // We consider a ClassNode resolved if it has resolved type argument(s).
                ClassNode sc = type.getUnresolvedSuperClass();
                if (sc != null) { // includes java.lang.Object
                    createTypeHierarchy(sc, accumulator, useResolved);
                }
            }
            if (!type.equals(OBJECT_CLASS_NODE)) {
                findAllInterfaces(type, accumulator, useResolved);
            }
            if (bounds != null && bounds.length > 1) {
                for (int i = 1; i < bounds.length; i += 1) {
                    findAllInterfaces(bounds[i], accumulator, useResolved);
                }
            }
        }
    }

    /**
     * Returns element type if {@code type} is an array or iterable/iterator.
     */
    public static ClassNode extractElementType(ClassNode type) {
        if (type.isArray()) {
            return type.getComponentType();
        }

        MethodNode method = GroovyUtils.getMethod(type, "iterator", Parameter.EMPTY_ARRAY);
        if (method == null) {
            if (GeneralUtils.isOrImplements(type, MAP_CLASS_NODE)) {
                method = GroovyUtils.getMethod(type, "entrySet", Parameter.EMPTY_ARRAY);
            } else if (GeneralUtils.isOrImplements(type, ITERATOR_CLASS_NODE)) {
                method = GroovyUtils.getMethod(type, "next", Parameter.EMPTY_ARRAY);
            } else if (GeneralUtils.isOrImplements(type, ENUMERATION_CLASS_NODE)) {
                method = GroovyUtils.getMethod(type, "nextElement", Parameter.EMPTY_ARRAY);
            }
        }
        if (method != null) {
            GenericsMapper mapper = GenericsMapper.gatherGenerics(Collections.EMPTY_LIST, type, method);
            ClassNode returnType = resolveTypeParameterization(mapper, clone(method.getReturnType()));
            switch (method.getName()) {
            case "iterator":
                if (GeneralUtils.isOrImplements(type, ITERABLE_CLASS_NODE)) {
                    GenericsType[] generics = GroovyUtils.getGenericsTypes(returnType);
                    if (generics.length == 1) return generics[0].getType();
                    return OBJECT_CLASS_NODE;
                }
                // TODO: Is this right for general iterator()?
                return extractElementType(returnType);
            case "entrySet":
                if (true) {
                    GenericsType[] generics = GroovyUtils.getGenericsTypes(returnType);
                    if (generics.length == 1) return generics[0].getType();
                    return GenericsUtils.nonGeneric(ENTRY_CLASS_NODE);
                }
                // falls through (not really)
            default:
                if (GroovyUtils.getBaseType(returnType).isGenericsPlaceHolder()) {
                    return GenericsUtils.nonGeneric(returnType);
                }
                return returnType;
            }
        }

        // TODO: Can DGM iterator() method lookup be generalized?

        // IOGroovyMethods.iterator(T) overloads
        if (GeneralUtils.isOrImplements(type, INPUT_STREAM_CLASS_NODE) ||
                GeneralUtils.isOrImplements(type, DATA_INPUT_STREAM_CLASS_NODE)) {
            return BYTE_CLASS_NODE;
        } else if (GeneralUtils.isOrImplements(type, READER_CLASS_NODE)) {
            return STRING_CLASS_NODE;
        }
        // StringGroovyMethods.iterator(Matcher) returns String or List<String>
        if (MATCHER_CLASS_NODE.equals(type)) {
            return OBJECT_CLASS_NODE;
        }
        // XmlGroovyMethods.iterator(NodeList): Iterator<Node>

        // String->String, otherwise assume non-aggregate type
        return type;
    }

    public static ClassNode extractSpreadType(ClassNode type) {
        ClassNode collectionType, elementType = extractElementType(type);
        if (!GeneralUtils.isOrImplements(type, MAP_CLASS_NODE)) { // end at entry set
            while (GeneralUtils.isOrImplements(elementType, COLLECTION_CLASS_NODE)) {
                collectionType = elementType;
                elementType = extractElementType(collectionType);
                if (elementType == collectionType) {
                    break; // no infinite loop
                }
            }
        }
        return elementType;
    }

    public static ClassNode getFirstGenerics(ClassNode type) {
        GenericsType[] genericsTypes = type.getGenericsTypes();
        if (genericsTypes != null) return genericsTypes[0].getType();
        return type.redirect().getGenericsTypes()[0].getType().redirect();
    }

    public static boolean isPlainClosure(ClassNode type) {
        return CLOSURE_CLASS_NODE.equals(type) && type.getGenericsTypes() == null;
    }

    public static boolean isParameterizedClosure(ClassNode type) {
        return CLOSURE_CLASS_NODE.equals(type) && type.getGenericsTypes() != null;
    }

    public static boolean isThisOrSuper(Variable var) {
        return var.getName().equals("this") || var.getName().equals("super");
    }

    public static boolean isVoidOrObject(ClassNode type) {
        return VOID_CLASS_NODE.equals(type) || OBJECT_CLASS_NODE.equals(type);
    }

    /**
     * Contains state that is shared amongst {@link VariableScope}s
     */
    private static class SharedState {
        /**
         * this field stores values that need to get passed between parts of the file to another
         */
        private final Map<String, Object> wormhole = new HashMap<>();
        /**
         * the enclosing method call is the one where there are the current node is part of an argument list
         */
        private final List<CallAndType> enclosingCallStack = new ArrayList<>();
        /**
         * Node currently being evaluated, or null if none
         */
        private final LinkedList<ASTNode> nodeStack = new LinkedList<>();
        /**
         * true iff current scope is implicit run method of script
         */
        private boolean isRunMethod;
    }

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
            this.scopeNode = (info.scopeNode != null ? info.scopeNode : node);
        }

        public String getTypeSignature() {
            return GroovyUtils.getTypeSignature(type, /*fully-qualified:*/ true, false);
        }
    }

    public static class CallAndType {

        public final MethodCall call;
        public final ASTNode declaration;
        public final ClassNode declaringType;
        private Map<ClosureExpression, Object[]> delegatesTo;

        /**
         * @param declaringType type that declares {@code declaration} in most cases;
         *        if {@code call} is a category method it's likely the calling object
         *        type; if {@code call} is an implicit-this call in a closure, then...
         */
        public CallAndType(final MethodCall call, final ASTNode declaration, final ClassNode declaringType, final ModuleNode enclosingModule) {
            this.call = call;
            this.declaration = declaration;
            this.declaringType = declaringType;

            // handle the Groovy 2.1+ @DelegatesTo annotation; see also org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor#checkClosureWithDelegatesTo
            if (declaration instanceof MethodNode) {
                MethodNode methodNode = (MethodNode) declaration;
                Parameter[] parameters = methodNode.getParameters();
                if (parameters != null && parameters.length > 0) {
                    List<Expression> arguments = null;
                    if (call.getArguments() instanceof TupleExpression) {
                        arguments = ((TupleExpression) call.getArguments()).getExpressions();
                    }
                    if (arguments != null && !arguments.isEmpty()) {
                        if (!methodNode.getDeclaringClass().equals(getPerceivedDeclaringType())) {
                            List<Expression> categoryMethodArguments = new ArrayList<>(arguments.size() + 1);
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
                                        CompilerConfiguration config = enclosingModule.getUnit().getConfig();

                                        Expression delegatesToType = annotation.getMember("type");
                                        Expression delegatesToValue = annotation.getMember("value");
                                        Expression delegatesToTarget = annotation.getMember("target");
                                        Expression delegatesToStrategy = annotation.getMember("strategy");
                                        Expression delegatesToGenericTypeIndex = annotation.getMember("genericTypeIndex");

                                        String typeName;
                                        Integer strategy;
                                        Integer generics;

                                        if (delegatesToType != null) {
                                            typeName = (String) evaluateExpression(castX(STRING_CLASS_NODE, delegatesToType), config);
                                        } else {
                                            typeName = null;
                                        }
                                        if (delegatesToStrategy != null) {
                                            strategy = (Integer) evaluateExpression(castX(INTEGER_CLASS_NODE, delegatesToStrategy), config);
                                        } else {
                                            strategy = null;
                                        }
                                        if (delegatesToGenericTypeIndex != null) {
                                            generics = (Integer) evaluateExpression(castX(INTEGER_CLASS_NODE, delegatesToGenericTypeIndex), config);
                                        } else {
                                            generics = null;
                                        }

                                        // handle three modes: @DelegatesTo(Type.class), @DelegatesTo(type="pack.Type"), @DelegatesTo(target="name", genericTypeIndex=i)
                                        if (delegatesToValue instanceof ClassExpression && !delegatesToValue.getType().getName().equals("groovy.lang.DelegatesTo$Target")) {
                                            addDelegatesToClosure(closure, delegatesToValue.getType(), strategy);

                                        } else if (typeName != null && !typeName.isEmpty()) {
                                            CompilationUnit compilationUnit = null;
                                            if (enclosingModule.getContext() instanceof org.codehaus.jdt.groovy.control.EclipseSourceUnit) {
                                                compilationUnit = ((org.codehaus.jdt.groovy.control.EclipseSourceUnit) enclosingModule.getContext()).resolver.compilationUnit;
                                            }
                                            ClassNode[] resolved = parseClassNodesFromString(typeName, enclosingModule.getContext(), compilationUnit, methodNode, delegatesToType);
                                            if (GenericsUtils.hasUnresolvedGenerics(resolved[0])) { // @DelegatesTo(type="T") or @DelegatesTo(type="List<T>")
                                                GenericsMapper mapper = GenericsMapper.gatherGenerics(GroovyUtils.getParameterTypes(methodNode.getParameters()), declaringType,
                                                    methodNode.getOriginal(), (call instanceof MethodCallExpression ? ((MethodCallExpression) call).getGenericsTypes() : null));
                                                resolved[0] = resolveTypeParameterization(mapper, resolved[0]);
                                            }
                                            addDelegatesToClosure(closure, resolved[0], strategy);

                                        } else if (delegatesToValue == null || (delegatesToValue instanceof ClassExpression && delegatesToValue.getType().getName().equals("groovy.lang.DelegatesTo$Target"))) {
                                            try {
                                                String targetName = (String) (delegatesToTarget != null ? evaluateExpression(castX(STRING_CLASS_NODE, delegatesToTarget), config) : DelegatesTo.class.getMethod("target").getDefaultValue());
                                                int j = indexOfDelegatesToTarget(parameters, targetName, config);
                                                if (j >= 0 && j < arguments.size()) {
                                                    Expression target = arguments.get(j);
                                                    ClassNode targetType = target.getType(); // TODO: Look up expression type (unless j is 0 and it's a category method).
                                                    if (!targetType.isDerivedFrom(parameters[j].getType())) targetType = parameters[j].getType();
                                                    if (generics != null && generics >= 0) { // -1 is the default value
                                                        targetType = Optional.ofNullable(targetType.getGenericsTypes())
                                                            .filter(targetGenerics -> generics < targetGenerics.length)
                                                            .map(targetGenerics -> targetGenerics[generics].getType()).orElse(OBJECT_CLASS_NODE);
                                                    }
                                                    addDelegatesToClosure(closure, targetType, strategy);
                                                }
                                            } catch (NoSuchMethodException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
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
                return getFirstGenerics(declaringType);
            }
            return declaringType;
        }

        public ClassNode getDelegateType(final ClosureExpression closure) {
            Object[] tuple = delegatesTo.get(closure);
            return (tuple != null ? (ClassNode) tuple[0] : null);
        }

        public int getResolveStrategy(final ClosureExpression closure) {
            Object[] tuple = delegatesTo.get(closure);
            return (tuple != null && tuple[1] != null ? (Integer) tuple[1] : Closure.OWNER_FIRST);
        }

        private void addDelegatesToClosure(final ClosureExpression closure, final ClassNode delegateType, final Integer resolveStrategy) {
            if (delegatesTo == null) {
                delegatesTo = new HashMap<>();
            }
            delegatesTo.put(closure, new Object[] {delegateType, resolveStrategy});
        }

        /**
         * Finds param with DelegatesTo.Target annotation that has matching value string.
         */
        private static int indexOfDelegatesToTarget(final Parameter[] parameters, final String target, final CompilerConfiguration config)
                throws NoSuchMethodException {
            for (int i = 0, n = parameters.length; i < n; i += 1) {
                List<AnnotationNode> annotations = parameters[i].getAnnotations();
                if (annotations != null && !annotations.isEmpty()) {
                    for (AnnotationNode annotation : annotations) {
                        if (annotation.getClassNode().getName().equals("groovy.lang.DelegatesTo$Target")) {
                            String value = (String) (annotation.getMember("value") != null
                                ? evaluateExpression(castX(STRING_CLASS_NODE, annotation.getMember("value")), config)
                                : DelegatesTo.Target.class.getMethod("value").getDefaultValue());
                            if (value.equals(target)) {
                                return i;
                            }
                        }
                    }
                }
            }
            return -1;
        }
    }
}
