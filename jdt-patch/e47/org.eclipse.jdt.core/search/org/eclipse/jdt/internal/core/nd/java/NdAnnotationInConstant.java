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
import org.eclipse.jdt.internal.core.nd.field.FieldOneToOne;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

public class NdAnnotationInConstant extends NdAnnotation {
	public static final FieldOneToOne<NdConstantAnnotation> OWNER;

	@SuppressWarnings("hiding")
	public static final StructDef<NdAnnotationInConstant> type;

	static {
		type = StructDef.create(NdAnnotationInConstant.class, NdAnnotation.type);
		OWNER = FieldOneToOne.createOwner(type, NdConstantAnnotation.class, NdConstantAnnotation.VALUE);
		type.done();
	}

	public NdAnnotationInConstant(Nd nd, long address) {
		super(nd, address);
	}

	public NdAnnotationInConstant(Nd nd) {
		super(nd);
	}

}
