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
package org.eclipse.jdt.internal.core.nd;

import org.eclipse.jdt.internal.core.nd.db.Database;
import org.eclipse.jdt.internal.core.nd.db.IndexException;
import org.eclipse.jdt.internal.core.nd.field.FieldInt;
import org.eclipse.jdt.internal.core.nd.field.FieldPointer;
import org.eclipse.jdt.internal.core.nd.field.FieldShort;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * Implements a growable array of pointers that supports constant-time insertions and removals. Items are inserted at
 * the end of the array, and each insertion hands back a unique identifier that can be used to remove the item quickly
 * at a later time.
 * <p>
 * The memory format contains a header is as follows:
 * <p>
 * 
 * <pre>
 * Byte				Meaning
 * --------------------------------------------
 * 0..3				Pointer to the growable block. Null if the number of records <= inlineRecordCount
 * 4..7				Record [0]
 * 8..11			Record [1]
 * ...
 * k...k+4			Record [inlineRecordCount-1]
 * </pre>
 * 
 * As shown above, the first few records are stored inline with the array. inlineRecordCount is a tunable parameter
 * which may be 0. If there are fewer than inlineRecordCount records, there is no growable block and all records are
 * stored in the header. Storing the first few records in the header is intended as an optimization for very small
 * arrays in the case where small arrays are expected to be a common case. If there are fewer than inlineRecordCount
 * records stored in the array, the size of the array is not stored explicitly. It is computed on demand by searching
 * for the first null entry among the inline records.
 *
 * <p>
 * The memory format for a growable block is as follows:
 * <p>
 * 
 * <pre>
 * Byte				Meaning
 * --------------------------------------------
 * 0..3				Size of the array, including all inline records. This is also the index at which the next entry will
 *					be inserted
 * 4..7				Capacity of this growable block.
 * 8..11			Record [n]
 * 12..15			Record [n+1]
 * ...
 * k...k+4			Record [blockSize-1]
 * </pre>
 * 
 * <p>
 * The growable block itself begins with a 4-byte int holding the size of the array, followed by a 4-byte int holding
 * the capacity of the growable block. In the event that the array is larger than
 * {@link GrowableBlockHeader#MAX_GROWABLE_SIZE} enough to be using a metablock, there will be multiple growable blocks
 * in use. In this case, the size and capacity stored in the metablock is used for the array and the size and capacity
 * stored in each growable block will be filled in with 0s.
 * <p>
 * If capacity <= MAX_BLOCK_SIZE then this is a normal block containing a flat array of record pointers starting from
 * the element numbered inlineRecordCount. If capacity > MAX_BLOCK_SIZE then then it is a metablock which holds record
 * pointers to separate growable blocks, each of which holds exactly MAX_BLOCK_SIZE elements.
 * <p>
 * Every time an element is inserted in the array, the add method returns the element's index. Indices can be used to
 * remove elements in constant time, but will be reassigned by the RawGrowableArray during removes by swapping the
 * removed element with the last element in the array. If the owner of the array is keeping track of indices, it should
 * update the relevant indices on remove.
 * <p>
 * The array itself is tightly packed. When an element is removed, the last element in the array is swapped into its
 * location. Anyone keeping track of indices may rely on the fact that they are consecutive integers.
 * <p>
 * These arrays preserve insertion order until the first call to "remove". If element order matters, you should not
 * remove individual elements but should instead destroy and rebuild the entire array.
 * <p>
 * Element additions and removals run in constant amortized time.
 * <p>
 * There are a lot of ints and longs used in the implementation of this class. In order to help clarify their function,
 * they get the following suffixes:
 * <ul>
 * <li>index - holds an index into the array
 * <li>size - holds a count of the number of indices
 * <li>value - holds one of the pointer values inserted into the array
 * <li>address - holds a pointer into the database (refers to a full 8-byte long, not the compressed 4-byte version)
 * <li>bytes - holds the size (in bytes) of something in the database
 * <li>block - holds a block number (in the case where a metablock is in use, the growable blocks are identified by
 * block numbers).
 * <li>blockCount - holds a number of blocks
 * </ul>
 */
public final class RawGrowableArray {
	private static final FieldPointer GROWABLE_BLOCK_ADDRESS;
	private static final int ARRAY_HEADER_BYTES;

	private static final StructDef<RawGrowableArray> type; 

	static {
		type = StructDef.createAbstract(RawGrowableArray.class);
		GROWABLE_BLOCK_ADDRESS = type.addPointer();
		type.done();

		ARRAY_HEADER_BYTES = type.size();
	}

	private static class GrowableBlockHeader {
		public static final FieldInt ARRAY_SIZE;
		public static final FieldInt ALLOCATED_SIZE;
		public static final int GROWABLE_BLOCK_HEADER_BYTES;
		public static final int MAX_GROWABLE_SIZE;

		@SuppressWarnings("hiding")
		private static final StructDef<GrowableBlockHeader> type;

		static {
			type = StructDef.createAbstract(GrowableBlockHeader.class);

			ARRAY_SIZE = type.addInt();
			ALLOCATED_SIZE = type.addInt();
			type.done();

			GROWABLE_BLOCK_HEADER_BYTES = type.size();

			MAX_GROWABLE_SIZE = (Database.MAX_SINGLE_BLOCK_MALLOC_SIZE - GROWABLE_BLOCK_HEADER_BYTES)
					/ Database.PTR_SIZE;
		}
	}

	@SuppressWarnings("synthetic-access")
	private static final class MetaBlockHeader extends GrowableBlockHeader {
		/**
		 * Holds the number of pages used for the metablock. Note that the start of the metablock array needs to be
		 * 4-byte aligned. Since all malloc calls are always 2 bytes away from 4-byte alignment, we need to use at
		 * least one short in this struct. */ 
		public static final FieldShort METABLOCK_NUM_PAGES;
		public static final int META_BLOCK_HEADER_BYTES;

		@SuppressWarnings("hiding")
		private static final StructDef<MetaBlockHeader> type;

		static {
			type = StructDef.createAbstract(MetaBlockHeader.class, GrowableBlockHeader.type);

			METABLOCK_NUM_PAGES = type.addShort();
			type.done();

			META_BLOCK_HEADER_BYTES = type.size();
		}
	}
	
	private final int inlineSize;

	public RawGrowableArray(int inlineRecords) {
		this.inlineSize = inlineRecords;
	}

	public static int getMaxGrowableBlockSize() {
		return GrowableBlockHeader.MAX_GROWABLE_SIZE;
	}

	/**
	 * Returns the size of the array.
	 * 
	 * @param address address of the array
	 * @return the array size, in number elements
	 */
	public int size(Nd nd, long address) {
		Database db = nd.getDB();
		long growableBlockAddress = GROWABLE_BLOCK_ADDRESS.get(nd, address);

		if (growableBlockAddress == 0) {
			// If there is no growable block or metablock, then the size is determined by the position of the first
			// null pointer among the inline records.
			long inlineRecordStartAddress = address + ARRAY_HEADER_BYTES;
			for (int index = 0; index < this.inlineSize; index++) {
				long nextAddress = inlineRecordStartAddress + index * Database.PTR_SIZE;

				long nextValue = db.getRecPtr(nextAddress);
				if (nextValue == 0) {
					return index;
				}
			}
			return this.inlineSize;
		}
		return GrowableBlockHeader.ARRAY_SIZE.get(nd, growableBlockAddress);
	}

	/**
	 * Adds the given value to the array. Returns an index which can be later passed into remove in order to
	 * remove the element at a later time.
	 */
	public int add(Nd nd, long address, long value) {
		if (value == 0) {
			throw new IllegalArgumentException("Null pointers cannot be inserted into " + getClass().getName()); //$NON-NLS-1$
		}
		Database db = nd.getDB();

		int insertionIndex = size(nd, address);
		int newSize = insertionIndex + 1;

		ensureCapacity(nd, address, newSize);
		long recordAddress = getAddressOfRecord(nd, address, insertionIndex);
		db.putRecPtr(recordAddress, value);
		setSize(nd, address, newSize);
		return insertionIndex;
	}

	/**
	 * Returns the element at the given index (nonzero). The given index must be < size().
	 */
	public long get(Nd nd, long address, int index) {
		long recordAddress = getAddressOfRecord(nd, address, index);
		return nd.getDB().getRecPtr(recordAddress);
	}

	/**
	 * Ensures that the array contains at least enough space allocated to fit the given number of new elements.
	 */
	public void ensureCapacity(Nd nd, long address, int desiredSize) {
		int growableBlockNeededSize = desiredSize - this.inlineSize;
		long growableBlockAddress = GROWABLE_BLOCK_ADDRESS.get(nd, address);
		int growableBlockCurrentSize = growableBlockAddress == 0 ? 0
				: GrowableBlockHeader.ALLOCATED_SIZE.get(nd, growableBlockAddress);

		// The growable region is already large enough.
		if (growableBlockNeededSize <= growableBlockCurrentSize) {
			return;
		}

		Database db = nd.getDB();

		int neededBlockSize = getGrowableRegionSizeFor(desiredSize); 
		if (neededBlockSize > GrowableBlockHeader.MAX_GROWABLE_SIZE) {
			// We need a metablock.
			long metablockAddress = growableBlockAddress;

			// neededBlockSize should always be a multiple of the max block size when metablocks are in use
			assert neededBlockSize % GrowableBlockHeader.MAX_GROWABLE_SIZE == 0;
			// Create extra growable blocks if necessary.
			int requiredBlockCount = divideRoundingUp(neededBlockSize, GrowableBlockHeader.MAX_GROWABLE_SIZE);

			int neededMetablockPages = computeMetablockPagesForBlocks(requiredBlockCount);

			if (neededMetablockPages > Short.MAX_VALUE) {
				throw new IndexException("A metablock overflowed. Unable to allocate " + neededMetablockPages //$NON-NLS-1$
						+ " pages."); //$NON-NLS-1$
			}
			if (!(growableBlockCurrentSize > GrowableBlockHeader.MAX_GROWABLE_SIZE)) {
				// We weren't using a metablock previously
				int currentSize = size(nd, address);
				// Need to convert to using metablocks.
				long firstGrowableBlockAddress = resizeBlock(nd, address, GrowableBlockHeader.MAX_GROWABLE_SIZE);

				metablockAddress = db.malloc(Database.getBytesThatFitInChunks(neededMetablockPages),
						Database.POOL_GROWABLE_ARRAY);
				GrowableBlockHeader.ARRAY_SIZE.put(nd, metablockAddress, currentSize);
				GrowableBlockHeader.ALLOCATED_SIZE.put(nd, metablockAddress,
						GrowableBlockHeader.MAX_GROWABLE_SIZE);
				MetaBlockHeader.METABLOCK_NUM_PAGES.put(nd, metablockAddress, (short)neededMetablockPages);

				// Link the first block into the metablock.
				db.putRecPtr(metablockAddress + MetaBlockHeader.META_BLOCK_HEADER_BYTES,
						firstGrowableBlockAddress);
				GROWABLE_BLOCK_ADDRESS.put(nd, address, metablockAddress);
			}

			short metablockCurrentPages = MetaBlockHeader.METABLOCK_NUM_PAGES.get(nd, metablockAddress);
			if (metablockCurrentPages < neededMetablockPages) {
				short newMetablockPages = (short)Math.min(Short.MAX_VALUE, neededMetablockPages * 1.5);
				long newMetablockAddress = db.malloc(Database.getBytesThatFitInChunks(newMetablockPages),
						Database.POOL_GROWABLE_ARRAY);
				int oldNumPages = MetaBlockHeader.METABLOCK_NUM_PAGES.get(nd, metablockAddress);
				db.memcpy(newMetablockAddress, metablockAddress, (int)Database.getBytesThatFitInChunks(oldNumPages));
				db.free(metablockAddress, Database.POOL_GROWABLE_ARRAY);
				metablockAddress = newMetablockAddress;
				MetaBlockHeader.METABLOCK_NUM_PAGES.put(nd, metablockAddress, newMetablockPages);
				GROWABLE_BLOCK_ADDRESS.put(nd, address, metablockAddress);
			}
			int currentAllocatedSize = GrowableBlockHeader.ALLOCATED_SIZE.get(nd, metablockAddress);
			assert currentAllocatedSize % GrowableBlockHeader.MAX_GROWABLE_SIZE == 0;
			int currentBlockCount = currentAllocatedSize / GrowableBlockHeader.MAX_GROWABLE_SIZE;

			for (int nextBlock = currentBlockCount; nextBlock < requiredBlockCount; nextBlock++) {
				long nextBlockAddress = db.malloc(computeBlockBytes(GrowableBlockHeader.MAX_GROWABLE_SIZE),
						Database.POOL_GROWABLE_ARRAY);

				db.putRecPtr(metablockAddress + MetaBlockHeader.META_BLOCK_HEADER_BYTES
						+ nextBlock * Database.PTR_SIZE, nextBlockAddress);
			}

			GrowableBlockHeader.ALLOCATED_SIZE.put(nd, metablockAddress, neededBlockSize);
		} else {
			long newBlockAddress = resizeBlock(nd, address, neededBlockSize);

			GROWABLE_BLOCK_ADDRESS.put(nd, address, newBlockAddress);
		}
	}

	private static int divideRoundingUp(int neededBlockSize, int maxGrowableSize) {
		return (neededBlockSize + maxGrowableSize - 1) / maxGrowableSize;
	}

	private int computeMetablockPagesForBlocks(int requiredBlockCount) {
		return Database.getChunksNeededForBytes(
				requiredBlockCount * Database.PTR_SIZE + GrowableBlockHeader.GROWABLE_BLOCK_HEADER_BYTES);
	}

	/**
	 * Allocates a new normal block, copies the contents of the old block to it, and deletes the old block. Should not
	 * be used if the array is using metablocks. Returns the address of the newly-allocated block.
	 */
	private long resizeBlock(Nd nd, long address, int newBlockSize) {
		Database db = nd.getDB();
		long oldGrowableBlockAddress = GROWABLE_BLOCK_ADDRESS.get(nd, address);

		// Check if the existing block is already exactly the right size
		if (oldGrowableBlockAddress != 0) {
			if (newBlockSize == 0) {
				db.free(oldGrowableBlockAddress, Database.POOL_GROWABLE_ARRAY);
				return 0;
			}

			int oldAllocatedSize = GrowableBlockHeader.ALLOCATED_SIZE.get(nd, oldGrowableBlockAddress);
			if (oldAllocatedSize == newBlockSize) {
				return oldGrowableBlockAddress;
			}
		}

		int arraySize = size(nd, address);
		int numToCopySize = Math.min(Math.max(0, arraySize - this.inlineSize), newBlockSize);
		long newGrowableBlockAddress = db.malloc(computeBlockBytes(newBlockSize), Database.POOL_GROWABLE_ARRAY);

		if (oldGrowableBlockAddress != 0) {
			db.memcpy(newGrowableBlockAddress, oldGrowableBlockAddress, computeBlockBytes(numToCopySize));
			db.free(oldGrowableBlockAddress, Database.POOL_GROWABLE_ARRAY);
		}

		GrowableBlockHeader.ARRAY_SIZE.put(nd, newGrowableBlockAddress, arraySize);
		GrowableBlockHeader.ALLOCATED_SIZE.put(nd, newGrowableBlockAddress, newBlockSize);
		return newGrowableBlockAddress;
	}

	private int computeBlockBytes(int size) {
		return size * Database.PTR_SIZE + GrowableBlockHeader.GROWABLE_BLOCK_HEADER_BYTES;
	}

	/**
	 * @param size
	 */
	private void setSize(Nd nd, long address, int size) {
		long growableBlockAddress = GROWABLE_BLOCK_ADDRESS.get(nd, address);

		// If we're not using a growable block, we don't explicitly store the size
		if (growableBlockAddress == 0) {
			return;
		}

		GrowableBlockHeader.ARRAY_SIZE.put(nd, growableBlockAddress, size);
	}

	/**
	 * Returns a record address given a record number
	 */
	private long getAddressOfRecord(Nd nd, long address, int index) {
		int growableBlockRelativeIndex = index - this.inlineSize;

		if (growableBlockRelativeIndex >= 0) {
			Database db = nd.getDB();
			// This record is located within the growable region
			long growableBlockAddress = GROWABLE_BLOCK_ADDRESS.get(nd, address);
			int size = size(nd, address);

			// We use reads of 1 past the end of the array to handle insertions.
			if (index > size) {
				throw new IndexException(
						"Record index " + index + " out of range. Array contains " + size + " elements"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}

			int growableBlockSize = GrowableBlockHeader.ALLOCATED_SIZE.get(nd, growableBlockAddress);

			if (growableBlockSize > GrowableBlockHeader.MAX_GROWABLE_SIZE) {
				// If this array is so big that it's using a metablock, look up the correct sub-block and use the
				// correct address within the sub-block
				int blockRelativeIndex = growableBlockRelativeIndex % GrowableBlockHeader.MAX_GROWABLE_SIZE;
				int block = growableBlockRelativeIndex / GrowableBlockHeader.MAX_GROWABLE_SIZE;

				growableBlockAddress = db.getRecPtr(growableBlockAddress + MetaBlockHeader.META_BLOCK_HEADER_BYTES
						+ block * Database.PTR_SIZE);
				growableBlockRelativeIndex = blockRelativeIndex;
			}

			long dataStartAddress = growableBlockAddress + GrowableBlockHeader.GROWABLE_BLOCK_HEADER_BYTES;
			return dataStartAddress + growableBlockRelativeIndex * Database.PTR_SIZE;
		} else {
			// This record is one of the ones inlined in the header
			return address + ARRAY_HEADER_BYTES + index * Database.PTR_SIZE;
		}
	}

	/**
	 * Removes an entry from the array, given an element index. If the given index is not the last element
	 * in the list, the last element will have its index swapped with the removed element. If another element
	 * was swapped into the position of the removed element, this returns the value of that element. Otherwise,
	 * it returns 0.
	 */
	public long remove(Nd nd, long address, int index) {
		int currentSize = size(nd, address);
		int lastElementIndex = currentSize - 1;

		Database db = nd.getDB();
		if (index > lastElementIndex || index < 0) {
			throw new IndexException("Attempt to remove nonexistent element " + index //$NON-NLS-1$
					+ " from an array of size " + (lastElementIndex + 1)); //$NON-NLS-1$
		}

		long toRemoveAddress = getAddressOfRecord(nd, address, index);
		long returnValue;
		// If we're removing the last element
		if (index == lastElementIndex) {
			returnValue = 0;
			// Clear out the removed element
			db.putRecPtr(toRemoveAddress, 0);
		} else {
			long lastElementAddress = getAddressOfRecord(nd, address, lastElementIndex);
			long lastElementValue = db.getRecPtr(lastElementAddress);

			// Move the last element into the position occupied by the element being removed (this is a noop if
			// removing the last element)
			db.putRecPtr(toRemoveAddress, lastElementValue);

			// Clear out the last element
			db.putRecPtr(lastElementAddress, 0);

			returnValue = lastElementValue;
		}

		// Update the array size
		setSize(nd, address, currentSize - 1);
		repackIfNecessary(nd, address, currentSize);

		return returnValue;
	}

	/**
	 * Checks if we should reduce the amount of allocated in the growable region, such that the array can hold the given
	 * number of elements.
	 * 
	 * @param desiredSize
	 *            the new current size of the array or 0 to free up all memory
	 */
	private void repackIfNecessary(Nd nd, long address, int desiredSize) {
		long growableBlockAddress = GROWABLE_BLOCK_ADDRESS.get(nd, address);

		// If there is no growable block then the array is already as small as we can make it. Nothing to do.
		if (growableBlockAddress == 0) {
			return;
		}

		int desiredGrowableSize = desiredSize - this.inlineSize;

		int currentGrowableSize = GrowableBlockHeader.ALLOCATED_SIZE.get(nd, growableBlockAddress);
		int newGrowableSize = getGrowableRegionSizeFor(desiredSize);

		// We only need to repack if the new size is smaller than the old one
		if (newGrowableSize >= currentGrowableSize) {
			return;
		}

		Database db = nd.getDB();
		if (currentGrowableSize > GrowableBlockHeader.MAX_GROWABLE_SIZE) {
			// We are currently using a metablock
			int desiredBlockCount = (newGrowableSize + GrowableBlockHeader.MAX_GROWABLE_SIZE - 1)
					/ GrowableBlockHeader.MAX_GROWABLE_SIZE;
			int currentBlockCount = currentGrowableSize / GrowableBlockHeader.MAX_GROWABLE_SIZE;

			// Only deallocate memory if either there are either two full unused blocks
			// or the desired size is less than or equal to half of a block + 1. We add one to ensure
			// that the newly-shrunk array will still be about double the size of the used elements.
			boolean needsRepacking = (currentBlockCount - desiredBlockCount > 1)
					|| (newGrowableSize <= (GrowableBlockHeader.MAX_GROWABLE_SIZE / 2 + 1));
			if (!needsRepacking) {
				return;
			}

			long metablockRecordsAddress = growableBlockAddress + MetaBlockHeader.META_BLOCK_HEADER_BYTES;
			int currentBlock = currentBlockCount;
			while (--currentBlock >= desiredBlockCount) {
				long nextAddress = metablockRecordsAddress + currentBlock * Database.PTR_SIZE;
				long oldBlockAddress = db.getRecPtr(nextAddress);
				db.free(oldBlockAddress, Database.POOL_GROWABLE_ARRAY);
				db.putRecPtr(nextAddress, 0);
			}

			// If we still need to be using a metablock, we're done
			if (newGrowableSize > GrowableBlockHeader.MAX_GROWABLE_SIZE) {
				// First record the new growable region size
				GrowableBlockHeader.ALLOCATED_SIZE.put(nd, growableBlockAddress, newGrowableSize);
				return;
			}

			// Else we need to stop using a metablock.
			// Dispose the metablock and replace it with the first growable block
			long firstBlockAddress = db.getRecPtr(metablockRecordsAddress);
			int oldSize = GrowableBlockHeader.ARRAY_SIZE.get(nd, growableBlockAddress);
			db.free(growableBlockAddress, Database.POOL_GROWABLE_ARRAY);

			GROWABLE_BLOCK_ADDRESS.put(nd, address, firstBlockAddress);

			if (firstBlockAddress != 0) {
				currentGrowableSize = GrowableBlockHeader.MAX_GROWABLE_SIZE;
				GrowableBlockHeader.ARRAY_SIZE.put(nd, firstBlockAddress, oldSize);
				GrowableBlockHeader.ALLOCATED_SIZE.put(nd, firstBlockAddress,
						GrowableBlockHeader.MAX_GROWABLE_SIZE);
			}

			// Then we'll fall through to the normal (non-metablock) case, which may shrink the size of the last
			// growable block further
		}

		// If we're not using metablocks, we only resize the growable region once the size of the array shrinks
		// such that we're only using 1/4 of it.
		if (desiredGrowableSize <= (currentGrowableSize / 4 + 1)) {
			long newBlockAddress = resizeBlock(nd, address, newGrowableSize);

			GROWABLE_BLOCK_ADDRESS.put(nd, address, newBlockAddress);
		}
	}

	/**
	 * Returns the number of elements that should actually be allocated in the growable region for an array of the given
	 * size
	 */
	private int getGrowableRegionSizeFor(int arraySize) {
		int growableRegionSize = arraySize - this.inlineSize;

		if (growableRegionSize <= 0) {
			return 0;
		}

		// Find the next power of two that is equal or greater than the required size. We use inlineSize
		// as the minimum growable block size since we tend to assign a large inlineSize to lists with a large
		// average number of elements, and these are also the lists that will benefit from a larger initial block size.
		int nextGrowableSize = getNextPowerOfTwo(Math.max(growableRegionSize, this.inlineSize));

		if (nextGrowableSize > GrowableBlockHeader.MAX_GROWABLE_SIZE) {
			// If the next power of two is greater than the max block size but the requested size is smaller than it,
			// clamp it to the the max block size
			if (growableRegionSize <= GrowableBlockHeader.MAX_GROWABLE_SIZE) {
				return GrowableBlockHeader.MAX_GROWABLE_SIZE;
			}

			// For sizes larger than the max block size, we need to use a metablock. In this case, the allocated size
			// will be a multiple of the max block size.
			return roundUpToMultipleOf(GrowableBlockHeader.MAX_GROWABLE_SIZE, growableRegionSize);
		}

		return nextGrowableSize;
	}

	/**
	 * Returns the largest power of two that is less than or equal to the given integer
	 */
	private static int getPrevPowerOfTwo(int n) {
		n |= (n >> 1);
		n |= (n >> 2);
		n |= (n >> 4);
		n |= (n >> 8);
		n |= (n >> 16);
		return n - (n >> 1);
	}

	/**
	 * Returns the next power of two that is equal to or greater than the given int.
	 */
	private static int getNextPowerOfTwo(int toTest) {
		int highBit = getPrevPowerOfTwo(toTest);
		int nextGrowableSize = highBit;

		if (highBit != toTest) {
			assert (nextGrowableSize << 1) != 0;
			nextGrowableSize <<= 1;
		}
		return nextGrowableSize;
	}

	/**
	 * Rounds a value up to the nearest multiple of another value
	 */
	private static int roundUpToMultipleOf(int unit, int valueToRound) {
		int numberOfMetablocks = (valueToRound + unit - 1) / unit;

		return numberOfMetablocks * unit;
	}

	/**
	 * Returns the record size for a RawGrowableSize with the given number of inline records
	 */
	public int getRecordSize() {
		return ARRAY_HEADER_BYTES + Database.PTR_SIZE * this.inlineSize;
	}

	public void destruct(Nd nd, long address) {
		repackIfNecessary(nd, address, 0);
	}

	
	/**
	 * Returns true iff the size of the array is 0
	 * 
	 * @param address address of the array
	 * @return the array size, in number elements
	 */
	public boolean isEmpty(Nd nd, long address) {
		Database db = nd.getDB();
		long growableBlockAddress = GROWABLE_BLOCK_ADDRESS.get(nd, address);

		if (growableBlockAddress == 0) {
			if (this.inlineSize == 0) {
				return true;
			}
			// If there is no growable block or metablock, then the size is determined by the position of the first
			// null pointer among the inline records.
			long firstValue = db.getRecPtr(address + ARRAY_HEADER_BYTES);

			return firstValue == 0;
		}
		return GrowableBlockHeader.ARRAY_SIZE.get(nd, growableBlockAddress) == 0;
	}

	public int getCapacity(Nd nd, long address) {
		long growableBlockAddress = GROWABLE_BLOCK_ADDRESS.get(nd, address);

		if (growableBlockAddress == 0) {
			return this.inlineSize;
		}

		int growableBlockCurrentSize = GrowableBlockHeader.ALLOCATED_SIZE.get(nd, growableBlockAddress);

		return growableBlockCurrentSize + this.inlineSize;
	}
}
