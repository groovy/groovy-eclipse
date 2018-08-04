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

import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.field.FieldString;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

public final class NdConstantString extends NdConstant {
	public static final FieldString VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<NdConstantString> type;

	static {
		type = StructDef.create(NdConstantString.class, NdConstant.type);
		VALUE = type.addString();
		type.done();
	}

	public NdConstantString(Nd nd, long address) {
		super(nd, address);
	}

	protected NdConstantString(Nd nd) {
		super(nd);
	}

	public static NdConstantString create(Nd nd, String value) {
		NdConstantString result = new NdConstantString(nd);
		result.setValue(value);
		return result;
	}

	public void setValue(String value) {
		VALUE.put(getNd(), this.address, value);
	}

	public IString getValue() {
		return VALUE.get(getNd(), this.address);
	}

	@Override
	public Constant getConstant() {
		return StringConstant.fromValue(getValue().getString());
	}
}
