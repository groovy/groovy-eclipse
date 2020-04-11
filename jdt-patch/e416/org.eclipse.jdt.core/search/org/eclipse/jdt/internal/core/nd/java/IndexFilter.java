/*******************************************************************************
 * Copyright (c) 2006, 2016 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.IBinding;

/**
 * Can be subclassed and used for queries in the index.
 */
public class IndexFilter {
	public static final IndexFilter ALL = new IndexFilter();

	/**
	 * Get an IndexFilter that accepts everything
	 *
	 * @return an IndexFilter instance
	 */
	public static IndexFilter getFilter() {
		return new IndexFilter();
	}

	/**
	 * Determines whether or not a binding is valid.
	 *
	 * @param binding the binding being checked for validity
	 * @return whether or not the binding is valid
	 * @throws CoreException
	 */
	public boolean acceptBinding(IBinding binding) throws CoreException {
		return true;
	}
}
