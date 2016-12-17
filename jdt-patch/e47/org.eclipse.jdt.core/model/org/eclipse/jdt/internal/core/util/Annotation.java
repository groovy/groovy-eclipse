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
import org.eclipse.jdt.core.util.IAnnotation;
import org.eclipse.jdt.core.util.IAnnotationComponent;
import org.eclipse.jdt.core.util.IConstantPool;
import org.eclipse.jdt.core.util.IConstantPoolConstant;
import org.eclipse.jdt.core.util.IConstantPoolEntry;

/**
 * Default implementation of IAnnotation
 */
public class Annotation extends ClassFileStruct implements IAnnotation {

	private static final IAnnotationComponent[] NO_ENTRIES = new IAnnotationComponent[0];

	private int typeIndex;
	private char[] typeName;
	private int componentsNumber;
	private IAnnotationComponent[] components;
	private int readOffset;

	/**
	 * Constructor for Annotation.
	 *
	 * @param classFileBytes
	 * @param constantPool
	 * @param offset
	 * @throws ClassFormatException
	 */
	public Annotation(
			byte[] classFileBytes,
			IConstantPool constantPool,
			int offset) throws ClassFormatException {

		final int index = u2At(classFileBytes, 0, offset);
		this.typeIndex = index;
		if (index != 0) {
			IConstantPoolEntry constantPoolEntry = constantPool.decodeEntry(index);
			if (constantPoolEntry.getKind() != IConstantPoolConstant.CONSTANT_Utf8) {
				throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
			}
			this.typeName = constantPoolEntry.getUtf8Value();
		} else {
			throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
		}
		final int length = u2At(classFileBytes, 2, offset);
		this.componentsNumber = length;
		this.readOffset = 4;
		if (length != 0) {
			this.components = new IAnnotationComponent[length];
			for (int i = 0; i < length; i++) {
				AnnotationComponent component = new AnnotationComponent(classFileBytes, constantPool, offset + this.readOffset);
				this.components[i] = component;
				this.readOffset += component.sizeInBytes();
			}
		} else {
			this.components = NO_ENTRIES;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotation#getTypeIndex()
	 */
	public int getTypeIndex() {
		return this.typeIndex;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotation#getComponentsNumber()
	 */
	public int getComponentsNumber() {
		return this.componentsNumber;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotation#getComponents()
	 */
	public IAnnotationComponent[] getComponents() {
		return this.components;
	}

	int sizeInBytes() {
		return this.readOffset;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotation#getTypeName()
	 */
	public char[] getTypeName() {
		return this.typeName;
	}
}
