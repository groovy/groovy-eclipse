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
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contribution for bug 185682 - Increment/decrement operators mark local variables as read
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class JavadocTest extends AbstractRegressionTest {

	boolean useLibrary = false;
	static String ZIP_FILE = "/TestJavadocVisibility.zip";
	static final String LINE_SEPARATOR = System.getProperty("line.separator");
	public static ArrayList ALL_CLASSES = null;
	static final String DOC_COMMENT_SUPPORT = System.getProperty("doc.support");

	// Javadoc execution
	protected static final String JAVADOC_NAME =
		File.pathSeparatorChar == ':' ? "javadoc" : "javadoc.exe";
  protected static String javadocCommandLineHeader;

	static {
		ALL_CLASSES = new ArrayList();
		ALL_CLASSES.add(JavadocBugsTest.class);
		ALL_CLASSES.add(JavadocTestForMethod.class);
		ALL_CLASSES.add(JavadocTestMixed.class);
		ALL_CLASSES.add(JavadocTestForClass.class);
		ALL_CLASSES.add(JavadocTestForConstructor.class);
		ALL_CLASSES.add(JavadocTestForField.class);
		ALL_CLASSES.add(JavadocTestForInterface.class);
		ALL_CLASSES.add(JavadocTestOptions.class);
	}


	public static void addTest(TestSuite suite, Class testClass) {
		TestSuite innerSuite = new TestSuite(testClass);
		suite.addTest(innerSuite);
	}

	public static Test suite() {
		TestSuite testSuite = new TestSuite(JavadocTest.class.getName());

		// Reset forgotten subsets of tests
		TestCase.TESTS_PREFIX = null;
		TestCase.TESTS_NAMES = null;
		TestCase.TESTS_NUMBERS = null;
		TestCase.TESTS_RANGE = null;
		TestCase.RUN_ONLY_ID = null;

		for (int i = 0, size=ALL_CLASSES.size(); i < size; i++) {
			Class testClass = (Class) ALL_CLASSES.get(i);
			Test suite = buildAllCompliancesTestSuite(testClass);
			testSuite.addTest(suite);
		}
		int complianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
		if ((complianceLevels & AbstractCompilerTest.F_1_3) != 0) {
			testSuite.addTest(buildUniqueComplianceTestSuite(JavadocTest_1_3.class, ClassFileConstants.JDK1_3));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_4) != 0) {
			testSuite.addTest(buildUniqueComplianceTestSuite(JavadocTest_1_4.class, ClassFileConstants.JDK1_4));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_5) != 0) {
			testSuite.addTest(buildUniqueComplianceTestSuite(JavadocTest_1_5.class, ClassFileConstants.JDK1_5));
		}
		if ((complianceLevels & AbstractCompilerTest.F_9) != 0) {
			testSuite.addTest(buildUniqueComplianceTestSuite(JavadocTestForModule.class, ClassFileConstants.JDK9));
		}
		if ((complianceLevels & AbstractCompilerTest.F_14) != 0) {
			testSuite.addTest(buildUniqueComplianceTestSuite(JavadocTestForRecord.class, ClassFileConstants.JDK14));
		}
		if ((complianceLevels & AbstractCompilerTest.F_15) != 0) {
			testSuite.addTest(buildUniqueComplianceTestSuite(JavadocTest_15.class, ClassFileConstants.JDK15));
		}
		if ((complianceLevels & AbstractCompilerTest.F_16) != 0) {
			testSuite.addTest(buildUniqueComplianceTestSuite(JavadocTest_15.class, ClassFileConstants.JDK16));
		}
		return testSuite;
	}

	public static Test suiteForComplianceLevel(long level, Class testClass) {
		TestSuite suite = new RegressionTestSetup(level);
		buildAllCompliancesTestSuite(suite, testClass);
		return suite;
	}

	public JavadocTest(String name) {
		super(name);
	}
	@Override
	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
		// Set default before bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=110964 changes
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, CompilerOptions.PRIVATE);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTags, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsDeprecatedRef, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsNotVisibleRef, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsVisibility, CompilerOptions.PRIVATE);
		options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
		return options;
	}

	@Override
	protected String[] getDefaultClassPaths() {
		if (this.useLibrary) {
			String[] classLibs = super.getDefaultClassPaths();
			final int length = classLibs.length;
			String[] newClassPaths = new String[length + 1];
			System.arraycopy(classLibs, 0, newClassPaths, 0, length);
			newClassPaths[length] = getClass().getResource(ZIP_FILE).getPath();
			return newClassPaths;
		}
		return super.getDefaultClassPaths();
	}

	static String[] referencedClasses = null;
	static {
		referencedClasses =
			new String[] {
				"test/AbstractVisibility.java",
				"package test;\n" +
				"public abstract class AbstractVisibility {\n" +
				"	private class AvcPrivate {\n" +
				"		private int avf_private = 10;\n" +
				"		public int avf_public = avf_private;\n" +
				"		private int avm_private() {\n" +
				"			avf_private = (new AvcPrivate()).avf_private;\n" +
				"			return avf_private;\n" +
				"		}\n" +
				"		public int avm_public() {\n" +
				"			return avm_private();\n" +
				"		}\n" +
				"	}\n" +
				"	public class AvcPublic {\n" +
				"		private int avf_private = 10;\n" +
				"		public int avf_public = avf_private;\n" +
				"		private int avm_private() {\n" +
				"			avf_private = (new AvcPrivate()).avf_private;\n" +
				"			return avf_private;\n" +
				"		}\n" +
				"		public int avm_public() {\n" +
				"			return avm_private();\n" +
				"		}\n" +
				"	}\n" +
				"	private int avf_private = 100;\n" +
				"	public int avf_public = avf_private;\n" +
				"	\n" +
				"	private int avm_private() {\n" +
				"		avf_private = (new AvcPrivate()).avf_private;\n" +
				"		return avf_private;\n" +
				"	}\n" +
				"	public int avm_public() {\n" +
				"		return avm_private();\n" +
				"	}\n" +
				"}\n",
				"test/Visibility.java",
				"package test;\n" +
				"public class Visibility extends AbstractVisibility {\n" +
				"	private class VcPrivate {\n" +
				"		private int vf_private = 10;\n" +
				"		public int vf_public = vf_private;\n" +
				"		private int vm_private() {\n" +
				"			vf_private = (new VcPrivate()).vf_private;\n" +
				"			avf_private = vf_private;\n" +
				"			return vf_private+avf_private;\n" +
				"		}\n" +
				"		public int vm_public() {\n" +
				"			return vm_private();\n" +
				"		}\n" +
				"	};\n" +
				"	public class VcPublic {\n" +
				"		private int vf_private = 10;\n" +
				"		public int vf_public = vf_private;\n" +
				"		private int vm_private() {\n" +
				"			vf_private = (new VcPrivate()).vf_private;\n" +
				"			avf_private = vf_private;\n" +
				"			return vf_private+avf_private;\n" +
				"		}\n" +
				"		public int vm_public() {\n" +
				"			return vm_private();\n" +
				"		}\n" +
				"	};\n" +
				"	private int vf_private = 100;\n" +
				"	private int avf_private = 100;\n" +
				"	public int vf_public = vf_private;\n" +
				"	public int avf_public = vf_private;\n" +
				"	\n" +
				"	private int vm_private() {\n" +
				"		vf_private = (new VcPrivate()).vf_private;\n" +
				"		avf_private = vf_private;\n" +
				"		return vf_private+avf_private;\n" +
				"	}\n" +
				"	public int vm_public() {\n" +
				"		return vm_private();\n" +
				"	}\n" +
				"}\n",
				"test/copy/VisibilityPackage.java",
				"package test.copy;\n" +
				"class VisibilityPackage {\n" +
				"	private class VpPrivate {\n" +
				"		private int vf_private = 10;\n" +
				"		public int vf_public = vf_private;\n" +
				"		private int vm_private() {\n" +
				"			vf_private = (new VpPrivate()).vf_private;\n" +
				"			return vf_private;\n" +
				"		}\n" +
				"		public int vm_public() {\n" +
				"			return vm_private();\n" +
				"		}\n" +
				"	}\n" +
				"	public class VpPublic {\n" +
				"		private int vf_private = 10;\n" +
				"		public int vf_public = vf_private;\n" +
				"		private int vm_private() {\n" +
				"			vf_private = (new VpPrivate()).vf_private;\n" +
				"			return vf_private;\n" +
				"		}\n" +
				"		public int vm_public() {\n" +
				"			return vm_private();\n" +
				"		}\n" +
				"	}\n" +
				"	private int vf_private = 100;\n" +
				"	public int vf_public = vf_private;\n" +
				"	\n" +
				"	private int vm_private() {\n" +
				"		vf_private = (new VpPrivate()).vf_private;\n" +
				"		return vf_private;\n" +
				"	}\n" +
				"	public int vm_public() {\n" +
				"		return vm_private();\n" +
				"	}\n" +
				"}\n",
				"test/copy/VisibilityPublic.java",
				"package test.copy;\n" +
				"public class VisibilityPublic {\n" +
				"	private class VpPrivate {\n" +
				"		private int vf_private = 10;\n" +
				"		public int vf_public = vf_private;\n" +
				"		private int vm_private() {\n" +
				"			vf_private = (new VpPrivate()).vf_private;\n" +
				"			return vf_private;\n" +
				"		}\n" +
				"		public int vm_public() {\n" +
				"			return vm_private();\n" +
				"		}\n" +
				"	}\n" +
				"	public class VpPublic {\n" +
				"		private int vf_private = 10;\n" +
				"		public int vf_public = vf_private;\n" +
				"		private int vm_private() {\n" +
				"			vf_private = (new VpPrivate()).vf_private;\n" +
				"			return vf_private;\n" +
				"		}\n" +
				"		public int vm_public() {\n" +
				"			return vm_private();\n" +
				"		}\n" +
				"	}\n" +
				"	private int vf_private = 100;\n" +
				"	public int vf_public = vf_private;\n" +
				"	\n" +
				"	private int vm_private() {\n" +
				"		vf_private = (new VpPrivate()).vf_private;\n" +
				"		return vf_private;\n" +
				"	}\n" +
				"	public int vm_public() {\n" +
				"		return vm_private();\n" +
				"	}\n" +
				"}\n" };
	}
	// The fix for https://bugs.eclipse.org/bugs/show_bug.cgi?id=201912 results in these additional
	// diagnostics to be generated. Just as we arrange for the ``referencedClasses'' to be compiled
	// automatically, we need to include these diagnostics automatically in the expected messages.
	static String expectedDiagnosticsFromReferencedClasses =
		"----------\n" +
		"1. WARNING in test\\AbstractVisibility.java (at line 5)\n" +
		"	public int avf_public = avf_private;\n" +
		"	           ^^^^^^^^^^\n" +
		"The value of the field AbstractVisibility.AvcPrivate.avf_public is not used\n" +
		"----------\n" +
		"2. WARNING in test\\AbstractVisibility.java (at line 10)\n" +
		"	public int avm_public() {\n" +
		"	           ^^^^^^^^^^^^\n" +
		"The method avm_public() from the type AbstractVisibility.AvcPrivate is never used locally\n" +
		"----------\n" +
		"----------\n" +
		"1. WARNING in test\\Visibility.java (at line 5)\n" +
		"	public int vf_public = vf_private;\n" +
		"	           ^^^^^^^^^\n" +
		"The value of the field Visibility.VcPrivate.vf_public is not used\n" +
		"----------\n" +
		"2. WARNING in test\\Visibility.java (at line 11)\n" +
		"	public int vm_public() {\n" +
		"	           ^^^^^^^^^^^\n" +
		"The method vm_public() from the type Visibility.VcPrivate is never used locally\n" +
		"----------\n" +
		"----------\n" +
		"1. WARNING in test\\copy\\VisibilityPackage.java (at line 5)\n" +
		"	public int vf_public = vf_private;\n" +
		"	           ^^^^^^^^^\n" +
		"The value of the field VisibilityPackage.VpPrivate.vf_public is not used\n" +
		"----------\n" +
		"2. WARNING in test\\copy\\VisibilityPackage.java (at line 10)\n" +
		"	public int vm_public() {\n" +
		"	           ^^^^^^^^^^^\n" +
		"The method vm_public() from the type VisibilityPackage.VpPrivate is never used locally\n" +
		"----------\n" +
		"----------\n" +
		"1. WARNING in test\\copy\\VisibilityPublic.java (at line 5)\n" +
		"	public int vf_public = vf_private;\n" +
		"	           ^^^^^^^^^\n" +
		"The value of the field VisibilityPublic.VpPrivate.vf_public is not used\n" +
		"----------\n" +
		"2. WARNING in test\\copy\\VisibilityPublic.java (at line 10)\n" +
		"	public int vm_public() {\n" +
		"	           ^^^^^^^^^^^\n" +
		"The method vm_public() from the type VisibilityPublic.VpPrivate is never used locally\n" +
		"----------\n";
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (RUN_JAVAC) {
			javadocCommandLineHeader =
				jdkRootDirPath.append("bin").append(JAVADOC_NAME).toString(); // PREMATURE replace JAVA_NAME and JAVAC_NAME with locals? depends on potential reuse
		}
//		SHIFT = true;
//		INDENT = 3;
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
//		SHIFT = false;
//		INDENT = 2;
		super.tearDown();
	}

	protected void runConformReferenceTest(String[] testFiles) {
		String[] completedFiles = testFiles;
		if (!this.useLibrary) {
			completedFiles = new String[testFiles.length + referencedClasses.length];
			System.arraycopy(referencedClasses, 0, completedFiles, 0, referencedClasses.length);
			System.arraycopy(testFiles, 0, completedFiles, referencedClasses.length, testFiles.length);
		}
		runConformTest(completedFiles);
	}
	protected void runNegativeReferenceTest(String[] testFiles, String expected) {
		runNegativeReferenceTest(testFiles, expected, JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	protected void runNegativeReferenceTest(String[] testFiles, String expected,
			JavacTestOptions javacTestOptions) {
		String[] completedFiles = testFiles;
		if (!this.useLibrary) {
			completedFiles = new String[testFiles.length + referencedClasses.length];
			System.arraycopy(referencedClasses, 0, completedFiles, 0, referencedClasses.length);
			System.arraycopy(testFiles, 0, completedFiles, referencedClasses.length, testFiles.length);
			expected = expectedDiagnosticsFromReferencedClasses + expected;
		}
		runNegativeTest(completedFiles, expected, javacTestOptions);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest#runConformTest(java.lang.String[], java.lang.String, java.lang.String[], boolean, java.lang.String[], java.util.Map, org.eclipse.jdt.internal.compiler.ICompilerRequestor)
	 *
	protected void runConformTest(String[] testFiles,
			String expectedSuccessOutputString,
			String[] classLib,
			boolean shouldFlushOutputDirectory,
			String[] vmArguments,
			Map customOptions,
			ICompilerRequestor clientRequestor) {
		if (TESTS_NAMES != null || TESTS_PREFIX != null || TESTS_NUMBERS != null || TESTS_RANGE != null) {
			writeFiles(testFiles);
		}
		super.runConformTest(testFiles,
			expectedSuccessOutputString,
			classLib,
			shouldFlushOutputDirectory,
			vmArguments,
			customOptions,
			clientRequestor);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest#runNegativeTest(java.lang.String[], java.lang.String, java.lang.String[], boolean, java.util.Map, boolean)
	 *
	protected void runNegativeTest(String[] testFiles,
			String expectedProblemLog,
			String[] classLib,
			boolean shouldFlushOutputDirectory,
			Map customOptions,
			boolean generateOutput) {
		if (TESTS_NAMES != null || TESTS_PREFIX != null || TESTS_NUMBERS != null || TESTS_RANGE != null) {
			writeFiles(testFiles);
		}
		super.runNegativeTest(testFiles,
			expectedProblemLog,
			classLib,
			shouldFlushOutputDirectory,
			customOptions,
			generateOutput);
	}
	*/
	@Override
	protected void writeFiles(String[] testFiles) {
		String classDirName = getClass().getName().substring(getClass().getName().lastIndexOf('.')+1); //.substring(11);
		String testName = getName();
		int idx = testName.indexOf(" - ");
		if (idx > 0) {
			testName = testName.substring(idx+3);
		}

    // File dir = new File("d:/usr/OTI/tests/javadoc/");
		// non portable
		createOutputTestDirectory(classDirName);
		createOutputTestDirectory(Character.toUpperCase(testName.charAt(0)) +
				testName.substring(1));
		System.out.println("Write test file to " +
				this.outputTestDirectory.getPath() + "...");
		for (int i=0, length=testFiles.length; i<length; i++) {
			String contents = testFiles[i+1];
			String fileName = testFiles[i++];
			String dirFileName = this.outputTestDirectory.getPath();
			if (fileName.indexOf("Visibility")>0) {
				continue;
			} else {
				int index = fileName.lastIndexOf('/');
				if (index > 0) {
					String subdirs = fileName.substring(0, index);
					String packName = subdirs.replace('/', '.');
					contents = "package "+packName+";"+contents.substring(contents.indexOf(';')+1);
					File dir = new File(dirFileName, subdirs);
					if (!dir.exists()) dir.mkdirs();
					if (RUN_JAVAC) {
						Util.writeToFile(contents, dirFileName+"/"+fileName);
						// PREMATURE this results into a duplicate file.
					}
					fileName = fileName.substring(index+1);
				}
			}
			Util.writeToFile(contents, dirFileName+"/"+fileName);
			// REVIEW don't know why this is created at the default package level
		}
	}

	/*
	 * Run Sun compilation using javadoc.
	 * See implementation in parent for details.
	 */
	@Override
	protected void runJavac(
			String[] testFiles,
			final String expectedProblemLog,
			final String expectedSuccessOutputString,
			boolean shouldFlushOutputDirectory) {
		String testName = null;
		Process compileProcess = null;
		try {
			// Init test name
			testName = testName();

			// Cleanup javac output dir if needed
			File javacOutputDirectory = new File(JAVAC_OUTPUT_DIR_NAME);
			if (shouldFlushOutputDirectory) {
				Util.delete(javacOutputDirectory);
			}

			// Write files in dir
			writeFiles(testFiles);

			// Prepare command line
			StringBuilder cmdLine = new StringBuilder(javadocCommandLineHeader);
			// compute extra classpath
			String[] classpath = Util.concatWithClassLibs(JAVAC_OUTPUT_DIR_NAME, false);
			StringBuilder cp = new StringBuilder(" -classpath ");
			int length = classpath.length;
			for (int i = 0; i < length; i++) {
				if (i > 0)
				  cp.append(File.pathSeparatorChar);
				if (classpath[i].indexOf(" ") != -1) {
					cp.append("\"" + classpath[i] + "\"");
				} else {
					cp.append(classpath[i]);
				}
			}
			cmdLine.append(cp);
			// add source files
			for (int i = 0; i < testFiles.length; i += 2) {
				// *.java is not enough (p1/X.java, p2/Y.java)
				cmdLine.append(' ');
				cmdLine.append(testFiles[i]);
			}

			// Launch process
			compileProcess = Runtime.getRuntime().exec(
				cmdLine.toString(), null, this.outputTestDirectory);

			// Log errors
      Logger errorLogger = new Logger(compileProcess.getErrorStream(), "ERROR");

      // Log output
      Logger outputLogger = new Logger(compileProcess.getInputStream(), "OUTPUT");

      // start the threads to run outputs (standard/error)
      errorLogger.start();
      outputLogger.start();

      // Wait for end of process
			int exitValue = compileProcess.waitFor();
			errorLogger.join(); // make sure we get the whole output
			outputLogger.join();

			// Report raw javadoc results
			if (! testName.equals(javacTestName)) {
				javacTestName = testName;
				javacTestErrorFlag = false;
				javacFullLog.println("-----------------------------------------------------------------");
				javacFullLog.println(CURRENT_CLASS_NAME + " " + testName);
			}
			if (exitValue != 0) {
				javacTestErrorFlag = true;
			}
			if (errorLogger.buffer.length() > 0) {
				javacFullLog.println("--- javac err: ---");
				javacFullLog.println(errorLogger.buffer.toString());
			}
			if (outputLogger.buffer.length() > 0) {
				javacFullLog.println("--- javac out: ---");
				javacFullLog.println(outputLogger.buffer.toString());
			}

			// Compare compilation results
			if (expectedProblemLog == null || expectedProblemLog.length() == 0) {
				// Eclipse found no error and no warning
				if (exitValue != 0) {
					// Javac found errors
					System.out.println("----------------------------------------");
					System.out.println(testName + " - Javadoc has found error(s) but Eclipse expects conform result:\n");
					javacFullLog.println("JAVAC_MISMATCH: Javadoc has found error(s) but Eclipse expects conform result");
					System.out.println(errorLogger.buffer.toString());
					printFiles(testFiles);
					DIFF_COUNTERS[0]++;
				}
				else {
					// Javac found no error - may have found warnings
					if (errorLogger.buffer.length() > 0) {
						System.out.println("----------------------------------------");
						System.out.println(testName + " - Javadoc has found warning(s) but Eclipse expects conform result:\n");
						javacFullLog.println("JAVAC_MISMATCH: Javadoc has found warning(s) but Eclipse expects conform result");
						System.out.println(errorLogger.buffer.toString());
						printFiles(testFiles);
						DIFF_COUNTERS[0]++;
					}
				}
			}
			else {
				// Eclipse found errors or warnings
				if (errorLogger.buffer.length() == 0) {
					System.out.println("----------------------------------------");
					System.out.println(testName + " - Eclipse has found error(s)/warning(s) but Javadoc did not find any:");
					javacFullLog.println("JAVAC_MISMATCH: Eclipse has found error(s)/warning(s) but Javadoc did not find any");
					dualPrintln("eclipse:");
					dualPrintln(expectedProblemLog);
					printFiles(testFiles);
					DIFF_COUNTERS[1]++;
				} else if (expectedProblemLog.indexOf("ERROR") > 0 && exitValue == 0){
					System.out.println("----------------------------------------");
					System.out.println(testName + " - Eclipse has found error(s) but Javadoc only found warning(s):");
					javacFullLog.println("JAVAC_MISMATCH: Eclipse has found error(s) but Javadoc only found warning(s)");
					dualPrintln("eclipse:");
					dualPrintln(expectedProblemLog);
					System.out.println("javadoc:");
					System.out.println(errorLogger.buffer.toString());
					printFiles(testFiles);
					DIFF_COUNTERS[1]++;
				} else {
					// PREMATURE refine comparison
					// TODO (frederic) compare warnings in each result and verify they are similar...
//						System.out.println(testName+": javac has found warnings :");
//						System.out.print(errorLogger.buffer.toString());
//						System.out.println(testName+": we're expecting warning results:");
//						System.out.println(expectedProblemLog);
				}
			}
		}
		catch (InterruptedException e1) {
			if (compileProcess != null) compileProcess.destroy();
			System.out.println(testName+": Sun javadoc compilation was aborted!");
			javacFullLog.println("JAVAC_WARNING: Sun javadoc compilation was aborted!");
			e1.printStackTrace(javacFullLog);
		}
		catch (Throwable e) {
			System.out.println(testName+": could not launch Sun javadoc compilation!");
			e.printStackTrace();
			javacFullLog.println("JAVAC_ERROR: could not launch Sun javac compilation!");
			e.printStackTrace(javacFullLog);
			// PREMATURE failing the javac pass or comparison could also fail
			//           the test itself
		}
		finally {
			Util.delete(this.outputTestDirectory);
		}
	}

	@Override
	protected void	printJavacResultsSummary() {
		if (RUN_JAVAC) {
			Integer count = (Integer)TESTS_COUNTERS.get(CURRENT_CLASS_NAME);
			if (count != null) {
				int newCount = count.intValue()-1;
				TESTS_COUNTERS.put(CURRENT_CLASS_NAME, Integer.valueOf(newCount));
				if (newCount == 0) {
					if (DIFF_COUNTERS[0]!=0 || DIFF_COUNTERS[1]!=0 || DIFF_COUNTERS[2]!=0) {
						dualPrintln("===========================================================================");
						dualPrintln("Results summary:");
					}
					if (DIFF_COUNTERS[0]!=0)
						dualPrintln("	- "+DIFF_COUNTERS[0]+" test(s) where Javadoc found errors/warnings but Eclipse did not");
					if (DIFF_COUNTERS[1]!=0)
						dualPrintln("	- "+DIFF_COUNTERS[1]+" test(s) where Eclipse found errors/warnings but Javadoc did not");
					System.out.println("\n");
				}
			}
			javacFullLog.flush();
		}
	}

}
