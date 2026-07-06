/*******************************************************************************
 * Copyright (c) 2024 Salesforce and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Salesforce - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.junit.Assert;

public class NameEnvironmentAnswerListenerTest extends AbstractComparableTest {

	public NameEnvironmentAnswerListenerTest(String name) {
		super(name);
	}

	/**
	 * Extension of ECJ Batch compiler to allow pre-configuration as well as registration of listener.
	 * <p>
	 * This is modeled after real-world use in Bazel ECJ Toolchain.
	 * </p>
	 * @see <a href="https://github.com/salesforce/bazel-jdt-java-toolchain/blob/04c27501b9623300ff38f500cd53af8ce0a11835/compiler/src/main/buildjar/com/google/devtools/build/buildjar/javac/BlazeEcjMain.java#L98">BlazeEcjMain.java</a>
	 */
	static class EclipseBatchCompiler extends Main {

		Set<String> answeredFileNames = new LinkedHashSet<>();

		public EclipseBatchCompiler(PrintWriter errAndOutWriter) {
			super(errAndOutWriter, errAndOutWriter, false /* systemExitWhenFinished */, null /* customDefaultOptions */,
					null /* compilationProgress */);

			setSeverity(CompilerOptions.OPTION_ReportForbiddenReference, ProblemSeverities.Error, true);
			setSeverity(CompilerOptions.OPTION_ReportDiscouragedReference, ProblemSeverities.Error, true);
		}

		@Override
		public FileSystem getLibraryAccess() {
			// we use this to collect information about all used dependencies during
			// compilation
			FileSystem nameEnvironment = super.getLibraryAccess();
			nameEnvironment.setNameEnvironmentAnswerListener(this::recordNameEnvironmentAnswer);
			return nameEnvironment;
		}

		protected void recordNameEnvironmentAnswer(NameEnvironmentAnswer answer) {
			Assert.assertNotNull("don't call without answer", answer);

			char[] fileName = null;
			if(answer.getBinaryType() != null) {
				URI uri = answer.getBinaryType().getURI();
				this.answeredFileNames.add(uri.toString());
				return;
			} else if(answer.getCompilationUnit() != null) {
				fileName = answer.getCompilationUnit().getFileName();
			} else if(answer.getSourceTypes() != null && answer.getSourceTypes().length > 0) {
				fileName = answer.getSourceTypes()[0].getFileName(); // the first type is guaranteed to be the requested type
			} else if(answer.getResolvedBinding() != null) {
				fileName = answer.getResolvedBinding().getFileName();
			}
			if (fileName != null) this.answeredFileNames.add(new String(fileName));
		}
	}

	public void testNameEnvironmentAnswerListener() throws IOException {
		String path = LIB_DIR;
		if(!path.endsWith(File.separator)) {
			path += File.separator;
		}
		String libPath = path + "lib.jar";
		Util.createJar(
				new String[] {
						"p/Color.java",
						"package p;\n" +
						"public enum Color {\n" +
						"	R, Y;\n" +
						"	public static Color getColor() {\n" +
						"		return R;\n" +
						"	}\n" +
						"}",
					},
				libPath, JavaCore.VERSION_17);

		String unusedLibPath = path + "lib_unused.jar";
		Util.createJar(
				new String[] {
						"p2/Color.java",
						"package p2;\n" +
						"public enum Color {\n" +
						"	R, Y;\n" +
						"	public static Color getColor() {\n" +
						"		return R;\n" +
						"	}\n" +
						"}",
					},
				unusedLibPath, JavaCore.VERSION_17);

		String srcDir =  path + "src";
		String[] pathsAndContents =
				new String[] {
					"s/X.java",
					"package s;\n" +
					"import p.Color;\n" +
					"public class X {\n" +
					"	public static final Color MY = Color.R;\n" +
					"}"
				};
		Util.createSourceDir(pathsAndContents, srcDir);

		List<String> classpath = new ArrayList<>(Arrays.asList(getDefaultClassPaths()));
		classpath.add(libPath);
		classpath.add(unusedLibPath);

		File outputDirectory = new File(Util.getOutputDirectory());
		if (!outputDirectory.isDirectory()) {
			outputDirectory.mkdirs();
		}

		List<String> ecjArguments = new ArrayList<>();

		ecjArguments.add("-classpath");
		ecjArguments.add(classpath.stream()
				.map(jar -> jar.equals(unusedLibPath) ? String.format("%s[-**/*]", jar) : jar)
				.collect(joining(File.pathSeparator)));


		ecjArguments.add("-d");
		ecjArguments.add(outputDirectory.getAbsolutePath());

		ecjArguments.add("--release");
		ecjArguments.add("17");

		ecjArguments.add(srcDir+ File.separator + "s"+ File.separator + "X.java");

		EclipseBatchCompiler compiler;
		File logFile = new File(outputDirectory, "compile.log");
		try(PrintWriter log = new PrintWriter(new FileOutputStream(logFile))) {
			compiler = new EclipseBatchCompiler(log);
			boolean compileOK;
			compileOK = compiler.compile(ecjArguments.toArray(new String[ecjArguments.size()]));
			if(!compileOK) {
				String logOutputString = Util.fileContent(logFile.getAbsolutePath());
				Assert.fail("Compile failed, output: '" + logOutputString + "'");
			}
		}
		String libPathExpected = libPath.replace('\\', '/');
		String unusedLibPathExpected = unusedLibPath.replace('\\', '/');
		Set<String> answeredFileNames = compiler.answeredFileNames;
		Assert.assertTrue("must reference p.Color: " + answeredFileNames, answeredFileNames.stream().anyMatch(s -> s.contains(libPathExpected)));
		Assert.assertFalse("must not reference p2.Color: " + answeredFileNames, answeredFileNames.stream().anyMatch(s -> s.contains(unusedLibPathExpected)));
	}
}
