/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.util;

import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * The <code>LRUCache</code> is a hashtable that stores a finite number of elements.  
 * When an attempt is made to add values to a full cache, the least recently used values
 * in the cache are discarded to make room for the new values as necessary.
 * 
 * <p>The data structure is based on the LRU virtual memory paging scheme.
 * 
 * <p>Objects can take up a variable amount of cache space by implementing
 * the <code>ILRUCacheable</code> interface.
 *
 * <p>This implementation is NOT thread-safe.  Synchronization wrappers would
 * have to be added to ensure atomic insertions and deletions from the cache.
 *
 * @see org.eclipse.jdt.internal.core.util.ILRUCacheable
 */
public class LRUCache implements Cloneable {

	/**
	 * This type is used internally by the LRUCache to represent entries 
	 * stored in the cache.
	 * It is static because it does not require a pointer to the cache
	 * which contains it.
	 *
	 * @see LRUCache
	 */
	protected static class LRUCacheEntry {
		
		/**
		 * Hash table key
		 */
		public Object _fKey;
		 
		/**
		 * Hash table value (an LRUCacheEntry object)
		 */
		public Object _fValue;		 

		/**
		 * Time value for queue sorting
		 */
		public int _fTimestamp;
		
		/**
		 * Cache footprint of this entry
		 */
		public int _fSpace;
		
		/**
		 * Previous entry in queue
		 */
		public LRUCacheEntry _fPrevious;
			
		/**
		 * Next entry in queue
		 */
		public LRUCacheEntry _fNext;
			
		/**
		 * Creates a new instance of the receiver with the provided values
		 * for key, value, and space.
		 */
		public LRUCacheEntry (Object key, Object value, int space) {
			_fKey = key;
			_fValue = value;
			_fSpace = space;
		}

		/**
		 * Returns a String that represents the value of this object.
		 */
		public String toString() {

			return "LRUCacheEntry [" + _fKey + "-->" + _fValue + "]"; //$NON-NLS-3$ //$NON-NLS-1$ //$NON-NLS-2$
		}
	}	

	public class Stats {
		private int[] counters = new int[20];
		private long[] timestamps = new long[20];
		private int counterIndex = -1;
		
		private void add(int counter) {
			for (int i = 0; i <= this.counterIndex; i++) {
				if (this.counters[i] == counter)
					return;
			}
			int length = this.counters.length;
			if (++this.counterIndex == length) {
				int newLength = this.counters.length * 2;
				System.arraycopy(this.counters, 0, this.counters = new int[newLength], 0, length);
				System.arraycopy(this.timestamps, 0, this.timestamps = new long[newLength], 0, length);
			}
			this.counters[this.counterIndex] = counter;
			this.timestamps[this.counterIndex] = System.currentTimeMillis();
		}
		private String getAverageAge(long totalTime, int numberOfElements, long currentTime) {
			if (numberOfElements == 0)
				return "N/A"; //$NON-NLS-1$
			long time = totalTime / numberOfElements;
			long age = currentTime - time;
			long ageInSeconds = age/1000;
			int seconds = 0;
			int minutes = 0;
			int hours = 0;
			int days = 0;
			if (ageInSeconds > 60) {
				long ageInMin = ageInSeconds / 60;
				seconds = (int) (ageInSeconds - (60 * ageInMin));
				if (ageInMin > 60) {
					long ageInHours = ageInMin / 60;
					minutes = (int) (ageInMin - (60 * ageInHours));
					if (ageInHours > 24) {
						long ageInDays = ageInHours / 24;
						hours = (int) (ageInHours - (24 * ageInDays));
						days = (int) ageInDays;
					} else {
						hours = (int) ageInHours;
					}
				} else {
					minutes = (int) ageInMin;
				}
			} else {
				seconds = (int) ageInSeconds;
			}
			StringBuffer buffer = new StringBuffer();
			if (days > 0) {
				buffer.append(days);
				buffer.append(" days "); //$NON-NLS-1$
			}
			if (hours > 0) {
				buffer.append(hours);
				buffer.append(" hours "); //$NON-NLS-1$
			}
			if (minutes > 0) {
				buffer.append(minutes);
				buffer.append(" minutes "); //$NON-NLS-1$
			}
			buffer.append(seconds);
			buffer.append(" seconds"); //$NON-NLS-1$
			return buffer.toString();
		}
		private long getTimestamps(int counter) {
			for (int i = 0; i <= this.counterIndex; i++) {
				if (this.counters[i] >= counter)
					return this.timestamps[i];
			}
			return -1;
		}
		public synchronized String printStats() {
			int numberOfElements = LRUCache.this.fCurrentSpace;
			if (numberOfElements == 0) {
				return "No elements in cache"; //$NON-NLS-1$
			}
			StringBuffer buffer = new StringBuffer();
			
			buffer.append("Number of elements in cache: "); //$NON-NLS-1$
			buffer.append(numberOfElements);
			
			final int numberOfGroups = 5;
			int numberOfElementsPerGroup = numberOfElements / numberOfGroups;
			buffer.append("\n("); //$NON-NLS-1$
			buffer.append(numberOfGroups);
			buffer.append(" groups of "); //$NON-NLS-1$
			buffer.append(numberOfElementsPerGroup);
			buffer.append(" elements)"); //$NON-NLS-1$
			buffer.append("\n\nAverage age:"); //$NON-NLS-1$
			int groupNumber = 1;
			int elementCounter = 0;
			LRUCacheEntry entry = LRUCache.this.fEntryQueueTail;
			long currentTime = System.currentTimeMillis();
			long accumulatedTime = 0;
			while (entry != null) {
				long timeStamps = getTimestamps(entry._fTimestamp);
				if (timeStamps > 0) {
					accumulatedTime += timeStamps;
					elementCounter++;
				}
				if (elementCounter >= numberOfElementsPerGroup && (groupNumber < numberOfGroups)) {
					buffer.append("\nGroup "); //$NON-NLS-1$
					buffer.append(groupNumber);
					if (groupNumber == 1) {
						buffer.append(" (oldest)\t: "); //$NON-NLS-1$
					} else {
						buffer.append("\t\t: "); //$NON-NLS-1$
					}
					groupNumber++;
					buffer.append(getAverageAge(accumulatedTime, elementCounter, currentTime));
					elementCounter = 0;
					accumulatedTime = 0;
				}
				entry = entry._fPrevious;
			}
			buffer.append("\nGroup "); //$NON-NLS-1$
			buffer.append(numberOfGroups);
			buffer.append(" (youngest)\t: "); //$NON-NLS-1$
			buffer.append(getAverageAge(accumulatedTime, elementCounter, currentTime));
			
			return buffer.toString();
		}
		private void removeCountersOlderThan(int counter) {
			for (int i = 0; i <= this.counterIndex; i++) {
				if (this.counters[i] >= counter) {
					if (i > 0) {
						int length = this.counterIndex-i+1;
						System.arraycopy(this.counters, i, this.counters, 0, length);
						System.arraycopy(this.timestamps, i, this.timestamps, 0, length);
						this.counterIndex = length;
					}
					return;
				}
			}
		}
		public Object getOldestElement() {
			return LRUCache.this.getOldestElement();
		}
		public long getOldestTimestamps() {
			return getTimestamps(getOldestTimestampCounter());
		}
		public synchronized void snapshot() {
			removeCountersOlderThan(getOldestTimestampCounter());
			add(getNewestTimestampCounter());
		}
	}
	
	/**
	 * Amount of cache space used so far
	 */
	protected int fCurrentSpace;
	
	/**
	 * Maximum space allowed in cache
	 */
	protected int fSpaceLimit;
	
	/**
	 * Counter for handing out sequential timestamps
	 */
	protected int	fTimestampCounter;
	
	/**
	 * Hash table for fast random access to cache entries
	 */
	protected Hashtable fEntryTable;

	/**
	 * Start of queue (most recently used entry) 
	 */	
	protected LRUCacheEntry fEntryQueue;

	/**
	 * End of queue (least recently used entry)
	 */	
	protected LRUCacheEntry fEntryQueueTail;
		
	/**
	 * Default amount of space in the cache
	 */
	protected static final int DEFAULT_SPACELIMIT = 100;
	/**
	 * Creates a new cache.  Size of cache is defined by 
	 * <code>DEFAULT_SPACELIMIT</code>.
	 */
	public LRUCache() {
		
		this(DEFAULT_SPACELIMIT);
	}
	/**
	 * Creates a new cache.
	 * @param size Size of Cache
	 */
	public LRUCache(int size) {
		
		fTimestampCounter = fCurrentSpace = 0;
		fEntryQueue = fEntryQueueTail = null;
		fEntryTable = new Hashtable(size);
		fSpaceLimit = size;
	}
	/**
	 * Returns a new cache containing the same contents.
	 *
	 * @return New copy of object.
	 */
	public Object clone() {
		
		LRUCache newCache = newInstance(fSpaceLimit);
		LRUCacheEntry qEntry;
		
		/* Preserve order of entries by copying from oldest to newest */
		qEntry = this.fEntryQueueTail;
		while (qEntry != null) {
			newCache.privateAdd (qEntry._fKey, qEntry._fValue, qEntry._fSpace);
			qEntry = qEntry._fPrevious;
		}
		return newCache;
	}
	public double fillingRatio() {
		return (fCurrentSpace) * 100.0 / fSpaceLimit;
	}
	/**
	 * Flushes all entries from the cache.
	 */
	public void flush() {

		fCurrentSpace = 0;
		LRUCacheEntry entry = fEntryQueueTail; // Remember last entry
		fEntryTable = new Hashtable();  // Clear it out
		fEntryQueue = fEntryQueueTail = null;  
		while (entry != null) {  // send deletion notifications in LRU order
			entry = entry._fPrevious;
		}
	}
	/**
	 * Flushes the given entry from the cache.  Does nothing if entry does not
	 * exist in cache.
	 *
	 * @param key Key of object to flush
	 */
	public void flush (Object key) {
		
		LRUCacheEntry entry;
		
		entry = (LRUCacheEntry) fEntryTable.get(key);

		/* If entry does not exist, return */
		if (entry == null) return;

		this.privateRemoveEntry (entry, false);
	}
	/*
	 * Answers the existing key that is equals to the given key.
	 * If the key is not in the cache, returns the given key
	 */
	public Object getKey(Object key) {
		LRUCacheEntry entry = (LRUCacheEntry) fEntryTable.get(key);
		if (entry == null) {
			return key;
		}
		return entry._fKey;
	}
	/**
	 * Answers the value in the cache at the given key.
	 * If the value is not in the cache, returns null
	 *
	 * @param key Hash table key of object to retrieve
	 * @return Retreived object, or null if object does not exist
	 */
	public Object get(Object key) {
		
		LRUCacheEntry entry = (LRUCacheEntry) fEntryTable.get(key);
		if (entry == null) {
			return null;
		}
		
		this.updateTimestamp (entry);
		return entry._fValue;
	}
	/**
	 * Returns the amount of space that is current used in the cache.
	 */
	public int getCurrentSpace() {
		return fCurrentSpace;
	}
	/**
	 * Returns the timestamps of the most recently used element in the cache.
	 */
	public int getNewestTimestampCounter() {
		return this.fEntryQueue == null ? 0 : this.fEntryQueue._fTimestamp;
	}
	/**
	 * Returns the timestamps of the least recently used element in the cache.
	 */
	public int getOldestTimestampCounter() {
		return this.fEntryQueueTail == null ? 0 : this.fEntryQueueTail._fTimestamp;
	}
	/**
	 * Returns the lest recently used element in the cache
	 */
	public Object getOldestElement() {
		return this.fEntryQueueTail == null ? null : this.fEntryQueueTail._fKey;
	}
	/**
	 * Returns the maximum amount of space available in the cache.
	 */
	public int getSpaceLimit() {
		return fSpaceLimit;
	}
	/**
	 * Returns an Enumeration of the keys currently in the cache.
	 */
	public Enumeration keys() {
		
		return fEntryTable.keys();
	}
	/**
	 * Returns an enumeration that iterates over all the keys and values 
	 * currently in the cache.
	 */
	public ICacheEnumeration keysAndValues() {
		return new ICacheEnumeration() {
		
			Enumeration fValues = fEntryTable.elements();
			LRUCacheEntry fEntry;
			
			public boolean hasMoreElements() {
				return fValues.hasMoreElements();
			}
			
			public Object nextElement() {
				fEntry = (LRUCacheEntry) fValues.nextElement();
				return fEntry._fKey;
			}
			
			public Object getValue() {
				if (fEntry == null) {
					throw new java.util.NoSuchElementException();
				}
				return fEntry._fValue;
			}
		};
	}
	/**
	 * Ensures there is the specified amount of free space in the receiver,
	 * by removing old entries if necessary.  Returns true if the requested space was
	 * made available, false otherwise.
	 *
	 * @param space Amount of space to free up
	 */
	protected boolean makeSpace (int space) {
		
		int limit;
		
		limit = this.getSpaceLimit();
		
		/* if space is already available */
		if (fCurrentSpace + space <= limit) {
			return true;
		}
		
		/* if entry is too big for cache */
		if (space > limit) {
			return false;
		}
		
		/* Free up space by removing oldest entries */
		while (fCurrentSpace + space > limit && fEntryQueueTail != null) {
			this.privateRemoveEntry (fEntryQueueTail, false);
		}
		return true;
	}
	/**
	 * Returns a new LRUCache instance
	 */
	protected LRUCache newInstance(int size) {
		return new LRUCache(size);
	}
	/**
	 * Answers the value in the cache at the given key.
	 * If the value is not in the cache, returns null
	 *
	 * This function does not modify timestamps.
	 */
	public Object peek(Object key) {
		
		LRUCacheEntry entry = (LRUCacheEntry) fEntryTable.get(key);
		if (entry == null) {
			return null;
		}
		return entry._fValue;
	}
	/**
	 * Adds an entry for the given key/value/space.
	 */
	protected void privateAdd (Object key, Object value, int space) {
		
		LRUCacheEntry entry;
		
		entry = new LRUCacheEntry(key, value, space);
		this.privateAddEntry (entry, false);
	}
	/**
	 * Adds the given entry from the receiver.
	 * @param shuffle Indicates whether we are just shuffling the queue 
	 * (in which case, the entry table is not modified).
	 */
	protected void privateAddEntry (LRUCacheEntry entry, boolean shuffle) {
		
		if (!shuffle) {
			fEntryTable.put (entry._fKey, entry);
			fCurrentSpace += entry._fSpace;
		}
		
		entry._fTimestamp = fTimestampCounter++;
		entry._fNext = this.fEntryQueue;
		entry._fPrevious = null;
		
		if (fEntryQueue == null) {
			/* this is the first and last entry */
			fEntryQueueTail = entry;
		} else {
			fEntryQueue._fPrevious = entry;
		}
		
		fEntryQueue = entry;
	}
	/**
	 * Removes the entry from the entry queue.  
	 * @param shuffle indicates whether we are just shuffling the queue 
	 * (in which case, the entry table is not modified).
	 */
	protected void privateRemoveEntry (LRUCacheEntry entry, boolean shuffle) {
		
		LRUCacheEntry previous, next;
		
		previous = entry._fPrevious;
		next = entry._fNext;
		
		if (!shuffle) {
			fEntryTable.remove(entry._fKey);
			fCurrentSpace -= entry._fSpace;
		}

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
		
		int newSpace, oldSpace, newTotal;
		LRUCacheEntry entry;
		
		/* Check whether there's an entry in the cache */
		newSpace = spaceFor(value);
		entry = (LRUCacheEntry) fEntryTable.get (key);
		
		if (entry != null) {
			
			/**
			 * Replace the entry in the cache if it would not overflow
			 * the cache.  Otherwise flush the entry and re-add it so as 
			 * to keep cache within budget
			 */
			oldSpace = entry._fSpace;
			newTotal = getCurrentSpace() - oldSpace + newSpace;
			if (newTotal <= getSpaceLimit()) {
				updateTimestamp (entry);
				entry._fValue = value;
				entry._fSpace = newSpace;
				this.fCurrentSpace = newTotal;
				return value;
			} else {
				privateRemoveEntry (entry, false);
			}
		}
		if (makeSpace(newSpace)) {
			privateAdd (key, value, newSpace);
		}
		return value;
	}
	/**
	 * Removes and returns the value in the cache for the given key.
	 * If the key is not in the cache, returns null.
	 *
	 * @param key Key of object to remove from cache.
	 * @return Value removed from cache.
	 */
	public Object removeKey (Object key) {
		
		LRUCacheEntry entry = (LRUCacheEntry) fEntryTable.get(key);
		if (entry == null) {
			return null;
		}
		Object value = entry._fValue;
		this.privateRemoveEntry (entry, false);
		return value;
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
	 * Returns the space taken by the given value.
	 */
	protected int spaceFor (Object value) {
		
		if (value instanceof ILRUCacheable) {
			return ((ILRUCacheable) value).getCacheFootprint();
		} else {
			return 1;
		}
	}
	/**
	 * Returns a String that represents the value of this object.  This method
	 * is for debugging purposes only.
	 */
	public String toString() {
		return 
			toStringFillingRation("LRUCache") + //$NON-NLS-1$
			toStringContents();
	}
	
	/**
	 * Returns a String that represents the contents of this object.  This method
	 * is for debugging purposes only.
	 */
	protected String toStringContents() {
		StringBuffer result = new StringBuffer();
		int length = fEntryTable.size();
		Object[] unsortedKeys = new Object[length];
		String[] unsortedToStrings = new String[length];
		Enumeration e = this.keys();
		for (int i = 0; i < length; i++) {
			Object key = e.nextElement();
			unsortedKeys[i] = key;
			unsortedToStrings[i] = 
				(key instanceof org.eclipse.jdt.internal.core.JavaElement) ?
					((org.eclipse.jdt.internal.core.JavaElement)key).getElementName() :
					key.toString();
		}
		ToStringSorter sorter = new ToStringSorter();
		sorter.sort(unsortedKeys, unsortedToStrings);
		for (int i = 0; i < length; i++) {
			String toString = sorter.sortedStrings[i];
			Object value = this.get(sorter.sortedObjects[i]);
			result.append(toString);		
			result.append(" -> "); //$NON-NLS-1$
			result.append(value);
			result.append("\n"); //$NON-NLS-1$
		}
		return result.toString();
	}
	
	public String toStringFillingRation(String cacheName) {
		StringBuffer buffer = new StringBuffer(cacheName);
		buffer.append('[');
		buffer.append(getSpaceLimit());
		buffer.append("]: "); //$NON-NLS-1$
		buffer.append(NumberFormat.getInstance().format(fillingRatio()));
		buffer.append("% full"); //$NON-NLS-1$
		return buffer.toString();
	}

	/**
	 * Updates the timestamp for the given entry, ensuring that the queue is 
	 * kept in correct order.  The entry must exist
	 */
	protected void updateTimestamp (LRUCacheEntry entry) {
		
		entry._fTimestamp = fTimestampCounter++;
		if (fEntryQueue != entry) {
			this.privateRemoveEntry (entry, true);
			this.privateAddEntry (entry, true);
		}
		return;
	}
}
