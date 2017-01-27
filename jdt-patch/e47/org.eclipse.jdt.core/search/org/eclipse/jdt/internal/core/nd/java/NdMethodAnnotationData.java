/*******************************************************************************
 * Copyright (c) 2017 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.java;

import java.util.List;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.field.FieldLong;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToOne;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * Stores all annotation-related information for a single method. This is stored separately from the rest of the
 * {@link NdMethod} structure in order to save space in the common case where a method has no annotations.
 */
public class NdMethodAnnotationData extends NdNode {
	public static final FieldOneToOne<NdMethod> METHOD;
	public static final FieldLong TAG_BITS;
	public static final FieldOneToMany<NdAnnotationInMethod> ANNOTATIONS;
	public static final FieldOneToMany<NdTypeAnnotationInMethod> TYPE_ANNOTATIONS;

	@SuppressWarnings("hiding")
	public static final StructDef<NdMethodAnnotationData> type;

	static {
		type = StructDef.create(NdMethodAnnotationData.class, NdNode.type);
		METHOD = FieldOneToOne.createOwner(type, NdMethod.class, NdMethod.ANNOTATION_DATA);
		TAG_BITS = type.addLong();
		ANNOTATIONS = FieldOneToMany.create(type, NdAnnotationInMethod.OWNER);
		TYPE_ANNOTATIONS = FieldOneToMany.create(type, NdTypeAnnotationInMethod.OWNER);
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

	public List<NdTypeAnnotationInMethod> getTypeAnnotations() {
		return TYPE_ANNOTATIONS.asList(getNd(), this.address);
	}

	public List<NdAnnotationInMethod> getAnnotations() {
		return ANNOTATIONS.asList(getNd(), this.address);
	}
}
