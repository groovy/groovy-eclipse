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
import org.eclipse.jdt.core.util.IParameterAnnotation;
import org.eclipse.jdt.core.util.IRuntimeInvisibleParameterAnnotationsAttribute;

/**
 * Default implementation of IRuntimeInvisibleParameterAnnotations
 */
public class RuntimeInvisibleParameterAnnotationsAttribute
	extends ClassFileAttribute
	implements IRuntimeInvisibleParameterAnnotationsAttribute {

	private static final IParameterAnnotation[] NO_ENTRIES = new IParameterAnnotation[0];
	private IParameterAnnotation[] parameterAnnotations;
	private int parametersNumber;

	/**
	 * Constructor for RuntimeVisibleParameterAnnotations.
	 * @param classFileBytes
	 * @param constantPool
	 * @param offset
	 * @throws ClassFormatException
	 */
	public RuntimeInvisibleParameterAnnotationsAttribute(
		byte[] classFileBytes,
		IConstantPool constantPool,
		int offset)
		throws ClassFormatException {
		super(classFileBytes, constantPool, offset);
		final int length = u1At(classFileBytes, 6, offset);
		this.parametersNumber = length;
		if (length != 0) {
			int readOffset = 7;
			this.parameterAnnotations = new IParameterAnnotation[length];
			for (int i = 0; i < length; i++) {
				ParameterAnnotation parameterAnnotation = new ParameterAnnotation(classFileBytes, constantPool, offset + readOffset);
				this.parameterAnnotations[i] = parameterAnnotation;
				readOffset += parameterAnnotation.sizeInBytes();
			}
		} else {
			this.parameterAnnotations = NO_ENTRIES;
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IRuntimeInvisibleParameterAnnotations#getAnnotations()
	 */
	public IParameterAnnotation[] getParameterAnnotations() {
		return this.parameterAnnotations;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.util.IRuntimeInvisibleParameterAnnotations#getParametersNumber()
	 */
	public int getParametersNumber() {
		return this.parametersNumber;
	}
}
