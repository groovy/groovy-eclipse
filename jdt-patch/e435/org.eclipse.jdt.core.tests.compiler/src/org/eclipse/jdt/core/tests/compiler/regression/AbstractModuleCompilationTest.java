/*******************************************************************************
 * Copyright (c) 2024 GK Software SE, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
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
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public abstract class AbstractModuleCompilationTest extends AbstractBatchCompilerTest {

	// use -source rather than --release but suppress: warning: [options] bootstrap class path not set in conjunction with -source 9:
	protected static final String JAVAC_SOURCE_9_OPTIONS = "-source 9 -Xlint:-options";

	public AbstractModuleCompilationTest(String name) {
		super(name);
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
			return AbstractModuleCompilationTest.this.runConformModuleTest(this.testFiles, commandLineString,
					this.expectedOutputString, this.expectedErrorString,
					this.shouldFlushOutputDirectory, this.outputDir,
					this.javacTestOptions, javacCommandLine);
		}
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

	protected void runConformModuleTest(List<String> testFileNames, StringBuilder commandLine, String expectedFailureOutOutputString, String expectedFailureErrOutputString) {
		runConformModuleTest(testFileNames, commandLine,
				expectedFailureOutOutputString, expectedFailureErrOutputString, OUTPUT_DIR + File.separator + "javac");
	}

	protected void runConformModuleTest(List<String> testFileNames, StringBuilder commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			String output) {
		runConformModuleTest(testFileNames, commandLine,
				expectedFailureOutOutputString, expectedFailureErrOutputString, output,
				JavacTestOptions.DEFAULT);
	}
	protected void runConformModuleTest(List<String> testFileNames, StringBuilder commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			String output, JavacTestOptions javacTestOptions) {
		for (String file : testFileNames)
			commandLine.append(" \"").append(file).append("\"");
		runConformModuleTest(new String[0], commandLine.toString(),
				expectedFailureOutOutputString, expectedFailureErrOutputString, false,
				output, javacTestOptions, null);
	}

	protected Set<String> runConformModuleTest(String[] testFiles, String commandLine, String expectedFailureOutOutputString, String expectedFailureErrOutputString, boolean shouldFlushOutputDirectory) {
		return runConformModuleTest(testFiles, commandLine, expectedFailureErrOutputString, expectedFailureErrOutputString,
				shouldFlushOutputDirectory, OUTPUT_DIR, JavacTestOptions.DEFAULT, null);
	}

	protected Set<String> runConformModuleTest(String[] testFiles, String commandLine, String expectedFailureOutOutputString, String expectedFailureErrOutputString, boolean shouldFlushOutputDirectory, String output,
			JavacTestOptions options, String javacCommandLine) {
				runConformTest(testFiles, commandLine, expectedFailureOutOutputString, expectedFailureErrOutputString, shouldFlushOutputDirectory);
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
							log = trimJavacLog(log);
							if (compileResult != 0 && !log.isEmpty()) {
								JavacTestOptions.Excuse excuse = options.excuseFor(javacCompiler);
								boolean hasError = log.toString().lines().anyMatch(l -> l.contains("error:"));
								int mismatch = hasError ? JavacTestOptions.MismatchType.JavacErrorsEclipseNone : JavacTestOptions.MismatchType.JavacWarningsEclipseNone;
								handleMismatch(javacCompiler, testName(), testFiles,
										"", expectedFailureOutOutputString, expectedFailureErrOutputString,
										log, "", "",
										excuse, mismatch);
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

	/** hook to filter uninteresting errors/warnings */
	protected StringBuilder trimJavacLog(StringBuilder log) {
		return log;
	}

	protected void runNegativeModuleTest(List<String> testFileNames, StringBuilder commandLine, String expectedFailureOutOutputString, String expectedFailureErrOutputString, String javacErrorMatch) {
				runNegativeModuleTest(testFileNames, commandLine, expectedFailureOutOutputString,
						expectedFailureErrOutputString, javacErrorMatch, OUTPUT_DIR + File.separator + "javac");
			}

	protected void runNegativeModuleTest(List<String> testFileNames, StringBuilder commandLine, String expectedFailureOutOutputString, String expectedFailureErrOutputString, String javacErrorMatch,
			String output) {
				runNegativeModuleTest(testFileNames, commandLine, expectedFailureOutOutputString, expectedFailureErrOutputString,
						javacErrorMatch, output, JavacTestOptions.DEFAULT);
			}

	protected void runNegativeModuleTest(List<String> testFileNames, StringBuilder commandLine, String expectedFailureOutOutputString, String expectedFailureErrOutputString, String javacErrorMatch,
			String output, JavacTestOptions options) {
				for (String file : testFileNames)
					commandLine.append(" \"").append(file).append("\"");
				runNegativeModuleTest(new String[0], commandLine.toString(),
						expectedFailureOutOutputString, expectedFailureErrOutputString, false, javacErrorMatch, output,
						options);
			}

	protected void runNegativeModuleTest(String[] testFiles, String commandLine, String expectedFailureOutOutputString, String expectedFailureErrOutputString, boolean shouldFlushOutputDirectory,
			String javacErrorMatch) {
				runNegativeModuleTest(testFiles, commandLine, expectedFailureOutOutputString, expectedFailureErrOutputString,
						shouldFlushOutputDirectory, javacErrorMatch, OUTPUT_DIR, JavacTestOptions.DEFAULT);
			}

	void runNegativeModuleTest(String[] testFiles, String commandLine, String expectedFailureOutOutputString, String expectedFailureErrOutputString, boolean shouldFlushOutputDirectory, String javacErrorMatch,
			String output, JavacTestOptions options) {
				runNegativeTest(testFiles, commandLine, expectedFailureOutOutputString, expectedFailureErrOutputString, shouldFlushOutputDirectory);
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
	protected String adjustForJavac(String commandLine, String versionOptions) {
		String[] tokens = commandLine.split(" ");
		StringBuilder buf = new StringBuilder();
		boolean skipNext = false;
		for (int i = 0; i < tokens.length; i++) {
			if (skipNext) {
				skipNext = false;
				continue;
			}
			switch (tokens[i].trim()) {
			case "-9":
				if (versionOptions == null)
					buf.append(' ').append(" --release 9 ");
				continue;
			case "-8":
				if (versionOptions == null)
					buf.append(' ').append(" --release 8 ");
				continue;
			case "-22":
				if (versionOptions == null)
					buf.append(' ').append(" --release 22 ");
				continue;
			case "-23":
				if (versionOptions == null)
					buf.append(' ').append(" --release 23 ");
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

	protected void assertClassFile(String msg, String fileName, Set<String> classFiles) {
		if (classFiles != null) {
			assertTrue(msg, classFiles.contains(fileName));
		} else {
			assertTrue(msg, (new File(fileName).exists()));
		}
	}

}
