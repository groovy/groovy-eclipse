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
package org.eclipse.jdt.internal.core.nd.field;

import org.eclipse.jdt.internal.core.nd.ITypeFactory;
import org.eclipse.jdt.internal.core.nd.Nd;

/**
 * Used to represent a single field of an object stored in the database. Objects 
 * which store themselves in the database should store a set of static final
 * FieldDefinitions at the top of their class definition to indicate their memory map.
 * This serves as a standard way to document the memory map for such structs, provides
 * access to the field offsets, and provides a convenience getter.
 * <p>
 * There are two ways to use this. Callers can either use the "get" method to access
 * the value of the field, or can use the public "offset" attribute to perform the reads
 * manually. The get function is more convenient but allocates objects and so should
 * probably not be used for frequently-accessed fields or primitive types that would
 * end up being autoboxed unnecessarily.
 * 
 * @param <T>
 */
public final class Field<T> implements IField, IDestructableField {
	private int offset;
	public final ITypeFactory<T> factory;

	public Field(ITypeFactory<T> objectFactory) {
		this.factory = objectFactory;
	}

	public T get(Nd nd, long address) {
		return this.factory.create(nd, address + this.offset);
	}

	public boolean hasDestructor() {
		return this.factory.hasDestructor();
	}

	@Override
	public void destruct(Nd nd, long address) {
		this.factory.destruct(nd, address + this.offset);
	}

	@Override
	public void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public int getRecordSize() {
		return this.factory.getRecordSize();
	}
}
