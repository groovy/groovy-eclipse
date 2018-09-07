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
package org.eclipse.jdt.internal.core.nd;

import org.eclipse.jdt.internal.core.nd.db.Database;
import org.eclipse.jdt.internal.core.nd.db.IndexException;

/**
 * {@link NdRawLinkedList} stores a list of fixed-sized records. Along with the records themselves, there is also
 * a bit field associated with each record which can hold a small number of bits of metadata per record.
 * The underlying format is as follows:
 *
 * <pre>
 * Bytes       Content
 * ----------------
 * 4           Pointer to the next block. If this is 0, this is the last block and it is not yet full. The number of
 *             elements will be stored at the position where the last element would normally start. If this points back
 *             to the start of the block, this is the last block and it is full. If this holds any other value, the
 *             block is full and this points to the next block.
 * headerSize  Bit field for this block (the bits for each element are tightly packed)
 * recordSize  The content of the first element in the block
 * recordSize  The content of the second element in the block
 * ...         repeated recordsPerBlock times
 * recordSize  If the block is full, this holds the last
 * </pre>
 *
 * stored in linked blocks where each block is an array of record pointers. Each block contains a pointer to the
 * subsequent block, so they can be chained.
 * <p>
 * The size of the blocks are generally hardcoded. All blocks are the same size except for the first block whose size
 * may be configured independently. The size of the first block may be zero, in which case the first "block" is
 * simply a pointer to the following block or null.
 */
public class NdRawLinkedList {
	private static final int NEXT_MEMBER_BLOCK = 0;
	private static final int ELEMENT_START_POSITION = NEXT_MEMBER_BLOCK + Database.PTR_SIZE;

	private final long address;
	private final Nd nd;
	private final int firstBlockRecordCount;
	private final int recordCount;
	private final int elementRecordSize;
	private final int metadataBitsPerRecord;

	// Derived data. Holds the address for the last block we know about
	private long lastKnownBlock;

	public static interface ILinkedListVisitor {
		public void visit(long address, short metadataBits, int index) throws IndexException;
	}

	/**
	 * @param nd the Nd object
	 * @param address pointer to the start of the linked list
	 * @param recordsPerBlock number of records per block. This is normally a hardcoded value.
	 */
	public NdRawLinkedList(Nd nd, long address, int elementRecordSize, int firstBlockRecordCount, int recordsPerBlock,
			int metadataBitsPerRecord) {
		assert(recordsPerBlock > 0);
		assert(firstBlockRecordCount >= 0);
		this.nd = nd;
		this.address = address;
		this.firstBlockRecordCount = firstBlockRecordCount;
		this.recordCount = recordsPerBlock;
		this.elementRecordSize = elementRecordSize;
		this.lastKnownBlock = address;
		this.metadataBitsPerRecord = metadataBitsPerRecord;
	}

	/**
	 * Returns the record size for a linked list with the given element record size and number of
	 * records per block
	 */
	public static int recordSize(int elementRecordSize, int recordsPerBlock, int metadataBitsPerRecord) {
		int metadataSize = 0;

		if (metadataBitsPerRecord > 0) {
			int metadataRecordsPerShort = 16 / metadataBitsPerRecord;
			int numberOfShorts = (recordsPerBlock + metadataRecordsPerShort - 1) / metadataRecordsPerShort;

			metadataSize = 2 * numberOfShorts;
		}

		return Database.PTR_SIZE + elementRecordSize * recordsPerBlock + metadataSize;
	}

	public Nd getNd() {
		return this.nd;
	}

	private int getElementsInBlock(long currentRecord, long ptr, int currentRecordCount) throws IndexException {
		if (ptr == 0 && currentRecordCount > 0) {
			return getDB().getInt(getAddressOfElement(currentRecord, currentRecordCount - 1));
		}
		return currentRecordCount;
	}

	private Database getDB() {
		return this.nd.getDB();
	}

	public long getAddress() {
		return this.address;
	}

	/**
	 * Adds a new element to the list and returns the record pointer to the start of the newly-allocated object
	 *
	 * @param metadataBits the metadata bits to attach to the new member. Use 0 if this list does not use metadata.
	 */
	public long addMember(short metadataBits) throws IndexException {
		Database db = getDB();
		long current = this.lastKnownBlock;
		int thisBlockRecordCount = this.firstBlockRecordCount;
		while (true) {
			long ptr = db.getRecPtr(current + NEXT_MEMBER_BLOCK);
			int elementsInBlock = getElementsInBlock(current, ptr, thisBlockRecordCount);

			// If there's room in this block
			if (elementsInBlock < thisBlockRecordCount) {
				long positionOfElementCount = getAddressOfElement(current, thisBlockRecordCount - 1);
				// If there's only one space left
				if (elementsInBlock == thisBlockRecordCount - 1) {
					// We use the fact that the next pointer points to itself as a sentinel to indicate that the
					// block is full and there are no further blocks
					db.putRecPtr(current + NEXT_MEMBER_BLOCK, current);
					// Zero out the int we've been using to hold the count of elements
					db.putInt(positionOfElementCount, 0);
				} else {
					// Increment the element count
					db.putInt(positionOfElementCount, elementsInBlock + 1);
				}

				if (this.metadataBitsPerRecord > 0) {
					int metadataMask = (1 << this.metadataBitsPerRecord) - 1;
					int metadataRecordsPerShort = this.metadataBitsPerRecord == 0 ? 0
							: (16 / this.metadataBitsPerRecord);
					metadataBits &= metadataMask;

					int metadataBitOffset = elementsInBlock % metadataRecordsPerShort;
					long metadataStart = getAddressOfMetadata(current, thisBlockRecordCount);
					int whichShort = elementsInBlock / metadataRecordsPerShort;
					long metadataOffset = metadataStart + 2 * whichShort;
					short metadataValue = db.getShort(metadataOffset);

					// Resetting the previous visibility bits of the target member.
					metadataValue &= ~(metadataMask << metadataBitOffset * this.metadataBitsPerRecord);
					// Setting the new visibility bits of the target member.
					metadataValue |= metadataBits << metadataBitOffset * this.metadataBitsPerRecord;

					getDB().putShort(metadataOffset, metadataValue);
				}

				this.lastKnownBlock = current;
				return getAddressOfElement(current, elementsInBlock);
			} else {
				// When ptr == current, this is a sentinel indicating that the block is full and there are no
				// further blocks. If this is the case, create a new block
				if (isLastBlock(current, ptr)) {
					current = db.malloc(
							recordSize(this.elementRecordSize, this.recordCount, this.metadataBitsPerRecord), Database.POOL_LINKED_LIST);
					db.putRecPtr(current + NEXT_MEMBER_BLOCK, current);
				} else {
					thisBlockRecordCount = this.recordCount;
					// Else, there are more blocks following this one so advance
					current = ptr;
				}
			}
		}
	}

	private long getAddressOfElement(long blockRecordStart, int elementNumber) {
		return blockRecordStart + ELEMENT_START_POSITION + elementNumber * this.elementRecordSize;
	}

	private long getAddressOfMetadata(long blockRecordStart, int blockRecordCount) {
		return getAddressOfElement(blockRecordStart, blockRecordCount);
	}

	public void accept(ILinkedListVisitor visitor) throws IndexException {
		int count = 0;
		Database db = getDB();

		int blockRecordCount = this.firstBlockRecordCount;
		int metadataMask = (1 << this.metadataBitsPerRecord) - 1;
		int metadataRecordsPerShort = this.metadataBitsPerRecord == 0 ? 0 : (16 / this.metadataBitsPerRecord);
		long current = this.address;
		while (true) {
			long ptr = db.getRecPtr(current + NEXT_MEMBER_BLOCK);
			int elementsInBlock = getElementsInBlock(current, ptr, blockRecordCount);

			long metadataStart = getAddressOfMetadata(current, blockRecordCount);
			for (int idx = 0; idx < elementsInBlock; idx++) {
				long elementRecord = getAddressOfElement(current, idx);

				short metadataBits = 0;

				if (metadataRecordsPerShort > 0) {
					int metadataBitOffset = idx % metadataRecordsPerShort;
					int whichShort = idx / metadataRecordsPerShort;
					long metadataOffset = metadataStart + 2 * whichShort;
					metadataBits = getDB().getShort(metadataOffset);

					metadataBits >>>= metadataBits * metadataBitOffset;
					metadataBits &= metadataMask;
				}

				visitor.visit(elementRecord, metadataBits, count++);
			}

			blockRecordCount = this.recordCount;

			if (isLastBlock(current, ptr)) {
				return;
			}

			current = ptr;
		}
	}

	public void destruct() throws IndexException {
		Database db = getDB();
		long current = this.address;
		while (true) {
			long ptr = db.getRecPtr(current + NEXT_MEMBER_BLOCK);
			db.free(current, Database.POOL_LINKED_LIST);

			if (isLastBlock(current, ptr)) {
				return;
			}

			current = ptr;
		}
	}

	private boolean isLastBlock(long blockAddress, long pointerToNextBlock) {
		return pointerToNextBlock == 0 || pointerToNextBlock == blockAddress;
	}

	/**
	 * Returns the number of elements in this list. This is an O(n) operation.
	 * @throws IndexException
	 */
	public int size() throws IndexException {
		int count = 0;
		Database db = getDB();
		int currentRecordCount = this.firstBlockRecordCount;
		long current = this.address;
		while (true) {
			long ptr = db.getRecPtr(current + NEXT_MEMBER_BLOCK);
			count += getElementsInBlock(current, ptr, currentRecordCount);

			if (isLastBlock(current, ptr)) {
				break;
			}

			currentRecordCount = this.recordCount;
			current = ptr;
		}

		return count;
	}
}
