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
package org.eclipse.jdt.internal.compiler.classfmt;

import org.eclipse.jdt.internal.compiler.codegen.AnnotationTargetTypeConstants;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;

/**
 * A TypeAnnotationWalker is initialized with all type annotations found at a given element.
 * It can be used to walk into the types at the given element and finally answer the
 * actual annotations at any node of the walk.
 * 
 * The walker is implemented as immutable objects. During the walk either new instances
 * are created, or the current instance is shared if no difference is encountered.
 */
public class TypeAnnotationWalker {

	public static final IBinaryAnnotation[] NO_ANNOTATIONS = new IBinaryAnnotation[0];

	/**
	 * A no-effect annotation walker, all walking methods are implemented as identity-functions.
	 * At the end of any walk an empty array of annotations is returned.
	 */
	public static final TypeAnnotationWalker EMPTY_ANNOTATION_WALKER = new TypeAnnotationWalker(new IBinaryTypeAnnotation[0], 0L) {
		public TypeAnnotationWalker toField() { return this; }
		public TypeAnnotationWalker toTarget(int targetType) { return this; }
		public TypeAnnotationWalker toThrows(int rank) { return this; }
		public TypeAnnotationWalker toTypeArgument(int rank) { return this; }
		public TypeAnnotationWalker toMethodParameter(short index) { return this; }
		public TypeAnnotationWalker toSupertype(short index) { return this; }
		public TypeAnnotationWalker toTypeParameterBounds(boolean isClassTypeParameter, int parameterRank) { return this; }
		public TypeAnnotationWalker toTypeBound(short boundIndex) { return this; }
		public TypeAnnotationWalker toTypeParameter(boolean isClassTypeParameter, int rank) { return this; }
		public TypeAnnotationWalker toNextDetail(int detailKind) { return this; }
		public IBinaryAnnotation[] getAnnotationsAtCursor(int currentTypeId) { return NO_ANNOTATIONS; }
	};
	
	final protected IBinaryTypeAnnotation[] typeAnnotations;	// the actual material we're managing here
	final protected long matches;								// bit mask of indices into typeAnnotations, 1 means active, 0 is filtered during the walk
	final protected int pathPtr;								// pointer into the typePath

	// precondition: not-empty typeAnnotations
	public TypeAnnotationWalker(IBinaryTypeAnnotation[] typeAnnotations) {
		this(typeAnnotations, -1L >>> (64-typeAnnotations.length)); // initialize so lowest length bits are 1
	}
	TypeAnnotationWalker(IBinaryTypeAnnotation[] typeAnnotations, long matchBits) {
		this(typeAnnotations, matchBits, 0);
	}
	protected TypeAnnotationWalker(IBinaryTypeAnnotation[] typeAnnotations, long matchBits, int pathPtr) {
		this.typeAnnotations = typeAnnotations;
		this.matches = matchBits;
		this.pathPtr = pathPtr;
	}

	protected TypeAnnotationWalker restrict(long newMatches, int newPathPtr) {
		if (this.matches == newMatches && this.pathPtr == newPathPtr) return this;
		if (newMatches == 0 || this.typeAnnotations == null || this.typeAnnotations.length == 0)
			return EMPTY_ANNOTATION_WALKER;
		return new TypeAnnotationWalker(this.typeAnnotations, newMatches, newPathPtr);
	}

	// ==== filter by top-level targetType: ====
	
	/** Walk to a field. */
	public TypeAnnotationWalker toField() {
		return toTarget(AnnotationTargetTypeConstants.FIELD);
	}

	/** Walk to the return type of a method. */
	public TypeAnnotationWalker toMethodReturn() {
		return toTarget(AnnotationTargetTypeConstants.METHOD_RETURN);
	}

	/**
	 * Walk to the receiver type of a method.
	 * Note: Type annotations on receiver are not currently used by the compiler.
	 */
	public TypeAnnotationWalker toReceiver() {
		return toTarget(AnnotationTargetTypeConstants.METHOD_RECEIVER);
	}

	/*
	 * Implementation for walking to methodReturn, receiver type or field.
	 */
	protected TypeAnnotationWalker toTarget(int targetType) {
		long newMatches = this.matches;
		if (newMatches == 0)
			return EMPTY_ANNOTATION_WALKER;
		int length = this.typeAnnotations.length;
		long mask = 1;
		for (int i = 0; i < length; i++, mask = mask << 1) {
			if (this.typeAnnotations[i].getTargetType() != targetType)
				newMatches &= ~mask;
		}
		return restrict(newMatches, 0);
	}

	/**
	 * Walk to the type parameter of the given rank.
	 * @param isClassTypeParameter whether we are looking for a class type parameter (else: method type type parameter)
	 * @param rank rank of the type parameter
	 */
	public TypeAnnotationWalker toTypeParameter(boolean isClassTypeParameter, int rank) {
		long newMatches = this.matches;
		if (newMatches == 0)
			return EMPTY_ANNOTATION_WALKER;
		int targetType = isClassTypeParameter ? AnnotationTargetTypeConstants.CLASS_TYPE_PARAMETER : AnnotationTargetTypeConstants.METHOD_TYPE_PARAMETER;
		int length = this.typeAnnotations.length;
		long mask = 1;
		for (int i = 0; i < length; i++, mask = mask << 1) {
			IBinaryTypeAnnotation candidate = this.typeAnnotations[i];
			if (candidate.getTargetType() != targetType || candidate.getTypeParameterIndex() != rank) {
				newMatches &= ~mask;
			}
		}
		return restrict(newMatches, 0);		
	}

	/**
	 * Walk to the bounds of a type parameter of either a class or a method (signaled by isClassTypeParameter).
	 * Clients must then call {@link #toTypeBound(short)} on the resulting walker.
	 * @param isClassTypeParameter whether we are looking at a class type parameter (else: method type type parameter)
	 * @param parameterRank rank of the type parameter.
	 */
	public TypeAnnotationWalker toTypeParameterBounds(boolean isClassTypeParameter, int parameterRank) {
		long newMatches = this.matches;
		if (newMatches == 0)
			return EMPTY_ANNOTATION_WALKER;
		int length = this.typeAnnotations.length;
		int targetType = isClassTypeParameter ?
				AnnotationTargetTypeConstants.CLASS_TYPE_PARAMETER_BOUND : AnnotationTargetTypeConstants.METHOD_TYPE_PARAMETER_BOUND;
		long mask = 1;
		for (int i = 0; i < length; i++, mask = mask << 1) {
			IBinaryTypeAnnotation candidate = this.typeAnnotations[i];
			if (candidate.getTargetType() != targetType || (short)candidate.getTypeParameterIndex() != parameterRank) {
				newMatches &= ~mask;
			}
		}
		return restrict(newMatches, 0);	
	}
	/**
	 * Detail of {@link #toTypeParameterBounds(boolean, int)}: walk to the bounds
	 * of the previously selected type parameter. 
	 * @param boundIndex
	 */
	public TypeAnnotationWalker toTypeBound(short boundIndex) {
		long newMatches = this.matches;
		if (newMatches == 0)
			return EMPTY_ANNOTATION_WALKER;
		int length = this.typeAnnotations.length;
		long mask = 1;
		for (int i = 0; i < length; i++, mask = mask << 1) {
			IBinaryTypeAnnotation candidate = this.typeAnnotations[i];
			if ((short)candidate.getBoundIndex() != boundIndex) {
				newMatches &= ~mask;
			}
		}
		return restrict(newMatches, 0);		
	}
	
	
	/** Walk to the specified supertype: -1 is superclass, else the superinterface at the given index. */
	public TypeAnnotationWalker toSupertype(short index) {
		long newMatches = this.matches;
		if (newMatches == 0)
			return EMPTY_ANNOTATION_WALKER;
		int length = this.typeAnnotations.length;
		long mask = 1;
		for (int i = 0; i < length; i++, mask = mask << 1) {
			IBinaryTypeAnnotation candidate = this.typeAnnotations[i];
			if (candidate.getTargetType() != AnnotationTargetTypeConstants.CLASS_EXTENDS || (short)candidate.getSupertypeIndex() != index) {
				newMatches &= ~mask;
			}
		}
		return restrict(newMatches, 0);		
	}

	/** Walk to the index'th visible formal method parameter (i.e., not counting synthetic args). */
	public TypeAnnotationWalker toMethodParameter(short index) {
		long newMatches = this.matches;
		if (newMatches == 0)
			return EMPTY_ANNOTATION_WALKER;
		int length = this.typeAnnotations.length;
		long mask = 1;
		for (int i = 0; i < length; i++, mask = mask << 1) {
			IBinaryTypeAnnotation candidate = this.typeAnnotations[i];
			if (candidate.getTargetType() != AnnotationTargetTypeConstants.METHOD_FORMAL_PARAMETER || (short)candidate.getMethodFormalParameterIndex() != index) {
				newMatches &= ~mask;
			}
		}
		return restrict(newMatches, 0);		
	}

	/**
	 * Walk to the throws type at the given index.
	 */
	public TypeAnnotationWalker toThrows(int index) {
		long newMatches = this.matches;
		if (newMatches == 0)
			return EMPTY_ANNOTATION_WALKER;
		int length = this.typeAnnotations.length;
		long mask = 1;
		for (int i = 0; i < length; i++, mask = mask << 1) {
			IBinaryTypeAnnotation candidate = this.typeAnnotations[i];
			if (candidate.getTargetType() != AnnotationTargetTypeConstants.THROWS || candidate.getThrowsTypeIndex() != index) {
				newMatches &= ~mask;
			}
		}
		return restrict(newMatches, 0);		
	}

	// ==== descending into details: ====

	/** Walk to the type argument of the given rank. */
	public TypeAnnotationWalker toTypeArgument(int rank) {
		// like toNextDetail() but also checking byte 2 against rank
		long newMatches = this.matches;
		if (newMatches == 0)
			return EMPTY_ANNOTATION_WALKER;
		int length = this.typeAnnotations.length;
		long mask = 1;
		for (int i = 0; i < length; i++, mask = mask << 1) {
			IBinaryTypeAnnotation candidate = this.typeAnnotations[i];
			int[] path = candidate.getTypePath();
			if (this.pathPtr >= path.length 
					|| path[this.pathPtr] != AnnotationTargetTypeConstants.TYPE_ARGUMENT
					|| path[this.pathPtr+1] != rank) {
				newMatches &= ~mask;
			}
		}
		return restrict(newMatches, this.pathPtr+2);		
	}

	/** Walk to the bound of a wildcard. */
	public TypeAnnotationWalker toWildcardBound() {
		long newMatches = this.matches;
		if (newMatches == 0)
			return EMPTY_ANNOTATION_WALKER;
		int length = this.typeAnnotations.length;
		long mask = 1;
		for (int i = 0; i < length; i++, mask = mask << 1) {
			IBinaryTypeAnnotation candidate = this.typeAnnotations[i];
			int[] path = candidate.getTypePath();
			if (this.pathPtr >= path.length 
					|| path[this.pathPtr] != AnnotationTargetTypeConstants.WILDCARD_BOUND) {
				newMatches &= ~mask;
			}
		}
		return restrict(newMatches, this.pathPtr+2);		
	}

	/**
	 * Descend down one level of array dimensions.
	 */
	public TypeAnnotationWalker toNextArrayDimension() {
		return toNextDetail(AnnotationTargetTypeConstants.NEXT_ARRAY_DIMENSION);
	}
	
	/**
	 * Descend down one level of type nesting.
	 */
	public TypeAnnotationWalker toNextNestedType() {
		return toNextDetail(AnnotationTargetTypeConstants.NEXT_NESTED_TYPE);
	}

	/*
	 * Implementation for walking along the type_path for array dimensions & nested types.
	 * FIXME(stephan): support wildcard bounds.
	 */
	protected TypeAnnotationWalker toNextDetail(int detailKind) {
		long newMatches = this.matches;
		if (newMatches == 0)
			return EMPTY_ANNOTATION_WALKER;
		int length = this.typeAnnotations.length;
		long mask = 1;
		for (int i = 0; i < length; i++, mask = mask << 1) {
			IBinaryTypeAnnotation candidate = this.typeAnnotations[i];
			int[] path = candidate.getTypePath();
			if (this.pathPtr >= path.length || path[this.pathPtr] != detailKind) {
				newMatches &= ~mask;
			}
		}
		return restrict(newMatches, this.pathPtr+2);
	}
	
	// ==== leaves: the actual annotations: ====
	
	/**
	 * Retrieve the type annotations at the current position
	 * reached by invocations of toXYZ() methods.
	 * @param currentTypeId the id of the type being annotated; 0 signals don't care / unknown;
	 * 		 -1 signals if annotating a wildcard or a use of a type variable.
	 */
	public IBinaryAnnotation[] getAnnotationsAtCursor(int currentTypeId) {
		int length = this.typeAnnotations.length;
		IBinaryAnnotation[] filtered = new IBinaryAnnotation[length];
		long ptr = 1;
		int count = 0;
		for (int i = 0; i < length; i++, ptr<<=1) {
			if ((this.matches & ptr) == 0)
				continue;
			IBinaryTypeAnnotation candidate = this.typeAnnotations[i];
			if (candidate.getTypePath().length > this.pathPtr)
				continue;
			filtered[count++] = candidate.getAnnotation();
		}
		if (count == 0)
			return NO_ANNOTATIONS;
		if (count < length)
			System.arraycopy(filtered, 0, filtered = new IBinaryAnnotation[count], 0, count);
		return filtered;
	}
}
