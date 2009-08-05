/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.Test;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.batch.ClasspathJar;
import org.eclipse.jdt.internal.compiler.batch.ClasspathLocation;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.util.ManifestAnalyzer;

public class BatchCompilerTest extends AbstractRegressionTest {
	public static final String OUTPUT_DIR_PLACEHOLDER = "---OUTPUT_DIR_PLACEHOLDER---";
	public static final String LIB_DIR_PLACEHOLDER = "---LIB_DIR_PLACEHOLDER---";
	static final String JRE_HOME_DIR = Util.getJREDirectory();
	private static final Main MAIN = new Main(null/*outWriter*/, null/*errWriter*/, false/*systemExit*/, null/*options*/, null/*progress*/);

	static {
//	TESTS_NAMES = new String[] { "test000" };
//	TESTS_NUMBERS = new int[] { 152 };
//	TESTS_RANGE = new int[] { 107, -1 };
	}
public BatchCompilerTest(String name) {
	super(name);
}
/**
 * This test suite only needs to be run on one compliance.
 * As it includes some specific 1.5 tests, it must be used with a least a 1.5 VM
 * and not be duplicated in general test suite.
 * @see TestAll
 */
public static Test suite() {
	return buildUniqueComplianceTestSuite(testClass(), ClassFileConstants.JDK1_5);
}
public static Class testClass() {
	return BatchCompilerTest.class;
}
private static boolean CASCADED_JARS_CREATED;
private void createCascadedJars() {
	if (!CASCADED_JARS_CREATED) {
		File libDir = new File(LIB_DIR);
		Util.delete(libDir); // make sure we recycle the libs
 		libDir.mkdirs();
		try {
			Util.createJar(
				new String[] {
					"p/A.java",
					"package p;\n" +
					"public class A {\n" +
					"}",
				},
				new String[] {
					"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n" +
					"Created-By: Eclipse JDT Test Harness\n" +
					"Class-Path: lib2.jar\n",
					"p/S1.java",
					"package p;\n" +
					"public class S1 {\n" +
					"}",
				},
				LIB_DIR + "/lib1.jar",
				JavaCore.VERSION_1_4);
			Util.createJar(
				new String[] {
					"p/B.java",
					"package p;\n" +
					"public class B {\n" +
					"}",
					"p/R.java",
					"package p;\n" +
					"public class R {\n" +
					"  public static final int R2 = 2;\n" +
					"}",
				},
				new String[] {
					"p/S2.java",
					"package p;\n" +
					"public class S2 {\n" +
					"}",
				},
				LIB_DIR + "/lib2.jar",
				JavaCore.VERSION_1_4);
			Util.createJar(
				new String[] {
					"p/C.java",
					"package p;\n" +
					"public class C {\n" +
					"}",
					"p/R.java",
					"package p;\n" +
					"public class R {\n" +
					"  public static final int R3 = 3;\n" +
					"}",
				},
				new String[] {
					"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n" +
					"Created-By: Eclipse JDT Test Harness\n" +
					"Class-Path: lib4.jar\n",
				},
				LIB_DIR + "/lib3.jar",
				JavaCore.VERSION_1_4);
			Util.createJar(
				new String[] {
					"p/D.java",
					"package p;\n" +
					"public class D {\n" +
					"}",
				},
				new String[] {
					"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n" +
					"Created-By: Eclipse JDT Test Harness\n" +
					"Class-Path: lib1.jar lib3.jar\n",
				},
				LIB_DIR + "/lib4.jar",
				JavaCore.VERSION_1_4);
			Util.createJar(
				new String[] {
					"p/C.java",
					"package p;\n" +
					"public class C {\n" +
					"}",
					"p/R.java",
					"package p;\n" +
					"public class R {\n" +
					"  public static final int R3 = 3;\n" +
					"}",
				},
				new String[] {
					"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n" +
					"Created-By: Eclipse JDT Test Harness\n" +
					"Class-Path: s/lib6.jar\n",
				},
				LIB_DIR + "/lib5.jar",
				JavaCore.VERSION_1_4);
			new File(LIB_DIR + "/s").mkdir();
			Util.createJar(
				new String[] {
					"p/D.java",
					"package p;\n" +
					"public class D {\n" +
					"}",
				},
				new String[] {
					"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n" +
					"Created-By: Eclipse JDT Test Harness\n" +
					"Class-Path: ../lib7.jar\n",
				},
				LIB_DIR + "/s/lib6.jar",
				JavaCore.VERSION_1_4);
			Util.createJar(
				new String[] {
					"p/A.java",
					"package p;\n" +
					"public class A {\n" +
					"}",
				},
				new String[] {
					"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n" +
					"Created-By: Eclipse JDT Test Harness\n" +
					"Class-Path: lib2.jar\n",
				},
				LIB_DIR + "/lib7.jar",
				JavaCore.VERSION_1_4);
			Util.createJar(
				new String[] {
					"p/F.java",
					"package p;\n" +
					"public class F {\n" +
					"}",
				},
				new String[] {
					"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n" +
					"Created-By: Eclipse JDT Test Harness\n" +
					"Class-Path: " + LIB_DIR + "/lib3.jar lib1.jar\n",
				},
				LIB_DIR + "/lib8.jar",
				JavaCore.VERSION_1_4);
			Util.createJar(
				new String[] {
					"p/G.java",
					"package p;\n" +
					"public class G {\n" +
					"}",
				},
				new String[] {
					"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n" +
					"Created-By: Eclipse JDT Test Harness\n" +
					"Class-Path: lib1.jar\n" +
					"Class-Path: lib3.jar\n",
				},
				LIB_DIR + "/lib9.jar",
				JavaCore.VERSION_1_4);
			Util.createJar(
				new String[] {
					"p/A.java",
					"package p;\n" +
					"public class A {\n" +
					"}",
				},
				// spoiled jar: MANIFEST.MF is a directory
				new String[] {
					"META-INF/MANIFEST.MF/MANIFEST.MF",
					"Manifest-Version: 1.0\n" +
					"Created-By: Eclipse JDT Test Harness\n" +
					"Class-Path: lib2.jar\n",
				},
				LIB_DIR + "/lib10.jar",
				JavaCore.VERSION_1_4);
			Util.createJar(
				new String[] {
					"p/A.java",
					"package p;\n" +
					"public class A {\n" +
					"}",
				},
				new String[] {
					"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n" +
					"Created-By: Eclipse JDT Test Harness\n" +
					"Class-Path:\n",
				},
				LIB_DIR + "/lib11.jar",
				JavaCore.VERSION_1_4);
			Util.createJar(
				null,
				new String[] {
					"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n" +
					"Created-By: Eclipse JDT Test Harness\n" +
					"Class-Path:lib1.jar\n", // missing space
				},
				LIB_DIR + "/lib12.jar",
				JavaCore.VERSION_1_4);
			Util.createJar(
				null,
				new String[] {
					"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n" +
					"Created-By: Eclipse JDT Test Harness\n" +
					"Class-Path:lib1.jar lib1.jar\n", // missing space
				},
				LIB_DIR + "/lib13.jar",
				JavaCore.VERSION_1_4);			
			Util.createJar(
				null,
				new String[] {
					"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n" +
					"Created-By: Eclipse JDT Test Harness\n" +
					" Class-Path: lib1.jar\n", // extra space at line start
				},
				LIB_DIR + "/lib14.jar",
				JavaCore.VERSION_1_4);			
			Util.createJar(
				null,
				new String[] {
					"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n" +
					"Created-By: Eclipse JDT Test Harness\n" +
					"Class-Path: lib1.jar", // missing newline at end
				},
				LIB_DIR + "/lib15.jar",
				JavaCore.VERSION_1_4);			
			Util.createJar(
				new String[] {
					"p/A.java",
					"package p;\n" +
					"public class A {\n" +
					"}",
				},
				new String[] {
					"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n" +
					"Created-By: Eclipse JDT Test Harness\n" +
					"Class-Path: \n" +
					" lib2.jar\n",
					"p/S1.java",
					"package p;\n" +
					"public class S1 {\n" +
					"}",
				},
				LIB_DIR + "/lib16.jar",
				JavaCore.VERSION_1_4);
			new File(LIB_DIR + "/dir").mkdir();
			Util.createJar(
				new String[] {
					"p/A.java",
					"package p;\n" +
					"public class A {\n" +
					"}",
				},
				new String[] {
					"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n" +
					"Created-By: Eclipse JDT Test Harness\n" +
					"Class-Path: ../lib2.jar\n",
				},
				LIB_DIR + "/dir/lib17.jar",
				JavaCore.VERSION_1_4);
			CASCADED_JARS_CREATED = true;
		} catch (IOException e) {
			// ignore
		}
	}
}

private String getLibraryClassesAsQuotedString() {
	String[] paths = Util.getJavaClassLibs();
	StringBuffer buffer = new StringBuffer();
	buffer.append('"');
	for (int i = 0, max = paths.length; i < max; i++) {
		if (i != 0) {
			buffer.append(File.pathSeparatorChar);
		}
		buffer.append(paths[i]);
	}
	buffer.append('"');
	return String.valueOf(buffer);
}


	private String getJCEJarAsQuotedString() {
		if (Util.isMacOS()) {
			return "\"" + JRE_HOME_DIR + "/../Classes/jce.jar\"";
		}
		return "\"" + JRE_HOME_DIR + "/lib/jce.jar\"";
	}

	private String getExtDirectory() {
		return JRE_HOME_DIR + "/lib/ext";
	}

	/**
	 * Run a compilation test that is expected to complete successfully and
	 * compare the outputs to expected ones.
	 *
	 * @param testFiles
	 *            the source files, given as a suite of file name, file content;
	 *            file names are relative to the output directory
	 * @param commandLine
	 *            the command line to pass to
	 *            {@link BatchCompiler#compile(String, PrintWriter, PrintWriter, org.eclipse.jdt.core.compiler.CompilationProgress) BatchCompiler#compile}
	 * @param expectedSuccessOutOutputString
	 *            the expected contents of the standard output stream; pass null
	 *            to bypass the comparison
	 * @param expectedSuccessErrOutputString
	 *            the expected contents of the standard error output stream;
	 *            pass null to bypass the comparison
	 * @param shouldFlushOutputDirectory
	 *            pass true to get the output directory flushed before the test
	 *            runs
	 */
	protected void runConformTest(String[] testFiles, String commandLine,
			String expectedSuccessOutOutputString,
			String expectedSuccessErrOutputString,
			boolean shouldFlushOutputDirectory) {
		runTest(true, testFiles, commandLine, expectedSuccessOutOutputString,
				expectedSuccessErrOutputString, shouldFlushOutputDirectory, null/*progress*/);
	}

	/**
	 * Run a compilation test that is expected to fail and compare the outputs
	 * to expected ones.
	 *
	 * @param testFiles
	 *            the source files, given as a suite of file name, file content;
	 *            file names are relative to the output directory
	 * @param commandLine
	 *            the command line to pass to
	 *            {@link BatchCompiler#compile(String, PrintWriter, PrintWriter, org.eclipse.jdt.core.compiler.CompilationProgress) BatchCompiler#compile}
	 * @param expectedFailureOutOutputString
	 *            the expected contents of the standard output stream; pass null
	 *            to bypass the comparison
	 * @param expectedFailureErrOutputString
	 *            the expected contents of the standard error output stream;
	 *            pass null to bypass the comparison
	 * @param shouldFlushOutputDirectory
	 *            pass true to get the output directory flushed before the test
	 *            runs
	 */
	protected void runNegativeTest(String[] testFiles, String commandLine,
			String expectedFailureOutOutputString,
			String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory) {
		runTest(false, testFiles, commandLine, expectedFailureOutOutputString,
				expectedFailureErrOutputString, shouldFlushOutputDirectory, null/*progress*/);
	}

	private void runProgressTest(
			String[] testFiles, 
			String commandLine,
			String expectedOutOutputString,
			String expectedErrOutputString,
			String expectedProgress) {
		runTest(true/*shouldCompileOK*/, testFiles, commandLine, expectedOutOutputString, expectedErrOutputString, true/*shouldFlushOutputDirectory*/, new TestCompilationProgress());
	}
	
	private void runProgressTest(
			boolean shouldCompileOK,
			String[] testFiles, 
			String commandLine,
			String expectedOutOutputString,
			String expectedErrOutputString,
			TestCompilationProgress progress,
			String expectedProgress) {
		runTest(shouldCompileOK, testFiles, commandLine, expectedOutOutputString, expectedErrOutputString, true/*shouldFlushOutputDirectory*/, progress);
		String actualProgress = progress.toString();
		if (!semiNormalizedComparison(expectedProgress, actualProgress, outputDirNormalizer)) {
			System.out.println(Util.displayString(outputDirNormalizer.normalized(actualProgress), 2));
			assertEquals(
				"Unexpected progress",
				expectedProgress,
				actualProgress);
		}
	}	
	/**
	 * Worker method for runConformTest and runNegativeTest.
	 *
	 * @param shouldCompileOK
	 *            set to true if the compiler should compile the given sources
	 *            without errors
	 * @param testFiles
	 *            the source files, given as a suite of file name, file content;
	 *            file names are relative to the output directory
	 * @param extraArguments
	 *            the command line to pass to {@link Main#compile(String[]) 
	 *            Main#compile} or other arguments to pass to {@link 
	 *            #invokeCompiler(PrintWriter, PrintWriter, Object, 
	 *            BatchCompilerTest.TestCompilationProgress)} (for use 
	 *            by extending test classes)
	 * @param expectedOutOutputString
	 *            the expected contents of the standard output stream; pass null
	 *            to bypass the comparison
	 * @param expectedErrOutputString
	 *            the expected contents of the standard error output stream;
	 *            pass null to bypass the comparison
	 * @param shouldFlushOutputDirectory
	 *            pass true to get the output directory flushed before the test
	 *            runs
	 */
	protected void runTest(
			boolean shouldCompileOK, 
			String[] testFiles, 
			Object extraArguments,
			String expectedOutOutputString,
			String expectedErrOutputString,
			boolean shouldFlushOutputDirectory,
			TestCompilationProgress progress) {
		File outputDirectory = new File(OUTPUT_DIR);
		if (shouldFlushOutputDirectory)
			Util.flushDirectoryContent(outputDirectory);
		try {
			if (!outputDirectory.isDirectory()) {
				outputDirectory.mkdirs();
			}
			if (testFiles != null) {
				PrintWriter sourceFileWriter;
				for (int i = 0; i < testFiles.length; i += 2) {
					String fileName = OUTPUT_DIR + File.separator + testFiles[i];
					File file = new File(fileName), innerOutputDirectory = file
							.getParentFile();
					if (!innerOutputDirectory.isDirectory()) {
						innerOutputDirectory.mkdirs();
					}
					sourceFileWriter = new PrintWriter(new FileOutputStream(file));
					try {
						sourceFileWriter.write(testFiles[i + 1]);
					} finally {
						sourceFileWriter.close();
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		String printerWritersNameRoot = OUTPUT_DIR + File.separator + testName();
		String outFileName = printerWritersNameRoot + "out.txt",
			   errFileName = printerWritersNameRoot + "err.txt";
		PrintWriter out = null;
		PrintWriter err = null;
		boolean compileOK;
		try {
			try {
				out = new PrintWriter(new FileOutputStream(outFileName));
				err = new PrintWriter(new FileOutputStream(errFileName));
			} catch (FileNotFoundException e) {
				System.out.println(getClass().getName() + '#' + getName());
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			compileOK = invokeCompiler(out, err, extraArguments, progress);
		} finally {
			if (out != null)
				out.close();
			if (err != null)
				err.close();
		}
		String outOutputString = Util.fileContent(outFileName),
		       errOutputString = Util.fileContent(errFileName);
		boolean compareOK = false, outCompareOK = false, errCompareOK = false;
		if (compileOK == shouldCompileOK) {
			compareOK =
				(outCompareOK = semiNormalizedComparison(expectedOutOutputString,
					outOutputString, outputDirNormalizer))
				&& (errCompareOK = semiNormalizedComparison(expectedErrOutputString,
						errOutputString, outputDirNormalizer));
		}
		if (compileOK != shouldCompileOK || !compareOK) {
			System.out.println(getClass().getName() + '#' + getName());
			if (testFiles != null) {
				for (int i = 0; i < testFiles.length; i += 2) {
					System.out.print(testFiles[i]);
					System.out.println(" [");
					System.out.println(testFiles[i + 1]);
					System.out.println("]");
				}
			}
		}
		if (compileOK != shouldCompileOK)
			System.out.println(errOutputString);
		if (compileOK == shouldCompileOK && !compareOK) {
			System.out.println(
					    "------------ [START OUT] ------------\n"
					+   "------------- Expected: -------------\n"
					+ expectedOutOutputString
					+ "\n------------- but was:  -------------\n"
					+ outOutputString
					+ "\n--------- (cut and paste:) ----------\n"
					+ Util.displayString(outputDirNormalizer
							.normalized(outOutputString))
					+ "\n------------- [END OUT] -------------\n"
					+   "------------ [START ERR] ------------\n"
					+   "------------- Expected: -------------\n"
					+ expectedErrOutputString
					+ "\n------------- but was:  -------------\n"
					+ errOutputString
					+ "\n--------- (cut and paste:) ----------\n"
					+ Util.displayString(outputDirNormalizer
							.normalized(errOutputString))
					+ "\n------------- [END ERR] -------------\n");
		}
		if (shouldCompileOK)
			assertTrue("Unexpected problems [out: " + outOutputString + "][err: " + errOutputString + "]", compileOK);
		else
			assertFalse("Unexpected success: [out: " + outOutputString + "][err: " + errOutputString + "]", compileOK);
		if (!outCompareOK) {
			// calling assertEquals to benefit from the comparison UI
			// (need appropriate exception)
			assertEquals(
					"Unexpected standard output for invocation with arguments ["
						+ extraArguments + "]",
					expectedOutOutputString,
					outOutputString);
		}
		if (!errCompareOK) {
			assertEquals(
					"Unexpected error output for invocation with arguments ["
						+ extraArguments + "]",
					expectedErrOutputString,
					errOutputString);
		}
	}
// in this case, extraArguments is expected to hold a command line (as a String)
protected boolean invokeCompiler(PrintWriter out, PrintWriter err, Object extraArguments, TestCompilationProgress compilationProgress) {
	try {
		final String[] tokenizedCommandLine = Main.tokenize((String) extraArguments);
		return new Main(out, err, false, null /* customDefaultOptions */, compilationProgress /* compilationProgress*/).compile(tokenizedCommandLine);
	} catch (RuntimeException e) {
		System.out.println(getClass().getName() + '#' + getName());
		e.printStackTrace();
		throw e;
	}
}
private void runTest(
		boolean shouldCompileOK, 
		String[] testFiles, 
		String commandLine,
		Matcher outOutputStringMatcher,
		Matcher errOutputStringMatcher,
		boolean shouldFlushOutputDirectory) {
	File outputDirectory = new File(OUTPUT_DIR);
	if (shouldFlushOutputDirectory)
		Util.flushDirectoryContent(outputDirectory);
	try {
		if (!outputDirectory.isDirectory()) {
			outputDirectory.mkdirs();
		}
		PrintWriter sourceFileWriter;
		for (int i = 0; i < testFiles.length; i += 2) {
			String fileName = OUTPUT_DIR + File.separator + testFiles[i];
			File file = new File(fileName), innerOutputDirectory = file
					.getParentFile();
			if (!innerOutputDirectory.isDirectory()) {
				innerOutputDirectory.mkdirs();
			}
			sourceFileWriter = new PrintWriter(new FileOutputStream(file));
			try {
				sourceFileWriter.write(testFiles[i + 1]);
			} finally {
				sourceFileWriter.close();
			}
		}
	} catch (FileNotFoundException e) {
		e.printStackTrace();
		throw new RuntimeException(e);
	}
	String printerWritersNameRoot = OUTPUT_DIR + File.separator + testName();
	String outFileName = printerWritersNameRoot + "out.txt",
		   errFileName = printerWritersNameRoot + "err.txt";
	Main batchCompiler;
	PrintWriter out = null;
	PrintWriter err = null;
	boolean compileOK;
	try {
		try {
			out = new PrintWriter(new FileOutputStream(outFileName));
			err = new PrintWriter(new FileOutputStream(errFileName));
			batchCompiler = new Main(out, err, false/*systemExit*/, null/*options*/, null/*progress*/);
		} catch (FileNotFoundException e) {
			System.out.println(getClass().getName() + '#' + getName());
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		try {
			final String[] tokenizeCommandLine = Main.tokenize(commandLine);
			compileOK = batchCompiler.compile(tokenizeCommandLine);
		} catch (RuntimeException e) {
			compileOK = false;
			System.out.println(getClass().getName() + '#' + getName());
			e.printStackTrace();
			throw e;
		}
	} finally {
		if (out != null)
			out.close();
		if (err != null)
			err.close();
	}
	String outOutputString = Util.fileContent(outFileName),
	       errOutputString = Util.fileContent(errFileName);
	boolean compareOK = false, outCompareOK = false, errCompareOK = false;
	String expectedErrOutputString = null, expectedOutOutputString = null;
	if (compileOK == shouldCompileOK) {
		if (outOutputStringMatcher == null) {
			outCompareOK = true;
		} else {
			outCompareOK = outOutputStringMatcher.match(outOutputString);
			expectedOutOutputString = outOutputStringMatcher.expected();
		}
		if (errOutputStringMatcher == null) {
			errCompareOK = true;
		} else {
			errCompareOK = errOutputStringMatcher.match(errOutputString);
			expectedErrOutputString = errOutputStringMatcher.expected();
		}
		compareOK = outCompareOK && errCompareOK;
	}
	if (compileOK != shouldCompileOK || !compareOK) {
		System.out.println(getClass().getName() + '#' + getName());
		for (int i = 0; i < testFiles.length; i += 2) {
			System.out.print(testFiles[i]);
			System.out.println(" [");
			System.out.println(testFiles[i + 1]);
			System.out.println("]");
		}
	}
	if (compileOK != shouldCompileOK)
		System.out.println(errOutputString);
	if (compileOK == shouldCompileOK && !compareOK) {
		System.out.println(
				    "------------ [START OUT] ------------\n"
				+   "------------- Expected: -------------\n"
				+ expectedOutOutputString
				+ "\n------------- but was:  -------------\n"
				+ outOutputString
				+ "\n--------- (cut and paste:) ----------\n"
				+ Util.displayString(outputDirNormalizer
						.normalized(outOutputString))
				+ "\n------------- [END OUT] -------------\n"
				+   "------------ [START ERR] ------------\n"
				+   "------------- Expected: -------------\n"
				+ expectedErrOutputString
				+ "\n------------- but was:  -------------\n"
				+ errOutputString
				+ "\n--------- (cut and paste:) ----------\n"
				+ Util.displayString(outputDirNormalizer
						.normalized(errOutputString))
				+ "\n------------- [END ERR] -------------\n");
	}
	if (shouldCompileOK)
		assertTrue("Unexpected problems: " + errOutputString, compileOK);
	else
		assertTrue("Unexpected success: " + errOutputString, !compileOK);
	if (!outCompareOK) {
		// calling assertEquals to benefit from the comparison UI
		// (need appropriate exception)
		assertEquals(
				"Unexpected standard output for invocation with arguments ["
					+ commandLine + "]",
				expectedOutOutputString,
				outOutputString);
	}
	if (!errCompareOK) {
		assertEquals(
				"Unexpected error output for invocation with arguments ["
					+ commandLine + "]",
				expectedErrOutputString,
				errOutputString);
	}
}	
private void runClasspathTest(String classpathInput, String[] expectedClasspathEntries,
		String expectedError) {
	File outputDirectory = new File(OUTPUT_DIR);
	if (!outputDirectory.isDirectory()) {
		outputDirectory.mkdirs();
	}
	ArrayList paths = new ArrayList(Main.DEFAULT_SIZE_CLASSPATH);
//	try {
		(new Main(new PrintWriter(System.out), new PrintWriter(System.err), true/*systemExit*/, null/*options*/, null/*progress*/)).
			processPathEntries(Main.DEFAULT_SIZE_CLASSPATH, paths, classpathInput, null /* customEncoding */, true /* isSourceOnly */, false /* rejectDestinationPathOnJars*/);
//	} catch (InvalidInputException e) {
//		// e.printStackTrace();
//		if (expectedError == null) {
//			fail("unexpected invalid input exception: " + e.getMessage());
//		} else if (! expectedError.equals(e.getMessage())) {
//			System.out.println("\"" + e.getMessage() + "\"");
//			assertEquals(expectedError, e.getMessage());
//		}
//		return;
//	}
	if (expectedError == null) {
		int l = paths.size();
		assertEquals("unexpected classpaths entries number: ",
				expectedClasspathEntries == null ? 0 : expectedClasspathEntries.length / 3, l);
		for (int i = 0, j = 0; i < l ; i++) {
			ClasspathLocation result = (ClasspathLocation) paths.get(i);
			String expected = expectedClasspathEntries[j++];
			String actual = result.toString();
			if (! actual.equals("ClasspathDirectory " + expected + File.separator) &&
					! actual.equals("Classpath for jar file " + expected)) {
				assertEquals("dir/jar " + expected, actual);
			}
			expected = expectedClasspathEntries[j++];
			if (result.accessRuleSet == null) {
				assertNull("actual access rule is null instead of <" + expected +">", expected);
			} else if (! result.accessRuleSet.toString(false).
					startsWith("AccessRuleSet " + expected)) {
				System.out.println("\"" + result.accessRuleSet.toString(false) + "\"");
				fail("inappropriate rules (expected " + expected +
					", got " + result.accessRuleSet.toString(false));
			}
			expected = expectedClasspathEntries[j++];
			if (expected == null) {
				assertNull(result.destinationPath);
			} else if (expected == Main.NONE &&
					result.destinationPath != Main.NONE) {
				fail("expected 'none' output directory");
			} else if (! expected.equals(result.destinationPath)) {
				System.out.println("\"" + result.destinationPath + "\"");
				assertEquals(expected, result.destinationPath);
			}
		}
	} else {
		fail("missing error: " + expectedError);
	}
}

/**
 * Check that no line of message extends beyond width columns. Tabs count for
 * 4 characters.
 * @param message the message to check
 * @param width the maximum number of columns for the message
 */
private void checkWidth(String message, int width) {
	BufferedReader reader = new BufferedReader(
			new StringReader(message.replaceAll("\t", "    ")));
	String line;
	try {
		while ((line = reader.readLine()) != null) {
			assertTrue("line exceeds " + width + "characters: " + line,
				line.length() <= width);
		}
	} catch (IOException e) {
		// should never happen on a StringReader
	}
}

static abstract class Matcher {
	abstract boolean match(String effective);
	abstract String expected(); // for use in JUnit comparison framework
}
static class StringMatcher extends Matcher {
	private String expected;
	private Normalizer normalizer;
	StringMatcher(String expected, Normalizer normalizer) {
		this.expected = expected;
		this.normalizer = normalizer;
	}
	boolean match(String effective) {
		if (this.expected == null) {
			return effective == null;
		}
		if (this.normalizer == null) {
			return this.expected.equals(effective);
		}
		return this.expected.equals(this.normalizer.normalized(effective));
	}
	String expected() {
		return this.expected;
	}
}
static class SubstringMatcher extends Matcher {
	private String substring;
	SubstringMatcher(String substring) {
		this.substring = substring;
	}
	boolean match(String effective) {
		effective = outputDirNormalizer.normalized(effective);
		return effective.indexOf(this.substring) != -1;
	}
	String expected() {
		return "*" + this.substring + "*";
	}
}
static final Matcher EMPTY_STRING_MATCHER = new Matcher() {
	String expected() {
		return org.eclipse.jdt.internal.compiler.util.Util.EMPTY_STRING;
	}
	boolean match(String effective) {
		return effective != null && effective.length() == 0;
	}
};
static final Matcher ONE_FILE_GENERATED_MATCHER = new SubstringMatcher("[1 .class file generated]");
static final Matcher TWO_FILES_GENERATED_MATCHER = new SubstringMatcher("[2 .class files generated]");
	/**
	 * Abstract normalizer for output comparison. This class merely embodies a
	 * chain of responsibility, plus the signature of the method of interest
	 * here, that is {@link #normalized(String) normalized}.
	 */
	private static abstract class Normalizer {
		private Normalizer nextInChain;
		Normalizer(Normalizer nextInChain) {
			this.nextInChain = nextInChain;
		}
		String normalized(String originalValue) {
			String result;
			if (nextInChain == null)
				result = Util.convertToIndependantLineDelimiter(originalValue);
			else
				result = nextInChain.normalized(originalValue);
			return result;
		}
	}

	/**
	 * This normalizer replaces occurrences of a given string with a given
	 * placeholder.
	 */
	private static class StringNormalizer extends Normalizer {
		private String match;
		private int matchLength;
		private String placeholder;
		StringNormalizer(Normalizer nextInChain, String match, String placeholder) {
			super(nextInChain);
			this.match = match;
			this.matchLength = match.length();
			this.placeholder = placeholder;
		}
		String normalized(String originalValue) {
			String result;
			StringBuffer normalizedValueBuffer = new StringBuffer(originalValue);
			int nextOccurrenceIndex;
			while ((nextOccurrenceIndex = normalizedValueBuffer.indexOf(match)) != -1)
				normalizedValueBuffer.replace(nextOccurrenceIndex,
						nextOccurrenceIndex + matchLength, placeholder);
			result = super.normalized(normalizedValueBuffer.toString());
			return result;
		}
	}

	protected static class TestCompilationProgress extends CompilationProgress {
		boolean isCanceled = false;
		int workedSoFar = 0;
		StringBuffer buffer = new StringBuffer();
		public void begin(int remainingWork) {
			this.buffer.append("----------\n[worked: 0 - remaining: ").append(remainingWork).append("]\n");
		}
		public void done() {
			this.buffer.append("----------\n");
		}
		public boolean isCanceled() {
			return this.isCanceled;
		}
		public void setTaskName(String name) {
			this.buffer.append(name).append('\n');
		}
		public String toString() {
			return this.buffer.toString();
		}
		public void worked(int workIncrement, int remainingWork) {
			this.workedSoFar += workIncrement;
			this.buffer.append("[worked: ").append(this.workedSoFar).append(" - remaining: ").append(remainingWork).append("]\n");
		}
	}
	
	/**
	 * This normalizer replaces the whole classpaths section of a log file with
	 * a normalized placeholder.
	 */
	private static class XMLClasspathsSectionNormalizer extends Normalizer {
		XMLClasspathsSectionNormalizer() {
			super(null);
		}
		XMLClasspathsSectionNormalizer(Normalizer nextInChain) {
			super(nextInChain);
		}
		String normalized(String originalValue) {
			String result;
			StringBuffer normalizedValueBuffer = new StringBuffer(originalValue);
			int classpathsStartTagStart = normalizedValueBuffer
					.indexOf("<classpaths>"), classpathsEndTagStart = normalizedValueBuffer
					.indexOf("</classpaths>");
			if (classpathsStartTagStart != -1 && classpathsEndTagStart != -1
					&& classpathsStartTagStart < classpathsEndTagStart)
				normalizedValueBuffer.replace(classpathsStartTagStart + 12,
						classpathsEndTagStart, "NORMALIZED SECTION");
			result = super.normalized(normalizedValueBuffer.toString());
			return result;
		}
	}

	/**
	 * This normalizer removes a selected range of lines from a log file.
	 */
	private static class LinesRangeNormalizer extends Normalizer {
		private int first, number;

		LinesRangeNormalizer() {
			super(null);
			first = number = 0;
		}

		LinesRangeNormalizer(Normalizer nextInChain) {
			super(nextInChain);
			first = number = 0;
		}

		/**
		 * Make a new normalizer able to suppress a range of lines delimited by
		 * "\n" sequences from a log file (or another string).
		 *
		 * @param nextInChain
		 *            the next normalizer in the chain of responsibility; pass
		 *            null if none is needed
		 * @param firstLineToRemove
		 *            the index of the first line to remove, starting at 0
		 * @param linesNumber
		 *            the number or lines to remove; if 0, no other
		 *            transformation occurs than those operated by nextInChain
		 *            (if any)
		 */
		LinesRangeNormalizer(Normalizer nextInChain, int firstLineToRemove,
				int linesNumber) {
			super(nextInChain);
			first = firstLineToRemove;
			number = linesNumber >= 0 ? linesNumber : 0;
		}

		String normalized(String originalValue) {
			String result;
			if (number == 0 || originalValue.length() == 0)
				result = super.normalized(originalValue);
			else {
				final int START = 0, KEEPING = 1, KEEPING_R = 2, SKIPING = 3, SKIPING_R = 4, END = 5, ERROR = 6;
				int state = START, currentLineIndex = 0, currentCharIndex = 0, sourceLength;
				char currentChar = '\0';
				if (first <= 0)
					state = SKIPING;
				else
					state = KEEPING;
				StringBuffer normalizedValueBuffer = new StringBuffer(), source = new StringBuffer(
						originalValue);
				sourceLength = source.length();
				while (state != END && state != ERROR) {
					if (currentCharIndex < sourceLength) {
						currentChar = source.charAt(currentCharIndex++);
						switch (currentChar) {
						case '\r':
							switch (state) {
							case KEEPING:
								normalizedValueBuffer.append(currentChar);
								state = KEEPING_R;
								break;
							case SKIPING:
								state = SKIPING_R;
								break;
							default:
								state = ERROR;
							}
							break;
						case '\n':
							currentLineIndex++;
							switch (state) {
							case KEEPING: // tolerate Linux line delimiters
							case KEEPING_R:
								normalizedValueBuffer.append(currentChar);
								if (currentLineIndex == first) {
									state = SKIPING;
								}
								break;
							case SKIPING: // tolerate Linux line delimiters
							case SKIPING_R:
								// in effect, we tolerate too big first and number
								// values
								if (currentLineIndex >= first + number) {
									if (currentCharIndex < sourceLength)
										normalizedValueBuffer.append(source
												.substring(currentCharIndex));
									state = END;
								} else {
									state = SKIPING;
								}
								break;
							default:
								state = ERROR;
							}
							break;
						default:
							switch (state) {
							case KEEPING:
								normalizedValueBuffer.append(currentChar);
								break;
							case SKIPING:
								break;
							default:
								state = ERROR;
							}

						}
					}
					else if (currentChar == '\n')
						state = END;
					else
						state = ERROR;
				}
				if (state == ERROR)
					normalizedValueBuffer
							.append("UNEXPECTED ERROR in LinesRangeNormalizer");
				result = super.normalized(normalizedValueBuffer.toString());
			}
			return result;
		}
	}

	/**
	 * Normalizer instance that replaces occurrences of OUTPUT_DIR with
	 * OUTPUT_DIR_PLACEHOLDER and changes file separator to / if the
	 * platform file separator is different from /.
	 */
	static Normalizer outputDirNormalizer;
	static {
		if (File.separatorChar == '/') {
			outputDirNormalizer = 
				new StringNormalizer(
					new StringNormalizer(
						null, OUTPUT_DIR, OUTPUT_DIR_PLACEHOLDER),
					LIB_DIR, LIB_DIR_PLACEHOLDER);
		}
		else {
			outputDirNormalizer = 
				new StringNormalizer(
					new StringNormalizer(
						new StringNormalizer(
							null, File.separator, "/"),
						OUTPUT_DIR, OUTPUT_DIR_PLACEHOLDER),
					LIB_DIR, LIB_DIR_PLACEHOLDER);
		}
	}

	/**
	 * Normalizer instance for non XML log files.
	 */
	private static Normalizer textLogsNormalizer = new StringNormalizer(
			new XMLClasspathsSectionNormalizer(new LinesRangeNormalizer(null,
					0, 2)), OUTPUT_DIR, OUTPUT_DIR_PLACEHOLDER);

	/**
	 * Normalizer instance for XML log files.
	 */
	private static Normalizer xmlLogsNormalizer = new StringNormalizer(
			new XMLClasspathsSectionNormalizer(new LinesRangeNormalizer(null,
					1, 1)), OUTPUT_DIR, OUTPUT_DIR_PLACEHOLDER);


	/**
	 * Return true if and only if the two strings passed as parameters compare
	 * equal, modulo the transformation of the second string by a normalizer
	 * passed in parameter. This is meant to erase the variations of subparts of
	 * the compared strings in function of the test machine, the user account,
	 * etc.
	 *
	 * @param keep
	 *            the first string to compare, gets compared as it is
	 * @param normalize
	 *            the second string to compare, passed through the normalizer
	 *            before comparison
	 * @param normalizer
	 *            the transformation applied to normalize
	 * @return true if keep and normalize compare equal after normalize has been
	 *         normalized
	 */
	private boolean semiNormalizedComparison(String keep, String normalize,
			Normalizer normalizer) {
		if (keep == null)
			return normalize == null;
		if (normalize == null)
			return false;
		// return keep.equals(normalizer.normalized(normalize));
		return equals(keep, normalizer.normalized(normalize));
	}
	
private static boolean equals(String a, String b) {
	StringBuffer aBuffer = new StringBuffer(a), bBuffer = new StringBuffer(b);
	int length = aBuffer.length(), bLength;
	boolean result = true;
	if (length != (bLength = bBuffer.length())) {
		System.err.println("a and b lengths differ");
		if (length > bLength) {
			length = bLength;
		}
		result = false;
	}
	for (int i = 0; i < length; i++)
		if (aBuffer.charAt(i) != bBuffer.charAt(i)) {
			int beforeStart = i - 5, beforeEnd = i - 1, afterStart = i + 1, afterEnd = i + 5;
			if (beforeStart < 0) {
				beforeStart = 0;
				if (beforeEnd < 0)
					beforeEnd = 0;
			}
			if (afterEnd >= length) {
				afterEnd = length - 1;
				if (afterStart >= length)
					afterStart = length - 1;
			}
			System.err.println("a and b differ at rank: " + i
					+ "\na: ..." + aBuffer.substring(beforeStart, beforeEnd)
						+ "<" + aBuffer.charAt(i) + ">"
						+ aBuffer.substring(afterStart, afterEnd) + "..."
					+ "\nb: ..." + bBuffer.substring(beforeStart, beforeEnd)
						+ "<" + bBuffer.charAt(i) + ">"
						+ bBuffer.substring(afterStart, afterEnd) + "...");
			return false;
		}
	return result; // may be false if one of the strings equals the beginning
	               // of the other one, which is longer anyway
}

public void test001() {

		String commandLine = "-classpath \"D:/a folder\";d:/jdk1.4/jre/lib/rt.jar -1.4 -preserveAllLocals -g -verbose d:/eclipse/workspaces/development2.0/plugins/Bar/src2/ -d d:/test";
		String expected = " <-classpath> <D:/a folder;d:/jdk1.4/jre/lib/rt.jar> <-1.4> <-preserveAllLocals> <-g> <-verbose> <d:/eclipse/workspaces/development2.0/plugins/Bar/src2/> <-d> <d:/test>";

		String[] args = Main.tokenize(commandLine);
		StringBuffer  buffer = new StringBuffer(30);
		for (int i = 0; i < args.length; i++){
			buffer.append(" <"+args[i]+">");
		}
		String result = buffer.toString();
		//System.out.println(Util.displayString(result, 2));
		assertEquals("incorrect tokenized command line",
			expected,
			result);
}
public void test002() {

		String commandLine = "-classpath \"a folder\";\"b folder\"";
		String expected = " <-classpath> <a folder;b folder>";

		String[] args = Main.tokenize(commandLine);
		StringBuffer  buffer = new StringBuffer(30);
		for (int i = 0; i < args.length; i++){
			buffer.append(" <"+args[i]+">");
		}
		String result = buffer.toString();
		//System.out.println(Util.displayString(result, 2));
		assertEquals("incorrect tokenized command line",
			expected,
			result);
}
public void test003() {

		String commandLine = "-classpath \"a folder;b folder\"";
		String expected = " <-classpath> <a folder;b folder>";

		String[] args = Main.tokenize(commandLine);
		StringBuffer  buffer = new StringBuffer(30);
		for (int i = 0; i < args.length; i++){
			buffer.append(" <"+args[i]+">");
		}
		String result = buffer.toString();
		//System.out.println(Util.displayString(result, 2));
		assertEquals("incorrect tokenized command line",
			expected,
			result);
}
public void test004() {

		String commandLine = "\"d:/tmp A/\"A.java  -classpath \"d:/tmp A\";d:/jars/rt.jar -nowarn -time -g -d d:/tmp";
		String expected = " <d:/tmp A/A.java> <-classpath> <d:/tmp A;d:/jars/rt.jar> <-nowarn> <-time> <-g> <-d> <d:/tmp>";

		String[] args = Main.tokenize(commandLine);
		StringBuffer  buffer = new StringBuffer(30);
		for (int i = 0; i < args.length; i++){
			buffer.append(" <"+args[i]+">");
		}
		String result = buffer.toString();
		//System.out.println(Util.displayString(result, 2));
		assertEquals("incorrect tokenized command line",
			expected,
			result);
}
public void test005() {

		String commandLine = "\"d:/tmp A/\"A.java  -classpath d:/jars/rt.jar;\"d:/tmp A\";\"toto\" -nowarn -time -g -d d:/tmp";
		String expected = " <d:/tmp A/A.java> <-classpath> <d:/jars/rt.jar;d:/tmp A;toto> <-nowarn> <-time> <-g> <-d> <d:/tmp>";

		String[] args = Main.tokenize(commandLine);
		StringBuffer  buffer = new StringBuffer(30);
		for (int i = 0; i < args.length; i++){
			buffer.append(" <"+args[i]+">");
		}
		String result = buffer.toString();
		//System.out.println(Util.displayString(result, 2));
		assertEquals("incorrect tokenized command line",
			expected,
			result);
}
public void test006() {

		String commandLine = "\"d:/tmp A/A.java\"  -classpath d:/jars/rt.jar;\"d:/tmp A\";d:/tmpB/ -nowarn -time -g -d d:/tmp";
		String expected = " <d:/tmp A/A.java> <-classpath> <d:/jars/rt.jar;d:/tmp A;d:/tmpB/> <-nowarn> <-time> <-g> <-d> <d:/tmp>";

		String[] args = Main.tokenize(commandLine);
		StringBuffer  buffer = new StringBuffer(30);
		for (int i = 0; i < args.length; i++){
			buffer.append(" <"+args[i]+">");
		}
		String result = buffer.toString();
		//System.out.println(Util.displayString(result, 2));
		assertEquals("incorrect tokenized command line",
			expected,
			result);
}
// test the tester - runConformTest
public void test007(){
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"\n" +
			"@SuppressWarnings(\"all\"//$NON-NLS-1$\n" +
			")\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		if (false) {\n" +
			"			;\n" +
			"		} else {\n" +
			"		}\n" +
			"		// Zork z;\n" +
			"	}\n" +
			"}"
        },
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -bootclasspath " + getLibraryClassesAsQuotedString()
        + " -cp " + getJCEJarAsQuotedString()
        + " -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -verbose -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "[parsing    ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]\n" +
		"[reading    java/lang/Object.class]\n" +
		"[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]\n" +
		"[reading    java/util/List.class]\n" +
		"[reading    java/lang/SuppressWarnings.class]\n" +
		"[reading    java/lang/String.class]\n" +
		"[writing    X.class - #1]\n" +
		"[completed  ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]\n" +
		"[1 unit compiled]\n" +
		"[1 .class file generated]\n",
        "", // changed with bug 123522: now the SuppressWarning upon the first type
        	// influences warnings on unused imports
        true);
}
// test the tester - runNegativeTest
public void test008(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"\n" +
			"@SuppressWarnings(\"all\"//$NON-NLS-1$\n" +
			")\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		if (false) {\n" +
			"			;\n" +
			"		} else {\n" +
			"		}\n" +
			"		Zork z;\n" +
			"	}\n" +
			"}"
        },
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -bootclasspath " + getLibraryClassesAsQuotedString()
        + " -cp " + getJCEJarAsQuotedString()
        + " -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "----------\n" +
        "1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 11)\n" +
        "	Zork z;\n" +
        "	^^^^\n" +
        "Zork cannot be resolved to a type\n" +
        "----------\n" +
        "1 problem (1 error)",
        true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=92398 -- a case that works, another that does not
// revisit this test case depending on https://bugs.eclipse.org/bugs/show_bug.cgi?id=95349
public void test009(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"	OK1 ok1;\n" +
			"	OK2 ok2;\n" +
			"	Warn warn;\n" +
			"	KO ko;\n" +
	        "	Zork z;\n" +
			"}",
			"OK1.java",
			"/** */\n" +
			"public class OK1 {\n" +
			"	// empty\n" +
			"}",
			"OK2.java",
			"/** */\n" +
			"public class OK2 {\n" +
			"	// empty\n" +
			"}",
			"Warn.java",
			"/** */\n" +
			"public class Warn {\n" +
			"	// empty\n" +
			"}",
			"KO.java",
			"/** */\n" +
			"public class KO {\n" +
			"	// empty\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[+OK2" + File.pathSeparator + "~Warn"
        	+ File.pathSeparator + "-KO]\""
        + " -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
		"----------\n" +
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" +
		"	Warn warn;\n" +
		"	^^^^\n" +
		"Discouraged access: The type Warn is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---\n" +
		"----------\n" +
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)\n" +
		"	KO ko;\n" +
		"	^^\n" +
		"Access restriction: The type KO is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---\n" +
		"----------\n" +
		"3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n" +
		"3 problems (1 error, 2 warnings)",
        true);
}
// command line - no user classpath nor bootclasspath
public void test010(){
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"\n" +
			"@SuppressWarnings(\"all\"//$NON-NLS-1$\n" +
			")\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		if (false) {\n" +
			"			;\n" +
			"		} else {\n" +
			"		}\n" +
			"		// Zork z;\n" +
			"	}\n" +
			"}"
        },
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -verbose -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "[parsing    ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]\n" +
		"[reading    java/lang/Object.class]\n" +
		"[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]\n" +
		"[reading    java/util/List.class]\n" +
		"[reading    java/lang/SuppressWarnings.class]\n" +
		"[reading    java/lang/String.class]\n" +
		"[writing    X.class - #1]\n" +
		"[completed  ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]\n" +
		"[1 unit compiled]\n" +
		"[1 .class file generated]\n",
        "",
        true);
}
// command line - unusual classpath (ends with ';', still OK)
public void test011_classpath(){
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[+**/OK2;~**/Warn;-KO]"
        + "\"" + File.pathSeparator
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
}
// command line - help
// amended for https://bugs.eclipse.org/bugs/show_bug.cgi?id=141512 (checking
// width)
public void test012(){
	final String expectedOutput =
        "{0} {1}\n" +
        "{2}\n" +
        " \n" +
        " Usage: <options> <source files | directories>\n" +
        " If directories are specified, then their source contents are compiled.\n" +
        " Possible options are listed below. Options enabled by default are prefixed\n" +
        " with ''+''.\n" +
        " \n" +
        " Classpath options:\n" +
        "    -cp -classpath <directories and ZIP archives separated by " + File.pathSeparator + ">\n" +
        "                       specify location for application classes and sources.\n" +
        "                       Each directory or file can specify access rules for\n" +
        "                       types between ''['' and '']'' (e.g. [-X] to forbid\n" +
        "                       access to type X, [~X] to discourage access to type X,\n" +
        "                       [+p/X" + File.pathSeparator + "-p/*] to forbid access to all types in package p\n" +
        "                       but allow access to p/X)\n" +
        "    -bootclasspath <directories and ZIP archives separated by " + File.pathSeparator + ">\n" +
        "                       specify location for system classes. Each directory or\n" +
        "                       file can specify access rules for types between ''[''\n" +
        "                       and '']''\n" +
        "    -sourcepath <directories and ZIP archives separated by " + File.pathSeparator + ">\n" +
        "                       specify location for application sources. Each directory\n" +
        "                       or file can specify access rules for types between ''[''\n" +
        "                       and '']''. Each directory can further specify a specific\n" +
        "                       destination directory using a ''-d'' option between ''[''\n" +
        "                       and '']''; this overrides the general ''-d'' option.\n" +
        "                       .class files created from source files contained in a\n" +
        "                       jar file are put in the user.dir folder in case no\n" +
        "                       general ''-d'' option is specified. ZIP archives cannot\n" +
        "                       override the general ''-d'' option\n" +
        "    -extdirs <directories separated by " + File.pathSeparator + ">\n" +
        "                       specify location for extension ZIP archives\n" +
        "    -endorseddirs <directories separated by " + File.pathSeparator + ">\n" +
        "                       specify location for endorsed ZIP archives\n" +
        "    -d <dir>           destination directory (if omitted, no directory is\n" +
        "                       created); this option can be overridden per source\n" +
		"                       directory\n" +
		"    -d none            generate no .class files\n" +
        "    -encoding <enc>    specify custom encoding for all sources. Each\n" +
        "                       file/directory can override it when suffixed with\n" +
        "                       ''[''<enc>'']'' (e.g. X.java[utf8])\n" +
        " \n" +
        " Compliance options:\n" +
        "    -1.3               use 1.3 compliance (-source 1.3 -target 1.1)\n" +
        "    -1.4             + use 1.4 compliance (-source 1.3 -target 1.2)\n" +
        "    -1.5 -5 -5.0       use 1.5 compliance (-source 1.5 -target 1.5)\n" +
        "    -1.6 -6 -6.0       use 1.6 compliance (-source 1.6 -target 1.6)\n" +
        "    -1.7 -7 -7.0       use 1.7 compliance (-source 1.7 -target 1.7)\n" +
        "    -source <version>  set source level: 1.3 to 1.7 (or 5, 5.0, etc)\n" +
        "    -target <version>  set classfile target: 1.1 to 1.7 (or 5, 5.0, etc)\n" +
        "                       cldc1.1 can also be used to generate the StackMap\n" + 
        "                       attribute\n" + 
        " \n" +
        " Warning options:\n" +
        "    -deprecation     + deprecation outside deprecated code (equivalent to\n" +
        "                       -warn:+deprecation)\n" +
        "    -nowarn -warn:none disable all warnings\n" +
        "    -?:warn -help:warn display advanced warning options\n" +
        " \n" +
        " Debug options:\n" +
        "    -g[:lines,vars,source] custom debug info\n" +
        "    -g:lines,source  + both lines table and source debug info\n" +
        "    -g                 all debug info\n" +
        "    -g:none            no debug info\n" +
        "    -preserveAllLocals preserve unused local vars for debug purpose\n" +
        " \n" +
        " Annotation processing options:\n" + 
        "   These options are meaningful only in a 1.6 environment.\n" + 
        "    -Akey[=value]        options that are passed to annotation processors\n" + 
        "    -processorpath <directories and ZIP archives separated by " + File.pathSeparator + ">\n" + 
        "                         specify locations where to find annotation processors.\n" + 
        "                         If this option is not used, the classpath will be\n" + 
        "                         searched for processors\n" + 
        "    -processor <class1[,class2,...]>\n" + 
        "                         qualified names of the annotation processors to run.\n" + 
        "                         This bypasses the default annotation discovery process\n" + 
        "    -proc:only           run annotation processors, but do not compile\n" + 
        "    -proc:none           perform compilation but do not run annotation\n" + 
        "                         processors\n" + 
        "    -s <dir>             destination directory for generated source files\n" + 
        "    -XprintProcessorInfo print information about which annotations and elements\n" + 
        "                         a processor is asked to process\n" + 
        "    -XprintRounds        print information about annotation processing rounds\n" + 
        "    -classNames <className1[,className2,...]>\n" + 
        "                         qualified names of binary classes to process\n" + 
        " \n" +
        " Advanced options:\n" +
        "    @<file>            read command line arguments from file\n" +
        "    -maxProblems <n>   max number of problems per compilation unit (100 by\n" +
        "                       default)\n" +
        "    -log <file>        log to a file. If the file extension is ''.xml'', then\n" +
        "                       the log will be a xml file.\n" +
        "    -proceedOnError    do not stop at first error, dumping class files with\n" +
        "                       problem methods\n" +
        "    -verbose           enable verbose output\n" +
        "    -referenceInfo     compute reference info\n" +
        "    -progress          show progress (only in -log mode)\n" +
        "    -time              display speed information \n" +
        "    -noExit            do not call System.exit(n) at end of compilation (n==0\n" +
        "                       if no error)\n" +
        "    -repeat <n>        repeat compilation process <n> times for perf analysis\n" +
        "    -inlineJSR         inline JSR bytecode (implicit if target >= 1.5)\n" +
        "    -enableJavadoc     consider references in javadoc\n" +
        "    -Xemacs            used to enable emacs-style output in the console.\n" +
        "                       It does not affect the xml log output\n" +
        " \n" +
        "    -? -help           print this help message\n" +
        "    -v -version        print compiler version\n" +
        "    -showversion       print compiler version and continue\n" +
        " \n" +
        " Ignored options:\n" +
        "    -J<option>         pass option to virtual machine (ignored)\n" +
        "    -X<option>         specify non-standard option (ignored\n" +
        "                       except for listed -X options)\n" +
        "    -X                 print non-standard options and exit (ignored)\n" +
        "    -O                 optimize for execution time (ignored)\n" +
        "\n";
	String expandedExpectedOutput =
		MessageFormat.format(expectedOutput, new String[] {
				MAIN.bind("compiler.name"),
				MAIN.bind("compiler.version"),
				MAIN.bind("compiler.copyright")
		// because misc.version is mono-line - reconsider if this changes
//		MessageFormat.format(expectedOutput, new String[] {
//				Main.bind("misc.version", new String[] {
//					Main.bind("compiler.name"),
//					Main.bind("compiler.version"),
//					Main.bind("compiler.copyright")
//				}),
				// File.pathSeparator
			});
		this.runConformTest(
		new String[0],
        " -help -referenceInfo",
        expandedExpectedOutput,
        "", true);
	checkWidth(expandedExpectedOutput, 80);
}
//command line - help
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=144248
// Progressive help text modifies the help options and messages.
// amended for https://bugs.eclipse.org/bugs/show_bug.cgi?id=141512 (checking
// width)
public void test012b(){
	final String expectedOutput =
        "{0} {1}\n" +
        "{2}\n" +
        " \n" +
        " Warning options:\n" +
        "    -deprecation     + deprecation outside deprecated code\n" +
        "    -nowarn -warn:none disable all warnings\n" +
        "    -warn:<warnings separated by ,>    enable exactly the listed warnings\n" +
        "    -warn:+<warnings separated by ,>   enable additional warnings\n" +
        "    -warn:-<warnings separated by ,>   disable specific warnings\n" +
        "      allDeprecation       deprecation including inside deprecated code\n" +
        "      allJavadoc           invalid or missing javadoc\n" +
        "      assertIdentifier   + ''assert'' used as identifier\n" +
        "      boxing               autoboxing conversion\n" +
        "      charConcat         + char[] in String concat\n" +
        "      conditionAssign      possible accidental boolean assignment\n" +
        "      constructorName    + method with constructor name\n" +
        "      dep-ann              missing @Deprecated annotation\n" +
        "      deprecation        + deprecation outside deprecated code\n" +
        "      discouraged        + use of types matching a discouraged access rule\n" +
        "      emptyBlock           undocumented empty block\n" +
        "      enumSwitch           incomplete enum switch\n" +
        "      fallthrough          possible fall-through case\n" +
        "      fieldHiding          field hiding another variable\n" +
        "      finalBound           type parameter with final bound\n" +
        "      finally            + finally block not completing normally\n" +
        "      forbidden          + use of types matching a forbidden access rule\n" +
        "      hiding               macro for fieldHiding, localHiding, typeHiding and\n" +
        "                           maskedCatchBlock\n" +
        "      incomplete-switch    same as enumSwitch\n" +
        "      indirectStatic       indirect reference to static member\n" +
        "      intfAnnotation     + annotation type used as super interface\n" +
        "      intfNonInherited   + interface non-inherited method compatibility\n" +
        "      javadoc              invalid javadoc\n" +
        "      localHiding          local variable hiding another variable\n" +
        "      maskedCatchBlock   + hidden catch block\n" +
        "      nls                  string literal lacking non-nls tag //$NON-NLS-<n>$\n" +
        "      noEffectAssign     + assignment without effect\n" +
        "      null                 potential missing or redundant null check\n" +
        "      nullDereference    + missing null check\n" +
        "      over-ann             missing @Override annotation\n" +
        "      paramAssign          assignment to a parameter\n" +
        "      pkgDefaultMethod   + attempt to override package-default method\n" +
        "      raw                + usage of raw type\n" +
        "      redundantSuperinterface                + find redundant superinterfaces\n" +
        "      semicolon            unnecessary semicolon, empty statement\n" +
        "      serial             + missing serialVersionUID\n" +
        "      specialParamHiding   constructor or setter parameter hiding a field\n" +
        "      static-access        macro for indirectStatic and staticReceiver\n" +
        "      staticReceiver     + non-static reference to static member\n" +
        "      super                overriding a method without making a super invocation\n" +
        "      suppress           + enable @SuppressWarnings\n" +
        "      synthetic-access     same as syntheticAccess\n" +
        "      syntheticAccess      synthetic access for innerclass\n" +
        "      tasks(<tags separated by |>) tasks identified by tags inside comments\n" +
        "      typeHiding         + type parameter hiding another type\n" +
        "      unchecked          + unchecked type operation\n" +
        "      unnecessaryElse      unnecessary else clause\n" +
        "      unqualified-field-access same as unqualifiedField\n" +
        "      unqualifiedField     unqualified reference to field\n" +
        "      unused               macro for unusedArgument, unusedImport, unusedLabel,\n" +
        "                               unusedLocal, unusedPrivate, unusedThrown,\n" + 
        "                               and unusedTypeArgs\n" +
        "      unusedArgument       unread method parameter\n" +
        "      unusedImport       + unused import declaration\n" +
        "      unusedLabel        + unused label\n" +
        "      unusedLocal        + unread local variable\n" +
        "      unusedPrivate      + unused private member declaration\n" +
        "      unusedThrown         unused declared thrown exception\n" +
        "      unusedTypeArgs     + unused type arguments for method\n" +
        "      uselessTypeCheck     unnecessary cast/instanceof operation\n" +
        "      varargsCast        + varargs argument need explicit cast\n" +
        "      warningToken       + unsupported or unnecessary @SuppressWarnings\n" +
        " \n" +
        " Advanced options:\n" +
        "    -? -help           print the help message\n" +
        "\n";
	String expandedExpectedOutput =
		MessageFormat.format(expectedOutput, new String[] {
				MAIN.bind("compiler.name"),
				MAIN.bind("compiler.version"),
				MAIN.bind("compiler.copyright")
		// because misc.version is mono-line - reconsider if this changes
//		MessageFormat.format(expectedOutput, new String[] {
//				Main.bind("misc.version", new String[] {
//					Main.bind("compiler.name"),
//					Main.bind("compiler.version"),
//					Main.bind("compiler.copyright")
//				}),
				// File.pathSeparator
			});
	this.runConformTest(
		new String[0],
        " -help:warn -referenceInfo",
        expandedExpectedOutput,
        "", true);
	checkWidth(expandedExpectedOutput, 80);
}

	// command line - xml log contents https://bugs.eclipse.org/bugs/show_bug.cgi?id=93904
	public void test013() {
		String logFileName = OUTPUT_DIR + File.separator + "log.xml";
		this.runNegativeTest(new String[] {
				"X.java",
				"/** */\n" +
				"public class X {\n" +
				"	Zork z;\n" +
				"}", },
				"\"" + OUTPUT_DIR + File.separator + "X.java\""
				+ " -1.5 -proceedOnError"
				+ " -log \"" + logFileName + "\" -d \"" + OUTPUT_DIR + "\"",
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"1 problem (1 error)",
				true);
		String logContents = Util.fileContent(logFileName);
		String expectedLogContents =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<!DOCTYPE compiler PUBLIC \"-//Eclipse.org//DTD Eclipse JDT 3.2.003 Compiler//EN\" \"http://www.eclipse.org/jdt/core/compiler_32_003.dtd\">\n" +
			"<compiler copyright=\"{2}\" name=\"{1}\" version=\"{3}\">\n" +
			"	<command_line>\n" +
			"		<argument value=\"---OUTPUT_DIR_PLACEHOLDER---{0}X.java\"/>\n" +
			"		<argument value=\"-1.5\"/>\n" +
			"		<argument value=\"-proceedOnError\"/>\n" +
			"		<argument value=\"-log\"/>\n" +
			"		<argument value=\"---OUTPUT_DIR_PLACEHOLDER---{0}log.xml\"/>\n" +
			"		<argument value=\"-d\"/>\n" +
			"		<argument value=\"---OUTPUT_DIR_PLACEHOLDER---\"/>\n" +
			"	</command_line>\n" +
			"	<options>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.codegen.inlineJsrBytecode\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.codegen.targetPlatform\" value=\"1.5\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.codegen.unusedLocal\" value=\"optimize out\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.compliance\" value=\"1.5\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.debug.lineNumber\" value=\"generate\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.debug.localVariable\" value=\"do not generate\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.debug.sourceFile\" value=\"generate\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.doc.comment.support\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.generateClassFiles\" value=\"enabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.maxProblemPerUnit\" value=\"100\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.annotationSuperInterface\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.assertIdentifier\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.autoboxing\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.deprecation\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.deprecationInDeprecatedCode\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.deprecationWhenOverridingDeprecatedMethod\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.discouragedReference\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.emptyStatement\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.enumIdentifier\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.fallthroughCase\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.fatalOptionalError\" value=\"enabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.fieldHiding\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.finalParameterBound\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.finallyBlockNotCompletingNormally\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.forbiddenReference\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.hiddenCatchBlock\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.incompatibleNonInheritedInterfaceMethod\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.incompleteEnumSwitch\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.indirectStaticAccess\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.invalidJavadoc\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.invalidJavadocTags\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.invalidJavadocTagsDeprecatedRef\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.invalidJavadocTagsNotVisibleRef\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.invalidJavadocTagsVisibility\" value=\"public\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.localVariableHiding\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.methodWithConstructorName\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingDeprecatedAnnotation\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocComments\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocCommentsOverriding\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocCommentsVisibility\" value=\"public\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocTagDescription\" value=\"return_tag\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocTags\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocTagsOverriding\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocTagsVisibility\" value=\"public\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingOverrideAnnotation\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingSerialVersion\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.noEffectAssignment\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.noImplicitStringConversion\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.nullReference\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.overridingMethodWithoutSuperInvocation\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.overridingPackageDefaultMethod\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.parameterAssignment\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.possibleAccidentalBooleanAssignment\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.potentialNullReference\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.rawTypeReference\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.redundantNullCheck\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.redundantSuperinterface\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.specialParameterHidingField\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.staticAccessReceiver\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.suppressWarnings\" value=\"enabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.syntheticAccessEmulation\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.typeParameterHiding\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.uncheckedTypeOperation\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.undocumentedEmptyBlock\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unhandledWarningToken\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unnecessaryElse\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unnecessaryTypeCheck\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unqualifiedFieldAccess\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownException\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownExceptionExemptExceptionAndThrowable\" value=\"enabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownExceptionIncludeDocCommentReference\" value=\"enabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownExceptionWhenOverriding\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedImport\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedLabel\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedLocal\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedParameter\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedParameterIncludeDocCommentReference\" value=\"enabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedParameterWhenImplementingAbstract\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedParameterWhenOverridingConcrete\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedPrivateMember\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedTypeArgumentsForMethodInvocation\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedWarningToken\" value=\"warning\"/>\n" +			"		<option key=\"org.eclipse.jdt.core.compiler.problem.varargsArgumentNeedCast\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.processAnnotations\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.source\" value=\"1.5\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.taskCaseSensitive\" value=\"enabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.taskPriorities\" value=\"\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.taskTags\" value=\"\"/>\n" + 
			"	</options>\n" +
			"	<classpaths>NORMALIZED SECTION</classpaths>\n" +
			"	<sources>\n" +
			"		<source output=\"---OUTPUT_DIR_PLACEHOLDER---\" path=\"---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java\">\n" +
			"			<problems errors=\"1\" problems=\"1\" warnings=\"0\">\n" +
			"				<problem categoryID=\"40\" charEnd=\"28\" charStart=\"25\" id=\"UndefinedType\" line=\"3\" problemID=\"16777218\" severity=\"ERROR\">\n" +
			"					<message value=\"Zork cannot be resolved to a type\"/>\n" +
			"					<source_context sourceEnd=\"3\" sourceStart=\"0\" value=\"Zork z;\"/>\n" +
			"					<arguments>\n" +
			"						<argument value=\"Zork\"/>\n" +
			"					</arguments>\n" +
			"				</problem>\n" +
			"			</problems>\n" +
			"			<classfile path=\"---OUTPUT_DIR_PLACEHOLDER---{0}X.class\"/>\n" +
			"		</source>\n" +
			"	</sources>\n" +
			"	<stats>\n" +
			"		<problem_summary errors=\"1\" problems=\"1\" tasks=\"0\" warnings=\"0\"/>\n" +
			"	</stats>\n" +
			"</compiler>\n";
		String normalizedExpectedLogContents =
				MessageFormat.format(
						expectedLogContents,
						new String[] {
								File.separator,
								MAIN.bind("compiler.name"),
								MAIN.bind("compiler.copyright"),
								MAIN.bind("compiler.version")
						});
		String normalizedLogContents =
				xmlLogsNormalizer.normalized(logContents);
		boolean compareOK = normalizedExpectedLogContents.equals(
				normalizedLogContents);
		if (!compareOK) {
			System.out.println(getClass().getName() + '#' + getName());
			System.out.println(
					  "------------ [START LOG] ------------\n"
					+ "------------- Expected: -------------\n"
					+ expectedLogContents
				  + "\n------------- but was:  -------------\n"
					+ xmlLogsNormalizer.normalized(logContents)
				  + "\n--------- (cut and paste:) ----------\n"
					+ Util.displayString(xmlLogsNormalizer.normalized(logContents))
				  + "\n------------- [END LOG] -------------\n");
			assertEquals("Unexpected log contents",
					normalizedExpectedLogContents, normalizedLogContents);
		}
	}

	// command line - txt log contents https://bugs.eclipse.org/bugs/show_bug.cgi?id=93904
	public void test014() {
		String logFileName = OUTPUT_DIR + File.separator + "log.txt";
		this.runNegativeTest(new String[] {
				"X.java",
				"/** */\n" +
				"public class X {\n" +
				"	Zork z;\n" +
				"}", },
				"\"" + OUTPUT_DIR + File.separator + "X.java\""
				+ " -1.5 -proceedOnError"
				+ " -log \"" + logFileName + "\" -d \"" + OUTPUT_DIR + "\"",
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"1 problem (1 error)",
				false);
		String logContents = Util.fileContent(logFileName);
		String expectedLogContents =
			"----------\n" +
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java (at line 3)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"1 problem (1 error)";
		boolean compareOK = semiNormalizedComparison(expectedLogContents,
				logContents, textLogsNormalizer);
		if (!compareOK) {
			System.out.println(getClass().getName() + '#' + getName());
			System.out.println(
							  "------------ [START LOG] ------------\n"
							+ "------------- Expected: -------------\n"
							+ expectedLogContents
						  + "\n------------- but was:  -------------\n"
							+ outputDirNormalizer.normalized(logContents)
						  + "\n--------- (cut and paste:) ----------\n"
							+ Util.displayString(outputDirNormalizer.normalized(logContents))
						  + "\n------------- [END LOG] -------------\n");
		}
		assertTrue("unexpected log contents", compareOK);
	}

	// command line - no extension log contents https://bugs.eclipse.org/bugs/show_bug.cgi?id=93904
	public void test015() {
		String logFileName = OUTPUT_DIR + File.separator + "log";
		this.runNegativeTest(new String[] {
				"X.java",
				"/** */\n" +
				"public class X {\n" +
				"	Zork z;\n" +
				"}", },
				"\"" + OUTPUT_DIR + File.separator + "X.java\""
				+ " -1.5 -proceedOnError"
				+ " -log \"" + logFileName + "\" -d \"" + OUTPUT_DIR + "\"",
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" +
				"	Zork z;\n" +
				"	^^^^\n" +
				"Zork cannot be resolved to a type\n" +
				"----------\n" +
				"1 problem (1 error)",
				false);
		String logContents = Util.fileContent(logFileName);
		String expectedLogContents =
			"----------\n" +
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java (at line 3)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"1 problem (1 error)";
		boolean compareOK = semiNormalizedComparison(expectedLogContents,
				logContents, textLogsNormalizer);
		if (!compareOK) {
			System.out.println(getClass().getName() + '#' + getName());
			System.out.println(
					  "------------ [START LOG] ------------\n"
					+ "------------- Expected: -------------\n"
					+ expectedLogContents
				  + "\n------------- but was:  -------------\n"
					+ outputDirNormalizer.normalized(logContents)
				  + "\n--------- (cut and paste:) ----------\n"
					+ Util.displayString(outputDirNormalizer.normalized(logContents))
				  + "\n------------- [END LOG] -------------\n");
		}
		assertTrue("unexpected log contents", compareOK);
	}
// command line - several path separators within the classpath
public void test016(){
	String setting = System.getProperty("jdt.compiler.useSingleThread");
	try {
		System.setProperty("jdt.compiler.useSingleThread", "true");
		this.runConformTest(
			new String[] {
					"X.java",
					"/** */\n" +
					"public class X {\n" +
					"	OK1 ok1;\n" +
					"}",
					"OK1.java",
					"/** */\n" +
					"public class OK1 {\n" +
					"	// empty\n" +
					"}"
			},
	        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
	        + " -1.5 -g -preserveAllLocals"
	        + " -cp ." + File.pathSeparator + File.pathSeparator + File.pathSeparator + "\"" + OUTPUT_DIR + "\""
	        + " -verbose -proceedOnError -referenceInfo"
	        + " -d \"" + OUTPUT_DIR + "\"",
			"[parsing    ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]\n" +
			"[reading    java/lang/Object.class]\n" +
			"[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]\n" +
			"[parsing    ---OUTPUT_DIR_PLACEHOLDER---/OK1.java - #2/2]\n" +
			"[writing    X.class - #1]\n" +
			"[completed  ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/2]\n" +
			"[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/OK1.java - #2/2]\n" +
			"[writing    OK1.class - #2]\n" +
			"[completed  ---OUTPUT_DIR_PLACEHOLDER---/OK1.java - #2/2]\n" +
			"[2 units compiled]\n" +
			"[2 .class files generated]\n",
	        "",
	        true);
	} finally {
		System.setProperty("jdt.compiler.useSingleThread", setting == null ? "false" : setting);
	}
}
public void test017(){
		this.runConformTest(
			new String[] {
					"X.java",
					"/** */\n" +
					"public class X {\n" +
					"	OK1 ok1;\n" +
					"}",
					"OK1.java",
					"/** */\n" +
					"public class OK1 {\n" +
					"	// empty\n" +
					"}"
			},
	        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
	        + " -1.5 -g -preserveAllLocals"
	        + " -cp dummmy_dir" + File.pathSeparator + "dummy.jar" + File.pathSeparator + File.pathSeparator + "\"" + OUTPUT_DIR + "\""
	        + " -proceedOnError -referenceInfo"
	        + " -d \"" + OUTPUT_DIR + "\"",
	        "",
	        "incorrect classpath: dummmy_dir\n",
	        true);
	}
// we tolerate inexisting jars on the classpath, and we don't even warn about
// them (javac does the same as us)
public void test017b(){
	this.runTest(
		true,
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"	OK1 ok1;\n" +
			"}",
			"OK1.java",
			"/** */\n" +
			"public class OK1 {\n" +
			"	// empty\n" +
			"}"
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp dummy.jar" + File.pathSeparator + File.pathSeparator + "\"" + OUTPUT_DIR + "\""
        + " -verbose -proceedOnError -referenceInfo"
        + " -d \"" + OUTPUT_DIR + "\"",
        TWO_FILES_GENERATED_MATCHER,
        EMPTY_STRING_MATCHER,
        true);
}
// we tolerate empty classpath entries, and we don't even warn about
// them (javac does the same as us)
public void test017c(){
	this.runTest(
		true,
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"	OK1 ok1;\n" +
			"}",
			"OK1.java",
			"/** */\n" +
			"public class OK1 {\n" +
			"	// empty\n" +
			"}"
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp " + File.pathSeparator + File.pathSeparator + "\"" + OUTPUT_DIR + "\""
        + " -verbose -proceedOnError -referenceInfo"
        + " -d \"" + OUTPUT_DIR + "\"",
        TWO_FILES_GENERATED_MATCHER,
        EMPTY_STRING_MATCHER,
        true);
}
// command line - unusual classpath (empty)
// ok provided we explicit the sourcepath
public void test018a(){
	String currentWorkingDirectoryPath = System.getProperty("user.dir");
	if (currentWorkingDirectoryPath == null) {
		System.err.println("BatchCompilerTest#18a could not access the current working directory " + currentWorkingDirectoryPath);
	} else if (!new File(currentWorkingDirectoryPath).isDirectory()) {
		System.err.println("BatchCompilerTest#18a current working directory is not a directory " + currentWorkingDirectoryPath);
	} else {
		String xPath = currentWorkingDirectoryPath + File.separator + "X.java";
		String ok1Path = currentWorkingDirectoryPath + File.separator + "OK1.java";
		PrintWriter sourceFileWriter;
		try {
			File file = new File(xPath);
			sourceFileWriter = new PrintWriter(new FileOutputStream(file));
			try {
				sourceFileWriter.write(
					"/** */\n" +
					"public class X {\n" +
					"	OK1 ok1;\n" +
					"}");
			} finally {
				sourceFileWriter.close();
			}
			file = new File(ok1Path);
			sourceFileWriter = new PrintWriter(new FileOutputStream(file));
			try {
				sourceFileWriter.write(
					"/** */\n" +
					"public class OK1 {\n" +
					"	// empty\n" +
					"}");
			} finally {
				sourceFileWriter.close();
			}
			this.runTest(
				true,
				new String[] {
					"dummy.java", // enforce output directory creation
					""
				},
		        "X.java"
		        + " -1.5 -g -preserveAllLocals"
		        + " -verbose -proceedOnError"
		        + " -sourcepath ."
		        + " -d \"" + OUTPUT_DIR + "\"",
		        TWO_FILES_GENERATED_MATCHER,
		        EMPTY_STRING_MATCHER,
		        false);
		} catch (FileNotFoundException e) {
			System.err.println("BatchCompilerTest#18a could not write to current working directory " + currentWorkingDirectoryPath);
		} finally {
			new File(xPath).delete();
			new File(ok1Path).delete();
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=214725
// empty sourcepath works with javac but not with ecj
public void _test018b(){
	String currentWorkingDirectoryPath = System.getProperty("user.dir");
	if (currentWorkingDirectoryPath == null) {
		System.err.println("BatchCompilerTest#18b could not access the current working directory " + currentWorkingDirectoryPath);
	} else if (!new File(currentWorkingDirectoryPath).isDirectory()) {
		System.err.println("BatchCompilerTest#18b current working directory is not a directory " + currentWorkingDirectoryPath);
	} else {
		String xPath = currentWorkingDirectoryPath + File.separator + "X.java";
		String ok1Path = currentWorkingDirectoryPath + File.separator + "OK1.java";
		PrintWriter sourceFileWriter;
		try {
			File file = new File(xPath);
			sourceFileWriter = new PrintWriter(new FileOutputStream(file));
			sourceFileWriter.write(
				"/** */\n" +
				"public class X {\n" +
				"	OK1 ok1;\n" +
				"}");
			sourceFileWriter.close();
			file = new File(ok1Path);
			sourceFileWriter = new PrintWriter(new FileOutputStream(file));
			sourceFileWriter.write(
				"/** */\n" +
				"public class OK1 {\n" +
				"	// empty\n" +
				"}");
			sourceFileWriter.close();
			this.runTest(
				true,
				new String[] {
					"dummy.java", // enforce output directory creation
					""
				},
		        "X.java"
		        + " -1.5 -g -preserveAllLocals"
		        + " -verbose -proceedOnError"
		        + " -d \"" + OUTPUT_DIR + "\"",
		        TWO_FILES_GENERATED_MATCHER,
		        EMPTY_STRING_MATCHER,
		        false);
		} catch (FileNotFoundException e) {
			System.err.println("BatchCompilerTest#18b could not write to current working directory " + currentWorkingDirectoryPath);
		} finally {
			new File(xPath).delete();
			new File(ok1Path).delete();
		}
	}
}
public void test019(){
		this.runNegativeTest(
			new String[] {
				"X.java",
				"/** */\n" +
				"public class X {\n" +
				"	OK1 ok1;\n" +
				"	OK2 ok2;\n" +
				"	Warn warn;\n" +
				"	KO ko;\n" +
		        "	Zork z;\n" +
				"}",
				"OK1.java",
				"/** */\n" +
				"public class OK1 {\n" +
				"	// empty\n" +
				"}",
				"OK2.java",
				"/** */\n" +
				"public class OK2 {\n" +
				"	// empty\n" +
				"}",
				"Warn.java",
				"/** */\n" +
				"public class Warn {\n" +
				"	// empty\n" +
				"}",
				"KO.java",
				"/** */\n" +
				"public class KO {\n" +
				"	// empty\n" +
				"}",
			},
	        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
	        + " -1.5 -g -preserveAllLocals"
	        + " -cp \"" + OUTPUT_DIR + "[+OK2" + File.pathSeparator + "~Warn" + File.pathSeparator + "-KO]\""
	        + " -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
	        + " -proceedOnError -referenceInfo"
	        + " -d \"" + OUTPUT_DIR + "\"",
	        "",
			"----------\n" +
			"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" +
			"	Warn warn;\n" +
			"	^^^^\n" +
			"Discouraged access: The type Warn is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---\n" +
			"----------\n" +
			"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)\n" +
			"	KO ko;\n" +
			"	^^\n" +
			"Access restriction: The type KO is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---\n" +
			"----------\n" +
			"3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"3 problems (1 error, 2 warnings)",
	        true);
	}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88364 - skip options -O -Jxxx and -Xxxx, multiple times if needed
	public void test020(){
		this.runConformTest(
			new String[] {
					"X.java",
					"/** */\n" +
					"public class X {\n" +
					"}",
			},
	        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
	        + " -1.5 -g -preserveAllLocals"
	        + " -verbose -proceedOnError -referenceInfo"
	        + " -d \"" + OUTPUT_DIR + "\" -O -Xxxx -O -Jxyz -Xtyu -Jyu",
			"[parsing    ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]\n" +
			"[reading    java/lang/Object.class]\n" +
			"[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]\n" +
			"[writing    X.class - #1]\n" +
			"[completed  ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]\n" +
			"[1 unit compiled]\n" +
			"[1 .class file generated]\n",
	        "",
	        true);
	}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88364 - -sourcepath finds additional source files
	public void test021(){
		String setting= System.getProperty("jdt.compiler.useSingleThread");
		try {
			System.setProperty("jdt.compiler.useSingleThread", "true");
			this.runConformTest(
				new String[] {
						"src1/X.java",
						"/** */\n" +
						"public class X {\n" +
						"}",
						"src2/Y.java",
						"/** */\n" +
						"public class Y extends X {\n" +
						"}",
				},
		        "\"" + OUTPUT_DIR +  File.separator + "src2/Y.java\""
				+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src1\""
				  + File.pathSeparator + "\"" + OUTPUT_DIR +  File.separator + "src2\""
		        + " -1.5 -g -preserveAllLocals"
		        + " -verbose -proceedOnError -referenceInfo"
		        + " -d \"" + OUTPUT_DIR + "\" ",
				"[parsing    ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/1]\n" +
				"[parsing    ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]\n" +
				"[reading    java/lang/Object.class]\n" +
				"[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/2]\n" +
				"[writing    Y.class - #1]\n" +
				"[completed  ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/2]\n" +
				"[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]\n" +
				"[writing    X.class - #2]\n" +
				"[completed  ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]\n" +
				"[2 units compiled]\n" +
				"[2 .class files generated]\n",
		        "",
		        true);
		} finally {
			System.setProperty("jdt.compiler.useSingleThread", setting == null ? "false" : setting);
		}
	}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88364 - repeated -sourcepath fails - even if the error is more
// explicit here than what javac does
	public void test022_repeated_sourcepath(){
		this.runNegativeTest(
			new String[] {
					"src1/X.java",
					"/** */\n" +
					"public class X {\n" +
					"}",
					"src2/Y.java",
					"/** */\n" +
					"public class Y extends X {\n" +
					"}",
			},
			" -sourcepath \"" + OUTPUT_DIR +  File.separator + "src1\""
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src2\""
	        + " \"" + OUTPUT_DIR +  File.separator + "src2/Y.java\""
	        + " -1.5 -g -preserveAllLocals"
	        + " -verbose -proceedOnError -referenceInfo"
	        + " -d \"" + OUTPUT_DIR + "\" ",
	        "",
	        "duplicate sourcepath specification: -sourcepath ---OUTPUT_DIR_PLACEHOLDER---/src2\n",
	        true);
	}
//	 https://bugs.eclipse.org/bugs/show_bug.cgi?id=88364 - repeated -extdirs fails
	public void test023(){
		this.runNegativeTest(
			new String[] {
					"src1/X.java",
					"/** */\n" +
					"public class X {\n" +
					"}",
					"src2/Y.java",
					"/** */\n" +
					"public class Y extends X {\n" +
					"}",
			},
			" -extdirs \"" + OUTPUT_DIR +  File.separator + "src1\""
			+ " -extdirs \"" + OUTPUT_DIR +  File.separator + "src2\""
	        + " \"" + OUTPUT_DIR +  File.separator + "src1" + File.separator + "X.java\""
	        + " -1.5 -g -preserveAllLocals"
	        + " -verbose -proceedOnError -referenceInfo"
	        + " -d \"" + OUTPUT_DIR + "\" ",
	        "",
	        "duplicate extdirs specification: -extdirs ---OUTPUT_DIR_PLACEHOLDER---/src2\n",
	        true);
	}
//	 https://bugs.eclipse.org/bugs/show_bug.cgi?id=88364 - explicit empty -extdirs removes extensions
	public void test024(){
		if (!System.getProperty("java.vm.vendor").equals("Sun Microsystems Inc.")) return;
		/* this tests is using Sun vm layout. The type sun.net.spi.nameservice.dns.DNSNameService
		 * is located in the ext dir.
		 */
		this.runNegativeTest(
				new String[] {
						"X.java",
						"/** */\n" +
						"public class X {\n" +
						"  sun.net.spi.nameservice.dns.DNSNameService dummy;\n" +
						"}",
				},
				"\"" + OUTPUT_DIR +  File.separator + "X.java\""
				+ " -extdirs \"\""
				+ " -1.5 -g -preserveAllLocals"
				+ " -proceedOnError -referenceInfo"
				+ " -d \"" + OUTPUT_DIR + "\" ",
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" +
				"	sun.net.spi.nameservice.dns.DNSNameService dummy;\n" +
				"	^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"sun.net.spi.nameservice.dns cannot be resolved to a type\n" +
				"----------\n" +
				"1 problem (1 error)",
				true);
	}
//	 https://bugs.eclipse.org/bugs/show_bug.cgi?id=88364 - cumulative -extdirs extends the classpath
	public void test025(){
		String path = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		String libPath = null;
		if (path.endsWith(File.separator)) {
			libPath = path + "lib.jar";
		} else {
			libPath = path + File.separator + "lib.jar";
		}
		boolean jarCreated = false;
		try {
			Util.createJar(new String[] {
					"my/pkg/Zork.java",
					"package my.pkg;\n" +
					"public class Zork {\n" +
					"}",
				},
				libPath,
				JavaCore.VERSION_1_4);
			jarCreated = true;
		} catch (IOException e) {
			// ignore
		}
		String setting= System.getProperty("jdt.compiler.useSingleThread");
		try {
			System.setProperty("jdt.compiler.useSingleThread", "true");
			this.runConformTest(
				new String[] {
						"src1/X.java",
						"/** */\n" +
						"public class X {\n" +
						"  my.pkg.Zork dummy;\n" +
						"}",
						"src2/Y.java",
						"/** */\n" +
						"public class Y extends X {\n" +
						"}",
				},
		        "\"" + OUTPUT_DIR +  File.separator + "src2/Y.java\""
				+ " -extdirs \"" + path + File.pathSeparator + OUTPUT_DIR +  File.separator + "src1\""
				+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src1\""
		        + " -1.5 -g -preserveAllLocals"
		        + " -verbose -proceedOnError -referenceInfo"
		        + " -d \"" + OUTPUT_DIR + "\" ",
		        "[parsing    ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/1]\n" +
		        "[parsing    ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]\n" +
		        "[reading    java/lang/Object.class]\n" +
		        "[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/2]\n" +
		        "[writing    Y.class - #1]\n" +
		        "[completed  ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/2]\n" +
		        "[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]\n" +
		        "[reading    my/pkg/Zork.class]\n" +
		        "[writing    X.class - #2]\n" +
		        "[completed  ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]\n" +
		        "[2 units compiled]\n" +
		        "[2 .class files generated]\n",
		        "",
		        true);
		} finally {
			System.setProperty("jdt.compiler.useSingleThread", setting == null ? "false" : setting);
		}
		if (jarCreated) {
			Util.delete(libPath);
		}
	}
//	 https://bugs.eclipse.org/bugs/show_bug.cgi?id=88364 - -extdirs extends the classpath before -classpath
	public void test026(){
		String setting= System.getProperty("jdt.compiler.useSingleThread");
		try {
			System.setProperty("jdt.compiler.useSingleThread", "true");
			this.runConformTest(
				new String[] {
						"src1/X.java",
						"/** */\n" +
						"public class X {\n" +
						"}",
						"src2/Y.java",
						"/** */\n" +
						"public class Y extends X {\n" +
						"}",
						"src3/X.java",
						"/** */\n" +
						"public class X {\n" +
						"  Zork error;\n" +
						"}",
				},
		        "\"" + OUTPUT_DIR +  File.separator + "src2/Y.java\""
				+ " -classpath \"" + OUTPUT_DIR +  File.separator + "src3\""
				+ " -extdirs \"" + getExtDirectory() + File.pathSeparator + OUTPUT_DIR +  File.separator + "src1\""
				+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src2" + File.pathSeparator + OUTPUT_DIR +  File.separator + "src1\""
		        + " -1.5 -g -preserveAllLocals"
		        + " -verbose -proceedOnError -referenceInfo"
		        + " -d \"" + OUTPUT_DIR + "\" ",
				"[parsing    ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/1]\n" +
				"[parsing    ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]\n" +
				"[reading    java/lang/Object.class]\n" +
				"[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/2]\n" +
				"[writing    Y.class - #1]\n" +
				"[completed  ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/2]\n" +
				"[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]\n" +
				"[writing    X.class - #2]\n" +
				"[completed  ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]\n" +
				"[2 units compiled]\n" +
				"[2 .class files generated]\n",
				"",
		        true);
		} finally {
			System.setProperty("jdt.compiler.useSingleThread", setting == null ? "false" : setting);
		}
	}

public void test027(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"	OK1 ok1;\n" +
			"	OK2 ok2;\n" +
			"	Warn warn;\n" +
			"	KO ko;\n" +
	        "	Zork z;\n" +
			"}",
			"OK1.java",
			"/** */\n" +
			"public class OK1 {\n" +
			"	// empty\n" +
			"}",
			"OK2.java",
			"/** */\n" +
			"public class OK2 {\n" +
			"	// empty\n" +
			"}",
			"p1/Warn.java",
			"/** */\n" +
			"public class Warn {\n" +
			"	// empty\n" +
			"}",
			"KO.java",
			"/** */\n" +
			"public class KO {\n" +
			"	// empty\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[+OK2" + File.pathSeparator + "-KO]" + File.pathSeparator
        + OUTPUT_DIR + File.separator + "p1[~Warn]\""
        + " -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        // TODO (maxime) reintroduce the -verbose option to check the number of files
        //               generated, once able to avoid console echoing
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "----------\n" +
        "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" +
        "	Warn warn;\n" +
        "	^^^^\n" +
		"Discouraged access: The type Warn is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---/p1\n" +
		"----------\n" +
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)\n" +
		"	KO ko;\n" +
		"	^^\n" +
		"Access restriction: The type KO is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---\n" +
        "----------\n" +
        "3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)\n" +
        "	Zork z;\n" +
        "	^^^^\n" +
        "Zork cannot be resolved to a type\n" +
        "----------\n" +
        "3 problems (1 error, 2 warnings)",
        true);
}
public void test028(){
			this.runConformTest(
				new String[] {
					"src1/X.java",
					"/** */\n" +
					"public class X {\n" +
					"}",
				},
		        "\"" + OUTPUT_DIR +  File.separator + "src1/X.java\""
		        + " -1.5 -g -preserveAllLocals"
		        + " -proceedOnError -referenceInfo"
		        + " -d \"" + OUTPUT_DIR + File.separator + "bin/\"",
		        "",
		        "",
		        true);
			this.runConformTest(
				new String[] {
					"src2/Y.java",
					"/** */\n" +
					"public class Y extends X {\n" +
					"}",
				},
		        "\"" + OUTPUT_DIR +  File.separator + "src2/Y.java\""
		        + " -1.5 -g -preserveAllLocals"
		        + " -cp dummy" + File.pathSeparator + "\"" + OUTPUT_DIR + File.separator + "bin\"" + File.pathSeparator + "dummy"
		        + " -proceedOnError -referenceInfo"
		        + " -d \"" + OUTPUT_DIR + File.separator + "bin/\"",
		        "",
		        "incorrect classpath: dummy\n" +
		        "incorrect classpath: dummy\n",
		        false);
		}
//Extraneous auto-build error message - https://bugs.eclipse.org/bugs/show_bug.cgi?id=93377
public void test030(){
	// first series shows that a clean build is OK
	this.runConformTest(
		new String[] {
			"X.java",
			"public interface X<T extends X<T, K, S>, \n" +
			"                   K extends X.K<T, S>, \n" +
			"                   S extends X.S> {\n" +
			"	public interface K<KT extends X<KT, ?, KS>, \n" +
			"	                   KS extends X.S> {\n" +
			"	}\n" +
			"	public interface S {\n" +
			"	}\n" +
			"}\n",
			"Y.java",
			"public class Y<T extends X<T, K, S>, \n" +
			"               K extends X.K<T, S>, \n" +
			"               S extends X.S> { \n" +
			"}\n",
		},
        "\"" + OUTPUT_DIR +  File.separator + "Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + File.separator + "\""
        + " -proceedOnError -referenceInfo"
        + " -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
	// second series shows that a staged build - that simulates the auto build context - is OK as well
	this.runConformTest(
		new String[] {
			"X.java",
			"public interface X<T extends X<T, K, S>, \n" +
			"                   K extends X.K<T, S>, \n" +
			"                   S extends X.S> {\n" +
			"	public interface K<KT extends X<KT, ?, KS>, \n" +
			"	                   KS extends X.S> {\n" +
			"	}\n" +
			"	public interface S {\n" +
			"	}\n" +
			"}\n",
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -proceedOnError -referenceInfo"
        + " -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
	this.runConformTest(
		new String[] {
			"Y.java",
			"public class Y<T extends X<T, K, S>, \n" +
			"               K extends X.K<T, S>, \n" +
			"               S extends X.S> { \n" +
			"}\n",
		},
        "\"" + OUTPUT_DIR +  File.separator + "Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + File.separator + "\""
        + " -proceedOnError -referenceInfo"
        + " -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        false);
}
// Extraneous auto-build error message - https://bugs.eclipse.org/bugs/show_bug.cgi?id=93377
// More complex test case than test30
public void test032(){
	// first series shows that a clean build is OK (warning messages only)
	this.runConformTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"import java.io.Serializable;\n" +
				"public interface X<T extends X<T, U, V>, \n" +
				"				   U extends X.XX<T, V>, \n" +
				"				   V extends X.XY> {\n" +
				"	public interface XX<TT extends X<TT, ?, UU>, \n" +
				"	                    UU extends X.XY> \n" +
				"			extends	Serializable {\n" +
				"	}\n" +
				"	public interface XY extends Serializable {\n" +
				"	}\n" +
				"}\n",
				"p/Y.java",
				"package p;\n" +
				"import java.util.*;\n" +
				"import p.X.*;\n" +
				"public class Y<T extends X<T, U, V>, \n" +
				"               U extends X.XX<T, V>, \n" +
				"               V extends X.XY> {\n" +
				"	private final Map<U, V> m1 = new HashMap<U, V>();\n" +
				"	private final Map<U, T> m2 = new HashMap<U, T>();\n" +
				"	private final Z m3;\n" +
				"\n" +
				"	public Y(final Z p1) {\n" +
				"		this.m3 = p1;\n" +
				"	}\n" +
				"\n" +
				"	public void foo1(final U p1, final V p2, final T p3) {\n" +
				"		m1.put(p1, p2);\n" +
				"		m2.put(p1, p3);\n" +
				"		m3.foo2(p1, p2);\n" +
				"	}\n" +
				"\n" +
				"	public void foo3(final U p1) {\n" +
				"		assert m1.containsKey(p1);\n" +
				"		m1.remove(p1);\n" +
				"		m2.remove(p1);\n" +
				"		m3.foo2(p1, null);\n" +
				"	}\n" +
				"\n" +
				"	public Collection<T> foo4() {\n" +
				"		return Collections.unmodifiableCollection(m2.values());\n" +
				"	}\n" +
				"\n" +
				"	public void foo5(final Map<XX<?, ?>, XY> p1) {\n" +
				"		p1.putAll(m1);\n" +
				"	}\n" +
				"\n" +
				"	public void foo6(final Map<XX<?, ?>, XY> p1) {\n" +
				"		m1.keySet().retainAll(p1.keySet());\n" +
				"		m2.keySet().retainAll(p1.keySet());\n" +
				"	}\n" +
				"}\n",
				"p/Z.java",
				"package p;\n" +
				"\n" +
				"import java.util.*;\n" +
				"\n" +
				"import p.X.*;\n" +
				"\n" +
				"public class Z {\n" +
				"	private final Map<Class<? extends X>, \n" +
				"		              Y<?, ? extends XX<?, ?>, ? extends XY>> \n" +
				"		m1 = new HashMap<Class<? extends X>, \n" +
				"		                 Y<?, ? extends XX<?, ?>, ? extends XY>>();\n" +
				"\n" +
				"	private Map<X.XX<?, XY>, \n" +
				"	            X.XY> \n" +
				"		m2 = new HashMap<X.XX<?, XY>, \n" +
				"		                 X.XY>();\n" +
				"\n" +
				"	public <T extends X<T, U, V>, \n" +
				"	        U extends X.XX<T, V>, \n" +
				"	        V extends X.XY> \n" +
				"	Y<T, U, V> foo1(final Class<T> p1) {\n" +
				"		Y l1 = m1.get(p1);\n" +
				"		if (l1 == null) {\n" +
				"			l1 = new Y<T, U, V>(this);\n" +
				"			m1.put(p1, l1);\n" +
				"		}\n" +
				"		return l1;\n" +
				"	}\n" +
				"\n" +
				"	public <TT extends X.XX<?, UU>, \n" +
				"	        UU extends X.XY> \n" +
				"	void foo2(final TT p1, final UU p2) {\n" +
				"		m2.put((XX<?, XY>) p1, p2);\n" +
				"	}\n" +
				"\n" +
				"	public Map<XX<?, ?>, XY> foo3() {\n" +
				"		final Map<XX<?, ?>, \n" +
				"		          XY> l1 = new HashMap<XX<?, ?>, \n" +
				"		                               XY>();\n" +
				"		for (final Y<?, \n" +
				"				     ? extends XX<?, ?>, \n" +
				"				     ? extends XY> \n" +
				"				i : m1.values()) {\n" +
				"			i.foo5(l1);\n" +
				"		}\n" +
				"		return l1;\n" +
				"	}\n" +
				"\n" +
				"	public void foo4(final Object p1, final Map<XX<?, ?>, \n" +
				"			                                    XY> p2) {\n" +
				"		for (final Y<?, \n" +
				"				     ? extends XX<?, ?>, \n" +
				"				     ? extends XY> i : m1.values()) {\n" +
				"			i.foo6(p2);\n" +
				"		}\n" +
				"		for (final Map.Entry<XX<?, ?>, \n" +
				"				             XY> i : p2.entrySet()) {\n" +
				"			final XX<?, XY> l1 = (XX<?, XY>) i.getKey();\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
			},
	        "\"" + OUTPUT_DIR +  File.separator + "p/X.java\""
	        + " \"" + OUTPUT_DIR +  File.separator + "p/Y.java\""
	        + " \"" + OUTPUT_DIR +  File.separator + "p/Z.java\""
	        + " -1.5 -g -preserveAllLocals"
	        + " -cp \"" + OUTPUT_DIR + File.separator + "\""
	        + " -proceedOnError -referenceInfo"
	        + " -d \"" + OUTPUT_DIR + "\"",
	        "",
	        "----------\n" +
	        "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 8)\n" +
	        "	private final Map<Class<? extends X>, \n" +
	        "	                                  ^\n" +
	        "X is a raw type. References to generic type X<T,U,V> should be parameterized\n" +
	        "----------\n" +
	        "2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 10)\n" +
	        "	m1 = new HashMap<Class<? extends X>, \n" +
	        "	                                 ^\n" +
	        "X is a raw type. References to generic type X<T,U,V> should be parameterized\n" +
	        "----------\n" +
	        "3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 22)\n" +
	        "	Y l1 = m1.get(p1);\n" +
	        "	^\n" +
	        "Y is a raw type. References to generic type Y<T,U,V> should be parameterized\n" +
	        "----------\n" +
	        "4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 25)\n" +
	        "	m1.put(p1, l1);\n" +
	        "	           ^^\n" +
	        "Type safety: The expression of type Y needs unchecked conversion to conform to Y<?,? extends X.XX<?,?>,? extends X.XY>\n" +
	        "----------\n" +
	        "5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 27)\n" +
	        "	return l1;\n" +
	        "	       ^^\n" +
	        "Type safety: The expression of type Y needs unchecked conversion to conform to Y<T,U,V>\n" +
	        "----------\n" +
	        "6. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 33)\n" +
	        "	m2.put((XX<?, XY>) p1, p2);\n" +
	        "	       ^^^^^^^^^^^^^^\n" +
	        "Type safety: Unchecked cast from TT to X.XX<?,X.XY>\n" +
	        "----------\n" +
	        "7. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 58)\n" +
	        "	final XX<?, XY> l1 = (XX<?, XY>) i.getKey();\n" +
	        "	                ^^\n" +
	        "The local variable l1 is never read\n" +
	        "----------\n" +
	        "8. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 58)\n" +
	        "	final XX<?, XY> l1 = (XX<?, XY>) i.getKey();\n" +
	        "	                     ^^^^^^^^^^^^^^^^^^^^^^\n" +
	        "Type safety: Unchecked cast from X.XX<capture#22-of ?,capture#23-of ?> to X.XX<?,X.XY>\n" +
	        "----------\n" +
	        "8 problems (8 warnings)",
	        true);
	// second series shows that a staged build - that simulates the auto build context - is OK as well
	this.runConformTest(
		new String[] {
			"p/X.java",
			"package p;\n" +
			"import java.io.Serializable;\n" +
			"public interface X<T extends X<T, U, V>, \n" +
			"				   U extends X.XX<T, V>, \n" +
			"				   V extends X.XY> {\n" +
			"	public interface XX<TT extends X<TT, ?, UU>, \n" +
			"	                    UU extends X.XY> \n" +
			"			extends	Serializable {\n" +
			"	}\n" +
			"	public interface XY extends Serializable {\n" +
			"	}\n" +
			"}\n",
		},
        "\"" + OUTPUT_DIR +  File.separator + "p/X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -proceedOnError -referenceInfo"
        + " -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
	this.runConformTest(
		new String[] {
			"p/Y.java",
			"package p;\n" +
			"import java.util.*;\n" +
			"import p.X.*;\n" +
			"public class Y<T extends X<T, U, V>, \n" +
			"               U extends X.XX<T, V>, \n" +
			"               V extends X.XY> {\n" +
			"	private final Map<U, V> m1 = new HashMap<U, V>();\n" +
			"	private final Map<U, T> m2 = new HashMap<U, T>();\n" +
			"	private final Z m3;\n" +
			"\n" +
			"	public Y(final Z p1) {\n" +
			"		this.m3 = p1;\n" +
			"	}\n" +
			"\n" +
			"	public void foo1(final U p1, final V p2, final T p3) {\n" +
			"		m1.put(p1, p2);\n" +
			"		m2.put(p1, p3);\n" +
			"		m3.foo2(p1, p2);\n" +
			"	}\n" +
			"\n" +
			"	public void foo3(final U p1) {\n" +
			"		assert m1.containsKey(p1);\n" +
			"		m1.remove(p1);\n" +
			"		m2.remove(p1);\n" +
			"		m3.foo2(p1, null);\n" +
			"	}\n" +
			"\n" +
			"	public Collection<T> foo4() {\n" +
			"		return Collections.unmodifiableCollection(m2.values());\n" +
			"	}\n" +
			"\n" +
			"	public void foo5(final Map<XX<?, ?>, XY> p1) {\n" +
			"		p1.putAll(m1);\n" +
			"	}\n" +
			"\n" +
			"	public void foo6(final Map<XX<?, ?>, XY> p1) {\n" +
			"		m1.keySet().retainAll(p1.keySet());\n" +
			"		m2.keySet().retainAll(p1.keySet());\n" +
			"	}\n" +
			"}\n",
			"p/Z.java",
			"package p;\n" +
			"\n" +
			"import java.util.*;\n" +
			"\n" +
			"import p.X.*;\n" +
			"\n" +
			"public class Z {\n" +
			"	private final Map<Class<? extends X>, \n" +
			"		              Y<?, ? extends XX<?, ?>, ? extends XY>> \n" +
			"		m1 = new HashMap<Class<? extends X>, \n" +
			"		                 Y<?, ? extends XX<?, ?>, ? extends XY>>();\n" +
			"\n" +
			"	private Map<X.XX<?, XY>, \n" +
			"	            X.XY> \n" +
			"		m2 = new HashMap<X.XX<?, XY>, \n" +
			"		                 X.XY>();\n" +
			"\n" +
			"	public <T extends X<T, U, V>, \n" +
			"	        U extends X.XX<T, V>, \n" +
			"	        V extends X.XY> \n" +
			"	Y<T, U, V> foo1(final Class<T> p1) {\n" +
			"		Y l1 = m1.get(p1);\n" +
			"		if (l1 == null) {\n" +
			"			l1 = new Y<T, U, V>(this);\n" +
			"			m1.put(p1, l1);\n" +
			"		}\n" +
			"		return l1;\n" +
			"	}\n" +
			"\n" +
			"	public <TT extends X.XX<?, UU>, \n" +
			"	        UU extends X.XY> \n" +
			"	void foo2(final TT p1, final UU p2) {\n" +
			"		m2.put((XX<?, XY>) p1, p2);\n" +
			"	}\n" +
			"\n" +
			"	public Map<XX<?, ?>, XY> foo3() {\n" +
			"		final Map<XX<?, ?>, \n" +
			"		          XY> l1 = new HashMap<XX<?, ?>, \n" +
			"		                               XY>();\n" +
			"		for (final Y<?, \n" +
			"				     ? extends XX<?, ?>, \n" +
			"				     ? extends XY> \n" +
			"				i : m1.values()) {\n" +
			"			i.foo5(l1);\n" +
			"		}\n" +
			"		return l1;\n" +
			"	}\n" +
			"\n" +
			"	public void foo4(final Object p1, final Map<XX<?, ?>, \n" +
			"			                                    XY> p2) {\n" +
			"		for (final Y<?, \n" +
			"				     ? extends XX<?, ?>, \n" +
			"				     ? extends XY> i : m1.values()) {\n" +
			"			i.foo6(p2);\n" +
			"		}\n" +
			"		for (final Map.Entry<XX<?, ?>, \n" +
			"				             XY> i : p2.entrySet()) {\n" +
			"			final XX<?, XY> l1 = (XX<?, XY>) i.getKey();\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
        "\"" + OUTPUT_DIR +  File.separator + "p/Y.java\""
        + " \"" + OUTPUT_DIR +  File.separator + "p/Z.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + File.separator + "\""
        + " -proceedOnError -referenceInfo"
        + " -d \"" + OUTPUT_DIR + "\"",
        "",
        "----------\n" +
        "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 8)\n" +
        "	private final Map<Class<? extends X>, \n" +
        "	                                  ^\n" +
        "X is a raw type. References to generic type X<T,U,V> should be parameterized\n" +
        "----------\n" +
        "2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 10)\n" +
        "	m1 = new HashMap<Class<? extends X>, \n" +
        "	                                 ^\n" +
        "X is a raw type. References to generic type X<T,U,V> should be parameterized\n" +
        "----------\n" +
        "3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 22)\n" +
        "	Y l1 = m1.get(p1);\n" +
        "	^\n" +
        "Y is a raw type. References to generic type Y<T,U,V> should be parameterized\n" +
        "----------\n" +
        "4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 25)\n" +
        "	m1.put(p1, l1);\n" +
        "	           ^^\n" +
        "Type safety: The expression of type Y needs unchecked conversion to conform to Y<?,? extends X.XX<?,?>,? extends X.XY>\n" +
        "----------\n" +
        "5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 27)\n" +
        "	return l1;\n" +
        "	       ^^\n" +
        "Type safety: The expression of type Y needs unchecked conversion to conform to Y<T,U,V>\n" +
        "----------\n" +
        "6. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 33)\n" +
        "	m2.put((XX<?, XY>) p1, p2);\n" +
        "	       ^^^^^^^^^^^^^^\n" +
        "Type safety: Unchecked cast from TT to X.XX<?,X.XY>\n" +
        "----------\n" +
        "7. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 58)\n" +
        "	final XX<?, XY> l1 = (XX<?, XY>) i.getKey();\n" +
        "	                ^^\n" +
        "The local variable l1 is never read\n" +
        "----------\n" +
        "8. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 58)\n" +
        "	final XX<?, XY> l1 = (XX<?, XY>) i.getKey();\n" +
        "	                     ^^^^^^^^^^^^^^^^^^^^^^\n" +
        "Type safety: Unchecked cast from X.XX<capture#22-of ?,capture#23-of ?> to X.XX<?,X.XY>\n" +
        "----------\n" +
        "8 problems (8 warnings)",
        false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=104664
public void test033(){
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR
        + "\"" + File.pathSeparator
        + " -repeat 2 -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "[repetition 1/2]\n" +
        "[repetition 2/2]\n",
        "",
        true);
}
public void test034(){
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp " + File.pathSeparator + "\"" + OUTPUT_DIR
        + "\"" + File.pathSeparator
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
}
// check classpath value
public void test035(){
	final String javaClassspath = System.getProperty("java.class.path");
	final String javaUserDir = System.getProperty("user.dir");
	try {
		System.setProperty("user.dir", OUTPUT_DIR);
		this.runConformTest(
			new String[] {
				"p/Y.java",
				"public class Y { public static final String S = \"\"; }",
			},
	        "\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "Y.java\""
	        + " -1.5 -g -preserveAllLocals -proceedOnError -referenceInfo ",
	        "",
	        "",
	        true);
		System.setProperty("java.class.path", "");
		this.runConformTest(
				new String[] {
					"X.java",
					"import p.Y;\n" +
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		System.out.print(Y.S);\n" +
					"	}\n" +
					"}",
				},
		        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		        + " -1.5 -g -preserveAllLocals -proceedOnError -referenceInfo ",
		        "",// this is not the runtime output
		        "no classpath defined, using default directory instead\n",
		        false);
		final String userDir = System.getProperty("user.dir");
		File f = new File(userDir, "X.java");
		if (!Util.delete(f)) {
			System.out.println("Could not delete X");
		}
		f = new File(userDir, "p" + File.separator + "Y.java");
		if (!Util.delete(f)) {
			System.out.println("Could not delete Y");
		}

	} finally {
		System.setProperty("java.class.path", javaClassspath);
		System.setProperty("user.dir", javaUserDir);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=119108
// \ in call to AccessRulesSet.getViolatedRestriction
public void test036(){
	this.runConformTest(
		new String[] {
			"src1/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"}",
		},
        "\"" + OUTPUT_DIR + "/src1/p/X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -proceedOnError -referenceInfo"
        + " -d \"" + OUTPUT_DIR + "/bin1/\"",
        "",
        "",
        true);
	this.runConformTest(
		new String[] {
			"src2/Y.java",
			"/** */\n" +
			"public class Y extends p.X {\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "src2/Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + File.separator + "bin1[~**/X]\""
        + " -proceedOnError -referenceInfo"
        + " -d \"" + OUTPUT_DIR + File.separator + "bin2/\"",
        "",
        "----------\n" +
        "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java (at line 2)\n" +
        "	public class Y extends p.X {\n" +
        "	                       ^^^\n" +
        "Discouraged access: The type X is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---/bin1\n" +
        "----------\n" +
        "1 problem (1 warning)",
        false);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=53773
// complain on assignment to parameters
public void test037() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(int i, final int j) {\n" +
			"    i =  0; // warning\n" +
			"    j =  0; // error\n" +
			"  }\n" +
			"}\n"},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.5 "
		+ " -cp \"" + OUTPUT_DIR + "\""
		+ " -warn:+paramAssign"
		+ " -proceedOnError"
		+ " -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" +
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" +
		"	i =  0; // warning\n" +
		"	^\n" +
		"The parameter i should not be assigned\n" +
		"----------\n" +
		"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" +
		"	j =  0; // error\n" +
		"	^\n" +
		"The final local variable j cannot be assigned. It must be blank and not using a compound assignment\n" +
		"----------\n" +
		"2 problems (1 error, 1 warning)",
		true);
}

// Missing access restriction violation error on generic type.
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=122995
// Binary case.
public void test039(){
	this.runConformTest(
		new String[] {
			"src1/p/X.java",
			"package p;\n" +
			"public class X<T> {\n" +
			"	T m;\n" +
			"}",
		},
        "\"" + OUTPUT_DIR + "/src1/p/X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -proceedOnError -referenceInfo"
        + " -d \"" + OUTPUT_DIR + "/bin1/\"",
        "",
        "",
        true);
	this.runConformTest(
		new String[] {
			"src2/Y.java",
			"package p;\n" +
			"public class Y {\n" +
			"	X x1;\n" +
			"	X<String> x2 = new X<String>();\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "src2/Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + File.separator + "bin1[~**/X]\""
        + " -proceedOnError -referenceInfo"
        + " -d \"" + OUTPUT_DIR + File.separator + "bin2/\"",
        "",
        "----------\n" +
        "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java (at line 3)\n" +
        "	X x1;\n" +
        "	^\n" +
        "Discouraged access: The type X<T> is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---/bin1\n" +
        "----------\n" +
        "2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java (at line 3)\n" +
        "	X x1;\n" +
        "	^\n" +
        "X is a raw type. References to generic type X<T> should be parameterized\n" +
        "----------\n" +
        "3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java (at line 4)\n" +
        "	X<String> x2 = new X<String>();\n" +
        "	^\n" +
        "Discouraged access: The type X<String> is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---/bin1\n" +
        "----------\n" +
        "4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java (at line 4)\n" +
        "	X<String> x2 = new X<String>();\n" +
        "	               ^^^^^^^^^^^^^^^\n" +
        "Discouraged access: The constructor X<String>() is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---/bin1\n" +
        "----------\n" +
        "5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java (at line 4)\n" +
        "	X<String> x2 = new X<String>();\n" +
        "	                   ^\n" +
        "Discouraged access: The type X<String> is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---/bin1\n" +
        "----------\n" +
        "5 problems (5 warnings)",
        false);
}

// check we get appropriate combination of access rules
public void test040(){
	this.runConformTest(
		new String[] {
			"p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"}",
			"p/Z.java",
			"package p;\n" +
			"/** */\n" +
			"public class Z {\n" +
			"}"
		},
        "\"" + OUTPUT_DIR +  File.separator + "p"  +  File.separator + "X.java\""
        + " \"" + OUTPUT_DIR +  File.separator + "p"  +  File.separator + "Z.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -nowarn"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
	this.runConformTest(
		new String[] {
			"Y.java",
			"/** */\n" +
			"public class Y {\n" +
			"  p.X x;\n" +
			"  p.Z z;\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[+p/X" + File.pathSeparator + "-p/*]\""
        + " -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
		"----------\n" +
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/Y.java (at line 4)\n" +
		"	p.Z z;\n" +
		"	^^^\n" +
		"Access restriction: The type Z is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---\n" +
		"----------\n" +
		"1 problem (1 warning)",
        false);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=124533
// turn off discouraged references warnings
public void test041(){
	this.runConformTest(
		new String[] {
			"p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"}",
			"p/Z.java",
			"package p;\n" +
			"/** */\n" +
			"public class Z {\n" +
			"}"
		},
        "\"" + OUTPUT_DIR +  File.separator + "p"  +  File.separator + "X.java\""
        + " \"" + OUTPUT_DIR +  File.separator + "p"  +  File.separator + "Z.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -nowarn"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
	this.runConformTest(
		new String[] {
			"Y.java",
			"/** */\n" +
			"public class Y {\n" +
			"  p.X x;\n" +
			"  p.Z z;\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[~p/X" + File.pathSeparator + "-p/*]\""
        + " -warn:-discouraged -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
		"----------\n" +
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/Y.java (at line 4)\n" +
		"	p.Z z;\n" +
		"	^^^\n" +
		"Access restriction: The type Z is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---\n" +
		"----------\n" +
		"1 problem (1 warning)",
        false);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=124533
// turn off forbidden references warnings
public void test042(){
	this.runConformTest(
		new String[] {
			"p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"}",
			"p/Z.java",
			"package p;\n" +
			"/** */\n" +
			"public class Z {\n" +
			"}"
		},
        "\"" + OUTPUT_DIR +  File.separator + "p"  +  File.separator + "X.java\""
        + " \"" + OUTPUT_DIR +  File.separator + "p"  +  File.separator + "Z.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -nowarn"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
	this.runConformTest(
		new String[] {
			"Y.java",
			"/** */\n" +
			"public class Y {\n" +
			"  p.X x;\n" +
			"  p.Z z;\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[~p/X" + File.pathSeparator + "-p/*]\""
        + " -warn:-forbidden -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
		"----------\n" +
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/Y.java (at line 3)\n" +
		"	p.X x;\n" +
		"	^^^\n" +
		"Discouraged access: The type X is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---\n" +
		"----------\n" +
		"1 problem (1 warning)",
        false);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=124533
// turn off discouraged and forbidden references warnings
public void test043(){
	this.runConformTest(
		new String[] {
			"p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"}",
			"p/Z.java",
			"package p;\n" +
			"/** */\n" +
			"public class Z {\n" +
			"}"
		},
        "\"" + OUTPUT_DIR +  File.separator + "p"  +  File.separator + "X.java\""
        + " \"" + OUTPUT_DIR +  File.separator + "p"  +  File.separator + "Z.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -nowarn"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
	this.runConformTest(
		new String[] {
			"Y.java",
			"/** */\n" +
			"public class Y {\n" +
			"  p.X x;\n" +
			"  p.Z z;\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[~p/X" + File.pathSeparator + "-p/*]\""
        + " -warn:-discouraged,forbidden -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
		"",
        false);
}

// null ref option
public void test044(){
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}"},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -bootclasspath " + getLibraryClassesAsQuotedString()
        + " -cp " + getJCEJarAsQuotedString()
        + " -warn:+null"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "----------\n" +
        "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" +
        "	o.toString();\n" +
        "	^\n" +
        "Null pointer access: The variable o can only be null at this location\n" +
        "----------\n" +
        "1 problem (1 warning)",
        true);
}

// null ref option
public void test045(){
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}"},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -bootclasspath " + getLibraryClassesAsQuotedString()
        + " -cp " + getJCEJarAsQuotedString()
        + " -warn:-null" // contrast with test036
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "", true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=114456
// turn off discouraged and forbidden references warnings using SuppressWarnings all
public void test046(){
	this.runConformTest(
		new String[] {
			"p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "p"  +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -nowarn"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
	this.runConformTest(
		new String[] {
			"Y.java",
			"/** */\n" +
			"@SuppressWarnings(\"all\")\n" +
			"public class Y {\n" +
			"  p.X x;\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[~p/X" + File.pathSeparator + "-p/*]\""
        + " -warn:+discouraged,forbidden,deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
		"",
        false);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=114456
// turn off discouraged and forbidden references warnings using SuppressWarnings restriction
public void test047(){
	this.runConformTest(
		new String[] {
			"p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "p"  +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -nowarn"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
	this.runConformTest(
		new String[] {
			"Y.java",
			"/** */\n" +
			"@SuppressWarnings(\"restriction\")\n" +
			"public class Y {\n" +
			"  p.X x;\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[~p/X" + File.pathSeparator + "-p/*]\""
        + " -warn:+discouraged,forbidden,deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
		"",
        false);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=114456
// turn off discouraged and forbidden references warnings using SuppressWarnings
public void test048(){
	this.runConformTest(
		new String[] {
			"p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "p"  +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -nowarn"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
	this.runConformTest(
		new String[] {
			"Y.java",
			"/** */\n" +
			"@SuppressWarnings(\"deprecation\")\n" +
			"public class Y {\n" +
			"  p.X x;\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[~p/X" + File.pathSeparator + "-p/*]\""
        + " -warn:+discouraged,forbidden,deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "----------\n" + 
        "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/Y.java (at line 2)\n" + 
        "	@SuppressWarnings(\"deprecation\")\n" + 
        "	                  ^^^^^^^^^^^^^\n" + 
        "Unnecessary @SuppressWarnings(\"deprecation\")\n" + 
        "----------\n" + 
        "2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/Y.java (at line 4)\n" + 
        "	p.X x;\n" + 
        "	^^^\n" + 
        "Discouraged access: The type X is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---\n" + 
        "----------\n" + 
        "2 problems (2 warnings)",
        false);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// disable warning on command line (implicit)
public void test049(){
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"    public void test(int p) {\n" +
			"        switch (p) {\n" +
			"        case 0:\n" +
			"            System.out.println(0);\n" +
			"        case 1:\n" +
			"            System.out.println(1); // possible fall-through\n" +
			"        }\n" +
			"    }\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -nowarn"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// disable warning on command line (explicit)
public void test050(){
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"    public void test(int p) {\n" +
			"        switch (p) {\n" +
			"        case 0:\n" +
			"            System.out.println(0);\n" +
			"        case 1:\n" +
			"            System.out.println(1); // possible fall-through\n" +
			"        }\n" +
			"    }\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -warn:-fallthrough"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// enable warning on command line
public void test051(){
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"    public void test(int p) {\n" +
			"        switch (p) {\n" +
			"        case 0:\n" +
			"            System.out.println(0);\n" +
			"        case 1:\n" +
			"            System.out.println(1); // complain: possible fall-through\n" +
			"        }\n" +
			"    }\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -warn:+fallthrough"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
		"----------\n" +
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)\n" +
		"	case 1:\n" +
		"	^^^^^^\n" +
		"Switch case may be entered by falling through previous case\n" +
		"----------\n" +
		"1 problem (1 warning)",
        true);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=123476
public void test052(){
	try {
		new File(OUTPUT_DIR).mkdirs();
		File barFile = new File(OUTPUT_DIR +  File.separator + "Bar.java");
		FileOutputStream barOutput = new FileOutputStream(barFile);
		try {
			String barContents =
				"public class Bar	\n" +
				"{	\n" +
				"  Bar(int class)	\n" +
				"  {	\n" +
				"  }	\n" +
				"}\n";
			barOutput.write(barContents.getBytes());
		} finally {
			barOutput.close();
		}
	} catch(IOException e) {
		// do nothing, will fail below
	}

	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X\n" +
			"{\n" +
			"  static Object x()\n" +
			"  {\n" +
			"    return new Bar(5);\n" +
			"  }\n" +
			"}\n",
		},
     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
     + " -cp \"" + OUTPUT_DIR + File.pathSeparator + "\""
     + " -d \"" + OUTPUT_DIR + "\"",
     "",
     "----------\n" +
     "1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" +
     "	return new Bar(5);\n" +
     "	       ^^^^^^^^^^\n" +
     "The constructor Bar(int) is undefined\n" +
     "----------\n" +
     "----------\n" +
     "2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/Bar.java (at line 2)\n" +
     "	{	\n" +
     "	^\n" +
     "Syntax error, insert \"}\" to complete ClassBody\n" +
     "----------\n" +
     "3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/Bar.java (at line 3)\n" +
     "	Bar(int class)	\n" +
     "	        ^^^^^\n" +
     "Syntax error on token \"class\", invalid VariableDeclaratorId\n" +
     "----------\n" +
     "4. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/Bar.java (at line 3)\n" +
     "	Bar(int class)	\n" +
     "  {	\n" +
     "  }	\n" +
     "	        ^^^^^^^^^^^^^^^^\n" +
     "Syntax error on tokens, delete these tokens\n" +
     "----------\n" +
     "5. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/Bar.java (at line 6)\n" +
     "	}\n" +
     "	^\n" +
     "Syntax error on token \"}\", delete this token\n" +
     "----------\n" +
     "5 problems (5 errors)",
     false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=137053
public void test053(){
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {}"
        },
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -d \"" + OUTPUT_DIR + File.separator + "X.java\"",
		"",
		"No .class file created for file X.class in ---OUTPUT_DIR_PLACEHOLDER" +
			"---/X.java because of an IOException: Regular file " +
			"---OUTPUT_DIR_PLACEHOLDER---/X.java cannot be used " +
			"as output directory\n",
		true);
}
// suggested by https://bugs.eclipse.org/bugs/show_bug.cgi?id=141522
// only checking messages (the bug itself involves concurrent access to
// the file system and a true test case would call for instrumented
// code)
public void test054(){
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {}",
			"f", // create simple file f
			""
        },
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -d \"" + OUTPUT_DIR + "/f/out\"",
		"",
		"No .class file created for file X.class in ---OUTPUT_DIR_PLACEHOLDER" +
			"---/f/out because of an IOException: " +
			"Could not create output directory ---OUTPUT_DIR_PLACEHOLDER---/f/out\n",
		true);
}
// suggested by https://bugs.eclipse.org/bugs/show_bug.cgi?id=141522
// only checking messages (the bug itself involves concurrent access to
// the file system and a true test case would call for instrumented
// code)
// this test only works on appropriate file systems
public void test055(){
	if (File.separatorChar == '/') {
	  	String tentativeOutputDirNameTail =
	      	File.separator + "out";
	  	File outputDirectory = new File(OUTPUT_DIR + tentativeOutputDirNameTail);
	  	outputDirectory.mkdirs();
	  	outputDirectory.setReadOnly();
	  	// read-only directories do not prevent file creation
	  	// on under-gifted file systems
		this.runConformTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {}",
	        },
	        "\"" + OUTPUT_DIR +  File.separator + "p/X.java\""
	        + " -1.5 -g -preserveAllLocals"
	        + " -d \"" + OUTPUT_DIR + "/out\"",
			"",
			"No .class file created for file p/X.class in " +
				"---OUTPUT_DIR_PLACEHOLDER---/out because of " +
				"an IOException: Could not create subdirectory p into output directory " +
				"---OUTPUT_DIR_PLACEHOLDER---/out\n",
			false /* do not flush output directory */);
	}
}
// suggested by https://bugs.eclipse.org/bugs/show_bug.cgi?id=141522
// only checking messages (the bug itself involves concurrent access to
// the file system and a true test case would call for instrumented
// code)
public void test056(){
  	String tentativeOutputDirNameTail =
      	File.separator + "out";
	this.runConformTest(
		new String[] {
			"p/X.java",
			"package p;\n" +
			"public class X {}",
			"out/p", // create simple file out/p
			""
        },
        "\"" + OUTPUT_DIR +  File.separator + "p/X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -d \"" + OUTPUT_DIR + tentativeOutputDirNameTail + "\"",
		"",
		"No .class file created for file p/X.class in " +
			"---OUTPUT_DIR_PLACEHOLDER---/out" +
			" because of an IOException: Regular file ---OUTPUT_DIR_PLACEHOLDER---" +
			"/out/p cannot be used as output directory\n",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=147461
// the compilation is successful because we do not check the classpath entries
// given in the rules; accordingly OK<sep>-KO is seen as a directory that is
// added to positive rules, and the compilation completes normally
public void test057_access_restrictions_separator(){
	String oppositeSeparator = File.pathSeparatorChar == ':' ?
			";" : ":";
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"	OK1 ok1;\n" +
			"	OK2 ok2;\n" +
			"	KO ko;\n" +
			"}",
			"OK1.java",
			"/** */\n" +
			"public class OK1 {\n" +
			"	// empty\n" +
			"}",
			"OK2.java",
			"/** */\n" +
			"public class OK2 {\n" +
			"	// empty\n" +
			"}",
			"KO.java",
			"/** */\n" +
			"public class KO {\n" +
			"	// empty\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[+OK2" + oppositeSeparator + "-KO]\""
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
}

// .java ending directory name
// as a sibling of the compiled file
public void test058(){
  	File outputDirectory = new File(OUTPUT_DIR + File.separator + "foo.java");
  	outputDirectory.mkdirs();
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {}",
        },
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -d \"" + OUTPUT_DIR + "/out\"",
		"",
		"",
		false /* do not flush output directory */);
}
// .java ending directory name
// subdirectory of a compiled directory, unreferenced
public void test060(){
	File outputDirectory = new File(OUTPUT_DIR + File.separator + "foo.java");
	outputDirectory.mkdirs();
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {}",
        },
	    "\"" + OUTPUT_DIR + "\""
	    + " -1.5 -g -preserveAllLocals"
	    + " -d \"" + OUTPUT_DIR + "/out\"",
		"",
		"",
		false /* do not flush output directory */);
}

// .java ending directory name
// subdirectory of a compiled directory, referenced
public void test061(){
	File outputDirectory = new File(OUTPUT_DIR + File.separator + "foo.java");
	outputDirectory.mkdirs();
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"  foo m;\n" +
				"}",
	        },
	    "\"" + OUTPUT_DIR + "\""
	    + " -1.5 -g -preserveAllLocals"
	    + " -d \"" + OUTPUT_DIR + "/out\"",
		"",
		"----------\n" +
		"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)\n" +
		"	foo m;\n" +
		"	^^^\n" +
		"foo cannot be resolved to a type\n" +
		"----------\n" +
		"1 problem (1 error)",
		false /* do not flush output directory */);
}

// self-referential jar file
// variant using a relative path to the jar file in the -cp option
// this only tests that the fact the jar file references itself in a Class-Path
// clause does not break anything; the said clause is not needed for the
// compilation to succeed, and is merely irrelevant, but other compilers have
// shown a bug in that area
// TODO (maxime) improve management of current working directory
// we have a problem here in that the working directory is set once and for all
// from above, and we cannot change it afterwards (more on that when jdk7 gets
// ready); moreover, the default working directory may not the best choice we
// could expect (it is within the workspace, endangering the test project
// layout); this would need to be reconsidered from scratch, keeping in mind
// that some disk regions are not available for writing when running the releng
// tests; the pity is that this is precisely the case that fails with other
// compilers, whereas test063 passes;
// this problem affects other batch compiler tests as well, since we must be
// able to simulate command lines that would include relative paths; the
// solution probably encompasses putting the effective working directory under
// control;
public void _test062(){
	String outputDirName = OUTPUT_DIR + File.separator + "d",
	  metaInfDirName = outputDirName + File.separator + "META-INF",
	  jarFileName = outputDirName + File.separator + "L.jar";
//	  currentWorkingDirectory = System.getProperty("user.dir");
	this.runConformTest(
		new String[] {
			"d/Y.java",
			"public class Y {\n" +
			"}"},
	    "\"" + outputDirName + "\""
	    + " -1.5 -g -preserveAllLocals"
	    + " -d \"" + outputDirName + "\"",
		"",
		"",
		true /* flush output directory */);
	File outputDirectory = new File(outputDirName);
	File metaInfDirectory = new File(metaInfDirName);
	metaInfDirectory.mkdirs();
	try {
		Util.createFile(metaInfDirName + File.separator + "MANIFEST.MF",
			"Manifest-Version: 1.0\n" +
			"Class-Path: ../d/L.jar\n");
	} catch (IOException e) {
		fail("could not create manifest file");
	}
	try {
		Util.zip(outputDirectory, jarFileName);
	} catch (IOException e) {
		fail("could not create jar file");
	}
	Util.delete(outputDirName + File.separator + "Y.class");
	Util.delete(outputDirName + File.separator + "Y.java");
	this.runConformTest(
		new String[] {
			"d/X.java",
			"public class X {\n" +
			"  Y m;\n" +
			"}"},
	    "\"" + outputDirName + "\""
	    + " -1.5 -g -preserveAllLocals"
	    + " -cp L.jar"
	    + " -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		false /* do not flush output directory */);
}

// self-referential jar file
// variant using an absolute path to the jar file in the -cp option
public void test063(){
	String outputDirName = OUTPUT_DIR + File.separator + "d",
	  metaInfDirName = outputDirName + File.separator + "META-INF",
	  jarFileName = outputDirName + File.separator + "L.jar";
	this.runConformTest(
		new String[] {
			"d/Y.java",
			"public class Y {\n" +
			"}"},
	    "\"" + outputDirName + "\""
	    + " -1.5 -g -preserveAllLocals"
	    + " -d \"" + outputDirName + "\"",
		"",
		"",
		true /* flush output directory */);
	File outputDirectory = new File(outputDirName);
	File metaInfDirectory = new File(metaInfDirName);
	metaInfDirectory.mkdirs();
	try {
		Util.createFile(metaInfDirName + File.separator + "MANIFEST.MF",
			"Manifest-Version: 1.0\n" +
			"Class-Path: ../d/L.jar\n");
	} catch (IOException e) {
		fail("could not create manifest file");
	}
	try {
		Util.zip(outputDirectory, jarFileName);
	} catch (IOException e) {
		fail("could not create jar file");
	}
	Util.delete(outputDirName + File.separator + "Y.class");
	Util.delete(outputDirName + File.separator + "Y.java");
	this.runConformTest(
		new String[] {
			"d/X.java",
			"public class X {\n" +
			"  Y m;\n" +
			"}"},
	    "\"" + outputDirName + "\""
	    + " -1.5 -g -preserveAllLocals"
	    + " -cp \"" + jarFileName + "\""
	    + " -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		false /* do not flush output directory */);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=155809
// per sourcepath directory default encoding
public void _test064_per_sourcepath_directory_default_encoding(){
	String source1 = "src1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + File.separator + source1
        + "[UTF-8]\"",
		"",
		"",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// pre-existing case 1: using a single, definite output directory
public void test065_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + " -d \"" + OUTPUT_DIR + File.separator + output1 + "\"",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// pre-existing case 2: using no definite output directory
public void test066_per_source_output_directory(){
	String source1 = "src1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + File.separator + source1 + "\"",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// pre-existing case 3: -d none absorbs output
public void test067_per_source_output_directory(){
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"X.java\""
        + " -1.5"
        + " -d none",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 1: overriding the default output directory for one of the sources
// -sourcepath series
public void test068_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1", output2 = "bin2";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -d \"" + OUTPUT_DIR + File.separator + output2 + "\"",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + output2 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + output2 +
			File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 2: specifying an output directory for a given source directory only
// -sourcepath series
public void test069_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 3: [-d none] selectively absorbs output
// -sourcepath series
public void test070_per_source_output_directory(){
	String source1 = "src1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d none]",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 4: overriding -d none for one of the sources
// -sourcepath series
public void test071_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -d none",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// [-d dir][rule] is forbidden
// -sourcepath series
public void test072_per_source_output_directory(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"}"},
        "\"" + OUTPUT_DIR +  File.separator +
        	"X.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + "\"" + "[-d dir][~**/internal/*]",
		"",
		"access rules cannot follow destination path entries: ---OUTPUT_DIR_PLACEHOLDER---[-d dir][~**/internal/*]\n",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// [rule][-d dir] is ok
// -sourcepath series
public void test073_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + File.separator + source1 + "\"" +
        	"[-**/*][-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]",
		"",
		"----------\n" +
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/Z.java (at line 2)\n" +
		"	X f;\n" +
		"	^\n" +
		"Access restriction: The type X is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---/src1\n" +
		"----------\n" +
		"1 problem (1 warning)",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 1: overriding the default output directory for one of the sources
// -classpath series
public void test074_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1", output2 = "bin2";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -classpath \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -d \"" + OUTPUT_DIR + File.separator + output2 + "\"",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + output2 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + output2 +
			File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 2: specifying an output directory for a given source directory only
// -classpath series
public void test075_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -cp \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 3: [-d none] selectively absorbs output
// -classpath series
public void test076_per_source_output_directory(){
	String source1 = "src1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -classpath \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d none]",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 4: overriding -d none for one of the sources
// -classpath series
public void test077_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -classpath \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -d none",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// [-d dir][rule] is forbidden
// -classpath series
public void test078_per_source_output_directory(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"}"},
        "\"" + OUTPUT_DIR +  File.separator +
        	"X.java\""
        + " -1.5"
        + " -classpath \"" + OUTPUT_DIR + "\"" + "[-d dir][~**/internal/*]",
		"",
		"access rules cannot follow destination path entries: ---OUTPUT_DIR_PLACEHOLDER---[-d dir][~**/internal/*]\n",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// [rule][-d dir] is ok
// -classpath series
public void test079_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -classpath \"" + OUTPUT_DIR + File.separator + source1 + "\"" +
        	"[-**/*][-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]",
		"",
		"----------\n" +
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/Z.java (at line 2)\n" +
		"	X f;\n" +
		"	^\n" +
		"Access restriction: The type X is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---/src1\n" +
		"----------\n" +
		"1 problem (1 warning)",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 1: overriding the default output directory for one of the sources
// -bootclasspath series
public void test080_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1", output2 = "bin2";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -bootclasspath " + getLibraryClassesAsQuotedString() + File.pathSeparator + "\"" +
          OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -d \"" + OUTPUT_DIR + File.separator + output2 + "\"",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + output2 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + output2 +
			File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 2: specifying an output directory for a given source directory only
// -bootclasspath series
public void test081_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -bootclasspath " + getLibraryClassesAsQuotedString() + File.pathSeparator + "\"" +
          OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 3: [-d none] selectively absorbs output
// -bootclasspath series
public void test082_per_source_output_directory(){
	String source1 = "src1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -bootclasspath " + getLibraryClassesAsQuotedString() + File.pathSeparator + "\"" +
          OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d none]",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 4: overriding -d none for one of the sources
// -bootclasspath series
public void test083_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -bootclasspath " + getLibraryClassesAsQuotedString() + File.pathSeparator + "\"" +
          OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -d none",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + File.separator + "Z.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// [-d dir][rule] is forbidden
// -bootclasspath series
public void test084_per_source_output_directory(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"}"},
        "\"" + OUTPUT_DIR +  File.separator +
        	"X.java\""
        + " -1.5"
        + " -bootclasspath \"" + OUTPUT_DIR + "\"" + "[-d dir][~**/internal/*]",
		"",
		"access rules cannot follow destination path entries: ---OUTPUT_DIR_PLACEHOLDER---[-d dir][~**/internal/*]\n",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// [rule][-d dir] is ok
// -bootclasspath series
public void test085_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -bootclasspath " + getLibraryClassesAsQuotedString() + File.pathSeparator +
        	"\"" + OUTPUT_DIR + File.separator + source1 + "\"" +
        	"[-**/*][-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]",
		"",
		"----------\n" +
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/Z.java (at line 2)\n" +
		"	X f;\n" +
		"	^\n" +
		"Access restriction: The type X is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---/src1\n" +
		"----------\n" +
		"1 problem (1 warning)",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// jar / zip files in sourcepath
public void test086_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1", output2 = "bin2";
	File outputDir = new File(OUTPUT_DIR),
		sourceDir = new File(OUTPUT_DIR + File.separator + source1);
	try {
		if (outputDir.exists()) {
			Util.flushDirectoryContent(outputDir);
		} else {
			outputDir.mkdirs();
		}
		sourceDir.mkdir();
		Util.createFile(OUTPUT_DIR + File.separator +
			source1 + File.separator + "X.java",
			"public class X {}");
		Util.zip(sourceDir,	OUTPUT_DIR + File.separator + "X.jar");
	} catch (IOException e) {
		fail("could not create jar file");
	}
	this.runConformTest(
		new String[] {
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + File.separator + "X.jar\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -d \"" + OUTPUT_DIR + File.separator + output2 + "\"",
		"",
		"",
		false); // keep jar
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + output2 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + output2 +
			File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// jar / zip files in classpath are binaries only: no -d argument
public void test087_per_source_output_directory(){
	String output1 = "bin1", output2 = "bin2";
	this.runNegativeTest(
		new String[] {
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -classpath \"" + OUTPUT_DIR + File.separator + "X.jar\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -d \"" + OUTPUT_DIR + File.separator + output2 + "\"",
		"",
		"unexpected destination path entry for file: ---OUTPUT_DIR_PLACEHOLDER---/X.jar\n",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// jar / zip files in bootclasspath are binaries only: no -d argument
public void test088_per_source_output_directory(){
	String output1 = "bin1", output2 = "bin2";
	this.runNegativeTest(
		new String[] {
			"Z.java",
			"public class Z {\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -bootclasspath \"" + OUTPUT_DIR + File.separator + "X.jar\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -d \"" + OUTPUT_DIR + File.separator + output2 + "\"",
		"",
		"unexpected destination path entry for file: ---OUTPUT_DIR_PLACEHOLDER---/X.jar\n",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 2: specifying an output directory for a given source directory only
// jar / zip files in sourcepath
public void test089_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	File outputDir = new File(OUTPUT_DIR),
		sourceDir = new File(OUTPUT_DIR + File.separator + source1),
		standardXOutputFile = new File(System.getProperty("user.dir") +
			File.separator + "X.class");
	try {
		if (outputDir.exists()) {
			Util.flushDirectoryContent(outputDir);
		} else {
			outputDir.mkdirs();
		}
		sourceDir.mkdir();
		Util.createFile(OUTPUT_DIR + File.separator +
			source1 + File.separator + "X.java",
			"public class X {}");
		Util.zip(sourceDir,	OUTPUT_DIR + File.separator + "X.jar");
		if (standardXOutputFile.exists()) {
			Util.delete(standardXOutputFile);
		}
	} catch (IOException e) {
		fail("could not create jar file");
	}
	this.runConformTest(
		new String[] {
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + File.separator + "X.jar\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        ,
		"",
		"",
		false);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	assertFalse("extraneous file: " + standardXOutputFile.getPath(),
		standardXOutputFile.exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 3: [-d none] selectively absorbs output
// jar / zip files
public void test090_per_source_output_directory(){
	String source1 = "src1";
	File outputDir = new File(OUTPUT_DIR),
		sourceDir = new File(OUTPUT_DIR + File.separator + source1),
		standardXOutputFile = new File(System.getProperty("user.dir") +
			File.separator + "X.class");
	try {
		if (outputDir.exists()) {
			Util.flushDirectoryContent(outputDir);
		} else {
			outputDir.mkdirs();
		}
		sourceDir.mkdir();
		Util.createFile(OUTPUT_DIR + File.separator +
			source1 + File.separator + "X.java",
			"public class X {}");
		Util.zip(sourceDir,	OUTPUT_DIR + File.separator + "X.jar");
		if (standardXOutputFile.exists()) {
			Util.delete(standardXOutputFile);
		}
	} catch (IOException e) {
		fail("could not create jar file");
	}
	this.runConformTest(
		new String[] {
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + File.separator + "X.jar\""
        + "[-d none]",
		"",
		"",
		false);
	String fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	assertFalse("extraneous file: " + standardXOutputFile.getPath(),
		standardXOutputFile.exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 4: overriding -d none for one of the sources
// jar / zip files
public void test091_per_source_output_directory(){
	String source1 = "src1", output1 = "bin1";
	File outputDir = new File(OUTPUT_DIR),
		sourceDir = new File(OUTPUT_DIR + File.separator + source1),
		standardXOutputFile = new File(System.getProperty("user.dir") +
			File.separator + "X.class");
	try {
		if (outputDir.exists()) {
			Util.flushDirectoryContent(outputDir);
		} else {
			outputDir.mkdirs();
		}
		sourceDir.mkdir();
		Util.createFile(OUTPUT_DIR + File.separator +
			source1 + File.separator + "X.java",
			"public class X {}");
		Util.zip(sourceDir,	OUTPUT_DIR + File.separator + "X.jar");
		if (standardXOutputFile.exists()) {
			Util.delete(standardXOutputFile);
		}
	} catch (IOException e) {
		fail("could not create jar file");
	}
	this.runConformTest(
		new String[] {
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -d none",
		"",
		"",
		false);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 1: overriding the default output directory for one of the sources
// source directories series
public void test092_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1", output2 = "bin2";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -1.5"
        + " -d \"" + OUTPUT_DIR + File.separator + output2 + "\"",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + output2 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + output2 +
			File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 2: specifying an output directory for a given source directory only
// source directories series
public void test093_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -1.5",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 3: [-d none] selectively absorbs output
// source directories series
public void test094_per_source_output_directory(){
	String source1 = "src1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"public class Z {\n" +
			"  // X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d none]"
        + " -1.5",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 3: [-d none] selectively absorbs output
// source directories series
// variant: swap entries
public void test095_per_source_output_directory(){
	String source1 = "src1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"public class Z {\n" +
			"  // X f;\n" +
			"}",
        },
        " \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d none] "
        + "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 4: overriding -d none for one of the sources
// source directories series
public void test096_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -1.5"
        + " -d none",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 4: overriding -d none for one of the sources
// source directories series
// variant: two source folders
public void test097_per_source_output_directory(){
	String source1 = "src1", source2 = "src2",
		output1 = "bin1", output2 = "bin2";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			source2 + File.separator + "Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        " \"" + OUTPUT_DIR + File.separator + source2 + "\""
        + " \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -1.5"
        + " -d none",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + output2 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source2 +
			File.separator + "Z.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// [rule] is forbidden for source directories
public void test098_per_source_output_directory(){
	String source1 = "src1";
	this.runNegativeTest(
		new String[] {
			source1 + File.separator + "X.java",
			"public class X {\n" +
			"  Zork z;\n" +
			"}"},
        "\"" + OUTPUT_DIR +  File.separator + source1 + "\""
        + "[~**/internal/*]"
        + " -1.5",
		"",
		"unsupported encoding format: ~**/internal/*\n",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// changing the coding of -d none option
public void test099_per_source_output_directory() {
	File none = new File(Main.NONE);
	if (none.exists()) {
		fail("unexpected file: " + none.getAbsolutePath() +
				"; please cleanup the test environment");
		// by design, we do not want to agressively destroy a directory that
		// could well exist outside of our dedicated output area
		// TODO (maxime) one more case that calls for a better management of the
		//               current working directory in our batch compiler tests
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"X.java\""
        + " -1.5"
        + " -d none",
		"",
		"",
		true);
	String fileName = Main.NONE + File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// -extdirs cannot receive a -d option
public void test100_per_source_output_directory(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"}"},
        "\"" + OUTPUT_DIR +  File.separator +
        	"X.java\""
        + " -1.5"
        + " -extdirs \"" + OUTPUT_DIR + "\"" + "[-d dir]",
		"",
		"unexpected destination path entry in -extdir option\n",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// -endorseddirs cannot receive a -d option
public void test101_per_source_output_directory(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"}"},
        "\"" + OUTPUT_DIR +  File.separator +
        	"X.java\""
        + " -1.5"
        + " -endorseddirs \"" + OUTPUT_DIR + "\"" + "[-d dir]",
		"",
		"unexpected destination path entry in -endorseddirs option\n",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// bad syntax
public void test102_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runNegativeTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
        },
        " \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\""
        + " -1.5"
        + " -d none",
		"",
		"incorrect destination path entry: [-d ---OUTPUT_DIR_PLACEHOLDER---/bin1\n",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// bad syntax
public void test103_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runNegativeTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
        },
        " \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + " [-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -1.5",
		"",
		"unexpected bracket: [-d\n",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// bad syntax
public void test104_per_source_output_directory(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"}"},
        "\"" + OUTPUT_DIR +  File.separator +
        	"X.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + "\"" + "[[-d dir]",
		"",
		"unexpected bracket: ---OUTPUT_DIR_PLACEHOLDER---[[-d\n",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// bad syntax
public void test105_per_source_output_directory(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"}"},
        "\"" + OUTPUT_DIR +  File.separator +
        	"X.java\""
        + " -1.5"
        + " -cp \"" + OUTPUT_DIR + "\"" + "[-d dir]]",
		"",
		"unexpected bracket: dir]]\n",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// bad syntax
public void test106_per_source_output_directory(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"}"},
        "\"" + OUTPUT_DIR +  File.separator +
        	"X.java\""
        + " -1.5"
        + " -cp \"" + OUTPUT_DIR + "\"" + "[-d dir1" + File.pathSeparator +
        	"dir2]",
		"",
		"incorrect destination path entry: ---OUTPUT_DIR_PLACEHOLDER---" +
			"[-d dir1" + File.pathSeparator + "dir2]\n",
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
// source 1.3 compliance 1.3
public void test107() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"}",
		},
     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
     + " -1.3 -source 1.3 -d \"" + OUTPUT_DIR + "\"",
     "",
     "",
     true);
	String expectedOutput = "// Compiled from X.java (version 1.1 : 45.3, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.4 source 1.3
public void test108() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.4 -source 1.3 -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.2 : 46.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.4 source 1.4
public void test109() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.4 -source 1.4 -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.4 : 48.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.5 source 1.3
public void test110() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.5 -source 1.3 -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.4 : 48.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.5 source 1.4
public void test111() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.5 -source 1.4 -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.4 : 48.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.5 source 1.5
public void test112() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.5 -source 1.5 -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.5 : 49.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.6 source 1.3
public void test113() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.6 -source 1.3 -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.4 : 48.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.6 source 1.4
public void test114() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.6 -source 1.4 -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.4 : 48.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.6 source 1.5
public void test115() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.6 -source 1.5 -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.6 : 50.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.6 source 1.6
public void test116() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.6 -source 1.6 -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.6 : 50.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.7 source 1.3
public void test117() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.7 -source 1.3 -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.4 : 48.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.7 source 1.4
public void test118() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.7 -source 1.4 -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.4 : 48.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.7 source 1.5
public void test119() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.7 -source 1.5 -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.6 : 50.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.7 source 1.6
public void test120() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.7 -source 1.6 -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.6 : 50.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.7 source 1.7
// TODO part of the changes for 206483
public void test121() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.7 -source 1.7 -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.6 : 50.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
// command line - unusual classpath (ends with ';;;', still OK)
public void test122_classpath(){
	runClasspathTest(
		OUTPUT_DIR + "[+**/OK2]" + File.pathSeparator + File.pathSeparator +
				File.pathSeparator,
		new String[] {
			OUTPUT_DIR,	"{pattern=**/OK2 (ACCESSIBLE)}", null,
		},
		null);
}
// command line - unusual classpath (rules with multiple path separators KO, but
// without any error message though)
public void test123_classpath(){
	String cp = OUTPUT_DIR + "[+OK2" + File.pathSeparator + File.pathSeparator +
			File.pathSeparator + "~Warn" + File.pathSeparator + "-KO]";
	runClasspathTest(
		cp,
		null,
		null);
}
// command line - unusual classpath (rules with embedded -d OK)
public void test124_classpath (){
	runClasspathTest(
		OUTPUT_DIR + "[+OK2" + File.pathSeparator +	"-d ~Warn" +
				File.pathSeparator + "-KO]",
		new String[] {
			OUTPUT_DIR,
				"{pattern=OK2 (ACCESSIBLE), pattern=d ~Warn (NON ACCESSIBLE), pattern=KO (NON ACCESSIBLE)}",
				null,
		},
		null);
}
// command line - unusual classpath (rules starting with -d KO)
public void test125_classpath() {
	String cp = OUTPUT_DIR + "[-d +OK2" + File.pathSeparator + "~Warn" +
			File.pathSeparator + "-KO]";
	runClasspathTest(
		cp,
		null,
		"incorrect destination path entry: " + cp);
}
// command line - unusual classpath (rules starting with -d KO)
public void test126_classpath() {
	String cp = OUTPUT_DIR + "[-d +OK2" + File.pathSeparator + "~Warn" +
			File.pathSeparator + "-KO][-d dummy]";
	runClasspathTest(
		cp,
		null,
		"incorrect destination path entry: " + cp);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=161996
public void test127_classpath() {
	String jarFile = OUTPUT_DIR + File.separator + "[squarebracket].jar";
	runClasspathTest(
		jarFile,
		new String[] {
			jarFile, null, null,
		},
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=161996
public void test128_classpath() {
	String jarFile = OUTPUT_DIR + File.separator + "[square][bracket].jar";
	runClasspathTest(
		jarFile,
		new String[] {
			jarFile, null, null,
		},
		null);
}
// command line - classpath order
public void test129_classpath() {
	runClasspathTest(
		"file.jar[+A]" + File.pathSeparator + OUTPUT_DIR,
		new String[] {
			"file.jar",	"{pattern=A (ACCESSIBLE)}", null,
			OUTPUT_DIR, null, null,
		},
		null);
}
// command line - output directories
// see also test072
public void test130_classpath() {
	String cp = OUTPUT_DIR + "[-d dir][~**/internal/*]";
	runClasspathTest(
		cp,
		null,
		"access rules cannot follow destination path entries: " + cp);
}
// command line - output directories
public void test131_classpath() {
	String cp = OUTPUT_DIR + "[~**/internal/*][-d dir]";
	runClasspathTest(
		cp,
		new String[] {
			OUTPUT_DIR,	"{pattern=**/internal/* (DISCOURAGED)}", "dir",
		},
		null);
}
// command line - brackets in classpath
// unbalanced brackets fail (without any message though)
public void test132_classpath() {
	String cp = OUTPUT_DIR + "[~**/internal/*[-d dir]";
	runClasspathTest(
		cp,
		null,
		null);
}
// command line - brackets in classpath
// unbalanced brackets fail (without any message though)
public void test133_classpath() {
	String cp = OUTPUT_DIR + "[~**/internal/*]-d dir]";
	runClasspathTest(
		cp,
		null,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=161996
public void test134_classpath() {
	String jarFile = OUTPUT_DIR + File.separator + "[squarebracket].jar";
	runClasspathTest(
		jarFile + "[~**/internal/*][-d " + OUTPUT_DIR + "]",
		new String[] {
			jarFile, "{pattern=**/internal/* (DISCOURAGED)}", OUTPUT_DIR,
		},
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=161996
public void test135_classpath() {
	String jarFile = OUTPUT_DIR + File.separator + "[square][bracket].jar";
	runClasspathTest(
		jarFile + "[~**/internal/*][-d dir]",
		new String[] {
			jarFile, "{pattern=**/internal/* (DISCOURAGED)}", "dir",
		},
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=161996
public void test136_classpath() {
	String target = OUTPUT_DIR + File.separator + "[a]";
	(new File(target)).mkdirs();
	runClasspathTest(
		target + File.separator,
		new String[] {
			target, null, null,
		},
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=161996
public void test137_classpath() {
	String target = OUTPUT_DIR + File.separator + "[a]";
	(new File(target)).mkdirs();
	runClasspathTest(
		target + File.separator + "[~**/internal/*][-d dir]",
		new String[] {
			target, "{pattern=**/internal/* (DISCOURAGED)}", "dir",
		},
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=161996
// too many brackets series KO (no error though)
public void test138_classpath() {
	runClasspathTest(
		OUTPUT_DIR + File.separator + "[a][~**/internal/*][-d dir]",
		null,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=173416
// start with a bracket
public void test139_classpath() {
    String cp = "[a].jar";
    runClasspathTest(
        cp,
        new String [] {
            cp, null, null,
        },
        null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=173416
// start with a bracket
public void test140_classpath() {
    String cp = "[a].jar";
    runClasspathTest(
        cp + "[~**/internal/*][-d dir]",
        new String [] {
            cp, "{pattern=**/internal/* (DISCOURAGED)}", "dir",
        },
        null);
}
// null ref option
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170704
// adding distinct options to control null checks in more detail
public void test141_null_ref_option(){
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}"},
     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
     + " -1.5 -g -preserveAllLocals"
     + " -bootclasspath " + getLibraryClassesAsQuotedString()
     + " -cp " + getJCEJarAsQuotedString()
     + " -warn:+nullDereference"
     + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
     "",
     "----------\n" +
     "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" +
     "	o.toString();\n" +
     "	^\n" +
     "Null pointer access: The variable o can only be null at this location\n" +
     "----------\n" +
     "1 problem (1 warning)",
     true);
}
// null ref option
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170704
// adding distinct options to control null checks in more detail
public void test142_null_ref_option(){
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    if (o == null) {}\n" +
			"  }\n" +
			"}"},
  "\"" + OUTPUT_DIR +  File.separator + "X.java\""
  + " -1.5 -g -preserveAllLocals"
  + " -bootclasspath " + getLibraryClassesAsQuotedString()
  + " -cp " + getJCEJarAsQuotedString()
  + " -warn:+null"
  + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
  "",
  "----------\n" +
  "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" +
  "	if (o == null) {}\n" +
  "	    ^\n" +
  "Redundant null check: The variable o can only be null at this location\n" +
  "----------\n" +
  "1 problem (1 warning)",
  true);
}
// null ref option
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170704
// adding distinct options to control null checks in more detail
public void test143_null_ref_option(){
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    if (o == null) {}\n" +
			"  }\n" +
			"}"},
"\"" + OUTPUT_DIR +  File.separator + "X.java\""
+ " -1.5 -g -preserveAllLocals"
+ " -bootclasspath " + getLibraryClassesAsQuotedString()
+ " -cp " + getJCEJarAsQuotedString()
+ " -warn:+nullDereference"
+ " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
"",
"",
true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=190493
public void test144() throws Exception {
	String version = System.getProperty("java.class.version");
	if ("49.0".equals(version)) {
		this.runConformTest(
			new String[] {
				"X.java",
				"/** */\n" +
				"public class X {\n" +
				"}",
			},
			"\"" + OUTPUT_DIR +  File.separator + "X.java\""
			+ " -1.6 -source 1.6 -d \"" + OUTPUT_DIR + "\"",
			"",
			"Annotation processing got disabled, since it requires a 1.6 compliant JVM\n",
			true);
		String expectedOutput = "// Compiled from X.java (version 1.6 : 50.0, super bit)";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
}
// reporting unnecessary declaration of thrown checked exceptions
// default is off
public void test145_declared_thrown_checked_exceptions(){
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" + 
			"public class X {\n" + 
			"  public void foo() throws IOException {\n" + 
			"  }\n" + 
			"}\n"},
  "\"" + OUTPUT_DIR +  File.separator + "X.java\""
  + " -1.5 -g -preserveAllLocals"
  + " -bootclasspath " + getLibraryClassesAsQuotedString()
  + " -cp " + getJCEJarAsQuotedString()
  + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
  "",
  "",
  true);
}
// reporting unnecessary declaration of thrown checked exceptions
public void test146_declared_thrown_checked_exceptions(){
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" + 
			"public class X {\n" + 
			"  public void foo() throws IOException {\n" + 
			"  }\n" + 
			"}\n"},
  "\"" + OUTPUT_DIR +  File.separator + "X.java\""
  + " -1.5 -g -preserveAllLocals"
  + " -bootclasspath " + getLibraryClassesAsQuotedString()
  + " -cp " + getJCEJarAsQuotedString()
  + " -warn:+unusedThrown"
  + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
  "",
  "----------\n" + 
  "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
  "	public void foo() throws IOException {\n" + 
  "	                         ^^^^^^^^^^^\n" + 
  "The declared exception IOException is not actually thrown by the method foo() from type X\n" + 
  "----------\n" + 
  "1 problem (1 warning)",
  true);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=122885
//coverage test
public void test148_access_restrictions(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"	KO ko;\n" +
			"   void foo() {\n" +
			"     ko = new KO();\n" +
			"     ko.bar();\n" +
			"     if (ko.m) {}\n" +
			"   }\n" +
			"   Zork z;\n" +
			"}",
			"KO.java",
			"/** */\n" +
			"public class KO {\n" +
			"  void bar() {};\n" +
			"  boolean m;\n" +
			"}",
		},
  "\"" + OUTPUT_DIR +  File.separator + "X.java\""
  + " -1.5 -g -preserveAllLocals"
  + " -cp \"" + OUTPUT_DIR + "[-KO]\""
  + " -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
  + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
  "",
  "----------\n" + 
  "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
  "	KO ko;\n" + 
  "	^^\n" + 
  "Access restriction: The type KO is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---\n" + 
  "----------\n" + 
  "2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" + 
  "	ko = new KO();\n" + 
  "	     ^^^^^^^^\n" + 
  "Access restriction: The constructor KO() is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---\n" + 
  "----------\n" + 
  "3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" + 
  "	ko = new KO();\n" + 
  "	         ^^\n" + 
  "Access restriction: The type KO is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---\n" + 
  "----------\n" + 
  "4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)\n" + 
  "	ko.bar();\n" + 
  "	^^^^^^^^\n" + 
  "Access restriction: The method bar() from the type KO is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---\n" + 
  "----------\n" + 
  "5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)\n" + 
  "	if (ko.m) {}\n" + 
  "	       ^\n" + 
  "Access restriction: The field m from the type KO is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---\n" + 
  "----------\n" + 
  "6. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 9)\n" + 
  "	Zork z;\n" + 
  "	^^^^\n" + 
  "Zork cannot be resolved to a type\n" + 
  "----------\n" + 
  "6 problems (1 error, 5 warnings)",
  true);
}
//http://bugs.eclipse.org/bugs/show_bug.cgi?id=168230
public void test149() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void foo() {}\n" + 
			"	public static void bar() {\n" + 
			"		X.<String>foo();\n" + 
			"	}\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.7 -warn:-unused -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// http://bugs.eclipse.org/bugs/show_bug.cgi?id=192875
// default in now on for nullDereference
public void test150_null_ref_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void foo() {\n" +
			"     String s = null;\n" +
			"     s.toString();\n" +
			"   }\n" + 
			"	// Zork z;\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	s.toString();\n" + 
		"	^\n" + 
		"Null pointer access: The variable s can only be null at this location\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// http://bugs.eclipse.org/bugs/show_bug.cgi?id=192875
// default in now on for nullDereference
public void test151_null_ref_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void foo() {\n" +
			"     String s = null;\n" +
			"     s.toString();\n" +
			"   }\n" + 
			"	// Zork z;\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:-nullDereference -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=138018
public void test152() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void foo() {\n" +
			"     String s = null;\n" +
			"     s.toString();\n" +
			"   }\n" + 
			"	// Zork z;\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:-nullDereferences -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"invalid warning: nullDereferences. Ignoring warning and compiling\n" + 
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	s.toString();\n" + 
		"	^\n" + 
		"Null pointer access: The variable s can only be null at this location\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// -warn option - regression tests
public void test153_warn_options() {
	// check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void foo() {\n" +
			"     String s = null;\n" +
			"     s.toString();\n" +
			"     String u;\n" +
			"   }\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -proc:none -d \"" + OUTPUT_DIR + "\"",
//		+ " -warn:none -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	s.toString();\n" + 
		"	^\n" + 
		"Null pointer access: The variable s can only be null at this location\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" + 
		"	String u;\n" + 
		"	       ^\n" + 
		"The local variable u is never read\n" + 
		"----------\n" + 
		"2 problems (2 warnings)",
		true);
	// observe -warn options variations
	this.runConformTest(
		new String[] { },
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:none -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		false);
}
// -warn option - regression tests
public void test154_warn_options() {
	// same source as 153, skip default checks
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void foo() {\n" +
			"     String s = null;\n" +
			"     s.toString();\n" +
			"     String u;\n" +
			"   }\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:null -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	s.toString();\n" + 
		"	^\n" + 
		"Null pointer access: The variable s can only be null at this location\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// -warn option - regression tests
public void test155_warn_options() {
	// same source as 153, skip default checks
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void foo() {\n" +
			"     String s = null;\n" +
			"     s.toString();\n" +
			"     String u;\n" +
			"   }\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:none -warn:null -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	s.toString();\n" + 
		"	^\n" + 
		"Null pointer access: The variable s can only be null at this location\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210518
// bad behavior for -warn:null -warn:unused
public void test156_warn_options() {
	// same source as 153, skip default checks
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void foo() {\n" +
			"     String s = null;\n" +
			"     s.toString();\n" +
			"     String u;\n" +
			"   }\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:null -warn:unused -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" + 
		"	String u;\n" + 
		"	       ^\n" + 
		"The local variable u is never read\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210518
// variant
public void test157_warn_options() {
	// same source as 153, skip default checks
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void foo() {\n" +
			"     String s = null;\n" +
			"     s.toString();\n" +
			"     String u;\n" +
			"   }\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:null -warn:+unused -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	s.toString();\n" + 
		"	^\n" + 
		"Null pointer access: The variable s can only be null at this location\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" + 
		"	String u;\n" + 
		"	       ^\n" + 
		"The local variable u is never read\n" + 
		"----------\n" + 
		"2 problems (2 warnings)",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210518
// variant
public void test158_warn_options() {
	// same source as 153, skip default checks
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void foo() {\n" +
			"     String s = null;\n" +
			"     s.toString();\n" +
			"     String u;\n" +
			"   }\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:null -warn:+unused -warn:-null -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" + 
		"	String u;\n" + 
		"	       ^\n" + 
		"The local variable u is never read\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// -warn option - regression tests
public void test159_warn_options() {
	// same source as 153, skip default checks
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void foo() {\n" +
			"     String s = null;\n" +
			"     s.toString();\n" +
			"     String u;\n" +
			"   }\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" + 
		"	String u;\n" + 
		"	       ^\n" + 
		"The local variable u is never read\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// -warn option - regression tests
public void test160_warn_options() {
	// same source as 153, skip default checks
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void foo() {\n" +
			"     String s = null;\n" +
			"     s.toString();\n" +
			"     String u;\n" +
			"   }\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:none -warn:+unused,null -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	s.toString();\n" + 
		"	^\n" + 
		"Null pointer access: The variable s can only be null at this location\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" + 
		"	String u;\n" + 
		"	       ^\n" + 
		"The local variable u is never read\n" + 
		"----------\n" + 
		"2 problems (2 warnings)",
		true);
}
// -warn option - regression tests
// this one is undocumented but makes some sense
public void test161_warn_options() {
	// same source as 153, skip default checks
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void foo() {\n" +
			"     String s = null;\n" +
			"     s.toString();\n" +
			"     String u;\n" +
			"   }\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:-null,unused,+unused,null -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	s.toString();\n" + 
		"	^\n" + 
		"Null pointer access: The variable s can only be null at this location\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" + 
		"	String u;\n" + 
		"	       ^\n" + 
		"The local variable u is never read\n" + 
		"----------\n" + 
		"2 problems (2 warnings)",
		true);
}
// -warn option - regression tests
// this one is undocumented but makes some sense
public void test162_warn_options() {
	// same source as 153, skip default checks
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void foo() {\n" +
			"     String s = null;\n" +
			"     s.toString();\n" +
			"     String u;\n" +
			"   }\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -nowarn -warn:+null,unused,-unused,null -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// -warn option - regression tests
public void test163_warn_options() {
	// check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  Y f;\n" +
			"}",
			"Y.java",
			"/** @deprecated */\n" +
			"public class Y {\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)\n" + 
		"	Y f;\n" + 
		"	^\n" + 
		"The type Y is deprecated\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
	// observe -warn options variations
	this.runConformTest(
		new String[] { },
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:none -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		false);
}
// -warn option - regression tests
public void test164_warn_options() {
	// same source as 163, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  Y f;\n" +
			"}",
			"Y.java",
			"/** @deprecated */\n" +
			"public class Y {\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -nowarn -deprecation -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)\n" + 
		"	Y f;\n" + 
		"	^\n" + 
		"The type Y is deprecated\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// -warn option - regression tests
public void test165_warn_options() {
	// same source as 163, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  Y f;\n" +
			"}",
			"Y.java",
			"/** @deprecated */\n" +
			"public class Y {\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -nowarn -deprecation -warn:-deprecation -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// -warn option - regression tests
public void test166_warn_options() {
	// same source as 163, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  Y f;\n" +
			"}",
			"Y.java",
			"/** @deprecated */\n" +
			"public class Y {\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -nowarn -deprecation -warn:-allDeprecation -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// -warn option - regression tests
public void test167_warn_options() {
	// same source as 163, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  Y f;\n" +
			"}",
			"Y.java",
			"/** @deprecated */\n" +
			"public class Y {\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:allDeprecation -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)\n" + 
		"	Y f;\n" + 
		"	^\n" + 
		"The type Y is deprecated\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// -warn option - regression tests
public void test168_warn_options() {
	// check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  /**\n" +
			"    @param\n" +
			"  */\n" +
			"  public void foo() {\n" +
			"  }\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	// observe -warn options variations
	this.runConformTest(
		new String[] { },
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:javadoc -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	@param\n" + 
		"	 ^^^^^\n" + 
		"Javadoc: Missing parameter name\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		false);
}
// -warn option - regression tests
public void test169_warn_options() {
	// same source as 168, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  /**\n" +
			"    @param\n" +
			"  */\n" +
			"  public void foo() {\n" +
			"  }\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:allJavadoc -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)\n" + 
		"	public class X {\n" + 
		"	             ^\n" + 
		"Javadoc: Missing comment for public declaration\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	@param\n" + 
		"	 ^^^^^\n" + 
		"Javadoc: Missing parameter name\n" + 
		"----------\n" + 
		"2 problems (2 warnings)",
		true);
}
// -warn option - regression tests
public void test170_warn_options() {
	// same source as 168, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  /**\n" +
			"    @param\n" +
			"  */\n" +
			"  public void foo() {\n" +
			"  }\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:javadoc -warn:-allJavadoc -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210521
// -warn option - regression tests
public void test171_warn_options() {
	// same source as 168, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  /**\n" +
			"    @param\n" +
			"  */\n" +
			"  public void foo() {\n" +
			"  }\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:allJavadoc -warn:-javadoc -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)\n" + 
		"	public class X {\n" + 
		"	             ^\n" + 
		"Javadoc: Missing comment for public declaration\n" + 
		"----------\n" +		
		"1 problem (1 warning)",
		true);
}
// -warn option - regression tests
public void test172_warn_options() {
	// check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  Y f;\n" +
			"  /** @deprecated */\n" +
			"  void foo(Y p) {\n" +
			"  }\n" +
			"}",
			"Y.java",
			"/** @deprecated */\n" +
			"public class Y {\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)\n" + 
		"	Y f;\n" + 
		"	^\n" + 
		"The type Y is deprecated\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
	// observe -warn options variations
	this.runConformTest(
		new String[] { },
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:allDeprecation -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)\n" + 
		"	Y f;\n" + 
		"	^\n" + 
		"The type Y is deprecated\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	void foo(Y p) {\n" + 
		"	         ^\n" + 
		"The type Y is deprecated\n" + 
		"----------\n" + 
		"2 problems (2 warnings)",
		false);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210524
// -warn option - regression tests
public void _test173_warn_options() {
	// same source as 172, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  Y f;\n" +
			"  /** @deprecated */\n" +
			"  void foo(Y p) {\n" +
			"  }\n" +
			"}",
			"Y.java",
			"/** @deprecated */\n" +
			"public class Y {\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:allDeprecation -warn:-deprecation -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	void foo(Y p) {\n" + 
		"	         ^\n" + 
		"The type Y is deprecated\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// -warn option - regression tests
public void test174_warn_options() {
	// check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  int i;\n" + 
			"  class XX {\n" + 
			"    int i;\n" + 
			"  }\n" + 
			"  void foo(int i) {\n" + 
			"    class XX {\n" + 
			"    }\n" + 
			"    if (i > 0) {\n" + 
			"      try {\n" + 
			"        bar();\n" + 
			"      } catch (E2 e2) {\n" + 
			"      } catch (E1 e1) {\n" + 
			"      }\n" + 
			"    }\n" + 
			"  }\n" + 
			"  void bar() throws E2 {\n" + 
			"    throw new E2();\n" + 
			"  }\n" + 
			"}\n" + 
			"class E1 extends Exception {\n" + 
			"  private static final long serialVersionUID = 1L;\n" + 
			"}\n" + 
			"class E2 extends E1 {\n" + 
			"  private static final long serialVersionUID = 1L;\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)\n" + 
		"	class XX {\n" + 
		"	      ^^\n" + 
		"The type XX is hiding the type X.XX\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)\n" + 
		"	class XX {\n" + 
		"	      ^^\n" + 
		"The type XX is never used locally\n" + 
		"----------\n" + 
		"3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 13)\n" + 
		"	} catch (E1 e1) {\n" + 
		"	         ^^\n" + 
		"Unreachable catch block for E1. Only more specific exceptions are thrown and handled by previous catch block(s).\n" + 
		"----------\n" + 
		"3 problems (3 warnings)",
		true);
	// observe -warn options variations
	this.runConformTest(
		new String[] { },
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -nowarn -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		false);
}
// -warn option - regression tests
public void test175_warn_options() {
	// same source as 174, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  int i;\n" + 
			"  class XX {\n" + 
			"    int i;\n" + 
			"  }\n" + 
			"  void foo(int i) {\n" + 
			"    class XX {\n" + 
			"    }\n" + 
			"    if (i > 0) {\n" + 
			"      try {\n" + 
			"        bar();\n" + 
			"      } catch (E2 e2) {\n" + 
			"      } catch (E1 e1) {\n" + 
			"      }\n" + 
			"    }\n" + 
			"  }\n" + 
			"  void bar() throws E2 {\n" + 
			"    throw new E2();\n" + 
			"  }\n" + 
			"}\n" + 
			"class E1 extends Exception {\n" + 
			"  private static final long serialVersionUID = 1L;\n" + 
			"}\n" + 
			"class E2 extends E1 {\n" + 
			"  private static final long serialVersionUID = 1L;\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:hiding -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	int i;\n" + 
		"	    ^\n" + 
		"The field X.XX.i is hiding a field from type X\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)\n" + 
		"	void foo(int i) {\n" + 
		"	             ^\n" + 
		"The parameter i is hiding a field from type X\n" + 
		"----------\n" + 
		"3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)\n" + 
		"	class XX {\n" + 
		"	      ^^\n" + 
		"The type XX is hiding the type X.XX\n" + 
		"----------\n" + 
		"4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 13)\n" + 
		"	} catch (E1 e1) {\n" + 
		"	         ^^\n" + 
		"Unreachable catch block for E1. Only more specific exceptions are thrown and handled by previous catch block(s).\n" + 
		"----------\n" + 
		"4 problems (4 warnings)",
		true);
}
// -warn option - regression tests
public void test176_warn_options() {
	// same source as 174, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  int i;\n" + 
			"  class XX {\n" + 
			"    int i;\n" + 
			"  }\n" + 
			"  void foo(int i) {\n" + 
			"    class XX {\n" + 
			"    }\n" + 
			"    if (i > 0) {\n" + 
			"      try {\n" + 
			"        bar();\n" + 
			"      } catch (E2 e2) {\n" + 
			"      } catch (E1 e1) {\n" + 
			"      }\n" + 
			"    }\n" + 
			"  }\n" + 
			"  void bar() throws E2 {\n" + 
			"    throw new E2();\n" + 
			"  }\n" + 
			"}\n" + 
			"class E1 extends Exception {\n" + 
			"  private static final long serialVersionUID = 1L;\n" + 
			"}\n" + 
			"class E2 extends E1 {\n" + 
			"  private static final long serialVersionUID = 1L;\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:fieldHiding -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	int i;\n" + 
		"	    ^\n" + 
		"The field X.XX.i is hiding a field from type X\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// -warn option - regression tests
public void test177_warn_options() {
	// same source as 174, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  int i;\n" + 
			"  class XX {\n" + 
			"    int i;\n" + 
			"  }\n" + 
			"  void foo(int i) {\n" + 
			"    class XX {\n" + 
			"    }\n" + 
			"    if (i > 0) {\n" + 
			"      try {\n" + 
			"        bar();\n" + 
			"      } catch (E2 e2) {\n" + 
			"      } catch (E1 e1) {\n" + 
			"      }\n" + 
			"    }\n" + 
			"  }\n" + 
			"  void bar() throws E2 {\n" + 
			"    throw new E2();\n" + 
			"  }\n" + 
			"}\n" + 
			"class E1 extends Exception {\n" + 
			"  private static final long serialVersionUID = 1L;\n" + 
			"}\n" + 
			"class E2 extends E1 {\n" + 
			"  private static final long serialVersionUID = 1L;\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:localHiding -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)\n" + 
		"	void foo(int i) {\n" + 
		"	             ^\n" + 
		"The parameter i is hiding a field from type X\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// -warn option - regression tests
public void test178_warn_options() {
	// same source as 174, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  int i;\n" + 
			"  class XX {\n" + 
			"    int i;\n" + 
			"  }\n" + 
			"  void foo(int i) {\n" + 
			"    class XX {\n" + 
			"    }\n" + 
			"    if (i > 0) {\n" + 
			"      try {\n" + 
			"        bar();\n" + 
			"      } catch (E2 e2) {\n" + 
			"      } catch (E1 e1) {\n" + 
			"      }\n" + 
			"    }\n" + 
			"  }\n" + 
			"  void bar() throws E2 {\n" + 
			"    throw new E2();\n" + 
			"  }\n" + 
			"}\n" + 
			"class E1 extends Exception {\n" + 
			"  private static final long serialVersionUID = 1L;\n" + 
			"}\n" + 
			"class E2 extends E1 {\n" + 
			"  private static final long serialVersionUID = 1L;\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:maskedCatchBlock -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 13)\n" + 
		"	} catch (E1 e1) {\n" + 
		"	         ^^\n" + 
		"Unreachable catch block for E1. Only more specific exceptions are thrown and handled by previous catch block(s).\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// -warn option - regression tests
public void test179_warn_options() {
	// same source as 174, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  int i;\n" + 
			"  class XX {\n" + 
			"    int i;\n" + 
			"  }\n" + 
			"  void foo(int i) {\n" + 
			"    class XX {\n" + 
			"    }\n" + 
			"    if (i > 0) {\n" + 
			"      try {\n" + 
			"        bar();\n" + 
			"      } catch (E2 e2) {\n" + 
			"      } catch (E1 e1) {\n" + 
			"      }\n" + 
			"    }\n" + 
			"  }\n" + 
			"  void bar() throws E2 {\n" + 
			"    throw new E2();\n" + 
			"  }\n" + 
			"}\n" + 
			"class E1 extends Exception {\n" + 
			"  private static final long serialVersionUID = 1L;\n" + 
			"}\n" + 
			"class E2 extends E1 {\n" + 
			"  private static final long serialVersionUID = 1L;\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:typeHiding -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)\n" + 
		"	class XX {\n" + 
		"	      ^^\n" + 
		"The type XX is hiding the type X.XX\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// -warn option - regression tests
public void test180_warn_options() {
	// same source as 174, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  int i;\n" + 
			"  class XX {\n" + 
			"    int i;\n" + 
			"  }\n" + 
			"  void foo(int i) {\n" + 
			"    class XX {\n" + 
			"    }\n" + 
			"    if (i > 0) {\n" + 
			"      try {\n" + 
			"        bar();\n" + 
			"      } catch (E2 e2) {\n" + 
			"      } catch (E1 e1) {\n" + 
			"      }\n" + 
			"    }\n" + 
			"  }\n" + 
			"  void bar() throws E2 {\n" + 
			"    throw new E2();\n" + 
			"  }\n" + 
			"}\n" + 
			"class E1 extends Exception {\n" + 
			"  private static final long serialVersionUID = 1L;\n" + 
			"}\n" + 
			"class E2 extends E1 {\n" + 
			"  private static final long serialVersionUID = 1L;\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:hiding -warn:-fieldHiding -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)\n" + 
		"	void foo(int i) {\n" + 
		"	             ^\n" + 
		"The parameter i is hiding a field from type X\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)\n" + 
		"	class XX {\n" + 
		"	      ^^\n" + 
		"The type XX is hiding the type X.XX\n" + 
		"----------\n" + 
		"3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 13)\n" + 
		"	} catch (E1 e1) {\n" + 
		"	         ^^\n" + 
		"Unreachable catch block for E1. Only more specific exceptions are thrown and handled by previous catch block(s).\n" + 
		"----------\n" + 
		"3 problems (3 warnings)",
		true);
}
// -warn option - regression tests
public void test181_warn_options() {
	// same source as 174, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  int i;\n" + 
			"  class XX {\n" + 
			"    int i;\n" + 
			"  }\n" + 
			"  void foo(int i) {\n" + 
			"    class XX {\n" + 
			"    }\n" + 
			"    if (i > 0) {\n" + 
			"      try {\n" + 
			"        bar();\n" + 
			"      } catch (E2 e2) {\n" + 
			"      } catch (E1 e1) {\n" + 
			"      }\n" + 
			"    }\n" + 
			"  }\n" + 
			"  void bar() throws E2 {\n" + 
			"    throw new E2();\n" + 
			"  }\n" + 
			"}\n" + 
			"class E1 extends Exception {\n" + 
			"  private static final long serialVersionUID = 1L;\n" + 
			"}\n" + 
			"class E2 extends E1 {\n" + 
			"  private static final long serialVersionUID = 1L;\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:hiding -warn:-localHiding -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	int i;\n" + 
		"	    ^\n" + 
		"The field X.XX.i is hiding a field from type X\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)\n" + 
		"	class XX {\n" + 
		"	      ^^\n" + 
		"The type XX is hiding the type X.XX\n" + 
		"----------\n" + 
		"3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 13)\n" + 
		"	} catch (E1 e1) {\n" + 
		"	         ^^\n" + 
		"Unreachable catch block for E1. Only more specific exceptions are thrown and handled by previous catch block(s).\n" + 
		"----------\n" + 
		"3 problems (3 warnings)",
		true);
}
// -warn option - regression tests
public void test182_warn_options() {
	// same source as 174, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  int i;\n" + 
			"  class XX {\n" + 
			"    int i;\n" + 
			"  }\n" + 
			"  void foo(int i) {\n" + 
			"    class XX {\n" + 
			"    }\n" + 
			"    if (i > 0) {\n" + 
			"      try {\n" + 
			"        bar();\n" + 
			"      } catch (E2 e2) {\n" + 
			"      } catch (E1 e1) {\n" + 
			"      }\n" + 
			"    }\n" + 
			"  }\n" + 
			"  void bar() throws E2 {\n" + 
			"    throw new E2();\n" + 
			"  }\n" + 
			"}\n" + 
			"class E1 extends Exception {\n" + 
			"  private static final long serialVersionUID = 1L;\n" + 
			"}\n" + 
			"class E2 extends E1 {\n" + 
			"  private static final long serialVersionUID = 1L;\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:hiding -warn:-maskedCatchBlock -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	int i;\n" + 
		"	    ^\n" + 
		"The field X.XX.i is hiding a field from type X\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)\n" + 
		"	void foo(int i) {\n" + 
		"	             ^\n" + 
		"The parameter i is hiding a field from type X\n" + 
		"----------\n" + 
		"3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)\n" + 
		"	class XX {\n" + 
		"	      ^^\n" + 
		"The type XX is hiding the type X.XX\n" + 
		"----------\n" + 
		"3 problems (3 warnings)",
		true);
}
// -warn option - regression tests
public void test183_warn_options() {
	// same source as 174, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  int i;\n" + 
			"  class XX {\n" + 
			"    int i;\n" + 
			"  }\n" + 
			"  void foo(int i) {\n" + 
			"    class XX {\n" + 
			"    }\n" + 
			"    if (i > 0) {\n" + 
			"      try {\n" + 
			"        bar();\n" + 
			"      } catch (E2 e2) {\n" + 
			"      } catch (E1 e1) {\n" + 
			"      }\n" + 
			"    }\n" + 
			"  }\n" + 
			"  void bar() throws E2 {\n" + 
			"    throw new E2();\n" + 
			"  }\n" + 
			"}\n" + 
			"class E1 extends Exception {\n" + 
			"  private static final long serialVersionUID = 1L;\n" + 
			"}\n" + 
			"class E2 extends E1 {\n" + 
			"  private static final long serialVersionUID = 1L;\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:hiding -warn:-typeHiding -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	int i;\n" + 
		"	    ^\n" + 
		"The field X.XX.i is hiding a field from type X\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)\n" + 
		"	void foo(int i) {\n" + 
		"	             ^\n" + 
		"The parameter i is hiding a field from type X\n" + 
		"----------\n" + 
		"3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 13)\n" + 
		"	} catch (E1 e1) {\n" + 
		"	         ^^\n" + 
		"Unreachable catch block for E1. Only more specific exceptions are thrown and handled by previous catch block(s).\n" + 
		"----------\n" + 
		"3 problems (3 warnings)",
		true);
}
// -warn option - regression tests
public void test184_warn_options() {
	// check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X extends Y {\n" + 
			"  public static int i;\n" + 
			"  void foo() {\n" + 
			"    if (this.i > X.j) {\n" + 
			"    }\n" + 
			"  }\n" + 
			"}\n" + 
			"class Y {\n" + 
			"  static int j;\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	if (this.i > X.j) {\n" + 
		"	         ^\n" + 
		"The static field X.i should be accessed in a static way\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
	// observe -warn options variations
	this.runConformTest(
		new String[] { },
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -nowarn -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		false);
}
// -warn option - regression tests
public void test185_warn_options() {
	// same source as 184, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X extends Y {\n" + 
			"  public static int i;\n" + 
			"  void foo() {\n" + 
			"    if (this.i > X.j) {\n" + 
			"    }\n" + 
			"  }\n" + 
			"}\n" + 
			"class Y {\n" + 
			"  static int j;\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:staticReceiver -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	if (this.i > X.j) {\n" + 
		"	         ^\n" + 
		"The static field X.i should be accessed in a static way\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// -warn option - regression tests
public void test186_warn_options() {
	// same source as 184, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X extends Y {\n" + 
			"  public static int i;\n" + 
			"  void foo() {\n" + 
			"    if (this.i > X.j) {\n" + 
			"    }\n" + 
			"  }\n" + 
			"}\n" + 
			"class Y {\n" + 
			"  static int j;\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:indirectStatic -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	if (this.i > X.j) {\n" + 
		"	               ^\n" + 
		"The static field Y.j should be accessed directly\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// -warn option - regression tests
public void test187_warn_options() {
	// same source as 184, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X extends Y {\n" + 
			"  public static int i;\n" + 
			"  void foo() {\n" + 
			"    if (this.i > X.j) {\n" + 
			"    }\n" + 
			"  }\n" + 
			"}\n" + 
			"class Y {\n" + 
			"  static int j;\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:static-access -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	if (this.i > X.j) {\n" + 
		"	         ^\n" + 
		"The static field X.i should be accessed in a static way\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	if (this.i > X.j) {\n" + 
		"	               ^\n" + 
		"The static field Y.j should be accessed directly\n" + 
		"----------\n" + 
		"2 problems (2 warnings)",
		true);
}
// -warn option - regression tests
public void test188_warn_options() {
	// same source as 184, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X extends Y {\n" + 
			"  public static int i;\n" + 
			"  void foo() {\n" + 
			"    if (this.i > X.j) {\n" + 
			"    }\n" + 
			"  }\n" + 
			"}\n" + 
			"class Y {\n" + 
			"  static int j;\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:static-access -warn:-staticReceiver -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	if (this.i > X.j) {\n" + 
		"	               ^\n" + 
		"The static field Y.j should be accessed directly\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// -warn option - regression tests
public void test189_warn_options() {
	// same source as 184, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X extends Y {\n" + 
			"  public static int i;\n" + 
			"  void foo() {\n" + 
			"    if (this.i > X.j) {\n" + 
			"    }\n" + 
			"  }\n" + 
			"}\n" + 
			"class Y {\n" + 
			"  static int j;\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:static-access -warn:-indirectStatic -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	if (this.i > X.j) {\n" + 
		"	         ^\n" + 
		"The static field X.i should be accessed in a static way\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// -warn option - regression tests
public void test190_warn_options() {
	// check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"public class X extends Y {\n" + 
			"  private void foo(int i) throws java.io.IOException {\n" + 
			"    int j;\n" + 
			"    this.<String>bar();\n" + 
			"    next: for (;;) {\n" + 
			"      return;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  void bar() {\n" + 
			"  }\n" + 
			"}\n" + 
			"class Y {\n" + 
			"  <T> void bar() {\n" + 
			"  }\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)\n" + 
		"	import java.util.ArrayList;\n" + 
		"	       ^^^^^^^^^^^^^^^^^^^\n" + 
		"The import java.util.ArrayList is never used\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	private void foo(int i) throws java.io.IOException {\n" + 
		"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The method foo(int) from the type X is never used locally\n" + 
		"----------\n" + 
		"3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	int j;\n" + 
		"	    ^\n" + 
		"The local variable j is never read\n" + 
		"----------\n" + 
		"4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" + 
		"	this.<String>bar();\n" + 
		"	      ^^^^^^\n" + 
		"Unused type arguments for the non generic method bar() of type X; it should not be parameterized with arguments <String>\n" + 
		"----------\n" + 
		"5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)\n" + 
		"	next: for (;;) {\n" + 
		"	^^^^\n" + 
		"The label next is never explicitly referenced\n" + 
		"----------\n" + 
		"5 problems (5 warnings)",
		true);
	// observe -warn options variations
	this.runConformTest(
		new String[] { },
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -nowarn -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		false);
}
// -warn option - regression tests
public void test191_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"public class X extends Y {\n" + 
			"  private void foo(int i) throws java.io.IOException {\n" + 
			"    int j;\n" + 
			"    this.<String>bar();\n" + 
			"    next: for (;;) {\n" + 
			"      return;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  void bar() {\n" + 
			"  }\n" + 
			"}\n" + 
			"class Y {\n" + 
			"  <T> void bar() {\n" + 
			"  }\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)\n" + 
		"	import java.util.ArrayList;\n" + 
		"	       ^^^^^^^^^^^^^^^^^^^\n" + 
		"The import java.util.ArrayList is never used\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	private void foo(int i) throws java.io.IOException {\n" + 
		"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The method foo(int) from the type X is never used locally\n" + 
		"----------\n" + 
		"3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	private void foo(int i) throws java.io.IOException {\n" + 
		"	                     ^\n" + 
		"The parameter i is never read\n" + 
		"----------\n" + 
		"4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	private void foo(int i) throws java.io.IOException {\n" + 
		"	                               ^^^^^^^^^^^^^^^^^^^\n" + 
		"The declared exception IOException is not actually thrown by the method foo(int) from type X\n" + 
		"----------\n" + 
		"5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	int j;\n" + 
		"	    ^\n" + 
		"The local variable j is never read\n" + 
		"----------\n" + 
		"6. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" + 
		"	this.<String>bar();\n" + 
		"	      ^^^^^^\n" + 
		"Unused type arguments for the non generic method bar() of type X; it should not be parameterized with arguments <String>\n" + 
		"----------\n" + 
		"7. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)\n" + 
		"	next: for (;;) {\n" + 
		"	^^^^\n" + 
		"The label next is never explicitly referenced\n" + 
		"----------\n" + 
		"7 problems (7 warnings)",
		true);
}
// -warn option - regression tests
public void test192_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"public class X extends Y {\n" + 
			"  private void foo(int i) throws java.io.IOException {\n" + 
			"    int j;\n" + 
			"    this.<String>bar();\n" + 
			"    next: for (;;) {\n" + 
			"      return;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  void bar() {\n" + 
			"  }\n" + 
			"}\n" + 
			"class Y {\n" + 
			"  <T> void bar() {\n" + 
			"  }\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unusedArgument -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	private void foo(int i) throws java.io.IOException {\n" + 
		"	                     ^\n" + 
		"The parameter i is never read\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// -warn option - regression tests
public void test193_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"public class X extends Y {\n" + 
			"  private void foo(int i) throws java.io.IOException {\n" + 
			"    int j;\n" + 
			"    this.<String>bar();\n" + 
			"    next: for (;;) {\n" + 
			"      return;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  void bar() {\n" + 
			"  }\n" + 
			"}\n" + 
			"class Y {\n" + 
			"  <T> void bar() {\n" + 
			"  }\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unusedImport -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)\n" + 
		"	import java.util.ArrayList;\n" + 
		"	       ^^^^^^^^^^^^^^^^^^^\n" + 
		"The import java.util.ArrayList is never used\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// -warn option - regression tests
public void test194_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"public class X extends Y {\n" + 
			"  private void foo(int i) throws java.io.IOException {\n" + 
			"    int j;\n" + 
			"    this.<String>bar();\n" + 
			"    next: for (;;) {\n" + 
			"      return;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  void bar() {\n" + 
			"  }\n" + 
			"}\n" + 
			"class Y {\n" + 
			"  <T> void bar() {\n" + 
			"  }\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unusedLabel -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)\n" + 
		"	next: for (;;) {\n" + 
		"	^^^^\n" + 
		"The label next is never explicitly referenced\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// -warn option - regression tests
public void test195_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"public class X extends Y {\n" + 
			"  private void foo(int i) throws java.io.IOException {\n" + 
			"    int j;\n" + 
			"    this.<String>bar();\n" + 
			"    next: for (;;) {\n" + 
			"      return;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  void bar() {\n" + 
			"  }\n" + 
			"}\n" + 
			"class Y {\n" + 
			"  <T> void bar() {\n" + 
			"  }\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unusedLocal -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	int j;\n" + 
		"	    ^\n" + 
		"The local variable j is never read\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// -warn option - regression tests
public void test196_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"public class X extends Y {\n" + 
			"  private void foo(int i) throws java.io.IOException {\n" + 
			"    int j;\n" + 
			"    this.<String>bar();\n" + 
			"    next: for (;;) {\n" + 
			"      return;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  void bar() {\n" + 
			"  }\n" + 
			"}\n" + 
			"class Y {\n" + 
			"  <T> void bar() {\n" + 
			"  }\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unusedPrivate -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	private void foo(int i) throws java.io.IOException {\n" + 
		"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The method foo(int) from the type X is never used locally\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// -warn option - regression tests
public void test197_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"public class X extends Y {\n" + 
			"  private void foo(int i) throws java.io.IOException {\n" + 
			"    int j;\n" + 
			"    this.<String>bar();\n" + 
			"    next: for (;;) {\n" + 
			"      return;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  void bar() {\n" + 
			"  }\n" + 
			"}\n" + 
			"class Y {\n" + 
			"  <T> void bar() {\n" + 
			"  }\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unusedTypeArgs -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" + 
		"	this.<String>bar();\n" + 
		"	      ^^^^^^\n" + 
		"Unused type arguments for the non generic method bar() of type X; it should not be parameterized with arguments <String>\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// -warn option - regression tests
public void test198_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"public class X extends Y {\n" + 
			"  private void foo(int i) throws java.io.IOException {\n" + 
			"    int j;\n" + 
			"    this.<String>bar();\n" + 
			"    next: for (;;) {\n" + 
			"      return;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  void bar() {\n" + 
			"  }\n" + 
			"}\n" + 
			"class Y {\n" + 
			"  <T> void bar() {\n" + 
			"  }\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unusedThrown -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	private void foo(int i) throws java.io.IOException {\n" + 
		"	                               ^^^^^^^^^^^^^^^^^^^\n" + 
		"The declared exception IOException is not actually thrown by the method foo(int) from type X\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// -warn option - regression tests
public void test199_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"public class X extends Y {\n" + 
			"  private void foo(int i) throws java.io.IOException {\n" + 
			"    int j;\n" + 
			"    this.<String>bar();\n" + 
			"    next: for (;;) {\n" + 
			"      return;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  void bar() {\n" + 
			"  }\n" + 
			"}\n" + 
			"class Y {\n" + 
			"  <T> void bar() {\n" + 
			"  }\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -warn:-unusedArgument -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)\n" + 
		"	import java.util.ArrayList;\n" + 
		"	       ^^^^^^^^^^^^^^^^^^^\n" + 
		"The import java.util.ArrayList is never used\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	private void foo(int i) throws java.io.IOException {\n" + 
		"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The method foo(int) from the type X is never used locally\n" + 
		"----------\n" + 
		"3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	private void foo(int i) throws java.io.IOException {\n" + 
		"	                               ^^^^^^^^^^^^^^^^^^^\n" + 
		"The declared exception IOException is not actually thrown by the method foo(int) from type X\n" + 
		"----------\n" + 
		"4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	int j;\n" + 
		"	    ^\n" + 
		"The local variable j is never read\n" + 
		"----------\n" + 
		"5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" + 
		"	this.<String>bar();\n" + 
		"	      ^^^^^^\n" + 
		"Unused type arguments for the non generic method bar() of type X; it should not be parameterized with arguments <String>\n" + 
		"----------\n" + 
		"6. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)\n" + 
		"	next: for (;;) {\n" + 
		"	^^^^\n" + 
		"The label next is never explicitly referenced\n" + 
		"----------\n" + 
		"6 problems (6 warnings)",
		true);
}
// -warn option - regression tests
public void test200_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"public class X extends Y {\n" + 
			"  private void foo(int i) throws java.io.IOException {\n" + 
			"    int j;\n" + 
			"    this.<String>bar();\n" + 
			"    next: for (;;) {\n" + 
			"      return;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  void bar() {\n" + 
			"  }\n" + 
			"}\n" + 
			"class Y {\n" + 
			"  <T> void bar() {\n" + 
			"  }\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -warn:-unusedImport -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	private void foo(int i) throws java.io.IOException {\n" + 
		"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The method foo(int) from the type X is never used locally\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	private void foo(int i) throws java.io.IOException {\n" + 
		"	                     ^\n" + 
		"The parameter i is never read\n" + 
		"----------\n" + 
		"3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	private void foo(int i) throws java.io.IOException {\n" + 
		"	                               ^^^^^^^^^^^^^^^^^^^\n" + 
		"The declared exception IOException is not actually thrown by the method foo(int) from type X\n" + 
		"----------\n" + 
		"4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	int j;\n" + 
		"	    ^\n" + 
		"The local variable j is never read\n" + 
		"----------\n" + 
		"5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" + 
		"	this.<String>bar();\n" + 
		"	      ^^^^^^\n" + 
		"Unused type arguments for the non generic method bar() of type X; it should not be parameterized with arguments <String>\n" + 
		"----------\n" + 
		"6. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)\n" + 
		"	next: for (;;) {\n" + 
		"	^^^^\n" + 
		"The label next is never explicitly referenced\n" + 
		"----------\n" + 
		"6 problems (6 warnings)",
		true);
}
// -warn option - regression tests
public void test201_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"public class X extends Y {\n" + 
			"  private void foo(int i) throws java.io.IOException {\n" + 
			"    int j;\n" + 
			"    this.<String>bar();\n" + 
			"    next: for (;;) {\n" + 
			"      return;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  void bar() {\n" + 
			"  }\n" + 
			"}\n" + 
			"class Y {\n" + 
			"  <T> void bar() {\n" + 
			"  }\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -warn:-unusedLabel -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)\n" + 
		"	import java.util.ArrayList;\n" + 
		"	       ^^^^^^^^^^^^^^^^^^^\n" + 
		"The import java.util.ArrayList is never used\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	private void foo(int i) throws java.io.IOException {\n" + 
		"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The method foo(int) from the type X is never used locally\n" + 
		"----------\n" + 
		"3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	private void foo(int i) throws java.io.IOException {\n" + 
		"	                     ^\n" + 
		"The parameter i is never read\n" + 
		"----------\n" + 
		"4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	private void foo(int i) throws java.io.IOException {\n" + 
		"	                               ^^^^^^^^^^^^^^^^^^^\n" + 
		"The declared exception IOException is not actually thrown by the method foo(int) from type X\n" + 
		"----------\n" + 
		"5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	int j;\n" + 
		"	    ^\n" + 
		"The local variable j is never read\n" + 
		"----------\n" + 
		"6. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" + 
		"	this.<String>bar();\n" + 
		"	      ^^^^^^\n" + 
		"Unused type arguments for the non generic method bar() of type X; it should not be parameterized with arguments <String>\n" + 
		"----------\n" + 
		"6 problems (6 warnings)",
		true);
}
// -warn option - regression tests
public void test202_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"public class X extends Y {\n" + 
			"  private void foo(int i) throws java.io.IOException {\n" + 
			"    int j;\n" + 
			"    this.<String>bar();\n" + 
			"    next: for (;;) {\n" + 
			"      return;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  void bar() {\n" + 
			"  }\n" + 
			"}\n" + 
			"class Y {\n" + 
			"  <T> void bar() {\n" + 
			"  }\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -warn:-unusedLocal -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)\n" + 
		"	import java.util.ArrayList;\n" + 
		"	       ^^^^^^^^^^^^^^^^^^^\n" + 
		"The import java.util.ArrayList is never used\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	private void foo(int i) throws java.io.IOException {\n" + 
		"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The method foo(int) from the type X is never used locally\n" + 
		"----------\n" + 
		"3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	private void foo(int i) throws java.io.IOException {\n" + 
		"	                     ^\n" + 
		"The parameter i is never read\n" + 
		"----------\n" + 
		"4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	private void foo(int i) throws java.io.IOException {\n" + 
		"	                               ^^^^^^^^^^^^^^^^^^^\n" + 
		"The declared exception IOException is not actually thrown by the method foo(int) from type X\n" + 
		"----------\n" + 
		"5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" + 
		"	this.<String>bar();\n" + 
		"	      ^^^^^^\n" + 
		"Unused type arguments for the non generic method bar() of type X; it should not be parameterized with arguments <String>\n" + 
		"----------\n" + 
		"6. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)\n" + 
		"	next: for (;;) {\n" + 
		"	^^^^\n" + 
		"The label next is never explicitly referenced\n" + 
		"----------\n" + 
		"6 problems (6 warnings)",
		true);
}
// -warn option - regression tests
public void test203_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"public class X extends Y {\n" + 
			"  private void foo(int i) throws java.io.IOException {\n" + 
			"    int j;\n" + 
			"    this.<String>bar();\n" + 
			"    next: for (;;) {\n" + 
			"      return;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  void bar() {\n" + 
			"  }\n" + 
			"}\n" + 
			"class Y {\n" + 
			"  <T> void bar() {\n" + 
			"  }\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -warn:-unusedPrivate -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)\n" + 
		"	import java.util.ArrayList;\n" + 
		"	       ^^^^^^^^^^^^^^^^^^^\n" + 
		"The import java.util.ArrayList is never used\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	private void foo(int i) throws java.io.IOException {\n" + 
		"	                     ^\n" + 
		"The parameter i is never read\n" + 
		"----------\n" + 
		"3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	private void foo(int i) throws java.io.IOException {\n" + 
		"	                               ^^^^^^^^^^^^^^^^^^^\n" + 
		"The declared exception IOException is not actually thrown by the method foo(int) from type X\n" + 
		"----------\n" + 
		"4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	int j;\n" + 
		"	    ^\n" + 
		"The local variable j is never read\n" + 
		"----------\n" + 
		"5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" + 
		"	this.<String>bar();\n" + 
		"	      ^^^^^^\n" + 
		"Unused type arguments for the non generic method bar() of type X; it should not be parameterized with arguments <String>\n" + 
		"----------\n" + 
		"6. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)\n" + 
		"	next: for (;;) {\n" + 
		"	^^^^\n" + 
		"The label next is never explicitly referenced\n" + 
		"----------\n" + 
		"6 problems (6 warnings)",
		true);
}
// -warn option - regression tests
public void test204_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"public class X extends Y {\n" + 
			"  private void foo(int i) throws java.io.IOException {\n" + 
			"    int j;\n" + 
			"    this.<String>bar();\n" + 
			"    next: for (;;) {\n" + 
			"      return;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  void bar() {\n" + 
			"  }\n" + 
			"}\n" + 
			"class Y {\n" + 
			"  <T> void bar() {\n" + 
			"  }\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -warn:-unusedThrown -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)\n" + 
		"	import java.util.ArrayList;\n" + 
		"	       ^^^^^^^^^^^^^^^^^^^\n" + 
		"The import java.util.ArrayList is never used\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	private void foo(int i) throws java.io.IOException {\n" + 
		"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The method foo(int) from the type X is never used locally\n" + 
		"----------\n" + 
		"3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	private void foo(int i) throws java.io.IOException {\n" + 
		"	                     ^\n" + 
		"The parameter i is never read\n" + 
		"----------\n" + 
		"4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	int j;\n" + 
		"	    ^\n" + 
		"The local variable j is never read\n" + 
		"----------\n" + 
		"5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" + 
		"	this.<String>bar();\n" + 
		"	      ^^^^^^\n" + 
		"Unused type arguments for the non generic method bar() of type X; it should not be parameterized with arguments <String>\n" + 
		"----------\n" + 
		"6. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)\n" + 
		"	next: for (;;) {\n" + 
		"	^^^^\n" + 
		"The label next is never explicitly referenced\n" + 
		"----------\n" + 
		"6 problems (6 warnings)",
		true);
}
// -warn option - regression tests
public void test205_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" + 
			"public class X extends Y {\n" + 
			"  private void foo(int i) throws java.io.IOException {\n" + 
			"    int j;\n" + 
			"    this.<String>bar();\n" + 
			"    next: for (;;) {\n" + 
			"      return;\n" + 
			"    }\n" + 
			"  }\n" + 
			"  void bar() {\n" + 
			"  }\n" + 
			"}\n" + 
			"class Y {\n" + 
			"  <T> void bar() {\n" + 
			"  }\n" + 
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -warn:-unusedTypeArgs -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)\n" + 
		"	import java.util.ArrayList;\n" + 
		"	       ^^^^^^^^^^^^^^^^^^^\n" + 
		"The import java.util.ArrayList is never used\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	private void foo(int i) throws java.io.IOException {\n" + 
		"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The method foo(int) from the type X is never used locally\n" + 
		"----------\n" + 
		"3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	private void foo(int i) throws java.io.IOException {\n" + 
		"	                     ^\n" + 
		"The parameter i is never read\n" + 
		"----------\n" + 
		"4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	private void foo(int i) throws java.io.IOException {\n" + 
		"	                               ^^^^^^^^^^^^^^^^^^^\n" + 
		"The declared exception IOException is not actually thrown by the method foo(int) from type X\n" + 
		"----------\n" + 
		"5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	int j;\n" + 
		"	    ^\n" + 
		"The local variable j is never read\n" + 
		"----------\n" + 
		"6. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)\n" + 
		"	next: for (;;) {\n" + 
		"	^^^^\n" + 
		"The label next is never explicitly referenced\n" + 
		"----------\n" + 
		"6 problems (6 warnings)",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// -warn option - regression tests
public void test206_warn_options() {
	// same source as 168, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  /**\n" +
			"    @param\n" +
			"  */\n" +
			"  public void foo() {\n" +
			"  }\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:allJavadoc -enableJavadoc -warn:-allJavadoc -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// -warn option - regression tests
public void test207_warn_options() {
	// same source as 168, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  /**\n" +
			"    @param\n" +
			"  */\n" +
			"  public void foo() {\n" +
			"  }\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:javadoc -enableJavadoc -warn:-javadoc -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// variant
public void test208_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  /**\n" +
			"    @param i explained\n" +
			"  */\n" +
			"  public void foo(int i) {\n" +
			"  }\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" + 
		"	public void foo(int i) {\n" + 
		"	                    ^\n" + 
		"The parameter i is never read\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// variant
public void test209_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  /**\n" +
			"    @param i explained\n" +
			"  */\n" +
			"  public void foo(int i) {\n" +
			"  }\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -enableJavadoc -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// variant
public void test210_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  /**\n" +
			"    @param i explained\n" +
			"  */\n" +
			"  public void foo(int i) {\n" +
			"  }\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -warn:-allJavadoc -enableJavadoc -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// variant
public void test211_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  /**\n" +
			"    @param i explained\n" +
			"  */\n" +
			"  public void foo(int i) {\n" +
			"  }\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -enableJavadoc -warn:-allJavadoc -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// variant - javadoc and allJavadoc mistakenly imply enableJavadoc
public void test212_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  /**\n" +
			"    @param i explained\n" +
			"  */\n" +
			"  public void foo(int i) {\n" +
			"  }\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused,allJavadoc -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)\n" + 
		"	public class X {\n" + 
		"	             ^\n" + 
		"Javadoc: Missing comment for public declaration\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" + 
		"	public void foo(int i) {\n" + 
		"	                    ^\n" + 
		"The parameter i is never read\n" + 
		"----------\n" + 
		"2 problems (2 warnings)",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// variant - javadoc and allJavadoc mistakenly imply enableJavadoc
public void test213_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  /**\n" +
			"    @param i explained\n" +
			"  */\n" +
			"  public void foo(int i) {\n" +
			"  }\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused,javadoc -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" + 
		"	public void foo(int i) {\n" + 
		"	                    ^\n" + 
		"The parameter i is never read\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210518
// variant
public void test214_warn_options() {
	// same source as 153, skip default checks
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void foo() {\n" +
			"     String s = null;\n" +
			"     s.toString();\n" +
			"     String u;\n" +
			"   }\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:null,-unused -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"invalid warning option: -warn:null,-unused. Must specify a warning token\n",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210518
// variant
public void test215_warn_options() {
	// same source as 153, skip default checks
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void foo() {\n" +
			"     String s = null;\n" +
			"     s.toString();\n" +
			"     String u;\n" +
			"   }\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:null,+unused -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"invalid warning option: -warn:null,+unused. Must specify a warning token\n",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// variant - check impact of javadoc upon other warnings
public void _test216a_warn_options() {
	// check what if allJavadoc on
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */" +
			"public class X {\n" +
			"  /**\n" +
			"    {@link Y}\n" +
			"  */\n" +
			"  public void foo() {\n" +
			"  }\n" + 
			"}\n",
			"Y.java",
			"/** @deprecated */" +
			"public class Y {\n" +
			"}",			
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:allJavadoc -proc:none -d \"" + OUTPUT_DIR + "\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	// same sources, what if we add -warn:+javadoc
	this.runConformTest(
		new String[] { },
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:allJavadoc -warn:+javadoc -proc:none -d \"" + OUTPUT_DIR + "\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\"",
		"",
		"",
		false);
	// same sources, what if we only have -warn:javadoc
	this.runConformTest(
		new String[] { },
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:javadoc -proc:none -d \"" + OUTPUT_DIR + "\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\"",
		"",
		"",
		false);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// variant - check impact of javadoc upon other warnings
public void test216b_warn_options() {
	// check what if allJavadoc on
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */" +
			"public class X {\n" +
			"  /**\n" +
			"    {@link Y}\n" +
			"  */\n" +
			"  public void foo() {\n" +
			"  }\n" + 
			"}\n",
			"Y.java",
			"/** @deprecated */" +
			"public class Y {\n" +
			"}",			
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:+allJavadoc -proc:none -d \"" + OUTPUT_DIR + "\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	{@link Y}\n" + 
		"	       ^\n" + 
		"Javadoc: The type Y is deprecated\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
	// same sources, what if we add -warn:+javadoc
	this.runConformTest(
		new String[] { },
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:+allJavadoc -warn:+javadoc -proc:none -d \"" + OUTPUT_DIR + "\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	{@link Y}\n" + 
		"	       ^\n" + 
		"Javadoc: The type Y is deprecated\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		false);
	// same sources, what if we only have -warn:javadoc
	this.runConformTest(
		new String[] { },
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:+javadoc -proc:none -d \"" + OUTPUT_DIR + "\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	{@link Y}\n" + 
		"	       ^\n" + 
		"Javadoc: The type Y is deprecated\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		false);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// variant - check impact of javadoc upon other warnings
public void test217_warn_options() {
	// check what if allJavadoc on
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */" +
			"public class X {\n" +
			"  /**\n" +
			"    @see #bar()\n" +
			"  */\n" +
			"  public void foo() {\n" +
			"    bar();\n" +
			"  }\n" + 
			"  private void bar() {\n" +
			"  }\n" + 
			"}\n"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:allJavadoc -proc:none -d \"" + OUTPUT_DIR + "\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	@see #bar()\n" + 
		"	     ^^^^^^\n" + 
		"Javadoc: \'public\' visibility for malformed doc comments hides this \'private\' reference\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
	// same sources, what if we add -warn:+javadoc
	this.runConformTest(
		new String[] { },
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:allJavadoc -warn:+javadoc -proc:none -d \"" + OUTPUT_DIR + "\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	@see #bar()\n" + 
		"	     ^^^^^^\n" + 
		"Javadoc: \'public\' visibility for malformed doc comments hides this \'private\' reference\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		false);
	// same sources, what if we only have -warn:javadoc
	this.runConformTest(
		new String[] { },
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:javadoc -proc:none -d \"" + OUTPUT_DIR + "\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	@see #bar()\n" + 
		"	     ^^^^^^\n" + 
		"Javadoc: \'public\' visibility for malformed doc comments hides this \'private\' reference\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		false);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=214731
// white-box test for internal API
public void test218_batch_classpath_apis() {
	assertFalse("path should be absolute", 
		new ClasspathJar(new File("relative.jar"), true, null, null).
		getPath().indexOf(File.separator) == -1);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=214731
// white-box test for internal API
public void test219_batch_classpath_apis() {
	assertFalse("path should be absolute", 
		CharOperation.indexOf('/', 
			new ClasspathJar(new File("relative.jar"), true, null, null).
			normalizedPath()) == -1);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210524
// -warn option - regression tests
// variant
public void _test220_warn_options() {
	// same source as 172, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  Y f;\n" +
			"  /** @deprecated */\n" +
			"  void foo(Y p) {\n" +
			"  }\n" +
			"}",
			"Y.java",
			"/** @deprecated */\n" +
			"public class Y {\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:allDeprecation -warn:+deprecation -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)\n" + 
		"	Y f;\n" + 
		"	^\n" + 
		"The type Y is deprecated\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	void foo(Y p) {\n" + 
		"	         ^\n" + 
		"The type Y is deprecated\n" + 
		"----------\n" + 
		"2 problems (2 warnings)",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210524
// -warn option - regression tests
// variant
public void test221_warn_options() {
	// same source as 172, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  Y f;\n" +
			"  /** @deprecated */\n" +
			"  void foo(Y p) {\n" +
			"  }\n" +
			"}",
			"Y.java",
			"/** @deprecated */\n" +
			"public class Y {\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:allDeprecation -warn:deprecation -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)\n" + 
		"	Y f;\n" + 
		"	^\n" + 
		"The type Y is deprecated\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210524
// -warn option - regression tests
// variant
public void test222_warn_options() {
	// same source as 172, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  Y f;\n" +
			"  /** @deprecated */\n" +
			"  void foo(Y p) {\n" +
			"  }\n" +
			"}",
			"Y.java",
			"/** @deprecated */\n" +
			"public class Y {\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:allDeprecation -deprecation -proc:none -d \"" + OUTPUT_DIR + "\"",
		// according to the documentation, equivalent to -warn:allDeprecation -warn:+deprecation
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)\n" + 
		"	Y f;\n" + 
		"	^\n" + 
		"The type Y is deprecated\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	void foo(Y p) {\n" + 
		"	         ^\n" + 
		"The type Y is deprecated\n" + 
		"----------\n" + 
		"2 problems (2 warnings)",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210524
// -warn option - regression tests
// variant
public void test223_warn_options() {
	// same source as 172, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  Y f;\n" +
			"  /** @deprecated */\n" +
			"  void foo(Y p) {\n" +
			"  }\n" +
			"}",
			"Y.java",
			"/** @deprecated */\n" +
			"public class Y {\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -deprecation -warn:-allDeprecation -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210524
// -warn option - regression tests
// variant
public void _test224_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  int i;\n" +
			"  X(int i) {\n" +
			"  }\n" +
			"}"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:+localHiding,specialParamHiding -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	X(int i) {\n" + 
		"	      ^\n" + 
		"The parameter i is hiding a field from type X\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
	// deprecation should erase whatever warnings have been set previously
	this.runConformTest(
		new String[] {},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:+localHiding,specialParamHiding -warn:deprecation -warn:+localHiding -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		false);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210524
// -warn option - regression tests
// variant
public void test225_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"@SuppressWarnings(\"deprecation\")\n" +
			"public class X {\n" + 
			"  Y f;\n" +
			"  /** @deprecated */\n" +
			"  void foo(Y p) {\n" +
			"  }\n" +
			"}",
			"Y.java",
			"/** @deprecated */\n" +
			"public class Y {\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.5 -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -deprecation -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210524
// -warn option - regression tests
// variant
public void _test226_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"@SuppressWarnings(\"deprecation\")\n" +
			"public class X {\n" + 
			"  Y f;\n" +
			"  /** @deprecated */\n" +
			"  void foo(Y p) {\n" +
			"  }\n" +
			"}",
			"Y.java",
			"/** @deprecated */\n" +
			"public class Y {\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.5 -sourcepath \"" + OUTPUT_DIR + "\""
		// default -warn:+suppress gets overriden
		+ " -warn:deprecation -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	Y f;\n" + 
		"	^\n" + 
		"The type Y is deprecated\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// -warn option - regression tests
// variant detected while exploring:
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210524 
public void test227_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  /**\n" +
			"    @param i explained\n" +
			"  */\n" +
			"  public void foo(int i) {\n" +
			"  }\n" + 
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" + 
		"	public void foo(int i) {\n" + 
		"	                    ^\n" + 
		"The parameter i is never read\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// -warn option - regression tests
// variant
public void test228_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" + 
			"public class X {\n" + 
			"/** @throws IOException mute warning **/\n" + 
			"  public void foo() throws IOException {\n" + 
			"  }\n" + 
			"}\n",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unusedThrown -enableJavadoc -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// -warn option - regression tests
// variant
public void test229_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" + 
			"public class X {\n" + 
			"/** @throws IOException mute warning **/\n" + 
			"  public void foo() throws IOException {\n" + 
			"  }\n" + 
			"}\n",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unusedThrown -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	public void foo() throws IOException {\n" + 
		"	                         ^^^^^^^^^^^\n" + 
		"The declared exception IOException is not actually thrown by the method foo() from type X\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=216684
// .java/.class files precedence depending on sourcepath and other conditions
// ecj always selects source files from the sourcepath over class files
// javac selects the class file over the source file when the class file is
// newer than the source file, unless option -Xprefer:source is used (available
// since 1.6)
public void test230_sourcepath_vs_classpath() throws IOException, InterruptedException {
	runTest(
		true /* shouldCompileOK*/,
		new String[] { /* testFiles */
			"src1/X.java",
			"public class X {\n" +
			"  public static final int CONST = 1;\n" + 
			"}\n",
			"src2/X.java",
			"public class X {\n" +
			"  public static final int CONST = 2;\n" + 
			"}\n",
		},
		"\"" + OUTPUT_DIR + File.separator + "src1" + File.separator + "X.java\"" /* commandLine */
		+ " -verbose -proc:none -d \"" + OUTPUT_DIR + File.separator + "bin1" + "\"",
		"[parsing    ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #1/1]\n" + /* expectedOutOutputString */
		"[reading    java/lang/Object.class]\n" + 
		"[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #1/1]\n" + 
		"[writing    X.class - #1]\n" + 
		"[completed  ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #1/1]\n" + 
		"[1 unit compiled]\n" + 
		"[1 .class file generated]\n",
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		null /* progress */);
	// ensure that class file is newer than source file (some file systems
	// store the modification time at a second precision)
	File sourceFile = new File(OUTPUT_DIR + File.separator + "src1" + File.separator + "X.java"),
	  classFile = new File(OUTPUT_DIR + File.separator + "bin1" + File.separator + "X.class");
	while (classFile.lastModified() <= sourceFile.lastModified()) {
		runConformTest(
			null,
			"\"" + OUTPUT_DIR +  File.separator + "src1" + File.separator + "X.java\""
			+ " -proc:none -d \"" + OUTPUT_DIR + File.separator + "bin1" + "\"",
			"",
			"",
			false);
	}
	// compile with classpath only: X.class gets selected
	runConformTest(
		new String[] {
			"Y.java",
			"public class Y {\n" +
			"  public static void main (String[] args) {\n" +
			"    System.out.println(X.CONST);\n" +
			"  }\n" + 
			"}\n",
		},
		"\"" + OUTPUT_DIR +  File.separator + "Y.java\""
		+ " -classpath \"" + OUTPUT_DIR + File.separator + "bin1" + "\""
		+ " -verbose -proc:none -d \"" + OUTPUT_DIR + "\"",
		"[parsing    ---OUTPUT_DIR_PLACEHOLDER---/Y.java - #1/1]\n" + 
		"[reading    java/lang/Object.class]\n" + 
		"[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/Y.java - #1/1]\n" + 
		"[reading    java/lang/String.class]\n" + 
		"[reading    java/lang/System.class]\n" + 
		"[reading    java/io/PrintStream.class]\n" + 
		"[reading    X.class]\n" + 
		"[writing    Y.class - #1]\n" + 
		"[completed  ---OUTPUT_DIR_PLACEHOLDER---/Y.java - #1/1]\n" + 
		"[1 unit compiled]\n" + 
		"[1 .class file generated]\n",
		"",
		false);
	// compile with sourcepath and classpath: src2/X.java is preferred
	// this matches -Xprefer:source option of javac - except that
	// javac then does it for classpath too; by default, javac would select
	// bin1/X.class (as shown below)
	String sourceFilePath = "\"" + OUTPUT_DIR +  File.separator + "Y.java\"";
	String commonOptions = 
		" -classpath \"" + OUTPUT_DIR + File.separator + "bin1" + "\""
		+ " -sourcepath \"" + OUTPUT_DIR + File.separator + "src2" + "\""
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin2" + "\"";
	String setting= System.getProperty("jdt.compiler.useSingleThread");
	try {
		System.setProperty("jdt.compiler.useSingleThread", "true");
		runConformTest(
			null,
			sourceFilePath + commonOptions + " -verbose -proc:none",
			"[parsing    ---OUTPUT_DIR_PLACEHOLDER---/Y.java - #1/1]\n" + 
			"[reading    java/lang/Object.class]\n" + 
			"[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/Y.java - #1/1]\n" + 
			"[reading    java/lang/String.class]\n" + 
			"[reading    java/lang/System.class]\n" + 
			"[reading    java/io/PrintStream.class]\n" + 
			"[parsing    ---OUTPUT_DIR_PLACEHOLDER---/src2/X.java - #2/2]\n" + 
			"[writing    Y.class - #1]\n" + 
			"[completed  ---OUTPUT_DIR_PLACEHOLDER---/Y.java - #1/2]\n" + 
			"[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/src2/X.java - #2/2]\n" + 
			"[writing    X.class - #2]\n" + 
			"[completed  ---OUTPUT_DIR_PLACEHOLDER---/src2/X.java - #2/2]\n" + 
			"[2 units compiled]\n" + 
			"[2 .class files generated]\n",
			"",
			false);
	} finally {
		System.setProperty("jdt.compiler.useSingleThread", setting == null ? "false" : setting);
	}
	if (RUN_JAVAC) {
		// run ecj result
		this.verifier.execute("Y", new String[] {OUTPUT_DIR + File.separator + "bin2"});
		assertTrue(this.verifier.getExecutionOutput().startsWith("2")); // skip trailing newline
		// 2 means we selected src2
		// recompile and run result using various levels of javac
		Iterator javacCompilersIterator = javacCompilers.iterator();
		String specialOptions = commonOptions + " -Xprefer:source ";
		String sourceFileNames[] = new String[] {sourceFilePath};
		File outputDir = new File(OUTPUT_DIR);
		while (javacCompilersIterator.hasNext()) {
			JavacCompiler javacCompiler = (JavacCompiler) javacCompilersIterator.next();
			assertTrue(javacCompiler.compile(
					outputDir, /* directory */
					commonOptions /* options */, 
					sourceFileNames /* source file names */,
					null /* log */) == 0);
			this.verifier.execute("Y", new String[] {OUTPUT_DIR + File.separator + "bin2"});
			assertEquals('1', this.verifier.getExecutionOutput().charAt(0)); // skip trailing newline
			// 1 means javac selected bin1 by default
			if (javacCompiler.version.compareTo(JavaCore.VERSION_1_6) >= 0) {
				assertTrue(javacCompiler.compile(
						outputDir, /* directory */
						specialOptions /* options */, 
						sourceFileNames /* source file names */,
						null /* log */) == 0);
				this.verifier.execute("Y", new String[] {OUTPUT_DIR + File.separator + "bin2"});
				assertEquals('2', this.verifier.getExecutionOutput().charAt(0)); // skip trailing newline
				// 2 means javac selected src2
			}
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=216684
// .java/.class files precedence depending on sourcepath
// ecj always selects sourcepath over classpath
// javac takes the source file if it is more recent than the class file
public void test231_sourcepath_vs_classpath() throws IOException, InterruptedException {
	// compile into bin1
	runConformTest(
		new String[] {
			"src1/X.java",
			"public class X {\n" +
			"  public static final int CONST = 1;\n" + 
			"}\n",
		},
		"\"" + OUTPUT_DIR + File.separator + "src1" + File.separator + "X.java\""
		+ " -proc:none -d \"" + OUTPUT_DIR + File.separator + "bin1" + "\"",
		"",
		"",
		true);
	// ensure that source file is newer than class file (some file systems
	// store the modification time at a second precision)
	File sourceFile = new File(OUTPUT_DIR + File.separator + "src2" + File.separator + "X.java"),
	  classFile = new File(OUTPUT_DIR + File.separator + "bin1" + File.separator + "X.class");
	new File(OUTPUT_DIR + File.separator + "src2").mkdirs();
	do {
		Util.writeToFile(
			"public class X {\n" +
			"}\n", 
			sourceFile.getPath());
	} while (classFile.lastModified() >= sourceFile.lastModified());
	// compile with sourcepath and classpath: src2/X.java is preferred
	String sourceFilePath = "\"" + OUTPUT_DIR +  File.separator + "Y.java\"";
	String commonOptions = 
		" -classpath \"" + OUTPUT_DIR + File.separator + "bin1" + "\""
		+ " -sourcepath \"" + OUTPUT_DIR + File.separator + "src2" + "\""
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin2" + "\"";
	// sourcepath preferred over classpath
	runTest(
		false /* shouldCompileOK */,
		new String[] { /* testFiles */
			"Y.java",
			"public class Y {\n" +
			"  public static void main (String[] args) {\n" +
			"    System.out.println(X.CONST);\n" +
			"  }\n" + 
			"}\n",
		},
		sourceFilePath + commonOptions + " -proc:none " /* commandLine */,
		"" /* expectedOutOutputString */,
		"----------\n" +  /* expectedErrOutputString */
		"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/Y.java (at line 3)\n" + 
		"	System.out.println(X.CONST);\n" + 
		"	                   ^^^^^^^\n" + 
		"X.CONST cannot be resolved\n" + 
		"----------\n" + 
		"1 problem (1 error)",
		false /* shouldFlushOutputDirectory */,
		null /* progress */);
	if (RUN_JAVAC) {
		Iterator javacCompilersIterator = javacCompilers.iterator();
		String sourceFileNames[] = new String[] {sourceFilePath}; 
		File outputDir = new File(OUTPUT_DIR);
		while (javacCompilersIterator.hasNext()) {
			JavacCompiler javacCompiler = (JavacCompiler) javacCompilersIterator.next();
			assertFalse(javacCompiler.compile(
					outputDir /* directory */,
					commonOptions /* options */, 
					sourceFileNames /* source file names */,
					null /* log */) == 0);
			// compile fails as well
		}
		assertFalse(runJavac(commonOptions, new String[] {sourceFilePath}, OUTPUT_DIR));
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=216684
// ecj different from javac: repeated -classpath concatenates entries, while javac 
// only keeps the last one (and swallows the others silently)
public void test232_repeated_classpath() throws IOException, InterruptedException {
	String commonOptions = " -d \"" + OUTPUT_DIR + File.separator + "bin" 
		+ "\" -classpath \"" + OUTPUT_DIR + File.separator + "src1";
	String combinedClasspathOptions = commonOptions + File.pathSeparator
		+ OUTPUT_DIR + File.separator + "src2\" ";
	String splitClasspathOptions = commonOptions
		+ "\" -classpath \"" + OUTPUT_DIR + File.separator + "src2\" ";
	String sourceFilePath = "\"" + OUTPUT_DIR + File.separator + "src3" + File.separator + "Z.java\"";
	// ecj considers repeated classpath entries as if they were concatenated
	// into one
	runTest(
		true /* shouldCompileOK*/,
		new String[] { /* testFiles */
			"src1/X.java",
			"public class X {\n" +
			"}\n",
			"src2/Y.java",
			"public class Y {\n" +
			"}\n",
			"src3/Z.java",
			"public class Z {\n" +
			"  X x;\n" +
			"  Y y;\n" +
			"}\n",
		},
		sourceFilePath + " -proc:none " + combinedClasspathOptions /* commandLine */,
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		null /* progress */);
	runTest(
		true /* shouldCompileOK*/,
		null /* testFiles */,
		sourceFilePath + " -proc:none " + splitClasspathOptions /* commandLine */,
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		false /* shouldFlushOutputDirectory */,
		null /* progress */);
	if (RUN_JAVAC) {
		// javac skips all but the last classpath entry (which results into an
		// error in the split case here)
		Iterator javacCompilersIterator = javacCompilers.iterator();
		String sourceFileNames[] = new String[] {sourceFilePath}; 
		File outputDir = new File(OUTPUT_DIR);
		while (javacCompilersIterator.hasNext()) {
			JavacCompiler javacCompiler = (JavacCompiler) javacCompilersIterator.next();
			assertTrue(javacCompiler.compile(
					outputDir /* directory */,
					combinedClasspathOptions /* options */, 
					sourceFileNames,
					null /* log */) == 0);
			assertFalse(javacCompiler.compile(
					outputDir /* directory */,
					splitClasspathOptions /* options */, 
					sourceFileNames,
					null /* log */) == 0);
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=216684
// ecj different from javac: repeated -sourcepath yields an error, while javac 
// only keeps the last one (and swallows the others silently)
public void test233_repeated_sourcepath() throws IOException, InterruptedException {
	String commonOptions = " -d \"" + OUTPUT_DIR + "\"" 
		+ " -sourcepath \"" + OUTPUT_DIR + File.separator + "src1\""
		+ " -sourcepath \"" + OUTPUT_DIR + File.separator + "src2\"";
	String sourceFilePathZ = "\"" + OUTPUT_DIR + File.separator + "src3" + File.separator + "Z.java\"";
	String sourceFilePathW = "\"" + OUTPUT_DIR + File.separator + "src3" + File.separator + "W.java\"";
	runTest(
		false /* shouldCompileOK */,
		new String[] { /* testFiles */
			"src1/X.java",
			"public class X {\n" +
			"}\n",
			"src2/Y.java",
			"public class Y {\n" +
			"}\n",
			"src3/Z.java",
			"public class Z {\n" +
			"  Y y;\n" +
			"}\n",
			"src3/W.java",
			"public class W {\n" +
			"  X x;\n" +
			"  Y y;\n" +
			"}\n",
		},
		sourceFilePathZ + " -proc:none " + commonOptions /* commandLine */,
		"" /* expectedOutOutputString */,
		"duplicate sourcepath specification: -sourcepath ---OUTPUT_DIR_PLACEHOLDER---/src2\n" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		null /* progress */);
	if (RUN_JAVAC) {
		Iterator javacCompilersIterator = javacCompilers.iterator();
		String sourceFileNamesZ[] = new String[] {sourceFilePathZ}; 
		String sourceFileNamesW[] = new String[] {sourceFilePathW}; 
		File outputDir = new File(OUTPUT_DIR);
		while (javacCompilersIterator.hasNext()) {
			JavacCompiler javacCompiler = (JavacCompiler) javacCompilersIterator.next();
			// succeeds because it picks src2 up
			assertTrue(javacCompiler.compile(
					outputDir /* directory */,
					commonOptions /* options */, 
					sourceFileNamesZ /* source file names */,
					null /* log */) == 0);
			// fails because it misses src1
			assertFalse(javacCompiler.compile(
					outputDir /* directory */,
					commonOptions /* options */, 
					sourceFileNamesW /* source file names */,
					null /* log */) == 0);
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=216684
// different from javac: javac sourcepath inhibits compile in classpath, while 
// ecj goes on finding source files there
public void test234_sourcepath_vs_classpath() throws IOException, InterruptedException {
	String commonOptions = " -d \"" + OUTPUT_DIR + File.separator + "bin\"" 
		+ " -sourcepath \"" + OUTPUT_DIR + File.separator + "src1\""
		+ " -classpath \"" + OUTPUT_DIR + File.separator + "src2\" ";
	String sourceFilePath = "\"" + OUTPUT_DIR + File.separator + "src3" + File.separator + "Z.java\"";
	// ecj compiles src1 and src2 source files as needed, regardless of their
	// being on the sourcepath or the classpath
	runTest(
		true /* shouldCompileOK*/,
		new String[] { /* testFiles */
			"src1/X.java",
			"public class X {\n" +
			"}\n",
			"src2/Y.java",
			"public class Y {\n" +
			"}\n",
			"src3/Z.java",
			"public class Z {\n" +
			"  X x;\n" +
			"  Y y;\n" +
			"}\n",
		},
		sourceFilePath + " -proc:none " + commonOptions /* commandLine */,
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		null /* progress */);
	if (RUN_JAVAC) {
		// in contrast with test#232 when src1 is on the classpath, javac fails
		// to find src1/X.java; this is because -sourcepath inhibits source files
		// search in classpath directories
		Iterator javacCompilersIterator = javacCompilers.iterator();
		String sourceFileNames[] = new String[] {sourceFilePath}; 
		File outputDir = new File(OUTPUT_DIR);
		while (javacCompilersIterator.hasNext()) {
			JavacCompiler javacCompiler = (JavacCompiler) javacCompilersIterator.next();
			assertFalse(javacCompiler.compile(
					outputDir, /* directory */
					commonOptions /* options */, 
					sourceFileNames /* source file names */,
					null /* log */) == 0);
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=216684
// different from javac: with javac, newer class file down the classpath wins 
// over source file upstream, while ecj selects the first source or binary found
// in classpath order (no sourcepath involved here)
public void test235_classpath() throws IOException, InterruptedException {
	runTest(
		true /* shouldCompileOK*/,
		new String[] { /* testFiles */
			"src1/X.java",
			"public class X {\n" +
			"  public static final int CONST = 1;\n" + 
			"}\n",
			"src2/X.java",
			"public class X {\n" +
			"}\n",
		},
		"\"" + OUTPUT_DIR + File.separator + "src1" + File.separator + "X.java\"" /* commandLine */
		+ " -proc:none -d \"" + OUTPUT_DIR + File.separator + "bin1" + "\"",
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		null /* progress */);
	// ensure that class file is newer than source file (some file systems
	// store the modification time at a second precision)
	File sourceFile = new File(OUTPUT_DIR + File.separator + "src2" + File.separator + "X.java"),
	  classFile = new File(OUTPUT_DIR + File.separator + "bin1" + File.separator + "X.class");
	while (classFile.lastModified() <= sourceFile.lastModified()) {
		runTest(
			true /* shouldCompileOK*/,
			null /* testFiles */,
			"\"" + OUTPUT_DIR +  File.separator + "src1" + File.separator + "X.java\"" /* commandLine */
			+ " -proc:none -d \"" + OUTPUT_DIR + File.separator + "bin1" + "\"",
			"" /* expectedOutOutputString */,
			"" /* expectedErrOutputString */,
			false /* shouldFlushOutputDirectory */,
			null /* progress */);
	}
	// compile with (buggy) src2 before (correct) bin1 in the classpath
	String sourceFilePath = 
		"\"" + OUTPUT_DIR + File.separator + "Y.java\"";
	String commonOptions =
		" -classpath \"" + OUTPUT_DIR + File.separator + "src2"
		+ File.pathSeparator + OUTPUT_DIR + File.separator + "bin1\""
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin2" + "\"";
	runTest(
		false /* shouldCompileOK*/,
		new String[] { /* testFiles */
			"Y.java",
			"public class Y {\n" +
			"  public static void main (String[] args) {\n" +
			"    System.out.println(X.CONST);\n" +
			"  }\n" + 
			"}\n",
		},
		sourceFilePath /* commandLine */
		+ " -proc:none " + commonOptions,
		"" /* expectedOutOutputString */,
		"----------\n" + /* expectedErrOutputString */
		"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/Y.java (at line 3)\n" + 
		"	System.out.println(X.CONST);\n" + 
		"	                   ^^^^^^^\n" + 
		"X.CONST cannot be resolved\n" + 
		"----------\n" + 
		"1 problem (1 error)",
		false /* shouldFlushOutputDirectory */,
		null /* progress */);
	// javac passes, using the most recent file amongst source and class files
	// present on the classpath
	if (RUN_JAVAC) {
		Iterator javacCompilersIterator = javacCompilers.iterator();
		String sourceFileNames[] = new String[] {sourceFilePath}; 
		File outputDir = new File(OUTPUT_DIR);
		while (javacCompilersIterator.hasNext()) {
			JavacCompiler javacCompiler = (JavacCompiler) javacCompilersIterator.next();
			assertTrue(javacCompiler.compile(
					outputDir /* directory */,
					commonOptions /* options */, 
					sourceFileNames /* source file names */,
					null /* log */) == 0);
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=216684
// when class files are ready in all classpath entries, ecj and javac pick
// the first available class file up, regardless of which is newer  (no sourcepath here)
public void test236_classpath() throws IOException, InterruptedException {
	runTest(
		true /* shouldCompileOK*/,
		new String[] { /* testFiles */
			"src1/X.java",
			"public class X {\n" +
			"  public static final int CONST = 1;\n" + 
			"}\n",
		},
		"\"" + OUTPUT_DIR + File.separator + "src1" + File.separator + "X.java\"" /* commandLine */
		+ " -proc:none -d \"" + OUTPUT_DIR + File.separator + "bin1" + "\"",
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		null /* progress */);
	File bin1File = new File(OUTPUT_DIR + File.separator + "bin1" + File.separator + "X.class"),
	  bin2File = new File(OUTPUT_DIR + File.separator + "bin2" + File.separator + "X.class");
	do {
		runTest(
			true /* shouldCompileOK*/,
			new String[] { /* testFiles */
				"src2/X.java",
				"public class X {\n" +
				"  public static final int CONST = 2;\n" + 
				"}\n",
			},
			"\"" + OUTPUT_DIR + File.separator + "src2" + File.separator + "X.java\"" /* commandLine */
			+ " -proc:none -d \"" + OUTPUT_DIR + File.separator + "bin2" + "\"",
			"" /* expectedOutOutputString */,
			"" /* expectedErrOutputString */,
			false /* shouldFlushOutputDirectory */,
			null /* progress */);
	} while (bin2File.lastModified() <= bin1File.lastModified());
	String sourceFilePath = "\"" + OUTPUT_DIR +  File.separator + "Y.java\"";
	String commonOptions = 
		" -classpath \"" + OUTPUT_DIR + File.separator + "bin1" + File.pathSeparator
		+ OUTPUT_DIR + File.separator + "bin2" + "\""
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin" + "\"";
	runTest(
		true /* shouldCompileOK*/,
		new String[] { /* testFiles */
			"Y.java",
			"public class Y {\n" +
			"  public static void main (String[] args) {\n" +
			"    System.out.println(X.CONST);\n" +
			"  }\n" + 
			"}\n",
		},
		sourceFilePath + commonOptions	+ " -proc:none " /* commandLine */,
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		false /* shouldFlushOutputDirectory */,
		null /* progress */);
	this.verifier.execute("Y", new String[] {OUTPUT_DIR + File.separator + "bin"});
	assertTrue(this.verifier.getExecutionOutput().startsWith("1")); // skip trailing newline
	if (RUN_JAVAC) {
		Iterator javacCompilersIterator = javacCompilers.iterator();
		String sourceFileNames[] = new String[] {sourceFilePath}; 
		File outputDir = new File(OUTPUT_DIR);
		while (javacCompilersIterator.hasNext()) {
			JavacCompiler javacCompiler = (JavacCompiler) javacCompilersIterator.next();
			assertTrue(javacCompiler.compile(
					outputDir /* directory */,
					commonOptions /* options */, 
					sourceFileNames,
					null /* log */) == 0);
			this.verifier.execute("Y", new String[] {OUTPUT_DIR + File.separator + "bin"});
			assertEquals('1', this.verifier.getExecutionOutput().charAt(0)); // skip trailing newline
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=216684
// when a source file is more recent than a class file in a former 
// classpath entry, ecj picks the class file up, while javac choses the
// source file (no sourcepath here)
public void test237_classpath() throws IOException, InterruptedException {
	runTest(
		true /* shouldCompileOK*/,
		new String[] { /* testFiles */
			"src1/X.java",
			"public class X {\n" +
			"  public static final int CONST = 1;\n" + 
			"}\n",
		},
		"\"" + OUTPUT_DIR + File.separator + "src1" + File.separator + "X.java\"" /* commandLine */
		+ " -proc:none -d \"" + OUTPUT_DIR + File.separator + "bin1" + "\"",
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		null /* progress */);
	File sourceFile = new File(OUTPUT_DIR + File.separator + "src2" + File.separator + "X.java"),
	  classFile = new File(OUTPUT_DIR + File.separator + "bin1" + File.separator + "X.class");
	new File(OUTPUT_DIR + File.separator + "src2").mkdirs();
	do {
		Util.writeToFile(
			"public class X {\n" +
			"  public static final int CONST = 2;\n" + 
			"}\n",
			sourceFile.getPath());
	} while (classFile.lastModified() >= sourceFile.lastModified());	
	String sourceFilePath = "\"" + OUTPUT_DIR +  File.separator + "Y.java\"";
	String commonOptions = 
		" -classpath \"" + OUTPUT_DIR + File.separator + "bin1" + File.pathSeparator
		+ OUTPUT_DIR + File.separator + "src2" + "\""
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin" + "\"";
	runTest(
		true /* shouldCompileOK*/,
		new String[] { /* testFiles */
			"Y.java",
			"public class Y {\n" +
			"  public static void main (String[] args) {\n" +
			"    System.out.println(X.CONST);\n" +
			"  }\n" + 
			"}\n",
		},
		sourceFilePath + commonOptions	+ " -proc:none " /* commandLine */,
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		false /* shouldFlushOutputDirectory */,
		null /* progress */);
	this.verifier.execute("Y", new String[] {OUTPUT_DIR + File.separator + "bin"});
	assertTrue(this.verifier.getExecutionOutput().startsWith("1")); // skip trailing newline
	if (RUN_JAVAC) {
		Iterator javacCompilersIterator = javacCompilers.iterator();
		String sourceFileNames[] = new String[] {sourceFilePath}; 
		File outputDir = new File(OUTPUT_DIR);
		while (javacCompilersIterator.hasNext()) {
			JavacCompiler javacCompiler = (JavacCompiler) javacCompilersIterator.next();
			assertTrue(javacCompiler.compile(
					outputDir /* directory */,
					commonOptions /* options */, 
					sourceFileNames /* source file names */,
					null /* log */) == 0);
			this.verifier.execute("Y", new String[] {OUTPUT_DIR + File.separator + "bin"});
			assertEquals('2', this.verifier.getExecutionOutput().charAt(0)); // skip trailing newline
			// 2 means javac selected src2 (because the source file was more recent than bin1/X.class)
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=216684
// when a source file is more recent than another source file in a former 
// classpath entry, ecj and javac pick the latter file up (in other words, if
// only source files are involved, the classpath entries order prevails - no sourcepath here)
public void test238_classpath() throws IOException, InterruptedException {
	new File(OUTPUT_DIR + File.separator + "src1").mkdirs();
	File sourceFile1 = new File(OUTPUT_DIR + File.separator + "src1" + File.separator + "X.java");
	File sourceFile2 = new File(OUTPUT_DIR + File.separator + "src2" + File.separator + "X.java");
	Util.writeToFile(
		"public class X {\n" +
		"  public static final int CONST = 1;\n" + 
		"}\n",
		sourceFile1.getPath());
	new File(OUTPUT_DIR + File.separator + "src2").mkdirs();
	do {
		Util.writeToFile(
			"public class X {\n" +
			"  public static final int CONST = 2;\n" + 
			"}\n",
			sourceFile2.getPath());
	} while (sourceFile1.lastModified() >= sourceFile2.lastModified());	
	String sourceFilePath = "\"" + OUTPUT_DIR +  File.separator + "Y.java\"";
	String commonOptions = 
		" -classpath \"" + OUTPUT_DIR + File.separator + "src1" + File.pathSeparator
		+ OUTPUT_DIR + File.separator + "src2" + "\""
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin" + "\"";
	runTest(
		true /* shouldCompileOK*/,
		new String[] { /* testFiles */
			"Y.java",
			"public class Y {\n" +
			"  public static void main (String[] args) {\n" +
			"    System.out.println(X.CONST);\n" +
			"  }\n" + 
			"}\n",
		},
		sourceFilePath + commonOptions	+ " -proc:none " /* commandLine */,
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		false /* shouldFlushOutputDirectory */,
		null /* progress */);
	this.verifier.execute("Y", new String[] {OUTPUT_DIR + File.separator + "bin"});
	assertTrue(this.verifier.getExecutionOutput().startsWith("1")); // skip trailing newline
	if (RUN_JAVAC) {
		Iterator javacCompilersIterator = javacCompilers.iterator();
		String sourceFileNames[] = new String[] {sourceFilePath}; 
		File outputDir = new File(OUTPUT_DIR);
		while (javacCompilersIterator.hasNext()) {
			JavacCompiler javacCompiler = (JavacCompiler) javacCompilersIterator.next();
			assertTrue(javacCompiler.compile(
					outputDir /* directory */,
					commonOptions /* options */, 
					sourceFileNames /* source file names */,
					null /* log */) == 0);
			this.verifier.execute("Y", new String[] {OUTPUT_DIR + File.separator + "bin"});
			assertEquals('1', this.verifier.getExecutionOutput().charAt(0)); // skip trailing newline
			// 1 means javac selected src1 (because src1/X.java comes ahead of src2/X.java on the classpath)
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// basic link: a jar only referenced in the manifest of the first one is found
public void test239_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"  A a;\n" +
			"  B b;\n" +
			"}",
		},
     "\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -cp \"" + LIB_DIR + File.separator + "lib1.jar\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
     + " -1.5 -g -preserveAllLocals"
     + " -proceedOnError -referenceInfo"
     + " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
     "",
     "",
     true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// links are followed recursively, eliminating dupes
public void test240_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"  A a;\n" +
			"  B b;\n" +
			"  C c;\n" +
			"  D d;\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -cp \"" + LIB_DIR + File.separator + "lib3.jar\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// at first level, this is depth first, masking tailing libs
public void test241_jar_ref_in_jar(){
	createCascadedJars();
	this.runNegativeTest(
		new String[] {
			"src/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"  int i = R.R2;\n" +
			"  int j = R.R3;\n" +
			"}",
		},
	  "\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
			+ " -cp \"" + LIB_DIR + File.separator + "lib1.jar\""
			+ " -cp \"" + LIB_DIR + File.separator + "lib3.jar\""
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
	  + " -1.5 -g -preserveAllLocals"
	  + " -proceedOnError -referenceInfo"
	  + " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
	  "",
	  "----------\n" + 
	  "1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 5)\n" + 
	  "	int j = R.R3;\n" + 
	  "	        ^^^^\n" + 
	  "R.R3 cannot be resolved\n" + 
	  "----------\n" + 
	  "1 problem (1 error)",
	  true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// using only links, we adopt a depth first algorithm
public void test242_jar_ref_in_jar(){
	createCascadedJars();
	this.runNegativeTest(
		new String[] {
			"src/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"  int i = R.R2;\n" +
			"  int j = R.R3;\n" +
			"}",
		},
	  "\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
			+ " -cp \"" + LIB_DIR + File.separator + "lib4.jar\""
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
	  + " -1.5 -g -preserveAllLocals"
	  + " -proceedOnError -referenceInfo"
	  + " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
	  "",
	  "----------\n" + 
	  "1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 5)\n" + 
	  "	int j = R.R3;\n" + 
	  "	        ^^^^\n" + 
	  "R.R3 cannot be resolved\n" + 
	  "----------\n" + 
	  "1 problem (1 error)",
	  true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// managing subdirectories and .. properly
public void test243_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"  A a;\n" +
			"  B b;\n" +
			"  C c;\n" +
			"  D d;\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -cp \"" + LIB_DIR + File.separator + "lib5.jar\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// variant: the second jar on a line is found as well
public void test244_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"  C c;\n" +
			"}",
		},
  "\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -cp \"" + LIB_DIR + File.separator + "lib4.jar\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
  + " -1.5 -g -preserveAllLocals"
  + " -proceedOnError -referenceInfo"
  + " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
  "",
  "",
  true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// we eat up absolute links silently
public void test245_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"  F f;\n" +
			"}",
		},
	"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
			+ " -cp \"" + LIB_DIR + File.separator + "lib8.jar\""
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
	+ " -1.5 -g -preserveAllLocals"
	+ " -proceedOnError -referenceInfo"
	+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
	"",
	"",
	true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// absolute links do not mask following relative links
public void test246_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"  A a;\n" +
			"  F f;\n" +
			"}",
		},
	"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
			+ " -cp \"" + LIB_DIR + File.separator + "lib8.jar\""
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
	+ " -1.5 -g -preserveAllLocals"
	+ " -proceedOnError -referenceInfo"
	+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
	"",
	"",
	true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// absolute links are not followed
public void test247_jar_ref_in_jar(){
	createCascadedJars();
	this.runNegativeTest(
		new String[] {
			"src/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"  C c;\n" +
			"}",
		},
	"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
			+ " -cp \"" + LIB_DIR + File.separator + "lib8.jar\""
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
	+ " -1.5 -g -preserveAllLocals"
	+ " -proceedOnError -referenceInfo"
	+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
	"",
	"----------\n" + 
	"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 4)\n" + 
	"	C c;\n" + 
	"	^\n" + 
	"C cannot be resolved to a type\n" + 
	"----------\n" + 
	"1 problem (1 error)",
	true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// we accept duplicate classpath lines in manifest and we follow the jars of the
// second and following lines as well as the first line (emit a warning as javac does)
public void test248_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"  A a;\n" +
			"  G g;\n" +
			"}",
		},
	"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
			+ " -cp \"" + LIB_DIR + File.separator + "lib9.jar\""
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
	+ " -1.5 -g -preserveAllLocals"
	+ " -proceedOnError -referenceInfo"
	+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
	"",
	"multiple Class-Path headers in manifest of jar file: ---LIB_DIR_PLACEHOLDER---/lib9.jar\n",
	true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// we accept duplicate classpath lines in manifest and we follow the jars of the
// second and following lines as well as the first line (emit a warning as javac does)
public void test249_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"  C c;\n" +
			"  G g;\n" +
			"}",
		},
	"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
			+ " -cp \"" + LIB_DIR + File.separator + "lib9.jar\""
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
	+ " -1.5 -g -preserveAllLocals"
	+ " -proceedOnError -referenceInfo"
	+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
	"",
	"multiple Class-Path headers in manifest of jar file: ---LIB_DIR_PLACEHOLDER---/lib9.jar\n",
	true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// bootclasspath does not get expanded with linked files
public void test250_jar_ref_in_jar(){
	createCascadedJars();
	this.runNegativeTest(
		new String[] {
			"src/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"  A a;\n" +
			"  B b;\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
	  	+ " -bootclasspath " + getLibraryClassesAsQuotedString() 
	  	+ File.pathSeparator + "\"" + LIB_DIR + File.separator + "lib1.jar\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"----------\n" + 
		"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 5)\n" + 
		"	B b;\n" + 
		"	^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n" + 
		"1 problem (1 error)",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// jar files reached indirectly bear the access rules of the entry that 
// references them
public void test251_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"  A a;\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -cp \"" + LIB_DIR + File.separator + "lib3.jar[~p/A]\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 4)\n" + 
		"	A a;\n" + 
		"	^\n" + 
		"Discouraged access: The type A is not accessible due to restriction on classpath entry ---LIB_DIR_PLACEHOLDER---/lib3.jar\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=217233
// compiler progress test (1 unit)
public void test252_progress() {
	runProgressTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"}\n",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -d \"" + OUTPUT_DIR + "\"",
		""/*out output*/,
		""/*err output*/,
		"----------\n" + 
		"[worked: 0 - remaining: 1]\n" + 
		"Beginning to compile\n" + 
		"Processing ---OUTPUT_DIR_PLACEHOLDER---/X.java\n" + 
		"[worked: 1 - remaining: 0]\n" + 
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=217233
// compiler progress test (2 units)
public void test253_progress() {
	runProgressTest(
		new String[] {
			"Y.java",
			"public class Y {\n" + 
			"}\n",
			"X.java",
			"public class X extends Y {\n" + 
			"}\n",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -cp " + File.pathSeparator + File.pathSeparator + "\"" + OUTPUT_DIR + "\""
		+ " -d \"" + OUTPUT_DIR + "\"",
		""/*out output*/,
		""/*err output*/,
		"----------\n" + 
		"[worked: 0 - remaining: 1]\n" + 
		"Beginning to compile\n" + 
		"Processing ---OUTPUT_DIR_PLACEHOLDER---/X.java\n" + 
		"[worked: 1 - remaining: 1]\n" + 
		"Processing ---OUTPUT_DIR_PLACEHOLDER---/Y.java\n" + 
		"[worked: 2 - remaining: 0]\n" + 
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=217233
// compiler progress test (multiple iterations)
public void test254_progress() {
	runProgressTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"}\n",
			"Y.java",
			"public class Y {\n" + 
			"}\n",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " \"" + OUTPUT_DIR +  File.separator + "Y.java\""
        + " -cp " + File.pathSeparator + File.pathSeparator + "\"" + OUTPUT_DIR + "\""
		+ " -d \"" + OUTPUT_DIR + "\""
		+ " -repeat 3",
		"[repetition 1/3]\n" + 
		"[repetition 2/3]\n" + 
		"[repetition 3/3]\n"/*out output*/,
		""/*err output*/,
		"----------\n" + 
		"[worked: 0 - remaining: 6]\n" + 
		"Beginning to compile\n" + 
		"Processing ---OUTPUT_DIR_PLACEHOLDER---/X.java\n" + 
		"[worked: 1 - remaining: 5]\n" + 
		"Processing ---OUTPUT_DIR_PLACEHOLDER---/Y.java\n" + 
		"[worked: 2 - remaining: 4]\n" + 
		"Beginning to compile\n" + 
		"Processing ---OUTPUT_DIR_PLACEHOLDER---/X.java\n" + 
		"[worked: 3 - remaining: 3]\n" + 
		"Processing ---OUTPUT_DIR_PLACEHOLDER---/Y.java\n" + 
		"[worked: 4 - remaining: 2]\n" + 
		"Beginning to compile\n" + 
		"Processing ---OUTPUT_DIR_PLACEHOLDER---/X.java\n" + 
		"[worked: 5 - remaining: 1]\n" + 
		"Processing ---OUTPUT_DIR_PLACEHOLDER---/Y.java\n" + 
		"[worked: 6 - remaining: 0]\n" + 
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=217233
// compiler progress test (cancellation)
public void test255_progress() {
	TestCompilationProgress progress = new TestCompilationProgress() {
		public void worked(int workIncrement, int remainingWork) {
			if (remainingWork == 1)
				this.isCanceled = true;
			super.worked(workIncrement, remainingWork);
		}
	};
	String setting= System.getProperty("jdt.compiler.useSingleThread");
	try {
		System.setProperty("jdt.compiler.useSingleThread", "true");
		runProgressTest(
			false/*shouldCompileOK*/,
			new String[] {
				"Y.java",
				"public class Y {\n" + 
				"}\n",
				"X.java",
				"public class X extends Y {\n" + 
				"}\n",
			},
			"\"" + OUTPUT_DIR +  File.separator + "X.java\""
	        + " -cp " + File.pathSeparator + File.pathSeparator + "\"" + OUTPUT_DIR + "\""
			+ " -d \"" + OUTPUT_DIR + "\"",
			""/*out output*/,
			""/*err output*/,
			progress,
			"----------\n" + 
			"[worked: 0 - remaining: 1]\n" + 
			"Beginning to compile\n" + 
			"Processing ---OUTPUT_DIR_PLACEHOLDER---/X.java\n" + 
			"[worked: 1 - remaining: 1]\n" + 
			"----------\n"
		);
	} finally {
		System.setProperty("jdt.compiler.useSingleThread", setting == null ? "false" : setting);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// jar files reached indirectly bear the access rules of the entry that 
// references them - this hides the access rules of further instances of the
// same jar on the classpath
public void test256_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"  A a;\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -cp \"" + LIB_DIR + File.separator + "lib3.jar[~p/A]\""
		+ " -cp \"" + LIB_DIR + File.separator + "lib1.jar[-p/A]\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 4)\n" + 
		"	A a;\n" + 
		"	^\n" + 
		"Discouraged access: The type A is not accessible due to restriction on classpath entry ---LIB_DIR_PLACEHOLDER---/lib3.jar\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// jar files reached indirectly bear the access rules of the entry that 
// references them - this hides the access rules of further instances of the
// same jar on the classpath
public void test257_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"  A a;\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -cp \"" + LIB_DIR + File.separator + "lib3.jar[-DUMMY]\""
		+ " -cp \"" + LIB_DIR + File.separator + "lib1.jar[-p/A]\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// jar files reached indirectly bear the access rules of the entry that 
// references them - this hides the access rules of further instances of the
// same jar on the classpath, to the point of absorbing it if none is specified
public void test258_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"  A a;\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -cp \"" + LIB_DIR + File.separator + "lib3.jar\""
		+ " -cp \"" + LIB_DIR + File.separator + "lib1.jar[-p/A]\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// -sourcepath is OK at first level
public void test259_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"  S1 s;\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -sourcepath \"" + LIB_DIR + File.separator + "lib1.jar\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// -sourcepath is KO at second level (that is, it does not leverage the links
// at all)
public void test260_jar_ref_in_jar(){
	createCascadedJars();
	this.runNegativeTest(
		new String[] {
			"src/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"  S2 s;\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -sourcepath \"" + LIB_DIR + File.separator + "lib1.jar\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"----------\n" + 
		"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 4)\n" + 
		"	S2 s;\n" + 
		"	^^\n" + 
		"S2 cannot be resolved to a type\n" + 
		"----------\n" + 
		"1 problem (1 error)",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// error case: the MANIFEST.MF is a directory; should fail gracefully
public void test261_jar_ref_in_jar(){
	createCascadedJars();
	this.runNegativeTest(
		new String[] {
			"src/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"  A a;\n" +
			"  B b;\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
				+ " -cp \"" + LIB_DIR + File.separator + "lib10.jar\""
				+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"----------\n" + 
		"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 5)\n" + 
		"	B b;\n" + 
		"	^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n" + 
		"1 problem (1 error)",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// using relative paths for libs
public void test262_jar_ref_in_jar(){
	String currentWorkingDirectoryPath = System.getProperty("user.dir");
	if (currentWorkingDirectoryPath == null) {
		System.err.println("BatchCompilerTest#235 could not access the current working directory " + currentWorkingDirectoryPath);
	} else if (!new File(currentWorkingDirectoryPath).isDirectory()) {
		System.err.println("BatchCompilerTest#235 current working directory is not a directory " + currentWorkingDirectoryPath);
	} else {
		String lib1Path = currentWorkingDirectoryPath + File.separator + "lib1.jar";
		String lib2Path = currentWorkingDirectoryPath + File.separator + "lib2.jar";
		try {
			Util.createJar(
				null,
				new String[] {
					"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n" +
					"Created-By: Eclipse JDT Test Harness\n" +
					"Class-Path: lib2.jar\n",
				},
				lib1Path,
				JavaCore.VERSION_1_4);
			Util.createJar(
				new String[] {
					"p/A.java",
					"package p;\n" +
					"public class A {\n" +
					"}",
				},
				null,
				lib2Path,
				JavaCore.VERSION_1_4);
			this.runConformTest(
				new String[] {
					"src/p/X.java",
					"package p;\n" +
					"/** */\n" +
					"public class X {\n" +
					"  A a;\n" +
					"}",
				},
		        "\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
				+ " -cp lib1.jar" // relative
				+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		        + " -1.5 -g -preserveAllLocals"
		        + " -proceedOnError -referenceInfo"
		        + " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		        "",
		        "",
		        true);
		} catch (IOException e) {
			System.err.println("BatchCompilerTest#235 could not write to current working directory " + currentWorkingDirectoryPath);
		} finally {
			new File(lib1Path).delete();
			new File(lib2Path).delete();
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// empty Class-Path header
// javac 1.4.2 passes, later versions fail in error
// java accepts the same jar (which makes the compiler responsible for the 
// error detection)
// design: will issue a warning
public void test263_jar_ref_in_jar(){
	createCascadedJars();
	this.runTest(
		true,
		new String[] {
			"src/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"  A a;\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -classpath \"" + LIB_DIR + File.separator + "lib11.jar\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -verbose -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		ONE_FILE_GENERATED_MATCHER,
		new StringMatcher(
			"invalid Class-Path header in manifest of jar file: ---LIB_DIR_PLACEHOLDER---/lib11.jar\n", 
			outputDirNormalizer),
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// missing space after ClassPath:
public void test264_jar_ref_in_jar(){
	createCascadedJars();
	this.runTest(
		false,
		new String[] {
			"src/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"  A a;\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -classpath \"" + LIB_DIR + File.separator + "lib12.jar\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"invalid Class-Path header in manifest of jar file: ---LIB_DIR_PLACEHOLDER---/lib12.jar\n" + 
		"----------\n" + 
		"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 4)\n" + 
		"	A a;\n" + 
		"	^\n" + 
		"A cannot be resolved to a type\n" + 
		"----------\n" + 
		"1 problem (1 error)",
		true,
		null /* progress */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// missing space after ClassPath
// javac reports an error (including an explicit manifest header error since
// version 1.5); moreover, it stops interpreting the said header
// design: we report a warning and eat up the remainding of the line
public void test265_jar_ref_in_jar(){
	createCascadedJars();
	this.runTest(
		false,
		new String[] {
			"src/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"  A a;\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -classpath \"" + LIB_DIR + File.separator + "lib13.jar\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"invalid Class-Path header in manifest of jar file: ---LIB_DIR_PLACEHOLDER---/lib13.jar\n" + 
		"----------\n" + 
		"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 4)\n" + 
		"	A a;\n" + 
		"	^\n" + 
		"A cannot be resolved to a type\n" + 
		"----------\n" + 
		"1 problem (1 error)",
		true,
		null /* progress */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// extra space before Class-Path header
// the net result is that the line is part of the value of the previous header
// we then simply don't see the remainding of the line as jars
public void test266_jar_ref_in_jar(){
	createCascadedJars();
	this.runTest(
		false,
		new String[] {
			"src/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"  A a;\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -classpath \"" + LIB_DIR + File.separator + "lib14.jar\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"----------\n" + 
		"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 4)\n" + 
		"	A a;\n" + 
		"	^\n" + 
		"A cannot be resolved to a type\n" + 
		"----------\n" + 
		"1 problem (1 error)",
		true,
		null /* progress */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// missing newline at the end of the line
// javac eats the line silently, which results into not finding A
// design: we report a warning and eat up the remainding of the line
public void test267_jar_ref_in_jar(){
	createCascadedJars();
	this.runTest(
		false,
		new String[] {
			"src/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"  A a;\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -classpath \"" + LIB_DIR + File.separator + "lib15.jar\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"invalid Class-Path header in manifest of jar file: ---LIB_DIR_PLACEHOLDER---/lib15.jar\n" + 
		"----------\n" + 
		"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 4)\n" + 
		"	A a;\n" + 
		"	^\n" + 
		"A cannot be resolved to a type\n" + 
		"----------\n" + 
		"1 problem (1 error)",
		true,
		null /* progress */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// white-box test for duplicate classpath lines variant (empty line between the
// entries)
//public void test268_jar_ref_in_jar(){
//	try {
//		ManifestAnalyzer analyzer = new ManifestAnalyzer();
//		assertTrue(analyzer.analyzeManifestContents(
//			new StringReader(
//				"Manifest-Version: 1.0\n" +
//				"Created-By: Eclipse JDT Test Harness\n" +
//				"Class-Path: lib1.jar\n" +
//				"\n" +
//				"Class-Path: lib3.jar\n")));
//		assertEquals(2, analyzer.getClasspathSectionsCount());
//		assertEquals(2, analyzer.getCalledFileNames().size());
//	} catch (IOException e) {
//		e.printStackTrace();
//		fail();
//	}
//}
//// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
//// white-box test for duplicate classpath lines variant (other header between the
//// entries - note that since we are not doing a full-fledged manifest analysis,
//// a dummy header passes)
//public void test269_jar_ref_in_jar(){
//	try {
//		ManifestAnalyzer analyzer = new ManifestAnalyzer();
//		assertTrue(analyzer.analyzeManifestContents(
//			new StringReader(
//				"Manifest-Version: 1.0\n" +
//				"Created-By: Eclipse JDT Test Harness\n" +
//				"Class-Path: lib1.jar\n" +
//				"Dummy:\n" +
//				"Class-Path: lib3.jar\n")));
//		assertEquals(2, analyzer.getClasspathSectionsCount());
//		assertEquals(2, analyzer.getCalledFileNames().size());
//	} catch (IOException e) {
//		e.printStackTrace();
//		fail();
//	}
//}
//// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
//// white-box test: tabs are not seen as URI separator, but as parts of URI instead
//// will trigger downstream errors if the jars are really needed
//public void test270_jar_ref_in_jar(){
//	try {
//		ManifestAnalyzer analyzer = new ManifestAnalyzer();
//		assertTrue(analyzer.analyzeManifestContents(
//			new StringReader(
//				"Manifest-Version: 1.0\n" +
//				"Created-By: Eclipse JDT Test Harness\n" +
//				"Class-Path: lib1.jar\tlib2.jar\n")));
//		assertEquals(1, analyzer.getClasspathSectionsCount());
//		assertEquals(1, analyzer.getCalledFileNames().size());
//	} catch (IOException e) {
//		e.printStackTrace();
//		fail();
//	}
//}
//// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
//// managing continuations properly
//public void test271_jar_ref_in_jar(){
//	createCascadedJars();
//	this.runConformTest(
//		new String[] {
//			"src/p/X.java",
//			"package p;\n" +
//			"/** */\n" +
//			"public class X {\n" +
//			"  A a;\n" +
//			"  B b;\n" +
//			"}",
//		},
//		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
//				+ " -cp \"" + LIB_DIR + File.separator + "lib16.jar\""
//				+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
//		+ " -1.5 -g -preserveAllLocals"
//		+ " -proceedOnError -referenceInfo"
//		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
//		"",
//		"",
//		true);
//}
//// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
//// white-box test: variants on continuations
//public void test272_jar_ref_in_jar(){
//	try {
//		ManifestAnalyzer analyzer = new ManifestAnalyzer();
//		assertTrue(analyzer.analyzeManifestContents(
//			new StringReader(
//				"Manifest-Version: 1.0\n" +
//				"Created-By: Eclipse JDT Test Harness\n" +
//				"Class-Path: \n" +
//				"            lib1.jar       \n" +
//				"\n")));
//		assertEquals(1, analyzer.getClasspathSectionsCount());
//		assertEquals(1, analyzer.getCalledFileNames().size());
//	} catch (IOException e) {
//		e.printStackTrace();
//		fail();
//	}
//}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// white-box test: variants on continuations
//public void test273_jar_ref_in_jar(){
//	try {
//		ManifestAnalyzer analyzer = new ManifestAnalyzer();
//		assertTrue(analyzer.analyzeManifestContents(
//			new StringReader(
//				"Manifest-Version: 1.0\n" +
//				"Created-By: Eclipse JDT Test Harness\n" +
//				"Class-Path: \n" +
//				" \n" +
//				"            lib1.jar       \n" +
//				" \n" +
//				"            lib1.jar       \n" +
//				"\n")));
//		assertEquals(1, analyzer.getClasspathSectionsCount());
//		assertEquals(2, analyzer.getCalledFileNames().size());
//	} catch (IOException e) {
//		e.printStackTrace();
//		fail();
//	}
//}
//// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
//// white-box test: variants on continuations
//public void test274_jar_ref_in_jar(){
//	try {
//		assertFalse(new ManifestAnalyzer().analyzeManifestContents(
//			new StringReader(
//				"Manifest-Version: 1.0\n" +
//				"Created-By: Eclipse JDT Test Harness\n" +
//				"Class-Path: \n" +
//				"            lib1.jar")));
//	} catch (IOException e) {
//		e.printStackTrace();
//		fail();
//	}
//}
//// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
//// white-box test: variants on continuations
//public void test275_jar_ref_in_jar(){
//	try {
//		assertFalse(new ManifestAnalyzer().analyzeManifestContents(
//			new StringReader(
//				"Manifest-Version: 1.0\n" +
//				"Created-By: Eclipse JDT Test Harness\n" +
//				"Class-Path: \n" +
//				" \n" +
//				"            lib1.jar")));
//	} catch (IOException e) {
//		e.printStackTrace();
//		fail();
//	}
//}
//// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
//// white-box test: variants on continuations
//public void test276_jar_ref_in_jar(){
//	try {
//		assertFalse(new ManifestAnalyzer().analyzeManifestContents(
//			new StringReader(
//				"Manifest-Version: 1.0\n" +
//				"Created-By: Eclipse JDT Test Harness\n" +
//				"Class-Path:      \n" +
//				"lib1.jar")));
//	} catch (IOException e) {
//		e.printStackTrace();
//		fail();
//	}
//}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// extdirs jars do not follow links
public void test277_jar_ref_in_jar(){
	createCascadedJars();
	this.runNegativeTest(
		new String[] {
			"src/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"  A a;\n" +
			"  B b;\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
	  	+ " -extdirs \"" + LIB_DIR + File.separator + "dir\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"----------\n" + 
		"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 5)\n" + 
		"	B b;\n" + 
		"	^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n" + 
		"1 problem (1 error)",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// endorseddirs does not get expanded with linked files
public void test278_jar_ref_in_jar(){
	createCascadedJars();
	this.runNegativeTest(
		new String[] {
			"src/p/X.java",
			"package p;\n" +
			"/** */\n" +
			"public class X {\n" +
			"  A a;\n" +
			"  B b;\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
	  	+ " -endorseddirs \"" + LIB_DIR + File.separator + "dir\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"----------\n" + 
		"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 5)\n" + 
		"	B b;\n" + 
		"	^\n" + 
		"B cannot be resolved to a type\n" + 
		"----------\n" + 
		"1 problem (1 error)",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=216684
// looking at access rules: ignore if better makes the class file selected if
// it is newer, but see test#280 for what happens when it is not
public void test279_sourcepath_vs_classpath() throws IOException, InterruptedException {
	runTest(
		true /* shouldCompileOK*/,
		new String[] { /* testFiles */
			"src1/X.java",
			"public class X {\n" +
			"  public static final int CONST = 1;\n" + 
			"}\n",
			"src2/X.java",
			"public class X {\n" +
			"  public static final int CONST = 2;\n" + 
			"}\n",
		},
		"\"" + OUTPUT_DIR + File.separator + "src1" + File.separator + "X.java\"" /* commandLine */
		+ " -proc:none -d \"" + OUTPUT_DIR + File.separator + "bin1" + "\"",
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		null /* progress */);
	// ensure that bin1/X.class file is newer than src2/X.java (some file systems
	// store the modification time at a second precision)
	File sourceFile = new File(OUTPUT_DIR + File.separator + "src2" + File.separator + "X.java"),
	  classFile = new File(OUTPUT_DIR + File.separator + "bin1" + File.separator + "X.class");
	while (classFile.lastModified() <= sourceFile.lastModified()) {
		runConformTest(
			null,
			"\"" + OUTPUT_DIR +  File.separator + "src1" + File.separator + "X.java\""
			+ " -proc:none -d \"" + OUTPUT_DIR + File.separator + "bin1" + "\"",
			"",
			"",
			false);
	}
	// the ignore if better rule upon src2 leads to bin1 being selected
	runConformTest(
		new String[] {
			"Y.java",
			"public class Y {\n" +
			"  public static void main (String[] args) {\n" +
			"    System.out.println(X.CONST);\n" +
			"  }\n" + 
			"}\n",
		},
		"\"" + OUTPUT_DIR +  File.separator + "Y.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + File.separator + "src2[?**/*]" + "\""
		+ " -classpath \"" + OUTPUT_DIR + File.separator + "bin1" + "\""
		+ " -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		false);
	this.verifier.execute("Y", new String[] {OUTPUT_DIR });
	assertTrue(this.verifier.getExecutionOutput().startsWith("1")); // skip trailing newline
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=216684
// looking at access rules: ignore if better makes the class file selected even
// if it is older (in test#279 it was newer); access rules are thus no work 
// around since they ignore modification dates
public void test280_sourcepath_vs_classpath() throws IOException, InterruptedException {
	runTest(
		true /* shouldCompileOK*/,
		new String[] { /* testFiles */
			"src1/X.java",
			"public class X {\n" +
			"  public static final int CONST = 1;\n" + 
			"}\n",
		},
		"\"" + OUTPUT_DIR + File.separator + "src1" + File.separator + "X.java\"" /* commandLine */
		+ " -proc:none -d \"" + OUTPUT_DIR + File.separator + "bin1" + "\"",
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		null /* progress */);
	// ensure that bin1/X.class file is older than src2/X.java (some file systems
	// store the modification time at a second precision)
	File sourceFile = new File(OUTPUT_DIR + File.separator + "src2" + File.separator + "X.java"),
	  classFile = new File(OUTPUT_DIR + File.separator + "bin1" + File.separator + "X.class");
	new File(OUTPUT_DIR + File.separator + "src2").mkdirs();
	do {
		Util.writeToFile(
			"public class X {\n" +
			"  public static final int CONST = 2;\n" + 
			"}\n",
			sourceFile.getPath());
	} while (classFile.lastModified() >= sourceFile.lastModified());
	// the ignore if better rule upon src2 leads to bin1 being selected even if
	// src2/X.java is newer
	runConformTest(
		new String[] {
			"Y.java",
			"public class Y {\n" +
			"  public static void main (String[] args) {\n" +
			"    System.out.println(X.CONST);\n" +
			"  }\n" + 
			"}\n",
		},
		"\"" + OUTPUT_DIR +  File.separator + "Y.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + File.separator + "src2[?**/*]" + "\""
		+ " -classpath \"" + OUTPUT_DIR + File.separator + "bin1" + "\""
		+ " -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		false);
	this.verifier.execute("Y", new String[] {OUTPUT_DIR });
	assertTrue(this.verifier.getExecutionOutput().startsWith("1")); // skip trailing newline
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=182360
public void test281_classpath() {
	runConformTest(
		new String[] {
			"src1/X.java",
			"public class X {\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "src1/X.java\" -cp Y.java",
        "" /* expectedOutOutputString */,
        "incorrect classpath: Y.java\n",
        false/*shouldFlushOutput*/);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=182360
public void test282_classpath() {
	runConformTest(
		new String[] {
			"src1/X.java",
			"public class X {\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "src1/X.java\" -cp p/Y.java",
        "" /* expectedOutOutputString */,
        "incorrect classpath: p/Y.java\n",
        false/*shouldFlushOutput*/);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=182360
public void test283_classpath() {
	runConformTest(
		new String[] {
			"src1/X.java",
			"public class X {\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "src1/X.java\" -cp Y.class",
        "" /* expectedOutOutputString */,
        "incorrect classpath: Y.class\n",
        false/*shouldFlushOutput*/);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=182360
public void test284_classpath() {
	runConformTest(
		new String[] {
			"src1/X.java",
			"public class X {\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "src1/X.java\" -cp p/Y.class",
        "" /* expectedOutOutputString */,
        "incorrect classpath: p/Y.class\n",
        false/*shouldFlushOutput*/);
}

// command-line expansion
public void test285_option_files() {
	runConformTest(
		new String[] {
			"X.java",
			"public @interface X {\n" +
			"}",
			"options.txt",
			"-source 1.5"
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\" " +
        "\"@" + OUTPUT_DIR +  File.separator + "options.txt\"",
        "" /* expectedOutOutputString */,
        "" /* stderr */,
        true /*shouldFlushOutput*/);
}

// command-line expansion
public void test286_option_files() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public @interface X {\n" +
			"}",
			"options.txt",
			"-source 1.4"
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\" " +
        "\"@" + OUTPUT_DIR +  File.separator + "options.txt\"",
        "" /* expectedOutOutputString */,
        "----------\n" + /* stderr */
        "1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)\n" + 
        "	public @interface X {\n" + 
        "	                  ^\n" + 
        "Syntax error, annotation declarations are only available if source level is 1.5\n" + 
        "----------\n" + 
        "1 problem (1 error)",
        true /*shouldFlushOutput*/);
}

// command-line expansion
// shows that we don't recurse
public void test287_option_files() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public @interface X {\n" +
			"}",
			"options1.txt",
			"@options2.txt",
			"options2.txt",
			"@options1.txt"
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\" " +
        "\"@" + OUTPUT_DIR +  File.separator + "options1.txt\"",
        "" /* expectedOutOutputString */,
        "Unrecognized option : @options2.txt\n" /* stderr */,
        true /*shouldFlushOutput*/);
}
}
