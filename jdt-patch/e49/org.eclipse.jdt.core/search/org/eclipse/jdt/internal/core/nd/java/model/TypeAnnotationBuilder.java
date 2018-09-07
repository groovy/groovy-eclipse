/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.java.model;

import org.eclipse.jdt.internal.compiler.codegen.AnnotationTargetTypeConstants;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;

public class TypeAnnotationBuilder implements ITypeAnnotationBuilder {
	TypeAnnotationBuilder parent;
	int kind;
	int index;
	int length;
	int target;
	int targetParameter;
	int targetParameter2;

	private TypeAnnotationBuilder(TypeAnnotationBuilder parent, int kind, int index,
			int length, int nextTarget, int nextTargetParameter, int nextTargetParameter2) {
		super();
		this.parent = parent;
		this.kind = kind;
		this.index = index;
		this.length = length;
		this.target = nextTarget;
		this.targetParameter = nextTargetParameter;
		this.targetParameter2 = nextTargetParameter2;
	}

	public static TypeAnnotationBuilder create() {
		return new TypeAnnotationBuilder(null, 0, 0, 0, -1, -1, -1);
	}

	private TypeAnnotationBuilder walk(int nextKind, int nextIndex) {
		return new TypeAnnotationBuilder(this, nextKind, nextIndex, this.length+1, this.target, this.targetParameter, this.targetParameter2);
	}

	private TypeAnnotationBuilder toTarget(int newTarget) {
		return new TypeAnnotationBuilder(this.parent, this.kind, this.index, this.length, newTarget, this.targetParameter, this.targetParameter2);
	}

	private TypeAnnotationBuilder toTarget(int newTarget, int parameter) {
		return new TypeAnnotationBuilder(this.parent, this.kind, this.index, this.length, newTarget, parameter, this.targetParameter2);
	}

	private TypeAnnotationBuilder toTarget2(int parameter) {
		return new TypeAnnotationBuilder(this.parent, this.kind, this.index, this.length, this.target, this.targetParameter, parameter);
	}

	@Override
	public ITypeAnnotationBuilder toField() {
		return toTarget(AnnotationTargetTypeConstants.FIELD);
	}

	@Override
	public ITypeAnnotationBuilder toMethodReturn() {
		return toTarget(AnnotationTargetTypeConstants.METHOD_RETURN);
	}

	@Override
	public ITypeAnnotationBuilder toReceiver() {
		return toTarget(AnnotationTargetTypeConstants.METHOD_RECEIVER);
	}

	@Override
	public ITypeAnnotationBuilder toTypeParameter(boolean isClassTypeParameter, int rank) {
		int targetType = isClassTypeParameter ? AnnotationTargetTypeConstants.CLASS_TYPE_PARAMETER
				: AnnotationTargetTypeConstants.METHOD_TYPE_PARAMETER;
		return toTarget(targetType, rank);
	}

	@Override
	public ITypeAnnotationBuilder toTypeParameterBounds(boolean isClassTypeParameter, int parameterRank) {
		int targetType = isClassTypeParameter ?
				AnnotationTargetTypeConstants.CLASS_TYPE_PARAMETER_BOUND : AnnotationTargetTypeConstants.METHOD_TYPE_PARAMETER_BOUND;

		return toTarget(targetType, parameterRank);
	}

	@Override
	public ITypeAnnotationBuilder toTypeBound(short boundIndex) {
		return toTarget2(boundIndex);
	}

	@Override
	public ITypeAnnotationBuilder toSupertype(short superTypeIndex) {
		return toTarget(AnnotationTargetTypeConstants.CLASS_EXTENDS, superTypeIndex);
	}

	@Override
	public ITypeAnnotationBuilder toMethodParameter(short parameterIndex) {
		return toTarget(AnnotationTargetTypeConstants.METHOD_FORMAL_PARAMETER, parameterIndex);
	}

	@Override
	public ITypeAnnotationBuilder toThrows(int rank) {
		return toTarget(AnnotationTargetTypeConstants.THROWS, rank);
	}

	@Override
	public ITypeAnnotationBuilder toTypeArgument(int rank) {
		return walk(AnnotationTargetTypeConstants.TYPE_ARGUMENT, rank);
	}

	@Override
	public ITypeAnnotationBuilder toWildcardBound() {
		return walk(AnnotationTargetTypeConstants.WILDCARD_BOUND, 0);
	}

	@Override
	public ITypeAnnotationBuilder toNextArrayDimension() {
		return walk(AnnotationTargetTypeConstants.NEXT_ARRAY_DIMENSION, 0);
	}

	@Override
	public ITypeAnnotationBuilder toNextNestedType() {
		return walk(AnnotationTargetTypeConstants.NEXT_NESTED_TYPE, 0);
	}

	@Override
	public IBinaryTypeAnnotation build(IBinaryAnnotation annotation) {
		return new IndexBinaryTypeAnnotation(this.target, this.targetParameter, this.targetParameter2, getTypePath(), annotation);
	}

	private int[] getTypePath() {
		if (this.length == 0) {
			return IBinaryTypeAnnotation.NO_TYPE_PATH;
		}

		int[] result = new int[this.length * 2];

		TypeAnnotationBuilder next = this;
		while (next != null && next.length > 0) {
			int writeIdx = (next.length - 1) * 2;
			result[writeIdx] = next.kind;
			result[writeIdx + 1] = next.index;
			next = next.parent;
		}

		return result;
	}
}
