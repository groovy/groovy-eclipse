/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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
 * Description of a local variable type attribute as described in the JVM specifications
 * (added in J2SE 1.5).
 *
 * This interface may be implemented by clients.
 *
 * @since 3.0
 */
public interface ILocalVariableTypeTableAttribute extends IClassFileAttribute {

	/**
	 * Answer back the local variable type table length of this entry as specified in
	 * the JVM specifications.
	 *
	 * @return the local variable type table length of this entry as specified in
	 * the JVM specifications
	 */
	int getLocalVariableTypeTableLength();

	/**
	 * Answer back the local variable type table of this entry as specified in
	 * the JVM specifications. Answer an empty array if none.
	 *
	 * @return the local variable type table of this entry as specified in
	 * the JVM specifications. Answer an empty array if none
	 */
	ILocalVariableTypeTableEntry[] getLocalVariableTypeTable();
}
