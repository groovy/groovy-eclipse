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
package org.eclipse.jdt.internal.core.nd.field;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.core.nd.ITypeFactory;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.db.Database;
import org.eclipse.jdt.internal.core.nd.db.ModificationLog;
import org.eclipse.jdt.internal.core.nd.db.ModificationLog.Tag;
import org.eclipse.jdt.internal.core.nd.util.MathUtils;

/**
 * Stores a singly-linked list of blocks, each of which contains a variable number of embedded elements.
 * Each block contains a header containing the size of the block and pointer to the next block, followed
 * by the embedded elements themselves.
 */
public class FieldList<T> extends BaseField implements IDestructableField {
	/**
	 * Pointer to the first block.
	 */
	public final static FieldPointer FIRST_BLOCK;
	/**
	 * Pointer to the block where insertions are currently happening. This is only null if there are no allocated
	 * blocks. If there are any blocks containing elements, this points to the last block with a nonzero number of
	 * elements.
	 */
	public final static FieldPointer LAST_BLOCK_WITH_ELEMENTS;

	@SuppressWarnings("rawtypes")
	private static final StructDef<FieldList> type;
	private static final int LIST_HEADER_BYTES;
	private static final long MAX_BYTES_IN_A_CHUNK = Database.getBytesThatFitInChunks(1);

	private final StructDef<T> elementType;
	private final int elementsPerBlock;
	private final StructDef<?> ownerType;
	private final Tag allocateTag;
	private final Tag appendTag;
	private final Tag destructTag;

	static {
		type = StructDef.createAbstract(FieldList.class);
		FIRST_BLOCK = type.addPointer();
		LAST_BLOCK_WITH_ELEMENTS = type.addPointer();

		type.done();
		LIST_HEADER_BYTES = MathUtils.roundUpToNearestMultipleOfPowerOfTwo(type.size(), Database.BLOCK_SIZE_DELTA);
	}

	private static class BlockHeader {
		// This points to the next block if there is one, or null if not.
		public final static FieldPointer NEXT_BLOCK;
		public final static FieldShort BLOCK_SIZE;
		public final static FieldShort ELEMENTS_IN_USE;
		public static final int BLOCK_HEADER_BYTES;

		@SuppressWarnings("hiding")
		private static final StructDef<BlockHeader> type;

		static {
			type = StructDef.createAbstract(BlockHeader.class);

			NEXT_BLOCK = type.addPointer();
			BLOCK_SIZE = type.addShort();
			ELEMENTS_IN_USE = type.addShort();
			type.done();

			BLOCK_HEADER_BYTES = MathUtils.roundUpToNearestMultipleOfPowerOfTwo(type.size(), Database.BLOCK_SIZE_DELTA);
		}
	}

	private FieldList(StructDef<?> ownerType, StructDef<T> elementType,  int elementsPerBlock) {
		this.elementType = elementType;
		this.elementsPerBlock = elementsPerBlock;
		this.ownerType = ownerType;
		int fieldNumber = ownerType.getNumFields();
		setFieldName("field " + fieldNumber + ", a " + getClass().getSimpleName() //$NON-NLS-1$//$NON-NLS-2$
				+ " in struct " + ownerType.getStructName());//$NON-NLS-1$
		this.allocateTag = ModificationLog.createTag("Allocating elements for " + getFieldName()); //$NON-NLS-1$
		this.appendTag = ModificationLog.createTag("Appending to " + getFieldName()); //$NON-NLS-1$
		this.destructTag = ModificationLog.createTag("Deallocating " + getFieldName()); //$NON-NLS-1$
	}

	/**
	 * Creates a new {@link FieldList} in the given struct which contains elements of the given type. The resulting list
	 * will grow by 1 element each time it overflows.
	 * 
	 * @param ownerStruct
	 *            the struct to which the new list field will be added. Must not have had {@link StructDef#done()}
	 *            invoked on it yet.
	 * @param elementType
	 *            the type of elements that will be contained in the struct.
	 * @return a newly-constructed list field in the given struct.
	 */
	public static <T> FieldList<T> create(StructDef<?> ownerStruct, StructDef<T> elementType) {
		return create(ownerStruct, elementType, 1);
	}

	/**
	 * Creates a new {@link FieldList} in the given struct which contains elements of the given type. The resulting list
	 * will grow by the given number of elements each time it overflows.
	 * 
	 * @param ownerStruct
	 *            the struct to which the new list field will be added. Must not have had {@link StructDef#done()}
	 *            invoked on it yet.
	 * @param elementType
	 *            the type of elements that will be contained in the struct.
	 * @param elementsPerBlock
	 *            the number of elements that will be allocated each time the list overflows.
	 * @return a newly-constructed list field in the given struct.
	 */
	public static <T> FieldList<T> create(StructDef<?> ownerStruct, StructDef<T> elementType, int elementsPerBlock) {
		FieldList<T> result = new FieldList<>(ownerStruct, elementType, elementsPerBlock);
		ownerStruct.add(result);
		ownerStruct.addDestructableField(result);
		return result;
	}

	private int getElementSize() {
		int recordSize = this.elementType.getFactory().getRecordSize();
		return MathUtils.roundUpToNearestMultipleOfPowerOfTwo(recordSize, Database.BLOCK_SIZE_DELTA);
	}

	@Override
	public int getRecordSize() {
		return LIST_HEADER_BYTES;
	}

	/**
	 * Returns the contents of the receiver as a {@link List}.
	 * 
	 * @param nd the database to be queried.
	 * @param address the address of the parent struct
	 */
	public List<T> asList(Nd nd, long address) {
		long headerStartAddress = address + this.offset;
		long firstBlockAddress = FIRST_BLOCK.get(nd, headerStartAddress);

		List<T> result = new ArrayList<>();

		long nextBlockAddress = firstBlockAddress;
		while (nextBlockAddress != 0) {
			long currentBlockAddress = nextBlockAddress;
			nextBlockAddress = BlockHeader.NEXT_BLOCK.get(nd, currentBlockAddress);
			int elementsInBlock = BlockHeader.ELEMENTS_IN_USE.get(nd, currentBlockAddress);
			long firstElementInBlockAddress = currentBlockAddress + BlockHeader.BLOCK_HEADER_BYTES;

			readElements(result, nd, firstElementInBlockAddress, elementsInBlock);
		}

		return result;
	}

	private void readElements(List<T> result, Nd nd, long nextElementAddress, int count) {
		ITypeFactory<T> factory = this.elementType.getFactory();

		int size = getElementSize();
		for (; count > 0; count--) {
			result.add(factory.create(nd, nextElementAddress));
			nextElementAddress += size;
		}
	}

	public T append(Nd nd, long address) {
		Database db = nd.getDB();
		db.getLog().start(this.appendTag);
		try {
			long headerStartAddress = address + this.offset;
			long nextBlockAddress = LAST_BLOCK_WITH_ELEMENTS.get(nd, headerStartAddress);
	
			// Ensure that there's at least one block
			long insertionBlockAddress = nextBlockAddress;
			if (nextBlockAddress == 0) {
				long newBlockAddress = allocateNewBlock(nd, this.elementsPerBlock);
				LAST_BLOCK_WITH_ELEMENTS.put(nd, headerStartAddress, newBlockAddress);
				FIRST_BLOCK.put(nd, headerStartAddress, newBlockAddress);
				insertionBlockAddress = newBlockAddress;
			}
	
			// Check if there's any free space in this block
			int elementsInBlock = BlockHeader.ELEMENTS_IN_USE.get(nd, insertionBlockAddress);
			int blockSize = BlockHeader.BLOCK_SIZE.get(nd, insertionBlockAddress);
	
			if (elementsInBlock >= blockSize) {
				long nextBlock = BlockHeader.NEXT_BLOCK.get(nd, insertionBlockAddress);
				if (nextBlock == 0) {
					nextBlock = allocateNewBlock(nd, this.elementsPerBlock);
					BlockHeader.NEXT_BLOCK.put(nd, insertionBlockAddress, nextBlock);
				}
				LAST_BLOCK_WITH_ELEMENTS.put(nd, headerStartAddress, nextBlock);
				insertionBlockAddress = nextBlock;
				elementsInBlock = BlockHeader.ELEMENTS_IN_USE.get(nd, insertionBlockAddress); 
			}
	
			BlockHeader.ELEMENTS_IN_USE.put(nd, insertionBlockAddress, (short) (elementsInBlock + 1));
			int elementSize = getElementSize();
	
			long resultAddress = insertionBlockAddress + BlockHeader.BLOCK_HEADER_BYTES + elementsInBlock * elementSize;
			assert ((resultAddress - Database.BLOCK_HEADER_SIZE) & (Database.BLOCK_SIZE_DELTA - 1)) == 0;
			return this.elementType.getFactory().create(nd, resultAddress);
		} finally {
			db.getLog().end(this.appendTag);
		}
	}

	/**
	 * Ensures that the receiver will have space for the given number of elements without additional
	 * allocation. Callers should invoke this prior to a sequence of {@link FieldList#append(Nd, long)}
	 * calls if they know in advance how many elements will be appended. Will create the minimum number
	 * of extra blocks needed to the given number of additional elements.
	 */
	public void allocate(Nd nd, long address, int numElements) {
		Database db = nd.getDB();
		db.getLog().start(this.allocateTag);
		try {
			if (numElements == 0) {
				// Not an error, but there's nothing to do if the caller didn't actually ask for anything to be allocated.
				return;
			}
			long headerStartAddress = address + this.offset;
			long nextBlockAddress = LAST_BLOCK_WITH_ELEMENTS.get(nd, headerStartAddress);
	
			int maxBlockSizeThatFitsInAChunk = (int) ((MAX_BYTES_IN_A_CHUNK - BlockHeader.BLOCK_HEADER_BYTES)
					/ getElementSize());

			// Ensure that there's at least one block
			if (nextBlockAddress == 0) {
				int firstAllocation = Math.min(numElements, maxBlockSizeThatFitsInAChunk);
				nextBlockAddress = allocateNewBlock(nd, firstAllocation);
				LAST_BLOCK_WITH_ELEMENTS.put(nd, headerStartAddress, nextBlockAddress);
				FIRST_BLOCK.put(nd, headerStartAddress, nextBlockAddress);
			}
	
			// Check if there's any free space in this block
			int remainingToAllocate = numElements;
			while (true) {
				long currentBlockAddress = nextBlockAddress;
				nextBlockAddress = BlockHeader.NEXT_BLOCK.get(nd, currentBlockAddress);
				int elementsInUse = BlockHeader.ELEMENTS_IN_USE.get(nd, currentBlockAddress);
				int blockSize = BlockHeader.BLOCK_SIZE.get(nd, currentBlockAddress);
	
				remainingToAllocate -= (blockSize - elementsInUse);
				if (remainingToAllocate <= 0) {
					break;
				}
	
				if (nextBlockAddress == 0) {
					nextBlockAddress = allocateNewBlock(nd, Math.min(maxBlockSizeThatFitsInAChunk, numElements));
					BlockHeader.NEXT_BLOCK.put(nd, currentBlockAddress, nextBlockAddress);
				}
			}
		} finally {
			db.getLog().end(this.allocateTag);
		}
	}

	private long allocateNewBlock(Nd nd, int blockSize) {
		short poolId = getMemoryPoolId(nd);
		int elementSize = getElementSize();
		long bytesNeeded = BlockHeader.BLOCK_HEADER_BYTES + blockSize * elementSize;
		// If we're close enough to filling the chunk that we wouldn't be able to fit any more elements anyway, allocate
		// the entire chunk. Although it wastes a small amount of space, it ensures that the blocks can be more easily
		// reused rather than being fragmented. It also allows freed blocks to be merged via the large block allocator.
		if (MAX_BYTES_IN_A_CHUNK - bytesNeeded < elementSize) {
			bytesNeeded = MAX_BYTES_IN_A_CHUNK;
		}
		long result = nd.getDB().malloc(bytesNeeded, poolId);
		BlockHeader.BLOCK_SIZE.put(nd, result, (short) blockSize);
		return result;
	}

	private short getMemoryPoolId(Nd nd) {
		short poolId = Database.POOL_LINKED_LIST;
		if (this.ownerType != null) {
			Class<?> structClass = this.ownerType.getStructClass();
			if (nd.getTypeRegistry().isRegisteredClass(structClass)) {
				poolId = (short) (Database.POOL_FIRST_NODE_TYPE + nd.getNodeType(structClass));
			}
		}
		return poolId;
	}

	@Override
	public void destruct(Nd nd, long address) {
		Database db = nd.getDB();
		db.getLog().start(this.destructTag);
		try {
			short poolId = getMemoryPoolId(nd);
			long headerStartAddress = address + this.offset;
			long firstBlockAddress = FIRST_BLOCK.get(nd, headerStartAddress);
	
			long nextBlockAddress = firstBlockAddress;
			while (nextBlockAddress != 0) {
				long currentBlockAddress = nextBlockAddress;
				nextBlockAddress = BlockHeader.NEXT_BLOCK.get(nd, currentBlockAddress);
				int elementsInBlock = BlockHeader.ELEMENTS_IN_USE.get(nd, currentBlockAddress);
				destructElements(nd, currentBlockAddress + BlockHeader.BLOCK_HEADER_BYTES, elementsInBlock);
				db.free(currentBlockAddress, poolId);
			}
	
			db.clearRange(headerStartAddress, getRecordSize());
		} finally {
			db.getLog().end(this.destructTag);
		} 
	}

	private void destructElements(Nd nd, long nextElementAddress, int count) {
		ITypeFactory<T> factory = this.elementType.getFactory();

		int size = getElementSize();
		while (--count >= 0) {
			factory.destruct(nd, nextElementAddress);
			nextElementAddress += size;
		}
	}
}
