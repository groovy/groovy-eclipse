/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.Enumeration;
import java.util.Iterator;

import org.eclipse.jdt.internal.core.util.LRUCache;
import org.eclipse.jdt.internal.core.util.Messages;

/**
 *	The <code>OverflowingLRUCache</code> is an LRUCache which attempts
 *	to maintain a size equal or less than its <code>fSpaceLimit</code>
 *	by removing the least recently used elements.
 *
 *	<p>The cache will remove elements which successfully close and all
 *	elements which are explicitly removed.
 *
 *	<p>If the cache cannot remove enough old elements to add new elements
 *	it will grow beyond <code>fSpaceLimit</code>. Later, it will attempt to 
 *	shink back to the maximum space limit.
 *
 *	The method <code>close</code> should attempt to close the element.  If
 *	the element is successfully closed it will return true and the element will
 *	be removed from the cache.  Otherwise the element will remain in the cache.
 *
 *	<p>The cache implicitly attempts shrinks on calls to <code>put</code>and
 *	<code>setSpaceLimit</code>.  Explicitly calling the <code>shrink</code> method
 *	will also cause the cache to attempt to shrink.
 *
 *	<p>The cache calculates the used space of all elements which implement 
 *	<code>ILRUCacheable</code>.  All other elements are assumed to be of size one.
 *
 *	<p>Use the <code>#peek(Object)</code> and <code>#disableTimestamps()</code> method to
 *	circumvent the timestamp feature of the cache.  This feature is intended to be used
 *	only when the <code>#close(LRUCacheEntry)</code> method causes changes to the cache.  
 *	For example, if a parent closes its children when </code>#close(LRUCacheEntry)</code> is called, 
 *	it should be careful not to change the LRU linked list.  It can be sure it is not causing 
 *	problems by calling <code>#peek(Object)</code> instead of <code>#get(Object)</code> method.
 *	
 *	@see LRUCache
 */
public abstract class OverflowingLRUCache extends LRUCache {
	/**
	 * Indicates if the cache has been over filled and by how much.
	 */
	protected int fOverflow = 0;
	/**
	 *	Indicates whether or not timestamps should be updated
	 */
	protected boolean fTimestampsOn = true;
	/**
	 *	Indicates how much space should be reclaimed when the cache overflows.
	 *	Inital load factor of one third.
	 */
	protected double fLoadFactor = 0.333;
/**
 * Creates a OverflowingLRUCache. 
 * @param size Size limit of cache.
 */
public OverflowingLRUCache(int size) {
	this(size, 0);
}
/**
 * Creates a OverflowingLRUCache. 
 * @param size Size limit of cache.
 * @param overflow Size of the overflow.
 */
public OverflowingLRUCache(int size, int overflow) {
	super(size);
	fOverflow = overflow;
}
	/**
	 * Returns a new cache containing the same contents.
	 *
	 * @return New copy of this object.
	 */
	public Object clone() {
		
		OverflowingLRUCache newCache = (OverflowingLRUCache)newInstance(fSpaceLimit, fOverflow);
		LRUCacheEntry qEntry;
		
		/* Preserve order of entries by copying from oldest to newest */
		qEntry = this.fEntryQueueTail;
		while (qEntry != null) {
			newCache.privateAdd (qEntry._fKey, qEntry._fValue, qEntry._fSpace);
			qEntry = qEntry._fPrevious;
		}
		return newCache;
	}
/**
 * Returns true if the element is successfully closed and
 * removed from the cache, otherwise false.
 *
 * <p>NOTE: this triggers an external remove from the cache
 * by closing the obejct.
 *
 */
protected abstract boolean close(LRUCacheEntry entry);
	/**
	 *	Returns an enumerator of the values in the cache with the most
	 *	recently used first.
	 */
	public Enumeration elements() {
		if (fEntryQueue == null)
			return new LRUCacheEnumerator(null);
		LRUCacheEnumerator.LRUEnumeratorElement head = 
			new LRUCacheEnumerator.LRUEnumeratorElement(fEntryQueue._fValue);
		LRUCacheEntry currentEntry = fEntryQueue._fNext;
		LRUCacheEnumerator.LRUEnumeratorElement currentElement = head;
		while(currentEntry != null) {
			currentElement.fNext = new LRUCacheEnumerator.LRUEnumeratorElement(currentEntry._fValue);
			currentElement = currentElement.fNext;
			
			currentEntry = currentEntry._fNext;
		}
		return new LRUCacheEnumerator(head);
	}
	public double fillingRatio() {
		return (fCurrentSpace + fOverflow) * 100.0 / fSpaceLimit;
	}
	/**
	 * For internal testing only.
	 * This method exposed only for testing purposes!
	 *
	 * @return Hashtable of entries
	 */
	public java.util.Hashtable getEntryTable() {
		return fEntryTable;
	}
/**
 * Returns the load factor for the cache.  The load factor determines how 
 * much space is reclaimed when the cache exceeds its space limit.
 * @return double
 */
public double getLoadFactor() {
	return fLoadFactor;
}
	/**
	 *	@return The space by which the cache has overflown.
	 */
	public int getOverflow() {
		return fOverflow;
	}
	/**
	 * Ensures there is the specified amount of free space in the receiver,
	 * by removing old entries if necessary.  Returns true if the requested space was
	 * made available, false otherwise.  May not be able to free enough space
	 * since some elements cannot be removed until they are saved.
	 *
	 * @param space Amount of space to free up
	 */
	protected boolean makeSpace(int space) {
	
		int limit = fSpaceLimit;
		if (fOverflow == 0 && fCurrentSpace + space <= limit) {
			/* if space is already available */
			return true;
		}
	
		/* Free up space by removing oldest entries */
		int spaceNeeded = (int)((1 - fLoadFactor) * limit);
		spaceNeeded = (spaceNeeded > space) ? spaceNeeded : space;
		LRUCacheEntry entry = fEntryQueueTail;
	
		try {
			// disable timestamps update while making space so that the previous and next links are not changed
			// (by a call to get(Object) for example)
			fTimestampsOn = false;
			
			while (fCurrentSpace + spaceNeeded > limit && entry != null) {
				this.privateRemoveEntry(entry, false, false);
				entry = entry._fPrevious;
			}
		} finally {
			fTimestampsOn = true;
		}
	
		/* check again, since we may have aquired enough space */
		if (fCurrentSpace + space <= limit) {
			fOverflow = 0;
			return true;
		}
	
		/* update fOverflow */
		fOverflow = fCurrentSpace + space - limit;
		return false;
	}
	/**
	 * Returns a new instance of the reciever.
	 */
	protected abstract LRUCache newInstance(int size, int overflow);
/**
 * For testing purposes only
 */
public void printStats() {
	int forwardListLength = 0;
	LRUCacheEntry entry = fEntryQueue;
	while(entry != null) {
		forwardListLength++;
		entry = entry._fNext;
	}
	System.out.println("Forward length: " + forwardListLength); //$NON-NLS-1$
	
	int backwardListLength = 0;
	entry = fEntryQueueTail;
	while(entry != null) {
		backwardListLength++;
		entry = entry._fPrevious;
	}
	System.out.println("Backward length: " + backwardListLength); //$NON-NLS-1$

	Enumeration keys = fEntryTable.keys();
	class Temp {
		public Class fClass;
		public int fCount;
		public Temp(Class aClass) {
			fClass = aClass;
			fCount = 1;
		}
		public String toString() {
			return "Class: " + fClass + " has " + fCount + " entries."; //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-1$
		}
	}
	java.util.HashMap h = new java.util.HashMap();
	while(keys.hasMoreElements()) {
		entry = (LRUCacheEntry)fEntryTable.get(keys.nextElement());
		Class key = entry._fValue.getClass();
		Temp t = (Temp)h.get(key);
		if (t == null) {
			h.put(key, new Temp(key));
		} else {
			t.fCount++;
		}
	}

	for (Iterator iter = h.values().iterator(); iter.hasNext();){
		System.out.println(iter.next());
	}
}
	/**
	 *	Removes the entry from the entry queue.
	 *	Calls <code>privateRemoveEntry</code> with the external functionality enabled.
	 *
	 * @param shuffle indicates whether we are just shuffling the queue 
	 * (in which case, the entry table is not modified).
	 */
	protected void privateRemoveEntry (LRUCacheEntry entry, boolean shuffle) {
		privateRemoveEntry(entry, shuffle, true);
	}
/**
 *	Removes the entry from the entry queue.  If <i>external</i> is true, the entry is removed
 *	without checking if it can be removed.  It is assumed that the client has already closed
 *	the element it is trying to remove (or will close it promptly).
 *
 *	If <i>external</i> is false, and the entry could not be closed, it is not removed and the 
 *	pointers are not changed.
 *
 *	@param shuffle indicates whether we are just shuffling the queue 
 *	(in which case, the entry table is not modified).
 */
protected void privateRemoveEntry(LRUCacheEntry entry, boolean shuffle, boolean external) {

	if (!shuffle) {
		if (external) {
			fEntryTable.remove(entry._fKey);			
			fCurrentSpace -= entry._fSpace;
		} else {
			if (!close(entry)) return;
			// buffer close will recursively call #privateRemoveEntry with external==true
			// thus entry will already be removed if reaching this point.
			if (fEntryTable.get(entry._fKey) == null){
				return;
			} else {
				// basic removal
				fEntryTable.remove(entry._fKey);			
				fCurrentSpace -= entry._fSpace;
			}
		}
	}
	LRUCacheEntry previous = entry._fPrevious;
	LRUCacheEntry next = entry._fNext;
		
	/* if this was the first entry */
	if (previous == null) {
		fEntryQueue = next;
	} else {
		previous._fNext = next;
	}
	/* if this was the last entry */
	if (next == null) {
		fEntryQueueTail = previous;
	} else {
		next._fPrevious = previous;
	}
}
	/**
	 * Sets the value in the cache at the given key. Returns the value.
	 *
	 * @param key Key of object to add.
	 * @param value Value of object to add.
	 * @return added value.
	 */
	public Object put(Object key, Object value) {
		/* attempt to rid ourselves of the overflow, if there is any */
		if (fOverflow > 0)
			shrink();
			
		/* Check whether there's an entry in the cache */
		int newSpace = spaceFor(value);
		LRUCacheEntry entry = (LRUCacheEntry) fEntryTable.get (key);
		
		if (entry != null) {
			
			/**
			 * Replace the entry in the cache if it would not overflow
			 * the cache.  Otherwise flush the entry and re-add it so as 
			 * to keep cache within budget
			 */
			int oldSpace = entry._fSpace;
			int newTotal = fCurrentSpace - oldSpace + newSpace;
			if (newTotal <= fSpaceLimit) {
				updateTimestamp (entry);
				entry._fValue = value;
				entry._fSpace = newSpace;
				fCurrentSpace = newTotal;
				fOverflow = 0;
				return value;
			} else {
				privateRemoveEntry (entry, false, false);
			}
		}
		
		// attempt to make new space
		makeSpace(newSpace);
		
		// add without worring about space, it will
		// be handled later in a makeSpace call
		privateAdd (key, value, newSpace);
		
		return value;
	}
	/**
	 * Removes and returns the value in the cache for the given key.
	 * If the key is not in the cache, returns null.
	 *
	 * @param key Key of object to remove from cache.
	 * @return Value removed from cache.
	 */
	public Object remove(Object key) {
		return removeKey(key);
	}
/**
 * Sets the load factor for the cache.  The load factor determines how 
 * much space is reclaimed when the cache exceeds its space limit.
 * @param newLoadFactor double
 * @throws IllegalArgumentException when the new load factor is not in (0.0, 1.0]
 */
public void setLoadFactor(double newLoadFactor) throws IllegalArgumentException {
	if(newLoadFactor <= 1.0 && newLoadFactor > 0.0)
		fLoadFactor = newLoadFactor;
	else
		throw new IllegalArgumentException(Messages.cache_invalidLoadFactor); 
}
	/**
	 * Sets the maximum amount of space that the cache can store
	 *
	 * @param limit Number of units of cache space
	 */
	public void setSpaceLimit(int limit) {
		if (limit < fSpaceLimit) {
			makeSpace(fSpaceLimit - limit);
		}
		fSpaceLimit = limit;
	}
	/**
	 * Attempts to shrink the cache if it has overflown.
	 * Returns true if the cache shrinks to less than or equal to <code>fSpaceLimit</code>.
	 */
	public boolean shrink() {
		if (fOverflow > 0)
			return makeSpace(0);
		return true;
	}
/**
 * Returns a String that represents the value of this object.  This method
 * is for debugging purposes only.
 */
public String toString() {
	return 
		toStringFillingRation("OverflowingLRUCache ") + //$NON-NLS-1$
		toStringContents();
}
/**
 * Updates the timestamp for the given entry, ensuring that the queue is 
 * kept in correct order.  The entry must exist.
 *
 * <p>This method will do nothing if timestamps have been disabled.
 */
protected void updateTimestamp(LRUCacheEntry entry) {
	if (fTimestampsOn) {
		entry._fTimestamp = fTimestampCounter++;
		if (fEntryQueue != entry) {
			this.privateRemoveEntry(entry, true);
			this.privateAddEntry(entry, true);
		}
	}
}
}
