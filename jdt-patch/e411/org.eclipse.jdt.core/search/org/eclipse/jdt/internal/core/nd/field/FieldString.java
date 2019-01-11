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
package org.eclipse.jdt.internal.core.nd.field;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.db.ModificationLog;
import org.eclipse.jdt.internal.core.nd.db.ModificationLog.Tag;
import org.eclipse.jdt.internal.core.nd.db.Database;
import org.eclipse.jdt.internal.core.nd.db.EmptyString;
import org.eclipse.jdt.internal.core.nd.db.IString;

/**
 * Declares a Nd field of type string. Can be used in place of  {@link Field}&lt{@link String}&gt in order to
 * avoid extra GC overhead.
 */
public class FieldString extends BaseField implements IDestructableField {
	public static final int RECORD_SIZE = Database.STRING_SIZE;
	private static final char[] EMPTY_CHAR_ARRAY = new char[0];
	private final Tag putTag;
	private final Tag destructTag;

	public FieldString(String structName, int fieldNumber) {
		this.putTag = ModificationLog.createTag("Writing field " + fieldNumber + ", a " + getClass().getSimpleName() //$NON-NLS-1$//$NON-NLS-2$
				+ " in struct " + structName); //$NON-NLS-1$
		this.destructTag = ModificationLog
				.createTag("Destructing field " + fieldNumber + ", a " + getClass().getSimpleName() //$NON-NLS-1$//$NON-NLS-2$
						+ " in struct " + structName); //$NON-NLS-1$
	}

	public IString get(Nd nd, long address) {
		Database db = nd.getDB();
		long namerec = db.getRecPtr(address + this.offset);

		if (namerec == 0) {
			return EmptyString.create();
		}
		return db.getString(namerec);
	}

	public void put(Nd nd, long address, char[] newString) {
		Database db = nd.getDB();
		db.getLog().start(this.putTag);
		try {
			if (newString == null) {
				newString = EMPTY_CHAR_ARRAY;
			}
			IString name= get(nd, address);
			if (name.compare(newString, true) != 0) {
				name.delete();
				if (newString != null && newString.length > 0) {
					db.putRecPtr(address + this.offset, db.newString(newString).getRecord());
				} else {
					db.putRecPtr(address + this.offset, 0);
				}
			}
		} finally {
			db.getLog().end(this.putTag);
		}
	}

	public void put(Nd nd, long address, String newString) {
		put(nd, address, newString.toCharArray());
	}

	@Override
	public void destruct(Nd nd, long address) {
		Database db = nd.getDB();
		db.getLog().start(this.destructTag);
		try {
			get(nd, address).delete();
			nd.getDB().putRecPtr(address + this.offset, 0);
		} finally {
			db.getLog().end(this.destructTag);
		}
	}

	@Override
	public int getRecordSize() {
		return RECORD_SIZE;
	}
}
