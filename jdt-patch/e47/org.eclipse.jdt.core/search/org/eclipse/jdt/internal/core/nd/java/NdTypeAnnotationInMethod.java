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

public class NdTypeAnnotationInMethod extends NdTypeAnnotation {
	public static final FieldManyToOne<NdMethod> OWNER;

	@SuppressWarnings("hiding")
	public static final StructDef<NdTypeAnnotationInMethod> type;

	static {
		type = StructDef.create(NdTypeAnnotationInMethod.class, NdTypeAnnotation.type);
		OWNER = FieldManyToOne.createOwner(type, NdMethod.TYPE_ANNOTATIONS);
		type.done();
	}

	public NdTypeAnnotationInMethod(Nd nd, long address) {
		super(nd, address);
	}

	public NdTypeAnnotationInMethod(Nd nd, NdMethod variable) {
		super(nd);

		OWNER.put(getNd(), this.address, variable);
	}

}
