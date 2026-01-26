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
 *     Stephan Herrmann - Contributions for
 *	 							bug 186342 - [compiler][null] Using annotations for null checking
 *								bug 387612 - Unreachable catch block...exception is never thrown from the try
 *								bug 395002 - Self bound generic class doesn't resolve bounds properly for wildcards for certain parametrisation.
 *								bug 401456 - Code compiles from javac/intellij, but fails from eclipse
 *								bug 401271 - StackOverflowError when searching for a methods references
 *								bug 405706 - Eclipse compiler fails to give compiler error when return type is a inferred generic
 *								Bug 408441 - Type mismatch using Arrays.asList with 3 or more implementations of an interface with the interface type as the last parameter
 *								Bug 413958 - Function override returning inherited Generic Type
 *								Bug 392238 - [1.8][compiler][null] Detect semantically invalid null type annotations
 *								Bug 416183 - [1.8][compiler][null] Overload resolution fails with null annotations
 *								Bug 416176 - [1.8][compiler][null] null type annotations cause grief on type variables
 *								Bug 400874 - [1.8][compiler] Inference infrastructure should evolve to meet JLS8 18.x (Part G of JSR335 spec)
 *								Bug 424710 - [1.8][compiler] CCE in SingleNameReference.localVariableBinding
 *								Bug 424205 - [1.8] Cannot infer type for diamond type with lambda on method invocation
 *								Bug 424415 - [1.8][compiler] Eventual resolution of ReferenceExpression is not seen to be happening.
 *								Bug 426366 - [1.8][compiler] Type inference doesn't handle multiple candidate target types in outer overload context
 *								Bug 426290 - [1.8][compiler] Inference + overloading => wrong method resolution ?
 *								Bug 426589 - [1.8][compiler] Compiler error with generic method/constructor invocation as vargs argument
 *								Bug 426590 - [1.8][compiler] Compiler error with tenary operator
 *								Bug 426764 - [1.8] Presence of conditional expression as method argument confuses compiler
 *								Bug 426998 - [1.8][compiler] method(java.lang.Class, java.lang.String) not applicable for the arguments (java.lang.Class, java.lang.String)
 *								Bug 423505 - [1.8] Implement "18.5.4 More Specific Method Inference"
 *								Bug 427196 - [1.8][compiler] Compiler error for method reference to overloaded method
 *								Bug 427483 - [Java 8] Variables in lambdas sometimes can't be resolved
 *								Bug 427728 - [1.8] Type Inference rejects calls requiring boxing/unboxing
 *								Bug 427218 - [1.8][compiler] Verify error varargs + inference
 *								Bug 426836 - [1.8] special handling for return type in references to method getClass()?
 *								Bug 427628 - [1.8] regression : The method * is ambiguous for the type *
 *								Bug 428352 - [1.8][compiler] Resolution errors don't always surface
 *								Bug 428366 - [1.8] [compiler] The method valueAt(ObservableList<Object>, int) is ambiguous for the type Bindings
 *								Bug 424728 - [1.8][null] Unexpected error: The nullness annotation 'XXXX' is not applicable at this location
 *								Bug 428811 - [1.8][compiler] Type witness unnecessarily required
 *								Bug 429424 - [1.8][inference] Problem inferring type of method's parameter
 *								Bug 429958 - [1.8][null] evaluate new DefaultLocation attribute of @NonNullByDefault
 *								Bug 434570 - Generic type mismatch for parametrized class annotation attribute with inner class
 *								Bug 434483 - [1.8][compiler][inference] Type inference not picked up with method reference
 *								Bug 441734 - [1.8][inference] Generic method with nested parameterized type argument fails on method reference
 *								Bug 452194 - Code no longer compiles in 4.4.1, but with confusing error
 *								Bug 452788 - [1.8][compiler] Type not correctly inferred in lambda expression
 *								Bug 456236 - [1.8][null] Cannot infer type when constructor argument is annotated with @Nullable
 *								Bug 437072 - [compiler][null] Null analysis emits possibly incorrect warning for new int[][] despite @NonNullByDefault
 *								Bug 462083 - [1.8][inference] Java 8 generic return type mismatch with interface involving type parameter.
 *     Jesper S Moller - Contributions for
 *								Bug 378674 - "The method can be declared as static" is wrong
 *  							Bug 405066 - [1.8][compiler][codegen] Implement code generation infrastructure for JSR335
 *     Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *                          	Bug 405104 - [1.8][compiler][codegen] Implement support for serializeable lambdas
 *     Pierre-Yves B. <pyvesdev@gmail.com> - Contributions for
 *                              Bug 559618 - No compiler warning for import from same package
 *                              Bug 560630 - No warning on unused import on class from same package
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.JavaFeature;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;

public abstract class Scope {

	public static Binding NOT_REDUNDANT = new Binding() {
		@Override
		public int kind() {
			throw new IllegalStateException();
		}

		@Override
		public char[] readableName() {
			throw new IllegalStateException();
		}
	};

	/* Scope kinds */
	public final static int BLOCK_SCOPE = 1;
	public final static int CLASS_SCOPE = 3;
	public final static int COMPILATION_UNIT_SCOPE = 4;
	public final static int METHOD_SCOPE = 2;
	public final static int MODULE_SCOPE = 5;

	/* Argument Compatibilities */
	public final static int NOT_COMPATIBLE = -1;
	public final static int COMPATIBLE = 0;
	public final static int AUTOBOX_COMPATIBLE = 1;
	public final static int VARARGS_COMPATIBLE = 2;
	public final static int COMPATIBLE_IGNORING_MISSING_TYPE = -2;

	/* Type Compatibilities */
	public static final int EQUAL_OR_MORE_SPECIFIC = -1;
	public static final int NOT_RELATED = 0;
	public static final int MORE_GENERIC = 1;

	public int kind;
	public final Scope parent;
	public final CompilationUnitScope compilationUnitScope;
	private Map<String, Supplier<ReferenceBinding>> commonTypeBindings = null;


	private static class NullDefaultRange {
		final int start, end;
		int value;
		private Annotation[] annotations;
		Binding target;

		NullDefaultRange(int value, Annotation annotation, int start, int end, Binding target) {
			this.start = start;
			this.end = end;
			this.value = value;
			this.annotations = new Annotation[] { annotation };
			this.target = target;
		}

		boolean contains(Annotation annotation) {
			for (Annotation a : this.annotations) {
				if (a == annotation)
					return true;
			}
			return false;
		}

		void merge(int nextValue, Annotation nextAnnotation, Binding nextTarget) {
			int len = this.annotations.length;
			System.arraycopy(this.annotations, 0, this.annotations = new Annotation[len + 1], 0, len);
			this.annotations[len] = nextAnnotation;
			this.target = nextTarget;
			this.value |= nextValue;
		}
	}

	private /* @Nullable */ ArrayList<NullDefaultRange> nullDefaultRanges;

	protected Scope(int kind, Scope parent) {
		this.kind = kind;
		this.parent = parent;
		this.commonTypeBindings = null;
		this.compilationUnitScope = (CompilationUnitScope) (parent == null ? this : parent.compilationUnitScope());
	}

	/* Answer an int describing the relationship between the given types.
	*
	* 		NOT_RELATED
	* 		EQUAL_OR_MORE_SPECIFIC : left is compatible with right
	* 		MORE_GENERIC : right is compatible with left
	*/
	public static int compareTypes(TypeBinding left, TypeBinding right) {
		if (left.isCompatibleWith(right))
			return Scope.EQUAL_OR_MORE_SPECIFIC;
		if (right.isCompatibleWith(left))
			return Scope.MORE_GENERIC;
		return Scope.NOT_RELATED;
	}

	/**
	 * Returns a type where either all variables or specific ones got discarded.
	 * e.g. {@code List<E> (discarding <E extends Enum<E>)} will return: {@code List<? extends Enum<?>>}
	 */
	public static TypeBinding convertEliminatingTypeVariables(TypeBinding originalType, ReferenceBinding genericType, int rank, Set<TypeBinding> eliminatedVariables) {
		if ((originalType.tagBits & TagBits.HasTypeVariable) != 0) {
			switch (originalType.kind()) {
				case Binding.ARRAY_TYPE :
					ArrayBinding originalArrayType = (ArrayBinding) originalType;
					TypeBinding originalLeafComponentType = originalArrayType.leafComponentType;
					TypeBinding substitute = convertEliminatingTypeVariables(originalLeafComponentType, genericType, rank, eliminatedVariables); // substitute could itself be array type
					if (TypeBinding.notEquals(substitute, originalLeafComponentType)) {
						return originalArrayType.environment.createArrayType(substitute.leafComponentType(), substitute.dimensions() + originalArrayType.dimensions());
					}
					break;
				case Binding.PARAMETERIZED_TYPE :
					ParameterizedTypeBinding paramType = (ParameterizedTypeBinding) originalType;
					ReferenceBinding originalEnclosing = paramType.enclosingType();
					ReferenceBinding substitutedEnclosing = originalEnclosing;
					if (originalEnclosing != null) {
						substitutedEnclosing = (ReferenceBinding) convertEliminatingTypeVariables(originalEnclosing, genericType, rank, eliminatedVariables);
					}
					TypeBinding[] originalArguments = paramType.arguments;
					TypeBinding[] substitutedArguments = originalArguments;
					for (int i = 0, length = originalArguments == null ? 0 : originalArguments.length; i < length; i++) {
						TypeBinding originalArgument = originalArguments[i];
						TypeBinding substitutedArgument = convertEliminatingTypeVariables(originalArgument, paramType.genericType(), i, eliminatedVariables);
						if (TypeBinding.notEquals(substitutedArgument, originalArgument)) {
							if (substitutedArguments == originalArguments) {
								System.arraycopy(originalArguments, 0, substitutedArguments = new TypeBinding[length], 0, i);
							}
							substitutedArguments[i] = substitutedArgument;
						} else 	if (substitutedArguments != originalArguments) {
							substitutedArguments[i] = originalArgument;
						}
					}
					if (TypeBinding.notEquals(originalEnclosing, substitutedEnclosing) || originalArguments != substitutedArguments) {
						return paramType.environment.createParameterizedType(paramType.genericType(), substitutedArguments, substitutedEnclosing);
					}
					break;
				case Binding.TYPE_PARAMETER :
					if (genericType == null) {
						break;
					}
					TypeVariableBinding originalVariable = (TypeVariableBinding) originalType;
					if (eliminatedVariables != null && eliminatedVariables.contains(originalType)) {
						return originalVariable.environment.createWildcard(genericType, rank, null, null, Wildcard.UNBOUND);
					}
					TypeBinding originalUpperBound = originalVariable.upperBound();
					if (eliminatedVariables == null) {
						eliminatedVariables = new HashSet<>(2);
					}
					eliminatedVariables.add(originalVariable);
					TypeBinding substitutedUpperBound = convertEliminatingTypeVariables(originalUpperBound, genericType, rank, eliminatedVariables);
					eliminatedVariables.remove(originalVariable);
					return originalVariable.environment.createWildcard(genericType, rank, substitutedUpperBound, null, Wildcard.EXTENDS);
				case Binding.RAW_TYPE :
					break;
				case Binding.GENERIC_TYPE :
					ReferenceBinding currentType = (ReferenceBinding) originalType;
					originalEnclosing = currentType.enclosingType();
					substitutedEnclosing = originalEnclosing;
					if (originalEnclosing != null) {
						substitutedEnclosing = (ReferenceBinding) convertEliminatingTypeVariables(originalEnclosing, genericType, rank, eliminatedVariables);
					}
					originalArguments = currentType.typeVariables();
					substitutedArguments = originalArguments;
					for (int i = 0, length = originalArguments == null ? 0 : originalArguments.length; i < length; i++) {
						TypeBinding originalArgument = originalArguments[i];
						TypeBinding substitutedArgument = convertEliminatingTypeVariables(originalArgument, currentType, i, eliminatedVariables);
						if (TypeBinding.notEquals(substitutedArgument, originalArgument)) {
							if (substitutedArguments == originalArguments) {
								System.arraycopy(originalArguments, 0, substitutedArguments = new TypeBinding[length], 0, i);
							}
							substitutedArguments[i] = substitutedArgument;
						} else 	if (substitutedArguments != originalArguments) {
							substitutedArguments[i] = originalArgument;
						}
					}
					if (TypeBinding.notEquals(originalEnclosing, substitutedEnclosing) || originalArguments != substitutedArguments) {
						return ((TypeVariableBinding)originalArguments[0]).environment.createParameterizedType(genericType, substitutedArguments, substitutedEnclosing);
					}
					break;
				case Binding.WILDCARD_TYPE :
					WildcardBinding wildcard = (WildcardBinding) originalType;
					TypeBinding originalBound = wildcard.bound;
					TypeBinding substitutedBound = originalBound;
					if (originalBound != null) {
						substitutedBound = convertEliminatingTypeVariables(originalBound, genericType, rank, eliminatedVariables);
						if (TypeBinding.notEquals(substitutedBound, originalBound)) {
							return wildcard.environment.createWildcard(wildcard.genericType, wildcard.rank, substitutedBound, null, wildcard.boundKind);
						}
					}
					break;
				case Binding.INTERSECTION_TYPE :
					WildcardBinding intersection = (WildcardBinding) originalType;
					originalBound = intersection.bound;
					substitutedBound = originalBound;
					if (originalBound != null) {
						substitutedBound = convertEliminatingTypeVariables(originalBound, genericType, rank, eliminatedVariables);
					}
					TypeBinding[] originalOtherBounds = intersection.otherBounds;
					TypeBinding[] substitutedOtherBounds = originalOtherBounds;
					for (int i = 0, length = originalOtherBounds == null ? 0 : originalOtherBounds.length; i < length; i++) {
						TypeBinding originalOtherBound = originalOtherBounds[i];
						TypeBinding substitutedOtherBound = convertEliminatingTypeVariables(originalOtherBound, genericType, rank, eliminatedVariables);
						if (TypeBinding.notEquals(substitutedOtherBound, originalOtherBound)) {
							if (substitutedOtherBounds == originalOtherBounds) {
								System.arraycopy(originalOtherBounds, 0, substitutedOtherBounds = new TypeBinding[length], 0, i);
							}
							substitutedOtherBounds[i] = substitutedOtherBound;
						} else 	if (substitutedOtherBounds != originalOtherBounds) {
							substitutedOtherBounds[i] = originalOtherBound;
						}
					}
					if (TypeBinding.notEquals(substitutedBound, originalBound) || substitutedOtherBounds != originalOtherBounds) {
						return intersection.environment.createWildcard(intersection.genericType, intersection.rank, substitutedBound, substitutedOtherBounds, intersection.boundKind);
					}
					break;
				}
		}
		return originalType;
	}

   public static TypeBinding getBaseType(char[] name) {
		// list should be optimized (with most often used first)
		int length = name.length;
		if (length > 2 && length < 8) {
			switch (name[0]) {
				case 'i' :
					if (length == 3 && name[1] == 'n' && name[2] == 't')
						return TypeBinding.INT;
					break;
				case 'v' :
					if (length == 4 && name[1] == 'o' && name[2] == 'i' && name[3] == 'd')
						return TypeBinding.VOID;
					break;
				case 'b' :
					if (length == 7
						&& name[1] == 'o'
						&& name[2] == 'o'
						&& name[3] == 'l'
						&& name[4] == 'e'
						&& name[5] == 'a'
						&& name[6] == 'n')
						return TypeBinding.BOOLEAN;
					if (length == 4 && name[1] == 'y' && name[2] == 't' && name[3] == 'e')
						return TypeBinding.BYTE;
					break;
				case 'c' :
					if (length == 4 && name[1] == 'h' && name[2] == 'a' && name[3] == 'r')
						return TypeBinding.CHAR;
					break;
				case 'd' :
					if (length == 6
						&& name[1] == 'o'
						&& name[2] == 'u'
						&& name[3] == 'b'
						&& name[4] == 'l'
						&& name[5] == 'e')
						return TypeBinding.DOUBLE;
					break;
				case 'f' :
					if (length == 5
						&& name[1] == 'l'
						&& name[2] == 'o'
						&& name[3] == 'a'
						&& name[4] == 't')
						return TypeBinding.FLOAT;
					break;
				case 'l' :
					if (length == 4 && name[1] == 'o' && name[2] == 'n' && name[3] == 'g')
						return TypeBinding.LONG;
					break;
				case 's' :
					if (length == 5
						&& name[1] == 'h'
						&& name[2] == 'o'
						&& name[3] == 'r'
						&& name[4] == 't')
						return TypeBinding.SHORT;
			}
		}
		return null;
	}

	// 5.1.10
	public static ReferenceBinding[] greaterLowerBound(ReferenceBinding[] types) {
		if (types == null) return null;
		types = filterValidTypes(types, ReferenceBinding[]::new);
		int length = types.length;
		if (length == 0) return null;
		ReferenceBinding[] result = types;
		int removed = 0;
		for (int i = 0; i < length; i++) {
			ReferenceBinding iType = result[i];
			if (iType == null) continue;
			for (int j = 0; j < length; j++) {
				if (i == j) continue;
				ReferenceBinding jType = result[j];
				if (jType == null) continue;
				if (isMalformedPair(iType, jType, null)) {
					return null;
				}
				if (iType.isCompatibleWith(jType)) { // if Vi <: Vj, Vj is removed
					if (result == types) { // defensive copy
						System.arraycopy(result, 0, result = new ReferenceBinding[length], 0, length);
					}
					result[j] = null;
					removed ++;
				}
			}
		}
		if (removed == 0) return result;
		if (length == removed) return null;
		ReferenceBinding[] trimmedResult = new ReferenceBinding[length - removed];
		for (int i = 0, index = 0; i < length; i++) {
			ReferenceBinding iType = result[i];
			if (iType != null) {
				trimmedResult[index++] = iType;
			}
		}
		return trimmedResult;
	}

	// 5.1.10
	public static TypeBinding[] greaterLowerBound(TypeBinding[] types, /*@Nullable*/ Scope scope, LookupEnvironment environment) {
		if (types == null) return null;
		types = filterValidTypes(types, TypeBinding[]::new);
		int length = types.length;
		if (length == 0) return null;
		TypeBinding[] result = types;
		int removed = 0;
		for (int i = 0; i < length; i++) {
			TypeBinding iType = result[i];
			if (iType == null) continue;
			for (int j = 0; j < length; j++) {
				if (i == j) continue;
				TypeBinding jType = result[j];
				if (jType == null) continue;
				if (isMalformedPair(iType, jType, scope)) {
					return null;
				}
				if (iType.isSubtypeOf(jType, false)) { // if Vi <: Vj, Vj is removed
					if (result == types) { // defensive copy
						System.arraycopy(result, 0, result = new TypeBinding[length], 0, length);
					}
					result[j] = null;
					removed ++;
				}
			}
		}
		if (removed == 0) return result;
		if (length == removed) return null; // how is this possible ???
		TypeBinding[] trimmedResult = new TypeBinding[length - removed];
		for (int i = 0, index = 0; i < length; i++) {
			TypeBinding iType = result[i];
			if (iType != null) {
				trimmedResult[index++] = iType;
			}
		}
		return trimmedResult;
	}

	static <T extends TypeBinding> T[] filterValidTypes(T[] allTypes, Function<Integer,T[]> ctor) {
		T[] valid = ctor.apply(allTypes.length);
		int count = 0;
		for (T type : allTypes) {
			if (type.isValidBinding())
				valid[count++] = type;
		}
		if (count == allTypes.length)
			return allTypes;
		if (count == 0 && allTypes.length > 0)
			return Arrays.copyOf(allTypes, 1); // if all are invalid pick the first as a placeholder to prevent general glb failure
		return Arrays.copyOf(valid, count);
	}

	static boolean isMalformedPair(TypeBinding t1, TypeBinding t2, Scope scope) {
		// this is a combination from JLS 4.9 and private email communication (2017-09-13):
		InferenceVariable iv1 = t1 instanceof InferenceVariable iv ? iv : null;
		InferenceVariable iv2 = t2 instanceof InferenceVariable iv ? iv : null;
		TypeVariableBinding tv1 = getTypeVariable(iv1 != null ? iv1.typeParameter : t1);
		TypeVariableBinding tv2 = getTypeVariable(iv2 != null ? iv2.typeParameter : t2);
		if (tv1 != null) {
			if (tv2 != null && iv1 == null && iv2 == null) { // two real type variables
				// OK only if one is a true subtype of the other:
				return !tv1.isSubtypeOf(tv2, false) && !tv2.isSubtypeOf(tv1, false);
			}
			return isTypeVariableClassConflict(tv1, iv1 != null, t2);
		} else if (tv2 != null) {
			return isTypeVariableClassConflict(tv2, iv2 != null, t1);
		}
		return false;
	}

	private static TypeVariableBinding getTypeVariable(TypeBinding type) {
		if (type.getClass() == TypeVariableBinding.class) // exact type only
			return ((TypeVariableBinding) type);
		return null;
	}

	private static boolean isTypeVariableClassConflict(TypeVariableBinding typeVariable, boolean fromInferenceVariable, TypeBinding otherType) {
		if (otherType.isClass()) {
			TypeBinding classErasure = otherType.erasure();
			if (classErasure.id != TypeIds.T_JavaLangObject) {
				TypeBinding bound1 = typeVariable.firstBound;
				if (bound1 == null) {
					return !fromInferenceVariable;
				} else if (!bound1.erasure().isCompatibleWith(classErasure))  // use of erasure is heuristic-based
					return true;
			}
		}
		return false;
	}

	/**
	 * Returns an array of types, where original types got substituted given a substitution.
	 * Only allocate an array if anything is different.
	 */
	public static ReferenceBinding[] substitute(Substitution substitution, ReferenceBinding[] originalTypes) {
		return defaultSubstitutor.substitute(substitution, originalTypes);
	}

	/**
	 * Returns a type, where original type was substituted using the receiver
	 * parameterized type.
	 * In raw mode (see {@link Substitution#isRawSubstitution()}),
	 * all parameterized types are converted to raw types.
	 * Cf. 4.8: "The type of a constructor (8.8), instance method (8.4, 9.4),
	 *  or non-static field (8.3) M of a raw type C that is not inherited from its
	 *  superclasses or superinterfaces is the raw type that corresponds to the erasure
	 *  of its type in the generic declaration corresponding to C."
	 */
	public static TypeBinding substitute(Substitution substitution, TypeBinding originalType) {
		return defaultSubstitutor.substitute(substitution, originalType);
	}

	/**
	 * Returns an array of types, where original types got substituted given a substitution.
	 * Only allocate an array if anything is different.
	 */
	public static TypeBinding[] substitute(Substitution substitution, TypeBinding[] originalTypes) {
		return defaultSubstitutor.substitute(substitution, originalTypes);
	}

	/** Bridge to non-static implementation in {@link Substitutor}, to make methods overridable. */
	private static Substitutor defaultSubstitutor = new Substitutor();
	public static class Substitutor {
		protected ReferenceBinding staticContext;
		/**
		 * Returns an array of types, where original types got substituted given a substitution.
		 * Only allocate an array if anything is different.
		 */
		public ReferenceBinding[] substitute(Substitution substitution, ReferenceBinding[] originalTypes) {
			if (originalTypes == null) return null;
		    ReferenceBinding[] substitutedTypes = originalTypes;
		    for (int i = 0, length = originalTypes.length; i < length; i++) {
		        ReferenceBinding originalType = originalTypes[i];
		        TypeBinding substitutedType = substitute(substitution, originalType);
		        if (!(substitutedType instanceof ReferenceBinding)) {
		        	return null; // impossible substitution
		        }
		        if (substitutedType != originalType) { //$IDENTITY-COMPARISON$
		            if (substitutedTypes == originalTypes) {
		                System.arraycopy(originalTypes, 0, substitutedTypes = new ReferenceBinding[length], 0, i);
		            }
		            substitutedTypes[i] = (ReferenceBinding)substitutedType;
		        } else if (substitutedTypes != originalTypes) {
		            substitutedTypes[i] = originalType;
		        }
		    }
		    return substitutedTypes;
		}

		/**
		 * Returns a type, where original type was substituted using the receiver
		 * parameterized type.
		 * In raw mode (see {@link Substitution#isRawSubstitution()}),
		 * all parameterized types are converted to raw types.
		 * Cf. 4.8: "The type of a constructor (8.8), instance method (8.4, 9.4),
		 *  or non-static field (8.3) M of a raw type C that is not inherited from its
		 *  superclasses or superinterfaces is the raw type that corresponds to the erasure
		 *  of its type in the generic declaration corresponding to C."
		 */
		public TypeBinding substitute(Substitution substitution, TypeBinding originalType) {
			if (originalType == null) return null;

			switch (originalType.kind()) {

				case Binding.TYPE_PARAMETER:
					return substitution.substitute((TypeVariableBinding) originalType);

				case Binding.PARAMETERIZED_TYPE:
					ParameterizedTypeBinding originalParameterizedType = (ParameterizedTypeBinding) originalType;
					ReferenceBinding originalEnclosing = originalType.enclosingType();
					ReferenceBinding substitutedEnclosing = originalEnclosing;
					if (originalEnclosing != null && originalParameterizedType.hasEnclosingInstanceContext()) {
						substitutedEnclosing = (ReferenceBinding) substitute(substitution, originalEnclosing);
						if (isMemberTypeOfRaw(originalType, substitutedEnclosing))
							return originalParameterizedType.environment.createRawType(
									originalParameterizedType.genericType(), substitutedEnclosing, originalType.getTypeAnnotations());
					}
					TypeBinding[] originalArguments = originalParameterizedType.arguments;
					TypeBinding[] substitutedArguments = originalArguments;
					if (originalArguments != null) {
						if (substitution.isRawSubstitution()) {
							return originalParameterizedType.environment.createRawType(originalParameterizedType.genericType(), substitutedEnclosing, originalType.getTypeAnnotations());
						}
						substitutedArguments = substitute(substitution, originalArguments);
					}
					if (substitutedArguments != originalArguments || substitutedEnclosing != originalEnclosing) { //$IDENTITY-COMPARISON$
						return originalParameterizedType.environment.createParameterizedType(
								originalParameterizedType.genericType(), substitutedArguments, substitutedEnclosing, originalType.getTypeAnnotations());
					}
					break;

				case Binding.ARRAY_TYPE:
					ArrayBinding originalArrayType = (ArrayBinding) originalType;
					TypeBinding originalLeafComponentType = originalArrayType.leafComponentType;
					TypeBinding substitute = substitute(substitution, originalLeafComponentType); // substitute could itself be array type, TODO(Srikanth): need a test case.
					if (substitute != originalLeafComponentType) { //$IDENTITY-COMPARISON$
						return originalArrayType.environment.createArrayType(substitute.leafComponentType(), substitute.dimensions() + originalType.dimensions(), originalType.getTypeAnnotations());
					}
					break;

				case Binding.WILDCARD_TYPE:
				case Binding.INTERSECTION_TYPE:
			        WildcardBinding wildcard = (WildcardBinding) originalType;
			        if (wildcard.boundKind != Wildcard.UNBOUND) {
				        TypeBinding originalBound = wildcard.bound;
				        TypeBinding substitutedBound = substitute(substitution, originalBound);
				        TypeBinding[] originalOtherBounds = wildcard.otherBounds;
				        TypeBinding[] substitutedOtherBounds = substitute(substitution, originalOtherBounds);
				        if (substitutedBound != originalBound || originalOtherBounds != substitutedOtherBounds) { //$IDENTITY-COMPARISON$
				        	if (originalOtherBounds != null) {
				        		/* https://bugs.eclipse.org/bugs/show_bug.cgi?id=347145: the constituent intersecting types have changed
				        		   in the last round of substitution. Reevaluate the composite intersection type, as there is a possibility
				        		   of the intersection collapsing into one of the constituents, the other being fully subsumed.
				        		*/
				    			TypeBinding [] bounds = new TypeBinding[1 + substitutedOtherBounds.length];
				    			bounds[0] = substitutedBound;
				    			System.arraycopy(substitutedOtherBounds, 0, bounds, 1, substitutedOtherBounds.length);
				    			TypeBinding[] glb = Scope.greaterLowerBound(bounds, null, substitution.environment()); // re-evaluate
				    			if (glb != null && glb != bounds) {
				    				substitutedBound = glb[0];
			    					if (glb.length == 1) {
				    					substitutedOtherBounds = null;
				    				} else {
				    					System.arraycopy(glb, 1, substitutedOtherBounds = new TypeBinding[glb.length - 1], 0, glb.length - 1);
				    				}
				    			}
				        	}
			        		return wildcard.environment.createWildcard(wildcard.genericType, wildcard.rank, substitutedBound, substitutedOtherBounds, wildcard.boundKind, wildcard.getTypeAnnotations());
				        }
			        }
					break;

				case Binding.INTERSECTION_TYPE18:
					IntersectionTypeBinding18 intersection = (IntersectionTypeBinding18) originalType;
					ReferenceBinding[] types = intersection.getIntersectingTypes();
					TypeBinding[] substitutes = substitute(substitution, types);
					ReferenceBinding[] refSubsts = new ReferenceBinding[substitutes.length];
					System.arraycopy(substitutes, 0, refSubsts, 0, substitutes.length);
					return substitution.environment().createIntersectionType18(refSubsts);

				case Binding.TYPE:
					if (!originalType.isMemberType()) break;
					ReferenceBinding originalReferenceType = (ReferenceBinding) originalType;
					originalEnclosing = originalType.enclosingType();
					substitutedEnclosing = originalEnclosing;
					if (originalEnclosing != null) {
						substitutedEnclosing = (ReferenceBinding) substitute(substitution, originalEnclosing);
						if (isMemberTypeOfRaw(originalType, substitutedEnclosing))
							return substitution.environment().createRawType(originalReferenceType, substitutedEnclosing, originalType.getTypeAnnotations());
					}

				    // treat as if parameterized with its type variables (non generic type gets 'null' arguments)
					if (substitutedEnclosing != originalEnclosing && originalReferenceType.hasEnclosingInstanceContext()) { //$IDENTITY-COMPARISON$
						return substitution.isRawSubstitution()
							? substitution.environment().createRawType(originalReferenceType, substitutedEnclosing, originalType.getTypeAnnotations())
							:  substitution.environment().createParameterizedType(originalReferenceType, null, substitutedEnclosing, originalType.getTypeAnnotations());
					}
					break;
				case Binding.GENERIC_TYPE:
					originalReferenceType = (ReferenceBinding) originalType.unannotated();
					originalEnclosing = originalType.enclosingType();
					substitutedEnclosing = originalEnclosing;
					if (originalEnclosing != null) {
						substitutedEnclosing = (ReferenceBinding) (originalType.isStatic() ? substitution.environment().convertToRawType(originalEnclosing, true) :
																							(ReferenceBinding) substitute(substitution, originalEnclosing));
						if (isMemberTypeOfRaw(originalType, substitutedEnclosing))
							return substitution.environment().createRawType(originalReferenceType, substitutedEnclosing, originalType.getTypeAnnotations());
					}

					if (substitution.isRawSubstitution()) {
						return substitution.environment().createRawType(originalReferenceType, substitutedEnclosing, originalType.getTypeAnnotations());
					}
				    // potentially treat as if parameterized with its type variables (non generic type gets 'null' arguments)
					if (TypeBinding.equalsEquals(this.staticContext, originalType))
						return originalType; // substitution happens on a static member of the generic type, where its type variables are not available
					originalArguments = originalReferenceType.typeVariables();
					substitutedArguments = substitute(substitution, originalArguments);
					return substitution.environment().createParameterizedType(originalReferenceType, substitutedArguments, substitutedEnclosing, originalType.getTypeAnnotations());
			}
			return originalType;
		}

		private static boolean isMemberTypeOfRaw(TypeBinding originalType, ReferenceBinding substitutedEnclosing) {
			// 4.8:
			// "a raw type is defined to be one of:
			// ...
			// * A non-static member type of a raw type R that is not
			//   inherited from a superclass or superinterface of R."

			// Due to staticness, e.g., Map.Entry<String,Object> is *not* considered as a raw type

			return (substitutedEnclosing != null && substitutedEnclosing.isRawType())
					&& ((originalType instanceof ReferenceBinding) && !((ReferenceBinding)originalType).isStatic());
		}

		/**
		 * Returns an array of types, where original types got substituted given a substitution.
		 * Only allocate an array if anything is different.
		 */
		public TypeBinding[] substitute(Substitution substitution, TypeBinding[] originalTypes) {
			if (originalTypes == null) return null;
			TypeBinding[] substitutedTypes = originalTypes;
			for (int i = 0, length = originalTypes.length; i < length; i++) {
				TypeBinding originalType = originalTypes[i];
				TypeBinding substitutedParameter = substitute(substitution, originalType);
				if (substitutedParameter != originalType) { //$IDENTITY-COMPARISON$
					if (substitutedTypes == originalTypes) {
						System.arraycopy(originalTypes, 0, substitutedTypes = new TypeBinding[length], 0, i);
					}
					substitutedTypes[i] = substitutedParameter;
				} else if (substitutedTypes != originalTypes) {
					substitutedTypes[i] = originalType;
				}
			}
			return substitutedTypes;
		}
	}

	/*
	 * Boxing primitive
	 */
	public TypeBinding boxing(TypeBinding type) {
		if (type.isBaseType() || type.kind() == Binding.POLY_TYPE)
			return environment().computeBoxingType(type);
		return type;
	}

	public final ClassScope classScope() {
		Scope scope = this;
		do {
			if (scope instanceof ClassScope)
				return (ClassScope) scope;
			scope = scope.parent;
		} while (scope != null);
		return null;
	}

	public final CompilationUnitScope compilationUnitScope() {
		return this.compilationUnitScope;
	}

	public ModuleBinding module() {
		return environment().module;
	}
	public boolean isLambdaScope() {
		return false;
	}

	public boolean isLambdaSubscope() {
		for (Scope scope = this; scope != null; scope = scope.parent) {
			switch (scope.kind) {
				case BLOCK_SCOPE:
			        continue;
				case METHOD_SCOPE:
					return scope.isLambdaScope();
				default:
					return false;
			}
		}
		return false;
	}

	/**
	 * Finds the most specific compiler options
	 */
	public final CompilerOptions compilerOptions() {

		return compilationUnitScope().environment.globalOptions;
	}

	/**
	 * Internal use only
	 * Given a method, returns null if arguments cannot be converted to parameters.
	 * Will answer a substituted method in case the method was generic and type inference got triggered;
	 * in case the method was originally compatible, then simply answer it back.
	 */
	protected final MethodBinding computeCompatibleMethod(MethodBinding method, TypeBinding[] arguments, InvocationSite invocationSite) {
		TypeBinding[] genericTypeArguments = invocationSite.genericTypeArguments();
		TypeBinding[] parameters = method.parameters;
		TypeVariableBinding[] typeVariables = method.typeVariables;
		if (parameters == arguments
			&& (method.returnType.tagBits & TagBits.HasTypeVariable) == 0
			&& genericTypeArguments == null
			&& typeVariables == Binding.NO_TYPE_VARIABLES)
				return method;

		int argLength = arguments.length;
		int paramLength = parameters.length;
		boolean isVarArgs = method.isVarargs();
		if (argLength != paramLength)
			if (!isVarArgs || argLength < paramLength - 1)
				return null; // incompatible
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=330435, inference should kick in only at source 1.5+
		if (typeVariables != Binding.NO_TYPE_VARIABLES) { // generic method
			TypeBinding[] newArgs = null;
			if (genericTypeArguments != null) { // for 1.8+ inferred calls, we do this inside PGMB.cCM18.
				for (int i = 0; i < argLength; i++) {
					TypeBinding param = i < paramLength ? parameters[i] : parameters[paramLength - 1];
					if (arguments[i].isBaseType() != param.isBaseType()) {
						if (newArgs == null) {
							newArgs = new TypeBinding[argLength];
							System.arraycopy(arguments, 0, newArgs, 0, argLength);
						}
						newArgs[i] = environment().computeBoxingType(arguments[i]);
					}
				}
			}
			if (newArgs != null)
				arguments = newArgs;
			method = ParameterizedGenericMethodBinding.computeCompatibleMethod(method, arguments, this, invocationSite);
			if (method == null) return null; // incompatible
			if (!method.isValidBinding()) return method; // bound check issue is taking precedence
			if (method instanceof ParameterizedGenericMethodBinding && invocationSite instanceof Invocation) {
				Invocation invocation = (Invocation) invocationSite;
				InferenceContext18 infCtx = invocation.getInferenceContext((ParameterizedGenericMethodBinding) method);
				if (infCtx != null)
					return method; // inference is responsible, no need to recheck.
			}
		} else if (typeVariables == Binding.NO_TYPE_VARIABLES && method instanceof ParameterizedGenericMethodBinding) {
			if (invocationSite instanceof Invocation) {
				Invocation invocation = (Invocation) invocationSite;
				InferenceContext18 infCtx = invocation.getInferenceContext((ParameterizedGenericMethodBinding) method);
				if (infCtx != null)
					return method; // inference is responsible, no need to recheck.
			}
		}

		int level = parameterCompatibilityLevel(method, arguments, invocationSite);
		if (level > NOT_COMPATIBLE) {
			if (method.hasPolymorphicSignature(this)) {
				// generate polymorphic method and set polymorphic tagbits as well
				method.tagBits |= TagBits.AnnotationPolymorphicSignature;
				return this.environment().createPolymorphicMethod(method, arguments, this);
			}
			return method;
		} else if (level == COMPATIBLE_IGNORING_MISSING_TYPE) {
			return new ProblemMethodBinding(method, method.selector, method.parameters, ProblemReasons.MissingTypeInSignature);
		}
		// if method is generic and type arguments have been supplied, only then answer a problem
		// of ParameterizedMethodTypeMismatch, else a non-generic method was invoked using type arguments
		// in which case this problem category will be bogus
		if (genericTypeArguments != null && typeVariables != Binding.NO_TYPE_VARIABLES)
			return new ProblemMethodBinding(method, method.selector, arguments, ProblemReasons.ParameterizedMethodTypeMismatch);
		// 18.5.1 ignores arguments not pertinent to applicability. When these are taken into consideration method could fail applicability (e.g, lambda shape/arity mismatch ...)
		if (method instanceof PolyParameterizedGenericMethodBinding) // Not reached, but left in for now.
			return new ProblemMethodBinding(method, method.selector, method.parameters, ProblemReasons.InferredApplicableMethodInapplicable);
		return null; // incompatible
	}

	/**
	 * Connect type variable supertypes, and returns true if no problem was detected
	 */
	protected boolean connectTypeVariables(TypeParameter[] typeParameters, boolean checkForErasedCandidateCollisions) {
		/* https://bugs.eclipse.org/bugs/show_bug.cgi?id=305259 - We used to not bother with connecting
		   type variables if source level is < 1.5. This creates problems in the reconciler if a 1.4
		   project references the generified API of a 1.5 project. The "current" project's source
		   level cannot decide this question for some other project. Now, if we see type parameters
		   at all, we assume that the concerned java element has some legitimate business with them.
		 */
		if (typeParameters == null || typeParameters.length == 0) return true;
		Map<TypeBinding, Object> invocations = new HashMap<>(2);
		boolean noProblems = true;
		// preinitializing each type variable
		int paramLength = typeParameters.length;
		for (int i = 0; i < paramLength; i++) {
			TypeParameter typeParameter = typeParameters[i];
			TypeVariableBinding typeVariable = typeParameter.binding;
			if (typeVariable == null) return false;

			typeVariable.setSuperClass(getJavaLangObject());
			typeVariable.setSuperInterfaces(Binding.NO_SUPERINTERFACES);
			// set firstBound to the binding of the first explicit bound in parameter declaration
			typeVariable.setFirstBound(null); // first bound used to compute erasure
		}
		nextVariable: for (int i = 0; i < paramLength; i++) {
			TypeParameter typeParameter = typeParameters[i];
			TypeVariableBinding typeVariable = typeParameter.binding;
			TypeReference typeRef = typeParameter.type;
			if (typeRef == null)
				continue nextVariable;
			boolean isFirstBoundTypeVariable = false;
			TypeBinding superType = this.kind == METHOD_SCOPE
				? typeRef.resolveType((BlockScope)this, false/*no bound check*/, Binding.DefaultLocationTypeBound)
				: typeRef.resolveType((ClassScope)this, Binding.DefaultLocationTypeBound);
			if (superType == null) {
				typeVariable.tagBits |= TagBits.HierarchyHasProblems;
			} else {
				typeRef.resolvedType = superType; // hold onto the problem type
				firstBound: {
					switch (superType.kind()) {
						case Binding.ARRAY_TYPE :
							problemReporter().boundCannotBeArray(typeRef, superType);
							typeVariable.tagBits |= TagBits.HierarchyHasProblems;
							break firstBound; // do not keep first bound
						case Binding.TYPE_PARAMETER :
							isFirstBoundTypeVariable = true;
							TypeVariableBinding varSuperType = (TypeVariableBinding) superType;

							// https://bugs.eclipse.org/bugs/show_bug.cgi?id=335751
							if (typeVariable.rank >= varSuperType.rank && varSuperType.declaringElement == typeVariable.declaringElement) {
								SimpleSet set = new SimpleSet(typeParameters.length);
								set.add(typeVariable);
								ReferenceBinding superBinding = varSuperType;
								while (superBinding instanceof TypeVariableBinding) {
									if (set.includes(superBinding)) {
										problemReporter().hierarchyCircularity(typeVariable, varSuperType, typeRef);
										typeVariable.tagBits |= TagBits.HierarchyHasProblems;
										break firstBound; // do not keep first bound
									} else {
										set.add(superBinding);
										superBinding = ((TypeVariableBinding)superBinding).superclass;
									}
								}
							}
							break;
						default :
							if (((ReferenceBinding) superType).isFinal()) {
								if (!environment().usesNullTypeAnnotations() || (superType.tagBits & TagBits.AnnotationNullable) == 0) {
									problemReporter().finalVariableBound(typeVariable, typeRef);
								}
							}
							break;
					}
					ReferenceBinding superRefType = (ReferenceBinding) superType;
					if (!superType.isInterface()) {
						typeVariable.setSuperClass(superRefType);
					} else {
						typeVariable.setSuperInterfaces(new ReferenceBinding[] {superRefType});
					}
					typeVariable.tagBits |= superType.tagBits & (TagBits.ContainsNestedTypeReferences | TagBits.HasMissingType);
					typeVariable.setFirstBound(superRefType); // first bound used to compute erasure
				}
			}
			TypeReference[] boundRefs = typeParameter.bounds;
			if (boundRefs != null) {
				nextBound: for (int j = 0, boundLength = boundRefs.length; j < boundLength; j++) {
					typeRef = boundRefs[j];
					superType = this.kind == METHOD_SCOPE
						? typeRef.resolveType((BlockScope)this, false, Binding.DefaultLocationTypeBound)
						: typeRef.resolveType((ClassScope)this, Binding.DefaultLocationTypeBound);
					if (superType == null) {
						typeVariable.tagBits |= TagBits.HierarchyHasProblems;
						continue nextBound;
					} else {
						typeVariable.tagBits |= superType.tagBits & (TagBits.ContainsNestedTypeReferences | TagBits.HasMissingType);
						boolean didAlreadyComplain = !typeRef.resolvedType.isValidBinding();
						if (isFirstBoundTypeVariable && j == 0) {
							problemReporter().noAdditionalBoundAfterTypeVariable(typeRef);
							typeVariable.tagBits |= TagBits.HierarchyHasProblems;
							didAlreadyComplain = true;
							//continue nextBound; - keep these bounds to minimize secondary errors
						} else if (superType.isArrayType()) {
							if (!didAlreadyComplain) {
								problemReporter().boundCannotBeArray(typeRef, superType);
								typeVariable.tagBits |= TagBits.HierarchyHasProblems;
							}
							continue nextBound;
						} else {
							if (!superType.isInterface()) {
								if (!didAlreadyComplain) {
									problemReporter().boundMustBeAnInterface(typeRef, superType);
									typeVariable.tagBits |= TagBits.HierarchyHasProblems;
								}
								continue nextBound;
							}
						}
						// check against superclass
						if (checkForErasedCandidateCollisions && TypeBinding.equalsEquals(typeVariable.firstBound, typeVariable.superclass)) {
							if (hasErasedCandidatesCollisions(superType, typeVariable.superclass, invocations, typeVariable, typeRef)) {
								continue nextBound;
							}
						}
						// check against superinterfaces
						ReferenceBinding superRefType = (ReferenceBinding) superType;
						for (int index = typeVariable.superInterfaces.length; --index >= 0;) {
							ReferenceBinding previousInterface = typeVariable.superInterfaces[index];
							if (TypeBinding.equalsEquals(previousInterface, superRefType)) {
								problemReporter().duplicateBounds(typeRef, superType);
								typeVariable.tagBits |= TagBits.HierarchyHasProblems;
								continue nextBound;
							}
							if (checkForErasedCandidateCollisions) {
								if (hasErasedCandidatesCollisions(superType, previousInterface, invocations, typeVariable, typeRef)) {
									continue nextBound;
								}
							}
						}
						int size = typeVariable.superInterfaces.length;
						System.arraycopy(typeVariable.superInterfaces, 0, typeVariable.setSuperInterfaces(new ReferenceBinding[size + 1]), 0, size);
						typeVariable.superInterfaces[size] = superRefType;
					}
				}
			}
			noProblems &= (typeVariable.tagBits & TagBits.HierarchyHasProblems) == 0;
		}
		// after bounds have been resolved we're ready for resolving the type parameter itself,
		// which includes resolving/evaluating type annotations and checking for inconsistencies
		boolean declaresNullTypeAnnotation = false;
		for (int i = 0; i < paramLength; i++) {
			resolveTypeParameter(typeParameters[i]);
			declaresNullTypeAnnotation |= typeParameters[i].binding.hasNullTypeAnnotations();
		}
		if (declaresNullTypeAnnotation)
			for (int i = 0; i < paramLength; i++)
				typeParameters[i].binding.updateTagBits(); // <T extends List<U>, @NonNull U> --> tag T as having null type annotations
		return noProblems;
	}

	public ArrayBinding createArrayType(TypeBinding type, int dimension) {
		return createArrayType(type, dimension, Binding.NO_ANNOTATIONS);
	}

	public ArrayBinding createArrayType(TypeBinding type, int dimension, AnnotationBinding[] annotations) {
		if (type.isValidBinding())
			return environment().createArrayType(type, dimension, annotations);
		// do not cache obvious invalid types
		return new ArrayBinding(type, dimension, environment());
	}

	public TypeVariableBinding[] createTypeVariables(TypeParameter[] typeParameters, Binding declaringElement) {
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=324850, If they exist at all, process type parameters irrespective of source level.
		if (typeParameters == null || typeParameters.length == 0)
			return Binding.NO_TYPE_VARIABLES;

		PackageBinding unitPackage = compilationUnitScope().fPackage;
		int length = typeParameters.length;
		TypeVariableBinding[] typeVariableBindings = new TypeVariableBinding[length];
		int count = 0;
		for (int i = 0; i < length; i++) {
			TypeParameter typeParameter = typeParameters[i];
			TypeVariableBinding parameterBinding = new TypeVariableBinding(typeParameter.name, declaringElement, i, environment());
			parameterBinding.fPackage = unitPackage;
			typeParameter.binding = parameterBinding;

			if ((typeParameter.bits & ASTNode.HasTypeAnnotations) != 0) {
				switch(declaringElement.kind()) {
					case Binding.METHOD :
						MethodBinding methodBinding = (MethodBinding) declaringElement;
						AbstractMethodDeclaration sourceMethod = methodBinding.sourceMethod();
						if (sourceMethod != null) {
							sourceMethod.bits |= ASTNode.HasTypeAnnotations;
						}
						break;
					case Binding.TYPE :
						if (declaringElement instanceof SourceTypeBinding) {
							SourceTypeBinding sourceTypeBinding = (SourceTypeBinding) declaringElement;
							TypeDeclaration typeDeclaration = sourceTypeBinding.scope.referenceContext;
							if (typeDeclaration != null) {
								typeDeclaration.bits |= ASTNode.HasTypeAnnotations;
							}
						}
				}
			}
			// detect duplicates, but keep each variable to reduce secondary errors with instantiating this generic type (assume number of variables is correct)
			for (int j = 0; j < count; j++) {
				TypeVariableBinding knownVar = typeVariableBindings[j];
				if (CharOperation.equals(knownVar.sourceName, typeParameter.name))
					problemReporter().duplicateTypeParameterInType(typeParameter);
			}
			typeVariableBindings[count++] = parameterBinding;
//				TODO should offer warnings to inform about hiding declaring, enclosing or member types
//				ReferenceBinding type = sourceType;
//				// check that the member does not conflict with an enclosing type
//				do {
//					if (CharOperation.equals(type.sourceName, memberContext.name)) {
//						problemReporter().hidingEnclosingType(memberContext);
//						continue nextParameter;
//					}
//					type = type.enclosingType();
//				} while (type != null);
//				// check that the member type does not conflict with another sibling member type
//				for (int j = 0; j < i; j++) {
//					if (CharOperation.equals(referenceContext.memberTypes[j].name, memberContext.name)) {
//						problemReporter().duplicateNestedType(memberContext);
//						continue nextParameter;
//					}
//				}
		}
		if (count != length)
			System.arraycopy(typeVariableBindings, 0, typeVariableBindings = new TypeVariableBinding[count], 0, count);
		return typeVariableBindings;
	}

	void resolveTypeParameter(TypeParameter typeParameter) {
		// valid only for ClassScope and MethodScope
	}

	public final ClassScope enclosingClassScope() {
		Scope scope = this;
		while ((scope = scope.parent) != null) {
			if (scope instanceof ClassScope) return (ClassScope) scope;
		}
		return null; // may answer null if no type around
	}

	public ClassScope enclosingInstanceScope() {
		Scope scope = this;
		while (true) {
			scope = scope.parent;
			if (scope == null || scope instanceof MethodScope ms && ms.isStatic) {
				return null;
			} else if (scope instanceof ClassScope cs) {
				return cs;
			}
		}
	}

	public final MethodScope enclosingMethodScope() {
		Scope scope = this;
		while ((scope = scope.parent) != null) {
			if (scope instanceof MethodScope) return (MethodScope) scope;
		}
		return null; // may answer null if no method around
	}

	public final MethodScope enclosingLambdaScope() {
		Scope scope = this;
		while ((scope = scope.parent) != null) {
			if (scope instanceof MethodScope) {
				MethodScope methodScope = (MethodScope) scope;
				if (methodScope.referenceContext instanceof LambdaExpression)
					return methodScope;
			}
		}
		return null; // may answer null if no method around
	}
	public final MethodScope nearestEnclosingStaticScope() {
		Scope current = this;
		while (current != null) {
			MethodScope methodScope = current.methodScope();
			if (methodScope != null && !methodScope.isLambdaScope() && methodScope.isStatic)
				return methodScope;
			current = current.parent;
		}
		return null;
	}

	/* Answer the scope receiver type (could be parameterized)
	*/
	public final ReferenceBinding enclosingReceiverType() {
		Scope scope = this;
		do {
			if (scope instanceof ClassScope) {
				return environment().convertToParameterizedType(((ClassScope) scope).referenceContext.binding);
			}
			scope = scope.parent;
		} while (scope != null);
		return null;
	}
	/**
	 * Returns the immediately enclosing reference context, starting from current scope parent.
	 * If starting on a class, it will skip current class. If starting on unitScope, returns null.
	 */
	public ReferenceContext enclosingReferenceContext() {
		Scope current = this;
		while ((current = current.parent) != null) {
			switch(current.kind) {
				case METHOD_SCOPE :
					return ((MethodScope) current).referenceContext;
				case CLASS_SCOPE :
					return ((ClassScope) current).referenceContext;
				case MODULE_SCOPE :
					return ((ModuleScope) current).referenceContext;
				case COMPILATION_UNIT_SCOPE :
					return ((CompilationUnitScope) current).referenceContext;
			}
		}
		return null;
	}

	/* Answer the scope enclosing source type (could be generic)
	*/
	public final SourceTypeBinding enclosingSourceType() {
		Scope scope = this;
		do {
			if (scope instanceof ClassScope)
				return ((ClassScope) scope).referenceContext.binding;
			scope = scope.parent;
		} while (scope != null);
		return null;
	}

	public final LookupEnvironment environment() {
		return this.compilationUnitScope.environment;
	}

	/* Abstract method lookup (since maybe missing default abstract methods). "Default abstract methods" are methods that used to be emitted into
	   abstract classes for unimplemented interface methods at JDK 1.1 time frame. See SourceTypeBinding.addDefaultAbstractMethods()
	   See also https://bugs.eclipse.org/bugs/show_bug.cgi?id=174588 for details of problem addressed here. Problem was in the method call in the
	   *abstract* class. Unless the interface methods are looked up, we will emit code that results in infinite recursion.
	*/
	protected MethodBinding findDefaultAbstractMethod(
		ReferenceBinding receiverType,
		char[] selector,
		TypeBinding[] argumentTypes,
		InvocationSite invocationSite,
		ReferenceBinding classHierarchyStart,
		ObjectVector found,
		MethodBinding [] concreteMatches) {

		int startFoundSize = found.size;
		ReferenceBinding currentType = classHierarchyStart;
		List<TypeBinding> visitedTypes = new ArrayList<>();
		while (currentType != null) {
			findMethodInSuperInterfaces(currentType, selector, found, visitedTypes, invocationSite);
			currentType = currentType.superclass();
		}

		int candidatesCount = concreteMatches == null ? 0 : concreteMatches.length;
		int foundSize = found.size;
		MethodBinding[] candidates = new MethodBinding[foundSize - startFoundSize + candidatesCount];
		if (concreteMatches != null)
			System.arraycopy(concreteMatches, 0, candidates, 0, candidatesCount);

		MethodBinding problemMethod = null;
		if (foundSize > startFoundSize) {
			// argument type compatibility check
			final MethodVerifier methodVerifier = environment().methodVerifier();
			next:
			for (int i = startFoundSize; i < foundSize; i++) {
				MethodBinding methodBinding = (MethodBinding) found.elementAt(i);
				MethodBinding compatibleMethod = computeCompatibleMethod(methodBinding, argumentTypes, invocationSite);
				if (compatibleMethod != null) {
					if (compatibleMethod.isValidBinding()) {
						if (concreteMatches != null) {
							for (MethodBinding concreteMatch : concreteMatches) {
								if (methodVerifier.areMethodsCompatible(concreteMatch, compatibleMethod))
									continue; // can skip this method since concreteMatch overrides it
							}
						}
						for (int j = 0; j < startFoundSize; j++) {
							MethodBinding classMethod = (MethodBinding) found.elementAt(j);
							if (classMethod != null && methodVerifier.areMethodsCompatible(classMethod, compatibleMethod))
								continue next; // can skip this method since classMethod overrides it
						}
						candidates[candidatesCount++] = compatibleMethod;
					} else if (problemMethod == null) {
						problemMethod = compatibleMethod;
					}
				}
			}
		}
		MethodBinding concreteMatch = null;
		if (candidatesCount < 2) {
			if (concreteMatches == null) {
				if (candidatesCount == 0)
					return problemMethod; // can be null
			}
			concreteMatch = candidates[0];
			if (concreteMatch != null)
				compilationUnitScope().recordTypeReferences(concreteMatch.thrownExceptions);
			return concreteMatch;
		}
		// no need to check for visibility - interface methods are public
		return mostSpecificMethodBinding(candidates, candidatesCount, argumentTypes, invocationSite, receiverType);
	}

	// Internal use only
	public ReferenceBinding findDirectMemberType(char[] typeName, ReferenceBinding enclosingType) {
		if ((enclosingType.tagBits & TagBits.HasNoMemberTypes) != 0)
			return null; // know it has no member types (nor inherited member types)

		ReferenceBinding enclosingReceiverType = enclosingReceiverType();
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordReference(enclosingType, typeName);
		ReferenceBinding memberType = enclosingType.getMemberType(typeName);
		if (memberType != null) {
			unitScope.recordTypeReference(memberType);
			if (enclosingReceiverType == null) {
				if (memberType.canBeSeenBy(getCurrentPackage())) {
					return memberType;
				}
			} else if (memberType.canBeSeenBy(enclosingType, enclosingReceiverType)) {
				return memberType;
			}
			return new ProblemReferenceBinding(new char[][]{typeName}, memberType, ProblemReasons.NotVisible);
		}
		return null;
	}

	// Internal use only
	public MethodBinding findExactMethod(ReferenceBinding receiverType, char[] selector, TypeBinding[] argumentTypes, InvocationSite invocationSite) {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordTypeReferences(argumentTypes);
		MethodBinding exactMethod = receiverType.getExactMethod(selector, argumentTypes, unitScope);
		if (exactMethod != null && exactMethod.typeVariables == Binding.NO_TYPE_VARIABLES && !exactMethod.isBridge()) {
			// in >= 1.5 mode, ensure the exactMatch did not match raw types
			for (int i = argumentTypes.length; --i >= 0;) {
				// workaround for bug 464229: The type * cannot be resolved. It is indirectly referenced from required .class files
				TypeBinding t = argumentTypes[i].leafComponentType();
				if (! (t instanceof ReferenceBinding))
					continue;
				ReferenceBinding r = (ReferenceBinding)t;
				if (r.isHierarchyConnected()) {
					if (isSubtypeOfRawType(r))
						return null;
				} else if (r.isRawType()) {
					return null;
				}
				//TODO: should also check if any supertype of r is raw, but can't do this without resolving the whole hierarchy
			}
			// must find both methods for this case: <S extends A> void foo() {}  and  <N extends B> N foo() { return null; }
			// or find an inherited method when the exact match is to a bridge method
			unitScope.recordTypeReferences(exactMethod.thrownExceptions);
			if (exactMethod.isAbstract() && exactMethod.thrownExceptions != Binding.NO_EXCEPTIONS)
				return null; // may need to merge exceptions with interface method
			// special treatment for Object.getClass() in 1.5 mode (substitute parameterized return type)
			if (exactMethod.canBeSeenBy(receiverType, invocationSite, this)) {
				if (argumentTypes == Binding.NO_PARAMETERS
				    && CharOperation.equals(selector, TypeConstants.GETCLASS)
				    && exactMethod.returnType.isParameterizedType()/*1.5*/) {
						return environment().createGetClassMethod(receiverType, exactMethod, this);
			    }
				// targeting a generic method could find an exact match with variable return type
				if (invocationSite.genericTypeArguments() != null) {
					// computeCompatibleMethod(..) will return a PolymorphicMethodBinding if needed
					exactMethod = computeCompatibleMethod(exactMethod, argumentTypes, invocationSite);
				} else if (exactMethod.hasPolymorphicSignature(this)) {
					// generate polymorphic method and set polymorphic tagbits as well
					exactMethod.tagBits |= TagBits.AnnotationPolymorphicSignature;
					return this.environment().createPolymorphicMethod(exactMethod, argumentTypes, this);
				}
				return exactMethod;
			}
		}
		return null;
	}
	// Internal use only
	/*	Answer the field binding that corresponds to fieldName.
		Start the lookup at the receiverType.
		InvocationSite implements
			isSuperAccess(); this is used to determine if the discovered field is visible.
		Only fields defined by the receiverType or its supertypes are answered;
		a field of an enclosing type will not be found using this API.
    	If no visible field is discovered, null is answered.
	 */
	public FieldBinding findField(TypeBinding receiverType, char[] fieldName, InvocationSite invocationSite, boolean needResolve) {
		return findField(receiverType, fieldName, invocationSite, needResolve, false);
	}
	// Internal use only
	/*	Answer the field binding that corresponds to fieldName.
		Start the lookup at the receiverType.
		InvocationSite implements
			isSuperAccess(); this is used to determine if the discovered field is visible.
		Only fields defined by the receiverType or its supertypes are answered;
		a field of an enclosing type will not be found using this API.
        If the parameter invisibleFieldsOk is true, visibility checks have not been run on
        any returned fields. The caller needs to apply these checks as needed. Otherwise,
		If no visible field is discovered, null is answered.
	*/
	public FieldBinding findField(TypeBinding receiverType, char[] fieldName, InvocationSite invocationSite, boolean needResolve, boolean invisibleFieldsOk) {

		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordTypeReference(receiverType);

		checkArrayField: {
			TypeBinding leafType;
			switch (receiverType.kind()) {
				case Binding.BASE_TYPE :
					return null;
				case Binding.WILDCARD_TYPE :
				case Binding.INTERSECTION_TYPE:
				case Binding.TYPE_PARAMETER : // capture
					TypeBinding receiverErasure = receiverType.erasure();
					if (!receiverErasure.isArrayType())
						break checkArrayField;
					leafType = receiverErasure.leafComponentType();
					break;
				case Binding.ARRAY_TYPE :
					leafType = receiverType.leafComponentType();
					break;
				default:
					break checkArrayField;
			}
			if (leafType instanceof ReferenceBinding)
				if (!((ReferenceBinding) leafType).canBeSeenBy(this))
					return new ProblemFieldBinding((ReferenceBinding)leafType, fieldName, ProblemReasons.ReceiverTypeNotVisible);
			if (CharOperation.equals(fieldName, TypeConstants.LENGTH)) {
				if ((leafType.tagBits & TagBits.HasMissingType) != 0) {
					return new ProblemFieldBinding(ArrayBinding.ArrayLength, null, fieldName, ProblemReasons.NotFound);
				}
				return ArrayBinding.ArrayLength;
			}
			return null;
		}

		ReferenceBinding currentType = (ReferenceBinding) receiverType;
		if (!currentType.canBeSeenBy(this))
			return new ProblemFieldBinding(currentType, fieldName, ProblemReasons.ReceiverTypeNotVisible);

		currentType.initializeForStaticImports();
		FieldBinding field = currentType.getField(fieldName, needResolve);
		if (field != null) {
			if (invisibleFieldsOk) {
				return field;
			}
			if (invocationSite == null
				? field.canBeSeenBy(getCurrentPackage())
				: field.canBeSeenBy(currentType, invocationSite, this))
					return field;
			return new ProblemFieldBinding(field /* closest match*/, field.declaringClass, fieldName, ProblemReasons.NotVisible);
		}
		// collect all superinterfaces of receiverType until the field is found in a supertype
		ReferenceBinding[] interfacesToVisit = null;
		int nextPosition = 0;
		FieldBinding visibleField = null;
		boolean keepLooking = true;
		FieldBinding notVisibleField = null;
		// we could hold onto the not visible field for extra error reporting
		while (keepLooking) {
			ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
			if (itsInterfaces != null && itsInterfaces != Binding.NO_SUPERINTERFACES) {
				if (interfacesToVisit == null) {
					interfacesToVisit = itsInterfaces;
					nextPosition = interfacesToVisit.length;
				} else {
					int itsLength = itsInterfaces.length;
					if (nextPosition + itsLength >= interfacesToVisit.length)
						System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
					nextInterface : for (int a = 0; a < itsLength; a++) {
						ReferenceBinding next = itsInterfaces[a];
						for (int b = 0; b < nextPosition; b++)
							if (TypeBinding.equalsEquals(next, interfacesToVisit[b])) continue nextInterface;
						interfacesToVisit[nextPosition++] = next;
					}
				}
			}
			if ((currentType = currentType.superclass()) == null)
				break;

			unitScope.recordTypeReference(currentType);
			currentType.initializeForStaticImports();
			currentType = (ReferenceBinding) currentType.capture(this, invocationSite == null ? 0 : invocationSite.sourceStart(), invocationSite == null ? 0 : invocationSite.sourceEnd());
			if ((field = currentType.getField(fieldName, needResolve)) != null) {
				if (invisibleFieldsOk) {
					return field;
				}
				keepLooking = false;
				if (field.canBeSeenBy(receiverType, invocationSite, this)) {
					if (visibleField == null)
						visibleField = field;
					else
						return new ProblemFieldBinding(visibleField /* closest match*/, visibleField.declaringClass, fieldName, ProblemReasons.Ambiguous);
				} else {
					if (notVisibleField == null)
						notVisibleField = field;
				}
			}
		}

		// walk all visible interfaces to find ambiguous references
		if (interfacesToVisit != null) {
			ProblemFieldBinding ambiguous = null;
			done : for (int i = 0; i < nextPosition; i++) {
				ReferenceBinding anInterface = interfacesToVisit[i];
				unitScope.recordTypeReference(anInterface);
				// no need to capture rcv interface, since member field is going to be static anyway
				if ((field = anInterface.getField(fieldName, true /*resolve*/)) != null) {
					if (invisibleFieldsOk) {
						return field;
					}
					if (visibleField == null) {
						visibleField = field;
					} else {
						ambiguous = new ProblemFieldBinding(visibleField /* closest match*/, visibleField.declaringClass, fieldName, ProblemReasons.Ambiguous);
						break done;
					}
				} else {
					ReferenceBinding[] itsInterfaces = anInterface.superInterfaces();
					if (itsInterfaces != null && itsInterfaces != Binding.NO_SUPERINTERFACES) {
						int itsLength = itsInterfaces.length;
						if (nextPosition + itsLength >= interfacesToVisit.length)
							System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
						nextInterface : for (int a = 0; a < itsLength; a++) {
							ReferenceBinding next = itsInterfaces[a];
							for (int b = 0; b < nextPosition; b++)
								if (TypeBinding.equalsEquals(next, interfacesToVisit[b])) continue nextInterface;
							interfacesToVisit[nextPosition++] = next;
						}
					}
				}
			}
			if (ambiguous != null)
				return ambiguous;
		}

		if (visibleField != null)
			return visibleField;
		if (notVisibleField != null) {
			return new ProblemFieldBinding(notVisibleField, currentType, fieldName, ProblemReasons.NotVisible);
		}
		return null;
	}

	// Internal use only
	public ReferenceBinding findMemberType(char[] typeName, ReferenceBinding enclosingType) {
		if ((enclosingType.tagBits & TagBits.HasNoMemberTypes) != 0)
			return null; // know it has no member types (nor inherited member types)

		ReferenceBinding enclosingSourceType = enclosingSourceType();
		PackageBinding currentPackage = getCurrentPackage();
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordReference(enclosingType, typeName);
		ReferenceBinding memberType = enclosingType.getMemberType(typeName);
		if (memberType != null) {
			unitScope.recordTypeReference(memberType);
			if (enclosingSourceType == null || (this.parent == unitScope && (enclosingSourceType.tagBits & TagBits.TypeVariablesAreConnected) == 0)
				? memberType.canBeSeenBy(currentPackage)
				: memberType.canBeSeenBy(enclosingType, enclosingSourceType))
					return memberType;
			return new ProblemReferenceBinding(new char[][]{typeName}, memberType, ProblemReasons.NotVisible);
		}

		// collect all superinterfaces of receiverType until the memberType is found in a supertype
		ReferenceBinding currentType = enclosingType;
		ReferenceBinding[] interfacesToVisit = null;
		int nextPosition = 0;
		ReferenceBinding visibleMemberType = null;
		boolean keepLooking = true;
		ReferenceBinding notVisible = null;
		// we could hold onto the not visible field for extra error reporting
		while (keepLooking) {
			ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
			if (itsInterfaces == null) { // needed for statically imported types which don't know their hierarchy yet
				ReferenceBinding sourceType = currentType.isParameterizedType()
					? ((ParameterizedTypeBinding) currentType).genericType()
					: currentType;
				if (sourceType instanceof SourceTypeBinding) { // could be TypeVariableBinding
					if (sourceType.isHierarchyBeingConnected())
						return null; // looking for an undefined member type in its own superclass ref
					((SourceTypeBinding) sourceType).scope.connectTypeHierarchy();
				}
				itsInterfaces = currentType.superInterfaces();
			}
			if (itsInterfaces != null && itsInterfaces != Binding.NO_SUPERINTERFACES) {
				if (interfacesToVisit == null) {
					interfacesToVisit = itsInterfaces;
					nextPosition = interfacesToVisit.length;
				} else {
					int itsLength = itsInterfaces.length;
					if (nextPosition + itsLength >= interfacesToVisit.length)
						System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
					nextInterface : for (int a = 0; a < itsLength; a++) {
						ReferenceBinding next = itsInterfaces[a];
						for (int b = 0; b < nextPosition; b++)
							if (TypeBinding.equalsEquals(next, interfacesToVisit[b])) continue nextInterface;
						interfacesToVisit[nextPosition++] = next;
					}
				}
			}
			if ((currentType = currentType.superclass()) == null)
				break;

			unitScope.recordReference(currentType, typeName);
			if ((memberType = currentType.getMemberType(typeName)) != null) {
				unitScope.recordTypeReference(memberType);
				keepLooking = false;
				if (enclosingSourceType == null
					? memberType.canBeSeenBy(currentPackage)
					: memberType.canBeSeenBy(enclosingType, enclosingSourceType)) {
						if (visibleMemberType == null)
							visibleMemberType = memberType;
						else
							return new ProblemReferenceBinding(new char[][]{typeName}, visibleMemberType, ProblemReasons.Ambiguous);
				} else {
					notVisible = memberType;
				}
			}
		}
		// walk all visible interfaces to find ambiguous references
		if (interfacesToVisit != null) {
			ProblemReferenceBinding ambiguous = null;
			done : for (int i = 0; i < nextPosition; i++) {
				ReferenceBinding anInterface = interfacesToVisit[i];
				unitScope.recordReference(anInterface, typeName);
				if ((memberType = anInterface.getMemberType(typeName)) != null) {
					unitScope.recordTypeReference(memberType);
					if (visibleMemberType == null) {
						visibleMemberType = memberType;
					} else {
						ambiguous = new ProblemReferenceBinding(new char[][]{typeName}, visibleMemberType, ProblemReasons.Ambiguous);
						break done;
					}
				} else {
					ReferenceBinding[] itsInterfaces = anInterface.superInterfaces();
					if (itsInterfaces != null && itsInterfaces != Binding.NO_SUPERINTERFACES) {
						int itsLength = itsInterfaces.length;
						if (nextPosition + itsLength >= interfacesToVisit.length)
							System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
						nextInterface : for (int a = 0; a < itsLength; a++) {
							ReferenceBinding next = itsInterfaces[a];
							for (int b = 0; b < nextPosition; b++)
								if (TypeBinding.equalsEquals(next, interfacesToVisit[b])) continue nextInterface;
							interfacesToVisit[nextPosition++] = next;
						}
					}
				}
			}
			if (ambiguous != null)
				return ambiguous;
		}
		if (visibleMemberType != null)
			return visibleMemberType;
		if (notVisible != null)
			return new ProblemReferenceBinding(new char[][]{typeName}, notVisible, ProblemReasons.NotVisible);
		return null;
	}

	// Internal use only - use findMethod()
	public MethodBinding findMethod(ReferenceBinding receiverType, char[] selector, TypeBinding[] argumentTypes, InvocationSite invocationSite, boolean inStaticContext) {
		MethodBinding method = findMethod0(receiverType, selector, argumentTypes, invocationSite, inStaticContext);
		if (method != null && method.isValidBinding() && method.isVarargs()) {
			TypeBinding elementType = method.parameters[method.parameters.length - 1].leafComponentType();
			if (elementType instanceof ReferenceBinding) {
				if (!elementType.erasure().canBeSeenBy(this)) {
					return new ProblemMethodBinding(method, method.selector, invocationSite.genericTypeArguments(), ProblemReasons.VarargsElementTypeNotVisible);
				}
			}
		}
		return method;
	}

	public MethodBinding findMethod0(ReferenceBinding receiverType, char[] selector, TypeBinding[] argumentTypes, InvocationSite invocationSite, boolean inStaticContext) {
		ReferenceBinding currentType = receiverType;
		boolean receiverTypeIsInterface = receiverType.isInterface();
		ObjectVector found = new ObjectVector(3);
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordTypeReferences(argumentTypes);
		List<TypeBinding> visitedTypes = new ArrayList<>();
		if (receiverTypeIsInterface) {
			unitScope.recordTypeReference(receiverType);
			MethodBinding[] receiverMethods = receiverType.getMethods(selector, argumentTypes.length);
			if (receiverMethods.length > 0)
				found.addAll(receiverMethods);
			findMethodInSuperInterfaces(receiverType, selector, found, visitedTypes, invocationSite);
			currentType = getJavaLangObject();
		}

		// superclass lookup
		ReferenceBinding classHierarchyStart = currentType;
		MethodVerifier verifier = environment().methodVerifier();
		boolean currentIsSuper = false;
		MethodBinding singlePrivateMethod = null;
		boolean multiplePrivateMethods = false;
		while (currentType != null) {
			unitScope.recordTypeReference(currentType);
			currentType = (ReferenceBinding) currentType.capture(this, invocationSite == null ? 0 : invocationSite.sourceStart(), invocationSite == null ? 0 : invocationSite.sourceEnd());
			MethodBinding[] currentMethods = currentType.getMethods(selector, argumentTypes.length);
			int currentLength = currentMethods.length;
			if (currentLength > 0) {
				if ((receiverTypeIsInterface || found.size > 0)) {
					nextMethod: for (int i = 0, l = currentLength; i < l; i++) { // currentLength can be modified inside the loop
						MethodBinding currentMethod = currentMethods[i];
						if (currentMethod == null) continue nextMethod;
						if (receiverTypeIsInterface && !currentMethod.isPublic()) { // only public methods from Object are visible to interface receiverTypes
							currentLength--;
							currentMethods[i] = null;
							continue nextMethod;
						}

						// if 1.4 compliant, must filter out redundant protected methods from superclasses
						// protected method need to be checked only - default access is already dealt with in #canBeSeen implementation
						// when checking that p.C -> q.B -> p.A cannot see default access members from A through B.
						// if ((currentMethod.modifiers & AccProtected) == 0) continue nextMethod;
						// BUT we can also ignore any overridden method since we already know the better match (fixes 80028)
						for (int j = 0, max = found.size; j < max; j++) {
							MethodBinding matchingMethod = (MethodBinding) found.elementAt(j);
							MethodBinding matchingOriginal = matchingMethod.original();
							MethodBinding currentOriginal = matchingOriginal.findOriginalInheritedMethod(currentMethod);
							if (currentOriginal != null && verifier.isParameterSubsignature(matchingOriginal, currentOriginal)) {
								if (matchingMethod.isBridge() && !currentMethod.isBridge())
									continue nextMethod; // keep inherited methods to find concrete method over a bridge method
								currentLength--;
								currentMethods[i] = null;
								continue nextMethod;
							}
						}
					}
				}

				if (currentLength > 0) {
					// append currentMethods, filtering out null entries and private super methods
					if (currentMethods.length == currentLength && !currentIsSuper) {
						found.addAll(currentMethods);
					} else {
						for (MethodBinding currentMethod : currentMethods) {
							if (currentMethod != null) {
								if (currentIsSuper && currentMethod.isPrivate()) {
									if (singlePrivateMethod == null && !multiplePrivateMethods) {
										singlePrivateMethod = currentMethod;
									} else {
										singlePrivateMethod = null;
										multiplePrivateMethods = true;
									}
								} else {
									found.add(currentMethod);
								}
							}
						}
					}
				}
			}
			currentType = currentType.superclass();
			currentIsSuper = true;
		}

		// if found several candidates, then eliminate those not matching argument types
		int foundSize = found.size;
		MethodBinding[] candidates = null;
		int candidatesCount = 0;
		MethodBinding problemMethod = null;
		if (foundSize > 0) {
			// argument type compatibility check
			for (int i = 0; i < foundSize; i++) {
				MethodBinding methodBinding = (MethodBinding) found.elementAt(i);
				MethodBinding compatibleMethod = computeCompatibleMethod(methodBinding, argumentTypes, invocationSite);
				if (compatibleMethod != null) {
					if (compatibleMethod.isValidBinding() || compatibleMethod.problemId() == ProblemReasons.InvocationTypeInferenceFailure) {
						// we need to accept methods with InvocationTypeInferenceFailure, because logically overload resolution happens *before* invocation type inference
						if (foundSize == 1 && compatibleMethod.canBeSeenBy(receiverType, invocationSite, this)) {
							// return the single visible match now
							return findDefaultAbstractMethod(receiverType, selector, argumentTypes, invocationSite, classHierarchyStart, found, new MethodBinding [] {compatibleMethod});
						}
						if (candidatesCount == 0)
							candidates = new MethodBinding[foundSize];
						candidates[candidatesCount++] = compatibleMethod;
					} else if (compatibleMethod.problemId() == ProblemReasons.MissingTypeInSignature) {
						return compatibleMethod; // use this method for error message to give a hint about the missing type
					} else if (problemMethod == null) {
						problemMethod = compatibleMethod;
					}
				}
			}
		}

		// no match was found
		if (candidatesCount == 0) {
			if (problemMethod != null) {
				switch (problemMethod.problemId()) {
					case ProblemReasons.TypeArgumentsForRawGenericMethod :
					case ProblemReasons.TypeParameterArityMismatch :
						return problemMethod;
				}
			}

			// abstract classes may get a match in interfaces; for non abstract
			// classes, reduces secondary errors since missing interface method
			// error is already reported
			MethodBinding interfaceMethod =
				findDefaultAbstractMethod(receiverType, selector, argumentTypes, invocationSite, classHierarchyStart, found, null);
			if (interfaceMethod != null) {
				if (foundSize > 0 && interfaceMethod.isVarargs() && interfaceMethod instanceof ParameterizedGenericMethodBinding) {
					MethodBinding original = interfaceMethod.original();
					for (int i = 0; i < foundSize; i++) {
						MethodBinding classMethod = (MethodBinding) found.elementAt(i);
						if (!classMethod.isAbstract()) { // this check shouldn't matter, but to compatible with javac...
							MethodBinding substitute = verifier.computeSubstituteMethod(original, classMethod);
							if (substitute != null && verifier.isSubstituteParameterSubsignature(classMethod, substitute))
								return new ProblemMethodBinding(interfaceMethod, selector, argumentTypes, ProblemReasons.ApplicableMethodOverriddenByInapplicable);
						}
					}
				}
				return interfaceMethod;
			}
			if (found.size == 0) {
				if (singlePrivateMethod != null) {
					// if there is only one private method, we want to report that it is not visible.
					return new ProblemMethodBinding(singlePrivateMethod, selector, singlePrivateMethod.parameters, ProblemReasons.NotVisible);
				}
				return null;
			}
			if (problemMethod != null) return problemMethod;

			// still no match; try to find a close match when the parameter
			// order is wrong or missing some parameters

			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=69471
			// bad guesses are foo(), when argument types have been supplied
			// and foo(X, Y), when the argument types are (int, float, Y)
			// so answer the method with the most argType matches and least parameter type mismatches
			int bestArgMatches = -1;
			MethodBinding bestGuess = (MethodBinding) found.elementAt(0); // if no good match so just use the first one found
			int argLength = argumentTypes.length;
			foundSize = found.size;
			nextMethod : for (int i = 0; i < foundSize; i++) {
				MethodBinding methodBinding = (MethodBinding) found.elementAt(i);
				TypeBinding[] params = methodBinding.parameters;
				int paramLength = params.length;
				int argMatches = 0;
				next: for (int a = 0; a < argLength; a++) {
					TypeBinding arg = argumentTypes[a];
					for (int p = a == 0 ? 0 : a - 1; p < paramLength && p < a + 1; p++) { // look one slot before & after to see if the type matches
						if (TypeBinding.equalsEquals(params[p], arg)) {
							argMatches++;
							continue next;
						}
					}
				}
				if (argMatches < bestArgMatches)
					continue nextMethod;
				if (argMatches == bestArgMatches) {
					int diff1 = paramLength < argLength ? 2 * (argLength - paramLength) : paramLength - argLength;
					int bestLength = bestGuess.parameters.length;
					int diff2 = bestLength < argLength ? 2 * (argLength - bestLength) : bestLength - argLength;
					if (diff1 >= diff2)
						continue nextMethod;
				}
				if (bestGuess != methodBinding && MethodVerifier.doesMethodOverride(bestGuess, methodBinding, this.environment()))
					continue;
				bestArgMatches = argMatches;
				bestGuess = methodBinding;
			}
			return new ProblemMethodBinding(bestGuess, bestGuess.selector, argumentTypes, ProblemReasons.NotFound);
		}

		// tiebreak using visibility check
		int visiblesCount = 0;
		for (int i = 0; i < candidatesCount; i++) {
			MethodBinding methodBinding = candidates[i];
			if (methodBinding.canBeSeenBy(receiverType, invocationSite, this)) {
				if (visiblesCount != i) {
					candidates[i] = null;
					candidates[visiblesCount] = methodBinding;
				}
				visiblesCount++;
			}
		}

		switch (visiblesCount) {
			case 0 :
				MethodBinding interfaceMethod =
				findDefaultAbstractMethod(receiverType, selector, argumentTypes, invocationSite, classHierarchyStart, found, null);
				if (interfaceMethod != null) return interfaceMethod;
				MethodBinding candidate = candidates[0];
				int reason = ProblemReasons.NotVisible;
				if (candidate.isStatic() && candidate.declaringClass.isInterface() && !candidate.isPrivate()) {
					reason = ProblemReasons.NonStaticOrAlienTypeReceiver;
				}
				return new ProblemMethodBinding(candidate, candidate.selector, candidate.parameters, reason);
			case 1 :
				return findDefaultAbstractMethod(receiverType, selector, argumentTypes, invocationSite, classHierarchyStart, found, new MethodBinding [] { candidates[0] });
			default :
				break;
		}

		for (int i = 0; i < visiblesCount; i++) {
			MethodBinding candidate = candidates[i];
			if (candidate.isParameterizedGeneric())
				candidate = candidate.shallowOriginal();
			if (candidate.hasSubstitutedParameters()) {
				for (int j = i + 1; j < visiblesCount; j++) {
					MethodBinding otherCandidate = candidates[j];
					if (otherCandidate.hasSubstitutedParameters()) {
						if (otherCandidate == candidate
								|| (TypeBinding.equalsEquals(candidate.declaringClass, otherCandidate.declaringClass) && candidate.areParametersEqual(otherCandidate))) {
							return new ProblemMethodBinding(candidates[i], candidates[i].selector, candidates[i].parameters, ProblemReasons.Ambiguous);
						}
					}
				}
			}
		}
		if (inStaticContext) {
			MethodBinding[] staticCandidates = new MethodBinding[visiblesCount];
			int staticCount = 0;
			for (int i = 0; i < visiblesCount; i++)
				if (candidates[i].isStatic())
					staticCandidates[staticCount++] = candidates[i];
			if (staticCount == 1)
				return staticCandidates[0];
			if (staticCount > 1)
				return mostSpecificMethodBinding(staticCandidates, staticCount, argumentTypes, invocationSite, receiverType);
		}
		if (visiblesCount != candidates.length)
			System.arraycopy(candidates, 0, candidates = new MethodBinding[visiblesCount], 0, visiblesCount);
		return findDefaultAbstractMethod(receiverType, selector, argumentTypes, invocationSite, classHierarchyStart, found, candidates);
	}

	// Internal use only
	public MethodBinding findMethodForArray(
		ArrayBinding receiverType,
		char[] selector,
		TypeBinding[] argumentTypes,
		InvocationSite invocationSite) {

		TypeBinding leafType = receiverType.leafComponentType();
		if (leafType instanceof ReferenceBinding) {
			if (!((ReferenceBinding) leafType).canBeSeenBy(this))
				return new ProblemMethodBinding(selector, Binding.NO_PARAMETERS, (ReferenceBinding)leafType, ProblemReasons.ReceiverTypeNotVisible);
		}

		ReferenceBinding object = getJavaLangObject();
		MethodBinding methodBinding = object.getExactMethod(selector, argumentTypes, null);
		if (methodBinding != null) {
			// handle the method clone() specially... cannot be protected or throw exceptions
			if (argumentTypes == Binding.NO_PARAMETERS) {
			    switch (selector[0]) {
			        case 'c':
			            if (CharOperation.equals(selector, TypeConstants.CLONE)) {
			            	return receiverType.getCloneMethod(methodBinding);
			            }
			            break;
			        case 'g':
			            if (CharOperation.equals(selector, TypeConstants.GETCLASS) && methodBinding.returnType.isParameterizedType()/*1.5*/) {
							return environment().createGetClassMethod(receiverType, methodBinding, this);
			            }
			            break;
			    }
			}
			if (methodBinding.canBeSeenBy(receiverType, invocationSite, this))
				return methodBinding;
		}
		methodBinding = findMethod(object, selector, argumentTypes, invocationSite, false);
		if (methodBinding == null)
			return new ProblemMethodBinding(selector, argumentTypes, ProblemReasons.NoSuchMethodOnArray);
		return methodBinding;
	}

	protected void findMethodInSuperInterfaces(ReferenceBinding receiverType, char[] selector, ObjectVector found, List<TypeBinding> visitedTypes, InvocationSite invocationSite) {
		ReferenceBinding currentType = receiverType;
		ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
		if (itsInterfaces != null && itsInterfaces != Binding.NO_SUPERINTERFACES) {
			ReferenceBinding[] interfacesToVisit = itsInterfaces;
			int nextPosition = interfacesToVisit.length;
			interfaces: for (int i = 0; i < nextPosition; i++) {
				currentType = interfacesToVisit[i];
				if (visitedTypes != null) {
					TypeBinding uncaptured = currentType.uncapture(this);
					for (TypeBinding visited : visitedTypes) {
						if (uncaptured.isEquivalentTo(visited))
							continue interfaces;
					}
					visitedTypes.add(uncaptured);
				}
				compilationUnitScope().recordTypeReference(currentType);
				currentType = (ReferenceBinding) currentType.capture(this, invocationSite == null ? 0 : invocationSite.sourceStart(), invocationSite == null ? 0 : invocationSite.sourceEnd());
				MethodBinding[] currentMethods = currentType.getMethods(selector);
				if (currentMethods.length > 0) {
					int foundSize = found.size;
					next : for (MethodBinding current : currentMethods) {
						if (!current.canBeSeenBy(receiverType, invocationSite, this)) continue next;

						if (foundSize > 0) {
							// its possible to walk the same superinterface from different classes in the hierarchy
							for (int f = 0; f < foundSize; f++)
								if (current == found.elementAt(f)) continue next;
						}
						found.add(current);
					}
				}
				if ((itsInterfaces = currentType.superInterfaces()) != null && itsInterfaces != Binding.NO_SUPERINTERFACES) {
					int itsLength = itsInterfaces.length;
					if (nextPosition + itsLength >= interfacesToVisit.length)
						System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
					nextInterface : for (int a = 0; a < itsLength; a++) {
						ReferenceBinding next = itsInterfaces[a];
						for (int b = 0; b < nextPosition; b++)
							if (TypeBinding.equalsEquals(next, interfacesToVisit[b])) continue nextInterface;
						interfacesToVisit[nextPosition++] = next;
					}
				}
			}
		}
	}

	// Internal use only
	public ReferenceBinding findType(
		char[] typeName,
		PackageBinding declarationPackage,
		PackageBinding invocationPackage) {

		compilationUnitScope().recordReference(declarationPackage.compoundName, typeName);
		ReferenceBinding typeBinding = declarationPackage.getType(typeName, module());
		if (typeBinding == null)
			return null;

		if (typeBinding.isValidBinding()) {
			if (declarationPackage != invocationPackage && !typeBinding.canBeSeenBy(invocationPackage))
				return new ProblemReferenceBinding(new char[][]{typeName}, typeBinding, ProblemReasons.NotVisible);
		}
		return typeBinding;
	}

	public LocalVariableBinding findVariable(char[] variable) {
		return null;
	}

	public boolean resolvingGuardExpression() {
		return false;
	}

	/* API
	 *
	 *	Answer the binding that corresponds to the argument name.
	 *	flag is a mask of the following values VARIABLE (= FIELD or LOCAL), TYPE, PACKAGE.
	 *	Only bindings corresponding to the mask can be answered.
	 *
	 *	For example, getBinding("foo", VARIABLE, site) will answer
	 *	the binding for the field or local named "foo" (or an error binding if none exists).
	 *	If a type named "foo" exists, it will not be detected (and an error binding will be answered)
	 *
	 *	The VARIABLE mask has precedence over the TYPE mask.
	 *
	 *	If the VARIABLE mask is not set, neither fields nor locals will be looked for.
	 *
	 *	InvocationSite implements:
	 *		isSuperAccess(); this is used to determine if the discovered field is visible.
	 *
	 *	Limitations: cannot request FIELD independently of LOCAL, or vice versa
	 */
	public Binding getBinding(char[] name, int mask, InvocationSite invocationSite, boolean needResolve) {
		CompilationUnitScope unitScope = compilationUnitScope();
		LookupEnvironment env = unitScope.environment;
		try {
			env.missingClassFileLocation = invocationSite;
			Binding binding = null;
			FieldBinding problemField = null;
			if ((mask & Binding.VARIABLE) != 0) {
				boolean insideStaticContext = false;
				boolean insideLocalRecordContext = false;
				boolean insideConstructorCall = false;
				boolean insideTypeAnnotation = false;

				FieldBinding foundField = null;
				// can be a problem field which is answered if a valid field is not found
				ProblemFieldBinding foundInsideProblem = null;
				// inside Constructor call or inside static context
				Scope scope = this;
				MethodScope methodScope = null;
				int depth = 0;
				int foundDepth = 0;
				boolean shouldTrackOuterLocals = false;
				boolean resolvingGuardExpression = false;
				ReferenceBinding foundActualReceiverType = null;
				done : while (true) { // done when a COMPILATION_UNIT_SCOPE is found
					switch (scope.kind) {
						case METHOD_SCOPE :
							methodScope = (MethodScope) scope;
							insideStaticContext |= methodScope.isStatic;
							insideConstructorCall |= methodScope.isConstructorCall;
							insideTypeAnnotation = methodScope.insideTypeAnnotation;

							//$FALL-THROUGH$ could duplicate the code below to save a cast - questionable optimization
						case BLOCK_SCOPE :
							if (!resolvingGuardExpression)
								resolvingGuardExpression = scope.resolvingGuardExpression();
							LocalVariableBinding variableBinding = scope.findVariable(name);
							// looks in this scope only
							if (variableBinding != null) {
								if (insideLocalRecordContext) {
									return new ProblemLocalVariableBinding(
											variableBinding,
											ProblemReasons.NonStaticReferenceInStaticContext);
								}
								if (foundField != null && foundField.isValidBinding())
									return new ProblemFieldBinding(
										foundField, // closest match
										foundField.declaringClass,
										name,
										ProblemReasons.InheritedNameHidesEnclosingName);
								if (depth > 0)
									invocationSite.setDepth(depth);
								if (shouldTrackOuterLocals) {
									if (invocationSite instanceof NameReference) {
										NameReference nameReference = (NameReference) invocationSite;
										nameReference.bits |= ASTNode.IsCapturedOuterLocal;
										variableBinding.tagBits |= TagBits.HasToBeEffectivelyFinal;
									} else if (invocationSite instanceof AbstractVariableDeclaration) {
										AbstractVariableDeclaration variableDeclaration = (AbstractVariableDeclaration) invocationSite;
										variableDeclaration.bits |= ASTNode.ShadowsOuterLocal;
									}
								}
								if (resolvingGuardExpression && invocationSite instanceof NameReference nameReference) {
									nameReference.bits |= ASTNode.IsUsedInPatternGuard;
									variableBinding.tagBits |= TagBits.HasToBeEffectivelyFinal;
								}
								return variableBinding;
							}
							break;
						case CLASS_SCOPE :
							ClassScope classScope = (ClassScope) scope;
							ReferenceBinding receiverType = classScope.enclosingReceiverType();
							if (!insideTypeAnnotation) {
								FieldBinding fieldBinding = classScope.findField(receiverType, name, invocationSite, needResolve);
								// Use next line instead if willing to enable protected access accross inner types
								// FieldBinding fieldBinding = findField(enclosingType, name, invocationSite);

								if (fieldBinding != null) { // skip it if we did not find anything
									if (fieldBinding.problemId() == ProblemReasons.Ambiguous) {
										if (foundField == null || foundField.problemId() == ProblemReasons.NotVisible)
											// supercedes any potential InheritedNameHidesEnclosingName problem
											return fieldBinding;
										// make the user qualify the field, likely wants the first inherited field (javac generates an ambiguous error instead)
										return new ProblemFieldBinding(
											foundField, // closest match
											foundField.declaringClass,
											name,
											ProblemReasons.InheritedNameHidesEnclosingName);
									}

									ProblemFieldBinding insideProblem = null;
									if (fieldBinding.isValidBinding()) {
										if (!fieldBinding.isStatic()) {
											if (insideConstructorCall) {
												// in 25+ check for assignment legality is deferred to Reference.checkFieldAccessInEarlyConstructionContext()
												if (!JavaFeature.FLEXIBLE_CONSTRUCTOR_BODIES.isSupported(compilerOptions())) {
													insideProblem =
														new ProblemFieldBinding(
															fieldBinding, // closest match
															fieldBinding.declaringClass,
															name,
															ProblemReasons.NonStaticReferenceInConstructorInvocation);
												}
											} else if (insideStaticContext) {
												insideProblem =
													new ProblemFieldBinding(
														fieldBinding, // closest match
														fieldBinding.declaringClass,
														name,
														ProblemReasons.NonStaticReferenceInStaticContext);
											}
										}
										// found a valid field in the 'immediate' scope (i.e. not inherited)
										// OR in 1.4 mode (inherited shadows enclosing)
										if (foundField == null || foundField.problemId() == ProblemReasons.NotVisible) {
											if (depth > 0){
												invocationSite.setDepth(depth);
												invocationSite.setActualReceiverType(receiverType);
											}
											// return the fieldBinding if it is not declared in a superclass of the scope's binding (that is, inherited)
											return insideProblem == null ? fieldBinding : insideProblem;
										}
										if (foundField.isValidBinding())
											// if a valid field was found, complain when another is found in an 'immediate' enclosing type (that is, not inherited)
											// but only if "valid field" was inherited in the first place.
											if (TypeBinding.notEquals(foundField.declaringClass, fieldBinding.declaringClass) &&
											    TypeBinding.notEquals(foundField.declaringClass, foundActualReceiverType)) // https://bugs.eclipse.org/bugs/show_bug.cgi?id=316956
												// i.e. have we found the same field - do not trust field identity yet
												return new ProblemFieldBinding(
													foundField, // closest match
													foundField.declaringClass,
													name,
													ProblemReasons.InheritedNameHidesEnclosingName);
									}

									if (foundField == null || (foundField.problemId() == ProblemReasons.NotVisible && fieldBinding.problemId() != ProblemReasons.NotVisible)) {
										// only remember the fieldBinding if its the first one found or the previous one was not visible & fieldBinding is...
										foundDepth = depth;
										foundActualReceiverType = receiverType;
										foundInsideProblem = insideProblem;
										foundField = fieldBinding;
									}
								}
							}
							insideTypeAnnotation = false;
							depth++;
							shouldTrackOuterLocals = true;
							insideStaticContext |= receiverType.isStatic();
							insideLocalRecordContext |= receiverType.isRecord() && receiverType.isLocalType();
							// 1EX5I8Z - accessing outer fields within a constructor call is permitted
							// in order to do so, we change the flag as we exit from the type, not the method
							// itself, because the class scope is used to retrieve the fields.
							MethodScope enclosingMethodScope = scope.methodScope();
							insideConstructorCall = enclosingMethodScope == null ? false : enclosingMethodScope.isConstructorCall;
							break;
						case COMPILATION_UNIT_SCOPE :
						case MODULE_SCOPE :
							break done;
					}
					if (scope.isLambdaScope()) // Not in Kansas anymore ...
						shouldTrackOuterLocals = true;
					scope = scope.parent;
				}

				if (foundInsideProblem != null)
					return foundInsideProblem;
				if (foundField != null) {
					if (foundField.isValidBinding()) {
						if (foundDepth > 0) {
							invocationSite.setDepth(foundDepth);
							invocationSite.setActualReceiverType(foundActualReceiverType);
						}
						return foundField;
					}
					problemField = foundField;
					foundField = null;
				}

				// at this point the scope is a compilation unit scope & need to check for imported static fields
				unitScope.faultInImports(); // ensure static imports are resolved
				ImportBinding[] imports = unitScope.imports;
				if (imports != null) {
					// check single static imports
					for (ImportBinding importBinding : imports) {
						if (importBinding.isStatic() && !importBinding.onDemand) {
							if (CharOperation.equals(importBinding.getSimpleName(), name)) {
								if (unitScope.resolveSingleImport(importBinding, Binding.TYPE | Binding.FIELD | Binding.METHOD) != null
										&& importBinding.getResolvedImport() instanceof FieldBinding resolvedField)
								{
									foundField = resolvedField;
									ImportReference importReference = importBinding.reference;
									if (importReference != null && needResolve) {
										importReference.bits |= ASTNode.Used;
									}
									TypeBinding importedType = this.getType(importBinding.compoundName, importBinding.compoundName.length-1);
									if (importedType instanceof ReferenceBinding && importBinding.isValidBinding()) {
										invocationSite.setActualReceiverType((ReferenceBinding) importedType);
									} else {
										invocationSite.setActualReceiverType(foundField.declaringClass);
									}
									if (foundField.isValidBinding()) {
										return foundField;
									}
									if (problemField == null)
										problemField = foundField;
								}
							}
						}
					}
					// check on demand imports
					boolean foundInImport = false;
					ReferenceBinding sourceCodeReceiver = null;
					for (ImportBinding importBinding : imports) {
						if (importBinding.isStatic() && importBinding.onDemand) {
							Binding resolvedImport = importBinding.getResolvedImport();
							if (resolvedImport instanceof ReferenceBinding) {
								ReferenceBinding importedReferenceBinding = (ReferenceBinding) resolvedImport;
								FieldBinding temp = findField(importedReferenceBinding, name, invocationSite, needResolve);
								if (temp != null) {
									if (!temp.isValidBinding()) {
										if (problemField == null)
											problemField = temp;
									} else if (temp.isStatic()) {
										if (foundField == temp) continue;
										ImportReference importReference = importBinding.reference;
										if (importReference != null && needResolve) {
											importReference.bits |= ASTNode.Used;
										}
										if (foundInImport)
											// Answer error binding -- import on demand conflict; name found in two import on demand packages.
											return new ProblemFieldBinding(
													foundField, // closest match
													foundField.declaringClass,
													name,
													ProblemReasons.Ambiguous);
										foundField = temp;
										sourceCodeReceiver = importedReferenceBinding;
										foundInImport = true;
									}
								}
							}
						}
					}
					if (foundField != null) {
						if (sourceCodeReceiver != null)
							invocationSite.setActualReceiverType(sourceCodeReceiver);
						else
							invocationSite.setActualReceiverType(foundField.declaringClass);
						return foundField;
					}
				}
			}

			// We did not find a local or instance variable.
			if ((mask & Binding.TYPE) != 0) {
				if ((binding = getBaseType(name)) != null)
					return binding;
				binding = getTypeOrPackage(name, (mask & Binding.PACKAGE) == 0 ? Binding.TYPE : Binding.TYPE | Binding.PACKAGE, needResolve);
				if (binding.isValidBinding() || mask == Binding.TYPE)
					return binding;
				// answer the problem type binding if we are only looking for a type
			} else if ((mask & Binding.PACKAGE) != 0) {
				unitScope.recordSimpleReference(name);
				if ((binding = env.getTopLevelPackage(name)) != null)
					return binding;
			}
			if (problemField != null) return problemField;
			if (binding != null && binding.problemId() != ProblemReasons.NotFound)
				return binding; // answer the better problem binding
			return new ProblemBinding(name, enclosingSourceType(), ProblemReasons.NotFound);
		} catch (AbortCompilation e) {
			e.updateContext(invocationSite, referenceCompilationUnit().compilationResult);
			throw e;
		} finally {
			env.missingClassFileLocation = null;
		}
	}

	static class MethodClashException extends RuntimeException {
		private static final long serialVersionUID = -7996779527641476028L;
	}

	// For exact method references. 15.13.1
	private MethodBinding getExactMethod(TypeBinding receiverType, TypeBinding type, char[] selector, InvocationSite invocationSite, MethodBinding candidate) {

		if (type == null)
			return null;

		TypeBinding [] superInterfaces = type.superInterfaces();
		TypeBinding [] typePlusSupertypes = new TypeBinding[2 + superInterfaces.length];
		typePlusSupertypes[0] = type;
		typePlusSupertypes[1] = type.superclass();
		if (superInterfaces.length != 0)
			System.arraycopy(superInterfaces, 0, typePlusSupertypes, 2, superInterfaces.length);

		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordTypeReference(type);
		type = type.capture(this, invocationSite.sourceStart(), invocationSite.sourceEnd());

		for (int i = 0, typesLength = typePlusSupertypes.length; i < typesLength; i++) {
			MethodBinding[] methods = i == 0 ? type.getMethods(selector) : new MethodBinding [] { getExactMethod(receiverType, typePlusSupertypes[i], selector, invocationSite, candidate) };
			for (MethodBinding currentMethod : methods) {
				if (currentMethod == null || candidate == currentMethod)
					continue;
				if (i == 0 && (!currentMethod.canBeSeenBy(receiverType, invocationSite, this) || currentMethod.isSynthetic() || currentMethod.isBridge()))
					continue;
				if (candidate != null) {
					if (!candidate.areParameterErasuresEqual(currentMethod))
						throw new MethodClashException();
				} else {
					candidate = currentMethod;
				}
			}
		}
		return candidate;
	}

	// For exact method references. 15.13.1
	public MethodBinding getExactMethod(TypeBinding receiverType, char[] selector, InvocationSite invocationSite) {
		if (receiverType == null || !receiverType.isValidBinding() || receiverType.isBaseType())
			return null;
		TypeBinding currentType = receiverType;
		if (currentType.isArrayType()) {
			if (!currentType.leafComponentType().canBeSeenBy(this))
				return null;
			currentType = getJavaLangObject();
		}

		MethodBinding exactMethod = null;
		try {
			exactMethod = getExactMethod(receiverType, currentType, selector, invocationSite, null);
		} catch (MethodClashException e) {
			return null;
		}
		if (exactMethod == null || !exactMethod.canBeSeenBy(invocationSite, this))
			return null;

		final TypeBinding[] typeArguments = invocationSite.genericTypeArguments();
		TypeVariableBinding[] typeVariables = exactMethod.typeVariables();
		if (exactMethod.isVarargs() || (typeVariables != Binding.NO_TYPE_VARIABLES && (typeArguments == null || typeArguments.length != typeVariables.length)))
			return null;

		if (receiverType.isArrayType()) {
			if (CharOperation.equals(selector, TypeConstants.CLONE))
				return ((ArrayBinding) receiverType).getCloneMethod(exactMethod);
			if (CharOperation.equals(selector, TypeConstants.GETCLASS))
				return environment().createGetClassMethod(receiverType, exactMethod, this);
		}
		if (exactMethod.declaringClass.id == TypeIds.T_JavaLangObject
				&& CharOperation.equals(selector, TypeConstants.GETCLASS)
			    && exactMethod.returnType.isParameterizedType())
		{
			return environment().createGetClassMethod(receiverType, exactMethod, this);
		}

		if (typeVariables != Binding.NO_TYPE_VARIABLES)
			return environment().createParameterizedGenericMethod(exactMethod, typeArguments);

		return exactMethod;
	}

	// For exact constructor references. 15.13.1
	public MethodBinding getExactConstructor(TypeBinding receiverType, InvocationSite invocationSite) {
		if (receiverType == null || !receiverType.isValidBinding() || !receiverType.canBeInstantiated() || receiverType.isBaseType())
			return null;
		if (receiverType.isArrayType()) {
			TypeBinding leafType = receiverType.leafComponentType();
			if (!leafType.canBeSeenBy(this) || !leafType.isReifiable())
				return null;
			return new MethodBinding(ClassFileConstants.AccPublic | ClassFileConstants.AccSynthetic, TypeConstants.INIT,
								receiverType,
								new TypeBinding[] { TypeBinding.INT },
								Binding.NO_EXCEPTIONS,
								getJavaLangObject()); // just lie.
		}

		CompilationUnitScope unitScope = compilationUnitScope();
		MethodBinding exactConstructor = null;
		unitScope.recordTypeReference(receiverType);
		MethodBinding[] methods = receiverType.getMethods(TypeConstants.INIT);
		final TypeBinding[] genericTypeArguments = invocationSite.genericTypeArguments();
		for (MethodBinding constructor : methods) {
			if (!constructor.canBeSeenBy(invocationSite, this))
				continue;
			if (constructor.isVarargs())
				return null;
			if (constructor.typeVariables() != Binding.NO_TYPE_VARIABLES && genericTypeArguments == null)
				return null;
			if (exactConstructor == null) {
				exactConstructor = constructor;
			} else {
				return null;
			}
		}
		if (exactConstructor != null) {
			final TypeVariableBinding[] typeVariables = exactConstructor.typeVariables();
			if (typeVariables != Binding.NO_TYPE_VARIABLES) {
				if (typeVariables.length != genericTypeArguments.length)
					return null;
				exactConstructor = environment().createParameterizedGenericMethod(exactConstructor, genericTypeArguments);
			}
		}
		return exactConstructor;
	}

	public MethodBinding getConstructor(ReferenceBinding receiverType, TypeBinding[] argumentTypes, InvocationSite invocationSite) {
		MethodBinding method = getConstructor0(receiverType, argumentTypes, invocationSite);
		if (method != null && method.isValidBinding() && method.isVarargs()) {
			TypeBinding elementType = method.parameters[method.parameters.length - 1].leafComponentType();
			if (elementType instanceof ReferenceBinding) {
				if (!((ReferenceBinding) elementType).canBeSeenBy(this)) {
					return new ProblemMethodBinding(method, method.selector, invocationSite.genericTypeArguments(), ProblemReasons.VarargsElementTypeNotVisible);
				}
			}
		}
		return method;
	}

	public MethodBinding getConstructor0(ReferenceBinding receiverType, TypeBinding[] argumentTypes, InvocationSite invocationSite) {
		CompilationUnitScope unitScope = compilationUnitScope();
		LookupEnvironment env = unitScope.environment;
		try {
			env.missingClassFileLocation = invocationSite;
			unitScope.recordTypeReference(receiverType);
			unitScope.recordTypeReferences(argumentTypes);
			MethodBinding methodBinding = receiverType.getExactConstructor(argumentTypes);
			if (methodBinding != null && methodBinding.canBeSeenBy(invocationSite, this)) {
			    // targeting a non generic constructor with type arguments ?
			    if (invocationSite.genericTypeArguments() != null)
			    	methodBinding = computeCompatibleMethod(methodBinding, argumentTypes, invocationSite);
				return methodBinding;
			}
			MethodBinding[] methods = receiverType.getMethods(TypeConstants.INIT, argumentTypes.length);
			if (methods == Binding.NO_METHODS)
				return new ProblemMethodBinding(
					TypeConstants.INIT,
					argumentTypes,
					ProblemReasons.NotFound);

			MethodBinding[] compatible = new MethodBinding[methods.length];
			int compatibleIndex = 0;
			MethodBinding problemMethod = null;
			for (MethodBinding method : methods) {
				MethodBinding compatibleMethod = computeCompatibleMethod(method, argumentTypes, invocationSite);
				if (compatibleMethod != null) {
					if (compatibleMethod.isValidBinding())
						compatible[compatibleIndex++] = compatibleMethod;
					else if (compatibleMethod.problemId() == ProblemReasons.MissingTypeInSignature)
						return compatibleMethod; // use this method for error message to give a hint about the missing type
					else if (problemMethod == null)
						problemMethod = compatibleMethod;
				}
			}
			if (compatibleIndex == 0) {
				if (problemMethod == null)
					return new ProblemMethodBinding(methods[0], TypeConstants.INIT, argumentTypes, ProblemReasons.NotFound);
				return problemMethod;
			}
			// need a more descriptive error... cannot convert from X to Y

			MethodBinding[] visible = new MethodBinding[compatibleIndex];
			int visibleIndex = 0;
			for (int i = 0; i < compatibleIndex; i++) {
				MethodBinding method = compatible[i];
				if (method.canBeSeenBy(invocationSite, this))
					visible[visibleIndex++] = method;
			}
			if (visibleIndex == 1) {
				return visible[0];
			}
			if (visibleIndex == 0)
				return new ProblemMethodBinding(
					compatible[0],
					TypeConstants.INIT,
					compatible[0].parameters,
					ProblemReasons.NotVisible);
			// all of visible are from the same declaringClass, even before 1.4 we can call this method instead of mostSpecificClassMethodBinding
			return mostSpecificMethodBinding(visible, visibleIndex, argumentTypes, invocationSite, receiverType);
		} catch (AbortCompilation e) {
			e.updateContext(invocationSite, referenceCompilationUnit().compilationResult);
			throw e;
		} finally {
			env.missingClassFileLocation = null;
		}
	}

	public final PackageBinding getCurrentPackage() {
		return this.compilationUnitScope.fPackage;
	}

	/**
	 * Returns the modifiers of the innermost enclosing declaration.
	 * @return modifiers
	 */
	public int getDeclarationModifiers() {
		switch(this.kind){
			case Scope.BLOCK_SCOPE :
			case Scope.METHOD_SCOPE :
				MethodScope methodScope = methodScope();
				if (!methodScope.isInsideInitializer()){
					// check method modifiers to see if deprecated
					MethodBinding context = ((AbstractMethodDeclaration)methodScope.referenceContext).binding;
					if (context != null)
						return context.modifiers;
				} else {
					SourceTypeBinding type = ((BlockScope) this).referenceType().binding;

					// inside field declaration ? check field modifier to see if deprecated
					if (methodScope.initializedField != null)
						return methodScope.initializedField.modifiers;
					if (type != null)
						return type.modifiers;
				}
				break;
			case Scope.MODULE_SCOPE :
				return ((ModuleScope)this).referenceContext.modifiers;
			case Scope.CLASS_SCOPE :
				ReferenceBinding context = ((ClassScope)this).referenceType().binding;
				if (context != null)
					return context.modifiers;
				break;
		}
		return -1;
	}

	public FieldBinding getField(TypeBinding receiverType, char[] fieldName, InvocationSite invocationSite) {
		LookupEnvironment env = environment();
		try {
			env.missingClassFileLocation = invocationSite;
			FieldBinding field = findField(receiverType, fieldName, invocationSite, true /*resolve*/);
			if (field != null) return field;

			return new ProblemFieldBinding(
				receiverType instanceof ReferenceBinding ? (ReferenceBinding) receiverType : null,
				fieldName,
				ProblemReasons.NotFound);
		} catch (AbortCompilation e) {
			e.updateContext(invocationSite, referenceCompilationUnit().compilationResult);
			throw e;
		} finally {
			env.missingClassFileLocation = null;
		}
	}

	/* API
	 *
	 *	Answer the method binding that corresponds to selector, argumentTypes.
	 *	Start the lookup at the enclosing type of the receiver.
	 *	InvocationSite implements
	 *		isSuperAccess(); this is used to determine if the discovered method is visible.
	 *		setDepth(int); this is used to record the depth of the discovered method
	 *			relative to the enclosing type of the receiver. (If the method is defined
	 *			in the enclosing type of the receiver, the depth is 0; in the next enclosing
	 *			type, the depth is 1; and so on
	 *
	 *	If no visible method is discovered, an error binding is answered.
	 */
	public MethodBinding getImplicitMethod(char[] selector, TypeBinding[] argumentTypes, InvocationSite invocationSite) {

		boolean insideStaticContext = false;
		boolean insideConstructorCall = false;
		boolean insideTypeAnnotation = false;

		MethodBinding foundProblem = null;
		boolean foundProblemVisible = false;
		Scope scope = this;
		MethodScope methodScope = null;
		int depth = 0;

		done : while (true) { // done when a COMPILATION_UNIT_SCOPE is found
			switch (scope.kind) {
				case METHOD_SCOPE :
					methodScope = (MethodScope) scope;
					insideStaticContext |= methodScope.isStatic;
					insideConstructorCall |= methodScope.isConstructorCall;
					insideTypeAnnotation = methodScope.insideTypeAnnotation;
					break;
				case CLASS_SCOPE :
					ClassScope classScope = (ClassScope) scope;
					ReferenceBinding receiverType = classScope.enclosingReceiverType();
					if (!insideTypeAnnotation) {
						// retrieve an exact visible match (if possible)
						// compilationUnitScope().recordTypeReference(receiverType);   not needed since receiver is the source type
						MethodBinding methodBinding = classScope.findExactMethod(receiverType, selector, argumentTypes, invocationSite);
						if (methodBinding == null)
							methodBinding = classScope.findMethod(receiverType, selector, argumentTypes, invocationSite, false);
						if (methodBinding != null) { // skip it if we did not find anything
							 if (methodBinding.isValidBinding()) {
							 	if (lacksRequiredInstanceScope(methodBinding, insideConstructorCall, insideStaticContext)) {
							 		if (foundProblem != null && foundProblem.problemId() != ProblemReasons.NotVisible)
							 			return foundProblem; // takes precedence
							 		return new ProblemMethodBinding(
							 			methodBinding, // closest match
							 			methodBinding.selector,
							 			methodBinding.parameters,
							 			insideConstructorCall
							 				? ProblemReasons.NonStaticReferenceInConstructorInvocation
							 				: ProblemReasons.NonStaticReferenceInStaticContext);
							 	} else if (!methodBinding.isStatic() && methodScope != null) {
							 		tagAsAccessingEnclosingInstanceStateOf(receiverType, false /* type variable access */);
							 	}
							 	// found a valid method in the 'immediate' scope (i.e. not inherited)
							 	// OR in 1.4 mode (inherited visible shadows enclosing)
							 	// OR the receiverType implemented a method with the correct name
							 	// return the methodBinding if it is not declared in a superclass of the scope's binding (that is, inherited)
							 	if (foundProblemVisible) {
							 		return foundProblem;
							 	}
							 	if (depth > 0) {
							 		invocationSite.setDepth(depth);
							 		invocationSite.setActualReceiverType(receiverType);
							 	}
							 	// special treatment for Object.getClass() in 1.5 mode (substitute parameterized return type)
							 	if (argumentTypes == Binding.NO_PARAMETERS
							 	    && CharOperation.equals(selector, TypeConstants.GETCLASS)
							 	    && methodBinding.returnType.isParameterizedType()/*1.5*/) {
							 			return environment().createGetClassMethod(receiverType, methodBinding, this);
							 	}
							 	return methodBinding;
							 } else { // methodBinding is a problem method
							 	if (methodBinding.problemId() != ProblemReasons.NotVisible && methodBinding.problemId() != ProblemReasons.NotFound)
							 		return methodBinding; // return the error now
							 	if (foundProblem == null) {
							 		foundProblem = methodBinding; // hold onto the first not visible/found error and keep the second not found if first is not visible
							 	}
							 	if (! foundProblemVisible && methodBinding.problemId() == ProblemReasons.NotFound) {
							 		MethodBinding closestMatch = ((ProblemMethodBinding) methodBinding).closestMatch;
							 		if (closestMatch != null && closestMatch.canBeSeenBy(receiverType, invocationSite, this)) {
							 			foundProblem = methodBinding; // hold onto the first not visible/found error and keep the second not found if first is not visible
							 			foundProblemVisible = true;
							 		}
							 	}
							 }
						}
					}
					insideTypeAnnotation = false;
					depth++;
					insideStaticContext |= receiverType.isStatic();
					// 1EX5I8Z - accessing outer fields within a constructor call is permitted
					// in order to do so, we change the flag as we exit from the type, not the method
					// itself, because the class scope is used to retrieve the fields.
					MethodScope enclosingMethodScope = scope.methodScope();
					insideConstructorCall = enclosingMethodScope == null ? false : enclosingMethodScope.isConstructorCall;
					break;
				case COMPILATION_UNIT_SCOPE :
					break done;
			}
			scope = scope.parent;
		}

		MethodBinding foundMethod = null;

		Map<MethodBinding,ReferenceBinding> method2sourceDeclaring = new HashMap<>();
		if (insideStaticContext) {
			if (foundProblem != null) {
				if (foundProblem.declaringClass != null && foundProblem.declaringClass.id == TypeIds.T_JavaLangObject)
					return foundProblem; // static imports lose to methods from Object
				if (foundProblem.problemId() == ProblemReasons.NotFound && foundProblemVisible) {
					return foundProblem; // visible method selectors take precedence
				}
			}

			// at this point the scope is a compilation unit scope & need to check for imported static methods
			CompilationUnitScope unitScope = (CompilationUnitScope) scope;
			unitScope.faultInImports(); // field constants can cause static imports to be accessed before they're resolved
			ImportBinding[] imports = unitScope.imports;
			if (imports != null) {
				ObjectVector visible = null;
				boolean skipOnDemand = false; // set to true when matched static import of method name so stop looking for on demand methods
				for (ImportBinding importBinding : imports) {
					if (importBinding.isStatic()) {
						Binding resolvedImport = importBinding.getResolvedImport();
						MethodBinding possible = null;
						if (importBinding.onDemand) {
							if (!skipOnDemand && resolvedImport instanceof ReferenceBinding) {
								// answers closest approximation, may not check argumentTypes or visibility
								ReferenceBinding resolvedImportReferenceBinding = (ReferenceBinding) resolvedImport;
								possible = findMethod(resolvedImportReferenceBinding, selector, argumentTypes, invocationSite, true);
								if (possible != null && possible.isValidBinding())
									method2sourceDeclaring.put(possible, resolvedImportReferenceBinding);
							}
						} else {
							if (resolvedImport instanceof MethodBinding) {
								MethodBinding staticMethod = (MethodBinding) resolvedImport;
								if (CharOperation.equals(staticMethod.selector, selector)) {
									// answers closest approximation, may not check argumentTypes or visibility
									possible = findMethod(staticMethod.declaringClass, selector, argumentTypes, invocationSite, true);
									if (possible != null && possible.isValidBinding()) {
										TypeBinding importedType = getType(importBinding.compoundName, importBinding.compoundName.length-1);
										if (importedType instanceof ReferenceBinding && importedType.isValidBinding())
											method2sourceDeclaring.put(possible, (ReferenceBinding) importedType);
									}
								}
							} else if (resolvedImport instanceof FieldBinding) {
								// check to see if there are also methods with the same name
								FieldBinding staticField = (FieldBinding) resolvedImport;
								if (CharOperation.equals(staticField.name, selector)) {
									// must find the importRef's type again since the field can be from an inherited type
									char[][] importName = importBinding.reference.tokens;
									TypeBinding referencedType = getType(importName, importName.length - 1);
									if (referencedType != null)
										// answers closest approximation, may not check argumentTypes or visibility
										possible = findMethod((ReferenceBinding) referencedType, selector, argumentTypes, invocationSite, true);
								}
							}
						}
						if (possible != null && possible != foundProblem) {
							if (!possible.isValidBinding()) {
								if (foundProblem == null)
									foundProblem = possible; // answer as error case match
							} else if (possible.isStatic()) {
								MethodBinding compatibleMethod = computeCompatibleMethod(possible, argumentTypes, invocationSite);
								if (compatibleMethod != null) {
									if (compatibleMethod.isValidBinding()) {
										method2sourceDeclaring.put(compatibleMethod, method2sourceDeclaring.get(possible));
										if (compatibleMethod.canBeSeenBy(unitScope.fPackage)) {
											if (!skipOnDemand && !importBinding.onDemand) {
												visible = null; // forget previous matches from on demand imports
												skipOnDemand = true;
											}
											if (visible == null || !visible.contains(compatibleMethod)) {
												ImportReference importReference = importBinding.reference;
												if (importReference != null) {
													importReference.bits |= ASTNode.Used;
												}
												if (visible == null)
													visible = new ObjectVector(3);
												visible.add(compatibleMethod);
											}
										} else if (foundProblem == null) {
											foundProblem = new ProblemMethodBinding(compatibleMethod, selector, compatibleMethod.parameters, ProblemReasons.NotVisible);
										}
									} else if (foundProblem == null) {
										foundProblem = compatibleMethod;
									}
								} else if (foundProblem == null) {
									foundProblem = new ProblemMethodBinding(possible, selector, argumentTypes, ProblemReasons.NotFound);
								}
							}
						}
					}
				}
				if (visible != null) {
					if (visible.size == 1) {
						foundMethod = (MethodBinding) visible.elementAt(0);
					} else {
						MethodBinding[] temp = new MethodBinding[visible.size];
						visible.copyInto(temp);
						foundMethod = mostSpecificMethodBinding(temp, temp.length, argumentTypes, invocationSite, null);
					}
				}
			}
		}

		if (foundMethod != null) {
			ReferenceBinding sourceDeclaring = method2sourceDeclaring.get(foundMethod);
			if (sourceDeclaring != null) {
				invocationSite.setActualReceiverType(sourceDeclaring);
			} else {
				invocationSite.setActualReceiverType(foundMethod.declaringClass);
			}
			return foundMethod;
		}
		if (foundProblem != null)
			return foundProblem;

		return new ProblemMethodBinding(selector, argumentTypes, ProblemReasons.NotFound);
	}

	boolean lacksRequiredInstanceScope(MethodBinding methodBinding, boolean insideConstructorCall, boolean insideStaticContext) {
		if (methodBinding.isStatic())
			return false; // target instance not needed
		if (!insideConstructorCall && !insideStaticContext)
			return false; // target instance available
		if (insideConstructorCall && JavaFeature.FLEXIBLE_CONSTRUCTOR_BODIES.isSupported(compilerOptions())) {
			if (!isInsideEarlyConstructionContext(methodBinding.declaringClass, false))
				return false; // this case is excused
		}
		return true;
	}

	public final ReferenceBinding getJavaIoSerializable() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_IO_SERIALIZABLE);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_IO_SERIALIZABLE, this);
	}

	public final ReferenceBinding getJavaLangAnnotationAnnotation() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_ANNOTATION_ANNOTATION);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_ANNOTATION_ANNOTATION, this);
	}

	public final ReferenceBinding getJavaLangAssertionError() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_ASSERTIONERROR);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_ASSERTIONERROR, this);
	}

	public final ReferenceBinding getJavaLangBoolean() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_BOOLEAN);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_BOOLEAN, this);
	}
	public final ReferenceBinding getJavaLangByte() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_BYTE);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_BYTE, this);
	}
	public final ReferenceBinding getJavaLangCharacter() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_CHARACTER);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_CHARACTER, this);
	}
	public final ReferenceBinding getJavaLangClass() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_CLASS);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_CLASS, this);
	}

	public final ReferenceBinding getJavaLangCloneable() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_CLONEABLE);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_CLONEABLE, this);
	}
	public final ReferenceBinding getJavaLangClassNotFoundException() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_CLASSNOTFOUNDEXCEPTION);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_CLASSNOTFOUNDEXCEPTION, this);
	}
	public final ReferenceBinding getJavaLangDouble() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_DOUBLE);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_DOUBLE, this);
	}
	public final ReferenceBinding getJavaLangFloat() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_FLOAT);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_FLOAT, this);
	}
	public final ReferenceBinding getJavaLangIncompatibleClassChangeError() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_INCOMPATIBLECLASSCHANGEERROR);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_INCOMPATIBLECLASSCHANGEERROR, this);
	}
	public final ReferenceBinding getJavaLangNoClassDefFoundError() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_NOCLASSDEFFOUNDERROR);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_NOCLASSDEFFOUNDERROR, this);
	}
	public final ReferenceBinding getJavaLangNoSuchFieldError() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_NOSUCHFIELDERROR);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_NOSUCHFIELDERROR, this);
	}
	public final ReferenceBinding getJavaLangEnum() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_ENUM);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_ENUM, this);
	}
	public final ReferenceBinding getJavaLangError() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_ERROR);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_ERROR, this);
	}

	public final ReferenceBinding getJavaLangReflectField() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_REFLECT_FIELD);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_REFLECT_FIELD, this);
	}

	public final ReferenceBinding getJavaLangReflectMethod() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_REFLECT_METHOD);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_REFLECT_METHOD, this);
	}

	public final ReferenceBinding getJavaLangRuntimeObjectMethods() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_RUNTIME_OBJECTMETHODS);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_RUNTIME_OBJECTMETHODS, this);
	}
	public final ReferenceBinding getJavaLangRuntimeSwitchBootstraps() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_RUNTIME_SWITCHBOOTSTRAPS);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_RUNTIME_SWITCHBOOTSTRAPS, this);
	}
	public final ReferenceBinding getJavaLangInvokeConstantBootstraps() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_INVOKE_CONSTANTBOOTSTRAP);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_INVOKE_CONSTANTBOOTSTRAP, this);
	}
	public final ReferenceBinding getJavaLangEnumDesc() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_ENUM_ENUMDESC);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_ENUM_ENUMDESC, this);
	}
	public final ReferenceBinding getJavaLangClassDesc() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_CONSTANT_CLASSDESC);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_CONSTANT_CLASSDESC, this);
	}
	public final ReferenceBinding getJavaLangInvokeStringConcatFactory() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_INVOKE_STRING_CONCAT_FACTORY);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_INVOKE_STRING_CONCAT_FACTORY, this);
	}
	public final ReferenceBinding getJavaLangInvokeLambdaMetafactory() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_INVOKE_LAMBDAMETAFACTORY);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_INVOKE_LAMBDAMETAFACTORY, this);
	}

	public final ReferenceBinding getJavaLangInvokeSerializedLambda() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_INVOKE_SERIALIZEDLAMBDA);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_INVOKE_SERIALIZEDLAMBDA, this);
	}

	public final ReferenceBinding getJavaLangInvokeMethodHandlesLookup() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_INVOKE_METHODHANDLES);
		ReferenceBinding outerType = unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_INVOKE_METHODHANDLES, this);
		return findDirectMemberType("Lookup".toCharArray(), outerType); //$NON-NLS-1$
	}

	public final ReferenceBinding getJavaLangInvokeMethodHandle() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_INVOKE_METHODHANDLE);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_INVOKE_METHODHANDLE, this);
	}

	public final ReferenceBinding getJavaLangInvokeVarHandle() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_INVOKE_VARHANDLE);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_INVOKE_VARHANDLE, this);
	}

	public final ReferenceBinding getJavaLangInteger() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_INTEGER);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_INTEGER, this);
	}
	public final ReferenceBinding getJavaLangIterable() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_ITERABLE);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_ITERABLE, this);
	}
	public final ReferenceBinding getJavaLangLong() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_LONG);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_LONG, this);
	}
	public final ReferenceBinding getJavaLangObject() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_OBJECT);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_OBJECT, this);
	}

	public final ReferenceBinding getJavaLangRecord() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_RECORD);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_RECORD, this);
	}

	public final ReferenceBinding getJavaLangShort() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_SHORT);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_SHORT, this);
	}
	public final ReferenceBinding getJavaLangString() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_STRING);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_STRING, this);
	}
	public final ReferenceBinding getJavaLangStringBuffer() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_STRINGBUFFER);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_STRINGBUFFER, this);
	}

	public final ReferenceBinding getJavaLangStringBuilder() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_STRINGBUILDER);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_STRINGBUILDER, this);
	}

	public final ReferenceBinding getJavaLangThrowable() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_THROWABLE);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_THROWABLE, this);
	}

	public final ReferenceBinding getJavaLangVoid() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_VOID);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_VOID, this);
	}
	public final ReferenceBinding getJavaLangIllegalArgumentException() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_LANG_ILLEGALARGUMENTEXCEPTION);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_ILLEGALARGUMENTEXCEPTION, this);
	}

	public final ReferenceBinding getJavaUtilIterator() {
		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(TypeConstants.JAVA_UTIL_ITERATOR);
		return unitScope.environment.getResolvedJavaBaseType(TypeConstants.JAVA_UTIL_ITERATOR, this);
	}

	/* Answer the type binding corresponding to the typeName argument, relative to the enclosingType.
	*/
	public final ReferenceBinding getMemberType(char[] typeName, ReferenceBinding enclosingType) {
		ReferenceBinding memberType = findMemberType(typeName, enclosingType);
		if (enclosingType.isHierarchyBeingConnected() && memberType == null && this.kind == CLASS_SCOPE) {
			ClassScope scope = (ClassScope) this;
			TypeReference superTypeReference = scope.referenceContext.superclass;
			if (superTypeReference != null && superTypeReference.resolvedType instanceof ReferenceBinding) {
				memberType = findMemberType(typeName, (ReferenceBinding) superTypeReference.resolvedType);
			}
			if (memberType == null && scope.referenceContext.superInterfaces != null
					&& scope.referenceContext.superInterfaces.length > 0) {
				TypeReference[] interfaces = scope.referenceContext.superInterfaces;
				for (TypeReference reference : interfaces) {
					if (reference != null && reference.resolvedType instanceof ReferenceBinding) {
						memberType = findMemberType(typeName, (ReferenceBinding) reference.resolvedType);
						if (memberType != null) {
							break;
						}
					}
				}
			}
		}
		if (memberType != null) return memberType;
		char[][] compoundName = new char[][] { typeName };
		return new ProblemReferenceBinding(compoundName, null, ProblemReasons.NotFound);
	}

	public MethodBinding getMethod(TypeBinding receiverType, char[] selector, TypeBinding[] argumentTypes, InvocationSite invocationSite) {
		CompilationUnitScope unitScope = compilationUnitScope();
		LookupEnvironment env = unitScope.environment;
		try {
			env.missingClassFileLocation = invocationSite;
			switch (receiverType.kind()) {
				case Binding.BASE_TYPE :
					return new ProblemMethodBinding(selector, argumentTypes, ProblemReasons.NotFound);
				case Binding.ARRAY_TYPE :
					unitScope.recordTypeReference(receiverType);
					return findMethodForArray((ArrayBinding) receiverType, selector, argumentTypes, invocationSite);
			}
			unitScope.recordTypeReference(receiverType);

			ReferenceBinding currentType = (ReferenceBinding) receiverType;
			if (!currentType.canBeSeenBy(this))
				return new ProblemMethodBinding(selector, argumentTypes, ProblemReasons.ReceiverTypeNotVisible);

			// retrieve an exact visible match (if possible)
			MethodBinding methodBinding = findExactMethod(currentType, selector, argumentTypes, invocationSite);
			if (methodBinding != null && methodBinding.isValidBinding())
				return methodBinding;

			methodBinding = findMethod(currentType, selector, argumentTypes, invocationSite, false);
			if (methodBinding == null)
				return new ProblemMethodBinding(selector, argumentTypes, ProblemReasons.NotFound);
			if (!methodBinding.isValidBinding())
				return methodBinding;

			// special treatment for Object.getClass() in 1.5 mode (substitute parameterized return type)
			if (argumentTypes == Binding.NO_PARAMETERS
				&& CharOperation.equals(selector, TypeConstants.GETCLASS)
				&& methodBinding.returnType.isParameterizedType()/*1.5*/) {
					return environment().createGetClassMethod(receiverType, methodBinding, this);
			}
			return methodBinding;
		} catch (AbortCompilation e) {
			e.updateContext(invocationSite, referenceCompilationUnit().compilationResult);
			throw e;
		} finally {
			env.missingClassFileLocation = null;
		}
	}

	/* Answer the package from the compoundName or null if it begins with a type.
	* Intended to be used while resolving a qualified type name.
	*
	* NOTE: If a problem binding is returned, senders should extract the compound name
	* from the binding & not assume the problem applies to the entire compoundName.
	*/
	public final Binding getPackage(char[][] compoundName) {
 		compilationUnitScope().recordQualifiedReference(compoundName);
		Binding binding = getTypeOrPackage(compoundName[0], Binding.TYPE | Binding.PACKAGE, true);
		if (binding == null) {
			char[][] qName = new char[][] { compoundName[0] };
			return new ProblemReferenceBinding(qName, environment().createMissingType(null, compoundName), ProblemReasons.NotFound);
		}
		if (!binding.isValidBinding()) {
			if (binding instanceof PackageBinding) { /* missing package */
				char[][] qName = new char[][] { compoundName[0] };
				return new ProblemReferenceBinding(qName, null /* no closest match since search for pkg*/, ProblemReasons.NotFound);
			}
			return problemType(compoundName, -1, binding);
		}
		if (!(binding instanceof PackageBinding)) return null; // compoundName does not start with a package

		int currentIndex = 1, length = compoundName.length;
		PackageBinding packageBinding = (PackageBinding) binding;
		while (currentIndex < length) {
			binding = packageBinding.getTypeOrPackage(compoundName[currentIndex++], module(), currentIndex<length);
			if (binding == null) {
				return problemType(compoundName, currentIndex, null);
			}
			if (!binding.isValidBinding() && binding.problemId() != ProblemReasons.Ambiguous)
				return new ProblemReferenceBinding(
					CharOperation.subarray(compoundName, 0, currentIndex),
					binding instanceof ReferenceBinding ? (ReferenceBinding)((ReferenceBinding)binding).closestMatch() : null,
					binding.problemId());
			if (!(binding instanceof PackageBinding))
				return packageBinding;
			packageBinding = (PackageBinding) binding;
		}
		return new ProblemReferenceBinding(compoundName, null /* no closest match since search for pkg*/, ProblemReasons.NotFound);
	}

	/**
	 * Return the most suitable ProblemReferenceBinding:
	 * (1) previousProblem if provided and problem different from NotFound
	 * (2) a new NotAccessible binding
	 * (3) previousProblem if provided otherwise
	 * (4) a new NotFound binding
	 */
	Binding problemType(char[][] compoundName, int currentIndex, Binding previousProblem) {
		if (previousProblem != null && previousProblem.problemId() != ProblemReasons.NotFound)
			return previousProblem;

		LookupEnvironment environment = environment();
		if (environment.useModuleSystem && module() != environment.UnNamedModule) {
			// try if the UnNamedModule can see the type:
			ReferenceBinding notAccessibleType = environment.root.getType(compoundName, environment.UnNamedModule);
			if (notAccessibleType != null && notAccessibleType.isValidBinding())
				return new ProblemReferenceBinding(compoundName, notAccessibleType, ProblemReasons.NotAccessible);
		}
		return previousProblem != null
			? previousProblem
			: new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, currentIndex), null, ProblemReasons.NotFound);
	}

	/* Answer the package from the compoundName or null if it begins with a type.
	* Intended to be used while resolving a package name only.
	*
	* Internal use only
	*/
	public final Binding getOnlyPackage(char[][] compoundName) {
 		compilationUnitScope().recordQualifiedReference(compoundName);
		Binding binding = getTypeOrPackage(compoundName[0], Binding.PACKAGE, true);
		if (binding == null || !binding.isValidBinding()) {
			char[][] qName = new char[][] { compoundName[0] };
			return new ProblemReferenceBinding(qName, null /* no closest match since search for pkg*/, ProblemReasons.NotFound);
		}
		if (!(binding instanceof PackageBinding)) {
			return null; // compoundName does not start with a package
		}

		int currentIndex = 1, length = compoundName.length;
		PackageBinding packageBinding = (PackageBinding) binding;
		while (currentIndex < length) {
			binding = packageBinding.getPackage(compoundName[currentIndex++], module());
			if (binding == null) {
				return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, currentIndex), null /* no closest match since search for pkg*/, ProblemReasons.NotFound);
			}
			if (!binding.isValidBinding()) {
				return new ProblemReferenceBinding(
					CharOperation.subarray(compoundName, 0, currentIndex),
					binding instanceof ReferenceBinding ? (ReferenceBinding)((ReferenceBinding)binding).closestMatch() : null,
					binding.problemId());
			}
			packageBinding = (PackageBinding) binding;
		}
		return packageBinding;
	}

	/* Answer the type binding that corresponds the given name, starting the lookup in the receiver.
	* The name provided is a simple source name (e.g., "Object" , "Point", ...)
	*/
	// The return type of this method could be ReferenceBinding if we did not answer base types.
	// NOTE: We could support looking for Base Types last in the search, however any code using
	// this feature would be extraordinarily slow. Therefore we don't do this.
	public final TypeBinding getType(char[] name) {
		// Would like to remove this test and require senders to specially handle base types
		TypeBinding binding = getBaseType(name);
		if (binding != null) return binding;
		return (ReferenceBinding) getTypeOrPackage(name, Binding.TYPE, true);
	}

	/* Answer the type binding that corresponds to the given name, starting the lookup in the receiver
	* or the packageBinding if provided.
	* The name provided is a simple source name (e.g., "Object" , "Point", ...)
	*/
	public final TypeBinding getType(char[] name, PackageBinding packageBinding) {
		if (packageBinding == null)
			return getType(name);

		Binding binding = packageBinding.getTypeOrPackage(name, module(), false);
		if (binding == null) {
			return new ProblemReferenceBinding(
				CharOperation.arrayConcat(packageBinding.compoundName, name),
				null,
				ProblemReasons.NotFound);
		}
		if (!binding.isValidBinding()) {
				return new ProblemReferenceBinding(
						binding instanceof ReferenceBinding ? ((ReferenceBinding)binding).compoundName : CharOperation.arrayConcat(packageBinding.compoundName, name),
						binding instanceof ReferenceBinding ? (ReferenceBinding)((ReferenceBinding)binding).closestMatch() : null,
						binding.problemId());
		}
		ReferenceBinding typeBinding = (ReferenceBinding) binding;
		if (!typeBinding.canBeSeenBy(this))
			return new ProblemReferenceBinding(
				typeBinding.compoundName,
				typeBinding,
				ProblemReasons.NotVisible);
		return typeBinding;
	}

	/* Answer the type binding corresponding to the compoundName.
	*
	* NOTE: If a problem binding is returned, senders should extract the compound name
	* from the binding & not assume the problem applies to the entire compoundName.
	*/
	public final TypeBinding getType(char[][] compoundName, int typeNameLength) {
		if (typeNameLength == 1) {
			// Would like to remove this test and require senders to specially handle base types
			TypeBinding binding = getBaseType(compoundName[0]);
			if (binding != null) return binding;
		}

		CompilationUnitScope unitScope = compilationUnitScope();
		unitScope.recordQualifiedReference(compoundName);
		Binding binding = getTypeOrPackage(compoundName[0], typeNameLength == 1 ? Binding.TYPE : Binding.TYPE | Binding.PACKAGE, true);
		if (binding == null) {
			char[][] qName = new char[][] { compoundName[0] };
			return new ProblemReferenceBinding(qName, environment().createMissingType(compilationUnitScope().getCurrentPackage(), qName), ProblemReasons.NotFound);
		}
		if (!binding.isValidBinding()) {
			if (binding instanceof PackageBinding) {
				char[][] qName = new char[][] { compoundName[0] };
				return new ProblemReferenceBinding(
						qName,
						environment().createMissingType(null, qName),
						ProblemReasons.NotFound);
			}
			return (ReferenceBinding) binding;
		}
		int currentIndex = 1;
		boolean checkVisibility = false;
		if (binding instanceof PackageBinding) {
			PackageBinding packageBinding = (PackageBinding) binding;
			while (currentIndex < typeNameLength) {
				binding = packageBinding.getTypeOrPackage(compoundName[currentIndex++], module(), currentIndex<typeNameLength); // does not check visibility
				if (binding == null) {
					char[][] qName = CharOperation.subarray(compoundName, 0, currentIndex);
					return new ProblemReferenceBinding(
						qName,
						environment().createMissingType(packageBinding, qName),
						ProblemReasons.NotFound);
				}
				if (!binding.isValidBinding())
					return new ProblemReferenceBinding(
						CharOperation.subarray(compoundName, 0, currentIndex),
						binding instanceof ReferenceBinding ? (ReferenceBinding)((ReferenceBinding)binding).closestMatch() : null,
						binding.problemId());
				if (!(binding instanceof PackageBinding))
					break;
				packageBinding = (PackageBinding) binding;
			}
			if (binding instanceof PackageBinding) {
				char[][] qName = CharOperation.subarray(compoundName, 0, currentIndex);
				return new ProblemReferenceBinding(
					qName,
					environment().createMissingType(null, qName),
					ProblemReasons.NotFound);
			}
			checkVisibility = true;
		}

		// binding is now a ReferenceBinding
		ReferenceBinding typeBinding = (ReferenceBinding) binding;
		unitScope.recordTypeReference(typeBinding);
		if (checkVisibility) // handles the fall through case
			if (!typeBinding.canBeSeenBy(this))
				return new ProblemReferenceBinding(
					CharOperation.subarray(compoundName, 0, currentIndex),
					typeBinding,
					ProblemReasons.NotVisible);

		while (currentIndex < typeNameLength) {
			typeBinding = getMemberType(compoundName[currentIndex++], typeBinding);
			if (!typeBinding.isValidBinding()) {
				if (typeBinding instanceof ProblemReferenceBinding) {
					ProblemReferenceBinding problemBinding = (ProblemReferenceBinding) typeBinding;
					return new ProblemReferenceBinding(
						CharOperation.subarray(compoundName, 0, currentIndex),
						problemBinding.closestReferenceMatch(),
						typeBinding.problemId());
				}
				return new ProblemReferenceBinding(
					CharOperation.subarray(compoundName, 0, currentIndex),
					(ReferenceBinding)((ReferenceBinding)binding).closestMatch(),
					typeBinding.problemId());
			}
		}
		return typeBinding;
	}

	/* Internal use only
	*/
	final Binding getTypeOrPackage(char[] name, int mask, boolean needResolve) {
		Scope scope = this;
		MethodScope methodScope = null;
		ReferenceBinding foundType = null;
		boolean insideStaticContext = false;
		boolean insideClassContext = false;
		boolean insideTypeAnnotation = false;
		if ((mask & Binding.TYPE) == 0) {
			scope = this.compilationUnitScope;
		} else {
			done : while (true) { // done when a COMPILATION_UNIT_SCOPE is found
				switch (scope.kind) {
					case METHOD_SCOPE :
						methodScope = (MethodScope) scope;
						AbstractMethodDeclaration methodDecl = methodScope.referenceMethod();
						if (methodDecl != null) {
							if (methodDecl.binding != null) {
								TypeVariableBinding typeVariable = methodDecl.binding.getTypeVariable(name);
								if (typeVariable != null) {
									if (insideStaticContext && insideClassContext) {
										return new ProblemReferenceBinding(new char[][]{name}, typeVariable, ProblemReasons.NonStaticReferenceInStaticContext);
									}
									return typeVariable;
								}
							} else {
								// use the methodDecl's typeParameters to handle problem cases when the method binding doesn't exist
								TypeParameter[] params = methodDecl.typeParameters();
								for (int i = params == null ? 0 : params.length; --i >= 0;)
									if (CharOperation.equals(params[i].name, name))
										if (params[i].binding != null && params[i].binding.isValidBinding())
											return params[i].binding;
							}
						}
						insideStaticContext |= methodScope.isStatic;
						insideTypeAnnotation = methodScope.insideTypeAnnotation;
						//$FALL-THROUGH$
					case BLOCK_SCOPE :
						ReferenceBinding localType = ((BlockScope) scope).findLocalType(name); // looks in this scope only
						if (localType != null) {
							if (foundType != null && TypeBinding.notEquals(foundType, localType))
								return new ProblemReferenceBinding(new char[][]{name}, foundType, ProblemReasons.InheritedNameHidesEnclosingName);
							return localType;
						}
						break;
					case CLASS_SCOPE :
						SourceTypeBinding sourceType = ((ClassScope) scope).referenceContext.binding;
						if (scope == this && (sourceType.tagBits & TagBits.TypeVariablesAreConnected) == 0) {
							// type variables take precedence over the source type, ex. class X <X> extends X == class X <Y> extends Y
							// but not when we step out to the enclosing type
							TypeVariableBinding typeVariable = sourceType.getTypeVariable(name);
							if (typeVariable != null)
								return typeVariable;
							if (CharOperation.equals(name, sourceType.sourceName))
								return sourceType;
							insideStaticContext |= sourceType.isStatic();
							break;
						}
						if (scope == this && (sourceType.tagBits & TagBits.SealingTypeHierarchy) != 0) {
							insideStaticContext |= sourceType.isStatic();
							break; // while resolving permits we should not consider member types not in scope
						}
						// member types take precedence over type variables
						if (!insideTypeAnnotation) {
							// 6.5.5.1 - member types have precedence over top-level type in same unit
							ReferenceBinding memberType = findMemberType(name, sourceType);
							if (memberType != null) { // skip it if we did not find anything
								if (memberType.problemId() == ProblemReasons.Ambiguous) {
									if (foundType == null || foundType.problemId() == ProblemReasons.NotVisible)
										// supercedes any potential InheritedNameHidesEnclosingName problem
										return memberType;
									// make the user qualify the type, likely wants the first inherited type
									return new ProblemReferenceBinding(new char[][]{name}, foundType, ProblemReasons.InheritedNameHidesEnclosingName);
								}
								if (memberType.isValidBinding()) {
									if (insideStaticContext && !memberType.isStatic() && sourceType.isGenericType())
										return new ProblemReferenceBinding(new char[][]{name}, memberType, ProblemReasons.NonStaticReferenceInStaticContext);
									// found a valid type in the 'immediate' scope (i.e. not inherited)
									// OR in 1.4 mode (inherited visible shadows enclosing)
									if (foundType == null || (foundType.problemId() == ProblemReasons.NotVisible))
										return memberType;
									// if a valid type was found, complain when another is found in an 'immediate' enclosing type (i.e. not inherited)
									if (foundType.isValidBinding() && TypeBinding.notEquals(foundType, memberType))
										return new ProblemReferenceBinding(new char[][]{name}, foundType, ProblemReasons.InheritedNameHidesEnclosingName);
								}
								if (foundType == null || (foundType.problemId() == ProblemReasons.NotVisible && memberType.problemId() != ProblemReasons.NotVisible))
									// only remember the memberType if its the first one found or the previous one was not visible & memberType is...
									foundType = memberType;
							}
						}
						TypeVariableBinding typeVariable = sourceType.getTypeVariable(name);
						if (typeVariable != null) {
							if (insideStaticContext) // do not consider this type modifiers: access is legite within same type
								return new ProblemReferenceBinding(new char[][]{name}, typeVariable, ProblemReasons.NonStaticReferenceInStaticContext);
							return typeVariable;
						}
						insideStaticContext |= sourceType.isStatic();
						insideClassContext = !sourceType.isAnonymousType();
						insideTypeAnnotation = false;
						if (CharOperation.equals(sourceType.sourceName, name)) {
							if (sourceType.isImplicit) {
								char[][] compoundName = new char[][] { name };
								return new ProblemReferenceBinding(compoundName, null, ProblemReasons.NotFound);
							}
							if (foundType != null && TypeBinding.notEquals(foundType, sourceType) && foundType.problemId() != ProblemReasons.NotVisible)
								return new ProblemReferenceBinding(new char[][]{name}, foundType, ProblemReasons.InheritedNameHidesEnclosingName);
							return sourceType;
						}
						break;
					case COMPILATION_UNIT_SCOPE :
						break done;
				}
				scope = scope.parent;
			}
			if (foundType != null && foundType.problemId() != ProblemReasons.NotVisible)
				return foundType;
		}

		// at this point the scope is a compilation unit scope
		CompilationUnitScope unitScope = (CompilationUnitScope) scope;
		HashtableOfObject typeOrPackageCache = unitScope.typeOrPackageCache;
		if (typeOrPackageCache != null) {
			Binding cachedBinding = (Binding) typeOrPackageCache.get(name);
			if (cachedBinding != null) { // can also include NotFound ProblemReferenceBindings if we already know this name is not found
				if (cachedBinding instanceof ImportBinding) { // single type import cached in faultInImports(), replace it in the cache with the type
					ImportBinding importBinding = (ImportBinding) cachedBinding;
					ImportReference importReference = importBinding.reference;
					Binding resolvedImport = importBinding.getResolvedImport();
					if (importReference != null && !isUnnecessarySamePackageImport(resolvedImport, unitScope)) {
						importReference.bits |= ASTNode.Used;
					}
					if (cachedBinding instanceof ImportConflictBinding)
						typeOrPackageCache.put(name, cachedBinding = ((ImportConflictBinding) cachedBinding).conflictingTypeBinding); // already know its visible
					else
						typeOrPackageCache.put(name, cachedBinding = resolvedImport); // already know its visible
				}
				if ((mask & Binding.TYPE) != 0) {
					if (foundType != null && foundType.problemId() != ProblemReasons.NotVisible && cachedBinding.problemId() != ProblemReasons.Ambiguous)
						return foundType; // problem type from above supercedes NotFound type but not Ambiguous import case
					if (cachedBinding instanceof ReferenceBinding)
						return cachedBinding; // cached type found in previous walk below
				}
				if ((mask & Binding.PACKAGE) != 0 && cachedBinding instanceof PackageBinding)
					return cachedBinding; // cached package found in previous walk below
			}
		}

		// ask for the imports + name
		if ((mask & Binding.TYPE) != 0) {
			ImportBinding[] imports = unitScope.imports;
			if (imports != null && typeOrPackageCache == null) { // walk single type imports since faultInImports() has not run yet
				nextImport : for (ImportBinding importBinding : imports) {
					if (!importBinding.onDemand) {
						if (CharOperation.equals(importBinding.getSimpleName(), name)) {
							Binding resolvedImport = unitScope.resolveSingleImport(importBinding, Binding.TYPE);
							if (resolvedImport == null) continue nextImport;
							if (resolvedImport instanceof TypeBinding) {
								ImportReference importReference = importBinding.reference;
								if (importReference != null && !isUnnecessarySamePackageImport(importBinding.getResolvedImport(), unitScope))
									importReference.bits |= ASTNode.Used;
								return resolvedImport; // already know its visible
							}
						}
					}
				}
			}
			// In this location we had a fix for
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318401
			// However, as of today (4.3M6 candidate) this fix seems unnecessary, while causing StackOverflowError in
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401271

			// check if the name is in the current package, skip it if its a sub-package
			PackageBinding currentPackage = unitScope.fPackage;
			unitScope.recordReference(currentPackage.compoundName, name);
			Binding binding = currentPackage.getTypeOrPackage(name, module(), false);
			if (binding instanceof ReferenceBinding) {
				ReferenceBinding referenceType = (ReferenceBinding) binding;
				if ((referenceType.tagBits & TagBits.HasMissingType) == 0) {
					if (typeOrPackageCache != null)
						typeOrPackageCache.put(name, referenceType);
					return referenceType; // type is always visible to its own package
				}
			}

			// check on demand imports
			if (imports != null) {
				ReferenceBinding type = findTypeInOnDemandImports(imports, currentPackage, name, false, typeOrPackageCache);
				if (type == null) { // module imports would otherwise be shadowed
					type = findTypeInOnDemandImports(imports, currentPackage, name, true/*module imports*/, typeOrPackageCache);
				}
				if (type != null && type.isValidBinding()) {
					if (typeOrPackageCache != null)
						typeOrPackageCache.put(name, type);
					return type;
				}
				if (foundType == null)
					foundType = type;
			}
		}

		unitScope.recordSimpleReference(name);
		if ((mask & Binding.PACKAGE) != 0) {
			PackageBinding packageBinding = unitScope.environment.getTopLevelPackage(name);
			if (packageBinding != null && (packageBinding.tagBits & TagBits.HasMissingType) == 0) {
				if (typeOrPackageCache != null)
					typeOrPackageCache.put(name, packageBinding);
				return packageBinding;
			}
		}

		// Answer error binding -- could not find name
		if (foundType == null) {
			char[][] qName = new char[][] { name };
			ReferenceBinding closestMatch = null;
			if ((mask & Binding.PACKAGE) != 0) {
				if (needResolve) {
					closestMatch = environment().createMissingType(unitScope.fPackage, qName);
				}
			} else {
				PackageBinding packageBinding = unitScope.environment.getTopLevelPackage(name);
				if (packageBinding == null || !packageBinding.isValidBinding()) {
					if (needResolve) {
						closestMatch = environment().createMissingType(unitScope.fPackage, qName);
					}
				}
			}
			foundType = new ProblemReferenceBinding(qName, closestMatch, ProblemReasons.NotFound);
			if (typeOrPackageCache != null && (mask & Binding.PACKAGE) != 0) { // only put NotFound type in cache if you know its not a package
				typeOrPackageCache.put(name, foundType);
			}
		} else if ((foundType.tagBits & TagBits.HasMissingType) != 0) {
			char[][] qName = new char[][] { name };
			foundType = new ProblemReferenceBinding(qName, foundType, ProblemReasons.NotFound);
			if (typeOrPackageCache != null && (mask & Binding.PACKAGE) != 0) // only put NotFound type in cache if you know its not a package
				typeOrPackageCache.put(name, foundType);
		}
		return foundType;
	}

	private ReferenceBinding findTypeInOnDemandImports(ImportBinding[] imports, PackageBinding currentPackage, char[] name,
				boolean inModules, HashtableOfObject typeOrPackageCache) {
		// this method is run in two iterations in order to let traditional imports shadow module imports
		boolean foundInImport = false;
		ReferenceBinding type = null;
		ReferenceBinding problemType = null;
		for (ImportBinding someImport : imports) {
			if (someImport.onDemand) {
				Binding resolvedImport = someImport.getResolvedImport();
				ReferenceBinding temp = null;
				if (inModules) {
					if (resolvedImport instanceof ModuleBinding moduleBinding) {
						temp = findTypeInModule(name, moduleBinding, currentPackage);
					}
				} else {
					if (resolvedImport instanceof PackageBinding packageBinding) {
						temp = findType(name, packageBinding, currentPackage);
					} else if (resolvedImport instanceof ReferenceBinding referenceBinding) {
						if (someImport.isStatic()) {
							// Imports are always resolved in the CU Scope (bug 520874)
							temp = compilationUnitScope().findMemberType(name, referenceBinding); // static imports are allowed to see inherited member types
							if (temp != null && !temp.isStatic())
								temp = null;
						} else {
							temp = compilationUnitScope().findDirectMemberType(name, referenceBinding);
						}
					}
				}
				if (TypeBinding.notEquals(temp, type) && temp != null) {
					if (temp.isValidBinding()) {
						// GROOVY add -- allow for imports expressed in source to override 'default' imports - GRECLIPSE-945
						boolean conflict = true; // do we need to continue checking
						if (this.parent != null && foundInImport) {
							CompilationUnitScope cuScope = compilationUnitScope();
							if (cuScope != null) {
								ReferenceBinding chosenBinding = cuScope.selectBinding(temp, type, (someImport.reference != null));
								if (chosenBinding != null) {
									// The new binding was selected as a valid answer
									conflict = false;
									foundInImport = true;
									type = chosenBinding;
								}
							}
						}
						if (conflict) {
						// GROOVY end
						ImportReference importReference = someImport.reference;
						if (importReference != null) {
							importReference.bits |= ASTNode.Used;
						}
						if (foundInImport) {
							// Answer error binding -- import on demand conflict; name found in two import on demand packages.
							temp = new ProblemReferenceBinding(new char[][]{name}, type, ProblemReasons.Ambiguous);
							if (typeOrPackageCache != null)
								typeOrPackageCache.put(name, temp);
							return temp;
						}
						type = temp;
						foundInImport = true;
						// GROOVY add
						}
						// GROOVY end
					} else if (problemType == null) {
						problemType = temp;
					}
				}
			}
		}
		return type != null ? type : problemType;
	}

	private ReferenceBinding findTypeInModule(char[] name, ModuleBinding moduleBinding, PackageBinding currentPackage) {
		ReferenceBinding type = null;
		for (PackageBinding packageBinding : moduleBinding.getExports()) {
			if (packageBinding.enclosingModule.isPackageExportedTo(packageBinding, module())) {
				ReferenceBinding temp = findType(name, packageBinding, currentPackage);
				if (temp != null && temp.canBeSeenBy(currentPackage)) {// imported only if accessible
					if (type != null) {
						// Answer error binding -- import on demand conflict; name found in two exported packages.
						return new ProblemReferenceBinding(new char[][]{name}, temp, ProblemReasons.Ambiguous);
					}
					type = temp;
				}
			}
		}
		for (ModuleBinding required : moduleBinding.getRequiresTransitive()) {
			ReferenceBinding temp = findTypeInModule(name, required, currentPackage);
			if (temp != null) {
				if (temp.problemId() == ProblemReasons.Ambiguous)
					return temp; // don't look further
				if (temp.canBeSeenBy(currentPackage)) {
					if (type != null) {
						// Answer error binding -- import on demand conflict; name found in two modules.
						return new ProblemReferenceBinding(new char[][]{name}, temp, ProblemReasons.Ambiguous);
					}
					type = temp;
				}
			}
		}
		return type;
	}

	private boolean isUnnecessarySamePackageImport(Binding resolvedImport, Scope unitScope) {
		if (resolvedImport instanceof ReferenceBinding) {
			ReferenceBinding referenceBinding = (ReferenceBinding) resolvedImport;
			if (unitScope.getCurrentPackage() == referenceBinding.getPackage()) {
				if (referenceBinding.isNestedType())
					return false; // importing nested types is still necessary
				return true;
			}
		}
		return false;
	}

	// Added for code assist... NOT Public API
	// DO NOT USE to resolve import references since this method assumes 'A.B' is relative to a single type import of 'p1.A'
	// when it may actually mean the type B in the package A
	// use CompilationUnitScope.getImport(char[][]) instead
	public final Binding getTypeOrPackage(char[][] compoundName) {
		int nameLength = compoundName.length;
		if (nameLength == 1) {
			TypeBinding binding = getBaseType(compoundName[0]);
			if (binding != null) return binding;
		}
		Binding binding = getTypeOrPackage(compoundName[0], Binding.TYPE | Binding.PACKAGE, true);
		if (!binding.isValidBinding()) return binding;

		int currentIndex = 1;
		boolean checkVisibility = false;
		if (binding instanceof PackageBinding) {
			PackageBinding packageBinding = (PackageBinding) binding;

			while (currentIndex < nameLength) {
				binding = packageBinding.getTypeOrPackage(compoundName[currentIndex++], module(), currentIndex<nameLength);
				if (binding == null)
					return new ProblemReferenceBinding(
						CharOperation.subarray(compoundName, 0, currentIndex),
						null,
						ProblemReasons.NotFound);
				if (!binding.isValidBinding())
					return new ProblemReferenceBinding(
						CharOperation.subarray(compoundName, 0, currentIndex),
						binding instanceof ReferenceBinding ? (ReferenceBinding)((ReferenceBinding)binding).closestMatch() : null,
						binding.problemId());
				if (!(binding instanceof PackageBinding))
					break;
				packageBinding = (PackageBinding) binding;
			}
			if (binding instanceof PackageBinding) return binding;
			checkVisibility = true;
		}
		// binding is now a ReferenceBinding
		ReferenceBinding typeBinding = (ReferenceBinding) binding;
		ReferenceBinding qualifiedType = (ReferenceBinding) environment().convertToRawType(typeBinding, false /*do not force conversion of enclosing types*/);

		if (checkVisibility) // handles the fall through case
			if (!typeBinding.canBeSeenBy(this))
				return new ProblemReferenceBinding(
					CharOperation.subarray(compoundName, 0, currentIndex),
					typeBinding,
					ProblemReasons.NotVisible);

		while (currentIndex < nameLength) {
			typeBinding = getMemberType(compoundName[currentIndex++], typeBinding);
			// checks visibility
			if (!typeBinding.isValidBinding())
				return new ProblemReferenceBinding(
					CharOperation.subarray(compoundName, 0, currentIndex),
					(ReferenceBinding)typeBinding.closestMatch(),
					typeBinding.problemId());

			if (typeBinding.isGenericType()) {
				qualifiedType = environment().createRawType(typeBinding, qualifiedType);
			} else {
				qualifiedType = environment().maybeCreateParameterizedType(typeBinding, qualifiedType);
			}
		}
		return qualifiedType;
	}

	public boolean hasErasedCandidatesCollisions(TypeBinding one, TypeBinding two, Map<TypeBinding, Object> invocations, ReferenceBinding type, ASTNode typeRef) {
		invocations.clear();
		TypeBinding[] mecs = minimalErasedCandidates(new TypeBinding[] {one, two}, invocations);
		if (mecs != null) {
			nextCandidate: for (TypeBinding mec : mecs) {
				if (mec == null) continue nextCandidate;
				Object value = invocations.get(mec);
				if (value instanceof TypeBinding[]) {
					TypeBinding[] invalidInvocations = (TypeBinding[]) value;
					if (areSignificantlyDifferent(invalidInvocations[0], invalidInvocations[1])) {
						problemReporter().superinterfacesCollide(invalidInvocations[0].erasure(), typeRef, invalidInvocations[0], invalidInvocations[1]);
						type.tagBits |= TagBits.HierarchyHasProblems;
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean areSignificantlyDifferent(TypeBinding one, TypeBinding two) {
		if (!one.enterRecursiveFunction())
			return true;
		try {
			if (one instanceof ParameterizedTypeBinding ptb1 && two instanceof ParameterizedTypeBinding ptb2) {
				if (TypeBinding.notEquals(ptb1.erasure(), ptb2.erasure()))
					return true;
				return areSignificantlyDifferent(ptb1.arguments, ptb2.arguments);
			} else if (one instanceof WildcardBinding wb1 && two instanceof WildcardBinding wb2) {
				if (wb1.boundKind() != wb2.boundKind())
					return true;
				if (TypeBinding.notEquals(wb1.bound, wb2.bound))
					return true;
				return areSignificantlyDifferent(wb1.otherBounds, wb2.otherBounds);
			}
			return TypeBinding.notEquals(one, two);
		} finally {
			one.exitRecursiveFunction();
		}
	}

	private boolean areSignificantlyDifferent(TypeBinding[] ones, TypeBinding[] twos) {
		if (ones == null || twos == null)
			return ones != twos;
		if (ones.length != twos.length)
			return true;
		for (int i = 0; i < ones.length; i++) {
			if (areSignificantlyDifferent(ones[i], twos[i]))
				return true;
		}
		return false; // no significant difference found
	}

	/**
	 * Returns the immediately enclosing case statement (carried by closest blockScope),
	 */
	public CaseStatement enclosingSwitchLabel() {
		Scope scope = this;
		do {
			if (scope instanceof BlockScope bs)
				return bs.enclosingCase;
			scope = scope.parent;
		} while (scope != null);
		return null;
	}

	public boolean isBoxingCompatibleWith(TypeBinding expressionType, TypeBinding targetType) {
		LookupEnvironment environment = environment();
		if (expressionType.isBaseType() == targetType.isBaseType())
			return false;

		// check if autoboxed type is compatible
		TypeBinding convertedType = environment.computeBoxingType(expressionType);
		return TypeBinding.equalsEquals(convertedType, targetType) || convertedType.isCompatibleWith(targetType, this);
	}

	/* Answer true if the scope is nested inside a given field declaration.
	 * Note: it works as long as the scope.fieldDeclarationIndex is reflecting the field being traversed
	 * e.g. during name resolution.
	*/
	public final boolean isDefinedInField(FieldBinding field) {
		Scope scope = this;
		do {
			if (scope instanceof MethodScope) {
				MethodScope methodScope = (MethodScope) scope;
				if (methodScope.initializedField == field) return true;
			}
			scope = scope.parent;
		} while (scope != null);
		return false;
	}

	/* Answer true if the scope is nested inside a given method declaration
	*/
	public final boolean isDefinedInMethod(MethodBinding method) {
		method = method.original(); // https://bugs.eclipse.org/bugs/show_bug.cgi?id=350738
		Scope scope = this;
		do {
			if (scope instanceof MethodScope) {
				ReferenceContext refContext = ((MethodScope) scope).referenceContext;
				if (refContext instanceof AbstractMethodDeclaration)
					if (((AbstractMethodDeclaration) refContext).binding == method)
						return true;
			}
			scope = scope.parent;
		} while (scope != null);
		return false;
	}

	/* Answer whether the type is defined in the same compilation unit as the receiver
	*/
	public final boolean isDefinedInSameUnit(ReferenceBinding type) {
		// find the outer most enclosing type
		ReferenceBinding enclosingType = type;
		while ((type = enclosingType.enclosingType()) != null)
			enclosingType = type;

		// test that the enclosingType is not part of the compilation unit
		SourceTypeBinding[] topLevelTypes = this.compilationUnitScope.topLevelTypes;
		for (int i = topLevelTypes.length; --i >= 0;)
			if (TypeBinding.equalsEquals(topLevelTypes[i], enclosingType.original()))
				return true;
		return false;
	}

	/* Answer true if this scope and the given type share a outermost enclosing type
	 */
	public final boolean isDefinedInSameEnclosingType(ReferenceBinding type) {
		ClassScope outerMostClassScope = outerMostClassScope();
		if (outerMostClassScope != null && outerMostClassScope.referenceContext != null)
			return TypeBinding.equalsEquals(outerMostClassScope.referenceContext.binding, type.outermostEnclosingType());
		return false;
	}

	/* Answer true if the scope is nested inside a given type declaration
	*/
	public final boolean isDefinedInType(ReferenceBinding type) {
		Scope scope = this;
		do {
			if (scope instanceof ClassScope)
				if (TypeBinding.equalsEquals(((ClassScope) scope).referenceContext.binding, type))
					return true;
			scope = scope.parent;
		} while (scope != null);
		return false;
	}

	/**
	 * Returns true if the scope or one of its parent is associated to a given caseStatement, denoting
	 * being part of a given switch case statement.
	 */
	public boolean isInsideCase(CaseStatement caseStatement) {
		Scope scope = this;
		do {
			switch (scope.kind) {
				case Scope.BLOCK_SCOPE :
					if (((BlockScope) scope).enclosingCase == caseStatement) {
						return true;
					}
			}
			scope = scope.parent;
		} while (scope != null);
		return false;
	}

	public boolean isInsideDeprecatedCode(){
		switch(this.kind){
			case Scope.BLOCK_SCOPE :
			case Scope.METHOD_SCOPE :
				MethodScope methodScope = methodScope();
				if (!methodScope.isInsideInitializer()){
					// check method modifiers to see if deprecated
					ReferenceContext referenceContext = methodScope.referenceContext();
					if (referenceContext instanceof AbstractMethodDeclaration) {
						MethodBinding context = ((AbstractMethodDeclaration) referenceContext).binding;
						if (context != null && context.isDeprecated())
							return true;
					} else if (referenceContext instanceof ModuleDeclaration) {
						ModuleBinding context = ((ModuleDeclaration) referenceContext).binding;
						return context != null && context.isDeprecated();
					}
				} else if (methodScope.initializedField != null && methodScope.initializedField.isDeprecated()) {
					// inside field declaration ? check field modifier to see if deprecated
					return true;
				}
				SourceTypeBinding declaringType = ((BlockScope)this).referenceType().binding;
				if (declaringType != null) {
					declaringType.initializeDeprecatedAnnotationTagBits(); // may not have been resolved until then
					if (declaringType.isDeprecated())
						return true;
				}
				break;
			case Scope.CLASS_SCOPE :
				ReferenceBinding context = ((ClassScope)this).referenceType().binding;
				if (context != null) {
					context.initializeDeprecatedAnnotationTagBits(); // may not have been resolved until then
					if (context.isDeprecated())
						return true;
				}
				break;
			case Scope.COMPILATION_UNIT_SCOPE :
				// consider import as being deprecated if first type is itself deprecated (123522)
				CompilationUnitDeclaration unit = referenceCompilationUnit();
				if (unit.types != null && unit.types.length > 0) {
					SourceTypeBinding type = unit.types[0].binding;
					if (type != null) {
						type.initializeDeprecatedAnnotationTagBits(); // may not have been resolved until then
						if (type.isDeprecated())
							return true;
					}
				}
		}
		if (this.parent != null && !(this.parent instanceof CompilationUnitScope))
			return this.parent.isInsideDeprecatedCode();
		return false;
	}

	public boolean isSubtypeOfRawType(TypeBinding paramType) {
		TypeBinding t = paramType.leafComponentType();
		if (t.isBaseType()) return false;

		ReferenceBinding currentType = (ReferenceBinding) t;
		ReferenceBinding[] interfacesToVisit = null;
		int nextPosition = 0;
		do {
			if (currentType.isRawType()) return true;
			//if (!currentType.isHierarchyConnected()) return true; !! not correct, see bug 460993
			ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
			if (itsInterfaces != null && itsInterfaces != Binding.NO_SUPERINTERFACES) {
				if (interfacesToVisit == null) {
					interfacesToVisit = itsInterfaces;
					nextPosition = interfacesToVisit.length;
				} else {
					int itsLength = itsInterfaces.length;
					if (nextPosition + itsLength >= interfacesToVisit.length)
						System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
					nextInterface : for (int a = 0; a < itsLength; a++) {
						ReferenceBinding next = itsInterfaces[a];
						for (int b = 0; b < nextPosition; b++)
							if (TypeBinding.equalsEquals(next, interfacesToVisit[b])) continue nextInterface;
						interfacesToVisit[nextPosition++] = next;
					}
				}
			}
		} while ((currentType = currentType.superclass()) != null);

		for (int i = 0; i < nextPosition; i++) {
			currentType = interfacesToVisit[i];
			if (currentType.isRawType()) return true;

			ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
			if (itsInterfaces != null && itsInterfaces != Binding.NO_SUPERINTERFACES) {
				int itsLength = itsInterfaces.length;
				if (nextPosition + itsLength >= interfacesToVisit.length)
					System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
				nextInterface : for (int a = 0; a < itsLength; a++) {
					ReferenceBinding next = itsInterfaces[a];
					for (int b = 0; b < nextPosition; b++)
						if (TypeBinding.equalsEquals(next, interfacesToVisit[b])) continue nextInterface;
					interfacesToVisit[nextPosition++] = next;
				}
			}
		}
		return false;
	}

	private TypeBinding leastContainingInvocation(TypeBinding mec, Object invocationData, ArrayList<TypeBinding[]> lubStack) {
		if (invocationData == null) return mec; // no alternate invocation
		if (invocationData instanceof TypeBinding) { // only one invocation, simply return it (array only allocated if more than one)
			return (TypeBinding) invocationData;
		}
		TypeBinding[] invocations = (TypeBinding[]) invocationData;

		// if mec is an array type, intersect invocation leaf component types, then promote back to array
		int dim = mec.dimensions();
		mec = mec.leafComponentType();

		int argLength = mec.typeVariables().length;
		if (argLength == 0) return mec; // should be caught by no invocation check

		// infer proper parameterized type from invocations
		TypeBinding[] bestArguments = new TypeBinding[argLength];
		for (TypeBinding binding : invocations) {
			TypeBinding invocation = binding.leafComponentType();
			switch (invocation.kind()) {
				case Binding.GENERIC_TYPE :
					TypeVariableBinding[] invocationVariables = invocation.typeVariables();
					for (int j = 0; j < argLength; j++) {
						TypeBinding bestArgument = leastContainingTypeArgument(bestArguments[j], invocationVariables[j], (ReferenceBinding) mec, j, new ArrayList<>(lubStack));
						if (bestArgument == null) return null;
						bestArguments[j] = bestArgument;
					}
					break;
				case Binding.PARAMETERIZED_TYPE :
					ParameterizedTypeBinding parameterizedType = (ParameterizedTypeBinding)invocation;
					for (int j = 0; j < argLength; j++) {
						TypeBinding bestArgument = leastContainingTypeArgument(bestArguments[j], parameterizedType.arguments[j], (ReferenceBinding) mec, j, new ArrayList<>(lubStack));
						if (bestArgument == null) return null;
						bestArguments[j] = bestArgument;
					}
					break;
				case Binding.RAW_TYPE :
					return dim == 0 ? invocation : environment().createArrayType(invocation, dim); // raw type is taking precedence
			}
		}
		TypeBinding least = environment().createParameterizedType((ReferenceBinding) mec.erasure(), bestArguments, mec.enclosingType());
		return dim == 0 ? least : environment().createArrayType(least, dim);
	}

	// JLS 15.12.2
	private TypeBinding leastContainingTypeArgument(TypeBinding u, TypeBinding v, ReferenceBinding genericType, int rank, ArrayList<TypeBinding[]> lubStack) {
		if (u == null) return v;
		if (TypeBinding.equalsEquals(u, v)) return u;
		if (v.isWildcard()) {
			WildcardBinding wildV = (WildcardBinding) v;
			if (u.isWildcard()) {
				WildcardBinding wildU = (WildcardBinding) u;
				switch (wildU.boundKind) {
					// ? extends U
					case Wildcard.EXTENDS :
						switch(wildV.boundKind) {
							// ? extends U, ? extends V
							case Wildcard.EXTENDS :
								TypeBinding lub = lowerUpperBound(new TypeBinding[]{wildU.bound,wildV.bound}, lubStack);
								if (lub == null) return null;
								// int is returned to denote cycle detected in lub computation - stop recursion by answering unbound wildcard
								if (TypeBinding.equalsEquals(lub, TypeBinding.INT)) return environment().createWildcard(genericType, rank, null, null /*no extra bound*/, Wildcard.UNBOUND);
								return environment().createWildcard(genericType, rank, lub, null /*no extra bound*/, Wildcard.EXTENDS);
							// ? extends U, ? SUPER V
							case Wildcard.SUPER :
								if (TypeBinding.equalsEquals(wildU.bound, wildV.bound)) return wildU.bound;
								return environment().createWildcard(genericType, rank, null, null /*no extra bound*/, Wildcard.UNBOUND);
						}
						break;
						// ? super U
					case Wildcard.SUPER :
						// ? super U, ? super V
						if (wildU.boundKind == Wildcard.SUPER) {
							TypeBinding[] glb = greaterLowerBound(new TypeBinding[]{wildU.bound,wildV.bound}, this, this.environment());
							if (glb == null) return null;
							return environment().createWildcard(genericType, rank, glb[0], null /*no extra bound*/, Wildcard.SUPER);	// TODO (philippe) need to capture entire bounds
						}
				}
			} else {
				switch (wildV.boundKind) {
					// U, ? extends V
					case Wildcard.EXTENDS :
						TypeBinding lub = lowerUpperBound(new TypeBinding[]{u,wildV.bound}, lubStack);
						if (lub == null) return null;
						// int is returned to denote cycle detected in lub computation - stop recursion by answering unbound wildcard
						if (TypeBinding.equalsEquals(lub, TypeBinding.INT)) return environment().createWildcard(genericType, rank, null, null /*no extra bound*/, Wildcard.UNBOUND);
						return environment().createWildcard(genericType, rank, lub, null /*no extra bound*/, Wildcard.EXTENDS);
					// U, ? super V
					case Wildcard.SUPER :
						TypeBinding[] glb = greaterLowerBound(new TypeBinding[]{u,wildV.bound}, this, this.environment());
						if (glb == null) return null;
						return environment().createWildcard(genericType, rank, glb[0], null /*no extra bound*/, Wildcard.SUPER);	// TODO (philippe) need to capture entire bounds
					case Wildcard.UNBOUND :
				}
			}
		} else if (u.isWildcard()) {
			WildcardBinding wildU = (WildcardBinding) u;
			switch (wildU.boundKind) {
				// U, ? extends V
				case Wildcard.EXTENDS :
					TypeBinding lub = lowerUpperBound(new TypeBinding[]{wildU.bound, v}, lubStack);
					if (lub == null) return null;
					// int is returned to denote cycle detected in lub computation - stop recursion by answering unbound wildcard
					if (TypeBinding.equalsEquals(lub, TypeBinding.INT)) return environment().createWildcard(genericType, rank, null, null /*no extra bound*/, Wildcard.UNBOUND);
					return environment().createWildcard(genericType, rank, lub, null /*no extra bound*/, Wildcard.EXTENDS);
				// U, ? super V
				case Wildcard.SUPER :
					TypeBinding[] glb = greaterLowerBound(new TypeBinding[]{wildU.bound, v}, this, this.environment());
					if (glb == null) return null;
					return environment().createWildcard(genericType, rank, glb[0], null /*no extra bound*/, Wildcard.SUPER); // TODO (philippe) need to capture entire bounds
				case Wildcard.UNBOUND :
			}
		}
		TypeBinding lub = lowerUpperBound(new TypeBinding[]{u,v}, lubStack);
		if (lub == null) return null;
		// int is returned to denote cycle detected in lub computation - stop recursion by answering unbound wildcard
		if (TypeBinding.equalsEquals(lub, TypeBinding.INT)) return environment().createWildcard(genericType, rank, null, null /*no extra bound*/, Wildcard.UNBOUND);
		return environment().createWildcard(genericType, rank, lub, null /*no extra bound*/, Wildcard.EXTENDS);
	}

	// 15.12.2
	/**
	 * Returns VoidBinding if types have no intersection (e.g. 2 unrelated interfaces), or null if
	 * no common supertype (e.g. {@code List<String>} and {@code List<Exception>}), or the intersection type if possible
	 */
	public TypeBinding lowerUpperBound(TypeBinding[] types) {
		int typeLength = types.length;
		if (typeLength == 1) {
			TypeBinding type = types[0];
			return type == null ? TypeBinding.VOID : type;
		}
		return lowerUpperBound(types, new ArrayList<>(1));
	}

	// 15.12.2
	private TypeBinding lowerUpperBound(TypeBinding[] types, ArrayList<TypeBinding[]> lubStack) {

		int typeLength = types.length;
		if (typeLength == 1) {
			TypeBinding type = types[0];
			return type == null ? TypeBinding.VOID : type;
		}
		// cycle detection
		int stackLength = lubStack.size();
		nextLubCheck: for (int i = 0; i < stackLength; i++) {
			TypeBinding[] lubTypes = lubStack.get(i);
			int lubTypeLength = lubTypes.length;
			if (lubTypeLength < typeLength) continue nextLubCheck;
			nextTypeCheck:	for (int j = 0; j < typeLength; j++) {
				TypeBinding type = types[j];
				if (type == null) continue nextTypeCheck; // ignore
				for (int k = 0; k < lubTypeLength; k++) {
					TypeBinding lubType = lubTypes[k];
					if (lubType == null) continue; // ignore
					if (TypeBinding.equalsEquals(lubType, type) || lubType.isEquivalentTo(type)) continue nextTypeCheck; // type found, jump to next one
				}
				continue nextLubCheck; // type not found in current lubTypes
			}
			// all types are included in some lub, cycle detected - stop recursion by answering special value (int)
			return TypeBinding.INT;
		}

		lubStack.add(types);
		Map<TypeBinding, Object> invocations = new HashMap<>(1);
		TypeBinding[] mecs = minimalErasedCandidates(types, invocations);
		if (mecs == null) return null;
		int length = mecs.length;
		if (length == 0) return TypeBinding.VOID;
		int count = 0;
		TypeBinding firstBound = null;
		int commonDim = -1;
		for (int i = 0; i < length; i++) {
			TypeBinding mec = mecs[i];
			if (mec == null) continue;
			mec = leastContainingInvocation(mec, invocations.get(mec), lubStack);
			if (mec == null) return null;
			int dim = mec.dimensions();
			if (commonDim == -1) {
				commonDim = dim;
			} else if (dim != commonDim) { // not all types have same dimension
				return null;
			}
			if (firstBound == null && !mec.leafComponentType().isInterface()) firstBound = mec.leafComponentType();
			mecs[count++] = mec; // recompact them to the front
		}
		switch (count) {
			case 0 : return TypeBinding.VOID;
			case 1 : return mecs[0];
			case 2 :
				// TODO(Stephan) : if null annotations differ, we need to create an intersection type and return.
				if ((commonDim == 0 ? mecs[1].id : mecs[1].leafComponentType().id) == TypeIds.T_JavaLangObject) return mecs[0];
				if ((commonDim == 0 ? mecs[0].id : mecs[0].leafComponentType().id) == TypeIds.T_JavaLangObject) return mecs[1];
		}
		TypeBinding[] otherBounds = new TypeBinding[count - 1];
		int rank = 0;
		for (int i = 0; i < count; i++) {
			TypeBinding mec = commonDim == 0 ? mecs[i] : mecs[i].leafComponentType();
			if (mec.isInterface()) {
				otherBounds[rank++] = mec;
			}
		}
		TypeBinding intersectionType;
		// It _should_ be safe to assume only ReferenceBindings at this point, because
		// - base types are rejected in minimalErasedCandidates
		// - arrays are peeled, different dims are rejected above ("not all types have same dimension")
		ReferenceBinding[] intersectingTypes = new ReferenceBinding[otherBounds.length+1];
		intersectingTypes[0] = (ReferenceBinding) firstBound;
		System.arraycopy(otherBounds, 0, intersectingTypes, 1, otherBounds.length);
		intersectionType = environment().createIntersectionType18(intersectingTypes);
		return commonDim == 0 ? intersectionType : environment().createArrayType(intersectionType, commonDim);
	}

	public final MethodScope methodScope() {
		Scope scope = this;
		do {
			if (scope instanceof MethodScope)
				return (MethodScope) scope;
			scope = scope.parent;
		} while (scope != null);
		return null;
	}

	public final MethodScope namedMethodScope() {
		Scope scope = this;
		do {
			if (scope instanceof MethodScope && !scope.isLambdaScope())
				return (MethodScope) scope;
			scope = scope.parent;
		} while (scope != null);
		return null;
	}

	/**
	 * Returns the most specific set of types compatible with all given types.
	 * (i.e. most specific common super types)
	 * If no types is given, will return an empty array. If not compatible
	 * reference type is found, returns null. In other cases, will return an array
	 * of minimal erased types, where some nulls may appear (and must simply be
	 * ignored).
	 */
	protected TypeBinding[] minimalErasedCandidates(TypeBinding[] types, Map<TypeBinding, Object> allInvocations) {
		int length = types.length;
		int indexOfFirst = -1, actualLength = 0;
		for (int i = 0; i < length; i++) {
			TypeBinding type = types[i];
			if (type == TypeBinding.NULL)
				types[i] = type = null; // completely ignore null-type now and further down
			if (type == null) continue;
			if (type.isBaseType()) return null;
			if (indexOfFirst < 0) indexOfFirst = i;
			actualLength ++;
		}
		switch (actualLength) {
			case 0: return Binding.NO_TYPES;
			case 1: return types;
		}
		TypeBinding firstType = types[indexOfFirst];
		if (firstType.isBaseType()) return null;

		// record all supertypes of type
		// intersect with all supertypes of otherType
		List<TypeBinding> typesToVisit = new ArrayList<>();

		int dim = firstType.dimensions();
		TypeBinding leafType = firstType.leafComponentType();
	    // do not allow type variables/intersection types to match with erasures for free
		TypeBinding firstErasure;
		switch(leafType.kind()) {
			case Binding.PARAMETERIZED_TYPE :
			case Binding.RAW_TYPE :
			case Binding.ARRAY_TYPE :
				firstErasure = firstType.erasure();
				break;
			default :
				firstErasure = firstType;
				break;
		}
		if (TypeBinding.notEquals(firstErasure, firstType)) {
			allInvocations.put(firstErasure, firstType);
		}
		typesToVisit.add(firstType);
		int max = 1;
		ReferenceBinding currentType;
		for (int i = 0; i < max; i++) {
			TypeBinding typeToVisit = typesToVisit.get(i);
			dim = typeToVisit.dimensions();
			if (dim > 0) {
				leafType = typeToVisit.leafComponentType();
				switch(leafType.id) {
					case TypeIds.T_JavaLangObject:
						if (dim > 1) { // Object[][] supertype is Object[]
							TypeBinding elementType = ((ArrayBinding)typeToVisit).elementsType();
							if (!typesToVisit.contains(elementType)) {
								typesToVisit.add(elementType);
								max++;
							}
							continue;
						}
						//$FALL-THROUGH$
					case TypeIds.T_byte:
					case TypeIds.T_short:
					case TypeIds.T_char:
					case TypeIds.T_boolean:
					case TypeIds.T_int:
					case TypeIds.T_long:
					case TypeIds.T_float:
					case TypeIds.T_double:
						TypeBinding superType = getJavaIoSerializable();
						if (!typesToVisit.contains(superType)) {
							typesToVisit.add(superType);
							max++;
						}
						superType = getJavaLangCloneable();
						if (!typesToVisit.contains(superType)) {
							typesToVisit.add(superType);
							max++;
						}
						superType = getJavaLangObject();
						if (!typesToVisit.contains(superType)) {
							typesToVisit.add(superType);
							max++;
						}
						continue;

					default:
				}
				typeToVisit = leafType;
			}
			currentType = (ReferenceBinding) typeToVisit;
			if (currentType.isCapture()) {
				TypeBinding firstBound = ((CaptureBinding) currentType).firstBound;
				if (firstBound != null && firstBound.isArrayType()) {
					TypeBinding superType = dim == 0 ? firstBound : (TypeBinding)environment().createArrayType(firstBound, dim); // recreate array if needed
					if (!typesToVisit.contains(superType)) {
						typesToVisit.add(superType);
						max++;
						TypeBinding superTypeErasure = (firstBound.isTypeVariable() || firstBound.isWildcard() /*&& !itsInterface.isCapture()*/) ? superType : superType.erasure();
						if (TypeBinding.notEquals(superTypeErasure, superType)) {
							allInvocations.put(superTypeErasure, superType);
						}
					}
					continue;
				}
			}
			// inject super interfaces prior to superclass
			ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
			if (itsInterfaces != null) { // can be null during code assist operations that use LookupEnvironment.completeTypeBindings(parsedUnit, buildFieldsAndMethods)
				for (ReferenceBinding itsInterface : itsInterfaces) {
					TypeBinding superType = dim == 0 ? itsInterface : (TypeBinding)environment().createArrayType(itsInterface, dim); // recreate array if needed
					if (!typesToVisit.contains(superType)) {
						typesToVisit.add(superType);
						max++;
						TypeBinding superTypeErasure = (itsInterface.isTypeVariable() || itsInterface.isWildcard() /*&& !itsInterface.isCapture()*/) ? superType : superType.erasure();
						if (TypeBinding.notEquals(superTypeErasure, superType)) {
							allInvocations.put(superTypeErasure, superType);
						}
					}
				}
			}
			TypeBinding itsSuperclass = currentType.superclass();
			if (itsSuperclass != null) {
				TypeBinding superType = dim == 0 ? itsSuperclass : (TypeBinding)environment().createArrayType(itsSuperclass, dim); // recreate array if needed
				if (!typesToVisit.contains(superType)) {
					typesToVisit.add(superType);
					max++;
					TypeBinding superTypeErasure = (itsSuperclass.isTypeVariable() || itsSuperclass.isWildcard() /*&& !itsSuperclass.isCapture()*/) ? superType : superType.erasure();
					if (TypeBinding.notEquals(superTypeErasure, superType)) {
						allInvocations.put(superTypeErasure, superType);
					}
				}
			}
		}
		int superLength = typesToVisit.size();
		TypeBinding[] erasedSuperTypes = new TypeBinding[superLength];
		int rank = 0;
		for (Object typeToVisit : typesToVisit) {
			TypeBinding type = (TypeBinding)typeToVisit;
			leafType = type.leafComponentType();
			erasedSuperTypes[rank++] = (leafType.isTypeVariable() || leafType.isWildcard() /*&& !leafType.isCapture()*/) ? type : type.erasure();
		}
		// intersecting first type supertypes with other types' ones, nullifying non matching supertypes
		int remaining = superLength;
		nextOtherType: for (int i = indexOfFirst+1; i < length; i++) {
			TypeBinding otherType = types[i];
			if (otherType == null) continue nextOtherType;
			if (otherType.isArrayType()) {
				nextSuperType: for (int j = 0; j < superLength; j++) {
					TypeBinding erasedSuperType = erasedSuperTypes[j];
					if (erasedSuperType == null || TypeBinding.equalsEquals(erasedSuperType, otherType)) continue nextSuperType;
					TypeBinding match;
					if ((match = otherType.findSuperTypeOriginatingFrom(erasedSuperType)) == null) {
						erasedSuperTypes[j] = null;
						if (--remaining == 0) return null;
						continue nextSuperType;
					}
					// record invocation
					Object invocationData = allInvocations.get(erasedSuperType);
					if (invocationData == null) {
						allInvocations.put(erasedSuperType, match); // no array for singleton
					} else if (invocationData instanceof TypeBinding) {
						if (TypeBinding.notEquals(match, (TypeBinding) invocationData)) {
							// using an array to record invocations in order (188103)
							TypeBinding[] someInvocations = { (TypeBinding) invocationData, match, };
							allInvocations.put(erasedSuperType, someInvocations);
						}
					} else { // using an array to record invocations in order (188103)
						TypeBinding[] someInvocations = (TypeBinding[]) invocationData;
						checkExisting: {
							int invocLength = someInvocations.length;
							for (int k = 0; k < invocLength; k++) {
								if (TypeBinding.equalsEquals(someInvocations[k], match)) break checkExisting;
							}
							System.arraycopy(someInvocations, 0, someInvocations = new TypeBinding[invocLength+1], 0, invocLength);
							allInvocations.put(erasedSuperType, someInvocations);
							someInvocations[invocLength] = match;
						}
					}
				}
				continue nextOtherType;
			}
			nextSuperType: for (int j = 0; j < superLength; j++) {
				TypeBinding erasedSuperType = erasedSuperTypes[j];
				if (erasedSuperType == null) continue nextSuperType;
				TypeBinding match;
				if (TypeBinding.equalsEquals(erasedSuperType, otherType) || erasedSuperType.id == TypeIds.T_JavaLangObject && otherType.isInterface()) {
					match = erasedSuperType;
				} else {
					if (erasedSuperType.isArrayType()) {
						match = null;
					} else {
						match = otherType.findSuperTypeOriginatingFrom(erasedSuperType);
					}
					if (match == null) { // incompatible super type
						erasedSuperTypes[j] = null;
						if (--remaining == 0) return null;
						continue nextSuperType;
					}
				}
				// record invocation
				Object invocationData = allInvocations.get(erasedSuperType);
				if (invocationData == null) {
					allInvocations.put(erasedSuperType, match); // no array for singleton
				} else if (invocationData instanceof TypeBinding) {
					if (TypeBinding.notEquals(match, (TypeBinding) invocationData)) {
						// using an array to record invocations in order (188103)
						TypeBinding[] someInvocations = { (TypeBinding) invocationData, match, };
						allInvocations.put(erasedSuperType, someInvocations);
					}
				} else { // using an array to record invocations in order (188103)
					TypeBinding[] someInvocations = (TypeBinding[]) invocationData;
					checkExisting: {
						int invocLength = someInvocations.length;
						for (int k = 0; k < invocLength; k++) {
							if (TypeBinding.equalsEquals(someInvocations[k], match)) break checkExisting;
						}
						System.arraycopy(someInvocations, 0, someInvocations = new TypeBinding[invocLength+1], 0, invocLength);
						allInvocations.put(erasedSuperType, someInvocations);
						someInvocations[invocLength] = match;
					}
				}
			}
		}
		// eliminate non minimal super types
		if (remaining > 1) {
			nextType: for (int i = 0; i < superLength; i++) {
				TypeBinding erasedSuperType = erasedSuperTypes[i];
				if (erasedSuperType == null) continue nextType;
				nextOtherType: for (int j = 0; j < superLength; j++) {
					if (i == j) continue nextOtherType;
					TypeBinding otherType = erasedSuperTypes[j];
					if (otherType == null) continue nextOtherType;
					if (erasedSuperType instanceof ReferenceBinding) {
						if (otherType.id == TypeIds.T_JavaLangObject && erasedSuperType.isInterface()) continue nextOtherType; // keep Object for an interface
						if (erasedSuperType.findSuperTypeOriginatingFrom(otherType) != null) {
							erasedSuperTypes[j] = null; // discard non minimal supertype
							remaining--;
						}
					} else if (erasedSuperType.isArrayType()) {
					if (otherType.isArrayType() // keep Object[...] for an interface array (same dimensions)
							&& otherType.leafComponentType().id == TypeIds.T_JavaLangObject
							&& otherType.dimensions() == erasedSuperType.dimensions()
							&& erasedSuperType.leafComponentType().isInterface()) continue nextOtherType;
						if (erasedSuperType.findSuperTypeOriginatingFrom(otherType) != null) {
							erasedSuperTypes[j] = null; // discard non minimal supertype
							remaining--;
						}
					}
				}
			}
		}
		return erasedSuperTypes;
	}

	// Internal use only
	/* All methods in visible are acceptable matches for the method in question...
	* The methods defined by the receiver type appear before those defined by its
	* superclass and so on. We want to find the one which matches best.
	*
	* Since the receiver type is a class, we know each method's declaring class is
	* either the receiver type or one of its superclasses. It is an error if the best match
	* is defined by a superclass, when a lesser match is defined by the receiver type
	* or a closer superclass.
	*/
	protected final MethodBinding mostSpecificClassMethodBinding(MethodBinding[] visible, int visibleSize, InvocationSite invocationSite) {
		MethodBinding previous = null;
		nextVisible : for (int i = 0; i < visibleSize; i++) {
			MethodBinding method = visible[i];
			if (previous != null && TypeBinding.notEquals(method.declaringClass, previous.declaringClass))
				break; // cannot answer a method farther up the hierarchy than the first method found

			if (!method.isStatic()) previous = method; // no ambiguity for static methods
			for (int j = 0; j < visibleSize; j++) {
				if (i == j) continue;
				if (!visible[j].areParametersCompatibleWith(method.parameters))
					continue nextVisible;
			}
			compilationUnitScope().recordTypeReferences(method.thrownExceptions);
			return method;
		}
		return new ProblemMethodBinding(visible[0], visible[0].selector, visible[0].parameters, ProblemReasons.Ambiguous);
	}

	// Internal use only
	/* All methods in visible are acceptable matches for the method in question...
	* Since the receiver type is an interface, we ignore the possibility that 2 inherited
	* but unrelated superinterfaces may define the same method in acceptable but
	* not identical ways... we just take the best match that we find since any class which
	* implements the receiver interface MUST implement all signatures for the method...
	* in which case the best match is correct.
	*
	* NOTE: This is different than javac... in the following example, the message send of
	* bar(X) in class Y is supposed to be ambiguous. But any class which implements the
	* interface I MUST implement both signatures for bar. If this class was the receiver of
	* the message send instead of the interface I, then no problem would be reported.
	*
	interface I1 {
		void bar(J j);
	}
	interface I2 {
	//	void bar(J j);
		void bar(Object o);
	}
	interface I extends I1, I2 {}
	interface J {}

	class X implements J {}

	class Y extends X {
		public void foo(I i, X x) { i.bar(x); }
	}
	*/
	protected final MethodBinding mostSpecificInterfaceMethodBinding(MethodBinding[] visible, int visibleSize, InvocationSite invocationSite) {
		nextVisible : for (int i = 0; i < visibleSize; i++) {
			MethodBinding method = visible[i];
			for (int j = 0; j < visibleSize; j++) {
				if (i == j) continue;
				if (!visible[j].areParametersCompatibleWith(method.parameters))
					continue nextVisible;
			}
			compilationUnitScope().recordTypeReferences(method.thrownExceptions);
			return method;
		}
		return new ProblemMethodBinding(visible[0], visible[0].selector, visible[0].parameters, ProblemReasons.Ambiguous);
	}

	// caveat: this is not a direct implementation of JLS
	protected final MethodBinding mostSpecificMethodBinding(MethodBinding[] visible, int visibleSize, TypeBinding[] argumentTypes, final InvocationSite invocationSite, ReferenceBinding receiverType) {

		if (invocationSite.checkingPotentialCompatibility()) {
			if (visibleSize != visible.length)
				System.arraycopy(visible, 0, visible = new MethodBinding[visibleSize], 0, visibleSize);
			invocationSite.acceptPotentiallyCompatibleMethods(visible);
		}
		// common part for all compliance levels:
		int[] compatibilityLevels = new int[visibleSize];
		int compatibleCount = 0;
		for (int i = 0; i < visibleSize; i++)
			if ((compatibilityLevels[i] = parameterCompatibilityLevelFromInference(visible[i], argumentTypes, invocationSite)) != NOT_COMPATIBLE) {
				if (compatibilityLevels[i] == COMPATIBLE_IGNORING_MISSING_TYPE) {
					// cannot conclusively select any candidate, use the method with missing types in the error message
					return new ProblemMethodBinding(visible[i], visible[i].selector, visible[i].parameters, ProblemReasons.Ambiguous);
				}
				if (i != compatibleCount) {
					visible[compatibleCount] = visible[i];
					compatibilityLevels[compatibleCount] = compatibilityLevels[i];
				}
				compatibleCount++;
			}

		if (compatibleCount == 0) {
			return new ProblemMethodBinding(visible[0].selector, argumentTypes, ProblemReasons.NotFound);
		} else if (compatibleCount == 1) {
			MethodBinding candidate = visible[0];
			if (candidate != null)
				compilationUnitScope().recordTypeReferences(candidate.thrownExceptions);
			return candidate;
		}
		if (compatibleCount != visibleSize) {
			System.arraycopy(visible, 0, visible = new MethodBinding[visibleSize = compatibleCount], 0, compatibleCount);
			System.arraycopy(compatibilityLevels, 0, compatibilityLevels = new int[compatibleCount], 0, compatibleCount);
		}


		MethodBinding[] moreSpecific = new MethodBinding[visibleSize];
		// 15.12.2.5 Choosing the Most Specific Method
		int count = 0;

		nextJ: for (int j = 0; j < visibleSize; j++) {
			MethodBinding mbj = visible[j].genericMethod();
			final TypeBinding[] mbjParameters = mbj.parameters;
			int levelj = compatibilityLevels[j];
			nextK: for (int k = 0; k < visibleSize; k++) {
				if (j == k) continue;
				int levelk = compatibilityLevels[k];
				if (levelj > -1 && levelk > -1 && levelj != levelk) {
					if (levelj < levelk)
						continue nextK; // j is more specific than this k
					else
						continue nextJ; // j cannot be more specific
				}
				MethodBinding mbk = visible[k].genericMethod();
				final TypeBinding[] mbkParameters = mbk.parameters;
				// TODO: should the following line also find diamond-typeVariables?
				if (((invocationSite instanceof Invocation) || (invocationSite instanceof ReferenceExpression))
						&& mbk.typeVariables() != Binding.NO_TYPE_VARIABLES)
				{
					// 18.5.4 More Specific Method Inference
					Expression[] expressions = null;
					if (invocationSite instanceof Invocation) {
						expressions = ((Invocation)invocationSite).arguments();
					} else {
						expressions = ((ReferenceExpression)invocationSite).createPseudoExpressions(argumentTypes);
					}
					InferenceContext18 ic18 = new InferenceContext18(this, expressions, invocationSite);
					if (!ic18.isMoreSpecificThan(mbj, mbk, levelj == VARARGS_COMPATIBLE, levelk == VARARGS_COMPATIBLE)) {
						continue nextJ;
					}
				} else {
					for (int i = 0, length = argumentTypes.length; i < length; i++) {
						TypeBinding argumentType = argumentTypes[i];
						TypeBinding s = InferenceContext18.getParameter(mbjParameters, i, levelj == VARARGS_COMPATIBLE);
						TypeBinding t = InferenceContext18.getParameter(mbkParameters, i, levelk == VARARGS_COMPATIBLE);
						if (TypeBinding.equalsEquals(s, t))
							continue;
						if (!argumentType.sIsMoreSpecific(s,t, this)) {
							continue nextJ;
						}
					}
					if (levelj == VARARGS_COMPATIBLE && levelk == VARARGS_COMPATIBLE) {
						TypeBinding s = InferenceContext18.getParameter(mbjParameters, argumentTypes.length, true);
						TypeBinding t = InferenceContext18.getParameter(mbkParameters, argumentTypes.length, true);
						if (TypeBinding.notEquals(s, t) && t.isSubtypeOf(s, false))
							continue nextJ;
					}
				}
			}
			moreSpecific[count++] = visible[j];
		}
		if (count == 0) {
			return new ProblemMethodBinding(visible[0], visible[0].selector, visible[0].parameters, ProblemReasons.Ambiguous);
		} else if (count == 1) {
			MethodBinding candidate = moreSpecific[0];
			if (visibleSize > 1 && candidate instanceof ParameterizedMethodBinding && invocationSite instanceof MessageSend) {
				for (TypeBinding typeBinding : argumentTypes) {
					if (!typeBinding.isProperType(true)) {
						InferenceContext18 ic18 = ((MessageSend) invocationSite).getInferenceContext((ParameterizedMethodBinding) candidate);
						if (ic18 != null)
							ic18.prematureOverloadResolution = true;
						break;
					}
				}
			}
			if (candidate != null)
				compilationUnitScope().recordTypeReferences(candidate.thrownExceptions);
			return candidate;
		} else {
			visibleSize = count;
			// we proceed with pre 1.8 code below, which checks for overriding
		}

		// found several methods that are mutually acceptable -> must be equal
		// so now with the first acceptable method, find the 'correct' inherited method for each other acceptable method AND
		// see if they are equal after substitution of type variables (do the type variables have to be equal to be considered an override???)
		if (receiverType != null)
			receiverType = receiverType instanceof CaptureBinding ? receiverType : (ReferenceBinding) receiverType.erasure();

		boolean hasConsideredNullContract = false;
		// perform 1 or 2 attempts, the second being the safety net, in case considering null contracts may have prevented finding a solution.
		for (int attempt = 0; attempt < 2; attempt++) {
			nextSpecific : for (int i = 0; i < visibleSize; i++) {
				MethodBinding current = moreSpecific[i];
				if (current != null) {
					ReferenceBinding[] mostSpecificExceptions = null;
					MethodBinding original = current.original();
					boolean shouldIntersectExceptions = original.declaringClass.isAbstract() && original.thrownExceptions != Binding.NO_EXCEPTIONS; // only needed when selecting from interface methods
					for (int j = 0; j < visibleSize; j++) {
						MethodBinding next = moreSpecific[j];
						if (next == null || i == j) continue;
						MethodBinding original2 = next.original();
						if (TypeBinding.equalsEquals(original.declaringClass, original2.declaringClass))
							break nextSpecific; // duplicates thru substitution

						if (!original.isAbstract()) {
							if (original2.isAbstract() || original2.isDefaultMethod())
								continue; // only compare current against other concrete methods

							original2 = original.findOriginalInheritedMethod(original2);
							if (original2 == null)
								continue nextSpecific; // current's declaringClass is not a subtype of next's declaringClass
							if (current.hasSubstitutedParameters() || original.typeVariables != Binding.NO_TYPE_VARIABLES) {
								if (!environment().methodVerifier().isParameterSubsignature(original, original2))
									continue nextSpecific; // current does not override next
							}
						} else if (receiverType != null) { // should not be null if original isAbstract, but be safe
							TypeBinding superType = receiverType.findSuperTypeOriginatingFrom(original.declaringClass.erasure());
							if (TypeBinding.equalsEquals(original.declaringClass, superType) || !(superType instanceof ReferenceBinding)) {
								// keep original
							} else {
								// must find inherited method with the same substituted variables
								MethodBinding[] superMethods = ((ReferenceBinding) superType).getMethods(original.selector, argumentTypes.length);
								for (MethodBinding superMethod : superMethods) {
									if (superMethod.original() == original) {
										original = superMethod;
										break;
									}
								}
							}
							superType = receiverType.findSuperTypeOriginatingFrom(original2.declaringClass.erasure());
							if (TypeBinding.equalsEquals(original2.declaringClass, superType) || !(superType instanceof ReferenceBinding)) {
								// keep original2
							} else {
								// must find inherited method with the same substituted variables
								MethodBinding[] superMethods = ((ReferenceBinding) superType).getMethods(original2.selector, argumentTypes.length);
								for (MethodBinding superMethod : superMethods) {
									if (superMethod.original() == original2) {
										original2 = superMethod;
										break;
									}
								}
							}
							if (original.typeVariables != Binding.NO_TYPE_VARIABLES)
								original2 = original.computeSubstitutedMethod(original2, environment());
							if (original2 == null || !original.areParameterErasuresEqual(original2))
								continue nextSpecific; // current does not override next
							if (TypeBinding.notEquals(original.returnType, original2.returnType)) {
								if (next.original().typeVariables != Binding.NO_TYPE_VARIABLES) {
									if (original.returnType.erasure().findSuperTypeOriginatingFrom(original2.returnType.erasure()) == null)
										continue nextSpecific;
								} else if (!current.returnType.isCompatibleWith(next.returnType)) {
									continue nextSpecific;
								}
								// continue with original 15.12.2.5
							}
							if (attempt == 0
									&& compilerOptions().isAnnotationBasedNullAnalysisEnabled
									&& j > i // don't go backwards
									&& NullAnnotationMatching.hasMoreSpecificNullness(next, current))
							{
								// In this case we want to prefer 'next' among equivalent methods.
								// (the case where JLS 15.12.2.5 says "...is chosen arbitrarily...")
								// To try if 'next' matches all criteria, skip outer loop to j (after increment):
								i = j -1 ;
								hasConsideredNullContract = true;
								continue nextSpecific;
							}
							if (shouldIntersectExceptions && original2.declaringClass.isInterface()) {
								if (current.thrownExceptions != next.thrownExceptions) {
									if (next.thrownExceptions == Binding.NO_EXCEPTIONS) {
										mostSpecificExceptions = Binding.NO_EXCEPTIONS;
									} else {
										if (mostSpecificExceptions == null) {
											mostSpecificExceptions = current.thrownExceptions;
										}
										int mostSpecificLength = mostSpecificExceptions.length;
										ReferenceBinding[] nextExceptions = getFilteredExceptions(next);
										int nextLength = nextExceptions.length;
										SimpleSet temp = new SimpleSet(mostSpecificLength);
										boolean changed = false;
										nextException : for (int t = 0; t < mostSpecificLength; t++) {
											ReferenceBinding exception = mostSpecificExceptions[t];
											for (int s = 0; s < nextLength; s++) {
												ReferenceBinding nextException = nextExceptions[s];
												if (exception.isCompatibleWith(nextException)) {
													temp.add(exception);
													continue nextException;
												} else if (nextException.isCompatibleWith(exception)) {
													temp.add(nextException);
													changed = true;
													continue nextException;
												} else {
													changed = true;
												}
											}
										}
										if (changed) {
											mostSpecificExceptions = temp.elementSize == 0 ? Binding.NO_EXCEPTIONS : new ReferenceBinding[temp.elementSize];
											temp.asArray(mostSpecificExceptions);
										}
									}
								}
							}
						}
					}
					if (mostSpecificExceptions != null && mostSpecificExceptions != current.thrownExceptions) {
						return new MostSpecificExceptionMethodBinding(current, mostSpecificExceptions);
					}
					return current;
				}
			}
			if (!hasConsideredNullContract)
				break;
			// otherwise retry without considering null contracts
		}
		return new ProblemMethodBinding(visible[0], visible[0].selector, visible[0].parameters, ProblemReasons.Ambiguous);
	}

	private ReferenceBinding[] getFilteredExceptions(MethodBinding method) {
		// http://bugs.eclipse.org/387612 - Unreachable catch block...exception is never thrown from the try
		// Need to filter redundant exceptions within the same throws clause.
		// In this filtering the *most general* exception wins in order to capture all possible exceptions
		// that could be thrown by the given method.
		ReferenceBinding[] allExceptions = method.thrownExceptions;
		int length = allExceptions.length;
		if (length < 2) return allExceptions;
		ReferenceBinding[] filteredExceptions = new ReferenceBinding[length];
		int count = 0;
		currents: for (int i = 0; i < length; i++) {
			ReferenceBinding currentException = allExceptions[i];
			for (int j = 0; j < length; j++) {
				if (i == j) continue;
				if (TypeBinding.equalsEquals(currentException, allExceptions[j])) {
					// duplicate same exception
					if (i < j)
						break; // take only the first occurrence
					else
						continue currents; // skip
				}
				if (currentException.isCompatibleWith(allExceptions[j])) {
					continue currents; // skip
				}
			}
			filteredExceptions[count++] = currentException;
		}
		if (count != length) {
			ReferenceBinding[] tmp = new ReferenceBinding[count];
			System.arraycopy(filteredExceptions, 0, tmp,  0, count);
			return tmp;
		}
		return allExceptions;
	}

	public final ClassScope outerMostClassScope() {
		ClassScope outerMostClassScope = null;
		Scope scope = this;
		do {
			if (scope instanceof ClassScope classScope)
				outerMostClassScope = classScope;
			scope = scope.parent;
		} while (scope != null);
		return outerMostClassScope; // may answer null if no class around
	}

	public final MethodScope outerMostMethodScope() {
		MethodScope lastMethodScope = null;
		Scope scope = this;
		do {
			if (scope instanceof MethodScope)
				lastMethodScope = (MethodScope) scope;
			scope = scope.parent;
		} while (scope != null);
		return lastMethodScope; // may answer null if no method around
	}

	// Version that just answers based on inference kind (at 1.8+) when available.
	public int parameterCompatibilityLevelFromInference(MethodBinding method, TypeBinding[] arguments, InvocationSite site) {
		if (method.problemId() == ProblemReasons.InvocationTypeInferenceFailure) {
			// we need to accept methods with InvocationTypeInferenceFailure, because logically overload resolution happens *before* invocation type inference
			method = ((ProblemMethodBinding)method).closestMatch; // for compatibility checks use the actual method
			if (method == null)
				return NOT_COMPATIBLE;
		}
		if (method instanceof ParameterizedGenericMethodBinding) {
			int inferenceKind = InferenceContext18.CHECK_UNKNOWN;
			InferenceContext18 context = null;
			if (site instanceof Invocation) {
				Invocation invocation = (Invocation) site;
				context = invocation.getInferenceContext((ParameterizedGenericMethodBinding) method);
				if (context != null)
					inferenceKind = context.inferenceKind;
			} else if (site instanceof ReferenceExpression) {
				ReferenceExpression referenceExpression = (ReferenceExpression) site;
				context = referenceExpression.getInferenceContext((ParameterizedGenericMethodBinding) method);
				if (context != null)
					inferenceKind = context.inferenceKind;
			}
			/* 1.8+ Post inference compatibility check policy: For non-functional-type arguments, trust inference. For functional type arguments apply compatibility checks after inference
			   has completed to ensure arguments that were not pertinent to applicability which have only seen potential compatibility checks are actually compatible.
			*/
			if (site instanceof Invocation && context != null && context.stepCompleted >= InferenceContext18.TYPE_INFERRED) {
				for (int i = 0, length = arguments.length; i < length; i++) {
					TypeBinding argument = arguments[i];
					if (!argument.isFunctionalType())
						continue;
					TypeBinding parameter = InferenceContext18.getParameter(method.parameters, i, context.isVarArgs());
					if (!argument.isCompatibleWith(parameter, this)) {
						if (argument.isPolyType()) {
							parameter = InferenceContext18.getParameter(method.original().parameters, i, context.isVarArgs());
							if (!((PolyTypeBinding)argument).expression.isPertinentToApplicability(parameter, method))
								continue;
						}
						return NOT_COMPATIBLE;
					}
				}
			}
			switch (inferenceKind) {
				case InferenceContext18.CHECK_STRICT:
					return COMPATIBLE;
				case InferenceContext18.CHECK_LOOSE:
					return AUTOBOX_COMPATIBLE;
				case InferenceContext18.CHECK_VARARG:
					return VARARGS_COMPATIBLE;
				default:
					break;
				}
		}
		return parameterCompatibilityLevel(method, arguments, site);
	}

	public int parameterCompatibilityLevel(MethodBinding method, TypeBinding[] arguments, InvocationSite site) {
		TypeBinding[] parameters = method.parameters;
		int paramLength = parameters.length;
		int argLength = arguments.length;
		int level = COMPATIBLE; // no autoboxing or varargs support needed
		int lastIndex = argLength;
		LookupEnvironment env = environment();
		if (method.isVarargs()) {
			lastIndex = paramLength - 1;
			if (paramLength == argLength) { // accept X or X[] but not X[][]
				TypeBinding param = parameters[lastIndex]; // is an ArrayBinding by definition
				TypeBinding arg = arguments[lastIndex];
				if (TypeBinding.notEquals(param, arg)) {
					level = parameterCompatibilityLevel(arg, param, env, method);
					if (level == NOT_COMPATIBLE) {
						// expect X[], is it called with X
						param = ((ArrayBinding) param).elementsType();
						if (parameterCompatibilityLevel(arg, param, env, method) == NOT_COMPATIBLE)
							return NOT_COMPATIBLE;
						level = VARARGS_COMPATIBLE; // varargs support needed
					}
				}
			} else {
				if (paramLength < argLength) { // all remaining argument types must be compatible with the elementsType of varArgType
					TypeBinding param = ((ArrayBinding) parameters[lastIndex]).elementsType();
					for (int i = lastIndex; i < argLength; i++) {
						TypeBinding arg = arguments[i];
						if (TypeBinding.notEquals(param, arg)) {
							level = parameterCompatibilityLevel(arg, param, env, method);
							if (level == NOT_COMPATIBLE)
								return NOT_COMPATIBLE;
						}
					}
				}  else if (lastIndex != argLength) { // can call foo(int i, X ... x) with foo(1) but NOT foo();
					return NOT_COMPATIBLE;
				}
				if (level != COMPATIBLE_IGNORING_MISSING_TYPE) // preserve any COMPATIBLE_IGNORING_MISSING_TYPE
					level = VARARGS_COMPATIBLE; // varargs support needed
			}
		} else if (paramLength != argLength) {
			return NOT_COMPATIBLE;
		}
		// now compare standard arguments from 0 to lastIndex
		for (int i = 0; i < lastIndex; i++) {
			TypeBinding param = parameters[i];
			TypeBinding arg = arguments[i];
			if (TypeBinding.notEquals(arg,param)) {
				if (site instanceof Invocation invocation) {
					Expression[] invArgs = invocation.arguments();
					int idx = i < invArgs.length ? i : invArgs.length - 1;
					CapturingContext.enter(invArgs[idx].sourceStart, invArgs[idx].sourceEnd, this);
				}
				int newLevel;
				try {
					newLevel = parameterCompatibilityLevel(arg, param, env, method);
				} finally {
					CapturingContext.leave();
				}
				if (newLevel == NOT_COMPATIBLE) {
					return NOT_COMPATIBLE;
				} else if (newLevel == COMPATIBLE_IGNORING_MISSING_TYPE) {
					level = newLevel;
				} else if (newLevel > level && level != COMPATIBLE_IGNORING_MISSING_TYPE) {
					level = newLevel;
				}
			}
		}
		return level;
	}

	public int parameterCompatibilityLevel(TypeBinding arg, TypeBinding param) {

		if (TypeBinding.equalsEquals(arg, param))
			return COMPATIBLE;

		if (arg == null || param == null)
			return NOT_COMPATIBLE;

		if (arg.isCompatibleWith(param, this))
			return COMPATIBLE;

		if (arg.kind() == Binding.POLY_TYPE || (arg.isBaseType() != param.isBaseType())) {
			TypeBinding convertedType = environment().computeBoxingType(arg);
			if (TypeBinding.equalsEquals(convertedType, param) || convertedType.isCompatibleWith(param, this))
				return AUTOBOX_COMPATIBLE;
		}
		return NOT_COMPATIBLE;
	}

	private int parameterCompatibilityLevel(TypeBinding arg, TypeBinding param, LookupEnvironment env, MethodBinding method) {
		// only called if env.options.sourceLevel >= ClassFileConstants.JDK1_5
		if (arg == null || param == null)
			return NOT_COMPATIBLE;
		if ((param.tagBits & TagBits.HasMissingType) != 0)
			return COMPATIBLE_IGNORING_MISSING_TYPE;
		if (arg instanceof PolyTypeBinding && !((PolyTypeBinding) arg).expression.isPertinentToApplicability(param, method)) {
			if (arg.isPotentiallyCompatibleWith(param, this))
				return COMPATIBLE;
		} else if (arg.isCompatibleWith(param, this)) {
			return COMPATIBLE;
		}
		if (arg.kind() == Binding.POLY_TYPE || (arg.isBaseType() != param.isBaseType())) {
			TypeBinding convertedType = env.computeBoxingType(arg);
			if (TypeBinding.equalsEquals(convertedType, param) || convertedType.isCompatibleWith(param, this))
				return AUTOBOX_COMPATIBLE;
		}
		return NOT_COMPATIBLE;
	}

	/** See also the contract of {@link ProblemReporter#close()}. */
	public abstract ProblemReporter problemReporter();

	public final CompilationUnitDeclaration referenceCompilationUnit() {
		return this.compilationUnitScope.referenceContext;
	}

	/**
	 * Returns the nearest reference context, starting from current scope.
	 * If starting on a class, it will return current class. If starting on unitScope, returns unit.
	 */
	public ReferenceContext referenceContext() {
		Scope current = this;
		do {
			switch(current.kind) {
				case METHOD_SCOPE :
					return ((MethodScope) current).referenceContext;
				case CLASS_SCOPE :
					return ((ClassScope) current).referenceContext;
				case COMPILATION_UNIT_SCOPE :
					return ((CompilationUnitScope) current).referenceContext;
				case MODULE_SCOPE :
					return ((ModuleScope) current).referenceContext;
			}
		} while ((current = current.parent) != null);
		return null;
	}

	/**
	 * Returns the nearest original reference context, starting from current scope.
	 * If starting on a class, it will return current class. If starting on unitScope, returns unit.
	 */
	public ReferenceContext originalReferenceContext() {
		Scope current = this;
		do {
			switch(current.kind) {
				case METHOD_SCOPE :
					ReferenceContext context = ((MethodScope) current).referenceContext;
					if (context instanceof LambdaExpression) {
						LambdaExpression expression = (LambdaExpression) context;
						while (expression != expression.original)
							expression = expression.original;
						return expression;
					}
					return context;
				case CLASS_SCOPE :
					return ((ClassScope) current).referenceContext;
				case COMPILATION_UNIT_SCOPE :
					return ((CompilationUnitScope) current).referenceContext;
			}
		} while ((current = current.parent) != null);
		return null;
	}

	public void deferBoundCheck(TypeReference typeRef) {
		// TODO: use dynamic binding rather than explicit type check
		if (this.kind == CLASS_SCOPE) {
			ClassScope classScope = (ClassScope) this;
			if (classScope.deferredBoundChecks == null) {
				classScope.deferredBoundChecks = new ArrayList<>(3);
				classScope.deferredBoundChecks.add(typeRef);
			} else if (!classScope.deferredBoundChecks.contains(typeRef)) {
				classScope.deferredBoundChecks.add(typeRef);
			}
		}
	}

	// start position in this scope - for ordering scopes vs. variables
	int startIndex() {
		return 0;
	}
	/* Given an allocation type and arguments at the allocation site, answer a synthetic generic static factory method
	   that could instead be invoked with identical results. Return null if no compatible, visible, most specific method
	   could be found. This method is modeled after Scope.getConstructor and Scope.getMethod.
	 */
	public MethodBinding getStaticFactory (ParameterizedTypeBinding allocationType, ReferenceBinding originalEnclosingType, TypeBinding[] argumentTypes, final InvocationSite allocationSite) {

		// allocationType is the diamond type. originalEnclosingType is the real enclosing type ==> may be parameterized, parameterized with own type variables, raw, just plain type or null.
		int classTypeVariablesArity = 0;
		TypeVariableBinding[] classTypeVariables = Binding.NO_TYPE_VARIABLES;
		ReferenceBinding genericType = allocationType.genericType();
		ReferenceBinding currentType = genericType;
		while (currentType != null) {
			TypeVariableBinding[] typeVariables = currentType.typeVariables();
			int length = typeVariables == null ? 0 : typeVariables.length;
			if (length > 0) {
				System.arraycopy(classTypeVariables, 0, classTypeVariables = new TypeVariableBinding[classTypeVariablesArity + length], 0, classTypeVariablesArity);
				System.arraycopy(typeVariables, 0, classTypeVariables, classTypeVariablesArity, length);
				classTypeVariablesArity += length;
			}
			if (currentType.isStatic()) // any enclosing types cannot be parameterized, if generic treat as raw.
				break;
			currentType = currentType.enclosingType();
		}
		boolean isInterface = allocationType.isInterface();
		ReferenceBinding typeToSearch = isInterface ? getJavaLangObject() : allocationType;

		MethodBinding[] methods = typeToSearch.getMethods(TypeConstants.INIT, argumentTypes.length);
		MethodBinding [] staticFactories = new MethodBinding[methods.length];
		int sfi = 0;
		for (MethodBinding method : methods) {
			if (!method.canBeSeenBy(allocationSite, this))
				continue;

			int paramLength = method.parameters.length;
			boolean isVarArgs = method.isVarargs();
			if (argumentTypes.length != paramLength)
				if (!isVarArgs || argumentTypes.length < paramLength - 1)
					continue; // incompatible

			TypeVariableBinding[] methodTypeVariables = method.typeVariables();
			int methodTypeVariablesArity = methodTypeVariables.length;
			final int factoryArity = classTypeVariablesArity + methodTypeVariablesArity;
			final LookupEnvironment environment = environment();
			MethodBinding targetMethod = isInterface ? new MethodBinding(method.original(), genericType) : method.original();
			MethodBinding staticFactory = new SyntheticFactoryMethodBinding(targetMethod, environment, originalEnclosingType);
			staticFactory.typeVariables = new TypeVariableBinding[factoryArity];
			Map<TypeBinding, TypeVariableBinding> map = new HashMap<>(factoryArity);

			// Rename each type variable T of the type to T' or T'' or T''' based on the enclosing level to avoid a clash.
			String prime = ""; //$NON-NLS-1$
			Binding declaringElement = null;
			for (int j = 0; j < classTypeVariablesArity; j++) {
				TypeVariableBinding original;
				original = classTypeVariables[j];
				if (original.declaringElement != declaringElement) {
					declaringElement = original.declaringElement;
					prime += "'"; //$NON-NLS-1$
				}
				map.put(original.unannotated(), staticFactory.typeVariables[j] = new TypeVariableBinding(CharOperation.concat(original.sourceName, prime.toCharArray()),
																			staticFactory, j, environment));
			}
			// Rename each type variable U of method
			prime += "'"; //$NON-NLS-1$
			for (int j = classTypeVariablesArity, k = 0; j < factoryArity; j++, k++) {
				map.put(methodTypeVariables[k].unannotated(),
						(staticFactory.typeVariables[j] = new TypeVariableBinding(CharOperation.concat(methodTypeVariables[k].sourceName, prime.toCharArray()),
																			staticFactory, j, environment)));

			}
			final Scope scope = this;
			Substitution substitution = new Substitution() {
					@Override
					public LookupEnvironment environment() {
						return scope.environment();
					}
					@Override
					public boolean isRawSubstitution() {
						return false;
					}
					@Override
					public TypeBinding substitute(TypeVariableBinding typeVariable) {
						TypeBinding retVal = map.get(typeVariable.unannotated());
						return retVal == null ? typeVariable : typeVariable.hasTypeAnnotations() ? environment().createAnnotatedType(retVal, typeVariable.getTypeAnnotations()) : retVal;
					}
				};

			// initialize new variable bounds
			for (int j = 0; j < factoryArity; j++) {
				TypeVariableBinding originalVariable = j < classTypeVariablesArity ? classTypeVariables[j] : methodTypeVariables[j - classTypeVariablesArity];
				TypeVariableBinding substitutedVariable = map.get(originalVariable.unannotated());

				TypeBinding substitutedSuperclass = Scope.substitute(substitution, originalVariable.superclass);
				ReferenceBinding[] substitutedInterfaces = Scope.substitute(substitution, originalVariable.superInterfaces);
				if (originalVariable.firstBound != null) {
					TypeBinding firstBound;
					firstBound = TypeBinding.equalsEquals(originalVariable.firstBound, originalVariable.superclass)
							? substitutedSuperclass // could be array type or interface
									: substitutedInterfaces[0];
					substitutedVariable.setFirstBound(firstBound);
				}
				switch (substitutedSuperclass.kind()) {
					case Binding.ARRAY_TYPE :
						substitutedVariable.setSuperClass(environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_OBJECT, null));
						substitutedVariable.setSuperInterfaces(substitutedInterfaces);
						break;
					default:
						if (substitutedSuperclass.isInterface()) {
							substitutedVariable.setSuperClass(environment.getResolvedJavaBaseType(TypeConstants.JAVA_LANG_OBJECT, null));
							int interfaceCount = substitutedInterfaces.length;
							System.arraycopy(substitutedInterfaces, 0, substitutedInterfaces = new ReferenceBinding[interfaceCount+1], 1, interfaceCount);
							substitutedInterfaces[0] = (ReferenceBinding) substitutedSuperclass;
							substitutedVariable.setSuperInterfaces(substitutedInterfaces);
						} else {
							substitutedVariable.setSuperClass((ReferenceBinding) substitutedSuperclass); // typeVar was extending other typeVar which got substituted with interface
							substitutedVariable.setSuperInterfaces(substitutedInterfaces);
						}
				}
			}
			staticFactory.returnType = environment.createParameterizedType(genericType,
					Scope.substitute(substitution, genericType.typeVariables()),
					(ReferenceBinding) Scope.substitute(substitution, originalEnclosingType));
			staticFactory.parameters = Scope.substitute(substitution, method.parameters);
			staticFactory.thrownExceptions = Scope.substitute(substitution, method.thrownExceptions);
			if (staticFactory.thrownExceptions == null) {
				staticFactory.thrownExceptions = Binding.NO_EXCEPTIONS;
			}
			staticFactories[sfi++] = new ParameterizedMethodBinding((ParameterizedTypeBinding) environment.convertToParameterizedType(isInterface ? allocationType : staticFactory.declaringClass),
																												staticFactory);
		}
		if (sfi == 0)
			return null;
		if (sfi != methods.length) {
			System.arraycopy(staticFactories, 0, staticFactories = new MethodBinding[sfi], 0, sfi);
		}
		MethodBinding[] compatible = new MethodBinding[sfi];
		int compatibleIndex = 0;
		for (int i = 0; i < sfi; i++) {
			MethodBinding compatibleMethod = computeCompatibleMethod(staticFactories[i], argumentTypes, allocationSite);
			if (compatibleMethod != null) {
				if (compatibleMethod.isValidBinding())
					compatible[compatibleIndex++] = compatibleMethod;
			}
		}

		if (compatibleIndex == 0) {
			return null;
		}
		return compatibleIndex == 1 ? compatible[0] : mostSpecificMethodBinding(compatible, compatibleIndex, argumentTypes, allocationSite, allocationType);
	}

	public boolean validateNullAnnotation(long tagBits, TypeReference typeRef, Annotation[] annotations) {
		if (typeRef == null || typeRef.resolvedType == null)
			return true;
		TypeBinding type = typeRef.resolvedType;

		boolean usesNullTypeAnnotations = this.environment().usesNullTypeAnnotations();
		long nullAnnotationTagBit;
		if (usesNullTypeAnnotations) {
			type = type.leafComponentType(); // if it's an array, the annotation applies to the leaf component type
			nullAnnotationTagBit = type.tagBits & TagBits.AnnotationNullMASK;
		} else {
			nullAnnotationTagBit = tagBits & (TagBits.AnnotationNullMASK);
		}

		if (nullAnnotationTagBit != 0) {
			if (type != null && type.isBaseType()) {
				// type annotations are *always* illegal for 'void' (already reported)
				if (!(typeRef.resolvedType.id == TypeIds.T_void && usesNullTypeAnnotations))
					problemReporter().illegalAnnotationForBaseType(typeRef, annotations, nullAnnotationTagBit);
				return false;
			}
			// for type annotations, more problems are detected in Annotation.isTypeUseCompatible()
		}
		return true;
	}

	/**
	 * Record a NNBD annotation applying to a given source range within the current scope
	 * @param target the annotated element
	 * @param value bitset describing the default nullness (see Binding.NullnessDefaultMASK)
	 * @param annotation the NNBD annotation
	 * @param scopeStart start of the source range affected by the default
	 * @param scopeEnd end of the source range affected by the default
	 * @return <code>true</code> if the annotation was newly recorded, <code>false</code> if a corresponding entry already existed.
	 */
	public boolean recordNonNullByDefault(Binding target, int value, Annotation annotation, int scopeStart, int scopeEnd) {
		ReferenceContext context = referenceContext();
		if (context instanceof LambdaExpression && context != ((LambdaExpression) context).original)
			return false; // Do not record from copies. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=441929

		if (this.nullDefaultRanges == null) {
			this.nullDefaultRanges=new ArrayList<>(3);
		}
		for (NullDefaultRange nullDefaultRange : this.nullDefaultRanges) {
			if (nullDefaultRange.start== scopeStart && nullDefaultRange.end==scopeEnd) {
				if (nullDefaultRange.contains(annotation)) {
					// annotation data already recorded
					return false;
				} else {
					nullDefaultRange.merge(value, annotation, target);
					return true;
				}
			}
		}
		this.nullDefaultRanges.add(new NullDefaultRange(value, annotation, scopeStart, scopeEnd, target));
		return true;
	}

	/**
	 * Check whether the given null default is redundant at the given position inside this scope.
	 * @param nullBits locally defined nullness default, see Binding.NullnessDefaultMASK
	 * @return enclosing binding that already has a matching NonNullByDefault annotation,
	 * 		or the special binding {@link #NOT_REDUNDANT}, indicating that a different enclosing nullness default was found,
	 * 		or null to indicate that no enclosing nullness default was found.
	 */
	public Binding checkRedundantDefaultNullness(int nullBits, int sourceStart) {
		Binding target = localCheckRedundantDefaultNullness(nullBits, sourceStart);
		if (target != null) {
			return target;
		}
		return this.parent.checkRedundantDefaultNullness(nullBits, sourceStart);
	}

	/** Answer a defaultNullness defined for the closest enclosing scope, using bits from Binding.NullnessDefaultMASK. */
	public boolean hasDefaultNullnessFor(int location, int sourceStart) {
		int nonNullByDefaultValue = localNonNullByDefaultValue(sourceStart);
		if (nonNullByDefaultValue != 0) {
			return (nonNullByDefaultValue & location) != 0;
		}
		return this.parent.hasDefaultNullnessFor(location, sourceStart);
	}

	public boolean hasDefaultNullnessForType(TypeBinding type, int location, int sourceStart) {
		TypeBinding enclosingType = enclosingReceiverType();
		if (enclosingType != null && (enclosingType.original().tagBits & TagBits.EndHierarchyCheck) == 0)
			return false;
		if (environment().usesNullTypeAnnotations() && type != null && !type.acceptsNonNullDefault())
			return false;
		return hasDefaultNullnessFor(location, sourceStart);
	}

	/*
	 * helper for hasDefaultNullnessFor(..) which inspects only ranges recorded within this scope.
	 */
	public final int localNonNullByDefaultValue(int start) {
		NullDefaultRange nullDefaultRange = nullDefaultRangeForPosition(start);
		return nullDefaultRange != null ? nullDefaultRange.value : 0;
	}

	/*
	 * local variant of checkRedundantDefaultNullness(..), i.e., only inspect ranges recorded within this scope.
	 */
	final protected /* @Nullable */ Binding localCheckRedundantDefaultNullness(int nullBits, int position) {
		NullDefaultRange nullDefaultRange = nullDefaultRangeForPosition(position);
		if (nullDefaultRange != null)
			return (nullBits == nullDefaultRange.value) ? nullDefaultRange.target : NOT_REDUNDANT;
		return null;
	}

	private /* @Nullable */ NullDefaultRange nullDefaultRangeForPosition(int start) {
		if (this.nullDefaultRanges != null) {
			for (NullDefaultRange nullDefaultRange : this.nullDefaultRanges) {
				if (start >= nullDefaultRange.start && start < nullDefaultRange.end) {
					return nullDefaultRange;
				}
			}
		}
		return null;
	}

	public static BlockScope typeAnnotationsResolutionScope(Scope scope) {
		BlockScope resolutionScope = null;
		switch(scope.kind) {
			case Scope.CLASS_SCOPE:
				resolutionScope = ((ClassScope) scope).referenceContext.staticInitializerScope;
				break;
			case Scope.BLOCK_SCOPE :
			case Scope.METHOD_SCOPE :
				resolutionScope = (BlockScope) scope;
				break;
		}
		return resolutionScope;
	}
	// Some entity in the receiver scope is referencing instance data of enclosing type. Tag all intervening methods as instance methods.
	public void tagAsAccessingEnclosingInstanceStateOf(ReferenceBinding enclosingType, boolean typeVariableAccess) {
		MethodScope methodScope = methodScope();
		if (methodScope != null && methodScope.referenceContext instanceof TypeDeclaration) {
			if (!methodScope.enclosingReceiverType().isCompatibleWith(enclosingType)) { // unless invoking a method of the local type ...
				// anonymous type, find enclosing method
				methodScope = methodScope.enclosingMethodScope();
			}
		}
		boolean isFlexibleConstructorsEnabled = methodScope != null
				&& JavaFeature.FLEXIBLE_CONSTRUCTOR_BODIES.isSupported(methodScope.compilerOptions());
		MethodBinding enclosingMethod = enclosingType != null ? enclosingType.enclosingMethod() : null;
		while (methodScope != null) {
			while (methodScope != null && methodScope.referenceContext instanceof LambdaExpression) {
				LambdaExpression lambda = (LambdaExpression) methodScope.referenceContext;
				SourceTypeBinding lambdaEnclosingType = methodScope.classScope().referenceContext.binding;
				boolean insideEarlyConstructionContext = isFlexibleConstructorsEnabled &&
						methodScope.isInsideEarlyConstructionContext(lambdaEnclosingType, false);
				if (methodScope.isConstructorCall || insideEarlyConstructionContext) {
					ReferenceBinding tmp = lambdaEnclosingType;
					while ((tmp = tmp.enclosingType()) != null) {
						if (!TypeBinding.equalsEquals(enclosingType, tmp)) continue;
						lambda.ensureSyntheticOuterAccess((SourceTypeBinding) enclosingType);
						lambda.hasOuterClassMemberReference = true; // ref to Outer class members allowed - interpreting 8.8.7.1
						break;
					}
				}
				if (!typeVariableAccess && !lambda.scope.isStatic && !lambda.hasOuterClassMemberReference && !insideEarlyConstructionContext)
					lambda.shouldCaptureInstance = true;  // lambda can still be static, only when `this' is touched (implicitly or otherwise) it cannot be.
				methodScope = methodScope.enclosingMethodScope();
			}
			if (methodScope != null) {
				AbstractMethodDeclaration methodDecl = methodScope.referenceMethod();
				if (methodDecl != null && methodDecl.binding == enclosingMethod)
					break;
				if (methodDecl instanceof MethodDeclaration)
					methodDecl.bits &= ~ASTNode.CanBeStatic;
				ClassScope enclosingClassScope = methodScope.enclosingClassScope();
				if (enclosingClassScope != null) {
					TypeDeclaration type = enclosingClassScope.referenceContext;
					if (type != null && type.binding != null && enclosingType != null && !type.binding.isCompatibleWith(enclosingType.original())) {
						methodScope = enclosingClassScope.enclosingMethodScope();
						continue;
					}
				}
				break;
			}
		}
	}

	public Supplier<ReferenceBinding> getCommonReferenceBinding(char[] typeName) {
		assert typeName != null && typeName.length > 0;
		initializeCommonTypeBindings();
		Supplier<ReferenceBinding> typeSupplier = this.commonTypeBindings.get(new String(typeName));
		return typeSupplier;
	}

	private Map<String, Supplier<ReferenceBinding>> initializeCommonTypeBindings() {
		if (this.commonTypeBindings != null)
			return this.commonTypeBindings;
		Map<String, Supplier<ReferenceBinding>> t = new HashMap<>();
		t.put(new String(ConstantPool.JavaLangAssertionErrorConstantPoolName), this :: getJavaLangAssertionError);
		t.put(new String(ConstantPool.JavaLangErrorConstantPoolName), this :: getJavaLangError);
		t.put(new String(ConstantPool.JavaLangIncompatibleClassChangeErrorConstantPoolName), this :: getJavaLangIncompatibleClassChangeError);
		t.put(new String(ConstantPool.JavaLangNoClassDefFoundErrorConstantPoolName), this :: getJavaLangNoClassDefFoundError);
		t.put(new String(ConstantPool.JavaLangStringBufferConstantPoolName), this :: getJavaLangStringBuffer);
		t.put(new String(ConstantPool.JavaLangIntegerConstantPoolName), this :: getJavaLangInteger);
		t.put(new String(ConstantPool.JavaLangBooleanConstantPoolName), this :: getJavaLangBoolean);
		t.put(new String(ConstantPool.JavaLangByteConstantPoolName), this :: getJavaLangByte);
		t.put(new String(ConstantPool.JavaLangCharacterConstantPoolName), this :: getJavaLangCharacter);
		t.put(new String(ConstantPool.JavaLangFloatConstantPoolName), this :: getJavaLangFloat);
		t.put(new String(ConstantPool.JavaLangDoubleConstantPoolName), this :: getJavaLangDouble);
		t.put(new String(ConstantPool.JavaLangShortConstantPoolName), this :: getJavaLangShort);
		t.put(new String(ConstantPool.JavaLangLongConstantPoolName), this :: getJavaLangLong);
		t.put(new String(ConstantPool.JavaLangVoidConstantPoolName), this :: getJavaLangVoid);
		t.put(new String(ConstantPool.JavaLangStringConstantPoolName), this :: getJavaLangString);
		t.put(new String(ConstantPool.JavaLangStringBuilderConstantPoolName), this :: getJavaLangStringBuilder);
		t.put(new String(ConstantPool.JavaLangClassConstantPoolName), this :: getJavaLangClass);
		t.put(new String(ConstantPool.JAVALANGREFLECTFIELD_CONSTANTPOOLNAME), this :: getJavaLangReflectField);
		t.put(new String(ConstantPool.JAVALANGREFLECTMETHOD_CONSTANTPOOLNAME), this :: getJavaLangReflectMethod);
		t.put(new String(ConstantPool.JavaUtilIteratorConstantPoolName), this :: getJavaUtilIterator);
		t.put(new String(ConstantPool.JavaLangEnumConstantPoolName), this :: getJavaLangEnum);
		t.put(new String(ConstantPool.JavaLangObjectConstantPoolName), this :: getJavaLangObject);
		return this.commonTypeBindings = t;
	}

	/** Called when a ctor with a late explicit constructor call starts processing statements. */
	public void enterEarlyConstructionContext() {
		classScope().insideEarlyConstructionContext = true;
	}
	/** Called at the end of processing a late explicit constructor call. */
	public void leaveEarlyConstructionContext() {
		classScope().insideEarlyConstructionContext = false;
	}
	/**
	 * Is an instance of targetClass currently being constructed in this scope?
	 * @param targetClass class which is queried for initialization-complete
	 * 		if {@code null} is passed, this scope's enclosingReceiverType is used
	 * @param considerEnclosings if {@code true} also enclosing types or targetClass must be initialized
	 */
	public boolean isInsideEarlyConstructionContext(TypeBinding targetClass, boolean considerEnclosings) {
		return getMatchingUninitializedType(targetClass, considerEnclosings) != null;
	}
	private enum MatchPhase { WITHOUT_SUPERS, WITH_SUPERS }
	private static MatchPhase[] SinglePass = new MatchPhase[] { MatchPhase.WITHOUT_SUPERS };
	public TypeBinding getMatchingUninitializedType(TypeBinding targetClass, boolean considerEnclosings) {
		MatchPhase[] phases = MatchPhase.values();
		if (targetClass == null) {
			targetClass = enclosingReceiverType();
			phases = SinglePass;
		} else if (!(targetClass instanceof ReferenceBinding)) {
			return null;
		}
		// First iteration ignores superclasses, to prefer finding the target in outers, rather than supers.
		// Note on performance: while deeply nested loops look painful, poor-man's measurements showed good results.
		for (Enum<?> phase : phases) {
			// 1. Scope in->out
			ClassScope currentEnclosing = classScope();
			while (currentEnclosing != null) {
				SourceTypeBinding enclosingType = currentEnclosing.referenceContext.binding;
				// 2. targetClass to supers
				TypeBinding currentTarget = targetClass;
				while (currentTarget != null) {
					// 3. enclosing type to supers (in phase 2 only)
					TypeBinding tmpEnclosing = enclosingType;
					while (tmpEnclosing != null) { // this loop is not effective during PHASE.WITHOUT_SUPERS
						if (TypeBinding.equalsEquals(tmpEnclosing, currentTarget.actualType())) {
							if (currentEnclosing.insideEarlyConstructionContext)
								return enclosingType;
							return null;
						}
						if (phase == MatchPhase.WITH_SUPERS)
							tmpEnclosing = tmpEnclosing.superclass();
						else
							break;
					}
					if (!considerEnclosings
							|| (currentTarget instanceof ReferenceBinding currentRefBind && !currentRefBind.hasEnclosingInstanceContext())) {
						break;
					}
					if (currentTarget.isStatic() || currentTarget.isLocalType())
						break;
					currentTarget = currentTarget.enclosingType();
				}
				currentEnclosing = currentEnclosing.parent.classScope();
			}
		}
		return null;
	}

	/**
	 * This method captures the status of early construction context at the declaration
	 * of a lambda expression. This information will be needed during generateCode()
	 * where this (temporary) context is lost.
	 */
	public List<ClassScope> collectClassesBeingInitialized() {
		List<ClassScope> list = null;
		Scope skope = this;
		while (skope != null) {
			if (skope instanceof ClassScope cs && cs.insideEarlyConstructionContext) {
				if (list == null)
					list = new ArrayList<>();
				list.add(cs);
			}
			skope = skope.parent;
		}
		return list;
	}

	public boolean isInStaticContext() {
		MethodScope methodScope = methodScope();
		return methodScope != null && methodScope.isStatic;
	}

	public void include(LocalVariableBinding[] bindings) {
		// `this` is assumed to be populated with bindings.
		if (bindings != null) {
			for (LocalVariableBinding binding : bindings) {
				binding.modifiers &= ~ExtraCompilerModifiers.AccOutOfFlowScope;
			}
		}
	}

	public void exclude(LocalVariableBinding[] bindings) {
		// `this` is assumed to be populated with bindings.
		if (bindings != null) {
			for (LocalVariableBinding binding : bindings) {
				binding.modifiers |= ExtraCompilerModifiers.AccOutOfFlowScope;
			}
		}
	}
}
