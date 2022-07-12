/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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
 *     Alexander Kriegisch - bug 286316: Get classpath via DataInputStream and
 *         use it in an isolated URLClassLoader, enabling formerly locked
 *         classpath JARs to be closed on Windows
 *******************************************************************************/
package org.eclipse.jdt.core.tests.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * <b>IMPORTANT NOTE:</b> When modifying this class, please copy the source into the static initialiser block for field
 * {@link TestVerifier#VERIFY_TEST_CODE_DEFAULT}. See also {@link TestVerifier#READ_VERIFY_TEST_FROM_FILE}, if you want
 * to dynamically load the source code directly from this file when running tests, which is a convenient way to test if
 * changes in this class work as expected, without the need to update the hard-coded default value every single time
 * during an ongoing refactoring.
 * <p>
 * In order to make the copying job easier, keep this class compatible with Java 5 language level. You may however use
 * things like {@code @Override} for interfaces, {@code assert} (if in a single line), {@code @SuppressWarnings},
 * because {@link TestVerifier#getVerifyTestsCode()} can filter them out dynamically. You should however avoid things
 * like diamonds, multi-catch, catch-with-resources and more recent Java features.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class VerifyTests {
	int portNumber;
	Socket socket;

private static URL[] classPathToURLs(String[] classPath) throws MalformedURLException {
	URL[] urls = new URL[classPath.length];
	for (int i = 0; i < classPath.length; i++) {
		urls[i] = new File(classPath[i]).toURI().toURL();
	}
	return urls;
}

public void loadAndRun(String className, String[] classPath) throws Throwable {
	URLClassLoader urlClassLoader = new URLClassLoader(classPathToURLs(classPath));
	try {
		//System.out.println("Loading " + className + "...");
		Class testClass = urlClassLoader.loadClass(className);
		//System.out.println("Loaded " + className);
		try {
			Method main = testClass.getMethod("main", new Class[] {String[].class});
			//System.out.println("Running " + className);
			main.invoke(null, new Object[] {new String[] {}});
			//System.out.println("Finished running " + className);
		} catch (NoSuchMethodException e) {
			return;
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	} finally {
		urlClassLoader.close();
	}
}
public static void main(String[] args) throws IOException {
	VerifyTests verify = new VerifyTests();
	verify.portNumber = Integer.parseInt(args[0]);
	verify.run();
}
public void run() throws IOException {
	this.socket = new Socket("localhost", this.portNumber);
	this.socket.setTcpNoDelay(true);

	DataInputStream in = new DataInputStream(this.socket.getInputStream());
	final DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
	while (true) {
		final String className = in.readUTF();
		final int length = in.readInt();
		final String[] classPath = new String[length];
		for (int i = 0; i < length; i++) {
			classPath[i] = in.readUTF();
		}
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					loadAndRun(className, classPath);
					out.writeBoolean(true);
					System.out.println(VerifyTests.class.getName());
					System.err.println(VerifyTests.class.getName());
				} catch (Throwable e) {
					e.printStackTrace();
					try {
						out.writeBoolean(false);
						System.out.println(VerifyTests.class.getName());
						System.err.println(VerifyTests.class.getName());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				// Flush all streams, in case the test executor VM is shut down before
				// the controlling VM receives the responses it depends on
				try {
					out.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.flush();
				System.err.flush();
			}
		};
		thread.start();
	}
}
}
