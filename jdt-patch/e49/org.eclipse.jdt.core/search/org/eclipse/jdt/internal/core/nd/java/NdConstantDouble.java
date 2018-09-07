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
import org.eclipse.jdt.internal.compiler.impl.DoubleConstant;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.field.FieldDouble;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

public final class NdConstantDouble extends NdConstant {
	public static final FieldDouble VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<NdConstantDouble> type;

	static {
		type = StructDef.create(NdConstantDouble.class, NdConstant.type);
		VALUE = type.addDouble();
		type.done();
	}

	public NdConstantDouble(Nd nd, long address) {
		super(nd, address);
	}

	protected NdConstantDouble(Nd nd) {
		super(nd);
	}

	public static NdConstantDouble create(Nd nd, double value) {
		NdConstantDouble result = new NdConstantDouble(nd);
		result.setValue(value);
		return result;
	}

	public void setValue(double value) {
		VALUE.put(getNd(), this.address, value);
	}

	public double getValue() {
		return VALUE.get(getNd(), this.address);
	}

	@Override
	public Constant getConstant() {
		return DoubleConstant.fromValue(getValue());
	}
}
