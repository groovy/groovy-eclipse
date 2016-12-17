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
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.field.FieldByte;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.nd.field.FieldString;
import org.eclipse.jdt.internal.core.nd.field.StructDef;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;

public class NdMethodParameter extends NdNode {
	public static final FieldManyToOne<NdMethod> PARENT;
	public static final FieldManyToOne<NdTypeSignature> ARGUMENT_TYPE;
	public static final FieldString NAME;
	public static final FieldOneToMany<NdAnnotationInMethodParameter> ANNOTATIONS;
	public static final FieldByte FLAGS;

	private static final byte FLG_COMPILER_DEFINED = 0x01;

	@SuppressWarnings("hiding")
	public static StructDef<NdMethodParameter> type;

	static {
		type = StructDef.create(NdMethodParameter.class, NdNode.type);
		PARENT = FieldManyToOne.create(type, NdMethod.PARAMETERS);
		ARGUMENT_TYPE = FieldManyToOne.create(type, NdTypeSignature.USED_AS_METHOD_ARGUMENT);
		NAME = type.addString();
		ANNOTATIONS = FieldOneToMany.create(type, NdAnnotationInMethodParameter.OWNER);
		FLAGS = type.addByte();
		type.done();
	}

	public NdMethodParameter(Nd nd, long address) {
		super(nd, address);
	}

	public NdMethodParameter(NdMethod parent, NdTypeSignature argumentType) {
		super(parent.getNd());

		PARENT.put(getNd(), this.address, parent);
		ARGUMENT_TYPE.put(getNd(), this.address, argumentType);
	}

	public NdTypeSignature getType() {
		return ARGUMENT_TYPE.get(getNd(), this.address);
	}

	public void setName(char[] name) {
		NAME.put(getNd(), this.address, name);
	}

	public IString getName() {
		return NAME.get(getNd(), this.address);
	}

	public List<NdAnnotationInMethodParameter> getAnnotations() {
		return ANNOTATIONS.asList(getNd(), this.address);
	}

	private void setFlag(byte flagConstant, boolean value) {
		int oldFlags = FLAGS.get(getNd(), this.address);
		int newFlags = ((oldFlags & ~flagConstant) | (value ? flagConstant : 0));
		FLAGS.put(getNd(), this.address, (byte) newFlags);
	}

	private boolean getFlag(byte flagConstant) {
		return (FLAGS.get(getNd(), this.address) & flagConstant) != 0;
	}

	public void setCompilerDefined(boolean isCompilerDefined) {
		setFlag(FLG_COMPILER_DEFINED, isCompilerDefined);
	}

	public boolean isCompilerDefined() {
		return getFlag(FLG_COMPILER_DEFINED);
	}

	public String toString() {
		try {
			CharArrayBuffer buf = new CharArrayBuffer();
			buf.append(getType().toString());
			buf.append(" "); //$NON-NLS-1$
			buf.append(getName().toString());
			return buf.toString();
		} catch (RuntimeException e) {
			// This is called most often from the debugger, so we want to return something meaningful even
			// if the code is buggy, the database is corrupt, or we don't have a read lock.
			return super.toString();
		}
	}
}
