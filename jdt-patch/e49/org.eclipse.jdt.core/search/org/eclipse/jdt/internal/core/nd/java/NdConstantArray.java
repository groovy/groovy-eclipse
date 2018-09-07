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

import java.util.List;

import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

public final class NdConstantArray extends NdConstant {
	public static final FieldOneToMany<NdConstant> ELEMENTS;

	@SuppressWarnings("hiding")
	public static StructDef<NdConstantArray> type;

	static {
		type = StructDef.create(NdConstantArray.class, NdConstant.type);
		ELEMENTS = FieldOneToMany.create(type, NdConstant.PARENT_ARRAY, 2);
		type.done();
	}

	public NdConstantArray(Nd nd, long address) {
		super(nd, address);
	}

	public NdConstantArray(Nd nd) {
		super(nd);
	}

	public List<NdConstant> getValue() {
		return ELEMENTS.asList(getNd(), this.address);
	}

	@Override
	public Constant getConstant() {
		return null;
	}
}
