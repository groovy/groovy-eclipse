/*******************************************************************************
 * Copyright (c) 2017 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd;

/**
 * Implementations of this interface wrap content in the database as a java object.
 * All such objects have an address and a pointer to the database.
 */
public interface INdStruct {
	/**
	 * Returns the database address at which the struct begins.
	 */
	public long getAddress();

	/**
	 * Returns the database backing this struct.
	 */
	public Nd getNd();

	/**
	 * Given a nullable {@link INdStruct}, this returns the address of the struct
	 * or 0 if the object was null.
	 */
	static long addressOf(INdStruct nullable) {
		if (nullable == null) {
			return 0;
		}
		return nullable.getAddress();
	}
}
