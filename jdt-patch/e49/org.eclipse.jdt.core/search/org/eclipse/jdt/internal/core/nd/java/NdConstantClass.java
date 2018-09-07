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

import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

public final class NdConstantClass extends NdConstant {
	public static final FieldManyToOne<NdTypeSignature> VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<NdConstantClass> type;

	static {
		type = StructDef.create(NdConstantClass.class, NdConstant.type);
		VALUE = FieldManyToOne.create(type, NdTypeSignature.USED_AS_CONSTANT);
		type.done();
	}

	public NdConstantClass(Nd nd, long address) {
		super(nd, address);
	}

	protected NdConstantClass(Nd nd) {
		super(nd);
	}

	public static NdConstantClass create(Nd nd, NdTypeSignature value) {
		NdConstantClass result = new NdConstantClass(nd);
		result.setValue(value);
		return result;
	}

	public void setValue(NdTypeSignature value) {
		VALUE.put(getNd(), this.address, value);
	}

	public NdTypeSignature getValue() {
		return VALUE.get(getNd(), this.address);
	}

	@Override
	public Constant getConstant() {
		return null;
	}
}
