/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.formatter;

/**
 * Internal code formatter constants.
 *
 * @since 3.4
 */

public interface ICodeFormatterConstants {
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=122247
    // constants used to handle the addition of new lines after annotations

	/** annotation on unspecified source*/
	public static final int ANNOTATION_UNSPECIFIED = 0;

	/** annotation on a type */
	public static final int ANNOTATION_ON_TYPE = 1;

	/** annotation on a field */
	public static final int ANNOTATION_ON_FIELD = 2;

	/** annotation on a method */
	public static final int ANNOTATION_ON_METHOD = 3;

	/** annotation on a package */
	public static final int ANNOTATION_ON_PACKAGE = 4;

	/** annotation on a parameter */
	public static final int ANNOTATION_ON_PARAMETER = 5;

	/** annotation on a local variable */
	public static final int ANNOTATION_ON_LOCAL_VARIABLE = 6;
}
