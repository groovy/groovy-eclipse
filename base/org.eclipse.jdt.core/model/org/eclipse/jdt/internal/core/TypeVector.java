/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IType;

public final class TypeVector {
	static int INITIAL_SIZE = 10;
	
	public int size;
	int maxSize;
	IType[] elements;
	
	public final static IType[] NoElements = new IType[0];
	
public TypeVector() {
	maxSize = INITIAL_SIZE;
	size = 0;
	elements = new IType[maxSize];
}
public TypeVector(IType[] types) {
	this.size = types.length; 
	this.maxSize = this.size + 1; // when an element is added, it assumes that the length is > 0
	elements = new IType[this.maxSize];
	System.arraycopy(types, 0, elements, 0, this.size);	
}
public TypeVector(IType type) {
	this.maxSize = INITIAL_SIZE;
	this.size = 1;
	elements = new IType[this.maxSize];
	elements[0] = type;
}
public void add(IType newElement) {
	if (size == maxSize)	// knows that size starts <= maxSize
		System.arraycopy(elements, 0, (elements = new IType[maxSize *= 2]), 0, size);
	elements[size++] = newElement;
}
public void addAll(IType[] newElements) {
	if (size + newElements.length >= maxSize) {
		maxSize = size + newElements.length;	// assume no more elements will be added
		System.arraycopy(elements, 0, (elements = new IType[maxSize]), 0, size);
	}
	System.arraycopy(newElements, 0, elements, size, newElements.length);
	size += newElements.length;
}
public boolean contains(IType element) {
	for (int i = size; --i >= 0;)
		if (element.equals(elements[i]))
			return true;
	return false;
}
public TypeVector copy() {
	TypeVector clone = new TypeVector();
	int length = this.elements.length;
	System.arraycopy(this.elements, 0, clone.elements = new IType[length], 0, length);
	clone.size = this.size;
	clone.maxSize = this.maxSize;
	return clone;
}
public IType elementAt(int index) {
	return elements[index];
}
public IType[] elements() {
	
	// do not resize to 0 if empty since may add more elements later
	if (this.size == 0) return NoElements;
	
	if (this.size < this.maxSize) {
		maxSize = size;
		System.arraycopy(this.elements, 0, (this.elements = new IType[maxSize]), 0, size);
	}
	return this.elements;
}
public IType find(IType element) {
	for (int i = size; --i >= 0;)
		if (element == elements[i])
			return elements[i];
	return null;
}
public IType remove(IType element) {
	// assumes only one occurrence of the element exists
	for (int i = size; --i >= 0;)
		if (element == elements[i]) {
			// shift the remaining elements down one spot
			System.arraycopy(elements, i + 1, elements, i, --size - i);
			elements[size] = null;
			return element;
		}
	return null;
}
public void removeAll() {
	for (int i = size; --i >= 0;)
		elements[i] = null;
	size = 0;
}
public String toString() {
	StringBuffer buffer = new StringBuffer("["); //$NON-NLS-1$
	for (int i = 0; i < size; i++) {
		buffer.append("\n"); //$NON-NLS-1$
		buffer.append(elements[i]);
	}
	buffer.append("\n]"); //$NON-NLS-1$
	return buffer.toString();
}
}
