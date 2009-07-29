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
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.jdt.core.compiler.CharOperation;

public final class NameSet {

// to avoid using Enumerations, walk the individual values skipping nulls
public char[][] names;
public int elementSize; // number of elements in the table
public int threshold;

public NameSet(int size) {
	this.elementSize = 0;
	this.threshold = size; // size represents the expected number of elements
	int extraRoom = (int) (size * 1.5f);
	if (this.threshold == extraRoom)
		extraRoom++;
	this.names = new char[extraRoom][];
}

public char[] add(char[] name) {
	int length = names.length;
	int index = CharOperation.hashCode(name) % length;
	char[] current;
	while ((current = names[index]) != null) {
		if (CharOperation.equals(current, name)) return current;
		if (++index == length) index = 0;
	}
	names[index] = name;

	// assumes the threshold is never equal to the size of the table
	if (++elementSize > threshold) rehash();
	return name;
}

private void rehash() {
	NameSet newSet = new NameSet(elementSize * 2); // double the number of expected elements
	char[] current;
	for (int i = names.length; --i >= 0;)
		if ((current = names[i]) != null)
			newSet.add(current);

	this.names = newSet.names;
	this.elementSize = newSet.elementSize;
	this.threshold = newSet.threshold;
}

public String toString() {
	String s = ""; //$NON-NLS-1$
	char[] name;
	for (int i = 0, l = names.length; i < l; i++)
		if ((name = names[i]) != null)
			s += new String(name) + "\n"; //$NON-NLS-1$
	return s;
}
}
