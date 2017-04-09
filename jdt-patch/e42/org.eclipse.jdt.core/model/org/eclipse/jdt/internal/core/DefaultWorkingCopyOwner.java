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
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.WorkingCopyOwner;

/**
 * A working copy owner that creates internal buffers.
 * It also defines the PRIMARY working copy owner that is used by JDT/Core.
 */
public class DefaultWorkingCopyOwner extends WorkingCopyOwner {

	public WorkingCopyOwner primaryBufferProvider;

	public static final DefaultWorkingCopyOwner PRIMARY =  new DefaultWorkingCopyOwner();

	private DefaultWorkingCopyOwner() {
		// only one instance can be created
	}

	public IBuffer createBuffer(ICompilationUnit workingCopy) {
		if (this.primaryBufferProvider != null) return this.primaryBufferProvider.createBuffer(workingCopy);
		return super.createBuffer(workingCopy);
	}
	public String toString() {
		return "Primary owner"; //$NON-NLS-1$
	}
}
