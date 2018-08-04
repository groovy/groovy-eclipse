/*******************************************************************************
 * Copyright (c) 2016 Till Brychcy and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Till Brychcy - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.classfmt;

import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;

class AnnotationMethodInfoWithTypeAnnotations extends AnnotationMethodInfoWithAnnotations {
	private TypeAnnotationInfo[] typeAnnotations;

AnnotationMethodInfoWithTypeAnnotations(MethodInfo methodInfo, Object defaultValue, AnnotationInfo[] annotations, TypeAnnotationInfo[] typeAnnotations) {
	super(methodInfo, defaultValue, annotations);
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
