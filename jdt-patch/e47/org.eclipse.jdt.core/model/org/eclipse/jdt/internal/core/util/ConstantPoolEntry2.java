/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.util;

import org.eclipse.jdt.core.util.IConstantPoolEntry2;

/**
 * Default implementation of IConstantPoolEntry
 *
 * @since 2.0
 */
public class ConstantPoolEntry2 extends ConstantPoolEntry implements IConstantPoolEntry2 {
	
	private int descriptorIndex;
	private int referenceKind;
	private int referenceIndex;
	private int bootstrapMethodAttributeIndex;
	
	public int getDescriptorIndex() {
		return this.descriptorIndex;
	}

	public int getReferenceKind() {
		return this.referenceKind;
	}

	public int getReferenceIndex() {
		return this.referenceIndex;
	}

	public int getBootstrapMethodAttributeIndex() {
		return this.bootstrapMethodAttributeIndex;
	}

	public void setDescriptorIndex(int descriptorIndex) {
		this.descriptorIndex = descriptorIndex;
	}

	public void setReferenceKind(int referenceKind) {
		this.referenceKind = referenceKind;
	}

	public void setReferenceIndex(int referenceIndex) {
		this.referenceIndex = referenceIndex;
	}

	public void setBootstrapMethodAttributeIndex(int bootstrapMethodAttributeIndex) {
		this.bootstrapMethodAttributeIndex = bootstrapMethodAttributeIndex;
	}
	
	public void reset() {
		super.reset();
		this.descriptorIndex = 0;
		this.referenceKind = 0;
		this.referenceIndex = 0;
		this.bootstrapMethodAttributeIndex = 0;
	}
}
