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
package org.eclipse.jdt.core.util;

/**
 * Description of a constant pool as described in the JVM specifications.
 *
 * This interface may be implemented by clients.
 *
 * @since 2.0
 */
public interface IConstantPool {

	/**
	 * Answer back the number of entries in the constant pool.
	 *
	 * @return the number of entries in the constant pool
	 */
	int getConstantPoolCount();

	/**
	 * Answer back the type of the entry at the index @index
	 * in the constant pool.
	 *
	 * @param index the index of the entry in the constant pool
	 * @return the type of the entry at the index @index in the constant pool
	 */
	int getEntryKind(int index);

	/**
	 * Answer back the entry at the index @index
	 * in the constant pool.
	 *
	 * @param index the index of the entry in the constant pool
	 * @return the entry at the index @index in the constant pool
	 */
	IConstantPoolEntry decodeEntry(int index);
}
