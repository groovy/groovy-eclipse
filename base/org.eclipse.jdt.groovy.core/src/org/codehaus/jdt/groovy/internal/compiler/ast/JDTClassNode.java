/*
 * Copyright 2009-2018 the original author or authors.
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

import static java.beans.Introspector.decapitalize;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.codehaus.groovy.GroovyBugError;
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
import org.codehaus.groovy.vmplugin.v5.Java5;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration.FieldDeclarationWithInitializer;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.search.AccessorSupport;
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
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MemberTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodVerifier;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.RawTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;

/**
 * Groovy can use these to ask questions of JDT bindings. They are only built as
 * required (as Groovy references to Java files are resolved). They remain unset
 * until Groovy starts digging into them. At that time the details are filled in
 * (eg. members).
 */
public class JDTClassNode extends ClassNode implements JDTNode {

    private static final ClassNode unboundWildcard; // represents plain old '?'
    static {
        unboundWildcard = ClassHelper.makeWithoutCaching("?");
        unboundWildcard.setRedirect(ClassHelper.OBJECT_TYPE);
    }

    //--------------------------------------------------------------------------

    /** Configuration flags */
    private volatile int bits;
    private boolean beingInitialized;
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
    public void setJdtBinding(ReferenceBinding jdtBinding) {
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

    public JDTClassNode(ReferenceBinding jdtReferenceBinding, JDTResolver resolver) {
        super(getName(jdtReferenceBinding), getMods(jdtReferenceBinding), null);
        this.jdtBinding = jdtReferenceBinding;
        this.resolver = resolver;

        // population of the methods/ctors/fields/etc is not done until required
        this.lazyInitDone = false;

        // a primary node will result in a class file
        this.isPrimaryNode = false;
    }

    private static String getName(TypeBinding tb) {
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

    private static int getMods(TypeBinding tb) {
        if (tb instanceof ReferenceBinding) {
            return ((ReferenceBinding) tb).modifiers;
        } else {
            // FIXASC need to be smarter here? Who is affected?
            return Flags.AccPublic;
        }
    }

    @Override
    public void lazyClassInit() {
        synchronized (lazyInitLock) {
            if (lazyInitDone) {
                return;
            }
            initialize();
            lazyInitDone = true;
        }
    }

    /**
     * Basic initialization of the node - try and do most resolution lazily but some elements are worth getting correct up front: superclass, superinterfaces
     */
    // FIXASC confusing (and problematic?) that the superclass is setup after the generics information
    private void initialize() {
        if (beingInitialized) {
            return;
        }
        try {
            beingInitialized = true;

            if (!jdtBinding.isInterface()) {
                ReferenceBinding superClass = jdtBinding.superclass();
                if (superClass != null) {
                    setUnresolvedSuperClass(resolver.convertToClassNode(superClass));
                }
            }

            ReferenceBinding[] superInterfaceBindings = jdtBinding.superInterfaces();
            if (superInterfaceBindings == null)
                superInterfaceBindings = Binding.NO_SUPERINTERFACES;
            int n = superInterfaceBindings.length;
            ClassNode[] interfaces = new ClassNode[n];
            for (int i = 0; i < n; i += 1) {
                interfaces[i] = resolver.convertToClassNode(superInterfaceBindings[i]);
            }
            setInterfaces(interfaces);

            initializeMembers();
        } finally {
            beingInitialized = false;
        }
    }

    private void initializeMembers() {
        if (jdtBinding instanceof SourceTypeBinding) {
            SourceTypeBinding sourceTypeBinding = (SourceTypeBinding) jdtBinding;
            if (sourceTypeBinding.scope != null) {
                TypeDeclaration typeDecl = sourceTypeBinding.scope.referenceContext;
                if (typeDecl instanceof GroovyTypeDeclaration) {
                    groovyTypeDecl = (GroovyTypeDeclaration) typeDecl;
                }
            }
        }

        // We do this here rather than at the start of the method because
        // the preceding code sets 'groovyDecl', later used to 'initializeProperties'.

        // From this point onward... the code is only about initializing fields, constructors and methods.
        if (redirect != null) {
            // The code in ClassNode seems set up to get field information *always* from the end of the 'redirect' chain.
            // So, the redirect target should be responsible for its own members initialisation.

            // If we initialize members here again, when redirect target is already
            // initialised then we will be adding duplicated methods to the redirect target.

            return;
        }

        try {
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
                        addConstructor(cNode);
                    } else {
                        MethodNode mNode = methodBindingToMethodNode(methodBinding);
                        addMethod(mNode);
                    }
                }
            }

            if (jdtBinding instanceof BinaryTypeBinding) {
                MethodBinding[] infraBindings = ((BinaryTypeBinding) jdtBinding).infraMethods();
                for (MethodBinding methodBinding : infraBindings) {
                    if (methodBinding.isConstructor()) {
                        ConstructorNode cNode = constructorBindingToConstructorNode(methodBinding);
                        addConstructor(cNode);
                    } else {
                        MethodNode mNode = methodBindingToMethodNode(methodBinding);
                        addMethod(mNode);
                    }
                }
            } else if (jdtBinding instanceof SourceTypeBinding) {
                SourceTypeBinding jdtSourceTypeBinding = (SourceTypeBinding) jdtBinding;
                ClassScope classScope = jdtSourceTypeBinding.scope;
                // a null scope indicates it has already been 'cleaned up' so nothing to do (CUDeclaration.cleanUp())
                if (classScope != null) {
                    CompilationUnitScope cuScope = classScope.compilationUnitScope();
                    LookupEnvironment environment = classScope.environment();
                    MethodVerifier verifier = environment.methodVerifier();
                    cuScope.verifyMethods(verifier);
                }
                if (jdtSourceTypeBinding.isPrototype()) {
                    // Synthetic bindings are created for features like covariance, where the method implementing an interface method uses a
                    // different return type (interface I { A foo(); } class C implements I { AA foo(); } - this needs a method 'A foo()' in C.
                    SyntheticMethodBinding[] syntheticMethodBindings = jdtSourceTypeBinding.syntheticMethods();
                    if (syntheticMethodBindings != null) {
                        for (SyntheticMethodBinding syntheticBinding : syntheticMethodBindings) {
                            if (syntheticBinding.isConstructor()) {
                                ConstructorNode cNode = constructorBindingToConstructorNode(syntheticBinding);
                                addConstructor(cNode);
                            } else {
                                MethodNode mNode = methodBindingToMethodNode(syntheticBinding);
                                addMethod(mNode);
                            }
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

            if (mightHaveInners()) {
                Stream.of(jdtBinding.memberTypes()).map(resolver::convertToClassNode).forEach(cn -> {
                    @SuppressWarnings("unused") // InnerClassNode constructor adds reference to this.innerClasses
                    ClassNode icn = new InnerClassNode(this, cn.getName(), cn.getModifiers(), cn.getSuperClass()) {{
                        isPrimaryNode = false;
                        setRedirect(cn);
                    }};
                });
            }
        } catch (AbortCompilation e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to initialize members for type " + getName(), e);
        }
    }

    /**
     * Convert a JDT MethodBinding to a Groovy MethodNode
     */
    private MethodNode methodBindingToMethodNode(MethodBinding methodBinding) {
        try {
            // FIXASC What value is there in getting the parameter names correct? (for methods and ctors)
            // If they need to be correct we need to retrieve the method decl from the binding scope

            int modifiers = methodBinding.modifiers;
            if (jdtBinding.isInterface() && !Flags.isStatic(modifiers) && !Flags.isSynthetic(modifiers) && !Flags.isDefaultMethod(modifiers)) {
                modifiers |= Flags.AccAbstract;
            }

            ClassNode returnType = methodBinding.returnType != null ?
                resolver.convertToClassNode(methodBinding.returnType) : ClassHelper.DYNAMIC_TYPE;

            Parameter[] parameters = makeParameters(methodBinding.parameters, methodBinding.parameterNames);

            // initialize parameter annotations
            AnnotationBinding[][] parameterAnnotations = methodBinding.getParameterAnnotations();
            if (parameterAnnotations != null) {
                for (int i = 0, n = parameters.length; i < n; i += 1) {
                    for (AnnotationBinding annotationBinding : parameterAnnotations[i]) {
                        parameters[i].addAnnotation(new JDTAnnotationNode(annotationBinding, resolver));
                    }
                }
            }

            int nExceptions; ClassNode[] exceptions = ClassNode.EMPTY_ARRAY;
            if (methodBinding.thrownExceptions != null && (nExceptions = methodBinding.thrownExceptions.length) > 0) {
                exceptions = new ClassNode[nExceptions];
                for (int i = 0; i < nExceptions; i += 1) {
                    exceptions[i] = resolver.convertToClassNode(methodBinding.thrownExceptions[i]);
                }
            }

            MethodNode methodNode = new JDTMethodNode(methodBinding, resolver, String.valueOf(methodBinding.selector), modifiers, returnType, parameters, exceptions, null);
            methodNode.setGenericsTypes(new JDTClassNodeBuilder(resolver).configureTypeVariables(methodBinding.typeVariables()));
            return methodNode;
        } catch (AbortCompilation e) {
            throw e;
        } catch (RuntimeException e) {
            throw new IllegalStateException("Failed to resolve method node for " + String.valueOf(
                CharOperation.concatWith(jdtBinding.compoundName, methodBinding.selector, '.')), e);
        }
    }

    private Parameter[] makeParameters(TypeBinding[] parameterTypes, char[][] parameterNames) {
        int nParameters; Parameter[] parameters = Parameter.EMPTY_ARRAY;
        if (parameterTypes != null && (nParameters = parameterTypes.length) > 0) {
            parameters = new Parameter[nParameters];
            for (int i = 0; i < nParameters; i += 1) {
                String parameterName;
                if (i < parameterNames.length) {
                    parameterName = String.valueOf(parameterNames[i]);
                } else if (i < Java5.ARGS.length) {
                    parameterName = Java5.ARGS[i];
                } else {
                    parameterName = "arg" + i;
                }
                parameters[i] = makeParameter(parameterTypes[i], parameterName);
            }
        }
        return parameters;
    }

    private Parameter makeParameter(TypeBinding parameterType, String parameterName) {
        TypeBinding erasureType;
        if (parameterType instanceof ParameterizedTypeBinding) {
            erasureType = ((ParameterizedTypeBinding) parameterType).genericType();
        } else {
            erasureType = new JDTClassNodeBuilder(resolver).toRawType(parameterType);
        }
        return new Parameter(makeClassNode(parameterType, erasureType), parameterName);
    }

    /**
     * @param t type
     * @param c erasure of type
     */
    private ClassNode makeClassNode(TypeBinding t, TypeBinding c) {
        ClassNode back = resolver.convertToClassNode(c);
        if (!((t instanceof BinaryTypeBinding) || (t instanceof SourceTypeBinding))) {
            ClassNode front = new JDTClassNodeBuilder(resolver).configureType(t);
            front.setRedirect(back);
            return front;
        }
        return back;
    }

    private ConstructorNode constructorBindingToConstructorNode(MethodBinding methodBinding) {
        int modifiers = methodBinding.modifiers;
        Parameter[] parameters = makeParameters(methodBinding.parameters, methodBinding.parameterNames);
        ClassNode[] thrownExceptions = ClassNode.EMPTY_ARRAY;
        if (methodBinding.thrownExceptions != null) {
            thrownExceptions = new ClassNode[methodBinding.thrownExceptions.length];
            for (int i = 0, n = methodBinding.thrownExceptions.length; i < n; i += 1) {
                thrownExceptions[i] = resolver.convertToClassNode(methodBinding.thrownExceptions[i]);
            }
        }
        ConstructorNode ctorNode = new ConstructorNode(modifiers, parameters, thrownExceptions, null);
        ctorNode.setGenericsTypes(
            new JDTClassNodeBuilder(resolver).configureTypeVariables(methodBinding.typeVariables()));
        return ctorNode;
    }

    private FieldNode fieldBindingToFieldNode(FieldBinding fieldBinding, TypeDeclaration groovyTypeDecl) {
        String name = String.valueOf(fieldBinding.name);
        int modifiers = fieldBinding.modifiers;
        ClassNode fieldType = resolver.convertToClassNode(fieldBinding.type);
        Constant c = fieldBinding.constant();

        Expression initializerExpression = null;
        // FIXASC for performance reasons could fetch the initializer lazily if a JDTFieldNode were created
        if (c == Constant.NotAConstant) {
            /**
             * If the field binding is for a real source field, we should be able to see any initializer in it.
             */
            if (groovyTypeDecl != null) {
                FieldDeclaration fieldDecl = groovyTypeDecl.declarationOf(fieldBinding);
                if (fieldDecl instanceof FieldDeclarationWithInitializer) {
                    initializerExpression = ((FieldDeclarationWithInitializer) fieldDecl).getGroovyInitializer();
                }
            }
        } else {
            if (c instanceof StringConstant) {
                initializerExpression = new ConstantExpression(((StringConstant) c).stringValue());
            } else if (c instanceof BooleanConstant) {
                initializerExpression = new ConstantExpression(((BooleanConstant) c).booleanValue());
            } else if (c instanceof IntConstant) {
                initializerExpression = new ConstantExpression(((IntConstant) c).intValue());
            } else if (c instanceof LongConstant) {
                initializerExpression = new ConstantExpression(((LongConstant) c).longValue());
            } else if (c instanceof DoubleConstant) {
                initializerExpression = new ConstantExpression(((DoubleConstant) c).doubleValue());
            } else if (c instanceof FloatConstant) {
                initializerExpression = new ConstantExpression(((FloatConstant) c).floatValue());
            } else if (c instanceof ByteConstant) {
                initializerExpression = new ConstantExpression(((ByteConstant) c).byteValue());
            } else if (c instanceof CharConstant) {
                initializerExpression = new ConstantExpression(((CharConstant) c).charValue());
            } else if (c instanceof ShortConstant) {
                initializerExpression = new ConstantExpression(((ShortConstant) c).shortValue());
            }
        }
        FieldNode fNode = new JDTFieldNode(fieldBinding, resolver, name, modifiers, fieldType, this, initializerExpression);
        return fNode;
    }

    //--------------------------------------------------------------------------

    @Override
    public String getClassInternalName() {
        return getName().replace('.', '/');
    }

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
                        this.addAnnotation(new JDTAnnotationNode(annotationBinding, resolver));
                    }
                    bits |= ANNOTATIONS_INITIALIZED;
                }
            }
        }
        return Collections.unmodifiableList(super.getAnnotations());
    }

    @Override
    public List<AnnotationNode> getAnnotations(ClassNode type) {
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
    public void setGenericsTypes(GenericsType[] genericsTypes) {
        anyGenericsInitialized = true;
        super.setGenericsTypes(genericsTypes);
    }

    void setUpGenerics() {
        if (!anyGenericsInitialized)
        try {
            if (jdtBinding instanceof RawTypeBinding) {
                // nothing to do
            } else if (jdtBinding instanceof ParameterizedTypeBinding) {
                GenericsType[] gts = new JDTClassNodeBuilder(resolver).configureTypeArguments(((ParameterizedTypeBinding) jdtBinding).arguments);
                setGenericsTypes(gts);
            } else {
                // SourceTB, BinaryTB, TypeVariableB, WildcardB
                TypeVariableBinding[] typeVariables = jdtBinding.typeVariables();
                GenericsType[] generics = new JDTClassNodeBuilder(resolver).configureTypeVariables(typeVariables);
                if (generics != null) {
                    setGenericsTypes(generics);
                }
            }
        } finally {
            anyGenericsInitialized = true;
        }
    }

    @Override
    public void addProperty(PropertyNode node) {
        throw new UnsupportedOperationException("JDTClassNode is immutable, should not be called to add property: " + node.getName());
    }

    @Override
    public PropertyNode addProperty(String name, int modifiers, ClassNode type, Expression initialValueExpression, Statement getterBlock, Statement setterBlock) {
        throw new UnsupportedOperationException("JDTClassNode is immutable, should not be called to add property: " + name);
    }

    @Override
    public List<PropertyNode> getProperties() {
        if ((bits & PROPERTIES_INITIALIZED) == 0) {
            synchronized (this) {
                if ((bits & PROPERTIES_INITIALIZED) == 0) {
                    lazyClassInit();
                    if (groovyTypeDecl != null) {
                        Set<String> names = new HashSet<>();
                        List<PropertyNode> nodes = super.getProperties();
                        getMethods().stream().filter(AccessorSupport::isGetter).forEach(methodNode -> {
                            String methodName = methodNode.getName();
                            String propertyName = decapitalize(methodName.substring(methodName.startsWith("is") ? 2 : 3));

                            // STS-2628: don't double-add properties if there is a getter and an isser variant
                            if (names.add(propertyName)) {
                                FieldNode field = getField(propertyName);
                                boolean synth = (field == null);
                                if (synth) {
                                    field = new FieldNode(propertyName, methodNode.getModifiers(), methodNode.getReturnType(), this, null);
                                    field.setDeclaringClass(this);
                                    field.setSynthetic(true);
                                }
                                PropertyNode property = new PropertyNode(field, methodNode.getModifiers(), null, null);
                                property.setDeclaringClass(this);
                                property.setSynthetic(synth);

                                nodes.add(property);
                            }
                        });
                    }
                    bits |= PROPERTIES_INITIALIZED;
                }
            }
        }

        return Collections.unmodifiableList(super.getProperties());
    }

    /**
     * Some AST transforms are written such that they refer to typeClass on a ClassNode.
     * This is not available under Eclipse. However, we can support it in a rudimentary
     * fashion by attempting a class load for the class using the transform loader (if
     * available).
     */
    @Override
    public Class getTypeClass() {
        if (clazz != null || unfindable) {
            return clazz;
        }
        ClassLoader transformLoader = resolver.compilationUnit.getTransformLoader();
        if (transformLoader != null) {
            // TODO: What about array types?
            try {
                clazz = Class.forName(getName(), false, transformLoader);
                return clazz;
            } catch (ClassNotFoundException e) {
                unfindable = true;
            }
        }
        throw new GroovyBugError("JDTClassNode.getTypeClass() cannot locate class for " + getName() + " using transform loader " + transformLoader);
    }

    @Override
    public boolean isDeprecated() {
        return jdtBinding.isDeprecated();
    }

    @Override
    public boolean isPrimitive() {
        return false; // FIXASC (M3) verify always true. Think it is a jdtReferenceBinding is a reference binding and not a typebinding
    }

    @Override
    public boolean isResolved() {
        return true; // JDTClassNodes are created because of a JDT Reference Binding file so are always 'resolved' (although not initialized upon creation)
    }

    // TODO: Remove when Groovy 2.4 is hard minimum.
    public boolean isReallyResolved() {
        return true;
    }

    @Override
    public boolean mightHaveInners() {
        return (jdtBinding.memberTypes().length != 0);
    }
}
