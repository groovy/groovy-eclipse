/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.control;

import groovy.lang.Tuple2;
import org.apache.groovy.ast.tools.ExpressionUtils;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.SpreadMapExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.ClassNodeResolver.LookupResult;
import org.codehaus.groovy.runtime.memoize.EvictableCache;
import org.codehaus.groovy.runtime.memoize.UnlimitedConcurrentCache;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.trait.Traits;
import groovyjarjarasm.asm.Opcodes;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

import static org.codehaus.groovy.ast.CompileUnit.ConstructedOuterNestedClassNode;
import static org.codehaus.groovy.ast.GenericsType.GenericsTypeName;
import static org.codehaus.groovy.ast.tools.ClosureUtils.getParametersSafe;
import static org.codehaus.groovy.ast.tools.GeneralUtils.inSamePackage;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isDefaultVisibility;

/**
 * Visitor to resolve Types and convert VariableExpression to
 * ClassExpressions if needed. The ResolveVisitor will try to
 * find the Class for a ClassExpression and prints an error if
 * it fails to do so. Constructions like C[], foo as C, (C) foo
 * will force creation of a ClassExpression for C
 * <p>
 * Note: the method to start the resolving is  startResolving(ClassNode, SourceUnit).
 */
public class ResolveVisitor extends ClassCodeExpressionTransformer {
    // note: BigInteger and BigDecimal are also imported by default
    public static final String[] DEFAULT_IMPORTS = {"java.lang.", "java.util.", "java.io.", "java.net.", "groovy.lang.", "groovy.util."};
    private static final String BIGINTEGER_STR = "BigInteger";
    private static final String BIGDECIMAL_STR = "BigDecimal";
    public static final String QUESTION_MARK = "?";
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    // GRECLIPSE private->protected
    protected ClassNode currentClass;
    // GRECLIPSE private->public
    public final CompilationUnit compilationUnit;
    private SourceUnit source;
    private VariableScope currentScope;

    private boolean isTopLevelProperty = true;
    private boolean inPropertyExpression = false;
    private boolean inClosure = false;

    private final Map<ClassNode, ClassNode> possibleOuterClassNodeMap = new HashMap<>();
    private Map<GenericsTypeName, GenericsType> genericParameterNames = new HashMap<>();
    private final Set<FieldNode> fieldTypesChecked = new HashSet<>();
    // GRECLIPSE add
    private final Set<String> resolutionFailedCache = new HashSet<>();
    // GRECLIPSE end
    private boolean checkingVariableTypeInDeclaration;
    // GRECLIPSE private->protected
    protected ImportNode currImportNode;
    private MethodNode currentMethod;
    private ClassNodeResolver classNodeResolver;

    /**
     * A ConstructedNestedClass consists of an outer class and a name part, denoting a
     * nested class with an unknown number of levels down. This allows resolve tests to
     * skip this node for further inner class searches and combinations with imports, since
     * the outer class we know is already resolved.
     */
    private static class ConstructedNestedClass extends ClassNode {
        final ClassNode knownEnclosingType;

        public ConstructedNestedClass(ClassNode outer, String inner) {
            super(outer.getName() + "$" + replacePoints(inner), Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE);
            this.knownEnclosingType = outer;
            this.isPrimaryNode = false;
        }

        // GRECLIPSE add
        public String getUnresolvedName() {
            // outer class (aka knownEnclosingType) may have aliased name that should be reflected here too
            return super.getUnresolvedName().replace(knownEnclosingType.getName(), knownEnclosingType.getUnresolvedName());
        }
        // GRECLIPSE end

        public boolean hasPackageName() {
            if (redirect() != this) return super.hasPackageName();
            return knownEnclosingType.hasPackageName();
        }

        public String setName(String name) {
            if (redirect() != this) {
                return super.setName(name);
            } else {
                throw new GroovyBugError("ConstructedNestedClass#setName should not be called");
            }
        }
    }

    private static String replacePoints(String name) {
        return name.replace('.','$');
    }

    /**
     * we use ConstructedClassWithPackage to limit the resolving the compiler
     * does when combining package names and class names. The idea
     * that if we use a package, then we do not want to replace the
     * '.' with a '$' for the package part, only for the class name
     * part. There is also the case of a imported class, so this logic
     * can't be done in these cases...
     */
    private static class ConstructedClassWithPackage extends ClassNode {
        final String prefix;
        String className;
        public ConstructedClassWithPackage(String pkg, String name) {
            super(pkg+name, Opcodes.ACC_PUBLIC,ClassHelper.OBJECT_TYPE);
            isPrimaryNode = false;
            this.prefix = pkg;
            this.className = name;
        }
        public String getName() {
            if (redirect()!=this) return super.getName();
            return prefix+className;
        }
        public boolean hasPackageName() {
            if (redirect()!=this) return super.hasPackageName();
            return className.indexOf('.')!=-1;
        }
        public String setName(String name) {
            if (redirect()!=this) {
                return super.setName(name);
            } else {
                throw new GroovyBugError("ConstructedClassWithPackage#setName should not be called");
            }
        }
    }

    /**
     * we use LowerCaseClass to limit the resolving the compiler
     * does for vanilla names starting with a lower case letter. The idea
     * that if we use a vanilla name with a lower case letter, that this
     * is in most cases no class. If it is a class the class needs to be
     * imported explicitly. The effect is that in an expression like
     * "def foo = bar" we do not have to use a loadClass call to check the
     * name foo and bar for being classes. Instead we will ask the module
     * for an alias for this name which is much faster.
     */
    private static class LowerCaseClass extends ClassNode {
        final String className;
        public LowerCaseClass(String name) {
            super(name, Opcodes.ACC_PUBLIC,ClassHelper.OBJECT_TYPE);
            isPrimaryNode = false;
            this.className = name;
        }
        public String getName() {
            if (redirect()!=this) return super.getName();
            return className;
        }
        public boolean hasPackageName() {
            if (redirect()!=this) return super.hasPackageName();
            return false;
        }
        public String setName(String name) {
            if (redirect()!=this) {
                return super.setName(name);
            } else {
                throw new GroovyBugError("LowerCaseClass#setName should not be called");
            }
        }
    }

    public ResolveVisitor(CompilationUnit cu) {
        compilationUnit = cu;
        // GRECLIPSE edit -- fix for NPE
        //this.classNodeResolver = new ClassNodeResolver();
        setClassNodeResolver(new ClassNodeResolver() {
            public LookupResult findClassNode(String name, CompilationUnit compilationUnit) {
                return compilationUnit == null ? null : super.findClassNode(name, compilationUnit);
            }
        });
        // GRECLIPSE end
    }

    public void startResolving(ClassNode node, SourceUnit source) {
        this.source = source;
        visitClass(node);
    }

    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        VariableScope oldScope = currentScope;
        currentScope = node.getVariableScope();
        Map<GenericsTypeName, GenericsType> oldPNames = genericParameterNames;
        genericParameterNames = node.isStatic() && !Traits.isTrait(node.getDeclaringClass())
                ? new HashMap<GenericsTypeName, GenericsType>()
                : new HashMap<GenericsTypeName, GenericsType>(genericParameterNames);

        resolveGenericsHeader(node.getGenericsTypes());

        Parameter[] paras = node.getParameters();
        for (Parameter p : paras) {
            p.setInitialExpression(transform(p.getInitialExpression()));
            resolveOrFail(p.getType(), p.getType());
            visitAnnotations(p);
        }
        ClassNode[] exceptions = node.getExceptions();
        for (ClassNode t : exceptions) {
            resolveOrFail(t, node);
        }
        resolveOrFail(node.getReturnType(), node);

        MethodNode oldCurrentMethod = currentMethod;
        currentMethod = node;
        super.visitConstructorOrMethod(node, isConstructor);

        currentMethod = oldCurrentMethod;
        genericParameterNames = oldPNames;
        currentScope = oldScope;
    }

    public void visitField(FieldNode node) {
        ClassNode t = node.getType();
        if(!fieldTypesChecked.contains(node)) {
            resolveOrFail(t, node);
        }
        super.visitField(node);
    }

    public void visitProperty(PropertyNode node) {
        Map<GenericsTypeName, GenericsType> oldPNames = genericParameterNames;
        if (node.isStatic() && !Traits.isTrait(node.getDeclaringClass())) {
            genericParameterNames = new HashMap<GenericsTypeName, GenericsType>();
        }

        ClassNode t = node.getType();
        resolveOrFail(t, node);
        super.visitProperty(node);
        fieldTypesChecked.add(node.getField());

        genericParameterNames = oldPNames;
    }

    // GRECLIPSE private->protected
    protected boolean resolveToInner(ClassNode type) {
        // we do not do our name mangling to find an inner class
        // if the type is a ConstructedClassWithPackage, because in this case we
        // are resolving the name at a different place already
        if (type instanceof ConstructedClassWithPackage) return false;
        if (type instanceof ConstructedNestedClass) return false;
        String name = type.getName();
        String saved = name;
        while (-1 != name.lastIndexOf('.')) {
            name = replaceLastPointWithDollar(name);
            type.setName(name);
            if (resolve(type)) return true;
        }

        if (resolveToNestedOfCurrentClassAndSuperClasses(type)) return true;

        type.setName(saved);
        return false;
    }

    // GRECLIPSE private->protected
    protected boolean resolveToNestedOfCurrentClassAndSuperClasses(ClassNode type) {
        // GROOVY-8531: Fail to resolve type defined in super class written in Java
        for (ClassNode enclosingClassNode = currentClass; ClassHelper.OBJECT_TYPE != enclosingClassNode && null != enclosingClassNode; enclosingClassNode = enclosingClassNode.getSuperClass()) {
            if(resolveToNested(enclosingClassNode, type)) return true;
        }

        return false;
    }

    private boolean resolveToNested(ClassNode enclosingType, ClassNode type) {
        if (type instanceof ConstructedNestedClass) return false;
        // GROOVY-3110: It may be an inner enum defined by this class itself, in which case it does not need to be
        // explicitly qualified by the currentClass name
        String name = type.getName();
        if (enclosingType != type && !name.contains(".") && type.getClass().equals(ClassNode.class)) {
            ClassNode tmp = new ConstructedNestedClass(enclosingType,name);
            if (resolve(tmp)) {
                if (!checkInnerTypeVisibility(enclosingType, tmp)) return false;

                type.setRedirect(tmp);
                return true;
            }
        }

        return false;
    }

    private boolean checkInnerTypeVisibility(ClassNode enclosingType, ClassNode innerClassNode) {
        if (currentClass == enclosingType) {
            return true;
        }

        int modifiers = innerClassNode.getModifiers();
        return Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers);
    }

    private void resolveOrFail(ClassNode type, String msg, ASTNode node) {
        if (resolve(type)) return;
        if (resolveToInner(type)) return;
        if (resolveToOuterNested(type)) return;
        // GRECLIPSE edit
        //addError("unable to resolve class " + type.getName() + " " + msg, node);
        addError("unable to resolve class " + type.toString(false) + msg, type.getEnd() > 0 ? type : node);
        // GRECLIPSE end
    }

    // GROOVY-7812(#1): Static inner classes cannot be accessed from other files when running by 'groovy' command
    // if the type to resolve is an inner class and it is in an outer class which is not resolved,
    // we set the resolved type to a placeholder class node, i.e. a ConstructedOuterNestedClass instance
    // when resolving the outer class later, we set the resolved type of ConstructedOuterNestedClass instance to the actual inner class node(SEE GROOVY-7812(#2))
    private boolean resolveToOuterNested(ClassNode type) {
        CompileUnit compileUnit = currentClass.getCompileUnit();
        // GRECLIPSE add -- fix for NPE in config script
        if (compileUnit == null) return false;
        // GRECLIPSE end
        String typeName = type.getName();

        ModuleNode module = currentClass.getModule();
        for (ImportNode importNode : module.getStaticImports().values()) {
            String importFieldName = importNode.getFieldName();
            String importAlias = importNode.getAlias();

            if (!typeName.equals(importAlias)) continue;

            ConstructedOuterNestedClassNode constructedOuterNestedClassNode = tryToConstructOuterNestedClassNodeViaStaticImport(compileUnit, importNode, importFieldName);
            if (null != constructedOuterNestedClassNode) {
                compileUnit.addClassNodeToResolve(constructedOuterNestedClassNode);
                return true;
            }
        }

        for (Map.Entry<String, ClassNode> entry : compileUnit.getClassesToCompile().entrySet()) {
            ClassNode outerClassNode = entry.getValue();
            ConstructedOuterNestedClassNode constructedOuterNestedClassNode = tryToConstructOuterNestedClassNode(type, outerClassNode);
            if (null != constructedOuterNestedClassNode) {
                compileUnit.addClassNodeToResolve(constructedOuterNestedClassNode);
                return true;
            }
        }

        boolean toResolveFurther = false;
        for (ImportNode importNode : module.getStaticStarImports().values()) {
            ConstructedOuterNestedClassNode constructedOuterNestedClassNode = tryToConstructOuterNestedClassNodeViaStaticImport(compileUnit, importNode, typeName);
            if (null != constructedOuterNestedClassNode) {
                compileUnit.addClassNodeToResolve(constructedOuterNestedClassNode);
                toResolveFurther = true; // do not return here to try all static star imports because currently we do not know which outer class the class to resolve is declared in.
            }
        }

        return toResolveFurther;
    }

    private ConstructedOuterNestedClassNode tryToConstructOuterNestedClassNodeViaStaticImport(CompileUnit compileUnit, ImportNode importNode, String typeName) {
        String importClassName = importNode.getClassName();
        ClassNode outerClassNode = compileUnit.getClass(importClassName);

        if (null == outerClassNode) return null;

        String outerNestedClassName = importClassName + "$" + typeName.replace(".", "$");
        return new ConstructedOuterNestedClassNode(outerClassNode, outerNestedClassName);
    }

    private ConstructedOuterNestedClassNode tryToConstructOuterNestedClassNode(ClassNode type, ClassNode outerClassNode) {
        String outerClassName = outerClassNode.getName();

        for (String typeName = type.getName(), ident = typeName; ident.contains("."); ) {
            ident = ident.substring(0, ident.lastIndexOf("."));
            if (outerClassName.endsWith(ident)) {
                String outerNestedClassName = outerClassName + typeName.substring(ident.length()).replace(".", "$");
                return new ConstructedOuterNestedClassNode(outerClassNode, outerNestedClassName);
            }
        }

        return null;
    }

    private void resolveOrFail(ClassNode type, ASTNode node, boolean prefereImports) {
        resolveGenericsTypes(type.getGenericsTypes());
        if (prefereImports && resolveAliasFromModule(type)) return;
        resolveOrFail(type, node);
    }

    private void resolveOrFail(ClassNode type, ASTNode node) {
        resolveOrFail(type, "", node);
    }

    // GRECLIPSE add
    public ClassNode resolve(String name) {
        return null;
    }
    // GRECLIPSE end

    // GRECLIPSE private->protected
    protected boolean resolve(ClassNode type) {
        return resolve(type, true, true, true);
    }

    // GRECLIPSE private->protected
    protected boolean resolve(ClassNode type, boolean testModuleImports, boolean testDefaultImports, boolean testStaticInnerClasses) {
        resolveGenericsTypes(type.getGenericsTypes());
        if (type.isResolved() || type.isPrimaryClassNode()) return true;
        if (type.isArray()) {
            ClassNode element = type.getComponentType();
            boolean resolved = resolve(element, testModuleImports, testDefaultImports, testStaticInnerClasses);
            if (resolved) {
                ClassNode cn = element.makeArray();
                type.setRedirect(cn);
            }
            return resolved;
        }

        // test if vanilla name is current class name
        if (currentClass == type) return true;

        String typeName = type.getName();

        GenericsType genericsType = genericParameterNames.get(new GenericsTypeName(typeName));
        if (genericsType != null) {
            type.setRedirect(genericsType.getType());
            type.setGenericsTypes(new GenericsType[]{ genericsType });
            type.setGenericsPlaceHolder(true);
            return true;
        }

        if (currentClass.getNameWithoutPackage().equals(typeName)) {
            type.setRedirect(currentClass);
            return true;
        }

        return resolveNestedClass(type) ||
                resolveFromModule(type, testModuleImports) ||
                resolveFromCompileUnit(type) ||
                resolveFromDefaultImports(type, testDefaultImports) ||
                resolveFromStaticInnerClasses(type, testStaticInnerClasses) ||
                resolveToOuter(type);
    }

    private boolean resolveNestedClass(ClassNode type) {
        if (type instanceof ConstructedNestedClass || type instanceof ConstructedClassWithPackage) return false;
        // we have for example a class name A, are in class X
        // and there is a nested class A$X. we want to be able 
        // to access that class directly, so A becomes a valid
        // name in X.
        // GROOVY-4043: Do this check up the hierarchy, if needed
        Map<String, ClassNode> hierClasses = new LinkedHashMap<String, ClassNode>();
        for(ClassNode classToCheck = currentClass; classToCheck != ClassHelper.OBJECT_TYPE;
            classToCheck = classToCheck.getSuperClass()) {
            if(classToCheck == null || hierClasses.containsKey(classToCheck.getName())) break;
            hierClasses.put(classToCheck.getName(), classToCheck);
        }

        for (ClassNode classToCheck : hierClasses.values()) {
            if (setRedirect(type, classToCheck)) return true;
        }

        // GROOVY-8947: Fail to resolve non-static inner class outside of outer class
        ClassNode possibleOuterClassNode = possibleOuterClassNodeMap.get(type);
        if (null != possibleOuterClassNode) {
            if (setRedirect(type, possibleOuterClassNode)) return true;
        }

        // another case we want to check here is if we are in a
        // nested class A$B$C and want to access B without
        // qualifying it by A.B. A alone will work, since that
        // is the qualified (minus package) name of that class
        // anyway. 
        
        // That means if the current class is not an InnerClassNode
        // there is nothing to be done.
        if (!(currentClass instanceof InnerClassNode)) return false;
        
        // since we have B and want to get A we start with the most 
        // outer class, put them together and then see if that does
        // already exist. In case of B from within A$B we are done 
        // after the first step already. In case of for example
        // A.B.C.D.E.F and accessing E from F we test A$E=failed, 
        // A$B$E=failed, A$B$C$E=fail, A$B$C$D$E=success
        
        LinkedList<ClassNode> outerClasses = new LinkedList<ClassNode>();
        ClassNode outer = currentClass.getOuterClass();
        while (outer!=null) {
            outerClasses.addFirst(outer);
            outer = outer.getOuterClass();
        }
        // most outer class is now element 0
        /* GRECLIPSE edit -- unroll setRedirect and insert resolution cache checks
        for (ClassNode testNode : outerClasses) {
            if (setRedirect(type, testNode)) return true;
        }
        */
        if (type.getName().indexOf('.') < 0) {
            for (ClassNode classToCheck : outerClasses) {
                ClassNode val = new ConstructedNestedClass(classToCheck, type.getName());
                if (!resolutionFailedCache.contains(val.getName())) {
                    if (resolveFromCompileUnit(val)) {
                        type.setRedirect(val);
                        return true;
                    }
                }
                resolutionFailedCache.add(val.getName());

                // also check interfaces in case we have interfaces with nested classes
                for (ClassNode next : classToCheck.getAllInterfaces()) {
                    if (type.getName().contains(next.getName())) continue;
                    val = new ConstructedNestedClass(next, type.getName());
                    if (!resolutionFailedCache.contains(val.getName())) {
                        if (resolve(val, false, false, false)) {
                            type.setRedirect(val);
                            return true;
                        }
                    }
                    resolutionFailedCache.add(val.getName());
                }
            }
        }
        // GRECLIPSE end

        return false;
    }

    private boolean setRedirect(ClassNode type, ClassNode classToCheck) {
        ClassNode val = new ConstructedNestedClass(classToCheck, type.getName());
        // GRECLIPSE add
        String qualName = type.getName();
        int dotIndex = qualName.indexOf('.'), dollarIndex = qualName.indexOf('$');
        String firstComponent = (dotIndex == -1 && dollarIndex == -1 ? qualName : (dotIndex == -1 ? qualName.substring(0, dollarIndex) : qualName.substring(0, dotIndex)));
        if (existsAsInnerClass(classToCheck::getInnerClasses, classToCheck.getName() + '$' + firstComponent)) {
        // GRECLIPSE end
        if (resolveFromCompileUnit(val)) {
            type.setRedirect(val);
            return true;
        }
        // GRECLIPSE add
        }
        // GRECLIPSE end
        // also check interfaces in case we have interfaces with nested classes
        for (ClassNode next : classToCheck.getAllInterfaces()) {
            if (type.getName().contains(next.getName())) continue;
            val = new ConstructedNestedClass(next, type.getName());
            if (resolve(val, false, false, false)) {
                type.setRedirect(val);
                return true;
            }
        }
        return false;
    }

    // GRECLIPSE add
    private boolean existsAsInnerClass(Iterable<InnerClassNode> innerClasses, String name) {
        return StreamSupport.stream(innerClasses.spliterator(), false)
            .anyMatch(innerClass -> name.equals(innerClass.getName()));
    }
    // GRECLIPSE end

    private static String replaceLastPointWithDollar(String name) {
        int lastPointIndex = name.lastIndexOf('.');

        return name.substring(0, lastPointIndex) + "$" + name.substring(lastPointIndex + 1);
    }

    // GRECLIPSE private->protected
    protected boolean resolveFromStaticInnerClasses(ClassNode type, boolean testStaticInnerClasses) {
        if (type instanceof ConstructedNestedClass) return false;

        // a class consisting of a vanilla name can never be
        // a static inner class, because at least one dot is
        // required for this. Example: foo.bar -> foo$bar
        if (type instanceof LowerCaseClass) return false;

        // try to resolve a public static inner class' name
        testStaticInnerClasses &= type.hasPackageName();
        if (testStaticInnerClasses) {
            if (type instanceof ConstructedClassWithPackage) {
                // we replace '.' only in the className part
                // with '$' to find an inner class. The case that
                // the package is really a class is handled elsewhere
                ConstructedClassWithPackage tmp = (ConstructedClassWithPackage) type;
                String savedName = tmp.className;
                tmp.className = replaceLastPointWithDollar(savedName);
                if (resolve(tmp, false, true, true)) {
                    type.setRedirect(tmp.redirect());
                    return true;
                }
                tmp.className = savedName;
            } else {
                String savedName = type.getName();
                String replacedPointType = replaceLastPointWithDollar(savedName);
                type.setName(replacedPointType);
                if (resolve(type, false, true, true)) return true;
                type.setName(savedName);
            }
        }
        return false;
    }

    // GRECLIPSE private->protected
    protected boolean resolveFromDefaultImports(ClassNode type, boolean testDefaultImports) {
        // test default imports
        testDefaultImports &= !type.hasPackageName();
        // we do not resolve a vanilla name starting with a lower case letter
        // try to resolve against a default import, because we know that the
        // default packages do not contain classes like these
        testDefaultImports &= !(type instanceof LowerCaseClass);

        if (testDefaultImports) {
            if (resolveFromDefaultImports(type)) return true;

            final String typeName = type.getName();
            if (BIGINTEGER_STR.equals(typeName)) {
                type.setRedirect(ClassHelper.BigInteger_TYPE);
                return true;
            } else if (BIGDECIMAL_STR.equals(typeName)) {
                type.setRedirect(ClassHelper.BigDecimal_TYPE);
                return true;
            }
        }
        return false;
    }

    private boolean resolveFromDefaultImports(ClassNode type) {
        final String typeName = type.getName();

        Set<String> packagePrefixSet = DEFAULT_IMPORT_CLASS_AND_PACKAGES_CACHE.get(typeName);
        if (null != packagePrefixSet) {
            // if the type name was resolved before, we can try the successfully resolved packages first, which are much less and very likely successful to resolve.
            // As a result, we can avoid trying other default import packages and further resolving, which can improve the resolving performance to some extent.
            if (resolveFromDefaultImports(type, packagePrefixSet.toArray(EMPTY_STRING_ARRAY))) {
                return true;
            }
        }

        if (resolveFromDefaultImports(type, DEFAULT_IMPORTS)) {
            return true;
        }
        return false;
    }

    private static final EvictableCache<String, Set<String>> DEFAULT_IMPORT_CLASS_AND_PACKAGES_CACHE = new UnlimitedConcurrentCache<>();

    private boolean resolveFromDefaultImports(final ClassNode type, final String[] packagePrefixes) {
        final String typeName = type.getName();

        for (String packagePrefix : packagePrefixes) {
            // We limit the inner class lookups here by using ConstructedClassWithPackage.
            // This way only the name will change, the packagePrefix will
            // not be included in the lookup. The case where the
            // packagePrefix is really a class is handled elsewhere.
            // WARNING: This code does not expect a class that has a static
            //          inner class in DEFAULT_IMPORTS
            ConstructedClassWithPackage tmp = new ConstructedClassWithPackage(packagePrefix, typeName);
            // GRECLIPSE add
            if (resolutionFailedCache.contains(tmp.getName())) continue;
            // GRECLIPSE end
            if (resolve(tmp, false, false, false)) {
                type.setRedirect(tmp.redirect());

                if (DEFAULT_IMPORTS == packagePrefixes) { // Only the non-cached type and packages should be cached
                    Set<String> packagePrefixSet = DEFAULT_IMPORT_CLASS_AND_PACKAGES_CACHE.getAndPut(typeName, key -> new HashSet<>(2));
                    packagePrefixSet.add(packagePrefix);
                }

                return true;
            }
            // GRECLIPSE add
            resolutionFailedCache.add(tmp.getName());
            // GRECLIPSE end
        }

        return false;
    }

    // GRECLIPSE private->protected
    protected boolean resolveFromCompileUnit(ClassNode type) {
        // look into the compile unit if there is a class with that name
        CompileUnit compileUnit = currentClass.getCompileUnit();
        if (compileUnit == null) return false;
        ClassNode cuClass = compileUnit.getClass(type.getName());
        if (cuClass != null) {
            if (type != cuClass) type.setRedirect(cuClass);
            return true;
        }
        return false;
    }

    private void ambiguousClass(ClassNode type, ClassNode iType, String name) {
        if (type.getName().equals(iType.getName())) {
            addError("reference to " + name + " is ambiguous, both class " + type.getName() + " and " + iType.getName() + " match", type);
        } else {
            type.setRedirect(iType);
        }
    }

    private boolean resolveAliasFromModule(ClassNode type) {
        // In case of getting a ConstructedClassWithPackage here we do not do checks for partial
        // matches with imported classes. The ConstructedClassWithPackage is already a constructed
        // node and any subclass resolving will then take place elsewhere
        if (type instanceof ConstructedClassWithPackage) return false;

        ModuleNode module = currentClass.getModule();
        if (module == null) return false;
        String name = type.getName();

        // check module node imports aliases
        // the while loop enables a check for inner classes which are not fully imported,
        // but visible as the surrounding class is imported and the inner class is public/protected static
        String pname = name;
        int index = name.length();
        /*
         * we have a name foo.bar and an import foo.foo. This means foo.bar is possibly
         * foo.foo.bar rather than foo.bar. This means to cut at the dot in foo.bar and
         * foo for import
         */
        do {
            pname = name.substring(0, index);
            ClassNode aliasedNode = null;
            ImportNode importNode = module.getImport(pname);
            if (importNode != null && importNode != currImportNode) {
                aliasedNode = importNode.getType();
            }
            if (aliasedNode == null) {
                importNode = module.getStaticImports().get(pname);
                if (importNode != null && importNode != currImportNode) {
                    // static alias only for inner classes and must be at end of chain
                    ClassNode tmp = new ConstructedNestedClass(importNode.getType(), importNode.getFieldName());
                    if (resolve(tmp, false, false, true)) {
                        if ((tmp.getModifiers() & Opcodes.ACC_STATIC) != 0) {
                            type.setRedirect(tmp.redirect());
                            return true;
                        }
                    }
                }
            }

            if (aliasedNode != null) {
                if (pname.length() == name.length()) {
                    // full match

                    // We can compare here by length, because pname is always
                    // a substring of name, so same length means they are equal.
                    type.setRedirect(aliasedNode);
                    return true;
                } else {
                    //partial match

                    // At this point we know that we have a match for pname. This may
                    // mean, that name[pname.length()..<-1] is a static inner class.
                    // For this the rest of the name does not need any dots in its name.
                    // It is either completely a inner static class or it is not.
                    // Since we do not want to have useless lookups we create the name
                    // completely and use a ConstructedClassWithPackage to prevent lookups against the package.
                    String className = aliasedNode.getNameWithoutPackage() + '$' +
                            name.substring(pname.length() + 1).replace('.', '$');
                    ConstructedClassWithPackage tmp = new ConstructedClassWithPackage(aliasedNode.getPackageName() + ".", className);
                    if (resolve(tmp, true, true, false)) {
                        type.setRedirect(tmp.redirect());
                        return true;
                    }
                }
            }
            index = pname.lastIndexOf('.');
        } while (index != -1);
        return false;
    }

    // GRECLIPSE private->protected
    protected boolean resolveFromModule(ClassNode type, boolean testModuleImports) {
        if (type instanceof ConstructedNestedClass) return false;

        // we decided if we have a vanilla name starting with a lower case
        // letter that we will not try to resolve this name against .*
        // imports. Instead a full import is needed for these.
        // resolveAliasFromModule will do this check for us. This method
        // does also check the module contains a class in the same package
        // of this name. This check is not done for vanilla names starting
        // with a lower case letter anymore
        if (type instanceof LowerCaseClass) {
            return resolveAliasFromModule(type);
        }

        String name = type.getName();
        ModuleNode module = currentClass.getModule();
        if (module == null) return false;

        boolean newNameUsed = false;
        // we add a package if there is none yet and the module has one. But we
        // do not add that if the type is a ConstructedClassWithPackage. The code in ConstructedClassWithPackage
        // hasPackageName() will return true if ConstructedClassWithPackage#className has no dots.
        // but since the prefix may have them and the code there does ignore that
        // fact. We check here for ConstructedClassWithPackage.
        if (!type.hasPackageName() && module.hasPackageName() && !(type instanceof ConstructedClassWithPackage)) {
            type.setName(module.getPackageName() + name);
            newNameUsed = true;
        }
        // look into the module node if there is a class with that name
        List<ClassNode> moduleClasses = module.getClasses();
        for (ClassNode mClass : moduleClasses) {
            if (mClass.getName().equals(type.getName())) {
                if (mClass != type) type.setRedirect(mClass);
                return true;
            }
        }
        if (newNameUsed) type.setName(name);

        if (testModuleImports) {
            if (resolveAliasFromModule(type)) return true;

            if (module.hasPackageName()) {
                // check package this class is defined in. The usage of ConstructedClassWithPackage here
                // means, that the module package will not be involved when the
                // compiler tries to find an inner class.
                ConstructedClassWithPackage tmp =  new ConstructedClassWithPackage(module.getPackageName(), name);
                // GRECLIPSE add
                if (!resolutionFailedCache.contains(tmp.getName())) {
                // GRECLIPSE end
                if (resolve(tmp, false, false, false)) {
                    ambiguousClass(type, tmp, name);
                    type.setRedirect(tmp.redirect());
                    return true;
                }
                // GRECLIPSE add
                resolutionFailedCache.add(tmp.getName());
                }
                // GRECLIPSE end
            }

            // check module static imports (for static inner classes)
            for (ImportNode importNode : module.getStaticImports().values()) {
                if (importNode.getFieldName().equals(name)) {
                    ClassNode tmp = new ConstructedNestedClass(importNode.getType(), name);
                    // GRECLIPSE add
                    if (resolutionFailedCache.contains(tmp.getName())) continue;
                    // GRECLIPSE end
                    if (resolve(tmp, false, false, true)) {
                        if ((tmp.getModifiers() & Opcodes.ACC_STATIC) != 0) {
                            type.setRedirect(tmp.redirect());
                            return true;
                        }
                    }
                    // GRECLIPSE add
                    resolutionFailedCache.add(tmp.getName());
                    // GRECLIPSE end
                }
            }

            // check module node import packages
            for (ImportNode importNode : module.getStarImports()) {
                String packagePrefix = importNode.getPackageName();
                // We limit the inner class lookups here by using ConstructedClassWithPackage.
                // This way only the name will change, the packagePrefix will
                // not be included in the lookup. The case where the
                // packagePrefix is really a class is handled elsewhere.
                ConstructedClassWithPackage tmp = new ConstructedClassWithPackage(packagePrefix, name);
                // GRECLIPSE add
                if (resolutionFailedCache.contains(tmp.getName())) continue;
                // GRECLIPSE end
                if (resolve(tmp, false, false, true)) {
                    ambiguousClass(type, tmp, name);
                    type.setRedirect(tmp.redirect());
                    return true;
                }
                // GRECLIPSE add
                resolutionFailedCache.add(tmp.getName());
                // GRECLIPSE end
            }

            // check for star imports (import static pkg.Outer.*) matching static inner classes
            for (ImportNode importNode : module.getStaticStarImports().values()) {
                ClassNode tmp = new ConstructedNestedClass(importNode.getType(), name);
                // GRECLIPSE add
                if (resolutionFailedCache.contains(tmp.getName())) continue;
                // GRECLIPSE end
                if (resolve(tmp, false, false, true)) {
                    if ((tmp.getModifiers() & Opcodes.ACC_STATIC) != 0) {
                        ambiguousClass(type, tmp, name);
                        type.setRedirect(tmp.redirect());
                        return true;
                    }
                }
                // GRECLIPSE add
                resolutionFailedCache.add(tmp.getName());
                // GRECLIPSE end
            }
        }
        return false;
    }

    // GRECLIPSE private->protected
    protected boolean resolveToOuter(ClassNode type) {
        String name = type.getName();

        // We do not need to check instances of LowerCaseClass
        // to be a Class, because unless there was an import for
        // for this we do not lookup these cases. This was a decision
        // made on the mailing list. To ensure we will not visit this
        // method again we set a NO_CLASS for this name
        if (type instanceof LowerCaseClass) {
            classNodeResolver.cacheClass(name, ClassNodeResolver.NO_CLASS);
            return false;
        }

        if (currentClass.getModule().hasPackageName() && name.indexOf('.') == -1) return false;

        LookupResult lr = classNodeResolver.resolveName(name, compilationUnit);
        if (lr != null) {
            if (lr.isSourceUnit()) {
                SourceUnit su = lr.getSourceUnit();
                currentClass.getCompileUnit().addClassNodeToCompile(type, su);
            } else {
                type.setRedirect(lr.getClassNode());
            }
            return true;
        }
        return false;
    }

    public Expression transform(Expression exp) {
        if (exp == null) return null;
        Expression ret = null;
        if (exp instanceof VariableExpression) {
            ret = transformVariableExpression((VariableExpression) exp);
        } else if (exp.getClass() == PropertyExpression.class) {
            ret = transformPropertyExpression((PropertyExpression) exp);
        } else if (exp instanceof DeclarationExpression) {
            ret = transformDeclarationExpression((DeclarationExpression) exp);
        } else if (/* GRECLIPSE avoid transforming CompareIdentity and CompareToNull: exp instanceof BinaryExpression*/exp.getClass() == BinaryExpression.class) {
            ret = transformBinaryExpression((BinaryExpression) exp);
        } else if (exp instanceof MethodCallExpression) {
            ret = transformMethodCallExpression((MethodCallExpression) exp);
        } else if (exp instanceof ClosureExpression) {
            ret = transformClosureExpression((ClosureExpression) exp);
        } else if (exp instanceof ConstructorCallExpression) {
            ret = transformConstructorCallExpression((ConstructorCallExpression) exp);
        } else if (exp instanceof AnnotationConstantExpression) {
            ret = transformAnnotationConstantExpression((AnnotationConstantExpression) exp);
        } else {
            resolveOrFail(exp.getType(), exp);
            ret = exp.transformExpression(this);
        }
        if (ret!=null && ret!=exp) ret.setSourcePosition(exp);
        return ret;
    }

    private static String lookupClassName(PropertyExpression pe) {
        boolean doInitialClassTest=true;
        StringBuilder name = new StringBuilder(32);
        // this loop builds a name from right to left each name part
        // separated by "."
        for (Expression it = pe; it != null; it = ((PropertyExpression) it).getObjectExpression()) {
            if (it instanceof VariableExpression) {
                VariableExpression ve = (VariableExpression) it;
                // stop at super and this
                if (ve.isSuperExpression() || ve.isThisExpression()) {
                    return null;
                }
                String varName = ve.getName();
                Tuple2<StringBuilder, Boolean> classNameInfo = makeClassName(doInitialClassTest, name, varName);
                name = classNameInfo.getFirst();
                doInitialClassTest = classNameInfo.getSecond();

                break;
            }
            // anything other than PropertyExpressions or
            // VariableExpressions will stop resolving
            else if (it.getClass() != PropertyExpression.class) {
                return null;
            } else {
                PropertyExpression current = (PropertyExpression) it;
                String propertyPart = current.getPropertyAsString();
                // the class property stops resolving, dynamic property names too
                if (propertyPart == null || propertyPart.equals("class")) {
                    return null;
                }
                Tuple2<StringBuilder, Boolean> classNameInfo = makeClassName(doInitialClassTest, name, propertyPart);
                name = classNameInfo.getFirst();
                doInitialClassTest = classNameInfo.getSecond();
            }
        }

        if (null == name || name.length() == 0) return null;

        return name.toString();
    }

    private static Tuple2<StringBuilder, Boolean> makeClassName(boolean doInitialClassTest, StringBuilder name, String varName) {
        if (doInitialClassTest) {
            // we are at the first name part. This is the right most part.
            // If this part is in lower case, then we do not need a class
            // check. other parts of the property expression will be tested
            // by a different method call to this method, so foo.Bar.bar
            // can still be resolved to the class foo.Bar and the static
            // field bar.
            if (!testVanillaNameForClass(varName)) {
                name = null;
            } else {
                doInitialClassTest = false;
                name = new StringBuilder(varName);
            }
        } else {
            name.insert(0, varName + ".");
        }

        return new Tuple2<StringBuilder, Boolean>(name, doInitialClassTest);
    }

    // iterate from the inner most to the outer and check for classes
    // this check will ignore a .class property, for Example Integer.class will be
    // a PropertyExpression with the ClassExpression of Integer as objectExpression
    // and class as property
    private static Expression correctClassClassChain(PropertyExpression pe) {
        LinkedList<Expression> stack = new LinkedList<Expression>();
        ClassExpression found = null;
        for (Expression it = pe; it != null; it = ((PropertyExpression) it).getObjectExpression()) {
            if (it instanceof ClassExpression) {
                found = (ClassExpression) it;
                break;
            } else if (!(it.getClass() == PropertyExpression.class)) {
                return pe;
            }
            stack.addFirst(it);
        }
        if (found == null) return pe;

        if (stack.isEmpty()) return pe;
        Object stackElement = stack.removeFirst();
        if (!(stackElement.getClass() == PropertyExpression.class)) return pe;
        PropertyExpression classPropertyExpression = (PropertyExpression) stackElement;
        String propertyNamePart = classPropertyExpression.getPropertyAsString();
        if (propertyNamePart == null || !propertyNamePart.equals("class")) return pe;

        found.setSourcePosition(classPropertyExpression);
        if (stack.isEmpty()) return found;
        stackElement = stack.removeFirst();
        if (!(stackElement.getClass() == PropertyExpression.class)) return pe;
        PropertyExpression classPropertyExpressionContainer = (PropertyExpression) stackElement;

        classPropertyExpressionContainer.setObjectExpression(found);
        return pe;
    }

    protected Expression transformPropertyExpression(PropertyExpression pe) {
        boolean itlp = isTopLevelProperty;
        boolean ipe = inPropertyExpression;

        Expression objectExpression = pe.getObjectExpression();
        inPropertyExpression = true;
        isTopLevelProperty = (objectExpression.getClass() != PropertyExpression.class);
        objectExpression = transform(objectExpression);
        // we handle the property part as if it were not part of the property
        inPropertyExpression = false;
        Expression property = transform(pe.getProperty());
        isTopLevelProperty = itlp;
        inPropertyExpression = ipe;

        boolean spreadSafe = pe.isSpreadSafe();
        PropertyExpression old = pe;
        pe = new PropertyExpression(objectExpression, property, pe.isSafe());
        pe.setSpreadSafe(spreadSafe);
        pe.setSourcePosition(old);

        String className = lookupClassName(pe);
        if (className != null) {
            ClassNode type = ClassHelper.make(className);
            if (resolve(type)) {
                Expression ret =  new ClassExpression(type);
                ret.setSourcePosition(pe);
                return ret;
            }
        }
        if (objectExpression instanceof ClassExpression && pe.getPropertyAsString() != null) {
            // possibly an inner class (or inherited inner class)
            ClassExpression ce = (ClassExpression) objectExpression;
            ClassNode classNode = ce.getType();
            while (classNode != null) {
                ClassNode type = new ConstructedNestedClass(classNode, pe.getPropertyAsString());
                if (resolve(type, false, false, false)) {
                    if (classNode == ce.getType() || isVisibleNestedClass(type, ce.getType())) {
                        Expression ret = new ClassExpression(type);
                        ret.setSourcePosition(pe); // GRECLIPSE ce->pe
                        return ret;
                    }
                }
                classNode = classNode.getSuperClass();
            }
        }
        Expression ret = pe;
        checkThisAndSuperAsPropertyAccess(pe);
        if (isTopLevelProperty) ret = correctClassClassChain(pe);
        return ret;
    }

    private boolean isVisibleNestedClass(ClassNode type, ClassNode ceType) {
        if (!type.isRedirectNode()) return false;
        ClassNode redirect = type.redirect();
        if (Modifier.isPublic(redirect.getModifiers()) || Modifier.isProtected(redirect.getModifiers())) return true;
        // package local
        return isDefaultVisibility(redirect.getModifiers()) && inSamePackage(ceType, redirect);
    }

    private boolean directlyImplementsTrait(ClassNode trait) {
        ClassNode[] interfaces = currentClass.getInterfaces();
        if (interfaces==null) {
            return currentClass.getSuperClass().equals(trait);
        }
        for (ClassNode node : interfaces) {
            if (node.equals(trait)) {
                return true;
            }
        }
        return currentClass.getSuperClass().equals(trait);
    }

    private void checkThisAndSuperAsPropertyAccess(PropertyExpression expression) {
        if (expression.isImplicitThis()) return;
        String prop = expression.getPropertyAsString();
        if (prop == null) return;
        if (!prop.equals("this") && !prop.equals("super")) return;

        ClassNode type = expression.getObjectExpression().getType();
        if (expression.getObjectExpression() instanceof ClassExpression) {
            if (!(currentClass instanceof InnerClassNode) && !Traits.isTrait(type)) {
                addError("The usage of 'Class.this' and 'Class.super' is only allowed in nested/inner classes.", expression);
                return;
            }
            if (currentScope!=null && !currentScope.isInStaticContext() && Traits.isTrait(type) && "super".equals(prop) && directlyImplementsTrait(type)) {
                return;
            }
            ClassNode iterType = currentClass;
            while (iterType != null) {
                if (iterType.equals(type)) break;
                iterType = iterType.getOuterClass();
            }
            if (iterType == null) {
                addError("The class '" + type.getName() + "' needs to be an outer class of '" +
                        currentClass.getName() + "' when using '.this' or '.super'.", expression);
            }
            if ((currentClass.getModifiers() & Opcodes.ACC_STATIC) == 0) return;
            if (currentScope != null && !currentScope.isInStaticContext()) return;
            addError("The usage of 'Class.this' and 'Class.super' within static nested class '" +
                    currentClass.getName() + "' is not allowed in a static context.", expression);
        }
    }

    protected Expression transformVariableExpression(VariableExpression ve) {
        visitAnnotations(ve);
        Variable v = ve.getAccessedVariable();
        
        if(!(v instanceof DynamicVariable) && !checkingVariableTypeInDeclaration) {
            /*
             *  GROOVY-4009: when a normal variable is simply being used, there is no need to try to 
             *  resolve its type. Variable type resolve should proceed only if the variable is being declared. 
             */
            return ve;
        }
        if (v instanceof DynamicVariable){
            String name = ve.getName();
            ClassNode t = ClassHelper.make(name);
            // asking isResolved here allows to check if a primitive
            // type name like "int" was used to make t. In such a case
            // we have nothing left to do.
            boolean isClass = t.isResolved();
            if (!isClass) {
                // It was no primitive type, so next we see if the name,
                // which is a vanilla name, starts with a lower case letter.
                // In that case we change it to a LowerCaseClass to let the
                // compiler skip the resolving at several places in this class.
                if (Character.isLowerCase(name.charAt(0))) {
                  t = new LowerCaseClass(name);
                }
                isClass = resolve(t);
                if(!isClass) {
                    isClass = resolveToNestedOfCurrentClassAndSuperClasses(t);
                }
            }
            if (isClass) {
                // the name is a type so remove it from the scoping
                // as it is only a classvariable, it is only in
                // referencedClassVariables, but must be removed
                // for each parentscope too
                for (VariableScope scope = currentScope; scope != null && !scope.isRoot(); scope = scope.getParent()) {
                    if (scope.removeReferencedClassVariable(ve.getName()) == null) break;
                }
                ClassExpression ce = new ClassExpression(t);
                ce.setSourcePosition(ve);
                return ce;
            }
        }
        resolveOrFail(ve.getType(), ve);
        ClassNode origin = ve.getOriginType();
        if (origin!=ve.getType()) resolveOrFail(origin, ve);
        return ve;
    }

    private static boolean testVanillaNameForClass(String name) {
        if (name==null || name.length()==0) return false;
        return !Character.isLowerCase(name.charAt(0));
    }

    private static boolean isLeftSquareBracket(int op) {
        return op == Types.ARRAY_EXPRESSION
                || op == Types.LEFT_SQUARE_BRACKET
                || op == Types.SYNTH_LIST
                || op == Types.SYNTH_MAP;
    }

    protected Expression transformBinaryExpression(BinaryExpression be) {
        Expression left = transform(be.getLeftExpression());
        int type = be.getOperation().getType();
        if ((type == Types.ASSIGNMENT_OPERATOR || type == Types.EQUAL) &&
                left instanceof ClassExpression) {
            ClassExpression ce = (ClassExpression) left;
            String error = "you tried to assign a value to the class '" + ce.getType().getName() + "'";
            if (ce.getType().isScript()) {
                error += ". Do you have a script with this name?";
            }
            addError(error, be.getLeftExpression());
            return be;
        }
        if (left instanceof ClassExpression && isLeftSquareBracket(type)) {
            if (be.getRightExpression() instanceof ListExpression) {
                ListExpression list = (ListExpression) be.getRightExpression();
                if (list.getExpressions().isEmpty()) {
                    // we have C[] if the list is empty -> should be an array then!
                    final ClassExpression ce = new ClassExpression(left.getType().makeArray());
                    ce.setSourcePosition(be);
                    return ce;
                }
                else {
                    // maybe we have C[k1:v1, k2:v2] -> should become (C)([k1:v1, k2:v2])
                    boolean map = true;
                    for (Expression expression : list.getExpressions()) {
                        if(!(expression instanceof MapEntryExpression)) {
                            map = false;
                            break;
                        }
                    }

                    if (map) {
                        final MapExpression me = new MapExpression();
                        for (Expression expression : list.getExpressions()) {
                            me.addMapEntryExpression((MapEntryExpression) transform(expression));
                        }
                        me.setSourcePosition(list);
                        final CastExpression ce = new CastExpression(left.getType(), me);
                        ce.setCoerce(true);
                        ce.setSourcePosition(be);
                        return ce;
                    }
                }
            } else if (be.getRightExpression() instanceof SpreadMapExpression) {
                // we have C[*:map] -> should become (C) map
                SpreadMapExpression mapExpression = (SpreadMapExpression) be.getRightExpression();
                Expression right = transform(mapExpression.getExpression());
                CastExpression ce = new CastExpression(left.getType(), right);
                ce.setCoerce(true);
                ce.setSourcePosition(be);
                return ce;
            }

            if (be.getRightExpression() instanceof MapEntryExpression) {
                // may be we have C[k1:v1] -> should become (C)([k1:v1])
                final MapExpression me = new MapExpression();
                me.addMapEntryExpression((MapEntryExpression) transform(be.getRightExpression()));
                me.setSourcePosition(be.getRightExpression());
                final CastExpression ce = new CastExpression(left.getType(), me);
                ce.setSourcePosition(be);
                return ce;
            }
        }
        Expression right = transform(be.getRightExpression());
        be.setLeftExpression(left);
        be.setRightExpression(right);
        return be;
    }

    protected Expression transformClosureExpression(ClosureExpression ce) {
        boolean oldInClosure = inClosure;
        inClosure = true;
        for (Parameter para : getParametersSafe(ce)) {
            ClassNode t = para.getType();
            resolveOrFail(t, ce);
            visitAnnotations(para);
            if (para.hasInitialExpression()) {
                para.setInitialExpression(transform(para.getInitialExpression()));
            }
            visitAnnotations(para);
        }

        Statement code = ce.getCode();
        if (code != null) code.visit(this);
        inClosure = oldInClosure;
        return ce;
    }

    protected Expression transformConstructorCallExpression(ConstructorCallExpression cce) {
        findPossibleOuterClassNodeForNonStaticInnerClassInstantiation(cce);

        ClassNode type = cce.getType();
        resolveOrFail(type, cce);
        if (Modifier.isAbstract(type.getModifiers())) {
            addError("You cannot create an instance from the abstract " + getDescription(type) + ".", cce);
        }

        return cce.transformExpression(this);
    }

    private void findPossibleOuterClassNodeForNonStaticInnerClassInstantiation(ConstructorCallExpression cce) {
        // GROOVY-8947: Fail to resolve non-static inner class outside of outer class
        // `new Computer().new Cpu(4)` will be parsed to `new Cpu(new Computer(), 4)`
        // so non-static inner class instantiation expression's first argument is a constructor call of outer class
        // but the first argument is constructor call can not be non-static inner class instantiation expression, e.g.
        // `new HashSet(new ArrayList())`, so we add "possible" to the variable name
        Expression argumentExpression = cce.getArguments();
        if (argumentExpression instanceof ArgumentListExpression) {
            ArgumentListExpression argumentListExpression = (ArgumentListExpression) argumentExpression;
            List<Expression> expressionList = argumentListExpression.getExpressions();
            if (!expressionList.isEmpty()) {
                Expression firstExpression = expressionList.get(0);

                if (firstExpression instanceof ConstructorCallExpression) {
                    ConstructorCallExpression constructorCallExpression = (ConstructorCallExpression) firstExpression;
                    ClassNode possibleOuterClassNode = constructorCallExpression.getType();
                    possibleOuterClassNodeMap.put(cce.getType(), possibleOuterClassNode);
                }
            }
        }
    }

    private static String getDescription(ClassNode node) {
        return (node.isInterface() ? "interface" : "class") + " '" + node.getName() + "'";
    }

    protected Expression transformMethodCallExpression(MethodCallExpression mce) {
        Expression args = transform(mce.getArguments());
        Expression method = transform(mce.getMethod());
        Expression object = transform(mce.getObjectExpression());

        resolveGenericsTypes(mce.getGenericsTypes());
        
        MethodCallExpression result = new MethodCallExpression(object, method, args);
        result.setSafe(mce.isSafe());
        result.setImplicitThis(mce.isImplicitThis());
        result.setSpreadSafe(mce.isSpreadSafe());
        result.setSourcePosition(mce);
        result.setGenericsTypes(mce.getGenericsTypes());
        result.setMethodTarget(mce.getMethodTarget());
        return result;
    }

    protected Expression transformDeclarationExpression(DeclarationExpression de) {
        visitAnnotations(de);
        Expression oldLeft = de.getLeftExpression();
        checkingVariableTypeInDeclaration = true;
        Expression left = transform(oldLeft);
        checkingVariableTypeInDeclaration = false;
        if (left instanceof ClassExpression) {
            ClassExpression ce = (ClassExpression) left;
            addError("you tried to assign a value to the class " + ce.getType().getName(), oldLeft);
            return de;
        }
        Expression right = transform(de.getRightExpression());
        if (right == de.getRightExpression()) {
            fixDeclaringClass(de);
            return de;
        }
        DeclarationExpression newDeclExpr = new DeclarationExpression(left, de.getOperation(), right);
        newDeclExpr.setDeclaringClass(de.getDeclaringClass());
        fixDeclaringClass(newDeclExpr);
        newDeclExpr.setSourcePosition(de);
        // GRECLIPSE add
        newDeclExpr.copyNodeMetaData(de);
        // GRECLIPSE end
        newDeclExpr.addAnnotations(de.getAnnotations());
        return newDeclExpr;
    }

    // TODO get normal resolving to set declaring class
    private void fixDeclaringClass(DeclarationExpression newDeclExpr) {
        if (newDeclExpr.getDeclaringClass() == null && currentMethod != null) {
            newDeclExpr.setDeclaringClass(currentMethod.getDeclaringClass());
        }
    }

    protected Expression transformAnnotationConstantExpression(AnnotationConstantExpression ace) {
        AnnotationNode an = (AnnotationNode) ace.getValue();
        ClassNode type = an.getClassNode();
        // GRECLIPSE edit
        //resolveOrFail(type, ", unable to find class for annotation", an);
        resolveOrFail(type, " for annotation", an);
        // GRECLIPSE end
        for (Map.Entry<String, Expression> member : an.getMembers().entrySet()) {
            member.setValue(transform(member.getValue()));
        }
        return ace;
    }

    public void visitAnnotations(AnnotatedNode node) {
        List<AnnotationNode> annotations = node.getAnnotations();
        if (annotations.isEmpty()) return;
        /* GRECLIPSE edit
        Map<String, AnnotationNode> tmpAnnotations = new HashMap<String, AnnotationNode>();
        */
        ClassNode annType;
        for (AnnotationNode an : annotations) {
            // skip built-in properties
            if (an.isBuiltIn()) continue;
            annType = an.getClassNode();
            // GRECLIPSE edit
            //resolveOrFail(annType, ",  unable to find class for annotation", an);
            resolveOrFail(annType, " for annotation", an);
            // GRECLIPSE end
            for (Map.Entry<String, Expression> member : an.getMembers().entrySet()) {
                Expression newValue = transform(member.getValue());
                Expression adjusted = transformInlineConstants(newValue);
                member.setValue(adjusted);
                checkAnnotationMemberValue(adjusted);
            }
            /* GRECLIPSE edit -- redundant check
            if (annType.isResolved()) {
                Class annTypeClass = annType.getTypeClass();
                Retention retAnn = (Retention) annTypeClass.getAnnotation(Retention.class);
                if (retAnn != null && !retAnn.value().equals(RetentionPolicy.SOURCE) && !isRepeatable(annTypeClass)) {
                    // remember non-source/non-repeatable annos (auto collecting of Repeatable annotations is handled elsewhere)
                    AnnotationNode anyPrevAnnNode = tmpAnnotations.put(annTypeClass.getName(), an);
                    if (anyPrevAnnNode != null) {
                        addError("Cannot specify duplicate annotation on the same member : " + annType.getName(), an);
                    }
                }
            }
            */
        }
    }

    /* GRECLIPSE edit
    private boolean isRepeatable(Class annTypeClass) {
        Annotation[] annTypeAnnotations = annTypeClass.getAnnotations();
        for (Annotation annTypeAnnotation : annTypeAnnotations) {
            if (annTypeAnnotation.annotationType().getName().equals("java.lang.annotation.Repeatable")) {
                return true;
            }
        }
        return false;
    }
    */

    // resolve constant-looking expressions statically (do here as they get transformed away later)
    private static Expression transformInlineConstants(final Expression exp) {
        if (exp instanceof AnnotationConstantExpression) {
            ConstantExpression ce = (ConstantExpression) exp;
            if (ce.getValue() instanceof AnnotationNode) {
                // replicate a little bit of AnnotationVisitor here
                // because we can't wait until later to do this
                AnnotationNode an = (AnnotationNode) ce.getValue();
                for (Map.Entry<String, Expression> member : an.getMembers().entrySet()) {
                    member.setValue(transformInlineConstants(member.getValue()));
                }
            }
        } else {
            return ExpressionUtils.transformInlineConstants(exp);
        }
        return exp;
    }

    private void checkAnnotationMemberValue(Expression newValue) {
        if (newValue instanceof PropertyExpression) {
            PropertyExpression pe = (PropertyExpression) newValue;
            if (!(pe.getObjectExpression() instanceof ClassExpression)) {
                addError("unable to find class '" + pe.getText() + "' for annotation attribute constant", pe.getObjectExpression());
            }
        } else if (newValue instanceof ListExpression) {
            ListExpression le = (ListExpression) newValue;
            for (Expression e : le.getExpressions()) {
                checkAnnotationMemberValue(e);
            }
        }
    }

    public void visitClass(ClassNode node) {
        ClassNode oldNode = currentClass;

        currentClass = node;
        // GRECLIPSE add
        if (commencingResolution()) try {
        // GRECLIPSE end

        if (node instanceof InnerClassNode) {
            if (Modifier.isStatic(node.getModifiers())) {
                genericParameterNames = new HashMap<GenericsTypeName, GenericsType>();
            }

            InnerClassNode innerClassNode = (InnerClassNode) node;
            if (innerClassNode.isAnonymous()) {
                MethodNode enclosingMethod = innerClassNode.getEnclosingMethod();
                if (null != enclosingMethod) {
                    resolveGenericsHeader(enclosingMethod.getGenericsTypes());
                }
            }
        } else {
            genericParameterNames = new HashMap<GenericsTypeName, GenericsType>();
        }

        resolveGenericsHeader(node.getGenericsTypes());

        ModuleNode module = node.getModule();
        if (!module.hasImportsResolved()) {
            for (ImportNode importNode : module.getImports()) {
                currImportNode = importNode;
                ClassNode type = importNode.getType();
                if (resolve(type, false, false, true)) {
                    currImportNode = null;
                    continue;
                }
                currImportNode = null;
                addError("unable to resolve class " + type.getName(), type);
            }
            /* GRECLIPSE edit
            for (ImportNode importNode : module.getStaticStarImports().values()) {
                ClassNode type = importNode.getType();
                if (resolve(type, false, false, true)) continue;
                // Maybe this type belongs in the same package as the node that is doing the
                // static import. In that case, the package may not have been explicitly specified.
                // Try with the node's package too. If still not found, revert to original type name.
                if (type.getPackageName() == null && node.getPackageName() != null) {
                    String oldTypeName = type.getName();
                    type.setName(node.getPackageName() + "." + oldTypeName);
                    if (resolve(type, false, false, true)) continue;
                    type.setName(oldTypeName);
                }
                addError("unable to resolve class " + type.getName(), type);
            }
            */
            for (ImportNode importNode : module.getStarImports()) {
                if (importNode.getEnd() > 0) {
                    currImportNode = importNode;
                    String importName = importNode.getPackageName();
                    importName = importName.substring(0, importName.length()-1);
                    ClassNode type = ClassHelper.makeWithoutCaching(importName);
                    if (resolve(type, false, false, true)) {
                        importNode.setType(type);
                        type.setStart(importNode.getStart() + 7);
                        type.setEnd(type.getStart() + importName.length());
                    }
                    currImportNode = null;
                }
            }
            // GRECLIPSE end
            for (ImportNode importNode : module.getStaticImports().values()) {
                ClassNode type = importNode.getType();
                if (resolve(type, true, true, true)) continue;
                addError("unable to resolve class " + type.getName(), type);
            }
            for (ImportNode importNode : module.getStaticStarImports().values()) {
                ClassNode type = importNode.getType();
                if (resolve(type, true, true, true)) continue;
                addError("unable to resolve class " + type.getName(), type);
            }
            module.setImportsResolved(true);
        }

        ClassNode sn = node.getUnresolvedSuperClass();
        if (sn != null) resolveOrFail(sn, node, true);

        for (ClassNode anInterface : node.getInterfaces()) {
            resolveOrFail(anInterface, node, true);
        }

        checkCyclicInheritance(node, node.getUnresolvedSuperClass(), node.getInterfaces());

        super.visitClass(node);

        resolveOuterNestedClassFurther(node);

        // GRECLIPSE add
        finishedResolution();
        } finally {
        if (currentClass == node)
        // GRECLIPSE end
        currentClass = oldNode;
        // GRECLIPSE add
        }
        // GRECLIPSE end
    }

    // GRECLIPSE add
    /**
     * @return {@code true} if resolution should continue, {@code false} otherwise (because, for example, it previously succeeded for this unit)
     */
    protected boolean commencingResolution() {
        // template method
        return true;
    }

    protected void finishedResolution() {
        // template method
    }
    // GRECLIPSE end

    // GROOVY-7812(#2): Static inner classes cannot be accessed from other files when running by 'groovy' command
    private void resolveOuterNestedClassFurther(ClassNode node) {
        CompileUnit compileUnit = currentClass.getCompileUnit();

        if (null == compileUnit) return;

        Map<String, ConstructedOuterNestedClassNode> classesToResolve = compileUnit.getClassesToResolve();
        List<String> resolvedInnerClassNameList = new LinkedList<>();

        for (Map.Entry<String, ConstructedOuterNestedClassNode> entry : classesToResolve.entrySet()) {
            String innerClassName = entry.getKey();
            ConstructedOuterNestedClassNode constructedOuterNestedClass = entry.getValue();

            // When the outer class is resolved, all inner classes are resolved too
            if (node.getName().equals(constructedOuterNestedClass.getEnclosingClassNode().getName())) {
                ClassNode innerClassNode = compileUnit.getClass(innerClassName); // find the resolved inner class

                if (null == innerClassNode) {
                    return; // "unable to resolve class" error can be thrown already, no need to `addError`, so just return
                }

                constructedOuterNestedClass.setRedirect(innerClassNode);
                resolvedInnerClassNameList.add(innerClassName);
            }
        }

        for (String innerClassName : resolvedInnerClassNameList) {
            classesToResolve.remove(innerClassName);
        }
    }

    private void checkCyclicInheritance(ClassNode originalNode, ClassNode parentToCompare, ClassNode[] interfacesToCompare) {
        if(!originalNode.isInterface()) {
            if(parentToCompare == null) return;
            if(originalNode == parentToCompare.redirect()) {
                addError("Cyclic inheritance involving " + parentToCompare.getName() + " in class " + originalNode.getName(), originalNode);
                // GRECLIPSE add
                originalNode.redirect().setHasInconsistentHierarchy(true);
                // GRECLIPSE end
                return;
            }
            if(interfacesToCompare != null && interfacesToCompare.length > 0) {
                for(ClassNode intfToCompare : interfacesToCompare) {
                    if(originalNode == intfToCompare.redirect()) {
                        addError("Cycle detected: the type " + originalNode.getName() + " cannot implement itself" , originalNode);
                        // GRECLIPSE add
                        originalNode.redirect().setHasInconsistentHierarchy(true);
                        // GRECLIPSE end
                        return;
                    }
                }
            }
            if(parentToCompare == ClassHelper.OBJECT_TYPE) return;
            checkCyclicInheritance(originalNode, parentToCompare.getUnresolvedSuperClass(), null);
        } else {
            if(interfacesToCompare != null && interfacesToCompare.length > 0) {
                // check interfaces at this level first
                for(ClassNode intfToCompare : interfacesToCompare) {
                    if(originalNode == intfToCompare.redirect()) {
                        addError("Cyclic inheritance involving " + intfToCompare.getName() + " in interface " + originalNode.getName(), originalNode);
                        // GRECLIPSE add
                        originalNode.redirect().setHasInconsistentHierarchy(true);
                        // GRECLIPSE end
                        return;
                    }
                }
                // check next level of interfaces
                for(ClassNode intf : interfacesToCompare) {
                    checkCyclicInheritance(originalNode, null, intf.getInterfaces());
                }
            }
        }
    }

    public void visitCatchStatement(CatchStatement cs) {
        resolveOrFail(cs.getExceptionType(), cs);
        if (cs.getExceptionType() == ClassHelper.DYNAMIC_TYPE) {
            cs.getVariable().setType(ClassHelper.make(Exception.class));
        }
        super.visitCatchStatement(cs);
    }

    public void visitForLoop(ForStatement forLoop) {
        resolveOrFail(forLoop.getVariableType(), forLoop);
        super.visitForLoop(forLoop);
    }

    public void visitBlockStatement(BlockStatement block) {
        VariableScope oldScope = currentScope;
        currentScope = block.getVariableScope();
        super.visitBlockStatement(block);
        currentScope = oldScope;
    }

    protected SourceUnit getSourceUnit() {
        return source;
    }
    // GRECLIPSE add
    protected void resetSourceUnit() {
        source = null;
    }

    protected void resetVariableScope() {
        currentScope = null;
    }
    // GRECLIPSE end

    private boolean resolveGenericsTypes(GenericsType[] types) {
        if (types == null) return true;
        currentClass.setUsingGenerics(true);
        boolean resolved = true;
        for (GenericsType type : types) {
            // attempt resolution on all types, so don't short-circuit and stop if we've previously failed
            resolved = resolveGenericsType(type) && resolved;
        }
        return resolved;
    }

    private void resolveGenericsHeader(GenericsType[] types) {
        resolveGenericsHeader(types, null, 0);
    }

    private void resolveGenericsHeader(GenericsType[] types, GenericsType rootType, int level) {
        if (types == null) return;
        currentClass.setUsingGenerics(true);
        List<Tuple2<ClassNode, GenericsType>> upperBoundsWithGenerics = new LinkedList<>();
        List<Tuple2<ClassNode, ClassNode>> upperBoundsToResolve = new LinkedList<>();
        for (GenericsType type : types) {
            if (level > 0 && type.getName().equals(rootType.getName())) {
                continue;
            }

            ClassNode classNode = type.getType();
            String name = type.getName();
            GenericsTypeName gtn = new GenericsTypeName(name);
            ClassNode[] bounds = type.getUpperBounds();
            boolean isWild = QUESTION_MARK.equals(name);
            boolean toDealWithGenerics = 0 == level || (level > 0 && null != genericParameterNames.get(gtn));

            if (bounds != null) {
                boolean nameAdded = false;
                for (ClassNode upperBound : bounds) {
                    if (!isWild) {
                        if (!nameAdded && upperBound != null || !resolve(classNode)) {
                            if (toDealWithGenerics) {
                                genericParameterNames.put(gtn, type);
                                type.setPlaceholder(true);
                                classNode.setRedirect(upperBound);
                                nameAdded = true;
                            }
                        }

                        upperBoundsToResolve.add(new Tuple2<>(upperBound, classNode));
                    }

                    if (upperBound != null && upperBound.isUsingGenerics()) {
                        upperBoundsWithGenerics.add(new Tuple2<>(upperBound, type));
                    }
                }
            } else {
                if (!isWild) {
                    if (toDealWithGenerics) {
                        GenericsType originalGt = genericParameterNames.get(gtn);
                        genericParameterNames.put(gtn, type);
                        type.setPlaceholder(true);

                        if (null == originalGt) {
                            classNode.setRedirect(ClassHelper.OBJECT_TYPE);
                        } else {
                            classNode.setRedirect(originalGt.getType());
                        }
                    }
                }
            }
        }

        for (Tuple2<ClassNode, ClassNode> tp : upperBoundsToResolve) {
            ClassNode upperBound = tp.getFirst();
            ClassNode classNode = tp.getSecond();
            resolveOrFail(upperBound, classNode);
        }

        for (Tuple2<ClassNode, GenericsType> tp : upperBoundsWithGenerics) {
            ClassNode upperBound = tp.getFirst();
            GenericsType gt = tp.getSecond();
            resolveGenericsHeader(upperBound.getGenericsTypes(), 0 == level ? gt : rootType, level + 1);
        }
    }

    private boolean resolveGenericsType(GenericsType genericsType) {
        if (genericsType.isResolved()) return true;
        currentClass.setUsingGenerics(true);
        ClassNode type = genericsType.getType();
        // save name before redirect
        GenericsTypeName name = new GenericsTypeName(type.getName());
        ClassNode[] bounds = genericsType.getUpperBounds();
        if (!genericParameterNames.containsKey(name)) {
            if (bounds != null) {
                for (ClassNode upperBound : bounds) {
                    resolveOrFail(upperBound, genericsType);
                    type.setRedirect(upperBound);
                    resolveGenericsTypes(upperBound.getGenericsTypes());
                }
            } else if (genericsType.isWildcard()) {
                type.setRedirect(ClassHelper.OBJECT_TYPE);
            } else {
                resolveOrFail(type, genericsType);
            }
        } else {
            GenericsType gt = genericParameterNames.get(name);
            type.setRedirect(gt.getType());
            genericsType.setPlaceholder(true);
        }

        if (genericsType.getLowerBound() != null) {
            resolveOrFail(genericsType.getLowerBound(), genericsType);
        }

        if (resolveGenericsTypes(type.getGenericsTypes())) {
            genericsType.setResolved(genericsType.getType().isResolved());
        }
        return genericsType.isResolved();

    }

    public void setClassNodeResolver(ClassNodeResolver classNodeResolver) {
        this.classNodeResolver = classNodeResolver;
    }
}
