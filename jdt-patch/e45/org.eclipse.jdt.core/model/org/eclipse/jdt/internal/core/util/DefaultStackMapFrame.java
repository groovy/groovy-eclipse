/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
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
import org.eclipse.jdt.core.util.IStackMapFrame;
import org.eclipse.jdt.core.util.IVerificationTypeInfo;

/**
 * Default implementation of IStackMapFrame
 */
public class DefaultStackMapFrame extends ClassFileStruct implements IStackMapFrame {
	private static final IVerificationTypeInfo[] EMPTY_LOCALS_OR_STACK_ITEMS = new IVerificationTypeInfo[0];

	private int readOffset;
	private int numberOfLocals;
	private int numberOfStackItems;
	private IVerificationTypeInfo[] locals;
	private IVerificationTypeInfo[] stackItems;
	private int offsetDelta;

	/**
	 * Constructor for StackMapFrame.
	 *
	 * @param classFileBytes
	 * @param constantPool
	 * @param offset
	 * @throws ClassFormatException
	 */
	public DefaultStackMapFrame(
			byte[] classFileBytes,
			IConstantPool constantPool,
			int offset) throws ClassFormatException {
		// FULL_FRAME
		this.offsetDelta = u2At(classFileBytes, 0, offset);
		int tempLocals = u2At(classFileBytes, 2, offset);
		this.numberOfLocals = tempLocals;
		this.readOffset = 4;
		if (tempLocals != 0) {
			this.locals = new IVerificationTypeInfo[tempLocals];
			for (int i = 0; i < tempLocals; i++) {
				VerificationInfo verificationInfo = new VerificationInfo(classFileBytes, constantPool, offset + this.readOffset);
				this.locals[i] = verificationInfo;
				this.readOffset += verificationInfo.sizeInBytes();
			}
		} else {
			this.locals = EMPTY_LOCALS_OR_STACK_ITEMS;
		}
		int tempStackItems = u2At(classFileBytes, this.readOffset, offset);
		this.readOffset += 2;
		this.numberOfStackItems = tempStackItems;
		if (tempStackItems != 0) {
			this.stackItems = new IVerificationTypeInfo[tempStackItems];
			for (int i = 0; i < tempStackItems; i++) {
				VerificationInfo verificationInfo = new VerificationInfo(classFileBytes, constantPool, offset + this.readOffset);
				this.stackItems[i] = verificationInfo;
				this.readOffset += verificationInfo.sizeInBytes();
			}
		} else {
			this.stackItems = EMPTY_LOCALS_OR_STACK_ITEMS;
		}
	}
	int sizeInBytes() {
		return this.readOffset;
	}
	public int getFrameType() {
		return 255; // full_frame
	}
	public IVerificationTypeInfo[] getLocals() {
		return this.locals;
	}
	public int getNumberOfLocals() {
		return this.numberOfLocals;
	}
	public int getNumberOfStackItems() {
		return this.numberOfStackItems;
	}
	public int getOffsetDelta() {
		return this.offsetDelta;
	}
	public IVerificationTypeInfo[] getStackItems() {
		return this.stackItems;
	}
}
