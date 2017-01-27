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
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.db.Database;

/**
 * Represents a 1-to-0..1 relationship in a Nd database.
 */
public class FieldOneToOne<T extends NdNode> implements IField, IDestructableField, IRefCountedField {
	private int offset;
	public final Class<T> nodeType; 
	FieldOneToOne<?> backPointer;
	private boolean pointsToOwner;

	/**
	 * @param nodeType
	 * @param backPointer
	 */
	private FieldOneToOne(Class<T> nodeType, FieldOneToOne<?> backPointer, boolean pointsToOwner) {
		this.nodeType = nodeType;

		if (backPointer != null) {
			if (backPointer.backPointer != null && backPointer.backPointer != this) {
				throw new IllegalArgumentException(
					"Attempted to construct a FieldOneToOne referring to a backpointer list that is already in use" //$NON-NLS-1$
						+ " by another field"); //$NON-NLS-1$
			}
			backPointer.backPointer = this;
		}
		this.backPointer = backPointer;
		this.pointsToOwner = pointsToOwner;
	}

	public static <T extends NdNode, B extends NdNode> FieldOneToOne<T> create(StructDef<B> builder,
			Class<T> nodeType, FieldOneToOne<B> forwardPointer) {

		FieldOneToOne<T> result = new FieldOneToOne<T>(nodeType, forwardPointer, false);
		builder.add(result);
		builder.addDestructableField(result);
		return result;
	}

	public static <T extends NdNode, B extends NdNode> FieldOneToOne<T> createOwner(StructDef<B> builder,
			Class<T> nodeType, FieldOneToOne<B> forwardPointer) {

		FieldOneToOne<T> result = new FieldOneToOne<T>(nodeType, forwardPointer, true);
		builder.add(result);
		builder.addDestructableField(result);
		builder.addOwnerField(result);
		return result;
	}

	public T get(Nd nd, long address) {
		long ptr = nd.getDB().getRecPtr(address + this.offset);
		return NdNode.load(nd, ptr, this.nodeType);
	}

	public void put(Nd nd, long address, T target) {
		Database db = nd.getDB();
		cleanup(nd, address);
		if (target == null) {
			db.putRecPtr(address + this.offset, 0);
			if (this.pointsToOwner) {
				nd.scheduleDeletion(address);
			}
		} else {
			db.putRecPtr(address + this.offset, target.address);
			db.putRecPtr(target.address + this.backPointer.offset, address);
		}
	}

	@Override
	public void destruct(Nd nd, long address) {
		cleanup(nd, address);
	}

	private void cleanup(Nd nd, long address) {
		Database db = nd.getDB();
		long ptr = db.getRecPtr(address + this.offset);
		if (ptr != 0) {
			db.putRecPtr(ptr + this.backPointer.offset, 0);
			// If we own our target, delete it
			if (this.backPointer.pointsToOwner) {
				nd.scheduleDeletion(ptr);
			}
		}
	}

	@Override
	public void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public int getRecordSize() {
		return Database.PTR_SIZE;
	}

	@Override
	public boolean hasReferences(Nd nd, long address) {
		if (this.pointsToOwner) {
			long ptr = nd.getDB().getRecPtr(address + this.offset);
			return ptr != 0;
		}
		return false;
	}
}
