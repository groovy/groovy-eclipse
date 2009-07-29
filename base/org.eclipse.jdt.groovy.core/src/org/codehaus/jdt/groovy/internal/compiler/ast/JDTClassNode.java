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
import java.util.List;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MemberTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.WildcardBinding;

/**
 * Groovy can use these to ask questions of JDT bindings. They are only built as required (as groovy references to java files are
 * resolved). They remain uninitialized until groovy starts digging into them - at that time the details are filled in (eg.
 * members).
 * 
 * @author Andy Clement
 */
@SuppressWarnings("restriction")
public class JDTClassNode extends ClassNode {

	private static final Parameter[] NO_PARAMETERS = new Parameter[0];

	// arbitrary choice of first eight. Maintaining these as a constant array prevents 10000 strings called 'arg0' consuming
	// memory
	private final static String[] argNames = new String[] { "arg0", "arg1", "arg2", "arg3", "arg4", "arg5", "arg6", "arg7" };

	// The binding which this JDTClassNode represents
	ReferenceBinding jdtBinding;

	// The resolver instance involved at the moment
	JDTResolver resolver;

	// Configuration flags
	private int bits = 0;
	private static final int ANNOTATIONS_INITIALIZED = 0x001;

	static final ClassNode unboundWildcard; // represents plain old '?'

	static {
		ClassNode base = ClassHelper.makeWithoutCaching("?");
		ClassNode[] allUppers = new ClassNode[] { ClassHelper.OBJECT_TYPE };
		GenericsType t = new GenericsType(base, allUppers, null);
		t.setWildcard(true);
		// Can't use the constant ClassHelper.OBJECT_TYPE here as we are about to setGenericTypes on it.
		unboundWildcard = new ClassNode(Object.class);// ClassHelper.makeWithoutCaching(Object.class, false);
		unboundWildcard.setGenericsTypes(new GenericsType[] { t });
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
			// FIXASC (M2) need to be smarter here? Who is affected?
			return ClassFileConstants.AccPublic;
		}
	}

	@Override
	protected void lazyClassInit() {
		synchronized (lazyInitLock) {
			if (lazyInitDone) {
				return;
			}
			initialize();
			lazyInitDone = true;
		}
	}

	public void setupGenerics() {
		if (jdtBinding instanceof ParameterizedTypeBinding) { // Includes RawTB
			ParameterizedTypeBinding ptb = (ParameterizedTypeBinding) jdtBinding;
			TypeBinding[] parameterizationArguments = ptb.arguments;
			if (parameterizationArguments != null && parameterizationArguments.length > 0) {
				GenericsType[] generics = new GenericsType[parameterizationArguments.length];
				for (int g = 0; g < parameterizationArguments.length; g++) {
					TypeBinding typeBinding = parameterizationArguments[g];
					if (typeBinding instanceof TypeVariableBinding) {
						generics[g] = createGenericsTypeInfoForTypeVariableBinding((TypeVariableBinding) parameterizationArguments[g]);
					} else {
						// minor optimization for the case of the unbound wildcard '?'
						if (typeBinding instanceof WildcardBinding && ((WildcardBinding) typeBinding).boundKind == Wildcard.UNBOUND) {
							generics[g] = new GenericsType(JDTClassNode.unboundWildcard);
						} else {
							generics[g] = new GenericsType(resolver.convertToClassNode(typeBinding));
						}
					}
				}
				this.setGenericsTypes(generics);
			}
		} else {
			// SourceTB, BinaryTB, TypeVariableB, WildcardB
			TypeVariableBinding[] typeVariables = jdtBinding.typeVariables();
			GenericsType[] generics = createGenericsTypeInfoForTypeVariableBindings(typeVariables);
			if (generics != null) {
				this.setGenericsTypes(generics);
			}
		}
	}

	// JDTClassNodes are created because of a JDT Reference Binding file so are
	// always 'resolved' (although not initialized on creation)
	@Override
	public boolean isResolved() {
		return true;
	}

	/**
	 * Basic initialization of the node - try and do most resolution lazily but some elements are worth getting correct up front:
	 * superclass, superinterfaces
	 */
	// FIXASC (M2) confusing (and problematic?) that the superclass is setup after the generics information
	void initialize() {
		resolver.pushTypeGenerics(getGenericsTypes());
		ReferenceBinding superClass = jdtBinding.superclass();
		if (superClass != null) {
			setUnresolvedSuperClass(resolver.convertToClassNode(superClass));
		}

		ReferenceBinding[] superInterfaceBindings = jdtBinding.superInterfaces();
		ClassNode[] interfaces = new ClassNode[superInterfaceBindings.length];
		for (int i = 0; i < superInterfaceBindings.length; i++) {
			interfaces[i] = resolver.convertToClassNode(superInterfaceBindings[i]);
		}
		setInterfaces(interfaces);
		initializeMethodsAndConstructors();
		resolver.popTypeGenerics();
	}

	private void initializeMethodsAndConstructors() {
		MethodBinding[] bindings = jdtBinding.methods();
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

		FieldBinding[] fieldBindings = jdtBinding.fields();
		if (fieldBindings != null) {
			for (int i = 0; i < fieldBindings.length; i++) {
				FieldNode fNode = fieldBindingToFieldNode(fieldBindings[i]);
				addField(fNode);
			}
		}
	}

	/**
	 * Convert a JDT MethodBinding to a Groovy MethodNode
	 */
	private MethodNode methodBindingToMethodNode(MethodBinding methodBinding) {
		String name = new String(methodBinding.selector);

		TypeVariableBinding[] typeVariables = methodBinding.typeVariables();
		GenericsType[] generics = createGenericsTypeInfoForTypeVariableBindings(typeVariables);
		MethodNode mNode = null;
		try {
			resolver.pushMemberGenerics(generics);
			// FIXASC (M2) What value is there in getting the parameter names correct? (for methods and ctors)
			// If they need to be correct we need to retrieve the method decl from the binding scope
			int modifiers = methodBinding.modifiers;
			if (jdtBinding.isInterface()) {
				modifiers |= Modifier.ABSTRACT;
			}
			ClassNode returnType = resolver.convertToClassNode(methodBinding.returnType);
			Parameter[] gParameters = convertJdtParametersToGroovyParameters(methodBinding.parameters);
			ClassNode[] thrownExceptions = new ClassNode[0];
			if (methodBinding.thrownExceptions != null) {
				thrownExceptions = new ClassNode[methodBinding.thrownExceptions.length];
				for (int i = 0; i < methodBinding.thrownExceptions.length; i++) {
					thrownExceptions[i] = resolver.convertToClassNode(methodBinding.thrownExceptions[i]);
				}
			}
			mNode = new MethodNode(name, modifiers, returnType, gParameters, thrownExceptions, null);
		} finally {
			resolver.popMemberGenerics();
		}
		mNode.setGenericsTypes(generics);
		return mNode;
	}

	private Parameter[] convertJdtParametersToGroovyParameters(TypeBinding[] jdtParameters) {
		Parameter[] gParameters = NO_PARAMETERS;
		if (jdtParameters != null && jdtParameters.length > 0) {
			gParameters = new Parameter[jdtParameters.length];
			// optimized form the loop below if we know we won't run out
			if (jdtParameters.length < 8) {
				for (int i = 0; i < jdtParameters.length; i++) {
					ClassNode paramType = resolver.convertToClassNode(jdtParameters[i]);
					gParameters[i] = new Parameter(paramType, argNames[i]);
				}
			} else {
				for (int i = 0; i < jdtParameters.length; i++) {
					ClassNode c2 = resolver.convertToClassNode(jdtParameters[i]);
					if (i < 8) {
						gParameters[i] = new Parameter(c2, argNames[i]);
					} else {
						gParameters[i] = new Parameter(c2, "arg" + i);
					}
				}
			}
		}
		return gParameters;
	}

	private ConstructorNode constructorBindingToConstructorNode(MethodBinding methodBinding) {
		TypeVariableBinding[] typeVariables = methodBinding.typeVariables();
		GenericsType[] generics = createGenericsTypeInfoForTypeVariableBindings(typeVariables);
		ConstructorNode ctorNode = null;
		try {
			resolver.pushMemberGenerics(generics);
			int modifiers = methodBinding.modifiers;
			Parameter[] parameters = convertJdtParametersToGroovyParameters(methodBinding.parameters);
			ClassNode[] thrownExceptions = new ClassNode[0];
			if (methodBinding.thrownExceptions != null) {
				thrownExceptions = new ClassNode[methodBinding.thrownExceptions.length];
				for (int i = 0; i < methodBinding.thrownExceptions.length; i++) {
					thrownExceptions[i] = resolver.convertToClassNode(methodBinding.thrownExceptions[i]);
				}
			}
			ctorNode = new ConstructorNode(modifiers, parameters, thrownExceptions, null);
		} finally {
			resolver.popMemberGenerics();
		}
		ctorNode.setGenericsTypes(generics);
		return ctorNode;
	}

	private FieldNode fieldBindingToFieldNode(FieldBinding fieldBinding) {
		String name = new String(fieldBinding.name);
		int modifiers = fieldBinding.modifiers;
		ClassNode fieldType = resolver.convertToClassNode(fieldBinding.type);
		FieldNode fNode = new FieldNode(name, modifiers, fieldType, this, null);
		return fNode;
	}

	// FIXASC (M2) Need to override anything else from the supertype?

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

	private GenericsType[] createGenericsTypeInfoForTypeVariableBindings(TypeVariableBinding... typeVariables) {
		if (typeVariables == null || typeVariables.length == 0) {
			return null;
		}
		// FIXASC (M2) anything for RAW types here?
		GenericsType[] genericTypeInfo = new GenericsType[typeVariables.length];
		for (int g = 0; g < typeVariables.length; g++) {
			genericTypeInfo[g] = createGenericsTypeInfoForTypeVariableBinding(typeVariables[g]);
		}
		return genericTypeInfo;
	}

	private GenericsType createGenericsTypeInfoForTypeVariableBinding(TypeVariableBinding typeVariableBinding) {
		// By creating something 'lazy' we can avoid self referential initialization problems like
		// "T extends Foo & Comparable<? super T>"
		GenericsType gt = new LazyGenericsType(typeVariableBinding, resolver);
		return gt;
	}

	/**
	 * Annotations on a JDTClassNode are initialized lazily when requested.
	 */
	@Override
	public List<AnnotationNode> getAnnotations() {
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
		return super.getAnnotations();
	}

}