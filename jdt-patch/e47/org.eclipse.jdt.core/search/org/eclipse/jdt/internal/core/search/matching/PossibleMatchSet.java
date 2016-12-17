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
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;

/**
 * A set of PossibleMatches that is sorted by package fragment roots.
 */
public class PossibleMatchSet {

private SimpleLookupTable rootsToPossibleMatches = new SimpleLookupTable(5);
private int elementCount = 0;

public void add(PossibleMatch possibleMatch) {
	IPath path = possibleMatch.openable.getPackageFragmentRoot().getPath();
	ObjectVector possibleMatches = (ObjectVector) this.rootsToPossibleMatches.get(path);
	if (possibleMatches != null) {
		PossibleMatch storedMatch = (PossibleMatch) possibleMatches.find(possibleMatch);
		if (storedMatch != null) {
			while (storedMatch.getSimilarMatch() != null) {
				storedMatch = storedMatch.getSimilarMatch();
			}
			storedMatch.setSimilarMatch(possibleMatch);
			return;
		}
	} else{
		this.rootsToPossibleMatches.put(path, possibleMatches = new ObjectVector());
	}

	possibleMatches.add(possibleMatch);
	this.elementCount++;
}
public PossibleMatch[] getPossibleMatches(IPackageFragmentRoot[] roots) {
	PossibleMatch[] result = new PossibleMatch[this.elementCount];
	int index = 0;
	for (int i = 0, length = roots.length; i < length; i++) {
		ObjectVector possibleMatches = (ObjectVector) this.rootsToPossibleMatches.get(roots[i].getPath());
		if (possibleMatches != null) {
			possibleMatches.copyInto(result, index);
			index += possibleMatches.size();
		}
	}
	if (index < this.elementCount)
		System.arraycopy(result, 0, result = new PossibleMatch[index], 0, index);
	return result;
}
public void reset() {
	this.rootsToPossibleMatches = new SimpleLookupTable(5);
	this.elementCount = 0;
}
}
