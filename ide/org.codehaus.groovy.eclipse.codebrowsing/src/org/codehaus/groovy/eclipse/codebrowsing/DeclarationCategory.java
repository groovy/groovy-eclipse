/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Edward Povazan   - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.codebrowsing;

/**
 * The following categories are the predefined categories. Other categories
 * can be defined by extensions. Categories are used to group multiple
 * declaration search proposals.
 */
public interface DeclarationCategory {
	public static final String LOCAL = "localCategory";

	public static final String FIELD = "fieldCategory";

	public static final String METHOD = "methodCategory";

	public static final String STATIC_METHOD = "staticMethodCategory";

	public static final String METHOD_PARAMETER = "methodParameterCategory";

	public static final String CLASS = "classCategory";

}