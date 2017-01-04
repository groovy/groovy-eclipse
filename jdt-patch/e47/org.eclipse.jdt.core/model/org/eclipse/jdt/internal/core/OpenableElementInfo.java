/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Vladimir Piskarev <pisv@1c.ru> - Thread safety of OpenableElementInfo - https://bugs.eclipse.org/450490
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
	protected volatile IJavaElement[] children = JavaElement.NO_ELEMENTS;
	
	/**
	 * Is the structure of this element known
	 * @see IJavaElement#isStructureKnown()
	 */
	protected boolean isStructureKnown = false;

	public void addChild(IJavaElement child) {
		IJavaElement[] oldChildren = this.children;
		int length = oldChildren.length;
		if (length == 0) {
			this.children = new IJavaElement[] {child};
		} else {
			for (int i = 0; i < length; i++) {
				if (oldChildren[i].equals(child))
					return; // already included
			}
			IJavaElement[] newChildren = new IJavaElement[length+1];
			System.arraycopy(oldChildren, 0, newChildren, 0, length);
			newChildren[length] = child;
			this.children = newChildren;
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
		IJavaElement[] oldChildren = this.children;
		for (int i = 0, length = oldChildren.length; i < length; i++) {
			if (oldChildren[i].equals(child)) {
				if (length == 1) {
					this.children = JavaElement.NO_ELEMENTS;
				} else {
					IJavaElement[] newChildren = new IJavaElement[length-1];
					System.arraycopy(oldChildren, 0, newChildren , 0, i);
					if (i < length-1)
						System.arraycopy(oldChildren, i+1, newChildren, i, length-1-i);
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
