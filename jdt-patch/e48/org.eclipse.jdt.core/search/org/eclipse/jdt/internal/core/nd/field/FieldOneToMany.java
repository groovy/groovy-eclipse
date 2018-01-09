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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.core.nd.INdStruct;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.RawGrowableArray;

/**
 * Holds the 1 side of a 1..n relationship between two objects. FieldNodePointer and FieldBackPointer fields always go
 * together in pairs.
 */
public class FieldOneToMany<T extends INdStruct> extends BaseField implements IDestructableField, IRefCountedField {
	public StructDef<T> targetType;
	public final StructDef<? extends INdStruct> localType;
	private final RawGrowableArray backPointerArray;
	FieldManyToOne<?> forwardPointer;

	public interface Visitor<T> {
		public void visit(int index, T toVisit);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private FieldOneToMany(StructDef<? extends INdStruct> localType, FieldManyToOne<? extends INdStruct> forwardPointer,
			int inlineElements) {
		this.localType = localType;

		if (forwardPointer != null) {
			if (forwardPointer.backPointer != null && forwardPointer.backPointer != this) {
				throw new IllegalArgumentException(
					"Attempted to construct a FieldBackPointer referring to a forward pointer that is already in use" //$NON-NLS-1$
						+ " by another field"); //$NON-NLS-1$
			}
			forwardPointer.targetType = (StructDef) localType;
			this.targetType = (StructDef) forwardPointer.localType;
			forwardPointer.backPointer = this;
		}
		this.forwardPointer = forwardPointer;
		setFieldName("field " + localType.getNumFields() + ", a " + getClass().getSimpleName() //$NON-NLS-1$//$NON-NLS-2$
				+ " in struct " + localType.getStructName()); //$NON-NLS-1$
		this.backPointerArray = new RawGrowableArray(inlineElements);
	}

	/**
	 * Creates a {@link FieldOneToMany} using the given builder. It will hold the many side of a one-to-many
	 * relationship with nodeType. 
	 * 
	 * @param builder builder that is being used to construct the struct containing this field
	 * @param forwardPointer field of the model object which holds the one side of this one-to-many relationship
	 * @param inlineElementCount number of inline elements. If this is nonzero, space for this number elements is
	 * preallocated and reserved in the header. The first few elements inserted will be stored here. For relationships
	 * which will usually have more than a certain number of participants, using a small number of inline elements will
	 * offer a performance improvement. For relationships that will normally be empty, this should be 0.
	 * @return the newly constructed backpointer field
	 */
	public static <T extends INdStruct, B extends INdStruct> FieldOneToMany<T> create(StructDef<B> builder, 
			FieldManyToOne<B> forwardPointer, int inlineElementCount) {
		FieldOneToMany<T> result = new FieldOneToMany<T>(builder, forwardPointer, inlineElementCount);
		builder.add(result);
		builder.addDestructableField(result);
		builder.addRefCountedField(result);
		return result;
	}

	public static <T extends INdStruct, B extends INdStruct> FieldOneToMany<T> create(StructDef<B> builder, 
			FieldManyToOne<B> forwardPointer) {
		return create(builder, forwardPointer, 0);
	}

	public void accept(Nd nd, long address, Visitor<T> visitor) {
		int size = size(nd, address);

		for (int idx = 0; idx < size; idx++) {
			visitor.visit(idx, get(nd, address, idx));
		}
	}

	public List<T> asList(Nd nd, long address) {
		final List<T> result = new ArrayList<>(size(nd, address));

		accept(nd, address, new Visitor<T>() {
			@Override
			public void visit(int index, T toVisit) {
				result.add(toVisit);
			}
		});

		return result;
	}

	public boolean isEmpty(Nd nd, long address) {
		return this.backPointerArray.isEmpty(nd, address + this.offset);
	}
	
	public int size(Nd nd, long address) {
		return this.backPointerArray.size(nd, address + this.offset);
	}

	public T get(Nd nd, long address, int index) {
		long nextPointer = this.backPointerArray.get(nd, address + this.offset, index);

		return NdNode.load(nd, nextPointer, this.targetType);
	}

	public long getAddressOf(Nd nd, long address, int index) {
		return this.backPointerArray.get(nd, address + this.offset, index);
	}

	/**
	 * Removes the given index from the list. If another element is swapped into the removed element's
	 * location, that element's index will be updated. The removed element itself will not be modified. The
	 * caller is responsible for nulling out the pointer and updating its index if necessary.
	 * <p>
	 * Not intended to be called by clients. The normal way to remove something from a backpointer list is
	 * by calling {@link FieldManyToOne#put}, which performs the appropriate removals automatically.
	 */
	void remove(Nd nd, long address, int index) {
		long swappedElement = this.backPointerArray.remove(nd, address + this.offset, index);

		if (swappedElement != 0) {
			this.forwardPointer.adjustIndex(nd, swappedElement, index);
		}
	}

	/**
	 * Addss the given forward pointer to the list and returns the insertion index. This should not be invoked
	 * directly by clients. The normal way to insert into a backpointer list is to assign a forward pointer.
	 */
	int add(Nd nd, long address, long value) {
		return this.backPointerArray.add(nd, address + this.offset, value);
	}

	/**
	 * Returns the record size of the back pointer list 
	 */
	@Override
	public int getRecordSize() {
		return this.backPointerArray.getRecordSize();
	}

	public void ensureCapacity(Nd nd, long address, int capacity) {
		long arrayAddress = address + this.offset;
		this.backPointerArray.ensureCapacity(nd, arrayAddress, capacity);
	}

	@Override
	public void destruct(Nd nd, long address) {
		long arrayAddress = address + this.offset;
		int size = size(nd, address);

		boolean isOwner = this.forwardPointer.pointsToOwner;
		for (int idx = 0; idx < size; idx++) {
			long target = this.backPointerArray.get(nd, arrayAddress, idx);

			this.forwardPointer.clearedByBackPointer(nd, target);

			if (isOwner) {
				nd.scheduleDeletion(target);
			}
		}

		this.backPointerArray.destruct(nd, arrayAddress);
	}

	public int getCapacity(Nd nd, long address) {
		return this.backPointerArray.getCapacity(nd, address + this.offset);
	}

	@Override
	public boolean hasReferences(Nd nd, long address) {
		// If this field owns the objects it points to, don't treat the incoming pointers as ref counts
		if (this.forwardPointer.pointsToOwner) {
			return false;
		}
		return !isEmpty(nd, address);
	}
}
