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
package org.eclipse.jdt.core;

/**
 * A listener which gets notified when a particular type hierarchy object
 * changes.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 */
public interface ITypeHierarchyChangedListener {
	/**
	 * Notifies that the given type hierarchy has changed in some way and should
	 * be refreshed at some point to make it consistent with the current state of
	 * the Java model.
	 *
	 * @param typeHierarchy the given type hierarchy
	 */
	void typeHierarchyChanged(ITypeHierarchy typeHierarchy);
}
