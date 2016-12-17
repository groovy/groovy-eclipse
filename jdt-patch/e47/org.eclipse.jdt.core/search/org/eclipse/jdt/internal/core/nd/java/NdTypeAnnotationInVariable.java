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

public class NdTypeAnnotationInVariable extends NdTypeAnnotation {
	public static final FieldManyToOne<NdVariable> OWNER;

	@SuppressWarnings("hiding")
	public static final StructDef<NdTypeAnnotationInVariable> type;

	static {
		type = StructDef.create(NdTypeAnnotationInVariable.class, NdTypeAnnotation.type);
		OWNER = FieldManyToOne.createOwner(type, NdVariable.TYPE_ANNOTATIONS);
		type.done();
	}

	public NdTypeAnnotationInVariable(Nd nd, long address) {
		super(nd, address);
	}

	public NdTypeAnnotationInVariable(Nd nd, NdVariable variable) {
		super(nd);

		OWNER.put(getNd(), this.address, variable);
	}

}
