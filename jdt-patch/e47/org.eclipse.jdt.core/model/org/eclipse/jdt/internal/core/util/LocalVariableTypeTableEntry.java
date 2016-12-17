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
import org.eclipse.jdt.core.util.IConstantPoolConstant;
import org.eclipse.jdt.core.util.IConstantPoolEntry;
import org.eclipse.jdt.core.util.ILocalVariableTypeTableEntry;

/**
 * Default implementation of ILocalVariableTypeTableEntry
 */
public class LocalVariableTypeTableEntry extends ClassFileStruct implements ILocalVariableTypeTableEntry {

	private int startPC;
	private int length;
	private int nameIndex;
	private int signatureIndex;
	private char[] name;
	private char[] signature;
	private int index;

	/**
	 * Constructor for LocalVariableTypeTableEntry.
	 *
	 * @param classFileBytes
	 * @param constantPool
	 * @param offset
	 * @throws ClassFormatException
	 */
	public LocalVariableTypeTableEntry(
		byte[] classFileBytes,
		IConstantPool constantPool,
		int offset) throws ClassFormatException {
			this.startPC = u2At(classFileBytes, 0, offset);
			this.length = u2At(classFileBytes, 2, offset);
			this.nameIndex = u2At(classFileBytes, 4, offset);
			this.signatureIndex = u2At(classFileBytes, 6, offset);
			this.index = u2At(classFileBytes, 8, offset);
			IConstantPoolEntry constantPoolEntry = constantPool.decodeEntry(this.nameIndex);
			if (constantPoolEntry.getKind() != IConstantPoolConstant.CONSTANT_Utf8) {
				throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
			}
			this.name = constantPoolEntry.getUtf8Value();
			constantPoolEntry = constantPool.decodeEntry(this.signatureIndex);
			if (constantPoolEntry.getKind() != IConstantPoolConstant.CONSTANT_Utf8) {
				throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
			}
			this.signature = constantPoolEntry.getUtf8Value();
		}

	/**
	 * @see ILocalVariableTypeTableEntry#getStartPC()
	 */
	public int getStartPC() {
		return this.startPC;
	}

	/**
	 * @see ILocalVariableTypeTableEntry#getLength()
	 */
	public int getLength() {
		return this.length;
	}

	/**
	 * @see ILocalVariableTypeTableEntry#getNameIndex()
	 */
	public int getNameIndex() {
		return this.nameIndex;
	}

	/**
	 * @see ILocalVariableTypeTableEntry#getSignatureIndex()
	 */
	public int getSignatureIndex() {
		return this.signatureIndex;
	}

	/**
	 * @see ILocalVariableTypeTableEntry#getIndex()
	 */
	public int getIndex() {
		return this.index;
	}

	/**
	 * @see ILocalVariableTypeTableEntry#getName()
	 */
	public char[] getName() {
		return this.name;
	}

	/**
	 * @see ILocalVariableTypeTableEntry#getSignature()
	 */
	public char[] getSignature() {
		return this.signature;
	}

}
