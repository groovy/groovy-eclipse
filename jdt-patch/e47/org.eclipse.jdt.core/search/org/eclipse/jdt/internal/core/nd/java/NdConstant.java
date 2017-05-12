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

import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToOne;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

public abstract class NdConstant extends NdNode {
	// Parent pointers. Only one will be non-null.
	// TODO(sxenos): Create something like a union to hold these, to eliminate this
	// sparse data
	public static final FieldManyToOne<NdConstantArray> PARENT_ARRAY;
	public static final FieldOneToOne<NdAnnotationValuePair> PARENT_ANNOTATION_VALUE;
	public static final FieldOneToOne<NdVariable> PARENT_VARIABLE;
	public static final FieldOneToOne<NdMethod> PARENT_METHOD;

	@SuppressWarnings("hiding")
	public static StructDef<NdConstant> type;

	static {
		type = StructDef.createAbstract(NdConstant.class, NdNode.type);
		PARENT_ARRAY = FieldManyToOne.createOwner(type, NdConstantArray.ELEMENTS);
		PARENT_ANNOTATION_VALUE = FieldOneToOne.createOwner(type, NdAnnotationValuePair.type,
				NdAnnotationValuePair.VALUE);
		PARENT_VARIABLE = FieldOneToOne.createOwner(type, NdVariable.type, NdVariable.CONSTANT);
		PARENT_METHOD = FieldOneToOne.createOwner(type, NdMethod.type, NdMethod.DEFAULT_VALUE);
		type.done();
	}

	public NdConstant(Nd nd, long address) {
		super(nd, address);
	}

	protected NdConstant(Nd nd) {
		super(nd);
	}

	public static NdConstant create(Nd nd, Constant constant) {
		if (constant == Constant.NotAConstant) {
			return null;
		}

		switch (constant.typeID()) {
			case TypeIds.T_boolean:
				return NdConstantBoolean.create(nd, constant.booleanValue());
			case TypeIds.T_byte:
				return NdConstantByte.create(nd, constant.byteValue());
			case TypeIds.T_char:
				return NdConstantChar.create(nd, constant.charValue());
			case TypeIds.T_double:
				return NdConstantDouble.create(nd, constant.doubleValue());
			case TypeIds.T_float:
				return NdConstantFloat.create(nd, constant.floatValue());
			case TypeIds.T_int:
				return NdConstantInt.create(nd, constant.intValue());
			case TypeIds.T_long:
				return NdConstantLong.create(nd, constant.longValue());
			case TypeIds.T_short:
				return NdConstantShort.create(nd, constant.shortValue());
			case TypeIds.T_JavaLangString:
				return NdConstantString.create(nd, constant.stringValue());
			default:
				throw new IllegalArgumentException("Unknown typeID() " + constant.typeID()); //$NON-NLS-1$
		}
	}

	public void setParent(NdConstantArray result) {
		PARENT_ARRAY.put(getNd(), this.address, result);
	}

	/**
	 * Returns the {@link Constant} corresponding to the value of this {@link NdConstant} or null if the receiver
	 * corresponds to a {@link Constant}.
	 */
	public abstract Constant getConstant();

	public String toString() {
		try {
			return getConstant().toString();
		} catch (RuntimeException e) {
			// This is called most often from the debugger, so we want to return something meaningful even
			// if the code is buggy, the database is corrupt, or we don't have a read lock.
			return super.toString();
		}
	}
}
