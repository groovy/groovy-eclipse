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
package org.codehaus.jdt.groovy.internal.compiler.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import groovy.lang.GroovyClassLoader;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.ResolveVisitor;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyParser.GrapeAwareGroovyClassLoader;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

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
 *
 * @author Andy Clement
 */
public class JDTResolver extends ResolveVisitor {

    /** Any type name that is equal to or shorter than this is likely to be a primitive type. */
    private static final int BOOLEAN_LENGTH = "boolean".length();

    /** Arbitrary selection of common types. */
    private static final Map<String, ClassNode> COMMON_TYPES;
    static {
        Map<String, ClassNode> commonTypes = new HashMap<String, ClassNode>();

        commonTypes.put("java.lang.Class", ClassHelper.CLASS_Type);
        commonTypes.put("java.lang.Object", ClassHelper.OBJECT_TYPE);
        commonTypes.put("java.lang.String", ClassHelper.STRING_TYPE);

        commonTypes.put("java.lang.Boolean", ClassHelper.Boolean_TYPE);
        commonTypes.put("java.lang.Byte", ClassHelper.Byte_TYPE);
        commonTypes.put("java.lang.Character", ClassHelper.Character_TYPE);
        commonTypes.put("java.lang.Double", ClassHelper.Double_TYPE);
        commonTypes.put("java.lang.Float", ClassHelper.Float_TYPE);
        commonTypes.put("java.lang.Integer", ClassHelper.Integer_TYPE);
        commonTypes.put("java.lang.Long", ClassHelper.Long_TYPE);
        commonTypes.put("java.lang.Short", ClassHelper.Short_TYPE);

        commonTypes.put("boolean", ClassHelper.boolean_TYPE);
        commonTypes.put("byte", ClassHelper.byte_TYPE);
        commonTypes.put("char", ClassHelper.char_TYPE);
        commonTypes.put("double", ClassHelper.double_TYPE);
        commonTypes.put("float", ClassHelper.float_TYPE);
        commonTypes.put("int", ClassHelper.int_TYPE);
        commonTypes.put("long", ClassHelper.long_TYPE);
        commonTypes.put("short", ClassHelper.short_TYPE);

        COMMON_TYPES = Collections.unmodifiableMap(commonTypes);
    }

    private static final boolean DEBUG = false;

    private void log(String string) {
        System.err.printf("JDTResolver@%x[%d]: %s%n", System.identityHashCode(this), Thread.currentThread().getId(), string);
    }
    private void log(String string, ClassNode type, boolean foundit) {
        log(string + " " + type.getName() + "? " + foundit);
    }

    // allow test cases to quiz a resolver
    public static boolean recordInstances = false;
    public static List<JDTResolver> instances = null;
    public static JDTClassNode getCachedNode(String name) {
        for (JDTResolver instance : instances) {
            JDTClassNode node = getCachedNode(instance, name);
            if (node != null) return node;
        }
        return null;
    }
    public static JDTClassNode getCachedNode(JDTResolver instance, String name) {
        for (JDTClassNode nodeFromCache : instance.nodeCache.values()) {
            if (name.equals(String.valueOf(nodeFromCache.jdtBinding.readableName()))) {
                return nodeFromCache;
            }
        }
        return null;
    }

    // Type references are resolved through the 'activeScope'. This ensures visibility rules are obeyed - just because a
    // type exists does not mean it is visible to some other type and scope lookups verify this.
    protected GroovyCompilationUnitScope activeScope = null;

    // map of scopes in which resolution can happen
    private Map<ClassNode, GroovyTypeDeclaration> scopes = new HashMap<ClassNode, GroovyTypeDeclaration>();

    // By recording what is currently in progress in terms of creation, we avoid recursive problems (like Enum<E extends Enum<E>>)
    private Map<TypeBinding, JDTClassNode> inProgress = new IdentityHashMap<TypeBinding, JDTClassNode>();

    // Cache from bindings to JDTClassNodes to avoid unnecessary JDTClassNode creation
    private Map<TypeBinding, JDTClassNode> nodeCache = new IdentityHashMap<TypeBinding, JDTClassNode>();

    private Set<ClassNode> resolvedClassNodes = new HashSet<ClassNode>();

    public JDTResolver(CompilationUnit groovyCompilationUnit) {
        super(groovyCompilationUnit);
        if (recordInstances) {
            if (instances == null) {
                instances = new ArrayList<JDTResolver>();
            }
            instances.add(this);
        }
    }

    public void cleanUp() {
        inProgress.clear();
        //nodeCache.clear();
    }

    @Override
    protected boolean resolveFromModule(ClassNode type, boolean testModuleImports) {
        boolean foundit = super.resolveFromModule(type, testModuleImports);
        recordDependency(type.getName());
        if (DEBUG) {
            log("resolveFromModule", type, foundit);
        }
        return foundit;
    }

    @Override
    protected boolean resolveFromCompileUnit(ClassNode type) {
        boolean foundit = super.resolveFromCompileUnit(type);
        recordDependency(type.getName());
        if (DEBUG) {
            log("resolveFromCompileUnit", type, foundit);
        }
        if (foundit) {
            return true;
        }
        if (activeScope != null) {
            // Ask JDT for a source file, visible from this scope
            ClassNode node = activeScope.lookupClassNodeForSource(type.getName(), this);
            if (DEBUG) {
                log("resolveFromCompileUnit (jdt) ", type, node != null);
            }
            if (node != null) {
                type.setRedirect(node);
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean resolveFromDefaultImports(ClassNode type, boolean testDefaultImports) {
        boolean foundit = super.resolveFromDefaultImports(type, testDefaultImports);
        if (activeScope != null) {
            // TODO need to refactor (duplicated in GroovyCompilationUnitScope)
            boolean b = testDefaultImports & !type.hasPackageName();
            // we do not resolve a vanilla name starting with a lower case letter
            // try to resolve against adefault import, because we know that the
            // default packages do not contain classes like these
            b &= !(type instanceof LowerCaseClass);
            if (b) {
                String extraImports = activeScope.compilerOptions().groovyExtraImports;
                if (extraImports != null) {
                    try {
                        String filename = String.valueOf(activeScope.referenceContext.getFileName());
                        // may be something to do
                        StringTokenizer st = new StringTokenizer(extraImports, ";");
                        // Form would be 'com.foo.*,com.bar.MyType;.gradle=com.this.*,com.foo.Type"
                        // If there is no qualifying suffix it applies to all types

                        while (st.hasMoreTokens()) {
                            String onesuffix = st.nextToken();
                            int equals = onesuffix.indexOf('=');
                            @SuppressWarnings("unused")
                            boolean shouldApply = false;
                            String imports = null;
                            if (equals == -1) {
                                // definetly applies
                                shouldApply = true;
                                imports = onesuffix;
                            } else {
                                // need to check the suffix
                                String suffix = onesuffix.substring(0, equals);
                                shouldApply = filename.endsWith(suffix);
                                imports = onesuffix.substring(equals + 1);
                            }
                            StringTokenizer st2 = new StringTokenizer(imports, ",");
                            while (st2.hasMoreTokens()) {
                                String nextElement = st2.nextToken();
                                // One of two forms: a.b.c.* or a.b.c.Type
                                if (nextElement.endsWith(".*")) {
                                    String withoutStar = nextElement.substring(0, nextElement.length() - 1);
                                    ConstructedClassWithPackage tmp = new ConstructedClassWithPackage(withoutStar, type.getName());
                                    if (resolve(tmp, false, false, false)) {
                                        type.setRedirect(tmp.redirect());
                                        return true;
                                    }
                                } else {
                                    String importedTypeName = nextElement;
                                    int asIndex = importedTypeName.indexOf(" as ");
                                    String asName = null;

                                    if (asIndex != -1) {
                                        asName = importedTypeName.substring(asIndex + 4).trim();
                                        importedTypeName = importedTypeName.substring(0, asIndex).trim();
                                    }
                                    String typeName = type.getName();
                                    if (importedTypeName.endsWith(typeName) || typeName.equals(asName)) {
                                        int lastdot = importedTypeName.lastIndexOf('.');
                                        String importTypeNameChopped = importedTypeName.substring(0, lastdot + 1);
                                        if (typeName.equals(asName)) {
                                            typeName = importedTypeName.substring(lastdot + 1);
                                        }
                                        ConstructedClassWithPackage tmp = new ConstructedClassWithPackage(importTypeNameChopped,
                                                typeName);
                                        if (resolve(tmp, false, false, false)) {
                                            type.setRedirect(tmp.redirect());
                                            return true;
                                        }
                                    }
                                }
                            }

                        }
                    } catch (Exception e) {
                        new RuntimeException("Problem processing extraImports: " + extraImports, e).printStackTrace();
                    }
                }
            }
        }

        recordDependency(type.getName());
        if (DEBUG) {
            log("resolveFromDefaultImports", type, foundit);
        }
        return foundit;
    }

    @Override
    protected boolean resolveFromStaticInnerClasses(ClassNode type, boolean testStaticInnerClasses) {
        boolean foundit = super.resolveFromStaticInnerClasses(type, testStaticInnerClasses);
        recordDependency(type.getName());
        if (DEBUG) {
            log("resolveFromStaticInnerClasses", type, foundit);
        }
        return foundit;
        // FIXASC (M3) anything special for inner types?
    }

    @Override
    protected boolean resolveFromClassCache(ClassNode type) {
        return false;
    }

    protected boolean resolveToOuter(ClassNode type) {
        return resolveToClass(type);
    }

    protected boolean resolveToClass(ClassNode type) {
        ClassNode node;
        if (activeScope != null) {
            node = activeScope.lookupClassNodeForBinary(type.getName(), this);
            if (DEBUG) {
                log("resolveToClass (jdt)", type, node != null);
            }
            if (node != null) {
                type.setRedirect(node);
                return true;
            }
        }
        // Rudimentary grab support - if the compilation unit has our special classloader and a
        // grab has occurred, try and find the class through it
        GroovyClassLoader loader = compilationUnit.getClassLoader();
        if (loader instanceof GrapeAwareGroovyClassLoader) {
            GrapeAwareGroovyClassLoader gagc = (GrapeAwareGroovyClassLoader) loader;
            if (gagc.grabbed) {
                Class<?> cls;
                try {
                    cls = loader.loadClass(type.getName(), false, true);
                } catch (ClassNotFoundException cnfe) {
                    return false;
                } catch (CompilationFailedException cfe) {
                    return false;
                }
                if (cls == null) {
                    return false;
                }
                node = ClassHelper.make(cls);
                type.setRedirect(node);
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean resolveToScript(ClassNode type) {
        return false;
    }

    // Records a list of type names that aren't resolvable for the current resolution (unresolvables is cleared in
    // finishedResolution()). This means we won't constantly attempt to lookup something that is not found through the same routes
    // over and over (GRECLIPSE-870)
    private Set<String> unresolvables = new HashSet<String>();

    @Override
    protected boolean resolve(ClassNode type, boolean testModuleImports, boolean testDefaultImports, boolean testStaticInnerClasses) {
        String name = type.getName();
        // save time by being selective about whether to consult the commonRedirectMap
        if (name.charAt(0) == 'j' || name.length() <= BOOLEAN_LENGTH) {
            ClassNode commonRedirect = COMMON_TYPES.get(type.getName());
            if (commonRedirect != null) {
                type.setRedirect(commonRedirect);
                return true;
            }
        }
        if (unresolvables.contains(name)) {
            return false;
        } else {
            boolean b = super.resolve(type, testModuleImports, testDefaultImports, testStaticInnerClasses);
            if (!b) {
                unresolvables.add(name);
            }
            return b;
        }
    }

    public ClassNode resolve(String qualifiedName) {
        ClassNode type = ClassHelper.makeWithoutCaching(qualifiedName);
        if (super.resolve(type)) {
            return type.redirect();
        } else {
            return ClassHelper.DYNAMIC_TYPE;
        }
    }

    // avoiding an inner resolve is dangerous.
    // leave a back door here to turn it back on
    // if no one complains, then safe to remove
    private static boolean doInnerResolve = Boolean.getBoolean("greclipse.doInnerResolve");

    @Override
    protected boolean resolveToInnerEnum(ClassNode type) {
        if (doInnerResolve) {
            return super.resolveToInnerEnum(type);
        }
        // inner classes are resolved by JDT, so
        // if we get here then the inner class does not exist
        return false;
    }

    @Override
    protected boolean resolveToInner(ClassNode type) {
        if (doInnerResolve) {
            return super.resolveToInner(type);
        }
        // inner classes are resolved by JDT, so
        // if we get here then the inner class does not exist
        return false;
    }

    // FIXASC callers could check if it is a 'funky' type before always recording a depedency
    // by 'funky' I mean that the type was constructed just to try something (org.foo.bar.java$lang$Wibble doesn't want recording!)
    private void recordDependency(String typename) {
        if (activeScope != null) {
            if (typename.indexOf('.') != -1) {
                activeScope.recordQualifiedReference(CharOperation.splitOn('.', typename.toCharArray()));
            } else {
                activeScope.recordSimpleReference(typename.toCharArray());
            }
        }
    }

    /**
     * Converts a JDT TypeBinding to a Groovy ClassNode.
     */
    protected ClassNode convertToClassNode(TypeBinding jdtBinding) {
        JDTClassNode existingNode = checkForExisting(jdtBinding);
        if (existingNode != null) {
            if (DEBUG) {
                log("Using cached JDTClassNode for binding " + toString(jdtBinding));
            }
            return existingNode;
        }
        if (DEBUG) {
            if (jdtBinding.id != TypeIds.T_void && !jdtBinding.isPrimitiveOrBoxedPrimitiveType()) {
                log("createJDTClassNode: Building new JDTClassNode for binding " + toString(jdtBinding));
            }
        }
        return createJDTClassNode(jdtBinding);
    }

    private JDTClassNode checkForExisting(TypeBinding jdtBinding) {
        JDTClassNode node = inProgress.get(jdtBinding);
        if (node == null) {
            node = nodeCache.get(jdtBinding);
        }
        if (node != null) {
            assert Arrays.equals(jdtBinding.readableName(), node.jdtBinding.readableName());
        }
        return node;
    }

    /**
     * Creates a Groovy ClassNode that represents the JDT TypeBinding. Build the basic structure, mark it as 'in progress' and then
     * continue with initialization. This allows self referential generic declarations.
     *
     * @param jdtBinding the JDT binding for which to create a ClassNode
     * @return the new ClassNode, of type JDTClassNode
     */
    private ClassNode createJDTClassNode(TypeBinding jdtBinding) {
        JDTClassNodeBuilder cnb = new JDTClassNodeBuilder(this);
        ClassNode classNode = cnb.configureType(jdtBinding);
        if (classNode instanceof JDTClassNode) {
            final JDTClassNode jdtNode = (JDTClassNode) classNode;
            assert !inProgress.containsKey(jdtBinding);
            inProgress.put(jdtBinding, jdtNode);

            jdtNode.setupGenerics(); // for a BinaryTypeBinding this fixes up those generics

            assert nodeCache.get(jdtBinding) == null : "not unique";
            nodeCache.put(jdtBinding, jdtNode);
            inProgress.remove(jdtBinding);
        }
        return classNode;
    }

    /**
     * Called when a resolvevisitor is commencing resolution for a type - allows us to setup the JDTResolver to point at the right
     * scope for resolutionification. If not able to find a scope, that is a serious problem!
     */
    @Override
    protected boolean commencingResolution() {
        GroovyTypeDeclaration gtDeclaration = scopes.get(currentClass);
        if (gtDeclaration == null) {
            if (resolvedClassNodes.contains(currentClass)) {
                // already resolved!
                return false;
            }
            GroovyEclipseBug geb = new GroovyEclipseBug("commencingResolution failed: no declaration found for class " + currentClass);
            geb.printStackTrace();
            throw geb;
        }
        activeScope = null;
        if (gtDeclaration.scope == null) {
            // The scope may be null if there were errors in the code - let's not freak out the user here
            if (gtDeclaration.hasErrors()) {
                return false;
            }
            GroovyEclipseBug geb = new GroovyEclipseBug("commencingResolution failed: declaration found, but unexpectedly found no scope for " + currentClass.getName());
            geb.printStackTrace();
            throw geb;
        }
        activeScope = (GroovyCompilationUnitScope) gtDeclaration.scope.compilationUnitScope();
        if (DEBUG) {
            log("commencing resolution for " + currentClass.getName());
        }
        return true;
    }

    @Override
    protected void finishedResolution() {
        resolvedClassNodes.add(currentClass);
        scopes.remove(currentClass);
        unresolvables.clear();
    }

    public GroovyCompilationUnitScope getScope() {
        return activeScope;
    }

    /**
     * When recorded, the jdt resolver will be able to (later on) navigate from the classnode back to the JDT scope that should be
     * used.
     */
    public void record(GroovyTypeDeclaration gtDeclaration) {
        // FIXASC can the relationship here from classNode to scope be better preserved to remove the need for this map?
        scopes.put(gtDeclaration.getClassNode(), gtDeclaration);
        if (gtDeclaration.memberTypes != null) {
            TypeDeclaration[] members = gtDeclaration.memberTypes;
            for (int m = 0; m < members.length; m++) {
                record((GroovyTypeDeclaration) members[m]);
            }
        }
        GroovyTypeDeclaration[] anonymousTypes = gtDeclaration.getAnonymousTypes();
        if (anonymousTypes != null) {
            for (int m = 0; m < anonymousTypes.length; m++) {
                record(anonymousTypes[m]);
            }
        }
    }

    public void startResolving(ClassNode node, SourceUnit source) {
        try {
            super.startResolving(node, source);
            unresolvables.clear();
        } catch (AbortResolutionException are) {
            // Can occur if there are other problems with the node (syntax errors) - so don't try resolving it
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
