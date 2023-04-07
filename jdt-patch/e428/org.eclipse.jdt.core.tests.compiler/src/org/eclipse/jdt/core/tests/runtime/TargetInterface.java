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
package org.eclipse.jdt.core.tests.runtime;

import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.util.Util;

import java.io.*;
import java.net.*;
/**
 * This is the interface to the target VM. It connects to an IDEInterface on the target side
 * using TCP/IO to send request for code snippet evaluation and to get the result back.
 *
 * @see org.eclipse.jdt.core.tests.eval.target.IDEInterface for details about the protocol.
 */
public class TargetInterface {
	/**
	 * Whether class files should be written in d:\eval\ instead of sending them to the target
	 * NB: d:\eval should contain a batch file TestCodeSnippet.bat with the following contents:
	 *		SET JDK=c:\jdk1.2.2
	 *		SET EVAL=d:\eval
	 *		%JDK%\bin\java -Xbootclasspath:%JDK%\jre\lib\rt.jar;%EVAL%\javaClasses; -classpath c:\temp;%EVAL%\snippets;%EVAL%\classes;"d:\ide\project_resources\Eclipse Java Evaluation\CodeSnippetSupport.jar" CodeSnippetTester %1
	 */
	static final boolean DEBUG = false;
	String codeSnippetClassName;

	/**
	 * Whether timing info should be printed to stdout
	 */
	static final boolean TIMING = false;
	long sentTime;

	/**
	 * The connection to the target's ide interface.
	 */
	Socket socket;

	public static class Result {
		public char[] displayString;
		public char[] typeName;
	}

/**
 * (PRIVATE API)
 * Connects this interface to the target.
 * Try as long as the given time (in ms) has not expired.
 * Use isConnected() to find out if the connection was successful.
 */
public void connect(ServerSocket server, int timeout) {
	if (isConnected()) {
		return;
	}
	if (server != null) {
		long startTime = System.currentTimeMillis();
		do {
			try {
				this.socket = server.accept();
				this.socket.setTcpNoDelay(true);
				break;
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (this.socket == null) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
		} while (!isConnected() && (System.currentTimeMillis() - startTime) < timeout);
	}
}
/**
 * (PRIVATE API)
 * Disconnects this interface from the target.
 */
public void disconnect() {
	if (this.socket != null) {
		try {
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
			// Already closed. Nothing more to do
		}
		this.socket = null;
	}
}
/**
 * Returns the result of the evaluation sent previously to the target.
 */
public Result getResult() {
	boolean hasValue = false;
	String typeName = null;
	String toString = null;
	if (DEBUG) {
		hasValue = true;
		typeName = "TargetInterface in debug mode. Run d:\\eval\\TestCodeSnippet.bat d:\\eval\\snippets\\" + this.codeSnippetClassName;
		toString = "";
	} else {
		if (isConnected()) {
			// TBD: Read type name and toString as a character array
			try {
				DataInputStream in = new DataInputStream(this.socket.getInputStream());
				hasValue = in.readBoolean();
				if (hasValue) {
					typeName = in.readUTF();
					toString = in.readUTF();
				} else {
					typeName = null;
					toString = null;
				}
			} catch (IOException e) {
				// The socket has likely been closed on the other end. So the code snippet runner has stopped.
				hasValue = true;
				typeName = e.getMessage();
				toString = "";
				disconnect();
			}
		} else {
			hasValue = true;
			typeName = "Connection has been lost";
			toString = "";
		}
	}
	if (TIMING) {
		System.out.println("Time to send compiled classes, run on target and get result is " + (System.currentTimeMillis() - this.sentTime) + "ms");
	}
	Result result = new Result();
	result.displayString = toString == null ? null : toString.toCharArray();
	result.typeName = typeName == null ? null : typeName.toCharArray();
	return result;
}
/**
 * Returns whether this interface is connected to the target.
 */
public boolean isConnected() {
	return this.socket != null;
}
/**
 * Sends the given class definitions to the target for loading and (if specified) for running.
 */
public void sendClasses(boolean mustRun, ClassFile[] classes) throws TargetException {
	if (DEBUG) {
		for (int i = 0; i < classes.length; i++) {
			String className = new String(classes[i].fileName()).replace('/', '\\') + ".class";
			if ((i == 0) && (className.indexOf("CodeSnippet") != -1)) {
				this.codeSnippetClassName = className;
				try {
					Util.writeToDisk(true, "d:\\eval\\snippets", className, classes[0]);
				} catch(IOException e) {
					e.printStackTrace();
				}
			} else {
				String dirName;
				if (className.startsWith("java")) {
					dirName = "d:\\eval\\" + LocalVMLauncher.BOOT_CLASSPATH_DIRECTORY;
				} else {
					dirName = "d:\\eval\\" + LocalVMLauncher.REGULAR_CLASSPATH_DIRECTORY;
				}
				try {
					Util.writeToDisk(true, dirName, className, classes[i]);
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
	} else {
		if (TIMING) {
			this.sentTime = System.currentTimeMillis();
		}
		if (!isConnected()) {
			throw new TargetException("Connection to the target VM has been lost");
		}
		try {
			DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
			out.writeBoolean(mustRun);
			out.writeInt(classes.length);
			for (int i = 0; i < classes.length; i++) {
				byte[] classDefinition = classes[i].getBytes();
				out.writeInt(classDefinition.length);
				out.write(classDefinition);
			}
		} catch (IOException e) {
			e.printStackTrace();
			// The socket has likely been closed on the other end. So the code snippet runner has stopped.
			disconnect();
		}
	}
}
}
