/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.runtime;

/**
 * Wrapper around the external processes that are running a J9 VM
 * and a J9 Proxy.
 * This allows to kill these processes when we exit this vm.
 */
class J9VirtualMachine extends LocalVirtualMachine {
	private Process proxyProcess;
	private AbstractReader proxyConsoleReader;
	private String proxyOutputFile;
/**
 * Creates a new J9VirtualMachine from the Processes that runs this VM
 * and its J9 Proxy and with the given info.
 */
public J9VirtualMachine(Process vmProcess, int debugPort, String evalTargetPath, Process proxyProcess, String proxyOutputFile) {
	super(vmProcess, debugPort, evalTargetPath);
	this.proxyProcess = proxyProcess;
	this.proxyOutputFile = proxyOutputFile;

	// Start the Proxy console reader so that the proxy is not blocked on its stdout.
	if (this.proxyProcess != null) {
		if (this.proxyOutputFile == null) {
			this.proxyConsoleReader=
				new NullConsoleReader(
					"J9 Proxy Console Reader",
					this.proxyProcess.getInputStream());
		} else {
			this.proxyConsoleReader=
				new ProxyConsoleReader(
					"J9 Proxy Console Reader",
					this.proxyProcess.getInputStream(),
					this.proxyOutputFile);
		}
		this.proxyConsoleReader.start();
	}

}
/**
 * @see LocalVirtualMachine#shutDown
 */
public synchronized void shutDown() throws TargetException {
	super.shutDown();
	if (this.proxyConsoleReader != null)
		this.proxyConsoleReader.stop();
	if (this.proxyProcess != null) {
		this.proxyProcess.destroy();
		this.proxyProcess = null;
	}
}
}
