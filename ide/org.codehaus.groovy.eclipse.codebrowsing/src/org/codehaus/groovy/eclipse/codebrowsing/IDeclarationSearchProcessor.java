/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Edward Povazan   - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.codebrowsing;

import org.eclipse.jdt.core.IJavaElement;


/**
 * Processes a search for a declaration. There are many such processors, some
 * specialized for searching for methods, other for variables, other which try
 * to infer declaration matches for dynamic types and so on.
 * 
 * @author emp
 */
public interface IDeclarationSearchProcessor {
	/**
	 * Get declaration match proposals, if any.
	 * @param info
	 * @return An array of proposals, or an empty array if there weren't any.
	 */
	public IJavaElement[] getProposals(IDeclarationSearchInfo info);
	
    public static final IJavaElement[] NONE = new IJavaElement[0];
}
