/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search;

import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.core.index.IndexLocation;
import org.eclipse.jdt.internal.core.search.indexing.BinaryIndexer;
import org.eclipse.jdt.internal.core.search.indexing.ManifestIndexer;
import org.eclipse.jdt.internal.core.search.indexing.SourceIndexer;
import org.eclipse.jdt.internal.core.search.matching.MatchLocator;

/**
 * A search participant describes a particular extension to a generic search mechanism, allowing thus to
 * perform combined search actions which will involve all required participants
 *
 * A search scope defines which participants are involved.
 *
 * A search participant is responsible for holding index files, and selecting the appropriate ones to feed to
 * index queries. It also can map a document path to an actual document (note that documents could live outside
 * the workspace or no exist yet, and thus aren't just resources).
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class JavaSearchParticipant extends SearchParticipant {

	private ThreadLocal indexSelector = new ThreadLocal();
	private SourceIndexer sourceIndexer;

	@Override
	public void beginSearching() {
		super.beginSearching();
		this.indexSelector.set(null);
	}

	@Override
	public void doneSearching() {
		this.indexSelector.set(null);
		super.doneSearching();
	}

	@Override
	public String getDescription() {
		return "Java"; //$NON-NLS-1$
	}

	@Override
	public SearchDocument getDocument(String documentPath) {
		return new JavaSearchDocument(documentPath, this);
	}

	@Override
	public void indexDocument(SearchDocument document, IPath indexPath) {
		// TODO must verify that the document + indexPath match, when this is not called from scheduleDocumentIndexing
		document.removeAllIndexEntries(); // in case the document was already indexed

		String documentPath = document.getPath();
		if (org.eclipse.jdt.internal.core.util.Util.isJavaLikeFileName(documentPath)) {
			this.sourceIndexer = new SourceIndexer(document);
			this.sourceIndexer.indexDocument();
		} else if (org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(documentPath)) {
			new BinaryIndexer(document).indexDocument();
		} else if (documentPath.endsWith(TypeConstants.AUTOMATIC_MODULE_NAME)) {
			new ManifestIndexer(document).indexDocument();
		}
	}

	@Override
	public void indexResolvedDocument(SearchDocument document, IPath indexPath) {
		String documentPath = document.getPath();
		if (org.eclipse.jdt.internal.core.util.Util.isJavaLikeFileName(documentPath)) {
			if (this.sourceIndexer != null)
				this.sourceIndexer.indexResolvedDocument();
			this.sourceIndexer = null;
		}
	}

	@Override
	public void resolveDocument(SearchDocument document) {
		String documentPath = document.getPath();
		if (org.eclipse.jdt.internal.core.util.Util.isJavaLikeFileName(documentPath)) {
			if (this.sourceIndexer != null)
				this.sourceIndexer.resolveDocument();
		}
	}

	@Override
	public void locateMatches(SearchDocument[] indexMatches, SearchPattern pattern,
			IJavaSearchScope scope, SearchRequestor requestor, IProgressMonitor monitor) throws CoreException {

		MatchLocator matchLocator =
			new MatchLocator(
				pattern,
				requestor,
				scope,
				monitor
		);

		/* eliminating false matches and locating them */
		if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
		matchLocator.locateMatches(indexMatches);
	}

	@Override
	public IPath[] selectIndexes(SearchPattern pattern, IJavaSearchScope scope) {
		IndexSelector selector = (IndexSelector) this.indexSelector.get();
		if (selector == null) {
			selector = new IndexSelector(scope, pattern);
			this.indexSelector.set(selector);
		}
		IndexLocation[] urls = selector.getIndexLocations();
		IPath[] paths = new IPath[urls.length];
		for (int i = 0; i < urls.length; i++) {
			paths[i] = new Path(urls[i].getIndexFile().getPath());
		}
		return paths;
	}

	public IndexLocation[] selectIndexURLs(SearchPattern pattern, IJavaSearchScope scope) {
		IndexSelector selector = (IndexSelector) this.indexSelector.get();
		if (selector == null) {
			selector = new IndexSelector(scope, pattern);
			this.indexSelector.set(selector);
		}
		return selector.getIndexLocations();
	}

}
