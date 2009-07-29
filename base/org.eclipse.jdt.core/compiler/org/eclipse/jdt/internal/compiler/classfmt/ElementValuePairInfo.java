/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.classfmt;

public class ElementValuePairInfo implements org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair {

	static final ElementValuePairInfo[] NoMembers = new ElementValuePairInfo[0];

	private char[] name;
	private Object value;

ElementValuePairInfo(char[] name, Object value) {
	this.name = name;
	this.value = value;
}
public char[] getName() {
	return this.name;
}
public Object getValue() {
	return this.value;
}
public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append(this.name);
	buffer.append('=');
	if (this.value instanceof Object[]) {
		final Object[] values = (Object[]) this.value;
		buffer.append('{');
		for (int i = 0, l = values.length; i < l; i++) {
			if (i > 0)
				buffer.append(", "); //$NON-NLS-1$
			buffer.append(values[i]);
		}
		buffer.append('}');
	} else {
		buffer.append(this.value);
	}
	return buffer.toString();
}
}
