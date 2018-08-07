/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.util;

/**
 * Description of the new constant pool entry as described in the JVM specifications
 * added for Java 7 support.
 * Its contents is initialized according to its kind.
 *
 * This interface may be implemented by clients.
 *
 * @since 3.8
 */
public interface IConstantPoolEntry2 extends IConstantPoolEntry {
	/**
	 * Returns the descriptor index. This value is set only when decoding a MethodType entry.
	 * The value is unspecified otherwise. The corresponding UTF8 value can be retrieved by using
	 * {@link #getMethodDescriptor()}.
	 *
	 * @return the descriptor index. This value is set only when decoding a MethodType entry.
	 * @see IConstantPoolConstant#CONSTANT_MethodType
	 */
	int getDescriptorIndex();

	/**
	 * Returns the reference kind. This value is set only when decoding a MethodHandle entry.
	 * The value is unspecified otherwise.
	 *
	 * @return the reference kind. This value is set only when decoding a MethodHandle entry.
	 * @see IConstantPoolConstant#CONSTANT_MethodHandle
	 */
	int getReferenceKind();

	/**
	 * Returns the reference index. This value is set only when decoding a MethodHandle entry.
	 * The value is unspecified otherwise.
	 *
	 * @return the reference kind. This value is set only when decoding a MethodHandle entry.
	 * @see IConstantPoolConstant#CONSTANT_MethodHandle
	 */
	int getReferenceIndex();
	
	/**
	 * Returns the bootstrap method attribute index. This value is set only when decoding a InvokeDynamic entry.
	 * The value is unspecified otherwise.
	 *
	 * @return the reference kind. This value is set only when decoding a MethodHandle entry.
	 * @see IConstantPoolConstant#CONSTANT_InvokeDynamic
	 */
	int getBootstrapMethodAttributeIndex();
}
