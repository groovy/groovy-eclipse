/*******************************************************************************
 * Copyright (c) 2017 Google, Inc and others.
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
package org.eclipse.jdt.internal.core.nd.indexer;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.internal.core.nd.Nd;

/**
 * Holds a cache that maps filenames (absolute paths on the local filesystem) to "up to date" states.
 * A file is "up to date" if its content is known to be in sync with the index. A file
 * is not up to date if there is any possibility that its content might be out of sync with the
 * index.
 */
public class FileStateCache {
	private final Map<String, Boolean> fileStateCache = new HashMap<>();

	/**
	 * Returns true if the file at the given path is in sync with the index. Returns false if the file has already
	 * been tested and might be out-of-sync. Returns null if its status is unknown and needs to be tested.
	 *
	 * @param location an absolute path on the filesystem
	 */
	public Boolean isUpToDate(String location) {
		synchronized (this.fileStateCache) {
			return this.fileStateCache.get(location);
		}
	}

	/**
	 * Returns the cache for the given {@link Nd} instance.
	 *
	 * @return the cache for the given {@link Nd}. Creates one if it doesn't exist yet.
	 */
	public static FileStateCache getCache(Nd nd) {
		return nd.getData(FileStateCache.class, FileStateCache::create);
	}

	/**
	 * Creates a new instance of {@link FileStateCache}.
	 */
	private static FileStateCache create() {
		return new FileStateCache();
	}

	/**
	 * Inserts a new entry into the cache.
	 * 
	 * @param location absolute filesystem path to the file
	 * @param result true if the file is definitely in sync with the index, false if there is any possibility of it
	 * being out of sync.
	 */
	public void put(String location, boolean result) {
		synchronized (this.fileStateCache) {
			this.fileStateCache.put(location, result);
		}
	}

	/**
	 * Clears the entire cache.
	 */
	public void clear() {
		synchronized (this.fileStateCache) {
			this.fileStateCache.clear();
		}
	}

	/**
	 * Removes a single entry from the cache.
	 * 
	 * @param location absolute filesystem path to the file.
	 */
	public void remove(String location) {
		synchronized (this.fileStateCache) {
			this.fileStateCache.remove(location);
		}
	}
}
