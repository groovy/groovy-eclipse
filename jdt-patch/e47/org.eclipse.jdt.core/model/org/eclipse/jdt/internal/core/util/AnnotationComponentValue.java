/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.util;

import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.core.util.IAnnotation;
import org.eclipse.jdt.core.util.IAnnotationComponentValue;
import org.eclipse.jdt.core.util.IConstantPool;
import org.eclipse.jdt.core.util.IConstantPoolConstant;
import org.eclipse.jdt.core.util.IConstantPoolEntry;

/**
 * Default implementation of IAnnotationComponent
 */
public class AnnotationComponentValue extends ClassFileStruct implements IAnnotationComponentValue {
	private static final IAnnotationComponentValue[] NO_VALUES = new AnnotationComponentValue[0];

	private IAnnotationComponentValue[] annotationComponentValues;
	private IAnnotation annotationValue;
	private IConstantPoolEntry classInfo;
	private int classFileInfoIndex;
	private IConstantPoolEntry constantValue;
	private int constantValueIndex;
	private int enumConstantTypeNameIndex;
	private int enumConstantNameIndex;
	private char[] enumConstantTypeName;
	private char[] enumConstantName;

	private int readOffset;
	private int tag;
	private int valuesNumber;

	public AnnotationComponentValue(
			byte[] classFileBytes,
			IConstantPool constantPool,
			int offset) throws ClassFormatException {
		this.classFileInfoIndex = -1;
		this.constantValueIndex = -1;
		this.enumConstantTypeNameIndex = -1;
		this.enumConstantNameIndex = -1;
		final int t = u1At(classFileBytes, 0, offset);
		this.tag = t;
		this.readOffset = 1;
		switch(t) {
			case 'B' :
			case 'C' :
			case 'D' :
			case 'F' :
			case 'I' :
			case 'J' :
			case 'S' :
			case 'Z' :
			case 's' :
				final int constantIndex = u2At(classFileBytes, this.readOffset, offset);
				this.constantValueIndex = constantIndex;
				if (constantIndex != 0) {
					IConstantPoolEntry constantPoolEntry = constantPool.decodeEntry(constantIndex);
					switch(constantPoolEntry.getKind()) {
						case IConstantPoolConstant.CONSTANT_Long :
						case IConstantPoolConstant.CONSTANT_Float :
						case IConstantPoolConstant.CONSTANT_Double :
						case IConstantPoolConstant.CONSTANT_Integer :
						case IConstantPoolConstant.CONSTANT_Utf8 :
							break;
						default :
							throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
					}
					this.constantValue = constantPoolEntry;
				}
				this.readOffset += 2;
				break;
			case 'e' :
				int index = u2At(classFileBytes, this.readOffset, offset);
				this.enumConstantTypeNameIndex = index;
				if (index != 0) {
					IConstantPoolEntry constantPoolEntry = constantPool.decodeEntry(index);
					if (constantPoolEntry.getKind() != IConstantPoolConstant.CONSTANT_Utf8) {
						throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
					}
					this.enumConstantTypeName = constantPoolEntry.getUtf8Value();
				}
				this.readOffset += 2;
				index = u2At(classFileBytes, this.readOffset, offset);
				this.enumConstantNameIndex = index;
				if (index != 0) {
					IConstantPoolEntry constantPoolEntry = constantPool.decodeEntry(index);
					if (constantPoolEntry.getKind() != IConstantPoolConstant.CONSTANT_Utf8) {
						throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
					}
					this.enumConstantName = constantPoolEntry.getUtf8Value();
				}
				this.readOffset += 2;
				break;
			case 'c' :
				final int classFileIndex = u2At(classFileBytes, this.readOffset, offset);
				this.classFileInfoIndex = classFileIndex;
				if (classFileIndex != 0) {
					IConstantPoolEntry constantPoolEntry = constantPool.decodeEntry(classFileIndex);
					if (constantPoolEntry.getKind() != IConstantPoolConstant.CONSTANT_Utf8) {
						throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
					}
					this.classInfo = constantPoolEntry;
				}
				this.readOffset += 2;
				break;
			case '@' :
				Annotation annotation = new Annotation(classFileBytes, constantPool, this.readOffset + offset);
				this.annotationValue = annotation;
				this.readOffset += annotation.sizeInBytes();
				break;
			case '[' :
				final int numberOfValues = u2At(classFileBytes, this.readOffset, offset);
				this.valuesNumber = numberOfValues;
				this.readOffset += 2;
				if (numberOfValues != 0) {
					this.annotationComponentValues = new IAnnotationComponentValue[numberOfValues];
					for (int i = 0; i < numberOfValues; i++) {
						AnnotationComponentValue value = new AnnotationComponentValue(classFileBytes, constantPool, offset + this.readOffset);
						this.annotationComponentValues[i] = value;
						this.readOffset += value.sizeInBytes();
					}
				} else {
					this.annotationComponentValues = NO_VALUES;
				}
				break;
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotationComponentValue#getAnnotationComponentValues()
	 */
	public IAnnotationComponentValue[] getAnnotationComponentValues() {
		return this.annotationComponentValues;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotationComponentValue#getAnnotationValue()
	 */
	public IAnnotation getAnnotationValue() {
		return this.annotationValue;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotationComponentValue#getClassInfo()
	 */
	public IConstantPoolEntry getClassInfo() {
		return this.classInfo;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotationComponentValue#getClassInfoIndex()
	 */
	public int getClassInfoIndex() {
		return this.classFileInfoIndex;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotationComponentValue#getConstantValue()
	 */
	public IConstantPoolEntry getConstantValue() {
		return this.constantValue;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotationComponentValue#getConstantValueIndex()
	 */
	public int getConstantValueIndex() {
		return this.constantValueIndex;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotationComponentValue#getEnumConstantName()
	 */
	public char[] getEnumConstantName() {
		return this.enumConstantName;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotationComponentValue#getEnumConstantNameIndex()
	 */
	public int getEnumConstantNameIndex() {
		return this.enumConstantNameIndex;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotationComponentValue#getEnumConstantTypeName()
	 */
	public char[] getEnumConstantTypeName() {
		return this.enumConstantTypeName;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotationComponentValue#getEnumConstantTypeNameIndex()
	 */
	public int getEnumConstantTypeNameIndex() {
		return this.enumConstantTypeNameIndex;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotationComponentValue#getTag()
	 */
	public int getTag() {
		return this.tag;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotationComponentValue#getValuesNumber()
	 */
	public int getValuesNumber() {
		return this.valuesNumber;
	}

	int sizeInBytes() {
		return this.readOffset;
	}
}
