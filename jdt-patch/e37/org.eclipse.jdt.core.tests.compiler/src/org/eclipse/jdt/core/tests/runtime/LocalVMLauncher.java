/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Nina Rinskaya
 *     		Fix for https://bugs.eclipse.org/bugs/show_bug.cgi?id=172820.
 *******************************************************************************/
package org.eclipse.jdt.core.tests.runtime;

import java.io.*;
import java.util.*;

import org.eclipse.jdt.core.tests.util.Util;

/**
 * The root of the VM launchers that launch VMs on the same machine.
 * <p>
 * A local VM launcher has the following limitations:
 * <ul>
 *   <li>It can only retrieve the running virtual machines that it has launched. So if
 *       a client is using 2 instances of <code>LocalVMLauncher</code>, each of these
 *       instances will be able to retrieve only a part of the running VMs.
 * </ul>
 */
public abstract class LocalVMLauncher implements RuntimeConstants {

	/**
	 * Whether the target has a file system and thus whether it supports writing
	 * class files to disk. See org.eclipse.jdt.core.tests.eval.target.CodeSnippetRunner for more
	 * information.
	 */
	public static final boolean TARGET_HAS_FILE_SYSTEM = true;
	public static final String REGULAR_CLASSPATH_DIRECTORY = "regularPath";
	public static final String BOOT_CLASSPATH_DIRECTORY = "bootPath";

	protected String[] bootPath;
	protected String[] classPath;
	protected int debugPort = -1;
	protected int evalPort = -1;
	protected String evalTargetPath;
	protected String[] programArguments;
	protected String programClass;
	protected Vector runningVMs = new Vector(); // a Vector of LocalVirtualMachine
	protected String[] vmArguments;
	protected String vmPath;

/**
 * Returns a launcher that will launch the same kind of VM that is currently running
 */
public static LocalVMLauncher getLauncher() {
	final String vmName = System.getProperty("java.vm.name");
	if ("J9".equals(vmName)) {
		return new J9VMLauncher();
	}
	if (vmName != null && vmName.indexOf("JRockit") != -1) {
		return new JRockitVMLauncher();
	}
	final String osName = System.getProperty("os.name");
	if (osName.startsWith("Mac")) {
		return new MacVMLauncher();
	}
	File file = new File(Util.getJREDirectory() + "/lib/rt.jar");
	if (file.exists()) {
		return new StandardVMLauncher();
	}
	if ("IBM J9SE VM".equals(vmName)) {
		return new SideCarJ9VMLauncher();
	}
	if ("DRLVM".equals(vmName)) {
		return new DRLVMLauncher();
	}
	return new SideCarVMLauncher();
}
/**
 * Builds the actual class path that is going to be passed to the VM.
 */
protected String buildClassPath() {
	StringBuffer classPathString = new StringBuffer();
	char pathSeparator = File.pathSeparatorChar;

	// Add jar support if in evaluation mode
	if (this.evalPort != -1) {
		classPathString.append(new File(this.evalTargetPath, SUPPORT_ZIP_FILE_NAME).getPath());
		classPathString.append(pathSeparator);
	}

	// Add class path given by client
	if (this.classPath != null) {
		int length = this.classPath.length;
		for (int i = 0; i < length; i++){
			classPathString.append(this.classPath[i]);
			classPathString.append(pathSeparator);
		}
	}

	// Add regular classpath directory if needed
	if (this.evalPort != -1 && TARGET_HAS_FILE_SYSTEM) {
		classPathString.append(this.evalTargetPath);
		classPathString.append(File.separatorChar);
		classPathString.append(REGULAR_CLASSPATH_DIRECTORY);
	}

	return classPathString.toString();
}
/**
 * Launches the VM by exec'ing the command line and returns the resulting Process.
 */
protected Process execCommandLine() throws TargetException {
	// Check that the VM path has been specified
	if (this.vmPath == null) {
		throw new TargetException("Path to the VM has not been specified");
	}

	// Check that the program class has been specified if not in evaluation mode
	if ((this.programClass == null) && (this.evalPort == -1)) {
		throw new TargetException("Program class has not been specified");
	}

	// Launch VM
	Process vmProcess= null;
	try {
		// Use Runtime.exec(String[]) with tokens because Runtime.exec(String) with commandLineString
		// does not properly handle spaces in arguments on Unix/Linux platforms.
		String[] commandLine = getCommandLine();

		// DEBUG
		/*for (int i = 0; i < commandLine.length; i++) {
			System.out.print(commandLine[i] + " ");
		}
		System.out.println();
		*/

		vmProcess= Runtime.getRuntime().exec(commandLine);
	} catch (IOException e) {
		throw new TargetException("Error launching VM at " + this.vmPath);
	}
	return vmProcess;
}
/**
 * Returns the boot class path used when a VM is launched.
 */
public String[] getBootClassPath() {
	return this.bootPath;
}
/**
 * Returns the class path used when a VM is launched.
 */
public String[] getClassPath() {
	return this.classPath;
}
/**
 * Returns the command line which will be used to launch the VM.
 * The segments are in the following order:
 * <p><ul>
 * <li> VM path,
 * <li> VM arguments,
 * <li> the class path,
 * <li> the program class
 * <li> the program arguments
 * </ul>
 */
public abstract String[] getCommandLine();
/**
 * Returns the debug port, or -1 if debug mode is disabled.
 * The default is -1.
 */
public int getDebugPort() {
	return this.debugPort;
}
/**
 * Returns the evaluation port for evaluation support.
 * The default is -1, indicating no evaluation support.
 *
 * @see #setEvalPort(int)
 */
public int getEvalPort() {
	return this.evalPort;
}
/**
 * Returns the evaluation target path for evaluation support.
 *
 * @see #setEvalTargetPath(String)
 */
public String getEvalTargetPath() {
	return this.evalTargetPath;
}
/**
 * Returns the arguments passed to the program class.
 * Returns null if the VM is being launched for evaluation support only.
 */
public String[] getProgramArguments() {
	if (this.evalPort != -1) {
		return null;
	}
	return this.programArguments;
}
/**
 * Returns the dot-separated, fully qualified name of the class to run.
 * It must implement main(String[] args).
 * Returns null if the VM is being launched for evaluation support only.
 */
public String getProgramClass() {
	if (this.evalPort != -1) {
		return null;
	}
	return this.programClass;
}
/**
 * Returns all the target VMs that are running at this launcher's target
 * address.
 * Note that these target VMs may or may not have been launched by this
 * launcher.
 * Note also that if the list of running VMs doesn't change on the target,
 * two calls to this method return VMs that are equal.
 *
 * @return the list of running target VMs
 */
public LocalVirtualMachine[] getRunningVirtualMachines() {
	// Select the VMs that are actually running
	Vector actuallyRunning = new Vector();
	Enumeration en = this.runningVMs.elements();
	while (en.hasMoreElements()) {
		LocalVirtualMachine vm = (LocalVirtualMachine)en.nextElement();
		if (vm.isRunning())
			actuallyRunning.addElement(vm);
	}
	this.runningVMs = actuallyRunning;

	// Return the running VMs
	int size = actuallyRunning.size();
	LocalVirtualMachine[] result = new LocalVirtualMachine[size];
	for (int i=0; i<size; i++)
		result[i] = (LocalVirtualMachine)actuallyRunning.elementAt(i);
	return result;
}
/**
 * Returns the address of the target where this launcher runs the target VMs. The format
 * of this address is transport specific.
 * For example, a VM launcher using a TCP/IP transport returns target addresses looking like:
 * <code>"localhost:2010"</code>, or <code>"joe.ibm.com"</code>.
 *
 * @return transport specific address of the target
 */
public String getTargetAddress() {
	return "localhost";
}
/**
 * Returns the VM-specific arguments. This does not include:
 * <p><ul>
 * <li>the VM path
 * <li>the class path or the boot class path
 * <li>the program class or program arguments
 * </ul>
 */
public String[] getVMArguments() {
	return this.vmArguments;
}
/**
 * Returns the path on disk of the VM to launch.
 */
public String getVMPath() {
	return this.vmPath;
}
/**
 * Initializes this context's target path by copying the jar file for the code snippet support
 * and by creating the 2 directories that will contain the code snippet classes (see TARGET_HAS_FILE_SYSTEM).
 * Add the code snipport root class to the boot path directory so that code snippets can be run in
 * java.* packages
 *
 * @throws TargetException if the path could not be initialized with the code snippet support
 */
protected void initTargetPath() throws TargetException {
	// create directories
	File directory = new File(this.evalTargetPath);
	directory.mkdirs();
	if (!directory.exists()) {
		throw new TargetException("Could not create directory " + this.evalTargetPath);
	}
	if (TARGET_HAS_FILE_SYSTEM) {
		File classesDirectory = new File(directory, REGULAR_CLASSPATH_DIRECTORY);
		classesDirectory.mkdir();
		if (!classesDirectory.exists()) {
			throw new TargetException("Could not create directory " + classesDirectory.getPath());
		}
		File bootDirectory = new File(directory, BOOT_CLASSPATH_DIRECTORY);
		bootDirectory.mkdir();
		if (!bootDirectory.exists()) {
			throw new TargetException("Could not create directory " + bootDirectory.getPath());
		}
		/*
		// add the code snippet root class to the boot path directory
		InputStream in = null;
		try {
			in = EvaluationContext.class.getResourceAsStream("/" + SUPPORT_ZIP_FILE_NAME);
			ZipInputStream zip = new ZipInputStream(in);
			String rootClassFileName = ROOT_FULL_CLASS_NAME.replace('.', '/') + ".class";
			while (true) {
				ZipEntry entry = zip.getNextEntry();
				if (entry.getName().equals(rootClassFileName)) {
					// read root class file contents
					int size = (int)entry.getSize();
					byte[] buffer = new byte[size];
					int totalRead = 0;
					int read = 0;
					while (totalRead < size) {
						read = zip.read(buffer, totalRead, size - totalRead);
						if (read != -1) {
							totalRead += read;
						}
					}
					// write root class file contents
					FileOutputStream out = null;
					try {
						File rootFile = new File(bootDirectory, rootClassFileName.replace('/', File.separatorChar));
						File parent = new File(rootFile.getParent());
						parent.mkdirs();
						out = new FileOutputStream(rootFile);
						out.write(buffer);
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
						if (out != null) {
							try {
								out.close();
							} catch (IOException e2) {
							}
						}
					}
					break;
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			if (in != null) {
				try {
					in.close();
				} catch (IOException e2) {
				}
			}
		}*/
	}

	// copy jar file
	InputStream in = null;
	FileOutputStream out = null;
	try {
		in = getClass().getResourceAsStream("/" + SUPPORT_ZIP_FILE_NAME);
		if (in == null) {
			throw new TargetException("Could not find resource /" + SUPPORT_ZIP_FILE_NAME);
		}
		int bufferLength = 1024;
		byte[] buffer = new byte[bufferLength];
		File file = new File(directory, SUPPORT_ZIP_FILE_NAME);
		out = new FileOutputStream(file);
		int read = 0;
		while (read != -1) {
			read = in.read(buffer, 0, bufferLength);
			if (read != -1) {
				out.write(buffer, 0, read);
			}
		}
	} catch (IOException e) {
		throw new TargetException("IOException while copying " + SUPPORT_ZIP_FILE_NAME + ": " + e.getMessage());
	} finally {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
			}
		}
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
			}
		}
	}
}
/**
 * Launches a new target VM with the registered arguments.
 * This operation returns once a new target VM has been launched.
 *
 * @exception TargetException if the target VM could not be launched.
 */
public LocalVirtualMachine launch() throws TargetException {
	// evaluation mode
	if (this.evalTargetPath != null) {
		// init target path
		initTargetPath();
	}

	// launch VM
	LocalVirtualMachine vm;
	Process p = execCommandLine();
	vm = new LocalVirtualMachine(p, this.debugPort, this.evalTargetPath);

	// TBD: Start reading VM stdout and stderr right away otherwise this may prevent the connection
	//		from happening.

	// add VM to list of known running VMs
	this.runningVMs.addElement(vm);
	return vm;
}
/**
 * Sets the boot class path used when a VM is launched.
 */
public void setBootClassPath(java.lang.String[] bootClassPath) {
	this.bootPath = bootClassPath;
}
/**
 * Sets the class path used when a VM is launched.
 */
public void setClassPath(String[] classPath) {
	this.classPath = classPath;
}
/**
 * Sets the debug port to use for debug support.
 * Specify -1 to disable debug mode.
 */
public void setDebugPort(int debugPort) {
	this.debugPort = debugPort;
}
/**
 * Sets the evaluation port to use for evaluation support.
 * Setting the port enables evaluation support.
 * Specify null to disable evaluation support.
 */
public void setEvalPort(int evalPort) {
	this.evalPort = evalPort;
}
/**
 * Sets the evaluation target path to use for evaluation support.
 */
public void setEvalTargetPath(String evalTargetPath) {
	this.evalTargetPath = evalTargetPath;
}
/**
 * Sets the arguments passed to the program class.
 * This is ignored if the VM is being launched for evaluation support only.
 */
public void setProgramArguments(String[] args) {
	this.programArguments = args;
}
/**
 * Sets the dot-separated, fully qualified name of the class to run.
 * It must implement main(String[] args).
 * This is ignored if the VM is being launched for evaluation support only.
 */
public void setProgramClass(String programClass) {
	this.programClass = programClass;
}
/**
 * Sets the VM-specific arguments. This does not include:
 * <p><ul>
 * <li>the VM path
 * <li>the class path or the boot class path
 * <li>the program class or program arguments
 * </ul>
 */
public void setVMArguments(String[] args) {
	this.vmArguments = args;
}
/**
 * Sets the path on disk of the VM to launch.
 */
public void setVMPath(String vmPath) {
	this.vmPath = vmPath;
}
}
