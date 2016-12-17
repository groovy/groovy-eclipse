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
package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.StructDef;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;

/**
 * Represents the bound on a generic parameter (a ClassBound or InterfaceBound in
 * the sense of the Java VM spec Java SE 8 Edition, section 4.7.9.1).
 */
public class NdTypeBound extends NdNode {
	public static final FieldManyToOne<NdTypeParameter> PARENT;
	public static final FieldManyToOne<NdTypeSignature> TYPE;

	@SuppressWarnings("hiding")
	public static final StructDef<NdTypeBound> type;

	static {
		type = StructDef.create(NdTypeBound.class, NdNode.type);
		PARENT = FieldManyToOne.createOwner(type, NdTypeParameter.BOUNDS);
		TYPE = FieldManyToOne.create(type, NdTypeSignature.USED_AS_TYPE_BOUND);

		type.done();
	}

	public NdTypeBound(Nd nd, long address) {
		super(nd, address);
	}

	public NdTypeBound(NdTypeParameter parent, NdTypeSignature signature) {
		super(parent.getNd());

		PARENT.put(getNd(), this.address, parent);
		TYPE.put(getNd(), this.address, signature);
	}

	public NdTypeParameter getParent() {
		return PARENT.get(getNd(), this.address);
	}

	public NdTypeSignature getType() {
		return TYPE.get(getNd(), this.address);
	}

	public void getSignature(CharArrayBuffer result) {
		result.append(':');
		getType().getSignature(result);
	}
}
