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
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * Represents one interface implemented by a specific type. This is an intermediate object between a {@link NdType} and
 * the {@link NdTypeId}s corresponding to its interfaces, which is necessary in order to implement the many-to-many
 * relationship between them.
 */
public class NdTypeInterface extends NdNode {
	public static final FieldManyToOne<NdType> APPLIES_TO;
	public static final FieldManyToOne<NdTypeSignature> IMPLEMENTS;

	@SuppressWarnings("hiding")
	public static StructDef<NdTypeInterface> type;

	static {
		type = StructDef.create(NdTypeInterface.class, NdNode.type);
		APPLIES_TO = FieldManyToOne.createOwner(type, NdType.INTERFACES);
		IMPLEMENTS = FieldManyToOne.create(type, NdTypeSignature.IMPLEMENTATIONS);
		type.done();
	}
	
	public NdTypeInterface(Nd nd, long address) {
		super(nd, address);
	}

	public NdTypeInterface(Nd nd, NdType targetType, NdTypeSignature makeTypeId) {
		super(nd);

		APPLIES_TO.put(nd, this.address, targetType);
		IMPLEMENTS.put(nd, this.address, makeTypeId);
	}

	public NdType getImplementation() {
		return APPLIES_TO.get(getNd(), this.address);
	}

	public NdTypeSignature getInterface() {
		return IMPLEMENTS.get(getNd(), this.address);
	}
}
