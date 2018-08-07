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
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.field.FieldInt;
import org.eclipse.jdt.internal.core.nd.field.FieldList;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldShort;
import org.eclipse.jdt.internal.core.nd.field.FieldString;
import org.eclipse.jdt.internal.core.nd.field.StructDef;
import org.eclipse.jdt.internal.core.nd.util.CharArrayUtils;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;

public class NdMethod extends NdBinding {
	public static final FieldString METHOD_NAME;
	public static final FieldShort METHOD_FLAGS;
	public static final FieldOneToMany<NdVariable> DECLARED_VARIABLES;
	public static final FieldList<NdMethodParameter> PARAMETERS;
	public static final FieldOneToOne<NdConstant> DEFAULT_VALUE;
	public static final FieldList<NdMethodException> EXCEPTIONS;
	public static final FieldManyToOne<NdTypeSignature> RETURN_TYPE;
	public static final FieldOneToOne<NdMethodAnnotationData> ANNOTATION_DATA;
	public static final FieldInt DECLARATION_POSITION;

	@SuppressWarnings("hiding")
	public static final StructDef<NdMethod> type;

	static {
		type = StructDef.create(NdMethod.class, NdBinding.type);
		METHOD_NAME = type.addString();
		METHOD_FLAGS = type.addShort();
		PARAMETERS = FieldList.create(type, NdMethodParameter.type);
		DECLARED_VARIABLES = FieldOneToMany.create(type, NdVariable.DECLARING_METHOD);
		DEFAULT_VALUE = FieldOneToOne.create(type, NdConstant.type, NdConstant.PARENT_METHOD);
		EXCEPTIONS = FieldList.create(type, NdMethodException.type);
		RETURN_TYPE = FieldManyToOne.create(type, NdTypeSignature.USED_AS_RETURN_TYPE);
		ANNOTATION_DATA = FieldOneToOne.create(type, NdMethodAnnotationData.type, NdMethodAnnotationData.METHOD);
		DECLARATION_POSITION = type.addInt();
		type.done();
	}

	public static final byte FLG_GENERIC_SIGNATURE_PRESENT = 0x0001;
	public static final byte FLG_THROWS_SIGNATURE_PRESENT = 0x0002;

	public NdMethod(Nd nd, long address) {
		super(nd, address);
	}

	public NdMethodParameter createNewParameter() {
		return PARAMETERS.append(getNd(), getAddress());
	}

	public void allocateParameters(int numParameters) {
		PARAMETERS.allocate(this.nd, this.address, numParameters);
	}

	public IString getMethodName() {
		return METHOD_NAME.get(getNd(), this.address);
	}

	public void setMethodName(char[] selectorAndDescriptor) {
		METHOD_NAME.put(getNd(), getAddress(), selectorAndDescriptor);
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

	public List<NdAnnotation> getAnnotations() {
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

	public List<NdTypeAnnotation> getTypeAnnotations() {
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

	@Override
	public String toString() {
		try {
			CharArrayBuffer arrayBuffer = new CharArrayBuffer();
			arrayBuffer.append(getSelector());
			getGenericSignature(arrayBuffer, true);
			return arrayBuffer.toString();
		} catch (RuntimeException e) {
			// This is called most often from the debugger, so we want to return something meaningful even
			// if the code is buggy, the database is corrupt, or we don't have a read lock.
			return super.toString();
		}
	}

	public char[] getSelector() {
		IString methodName = METHOD_NAME.get(getNd(), getAddress());
		char[] methodNameString = methodName.getChars();
		int bracketIndex = CharArrayUtils.indexOf('(', methodNameString);
		if (bracketIndex == -1) {
			bracketIndex = methodNameString.length;
		}
		return CharArrayUtils.subarray(methodNameString, 0, bracketIndex);
	}

	public boolean isConstructor() {
		return org.eclipse.jdt.internal.compiler.classfmt.JavaBinaryNames.isConstructor(getSelector());
	}

	public boolean isClInit() {
		return org.eclipse.jdt.internal.compiler.classfmt.JavaBinaryNames.isClinit(getSelector());
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

	public NdMethodException createException(NdTypeSignature createTypeSignature) {
		NdMethodException result = EXCEPTIONS.append(getNd(), getAddress());
		result.setExceptionType(createTypeSignature);
		return result;
	}

	public void allocateExceptions(int length) {
		EXCEPTIONS.allocate(this.nd, this.address, length);
	}

	public NdAnnotation createAnnotation() {
		return createAnnotationData().createAnnotation();
	}

	public NdTypeAnnotation createTypeAnnotation() {
		return createAnnotationData().createTypeAnnotation();
	}

	public void allocateAnnotations(int length) {
		if (length > 0) {
			createAnnotationData().allocateAnnotations(length);
		}
	}

	public void allocateTypeAnnotations(int length) {
		if (length > 0) {
			createAnnotationData().allocateTypeAnnotations(length);
		}
	}

	public void setDeclarationPosition(int position) {
		DECLARATION_POSITION.put(getNd(), getAddress(), position);
	}

	/**
	 * Returns the unique 0-based position of the method within the class it was
	 * declared in.
	 */
	public int getDeclarationPosition() {
		return DECLARATION_POSITION.get(getNd(), getAddress());
	}

	public char[] getMethodDescriptor() {
		char[] name = getMethodName().getChars();
		int descriptorStart = CharArrayUtils.indexOf('(', name, 0, name.length);
		return CharArrayUtils.subarray(name, descriptorStart, name.length);
	}
}
