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
package org.eclipse.jdt.internal.core.search.indexing;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.core.index.Index;

class RemoveFromIndex extends IndexRequest {
	String resourceName;

	public RemoveFromIndex(String resourceName, IPath containerPath, IndexManager manager) {
		super(containerPath, manager);
		this.resourceName = resourceName;
	}
	public boolean execute(IProgressMonitor progressMonitor) {

		if (this.isCancelled || progressMonitor != null && progressMonitor.isCanceled()) return true;

		/* ensure no concurrent write access to index */
		Index index = this.manager.getIndex(this.containerPath, true, /*reuse index file*/ false /*create if none*/);
		if (index == null) return true;
		ReadWriteMonitor monitor = index.monitor;
		if (monitor == null) return true; // index got deleted since acquired

		try {
			monitor.enterWrite(); // ask permission to write
			index.remove(this.resourceName);
		} finally {
			monitor.exitWrite(); // free write lock
		}
		return true;
	}
	public String toString() {
		return "removing " + this.resourceName + " from index " + this.containerPath; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
