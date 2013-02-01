/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
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
import org.eclipse.jdt.core.util.IBootstrapMethodsAttribute;
import org.eclipse.jdt.core.util.IBootstrapMethodsEntry;
import org.eclipse.jdt.core.util.IConstantPool;

/**
 * Default implementation of IBootstrapMethodsAttribute.
 */
public class BootstrapMethodsAttribute extends ClassFileAttribute implements IBootstrapMethodsAttribute {
	private static final IBootstrapMethodsEntry[] NO_ENTRIES = new IBootstrapMethodsEntry[0];

	private IBootstrapMethodsEntry[] entries;
	private int numberOfBootstrapMethods;

	/**
	 * Constructor for BootstrapMethodsAttribute.
	 * @param classFileBytes
	 * @param constantPool
	 * @param offset
	 * @throws ClassFormatException
	 */
	public BootstrapMethodsAttribute(
			byte[] classFileBytes,
			IConstantPool constantPool,
			int offset) throws ClassFormatException {
		super(classFileBytes, constantPool, offset);
		this.numberOfBootstrapMethods = u2At(classFileBytes, 6, offset);
		final int length = this.numberOfBootstrapMethods;
		if (length != 0) {
			int readOffset = 8;
			this.entries = new IBootstrapMethodsEntry[length];
			for (int i = 0; i < length; i++) {
				this.entries[i] = new BootstrapMethodsEntry(classFileBytes, constantPool, offset + readOffset);
				readOffset += 8;
			}
		} else {
			this.entries = NO_ENTRIES;
		}
	}

	/**
	 * @see IBootstrapMethodsAttribute#getBootstrapMethods()
	 */
	public IBootstrapMethodsEntry[] getBootstrapMethods() {
		return this.entries;
	}
	
	public int getBootstrapMethodsLength() {
		return this.numberOfBootstrapMethods;
	}
}
