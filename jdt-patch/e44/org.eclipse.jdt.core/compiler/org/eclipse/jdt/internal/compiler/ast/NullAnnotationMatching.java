/*******************************************************************************
 * Copyright (c) 2013, 2015 GK Software AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.CaptureBinding;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.RawTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBindingVisitor;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.WildcardBinding;

/**
 * Performs matching of null type annotations.
 * Instances are used to encode result from this analysis.
 * @since 3.10
 */
public class NullAnnotationMatching {
	
	public static final NullAnnotationMatching NULL_ANNOTATIONS_OK = new NullAnnotationMatching(0, FlowInfo.UNKNOWN, null);
	public static final NullAnnotationMatching NULL_ANNOTATIONS_OK_NONNULL = new NullAnnotationMatching(0, FlowInfo.NON_NULL, null);
	public static final NullAnnotationMatching NULL_ANNOTATIONS_UNCHECKED = new NullAnnotationMatching(1, FlowInfo.UNKNOWN, null);
	public static final NullAnnotationMatching NULL_ANNOTATIONS_MISMATCH = new NullAnnotationMatching(2, FlowInfo.UNKNOWN, null);

	public enum CheckMode {
		/** in this mode we check normal assignment compatibility. */
		COMPATIBLE,
		/** in this mode we do not tolerate incompatibly missing annotations on type parameters (for overriding analysis) */
		OVERRIDE,
		/** in this mode we check compatibility of a type argument against the corresponding type parameter. */
		BOUND_CHECK
	}

	/** 0 = OK, 1 = unchecked, 2 = definite mismatch */
	public final int severity;
	
	/** If non-null this field holds the supertype of the provided type which was used for direct matching. */
	public final TypeBinding superTypeHint;
	public final int nullStatus;
	
	public NullAnnotationMatching(int severity, int nullStatus, TypeBinding superTypeHint) {
		this.severity = severity;
		this.superTypeHint = superTypeHint;
		this.nullStatus = nullStatus;
	}

	public boolean isAnyMismatch()      { return this.severity != 0; }
	public boolean isUnchecked()        { return this.severity == 1; }
	public boolean isDefiniteMismatch() { return this.severity == 2; }
	
	public String superTypeHintName(CompilerOptions options, boolean shortNames) {
		return String.valueOf(this.superTypeHint.nullAnnotatedReadableName(options, shortNames));
	}
	
	/** Check null-ness of 'var' against a possible null annotation */
	public static int checkAssignment(BlockScope currentScope, FlowContext flowContext,
									   VariableBinding var, int nullStatus, Expression expression, TypeBinding providedType)
	{
		long lhsTagBits = 0L;
		boolean hasReported = false;
		if (currentScope.compilerOptions().sourceLevel < ClassFileConstants.JDK1_8) {
			lhsTagBits = var.tagBits & TagBits.AnnotationNullMASK;
		} else {
			if (expression instanceof ConditionalExpression && expression.isPolyExpression()) {
				// drill into both branches:
				ConditionalExpression ce = ((ConditionalExpression) expression);
				int status1 = NullAnnotationMatching.checkAssignment(currentScope, flowContext, var, ce.ifTrueNullStatus, ce.valueIfTrue, ce.valueIfTrue.resolvedType);
				int status2 = NullAnnotationMatching.checkAssignment(currentScope, flowContext, var, ce.ifFalseNullStatus, ce.valueIfFalse, ce.valueIfFalse.resolvedType);
				if (status1 == status2)
					return status1;
				return nullStatus; // if both branches disagree use the precomputed & merged nullStatus
			}
			lhsTagBits = var.type.tagBits & TagBits.AnnotationNullMASK;
			NullAnnotationMatching annotationStatus = analyse(var.type, providedType, nullStatus);
			if (annotationStatus.isDefiniteMismatch()) {
				currentScope.problemReporter().nullityMismatchingTypeAnnotation(expression, providedType, var.type, annotationStatus);
				hasReported = true;
			} else if (annotationStatus.isUnchecked()) {
				flowContext.recordNullityMismatch(currentScope, expression, providedType, var.type, nullStatus);
				hasReported = true;
			} else if (annotationStatus.nullStatus != FlowInfo.UNKNOWN) {
				return annotationStatus.nullStatus;
			}
		}
		if (lhsTagBits == TagBits.AnnotationNonNull && nullStatus != FlowInfo.NON_NULL) {
			if (!hasReported)
				flowContext.recordNullityMismatch(currentScope, expression, providedType, var.type, nullStatus);
			return FlowInfo.NON_NULL;
		} else if (lhsTagBits == TagBits.AnnotationNullable && nullStatus == FlowInfo.UNKNOWN) {	// provided a legacy type?
			return FlowInfo.POTENTIALLY_NULL;			// -> use more specific info from the annotation
		}
		return nullStatus;
	}

	/**
	 * Find any mismatches between the two given types, which are caused by null type annotations.
	 * @param requiredType
	 * @param providedType
	 * @param nullStatus we are only interested in NULL or NON_NULL, -1 indicates that we are in a recursion, where flow info is ignored
	 * @return a status object representing the severity of mismatching plus optionally a supertype hint
	 */
	public static NullAnnotationMatching analyse(TypeBinding requiredType, TypeBinding providedType, int nullStatus) {
		return analyse(requiredType, providedType, null, nullStatus, CheckMode.COMPATIBLE);
	}
	/**
	 * Find any mismatches between the two given types, which are caused by null type annotations.
	 * @param requiredType
	 * @param providedType
	 * @param providedSubstitute in inheritance situations this maps the providedType into the realm of the subclass, needed for TVB identity checks.
	 * 		Pass null if not interested in these added checks.
	 * @param nullStatus we are only interested in NULL or NON_NULL, -1 indicates that we are in a recursion, where flow info is ignored
	 * @param mode controls the kind of check performed (see {@link CheckMode}).
	 * @return a status object representing the severity of mismatching plus optionally a supertype hint
	 */
	public static NullAnnotationMatching analyse(TypeBinding requiredType, TypeBinding providedType, TypeBinding providedSubstitute, int nullStatus, CheckMode mode) {
		if (!requiredType.enterRecursiveFunction())
			return NullAnnotationMatching.NULL_ANNOTATIONS_OK;
		try {
			int severity = 0;
			TypeBinding superTypeHint = null;
			NullAnnotationMatching okStatus = NullAnnotationMatching.NULL_ANNOTATIONS_OK;
			if (areSameTypes(requiredType, providedType, providedSubstitute)) {
				if ((requiredType.tagBits & TagBits.AnnotationNonNull) != 0)
					return NullAnnotationMatching.NULL_ANNOTATIONS_OK_NONNULL;
				return okStatus;
			}
			if (mode == CheckMode.BOUND_CHECK && requiredType instanceof TypeVariableBinding) {
				// during bound check against a type variable check the provided type against all upper bounds:
				TypeBinding superClass = requiredType.superclass();
				if (superClass != null && superClass.hasNullTypeAnnotations()) {
					NullAnnotationMatching status = analyse(superClass, providedType, null, nullStatus, mode);
					severity = Math.max(severity, status.severity);
					if (severity == 2)
						return new NullAnnotationMatching(severity, nullStatus, superTypeHint);
				}
				TypeBinding[] superInterfaces = requiredType.superInterfaces();
				if (superInterfaces != null) {
					for (int i = 0; i < superInterfaces.length; i++) {
						if (superInterfaces[i].hasNullTypeAnnotations()) {
							NullAnnotationMatching status = analyse(superInterfaces[i], providedType, null, nullStatus, mode);
							severity = Math.max(severity, status.severity);
							if (severity == 2)
								return new NullAnnotationMatching(severity, nullStatus, superTypeHint);						
						}
					}
				}
			}
			if (requiredType instanceof ArrayBinding) {
				long[] requiredDimsTagBits = ((ArrayBinding)requiredType).nullTagBitsPerDimension;
				if (requiredDimsTagBits != null) {
					int dims = requiredType.dimensions();
					if (requiredType.dimensions() == providedType.dimensions()) {
						long[] providedDimsTagBits = ((ArrayBinding)providedType).nullTagBitsPerDimension;
						if (providedDimsTagBits == null) {
							severity = 1; // required is annotated, provided not, need unchecked conversion
						} else {
							for (int i=0; i<=dims; i++) {
								long requiredBits = validNullTagBits(requiredDimsTagBits[i]);
								long providedBits = validNullTagBits(providedDimsTagBits[i]);
								if (i > 0)
									nullStatus = -1; // don't use beyond the outermost dimension
								severity = Math.max(severity, computeNullProblemSeverity(requiredBits, providedBits, nullStatus, mode == CheckMode.OVERRIDE));
								if (severity == 2)
									return NullAnnotationMatching.NULL_ANNOTATIONS_MISMATCH;
							}
						}
					} else if (providedType.id == TypeIds.T_null) {
						if (dims > 0 && requiredDimsTagBits[0] == TagBits.AnnotationNonNull)
							return NullAnnotationMatching.NULL_ANNOTATIONS_MISMATCH;
					}
				}
			} else if (requiredType.hasNullTypeAnnotations() || providedType.hasNullTypeAnnotations() || requiredType.isTypeVariable()) {
				long requiredBits = requiredNullTagBits(requiredType, mode);
				if (requiredBits != TagBits.AnnotationNullable // nullable lhs accepts everything, ...
						|| nullStatus == -1) // only at detail/recursion even nullable must be matched exactly
				{
					long providedBits = providedNullTagBits(providedType);
					int s = computeNullProblemSeverity(requiredBits, providedBits, nullStatus, mode == CheckMode.OVERRIDE && nullStatus == -1);
					severity = Math.max(severity, s);
					if (severity == 0 && (providedBits & TagBits.AnnotationNonNull) != 0)
						okStatus = NullAnnotationMatching.NULL_ANNOTATIONS_OK_NONNULL;
				}
				if (severity < 2) {
					TypeBinding providedSuper = providedType.findSuperTypeOriginatingFrom(requiredType);
					TypeBinding providedSubstituteSuper = providedSubstitute != null ? providedSubstitute.findSuperTypeOriginatingFrom(requiredType) : null;
					if (providedSuper != providedType) //$IDENTITY-COMPARISON$
						superTypeHint = providedSuper;
					if (requiredType.isParameterizedType()  && providedSuper instanceof ParameterizedTypeBinding) { // TODO(stephan): handle providedType.isRaw()
						TypeBinding[] requiredArguments = ((ParameterizedTypeBinding) requiredType).arguments;
						TypeBinding[] providedArguments = ((ParameterizedTypeBinding) providedSuper).arguments;
						TypeBinding[] providedSubstitutes = (providedSubstituteSuper instanceof ParameterizedTypeBinding) ? ((ParameterizedTypeBinding)providedSubstituteSuper).arguments : null;
						if (requiredArguments != null && providedArguments != null && requiredArguments.length == providedArguments.length) {
							for (int i = 0; i < requiredArguments.length; i++) {
								TypeBinding providedArgSubstitute = providedSubstitutes != null ? providedSubstitutes[i] : null;
								NullAnnotationMatching status = analyse(requiredArguments[i], providedArguments[i], providedArgSubstitute, -1, mode);
								severity = Math.max(severity, status.severity);
								if (severity == 2)
									return new NullAnnotationMatching(severity, nullStatus, superTypeHint);
							}
						}
					}
					TypeBinding requiredEnclosing = requiredType.enclosingType();
					TypeBinding providedEnclosing = providedType.enclosingType();
					if (requiredEnclosing != null && providedEnclosing != null) {
						TypeBinding providedEnclSubstitute = providedSubstitute != null ? providedSubstitute.enclosingType() : null;
						NullAnnotationMatching status = analyse(requiredEnclosing, providedEnclosing, providedEnclSubstitute, -1, mode);
						severity = Math.max(severity, status.severity);
					}
				}
			}
			if (severity == 0)
				return okStatus;
			return new NullAnnotationMatching(severity, nullStatus, superTypeHint);
		} finally {
			requiredType.exitRecursiveFunction();
		}
	}

	/** Are both types identical wrt the unannotated type and any null type annotations? Only unstructured types and captures are considered. */
	protected static boolean areSameTypes(TypeBinding requiredType, TypeBinding providedType, TypeBinding providedSubstitute) {
		if (requiredType == providedType)  //$IDENTITY-COMPARISON$ // short cut for really-really-same types
			return true;
		if (requiredType.isParameterizedType() || requiredType.isArrayType())
			return false; // not analysing details here
		if (TypeBinding.notEquals(requiredType, providedType)) {
			if (requiredType instanceof CaptureBinding) {
				// when providing exactly the lower bound of the required type we're definitely fine:
				TypeBinding lowerBound = ((CaptureBinding)requiredType).lowerBound;
				if (lowerBound != null && areSameTypes(lowerBound, providedType, providedSubstitute))
					return true;
			} else if (requiredType.kind() == Binding.TYPE_PARAMETER && requiredType == providedSubstitute) { //$IDENTITY-COMPARISON$
				return true;
			} else if (providedType instanceof CaptureBinding) {
				// when requiring exactly the upper bound of the provided type we're fine, too:
				TypeBinding upperBound = ((CaptureBinding)providedType).upperBound();
				if (upperBound != null && areSameTypes(requiredType, upperBound, providedSubstitute))
					return true;
			}
			return false;
		}
		return (requiredType.tagBits & TagBits.AnnotationNullMASK) == (providedType.tagBits & TagBits.AnnotationNullMASK);
	}

	// interpreting 'type' as a required type, compute the required null bits
	// we inspect the main type plus bounds of type variables and wildcards
	static long requiredNullTagBits(TypeBinding type, CheckMode mode) {

		long tagBits = type.tagBits & TagBits.AnnotationNullMASK;
		if (tagBits != 0)
			return validNullTagBits(tagBits);

		if (type.isWildcard()) {
			WildcardBinding wildcard = (WildcardBinding)type;
			if (wildcard.boundKind == Wildcard.UNBOUND)
				return 0;
			tagBits = wildcard.bound.tagBits & TagBits.AnnotationNullMASK;
			if (tagBits == 0)
				return 0;
			switch (wildcard.boundKind) {
				case Wildcard.EXTENDS :
					if (tagBits == TagBits.AnnotationNonNull)
						return TagBits.AnnotationNonNull;
					return TagBits.AnnotationNullMASK; // wildcard accepts @Nullable or better
				case Wildcard.SUPER :
					if (tagBits == TagBits.AnnotationNullable)
						return TagBits.AnnotationNullable;
					return TagBits.AnnotationNullMASK; // wildcard accepts @NonNull or worse
			}
			return 0;
		} 
		
		if (type.isTypeVariable()) {
			// assume we must require @NonNull, unless lower @Nullable bound
			// (annotation directly on the TV has already been checked above)
			if (type.isCapture()) {
				TypeBinding lowerBound = ((CaptureBinding) type).lowerBound;
				if (lowerBound != null) {
					tagBits = lowerBound.tagBits & TagBits.AnnotationNullMASK;
					if (tagBits == TagBits.AnnotationNullable)
						return TagBits.AnnotationNullable; // type cannot require @NonNull
				}
			}
			if (mode != CheckMode.BOUND_CHECK) // no pessimistic checks during boundcheck (we *have* the instantiation)
				return TagBits.AnnotationNonNull; // instantiation could require @NonNull
		}

		return 0;
	}

	// interpreting 'type' as a provided type, compute the provide null bits
	// we inspect the main type plus bounds of type variables and wildcards
	static long providedNullTagBits(TypeBinding type) {

		long tagBits = type.tagBits & TagBits.AnnotationNullMASK;
		if (tagBits != 0)
			return validNullTagBits(tagBits);
		
		if (type.isWildcard()) { // wildcard can be 'provided' during inheritance checks
			WildcardBinding wildcard = (WildcardBinding)type;
			if (wildcard.boundKind == Wildcard.UNBOUND)
				return 0;
			tagBits = wildcard.bound.tagBits & TagBits.AnnotationNullMASK;
			if (tagBits == 0)
				return 0;
			switch (wildcard.boundKind) {
				case Wildcard.EXTENDS :
					if (tagBits == TagBits.AnnotationNonNull)
						return TagBits.AnnotationNonNull;
					return TagBits.AnnotationNullMASK; // @Nullable or better
				case Wildcard.SUPER :
					if (tagBits == TagBits.AnnotationNullable)
						return TagBits.AnnotationNullable;
					return TagBits.AnnotationNullMASK; // @NonNull or worse
			}
			return 0;
		}
	
		if (type.isTypeVariable()) { // incl. captures
			TypeVariableBinding typeVariable = (TypeVariableBinding)type;
			boolean haveNullBits = false;
			if (typeVariable.isCapture()) {
				TypeBinding lowerBound = ((CaptureBinding) typeVariable).lowerBound;
				if (lowerBound != null) {
					tagBits = lowerBound.tagBits & TagBits.AnnotationNullMASK;
					if (tagBits == TagBits.AnnotationNullable)
						return TagBits.AnnotationNullable; // cannot be @NonNull
					haveNullBits |= (tagBits != 0);
				}
			}
			if (typeVariable.firstBound != null) {
				long boundBits = typeVariable.firstBound.tagBits & TagBits.AnnotationNullMASK;
				if (boundBits == TagBits.AnnotationNonNull)
					return TagBits.AnnotationNonNull; // cannot be @Nullable
				haveNullBits |= (boundBits != 0);
			}
			if (haveNullBits)
				return TagBits.AnnotationNullMASK; // could be either, can only match to a wildcard accepting both
		}

		return 0;
	}

	public static long validNullTagBits(long bits) {
		bits &= TagBits.AnnotationNullMASK;
		return bits == TagBits.AnnotationNullMASK ? 0 : bits;
	}
	
	/** Provided that both types are {@link TypeBinding#equalsEquals}, return the one that is more likely to show null at runtime. */
	public static TypeBinding moreDangerousType(TypeBinding one, TypeBinding two) {
		if (one == null) return null;
		long oneNullBits = validNullTagBits(one.tagBits);
		long twoNullBits = validNullTagBits(two.tagBits);
		if (oneNullBits != twoNullBits) {
			if (oneNullBits == TagBits.AnnotationNullable)
				return one;			// nullable is dangerous
			if (twoNullBits == TagBits.AnnotationNullable)
				return two;			// nullable is dangerous
			// below this point we have unknown vs. nonnull, which is which?
			if (oneNullBits == 0)
				return one;			// unknown is more dangerous than nonnull
			return two;				// unknown is more dangerous than nonnull
		} else if (one != two) { //$IDENTITY-COMPARISON$
			if (analyse(one, two, -1).isAnyMismatch())
				return two;			// two doesn't snugly fit into one, so it must be more dangerous
		}
		return one;
	}

	private static int computeNullProblemSeverity(long requiredBits, long providedBits, int nullStatus, boolean strict) {
		if ((requiredBits != 0 || strict) && requiredBits != providedBits) {
			if (requiredBits == TagBits.AnnotationNonNull && nullStatus == FlowInfo.NON_NULL) {
				return 0; // OK by flow analysis
			}
			if (requiredBits == TagBits.AnnotationNullMASK)
				return 0; // OK since LHS accepts either
			if (providedBits != 0) {
				return 2; // mismatching annotations
			} else {
				return 1; // need unchecked conversion regarding type detail
			}
		}
		return 0; // OK by tagBits
	}

	/**
	 * After a method has substituted type parameters, check if this resulted in any contradictory null annotations.
	 * Problems are either reported directly (if scope != null) or by returning a ProblemMethodBinding.
	 */
	public static MethodBinding checkForContraditions(
			final MethodBinding method, final InvocationSite invocationSite, final Scope scope) {
		
		class SearchContradictions extends TypeBindingVisitor {
			ReferenceBinding typeWithContradiction;
			@Override
			public boolean visit(ReferenceBinding referenceBinding) {
				if ((referenceBinding.tagBits & TagBits.AnnotationNullMASK) == TagBits.AnnotationNullMASK) {
					this.typeWithContradiction = referenceBinding;
					return false;
				}
				return true;
			}
			@Override
			public boolean visit(TypeVariableBinding typeVariable) {
				return visit((ReferenceBinding)typeVariable);
			}
			@Override
			public boolean visit(RawTypeBinding rawType) {
				return visit((ReferenceBinding)rawType);
			}
		}

		SearchContradictions searchContradiction = new SearchContradictions();
		TypeBindingVisitor.visit(searchContradiction, method.returnType);
		if (searchContradiction.typeWithContradiction != null) {
			if (scope == null)
				return new ProblemMethodBinding(method, method.selector, method.parameters, ProblemReasons.ContradictoryNullAnnotations);
			scope.problemReporter().contradictoryNullAnnotationsInferred(method, invocationSite);
			// note: if needed, we might want to update the method by removing the contradictory annotations??
			return method;
		}

		Expression[] arguments = null;
		if (invocationSite instanceof Invocation)
			arguments = ((Invocation)invocationSite).arguments();
		for (int i = 0; i < method.parameters.length; i++) {
			TypeBindingVisitor.visit(searchContradiction, method.parameters[i]);
			if (searchContradiction.typeWithContradiction != null) {
				if (scope == null)
					return new ProblemMethodBinding(method, method.selector, method.parameters, ProblemReasons.ContradictoryNullAnnotations);
				if (arguments != null && i < arguments.length)
					scope.problemReporter().contradictoryNullAnnotationsInferred(method, arguments[i]);
				else
					scope.problemReporter().contradictoryNullAnnotationsInferred(method, invocationSite);
				return method;
			}
		}
		return method;
	}
}
