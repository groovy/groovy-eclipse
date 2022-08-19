/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for
 *								bug 335093 - [compiler][null] minimal hook for future null annotation support
 *								bug 388800 - [1.8] adjust tests to 1.8 JRE
 *								bug 402237 - [1.8][compiler] investigate differences between compilers re MethodVerifyTest
 *								bug 391376 - [1.8] check interaction of default methods with bridge methods and generics
 *								Bug 412203 - [compiler] Internal compiler error: java.lang.IllegalArgumentException: info cannot be null
 *								Bug 422051 - [1.8][compiler][tests] cleanup excuses (JavacHasABug) in InterfaceMethodTests
 *								Bug 400874 - [1.8][compiler] Inference infrastructure should evolve to meet JLS8 18.x (Part G of JSR335 spec)
 *								Bug 425721 - [1.8][compiler] Nondeterministic results in GenericsRegressionTest_1_8.testBug424195a
 *     Jesper S Moller - Contributions for bug 378674 - "The method can be declared as static" is wrong
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
import org.eclipse.jdt.core.search.SearchDocument;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.tests.junit.extension.StopableTestCase;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.CompilerTestSetup;
import org.eclipse.jdt.core.tests.util.TestVerifier;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.IrritantSet;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.search.JavaSearchParticipant;
import org.eclipse.jdt.internal.core.search.indexing.BinaryIndexer;
import org.eclipse.jdt.internal.core.util.Messages;
import org.osgi.framework.Bundle;

@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class AbstractRegressionTest extends AbstractCompilerTest implements StopableTestCase {

	static final String[] env = System.getenv().entrySet().stream()
		.filter(e -> !"JAVA_TOOL_OPTIONS".equals(e.getKey()) && !"_JAVA_OPTIONS".equals(e.getKey()))
		.map(e -> e.getKey() + "=" + e.getValue())
		.toArray(String[]::new);

	protected class Runner {
		boolean shouldFlushOutputDirectory = true;
		// input:
		String[] testFiles;
		String[] dependantFiles;
		String[] classLibraries;
		boolean  libsOnModulePath;
		// control compilation:
		Map<String,String> customOptions;
		boolean performStatementsRecovery;
		boolean generateOutput;
		ICompilerRequestor customRequestor;
		// compiler result:
		String expectedCompilerLog;
		String[] alternateCompilerLogs;
		boolean showCategory;
		boolean showWarningToken;
		// javac:
		boolean skipJavac;
		public String expectedJavacOutputString;
		JavacTestOptions javacTestOptions;
		// execution:
		boolean forceExecution;
		String[] vmArguments;
		String expectedOutputString;
		String expectedErrorString;

		ASTVisitor visitor;

		protected void runConformTest() {
			runTest(this.shouldFlushOutputDirectory,
					this.testFiles,
					this.dependantFiles != null ? this.dependantFiles : new String[] {},
					this.classLibraries,
					this.libsOnModulePath,
					this.customOptions,
					this.performStatementsRecovery,
					new Requestor(
							this.generateOutput,
							this.customRequestor,
							this.showCategory,
							this.showWarningToken),
					false,
					this.expectedCompilerLog,
					this.alternateCompilerLogs,
					this.forceExecution,
					this.vmArguments,
					this.expectedOutputString,
					this.expectedErrorString,
					this.visitor,
					this.expectedJavacOutputString != null ? this.expectedJavacOutputString : this.expectedOutputString,
					this.skipJavac ? JavacTestOptions.SKIP : this.javacTestOptions);
		}

		protected void runNegativeTest() {
			runTest(this.shouldFlushOutputDirectory,
					this.testFiles,
					this.dependantFiles != null ? this.dependantFiles : new String[] {},
					this.classLibraries,
					this.libsOnModulePath,
					this.customOptions,
					this.performStatementsRecovery,
					new Requestor(
							this.generateOutput,
							this.customRequestor,
							this.showCategory,
							this.showWarningToken),
					true,
					this.expectedCompilerLog,
					this.alternateCompilerLogs,
					this.forceExecution,
					this.vmArguments,
					this.expectedOutputString,
					this.expectedErrorString,
					this.visitor,
					this.expectedJavacOutputString != null ? this.expectedJavacOutputString : this.expectedOutputString,
					this.skipJavac ? JavacTestOptions.SKIP : this.javacTestOptions);
		}

		protected void runWarningTest() {
			runTest(this.shouldFlushOutputDirectory,
					this.testFiles,
					this.dependantFiles != null ? this.dependantFiles : new String[] {},
					this.classLibraries,
					this.libsOnModulePath,
					this.customOptions,
					this.performStatementsRecovery,
					new Requestor(
							this.generateOutput,
							this.customRequestor,
							this.showCategory,
							this.showWarningToken),
					false,
					this.expectedCompilerLog,
					this.alternateCompilerLogs,
					this.forceExecution,
					this.vmArguments,
					this.expectedOutputString,
					this.expectedErrorString,
					this.visitor,
					this.expectedJavacOutputString != null ? this.expectedJavacOutputString : this.expectedOutputString,
					this.skipJavac ? JavacTestOptions.SKIP : this.javacTestOptions);
		}
	}

	// javac comparison related types, fields and methods - see runJavac for
	// details
static class JavacCompiler {
	String rootDirectoryPath;
	String javacPathName;
	String version; // not intended to be modified - one of JavaCore.VERSION_1_*
	int minor;
	String rawVersion; // not intended to be modified - more complete version name
	long compliance;
	public static final long EXIT_VALUE_MASK = 0x00000000FFFFFFFFL;
	public static final long ERROR_LOG_MASK =  0xFFFFFFFF00000000L;
	private String classpath;
	JavacCompiler(String rootDirectoryPath) throws IOException, InterruptedException {
		this(rootDirectoryPath, null);
	}
	JavacCompiler(String rootDirectoryPath, String rawVersion) throws IOException, InterruptedException {
		this.rootDirectoryPath = rootDirectoryPath;
		this.javacPathName = new File(rootDirectoryPath + File.separator
				+ "bin" + File.separator + JAVAC_NAME).getCanonicalPath();
		// WORK don't need JAVAC_NAME any more; suppress this as we work towards code cleanup
		if (rawVersion == null) {
			rawVersion = getVersion(this.javacPathName);
		}
		this.version = versionFromRawVersion(rawVersion, this.javacPathName);
		this.compliance = CompilerOptions.versionToJdkLevel(this.version);
		this.minor = minorFromRawVersion(this.version, rawVersion);
		this.rawVersion = rawVersion;
		StringBuilder classpathBuffer = new StringBuilder(" -classpath ");
		this.classpath = classpathBuffer.toString();
	}
	/** Call this if " -classpath " should be replaced by some other option token. */
	protected void usePathOption(String option) {
		this.classpath = option;
	}
	static String getVersion(String javacPathName) throws IOException, InterruptedException {
		Process fetchVersionProcess = null;
		try {
			fetchVersionProcess = Runtime.getRuntime().exec(javacPathName + " -version", env, null);
		    Logger versionStdErrLogger = new Logger(fetchVersionProcess.getErrorStream(), ""); // for javac <= 1.8
		    Logger versionStdOutLogger = new Logger(fetchVersionProcess.getInputStream(), ""); // for javac >= 9
		    versionStdErrLogger.start();
		    versionStdOutLogger.start();
			fetchVersionProcess.waitFor();
			// make sure we get the whole output
			versionStdErrLogger.join();
			versionStdOutLogger.join();
			String loggedVersion = versionStdErrLogger.buffer.toString();
			if (loggedVersion.isEmpty())
				loggedVersion = versionStdOutLogger.buffer.toString();
			int eol = loggedVersion.indexOf('\n');
			if (eol != -1) {
				loggedVersion = loggedVersion.substring(0, eol);
			}
			if (loggedVersion.startsWith("javac ")) {
				loggedVersion = loggedVersion.substring(6, loggedVersion.length());
			}
			return loggedVersion;
		} finally {
			if (fetchVersionProcess != null) {
				fetchVersionProcess.destroy(); // closes process streams
			}
		}
	}
	static String versionFromRawVersion(String rawVersion, String javacPathName) {
		if (rawVersion.indexOf("1.4") != -1 ||
				(javacPathName != null &&
				javacPathName.indexOf("1.4") != -1)
				/* in fact, SUN javac 1.4 does not support the -version option;
				 * this is a imperfect heuristic to catch the case */) {
			return JavaCore.VERSION_1_4;
		} else if (rawVersion.indexOf("1.5") != -1) {
			return JavaCore.VERSION_1_5;
		} else if (rawVersion.indexOf("1.6") != -1) {
			return JavaCore.VERSION_1_6;
		} else if (rawVersion.indexOf("1.7") != -1) {
			return JavaCore.VERSION_1_7;
		} else if (rawVersion.indexOf("1.8") != -1) {
			return JavaCore.VERSION_1_8;
		} else if(rawVersion.startsWith("9")) {
			return JavaCore.VERSION_9;
		} else if(rawVersion.startsWith("10")) {
			return JavaCore.VERSION_10;
		} else if(rawVersion.startsWith("11")) {
			return JavaCore.VERSION_11;
		} else if(rawVersion.startsWith("12")) {
			return JavaCore.VERSION_12;
		} else if(rawVersion.startsWith("13")) {
			return JavaCore.VERSION_13;
		} else if(rawVersion.startsWith("14")) {
			return JavaCore.VERSION_14;
		} else if(rawVersion.startsWith("15")) {
			return JavaCore.VERSION_15;
		} else if(rawVersion.startsWith("16")) {
			return JavaCore.VERSION_16;
		} else if(rawVersion.startsWith("17")) {
			return JavaCore.VERSION_17;
		} else if(rawVersion.startsWith("18")) {
			return JavaCore.VERSION_18;
		} else {
			throw new RuntimeException("unknown javac version: " + rawVersion);
		}
	}
	// projects known raw versions to minors; minors should grow with time, so
	// that before and after relationships be easy to implement upon compilers
	// of the same version; two latest digits are used for variants into levels
	// denoted by the two first digits
	static int minorFromRawVersion (String version, String rawVersion) {
		if (version == JavaCore.VERSION_1_5) {
			if ("1.5.0_15-ea".equals(rawVersion)) {
				return 1500;
			}
			if ("1.5.0_16-ea".equals(rawVersion)) { // b01
				return 1600;
			}
		}
		if (version == JavaCore.VERSION_1_6) {
			if ("1.6.0_10-ea".equals(rawVersion)) {
				return 1000;
			}
			if ("1.6.0_10-beta".equals(rawVersion)) { // b24
				return 1010;
			}
			if ("1.6.0_45".equals(rawVersion)) {
				return 1045;
			}
		}
		if (version == JavaCore.VERSION_1_7) {
			if ("1.7.0-ea".equals(rawVersion)) {
				return 0000;
			}
			if ("1.7.0_10".equals(rawVersion)) {
				return 1000;
			}
			if ("1.7.0_25".equals(rawVersion)) {
				return 2500;
			}
			if ("1.7.0_80".equals(rawVersion)) {
				return 8000;
			}
		}
		if (version == JavaCore.VERSION_1_8) {
			if ("1.8.0-ea".equals(rawVersion) || ("1.8.0".equals(rawVersion))) {
				return 0000;
			}
			if ("1.8.0_40".equals(rawVersion)) {
				return 1000; // corresponds to JLS maintenance release 2015-02-13
			}
			if ("1.8.0_45".equals(rawVersion)) {
				return 1100; // corresponds to JLS maintenance release 2015-02-13
			}
			if ("1.8.0_60".equals(rawVersion)) {
				return 1500;
			}
			if ("1.8.0_131".equals(rawVersion)) {
				return 1700;
			}
			if ("1.8.0_152".equals(rawVersion)) {
				return 1900;
			}
			if ("1.8.0_162".equals(rawVersion)) {
				return 2100;
			}
			if ("1.8.0_171".equals(rawVersion)) {
				return 2200;
			}
			if ("1.8.0_172".equals(rawVersion)) {
				return 2300;
			}
			if ("1.8.0_181".equals(rawVersion)) {
				return 2400;
			}
			if ("1.8.0_182".equals(rawVersion)) {
				return 2500;
			}
			if ("1.8.0_202".equals(rawVersion)) {
				return 2600;
			}
			if ("1.8.0_212".equals(rawVersion)) {
				return 2700;
			}
		}
		if (version == JavaCore.VERSION_9) {
			if ("9".equals(rawVersion)) {
				return 0000;
			}
			if ("9.0.1".equals(rawVersion)) {
				return 0100;
			}
			if ("9.0.4".equals(rawVersion)) {
				return 0400;
			}
		}
		if (version == JavaCore.VERSION_10) {
			if ("10".equals(rawVersion)) {
				return 0000;
			}
			if ("10.0.1".equals(rawVersion)) {
				return 0100;
			}
			if ("10.0.2".equals(rawVersion)) {
				return 0200;
			}
		}
		if (version == JavaCore.VERSION_11) {
			if ("11".equals(rawVersion)) {
				return 0000;
			}
			if ("11.0.1".equals(rawVersion)) {
				return 0100;
			}
			if ("11.0.2".equals(rawVersion)) {
				return 0200;
			}
		}
		if (version == JavaCore.VERSION_12) {
			if ("12".equals(rawVersion)) {
				return 0000;
			}
			if ("12.0.1".equals(rawVersion)) {
				return 0100;
			}
			if ("12.0.2".equals(rawVersion)) {
				return 0200;
			}
		}
		if (version == JavaCore.VERSION_13) {
			if ("13-ea".equals(rawVersion)) {
				return 0000;
			}
			if ("13".equals(rawVersion)) {
				return 0000;
			}
			if ("13.0.1".equals(rawVersion)) {
				return 0100;
			}
			if ("13.0.2".equals(rawVersion)) {
				return 0200;
			}
		}
		if (version == JavaCore.VERSION_14) {
			if ("14-ea".equals(rawVersion)) {
				return 0000;
			}
			if ("14".equals(rawVersion)) {
				return 0000;
			}
			if ("14.0.1".equals(rawVersion)) {
				return 0100;
			}
			if ("14.0.2".equals(rawVersion)) {
				return 0200;
			}
		}
		if (version == JavaCore.VERSION_15) {
			if ("15-ea".equals(rawVersion)) {
				return 0000;
			}
			if ("15".equals(rawVersion)) {
				return 0000;
			}
			if ("15.0.1".equals(rawVersion)) {
				return 0100;
			}
			if ("15.0.2".equals(rawVersion)) {
				return 0200;
			}
		}
		if (version == JavaCore.VERSION_16) {
			if ("16-ea".equals(rawVersion)) {
				return 0000;
			}
			if ("16".equals(rawVersion)) {
				return 0000;
			}
			if ("16.0.1".equals(rawVersion)) {
				return 0100;
			}
			if ("16.0.2".equals(rawVersion)) {
				return 0200;
			}
		}
		if (version == JavaCore.VERSION_17) {
			if ("17-ea".equals(rawVersion)) {
				return 0000;
			}
			if ("17".equals(rawVersion)) {
				return 0000;
			}
			if ("17.0.1".equals(rawVersion)) {
				return 0100;
			}
			if ("17.0.2".equals(rawVersion)) {
				return 0200;
			}
		}
		if (version == JavaCore.VERSION_18) {
			if ("18-ea".equals(rawVersion)) {
				return 0000;
			}
			if ("18".equals(rawVersion)) {
				return 0000;
			}
			if ("18.0.1".equals(rawVersion)) {
				return 0100;
			}
			if ("18.0.2".equals(rawVersion)) {
				return 0200;
			}
		}
		throw new RuntimeException("unknown raw javac version: " + rawVersion);
	}
	// returns 0L if everything went fine; else the lower word contains the
	// exit value and the upper word is non-zero iff the error log has contents
	long compile(File directory, String options, String[] sourceFileNames, StringBuffer log) throws IOException, InterruptedException {
		return compile(directory, options, sourceFileNames, log, true);
	}
	long compile(File directory, String options, String[] sourceFileNames, StringBuffer log, boolean extendCommandLine) throws IOException, InterruptedException {
		Process compileProcess = null;
		long result = 0L;
		// WORK classpath should depend on the compiler, not on the default runtime
		try {
			if (!directory.exists()) {
				directory.mkdir();
			}
			StringBuilder cmdLine = new StringBuilder(this.javacPathName);
			cmdLine.append(this.classpath);
			cmdLine.append(". ");
			cmdLine.append(options);
			if (extendCommandLine) {
				for (int i = 0; i < sourceFileNames.length; i ++) {
					cmdLine.append(' ');
					cmdLine.append(sourceFileNames[i]);
				}
			}
			String cmdLineAsString;
			// WORK improve double-quotes management on Linux
			if ("Linux".equals(System.getProperty("os.name"))) {
				cmdLineAsString = cmdLine.toString().replaceAll("\"", "");
			} else {
				cmdLineAsString = cmdLine.toString();
			}
			compileProcess = Runtime.getRuntime().exec(cmdLineAsString, env, directory);
			Logger errorLogger = new Logger(compileProcess.getErrorStream(),
					"ERROR", log == null ? new StringBuffer() : log);
			errorLogger.start();
			int compilerResult = compileProcess.waitFor();
			result |= compilerResult; // caveat: may never terminate under specific conditions
			errorLogger.join(); // make sure we get the whole output
			if (errorLogger.buffer.length() > 0) {
				System.err.println("--- javac err: ---");
				System.err.println(errorLogger.buffer.toString());
				result |= ERROR_LOG_MASK;
			}
		} finally {
			if (compileProcess != null) {
				compileProcess.destroy();
			}
		}
		return result;
	}
}
static class JavaRuntime {
	private String rootDirectoryPath;
	private String javaPathName;
	String version; // not intended to be modified - one of JavaCore.VERSION_1_*
	String rawVersion; // not intended to be modified - more complete version name
	int minor;
	private static HashMap runtimes = new HashMap();
	static JavaRuntime runtimeFor(JavacCompiler compiler) throws IOException, InterruptedException {
		JavaRuntime cached = (JavaRuntime) runtimes.get(compiler.rawVersion);
		if (cached == null) {
			cached = new JavaRuntime(compiler.rootDirectoryPath, compiler.version, compiler.rawVersion, compiler.minor);
			runtimes.put(compiler.rawVersion, cached);
		}
		return cached;
	}
	public static JavaRuntime fromCurrentVM() throws IOException, InterruptedException {
		String rawVersion = System.getProperty("java.version");
		JavaRuntime cached = (JavaRuntime) runtimes.get(rawVersion);
		if (cached == null) {
			String jreRootDirPath = Util.getJREDirectory();
			String version = JavacCompiler.versionFromRawVersion(rawVersion, jreRootDirPath);
			int minor = JavacCompiler.minorFromRawVersion(version, rawVersion);
			cached = new JavaRuntime(jreRootDirPath, version, rawVersion, minor);
			runtimes.put(rawVersion, cached);
		}
		return cached;
	}
	private JavaRuntime(String rootDirectoryPath, String version, String rawVersion, int minor) throws IOException, InterruptedException {
		this.rootDirectoryPath = rootDirectoryPath;
		this.javaPathName = new File(this.rootDirectoryPath + File.separator
				+ "bin" + File.separator + JAVA_NAME).getCanonicalPath();
		this.version = version;
		this.rawVersion = rawVersion;
		this.minor = minor;
	}
	// returns 0 if everything went fine
	int execute(File directory, String options, String className, StringBuffer stdout, StringBuffer stderr) throws IOException, InterruptedException {
		Process executionProcess = null;
		try {
			StringBuilder cmdLine = new StringBuilder(this.javaPathName);
			if (options.contains("-cp "))
				cmdLine.append(' '); // i.e., -cp will be appended below, just ensure separation from javaPathname
			else
				cmdLine.append(" -classpath . "); // default classpath
			cmdLine.append(options);
			cmdLine.append(' ');
			cmdLine.append(className);
			executionProcess = Runtime.getRuntime().exec(cmdLine.toString(), env, directory);
			Logger outputLogger = new Logger(executionProcess.getInputStream(),
					"RUNTIME OUTPUT", stdout == null ? new StringBuffer() : stdout);
			outputLogger.start();
			Logger errorLogger = new Logger(executionProcess.getErrorStream(),
					"RUNTIME ERROR", stderr == null ? new StringBuffer() : stderr);
			errorLogger.start();
			int result = executionProcess.waitFor(); // caveat: may never terminate under specific conditions
			outputLogger.join(); // make sure we get the whole output
			errorLogger.join(); // make sure we get the whole output
			return result;
		} finally {
			if (executionProcess != null) {
				executionProcess.destroy();
			}
		}
	}
}
@SuppressWarnings("synthetic-access")
protected static class JavacTestOptions {
	static final JavacTestOptions DEFAULT = new JavacTestOptions();
	static final JavacTestOptions SKIP = new JavacTestOptions() {
		@Override
		boolean skip(JavacCompiler compiler) {
			return true;
		}
	};
	static JavacTestOptions forRelease(String release) {
		JavacTestOptions options = new JavacTestOptions(Long.parseLong(release));
		if (isJRE9Plus)
			options.setCompilerOptions("-release "+release);
		else
			options.setCompilerOptions("-source 1."+release+" -target 1."+release);
		return options;
	}
	static JavacTestOptions forRelease(String release, String additionalOptions) {
		JavacTestOptions options = new JavacTestOptions(Long.parseLong(release));
		String result = isJRE9Plus ? "-release "+release : "-source 1."+release+" -target 1."+release;
		if (additionalOptions != null)
			result = result + " " + additionalOptions;
		options.setCompilerOptions(result);
		return options;
	}
	static JavacTestOptions forReleaseWithPreview(String release) {
		JavacTestOptions options = new JavacTestOptions(Long.parseLong(release));
		if (isJRE9Plus)
			options.setCompilerOptions("--release "+release+" --enable-preview -Xlint:-preview");
		else
			throw new IllegalArgumentException("preview not supported at release "+release);
		return options;
	}
	static JavacTestOptions forReleaseWithPreview(String release, String additionalOptions) {
		JavacTestOptions options = new JavacTestOptions(Long.parseLong(release));
		if (isJRE9Plus) {
			String result = "--release "+release+" --enable-preview -Xlint:-preview";
			if (additionalOptions != null)
				result = result + " " + additionalOptions;
			options.setCompilerOptions(result);

		}
		else
			throw new IllegalArgumentException("preview not supported at release "+release);
		return options;
	}
	public static class SuppressWarnings extends JavacTestOptions {
		public SuppressWarnings(String token) {
			setCompilerOptions("-Xlint:-"+token);
		}
	}
	// TODO (maxime) enable selective javac output dir manipulations between
	//      tests steps
	// some tests manipulate the OUTPUT_DIR explicitly between run*Test calls;
	// however, these manipulations are not reflected in the javac output
	// directory (yet); skipping until we fix this
	static final JavacTestOptions SKIP_UNTIL_FRAMEWORK_FIX = new JavacTestOptions() {
		@Override
		boolean skip(JavacCompiler compiler) {
			return true;
		}
	};
	long minJavacCompliance = 0;
	private String compilerOptions = "";
	public JavacTestOptions() {
	}
	public JavacTestOptions(String compilerOptions) {
		this.compilerOptions = compilerOptions;
	}
	public JavacTestOptions(long compliance) {
		this.minJavacCompliance = compliance;
	}
	String getCompilerOptions() {
		return this.compilerOptions;
	}
	public void setCompilerOptions(String options) {
		this.compilerOptions = options;
	}
	boolean skip(JavacCompiler compiler) {
		if (this.minJavacCompliance > 0)
			return compiler.compliance < this.minJavacCompliance;
		return false;
	}
	static class MismatchType {
		static final int EclipseErrorsJavacNone = 0x0001;
		static final int EclipseErrorsJavacWarnings = 0x0002;
		static final int JavacErrorsEclipseNone = 0x0004;
		static final int JavacErrorsEclipseWarnings = 0x0008;
		static final int EclipseWarningsJavacNone = 0x0010;
		static final int JavacWarningsEclipseNone = 0x0020;
		static final int StandardOutputMismatch = 0x0040;
		static final int ErrorOutputMismatch = 0x0080;
		static final int JavacAborted = 0x0100;
		static final int JavacNotLaunched = 0x0200;
		static final int JavaAborted = 0x0400;
		static final int JavaNotLaunched = 0x0800;
		static final int CompileErrorMismatch = 0x1000;
	}
	public static class Excuse extends JavacTestOptions {
		protected int mismatchType;
		public boolean isIntermittent;
		Excuse(int mismatchType) {
			this.mismatchType = mismatchType;
		}
		@Override
		Excuse excuseFor(JavacCompiler compiler) {
			return this;
		}
		public boolean clears(int mismatch) {
			return this.mismatchType == 0 || (this.mismatchType & mismatch) == mismatch; // one excuse can clear multiple mismatches
		}
		public static Excuse
			EclipseHasSomeMoreWarnings = RUN_JAVAC ?
				new Excuse(MismatchType.EclipseWarningsJavacNone) : null,
			EclipseWarningConfiguredAsError = RUN_JAVAC ?
				new Excuse(MismatchType.EclipseErrorsJavacWarnings | MismatchType.EclipseErrorsJavacNone) : null,
			JavacCompilesBogusReferencedFileAgain = RUN_JAVAC ?
				new Excuse(MismatchType.EclipseErrorsJavacNone) : null,
				// bugs not found on javac bug site, but points to a javac bug.
			JavacDoesNotCompileCorrectSource = RUN_JAVAC ?
				new JavacHasABug(MismatchType.JavacErrorsEclipseNone) : null,
			/* A General Excuse - Revisit periodically */
			JavacCompilesIncorrectSource = RUN_JAVAC ?
				new JavacHasABug(MismatchType.EclipseErrorsJavacNone |
						MismatchType.EclipseErrorsJavacWarnings |
						MismatchType.EclipseWarningsJavacNone) : null,
			JavacGeneratesIncorrectCode = RUN_JAVAC ?
					new JavacHasABug(MismatchType.StandardOutputMismatch) : null,
			JavacHasWarningsEclipseNotConfigured = RUN_JAVAC ?
					new JavacHasABug(MismatchType.JavacWarningsEclipseNone) : null,
			JavacHasErrorsEclipseHasWarnings = RUN_JAVAC ?
					new JavacHasABug(MismatchType.JavacErrorsEclipseWarnings) : null,
			JavacHasErrorsEclipseHasNone = RUN_JAVAC ?
					new JavacHasABug(MismatchType.JavacErrorsEclipseNone) : null;
	}
	Excuse excuseFor(JavacCompiler compiler) {
		return null;
	}
	public static class EclipseHasABug extends Excuse {
		EclipseHasABug(int mismatchType) {
			super(mismatchType);
		}
		public static EclipseHasABug
			EclipseBug159851 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=159851
				new EclipseHasABug(MismatchType.JavacErrorsEclipseNone) {
					@Override
					Excuse excuseFor(JavacCompiler compiler) {
						return compiler.compliance < ClassFileConstants.JDK1_7 ? this : null;
					}
				} : null,
			EclipseBug177715 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=177715
				new EclipseHasABug(MismatchType.JavacErrorsEclipseNone) {
					@Override
					Excuse excuseFor(JavacCompiler compiler) {
						return compiler.compliance < ClassFileConstants.JDK1_8 ? this : null; // in 1.8 rejected by both compilers
					}
				} : null,
			EclipseBug207935 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=207935
				new EclipseHasABug(MismatchType.EclipseErrorsJavacNone | MismatchType.EclipseWarningsJavacNone) : null,
			EclipseBug216558 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=216558
				new EclipseHasABug(MismatchType.JavacErrorsEclipseNone) : null,
			EclipseBug235550 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=235550
				new EclipseHasABug(MismatchType.JavacErrorsEclipseNone) : null,
			EclipseBug235809 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=235809
				new EclipseHasABug(MismatchType.StandardOutputMismatch) : null,
			EclipseBug236217 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=236217
				new EclipseHasABug(MismatchType.JavacErrorsEclipseNone) {
					@Override
					Excuse excuseFor(JavacCompiler compiler) {
						return compiler.compliance < ClassFileConstants.JDK1_8 ? this : null; // in 1.8 accepted by both compilers
					}
				} : null,
			EclipseBug236236 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=236236
				new EclipseHasABug(MismatchType.EclipseErrorsJavacNone) {
					@Override
					Excuse excuseFor(JavacCompiler compiler) {
						return compiler.compliance > ClassFileConstants.JDK1_5 ? this : null;
					}
				}: null,
			EclipseBug236242 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=236242
				new EclipseHasABug(MismatchType.EclipseErrorsJavacWarnings) {
					@Override
					Excuse excuseFor(JavacCompiler compiler) {
						return compiler.compliance == ClassFileConstants.JDK1_7 ? this : null;
					}
				}: null,
			EclipseBug236243 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=236243
				new EclipseHasABug(MismatchType.EclipseErrorsJavacNone) {
					@Override
					Excuse excuseFor(JavacCompiler compiler) {
						return compiler.compliance > ClassFileConstants.JDK1_6 ? this : null;
					}
				}: null,
			EclipseBug236379 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=236379
				new EclipseHasABug(MismatchType.EclipseWarningsJavacNone) {
					@Override
					Excuse excuseFor(JavacCompiler compiler) {
						return compiler.compliance > ClassFileConstants.JDK1_5 ? null : this;
					}
				}: null,
			EclipseBug424410 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=424410
				new EclipseHasABug(MismatchType.JavacErrorsEclipseNone) : null,
			EclipseBug427719 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=427719
				new EclipseHasABug(MismatchType.JavacErrorsEclipseWarnings) : null,
			EclipseBug421922 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=421922
						new EclipseHasABug(MismatchType.EclipseErrorsJavacNone) : null,
			EclipseBug428061 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=428061
								new EclipseHasABug(MismatchType.JavacErrorsEclipseNone |
										MismatchType.JavacErrorsEclipseWarnings) : null,
			EclipseBug510528 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=510528
				new EclipseHasABug(MismatchType.JavacErrorsEclipseNone) : null,
			EclipseBug531531 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=531531
					new EclipseHasABug(MismatchType.EclipseErrorsJavacNone) : null;
	}
	// Justification based upon:
	// - Eclipse bugs opened to investigate differences and closed as INVALID
	//   on grounds other than an identified javac bug;
	// - Eclipse bugs that discuss the topic in depth and explain why we made a
	//   given choice;
	// - explanations inlined here (no bug available, apparently not worth
	//   opening one).
	public static class EclipseJustification extends Excuse {
		EclipseJustification(int mismatchType) {
			super(mismatchType);
		}
		public static final EclipseJustification
			EclipseBug72704 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=72704
				new EclipseJustification(MismatchType.EclipseErrorsJavacNone) : null,
			EclipseBug83902 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=83902
				new EclipseJustification(MismatchType.EclipseWarningsJavacNone) {
					@Override
					Excuse excuseFor(JavacCompiler compiler) {
						return compiler.compliance > ClassFileConstants.JDK1_5 ? this : null;
					}
				} : null,
			EclipseBug83902b = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=83902
				new EclipseJustification(MismatchType.JavacErrorsEclipseWarnings) : null,
			EclipseBug95021 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=95021
				new EclipseJustification(MismatchType.JavacErrorsEclipseNone) {
					@Override
					Excuse excuseFor(JavacCompiler compiler) {
						return compiler.compliance == ClassFileConstants.JDK1_7 ? this : null;
					}
					// WORK consider adding reversed pivots
				} : null,
			EclipseBug126712 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=126712 & http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6342411
				new EclipseJustification(MismatchType.StandardOutputMismatch) {
					@Override
					Excuse excuseFor(JavacCompiler compiler) {
						return compiler.compliance > ClassFileConstants.JDK1_5 ? this : null;
					}
					// WORK consider adding reversed pivots
				} : null,
			EclipseBug126744 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=126744
				new EclipseJustification(MismatchType.JavacErrorsEclipseNone) : null,
			EclipseBug151275 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=151275
				new EclipseJustification(MismatchType.JavacErrorsEclipseNone) {
					@Override
					Excuse excuseFor(JavacCompiler compiler) {
						return compiler.compliance < ClassFileConstants.JDK1_7 ? this : null;
					}
				} : null,
			EclipseBug159214 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=159214
				new EclipseJustification(MismatchType.EclipseErrorsJavacNone) {
					@Override
					Excuse excuseFor(JavacCompiler compiler) {
						return compiler.compliance == ClassFileConstants.JDK1_6 ? this : null;
					}
					// WORK consider adding reversed pivots
				} : null,
			EclipseBug169017 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=169017
				new EclipseJustification(MismatchType.JavacErrorsEclipseNone) {
					@Override
					Excuse excuseFor(JavacCompiler compiler) {
						return compiler.compliance > ClassFileConstants.JDK1_5 ? this : null;
					}
					// WORK consider adding reversed pivots
				} : null,
			EclipseBug180789 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=180789
				new EclipseJustification(MismatchType.EclipseErrorsJavacWarnings) : null,
			EclipseBug218677 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=218677
				new EclipseJustification(MismatchType.EclipseErrorsJavacNone) {
					@Override
					Excuse excuseFor(JavacCompiler compiler) {
						return compiler.compliance > ClassFileConstants.JDK1_6 ? this : null;
					}
					// WORK consider adding reversed pivots
				} : null,
			EclipseBug234815 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=234815
				new EclipseJustification(MismatchType.JavacErrorsEclipseNone) {
					@Override
					Excuse excuseFor(JavacCompiler compiler) {
						return compiler.compliance < ClassFileConstants.JDK1_7 ? this : null;
					}
				}: null,
			EclipseBug235543 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=235543
				new EclipseJustification(MismatchType.EclipseErrorsJavacNone) : null,
			EclipseBug235546 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=235546
				new EclipseJustification(MismatchType.JavacErrorsEclipseNone) : null,
			EclipseBug449063 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=449063
					 new EclipseJustification(MismatchType.StandardOutputMismatch) : null,
			EclipseBug561549 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=561549
					 new EclipseJustification(MismatchType.EclipseErrorsJavacNone) {
						@Override
						Excuse excuseFor(JavacCompiler compiler) {
							return compiler.compliance > ClassFileConstants.JDK9 ? this : null;
						}
					 } : null;
		public static final EclipseJustification
			EclipseJustification0001 = RUN_JAVAC ?
					new EclipseJustification(MismatchType.EclipseErrorsJavacNone) {
						@Override
						Excuse excuseFor(JavacCompiler compiler) {
							return compiler.compliance < ClassFileConstants.JDK1_7 ? this : null;
						}
					} : null;
			/* javac 1.6- properly detects duplicate attributes in annotations in the
			 * simplest case (AnnotationTest#18b) but fails on a slightly more
			 * complex one where the duplicate is within an embedded annotation;
			 * there seems to be no reason for not reporting the error
			 * (AnnotationTest#18) */
	}
	public static class JavacHasABug extends Excuse {
		long pivotCompliance;
		int pivotMinor;
		int[] minorsFixed;
		static final int NO_FIX = -1;
		static final int IRRELEVANT = -2;
		JavacHasABug(int mismatchType) {
			super(mismatchType);
		}
//		private JavacHasABug(int mismatchType, int[] minorsFixed) {
//			super(mismatchType);
//			this.minorsFixed = minorsFixed;
//		}
		private JavacHasABug(int mismatchType, long pivotCompliance, int pivotMinor) {
			super(mismatchType);
			this.pivotCompliance = pivotCompliance;
			this.pivotMinor = pivotMinor;
		}
		public JavacHasABug(int mismatchType, long pivotCompliance, int pivotMinor, boolean intermittent) {
			this(mismatchType, pivotCompliance, pivotMinor);
			this.isIntermittent = intermittent;
		}
		@Override
		Excuse excuseFor(JavacCompiler compiler) {
			if (this.minorsFixed != null) {
				if (compiler.compliance == ClassFileConstants.JDK1_8) {
					return this.minorsFixed[5] > compiler.minor || this.minorsFixed[5] < 0 ?
							this : null;
				} else if (compiler.compliance == ClassFileConstants.JDK1_7) {
					return this.minorsFixed[4] > compiler.minor || this.minorsFixed[4] < 0 ?
							this : null;
				} else if (compiler.compliance == ClassFileConstants.JDK1_6) {
					return this.minorsFixed[3] > compiler.minor || this.minorsFixed[3] < 0 ?
							this : null;
				} else if (compiler.compliance == ClassFileConstants.JDK1_5) {
					return this.minorsFixed[2] > compiler.minor || this.minorsFixed[2] < 0 ?
							this : null;
				} else if (compiler.compliance == ClassFileConstants.JDK1_4) {
					return this.minorsFixed[1] > compiler.minor || this.minorsFixed[1] < 0 ?
							this : null;
				} else if (compiler.compliance == ClassFileConstants.JDK1_3) {
					return this.minorsFixed[0] > compiler.minor || this.minorsFixed[0] < 0 ?
							this : null;
				}
				throw new RuntimeException(); // should not get there
			} else if (this.pivotCompliance > 0) {
				if (this.pivotCompliance < compiler.compliance) {
					return null;
				} else if (this.pivotCompliance > compiler.compliance) {
					return this;
				} else {
					return this.pivotMinor > compiler.minor ? this : null;
				}
			} else if (this.pivotCompliance < 0) {
				if (this.pivotCompliance < -compiler.compliance) {
					return null;
				} else if (this.pivotCompliance > -compiler.compliance) {
					return this;
				} else {
					return this.pivotMinor <= compiler.minor ? this : null;
				}
			}
			return this;
		}
		// bugs that we know precisely of
		public static JavacHasABug
			JavacBug4094180 = RUN_JAVAC ? // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4094180
				new JavacHasABug(MismatchType.EclipseErrorsJavacNone) : null,
			JavacBug4660984 = RUN_JAVAC ? // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4660984 & https://bugs.eclipse.org/bugs/show_bug.cgi?id=235555
				new JavacHasABug(MismatchType.JavacErrorsEclipseNone) : null,
			JavacBug5042462 = RUN_JAVAC ? // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5042462 & https://bugs.eclipse.org/bugs/show_bug.cgi?id=208873
				new JavacHasABug(
					MismatchType.JavacErrorsEclipseNone,
					ClassFileConstants.JDK1_7, 0 /* 1.7.0 b17 */) : null,
			JavacBug5061359 = RUN_JAVAC ? // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5061359
				new JavacHasABug(
					MismatchType.EclipseErrorsJavacNone,
					ClassFileConstants.JDK1_7, 0 /* 1.7.0 b03 */) : null,
			JavacBug6302954 = RUN_JAVAC ? // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6302954 & https://bugs.eclipse.org/bugs/show_bug.cgi?id=98379
				new JavacHasABug(
					MismatchType.JavacErrorsEclipseNone,
					ClassFileConstants.JDK1_7, 0 /* 1.7.0 b03 */) : null,
			JavacBug6400189 = RUN_JAVAC ? // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6400189 & https://bugs.eclipse.org/bugs/show_bug.cgi?id=106744 & https://bugs.eclipse.org/bugs/show_bug.cgi?id=167952
				new JavacHasABug(
					MismatchType.EclipseErrorsJavacNone) {
						@Override
						Excuse excuseFor(JavacCompiler compiler) {
							return compiler.compliance == ClassFileConstants.JDK1_6 ? this : null;
						}
					} : null,
			JavacBug6500701 = RUN_JAVAC ? // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6500701 & https://bugs.eclipse.org/bugs/show_bug.cgi?id=209779
				new JavacHasABug(
					MismatchType.StandardOutputMismatch,
					ClassFileConstants.JDK1_7, 0) : null,
			JavacBug6531075 = RUN_JAVAC ? // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6531075
				new JavacHasABug(
					MismatchType.StandardOutputMismatch,
					ClassFileConstants.JDK1_7, 0) : null, // fixed in jdk7 b27; unfortunately, we do not have a distinct minor for this, hence former jdk7s will report an unused excuse
			JavacBug6569404 = RUN_JAVAC ? // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6569404
				new JavacHasABug(
					MismatchType.JavacErrorsEclipseNone) {
						@Override
						Excuse excuseFor(JavacCompiler compiler) {
							// present only in javac6 between 1.6.0_10_b08 and EOL
							return (compiler.compliance == ClassFileConstants.JDK1_6 && compiler.minor >= 10) ? this : null;
						}
					} : null,
			JavacBug6557661 = RUN_JAVAC ? // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6557661 & https://bugs.eclipse.org/bugs/show_bug.cgi?id=129261
				new JavacHasABug(
					MismatchType.EclipseErrorsJavacNone) : null,
			JavacBug6573446 = RUN_JAVAC ? // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6573446 & https://bugs.eclipse.org/bugs/show_bug.cgi?id=190945
				new JavacHasABug(
					MismatchType.EclipseErrorsJavacNone) : null,
			JavacBug6575821 = RUN_JAVAC ? // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6575821
				new JavacHasABug(
					MismatchType.JavacErrorsEclipseNone,
					ClassFileConstants.JDK1_6, 10 /* 1.6.0_10_b08 or better - maybe before */) : null,
			JavacBug8033810 = RUN_JAVAC ? // https://bugs.openjdk.java.net/browse/JDK-8033810
				new JavacHasABug(MismatchType.EclipseErrorsJavacNone) : null,
			JavacBug8144673 = RUN_JAVAC ? // https://bugs.openjdk.java.net/browse/JDK-8144673
				new JavacHasABug(MismatchType.JavacErrorsEclipseNone, ClassFileConstants.JDK9, 0100) : null,
			JavacBug8204534 = RUN_JAVAC ? // https://bugs.openjdk.java.net/browse/JDK-8204534
				new JavacHasABug(MismatchType.EclipseErrorsJavacNone, ClassFileConstants.JDK11, 0000) : null,
			JavacBug8207032 = RUN_JAVAC ? // https://bugs.openjdk.java.net/browse/JDK-8207032
				new JavacHasABug(MismatchType.EclipseErrorsJavacNone) : null,
			JavacBug8044196 = RUN_JAVAC ? // likely https://bugs.openjdk.java.net/browse/JDK-8044196, intermittently masked by https://bugs.openjdk.java.net/browse/JDK-8029161
				new JavacHasABug(MismatchType.EclipseErrorsJavacNone, ClassFileConstants.JDK9, 0000, true) : null,
			JavacBug6337964 = RUN_JAVAC ? // https://bugs.eclipse.org/bugs/show_bug.cgi?id=112433
					new JavacHasABug(MismatchType.JavacErrorsEclipseNone, ClassFileConstants.JDK1_6, 1045/*guessed*/, true) : null,
			JavacBug8144832 = RUN_JAVAC ? // https://bugs.openjdk.java.net/browse/JDK-8144832
					new JavacHasABug(MismatchType.JavacErrorsEclipseNone, ClassFileConstants.JDK9, 0000) : null,
			JavacBug8179483_switchExpression = RUN_JAVAC ? // https://bugs.openjdk.java.net/browse/JDK-8179483
					new JavacBug8179483(" --release 13 --enable-preview -Xlint:-preview") : null,
			JavacBug8221413_switchExpression = RUN_JAVAC ? // https://bugs.openjdk.java.net/browse/JDK-8221413
					new JavacBug8221413(" --release 12 --enable-preview -Xlint:-preview") : null,
			JavacBug8226510_switchExpression = RUN_JAVAC ? // https://bugs.openjdk.java.net/browse/JDK-8226510
					new JavacBug8226510(" --release 12 --enable-preview -Xlint:-preview") : null,
			JavacBug8231436 = RUN_JAVAC ? // https://bugs.openjdk.java.net/browse/JDK-8231436 to implement https://bugs.openjdk.java.net/browse/JDK-8231435
				    new JavacHasABug(MismatchType.JavacErrorsEclipseNone) : null,
			JavacBug8231436_EclipseWarns = RUN_JAVAC ? // https://bugs.openjdk.java.net/browse/JDK-8231436 to implement https://bugs.openjdk.java.net/browse/JDK-8231435
				    new JavacHasABug(MismatchType.JavacErrorsEclipseWarnings) : null;

		// bugs that have been fixed but that we've not identified
		public static JavacHasABug
			JavacBugFixed_6_10 = RUN_JAVAC ?
				new JavacHasABug(
					0 /* all */,
					ClassFileConstants.JDK1_6, 1000 /* 1.6.0_10_b08 or better - maybe before */) : null,
			JavacBugFixed_6_10_b24 = RUN_JAVAC ?
				new JavacHasABug(
					0 /* all */,
					ClassFileConstants.JDK1_6, 1010 /* 1.6.0_10_b24 or better - maybe before */) : null,
			JavacBugFixed_7 = RUN_JAVAC ?
				new JavacHasABug(
					0 /* all */,
					ClassFileConstants.JDK1_7, 0 /* 1.7.0_b24 or better - maybe before */) : null,
			JavacBugFixed_901 = RUN_JAVAC ?
				new JavacHasABug(
					0 /* all */,
					ClassFileConstants.JDK9, 0100 /* 9.0.1 or better */) : null;
		// bugs that have neither been fixed nor formally identified but which outcomes are obvious enough to clear any doubts
		public static JavacHasABug
			JavacThrowsAnException = RUN_JAVAC ? // some of these are transient - that is, depend on the system on which the test is run, aka stack overflow
				new JavacHasABug(
					MismatchType.JavacErrorsEclipseNone) : null,
			JavacThrowsAnExceptionForJava_1_5_0_16 = RUN_JAVAC ?
					new JavacHasABug(
						MismatchType.JavacErrorsEclipseNone) {
							@Override
							Excuse excuseFor(JavacCompiler compiler) {
								return compiler.compliance != ClassFileConstants.JDK1_5 ||
										compiler.minor != 1600 ? null : this;
							}
					}: null,
			JavacThrowsAnExceptionForJava_since9_EclipseWarns = RUN_JAVAC ?
					new JavacHasABug(
						MismatchType.JavacErrorsEclipseWarnings) {
							@Override
							Excuse excuseFor(JavacCompiler compiler) {
								return compiler.compliance < ClassFileConstants.JDK9 ? null : this;
							}
					}: null;
		public static JavacHasABug
				NoWarningForMissingJavadocTag = RUN_JAVAC ?
						new JavacHasABug(MismatchType.EclipseErrorsJavacNone)
						: null,
				NoWarningForDuplicateJavadocTag = RUN_JAVAC ?
						new JavacHasABug(MismatchType.EclipseErrorsJavacNone)
						: null;
	}
	public static class JavacBug8179483 extends JavacHasABug {
		String extraJavacOptions;
		public JavacBug8179483(String extraJavacOptions) {
			super(MismatchType.EclipseErrorsJavacWarnings);
			this.extraJavacOptions = extraJavacOptions;
		}
		@Override
		String getCompilerOptions() {
			return super.getCompilerOptions() + this.extraJavacOptions;
		}
	}
	public static class JavacBug8221413 extends JavacHasABug {
		String extraJavacOptions;
		public JavacBug8221413(String extraJavacOptions) {
			super(MismatchType.JavacErrorsEclipseNone);
			this.extraJavacOptions = extraJavacOptions;
		}
		@Override
		String getCompilerOptions() {
			return super.getCompilerOptions() + this.extraJavacOptions;
		}
	}
	public static class JavacBug8226510 extends JavacHasABug {
		String extraJavacOptions;
		public JavacBug8226510(String extraJavacOptions) {
			super(MismatchType.EclipseErrorsJavacWarnings);
			this.extraJavacOptions = extraJavacOptions;
		}
		@Override
		String getCompilerOptions() {
			return super.getCompilerOptions() + this.extraJavacOptions;
		}
	}
}

// PREMATURE: Logger helps us monitor processes outputs (standard and error);
//            some asynchronous mechanism is needed here since not consuming
//            the streams fast enough can result into bad behaviors (as
//            documented in Process); however, we could have a single worker
//            take care of this
	static class Logger extends Thread {
		StringBuffer buffer;
		InputStream inputStream;
		String type;
		Logger(InputStream inputStream, String type) {
			this.inputStream = inputStream;
			this.type = type;
			this.buffer = new StringBuffer();
		}
		Logger(InputStream inputStream, String type, StringBuffer buffer) {
			this.inputStream = inputStream;
			this.type = type;
			this.buffer = buffer;
		}

		@Override
		public void run() {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(this.inputStream));
				String line = null;
				while ((line = reader.readLine()) != null) {
					this.buffer./*append(this.type).append("->").*/append(line).append("\n");
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	protected static int[] DIFF_COUNTERS = new int[3];
	protected static final String EVAL_DIRECTORY = Util.getOutputDirectory()  + File.separator + "eval";
	public static int INDENT = 2;
	protected static final String JAVA_NAME =
		File.pathSeparatorChar == ':' ? "java" : "java.exe";
	protected static final String JAVAC_NAME =
		File.pathSeparatorChar == ':' ? "javac" : "javac.exe";

	protected static String JAVAC_OUTPUT_DIR_NAME =
		Util.getOutputDirectory() + File.separator + "javac";
	static File JAVAC_OUTPUT_DIR;
	protected static String javacCommandLineHeader;
	protected static PrintWriter javacFullLog;
	// flags errors so that any error in a test case prevents
	  // java execution
	private static String javacFullLogFileName;
	protected static String javaCommandLineHeader;

	// needed for multiple test calls within a single test method
	protected static boolean javacTestErrorFlag;

	protected static String javacTestName;

	protected static IPath jdkRootDirPath;

	// list of available javac compilers, as defined by the jdk.roots
	// variable, which should hold a File.pathSeparatorChar separated
	// list of paths for to-be-tested JDK root directories
	protected static List<JavacCompiler> javacCompilers = null;

	public static final String OUTPUT_DIR = Util.getOutputDirectory() + File.separator + "regression";
	public static final String LIB_DIR = Util.getOutputDirectory() + File.separator + "lib";

	public final static String PACKAGE_INFO_NAME = new String(TypeConstants.PACKAGE_INFO_NAME);
	public final static String MODULE_INFO_NAME = new String(TypeConstants.MODULE_INFO_NAME);

	public static boolean SHIFT = false;
	public static String PREVIEW_ALLOWED_LEVEL = JavaCore.latestSupportedJavaVersion();

	protected static final String SOURCE_DIRECTORY = Util.getOutputDirectory()  + File.separator + "source";

	protected String[] classpaths;
	protected boolean createdVerifier;
	protected INameEnvironment javaClassLib;
	protected TestVerifier verifier;
	protected boolean shouldSwallowCaptureId;
	public AbstractRegressionTest(String name) {
		super(name);
	}
	protected boolean checkPreviewAllowed() {
		return this.complianceLevel == ClassFileConstants.getLatestJDKLevel();
	}
	protected void checkClassFile(String className, String source, String expectedOutput) throws ClassFormatException, IOException {
		this.checkClassFile("", className, source, expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}
	protected void checkClassFile(String className, String source, String expectedOutput, int mode) throws ClassFormatException, IOException {
		this.checkClassFile("", className, source, expectedOutput, mode);
	}
	protected void checkClassFile(String directoryName, String className, String disassembledClassName, String source, String expectedOutput,
			int mode, boolean suppressConsole) throws ClassFormatException, IOException
	{
		compileAndDeploy(source, directoryName, className, suppressConsole);
		try {
			File directory = new File(EVAL_DIRECTORY, directoryName);
			if (!directory.exists()) {
				assertTrue(".class file not generated properly in " + directory, false);
			}
			File f = new File(directory, disassembledClassName + ".class");
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			String result = disassembler.disassemble(classFileBytes, "\n", mode);
			int index = result.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(result, 3));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, result);
			}
			FileInputStream stream = null;
			try {
				stream = new FileInputStream(f);
				ClassFileReader.read(stream, className + ".class", true);
			} catch (org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException e) {
				e.printStackTrace();
				assertTrue("ClassFormatException", false);
			} catch (IOException e) {
				e.printStackTrace();
				assertTrue("IOException", false);
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException e) {
						/* ignore */
					}
				}
			}
		} finally {
			removeTempClass(className);
		}
	}

	protected void checkClassFile(String directoryName, String className, String source, String expectedOutput, int mode) throws ClassFormatException, IOException {
		this.checkClassFile(directoryName, className, className, source, expectedOutput, mode, false);
	}

	protected ClassFileReader getInternalClassFile(String directoryName, String className, String disassembledClassName, String source) throws ClassFormatException, IOException {
		compileAndDeploy(source, directoryName, className, false);
		try {
			File directory = new File(EVAL_DIRECTORY, directoryName);
			if (!directory.exists()) {
				assertTrue(".class file not generated properly in " + directory, false);
			}
			File f = new File(directory, disassembledClassName + ".class");
			ClassFileReader classFileReader = null;
			FileInputStream stream = null;
			try {
				stream = new FileInputStream(f);
				classFileReader = ClassFileReader.read(stream, className + ".class", true);
			} catch (org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException e) {
				e.printStackTrace();
				assertTrue("ClassFormatException", false);
			} catch (IOException e) {
				e.printStackTrace();
				assertTrue("IOException", false);
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException e) {
						/* ignore */
					}
				}
			}
			return classFileReader;
		} finally {
			removeTempClass(className);
		}
	}

	protected void checkDisassembledClassFile(String fileName, String className, String expectedOutput) throws Exception {
		this.checkDisassembledClassFile(fileName, className, expectedOutput, ClassFileBytesDisassembler.DETAILED);
	}
	protected void checkDisassembledClassFile(String fileName, String className, String expectedOutput, int mode) throws Exception {
		File classFile = new File(fileName);
		if (!classFile.exists()) {
			assertTrue(".class file doesn't exist", false);
		}
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(classFile);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", mode);
		int index = result.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(result, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, result);
		}
		index = result.indexOf(Messages.classformat_classformatexception);
		if (index != -1) {
			int start = Math.max(0, index - 300);
			int end = index + Messages.classformat_classformatexception.length();
			fail("ClassFormatException swallowed in Disassembler:\n..." + result.substring(start, end));
		}

		FileInputStream stream = null;
		try {
			stream = new FileInputStream(classFile);
			ClassFileReader.read(stream, className + ".class", true);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					/* ignore */
				}
			}
		}
	}

	protected void compileAndDeploy(String source, String directoryName, String className, boolean suppressConsole) {
		File directory = new File(SOURCE_DIRECTORY);
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				System.out.println("Could not create " + SOURCE_DIRECTORY);
				return;
			}
		}
		if (directoryName != null && directoryName.length() != 0) {
			directory = new File(SOURCE_DIRECTORY, directoryName);
			if (!directory.exists()) {
				if (!directory.mkdirs()) {
					System.out.println("Could not create " + directory);
					return;
				}
			}
		}
		String fileName = directory.getAbsolutePath() + File.separator + className + ".java";
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			writer.write(source);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		StringBuffer buffer = new StringBuffer()
			.append("\"")
			.append(fileName)
			.append("\" -d \"")
			.append(EVAL_DIRECTORY);
		String processAnnot = this.enableAPT ? "" : "-proc:none";
		if (this.complianceLevel < ClassFileConstants.JDK1_5) {
			buffer.append("\" -1.4 -source 1.3 -target 1.2");
		} else if (this.complianceLevel == ClassFileConstants.JDK1_5) {
			buffer.append("\" -1.5");
		} else if (this.complianceLevel == ClassFileConstants.JDK1_6) {
			buffer.append("\" -1.6 " + processAnnot);
		} else if (this.complianceLevel == ClassFileConstants.JDK1_7) {
			buffer.append("\" -1.7 " + processAnnot);
		} else if (this.complianceLevel == ClassFileConstants.JDK1_8) {
			buffer.append("\" -1.8 " + processAnnot);
		} else if (this.complianceLevel == ClassFileConstants.JDK9) {
			buffer.append("\" -9 " + processAnnot);
		} else if (this.complianceLevel == ClassFileConstants.JDK10) {
			buffer.append("\" -10 " + processAnnot);
		} else {
			int major = (int)(this.complianceLevel>>16);
			buffer.append("\" -" + (major - ClassFileConstants.MAJOR_VERSION_0));
		}
		if (this.complianceLevel == ClassFileConstants.getLatestJDKLevel()  && this.enablePreview)
			buffer.append(" --enable-preview ");
		buffer
			.append(" -preserveAllLocals -proceedOnError -nowarn -g -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(SOURCE_DIRECTORY)
			.append("\"");
		OutputStream out = System.out;
		OutputStream err = System.err;
		if (suppressConsole) {
			out = err = new OutputStream() {
				@Override
				public void write(int b) {
					// silently swallow
				}
			};
		}
		BatchCompiler.compile(buffer.toString(), new PrintWriter(out), new PrintWriter(err), null/*progress*/);
	}

	/*
	 * Compute the problem log from given requestor and compare the result to
	 * the expected one.
	 * When there's a difference, display the expected output in the console as
	 * code string to allow easy copy/paste in the test to fix the failure.
	 * Also write test files to the console output.
	 * Fail if exception is non null.
	 */
	protected void checkCompilerLog(String[] testFiles, Requestor requestor,
			String[] alternatePlatformIndependantExpectedLogs, Throwable exception) {
		String computedProblemLog = Util.convertToIndependantLineDelimiter(requestor.problemLog.toString());
		if (this.shouldSwallowCaptureId)
			computedProblemLog = Pattern.compile("capture#(\\d+)").matcher(computedProblemLog).replaceAll("capture");

		ProblemLog problemLog = new ProblemLog(computedProblemLog);
		int i;
		for (i = 0; i < alternatePlatformIndependantExpectedLogs.length; i++) {
			if (problemLog.sameAs(alternatePlatformIndependantExpectedLogs[i]))
				return; // OK
		}
		logTestTitle();
		System.out.println(Util.displayString(computedProblemLog, INDENT, SHIFT));
		logTestFiles(false, testFiles);
		if (exception == null) {
			assertEquals("Invalid problem log ", alternatePlatformIndependantExpectedLogs[i-1], computedProblemLog);
		}
    }

	/**
	 * Used for performing order-independent log comparisons.
	 */
	private static final class ProblemLog {
		final Set<String> logEntry = new HashSet<>();

		public ProblemLog(String log) {
			String[] entries = log.split("----------\n");
			Pattern pattern = Pattern.compile("\\A(\\d*\\. )");

			for (String entry : entries) {
				Matcher matcher = pattern.matcher(entry);
				if (matcher.find()) {
					entry = entry.substring(matcher.end());
				}
				this.logEntry.add(entry);
			}
		}

		public boolean sameAs(String toTest) {
			ProblemLog log = new ProblemLog(toTest);
			return equals(log);
		}

		@Override
		public int hashCode() {
			return this.logEntry.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ProblemLog other = (ProblemLog) obj;
			return this.logEntry.equals(other.logEntry);
		}
	}

	protected void dualPrintln(String message) {
		System.out.println(message);
		javacFullLog.println(message);
	}
	protected void executeClass(
			String sourceFile,
			String expectedSuccessOutputString,
			String[] classLib,
			boolean shouldFlushOutputDirectory,
			String[] vmArguments,
			Map customOptions,
			ICompilerRequestor clientRequestor) {

		// Compute class name by removing ".java" and replacing slashes with dots
		String className = sourceFile.substring(0, sourceFile.lastIndexOf('.')).replace('/', '.').replace('\\', '.');
		if (className.endsWith(PACKAGE_INFO_NAME)) return;

		if (vmArguments != null) {
			if (this.verifier != null) {
				this.verifier.shutDown();
			}
			this.verifier = new TestVerifier(false);
			this.createdVerifier = true;
		}
		boolean passed =
			this.verifier.verifyClassFiles(
				sourceFile,
				className,
				expectedSuccessOutputString,
				this.classpaths,
				null,
				vmArguments);
		assertTrue(this.verifier.failureReason, // computed by verifyClassFiles(...) action
				passed);
		if (vmArguments != null) {
			if (this.verifier != null) {
				this.verifier.shutDown();
			}
			this.verifier = new TestVerifier(false);
			this.createdVerifier = true;
		}
	}

	/*
	 * Returns the references in the given .class file.
	 */
	protected String findReferences(String classFilePath) {
		// check that "new Z().init()" is bound to "AbstractB.init()"
		final StringBuilder references = new StringBuilder(10);
		final SearchParticipant participant = new JavaSearchParticipant() {
			final SearchParticipant searchParticipant = this;
			public SearchDocument getDocument(final String documentPath) {
				return new SearchDocument(documentPath, this.searchParticipant) {
					public byte[] getByteContents() {
						try {
							return  org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(getPath()));
						} catch (IOException e) {
							e.printStackTrace();
							return null;
						}
					}
					public char[] getCharContents() {
						// not used
						return null;
					}
					public String getEncoding() {
						// not used
						return null;
					}
				};
			}
		};
		SearchDocument document = participant.getDocument(new File(classFilePath).getPath());
		BinaryIndexer indexer = new BinaryIndexer(document) {
			protected void addIndexEntry(char[] category, char[] key) {
				references.append(category);
				references.append('/');
				references.append(key);
				references.append('\n');
			}
		};
		indexer.indexDocument();
		String computedReferences = references.toString();
		return computedReferences;
	}

	protected ClassFileReader getClassFileReader(String fileName, String className) {
		File classFile = new File(fileName);
		if (!classFile.exists()) {
			assertTrue(".class file doesn't exist", false);
		}
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(classFile);
			ClassFileReader reader = ClassFileReader.read(stream, className + ".class", true);
			return reader;
		} catch (org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException e) {
			e.printStackTrace();
			assertTrue("ClassFormatException", false);
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue("IOException", false);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					/* ignore */
				}
			}
		}
		return null;
	}

	protected INameEnvironment[] getClassLibs(boolean useDefaultClasspaths, Map<String, String> options) {
		if (options == null)
			options = getCompilerOptions();
		String encoding = getCompilerOptions().get(CompilerOptions.OPTION_Encoding);
		if ("".equals(encoding))
			encoding = null;
		String release = null;
		if (CompilerOptions.ENABLED.equals(options.get(CompilerOptions.OPTION_Release))) {
			release = getCompilerOptions().get(CompilerOptions.OPTION_Compliance);
		}
		if (useDefaultClasspaths && encoding == null)
			return DefaultJavaRuntimeEnvironment.create(this.classpaths, release);
		// fall back to FileSystem
		INameEnvironment[] classLibs = new INameEnvironment[1];
		classLibs[0] = new FileSystem(this.classpaths, new String[]{}, // ignore initial file names
				encoding, release
		);
		return classLibs;
	}
	protected INameEnvironment[] getClassLibs(boolean useDefaultClasspaths) {
		return getClassLibs(useDefaultClasspaths, null);
	}
	@Override
	protected Map<String, String> getCompilerOptions() {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_LocalVariableAttribute, CompilerOptions.GENERATE);
		defaultOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
		defaultOptions.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.WARNING);
		defaultOptions.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.WARNING);
		defaultOptions.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.WARNING);
		defaultOptions.put(CompilerOptions.OPTION_ReportPossibleAccidentalBooleanAssignment, CompilerOptions.WARNING);
		defaultOptions.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.WARNING);
		defaultOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
		defaultOptions.put(CompilerOptions.OPTION_ReportUnnecessaryElse, CompilerOptions.WARNING );
		defaultOptions.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.WARNING);
		return defaultOptions;
	}
	protected boolean isMinimumCompliant(long compliance) {
		Map options = getCompilerOptions();
		CompilerOptions compOptions = new CompilerOptions(options);
		return compOptions.complianceLevel >= compliance;
	}

	protected void enableAllWarningsForIrritants(Map<String, String> options, IrritantSet irritants) {
		int[] bits = irritants.getBits();
		for (int i = 0; i < bits.length; i++) {
			int bit = bits[i];
			for (int b = 0; b < IrritantSet.GROUP_SHIFT; b++) {
				int single = bit & (1 << b);
				if (single != 0) {
					single |= (i<<IrritantSet.GROUP_SHIFT);
					if (single == CompilerOptions.MissingNonNullByDefaultAnnotation)
						continue;
					String optionKey = CompilerOptions.optionKeyFromIrritant(single);
					options.put(optionKey, CompilerOptions.WARNING);
				}
			}
		}
	}

	protected String[] getDefaultClassPaths() {
		return DefaultJavaRuntimeEnvironment.getDefaultClassPaths();
	}
	/** Get class library paths built from default class paths plus the JDT null annotations. */
	protected String[] getLibsWithNullAnnotations(long sourceLevel) {
		String[] defaultLibs = getDefaultClassPaths();
		int len = defaultLibs.length;
		String[] libs = new String[len+1];
		System.arraycopy(defaultLibs, 0, libs, 0, len);
		String version = sourceLevel < ClassFileConstants.JDK1_8 ? "[1.1.0,2.0.0)" : "[2.0.0,3.0.0)";
		Bundle[] bundles = Platform.getBundles("org.eclipse.jdt.annotation", version);
		File bundleFile = FileLocator.getBundleFileLocation(bundles[0]).get();
		if (bundleFile.isDirectory())
			libs[len] = bundleFile.getPath()+"/bin";
		else
			libs[len] = bundleFile.getPath();
		return libs;
	}
	protected IErrorHandlingPolicy getErrorHandlingPolicy() {
		return new IErrorHandlingPolicy() {
			public boolean stopOnFirstError() {
				return false;
			}
			public boolean proceedOnErrors() {
				return true;
			}
			public boolean ignoreAllErrors() {
				return false;
			}
		};
	}
	static class CustomFileSystem extends FileSystem {
		// make protected constructor accessible
		CustomFileSystem(Collection<String> limitModules) {
			super(Util.getJavaClassLibs(), new String[0], null, limitModules);
		}
	}
	/*
	 * Will consider first the source units passed as arguments, then investigate the classpath: jdklib + output dir
	 */
	protected INameEnvironment getNameEnvironment(final String[] testFiles, String[] classPaths, Map<String, String> options) {
		this.classpaths = classPaths == null ? getDefaultClassPaths() : classPaths;
		return new InMemoryNameEnvironment(testFiles, getClassLibs((classPaths == null), options));
	}
	protected INameEnvironment getNameEnvironment(final String[] testFiles, String[] classPaths) {
		return getNameEnvironment(testFiles, classPaths, null);
	}
	protected IProblemFactory getProblemFactory() {
		return new DefaultProblemFactory(Locale.getDefault());
	}
	// overridden in AbstractRegressionTests9
	protected CompilationUnit[] getCompilationUnits(String[] testFiles) {
		return Util.compilationUnits(testFiles);
	}

	@Override
	public void initialize(CompilerTestSetup setUp) {
		super.initialize(setUp);
		if (setUp instanceof RegressionTestSetup) {
			RegressionTestSetup regressionTestSetUp = (RegressionTestSetup)setUp;
			this.javaClassLib = regressionTestSetUp.javaClassLib;
			this.verifier = regressionTestSetUp.verifier;
		}
	}

	void logTestFiles(boolean logTitle, String[] testFiles) {
		if (logTitle) {
			logTestTitle();
		}
		for (int i = 0; i < testFiles.length; i += 2) {
			System.out.print(testFiles[i]);
			System.out.println(" ["); //$NON-NLS-1$
			String content = testFiles[i + 1];
			if (content.length() > 10000) {
				System.out.println(content.substring(0, 10000));
				System.out.println("...(truncated)"); //$NON-NLS-1$
			} else {
				System.out.println(content);
			}
			System.out.println("]"); //$NON-NLS-1$
		}
	}
	void logTestTitle() {
		System.out.println(getClass().getName() + '#' + getName());
	}

	/*
	 * Write given source test files in current output sub-directory.
	 * Use test name for this sub-directory name (ie. test001, test002, etc...)
	 */
	protected void printFiles(String[] testFiles) {
		for (int i=0, length=testFiles.length; i<length; i++) {
			System.out.println(testFiles[i++]);
			String content = testFiles[i];
			if (content.length() > 10000) {
				System.out.println(content.substring(0, 10000));
				System.out.println("...(truncated)"); //$NON-NLS-1$
			} else {
				System.out.println(content);
			}
		}
		System.out.println("");
	}
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
						dualPrintln("	- "+DIFF_COUNTERS[0]+" test(s) where Javac found errors/warnings but Eclipse did not");
					if (DIFF_COUNTERS[1]!=0)
						dualPrintln("	- "+DIFF_COUNTERS[1]+" test(s) where Eclipse found errors/warnings but Javac did not");
					if (DIFF_COUNTERS[2]!=0)
						dualPrintln("	- "+DIFF_COUNTERS[2]+" test(s) where Eclipse and Javac did not have same output");
					System.out.println("\n");
				}
			}
			dualPrintln("\n\nFull results sent to " + javacFullLogFileName);
			javacFullLog.flush();
		}
	}
	protected void removeTempClass(String className) {
		File dir = new File(SOURCE_DIRECTORY);
		String[] fileNames = dir.list();
		if (fileNames != null) {
			for (int i = 0, max = fileNames.length; i < max; i++) {
				if (fileNames[i].indexOf(className) != -1) {
					Util.delete(SOURCE_DIRECTORY + File.separator + fileNames[i]);
				}
			}
		}

		dir = new File(EVAL_DIRECTORY);
		fileNames = dir.list();
		if (fileNames != null) {
			for (int i = 0, max = fileNames.length; i < max; i++) {
				if (fileNames[i].indexOf(className) != -1) {
					Util.delete(EVAL_DIRECTORY + File.separator + fileNames[i]);
				}
			}
		}

	}

// WORK replace null logs (no test) by empty string in most situations (more
//      complete coverage) and see what happens
	protected void runConformTest(String[] testFiles) {
		runTest(
	 		// test directory preparation
			true /* flush output directory */,
			testFiles /* test files */,
			// compiler options
			null /* no class libraries */,
			null /* no custom options */,
			false /* do not perform statements recovery */,
			null /* no custom requestor */,
			// compiler results
			false /* expecting no compiler errors */,
			null /* do not check compiler log */,
			// runtime options
			false /* do not force execution */,
			null /* no vm arguments */,
			// runtime results
			null /* do not check output string */,
			null /* do not check error string */,
			// javac options
			JavacTestOptions.DEFAULT /* default javac test options */);
	}

	// WORK replace null logs (no test) by empty string in most situations (more
	//  complete coverage) and see what happens
	protected void runConformTest(String[] testFiles, ASTVisitor visitor) {
		runTest(
	 		// test directory preparation
			true /* flush output directory */,
			testFiles /* test files */,
			new String[] {},
			// compiler options
			null /* no class libraries */,
			false,
			null /* no custom options */,
			false /* do not perform statements recovery */,
			null /* no custom requestor */,
			// compiler results
			false /* expecting no compiler errors */,
			null /* do not check compiler log */,
			null /* no alternate compiler logs */,
			// runtime options
			false /* do not force execution */,
			null /* no vm arguments */,
			// runtime results
			null /* do not check output string */,
			null /* do not check error string */,
			visitor,
			// javac options
			null /* do not check javac output string */,
			JavacTestOptions.DEFAULT /* default javac test options */);
	}

	protected void runConformTest(String[] testFiles, String expectedOutputString) {
		runConformTest(false, JavacTestOptions.DEFAULT, testFiles, expectedOutputString);
	}
	protected void runConformTest(boolean skipJavac, JavacTestOptions javacTestOptions, String[] testFiles, String expectedOutputString) {
		runTest(
			// test directory preparation
			true /* flush output directory */,
			testFiles /* test files */,
			// compiler options
			null /* no class libraries */,
			null /* no custom options */,
			false /* do not perform statements recovery */,
			null /* no custom requestor */,
			// compiler results
			false /* expecting no compiler errors */,
			null /* do not check compiler log */,
			// runtime options
			false /* do not force execution */,
			null /* no vm arguments */,
			// runtime results
			expectedOutputString /* expected output string */,
			null /* do not check error string */,
			// javac options
			skipJavac ? JavacTestOptions.SKIP :
				javacTestOptions != null ? javacTestOptions : JavacTestOptions.DEFAULT /* default javac test options */);
	}
	protected void runConformTest(String[] testFiles, Map<String, String> customOptions) {
		runTest(
			// test directory preparation
			true /* flush output directory */,
			testFiles /* test files */,
			// compiler options
			null /* no class libraries */,
			customOptions /* no custom options */,
			false /* do not perform statements recovery */,
			null /* no custom requestor */,
			// compiler results
			false /* expecting no compiler errors */,
			null /* do not check compiler log */,
			// runtime options
			false /* do not force execution */,
			null /* no vm arguments */,
			// runtime results
			null /* expected output string */,
			null /* do not check error string */,
			// javac options
			JavacTestOptions.DEFAULT /* default javac test options */);
	}
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		runConformTest(testFiles, expectedOutput, customOptions, null);
	}
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions, String[] vmArguments, Charset charset) {
		runTest(
				// test directory preparation
				true /* flush output directory */,
				testFiles /* test files */,
				// compiler options
				null /* no class libraries */,
				customOptions /* no custom options */,
				false /* do not perform statements recovery */,
				null /* no custom requestor */,
				// compiler results
				false /* expecting no compiler errors */,
				null /* do not check compiler log */,
				// runtime options
				false /* do not force execution */,
				vmArguments /* no vm arguments */,
				// runtime results
				expectedOutput /* expected output string */,
				null /* do not check error string */,
				// javac options
				JavacTestOptions.DEFAULT /* default javac test options */,
				charset);
	}
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions, String[] vmArguments, JavacTestOptions javacOptions) {
		runTest(
			// test directory preparation
			true /* flush output directory */,
			testFiles /* test files */,
			// compiler options
			null /* no class libraries */,
			customOptions /* no custom options */,
			false /* do not perform statements recovery */,
			null /* no custom requestor */,
			// compiler results
			false /* expecting no compiler errors */,
			null /* do not check compiler log */,
			// runtime options
			false /* do not force execution */,
			vmArguments /* no vm arguments */,
			// runtime results
			expectedOutput /* expected output string */,
			null /* do not check error string */,
			// javac options
			javacOptions);
	}
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions, String[] vmArguments) {
		runTest(
			// test directory preparation
			true /* flush output directory */,
			testFiles /* test files */,
			// compiler options
			null /* no class libraries */,
			customOptions /* no custom options */,
			false /* do not perform statements recovery */,
			null /* no custom requestor */,
			// compiler results
			false /* expecting no compiler errors */,
			null /* do not check compiler log */,
			// runtime options
			false /* do not force execution */,
			vmArguments /* no vm arguments */,
			// runtime results
			expectedOutput /* expected output string */,
			null /* do not check error string */,
			// javac options
			JavacTestOptions.DEFAULT /* default javac test options */);
	}
	protected void runConformTest(
			String[] testFiles,
			String[] dependantFiles,
			String expectedSuccessOutputString) {
		runTest(
				true,
				testFiles,
				dependantFiles,
				null,
				false,
				null,
				false,
				null,
				false,
				null,
				null,
				false,
				null,
				expectedSuccessOutputString,
				null,
				null,
				expectedSuccessOutputString,
				JavacTestOptions.DEFAULT,
				Charset.defaultCharset());
	}

	protected void runConformTest(
		String[] testFiles,
		String expectedOutputString,
		String[] classLibraries,
		boolean shouldFlushOutputDirectory,
		String[] vmArguments) {
		runTest(
	 		// test directory preparation
			shouldFlushOutputDirectory /* should flush output directory */,
			testFiles /* test files */,
			// compiler options
			classLibraries /* class libraries */,
			null /* no custom options */,
			false /* do not perform statements recovery */,
			null /* no custom requestor */,
			// compiler results
			false /* expecting no compiler errors */,
			null /* do not check compiler log */,
			// runtime options
			false /* do not force execution */,
			vmArguments /* vm arguments */,
			// runtime results
			expectedOutputString /* expected output string */,
			null /* do not check error string */,
			// javac options
			JavacTestOptions.DEFAULT /* default javac test options */);
	}

	protected void runConformTest(
		String[] testFiles,
		String expectedOutputString,
		String[] classLibraries,
		boolean shouldFlushOutputDirectory,
		String[] vmArguments,
		Map<String, String> customOptions,
		ICompilerRequestor customRequestor) {
		runTest(
	 		// test directory preparation
			shouldFlushOutputDirectory /* should flush output directory */,
			testFiles /* test files */,
			// compiler options
			classLibraries /* class libraries */,
			customOptions /* custom options */,
			false /* do not perform statements recovery */,
			customRequestor /* custom requestor */,
			// compiler results
			false /* expecting no compiler errors */,
			null /* do not check compiler log */,
			// runtime options
			false /* do not force execution */,
			vmArguments /* vm arguments */,
			// runtime results
			expectedOutputString /* expected output string */,
			null /* do not check error string */,
			// javac options
			JavacTestOptions.DEFAULT /* default javac test options */);
	}

	// WORK good candidate for elimination (8 instances)
	protected void runConformTest(
		String[] testFiles,
		String expectedSuccessOutputString,
		String[] classLib,
		boolean shouldFlushOutputDirectory,
		String[] vmArguments,
		Map customOptions,
		ICompilerRequestor clientRequestor,
		boolean skipJavac) {
		runConformTest(
				testFiles,
				expectedSuccessOutputString,
				classLib,
				shouldFlushOutputDirectory,
				vmArguments,
				customOptions,
				clientRequestor,
				skipJavac,
				(skipJavac ?
						JavacTestOptions.SKIP :
						JavacTestOptions.DEFAULT));
	}

	protected void runConformTest(
			String[] testFiles,
			String expectedSuccessOutputString,
			String[] classLib,
			boolean shouldFlushOutputDirectory,
			String[] vmArguments,
			Map customOptions,
			ICompilerRequestor clientRequestor,
			boolean skipJavac,
			JavacTestOptions javacTestOptions) {
			runTest(
				shouldFlushOutputDirectory,
				testFiles,
				classLib,
				customOptions,
				false /* do not perform statements recovery */,
				clientRequestor,
				false,
				null,
				false,
				vmArguments,
				expectedSuccessOutputString,
				null,
				javacTestOptions);
	}

	protected static void javacUsePathOption(String option) {
		if (AbstractRegressionTest.javacCompilers != null) {
			for (JavacCompiler compiler : AbstractRegressionTest.javacCompilers) {
				compiler.usePathOption(option);
			}
		}
	}

	/*
	 * Run Sun compilation using javac.
	 * Launch compilation in a thread and verify that it does not take more than 5s
	 * to perform it. Otherwise abort the process and log in console.
	 * TODO (maxime) not sure we really do that 5s cap any more.
	 * A semi verbose output is sent to the console that analyzes differences
	 * of behaviors between javac and Eclipse on a per test basis. A more
	 * verbose output is produced into a file which name is printed on the
	 * console. Such files can be compared between various javac releases
	 * to check potential changes.
	 * To enable such tests, specify the following VM properies in the launch
	 * configuration:
	 * -Drun.javac=enabled
	 *     mandatory - tells the test suite to run javac tests
	 * -Djdk.root=<the root directory of the tested javac>
	 *     optional - enables to find the javac that will be run by the tests
	 *     suite; the root directory must be specified as an absolute path and
	 *     should point to the JDK root, aka /opt/jdk1.5.0_05 for Linux or
	 *     c:/JDK_50 for Windows; in case this property is not specified, the
	 *     tests suite will use the runtime JRE of the launching configuration.
	 * Note that enabling javac tests implies running into 1.5 compliance level
	 * (or higher).
	 */
	protected void runJavac(
			String[] testFiles,
			final String expectedProblemLog,
			final String expectedSuccessOutputString,
			boolean shouldFlushOutputDirectory) {
		String testName = null;
		Process compileProcess = null;
		Process execProcess = null;
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
			StringBuilder cmdLine = new StringBuilder(javacCommandLineHeader);
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
				cmdLine.toString(), env, this.outputTestDirectory);

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

			// Report raw javac results
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
					System.out.println(testName + " - Javac has found error(s) but Eclipse expects conform result:\n");
					javacFullLog.println("JAVAC_MISMATCH: Javac has found error(s) but Eclipse expects conform result");
					System.out.println(errorLogger.buffer.toString());
					printFiles(testFiles);
					DIFF_COUNTERS[0]++;
				}
				else {
					// Javac found no error - may have found warnings
					if (errorLogger.buffer.length() > 0) {
						System.out.println("----------------------------------------");
						System.out.println(testName + " - Javac has found warning(s) but Eclipse expects conform result:\n");
						javacFullLog.println("JAVAC_MISMATCH: Javac has found warning(s) but Eclipse expects conform result");
						System.out.println(errorLogger.buffer.toString());
						printFiles(testFiles);
						DIFF_COUNTERS[0]++;
					}
					if (expectedSuccessOutputString != null && !javacTestErrorFlag) {
						// Neither Eclipse nor Javac found errors, and we have a runtime
						// bench value
						StringBuilder javaCmdLine = new StringBuilder(javaCommandLineHeader);
						javaCmdLine.append(cp);
						javaCmdLine.append(' ').append(testFiles[0].substring(0, testFiles[0].indexOf('.')));
							// assume executable class is name of first test file - PREMATURE check if this is also the case in other test fwk classes
						execProcess = Runtime.getRuntime().exec(javaCmdLine.toString(), env, this.outputTestDirectory);
						Logger logger = new Logger(execProcess.getInputStream(), "");
						// PREMATURE implement consistent error policy
	     				logger.start();
						exitValue = execProcess.waitFor();
						logger.join(); // make sure we get the whole output
						String javaOutput = logger.buffer.toString().trim();
						if (!expectedSuccessOutputString.equals(javaOutput)) {
							System.out.println("----------------------------------------");
							System.out.println(testName + " - Javac and Eclipse runtime output is not the same:");
							javacFullLog.println("JAVAC_MISMATCH: Javac and Eclipse runtime output is not the same");
							dualPrintln("eclipse:");
							dualPrintln(expectedSuccessOutputString);
							dualPrintln("javac:");
							dualPrintln(javaOutput);
							System.out.println("\n");
							printFiles(testFiles); // PREMATURE consider printing files to the log as well
							DIFF_COUNTERS[2]++;
						}
					}
				}
			}
			else {
				// Eclipse found errors or warnings
				if (errorLogger.buffer.length() == 0) {
					System.out.println("----------------------------------------");
					System.out.println(testName + " - Eclipse has found error(s)/warning(s) but Javac did not find any:");
					javacFullLog.println("JAVAC_MISMATCH: Eclipse has found error(s)/warning(s) but Javac did not find any");
					dualPrintln("eclipse:");
					dualPrintln(expectedProblemLog);
					printFiles(testFiles);
					DIFF_COUNTERS[1]++;
				} else if (expectedProblemLog.indexOf("ERROR") > 0 && exitValue == 0){
					System.out.println("----------------------------------------");
					System.out.println(testName + " - Eclipse has found error(s) but Javac only found warning(s):");
					javacFullLog.println("JAVAC_MISMATCH: Eclipse has found error(s) but Javac only found warning(s)");
					dualPrintln("eclipse:");
					dualPrintln(expectedProblemLog);
					System.out.println("javac:");
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
			if (execProcess != null) execProcess.destroy();
			System.out.println(testName+": Sun javac compilation was aborted!");
			javacFullLog.println("JAVAC_WARNING: Sun javac compilation was aborted!");
			e1.printStackTrace(javacFullLog);
		}
		catch (Throwable e) {
			System.out.println(testName+": could not launch Sun javac compilation!");
			e.printStackTrace();
			javacFullLog.println("JAVAC_ERROR: could not launch Sun javac compilation!");
			e.printStackTrace(javacFullLog);
			// PREMATURE failing the javac pass or comparison could also fail
			//           the test itself
		}
		finally {
			// Clean up written file(s)
			Util.delete(this.outputTestDirectory);
		}
	}
	// WORK factorize all runJavac implementations, including overrides
	protected boolean runJavac(String options, String[] testFileNames, String currentDirectoryPath) {
		Process compileProcess = null;
		try {
			// Prepare command line
			StringBuilder cmdLine = new StringBuilder(javacCommandLineHeader);
			cmdLine.append(' ');
			cmdLine.append(options);
			// add source files
			for (int i = 0; i < testFileNames.length; i ++) {
				// *.java is not enough (p1/X.java, p2/Y.java)
				cmdLine.append(' ');
				cmdLine.append(testFileNames[i]);
			}
			// Launch process
			File currentDirectory = new File(currentDirectoryPath);
			compileProcess = Runtime.getRuntime().exec(
				cmdLine.toString(), env, currentDirectory);

			// Log errors
			Logger errorLogger = new Logger(compileProcess.getErrorStream(), "ERROR");
		    errorLogger.start();

		    // Wait for end of process
			int exitValue = compileProcess.waitFor();
			errorLogger.join(); // make sure we get the whole output

			// Check results
			if (exitValue != 0) {
				return false;
			}
			if (errorLogger.buffer.length() > 0) {
				System.err.println("--- javac err: ---");
				System.err.println(errorLogger.buffer.toString());
				return false;
			}
		}
		catch (Throwable e) {
			e.printStackTrace(System.err);
		}
		finally {
			if (compileProcess != null) {
				compileProcess.destroy(); // closes process streams
			}
		}
		return true;
	}
/*
 * Run Sun compilation using one or more versions of javac. Compare the
 * results to expected ones, raising mismatches as needed.
 * To enable such tests, specify the following VM properies in the launch
 * configuration:
 * -Drun.javac=enabled
 *     mandatory - tells the test suite to run javac tests
 * -Djdk.roots=<the root directories of the tested javac(s)>
 *     optional - enables to find the versions of javac that will be run by
 *     the tests suite; the root directories must be specified as a
 *     File.pathSeparator separated list of absolute paths which should
 *     point each to a JDK root, aka /opt/jdk1.5.0_05 for Linux or
 *     c:/JDK_50 for Windows; in case this property is not specified, the
 *     tests suite will use the runtime JRE of the launching configuration.
 * Note that enabling javac tests implies running into 1.5 compliance level
 * (or higher).
 */
// WORK unify use of output, error, log, etc...
protected void runJavac(
		String[] testFiles,
		boolean expectingCompilerErrors,
		String expectedCompilerLog,
		String expectedOutputString,
		String expectedErrorString,
		boolean shouldFlushOutputDirectory,
		JavacTestOptions options,
		String[] vmArguments,
		String[] classLibraries,
		boolean libsOnModulePath) {
	// WORK we're probably doing too much around class libraries in general - java should be able to fetch its own runtime libs
	// WORK reorder parameters
	if (options == JavacTestOptions.SKIP) {
		return;
	}
	if (options == null) {
		options = JavacTestOptions.DEFAULT;
	}
	String newOptions = options.getCompilerOptions();
	if (newOptions.indexOf(" -d ") < 0) {
		newOptions = newOptions.concat(" -d .");
	}
	if (newOptions.indexOf(" -Xlint") < 0) {
		newOptions = newOptions.concat(" -Xlint");
	}
	if (newOptions.indexOf(" -implicit") < 0) {
		newOptions = newOptions.concat(" -implicit:none");
	}
	if (classLibraries != null) {
		List<String> filteredLibs = new ArrayList<>();
		for (String lib : classLibraries) {
			if (!lib.startsWith(jdkRootDirPath.toString())) // this can fail if jdkRootDirPath is a symlink
				filteredLibs.add(lib);
		}
		if (!filteredLibs.isEmpty()) {
			newOptions = newOptions
					.concat(libsOnModulePath ? " --module-path " : " -classpath ")
					.concat(String.join(File.pathSeparator, filteredLibs.toArray(new String[filteredLibs.size()])));
		}
	}
	String testName = testName();
	Iterator<JavacCompiler> compilers = javacCompilers.iterator();
	while (compilers.hasNext()) {
		JavacCompiler compiler = compilers.next();
		if (!options.skip(compiler) && compiler.compliance == this.complianceLevel) {
			// WORK this may exclude some compilers under some conditions (when
			//      complianceLevel is not set); consider accepting the compiler
			//      in such case and see what happens
			JavacTestOptions.Excuse excuse = options.excuseFor(compiler);
			StringBuffer compilerLog = new StringBuffer();
			File javacOutputDirectory = null;
			int mismatch = 0;
			String sourceFileNames[] = null;
			try {
				// cleanup javac output dir if needed
 				javacOutputDirectory = new File(JAVAC_OUTPUT_DIR_NAME +
						File.separator + compiler.rawVersion); // need to change output directory per javac version
				if (shouldFlushOutputDirectory) {
					Util.delete(javacOutputDirectory);
				} else {
					deleteSourceFiles(javacOutputDirectory);
				}
				javacOutputDirectory.mkdirs();
				// write test files
				for (int i = 0, length = testFiles.length; i < length; ) {
					String fileName = testFiles[i++];
					String contents = testFiles[i++];
					fileName = expandFileNameForJavac(fileName);
					File file = new File(javacOutputDirectory, fileName);
					if (fileName.lastIndexOf('/') >= 0) {
						File dir = file.getParentFile();
						if (!dir.exists()) {
							dir.mkdirs();
						}
					}
					Util.writeToFile(contents, file.getPath());
				}
				// compute source file names
				int testFilesLength = testFiles.length;
				sourceFileNames = new String[testFilesLength / 2];
				for (int i = 0, j = 0; i < testFilesLength; i += 2, j++) {
					sourceFileNames[j] = expandFileNameForJavac(testFiles[i]);
				}

				// compile
				long compilerResult = compiler.compile(javacOutputDirectory, newOptions /* options */, sourceFileNames, compilerLog);
				// check cumulative javac results
				// WORK need to use a per compiler approach
				if (! testName.equals(javacTestName)) {
					javacTestName = testName;
					javacTestErrorFlag = false;
				}
				if ((compilerResult & JavacCompiler.EXIT_VALUE_MASK) != 0) {
					javacTestErrorFlag = true;
				}
				// compare compilation results
				if (expectingCompilerErrors) {
					if ((compilerResult & JavacCompiler.EXIT_VALUE_MASK) == 0) {
						if ((compilerResult & JavacCompiler.ERROR_LOG_MASK) == 0) {
							mismatch = JavacTestOptions.MismatchType.EclipseErrorsJavacNone;
						} else {
							mismatch = JavacTestOptions.MismatchType.EclipseErrorsJavacWarnings;
						}
					}
				} else {
					if ((compilerResult & JavacCompiler.EXIT_VALUE_MASK) != 0) {
						if (expectedCompilerLog != null /* null skips warnings test */ && expectedCompilerLog.length() > 0) {
							mismatch = JavacTestOptions.MismatchType.JavacErrorsEclipseWarnings;
						} else {
							mismatch = JavacTestOptions.MismatchType.JavacErrorsEclipseNone;
						}
					} else if (expectedCompilerLog != null /* null skips warnings test */) {
						if (expectedCompilerLog.length() > 0 && (compilerResult & JavacCompiler.ERROR_LOG_MASK) == 0) {
							mismatch = JavacTestOptions.MismatchType.EclipseWarningsJavacNone;
						} else if (expectedCompilerLog.length() == 0 && (compilerResult & JavacCompiler.ERROR_LOG_MASK) != 0) {
							mismatch = JavacTestOptions.MismatchType.JavacWarningsEclipseNone;
						}
					}
				}
			}
			catch (InterruptedException e1) {
				e1.printStackTrace();
				mismatch = JavacTestOptions.MismatchType.JavacAborted;
			}
			catch (Throwable e) {
				e.printStackTrace();
				mismatch = JavacTestOptions.MismatchType.JavacNotLaunched;
			}
			String output = null;
			String err = null;
			try {
				String className = testFiles[0].substring(0, testFiles[0].lastIndexOf('.'));
				if ((expectedOutputString != null || expectedErrorString != null) &&
						!javacTestErrorFlag && mismatch == 0 && sourceFileNames != null &&
						!className.endsWith(PACKAGE_INFO_NAME) && !className.endsWith(MODULE_INFO_NAME)) {
					JavaRuntime runtime = JavaRuntime.fromCurrentVM();
					StringBuffer stderr = new StringBuffer();
					StringBuffer stdout = new StringBuffer();
					String vmOptions = "";
					if (vmArguments != null) {
						int l = vmArguments.length;
						if (l > 0) {
							StringBuffer buffer = new StringBuffer(vmArguments[0]);
							for (int i = 1; i < l; i++) {
								buffer.append(' ');
								buffer.append(vmArguments[i]);
							}
							vmOptions = buffer.toString();
						}
					}
					runtime.execute(javacOutputDirectory, vmOptions, className, stdout, stderr);
					if (expectedOutputString != null /* null skips output test */) {
						output = stdout.toString().trim();
						if (!expectedOutputString.equals(output)) {
							mismatch = JavacTestOptions.MismatchType.StandardOutputMismatch;
						}
					}
					// WORK move to a logic in which if stdout is empty whereas
					//      it should have had contents, stderr is leveraged as
					//      potentially holding indications regarding the failure
					if (expectedErrorString != null /* null skips error test */ && mismatch == 0) {
						err = adjustErrorOutput(stderr.toString().trim());
						if (!errorStringMatch(expectedErrorString, err)) {
							mismatch = JavacTestOptions.MismatchType.ErrorOutputMismatch;
						}
					}
				}
			}
			catch (InterruptedException e1) {
				e1.printStackTrace();
				mismatch = JavacTestOptions.MismatchType.JavaAborted;
			}
			catch (Throwable e) {
				e.printStackTrace();
				mismatch = JavacTestOptions.MismatchType.JavaNotLaunched;
			}
			handleMismatch(compiler, testName, testFiles, expectedCompilerLog, expectedOutputString,
					expectedErrorString, compilerLog, output, err, excuse, mismatch);
		}
	}
}
private void deleteSourceFiles(File directory) {
	try {
		if (!directory.exists())
			return;
		Files.walk(directory.toPath())
			.filter(f -> f.toString().endsWith(SuffixConstants.SUFFIX_STRING_java))
			.map(java.nio.file.Path::toFile)
			.filter(File::isFile)
			.forEach(File::delete);
	} catch (IOException e) {
		e.printStackTrace();
	}
}
private boolean errorStringMatch(String expectedErrorStringStart, String actualError) {
	/*
	 * From TestVerifier.checkBuffers():
	 * This is an opportunistic heuristic for error strings comparison:
	 * - null means skip test;
	 * - empty means exactly empty;
	 * - other means starts with.
	 * If this became insufficient, we could envision using specific
	 * matchers for specific needs.
	 */
	if (expectedErrorStringStart == null)
		return true;
	// special case: command-line java does not like missing main methods ...
	if (actualError.indexOf("java.lang.NoSuchMethodError: main") != -1
			|| actualError.indexOf("Error: Main method not found in class") != -1)
		return true; // ... ignore this.
	if (expectedErrorStringStart.length() == 0)
		return expectedErrorStringStart.equals(actualError);
	if (actualError.startsWith(expectedErrorStringStart))
		return true;
	return false;
}
/** Hook for AbstractRegressionTest9 */
protected String expandFileNameForJavac(String fileName) {
	return fileName;
}
void handleMismatch(JavacCompiler compiler, String testName, String[] testFiles, String expectedCompilerLog,
		String expectedOutputString, String expectedErrorString, StringBuffer compilerLog, String output, String err,
		JavacTestOptions.Excuse excuse, int mismatch) {
	if (mismatch != 0) {
		if (excuse != null && excuse.clears(mismatch)) {
			excuse = null;
		} else {
			System.err.println("----------------------------------------");
			logTestFiles(true, testFiles);
			switch (mismatch) {
				case JavacTestOptions.MismatchType.EclipseErrorsJavacNone:
					assertEquals(testName + " - Eclipse found error(s) but Javac did not find any",
							"", expectedCompilerLog.toString());
					break;
				case JavacTestOptions.MismatchType.EclipseErrorsJavacWarnings:
					assertEquals(testName + " - Eclipse found error(s) but Javac only found warning(s)",
							expectedCompilerLog.toString(),	compilerLog.toString());
					break;
				case JavacTestOptions.MismatchType.JavacErrorsEclipseNone:
					assertEquals(testName + " - Javac found error(s) but Eclipse did not find any",
							"", compilerLog.toString());
					break;
				case JavacTestOptions.MismatchType.JavacErrorsEclipseWarnings:
					assertEquals(testName + " - Javac found error(s) but Eclipse only found warning(s)",
							expectedCompilerLog.toString(),	compilerLog.toString());
					break;
				case JavacTestOptions.MismatchType.EclipseWarningsJavacNone:
					assertEquals(testName + " - Eclipse found warning(s) but Javac did not find any",
							"", expectedCompilerLog.toString());
					break;
				case JavacTestOptions.MismatchType.JavacWarningsEclipseNone:
					assertEquals(testName + " - Javac found warning(s) but Eclipse did not find any",
							"", compilerLog.toString());
					break;
				case JavacTestOptions.MismatchType.StandardOutputMismatch:
					assertEquals(testName + " - Eclipse/Javac standard output mismatch",
							expectedOutputString, output);
					break;
				case JavacTestOptions.MismatchType.ErrorOutputMismatch:
					assertEquals(testName + " - Eclipse/Javac standard error mismatch",
							expectedErrorString, err);
					break;
				case JavacTestOptions.MismatchType.JavacAborted:
				case JavacTestOptions.MismatchType.JavacNotLaunched:
					fail(testName + " - Javac failure");
					break;
				case JavacTestOptions.MismatchType.JavaAborted:
				case JavacTestOptions.MismatchType.JavaNotLaunched:
					fail(testName + " - Java failure");
					break;
				case JavacTestOptions.MismatchType.CompileErrorMismatch:
					fail(testName + " Javac failure did not match \"" + expectedCompilerLog +'\"');
					break;
				default:
					throw new RuntimeException("unexpected mismatch value: " + mismatch);
			}
		}
	}
	if (excuse != null) {
		if (excuse.isIntermittent)
			System.err.println(testName + ": unused execuse (intermittent bug) "+excuse + " for compiler " + compiler);
		else
			fail(testName + ": unused excuse " + excuse + " for compiler " + compiler);
	}
}

private String adjustErrorOutput(String error) {
	// VerifyTests performs an explicit e.printStackTrace() which has slightly different format
	// from a stack trace written directly by a dying JVM (during javac testing), adjust if needed:
	final String excPrefix = "Exception in thread \"main\" ";
	if (error.startsWith(excPrefix))
		return error.substring(excPrefix.length())+'\n';
	return error;
}

//runNegativeTest(
//	// test directory preparation
//	new String[] { /* test files */
// 	},
//	// compiler results
//	"" /* expected compiler log */);
protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
	runNegativeTest(false/*skipJavac*/, null, testFiles, expectedCompilerLog);
}
//runNegativeTest(
// skipJavac
// javacTestOptions
//// test directory preparation
//new String[] { /* test files */
//	},
//// compiler results
//"" /* expected compiler log */);
protected void runNegativeTest(boolean skipJavac, JavacTestOptions javacTestOptions, String[] testFiles, String expectedCompilerLog) {
	runTest(
 		// test directory preparation
		true /* flush output directory */,
		testFiles /* test files */,
		// compiler options
		null /* no class libraries */,
		null /* no custom options */,
		false /* do not perform statements recovery */,
		new Requestor( /* custom requestor */
				false,
				null /* no custom requestor */,
				false,
				false),
		// compiler results
		expectedCompilerLog == null || /* expecting compiler errors */
			expectedCompilerLog.indexOf("ERROR") != -1,
		expectedCompilerLog /* expected compiler log */,
		// runtime options
		false /* do not force execution */,
		null /* no vm arguments */,
		// runtime results
		null /* do not check output string */,
		null /* do not check error string */,
		// javac options
		skipJavac ? JavacTestOptions.SKIP :
			javacTestOptions != null ? javacTestOptions :
		JavacTestOptions.DEFAULT /* default javac test options */);
}
protected void runNegativeTest(String[] testFiles, String expectedCompilerLog, boolean performStatementRecovery) {
	runNegativeTest(false/*skipJavac*/, null, testFiles, expectedCompilerLog, performStatementRecovery);
}
protected void runNegativeTest(boolean skipJavac, JavacTestOptions javacTestOptions, String[] testFiles, String expectedCompilerLog, boolean performStatementRecovery) {
	runTest(
 		// test directory preparation
		true /* flush output directory */,
		testFiles /* test files */,
		// compiler options
		null /* no class libraries */,
		null /* no custom options */,
		performStatementRecovery,
		new Requestor( /* custom requestor */
				false,
				null /* no custom requestor */,
				false,
				false),
		// compiler results
		expectedCompilerLog == null || /* expecting compiler errors */
			expectedCompilerLog.indexOf("ERROR") != -1,
		expectedCompilerLog /* expected compiler log */,
		// runtime options
		false /* do not force execution */,
		null /* no vm arguments */,
		// runtime results
		null /* do not check output string */,
		null /* do not check error string */,
		// javac options
		skipJavac ? JavacTestOptions.SKIP :
			javacTestOptions != null ? javacTestOptions :
		JavacTestOptions.DEFAULT /* default javac test options */);
}
	// WORK potential elimination candidate (24 calls) - else clean up inline
	protected void runNegativeTest(
		String[] testFiles,
		String expectedProblemLog,
		String[] classLib,
		boolean shouldFlushOutputDirectory) {
		runNegativeTest(false, null, testFiles, expectedProblemLog, classLib, shouldFlushOutputDirectory);
	}
	protected void runNegativeTest(
			boolean skipJavac,
			JavacTestOptions javacTestOptions,
			String[] testFiles,
			String expectedProblemLog,
			String[] classLib,
			boolean shouldFlushOutputDirectory) {
			runTest(
				shouldFlushOutputDirectory,
				testFiles,
				classLib,
				null,
				false,
				new Requestor( /* custom requestor */
						false,
						null /* no custom requestor */,
						false,
						false),
				// compiler results
				expectedProblemLog == null || /* expecting compiler errors */
					expectedProblemLog.indexOf("ERROR") != -1,
				expectedProblemLog,
				// runtime options
				false /* do not force execution */,
				null /* no vm arguments */,
				// runtime results
				null /* do not check output string */,
				null /* do not check error string */,
				// javac options
				skipJavac ? JavacTestOptions.SKIP :
					javacTestOptions != null ? javacTestOptions :
						JavacTestOptions.DEFAULT /* javac test options */);
		}
	protected void runNegativeTest(
			String[] testFiles,
			String expectedCompilerLog,
			String[] classLibraries,
			boolean shouldFlushOutputDirectory,
			Map customOptions) {
		runNegativeTest(testFiles, expectedCompilerLog, classLibraries, shouldFlushOutputDirectory, null, customOptions);
	}
	protected void runNegativeTest(
		String[] testFiles,
		String expectedCompilerLog,
		String[] classLibraries,
		boolean shouldFlushOutputDirectory,
		String[] vmArguments,
		Map customOptions) {
		runTest(
	 		// test directory preparation
			shouldFlushOutputDirectory /* should flush output directory */,
			testFiles /* test files */,
			// compiler options
			classLibraries /* class libraries */,
			customOptions /* custom options */,
			false /* do not perform statements recovery */,
			new Requestor( /* custom requestor */
					false,
					null /* no custom requestor */,
					false,
					false),
			// compiler results
			expectedCompilerLog == null || /* expecting compiler errors */
				expectedCompilerLog.indexOf("ERROR") != -1,
			expectedCompilerLog /* expected compiler log */,
			// runtime options
			false /* do not force execution */,
			vmArguments /* no vm arguments */,
			// runtime results
			null /* do not check output string */,
			null /* do not check error string */,
			// javac options
			JavacTestOptions.DEFAULT /* default javac test options */);
	}
	protected void runNegativeTest(
			boolean skipJavac,
			JavacTestOptions javacTestOptions,
			String[] testFiles,
			String expectedCompilerLog,
			String[] classLibraries,
			boolean shouldFlushOutputDirectory,
			Map customOptions) {
			runTest(
		 		// test directory preparation
				shouldFlushOutputDirectory /* should flush output directory */,
				testFiles /* test files */,
				// compiler options
				classLibraries /* class libraries */,
				customOptions /* custom options */,
				false /* do not perform statements recovery */,
				new Requestor( /* custom requestor */
						false,
						null /* no custom requestor */,
						false,
						false),
				// compiler results
				expectedCompilerLog == null || /* expecting compiler errors */
					expectedCompilerLog.indexOf("ERROR") != -1,
				expectedCompilerLog /* expected compiler log */,
				// runtime options
				false /* do not force execution */,
				null /* no vm arguments */,
				// runtime results
				null /* do not check output string */,
				null /* do not check error string */,
				// javac options
				skipJavac ? JavacTestOptions.SKIP :
				javacTestOptions != null ? javacTestOptions :
				JavacTestOptions.DEFAULT /* default javac test options */);
		}
	protected void runNegativeTest(
			String[] testFiles,
			String expectedCompilerLog,
			String[] classLibraries,
			boolean shouldFlushOutputDirectory,
			Map customOptions,
			String expectedErrorString) {
			runNegativeTest(testFiles, expectedCompilerLog, classLibraries,
					shouldFlushOutputDirectory, customOptions, expectedErrorString,
					JavacTestOptions.DEFAULT);
		}
	protected void runNegativeTest(
			String[] testFiles,
			String expectedCompilerLog,
			String[] classLibraries,
			boolean shouldFlushOutputDirectory,
			Map customOptions,
			String expectedErrorString,
			JavacTestOptions javacTestOptions) {
			runTest(
		 		// test directory preparation
				shouldFlushOutputDirectory /* should flush output directory */,
				testFiles /* test files */,
				// compiler options
				classLibraries /* class libraries */,
				customOptions /* custom options */,
				false /* do not perform statements recovery */,
				new Requestor( /* custom requestor */
						false,
						null /* no custom requestor */,
						false,
						false),
				// compiler results
				expectedCompilerLog == null || /* expecting compiler errors */
					expectedCompilerLog.indexOf("ERROR") != -1,
				expectedCompilerLog /* expected compiler log */,
				// runtime options
				false /* do not force execution */,
				null /* no vm arguments */,
				// runtime results
				null /* do not check output string */,
				expectedErrorString /* do not check error string */,
				// javac options
				javacTestOptions /* default javac test options */);
		}
	protected void runNegativeTest(
		String[] testFiles,
		String expectedProblemLog,
		String[] classLibraries,
		boolean shouldFlushOutputDirectory,
		Map customOptions,
		boolean generateOutput,
		boolean showCategory,
		boolean showWarningToken) {
		runTest(
			// test directory preparation
			shouldFlushOutputDirectory /* should flush output directory */,
			testFiles /* test files */,
			// compiler options
			classLibraries /* class libraries */,
			customOptions /* custom options */,
			false,
			new Requestor( /* custom requestor */
					generateOutput,
					null /* no custom requestor */,
					showCategory,
					showWarningToken),
			// compiler results
			expectedProblemLog == null || /* expecting compiler errors */
				expectedProblemLog.indexOf("ERROR") != -1,
			expectedProblemLog,
			// runtime options
			false /* do not force execution */,
			null /* no vm arguments */,
			// runtime results
			null /* do not check output string */,
			null /* do not check error string */,
			// javac options
			JavacTestOptions.DEFAULT /* javac test options */);
	}

	/**
	 * Log contains all problems (warnings+errors)
	 */
	// WORK potential candidate for elimination (19 calls)
	protected void runNegativeTest(
		String[] testFiles,
		String expectedCompilerLog,
		String[] classLibraries,
		boolean shouldFlushOutputDirectory,
		Map customOptions,
		boolean generateOutput,
		boolean showCategory,
		boolean showWarningToken,
		boolean skipJavac,
		boolean performStatementsRecovery) {
		runTest(
	 		// test directory preparation
			shouldFlushOutputDirectory /* should flush output directory */,
			testFiles /* test files */,
			// compiler options
			classLibraries /* class libraries */,
			customOptions /* custom options */,
			performStatementsRecovery /* perform statements recovery */,
			new Requestor( /* custom requestor */
					generateOutput,
					null /* no custom requestor */,
					showCategory,
					showWarningToken),
			// compiler results
			expectedCompilerLog == null || /* expecting compiler errors */
				expectedCompilerLog.indexOf("ERROR") != -1,
			expectedCompilerLog /* expected compiler log */,
			// runtime options
			false /* do not force execution */,
			null /* no vm arguments */,
			// runtime results
			null /* do not check output string */,
			null /* do not check error string */,
			// javac options
			skipJavac ?
					JavacTestOptions.SKIP :
					JavacTestOptions.DEFAULT /* javac test options */);
	}
	protected void runNegativeTest(
			String[] testFiles,
			String expectedCompilerLog,
			String[] classLibraries,
			boolean shouldFlushOutputDirectory,
			Map customOptions,
			boolean generateOutput,
			boolean showCategory,
			boolean showWarningToken,
			boolean skipJavac,
			JavacTestOptions javacOptions,
			boolean performStatementsRecovery) {
			runTest(
		 		// test directory preparation
				shouldFlushOutputDirectory /* should flush output directory */,
				testFiles /* test files */,
				// compiler options
				classLibraries /* class libraries */,
				customOptions /* custom options */,
				performStatementsRecovery /* perform statements recovery */,
				new Requestor( /* custom requestor */
						generateOutput,
						null /* no custom requestor */,
						showCategory,
						showWarningToken),
				// compiler results
				expectedCompilerLog == null || /* expecting compiler errors */
					expectedCompilerLog.indexOf("ERROR") != -1,
				expectedCompilerLog /* expected compiler log */,
				// runtime options
				false /* do not force execution */,
				null /* no vm arguments */,
				// runtime results
				null /* do not check output string */,
				null /* do not check error string */,
				// javac options
				skipJavac ?
						JavacTestOptions.SKIP :
						 javacOptions/* javac test options */);
		}
	public void runTest(
			String[] testFiles,
			boolean expectingCompilerErrors,
			String expectedCompilerLog,
			String expectedOutputString,
			String expectedErrorString,
			boolean forceExecution,
			String[] classLibraries,
			boolean shouldFlushOutputDirectory,
			String[] vmArguments,
			Map customOptions,
			ICompilerRequestor customRequestor,
			boolean skipJavac) {
		runTest(
	 		// test directory preparation
			shouldFlushOutputDirectory /* should flush output directory */,
			testFiles /* test files */,
			// compiler options
			classLibraries /* class libraries */,
			customOptions /* custom options */,
			false /* do not perform statements recovery */,
			customRequestor /* custom requestor */,
			// compiler results
			expectingCompilerErrors /* expecting compiler errors */,
			expectedCompilerLog /* expected compiler log */,
			// runtime options
			forceExecution /* force execution */,
			vmArguments /* vm arguments */,
			// runtime results
			expectedOutputString /* expected output string */,
			expectedErrorString /* expected error string */,
			// javac options
			skipJavac ?
				JavacTestOptions.SKIP :
				JavacTestOptions.DEFAULT /* javac test options */);

	}

	// WORK get this out
	protected void runTest(
			String[] testFiles,
			boolean expectingCompilerErrors,
			String expectedCompilerLog,
			String expectedOutputString,
			String expectedErrorString,
			boolean forceExecution,
			String[] classLibraries,
			boolean shouldFlushOutputDirectory,
			String[] vmArguments,
			Map customOptions,
			ICompilerRequestor clientRequestor,
			JavacTestOptions javacTestOptions) {
		runTest(
	 		// test directory preparation
			shouldFlushOutputDirectory /* should flush output directory */,
			testFiles /* test files */,
			// compiler options
			classLibraries /* class libraries */,
			customOptions /* custom options */,
			false /* do not perform statements recovery */,
			clientRequestor /* custom requestor */,
			// compiler results
			expectingCompilerErrors /* expecting compiler errors */,
			expectedCompilerLog /* expected compiler log */,
			// runtime options
			forceExecution /* force execution */,
			vmArguments /* vm arguments */,
			// runtime results
			expectedOutputString /* expected output string */,
			expectedErrorString /* expected error string */,
			// javac options
			javacTestOptions /* javac test options */);
	}
	protected void runTest(
			// test directory preparation
			boolean shouldFlushOutputDirectory,
			String[] testFiles,
			// compiler options
			String[] classLibraries,
			Map<String, String> customOptions,
			boolean performStatementsRecovery,
			ICompilerRequestor customRequestor,
			// compiler results
			boolean expectingCompilerErrors,
			String expectedCompilerLog,
			// runtime options
			boolean forceExecution,
			String[] vmArguments,
			// runtime results
			String expectedOutputString,
			String expectedErrorString,
			// javac options
			JavacTestOptions javacTestOptions) {
		runTest(
			shouldFlushOutputDirectory,
			testFiles,
			new String[] {},
			classLibraries,
			false,
			customOptions,
			performStatementsRecovery,
			customRequestor,
			expectingCompilerErrors,
			expectedCompilerLog,
			null, // alternate compile errors
			forceExecution,
			vmArguments,
			expectedOutputString,
			expectedErrorString,
			null,
			expectedOutputString,
			javacTestOptions,
			Charset.defaultCharset());
	}
	private void runTest(
			// test directory preparation
			boolean shouldFlushOutputDirectory,
			String[] testFiles,
			// compiler options
			String[] classLibraries,
			Map<String, String> customOptions,
			boolean performStatementsRecovery,
			ICompilerRequestor customRequestor,
			// compiler results
			boolean expectingCompilerErrors,
			String expectedCompilerLog,
			// runtime options
			boolean forceExecution,
			String[] vmArguments,
			// runtime results
			String expectedOutputString,
			String expectedErrorString,
			// javac options
			JavacTestOptions javacTestOptions,
			Charset charset) {
		runTest(
			shouldFlushOutputDirectory,
			testFiles,
			new String[] {},
			classLibraries,
			false,
			customOptions,
			performStatementsRecovery,
			customRequestor,
			expectingCompilerErrors,
			expectedCompilerLog,
			null, // alternate compile errors
			forceExecution,
			vmArguments,
			expectedOutputString,
			expectedErrorString,
			null,
			expectedOutputString,
			javacTestOptions,
			charset);
	}
	/** Call this if the compiler randomly produces different error logs. */
	protected void runNegativeTestMultiResult(String[] testFiles, Map options, String[] alternateCompilerErrorLogs) {
		runTest(
			false,
			testFiles,
			new String[] {},
			null,
			false,
			options,
			false,
			new Requestor( /* custom requestor */
					false,
					null /* no custom requestor */,
					false,
					false),
			true,
			null,
			alternateCompilerErrorLogs,
			false,
			null,
			null,
			null,
			null,
			null,
			JavacTestOptions.DEFAULT,
			Charset.defaultCharset());
	}
	private void runTest(
			// test directory preparation
			boolean shouldFlushOutputDirectory,
			String[] testFiles,
			String[] dependantFiles,
			// compiler options
			String[] classLibraries,
			boolean libsOnModulePath,
			Map<String, String> customOptions,
			boolean performStatementsRecovery,
			ICompilerRequestor customRequestor,
			// compiler results
			boolean expectingCompilerErrors,
			String expectedCompilerLog,
			String[] alternateCompilerLogs,
			// runtime options
			boolean forceExecution,
			String[] vmArguments,
			// runtime results
			String expectedOutputString,
			String expectedErrorString,
			final ASTVisitor visitor,
			// javac options
			String expectedJavacOutputString,
			JavacTestOptions javacTestOptions) {
		runTest( shouldFlushOutputDirectory,
				testFiles,
				dependantFiles,
				classLibraries,
				libsOnModulePath,
				customOptions,
				performStatementsRecovery,
				customRequestor,
				expectingCompilerErrors,
				expectedCompilerLog,
				alternateCompilerLogs,
				forceExecution,
				vmArguments,
				expectedOutputString,
				expectedErrorString,
				visitor,
				expectedJavacOutputString,
				javacTestOptions,
				Charset.defaultCharset());
	}
// This is a worker method to support regression tests. To ease policy changes,
// it should not be called directly, but through the runConformTest and
// runNegativeTest series.
// calling templates:
//	runTest(
//	 		// test directory preparation
//			true /* flush output directory */,
//			false /* do not flush output directory */,
//			shouldFlushOutputDirectory /* should flush output directory */,
//
//			new String[] { /* test files */
//			},
//			null /* no test files */,
//			testFiles /* test files */,
//
//			// compiler options
//			null /* no class libraries */,
//			new String[] { /* class libraries */
//			},
//			classLibraries /* class libraries */,
//
//			null /* no custom options */,
//			customOptions /* custom options */,
//
//			true /* perform statements recovery */,
//			false /* do not perform statements recovery */,
//			performStatementsRecovery /* perform statements recovery */,
//
//			null /* no custom requestor */,
//			customRequestor /* custom requestor */,
//
//			// compiler results
//			false /* expecting no compiler errors */,
//			true /* expecting compiler errors */,
//			expectingCompilerErrors /* expecting compiler errors */,
//
//			null /* do not check compiler log */,
//			"" /* expected compiler log */,
//			expectedCompilerLog /* expected compiler log */,
//
//			// runtime options
//			false /* do not force execution */,
//			true /* force execution */,
//			forceExecution /* force execution */,
//
//			null /* no vm arguments */,
//			new String[] { /* vm arguments */
//			},
//			vmArguments /* vm arguments */,
//
//			// runtime results
//			null /* do not check output string */,
//			"" /* expected output string */,
//			expectedOutputString /* expected output string */,
//
//			null /* do not check error string */,
//			"" /* expected error string */,
//			expectedErrorString /* expected error string */,
//
//			// javac options
//			JavacTestOptions.SKIP /* skip javac tests */);
//			JavacTestOptions.DEFAULT /* default javac test options */);
//			javacTestOptions /* javac test options */);
// TODO Maxime future work:
// - reduce the number of tests that implicitly skip parts like logs
//   comparisons; while this is due to eat up more time, we will gain in
//   coverage (and detection of unwanted changes); of course, this will tend
//   to 'over constrain' some tests, but a reasonable approach would be to
//   unable the comparison for tests which just happen to be fine;
// - check the callees statistics for wrapper methods and tune them accordingly
//   (aka, suppress low profile ones).
// WORK log test files in all failure cases (ez cut and paste)
	private void runTest(
			// test directory preparation
			boolean shouldFlushOutputDirectory,
			String[] testFiles,
			String[] dependantFiles,
			// compiler options
			String[] classLibraries,
			boolean libsOnModulePath,
			Map<String, String> customOptions,
			boolean performStatementsRecovery,
			ICompilerRequestor customRequestor,
			// compiler results
			boolean expectingCompilerErrors,
			String expectedCompilerLog,
			String[] alternateCompilerLogs,
			// runtime options
			boolean forceExecution,
			String[] vmArguments,
			// runtime results
			String expectedOutputString,
			String expectedErrorString,
			final ASTVisitor visitor,
			// javac options
			String expectedJavacOutputString,
			JavacTestOptions javacTestOptions,
			Charset charset) {
		// non-javac part
		if (shouldFlushOutputDirectory)
			Util.flushDirectoryContent(new File(OUTPUT_DIR));
		if (testFiles != null && (testFiles.length % 2) != 0) {
			fail("odd number of strings in testFiles");
		}

		Requestor requestor =
			customRequestor instanceof Requestor ?
					(Requestor) customRequestor :
					new Requestor(
						forceExecution,
						customRequestor,
						false, /* show category */
						false /* show warning token*/);
		requestor.outputPath = OUTPUT_DIR.endsWith(File.separator) ? OUTPUT_DIR : OUTPUT_DIR + File.separator;
				// WORK should not have to test a constant?

		Map<String, String> options = getCompilerOptions();
		if (customOptions != null) {
			options.putAll(customOptions);
		}
		CompilerOptions compilerOptions = new CompilerOptions(options);
		compilerOptions.performMethodsFullRecovery = performStatementsRecovery;
		compilerOptions.performStatementsRecovery = performStatementsRecovery;
		INameEnvironment nameEnvironment = getNameEnvironment(dependantFiles, classLibraries, options);
		Compiler batchCompiler =
			new Compiler(
				nameEnvironment,
				getErrorHandlingPolicy(),
				compilerOptions,
				requestor,
				getProblemFactory()) {
				public void process(org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration unit, int i) {
					super.process(unit, i);
					if (visitor != null) {
						unit.traverse(visitor, unit.scope);
					}
				}
			};
		/* GROOVY edit
		if (this.enableAPT) {
			batchCompiler.annotationProcessorManager = getAnnotationProcessorManager(batchCompiler);
		}
		*/
		compilerOptions.produceReferenceInfo = true;
		Throwable exception = null;
		try {
			batchCompiler.compile(getCompilationUnits(testFiles)); // compile all files together
		} catch(RuntimeException e){
			exception = e;
			throw e;
		} catch(Error e) {
			exception = e;
			throw e;
		} finally {
			nameEnvironment.cleanup();
			String[] alternatePlatformIndepentLogs = null;
			if (expectedCompilerLog != null) {
				alternatePlatformIndepentLogs = new String[] {Util.convertToIndependantLineDelimiter(expectedCompilerLog)};
			} else if (alternateCompilerLogs != null) {
				alternatePlatformIndepentLogs = new String[alternateCompilerLogs.length];
				for (int i = 0; i < alternateCompilerLogs.length; i++)
					alternatePlatformIndepentLogs[i] = Util.convertToIndependantLineDelimiter(alternateCompilerLogs[i]);
			}
			if (alternatePlatformIndepentLogs != null) {
				checkCompilerLog(testFiles, requestor, alternatePlatformIndepentLogs, exception);
			}
			if (exception == null) {
				if (expectingCompilerErrors) {
					if (!requestor.hasErrors) {
						logTestFiles(true, testFiles);
						fail("Unexpected success");
					}
				} else if (requestor.hasErrors) {
					if (!"".equals(requestor.problemLog)) {
						logTestFiles(true, testFiles);
						System.out.println("Copy-paste compiler log:");
						System.out.println(Util.displayString(Util.convertToIndependantLineDelimiter(requestor.problemLog.toString()), INDENT, SHIFT));
						assertEquals("Unexpected failure", "", requestor.problemLog);
					}
				}
			}
		}
		if (!requestor.hasErrors || forceExecution) {
			String sourceFile = testFiles[0];

			// Compute class name by removing ".java" and replacing slashes with dots
			String className = sourceFile.substring(0, sourceFile.lastIndexOf('.')).replace('/', '.').replace('\\', '.');
			if (!className.endsWith(PACKAGE_INFO_NAME) && !className.endsWith(MODULE_INFO_NAME)) {

				if (vmArguments != null) {
					if (this.verifier != null) {
						this.verifier.shutDown();
					}
					this.verifier = new TestVerifier(false);
					this.createdVerifier = true;
				}
				boolean passed =
					this.verifier.verifyClassFiles(
						sourceFile,
						className,
						expectedOutputString,
						expectedErrorString,
						this.classpaths,
						null,
						vmArguments);
				if (!passed) {
					System.out.println(getClass().getName() + '#' + getName());
					String execErrorString = this.verifier.getExecutionError();
					if (execErrorString != null && execErrorString.length() > 0) {
						System.out.println("[ERR]:"+execErrorString); //$NON-NLS-1$
					}
					String execOutputString = this.verifier.getExecutionOutput();
					if (execOutputString != null && execOutputString.length() > 0) {
						System.out.println("[OUT]:"+execOutputString); //$NON-NLS-1$
					}
					logTestFiles(false, testFiles);
					assertEquals(this.verifier.failureReason, expectedErrorString == null ? "" : expectedErrorString, execErrorString);
					assertEquals(this.verifier.failureReason, expectedOutputString == null ? "" : expectedOutputString, execOutputString);
				}
				assertTrue(this.verifier.failureReason, // computed by verifyClassFiles(...) action
						passed);
				if (vmArguments != null) {
					if (this.verifier != null) {
						this.verifier.shutDown();
					}
					this.verifier = new TestVerifier(false);
					this.createdVerifier = true;
				}
			}
		}
		// javac part
		if (RUN_JAVAC && javacTestOptions != JavacTestOptions.SKIP) {
			runJavac(testFiles, expectingCompilerErrors, expectedCompilerLog,
					expectedJavacOutputString, expectedErrorString, shouldFlushOutputDirectory,
					javacTestOptions, vmArguments, classLibraries, libsOnModulePath);
		}
	}
	/* GROOVY edit
	class DummyAnnotationProcessingManager extends BaseAnnotationProcessorManager {

		ProcessorInfo processorInfo = null;
		public ProcessorInfo discoverNextProcessor() {
			ProcessorInfo temp = this.processorInfo;
			this.processorInfo = null;
			return temp;
		}

		public void reportProcessorException(Processor p, Exception e) {
			throw new AbortCompilation(null, e);
		}

		@Override
		public void setProcessors(Object[] processors) {
			// Nothing to do here
		}

		@Override
		public void configure(Object batchCompiler, String[] options) {
			this._processingEnv = new DummyEnvironmentImpl((Compiler) batchCompiler);
		}
		public void processAnnotations(CompilationUnitDeclaration[] units, ReferenceBinding[] referenceBindings, boolean isLastRound) {
			if (this.processorInfo == null) {
				this.processorInfo = new ProcessorInfo(new DummyProcessor());
			}
			super.processAnnotations(units, referenceBindings, isLastRound);
		}

		@Override
		public void configureFromPlatform(Compiler compiler, Object compilationUnitLocator, Object javaProject, boolean isTestCode) {
			// Nothing to do here
		}
		@SupportedAnnotationTypes("*")
		class DummyProcessor extends AbstractProcessor {
			@Override
			public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
				return true;
			}
		}

		class DummyEnvironmentImpl extends BaseProcessingEnvImpl {
			public DummyEnvironmentImpl(Compiler compiler) {
				this._compiler = compiler;
			}
			@Override
			public Locale getLocale() {
				return Locale.getDefault();
			}
		}
	}

	protected AbstractAnnotationProcessorManager getAnnotationProcessorManager(Compiler compiler) {
		try {
			AbstractAnnotationProcessorManager annotationManager = new DummyAnnotationProcessingManager();
			annotationManager.configure(compiler, new String[0]);
			annotationManager.setErr(new PrintWriter(System.err));
			annotationManager.setOut(new PrintWriter(System.out));
			return annotationManager;
		} catch(UnsupportedClassVersionError e) {
			System.err.println(e);
		}
		return null;
	}
	*/
public void runConformTest(
	// test directory preparation
	String[] testFiles,
	// javac options
	JavacTestOptions javacTestOptions) {
runTest(
	// test directory preparation
	true /* flush output directory */,
	testFiles /* test files */,
	// compiler options
	null /* no class libraries */,
	null /* no custom options */,
	false /* do not perform statements recovery */,
	null /* no custom requestor */,
	// compiler results
	false /* expecting no compiler errors */,
	"" /* expected compiler log */,
	// runtime options
	false /* do not force execution */,
	null /* no vm arguments */,
	// runtime results
	"" /* expected output string */,
	"" /* expected error string */,
	// javac options
	javacTestOptions /* javac test options */);
}
//	runConformTest(
//		// test directory preparation
//		true /* flush output directory */,
//		false /* do not flush output directory */,
//		shouldFlushOutputDirectory /* should flush output directory */,
//
//		new String[] { /* test files */
//		},
//		null /* no test files */,
//		testFiles /* test files */,
//
//		// compiler results
//		null /* do not check compiler log */,
//		"" /* expected compiler log */,
//		expectedCompilerLog /* expected compiler log */,
//
//		// runtime results
//		null /* do not check output string */,
//		"" /* expected output string */,
//		expectedOutputString /* expected output string */,
//
//		null /* do not check error string */,
//		"" /* expected error string */,
//		expectedErrorString /* expected error string */,
//
//		// javac options
//		JavacTestOptions.SKIP /* skip javac tests */);
//		JavacTestOptions.DEFAULT /* default javac test options */);
//		javacTestOptions /* javac test options */);
protected void runConformTest(
		// test directory preparation
		boolean shouldFlushOutputDirectory,
		String[] testFiles,
		// compiler results
		String expectedCompilerLog,
		// runtime results
		String expectedOutputString,
		String expectedErrorString,
		// javac options
		JavacTestOptions javacTestOptions) {
	runTest(
		// test directory preparation
		shouldFlushOutputDirectory /* should flush output directory */,
		testFiles /* test files */,
		// compiler options
		null /* no class libraries */,
		null /* no custom options */,
		false /* do not perform statements recovery */,
		null /* no custom requestor */,
		// compiler results
		false /* expecting no compiler errors */,
		expectedCompilerLog /* expected compiler log */,
		// runtime options
		false /* do not force execution */,
		null /* no vm arguments */,
		// runtime results
		expectedOutputString /* expected output string */,
		expectedErrorString /* expected error string */,
		// javac options
		javacTestOptions /* javac test options */);
}
//	runConformTest(
//		// test directory preparation
//		true /* flush output directory */,
//		false /* do not flush output directory */,
//
//		new String[] { /* test files */
//		},
//		null /* no test files */,
//
//		// compiler options
//		null /* no class libraries */,
//		new String[] { /* class libraries */
//		},
//
//		null /* no custom options */,
//		customOptions /* custom options */,
//
//		// compiler results
//		null /* do not check compiler log */,
//		"----------\n" +  /* expected compiler log */
//		"" /* expected compiler log */,
//
//		// runtime results
//		null /* do not check output string */,
//		"" /* expected output string */,
//		expectedOutputString /* expected output string */,
//
//		null /* do not check error string */,
//		"" /* expected error string */,
//		expectedErrorString /* expected error string */,
//
//		// javac options
//		JavacTestOptions.SKIP /* skip javac tests */);
//		JavacTestOptions.DEFAULT /* default javac test options */);
//		javacTestOptions /* javac test options */);
protected void runConformTest(
		// test directory preparation
		boolean shouldFlushOutputDirectory,
		String[] testFiles,
		//compiler options
		String[] classLibraries /* class libraries */,
		Map customOptions /* custom options */,
		// compiler results
		String expectedCompilerLog,
		// runtime results
		String expectedOutputString,
		String expectedErrorString,
		// javac options
		JavacTestOptions javacTestOptions) {
	runTest(
		// test directory preparation
		shouldFlushOutputDirectory /* should flush output directory */,
		testFiles /* test files */,
		// compiler options
		classLibraries /* class libraries */,
		customOptions /* custom options */,
		false /* do not perform statements recovery */,
		null /* no custom requestor */,
		// compiler results
		false /* expecting no compiler errors */,
		expectedCompilerLog /* expected compiler log */,
		// runtime options
		false /* do not force execution */,
		null /* no vm arguments */,
		// runtime results
		expectedOutputString /* expected output string */,
		expectedErrorString /* expected error string */,
		// javac options
		javacTestOptions /* javac test options */);
}
//	runNegativeTest(
//		// test directory preparation
//		new String[] { /* test files */
//		},
//		null /* no test files */,
//		// compiler results
//		"----------\n" + /* expected compiler log */
//		// javac options
//		JavacTestOptions.SKIP /* skip javac tests */);
//		JavacTestOptions.DEFAULT /* default javac test options */);
//		javacTestOptions /* javac test options */);
protected void runNegativeTest(
		// test directory preparation
		String[] testFiles,
		// compiler results
		String expectedCompilerLog,
		// javac options
		JavacTestOptions javacTestOptions) {
	runTest(
 		// test directory preparation
		true /* flush output directory */,
		testFiles /* test files */,
		// compiler options
		null /* no class libraries */,
		null /* no custom options */,
		false /* do not perform statements recovery */,
		null /* no custom requestor */,
		// compiler results
		true /* expecting compiler errors */,
		expectedCompilerLog /* expected compiler log */,
		// runtime options
		false /* do not force execution */,
		null /* no vm arguments */,
		// runtime results
		null /* do not check output string */,
		null /* do not check error string */,
		// javac options
		javacTestOptions /* javac test options */);
}
protected void runNegativeTest(
// test directory preparation
String[] testFiles,
String[] dependentFiles,
// compiler results
String expectedCompilerLog,
// javac options
JavacTestOptions javacTestOptions) {
	runTest(
			// test directory preparation
		true /* flush output directory */,
		testFiles /* test files */,
		dependentFiles,
		// compiler options
		null /* no class libraries */,
		false,
		null /* no custom options */,
		false /* do not perform statements recovery */,
		null /* no custom requestor */,
		// compiler results
		true /* expecting compiler errors */,
		expectedCompilerLog /* expected compiler log */,
		// runtime options
		null,
		false /* do not force execution */,
		null /* no vm arguments */,
		// runtime results
		null /* do not check output string */,
		null /* do not check error string */,
		null,
		null,
		// javac options
		javacTestOptions /* javac test options */,
		Charset.defaultCharset());
}
protected void runNegativeTest(
		// test directory preparation
		boolean shouldFlushOutputDirectory,
		String[] testFiles,
		// compiler options
		String[] classLibraries,
		Map customOptions,
		// compiler results
		String expectedCompilerLog,
		// javac options
		JavacTestOptions javacTestOptions) {
	runTest(
		// test directory preparation
		shouldFlushOutputDirectory /* should flush output directory */,
		testFiles /* test files */,
		// compiler options
		classLibraries /* class libraries */,
		customOptions /* custom options */,
		false /* do not perform statements recovery */,
		null /* no custom requestor */,
		// compiler results
		expectedCompilerLog == null || /* expecting compiler errors */
		expectedCompilerLog.indexOf("ERROR") != -1,
		expectedCompilerLog /* expected compiler log */,
		// runtime options
		false /* do not force execution */,
		null /* no vm arguments */,
		// runtime results
		null /* do not check output string */,
		null /* do not check error string */,
		// javac options
		javacTestOptions /* javac test options */);
}
//  runNegativeTest(
//    // test directory preparation
//    true /* flush output directory */,
//    false /* do not flush output directory */,
//    shouldFlushOutputDirectory /* should flush output directory */,
//
//    new String[] { /* test files */
//    },
//    null /* no test files */,
//
//    // compiler options
//    null /* no class libraries */,
//    new String[] { /* class libraries */
//    },
//    classLibraries /* class libraries */,
//
//    null /* no custom options */,
//    customOptions /* custom options */,
//
//    // compiler results
//    null /* do not check compiler log */,
//    "----------\n" + /* expected compiler log */
//
//    //runtime results
//    null /* do not check output string */,
//    "" /* expected output string */,
//    expectedOutputString /* expected output string */,
//
//    null /* do not check error string */,
//    "" /* expected error string */,
//    expectedErrorString /* expected error string */,
//
//    // javac options
//    JavacTestOptions.SKIP /* skip javac tests */);
//    JavacTestOptions.DEFAULT /* default javac test options */);
//    javacTestOptions /* javac test options */);
protected void runNegativeTest(
	// test directory preparation
	boolean shouldFlushOutputDirectory,
	String[] testFiles,
	// compiler options
	String[] classLibraries,
	Map customOptions,
	// compiler results
	String expectedCompilerLog,
	// runtime results
	String expectedOutputString,
	String expectedErrorString,
	// javac options
	JavacTestOptions javacTestOptions) {
	runTest(
		// test directory preparation
		shouldFlushOutputDirectory /* should flush output directory */,
		testFiles /* test files */,
		// compiler options
		classLibraries /* class libraries */,
		customOptions /* custom options */,
		false /* do not perform statements recovery */,
		null /* no custom requestor */,
		// compiler results
		true /* expecting compiler errors */,
		expectedCompilerLog /* expected compiler log */,
		// runtime options
		true /* force execution */,
		null /* no vm arguments */,
		// runtime results
		expectedOutputString /* expected output string */,
		expectedErrorString /* expected error string */,
		// javac options
		javacTestOptions /* javac test options */);
}
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (this.verifier == null) {
			this.verifier = new TestVerifier(true);
			this.createdVerifier = true;
		}
		if (RUN_JAVAC) {
			// WORK make all needed inits once and for all
			if (isFirst()) {
				if (javacFullLog == null) {
					// One time initialization of javac related concerns
					// compute command lines and extract javac version
					JAVAC_OUTPUT_DIR = new File(JAVAC_OUTPUT_DIR_NAME);
					// WORK simplify jdk.root out
					String jdkRootDirectory = System.getProperty("jdk.root");
					if (jdkRootDirectory == null)
						jdkRootDirPath = (new Path(Util.getJREDirectory())).removeLastSegments(1);
					else
						jdkRootDirPath = new Path(jdkRootDirectory);

					StringBuilder cmdLineHeader = new StringBuilder(jdkRootDirPath.
							append("bin").append(JAVA_NAME).toString()); // PREMATURE replace JAVA_NAME and JAVAC_NAME with locals? depends on potential reuse
					javaCommandLineHeader = cmdLineHeader.toString();
					cmdLineHeader = new StringBuilder(jdkRootDirPath.
							append("bin").append(JAVAC_NAME).toString());
					cmdLineHeader.append(" -classpath . ");
					  // start with the current directory which contains the source files
					String version = JavacCompiler.getVersion(cmdLineHeader.toString());
					cmdLineHeader.append(" -d ");
					cmdLineHeader.append(JAVAC_OUTPUT_DIR_NAME.indexOf(" ") != -1 ? "\"" + JAVAC_OUTPUT_DIR_NAME + "\"" : JAVAC_OUTPUT_DIR_NAME);
					cmdLineHeader.append(" -source 1.5 -deprecation -Xlint "); // enable recommended warnings
					// WORK new javac system does not do that... reconsider
					// REVIEW consider enabling all warnings instead? Philippe does not see
					//        this as ez to use (too many changes in logs)
					javacCommandLineHeader = cmdLineHeader.toString();
					new File(Util.getOutputDirectory()).mkdirs();
					// TODO maxime check why this happens to miss in some cases
					// WORK if we keep a full log, it should not mix javac versions...
					javacFullLogFileName = Util.getOutputDirectory() +	File.separatorChar +
                    							version.replace(' ', '_') + "_" +
                    					    (new SimpleDateFormat("yyyyMMdd_HHmmss")).format(new Date()) +
                    					    ".txt";
					javacFullLog =
					  	new PrintWriter(new FileOutputStream(javacFullLogFileName)); // static that is initialized once, closed at process end
					javacFullLog.println(version); // so that the contents is self sufficient
					System.out.println("***************************************************************************");
					System.out.println("* Sun Javac compiler output archived into file:");
					System.out.println("* " + javacFullLogFileName);
					System.out.println("***************************************************************************");
					javacCompilers = new ArrayList<>();
					String jdkRoots = System.getProperty("jdk.roots");
					if (jdkRoots == null) {
						javacCompilers.add(new JavacCompiler(jdkRootDirPath.toString()));
					} else {
						StringTokenizer tokenizer = new StringTokenizer(jdkRoots, File.pathSeparator);
						while (tokenizer.hasMoreTokens()) {
							javacCompilers.add(new JavacCompiler(tokenizer.nextToken()));
						}
					}
				}
				// per class initialization
				CURRENT_CLASS_NAME = getClass().getName();
				dualPrintln("***************************************************************************");
				System.out.print("* Comparison with Sun Javac compiler for class ");
				dualPrintln(CURRENT_CLASS_NAME.substring(CURRENT_CLASS_NAME.lastIndexOf('.')+1) +
						" (" + TESTS_COUNTERS.get(CURRENT_CLASS_NAME) + " tests)");
				System.out.println("***************************************************************************");
				DIFF_COUNTERS[0] = 0;
				DIFF_COUNTERS[1] = 0;
				DIFF_COUNTERS[2] = 0;
			}
		}
	}

	@Override
	public void stop() {
		this.verifier.shutDown();
	}

	@Override
	protected void tearDown() throws Exception {
		if (this.createdVerifier) {
			stop();
		}
		// clean up output dir
		File outputDir = new File(OUTPUT_DIR);
		if (outputDir.exists()) {
			Util.flushDirectoryContent(outputDir);
		}
		File libDir = new File(LIB_DIR);
		if (libDir.exists()) {
			Util.flushDirectoryContent(libDir);
		}
		super.tearDown();
		if (RUN_JAVAC) {
			if (JAVAC_OUTPUT_DIR.exists()) {
				Util.flushDirectoryContent(JAVAC_OUTPUT_DIR);
			}
			printJavacResultsSummary();
		}
	}
	/**
	 * Returns the OS path to the directory that contains this plugin.
	 */
	protected String getCompilerTestsPluginDirectoryPath() {
		try {
			URL platformURL = Platform.getBundle("org.eclipse.jdt.core.tests.compiler").getEntry("/");
			return new File(FileLocator.toFileURL(platformURL).getFile()).getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	protected Map<String, String> setPresetPreviewOptions() {
		Map<String, String> options = getCompilerOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.latestSupportedJavaVersion());
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.latestSupportedJavaVersion());
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.latestSupportedJavaVersion());
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		return options;
	}
}
