/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.eval;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
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

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DebugEvaluationSetup extends EvaluationSetup {

	VirtualMachine vm;

	public DebugEvaluationSetup(long complianceLevel) {
		super(complianceLevel);
	}

	protected void setUp() {
		if (this.context == null) {
			// Launch VM in evaluation mode
			try (ServerSocket evalServer = new ServerSocket(0)) {
				int debugPort = Util.getFreePort();
				int evalPort = evalServer.getLocalPort();
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
					e.printStackTrace();
					throw new Error(e.getMessage(), e);
				}
	
				// Thread that read the stout of the VM so that the VM doesn't block
				try {
					startReader("VM's stdout reader", this.launchedVM.getInputStream(), System.out);
				} catch (TargetException e) {
					e.printStackTrace();
				}
	
				// Thread that read the sterr of the VM so that the VM doesn't block
				try {
					startReader("VM's sterr reader", this.launchedVM.getErrorStream(), System.err);
				} catch (TargetException e) {
					e.printStackTrace();
				}
	
				// Start JDI connection (try 10 times)
				for (int i = 0; i < 10; i++) {
					try {
						VirtualMachineManager manager = org.eclipse.jdi.Bootstrap.virtualMachineManager();
						List connectors = manager.attachingConnectors();
						if (connectors.size() == 0) {
							System.err.println(getName() + ": could not get attachingConnectors() from VM");
							break;
						}
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
							argument.setValue("20000");
						}
						this.vm = connector.attach(args);
						System.out.println(getName() + ": connected to VM using port " + debugPort);
						
						// workaround pb with some VMs
						this.vm.resume();
	
						break;
					} catch (IllegalConnectorArgumentsException | IOException e) {
						e.printStackTrace();
						try {
							System.out.println(getName() + ": could not contact the VM at " + launcher.getTargetAddress() + ":" + debugPort + ". Retrying...");
							Thread.sleep(100);
						} catch (InterruptedException e2) {
							e2.printStackTrace();
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
						} catch (TargetException | IOException e) {
							e.printStackTrace();
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
									e.printStackTrace();
								}
							}
							if (this.launchedVM.isRunning()) {
								this.launchedVM.shutDown();
							}
						} catch (TargetException e) {
							e.printStackTrace();
						}
					}
					System.err.println(getName() + ": could not contact the VM");
					return;
				}
	
				// Create context
				this.context = new EvaluationContext();
	
				// Create target
				this.target = new TargetInterface();
				
				// allow 30s max to connect (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=188127)
				// Increased to 60 s for https://bugs.eclipse.org/bugs/show_bug.cgi?id=547417
				this.target.connect(evalServer, 60000); 
	
				assertTrue(getName() + ": failed to connect VM server", this.target.isConnected());
				
				System.out.println(getName() + ": connected to target using port " + debugPort);
				
				// Create name environment
				this.env = new FileSystem(Util.getJavaClassLibs(), new String[0], null);
			} catch (IOException e1) {
				throw new Error(getName() + ": Failed to open socket", e1);
			}
		}
		super.setUp();
	}
}
