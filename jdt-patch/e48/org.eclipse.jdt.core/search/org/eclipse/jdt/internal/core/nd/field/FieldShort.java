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
import org.eclipse.jdt.internal.core.nd.db.ModificationLog;
import org.eclipse.jdt.internal.core.nd.db.ModificationLog.Tag;
import org.eclipse.jdt.internal.core.nd.db.Database;

/**
 * Declares a Nd field of type short. Can be used in place of  {@link Field}&lt{@link Short}&gt in order to
 * avoid extra GC overhead.
 */
public class FieldShort extends BaseField {
	private final Tag putTag;

	public FieldShort(String structName, int fieldNumber) {
		setFieldName("field " + fieldNumber + ", a " + getClass().getSimpleName() //$NON-NLS-1$//$NON-NLS-2$
				+ " in struct " + structName); //$NON-NLS-1$
		this.putTag = ModificationLog.createTag("Writing " + getFieldName()); //$NON-NLS-1$
	}

	public short get(Nd nd, long address) {
		Database db = nd.getDB();
		return db.getShort(address + this.offset);
	}

	public void put(Nd nd, long address, short newValue) {
		Database db = nd.getDB();
		db.getLog().start(this.putTag);
		try {
			nd.getDB().putShort(address + this.offset, newValue);
		} finally {
			db.getLog().end(this.putTag);
		}
	}

	@Override
	public int getRecordSize() {
		return Database.SHORT_SIZE;
	}
}
