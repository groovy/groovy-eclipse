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
 * Description of a Module Main Class Attribute as described in the JVMS9 4.7.27
 *
 * This interface may be implemented by clients.
 *
 * @since 3.14
 */
public interface IModuleMainClassAttribute extends IClassFileAttribute {

	/**
	 * Answer back the main class index.
	 *
	 * @return the main class index
	 */
	int getMainClassIndex();

	/**
	 * Answer back the name of main class.
	 *
	 * @return the name of main class
	 */
	char[] getMainClassName();
}
