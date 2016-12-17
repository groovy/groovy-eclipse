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

import org.eclipse.jdt.internal.core.nd.ITypeFactory;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;

/**
 * Holds the n side of a n..1 relationship. Declares a Nd field which is a pointer of a NdNode of the specified
 * type. {@link FieldManyToOne} forms a one-to-many relationship with {@link FieldOneToMany}. Whenever a
 * {@link FieldManyToOne} points to an object, the inverse pointer is automatically inserted into the matching back
 * pointer list.
 */
public class FieldManyToOne<T extends NdNode> implements IDestructableField, IField, IRefCountedField {
	public final static FieldPointer TARGET;
	public final static FieldInt BACKPOINTER_INDEX;

	private int offset;
	Class<T> targetType;
	final Class<? extends NdNode> localType;
	FieldOneToMany<?> backPointer;
	@SuppressWarnings("rawtypes")
	private final static StructDef<FieldManyToOne> type;
	/**
	 * True iff the other end of this pointer should delete this object when its end of the pointer is cleared.
	 */
	public final boolean pointsToOwner;

	static {
		type = StructDef.createAbstract(FieldManyToOne.class);
		TARGET = type.addPointer();
		BACKPOINTER_INDEX = type.addInt();
		type.done();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private FieldManyToOne(Class<? extends NdNode> localType, FieldOneToMany<?> backPointer, boolean pointsToOwner) {
		this.localType = localType;
		this.pointsToOwner = pointsToOwner;

		if (backPointer != null) {
			if (backPointer.forwardPointer != null && backPointer.forwardPointer != this) {
				throw new IllegalArgumentException(
						"Attempted to construct a FieldNodePointer referring to a backpointer list that is already in use" //$NON-NLS-1$
								+ " by another field"); //$NON-NLS-1$
			}
			backPointer.targetType = (Class) localType;
			this.targetType = (Class) backPointer.localType;
			backPointer.forwardPointer = this;
		}
		this.backPointer = backPointer;
	}

	public static <T extends NdNode, B extends NdNode> FieldManyToOne<T> create(StructDef<B> builder,
			FieldOneToMany<B> forwardPointer) {
		FieldManyToOne<T> result = new FieldManyToOne<T>(builder.getStructClass(), forwardPointer, false);
		builder.add(result);
		builder.addDestructableField(result);
		return result;
	}

	/**
	 * Creates a many-to-one pointer which points to this object's owner. If the pointer is non-null when the owner is
	 * deleted, this object will be deleted too.
	 * 
	 * @param builder the struct to which the field will be added
	 * @param forwardPointer the field which holds the pointer in the other direction
	 * @return a newly constructed field
	 */
	public static <T extends NdNode, B extends NdNode> FieldManyToOne<T> createOwner(StructDef<B> builder,
			FieldOneToMany<B> forwardPointer) {

		FieldManyToOne<T> result = new FieldManyToOne<T>(builder.getStructClass(), forwardPointer, true);
		builder.add(result);
		builder.addDestructableField(result);
		builder.addOwnerField(result);
		return result;
	}

	public T get(Nd nd, long address) {
		return NdNode.load(nd, getAddress(nd, address), this.targetType);
	}

	public long getAddress(Nd nd, long address) {
		return nd.getDB().getRecPtr(address + this.offset);
	}

	/**
	 * Directs this pointer to the given target. Also removes this pointer from the old backpointer list (if any) and
	 * inserts it into the new backpointer list (if any)
	 */
	public void put(Nd nd, long address, T value) {
		if (value != null) {
			put(nd, address, value.address);
		} else {
			put(nd, address, 0);
		}
	}

	public void put(Nd nd, long address, long newTargetAddress) {
		long fieldStart = address + this.offset;
		if (this.backPointer == null) {
			throw new IllegalStateException("FieldNodePointer must be associated with a FieldBackPointer"); //$NON-NLS-1$
		}
		
		long oldTargetAddress = TARGET.get(nd, fieldStart);
		if (oldTargetAddress == newTargetAddress) {
			return;
		}

		detachFromOldTarget(nd, address, oldTargetAddress);

		TARGET.put(nd, fieldStart, newTargetAddress);
		if (newTargetAddress != 0) {
			// Note that newValue is the address of the backpointer list and record (the address of the struct
			// containing the forward pointer) is the value being inserted into the list.
			BACKPOINTER_INDEX.put(nd, fieldStart, this.backPointer.add(nd, newTargetAddress, address));
		} else {
			if (this.pointsToOwner) {
				nd.scheduleDeletion(address);
			}
		}
	}

	protected void detachFromOldTarget(Nd nd, long address, long oldTargetAddress) {
		long fieldStart = address + this.offset;
		if (oldTargetAddress != 0) {
			int oldIndex = BACKPOINTER_INDEX.get(nd, fieldStart);

			this.backPointer.remove(nd, oldTargetAddress, oldIndex);

			short targetTypeId = NdNode.NODE_TYPE.get(nd, oldTargetAddress);

			ITypeFactory<T> typeFactory = nd.getTypeFactory(targetTypeId);

			if (typeFactory.getDeletionSemantics() == StructDef.DeletionSemantics.REFCOUNTED 
					&& typeFactory.isReadyForDeletion(nd, oldTargetAddress)) {
				nd.scheduleDeletion(oldTargetAddress);
			}
		}
	}

	/**
	 * Called when the index of this forward pointer has moved in the backpointer list. Adjusts the index.
	 * <p>
	 * Not intended to be called by clients. This is invoked by {@link FieldOneToMany} whenever it reorders elements in
	 * the array.
	 */
	void adjustIndex(Nd nd, long address, int index) {
		BACKPOINTER_INDEX.put(nd, address + this.offset, index);
	}

	@Override
	public void destruct(Nd nd, long address) {
		long fieldStart = address + this.offset;
		long oldTargetAddress = TARGET.get(nd, fieldStart);
		detachFromOldTarget(nd, address, oldTargetAddress);
		TARGET.put(nd, fieldStart, 0);
	}

	void clearedByBackPointer(Nd nd, long address) {
		long fieldStart = this.offset + address;
		FieldManyToOne.TARGET.put(nd, fieldStart, 0);
		FieldManyToOne.BACKPOINTER_INDEX.put(nd, fieldStart, 0);
	}

	@Override
	public void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public int getRecordSize() {
		return type.size();
	}

	@Override
	public boolean hasReferences(Nd nd, long address) {
		long fieldStart = this.offset + address;
		long target = TARGET.get(nd, fieldStart);
		return target != 0;
	}
}
