/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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
 *     Alexander Kriegisch - bug 286316: Set classpath for forked test JVM via
 *       DataOutputStream instead of JVM parameter; improve file deletion logic
 *******************************************************************************/
package org.eclipse.jdt.core.tests.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.ref.Cleaner;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
import org.eclipse.jdt.core.tests.runtime.LocalVMLauncher;
import org.eclipse.jdt.core.tests.runtime.LocalVirtualMachine;
import org.eclipse.jdt.core.tests.runtime.TargetException;

/**
 * Verifies that the .class files resulting from a compilation can be loaded
 * in a VM and that they can be run.
 */
public class TestVerifier {
	public String failureReason;

	private final boolean reuseVM;
	private String[] classpathCache;
	private StringBuilder outputBuffer;
	private StringBuilder errorBuffer;
	private final VmCleaner managedVMs= new VmCleaner();
	private static final Cleaner cleaner= Cleaner.create();

public TestVerifier(boolean reuseVM) {
	this.reuseVM = reuseVM;
	cleaner.register(this, this.managedVMs);
}
static class ClassPath {
	private String[] classpath;

	ClassPath(String[] classpath){
		this.classpath =classpath;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.classpath);
	}

	@Override
	public boolean equals(Object obj) {
		ClassPath other = (ClassPath) obj;
		return Arrays.equals(this.classpath, other.classpath);
	}

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
	String trimmedErrorString = errorString.trim().replaceFirst("^Exception in thread \"main\" ", "");
	if (expectedErrorStringStart != null) {
		platformIndependantString = Util.convertToIndependantLineDelimiter(trimmedErrorString);
		if (expectedErrorStringStart.isEmpty() && !platformIndependantString.isEmpty() ||
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
	BatchCompiler.compile("\"" + fileName + "\" -d \"" + verifierDir + "\" -warn:-resource -classpath \"" + Util.getJavaClassLibsAsString() + "\"", new PrintWriter(System.out), new PrintWriter(System.err), null/*progress*/);
}
public void execute(String className, String[] classpaths) {
	setOutputBuffer(new StringBuilder());
	setErrorBuffer(new StringBuilder());

	launchAndRun(className, classpaths, null, null);
}
public void execute(String className, String[] classpaths, String[] programArguments, String[] vmArguments) {
	setOutputBuffer(new StringBuilder());
	setErrorBuffer(new StringBuilder());

	launchAndRun(className, classpaths, programArguments, vmArguments);
}

private static class VmCleaner implements Runnable{
	private final Map<ClassPath, LocalVirtualMachine> vmByClassPath = new HashMap<>();
	private final Map<ClassPath, Socket> socketByClassPath = new HashMap<>();

	@Override
	public void run() {
		// Close the socket first so that the OS resource has a chance to be freed.
		for (Socket socket : this.socketByClassPath.values()) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.socketByClassPath.clear();
		// Wait for the vm to shut down by itself for 2 seconds. If not succesfull,
		// force the shut down.
		for (LocalVirtualMachine vm : this.vmByClassPath.values()) {
			try {
				long n0 = System.nanoTime();
				while (vm.isRunning() && (System.nanoTime() - n0 < 2_000_000_000L)) {// 2 sec
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
					}
				}
				if (vm.isRunning()) {
					vm.shutDown();
				}
			} catch (TargetException e) {
				e.printStackTrace();
			}
		}
		this.vmByClassPath.clear();
	}
}

public String getExecutionOutput(){
	StringBuilder ob = getOutputBuffer();
	synchronized (ob) {
		return ob.toString();
	}
}

public String getExecutionError(){
	StringBuilder eb = getErrorBuffer();
	synchronized (eb) {
		return getErrorBuffer().toString();
	}
}

/**
 * Default value for {@link VerifyTests} source code, copied and regularly refreshed from original source code
 * <p>
 * IMPORTANT NOTE: DO NOTE EDIT BUT GENERATE INSTEAD (see below)
 * <p>
 * To generate:<ul>
 *   <li>export VerifyTests.java to d:/temp</li>
 *   <li>inspect org.eclipse.jdt.core.tests.util.Util.fileContentToDisplayString("d:/temp/VerifyTests.java", 2, true)</li>
 * </ul><p>
 */
static final String VERIFY_TEST_CODE_DEFAULT;

static {
	// Use static initialiser block instead of direct field initialisation, because it permits for code folding in IDEs,
	// i.e. this huge string can easily be folded away, which minimises scrolling.
	VERIFY_TEST_CODE_DEFAULT =
"""
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
 * In order to make the copying job easier, keep this class compatible with the lowest supported Java language level (1.8).
 */
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
	try (URLClassLoader urlClassLoader = new URLClassLoader(classPathToURLs(classPath))) {
		//System.out.println("Loading " + className + "...");
		Class<?> testClass = urlClassLoader.loadClass(className);
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
""";
}

/**
 * Activate, if you want to read the {@link VerifyTests} source code directly from the file system in
 * {@link #getVerifyTestsCode()}, e.g. during development while refactoring the source code.
 */
public static boolean READ_VERIFY_TEST_FROM_FILE = false;
/**
 * Adjust, if in {@link #READ_VERIFY_TEST_FROM_FILE} mode method {@link #getVerifyTestsCode()} cannot find
 * the source file based on the current directory. In that case, set the correct JDT Core project base
 * directory as PROJECT_BASE_DIR environment variable, so that the 'org.eclipse.jdt.core.tests.compiler/src'
 * sub-directory can be found from there.
 */
public static String PROJECT_BASE_DIR = System.getenv("PROJECT_BASE_DIR");

// Cached value for VerifyTests.java source code, read only once, either directly from the source code directory or
// from VERIFY_TEST_CODE_DEFAULT
private static String verifyTestCode;

// Helper object for guarding 'verifyTestCode' with 'synchronized (verifyTestCodeLock)', in case tests are to be run in
// parallel
private static final Object verifyTestCodeLock = new Object();

/**
 * Returns {@link VerifyTests} source code, to be used as a boot-strapping class in forked test JVMs
 * <p>
 * Optionally, you can use {@link #READ_VERIFY_TEST_FROM_FILE} in order to read the source code from the project's
 * source directory. If it is not found automatically, you may also adjust {@link #PROJECT_BASE_DIR}. Both values are
 * public and writable during runtime.
 * <p>
 * <b>Caveat:</b> The return value is only lazily initialised once, then cached. If you change
 * {@link #READ_VERIFY_TEST_FROM_FILE} after calling this method for the first time, the return value will not change
 * anymore.
 *
 * @return {@link VerifyTests} source code
 */
String getVerifyTestsCode() {
	synchronized (verifyTestCodeLock) {
		if (verifyTestCode == null) {
			if (READ_VERIFY_TEST_FROM_FILE) {
				String sourceFile = "src/org/eclipse/jdt/core/tests/util/VerifyTests.java";
				if (!new File(sourceFile).exists()) {
					sourceFile = PROJECT_BASE_DIR + "/org.eclipse.jdt.core.tests.compiler/" + sourceFile;
				}
				try {
					verifyTestCode=Files.readString(Path.of(sourceFile));
				}
				catch (IOException e) {
					System.out.println("WARNING: Cannot read & filter VerifyTests source code from file, using default value");
					System.out.println("	- exception: " + e);
				}
			}
		}
		if (verifyTestCode == null) {
			verifyTestCode = VERIFY_TEST_CODE_DEFAULT;
		}
		return verifyTestCode;
	}
}

/**
 * Remove non-essential parts of the test JVM classpath
 * <p>
 * The classpath for the forked test JVM should only contain JDK paths and the 'verifier' subdirectory where the
 * {@link VerifyTests} class boot-strapping the test resides, because those need to be present during JVM start-up.
 * Other parts of the classpath are stripped off, because they are to be communicated to the forked JVM via direct
 * socket communication.
 *
 * @param classPath full classpath
 * @return minimal classpath necessary for forked test JVM boot-strapping
 */
private String[] getMinimalClassPath(String[] classPath) {
return Arrays.stream(classPath)
	.filter(s -> {
		String path = s.replace('\\', '/');
		return !path.contains("/comptest/") || path.endsWith("/verifier");
	})
	.toArray(String[]::new);
}

private String[] getVMArguments(String[] vmArguments) {
	List<String> completeVmArguments = new ArrayList<>();

	Collections.addAll(completeVmArguments, "--add-opens", "java.base/java.io=ALL-UNNAMED");
	Collections.addAll(completeVmArguments, "--add-opens", "java.base/java.net=ALL-UNNAMED");
	Collections.addAll(completeVmArguments, "--add-opens", "java.base/java.lang=ALL-UNNAMED");
	Collections.addAll(completeVmArguments, "--add-opens", "java.base/java.math=ALL-UNNAMED");
	Collections.addAll(completeVmArguments, "--add-opens", "java.base/java.text=ALL-UNNAMED");
	Collections.addAll(completeVmArguments, "--add-opens", "java.base/java.util=ALL-UNNAMED");
	Collections.addAll(completeVmArguments, "--add-opens", "java.base/java.util.regex=ALL-UNNAMED");
	Collections.addAll(completeVmArguments, "--add-opens", "java.base/java.util.stream=ALL-UNNAMED");
	Collections.addAll(completeVmArguments, "--add-opens", "java.base/java.lang.invoke=ALL-UNNAMED");
	Collections.addAll(completeVmArguments, "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED");
	Collections.addAll(completeVmArguments, "--add-opens", "java.base/java.util.function=ALL-UNNAMED");

	if (vmArguments != null) {
		Collections.addAll(completeVmArguments, vmArguments);
	}
	completeVmArguments.add("-verify");

	return completeVmArguments.toArray(new String[0]);
}

private void launchAndRun(String className, String[] classpaths, String[] programArguments, String[] vmArguments) {
	// we won't reuse the vm, shut the existing one if running
	for (LocalVirtualMachine vm : this.managedVMs.vmByClassPath.values()) {
		try {
			vm.shutDown();
		} catch (TargetException e) {
		}
	}
	this.managedVMs.vmByClassPath.clear();
	this.classpathCache = null;

	// launch a new one
	LocalVMLauncher launcher = LocalVMLauncher.getLauncher();
	launcher.setClassPath(classpaths);
	launcher.setVMPath(Util.getJREDirectory());
	launcher.setVMArguments(getVMArguments(vmArguments));
	launcher.setProgramClass(className);
	launcher.setProgramArguments(programArguments);
	Thread outputThread;
	Thread errorThread;
	try {
		LocalVirtualMachine vm  = launcher.launch();
		this.managedVMs.vmByClassPath.put(new ClassPath(classpaths), vm);
		InputStream input = vm.getInputStream();
		outputThread = new Thread(() -> transferTo(input, this::getOutputBuffer), "stdOutReader");
		InputStream errorStream = vm.getErrorStream();
		errorThread = new Thread(() -> transferTo(errorStream, this::getErrorBuffer), "stdErrReader");
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

private static void transferTo(InputStream stream, Supplier<StringBuilder> b) {
	try {
		int c = stream.read();
		while (c != -1) {
			StringBuilder buffer = b.get();
			synchronized (buffer) {
				buffer.append((char) c);
			}
			c = stream.read();
		}
	} catch (IOException ioEx) {
		ioEx.printStackTrace();
	} finally {
		try {
			stream.close();
		} catch (IOException e) {
		}
	}
}

private void launchVerifyTestsIfNeeded(String[] classpaths, String[] vmArguments) {
	// determine if we can reuse the vm
	LocalVirtualMachine vm = this.managedVMs.vmByClassPath.get(new ClassPath(classpaths));
	if (vm != null && vm.isRunning() && this.classpathCache != null) {
		return;
	}

	// we could not reuse the vm, shut the existing one if running
	if (vm != null) {
		try {
			vm.shutDown();
		} catch (TargetException e) {
		}
		vm = null;
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
	launcher.setClassPath(getMinimalClassPath(cp));
	launcher.setVMPath(Util.getJREDirectory());
	launcher.setVMArguments(getVMArguments(vmArguments));
	launcher.setProgramClass(VerifyTests.class.getName());
	try (ServerSocket server = new ServerSocket(0)) {
		int portNumber = server.getLocalPort();

		launcher.setProgramArguments(new String[] {Integer.toString(portNumber)});
		try {
			vm = launcher.launch();
			this.managedVMs.vmByClassPath.put(new ClassPath(classpaths), vm);
			InputStream input = vm.getInputStream();
			Thread outputThread = new Thread(() -> transferTo(input, this::getOutputBuffer), "stdOutReader");
			InputStream errorStream = vm.getErrorStream();
			Thread errorThread = new Thread(() -> transferTo(errorStream, this::getErrorBuffer), "stdErrReader");
			outputThread.start();
			errorThread.start();
		} catch(TargetException e) {
			e.printStackTrace();
			throw new Error(e.getMessage());
		}

		// connect to the vm
		boolean isVMRunning = false;
		Socket socket = null;
		do {
			try {
				socket=server.accept();
				this.managedVMs.socketByClassPath.put(new ClassPath(classpaths), socket);
				socket.setTcpNoDelay(true);
				break;
			} catch (UnknownHostException e) {
			} catch (IOException e) {
			}
			if (socket == null) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}
				isVMRunning = vm.isRunning();
			}
		} while (socket == null && isVMRunning);
	} catch (IOException e) {
		e.printStackTrace();
		throw new Error(e.getMessage());
	}
}
/**
 * Loads and runs the given class.
 * Return whether no exception was thrown while running the class.
 */
private boolean loadAndRun(String className, String[] classPath) {
	Socket socket = this.managedVMs.socketByClassPath.get(new ClassPath(classPath));
	if (socket != null) {
		try {
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeUTF(className);
				if (classPath == null)
					classPath = new String[0];
				out.writeInt(classPath.length);
				for (String classpath : classPath) {
					out.writeUTF(classpath);
				}
			DataInputStream in = new DataInputStream(socket.getInputStream());
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
	this.managedVMs.run();
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
	setOutputBuffer(new StringBuilder());
	setErrorBuffer(new StringBuilder());
	if (this.reuseVM && programArguments == null) {
		launchVerifyTestsIfNeeded(classpaths, vmArguments);
		loadAndRun(className, classpaths);
	} else {
		launchAndRun(className, classpaths, programArguments, vmArguments);
	}

	this.failureReason = null;
	return checkBuffers(getExecutionOutput(), getExecutionError(), sourceFilePath, expectedOutputString, expectedErrorStringStart);
}

/**
 * Wait until there is nothing more to read from the stdout or sterr.
 */
private void waitForFullBuffers() {
	String endString = VerifyTests.class.getName();
	long n0 = System.nanoTime();
	int errorEndStringStart;
	int outputEndStringStart;
	do {
		errorEndStringStart = getExecutionError().indexOf(endString);
		outputEndStringStart = getExecutionOutput().indexOf(endString);
		if (errorEndStringStart != -1 && outputEndStringStart != -1) {
			break;
		}
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
		}
		if (System.nanoTime() - n0 > 5_000_000_000L) {// 5 sec
			throw new RuntimeException(
					"Timeout after " + (System.nanoTime() - n0) / 1_000_000L + "ms");
		}
	} while (true);
	StringBuilder eb = getErrorBuffer();
	synchronized (eb) {
		eb.setLength(errorEndStringStart);
	}
	StringBuilder ob = getOutputBuffer();
	synchronized (ob) {
		ob.setLength(outputEndStringStart);
	}
}
public StringBuilder getOutputBuffer() {
	return this.outputBuffer;
}
public void setOutputBuffer(StringBuilder outputBuffer) {
	this.outputBuffer = outputBuffer;
}
public StringBuilder getErrorBuffer() {
	return this.errorBuffer;
}
public void setErrorBuffer(StringBuilder errorBuffer) {
	this.errorBuffer = errorBuffer;
}
}
