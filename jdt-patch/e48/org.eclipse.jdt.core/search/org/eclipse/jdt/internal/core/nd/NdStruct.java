/*******************************************************************************
 * Copyright (c) 2017 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd;

import org.eclipse.jdt.internal.core.nd.db.Database;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * Base class for standard implementations of {@link INdStruct}. Holds the address of the struct
 * and the pointer to the database.
 */
public class NdStruct implements INdStruct {
	public long address;
	protected final Nd nd;

	public static final StructDef<NdStruct> type;

	static {
		type = StructDef.createAbstract(NdStruct.class);
		type.done();
	}

	protected NdStruct(Nd nd, long address) {
		this.nd = nd;
		this.address = address;
	}

	@Override
	public long getAddress() {
		return this.address;
	}

	@Override
	public Nd getNd() {
		return this.nd;
	}

	protected final Database getDB() {
		return this.nd.getDB();
	}
}
