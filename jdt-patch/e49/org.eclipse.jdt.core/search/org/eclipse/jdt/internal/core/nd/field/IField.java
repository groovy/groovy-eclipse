/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.field;

import org.eclipse.jdt.internal.core.nd.db.Database;

/**
 * Represents a single field of a struct in the {@link Database}. Holds metadata for that field
 * and permits laziy initialization of the field offset. Fields are normally instantiated as static
 * variables. Collectively, they describe the database schema but they are not associated with any
 * particular instance of data in the database.
 * <p>
 * Fields are temporarily mutable. On construction, a number of attributes (such as offset) are
 * computed in a second pass or are initialized as other fields are constructed. Generally such
 * attributes can't be computed in the constructor since they depend on knowledge of other fields
 * that must be instantiated first. However, once {@link StructDef#done()} has been called on the
 * last {@link StructDef}, fields are immutable and should not ever be modified again.
 */
public interface IField {
	/**
	 * Sets the field offset (bytes from the start of the struct). This is invoked some time after field construction,
	 * after the sizes of all preceeding fields are known.
	 */
	void setOffset(int offset);

	/**
	 * Returns the size of the field, in bytes.
	 */
	int getRecordSize();

	/**
	 * Returns the required byte alignment for the field.
	 */
	default int getAlignment() {
		return 1;
	}

	/**
	 * Returns the name of the field. This is mainly used for error messages, debug output, and diagnostic tools.
	 * Meant to be programmer-readable but not user-readable.
	 */
	String getFieldName();

	/**
	 * Returns the field offset, in bytes from the start of the struct.
	 */
	int getOffset();
}
