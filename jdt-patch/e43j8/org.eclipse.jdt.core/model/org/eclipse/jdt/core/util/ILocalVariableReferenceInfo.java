/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *        Andy Clement - Contributions for
 *                          Bug 383624 - [1.8][compiler] Revive code generation support for type annotations (from Olivier's work)
 *******************************************************************************/
package org.eclipse.jdt.core.util;

/**
 * Description of a local variable reference info table entry as specified in the JVM specifications.
 *
 * This interface may be implemented by clients.
 *
 * @since 3.10
 */
public interface ILocalVariableReferenceInfo {

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
	 * Answer back the resolved position of the local variable as specified in
	 * the JVM specifications.
	 *
	 * @return the resolved position of the local variable as specified in
	 * the JVM specifications
	 */
	int getIndex();
}
