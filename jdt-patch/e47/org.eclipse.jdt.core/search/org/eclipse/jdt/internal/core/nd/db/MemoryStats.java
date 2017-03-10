/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.internal.core.nd.ITypeFactory;
import org.eclipse.jdt.internal.core.nd.NdNodeTypeRegistry;

public class MemoryStats {
	public static final int TOTAL_MALLOC_POOLS = 64;
	/** The size of the statistics for a single malloc pool */
	public static final int SIZE = TOTAL_MALLOC_POOLS * PoolStats.RECORD_SIZE;

	private Map<Integer, PoolStats> stats = new HashMap<>();

	public final long address;
	private Chunk db;

	public static final class PoolStats {
		public static int POOL_ID_OFFSET = 0;
		public static int NUM_ALLOCATIONS_OFFSET = POOL_ID_OFFSET + Database.SHORT_SIZE;
		public static int TOTAL_SIZE_OFFSET = NUM_ALLOCATIONS_OFFSET + Database.LONG_SIZE;

		public static final int RECORD_SIZE = TOTAL_SIZE_OFFSET + Database.LONG_SIZE;

		short poolId;
		long numAllocations;
		long totalSize;
		long address;

		public PoolStats(Chunk db, long address) {
			this.address = address;
			this.poolId = db.getShort(POOL_ID_OFFSET + address);
			this.numAllocations = db.getLong(NUM_ALLOCATIONS_OFFSET + address);
			this.totalSize = db.getLong(TOTAL_SIZE_OFFSET + address);
		}

		public void setAllocations(Chunk db, long numAllocations) {
			this.numAllocations = numAllocations;
			db.putLong(this.address + NUM_ALLOCATIONS_OFFSET, numAllocations);
		}

		public void setTotalSize(Chunk db, long totalSize) {
			this.totalSize = totalSize;
			db.putLong(this.address + TOTAL_SIZE_OFFSET, totalSize);
		}

		public void setPoolId(Chunk db, short poolId) {
			this.poolId = poolId;
			db.putShort(this.address + POOL_ID_OFFSET, poolId);
		}

		public long getNumAllocations() {
			return this.numAllocations;
		}

		public short getPoolId() {
			return this.poolId;
		}

		public long getTotalSize() {
			return this.totalSize;
		}
	}

	public MemoryStats(Chunk db, long address) {
		this.db = db;
		this.address = address;
	}

	public void printMemoryStats(NdNodeTypeRegistry<?> nodeRegistry) {
		StringBuilder builder = new StringBuilder();
		for (PoolStats next : getSortedPools()) {
			builder.append(getPoolName(nodeRegistry, next.poolId));
			builder.append(" "); //$NON-NLS-1$
			builder.append(next.numAllocations);
			builder.append(" allocations, "); //$NON-NLS-1$
			builder.append(Database.formatByteString(next.totalSize));
			builder.append("\n"); //$NON-NLS-1$
		}
		System.out.println(builder.toString());
	}

	private String getPoolName(NdNodeTypeRegistry<?> registry, int poolId) {
		switch (poolId) {
			case Database.POOL_MISC: return "Miscellaneous"; //$NON-NLS-1$
			case Database.POOL_BTREE: return "B-Trees"; //$NON-NLS-1$
			case Database.POOL_DB_PROPERTIES: return "DB Properties"; //$NON-NLS-1$
			case Database.POOL_STRING_LONG: return "Long Strings"; //$NON-NLS-1$
			case Database.POOL_STRING_SHORT: return "Short Strings"; //$NON-NLS-1$
			case Database.POOL_LINKED_LIST: return "Linked Lists"; //$NON-NLS-1$
			case Database.POOL_STRING_SET: return "String Sets"; //$NON-NLS-1$
			case Database.POOL_GROWABLE_ARRAY: return "Growable Arrays"; //$NON-NLS-1$
			default:
				if (poolId >= Database.POOL_FIRST_NODE_TYPE) {
					ITypeFactory<?> type = registry.getClassForType((short)(poolId - Database.POOL_FIRST_NODE_TYPE));

					if (type != null) {
						return type.getElementClass().getSimpleName();
					}
				}
				return "Unknown memory pool " + poolId; //$NON-NLS-1$
		}
	}

	public Collection<PoolStats> getPools() {
		return this.stats.values();
	}

	public List<PoolStats> getSortedPools() {
		List<PoolStats> unsorted = new ArrayList<>();
		unsorted.addAll(getPools());
		Collections.sort(unsorted, new Comparator<PoolStats>() {
			@Override
			public int compare(PoolStats o1, PoolStats o2) {
				return Long.signum(o2.totalSize - o1.totalSize);
			}
		});
		return unsorted;
	}

	public void recordMalloc(short poolId, long size) {
		PoolStats toRecord = getPoolStats(poolId);
		toRecord.setAllocations(this.db, toRecord.numAllocations + 1);
		toRecord.setTotalSize(this.db, toRecord.totalSize + size);
	}

	private PoolStats getPoolStats(short poolId) {
		if (this.stats.isEmpty()) {
			refresh();
		}
		PoolStats result = this.stats.get((int)poolId);
		if (result == null) {
			if (this.stats.size() >= TOTAL_MALLOC_POOLS) {
				throw new IndexException("Too many malloc pools. Please increase the size of TOTAL_MALLOC_POOLS."); //$NON-NLS-1$
			}
			// Find the insertion position
			int idx = 0;
			for (;;idx++) {
				PoolStats nextPool = readPool(idx);
				if (idx > 0 && nextPool.poolId == 0) {
					break;
				}
				if (nextPool.poolId == poolId) {
					throw new IllegalStateException("The stats were out of sync with the database."); //$NON-NLS-1$
				}
				if (nextPool.poolId > poolId) {
					break;
				}
			}

			// Find the last pool position
			int lastIdx = idx;
			for (;;lastIdx++) {
				PoolStats nextPool = readPool(lastIdx);
				if (lastIdx > 0 && nextPool.poolId == 0) {
					break;
				}
			}

			// Shift all the pools to make room
			for (int shiftIdx = lastIdx; shiftIdx > idx; shiftIdx--) {
				PoolStats writeTo = readPool(shiftIdx);
				PoolStats readFrom = readPool(shiftIdx - 1);

				writeTo.setAllocations(this.db, readFrom.numAllocations);
				writeTo.setTotalSize(this.db, readFrom.totalSize);
				writeTo.setPoolId(this.db, readFrom.poolId);
			}

			result = readPool(idx);
			result.setAllocations(this.db, 0);
			result.setTotalSize(this.db, 0);
			result.setPoolId(this.db, poolId);

			refresh();

			result = this.stats.get((int)poolId);
		}
		return result;
	}

	private List<PoolStats> loadStats() {
		List<PoolStats> result = new ArrayList<>();
		for (int idx = 0; idx < TOTAL_MALLOC_POOLS; idx++) {
			PoolStats next = readPool(idx);

			if (idx > 0 && next.poolId == 0) {
				break;
			}
			
			result.add(next);
		}
		return result;
	}

	public void refresh() {
		this.stats.clear();

		for (PoolStats next : loadStats()) {
			this.stats.put((int)next.poolId, next);
		}
	}

	public PoolStats readPool(int idx) {
		return new PoolStats(this.db, this.address + idx * PoolStats.RECORD_SIZE);
	}

	public void recordFree(short poolId, long size) {
		PoolStats toRecord = getPoolStats(poolId);
		if (toRecord.numAllocations <= 0 || toRecord.totalSize < size) {
			throw new IndexException("Attempted to free more memory from pool " + poolId + " than was ever allocated");  //$NON-NLS-1$//$NON-NLS-2$
		}
		toRecord.setAllocations(this.db, toRecord.numAllocations - 1);
		toRecord.setTotalSize(this.db, toRecord.totalSize - size);
	}
}
