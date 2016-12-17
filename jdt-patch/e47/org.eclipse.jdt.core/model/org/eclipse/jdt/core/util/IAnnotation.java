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
 * Description of a annotation structure as described in the JVM specifications
 * (added in J2SE 1.5).
 *
 * This interface may be implemented by clients.
 *
 * @since 3.0
 */
public interface IAnnotation {
	/**
	 * Answer back the type index as described in the JVM specifications.
	 *
	 * @return the type index
	 */
	int getTypeIndex();

	/**
	 * Answer back the type name as described in the JVM specifications.
	 *
	 * @return the type name
	 * @since 3.1
	 */
	char[] getTypeName();

	/**
	 * Answer back the number of components as described in the JVM specifications.
	 *
	 * @return the type index
	 */
	int getComponentsNumber();

	/**
	 * Answer back the components as described in the JVM specifications. Answer an
	 * empty collection if none.
	 *
	 * @return the components
	 */
	IAnnotationComponent[] getComponents();
}
