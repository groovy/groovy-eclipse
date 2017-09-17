/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.java.model;

import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;

public interface ITypeAnnotationBuilder {
	ITypeAnnotationBuilder toField();
	ITypeAnnotationBuilder toThrows(int rank);
	ITypeAnnotationBuilder toTypeArgument(int rank);
	ITypeAnnotationBuilder toMethodParameter(short index);
	ITypeAnnotationBuilder toSupertype(short index);
	ITypeAnnotationBuilder toTypeParameterBounds(boolean isClassTypeParameter, int parameterRank);
	ITypeAnnotationBuilder toTypeBound(short boundIndex);
	ITypeAnnotationBuilder toTypeParameter(boolean isClassTypeParameter, int rank);
	ITypeAnnotationBuilder toMethodReturn();
	ITypeAnnotationBuilder toReceiver();
	ITypeAnnotationBuilder toWildcardBound();
	ITypeAnnotationBuilder toNextArrayDimension();
	ITypeAnnotationBuilder toNextNestedType();

	IBinaryTypeAnnotation build(IBinaryAnnotation annotation);
}
