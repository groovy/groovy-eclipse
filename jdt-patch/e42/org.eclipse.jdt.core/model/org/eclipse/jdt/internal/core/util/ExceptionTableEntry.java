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
import org.eclipse.jdt.core.util.IExceptionTableEntry;

/**
 * This class describes an entry in the exception table attribute according
 * to the JVM specifications.
 */
public class ExceptionTableEntry
	extends ClassFileStruct
	implements IExceptionTableEntry {

	private int startPC;
	private int endPC;
	private int handlerPC;
	private int catchTypeIndex;
	private char[] catchType;

	ExceptionTableEntry(byte[] classFileBytes, IConstantPool constantPool, int offset) throws ClassFormatException {
		this.startPC = u2At(classFileBytes, 0, offset);
		this.endPC = u2At(classFileBytes, 2, offset);
		this.handlerPC = u2At(classFileBytes, 4, offset);
		this.catchTypeIndex = u2At(classFileBytes, 6, offset);
		if (this.catchTypeIndex != 0) {
			IConstantPoolEntry constantPoolEntry = constantPool.decodeEntry(this.catchTypeIndex);
			if (constantPoolEntry.getKind() != IConstantPoolConstant.CONSTANT_Class) {
				throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
			}
			this.catchType = constantPoolEntry.getClassInfoName();
		}
	}
	/**
	 * @see IExceptionTableEntry#getStartPC()
	 */
	public int getStartPC() {
		return this.startPC;
	}

	/**
	 * @see IExceptionTableEntry#getEndPC()
	 */
	public int getEndPC() {
		return this.endPC;
	}

	/**
	 * @see IExceptionTableEntry#getHandlerPC()
	 */
	public int getHandlerPC() {
		return this.handlerPC;
	}

	/**
	 * @see IExceptionTableEntry#getCatchTypeIndex()
	 */
	public int getCatchTypeIndex() {
		return this.catchTypeIndex;
	}

	/**
	 * @see IExceptionTableEntry#getCatchType()
	 */
	public char[] getCatchType() {
		return this.catchType;
	}

}
