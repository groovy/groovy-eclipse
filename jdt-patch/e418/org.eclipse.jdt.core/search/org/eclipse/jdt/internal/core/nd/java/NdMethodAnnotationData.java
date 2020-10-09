/*******************************************************************************
 * Copyright (c) 2017 Google, Inc and others.
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
package org.eclipse.jdt.internal.core.nd.java;

import java.util.List;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.field.FieldList;
import org.eclipse.jdt.internal.core.nd.field.FieldLong;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToOne;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * Stores all annotation-related information for a single method. This is stored separately from the rest of the
 * {@link NdMethod} structure in order to save space in the common case where a method has no annotations.
 */
public class NdMethodAnnotationData extends NdNode {
	public static final FieldOneToOne<NdMethod> METHOD;
	public static final FieldLong TAG_BITS;
	public static final FieldList<NdAnnotation> ANNOTATIONS;
	public static final FieldList<NdTypeAnnotation> TYPE_ANNOTATIONS;

	@SuppressWarnings("hiding")
	public static final StructDef<NdMethodAnnotationData> type;

	static {
		type = StructDef.create(NdMethodAnnotationData.class, NdNode.type);
		METHOD = FieldOneToOne.createOwner(type, NdMethod.type, NdMethod.ANNOTATION_DATA);
		TAG_BITS = type.addLong();
		ANNOTATIONS = FieldList.create(type, NdAnnotation.type);
		TYPE_ANNOTATIONS = FieldList.create(type, NdTypeAnnotation.type);
		type.done();
	}

	public NdMethodAnnotationData(Nd nd, long address) {
		super(nd, address);
	}

	public NdMethodAnnotationData(NdMethod method) {
		super(method.getNd());

		METHOD.put(getNd(), this.address, method);
	}

	public void setTagBits(long bits) {
		TAG_BITS.put(getNd(), this.address, bits);
	}

	public long getTagBits() {
		return TAG_BITS.get(getNd(), this.address);
	}

	public List<NdTypeAnnotation> getTypeAnnotations() {
		return TYPE_ANNOTATIONS.asList(getNd(), this.address);
	}

	public List<NdAnnotation> getAnnotations() {
		return ANNOTATIONS.asList(getNd(), this.address);
	}

	public NdAnnotation createAnnotation() {
		return ANNOTATIONS.append(getNd(), getAddress());
	}

	public void allocateAnnotations(int length) {
		ANNOTATIONS.allocate(getNd(), getAddress(), length);
	}

	public NdTypeAnnotation createTypeAnnotation() {
		return TYPE_ANNOTATIONS.append(getNd(), getAddress());
	}

	public void allocateTypeAnnotations(int length) {
		TYPE_ANNOTATIONS.allocate(getNd(), getAddress(), length);
	}
}
