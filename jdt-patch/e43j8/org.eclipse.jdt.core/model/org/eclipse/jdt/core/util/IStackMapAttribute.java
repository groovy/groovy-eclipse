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
 * This class represents a stack map attribute.
 *
 * This interface may be implemented by clients.
 *
 * @since 3.2
 */
public interface IStackMapAttribute extends IClassFileAttribute {

	/**
	 * Answer back the number of stack map frames of this atribute as specified in
	 * the JVM specifications.
	 *
	 * @return the number of stack map frames of this atribute as specified in
	 * the JVM specifications
	 */
	int getNumberOfEntries();

	/**
	 * Answer back the stack map frames for this attribute as specified
	 * in the JVM specifications.
	 *
	 * @return the stack map frames for this attribute as specified
	 * in the JVM specifications
	 */
	IStackMapFrame[] getStackMapFrame();
}
