/*******************************************************************************
 * Copyright (c) 2016, 2024 IBM Corporation and others.
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.IClassFileAttribute;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.IModuleAttribute;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.AttributeNamesConstants;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

public class ModuleCompilationTests extends AbstractBatchCompilerTest {

	static {
//		 TESTS_NAMES = new String[] { "testRelease565930" };
		// TESTS_NUMBERS = new int[] { 1 };
		// TESTS_RANGE = new int[] { 298, -1 };
	}

	// use -source rather than --release but suppress: warning: [options] bootstrap class path not set in conjunction with -source 9:
	private static final String JAVAC_SOURCE_9_OPTIONS = "-source 9 -Xlint:-options";

	public ModuleCompilationTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_9);
	}

	public static Class<?> testClass() {
		return ModuleCompilationTests.class;
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
			return ModuleCompilationTests.this.runConformModuleTest(this.testFiles, commandLineString,
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
		if (RUN_JAVAC) {
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
		if (RUN_JAVAC) {
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

	private void assertClassFile(String msg, String fileName, Set<String> classFiles) {
		if (classFiles != null) {
			assertTrue(msg, classFiles.contains(fileName));
		} else {
			assertTrue(msg, (new File(fileName).exists()));
		}
	}

	public void test001() {
		runNegativeModuleTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"     java.sql.Connection con = null;\n" +
				"	}\n" +
				"}",
				"module-info.java",
				"module mod.one { \n" +
				"	requires java.base;\n" +
				"}"
	        },
			" -9 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "----------\n" +
    		"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 4)\n" +
    		"	java.sql.Connection con = null;\n" +
    		"	^^^^^^^^^^^^^^^^^^^\n" +
    		"The type java.sql.Connection is not accessible\n" +
    		"----------\n" +
    		"1 problem (1 error)\n",
	        true,
	        "package java.sql" /* match for javac error */);
	}
	public void test002() {
		runConformModuleTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"     java.sql.Connection con = null;\n" +
				"     System.out.println(con);\n" +
				"	}\n" +
				"}",
				"module-info.java",
				"module mod.one { \n" +
				"	requires java.base;\n" +
				"	requires java.sql;\n" +
				"}"
	        },
			" -9 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "",
	        true);
	}
	public void test003() {
		runConformModuleTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"     java.sql.Connection con = null;\n" +
				"     System.out.println(con);\n" +
				"	}\n" +
				"}",
	        },
	        "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "",
	        true);
	}
	public void test004() {
		Set<String> classFiles = runConformModuleTest(
			new String[] {
				"module-info.java",
				"module mod.one { \n" +
				"	requires java.base;\n" +
				"	requires java.sql;\n" +
				"}"
	        },
			" -9 \"" + OUTPUT_DIR +  File.separator + "module-info.java\"",
	        "",
	        "",
	        true);
		String fileName = OUTPUT_DIR + File.separator + "module-info.class";
		assertClassFile("Missing modul-info.class: " + fileName, fileName, classFiles);
	}
	public void test005() {
		Set<String> classFiles = runConformModuleTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"	java.sql.Connection con;\n" +
				"}",
				"module-info.java",
				"module mod.one { \n" +
				"	requires java.base;\n" +
				"	requires java.sql;\n" +
				"	requires java.desktop;\n" +
				"}",
				"q/Y.java",
				"package q;\n" +
				"public class Y {\n" +
				"   java.awt.Image image;\n" +
				"}"
	        },
			" -9 \"" + OUTPUT_DIR + File.separator + "module-info.java\" "
			+ "\"" + OUTPUT_DIR + File.separator + "q/Y.java\" "
	        + "\"" + OUTPUT_DIR + File.separator + "p/X.java\" "
	        + "-d " + OUTPUT_DIR ,
	        "",
	        "",
	        true);
		String fileName = OUTPUT_DIR  + File.separator + "module-info.class";
		assertClassFile("Missing modul-info.class: " + fileName, fileName, classFiles);
	}
	public void test006() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires java.sql;\n" +
						"	requires java.desktop;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"package p;\n" +
						"public class X {\n" +
						"	java.sql.Connection con;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"package q;\n" +
						"public class Y {\n" +
						"   java.awt.Image image;\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");
		runConformModuleTest(files, buffer, "", "", false);
	}
	public void test007() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires java.base;\n" +
						"	requires mod.one;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"package q;\n" +
						"public class Y {\n" +
						"   java.sql.Connection con = p.X.getConnection();\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");
		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 3)\n" +
				"	java.sql.Connection con = p.X.getConnection();\n" +
				"	                          ^^^\n" +
				"The type p.X is not accessible\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"p.X");
	}
	public void test008() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p;\n" +
						"	requires mod.two;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"package p;\n" +
						"import q.Y;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return Y.con;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	exports q;\n" +
						"	requires java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"package q;\n" +
						"public class Y {\n" +
						"   public static java.sql.Connection con = null;\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:-exports") // Y.con unreliably refers to Connection (missing requires transitive)
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
	}
	public void test008a() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p.q;\n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q", "X.java",
						"package p.q;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires java.base;\n" +
						"	requires mod.one;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q" + File.separator + "r", "Y.java",
						"package q.r;\n" +
						"public class Y {\n" +
						"   java.sql.Connection con = p.q.X.getConnection();\n" +
						"}");

		String systemDirectory = OUTPUT_DIR+File.separator+"system";
		writeFile(systemDirectory, "readme.txt", "Not a valid system");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append("--system ").append(systemDirectory)
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files,
				buffer,
				"",
				"invalid location for system libraries: ---OUTPUT_DIR_PLACEHOLDER---/system\n",
				false,
				"system");
	}
	public void test009() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p;\n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires java.base;\n" +
						"	requires mod.one;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"package q;\n" +
						"public class Y {\n" +
						"   java.sql.Connection con = p.X.getConnection();\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
	}
	private void createUnnamedLibrary(String unnamedLoc, String unnamedBin) {
		writeFile(unnamedLoc + File.separator + "s" + File.separator + "t", "Tester.java",
				"package s.t;\n" +
				"public class Tester {\n" +
				"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + unnamedBin)
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\"")
			.append(" -sourcepath \"" + unnamedLoc + "\" ")
			.append(unnamedLoc + File.separator + "s" + File.separator + "t" + File.separator + "Tester.java");

		runConformTest(new String[]{},
				buffer.toString(),
				"",
				"",
				false);
	}
	private void createReusableModules(String srcDir, String outDir, File modDir) {
		String moduleLoc = srcDir + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p;\n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		// This one is not exported (i.e. internal to this module)
		writeFileCollecting(files, moduleLoc + File.separator + "p1", "X1.java",
				"package p1;\n" +
				"public class X1 {\n" +
				"	public static java.sql.Connection getConnection() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}");

		moduleLoc = srcDir + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	exports q;\n" +
						"	requires java.base;\n" +
						"	requires mod.one;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"package q;\n" +
						"public class Y {\n" +
						"   java.sql.Connection con = p.X.getConnection();\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
			buffer.append("-d " + outDir )
			.append(" -9 ")
			.append(" --module-path \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + srcDir + "\"");
		for (String fileName : files)
			buffer.append(" \"").append(fileName).append("\"");

		runConformTest(new String[]{},
				buffer.toString(),
				"",
				"",
				false);

		String fileName = modDir + File.separator + "mod.one.jar";
		try {
			Util.zip(new File(outDir + File.separator + "mod.one"),
								fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!modDir.exists()) {
			if (!modDir.mkdirs()) {
				fail("Coult not create folder " + modDir);
			}
		}
		File mod2 = new File(modDir, "mod.two");
		if (!mod2.mkdir()) {
			fail("Coult not create folder " + mod2);
		}
		Util.copy(outDir + File.separator + "mod.two", mod2.getAbsolutePath());

		Util.flushDirectoryContent(new File(outDir));
		Util.flushDirectoryContent(new File(srcDir));
	}
	public void test010() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "r", "Z.java",
						"package r;\n" +
						"public class Z extends Object {\n" +
						"	p.X x = null;\n" +
						"	q.Y y = null;\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
			buffer.append("-d " + outDir )
			.append(" -9 ")
			.append(" -p \"")
			.append(Util.getJavaClassLibsAsString())
			.append(modDir.getAbsolutePath())
			.append("\" ")
			.append(" --module-source-path " + "\"" + srcDir + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false, outDir);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=487421
	public void test011() {
		runConformModuleTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"   java.lang.SecurityException ex = null;\n" +
				"}",
				"module-info.java",
				"module mod.one { \n" +
				"	requires java.base;\n" +
				"}"
	        },
			" -9 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "",
	        true);
	}
	// Modules used as regular -classpath (as opposed to --module-path) and module-info referencing
	// those modules are reported as missing.
	public void test012() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "r", "Z.java",
						"package r;\n" +
						"public class Z extends Object {\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
			buffer.append("-d " + outDir )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(modDir + File.separator + "mod.one.jar").append(File.pathSeparator)
			.append(modDir + File.separator + "mod.two").append(File.pathSeparator)
			.append("\" ")
			.append(" --module-source-path " + "\"" + srcDir + "\"");

		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 2)\n" +
				"	requires mod.one;\n" +
				"	         ^^^^^^^\n" +
				"mod.one cannot be resolved to a module\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 3)\n" +
				"	requires mod.two;\n" +
				"	         ^^^^^^^\n" +
				"mod.two cannot be resolved to a module\n" +
				"----------\n" +
				"2 problems (2 errors)\n",
				false,
				"module");
	}
	// Modules used as regular -classpath as opposed to --module-path. The files being compiled
	// aren't part of any modules (i.e. module-info is missing). The files should be able to
	// reference the types from referenced classpath.
	public void test013() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc + File.separator + "p", "Z.java",
						"package r;\n" +
						"public class Z extends Object {\n" +
						"	p.X x = null;\n" +
						"	q.Y y = null;\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
			buffer.append("-d " + outDir )
			.append(" -9")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(modDir + File.separator + "mod.one.jar").append(File.pathSeparator)
			.append(modDir + File.separator + "mod.two").append(File.pathSeparator)
			.append("\" ");
		runConformModuleTest(files,
				buffer,
				"",
				"",
				false,
				outDir);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=495500
	//-source 9
	public void testBug495500a() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"/** */\n" +
				"public class X {\n" +
				"}",
			},
	  "\"" + OUTPUT_DIR +  File.separator + "X.java\""
	  + " -9 -d \"" + OUTPUT_DIR + "\"",
	  "",
	  "",
	  true);
		String expectedOutput = "// Compiled from X.java (version 9 : 53.0, super bit)";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
	//-source 8 -target 9
	public void testBug495500b() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"/** */\n" +
				"public class X {\n" +
				"}",
			},
			"\"" + OUTPUT_DIR +  File.separator + "X.java\""
			+ " -9 -source 8 -target 9 -d \"" + OUTPUT_DIR + "\"",
			"",
			"",
			true);
		String expectedOutput = "// Compiled from X.java (version 9 : 53.0, super bit)";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
	// compliance 9 -source 9 -target 9
	public void testBug495500c() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"/** */\n" +
				"public class X {\n" +
				"}",
			},
			"\"" + OUTPUT_DIR +  File.separator + "X.java\""
			+ " -9 -source 9 -target 9 -d \"" + OUTPUT_DIR + "\"",
			"",
			"",
			true);
		String expectedOutput = "// Compiled from X.java (version 9 : 53.0, super bit)";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
	/*
	 * Test add-exports grants visibility to another module
	 */
	public void test014() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires java.base;\n" +
						"	requires mod.one;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"package q;\n" +
						"public class Y {\n" +
						"   java.sql.Connection con = p.X.getConnection();\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.two");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
	}
	public void test015() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires java.base;\n" +
						"	requires java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"package q;\n" +
						"public class Y {\n" +
						"   java.sql.Connection con = p.X.getConnection();\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.two");
		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 3)\n" +
				"	java.sql.Connection con = p.X.getConnection();\n" +
				"	                          ^^^\n" +
				"The type p.X is not accessible\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"cannot be resolved",
				OUTPUT_DIR + File.separator + out,
				JavacTestOptions.JavacHasABug.JavacBug8207032);
	}
	public void test016() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires java.base;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"package q;\n" +
						"public class Y {\n" +
						"   java.sql.Connection con = p.X.getConnection();\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.two")
			.append(" --add-reads mod.two=mod.one");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
	}
	public void test017() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires java.base;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"package q;\n" +
						"public class Y {\n" +
						"   java.sql.Connection con = p.X.getConnection();\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.three")
			.append(" --add-reads mod.two=mod.one");

		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 3)\n" +
				"	java.sql.Connection con = p.X.getConnection();\n" +
				"	                          ^^^\n" +
				"The type p.X is not accessible\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"visible",
				OUTPUT_DIR + File.separator + out);
	}
	public void test018() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires java.base;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"package q;\n" +
						"public class Y {\n" +
						"   java.sql.Connection con = p.X.getConnection();\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.three";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	requires java.base;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "r", "Z.java",
						"package r;\n" +
						"public class Z {\n" +
						"   java.sql.Connection con = p.X.getConnection();\n" +
						"}");


		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.two,mod.three")
			.append(" --add-reads mod.two=mod.one")
			.append(" --add-reads mod.three=mod.one");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
	}
	/*
	 * Unnamed module tries to access a type from an unexported package successfully due to --add-exports
	 */
	public void test019() {
		Runner runner = new Runner();
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		runner.createFile(moduleLoc + File.separator + "p", "X.java",
						"package p;\n" +
						"public abstract class X extends com.sun.security.ntlm.Server {\n" +
						"	//public X() {}\n" +
						"	public X(String arg0, String arg1) throws com.sun.security.ntlm.NTLMException {\n" +
						"		super(arg0, arg1);\n" +
						"	}\n" +
						"}");

		runner.commandLine.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -sourcepath " + "\"" + moduleLoc + "\" ")
			.append(" --add-exports java.base/com.sun.security.ntlm=ALL-UNNAMED ");

		runner.javacVersionOptions = JAVAC_SOURCE_9_OPTIONS; // otherwise javac: error: exporting a package from system module java.base is not allowed with --release
		runner.runConformModuleTest();
	}
	/*
	 * Named module tries to access a type from an unnamed module successfully due to --add-reads
	 */
	public void test019b() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String unnamedLoc = directory + File.separator + "nomodule";
		String unnamedBin = OUTPUT_DIR + File.separator + "un_bin";
		String moduleLoc = directory + File.separator + "mod" + File.separator + "mod.one";

		createUnnamedLibrary(unnamedLoc, unnamedBin);

		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one {\n" +
						"	exports p.q;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q", "X.java",
						"package p.q;\n" +
						"public abstract class X {\n" +
						"	s.t.Tester t;\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(unnamedBin + File.pathSeparator)
			.append("\"")
			.append(" --module-source-path \"" + directory + File.separator + "mod" + "\" ")
			.append(" --add-reads mod.one=ALL-UNNAMED ");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false,
				OUTPUT_DIR + File.separator + out);
	}

	/*
	 * Can only import from a package that contains compilation units (from the unnamed module)
	 */
	public void test019c() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String unnamedLoc = directory + File.separator + "nomodule";
		String unnamedBin = OUTPUT_DIR + File.separator + "un_bin";

		createUnnamedLibrary(unnamedLoc, unnamedBin);

		List<String> files = new ArrayList<>();
		String moduleLoc = directory + File.separator + "mod.one";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one {\n" +
						"	exports p.q;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q", "X.java",
						"package p.q;\n" +
						"import s.*;\n" +
						"import s.t.*;\n" +
						"public abstract class X {\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(unnamedBin + File.pathSeparator)
			.append("\"")
			.append(" --module-source-path \"" + directory + "\" ")
			.append(" --add-reads mod.one=ALL-UNNAMED ");

		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/q/X.java (at line 2)\n" +
				"	import s.*;\n" +
				"	       ^\n" +
				"The package s is not accessible\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"package s",
				 OUTPUT_DIR + File.separator + out,
				 JavacTestOptions.JavacHasABug.JavacBug8204534);
	}
	/*
	 * Unnamed module tries to access a type from an unexported package, fail
	 */
	public void test019fail() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"package p;\n" +
						"public abstract class X extends com.sun.security.ntlm.Server {\n" +
						"	//public X() {}\n" +
						"	public X(String arg0, String arg1) throws com.sun.security.ntlm.NTLMException {\n" +
						"		super(arg0, arg1);\n" +
						"	}\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -sourcepath " + "\"" + moduleLoc + "\" ");

		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/X.java (at line 2)\n" +
				"	public abstract class X extends com.sun.security.ntlm.Server {\n" +
				"	                                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"The type com.sun.security.ntlm.Server is not accessible\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/X.java (at line 4)\n" +
				"	public X(String arg0, String arg1) throws com.sun.security.ntlm.NTLMException {\n" +
				"	                                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"The type com.sun.security.ntlm.NTLMException is not accessible\n" +
				"----------\n" +
				"2 problems (2 errors)\n",
				false,
				"package com.sun.security.ntlm");
				/* javac9:
				 * src/mod.one/p/X.java:2: error: package com.sun.security.ntlm is not visible
				 * public abstract class X extends com.sun.security.ntlm.Server {
				 *                                                 ^
				 *   (package com.sun.security.ntlm is declared in module java.base, which does not export it to the unnamed module)
				 * src/mod.one/p/X.java:4: error: package com.sun.security.ntlm is not visible
				 * public X(String arg0, String arg1) throws com.sun.security.ntlm.NTLMException {
				 *                                                           ^
				 *   (package com.sun.security.ntlm is declared in module java.base, which does not export it to the unnamed module)
				 */
				/* javac10:
				 * src/mod.one/p/X.java:2: error: package com.sun.security.ntlm does not exist
				 * public abstract class X extends com.sun.security.ntlm.Server {
				 *                                                      ^
				 * src/mod.one/p/X.java:4: error: package com.sun.security.ntlm does not exist
				 * public X(String arg0, String arg1) throws com.sun.security.ntlm.NTLMException {
				 *                                                                ^
				 */
	}
	public void test020() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"")
			.append(" --add-exports mod.one=mod.two,mod.three");

		runNegativeModuleTest(files,
				buffer,
				"",
				"incorrectly formatted option: --add-exports mod.one=mod.two,mod.three\n",
				false,
				"option");
	}
	public void test021() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"")
			.append(" --add-reads mod.one/mod.two");

		runNegativeModuleTest(files,
				buffer,
				"",
				"incorrectly formatted option: --add-reads mod.one/mod.two\n",
				false,
				"option");
	}
	public void test022() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.three")
			.append(" --add-exports mod.one/p=mod.three");

		runNegativeModuleTest(files,
				buffer,
				"",
				"can specify a package in a module only once with --add-export\n",
				false,
				"export");
	}
	public void test023() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append("\"" + moduleLoc +  File.separator + "module-info.java\" ")
			.append(" -extdirs " + OUTPUT_DIR + File.separator + "src");

		runNegativeModuleTest(files,
				buffer,
				"",
				"option -extdirs not supported at compliance level 9 and above\n",
				false,
				"extdirs");
	}
	public void test024() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" \"" + moduleLoc +  File.separator + "module-info.java\" ")
			.append(" -bootclasspath " + OUTPUT_DIR + File.separator + "src");

		runNegativeModuleTest(files,
				buffer,
				"",
				"option -bootclasspath not supported at compliance level 9 and above\n",
				false,
				"not allowed"); // when specifying -bootclasspath javac answers: "option --boot-class-path not allowed with target 1.9" (two bugs)
	}
	public void test025() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append("\"" + moduleLoc +  File.separator + "module-info.java\" ")
			.append(" -endorseddirs " + OUTPUT_DIR + File.separator + "src");

		runNegativeModuleTest(files,
				buffer,
				"",
				"option -endorseddirs not supported at compliance level 9 and above\n",
				false,
				"endorseddirs");
	}
	public void test026() {
		Runner runner = new Runner();
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		runner.createFile(
						moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires transitive java.sql;\n" +
						"}");
		String javaHome = System.getProperty("java.home");
		runner.commandLine.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" --system \"").append(javaHome).append("\"");

		runner.javacVersionOptions = JAVAC_SOURCE_9_OPTIONS;
		runner.runConformModuleTest();
	}
	/**
	 * Mixed case of exported and non exported packages being referred to in another module
	 */
	public void test028() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "r", "Z.java",
						"package r;\n" +
						"public class Z extends Object {\n" +
						"	p.X x = null;\n" +
						"	p1.X1 x1 = null;\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + outDir )
		.append(" -9 ")
		.append(" -p \"")
		.append(Util.getJavaClassLibsAsString())
		.append(modDir.getAbsolutePath())
		.append("\" ")
		.append(" --module-source-path " + "\"" + srcDir + "\"");

		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/r/Z.java (at line 4)\n"+
				"	p1.X1 x1 = null;\n" +
				"	^^^^^\n" +
				"The type p1.X1 is not accessible\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"visible",
				outDir);
	}
	public void test029() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires java.base;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"package q;\n" +
						"public class Y {\n" +
						"   java.sql.Connection con = p.X.getConnection();\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.two,mod.three")
			.append(" --add-reads mod.two=mod.one");

		runNegativeModuleTest(files,
			buffer,
			"",
			"----------\n" +
			"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/X.java (at line 3)\n" +
			"	public static java.sql.Connection getConnection() {\n" +
			"	              ^^^^^^^^^^^^^^^^^^^\n" +
			"The type Connection from module java.sql may not be accessible to clients due to missing \'requires transitive\'\n" +
			"----------\n" +
			"----------\n" +
			"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 3)\n" +
			"	java.sql.Connection con = p.X.getConnection();\n" +
			"	^^^^^^^^^^^^^^^^^^^\n" +
			"The type java.sql.Connection is not accessible\n" +
			"----------\n" +
			"2 problems (1 error, 1 warning)\n",
			false,
			"visible");
	}
	public void test030() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires java.base;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"package q;\n" +
						"import java.sql.*;\n" +
						"public class Y {\n" +
						"   Connection con = null;\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:-exports") // getConnection() leaks non-transitively required type
			.append(" --module-source-path " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.two,mod.three")
			.append(" --add-reads mod.two=mod.one");

		runNegativeModuleTest(files,
			buffer,
			"",
			"----------\n"+
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 2)\n"+
			"	import java.sql.*;\n"+
			"	       ^^^^^^^^\n"+
			"The package java.sql is not accessible\n"+
			"----------\n"+
			"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 4)\n"+
			"	Connection con = null;\n"+
			"	^^^^^^^^^^\n"+
			"Connection cannot be resolved to a type\n"+
			"----------\n"+
			"2 problems (2 errors)\n",
			false,
			"visible",
			OUTPUT_DIR + File.separator + out);
	}
	public void test031() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"package p;\n" +
						"public class X {\n" +
						"	static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires java.base;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"package q;\n" +
						"import java.sql.Connection;\n" +
						"public class Y {\n" +
						"   Connection con = null;\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.two,mod.three")
			.append(" --add-reads mod.two=mod.one");

		runNegativeModuleTest(files,
			buffer,
			"",
			"----------\n"+
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 2)\n"+
			"	import java.sql.Connection;\n"+
			"	       ^^^^^^^^^^^^^^^^^^^\n"+
			"The type java.sql.Connection is not accessible\n"+
			"----------\n"+
			"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 4)\n"+
			"	Connection con = null;\n"+
			"	^^^^^^^^^^\n"+
			"Connection cannot be resolved to a type\n"+
			"----------\n"+
			"2 problems (2 errors)\n",
			false,
			"visible",
			OUTPUT_DIR + File.separator + out);
	}
	public void test032() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"}");
		writeFileCollecting(files, moduleLoc+"/p", "X.java",
						"package p;\n" +
						"public class X {\n" +
						"	public static class Inner {\n" +
						"	}\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files,
			buffer,
			"",
			"",
			false,
			OUTPUT_DIR + File.separator + out);
	}
	/**
	 * Test that a module can't access types/packages in a plain Jar put in classpath
	 */
	public void test033() {
		File libDir = new File(LIB_DIR);
		Util.delete(libDir); // make sure we recycle the libs
 		libDir.mkdirs();
		try {
			Util.createJar(
				new String[] {
					"a/A.java",
					"package a;\n" +
					"public class A {\n" +
					"}"
				},
				LIB_DIR + "/lib1.jar",
				JavaCore.VERSION_9);
		} catch (IOException e) {
			// ignore
		}
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"package p;\n" +
						"public class X extends a.A {\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(LIB_DIR).append(File.separator).append("lib1.jar").append(File.pathSeparator).append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");
		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/X.java (at line 2)\n" +
				"	public class X extends a.A {\n" +
				"	                       ^^^\n" +
				"The type a.A is not accessible\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"package a does not exist");
	}
	/**
	 * Test that a module can't access types/packages in a plain Jar put in modulepath
	 * but not explicitly added to the "requires" clause
	 */
	public void test034() {
		File libDir = new File(LIB_DIR);
		Util.delete(libDir); // make sure we recycle the libs
 		libDir.mkdirs();
		try {
			Util.createJar(
				new String[] {
					"a/A.java",
					"package a;\n" +
					"public class A {\n" +
					"}"
				},
				LIB_DIR + "/lib1.jar",
				JavaCore.VERSION_9);
		} catch (IOException e) {
			// ignore
		}
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"package p;\n" +
						"public class X extends a.A {\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString()).append("\" ")
			.append("-p \"")
			.append(LIB_DIR).append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");
		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/X.java (at line 2)\n" +
				"	public class X extends a.A {\n" +
				"	                       ^^^\n" +
				"The type a.A is not accessible\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"does not read");
	}
	/**
	 * Test that a module can access types/packages in a plain Jar put in modulepath
	 * and explicitly added to the "requires" clause
	 */
	public void test035() {
		File libDir = new File(LIB_DIR);
		Util.delete(libDir); // make sure we recycle the libs
 		libDir.mkdirs();
		try {
			Util.createJar(
				new String[] {
					"a/A.java",
					"package a;\n" +
					"public class A {\n" +
					"}"
				},
				LIB_DIR + "/lib1.jar",
				JavaCore.VERSION_9);
		} catch (IOException e) {
			// ignore
		}
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires java.sql;\n" +
						"	requires lib1;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"package p;\n" +
						"public class X extends a.A {\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString()).append("\" ")
			.append("-p \"")
			.append(LIB_DIR).append("\" ")
			.append(" -warn:-module ")
			.append(" --module-source-path " + "\"" + directory + "\"");
		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
	}
	public void testBug515985() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "impl", "Other.java",
						"package impl;\n" +
						"public class Other {\n" +
						"    public void privateMethod() {}" +
						"}\n");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"package pm;\n" +
						"import impl.Other;\n" +
						"public class C1 extends Other {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires mod.one;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "po", "Client.java",
						"package po;\n" +
						"import pm.C1;\n" +
						"public class Client {\n" +
						"    void test1(C1 one) {\n" +
						"        one.privateMethod(); // ecj: The method privateMethod() is undefined for the type C1\n" +
						"    }\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
	}

	public void testApiLeak1() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "impl", "Other.java",
						"package impl;\n" +
						"public class Other {\n" +
						"}\n");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"package pm;\n" +
						"import impl.Other;\n" +
						"public class C1 extends Other {\n" +
						"	public void m1(Other o) {}\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires mod.one;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "impl", "Other.java",
						"package impl;\n" +
						"public class Other {\n" +
						"}\n");
		writeFileCollecting(files, moduleLoc + File.separator + "po", "Client.java",
						"package po;\n" +
						"import pm.C1;\n" +
						"public class Client {\n" +
						"    void test1(C1 one) {\n" +
						"        one.m1(one);\n" +
						"    }\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/pm/C1.java (at line 4)\n" +
				"	public void m1(Other o) {}\n" +
				"	               ^^^^^\n" +
				"The type Other is not exported from this module\n" +
				"----------\n" +
				"1 problem (1 warning)\n",
				false);
	}

	/**
	 * Same-named classes should not conflict, since one is not accessible.
	 * Still a sub class of the inaccessible class can be accessed and used for a method argument.
	 */
	public void testApiLeak2() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "impl", "SomeImpl.java",
						"package impl;\n" +
						"public class SomeImpl {\n" +
						"}\n");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"package pm;\n" +
						"import impl.SomeImpl;\n" +
						"public class C1 {\n" +
						"	public void m1(SomeImpl o) {}\n" +
						"}\n");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "Other.java",
						"package pm;\n" +
						"import impl.SomeImpl;\n" +
						"public class Other extends SomeImpl {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires mod.one;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "impl", "SomeImpl.java",
						"package impl;\n" +
						"public class SomeImpl {\n" + // pseudo-conflict to same named, but inaccessible class from mod.one
						"}\n");
		writeFileCollecting(files, moduleLoc + File.separator + "po", "Client.java",
						"package po;\n" +
						"import pm.C1;\n" +
						"import pm.Other;\n" +
						"import impl.SomeImpl;\n" +
						"public class Client {\n" +
						"    void test1(C1 one) {\n" +
						"		 SomeImpl impl = new SomeImpl();\n" + // our own version
						"        one.m1(impl);\n" + // incompatible to what's required
						"		 one.m1(new Other());\n" + // OK
						"    }\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -info:+exports")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. INFO in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/pm/C1.java (at line 4)\n" +
				"	public void m1(SomeImpl o) {}\n" +
				"	               ^^^^^^^^\n" +
				"The type SomeImpl is not exported from this module\n" +
				"----------\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/po/Client.java (at line 8)\n" +
				"	one.m1(impl);\n" +
				"	    ^^\n" +
				"The method m1(impl.SomeImpl) in the type C1 is not applicable for the arguments (impl.SomeImpl)\n" +
				"----------\n" +
				"2 problems (1 error, 0 warnings, 1 info)\n",
				false,
				"incompatible",
				OUTPUT_DIR + File.separator + out);
	}

	// conflict even without any reference to the conflicting package
	// - three-way conflict between two direct and one indirect dependency
	public void testPackageConflict0() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.x";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.x { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1x.java",
						"package pm;\n" +
						"public class C1x {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.y";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.y { \n" +
						"	requires transitive mod.x;\n" +
						"}");

		moduleLoc = directory + File.separator + "mod.one";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports pm;\n" +
						"	exports p2;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");
		writeFileCollecting(files, moduleLoc + File.separator + "p2", "C2.java",
						"package p2;\n" +
						"public class C2 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	exports pm;\n" +
						"	exports p2.sub;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C3.java",
						"package pm;\n" +
						"public class C3 {\n" +
						"}\n");
		writeFileCollecting(files, moduleLoc + File.separator + "p2" + File.separator + "sub", "C4.java",
						"package p2.sub;\n" +
						"public class C4 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.three";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"	requires transitive mod.y;\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 2)\n" +
				"	requires mod.one;\n" +
				"	^^^^^^^^^^^^^^^^\n" +
				"The package pm is accessible from more than one module: mod.one, mod.two, mod.x\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 3)\n" +
				"	requires mod.two;\n" +
				"	^^^^^^^^^^^^^^^^\n" +
				"The package pm is accessible from more than one module: mod.one, mod.two, mod.x\n" +
				"----------\n" +
				"3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 4)\n" +
				"	requires transitive mod.y;\n" +
				"	^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"The package pm is accessible from more than one module: mod.one, mod.two, mod.x\n" +
				"----------\n" +
				"3 problems (3 errors)\n",
				false,
				"reads package pm");
	}

	public void testPackageConflict1() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports pm;\n" +
						"	exports p2;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");
		writeFileCollecting(files, moduleLoc + File.separator + "p2", "C2.java",
						"package p2;\n" +
						"public class C2 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	exports pm;\n" +
						"	exports p2.sub;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C3.java",
						"package pm;\n" +
						"public class C3 {\n" +
						"}\n");
		writeFileCollecting(files, moduleLoc + File.separator + "p2" + File.separator + "sub", "C4.java",
						"package p2.sub;\n" +
						"public class C4 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.three";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "po", "Client.java",
						"package po;\n" +
						"import pm.*;\n" +
						"import pm.C3;\n" +
						"import p2.C2;\n" +
						"public class Client {\n" +
						"    void test1(C1 one) {\n" +
						"    }\n" +
						"	 pm.C1 f1;\n" +
						"	 p2.sub.C4 f4;\n" + // no conflict mod.one/p2 <-> mod.two/p2.sub
						"}\n");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 2)\n" +
				"	requires mod.one;\n" +
				"	^^^^^^^^^^^^^^^^\n" +
				"The package pm is accessible from more than one module: mod.one, mod.two\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 3)\n" +
				"	requires mod.two;\n" +
				"	^^^^^^^^^^^^^^^^\n" +
				"The package pm is accessible from more than one module: mod.one, mod.two\n" +
				"----------\n" +
				"----------\n" +
				"3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/po/Client.java (at line 2)\n" +
				"	import pm.*;\n" +
				"	       ^^\n" +
				"The package pm is accessible from more than one module: mod.one, mod.two\n" +
				"----------\n" +
				"4. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/po/Client.java (at line 3)\n" +
				"	import pm.C3;\n" +
				"	       ^^\n" +
				"The package pm is accessible from more than one module: mod.one, mod.two\n" +
				"----------\n" +
				"5. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/po/Client.java (at line 8)\n" +
				"	pm.C1 f1;\n" +
				"	^^\n" +
				"The package pm is accessible from more than one module: mod.one, mod.two\n" +
				"----------\n" +
				"5 problems (5 errors)\n",
				false,
				"reads package pm");
	}
	// conflict foreign<->local package
	public void testPackageConflict3() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		List<String> files = new ArrayList<>();
		String moduleLoc = directory + File.separator + "mod.one";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	requires mod.one;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C3.java",
						"package pm;\n" +
						"public class C3 {\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/pm/C3.java (at line 1)\n" +
				"	package pm;\n" +
				"	        ^^\n" +
				"The package pm conflicts with a package accessible from another module: mod.one\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"",
				OUTPUT_DIR + File.separator + out);
	}
	public void testPackageConflict4() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		File srcDir = new File(directory);

		String moduleLoc = directory + File.separator + "mod.x";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.x { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.y";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.y { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
		Util.flushDirectoryContent(srcDir);
		files.clear();
		writeFileCollecting(files, directory + File.separator + "p", "X.java",
						"public class X extends pm.C1 { \n" +
						"}");
		buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\"");
		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 1)\n" +
				"	public class X extends pm.C1 { \n" +
				"	                       ^^\n" +
				"The package pm is accessible from more than one module: mod.x, mod.y\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"package conflict");
	}
	/**
	 * currently disabled because ECJ allows unnamed modules to read from other modules from
	 * module-path even if they are not part of root modules.
	 */
	public void _testPackageConflict4a() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		File srcDir = new File(directory);

		String moduleLoc = directory + File.separator + "mod.x";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.x { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
		Util.flushDirectoryContent(srcDir);
		files.clear();
		writeFileCollecting(files, directory + File.separator + "p", "X.java",
						"public class X extends pm.C1 { \n" +
						"}");
		buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\"");
		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 1)\n" +
				"	public class X extends pm.C1 { \n" +
				"	                       ^^\n" +
				"pm cannot be resolved to a type\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"package conflict");
	}
	public void testPackageConflict5() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		File srcDir = new File(directory);

		String moduleLoc = directory + File.separator + "mod.x";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.x { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.y";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.y { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
		Util.flushDirectoryContent(srcDir);
		files.clear();
		writeFileCollecting(files, directory + File.separator + "p", "X.java",
						"public class X extends pm.C1 { \n" +
						"}");
		buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\"")
			.append(" --add-modules mod.x,mod.y");
		runNegativeModuleTest(files,
				buffer,
				"",
				"The package pm is accessible from more than one module: mod.y, mod.x\n",
				false,
				"reads package pm from both");
		buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\"")
			.append(" --add-modules mod.x,mod.z");
		runNegativeModuleTest(files,
				buffer,
				"",
				"invalid module name: mod.z\n",
				false,
				"module not found");
	}
	public void testPackageConflict6() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		File srcDir = new File(directory);

		String moduleLoc = directory + File.separator + "mod.x";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.x { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.y";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.y { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
		Util.flushDirectoryContent(srcDir);
		files.clear();
		writeFileCollecting(files, directory + File.separator + "p", "X.java",
						"public class X extends pm.C1 { \n" +
						"}");
		buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\"")
			.append(" --add-modules mod.x,");
		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 1)\n" +
				"	public class X extends pm.C1 { \n" +
				"	                       ^^\n" +
				"The package pm is accessible from more than one module: mod.x, mod.y\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"package conflict");
	}
	public void testPackageConflict7() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		File srcDir = new File(directory);

		String moduleLoc = directory + File.separator + "mod.x";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.x { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.y";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.y { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
		Util.flushDirectoryContent(srcDir);
		files.clear();
		writeFileCollecting(files, directory + File.separator + "p", "X.java",
						"public class X { \n" +
						"}");
		buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\"")
			.append(" --add-modules mod.x,mod.y");
		runNegativeModuleTest(files,
				buffer,
				"",
				"The package pm is accessible from more than one module: mod.y, mod.x\n",
				false,
				"reads package pm from both");
	}
	public void testPackageTypeConflict1() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p1" + File.separator + "p2", "t3.java",
						"package p1.p2;\n" +
						"public class t3 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	exports p1.p2.t3;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p1" + File.separator + "p2" + File.separator + "t3", "t4.java",
						"package p1.p2.t3;\n" +
						"public class t4 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.three";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "po", "Client.java",
						"package po;\n" +
						"public class Client {\n" +
						"	 p1.p2.t3.t4 f;\n" + // no conflict mod.one/p1.p2.t3 <-> mod.two/p1.p2.t3
						"}\n");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
	}

	public void testBug519922() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		List<String> files = new ArrayList<>();
		writeFileCollecting(files, directory + File.separator + "test", "Test.java",
						"package test;\n" +
						"\n" +
						"public class Test implements org.eclipse.SomeInterface {\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ");

		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/test/Test.java (at line 3)\n" +
				"	public class Test implements org.eclipse.SomeInterface {\n" +
				"	                             ^^^^^^^^^^^\n" +
				"org.eclipse cannot be resolved to a type\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"does not exist");
	}
	public void testMixedSourcepath() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod" + File.separator + "mod.one";

		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one {\n" +
						"	exports p.q;\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\"")
			.append(" -sourcepath \"" + directory + "\" ")
			.append(" --module-source-path \"" + directory + File.separator + "mod" + "\" ")
			.append(" --add-reads mod.one=ALL-UNNAMED ");

		runNegativeModuleTest(files,
				buffer,
				"",
				"cannot specify both -source-path and --module-source-path\n",
				false,
				"cannot specify both",
				OUTPUT_DIR + File.separator + out);
	}

	// causes: non-public type (C0), non-exported package (p.priv)
	// locations: field, method parameter, method return
	public void testAPILeakDetection1() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p.exp;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp", "C1.java",
						"package p.exp;\n" +
						"import p.priv.*;\n" +
						"class C0 {\n" +
						"	public void test(C0 c) {}\n" +
						"}\n" +
						"public class C1 {\n" +
						"	public C2 f;\n" +
						"	public void test1(C0 c) {}\n" +
						"	public void test2(C2 c) {}\n" +
						"	protected void test3(C0 c) {}\n" +
						"	protected void test4(C2 c) {}\n" +
						"	public p.priv.C2 test5() { return null; }\n" +
						"}\n");

		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "priv", "C2.java",
						"package p.priv;\n" +
						"public class C2 {\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -err:exports")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files, buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 7)\n" +
				"	public C2 f;\n" +
				"	       ^^\n" +
				"The type C2 is not exported from this module\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 8)\n" +
				"	public void test1(C0 c) {}\n" +
				"	                  ^^\n" +
				"The type C0 is not accessible to clients that require this module\n" +
				"----------\n" +
				"3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 9)\n" +
				"	public void test2(C2 c) {}\n" +
				"	                  ^^\n" +
				"The type C2 is not exported from this module\n" +
				"----------\n" +
				"4. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 12)\n" +
				"	public p.priv.C2 test5() { return null; }\n" +
				"	       ^^^^^^^^^\n" +
				"The type C2 is not exported from this module\n" +
				"----------\n" +
				"4 problems (4 errors)\n",
				false,
				"is not exported");
	}

	// details: in array, parameterized type
	public void testAPILeakDetection2() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p.exp;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp", "C1.java",
						"package p.exp;\n" +
						"import java.util.*;\n" +
						"class C0 {\n" +
						"	public void test(C0 c) {}\n" +
						"}\n" +
						"public class C1 {\n" +
						"	public List<C0> f1;\n" +
						"	public C0[] f2;\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -err:+exports")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files, buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 7)\n" +
				"	public List<C0> f1;\n" +
				"	            ^^\n" +
				"The type C0 is not accessible to clients that require this module\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 8)\n" +
				"	public C0[] f2;\n" +
				"	       ^^\n" +
				"The type C0 is not accessible to clients that require this module\n" +
				"----------\n" +
				"2 problems (2 errors)\n",
				false,
				"not accessible to clients");
	}

	// suppress
	public void testAPILeakDetection3() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p.exp;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp", "C1.java",
						"package p.exp;\n" +
						"import java.util.*;\n" +
						"class C0 {\n" +
						"	public void test(C0 c) {}\n" +
						"}\n" +
						"public class C1 {\n" +
						"	@SuppressWarnings(\"exports\")\n" +
						"	public List<C0> f1;\n" +
						"	@SuppressWarnings(\"exports\")\n" +
						"	public C0[] f2;\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:+exports,+suppress")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
	}

	// details: nested types
	public void testAPILeakDetection4() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p.exp;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp", "C1.java",
						"package p.exp;\n" +
						"public class C1 {\n" +
						"	static class C3 {\n" +
						"		public static class C4 {}\n" + // public but nested in non-public
						"	}\n" +
						"	public C3 f1;\n" +
						"	public C3.C4 f2;\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -err:+exports")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files, buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 6)\n" +
				"	public C3 f1;\n" +
				"	       ^^\n" +
				"The type C1.C3 is not accessible to clients that require this module\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 7)\n" +
				"	public C3.C4 f2;\n" +
				"	       ^^^^^\n" +
				"The type C1.C3.C4 is not accessible to clients that require this module\n" +
				"----------\n" +
				"2 problems (2 errors)\n",
				false,
				"one is not accessible to clients");
	}

	// type from non-transitive required module
	public void testAPILeakDetection5() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p.exp1;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp1", "C1.java",
						"package p.exp1;\n" +
						"public class C1 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	exports p.exp2;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp2", "C2.java",
						"package p.exp2;\n" +
						"public class C2 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.three";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	requires mod.one; // missing transitive\n" +
						"	requires transitive mod.two;\n" +
						"	exports p.exp3;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp3", "C3.java",
						"package p.exp3;\n" +
						"public class C3 {\n" +
						"	public void m1(p.exp1.C1 arg) {}\n" +
						"	public void m2(p.exp2.C2 arg) {}\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -err:+exports")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files, buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/p/exp3/C3.java (at line 3)\n" +
				"	public void m1(p.exp1.C1 arg) {}\n" +
				"	               ^^^^^^^^^\n" +
				"The type C1 from module mod.one may not be accessible to clients due to missing \'requires transitive\'\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"is not indirectly exported");
	}

	// annotated types in API
	public void testAPILeakDetection6() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p.exp;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp", "C1.java",
						"package p.exp;\n" +
						"import java.lang.annotation.*;\n" +
						"@Target(ElementType.TYPE_USE)\n" +
						"@interface ANN {}\n" +
						"class C0 {}\n" +
						"public class C1 {\n" +
						"	public @ANN String f1;\n" +
						"	public @ANN C0 f3;\n" +
						"	public @ANN String test(@ANN String arg, @ANN C0 c) { return \"\"; }\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -err:exports")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files, buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 8)\n" +
				"	public @ANN C0 f3;\n" +
				"	            ^^\n" +
				"The type C0 is not accessible to clients that require this module\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 9)\n" +
				"	public @ANN String test(@ANN String arg, @ANN C0 c) { return \"\"; }\n" +
				"	                                              ^^\n" +
				"The type C0 is not accessible to clients that require this module\n" +
				"----------\n" +
				"2 problems (2 errors)\n",
				false,
				"is not accessible to clients");
	}

	// enum API
	public void testAPILeakDetection7() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p.exp;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp", "C1.java",
						"package p.exp;\n" +
						"public enum C1 {\n" +
						"	X, Y, Z;\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -err:exports")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files, buffer,
				"",
				"",
				false);
	}

	public void testBug486013_comment27() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String projLoc = directory + File.separator + "Proj";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, projLoc + File.separator + "p" + File.separator + "exp", "C1.java",
						"package p.exp;\n" +
						"import java.util.*;\n" +
						"public class C1 {\n" +
						"	List<?> l;\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -err:exports");

		runConformModuleTest(files, buffer,
				"",
				"",
				false);
	}
	public void testBug518295a() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + outDir )
		.append(" -9 ")
		.append(" -p \"")
		.append(Util.getJavaClassLibsAsString())
		.append(modDir.getAbsolutePath())
		.append("\" ")
		.append("-classNames mod.one/p.XYZ")
		.append(" --module-source-path " + "\"" + srcDir + "\"");

		files.forEach(name -> buffer.append(" \"" + name + "\""));
		runNegativeTest(new String[0],
				buffer.toString(),
				"",
				"invalid class name: mod.one/p.XYZ\n",
				false);
	}
	public void testBug518295b() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + outDir )
		.append(" -9 ")
		.append(" -p \"")
		.append(Util.getJavaClassLibsAsString())
		.append(modDir.getAbsolutePath())
		.append("\" ")
		.append("-classNames mod.xyz/p.X")
		.append(" --module-source-path " + "\"" + srcDir + "\"");

		files.forEach(name -> buffer.append(" \"" + name + "\""));
		runNegativeTest(new String[0],
				buffer.toString(),
				"",
				"invalid module name: mod.xyz\n",
				false);
	}
	public void testBug518295c() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + outDir )
		.append(" -9 ")
		.append(" -p \"")
		.append(Util.getJavaClassLibsAsString())
		.append(modDir.getAbsolutePath())
		.append("\" ")
		.append("-classNames mod.one/p.X")
		.append(" --module-source-path " + "\"" + srcDir + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false,
				outDir);
	}
	public void testUnnamedPackage_Bug520839() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"}");
		writeFileCollecting(files, moduleLoc, "X.java",
						"public class X {\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files,
			buffer,
			"",
			"----------\n" +
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/X.java (at line 1)\n" +
			"	public class X {\n" +
			"	^\n" +
			"Must declare a named package because this compilation unit is associated to the named module \'mod.one\'\n" +
			"----------\n" +
			"1 problem (1 error)\n",
			false,
			"unnamed package is not allowed in named modules",
			OUTPUT_DIR + File.separator + out);
	}
	public void testAutoModule1() throws Exception {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);

		String[] sources = {
			"p/a/X.java",
			"package p.a;\n" +
			"public class X {}\n;"
		};
		String jarPath = OUTPUT_DIR + File.separator + "lib-x.jar";
		Util.createJar(sources, jarPath, "1.8");

		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires lib.x;\n" + // lib.x is derived from lib-x.jar
						"}");
		writeFileCollecting(files, moduleLoc+File.separator+"q", "X.java",
						"package q;\n" +
						"public class X {\n" +
						"	p.a.X f;\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -info:+module ")
			.append(" --module-path " + "\"" + jarPath + "\"");

		runConformModuleTest(files,
			buffer,
			"",
			"----------\n" +
			"1. INFO in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 2)\n" +
			"	requires lib.x;\n" +
			"	         ^^^^^\n" +
			"Name of automatic module \'lib.x\' is unstable, it is derived from the module\'s file name.\n" +
			"----------\n" +
			"1 problem (1 info)\n",
			false,
			OUTPUT_DIR + File.separator + out);
	}
	public void testBug521458a() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod._3_ { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + outDir )
		.append(" -9 ")
		.append(" -p \"")
		.append(Util.getJavaClassLibsAsString())
		.append(modDir.getAbsolutePath())
		.append("\" ")
		.append("-classNames mod.one/p.X")
		.append(" --module-source-path " + "\"" + srcDir + "\"");

		runNegativeModuleTest(files,
				buffer,
				"",
				"module name mod._3_ does not match expected name mod.three\n",
				false,
				"does not match expected name");
	}
	/*
	 * Disabled because the parser seem to take the module path as mod and not mod.e
	 */
	public void _testBug521458b() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.3 { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + outDir )
		.append(" -9 ")
		.append(" -p \"")
		.append(Util.getJavaClassLibsAsString())
		.append(modDir.getAbsolutePath())
		.append("\" ")
		.append("-classNames mod.one/p.X")
		.append(" --module-source-path " + "\"" + srcDir + "\"");

		runNegativeModuleTest(files,
				buffer,
				"",
				"module name mod.3 does not match expected name mod.three\r\n",
				false,
				outDir);
	}
public void testBug521362_emptyFile() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p1;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p1", "X.java",
						"");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files,
			buffer,
			"",
			"----------\n" +
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 2)\n" +
			"	exports p1;\n" +
			"	        ^^\n" +
			"The package p1 does not exist or is empty\n" +
			"----------\n" +
			"1 problem (1 error)\n",
			false,
			"empty",
			OUTPUT_DIR + File.separator + out);
	}
	public void testBug521362_mismatchingdeclaration() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p1;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p1", "X.java",
						"package q;\n");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files,
			buffer,
			"",
			"----------\n" +
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 2)\n" +
			"	exports p1;\n" +
			"	        ^^\n" +
			"The package p1 does not exist or is empty\n" +
			"----------\n" +
			"1 problem (1 error)\n",
			false,
			"package is empty",
			OUTPUT_DIR + File.separator + out);
	}
	public void testBug521362_multiplePackages() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p1;\n" +
						"	exports p2;\n" +
						"	exports p3;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p1", "X.java",
						"package q;\n");
		writeFileCollecting(files, moduleLoc + File.separator + "p2", "X.java",
				"package q2;\n");
		writeFileCollecting(files, moduleLoc + File.separator + "p3", "X.java",
				"package p3;\n");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files,
			buffer,
			"",
			"----------\n" +
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 2)\n" +
			"	exports p1;\n" +
			"	        ^^\n" +
			"The package p1 does not exist or is empty\n" +
			"----------\n" +
			"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 3)\n" +
			"	exports p2;\n" +
			"	        ^^\n" +
			"The package p2 does not exist or is empty\n" +
			"----------\n" +
			"2 problems (2 errors)\n",
			false,
			"package is empty",
			OUTPUT_DIR + File.separator + out);
	}
	public void testBug521362_multiplePackages2() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p1;\n" +
						"	exports p2;\n" +
						"	exports p3.p4.p5;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p1", "X.java",
						"package q;\n");
		writeFileCollecting(files, moduleLoc + File.separator + "p2", "X.java",
				"package q2;\n");
		writeFileCollecting(files, moduleLoc + File.separator + "p3" + File.separator + "p4" + File.separator + "p5", "X.java",
				"package p3.p4.p5;\n");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files,
			buffer,
			"",
			"----------\n" +
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 2)\n" +
			"	exports p1;\n" +
			"	        ^^\n" +
			"The package p1 does not exist or is empty\n" +
			"----------\n" +
			"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 3)\n" +
			"	exports p2;\n" +
			"	        ^^\n" +
			"The package p2 does not exist or is empty\n" +
			"----------\n" +
			"2 problems (2 errors)\n",
			false,
			"package is empty",
			OUTPUT_DIR + File.separator + out);
	}
	/*
	 * Test that when module-info is the only file being compiled, the class is still
	 * generated inside the module's sub folder.
	 */
	public void testBug500170a() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.sql;\n" +
						"}");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ")
			.append(moduleLoc + File.separator + "module-info.java");

		Set<String> classFiles = runConformModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"",
				false);
		String fileName = OUTPUT_DIR + File.separator + out + File.separator + "mod.one" + File.separator + "module-info.class";
		assertClassFile("Missing modul-info.class: " + fileName, fileName, classFiles);
	}
	/*
	 * Test that no NPE is thrown when the module-info is compiled at a level below 9
	 */
	public void testBug500170b() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.sql;\n" +
						"}");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -8")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ");

		runNegativeModuleTest(files,
				buffer,
				"",
				"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 1)
					module mod.one {\s
					^^^^^^
				Syntax error on token(s), misplaced construct(s)
				----------
				2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 1)
					module mod.one {\s
					^^^^^^^^^^^^^^
				Syntax error on token(s), misplaced construct(s)
				----------
				3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 2)
					requires java.sql;
					             ^
				Syntax error on token ".", , expected
				----------
				4. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 3)
					}
					^
				Syntax error on token "}", delete this token
				----------
				4 problems (4 errors)
				""",
				false,
				"modules are not supported");
	}
	public void testBug522472c() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		File srcDir = new File(directory);
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"module mod.one { \n" +
				"	exports x.y.z;\n" +
				"	exports a.b.c;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "x" + File.separator + "y" + File.separator + "z",
				"X.java",
				"package x.y.z;\n");
		writeFileCollecting(files, moduleLoc + File.separator + "a" + File.separator + "b" + File.separator + "c",
				"A.java",
				"package a.b.c;\n" +
				"public class A {}");

		moduleLoc = directory + File.separator + "mod.one.a";
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"module mod.one.a { \n" +
				"	exports x.y.z;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "x" + File.separator + "y" + File.separator + "z",
				"X.java",
				"package x.y.z;\n" +
				"public class X {}\n");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);

		Util.flushDirectoryContent(srcDir);
		files.clear();
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"module mod.two { \n" +
					"	requires mod.one;\n" +
					"	requires mod.one.a;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "r",
				"Main.java",
				"package p.q.r;\n" +
				"import a.b.c.*;\n" +
				"import x.y.z.*;\n" +
				"@SuppressWarnings(\"unused\")\n" +
				"public class Main {"
				+ "}");
		buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ");
		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/module-info.java (at line 2)\n" +
				"	requires mod.one;\n" +
				"	^^^^^^^^^^^^^^^^\n" +
				"The package x.y.z is accessible from more than one module: mod.one, mod.one.a\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/module-info.java (at line 3)\n" +
				"	requires mod.one.a;\n" +
				"	^^^^^^^^^^^^^^^^^^\n" +
				"The package x.y.z is accessible from more than one module: mod.one, mod.one.a\n" +
				"----------\n" +
				"2 problems (2 errors)\n",
				false,
				"module mod.two reads package x.y.z from both mod.one and mod.one.a");
	}
	public void testReleaseOption1() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"/** */\n" +
					"public class X {\n" +
					"}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		     + " --release 8 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "",
		     true);
		String expectedOutput = "// Compiled from X.java (version 1.8 : 52.0, super bit)";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
	public void testReleaseOption2() throws Exception {
		if (!isJRE17Plus) return;
		this.runConformTest(
				new String[] {
					"X.java",
					"/** */\n" +
					"public class X {\n" +
					"}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		     + " --release 10 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "",
		     true);
		String expectedOutput = "// Compiled from X.java (version 10 : 54.0, super bit)";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
	public void testReleaseOption3() throws Exception {
		if (!isJRE17Plus) return;
		this.runConformTest(
				new String[] {
					"X.java",
					"/** */\n" +
					"public class X {\n" +
					"}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		     + " --release 10 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "",
		     true);
		String expectedOutput = "// Compiled from X.java (version 10 : 54.0, super bit)";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
	public void testReleaseOption4() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"/** */\n" +
					"public class X {\n" +
					"}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		     + " --release 6 -source 1.6 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "option -source is not supported when --release is used\n",
		     true);
	}
	public void testReleaseOption5() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"/** */\n" +
					"public class X {\n" +
					"}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		     + " --release 8 -target 1.8 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "option -target is not supported when --release is used\n",
		     true);
	}
	public void testReleaseOption6() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"/** */\n" +
					"public class X {\n" +
					"}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		     + " --release 5 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "release 5 is not found in the system\n",
		     true);
	}
	public void testReleaseOption7() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"import java.util.stream.*;\n" +
					"/** */\n" +
					"public class X {\n" +
					"	public Stream<String> emptyStream() {\n" +
					"		Stream<String> st = Stream.empty();\n" +
					"		return st;\n" +
					"	}\n" +
					"}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		     + " --release 8 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "",
		     true);
	}
	public void testReleaseOption8() throws Exception {
		if (isJRE20Plus) return;
		String output =
				isJRE12Plus ?
						"	public java.util.stream.Stream<String> emptyStream() {\n" +
						"	       ^^^^^^^^^^^^^^^^\n" +
						"java.util.stream cannot be resolved to a type\n" :
							"	public java.util.stream.Stream<String> emptyStream() {\n" +
							"	       ^^^^^^^^^^^^^^^^^^^^^^^\n" +
							"java.util.stream.Stream cannot be resolved to a type\n";

		this.runNegativeTest(
				new String[] {
					"X.java",
					"/** */\n" +
					"public class X {\n" +
					"	public java.util.stream.Stream<String> emptyStream() {\n" +
					"		return null;\n" +
					"	}\n" +
					"}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		     + " --release 7 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "----------\n" +
    		 "1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" +
    		 output +
    		 "----------\n" +
    		 "1 problem (1 error)\n",
		     true);
	}
	public void testReleaseOption9() throws Exception {
		if (isJRE20Plus) return;
		this.runNegativeTest(
				new String[] {
					"X.java",
					"interface I {\n" +
					"  int add(int x, int y);\n" +
					"}\n" +
					"public class X {\n" +
					"  public static void main(String[] args) {\n" +
					"    I i = (x, y) -> {\n" +
					"      return x + y;\n" +
					"    };\n" +
					"  }\n" +
					"}\n",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		     + " --release 7 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "----------\n" +
    		 "1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)\n" +
    		 "	I i = (x, y) -> {\n" +
    		 "	      ^^^^^^^^^\n" +
    		 "Lambda expressions are allowed only at source level 1.8 or above\n" +
    		 "----------\n" +
    		 "1 problem (1 error)\n",
		     true);
	}
	public void testReleaseOption10() throws Exception {
		if (isJRE12Plus) return;
		this.runNegativeTest(
				new String[] {
					"X.java",
					"import java.io.*;\n" +
					"\n" +
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		try {\n" +
					"			System.out.println();\n" +
					"			Reader r = new FileReader(args[0]);\n" +
					"			r.read();\n" +
					"		} catch(IOException | FileNotFoundException e) {\n" +
					"			e.printStackTrace();\n" +
					"		}\n" +
					"	}\n" +
					"}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		     + " --release 6 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "----------\n" +
    		 "1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 9)\n" +
    		 "	} catch(IOException | FileNotFoundException e) {\n" +
    		 "	        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
    		 "Multi-catch parameters are not allowed for source level below 1.7\n" +
    		 "----------\n" +
    		 "2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 9)\n" +
    		 "	} catch(IOException | FileNotFoundException e) {\n" +
    		 "	                      ^^^^^^^^^^^^^^^^^^^^^\n" +
    		 "The exception FileNotFoundException is already caught by the alternative IOException\n" +
    		 "----------\n" +
    		 "2 problems (2 errors)\n",
		     true);
	}
	public void testReleaseOption11() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"	}\n" +
					"}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\"" +
		     " -bootclasspath " + OUTPUT_DIR + File.separator + "src " +
		     " --release 9 -d \"" + OUTPUT_DIR + "\"",
		     "",
    		 "option -bootclasspath not supported at compliance level 9 and above\n",
		     true);
	}
	public void _testReleaseOption12() throws Exception {
		String javaHome = System.getProperty("java.home");
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"	}\n" +
					"}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\"" +
		      " --system \"" + javaHome + "\"" +
		     " --release 6 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "----------\n" +
    		 "option --system not supported below compliance level 9",
		     true);
	}
	public void testReleaseOption13() {
		runConformModuleTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"	}\n" +
				"}",
				"module-info.java",
				"module mod.one { \n" +
				"	requires java.base;\n" +
				"}"
	        },
			" --release 9 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "",
	        true);
	}
	public void testReleaseOption13a() {
		Runner runner = new Runner();
		runner.createFile(
				OUTPUT_DIR +File.separator+"p", "X.java",
				"package p;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"	}\n" +
				"}");
		runner.createFile(
				OUTPUT_DIR, "module-info.java",
				"module mod.one { \n" +
				"	requires java.base;\n" +
				"}");
		runner.commandLine.append(" --release 10");
		runner.javacTestOptions = new JavacTestOptions(ClassFileConstants.JDK10);
		runner.runConformModuleTest();
	}
	public void testReleaseOption14() {
		runNegativeModuleTest(
			new String[] {
				"module-info.java",
				"module mod.one { \n" +
				"}"
			},
			" --release 8 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" ",
			"",
			"""
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/module-info.java (at line 1)
				module mod.one {\s
				^^^^^^
			Syntax error on token(s), misplaced construct(s)
			----------
			2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/module-info.java (at line 1)
				module mod.one {\s
				           ^^^
			Syntax error, insert "Identifier (" to complete MethodHeaderName
			----------
			3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/module-info.java (at line 1)
				module mod.one {\s
				           ^^^
			Syntax error, insert ")" to complete MethodDeclaration
			----------
			3 problems (3 errors)
			""",
			true,
			/*not tested with javac*/"");
	}
	// Test from https://bugs.eclipse.org/bugs/show_bug.cgi?id=526997
	public void testReleaseOption15() {
		Runner runner = new Runner();
		String fooDir = OUTPUT_DIR + File.separator + "foo";
		runner.createFile(
				fooDir, "Module.java",
				"package foo;\n" +
				"public class Module {}\n");
		runner.createFile(
				fooDir, "X.java",
				"package foo;\n" +
				"public class X { \n" +
				"	public Module getModule(String name) {\n" +
				"		return null;\n" +
				"	}\n" +
				"}");
		runner.commandLine.append(" --release 8 ");
	    runner.runConformModuleTest();
	}
	// Test from https://bugs.eclipse.org/bugs/show_bug.cgi?id=526997
	public void testReleaseOption16() {
		runNegativeModuleTest(
			new String[] {
				"foo/Module.java",
				"package foo;\n" +
				"public class Module {}\n",
				"bar/X.java",
				"package bar;\n" +
				"import foo.*;\n" +
				"public class X { \n" +
				"	public Module getModule(String name) {\n" +
				"		return null;\n" +
				"	}\n" +
				"}"
	        },
			" -source 9 \"" + OUTPUT_DIR +  File.separator + "foo" + File.separator + "Module.java\" " +
			"\"" +  OUTPUT_DIR +  File.separator + "bar" + File.separator + "X.java\" ",
	        "",
	        "----------\n" +
    		"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/bar/X.java (at line 4)\n" +
    		"	public Module getModule(String name) {\n" +
    		"	       ^^^^^^\n" +
    		"The type Module is ambiguous\n" +
    		"----------\n" +
    		"1 problem (1 error)\n",
	        true,
	        /*not tested with javac*/"");
	}
	public void testReleaseOption17() {
		runNegativeModuleTest(
			new String[] {
				"foo/Module.java",
				"package foo;\n" +
				"public class Module {}\n",
				"foo/X.java",
				"package foo;\n" +
				"public class X { \n" +
				"	public Module getModule(String name) {\n" +
				"		return null;\n" +
				"	}\n" +
				"}"
	        },
			" --release 60 \"" + OUTPUT_DIR +  File.separator + "foo" + File.separator + "Module.java\" " +
			"\"" +  OUTPUT_DIR +  File.separator + "foo" + File.separator + "X.java\" ",
	        "",
	        "release version 60 is not supported\n",
	        true,
	        /*not tested with javac*/"");
	}
	public void testReleaseOption18() {
		runNegativeModuleTest(
			new String[] {
				"X.java",
				"/** */\n" +
				"public class X {\n" +
				"}",
				},
			" --release 6 -1.8 \"" + OUTPUT_DIR +  File.separator + "foo" + File.separator + "Module.java\" " +
			"\"" +  OUTPUT_DIR +  File.separator + "foo" + File.separator + "X.java\" ",
	        "",
    		"option 1.8 is not supported when --release is used\n",
	        true,
	        /*not tested with javac*/"");
	}
	public void testReleaseOption19() {
		runNegativeModuleTest(
			new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"}",
			},
			" -9 --release 9 \"" + OUTPUT_DIR +  File.separator + "foo" + File.separator + "Module.java\" " +
			"\"" +  OUTPUT_DIR +  File.separator + "foo" + File.separator + "X.java\" ",
	        "",
    		"option 9 is not supported when --release is used\n",
	        true,
	        /*not tested with javac*/"");
	}
	public void testReleaseOption20() throws Exception {
		if (!isJRE12Plus || isJRE20Plus) return;
		this.runNegativeTest(
				new String[] {
					"X.java",
					"import java.io.*;\n" +
					"\n" +
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		String str = Integer.toUnsignedString(1, 1);\n" +
					"	}\n" +
					"}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		     + " --release 7 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "----------\n" +
    		 "1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" +
    		 "	String str = Integer.toUnsignedString(1, 1);\n" +
    		 "	                     ^^^^^^^^^^^^^^^^\n" +
    		 "The method toUnsignedString(int, int) is undefined for the type Integer\n" +
    		 "----------\n" +
    		 "1 problem (1 error)\n",
		     true);
	}
	public void testReleaseOption21() throws Exception {
		if (!isJRE12Plus) return;
		this.runConformTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		Integer.toUnsignedString(1, 1);\n" +
					"	}\n" +
					"}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		     + " --release 8 -d \"" + OUTPUT_DIR + "\"",
		     "",
    		 "",
		     true);
	}
	public void testReleaseOption22() {
		if (isJRE11Plus || isJRE12Plus) return;
		runConformTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"	}\n" +
				"}",
				"module-info.java",
				"module mod.one { \n" +
				"	requires java.base;\n" +
				"	requires java.xml.ws.annotation;\n" +
				"}"
	        },
			" --limit-modules java.base,java.xml.ws.annotation " +
			" --release 10 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "----------\n" +
    		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/module-info.java (at line 3)\n" +
    		"	requires java.xml.ws.annotation;\n" +
    		"	         ^^^^^^^^^^^^^^^^^^^^^^\n" +
    		"The module java.xml.ws.annotation has been deprecated since version 9 and marked for removal\n" +
    		"----------\n" +
    		"1 problem (1 warning)\n",
	        true);
	}
	public void testReleaseOption23() {
		if (!isJRE11Plus) return;
		runNegativeTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"	}\n" +
				"}",
				"module-info.java",
				"module mod.one { \n" +
				"	requires java.xml.ws.annotation;\n" +
				"}"
	        },
			" --limit-modules java.base,java.xml.ws.annotation " +
			" --release 11 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "invalid module name: java.xml.ws.annotation\n",
	        true);
	}
	public void testReleaseOption24() {
		if (!isJRE11Plus) return;
		runNegativeTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"	}\n" +
				"}",
				"module-info.java",
				"module mod.one { \n" +
				"	requires java.xml.ws.annotation;\n" +
				"}"
	        },
			" --limit-modules java.base,java.xml.ws.annotation " +
			" --release 12 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "invalid module name: java.xml.ws.annotation\n",
	        true);
	}
	public void testLimitModules1() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.x";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.x { \n" +
						"	exports pm;\n" +
						"	requires java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");


		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --limit-modules java.base")
			.append(" --module-source-path " + "\"" + directory + "\"");
		runNegativeModuleTest(files, buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.x/module-info.java (at line 3)\n" +
				"	requires java.sql;\n" +
				"	         ^^^^^^^^\n" +
				"java.sql cannot be resolved to a module\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"module not found");
	}
	public void testLimitModules2() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.x";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.x { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"package pm;\n" +
						"import java.sql.Connection;\n" +
						"public class C1 {\n" +
						"}\n");


		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --limit-modules java.base")
			.append(" --module-source-path " + "\"" + directory + "\"");
		runNegativeModuleTest(files, buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.x/pm/C1.java (at line 2)\n" +
				"	import java.sql.Connection;\n" +
				"	       ^^^^^^^^\n" +
				"The import java.sql cannot be resolved\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"is not visible");
	}
	public void testLimitModules3() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.x";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.x { \n" +
						"	exports pm;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"package pm;\n" +
						"public class C1 {\n" +
						"}\n");


		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --limit-modules java.sql")
			.append(" --module-source-path " + "\"" + directory + "\"");
		runConformModuleTest(files, buffer,
				"",
				"",
				false);
	}
	public void testLimitModules4() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"}");


		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + outDir )
			.append(" -9 ")
			.append(" --module-path \"")
			.append(Util.getJavaClassLibsAsString())
			.append(modDir.getAbsolutePath())
			.append("\" ")
			.append(" --limit-modules mod.one,mod.two ")
			.append(" --module-source-path " + "\"" + srcDir + "\" ");
		runConformModuleTest(files, buffer,
				"",
				"",
				false);
	}
	public void testLimitModules5() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"}");


		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + outDir )
			.append(" -9 ")
			.append(" --module-path \"")
			.append(Util.getJavaClassLibsAsString())
			.append(modDir.getAbsolutePath())
			.append("\" ")
			.append(" --limit-modules mod.one ")
			.append(" --module-source-path " + "\"" + srcDir + "\" ");
		runNegativeModuleTest(files, buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 3)\n" +
				"	requires mod.two;\n" +
				"	         ^^^^^^^\n" +
				"mod.two cannot be resolved to a module\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"");
	}
	public void testBug519600() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = srcDir + File.separator + "test";
		Runner runner = new Runner();
		runner.createFile(moduleLoc, "module-info.java",
						"module test {}");
		runner.createFile(moduleLoc + File.separator + "test", "Thing.java",
				"package test;\n" +
				"import java.util.Comparator;\n" +
				"import java.util.Iterator;\n" +
				"public abstract class Thing implements Iterator<Object> {\n" +
				"    void breaking() {\n" +
				"        remove(); // allowed (good)\n" +
				"        Iterator.super.remove(); // not 1.7-compliant (must be an error)\n" +
				"        Comparator.naturalOrder(); // not 1.7-compliant (bad error message)\n" +
				"    }\n" +
				"}\n");

		runner.commandLine.append("-d " + outDir )
			.append(" -source 9 ")
			.append(" --module-source-path " + "\"" + srcDir + "\" ");
		runner.javacVersionOptions = "-Xlint:-options"; // -source 9 already provided
		runner.runConformModuleTest();
	}
	public void testBug508889_001() throws Exception {
		this.runConformTest(
			new String[] {
				"module-info.java",
				"module mymodule {\n" +
				"}",
			},
			"\"" + OUTPUT_DIR +  File.separator + "module-info.java\""
			+ " -9 -source 9 -target 9 -d \"" + OUTPUT_DIR + "\"",
			"",
			"",
			true);
		String expectedOutput = "// Compiled from module-info.java (version 9 : 53.0, no super bit)\n" +
				" module mymodule  {\n" +
				"  // Version: \n" +
				"\n" +
				"  requires java.base;\n" +
				"\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "module-info.class", "module-info", expectedOutput);
	}
	public void testBug508889_002() throws Exception {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
								"    exports pack1;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pack1", "X11.java",
						"package pack1;\n" +
						"public class X11 {\n" +
						"}");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
		String expectedOutput = "// Compiled from module-info.java (version 9 : 53.0, no super bit)\n" +
				" module mod.one  {\n" +
				"  // Version: \n" +
				"\n" +
				"  requires java.base;\n" +
				"\n" +
				"  exports pack1;\n" +
				"\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + out + File.separator + "module-info.class", "module-info", expectedOutput);
	}
	public void testBug508889_003() throws Exception {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
								"    exports pack1;\n" +
								"    exports pack2 to second;\n" +
								"    opens pack3;\n" +
								"    opens pack4 to third;\n" +
								"    uses pack5.X51;\n" +
								"    provides pack1.I11 with pack1.X11;\n" +
								"    requires transitive java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pack1", "I11.java",
				"package pack1;\n" +
						"public interface I11 {\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pack1", "X11.java",
						"package pack1;\n" +
						"public class X11 implements I11{\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pack2", "X21.java",
				"package pack2;\n" +
				"public class X21 {\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pack3", "X31.java",
				"package pack3;\n" +
				"public class X31 {\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pack4", "X41.java",
				"package pack4;\n" +
				"public class X41 {\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pack5", "X51.java",
				"package pack5;\n" +
				"public class X51 {\n" +
				"}");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ");
		files.forEach(name -> buffer.append(" \"" + name + "\""));
		runConformTest(new String[0],
				buffer.toString(),
				"",
				"",
				false);
		String expectedOutput = "// Compiled from module-info.java (version 9 : 53.0, no super bit)\n" +
				" module mod.one  {\n" +
				"  // Version: \n" +
				"\n" +
				"  requires transitive java.sql;\n" +
				"  requires java.base;\n" +
				"\n" +
				"  exports pack1;\n" +
				"  exports pack2 to second;\n" +
				"\n" +
				"  opens pack3;\n" +
				"  opens pack4 to third;\n" +
				"\n" +
				"  uses pack5.X51\n" +
				"\n" +
				"  provides pack1.I11 with pack1.X11;\n" +
				"\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + out + File.separator + "module-info.class", "module-info", expectedOutput);
	}
	public void testBug520858() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = srcDir + File.separator + "test";
		Runner runner = new Runner();
		runner.createFile(moduleLoc, "module-info.java",
						"module test {\n" +
						"	requires org.astro;\n" +
						"}");
		runner.createFile(moduleLoc + File.separator + "p", "Test.java",
			"package p;\n" +
			"import org.astro.World;\n" +
			"public class Test {\n" +
			"	World w = null;\n" +
			"}");
		moduleLoc = srcDir + File.separator + "org.astro";
		runner.createFile(moduleLoc, "module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" +
				"}");
		runner.createFile(moduleLoc + File.separator + "org" + File.separator + "astro", "World.java",
			"package org.astro;\n" +
			"public interface World {\n" +
			"	public static String name() {\n" +
			"		return \"\";\n" +
			"	}\n" +
			"}");
		runner.commandLine.append("-d " + outDir )
			.append(" -source 9 ")
			.append(" --module-source-path " + "\"" + srcDir + "\" ");
		runner.javacVersionOptions = " -Xlint:-options";
		runner.runConformModuleTest();
	}
	public void testBug520858a() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = srcDir + File.separator + "test";
		Runner runner = new Runner();
		// not adding some files to the command line
		writeFile(moduleLoc, "module-info.java",
						"module test {\n" +
						"	requires org.astro;\n" +
						"}");
		// the only file added:
		runner.createFile(moduleLoc + File.separator + "p", "Test.java",
			"package p;\n" +
			"import org.astro.World;\n" +
			"public class Test {\n" +
			"	World w = null;\n" +
			"}");
		moduleLoc = srcDir + File.separator + "org.astro";
		writeFile(moduleLoc, "module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" +
				"}");
		writeFile(moduleLoc + File.separator + "org" + File.separator + "astro", "World.java",
			"package org.astro;\n" +
			"public interface World {\n" +
			"	public static String name() {\n" +
			"		return \"\";\n" +
			"	}\n" +
			"}");
		runner.commandLine.append("-d " + outDir )
			.append(" -source 9 ")
			.append(" --module-source-path " + "\"" + srcDir + "\" ");
		runner.javacVersionOptions = " -Xlint:-options";
		runner.runConformModuleTest();
	}
	public void testBug520858b() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = srcDir + File.separator + "test";
		Runner runner = new Runner();
		runner.createFile(moduleLoc, "module-info.java",
						"module test {\n" +
						"	requires org.astro;\n" +
						"}");
		runner.createFile(moduleLoc + File.separator + "p", "Test.java",
			"package p;\n" +
			"import org.astro.World;\n" +
			"public class Test {\n" +
			"	World w = null;\n" +
			"}");
		moduleLoc = srcDir + File.separator + "org.astro";
		// not adding this file to the command line (intentional?):
		writeFile(moduleLoc, "module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" +
				"}");
		runner.createFile(moduleLoc + File.separator + "org" + File.separator + "astro", "World.java",
			"package org.astro;\n" +
			"public interface World {\n" +
			"	public static String name() {\n" +
			"		return \"\";\n" +
			"	}\n" +
			"}");
		runner.commandLine.append("-d " + outDir )
			.append(" -source 9 ")
			.append(" --module-source-path " + "\"" + srcDir + "\" ");
		runner.javacVersionOptions = " -Xlint:-options";
		runner.runConformModuleTest();
	}
	public void testBug520858c() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = srcDir + File.separator + "test";
		List<String> files = new ArrayList<>();

		writeFileCollecting(files, moduleLoc + File.separator + "p", "Test.java",
			"package p;\n" +
			"import org.astro.World;\n" +
			"public class Test {\n" +
			"	World w = null;\n" +
			"}");
		moduleLoc = srcDir + File.separator + "org.astro";
		writeFileCollecting(files, moduleLoc, "module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "org" + File.separator + "astro", "World.java",
			"package org.astro;\n" +
			"public interface World {\n" +
			"	public static String name() {\n" +
			"		return \"\";\n" +
			"	}\n" +
			"}");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + outDir )
			.append(" -source 9 ")
			.append(" --module-source-path " + "\"" + srcDir + "\" ");
		runNegativeModuleTest(files, buffer,
				"",
				"\'---OUTPUT_DIR_PLACEHOLDER---/src/test/p/Test.java\' does not belong to a module on the module source path\n",
				false,
				"not in a module on the module source path");
	}
	public void testBug520858d() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = srcDir + File.separator + "test";
		List<String> files = new ArrayList<>();

		writeFileCollecting(files, moduleLoc + File.separator + "p", "Test.java",
			"package p;\n" +
			"import org.astro.World;\n" +
			"public class Test {\n" +
			"	World w = null;\n" +
			"}");
		moduleLoc = srcDir + File.separator + "org.astro";
		writeFileCollecting(files, moduleLoc, "module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "org" + File.separator + "astro", "World.java",
			"package org.astro;\n" +
			"public interface World {\n" +
			"	public static String name() {\n" +
			"		return \"\";\n" +
			"	}\n" +
			"}");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + outDir )
			.append(" -source 9 ")
			.append(" --module-source-path " + "\"" + srcDir + "\" ")
			.append(srcDir + File.separator + "org.astro" + File.separator + "org" + File.separator + "astro" + File.separator + "World.java ")
			.append(srcDir + File.separator + "test" + File.separator + "p" + File.separator + "Test.java");
		runNegativeModuleTest(Collections.emptyList(), buffer,
			"",
			"\'---OUTPUT_DIR_PLACEHOLDER---/src/test/p/Test.java\' does not belong to a module on the module source path\n",
			false,
			"not in a module on the module source path");
	}
	public void testBug520858e() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = srcDir + File.separator + "test";
		List<String> files = new ArrayList<>();

		writeFileCollecting(files, moduleLoc + File.separator + "p", "Test.java",
			"package p;\n" +
			"import org.astro.World;\n" +
			"public class Test {\n" +
			"	World w = null;\n" +
			"}");
		moduleLoc = srcDir + File.separator + "org.astro";
		writeFileCollecting(files, moduleLoc, "module-info.java",
				"module org.astro {\n" +
				"	exports org.astro;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "org" + File.separator + "astro", "World.java",
			"package org.astro;\n" +
			"public interface World {\n" +
			"	public static String name() {\n" +
			"		return \"\";\n" +
			"	}\n" +
			"}");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + outDir )
			.append(" -source 9 ")
			.append(" --module-source-path " + "\"" + srcDir + "\" ")
			.append(srcDir + File.separator + "org.astro" + File.separator + "org" + File.separator + "astro" + File.separator + "World.java ")
			.append(srcDir + File.separator + "org.astro" + File.separator + "module-info.java ")
			.append(srcDir + File.separator + "test" + File.separator + "p" + File.separator + "Test.java");
		runNegativeModuleTest(Collections.emptyList(), buffer,
			"",
			"\'---OUTPUT_DIR_PLACEHOLDER---/src/test/p/Test.java\' does not belong to a module on the module source path\n",
			false,
			"not in a module on the module source path");
	}
	public void testBug530575() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		File srcDir = new File(directory);

		String moduleLoc = directory + File.separator + "mod.x";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.x { \n" +
						"	exports px;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "px", "C1.java",
						"package px;\n" +
						"public class C1 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.y";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.y { \n" +
						"	exports py;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "py", "C1.java",
						"package py;\n" +
						"public class C1 {\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");
		for (String fileName : files)
			buffer.append(" \"").append(fileName).append("\"");
		runConformTest(new String[0],
				buffer.toString(),
				"",
				"",
				false);
		Util.flushDirectoryContent(srcDir);
		files.clear();
		writeFileCollecting(files, directory, "module-info.java",
				"module test { \n" +
				"	requires mod.x;\n" +
				"	requires mod.y;\n" +
				"}");
		writeFileCollecting(files, directory + File.separator + "p", "X.java",
						"package p;\n" +
						"public class X extends px.C1 { \n" +
						"	py.C1 c = null;\n" +
						"}");
		buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + File.separator + "mod.x" + File.pathSeparator + OUTPUT_DIR + File.separator + out + File.separator + "mod.y" + "\"");
		runConformModuleTest(files,
				buffer,
				"",
				"",
				false,
				OUTPUT_DIR + "javac");
	}
	/*
	 * Test that when module-info is not included in the command line, the class is still
	 * generated inside the module's sub folder.
	 */
	public void testBug533411() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	requires java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "Test.java",
				"package p;\n" +
				"public class Test {\n" +
				"	java.sql.Connection conn = null;\n" +
				"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ")
			.append(moduleLoc + File.separator + "p" + File.separator + "Test.java");

		Set<String> classFiles = runConformModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"",
				false);
		String fileName = OUTPUT_DIR + File.separator + out + File.separator + "mod.one" + File.separator + "module-info.class";
		assertClassFile("Missing modul-info.class: " + fileName, fileName, classFiles);
	}
	public void test_npe_bug535107() {
		runConformModuleTest(
				new String[] {
					"p/X.java",
					"package p;\n" +
			  		"import java.lang.annotation.*;\n" +
					"@Target(ElementType.MODULE)\n" +
					"public @interface X {\n" +
					"	ElementType value();\n" +
					"}",
					"module-info.java",
			  		"import java.lang.annotation.*;\n" +
			  		"@p.X(ElementType.MODULE)\n" +
					"module mod.one { \n" +
					"}"
		        },
				" -9 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
		        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
		        "",
		        "",
		        true);
	}
	public void testBug540067a() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						" exports p;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
				"package p;\n" +
				"public class X {\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q", "Test.java",
				"/*nothing in it */");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ")
			.append(moduleLoc + File.separator + "module-info.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "X.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "Test.java");

		runConformModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"",
				false);
	}
	public void testBug540067b() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						" exports p;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
				"package p;\n" +
				"public class X {\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q", "Test.java",
				"package p.q;");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ")
			.append(moduleLoc + File.separator + "module-info.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "X.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "Test.java");

		runConformModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"",
				false);
	}
	public void testBug540067c() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						" exports p;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
				"package p;\n" +
				"public class X {\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q", "Test.java",
				"package p.q;\n"
				+ "class Test {}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ")
			.append(moduleLoc + File.separator + "module-info.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "X.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "Test.java");

		runConformModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"",
				false);
	}
	public void testBug540067d() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						" exports p;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
				"package p;\n" +
				"public class X {\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q", "Test.java",
				"class Test {}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ")
			.append(moduleLoc + File.separator + "module-info.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "X.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "Test.java");

		runNegativeModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/q/Test.java (at line 1)\n" +
				"	class Test {}\n" +
				"	^\n" +
				"Must declare a named package because this compilation unit is associated to the named module \'mod.one\'\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"unnamed package is not allowed in named modules");
	}
	public void testBug540067e() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						" exports p;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
				"package p;\n" +
				"public class X {\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q", "Test.java",
				"import java.lang.*;");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:-unused")
			.append(" --module-source-path " + "\"" + directory + "\" ")
			.append(moduleLoc + File.separator + "module-info.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "X.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "Test.java");

		runNegativeModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/q/Test.java (at line 1)\n" +
				"	import java.lang.*;\n" +
				"	^\n" +
				"Must declare a named package because this compilation unit is associated to the named module \'mod.one\'\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"unnamed package is not allowed in named modules");
	}
	public void testBug548195() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						" exports p;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
				"package p;\n" +
				"public class X {\n" +
				"}");

		StringBuilder buffer = new StringBuilder();
		String binDir = OUTPUT_DIR + File.separator + out;
		buffer.append("-d " + binDir )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:-unused")
			.append(" --module-source-path " + "\"" + directory + "\" ")
			.append(" --module-version 47.11 ");
		String outText = isJRE9Plus ? "" : "Could not invoke method java.lang.module.ModuleDescriptor.Version.parse(), cannot validate module version.\n";
		runConformModuleTest(files, buffer, outText, "", false);

		IClassFileReader cfr = ToolFactory.createDefaultClassFileReader(binDir + File.separator + "mod.one" + File.separator + "module-info.class", IClassFileReader.CLASSFILE_ATTRIBUTES);
		assertNotNull("Error reading module-info.class", cfr);
		IClassFileAttribute[] attrs = cfr.getAttributes();
		for (IClassFileAttribute attr : attrs) {
			char[] name = attr.getAttributeName();
			if (CharOperation.equals(name, AttributeNamesConstants.ModuleName)) {
				IModuleAttribute modAttr = (IModuleAttribute) attr;
				String expectedVersion = isJRE9Plus ? "47.11" : "";
				assertEquals("version in attribute", expectedVersion, new String(modAttr.getModuleVersionValue()));
				return;
			}
		}
		fail("module attribute not found");
	}
	public void testBug548195fail() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						" exports p;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
				"package p;\n" +
				"public class X {\n" +
				"}");

		StringBuilder buffer = new StringBuilder();
		String binDir = OUTPUT_DIR + File.separator + out;
		buffer.append("-d " + binDir )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:-unused")
			.append(" --module-source-path " + "\"" + directory + "\" ")
			.append(" --module-version fourtyseven.11 ");
		if (isJRE9Plus) {
			runNegativeModuleTest(files, buffer, "", "fourtyseven.11: Version string does not start with a number\n", false, "bad value");
		} else {
			runConformModuleTest(files, buffer, "Could not invoke method java.lang.module.ModuleDescriptor.Version.parse(), cannot validate module version.\n", "", false);
		}
	}
	public void testPackageTypeConflict2() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
								"	exports p1.p2;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p1" + File.separator + "p2", "t3.java",
						"package p1.p2;\n" +
						"public class t3 {\n" +
						"}\n");

		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
							"	exports p1.p2.t3;\n" +
							"	requires mod.one;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p1" + File.separator + "p2" + File.separator + "t3", "t4.java",
						"package p1.p2.t3;\n" +
						"public class t4 {\n" +
						"}\n");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(
				files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/p1/p2/t3/t4.java (at line 1)\n" +
				"	package p1.p2.t3;\n" +
				"	        ^^^^^^^^\n" +
				"The package p1.p2.t3 collides with a type\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"package p1.p2.t3 clashes with class of same name");
	}
	public void testBug550178() throws Exception {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.three { \n" +
						"	exports pkg.invalid;\n" +
						"}");


		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + outDir )
			.append(" -9 ")
			.append(" --module-path \"")
			.append(Util.getJavaClassLibsAsString())
			.append(modDir.getAbsolutePath())
			.append("\" ")
			.append(" --module-source-path " + "\"" + srcDir + "\" ");
		runNegativeModuleTest(files, buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 2)\n" +
				"	exports pkg.invalid;\n" +
				"	        ^^^^^^^^^^^\n" +
				"The package pkg.invalid does not exist or is empty\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"");
	}
	public void testRelease565930_1() throws Exception {
		this.runConformTest(
				new String[] {
					"Dummy.java",
					"public class Dummy {\n"
					+ "	boolean b = new String(\"a\").contains(\"b\");\n"
					+ "}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "Dummy.java\""
		     + " --release 9 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "",
		     true);
	}
	public void testRelease565930_2() throws Exception {
		this.runConformTest(
				new String[] {
					"Main.java",
					"public final class Main<T extends Object> {\n"
					+ "    public void test() {\n"
					+ "    	final ClassLoader classLoader = this.getClass().getClassLoader();\n"
					+ "    } \n"
					+ "}",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "Main.java\""
		     + " --release 9 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "----------\n" +
    		 "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/Main.java (at line 3)\n" +
    		 "	final ClassLoader classLoader = this.getClass().getClassLoader();\n" +
    		 "	                  ^^^^^^^^^^^\n" +
    		 "The value of the local variable classLoader is not used\n" +
    		 "----------\n" +
    		 "1 problem (1 warning)\n",
		     true);
	}
	public void testBug571363() throws Exception {
		if (!isJRE12Plus) return;
		this.runConformTest(
			new String[] {
				"A.java",
				"public final class A {\n"
				+ "    org.w3c.dom.Element list;\n"
				+ "}",
			},
	     "\"" + OUTPUT_DIR +  File.separator + "A.java\""
	     + " -classpath " + "\"" + this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "Test571363.jar\""
	     + " --release 11 -d \"" + OUTPUT_DIR + "\"",
	     "",
	     "",
	     true);
	}
	public void testBug574097() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		String moduleLoc = srcDir + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"	exports p;\n" +
						"	requires transitive java.compiler;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "TestProcessor.java",
						"package p;\n"
						+ "import java.util.Set;\n"
						+ "import javax.annotation.processing.AbstractProcessor;\n"
						+ "import javax.annotation.processing.RoundEnvironment;\n"
						+ "import javax.lang.model.element.TypeElement;\n"
						+ "public class TestProcessor extends AbstractProcessor {\n"
						+ "	@Override\n"
						+ "	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {\n"
						+ "		return false;\n"
						+ "	}\n"
						+ "}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + outDir )
		.append(" -9 ")
		.append(" --module-path \"")
		.append(Util.getJavaClassLibsAsString())
		.append("\" ")
		.append(" --module-source-path " + "\"" + srcDir + "\"");
		for (String fileName : files)
			buffer.append(" \"").append(fileName).append("\"");

		runConformTest(new String[]{},
			buffer.toString(),
			"",
			"",
			false);
		String jarName = modDir + File.separator + "mod.one.jar";
		try {
			Util.zip(new File(outDir + File.separator + "mod.one"),
								jarName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!modDir.exists()) {
			if (!modDir.mkdirs()) {
				fail("Coult not create folder " + modDir);
			}
		}
		Util.flushDirectoryContent(new File(srcDir));
		files = new ArrayList<>();
		moduleLoc = srcDir + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.two { \n" +
						"	exports q;\n" +
						"	requires java.base;\n" +
						"	requires mod.one;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "A.java",
						"package q;\n" +
						"public class A {\n" +
						"   p.TestProcessor prc = null;\n" +
						"}");
		buffer = new StringBuilder();
		buffer.append("-d " + outDir )
		.append(" -9 ")
		.append(" --module-path \"")
		.append(Util.getJavaClassLibsAsString())
		.append("\" ")
		.append(" --module-source-path " + "\"" + srcDir + "\"")
		.append(" --processor-module-path " + "\"" + jarName + "\"");
		for (String name : files)
			buffer.append(" \"").append(name).append("\"");

		runNegativeTest(new String[]{},
			buffer.toString(),
			"",
			"----------\n" +
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/module-info.java (at line 4)\n" +
			"	requires mod.one;\n" +
			"	         ^^^^^^^\n" +
			"mod.one cannot be resolved to a module\n" +
			"----------\n" +
			"----------\n" +
			"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/A.java (at line 3)\n" +
			"	p.TestProcessor prc = null;\n" +
			"	^\n" +
			"p cannot be resolved to a type\n" +
			"----------\n" +
			"2 problems (2 errors)\n",
			false);
	}
	/*
	 * Test that reference to a binary package that is exported in a module
	 * but doesn't have a corresponding resource or .class files is reported.
	 */
	public void testBug522472a() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		File srcDir = new File(directory);
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"module mod.one { \n" +
				"	exports x.y.z;\n" +
				"	exports a.b.c;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "x" + File.separator + "y" + File.separator + "z",
				"X.java",
				"package x.y.z;\n");
		writeFileCollecting(files, moduleLoc + File.separator + "a" + File.separator + "b" + File.separator + "c",
				"A.java",
				"package a.b.c;\n" +
				"public class A {}");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);

		Util.flushDirectoryContent(srcDir);
		files.clear();
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"module mod.two { \n" +
					"	requires mod.one;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "r",
				"Main.java",
				"package p.q.r;\n" +
				"import a.b.c.*;\n" +
				"import x.y.z.*;\n" +
				"public class Main {"
				+ "}");
		buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ");
		runConformModuleTest(files,
				buffer,
				"",
				"----------\n"
				+ "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/p/q/r/Main.java (at line 2)\n"
				+ "	import a.b.c.*;\n"
				+ "	       ^^^^^\n"
				+ "The import a.b.c is never used\n"
				+ "----------\n"
				+ "2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/p/q/r/Main.java (at line 3)\n"
				+ "	import x.y.z.*;\n"
				+ "	       ^^^^^\n"
				+ "The import x.y.z is never used\n"
				+ "----------\n"
				+ "2 problems (2 warnings)\n",
				false,
				"package conflict");
	}
	/*
	 * Same as above test case, but two binary modules export the package, without any .class files
	 */
	public void testBug522472b() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		File srcDir = new File(directory);
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"module mod.one { \n" +
				"	exports x.y.z;\n" +
				"	exports a.b.c;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "x" + File.separator + "y" + File.separator + "z",
				"X.java",
				"package x.y.z;\n");
		writeFileCollecting(files, moduleLoc + File.separator + "a" + File.separator + "b" + File.separator + "c",
				"A.java",
				"package a.b.c;\n" +
				"public class A {}");

		moduleLoc = directory + File.separator + "mod.one.a";
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"module mod.one.a { \n" +
				"	exports x.y.z;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "x" + File.separator + "y" + File.separator + "z",
				"X.java",
				"package x.y.z;\n");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);

		Util.flushDirectoryContent(srcDir);
		files.clear();
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"module mod.two { \n" +
					"	requires mod.one;\n" +
					"	requires mod.one.a;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "r",
				"Main.java",
				"package p.q.r;\n" +
				"import a.b.c.*;\n" +
				"import x.y.z.*;\n" +
				"public class Main {"
				+ "}");
		buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ");
		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/module-info.java (at line 2)\n" +
				"	requires mod.one;\n" +
				"	^^^^^^^^^^^^^^^^\n" +
				"The package x.y.z is accessible from more than one module: mod.one, mod.one.a\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/module-info.java (at line 3)\n" +
				"	requires mod.one.a;\n" +
				"	^^^^^^^^^^^^^^^^^^\n" +
				"The package x.y.z is accessible from more than one module: mod.one, mod.one.a\n" +
				"----------\n" +
				"----------\n" +
				"3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/p/q/r/Main.java (at line 3)\n" +
				"	import x.y.z.*;\n" +
				"	       ^^^^^\n" +
				"The import x.y.z cannot be resolved\n" +
				"----------\n" +
				"3 problems (3 errors)\n",
				false,
				"package conflict");
	}
	public void testBug522472d() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"module mod.one { \n" +
				"	exports x.y.z;\n" +
				"	exports a.b.c;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "x" + File.separator + "y" + File.separator + "z",
				"X.java",
				"package x.y.z;\n");
		writeFileCollecting(files, moduleLoc + File.separator + "a" + File.separator + "b" + File.separator + "c",
				"A.java",
				"package a.b.c;\n" +
				"public class A {}");

		moduleLoc = directory + File.separator + "mod.one.a";
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"module mod.one.a { \n" +
				"	exports x.y.z;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "x" + File.separator + "y" + File.separator + "z",
				"X.java",
				"package x.y.z;\n");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ");

		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"module mod.two { \n" +
					"	requires mod.one;\n" +
					"	requires mod.one.a;\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "r",
				"Main.java",
				"package p.q.r;\n" +
				"import a.b.c.*;\n" +
				"import x.y.z.*;\n" +
				"public class Main {"
				+ "}");

		runNegativeModuleTest(files,
				buffer,
				"",
				"----------\n" +
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/module-info.java (at line 2)\n" +
				"	requires mod.one;\n" +
				"	^^^^^^^^^^^^^^^^\n" +
				"The package x.y.z is accessible from more than one module: mod.one, mod.one.a\n" +
				"----------\n" +
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/module-info.java (at line 3)\n" +
				"	requires mod.one.a;\n" +
				"	^^^^^^^^^^^^^^^^^^\n" +
				"The package x.y.z is accessible from more than one module: mod.one, mod.one.a\n" +
				"----------\n" +
				"----------\n" +
				"3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/p/q/r/Main.java (at line 3)\n" +
				"	import x.y.z.*;\n" +
				"	       ^^^^^\n" +
				"The package x.y.z is accessible from more than one module: mod.one, mod.one.a\n" +
				"----------\n" +
				"3 problems (3 errors)\n",
				false,
				"conflict");
	}
	public void testIssue2357_001() throws Exception {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
								"    exports pack1;\n" +
								"    exports pack2 to second;\n" +
								"    opens pack3;\n" +
								"    opens pack4 to third;\n" +
								"    uses pack5.X51;\n" +
								"    provides pack1.I11 with pack1.X11;\n" +
								"    requires static java.sql;\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pack1", "I11.java",
				"package pack1;\n" +
						"public interface I11 {\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pack1", "X11.java",
						"package pack1;\n" +
						"public class X11 implements I11{\n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pack2", "X21.java",
				"package pack2;\n" +
				"public class X21 {\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pack3", "X31.java",
				"package pack3;\n" +
				"public class X31 {\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pack4", "X41.java",
				"package pack4;\n" +
				"public class X41 {\n" +
				"}");
		writeFileCollecting(files, moduleLoc + File.separator + "pack5", "X51.java",
				"package pack5;\n" +
				"public class X51 {\n" +
				"}");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ");
		files.forEach(name -> buffer.append(" \"" + name + "\""));
		runConformTest(new String[0],
				buffer.toString(),
				"",
				"",
				false);
		String expectedOutput = "// Compiled from module-info.java (version 9 : 53.0, no super bit)\n" +
				" module mod.one  {\n" +
				"  // Version: \n" +
				"\n" +
				"  requires static java.sql;\n" +
				"  requires java.base;\n" +
				"\n" +
				"  exports pack1;\n" +
				"  exports pack2 to second;\n" +
				"\n" +
				"  opens pack3;\n" +
				"  opens pack4 to third;\n" +
				"\n" +
				"  uses pack5.X51\n" +
				"\n" +
				"  provides pack1.I11 with pack1.X11;\n" +
				"\n" +
				"}";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + out + File.separator + "module-info.class", "module-info", expectedOutput);
	}
}
