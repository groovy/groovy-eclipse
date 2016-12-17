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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.util.LRUCache;

/**
 * An LRU cache of <code>JavaElements</code>.
 */
public class ElementCache extends OverflowingLRUCache {

	IJavaElement spaceLimitParent = null;

/**
 * Constructs a new element cache of the given size.
 */
public ElementCache(int size) {
	super(size);
}
/**
 * Constructs a new element cache of the given size.
 */
public ElementCache(int size, int overflow) {
	super(size, overflow);
}
/**
 * Returns true if the element is successfully closed and
 * removed from the cache, otherwise false.
 *
 * <p>NOTE: this triggers an external removal of this element
 * by closing the element.
 */
protected boolean close(LRUCacheEntry entry) {
	Openable element = (Openable) entry.key;
	try {
		if (!element.canBeRemovedFromCache()) {
			return false;
		} else {
			element.close();
			return true;
		}
	} catch (JavaModelException npe) {
		return false;
	}
}

/*
 * Ensures that there is enough room for adding the children of the given info.
 * If the space limit must be increased, record the parent that needed this space limit.
 */
protected void ensureSpaceLimit(Object info, IJavaElement parent) {
	// ensure the children can be put without closing other elements
	int childrenSize = ((JavaElementInfo) info).getChildren().length;
	int spaceNeeded = 1 + (int)((1 + this.loadFactor) * (childrenSize + this.overflow));
	if (this.spaceLimit < spaceNeeded) {
		// parent is being opened with more children than the space limit
		shrink(); // remove overflow
		setSpaceLimit(spaceNeeded);
		this.spaceLimitParent = parent;
	}
}

/*
 * Returns a new instance of the receiver.
 */
protected LRUCache newInstance(int size, int newOverflow) {
	return new ElementCache(size, newOverflow);
}

/*
 * If the given parent was the one that increased the space limit, reset
 * the space limit to the given default value.
 */
protected void resetSpaceLimit(int defaultLimit, IJavaElement parent) {
	if (parent.equals(this.spaceLimitParent)) {
		setSpaceLimit(defaultLimit);
		this.spaceLimitParent = null;
	}
}

}
