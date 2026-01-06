// GROOVY PATCHED
/*******************************************************************************
 * Copyright (c) 2000, 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contributions for
 *								bug 328281 - visibility leaks not detected when analyzing unused field in private class
 *								bug 349326 - [1.7] new warning for missing try-with-resources
 *								bug 186342 - [compiler][null] Using annotations for null checking
 *								bug 365836 - [compiler][null] Incomplete propagation of null defaults.
 *								bug 365519 - editorial cleanup after bug 186342 and bug 365387
 *								bug 365662 - [compiler][null] warn on contradictory and redundant null annotations
 *								bug 365531 - [compiler][null] investigate alternative strategy for internally encoding nullness defaults
 *								bug 366063 - Compiler should not add synthetic @NonNull annotations
 *								bug 384663 - Package Based Annotation Compilation Error in JDT 3.8/4.2 (works in 3.7.2)
 *								bug 386356 - Type mismatch error with annotations and generics
 *								bug 388281 - [compiler][null] inheritance of null annotations as an option
 *								bug 331649 - [compiler][null] consider null annotations for fields
 *								bug 380896 - [compiler][null] Enum constants not recognised as being NonNull.
 *								bug 391376 - [1.8] check interaction of default methods with bridge methods and generics
 *								Bug 392099 - [1.8][compiler][null] Apply null annotation on types for null analysis
 *								Bug 415043 - [1.8][null] Follow-up re null type annotations after bug 392099
 *								Bug 392238 - [1.8][compiler][null] Detect semantically invalid null type annotations
 *								Bug 415850 - [1.8] Ensure RunJDTCoreTests can cope with null annotations enabled
 *								Bug 416172 - [1.8][compiler][null] null type annotation not evaluated on method return type
 *								Bug 417295 - [1.8[[null] Massage type annotated null analysis to gel well with deep encoded type bindings.
 *								Bug 426048 - [1.8] NPE in TypeVariableBinding.internalBoundCheck when parentheses are not balanced
 *								Bug 392238 - [1.8][compiler][null] Detect semantically invalid null type annotations
 *								Bug 429958 - [1.8][null] evaluate new DefaultLocation attribute of @NonNullByDefault
 *								Bug 432348 - [1.8] Internal compiler error (NPE) after upgrade to 1.8
 *								Bug 438458 - [1.8][null] clean up handling of null type annotations wrt type variables
 *								Bug 435570 - [1.8][null] @NonNullByDefault illegally tries to affect "throws E"
 *								Bug 441693 - [1.8][null] Bogus warning for type argument annotated with @NonNull
 *								Bug 435805 - [1.8][compiler][null] Java 8 compiler does not recognize declaration style null annotations
 *								Bug 457210 - [1.8][compiler][null] Wrong Nullness errors given on full build build but not on incremental build?
 *								Bug 461250 - ArrayIndexOutOfBoundsException in SourceTypeBinding.fields
 *								Bug 466713 - Null Annotations: NullPointerException using <int @Nullable []> as Type Param
 *      Jesper S Moller <jesper@selskabet.org> -  Contributions for
 *								Bug 412153 - [1.8][compiler] Check validity of annotations which may be repeatable
 *      Till Brychcy - Contributions for
 *     							bug 415269 - NonNullByDefault is not always inherited to nested classes
 *      Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *                          	Bug 405104 - [1.8][compiler][codegen] Implement support for serializeable lambdas
 *      Sebastian Zarnekow - Contributions for
 *								bug 544921 - [performance] Poor performance with large source files
 *      Christoph Läubrich - Contributions for
 *								Issue 674 - Enhance the BuildContext with the discovered annotations
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.ast.TypeReference.AnnotationPosition;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ExternalAnnotationProvider;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.Util;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class SourceTypeBinding extends ReferenceBinding {
	public ReferenceBinding superclass;                    // MUST NOT be modified directly, use setter !
	public ReferenceBinding[] superInterfaces;             // MUST NOT be modified directly, use setter !
	private FieldBinding[] fields;                         // MUST NOT be modified directly, use setter !
	/*package*/ RecordComponentBinding[] components;       // MUST NOT be modified directly, use setter !
	private MethodBinding[] methods;                       // MUST NOT be modified directly, use setter !
	public ReferenceBinding[] memberTypes;                 // MUST NOT be modified directly, use setter !
	public TypeVariableBinding[] typeVariables;            // MUST NOT be modified directly, use setter !
	public ReferenceBinding[] permittedTypes;              // MUST NOT be modified directly, use setter !

	public ClassScope scope;
	protected SourceTypeBinding prototype;
	LookupEnvironment environment;
	public ModuleBinding module;
	// Synthetics are separated into 2 categories: methods & fields
	// if a new category is added, also increment MAX_SYNTHETICS
	private final static int METHOD_EMUL    = 0; // value type: SyntheticMethodBinding[]
	private final static int FIELD_EMUL     = 1; // value type: FieldBinding
	private final static int MAX_SYNTHETICS = 2;

	Map[] synthetics;
	char[] genericReferenceTypeSignature;

	private Map<Binding, AnnotationHolder> storedAnnotations; // keys are this ReferenceBinding & its fields and methods, value is an AnnotationHolder

	public int defaultNullness;
	boolean memberTypesSorted;
	private int nullnessDefaultInitialized = 0; // 0: nothing; 1: type; 2: package
	private ReferenceBinding containerAnnotationType;

	public ExternalAnnotationProvider externalAnnotationProvider;

	private SourceTypeBinding nestHost;
	private Set<SourceTypeBinding> nestMembers;

	public boolean isImplicit;
	public boolean supertypeAnnotationsUpdated; // have any supertype annotations been updated during CompleteTypeBindingsSteps.INTEGRATE_ANNOTATIONS_IN_HIERARCHY?

public SourceTypeBinding(char[][] compoundName, PackageBinding fPackage, ClassScope scope) {
	this.compoundName = compoundName;
	this.fPackage = fPackage;
	this.fileName = scope.referenceCompilationUnit().getFileName();
	this.modifiers = scope.referenceContext.modifiers;
	this.sourceName = scope.referenceContext.name;
	this.scope = scope;
	this.environment = scope.environment();

	// expect the fields & methods to be initialized correctly later
	this.fields = Binding.UNINITIALIZED_FIELDS;
	this.methods = Binding.UNINITIALIZED_METHODS;
	this.components = this.isRecord() ? Binding.UNINITIALIZED_COMPONENTS : NO_COMPONENTS;
	this.prototype = this;
	this.isImplicit = scope.referenceContext.isImplicitType();
	computeId();
}

public SourceTypeBinding(SourceTypeBinding prototype) {
	super(prototype);

	this.prototype = prototype.prototype;
	this.tagBits &= ~TagBits.HasAnnotatedVariants;
	this.prototype.tagBits |= TagBits.HasAnnotatedVariants;
	this.tagBits |= TagBits.HasUnresolvedMemberTypes; // see memberTypes()

	this.superclass = prototype.superclass;
	this.superInterfaces = prototype.superInterfaces;
	this.permittedTypes = prototype.permittedTypes;
	this.fields = prototype.fields;
	this.methods = prototype.methods;
	this.components = prototype.components;
	this.memberTypes = prototype.memberTypes;
	this.typeVariables = prototype.typeVariables;
	this.environment = prototype.environment;

	this.scope = prototype.scope; // compensated by TypeSystem.cleanUp(int)

	this.synthetics = prototype.synthetics;
	this.genericReferenceTypeSignature = prototype.genericReferenceTypeSignature;
	this.storedAnnotations = prototype.storedAnnotations;
	this.defaultNullness = prototype.defaultNullness;
	this.nullnessDefaultInitialized= prototype.nullnessDefaultInitialized;
	this.containerAnnotationType = prototype.containerAnnotationType;
	this.isImplicit = this.prototype.isImplicit;
}

/**
 * Adds a new synthetic field for {@code <actualOuterLocalVariable>}. Answers the
 * new field or the existing field if one already existed.
 */
public FieldBinding addSyntheticFieldForInnerclass(LocalVariableBinding actualOuterLocalVariable) {
	if (!isPrototype()) throw new IllegalStateException();

	if (this.synthetics == null)
		this.synthetics = new Map[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.FIELD_EMUL] == null)
		this.synthetics[SourceTypeBinding.FIELD_EMUL] = new LinkedHashMap(5);

	FieldBinding synthField = (FieldBinding) this.synthetics[SourceTypeBinding.FIELD_EMUL].get(actualOuterLocalVariable);
	if (synthField == null) {
		synthField = new SyntheticFieldBinding(
			CharOperation.concat(TypeConstants.SYNTHETIC_OUTER_LOCAL_PREFIX, actualOuterLocalVariable.name),
			actualOuterLocalVariable.type,
			ClassFileConstants.AccPrivate | ClassFileConstants.AccFinal | ClassFileConstants.AccSynthetic,
			this,
			Constant.NotAConstant);
		this.synthetics[SourceTypeBinding.FIELD_EMUL].put(actualOuterLocalVariable, synthField);
	}

	// ensure there is not already such a field defined by the user
	boolean needRecheck;
	int index = 1;
	do {
		needRecheck = false;
		FieldBinding existingField;
		if ((existingField = getField(synthField.name, true /*resolve*/)) != null) {
			TypeDeclaration typeDecl = this.scope.referenceContext;
			FieldDeclaration[] fieldDeclarations = typeDecl.fields;
			int max = fieldDeclarations == null ? 0 : fieldDeclarations.length;
			for (int i = 0; i < max; i++) {
				FieldDeclaration fieldDecl = fieldDeclarations[i];
				if (fieldDecl.binding == existingField) {
					synthField.name = CharOperation.concat(
						TypeConstants.SYNTHETIC_OUTER_LOCAL_PREFIX,
						actualOuterLocalVariable.name,
						("$" + String.valueOf(index++)).toCharArray()); //$NON-NLS-1$
					needRecheck = true;
					break;
				}
			}
		}
	} while (needRecheck);
	return synthField;
}

/**
 * Adds a new synthetic field for {@code <enclosingType>}. Answers the new field
 * or the existing field if one already existed.
 */
public FieldBinding addSyntheticFieldForInnerclass(ReferenceBinding enclosingType) {
	if (!isPrototype()) throw new IllegalStateException();

	if (this.synthetics == null)
		this.synthetics = new Map[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.FIELD_EMUL] == null)
		this.synthetics[SourceTypeBinding.FIELD_EMUL] = new LinkedHashMap(5);

	FieldBinding synthField = (FieldBinding) this.synthetics[SourceTypeBinding.FIELD_EMUL].get(enclosingType);
	if (synthField == null) {
		synthField = new SyntheticFieldBinding(
			CharOperation.concat(
				TypeConstants.SYNTHETIC_ENCLOSING_INSTANCE_PREFIX,
				String.valueOf(enclosingType.depth()).toCharArray()),
			enclosingType,
			ClassFileConstants.AccDefault | ClassFileConstants.AccFinal | ClassFileConstants.AccSynthetic,
			this,
			Constant.NotAConstant);
		this.synthetics[SourceTypeBinding.FIELD_EMUL].put(enclosingType, synthField);
	}
	// ensure there is not already such a field defined by the user
	boolean needRecheck;
	do {
		needRecheck = false;
		FieldBinding existingField;
		if ((existingField = getField(synthField.name, true /*resolve*/)) != null) {
			TypeDeclaration typeDecl = this.scope.referenceContext;
			FieldDeclaration[] fieldDeclarations = typeDecl.fields;
			int max = fieldDeclarations == null ? 0 : fieldDeclarations.length;
			for (int i = 0; i < max; i++) {
				FieldDeclaration fieldDecl = fieldDeclarations[i];
				if (fieldDecl.binding == existingField) {
					synthField.name = CharOperation.concat(
						synthField.name,
						"$".toCharArray()); //$NON-NLS-1$
					needRecheck = true;
					break;
				}
			}
		}
	} while (needRecheck);
	return synthField;
}

/**
 * Adds a new synthetic instance field corresponding to a component in the record header
 * Any clash with existing fields to be dealt with at call site
 */
public void addSyntheticRecordState(RecordComponent component, FieldBinding synthField) {
	if (!isPrototype()) throw new IllegalStateException();

	if (this.synthetics == null)
		this.synthetics = new Map[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.FIELD_EMUL] == null)
		this.synthetics[SourceTypeBinding.FIELD_EMUL] = new LinkedHashMap(5);

	if (component.binding != null) {
		synthField.modifiers |= component.binding.modifiers & ExtraCompilerModifiers.AccGenericSignature;
		if ((component.binding.tagBits & TagBits.HasMissingType) != 0)
			synthField.tagBits |= TagBits.HasMissingType;
	}
	if (component.annotations != null)
		ASTNode.copyRecordComponentAnnotations(this.scope, synthField, component.annotations);

	this.synthetics[SourceTypeBinding.FIELD_EMUL].put(component, synthField);
}

/**
 * Adds a new synthetic field for the emulation of the assert statement. Answers
 * the new field or the existing field if one already existed.
 */
public FieldBinding addSyntheticFieldForAssert(BlockScope blockScope) {
	if (!isPrototype()) throw new IllegalStateException();

	if (this.synthetics == null)
		this.synthetics = new Map[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.FIELD_EMUL] == null)
		this.synthetics[SourceTypeBinding.FIELD_EMUL] = new LinkedHashMap(5);

	FieldBinding synthField = (FieldBinding) this.synthetics[SourceTypeBinding.FIELD_EMUL].get("assertionEmulation"); //$NON-NLS-1$
	if (synthField == null) {
		synthField = new SyntheticFieldBinding(
			TypeConstants.SYNTHETIC_ASSERT_DISABLED,
			TypeBinding.BOOLEAN,
			(isInterface() ? ClassFileConstants.AccPublic : ClassFileConstants.AccDefault) | ClassFileConstants.AccStatic | ClassFileConstants.AccSynthetic | ClassFileConstants.AccFinal,
			this,
			Constant.NotAConstant);
		this.synthetics[SourceTypeBinding.FIELD_EMUL].put("assertionEmulation", synthField); //$NON-NLS-1$
	}
	// ensure there is not already such a field defined by the user
	// ensure there is not already such a field defined by the user
	boolean needRecheck;
	int index = 0;
	do {
		needRecheck = false;
		FieldBinding existingField;
		if ((existingField = getField(synthField.name, true /*resolve*/)) != null) {
			TypeDeclaration typeDecl = this.scope.referenceContext;
			int max = (typeDecl.fields == null) ? 0 : typeDecl.fields.length;
			for (int i = 0; i < max; i++) {
				FieldDeclaration fieldDecl = typeDecl.fields[i];
				if (fieldDecl.binding == existingField) {
					synthField.name = CharOperation.concat(
						TypeConstants.SYNTHETIC_ASSERT_DISABLED,
						("_" + String.valueOf(index++)).toCharArray()); //$NON-NLS-1$
					needRecheck = true;
					break;
				}
			}
		}
	} while (needRecheck);
	return synthField;
}

/**
 * Adds a new synthetic field for recording all enum constant values. Answers the
 * new field or the existing field if one already existed.
 */
public FieldBinding addSyntheticFieldForEnumValues() {
	if (!isPrototype()) throw new IllegalStateException();

	if (this.synthetics == null)
		this.synthetics = new Map[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.FIELD_EMUL] == null)
		this.synthetics[SourceTypeBinding.FIELD_EMUL] = new LinkedHashMap(5);

	FieldBinding synthField = (FieldBinding) this.synthetics[SourceTypeBinding.FIELD_EMUL].get("enumConstantValues"); //$NON-NLS-1$
	if (synthField == null) {
		synthField = new SyntheticFieldBinding(
			TypeConstants.SYNTHETIC_ENUM_VALUES,
			this.scope.createArrayType(this,1),
			ClassFileConstants.AccPrivate | ClassFileConstants.AccStatic | ClassFileConstants.AccSynthetic | ClassFileConstants.AccFinal,
			this,
			Constant.NotAConstant);
		this.synthetics[SourceTypeBinding.FIELD_EMUL].put("enumConstantValues", synthField); //$NON-NLS-1$
	}
	// ensure there is not already such a field defined by the user
	// ensure there is not already such a field defined by the user
	boolean needRecheck;
	int index = 0;
	do {
		needRecheck = false;
		FieldBinding existingField;
		if ((existingField = getField(synthField.name, true /*resolve*/)) != null) {
			TypeDeclaration typeDecl = this.scope.referenceContext;
			FieldDeclaration[] fieldDeclarations = typeDecl.fields;
			int max = fieldDeclarations == null ? 0 : fieldDeclarations.length;
			for (int i = 0; i < max; i++) {
				FieldDeclaration fieldDecl = fieldDeclarations[i];
				if (fieldDecl.binding == existingField) {
					synthField.name = CharOperation.concat(
						TypeConstants.SYNTHETIC_ENUM_VALUES,
						("_" + String.valueOf(index++)).toCharArray()); //$NON-NLS-1$
					needRecheck = true;
					break;
				}
			}
		}
	} while (needRecheck);
	return synthField;
}

/**
 * Adds a new synthetic access method for read/write access to {@code <targetField>}.
 * Answers the new method or the existing method if one already existed.
 */
public SyntheticMethodBinding addSyntheticMethod(FieldBinding targetField, boolean isReadAccess, boolean isSuperAccess) {
	if (!isPrototype()) throw new IllegalStateException();

	if (this.synthetics == null)
		this.synthetics = new Map[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.METHOD_EMUL] == null)
		this.synthetics[SourceTypeBinding.METHOD_EMUL] = new LinkedHashMap(5);

	SyntheticMethodBinding accessMethod = null;
	SyntheticMethodBinding[] accessors = (SyntheticMethodBinding[]) this.synthetics[SourceTypeBinding.METHOD_EMUL].get(targetField);
	if (accessors == null) {
		accessMethod = new SyntheticMethodBinding(targetField, isReadAccess, isSuperAccess, this);
		this.synthetics[SourceTypeBinding.METHOD_EMUL].put(targetField, accessors = new SyntheticMethodBinding[2]);
		accessors[isReadAccess ? 0 : 1] = accessMethod;
	} else {
		if ((accessMethod = accessors[isReadAccess ? 0 : 1]) == null) {
			accessMethod = new SyntheticMethodBinding(targetField, isReadAccess, isSuperAccess, this);
			accessors[isReadAccess ? 0 : 1] = accessMethod;
		}
	}
	return accessMethod;
}

/**
 * Adds a new synthetic method the enum type. Selector can either be 'values' or
 * 'valueOf'. {@code char[]} constants from {@link TypeConstants} must be used:
 * {@code TypeConstants.VALUES/VALUEOF}.
 */
public SyntheticMethodBinding addSyntheticEnumMethod(char[] selector) {
	if (!isPrototype()) throw new IllegalStateException();

	if (this.synthetics == null)
		this.synthetics = new Map[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.METHOD_EMUL] == null)
		this.synthetics[SourceTypeBinding.METHOD_EMUL] = new LinkedHashMap(5);

	SyntheticMethodBinding accessMethod = null;
	SyntheticMethodBinding[] accessors = (SyntheticMethodBinding[]) this.synthetics[SourceTypeBinding.METHOD_EMUL].get(selector);
	if (accessors == null) {
		accessMethod = new SyntheticMethodBinding(this, selector);
		this.synthetics[SourceTypeBinding.METHOD_EMUL].put(selector, accessors = new SyntheticMethodBinding[2]);
		accessors[0] = accessMethod;
	} else {
		if ((accessMethod = accessors[0]) == null) {
			accessMethod = new SyntheticMethodBinding(this, selector);
			accessors[0] = accessMethod;
		}
	}
	return accessMethod;
}

/**
 * Adds a synthetic field to handle the cache of the switch translation table for
 * the corresponding enum type.
 */
public SyntheticFieldBinding addSyntheticFieldForSwitchEnum(char[] fieldName, String key) {
	if (!isPrototype()) throw new IllegalStateException();

	if (this.synthetics == null)
		this.synthetics = new Map[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.FIELD_EMUL] == null)
		this.synthetics[SourceTypeBinding.FIELD_EMUL] = new LinkedHashMap(5);

	SyntheticFieldBinding synthField = (SyntheticFieldBinding) this.synthetics[SourceTypeBinding.FIELD_EMUL].get(key);
	if (synthField == null) {
		synthField = new SyntheticFieldBinding(
			fieldName,
			this.scope.createArrayType(TypeBinding.INT,1),
			(isInterface() ? (ClassFileConstants.AccPublic | ClassFileConstants.AccFinal) : ClassFileConstants.AccPrivate | ClassFileConstants.AccVolatile) | ClassFileConstants.AccStatic | ClassFileConstants.AccSynthetic,
			this,
			Constant.NotAConstant);
		this.synthetics[SourceTypeBinding.FIELD_EMUL].put(key, synthField);
	}
	// ensure there is not already such a field defined by the user
	boolean needRecheck;
	int index = 0;
	do {
		needRecheck = false;
		FieldBinding existingField;
		if ((existingField = getField(synthField.name, true /*resolve*/)) != null) {
			TypeDeclaration typeDecl = this.scope.referenceContext;
			FieldDeclaration[] fieldDeclarations = typeDecl.fields;
			int max = fieldDeclarations == null ? 0 : fieldDeclarations.length;
			for (int i = 0; i < max; i++) {
				FieldDeclaration fieldDecl = fieldDeclarations[i];
				if (fieldDecl.binding == existingField) {
					synthField.name = CharOperation.concat(
						fieldName,
						("_" + String.valueOf(index++)).toCharArray()); //$NON-NLS-1$
					needRecheck = true;
					break;
				}
			}
		}
	} while (needRecheck);
	return synthField;
}

/**
 * Adds a new synthetic method the enum type. Selector can either be 'values' or
 * 'valueOf'. {@code char[]} constants from {@link TypeConstants} must be used:
 * {@code TypeConstants.VALUES/VALUEOF}.
*/
public SyntheticMethodBinding addSyntheticMethodForSwitchEnum(TypeBinding enumBinding, SwitchStatement switchStatement) {
	if (!isPrototype()) throw new IllegalStateException();

	if (this.synthetics == null)
		this.synthetics = new Map[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.METHOD_EMUL] == null)
		this.synthetics[SourceTypeBinding.METHOD_EMUL] = new LinkedHashMap(5);

	SyntheticMethodBinding accessMethod = null;
	char[] selector = CharOperation.concat(TypeConstants.SYNTHETIC_SWITCH_ENUM_TABLE, enumBinding.constantPoolName());
	CharOperation.replace(selector, '/', '$');
	final String key = new String(selector);
	SyntheticMethodBinding[] accessors = (SyntheticMethodBinding[]) this.synthetics[SourceTypeBinding.METHOD_EMUL].get(key);
	// first add the corresponding synthetic field
	if (accessors == null) {
		// then create the synthetic method
		final SyntheticFieldBinding fieldBinding = addSyntheticFieldForSwitchEnum(selector, key);
		accessMethod = new SyntheticMethodBinding(fieldBinding, this, enumBinding, selector, switchStatement);
		this.synthetics[SourceTypeBinding.METHOD_EMUL].put(key, accessors = new SyntheticMethodBinding[2]);
		accessors[0] = accessMethod;
	} else {
		if ((accessMethod = accessors[0]) == null) {
			final SyntheticFieldBinding fieldBinding = addSyntheticFieldForSwitchEnum(selector, key);
			accessMethod = new SyntheticMethodBinding(fieldBinding, this, enumBinding, selector, switchStatement);
			accessors[0] = accessMethod;
		}
	}
	return accessMethod;
}

public SyntheticMethodBinding addSyntheticMethodForEnumInitialization(int begin, int end) {
	if (!isPrototype()) throw new IllegalStateException();

	if (this.synthetics == null)
		this.synthetics = new Map[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.METHOD_EMUL] == null)
		this.synthetics[SourceTypeBinding.METHOD_EMUL] = new LinkedHashMap(5);

	SyntheticMethodBinding accessMethod = new SyntheticMethodBinding(this, begin, end);
	SyntheticMethodBinding[] accessors = new SyntheticMethodBinding[2];
	this.synthetics[SourceTypeBinding.METHOD_EMUL].put(accessMethod.selector, accessors);
	accessors[0] = accessMethod;
	return accessMethod;
}

public SyntheticMethodBinding addSyntheticMethod(LambdaExpression lambda) {
	if (!isPrototype()) throw new IllegalStateException();

	if (this.synthetics == null)
		this.synthetics = new Map[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.METHOD_EMUL] == null)
		this.synthetics[SourceTypeBinding.METHOD_EMUL] = new LinkedHashMap(5);

	SyntheticMethodBinding lambdaMethod = null;
	SyntheticMethodBinding[] lambdaMethods = (SyntheticMethodBinding[]) this.synthetics[SourceTypeBinding.METHOD_EMUL].get(lambda);
	if (lambdaMethods == null) {
		lambdaMethod = new SyntheticMethodBinding(lambda, CharOperation.concat(TypeConstants.ANONYMOUS_METHOD, Integer.toString(lambda.ordinal).toCharArray()), this);
		this.synthetics[SourceTypeBinding.METHOD_EMUL].put(lambda, lambdaMethods = new SyntheticMethodBinding[1]);
		lambdaMethods[0] = lambdaMethod;
	} else {
		lambdaMethod = lambdaMethods[0];
	}

	// Create a $deserializeLambda$ method if necessary, one is shared amongst all lambdas
	if (lambda.isSerializable) {
		addDeserializeLambdaMethod();
	}

	return lambdaMethod;
}

/**
 * Adds a synthetic method for the reference expression as a place holder for code
 * generation only if the reference expression's target is serializable.
 */
public SyntheticMethodBinding addSyntheticMethod(ReferenceExpression ref) {
	if (!isPrototype()) throw new IllegalStateException();

	if (!ref.isSerializable)
		return null;
	if (this.synthetics == null)
		this.synthetics = new Map[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.METHOD_EMUL] == null)
		this.synthetics[SourceTypeBinding.METHOD_EMUL] = new LinkedHashMap(5);

	SyntheticMethodBinding lambdaMethod = null;
	SyntheticMethodBinding[] lambdaMethods = (SyntheticMethodBinding[]) this.synthetics[SourceTypeBinding.METHOD_EMUL].get(ref);
	if (lambdaMethods == null) {
		lambdaMethod = new SyntheticMethodBinding(ref, this);
		this.synthetics[SourceTypeBinding.METHOD_EMUL].put(ref, lambdaMethods = new SyntheticMethodBinding[1]);
		lambdaMethods[0] = lambdaMethod;
	} else {
		lambdaMethod = lambdaMethods[0];
	}

	// Create a $deserializeLambda$ method, one is shared amongst all lambdas
	addDeserializeLambdaMethod();
	return lambdaMethod;
}

private void addDeserializeLambdaMethod() {
	SyntheticMethodBinding[] deserializeLambdaMethods = (SyntheticMethodBinding[]) this.synthetics[SourceTypeBinding.METHOD_EMUL].get(TypeConstants.DESERIALIZE_LAMBDA);
	if (deserializeLambdaMethods == null) {
		SyntheticMethodBinding deserializeLambdaMethod = new SyntheticMethodBinding(this);
		this.synthetics[SourceTypeBinding.METHOD_EMUL].put(TypeConstants.DESERIALIZE_LAMBDA,deserializeLambdaMethods = new SyntheticMethodBinding[1]);
		deserializeLambdaMethods[0] = deserializeLambdaMethod;
	}
}

/**
 * Adds a new synthetic access method for access to {@code <targetMethod>}. Must
 * distinguish access method used for super access from others (need to use {@code
 * invokespecial} bytecode). Answers the new method or the existing method if one
 * already existed.
 */
public SyntheticMethodBinding addSyntheticMethod(MethodBinding targetMethod, boolean isSuperAccess) {
	if (!isPrototype()) throw new IllegalStateException();

	if (this.synthetics == null)
		this.synthetics = new Map[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.METHOD_EMUL] == null)
		this.synthetics[SourceTypeBinding.METHOD_EMUL] = new LinkedHashMap(5);

	SyntheticMethodBinding accessMethod = null;
	SyntheticMethodBinding[] accessors = (SyntheticMethodBinding[]) this.synthetics[SourceTypeBinding.METHOD_EMUL].get(targetMethod);
	if (accessors == null) {
		accessMethod = new SyntheticMethodBinding(targetMethod, isSuperAccess, this);
		this.synthetics[SourceTypeBinding.METHOD_EMUL].put(targetMethod, accessors = new SyntheticMethodBinding[2]);
		accessors[isSuperAccess ? 0 : 1] = accessMethod;
	} else {
		if ((accessMethod = accessors[isSuperAccess ? 0 : 1]) == null) {
			accessMethod = new SyntheticMethodBinding(targetMethod, isSuperAccess, this);
			accessors[isSuperAccess ? 0 : 1] = accessMethod;
		}
	}
	if (targetMethod.declaringClass.isStatic()) {
		if ((targetMethod.isConstructor() && targetMethod.parameters.length >= 0xFE)
				|| targetMethod.parameters.length >= 0xFF) {
			this.scope.problemReporter().tooManyParametersForSyntheticMethod(targetMethod.sourceMethod());
		}
	} else if ((targetMethod.isConstructor() && targetMethod.parameters.length >= 0xFD)
			|| targetMethod.parameters.length >= 0xFE) {
		this.scope.problemReporter().tooManyParametersForSyntheticMethod(targetMethod.sourceMethod());
	}
	return accessMethod;
}

public SyntheticMethodBinding addSyntheticArrayMethod(ArrayBinding arrayType, int purpose, char[] selector) {
	if (!isPrototype()) throw new IllegalStateException();

	if (this.synthetics == null)
		this.synthetics = new Map[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.METHOD_EMUL] == null)
		this.synthetics[SourceTypeBinding.METHOD_EMUL] = new LinkedHashMap(5);

	SyntheticMethodBinding arrayMethod = null;
	SyntheticMethodBinding[] arrayMethods = (SyntheticMethodBinding[]) this.synthetics[SourceTypeBinding.METHOD_EMUL].get(arrayType);
	if (arrayMethods == null) {
		arrayMethod = new SyntheticMethodBinding(purpose, arrayType, selector, this);
		this.synthetics[SourceTypeBinding.METHOD_EMUL].put(arrayType, arrayMethods = new SyntheticMethodBinding[2]);
		arrayMethods[purpose == SyntheticMethodBinding.ArrayConstructor ? 0 : 1] = arrayMethod;
	} else {
		if ((arrayMethod = arrayMethods[purpose == SyntheticMethodBinding.ArrayConstructor ? 0 : 1]) == null) {
			arrayMethod = new SyntheticMethodBinding(purpose, arrayType, selector, this);
			arrayMethods[purpose == SyntheticMethodBinding.ArrayConstructor ? 0 : 1] = arrayMethod;
		}
	}
	return arrayMethod;
}

public SyntheticMethodBinding addSyntheticFactoryMethod(MethodBinding privateConstructor, MethodBinding publicConstructor, TypeBinding [] enclosingInstances, char[] selector) {
	if (!isPrototype()) throw new IllegalStateException();

	if (this.synthetics == null)
		this.synthetics = new Map[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.METHOD_EMUL] == null)
		this.synthetics[SourceTypeBinding.METHOD_EMUL] = new LinkedHashMap(5);

	SyntheticMethodBinding factory = new SyntheticMethodBinding(privateConstructor, publicConstructor, selector, enclosingInstances, this);
	this.synthetics[SourceTypeBinding.METHOD_EMUL].put(selector, new SyntheticMethodBinding[] { factory });
	return factory;
}

/**
 * Records the fact that bridge methods need to be generated to override certain
 * inherited methods.
 */
public SyntheticMethodBinding addSyntheticBridgeMethod(MethodBinding inheritedMethodToBridge, MethodBinding targetMethod) {
	if (!isPrototype()) throw new IllegalStateException();

	// targetMethod may be inherited
	if (TypeBinding.equalsEquals(inheritedMethodToBridge.returnType.erasure(), targetMethod.returnType.erasure())
		&& inheritedMethodToBridge.areParameterErasuresEqual(targetMethod)) {
			return null; // do not need bridge method
	}
	if (this.synthetics == null)
		this.synthetics = new Map[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.METHOD_EMUL] == null) {
		this.synthetics[SourceTypeBinding.METHOD_EMUL] = new LinkedHashMap(5);
	} else {
		// check to see if there is another equivalent inheritedMethod already added
		Iterator synthMethods = this.synthetics[SourceTypeBinding.METHOD_EMUL].keySet().iterator();
		while (synthMethods.hasNext()) {
			Object synthetic = synthMethods.next();
			if (synthetic instanceof MethodBinding) {
				MethodBinding method = (MethodBinding) synthetic;
				if (CharOperation.equals(inheritedMethodToBridge.selector, method.selector)
					&& TypeBinding.equalsEquals(inheritedMethodToBridge.returnType.erasure(), method.returnType.erasure())
					&& inheritedMethodToBridge.areParameterErasuresEqual(method)) {
						return null;
				}
			}
		}
	}

	SyntheticMethodBinding accessMethod = null;
	SyntheticMethodBinding[] accessors = (SyntheticMethodBinding[]) this.synthetics[SourceTypeBinding.METHOD_EMUL].get(inheritedMethodToBridge);
	if (accessors == null) {
		accessMethod = new SyntheticMethodBinding(inheritedMethodToBridge, targetMethod, this);
		this.synthetics[SourceTypeBinding.METHOD_EMUL].put(inheritedMethodToBridge, accessors = new SyntheticMethodBinding[2]);
		accessors[1] = accessMethod;
	} else {
		if ((accessMethod = accessors[1]) == null) {
			accessMethod = new SyntheticMethodBinding(inheritedMethodToBridge, targetMethod, this);
			accessors[1] = accessMethod;
		}
	}
	return accessMethod;
}

/**
 * Generates a bridge method if a public method is inherited from a non-public
 * class into a public class (only in 1.6 or greater) -- this doesn't apply to
 * inherited interface methods (i.e., default methods).
 *
 * @see <a href="https://bugs.eclipse.org/288658">bug 288658</a>
 * @see <a href="https://bugs.eclipse.org/404690">bug 404690</a>
 */
public SyntheticMethodBinding addSyntheticBridgeMethod(MethodBinding inheritedMethodToBridge) {
	if (!isPrototype()) throw new IllegalStateException();

	if (isInterface() && !inheritedMethodToBridge.isDefaultMethod()) return null;
	if (inheritedMethodToBridge.isAbstract() || inheritedMethodToBridge.isFinal() || inheritedMethodToBridge.isStatic()) {
		return null;
	}
	if (this.synthetics == null)
		this.synthetics = new Map[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.METHOD_EMUL] == null) {
		this.synthetics[SourceTypeBinding.METHOD_EMUL] = new LinkedHashMap(5);
	} else {
		// check to see if there is another equivalent inheritedMethod already added
		Iterator synthMethods = this.synthetics[SourceTypeBinding.METHOD_EMUL].keySet().iterator();
		while (synthMethods.hasNext()) {
			Object synthetic = synthMethods.next();
			if (synthetic instanceof MethodBinding) {
				MethodBinding method = (MethodBinding) synthetic;
				if (CharOperation.equals(inheritedMethodToBridge.selector, method.selector)
					&& TypeBinding.equalsEquals(inheritedMethodToBridge.returnType.erasure(), method.returnType.erasure())
					&& inheritedMethodToBridge.areParameterErasuresEqual(method)) {
						return null;
				}
			}
		}
	}

	SyntheticMethodBinding accessMethod = null;
	SyntheticMethodBinding[] accessors = (SyntheticMethodBinding[]) this.synthetics[SourceTypeBinding.METHOD_EMUL].get(inheritedMethodToBridge);
	if (accessors == null) {
		accessMethod = new SyntheticMethodBinding(inheritedMethodToBridge, this);
		this.synthetics[SourceTypeBinding.METHOD_EMUL].put(inheritedMethodToBridge, accessors = new SyntheticMethodBinding[2]);
		accessors[1] = accessMethod;
	} else {
		if ((accessMethod = accessors[1]) == null) {
			accessMethod = new SyntheticMethodBinding(inheritedMethodToBridge, this);
			accessors[1] = accessMethod;
		}
	}
	return accessMethod;
}

public SyntheticMethodBinding addSyntheticCanonicalConstructor() {
	if (!isPrototype()) throw new IllegalStateException();
	if (this.synthetics == null)
		this.synthetics = new Map[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.METHOD_EMUL] == null)
		this.synthetics[SourceTypeBinding.METHOD_EMUL] = new LinkedHashMap(5);

	SyntheticMethodBinding canonicalConstructor = new SyntheticMethodBinding(this, this.components);
	SyntheticMethodBinding[] accessors = new SyntheticMethodBinding[2];
	this.synthetics[SourceTypeBinding.METHOD_EMUL].put(TypeConstants.INIT, accessors);
	return accessors[0] = canonicalConstructor;
}

/**
 * Adds a new synthetic component accessor for the record class.
*/
public SyntheticMethodBinding addSyntheticRecordComponentAccessor(RecordComponentBinding rcb) {
	if (!isPrototype()) throw new IllegalStateException();

	if (this.synthetics == null)
		this.synthetics = new Map[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.METHOD_EMUL] == null)
		this.synthetics[SourceTypeBinding.METHOD_EMUL] = new LinkedHashMap(5);

	SyntheticMethodBinding accessor = new SyntheticMethodBinding(this, rcb);
	SyntheticMethodBinding[] accessors = new SyntheticMethodBinding[2];
	this.synthetics[SourceTypeBinding.METHOD_EMUL].put(rcb.name, accessors);
	return accessors[0] = accessor;
}

public SyntheticMethodBinding addSyntheticRecordOverrideMethod(char[] selector) {
	if (this.synthetics == null)
		this.synthetics = new Map[MAX_SYNTHETICS];
	if (this.synthetics[SourceTypeBinding.METHOD_EMUL] == null)
		this.synthetics[SourceTypeBinding.METHOD_EMUL] = new LinkedHashMap(5);

	SyntheticMethodBinding accessMethod = null;
	SyntheticMethodBinding[] accessors = (SyntheticMethodBinding[]) this.synthetics[SourceTypeBinding.METHOD_EMUL].get(selector);
	accessMethod = new SyntheticMethodBinding(this, selector);
	if (accessors == null) {
		this.synthetics[SourceTypeBinding.METHOD_EMUL].put(selector, accessors = new SyntheticMethodBinding[2]);
		accessors[0] = accessMethod;
	} else {
		if ((accessMethod = accessors[0]) == null) {
			accessors[0] = accessMethod;
		}
	}
	return accessMethod;
}
boolean areComponentsInitialized() {
	if (!isPrototype())
		return this.prototype.areComponentsInitialized();
	return this.components != Binding.UNINITIALIZED_COMPONENTS;
}

boolean areFieldsInitialized() {
	if (!isPrototype())
		return this.prototype.areFieldsInitialized();
	return this.fields != Binding.UNINITIALIZED_FIELDS;
}

boolean areMethodsInitialized() {
	if (!isPrototype())
		return this.prototype.areMethodsInitialized();
	return this.methods != Binding.UNINITIALIZED_METHODS;
}

@Override
public int kind() {
	if (!isPrototype())
		return this.prototype.kind();
	if (this.typeVariables != Binding.NO_TYPE_VARIABLES)
		return Binding.GENERIC_TYPE;
	return Binding.TYPE;
}

@Override
public TypeBinding clone(TypeBinding immaterial) {
	return new SourceTypeBinding(this);
}

@Override
public char[] computeUniqueKey(boolean isLeaf) {
	if (!isPrototype())
		return this.prototype.computeUniqueKey();
	char[] uniqueKey = super.computeUniqueKey(isLeaf);
	if (uniqueKey.length == 2) return uniqueKey; // problem type's unique key is "L;"
	if (Util.isClassFileName(this.fileName)) return uniqueKey; // no need to insert compilation unit name for a .class file

	// insert compilation unit name if the type name is not the main type name
	int end = CharOperation.lastIndexOf('.', this.fileName);
	if (end != -1) {
		int start = CharOperation.lastIndexOf('/', this.fileName) + 1;
		char[] mainTypeName = CharOperation.subarray(this.fileName, start, end);
		start = CharOperation.lastIndexOf('/', uniqueKey) + 1;
		if (start == 0)
			start = 1; // start after L
		if (this.isMemberType()) {
			end = CharOperation.indexOf('$', uniqueKey, start);
		} else {
			// '$' is part of the type name
			end = -1;
		}
		if (end == -1)
			end = CharOperation.indexOf('<', uniqueKey, start);
		if (end == -1)
			end = CharOperation.indexOf(';', uniqueKey, start);
		char[] topLevelType = CharOperation.subarray(uniqueKey, start, end);
		if (!CharOperation.equals(topLevelType, mainTypeName)) {
			StringBuilder buffer = new StringBuilder();
			buffer.append(uniqueKey, 0, start);
			buffer.append(mainTypeName);
			buffer.append('~');
			buffer.append(topLevelType);
			buffer.append(uniqueKey, end, uniqueKey.length - end);
			int length = buffer.length();
			uniqueKey = new char[length];
			buffer.getChars(0, length, uniqueKey, 0);
			return uniqueKey;
		}
	}
	return uniqueKey;
}

private void checkAnnotationsInType() {
	// check @Deprecated annotation
	getAnnotationTagBits(); // marks as deprecated by side effect
	for (ReferenceBinding memberType : this.memberTypes)
		((SourceTypeBinding) memberType).checkAnnotationsInType();
}

void faultInTypesForFieldsAndMethods() {
	if (!isPrototype()) throw new IllegalStateException();
	if (!this.isLocalType())
		complainIfUnpermittedSubtyping();  // this has nothing to do with fields and methods but time is ripe for this check.
	checkAnnotationsInType();
	internalFaultInTypeForFieldsAndMethods();
}

private boolean isAnUnpermittedSubtypeOf(ReferenceBinding superType) {

	if (superType == null || !superType.isSealed())
		return false;

	for (ReferenceBinding permittedType : superType.actualType().permittedTypes()) {
		if (TypeBinding.equalsEquals(this, permittedType))
			return false;
	}

	return true;
}

private void complainIfUnpermittedSubtyping() {

	// Diagnose unauthorized subtyping: This cannot be correctly hoisted into ClassScope.{ connectSuperclass() | connectSuperInterfaces() | connectPermittedTypes() }
	// but can be taken up now

	TypeDeclaration typeDecl = this.scope.referenceContext;
	if (this.isAnUnpermittedSubtypeOf(this.superclass)) {
		this.scope.problemReporter().sealedSupertypeDoesNotPermit(this, typeDecl.superclass, this.superclass);
	}

	for (int i = 0, l = this.superInterfaces.length; i < l; ++i) {
		ReferenceBinding superInterface = this.superInterfaces[i];
		if (this.isAnUnpermittedSubtypeOf(superInterface)) {
			TypeReference superInterfaceRef = typeDecl.superInterfaces[i];
			this.scope.problemReporter().sealedSupertypeDoesNotPermit(this, superInterfaceRef, superInterface);
		}
	}

	if (typeDecl.permittedTypes != null) {
		for (TypeReference permittedTypeRef : typeDecl.permittedTypes) {
			TypeBinding permittedType = permittedTypeRef.resolvedType;
			if (permittedType != null && permittedType.isValidBinding()) {
				if (isClass()) {
					ReferenceBinding superClass = permittedType.superclass();
					superClass = superClass == null ? null : superClass.actualType();
					if (!TypeBinding.equalsEquals(this, superClass))
						this.scope.problemReporter().sealedClassNotDirectSuperClassOf(permittedType, permittedTypeRef, this);
				} else if (isInterface()) {
					ReferenceBinding[] permittedTypesSuperInterfaces = permittedType.superInterfaces();
					boolean hierarchyOK = false;
					if (permittedTypesSuperInterfaces != null) {
						for (ReferenceBinding superInterface : permittedTypesSuperInterfaces) {
							superInterface = superInterface == null ? null : superInterface.actualType();
							if (TypeBinding.equalsEquals(this, superInterface)) {
								hierarchyOK = true;
								break;
							}
						}
						if (!hierarchyOK)
							this.scope.problemReporter().sealedInterfaceNotDirectSuperInterfaceOf(permittedType, permittedTypeRef, this);
					}
				}
			}
		}
	}

	for (ReferenceBinding memberType : this.memberTypes)
		((SourceTypeBinding) memberType).complainIfUnpermittedSubtyping();

	return;
}

@Override
public RecordComponentBinding[] components() {
	if (!this.isRecord())
		return NO_COMPONENTS;

	if (!isPrototype()) {
		return this.components = this.prototype.components();
	}
	if ((this.tagBits & TagBits.HasUnresolvedComponents) == 0)
		return this.components;

	int length = this.components.length;
	int count = 0;
	RecordComponentBinding[] rcbs = length == 0 ? Binding.NO_COMPONENTS : new RecordComponentBinding[length];
	for (int i = 0; i < length; i++) {
		if (resolveTypeFor(this.components[i]) != null) {
			rcbs[count++] = this.components[i];
		}
	}
	if (count != rcbs.length) // remove duplicate or broken components
		System.arraycopy(rcbs, 0, rcbs = count == 0 ? Binding.NO_COMPONENTS : new RecordComponentBinding[count], 0, count);
	this.tagBits &= ~TagBits.HasUnresolvedComponents;
	return setComponents(rcbs);
}

private VariableBinding resolveTypeFor(VariableBinding variable) {

	if (!isPrototype())
		return this.prototype.resolveTypeFor(variable);

	if ((variable.modifiers & ExtraCompilerModifiers.AccUnresolved) == 0)
		return variable;

	MethodScope initializationScope = variable.isStatic()
		? this.scope.referenceContext.staticInitializerScope
		: this.scope.referenceContext.initializerScope;
	FieldBinding previousField = initializationScope.initializedField;
	try {
		if (variable instanceof FieldBinding field)
			initializationScope.initializedField = field;
		AbstractVariableDeclaration variableDeclaration = variable instanceof FieldBinding field ? field.sourceField() : ((RecordComponentBinding) variable).sourceRecordComponent();
		ASTNode.resolveNullDefaultAnnotations(initializationScope, variableDeclaration.annotations, variable);
		TypeBinding variableType =
			variableDeclaration.getKind() == AbstractVariableDeclaration.ENUM_CONSTANT
				? initializationScope.environment().convertToRawType(this, false /*do not force conversion of enclosing types*/) // enum constant is implicitly of declaring enum type
				: variableDeclaration.type.resolveType(initializationScope, true /* check bounds*/);
		variable.type = variableType;
		variable.modifiers &= ~ExtraCompilerModifiers.AccUnresolved;
		if (variableType == null) {
			variableDeclaration.setBinding(null);
			return null;
		}
		if (variableType == TypeBinding.VOID) {
			this.scope.problemReporter().variableTypeCannotBeVoid(variableDeclaration);
			variableDeclaration.setBinding(null);
			return null;
		}
		if (variableType.isArrayType() && ((ArrayBinding) variableType).leafComponentType == TypeBinding.VOID) {
			this.scope.problemReporter().variableTypeCannotBeVoidArray(variableDeclaration);
			variableDeclaration.setBinding(null);
			return null;
		}
		if ((variableType.tagBits & TagBits.HasMissingType) != 0) {
			variable.tagBits |= TagBits.HasMissingType;
		}
		TypeBinding leafType = variableType.leafComponentType();
		if (leafType instanceof ReferenceBinding && (((ReferenceBinding)leafType).modifiers & ExtraCompilerModifiers.AccGenericSignature) != 0) {
			variable.modifiers |= ExtraCompilerModifiers.AccGenericSignature;
		}

		if ((variable.getAnnotationTagBits() & TagBits.AnnotationDeprecated) != 0)
			variable.modifiers |= ClassFileConstants.AccDeprecated;
		if (hasRestrictedAccess())
			variable.modifiers |= ExtraCompilerModifiers.AccRestrictedAccess;

		Annotation [] annotations = variableDeclaration.annotations;

		if (variableDeclaration instanceof RecordComponent componentDeclaration) {
			if ((variable.modifiers & ExtraCompilerModifiers.AccJustFlag) != 0)
				this.scope.problemReporter().recordComponentsCannotHaveModifiers(componentDeclaration);
			if (TypeDeclaration.disallowedComponentNames.contains(new String(componentDeclaration.name))) {
				this.scope.problemReporter().illegalComponentNameInRecord(componentDeclaration, this.scope.referenceContext);
				componentDeclaration.setBinding(null);
				return null;
			}
			if (componentDeclaration.isUnnamed(this.scope)) {
				this.scope.problemReporter().illegalUseOfUnderscoreAsAnIdentifier(componentDeclaration.sourceStart, componentDeclaration.sourceEnd, true, true);
				componentDeclaration.setBinding(null);
				return null;
			}
			RecordComponent[] recordComponents = this.scope.referenceContext.recordComponents;
			if (componentDeclaration.isVarArgs() && recordComponents[recordComponents.length - 1] != componentDeclaration)
				this.scope.problemReporter().onlyLastRecordComponentMaybeVararg(componentDeclaration, this.scope.referenceContext);
			ASTNode.copyRecordComponentAnnotations(initializationScope, variable, annotations);
		}
		if (annotations != null && annotations.length != 0) {
			ASTNode.copySE8AnnotationsToType(initializationScope, variable, annotations,
					variableDeclaration.getKind() == AbstractVariableDeclaration.ENUM_CONSTANT); // type annotation is illegal on enum constant
		}

		Annotation.isTypeUseCompatible(variableDeclaration.type, this.scope, annotations);
		// apply null default:
		if (this.environment.globalOptions.isAnnotationBasedNullAnalysisEnabled) {
			// TODO(SH): different strategy for 1.8, or is "repair" below enough?
			if (variableDeclaration.getKind() == AbstractVariableDeclaration.ENUM_CONSTANT) {
				// enum constants neither have a type declaration nor can they be null
				variable.tagBits |= TagBits.AnnotationNonNull;
			} else {
				int location = variable.kind() == Binding.RECORD_COMPONENT ? DefaultLocationRecordComponent : DefaultLocationField;
				if (hasNonNullDefaultForType(variableType, location, variableDeclaration.sourceStart)) {
					variable.fillInDefaultNonNullness(variableDeclaration, initializationScope);
				}
				// validate null annotation:
				if (!this.scope.validateNullAnnotation(variable.tagBits, variableDeclaration.type, variableDeclaration.annotations))
					variable.tagBits &= ~TagBits.AnnotationNullMASK;
			}
		}
		if (initializationScope.shouldCheckAPILeaks(this, variable.isPublic()) && variableDeclaration.type != null) // variableDeclaration.type is null for enum constants
			initializationScope.detectAPILeaks(variableDeclaration.type, variableType);
	} finally {
	    initializationScope.initializedField = previousField;
	}
	if (this.externalAnnotationProvider != null) {
		if (variable instanceof FieldBinding field)
			ExternalAnnotationSuperimposer.annotateFieldBinding(field, this.externalAnnotationProvider, this.environment);
		else if (variable instanceof RecordComponentBinding component)
			ExternalAnnotationSuperimposer.annotateComponentBinding(component, this.externalAnnotationProvider, this.environment);
	}
	return variable;
}

public RecordComponentBinding resolveTypeFor(RecordComponentBinding component) {
	return (RecordComponentBinding) resolveTypeFor((VariableBinding) component);
}

private void internalFaultInTypeForFieldsAndMethods() {
	fields();
	methods();

	for (ReferenceBinding memberType : this.memberTypes)
		((SourceTypeBinding) memberType).internalFaultInTypeForFieldsAndMethods();
}

// NOTE: the type of each field of a source type is resolved when needed
@Override
public FieldBinding[] fields() {

	if (!isPrototype()) {
		if ((this.tagBits & TagBits.AreFieldsComplete) != 0)
			return this.fields;
		this.tagBits |= TagBits.AreFieldsComplete;
		return this.fields = this.prototype.fields();
	}

	if ((this.tagBits & TagBits.AreFieldsComplete) != 0)
		return this.fields;

	if ((this.tagBits & TagBits.HasUnresolvedComponents) != 0)
		components();

	int failed = 0;
	FieldBinding[] theFields = unResolvedFields();
	try {
		// lazily sort fields
		if ((this.tagBits & TagBits.AreFieldsSorted) == 0) {
			int length = theFields.length;
			if (length > 1)
				ReferenceBinding.sortFields(theFields, 0, length);
			this.tagBits |= TagBits.AreFieldsSorted;
		}
		FieldBinding[] fieldsSnapshot = theFields;
		for (int i = 0, length = fieldsSnapshot.length; i < length; i++) {
			if (resolveTypeFor(fieldsSnapshot[i]) == null) {
				// do not alter original field array until resolution is over, due to reentrance (143259)
				if (theFields == fieldsSnapshot) {
					System.arraycopy(fieldsSnapshot, 0, theFields = new FieldBinding[length], 0, length);
				}
				theFields[i] = null;
				if (this.isRecord() && !fieldsSnapshot[i].isStatic()) {
					Iterator<Map.Entry<?, ?>> iterator = this.synthetics[SourceTypeBinding.FIELD_EMUL].entrySet().iterator();
					while (iterator.hasNext()) {
						Map.Entry<?, ?> entry = iterator.next();
						if (entry.getValue().equals(fieldsSnapshot[i])) {
							iterator.remove();
							break;
						}
					}
				}
				failed++;
			}
		}
	} finally {
		if (failed > 0) {
			// ensure fields are consistent reqardless of the error
			int newSize = theFields.length - failed;
			if (newSize == 0)
				return setFields(Binding.NO_FIELDS);

			FieldBinding[] newFields = new FieldBinding[newSize];
			for (int i = 0, j = 0, length = theFields.length; i < length; i++) {
				if (theFields[i] != null)
					newFields[j++] = theFields[i];
			}
			setFields(newFields);
		}
	}
	this.tagBits |= TagBits.AreFieldsComplete;
	return this.fields;
}

/**
 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#genericTypeSignature()
 */
@Override
public char[] genericTypeSignature() {
	if (!isPrototype())
		return this.prototype.genericTypeSignature();

	if (this.genericReferenceTypeSignature == null)
		this.genericReferenceTypeSignature = computeGenericTypeSignature(this.typeVariables);
	return this.genericReferenceTypeSignature;
}

/**
 * {@code <param1 ... paramN>superclass superinterface1 ... superinterfaceN
 * <T:LY<TT;>;U:Ljava/lang/Object;V::Ljava/lang/Runnable;:Ljava/lang/Cloneable;:Ljava/util/Map;>Ljava/lang/Exception;Ljava/lang/Runnable;}
 */
public char[] genericSignature() {
	if (!isPrototype())
		return this.prototype.genericSignature();

	StringBuilder sig = null;
	if (this.typeVariables != Binding.NO_TYPE_VARIABLES) {
		sig = new StringBuilder(10);
		sig.append('<');
		for (TypeVariableBinding typeVariable : this.typeVariables)
			sig.append(typeVariable.genericSignature());
		sig.append('>');
	} else {
		// could still need a signature if any of supertypes is parameterized
		noSignature: if (this.superclass == null || !this.superclass.isParameterizedType()) {
			for (int i = 0, length = this.superInterfaces.length; i < length; i++)
				if (this.superInterfaces[i].isParameterizedType())
					break noSignature;
			return null;
		}
		sig = new StringBuilder(10);
	}
	if (this.superclass != null)
		sig.append(this.superclass.genericTypeSignature());
	else // interface scenario only (as Object cannot be generic) - 65953
		sig.append(this.scope.getJavaLangObject().genericTypeSignature());
	for (ReferenceBinding superInterface : this.superInterfaces)
		sig.append(superInterface.genericTypeSignature());
	return sig.toString().toCharArray();
}

/**
 * Computes the tagbits for standard annotations. For source types, these could
 * require lazily resolving corresponding annotation nodes, in case of forward
 * references. For type use bindings, this method still returns the tagbits
 * corresponding to the type declaration binding.
 *
 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#getAnnotationTagBits()
 */
@Override
public long getAnnotationTagBits() {
	if (!isPrototype())
		return this.prototype.getAnnotationTagBits();

	if (!ExtendedTagBits.areAllAnnotationsResolved(this.extendedTagBits) && this.scope != null) {
		TypeDeclaration typeDecl = this.scope.referenceContext;
		boolean old = typeDecl.staticInitializerScope.insideTypeAnnotation;
		try {
			typeDecl.staticInitializerScope.insideTypeAnnotation = true;
			ASTNode.resolveAnnotations(typeDecl.staticInitializerScope, typeDecl.annotations, this);
		} finally {
			typeDecl.staticInitializerScope.insideTypeAnnotation = old;
		}
		if ((this.tagBits & TagBits.AnnotationDeprecated) != 0)
			this.modifiers |= ClassFileConstants.AccDeprecated;
	}
	return this.tagBits;
}
void initializeNullDefaultAnnotation() {
	if (!isPrototype()) {
		this.prototype.initializeNullDefaultAnnotation();
		return;
	}
	if ((this.extendedTagBits & ExtendedTagBits.NullDefaultAnnotationResolved) == 0 && this.scope != null) {
		TypeDeclaration typeDecl = this.scope.referenceContext;
		boolean old = typeDecl.staticInitializerScope.insideTypeAnnotation;
		try {
			typeDecl.staticInitializerScope.insideTypeAnnotation = true;
			ASTNode.resolveNullDefaultAnnotations(typeDecl.staticInitializerScope, typeDecl.annotations, this);
			evaluateNullAnnotations();
		} finally {
			typeDecl.staticInitializerScope.insideTypeAnnotation = old;
		}
	}
}
@Override
public boolean isReadyForAnnotations() {
	if ((this.extendedTagBits & ExtendedTagBits.AnnotationResolved) != 0)
		return true;
	TypeDeclaration type;
	if (this.scope != null && (type = this.scope.referenceType()) != null) {
		if (type.annotations == null)
			return true; // nothing here to resolve
	}
	return false;
}
public MethodBinding[] getDefaultAbstractMethods() {
	if (!isPrototype())
		return this.prototype.getDefaultAbstractMethods();

	int count = 0;
	for (int i = this.methods.length; --i >= 0;)
		if (this.methods[i].isDefaultAbstract())
			count++;
	if (count == 0) return Binding.NO_METHODS;

	MethodBinding[] result = new MethodBinding[count];
	count = 0;
	for (int i = this.methods.length; --i >= 0;)
		if (this.methods[i].isDefaultAbstract())
			result[count++] = this.methods[i];
	return result;
}

// NOTE: the return type, arg & exception types of each method of a source type are resolved when needed
@Override
public MethodBinding getExactConstructor(TypeBinding[] argumentTypes) {
	if (!isPrototype())
		return this.prototype.getExactConstructor(argumentTypes);

	int argCount = argumentTypes.length;
	if ((this.tagBits & TagBits.AreMethodsComplete) != 0) { // have resolved all arg types & return type of the methods
		long range;
		if ((range = ReferenceBinding.binarySearch(TypeConstants.INIT, this.methods)) >= 0) {
			nextMethod: for (int imethod = (int)range, end = (int)(range >> 32); imethod <= end; imethod++) {
				MethodBinding method = this.methods[imethod];
				if (method.parameters.length == argCount) {
					TypeBinding[] toMatch = method.parameters;
					for (int iarg = 0; iarg < argCount; iarg++)
						if (TypeBinding.notEquals(toMatch[iarg], argumentTypes[iarg]))
							continue nextMethod;
					return method;
				}
			}
		}
	} else {
		// lazily sort methods
		if ((this.tagBits & TagBits.AreMethodsSorted) == 0) {
			int length = this.methods.length;
			if (length > 1)
				ReferenceBinding.sortMethods(this.methods, 0, length);
			this.tagBits |= TagBits.AreMethodsSorted;
		}
		long range;
		if ((range = ReferenceBinding.binarySearch(TypeConstants.INIT, this.methods)) >= 0) {
			nextMethod: for (int imethod = (int)range, end = (int)(range >> 32); imethod <= end; imethod++) {
				MethodBinding method = this.methods[imethod];
				if (resolveTypesFor(method) == null || method.returnType == null) {
					methods();
					return getExactConstructor(argumentTypes);  // try again since the problem methods have been removed
				}
				if (method.parameters.length == argCount) {
					TypeBinding[] toMatch = method.parameters;
					for (int iarg = 0; iarg < argCount; iarg++)
						if (TypeBinding.notEquals(toMatch[iarg], argumentTypes[iarg]))
							continue nextMethod;
					return method;
				}
			}
		}
		if (this.isRecord()) {
			methods();
			return getExactConstructor(argumentTypes); // try again with special record methods synthesized
		}
	}
	return null;
}

//NOTE: the return type, arg & exception types of each method of a source type are resolved when needed
//searches up the hierarchy as long as no potential (but not exact) match was found.
@Override
public MethodBinding getExactMethod(char[] selector, TypeBinding[] argumentTypes, CompilationUnitScope refScope) {
	if (!isPrototype())
		return this.prototype.getExactMethod(selector, argumentTypes, refScope);

	// sender from refScope calls recordTypeReference(this)
	int argCount = argumentTypes.length;
	boolean foundNothing = true;

	if ((this.tagBits & TagBits.AreMethodsComplete) != 0) { // have resolved all arg types & return type of the methods
		long range;
		if ((range = ReferenceBinding.binarySearch(selector, this.methods)) >= 0) {
			nextMethod: for (int imethod = (int)range, end = (int)(range >> 32); imethod <= end; imethod++) {
				MethodBinding method = this.methods[imethod];
				foundNothing = false; // inner type lookups must know that a method with this name exists
				if (method.parameters.length == argCount) {
					TypeBinding[] toMatch = method.parameters;
					for (int iarg = 0; iarg < argCount; iarg++)
						if (TypeBinding.notEquals(toMatch[iarg], argumentTypes[iarg]))
							continue nextMethod;
					return method;
				}
			}
		}
	} else {
		// lazily sort methods
		if ((this.tagBits & TagBits.AreMethodsSorted) == 0) {
			int length = this.methods.length;
			if (length > 1)
				ReferenceBinding.sortMethods(this.methods, 0, length);
			this.tagBits |= TagBits.AreMethodsSorted;
		}

		long range;
		if ((range = ReferenceBinding.binarySearch(selector, this.methods)) >= 0) {
			// check unresolved method
			int start = (int) range, end = (int) (range >> 32);
			for (int imethod = start; imethod <= end; imethod++) {
				MethodBinding method = this.methods[imethod];
				if (resolveTypesFor(method) == null || method.returnType == null) {
					methods();
					return getExactMethod(selector, argumentTypes, refScope); // try again since the problem methods have been removed
				}
			}
			// check dup collisions
			for (int i = start; i <= end; i++) {
				MethodBinding method1 = this.methods[i];
				for (int j = end; j > i; j--) {
					MethodBinding method2 = this.methods[j];
					boolean paramsMatch = method1.areParameterErasuresEqual(method2);
					if (paramsMatch) {
						methods();
						return getExactMethod(selector, argumentTypes, refScope); // try again since the problem methods have been removed
					}
				}
			}
			nextMethod: for (int imethod = start; imethod <= end; imethod++) {
				MethodBinding method = this.methods[imethod];
				TypeBinding[] toMatch = method.parameters;
				if (toMatch.length == argCount) {
					for (int iarg = 0; iarg < argCount; iarg++)
						if (TypeBinding.notEquals(toMatch[iarg], argumentTypes[iarg]))
							continue nextMethod;
					return method;
				}
			}
		}
		if (this.isRecord()) {
			methods();
			return getExactMethod(selector, argumentTypes, refScope); // try again with special record methods synthesized
		}
	}

	if (foundNothing) {
		if (isInterface()) {
			 if (this.superInterfaces.length == 1) {
				if (refScope != null)
					refScope.recordTypeReference(this.superInterfaces[0]);
				return this.superInterfaces[0].getExactMethod(selector, argumentTypes, refScope);
			 }
		} else if (this.superclass != null) {
			if (refScope != null)
				refScope.recordTypeReference(this.superclass);
			return this.superclass.getExactMethod(selector, argumentTypes, refScope);
		}
	}
	return null;
}

//NOTE: the type of a field of a source type is resolved when needed
@Override
public FieldBinding getField(char[] fieldName, boolean needResolve) {
	if (!isPrototype())
		return this.prototype.getField(fieldName, needResolve);

	if ((this.tagBits & TagBits.AreFieldsComplete) != 0)
		return ReferenceBinding.binarySearch(fieldName, this.fields);

	if ((this.tagBits & TagBits.HasUnresolvedComponents) != 0)
		components();

	FieldBinding[] theFields = unResolvedFields();

	// lazily sort fields
	if ((this.tagBits & TagBits.AreFieldsSorted) == 0) {
		int length = theFields.length;
		if (length > 1)
			ReferenceBinding.sortFields(theFields, 0, length);
		this.tagBits |= TagBits.AreFieldsSorted;
	}
	// always resolve anyway on source types
	FieldBinding field = ReferenceBinding.binarySearch(fieldName, theFields);
	if (field != null) {
		FieldBinding result = null;
		try {
			result = resolveTypeFor(field);
			return result;
		} finally {
			if (result == null) {
				// ensure fields are consistent regardless of the error
				int newSize = theFields.length - 1;
				if (newSize == 0) {
					setFields(Binding.NO_FIELDS);
				} else {
					FieldBinding[] newFields = new FieldBinding[newSize];
					int index = 0;
					for (FieldBinding f : theFields) {
						if (f == field) continue;
						newFields[index++] = f;
					}
					setFields(newFields);
				}
			}
		}
	}
	return null;
}

// NOTE: the return type, arg & exception types of each method of a source type are resolved when needed
@Override
public MethodBinding[] getMethods(char[] selector) {
	if (!isPrototype())
		return this.prototype.getMethods(selector);

	if ((this.tagBits & TagBits.AreMethodsComplete) != 0) {
		long range;
		if ((range = ReferenceBinding.binarySearch(selector, this.methods)) >= 0) {
			int start = (int) range, end = (int) (range >> 32);
			int length = end - start + 1;
			MethodBinding[] result;
			System.arraycopy(this.methods, start, result = new MethodBinding[length], 0, length);
			return result;
		} else {
			return Binding.NO_METHODS;
		}
	}
	// lazily sort methods
	if ((this.tagBits & TagBits.AreMethodsSorted) == 0) {
		int length = this.methods.length;
		if (length > 1)
			ReferenceBinding.sortMethods(this.methods, 0, length);
		this.tagBits |= TagBits.AreMethodsSorted;
	}
	if (this.isRecord()) {
		methods();
		return getMethods(selector); // try again with special record methods synthesized
	}
	MethodBinding[] result;
	long range;
	if ((range = ReferenceBinding.binarySearch(selector, this.methods)) >= 0) {
		int start = (int) range, end = (int) (range >> 32);
		for (int i = start; i <= end; i++) {
			MethodBinding method = this.methods[i];
			if (resolveTypesFor(method) == null || method.returnType == null) {
				methods();
				return getMethods(selector); // try again since the problem methods have been removed
			}
		}
		int length = end - start + 1;
		System.arraycopy(this.methods, start, result = new MethodBinding[length], 0, length);
	} else {
		return Binding.NO_METHODS;
	}
	for (int i = 0, length = result.length - 1; i < length; i++) {
		MethodBinding method = result[i];
		for (int j = length; j > i; j--) {
			boolean paramsMatch = method.areParameterErasuresEqual(result[j]);
			if (paramsMatch) {
				methods();
				return getMethods(selector); // try again since the duplicate methods have been removed
			}
		}
	}
	return result;
}

public void generateSyntheticFinalFieldInitialization(CodeStream codeStream) {
	if (this.synthetics == null || this.synthetics[SourceTypeBinding.FIELD_EMUL] == null)
		return;
	Collection<FieldBinding> syntheticFields = this.synthetics[SourceTypeBinding.FIELD_EMUL].values();
	for (FieldBinding field : syntheticFields) {
		if (CharOperation.prefixEquals(TypeConstants.SYNTHETIC_SWITCH_ENUM_TABLE, field.name) && field.isFinal()) {
			MethodBinding[] accessors = (MethodBinding[]) this.synthetics[SourceTypeBinding.METHOD_EMUL].get(new String(field.name));
			if (accessors == null || accessors[0] == null) // not a field for switch enum
				continue;
			codeStream.invoke(Opcodes.OPC_invokestatic, accessors[0], null /* default declaringClass */);
			codeStream.fieldAccess(Opcodes.OPC_putstatic, field, null /* default declaringClass */);
		}
	}
}

/**
 * Answers the synthetic field for {@code <actualOuterLocalVariable>} or null if
 * one does not exist.
 */
public FieldBinding getSyntheticField(LocalVariableBinding actualOuterLocalVariable) {
	if (!isPrototype()) throw new IllegalStateException();

	if (this.synthetics == null || this.synthetics[SourceTypeBinding.FIELD_EMUL] == null) return null;
	return (FieldBinding) this.synthetics[SourceTypeBinding.FIELD_EMUL].get(actualOuterLocalVariable);
}

/**
 * Answers the synthetic field for {@code <targetEnclosingType>} or null if one
 * does not exist.
 */
public FieldBinding getSyntheticField(ReferenceBinding targetEnclosingType, boolean onlyExactMatch) {
	if (!isPrototype()) throw new IllegalStateException();

	if (this.synthetics == null || this.synthetics[SourceTypeBinding.FIELD_EMUL] == null) return null;
	FieldBinding field = (FieldBinding) this.synthetics[SourceTypeBinding.FIELD_EMUL].get(targetEnclosingType);
	if (field != null) return field;

	// type compatibility : to handle cases such as
	// class T { class M{}}
	// class S extends T { class N extends M {}} --> need to use S as a default enclosing instance for the super constructor call in N().
	if (!onlyExactMatch){
		Iterator accessFields = this.synthetics[SourceTypeBinding.FIELD_EMUL].values().iterator();
		while (accessFields.hasNext()) {
			field = (FieldBinding) accessFields.next();
			if (CharOperation.prefixEquals(TypeConstants.SYNTHETIC_ENCLOSING_INSTANCE_PREFIX, field.name)
				&& field.type.findSuperTypeOriginatingFrom(targetEnclosingType) != null)
					return field;
		}
	}
	return null;
}

/**
 * Answers the bridge method associated for an inherited methods or null if one
 * does not exist.
 */
public SyntheticMethodBinding getSyntheticBridgeMethod(MethodBinding inheritedMethodToBridge) {
	if (!isPrototype()) throw new IllegalStateException();

	if (this.synthetics == null) return null;
	if (this.synthetics[SourceTypeBinding.METHOD_EMUL] == null) return null;
	SyntheticMethodBinding[] accessors = (SyntheticMethodBinding[]) this.synthetics[SourceTypeBinding.METHOD_EMUL].get(inheritedMethodToBridge);
	if (accessors == null) return null;
	return accessors[1];
}

@Override
public boolean hasTypeBit(int bit) {
	if (!isPrototype()) {
		return this.prototype.hasTypeBit(bit);
	}
	// source types initialize type bits during connectSuperclass/interfaces()
	return (this.typeBits & bit) != 0;
}

/**
 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#initializeDeprecatedAnnotationTagBits()
 */
@Override
public void initializeDeprecatedAnnotationTagBits() {
	if (!isPrototype()) {
		this.prototype.initializeDeprecatedAnnotationTagBits();
		return;
	}
	if ((this.extendedTagBits & ExtendedTagBits.DeprecatedAnnotationResolved) == 0) {
		TypeDeclaration typeDecl = this.scope.referenceContext;
		boolean old = typeDecl.staticInitializerScope.insideTypeAnnotation;
		try {
			typeDecl.staticInitializerScope.insideTypeAnnotation = true;
			ASTNode.resolveDeprecatedAnnotations(typeDecl.staticInitializerScope, typeDecl.annotations, this);
			this.extendedTagBits |= ExtendedTagBits.DeprecatedAnnotationResolved;
		} finally {
			typeDecl.staticInitializerScope.insideTypeAnnotation = old;
		}
		if ((this.tagBits & TagBits.AnnotationDeprecated) != 0) {
			this.modifiers |= ClassFileConstants.AccDeprecated;
		}
	}
}

// ensure the receiver knows its hierarchy & fields/methods so static imports can be resolved correctly
// see bug 230026
@Override
void initializeForStaticImports() {
	if (!isPrototype()) {
		this.prototype.initializeForStaticImports();
		return;
	}
	if (this.scope == null) return; // already initialized

	if (this.superInterfaces == null)
		this.scope.connectTypeHierarchy();
	this.scope.buildComponents();
	this.scope.buildFields();
	this.scope.buildMethods();
}

@Override
int getNullDefault() {
	if (!isPrototype()) {
		return this.prototype.getNullDefault();
	}
	// ensure nullness defaults are initialized at all enclosing levels:
	switch (this.nullnessDefaultInitialized) {
	case 0:
		initializeNullDefaultAnnotation();
		//$FALL-THROUGH$
	case 1:
		getPackage().isViewedAsDeprecated(); // initialize annotations
		this.nullnessDefaultInitialized = 2;
	}
	return this.defaultNullness;
}

/**
 * Returns true if a type is identical to another one, or for generic types, true
 * if compared to its raw type.
 */
@Override
public boolean isEquivalentTo(TypeBinding otherType) {
	if (!isPrototype())
		return this.prototype.isEquivalentTo(otherType);

	if (TypeBinding.equalsEquals(this, otherType)) return true;
	if (otherType == null) return false;
	switch(otherType.kind()) {

		case Binding.WILDCARD_TYPE :
		case Binding.INTERSECTION_TYPE:
			return ((WildcardBinding) otherType).boundCheck(this);

		case Binding.PARAMETERIZED_TYPE :
			if ((otherType.tagBits & TagBits.HasDirectWildcard) == 0 && (!isMemberType() || !otherType.isMemberType()))
				return false; // should have been identical
			ParameterizedTypeBinding otherParamType = (ParameterizedTypeBinding) otherType;
			if (TypeBinding.notEquals(this, otherParamType.genericType()))
				return false;
			if (!isStatic()) { // static member types do not compare their enclosing
				ReferenceBinding enclosing = enclosingType();
				if (enclosing != null) {
					ReferenceBinding otherEnclosing = otherParamType.enclosingType();
					if (otherEnclosing == null) return false;
					if ((otherEnclosing.tagBits & TagBits.HasDirectWildcard) == 0) {
						if (TypeBinding.notEquals(enclosing, otherEnclosing)) return false;
					} else {
						if (!enclosing.isEquivalentTo(otherParamType.enclosingType())) return false;
					}
				}
			}
			int length = this.typeVariables == null ? 0 : this.typeVariables.length;
			TypeBinding[] otherArguments = otherParamType.arguments;
			int otherLength = otherArguments == null ? 0 : otherArguments.length;
			if (otherLength != length)
				return false;
			for (int i = 0; i < length; i++)
				if (!this.typeVariables[i].isTypeArgumentContainedBy(otherArguments[i]))
					return false;
			return true;

		case Binding.RAW_TYPE :
			return TypeBinding.equalsEquals(otherType.erasure(), this);
	}
	return false;
}

@Override
public boolean isGenericType() {
	if (!isPrototype())
		return this.prototype.isGenericType();
	return this.typeVariables != Binding.NO_TYPE_VARIABLES;
}

@Override
public boolean isHierarchyConnected() {
	if (!isPrototype())
		return this.prototype.isHierarchyConnected();
	return (this.tagBits & TagBits.EndHierarchyCheck) != 0;
}

@Override
public boolean isRepeatableAnnotationType() {
	if (!isPrototype()) throw new IllegalStateException();

	return this.containerAnnotationType != null;
}

@Override
public boolean isTaggedRepeatable() {  // tagged but not necessarily repeatable. see isRepeatableAnnotationType.
	return (this.tagBits & TagBits.AnnotationRepeatable) != 0;
}

@Override
public boolean canBeSeenBy(Scope sco) {
	SourceTypeBinding invocationType = sco.enclosingSourceType();
	if (TypeBinding.equalsEquals(invocationType, this)) return true;
	return ((this.environment.canTypeBeAccessed(this, sco)) && super.canBeSeenBy(sco));
}

@Override
public ReferenceBinding[] memberTypes() {
	if (!isPrototype()) {
		if ((this.tagBits & TagBits.HasUnresolvedMemberTypes) == 0)
			return sortedMemberTypes();
		// members obtained from the prototype are already sorted so it is safe
		// to set the sorted flag here immediately.
		ReferenceBinding [] members = this.memberTypes = this.prototype.memberTypes();
		int membersLength = members == null ? 0 : members.length;
		this.memberTypes = new ReferenceBinding[membersLength];
		for (int i = 0; i < membersLength; i++) {
			this.memberTypes[i] = this.environment.createMemberType(members[i], this);
		}
		this.tagBits &= ~TagBits.HasUnresolvedMemberTypes;
		this.memberTypesSorted = true;
	}
	return sortedMemberTypes();
}

private ReferenceBinding[] sortedMemberTypes() {
	if (!this.memberTypesSorted) {
		// lazily sort member types
		int length = this.memberTypes.length;
		if (length > 1)
			sortMemberTypes(this.memberTypes, 0, length);
		this.memberTypesSorted = true;
	}
	return this.memberTypes;
}

@Override
public boolean hasMemberTypes() {
	if (!isPrototype())
		return this.prototype.hasMemberTypes();
	return this.memberTypes.length > 0;
}

// NOTE: the return type, arg & exception types of each method of a source type are resolved when needed
@Override
public MethodBinding[] methods() {

	if (!isPrototype()) {
		if ((this.tagBits & TagBits.AreMethodsComplete) != 0)
			return this.methods;
		this.tagBits |= TagBits.AreMethodsComplete;
		return this.methods = this.prototype.methods();
	}

	if ((this.tagBits & TagBits.AreMethodsComplete) != 0)
		return this.methods;

	if (!areMethodsInitialized()) { // https://bugs.eclipse.org/384663
		this.scope.buildMethods();
	}

	if ((this.tagBits & TagBits.HasUnresolvedComponents) != 0)
		components();

	// lazily sort methods
	if ((this.tagBits & TagBits.AreMethodsSorted) == 0) {
		int length = this.methods.length;
		if (length > 1)
			ReferenceBinding.sortMethods(this.methods, 0, length);
		this.tagBits |= TagBits.AreMethodsSorted;
	}

	int failed = 0;
	MethodBinding[] resolvedMethods = this.methods;
	try {
		for (int i = 0, length = this.methods.length; i < length; i++) {
			if ((this.tagBits & TagBits.AreMethodsComplete) != 0) {
				// recursive call to methods() from resolveTypesFor(..) resolved the methods
				return this.methods;
			}

			if (resolveTypesFor(this.methods[i]) == null) {
				// do not alter original method array until resolution is over, due to reentrance (143259)
				if (resolvedMethods == this.methods) {
					System.arraycopy(this.methods, 0, resolvedMethods = new MethodBinding[length], 0, length);
				}
				resolvedMethods[i] = null; // unable to resolve parameters
				failed++;
			}
		}

		for (int i = 0, length = this.methods.length; i < length; i++) {
			int severity = ProblemSeverities.Error;
			MethodBinding method = resolvedMethods[i];
			if (method == null)
				continue;
			char[] selector = method.selector;
			AbstractMethodDeclaration methodDecl = null;
			nextSibling: for (int j = i + 1; j < length; j++) {
				MethodBinding method2 = resolvedMethods[j];
				if (method2 == null)
					continue nextSibling;
				if (!CharOperation.equals(selector, method2.selector))
					break nextSibling; // methods with same selector are contiguous

				if (method.areParameterErasuresEqual(method2)) {
					// we now ignore return types in 1.7 when detecting duplicates, just as we did before 1.5
					// Only in 1.6, we have to make sure even return types are different
					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317719
				} else {
					continue nextSibling;
				}
				// otherwise duplicates / name clash
				boolean isEnumSpecialMethod = isEnum() && (CharOperation.equals(selector,TypeConstants.VALUEOF) || CharOperation.equals(selector,TypeConstants.VALUES));
				// report duplicate
				boolean removeMethod2 = (severity == ProblemSeverities.Error) ? true : false; // do not remove if in 1.6 and just a warning given
				if (methodDecl == null) {
					methodDecl = method.sourceMethod(); // cannot be retrieved after binding is lost & may still be null if method is special
					if (methodDecl != null && methodDecl.binding != null) { // ensure its a valid user defined method
						boolean removeMethod = method.returnType == null && method2.returnType != null;
						if (isEnumSpecialMethod) {
							this.scope.problemReporter().duplicateEnumSpecialMethod(this, methodDecl);
							// remove user defined methods & keep the synthetic
							removeMethod = true;
						} else {
							this.scope.problemReporter().duplicateMethodInType(methodDecl, method.areParametersEqual(method2), severity);
						}
						if (removeMethod) {
							removeMethod2 = false;
							methodDecl.binding = null;
							// do not alter original method array until resolution is over, due to reentrance (143259)
							if (resolvedMethods == this.methods)
								System.arraycopy(this.methods, 0, resolvedMethods = new MethodBinding[length], 0, length);
							resolvedMethods[i] = null;
							failed++;
						}
					}
				}
				AbstractMethodDeclaration method2Decl = method2.sourceMethod();
				if (method2Decl != null && method2Decl.binding != null) { // ensure its a valid user defined method
					if (isEnumSpecialMethod) {
						this.scope.problemReporter().duplicateEnumSpecialMethod(this, method2Decl);
						removeMethod2 = true;
					} else {
						this.scope.problemReporter().duplicateMethodInType(method2Decl, method.areParametersEqual(method2), severity);
					}
					if (removeMethod2) {
						method2Decl.binding = null;
						// do not alter original method array until resolution is over, due to reentrance (143259)
						if (resolvedMethods == this.methods)
							System.arraycopy(this.methods, 0, resolvedMethods = new MethodBinding[length], 0, length);
						resolvedMethods[j] = null;
						failed++;
					}
				}
			}
			// GROOVY add
			if (method instanceof LazilyResolvedMethodBinding) continue;
			// GROOVY end
			if (method.returnType == null && resolvedMethods[i] != null) { // forget method with invalid return type... was kept to detect possible collisions
				methodDecl = method.sourceMethod();
				if (methodDecl != null)
					methodDecl.binding = null;
				// do not alter original method array until resolution is over, due to reentrance (143259)
				if (resolvedMethods == this.methods)
					System.arraycopy(this.methods, 0, resolvedMethods = new MethodBinding[length], 0, length);
				resolvedMethods[i] = null;
				failed++;
			}
		}
	} finally {
		if ((this.tagBits & TagBits.AreMethodsComplete) != 0) {
			// recursive call to methods() from resolveTypesFor(..) resolved the methods
			return this.methods;
		}
		if (failed > 0) {
			int newSize = resolvedMethods.length - failed;
			if (newSize == 0) {
				setMethods(Binding.NO_METHODS);
			} else {
				MethodBinding[] newMethods = new MethodBinding[newSize];
				for (int i = 0, j = 0, length = resolvedMethods.length; i < length; i++)
					if (resolvedMethods[i] != null)
						newMethods[j++] = resolvedMethods[i];
				setMethods(newMethods);
			}
		}
		if (this.isRecord())
			addRequiredSpecialRecordMethods();

		this.tagBits |= TagBits.AreMethodsComplete;
	}
	return this.methods;
}

private void addRequiredSpecialRecordMethods() {

	RecordComponentBinding[] rcbs = this.components;
	int rcLength = rcbs.length;

	List<MethodBinding> syntheticMethods = new ArrayList<>();
	List<RecordComponentBinding> missingAccessors = new ArrayList<>(Arrays.asList(rcbs));
	boolean needHashCode = true, needEquals = true, needToString = true, needConstructor = true;

nextMethod:
	for (int i = 0, length = this.methods.length; i < length; i++) {
		MethodBinding method = this.methods[i];
		if (method == null)
			continue;
		if (method.isConstructor()) {
			if (!needConstructor || method.parameters.length != rcbs.length)
				continue;
			for (int j = 0; j < rcLength; ++j) {
				TypeBinding mpt = method.parameters[j];
				TypeBinding rct = rcbs[j].type;
				if (TypeBinding.notEquals(mpt.erasure(), rct.erasure()))
					continue nextMethod;
			}
			AbstractMethodDeclaration methodDecl = method.sourceMethod();
			methodDecl.bits |= ASTNode.IsCanonicalConstructor;
			method.extendedTagBits |= ExtendedTagBits.IsCanonicalConstructor;
			needConstructor = false;
			continue;
		}

		if (CharOperation.equals(method.selector, TypeConstants.TOSTRING)) {
			if (method.parameters == null || method.parameters.length == 0)
				needToString = false;
			continue;
		}

		if (CharOperation.equals(method.selector, TypeConstants.HASHCODE)) {
			if (method.parameters == null || method.parameters.length == 0)
				needHashCode = false;
			continue;
		}

		if (CharOperation.equals(method.selector, TypeConstants.EQUALS)) {
			if (method.parameters != null && method.parameters.length == 1 && TypeBinding.equalsEquals(method.parameters[0], this.scope.getJavaLangObject()))
				needEquals = false;
			// fall through and check if method is an accessor; Unlike toString and hashCode, equals is a valid component name.
		}

		for (int j = 0; j < rcLength; j++) {
			RecordComponentBinding rcb = rcbs[j];
			if (CharOperation.equals(method.selector, rcb.name) && (method.parameterNames == null || method.parameterNames.length == 0)) {
				missingAccessors.remove(rcb);
				method.modifiers |= ExtraCompilerModifiers.AccOverriding;
				continue nextMethod;
			}
		}
	}

	for (RecordComponentBinding rcb : missingAccessors)
		syntheticMethods.add(addSyntheticRecordComponentAccessor(rcb));

	if (needToString)
		syntheticMethods.add(addSyntheticRecordOverrideMethod(TypeConstants.TOSTRING));
	if (needHashCode)
		syntheticMethods.add(addSyntheticRecordOverrideMethod(TypeConstants.HASHCODE));
	if (needEquals)
		syntheticMethods.add(addSyntheticRecordOverrideMethod(TypeConstants.EQUALS));

	if (needConstructor)
		syntheticMethods.add(addSyntheticCanonicalConstructor());

	final int newOnes = syntheticMethods.size();
	if (newOnes > 0) {
		int length = this.methods.length;
		System.arraycopy(this.methods, 0, setMethods(new MethodBinding[length + newOnes]), 0, length);
		System.arraycopy(syntheticMethods.toArray(), 0, this.methods, length, newOnes);
		length = length + newOnes;
		if (length > 1)
			ReferenceBinding.sortMethods(this.methods, 0, length); // TagBits.AreMethodsSorted; -- already set in #methods()
	}
}

@Override
public ReferenceBinding[] permittedTypes() {
	if (!isPrototype())
		return this.permittedTypes = this.prototype.permittedTypes();
	return this.permittedTypes;
}

@Override
public TypeBinding prototype() {
	return this.prototype;
}

public boolean isPrototype() {
	return this == this.prototype;  //$IDENTITY-COMPARISON$
}

@Override
public boolean isImplicitType() {
	return this.isImplicit;
}
@Override
public ReferenceBinding containerAnnotationType() {
	if (!isPrototype()) throw new IllegalStateException();

	if (this.containerAnnotationType instanceof UnresolvedReferenceBinding) {
		this.containerAnnotationType = (ReferenceBinding)BinaryTypeBinding.resolveType(this.containerAnnotationType, this.scope.environment(), false);
	}
	return this.containerAnnotationType;
}

public FieldBinding resolveTypeFor(FieldBinding field) {
	return (FieldBinding) resolveTypeFor((VariableBinding) field);
}

public MethodBinding resolveTypesFor(MethodBinding method) {
	ProblemReporter problemReporter = this.scope.problemReporter();
	try {
		IErrorHandlingPolicy suspendedPolicy = problemReporter.suspendTempErrorHandlingPolicy();
		try {
			return resolveTypesWithSuspendedTempErrorHandlingPolicy(method);
		} finally {
			problemReporter.resumeTempErrorHandlingPolicy(suspendedPolicy);
		}
	} finally {
		problemReporter.close();
	}
}

private MethodBinding resolveTypesWithSuspendedTempErrorHandlingPolicy(MethodBinding method) {
	if (!isPrototype())
		return this.prototype.resolveTypesFor(method);

	if ((method.modifiers & ExtraCompilerModifiers.AccUnresolved) == 0)
		return method;

	final long sourceLevel = this.scope.compilerOptions().sourceLevel;
	ReferenceBinding object = this.scope.getJavaLangObject();
	TypeVariableBinding[] tvb = method.typeVariables;
	for (int i = 0; i < tvb.length; i++)
		tvb[i].superclass = object;		// avoid null (see https://bugs.eclipse.org/426048)

	if ((method.getAnnotationTagBits() & TagBits.AnnotationDeprecated) != 0)
		method.modifiers |= ClassFileConstants.AccDeprecated;
	if (hasRestrictedAccess())
		method.modifiers |= ExtraCompilerModifiers.AccRestrictedAccess;

	AbstractMethodDeclaration methodDecl = method.sourceMethod();
	if (methodDecl == null) {
		// GROOVY add
		if (method.problemId() == ProblemReasons.NoError && method instanceof LazilyResolvedMethodBinding) {
			LazilyResolvedMethodBinding lrMethod = (LazilyResolvedMethodBinding) method;
			// the rest is a copy of the code below but doesn't depend on the method declaration
			// nothing to do for method type parameters (there are none)
			// nothing to do for method exceptions (there are none)
			TypeBinding ptb = lrMethod.getParameterTypeBinding();
			if (ptb == null) {
				method.parameters = Binding.NO_PARAMETERS;
			} else {
				method.parameters = new TypeBinding[] {ptb};
			}
			method.returnType = lrMethod.getReturnTypeBinding();
			method.modifiers &= ~ExtraCompilerModifiers.AccUnresolved;
			return method;
		}
		// GROOVY end
		return null; // method could not be resolved in previous iteration
	}

	TypeParameter[] typeParameters = methodDecl.typeParameters();
	if (typeParameters != null) {
		methodDecl.scope.connectTypeVariables(typeParameters, true);
		// Perform deferred bound checks for type variables (only done after type variable hierarchy is connected)
		for (TypeParameter typeParameter : typeParameters) {
			typeParameter.checkBounds(methodDecl.scope);
		}
	}
	TypeReference[] exceptionTypes = methodDecl.thrownExceptions;
	if (exceptionTypes != null) {
		int size = exceptionTypes.length;
		method.thrownExceptions = new ReferenceBinding[size];
		int count = 0;
		ReferenceBinding resolvedExceptionType;
		for (int i = 0; i < size; i++) {
			resolvedExceptionType = (ReferenceBinding) exceptionTypes[i].resolveType(methodDecl.scope, true /* check bounds*/);
			if (resolvedExceptionType == null)
				continue;
			if (resolvedExceptionType.isBoundParameterizedType()) {
				methodDecl.scope.problemReporter().invalidParameterizedExceptionType(resolvedExceptionType, exceptionTypes[i]);
				continue;
			}
			if (resolvedExceptionType.findSuperTypeOriginatingFrom(TypeIds.T_JavaLangThrowable, true) == null) {
				if (resolvedExceptionType.isValidBinding()) {
					methodDecl.scope.problemReporter().cannotThrowType(exceptionTypes[i], resolvedExceptionType);
					continue;
				}
			}
			if ((resolvedExceptionType.tagBits & TagBits.HasMissingType) != 0) {
				method.tagBits |= TagBits.HasMissingType;
			}
			if (exceptionTypes[i].hasNullTypeAnnotation(AnnotationPosition.ANY)) {
				methodDecl.scope.problemReporter().nullAnnotationUnsupportedLocation(exceptionTypes[i]);
			}
			method.modifiers |= (resolvedExceptionType.modifiers & ExtraCompilerModifiers.AccGenericSignature);
			method.thrownExceptions[count++] = resolvedExceptionType;
		}
		if (count < size)
			System.arraycopy(method.thrownExceptions, 0, method.thrownExceptions = new ReferenceBinding[count], 0, count);
	}

	if (methodDecl.receiver != null) {
		method.receiver = methodDecl.receiver.type.resolveType(methodDecl.scope, true /* check bounds*/);
	}
	final boolean reportUnavoidableGenericTypeProblems = this.scope.compilerOptions().reportUnavoidableGenericTypeProblems;
	boolean foundArgProblem = false;
	boolean checkAPIleak = methodDecl.scope.shouldCheckAPILeaks(this, method.isPublic());
	Argument[] arguments = methodDecl.arguments;
	if (arguments != null) {
		int size = arguments.length;
		method.parameters = Binding.NO_PARAMETERS;
		TypeBinding[] newParameters = new TypeBinding[size];
		for (int i = 0; i < size; i++) {
			Argument arg = arguments[i];
			if (arg.annotations != null) {
				method.tagBits |= TagBits.HasParameterAnnotations;
			}
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817
			boolean deferRawTypeCheck = !reportUnavoidableGenericTypeProblems && !method.isConstructor() && (arg.type.bits & ASTNode.IgnoreRawTypeCheck) == 0;
			TypeBinding parameterType;
			if (deferRawTypeCheck) {
				arg.type.bits |= ASTNode.IgnoreRawTypeCheck;
			}
			try {
				ASTNode.handleNonNullByDefault(methodDecl.scope, arg.annotations, arg);
				// don't pass optional 'location' arg, to avoid applying @NNBD before ImplicitNullAnnotationVerifier has run:
				parameterType = arg.type.resolveType(methodDecl.scope, true /* check bounds*/);
			} finally {
				if (deferRawTypeCheck) {
					arg.type.bits &= ~ASTNode.IgnoreRawTypeCheck;
				}
			}

			if (parameterType == null) {
				foundArgProblem = true;
			} else if (parameterType == TypeBinding.VOID) {
				methodDecl.scope.problemReporter().argumentTypeCannotBeVoid(methodDecl, arg);
				foundArgProblem = true;
			} else {
				if ((parameterType.tagBits & TagBits.HasMissingType) != 0) {
					method.tagBits |= TagBits.HasMissingType;
				}
				TypeBinding leafType = parameterType.leafComponentType();
				if (leafType instanceof ReferenceBinding && (((ReferenceBinding) leafType).modifiers & ExtraCompilerModifiers.AccGenericSignature) != 0)
					method.modifiers |= ExtraCompilerModifiers.AccGenericSignature;
				newParameters[i] = parameterType;
				if (checkAPIleak)
					methodDecl.scope.detectAPILeaks(arg.type, parameterType);
				arg.binding = new LocalVariableBinding(arg, parameterType, arg.modifiers, methodDecl.scope);
			}
		}
		// only assign parameters if no problems are found
		if (!foundArgProblem) {
			method.parameters = newParameters;
		}
	} else if (method.isCompactConstructor()) {
		RecordComponentBinding[] rcbs = components();
		int length = rcbs.length;
		method.parameters = new TypeBinding[length];
		AnnotationBinding[][] methodsParameterAnnotations = null;
		for (int i = 0; i < length; i++ ) {
			method.parameters[i] = rcbs[i].type;
			TypeBinding leafType = rcbs[i].type == null ? null : rcbs[i].type.leafComponentType();
			if (leafType instanceof ReferenceBinding && (((ReferenceBinding) leafType).modifiers & ExtraCompilerModifiers.AccGenericSignature) != 0)
				method.modifiers |= ExtraCompilerModifiers.AccGenericSignature;
			if (rcbs[i].type.hasTypeAnnotations())
				methodDecl.bits |= ASTNode.HasTypeAnnotations;
			// bind the implicit argument already.
			final LocalVariableBinding implicitArgument = new SyntheticArgumentBinding(rcbs[i]);
			implicitArgument.tagBits |= rcbs[i].tagBits & (TagBits.AnnotationNullMASK | TagBits.AnnotationOwningMASK);
			methodDecl.scope.addLocalVariable(implicitArgument);
			List<AnnotationBinding> propagatedAnnotations = new ArrayList<>();
			ASTNode.getRelevantAnnotations(rcbs[i].sourceRecordComponent().annotations, TagBits.AnnotationForParameter, propagatedAnnotations);
			AnnotationBinding[] annotationsForParameter = propagatedAnnotations.toArray(new AnnotationBinding[0]);
			if (annotationsForParameter != null && annotationsForParameter.length > 0) {
				implicitArgument.setAnnotations(annotationsForParameter, this.scope, true);
				implicitArgument.extendedTagBits |= ExtendedTagBits.AllAnnotationsResolved;
				if (methodsParameterAnnotations == null) {
					methodsParameterAnnotations = new AnnotationBinding[length][];
					for (int j = 0; j < i; j++) {
						methodsParameterAnnotations[j] = Binding.NO_ANNOTATIONS;
					}
				}
				methodsParameterAnnotations[i] = annotationsForParameter;
			} else if (methodsParameterAnnotations != null) {
				methodsParameterAnnotations[i] = Binding.NO_ANNOTATIONS;
			}
		}
		if (methodsParameterAnnotations != null) {
			methodDecl.binding.tagBits |= TagBits.HasParameterAnnotations;
			methodDecl.binding.setParameterAnnotations(methodsParameterAnnotations);
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=337799
	if ((method.tagBits & TagBits.AnnotationSafeVarargs) != 0) {
		if (!method.isVarargs()) {
			methodDecl.scope.problemReporter().safeVarargsOnFixedArityMethod(method);
		} else if (!method.isStatic() && !method.isFinal() && !method.isConstructor()
				&& !(sourceLevel >= ClassFileConstants.JDK9 && method.isPrivate())) {
			methodDecl.scope.problemReporter().safeVarargsOnNonFinalInstanceMethod(method);
		}
	} else {
		AbstractVariableDeclaration [] argv = methodDecl.arguments(true);
		AbstractVariableDeclaration argument = argv != null && argv.length > 0 ? argv[argv.length - 1] : null;
		if (argument != null)
			checkAndFlagHeapPollution(method, argument);
	}

	boolean foundReturnTypeProblem = false;
	if (!method.isConstructor()) {
		TypeReference returnType = methodDecl instanceof MethodDeclaration
			? ((MethodDeclaration) methodDecl).returnType
			: null;
		if (returnType == null) {
			methodDecl.scope.problemReporter().missingReturnType(methodDecl);
			method.returnType = null;
			foundReturnTypeProblem = true;
		} else {
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817
			boolean deferRawTypeCheck = !reportUnavoidableGenericTypeProblems && (returnType.bits & ASTNode.IgnoreRawTypeCheck) == 0;
			TypeBinding methodType;
			if (deferRawTypeCheck) {
				returnType.bits |= ASTNode.IgnoreRawTypeCheck;
			}
			try {
				methodType = returnType.resolveType(methodDecl.scope, true /* check bounds*/);
			} finally {
				if (deferRawTypeCheck) {
					returnType.bits &= ~ASTNode.IgnoreRawTypeCheck;
				}
			}
			if (methodType == null) {
				foundReturnTypeProblem = true;
			} else {
				if ((methodType.tagBits & TagBits.HasMissingType) != 0) {
					method.tagBits |= TagBits.HasMissingType;
				}
				method.returnType = methodType;
				if (!method.isVoidMethod()) {
					Annotation [] annotations = methodDecl.annotations;
					if (annotations != null && annotations.length != 0) {
						ASTNode.copySE8AnnotationsToType(methodDecl.scope, method, methodDecl.annotations, false);
					}
					Annotation.isTypeUseCompatible(returnType, this.scope, methodDecl.annotations);
				}
				TypeBinding leafType = methodType.leafComponentType();
				if (leafType instanceof ReferenceBinding && (((ReferenceBinding) leafType).modifiers & ExtraCompilerModifiers.AccGenericSignature) != 0)
					method.modifiers |= ExtraCompilerModifiers.AccGenericSignature;
				else if (leafType == TypeBinding.VOID && methodDecl.annotations != null)
					rejectTypeAnnotatedVoidMethod(methodDecl);
				if (checkAPIleak)
					methodDecl.scope.detectAPILeaks(returnType, methodType);
			}
		}
	} else {
		Annotation [] annotations = methodDecl.annotations;
		if (annotations != null && annotations.length != 0) {
			ASTNode.copySE8AnnotationsToType(methodDecl.scope, method, methodDecl.annotations, false);
		}
	}
	if (foundArgProblem) {
		methodDecl.binding = null;
		method.parameters = Binding.NO_PARAMETERS; // see 107004
		// nullify type parameter bindings as well as they have a backpointer to the method binding
		// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=81134)
		if (typeParameters != null)
			for (int i = 0, length = typeParameters.length; i < length; i++)
				typeParameters[i].binding = null;
		return null;
	}
	CompilerOptions compilerOptions = this.scope.compilerOptions();
	if (compilerOptions.isAnnotationBasedNullAnalysisEnabled) {
		if (!method.isConstructor() && method.returnType != null) {
			long nullTagBits = method.tagBits & TagBits.AnnotationNullMASK;
			if (nullTagBits != 0) {
				TypeReference returnTypeRef = ((MethodDeclaration)methodDecl).returnType;
				if (this.scope.environment().usesNullTypeAnnotations()) {
					if (!this.scope.validateNullAnnotation(nullTagBits, returnTypeRef, methodDecl.annotations))
						method.returnType.tagBits &= ~TagBits.AnnotationNullMASK;
					method.tagBits &= ~TagBits.AnnotationNullMASK;
				} else {
					if (!this.scope.validateNullAnnotation(nullTagBits, returnTypeRef, methodDecl.annotations))
						method.tagBits &= ~TagBits.AnnotationNullMASK;
				}
			}
		}
	}
	if (this.externalAnnotationProvider != null)
		ExternalAnnotationSuperimposer.annotateMethodBinding(method, arguments, this.externalAnnotationProvider, this.environment);
	if (compilerOptions.storeAnnotations)
		createArgumentBindings(method, compilerOptions); // need annotations resolved already at this point
	if (foundReturnTypeProblem)
		return method; // but its still unresolved with a null return type & is still connected to its method declaration

	method.modifiers &= ~ExtraCompilerModifiers.AccUnresolved;
	return method;
}

public void checkAndFlagHeapPollution(MethodBinding method, AbstractVariableDeclaration argument) {
	if (method.parameters != null && method.parameters.length > 0 && method.isVarargs()) { // https://bugs.eclipse.org/bugs/show_bug.cgi?id=337795
		if (!method.parameters[method.parameters.length - 1].isReifiable()) {
			this.scope.problemReporter().possibleHeapPollutionFromVararg(argument);
		}
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391108
private static void rejectTypeAnnotatedVoidMethod(AbstractMethodDeclaration methodDecl) {
	Annotation[] annotations = methodDecl.annotations;
	int length = annotations == null ? 0 : annotations.length;
	for (int i = 0; i < length; i++) {
		ReferenceBinding binding = (ReferenceBinding) annotations[i].resolvedType;
		if (binding != null
				&& (binding.tagBits & TagBits.AnnotationForTypeUse) != 0
				&& (binding.tagBits & TagBits.AnnotationForMethod) == 0) {
			methodDecl.scope.problemReporter().illegalUsageOfTypeAnnotations(annotations[i]);
		}
	}
}

private void createArgumentBindings(MethodBinding method, CompilerOptions compilerOptions) {
	if (!isPrototype()) throw new IllegalStateException();

	if (compilerOptions.isAnnotationBasedNullAnalysisEnabled)
		getNullDefault(); // ensure initialized
	AbstractMethodDeclaration methodDecl = method.sourceMethod();
	if (methodDecl != null) {
		// while creating argument bindings we also collect explicit null annotations:
		if (method.parameters != Binding.NO_PARAMETERS)
			methodDecl.createArgumentBindings();
		// add implicit annotations (inherited(?) & default):
		if (compilerOptions.isAnnotationBasedNullAnalysisEnabled) {
			new ImplicitNullAnnotationVerifier(this.scope.environment()).checkImplicitNullAnnotations(method, methodDecl, true, this.scope);
		}
	}
}

public void evaluateNullAnnotations() {
	if (!isPrototype()) throw new IllegalStateException();

	if (this.nullnessDefaultInitialized > 0 || !this.scope.compilerOptions().isAnnotationBasedNullAnalysisEnabled)
		return;

	if ((this.tagBits & TagBits.AnnotationNullMASK) != 0) {
		Annotation[] annotations = this.scope.referenceContext.annotations;
		for (Annotation annotation : annotations) {
			ReferenceBinding annotationType = annotation.getCompilerAnnotation().getAnnotationType();
			if (annotationType != null) {
				if (annotationType.hasNullBit(TypeIds.BitNonNullAnnotation|TypeIds.BitNullableAnnotation)) {
					this.scope.problemReporter().nullAnnotationUnsupportedLocation(annotation);
					this.tagBits &= ~TagBits.AnnotationNullMASK;
				}
			}
		}
	}

	boolean isPackageInfo = CharOperation.equals(this.sourceName, TypeConstants.PACKAGE_INFO_NAME);
	PackageBinding pkg = getPackage();
	boolean isInDefaultPkg = (pkg.compoundName == CharOperation.NO_CHAR_CHAR);
	if (!isPackageInfo) {
		boolean isInNullnessAnnotationPackage = (pkg.extendedTagBits & ExtendedTagBits.IsNullAnnotationPackage) != 0;
		if (pkg.getDefaultNullness() == NO_NULL_DEFAULT && !isInDefaultPkg && !isInNullnessAnnotationPackage && !(this instanceof NestedTypeBinding)) {
			ReferenceBinding packageInfo = pkg.getType(TypeConstants.PACKAGE_INFO_NAME, this.module);
			if (packageInfo == null) {
				// no pkgInfo - complain
				this.scope.problemReporter().missingNonNullByDefaultAnnotation(this.scope.referenceContext);
				pkg.setDefaultNullness(NULL_UNSPECIFIED_BY_DEFAULT);
			} else {
				// if pkgInfo has no default annot. - complain
				packageInfo.getAnnotationTagBits();
			}
		}
	}
	this.nullnessDefaultInitialized = 1;
	if (this.defaultNullness != 0) {
		TypeDeclaration typeDecl = this.scope.referenceContext;
		if (isPackageInfo) {
			if (pkg.enclosingModule.getDefaultNullness() == this.defaultNullness) {
				this.scope.problemReporter().nullDefaultAnnotationIsRedundant(typeDecl, typeDecl.annotations, pkg.enclosingModule);
			} else {
				pkg.setDefaultNullness(this.defaultNullness);
			}
		} else {
			Binding target = this.scope.parent.checkRedundantDefaultNullness(this.defaultNullness, typeDecl.declarationSourceStart);
			if(target != null) {
				this.scope.problemReporter().nullDefaultAnnotationIsRedundant(typeDecl, typeDecl.annotations, target);
			}
		}
	} else if (isPackageInfo || (isInDefaultPkg && !(this instanceof NestedTypeBinding))) {
		this.scope.problemReporter().missingNonNullByDefaultAnnotation(this.scope.referenceContext);
		if (!isInDefaultPkg)
			pkg.setDefaultNullness(NULL_UNSPECIFIED_BY_DEFAULT);
	}
	maybeMarkTypeParametersNonNull();
}

private void maybeMarkTypeParametersNonNull() {
	if (this.typeVariables != null && this.typeVariables.length > 0) {
		// when creating type variables we didn't yet have the defaultNullness, fill it in now:
		if (this.scope == null || !this.scope.hasDefaultNullnessFor(DefaultLocationTypeParameter, this.sourceStart()))
			return;
		AnnotationBinding[] annots = new AnnotationBinding[]{ this.environment.getNonNullAnnotation() };
		for (int i = 0; i < this.typeVariables.length; i++) {
			TypeVariableBinding tvb = this.typeVariables[i];
			TypeParameter typeParameter = this.scope.referenceContext.typeParameters[i];
			if (typeParameter.annotations != null && (tvb.extendedTagBits & ExtendedTagBits.AnnotationResolved) == 0)
				continue; // not yet ready
			if ((tvb.tagBits & TagBits.AnnotationNullMASK) == 0)
				this.typeVariables[i] = (TypeVariableBinding) this.environment.createAnnotatedType(tvb, annots);
		}
	}
}

@Override
boolean hasNonNullDefaultForType(TypeBinding type, int location, int sourceStart) {

	if (!isPrototype()) throw new IllegalStateException();

	if (this.scope == null) {
		return (this.defaultNullness & location) != 0;
	}
	if (this.scope.environment().usesNullTypeAnnotations() && type != null && !type.acceptsNonNullDefault())
		return false;
	Scope skope = this.scope.referenceContext.initializerScope; // for @NNBD on a field
	if (skope == null)
		skope = this.scope;
	return skope.hasDefaultNullnessFor(location, sourceStart);
}

@Override
protected boolean hasMethodWithNumArgs(char[] selector, int numArgs) {
	if ((this.tagBits & TagBits.AreMethodsComplete) != 0)
		return super.hasMethodWithNumArgs(selector, numArgs);
	// otherwise don't trigger unResolvedMethods() which would actually resolve!
	if (this.scope != null && this.scope.referenceContext.methods != null) {
		for (AbstractMethodDeclaration method : this.scope.referenceContext.methods) {
			if (CharOperation.equals(method.selector, selector)) {
				AbstractVariableDeclaration [] arguments = method.arguments(true);
				if (numArgs == 0) {
					if (arguments == null)
						return true;
				} else {
					if (arguments != null && arguments.length == numArgs)
						return true;
				}
			}
		}
	}
	return false;
}
@Override
public AnnotationBinding[] getAnnotations(long requestedInitialization) {
	AnnotationHolder holder = retrieveAnnotationHolder(prototype(), requestedInitialization);
	return holder == null ? Binding.NO_ANNOTATIONS : holder.getAnnotations();
}

@Override
public AnnotationHolder retrieveAnnotationHolder(Binding binding, boolean forceInitialization) {
	return retrieveAnnotationHolder(binding, ExtendedTagBits.AllAnnotationsResolved);
}
private AnnotationHolder retrieveAnnotationHolder(Binding binding, long requestedInitialization) {
	if (!isPrototype())
		return this.prototype.retrieveAnnotationHolder(binding, requestedInitialization);
	if (requestedInitialization == ExtendedTagBits.AllAnnotationsResolved) {
		binding.getAnnotationTagBits(); // ensure all annotations are up to date
	} else {
		if ((requestedInitialization & ExtendedTagBits.DeprecatedAnnotationResolved) != 0)
			binding.initializeDeprecatedAnnotationTagBits(); // selective initialization
	}
	return super.retrieveAnnotationHolder(binding, false);
}

@Override
public void setContainerAnnotationType(ReferenceBinding value) {
	if (!isPrototype()) throw new IllegalStateException();

	this.containerAnnotationType = value;
}

@Override
public void tagAsHavingDefectiveContainerType() {
	if (!isPrototype()) throw new IllegalStateException();

	if (this.containerAnnotationType != null && this.containerAnnotationType.isValidBinding())
		this.containerAnnotationType = new ProblemReferenceBinding(this.containerAnnotationType.compoundName, this.containerAnnotationType, ProblemReasons.DefectiveContainerAnnotationType);
}

/**
 * Propagates writes to all annotated variants so the clones evolve along.
 */
public RecordComponentBinding[] setComponents(RecordComponentBinding[] components) {
	if (!isPrototype())
		return this.prototype.setComponents(components);

	if ((this.tagBits & TagBits.HasAnnotatedVariants) != 0) {
		TypeBinding [] annotatedTypes = this.scope.environment().getAnnotatedTypes(this);
		for (int i = 0, length = annotatedTypes == null ? 0 : annotatedTypes.length; i < length; i++) {
			SourceTypeBinding annotatedType = (SourceTypeBinding) annotatedTypes[i];
			annotatedType.components = components;
		}
	}

	for (RecordComponentBinding component : components) {
		for (FieldBinding field : this.fields) {
			if (CharOperation.equals(field.name, component.name)) { // field got built before record component resolution
				field.type = component.type;
				field.modifiers |= component.modifiers & ExtraCompilerModifiers.AccGenericSignature;
				field.tagBits |= component.tagBits & (TagBits.AnnotationNullMASK | TagBits.AnnotationOwningMASK);
				if ((component.tagBits & TagBits.HasMissingType) != 0)
					field.tagBits |= TagBits.HasMissingType;
				RecordComponent componentDecl = component.sourceRecordComponent();
				if (componentDecl != null &&  componentDecl.annotations != null)
					ASTNode.copyRecordComponentAnnotations(this.scope, field, componentDecl.annotations);
			}
		}
	}
	return this.components = components;
}

/**
 * Propagates writes to all annotated variants so the clones evolve along.
 */
public FieldBinding[] setFields(FieldBinding[] fields) {
	if (!isPrototype())
		return this.prototype.setFields(fields);

	if ((this.tagBits & TagBits.HasAnnotatedVariants) != 0) {
		TypeBinding [] annotatedTypes = this.scope.environment().getAnnotatedTypes(this);
		for (int i = 0, length = annotatedTypes == null ? 0 : annotatedTypes.length; i < length; i++) {
			SourceTypeBinding annotatedType = (SourceTypeBinding) annotatedTypes[i];
			annotatedType.fields = fields;
		}
	}
	return this.fields = fields;
}

// We need to specialize member types, can't just propagate. Can't specialize here, clones could created post setMemberTypes()
public ReferenceBinding[] setMemberTypes(ReferenceBinding[] memberTypes) {
	if (!isPrototype())
		return this.prototype.setMemberTypes(memberTypes);

	this.memberTypes = memberTypes;
	if ((this.tagBits & TagBits.HasAnnotatedVariants) != 0) {
		TypeBinding [] annotatedTypes = this.scope.environment().getAnnotatedTypes(this);
		for (int i = 0, length = annotatedTypes == null ? 0 : annotatedTypes.length; i < length; i++) {
			SourceTypeBinding annotatedType = (SourceTypeBinding) annotatedTypes[i];
			annotatedType.tagBits |= TagBits.HasUnresolvedMemberTypes;
			annotatedType.memberTypes(); // recompute.
		}
	}
	sortedMemberTypes();
	return this.memberTypes;
}

/**
 * Propagates writes to all annotated variants so the clones evolve along.
 */
public MethodBinding[] setMethods(MethodBinding[] methods) {
	if (!isPrototype())
		return this.prototype.setMethods(methods);

	if ((this.tagBits & TagBits.HasAnnotatedVariants) != 0) {
		TypeBinding [] annotatedTypes = this.scope.environment().getAnnotatedTypes(this);
		for (int i = 0, length = annotatedTypes == null ? 0 : annotatedTypes.length; i < length; i++) {
			SourceTypeBinding annotatedType = (SourceTypeBinding) annotatedTypes[i];
			annotatedType.methods = methods;
		}
	}
	return this.methods = methods;
}

/**
 * Propagates writes to all annotated variants so the clones evolve along.
 */
public ReferenceBinding[] setPermittedTypes(ReferenceBinding [] permittedTypes) {
	if (!isPrototype())
		return this.prototype.setPermittedTypes(permittedTypes);

	if ((this.tagBits & TagBits.HasAnnotatedVariants) != 0) {
		TypeBinding [] annotatedTypes = this.scope.environment().getAnnotatedTypes(this);
		for (int i = 0, length = annotatedTypes == null ? 0 : annotatedTypes.length; i < length; i++) {
			SourceTypeBinding annotatedType = (SourceTypeBinding) annotatedTypes[i];
			annotatedType.permittedTypes = permittedTypes;
		}
	}
	return this.permittedTypes = permittedTypes;
}

private void setImplicitPermittedType(SourceTypeBinding permittedType) {
	ReferenceBinding[] typesPermitted = this.permittedTypes();
	int sz = typesPermitted == null ? 0 : typesPermitted.length;
	if (this.scope.referenceCompilationUnit() == permittedType.scope.referenceCompilationUnit()) {
		if (sz == 0) {
			typesPermitted = new ReferenceBinding[] { permittedType };
		} else {
			System.arraycopy(typesPermitted, 0, typesPermitted = new ReferenceBinding[sz + 1], 0, sz);
			typesPermitted[sz] = permittedType;
		}
		this.setPermittedTypes(typesPermitted);
	} else if (sz == 0) {
		this.setPermittedTypes(Binding.NO_PERMITTED_TYPES);
	}
}

/**
 * Propagates writes to all annotated variants so the clones evolve along.
 */
public ReferenceBinding setSuperClass(ReferenceBinding superClass) {
	if (!isPrototype())
		return this.prototype.setSuperClass(superClass);

	if ((this.tagBits & TagBits.HasAnnotatedVariants) != 0) {
		TypeBinding [] annotatedTypes = this.scope.environment().getAnnotatedTypes(this);
		for (int i = 0, length = annotatedTypes == null ? 0 : annotatedTypes.length; i < length; i++) {
			SourceTypeBinding annotatedType = (SourceTypeBinding) annotatedTypes[i];
			annotatedType.superclass = superClass;
		}
	}
	if (superClass != null && superClass.actualType() instanceof SourceTypeBinding sourceSuperType && sourceSuperType.isSealed() && sourceSuperType.scope.referenceContext.permittedTypes == null) {
		sourceSuperType.setImplicitPermittedType(this);
		if (this.isAnonymousType() && superClass.isEnum())
			this.modifiers |= ClassFileConstants.AccFinal;
	}
	return this.superclass = superClass;
}

/**
 * Propagates writes to all annotated variants so the clones evolve along.
 */
public ReferenceBinding[] setSuperInterfaces(ReferenceBinding [] superInterfaces) {
	if (!isPrototype())
		return this.prototype.setSuperInterfaces(superInterfaces);

	if ((this.tagBits & TagBits.HasAnnotatedVariants) != 0) {
		TypeBinding [] annotatedTypes = this.scope.environment().getAnnotatedTypes(this);
		for (int i = 0, length = annotatedTypes == null ? 0 : annotatedTypes.length; i < length; i++) {
			SourceTypeBinding annotatedType = (SourceTypeBinding) annotatedTypes[i];
			annotatedType.superInterfaces = superInterfaces;
		}
	}
	for (int i = 0, length = superInterfaces == null ? 0 : superInterfaces.length; i < length; i++) {
		ReferenceBinding superInterface = superInterfaces[i];
		if (superInterface.actualType() instanceof SourceTypeBinding sourceSuperType && sourceSuperType.isSealed() && sourceSuperType.scope.referenceContext.permittedTypes == null) {
			sourceSuperType.setImplicitPermittedType(this);
		}
	}
	return this.superInterfaces = superInterfaces;
}

/**
 * Propagates writes to all annotated variants so the clones evolve along.
 */
public TypeVariableBinding[] setTypeVariables(TypeVariableBinding [] typeVariables) {
	if (!isPrototype())
		return this.prototype.setTypeVariables(typeVariables);

	if ((this.tagBits & TagBits.HasAnnotatedVariants) != 0) {
		TypeBinding [] annotatedTypes = this.scope.environment().getAnnotatedTypes(this);
		for (int i = 0, length = annotatedTypes == null ? 0 : annotatedTypes.length; i < length; i++) {
			SourceTypeBinding annotatedType = (SourceTypeBinding) annotatedTypes[i];
			annotatedType.typeVariables = typeVariables;
		}
	}
	return this.typeVariables = typeVariables;
}

public final int sourceEnd() {
	if (!isPrototype())
		return this.prototype.sourceEnd();

	return this.scope.referenceContext.sourceEnd;
}

public final int sourceStart() {
	if (!isPrototype())
		return this.prototype.sourceStart();

	return this.scope.referenceContext.sourceStart;
}

@Override
Map<Binding, AnnotationHolder> storedAnnotations(boolean forceInitialize, boolean forceStore) {
	if (!isPrototype())
		return this.prototype.storedAnnotations(forceInitialize, forceStore);

	if (forceInitialize && this.storedAnnotations == null && this.scope != null) { // scope null when no annotation cached, and type got processed fully (159631)
		this.scope.referenceCompilationUnit().compilationResult.hasAnnotations = true;
		final CompilerOptions globalOptions = this.scope.environment().globalOptions;
		if (!globalOptions.storeAnnotations && !forceStore)
			return null; // not supported during this compile
		this.storedAnnotations = new HashMap<>();
	}
	return this.storedAnnotations;
}

@Override
void storeAnnotations(Binding binding, AnnotationBinding[] annotations, boolean forceStore) {
	super.storeAnnotations(binding, annotations, forceStore);
	this.scope.referenceCompilationUnit().compilationResult.annotations.add(annotations);
}

@Override
public ReferenceBinding superclass() {
	if (!isPrototype())
		return this.superclass = this.prototype.superclass();
	return this.superclass;
}

@Override
public ReferenceBinding[] superInterfaces() {
	if (!isPrototype())
		return this.superInterfaces = this.prototype.superInterfaces();
	return this.superInterfaces != null ? this.superInterfaces : isAnnotationType() ? this.superInterfaces = new ReferenceBinding [] { this.scope.getJavaLangAnnotationAnnotation() } : null;
}

public SyntheticMethodBinding[] syntheticMethods() {
	if (!isPrototype()) throw new IllegalStateException();

	if (this.synthetics == null
			|| this.synthetics[SourceTypeBinding.METHOD_EMUL] == null
			|| this.synthetics[SourceTypeBinding.METHOD_EMUL].size() == 0) {
		return null;
	}
	// difficult to compute size up front because of the embedded arrays so assume there is only 1
	int index = 0;
	SyntheticMethodBinding[] bindings = new SyntheticMethodBinding[1];
	Iterator methodArrayIterator = this.synthetics[SourceTypeBinding.METHOD_EMUL].values().iterator();
	while (methodArrayIterator.hasNext()) {
		SyntheticMethodBinding[] methodAccessors = (SyntheticMethodBinding[]) methodArrayIterator.next();
		for (SyntheticMethodBinding methodAccessor : methodAccessors) {
			if (methodAccessor != null) {
				if (index+1 > bindings.length) {
					System.arraycopy(bindings, 0, (bindings = new SyntheticMethodBinding[index + 1]), 0, index);
				}
				bindings[index++] = methodAccessor;
			}
		}
	}
	// sort them in according to their own indexes
	Arrays.sort(bindings, new Comparator<>() {
		@Override
		public int compare(SyntheticMethodBinding o1, SyntheticMethodBinding o2) {
			return o1.index - o2.index;
		}
	});


	return bindings;
}
/**
 * Answers the collection of synthetic fields to append into the classfile.
 */
public FieldBinding[] syntheticFields() {
	if (!isPrototype()) throw new IllegalStateException();

	if (this.synthetics == null) return null;
	int fieldSize = this.synthetics[SourceTypeBinding.FIELD_EMUL] == null ? 0 : this.synthetics[SourceTypeBinding.FIELD_EMUL].size();

	if (fieldSize == 0) return null;
	FieldBinding[] bindings = new FieldBinding[fieldSize];

	// add innerclass synthetics

	Iterator elements = this.synthetics[SourceTypeBinding.FIELD_EMUL].values().iterator();
	for (int i = 0; i < fieldSize; i++) {
		SyntheticFieldBinding synthBinding = (SyntheticFieldBinding) elements.next();
		bindings[i] = synthBinding;
	}

	return bindings;
}

@Override
public String toString() {
	if (this.hasTypeAnnotations()) {
		return annotatedDebugName();
	}

	StringBuilder buffer = new StringBuilder(30);
	buffer.append("(id="); //$NON-NLS-1$
	if (this.id == TypeIds.NoId)
		buffer.append("NoId"); //$NON-NLS-1$
	else
		buffer.append(this.id);
	buffer.append(")\n"); //$NON-NLS-1$
	if (isDeprecated()) buffer.append("deprecated "); //$NON-NLS-1$
	if (isPublic()) buffer.append("public "); //$NON-NLS-1$
	if (isProtected()) buffer.append("protected "); //$NON-NLS-1$
	if (isPrivate()) buffer.append("private "); //$NON-NLS-1$
	if (isAbstract() && isClass()) buffer.append("abstract "); //$NON-NLS-1$
	if (isStatic() && isNestedType()) buffer.append("static "); //$NON-NLS-1$
	if (isFinal()) buffer.append("final "); //$NON-NLS-1$

	if (isRecord()) buffer.append("record "); //$NON-NLS-1$
	else if (isEnum()) buffer.append("enum "); //$NON-NLS-1$
	else if (isAnnotationType()) buffer.append("@interface "); //$NON-NLS-1$
	else if (isClass()) buffer.append("class "); //$NON-NLS-1$
	else buffer.append("interface "); //$NON-NLS-1$
	buffer.append((this.compoundName != null) ? CharOperation.toString(this.compoundName) : "UNNAMED TYPE"); //$NON-NLS-1$

	if (this.typeVariables == null) {
		buffer.append("<NULL TYPE VARIABLES>"); //$NON-NLS-1$
	} else if (this.typeVariables != Binding.NO_TYPE_VARIABLES) {
		buffer.append("<"); //$NON-NLS-1$
		for (int i = 0, length = this.typeVariables.length; i < length; i++) {
			if (i  > 0) buffer.append(", "); //$NON-NLS-1$
			if (this.typeVariables[i] == null) {
				buffer.append("NULL TYPE VARIABLE"); //$NON-NLS-1$
				continue;
			}
			char[] varChars = this.typeVariables[i].toString().toCharArray();
			buffer.append(varChars, 1, varChars.length - 2);
		}
		buffer.append(">"); //$NON-NLS-1$
	}
	buffer.append("\n\textends "); //$NON-NLS-1$
	buffer.append((this.superclass != null) ? this.superclass.debugName() : "NULL TYPE"); //$NON-NLS-1$

	if (this.superInterfaces != null) {
		if (this.superInterfaces != Binding.NO_SUPERINTERFACES) {
			buffer.append("\n\timplements : "); //$NON-NLS-1$
			for (int i = 0, length = this.superInterfaces.length; i < length; i++) {
				if (i  > 0)
					buffer.append(", "); //$NON-NLS-1$
				buffer.append((this.superInterfaces[i] != null) ? this.superInterfaces[i].debugName() : "NULL TYPE"); //$NON-NLS-1$
			}
		}
	} else {
		buffer.append("NULL SUPERINTERFACES"); //$NON-NLS-1$
	}

	if (enclosingType() != null) {
		buffer.append("\n\tenclosing type : "); //$NON-NLS-1$
		buffer.append(enclosingType().debugName());
	}

	if (this.fields != null) {
		if (this.fields != Binding.NO_FIELDS) {
			buffer.append("\n/*   fields   */"); //$NON-NLS-1$
			for (FieldBinding field : this.fields)
				buffer.append('\n').append((field != null) ? field.toString() : "NULL FIELD"); //$NON-NLS-1$
		}
	} else {
		buffer.append("NULL FIELDS"); //$NON-NLS-1$
	}

	if (this.methods != null) {
		if (this.methods != Binding.NO_METHODS) {
			buffer.append("\n/*   methods   */"); //$NON-NLS-1$
			for (MethodBinding method : this.methods)
				buffer.append('\n').append((method != null) ? method.toString() : "NULL METHOD"); //$NON-NLS-1$
		}
	} else {
		buffer.append("NULL METHODS"); //$NON-NLS-1$
	}

	if (this.memberTypes != null) {
		if (this.memberTypes != Binding.NO_MEMBER_TYPES) {
			buffer.append("\n/*   members   */"); //$NON-NLS-1$
			for (ReferenceBinding memberType : this.memberTypes)
				buffer.append('\n').append((memberType != null) ? memberType.toString() : "NULL TYPE"); //$NON-NLS-1$
		}
	} else {
		buffer.append("NULL MEMBER TYPES"); //$NON-NLS-1$
	}

	buffer.append("\n\n"); //$NON-NLS-1$
	return buffer.toString();
}

@Override
public TypeVariableBinding[] typeVariables() {
	if (!isPrototype())
		return this.typeVariables = this.prototype.typeVariables();
	return this.typeVariables != null ? this.typeVariables : Binding.NO_TYPE_VARIABLES;
}

void verifyMethods(MethodVerifier verifier) {
	if (!isPrototype()) throw new IllegalStateException();

	verifier.verify(this);

	for (int i = this.memberTypes.length; --i >= 0;)
		 ((SourceTypeBinding) this.memberTypes[i]).verifyMethods(verifier);
}

@Override
public TypeBinding unannotated() {
	return this.prototype;
}

@Override
public TypeBinding withoutToplevelNullAnnotation() {
	if (!hasNullTypeAnnotations())
		return this;
	AnnotationBinding[] newAnnotations = this.environment.filterNullTypeAnnotations(this.typeAnnotations);
	if (newAnnotations.length > 0)
		return this.environment.createAnnotatedType(this.prototype, newAnnotations);
	return this.prototype;
}

@Override
public FieldBinding[] unResolvedFields() {
	if (!isPrototype())
		return this.prototype.unResolvedFields();
	// GROOVY add
	if (!areFieldsInitialized())
		this.scope.buildFields();
	// GROOVY end
	return this.fields;
}

public void tagIndirectlyAccessibleMembers() {
	if (!isPrototype()) {
		this.prototype.tagIndirectlyAccessibleMembers();
		return;
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328281
	for (int i = 0; i < this.fields.length; i++) {
		if (!this.fields[i].isPrivate())
			this.fields[i].modifiers |= ExtraCompilerModifiers.AccLocallyUsed;
	}
	for (int i = 0; i < this.memberTypes.length; i++) {
		if (!this.memberTypes[i].isPrivate())
			this.memberTypes[i].modifiers |= ExtraCompilerModifiers.AccLocallyUsed;
	}
	if (this.superclass.isPrivate())
		if (this.superclass instanceof SourceTypeBinding theSuperClass) // should always be true because private super type can only be accessed in same CU
			theSuperClass.tagIndirectlyAccessibleMembers();
}

@Override
public ModuleBinding module() {
	if (!isPrototype())
		return this.prototype.module;
	return this.module;
}

public SourceTypeBinding getNestHost() {
	return this.nestHost;
}

public Set<SourceTypeBinding> getNestMembers() {
	return this.nestMembers;
}

public void addNestMember(SourceTypeBinding member) {
	if (!member.equals(this)) {
		if (this.nestMembers == null)
			this.nestMembers = new HashSet<>(6);
		this.nestMembers.add(member);
	}
}

public void setNestHost(SourceTypeBinding nestHost) {
	this.nestHost = nestHost;
}

public boolean isNestmateOf(SourceTypeBinding other) {
	CompilerOptions options = this.scope.compilerOptions();
	if (options.targetJDK < ClassFileConstants.JDK11 ||
		options.complianceLevel < ClassFileConstants.JDK11)
		return false; // default false if level less than 11

	SourceTypeBinding otherHost = other.getNestHost();
	return TypeBinding.equalsEquals(this, other) ||
			TypeBinding.equalsEquals(this.nestHost == null ? this : this.nestHost,
					otherHost == null ? other : otherHost);
}

@Override
public MethodBinding getRecordComponentAccessor(char[] name) {
	if (this.isRecord()) {
		for (MethodBinding m : this.methods()) {
			if (CharOperation.equals(m.selector, name)) {
				if (m.parameters == null || m.parameters.length == 0)
					return m;
			}
		}
	}
	return null;
}

public void cleanUp() {
	if (this.environment != null) {
		// delegate so as to clean all variants of this prototype:
		this.environment.typeSystem.cleanUp(this.id);
	}
	this.scope = null; // for types that are not registered in typeSystem.
}

}