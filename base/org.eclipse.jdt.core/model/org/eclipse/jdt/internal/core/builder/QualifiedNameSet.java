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

public class QualifiedNameSet {

// to avoid using Enumerations, walk the individual values skipping nulls
public char[][][] qualifiedNames;
public int elementSize; // number of elements in the table
public int threshold;

public QualifiedNameSet(int size) {
	this.elementSize = 0;
	this.threshold = size; // size represents the expected number of elements
	int extraRoom = (int) (size * 1.5f);
	if (this.threshold == extraRoom)
		extraRoom++;
	this.qualifiedNames = new char[extraRoom][][];
}

public char[][] add(char[][] qualifiedName) {
	int qLength = qualifiedName.length;
	if (qLength == 0) return CharOperation.NO_CHAR_CHAR;

	int length = qualifiedNames.length;
	int index = CharOperation.hashCode(qualifiedName[qLength - 1]) % length;
	char[][] current;
	while ((current = qualifiedNames[index]) != null) {
		if (CharOperation.equals(current, qualifiedName)) return current;
		if (++index == length) index = 0;
	}
	qualifiedNames[index] = qualifiedName;

	// assumes the threshold is never equal to the size of the table
	if (++elementSize > threshold) rehash();
	return qualifiedName;
}

private void rehash() {
	QualifiedNameSet newSet = new QualifiedNameSet(elementSize * 2); // double the number of expected elements
	char[][] current;
	for (int i = qualifiedNames.length; --i >= 0;)
		if ((current = qualifiedNames[i]) != null)
			newSet.add(current);

	this.qualifiedNames = newSet.qualifiedNames;
	this.elementSize = newSet.elementSize;
	this.threshold = newSet.threshold;
}

public String toString() {
	String s = ""; //$NON-NLS-1$
	char[][] qualifiedName;
	for (int i = 0, l = qualifiedNames.length; i < l; i++)
		if ((qualifiedName = qualifiedNames[i]) != null)
			s += CharOperation.toString(qualifiedName) + "\n"; //$NON-NLS-1$
	return s;
}
}
