/*
 * Copyright 2009-2021 the original author or authors.
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
package org.codehaus.jdt.groovy.internal.compiler.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.ResolveVisitor;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.jdt.groovy.internal.compiler.GroovyClassLoaderFactory.GrapeAwareGroovyClassLoader;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.MissingTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.core.builder.AbortIncrementalBuildException;

/**
 * An extension to the standard groovy ResolveVisitor that can ask JDT for types when groovy cannot find them. A groovy project in
 * Eclipse is typically configured with very limited knowledge of its dependencies so most lookups are through JDT.
 *
 * Resolver lifecycle:<br>
 * The JDTResolver is created at the same time as the (Groovy) CompilationUnit. The CompilationUnit knows about all the code that is
 * to be compiled together. The resolver maintains a cache from Binding to JDTClassNode and the cache contents have the same
 * lifetime as the JDTResolver. The resolver does type lookups through the currently active scope - the active scope is set when the
 * method 'commencingResolution()' is called. This is called by the superclass (ResolveVisitor) when it is about to start resolving
 * every reference in a type.
 */
public class JDTResolver extends ResolveVisitor {

    private static final boolean DEBUG = Boolean.parseBoolean(Platform.getDebugOption("org.codehaus.groovy.eclipse.core/debug/resolver"));

    /** Any type name that is equal to or shorter than this could be a primitive type. */
    private static final int BOOLEAN_LENGTH = "boolean".length();

    /** Arbitrary selection of common types. */
    private static final Map<String, ClassNode> COMMON_TYPES;
    static {
        Map<String, ClassNode> commonTypes = new HashMap<>();

        commonTypes.put("boolean", ClassHelper.boolean_TYPE);
        commonTypes.put("byte", ClassHelper.byte_TYPE);
        commonTypes.put("char", ClassHelper.char_TYPE);
        commonTypes.put("double", ClassHelper.double_TYPE);
        commonTypes.put("float", ClassHelper.float_TYPE);
        commonTypes.put("int", ClassHelper.int_TYPE);
        commonTypes.put("long", ClassHelper.long_TYPE);
        commonTypes.put("short", ClassHelper.short_TYPE);
        commonTypes.put("void", ClassHelper.VOID_TYPE);

        commonTypes.put("java.lang.Boolean", ClassHelper.Boolean_TYPE);
        commonTypes.put("java.lang.Byte", ClassHelper.Byte_TYPE);
        commonTypes.put("java.lang.Character", ClassHelper.Character_TYPE);
        commonTypes.put("java.lang.Double", ClassHelper.Double_TYPE);
        commonTypes.put("java.lang.Float", ClassHelper.Float_TYPE);
        commonTypes.put("java.lang.Integer", ClassHelper.Integer_TYPE);
        commonTypes.put("java.lang.Long", ClassHelper.Long_TYPE);
        commonTypes.put("java.lang.Short", ClassHelper.Short_TYPE);
        commonTypes.put("java.lang.Void", ClassHelper.void_WRAPPER_TYPE);

        commonTypes.put("java.lang.Object", ClassHelper.OBJECT_TYPE);
        commonTypes.put("java.lang.String", ClassHelper.STRING_TYPE);

        COMMON_TYPES = Collections.unmodifiableMap(commonTypes);
    }

    private void log(String string) {
        System.out.printf("JDTResolver@%x[%d]: %s%n", System.identityHashCode(this), Thread.currentThread().getId(), string);
    }

    private void log(String string, ClassNode type, boolean foundit) {
        log(string + " " + type.getName() + "? " + foundit);
    }

    // allow test cases to quiz a resolver
    public static boolean recordInstances;
    public static List<JDTResolver> instances;

    public static JDTClassNode getCachedNode(String name) {
        for (JDTResolver instance : instances) {
            JDTClassNode node = getCachedNode(instance, name);
            if (node != null) return node;
        }
        return null;
    }

    public static JDTClassNode getCachedNode(JDTResolver instance, String name) {
        for (JDTClassNode nodeFromCache : instance.nodeCache.values()) {
            if (name.equals(String.valueOf(nodeFromCache.getJdtBinding().readableName()))) {
                return nodeFromCache;
            }
        }
        return null;
    }

    public GroovyCompilationUnitScope getScope() {
        return activeScope;
    }

    // Type references are resolved through the 'activeScope'. This ensures visibility rules are obeyed - just because a
    // type exists does not mean it is visible to some other type and scope lookups verify this.
    protected GroovyCompilationUnitScope activeScope;

    // map of scopes in which resolution can happen
    private Map<ClassNode, GroovyTypeDeclaration> scopes = new IdentityHashMap<>();

    // By recording what is currently in progress in terms of creation, we avoid recursive problems (like Enum<E extends Enum<E>>)
    private Map<TypeBinding, JDTClassNode> inProgress = new IdentityHashMap<>();

    // Cache from bindings to JDTClassNodes to avoid unnecessary JDTClassNode creation
    private Map<TypeBinding, JDTClassNode> nodeCache = new IdentityHashMap<>();

    private Set<ClassNode> resolvedClassNodes = new HashSet<>();

    /**
     * Records the type names that aren't resolvable for the current resolution.
     * This means we will not repeatedly attempt to lookup something that is not
     * found through the same routes.
     */
    private Map<String, Set<String>> unresolvables = new HashMap<>();

    //--------------------------------------------------------------------------

    public JDTResolver(CompilationUnit compUnit) {
        super(compUnit);
        if (recordInstances) {
            if (instances == null) {
                instances = new ArrayList<>();
            }
            instances.add(this);
        }
    }

    /**
     * When recorded, the jdt resolver will be able to (later on) navigate from the classnode back to the JDT scope that should be used.
     */
    public void record(GroovyTypeDeclaration typeDecl) {
        // FIXASC: Can the relationship here from classNode to scope be better preserved to remove the need for this map?
        scopes.put(typeDecl.getClassNode(), typeDecl);

        TypeDeclaration[] memberTypes = typeDecl.memberTypes;
        if (memberTypes != null) {
            for (TypeDeclaration memberType : memberTypes) {
                record((GroovyTypeDeclaration) memberType);
            }
        }

        GroovyTypeDeclaration[] anonymousTypes = typeDecl.getAnonymousTypes();
        if (anonymousTypes != null) {
            for (GroovyTypeDeclaration anonymousType : anonymousTypes) {
                record(anonymousType);
            }
        }
    }

    @Override
    public void startResolving(ClassNode classNode, SourceUnit sourceUnit) {
        try {
            unresolvables.put(classNode.getModule().getMainClassName(), new HashSet<>());
            super.startResolving(classNode, sourceUnit);
        } catch (AbortResolutionException ignore) {
            // probably syntax error(s)
        } finally {
            resetSourceUnit();
            assert !scopes.containsKey(classNode);
        }
    }

    /**
     * Called when a ResolveVisitor is commencing resolution for a type. Allows
     * us to setup the JDTResolver to point at the right scope for resolution-
     * ification. If not able to find a scope, that is a serious problem!
     */
    @Override
    protected boolean commencingResolution() {
        GroovyTypeDeclaration typeDecl = scopes.remove(currentClass);
        if (typeDecl == null) {
            if (resolvedClassNodes.contains(currentClass)) {
                // already resolved
                return false;
            }
            if (activeScope != null && currentClass != null) {
                for (TypeDeclaration t : activeScope.referenceContext.types) {
                    if (currentClass == ((GroovyTypeDeclaration) t).getClassNode()) {
                        return !t.hasErrors();
                    }
                }
            }
            throw new GroovyEclipseBug("commencingResolution failed: no declaration found for class " + currentClass);
        }

        activeScope = null;
        if (typeDecl.scope == null) {
            // scope may be null if there were errors in the code - let's not freak out the user here
            if (typeDecl.hasErrors()) {
                return false;
            }
            throw new GroovyEclipseBug("commencingResolution failed: declaration found, but unexpectedly found no scope for " + currentClass.getName());
        }
        activeScope = (GroovyCompilationUnitScope) typeDecl.scope.compilationUnitScope();
        if (DEBUG) {
            log("commencing resolution for " + currentClass.getName());
        }
        if (currentClass.getOuterClass() == null) {
            // ensure JDT pre-resolve steps completed before Groovy resolve step
            activeScope.verifyMethods(activeScope.environment.methodVerifier());
            typeDecl.resolve(activeScope); // must come after verifyMethods
        }
        return true;
    }

    @Override
    protected void finishedResolution() {
        resolvedClassNodes.add(currentClass);
    }

    public synchronized void cleanUp() {
        scopes.clear();
        inProgress.clear();
        currentClass = null;
        resetVariableScope();
        setClassNodeResolver(null);
        // TODO: Reset things like currentMethod, currImportNode, etc.?
    }

    @Override
    public ClassNode resolve(String name) {
        if (name.charAt(0) == 'j' || name.length() <= BOOLEAN_LENGTH) {
            ClassNode commonType = COMMON_TYPES.get(name);
            if (commonType != null) {
                return commonType;
            }
        }

        int i = name.indexOf('<');
        if (i > 0) {
            name = name.substring(0, i);
        }
        if (name.indexOf('?') < 0) {
            for (ClassNode node : resolvedClassNodes) {
                if (node.getName().equals(name)) {
                    return node;
                }
            }

            List<ModuleNode> modules = compilationUnit.getAST().getModules();
            if (!modules.isEmpty() && !modules.get(0).getClasses().isEmpty()) {
                Set<String> unresolvable = unresolvables.computeIfAbsent(modules.get(0).getMainClassName(), x -> new HashSet<>());
                if (!unresolvable.contains(name)) {
                    synchronized (this) {
                        ClassNode previousClass = currentClass;
                        try {
                            currentClass = compilationUnit.getFirstClassNode().getPlainNodeReference();

                            ClassNode type = ClassHelper.makeWithoutCaching(name);
                            if (super.resolve(type, true, true, true)) {
                                return type.redirect();
                            } else {
                                unresolvable.add(name);
                            }
                        } finally {
                            currentClass = previousClass;
                        }
                    }
                }
            }
        }
        return ClassHelper.DYNAMIC_TYPE;
    }

    @Override
    protected boolean resolve(ClassNode type, boolean testModuleImports, boolean testDefaultImports, boolean testNestedClasses) {
        String name = type.getName();
        if (name.indexOf('?') != -1) return false;
        if (name.charAt(0) == 'j' || name.length() <= BOOLEAN_LENGTH) {
            ClassNode commonRedirect = COMMON_TYPES.get(name);
            if (commonRedirect != null) {
                type.setRedirect(commonRedirect);
                return true;
            }
        }

        Set<String> unresolvable = unresolvables.get(currentClass.getModule().getMainClassName());
        if (unresolvable.contains(name)) {
            return false;
        }

        boolean b = super.resolve(type, testModuleImports, testDefaultImports, testNestedClasses);
        if (!b) {
            unresolvable.add(name);
        }
        return b;
    }

    @Override
    protected boolean resolveNestedClass(ClassNode type) {
        boolean resolved = super.resolveNestedClass(type);
        recordDependency(type.getName());
        if (DEBUG) {
            log("resolveNestedClass", type, resolved);
        }
        return resolved;
    }

    @Override
    protected boolean resolveFromModule(ClassNode type, boolean testModuleImports) {
        boolean resolved = super.resolveFromModule(type, testModuleImports);
        recordDependency(type.getName());
        if (DEBUG) {
            log("resolveFromModule", type, resolved);
        }
        if (resolved) {
            if (type.redirect() instanceof JDTClassNode && ((JDTClassNode) type.redirect()).getJdtBinding().hasRestrictedAccess()) {
                TypeBinding binding = ((JDTClassNode) type.redirect()).getJdtBinding();
                AccessRestriction restriction = activeScope.environment().getAccessRestriction(binding.erasure());
                if (restriction != null) {
                    SingleTypeReference ref = new SingleTypeReference(type.getNameWithoutPackage().toCharArray(), ((long) type.getStart() << 32 | (long) type.getEnd() - 1));
                    activeScope.problemReporter().forbiddenReference(binding, ref, restriction.classpathEntryType, restriction.classpathEntryName, restriction.getProblemId());
                }
            }
        }
        return resolved;
    }

    @Override
    protected boolean resolveFromCompileUnit(ClassNode type) {
        boolean resolved = super.resolveFromCompileUnit(type);
        recordDependency(type.getName());
        if (DEBUG) {
            log("resolveFromCompileUnit", type, resolved);
        }
        return resolved;
    }

    @Override
    protected boolean resolveFromDefaultImports(ClassNode type, boolean testDefaultImports) {
        boolean resolved = super.resolveFromDefaultImports(type, testDefaultImports);
        recordDependency(type.getName());
        if (DEBUG) {
            log("resolveFromDefaultImports", type, resolved);
        }
        return resolved;
    }

    @Override
    protected boolean resolveFromStaticInnerClasses(ClassNode type, boolean testNestedClasses) {
        boolean resolved = super.resolveFromStaticInnerClasses(type, testNestedClasses);
        recordDependency(type.getName());
        if (DEBUG) {
            log("resolveFromStaticInnerClasses", type, resolved);
        }
        return resolved;
    }

    @Override
    protected boolean resolveToOuter(ClassNode type) {
        if (activeScope != null) {
            // ask the JDT for a binary or source type that is visible from the active scope
            char[][] compoundName = CharOperation.splitOn('.', type.getName().toCharArray());
            TypeBinding jdtBinding = null;
            try {
                jdtBinding = activeScope.getType(compoundName, compoundName.length);
            } catch (AbortCompilation t) {
                if (!(t.silentException instanceof AbortIncrementalBuildException)) {
                    throw t;
                }
            }
            if (jdtBinding instanceof ProblemReferenceBinding) {
                ProblemReferenceBinding prBinding = (ProblemReferenceBinding) jdtBinding;
                if (prBinding.problemId() == ProblemReasons.InternalNameProvided) {
                    jdtBinding = prBinding.closestMatch();
                } else if (prBinding.problemId() == ProblemReasons.NotFound &&
                        prBinding.closestMatch() instanceof MissingTypeBinding && currImportNode != null && currImportNode.isStar()) {
                    MissingTypeBinding mtBinding = (MissingTypeBinding) prBinding.closestMatch();
                    mtBinding.fPackage.knownTypes.put(compoundName[compoundName.length - 1], null);
                }
            }

            if ((jdtBinding instanceof BinaryTypeBinding || jdtBinding instanceof SourceTypeBinding) &&
                    CharOperation.equals(compoundName, ((ReferenceBinding) jdtBinding).compoundName)) {
                if (DEBUG) log("resolveToOuter (jdt)", type, true);
                type.setRedirect(convertToClassNode(jdtBinding));
                return true;
            }
        }

        // rudimentary grab support; if the compilation unit has our special classloader and a grab has occurred, try to find the class through it
        if (compilationUnit.getClassLoader() instanceof GrapeAwareGroovyClassLoader) {
            GrapeAwareGroovyClassLoader loader = (GrapeAwareGroovyClassLoader) compilationUnit.getClassLoader();
            if (loader.grabbed) {
                try {
                    Class<?> c = loader.loadClass(type.getName(), false, true);
                    if (DEBUG) log("resolveToOuter (grab)", type, true);
                    type.setRedirect(ClassHelper.make(c));
                    return true;
                } catch (ClassNotFoundException | CompilationFailedException ignore) {
                }
            }
        }

        if (DEBUG) log("resolveToOuter", type, false);
        return false;
    }

    @Override
    protected boolean resolveToInner(ClassNode type) {
        ModuleNode module = currentClass.getModule();
        if (module != null) {
            String name = type.getName();
            int i = name.lastIndexOf('.');
            if (i > 0 && module.hasPackageName()) {
                type.setName(module.getPackageName() + name.substring(0, i) + '$' + name.substring(i + 1));
                if (resolve(type, false, false, true)) {
                    return true;
                }
            }
            for (ImportNode node : module.getStarImports()) { String qual = node.getPackageName();
                type.setName(qual.substring(0, qual.length() - 1) + '$' + name.replace('.', '$'));
                if (resolve(type, false, false, true)) {
                    return true;
                }
            }
            type.setName(name);
        }
        return false;
    }

    //--------------------------------------------------------------------------

    /**
     * Converts a JDT {@code AnnotationBinding} to a Groovy {@code AnnotationNode}.
     */
    protected AnnotationNode convertToAnnotationNode(AnnotationBinding jdtBinding) {
        if (jdtBinding == null || jdtBinding.getAnnotationType().problemId() != 0) {
            return null;
        }
        return new JDTAnnotationNode(jdtBinding, this);
    }

    /**
     * Converts a JDT {@code TypeBinding} to a Groovy {@code ClassNode}.
     */
    protected ClassNode convertToClassNode(TypeBinding jdtBinding) {
        ClassNode existingNode = checkForExisting(jdtBinding);
        if (existingNode != null) {
            if (DEBUG) {
                log("Using cached ClassNode for binding " + toString(jdtBinding));
            }
            return existingNode;
        }
        if (DEBUG) {
            log("Building new JDTClassNode for binding " + toString(jdtBinding));
        }
        return createJDTClassNode(jdtBinding);
    }

    private ClassNode checkForExisting(TypeBinding jdtBinding) {
        if (jdtBinding.id > TypeIds.T_undefined && (jdtBinding.id <= TypeIds.T_JavaLangString ||
                (jdtBinding.id <= TypeIds.T_JavaLangVoid && jdtBinding.id >= TypeIds.T_JavaLangByte))) {
            ClassNode existing = COMMON_TYPES.get(String.valueOf(jdtBinding.readableName()));
            if (existing != null) {
                return existing;
            }
        }

        JDTClassNode node = inProgress.get(jdtBinding);
        if (node == null) {
            node = nodeCache.get(jdtBinding);
        }
        if (node != null) {
            assert CharOperation.equals(jdtBinding.readableName(), node.getJdtBinding().readableName());
        }
        return node;
    }

    /**
     * Creates a Groovy ClassNode that represents the JDT TypeBinding. Steps
     * include building the basic structure, marking node as 'in progress' and
     * continuing with initialization. This allows self-referential generics.
     *
     * @param jdtBinding the JDT binding for which to create a ClassNode
     */
    private ClassNode createJDTClassNode(TypeBinding jdtBinding) {
        JDTClassNodeBuilder cnb = new JDTClassNodeBuilder(this);
        ClassNode classNode = cnb.configureType(jdtBinding);
        if (classNode instanceof JDTClassNode) {
            final JDTClassNode jdtNode = (JDTClassNode) classNode;
            assert !inProgress.containsKey(jdtBinding);
            inProgress.put(jdtBinding, jdtNode);

            // fix up generics for BinaryTypeBinding
            jdtNode.setUpGenerics();

            assert nodeCache.get(jdtBinding) == null : "not unique";
            nodeCache.put(jdtBinding, jdtNode);
            inProgress.remove(jdtBinding);
        }
        return classNode;
    }

    // FIXASC callers could check if it is a 'funky' type before always recording a depedency
    // by 'funky' I mean that the type was constructed just to try something (org.foo.bar.java$lang$Wibble doesn't want recording!)
    private void recordDependency(String typeName) {
        if (activeScope != null) {
            if (typeName.indexOf('.') != -1) {
                activeScope.recordQualifiedReference(CharOperation.splitOn('.', typeName.toCharArray()));
            } else {
                activeScope.recordSimpleReference(typeName.toCharArray());
            }
        }
    }

    private static String toString(TypeBinding jdtBinding) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(jdtBinding.readableName());
        buffer.append('(').append(jdtBinding.id).append(')');
        try {
            Object lookup = ReflectionUtils.throwableGetPrivateField(jdtBinding.getClass(), "environment", jdtBinding);
            buffer.append("[from lookup ").append(Integer.toHexString(System.identityHashCode(lookup))).append(']');
        } catch (Throwable t) {
            // not available
        }
        return buffer.toString();
    }
}
