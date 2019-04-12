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

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.field.FieldByte;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.StructDef;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;

public class NdTypeArgument extends NdNode {
	public static final FieldManyToOne<NdComplexTypeSignature> PARENT;
	public static final FieldManyToOne<NdTypeSignature> TYPE_SIGNATURE;
	public static final FieldByte WILDCARD;

	@SuppressWarnings("hiding")
	public static final StructDef<NdTypeArgument> type;

	static {
		type = StructDef.create(NdTypeArgument.class, NdNode.type);
		PARENT = FieldManyToOne.createOwner(type, NdComplexTypeSignature.TYPE_ARGUMENTS);
		TYPE_SIGNATURE = FieldManyToOne.create(type, NdTypeSignature.USED_AS_TYPE_ARGUMENT);
		WILDCARD = type.addByte();
		type.done();
	}

	public static final int WILDCARD_NONE = 0;
	public static final int WILDCARD_EXTENDS = 1;
	public static final int WILDCARD_SUPER = 2;
	public static final int WILDCARD_QUESTION = 3;

	public NdTypeArgument(Nd nd, long address) {
		super(nd, address);
	}

	public NdTypeArgument(Nd nd, NdComplexTypeSignature typeSignature) {
		super(nd);

		PARENT.put(nd, this.address, typeSignature);
	}

	/**
	 * Sets the wildcard to use, one of the WILDCARD_* constants.
	 *
	 * @param wildcard
	 */
	public void setWildcard(int wildcard) {
		WILDCARD.put(getNd(), this.address, (byte) wildcard);
	}

	public void setType(NdTypeSignature typeSignature) {
		TYPE_SIGNATURE.put(getNd(), this.address, typeSignature);
	}

	public int getWildcard() {
		return WILDCARD.get(getNd(), this.address);
	}

	public NdComplexTypeSignature getParent() {
		return PARENT.get(getNd(), this.address);
	}

	public NdTypeSignature getType() {
		return TYPE_SIGNATURE.get(getNd(), this.address);
	}

	public void getSignature(CharArrayBuffer result) {
		switch (getWildcard()) {
			case NdTypeArgument.WILDCARD_EXTENDS: result.append('-'); break;
			case NdTypeArgument.WILDCARD_QUESTION: result.append('*'); return;
			case NdTypeArgument.WILDCARD_SUPER: result.append('+'); break;
		}

		NdTypeSignature theType = getType();
		if (theType != null) {
			theType.getSignature(result);
		}
	}
}
