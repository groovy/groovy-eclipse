/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IJavaElement;

/** Element info for IOpenable elements. */
public class OpenableElementInfo extends JavaElementInfo {

	/**
	 * Is the structure of this element known
	 * @see IJavaElement#isStructureKnown()
	 */
	protected boolean isStructureKnown = false;

	/**
	 * @see IJavaElement#isStructureKnown()
	 */
	public boolean isStructureKnown() {
		return this.isStructureKnown;
	}
	
	/**
	 * Sets whether the structure of this element known
	 * @see IJavaElement#isStructureKnown()
	 */
	public void setIsStructureKnown(boolean newIsStructureKnown) {
		this.isStructureKnown = newIsStructureKnown;
	}
}
