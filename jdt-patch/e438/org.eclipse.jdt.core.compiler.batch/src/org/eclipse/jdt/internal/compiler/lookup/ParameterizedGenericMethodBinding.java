/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
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
 *								bug 186342 - [compiler][null] Using annotations for null checking
 *								bug 395002 - Self bound generic class doesn't resolve bounds properly for wildcards for certain parametrisation.
 *								Bug 415043 - [1.8][null] Follow-up re null type annotations after bug 392099
 *								bug 413958 - Function override returning inherited Generic Type
 *								Bug 400874 - [1.8][compiler] Inference infrastructure should evolve to meet JLS8 18.x (Part G of JSR335 spec)
 *								Bug 424710 - [1.8][compiler] CCE in SingleNameReference.localVariableBinding
 *								Bug 423505 - [1.8] Implement "18.5.4 More Specific Method Inference"
 *								Bug 427438 - [1.8][compiler] NPE at org.eclipse.jdt.internal.compiler.ast.ConditionalExpression.generateCode(ConditionalExpression.java:280)
 *								Bug 418743 - [1.8][null] contradictory annotations on invocation of generic method not reported
 *								Bug 416182 - [1.8][compiler][null] Contradictory null annotations not rejected
 *								Bug 429958 - [1.8][null] evaluate new DefaultLocation attribute of @NonNullByDefault
 *								Bug 434602 - Possible error with inferred null annotations leading to contradictory null annotations
 *								Bug 434483 - [1.8][compiler][inference] Type inference not picked up with method reference
 *								Bug 446442 - [1.8] merge null annotations from super methods
 *								Bug 457079 - Regression: type inference
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.Invocation;
import org.eclipse.jdt.internal.compiler.ast.NullAnnotationMatching;
import org.eclipse.jdt.internal.compiler.ast.ReferenceExpression;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * Binding denoting a generic method after type parameter substitutions got performed.
 * On parameterized type bindings, all methods got substituted, regardless whether
 * their signature did involve generics or not, so as to get the proper declaringClass for
 * these methods.
 */
public class ParameterizedGenericMethodBinding extends ParameterizedMethodBinding implements Substitution {

    public TypeBinding[] typeArguments;
    protected LookupEnvironment environment;
    public boolean wasInferred; // only set to true for instances resulting from method invocation inferrence
    public boolean isRaw; // set to true for method behaving as raw for substitution purpose
	public boolean inferredWithUncheckedConversion;
	public TypeBinding targetType; // used to distinguish different PGMB created for different target types (needed because inference contexts are remembered per PGMB)

	/**
	 * Perform inference of generic method type parameters and/or expected type
	 * <p>
	 * In 1.8+ if the expected type is not yet available due to this call being an argument to an outer call which is not overload-resolved yet,
	 * the returned method binding will be a PolyParameterizedGenericMethodBinding.
	 * </p>
	 */
	public static MethodBinding computeCompatibleMethod(MethodBinding originalMethod, TypeBinding[] arguments, Scope scope,	InvocationSite invocationSite)
	{
		LookupEnvironment environment = scope.environment();
		if(environment.globalOptions.isAnnotationBasedNullAnalysisEnabled) {
			ImplicitNullAnnotationVerifier.ensureNullnessIsKnown(originalMethod, scope);
		}
		ParameterizedGenericMethodBinding methodSubstitute;
		TypeVariableBinding[] typeVariables = originalMethod.typeVariables;
		TypeBinding[] substitutes = invocationSite.genericTypeArguments();
		computeSubstitutes: {
			if (substitutes != null) {
				// explicit type arguments got supplied
				if (substitutes.length != typeVariables.length) {
			        // incompatible due to wrong arity
			        return new ProblemMethodBinding(originalMethod, originalMethod.selector, substitutes, ProblemReasons.TypeParameterArityMismatch);
				}
				methodSubstitute = environment.createParameterizedGenericMethod(originalMethod, substitutes);
				break computeSubstitutes;
			}
			// perform type argument inference (15.12.2.7)
			// initializes the map of substitutes (var --> type[][]{ equal, extends, super}
			return computeCompatibleMethod18(originalMethod, arguments, scope, invocationSite);
		}

		/* bounds check: https://bugs.eclipse.org/bugs/show_bug.cgi?id=242159, Inferred types may contain self reference
		   in formal bounds. If "T extends I<T>" is a original type variable and T was inferred to be I<T> due possibly
		   to under constraints and resultant glb application per 15.12.2.8, using this.typeArguments to drive the bounds
		   check against itself is doomed to fail. For, the variable T would after substitution be I<I<T>> and would fail
		   bounds check against I<T>. Use the inferred types from the context directly - see that there is one round of
		   extra substitution that has taken place to properly substitute a remaining unresolved variable which also appears
		   in a formal bound  (So we really have a bounds mismatch between I<I<T>> and I<I<I<T>>>, in the absence of a fix.)
		*/
		Substitution substitution = methodSubstitute;
		for (int i = 0, length = typeVariables.length; i < length; i++) {
		    TypeVariableBinding typeVariable = typeVariables[i];
		    TypeBinding substitute = methodSubstitute.typeArguments[i]; // retain for diagnostics
		    /* https://bugs.eclipse.org/bugs/show_bug.cgi?id=375394, To avoid spurious bounds check failures due to circularity in formal bounds,
		       we should eliminate only the lingering embedded type variable references after substitution, not alien type variable references
		       that constitute the inference per se.
		     */
		    TypeBinding substituteForChecks;
		    if (substitute instanceof TypeVariableBinding) {
		    	substituteForChecks = substitute;
		    } else {
		    	substituteForChecks = Scope.substitute(new LingeringTypeVariableEliminator(typeVariables, null, scope), substitute); // while using this for bounds check
		    }

			switch (typeVariable.boundCheck(substitution, substituteForChecks, scope, null)) {
				case MISMATCH :
			        // incompatible due to bound check
					int argLength = arguments.length;
					TypeBinding[] augmentedArguments = new TypeBinding[argLength + 2]; // append offending substitute and typeVariable
					System.arraycopy(arguments, 0, augmentedArguments, 0, argLength);
					augmentedArguments[argLength] = substitute;
					augmentedArguments[argLength+1] = typeVariable;
			        return new ProblemMethodBinding(methodSubstitute, originalMethod.selector, augmentedArguments, ProblemReasons.ParameterBoundMismatch);
				case UNCHECKED :
					// tolerate unchecked bounds
					methodSubstitute.tagBits |= TagBits.HasUncheckedTypeArgumentForBoundCheck;
					break;
				default:
					break;
			}
		}
		if (invocationSite instanceof Invocation) {
			InferenceContext18.updateInnerDiamonds(methodSubstitute, ((Invocation) invocationSite).arguments());
		}
		// check presence of unchecked argument conversion a posteriori (15.12.2.6)
		return methodSubstitute;
	}

	public static MethodBinding computeCompatibleMethod18(MethodBinding originalMethod, TypeBinding[] arguments, final Scope scope, InvocationSite invocationSite) {

		TypeVariableBinding[] typeVariables = originalMethod.typeVariables;
		if (invocationSite.checkingPotentialCompatibility()) {
			// Not interested in a solution, only that there could potentially be one.
			return scope.environment().createParameterizedGenericMethod(originalMethod, typeVariables);
		}

		ParameterizedGenericMethodBinding methodSubstitute = null;
		InferenceContext18 infCtx18 = invocationSite.freshInferenceContext(scope);
		if (infCtx18 == null)
			return originalMethod;  // per parity with old F & G integration.
		TypeBinding[] parameters = originalMethod.parameters;
		CompilerOptions compilerOptions = scope.compilerOptions();
		boolean invocationTypeInferred = false;
		boolean requireBoxing = false;
		boolean allArgumentsAreProper = true;

		// See if we should start in loose inference mode.
		TypeBinding [] argumentsCopy = new TypeBinding[arguments.length];
		for (int i = 0, length = arguments.length, parametersLength = parameters.length ; i < length; i++) {
			TypeBinding parameter = i < parametersLength ? parameters[i] : parameters[parametersLength - 1];
			final TypeBinding argument = arguments[i];
			allArgumentsAreProper &= argument.isProperType(true);
			if (argument.isPrimitiveType() != parameter.isPrimitiveType()) { // Scope.cCM incorrectly but harmlessly uses isBaseType which answers true for null.
				argumentsCopy[i] = scope.environment().computeBoxingType(argument);
				requireBoxing = true; // can't be strict mode, needs at least loose.
			} else {
				argumentsCopy[i] = argument;
			}
		}
		arguments = argumentsCopy; // either way, this allows the engine to update arguments without harming the callers.

		LookupEnvironment environment = scope.environment();
		InferenceContext18 previousContext = environment.currentInferenceContext;
		if (previousContext == null)
			environment.currentInferenceContext = infCtx18;
		try {
			BoundSet provisionalResult = null;
			BoundSet result = null;
			// ---- 18.5.1 (Applicability): ----
			final boolean isPolyExpression = invocationSite instanceof Expression &&   ((Expression) invocationSite).isTrulyExpression() &&
					((Expression)invocationSite).isPolyExpression(originalMethod);
			boolean isDiamond = isPolyExpression && originalMethod.isConstructor();
			boolean isInexactVarargsInference = false;
			if (arguments.length == parameters.length) {
				infCtx18.inferenceKind = requireBoxing ? InferenceContext18.CHECK_LOOSE : InferenceContext18.CHECK_STRICT; // engine may still slip into loose mode and adjust level.
				infCtx18.inferInvocationApplicability(originalMethod, arguments, isDiamond);
				if (InferenceContext18.DEBUG) {
					System.out.println("Infer applicability for "+invocationSite+":\n"+infCtx18); //$NON-NLS-1$ //$NON-NLS-2$
				}
				result = infCtx18.solve(true);
				if (InferenceContext18.DEBUG) {
					System.out.println("Result=\n"+result); //$NON-NLS-1$
				}
				isInexactVarargsInference = (result != null && originalMethod.isVarargs() && !allArgumentsAreProper);
			}
			if (result == null && originalMethod.isVarargs()) {
				// check for variable-arity applicability
				infCtx18 = invocationSite.freshInferenceContext(scope); // start over
				infCtx18.inferenceKind = InferenceContext18.CHECK_VARARG;
				infCtx18.inferInvocationApplicability(originalMethod, arguments, isDiamond);
				if (InferenceContext18.DEBUG) {
					System.out.println("Infer varargs applicability for "+invocationSite+":\n"+infCtx18); //$NON-NLS-1$ //$NON-NLS-2$
				}
				result = infCtx18.solve(true);
				if (InferenceContext18.DEBUG) {
					System.out.println("Result=\n"+result); //$NON-NLS-1$
				}
			}
			if (result == null)
				return null;
			if (infCtx18.isResolved(result)) {
				infCtx18.stepCompleted = InferenceContext18.APPLICABILITY_INFERRED;
			} else {
				return null;
			}
			// Applicability succeeded, proceed to infer invocation type, if possible.
			TypeBinding expectedType = invocationSite.invocationTargetType();
			boolean hasReturnProblem = false;
			if (expectedType != null || !invocationSite.getExpressionContext().definesTargetType() || !isPolyExpression) {
				// ---- 18.5.2 (Invocation type): ----
				provisionalResult = result;
				result = infCtx18.inferInvocationType(expectedType, invocationSite, originalMethod);
				if (InferenceContext18.DEBUG) {
					System.out.println("Infer invocation type for "+invocationSite+ " with target " //$NON-NLS-1$ //$NON-NLS-2$
							+(expectedType == null ? "<no type>" : expectedType.debugName())); //$NON-NLS-1$
					System.out.println("Result=\n"+result); //$NON-NLS-1$
				}
				invocationTypeInferred = infCtx18.stepCompleted == InferenceContext18.TYPE_INFERRED_FINAL;
				hasReturnProblem |= result == null;
				if (hasReturnProblem)
					result = provisionalResult; // let's prefer a type error regarding the return type over reporting no match at all
			}
			if (result != null) {
				// assemble the solution etc:
				TypeBinding[] solutions = infCtx18.getSolutions(typeVariables, invocationSite, result);
				if (solutions != null) {
					methodSubstitute = scope.environment().createParameterizedGenericMethod(originalMethod, solutions, infCtx18.usesUncheckedConversion, hasReturnProblem, expectedType);
					if (InferenceContext18.DEBUG) {
						System.out.println("Method substitute: "+methodSubstitute); //$NON-NLS-1$
					}
					if (invocationSite instanceof Invocation && allArgumentsAreProper && (expectedType == null || expectedType.isProperType(true)))
						infCtx18.forwardResults(result, (Invocation) invocationSite, methodSubstitute, expectedType);
					try {
						if (infCtx18.missingType != null
								&& (expectedType == null
									|| (expectedType.tagBits & TagBits.HasMissingType) == 0)) { // here the context will have reported an error already, so keep quiet
							ProblemMethodBinding problemMethod = new ProblemMethodBinding(originalMethod, originalMethod.selector, parameters, ProblemReasons.MissingTypeInSignature);
							problemMethod.missingType = infCtx18.missingType;
							return problemMethod;
						}
						if (hasReturnProblem) { // illegally working from the provisional result?
							MethodBinding problemMethod = infCtx18.getReturnProblemMethodIfNeeded(expectedType, methodSubstitute);
							if (problemMethod instanceof ProblemMethodBinding) {
								if (InferenceContext18.DEBUG) {
									System.out.println("Inference reports problem method "+problemMethod); //$NON-NLS-1$
								}
								return problemMethod;
							}
						}
						if (invocationTypeInferred) {
							if (compilerOptions.isAnnotationBasedNullAnalysisEnabled)
								NullAnnotationMatching.checkForContradictions(methodSubstitute, invocationSite, scope);
							MethodBinding problemMethod = methodSubstitute.boundCheck18(scope, arguments, invocationSite);
							if (problemMethod != null) {
								if (InferenceContext18.DEBUG) {
									System.out.println("Bound check after inference failed: "+problemMethod); //$NON-NLS-1$
								}
								return problemMethod;
							}
						} else {
							methodSubstitute = new PolyParameterizedGenericMethodBinding(methodSubstitute);
							if (InferenceContext18.DEBUG) {
								System.out.println("PolyParameterizedGenericMethodBinding: "+methodSubstitute); //$NON-NLS-1$
							}
						}
					} finally {
						infCtx18.setInexactVarargsInference(isInexactVarargsInference);
						if (!infCtx18.hasPrematureOverloadResolution()) {
							if (invocationSite instanceof Invocation)
								((Invocation) invocationSite).registerInferenceContext(methodSubstitute, infCtx18); // keep context so we can finish later
							else if (invocationSite instanceof ReferenceExpression)
								((ReferenceExpression) invocationSite).registerInferenceContext(methodSubstitute, infCtx18); // keep context so we can finish later
						}
					}
					return methodSubstitute;
				}
			}
			return null;
		} catch (InferenceFailureException e) {
			// FIXME stop-gap measure
			scope.problemReporter().genericInferenceError(e.getMessage(), invocationSite);
			return null;
		} finally {
			environment.currentInferenceContext = previousContext;
		}
	}

	MethodBinding boundCheck18(Scope scope, TypeBinding[] arguments, InvocationSite site) {
		Substitution substitution = this;
		ParameterizedGenericMethodBinding methodSubstitute = this;
		TypeVariableBinding[] originalTypeVariables = this.originalMethod.typeVariables;
		// mostly original extract from above, TODO: remove stuff that's no longer needed in 1.8+
		for (int i = 0, length = originalTypeVariables.length; i < length; i++) {
		    TypeVariableBinding typeVariable = originalTypeVariables[i];
		    TypeBinding substitute = methodSubstitute.typeArguments[i]; // retain for diagnostics

			ASTNode location = site instanceof ASTNode ? (ASTNode) site : null;
			switch (typeVariable.boundCheck(substitution, substitute, scope, location)) {
				case MISMATCH :
			        // incompatible due to bound check
					int argLength = arguments.length;
					TypeBinding[] augmentedArguments = new TypeBinding[argLength + 2]; // append offending substitute and typeVariable
					System.arraycopy(arguments, 0, augmentedArguments, 0, argLength);
					augmentedArguments[argLength] = substitute;
					augmentedArguments[argLength+1] = typeVariable;
			        return new ProblemMethodBinding(methodSubstitute, this.originalMethod.selector, augmentedArguments, ProblemReasons.ParameterBoundMismatch);
				case UNCHECKED :
					// tolerate unchecked bounds
					methodSubstitute.tagBits |= TagBits.HasUncheckedTypeArgumentForBoundCheck;
					break;
				default:
					break;
			}
		}
		return null;
	}

	/**
	 * Create raw generic method for raw type (double substitution from type vars with raw type arguments, and erasure of method variables)
	 * Only invoked for non-static generic methods of raw type
	 */
	public ParameterizedGenericMethodBinding(MethodBinding originalMethod, RawTypeBinding rawType, LookupEnvironment environment) {
		TypeVariableBinding[] originalVariables = originalMethod.typeVariables;
		int length = originalVariables.length;
		TypeBinding[] rawArguments = new TypeBinding[length];
		for (int i = 0; i < length; i++) {
			rawArguments[i] =  environment.convertToRawType(originalVariables[i].erasure(), false /*do not force conversion of enclosing types*/);
		}
	    this.isRaw = true;
	    this.tagBits = originalMethod.tagBits;
	    this.environment = environment;
		this.modifiers = originalMethod.modifiers;
		this.selector = originalMethod.selector;
		this.declaringClass = rawType == null ? originalMethod.declaringClass : rawType;
	    this.typeVariables = Binding.NO_TYPE_VARIABLES;
	    this.typeArguments = rawArguments;
	    this.originalMethod = originalMethod;
		boolean ignoreRawTypeSubstitution = rawType == null || originalMethod.isStatic();
	    this.parameters = Scope.substitute(this, ignoreRawTypeSubstitution
	    									? originalMethod.parameters // no substitution if original was static
	    									: Scope.substitute(rawType, originalMethod.parameters));
	    this.thrownExceptions = Scope.substitute(this, 	ignoreRawTypeSubstitution
	    									? originalMethod.thrownExceptions // no substitution if original was static
	    									: Scope.substitute(rawType, originalMethod.thrownExceptions));
	    // error case where exception type variable would have been substituted by a non-reference type (207573)
	    if (this.thrownExceptions == null) this.thrownExceptions = Binding.NO_EXCEPTIONS;
	    this.returnType = Scope.substitute(this, ignoreRawTypeSubstitution
	    									? originalMethod.returnType // no substitution if original was static
	    									: Scope.substitute(rawType, originalMethod.returnType));
	    this.wasInferred = false; // not resulting from method invocation inferrence
	    this.parameterFlowBits = originalMethod.parameterFlowBits;
	    this.defaultNullness = originalMethod.defaultNullness;
	}

    /**
     * Create method of parameterized type, substituting original parameters with type arguments.
     */
	public ParameterizedGenericMethodBinding(MethodBinding originalMethod, TypeBinding[] typeArguments, LookupEnvironment environment, boolean inferredWithUncheckConversion, boolean hasReturnProblem, TypeBinding targetType) {
	    this.environment = environment;
		this.inferredWithUncheckedConversion = inferredWithUncheckConversion;
		this.targetType = targetType;
		this.modifiers = originalMethod.modifiers;
		this.selector = originalMethod.selector;
		this.declaringClass = originalMethod.declaringClass;
		if (inferredWithUncheckConversion && originalMethod.isConstructor() && this.declaringClass.isParameterizedType()) {
			this.declaringClass = (ReferenceBinding) environment.convertToRawType(this.declaringClass.erasure(), false); // for diamond invocations
		}
	    this.typeVariables = Binding.NO_TYPE_VARIABLES;
	    this.typeArguments = typeArguments;
	    this.isRaw = false;
	    this.tagBits = originalMethod.tagBits;
	    this.originalMethod = originalMethod;
	    this.parameters = Scope.substitute(this, originalMethod.parameters);
	    // error case where exception type variable would have been substituted by a non-reference type (207573)
	    if (inferredWithUncheckConversion) { // JSL 18.5.2: "If unchecked conversion was necessary..."
	    	this.returnType = getErasure18_5_2(originalMethod.returnType, environment);
	    	this.thrownExceptions = new ReferenceBinding[originalMethod.thrownExceptions.length];
	    	for (int i = 0; i < originalMethod.thrownExceptions.length; i++) {
	    		this.thrownExceptions[i] = (ReferenceBinding) getErasure18_5_2(originalMethod.thrownExceptions[i], environment);
			}
	    } else {
	    	this.returnType = Scope.substitute(this, originalMethod.returnType);
	    	this.thrownExceptions = Scope.substitute(this, originalMethod.thrownExceptions);
	    }
	    if (this.thrownExceptions == null) this.thrownExceptions = Binding.NO_EXCEPTIONS;
		checkMissingType: {
			if ((this.tagBits & TagBits.HasMissingType) != 0)
				break checkMissingType;
			if ((this.returnType.tagBits & TagBits.HasMissingType) != 0) {
				this.tagBits |=  TagBits.HasMissingType;
				break checkMissingType;
			}
			for (int i = 0, max = this.parameters.length; i < max; i++) {
				if ((this.parameters[i].tagBits & TagBits.HasMissingType) != 0) {
					this.tagBits |=  TagBits.HasMissingType;
					break checkMissingType;
				}
			}
			for (int i = 0, max = this.thrownExceptions.length; i < max; i++) {
				if ((this.thrownExceptions[i].tagBits & TagBits.HasMissingType) != 0) {
					this.tagBits |=  TagBits.HasMissingType;
					break checkMissingType;
				}
			}
		}
	    this.wasInferred = true;// resulting from method invocation inferrence
	    this.parameterFlowBits = originalMethod.parameterFlowBits;
	    this.defaultNullness = originalMethod.defaultNullness;
	    // special case: @NonNull for a parameter that is inferred to 'null' is encoded the old way
	    // because we cannot (and don't want to) add type annotations to NullTypeBinding.
	    int len = this.parameters.length;
	    for (int i = 0; i < len; i++) {
	    	if (this.parameters[i] == TypeBinding.NULL) {
	    		long nullBits = originalMethod.parameters[i].tagBits & TagBits.AnnotationNullMASK;
	    		if (nullBits == TagBits.AnnotationNonNull) {
	    			if (this.parameterFlowBits == null)
	    				this.parameterFlowBits = new byte[len];
	    			this.parameterFlowBits[i] |= PARAM_NONNULL;
	    		}
	    	}
	    }
	}

	TypeBinding getErasure18_5_2(TypeBinding type, LookupEnvironment env) {
		// opportunistic interpretation of (JLS 18.5.2):
		// "If unchecked conversion was necessary ..., then ...
		// the return type and thrown types of the invocation type of m are given by
		// the erasure of the return type and thrown types of m's type."
		type = Scope.substitute(this, type); // compliant with Bug JDK-8135087: Erasure for unchecked invocation happens after inference
		return env.convertToRawType(type.erasure(), true);
	}

	/*
	 * parameterizedDeclaringUniqueKey dot selector originalMethodGenericSignature percent typeArguments
	 * p.X<U> { <T> void bar(T t, U u) { new X<String>().bar(this, "") } } --> Lp/X<Ljava/lang/String;>;.bar<T:Ljava/lang/Object;>(TT;Ljava/lang/String;)V%<Lp/X;>
	 */
	@Override
	public char[] computeUniqueKey(boolean isLeaf) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(this.originalMethod.computeUniqueKey(false/*not a leaf*/));
		buffer.append('%');
		buffer.append('<');
		if (!this.isRaw) {
			int length = this.typeArguments.length;
			for (int i = 0; i < length; i++) {
				TypeBinding typeArgument = this.typeArguments[i];
				buffer.append(typeArgument.computeUniqueKey(false/*not a leaf*/));
			}
		}
		buffer.append('>');
		int resultLength = buffer.length();
		char[] result = new char[resultLength];
		buffer.getChars(0, resultLength, result, 0);
		return result;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.Substitution#environment()
	 */
	@Override
	public LookupEnvironment environment() {
		return this.environment;
	}
	/**
	 * Returns true if some parameters got substituted.
	 * NOTE: generic method invocation delegates to its declaring method (could be a parameterized one)
	 */
	@Override
	public boolean hasSubstitutedParameters() {
		// generic parameterized method can represent either an invocation or a raw generic method
		if (this.wasInferred)
			return this.originalMethod.hasSubstitutedParameters();
		return super.hasSubstitutedParameters();
	}
	/**
	 * Returns true if the return type got substituted.
	 * NOTE: generic method invocation delegates to its declaring method (could be a parameterized one)
	 */
	@Override
	public boolean hasSubstitutedReturnType() {
		if (this.wasInferred)
			return this.originalMethod.hasSubstitutedReturnType();
		return super.hasSubstitutedReturnType();
	}

	@Override
	public boolean isParameterizedGeneric() {
		return true;
	}

	/* https://bugs.eclipse.org/bugs/show_bug.cgi?id=347600 && https://bugs.eclipse.org/bugs/show_bug.cgi?id=242159
	   Sometimes due to recursion/circularity in formal bounds, even *published bounds* fail bound check. We need to
	   break the circularity/self reference in order not to be overly strict during type equivalence checks.
	   See also http://bugs.sun.com/view_bug.do?bug_id=6932571
	 */
	private static class LingeringTypeVariableEliminator implements Substitution {

		final private TypeVariableBinding [] variables;
		final private TypeBinding [] substitutes; // when null, substitute type variables by unbounded wildcard
		final private Scope scope;

		/**
		 * @param substitutes when null, substitute type variable by unbounded wildcard
		 */
		public LingeringTypeVariableEliminator(TypeVariableBinding [] variables, TypeBinding [] substitutes, Scope scope) {
			this.variables = variables;
			this.substitutes = substitutes;
			this.scope = scope;
		}
		// With T mapping to I<T>, answer of I<?>, when given T, having eliminated the circularity/self reference.
		@Override
		public TypeBinding substitute(TypeVariableBinding typeVariable) {
			if (typeVariable.rank >= this.variables.length || TypeBinding.notEquals(this.variables[typeVariable.rank], typeVariable)) {   // not kosher, don't touch.
				return typeVariable;
			}
			if (this.substitutes != null) {
				return Scope.substitute(new LingeringTypeVariableEliminator(this.variables, null, this.scope), this.substitutes[typeVariable.rank]);
			}
			ReferenceBinding genericType = (ReferenceBinding) (typeVariable.declaringElement instanceof ReferenceBinding ? typeVariable.declaringElement : null);
			return this.scope.environment().createWildcard(genericType, typeVariable.rank, null, null, Wildcard.UNBOUND, typeVariable.getTypeAnnotations());
		}

		@Override
		public LookupEnvironment environment() {
			return this.scope.environment();
		}

		@Override
		public boolean isRawSubstitution() {
			return false;
		}
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.Substitution#isRawSubstitution()
	 */
	@Override
	public boolean isRawSubstitution() {
		return this.isRaw;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.Substitution#substitute(org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding)
	 */
	@Override
	public TypeBinding substitute(TypeVariableBinding originalVariable) {
        TypeVariableBinding[] variables = this.originalMethod.typeVariables;
        int length = variables.length;
        // check this variable can be substituted given parameterized type
        if (originalVariable.rank < length && TypeBinding.equalsEquals(variables[originalVariable.rank], originalVariable)) {
        	TypeBinding substitute = this.typeArguments[originalVariable.rank];
        	return originalVariable.combineTypeAnnotations(substitute);
        }
	    return originalVariable;
	}

	@Override
	public MethodBinding genericMethod() {
		if (this.isRaw) // mostSpecificMethodBinding() would need inference, but that doesn't work well for raw methods
			return this; // -> prefer traditional comparison using the substituted method
		return this.originalMethod;
	}
}
