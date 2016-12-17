/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

public class NdTypeAnnotationInType extends NdTypeAnnotation {
	public static final FieldManyToOne<NdType> OWNER;

	@SuppressWarnings("hiding")
	public static final StructDef<NdTypeAnnotationInType> type;

	static {
		type = StructDef.create(NdTypeAnnotationInType.class, NdTypeAnnotation.type);
		OWNER = FieldManyToOne.createOwner(type, NdType.TYPE_ANNOTATIONS);
		type.done();
	}

	public NdTypeAnnotationInType(Nd nd, long address) {
		super(nd, address);
	}

	public NdTypeAnnotationInType(Nd nd, NdType type) {
		super(nd);

		OWNER.put(getNd(), this.address, type);
	}

}
