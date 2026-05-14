/*******************************************************************************
 * Copyright (c) 2005, 2013 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *        Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *          Bug 407191 - [1.8] Binary access support for type annotations
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.classfmt;

public class FieldInfoWithAnnotation extends FieldInfo {
	private final AnnotationInfo[] annotations;

FieldInfoWithAnnotation(FieldInfo info, AnnotationInfo[] annos) {
	super(info.reference, info.constantPoolOffsets, info.structOffset, info.version);
	this.accessFlags = info.accessFlags;
	this.attributeBytes = info.attributeBytes;
	this.constant = info.constant;
	this.constantPoolOffsets = info.constantPoolOffsets;
	this.descriptor = info.descriptor;
	this.name = info.name;
	this.signature = info.signature;
	this.signatureUtf8Offset = info.signatureUtf8Offset;
	this.tagBits = info.tagBits;
	this.wrappedConstantValue = info.wrappedConstantValue;
	this.annotations = annos;
}
@Override
public org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation[] getAnnotations() {
	return this.annotations;
}
@Override
protected void initialize() {
	if (this.annotations != null)
		for (AnnotationInfo annotation : this.annotations)
			annotation.initialize();
	super.initialize();
}
@Override
protected void reset() {
	if (this.annotations != null)
		for (AnnotationInfo annotation : this.annotations)
			annotation.reset();
	super.reset();
}
@Override
public String toString() {
	StringBuilder buffer = new StringBuilder(getClass().getName());
	if (this.annotations != null) {
		buffer.append('\n');
		for (AnnotationInfo annotation : this.annotations) {
			buffer.append(annotation);
			buffer.append('\n');
		}
	}
	toStringContent(buffer);
	return buffer.toString();
}
}
