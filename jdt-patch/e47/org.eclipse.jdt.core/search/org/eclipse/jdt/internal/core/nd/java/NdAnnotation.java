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
package org.eclipse.jdt.internal.core.nd.java;

import java.util.List;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

public class NdAnnotation extends NdNode {
	public static final FieldManyToOne<NdTypeSignature> ANNOTATION_TYPE;
	public static final FieldOneToMany<NdAnnotationValuePair> ELEMENT_VALUE_PAIRS;

	@SuppressWarnings("hiding")
	public static final StructDef<NdAnnotation> type;

	static {
		type = StructDef.create(NdAnnotation.class, NdNode.type);
		ANNOTATION_TYPE = FieldManyToOne.create(type, NdTypeSignature.ANNOTATIONS_OF_THIS_TYPE);
		ELEMENT_VALUE_PAIRS = FieldOneToMany.create(type, NdAnnotationValuePair.APPLIES_TO);
		type.done();
	}

	public NdAnnotation(Nd nd, long address) {
		super(nd, address);
	}

	public NdAnnotation(Nd nd) {
		super(nd);
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
}
