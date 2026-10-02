/*******************************************************************************
 * Copyright (c) 2007, 2011 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.model;

/**
 * Additional information available for Elements that are implemented
 * within the Eclipse APT framework.
 * @see javax.lang.model.element.Element
 * @since 3.3
 */
public interface IElementInfo {
	/**
	 * Get the project-relative path to the source file that contains this element.
	 * If the element is a PackageElement, the "source file" is package-info.java.
	 * If the element is not recognized or does not exist in the project for some
	 * reason, returns null.
	 * @return the project-relative path, or null.
	 */
	public String getFileName();
}
