/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd;

/**
 * This mix-in interface is implemented by database objects that require a custom
 * destruction step.
 */
public interface IDestructable {
	/**
	 * Intended to be implemented by objects which require a custom destruction step.
	 * This should normally not be invoked by clients, since custom destruction is just
	 * one step in tearing down an object. The normal way to tear down an object is
	 * {@link NdNode#delete}
	 * <p>
	 * If you are writing code that must run as part of delete (or are implementing part
	 * of the destruct method on a custom ITypeFactory)the correct steps to destructing
	 * an object are:
	 * <ul>
	 * <li>Invoke this destruct method (which serves the same purpose as the user-implemented
	 *     portion of a C++ destructor)</li>
	 * <li>Invoke ITypeFactory.destructFields to destruct its fields (which serves the same
	 *     purpose as the compiler-implemented portion of a C++ destructor)</li>
	 * <li>Invoke Database.free on its address to free up memory allocated for the object
	 *     itself. (Which serves the same purpose as the memory deallocation step in
	 *     the C++ delete operator)</li>
	 * </ul>
	 * <p>
	 * Normally, first two steps are performed together as part of ITypeFactory.destruct
	 */
	void destruct();
}
