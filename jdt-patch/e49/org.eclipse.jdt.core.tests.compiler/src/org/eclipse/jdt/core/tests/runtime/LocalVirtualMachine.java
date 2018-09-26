/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.runtime;

import java.io.*;

import org.eclipse.jdt.core.tests.util.Util;

/**
 * Wrapper around the external process that is running a local VM.
 * This allows to kill this process when we exit this vm.
 */
public class LocalVirtualMachine {
	protected Process process;
	protected int debugPort;
	protected String evalTargetPath;
/**
 * Creates a new LocalVirtualMachine that doesn't run and that cannot be debugged nor used to
 * evaluate.
 */
public LocalVirtualMachine() {
	this.process = null;
	this.debugPort = -1;
	this.evalTargetPath = null;
}
/**
 * Creates a new LocalVirtualMachine from the Process that runs this VM
 * and with the given debug port number.
 */
public LocalVirtualMachine(Process p, int debugPort, String evalTargetPath) {
	this.process = p;
	this.debugPort = debugPort;
	this.evalTargetPath = evalTargetPath;
}
/*
 * Cleans up the given directory by removing all the files it contains as well
 * but leaving the directory.
 * @throws TargetException if the target path could not be cleaned up
 *
private void cleanupDirectory(File directory) throws TargetException {
	if (!directory.exists()) {
		return;
	}
	String[] fileNames = directory.list();
	for (int i = 0; i < fileNames.length; i++) {
		File file = new File(directory, fileNames[i]);
		if (file.isDirectory()) {
			cleanupDirectory(file);
			if (!file.delete()) {
				throw new TargetException("Could not delete directory " + directory.getPath());
			}
		} else {
			if (!file.delete()) {
				throw new TargetException("Could not delete file " + file.getPath());
			}
		}
	}
}
*/
/**
 * Cleans up this context's target path by removing all the files it contains
 * but leaving the directory.
 * @throws TargetException if the target path could not be cleaned up
 */
protected void cleanupTargetPath() throws TargetException {
	if (this.evalTargetPath == null) return;
	String targetPath = this.evalTargetPath;
	if (LocalVMLauncher.TARGET_HAS_FILE_SYSTEM) {
		Util.delete(new File(targetPath, LocalVMLauncher.REGULAR_CLASSPATH_DIRECTORY));
		Util.delete(new File(targetPath, LocalVMLauncher.BOOT_CLASSPATH_DIRECTORY));
		File file = new File(targetPath, RuntimeConstants.SUPPORT_ZIP_FILE_NAME);

		/* workaround pb with Process.exitValue() that returns the process has exited, but it has not free the file yet
		int count = 10;
		for (int i = 0; i < count; i++) {
			if (file.delete()) {
				break;
			}
			try {
				Thread.sleep(count * 100);
			} catch (InterruptedException e) {
			}
		}
		*/
		if (!Util.delete(file)) {
			throw new TargetException("Could not delete " + file.getPath());
		}
	} else {
		Util.delete(targetPath);
	}
}
/**
 * Returns the debug port number for this VM. This is the port number that was
 * passed as the "-debug" option if this VM was launched using a <code>LocalVMLauncher</code>.
 * Returns -1 if this information is not available or if this VM is not running in
 * debug mode.
 */
public int getDebugPortNumber() {
	return this.debugPort;
}
/**
 * Returns an input stream that is connected to the standard error
 * (<code>System.err</code>) of this target VM.
 * Bytes that are written to <code>System.err</code> by the target
 * program become available in the input stream.
 * <p>
 * Note 1: This stream is usually unbuffered.
 * <p>
 * Note 2: Two calls to this method return the same identical input stream.
 * <p>
 * See also <code>java.lang.Process.getErrorStream()</code>.
 *
 * @return an input stream connected to the target VM's <code>System.err</code>.
 * @exception TargetException if the target VM is not reachable.
 */
public InputStream getErrorStream() throws TargetException {
	if (this.process == null)
		throw new TargetException("The VM is not running");
	return this.process.getErrorStream();
}
/**
 * Returns an input stream that is connected to the standard output
 * (<code>System.out</code>) of this target VM.
 * Bytes that are written to <code>System.out</code> by the target
 * program become available in the input stream.
 * <p>
 * Note 1: This stream is usually buffered.
 * <p>
 * Note 2: Two calls to this method return the same identical input stream.
 * <p>
 * See also <code>java.lang.Process.getInputStream()</code>.
 *
 * @return an input stream connected to the target VM's <code>System.out</code>.
 * @exception TargetException if the target VM is not reachable.
 */
public InputStream getInputStream() throws TargetException {
	if (this.process == null)
		throw new TargetException("The VM is not running");
	// Workaround problem with input stream of a Process
	return new VMInputStream(this.process, this.process.getInputStream());
}
/**
 * Returns an output stream that is connected to the standard input
 * (<code>System.in</code>) of this target VM.
 * Bytes that are written to the output stream by a client become available to the target
 * program in <code>System.in</code>.
 * <p>
 * Note 1: This stream is usually buffered.
 * <p>
 * Note 2: Two calls to this method return the same identical output stream.
 * <p>
 * See also <code>java.lang.Process.getOutputStream()</code>.
 *
 * @return an output stream connected to the target VM's <code>System.in</code>.
 * @exception TargetException if the target VM is not reachable.
 */
public OutputStream getOutputStream() throws TargetException {
	if (this.process == null)
		throw new TargetException("The VM is not running");
	return this.process.getOutputStream();
}
/**
 * Returns whether this target VM is still running.
 * <p>
 * Note: This operation may require contacting the target VM to find out
 *       if it is still running.
 */
public boolean isRunning() {
	if (this.process == null) {
		return false;
	}
	boolean hasExited;
	try {
		this.process.exitValue();
		hasExited = true;
	} catch (IllegalThreadStateException e) {
		hasExited = false;
	}
	return !hasExited;
}
/**
 * Shuts down this target VM.
 * This causes the VM to exit. This operation is ignored
 * if the VM has already shut down.
 *
 * @throws TargetException if the target path could not be cleaned up
 */
public synchronized void shutDown() throws TargetException {
	if (this.process != null) {
		this.process.destroy();
		this.process = null;
		cleanupTargetPath();
	}
}
}
