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
import org.eclipse.jdt.core.util.IConstantPool;
import org.eclipse.jdt.core.util.IRuntimeVisibleAnnotationsAttribute;

/**
 * Default implementation of IRuntimeVisibleAnnotations
 */
public class RuntimeVisibleAnnotationsAttribute
	extends ClassFileAttribute
	implements IRuntimeVisibleAnnotationsAttribute {

	private static final IAnnotation[] NO_ENTRIES = new IAnnotation[0];
	private int annotationsNumber;
	private IAnnotation[] annotations;

	/**
	 * Constructor for RuntimeVisibleAnnotations.
	 * @param classFileBytes
	 * @param constantPool
	 * @param offset
	 * @throws ClassFormatException
	 */
	public RuntimeVisibleAnnotationsAttribute(
		byte[] classFileBytes,
		IConstantPool constantPool,
		int offset)
		throws ClassFormatException {
		super(classFileBytes, constantPool, offset);
		final int length = u2At(classFileBytes, 6, offset);
		this.annotationsNumber = length;
		if (length != 0) {
			int readOffset = 8;
			this.annotations = new IAnnotation[length];
			for (int i = 0; i < length; i++) {
				Annotation annotation = new Annotation(classFileBytes, constantPool, offset + readOffset);
				this.annotations[i] = annotation;
				readOffset += annotation.sizeInBytes();
			}
		} else {
			this.annotations = NO_ENTRIES;
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IRuntimeVisibleAnnotations#getAnnotations()
	 */
	public IAnnotation[] getAnnotations() {
		return this.annotations;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IRuntimeVisibleAnnotations#getAnnotationsNumber()
	 */
	public int getAnnotationsNumber() {
		return this.annotationsNumber;
	}
}
