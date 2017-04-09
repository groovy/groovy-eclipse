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

import org.eclipse.jdt.core.util.IConstantPool;
import org.eclipse.jdt.core.util.IConstantPoolConstant;
import org.eclipse.jdt.core.util.IConstantPoolEntry;

/**
 * Default implementation of IConstantPool.
 */
public class ConstantPool extends ClassFileStruct implements IConstantPool {

	private int constantPoolCount;
	private int[] constantPoolOffset;
	private byte[] classFileBytes;

	ConstantPool(byte[] reference, int[] constantPoolOffset) {
		this.constantPoolCount = constantPoolOffset.length;
		this.constantPoolOffset = constantPoolOffset;
		this.classFileBytes = reference;
	}

	/**
	 * @see IConstantPool#decodeEntry(int)
	 */
	public IConstantPoolEntry decodeEntry(int index) {
		ConstantPoolEntry constantPoolEntry = new ConstantPoolEntry();
		constantPoolEntry.reset();
		int kind = getEntryKind(index);
		constantPoolEntry.setKind(kind);
		switch(kind) {
			case IConstantPoolConstant.CONSTANT_Class :
				constantPoolEntry.setClassInfoNameIndex(u2At(this.classFileBytes,  1, this.constantPoolOffset[index]));
				constantPoolEntry.setClassInfoName(getUtf8ValueAt(constantPoolEntry.getClassInfoNameIndex()));
				break;
			case IConstantPoolConstant.CONSTANT_Double :
				constantPoolEntry.setDoubleValue(doubleAt(this.classFileBytes, 1, this.constantPoolOffset[index]));
				break;
			case IConstantPoolConstant.CONSTANT_Fieldref :
				constantPoolEntry.setClassIndex(u2At(this.classFileBytes,  1, this.constantPoolOffset[index]));
				int declaringClassIndex = u2At(this.classFileBytes,  1, this.constantPoolOffset[constantPoolEntry.getClassIndex()]);
				constantPoolEntry.setClassName(getUtf8ValueAt(declaringClassIndex));
				constantPoolEntry.setNameAndTypeIndex(u2At(this.classFileBytes,  3, this.constantPoolOffset[index]));
				int fieldNameIndex = u2At(this.classFileBytes,  1, this.constantPoolOffset[constantPoolEntry.getNameAndTypeIndex()]);
				int fieldDescriptorIndex = u2At(this.classFileBytes,  3, this.constantPoolOffset[constantPoolEntry.getNameAndTypeIndex()]);
				constantPoolEntry.setFieldName(getUtf8ValueAt(fieldNameIndex));
				constantPoolEntry.setFieldDescriptor(getUtf8ValueAt(fieldDescriptorIndex));
				break;
			case IConstantPoolConstant.CONSTANT_Methodref :
			case IConstantPoolConstant.CONSTANT_InterfaceMethodref :
				constantPoolEntry.setClassIndex(u2At(this.classFileBytes,  1, this.constantPoolOffset[index]));
				declaringClassIndex = u2At(this.classFileBytes,  1, this.constantPoolOffset[constantPoolEntry.getClassIndex()]);
				constantPoolEntry.setClassName(getUtf8ValueAt(declaringClassIndex));
				constantPoolEntry.setNameAndTypeIndex(u2At(this.classFileBytes,  3, this.constantPoolOffset[index]));
				int methodNameIndex = u2At(this.classFileBytes,  1, this.constantPoolOffset[constantPoolEntry.getNameAndTypeIndex()]);
				int methodDescriptorIndex = u2At(this.classFileBytes,  3, this.constantPoolOffset[constantPoolEntry.getNameAndTypeIndex()]);
				constantPoolEntry.setMethodName(getUtf8ValueAt(methodNameIndex));
				constantPoolEntry.setMethodDescriptor(getUtf8ValueAt(methodDescriptorIndex));
				break;
			case IConstantPoolConstant.CONSTANT_Float :
				constantPoolEntry.setFloatValue(floatAt(this.classFileBytes, 1, this.constantPoolOffset[index]));
				break;
			case IConstantPoolConstant.CONSTANT_Integer :
				constantPoolEntry.setIntegerValue(i4At(this.classFileBytes, 1, this.constantPoolOffset[index]));
				break;
			case IConstantPoolConstant.CONSTANT_Long :
				constantPoolEntry.setLongValue(i8At(this.classFileBytes, 1, this.constantPoolOffset[index]));
				break;
			case IConstantPoolConstant.CONSTANT_NameAndType :
				constantPoolEntry.setNameAndTypeNameIndex(u2At(this.classFileBytes,  1, this.constantPoolOffset[index]));
				constantPoolEntry.setNameAndTypeDescriptorIndex(u2At(this.classFileBytes,  3, this.constantPoolOffset[index]));
				break;
			case IConstantPoolConstant.CONSTANT_String :
				constantPoolEntry.setStringIndex(u2At(this.classFileBytes,  1, this.constantPoolOffset[index]));
				constantPoolEntry.setStringValue(getUtf8ValueAt(constantPoolEntry.getStringIndex()));
				break;
			case IConstantPoolConstant.CONSTANT_Utf8 :
				constantPoolEntry.setUtf8Length(u2At(this.classFileBytes,  1, this.constantPoolOffset[index]));
				constantPoolEntry.setUtf8Value(getUtf8ValueAt(index));
		}
		return constantPoolEntry;
	}

	/**
	 * @see IConstantPool#getConstantPoolCount()
	 */
	public int getConstantPoolCount() {
		return this.constantPoolCount;
	}

	/**
	 * @see IConstantPool#getEntryKind(int)
	 */
	public int getEntryKind(int index) {
		return u1At(this.classFileBytes, 0, this.constantPoolOffset[index]);
	}

	private char[] getUtf8ValueAt(int utf8Index) {
		int utf8Offset = this.constantPoolOffset[utf8Index];
		return utf8At(this.classFileBytes, 0, utf8Offset + 3, u2At(this.classFileBytes, 0, utf8Offset + 1));
	}
}
