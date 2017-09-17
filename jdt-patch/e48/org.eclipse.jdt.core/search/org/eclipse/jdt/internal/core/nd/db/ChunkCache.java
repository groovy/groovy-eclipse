/*******************************************************************************
 * Copyright (c) 2007, 2016 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.db;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.JavaCore;

public final class ChunkCache {
	private static ChunkCache sSharedInstance;

	private Chunk[] fPageTable;
	private boolean fTableIsFull;
	private int fPointer;

	public static final String CHUNK_CACHE_SIZE_MB = "chunkCacheSizeMb"; //$NON-NLS-1$
	public static final String CHUNK_CACHE_SIZE_PERCENT = "chunkCacheSizePercent"; //$NON-NLS-1$

	public static final double CHUNK_CACHE_SIZE_MB_DEFAULT = 128.0;
	public static final double CHUNK_CACHE_SIZE_PERCENT_DEFAULT = 5.0;

	static {
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(JavaCore.PLUGIN_ID);
		long chunkSize = getChunkCacheSize(node);
		sSharedInstance= new ChunkCache(chunkSize);
		node.addPreferenceChangeListener(event -> {
			String key = event.getKey();
			if (key.equals(CHUNK_CACHE_SIZE_MB) | key.equals(CHUNK_CACHE_SIZE_PERCENT)) {
				sSharedInstance.setMaxSize(getChunkCacheSize(node));
			}
		});
	}

	private static long getChunkCacheSize(IEclipsePreferences node) {
		double maxSizeMb = node.getDouble(CHUNK_CACHE_SIZE_MB, CHUNK_CACHE_SIZE_MB_DEFAULT);
		double maxSizePercent = node.getDouble(CHUNK_CACHE_SIZE_PERCENT, CHUNK_CACHE_SIZE_PERCENT_DEFAULT);

		maxSizePercent = Math.max(1.0, Math.min(50.0, maxSizePercent));
		maxSizeMb = Math.max(maxSizeMb, 1.0);

		long m1= (long) (Runtime.getRuntime().maxMemory() / 100.0 * maxSizePercent);
		return Math.min(m1, (long) (maxSizeMb * 1024.0 * 1024.0));
	}

	public static ChunkCache getSharedInstance() {
		return sSharedInstance;
	}

	public ChunkCache() {
		this(5 * 1024 * 1024);
	}

	public ChunkCache(long maxSize) {
		this.fPageTable= new Chunk[computeLength(maxSize)];
	}

	public synchronized void add(Chunk chunk) {
		if (chunk.fCacheIndex >= 0) {
			chunk.fCacheHitFlag= true;
			return;
		}
		if (this.fTableIsFull) {
			evictChunk();
			chunk.fCacheIndex= this.fPointer;
			this.fPageTable[this.fPointer]= chunk;
		} else {
			chunk.fCacheIndex= this.fPointer;
			this.fPageTable[this.fPointer]= chunk;

			this.fPointer++;
			if (this.fPointer == this.fPageTable.length) {
				this.fPointer= 0;
				this.fTableIsFull= true;
			}
		}
	}

	/**
	 * Evicts a chunk from the page table and the chunk table.
	 * After this method returns, {@link #fPointer}  will contain
	 * the index of the evicted chunk within the page table.
	 */
	private void evictChunk() {
		/*
		 * Use the CLOCK algorithm to determine which chunk to evict.
		 * i.e., if the chunk in the current slot of the page table has been
		 * recently referenced (i.e. the reference flag is set), unset the
		 * reference flag and move to the next slot.  Otherwise, evict the
		 * chunk in the current slot.
		 */
		while (true) {
			Chunk chunk = this.fPageTable[this.fPointer];
			if (chunk.fCacheHitFlag) {
				chunk.fCacheHitFlag = false;
				this.fPointer = (this.fPointer + 1) % this.fPageTable.length;
			} else {
				chunk.fCacheIndex = -1;
				chunk.fDatabase.checkIfChunkReleased(chunk);
				this.fPageTable[this.fPointer] = null;
				return;
			}
		}
	}

	public synchronized void remove(Chunk chunk) {
		final int idx= chunk.fCacheIndex;
		if (idx >= 0) {
			if (this.fTableIsFull) {
				this.fPointer= this.fPageTable.length-1;
				this.fTableIsFull= false;
			} else {
				this.fPointer--;
			}
			chunk.fCacheIndex= -1;
			final Chunk move= this.fPageTable[this.fPointer];
			this.fPageTable[idx]= move;
			move.fCacheIndex= idx;
			this.fPageTable[this.fPointer]= null;
		}
	}

	/**
	 * Returns the maximum size of the chunk cache in bytes.
	 */
	public synchronized long getMaxSize() {
		return (long) this.fPageTable.length * Database.CHUNK_SIZE;
	}

	/**
	 * Clears the page table and changes it to hold chunks with
	 * maximum total memory of <code>maxSize</code>.
	 * @param maxSize the total size of the chunks in bytes.
	 */
	public synchronized void setMaxSize(long maxSize) {
		final int newLength= computeLength(maxSize);
		final int oldLength= this.fTableIsFull ? this.fPageTable.length : this.fPointer;
		if (newLength > oldLength) {
			Chunk[] newTable= new Chunk[newLength];
			System.arraycopy(this.fPageTable, 0, newTable, 0, oldLength);
			this.fTableIsFull= false;
			this.fPointer= oldLength;
			this.fPageTable= newTable;
		} else {
			for (int i = newLength; i < oldLength; i++) {
				Chunk chunk = this.fPageTable[i];
				chunk.fCacheIndex = -1;
				chunk.fDatabase.checkIfChunkReleased(chunk);
			}
			Chunk[] newTable= new Chunk[newLength];
			System.arraycopy(this.fPageTable, 0, newTable, 0, newLength);
			this.fTableIsFull= true;
			this.fPointer= 0;
			this.fPageTable= newTable;
		}
	}

	private int computeLength(long maxSize) {
		long maxLength= Math.min(maxSize / Database.CHUNK_SIZE, Integer.MAX_VALUE);
		return Math.max(1, (int) maxLength);
	}

	public synchronized void clear() {
		for (int i = 0; i < this.fPageTable.length; i++) {
			Chunk chunk = this.fPageTable[i];
			if (chunk == null) {
				continue;
			}
			chunk.fCacheIndex = -1;
			chunk.fDatabase.checkIfChunkReleased(chunk);
			this.fPageTable[i] = null;
		}
		this.fTableIsFull = false;
		this.fPointer = 0;
	}
}
