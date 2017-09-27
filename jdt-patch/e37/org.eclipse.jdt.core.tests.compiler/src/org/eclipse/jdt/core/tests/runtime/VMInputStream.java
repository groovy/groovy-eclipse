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
package org.eclipse.jdt.core.tests.runtime;

import java.io.*;

/**
 * Workaround problem with input stream from a <code>java.lang.Process</code>
 * that throws an <code>IOException</code> even if there is something to read.
 */
public class VMInputStream extends InputStream {
	InputStream input;
	Process process;
public VMInputStream(Process process, InputStream input) {
	this.process= process;
	this.input= input;
}
public int available() throws IOException {
	return this.input.available();
}
public void close() throws IOException {
	this.input.close();
}
private boolean isRunning() {
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
public synchronized void mark(int readlimit) {
	this.input.mark(readlimit);
}
public boolean markSupported() {
	return this.input.markSupported();
}
public int read() throws IOException {
	try {
		return this.input.read();
	} catch (IOException e) {
		if (isRunning()) {
			return read();
		}
		throw e;
	}
}
public int read(byte b[]) throws IOException {
	// Make sure the byte array is initialized (value of 0 is used in the workaround below)
	for (int i=0;i<b.length;i++)
		b[i]=0;

	int read;
	try {
		read = this.input.read(b);
	} catch (IOException e) {
		if (isRunning()) {
			// Workaround problem with implementation of Process.getInputStream()
			// (see PR 1PRW670: LFRE:WINNT - Program hangs running in LeapFrog)
			read= 0;
			while (read < b.length && b[read] != 0) {
				read++;
			}
		} else
			throw e;
	}

	return read;
}
public int read(byte b[], int off, int len) throws IOException {
	// Make sure the byte array is initialized (value of 0 is used in the workaround below)
	for (int i = off; i < len; i++)
		b[i] = 0;

	int read;
	try {
		read = this.input.read(b, off, len);
	} catch (IOException e) {
		if (isRunning()) {
			// Workaround problem with implementation of Process.getInputStream()
			// (see PR 1PRW670: LFRE:WINNT - Program hangs running in LeapFrog)
			read = 0;
			while (off + read < len && b[off + read] != 0) {
				read++;
			}
		} else
			throw e;
	}
	return read;
}
public synchronized void reset() throws IOException {
	this.input.reset();
}
public long skip(long n) throws IOException {
	return this.input.skip(n);
}
}
