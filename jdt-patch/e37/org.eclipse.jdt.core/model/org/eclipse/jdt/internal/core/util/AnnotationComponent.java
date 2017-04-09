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
import org.eclipse.jdt.core.util.IAnnotationComponent;
import org.eclipse.jdt.core.util.IAnnotationComponentValue;
import org.eclipse.jdt.core.util.IConstantPool;
import org.eclipse.jdt.core.util.IConstantPoolConstant;
import org.eclipse.jdt.core.util.IConstantPoolEntry;

/**
 * Default implementation of IAnnotationComponent
 */
public class AnnotationComponent extends ClassFileStruct implements IAnnotationComponent {

	private int componentNameIndex;
	private char[] componentName;
	private IAnnotationComponentValue componentValue;
	private int readOffset;

	public AnnotationComponent(
			byte[] classFileBytes,
			IConstantPool constantPool,
			int offset) throws ClassFormatException {
		final int nameIndex = u2At(classFileBytes, 0, offset);
		this.componentNameIndex = nameIndex;
		if (nameIndex != 0) {
			IConstantPoolEntry constantPoolEntry = constantPool.decodeEntry(nameIndex);
			if (constantPoolEntry.getKind() != IConstantPoolConstant.CONSTANT_Utf8) {
				throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
			}
			this.componentName = constantPoolEntry.getUtf8Value();
		}
		this.readOffset = 2;
		AnnotationComponentValue value = new AnnotationComponentValue(classFileBytes, constantPool, offset + this.readOffset);
		this.componentValue = value;
		this.readOffset += value.sizeInBytes();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotationComponent#getComponentNameIndex()
	 */
	public int getComponentNameIndex() {
		return this.componentNameIndex;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotationComponent#getComponentName()
	 */
	public char[] getComponentName() {
		return this.componentName;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IAnnotationComponent#getComponentValue()
	 */
	public IAnnotationComponentValue getComponentValue() {
		return this.componentValue;
	}

	int sizeInBytes() {
		return this.readOffset;
	}
}
