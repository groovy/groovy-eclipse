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

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldShort;
import org.eclipse.jdt.internal.core.nd.field.StructDef;
import org.eclipse.jdt.internal.core.nd.util.CharArrayUtils;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;

public class NdMethod extends NdBinding {
	public static final FieldManyToOne<NdMethodId> METHOD_ID;
	public static final FieldShort METHOD_FLAGS;
	public static final FieldManyToOne<NdType> PARENT;
	public static final FieldOneToMany<NdVariable> DECLARED_VARIABLES;
	public static final FieldOneToMany<NdMethodParameter> PARAMETERS;
	public static final FieldOneToOne<NdConstant> DEFAULT_VALUE;
	public static final FieldOneToMany<NdMethodException> EXCEPTIONS;
	public static final FieldManyToOne<NdTypeSignature> RETURN_TYPE;
	public static final FieldOneToOne<NdMethodAnnotationData> ANNOTATION_DATA;

	@SuppressWarnings("hiding")
	public static final StructDef<NdMethod> type;

	static {
		type = StructDef.create(NdMethod.class, NdBinding.type);
		METHOD_ID = FieldManyToOne.create(type, NdMethodId.METHODS);
		METHOD_FLAGS = type.addShort();
		PARENT = FieldManyToOne.createOwner(type, NdType.METHODS);
		PARAMETERS = FieldOneToMany.create(type, NdMethodParameter.PARENT);
		DECLARED_VARIABLES = FieldOneToMany.create(type, NdVariable.DECLARING_METHOD);
		DEFAULT_VALUE = FieldOneToOne.create(type, NdConstant.class, NdConstant.PARENT_METHOD);
		EXCEPTIONS = FieldOneToMany.create(type, NdMethodException.PARENT);
		RETURN_TYPE = FieldManyToOne.create(type, NdTypeSignature.USED_AS_RETURN_TYPE);
		ANNOTATION_DATA = FieldOneToOne.create(type, NdMethodAnnotationData.class, NdMethodAnnotationData.METHOD);
		type.done();
	}

	public static final byte FLG_GENERIC_SIGNATURE_PRESENT = 0x0001;
	public static final byte FLG_THROWS_SIGNATURE_PRESENT = 0x0002;

	public NdMethod(Nd nd, long address) {
		super(nd, address);
	}

	public NdMethod(NdType parent) {
		super(parent.getNd());

		PARENT.put(getNd(), this.address, parent);
	}

	public NdMethodId getMethodId() {
		return METHOD_ID.get(getNd(), this.address);
	}

	/**
	 * Returns method parameter names that were not defined by the compiler.
	 */
	public char[][] getParameterNames() {
		List<NdMethodParameter> params = getMethodParameters();

		// Use index to count the "real" parameters.
		int index = 0;
		char[][] result = new char[params.size()][];
		for (int idx = 0; idx < result.length; idx++) {
			NdMethodParameter param = params.get(idx);
			if (!param.isCompilerDefined()) {
				result[index] = param.getName().getChars();
				index++;
			}
		}
		return CharArrayUtils.subarray(result, 0, index);
	}

	public List<NdMethodParameter> getMethodParameters() {
		return PARAMETERS.asList(getNd(), this.address);
	}

	public List<NdAnnotationInMethod> getAnnotations() {
		NdMethodAnnotationData annotationData = getAnnotationData();
		if (annotationData != null) {
			return annotationData.getAnnotations();
		}
		return Collections.emptyList();
	}

	public void setDefaultValue(NdConstant value) {
		DEFAULT_VALUE.put(getNd(), this.address, value);
	}

	public NdConstant getDefaultValue() {
		return DEFAULT_VALUE.get(getNd(), this.address);
	}

	public void setReturnType(NdTypeSignature createTypeSignature) {
		RETURN_TYPE.put(getNd(), this.address, createTypeSignature);
	}

	public void setMethodId(NdMethodId methodId) {
		METHOD_ID.put(getNd(), this.address, methodId);
	}

	public List<NdTypeAnnotationInMethod> getTypeAnnotations() {
		NdMethodAnnotationData annotationData = getAnnotationData();
		if (annotationData != null) {
			return annotationData.getTypeAnnotations();
		}
		return Collections.emptyList();
	}

	public List<NdMethodException> getExceptions() {
		return EXCEPTIONS.asList(getNd(), this.address);
	}

	/**
	 * Returns the return type for this method or null if the method returns void
	 */
	public NdTypeSignature getReturnType() {
		return RETURN_TYPE.get(getNd(), this.address);
	}

	public int getFlags() {
		return METHOD_FLAGS.get(getNd(), this.address);
	}

	public boolean hasAllFlags(int flags) {
		int ourFlags = getFlags();

		return (ourFlags & flags) == flags;
	}

	public void setFlags(int flags) {
		METHOD_FLAGS.put(getNd(), this.address, (short) (getFlags() | flags));
	}

	public void setTagBits(long bits) {
		if (bits != 0) {
			createAnnotationData().setTagBits(bits);
		} else {
			NdMethodAnnotationData annotationData = getAnnotationData();
			if (annotationData != null) {
				annotationData.setTagBits(bits);
			}
		}
	}

	public long getTagBits() {
		NdMethodAnnotationData annotations = getAnnotationData();
		if (annotations == null) {
			return 0;
		}
		return annotations.getTagBits();
	}

	public String toString() {
		try {
			CharArrayBuffer arrayBuffer = new CharArrayBuffer();
			arrayBuffer.append(getMethodId().getSelector());
			getGenericSignature(arrayBuffer, true);
			return arrayBuffer.toString();
		} catch (RuntimeException e) {
			// This is called most often from the debugger, so we want to return something meaningful even
			// if the code is buggy, the database is corrupt, or we don't have a read lock.
			return super.toString();
		}
	}

	public void getGenericSignature(CharArrayBuffer result, boolean includeExceptions) {
		NdTypeParameter.getSignature(result, getTypeParameters());

		result.append('(');
		for (NdMethodParameter next : getMethodParameters()) {
			// Compiler-defined arguments don't show up in the generic signature
			if (!next.isCompilerDefined()) {
				next.getType().getSignature(result);
			}
		}
		result.append(')');
		NdTypeSignature returnType = getReturnType();
		if (returnType == null) {
			result.append('V');
		} else {
			returnType.getSignature(result);
		}
		if (includeExceptions) {
			List<NdMethodException> exceptions = getExceptions();
			for (NdMethodException next : exceptions) {
				result.append('^');
				next.getExceptionType().getSignature(result);
			}
		}
	}

	/**
	 * Creates the {@link NdMethodAnnotationData} struct for this method if it does not already exist. Returns
	 * the existing or newly-created struct. 
	 */
	public NdMethodAnnotationData createAnnotationData() {
		NdMethodAnnotationData result = getAnnotationData();
		if (result == null) {
			result = new NdMethodAnnotationData(this);
		}
		return result;
	}

	private NdMethodAnnotationData getAnnotationData() {
		return ANNOTATION_DATA.get(getNd(), getAddress());
	}
}
