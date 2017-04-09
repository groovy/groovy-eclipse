package org.eclipse.jdt.internal.core;
import org.eclipse.core.runtime.IProgressMonitor;

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
public class BatchInitializationMonitor implements IProgressMonitor {

	public ThreadLocal initializeAfterLoadMonitor = new ThreadLocal();

	public String subTaskName = ""; //$NON-NLS-1$
	public int worked = 0;

	private IProgressMonitor getMonitor() {
		return (IProgressMonitor) this.initializeAfterLoadMonitor.get();
	}

	public void beginTask(String name, int totalWork) {
		IProgressMonitor monitor = getMonitor();
		if (monitor != null)
			monitor.beginTask(name, totalWork);
	}

	public void done() {
		IProgressMonitor monitor = getMonitor();
		if (monitor != null)
			monitor.done();
		this.worked = 0;
		this.subTaskName = ""; //$NON-NLS-1$
	}

	public void internalWorked(double work) {
		IProgressMonitor monitor = getMonitor();
		if (monitor != null)
			monitor.internalWorked(work);
	}

	public boolean isCanceled() {
		IProgressMonitor monitor = getMonitor();
		if (monitor != null)
			return monitor.isCanceled();
		return false;
	}

	public void setCanceled(boolean value) {
		IProgressMonitor monitor = getMonitor();
		if (monitor != null)
			monitor.setCanceled(value);
	}

	public void setTaskName(String name) {
		IProgressMonitor monitor = getMonitor();
		if (monitor != null)
			monitor.setTaskName(name);
	}

	public void subTask(String name) {
		IProgressMonitor monitor = getMonitor();
		if (monitor != null)
			monitor.subTask(name);
		this.subTaskName = name;
	}

	public void worked(int work) {
		IProgressMonitor monitor = getMonitor();
		if (monitor != null)
			monitor.worked(work);
		synchronized(this) {
			this.worked += work;
		}
	}

	public synchronized int getWorked() {
		int result = this.worked;
		this.worked = 0;
		return result;
	}
}
