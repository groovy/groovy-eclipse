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

public class NdMethodException extends NdNode {

	public static final FieldManyToOne<NdMethod> PARENT;
	public static final FieldManyToOne<NdTypeSignature> EXCEPTION_TYPE;

	@SuppressWarnings("hiding")
	public static StructDef<NdMethodException> type;

	static {
		type = StructDef.create(NdMethodException.class, NdNode.type);
		PARENT = FieldManyToOne.createOwner(type, NdMethod.EXCEPTIONS);
		EXCEPTION_TYPE = FieldManyToOne.create(type, NdTypeSignature.USED_AS_EXCEPTION);
		type.done();
	}

	public NdMethodException(Nd nd, long address) {
		super(nd, address);
	}

	public NdMethodException(NdMethod method, NdTypeSignature createTypeSignature) {
		super(method.getNd());

		PARENT.put(getNd(), this.address, method);
		EXCEPTION_TYPE.put(getNd(), this.address, createTypeSignature);
	}

	public NdTypeSignature getExceptionType() {
		return EXCEPTION_TYPE.get(getNd(), this.address);
	}

	public NdMethod getParent() {
		return PARENT.get(getNd(), this.address);
	}

	public String toString() {
		try {
			return getExceptionType().toString();
		} catch (RuntimeException e) {
			// This is called most often from the debugger, so we want to return something meaningful even
			// if the code is buggy, the database is corrupt, or we don't have a read lock.
			return super.toString();
		}
	}
}
