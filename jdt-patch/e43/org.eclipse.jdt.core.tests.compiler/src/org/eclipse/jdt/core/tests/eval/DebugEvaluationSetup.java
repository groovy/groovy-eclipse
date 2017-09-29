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
package org.eclipse.jdt.core.tests.eval;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.tests.runtime.LocalVMLauncher;
import org.eclipse.jdt.core.tests.runtime.TargetException;
import org.eclipse.jdt.core.tests.runtime.TargetInterface;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.eval.EvaluationContext;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

public class DebugEvaluationSetup extends EvaluationSetup {

	VirtualMachine vm;

	public DebugEvaluationSetup(long complianceLevel) {
		super(complianceLevel);
	}

	protected void setUp() {
		if (this.context == null) {
			// Launch VM in evaluation mode
			int debugPort = Util.getFreePort();
			int evalPort = Util.getFreePort();
			LocalVMLauncher launcher;
			try {
				launcher = LocalVMLauncher.getLauncher();
				launcher.setVMArguments(new String[]{"-verify"});
				launcher.setVMPath(JRE_PATH);
				launcher.setEvalPort(evalPort);
				launcher.setEvalTargetPath(EVAL_DIRECTORY);
				launcher.setDebugPort(debugPort);
				this.launchedVM = launcher.launch();
			} catch (TargetException e) {
				throw new Error(e.getMessage());
			}

			// Thread that read the stout of the VM so that the VM doesn't block
			try {
				startReader("VM's stdout reader", this.launchedVM.getInputStream(), System.out);
			} catch (TargetException e) {
			}

			// Thread that read the sterr of the VM so that the VM doesn't block
			try {
				startReader("VM's sterr reader", this.launchedVM.getErrorStream(), System.err);
			} catch (TargetException e) {
			}

			// Start JDI connection (try 10 times)
			for (int i = 0; i < 10; i++) {
				try {
					VirtualMachineManager manager = org.eclipse.jdi.Bootstrap.virtualMachineManager();
					List connectors = manager.attachingConnectors();
					if (connectors.size() == 0)
						break;
					AttachingConnector connector = (AttachingConnector)connectors.get(0);
					Map args = connector.defaultArguments();
					Connector.Argument argument = (Connector.Argument)args.get("port");
					if (argument != null) {
						argument.setValue(String.valueOf(debugPort));
					}
					argument = (Connector.Argument)args.get("hostname");
					if (argument != null) {
						argument.setValue(launcher.getTargetAddress());
					}
					argument = (Connector.Argument)args.get("timeout");
					if (argument != null) {
						argument.setValue("10000");
					}
					this.vm = connector.attach(args);

					// workaround pb with some VMs
					this.vm.resume();

					break;
				} catch (IllegalConnectorArgumentsException e) {
					e.printStackTrace();
					try {
						System.out.println("Could not contact the VM at " + launcher.getTargetAddress() + ":" + debugPort + ". Retrying...");
						Thread.sleep(100);
					} catch (InterruptedException e2) {
					}
				} catch (IOException e) {
					e.printStackTrace();
					try {
						System.out.println("Could not contact the VM at " + launcher.getTargetAddress() + ":" + debugPort + ". Retrying...");
						Thread.sleep(100);
					} catch (InterruptedException e2) {
					}
				}
			}
			if (this.vm == null) {
				if (this.launchedVM != null) {
					// If the VM is not running, output error stream
					try {
						if (!this.launchedVM.isRunning()) {
							InputStream in = this.launchedVM.getErrorStream();
							int read;
							do {
								read = in.read();
								if (read != -1)
									System.out.print((char)read);
							} while (read != -1);
						}
					} catch (TargetException e) {
					} catch (IOException e) {
					}

					// Shut it down
					try {
						if (this.target != null) {
							this.target.disconnect(); // Close the socket first so that the OS resource has a chance to be freed.
						}
						int retry = 0;
						while (this.launchedVM.isRunning() && (++retry < 20)) {
							try {
								Thread.sleep(retry * 100);
							} catch (InterruptedException e) {
							}
						}
						if (this.launchedVM.isRunning()) {
							this.launchedVM.shutDown();
						}
					} catch (TargetException e) {
					}
				}
				System.err.println("Could not contact the VM");
				return;
			}

			// Create context
			this.context = new EvaluationContext();

			// Create target
			this.target = new TargetInterface();
			this.target.connect("localhost", evalPort, 30000); // allow 30s max to connect (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=188127)

			// Create name environment
			this.env = new FileSystem(Util.getJavaClassLibs(), new String[0], null);
		}
		super.setUp();
	}
}
