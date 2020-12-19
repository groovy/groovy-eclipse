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
package org.codehaus.jdt.groovy.internal.compiler.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import groovy.lang.MissingClassException;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration.FieldDeclarationWithInitializer;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.util.ArrayUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.impl.BooleanConstant;
import org.eclipse.jdt.internal.compiler.impl.ByteConstant;
import org.eclipse.jdt.internal.compiler.impl.CharConstant;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.DoubleConstant;
import org.eclipse.jdt.internal.compiler.impl.FloatConstant;
import org.eclipse.jdt.internal.compiler.impl.IntConstant;
import org.eclipse.jdt.internal.compiler.impl.LongConstant;
import org.eclipse.jdt.internal.compiler.impl.ShortConstant;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.DelegateMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MemberTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.RawTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;

/**
 * Groovy can use these to ask questions of JDT bindings. They are only built as
 * required (as Groovy references to Java files are resolved). They remain unset
 * until Groovy starts digging into them. At that time the details are filled in
 * (e.g. members).
 */
public class JDTClassNode extends ClassNode implements JDTNode {

    private volatile int bits;
    private boolean lazyInitStarted;
    private boolean anyGenericsInitialized;

    private GroovyTypeDeclaration groovyTypeDecl;

    /** The binding which this JDTClassNode represents */
    private ReferenceBinding jdtBinding;

    @Override
    public ReferenceBinding getJdtBinding() {
        return jdtBinding;
    }

    /**
     * When working with parameterized types, groovy will create a simple ClassNode for the raw type and then initialize the
     * generics structure behind it. This setter is used to ensure that these 'simple' ClassNodes that are created for raw types
     * but are intended to represent parameterized types will have their generics info available (from the parameterized
     * jdt binding).
     */
    public void setJdtBinding(final ReferenceBinding jdtBinding) {
        this.jdtBinding = jdtBinding;
    }

    /** The resolver instance involved at the moment */
    private final JDTResolver resolver;

    @Override
    public JDTResolver getResolver() {
        return resolver;
    }

    private boolean unfindable;

    //--------------------------------------------------------------------------

    public JDTClassNode(final ReferenceBinding jdtReferenceBinding, final JDTResolver resolver) {
        super(getName(jdtReferenceBinding), getMods(jdtReferenceBinding), null);
        this.jdtBinding = jdtReferenceBinding;
        this.resolver = resolver;

        // population of fields, methods, etc. is deferred until required
        this.lazyInitDone = false;

        // a primary node will result in a class file
        this.isPrimaryNode = false;
    }

    private static String getName(final TypeBinding tb) {
        if (tb instanceof ArrayBinding) {
            return String.valueOf(((ArrayBinding) tb).signature());
        } else if (tb instanceof MemberTypeBinding) {
            MemberTypeBinding mtb = (MemberTypeBinding) tb;
            return CharOperation.toString(mtb.compoundName);
        } else if (tb instanceof ReferenceBinding) {
            return CharOperation.toString(((ReferenceBinding) tb).compoundName);
        } else {
            return String.valueOf(tb.sourceName());
        }
    }

    private static int getMods(final TypeBinding tb) {
        if (tb instanceof ReferenceBinding) {
            return ((ReferenceBinding) tb).modifiers;
        } else {
            // FIXASC need to be smarter here? Who is affected?
            return Flags.AccPublic;
        }
    }

    @Override
    public void lazyClassInit() {
        if (!lazyInitDone) {
            synchronized (lazyInitLock) {
                if (lazyInitDone || lazyInitStarted) return; lazyInitStarted = true;

                if (jdtBinding instanceof SourceTypeBinding) {
                    SourceTypeBinding sourceTypeBinding = (SourceTypeBinding) jdtBinding;
                    if (sourceTypeBinding.scope != null) {
                        TypeDeclaration typeDecl = sourceTypeBinding.scope.referenceContext;
                        if (typeDecl instanceof GroovyTypeDeclaration) {
                            groovyTypeDecl = (GroovyTypeDeclaration) typeDecl;
                        }
                    }
                }

                // defer most type resolution, but some items are worth getting correct up front: super class, super interfaces

                if (!jdtBinding.isInterface()) {
                    ReferenceBinding superClass = jdtBinding.superclass();
                    if (superClass != null) {
                        setUnresolvedSuperClass(resolver.convertToClassNode(superClass));
                    }
                }

                ReferenceBinding[] superInterfaceBindings = jdtBinding.superInterfaces();
                if (superInterfaceBindings == null)
                    superInterfaceBindings = Binding.NO_SUPERINTERFACES;
                final int n = superInterfaceBindings.length;
                ClassNode[] interfaces = new ClassNode[n];
                for (int i = 0; i < n; i += 1) {
                    interfaces[i] = resolver.convertToClassNode(superInterfaceBindings[i]);
                }
                setInterfaces(interfaces);

                initializeMembers();
                lazyInitDone = true;
            }
        }
    }

    private void initializeMembers() {
        if (isRedirectNode()) {
            // ClassNode is set up to get member information from the end of the "redirect" chain.
            // So the redirect target should be responsible for initialization of its own members.

            // If we initialize members here again, when redirect target is already initialized,
            // then we will be adding duplicated methods to the redirect target.

            return;
        }

        try {
            List<Object[]> pairs = new ArrayList<>(); //MethodBinding,MethodNode

            MethodBinding[] methodBindings;
            if (jdtBinding instanceof ParameterizedTypeBinding) {
                ReferenceBinding genericType = ((ParameterizedTypeBinding) jdtBinding).genericType();
                methodBindings = genericType.methods();
            } else {
                methodBindings = jdtBinding.methods();
            }
            if (methodBindings != null) {
                for (MethodBinding methodBinding : methodBindings) {
                    if (methodBinding.isConstructor()) {
                        ConstructorNode cNode = constructorBindingToConstructorNode(methodBinding);
                        pairs.add(new Object[] {methodBinding, cNode});
                        addConstructor(cNode);
                    } else {
                        MethodNode mNode = methodBindingToMethodNode(methodBinding);
                        pairs.add(new Object[] {methodBinding, mNode});
                        addMethod(mNode);
                    }
                }
            }

            if (jdtBinding instanceof BinaryTypeBinding) {
                MethodBinding[] infraBindings = ((BinaryTypeBinding) jdtBinding).infraMethods();
                for (MethodBinding methodBinding : infraBindings) {
                    if (methodBinding.isConstructor()) {
                        ConstructorNode cNode = constructorBindingToConstructorNode(methodBinding);
                        pairs.add(new Object[] {methodBinding, cNode});
                        addConstructor(cNode);
                    } else {
                        MethodNode mNode = methodBindingToMethodNode(methodBinding);
                        pairs.add(new Object[] {methodBinding, mNode});
                        addMethod(mNode);
                    }
                }
            } else if (jdtBinding instanceof SourceTypeBinding && (jdtBinding.tagBits & TagBits.HasMissingType) == 0) {
                SourceTypeBinding sourceTypeBinding = (SourceTypeBinding) jdtBinding;
                if (sourceTypeBinding.isPrototype()) {
                    ClassScope classScope = sourceTypeBinding.scope;
                    // a null scope indicates it has already been "cleaned up" (CompilationUnitDeclaration#cleanUp())
                    if (classScope != null) {
                        CompilationUnitScope cuScope = classScope.compilationUnitScope();
                        LookupEnvironment environment = classScope.environment();
                        cuScope.verifyMethods(environment.methodVerifier());
                    }
                    // Synthetic bindings are created for features like covariance, where the method implementing an interface method uses a
                    // different return type (interface I { A foo(); } class C implements I { AA foo(); } - this needs a method 'A foo()' in C.
                    SyntheticMethodBinding[] syntheticMethodBindings = sourceTypeBinding.syntheticMethods();
                    if (syntheticMethodBindings != null) {
                        for (SyntheticMethodBinding syntheticBinding : syntheticMethodBindings) {
                            if (syntheticBinding.isConstructor()) {
                                ConstructorNode cNode = constructorBindingToConstructorNode(syntheticBinding);
                                pairs.add(new Object[] {syntheticBinding, cNode});
                                addConstructor(cNode);
                            } else {
                                MethodNode mNode = methodBindingToMethodNode(syntheticBinding);
                                pairs.add(new Object[] {syntheticBinding, mNode});
                                addMethod(mNode);
                            }
                        }
                    }
                }
            }

            for (Object[] pair : pairs) {
                if (pair[0] instanceof DelegateMethodBinding) {
                    MethodBinding target = ((DelegateMethodBinding) pair[0]).delegateMethod.binding;
                    for (MethodNode candidate : pair[1] instanceof ConstructorNode ? constructors : methods.getNotNull(((MethodNode) pair[1]).getName())) {
                        Binding binding = pair[1] instanceof JDTNode ? ((JDTNode) candidate).getJdtBinding() : candidate.getNodeMetaData("JdtBinding");
                        if (binding == target) {
                            ((MethodNode) pair[1]).setOriginal(candidate);
                        }
                    }
                }
            }

            FieldBinding[] fieldBindings;
            if (jdtBinding instanceof ParameterizedTypeBinding) {
                fieldBindings = ((ParameterizedTypeBinding) jdtBinding).genericType().fields();
            } else {
                fieldBindings = jdtBinding.fields();
            }
            if (fieldBindings != null) {
                for (FieldBinding fieldBinding : fieldBindings) {
                    FieldNode fNode = fieldBindingToFieldNode(fieldBinding, groovyTypeDecl);
                    addField(fNode);
                }
            }

            if (groovyTypeDecl != null && isTrait()) {
                for (PropertyNode pNode : getProperties()) {
                    String pName = pNode.getName(), capitalizedName = MetaClassHelper.capitalize(pName);
                    int mMods = Flags.AccPublic | (pNode.getModifiers() & Flags.AccStatic);
                    if (ClassHelper.boolean_TYPE.equals(pNode.getType())) {
                        MethodNode mNode = addMethod("is" + capitalizedName, mMods, pNode.getType(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null);
                        if (!(mNode instanceof JDTNode)) {
                            mNode.setNameStart(pNode.getField().getNameStart());
                            mNode.setNameEnd(pNode.getField().getNameEnd());
                            mNode.setSynthetic(true);

                            // GROOVY-9382: include "getter" if "isser" was not declared
                            mNode = addMethod("get" + capitalizedName, mMods, pNode.getType(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null);
                            if (!(mNode instanceof JDTNode)) {
                                mNode.setNameStart(pNode.getField().getNameStart());
                                mNode.setNameEnd(pNode.getField().getNameEnd());
                                mNode.setSynthetic(true);
                            }
                        }
                    } else {
                        MethodNode mNode = addMethod("get" + capitalizedName, mMods, pNode.getType(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null);
                        if (!(mNode instanceof JDTNode)) {
                            mNode.setNameStart(pNode.getField().getNameStart());
                            mNode.setNameEnd(pNode.getField().getNameEnd());
                            mNode.setSynthetic(true);
                        }
                    }
                    if (!Flags.isFinal(pNode.getModifiers())) {
                        MethodNode mNode = addMethod("set" + capitalizedName, mMods, ClassHelper.VOID_TYPE, new Parameter[] {new Parameter(pNode.getType(), pName)}, ClassNode.EMPTY_ARRAY, null);
                        if (!(mNode instanceof JDTNode)) {
                            mNode.setNameStart(pNode.getField().getNameStart());
                            mNode.setNameEnd(pNode.getField().getNameEnd());
                            mNode.setSynthetic(true);
                        }
                    }
                }
            }
        } catch (AbortCompilation e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to initialize members for type " + getName(), e);
        }
    }

    private MethodNode methodBindingToMethodNode(final MethodBinding methodBinding) {
        try {
            int modifiers = methodBinding.modifiers;
            if (isInterface() && !Flags.isStatic(modifiers) && !Flags.isSynthetic(modifiers) && !Flags.isDefaultMethod(modifiers) && !isTrait()) {
                modifiers |= Flags.AccAbstract;
            }

            ClassNode returnType = (methodBinding.returnType != null ? resolver.convertToClassNode(methodBinding.returnType) : ClassHelper.DYNAMIC_TYPE);

            Parameter[] parameters = makeParameters(methodBinding.parameters, methodBinding.parameterNames, methodBinding.getParameterAnnotations());

            ClassNode[] exceptions = ClassNode.EMPTY_ARRAY;
            if (methodBinding.thrownExceptions != null && methodBinding.thrownExceptions.length > 0) {
                exceptions = new ClassNode[methodBinding.thrownExceptions.length];
                for (int i = 0; i < methodBinding.thrownExceptions.length; i += 1) {
                    exceptions[i] = resolver.convertToClassNode(methodBinding.thrownExceptions[i]);
                }
            }

            MethodNode methodNode = new JDTMethodNode(methodBinding, resolver, String.valueOf(methodBinding.selector), modifiers, returnType, parameters, exceptions, null);
            methodNode.setAnnotationDefault(jdtBinding.isAnnotationType() && methodBinding.getDefaultValue() != null); // TODO: Capture default value?
            methodNode.setGenericsTypes(new JDTClassNodeBuilder(resolver).configureTypeVariables(methodBinding.typeVariables()));
            methodNode.setSynthetic((modifiers & 0x4000000) != 0); // see GroovyClassScope
            return methodNode;
        } catch (AbortCompilation e) {
            throw e;
        } catch (RuntimeException e) {
            throw new IllegalStateException("Failed to resolve method node for " + String.valueOf(
                CharOperation.concatWith(jdtBinding.compoundName, methodBinding.selector, '.')), e);
        }
    }

    private ConstructorNode constructorBindingToConstructorNode(final MethodBinding methodBinding) {
        Parameter[] parameters = makeParameters(methodBinding.parameters, methodBinding.parameterNames, methodBinding.getParameterAnnotations());

        ClassNode[] exceptions = ClassNode.EMPTY_ARRAY;
        if (methodBinding.thrownExceptions != null && methodBinding.thrownExceptions.length > 0) {
            exceptions = new ClassNode[methodBinding.thrownExceptions.length];
            for (int i = 0; i < methodBinding.thrownExceptions.length; i += 1) {
                exceptions[i] = resolver.convertToClassNode(methodBinding.thrownExceptions[i]);
            }
        }

        ConstructorNode ctorNode = new ConstructorNode(methodBinding.modifiers, parameters, exceptions, null);
        for (AnnotationBinding annotationBinding : methodBinding.getAnnotations()) {
            ctorNode.addAnnotation(new JDTAnnotationNode(annotationBinding, resolver));
        }
        ctorNode.setGenericsTypes(new JDTClassNodeBuilder(resolver).configureTypeVariables(methodBinding.typeVariables()));
        ctorNode.putNodeMetaData("JdtBinding", methodBinding);
        return ctorNode;
    }

    private FieldNode fieldBindingToFieldNode(final FieldBinding fieldBinding, final TypeDeclaration typeDeclaration) {
        String name = String.valueOf(fieldBinding.name);
        int modifiers = fieldBinding.modifiers;
        ClassNode fieldType = resolver.convertToClassNode(fieldBinding.type);
        Constant c = fieldBinding.constant();

        Expression initializerExpression = null;
        // FIXASC for performance reasons could fetch the initializer lazily if a JDTFieldNode were created
        if (c == Constant.NotAConstant) {
            // if the field binding is for a real source field, we should be able to see any initializer in it
            if (typeDeclaration != null) {
                FieldDeclaration fieldDecl = typeDeclaration.declarationOf(fieldBinding);
                if (fieldDecl instanceof FieldDeclarationWithInitializer) {
                    initializerExpression = ((FieldDeclarationWithInitializer) fieldDecl).getGroovyInitializer();
                }
            }
        } else if (c instanceof BooleanConstant) {
            initializerExpression = new ConstantExpression(((BooleanConstant) c).booleanValue());
        } else if (c instanceof ByteConstant) {
            initializerExpression = new ConstantExpression(((ByteConstant) c).byteValue());
        } else if (c instanceof CharConstant) {
            initializerExpression = new ConstantExpression(((CharConstant) c).charValue());
        } else if (c instanceof DoubleConstant) {
            initializerExpression = new ConstantExpression(((DoubleConstant) c).doubleValue());
        } else if (c instanceof FloatConstant) {
            initializerExpression = new ConstantExpression(((FloatConstant) c).floatValue());
        } else if (c instanceof IntConstant) {
            initializerExpression = new ConstantExpression(((IntConstant) c).intValue());
        } else if (c instanceof LongConstant) {
            initializerExpression = new ConstantExpression(((LongConstant) c).longValue());
        } else if (c instanceof ShortConstant) {
            initializerExpression = new ConstantExpression(((ShortConstant) c).shortValue());
        } else if (c instanceof StringConstant) {
            initializerExpression = new ConstantExpression(((StringConstant) c).stringValue());
        }

        return new JDTFieldNode(fieldBinding, resolver, name, modifiers, fieldType, this, initializerExpression);
    }

    /**
     * @param t type
     * @param e erasure of type
     */
    private ClassNode makeClassNode(final TypeBinding t, final TypeBinding e) {
        ClassNode back = resolver.convertToClassNode(e);
        if (!(t instanceof BinaryTypeBinding || t instanceof SourceTypeBinding)) {
            ClassNode front = new JDTClassNodeBuilder(resolver).configureType(t);
            JDTClassNodeBuilder.setRedirect(front, back);
            return front;
        }
        return back;
    }

    private Parameter makeParameter(final TypeBinding parameterType, final String parameterName) {
        TypeBinding erasureType;
        if (parameterType instanceof ParameterizedTypeBinding) {
            erasureType = ((ParameterizedTypeBinding) parameterType).genericType();
        } else if (parameterType instanceof ArrayBinding ||
            parameterType instanceof TypeVariableBinding) {
            erasureType = parameterType.erasure();
        } else {
            assert !parameterType.isGenericType();
            erasureType = parameterType;
        }
        return new Parameter(makeClassNode(parameterType, erasureType), parameterName);
    }

    private Parameter[] makeParameters(final TypeBinding[] parameterTypes, final char[][] parameterNames, final AnnotationBinding[][] parameterAnnotations) {
        Parameter[] parameters = Parameter.EMPTY_ARRAY;
        if (parameterTypes != null && parameterTypes.length > 0) {
            parameters = new Parameter[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i += 1) {
                String parameterName;
                if (i < parameterNames.length) {
                    parameterName = String.valueOf(parameterNames[i]);
                } else {
                    parameterName = ("arg" + i).intern();
                }
                parameters[i] = makeParameter(parameterTypes[i], parameterName);
                if (parameterAnnotations != null && parameterAnnotations.length > i) {
                    for (AnnotationBinding annotationBinding : parameterAnnotations[i]) {
                        parameters[i].addAnnotation(new JDTAnnotationNode(annotationBinding, resolver));
                    }
                }
            }
        }
        return parameters;
    }

    //--------------------------------------------------------------------------

    @Override
    public List<AnnotationNode> getAnnotations() {
        if ((bits & ANNOTATIONS_INITIALIZED) == 0) {
            synchronized (this) {
                if ((bits & ANNOTATIONS_INITIALIZED) == 0) {
                    if (jdtBinding instanceof SourceTypeBinding) {
                        @SuppressWarnings("unused") // ensure resolved
                        long tagBits = ((SourceTypeBinding) jdtBinding).getAnnotationTagBits();
                    }
                    for (AnnotationBinding annotationBinding : jdtBinding.getAnnotations()) {
                        addAnnotation(new JDTAnnotationNode(annotationBinding, resolver));
                    }
                    bits |= ANNOTATIONS_INITIALIZED;
                }
            }
        }
        return Collections.unmodifiableList(super.getAnnotations());
    }

    @Override
    public List<AnnotationNode> getAnnotations(final ClassNode type) {
        if ((bits & ANNOTATIONS_INITIALIZED) == 0) {
            @SuppressWarnings("unused") // ensure initialized
            List<AnnotationNode> annotations = getAnnotations();
        }
        return Collections.unmodifiableList(super.getAnnotations(type));
    }

    @Override
    public GenericsType[] getGenericsTypes() {
        if (!anyGenericsInitialized) {
            setUpGenerics();
        }
        return super.getGenericsTypes();
    }

    @Override
    public boolean isUsingGenerics() {
        if (!anyGenericsInitialized) {
            setUpGenerics();
        }
        return super.isUsingGenerics();
    }

    @Override
    public void setUsingGenerics(final boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setGenericsPlaceHolder(final boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setGenericsTypes(final GenericsType[] genericsTypes) {
        anyGenericsInitialized = true;
        super.setGenericsTypes(genericsTypes);
    }

    void setUpGenerics() {
        if (!anyGenericsInitialized) {
            final GenericsType[] generics;
            if (jdtBinding instanceof RawTypeBinding) {
                generics = null;
            } else if (jdtBinding instanceof ParameterizedTypeBinding) {
                generics = new JDTClassNodeBuilder(resolver).configureTypeArguments(((ParameterizedTypeBinding) jdtBinding).arguments);
            } else { // BinaryTypeBinding, SourceTypeBinding, TypeVariableBinding, WildcardBinding
                generics = new JDTClassNodeBuilder(resolver).configureTypeVariables(jdtBinding.typeVariables());
            }
            setGenericsTypes(generics);
        }
    }

    @Override
    public void addProperty(final PropertyNode node) {
        throw new UnsupportedOperationException("JDTClassNode is immutable, should not be called to add property: " + node.getName());
    }

    @Override
    public PropertyNode addProperty(final String name, final int modifiers, final ClassNode type,
            final Expression initialValueExpression, final Statement getterBlock, final Statement setterBlock) {
        throw new UnsupportedOperationException("JDTClassNode is immutable, should not be called to add property: " + name);
    }

    @Override
    public List<PropertyNode> getProperties() {
        if ((bits & PROPERTIES_INITIALIZED) == 0) {
            synchronized (this) {
                if ((bits & PROPERTIES_INITIALIZED) == 0) {
                    lazyClassInit();
                    if (groovyTypeDecl != null) {
                        List<PropertyNode> nodes = super.getProperties();

                        for (PropertyNode node : groovyTypeDecl.getClassNode().getProperties()) {
                            FieldNode field = getField(node.getName());
                            if (field == null) {
                                field = new FieldNode(node.getName(), Flags.AccPrivate | (node.getModifiers() & Flags.AccStatic), resolver.resolve(node.getType().getName()), this, null);
                                field.setDeclaringClass(this);
                                field.setSourcePosition(node.getField());
                                field.setSynthetic(true);
                            } else if (Flags.isPackageDefault(field.getModifiers())) {
                                continue; // @PackageScope field, not property
                            }

                            PropertyNode clone = new PropertyNode(field, node.getModifiers(), null, null);
                            clone.setDeclaringClass(this);
                            clone.setSourcePosition(node);

                            nodes.add(clone);
                        }
                    }
                    bits |= PROPERTIES_INITIALIZED;
                }
            }
        }

        return Collections.unmodifiableList(super.getProperties());
    }

    @Override
    public Iterator<InnerClassNode> getInnerClasses() {
        if ((bits & INNER_TYPES_INITIALIZED) == 0) {
            synchronized (this) {
                if ((bits & INNER_TYPES_INITIALIZED) == 0) {
                    bits |= INNER_TYPES_INITIALIZED;
                    if (jdtBinding.hasMemberTypes()) {
                        // workaround for https://github.com/groovy/groovy-eclipse/issues/714
                        if (jdtBinding instanceof BinaryTypeBinding && jdtBinding == jdtBinding.prototype() && isTrait()) {
                            ReferenceBinding[] memberTypes = ReflectionUtils.getPrivateField(BinaryTypeBinding.class, "memberTypes", jdtBinding);
                            for (int i = 0; i < memberTypes.length; i += 1) {
                                if (String.valueOf(memberTypes[i].sourceName).endsWith("$Trait$FieldHelper$1")) {
                                    memberTypes = (ReferenceBinding[]) ArrayUtils.remove(memberTypes, i--);
                                }
                            }
                            ReflectionUtils.setPrivateField(BinaryTypeBinding.class, "memberTypes", jdtBinding, memberTypes);
                        }
                        // workaround end
                        Arrays.stream(jdtBinding.memberTypes()).map(resolver::convertToClassNode).forEach(cn -> {
                            @SuppressWarnings("unused") // InnerClassNode constructor adds itself to this.innerClasses
                            ClassNode icn = new InnerClassNode(this, cn.getName(), cn.getModifiers(), cn.getSuperClass()) {{
                                isPrimaryNode = false;
                                setRedirect(cn);
                            }};
                        });
                    }
                }
            }
        }
        return (innerClasses == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(innerClasses)).iterator();
    }

    @Override
    public ClassNode getOuterClass() {
        if (jdtBinding.isNestedType()) {
            return resolver.convertToClassNode(jdtBinding.enclosingType());
        }
        return super.getOuterClass();
    }

    @Override
    public FieldNode getOuterField(final String name) {
        return getOuterClass().getDeclaredField(name);
    }

    @Override
    public Class getTypeClass() {
        if (hasClass()) return clazz;
        throw new MissingClassException(this, "-- JDTClassNode.getTypeClass() cannot locate it using transform loader");
    }

    @Override
    public boolean hasClass() {
        if (clazz == null && !unfindable) {
            // Some AST transforms are written such that they refer to typeClass on a ClassNode.
            // This is not available within Eclipse. However, we can support it in a rudimentary
            // fashion by attempting a class load using the transform loader (if it's available).
            ClassLoader transformLoader = resolver.compilationUnit.getTransformLoader();
            if (transformLoader != null) {
                try {
                    clazz = Class.forName(getName(), false, transformLoader);
                } catch (ReflectiveOperationException | LinkageError e) {
                    unfindable = true;
                }
            }
        }
        return (clazz != null);
    }

    public boolean isAnonymous() {
        return jdtBinding.isAnonymousType();
    }

    @Override
    public boolean isDeprecated() {
        return jdtBinding.isDeprecated();
    }

    @Override
    public boolean isResolved() {
        return true; // JDTClassNode created because of a JDT ReferenceBinding, so it is always "resolved" (although not initialized upon creation)
    }

    public boolean isTrait() {
        if (isTrait == null) {
            if (isInterface() && !isAnnotationDefinition() && !CharOperation.equals(jdtBinding.compoundName[0], TypeConstants.JAVA)) {
                if (groovyTypeDecl != null) {
                    isTrait = org.codehaus.groovy.transform.trait.Traits.isTrait(groovyTypeDecl.getClassNode());
                } else {
                    isTrait = org.codehaus.groovy.transform.trait.Traits.isTrait(this); // populates annotations
                }
            } else {
                isTrait = Boolean.FALSE;
            }
        }
        return isTrait.booleanValue();
    }
    private Boolean isTrait;
}
