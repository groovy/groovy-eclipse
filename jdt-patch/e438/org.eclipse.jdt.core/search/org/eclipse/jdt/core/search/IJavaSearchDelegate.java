/*******************************************************************************
 * Copyright (c) 2025 Red Hat Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.search;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.core.search.matching.MatchLocator;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;

/**
 * This interface represents a delegate that can take over the discovery of search results
 * for java-based searches.
 *
 * This interface makes use of internal classes and is not considered stable or API.
 *
 * @since 3.41
 */
public interface IJavaSearchDelegate {

	/**
	 * Fill the PossibleMatch objects' state with confirmed and possible results using the
	 * search strategy that this delegate employs.
	 *
	 * @param locator The MatchLocator initiating the request
	 * @param javaProject The context in which the search is being performed
	 * @param possibleMatches An array of possible matches
	 * @param start The start index with which to begin searching
	 * @param length The length of matches with which to search
	 * @throws CoreException
	 */
	void locateMatches(MatchLocator locator, IJavaProject javaProject, PossibleMatch[] possibleMatches, int start, int length) throws CoreException;

}