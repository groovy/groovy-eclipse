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
package org.eclipse.jdt.internal.core.nd.java;

import java.util.List;
import java.util.function.Supplier;

import org.eclipse.jdt.internal.core.nd.DatabaseRef;
import org.eclipse.jdt.internal.core.nd.IReader;
import org.eclipse.jdt.internal.core.nd.Nd;

/**
 * Holds a reference to an NdType that can be retained while releasing and reacquiring a read lock.
 */
public final class TypeRef implements Supplier<NdType> {
	final DatabaseRef<NdType> ref;
	final char[] fileName;
	final char[] fieldDescriptor;
	final TypeSupplier typeSupplier = new TypeSupplier();
	private final class TypeSupplier implements Supplier<NdType> {
		public TypeSupplier() {
		}

		@Override
		public NdType get() {
			NdTypeId typeId = JavaIndex.getIndex(TypeRef.this.ref.getNd()).findType(TypeRef.this.fieldDescriptor);

			if (typeId == null) {
				return null;
			}

			List<NdType> implementations = typeId.getTypes();
			for (NdType next : implementations) {
				NdResourceFile nextResourceFile = next.getResourceFile();
				if (nextResourceFile.getLocation().compare(TypeRef.this.fileName, false) == 0) {
					if (nextResourceFile.isDoneIndexing()) {
						return next;
					}
				}
			}
			return null;
		}
	}

	private TypeRef(NdType type) {
		super();
		this.fieldDescriptor = type.getTypeId().getRawType().getFieldDescriptor().getChars();
		this.fileName = type.getResourceFile().getLocation().getChars();
		this.ref = new DatabaseRef<NdType>(type.getNd(), this.typeSupplier, type);
	}

	private TypeRef(Nd nd, char[] resourcePath, char[] fieldDescriptor) {
		super();
		this.fieldDescriptor = fieldDescriptor;
		this.fileName = resourcePath;
		this.ref = new DatabaseRef<NdType>(nd, this.typeSupplier);
	}

	public char[] getFieldDescriptor() {
		return this.fieldDescriptor;
	}

	public char[] getFileName() {
		return this.fileName;
	}

	/**
	 * Creates a {@link DatabaseRef} to the given {@link NdType}.
	 */
	public static TypeRef create(NdType type) {
		return new TypeRef(type);
	}

	/**
	 * Creates a {@link DatabaseRef} to the {@link NdType} with the given resource path and field descriptor.
	 */
	public static TypeRef create(Nd nd, char[] resourcePath, char[] fieldDescriptor) {
		return new TypeRef(nd, resourcePath, fieldDescriptor);
	}

	public IReader lock() {
		return this.ref.lock();
	}

	@Override
	public NdType get() {
		return this.ref.get();
	}
}
