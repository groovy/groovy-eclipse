/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.compiler.impl.CharConstant;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.field.FieldChar;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

public final class NdConstantChar extends NdConstant {
	public static final FieldChar VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<NdConstantChar> type;

	static {
		type = StructDef.create(NdConstantChar.class, NdConstant.type);
		VALUE = type.addChar();
		type.done();
	}

	public NdConstantChar(Nd nd, long address) {
		super(nd, address);
	}

	protected NdConstantChar(Nd nd) {
		super(nd);
	}

	public static NdConstantChar create(Nd nd, char value) {
		NdConstantChar result = new NdConstantChar(nd);
		result.setValue(value);
		return result;
	}

	public void setValue(char value) {
		VALUE.put(getNd(), this.address, value);
	}

	public char getValue() {
		return VALUE.get(getNd(), this.address);
	}

	@Override
	public Constant getConstant() {
		return CharConstant.fromValue(getValue());
	}
}
