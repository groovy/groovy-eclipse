/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd;

import org.eclipse.jdt.internal.core.nd.db.Database;

/**
 * Holds type factories for all primitive types known to the Database.
 */
public class PrimitiveTypes {
	public static final ITypeFactory<Long> Pointer = new AbstractTypeFactory<Long>() {
		@Override
		public Long create(Nd dom, long address) {
			return dom.getDB().getRecPtr(address);
		}

		@Override
		public int getRecordSize() {
			return Database.PTR_SIZE;
		}

		@Override
		public Class<?> getElementClass() {
			return Long.class;
		}
	};

	public static final ITypeFactory<Short> Short = new AbstractTypeFactory<Short>() {
		@Override
		public Short create(Nd dom, long address) {
			return dom.getDB().getShort(address);
		}

		@Override
		public int getRecordSize() {
			return Database.SHORT_SIZE;
		}

		@Override
		public Class<?> getElementClass() {
			return Short.class;
		}
	};

	public static final ITypeFactory<Integer> Integer = new AbstractTypeFactory<Integer>() {
		@Override
		public Integer create(Nd dom, long address) {
			return dom.getDB().getInt(address);
		}

		@Override
		public int getRecordSize() {
			return Database.INT_SIZE;
		}

		@Override
		public Class<?> getElementClass() {
			return Integer.class;
		}
	};
}
