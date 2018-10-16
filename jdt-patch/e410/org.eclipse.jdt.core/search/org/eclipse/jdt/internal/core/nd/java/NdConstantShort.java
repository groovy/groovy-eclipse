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
import org.eclipse.jdt.internal.compiler.impl.ShortConstant;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.field.FieldShort;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

public final class NdConstantShort extends NdConstant {
	public static final FieldShort VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<NdConstantShort> type;

	static {
		type = StructDef.create(NdConstantShort.class, NdConstant.type);
		VALUE = type.addShort();
		type.done();
	}

	public NdConstantShort(Nd nd, long address) {
		super(nd, address);
	}

	protected NdConstantShort(Nd nd) {
		super(nd);
	}

	public static NdConstantShort create(Nd nd, short value) {
		NdConstantShort result = new NdConstantShort(nd);
		result.setValue(value);
		return result;
	}

	public void setValue(short value) {
		VALUE.put(getNd(), this.address, value);
	}

	public short getValue() {
		return VALUE.get(getNd(), this.address);
	}

	@Override
	public Constant getConstant() {
		return ShortConstant.fromValue(getValue());
	}
}
