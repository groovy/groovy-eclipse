/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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
import java.util.ArrayList;
import java.util.List;
/**
 * This is a new vm launcher to support sidecar settings
 */
public class MacVMLauncher extends StandardVMLauncher {
/**
 * @see LocalVMLauncher#getCommandLine
 */
@Override
public String[] getCommandLine() {
	List<String> commandLine = new ArrayList<>();

	// VM binary
	commandLine.add(
		this.vmPath +
		(this.vmPath.endsWith(File.separator) ? "" : File.separator) +
		"bin" +
		File.separator +
		"java");

	// VM arguments
	if (this.vmArguments != null) {
		for (int i = 0; i < this.vmArguments.length; i++) {
			commandLine.add(this.vmArguments[i]);
		}
	}

	// boot classpath
	commandLine.add("-Xbootclasspath/a:" + buildBootClassPath());

	// debug mode
	if (this.debugPort != -1) {
		commandLine.add("-Xdebug");
		commandLine.add("-Xnoagent");
		// commandLine.add("-Djava.compiler=NONE");
		commandLine.add(
			"-Xrunjdwp:transport=dt_socket,address=" +
			this.debugPort +
			",server=y,suspend=n");
	} else {
		commandLine.add("-Xdebug");
	}

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

/**
 * Builds the actual boot class path that is going to be passed to the VM.
 */
@Override
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
	}

	// Add boot class path directory if needed
	if (this.evalTargetPath != null && TARGET_HAS_FILE_SYSTEM) {
		bootPathString.append(this.evalTargetPath);
		bootPathString.append(File.separatorChar);
		bootPathString.append(BOOT_CLASSPATH_DIRECTORY);
	}

	return bootPathString.toString();
}
}
