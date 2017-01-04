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

import java.util.Enumeration;

/**
 *	The <code>LRUCacheEnumerator</code> returns its elements in
 *	the order they are found in the <code>LRUCache</code>, with the
 *	most recent elements first.
 *
 *	Once the enumerator is created, elements which are later added
 *	to the cache are not returned by the enumerator.  However,
 *	elements returned from the enumerator could have been closed
 *	by the cache.
 */
@SuppressWarnings("rawtypes")
public class LRUCacheEnumerator implements Enumeration {
	/**
	 *	Current element;
	 */
	protected LRUEnumeratorElement elementQueue;

	public static class LRUEnumeratorElement {
		/**
		 *	Value returned by <code>nextElement()</code>;
		 */
		public Object value;

		/**
		 *	Next element
		 */
		public LRUEnumeratorElement next;

		/**
		 * Constructor
		 */
		public LRUEnumeratorElement(Object value) {
			this.value = value;
		}
	}
/**
 *	Creates a CacheEnumerator on the list of <code>LRUEnumeratorElements</code>.
 */
public LRUCacheEnumerator(LRUEnumeratorElement firstElement) {
	this.elementQueue = firstElement;
}
/**
 * Returns true if more elements exist.
 */
public boolean hasMoreElements() {
	return this.elementQueue != null;
}
/**
 * Returns the next element.
 */
public Object nextElement() {
	Object temp = this.elementQueue.value;
	this.elementQueue = this.elementQueue.next;
	return temp;
}
}
