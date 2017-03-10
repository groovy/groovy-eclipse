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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.internal.core.nd.db.ChunkCache;
import org.eclipse.jdt.internal.core.nd.db.Database;
import org.eclipse.jdt.internal.core.nd.db.IndexException;

/**
 * Network Database for storing semantic information.
 */
public final class Nd {
	private static final int CANCELLATION_CHECK_INTERVAL = 500;
	private static final int BLOCKED_WRITE_LOCK_OUTPUT_INTERVAL = 30000;
	private static final int LONG_WRITE_LOCK_REPORT_THRESHOLD = 1000;
	private static final int LONG_READ_LOCK_WAIT_REPORT_THRESHOLD = 1000;

	/**
	 * Controls the number of pages that are allowed to be dirty before a
	 * flush will occur. Specified as a ratio of the total cache size. For
	 * example, a ration of 0.5 would mean that a flush is forced if half
	 * of the cache is dirty.
	 */
	private static final double MAX_DIRTY_CACHE_RATIO = 0.25;
	public static boolean sDEBUG_LOCKS= false;
	public static boolean DEBUG_DUPLICATE_DELETIONS = false;

	private final int currentVersion;
	private final int maxVersion;
	private final int minVersion;

	/**
	 * Stores data that has been stored via {@link #setData}. Synchronize on {@link #cookies} before accessing.
	 */
	private final Map<Class<?>, Object> cookies = new HashMap<>();

	public static int version(int major, int minor) {
		return (major << 16) + minor;
	}

	/**
	 * Returns the version that shall be used when creating new databases.
	 */
	public int getDefaultVersion() {
		return this.currentVersion;
	}

	public boolean isSupportedVersion(int vers) {
		return vers >= this.minVersion && vers <= this.maxVersion;
	}

	public int getMinSupportedVersion() {
		return this.minVersion;
	}

	public int getMaxSupportedVersion() {
		return this.maxVersion;
	}

	public static String versionString(int version) {
		final int major= version >> 16;
		final int minor= version & 0xffff;
		return "" + major + '.' + minor; //$NON-NLS-1$
	}

	// Local caches
	protected Database db;
	private File fPath;
	private final HashMap<Object, Object> fResultCache = new HashMap<>();

	private final NdNodeTypeRegistry<NdNode> fNodeTypeRegistry;
	private HashMap<Long, Object> pendingDeletions = new HashMap<>();

	private IReader fReader = new IReader() {
		@Override
		public void close() {
			releaseReadLock();
		}
	};

	/**
	 * This long is incremented every time a change is written to the database. Can be used to determine if the database
	 * has changed.
	 */
	private long fWriteNumber;

	public Nd(File dbPath, NdNodeTypeRegistry<NdNode> nodeTypes, int minVersion, int maxVersion,
			int currentVersion) throws IndexException {
		this(dbPath, ChunkCache.getSharedInstance(), nodeTypes, minVersion, maxVersion, currentVersion);
	}

	public Nd(File dbPath, ChunkCache chunkCache, NdNodeTypeRegistry<NdNode> nodeTypes, int minVersion,
			int maxVersion, int currentVersion) throws IndexException {
		this.currentVersion = currentVersion;
		this.maxVersion = maxVersion;
		this.minVersion = minVersion;
		this.fNodeTypeRegistry = nodeTypes;
		loadDatabase(dbPath, chunkCache);
		if (sDEBUG_LOCKS) {
			this.fLockDebugging = new HashMap<>();
			System.out.println("Debugging database Locks"); //$NON-NLS-1$
		}
	}

	public File getPath() {
		return this.fPath;
	}

	public long getWriteNumber() {
		return this.fWriteNumber;
	}

	public void scheduleDeletion(long addressOfNodeToDelete) {
		if (this.pendingDeletions.containsKey(addressOfNodeToDelete)) {
			logDoubleDeletion(addressOfNodeToDelete);
			return;
		}

		Object data = Boolean.TRUE;
		if (DEBUG_DUPLICATE_DELETIONS) {
			data = new RuntimeException();
		}
		this.pendingDeletions.put(addressOfNodeToDelete, data);
	}

	protected void logDoubleDeletion(long addressOfNodeToDelete) {
		// Sometimes an object can be scheduled for deletion twice, if it is created and then discarded shortly
		// afterward during indexing. This may indicate an inefficiency in the indexer but is not necessarily
		// a bug.
		// If you're debugging issues related to duplicate deletions, set DEBUG_DUPLICATE_DELETIONS to true
		Package.log("Database object queued for deletion twice", new RuntimeException()); //$NON-NLS-1$
		Object earlierData = this.pendingDeletions.get(addressOfNodeToDelete);
		if (earlierData instanceof RuntimeException) {
			RuntimeException exception = (RuntimeException) earlierData;

			Package.log("Data associated with earlier deletion stack was:", exception); //$NON-NLS-1$
		}
	}

	/**
	 * Synchronously processes all pending deletions
	 */
	public void processDeletions() {
		while (!this.pendingDeletions.isEmpty()) {
			long next = this.pendingDeletions.keySet().iterator().next();

			deleteIfUnreferenced(next);

			this.pendingDeletions.remove(next);
		}
	}

	/**
	 * Inserts a cookie that can be later retrieved via getData(String). 
	 */
	public <T> void setData(Class<T> key, T value) {
		synchronized (this.cookies) {
			this.cookies.put(key, value);
		}
	}

	/**
	 * Returns a cookie that was previously attached using {@link #setData(Class, Object)}. If no such cookie
	 * exists, it is computed using the given function and remembered for later. The function may return null.
	 * If it does, this method will also return null and no cookie will be stored.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getData(Class<T> key, Supplier<T> defaultValue) {
		T result;
		synchronized (this.cookies) {
			result = (T) this.cookies.get(key);
		}

		if (result == null) {
			result = defaultValue.get();

			if (result != null) {
				synchronized (this.cookies) {
					T newResult = (T) this.cookies.get(key);
					if (newResult == null) {
						this.cookies.put(key, result);
					} else {
						result = newResult;
					}
				}
			}
		}

		return result;
	}

	/**
	 * Returns whether this {@link Nd} can never be written to. Writable subclasses should return false.
	 */
	protected boolean isPermanentlyReadOnly() {
		return false;
	}

	private void loadDatabase(File dbPath, ChunkCache cache) throws IndexException {
		this.fPath= dbPath;
		final boolean lockDB= this.db == null || this.lockCount != 0;

		clearCaches();
		this.db = new Database(this.fPath, cache, getDefaultVersion(), isPermanentlyReadOnly());

		this.db.setLocked(lockDB);
		if (!isSupportedVersion()) {
			Package.logInfo("Index database uses the unsupported version " + this.db.getVersion() //$NON-NLS-1$
				+ ". Deleting and recreating."); //$NON-NLS-1$
			this.db.close();
			this.fPath.delete();
			this.db = new Database(this.fPath, cache, getDefaultVersion(), isPermanentlyReadOnly());
			this.db.setLocked(lockDB);
		}
		this.fWriteNumber = this.db.getLong(Database.WRITE_NUMBER_OFFSET);
		this.db.setLocked(this.lockCount != 0);
	}

	public Database getDB() {
		return this.db;
	}

	// Read-write lock rules. Readers don't conflict with other readers,
	// Writers conflict with readers, and everyone conflicts with writers.
	private final Object mutex = new Object();
	private int lockCount;
	private int waitingReaders;
	private long lastWriteAccess= 0;
	//private long lastReadAccess= 0;
	private long timeWriteLockAcquired;
	private Thread writeLockOwner;

	public IReader acquireReadLock() {
		try {
			long t = sDEBUG_LOCKS ? System.nanoTime() : 0;
			synchronized (this.mutex) {
				++this.waitingReaders;
				try {
					while (this.lockCount < 0)
						this.mutex.wait();
				} finally {
					--this.waitingReaders;
				}
				++this.lockCount;
				this.db.setLocked(true);

				if (sDEBUG_LOCKS) {
					t = (System.nanoTime() - t) / 1000000;
					if (t >= LONG_READ_LOCK_WAIT_REPORT_THRESHOLD) {
						System.out.println("Acquired index read lock after " + t + " ms wait."); //$NON-NLS-1$//$NON-NLS-2$
					}
					incReadLock(this.fLockDebugging);
				}
				return this.fReader;
			}
		} catch (InterruptedException e) {
			throw new OperationCanceledException();
		}
	}

	public void releaseReadLock() {
		synchronized (this.mutex) {
			assert this.lockCount > 0: "No lock to release"; //$NON-NLS-1$
			if (sDEBUG_LOCKS) {
				decReadLock(this.fLockDebugging);
			}

			//this.lastReadAccess= System.currentTimeMillis();
			if (this.lockCount > 0)
				--this.lockCount;
			this.mutex.notifyAll();
			this.db.setLocked(this.lockCount != 0);
		}
		// A lock release probably means that some AST is going away. The result cache has to be
		// cleared since it may contain objects belonging to the AST that is going away. A failure
		// to release an AST object would cause a memory leak since the whole AST would remain
		// pinned to memory.
		// TODO(sprigogin): It would be more efficient to replace the global result cache with
		// separate caches for each AST.
		//clearResultCache();
	}

	/**
	 * Acquire a write lock on this {@link Nd}. Blocks until any existing read/write locks are released.
	 * @throws OperationCanceledException
	 * @throws IllegalStateException if this {@link Nd} is not writable
	 */
	public void acquireWriteLock(IProgressMonitor monitor) {
		try {
			acquireWriteLock(0, monitor);
		} catch (InterruptedException e) {
			throw new OperationCanceledException();
		}
	}

	/**
	 * Acquire a write lock on this {@link Nd}, giving up the specified number of read locks first. Blocks
	 * until any existing read/write locks are released.
	 * @throws InterruptedException
	 * @throws IllegalStateException if this {@link Nd} is not writable
	 */
	public void acquireWriteLock(int giveupReadLocks, IProgressMonitor monitor) throws InterruptedException {
		assert !isPermanentlyReadOnly();
		synchronized (this.mutex) {
			if (sDEBUG_LOCKS) {
				incWriteLock(giveupReadLocks);
			}

			if (giveupReadLocks > 0) {
				// give up on read locks
				assert this.lockCount >= giveupReadLocks: "Not enough locks to release"; //$NON-NLS-1$
				if (this.lockCount < giveupReadLocks) {
					giveupReadLocks= this.lockCount;
				}
			} else {
				giveupReadLocks= 0;
			}

			// Let the readers go first
			long start= sDEBUG_LOCKS ? System.currentTimeMillis() : 0;
			while (this.lockCount > giveupReadLocks || this.waitingReaders > 0 || (this.lockCount < 0)) {
				this.mutex.wait(CANCELLATION_CHECK_INTERVAL);
				if (monitor != null && monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				if (sDEBUG_LOCKS) {
					start = reportBlockedWriteLock(start, giveupReadLocks);
				}
			}
			this.lockCount= -1;
			if (sDEBUG_LOCKS)
				this.timeWriteLockAcquired = System.currentTimeMillis();
			this.db.setExclusiveLock();
			if (this.writeLockOwner != null && this.writeLockOwner != Thread.currentThread()) {
				throw new IllegalStateException("We somehow managed to acquire a write lock while another thread already holds it."); //$NON-NLS-1$
			}
			this.writeLockOwner = Thread.currentThread();
		}
	}

	public final void releaseWriteLock() {
		releaseWriteLock(0, false);
	}

	@SuppressWarnings("nls")
	public void releaseWriteLock(int establishReadLocks, boolean flush) {
		synchronized (this.mutex) {
			Thread current = Thread.currentThread();
			if (current != this.writeLockOwner) {
				throw new IllegalStateException("Index wasn't locked by this thread!!!");
			}
			this.writeLockOwner = null;
		}
		RuntimeException exception = null;
		boolean wasInterrupted = false;
		try {
			// When all locks are released we can clear the result cache.
			if (establishReadLocks == 0) {
				clearResultCache();
			}
			this.db.putLong(Database.WRITE_NUMBER_OFFSET, ++this.fWriteNumber);
			// Process any outstanding deletions now
			processDeletions();
		} catch (RuntimeException e) {
			exception = e;
		} finally {
			this.db.giveUpExclusiveLock();
			assert this.lockCount == -1;
			this.lastWriteAccess = System.currentTimeMillis();
			try {
				releaseWriteLockAndFlush(establishReadLocks, flush);
			} catch (RuntimeException e) {
				if (exception != null) {
					e.addSuppressed(exception);
				}
				throw e;
			}
		}

		if (wasInterrupted) {
			throw new OperationCanceledException();
		}
	}

	private void releaseWriteLockAndFlush(int establishReadLocks, boolean flush) throws AssertionError {
		int dirtyPages = this.getDB().getDirtyChunkCount();

		// If there are too many dirty pages, force a flush now.
		int totalCacheSize = (int) (this.db.getCache().getMaxSize() / Database.CHUNK_SIZE);
		if (dirtyPages > totalCacheSize * MAX_DIRTY_CACHE_RATIO) {
			flush = true;
		}

		int initialReadLocks = flush ? establishReadLocks + 1 : establishReadLocks;
		// Convert this write lock to a read lock while we flush the page cache to disk. That will prevent
		// other writers from dirtying more pages during the flush but will allow reads to proceed.
		synchronized (this.mutex) {
			if (sDEBUG_LOCKS) {
				long timeHeld = this.lastWriteAccess - this.timeWriteLockAcquired;
				if (timeHeld >= LONG_WRITE_LOCK_REPORT_THRESHOLD) {
					System.out.println("Index write lock held for " + timeHeld + " ms");  //$NON-NLS-1$//$NON-NLS-2$
				}
				decWriteLock(initialReadLocks);
			}

			if (this.lockCount < 0) {
				this.lockCount = initialReadLocks;
			}
			this.mutex.notifyAll();
			this.db.setLocked(initialReadLocks != 0);
		}

		if (flush) {
			try {
				this.db.flush();
			} finally {
				releaseReadLock();
			}
		}
	}

	public boolean hasWaitingReaders() {
		synchronized (this.mutex) {
			return this.waitingReaders > 0;
		}
	}

	public long getLastWriteAccess() {
		return this.lastWriteAccess;
	}

	public boolean isSupportedVersion() throws IndexException {
		final int version = this.db.getVersion();
		return version >= this.minVersion && version <= this.maxVersion;
	}

	public void close() throws IndexException {
		this.db.close();
		clearCaches();
	}

	private void clearCaches() {
//		fileIndex= null;
//		tagIndex = null;
//		indexOfDefectiveFiles= null;
//		indexOfFiledWithUnresolvedIncludes= null;
//		fLinkageIDCache.clear();
		clearResultCache();
	}

	public void clearResultCache() {
		synchronized (this.fResultCache) {
			this.fResultCache.clear();
		}
	}

	public Object getCachedResult(Object key) {
		synchronized (this.fResultCache) {
			return this.fResultCache.get(key);
		}
	}

	public void putCachedResult(Object key, Object result) {
		putCachedResult(key, result, true);
	}

	public Object putCachedResult(Object key, Object result, boolean replace) {
		synchronized (this.fResultCache) {
			Object old= this.fResultCache.put(key, result);
			if (old != null && !replace) {
				this.fResultCache.put(key, old);
				return old;
			}
			return result;
		}
	}

	public void removeCachedResult(Object key) {
		synchronized (this.fResultCache) {
			this.fResultCache.remove(key);
		}
	}

	// For debugging lock issues
	static class DebugLockInfo {
		int fReadLocks;
		int fWriteLocks;
		List<StackTraceElement[]> fTraces= new ArrayList<>();

		public int addTrace() {
			this.fTraces.add(Thread.currentThread().getStackTrace());
			return this.fTraces.size();
		}

		@SuppressWarnings("nls")
		public void write(String threadName) {
			System.out.println("Thread: '" + threadName + "': " + this.fReadLocks + " readlocks, " + this.fWriteLocks + " writelocks");
			for (StackTraceElement[] trace : this.fTraces) {
				System.out.println("  Stacktrace:");
				for (StackTraceElement ste : trace) {
					System.out.println("    " + ste);
				}
			}
		}

		public void inc(DebugLockInfo val) {
			this.fReadLocks+= val.fReadLocks;
			this.fWriteLocks+= val.fWriteLocks;
			this.fTraces.addAll(val.fTraces);
		}
	}

	// For debugging lock issues
	private Map<Thread, DebugLockInfo> fLockDebugging;

	// For debugging lock issues
	private static DebugLockInfo getLockInfo(Map<Thread, DebugLockInfo> lockDebugging) {
		assert sDEBUG_LOCKS;

		Thread key = Thread.currentThread();
		DebugLockInfo result= lockDebugging.get(key);
		if (result == null) {
			result= new DebugLockInfo();
			lockDebugging.put(key, result);
		}
		return result;
	}

	// For debugging lock issues
	static void incReadLock(Map<Thread, DebugLockInfo> lockDebugging) {
		DebugLockInfo info = getLockInfo(lockDebugging);
		info.fReadLocks++;
		if (info.addTrace() > 10) {
			outputReadLocks(lockDebugging);
		}
	}

	// For debugging lock issues
	@SuppressWarnings("nls")
	static void decReadLock(Map<Thread, DebugLockInfo> lockDebugging) throws AssertionError {
		DebugLockInfo info = getLockInfo(lockDebugging);
		if (info.fReadLocks <= 0) {
			outputReadLocks(lockDebugging);
			throw new AssertionError("Superfluous releaseReadLock");
		}
		if (info.fWriteLocks != 0) {
			outputReadLocks(lockDebugging);
			throw new AssertionError("Releasing readlock while holding write lock");
		}
		if (--info.fReadLocks == 0) {
			lockDebugging.remove(Thread.currentThread());
		} else {
			info.addTrace();
		}
	}

	// For debugging lock issues
	@SuppressWarnings("nls")
	private void incWriteLock(int giveupReadLocks) throws AssertionError {
		DebugLockInfo info = getLockInfo(this.fLockDebugging);
		if (info.fReadLocks != giveupReadLocks) {
			outputReadLocks(this.fLockDebugging);
			throw new AssertionError("write lock with " + giveupReadLocks + " readlocks, expected " + info.fReadLocks);
		}
		if (info.fWriteLocks != 0)
			throw new AssertionError("Duplicate write lock");
		info.fWriteLocks++;
	}

	// For debugging lock issues
	private void decWriteLock(int establishReadLocks) throws AssertionError {
		DebugLockInfo info = getLockInfo(this.fLockDebugging);
		if (info.fReadLocks != establishReadLocks)
			throw new AssertionError("release write lock with " + establishReadLocks + " readlocks, expected " + info.fReadLocks); //$NON-NLS-1$ //$NON-NLS-2$
		if (info.fWriteLocks != 1)
			throw new AssertionError("Wrong release write lock"); //$NON-NLS-1$
		info.fWriteLocks= 0;
		if (info.fReadLocks == 0) {
			this.fLockDebugging.remove(Thread.currentThread());
		}
	}

	// For debugging lock issues
	@SuppressWarnings("nls")
	private long reportBlockedWriteLock(long start, int giveupReadLocks) {
		long now= System.currentTimeMillis();
		if (now >= start + BLOCKED_WRITE_LOCK_OUTPUT_INTERVAL) {
			System.out.println();
			System.out.println("Blocked writeLock");
			System.out.println("  lockcount= " + this.lockCount + ", giveupReadLocks=" + giveupReadLocks + ", waitingReaders=" + this.waitingReaders);
			outputReadLocks(this.fLockDebugging);
			start= now;
		}
		return start;
	}

	// For debugging lock issues
	@SuppressWarnings("nls")
	private static void outputReadLocks(Map<Thread, DebugLockInfo> lockDebugging) {
		System.out.println("---------------------  Lock Debugging -------------------------");
		for (Thread th: lockDebugging.keySet()) {
			DebugLockInfo info = lockDebugging.get(th);
			info.write(th.getName());
		}
		System.out.println("---------------------------------------------------------------");
	}

	// For debugging lock issues
	public void adjustThreadForReadLock(Map<Thread, DebugLockInfo> lockDebugging) {
		for (Thread th : lockDebugging.keySet()) {
			DebugLockInfo val= lockDebugging.get(th);
			if (val.fReadLocks > 0) {
				DebugLockInfo myval= this.fLockDebugging.get(th);
				if (myval == null) {
					myval= new DebugLockInfo();
					this.fLockDebugging.put(th, myval);
				}
				myval.inc(val);
				for (int i = 0; i < val.fReadLocks; i++) {
					decReadLock(this.fLockDebugging);
				}
			}
		}
	}

    public NdNode getNode(long address, short nodeType) throws IndexException {
    	return this.fNodeTypeRegistry.createNode(this, address, nodeType);
    }

    public <T extends NdNode> ITypeFactory<T> getTypeFactory(short nodeType) {
    	return this.fNodeTypeRegistry.getTypeFactory(nodeType);
    }

	/**
	 * Returns the type ID for the given class
	 */
	public short getNodeType(Class<? extends NdNode> toQuery) {
		return this.fNodeTypeRegistry.getTypeForClass(toQuery);
	}

	private void deleteIfUnreferenced(long address) {
		if (address == 0) {
			return;
		}
		short nodeType = NdNode.NODE_TYPE.get(this, address);

		// Look up the type
		ITypeFactory<? extends NdNode> factory1 = getTypeFactory(nodeType);

		if (factory1.isReadyForDeletion(this, address)) {
			// Call its destructor
			factory1.destruct(this, address);

			// Free up its memory
			getDB().free(address, (short)(Database.POOL_FIRST_NODE_TYPE + nodeType));
		}
	}

	public void delete(long address) {
		if (address == 0) {
			return;
		}
		short nodeType = NdNode.NODE_TYPE.get(this, address);

		// Look up the type
		ITypeFactory<? extends NdNode> factory1 = getTypeFactory(nodeType);

		// Call its destructor
		factory1.destruct(this, address);

		// Free up its memory
		getDB().free(address, (short)(Database.POOL_FIRST_NODE_TYPE + nodeType));

		// If this node was in the list of pending deletions, remove it since it's now been deleted
		if (this.pendingDeletions.containsKey(address)) {
			logDoubleDeletion(address);
			this.pendingDeletions.remove(address);
		}
	}

	public NdNodeTypeRegistry<NdNode> getTypeRegistry() {
		return this.fNodeTypeRegistry;
	}

	public void clear(IProgressMonitor monitor) {
		this.pendingDeletions.clear();
		getDB().clear(getDefaultVersion());
	}

	public boolean isValidAddress(long address) {
		return address > 0 && address < (long) getDB().getChunkCount() * Database.CHUNK_SIZE;
	}
}
