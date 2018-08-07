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

import java.util.List;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.field.FieldByte;
import org.eclipse.jdt.internal.core.nd.field.FieldInt;
import org.eclipse.jdt.internal.core.nd.field.FieldList;
import org.eclipse.jdt.internal.core.nd.field.FieldLong;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldString;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

public class NdVariable extends NdBinding {
	public static final FieldManyToOne<NdTypeSignature> TYPE;
	public static final FieldInt VARIABLE_ID;
	public static final FieldManyToOne<NdMethod> DECLARING_METHOD;
	public static final FieldString NAME;
	public static final FieldOneToOne<NdConstant> CONSTANT;
	public static final FieldLong TAG_BITS;
	public static final FieldByte VARIABLE_FLAGS;
	public static final FieldList<NdAnnotation> ANNOTATIONS;
	public static final FieldList<NdTypeAnnotation> TYPE_ANNOTATIONS;

	@SuppressWarnings("hiding")
	public static StructDef<NdVariable> type;

	public static final byte FLG_GENERIC_SIGNATURE_PRESENT 	= 0x01;

	static {
		type = StructDef.create(NdVariable.class, NdBinding.type);
		TYPE = FieldManyToOne.create(type, NdTypeSignature.VARIABLES_OF_TYPE);
		VARIABLE_ID = type.addInt();
		DECLARING_METHOD = FieldManyToOne.create(type, NdMethod.DECLARED_VARIABLES);
		NAME = type.addString();
		CONSTANT = FieldOneToOne.create(type, NdConstant.type, NdConstant.PARENT_VARIABLE);
		TAG_BITS = type.addLong();
		VARIABLE_FLAGS = type.addByte();
		ANNOTATIONS = FieldList.create(type, NdAnnotation.type);
		TYPE_ANNOTATIONS = FieldList.create(type, NdTypeAnnotation.type);
		type.done();
	}

	public NdVariable(Nd nd, long bindingRecord) {
		super(nd, bindingRecord);
	}

	public boolean hasVariableFlag(int toTest) {
		return (VARIABLE_FLAGS.get(getNd(), this.address) & toTest) != 0;
	}

	public void setVariableFlag(byte toSet) {
		int newFlags = VARIABLE_FLAGS.get(getNd(), this.address) | toSet;
		VARIABLE_FLAGS.put(getNd(), this.address, (byte)newFlags);
	}

	public void setName(char[] name) {
		NAME.put(getNd(), this.address, name);
	}

	public IString getName() {
		return NAME.get(getNd(), this.address);
	}

	public void setType(NdTypeSignature typeId) {
		TYPE.put(getNd(), this.address, typeId);
	}

	public void setConstant(NdConstant constant) {
		CONSTANT.put(getNd(), this.address, constant);
	}

	public NdConstant getConstant() {
		return CONSTANT.get(getNd(), this.address);
	}

	public NdTypeSignature getType() {
		return TYPE.get(getNd(), this.address);
	}

	public long getTagBits() {
		return TAG_BITS.get(getNd(), this.address);
	}

	public void setTagBits(long tagBits) {
		TAG_BITS.put(getNd(), this.address, tagBits);
	}

	public List<NdTypeAnnotation> getTypeAnnotations() {
		return TYPE_ANNOTATIONS.asList(getNd(), this.address);
	}

	public List<NdAnnotation> getAnnotations() {
		return ANNOTATIONS.asList(getNd(), this.address);
	}

	public NdAnnotation createAnnotation() {
		return ANNOTATIONS.append(this.getNd(), this.getAddress());
	}

	public void allocateAnnotations(int length) {
		ANNOTATIONS.allocate(getNd(), getAddress(), length);
	}

	@Override
	public String toString() {
		try {
			StringBuilder result = new StringBuilder();
			NdTypeSignature localType = getType();
			if (localType != null) {
				result.append(localType.toString());
				result.append(" "); //$NON-NLS-1$
			}
			IString name = getName();
			if (name != null) {
				result.append(name.toString());
			}
			NdConstant constant = getConstant();
			if (constant != null) {
				result.append(" = "); //$NON-NLS-1$
				result.append(constant.toString());
			}
			return result.toString();
		} catch (RuntimeException e) {
			// This is called most often from the debugger, so we want to return something meaningful even
			// if the code is buggy, the database is corrupt, or we don't have a read lock.
			return super.toString();
		}
	}

	public NdTypeAnnotation createTypeAnnotation() {
		return TYPE_ANNOTATIONS.append(getNd(), getAddress());
	}

	public void allocateTypeAnnotations(int length) {
		TYPE_ANNOTATIONS.allocate(getNd(), getAddress(), length);
	}
}
