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
package org.eclipse.jdt.internal.core.nd.java;

import java.util.List;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdStruct;
import org.eclipse.jdt.internal.core.nd.field.FieldList;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

public class NdAnnotation extends NdStruct {
	public static final FieldManyToOne<NdTypeSignature> ANNOTATION_TYPE;
	public static final FieldList<NdAnnotationValuePair> ELEMENT_VALUE_PAIRS;

	@SuppressWarnings("hiding")
	public static final StructDef<NdAnnotation> type;

	static {
		type = StructDef.create(NdAnnotation.class, NdStruct.type);
		ANNOTATION_TYPE = FieldManyToOne.create(type, NdTypeSignature.ANNOTATIONS_OF_THIS_TYPE);
		ELEMENT_VALUE_PAIRS = FieldList.create(type, NdAnnotationValuePair.type);
		type.done();
	}

	public NdAnnotation(Nd nd, long address) {
		super(nd, address);
	}

	public NdTypeSignature getType() {
		return ANNOTATION_TYPE.get(getNd(), this.address);
	}

	public void setType(NdTypeSignature type) {
		ANNOTATION_TYPE.put(getNd(), this.address, type);
	}

	public List<NdAnnotationValuePair> getElementValuePairs() {
		return ELEMENT_VALUE_PAIRS.asList(getNd(), this.address);
	}

	public NdAnnotationValuePair createValuePair(char[] name) {
		NdAnnotationValuePair result = ELEMENT_VALUE_PAIRS.append(getNd(), getAddress());
		result.setName(name);
		return result;
	}

	public void allocateValuePairs(int length) {
		ELEMENT_VALUE_PAIRS.allocate(getNd(), getAddress(), length);
	}
}
