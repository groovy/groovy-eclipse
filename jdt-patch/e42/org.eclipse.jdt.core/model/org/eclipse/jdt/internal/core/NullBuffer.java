/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IOpenable;

/**
 * This class represents a null buffer. This buffer is used to represent a buffer for a class file
 * that has no source attached.
 */
public class NullBuffer extends Buffer {
	/**
	 * Creates a new null buffer on an underlying resource.
	 */
	public NullBuffer(IFile file, IOpenable owner, boolean readOnly) {
		super(file, owner, readOnly);
	}
}
