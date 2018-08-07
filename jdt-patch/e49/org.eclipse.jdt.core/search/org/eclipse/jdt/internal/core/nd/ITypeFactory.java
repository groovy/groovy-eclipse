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

import org.eclipse.jdt.internal.core.nd.field.StructDef.DeletionSemantics;

// TODO(sxenos): rename this to something like "StructDescriptor" -- it's more than a factory and the word
// type is overloaded in JDT.
public interface ITypeFactory<T> {
	/**
	 * Invokes the delete method on all the fields of the object, and calls deleteFields on the superclass' type (if
	 * any). Does not perform any higher-level cleanup operations. This is only intended to be called from the
	 * deleteFields methods of a subtype or the delete method of this class.
	 * <p>
	 * When destructing a type with a superclass, the correct destruction behavior is:
	 * <ul>
	 * <li>External code invokes the delete method on ITypeFactory
	 * <li>The ITypeFactory.delete method calls an instance method on the class (typically called T#delete()), which
	 * performs high-level deletion operations (if any).
	 * <li>T.delete also calls T.super.delete() (if any)
	 * <li>ITypeFactory.delete calls ITypeFactory.deleteFields, which performs low-level deletion operations on the
	 * fields, then calls ITypeFactory.deleteFields on the base type.
	 * </ul>
	 */
	void destructFields(Nd dom, long address);

	T create(Nd dom, long address);

	/**
	 * Invokes any cleanup code for this object. In particular, it deallocates any memory allocated by the type's
	 * fields. Does not free the memory at address, though. This is used for both objects which were allocated their own
	 * memory block and objects which are embedded as fields within a larger object. If the object was given its own
	 * memory block, it is the caller's responsibility to invoke free after calling this method.
	 */
	void destruct(Nd dom, long address);

	/**
	 * If this returns false, the delete and deleteFields methods both always do nothing.
	 */
	boolean hasDestructor();

	int getRecordSize();

	Class<?> getElementClass();

	/**
	 * Returns true if this object is orphaned. If the object is refcounted, this means the refcount is 0. If
	 * the object is deleted via an owner pointer, this means the owner pointer is null.
	 */
	boolean isReadyForDeletion(Nd dom, long address);

	/**
	 * Returns the deletion semantics used for this object.
	 */
	DeletionSemantics getDeletionSemantics();
}
