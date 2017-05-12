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
package org.eclipse.jdt.internal.core.nd.db;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Records a record of every modification to the database, in a circular buffer of fixed size. Whenever anything writes
 * to the database, the log records the address and size of the write, along with a call stack describing what was going
 * on at the time of the write. The actual bytes written to the database are not recorded. In addition to writes, it
 * also records every invocation of malloc and free.
 * <p>
 * Given a memory address range, we can trace the log backwards to find everything that ever happened to that address
 * range since the start of the log.
 * </p>
 * "call stacks" don't use java call stacks. They use explicit tags that are pushed and popped at the start and
 * end of operations related to modifying the database.
 */
public class ModificationLog {
	/**
	 * Used to attach messages to events in the log. Tags should be allocated in static initializers at application
	 * startup by calling {@link ModificationLog#createTag(String)}. Once allocated, the tag can be pushed and popped on to
	 * the stack in the log to mark the beginning and end of operations.
	 */
	public static class Tag {
		public final String name;
		public final int opNum;

		Tag(String name, int opNum) {
			this.name = name;
			this.opNum = opNum;
		}

		@Override
		public String toString() {
			return this.opNum + ":" + this.name; //$NON-NLS-1$
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + this.opNum;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Tag other = (Tag) obj;
			if (this.opNum != other.opNum)
				return false;
			return true;
		}
	}

	/**
	 * Represents a single entry in a {@link MemoryAccessLog}. That is, a single read, write, malloc, or free event that
	 * affected the memory range of interest.
	 */
	public static class MemoryOperation {
		private final List<Tag> stack;
		private final long time;
		private final long startAddress;
		private final int addressSize;
		private final byte operationType;

		public MemoryOperation(byte operationType, long time, long startAddress, int size, List<Tag> stack) {
			super();
			this.operationType = operationType;
			this.time = time;
			this.startAddress = startAddress;
			this.addressSize = size;
			this.stack = stack;
		}

		public List<Tag> getStack() {
			return this.stack;
		}

		public long getTime() {
			return this.time;
		}

		public long getStartAddress() {
			return this.startAddress;
		}

		public int getSize() {
			return this.addressSize;
		}

		public byte getOperationType() {
			return this.operationType;
		}

		public void printTo(StringBuilder builder, int indent) {
			indent(builder, indent);
			switch (getOperationType()) {
				case FREE_OPERATION: builder.append("freed"); break; //$NON-NLS-1$
				case MALLOC_OPERATION: builder.append("malloc'd"); break; //$NON-NLS-1$
				case WRITE_OPERATION: builder.append("wrote"); break; //$NON-NLS-1$
			}
			builder.append(" [address "); //$NON-NLS-1$
			builder.append(this.startAddress);
			builder.append(", size "); //$NON-NLS-1$
			builder.append(this.addressSize);
			builder.append("] at time "); //$NON-NLS-1$
			builder.append(this.time);
			builder.append("\n");  //$NON-NLS-1$
			List<Tag> theStack = new ArrayList<>();
			theStack.addAll(getStack());
			Collections.reverse(theStack);
			for (Tag next : theStack) {
				indent(builder, indent + 1);
				builder.append(next.name + "\n");  //$NON-NLS-1$
			}
		}
	}

	/**
	 * Contains a log of events related to a specific range of database addresses, in reverse chronological order.
	 */
	public static class MemoryAccessLog {
		private final List<MemoryOperation> operations;

		public MemoryAccessLog(List<MemoryOperation> operations) {
			super();
			this.operations = operations;
		}

		/**
		 * Returns a list of operations, in reverse order of time.
		 */
		public List<MemoryOperation> getOperations() {
			return this.operations;
		}

		/**
		 * Returns true iff this log contains a double malloc or a double free
		 */
		public boolean hasInconsistentMemoryAllocation() {
			boolean known = false;
			boolean allocated = false;
			for (MemoryOperation next : this.operations) {
				boolean newAllocatedState;
				if (next.getOperationType() == MALLOC_OPERATION) {
					newAllocatedState = false;
				} else if (next.getOperationType() == FREE_OPERATION) {
					newAllocatedState = true;
				} else {
					continue;
				}

				if (!known) {
					known = true;
				} else if (allocated == newAllocatedState) {
					return true;
				}
				allocated = newAllocatedState;
			}
			return false;
		}

		/**
		 * Search for anomalies in the log and produce a reduced report
		 * 
		 * @return a log containing the most interesting results
		 */
		public MemoryAccessLog reduce(int maxWrites) {
			boolean includeAllMallocs = hasInconsistentMemoryAllocation();
			int numWrites = 0;

			List<MemoryOperation> result = new ArrayList<>();
			for (MemoryOperation next : this.operations) {
				boolean keepGoing = true;
				switch (next.getOperationType()) {
					case MALLOC_OPERATION: {
						result.add(next);
						keepGoing = includeAllMallocs;
						break;
					}
					case FREE_OPERATION: {
						result.add(next);
						break;
					}
					case WRITE_OPERATION: {
						if (numWrites < maxWrites) {
							result.add(next);
						}
						numWrites++;
					}
				}
				if (!keepGoing) {
					break;
				}
			}

			return new MemoryAccessLog(result);
		}
	}

	private static Map<Integer, Tag> activeTags = new HashMap<>();
	private final ArrayDeque<Tag> operationStack = new ArrayDeque<>();
	private long[] buffer0;
	private int[] buffer1;
	private byte[] operation;
	private int insertionPosition;
	private int currentEntries;
	private long timer;

	public static final byte PUSH_OPERATION = 0;
	public static final byte POP_OPERATION = 1;
	public static final byte WRITE_OPERATION = 2;
	public static final byte MALLOC_OPERATION = 3;
	public static final byte FREE_OPERATION = 4;

	public ModificationLog(int size) {
		allocateBuffers(size);
	}

	public void clear() {
		this.currentEntries = 0;
	}

	private void allocateBuffers(int sizeInMegs) {
		int entries = getBufferEntriesFor(sizeInMegs);
		if (entries != 0) {
			this.buffer0 = new long[entries];
			this.buffer1 = new int[entries];
			this.operation = new byte[entries];
		} else {
			this.buffer0 = null;
			this.buffer1 = null;
			this.operation = null;
		}
	}

	private static int getBufferEntriesFor(int sizeInMegs) {
		int sizeOfABufferEntry = 8 + 4 + 1; // size, in bytes, of one long, one int, and one byte 
		return sizeInMegs * 1024 * 1024 / sizeOfABufferEntry;
	}

	public int getBufferEntries() {
		return this.buffer0 == null ? 0 : this.buffer0.length;
	}

	public void setBufferSize(int megs) {
		int oldBufferLength = getBufferEntries();
		int newBufferLength = getBufferEntriesFor(megs);

		if (oldBufferLength == newBufferLength) {
			return;
		}

		long[] oldBuffer0 = this.buffer0;
		int[] oldBuffer1 = this.buffer1;
		byte[] oldOperation = this.operation;

		allocateBuffers(megs);

		if (this.buffer0 == null) {
			this.currentEntries = 0;
			this.insertionPosition = 0;
			this.operationStack.clear();
			return;
		}
		int newBufferSize = Math.min(this.buffer0.length, this.currentEntries);
		if (oldBufferLength > 0) {
			int readStart = (this.insertionPosition + oldBufferLength - newBufferSize) % oldBufferLength;
			if (readStart >= this.insertionPosition) {
				int entriesFromEnd = oldBufferLength - readStart;
				System.arraycopy(oldBuffer0, readStart, this.buffer0, 0, entriesFromEnd);
				System.arraycopy(oldBuffer1, readStart, this.buffer1, 0, entriesFromEnd);
				System.arraycopy(oldOperation, readStart, this.operation, 0, entriesFromEnd);

				System.arraycopy(oldBuffer0, 0, this.buffer0, entriesFromEnd, this.insertionPosition);
				System.arraycopy(oldBuffer1, 0, this.buffer1, entriesFromEnd, this.insertionPosition);
				System.arraycopy(oldOperation, 0, this.operation, entriesFromEnd, this.insertionPosition);
			} else {
				int entriesToCopy = this.insertionPosition - readStart;
				System.arraycopy(oldBuffer0, readStart, this.buffer0, 0, entriesToCopy);
				System.arraycopy(oldBuffer1, readStart, this.buffer1, 0, entriesToCopy);
				System.arraycopy(oldOperation, readStart, this.operation, 0, entriesToCopy);
			}
		}

		this.currentEntries = newBufferSize;
		this.insertionPosition = newBufferSize % this.buffer0.length;
	}

	public static void indent(StringBuilder builder, int indent) {
		for (int count = 0; count < indent; count++) {
			builder.append("    "); //$NON-NLS-1$
		}
	}

	public boolean enabled() {
		return this.buffer0 != null;
	}

	public void start(Tag tag) {
		if (!enabled()) {
			return;
		}

		this.operationStack.add(tag);
		addToQueue(PUSH_OPERATION, 0, tag.opNum);
	}

	public void end(Tag tag) {
		if (!enabled()) {
			return;
		}
		if (!this.operationStack.getLast().equals(tag)) {
			throw new IllegalStateException();
		}
		this.operationStack.removeLast();
		addToQueue(POP_OPERATION, 0, tag.opNum);
	}

	public void recordWrite(long address, int size) {
		if (!enabled()) {
			return;
		}
		this.timer++;
		addToQueue(WRITE_OPERATION, address, size);
	}

	public void recordMalloc(long address, int size) {
		if (!enabled()) {
			return;
		}
		this.timer++;
		addToQueue(MALLOC_OPERATION, address, size);
	}

	public void recordFree(long address, int size) {
		if (!enabled()) {
			return;
		}
		this.timer++;
		addToQueue(FREE_OPERATION, address, size);
	}

	private void addToQueue(byte opConstant, long arg0, int arg1) {
		this.buffer0[this.insertionPosition] = arg0;
		this.buffer1[this.insertionPosition] = arg1;
		this.operation[this.insertionPosition] = opConstant;
		this.insertionPosition = (this.insertionPosition + 1) % this.buffer0.length;

		if (this.currentEntries < this.buffer0.length) {
			this.currentEntries++;
		}
	}

	public long getWriteCount() {
		return this.timer;
	}

	/**
	 * Returns information about the last write to the given address range
	 */
	public MemoryAccessLog getReportFor(long address, int size) {
		List<Tag> tags = new ArrayList<>();
		tags.addAll(this.operationStack);

		List<MemoryOperation> operations = new ArrayList<>();
		if (this.buffer0 != null) {
			int pointerToStart = (this.insertionPosition + this.buffer0.length - this.currentEntries) % this.buffer0.length;
			int currentPosition = (this.insertionPosition + this.buffer0.length - 1) % this.buffer0.length;
			long currentWrite = this.timer;
			do {
				long nextAddress = this.buffer0[currentPosition];
				int nextArgument = this.buffer1[currentPosition];
				byte nextOp = this.operation[currentPosition];
	
				switch (nextOp) {
					case POP_OPERATION: {
						tags.add(getTagForId(nextArgument));
						break;
					}
					case PUSH_OPERATION: {
						tags.remove(tags.size() - 1);
						break;
					}
					default: {
						boolean isMatch = false;
						if (address < nextAddress) {
							long diff = nextAddress - address;
							if (diff < size) {
								isMatch = true;
							}
						} else {
							long diff = address - nextAddress;
							if (diff < nextArgument) {
								isMatch = true;
							}
						}
	
						if (isMatch) {
							List<Tag> stack = new ArrayList<>();
							stack.addAll(tags);
							MemoryOperation nextOperation = new MemoryOperation(nextOp, currentWrite, nextAddress,
									nextArgument, stack);
							operations.add(nextOperation);
						}
	
						currentWrite--;
					}
				}
				currentPosition = (currentPosition + this.buffer0.length - 1) % this.buffer0.length;
			} while (currentPosition != pointerToStart);
		}
		return new MemoryAccessLog(operations);
	}

	public static Tag createTag(String tagName) {
		Tag result = new Tag(tagName, activeTags.size());
		activeTags.put(activeTags.size(), result);
		return result;
	}

	private Tag getTagForId(int nextArgument) {
		return activeTags.get(nextArgument);
	}
}
