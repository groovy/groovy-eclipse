/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.util;

import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.core.util.IConstantPool;
import org.eclipse.jdt.core.util.ILocalVariableAttribute;
import org.eclipse.jdt.core.util.ILocalVariableTableEntry;

/**
 * Default implementation of ILocalVariableAttribute.
 */
public class LocalVariableAttribute
	extends ClassFileAttribute
	implements ILocalVariableAttribute {

	private static final ILocalVariableTableEntry[] NO_ENTRIES = new ILocalVariableTableEntry[0];
	private int localVariableTableLength;
	private ILocalVariableTableEntry[] localVariableTable;

	/**
	 * Constructor for LocalVariableAttribute.
	 * @param classFileBytes
	 * @param constantPool
	 * @param offset
	 * @throws ClassFormatException
	 */
	public LocalVariableAttribute(
		byte[] classFileBytes,
		IConstantPool constantPool,
		int offset)
		throws ClassFormatException {
		super(classFileBytes, constantPool, offset);
		final int length = u2At(classFileBytes, 6, offset);
		this.localVariableTableLength = length;
		if (length != 0) {
			int readOffset = 8;
			this.localVariableTable = new ILocalVariableTableEntry[length];
			for (int i = 0; i < length; i++) {
				this.localVariableTable[i] = new LocalVariableTableEntry(classFileBytes, constantPool, offset + readOffset);
				readOffset += 10;
			}
		} else {
			this.localVariableTable = NO_ENTRIES;
		}
	}
	/**
	 * @see ILocalVariableAttribute#getLocalVariableTable()
	 */
	public ILocalVariableTableEntry[] getLocalVariableTable() {
		return this.localVariableTable;
	}

	/**
	 * @see ILocalVariableAttribute#getLocalVariableTableLength()
	 */
	public int getLocalVariableTableLength() {
		return this.localVariableTableLength;
	}

}
