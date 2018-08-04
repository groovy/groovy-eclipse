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
package org.eclipse.jdt.internal.core.nd.util;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Maps IPath keys onto values.
 */
public class PathMap<T> {
	private static class Node<T> {
		int depth;
		boolean exists;
		T value;
		Map<String, Node<T>> children;

		Node(int depth) {
			this.depth = depth;
		}

		String getSegment(IPath key) {
			return key.segment(this.depth);
		}

		Node<T> createNode(IPath key) {
			if (this.depth == key.segmentCount()) {
				this.exists = true;
				return this;
			}

			String nextSegment = getSegment(key);
			Node<T> next = createChild(nextSegment);
			return next.createNode(key);
		}

		public Node<T> createChild(String nextSegment) {
			if (this.children == null) {
				this.children = new HashMap<>();
			}
			Node<T> next = this.children.get(nextSegment);
			if (next == null) {
				next = new Node<>(this.depth + 1);
				this.children.put(nextSegment, next);
			}
			return next;
		}

		/**
		 * Returns this node or one of its descendants whose path is the longest-possible prefix of the given key (or
		 * equal to it).
		 */
		public Node<T> getMostSpecificNode(IPath key) {
			if (this.depth == key.segmentCount()) {
				return this;
			}
			String nextSegment = getSegment(key);

			Node<T> child = getChild(nextSegment);
			if (child == null) {
				return this;
			}
			Node<T> result = child.getMostSpecificNode(key);
			if (result.exists) {
				return result;
			} else {
				return this;
			}
		}

		Node<T> getChild(String nextSegment) {
			if (this.children == null) {
				return null;
			}
			return this.children.get(nextSegment);
		}

	    public void addAllKeys(Set<IPath> result, IPath parent) {
	    	if (this.exists) {
	    		result.add(parent);
	    	}

	    	if (this.children == null) {
	    		return;
	    	}
	
	    	for (Entry<String, Node<T>> next : this.children.entrySet()) {
	    		String key = next.getKey();
	    		IPath nextPath = buildChildPath(parent, key);
	    		next.getValue().addAllKeys(result, nextPath);
	    	}
	    }

	    IPath buildChildPath(IPath parent, String key) {
	      IPath nextPath = parent.append(key);
	      return nextPath;
	    }
		
	    public void toString(StringBuilder builder, IPath parentPath) {
		    if (this.exists) {
		    	builder.append("["); //$NON-NLS-1$
		    	builder.append(parentPath);
		    	builder.append("] = "); //$NON-NLS-1$
		    	builder.append(this.value);
		    	builder.append("\n"); //$NON-NLS-1$
		    }
		    if (this.children != null) { 
		    	for (Entry<String, Node<T>> next : this.children.entrySet()) {
		    		String key = next.getKey();
		    		IPath nextPath = buildChildPath(parentPath, key);
		    		next.getValue().toString(builder, nextPath);
		    	}
		    }
		}
	}

	private static class DeviceNode<T> extends Node<T> {
		Node<T> noDevice = new Node<>(0);

		DeviceNode() {
			super(-1);
		}

		@Override
		String getSegment(IPath key) {
			return key.getDevice();
		}

		@Override
		public Node<T> createChild(String nextSegment) {
			if (nextSegment == null) {
				return this.noDevice;
			}
			return super.createChild(nextSegment);
		}

		@Override
		Node<T> getChild(String nextSegment) {
			if (nextSegment == null) {
				return this.noDevice;
			}
			return super.getChild(nextSegment);
		}

		@Override
		IPath buildChildPath(IPath parent, String key) {
    		IPath nextPath = Path.EMPTY.append(parent);
    		nextPath.setDevice(key);
    		return nextPath;
		}
		
		@Override
		public void toString(StringBuilder builder, IPath parentPath) {
			this.noDevice.toString(builder, parentPath);
			super.toString(builder,parentPath);
		}
	}

	private Node<T> root = new DeviceNode<T>();

	/**
	 * Inserts the given key into the map.
	 */
	public T put(IPath key, T value) {
		Node<T> node = this.root.createNode(key);
		T result = node.value;
		node.value = value;
		return result;
	}

	/**
	 * Returns the value associated with the given key
	 */
	public T get(IPath key) {
		Node<T> node = this.root.getMostSpecificNode(key);
		if (!node.exists || node.depth < key.segmentCount()) {
			return null;
		}
		return node.value;
	}

	/**
	 * Returns the value associated with the longest prefix of the given key
	 * that can be found in the map.
	 */
	public T getMostSpecific(IPath key) {
		Node<T> node = this.root.getMostSpecificNode(key);
		if (!node.exists) {
			return null;
		}
		return node.value;
	}

	/**
	 * Returns true iff any key in this map is a prefix of the given path.
	 */
	public boolean containsPrefixOf(IPath path) {
		Node<T> node = this.root.getMostSpecificNode(path);
		return node.exists;
	}

	public Set<IPath> keySet() {
		Set<IPath> result = new HashSet<>();

		this.root.addAllKeys(result, Path.EMPTY);
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		this.root.toString(builder, Path.EMPTY);
		return builder.toString();
	}

	/**
	 * Returns true iff this map contains any key that starts with the given prefix.
	 */
	public boolean containsKeyStartingWith(IPath next) {
		Node<T> node = this.root.getMostSpecificNode(next);
		return node.depth == next.segmentCount();
	}
}
