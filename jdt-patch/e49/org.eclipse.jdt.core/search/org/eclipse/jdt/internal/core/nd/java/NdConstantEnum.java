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

import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldString;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

public final class NdConstantEnum extends NdConstant {
	public static final FieldManyToOne<NdTypeSignature> ENUM_TYPE;
	public static final FieldString ENUM_VALUE;

	@SuppressWarnings("hiding")
	public static StructDef<NdConstantEnum> type;

	static {
		type = StructDef.create(NdConstantEnum.class, NdConstant.type);
		ENUM_TYPE = FieldManyToOne.create(type, NdTypeSignature.USED_AS_ENUM_CONSTANT);
		ENUM_VALUE = type.addString();
		type.done();
	}

	public NdConstantEnum(Nd nd, long address) {
		super(nd, address);
	}

	protected NdConstantEnum(Nd nd) {
		super(nd);
	}

	public static NdConstantEnum create(NdTypeSignature enumType, String enumValue) {
		NdConstantEnum result = new NdConstantEnum(enumType.getNd());
		result.setEnumType(enumType);
		result.setEnumValue(enumValue);
		return result;
	}

	public void setEnumType(NdTypeSignature enumType) {
		ENUM_TYPE.put(getNd(), this.address, enumType);
	}

	public void setEnumValue(String enumType) {
		ENUM_VALUE.put(getNd(), this.address, enumType);
	}

	public NdTypeSignature getType() {
		return ENUM_TYPE.get(getNd(), this.address);
	}

	public char[] getValue() {
		return ENUM_VALUE.get(getNd(), this.address).getChars();
	}

	@Override
	public Constant getConstant() {
		return null;
	}
}
