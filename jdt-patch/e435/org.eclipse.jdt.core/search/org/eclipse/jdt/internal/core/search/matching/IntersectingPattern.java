/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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
 *     Microsoft Corporation - adapt to the new index match API
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import java.io.IOException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;
import org.eclipse.jdt.internal.core.index.EntryResult;
import org.eclipse.jdt.internal.core.index.Index;
import org.eclipse.jdt.internal.core.search.IndexQueryRequestor;

/**
 * Query the index multiple times and do an 'and' on the results.
 */
public abstract class IntersectingPattern extends JavaSearchPattern {

public IntersectingPattern(int patternKind, int matchRule) {
	super(patternKind, matchRule);
}

@Override
public void findIndexMatches(Index index, IndexQueryRequestor requestor, SearchParticipant participant, IJavaSearchScope scope, IProgressMonitor progressMonitor) throws IOException {
	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

	resetQuery();
	SimpleSet intersectedNames = null;
	try {
		index.startQuery();
		do {
			SearchPattern pattern = currentPattern();
			EntryResult[] entries = pattern.queryIn(index);
			if (entries == null) return;

			SearchPattern decodedResult = pattern.getBlankPattern();
			SimpleSet newIntersectedNames = new SimpleSet(3);
			for (EntryResult entry : entries) {
				if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

				decodedResult.decodeIndexKey(entry.getWord());
				if (pattern.matchesDecodedKey(decodedResult)) {
					String[] names = entry.getDocumentNames(index);
					if (intersectedNames != null) {
						for (String name : names)
							if (intersectedNames.includes(name))
								newIntersectedNames.add(name);
					} else {
						for (String name : names)
							newIntersectedNames.add(name);
					}
				}
			}

			if (newIntersectedNames.elementSize == 0) return;
			intersectedNames = newIntersectedNames;
		} while (hasNextQuery());
	} finally {
		index.stopQuery();
	}

	String containerPath = index.containerPath;
	char separator = index.separator;
	Object[] names = intersectedNames.values;
	for (Object name : names)
		if (name != null)
			acceptMatch((String) name, containerPath, separator, null/*no pattern*/, requestor, participant, scope, progressMonitor); // AndPatterns cannot provide the decoded result
}

@Override
public void findIndexMatches(Index index, IndexQueryRequestor requestor, SearchParticipant participant, IJavaSearchScope scope, boolean resolveDocumentName, IProgressMonitor progressMonitor) throws IOException {
	findIndexMatches(index, requestor, participant, scope, progressMonitor);
}

/**
 * Returns whether another query must be done.
 */
protected abstract boolean hasNextQuery();
/**
 * Resets the query and prepares this pattern to be queried.
 */
protected abstract void resetQuery();
}
