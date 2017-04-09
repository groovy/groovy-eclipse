/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
	this.maxSize = INITIAL_SIZE;
	this.size = 0;
	this.elements = new IType[this.maxSize];
}
public TypeVector(IType[] types) {
	this.size = types.length;
	this.maxSize = this.size + 1; // when an element is added, it assumes that the length is > 0
	this.elements = new IType[this.maxSize];
	System.arraycopy(types, 0, this.elements, 0, this.size);
}
public TypeVector(IType type) {
	this.maxSize = INITIAL_SIZE;
	this.size = 1;
	this.elements = new IType[this.maxSize];
	this.elements[0] = type;
}
public void add(IType newElement) {
	if (this.size == this.maxSize)	// knows that size starts <= maxSize
		System.arraycopy(this.elements, 0, (this.elements = new IType[this.maxSize *= 2]), 0, this.size);
	this.elements[this.size++] = newElement;
}
public void addAll(IType[] newElements) {
	if (this.size + newElements.length >= this.maxSize) {
		this.maxSize = this.size + newElements.length;	// assume no more elements will be added
		System.arraycopy(this.elements, 0, (this.elements = new IType[this.maxSize]), 0, this.size);
	}
	System.arraycopy(newElements, 0, this.elements, this.size, newElements.length);
	this.size += newElements.length;
}
public boolean contains(IType element) {
	for (int i = this.size; --i >= 0;)
		if (element.equals(this.elements[i]))
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
	return this.elements[index];
}
public IType[] elements() {

	// do not resize to 0 if empty since may add more elements later
	if (this.size == 0) return NoElements;

	if (this.size < this.maxSize) {
		this.maxSize = this.size;
		System.arraycopy(this.elements, 0, (this.elements = new IType[this.maxSize]), 0, this.size);
	}
	return this.elements;
}
public IType find(IType element) {
	for (int i = this.size; --i >= 0;)
		if (element == this.elements[i])
			return this.elements[i];
	return null;
}
public IType remove(IType element) {
	// assumes only one occurrence of the element exists
	for (int i = this.size; --i >= 0;)
		if (element == this.elements[i]) {
			// shift the remaining elements down one spot
			System.arraycopy(this.elements, i + 1, this.elements, i, --this.size - i);
			this.elements[this.size] = null;
			return element;
		}
	return null;
}
public void removeAll() {
	for (int i = this.size; --i >= 0;)
		this.elements[i] = null;
	this.size = 0;
}
public String toString() {
	StringBuffer buffer = new StringBuffer("["); //$NON-NLS-1$
	for (int i = 0; i < this.size; i++) {
		buffer.append("\n"); //$NON-NLS-1$
		buffer.append(this.elements[i]);
	}
	buffer.append("\n]"); //$NON-NLS-1$
	return buffer.toString();
}
}
