/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

public class TypeParameterElementInfo extends SourceRefElementInfo {

	/*
	 * The start position of this type parameter's name in the its
	 * openable's buffer.
	 */
	public int nameStart= -1;

	/*
	 * The last position of this type parameter name in the its
	 * openable's buffer.
	 */
	public int nameEnd= -1;

	/*
	 * The bounds names of this type parameter.
	 */
	public char[][] bounds;
	
	/*
	 * The bounds' signatures for this type parameter. 
	 */
	public char[][] boundsSignatures;
}
