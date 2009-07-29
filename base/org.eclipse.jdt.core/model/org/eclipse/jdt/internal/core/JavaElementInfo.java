/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

/**
 * Holds cached structure and properties for a Java element.
 * Subclassed to carry properties for specific kinds of elements.
 */
public class JavaElementInfo {

	/**
	 * Collection of handles of immediate children of this
	 * object. This is an empty array if this element has
	 * no children.
	 */
	protected IJavaElement[] children;

	/**
	 * Shared empty collection used for efficiency.
	 */
	static Object[] NO_NON_JAVA_RESOURCES = new Object[] {};	
	
	protected JavaElementInfo() {
		this.children = JavaElement.NO_ELEMENTS;
	}
	public void addChild(IJavaElement child) {
		int length = this.children.length;		
		if (length == 0) {
			this.children = new IJavaElement[] {child};
		} else {
			for (int i = 0; i < length; i++) {
				if (children[i].equals(child))
					return; // already included
			}
			System.arraycopy(this.children, 0, this.children = new IJavaElement[length+1], 0, length);
			this.children[length] = child;
		}
	}
	public Object clone() {
		try {
			return super.clone();
		}
		catch (CloneNotSupportedException e) {
			throw new Error();
		}
	}
	public IJavaElement[] getChildren() {
		return this.children;
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
}
