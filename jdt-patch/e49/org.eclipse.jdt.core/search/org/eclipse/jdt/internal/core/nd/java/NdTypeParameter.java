/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.java;

import java.util.List;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdStruct;
import org.eclipse.jdt.internal.core.nd.field.FieldByte;
import org.eclipse.jdt.internal.core.nd.field.FieldList;
import org.eclipse.jdt.internal.core.nd.field.FieldString;
import org.eclipse.jdt.internal.core.nd.field.StructDef;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;

/**
 * Represents a TypeParameter, as described in Section 4.7.9.1 of the java VM specification, Java SE 8 edititon.
 */
public class NdTypeParameter extends NdStruct {
	public static final FieldString IDENTIFIER;
	public static final FieldList<NdTypeBound> BOUNDS;
	public static final FieldByte TYPE_PARAMETER_FLAGS;

	public static final byte FLG_FIRST_BOUND_IS_A_CLASS = 0x01;

	@SuppressWarnings("hiding")
	public static final StructDef<NdTypeParameter> type;

	static {
		type = StructDef.create(NdTypeParameter.class, NdStruct.type);
		IDENTIFIER = type.addString();
		BOUNDS = FieldList.create(type, NdTypeBound.type);
		TYPE_PARAMETER_FLAGS = type.addByte();

		type.done();
	}

	public NdTypeParameter(Nd nd, long address) {
		super(nd, address);
	}

	public void setIdentifier(char[] identifier) {
		IDENTIFIER.put(getNd(), this.address, identifier);
	}

	public char[] getIdentifier() {
		return IDENTIFIER.get(getNd(), this.address).getChars();
	}

	public void setFirstBoundIsClass(boolean isClass) {
		setFlag(FLG_FIRST_BOUND_IS_A_CLASS, isClass);
	}

	public boolean isFirstBoundAClass() {
		return (TYPE_PARAMETER_FLAGS.get(getNd(), this.address) & FLG_FIRST_BOUND_IS_A_CLASS) != 0;
	}

	private void setFlag(byte flag, boolean value) {
		byte oldValue = TYPE_PARAMETER_FLAGS.get(getNd(), this.address);
		byte newValue;
		if (value) {
			newValue = (byte) (oldValue | flag);
		} else {
			newValue = (byte) (oldValue & ~flag);
		}
		TYPE_PARAMETER_FLAGS.put(getNd(), this.address, newValue);
	}

	public List<NdTypeBound> getBounds() {
		return BOUNDS.asList(getNd(), this.address);
	}

	public void getSignature(CharArrayBuffer result) {
		result.append(getIdentifier());

		List<NdTypeBound> bounds = getBounds();

		// If none of the bounds are classes and there is at least one bound, then insert a double-colon
		// in the type signature.
		if (!bounds.isEmpty() && !isFirstBoundAClass()) {
			result.append(':');
		}

		for (NdTypeBound next : bounds) {
			next.getSignature(result);
		}
	}

	public static void getSignature(CharArrayBuffer buffer, List<NdTypeParameter> params) {
		if (!params.isEmpty()) {
			buffer.append('<');
			for (NdTypeParameter next : params) {
				next.getSignature(buffer);
			}
			buffer.append('>');
		}
	}

	public void createBound(NdTypeSignature boundSignature) {
		BOUNDS.append(getNd(), getAddress()).setType(boundSignature);
	}

	public void allocateBounds(int numBounds) {
		BOUNDS.allocate(getNd(), getAddress(), numBounds);
	}
}
