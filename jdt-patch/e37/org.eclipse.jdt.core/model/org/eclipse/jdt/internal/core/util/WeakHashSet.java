/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.util;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * A hashset whose values can be garbage collected.
 */
public class WeakHashSet {

	public static class HashableWeakReference extends WeakReference {
		public int hashCode;
		public HashableWeakReference(Object referent, ReferenceQueue queue) {
			super(referent, queue);
			this.hashCode = referent.hashCode();
		}
		public boolean equals(Object obj) {
			if (!(obj instanceof HashableWeakReference)) return false;
			Object referent = get();
			Object other = ((HashableWeakReference) obj).get();
			if (referent == null) return other == null;
			return referent.equals(other);
		}
		public int hashCode() {
			return this.hashCode;
		}
		public String toString() {
			Object referent = get();
			if (referent == null) return "[hashCode=" + this.hashCode + "] <referent was garbage collected>"; //$NON-NLS-1$  //$NON-NLS-2$
			return "[hashCode=" + this.hashCode + "] " + referent.toString(); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	HashableWeakReference[] values;
	public int elementSize; // number of elements in the table
	int threshold;
	ReferenceQueue referenceQueue = new ReferenceQueue();

	public WeakHashSet() {
		this(5);
	}

	public WeakHashSet(int size) {
		this.elementSize = 0;
		this.threshold = size; // size represents the expected number of elements
		int extraRoom = (int) (size * 1.75f);
		if (this.threshold == extraRoom)
			extraRoom++;
		this.values = new HashableWeakReference[extraRoom];
	}

	/*
	 * Adds the given object to this set.
	 * If an object that is equals to the given object already exists, do nothing.
	 * Returns the existing object or the new object if not found.
	 */
	public Object add(Object obj) {
		cleanupGarbageCollectedValues();
		int valuesLength = this.values.length,
			index = (obj.hashCode() & 0x7FFFFFFF) % valuesLength;
		HashableWeakReference currentValue;
		while ((currentValue = this.values[index]) != null) {
			Object referent;
			if (obj.equals(referent = currentValue.get())) {
				return referent;
			}
			if (++index == valuesLength) {
				index = 0;
			}
		}
		this.values[index] = new HashableWeakReference(obj, this.referenceQueue);

		// assumes the threshold is never equal to the size of the table
		if (++this.elementSize > this.threshold)
			rehash();

		return obj;
	}

	private void addValue(HashableWeakReference value) {
		Object obj = value.get();
		if (obj == null) return;
		int valuesLength = this.values.length;
		int index = (value.hashCode & 0x7FFFFFFF) % valuesLength;
		HashableWeakReference currentValue;
		while ((currentValue = this.values[index]) != null) {
			if (obj.equals(currentValue.get())) {
				return;
			}
			if (++index == valuesLength) {
				index = 0;
			}
		}
		this.values[index] = value;

		// assumes the threshold is never equal to the size of the table
		if (++this.elementSize > this.threshold)
			rehash();
	}

	private void cleanupGarbageCollectedValues() {
		HashableWeakReference toBeRemoved;
		while ((toBeRemoved = (HashableWeakReference) this.referenceQueue.poll()) != null) {
			int hashCode = toBeRemoved.hashCode;
			int valuesLength = this.values.length;
			int index = (hashCode & 0x7FFFFFFF) % valuesLength;
			HashableWeakReference currentValue;
			while ((currentValue = this.values[index]) != null) {
				if (currentValue == toBeRemoved) {
					// replace the value at index with the last value with the same hash
					int sameHash = index;
					int current;
					while ((currentValue = this.values[current = (sameHash + 1) % valuesLength]) != null && currentValue.hashCode == hashCode)
						sameHash = current;
					this.values[index] = this.values[sameHash];
					this.values[sameHash] = null;
					this.elementSize--;
					break;
				}
				if (++index == valuesLength) {
					index = 0;
				}
			}
		}
	}

	public boolean contains(Object obj) {
		return get(obj) != null;
	}

	/*
	 * Return the object that is in this set and that is equals to the given object.
	 * Return null if not found.
	 */
	public Object get(Object obj) {
		cleanupGarbageCollectedValues();
		int valuesLength = this.values.length;
		int index = (obj.hashCode() & 0x7FFFFFFF) % valuesLength;
		HashableWeakReference currentValue;
		while ((currentValue = this.values[index]) != null) {
			Object referent;
			if (obj.equals(referent = currentValue.get())) {
				return referent;
			}
			if (++index == valuesLength) {
				index = 0;
			}
		}
		return null;
	}

	private void rehash() {
		WeakHashSet newHashSet = new WeakHashSet(this.elementSize * 2);		// double the number of expected elements
		newHashSet.referenceQueue = this.referenceQueue;
		HashableWeakReference currentValue;
		for (int i = 0, length = this.values.length; i < length; i++)
			if ((currentValue = this.values[i]) != null)
				newHashSet.addValue(currentValue);

		this.values = newHashSet.values;
		this.threshold = newHashSet.threshold;
		this.elementSize = newHashSet.elementSize;
	}

	/*
	 * Removes the object that is in this set and that is equals to the given object.
	 * Return the object that was in the set, or null if not found.
	 */
	public Object remove(Object obj) {
		cleanupGarbageCollectedValues();
		int valuesLength = this.values.length;
		int index = (obj.hashCode() & 0x7FFFFFFF) % valuesLength;
		HashableWeakReference currentValue;
		while ((currentValue = this.values[index]) != null) {
			Object referent;
			if (obj.equals(referent = currentValue.get())) {
				this.elementSize--;
				this.values[index] = null;
				rehash();
				return referent;
			}
			if (++index == valuesLength) {
				index = 0;
			}
		}
		return null;
	}

	public int size() {
		return this.elementSize;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer("{"); //$NON-NLS-1$
		for (int i = 0, length = this.values.length; i < length; i++) {
			HashableWeakReference value = this.values[i];
			if (value != null) {
				Object ref = value.get();
				if (ref != null) {
					buffer.append(ref.toString());
					buffer.append(", "); //$NON-NLS-1$
				}
			}
		}
		buffer.append("}"); //$NON-NLS-1$
		return buffer.toString();
	}
}
