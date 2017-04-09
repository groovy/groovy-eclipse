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
package org.eclipse.jdt.internal.core.search.matching;

import java.io.IOException;

import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.index.EntryResult;
import org.eclipse.jdt.internal.core.index.Index;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;

public class SecondaryTypeDeclarationPattern extends TypeDeclarationPattern {

	private final static char[] SECONDARY_PATTERN_KEY = "*/S".toCharArray(); //$NON-NLS-1$

public SecondaryTypeDeclarationPattern() {
	super(null, null, null, IIndexConstants.SECONDARY_SUFFIX, R_EXACT_MATCH | R_CASE_SENSITIVE);
}

public SecondaryTypeDeclarationPattern(int matchRule) {
	super(matchRule);
}

public SearchPattern getBlankPattern() {
	return new SecondaryTypeDeclarationPattern(R_EXACT_MATCH | R_CASE_SENSITIVE);
}
protected StringBuffer print(StringBuffer output) {
	output.append("Secondary"); //$NON-NLS-1$
	return super.print(output);
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.core.search.matching.TypeDeclarationPattern#queryIn(org.eclipse.jdt.internal.core.index.Index)
 */
public EntryResult[] queryIn(Index index) throws IOException {
	return index.query(CATEGORIES, SECONDARY_PATTERN_KEY, R_PATTERN_MATCH | R_CASE_SENSITIVE);
}

}
