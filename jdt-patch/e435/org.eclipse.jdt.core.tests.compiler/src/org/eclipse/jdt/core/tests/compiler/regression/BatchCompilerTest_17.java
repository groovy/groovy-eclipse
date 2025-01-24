/*******************************************************************************
 * Copyright (c) 2022 Andrey Loskutov (loskutov@gmx.de) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov (loskutov@gmx.de) - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import junit.framework.Test;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.compiler.regression.BatchCompilerTest.SubstringMatcher;
import org.eclipse.jdt.core.tests.util.Util;

public class BatchCompilerTest_17 extends AbstractBatchCompilerTest {

	/**
	 * This test suite only needs to be run on one compliance.
	 *
	 * @see TestAll
	 */
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_17);
	}

	public static Class<BatchCompilerTest_17> testClass() {
		return BatchCompilerTest_17.class;
	}

	public BatchCompilerTest_17(String name) {
		super(name);
	}

	static {
		// TESTS_NAMES = new String [] { "testGHI1774_Expression" };
	}

	/**
	 * Test tries to compile same sources <b>in parallel</b> to different output directories.
	 * There should be no interdependencies between compiler threads, but the test failed
	 * due some error in the initialization of shared static compiler data.
	 * @see <a href="https://github.com/eclipse-jdt/eclipse.jdt.core/issues/183">bug 183</a>
	 */
	public void testParallelCompilation() throws Throwable {
		Path root = Files.createDirectories(Paths.get(OUTPUT_DIR));
		String[] sources = new String[] {
				"X.java",
				"public class X {\n"
				+ "	public static void main(String[] args) {\n"
				+ "		new X().printHello();\n"
				+ "		new Thread(() -> {\n"
				+ "			new X().printHello();\n"
				+ "		}).start();\n"
				+ "	}\n"
				+ "	private void printHello() {\n"
				+ "		System.out.println(\"Hello \" + Thread.currentThread());\n"
				+ "	}\n"
				+ "}",
				"ExampleEnum.java",
				"public enum ExampleEnum {\n"
				+ "	A, B, C;\n"
				+ "}\n"
				+ ""
			};

		for (int i = 0; i < sources.length; i += 2) {
			Files.writeString(root.resolve(sources[i]), sources[i + 1]);
		}

		Map<String, Object> map = new TreeMap<>(compileParallel(sources));
		Optional<Object> err = map.values().stream().filter(x -> (x instanceof Throwable)).findFirst();
		if (err.isPresent()) {
			throw (Throwable) err.get();
		}
	}

	public Map<String, Object> compileParallel(String[] sources) throws Exception {
		int threads = Math.max(16, 2 * Runtime.getRuntime().availableProcessors());
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch finishLatch = new CountDownLatch(threads);
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		Map<String, Object> results = new ConcurrentHashMap<>();
		for (int i = 0; i < threads; i++) {
			String name = String.format("Compile-Thread-%02d", i);
			executor.execute(() -> {
				Thread.currentThread().setName(name);
				try {
					results.put(name, compile(sources, startLatch));
				} catch (Throwable e) {
					results.put(name, e);
				} finally {
					finishLatch.countDown();
				}
			});
		}
		startLatch.countDown();
		finishLatch.await();
		executor.shutdownNow();
		return results;
	}

	private String compile(String[] sources, CountDownLatch startLatch) throws Exception {
		String folder = "BatchCompilerTest_17_" + Thread.currentThread().getName();
		File outputFolder = new File(OUTPUT_DIR, folder);
		Files.createDirectories(outputFolder.toPath());
		StringBuilder output = new StringBuilder();
		StringBuilder error = new StringBuilder();
		startLatch.await();
		runTest(
				true,
				sources,
		        "\"" + OUTPUT_DIR +  File.separator + sources[0] + "\""
		        + " -g -nowarn -target 11 -source 11 --release 11"
		        + " -encoding UTF-8"
		        + " -d \"" + outputFolder + "\"",
		        new SubstringMatcher("") {
		    		@Override
		    		boolean match(String out) {
		    			output.append(out);
		    			return super.match(out);
		    		}
		    	},
		        new Matcher() {
		    		@Override
		    		String expected() {
		    			return org.eclipse.jdt.internal.compiler.util.Util.EMPTY_STRING;
		    		}
		    		@Override
		    		boolean match(String err) {
		    			error.append(err);
		    			return err != null && err.length() == 0;
		    		}
		    	},
		        false);

		return "StdErr: " + error + " StdOut: " + output;
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1774
	// Check behavior of expression switch in JDK18-
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3096#issuecomment-2417954288
	public void testGHI1774_Expression() throws Exception {
		String javaVersion = System.getProperty("java.version");
		if (javaVersion != null && JavaCore.compareJavaVersions(javaVersion, "18") < 0)
			return;
		String path = LIB_DIR;
		String libPath = null;
		if (path.endsWith(File.separator)) {
			libPath = path + "lib.jar";
		} else {
			libPath = path + File.separator + "lib.jar";
		}
		Util.createJar(new String[] {
			"p/Color.java",
			"package p;\n" +
			"public enum Color {\n" +
			"	R, Y;\n" +
			"	public static Color getColor() {\n" +
			"		return R;\n" +
			"	}\n" +
			"}",
		},
		libPath,
		JavaCore.VERSION_18);
		this.runConformTest(
			new String[] {
				"src/p/X.java",
				"package p;\n"
				+ "import p.Color;\n"
				+ "public class X {\n"
				+ "	public static void main(String argv[]) {\n"
				+ "		Color c = Color.getColor();\n"
				+ "		try {\n"
				+ "			int a = switch (c) {\n"
				+ "				case R -> 1;\n"
				+ "				case Y -> 2;\n"
				+ "			};\n"
				+ "		} catch (IncompatibleClassChangeError e) {\n"
				+ "			System.out.print(\"OK\");\n"
				+ "		} catch (Exception e) {\n"
				+ "			System.out.print(\"NOT OK: \" + e);\n"
				+ "		}\n"
				+ "			System.out.print(\"END\");\n"
				+ "	}\n"
				+ "}",
			},
			"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
			+ " -cp \"" + LIB_DIR + File.separator + "lib.jar\""
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
			+ " -source 18 -warn:none"
			+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
			"",
			"",
			true);
		this.verifier.execute("p.X", new String[] {OUTPUT_DIR + File.separator + "bin", libPath}, new String[0], new String[] {"--enable-preview"});
		assertEquals("Incorrect output", "END", this.verifier.getExecutionOutput());
		Util.createJar(new String[] {
				"p/Color.java",
				"package p;\n" +
				"public enum Color {\n" +
				"	R, Y, B;\n" +
				"	public static Color getColor() {\n" +
				"		return B;\n" +
				"	}\n" +
				"}",
			},
			libPath,
			JavaCore.VERSION_18);
		this.verifier.execute("p.X", new String[] {OUTPUT_DIR + File.separator + "bin", libPath}, new String[0], new String[] {"--enable-preview"});
		assertEquals("Incorrect output", "OKEND", this.verifier.getExecutionOutput());
	}
}
