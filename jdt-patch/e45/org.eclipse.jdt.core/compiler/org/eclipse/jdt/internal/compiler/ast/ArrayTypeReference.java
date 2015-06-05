/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								Bug 429958 - [1.8][null] evaluate new DefaultLocation attribute of @NonNullByDefault
 *								Bug 435570 - [1.8][null] @NonNullByDefault illegally tries to affect "throws E"
 *								Bug 466713 - Null Annotations: NullPointerException using <int @Nullable []> as Type Param
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class ArrayTypeReference extends SingleTypeReference {
	public int dimensions;
	private Annotation[][] annotationsOnDimensions; // jsr308 style type annotations on dimensions.
	public int originalSourceEnd;
	public int extendedDimensions;

	/**
	 * ArrayTypeReference constructor comment.
	 * @param source char[]
	 * @param dimensions int
	 * @param pos int
	 */
	public ArrayTypeReference(char[] source, int dimensions, long pos) {

		super(source, pos);
		this.originalSourceEnd = this.sourceEnd;
		this.dimensions = dimensions ;
		this.annotationsOnDimensions = null;
	}

	public ArrayTypeReference(char[] source, int dimensions, Annotation[][] annotationsOnDimensions, long pos) {
		this(source, dimensions, pos);
		if (annotationsOnDimensions != null) {
			this.bits |= ASTNode.HasTypeAnnotations;
		}
		this.annotationsOnDimensions = annotationsOnDimensions;
	}

	public int dimensions() {

		return this.dimensions;
	}
	
	public int extraDimensions() {
		return this.extendedDimensions;
	}

	/**
	 @see org.eclipse.jdt.internal.compiler.ast.TypeReference#getAnnotationsOnDimensions(boolean)
	*/
	public Annotation[][] getAnnotationsOnDimensions(boolean useSourceOrder) {
		if (useSourceOrder || this.annotationsOnDimensions == null || this.annotationsOnDimensions.length == 0 || this.extendedDimensions == 0 || this.extendedDimensions == this.dimensions)
			return this.annotationsOnDimensions;
		Annotation [][] externalAnnotations = new Annotation[this.dimensions][];
		final int baseDimensions = this.dimensions - this.extendedDimensions;
		System.arraycopy(this.annotationsOnDimensions, baseDimensions, externalAnnotations, 0, this.extendedDimensions);
		System.arraycopy(this.annotationsOnDimensions, 0, externalAnnotations, this.extendedDimensions, baseDimensions);
		return externalAnnotations;
	}
	
	public void setAnnotationsOnDimensions(Annotation [][] annotationsOnDimensions) {
		this.annotationsOnDimensions = annotationsOnDimensions;
	}
	/**
	 * @return char[][]
	 */
	public char [][] getParameterizedTypeName(){
		int dim = this.dimensions;
		char[] dimChars = new char[dim*2];
		for (int i = 0; i < dim; i++) {
			int index = i*2;
			dimChars[index] = '[';
			dimChars[index+1] = ']';
		}
		return new char[][]{ CharOperation.concat(this.token, dimChars) };
	}
	protected TypeBinding getTypeBinding(Scope scope) {

		if (this.resolvedType != null) {
			return this.resolvedType;
		}
		if (this.dimensions > 255) {
			scope.problemReporter().tooManyDimensions(this);
		}
		TypeBinding leafComponentType = scope.getType(this.token);
		return scope.createArrayType(leafComponentType, this.dimensions);

	}

	public StringBuffer printExpression(int indent, StringBuffer output){

		super.printExpression(indent, output);
		if ((this.bits & IsVarArgs) != 0) {
			for (int i= 0 ; i < this.dimensions - 1; i++) {
				if (this.annotationsOnDimensions != null && this.annotationsOnDimensions[i] != null) {
					output.append(' ');
					printAnnotations(this.annotationsOnDimensions[i], output);
					output.append(' ');
				}
				output.append("[]"); //$NON-NLS-1$
			}
			if (this.annotationsOnDimensions != null && this.annotationsOnDimensions[this.dimensions - 1] != null) {
				output.append(' ');
				printAnnotations(this.annotationsOnDimensions[this.dimensions - 1], output);
				output.append(' ');
			}
			output.append("..."); //$NON-NLS-1$
		} else {
			for (int i= 0 ; i < this.dimensions; i++) {
				if (this.annotationsOnDimensions != null && this.annotationsOnDimensions[i] != null) {
					output.append(" "); //$NON-NLS-1$
					printAnnotations(this.annotationsOnDimensions[i], output);
					output.append(" "); //$NON-NLS-1$
				}
				output.append("[]"); //$NON-NLS-1$
			}
		}
		return output;
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.annotations != null) {
				Annotation [] typeAnnotations = this.annotations[0];
				for (int i = 0, length = typeAnnotations == null ? 0 : typeAnnotations.length; i < length; i++) {
					typeAnnotations[i].traverse(visitor, scope);
				}
			}
			if (this.annotationsOnDimensions != null) {
				for (int i = 0, max = this.annotationsOnDimensions.length; i < max; i++) {
					Annotation[] annotations2 = this.annotationsOnDimensions[i];
					if (annotations2 != null) {
						for (int j = 0, max2 = annotations2.length; j < max2; j++) {
							Annotation annotation = annotations2[j];
							annotation.traverse(visitor, scope);
						}
					}
				}
			}
		}
		visitor.endVisit(this, scope);
	}

	public void traverse(ASTVisitor visitor, ClassScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.annotations != null) {
				Annotation [] typeAnnotations = this.annotations[0];
				for (int i = 0, length = typeAnnotations == null ? 0 : typeAnnotations.length; i < length; i++) {
					typeAnnotations[i].traverse(visitor, scope);
				}
			}
			if (this.annotationsOnDimensions != null) {
				for (int i = 0, max = this.annotationsOnDimensions.length; i < max; i++) {
					Annotation[] annotations2 = this.annotationsOnDimensions[i];
					if (annotations2 != null) {
						for (int j = 0, max2 = annotations2.length; j < max2; j++) {
							Annotation annotation = annotations2[j];
							annotation.traverse(visitor, scope);
						}
					}
				}
			}
		}
		visitor.endVisit(this, scope);
	}

	protected TypeBinding internalResolveType(Scope scope, int location) {
		TypeBinding internalResolveType = super.internalResolveType(scope, location);
		return internalResolveType;
	}
	
	@Override
	public boolean hasNullTypeAnnotation(AnnotationPosition position) {
		switch (position) {
			case LEAF_TYPE:
				// ignore annotationsOnDimensions:
				return super.hasNullTypeAnnotation(position);
			case MAIN_TYPE:
				// outermost dimension only:
				if (this.annotationsOnDimensions != null && this.annotationsOnDimensions.length > 0) {
					Annotation[] innerAnnotations = this.annotationsOnDimensions[0];
					return containsNullAnnotation(innerAnnotations);
				}
				break;
			case ANY:
				if (super.hasNullTypeAnnotation(position))
					return true;
				if (this.resolvedType != null && !this.resolvedType.hasNullTypeAnnotations())
					return false; // shortcut
				if (this.annotationsOnDimensions != null) {
					for (int i = 0; i < this.annotationsOnDimensions.length; i++) {
						Annotation[] innerAnnotations = this.annotationsOnDimensions[i];
						if (containsNullAnnotation(innerAnnotations))
							return true;
					}
				}
		}
    	return false;
	}
}
