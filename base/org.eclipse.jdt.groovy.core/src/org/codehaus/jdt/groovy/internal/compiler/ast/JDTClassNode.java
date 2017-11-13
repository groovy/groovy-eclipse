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
package org.codehaus.jdt.groovy.internal.compiler.ast;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration.FieldDeclarationWithInitializer;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.compiler.CharOperation;
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

    // arbitrary choice of first eight; maintaining these as a constant array prevents 10000 strings called 'arg0' consuming memory
    private final static String[] argNames = { "arg0", "arg1", "arg2", "arg3", "arg4", "arg5", "arg6", "arg7" };

    // The binding which this JDTClassNode represents
    ReferenceBinding jdtBinding;

    private boolean beingInitialized = false;

    private boolean anyGenericsInitialized = false;

    // The resolver instance involved at the moment
    JDTResolver resolver;

    // Configuration flags
    private int bits = 0;
    private static final int ANNOTATIONS_INITIALIZED = 0x0001;
    private static final int PROPERTIES_INITIALIZED = 0x0002;
    private TypeDeclaration groovyDecl = null;

    static final ClassNode unboundWildcard; // represents plain old '?'

    static final GenericsType genericsTypeUnboundWildcard;

    static {
        ClassNode base = ClassHelper.makeWithoutCaching("?");
        base.setRedirect(ClassHelper.OBJECT_TYPE);
        // ClassNode[] allUppers = new ClassNode[] { ClassHelper.OBJECT_TYPE };
        GenericsType t = new GenericsType(base, null, null);
        t.setName("?");
        t.setWildcard(true);
        // Can't use the constant ClassHelper.OBJECT_TYPE here as we are about to setGenericTypes on it.
        unboundWildcard = ClassHelper.makeWithoutCaching("?");
        unboundWildcard.setRedirect(ClassHelper.OBJECT_TYPE);

        genericsTypeUnboundWildcard = t;
    }

    /**
     * Create a new JDT ClassNode. Minimal setup is done initially (the superclass and superinterfaces are setup) and the rest of
     * the initialization is done later when required.
     */
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

    public void setupGenerics() {
        if (anyGenericsInitialized) {
            return;
        }
        try {
            if (jdtBinding instanceof RawTypeBinding) {
                // nothing to do
            } else if (jdtBinding instanceof ParameterizedTypeBinding) {
                GenericsType[] gts = new JDTClassNodeBuilder(this.resolver).configureTypeArguments(((ParameterizedTypeBinding) jdtBinding).arguments);
                setGenericsTypes(gts);
            } else {
                // SourceTB, BinaryTB, TypeVariableB, WildcardB
                TypeVariableBinding[] typeVariables = jdtBinding.typeVariables();
                GenericsType[] generics = new JDTClassNodeBuilder(this.resolver).configureTypeVariables(typeVariables);
                if (generics != null) {
                    this.setGenericsTypes(generics);
                }
            }
        } finally {
            anyGenericsInitialized = true;
        }
    }

    // JDTClassNodes are created because of a JDT Reference Binding file so are always 'resolved' (although not initialized on creation)
    @Override
    public boolean isResolved() {
        return true;
    }

    public void setGenericsTypes(GenericsType[] genericsTypes) {
        this.anyGenericsInitialized = true;
        super.setGenericsTypes(genericsTypes);
    }

    /**
     * Basic initialization of the node - try and do most resolution lazily but some elements are worth getting correct up front:
     * superclass, superinterfaces
     */
    // FIXASC confusing (and problematic?) that the superclass is setup after the generics information
    void initialize() {
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
            if (superInterfaceBindings == null) superInterfaceBindings = Binding.NO_SUPERINTERFACES;

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
            SourceTypeBinding sourceType = (SourceTypeBinding) jdtBinding;
            if (sourceType.scope != null) {
                TypeDeclaration typeDecl = sourceType.scope.referenceContext;
                if (typeDecl instanceof GroovyTypeDeclaration) {
                    groovyDecl = typeDecl;
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
                    FieldNode fNode = fieldBindingToFieldNode(fieldBinding, groovyDecl);
                    addField(fNode);
                }
            }
        } catch (AbortCompilation e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to initialize members for type " + getName(), e);
        }
    }

    @Override
    public boolean mightHaveInners() {
        return (jdtBinding.memberTypes().length != 0);
    }

    /**
     * Convert a JDT MethodBinding to a Groovy MethodNode
     */
    private MethodNode methodBindingToMethodNode(MethodBinding methodBinding) {
        try {
            // FIXASC What value is there in getting the parameter names correct? (for methods and ctors)
            // If they need to be correct we need to retrieve the method decl from the binding scope

            int modifiers = methodBinding.modifiers;
            if (jdtBinding.isInterface() && !Flags.isStatic(modifiers) && !Flags.isSynthetic(modifiers) && /*!Flags.isDefaultMethod(modifiers)*/ (modifiers & 0x10000) == 0) {
                modifiers |= Flags.AccAbstract;
            }

            ClassNode returnType = methodBinding.returnType != null ?
                resolver.convertToClassNode(methodBinding.returnType) : ClassHelper.VOID_TYPE;

            Parameter[] parameters = makeParameters(methodBinding.parameters);

            // initialize parameter annotations
            AnnotationBinding[][] parameterAnnotations = methodBinding.getParameterAnnotations();
            if (parameterAnnotations != null) {
                for (int i = 0, n = parameters.length; i < n; i += 1) {
                    Parameter parameter = parameters[i];
                    AnnotationBinding[] annotations = parameterAnnotations[i];
                    if (annotations != null && annotations.length > 0) {
                        for (AnnotationBinding annotation : annotations) {
                            parameter.addAnnotation(new JDTAnnotationNode(annotation, resolver));
                        }
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

    private Parameter[] makeParameters(TypeBinding[] jdtParameters) {
        int nParameters; Parameter[] parameters = Parameter.EMPTY_ARRAY;
        if (jdtParameters != null && (nParameters = jdtParameters.length) > 0) {
            parameters = new Parameter[nParameters];
            for (int i = 0; i < nParameters; i += 1) {
                parameters[i] = makeParameter(jdtParameters[i], i);
            }
        }
        return parameters;
    }

    private Parameter makeParameter(TypeBinding parameterType, int parameterPosition) {
        TypeBinding erasureType;
        if (parameterType instanceof ParameterizedTypeBinding) {
            erasureType = ((ParameterizedTypeBinding) parameterType).genericType();
        } else {
            erasureType = new JDTClassNodeBuilder(resolver).toRawType(parameterType);
        }
        ClassNode paramType = makeClassNode(parameterType, erasureType);
        String paramName = (parameterPosition < argNames.length ? argNames[parameterPosition] : "arg" + parameterPosition);
        return new Parameter(paramType, paramName);
    }

    /**
     * @param t type
     * @param c erasure of type
     */
    private ClassNode makeClassNode(TypeBinding t, TypeBinding c) {
        ClassNode back = resolver.convertToClassNode(c);
        if (!((t instanceof BinaryTypeBinding) || (t instanceof SourceTypeBinding))) {
            ClassNode front = JDTClassNodeBuilder.build(this.resolver, t);
            front.setRedirect(back);
            return front;
        }
        return back;
    }

    public GenericsType[] getGenericsTypes() {
        ensureGenericsInitialized();
        return genericsTypes;
    }

    @Override
    public boolean isUsingGenerics() {
        ensureGenericsInitialized();
        return super.isUsingGenerics();
    }

    private void ensureGenericsInitialized() {
        if (!anyGenericsInitialized) {
            setupGenerics();
        }
    }

    private ConstructorNode constructorBindingToConstructorNode(MethodBinding methodBinding) {
        TypeVariableBinding[] typeVariables = methodBinding.typeVariables();
        GenericsType[] generics = new JDTClassNodeBuilder(resolver).configureTypeVariables(typeVariables);
        ConstructorNode ctorNode = null;

        int modifiers = methodBinding.modifiers;
        Parameter[] parameters = makeParameters(methodBinding.parameters);
        ClassNode[] thrownExceptions = ClassNode.EMPTY_ARRAY;
        if (methodBinding.thrownExceptions != null) {
            thrownExceptions = new ClassNode[methodBinding.thrownExceptions.length];
            for (int i = 0, n = methodBinding.thrownExceptions.length; i < n; i += 1) {
                thrownExceptions[i] = resolver.convertToClassNode(methodBinding.thrownExceptions[i]);
            }
        }
        ctorNode = new ConstructorNode(modifiers, parameters, thrownExceptions, null);
        ctorNode.setGenericsTypes(generics);
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

    @Override
    public boolean isReallyResolved() {
        return true;
    }

    @Override
    public String getClassInternalName() {
        return getName().replace('.', '/');
    }

    @Override
    public boolean isPrimitive() {
        // FIXASC (M3) verify always true. Think it is a jdtReferenceBinding is a
        // reference binding and not a typebinding
        return false;
    }

    /**
     * Annotations on a JDTClassNode are initialized lazily when requested.
     */
    @Override
    public List<AnnotationNode> getAnnotations() {
        ensureAnnotationsInitialized();
        return super.getAnnotations();
    }

    @Override
    public List<AnnotationNode> getAnnotations(ClassNode type) {
        ensureAnnotationsInitialized();
        return super.getAnnotations(type);
    }

    private synchronized void ensureAnnotationsInitialized() {
        if ((bits & ANNOTATIONS_INITIALIZED) == 0) {
            if ((jdtBinding instanceof SourceTypeBinding)) {
                // ensure resolved
                ((SourceTypeBinding) jdtBinding).getAnnotationTagBits();
            }
            AnnotationBinding[] annotationBindings = jdtBinding.getAnnotations();
            for (AnnotationBinding annotationBinding : annotationBindings) {
                addAnnotation(new JDTAnnotationNode(annotationBinding, this.resolver));
            }
            bits |= ANNOTATIONS_INITIALIZED;
        }
    }

    protected void ensurePropertiesInitialized() {
        if ((bits & PROPERTIES_INITIALIZED) == 0) {
            initializeProperties();
        }
    }

    protected synchronized void initializeProperties() {
        if ((bits & PROPERTIES_INITIALIZED) == 0) {
            lazyClassInit();
            // getX methods
            // make it behave like groovy - no property nodes unless it is groovy source
            if (groovyDecl != null) {
                Set<String> existing = new HashSet<String>();
                for (MethodNode methodNode : getMethods()) {
                    if (isGetter(methodNode)) {
                        // STS-2628 be careful not to double-add properties if there is a getter and an isser variant
                        String propertyName = convertToPropertyName(methodNode.getName());
                        if (!existing.contains(propertyName)) {
                            existing.add(propertyName);
                            // Adding a real field for these accessors can trip up CompileStatic which
                            // will attempt to access it as a real field
                            super.addPropertyWithoutField(createPropertyNodeForMethodNode(methodNode, propertyName));
                            // super.addProperty(createPropertyNodeForMethodNode(methodNode, propertyName));
                        }
                    }
                }
                // fields - FIXASC nyi for fields
                // for (FieldNode fieldNode : getFields()) {
                // super.addProperty(createPropertyNodeFromFieldNode(fieldNode));
                // }
            }
            bits |= PROPERTIES_INITIALIZED;
        }
    }

    private PropertyNode createPropertyNodeForMethodNode(MethodNode methodNode, String propertyName) {
        ClassNode propertyType = methodNode.getReturnType();

        int mods = methodNode.getModifiers();
        FieldNode field = this.getField(propertyName);
        if (field == null) {
            field = new FieldNode(propertyName, mods, propertyType, this, null);
            field.setDeclaringClass(this);
        } else {
            // field already exists
            // must remove this field since when "addProperty" is called
            // later on, it will add it again. We do not want dups.
            this.removeField(propertyName);
        }
        PropertyNode property = new PropertyNode(field, mods, null, null);
        property.setDeclaringClass(this);
        return property;
    }

    /**
     * Converts from a method get/set/is name to a property name.
     * Assumes that methodName is more than 4/3 characters long and starts with a proper prefix.
     */
    private String convertToPropertyName(String methodName) {
        StringBuilder propertyName = new StringBuilder();
        int prefixLen;
        if (methodName.startsWith("is")) {
            prefixLen = 2;
        } else {
            prefixLen = 3;
        }
        propertyName.append(Character.toLowerCase(methodName.charAt(prefixLen)));
        if (methodName.length() > prefixLen + 1) {
            propertyName.append(methodName.substring(prefixLen + 1));
        }
        String name = propertyName.toString();
        return name;
    }

    /**
     * @return {@code true} if the methodNode looks like a setter method for a property:
     *         method starting set<Something> with a void return type and taking one parameter
     */
    @SuppressWarnings("unused")
    private boolean isSetter(MethodNode methodNode) {
        return methodNode.getReturnType() == ClassHelper.VOID_TYPE &&
            methodNode.getParameters().length == 1 &&
            methodNode.getName().startsWith("set") &&
            methodNode.getName().length() > 3;
    }

    /**
     * @return {@code true} if the methodNode looks like a getter method for a property:
     *         method starting get<Something> with a non void return type and taking no parameters
     */
    private boolean isGetter(MethodNode methodNode) {
        return methodNode.getReturnType() != ClassHelper.VOID_TYPE &&
            methodNode.getParameters().length == 0 &&
            ((methodNode.getName().startsWith("get") && methodNode.getName().length() > 3) ||
                (methodNode.getName().startsWith("is") && methodNode.getName().length() > 2));
    }

    @Override
    public List<PropertyNode> getProperties() {
        ensurePropertiesInitialized();
        return super.getProperties();
    }

    @Override
    public PropertyNode getProperty(String name) {
        ensurePropertiesInitialized();
        return super.getProperty(name);
    }

    @Override
    public boolean hasProperty(String name) {
        ensurePropertiesInitialized();
        return super.hasProperty(name);
    }

    @Override
    public void addProperty(PropertyNode node) {
        new RuntimeException("JDTClassNode is immutable, should not be called to add property: " + node.getName()).printStackTrace();
    }

    @Override
    public PropertyNode addProperty(String name, int modifiers, ClassNode type, Expression initialValueExpression, Statement getterBlock, Statement setterBlock) {
        new RuntimeException("JDTClassNode is immutable, should not be called to add property: " + name).printStackTrace();
        return null;
    }

    public ReferenceBinding getJdtBinding() {
        return jdtBinding;
    }

    public JDTResolver getResolver() {
        return resolver;
    }

    public boolean isDeprecated() {
        return jdtBinding.isDeprecated();
    }

    private boolean unfindable = false;

    /**
     * Some AST transforms are written such that they refer to typeClass on a ClassNode.
     * This is not available under Eclipse. However, we can support it in a rudimentary
     * fashion by attempting a class load for the class using the transform loader (if
     * available).
     */
    public Class getTypeClass() {
        if (clazz != null || unfindable) {
            return clazz;
        }
        ClassLoader transformLoader = resolver.compilationUnit.getTransformLoader();
        if (transformLoader != null) {
            // What about array types
            try {
                clazz = Class.forName(this.getName(), false, transformLoader);
                return clazz;
            } catch (ClassNotFoundException e) {
                unfindable = true;
            }
        }
        throw new GroovyBugError("JDTClassNode.getTypeClass() cannot locate class for " + getName() + " using transform loader " + transformLoader);
    }

    // When working with parameterized types, groovy will create a simple ClassNode for the raw type and then initialize the
    // generics structure behind it. This setter is used to ensure that these 'simple' ClassNodes that are created for raw types
    // but are intended to represent parameterized types will have their generics info available (from the parameterized
    // jdt binding).

    public void setJdtBinding(ReferenceBinding parameterizedType) {
        this.jdtBinding = parameterizedType;
    }
}
