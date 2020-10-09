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

import java.io.InputStream;

/**
 * A null console reader that continuously reads from the VM input stream
 * so that the VM doesn't block when the program writes to the stout.
 */

public class NullConsoleReader extends AbstractReader {
	private InputStream input;
/*
 * Creates a new console reader that will read from the given input stream.
 */
public NullConsoleReader(String name, InputStream input) {
	super(name);
	this.input = input;
}
/**
 * Continuously reads events that are coming from the event queue.
 */
@Override
protected void readerLoop() {
	java.io.BufferedReader bufferedInput = new java.io.BufferedReader(new java.io.InputStreamReader(this.input));
	try {
		int read= 0;
		while (!this.isStopping && read != -1) {
			read= bufferedInput.read();
		}
	} catch (java.io.IOException e) {
	}
}
}
