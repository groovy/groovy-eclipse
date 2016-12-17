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
package org.eclipse.jdt.internal.core.nd.field;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.db.Database;

/**
 * Declares a Nd field of type long. Can be used in place of  {@link Field}&lt{@link Long}&gt in order to
 * avoid extra GC overhead.
 */
public class FieldLong implements IField {
	private int offset;

	public FieldLong() {
	}

	public long get(Nd nd, long address) {
		Database db = nd.getDB();
		return db.getLong(address + this.offset);
	}

	public void put(Nd nd, long address, long newValue) {
		nd.getDB().putLong(address + this.offset, newValue);
	}

	@Override
	public void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public int getRecordSize() {
		return Database.LONG_SIZE;
	}
}
