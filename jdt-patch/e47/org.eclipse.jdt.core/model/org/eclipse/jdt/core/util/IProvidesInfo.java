/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
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
 * Description of a provides info as described in JVMS9 4.7.25.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since 3.14
 */
public interface IProvidesInfo {

	/**
	 * Answer back the provides index.
	 *
	 * @return the provides index
	 */
	int getIndex();

	/**
	 * Answer back the service class/interface
	 *
	 * @return the service class/interface
	 */
	char[] getServiceName();

	/**
	 * Answer back the number of implementations.
	 *
	 * @return the number of implementations.
	 */
	int getImplementationsCount();

	/**
	 * Answer back the array of implementation indices.
	 *
	 * @return the array of implementation indices.
	 */
	int[] getImplementationIndices();

	/**
	 * Answer back the array of implementation names.
	 *
	 * @return the array of implementation names.
	 */
	char[][] getImplementationNames();

}
