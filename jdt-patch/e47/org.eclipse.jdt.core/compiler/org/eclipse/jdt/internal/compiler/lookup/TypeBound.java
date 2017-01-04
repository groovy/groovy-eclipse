/*******************************************************************************
 * Copyright (c) 2013, 2015 GK Software AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *     IBM Corporation - bug fixes
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;

/**
 * Implementation of 18.1.3 in JLS8
 */
public class TypeBound extends ReductionResult {
	
	InferenceVariable left;
	
	// this flag contributes to the workaround controlled by InferenceContext18.ARGUMENT_CONSTRAINTS_ARE_SOFT:
	boolean isSoft;

	// here we accumulate null tagBits from any types that have been related to this type bound during incorporation:
	long nullHints;
	
	static TypeBound createBoundOrDependency(InferenceSubstitution theta, TypeBinding type, InferenceVariable variable) {
        // Part of JLS8 sect 18.1.3:
		return new TypeBound(variable, theta.substitute(theta, type), SUBTYPE, true);
	}

	/** Create a true type bound or a dependency. */
	TypeBound(InferenceVariable inferenceVariable, TypeBinding typeBinding, int relation) {
		this(inferenceVariable, typeBinding, relation, false);
	}
	
	TypeBound(InferenceVariable inferenceVariable, TypeBinding typeBinding, int relation, boolean isSoft) {
		this.left = inferenceVariable;
		this.right = safeType(typeBinding);
		if (((inferenceVariable.tagBits | this.right.tagBits) & TagBits.AnnotationNullMASK) != 0) {
			if ((inferenceVariable.tagBits & TagBits.AnnotationNullMASK) == (this.right.tagBits & TagBits.AnnotationNullMASK)) {
				// strip off identical nullness on both sides:
				this.left = (InferenceVariable) inferenceVariable.withoutToplevelNullAnnotation();
				this.right = this.right.withoutToplevelNullAnnotation();
			} else {
				long mask = 0;
				// extract hint, e.g.: T#0 <: @NonNull Right  =>  T#0 hinted as @NonNull
				switch (relation) {
					case SAME: 		mask = TagBits.AnnotationNullMASK; break;
					case SUBTYPE: 	mask = TagBits.AnnotationNonNull; break;  // sub of @Nullable is irrelevant
					case SUPERTYPE: mask = TagBits.AnnotationNullable; break; // super of @NonNull is irrelevant
				}
				inferenceVariable.prototype().nullHints |= this.right.tagBits & mask;
			}
		}
		this.relation = relation;
		this.isSoft = isSoft;
	}
	
	private TypeBinding safeType(TypeBinding type) {
		if (type != null && type.isLocalType()) {
			MethodBinding enclosingMethod = ((LocalTypeBinding) type.original()).enclosingMethod;
			if (enclosingMethod != null && CharOperation.prefixEquals(TypeConstants.ANONYMOUS_METHOD, enclosingMethod.selector))
				return type.superclass(); // don't use local class inside lambda: lambda is copied, type will be re-created and thus is unmatchable
		}
		return type;
	}


	/** distinguish bounds from dependencies. */
	boolean isBound() {
		return this.right.isProperType(true);
	}
	
	public int hashCode() {
		return this.left.hashCode() + this.right.hashCode() + this.relation;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof TypeBound) {
			TypeBound other = (TypeBound) obj;
			return (this.relation == other.relation) && TypeBinding.equalsEquals(this.left, other.left) && TypeBinding.equalsEquals(this.right, other.right);
		}
		return false;
	}
	
	// debugging:
	public String toString() {
		boolean isBound = this.right.isProperType(true);
		StringBuffer buf = new StringBuffer();
		buf.append(isBound ? "TypeBound  " : "Dependency "); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append(this.left.sourceName);
		buf.append(relationToString(this.relation));
		buf.append(this.right.readableName());
		return buf.toString();
	}
}
