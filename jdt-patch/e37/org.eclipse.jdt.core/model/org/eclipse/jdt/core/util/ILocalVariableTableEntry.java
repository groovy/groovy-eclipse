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
 * Description of a local variable table entry as specified in the JVM specifications.
 *
 * This interface may be implemented by clients.
 *
 * @since 2.0
 */
public interface ILocalVariableTableEntry {

	/**
	 * Answer back the start pc of this entry as specified in
	 * the JVM specifications.
	 *
	 * @return the start pc of this entry as specified in
	 * the JVM specifications
	 */
	int getStartPC();

	/**
	 * Answer back the length of this entry as specified in
	 * the JVM specifications.
	 *
	 * @return the length of this entry as specified in
	 * the JVM specifications
	 */
	int getLength();

	/**
	 * Answer back the name index in the constant pool of this entry as specified in
	 * the JVM specifications.
	 *
	 * @return the name index in the constant pool of this entry as specified in
	 * the JVM specifications
	 */
	int getNameIndex();

	/**
	 * Answer back the descriptor index in the constant pool of this entry as specified in
	 * the JVM specifications.
	 *
	 * @return the descriptor index in the constant pool of this entry as specified in
	 * the JVM specifications
	 */
	int getDescriptorIndex();

	/**
	 * Answer back the index of this entry as specified in
	 * the JVM specifications.
	 *
	 * @return the index of this entry as specified in
	 * the JVM specifications
	 */
	int getIndex();

	/**
	 * Answer back the name of this entry as specified in
	 * the JVM specifications.
	 *
	 * @return the name of this entry as specified in
	 * the JVM specifications
	 */
	char[] getName();

	/**
	 * Answer back the descriptor of this entry as specified in
	 * the JVM specifications.
	 *
	 * @return the descriptor of this entry as specified in
	 * the JVM specifications
	 */
	char[] getDescriptor();
}
