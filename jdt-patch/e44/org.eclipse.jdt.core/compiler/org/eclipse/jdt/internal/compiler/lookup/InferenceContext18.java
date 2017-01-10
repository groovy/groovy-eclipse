/*******************************************************************************
 * Copyright (c) 2013, 2014 GK Software AG, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *     IBM Corporation - Bug fixes
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FunctionalExpression;
import org.eclipse.jdt.internal.compiler.ast.Invocation;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.NullAnnotationMatching;
import org.eclipse.jdt.internal.compiler.ast.ReferenceExpression;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.util.Sorting;

/**
 * Main class for new type inference as per JLS8 sect 18.
 * Keeps contextual state and drives the algorithm.
 * 
 * <h2>Inference Basics</h2>
 * <ul>
 * <li>18.1.1 Inference variables: {@link InferenceVariable}</li>
 * <li>18.1.2 Constraint Formulas: subclasses of {@link ConstraintFormula}</li>
 * <li>18.1.3 Bounds: {@link TypeBound}<br/>
 * 	Capture bounds are directly captured in {@link BoundSet#captures}, throws-bounds in {@link BoundSet#inThrows}.<br/>
 * 	Also: {@link BoundSet}: main state during inference.</li>
 * </ul>
 * Each instance of {@link InferenceContext18} manages instances of the above and coordinates the inference process.
 * <h3>Queries and utilities</h3>
 * <ul>
 * <li>{@link TypeBinding#isProperType(boolean)}:
 * 	 used to exclude "types" that mention inference variables (18.1.1).</li>
 * <li>{@link TypeBinding#mentionsAny(TypeBinding[], int)}:
 * 	 does the receiver type binding mention any of the given types?</li>
 * <li>{@link TypeBinding#substituteInferenceVariable(InferenceVariable, TypeBinding)}:
 * 	 replace occurrences of an inference variable with a proper type.</li>
 * <li>{@link TypeBinding#collectInferenceVariables(Set)}:
 * 	 collect all inference variables mentioned in the receiver type into the given set.</li>
 * <li>{@link TypeVariableBinding#getTypeBounds(InferenceVariable, InferenceSubstitution)}:
 * 	Compute the initial type bounds for one inference variable as per JLS8 sect 18.1.3.</li>
 * </ul>
 * <h2>Phases of Inference</h2>
 * <ul>
 * <li>18.2 <b>Reduction</b>: {@link #reduce()} with most work happening in implementations of
 *  {@link ConstraintFormula#reduce(InferenceContext18)}:
 *  <ul>
 *  <li>18.2.1 Expression Compatibility Constraints: {@link ConstraintExpressionFormula#reduce(InferenceContext18)}.</li>
 *  <li>18.2.2 Type Compatibility Constraints ff. {@link ConstraintTypeFormula#reduce(InferenceContext18)}.</li>
 *  </ul></li>
 * <li>18.3 <b>Incorporation</b>: {@link BoundSet#incorporate(InferenceContext18)}; during inference new constraints
 * 	are accepted via {@link BoundSet#reduceOneConstraint(InferenceContext18, ConstraintFormula)} (combining 18.2 & 18.3)</li>
 * <li>18.4 <b>Resolution</b>: {@link #resolve(InferenceVariable[])}.
 * </ul>
 * Some of the above operations accumulate their results into {@link #currentBounds}, whereas
 * the last phase <em>returns</em> the resulting bound set while keeping the previous state in {@link #currentBounds}.
 * <h2>18.5. Uses of Inference</h2>
 * These are the main entries from the compiler into the inference engine:
 * <dl>
 * <dt>18.5.1 Invocation Applicability Inference</dt>
 * <dd>{@link #inferInvocationApplicability(MethodBinding, TypeBinding[], boolean)}. Prepare the initial state for
 * 	inference of a generic invocation - no target type used at this point.
 *  Need to call {@link #solve()} afterwards to produce the intermediate result.<br/>
 *  Called indirectly from {@link Scope#findMethod(ReferenceBinding, char[], TypeBinding[], InvocationSite, boolean)} et al
 *  to select applicable methods into overload resolution.</dd>
 * <dt>18.5.2 Invocation Type Inference</dt>
 * <dd>{@link InferenceContext18#inferInvocationType(BoundSet, TypeBinding, InvocationSite, MethodBinding)}. After a
 * 	most specific method has been picked, and given a target type determine the final generic instantiation.
 *  As long as a target type is still unavailable this phase keeps getting deferred.</br>
 *  Different wrappers exist for the convenience of different callers.</dd>
 * <dt>18.5.3 Functional Interface Parameterization Inference</dt>
 * <dd>Controlled from {@link LambdaExpression#resolveType(BlockScope)}.</dd>
 * <dt>18.5.4 More Specific Method Inference</dt>
 * <dd><em>Not Yet Implemented</em></dd>
 * </dl>
 * For 18.5.1 and 18.5.2 some high-level control is implemented in
 *  {@link ParameterizedGenericMethodBinding#computeCompatibleMethod(MethodBinding, TypeBinding[], Scope, InvocationSite, int)}.
 * <h2>Inference Lifecycle</h2>
 * The separation into 18.5.1 and 18.5.2 causes some complexity:
 * <ul>
 * <li>Calling both parts of inference is directly interwoven with overload resolution. See 
 * {@link ParameterizedGenericMethodBinding#computeCompatibleMethod(MethodBinding, TypeBinding[], Scope, InvocationSite, int)
 * 		PGMB#computeCompatibleMethod()} for the basic <b>protocol</b>.</li>
 * <li>Intermediate <b>state</b> regarding inference must be stored between both phases. Inference is performed with different
 *   inputs for each pair of {@link Invocation} x {@link ParameterizedGenericMethodBinding},
 *    see {@link Invocation#registerInferenceContext(ParameterizedGenericMethodBinding, InferenceContext18) Invocation.registerInferenceContext()} and
 *    {@link Invocation#getInferenceContext(ParameterizedMethodBinding) getInferenceContext()}.<br/>
 *    As part of the lifecycle state, each instance of InferenceContext18 remembers the current {@link #inferenceKind}
 *    and {@link #stepCompleted}.</li>
 * <li><b>Nested inference/resolving</b>: If an invocation argument is a poly expression itself, final resolving of the argument can only happened
 *    after Invocation Type Inference regarding the outer invocation. Outer inference must produce the <b>target type</b> that drives
 *    the inner inference / resolving. Two different protocols are applied:
 *    <ul>
 *    <li>If the inner poly expression is an invocation, inner inference is directly incorporated into
 *      the {@link #currentBounds}, see block inside {@link ConstraintExpressionFormula#reduce(InferenceContext18)}.<br/>
 *      In this case the results of the combined inference need to be applied to all contained inner invocations,
 *      which happens in {@link #rebindInnerPolies(BoundSet, TypeBinding[])}, which must be called whenever
 *      18.5.2 finishes.</li>
 *    <li>If the inner poly expression is a functional expression or a conditional expression no inference variables
 *      exist representing the inner. In this case the final target type is pushed into the inner using
 *      {@link Expression#checkAgainstFinalTargetType(TypeBinding, Scope)}, which, too, is called from 
 *      {@link #rebindInnerPolies(BoundSet, TypeBinding[])}.</li>
 *    <li>For recursively pushing target types into arguments of an invocation
 *    	method {@link ASTNode#resolvePolyExpressionArguments(Invocation, MethodBinding, TypeBinding[], Scope)} exists,
 *    	which is called in two situations: (1) for non-generic outer invocations from MessageSend#findMethodBinding() and
 *    	Statement#findConstructorBinding(); (2) for generic outer invocations from {@link #rebindInnerPolies(BoundSet, TypeBinding[])}.</li>
 *    <li>In some situations invocation arguments that are poly invocations need to be resolved in the middle of overload resolution
 *    	to answer {@link Scope#parameterCompatibilityLevel18} (where the outer invocation did not involve any inference).<br/>
 *    </ul>
 *    Pushing inference results into an inner invocation happens using {@link Invocation#updateBindings(MethodBinding,TypeBinding)}.</li>
 * <li>Decision whether or not an invocation is a <b>variable-arity</b> invocation is made by first attempting
 * 		to solve 18.5.1 in mode {@link #CHECK_LOOSE}. Only if that fails, another attempt is made in mode {@link #CHECK_VARARG}.
 * 		Which of these two attempts was successful is stored in {@link #inferenceKind}.
 * 		This field must be consulted whenever arguments of an invocation should be further processed.
 * 		See also {@link #getParameter(TypeBinding[], int, boolean)} and its clients.</li>
 * </ul>
 */
public class InferenceContext18 {

	/** to conform with javac regarding https://bugs.openjdk.java.net/browse/JDK-8026527 */
	static final boolean SIMULATE_BUG_JDK_8026527 = true;
	/**
	 * Detail flag to control the extent of {@link #SIMULATE_BUG_JDK_8026527}.
	 * A setting of 'false' implements the advice from http://mail.openjdk.java.net/pipermail/lambda-spec-experts/2013-December/000447.html
	 * i.e., raw types are not considered as compatible in constraints/bounds derived from invocation arguments,
	 * but only for constraints derived from type variable bounds.
	 */
	static final boolean ARGUMENT_CONSTRAINTS_ARE_SOFT = false;

	// --- Main State of the Inference: ---

	/** the invocation being inferred (for 18.5.1 and 18.5.2) */
	InvocationSite currentInvocation;
	/** arguments of #currentInvocation, if any */
	Expression[] invocationArguments;
	
	/** The inference variables for which as solution is sought. */
	InferenceVariable[] inferenceVariables;
	/** Number of inference variables. */
	int variableCount = 0;
	/** Constraints that have not yet been reduced and incorporated. */
	ConstraintFormula[] initialConstraints;
	/** The accumulated type bounds etc. */
	BoundSet currentBounds;

	/** solution of applicability inference, stored for use as fallback, if invocation type inference fails. */
	BoundSet storedSolution;

	/** For each candidate target type imposed from the outside store the solution of invocation type inference. */
	Map<TypeBinding,Solution> solutionsPerTargetType = new HashMap<TypeBinding, Solution>();

	/** One of CHECK_STRICT, CHECK_LOOSE, or CHECK_VARARGS. */
	int inferenceKind;
	/** Marks how much work has been done so far? Used to avoid performing any of these tasks more than once. */
	public int stepCompleted = NOT_INFERRED;

	public static final int NOT_INFERRED = 0;
	/** Applicability Inference (18.5.1) has been completed. */
	public static final int APPLICABILITY_INFERRED = 1;
	/** Invocation Type Inference (18.5.2) has been completed (for some target type). */
	public static final int TYPE_INFERRED = 2;
	/** All nested elements have been fully resolved. */
	public static final int BINDINGS_UPDATED = 3;
	
	/** Signals whether any type compatibility makes use of unchecked conversion. */
	public List<ConstraintFormula> constraintsWithUncheckedConversion;

	// ---

	/** Inner poly invocations which have been included in this inference. */
	List<InvocationSite> innerPolies = new ArrayList<InvocationSite>();
	/** Link to an outer inference context, used for bundled error reporting. */
	public InferenceContext18 outerContext;
	private ArrayList<MethodBinding> problemMethods;

	Scope scope;
	LookupEnvironment environment;
	ReferenceBinding object; // java.lang.Object
	
	public static final int CHECK_STRICT = 1;
	public static final int CHECK_LOOSE = 2;
	public static final int CHECK_VARARG = 3;
	
	static class SuspendedInferenceRecord {
		InvocationSite site;
		Expression[] invocationArguments;
		InferenceVariable[] inferenceVariables;
		int inferenceKind;
		SuspendedInferenceRecord(InvocationSite site, Expression[] invocationArguments, InferenceVariable[] inferenceVariables, int inferenceKind) {
			this.site = site;
			this.invocationArguments = invocationArguments;
			this.inferenceVariables = inferenceVariables;
			this.inferenceKind = inferenceKind;
		}
	}
	
	/** Record for a candidate solution of Invocation Type Inference for one specific target type. */
	static class Solution {
		TypeBinding resolvedType;
		MethodBinding method;
		BoundSet bounds;
		Solution(MethodBinding method, BoundSet bounds) {
			this.method = method;
			this.resolvedType = method.isConstructor() ? method.declaringClass : method.returnType;
			this.bounds = bounds;
		}
	}

	/** Construct an inference context for an invocation (method/constructor). */
	public InferenceContext18(Scope scope, Expression[] arguments, InvocationSite site) {
		this.scope = scope;
		this.environment = scope.environment();
		this.object = scope.getJavaLangObject();
		this.invocationArguments = arguments;
		this.currentInvocation = site;
	}

	public InferenceContext18(Scope scope) {
		this.scope = scope;
		this.environment = scope.environment();
		this.object = scope.getJavaLangObject();
	}

	/**
	 * JLS 18.1.3: Create initial bounds from a given set of type parameters declarations.
	 * @return the set of inference variables created for the given typeParameters
	 */
	public InferenceVariable[] createInitialBoundSet(TypeVariableBinding[] typeParameters) {
		// 
		if (this.currentBounds == null) {
			this.currentBounds = new BoundSet();
		}
		if (typeParameters != null) {
			InferenceVariable[] newInferenceVariables = addInitialTypeVariableSubstitutions(typeParameters);
			this.currentBounds.addBoundsFromTypeParameters(this, typeParameters, newInferenceVariables);
			return newInferenceVariables;
		}
		return Binding.NO_INFERENCE_VARIABLES;
	}

	/**
	 * Substitute any type variables mentioned in 'type' by the corresponding inference variable, if one exists. 
	 */
	public TypeBinding substitute(TypeBinding type) {
		InferenceSubstitution inferenceSubstitution = new InferenceSubstitution(this.environment, this.inferenceVariables);
		return 	inferenceSubstitution.substitute(inferenceSubstitution, type);
	}

	/** JLS 18.5.1: compute bounds from formal and actual parameters. */
	public void createInitialConstraintsForParameters(TypeBinding[] parameters, boolean checkVararg, TypeBinding varArgsType, MethodBinding method) {
		// TODO discriminate strict vs. loose invocations
		if (this.invocationArguments == null)
			return;
		int len = checkVararg ? parameters.length - 1 : Math.min(parameters.length, this.invocationArguments.length);
		int maxConstraints = checkVararg ? this.invocationArguments.length : len;
		int numConstraints = 0;
		if (this.initialConstraints == null) {
			this.initialConstraints = new ConstraintFormula[maxConstraints];
		} else {
			numConstraints = this.initialConstraints.length;
			maxConstraints += numConstraints;
			System.arraycopy(this.initialConstraints, 0,
					this.initialConstraints=new ConstraintFormula[maxConstraints], 0, numConstraints);
		}
		for (int i = 0; i < len; i++) {
			if (this.invocationArguments[i].isPertinentToApplicability(parameters[i], method)) {
				TypeBinding thetaF = substitute(parameters[i]);
				this.initialConstraints[numConstraints++] = new ConstraintExpressionFormula(this.invocationArguments[i], thetaF, ReductionResult.COMPATIBLE, ARGUMENT_CONSTRAINTS_ARE_SOFT);
			}
		}
		if (checkVararg && varArgsType instanceof ArrayBinding) {
			varArgsType = ((ArrayBinding)varArgsType).elementsType();
			TypeBinding thetaF = substitute(varArgsType);
			for (int i = len; i < this.invocationArguments.length; i++) {
				if (this.invocationArguments[i].isPertinentToApplicability(varArgsType, method)) {
					this.initialConstraints[numConstraints++] = new ConstraintExpressionFormula(this.invocationArguments[i], thetaF, ReductionResult.COMPATIBLE, ARGUMENT_CONSTRAINTS_ARE_SOFT);
				}
			}
		}
		if (numConstraints == 0)
			this.initialConstraints = ConstraintFormula.NO_CONSTRAINTS;
		else if (numConstraints < maxConstraints)
			System.arraycopy(this.initialConstraints, 0, this.initialConstraints = new ConstraintFormula[numConstraints], 0, numConstraints);
	}

	private InferenceVariable[] addInitialTypeVariableSubstitutions(TypeBinding[] typeVariables) {
		int len = typeVariables.length;
		if (len == 0) {
			if (this.inferenceVariables == null)
				this.inferenceVariables = Binding.NO_INFERENCE_VARIABLES;
			return Binding.NO_INFERENCE_VARIABLES;
		}
		InferenceVariable[] newVariables = new InferenceVariable[len];
		for (int i = 0; i < len; i++)
			newVariables[i] = new InferenceVariable(typeVariables[i], this.variableCount++, this.currentInvocation, this.environment, this.object);
		if (this.inferenceVariables == null || this.inferenceVariables.length == 0) {
			this.inferenceVariables = newVariables;
		} else {
			// merge into this.inferenceVariables:
			int prev = this.inferenceVariables.length;
			System.arraycopy(this.inferenceVariables, 0, this.inferenceVariables = new InferenceVariable[len+prev], 0, prev);
			System.arraycopy(newVariables, 0, this.inferenceVariables, prev, len);
		}
		return newVariables;
	}

	/** Add new inference variables for the given type variables. */
	public InferenceVariable[] addTypeVariableSubstitutions(TypeBinding[] typeVariables) {
		int len2 = typeVariables.length;
		InferenceVariable[] newVariables = new InferenceVariable[len2];
		for (int i = 0; i < typeVariables.length; i++) {
			if (typeVariables[i] instanceof InferenceVariable)
				newVariables[i] = (InferenceVariable) typeVariables[i]; // prevent double substitution of an already-substituted inferenceVariable
			else
				newVariables[i] = new InferenceVariable(typeVariables[i], this.variableCount++, this.currentInvocation, this.environment, this.object);
		}

		int start = 0;
		if (this.inferenceVariables != null) {
			int len1 = this.inferenceVariables.length;
			System.arraycopy(this.inferenceVariables, 0, this.inferenceVariables = new InferenceVariable[len1+len2], 0, len1);
			start = len1;
		} else {
			this.inferenceVariables = new InferenceVariable[len2];
		}
		System.arraycopy(newVariables, 0, this.inferenceVariables, start, len2);
		return newVariables;
	}

	/** JLS 18.1.3 Bounds: throws α: the inference variable α appears in a throws clause */
	public void addThrowsContraints(TypeBinding[] parameters, InferenceVariable[] variables, ReferenceBinding[] thrownExceptions) {
		for (int i = 0; i < parameters.length; i++) {
			TypeBinding parameter = parameters[i];
			for (int j = 0; j < thrownExceptions.length; j++) {
				if (TypeBinding.equalsEquals(parameter, thrownExceptions[j])) {
					this.currentBounds.inThrows.add(variables[i]);
					break;
				}
			}
		}		
	}

	/** JLS 18.5.1 Invocation Applicability Inference. */
	public void inferInvocationApplicability(MethodBinding method, TypeBinding[] arguments, boolean isDiamond) {
		ConstraintExpressionFormula.inferInvocationApplicability(this, method, arguments, isDiamond, this.inferenceKind);
	}

	/** JLS 18.5.2 Invocation Type Inference 
	 * <p>Callers are responsible for any post-processing (see {@link #rebindInnerPolies(BoundSet, TypeBinding[])}).</p>
	 * @param b1 "the bound set produced by reduction in order to demonstrate that m is applicable in 18.5.1"
	 */
	public BoundSet inferInvocationType(BoundSet b1, TypeBinding expectedType, InvocationSite invocationSite, MethodBinding method)
			throws InferenceFailureException 
	{
		// not JLS: simply ensure that null hints from the return type have been seen even in standalone contexts:
		if (expectedType == null && method.returnType != null)
			substitute(method.returnType); // result is ignore, the only effect is on InferenceVariable.nullHints
		//
		BoundSet previous = this.currentBounds.copy();
		this.currentBounds = b1;
		try {
			// bullets 1&2: definitions only.
			if (expectedType != null
					&& expectedType != TypeBinding.VOID
					&& invocationSite instanceof Expression
					&& ((Expression)invocationSite).isPolyExpression(method)) 
			{
				// 3. bullet: special treatment for poly expressions
				if (!ConstraintExpressionFormula.inferPolyInvocationType(this, invocationSite, expectedType, method)) {
					return null;
				}
			}
			// 4. bullet: assemble C:
			Set<ConstraintFormula> c = new HashSet<ConstraintFormula>();
			if (!addConstraintsToC(this.invocationArguments, c, method, this.inferenceKind))
				return null;
			// 5. bullet: determine B3 from C
			while (!c.isEmpty()) {
				// *
				Set<ConstraintFormula> bottomSet = findBottomSet(c, allOutputVariables(c));
				if (bottomSet.isEmpty()) {
					bottomSet.add(pickFromCycle(c));
				}
				// *
				c.removeAll(bottomSet);
				// * The union of the input variables of all the selected constraints, α1, ..., αm, ...
				Set<InferenceVariable> allInputs = new HashSet<InferenceVariable>();
				Iterator<ConstraintFormula> bottomIt = bottomSet.iterator();
				while (bottomIt.hasNext()) {
					allInputs.addAll(bottomIt.next().inputVariables(this));
				}
				InferenceVariable[] variablesArray = allInputs.toArray(new InferenceVariable[allInputs.size()]);
				//   ... is resolved
				this.currentBounds.incorporate(this);
				BoundSet solution = resolve(variablesArray);
				// in rare cases resolving just one set of variables doesn't suffice,
				// don't bother with finding the necessary superset, just resolve all:
				if (solution == null)
					solution = resolve(this.inferenceVariables);
				// * ~ apply substitutions to all constraints: 
				bottomIt = bottomSet.iterator();
				while (bottomIt.hasNext()) {
					ConstraintFormula constraint = bottomIt.next();
					if (solution != null)
						if (!constraint.applySubstitution(solution, variablesArray))
							return null;
				// * reduce and incorporate
					if (!this.currentBounds.reduceOneConstraint(this, constraint))
						return null;
				}
			}
			// 6. bullet: solve
			BoundSet solution = solve();
			if (solution == null || !isResolved(solution)) {
				this.currentBounds = previous; // don't let bounds from unsuccessful attempt leak into subsequent attempts
				return null;
			}
			// we're done, start reporting:
			reportUncheckedConversions(solution);
			return this.currentBounds = solution; // this is final, keep the result:
		} finally {
			this.stepCompleted = TYPE_INFERRED;
		}
	}

	private boolean addConstraintsToC(Expression[] exprs, Set<ConstraintFormula> c, MethodBinding method, int inferenceKindForMethod) {
		TypeBinding[] fs;
		if (exprs != null) {
			int k = exprs.length;
			int p = method.parameters.length;
			if (k < (method.isVarargs() ? p-1 : p))
				return false; // insufficient arguments for parameters!
			switch (inferenceKindForMethod) {
				case CHECK_STRICT:
				case CHECK_LOOSE:
					fs = method.parameters;
					break;
				case CHECK_VARARG:
					fs = varArgTypes(method.parameters, k);
					break;
				default:
					throw new IllegalStateException("Unexpected checkKind "+this.inferenceKind); //$NON-NLS-1$
			}
			for (int i = 0; i < k; i++) {
				TypeBinding fsi = fs[Math.min(i, p-1)];
				TypeBinding substF = substitute(fsi);
				if (!addConstraintsToC_OneExpr(exprs[i], c, fsi, substF, method))
					return false;
	        }
		}
		return true;
	}

	private boolean addConstraintsToC_OneExpr(Expression expri, Set<ConstraintFormula> c, TypeBinding fsi, TypeBinding substF, MethodBinding method) {
		// For all i (1 ≤ i ≤ k), if ei is not pertinent to applicability, the set contains ⟨ei → θ Fi⟩.
		if (!expri.isPertinentToApplicability(fsi, method)) {
			c.add(new ConstraintExpressionFormula(expri, substF, ReductionResult.COMPATIBLE, ARGUMENT_CONSTRAINTS_ARE_SOFT));
		}
		if (expri instanceof FunctionalExpression) {
			c.add(new ConstraintExceptionFormula((FunctionalExpression) expri, substF));
		} else if (expri instanceof Invocation && expri.isPolyExpression()) {
			Invocation invocation = (Invocation) expri;
			MethodBinding innerMethod = invocation.binding(null, false, null);
			if (innerMethod instanceof ParameterizedGenericMethodBinding) {
				InferenceContext18 innerCtx = invocation.getInferenceContext((ParameterizedMethodBinding) innerMethod);
				if (innerCtx != null) { // otherwise innerMethod does not participate in inference
					return addConstraintsToC(invocation.arguments(), c, innerMethod.genericMethod(), innerCtx.inferenceKind);
				}
			}
		} else if (expri instanceof ConditionalExpression) {
			ConditionalExpression ce = (ConditionalExpression) expri;
			return addConstraintsToC_OneExpr(ce.valueIfTrue, c, fsi, substF, method)
				 && addConstraintsToC_OneExpr(ce.valueIfFalse, c, fsi, substF, method);
		}
		return true;
	}

	/**
	 * Simplified API to perform Invocation Type Inference (JLS 18.5.2)
	 * and perform subsequent steps: bound check, rebinding of inner poly expressions,
	 * and creating of a problem method binding if needed.
	 * Should only be called if the inference has not yet finished.
	 * @param invocation invocation being inferred
	 * @param argumentTypes arguments being passed into the invocation
	 * @param method current candidate method binding for this invocation
	 * @return a valid method binding with updated type parameters,
	 * 	or a problem method binding signaling either inference failure or a bound mismatch.
	 */
	/*@NonNull*/ MethodBinding inferInvocationType(Invocation invocation, TypeBinding[] argumentTypes, ParameterizedGenericMethodBinding method) {
		// TODO optimize: if outerContext exists and is resolved, we probably don't need to infer again.
		TypeBinding targetType = invocation.invocationTargetType();
		ParameterizedGenericMethodBinding finalMethod = null;
		ParameterizedGenericMethodBinding methodToCheck = method;
		
		boolean haveProperTargetType = targetType != null && targetType.isProperType(true);
		if (haveProperTargetType || !invocation.getExpressionContext().definesTargetType()) {
			MethodBinding original = method.originalMethod;
			Solution solution = this.solutionsPerTargetType.get(targetType);
			BoundSet result = solution != null ? solution.bounds : null;
			if (result == null) {
				// start over from a previous candidate but discard its type variable instantiations
				// TODO: should we retain any instantiations of type variables not owned by the method? 
				try {
					result = inferInvocationType(this.currentBounds, targetType, invocation, original);
				} catch (InferenceFailureException e) {
					// no solution, but do more checks below
				}
			}
			if (result != null) {
				TypeBinding[] solutions = getSolutions(original.typeVariables(), invocation, result);
				if (solutions != null) {
					finalMethod = this.environment.createParameterizedGenericMethod(original, solutions);
					if (this.scope.compilerOptions().isAnnotationBasedNullAnalysisEnabled)
						NullAnnotationMatching.checkForContraditions(finalMethod, invocation, this.scope);
					invocation.registerInferenceContext(finalMethod, this);
					this.solutionsPerTargetType.put(targetType, new Solution(finalMethod, result));
				}
			}
			if (finalMethod != null)
				methodToCheck = finalMethod;
		} else {
			finalMethod = method;
		}
		
		MethodBinding problemMethod = methodToCheck.boundCheck18(this.scope, argumentTypes);
		if (problemMethod != null)
			return problemMethod;

		if (!haveProperTargetType && invocation.getExpressionContext().definesTargetType())
			return method; // still not ready!

		if (finalMethod != null) {
			if (rebindInnerPolies(finalMethod, invocation))
				return finalMethod;
		}
		return getReturnProblemMethodIfNeeded(targetType, method);
	}

	/**
	 * Simplified API to perform Invocation Type Inference (JLS 18.5.2)
	 * and perform subsequent steps: bound check, rebinding of inner poly expressions,
	 * and creating of a problem method binding if needed.
	 * Should only be called if the inference has not yet finished.
	 * Version used for inner invocations, where argument types need to be extracted
	 * from actual invocation arguments.
	 * @param invocation invocation being inferred
	 * @param method current candidate method binding for this invocation
	 * @return a valid method binding with updated type parameters,
	 * 	or a problem method binding signaling either inference failure or a bound mismatch.
	 */
	public /*@NonNull*/ MethodBinding inferInvocationType(Invocation invocation, ParameterizedGenericMethodBinding method) {
		TypeBinding[] argumentTypes = null;
		Expression[] arguments = invocation.arguments();
		if (arguments != null) {
			argumentTypes = new TypeBinding[arguments.length];
			for (int i = 0; i < arguments.length; i++)
				argumentTypes[i] = arguments[i].resolvedType;
		}
		return inferInvocationType(invocation, argumentTypes, method);
	}

	public boolean hasResultFor(TypeBinding targetType) {
		if (targetType == null)
			return this.stepCompleted >= TYPE_INFERRED;
		else
			return this.solutionsPerTargetType.containsKey(targetType);
	}

	public boolean registerSolution(TypeBinding targetType, MethodBinding updatedBinding) {
		Solution solution = this.solutionsPerTargetType.get(targetType);
		if (solution != null)
			return false; // no update
		this.solutionsPerTargetType.put(targetType, new Solution(updatedBinding, null));
		this.stepCompleted = Math.max(this.stepCompleted, TYPE_INFERRED);
		return true;
	}

	/**
	 * 18.5.3 Functional Interface Parameterization Inference
	 */
	public ReferenceBinding inferFunctionalInterfaceParameterization(LambdaExpression lambda, BlockScope blockScope, 
			ParameterizedTypeBinding targetTypeWithWildCards) 
	{
		TypeBinding[] q = createBoundsForFunctionalInterfaceParameterizationInference(targetTypeWithWildCards);
		if (q == null || q.length != lambda.arguments().length) {
			// fail  TODO: can this still happen here?
		} else {
			if (reduceWithEqualityConstraints(lambda.argumentTypes(), q)) {
				ReferenceBinding genericType = targetTypeWithWildCards.genericType();
				TypeBinding[] a = targetTypeWithWildCards.arguments; // a is not-null by construction of parameterizedWithWildcard()
				TypeBinding[] aprime = getFunctionInterfaceArgumentSolutions(a);
				// TODO If F<A'1, ..., A'm> is a well-formed type, ...
				return blockScope.environment().createParameterizedType(genericType, aprime, genericType.enclosingType());
			}
		}
		return targetTypeWithWildCards;
	}

	/**
	 * Create initial bound set for 18.5.3 Functional Interface Parameterization Inference
	 * @param functionalInterface the functional interface F<A1,..Am>
	 * @return the parameter types Q1..Qk of the function type of the type F<α1, ..., αm>, or null 
	 */
	TypeBinding[] createBoundsForFunctionalInterfaceParameterizationInference(ParameterizedTypeBinding functionalInterface) {
		if (this.currentBounds == null)
			this.currentBounds = new BoundSet();
		TypeBinding[] a = functionalInterface.arguments;
		if (a == null)
			return null;
		InferenceVariable[] alpha = addInitialTypeVariableSubstitutions(a);

		for (int i = 0; i < a.length; i++) {
			TypeBound bound;
			if (a[i].kind() == Binding.WILDCARD_TYPE) {
				WildcardBinding wildcard = (WildcardBinding) a[i];
				switch(wildcard.boundKind) {
    				case Wildcard.EXTENDS :
    					bound = new TypeBound(alpha[i], wildcard.allBounds(), ReductionResult.SUBTYPE);
    					break;
    				case Wildcard.SUPER :
    					bound = new TypeBound(alpha[i], wildcard.bound, ReductionResult.SUPERTYPE);
    					break;
    				case Wildcard.UNBOUND :
    					bound = new TypeBound(alpha[i], this.object, ReductionResult.SUBTYPE);
    					break;
    				default:
    					continue; // cannot
				}
			} else {
				bound = new TypeBound(alpha[i], a[i], ReductionResult.SAME);
			}
			this.currentBounds.addBound(bound, this.environment);
		}
		TypeBinding falpha = substitute(functionalInterface);
		return falpha.getSingleAbstractMethod(this.scope, true).parameters;
	}

	public boolean reduceWithEqualityConstraints(TypeBinding[] p, TypeBinding[] q) {
		if (p != null) {
			for (int i = 0; i < p.length; i++) {
				try {
					if (!this.reduceAndIncorporate(ConstraintTypeFormula.create(p[i], q[i], ReductionResult.SAME)))
						return false;
				} catch (InferenceFailureException e) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 18.5.4 More Specific Method Inference
	 */
	public boolean isMoreSpecificThan(MethodBinding m1, MethodBinding m2, boolean isVarArgs, boolean isVarArgs2) {
		// TODO: we don't yet distinguish vararg-with-passthrough from vararg-with-exactly-one-vararg-arg
		if (isVarArgs != isVarArgs2) {
			return isVarArgs2;
		}
		Expression[] arguments = this.invocationArguments;
		int numInvocArgs = arguments == null ? 0 : arguments.length;
		TypeVariableBinding[] p = m2.typeVariables();
		TypeBinding[] s = m1.parameters;
		TypeBinding[] t = new TypeBinding[m2.parameters.length];
		createInitialBoundSet(p);
		for (int i = 0; i < t.length; i++)
			t[i] = substitute(m2.parameters[i]);

		try {
			for (int i = 0; i < numInvocArgs; i++) {
				TypeBinding si = getParameter(s, i, isVarArgs);
				TypeBinding ti = getParameter(t, i, isVarArgs);
				Boolean result = moreSpecificMain(si, ti, this.invocationArguments[i]);
				if (result == Boolean.FALSE)
					return false;
				if (result == null)
					if (!reduceAndIncorporate(ConstraintTypeFormula.create(si, ti, ReductionResult.SUBTYPE)))
						return false;
			}
			if (t.length == numInvocArgs + 1) {
				TypeBinding skplus1 = getParameter(s, numInvocArgs, true);
				TypeBinding tkplus1 = getParameter(t, numInvocArgs, true);
				if (!reduceAndIncorporate(ConstraintTypeFormula.create(skplus1, tkplus1, ReductionResult.SUBTYPE)))
					return false;
			}
			return solve() != null;
		} catch (InferenceFailureException e) {
			return false;
		}
	}
	
	// FALSE: inference fails
	// TRUE:  constraints have been incorporated
	// null:  need the otherwise branch
	private Boolean moreSpecificMain(TypeBinding si, TypeBinding ti, Expression expri) throws InferenceFailureException {
		if (si.isProperType(true) && ti.isProperType(true)) {
			return expri.sIsMoreSpecific(si, ti, this.scope) ? Boolean.TRUE : Boolean.FALSE;
		}
		if (si.isFunctionalInterface(this.scope)) {
			TypeBinding funcI = ti.original();
			if (funcI.isFunctionalInterface(this.scope)) {
				// "... none of the following is true:" 
				if (siSuperI(si, funcI) || siSubI(si, funcI))
					return null;
				if (si instanceof IntersectionCastTypeBinding) {
					TypeBinding[] elements = ((IntersectionCastTypeBinding)si).intersectingTypes;
					checkSuper: {
						for (int i = 0; i < elements.length; i++)
							if (!siSuperI(elements[i], funcI))
								break checkSuper;
						return null; // each element of the intersection is a superinterface of I, or a parameterization of a superinterface of I.
					}
					for (int i = 0; i < elements.length; i++)
						if (siSubI(elements[i], funcI))
							return null; // some element of the intersection is a subinterface of I, or a parameterization of a subinterface of I.	
				}
				// all passed, time to do some work:
				TypeBinding siCapture = si.capture(this.scope, this.captureId++);
				MethodBinding sam = siCapture.getSingleAbstractMethod(this.scope, false); // no wildcards should be left needing replacement
				TypeBinding[] u = sam.parameters;
				TypeBinding r1 = sam.isConstructor() ? sam.declaringClass : sam.returnType;
				sam = ti.getSingleAbstractMethod(this.scope, true); // TODO
				TypeBinding[] v = sam.parameters;
				TypeBinding r2 = sam.isConstructor() ? sam.declaringClass : sam.returnType;
				return Boolean.valueOf(checkExpression(expri, u, r1, v, r2));
			}
		}
		return null;
	}

	private boolean checkExpression(Expression expri, TypeBinding[] u, TypeBinding r1, TypeBinding[] v, TypeBinding r2) 
			throws InferenceFailureException {
		if (expri instanceof LambdaExpression && !((LambdaExpression)expri).argumentsTypeElided()) {
			if (r2.id == TypeIds.T_void)
				return true;
			LambdaExpression lambda = (LambdaExpression) expri;
			Expression[] results = lambda.resultExpressions();
			if (r1.isFunctionalInterface(this.scope) && r2.isFunctionalInterface(this.scope)
					&& !(r1.isCompatibleWith(r2) || r2.isCompatibleWith(r1))) {
				// "these rules are applied recursively to R1 and R2, for each result expression in expi."
				// (what does "applied .. to R1 and R2" mean? Why mention R1/R2 and not U/V?)
				for (int i = 0; i < results.length; i++) {
					if (!checkExpression(results[i], u, r1, v, r2))
						return false;
				}
				return true;
			}
			checkPrimitive1: if (r1.isPrimitiveType() && !r2.isPrimitiveType()) {
				// check: each result expression is a standalone expression of a primitive type
				for (int i = 0; i < results.length; i++) {
					if (results[i].isPolyExpression() || (results[i].resolvedType != null && !results[i].resolvedType.isPrimitiveType()))
						break checkPrimitive1;
				}
				return true;
			}
			checkPrimitive2: if (r2.isPrimitiveType() && !r1.isPrimitiveType()) {
				for (int i = 0; i < results.length; i++) {
					// for all expressions (not for any expression not)
					if (!(
							(!results[i].isPolyExpression() && (results[i].resolvedType != null && !results[i].resolvedType.isPrimitiveType())) // standalone of a referencetype
							|| results[i].isPolyExpression()))	// or a poly
						break checkPrimitive2;
				}
				return true;
			}
			return reduceAndIncorporate(ConstraintTypeFormula.create(r1, r2, ReductionResult.SUBTYPE));
		} else if (expri instanceof ReferenceExpression && ((ReferenceExpression)expri).isExactMethodReference()) {
			ReferenceExpression reference = (ReferenceExpression) expri;
			for (int i = 0; i < u.length; i++) {
				if (!reduceAndIncorporate(ConstraintTypeFormula.create(u[i], v[i], ReductionResult.SAME)))
					return false;
			}
			if (r2.id == TypeIds.T_void)
				return true;
			MethodBinding method = reference.getExactMethod();
			TypeBinding returnType = method.isConstructor() ? method.declaringClass : method.returnType;
			if (r1.isPrimitiveType() && !r2.isPrimitiveType() && returnType.isPrimitiveType()) 
				return true;
			if (r2.isPrimitiveType() && !r1.isPrimitiveType() && !returnType.isPrimitiveType())
				return true;
			return reduceAndIncorporate(ConstraintTypeFormula.create(r1, r2, ReductionResult.SUBTYPE));
		} else if (expri instanceof ConditionalExpression) {
			ConditionalExpression cond = (ConditionalExpression) expri;
			return  checkExpression(cond.valueIfTrue, u, r1, v, r2) && checkExpression(cond.valueIfFalse, u, r1, v, r2);
		} else {
			return false;
		}
	}

	private boolean siSuperI(TypeBinding si, TypeBinding funcI) {
		if (TypeBinding.equalsEquals(si, funcI) || TypeBinding.equalsEquals(si.original(), funcI))
			return true;
		TypeBinding[] superIfcs = funcI.superInterfaces();
		if (superIfcs == null) return false;
		for (int i = 0; i < superIfcs.length; i++) {
			if (siSuperI(si, superIfcs[i]))
				return true;
		}
		return false;
	}

	private boolean siSubI(TypeBinding si, TypeBinding funcI) {
		if (TypeBinding.equalsEquals(si, funcI) || TypeBinding.equalsEquals(si.original(), funcI))
			return true;
		TypeBinding[] superIfcs = si.superInterfaces();
		if (superIfcs == null) return false;
		for (int i = 0; i < superIfcs.length; i++) {
			if (siSubI(superIfcs[i], funcI))
				return true;
		}
		return false;
	}

	// ========== Below this point: implementation of the generic algorithm: ==========

	/**
	 * Try to solve the inference problem defined by constraints and bounds previously registered.
	 * @return a bound set representing the solution, or null if inference failed
	 * @throws InferenceFailureException a compile error has been detected during inference
	 */
	public /*@Nullable*/ BoundSet solve() throws InferenceFailureException {
		if (!reduce())
			return null;
		if (!this.currentBounds.incorporate(this))
			return null;

		return resolve(this.inferenceVariables);
	}
	
	public /*@Nullable*/ BoundSet solve(InferenceVariable[] toResolve) throws InferenceFailureException {
		if (!reduce())
			return null;
		if (!this.currentBounds.incorporate(this))
			return null;

		return resolve(toResolve);
	}

	/**
	 * JLS 18.2. reduce all initial constraints 
	 * @throws InferenceFailureException 
	 */
	private boolean reduce() throws InferenceFailureException {
		if (this.initialConstraints != null) {
			for (int i = 0; i < this.initialConstraints.length; i++) {
				if (!this.currentBounds.reduceOneConstraint(this, this.initialConstraints[i]))
					return false;
			}
		}
		this.initialConstraints = null;
		return true;
	}

	/**
	 * Have all inference variables been instantiated successfully?
	 */
	public boolean isResolved(BoundSet boundSet) {
		if (this.inferenceVariables != null) {
			for (int i = 0; i < this.inferenceVariables.length; i++) {
				if (!boundSet.isInstantiated(this.inferenceVariables[i]))
					return false;
			}
		}
		return true;
	}

	/**
	 * Retrieve the resolved solutions for all given type variables.
	 * @param typeParameters
	 * @param boundSet where instantiations are to be found
	 * @return array containing the substituted types or <code>null</code> elements for any type variable that could not be substituted.
	 */
	public TypeBinding /*@Nullable*/[] getSolutions(TypeVariableBinding[] typeParameters, InvocationSite site, BoundSet boundSet) {
		int len = typeParameters.length;
		TypeBinding[] substitutions = new TypeBinding[len];
		for (int i = 0; i < typeParameters.length; i++) {
			for (int j = 0; j < this.inferenceVariables.length; j++) {
				InferenceVariable variable = this.inferenceVariables[j];
				if (variable.site == site && TypeBinding.equalsEquals(variable.typeParameter, typeParameters[i])) {
					substitutions[i] = boundSet.getInstantiation(variable, this.environment);
					break;
				}
			}
			if (substitutions[i] == null)
				return null;
		}
		return substitutions;
	}

	/** When inference produces a new constraint, reduce it to a suitable type bound and add the latter to the bound set. */
	public boolean reduceAndIncorporate(ConstraintFormula constraint) throws InferenceFailureException {
		return this.currentBounds.reduceOneConstraint(this, constraint); // TODO(SH): should we immediately call a diat incorporate, or can we simply wait for the next round?
	}

	/**
	 * <b>JLS 18.4</b> Resolution
	 * @return answer null if some constraint resolved to FALSE, otherwise the boundset representing the solution
	 * @throws InferenceFailureException 
	 */
	private /*@Nullable*/ BoundSet resolve(InferenceVariable[] toResolve) throws InferenceFailureException {
		this.captureId = 0;
		// NOTE: 18.5.2 ... 
		// "(While it was necessary to demonstrate that the inference variables in B1 could be resolved
		//   in order to establish applicability, the resulting instantiations are not considered part of B1.)
		// For this reason, resolve works on a temporary bound set, copied before any modification.
		BoundSet tmpBoundSet = this.currentBounds;
		if (this.inferenceVariables != null) {
			// find a minimal set of dependent variables:
			Set<InferenceVariable> variableSet;
			while ((variableSet = getSmallestVariableSet(tmpBoundSet, toResolve)) != null) {
				int oldNumUninstantiated = tmpBoundSet.numUninstantiatedVariables(this.inferenceVariables);
				final int numVars = variableSet.size();
				if (numVars > 0) {
					final InferenceVariable[] variables = variableSet.toArray(new InferenceVariable[numVars]);
					variables: if (!tmpBoundSet.hasCaptureBound(variableSet)) {
						// try to instantiate this set of variables in a fresh copy of the bound set:
						BoundSet prevBoundSet = tmpBoundSet;
						tmpBoundSet = tmpBoundSet.copy();
						for (int j = 0; j < variables.length; j++) {
							InferenceVariable variable = variables[j];
							// try lower bounds:
							TypeBinding[] lowerBounds = tmpBoundSet.lowerBounds(variable, true/*onlyProper*/);
							if (lowerBounds != Binding.NO_TYPES) {
								TypeBinding lub = this.scope.lowerUpperBound(lowerBounds);
								if (lub == TypeBinding.VOID || lub == null)
									return null;
								tmpBoundSet.addBound(new TypeBound(variable, lub, ReductionResult.SAME), this.environment);
							} else {
								TypeBinding[] upperBounds = tmpBoundSet.upperBounds(variable, true/*onlyProper*/);
								// check exception bounds:
								if (tmpBoundSet.inThrows.contains(variable) && tmpBoundSet.hasOnlyTrivialExceptionBounds(variable, upperBounds)) {
									TypeBinding runtimeException = this.scope.getType(TypeConstants.JAVA_LANG_RUNTIMEEXCEPTION, 3);
									tmpBoundSet.addBound(new TypeBound(variable, runtimeException, ReductionResult.SAME), this.environment);
								} else {
									// try upper bounds:
									TypeBinding glb = this.object;
									if (upperBounds != Binding.NO_TYPES) {
										if (upperBounds.length == 1) {
											glb = upperBounds[0];
										} else {
											ReferenceBinding[] glbs = Scope.greaterLowerBound((ReferenceBinding[])upperBounds);
											if (glbs == null) {
												throw new UnsupportedOperationException("no glb for "+Arrays.asList(upperBounds)); //$NON-NLS-1$
											} else if (glbs.length == 1) {
												glb = glbs[0];
											} else {
												IntersectionCastTypeBinding intersection = new IntersectionCastTypeBinding(glbs, this.environment);
												if (!ReferenceBinding.isConsistentIntersection(intersection.intersectingTypes)) {
													tmpBoundSet = prevBoundSet; // clean up
													break variables; // and start over
												}
												glb = intersection;
											}
										}
									}
									tmpBoundSet.addBound(new TypeBound(variable, glb, ReductionResult.SAME), this.environment);
								}
							}
						}
						if (tmpBoundSet.incorporate(this))
							continue;
						tmpBoundSet = prevBoundSet;// clean-up for second attempt
					}
					// Otherwise, a second attempt is made...
					Sorting.sortInferenceVariables(variables); // ensure stability of capture IDs
					final CaptureBinding18[] zs = new CaptureBinding18[numVars];
					for (int j = 0; j < numVars; j++)
						zs[j] = freshCapture(variables[j]);
					Substitution theta = new Substitution() {
						public LookupEnvironment environment() { 
							return InferenceContext18.this.environment;
						}
						public boolean isRawSubstitution() {
							return false;
						}
						public TypeBinding substitute(TypeVariableBinding typeVariable) {
							for (int j = 0; j < numVars; j++)
								if (variables[j] == typeVariable) //$IDENTITY-COMPARISON$ InferenceVariable does not participate in type annotation encoding
									return zs[j];
							return typeVariable;
						}
					};
					for (int j = 0; j < numVars; j++) {
						InferenceVariable variable = variables[j];
						CaptureBinding18 zsj = zs[j];
						// add lower bounds:
						TypeBinding[] lowerBounds = tmpBoundSet.lowerBounds(variable, true/*onlyProper*/);
						if (lowerBounds != Binding.NO_TYPES) {
							lowerBounds = Scope.substitute(theta, lowerBounds);
							TypeBinding lub = this.scope.lowerUpperBound(lowerBounds);
							if (lub != TypeBinding.VOID && lub != null)
								zsj.lowerBound = lub;
						}
						// add upper bounds:
						TypeBinding[] upperBounds = tmpBoundSet.upperBounds(variable, false/*onlyProper*/);
						if (upperBounds != Binding.NO_TYPES) {
							for (int k = 0; k < upperBounds.length; k++)
								upperBounds[k] = Scope.substitute(theta, upperBounds[k]);
							if (!setUpperBounds(zsj, upperBounds))
								continue; // at violation of well-formedness skip this candidate and proceed
						}
						if (tmpBoundSet == this.currentBounds)
							tmpBoundSet = tmpBoundSet.copy();
						Iterator<ParameterizedTypeBinding> captureKeys = tmpBoundSet.captures.keySet().iterator();
						Set<ParameterizedTypeBinding> toRemove = new HashSet<ParameterizedTypeBinding>();
						while (captureKeys.hasNext()) {
							ParameterizedTypeBinding key = captureKeys.next();
							int len = key.arguments.length;
							for (int i = 0; i < len; i++) {
								if (key.arguments[i] == variable) { //$IDENTITY-COMPARISON$
									toRemove.add(key);
									break;
								}
							}
						}
						captureKeys = toRemove.iterator();
						while (captureKeys.hasNext())
							tmpBoundSet.captures.remove(captureKeys.next());
						tmpBoundSet.addBound(new TypeBound(variable, zsj, ReductionResult.SAME), this.environment);
					}
					if (tmpBoundSet.incorporate(this)) {
						if (tmpBoundSet.numUninstantiatedVariables(this.inferenceVariables) == oldNumUninstantiated)
							return null; // abort because we made no progress
						continue;
					}
					return null;
				}
			}
		}
		return tmpBoundSet;
	}
	
	int captureId = 0;
	
	/** For 18.4: "Let Z1, ..., Zn be fresh type variables" use capture bindings. */
	private CaptureBinding18 freshCapture(InferenceVariable variable) {
		int id = this.captureId++;
		char[] sourceName = CharOperation.concat("Z".toCharArray(), '#', String.valueOf(id).toCharArray(), '-', variable.sourceName); //$NON-NLS-1$
		int position = this.currentInvocation != null ? this.currentInvocation.sourceStart() : 0;
		return new CaptureBinding18(this.scope.enclosingSourceType(), sourceName, variable.typeParameter.shortReadableName(),
						position, id, this.environment);
	}
	// === ===
	
	private boolean setUpperBounds(CaptureBinding18 typeVariable, TypeBinding[] substitutedUpperBounds) {
		// 18.4: ... define the upper bound of Zi as glb(L1θ, ..., Lkθ)
		if (substitutedUpperBounds.length == 1) {
			typeVariable.setUpperBounds(substitutedUpperBounds, this.object); // shortcut
		} else {
			TypeBinding[] glbs = Scope.greaterLowerBound(substitutedUpperBounds, this.scope, this.environment);
			if (glbs == null)
				return false;
			if (typeVariable.lowerBound != null) {
				for (int i = 0; i < glbs.length; i++) {
					if (!typeVariable.lowerBound.isCompatibleWith(glbs[i]))
						return false; // not well-formed
				}
			}
			// for deterministic results sort this array by id:
			sortTypes(glbs);
			if (!typeVariable.setUpperBounds(glbs, this.object))
				return false;
		}
		return true;
	}

	static void sortTypes(TypeBinding[] types) {
		Arrays.sort(types, new Comparator<TypeBinding>() {
			public int compare(TypeBinding o1, TypeBinding o2) {
				int i1 = o1.id, i2 = o2.id; 
				return (i1<i2 ? -1 : (i1==i2 ? 0 : 1));
			}
		});
	}

	/**
	 * Find the smallest set of uninstantiated inference variables not depending
	 * on any uninstantiated variable outside the set.
	 */
	private Set<InferenceVariable> getSmallestVariableSet(BoundSet bounds, InferenceVariable[] subSet) {
		int min = Integer.MAX_VALUE;
		Set<InferenceVariable> result = null;
		for (int i = 0; i < subSet.length; i++) {
			InferenceVariable currentVariable = subSet[i];
			if (!bounds.isInstantiated(currentVariable)) {
				Set<InferenceVariable> set = new HashSet<InferenceVariable>();
				if (!addDependencies(bounds, set, currentVariable, min))
					continue;
				int cur = set.size();
				if (cur == 1)
					return set; // won't get smaller
				if (cur < min) {
					result = set;
					min = cur;
				}
			}
		}
		return result;
	}

	private boolean addDependencies(BoundSet boundSet, Set<InferenceVariable> variableSet, InferenceVariable currentVariable, int min) {
		if (variableSet.size() >= min)
			return false; // no improvement
		if (boundSet.isInstantiated(currentVariable)) return true; // not added
		if (!variableSet.add(currentVariable)) return true; // already present
		for (int j = 0; j < this.inferenceVariables.length; j++) {
			InferenceVariable nextVariable = this.inferenceVariables[j];
			if (nextVariable == currentVariable) continue; //$IDENTITY-COMPARISON$ Inference variables
			if (boundSet.dependsOnResolutionOf(currentVariable, nextVariable))
				if (!addDependencies(boundSet, variableSet, nextVariable, min))
					return false; // abort traversal: no improvement
		}
		return true;
	}

	private ConstraintFormula pickFromCycle(Set<ConstraintFormula> c) {
		// Detail from 18.5.2 bullet 6.1

		// Note on performance: this implementation could quite possibly be optimized a lot.
		// However, we only *very rarely* reach here,
		// so nobody should really be affected by the performance penalty paid here.

		// Note on spec conformance: the spec seems to require _all_ criteria (i)-(iv) to be fulfilled
		// with the sole exception of (iii), which should only be used, if _any_ constraints matching (i) & (ii)
		// also fulfill this condition.
		// Experiments, however, show that strict application of the above is prone to failing to pick any constraint,
		// causing non-termination of the algorithm.
		// Since that is not acceptable, I'm *interpreting* the spec to request a search for a constraint
		// that "best matches" the given conditions.
		
		// collect all constraints participating in a cycle
		HashMap<ConstraintFormula,Set<ConstraintFormula>> dependencies = new HashMap<ConstraintFormula, Set<ConstraintFormula>>();
		Set<ConstraintFormula> cycles = new HashSet<ConstraintFormula>();
		for (ConstraintFormula constraint : c) {
			Collection<InferenceVariable> infVars = constraint.inputVariables(this);
			for (ConstraintFormula other : c) {
				if (other == constraint) continue;
				if (dependsOn(infVars, other.outputVariables(this))) {
					// found a dependency, record it:
					Set<ConstraintFormula> targetSet = dependencies.get(constraint);
					if (targetSet == null)
						dependencies.put(constraint, targetSet = new HashSet<ConstraintFormula>());
					targetSet.add(other);
					// look for a cycle:
					Set<ConstraintFormula> nodesInCycle = new HashSet<ConstraintFormula>();
					if (isReachable(dependencies, other, constraint, new HashSet<ConstraintFormula>(), nodesInCycle)) {
						// found a cycle, record the involved nodes:
						cycles.addAll(nodesInCycle);
					}
				}
			}
		}
		Set<ConstraintFormula> outside = new HashSet<ConstraintFormula>(c);
		outside.removeAll(cycles);

		Set<ConstraintFormula> candidatesII = new HashSet<ConstraintFormula>();
		// (i): participates in a cycle:
		candidates: for (ConstraintFormula candidate : cycles) {
			Collection<InferenceVariable> infVars = candidate.inputVariables(this);
			// (ii) does not depend on any constraints outside the cycle
			for (ConstraintFormula out : outside) {
				if (dependsOn(infVars, out.outputVariables(this)))
					continue candidates;
			}
			candidatesII.add(candidate);
		}
		if (candidatesII.isEmpty())
			candidatesII = c; // not spec'ed but needed to avoid returning null below, witness: java.util.stream.Collectors
		
		// tentatively: (iii)  has the form ⟨Expression → T⟩
		Set<ConstraintFormula> candidatesIII = new HashSet<ConstraintFormula>();
		for (ConstraintFormula candidate : candidatesII) {
			if (candidate instanceof ConstraintExpressionFormula)
				candidatesIII.add(candidate);
		}
		if (candidatesIII.isEmpty()) {
			candidatesIII = candidatesII; // no constraint fulfills (iii) -> ignore this condition
		} else { // candidatesIII contains all relevant constraints ⟨Expression → T⟩
			// (iv) contains an expression that appears to the left of the expression
			// 		of every other constraint satisfying the previous three requirements
			
			// collect containment info regarding all expressions in candidate constraints:
			// (a) find minimal enclosing expressions:
			Map<ConstraintExpressionFormula,ConstraintExpressionFormula> expressionContainedBy = new HashMap<ConstraintExpressionFormula, ConstraintExpressionFormula>();
			for (ConstraintFormula one : candidatesIII) {
				ConstraintExpressionFormula oneCEF = (ConstraintExpressionFormula) one;
				Expression exprOne = oneCEF.left;
				for (ConstraintFormula two : candidatesIII) {
					if (one == two) continue;
					ConstraintExpressionFormula twoCEF = (ConstraintExpressionFormula) two;
					Expression exprTwo = twoCEF.left;
					if (doesExpressionContain(exprOne, exprTwo)) {
						ConstraintExpressionFormula previous = expressionContainedBy.get(two);
						if (previous == null || doesExpressionContain(previous.left, exprOne)) // only if improving
							expressionContainedBy.put(twoCEF, oneCEF);
					}
				}
			}
			// (b) build the tree from the above
			Map<ConstraintExpressionFormula,Set<ConstraintExpressionFormula>> containmentForest = new HashMap<ConstraintExpressionFormula, Set<ConstraintExpressionFormula>>();
			for (Map.Entry<ConstraintExpressionFormula, ConstraintExpressionFormula> parentRelation : expressionContainedBy.entrySet()) {
				ConstraintExpressionFormula parent = parentRelation.getValue();
				Set<ConstraintExpressionFormula> children = containmentForest.get(parent);
				if (children == null)
					containmentForest.put(parent, children = new HashSet<ConstraintExpressionFormula>());
				children.add(parentRelation.getKey());
			}
			
			// approximate the spec by searching the largest containment tree:
			int bestRank = -1;
			ConstraintExpressionFormula candidate = null;
			for (ConstraintExpressionFormula parent : containmentForest.keySet()) {
				int rank = rankNode(parent, expressionContainedBy, containmentForest);
				if (rank > bestRank) {
					bestRank = rank;
					candidate = parent;
				}
			}
			if (candidate != null)
				return candidate;
		}
		
		if (candidatesIII.isEmpty())
			throw new IllegalStateException("cannot pick constraint from cyclic set"); //$NON-NLS-1$
		return candidatesIII.iterator().next();
	}

	/**
	 * Does the first constraint depend on the other?
	 * The first constraint is represented by its input variables and the other constraint by its output variables.
	 */
	private boolean dependsOn(Collection<InferenceVariable> inputsOfFirst, Collection<InferenceVariable> outputsOfOther) {
		for (InferenceVariable iv : inputsOfFirst) {
			for (InferenceVariable otherIV : outputsOfOther)
				if (this.currentBounds.dependsOnResolutionOf(iv, otherIV))
					return true;
		}
		return false;
	}

	/** Does 'deps' contain a chain of dependencies leading from 'from' to 'to'? */
	private boolean isReachable(Map<ConstraintFormula,Set<ConstraintFormula>> deps, ConstraintFormula from, ConstraintFormula to,
			Set<ConstraintFormula> nodesVisited, Set<ConstraintFormula> nodesInCycle)
	{
		if (from == to) {
			nodesInCycle.add(from);
			return true;
		}
		if (!nodesVisited.add(from))
			return false;
		Set<ConstraintFormula> targetSet = deps.get(from);
		if (targetSet != null) {
			for (ConstraintFormula tgt : targetSet) {
				if (isReachable(deps, tgt, to, nodesVisited, nodesInCycle)) {
					nodesInCycle.add(from);
					return true;
				}
			}
		}
		return false;
	}

	/** Does exprOne lexically contain exprTwo? */
	private boolean doesExpressionContain(Expression exprOne, Expression exprTwo) {
		if (exprTwo.sourceStart > exprOne.sourceStart) {
			return exprTwo.sourceEnd <= exprOne.sourceEnd;
		} else if (exprTwo.sourceStart == exprOne.sourceStart) {
			return exprTwo.sourceEnd < exprOne.sourceEnd;
		}
		return false;
	}

	/** non-roots answer -1, roots answer the size of the spanned tree */
	private int rankNode(ConstraintExpressionFormula parent, 
			Map<ConstraintExpressionFormula,ConstraintExpressionFormula> expressionContainedBy,
			Map<ConstraintExpressionFormula, Set<ConstraintExpressionFormula>> containmentForest)
	{
		if (expressionContainedBy.get(parent) != null)
			return -1; // not a root
		Set<ConstraintExpressionFormula> children = containmentForest.get(parent);
		if (children == null)
			return 1; // unconnected node or leaf
		int sum = 1;
		for (ConstraintExpressionFormula child : children) {
			int cRank = rankNode(child, expressionContainedBy, containmentForest);
			if (cRank > 0)
				sum += cRank;
		}
		return sum;
	}

	private Set<ConstraintFormula> findBottomSet(Set<ConstraintFormula> constraints, Set<InferenceVariable> allOutputVariables) {
		// 18.5.2 bullet 6.1
		//  A subset of constraints is selected, satisfying the property
		// that, for each constraint, no input variable depends on an
		// output variable of another constraint in C ...
		Set<ConstraintFormula> result = new HashSet<ConstraintFormula>();
		Iterator<ConstraintFormula> it = constraints.iterator();
		constraintLoop: while (it.hasNext()) {
			ConstraintFormula constraint = it.next();
			Iterator<InferenceVariable> inputIt = constraint.inputVariables(this).iterator();
			Iterator<InferenceVariable> outputIt = allOutputVariables.iterator();
			while (inputIt.hasNext()) {
				InferenceVariable in = inputIt.next();
				if (allOutputVariables.contains(in)) // not explicit in the spec, but let's assume any inference variable depends on itself
					continue constraintLoop;
				while (outputIt.hasNext()) {
					if (this.currentBounds.dependsOnResolutionOf(in, outputIt.next()))
						continue constraintLoop;
				}
			}
			result.add(constraint);
		}		
		return result;
	}

	Set<InferenceVariable> allOutputVariables(Set<ConstraintFormula> constraints) {
		Set<InferenceVariable> result = new HashSet<InferenceVariable>();
		Iterator<ConstraintFormula> it = constraints.iterator();
		while (it.hasNext()) {
			result.addAll(it.next().outputVariables(this));
		}
		return result;
	}

	private TypeBinding[] varArgTypes(TypeBinding[] parameters, int k) {
		TypeBinding[] types = new TypeBinding[k];
		int declaredLength = parameters.length-1;
		System.arraycopy(parameters, 0, types, 0, declaredLength);
		TypeBinding last = ((ArrayBinding)parameters[declaredLength]).elementsType();
		for (int i = declaredLength; i < k; i++)
			types[i] = last;
		return types;
	}
	
	public SuspendedInferenceRecord enterPolyInvocation(InvocationSite invocation, Expression[] innerArguments) {
		SuspendedInferenceRecord record = new SuspendedInferenceRecord(this.currentInvocation, this.invocationArguments, this.inferenceVariables, this.inferenceKind);
		this.inferenceVariables = null;
		this.invocationArguments = innerArguments;
		this.currentInvocation = invocation;
		
		// schedule for re-binding the inner after inference success:
		this.innerPolies.add(invocation);
		return record;
	}
	
	public SuspendedInferenceRecord enterLambda(LambdaExpression lambda) {
		SuspendedInferenceRecord record = new SuspendedInferenceRecord(this.currentInvocation, this.invocationArguments, this.inferenceVariables, this.inferenceKind);
		this.inferenceVariables = null;
		this.invocationArguments = null;
		this.currentInvocation = null;
		return record;
	}

	public void resumeSuspendedInference(SuspendedInferenceRecord record) {
		// merge inference variables:
		if (this.inferenceVariables == null) { // no new ones, assume we aborted prematurely
			this.inferenceVariables = record.inferenceVariables;
		} else {
			int l1 = this.inferenceVariables.length;
			int l2 = record.inferenceVariables.length;
			// move to back, add previous to front:
			System.arraycopy(this.inferenceVariables, 0, this.inferenceVariables=new InferenceVariable[l1+l2], l2, l1);
			System.arraycopy(record.inferenceVariables, 0, this.inferenceVariables, 0, l2);
		}

		// replace invocation site & arguments:
		this.currentInvocation = record.site;
		this.invocationArguments = record.invocationArguments;
		this.inferenceKind = record.inferenceKind;
	}

	public boolean rebindInnerPolies(MethodBinding method, InvocationSite site) {
		BoundSet bounds = this.currentBounds;
		TypeBinding targetType = site.invocationTargetType();
		if (targetType == null || !targetType.isProperType(true)) {
			if (!site.getExpressionContext().definesTargetType()) {
				// in this case we may not yet have the solution(?, get or compute it now:
				Solution solution = this.solutionsPerTargetType.get(targetType);
				try {
					if (solution != null && solution.bounds != null)
						bounds = solution.bounds;
					else
						bounds = inferInvocationType(this.currentBounds, null, site, method.shallowOriginal());
				} catch (InferenceFailureException e) {
					return false;
				}
				if (bounds == null)
					return false;
			}
		} else {
			Solution solution = this.solutionsPerTargetType.get(targetType);
			if (solution != null && solution.bounds != null)
				bounds = solution.bounds;
		}
		rebindInnerPolies(bounds, method.parameters);
		return true;
	}

	/**
	 * After inference has finished, iterate all inner poly expressions (Invocations), that
	 * have been included in the inference. For each of these update some type information
	 * from the inference result and perhaps trigger follow-up resolving as needed.
	 * Similar for poly expressions that did not directly participate in the inference
	 * but are direct arguments of the current invocation (FunctionalExpression, ConditionalExpression).
	 */
	public void rebindInnerPolies(BoundSet bounds, TypeBinding[] parameterTypes) {
		// This updates all remaining poly expressions that are direct arguments of the current invocation:
		// (handles FunctionalExpression & ConditionalExpression)
		if (this.currentInvocation instanceof ReferenceExpression)
			return; // no inner expressions
		boolean isVarargs = this.inferenceKind == CHECK_VARARG;
		acceptPendingPolyArguments(bounds, parameterTypes, isVarargs);
		// This loops over all poly expressions for which a sub-inference was triggered:
		// (handles generic invocations)
		int len = this.innerPolies.size();
		for (int i = 0; i < len; i++) {
			Expression inner = (Expression) this.innerPolies.get(i);
			if (inner instanceof ReferenceExpression) {
				ReferenceExpression referenceExpression = (ReferenceExpression) inner;
				MethodBinding compileTimeDecl = referenceExpression.prepareForInferenceResult(this.scope);
				if	(compileTimeDecl != null) {
	  				TypeVariableBinding[] variables = compileTimeDecl.typeVariables;
					if (variables != Binding.NO_TYPE_VARIABLES) {
						TypeBinding[] arguments = getSolutions(variables, (InvocationSite) inner, bounds);
						referenceExpression.binding = this.environment.createParameterizedGenericMethod(compileTimeDecl, arguments);
					}
				}
			} else if (inner instanceof Invocation) {
				Invocation innerMessage = (Invocation) inner;
				TypeBinding innerTargetType = inner.expectedType(); // may be set from acceptPendingPolyArguments
				if (innerTargetType != null && !innerTargetType.isProperType(true))
					innerTargetType = null;
				MethodBinding binding = innerMessage.binding(innerTargetType, innerTargetType != null, this.scope);
				if (binding == null)
					continue;
				MethodBinding original = binding.shallowOriginal();

				// apply inference results onto the allocation type of inner diamonds:
				if (original.isConstructor() && inner.isPolyExpression()) {
					ReferenceBinding declaringClass = original.declaringClass;
					TypeBinding[] arguments = getSolutions(declaringClass.typeVariables(), innerMessage, bounds);
					declaringClass = this.environment.createParameterizedType(declaringClass, arguments, declaringClass.enclosingType());
					original = ((ParameterizedTypeBinding)declaringClass).createParameterizedMethod(original);
					inner.checkAgainstFinalTargetType(innerTargetType, this.scope);	
					if (this.environment.globalOptions.isAnnotationBasedNullAnalysisEnabled)
						NullAnnotationMatching.checkForContraditions(original, innerMessage, this.scope);
				}
				
				// apply results of the combined inference onto the binding of the inner invocation:
				TypeBinding[] solutions = getSolutions(original.typeVariables(), innerMessage, bounds);
				if (solutions == null) {
					if (binding instanceof ParameterizedGenericMethodBinding) {
						InferenceContext18 innerCtx = innerMessage.getInferenceContext((ParameterizedGenericMethodBinding) binding);
						if (innerCtx != null && !binding.isValidBinding()) {
							innerCtx.reportInvalidInvocation(innerMessage, binding);
						}
					}
					continue; // inner inference not requested -> not a problem
				}
				ParameterizedGenericMethodBinding innerBinding = this.environment.createParameterizedGenericMethod(original, solutions);
				
				if (innerMessage.updateBindings(innerBinding, innerTargetType)) { // only if we are actually improving anything
					ASTNode.resolvePolyExpressionArguments(innerMessage, innerBinding, this.scope);
				}
			}
		}
		this.stepCompleted = BINDINGS_UPDATED; // we're done-done
	}

	private void acceptPendingPolyArguments(BoundSet acceptedResult, TypeBinding[] parameterTypes, boolean isVarArgs) {
		if (acceptedResult == null || this.invocationArguments == null) return;
		Substitution substitution = getResultSubstitution(acceptedResult);
		for (int i = 0; i < this.invocationArguments.length; i++) {
			TypeBinding targetType = getParameter(parameterTypes, i, isVarArgs);
			if (!targetType.isProperType(true))
				targetType = Scope.substitute(substitution, targetType);
			Expression expression = this.invocationArguments[i];
			if (expression instanceof Invocation) {
				Invocation invocation = (Invocation) expression;
				if (!this.innerPolies.contains(invocation)) {
					MethodBinding method = invocation.binding(targetType, true, this.scope);
					if (method instanceof ParameterizedGenericMethodBinding) {
						ParameterizedGenericMethodBinding previousBinding = (ParameterizedGenericMethodBinding) method;
						InferenceContext18 innerCtx = invocation.getInferenceContext(previousBinding);
						if (innerCtx != null) {
							// we have a non-poly generic invocation, which needs inference but is not connected via innerPolis.
							// Finish that inner inference now (incl. binding updates):
							MethodBinding innerBinding = innerCtx.inferInvocationType(invocation, previousBinding);
							if (!innerBinding.isValidBinding()) {
								innerCtx.reportInvalidInvocation(invocation, innerBinding);
							}
							if (invocation.updateBindings(innerBinding, targetType)) { // only if we are actually improving anything
								ASTNode.resolvePolyExpressionArguments(invocation, innerBinding, this.scope);
							}
						}
					} else if(method instanceof ParameterizedMethodBinding){
						expression.checkAgainstFinalTargetType(targetType, this.scope);
					}
				} else {
					expression.setExpectedType(targetType);
				}
			} else {
				if (this.innerPolies.contains(expression)) // may get here for ReferenceExpressions ...
					expression.setExpectedType(targetType); // ... prepare for final inference via rebindInnerPolies
				else
					expression.checkAgainstFinalTargetType(targetType, this.scope);
			}
		}
	}

	private Substitution getResultSubstitution(final BoundSet result) {
		return new Substitution() {
			public LookupEnvironment environment() { 
				return InferenceContext18.this.environment;
			}
			public boolean isRawSubstitution() {
				return false;
			}
			public TypeBinding substitute(TypeVariableBinding typeVariable) {
				if (typeVariable instanceof InferenceVariable) {
					return result.getInstantiation((InferenceVariable) typeVariable, InferenceContext18.this.environment);
				}
				return typeVariable;
			}
		};
	}

	public boolean isVarArgs() {
		return this.inferenceKind == CHECK_VARARG;
	}

	/**
	 * Retrieve the rank'th parameter, possibly respecting varargs invocation, see 15.12.2.4.
	 * Returns null if out of bounds and CHECK_VARARG was not requested.
	 * Precondition: isVarArgs implies method.isVarargs()
	 */
	public static TypeBinding getParameter(TypeBinding[] parameters, int rank, boolean isVarArgs) {
		if (isVarArgs) {
			if (rank >= parameters.length-1)
				return ((ArrayBinding)parameters[parameters.length-1]).elementsType();			
		} else if (rank >= parameters.length) {
			return null;
		}
		return parameters[rank];
	}

	/**
	 * Create a problem method signaling failure of invocation type inference,
	 * unless the given candidate is tolerable to be compatible with buggy javac.
	 */
	public MethodBinding getReturnProblemMethodIfNeeded(TypeBinding expectedType, MethodBinding method) {
		if (InferenceContext18.SIMULATE_BUG_JDK_8026527 && expectedType != null && method.returnType instanceof ReferenceBinding) {
			if (method.returnType.erasure().isCompatibleWith(expectedType))
				return method; // don't count as problem.
		}
		if (expectedType == null)
			return method; // assume inference failure concerned another expression
		ProblemMethodBinding problemMethod = new ProblemMethodBinding(method, method.selector, method.parameters, ProblemReasons.ParameterizedMethodExpectedTypeProblem);
		problemMethod.returnType = expectedType;
		problemMethod.inferenceContext = this;
		return problemMethod;
	}

	public void reportInvalidInvocation(Invocation invocation, MethodBinding binding) {
		if (invocation instanceof MessageSend)
			this.scope.problemReporter().invalidMethod((MessageSend) invocation, binding);
		else
			this.scope.problemReporter().invalidConstructor((Statement)invocation, binding);
	}

	// debugging:
	public String toString() {
		StringBuffer buf = new StringBuffer("Inference Context"); //$NON-NLS-1$
		switch (this.stepCompleted) {
			case NOT_INFERRED: buf.append(" (initial)");break; //$NON-NLS-1$
			case APPLICABILITY_INFERRED: buf.append(" (applicability inferred)");break; //$NON-NLS-1$
			case TYPE_INFERRED: buf.append(" (type inferred)");break; //$NON-NLS-1$
			case BINDINGS_UPDATED: buf.append(" (bindings updated)");break; //$NON-NLS-1$
		}
		switch (this.inferenceKind) {
			case CHECK_STRICT: buf.append(" (strict)");break; //$NON-NLS-1$
			case CHECK_LOOSE: buf.append(" (loose)");break; //$NON-NLS-1$
			case CHECK_VARARG: buf.append(" (vararg)");break; //$NON-NLS-1$
		}
		if (this.currentBounds != null && isResolved(this.currentBounds))
			buf.append(" (resolved)"); //$NON-NLS-1$
		buf.append('\n');
		if (this.inferenceVariables != null) {
			buf.append("Inference Variables:\n"); //$NON-NLS-1$
			for (int i = 0; i < this.inferenceVariables.length; i++) {
				buf.append('\t').append(this.inferenceVariables[i].sourceName).append("\t:\t"); //$NON-NLS-1$
				if (this.currentBounds != null && this.currentBounds.isInstantiated(this.inferenceVariables[i]))
					buf.append(this.currentBounds.getInstantiation(this.inferenceVariables[i], this.environment).readableName());
				else
					buf.append("NOT INSTANTIATED"); //$NON-NLS-1$
				buf.append('\n');
			}
		}
		if (this.initialConstraints != null) {
			buf.append("Initial Constraints:\n"); //$NON-NLS-1$
			for (int i = 0; i < this.initialConstraints.length; i++)
				if (this.initialConstraints[i] != null)
					buf.append('\t').append(this.initialConstraints[i].toString()).append('\n');
		}
		if (this.currentBounds != null)
			buf.append(this.currentBounds.toString());
		return buf.toString();
	}

	public void addProblemMethod(ProblemMethodBinding problemMethod) {
		if (this.problemMethods == null)
			this.problemMethods = new ArrayList<MethodBinding>();
		this.problemMethods.add(problemMethod);
	}

	/**
	 * If 'type' is a parameterized type and one of its arguments is a wildcard answer the casted type, else null.
	 * A nonnull answer is ensured to also have nonnull arguments.
	 */
	public static ParameterizedTypeBinding parameterizedWithWildcard(TypeBinding type) {
		if (type == null || type.kind() != Binding.PARAMETERIZED_TYPE)
			return null;
		ParameterizedTypeBinding parameterizedType = (ParameterizedTypeBinding) type;
		TypeBinding[] arguments = parameterizedType.arguments;
		if (arguments != null) {
			for (int i = 0; i < arguments.length; i++)
				if (arguments[i].isWildcard())
					return parameterizedType;
		}
		return null;
	}

	public TypeBinding[] getFunctionInterfaceArgumentSolutions(TypeBinding[] a) {
		int m = a.length;
		TypeBinding[] aprime = new TypeBinding[m];
		for (int i = 0; i < this.inferenceVariables.length; i++) {
			InferenceVariable alphai = this.inferenceVariables[i];
			TypeBinding t = this.currentBounds.getInstantiation(alphai, this.environment);
			if (t != null)
				aprime[i] = t;
			else
				aprime[i] = a[i];
		}
		return aprime;
	}

	/** Record the fact that the given constraint requires unchecked conversion. */
	public void recordUncheckedConversion(ConstraintTypeFormula constraint) {
		if (this.constraintsWithUncheckedConversion == null)
			this.constraintsWithUncheckedConversion = new ArrayList<ConstraintFormula>();
		this.constraintsWithUncheckedConversion.add(constraint);
	}
	
	void reportUncheckedConversions(BoundSet solution) {
		if (this.constraintsWithUncheckedConversion != null) {
			int len = this.constraintsWithUncheckedConversion.size();
			Substitution substitution = getResultSubstitution(solution);
			for (int i = 0; i < len; i++) {
				ConstraintTypeFormula constraint = (ConstraintTypeFormula) this.constraintsWithUncheckedConversion.get(i);
				TypeBinding expectedType = constraint.right;
				TypeBinding providedType = constraint.left;
				if (!expectedType.isProperType(true)) {
					expectedType = Scope.substitute(substitution, expectedType);
				}
				if (!providedType.isProperType(true)) {
					providedType = Scope.substitute(substitution, providedType);
				}
/* FIXME(stephan): enable once we solved:
                    (a) avoid duplication with traditional reporting
                    (b) improve location to report against
				if (this.currentInvocation instanceof Expression)
					this.scope.problemReporter().unsafeTypeConversion((Expression) this.currentInvocation, providedType, expectedType);
 */
			}
		}
	}
	
	/** For use by 15.12.2.6 Method Invocation Type */
	public boolean usesUncheckedConversion() {
		return this.constraintsWithUncheckedConversion != null;
	}

	// INTERIM: infrastructure for detecting failures caused by specific known incompleteness:
	public static void missingImplementation(String msg) {
		throw new UnsupportedOperationException(msg);
	}
}
