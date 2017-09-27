/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

public class ModuleCompilationTests extends AbstractBatchCompilerTest {

	static {
//		 TESTS_NAMES = new String[] { "testBug522164" };
		// TESTS_NUMBERS = new int[] { 1 };
		// TESTS_RANGE = new int[] { 298, -1 };
	}

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
	
	void runConformModuleTest(List<String> testFileNames, StringBuffer commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory)
	{
		runConformModuleTest(testFileNames, commandLine,
				expectedFailureOutOutputString, expectedFailureErrOutputString, shouldFlushOutputDirectory, OUTPUT_DIR);
	}

	void runConformModuleTest(List<String> testFileNames, StringBuffer commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory, String output)
	{
		for (String file : testFileNames)
			commandLine.append(" \"").append(file).append("\"");
		runConformModuleTest(new String[0], commandLine.toString(),
				expectedFailureOutOutputString, expectedFailureErrOutputString, shouldFlushOutputDirectory, output);
	}

	Set<String> runConformModuleTest(String[] testFiles, String commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory)
	{
		return runConformModuleTest(testFiles, commandLine, expectedFailureErrOutputString, expectedFailureErrOutputString, shouldFlushOutputDirectory, OUTPUT_DIR);
	}

	Set<String> runConformModuleTest(String[] testFiles, String commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory, String output)
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
			for (Object comp : javacCompilers) {
				JavacCompiler javacCompiler = (JavacCompiler) comp;
				if (javacCompiler.compliance < ClassFileConstants.JDK9)
					continue;
				commandLine = adjustForJavac(commandLine);
				StringBuffer log = new StringBuffer();
				try {
					long compileResult = javacCompiler.compile(
											outputDir, /* directory */
											commandLine /* options */,
											testFileNames /* source file names */,
											log);
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

	void runNegativeModuleTest(List<String> testFileNames, StringBuffer commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory, String javacErrorMatch) {
		runNegativeModuleTest(testFileNames, commandLine, expectedFailureOutOutputString,
				expectedFailureErrOutputString, shouldFlushOutputDirectory, javacErrorMatch, OUTPUT_DIR);
	}

	void runNegativeModuleTest(List<String> testFileNames, StringBuffer commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory, String javacErrorMatch, String output)
	{
		for (String file : testFileNames)
			commandLine.append(" \"").append(file).append("\"");
		runNegativeModuleTest(new String[0], commandLine.toString(),
				expectedFailureOutOutputString, expectedFailureErrOutputString, shouldFlushOutputDirectory, javacErrorMatch, output);
	}
	void runNegativeModuleTest(String[] testFiles, String commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory, String javacErrorMatch) {
		runNegativeModuleTest(testFiles, commandLine, expectedFailureOutOutputString, expectedFailureErrOutputString,
				shouldFlushOutputDirectory, javacErrorMatch, OUTPUT_DIR);
	}

	void runNegativeModuleTest(String[] testFiles, String commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory, String javacErrorMatch, String output)
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
			for (Object comp : javacCompilers) {
				JavacCompiler javacCompiler = (JavacCompiler) comp;
				if (javacCompiler.compliance < ClassFileConstants.JDK9)
					continue;
				commandLine = adjustForJavac(commandLine);
				StringBuffer log = new StringBuffer();
				try {
					long compileResult = javacCompiler.compile(
											outputDir, /* directory */
											commandLine /* options */,
											testFileNames /* source file names */,
											log);
					if (compileResult == 0) {
						System.err.println("Previous error was from "+testName());
						fail(testName()+": Unexpected success from javac");
					}
					if (!log.toString().contains(javacErrorMatch)) {
						System.err.println(testName()+": Error match " + javacErrorMatch + " not found in \n"+log.toString());
						fail("Expected error match not found: "+javacErrorMatch);
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
		}
	}

	String adjustForJavac(String commandLine) {
		String[] tokens = commandLine.split(" ");
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].trim().equals("-9")) {
				buf.append(" -source 9 ");
				continue;
			}
			if (tokens[i].startsWith("-warn") || tokens[i].startsWith("-err") || tokens[i].startsWith("-info")) {
				if (tokens[i].contains("exports") && !tokens[i].contains("-exports"))
					buf.append(" -Xlint:exports ");
				continue;
			}
			buf.append(tokens[i]).append(' ');
		}
		return buf.toString();
	}
	
	private void walkOutFiles(final String outputLocation, final Set<String> fileNames, boolean add) {
		if (!(new File(outputLocation)).exists()) 
			return;
		try {
			Files.walkFileTree(FileSystems.getDefault().getPath(outputLocation), new SimpleFileVisitor<Path>() {
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
    		"	^^^^^^^^\n" + 
    		"java.sql cannot be resolved to a type\n" + 
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

		StringBuffer buffer = new StringBuffer();
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
		
		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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
				"   java.lang.SecurityManager man = null;\n" +
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
			buffer.append("-d " + outDir )
			.append(" -9")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(modDir + File.separator + "mod.one.jar").append(File.pathSeparator)
			.append(modDir + File.separator + "mod.two").append(File.pathSeparator)
			.append("\" ")
			.append(" --module-source-path " + "\"" + srcDir + "\"");
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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
				"	                          ^\n" +
				"p cannot be resolved\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"cannot be resolved",
				OUTPUT_DIR + File.separator + out);
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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


		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -sourcepath " + "\"" + moduleLoc + "\" ")
			.append(" --add-exports java.base/com.sun.security.ntlm=ALL-UNNAMED ");

		runConformModuleTest(files, 
				buffer,
				"",
				"",
				false);
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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
				 OUTPUT_DIR + File.separator + out);
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

		StringBuffer buffer = new StringBuffer();
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
				"does not export it to the unnamed module");
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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
		String javaHome = System.getProperty("java.home");
		StringBuffer buffer = new StringBuffer();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" --system \"").append(javaHome).append("\"")
			.append(" \"" + moduleLoc +  File.separator + "module-info.java\" ");

		runConformModuleTest(new String[0], 
				buffer.toString(),
				"",
				"",
				false);
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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
				"	                       ^\n" + 
				"a cannot be resolved to a type\n" + 
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

		StringBuffer buffer = new StringBuffer();
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
				"	                       ^\n" + 
				"a cannot be resolved to a type\n" + 
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

		StringBuffer buffer = new StringBuffer();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString()).append("\" ")
			.append("-p \"")
			.append(LIB_DIR).append("\" ")
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/test/Test.java (at line 3)\n" +
				"	public class Test implements org.eclipse.SomeInterface {\n" +
				"	                             ^^^^^^^^^^^\n" +
				"org.eclipse cannot be resolved to a type\n" +
				"----------\n" +
				"1 problem (1 error)\n",
				false,
				"not in a module");
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
		buffer.append("-d " + outDir )
		.append(" -9 ")
		.append(" -p \"")
		.append(Util.getJavaClassLibsAsString())
		.append(modDir.getAbsolutePath())
		.append("\" ")
		.append("-classNames mod.one/p.XYZ")
		.append(" --module-source-path " + "\"" + srcDir + "\"");

		runNegativeModuleTest(files,
				buffer,
				"",
				"invalid class name: mod.one/p.XYZ\n",
				false,
				"", // not expected pass with Javac
				outDir);
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

		StringBuffer buffer = new StringBuffer();
		buffer.append("-d " + outDir )
		.append(" -9 ")
		.append(" -p \"")
		.append(Util.getJavaClassLibsAsString())
		.append(modDir.getAbsolutePath())
		.append("\" ")
		.append("-classNames mod.xyz/p.X")
		.append(" --module-source-path " + "\"" + srcDir + "\"");

		runNegativeModuleTest(files,
				buffer,
				"",
				"invalid module name: mod.xyz\n",
				false,
				"", // not expected pass with Javac
				outDir);
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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
			"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/X.java (at line 1)\n" + 
			"	public class X {\n" + 
			"	^\n" + 
			"Must declare a named package because this compilation unit is associated to the named module \'mod.one\'\n" + 
			"----------\n" + 
			"1 problem (1 warning)\n",
			false,
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

		StringBuffer buffer = new StringBuffer();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + jarPath + "\"");

		runConformModuleTest(files, 
			buffer,
			"",
			"",
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

		StringBuffer buffer = new StringBuffer();
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
				outDir);
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

		StringBuffer buffer = new StringBuffer();
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

		StringBuffer buffer = new StringBuffer();
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
			"----------\n" + 
			"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p1/X.java\n" + 
			"Must declare a named package because this compilation unit is associated to the named module \'mod.one\'\n" + 
			"----------\n" + 
			"2 problems (1 error, 1 warning)\n",
			false,
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

		StringBuffer buffer = new StringBuffer();
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
		StringBuffer buffer = new StringBuffer();
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
		StringBuffer buffer = new StringBuffer();
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
			OUTPUT_DIR + File.separator + out);
	}
}
