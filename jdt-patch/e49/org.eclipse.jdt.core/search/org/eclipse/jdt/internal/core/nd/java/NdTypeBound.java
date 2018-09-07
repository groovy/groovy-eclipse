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

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdStruct;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.StructDef;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;

/**
 * Represents the bound on a generic parameter (a ClassBound or InterfaceBound in
 * the sense of the Java VM spec Java SE 8 Edition, section 4.7.9.1).
 */
public class NdTypeBound extends NdStruct {
	public static final FieldManyToOne<NdTypeSignature> TYPE;

	@SuppressWarnings("hiding")
	public static final StructDef<NdTypeBound> type;

	static {
		type = StructDef.create(NdTypeBound.class, NdStruct.type);
		TYPE = FieldManyToOne.create(type, NdTypeSignature.USED_AS_TYPE_BOUND);

		type.done();
	}

	public NdTypeBound(Nd nd, long address) {
		super(nd, address);
	}

	public void setType(NdTypeSignature signature) {
		TYPE.put(getNd(), this.address, signature);
	}

	public NdTypeSignature getType() {
		return TYPE.get(getNd(), this.address);
	}

	public void getSignature(CharArrayBuffer result) {
		result.append(':');
		getType().getSignature(result);
	}
}
