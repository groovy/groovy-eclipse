/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc. All Rights Reserved.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *        Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *          Bug 407191 - [1.8] Binary access support for type annotations
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.classfmt;

import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;

public final class FieldInfoWithTypeAnnotation extends FieldInfoWithAnnotation {
	private TypeAnnotationInfo[] typeAnnotations;

FieldInfoWithTypeAnnotation(FieldInfo info, AnnotationInfo[] annos, TypeAnnotationInfo[] typeAnnos) {
	super(info, annos);
	this.typeAnnotations = typeAnnos;
}
public IBinaryTypeAnnotation[] getTypeAnnotations() {
	return this.typeAnnotations;
}
protected void initialize() {
	for (int i = 0, max = this.typeAnnotations.length; i < max; i++)
		this.typeAnnotations[i].initialize();
	super.initialize();
}
protected void reset() {
	if (this.typeAnnotations != null)
		for (int i = 0, max = this.typeAnnotations.length; i < max; i++)
			this.typeAnnotations[i].reset();
	super.reset();
}
public String toString() {
	StringBuffer buffer = new StringBuffer(getClass().getName());
	if (this.typeAnnotations != null) {
		buffer.append('\n');
		buffer.append("type annotations:"); //$NON-NLS-1$
		for (int i = 0; i < this.typeAnnotations.length; i++) {
			buffer.append(this.typeAnnotations[i]);
			buffer.append('\n');
		}
	}
	toStringContent(buffer);
	return buffer.toString();
}
}
