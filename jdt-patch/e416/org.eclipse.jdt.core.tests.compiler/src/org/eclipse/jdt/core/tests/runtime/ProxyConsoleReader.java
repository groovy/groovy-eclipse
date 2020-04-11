/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

/**
 * A <code>ProxyConsoleReader</code> reads the ouput from the proxy and
 * redirects it to a file or to the stdout if "con" is the name of the file.
 */
class ProxyConsoleReader extends AbstractReader {
	private InputStream input;
	private OutputStream output;
/*
 * Creates a new proxy console reader that will read from the given input stream
 * and rewrite what's read to the given file (or the stdout is the file name is
 * "con")
 */
public ProxyConsoleReader(String name, InputStream input, String fileName) {
	super(name);
	this.input = input;
	if (fileName.equals("con")) {
		this.output= System.out;
	} else {
		try {
			this.output= new FileOutputStream(fileName);
		} catch (IOException e) {
			System.out.println("Could not create file " + fileName + ". Redirecting to stdout");
			this.output= System.out;
		}
	}
}
/**
 * Continuously reads from the proxy output and redirect what's read to
 * this reader's file.
 */
@Override
protected void readerLoop() {
	try {
		byte[] buffer= new byte[1024];
		int read= 0;
		while (!this.isStopping && read != -1) {
			read= this.input.read(buffer);
			if (read != -1)
				this.output.write(buffer, 0, read);
		}
	} catch (java.io.IOException e) {
	}
}
/**
 * Stop this reader
 */
@Override
public void stop() {
	super.stop();
	try {
		if (this.output != System.out)
			this.output.close();
	} catch (IOException e) {
	}
}
}
