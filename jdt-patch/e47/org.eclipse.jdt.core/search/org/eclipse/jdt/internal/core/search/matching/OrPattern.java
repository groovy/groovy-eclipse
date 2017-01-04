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

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.index.Index;
import org.eclipse.jdt.internal.core.search.IndexQueryRequestor;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;

public class OrPattern extends SearchPattern implements IIndexConstants {

	protected SearchPattern[] patterns;

	/**
	 * One of {@link #R_ERASURE_MATCH}, {@link #R_EQUIVALENT_MATCH}, {@link #R_FULL_MATCH}.
	 */
	int matchCompatibility;

	public OrPattern(SearchPattern leftPattern, SearchPattern rightPattern) {
		super(Math.max(leftPattern.getMatchRule(), rightPattern.getMatchRule()));
		this.kind = OR_PATTERN;
		this.mustResolve = leftPattern.mustResolve || rightPattern.mustResolve;

		SearchPattern[] leftPatterns = leftPattern instanceof OrPattern ? ((OrPattern) leftPattern).patterns : null;
		SearchPattern[] rightPatterns = rightPattern instanceof OrPattern ? ((OrPattern) rightPattern).patterns : null;
		int leftSize = leftPatterns == null ? 1 : leftPatterns.length;
		int rightSize = rightPatterns == null ? 1 : rightPatterns.length;
		this.patterns = new SearchPattern[leftSize + rightSize];

		if (leftPatterns == null)
			this.patterns[0] = leftPattern;
		else
			System.arraycopy(leftPatterns, 0, this.patterns, 0, leftSize);
		if (rightPatterns == null)
			this.patterns[leftSize] = rightPattern;
		else
			System.arraycopy(rightPatterns, 0, this.patterns, leftSize, rightSize);

		// Store erasure match
		this.matchCompatibility = 0;
		for (int i = 0, length = this.patterns.length; i < length; i++) {
			this.matchCompatibility |= ((JavaSearchPattern) this.patterns[i]).matchCompatibility;
		}
	}
	public void findIndexMatches(Index index, IndexQueryRequestor requestor, SearchParticipant participant, IJavaSearchScope scope, IProgressMonitor progressMonitor) throws IOException {
		// per construction, OR pattern can only be used with a PathCollector (which already gather results using a set)
		try {
			index.startQuery();
			for (int i = 0, length = this.patterns.length; i < length; i++)
				this.patterns[i].findIndexMatches(index, requestor, participant, scope, progressMonitor);
		} finally {
			index.stopQuery();
		}
	}

	public SearchPattern getBlankPattern() {
		return null;
	}

	boolean isErasureMatch() {
		return (this.matchCompatibility & R_ERASURE_MATCH) != 0;
	}

	public boolean isPolymorphicSearch() {
		for (int i = 0, length = this.patterns.length; i < length; i++)
			if (this.patterns[i].isPolymorphicSearch()) return true;
		return false;
	}

	/**
	 * Returns whether the pattern has one or several package declaration or not.
	 *
	 * @return <code>true</code> if one at least of the stored pattern is a package declaration
	 * 	pattern ({@link PackageDeclarationPattern}), <code>false</code> otherwise.
	 */
	public final boolean hasPackageDeclaration() {
		for (int i = 0, length = this.patterns.length; i < length; i++) {
			if (this.patterns[i] instanceof PackageDeclarationPattern) return true;
		}
		return false;
	}

	/**
	 * Returns whether the pattern has signatures or not.
	 * @return true if one at least of the stored pattern has signatures.
	 */
	public final boolean hasSignatures() {
		boolean isErasureMatch = isErasureMatch();
		for (int i = 0, length = this.patterns.length; i < length && !isErasureMatch; i++) {
			if (((JavaSearchPattern) this.patterns[i]).hasSignatures()) return true;
		}
		return false;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.patterns[0].toString());
		for (int i = 1, length = this.patterns.length; i < length; i++) {
			buffer.append("\n| "); //$NON-NLS-1$
			buffer.append(this.patterns[i].toString());
		}
		return buffer.toString();
	}
}
