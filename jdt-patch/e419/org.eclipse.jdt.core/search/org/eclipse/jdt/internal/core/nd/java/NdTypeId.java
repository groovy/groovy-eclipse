/*******************************************************************************
 * Copyright (c) 2016, 2017 Google, Inc and others.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.nd.field.FieldSearchKey;
import org.eclipse.jdt.internal.core.nd.field.StructDef;
import org.eclipse.jdt.internal.core.nd.util.CharArrayUtils;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;

public class NdTypeId extends NdTypeSignature {
	public static final FieldSearchKey<JavaIndex> FIELD_DESCRIPTOR;
	public static final FieldSearchKey<JavaIndex> SIMPLE_NAME;
	public static final FieldOneToMany<NdType> TYPES;
	public static final FieldOneToMany<NdComplexTypeSignature> USED_AS_COMPLEX_TYPE;
	public static final FieldOneToMany<NdType> DECLARED_TYPES;

	@SuppressWarnings("hiding")
	public static final StructDef<NdTypeId> type;

	private String fName;

	static {
		type = StructDef.create(NdTypeId.class, NdTypeSignature.type);
		FIELD_DESCRIPTOR = FieldSearchKey.create(type, JavaIndex.TYPES);
		SIMPLE_NAME = FieldSearchKey.create(type, JavaIndex.SIMPLE_INDEX);
		TYPES = FieldOneToMany.create(type, NdType.TYPENAME, 2);
		USED_AS_COMPLEX_TYPE = FieldOneToMany.create(type, NdComplexTypeSignature.RAW_TYPE);
		DECLARED_TYPES = FieldOneToMany.create(type, NdType.DECLARING_TYPE);
		type.useStandardRefCounting().done();
	}

	public NdTypeId(Nd nd, long address) {
		super(nd, address);
	}

	public NdTypeId(Nd nd, char[] fieldDescriptor) {
		super(nd);

		char[] simpleName = JavaNames.fieldDescriptorToJavaName(fieldDescriptor, false);
		FIELD_DESCRIPTOR.put(nd, this.address, fieldDescriptor);
		SIMPLE_NAME.put(nd, this.address, simpleName);
	}

	@Override
	public List<NdType> getSubTypes() {
		List<NdType> result = new ArrayList<>();
		result.addAll(super.getSubTypes());
		for (NdComplexTypeSignature next : getComplexTypes()) {
			result.addAll(next.getSubTypes());
		}
		return result;
	}

	public List<NdComplexTypeSignature> getComplexTypes() {
		return USED_AS_COMPLEX_TYPE.asList(getNd(), this.address);
	}

	public NdType findTypeByResourceAddress(long resourceAddress) {
		int size = TYPES.size(getNd(), this.address);
		for (int idx = 0; idx < size; idx++) {
			NdType next = TYPES.get(getNd(), this.address, idx);

			if (next.getResourceAddress() == resourceAddress) {
				return next;
			}
		}
		return null;
	}

	public List<NdType> getTypes() {
		return TYPES.asList(getNd(), this.address);
	}

	/**
	 * Returns the field descriptor.
	 */
	public IString getFieldDescriptor() {
		return FIELD_DESCRIPTOR.get(getNd(), this.address);
	}

	public char[] getFieldDescriptorWithoutTrailingSemicolon() {
		char[] fieldDescriptor = getFieldDescriptor().getChars();

		int end = fieldDescriptor.length;
		if (fieldDescriptor.length > 0 && fieldDescriptor[end - 1] == ';') {
			end--;
		}

		return CharArrayUtils.subarray(fieldDescriptor, 0, end);
	}

	public char[] getBinaryName() {
		return JavaNames.fieldDescriptorToBinaryName(getFieldDescriptor().getChars());
	}

	public IString getSimpleName() {
		return SIMPLE_NAME.get(getNd(), this.address);
	}

	public char[] getSimpleNameCharArray() {
		if (this.fName == null) {
			this.fName= getSimpleName().getString();
		}
		return this.fName.toCharArray();
	}

	public boolean hasFieldDescriptor(String name) {
		return this.getFieldDescriptor().compare(name, true) == 0;
	}

	public boolean hasSimpleName(String name) {
		if (this.fName != null)
			return this.fName.equals(name);

		return getSimpleName().toString().equals(name);
	}

	public void setSimpleName(String name) {
		if (Objects.equals(name, this.fName)) {
			return;
		}
		this.fName = name;
		SIMPLE_NAME.put(getNd(), this.address, name);
	}

	public List<NdType> getDeclaredTypes() {
		return DECLARED_TYPES.asList(getNd(), this.address);
	}

	@Override
	public NdTypeId getRawType() {
		return this;
	}

	@Override
	public void getSignature(CharArrayBuffer result, boolean includeTrailingSemicolon) {
		if (includeTrailingSemicolon) {
			result.append(getFieldDescriptor().getChars());
		} else {
			result.append(getFieldDescriptorWithoutTrailingSemicolon());
		}
	}

	@Override
	public boolean isTypeVariable() {
		return false;
	}

	@Override
	public List<NdTypeSignature> getDeclaringTypeChain() {
		return Collections.singletonList((NdTypeSignature)this);
	}

	@Override
	public NdTypeSignature getArrayDimensionType() {
		return null;
	}

	@Override
	public List<NdTypeArgument> getTypeArguments() {
		return Collections.emptyList();
	}

	@Override
	public boolean isArrayType() {
		return false;
	}
}
