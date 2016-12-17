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
package org.eclipse.jdt.internal.core.nd.db;

/**
 * Represents an empty string.
 */
public class EmptyString implements IString {

	private int compareResult;
	private static EmptyString theEmptyString = new EmptyString();

	private EmptyString() {
		this.compareResult = "".compareTo("a");  //$NON-NLS-1$//$NON-NLS-2$
	}

	public static EmptyString create() {
		return theEmptyString;
	}

	@Override
	public long getRecord() {
		return 0;
	}

	@Override
	public int compare(IString string, boolean caseSensitive) {
		if (string.length() == 0) {
			return 0;
		}
		return this.compareResult;
	}

	@Override
	public int compare(String string, boolean caseSensitive) {
		if (string.length() == 0) {
			return 0;
		}
		return this.compareResult;
	}

	@Override
	public int compare(char[] chars, boolean caseSensitive) {
		if (chars.length == 0) {
			return 0;
		}
		return this.compareResult;
	}

	@Override
	public int compareCompatibleWithIgnoreCase(IString string) {
		if (string.length() == 0) {
			return 0;
		}
		return this.compareResult;
	}

	@Override
	public int compareCompatibleWithIgnoreCase(char[] chars) {
		if (chars.length == 0) {
			return 0;
		}
		return this.compareResult;
	}

	@Override
	public int comparePrefix(char[] name, boolean caseSensitive) {
		if (name.length == 0) {
			return 0;
		}
		return this.compareResult;
	}

	@Override
	public char[] getChars() {
		return new char[0];
	}

	@Override
	public String getString() {
		return ""; //$NON-NLS-1$
	}

	@Override
	public void delete() {
		// Can't be deleted
	}

	@Override
	public int length() {
		return 0;
	}
}
