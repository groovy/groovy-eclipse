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
package org.eclipse.jdt.core.tests.eval.target;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
/**
 * The interface to the IDE. When connected, it uses TCP/IP sockets
 * to get code snippet classes and global variable classes from the IDE.
 * It sends the result of the evaluation back using the same socket.
 * <p>
 * The format from the socket input stream is the following:
 * <pre>
 *		[run flag: boolean coded on 1 byte]
 * 		[number of class files: int coded on 4 bytes]
 *		*[
 *			[length of class file: int coded on 4 bytes]
 *      	[class file: Java Class file format]
 *       ]
 * </pre>
 * This sequence is infinitely repeated until the input socket stream is closed.
 * <p>
 * The format from the socket output stream is the following:
 * <pre>
 * 		[has result flag: 1 if there is a result, 0 otherwise]
 * 		if there is a result:
 *		[fully qualified type name of result: utf8 encoded string]
 *      [toString representation of result: utf8 encoded string]
 * </pre>
 * This sequence is infinitely repeated until the output socket stream is closed.
 */
@SuppressWarnings({ "rawtypes" })
public class IDEInterface {
	/**
	 * Whether timing info should be printed to stdout
	 */
	static final boolean TIMING = false;
	long startTime;

	int portNumber = 0;
	Socket socket;
/**
 * Creates a new IDEInterface.
 */
IDEInterface(int portNumber) {
	this.portNumber = portNumber;
}
/**
 * Waits for a connection from the ide on the given port.
 * @throws IOException if the connection could not be established.
 */
void connect() throws IOException {
	this.socket = new Socket("localhost", this.portNumber);
	this.socket.setTcpNoDelay(true);
}
/**
 * Disconnects this interface from the IDE.
 */
void disconnect() {
	if (this.socket != null) {
		try {
			this.socket.close();
		} catch (IOException e2) {
			// Ignore
		}
		this.socket = null;
	}
}
/**
 * Returns the class definitions of the classes that compose the next code snippet to evaluate.
 */
protected byte[][] getNextClasses() {
	if (this.socket == null) {
		return new byte[0][];
	}
	if (TIMING) {
		this.startTime = System.currentTimeMillis();
	}
	try {
		DataInputStream in = new DataInputStream(this.socket.getInputStream());
		int numberOfClasses = in.readInt();
		byte[][] result = new byte[numberOfClasses][];
		for (int i = 0; i < numberOfClasses; i++) {
			int lengthOfClassFile = in.readInt();
			byte[] classFile = new byte[lengthOfClassFile];
			int read = 0;
			while (read < lengthOfClassFile && read != -1) {
				read += in.read(classFile, read, lengthOfClassFile - read);
			}
			result[i] = classFile;
		}
		return result;
	} catch (IOException e) {
		// The socket has likely been closed on the other end, close this end too.
		disconnect();
		return new byte[0][];
	}
}
/**
 * Returns whether the code snippet classes that follow should be run or just loaded.
 */
protected boolean getRunFlag() {
	if (this.socket == null) {
		return false;
	}
	if (TIMING) {
		this.startTime = System.currentTimeMillis();
	}
	try {
		DataInputStream in = new DataInputStream(this.socket.getInputStream());
		return in.readBoolean();
	} catch (IOException e) {
		// The socket has likely been closed on the other end, close this end too.
		disconnect();
		return false;
	}
}
/**
 * Returns whether this interface is connected to the IDE.
 */
boolean isConnected() {
	return this.socket != null;
}
/**
 * Sends the result of the evaluation to the IDE.
 */
protected void sendResult(Class resultType, Object resultValue) {
	if (this.socket == null) {
		return;
	}
	try {
		DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
		if (resultType == void.class) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(resultType.isPrimitive() ? resultType.toString() : resultType.getName());
			out.writeUTF(resultValue == null ? "null" : resultValue.toString());
		}
	} catch (IOException e) {
		// The socket has likely been closed on the other end, disconnect this end too
		disconnect();
	}
	if (TIMING) {
		System.out.println("Time to run on target is " + (System.currentTimeMillis() - this.startTime) + "ms");
	}
}
}
