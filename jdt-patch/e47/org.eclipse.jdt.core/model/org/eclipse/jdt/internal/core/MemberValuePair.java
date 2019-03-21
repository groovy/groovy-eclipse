/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.internal.core.util.Util;

public class MemberValuePair implements IMemberValuePair {

	String memberName;
	public Object value;
	public int valueKind = K_UNKNOWN;

	public MemberValuePair(String memberName) {
		this.memberName = memberName;
	}

	public MemberValuePair(String memberName, Object value, int valueKind) {
		this(memberName);
		this.value = value;
		this.valueKind = valueKind;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof MemberValuePair)) {
			return false;
		}
		MemberValuePair other = (MemberValuePair) obj;
		return
			this.valueKind == other.valueKind
			&& this.memberName.equals(other.memberName)
			&& (this.value == other.value
				|| (this.value != null && this.value.equals(other.value))
				|| (this.value instanceof Object[] && other.value instanceof Object[] && Util.equalArraysOrNull((Object[])this.value, (Object[]) other.value)));
	}

	public String getMemberName() {
		return this.memberName;
	}

	public Object getValue() {
		return this.value;
	}

	public int getValueKind() {
		return this.valueKind;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.memberName == null) ? 0 : this.memberName.hashCode());
		result = prime * result + ((this.value == null) ? 0 : this.value.hashCode());
		result = prime * result + this.valueKind;
		return result;
	}
}
