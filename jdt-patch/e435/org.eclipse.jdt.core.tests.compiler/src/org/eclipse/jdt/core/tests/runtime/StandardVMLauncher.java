/*******************************************************************************
 * Copyright (c) 2000, 2024 IBM Corporation and others.
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


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

/**
 * A standard VM launcher launches an external standard VM with
 * the given arguments on the same machine.
 */
public class StandardVMLauncher extends LocalVMLauncher {
	String batchFileName;
	private boolean isJrtBasedVM;
/**
 * Creates a new StandardVMLauncher that launches a standard VM
 * on the same machine.
 */
public StandardVMLauncher(boolean isJrtBasedVM) {
	super();
	this.isJrtBasedVM = isJrtBasedVM;
}
public StandardVMLauncher() {
	super();
}
/**
 * Builds the actual boot class path that is going to be passed to the VM.
 */
protected String buildBootClassPath() {
	StringBuilder bootPathString = new StringBuilder();
	char pathSeparator = File.pathSeparatorChar;

	if (this.bootPath != null) {
		// Add boot class path given by client
		int length = this.bootPath.length;
		for (int i = 0; i < length; i++){
			bootPathString.append(this.bootPath[i]);
			bootPathString.append(pathSeparator);
		}
	} else {
		if (!this.isJrtBasedVM) {
			// Add regular rt.jar
			bootPathString.append(this.vmPath);
			bootPathString.append(File.separator);
			if (!(this.vmPath.toLowerCase().endsWith("jre") || this.vmPath.toLowerCase().endsWith("jre" + File.separator))) {
				bootPathString.append("jre");
				bootPathString.append(File.separator);
			}
			bootPathString.append("lib");
			bootPathString.append(File.separator);
			bootPathString.append("rt.jar");
			bootPathString.append(pathSeparator);
		}
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
 * Returns the name of the batch file used to launch the VM.
 */
public String getBatchFileName() {
	return this.batchFileName;
}
/**
 * @see LocalVMLauncher#getCommandLine
 */
@Override
public String[] getCommandLine() {
	List<String> commandLine = new ArrayList<>();

	// VM binary
	StringBuilder vmLocation = new StringBuilder(this.vmPath);
	vmLocation
		.append(this.vmPath.endsWith(File.separator) ? "" : File.separator)
		.append("bin")
		.append(File.separator)
		.append("java");
	commandLine.add(String.valueOf(vmLocation));

	// VM arguments
	if (this.vmArguments != null) {
		for (int i = 0; i < this.vmArguments.length; i++) {
			commandLine.add(this.vmArguments[i]);
		}
	}

	long vmVersion = Util.getMajorMinorVMVersion();
	if (vmVersion != -1) {
		if (vmVersion < ClassFileConstants.JDK13) { // FailOverToOldVerifier deprecated from 13
			commandLine.add("-XX:-FailOverToOldVerifier");
		}
		if (vmVersion >= ClassFileConstants.JDK1_6) {
			commandLine.add("-Xverify:all");
		}
		if (vmVersion >= ClassFileConstants.JDK1_7) {
			commandLine.add("-XX:+UnlockExperimentalVMOptions");
		}
	}

	// debug mode
	if (this.debugPort != -1) {
		addDebugOptions(commandLine);
		addXnoagent(commandLine);
		// commandLine.add("-Djava.compiler=NONE");
		commandLine.add(
			"-Xrunjdwp:transport=dt_socket,address=" +
			this.debugPort +
			",server=y,suspend=n");
	}

	// boot classpath
	commandLine.add("-Xbootclasspath/a:" + buildBootClassPath());

	// regular classpath
	commandLine.add("-classpath");
	commandLine.add(buildClassPath());

	// code snippet runner class
	if (this.evalPort != -1) {
		commandLine.add(CODE_SNIPPET_RUNNER_CLASS_NAME);
	}

	// code snippet runner arguments
	if (this.evalPort != -1) {
		commandLine.add(EVALPORT_ARG);
		commandLine.add(Integer.toString(this.evalPort));
		if (TARGET_HAS_FILE_SYSTEM) {
			commandLine.add(CODESNIPPET_CLASSPATH_ARG);
			commandLine.add(this.evalTargetPath + File.separator + REGULAR_CLASSPATH_DIRECTORY);
			commandLine.add(CODESNIPPET_BOOTPATH_ARG);
			commandLine.add(this.evalTargetPath + File.separator + BOOT_CLASSPATH_DIRECTORY);
		}
	}

	// program class
	if (this.programClass != null) {
		commandLine.add(this.programClass);
	}

	// program arguments
	if (this.programArguments != null) {
		for (int i=0;i<this.programArguments.length;i++) {
			commandLine.add(this.programArguments[i]);
		}
	}

	String[] result;
	if (this.batchFileName!= null) {
		// Write to batch file if specified
		writeBatchFile(this.batchFileName, commandLine);
		result = new String[] {this.batchFileName};
	} else {
		result = new String[commandLine.size()];
		commandLine.toArray(result);
	}

	// check for spaces in result
	for (int i = 0; i < result.length; i++) {
		String argument = result[i];
		if (argument.indexOf(' ') != -1) {
			result[i] = "\"" + argument + "\"";
		}
	}

	return result;
}

private void addXnoagent(List<String> commandLine) {
    long vmVersion = Util.getMajorMinorVMVersion();
    if (vmVersion != -1 && vmVersion < ClassFileConstants.JDK22) {
        commandLine.add("-Xnoagent");
    }
}

/**
 * Sets the name of the batch file used to launch the VM.
 * When this option is set, the launcher writes the command line to the given batch file,
 * and it launches the  batch file. This causes a DOS console to be opened. Note it
 * doesn't delete the batch file when done.
 */
public void setBatchFileName(String batchFileName) {
	this.batchFileName = batchFileName;
}
protected void writeBatchFile(String fileName, List<String> commandLine) {
	FileOutputStream output = null;
	try {
		output = new FileOutputStream(fileName);
		PrintWriter out= new PrintWriter(output);
		for (String string : commandLine) {
			out.print(string);
			out.print(" ");
		}
		out.println("pause");
		out.close();
	} catch (IOException e) {
		e.printStackTrace();
	} finally {
		if (output != null) {
			try {
				output.close();
			} catch (IOException e2) {
				// ignore
			}
		}
	}
}
}
