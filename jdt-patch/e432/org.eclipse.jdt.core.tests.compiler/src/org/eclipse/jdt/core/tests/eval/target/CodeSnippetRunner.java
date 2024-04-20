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
package org.eclipse.jdt.core.tests.eval.target;

import java.lang.reflect.*;
import java.io.*;
import java.util.*;

/**
 * A code snippet runner loads code snippet classes and global
 * variable classes, and that run the code snippet classes.
 * <p>
 * When started, this runner first connects using TCP/IP to the provided port number.
 * If a regular classpath directory is provided, it writes the class definitions it gets from the IDE
 * to this directory (or to the bootclasspath directory if the class name starts with "java") and it
 * lets the system class loader (or the bootstrap class loader if it is a "java" class) load
 * the class.
 * If the regular classpath directory is null, it uses a code snippet class loader to load the classes
 * it gets from the IDE.
 * <p>
 * IMPORTANT NOTE:
 * Using a code snippet class loader has the following limitation when the code snippet is ran:
 * <ul>
 * <li>The code snippet class can access only public classes, and public members or these classes.
 *	   This is because the "runtime package" of the code snippet class is always different from
 *	   the "runtime package" of the class it is trying to access since the class loaders are
 * 	   different.
 * <li>The code snippet class cannot be defined in a "java.*" package. Only the bootstrap class
 *	   loader can load such a class.
 * </ul>
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CodeSnippetRunner {
	public static CodeSnippetRunner theRunner;
	static final String CODE_SNIPPET_CLASS_NAME = "org.eclipse.jdt.internal.eval.target.CodeSnippet";
	static final String RUN_METHOD_NAME = "run";
	static final String GET_RESULT_TYPE_METHOD_NAME = "getResultType";
	static final String GET_RESULT_VALUE_METHOD_NAME = "getResultValue";

	IDEInterface ide;
	String classPathDirectory;
	String bootclassPathDirectory;
	CodeSnippetClassLoader loader;
	Class codeSnippetClass = null;
/**
 * Creates a new code snippet runner.
 */
public CodeSnippetRunner(int portNumber, String classPathDirectory, String bootclassPathDirectory) {
	this.ide = new IDEInterface(portNumber);
	if (classPathDirectory != null) {
		this.classPathDirectory = classPathDirectory;
		if (bootclassPathDirectory != null) {
			this.bootclassPathDirectory = bootclassPathDirectory;
		}
	} else {
		this.loader = new CodeSnippetClassLoader();
	}
}
/**
 * Returns the forward slash separated class name from the given class definition.
 */
private String className(byte[] classDefinition) {
	// NB: The following code was copied from org.eclipse.jdt.internal.compiler.cfmt,
	//     thus it is highly dependent on the class file format.
	int readOffset = 10;
	try {
		int constantPoolCount = u2At(8, classDefinition);
		int[] constantPoolOffsets = new int[constantPoolCount];
		for (int i = 1; i < constantPoolCount; i++) {
			int tag = u1At(readOffset, classDefinition);
			switch (tag) {
				case 1 : // Utf8Tag
					constantPoolOffsets[i] = readOffset;
					readOffset += u2At(readOffset + 1, classDefinition);
					readOffset += 3; // ConstantUtf8.fixedSize
					break;
				case 3 : // IntegerTag
					constantPoolOffsets[i] = readOffset;
					readOffset += 5; // ConstantInteger.fixedSize
					break;
				case 4 : // FloatTag
					constantPoolOffsets[i] = readOffset;
					readOffset += 5; // ConstantFloat.fixedSize
					break;
				case 5 : // LongTag
					constantPoolOffsets[i] = readOffset;
					readOffset += 9; // ConstantLong.fixedSize
					i++;
					break;
				case 6 : // DoubleTag
					constantPoolOffsets[i] = readOffset;
					readOffset += 9; // ConstantDouble.fixedSize
					i++;
					break;
				case 7 : // ClassTag
					constantPoolOffsets[i] = readOffset;
					readOffset += 3; // ConstantClass.fixedSize
					break;
				case 8 : // StringTag
					constantPoolOffsets[i] = readOffset;
					readOffset += 3; // ConstantString.fixedSize
					break;
				case 9 : // FieldRefTag
					constantPoolOffsets[i] = readOffset;
					readOffset += 5; // ConstantFieldRef.fixedSize
					break;
				case 10 : // MethodRefTag
					constantPoolOffsets[i] = readOffset;
					readOffset += 5; // ConstantMethodRef.fixedSize
					break;
				case 11 : // InterfaceMethodRefTag
					constantPoolOffsets[i] = readOffset;
					readOffset += 5; // ConstantInterfaceMethodRef.fixedSize
					break;
				case 12 : // NameAndTypeTag
					constantPoolOffsets[i] = readOffset;
					readOffset += 5; // ConstantNameAndType.fixedSize
					break;
				case 15 : // MethodHandleTag
					constantPoolOffsets[i] = readOffset;
					readOffset += 4; // MethodHandle.fixedSize
					break;
				case 16 : // MethodTypeTag
					constantPoolOffsets[i] = readOffset;
					readOffset += 3; // MethodType.fixedSize
					break;
				case 17 : // DynamicTag
					constantPoolOffsets[i] = readOffset;
					readOffset += 5; // Dynamic.fixedSize
					break;
				case 18 : // InvokeDynamicTag
					constantPoolOffsets[i] = readOffset;
					readOffset += 5; // InvokeDynamic.fixedSize
					break;
				case 19 : // ModuleTag
					constantPoolOffsets[i] = readOffset;
					readOffset += 3; // Module.fixedSize
					break;
				case 20 : // PackageTag
					constantPoolOffsets[i] = readOffset;
					readOffset += 3; // Package.fixedSize
			}
		}
		// Skip access flags
		readOffset += 2;

		// Read the classname, use exception handlers to catch bad format
		int constantPoolIndex = u2At(readOffset, classDefinition);
		int utf8Offset = constantPoolOffsets[u2At(constantPoolOffsets[constantPoolIndex] + 1, classDefinition)];
		char[] className = utf8At(utf8Offset + 3, u2At(utf8Offset + 1, classDefinition), classDefinition);
		return new String(className);
	} catch (ArrayIndexOutOfBoundsException e) {
		e.printStackTrace();
		return null;
	}
}
/**
 * Creates a new instance of the given class. It is
 * assumed that it is a subclass of CodeSnippet.
 */
Object createCodeSnippet(Class snippetClass) {
	Object object = null;
	try {
		object = snippetClass.getDeclaredConstructor().newInstance();
	} catch (InstantiationException e) {
		e.printStackTrace();
		this.ide.sendResult(void.class, null);
		return null;
	} catch (IllegalAccessException e) {
		e.printStackTrace();
		this.ide.sendResult(void.class, null);
		return null;
	} catch (IllegalArgumentException e) {
		e.printStackTrace();
		this.ide.sendResult(void.class, null);
		return null;
	} catch (InvocationTargetException e) {
		e.printStackTrace();
		this.ide.sendResult(void.class, null);
		return null;
	} catch (NoSuchMethodException e) {
		e.printStackTrace();
		this.ide.sendResult(void.class, null);
		return null;
	} catch (SecurityException e) {
		e.printStackTrace();
		this.ide.sendResult(void.class, null);
		return null;
	}
	return object;
}
/**
 * Whether this code snippet runner is currently running.
 */
public boolean isRunning() {
	return this.ide.isConnected();
}
/**
 * Starts a new CodeSnippetRunner that will serve code snippets from the IDE.
 * It waits for a connection on the given evaluation port number.
 * <p>
 * Usage: {@code java org.eclipse.jdt.tests.eval.target.CodeSnippetRunner -evalport <portNumber> [-options] [<mainClassName>] [<arguments>]}
 * where options include:
 * {@code -cscp <codeSnippetClasspath>} the the classpath directory for the code snippet classes.
 * that are not defined in a "java.*" package.
 * {@code -csbp <codeSnippetBootClasspath>} the bootclasspath directory for the code snippet classes
 * that are defined in a "java.*" package.
 * <p>
 * The mainClassName and its arguments are optional: when not present only the server will start
 * and run until the VM is shut down, when present the server will start, the main class will run
 * but the server will exit when the main class has finished running.
 */
public static void main(String[] args) {
	int length = args.length;
	if (length < 2 || !args[0].toLowerCase().equals("-evalport")) {
		printUsage();
		return;
	}
	int evalPort = Integer.parseInt(args[1]);
	String classPath = null;
	String bootPath = null;
	int mainClass = -1;
	for (int i = 2; i < length; i++) {
		String arg = args[i];
		if (arg.startsWith("-")) {
			if (arg.toLowerCase().equals("-cscp")) {
				if (++i < length) {
					classPath = args[i];
				} else {
					printUsage();
					return;
				}
			} else if (arg.toLowerCase().equals("-csbp")) {
				if (++i < length) {
					bootPath = args[i];
				} else {
					printUsage();
					return;
				}
			}
		} else {
			mainClass = i;
			break;
		}
	}
	theRunner = new CodeSnippetRunner(evalPort, classPath, bootPath);
	if (mainClass == -1) {
		theRunner.start();
	} else {
		Thread server = new Thread() {
			@Override
			public void run() {
				theRunner.start();
			}
		};
		server.setDaemon(true);
		server.start();
		int mainArgsLength = length-mainClass-1;
		String[] mainArgs = new String[mainArgsLength];
		System.arraycopy(args, mainClass+1, mainArgs, 0, mainArgsLength);
		try {
			Class clazz = Class.forName(args[mainClass]);
			Method mainMethod = clazz.getMethod("main", new Class[] {String[].class});
			mainMethod.invoke(null, (Object[]) mainArgs);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}
private static void printUsage() {
	System.out.println("Usage: java org.eclipse.jdt.tests.eval.target.CodeSnippetRunner -evalport <portNumber> [-options] [<mainClassName>] [<arguments>]");
	System.out.println("where options include:");
	System.out.println("-cscp <codeSnippetClasspath> the the classpath directory for the code snippet classes.");
	System.out.println("that are not defined in a \"java.*\" package.");
	System.out.println("-csbp <codeSnippetBootClasspath> the bootclasspath directory for the code snippet classes");
	System.out.println("that are defined in a \"java.*\" package.");
}
/**
 * Loads the given class definitions. The way these class definitions are loaded is described
 * in the CodeSnippetRunner constructor.
 * The class definitions are code snippet classes and/or global variable classes.
 * Code snippet classes are assumed be direct or indirect subclasses of CodeSnippet and implement
 * only the run()V method.
 * They are instanciated and run.
 * Global variable classes are assumed to be direct subclasses of CodeSnippet. Their fields are assumed
 * to be static. The value of each field is sent back to the IDE.
 */
void processClasses(boolean mustRun, byte[][] classDefinitions) {
	// store the class definitions (either in the code snippet class loader or on disk)
	String[] newClasses = new String[classDefinitions.length];
	for (int i = 0; i < classDefinitions.length; i++) {
		byte[] classDefinition = classDefinitions[i];
		String classFileName = className(classDefinition);
		String className = classFileName.replace('/', '.');
		if (this.loader != null) {
			this.loader.storeClassDefinition(className, classDefinition);
		} else {
			writeClassOnDisk(classFileName, classDefinition);
		}
		newClasses[i] = className;
	}

	// load the classes and collect code snippet classes
	List<Class> codeSnippetClasses = new ArrayList<>();
	for (int i = 0; i < newClasses.length; i++) {
		String className = newClasses[i];
		Class clazz = null;
		if (this.loader != null) {
			clazz = this.loader.loadIfNeeded(className);
			if (clazz == null) {
				System.err.println("Could not find class definition for " + className);
				break;
			}
		} else {
			// use the system class loader
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				e.printStackTrace(); // should never happen since we just wrote it on disk
				this.ide.sendResult(void.class, null);
				break;
			}
		}

		Class superclass = clazz.getSuperclass();
		Method[] methods = clazz.getDeclaredMethods();
		if (this.codeSnippetClass == null) {
			if (superclass.equals(Object.class) && clazz.getName().equals(CODE_SNIPPET_CLASS_NAME)) {
				// The CodeSnippet class is being deployed
				this.codeSnippetClass = clazz;
			} else {
				System.out.println("Expecting CodeSnippet class to be deployed first");
			}
		} else if (superclass.equals(this.codeSnippetClass)) {
			// It may be a code snippet class with no global variable
			if (methods.length == 1 && methods[0].getName().equals(RUN_METHOD_NAME)) {
				codeSnippetClasses.add(clazz);
			}
			// Evaluate global variables and send result back
			Field[] fields = clazz.getDeclaredFields();
			for (int j = 0; j < fields.length; j++) {
				Field field = fields[j];
				if (Modifier.isPublic(field.getModifiers())) {
					try {
						this.ide.sendResult(field.getType(), field.get(null));
					} catch (IllegalAccessException e) {
						e.printStackTrace(); // Cannot happen because the field is public
						this.ide.sendResult(void.class, null);
						break;
					}
				}
			}
		} else if (this.codeSnippetClass.equals(superclass.getSuperclass()) && methods.length == 1 && methods[0].getName().equals("run")) {
			// It is a code snippet class with a global variable superclass
			codeSnippetClasses.add(clazz);
		}
	}

	// run the code snippet classes
	if (codeSnippetClasses.size() != 0 && mustRun) {
		for (Class class1 : codeSnippetClasses) {
			Object codeSnippet = createCodeSnippet(class1);
			if (codeSnippet != null) {
				runCodeSnippet(codeSnippet);
			}
		}
	}
}
/**
 * Runs the given code snippet in a new thread and send the result back to the IDE.
 */
void runCodeSnippet(final Object snippet) {
	Thread thread = new Thread() {
		@Override
		public void run() {
			try {
				try {
					Method runMethod = CodeSnippetRunner.this.codeSnippetClass.getMethod(RUN_METHOD_NAME, new Class[] {});
					runMethod.invoke(snippet, new Object[] {});
				} finally {
					Method getResultTypeMethod = CodeSnippetRunner.this.codeSnippetClass.getMethod(GET_RESULT_TYPE_METHOD_NAME, new Class[] {});
					Class resultType = (Class)getResultTypeMethod.invoke(snippet, new Object[] {});
					Method getResultValueMethod = CodeSnippetRunner.this.codeSnippetClass.getMethod(GET_RESULT_VALUE_METHOD_NAME, new Class[] {});
					Object resultValue = getResultValueMethod.invoke(snippet, new Object[] {});
					CodeSnippetRunner.this.ide.sendResult(resultType, resultValue);
				}
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				System.out.println("codeSnippetClass = " + CodeSnippetRunner.this.codeSnippetClass.getName());
				System.out.println("snippet.class = " + snippet.getClass().getName());
				Class superclass = snippet.getClass().getSuperclass();
				System.out.println("snippet.superclass = " + (superclass == null ? "null" : superclass.getName()));
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.getTargetException().printStackTrace();
			}
		}
	};
	thread.setDaemon(true);
	thread.start();
}
/**
 * Starts this code snippet runner in a different thread.
 */
public void start() {
	Thread thread = new Thread("Code snippet runner") {
		@Override
		public void run() {
			try {
				CodeSnippetRunner.this.ide.connect();
			} catch (IOException e) {
				e.printStackTrace();
			}
			while (CodeSnippetRunner.this.ide.isConnected()) {
				try {
					processClasses(CodeSnippetRunner.this.ide.getRunFlag(), CodeSnippetRunner.this.ide.getNextClasses());
				} catch (Error e) {
					CodeSnippetRunner.this.ide.sendResult(void.class, null);
					e.printStackTrace();
				} catch (RuntimeException e) {
					CodeSnippetRunner.this.ide.sendResult(void.class, null);
					e.printStackTrace();
				}
			}
		}
	};
	thread.start();
}
/**
 * Stops this code snippet runner.
 */
public void stop() {
	this.ide.disconnect();
}
private int u1At(int position, byte[] bytes) {
	return bytes[position] & 0xFF;
}
private int u2At(int position, byte[] bytes) {
	return ((bytes[position++] & 0xFF) << 8) + (bytes[position] & 0xFF);
}
private char[] utf8At(int readOffset, int bytesAvailable, byte[] bytes) {
	int x, y, z;
	int length = bytesAvailable;
	char outputBuf[] = new char[bytesAvailable];
	int outputPos = 0;
	while (length != 0) {
		x = bytes[readOffset++] & 0xFF;
		length--;
		if ((0x80 & x) != 0) {
			y = bytes[readOffset++] & 0xFF;
			length--;
			if ((x & 0x20) != 0) {
				z = bytes[readOffset++] & 0xFF;
				length--;
				x = ((x & 0x1F) << 12) + ((y & 0x3F) << 6) + (z & 0x3F);
			} else {
				x = ((x & 0x1F) << 6) + (y & 0x3F);
			}
		}
		outputBuf[outputPos++] = (char) x;
	}

	if (outputPos != bytesAvailable) {
		System.arraycopy(outputBuf, 0, (outputBuf = new char[outputPos]), 0, outputPos);
	}
	return outputBuf;
}
/**
 * Writes the given class definition on disk. The give name is the forward slash separated
 * fully qualified name of the class.
 */
private void writeClassOnDisk(String className, byte[] classDefinition) {
	try {
		String fileName = className.replace('/', File.separatorChar) + ".class";
		File classFile = new File(
			(this.bootclassPathDirectory != null &&
			(className.startsWith("java") || className.replace('/', '.').equals(CODE_SNIPPET_CLASS_NAME))) ?
				this.bootclassPathDirectory :
				this.classPathDirectory, fileName);
		File parent = new File(classFile.getParent());
		parent.mkdirs();
		if (!parent.exists()) {
			throw new IOException("Could not create directory " + parent.getPath());
		}
		try (FileOutputStream out = new FileOutputStream(classFile)) {
			out.write(classDefinition);
		}
	} catch (IOException e) {
		e.printStackTrace();
	}
}
}
