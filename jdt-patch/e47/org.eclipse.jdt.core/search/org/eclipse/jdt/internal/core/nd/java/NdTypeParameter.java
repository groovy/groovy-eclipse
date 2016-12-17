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
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.field.FieldByte;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.nd.field.FieldString;
import org.eclipse.jdt.internal.core.nd.field.StructDef;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;

/**
 * Represents a TypeParameter, as described in Section 4.7.9.1 of the java VM specification, Java SE 8 edititon.
 */
public class NdTypeParameter extends NdNode {
	public static final FieldManyToOne<NdBinding> PARENT;
	public static final FieldString IDENTIFIER;
	public static final FieldOneToMany<NdTypeBound> BOUNDS;
	public static final FieldByte TYPE_PARAMETER_FLAGS;

	public static final byte FLG_FIRST_BOUND_IS_A_CLASS = 0x01;

	@SuppressWarnings("hiding")
	public static final StructDef<NdTypeParameter> type;

	static {
		type = StructDef.create(NdTypeParameter.class, NdNode.type);
		PARENT = FieldManyToOne.createOwner(type, NdBinding.TYPE_PARAMETERS);
		IDENTIFIER = type.addString();
		BOUNDS = FieldOneToMany.create(type, NdTypeBound.PARENT);
		TYPE_PARAMETER_FLAGS = type.addByte();

		type.done();
	}

	public NdTypeParameter(Nd nd, long address) {
		super(nd, address);
	}

	public NdTypeParameter(NdBinding parent, char[] identifier) {
		super(parent.getNd());

		PARENT.put(getNd(), this.address, parent);
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
}
