/*******************************************************************************
 * Copyright (c) 2008, 2024 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;

/** Reads a list of ICompilationUnit before actually needed (ahead) **/
public class ReadManager {
	private static final int CACHE_SIZE = 15; // do not waste memory by keeping too many files in memory
	/**
	 * Not more threads then cache size and leave 2 threads for compiler + writer. Executor should process in fifo order
	 * (first in first out).
	 */
	private static final ExecutorService READER_SERVICE = createExecutor(Math.max(0, Math.min(CACHE_SIZE, Runtime.getRuntime().availableProcessors() - 2)));

	private static ExecutorService createExecutor(int threadCount) {
		if (threadCount <= 0)
			return null;
		else {
			ThreadPoolExecutor executor = new ThreadPoolExecutor(threadCount, threadCount, /* keepAliveTime */ 5, TimeUnit.MINUTES,
					new LinkedBlockingQueue<>(), r -> {
						Thread t = new Thread(r, "Compiler Source File Reader"); //$NON-NLS-1$
						t.setDaemon(true);
						return t;
					});
			executor.allowCoreThreadTimeOut(true);
			return executor;
		}
	}

	private final Queue<ICompilationUnit> unitsToRead;
	private final Map<ICompilationUnit, Future<char[]>> cache = new ConcurrentHashMap<>();

	public ReadManager(ICompilationUnit[] files, int length) {
		this.unitsToRead = new ArrayDeque<>(length);
		if (READER_SERVICE == null) {
			return;
		}
		for (int l = 0; l < length; l++) {
			ICompilationUnit unit = files[l];
			this.unitsToRead.offer(unit);
		}
		while (queueNextReadAhead()) {
			// queued 1 more
		}
	}

	/** meant to called in the order of the initial supplied files **/
	public char[] getContents(ICompilationUnit unit) throws Error {
		if (READER_SERVICE == null) {
			return getWithoutExecutor(unit);
		}
		Future<char[]> future;
		synchronized (this) { // atomic remove from unitsToRead or cache
			future = this.cache.remove(unit);
			if (future == null) {
				// unit was not already scheduled
				// and does not need to be scheduled anymore
				this.unitsToRead.remove(unit);
			}
		}
		if (future == null) {
			// should not happen.
			return getWithoutFuture(unit);
		}
		// now: future != null
		queueNextReadAhead();
		try {
			// unit was already scheduled
			// in most cases future is already completed
			// Otherwise, when read ahead is slower then compiler,
			// wait for completion to avoid extra work of reading files multiple times:
			return getWithFuture(future);
		} catch (InterruptedException ignored) {
			return getWhileInterrupted(unit);
		} catch (ExecutionException e) {
			// rethrow the caught exception from the reading threads in the main compiler thread
			if (e.getCause() instanceof Error err) {
				throw err;
			}
			if (e.getCause() instanceof RuntimeException ex) {
				throw ex;
			}
			throw new RuntimeException(e);
		}
	}

	// distinct methods "getW*" with same content to make it possible to observe with method sampler which case took how long:
	private char[] getWithFuture(Future<char[]> future) throws InterruptedException, ExecutionException {
		// should happen in most cases
		return future.get();
	}

	private char[] getWithoutExecutor(ICompilationUnit unit) {
		// THREAD_COUNT==0 => no read ahead
		return unit.getContents();
	}

	private char[] getWithoutFuture(ICompilationUnit unit) {
		// should not happen
		return unit.getContents();
	}

	private char[] getWhileInterrupted(ICompilationUnit unit) {
		// should not happen
		return unit.getContents();
	}

	private boolean queueNextReadAhead() {
		if (this.cache.size() >= CACHE_SIZE) {
			return false;
		}
		synchronized (this) { // atomic move from unitsToRead to cache
			ICompilationUnit nextUnit = this.unitsToRead.poll();
			if (nextUnit == null) {
				return false;
			}
			Future<char[]> future = READER_SERVICE.submit(() -> readAhead(nextUnit));
			this.cache.put(nextUnit, future);
			return true;
		}
	}

	private char[] readAhead(ICompilationUnit unit) {
		queueNextReadAhead();
		return unit.getContents();
	}

	public void shutdown() {
		this.unitsToRead.clear();
		this.cache.clear();
	}
}