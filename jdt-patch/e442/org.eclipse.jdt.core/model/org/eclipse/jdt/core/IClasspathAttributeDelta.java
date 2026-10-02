/*******************************************************************************
 * Copyright (c) 2022 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core;

/**
 * A delta which describes changes in {@link IClasspathAttribute} of a classpath
 * between two discrete points in time.  Given a delta,
 * clients can access the name of the affected attribute
 * as well as the current value of the attribute.
 * <p>
 * Deltas have a different status depending on the kind of change they represent.
 * The list below summarizes each status (as returned by {@link #getKind})
 * and its meaning (see individual constants for a more detailed description):
 * <ul>
 * <li>{@link #ADDED} - The attribute has been added.</li>
 * <li>{@link #REMOVED} - The attribute has been removed.</li>
 * <li>{@link #CHANGED} - The value of the attribute has been changed.
 * </ul>
 *
 * @since 3.33
 */
public interface IClasspathAttributeDelta {

	/**
	 * Status constant indicating that the attribute has been added.
	 *
	 * @see #getKind()
	 */
	public int ADDED = 1;

	/**
	 * Status constant indicating that the attribute has been removed.
	 *
	 * @see #getKind()
	 */
	public int REMOVED = 2;

	/**
	 * Status constant indicating that the attribute has been changed.
	 *
	 * @see #getKind()
	 */
	public int CHANGED = 4;

	/**
	 * Returns the kind of this delta - one of {@link #ADDED}, {@link #REMOVED},
	 * or {@link #CHANGED}.
	 *
	 * @return the kind of this delta
	 */
	int getKind();

	/**
	 * Returns the name of the affected attribute.
	 *
	 * @return the name of the attribute
	 */
	String getAttributeName();

	/**
	 * Returns the current value of the affected attribute.
	 *
	 * @return the value of the attribute
	 */
	String getAttributeValue();
}
