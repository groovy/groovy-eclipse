/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.core.INameEnvironmentWithProgress;
import org.eclipse.jdt.internal.core.NameLookup;

/**
 * Batch name environment that can be canceled using a monitor.
 * @since 3.6
 */
class NameEnvironmentWithProgress extends FileSystem implements INameEnvironmentWithProgress {
	IProgressMonitor monitor;
	
	public NameEnvironmentWithProgress(Classpath[] paths, String[] initialFileNames, IProgressMonitor monitor) {
		super(paths, initialFileNames);
		setMonitor(monitor);
	}
	private void checkCanceled() {
		if (this.monitor != null && this.monitor.isCanceled()) {
			if (NameLookup.VERBOSE) {
				System.out.println(Thread.currentThread() + " CANCELLING LOOKUP "); //$NON-NLS-1$
			}
			throw new AbortCompilation(true/*silent*/, new OperationCanceledException());
		}
	}
	public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
		checkCanceled();
		return super.findType(typeName, packageName);
	}
	public NameEnvironmentAnswer findType(char[][] compoundName) {
		checkCanceled();
		return super.findType(compoundName);
	}
	public boolean isPackage(char[][] compoundName, char[] packageName) {
		checkCanceled();
		return super.isPackage(compoundName, packageName);
	}
	
	public void setMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}
}
