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
import org.eclipse.jdt.internal.core.nd.NdStruct;
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldString;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

public class NdAnnotationValuePair extends NdStruct {
	public static final FieldString NAME;
	public static final FieldOneToOne<NdConstant> VALUE;

	@SuppressWarnings("hiding")
	public static final StructDef<NdAnnotationValuePair> type;

	static {
		type = StructDef.create(NdAnnotationValuePair.class, NdStruct.type);
		NAME = type.addString();
		VALUE = FieldOneToOne.create(type, NdConstant.type, NdConstant.PARENT_ANNOTATION_VALUE);
		type.done();
	}

	public NdAnnotationValuePair(Nd nd, long address) {
		super(nd, address);
	}

	public IString getName() {
		return NAME.get(getNd(), this.address);
	}

	public void setName(char[] name) {
		NAME.put(getNd(), this.address, name);
	}

	/**
	 * Returns the value of this annotation or null if none
	 */
	public NdConstant getValue() {
		return VALUE.get(getNd(), this.address);
	}

	public void setValue(NdConstant value) {
		VALUE.put(getNd(), this.address, value);
	}
}
