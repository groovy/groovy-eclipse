/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.util;

import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
import org.eclipse.jdt.core.tests.runtime.*;
import java.io.*;
import java.net.*;
/**
 * Verifies that the .class files resulting from a compilation can be loaded
 * in a VM and that they can be run.
 */
public class TestVerifier {
	public String failureReason;

	boolean reuseVM = true;
	String[] classpathCache;
	LocalVirtualMachine vm;
	StringBuffer outputBuffer;
	StringBuffer errorBuffer;
	Socket socket;
public TestVerifier(boolean reuseVM) {
	this.reuseVM = reuseVM;
}
private boolean checkBuffers(String outputString, String errorString,
		String sourceFileName, String expectedOutputString, String expectedErrorStringStart) {
	boolean didMatchExpectation = true;
	String platformIndependantString;
	this.failureReason = null;
	if (expectedOutputString != null) {
		platformIndependantString = Util.convertToIndependantLineDelimiter(outputString.trim());
		if (!Util.convertToIndependantLineDelimiter(expectedOutputString).equals(platformIndependantString)) {
			System.out.println(Util.displayString(platformIndependantString, 2));
			this.failureReason =
				"Unexpected output running resulting class file for "
					+ sourceFileName
					+ ":\n"
					+ "--[START]--\n"
					+ outputString
					+ "---[END]---\n";
			didMatchExpectation = false;
		}
	}
	String trimmedErrorString = errorString.trim();
	if (expectedErrorStringStart != null) {
		platformIndependantString = Util.convertToIndependantLineDelimiter(trimmedErrorString);
		if (expectedErrorStringStart.length() == 0 && platformIndependantString.length() > 0 ||
				!platformIndependantString.startsWith(Util.convertToIndependantLineDelimiter(expectedErrorStringStart))) {
			/*
			 * This is an opportunistic heuristic for error strings comparison:
			 * - null means skip test;
			 * - empty means exactly empty;
			 * - other means starts with.
			 * If this became insufficient, we could envision using specific
			 * matchers for specific needs.
			 */
			System.out.println(Util.displayString(platformIndependantString, 2));
			this.failureReason =
				"Unexpected error running resulting class file for "
					+ sourceFileName
					+ ":\n"
					+ "--[START]--\n"
					+ errorString
					+ "---[END]---\n";
			didMatchExpectation = false;
		}
	} else if (trimmedErrorString.length() != 0){
		platformIndependantString = Util.convertToIndependantLineDelimiter(trimmedErrorString);
		System.out.println(Util.displayString(platformIndependantString, 2));
		this.failureReason =
			"Unexpected error running resulting class file for "
				+ sourceFileName
				+ ":\n"
				+ "--[START]--\n"
				+ errorString
				+ "---[END]---\n";
		didMatchExpectation = false;
	}
	return didMatchExpectation;
}

private boolean checkBuffersThrowingError(String errorString, String sourceFileName, String expectedSuccessOutputString) {

	if (errorString.length() > 0 && errorString.indexOf(expectedSuccessOutputString) != -1) {
		return true;
	}

	this.failureReason =
		"Expected error not thrown for "
			+ sourceFileName
			+ ":\n"
			+ expectedSuccessOutputString;
	return false;
}

private void compileVerifyTests(String verifierDir) {
	String fullyQualifiedName = VerifyTests.class.getName();

	int lastDot = fullyQualifiedName.lastIndexOf('.');
	String packageName = fullyQualifiedName.substring(0, lastDot);
	String simpleName = fullyQualifiedName.substring(lastDot + 1);

	String dirName = verifierDir.replace('\\', '/') + "/" + packageName.replace('.', '/');
	File dir = new File(dirName.replace('/', File.separatorChar));
	if (!dir.exists() && !dir.mkdirs()) {
		System.out.println("Could not create " + dir);
		return;
	}
	String fileName = dir + File.separator + simpleName + ".java";
	Util.writeToFile(getVerifyTestsCode(), fileName);
	BatchCompiler.compile("\"" + fileName + "\" -d \"" + verifierDir + "\" -classpath \"" + Util.getJavaClassLibsAsString() + "\"", new PrintWriter(System.out), new PrintWriter(System.err), null/*progress*/);
}
public void execute(String className, String[] classpaths) {
	this.outputBuffer = new StringBuffer();
	this.errorBuffer = new StringBuffer();

	launchAndRun(className, classpaths, null, null);
}
protected void finalize() throws Throwable {
	shutDown();
}
public String getExecutionOutput(){
	return this.outputBuffer.toString();
}

public String getExecutionError(){
	return this.errorBuffer.toString();
}
/**
 * Returns the code of the VerifyTests class.
 *
 * IMPORTANT NOTE: DO NOTE EDIT BUT GENERATE INSTEAD (see below)
 *
 * To generate:
 * - export VerifyTests.java to d:/temp
 * - inspect org.eclipse.jdt.core.tests.util.Util.fileContentToDisplayString("d:/temp/VerifyTests.java", 2, true)
 */
private String getVerifyTestsCode() {
	return
		"/*******************************************************************************" +
		" * Copyright (c) 2000, 2005 IBM Corporation and others." +
		" * All rights reserved. This program and the accompanying materials" +
		" * are made available under the terms of the Eclipse Public License v1.0" +
		" * which accompanies this distribution, and is available at" +
		" * http://www.eclipse.org/legal/epl-v10.html" +
		" *" +
		" * Contributors:" +
		" *     IBM Corporation - initial API and implementation" +
		" *******************************************************************************/" +
		"package org.eclipse.jdt.core.tests.util;\n" +
		"\n" +
		"import java.lang.reflect.*;\n" +
		"import java.io.*;\n" +
		"import java.net.*;\n" +
		"import java.util.*;\n" +
		"\n" +
		"/******************************************************\n" +
		" * \n" +
		" * IMPORTANT NOTE: If modifying this class, copy the source to TestVerifier#getVerifyTestsCode()\n" +
		" * (see this method for details)\n" +
		" * \n" +
		" ******************************************************/\n" +
		"\n" +
		"public class VerifyTests {\n" +
		"	int portNumber;\n" +
		"	Socket socket;\n" +
		"\n" +
		"/**\n" +
		" * NOTE: Code copied from junit.util.TestCaseClassLoader.\n" +
		" *\n" +
		" * A custom class loader which enables the reloading\n" +
		" * of classes for each test run. The class loader\n" +
		" * can be configured with a list of package paths that\n" +
		" * should be excluded from loading. The loading\n" +
		" * of these packages is delegated to the system class\n" +
		" * loader. They will be shared across test runs.\n" +
		" * <p>\n" +
		" * The list of excluded package paths is specified in\n" +
		" * a properties file \"excluded.properties\" that is located in \n" +
		" * the same place as the TestCaseClassLoader class.\n" +
		" * <p>\n" +
		" * <b>Known limitation:</b> the VerifyClassLoader cannot load classes\n" +
		" * from jar files.\n" +
		" */\n" +
		"\n" +
		"\n" +
		"public class VerifyClassLoader extends ClassLoader {\n" +
		"	/** scanned class path */\n" +
		"	private String[] fPathItems;\n" +
		"	\n" +
		"	/** excluded paths */\n" +
		"	private String[] fExcluded= {};\n" +
		"\n" +
		"	/**\n" +
		"	 * Constructs a VerifyClassLoader. It scans the class path\n" +
		"	 * and the excluded package paths\n" +
		"	 */\n" +
		"	public VerifyClassLoader() {\n" +
		"		super();\n" +
		"		String classPath= System.getProperty(\"java.class.path\");\n" +
		"		String separator= System.getProperty(\"path.separator\");\n" +
		"		\n" +
		"		// first pass: count elements\n" +
		"		StringTokenizer st= new StringTokenizer(classPath, separator);\n" +
		"		int i= 0;\n" +
		"		while (st.hasMoreTokens()) {\n" +
		"			st.nextToken();\n" +
		"			i++;\n" +
		"		}\n" +
		"		// second pass: split\n" +
		"		fPathItems= new String[i];\n" +
		"		st= new StringTokenizer(classPath, separator);\n" +
		"		i= 0;\n" +
		"		while (st.hasMoreTokens()) {\n" +
		"			fPathItems[i++]= st.nextToken();\n" +
		"		}\n" +
		"\n" +
		"	}\n" +
		"	public java.net.URL getResource(String name) {\n" +
		"		return ClassLoader.getSystemResource(name);\n" +
		"	}\n" +
		"	public InputStream getResourceAsStream(String name) {\n" +
		"		return ClassLoader.getSystemResourceAsStream(name);\n" +
		"	}\n" +
		"	protected boolean isExcluded(String name) {\n" +
		"		// exclude the \"java\" packages.\n" +
		"		// They always need to be excluded so that they are loaded by the system class loader\n" +
		"		if (name.startsWith(\"java\"))\n" +
		"			return true;\n" +
		"			\n" +
		"		// exclude the user defined package paths\n" +
		"		for (int i= 0; i < fExcluded.length; i++) {\n" +
		"			if (name.startsWith(fExcluded[i])) {\n" +
		"				return true;\n" +
		"			}\n" +
		"		}\n" +
		"		return false;	\n" +
		"	}\n" +
		"	public synchronized Class loadClass(String name, boolean resolve)\n" +
		"		throws ClassNotFoundException {\n" +
		"			\n" +
		"		Class c= findLoadedClass(name);\n" +
		"		if (c != null)\n" +
		"			return c;\n" +
		"		//\n" +
		"		// Delegate the loading of excluded classes to the\n" +
		"		// standard class loader.\n" +
		"		//\n" +
		"		if (isExcluded(name)) {\n" +
		"			try {\n" +
		"				c= findSystemClass(name);\n" +
		"				return c;\n" +
		"			} catch (ClassNotFoundException e) {\n" +
		"				// keep searching\n" +
		"			}\n" +
		"		}\n" +
		"		File file= locate(name);\n" +
		"		if (file == null)\n" +
		"			throw new ClassNotFoundException();\n" +
		"		byte data[]= loadClassData(file);\n" +
		"		c= defineClass(name, data, 0, data.length);\n" +
		"		if (resolve) \n" +
		"			resolveClass(c);\n" +
		"		return c;\n" +
		"	}\n" +
		"	private byte[] loadClassData(File f) throws ClassNotFoundException {\n" +
		"		try {\n" +
		"			//System.out.println(\"loading: \"+f.getPath());\n" +
		"			FileInputStream stream= new FileInputStream(f);\n" +
		"			\n" +
		"			try {\n" +
		"				byte[] b= new byte[stream.available()];\n" +
		"				stream.read(b);\n" +
		"				stream.close();\n" +
		"				return b;\n" +
		"			}\n" +
		"			catch (IOException e) {\n" +
		"				throw new ClassNotFoundException();\n" +
		"			}\n" +
		"		}\n" +
		"		catch (FileNotFoundException e) {\n" +
		"			throw new ClassNotFoundException();\n" +
		"		}\n" +
		"	}\n" +
		"	/**\n" +
		"	 * Locate the given file.\n" +
		"	 * @return Returns null if file couldn\'t be found.\n" +
		"	 */\n" +
		"	private File locate(String fileName) { \n" +
		"		if (fileName != null) {\n" +
		"		  fileName= fileName.replace(\'.\', \'/\')+\".class\";\n" +
		"		  File path= null;\n" +
		"			for (int i= 0; i < fPathItems.length; i++) {\n" +
		"				path= new File(fPathItems[i], fileName);\n" +
		"				if (path.exists())\n" +
		"					return path;\n" +
		"			}\n" +
		"		}\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n" +
		"	\n" +
		"public void loadAndRun(String className) throws Throwable {\n" +
		"	//System.out.println(\"Loading \" + className + \"...\");\n" +
		"	Class testClass = new VerifyClassLoader().loadClass(className);\n" +
		"	//System.out.println(\"Loaded \" + className);\n" +
		"	try {\n" +
		"		Method main = testClass.getMethod(\"main\", new Class[] {String[].class});\n" +
		"		//System.out.println(\"Running \" + className);\n" +
		"		main.invoke(null, new Object[] {new String[] {}});\n" +
		"		//System.out.println(\"Finished running \" + className);\n" +
		"	} catch (NoSuchMethodException e) {\n" +
		"		return;\n" +
		"	} catch (InvocationTargetException e) {\n" +
		"		throw e.getTargetException();\n" +
		"	}\n" +
		"}\n" +
		"public static void main(String[] args) throws IOException {\n" +
		"	VerifyTests verify = new VerifyTests();\n" +
		"	verify.portNumber = Integer.parseInt(args[0]);\n" +
		"	verify.run();\n" +
		"}\n" +
		"public void run() throws IOException {\n" +
		"	ServerSocket server = new ServerSocket(this.portNumber);\n" +
		"	this.socket = server.accept();\n" +
		"	this.socket.setTcpNoDelay(true);\n" +
		"	server.close();\n" +
		"\n" +
		"	DataInputStream in = new DataInputStream(this.socket.getInputStream());\n" +
		"	final DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());\n" +
		"	while (true) {\n" +
		"		final String className = in.readUTF();\n" +
		"		Thread thread = new Thread() {\n" +
		"			public void run() {\n" +
		"				try {\n" +
		"					loadAndRun(className);\n" +
		"					out.writeBoolean(true);\n" +
		"					System.err.println(VerifyTests.class.getName());\n" +
		"					System.out.println(VerifyTests.class.getName());\n" +
		"				} catch (Throwable e) {\n" +
		"					e.printStackTrace();\n" +
		"					try {\n" +
		"						System.err.println(VerifyTests.class.getName());\n" +
		"						System.out.println(VerifyTests.class.getName());\n" +
		"						out.writeBoolean(false);\n" +
		"					} catch (IOException e1) {\n" +
		"						// ignore\n" +
		"					}\n" +
		"				}\n" +
		"			}\n" +
		"		};\n" +
		"		thread.start();\n" +
		"	}\n" +
		"}\n" +
		"}\n";
}
private void launchAndRun(String className, String[] classpaths, String[] programArguments, String[] vmArguments) {
	// we won't reuse the vm, shut the existing one if running
	if (this.vm != null) {
		try {
			this.vm.shutDown();
		} catch (TargetException e) {
		}
	}
	this.classpathCache = null;

	// launch a new one
	LocalVMLauncher launcher = LocalVMLauncher.getLauncher();
	launcher.setClassPath(classpaths);
	launcher.setVMPath(Util.getJREDirectory());
	if (vmArguments != null) {
		String[] completeVmArguments = new String[vmArguments.length + 1];
		System.arraycopy(vmArguments, 0, completeVmArguments, 1, vmArguments.length);
		completeVmArguments[0] = "-verify";
		launcher.setVMArguments(completeVmArguments);
	} else {
		launcher.setVMArguments(new String[] {"-verify"});
	}
	launcher.setProgramClass(className);
	launcher.setProgramArguments(programArguments);
	Thread outputThread;
	Thread errorThread;
	try {
		this.vm = launcher.launch();
		final InputStream input = this.vm.getInputStream();
		outputThread = new Thread(new Runnable() {
			public void run() {
				try {
					int c = input.read();
					while (c != -1) {
						TestVerifier.this.outputBuffer.append((char) c);
						c = input.read();
					}
				} catch(IOException e) {
				}
			}
		});
		final InputStream errorStream = this.vm.getErrorStream();
		errorThread = new Thread(new Runnable() {
			public void run() {
				try {
					int c = errorStream.read();
					while (c != -1) {
						TestVerifier.this.errorBuffer.append((char) c);
						c = errorStream.read();
					}
				} catch(IOException e) {
				}
			}
		});
		outputThread.start();
		errorThread.start();
	} catch(TargetException e) {
		throw new Error(e.getMessage());
	}

	// wait for vm to shut down by itself
	try {
		outputThread.join(10000); // we shut VMs down forcefully downstream,
		errorThread.join(10000);  // hence let's have some slack here
	} catch (InterruptedException e) {
	}
}
private void launchVerifyTestsIfNeeded(String[] classpaths, String[] vmArguments) {
	// determine if we can reuse the vm
	if (this.vm != null && this.vm.isRunning() && this.classpathCache != null) {
		if (classpaths.length == this.classpathCache.length) {
			boolean sameClasspaths = true;
			for (int i = 0; i < classpaths.length; i++) {
				if (!this.classpathCache[i].equals(classpaths[i])) {
					sameClasspaths = false;
					break;
				}
			}
			if (sameClasspaths) {
				return;
			}
		}
	}

	// we could not reuse the vm, shut the existing one if running
	if (this.vm != null) {
		try {
			this.vm.shutDown();
		} catch (TargetException e) {
		}
	}

	this.classpathCache = classpaths;

	// launch a new one
	LocalVMLauncher launcher = LocalVMLauncher.getLauncher();
	int length = classpaths.length;
	String[] cp = new String[length + 1];
	System.arraycopy(classpaths, 0, cp, 0, length);
	String verifierDir = Util.getOutputDirectory() + File.separator + "verifier";
	compileVerifyTests(verifierDir);
	cp[length] = verifierDir;
	launcher.setClassPath(cp);
	launcher.setVMPath(Util.getJREDirectory());
	if (vmArguments != null) {
		String[] completeVmArguments = new String[vmArguments.length + 1];
		System.arraycopy(vmArguments, 0, completeVmArguments, 1, vmArguments.length);
		completeVmArguments[0] = "-verify";
		launcher.setVMArguments(completeVmArguments);
	} else {
		launcher.setVMArguments(new String[] {"-verify"});
	}
	launcher.setProgramClass(VerifyTests.class.getName());
	int portNumber = Util.getFreePort();
	launcher.setProgramArguments(new String[] {Integer.toString(portNumber)});
	try {
		this.vm = launcher.launch();
		final InputStream input = this.vm.getInputStream();
		Thread outputThread = new Thread(new Runnable() {
			public void run() {
				try {
					int c = input.read();
					while (c != -1) {
						TestVerifier.this.outputBuffer.append((char) c);
						c = input.read();
					}
				} catch(IOException ioEx) {
				}
			}
		});
		final InputStream errorStream = this.vm.getErrorStream();
		Thread errorThread = new Thread(new Runnable() {
			public void run() {
				try {
					int c = errorStream.read();
					while (c != -1) {
						TestVerifier.this.errorBuffer.append((char) c);
						c = errorStream.read();
					}
				} catch(IOException ioEx) {
				}
			}
		});
		outputThread.start();
		errorThread.start();
	} catch(TargetException e) {
		throw new Error(e.getMessage());
	}

	// connect to the vm
	this.socket = null;
	boolean isVMRunning = false;
	do {
		try {
			this.socket = new Socket("localhost", portNumber);
			this.socket.setTcpNoDelay(true);
			break;
		} catch (UnknownHostException e) {
		} catch (IOException e) {
		}
		if (this.socket == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			isVMRunning = this.vm.isRunning();
		}
	} while (this.socket == null && isVMRunning);

}
/**
 * Loads and runs the given class.
 * Return whether no exception was thrown while running the class.
 */
private boolean loadAndRun(String className) {
	if (this.socket != null) {
		try {
			DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
			out.writeUTF(className);
			DataInputStream in = new DataInputStream(this.socket.getInputStream());
			try {
				boolean result = in.readBoolean();
				waitForFullBuffers();
				return result;
			} catch (SocketException e) {
				// connection was reset because target program has exited
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	return true;
}
public void shutDown() {
	// Close the socket first so that the OS resource has a chance to be freed.
	if (this.socket != null) {
		try {
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// Wait for the vm to shut down by itself for 2 seconds. If not succesfull, force the shut down.
	if (this.vm != null) {
		try {
			int retry = 0;
			while (this.vm.isRunning() && (++retry < 20)) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			if (this.vm.isRunning()) {
				this.vm.shutDown();
			}
		} catch (TargetException e) {
			e.printStackTrace();
		}
	}
}
/**
 * Verify that the class files created for the given test file can be loaded by
 * a virtual machine.
 */
public boolean verifyClassFiles(String sourceFilePath, String className, String expectedSuccessOutputString, String[] classpaths) {
	return verifyClassFiles(sourceFilePath, className, expectedSuccessOutputString, "", classpaths, null, null);
}
/**
 * Verify that the class files created for the given test file can be loaded by
 * a virtual machine.
 */
public boolean verifyClassFiles(String sourceFilePath, String className, String expectedSuccessOutputString, String[] classpaths, String[] programArguments, String[] vmArguments) {
	return verifyClassFiles(sourceFilePath, className, expectedSuccessOutputString, "", classpaths, programArguments, vmArguments);
}
public boolean verifyClassFiles(String sourceFilePath, String className, String expectedOutputString,
		String expectedErrorStringStart, String[] classpaths, String[] programArguments, String[] vmArguments) {
	this.outputBuffer = new StringBuffer();
	this.errorBuffer = new StringBuffer();
	if (this.reuseVM && programArguments == null) {
		launchVerifyTestsIfNeeded(classpaths, vmArguments);
		loadAndRun(className);
	} else {
		launchAndRun(className, classpaths, programArguments, vmArguments);
	}

	this.failureReason = null;
	return checkBuffers(this.outputBuffer.toString(), this.errorBuffer.toString(), sourceFilePath, expectedOutputString, expectedErrorStringStart);
}

/**
 * Verify that the class files created for the given test file can be loaded and run with an expected error contained
 * in the expectedSuccessOutputString string.
 */
public boolean verifyClassFilesThrowingError(String sourceFilePath, String className, String expectedSuccessOutputString, String[] classpaths, String[] programArguments, String[] vmArguments) {
	this.outputBuffer = new StringBuffer();
	this.errorBuffer = new StringBuffer();
	if (this.reuseVM && programArguments == null) {
		launchVerifyTestsIfNeeded(classpaths, vmArguments);
		loadAndRun(className);
	} else {
		launchAndRun(className, classpaths, programArguments, vmArguments);
	}

	this.failureReason = null;
	return checkBuffersThrowingError(this.errorBuffer.toString(), sourceFilePath, expectedSuccessOutputString);
}

/**
 * Wait until there is nothing more to read from the stdout or sterr.
 */
private void waitForFullBuffers() {
	String endString = VerifyTests.class.getName();
	int count = 50;
	int errorEndStringStart = this.errorBuffer.toString().indexOf(endString);
	int outputEndStringStart = this.outputBuffer.toString().indexOf(endString);
	while (errorEndStringStart == -1 || outputEndStringStart == -1) {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
		if (--count == 0) return;
		errorEndStringStart = this.errorBuffer.toString().indexOf(endString);
		outputEndStringStart = this.outputBuffer.toString().indexOf(endString);
	}
	this.errorBuffer.setLength(errorEndStringStart);
	this.outputBuffer.setLength(outputEndStringStart);
}
}
