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
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IJavaElement;

/** Element info for IOpenable elements. */
public class OpenableElementInfo extends JavaElementInfo {

	/**
	 * Collection of handles of immediate children of this
	 * object. This is an empty array if this element has
	 * no children.
	 */
	protected IJavaElement[] children = JavaElement.NO_ELEMENTS;
	
	/**
	 * Is the structure of this element known
	 * @see IJavaElement#isStructureKnown()
	 */
	protected boolean isStructureKnown = false;

	public void addChild(IJavaElement child) {
		int length = this.children.length;
		if (length == 0) {
			this.children = new IJavaElement[] {child};
		} else {
			for (int i = 0; i < length; i++) {
				if (this.children[i].equals(child))
					return; // already included
			}
			System.arraycopy(this.children, 0, this.children = new IJavaElement[length+1], 0, length);
			this.children[length] = child;
		}
	}

	public IJavaElement[] getChildren() {
		return this.children;
	}
	
	/**
	 * @see IJavaElement#isStructureKnown()
	 */
	public boolean isStructureKnown() {
		return this.isStructureKnown;
	}

	public void removeChild(IJavaElement child) {
		for (int i = 0, length = this.children.length; i < length; i++) {
			IJavaElement element = this.children[i];
			if (element.equals(child)) {
				if (length == 1) {
					this.children = JavaElement.NO_ELEMENTS;
				} else {
					IJavaElement[] newChildren = new IJavaElement[length-1];
					System.arraycopy(this.children, 0, newChildren , 0, i);
					if (i < length-1)
						System.arraycopy(this.children, i+1, newChildren, i, length-1-i);
					this.children = newChildren;
				}
				break;
			}
		}
	}

	public void setChildren(IJavaElement[] children) {
		this.children = children;
	}

	/**
	 * Sets whether the structure of this element known
	 * @see IJavaElement#isStructureKnown()
	 */
	public void setIsStructureKnown(boolean newIsStructureKnown) {
		this.isStructureKnown = newIsStructureKnown;
	}
}
