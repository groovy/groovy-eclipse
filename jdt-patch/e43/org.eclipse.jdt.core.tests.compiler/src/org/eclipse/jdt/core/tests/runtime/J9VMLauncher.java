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

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.eclipse.jdt.core.tests.util.Util;

/**
 * A J9 VM launcher launches an external J9 VM (and J9 Proxy if needed) with
 * the given arguments on the same machine.
 * <p>
 * Unlike with the <code>StandardVMLauncher</code>, a debugger would have to
 * connect to the J9 Proxy instead of connecting to the VM directly. In this case,
 * the Proxy port is the specified debug port. Note that an internal debug port
 * must also be specified. This port is used for the communication between the
 * Proxy and the VM.
 */
public class J9VMLauncher extends LocalVMLauncher {
	int internalDebugPort = -1;
	String proxyOutFile;
	String symbolPath;
/**
 * Creates a new J9VMLauncher that launches a J9 VM
 * (and J9 Proxy if needed) on the same machine.
 */
public J9VMLauncher() {
	super();
}
/**
 * Builds the actual boot class path that is going to be passed to the VM.
 */
protected String buildBootClassPath() {
	StringBuffer bootPathString = new StringBuffer();
	char pathSeparator = File.pathSeparatorChar;

	if (this.bootPath != null) {
		// Add boot class path given by client
		int length = this.bootPath.length;
		for (int i = 0; i < length; i++){
			bootPathString.append(this.bootPath[i]);
			bootPathString.append(pathSeparator);
		}
	} else {
		// Add regular classes.zip
		bootPathString.append(this.vmPath);
		bootPathString.append(File.separator);
		bootPathString.append("lib");
		bootPathString.append(File.separator);
		bootPathString.append("jclMax");
		bootPathString.append(File.separator);
		bootPathString.append("classes.zip");
		bootPathString.append(pathSeparator);
	}

	// Add boot class path directory if needed
	if (this.evalTargetPath != null && TARGET_HAS_FILE_SYSTEM) {
		bootPathString.append(this.evalTargetPath);
		bootPathString.append(File.separatorChar);
		bootPathString.append(BOOT_CLASSPATH_DIRECTORY);
	}

	return bootPathString.toString();
}
/**
 * @see LocalVMLauncher#execCommandLine
 */
protected Process execCommandLine() throws TargetException {
	// Check that the internal debug port has been specified if in debug mode
	if (this.debugPort != -1 && this.internalDebugPort == -1) {
		throw new TargetException("Internal debug port has not been specified");
	}

	return super.execCommandLine();
}
/**
 * @see LocalVMLauncher#getCommandLine
 */
public String[] getCommandLine() {
	Vector commandLine = new Vector();

	// VM binary
	commandLine.addElement(
		this.vmPath +
		(this.vmPath.endsWith(File.separator) ? "" : File.separator) +
		"bin" +
		File.separator +
		"j9");

	// VM arguments
	if (this.vmArguments != null) {
		for (int i = 0; i < this.vmArguments.length; i++) {
			commandLine.addElement(this.vmArguments[i]);
		}
	}

	// debug mode
	if (this.debugPort != -1 && this.internalDebugPort != -1) {
		commandLine.addElement("-debug:" + this.internalDebugPort);
	}

	// boot class path
	commandLine.addElement("-Xbootclasspath:" + buildBootClassPath());

	// regular class path
	commandLine.addElement("-classpath");
	commandLine.addElement(buildClassPath());

	// code snippet runner class
	if (this.evalPort != -1) {
		commandLine.addElement(CODE_SNIPPET_RUNNER_CLASS_NAME);
	}

	// code snippet runner arguments
	if (this.evalPort != -1) {
		commandLine.addElement(EVALPORT_ARG);
		commandLine.addElement(Integer.toString(this.evalPort));
		if (TARGET_HAS_FILE_SYSTEM) {
			commandLine.addElement(CODESNIPPET_CLASSPATH_ARG);
			commandLine.addElement(this.evalTargetPath + File.separator + REGULAR_CLASSPATH_DIRECTORY);
			commandLine.addElement(CODESNIPPET_BOOTPATH_ARG);
			commandLine.addElement(this.evalTargetPath + File.separator + BOOT_CLASSPATH_DIRECTORY);
		}
	}

	// program class
	if (this.programClass != null) {
		commandLine.addElement(this.programClass);
	}

	// program arguments
	if (this.programArguments != null) {
		for (int i=0;i<this.programArguments.length;i++) {
			commandLine.addElement(this.programArguments[i]);
		}
	}

	String[] result= new String[commandLine.size()];
	commandLine.copyInto(result);

	// check for spaces in result
	for (int i = 0; i < result.length; i++) {
		String argument = result[i];
		if (argument.indexOf(' ') != -1) {
			result[i] = "\"" + argument + "\"";
		}
	}

	return result;
}
/**
 * Returns the debug port the J9 Proxy uses to connect to the J9 VM.
 * The value is unspecified if debug mode is disabled.
 * Note that the regular debug port is the port used to connect the J9 Proxy and
 * the IDE in the case of the J9 VM Launcher.
 */
public int getInternalDebugPort() {
	return this.internalDebugPort;
}
/**
 * Returns the command line which will be used to launch the Proxy.
 */
public String[] getProxyCommandLine() {
	Vector commandLine = new Vector();

	// Proxy binary
	commandLine.addElement(
		this.vmPath +
		(this.vmPath.endsWith(File.separator) ? "" : File.separator) +
		"bin" +
		File.separator +
		"j9proxy");

	// Arguments
	commandLine.addElement(getTargetAddress() + ":" + this.internalDebugPort);
	commandLine.addElement(Integer.toString(this.debugPort));
	if (this.symbolPath != null && this.symbolPath != "") {
		commandLine.addElement(this.symbolPath);
	}

	String[] result= new String[commandLine.size()];
	commandLine.copyInto(result);
	return result;
}
/**
 * Returns the full path name to the file where the proxy output is redirected.
 * Returns "con" if the proxy output is redirected to the stdout.
 * Returns null if the proxy output is not redirected.
 */
public String getProxyOutFile() {
	return this.proxyOutFile;
}
/**
 * Returns the full path name to the symbol file used by the J9 Proxy.
 * Returns null if the no symbol file is passed to the J9 Proxy.
 */
public String getSymbolPath() {
	return this.symbolPath;
}
/**
 * @see LocalVMLauncher#launch
 */
public LocalVirtualMachine launch() throws TargetException {
	// Launch VM
	LocalVirtualMachine localVM = super.launch();

	// Launch Proxy if needed
	Process proxyProcess= null;
	if (this.debugPort != -1) {
		try {
			// Use Runtime.exec(String[]) with tokens because Runtime.exec(String) with commandLineString
			// does not properly handle spaces in arguments on Unix/Linux platforms.
			String[] commandLine = getProxyCommandLine();

			// DEBUG
			/*
			for (int i = 0; i < commandLine.length; i++) {
				System.out.print(commandLine[i] + " ");
			}
			System.out.println();
			*/

			proxyProcess= Runtime.getRuntime().exec(commandLine);
		} catch (IOException e) {
			localVM.shutDown();
			throw new TargetException("Error launching J9 Proxy at " + this.vmPath);
		}
	}

	// Transform launched VM into J9 VM
	Process vmProcess = localVM.process;
	this.runningVMs.removeElement(localVM);
	J9VirtualMachine vm= new J9VirtualMachine(vmProcess, this.debugPort, this.evalTargetPath, proxyProcess, this.proxyOutFile);
	this.runningVMs.addElement(vm);
	return vm;
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.runtime.LocalVMLauncher#setDebugPort(int)
 */
public void setDebugPort(int debugPort) {
	super.setDebugPort(debugPort);

	// specify default internal debug port as well
	setInternalDebugPort(Util.getFreePort());
}

/**
 * Sets the debug port the J9 Proxy uses to connect to the J9 VM.
 * This is mandatory if debug mode is enabled.
 * This is ignored if debug mode is disabled.
 * Note that the regular debug port is the port used to connect the J9 Proxy and
 * the IDE in the case of the J9 VM Launcher.
 */
public void setInternalDebugPort(int internalDebugPort) {
	this.internalDebugPort = internalDebugPort;
}
/**
 * Sets the full path name to the file where the proxy output must be redirected.
 * Specify "con" if the proxy output must be redirected to the stdout.
 * Specify null if the proxy output must not be redirected (default value).
 * This is ignored if debug mode is disabled.
 */
public void setProxyOutFile(String proxyOutFile) {
	this.proxyOutFile = proxyOutFile;
}
/**
 * Sets the full path name to the symbol file used by the J9 Proxy.
 * Specify null if the no symbol file must be passed to the J9 Proxy (default value).
 * This is ignored if debug mode is disabled.
 */
public void setSymbolPath(String symbolPath) {
	this.symbolPath = symbolPath;
}
}
