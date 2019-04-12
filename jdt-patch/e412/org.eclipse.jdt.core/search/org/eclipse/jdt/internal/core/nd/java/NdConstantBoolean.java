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

import org.eclipse.jdt.internal.compiler.impl.BooleanConstant;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.field.FieldByte;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

public final class NdConstantBoolean extends NdConstant {
	public static final FieldByte VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<NdConstantBoolean> type;

	static {
		type = StructDef.create(NdConstantBoolean.class, NdConstant.type);
		VALUE = type.addByte();
		type.done();
	}

	public NdConstantBoolean(Nd nd, long address) {
		super(nd, address);
	}

	protected NdConstantBoolean(Nd nd) {
		super(nd);
	}

	public static NdConstantBoolean create(Nd nd, boolean value) {
		NdConstantBoolean result = new NdConstantBoolean(nd);
		result.setValue(value);
		return result;
	}

	public void setValue(boolean value) {
		VALUE.put(getNd(), this.address, value ? (byte)1 : (byte)0);
	}

	public boolean getValue() {
		return VALUE.get(getNd(), this.address) != 0;
	}

	@Override
	public Constant getConstant() {
		return BooleanConstant.fromValue(getValue());
	}
}
