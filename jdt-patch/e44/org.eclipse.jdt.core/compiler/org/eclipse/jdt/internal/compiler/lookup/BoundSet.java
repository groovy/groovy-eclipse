/*******************************************************************************
 * Copyright (c) 2013, 2014 GK Software AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.internal.compiler.ast.Wildcard;

/**
 * Implementation of 18.1.3 in JLS8.
 * This class is also responsible for incorporation as defined in 18.3.
 */
class BoundSet {

	static final BoundSet TRUE = new BoundSet();	// empty set of bounds
	static final BoundSet FALSE = new BoundSet();	// pseudo bounds
	
	/**
	 * For a given inference variable this structure holds all type bounds
	 * with a relation in { SUPERTYPE, SAME, SUBTYPE }.
	 * These are internally stored in three sets, one for each of the relations.
	 */
	private class ThreeSets {
		Set<TypeBound> superBounds;
		Set<TypeBound> sameBounds;
		Set<TypeBound> subBounds;
		TypeBinding	instantiation;
		Map<InferenceVariable,TypeBound> inverseBounds; // from right inference variable to bound
		
		public ThreeSets() {
			// empty, the sets are lazily initialized
		}
		/** Add a type bound to the appropriate set. */
		public void addBound(TypeBound bound) {
			switch (bound.relation) {
				case ReductionResult.SUPERTYPE:
					if (this.superBounds == null) this.superBounds = new HashSet<TypeBound>();
					this.superBounds.add(bound);
					break;
				case ReductionResult.SAME:
					if (this.sameBounds == null) this.sameBounds = new HashSet<TypeBound>();
					this.sameBounds.add(bound);
					break;
				case ReductionResult.SUBTYPE:
					if (this.subBounds == null) this.subBounds = new HashSet<TypeBound>();
					this.subBounds.add(bound);
					break;
				default:
					throw new IllegalArgumentException("Unexpected bound relation in : " + bound); //$NON-NLS-1$
			}
		}
		// pre: this.superBounds != null
		public TypeBinding[] lowerBounds(boolean onlyProper, InferenceVariable variable) {
			TypeBinding[] boundTypes = new TypeBinding[this.superBounds.size()];
			Iterator<TypeBound> it = this.superBounds.iterator();
			long nullHints = variable.nullHints;
			int i = 0;
			while(it.hasNext()) {
				TypeBound current = it.next();
				TypeBinding boundType = current.right;
				if (!onlyProper || boundType.isProperType(true)) {
					boundTypes[i++] = boundType;
					nullHints |= current.nullHints;
				}
			}
			if (i == 0)
				return Binding.NO_TYPES;
			if (i < boundTypes.length)
				System.arraycopy(boundTypes, 0, boundTypes=new TypeBinding[i], 0, i);
			useNullHints(nullHints, boundTypes, variable.environment);
			InferenceContext18.sortTypes(boundTypes);
			return boundTypes;
		}
		// pre: this.subBounds != null
		public TypeBinding[] upperBounds(boolean onlyProper, InferenceVariable variable) {
			ReferenceBinding[] rights = new ReferenceBinding[this.subBounds.size()];
			TypeBinding simpleUpper = null;
			Iterator<TypeBound> it = this.subBounds.iterator();
			long nullHints = variable.nullHints;
			int i = 0;
			while(it.hasNext()) {
				TypeBinding right=it.next().right;
				if (!onlyProper || right.isProperType(true)) {
					if (right instanceof ReferenceBinding) {
						rights[i++] = (ReferenceBinding) right;
						nullHints |= right.tagBits & TagBits.AnnotationNullMASK; 
					} else {
						if (simpleUpper != null)
							return Binding.NO_TYPES; // shouldn't
						simpleUpper = right;
					}
				}
			}
			if (i == 0)
				return Binding.NO_TYPES;
			if (i == 1 && simpleUpper != null)
				return new TypeBinding[] { simpleUpper }; // no nullHints since not a reference type
			if (i < rights.length)
				System.arraycopy(rights, 0, rights=new ReferenceBinding[i], 0, i);
			useNullHints(nullHints, rights, variable.environment);
			InferenceContext18.sortTypes(rights);
			return rights;
		}
		public boolean hasDependency(InferenceVariable beta) {
			if (this.superBounds != null && hasDependency(this.superBounds, beta))
				return true;
			if (this.sameBounds != null && hasDependency(this.sameBounds, beta))
				return true;
			if (this.subBounds != null && hasDependency(this.subBounds, beta))
				return true;
			if (this.inverseBounds != null) {
				if (this.inverseBounds.containsKey(beta)) {
					// TODO: not yet observed in tests
					return true;
				}
			}
			return false;
		}
		private boolean hasDependency(Set<TypeBound> someBounds, InferenceVariable var) {
			Iterator<TypeBound> bIt = someBounds.iterator();
			while (bIt.hasNext()) {
				TypeBound bound = bIt.next();
				if (bound.right == var || bound.right.mentionsAny(new TypeBinding[] {var}, -1)) //$IDENTITY-COMPARISON$ InferenceVariable
					return true;
			}
			return false;
		}
		/** Total number of type bounds in this container. */
		public int size() {
			int size = 0;
			if (this.superBounds != null)
				size += this.superBounds.size();
			if (this.sameBounds != null)
				size += this.sameBounds.size();
			if (this.subBounds != null)
				size += this.subBounds.size();
			return size;
		}
		public int flattenInto(TypeBound[] collected, int idx) {
			if (this.superBounds != null) {
				int len = this.superBounds.size();
				System.arraycopy(this.superBounds.toArray(), 0, collected, idx, len);
				idx += len;
			}
			if (this.sameBounds != null) {
				int len = this.sameBounds.size();
				System.arraycopy(this.sameBounds.toArray(), 0, collected, idx, len);
				idx += len;
			}
			if (this.subBounds != null) {
				int len = this.subBounds.size();
				System.arraycopy(this.subBounds.toArray(), 0, collected, idx, len);
				idx += len;
			}
			return idx;
		}
		public ThreeSets copy() {
			ThreeSets copy = new ThreeSets();
			if (this.superBounds != null)
				copy.superBounds = new HashSet<TypeBound>(this.superBounds);
			if (this.sameBounds != null)
				copy.sameBounds = new HashSet<TypeBound>(this.sameBounds);
			if (this.subBounds != null)
				copy.subBounds = new HashSet<TypeBound>(this.subBounds);
			copy.instantiation = this.instantiation;
			return copy;
		}
		public TypeBinding findSingleWrapperType() {
			TypeBinding wrapperBound = null;
			if (this.subBounds != null) {
				Iterator<TypeBound> it = this.subBounds.iterator();
				while(it.hasNext()) {
					TypeBinding boundType = it.next().right;
					if ((boundType).isProperType(true)) {
						switch (boundType.id) {
							case TypeIds.T_JavaLangByte:
							case TypeIds.T_JavaLangShort:
							case TypeIds.T_JavaLangCharacter:
							case TypeIds.T_JavaLangInteger:
							case TypeIds.T_JavaLangLong:
							case TypeIds.T_JavaLangFloat:
							case TypeIds.T_JavaLangDouble:
							case TypeIds.T_JavaLangBoolean:
								if (wrapperBound != null)
									return null;
								wrapperBound = boundType;
						}
					}
				}		
			}
			if (this.superBounds != null) {
				Iterator<TypeBound> it = this.superBounds.iterator();
				while(it.hasNext()) {
					TypeBinding boundType = it.next().right;
					if ((boundType).isProperType(true)) {
						switch (boundType.id) {
							case TypeIds.T_JavaLangByte:
							case TypeIds.T_JavaLangShort:
							case TypeIds.T_JavaLangCharacter:
							case TypeIds.T_JavaLangInteger:
							case TypeIds.T_JavaLangLong:
							case TypeIds.T_JavaLangFloat:
							case TypeIds.T_JavaLangDouble:
							case TypeIds.T_JavaLangBoolean:
								if (wrapperBound != null)
									return null;
								wrapperBound = boundType;
						}
					}
				}		
			}
			return wrapperBound;
		}
		/**
		 * Not per JLS: enhance the given type bounds using the nullHints, if useful.
		 * Will only ever be effective if any TypeBounds carry nullHints,
		 * which only happens if any TypeBindings have non-zero (tagBits & AnnotationNullMASK),
		 * which only happens if null annotations are enabled in the first place.
		 */
		private void useNullHints(long nullHints, TypeBinding[] boundTypes, LookupEnvironment environment) {
			if (nullHints == TagBits.AnnotationNullMASK) {
				// on contradiction remove null type annotations
				for (int i = 0; i < boundTypes.length; i++)
					boundTypes[i] = boundTypes[i].unannotated(true);
			} else {
				AnnotationBinding[] annot = environment.nullAnnotationsFromTagBits(nullHints);
				if (annot != null) {
					// only get here if exactly one of @NonNull or @Nullable was hinted; now apply this hint:
					for (int i = 0; i < boundTypes.length; i++)
						boundTypes[i] = environment.createAnnotatedType(boundTypes[i].unannotated(true), annot);
				}
			}
		}
		TypeBinding combineAndUseNullHints(TypeBinding type, long nullHints, LookupEnvironment environment) {
			// precondition: only called when null annotations are enabled.
			// TODO(optimization): may want to collect all nullHints in the ThreeSets, which, however,
			// needs a reference TypeBound->ThreeSets to propagate the bits as they are added.
			if (this.sameBounds != null) {
				Iterator<TypeBound> it = this.sameBounds.iterator();
				while(it.hasNext())
					nullHints |= it.next().nullHints;
			}
			if (this.superBounds != null) {
				Iterator<TypeBound> it = this.superBounds.iterator();
				while(it.hasNext())
					nullHints |= it.next().nullHints;
			}
			if (this.subBounds != null) {
				Iterator<TypeBound> it = this.subBounds.iterator();
				while(it.hasNext())
					nullHints |= it.next().nullHints;
			}
			if (nullHints == TagBits.AnnotationNullMASK) // on contradiction remove null type annotations
				return type.unannotated(true);
			AnnotationBinding[] annot = environment.nullAnnotationsFromTagBits(nullHints);
			if (annot != null)
				// only get here if exactly one of @NonNull or @Nullable was hinted; now apply this hint:
				return environment.createAnnotatedType(type.unannotated(true), annot);
			return type;
		}
		public void setInstantiation(TypeBinding type, InferenceVariable variable, LookupEnvironment environment) {
			if (environment.globalOptions.isAnnotationBasedNullAnalysisEnabled) {
				long oldBits = ((this.instantiation != null) ? this.instantiation.tagBits : variable.tagBits)
								& TagBits.AnnotationNullMASK;
				long requestedBits = type.tagBits & TagBits.AnnotationNullMASK;
				long newBits = (oldBits == TagBits.AnnotationNonNull) ? oldBits : requestedBits; // need to preserve @NonNull
				if (this.instantiation != null && oldBits == newBits) {
					return; // no update needed
				}
				if (requestedBits != newBits) {
					// adjust 'type' to fit the newBits
					AnnotationBinding[] annot = environment.nullAnnotationsFromTagBits(newBits);
					if (annot != null)
						type = environment.createAnnotatedType(type.unannotated(true), annot);
				}
			}
			this.instantiation = type;
		}
	}
	// main storage of type bounds:
	HashMap<InferenceVariable, ThreeSets> boundsPerVariable = new HashMap<InferenceVariable, ThreeSets>();
	
	/**
	 * 18.1.3 bullet 4: G<α1, ..., αn> = capture(G<A1, ..., An>)
	 * On both sides we only enter types with nonnull arguments. 
	 */
	HashMap<ParameterizedTypeBinding,ParameterizedTypeBinding> captures = new HashMap<ParameterizedTypeBinding, ParameterizedTypeBinding>();
	/** 18.1.3 bullet 5: throws α */
	Set<InferenceVariable> inThrows = new HashSet<InferenceVariable>();

	// avoid attempts to incorporate the same pair of type bounds more than once:
	Set<TypeBound> incorporatedBounds = new HashSet<TypeBound>();

	public BoundSet() {}
	
	// pre: typeParameters != null, variables[i].typeParameter == typeParameters[i]
	public void addBoundsFromTypeParameters(InferenceContext18 context, TypeVariableBinding[] typeParameters, InferenceVariable[] variables) {
		int length = typeParameters.length;
		for (int i = 0; i < length; i++) {
			TypeVariableBinding typeParameter = typeParameters[i];
			InferenceVariable variable = variables[i];
			TypeBound[] someBounds = typeParameter.getTypeBounds(variable, new InferenceSubstitution(context.environment, context.inferenceVariables));
			boolean hasProperBound = false;
			if (someBounds.length > 0)
				hasProperBound = addBounds(someBounds, context.environment);
			if (!hasProperBound)
				addBound(new TypeBound(variable, context.object, ReductionResult.SUBTYPE), context.environment);
		}
	}

	/** Answer a flat representation of this BoundSet. */
	public TypeBound[] flatten() {
		int size = 0;
		Iterator<ThreeSets> outerIt = this.boundsPerVariable.values().iterator();
		while (outerIt.hasNext())
			size += outerIt.next().size();
		TypeBound[] collected = new TypeBound[size];
		if (size == 0) return collected;
		outerIt = this.boundsPerVariable.values().iterator();
		int idx = 0;
		while (outerIt.hasNext())
			idx = outerIt.next().flattenInto(collected, idx);
		return collected;
	}

	/**
	 * For resolution we work with a copy of the bound set, to enable retrying.
	 * @return the new bound set.
	 */
	public BoundSet copy() {
		BoundSet copy = new BoundSet();
		Iterator<Entry<InferenceVariable, ThreeSets>> setsIterator = this.boundsPerVariable.entrySet().iterator();
		while (setsIterator.hasNext()) {
			Entry<InferenceVariable, ThreeSets> entry = setsIterator.next();
			copy.boundsPerVariable.put(entry.getKey(), entry.getValue().copy());
		}
		copy.inThrows.addAll(this.inThrows);
		copy.captures.putAll(this.captures);
		return copy;
	}

	public void addBound(TypeBound bound, LookupEnvironment environment) {
		ThreeSets three = this.boundsPerVariable.get(bound.left);
		if (three == null)
			this.boundsPerVariable.put(bound.left, (three = new ThreeSets()));
		three.addBound(bound);
		// check if this makes the inference variable instantiated:
		TypeBinding typeBinding = bound.right;
		if (bound.relation == ReductionResult.SAME && typeBinding.isProperType(true))
			three.setInstantiation(typeBinding, bound.left, environment);
		if (bound.right instanceof InferenceVariable) {
			// for a dependency between two IVs make a note about the inverse bound.
			// this should be needed to determine IV dependencies independent of direction.
			// TODO: so far no test could be identified which actually needs it ...
			InferenceVariable rightIV = (InferenceVariable) bound.right;
			three = this.boundsPerVariable.get(rightIV);
			if (three == null)
				this.boundsPerVariable.put(rightIV, (three = new ThreeSets()));
			if (three.inverseBounds == null)
				three.inverseBounds = new HashMap<InferenceVariable,TypeBound>();
			three.inverseBounds.put(rightIV, bound);
		}
	}

	private boolean addBounds(TypeBound[] newBounds, LookupEnvironment environment) {
		boolean hasProperBound = false;
		for (int i = 0; i < newBounds.length; i++) {
			addBound(newBounds[i], environment);
			hasProperBound |= newBounds[i].isBound();
		}
		return hasProperBound;
	}

	public boolean isInstantiated(InferenceVariable inferenceVariable) {
		ThreeSets three = this.boundsPerVariable.get(inferenceVariable);
		if (three != null)
			return three.instantiation != null;
		return false;
	}

	public TypeBinding getInstantiation(InferenceVariable inferenceVariable, LookupEnvironment environment) {
		ThreeSets three = this.boundsPerVariable.get(inferenceVariable);
		if (three != null) {
			TypeBinding instantiation = three.instantiation;
			if (environment != null && environment.globalOptions.isAnnotationBasedNullAnalysisEnabled 
					&& instantiation != null && (instantiation.tagBits & TagBits.AnnotationNullMASK) == 0)
				return three.combineAndUseNullHints(instantiation, inferenceVariable.nullHints, environment);
			return instantiation;
		}
		return null;
	}

	public int numUninstantiatedVariables(InferenceVariable[] variables) {
		int num = 0;
		for (int i = 0; i < variables.length; i++) {
			if (!isInstantiated(variables[i]))
				num++;
		}
		return num;
	}

	/**
	 * <b>JLS 18.3:</b> Try to infer new constraints from pairs of existing type bounds.
	 * Each new constraint is first reduced and checked for TRUE or FALSE, which will
	 * abort the processing. 
	 * @param context the context that manages our inference variables
	 * @return false if any constraint resolved to false, true otherwise  
	 * @throws InferenceFailureException a compile error has been detected during inference
	 */
	boolean incorporate(InferenceContext18 context) throws InferenceFailureException {
		boolean hasUpdate;
		do {
			hasUpdate = false;
			// using a flattened copy also allows us to insert more bounds during the process
			// without disturbing the current round of incorporation:
			TypeBound[] bounds = flatten();
			int boundsCount = bounds.length;
			if (boundsCount < 2)
				return true;
			// check each pair:
			for (int i = 0; i < boundsCount; i++) {
				TypeBound boundI = bounds[i];
				for (int j = i+1; j < boundsCount; j++) {
					TypeBound boundJ = bounds[j];
					if (this.incorporatedBounds.contains(boundI) && this.incorporatedBounds.contains(boundJ))
						continue;
					ConstraintTypeFormula newConstraint = null;
					switch (boundI.relation) {
						case ReductionResult.SAME:
							switch (boundJ.relation) {
								case ReductionResult.SAME:
									newConstraint = combineSameSame(boundI, boundJ);
									break;
								case ReductionResult.SUBTYPE:
								case ReductionResult.SUPERTYPE:
									newConstraint = combineSameSubSuper(boundI, boundJ);
									break;
							}
							break;
						case ReductionResult.SUBTYPE:
							switch (boundJ.relation) {
								case ReductionResult.SAME:
									newConstraint = combineSameSubSuper(boundJ, boundI);
									break;
								case ReductionResult.SUPERTYPE:
									newConstraint = combineSuperAndSub(boundJ, boundI);
									break;
								case ReductionResult.SUBTYPE:
									newConstraint = combineEqualSupers(boundI, boundJ);
									break;
							}
							break;
						case ReductionResult.SUPERTYPE:
							switch (boundJ.relation) {
								case ReductionResult.SAME:
									newConstraint = combineSameSubSuper(boundJ, boundI);
									break;
								case ReductionResult.SUBTYPE:
									newConstraint = combineSuperAndSub(boundI, boundJ);
									break;
								case ReductionResult.SUPERTYPE:
									newConstraint = combineEqualSupers(boundI, boundJ);
									break;
							}
					}
					if (newConstraint != null) {
						if (!reduceOneConstraint(context, newConstraint))
							return false;
						// TODO here and below: better checking if constraint really added to the boundset (optimization)?
						hasUpdate = true;
						// not per JLS: if the new constraint relates types where at least one has a null annotations,
						// record all null tagBits as hints for the final inference solution.
						long nullHints = (newConstraint.left.tagBits | newConstraint.right.tagBits) & TagBits.AnnotationNullMASK;
						boundI.nullHints |= nullHints;
						boundJ.nullHints |= nullHints;
					}
					ConstraintFormula[] typeArgumentConstraints = deriveTypeArgumentConstraints(boundI, boundJ);
					if (typeArgumentConstraints != null) {
						for (int k = 0; k < typeArgumentConstraints.length; k++) {
							if (!reduceOneConstraint(context, typeArgumentConstraints[k]))
								return false;
							hasUpdate = true;
						}
					}
				}
				this.incorporatedBounds.add(boundI);
			}
			/* TODO: are we sure this will always terminate? Cf. e.g. (Discussion in 18.3):
			 *  
			 *    "The assertion that incorporation reaches a fixed point oversimplifies the matter slightly. ..."
			 */
			Iterator<Entry<ParameterizedTypeBinding, ParameterizedTypeBinding>> captIter = this.captures.entrySet().iterator();
			while (captIter.hasNext()) {
				hasUpdate = true;
				Entry<ParameterizedTypeBinding, ParameterizedTypeBinding> capt = captIter.next();
				ParameterizedTypeBinding gAlpha = capt.getKey();
				ParameterizedTypeBinding gA = capt.getValue();
				ReferenceBinding g = (ReferenceBinding) gA.original();
				final TypeVariableBinding[] parameters = g.typeVariables();
				// construct theta = [P1:=alpha1,...]
				final InferenceVariable[] alphas = new InferenceVariable[gAlpha.arguments.length];
				System.arraycopy(gAlpha.arguments, 0, alphas, 0, alphas.length);
				InferenceSubstitution theta = new InferenceSubstitution(context.environment, alphas) {
					@Override
					protected TypeBinding getP(int i) {
						return parameters[i];
					}
				};
				for (int i = 0; i < parameters.length; i++) {
					// A set of bounds on α1, ..., αn, constructed from the declared bounds of P1, ..., Pn as described in 18.1.3, is immediately implied.
					TypeVariableBinding pi = parameters[i];
					InferenceVariable alpha = (InferenceVariable) gAlpha.arguments[i];
					addBounds(pi.getTypeBounds(alpha, theta), context.environment);

					TypeBinding ai = gA.arguments[i];
					if (ai instanceof WildcardBinding) {
						WildcardBinding wildcardBinding = (WildcardBinding)ai;
						TypeBinding t = wildcardBinding.bound;
						ThreeSets three = this.boundsPerVariable.get(alpha);
						if (three != null) {
							Iterator<TypeBound> it;
							if (three.sameBounds != null) {
								//  α = R implies false
								it = three.sameBounds.iterator();
								while (it.hasNext()) {
									TypeBound bound = it.next();
									if (!(bound.right instanceof InferenceVariable))
										return false;
								}
							}
							if (three.subBounds != null && pi.firstBound != null) {
								// If Bi is Object, α <: R implies ⟨T <: R⟩	(extends wildcard)
								// α <: R implies ⟨θ Bi <: R⟩				(else) 
								it = three.subBounds.iterator();
								while (it.hasNext()) {
									TypeBound bound = it.next();
									if (!(bound.right instanceof InferenceVariable)) {
										TypeBinding r = bound.right;
										TypeBinding bi1 = pi.firstBound;
										ReferenceBinding[] otherBounds = pi.superInterfaces;
										TypeBinding bi;
										if (otherBounds == Binding.NO_SUPERINTERFACES) {
											bi = bi1;
										} else {
											int n = otherBounds.length+1;
											ReferenceBinding[] allBounds = new ReferenceBinding[n];
											allBounds[0] = (ReferenceBinding) bi1; // TODO is this safe?
											System.arraycopy(otherBounds, 0, allBounds, 1, n-1);
											bi = new IntersectionCastTypeBinding(allBounds, context.environment);
										}
										addTypeBoundsFromWildcardBound(context, theta, wildcardBinding.boundKind, t, r, bi);
//										if (otherBounds != null) {
//											for (int j = 0; j < otherBounds.length; j++) {
//												TypeBinding tj = otherBounds[j];
//												if (TypeBinding.notEquals(tj, t))
//													addTypeBoundsFromWildcardBound(context, wildcardBinding, tj, r, bij);
//											}
//										}
									}
								}
							}
							if (three.superBounds != null) {
								//  R <: α implies ⟨R <: T⟩  (super wildcard)
								//  R <: α implies false	 (else) 
								it = three.superBounds.iterator();
								while (it.hasNext()) {
									TypeBound bound = it.next();
									if (!(bound.right instanceof InferenceVariable)) {
										if (wildcardBinding.boundKind == Wildcard.SUPER)
											reduceOneConstraint(context, ConstraintTypeFormula.create(bound.right, t, ReductionResult.SUBTYPE));
										else
											return false;
									}
								}
							}
						}
					} else {
						addBound(new TypeBound(alpha, ai, ReductionResult.SAME), context.environment);
					}
				}
			}
			this.captures.clear();
		} while (hasUpdate);
		return true;
	}

	void addTypeBoundsFromWildcardBound(InferenceContext18 context, InferenceSubstitution theta, int boundKind, TypeBinding t,
			TypeBinding r, TypeBinding bi) throws InferenceFailureException {
		ConstraintFormula formula = null;
		if (boundKind == Wildcard.EXTENDS) {
			if (bi.id == TypeIds.T_JavaLangObject)
				formula = ConstraintTypeFormula.create(t, r, ReductionResult.SUBTYPE);
			if (t.id == TypeIds.T_JavaLangObject)
				formula = ConstraintTypeFormula.create(theta.substitute(theta, bi), r, ReductionResult.SUBTYPE);
		} else {
			formula = ConstraintTypeFormula.create(theta.substitute(theta, bi), r, ReductionResult.SUBTYPE);
		}
		if (formula != null)
			reduceOneConstraint(context, formula);
	}

	private ConstraintTypeFormula combineSameSame(TypeBound boundS, TypeBound boundT) {
		
		// α = S and α = T imply ⟨S = T⟩
		if (boundS.left == boundT.left) //$IDENTITY-COMPARISON$ InferenceVariable
			return ConstraintTypeFormula.create(boundS.right, boundT.right, ReductionResult.SAME, boundS.isSoft||boundT.isSoft);

		// match against more shapes:
		ConstraintTypeFormula newConstraint;
		newConstraint = combineSameSameWithProperType(boundS, boundT);
		if (newConstraint != null)
			return newConstraint;
		newConstraint = combineSameSameWithProperType(boundT, boundS);
		if (newConstraint != null)
			return newConstraint;
		return null;
	}

	// pre: boundLeft.left != boundRight.left
	private ConstraintTypeFormula combineSameSameWithProperType(TypeBound boundLeft, TypeBound boundRight) {
		//  α = U and S = T imply ⟨S[α:=U] = T[α:=U]⟩
		TypeBinding u = boundLeft.right;
		if (u.isProperType(true)) {
			InferenceVariable alpha = boundLeft.left;
			TypeBinding left = boundRight.left; // no substitution since S inference variable and (S != α) per precondition
			TypeBinding right = boundRight.right.substituteInferenceVariable(alpha, u);
			return ConstraintTypeFormula.create(left, right, ReductionResult.SAME, boundLeft.isSoft||boundRight.isSoft);
		}
		return null;
	}
	
	private ConstraintTypeFormula combineSameSubSuper(TypeBound boundS, TypeBound boundT) {
		//  α = S and α <: T imply ⟨S <: T⟩ 
		//  α = S and T <: α imply ⟨T <: S⟩
		InferenceVariable alpha = boundS.left;
		TypeBinding s = boundS.right;
		if (alpha == boundT.left) //$IDENTITY-COMPARISON$ InferenceVariable
			return ConstraintTypeFormula.create(s, boundT.right, boundT.relation, boundT.isSoft||boundS.isSoft);
		if (alpha == boundT.right) //$IDENTITY-COMPARISON$ InferenceVariable
			return ConstraintTypeFormula.create(boundT.right, s, boundT.relation, boundT.isSoft||boundS.isSoft);

		if (boundS.right instanceof InferenceVariable) {
			// reverse:
			alpha = (InferenceVariable) boundS.right;
			s = boundS.left;
			if (alpha == boundT.left) //$IDENTITY-COMPARISON$ InferenceVariable
				return ConstraintTypeFormula.create(s, boundT.right, boundT.relation, boundT.isSoft||boundS.isSoft);
			if (alpha == boundT.right) //$IDENTITY-COMPARISON$ InferenceVariable
				return ConstraintTypeFormula.create(boundT.right, s, boundT.relation, boundT.isSoft||boundS.isSoft);			
		}
		
		//  α = U and S <: T imply ⟨S[α:=U] <: T[α:=U]⟩ 
		TypeBinding u = boundS.right;
		if (u.isProperType(true)) {
			TypeBinding left = (alpha == boundT.left) ? u : boundT.left; //$IDENTITY-COMPARISON$ InferenceVariable
			TypeBinding right = boundT.right.substituteInferenceVariable(alpha, u);
			return ConstraintTypeFormula.create(left, right, boundT.relation, boundT.isSoft||boundS.isSoft);
		}
		return null;
	}

	private ConstraintTypeFormula combineSuperAndSub(TypeBound boundS, TypeBound boundT) {
		//  permutations of: S <: α and α <: T imply ⟨S <: T⟩
		InferenceVariable alpha = boundS.left;
		if (alpha == boundT.left) //$IDENTITY-COMPARISON$ InferenceVariable
			//  α >: S and α <: T imply ⟨S <: T⟩
			return ConstraintTypeFormula.create(boundS.right, boundT.right, ReductionResult.SUBTYPE, boundT.isSoft||boundS.isSoft);
		if (boundS.right instanceof InferenceVariable) {
			// try reverse:
			alpha = (InferenceVariable) boundS.right;
			if (alpha == boundT.right) //$IDENTITY-COMPARISON$ InferenceVariable
				// S :> α and T <: α  imply ⟨S :> T⟩
				return ConstraintTypeFormula.create(boundS.left, boundT.left, ReductionResult.SUPERTYPE, boundT.isSoft||boundS.isSoft);
		}
		return null;
	}
	
	private ConstraintTypeFormula combineEqualSupers(TypeBound boundS, TypeBound boundT) {
		//  more permutations of: S <: α and α <: T imply ⟨S <: T⟩
		if (boundS.left == boundT.right) //$IDENTITY-COMPARISON$ InferenceVariable
			// came in as: α REL S and T REL α imply ⟨T REL S⟩ 
			return ConstraintTypeFormula.create(boundT.left, boundS.right, boundS.relation, boundT.isSoft||boundS.isSoft);
		if (boundS.right == boundT.left) //$IDENTITY-COMPARISON$ InferenceVariable
			// came in as: S REL α and α REL T imply ⟨S REL T⟩ 
			return ConstraintTypeFormula.create(boundS.left, boundT.right, boundS.relation, boundT.isSoft||boundS.isSoft);
		return null;
	}


	private ConstraintTypeFormula[] deriveTypeArgumentConstraints(TypeBound boundS, TypeBound boundT) {
		/* From 18.4:
		 *  If two bounds have the form α <: S and α <: T, and if for some generic class or interface, G,
		 *  there exists a supertype (4.10) of S of the form G<S1, ..., Sn> and a supertype of T of the form G<T1, ..., Tn>,
		 *  then for all i, 1 ≤ i ≤ n, if Si and Ti are types (not wildcards), the constraint ⟨Si = Ti⟩ is implied. 
		 */
		if (boundS.relation != ReductionResult.SUBTYPE || boundT.relation != ReductionResult.SUBTYPE)
			return null;
		if (boundS.left != boundT.left) //$IDENTITY-COMPARISON$ InferenceVariable
			return null;
		TypeBinding[] supers = superTypesWithCommonGenericType(boundS.right, boundT.right);
		if (supers != null)
			return typeArgumentEqualityConstraints(supers[0], supers[1], boundS.isSoft || boundT.isSoft);
		return null;
	}

	private ConstraintTypeFormula[] typeArgumentEqualityConstraints(TypeBinding s, TypeBinding t, boolean isSoft) {
		if (s == null || s.kind() != Binding.PARAMETERIZED_TYPE || t == null || t.kind() != Binding.PARAMETERIZED_TYPE)
			return null;
		if (TypeBinding.equalsEquals(s, t)) // don't create useless constraints
			return null;
		TypeBinding[] sis = s.typeArguments();
		TypeBinding[] tis = t.typeArguments();
		if (sis == null || tis == null || sis.length != tis.length)
			return null;
		List<ConstraintTypeFormula> result = new ArrayList<ConstraintTypeFormula>(); 
		for (int i = 0; i < sis.length; i++) {
			TypeBinding si = sis[i];
			TypeBinding ti = tis[i];
			if (si.isWildcard() || ti.isWildcard() || TypeBinding.equalsEquals(si, ti))
				continue;
			result.add(ConstraintTypeFormula.create(si, ti, ReductionResult.SAME, isSoft));
		}
		if (result.size() > 0)
			return result.toArray(new ConstraintTypeFormula[result.size()]);
		return null;
	}

	/**
	 * Try to reduce the one given constraint.
	 * If a constraint produces further constraints reduce those recursively.
	 * @throws InferenceFailureException a compile error has been detected during inference
	 */
	public boolean reduceOneConstraint(InferenceContext18 context, ConstraintFormula currentConstraint) throws InferenceFailureException {
		Object result = currentConstraint.reduce(context);
		if (result == ReductionResult.FALSE)
			return false;
		if (result == ReductionResult.TRUE)
			return true;
		if (result == currentConstraint) {
			// not reduceable
			throw new IllegalStateException("Failed to reduce constraint formula"); //$NON-NLS-1$
		}
		if (result != null) {
			if (result instanceof ConstraintFormula) {
				if (!reduceOneConstraint(context, (ConstraintFormula) result))
					return false;
			} else if (result instanceof ConstraintFormula[]) {
				ConstraintFormula[] resultArray = (ConstraintFormula[]) result;
				for (int i = 0; i < resultArray.length; i++)
					if (!reduceOneConstraint(context, resultArray[i]))
						return false;
			} else {
				addBound((TypeBound)result, context.environment);
			}
		}
		return true; // no FALSE encountered
	}

	/**
	 * Helper for resolution (18.4):
	 * Does this bound set define a direct dependency between the two given inference variables? 
	 */
	public boolean dependsOnResolutionOf(InferenceVariable alpha, InferenceVariable beta) {
		Iterator<Map.Entry<ParameterizedTypeBinding, ParameterizedTypeBinding>> captureIter = this.captures.entrySet().iterator();
		boolean betaIsInCaptureLhs = false;
		while (captureIter.hasNext()) { // TODO: optimization: consider separate index structure (by IV)
			Entry<ParameterizedTypeBinding, ParameterizedTypeBinding> entry = captureIter.next();
			ParameterizedTypeBinding g = entry.getKey();
			for (int i = 0; i < g.arguments.length; i++) {
				if (g.arguments[i] == alpha) { //$IDENTITY-COMPARISON$ InferenceVariable
					// An inference variable α appearing on the left-hand side of a bound of the form G<..., α, ...> = capture(G<...>)
					// depends on the resolution of every other inference variable mentioned in this bound (on both sides of the = sign).
					ParameterizedTypeBinding captured = entry.getValue();
					if (captured.mentionsAny(new TypeBinding[]{beta}, -1/*don't care about index*/))
						return true;
					if (g.mentionsAny(new TypeBinding[]{beta}, i)) // exclude itself 
						return true;
				} else if (g.arguments[i] == beta) { //$IDENTITY-COMPARISON$ InferenceVariable
					betaIsInCaptureLhs = true;
				}
			}
		}
		if (betaIsInCaptureLhs) { // swap α and β in the rule text to cover "then β depends on the resolution of α"
			ThreeSets sets = this.boundsPerVariable.get(beta);
			if (sets != null && sets.hasDependency(alpha))
				return true;
		} else {
			ThreeSets sets = this.boundsPerVariable.get(alpha);
			if (sets != null && sets.hasDependency(beta))
				return true;
		}
		return false;
	}

	// helper for 18.4
	public boolean hasCaptureBound(Set<InferenceVariable> variableSet) {
		Iterator<ParameterizedTypeBinding> captureIter = this.captures.keySet().iterator();
		while (captureIter.hasNext()) {
			ParameterizedTypeBinding g = captureIter.next();
			for (int i = 0; i < g.arguments.length; i++)
				if (variableSet.contains(g.arguments[i]))
					return true;
		}
		return false;
	}

	// helper for 18.4
	public boolean hasOnlyTrivialExceptionBounds(InferenceVariable variable, TypeBinding[] upperBounds) {
		if (upperBounds != null) {
			for (int i = 0; i < upperBounds.length; i++) {
				switch (upperBounds[i].id) {
					case TypeIds.T_JavaLangException:
					case TypeIds.T_JavaLangThrowable:
					case TypeIds.T_JavaLangObject:
						continue;
				}
				return false;
			}
		}
		return true;
	}

	/**
	 * JLS 18.1.3:
	 * Answer all upper bounds for the given inference variable as defined by any bounds in this set. 
	 */
	public TypeBinding[] upperBounds(InferenceVariable variable, boolean onlyProper) {
		ThreeSets three = this.boundsPerVariable.get(variable);
		if (three == null || three.subBounds == null)
			return Binding.NO_TYPES;
		return three.upperBounds(onlyProper, variable);
		// TODO: if !onlyProper: should we also consider ThreeSets.inverseBounds,
		//        or is it safe to rely on incorporation to produce the required bounds?
	}
	
	/**
	 * JLS 18.1.3:
	 * Answer all lower bounds for the given inference variable as defined by any bounds in this set. 
	 */
	TypeBinding[] lowerBounds(InferenceVariable variable, boolean onlyProper) {
		ThreeSets three = this.boundsPerVariable.get(variable);
		if (three == null || three.superBounds == null)
			return Binding.NO_TYPES;
		return three.lowerBounds(onlyProper, variable);
		// bounds where 'variable' appears at the RHS are not relevant because
		// we're only interested in bounds with a proper type, but if 'variable'
		// appears as RHS the bound is by construction an inference variable,too.
	}

	// debugging:
	public String toString() {
		StringBuffer buf = new StringBuffer("Type Bounds:\n"); //$NON-NLS-1$
		TypeBound[] flattened = flatten();
		for (int i = 0; i < flattened.length; i++) {
			buf.append('\t').append(flattened[i].toString()).append('\n');
		}
		buf.append("Capture Bounds:\n"); //$NON-NLS-1$
		Iterator<Map.Entry<ParameterizedTypeBinding,ParameterizedTypeBinding>> captIter = this.captures.entrySet().iterator();
		while (captIter.hasNext()) {
			Entry<ParameterizedTypeBinding, ParameterizedTypeBinding> capt = captIter.next();
			String lhs = String.valueOf(((TypeBinding)capt.getKey()).shortReadableName());
			String rhs = String.valueOf(((TypeBinding)capt.getValue()).shortReadableName());
			buf.append('\t').append(lhs).append(" = capt(").append(rhs).append(")\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return buf.toString();
	}

	public TypeBinding findWrapperTypeBound(InferenceVariable variable) {
		ThreeSets three = this.boundsPerVariable.get(variable);
		if (three == null) return null;
		return three.findSingleWrapperType();
	}

	// this condition is just way too complex to check it in-line:
	public boolean condition18_5_2_bullet_3_3_1(InferenceVariable alpha, TypeBinding targetType) {
		// T is a reference type, but is not a wildcard-parameterized type, and either 
		// i) B2 contains a bound of one of the forms α = S or S <: α, where S is a wildcard-parameterized type, or ...
		if (targetType.isBaseType()) return false;
		if (InferenceContext18.parameterizedWithWildcard(targetType) != null) return false;
		ThreeSets ts = this.boundsPerVariable.get(alpha);
		if (ts.sameBounds != null) {
			Iterator<TypeBound> bounds = ts.sameBounds.iterator();
			while (bounds.hasNext()) {
				TypeBound bound = bounds.next();
				if (InferenceContext18.parameterizedWithWildcard(bound.right) != null)
					return true;
			}
		}
		if (ts.superBounds != null) {
			Iterator<TypeBound> bounds = ts.superBounds.iterator();
			while (bounds.hasNext()) {
				TypeBound bound = bounds.next();
				if (InferenceContext18.parameterizedWithWildcard(bound.right) != null)
					return true;
			}
		}
		// ii) B2 contains two bounds of the forms S1 <: α and S2 <: α, where
		//     S1 and S2 have supertypes (4.10) that are two different parameterizations of the same generic class or interface.
		if (ts.superBounds != null) {
			ArrayList<TypeBound> superBounds = new ArrayList<TypeBound>(ts.superBounds);
			int len = superBounds.size();
			for (int i=0; i<len; i++) {
				TypeBinding s1 = superBounds.get(i).right;
				for (int j=i+1; j<len; j++) {
					TypeBinding s2 = superBounds.get(j).right;
					TypeBinding[] supers = superTypesWithCommonGenericType(s1, s2);
					if (supers != null && !TypeBinding.equalsEquals(supers[0], supers[1]))
						return true;
				}
			}
		}
		return false;
	}

	public boolean condition18_5_2_bullet_3_3_2(InferenceVariable alpha, TypeBinding targetType, InferenceContext18 ctx18) {
		// T is a parameterization of a generic class or interface, G, and
		// B2 contains a bound of one of the forms α = S or S <: α,
		//   where there exists no type of the form G<...> that is a supertype of S, but the raw type G is a supertype of S.
		if (!targetType.isParameterizedType()) return false;
		TypeBinding g = targetType.original();
		ThreeSets ts = this.boundsPerVariable.get(alpha);
		Iterator<TypeBound> boundIterator;
		if (ts.sameBounds != null) {
			boundIterator = ts.sameBounds.iterator();
			while (boundIterator.hasNext()) {
				TypeBound b = boundIterator.next();
				if (superOnlyRaw(g, b.right, ctx18.environment))
					return true;
			}
		}
		if (ts.superBounds != null) {
			boundIterator = ts.superBounds.iterator();
			while (boundIterator.hasNext()) {
				TypeBound b = boundIterator.next();
				if (superOnlyRaw(g, b.right, ctx18.environment))
					return true;
			}
		}
		return false;
	}
	private boolean superOnlyRaw(TypeBinding g, TypeBinding s, LookupEnvironment env) {
		if (s instanceof InferenceVariable)
			return false; // inference has no super types
		if (s.findSuperTypeOriginatingFrom(g) == null)
			return s.isCompatibleWith(env.convertToRawType(g, false));
		return false;
	}
	
	protected TypeBinding[] superTypesWithCommonGenericType(TypeBinding s, TypeBinding t) {
		if (s == null || s.id == TypeIds.T_JavaLangObject || t == null || t.id == TypeIds.T_JavaLangObject)
			return null;
		if (TypeBinding.equalsEquals(s.original(), t.original())) {
			return new TypeBinding[] { s, t };
		}
		TypeBinding tSuper = t.findSuperTypeOriginatingFrom(s);
		if (tSuper != null) {
			return new TypeBinding[] {s, tSuper};
		}
		TypeBinding[] result = superTypesWithCommonGenericType(s.superclass(), t);
		if (result != null)
			return result;
		ReferenceBinding[] superInterfaces = s.superInterfaces();
		if (superInterfaces != null) {
			for (int i = 0; i < superInterfaces.length; i++) {
				result = superTypesWithCommonGenericType(superInterfaces[i], t);
				if (result != null)
					return result;
			}
		}
		return null;
	}
}
