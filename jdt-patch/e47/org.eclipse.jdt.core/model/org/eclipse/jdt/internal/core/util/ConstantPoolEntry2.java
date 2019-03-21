/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.util;

import org.eclipse.jdt.core.util.IConstantPoolEntry3;

/**
 * Default implementation of IConstantPoolEntry2 and IConstantPoolEntry3.
 *
 * @since 2.0
 */
public class ConstantPoolEntry2 extends ConstantPoolEntry implements IConstantPoolEntry3 {

	private int descriptorIndex;
	private int referenceKind;
	private int referenceIndex;
	private int bootstrapMethodAttributeIndex;
	
	private int moduleIndex;
	private char[] moduleName;
	private int packageIndex;
	private char[] packageName;

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

	public int getModuleIndex() {
		return this.moduleIndex;
	}

	public char[] getModuleName() {
		return this.moduleName;
	}

	public int getPackageIndex() {
		return this.packageIndex;
	}

	public char[] getPackageName() {
		return this.packageName;
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
	
	public void setModuleIndex(int moduleIndex) {
		this.moduleIndex = moduleIndex;
	}

	public void setModuleName(char[] moduleName) {
		this.moduleName = moduleName;
	}

	public void setPackageIndex(int packageIndex) {
		this.packageIndex = packageIndex;
	}

	public void setPackageName(char[] packageName) {
		this.packageName = packageName;
	}

	public void reset() {
		super.reset();
		this.descriptorIndex = 0;
		this.referenceKind = 0;
		this.referenceIndex = 0;
		this.bootstrapMethodAttributeIndex = 0;
		this.moduleIndex = 0;
		this.moduleName = null;
		this.packageIndex = 0;
		this.packageName = null;
	}
}
