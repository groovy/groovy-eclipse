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
import org.eclipse.jdt.internal.compiler.impl.FloatConstant;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.field.FieldFloat;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

public final class NdConstantFloat extends NdConstant {
	public static final FieldFloat VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<NdConstantFloat> type;

	static {
		type = StructDef.create(NdConstantFloat.class, NdConstant.type);
		VALUE = type.addFloat();
		type.done();
	}

	public NdConstantFloat(Nd nd, long address) {
		super(nd, address);
	}

	protected NdConstantFloat(Nd nd) {
		super(nd);
	}

	public static NdConstantFloat create(Nd nd, float value) {
		NdConstantFloat result = new NdConstantFloat(nd);
		result.setValue(value);
		return result;
	}

	public void setValue(float value) {
		VALUE.put(getNd(), this.address, value);
	}

	public float getValue() {
		return VALUE.get(getNd(), this.address);
	}

	@Override
	public Constant getConstant() {
		return FloatConstant.fromValue(getValue());
	}
}
