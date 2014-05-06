/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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
import org.eclipse.jdt.core.util.IVerificationTypeInfo;

public class VerificationInfo extends ClassFileStruct implements IVerificationTypeInfo {

	private int tag;
	private int offset;
	private int constantPoolIndex;
	private char[] classTypeName;
	private int readOffset;

	public VerificationInfo(
			byte[] classFileBytes,
			IConstantPool constantPool,
			int offset) throws ClassFormatException {
		final int t = u1At(classFileBytes, 0, offset);
		this.tag = t;
		this.readOffset = 1;
		switch(t) {
			case IVerificationTypeInfo.ITEM_OBJECT :
				final int constantIndex = u2At(classFileBytes, 1, offset);
				this.constantPoolIndex = constantIndex;
				if (constantIndex != 0) {
					IConstantPoolEntry constantPoolEntry = constantPool.decodeEntry(constantIndex);
					if (constantPoolEntry.getKind() != IConstantPoolConstant.CONSTANT_Class) {
						throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
					}
					this.classTypeName = constantPoolEntry.getClassInfoName();
				}
				this.readOffset += 2;
				break;
			case IVerificationTypeInfo.ITEM_UNINITIALIZED :
				this.offset = u2At(classFileBytes, 1, offset);
				this.readOffset += 2;
		}
	}

	public int getTag() {
		return this.tag;
	}

	public int getOffset() {
		return this.offset;
	}

	public int getConstantPoolIndex() {
		return this.constantPoolIndex;
	}

	public char[] getClassTypeName() {
		return this.classTypeName;
	}

	public int sizeInBytes() {
		return this.readOffset;
	}
}
