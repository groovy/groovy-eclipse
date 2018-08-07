/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IMemberValuePair;

/*
 * Element info for an IAnnotation element that originated from source.
 */
public class AnnotationInfo extends SourceRefElementInfo {

	/*
	 * The start position of this annotation's name in the its
	 * openable's buffer.
	 */
	public int nameStart= -1;

	/*
	 * The last position of this annotation in the its
	 * openable's buffer.
	 */
	public int nameEnd= -1;

	/*
	 * The member-value pairs of this annotation.
	 */
	public IMemberValuePair[] members;

}
