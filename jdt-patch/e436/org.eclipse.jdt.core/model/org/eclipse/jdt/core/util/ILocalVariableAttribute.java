/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.util;

/**
 * Description of a local variable attribute as described in the JVM specifications.
 *
 * This interface may be implemented by clients.
 *
 * @since 2.0
 */
public interface ILocalVariableAttribute extends IClassFileAttribute {

	/**
	 * Answer back the local variable table length of this entry as specified in
	 * the JVM specifications.
	 *
	 * @return the local variable table length of this entry as specified in
	 * the JVM specifications
	 */
	int getLocalVariableTableLength();

	/**
	 * Answer back the local variable table of this entry as specified in
	 * the JVM specifications. Answer an empty array if none.
	 *
	 * @return the local variable table of this entry as specified in
	 * the JVM specifications. Answer an empty array if none
	 */
	ILocalVariableTableEntry[] getLocalVariableTable();

}
