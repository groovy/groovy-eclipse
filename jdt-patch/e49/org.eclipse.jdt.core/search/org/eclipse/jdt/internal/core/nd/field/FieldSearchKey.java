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
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.db.BTree;
import org.eclipse.jdt.internal.core.nd.db.ModificationLog;
import org.eclipse.jdt.internal.core.nd.db.ModificationLog.Tag;
import org.eclipse.jdt.internal.core.nd.db.Database;
import org.eclipse.jdt.internal.core.nd.db.EmptyString;
import org.eclipse.jdt.internal.core.nd.db.IString;

/**
 * Represents a search key into a global search index.
 */
public class FieldSearchKey<T> extends BaseField implements IDestructableField {
	FieldSearchIndex<?> searchIndex;
	private final Tag destructTag;
	private final Tag putTag;

	private FieldSearchKey(FieldSearchIndex<?> searchIndex, String structName, int fieldNumber) {
		if (searchIndex != null) {
			if (searchIndex.searchKey != null && searchIndex.searchKey != this) {
				throw new IllegalArgumentException(
					"Attempted to construct a FieldSearchKey referring to a search index that is " //$NON-NLS-1$
					+ "already in use by a different key"); //$NON-NLS-1$
			}
			searchIndex.searchKey = this;
		}
		this.searchIndex = searchIndex;
		setFieldName("field " + fieldNumber + ", a " + getClass().getSimpleName() //$NON-NLS-1$//$NON-NLS-2$
				+ " in struct " + structName); //$NON-NLS-1$
		this.putTag = ModificationLog.createTag("Writing " + getFieldName()); //$NON-NLS-1$
		this.destructTag = ModificationLog.createTag("Destructing " + getFieldName()); //$NON-NLS-1$
	}

	/**
	 * Creates a search key attribute in the given struct which stores an entry in the given global search index
	 */
	public static <T, B extends NdNode> FieldSearchKey<T> create(StructDef<B> builder,
			FieldSearchIndex<B> searchIndex) {
		FieldSearchKey<T> result = new FieldSearchKey<T>(searchIndex, builder.getStructName(), builder.getNumFields());

		builder.add(result);
		builder.addDestructableField(result);

		return result;
	}

	public void put(Nd nd, long address, String newString) {
		put(nd, address, newString.toCharArray());
	}

	/**
	 * Sets the value of the key and inserts it into the index if it is not already present
	 */
	public void put(Nd nd, long address, char[] newString) {
		Database db = nd.getDB();
		db.getLog().start(this.putTag);
		try {
			cleanup(nd, address);

			BTree btree = this.searchIndex.get(nd, Database.DATA_AREA_OFFSET);
			db.putRecPtr(address + this.offset, db.newString(newString).getRecord());
			btree.insert(address);
		} finally {
			db.getLog().end(this.putTag);
		}
	}

	public IString get(Nd nd, long address) {
		Database db = nd.getDB();
		long namerec = db.getRecPtr(address + this.offset);

		if (namerec == 0) {
			return EmptyString.create();
		}
		return db.getString(namerec);
	}

	@Override
	public void destruct(Nd nd, long address) {
		Database db = nd.getDB();
		db.getLog().start(this.destructTag);
		try {
			cleanup(nd, address);
		} finally {
			db.getLog().end(this.destructTag);
		}
	}

	private void cleanup(Nd nd, long address) {
		boolean isInIndex = isInIndex(nd, address);

		if (isInIndex) {
			// Remove this entry from the search index
			this.searchIndex.get(nd, Database.DATA_AREA_OFFSET).delete(address);

			get(nd, address).delete();
			nd.getDB().putRecPtr(address + this.offset, 0);
		}
	}

	/**
	 * Clears this key and removes it from the search index
	 */
	public void removeFromIndex(Nd nd, long address) {
		cleanup(nd, address);
	}

	/**
	 * Returns true iff this key is currently in the index
	 */
	public boolean isInIndex(Nd nd, long address) {
		long fieldAddress = address + this.offset;
		Database db = nd.getDB();
		long namerec = db.getRecPtr(fieldAddress);

		boolean isInIndex = namerec != 0;
		return isInIndex;
	}

	@Override
	public int getRecordSize() {
		return FieldString.RECORD_SIZE;
	}
}
