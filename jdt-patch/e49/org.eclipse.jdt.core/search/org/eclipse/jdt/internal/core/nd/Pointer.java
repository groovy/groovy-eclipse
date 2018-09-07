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
package org.eclipse.jdt.internal.core.nd;

import org.eclipse.jdt.internal.core.nd.db.Database;

/**
 * Points to a concrete type, NOT one of its subclasses. This should not be used for node
 * pointers, since they are stored as a pointer to the base class. If you want a pointer to
 * a node, use a NodeFieldDefinition instead.
 */
public class Pointer<T> {
	private final Nd nd;
	private final long address;
	private ITypeFactory<T> targetFactory;

	public Pointer(Nd nd, long address, ITypeFactory<T> targetFactory) {
		this.nd = nd;
		this.address = address;
		this.targetFactory = targetFactory;
	}

	public T get() {
		long ptr = this.nd.getDB().getRecPtr(this.address);

		if (ptr == 0) {
			return null;
		}

		return this.targetFactory.create(this.nd, ptr);
	}

	public static <T> ITypeFactory<Pointer<T>> getFactory(final ITypeFactory<T> targetFactory) {
		if (NdNode.class.isAssignableFrom(targetFactory.getElementClass())) {
			throw new IllegalArgumentException("Don't use Pointer<T> for references to NdNode"); //$NON-NLS-1$
		}
		return new AbstractTypeFactory<Pointer<T>>() {
			@Override
			public Pointer<T> create(Nd dom, long address) {
				return new Pointer<T>(dom, address, targetFactory);
			}

			@Override
			public int getRecordSize() {
				return Database.PTR_SIZE;
			}

			@Override
			public Class<?> getElementClass() {
				return Pointer.class;
			}
		};
	}
}
