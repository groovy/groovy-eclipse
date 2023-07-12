/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.Test;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.IAttributeNamesConstants;
import org.eclipse.jdt.core.util.IClassFileAttribute;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.ICodeAttribute;
import org.eclipse.jdt.core.util.ILocalVariableTypeTableAttribute;
import org.eclipse.jdt.core.util.ILocalVariableTypeTableEntry;
import org.eclipse.jdt.core.util.IMethodInfo;
import org.eclipse.jdt.core.util.ISignatureAttribute;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "rawtypes" })
public class GenericTypeSignatureTest extends AbstractRegressionTest {
	static class Logger extends Thread {
		StringBuffer buffer;
		InputStream inputStream;
		String type;
		Logger(InputStream inputStream, String type) {
			this.inputStream = inputStream;
			this.type = type;
			this.buffer = new StringBuffer();
		}

		@Override
		public void run() {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(this.inputStream));
				String line = null;
				while ((line = reader.readLine()) != null) {
					this.buffer.append(this.type).append("->").append(line);
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	static final String RUN_SUN_JAVAC = System.getProperty("run.javac");
	static boolean RunJavac = CompilerOptions.ENABLED.equals(RUN_SUN_JAVAC);
	// WORK unify runJavac methods (do we really need a different one here?)

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
//	static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 0 };
//		TESTS_RANGE = new int[] { 21, 50 };
//	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_5);
	}

	public static Class testClass() {
		return GenericTypeSignatureTest.class;
	}

	IPath dirPath = new Path(OUTPUT_DIR); // WORK check whether needed or not

	public GenericTypeSignatureTest(String name) {
		super(name);
	}

	/**
	 */
	protected void cleanUp() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
	}

	protected String[] getFileNames(String[] testFiles) {
		int length = testFiles.length;
		int max = length / 2;
		String[] fileNames = new String[max];
		for (int i=0; i < max; i++) {
			fileNames[i] = testFiles[i*2];
		}
		return fileNames;
	}
	/*
	 * Run Sun compilation using javac.
	 * Use JRE directory to retrieve javac bin directory and current classpath for
	 * compilation.
	 * Launch compilation in a thread and verify that it does not take more than 5s
	 * to perform it. Otherwise abort the process and log in console.
	 */
	// WORK unify with AbstractRegressionTest
	protected void runJavac(final String testName, String[] testFiles) {
		try {
			// Write files in dir
			writeFiles(testFiles);

			final String[] fileNames = getFileNames(testFiles);
			Process process = null;
			try {
				// Compute classpath
				String[] classpath = getDefaultClassPaths();
				StringBuilder cp = new StringBuilder();
				int length = classpath.length;
				for (int i = 0; i < length; i++) {
					if (classpath[i].indexOf(" ") != -1) {
						cp.append("\"" + classpath[i] + "\"");
					} else {
						cp.append(classpath[i]);
					}
					if (i<(length-1)) cp.append(";");
				}
				// Compute command line
				IPath jdkDir = (new Path(Util.getJREDirectory())).removeLastSegments(1);
				IPath javacPath;
				// WORK improve platform management here
				if ("Linux".equals(System.getProperty("os.name"))) {
					javacPath = jdkDir.append("bin").append("javac");
				} else {
					javacPath = jdkDir.append("bin").append("javac.exe");
				}
				StringBuilder cmdLine = new StringBuilder(javacPath.toString());
				cmdLine.append(" -classpath ");
				cmdLine.append(cp);
				cmdLine.append(" -source 1.5 -deprecation -g -Xlint "); // enable recommended warnings
				for (int i = 0, length2 = fileNames.length; i < length2; i++) {
					cmdLine.append(fileNames[i] + " ");
				}
//				System.out.println(testName+": "+cmdLine.toString());
//				System.out.println(GenericTypeTest.this.dirPath.toFile().getAbsolutePath());
				String cmdLineAsString;
				// WORK improve double-quotes management on Linux
				if ("Linux".equals(System.getProperty("os.name"))) {
					cmdLineAsString = cmdLine.toString().replaceAll("\"", "");
				} else {
					cmdLineAsString = cmdLine.toString();
				}
				// Launch process
				process = Runtime.getRuntime().exec(cmdLineAsString, null, GenericTypeSignatureTest.this.dirPath.toFile());
	            // Log errors
	            Logger errorLogger = new Logger(process.getErrorStream(), "ERROR");

	            // Log output
	            Logger outputLogger = new Logger(process.getInputStream(), "OUTPUT");

	            // start the threads to run outputs (standard/error)
	            errorLogger.start();
	            outputLogger.start();

	            // Wait for end of process
				if (process.waitFor() != 0) {
					System.out.println(testName+": javac has found error(s)!");
				}
			} catch (IOException ioe) {
				System.out.println(testName+": Not possible to launch Sun javac compilation!");
			} catch (InterruptedException e1) {
				if (process != null) process.destroy();
				System.out.println(testName+": Sun javac compilation was aborted!");
			}
		} catch (Exception e) {
			// fails silently...
			e.printStackTrace();
		}
	}

	public void test001() {
		final String[] testsSource = new String[] {
				"X.java",
				"public class X <T> extends p.A<T> {\n" +
				"    protected T t;\n" +
				"    X(T t) {\n" +
				"        super(t);\n" +
				"        this.t = t;\n" +
				"    }\n" +
				"    public static void main(String[] args) {\n" +
				"    	X<X<String>> xs = new X<X<String>>(new X<String>(\"SUCCESS\"));\n" +
				"        System.out.print(xs.t.t);\n" +
				"    }\n" +
				"}",
				"p/A.java",
				"package p;\n" +
				"public class A<P> {\n" +
				"    protected P p;\n" +
				"    protected A(P p) {\n" +
				"        this.p = p;\n" +
				"    }\n" +
				"}"
			};
		this.runConformTest(
			testsSource,
			"SUCCESS");

		try {
			ClassFileReader classFileReader = ClassFileReader.read(OUTPUT_DIR + File.separator + "X.class");
			assertEquals("Wrong signature", "<T:Ljava/lang/Object;>Lp/A<TT;>;", new String(classFileReader.getGenericSignature()));
		} catch (ClassFormatException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}

		IClassFileReader classFileReader = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "X.class", IClassFileReader.ALL);
		assertNotNull(classFileReader);
		IClassFileAttribute classFileAttribute = org.eclipse.jdt.internal.core.util.Util.getAttribute(classFileReader, IAttributeNamesConstants.SIGNATURE);
		assertNotNull(classFileAttribute);
		ISignatureAttribute signatureAttribute = (ISignatureAttribute) classFileAttribute;
		char[] signature = signatureAttribute.getSignature();
		assertNotNull("no signature", signature);
		assertEquals("Wrong signature", "<T:Ljava/lang/Object;>Lp/A<TT;>;", new String(signature));
		IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
		int length = methodInfos.length;
		assertEquals("Wrong size", 2, length);
		IMethodInfo mainMethod = null;
		for (int i = 0; i < length; i++) {
			IMethodInfo methodInfo = methodInfos[i];
			if ("main".equals(new String(methodInfo.getName()))) {
				mainMethod = methodInfo;
				break;
			}
		}
		if (mainMethod == null) {
			assertNotNull(mainMethod);
			return;
		}
		ICodeAttribute codeAttribute = mainMethod.getCodeAttribute();
		classFileAttribute = org.eclipse.jdt.internal.core.util.Util.getAttribute(codeAttribute, IAttributeNamesConstants.LOCAL_VARIABLE_TYPE_TABLE);
		assertNotNull(classFileAttribute);
		ILocalVariableTypeTableAttribute localVariableTypeTableAttribute = (ILocalVariableTypeTableAttribute) classFileAttribute;
		ILocalVariableTypeTableEntry[] entries = localVariableTypeTableAttribute.getLocalVariableTypeTable();
		ILocalVariableTypeTableEntry xsEntry = null;
		for (int i = 0, max = entries.length; i < max; i++) {
			ILocalVariableTypeTableEntry entry = entries[i];
			if ("xs".equals(new String(entry.getName()))) {
				xsEntry = entry;
				break;
			}
		}
		if (xsEntry == null) {
			assertNotNull(xsEntry);
			return;
		}
		signature = xsEntry.getSignature();
		assertNotNull("no signature", signature);
		assertEquals("Wrong signature", "LX<LX<Ljava/lang/String;>;>;", new String(signature));

		IMethodInfo constructorMethod = null;
		for (int i = 0; i < length; i++) {
			IMethodInfo methodInfo = methodInfos[i];
			if ("<init>".equals(new String(methodInfo.getName()))) {
				constructorMethod = methodInfo;
				break;
			}
		}
		if (constructorMethod == null) {
			assertNotNull(constructorMethod);
			return;
		}
		codeAttribute = constructorMethod.getCodeAttribute();
		classFileAttribute = org.eclipse.jdt.internal.core.util.Util.getAttribute(codeAttribute, IAttributeNamesConstants.LOCAL_VARIABLE_TYPE_TABLE);
		assertNotNull(classFileAttribute);
		localVariableTypeTableAttribute = (ILocalVariableTypeTableAttribute) classFileAttribute;
		entries = localVariableTypeTableAttribute.getLocalVariableTypeTable();
		ILocalVariableTypeTableEntry thisEntry = null;
		for (int i = 0, max = entries.length; i < max; i++) {
			ILocalVariableTypeTableEntry entry = entries[i];
			if ("this".equals(new String(entry.getName()))) {
				thisEntry = entry;
				break;
			}
		}
		if (thisEntry == null) {
			assertNotNull(thisEntry);
			return;
		}
		signature = thisEntry.getSignature();
		assertNotNull("no signature", signature);
		assertEquals("Wrong signature", "LX<TT;>;", new String(signature));
		ILocalVariableTypeTableEntry tEntry = null;
		for (int i = 0, max = entries.length; i < max; i++) {
			ILocalVariableTypeTableEntry entry = entries[i];
			if ("t".equals(new String(entry.getName()))) {
				tEntry = entry;
				break;
			}
		}
		if (tEntry == null) {
			assertNotNull(tEntry);
			return;
		}
		signature = tEntry.getSignature();
		assertNotNull("no signature", signature);
		assertEquals("Wrong signature", "TT;", new String(signature));

		if (!RunJavac) return;

		// Compare with javac
		cleanUp();
		runJavac("test001", testsSource);

		classFileReader = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "X.class", IClassFileReader.ALL);
		assertNotNull(classFileReader);
		classFileAttribute = org.eclipse.jdt.internal.core.util.Util.getAttribute(classFileReader, IAttributeNamesConstants.SIGNATURE);
		assertNotNull(classFileAttribute);
		signatureAttribute = (ISignatureAttribute) classFileAttribute;
		signature = signatureAttribute.getSignature();
		assertNotNull("no signature", signature);
		assertEquals("Wrong signature", "<T:Ljava/lang/Object;>Lp/A<TT;>;", new String(signature));
		methodInfos = classFileReader.getMethodInfos();
		length = methodInfos.length;
		assertEquals("Wrong size", 2, length);
		mainMethod = null;
		for (int i = 0; i < length; i++) {
			IMethodInfo methodInfo = methodInfos[i];
			if ("main".equals(new String(methodInfo.getName()))) {
				mainMethod = methodInfo;
				break;
			}
		}
		if (mainMethod == null) {
			assertNotNull(mainMethod);
			return;
		}
		codeAttribute = mainMethod.getCodeAttribute();
		classFileAttribute = org.eclipse.jdt.internal.core.util.Util.getAttribute(codeAttribute, IAttributeNamesConstants.LOCAL_VARIABLE_TYPE_TABLE);
		assertNotNull(classFileAttribute);
		localVariableTypeTableAttribute = (ILocalVariableTypeTableAttribute) classFileAttribute;
		entries = localVariableTypeTableAttribute.getLocalVariableTypeTable();
		xsEntry = null;
		for (int i = 0, max = entries.length; i < max; i++) {
			ILocalVariableTypeTableEntry entry = entries[i];
			if ("xs".equals(new String(entry.getName()))) {
				xsEntry = entry;
				break;
			}
		}
		if (xsEntry == null) {
			assertNotNull(xsEntry);
			return;
		}
		signature = xsEntry.getSignature();
		assertNotNull("no signature", signature);
		assertEquals("Wrong signature", "LX<LX<Ljava/lang/String;>;>;", new String(signature));

		constructorMethod = null;
		for (int i = 0; i < length; i++) {
			IMethodInfo methodInfo = methodInfos[i];
			if ("<init>".equals(new String(methodInfo.getName()))) {
				constructorMethod = methodInfo;
				break;
			}
		}
		if (constructorMethod == null) {
			assertNotNull(constructorMethod);
			return;
		}
		codeAttribute = constructorMethod.getCodeAttribute();
		classFileAttribute = org.eclipse.jdt.internal.core.util.Util.getAttribute(codeAttribute, IAttributeNamesConstants.LOCAL_VARIABLE_TYPE_TABLE);
		assertNotNull(classFileAttribute);
		localVariableTypeTableAttribute = (ILocalVariableTypeTableAttribute) classFileAttribute;
		entries = localVariableTypeTableAttribute.getLocalVariableTypeTable();
		thisEntry = null;
		for (int i = 0, max = entries.length; i < max; i++) {
			ILocalVariableTypeTableEntry entry = entries[i];
			if ("this".equals(new String(entry.getName()))) {
				thisEntry = entry;
				break;
			}
		}
		if (thisEntry == null) {
			assertNotNull(thisEntry);
			return;
		}
		signature = thisEntry.getSignature();
		assertNotNull("no signature", signature);
		assertEquals("Wrong signature", "LX<TT;>;", new String(signature));
		tEntry = null;
		for (int i = 0, max = entries.length; i < max; i++) {
			ILocalVariableTypeTableEntry entry = entries[i];
			if ("t".equals(new String(entry.getName()))) {
				tEntry = entry;
				break;
			}
		}
		if (tEntry == null) {
			assertNotNull(tEntry);
			return;
		}
		signature = tEntry.getSignature();
		assertNotNull("no signature", signature);
		assertEquals("Wrong signature", "TT;", new String(signature));
	}

	public void test002() {
		final String[] testsSource = new String[] {
				"X.java",
				"class X extends p.A<String> {\n" +
				"    X() {\n" +
				"        super(null);\n" +
				"    }\n" +
				"}",
				"p/A.java",
				"package p;\n" +
				"public class A<P> {\n" +
				"    protected A(P p) {\n" +
				"    }\n" +
				"}"
			};
		this.runConformTest(testsSource);

		IClassFileReader classFileReader = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "X.class", IClassFileReader.ALL);
		assertNotNull(classFileReader);
		IClassFileAttribute classFileAttribute = org.eclipse.jdt.internal.core.util.Util.getAttribute(classFileReader, IAttributeNamesConstants.SIGNATURE);
		assertNotNull(classFileAttribute);
		ISignatureAttribute signatureAttribute = (ISignatureAttribute) classFileAttribute;
		assertEquals("Wrong signature", "Lp/A<Ljava/lang/String;>;", new String(signatureAttribute.getSignature()));

		classFileReader = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "p/A.class", IClassFileReader.ALL);
		assertNotNull(classFileReader);
		classFileAttribute = org.eclipse.jdt.internal.core.util.Util.getAttribute(classFileReader, IAttributeNamesConstants.SIGNATURE);
		assertNotNull(classFileAttribute);
		signatureAttribute = (ISignatureAttribute) classFileAttribute;
		char[] signature = signatureAttribute.getSignature();
		assertNotNull("No signature", signature);
		assertEquals("Wrong signature", "<P:Ljava/lang/Object;>Ljava/lang/Object;", new String(signature));

		IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
		int length = methodInfos.length;
		assertEquals("Wrong size", 1, length);
		IMethodInfo constructorMethod = methodInfos[0];
		ICodeAttribute codeAttribute = constructorMethod.getCodeAttribute();
		classFileAttribute = org.eclipse.jdt.internal.core.util.Util.getAttribute(codeAttribute, IAttributeNamesConstants.LOCAL_VARIABLE_TYPE_TABLE);
		assertNotNull(classFileAttribute);
		ILocalVariableTypeTableAttribute localVariableTypeTableAttribute = (ILocalVariableTypeTableAttribute) classFileAttribute;
		ILocalVariableTypeTableEntry[] entries = localVariableTypeTableAttribute.getLocalVariableTypeTable();
		ILocalVariableTypeTableEntry thisEntry = null;
		for (int i = 0, max = entries.length; i < max; i++) {
			ILocalVariableTypeTableEntry entry = entries[i];
			if ("this".equals(new String(entry.getName()))) {
				thisEntry = entry;
				break;
			}
		}
		if (thisEntry == null) {
			assertNotNull(thisEntry);
			return;
		}
		assertEquals("Wrong signature", "Lp/A<TP;>;", new String(thisEntry.getSignature()));
		ILocalVariableTypeTableEntry tEntry = null;
		for (int i = 0, max = entries.length; i < max; i++) {
			ILocalVariableTypeTableEntry entry = entries[i];
			if ("p".equals(new String(entry.getName()))) {
				tEntry = entry;
				break;
			}
		}
		if (tEntry == null) {
			assertNotNull(tEntry);
			return;
		}
		signature = tEntry.getSignature();
		assertNotNull("No signature", signature);
		assertEquals("Wrong signature", "TP;", new String(signature));

		if (!RunJavac) return;

		// Compare with javac
		cleanUp();
		runJavac("test002", testsSource);

		classFileReader = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "X.class", IClassFileReader.ALL);
		assertNotNull(classFileReader);
		classFileAttribute = org.eclipse.jdt.internal.core.util.Util.getAttribute(classFileReader, IAttributeNamesConstants.SIGNATURE);
		assertNotNull(classFileAttribute);
		signatureAttribute = (ISignatureAttribute) classFileAttribute;
		assertEquals("Wrong signature", "Lp/A<Ljava/lang/String;>;", new String(signatureAttribute.getSignature()));

		classFileReader = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "p/A.class", IClassFileReader.ALL);
		assertNotNull(classFileReader);
		classFileAttribute = org.eclipse.jdt.internal.core.util.Util.getAttribute(classFileReader, IAttributeNamesConstants.SIGNATURE);
		assertNotNull(classFileAttribute);
		signatureAttribute = (ISignatureAttribute) classFileAttribute;
		signature = signatureAttribute.getSignature();
		assertNotNull("No signature", signature);
		assertEquals("Wrong signature", "<P:Ljava/lang/Object;>Ljava/lang/Object;", new String(signature));

		methodInfos = classFileReader.getMethodInfos();
		length = methodInfos.length;
		assertEquals("Wrong size", 1, length);
		constructorMethod = methodInfos[0];
		codeAttribute = constructorMethod.getCodeAttribute();
		classFileAttribute = org.eclipse.jdt.internal.core.util.Util.getAttribute(codeAttribute, IAttributeNamesConstants.LOCAL_VARIABLE_TYPE_TABLE);
		assertNotNull(classFileAttribute);
		localVariableTypeTableAttribute = (ILocalVariableTypeTableAttribute) classFileAttribute;
		entries = localVariableTypeTableAttribute.getLocalVariableTypeTable();
		thisEntry = null;
		for (int i = 0, max = entries.length; i < max; i++) {
			ILocalVariableTypeTableEntry entry = entries[i];
			if ("this".equals(new String(entry.getName()))) {
				thisEntry = entry;
				break;
			}
		}
		if (thisEntry == null) {
			assertNotNull(thisEntry);
			return;
		}
		signature = thisEntry.getSignature();
		assertNotNull("No signature", signature);
		assertEquals("Wrong signature", "Lp/A<TP;>;", new String(signature));
		tEntry = null;
		for (int i = 0, max = entries.length; i < max; i++) {
			ILocalVariableTypeTableEntry entry = entries[i];
			if ("p".equals(new String(entry.getName()))) {
				tEntry = entry;
				break;
			}
		}
		if (tEntry == null) {
			assertNotNull(tEntry);
			return;
		}
		signature = tEntry.getSignature();
		assertNotNull("No signature", signature);
		assertEquals("Wrong signature", "TP;", new String(signature));
	}

	public void test003() {
		final String[] testsSource = new String[] {
			"X.java",
			"public class X <T extends Object & p.B<? super T>> extends p.A<T> {\n" +
			"    protected T t;\n" +
			"    X(T t) {\n" +
			"        super(t);\n" +
			"        this.t = t;\n" +
			"    }\n" +
			"}",
			"p/A.java",
			"package p;\n" +
			"public class A<P> {\n" +
			"    protected P p;\n" +
			"    protected A(P p) {\n" +
			"        this.p = p;\n" +
			"    }\n" +
			"}",
			"p/B.java",
			"package p;\n" +
			"public interface B<T> {\n" +
			"}"
		};
		this.runConformTest(testsSource);

		IClassFileReader classFileReader = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "X.class", IClassFileReader.ALL);
		assertNotNull(classFileReader);
		IClassFileAttribute classFileAttribute = org.eclipse.jdt.internal.core.util.Util.getAttribute(classFileReader, IAttributeNamesConstants.SIGNATURE);
		assertNotNull(classFileAttribute);
		ISignatureAttribute signatureAttribute = (ISignatureAttribute) classFileAttribute;
		assertEquals("Wrong signature", "<T:Ljava/lang/Object;:Lp/B<-TT;>;>Lp/A<TT;>;", new String(signatureAttribute.getSignature()));

		if (!RunJavac) return;

		// Compare with javac
		cleanUp();
		runJavac("test003", testsSource);

		classFileReader = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "X.class", IClassFileReader.ALL);
		assertNotNull(classFileReader);
		classFileAttribute = org.eclipse.jdt.internal.core.util.Util.getAttribute(classFileReader, IAttributeNamesConstants.SIGNATURE);
		assertNotNull(classFileAttribute);
		signatureAttribute = (ISignatureAttribute) classFileAttribute;
		assertEquals("Wrong signature", "<T:Ljava/lang/Object;:Lp/B<-TT;>;>Lp/A<TT;>;", new String(signatureAttribute.getSignature()));
	}

	public void test004() {
		final String[] testsSource = new String[] {
			"X.java",
			"public class X <T extends Object & p.B> extends p.A<T> {\n" +
			"    protected T t;\n" +
			"    X(T t) {\n" +
			"        super(t);\n" +
			"        this.t = t;\n" +
			"    }\n" +
			"}",
			"p/A.java",
			"package p;\n" +
			"public class A<P> {\n" +
			"    protected P p;\n" +
			"    protected A(P p) {\n" +
			"        this.p = p;\n" +
			"    }\n" +
			"}",
			"p/B.java",
			"package p;\n" +
			"public interface B<T> {\n" +
			"}"
		};
		this.runConformTest(testsSource);

		IClassFileReader classFileReader = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "X.class", IClassFileReader.ALL);
		assertNotNull(classFileReader);
		IClassFileAttribute classFileAttribute = org.eclipse.jdt.internal.core.util.Util.getAttribute(classFileReader, IAttributeNamesConstants.SIGNATURE);
		assertNotNull(classFileAttribute);
		ISignatureAttribute signatureAttribute = (ISignatureAttribute) classFileAttribute;
		assertEquals("Wrong signature", "<T:Ljava/lang/Object;:Lp/B;>Lp/A<TT;>;", new String(signatureAttribute.getSignature()));

		if (!RunJavac) return;

		// Compare with javac
		cleanUp();
		runJavac("test004", testsSource);

		classFileReader = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "X.class", IClassFileReader.ALL);
		assertNotNull(classFileReader);
		classFileAttribute = org.eclipse.jdt.internal.core.util.Util.getAttribute(classFileReader, IAttributeNamesConstants.SIGNATURE);
		assertNotNull(classFileAttribute);
		signatureAttribute = (ISignatureAttribute) classFileAttribute;
		assertEquals("Wrong signature", "<T:Ljava/lang/Object;:Lp/B;>Lp/A<TT;>;", new String(signatureAttribute.getSignature()));
	}

	public void test005() {
		final String[] testsSource = new String[] {
			"X.java",
			"public class X <T extends Object & p.B & p.C> extends p.A<T> {\n" +
			"    protected T t;\n" +
			"    X(T t) {\n" +
			"        super(t);\n" +
			"        this.t = t;\n" +
			"    }\n" +
			"}",
			"p/A.java",
			"package p;\n" +
			"public class A<P> {\n" +
			"    protected P p;\n" +
			"    protected A(P p) {\n" +
			"        this.p = p;\n" +
			"    }\n" +
			"}",
			"p/B.java",
			"package p;\n" +
			"public interface B<T> {\n" +
			"}",
			"p/C.java",
			"package p;\n" +
			"public interface C<T> {\n" +
			"}"
		};
		this.runConformTest(testsSource);

		IClassFileReader classFileReader = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "X.class", IClassFileReader.ALL);
		assertNotNull(classFileReader);
		IClassFileAttribute classFileAttribute = org.eclipse.jdt.internal.core.util.Util.getAttribute(classFileReader, IAttributeNamesConstants.SIGNATURE);
		assertNotNull(classFileAttribute);
		ISignatureAttribute signatureAttribute = (ISignatureAttribute) classFileAttribute;
		char[] signature = signatureAttribute.getSignature();
		assertNotNull("No signature", signature);
		assertEquals("Wrong signature", "<T:Ljava/lang/Object;:Lp/B;:Lp/C;>Lp/A<TT;>;", new String(signature));

		if (!RunJavac) return;

		// Compare with javac
		cleanUp();
		runJavac("test005", testsSource);

		classFileReader = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "X.class", IClassFileReader.ALL);
		assertNotNull(classFileReader);
		classFileAttribute = org.eclipse.jdt.internal.core.util.Util.getAttribute(classFileReader, IAttributeNamesConstants.SIGNATURE);
		assertNotNull(classFileAttribute);
		signatureAttribute = (ISignatureAttribute) classFileAttribute;
		signature = signatureAttribute.getSignature();
		assertNotNull("No signature", signature);
		assertEquals("Wrong signature", "<T:Ljava/lang/Object;:Lp/B;:Lp/C;>Lp/A<TT;>;", new String(signature));
	}

	public void test006() {
		final String[] testsSource = new String[] {
			"X.java",
			"public class X <T> {\n" +
			"    protected T t;\n" +
			"    X(T t) {\n" +
			"        this.t = t;\n" +
			"    }\n" +
			"	T foo(T t1) {\n" +
			"		return t1;\n" +
			"    }\n" +
			"	T field;\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.print(\"SUCCESS\");\n" +
			"    }\n" +
			"}",
		};
		this.runConformTest(
			testsSource,
			"SUCCESS");

		try {
			ClassFileReader classFileReader = ClassFileReader.read(OUTPUT_DIR + File.separator + "X.class");
			assertEquals("Wrong signature", "<T:Ljava/lang/Object;>Ljava/lang/Object;", new String(classFileReader.getGenericSignature()));

			IBinaryField[] fields = classFileReader.getFields();
			assertNotNull("No fields", fields);
			assertEquals("Wrong size", 2, fields.length);
			assertEquals("Wrong name", "field", new String(fields[1].getName()));
			char[] signature = fields[1].getGenericSignature();
			assertNotNull("No signature", signature);
			assertEquals("Wrong signature", "TT;", new String(signature));

			IBinaryMethod[] methods = classFileReader.getMethods();
			assertNotNull("No methods", methods);
			assertEquals("Wrong size", 3, methods.length);
			assertEquals("Wrong name", "foo", new String(methods[1].getSelector()));
			signature = methods[1].getGenericSignature();
			assertNotNull("No signature", signature);
			assertEquals("Wrong signature", "(TT;)TT;", new String(signature));
		} catch (ClassFormatException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}
	}

	public void test007() {
		final String[] testsSource = new String[] {
			"X.java",
			"public class X <T> {\n" +
			"    protected T t;\n" +
			"    X(T t) {\n" +
			"        this.t = t;\n" +
			"    }\n" +
			"	T foo(X<T> x1) {\n" +
			"		return x1.t;\n" +
			"    }\n" +
			"	X<T> field;\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.print(\"SUCCESS\");\n" +
			"    }\n" +
			"}",
		};
		this.runConformTest(
			testsSource,
			"SUCCESS");

		try {
			ClassFileReader classFileReader = ClassFileReader.read(OUTPUT_DIR + File.separator + "X.class");
			assertEquals("Wrong signature", "<T:Ljava/lang/Object;>Ljava/lang/Object;", new String(classFileReader.getGenericSignature()));

			IBinaryField[] fields = classFileReader.getFields();
			assertNotNull("No fields", fields);
			assertEquals("Wrong size", 2, fields.length);
			assertEquals("Wrong name", "field", new String(fields[1].getName()));
			char[] signature = fields[1].getGenericSignature();
			assertNotNull("No signature", signature);
			assertEquals("Wrong signature", "LX<TT;>;", new String(signature));

			IBinaryMethod[] methods = classFileReader.getMethods();
			assertNotNull("No methods", methods);
			assertEquals("Wrong size", 3, methods.length);
			assertEquals("Wrong name", "foo", new String(methods[1].getSelector()));
			signature = methods[1].getGenericSignature();
			assertNotNull("No signature", signature);
			assertEquals("Wrong signature", "(LX<TT;>;)TT;", new String(signature));
		} catch (ClassFormatException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}
	}

	public void test008() {
		final String[] testsSource = new String[] {
			"X.java",
			"public class X  <T> {\n" +
			"	T newInstance() throws IllegalAccessException {\n" +
			"	    return null;\n" +
			"	}\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.print(\"SUCCESS\");\n" +
			"    }\n" +
			"}",
		};
		this.runConformTest(
			testsSource,
			"SUCCESS");

		try {
			ClassFileReader classFileReader = ClassFileReader.read(OUTPUT_DIR + File.separator + "X.class");
			IBinaryMethod[] methods = classFileReader.getMethods();
			assertNotNull("No methods", methods);
			assertEquals("Wrong size", 3, methods.length);
			assertEquals("Wrong name", "newInstance", new String(methods[1].getSelector()));
			char[] signature = methods[1].getGenericSignature();
			assertNotNull("No signature", signature);
			assertEquals("Wrong signature", "()TT;", new String(signature));
		} catch (ClassFormatException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}
	}

	public void test009() {
		final String[] testsSource = new String[] {
			"X.java",
			"public class X<T> {\n" +
			"class MX<U> {\n" +
			"}\n" +
			" \n" +
			"public static void main(String[] args) {\n" +
			"    new X<Thread>().foo(new X<String>().new MX<Thread>());\n" +
			"}\n" +
			"void foo(X<String>.MX<Thread> mx) {\n" +
			"   System.out.println(\"SUCCESS\");\n" +
			"}\n" +
			"}",
		};
		this.runConformTest(
			testsSource,
			"SUCCESS");

		try {
			ClassFileReader classFileReader = ClassFileReader.read(OUTPUT_DIR + File.separator + "X.class");
			IBinaryMethod[] methods = classFileReader.getMethods();
			assertNotNull("No methods", methods);
			assertEquals("Wrong size", 3, methods.length);
			assertEquals("Wrong name", "foo", new String(methods[2].getSelector()));
			char[] signature = methods[2].getGenericSignature();
			assertNotNull("No signature", signature);
			assertEquals("Wrong signature", "(LX<Ljava/lang/String;>.MX<Ljava/lang/Thread;>;)V", new String(signature));
		} catch (ClassFormatException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}
	}

	public void test010() {
		final String[] testsSource = new String[] {
			"X.java",
			"public class X<T> {\n" +
			"class MX<U> {\n" +
			"}\n" +
			" \n" +
			"public static void main(String[] args) {\n" +
			"    new X<Thread>().foo(new X<String>().new MX<Thread>());\n" +
			"}\n" +
			"void foo(X.MX mx) {\n" +
			"   System.out.println(\"SUCCESS\");\n" +
			"}\n" +
			"}",
		};
		this.runConformTest(
			testsSource,
			"SUCCESS");

		try {
			ClassFileReader classFileReader = ClassFileReader.read(OUTPUT_DIR + File.separator + "X.class");
			IBinaryMethod[] methods = classFileReader.getMethods();
			assertNotNull("No methods", methods);
			assertEquals("Wrong size", 3, methods.length);
			assertEquals("Wrong name", "foo", new String(methods[2].getSelector()));
			char[] signature = methods[2].getGenericSignature();
			assertNull("Unexpected generic signature", signature);
		} catch (ClassFormatException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}
	}

	public void test011() {
		final String[] testsSource = new String[] {
			"X.java",
			"public class X<T> {\n" +
			"  class MX<U> {\n" +
			"  }\n" +
			"\n" +
			"  public static void main(String[] args) {\n" +
			"    new X<Thread>().foo(new X<String>().new MX<Thread>());\n" +
			"  }\n" +
			"  void foo(X<String>.MX<?> mx) {\n" +
			"	System.out.println(\"SUCCESS\");\n" +
			"  }\n" +
			"}",
		};
		this.runConformTest(
			testsSource,
			"SUCCESS");

		try {
			ClassFileReader classFileReader = ClassFileReader.read(OUTPUT_DIR + File.separator + "X.class");
			IBinaryMethod[] methods = classFileReader.getMethods();
			assertNotNull("No methods", methods);
			assertEquals("Wrong size", 3, methods.length);
			assertEquals("Wrong name", "foo", new String(methods[2].getSelector()));
			char[] signature = methods[2].getGenericSignature();
			assertNotNull("No signature", signature);
			assertEquals("Wrong signature", "(LX<Ljava/lang/String;>.MX<*>;)V", new String(signature));
		} catch (ClassFormatException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}
	}

	// WORK check whether needed or not
	/*
	 * Write given source test files in current output sub-directory.
	 * Use test name for this sub-directory name (ie. test001, test002, etc...)
	 */
	@Override
	protected void writeFiles(String[] testFiles) {
		// Compute and create specific dir
		IPath dirFilePath = (IPath) this.dirPath.clone();
		File dir = dirFilePath.toFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}

		// For each given test files
		for (int i=0, length=testFiles.length; i<length; i++) {
			dirFilePath = (IPath) this.dirPath.clone();
			String contents = testFiles[i+1];
			String fileName = testFiles[i++];
			IPath filePath = dirFilePath.append(fileName);
			if (fileName.lastIndexOf('/') >= 0) {
				dirFilePath = filePath.removeLastSegments(1);
				dir = dirFilePath.toFile();
				if (!dir.exists()) {
					dir.mkdirs();
				}
			}
			Util.writeToFile(contents, filePath.toString());
		}
	}

	public void test012() {
		final String[] testsSource = new String[] {
			"X.java",
			"public class X<T> {\n" +
			"  class MX<U> {\n" +
			"  }\n" +
			"\n" +
			"  public static void main(String[] args) {\n" +
			"    new X<Thread>().foo(new X<String>().new MX<Thread>());\n" +
			"  }\n" +
			"  void foo(X.MX mx) {			// no signature\n" +
			"	System.out.println(\"SUCCESS\");\n" +
			"  }\n" +
			"}",
		};
		this.runConformTest(
			testsSource,
			"SUCCESS");

		try {
			ClassFileReader classFileReader = ClassFileReader.read(OUTPUT_DIR + File.separator + "X.class");
			IBinaryMethod[] methods = classFileReader.getMethods();
			assertNotNull("No methods", methods);
			assertEquals("Wrong size", 3, methods.length);
			assertEquals("Wrong name", "foo", new String(methods[2].getSelector()));
			assertNull("Wrong signature", methods[2].getGenericSignature());
		} catch (ClassFormatException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}
	}

	public void test013() {
		final String[] testsSource = new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"\n" +
			"public class X<T> {\n" +
			"	\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"	public <U> void foo(ArrayList<U> arr) {\n" +
			"		for (U e : arr) {\n" +
			"			System.out.println(e);\n" +
			"		}\n" +
			"	}\n" +
			"}",
		};
		this.runConformTest(
			testsSource,
			"SUCCESS");

		try {
			ClassFileReader classFileReader = ClassFileReader.read(OUTPUT_DIR + File.separator + "X.class");
			IBinaryMethod[] methods = classFileReader.getMethods();
			assertNotNull("No methods", methods);
			assertEquals("Wrong size", 3, methods.length);
			assertEquals("Wrong name", "foo", new String(methods[2].getSelector()));
			char[] signature = methods[2].getGenericSignature();
			assertNotNull("No signature", signature);
			assertEquals("Wrong signature", "<U:Ljava/lang/Object;>(Ljava/util/ArrayList<TU;>;)V", new String(signature));
		} catch (ClassFormatException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}
	}
	// 59983 - incorrect signature for List<X>
	public void test014() {
		final String[] testsSource = new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"import java.util.List;\n" +
			"public class X {\n" +
			"	private List<X> games = new ArrayList<X>();\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"}",
		};
		this.runConformTest(
			testsSource,
			"SUCCESS");

		try {
			ClassFileReader classFileReader = ClassFileReader.read(OUTPUT_DIR + File.separator + "X.class");
			IBinaryField[] fields = classFileReader.getFields();
			assertNotNull("No fields", fields);
			assertEquals("Wrong size", 1, fields.length);
			assertEquals("Wrong name", "games", new String(fields[0].getName()));
			char[] signature = fields[0].getGenericSignature();
			assertNotNull("No signature", signature);
			assertEquals("Wrong signature", "Ljava/util/List<LX;>;", new String(signature));
		} catch (ClassFormatException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}
	}
	// 65953 - incorrect signature for generic interface
	public void test015() {
		final String[] testsSource = new String[] {
			"X.java",
			"public interface X<T> {\n" +
			"}",
		};
		this.runConformTest(
			testsSource,
			"");

		try {
			ClassFileReader classFileReader = ClassFileReader.read(OUTPUT_DIR + File.separator + "X.class");
			char[] signature = classFileReader.getGenericSignature();
			assertNotNull("No signature", signature);
			assertEquals("Wrong signature", "<T:Ljava/lang/Object;>Ljava/lang/Object;", new String(signature));
		} catch (ClassFormatException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}
	}
	// 70975 - invalid signature for method with array of type variables
	public void test016() {
		final String[] testsSource = new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"\n" +
			"public class X<T> {\n" +
			"	\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"	public <U> void foo(U[] arr) {\n" +
			"	}\n" +
			"}",
		};
		this.runConformTest(
			testsSource,
			"SUCCESS");

		try {
			ClassFileReader classFileReader = ClassFileReader.read(OUTPUT_DIR + File.separator + "X.class");
			IBinaryMethod[] methods = classFileReader.getMethods();
			assertNotNull("No methods", methods);
			assertEquals("Wrong size", 3, methods.length);
			assertEquals("Wrong name", "foo", new String(methods[2].getSelector()));
			char[] signature = methods[2].getGenericSignature();
			assertNotNull("No signature", signature);
			assertEquals("Wrong signature", "<U:Ljava/lang/Object;>([TU;)V", new String(signature));
		} catch (ClassFormatException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}
	}
	public void test017() {
		final String[] testsSource = new String[] {
			"X.java",
			"public class X<T> {\n" +
			"  static class MX<U> {\n" +
			"  }\n" +
			"\n" +
			"  public static void main(String[] args) {\n" +
			"    new X<Thread>().foo(new MX<Thread>());\n" +
			"  }\n" +
			"  void foo(X.MX<?> mx) {\n" +
			"	System.out.println(\"SUCCESS\");\n" +
			"  }\n" +
			"}",
		};
		this.runConformTest(
			testsSource,
			"SUCCESS");

		try {
			ClassFileReader classFileReader = ClassFileReader.read(OUTPUT_DIR + File.separator + "X.class");
			IBinaryMethod[] methods = classFileReader.getMethods();
			assertNotNull("No methods", methods);
			assertEquals("Wrong size", 3, methods.length);
			assertEquals("Wrong name", "foo", new String(methods[2].getSelector()));
			char[] signature = methods[2].getGenericSignature();
			assertNotNull("No signature", signature);
			assertEquals("Wrong signature", "(LX$MX<*>;)V", new String(signature));
		} catch (ClassFormatException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=98322
	public void test018() {
		final String[] testsSource = new String[] {
			"X.java",
			"public class X<K extends X.Key> {\n" +
			"    public abstract static class Key {\n" +
			"         public abstract String getName();\n" +
			"    }\n" +
			"    public class Holder {}\n" +
			"    \n" +
			"    void baz(X<K>.Holder h) {} // (LX<TK;>.Holder;)V\n" +
			"    void bar(X.Holder h) {} // n/a\n" +
			"    void foo(X<Key>.Holder h) {} // (LX<LX$Key;>.Holder;)V\n" +
			"}\n",
		};
		this.runConformTest(
			testsSource,
			"");

		try {
			ClassFileReader classFileReader = ClassFileReader.read(OUTPUT_DIR + File.separator + "X.class");
			IBinaryMethod[] methods = classFileReader.getMethods();
			assertNotNull("No methods", methods);
			assertEquals("Wrong size", 4, methods.length);

			assertEquals("Wrong name", "baz", new String(methods[1].getSelector()));
			char[] signature = methods[1].getGenericSignature();
			assertNotNull("No signature", signature);
			assertEquals("Wrong signature", "(LX<TK;>.Holder;)V", new String(signature));

			assertEquals("Wrong name", "bar", new String(methods[2].getSelector()));
			signature = methods[2].getGenericSignature();
			assertNull("No signature", signature);

			assertEquals("Wrong name", "foo", new String(methods[3].getSelector()));
			signature = methods[3].getGenericSignature();
			assertNotNull("No signature", signature);
			assertEquals("Wrong signature", "(LX<LX$Key;>.Holder;)V", new String(signature));
		} catch (ClassFormatException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=100293
	public void test019() {
		final String[] testsSource = new String[] {
			"X.java",
			"public class X<K extends X.Key> {\n" +
			"    public abstract static class Key {\n" +
			"         public abstract String getName();\n" +
			"    }\n" +
			"    public class Holder {}\n" +
			"    \n" +
			"    X<K>.Holder foo() { return null; }\n" +
			"    \n" +
			"    static void bar() {\n" +
			"    	Object o = new X<Key>().foo();\n" +
			"    	class Local<U> {\n" +
			"    		X<Key>.Holder field;\n" +
			"    		Local<String> foo1() { return null; }\n" +
			"    		Local<U> foo2() { return null; }\n" +
			"    		Local foo3() { return null; }\n" +
			"    	}\n" +
			"    }\n" +
			"}\n",
		};
		this.runConformTest(
			testsSource,
			"");

		try {
			ClassFileReader classFileReader = ClassFileReader.read(OUTPUT_DIR + File.separator + "X$1Local.class");
			IBinaryField[] fields = classFileReader.getFields();
			assertNotNull("No fields", fields);
			assertEquals("Wrong size", 1, fields.length);

			assertEquals("Wrong name", "field", new String(fields[0].getName()));
			char[] signature = fields[0].getGenericSignature();
			assertNotNull("No signature", signature);
			assertEquals("Wrong signature", "LX<LX$Key;>.Holder;", new String(signature));

			IBinaryMethod[] methods = classFileReader.getMethods();
			assertNotNull("No methods", methods);
			assertEquals("Wrong size", 4, methods.length);

			assertEquals("Wrong name", "foo1", new String(methods[1].getSelector()));
			signature = methods[1].getGenericSignature();
			assertNotNull("No signature", signature);
			assertEquals("Wrong signature", "()LX$1Local<Ljava/lang/String;>;", new String(signature));

			assertEquals("Wrong name", "foo2", new String(methods[2].getSelector()));
			signature = methods[2].getGenericSignature();
			assertNotNull("No signature", signature);
			assertEquals("Wrong signature", "()LX$1Local<TU;>;", new String(signature));

			assertEquals("Wrong name", "foo3", new String(methods[3].getSelector()));
			signature = methods[3].getGenericSignature();
			assertNull("No signature", signature);
		} catch (ClassFormatException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=160132 - variation
	public void test020() {
		final String[] testsSource = new String[] {
			"X.java",
			"public interface X<E extends Object & X.Entry> {\n" +
			"  interface Entry {\n" +
			"    interface Internal extends Entry {\n" +
			"      Internal createEntry();\n" +
			"    }\n" +
			"  }\n" +
			"}\n",
		};
		this.runConformTest(
			testsSource,
			"");

		try {
			ClassFileReader classFileReader = ClassFileReader.read(OUTPUT_DIR + File.separator + "X$Entry$Internal.class");
			IBinaryMethod[] methods = classFileReader.getMethods();
			assertNotNull("No methods", methods);
			assertEquals("Wrong size", 1, methods.length);
			assertEquals("Wrong name", "createEntry", new String(methods[0].getSelector()));
			char[] signature = methods[0].getGenericSignature();
			assertNull("Unexpected signature", signature); // no generic signature should have been produced
		} catch (ClassFormatException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}
	}

	public void testBug460491() {
		final String[] testsSource = new String[] {
				"C.java",
				"public class C<E> {\n" +
				"  static class F { }\n" +
				"  interface G { }\n" +
				"  class H { }\n" +
				"  void m1(F f) {}\n" +
				"  void m2(G g) {}\n" +
				"  void m3(H h) {}\n" +
				"}\n"
		};
		this.runConformTest(
				testsSource,
				"");

		try {
			ClassFileReader classFileReader = ClassFileReader.read(OUTPUT_DIR + File.separator + "C.class");
			IBinaryMethod[] methods = classFileReader.getMethods();
			assertNotNull("No methods", methods);
			assertEquals("Wrong size", 4, methods.length); // incl ctor

			IBinaryMethod m1 = methods[1];
			assertEquals("Wrong name", "m1", new String(m1.getSelector()));
			char[] signature = m1.getGenericSignature();
			assertNull("Unexpected signature", signature); // no generic signature should have been produced


			IBinaryMethod m2 = methods[2];
			assertEquals("Wrong name", "m2", new String(m2.getSelector()));
			signature = m2.getGenericSignature();
			assertNull("Unexpected signature", signature); // no generic signature should have been produced

			IBinaryMethod m3 = methods[3];
			assertEquals("Wrong name", "m3", new String(m3.getSelector()));
			signature = m3.getGenericSignature();
			assertEquals("Wrong signature", "(LC<TE;>.H;)V", String.valueOf(signature)); // generic signature *should* have been produced
		} catch (ClassFormatException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}
	}

	public void testGenericVarargsMethodReferenceLambdasHaveNoSignature() {
		// uses lambdas
		if (this.complianceLevel < ClassFileConstants.JDK1_8)
			return;
		final String[] testsSource = new String[] {
				"X.java",
				"import java.util.Optional;\n" +
				"import java.util.stream.Stream;\n" +
				"public interface X {\n" +
				"  static void m1() {\n" +
				"    Optional.<Stream<String>>empty().orElseGet(Stream::of);\n" +
				"  }\n" +
                "\n" +
				"  static <T> Stream<T> m2() {\n" +
				"    return Optional.<Stream<T>>empty().orElseGet(Stream::of);\n" +
				"  }\n" +
				"}\n",
			};
			this.runConformTest(
				testsSource,
				"");

			try {
				ClassFileReader classFileReader = ClassFileReader.read(OUTPUT_DIR + File.separator + "X.class");
				IBinaryMethod[] methods = classFileReader.getMethods();
				assertNotNull("No methods", methods);
				assertEquals("Wrong size", 4, methods.length);

				IBinaryMethod m1 = methods[0];
				assertEquals("Wrong name", "m1", new String(m1.getSelector()));
				assertNull("Unexpected signature, m1 itself is not generic", m1.getGenericSignature());

				IBinaryMethod m2 = methods[1];
				assertEquals("Wrong name", "m2", new String(m2.getSelector()));
				String signature = String.valueOf(m2.getGenericSignature());
				assertEquals("Unexpected signature, m2 itself is generic",
						"<T:Ljava/lang/Object;>()Ljava/util/stream/Stream<TT;>;", signature);

				IBinaryMethod m1Lambda = methods[2];
				assertEquals("Wrong name", "lambda$2", new String(m1Lambda.getSelector()));
				assertNull("Wrong signature - non denotable should have been suppressed", m1Lambda.getGenericSignature());

				IBinaryMethod m2Lambda = methods[3];
				assertEquals("Wrong name", "lambda$3", new String(m2Lambda.getSelector()));
				assertNull("Wrong signature - non denotable should have been suppressed", m2Lambda.getGenericSignature());
			} catch (ClassFormatException e) {
				assertTrue(false);
			} catch (IOException e) {
				assertTrue(false);
			}
		}
}
