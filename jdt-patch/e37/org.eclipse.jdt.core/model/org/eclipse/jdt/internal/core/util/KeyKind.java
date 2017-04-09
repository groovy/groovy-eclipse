/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.util;

public class KeyKind extends BindingKeyParser {

	public static final int F_TYPE = 0x0001;
	public static final int F_METHOD = 0x0002;
	public static final int F_FIELD = 0x0004;
	public static final int F_TYPE_PARAMETER = 0x0008;
	public static final int F_LOCAL_VAR = 0x0010;
	public static final int F_MEMBER = 0x0020;
	public static final int F_LOCAL = 0x0040;
	public static final int F_PARAMETERIZED_TYPE = 0x0080;
	public static final int F_RAW_TYPE = 0x0100;
	public static final int F_WILDCARD_TYPE = 0x0200;
	public static final int F_PARAMETERIZED_METHOD = 0x0400;
	public static final int F_CAPTURE = 0x0800;
	public static final int F_CONSTRUCTOR = 0x1000;

	public int flags = 0;
	private KeyKind innerKeyKind;

	public KeyKind(BindingKeyParser parser) {
		super(parser);
	}

	public KeyKind(String key) {
		super(key);
	}

	public void consumeBaseType(char[] baseTypeSig) {
		this.flags |= F_TYPE;
	}

	public void consumeCapture(int position) {
		this.flags |= F_CAPTURE;
	}

	public void consumeField(char[] fieldName) {
		this.flags |= F_FIELD;
	}

	public void consumeLocalType(char[] uniqueKey) {
		this.flags |= F_LOCAL;
	}

	public void consumeLocalVar(char[] varName, int occurrenceCount) {
		this.flags |= F_LOCAL_VAR;
	}

	public void consumeMemberType(char[] simpleTypeName) {
		this.flags |= F_MEMBER;
	}

	public void consumeMethod(char[] selector, char[] signature) {
		this.flags |= F_METHOD;
		if (selector.length == 0)
			this.flags |= F_CONSTRUCTOR;
	}

	public void consumeParameterizedGenericMethod() {
		this.flags |= F_PARAMETERIZED_METHOD;
	}

	public void consumeParameterizedType(char[] simpleTypeName, boolean isRaw) {
		this.flags |= isRaw ? F_RAW_TYPE : F_PARAMETERIZED_TYPE;
	}

	public void consumeParser(BindingKeyParser parser) {
		this.innerKeyKind = (KeyKind) parser;
	}

	public void consumeRawType() {
		this.flags |= F_RAW_TYPE;
	}

	public void consumeTopLevelType() {
		this.flags |= F_TYPE;
	}

	public void consumeTypeParameter(char[] typeParameterName) {
		this.flags |= F_TYPE_PARAMETER;
	}

	public void consumeTypeWithCapture() {
		this.flags = this.innerKeyKind.flags;
	}

	public void consumeWildCard(int kind) {
		this.flags |= F_WILDCARD_TYPE;
	}

	public BindingKeyParser newParser() {
		return new KeyKind(this);
	}
}
