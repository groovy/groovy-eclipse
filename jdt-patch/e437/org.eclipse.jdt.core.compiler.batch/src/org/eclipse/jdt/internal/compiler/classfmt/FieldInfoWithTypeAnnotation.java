/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *        Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *          Bug 407191 - [1.8] Binary access support for type annotations
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.classfmt;

import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;

public final class FieldInfoWithTypeAnnotation extends FieldInfoWithAnnotation {
	private final TypeAnnotationInfo[] typeAnnotations;

FieldInfoWithTypeAnnotation(FieldInfo info, AnnotationInfo[] annos, TypeAnnotationInfo[] typeAnnos) {
	super(info, annos);
	this.typeAnnotations = typeAnnos;
}
@Override
public IBinaryTypeAnnotation[] getTypeAnnotations() {
	return this.typeAnnotations;
}
@Override
protected void initialize() {
	for (TypeAnnotationInfo typeAnnotation : this.typeAnnotations)
		typeAnnotation.initialize();
	super.initialize();
}
@Override
protected void reset() {
	if (this.typeAnnotations != null)
		for (TypeAnnotationInfo typeAnnotation : this.typeAnnotations)
			typeAnnotation.reset();
	super.reset();
}
@Override
public String toString() {
	StringBuilder buffer = new StringBuilder(getClass().getName());
	if (this.typeAnnotations != null) {
		buffer.append('\n');
		buffer.append("type annotations:"); //$NON-NLS-1$
		for (TypeAnnotationInfo typeAnnotation : this.typeAnnotations) {
			buffer.append(typeAnnotation);
			buffer.append('\n');
		}
	}
	toStringContent(buffer);
	return buffer.toString();
}
}
