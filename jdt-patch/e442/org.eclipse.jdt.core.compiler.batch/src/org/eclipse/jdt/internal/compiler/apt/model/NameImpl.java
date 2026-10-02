/*******************************************************************************
 * Copyright (c) 2006, 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.model;

import javax.lang.model.element.Name;

/**
 * A String-based implementation of the type used to return strings in javax.lang.model.
 */
public class NameImpl implements Name {

	private final String _name;

	/** nullary constructor is prohibited */
	@SuppressWarnings("unused")
	private NameImpl()
	{
		this._name = null;
	}

	public NameImpl(CharSequence cs)
	{
		this._name = cs.toString();
	}

	public NameImpl(char[] chars)
	{
		this._name = String.valueOf(chars);
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.element.Name#contentEquals(java.lang.CharSequence)
	 */
	@Override
	public boolean contentEquals(CharSequence cs) {
		return this._name.equals(cs.toString());
	}

	/* (non-Javadoc)
	 * @see java.lang.CharSequence#charAt(int)
	 */
	@Override
	public char charAt(int index) {
		return this._name.charAt(index);
	}

	/* (non-Javadoc)
	 * @see java.lang.CharSequence#length()
	 */
	@Override
	public int length() {
		return this._name.length();
	}

	/* (non-Javadoc)
	 * @see java.lang.CharSequence#subSequence(int, int)
	 */
	@Override
	public CharSequence subSequence(int start, int end) {
		return this._name.subSequence(start, end);
	}

	@Override
	public String toString() {
		return this._name;
	}

	@Override
	public int hashCode() {
		return this._name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final NameImpl other = (NameImpl) obj;
		return this._name.equals(other._name);
	}

}
