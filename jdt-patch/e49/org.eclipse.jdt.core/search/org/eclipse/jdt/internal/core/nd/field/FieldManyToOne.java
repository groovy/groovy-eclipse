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

import org.eclipse.jdt.internal.core.nd.INdStruct;
import org.eclipse.jdt.internal.core.nd.ITypeFactory;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.db.ModificationLog;
import org.eclipse.jdt.internal.core.nd.db.Database;
import org.eclipse.jdt.internal.core.nd.db.ModificationLog.Tag;

/**
 * Holds the n side of a n..1 relationship. Declares a Nd field which is a pointer of a NdNode of the specified
 * type. {@link FieldManyToOne} forms a one-to-many relationship with {@link FieldOneToMany}. Whenever a
 * {@link FieldManyToOne} points to an object, the inverse pointer is automatically inserted into the matching back
 * pointer list.
 */
public class FieldManyToOne<T extends INdStruct> extends BaseField implements IDestructableField, IRefCountedField {
	public final static FieldPointer TARGET;
	public final static FieldInt BACKPOINTER_INDEX;

	StructDef<T> targetType;
	final StructDef<? extends INdStruct> localType;
	FieldOneToMany<?> backPointer;
	@SuppressWarnings("rawtypes")
	private final static StructDef<FieldManyToOne> type;
	/**
	 * True iff the other end of this pointer should delete this object when its end of the pointer is cleared.
	 */
	public final boolean pointsToOwner;
	private final Tag putTag;
	private final Tag destructTag;
	private boolean permitsNull = true;

	static {
		type = StructDef.createAbstract(FieldManyToOne.class);
		TARGET = type.addPointer();
		BACKPOINTER_INDEX = type.addInt();
		type.done();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private FieldManyToOne(StructDef<? extends INdStruct> localType, FieldOneToMany<?> backPointer, boolean pointsToOwner) {
		this.localType = localType;
		this.pointsToOwner = pointsToOwner;

		if (backPointer != null) {
			if (backPointer.forwardPointer != null && backPointer.forwardPointer != this) {
				throw new IllegalArgumentException(
						"Attempted to construct a FieldNodePointer referring to a backpointer list that is already in use" //$NON-NLS-1$
								+ " by another field"); //$NON-NLS-1$
			}
			backPointer.targetType = (StructDef) localType;
			this.targetType = (StructDef) backPointer.localType;
			backPointer.forwardPointer = this;
		}
		this.backPointer = backPointer;
		setFieldName("field " + localType.getNumFields() + ", a " + getClass().getSimpleName() //$NON-NLS-1$//$NON-NLS-2$
				+ " in struct " + localType.getStructName()); //$NON-NLS-1$
		this.putTag = ModificationLog.createTag("Writing " + getFieldName()); //$NON-NLS-1$
		this.destructTag = ModificationLog.createTag("Destructing " + getFieldName()); //$NON-NLS-1$
	}

	public static <T extends INdStruct, B extends INdStruct> FieldManyToOne<T> createNonNull(StructDef<B> builder,
			FieldOneToMany<B> forwardPointer) {
		FieldManyToOne<T> result = create(builder, forwardPointer);
		result.permitsNull = false;
		return result;
	}

	public static <T extends INdStruct, B extends INdStruct> FieldManyToOne<T> create(StructDef<B> builder,
			FieldOneToMany<B> forwardPointer) {
		FieldManyToOne<T> result = new FieldManyToOne<T>(builder, forwardPointer, false);
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
	public static <T extends INdStruct, B extends INdStruct> FieldManyToOne<T> createOwner(StructDef<B> builder,
			FieldOneToMany<B> forwardPointer) {
		// Although it would work to have a non-NdNode owned in this manner, we currently have no legitimate use-cases
		// for this to occur. If this happens it is almost certainly an accidental copy-paste error where someone
		// intended to call create but called this method instead. If we ever discover a legitimate use-case for it,
		// this could be removed and things would probably still work.
		if (!NdNode.class.isAssignableFrom(builder.getStructClass())) {
			throw new IllegalArgumentException(FieldManyToOne.class.getSimpleName() + " can't be the owner of " //$NON-NLS-1$
					+ builder.getStructClass().getSimpleName() + " because the latter isn't a subclass of " //$NON-NLS-1$
					+ NdNode.class.getSimpleName()); 
		}

		FieldManyToOne<T> result = new FieldManyToOne<T>(builder, forwardPointer, true);
		builder.add(result);
		builder.addDestructableField(result);
		builder.addOwnerField(result);
		return result;
	}

	/**
	 * Sets whether or not this field permits nulls to be assigned.
	 * 
	 * @param permitted true iff the field permits nulls
	 * @return this
	 */
	public FieldManyToOne<T> permitNull(boolean permitted) {
		this.permitsNull = permitted;
		return this;
	}

	public T get(Nd nd, long address) {
		return NdNode.load(nd, getAddress(nd, address), this.targetType);
	}

	public long getAddress(Nd nd, long address) {
		long result = nd.getDB().getRecPtr(address + this.offset);
		if (!this.permitsNull && result == 0) {
			throw nd.describeProblem()
				.addProblemAddress(this, address)
				.build("Database contained a null in a non-null field"); //$NON-NLS-1$
		}
		return result;
	}

	/**
	 * Directs this pointer to the given target. Also removes this pointer from the old backpointer list (if any) and
	 * inserts it into the new backpointer list (if any)
	 */
	public void put(Nd nd, long address, T value) {
		if (value != null) {
			put(nd, address, value.getAddress());
		} else if (this.permitsNull) {
			put(nd, address, 0);
		} else {
			throw new IllegalArgumentException("Attempted to write a null into a non-null field"); //$NON-NLS-1$
		}
	}

	public void put(Nd nd, long address, long newTargetAddress) {
		Database db = nd.getDB();
		db.getLog().start(this.putTag);
		try {
			long fieldStart = address + this.offset;
			if (this.backPointer == null) {
				throw new IllegalStateException(
						getClass().getSimpleName() + " must be associated with a " + FieldOneToMany.class.getSimpleName()); //$NON-NLS-1$
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
		} finally {
			db.getLog().end(this.putTag);
		}
	}

	protected void detachFromOldTarget(Nd nd, long address, long oldTargetAddress) {
		long fieldStart = address + this.offset;
		if (oldTargetAddress != 0) {
			int oldIndex = BACKPOINTER_INDEX.get(nd, fieldStart);

			this.backPointer.remove(nd, oldTargetAddress, oldIndex);

			if (this.targetType.isNdNode()) {
				short targetTypeId = NdNode.NODE_TYPE.get(nd, oldTargetAddress);
				ITypeFactory<? extends NdNode> typeFactory = nd.getTypeFactory(targetTypeId);

				if (typeFactory.getDeletionSemantics() == StructDef.DeletionSemantics.REFCOUNTED 
						&& typeFactory.isReadyForDeletion(nd, oldTargetAddress)) {
					nd.scheduleDeletion(oldTargetAddress);
				}
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
		Database db = nd.getDB();
		db.getLog().start(this.destructTag);
		try {
			long fieldStart = address + this.offset;
			long oldTargetAddress = TARGET.get(nd, fieldStart);
			detachFromOldTarget(nd, address, oldTargetAddress);
			TARGET.put(nd, fieldStart, 0);
		} finally {
			db.getLog().end(this.destructTag);
		}
	}

	void clearedByBackPointer(Nd nd, long address) {
		long fieldStart = this.offset + address;
		FieldManyToOne.TARGET.put(nd, fieldStart, 0);
		FieldManyToOne.BACKPOINTER_INDEX.put(nd, fieldStart, 0);
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
