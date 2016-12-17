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

public class NdAnnotationInMethod extends NdAnnotation {
	public static final FieldManyToOne<NdMethod> OWNER;

	@SuppressWarnings("hiding")
	public static final StructDef<NdAnnotationInMethod> type;

	static {
		type = StructDef.create(NdAnnotationInMethod.class, NdAnnotation.type);
		OWNER = FieldManyToOne.createOwner(type, NdMethod.ANNOTATIONS);
		type.done();
	}

	public NdAnnotationInMethod(Nd nd, long address) {
		super(nd, address);
	}

	public NdAnnotationInMethod(Nd nd, NdMethod owner) {
		super(nd);

		OWNER.put(getNd(), this.address, owner);
	}

}
