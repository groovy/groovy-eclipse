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

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

import org.eclipse.jdt.core.tests.runtime.LocalVirtualMachine;

import org.eclipse.jdt.core.tests.runtime.LocalVMLauncher;
import org.eclipse.jdt.core.tests.runtime.TargetException;
import org.eclipse.jdt.core.tests.runtime.TargetInterface;
import org.eclipse.jdt.core.tests.util.CompilerTestSetup;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.eval.EvaluationContext;

public class EvaluationSetup extends CompilerTestSetup {

	public static final String EVAL_DIRECTORY = Util.getOutputDirectory() + File.separator + "evaluation";
	public static final String JRE_PATH = Util.getJREDirectory();

	EvaluationContext context;
	TargetInterface target;
	LocalVirtualMachine launchedVM;
	INameEnvironment env;

	public EvaluationSetup(long complianceLevel) {
		super(complianceLevel);
	}

	protected void setUp() {
		if (this.context == null) { // non null if called from subclass
			// Launch VM in evaluation mode
			int evalPort = Util.getFreePort();
			try {
				LocalVMLauncher launcher = LocalVMLauncher.getLauncher();
				launcher.setVMPath(JRE_PATH);
				launcher.setEvalPort(evalPort);
				launcher.setEvalTargetPath(EVAL_DIRECTORY);
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

	protected void startReader(String name, final InputStream in, final PrintStream out) {
		(new Thread(name) {
			public void run() {
				int read = 0;
				while (read != -1) {
					try {
						read = in.read();
					} catch (java.io.IOException e) {
						read = -1;
					}
					if (read != -1) {
						out.print((char)read);
					}
				}
			}
		}).start();
	}

	protected void tearDown() {
		if (this.context != null) {
			LocalVirtualMachine vm = this.launchedVM;
			if (vm != null) {
				try {
					if (this.target != null) {
						this.target.disconnect(); // Close the socket first so that the OS resource has a chance to be freed.
					}
					int retry = 0;
					while (vm.isRunning() && (++retry < 20)) {
						try {
							Thread.sleep(retry * 100);
						} catch (InterruptedException e) {
						}
					}
					if (vm.isRunning()) {
						vm.shutDown();
					}
					this.context = null;
				} catch (TargetException e) {
					throw new Error(e.getMessage());
				}
			}
		}
		if (this.env != null) {
			this.env.cleanup();
		}
	}
}
