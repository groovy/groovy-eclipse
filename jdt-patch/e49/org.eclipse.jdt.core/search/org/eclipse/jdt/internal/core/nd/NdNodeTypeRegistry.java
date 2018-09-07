/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.internal.core.nd.db.IndexException;

/**
 * Maps integer constants onto factories for {@link NdNode} objects.
 */
public class NdNodeTypeRegistry<R> {
	private final Map<Short, ITypeFactory<? extends R>> types = new HashMap<>();
	private final BitSet reserved = new BitSet();
	private final Map<Class<?>, Short> registeredClasses = new HashMap<>();

	/**
	 * Registers a class to be used with this node type registry. Note that if we ever want to stop registering a type
	 * name in the future, its fully-qualified class name should be passed to reserve(...) to prevent its hashfrom being
	 * reused in the future.
	 */
	public <T extends R> void register(int typeId, ITypeFactory<T> toRegister) {
		if ((typeId & 0xFFFF0000) != 0) {
			throw new IllegalArgumentException("The typeId " + typeId + " does not fit within a short int");  //$NON-NLS-1$//$NON-NLS-2$
		}
		short shortTypeId = (short)typeId;
		String fullyQualifiedClassName = toRegister.getElementClass().getName();

		if (this.types.containsKey(shortTypeId) || this.reserved.get(typeId)) {
			throw new IllegalArgumentException(
					"The type id " + typeId + " for class " + fullyQualifiedClassName + " is already in use."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		this.types.put(shortTypeId, toRegister);
		this.registeredClasses.put(toRegister.getElementClass(), shortTypeId);
	}

	/**
	 * Reserves the given node class name, such that its hash cannot be used by any other node registered with
	 * "register". If we ever want to unregister a given Class from the type registry, its class name should be reserved
	 * using this method. Doing so will prevent its type ID from being reused by another future class.
	 */
	public void reserve(short typeId) {
		if (this.types.containsKey(typeId) || this.reserved.get(typeId)) {
			throw new IllegalArgumentException("The type ID " + typeId + " is already in use"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		this.reserved.set(typeId);
	}

	/**
	 * Returns the class associated with the given type or null if the given type ID is not known
	 */
	public ITypeFactory<? extends R> getClassForType(short type) {
		return this.types.get(type);
	}

	public R createNode(Nd nd, long address, short nodeType) throws IndexException {
		ITypeFactory<? extends R> typeFactory = this.types.get(nodeType);

		if (typeFactory == null) {
			throw new IndexException("Index corruption detected. Unknown node type: " + nodeType + " at address "  //$NON-NLS-1$//$NON-NLS-2$
					+ address);
		}

		return typeFactory.create(nd, address);
	}

	public boolean isRegisteredClass(Class<?> toQuery) {
		return this.registeredClasses.containsKey(toQuery);
	}

	public short getTypeForClass(Class<?> toQuery) {
		Short classId = this.registeredClasses.get(toQuery);

		if (classId == null) {
			throw new IllegalArgumentException(toQuery.getName() + " was not registered as a node type"); //$NON-NLS-1$
		}
		return classId;
	}

	@SuppressWarnings("unchecked")
	public <T extends R> ITypeFactory<T> getTypeFactory(short nodeType) {
		ITypeFactory<T> result = (ITypeFactory<T>) this.types.get(nodeType);

		if (result == null) {
			throw new IllegalArgumentException("The node type " + nodeType  //$NON-NLS-1$
				+ " is not registered with this database"); //$NON-NLS-1$
		}

		return result;
	}
}
