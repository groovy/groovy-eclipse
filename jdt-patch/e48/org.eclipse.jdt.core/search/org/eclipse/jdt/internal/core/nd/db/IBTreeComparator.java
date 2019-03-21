/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.db;

import org.eclipse.jdt.internal.core.nd.Nd;

public interface IBTreeComparator {
	/**
	 * Compare two records. Used for insert.
	 */
	public abstract int compare(Nd nd, long record1, long record2);
}
