/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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

/**
 * An abstract reader that continuously reads.
 */

abstract public class AbstractReader {
	protected String name;
	protected Thread readerThread;
	protected boolean isStopping= false;
/*
 * Creates a new reader with the given name.
 */
public AbstractReader(String name) {
	this.name = name;
}
/**
 * Continuously reads. Note that if the read involves waiting
 * it can be interrupted and a InterruptedException will be thrown.
 */
abstract protected void readerLoop();
/**
 * Start the thread that reads events.
 *
 */
public void start() {
	this.readerThread = new Thread(
		new Runnable() {
			@Override
			public void run () {
				readerLoop();
			}
		},
		AbstractReader.this.name);
	this.readerThread.start();
}
/**
 * Tells the reader loop that it should stop.
 */
public void stop() {
	this.isStopping= true;
	if (this.readerThread != null)
		this.readerThread.interrupt();
}
}
