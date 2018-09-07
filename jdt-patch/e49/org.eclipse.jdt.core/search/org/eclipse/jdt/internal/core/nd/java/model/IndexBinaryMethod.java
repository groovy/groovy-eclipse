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
package org.eclipse.jdt.internal.core.nd.java.model;

import org.eclipse.jdt.internal.compiler.classfmt.BinaryTypeFormatter;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;

public final class IndexBinaryMethod implements IBinaryMethod {
	private int modifiers;
	private boolean isConstructor;
	private char[][] argumentNames;
	private IBinaryAnnotation[] annotations;
	private Object defaultValue;
	private char[][] exceptionTypeNames;
	private char[] genericSignature;
	private char[] methodDescriptor;
	private IBinaryAnnotation[][] parameterAnnotations;
	private char[] selector;
	private long tagBits;
	private boolean isClInit;
	private IBinaryTypeAnnotation[] typeAnnotations;

	public static IndexBinaryMethod create() {
		return new IndexBinaryMethod();
	}

	public IndexBinaryMethod setModifiers(int modifiers) {
		this.modifiers = modifiers;
		return this;
	}

	public IndexBinaryMethod setIsConstructor(boolean isConstructor) {
		this.isConstructor = isConstructor;
		return this;
	}

	public IndexBinaryMethod setArgumentNames(char[][] argumentNames) {
		this.argumentNames = argumentNames;
		return this;
	}

	public IndexBinaryMethod setAnnotations(IBinaryAnnotation[] annotations) {
		this.annotations = annotations;
		return this;
	}

	public IndexBinaryMethod setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}

	public IndexBinaryMethod setExceptionTypeNames(char[][] exceptionTypeNames) {
		this.exceptionTypeNames = exceptionTypeNames;
		return this;
	}

	public IndexBinaryMethod setGenericSignature(char[] genericSignature) {
		this.genericSignature = genericSignature;
		return this;
	}

	public IndexBinaryMethod setMethodDescriptor(char[] methodDescriptor) {
		this.methodDescriptor = methodDescriptor;
		return this;
	}

	public IndexBinaryMethod setParameterAnnotations(IBinaryAnnotation[][] parameterAnnotations) {
		this.parameterAnnotations = parameterAnnotations;
		return this;
	}

	public IndexBinaryMethod setSelector(char[] selector) {
		this.selector = selector;
		return this;
	}

	public IndexBinaryMethod setTagBits(long tagBits) {
		this.tagBits = tagBits;
		return this;
	}

	public IndexBinaryMethod setIsClInit(boolean isClInit) {
		this.isClInit = isClInit;
		return this;
	}

	public IndexBinaryMethod setTypeAnnotations(IBinaryTypeAnnotation[] typeAnnotations) {
		this.typeAnnotations = typeAnnotations;
		return this;
	}

	@Override
	public int getModifiers() {
		return this.modifiers;
	}

	@Override
	public boolean isConstructor() {
		return this.isConstructor;
	}

	@Override
	public char[][] getArgumentNames() {
		return this.argumentNames;
	}

	@Override
	public IBinaryAnnotation[] getAnnotations() {
		return this.annotations;
	}

	@Override
	public Object getDefaultValue() {
		return this.defaultValue;
	}

	@Override
	public char[][] getExceptionTypeNames() {
		return this.exceptionTypeNames;
	}

	@Override
	public char[] getGenericSignature() {
		return this.genericSignature;
	}

	@Override
	public char[] getMethodDescriptor() {
		return this.methodDescriptor;
	}

	@Override
	public IBinaryAnnotation[] getParameterAnnotations(int index, char[] classFileName) {
		if (this.parameterAnnotations == null || this.parameterAnnotations.length <= index) {
			return null;
		}
		return this.parameterAnnotations[index];
	}

	@Override
	public int getAnnotatedParametersCount() {
		if (this.parameterAnnotations == null) {
			return 0;
		}
		return this.parameterAnnotations.length;
	}

	@Override
	public char[] getSelector() {
		return this.selector;
	}

	@Override
	public long getTagBits() {
		return this.tagBits;
	}

	@Override
	public boolean isClinit() {
		return this.isClInit;
	}

	@Override
	public IBinaryTypeAnnotation[] getTypeAnnotations() {
		return this.typeAnnotations;
	}

	@Override
	public String toString() {
		return BinaryTypeFormatter.methodToString(this);
	}
}
