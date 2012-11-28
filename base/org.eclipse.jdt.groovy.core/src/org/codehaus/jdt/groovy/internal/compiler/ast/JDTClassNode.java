/*******************************************************************************
 * Copyright (c) 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement        - Initial API and implementation
 *     Andrew Eisenberg - Additional work
 *******************************************************************************/
package org.codehaus.jdt.groovy.internal.compiler.ast;

import java.lang.reflect.Modifier;
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
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
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

/**
 * Groovy can use these to ask questions of JDT bindings. They are only built as required (as groovy references to java files are
 * resolved). They remain uninitialized until groovy starts digging into them - at that time the details are filled in (eg.
 * members).
 * 
 * @author Andy Clement
 */
public class JDTClassNode extends ClassNode implements JDTNode {

	private static final Parameter[] NO_PARAMETERS = new Parameter[0];

	// arbitrary choice of first eight. Maintaining these as a constant array prevents 10000 strings called 'arg0' consuming
	// memory
	private final static String[] argNames = new String[] { "arg0", "arg1", "arg2", "arg3", "arg4", "arg5", "arg6", "arg7" };

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
			return new String(((ArrayBinding) tb).signature());
		} else if (tb instanceof MemberTypeBinding) {
			MemberTypeBinding mtb = (MemberTypeBinding) tb;
			return CharOperation.toString(mtb.compoundName);
		} else if (tb instanceof ReferenceBinding) {
			return CharOperation.toString(((ReferenceBinding) tb).compoundName);
		} else {
			return new String(tb.sourceName());
		}
	}

	private static int getMods(TypeBinding tb) {
		if (tb instanceof ReferenceBinding) {
			return ((ReferenceBinding) tb).modifiers;
		} else {
			// FIXASC need to be smarter here? Who is affected?
			return ClassFileConstants.AccPublic;
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
			if (jdtBinding instanceof ParameterizedTypeBinding && !(jdtBinding instanceof RawTypeBinding)) {
				// GenericsType[] gts = configureTypeArguments(((ParameterizedTypeBinding) jdtBinding).arguments);
				GenericsType[] gts = new JDTClassNodeBuilder(this.resolver)
						.configureTypeArguments(((ParameterizedTypeBinding) jdtBinding).arguments);
				setGenericsTypes(gts);
				// return base;
			} else if (jdtBinding instanceof RawTypeBinding) {
				// nothing to do
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

	// JDTClassNodes are created because of a JDT Reference Binding file so are
	// always 'resolved' (although not initialized on creation)
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
			superInterfaceBindings = superInterfaceBindings == null ? ReferenceBinding.NO_SUPERINTERFACES : superInterfaceBindings;
			ClassNode[] interfaces = new ClassNode[superInterfaceBindings.length];
			for (int i = 0; i < superInterfaceBindings.length; i++) {
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
		MethodBinding[] bindings = null;
		if (jdtBinding instanceof ParameterizedTypeBinding) {
			ReferenceBinding genericType = ((ParameterizedTypeBinding) jdtBinding).genericType();
			bindings = genericType.methods();
		} else {
			bindings = jdtBinding.methods();
		}
		if (bindings != null) {
			for (int i = 0; i < bindings.length; i++) {
				if (bindings[i].isConstructor()) {
					ConstructorNode cNode = constructorBindingToConstructorNode(bindings[i]);
					addConstructor(cNode);
				} else {
					MethodNode mNode = methodBindingToMethodNode(bindings[i]);
					addMethod(mNode);
				}
			}
		}
		if (jdtBinding instanceof BinaryTypeBinding) {
			MethodBinding[] infraBindings = ((BinaryTypeBinding) jdtBinding).infraMethods();
			for (int i = 0; i < infraBindings.length; i++) {
				if (infraBindings[i].isConstructor()) {
					ConstructorNode cNode = constructorBindingToConstructorNode(infraBindings[i]);
					addConstructor(cNode);
				} else {
					MethodNode mNode = methodBindingToMethodNode(infraBindings[i]);
					addMethod(mNode);
				}
			}
		}
		// Synthetic bindings are created for features like covariance, where the method implementing an interface method uses a
		// different return type (interface I { A foo(); } class C implements I { AA foo(); } - this needs a method 'A foo()' in C.
		if (jdtBinding instanceof SourceTypeBinding) {
			SourceTypeBinding jdtSourceTypeBinding = (SourceTypeBinding) jdtBinding;
			ClassScope classScope = jdtSourceTypeBinding.scope;
			// a null scope indicates it has already been 'cleaned up' so nothing to do (CUDeclaration.cleanUp())
			if (classScope != null) {
				CompilationUnitScope cuScope = classScope.compilationUnitScope();
				LookupEnvironment environment = classScope.environment();
				MethodVerifier verifier = environment.methodVerifier();
				cuScope.verifyMethods(verifier);
			}
			SyntheticMethodBinding[] syntheticMethodBindings = ((SourceTypeBinding) jdtBinding).syntheticMethods();
			if (syntheticMethodBindings != null) {
				for (int i = 0; i < syntheticMethodBindings.length; i++) {
					if (syntheticMethodBindings[i].isConstructor()) {
						ConstructorNode cNode = constructorBindingToConstructorNode(bindings[i]);
						addConstructor(cNode);
					} else {
						MethodNode mNode = methodBindingToMethodNode(syntheticMethodBindings[i]);
						addMethod(mNode);
					}
				}
			}
		}

		FieldBinding[] fieldBindings = null;
		if (jdtBinding instanceof ParameterizedTypeBinding) {
			fieldBindings = ((ParameterizedTypeBinding) jdtBinding).genericType().fields();
		} else {
			fieldBindings = jdtBinding.fields();
		}
		if (fieldBindings != null) {
			for (int i = 0; i < fieldBindings.length; i++) {
				FieldNode fNode = fieldBindingToFieldNode(fieldBindings[i], groovyDecl);
				addField(fNode);
			}
		}
	}

	@Override
	public boolean mightHaveInners() {
		// return super.hasInnerClasses();
		return jdtBinding.memberTypes().length != 0;
	}

	/**
	 * Convert a JDT MethodBinding to a Groovy MethodNode
	 */
	private MethodNode methodBindingToMethodNode(MethodBinding methodBinding) {
		String name = new String(methodBinding.selector);

		TypeVariableBinding[] typeVariables = methodBinding.typeVariables();

		GenericsType[] generics = new JDTClassNodeBuilder(resolver).configureTypeVariables(typeVariables);
		MethodNode mNode = null;

		// FIXASC What value is there in getting the parameter names correct? (for methods and ctors)
		// If they need to be correct we need to retrieve the method decl from the binding scope
		int modifiers = methodBinding.modifiers;
		if (jdtBinding.isInterface()) {
			modifiers |= Modifier.ABSTRACT;
		}
		ClassNode returnType = resolver.convertToClassNode(methodBinding.returnType);

		methodBinding.genericSignature();
		Parameter[] gParameters = makeParameters(methodBinding.parameters);

		ClassNode[] thrownExceptions = new ClassNode[0]; // FIXASC use constant of size 0
		if (methodBinding.thrownExceptions != null) {
			thrownExceptions = new ClassNode[methodBinding.thrownExceptions.length];
			for (int i = 0; i < methodBinding.thrownExceptions.length; i++) {
				thrownExceptions[i] = resolver.convertToClassNode(methodBinding.thrownExceptions[i]);
			}
		}
		mNode = new JDTMethodNode(methodBinding, resolver, name, modifiers, returnType, gParameters, thrownExceptions, null);

		// FIXASC (M3) likely to need something like this...
		// if (jdtBinding.isEnum()) {
		// if (methodBinding.getDefaultValue() != null) {
		// mNode.setAnnotationDefault(true);
		// }
		// }

		mNode.setGenericsTypes(generics);
		return mNode;
	}

	private Parameter[] makeParameters(TypeBinding[] jdtParameters) {
		Parameter[] params = NO_PARAMETERS;
		if (jdtParameters != null && jdtParameters.length > 0) {
			params = new Parameter[jdtParameters.length];
			for (int i = 0; i < params.length; i++) {
				params[i] = makeParameter(jdtParameters[i], i);
			}
		}
		return params;
	}

	private Parameter makeParameter(TypeBinding parameterType, int paramNumber) {
		TypeBinding clazz = null;
		if (parameterType instanceof ParameterizedTypeBinding) {
			clazz = ((ParameterizedTypeBinding) parameterType).genericType();
		} else {
			clazz = new JDTClassNodeBuilder(resolver).toRawType(parameterType);
		}
		ClassNode paramType = makeClassNode(parameterType, clazz);
		String paramName = (paramNumber < 8 ? argNames[paramNumber] : "arg" + paramNumber);
		return new Parameter(paramType, paramName);
	}

	/**
	 * 
	 * @param type
	 * @param cl erasure of type
	 * @return
	 */
	private ClassNode makeClassNode(TypeBinding t, TypeBinding c) {
		// was:
		// return resolver.convertToClassNode(type);
		ClassNode back = null;
		// This line would check the compile unit
		// if (cu != null) back = cu.getClass(c.getName());
		if (back == null)
			back = resolver.convertToClassNode(c);// ClassHelper.make(c);
		if (!((t instanceof BinaryTypeBinding) || (t instanceof SourceTypeBinding))) {
			ClassNode front = JDTClassNodeBuilder.build(this.resolver, t);
			front.setRedirect(back);
			return front;
		}
		return back;// .getPlainNodeReference();
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
		ClassNode[] thrownExceptions = new ClassNode[0];
		if (methodBinding.thrownExceptions != null) {
			thrownExceptions = new ClassNode[methodBinding.thrownExceptions.length];
			for (int i = 0; i < methodBinding.thrownExceptions.length; i++) {
				thrownExceptions[i] = resolver.convertToClassNode(methodBinding.thrownExceptions[i]);
			}
		}
		ctorNode = new ConstructorNode(modifiers, parameters, thrownExceptions, null);

		ctorNode.setGenericsTypes(generics);
		return ctorNode;
	}

	private FieldNode fieldBindingToFieldNode(FieldBinding fieldBinding, TypeDeclaration groovyTypeDecl) {
		String name = new String(fieldBinding.name);
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

	// FIXASC Need to override anything else from the supertype?

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
		if ((bits & ANNOTATIONS_INITIALIZED) == 0) {
			ensureAnnotationsInitialized();
		}
		return super.getAnnotations();
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
	 * Converts from a method get/set/is name to a property name Assumes that methodName is more than 4/3 characters long and starts
	 * with a proper prefix
	 * 
	 * @param methodNode
	 * @return
	 */
	private String convertToPropertyName(String methodName) {
		StringBuffer propertyName = new StringBuffer();
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
	 * @return true if the methodNode looks like a setter method for a property: method starting set<Something> with a void return
	 *         type and taking one parameter
	 */
	private boolean isSetter(MethodNode methodNode) {
		return methodNode.getReturnType() == ClassHelper.VOID_TYPE && methodNode.getParameters().length == 1
				&& methodNode.getName().startsWith("set") && methodNode.getName().length() > 3;
	}

	/**
	 * @return true if the methodNode looks like a getter method for a property: method starting get<Something> with a non void
	 *         return type and taking no parameters
	 */
	private boolean isGetter(MethodNode methodNode) {
		return methodNode.getReturnType() != ClassHelper.VOID_TYPE
				&& methodNode.getParameters().length == 0
				&& ((methodNode.getName().startsWith("get") && methodNode.getName().length() > 3) || (methodNode.getName()
						.startsWith("is") && methodNode.getName().length() > 2));
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
		new RuntimeException("JDTClassNode is immutable, should not be called to add property: " + node.getName())
				.printStackTrace();
	}

	@Override
	public PropertyNode addProperty(String name, int modifiers, ClassNode type, Expression initialValueExpression,
			Statement getterBlock, Statement setterBlock) {
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
	 * Some AST transforms are written such that they refer to typeClass on a ClassNode. This is not available under Eclipse.
	 * However, we can support it in a rudimentary fashion by attempting a class load for the class using the transform loader (if
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
		throw new GroovyBugError("JDTClassNode.getTypeClass() cannot locate class for " + getName() + " using transform loader "
				+ transformLoader);
	}

	// When working with parameterized types, groovy will create a simple ClassNode for the raw type and then initialize the
	// generics structure behind it. This setter is used to ensure that these 'simple' ClassNodes that are created for raw types
	// but are intended to represent parameterized types will have their generics info available (from the parameterized
	// jdt binding).

	public void setJdtBinding(ReferenceBinding parameterizedType) {
		this.jdtBinding = parameterizedType;
	}
}