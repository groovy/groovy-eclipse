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

import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;
import org.eclipse.jdt.internal.compiler.impl.Constant;

public class IndexBinaryField implements IBinaryField {
	private int modifiers;
	private IBinaryAnnotation[] annotations;
	private IBinaryTypeAnnotation[] typeAnnotations;
	private Constant constant;
	private char[] genericSignature;
	private char[] name;
	private long tagBits;
	private char[] typeName;

	public IndexBinaryField(IBinaryAnnotation[] annotations, Constant constant, char[] genericSignature, int modifiers,
			char[] name, long tagBits, IBinaryTypeAnnotation[] typeAnnotations, char[] fieldDescriptor) {
		super();
		this.modifiers = modifiers;
		this.annotations = annotations;
		this.typeAnnotations = typeAnnotations;
		this.constant = constant;
		this.genericSignature = genericSignature;
		this.name = name;
		this.tagBits = tagBits;
		this.typeName = fieldDescriptor;
	}

	@Override
	public int getModifiers() {
		return this.modifiers;
	}

	@Override
	public IBinaryAnnotation[] getAnnotations() {
		return this.annotations;
	}

	@Override
	public IBinaryTypeAnnotation[] getTypeAnnotations() {
		return this.typeAnnotations;
	}

	@Override
	public Constant getConstant() {
		return this.constant;
	}

	@Override
	public char[] getGenericSignature() {
		return this.genericSignature;
	}

	@Override
	public char[] getName() {
		return this.name;
	}

	@Override
	public long getTagBits() {
		return this.tagBits;
	}

	@Override
	public char[] getTypeName() {
		return this.typeName;
	}
}
