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

import org.eclipse.jdt.internal.core.nd.ITypeFactory;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.db.Database;

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
public final class Field<T> extends BaseField implements IDestructableField {
	public final ITypeFactory<T> factory;

	public Field(ITypeFactory<T> objectFactory, String structName, int fieldNumber) {
		setFieldName("field " + fieldNumber + ", a " + getClass().getSimpleName() //$NON-NLS-1$//$NON-NLS-2$
				+ " in struct " + structName); //$NON-NLS-1$
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
	public int getRecordSize() {
		return this.factory.getRecordSize();
	}

	@Override
	public int getAlignment() {
		// This sort of field is almost always used for embedding NdStructs within
		// other data types. Since most NdStructs allow incoming record pointers, they need to
		// be properly aligned. If we ever want to use this sort of field for other data types
		// that don't require alignment, we may want to replace this with something smarter
		// that can figure out the correct alignment based on the requirements of the actual
		// data type.
		return Database.BLOCK_SIZE_DELTA;
	}

	/**
	 * Creates a new {@link Field} in the given struct with the given type.
	 *
	 * @param struct the struct that will contain the newly-created field (must not have had
	 * {@link StructDef#done()} called on it yet).
	 * @param fieldType the data type for the contents of the newly created field
	 * @return the newly-constructed field
	 */
	public static <T> Field<T> create(StructDef<?> struct, StructDef<T> fieldType) {
		Field<T> result = new Field<>(fieldType.getFactory(), struct.getStructName(), struct.getNumFields());
		struct.add(result);
		struct.addDestructableField(result);
		fieldType.addDependency(struct);
		return result;
	}
}
