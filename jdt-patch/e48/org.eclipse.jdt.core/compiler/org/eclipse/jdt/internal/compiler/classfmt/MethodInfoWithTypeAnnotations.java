/*******************************************************************************
 * Copyright (c) 2016 GoPivotal, Inc. All Rights Reserved.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *        Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *            Bug 407191 - [1.8] Binary access support for type annotations
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.classfmt;

import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;

class MethodInfoWithTypeAnnotations extends MethodInfoWithParameterAnnotations {
	private TypeAnnotationInfo[] typeAnnotations;

MethodInfoWithTypeAnnotations(MethodInfo methodInfo, AnnotationInfo[] annotations, AnnotationInfo[][] parameterAnnotations, TypeAnnotationInfo[] typeAnnotations) {
	super(methodInfo, annotations, parameterAnnotations);
	this.typeAnnotations = typeAnnotations;
}
@Override
public IBinaryTypeAnnotation[] getTypeAnnotations() {
	return this.typeAnnotations;
}

@Override
protected void initialize() {
	for (int i = 0, l = this.typeAnnotations == null ? 0 : this.typeAnnotations.length; i < l; i++) {
		this.typeAnnotations[i].initialize();
	}
	super.initialize();
}
@Override
protected void reset() {
	for (int i = 0, l = this.typeAnnotations == null ? 0 : this.typeAnnotations.length; i < l; i++) {
		this.typeAnnotations[i].reset();
	}
	super.reset();
}
}
