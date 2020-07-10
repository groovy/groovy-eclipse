/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
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
package org.eclipse.jdt.internal.core.nd.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 * Provides functionality similar to a Map, with the feature that char arrays
 * and sections of char arrays (known as slices) may be used as keys.
 *
 * This class is useful because small pieces of an existing large char[] buffer
 * can be directly used as map keys. This avoids the need to create many String
 * objects as would normally be needed as keys in a standard java.util.Map.
 * Thus performance is improved in the CDT core.
 *
 * Most methods are overloaded with two versions, one that uses a
 * section of a char[] as the key (a slice), and one that uses
 * the entire char[] as the key.
 *
 * This class is intended as a replacement for CharArrayObjectMap.
 *
 * ex:
 * char[] key = "one two three".toCharArray();
 * map.put(key, 4, 3, new Integer(99));
 * map.get(key, 4, 3); // returns 99
 * map.get("two".toCharArray()); // returns 99
 *
 * @author Mike Kucera
 *
 * @param <V>
 */
public final class CharArrayMap<V> {

	/**
	 * Wrapper class used as keys in the map. The purpose
	 * of this class is to provide implementations of
	 * equals() and hashCode() that operate on array slices.
	 *
	 * This class is private so it is assumed that the arguments
	 * passed to the constructor are legal.
	 */
    private static final class Key implements Comparable<Key>{
        final char[] buffer;
        final int start;
        final int length;

        public Key(char[] buffer, int start, int length) {
            this.buffer = buffer;
            this.length = length;
            this.start = start;
        }

        /**
         * @throws NullPointerException if buffer is null
         */
        public Key(char[] buffer) {
        	this.buffer = buffer;
        	this.length = buffer.length; // throws NPE
        	this.start = 0;
        }

        @Override
        public boolean equals(Object x) {
        	if(this == x)
        		return true;
        	if(!(x instanceof Key))
        		return false;

            Key k = (Key) x;
            if(this.length != k.length)
            	return false;

            for(int i = this.start, j = k.start; i < this.length; i++, j++) {
            	if(this.buffer[i] != k.buffer[j]) {
            		return false;
            	}
            }
            return true;
        }

        @Override
        public int hashCode() {
            int result = 17;
            for(int i = this.start; i < this.start+this.length; i++) {
            	result = 37 * result + this.buffer[i];
            }
            return result;
        }

        @SuppressWarnings("nls")
		@Override
        public String toString() {
        	String slice = new String(this.buffer, this.start, this.length);
        	return "'" + slice + "'@(" + this.start + "," + this.length + ")";
        }


        @Override
		public int compareTo(Key other) {
        	char[] b1 = this.buffer, b2 = other.buffer;

        	for(int i = this.start, j = other.start; i < b1.length && j < b2.length; i++, j++) {
        		if(b1[i] != b2[j])
        			return b1[i] < b2[j] ? -1 : 1;
        	}
        	return b1.length - b2.length;
        }
    }


    /**
     * Used to enforce preconditions.
     * Note that the NPE thrown by mutator methods is thrown from the Key constructor.
     *
     * @throws IndexOutOfBoundsException if boundaries are wrong in any way
     */
    private static void checkBoundaries(char[] chars, int start, int length) {
    	if(start < 0 || length < 0 || start >= chars.length || start + length > chars.length)
    		throw new IndexOutOfBoundsException("Buffer length: " + chars.length + //$NON-NLS-1$
    				                          ", Start index: " + start + //$NON-NLS-1$
    				                          ", Length: " + length); //$NON-NLS-1$
    }


    private final Map<Key,V> map;


    /**
     * Constructs an empty CharArrayMap with default initial capacity.
     */
    public CharArrayMap() {
    	this.map = new HashMap<Key,V>();
    }


    /**
     * Static factory method that constructs an empty CharArrayMap with default initial capacity,
     * and the map will be kept in ascending key order.
     *
     * Characters are compared using a strictly numerical comparison; it is not locale-dependent.
     */
    public static <V> CharArrayMap<V> createOrderedMap() {
    	// TreeMap does not have a constructor that takes an initial capacity
    	return new CharArrayMap<V>(new TreeMap<Key, V>());
    }


    private CharArrayMap(Map<Key, V> map) {
    	assert map != null;
    	this.map = map;
    }


    /**
     * Constructs an empty CharArrayMap with the given initial capacity.
     * @throws IllegalArgumentException if the initial capacity is negative
     */
    public CharArrayMap(int initialCapacity) {
    	this.map = new HashMap<Key,V>(initialCapacity);
    }

    /**
	 * Creates a new mapping in this map, uses the given array slice as the key.
	 * If the map previously contained a mapping for this key, the old value is replaced.
	 * @throws NullPointerException if chars is null
	 * @throws IndexOutOfBoundsException if the boundaries specified by start and length are out of range
	 */
    public void put(char[] chars, int start, int length, V value) {
    	checkBoundaries(chars, start, length);
        this.map.put(new Key(chars, start, length), value);
    }

    /**
	 * Creates a new mapping in this map, uses all of the given array as the key.
	 * If the map previously contained a mapping for this key, the old value is replaced.
	 * @throws NullPointerException if chars is null
	 */
    public void put(char[] chars, V value) {
        this.map.put(new Key(chars), value);
    }

    /**
	 * Returns the value to which the specified array slice is mapped in this map,
	 * or null if the map contains no mapping for this key.
	 * @throws NullPointerException if chars is null
	 * @throws IndexOutOfBoundsException if the boundaries specified by start and length are out of range
	 */
    public V get(char[] chars, int start, int length) {
    	checkBoundaries(chars, start, length);
        return this.map.get(new Key(chars, start, length));
    }

    /**
	 * Returns the value to which the specified array is mapped in this map,
	 * or null if the map contains no mapping for this key.
	 * @throws NullPointerException if chars is null
	 */
    public V get(char[] chars) {
        return this.map.get(new Key(chars));
    }

    /**
	 * Removes the mapping for the given array slice if present.
	 * Returns the value object that corresponded to the key
	 * or null if the key was not in the map.
	 * @throws NullPointerException if chars is null
	 * @throws IndexOutOfBoundsException if the boundaries specified by start and length are out of range
	 */
    public V remove(char[] chars, int start, int length) {
    	checkBoundaries(chars, start, length);
    	return this.map.remove(new Key(chars, start, length));
    }

    /**
	 * Removes the mapping for the given array if present.
	 * Returns the value object that corresponded to the key
	 * or null if the key was not in the map.
	 * @throws NullPointerException if chars is null
	 */
    public V remove(char[] chars) {
    	return this.map.remove(new Key(chars));
    }

    /**
	 * Returns true if the given key has a value associated with it in the map.
	 * @throws NullPointerException if chars is null
	 * @throws IndexOutOfBoundsException if the boundaries specified by start and length are out of range
	 */
    public boolean containsKey(char[] chars, int start, int length) {
    	checkBoundaries(chars, start, length);
    	return this.map.containsKey(new Key(chars, start, length));
    }

    /**
	 * Returns true if the given key has a value associated with it in the map.
	 * @throws NullPointerException if chars is null
	 */
    public boolean containsKey(char[] chars) {
    	return this.map.containsKey(new Key(chars));
    }

    /**
	 * Returns true if the given value is contained in the map.
	 */
    public boolean containsValue(V value) {
    	return this.map.containsValue(value);
    }

    /**
	 * Use this in a foreach loop.
	 */
    public Collection<V> values() {
        return this.map.values();
    }

    /**
	 * Returns the keys stored in the map.
	 */
    public Collection<char[]> keys() {
    	Set<Key> keys= this.map.keySet();
    	ArrayList<char[]> r= new ArrayList<char[]>(keys.size());
    	for (Key key : keys) {
    		r.add(CharArrayUtils.extract(key.buffer, key.start, key.length));
		}
        return r;
    }

    /**
	 * Removes all mappings from the map.
	 */
    public void clear() {
    	this.map.clear();
    }

    /**
	 * Returns the number of mappings.
	 */
    public int size() {
    	return this.map.size();
    }

    /**
	 * Returns true if the map is empty.
	 */
    public boolean isEmpty() {
    	return this.map.isEmpty();
    }


    /**
     * Returns a String representation of the map.
     */
    @Override
    public String toString() {
    	return this.map.toString();
    }

}
