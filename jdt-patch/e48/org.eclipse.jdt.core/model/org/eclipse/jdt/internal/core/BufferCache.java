/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.ArrayList;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.internal.core.util.LRUCache;

/**
 * An LRU cache of <code>IBuffers</code>.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class BufferCache extends OverflowingLRUCache {

	private ThreadLocal buffersToClose = new ThreadLocal();
/**
 * Constructs a new buffer cache of the given size.
 */
public BufferCache(int size) {
	super(size);
}
/**
 * Constructs a new buffer cache of the given size.
 */
public BufferCache(int size, int overflow) {
	super(size, overflow);
}
/**
 * Returns true if the buffer is successfully closed and
 * removed from the cache, otherwise false.
 *
 * <p>NOTE: this triggers an external removal of this buffer
 * by closing the buffer.
 */
protected boolean close(LRUCacheEntry entry) {
	IBuffer buffer= (IBuffer) entry.value;

	// prevent buffer that have unsaved changes or working copy buffer to be removed
	// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=39311
	if (!((Openable)buffer.getOwner()).canBufferBeRemovedFromCache(buffer)) {
		return false;
	} else {
		ArrayList buffers = (ArrayList) this.buffersToClose.get();
		if (buffers == null) {
			buffers = new ArrayList();
			this.buffersToClose.set(buffers);
		}
		buffers.add(buffer);
		return true;
	}
}

void closeBuffers() {
	ArrayList buffers = (ArrayList) this.buffersToClose.get();
	if (buffers == null)
		return;
	this.buffersToClose.set(null);
	for (int i = 0, length = buffers.size(); i < length; i++) {
		((IBuffer) buffers.get(i)).close();
	}
}
	/**
	 * Returns a new instance of the reciever.
	 */
	protected LRUCache newInstance(int size, int newOverflow) {
		return new BufferCache(size, newOverflow);
	}
}
