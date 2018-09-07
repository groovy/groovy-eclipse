/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.jdt.internal.core.search;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;
import org.eclipse.jdt.internal.core.index.Index;

public class SubTypeSearchJob extends PatternSearchJob {

SimpleSet indexes = new SimpleSet(5);

public SubTypeSearchJob(SearchPattern pattern, SearchParticipant participant, IJavaSearchScope scope, IndexQueryRequestor requestor) {
	super(pattern, participant, scope, requestor);
}
public void finished() {
	Object[] values = this.indexes.values;
	for (int i = 0, l = values.length; i < l; i++)
		if (values[i] != null)
			((Index) values[i]).stopQuery();
}
@Override
public Index[] getIndexes(IProgressMonitor progressMonitor) {
	if (this.indexes.elementSize == 0) {
		return super.getIndexes(progressMonitor);
	}
	this.areIndexesReady = true; // use stored indexes until the job's end
	Index[] values = new Index[this.indexes.elementSize];
	this.indexes.asArray(values);
	return values;
}
@Override
public boolean search(Index index, IProgressMonitor progressMonitor) {
	if (index == null) return COMPLETE;
	if (this.indexes.addIfNotIncluded(index) == index)
		index.startQuery();
	return super.search(index, progressMonitor);
}
}
