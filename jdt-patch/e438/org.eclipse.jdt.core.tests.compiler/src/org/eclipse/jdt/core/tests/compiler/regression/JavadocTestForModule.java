/*******************************************************************************
 * Copyright (c) 2019, 2020 IBM Corporation and others.
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
 *     Red Hat Inc. - copied from ModuleCompilationTests and used for Javadoc
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public class JavadocTestForModule extends AbstractBatchCompilerTest {

	static {
//		 TESTS_NAMES = new String[] { "testBug549855a" };
		// TESTS_NUMBERS = new int[] { 1 };
		// TESTS_RANGE = new int[] { 298, -1 };
	}

	public JavadocTestForModule(String name) {
		super(name);
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_9);
	}

	public static Class<?> testClass() {
		return JavadocTestForModule.class;
	}

	protected void writeFileCollecting(List<String> collectedFiles, String directoryName, String fileName, String source) {
		writeFile(directoryName, fileName, source);
		collectedFiles.add(directoryName+File.separator+fileName);
	}

	protected void writeFile(String directoryName, String fileName, String source) {
		File directory = new File(directoryName);
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				System.out.println("Could not create " + directoryName);
				return;
			}
		}
		String filePath = directory.getAbsolutePath() + File.separator + fileName;
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
			writer.write(source);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	class Runner extends AbstractRegressionTest.Runner {
		StringBuilder commandLine = new StringBuilder();
		String outputDir = OUTPUT_DIR + File.separator + "javac";
		List<String> fileNames = new ArrayList<>();
		/** will replace any -8, -9 ... option for javac */
		String javacVersionOptions;

		Runner() {
			this.javacTestOptions = JavacTestOptions.DEFAULT;
			this.expectedOutputString = "";
			this.expectedErrorString = "";
		}
		/** Create a source file and add the filename to the compiler command line. */
		void createFile(String directoryName, String fileName, String source) {
			writeFileCollecting(this.fileNames, directoryName, fileName, source);
		}
		Set<String> runConformModuleTest() {
			if (!this.fileNames.isEmpty()) {
				this.shouldFlushOutputDirectory = false;
				if (this.testFiles == null)
					this.testFiles = new String[0];
				for (String fileName : this.fileNames) {
					this.commandLine.append(" \"").append(fileName).append("\"");
				}
			}
			String commandLineString = this.commandLine.toString();
			String javacCommandLine = adjustForJavac(commandLineString, this.javacVersionOptions);
			return JavadocTestForModule.this.runConformModuleTest(this.testFiles, commandLineString,
					this.expectedOutputString, this.expectedErrorString,
					this.shouldFlushOutputDirectory, this.outputDir,
					this.javacTestOptions, javacCommandLine);
		}
	}

	void runConformModuleTest(List<String> testFileNames, StringBuilder commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory)
	{
		runConformModuleTest(testFileNames, commandLine,
				expectedFailureOutOutputString, expectedFailureErrOutputString, shouldFlushOutputDirectory, OUTPUT_DIR + File.separator + "javac");
	}

	void runConformModuleTest(List<String> testFileNames, StringBuilder commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory, String output)
	{
		for (String file : testFileNames)
			commandLine.append(" \"").append(file).append("\"");
		runConformModuleTest(new String[0], commandLine.toString(),
				expectedFailureOutOutputString, expectedFailureErrOutputString, shouldFlushOutputDirectory,
				output, JavacTestOptions.DEFAULT, null);
	}

	Set<String> runConformModuleTest(String[] testFiles, String commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory)
	{
		return runConformModuleTest(testFiles, commandLine, expectedFailureErrOutputString, expectedFailureErrOutputString,
				shouldFlushOutputDirectory, OUTPUT_DIR, JavacTestOptions.DEFAULT, null);
	}

	Set<String> runConformModuleTest(String[] testFiles, String commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory, String output, JavacTestOptions options, String javacCommandLine)
	{
		this.runConformTest(testFiles, commandLine, expectedFailureOutOutputString, expectedFailureErrOutputString, shouldFlushOutputDirectory);
		if (shouldRunJavac()) {
			File outputDir = new File(output);
			final Set<String> outFiles = new HashSet<>();
			walkOutFiles(output, outFiles, true);
			String[] testFileNames = new String[testFiles.length/2];
			for (int i = 0; i < testFileNames.length; i++) {
				testFileNames[i] = testFiles[i*2];
			}
			if (javacCommandLine == null) {
				javacCommandLine = adjustForJavac(commandLine, null);
			}
			for (JavacCompiler javacCompiler : javacCompilers) {
				if (javacCompiler.compliance < ClassFileConstants.JDK9)
					continue;
				if (options.skip(javacCompiler)) {
					System.err.println("Skip testing javac in "+testName());
					continue;
				}
				StringBuilder log = new StringBuilder();
				try {
					long compileResult = javacCompiler.compile(
											outputDir, /* directory */
											javacCommandLine /* options */,
											testFileNames /* source file names */,
											log,
											false); // don't repeat filenames on the command line
					if (compileResult != 0) {
						System.err.println("Previous error was from "+testName());
						fail("Unexpected error from javac");
					}
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
					throw new AssertionFailedError(e.getMessage());
				}
				final Set<String> expectedFiles = new HashSet<>(outFiles);
				walkOutFiles(output, expectedFiles, false);
				for (String missingFile : expectedFiles)
					System.err.println("Missing output file from javac:    "+missingFile);
			}
			return outFiles;
		}
		return null;
	}

	void runNegativeModuleTest(List<String> testFileNames, StringBuilder commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory, String javacErrorMatch) {
		runNegativeModuleTest(testFileNames, commandLine, expectedFailureOutOutputString,
				expectedFailureErrOutputString, shouldFlushOutputDirectory, javacErrorMatch, OUTPUT_DIR + File.separator + "javac");
	}

	void runNegativeModuleTest(List<String> testFileNames, StringBuilder commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory, String javacErrorMatch, String output)
	{
		runNegativeModuleTest(testFileNames, commandLine, expectedFailureOutOutputString, expectedFailureErrOutputString,
				shouldFlushOutputDirectory, javacErrorMatch, output, JavacTestOptions.DEFAULT);
	}
	void runNegativeModuleTest(List<String> testFileNames, StringBuilder commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory, String javacErrorMatch, String output, JavacTestOptions options)
	{
		for (String file : testFileNames)
			commandLine.append(" \"").append(file).append("\"");
		runNegativeModuleTest(new String[0], commandLine.toString(),
				expectedFailureOutOutputString, expectedFailureErrOutputString, shouldFlushOutputDirectory, javacErrorMatch, output,
				options);
	}
	void runNegativeModuleTest(String[] testFiles, String commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory, String javacErrorMatch) {
		runNegativeModuleTest(testFiles, commandLine, expectedFailureOutOutputString, expectedFailureErrOutputString,
				shouldFlushOutputDirectory, javacErrorMatch, OUTPUT_DIR, JavacTestOptions.DEFAULT);
	}

	void runNegativeModuleTest(String[] testFiles, String commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory, String javacErrorMatch, String output, JavacTestOptions options)
	{
		this.runNegativeTest(testFiles, commandLine, expectedFailureOutOutputString, expectedFailureErrOutputString, shouldFlushOutputDirectory);
		if (shouldRunJavac()) {
			String[] testFileNames = new String[testFiles.length/2];
			for (int i = 0; i < testFileNames.length; i++) {
				testFileNames[i] = testFiles[i*2];
			}
			File outputDir = new File(OUTPUT_DIR);
			final Set<String> outFiles = new HashSet<>();
			walkOutFiles(output, outFiles, true);
			for (JavacCompiler javacCompiler : javacCompilers) {
				if (javacCompiler.compliance < ClassFileConstants.JDK9)
					continue;
				JavacTestOptions.Excuse excuse = options.excuseFor(javacCompiler);

				commandLine = adjustForJavac(commandLine, null);
				StringBuilder log = new StringBuilder();
				int mismatch = 0;
				try {
					long compileResult = javacCompiler.compile(
											outputDir, /* directory */
											commandLine /* options */,
											testFileNames /* source file names */,
											log);
					if (compileResult == 0) {
						mismatch = JavacTestOptions.MismatchType.EclipseErrorsJavacNone;
						javacErrorMatch = expectedFailureErrOutputString;
						System.err.println("Previous error was from "+testName());
					} else if (!log.toString().contains(javacErrorMatch)) {
						mismatch = JavacTestOptions.MismatchType.CompileErrorMismatch;
						System.err.println(testName()+": Error match " + javacErrorMatch + " not found in \n"+log.toString());
					}
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
					throw new AssertionFailedError(e.getMessage());
				}
				handleMismatch(javacCompiler, testName(), testFiles, javacErrorMatch,
						"", "", log, "", "",
						excuse, mismatch);
				final Set<String> expectedFiles = new HashSet<>(outFiles);
				walkOutFiles(output, expectedFiles, false);
				for (String missingFile : expectedFiles)
					System.err.println("Missing output file from javac:    "+missingFile);
			}
		}
	}

	/**
	 * @param commandLine command line arguments as used for ecj
	 * @param versionOptions if non-null use this to replace any ecj-specific -8, -9 etc. arg.
	 * 		If ecj-specific arg is not found, append anyway
	 * @return commandLine adjusted for javac
	 */
	String adjustForJavac(String commandLine, String versionOptions) {
		String[] tokens = commandLine.split(" ");
		StringBuilder buf = new StringBuilder();
		boolean skipNext = false;
		for (int i = 0; i < tokens.length; i++) {
			if (skipNext) {
				skipNext = false;
				continue;
			}
			if (tokens[i].trim().equals("-9")) {
				if (versionOptions == null)
					buf.append(' ').append(" --release 9 ");
				continue;
			}
			if (tokens[i].trim().equals("-8")) {
				if (versionOptions == null)
					buf.append(' ').append(" --release 8 ");
				continue;
			}
			if (tokens[i].startsWith("-warn") || tokens[i].startsWith("-err") || tokens[i].startsWith("-info")) {
				if (tokens[i].contains("exports") && !tokens[i].contains("-exports"))
					buf.append(" -Xlint:exports ");
				continue;
			}
			if (tokens[i].trim().equals("-classNames")) {
				skipNext = true;
				continue;
			}
			if (tokens[i].trim().equals("-enableJavadoc")) {
				buf.append(" -Xdoclint:all ");
				continue;
			}
			buf.append(tokens[i]).append(' ');
		}
		if (versionOptions != null) {
			buf.append(versionOptions);
		}
		return buf.toString();
	}

	private void walkOutFiles(final String outputLocation, final Set<String> fileNames, boolean add) {
		if (!(new File(outputLocation)).exists())
			return;
		try {
			Files.walkFileTree(Path.of(outputLocation), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (file.toString().endsWith(".class")) {
						if (add) {
							fileNames.add(file.toString());
						} else {
							if (!fileNames.remove(file.toString()))
								System.err.println("Unexpected output file from javac: "+file.toString());
						}
						Files.delete(file);
					}
					return FileVisitResult.CONTINUE;
				}
				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					if (!dir.toString().equals(outputLocation)) {
						try {
							Files.delete(dir);
						} catch (DirectoryNotEmptyException ex) {
							// expected
						}
					}
			        return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
			throw new AssertionFailedError(e.getMessage());
		}
	}

	public void testBug549855a() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"/**\n" +
						" */\n" +
						"module mod.one { \n" +
						" exports p;\n" +
						" provides p.I1 with p.P1;\n" +
						" uses java.util.Currency;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "I1.java",
				"package p;\n" +
				"/**\n" +
				" * interface I1\n" +
				" */\n" +
				"public interface I1 {\n" +
				"	/**\n" +
				"	 * Method foo\n" +
				"    * @return int\n" +
				"    */\n" +
				"	public int foo();\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "P1.java",
				"package p;\n" +
				"/**\n" +
				" * class P1\n" +
				" */\n" +
				"public class P1 implements I1 {\n" +
				"	@Override\n" +
				"	public int foo() { return 0; }\n" +
				"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -enableJavadoc ")
			.append(" -err:allJavadoc ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:-unused")
			.append(" --module-source-path " + "\"" + directory + "\" ")
			.append(moduleLoc + File.separator + "module-info.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "I1.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "P1.java");

		runNegativeModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 5)\n" +
				"	provides p.I1 with p.P1;\n" +
				"	^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Javadoc: Missing provides tag\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 6)\n" +
				"	uses java.util.Currency;\n" +
				"	^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Javadoc: Missing uses tag\n" +
				"----------\n" +
				"2 problems (2 errors)\n",
				false,
				"missing tags",
				OUTPUT_DIR,
				JavacTestOptions.JavacHasABug.NoWarningForMissingJavadocTag);
	}

	public void testBug549855b() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"/**\n" +
						" @provides p.I\n" +
						" @uses java.util.Currenc\n" +
						" */\n" +
						"module mod.one { \n" +
						" exports p;\n" +
						" provides p.I1 with p.P1;\n" +
						" uses java.util.Currency;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "I1.java",
				"package p;\n" +
				"/**\n" +
				" * interface I1\n" +
				" */\n" +
				"public interface I1 {\n" +
				"	/**\n" +
				"	 * Method foo\n" +
				"    * @return int\n" +
				"    */\n" +
				"	public int foo();\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "P1.java",
				"package p;\n" +
				"/**\n" +
				" * class P1\n" +
				" */\n" +
				"public class P1 implements I1 {\n" +
				"	@Override\n" +
				"	public int foo() { return 0; }\n" +
				"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -enableJavadoc ")
			.append(" -err:allJavadoc ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:-unused")
			.append(" --module-source-path " + "\"" + directory + "\" ")
			.append(moduleLoc + File.separator + "module-info.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "I1.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "P1.java");

		runNegativeModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 7)\n" +
				"	provides p.I1 with p.P1;\n" +
				"	^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Javadoc: Missing provides tag\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 8)\n" +
				"	uses java.util.Currency;\n" +
				"	^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Javadoc: Missing uses tag\n" +
				"----------\n" +
				"2 problems (2 errors)\n",
				false,
				"service-type not found");
	}

	public void testBug549855c() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"/**\n" +
						" @provides p.I1\n" +
						" @uses java.util.Currency\n" +
						" @provides p.I1\n" +
						" @uses java.util.Currency\n" +
						" */\n" +
						"module mod.one { \n" +
						" exports p;\n" +
						" provides p.I1 with p.P1;\n" +
						" uses java.util.Currency;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "I1.java",
				"package p;\n" +
				"/**\n" +
				" * interface I1\n" +
				" */\n" +
				"public interface I1 {\n" +
				"	/**\n" +
				"	 * Method foo\n" +
				"    * @return int\n" +
				"    */\n" +
				"	public int foo();\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "P1.java",
				"package p;\n" +
				"/**\n" +
				" * class P1\n" +
				" */\n" +
				"public class P1 implements I1 {\n" +
				"	@Override\n" +
				"	public int foo() { return 0; }\n" +
				"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -enableJavadoc ")
			.append(" -err:allJavadoc ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:-unused")
			.append(" --module-source-path " + "\"" + directory + "\" ")
			.append(moduleLoc + File.separator + "module-info.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "I1.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "P1.java");

		runNegativeModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 4)\n" +
				"	@provides p.I1\n" +
				"	          ^^^^\n" +
				"Javadoc: Duplicate provides tag\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 5)\n" +
				"	@uses java.util.Currency\n" +
				"	      ^^^^^^^^^^^^^^^^^^\n" +
				"Javadoc: Duplicate uses tag\n" +
				"----------\n" +
				"2 problems (2 errors)\n",
				false,
				"duplicate tags",
				OUTPUT_DIR,
				JavacTestOptions.JavacHasABug.NoWarningForDuplicateJavadocTag);
	}

	public void testBug549855d() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"/**\n" +
						" @provides p.I1\n" +
						" @uses java.util.Currency\n" +
						" */\n" +
						"module mod.one { \n" +
						" exports p;\n" +
						" provides p.I1 with p.P1;\n" +
						" uses java.util.Currency;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "I1.java",
				"package p;\n" +
				"/**\n" +
				" * interface I1\n" +
				" */\n" +
				"public interface I1 {\n" +
				"	/**\n" +
				"	 * Method foo\n" +
				"    * @return int\n" +
				"    */\n" +
				"	public int foo();\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "P1.java",
				"package p;\n" +
				"/**\n" +
				" * class P1\n" +
				" */\n" +
				"public class P1 implements I1 {\n" +
				"	@Override\n" +
				"	public int foo() { return 0; }\n" +
				"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -enableJavadoc ")
			.append(" -err:allJavadoc ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:-unused")
			.append(" --module-source-path " + "\"" + directory + "\" ");

		runConformModuleTest(files, buffer, "", "", false);
	}

	public void testBug549855e() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"/**\n" +
						" @provides p.I1\n" +
						" */\n" +
						"module mod.one { \n" +
						" exports p;\n" +
						" provides p.I1 with p.P1;\n" +
						" uses java.util.Currency;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "I1.java",
				"package p;\n" +
				"/**\n" +
				" * interface I1\n" +
				" */\n" +
				"public interface I1 {\n" +
				"	/**\n" +
				"	 * Method foo\n" +
				"    * @return int\n" +
				"    */\n" +
				"	public int foo();\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "P1.java",
				"package p;\n" +
				"/**\n" +
				" * class P1\n" +
				" */\n" +
				"public class P1 implements I1 {\n" +
				"	@Override\n" +
				"	public int foo() { return 0; }\n" +
				"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -enableJavadoc ")
			.append(" -err:allJavadoc ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:-unused")
			.append(" --module-source-path " + "\"" + directory + "\" ")
			.append(moduleLoc + File.separator + "module-info.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "I1.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "P1.java");

		runNegativeModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 7)\n" +
				"	uses java.util.Currency;\n" +
				"	^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Javadoc: Missing uses tag\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"missing tags",
				OUTPUT_DIR,
				JavacTestOptions.JavacHasABug.NoWarningForMissingJavadocTag);
	}

	public void testBug549855f() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"/**\n" +
						" @uses java.util.Currency\n" +
						" */\n" +
						"module mod.one { \n" +
						" exports p;\n" +
						" provides p.I1 with p.P1;\n" +
						" uses java.util.Currency;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "I1.java",
				"package p;\n" +
				"/**\n" +
				" * interface I1\n" +
				" */\n" +
				"public interface I1 {\n" +
				"	/**\n" +
				"	 * Method foo\n" +
				"    * @return int\n" +
				"    */\n" +
				"	public int foo();\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "P1.java",
				"package p;\n" +
				"/**\n" +
				" * class P1\n" +
				" */\n" +
				"public class P1 implements I1 {\n" +
				"	@Override\n" +
				"	public int foo() { return 0; }\n" +
				"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -enableJavadoc ")
			.append(" -err:allJavadoc ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:-unused")
			.append(" --module-source-path " + "\"" + directory + "\" ")
			.append(moduleLoc + File.separator + "module-info.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "I1.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "P1.java");

		runNegativeModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 6)\n" +
				"	provides p.I1 with p.P1;\n" +
				"	^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Javadoc: Missing provides tag\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"missing tags",
				OUTPUT_DIR,
				JavacTestOptions.JavacHasABug.NoWarningForMissingJavadocTag);
	}

	public void testBug549855g() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"/**\n" +
						" */\n" +
						"module mod.one { \n" +
						" exports p;\n" +
						" provides p.I1 with p.P1;\n" +
						" uses java.util.Currency;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "I1.java",
				"package p;\n" +
				"/**\n" +
				" * interface I1\n" +
				" */\n" +
				"public interface I1 {\n" +
				"	/**\n" +
				"	 * Method foo\n" +
				"    * @return int\n" +
				"    */\n" +
				"	public int foo();\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "P1.java",
				"package p;\n" +
				"/**\n" +
				" * class P1\n" +
				" */\n" +
				"public class P1 implements I1 {\n" +
				"	@Override\n" +
				"	public int foo() { return 0; }\n" +
				"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -enableJavadoc ")
			.append(" -err:allJavadoc ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:-unused")
			.append(" --module-source-path " + "\"" + directory + "\" ")
			.append(moduleLoc + File.separator + "module-info.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "I1.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "P1.java");

		runNegativeModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 5)\n" +
				"	provides p.I1 with p.P1;\n" +
				"	^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Javadoc: Missing provides tag\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 6)\n" +
				"	uses java.util.Currency;\n" +
				"	^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Javadoc: Missing uses tag\n" +
				"----------\n" +
				"2 problems (2 errors)\n",
				false,
				"missing tags",
				OUTPUT_DIR,
				JavacTestOptions.JavacHasABug.NoWarningForMissingJavadocTag);
	}

	public void testBug549855h() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"/**\n" +
						" * @provides p.I\n" +
						" * @uses java.util.Currenc\n" +
						" */\n" +
						"module mod.one { \n" +
						" exports p;\n" +
						" provides p.I1 with p.P1;\n" +
						" uses java.util.Currency;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "I1.java",
				"package p;\n" +
				"/**\n" +
				" * interface I1\n" +
				" */\n" +
				"public interface I1 {\n" +
				"	/**\n" +
				"	 * Method foo\n" +
				"    * @return int\n" +
				"    */\n" +
				"	public int foo();\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "P1.java",
				"package p;\n" +
				"/**\n" +
				" * class P1\n" +
				" */\n" +
				"public class P1 implements I1 {\n" +
				"	@Override\n" +
				"	public int foo() { return 0; }\n" +
				"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -enableJavadoc ")
			.append(" -err:allJavadoc ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:-unused")
			.append(" --module-source-path " + "\"" + directory + "\" ")
			.append(moduleLoc + File.separator + "module-info.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "I1.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "P1.java");

		runNegativeModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 7)\n" +
				"	provides p.I1 with p.P1;\n" +
				"	^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Javadoc: Missing provides tag\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 8)\n" +
				"	uses java.util.Currency;\n" +
				"	^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Javadoc: Missing uses tag\n" +
				"----------\n" +
				"2 problems (2 errors)\n",
				false,
				"reference not found");
	}

	public void testBug549855i() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one {\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -enableJavadoc ")
			.append(" -err:allJavadoc ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:-unused")
			.append(" --module-source-path " + "\"" + directory + "\" ")
			.append(moduleLoc + File.separator + "module-info.java ");

		runNegativeModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 1)\n" +
				"	module mod.one {\n" +
				"	^^^^^^^^^^^^^^^\n" +
				"Javadoc: Missing comment for module declaration\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"no comment");
	}

	public void testBug562960() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String options =
			"-d " + OUTPUT_DIR + File.separator + out +
			" -9 " +
			" -enableJavadoc " +
			" -err:allJavadoc " +
			" -classpath \"" + Util.getJavaClassLibsAsString() + "\" " +
			directory + File.separator + "Test.java";

		runNegativeModuleTest(
			new String[] {
				"src/Test.java",
				"/**\n" +
				" * {@link sun.security.ssl.X509TrustManagerImpl}\n" +
				" */\n" +
				"public class Test {}\n"
			},
			options,
			"",
			"----------\n" +
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/Test.java (at line 2)\n" +
			"	* {@link sun.security.ssl.X509TrustManagerImpl}\n" +
			"	         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The type sun.security.ssl.X509TrustManagerImpl is not accessible\n" +
			"----------\n" +
			"1 problem (1 error)\n",
			false,
			"reference not found");
	}

}
