/*******************************************************************************
 * Copyright (c) 2012, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *        Andy Clement - Contributions for
 *                          Bug 383624 - [1.8][compiler] Revive code generation support for type annotations (from Olivier's work)
 *******************************************************************************/
package org.eclipse.jdt.internal.core.util;

import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.core.util.IConstantPool;
import org.eclipse.jdt.core.util.ILocalVariableReferenceInfo;


public class LocalVariableReferenceInfo extends ClassFileStruct implements ILocalVariableReferenceInfo {

	private int startPC;
	private int length;
	private int index;

	/**
	 * Constructor for LocalVariableTableEntry.
	 *
	 * @param classFileBytes
	 * @param constantPool
	 * @param offset
	 * @throws ClassFormatException
	 */
	public LocalVariableReferenceInfo(
			byte[] classFileBytes,
			IConstantPool constantPool,
			int offset) throws ClassFormatException {
		this.startPC = u2At(classFileBytes, 0, offset);
		this.length = u2At(classFileBytes, 2, offset);
		this.index = u2At(classFileBytes, 4, offset);
	}

	/**
	 * @see ILocalVariableReferenceInfo#getStartPC()
	 */
	public int getStartPC() {
		return this.startPC;
	}

	/**
	 * @see ILocalVariableReferenceInfo#getLength()
	 */
	public int getLength() {
		return this.length;
	}

	/**
	 * @see ILocalVariableReferenceInfo#getIndex()
	 */
	public int getIndex() {
		return this.index;
	}
}
