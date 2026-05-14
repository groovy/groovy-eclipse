/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest.JavacTestOptions.JavacHasABug;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class PrimitiveInPatternsTestSH extends AbstractRegressionTest9 {

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("--enable-preview -source 23 -Xlint:-preview");
	private static final String[] VMARGS = new String[] {"--enable-preview"};

	private static final String[] PRIMITIVES = { "boolean", "byte", "char", "short", "int", "long", "float", "double" };
	private static final String[] BOXES = { "Boolean", "Byte", "Character", "Short", "Integer", "Long", "Float", "Double" };
	// note: Character.MAX_VALUE doesn't play well with stream handling around TestVerifier, so we avoid non-ascii chars during print():
	private static final String[] MAXVALUES = { "true", "Byte.MAX_VALUE", "'z'", "Short.MAX_VALUE", "Integer.MAX_VALUE", "Long.MAX_VALUE", "Float.MAX_VALUE", "Double.MAX_VALUE" };
	private static final String[] GOODVALUES = { "true", "49", "'1'", "49", "49", "49L", "49.0f", "49.0d" }; // 49 ~ '1'
	private static final String[] NEGVALUES = { "false", "-1", "'-'", "-1", "-1", "-1L", "-1.0f", "-1.0d" };

	// larger then MAX of previous type, still needs suffix added via toConstantOfType
	private static final String[] CONSTANTS = { "true", "1", "'1'", "300", "40000", "5000000000", "6.0E20", "7.0E40" };
	private static final boolean[] IS_NUMERICAL = { false, true, false, true, true, true, true, true };
	private static String MAX_VALUES_STRING = "true|127|z|32767|2147483647|9223372036854775807|3.4028235E38|1.7976931348623157E308|";
	/**
	 * Test programs may use the following placeholders, which are filled in by this method:
	 * <ul>
	 * <li>PRIM a primitive type
	 * <li>BOX the corresponding boxing type
	 * <li>NEGVAL a value of that type signaling failure
	 * <li>VAL a regular value
	 * </ul>
	 * @param template the template with placeholders
	 * @param idx index into {@link #PRIMITIVES} etc.
	 * @return the program snippet with placeholders filled in.
	 */
	private static String fillIn(String template, int idx) {
		return template.replaceAll("PRIM", PRIMITIVES[idx]).replaceAll("BOX", BOXES[idx])
						.replace("NEGVAL", NEGVALUES[idx]).replace("VAL", GOODVALUES[idx]).replace("MAX", MAXVALUES[idx]);
	}
	/** like {@link #fillIn(String, int)}, but may use {@link #MAXVALUES} if 'maxValue' is true. */
	private static String fillInMax(String template, int idx, boolean useMax) {
		return template.replaceAll("PRIM", PRIMITIVES[idx]).replaceAll("BOX", BOXES[idx])
						.replace("NEGVAL", NEGVALUES[idx]).replace("VAL", useMax ? MAXVALUES[idx] : GOODVALUES[idx]);
	}

	static String toConstantOfType(String constVal, String ptype) {
		return switch (ptype) {
			case "long" -> constVal+"L";
			case "float" -> constVal+"f";
			case "double" -> constVal+"d";
			default -> constVal;
		};
	}


	static {
//		TESTS_NUMBERS = new int [] { 1 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testPrimitivePatternInSwitch" };
	}
	private String extraLibPath;
	public static Class<?> testClass() {
		return PrimitiveInPatternsTestSH.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_23);
	}
	public PrimitiveInPatternsTestSH(String testName) {
		super(testName);
	}

	// ========= OPT-IN to run.javac mode: ===========
	@Override
	protected void setUp() throws Exception {
		this.runJavacOptIn = true;
		super.setUp();
	}
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		this.runJavacOptIn = false; // do it last, so super can still clean up
	}
	// =================================================

	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions(boolean preview) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_23);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_23);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_23);
		defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, preview ? CompilerOptions.ENABLED : CompilerOptions.DISABLED);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.WARNING);
		return defaultOptions;
	}

	protected Map<String, String> getCompilerOptions() {
		return getCompilerOptions(false);
	}
	protected String[] getDefaultClassPaths() {
		String[] libs = DefaultJavaRuntimeEnvironment.getDefaultClassPaths();
		if (this.extraLibPath != null) {
			String[] l = new String[libs.length + 1];
			System.arraycopy(libs, 0, l, 0, libs.length);
			l[libs.length] = this.extraLibPath;
			return l;
		}
		return libs;
	}
	@Override
	protected INameEnvironment getNameEnvironment(final String[] testFiles, String[] classPaths, Map<String, String> options) {
		this.classpaths = classPaths == null ? getDefaultClassPaths() : classPaths;
		INameEnvironment[] classLibs = getClassLibs(false, options);
		for (INameEnvironment nameEnvironment : classLibs) {
			((FileSystem) nameEnvironment).scanForModules(createParser());
		}
		return new InMemoryNameEnvironment9(testFiles, this.moduleMap, classLibs);
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, getCompilerOptions(true), VMARGS, JAVAC_OPTIONS);
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		if(!isJRE23Plus)
			return;
		runConformTest(testFiles, expectedOutput, customOptions, VMARGS, JAVAC_OPTIONS);
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
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		Map<String, String> customOptions = getCompilerOptions(true);
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.javacTestOptions = JAVAC_OPTIONS;
		runner.customOptions = customOptions;
		runner.expectedJavacOutputString = null;
		runner.runNegativeTest();
	}
	protected void runNegativeTest(
			String[] testFiles,
			String expectedCompilerLog,
			String javacLog,
			String[] classLibraries,
			boolean shouldFlushOutputDirectory,
			Map<String, String> customOptions) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.javacTestOptions = JAVAC_OPTIONS;
		runner.customOptions = customOptions;
		runner.expectedJavacOutputString = javacLog;
		runner.runNegativeTest();
	}

	// https://cr.openjdk.org/~abimpoudis/instanceof/jep455-20240424/specs/instanceof-jls.html#jls-5.1.2
	// 5.7 Testing Contexts
	// Identity Conversion
	public void testIdentity() {
		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static PRIM fooPRIM(PRIM v) {
						if (v instanceof PRIM) {
							return v;
						}
						return NEGVAL;
					}
				""";
		String callTmpl =
				"""
						PRIM vPRIM = VAL;
						System.out.print(X.fooPRIM(vPRIM));
						System.out.print('|');
				""";
		// for all primitive types:
		for (int i = 0; i < PRIMITIVES.length; i++) {
			methods.append(fillIn(methodTmpl, i));
			calls.append(fillInMax(callTmpl, i, true));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append("public static void main(String[] args) {\n");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() }, MAX_VALUES_STRING);
	}
	public void testIdentityPattern() {
		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static PRIM fooPRIM(PRIM v) {
						if (v instanceof PRIM vv) {
							return vv;
						}
						return NEGVAL;
					}
				""";
		String callTmpl =
				"""
						PRIM vPRIM = VAL;
						System.out.print(X.fooPRIM(vPRIM));
						System.out.print('|');
				""";
		// for all primitive types:
		for (int i = 0; i < PRIMITIVES.length; i++) {
			methods.append(fillIn(methodTmpl, i));
			calls.append(fillInMax(callTmpl, i, true));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append("public static void main(String[] args) {\n");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() }, MAX_VALUES_STRING);
	}

	public void testIdentity_functionLhs() {
		// one sample should suffice here:
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static byte foo() {
						if (bar() instanceof byte) {
							byte b = (byte) bar();
							return b;
						}
						return -1;
					}
					public static byte bar() {
						byte b = 1;
						return b;
					}
					public static void main(String[] args) {
						System.out.println(X.foo());
					}
				}
				"""
			},
			"1");
	}

	public void testIdentityPattern_functionLhs() {
		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static PRIM fooPRIM() {
						if (barPRIM() instanceof PRIM vv) {
							return vv;
						}
						return NEGVAL;
					}
					public static PRIM barPRIM() {
						return VAL;
					}
				""";
		String callTmpl =
				"""
						System.out.print(X.fooPRIM());
						System.out.print('|');
				""";
		// for all primitive types:
		for (int i = 0; i < PRIMITIVES.length; i++) {
			methods.append(fillIn(methodTmpl, i));
			calls.append(fillIn(callTmpl, i));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append("public static void main(String[] args) {\n");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() },
			"true|49|1|49|49|49|49.0|49.0|");
	}

	// Widening primitive conversions
	// 5.1.2: [...] exact widening primitive conversion [...]. Such a conversion can be one of the following:
	// * from an integral type to another integral type
	// * from byte, short, or char to a floating-point type
	// * from int to double
	// * from float to double
	// inexact widening conversions:
	// * from int to float, or from long to float, or from long to double
	private void testWideningFrom(String from, int idx, boolean useMax, String expectedOut) {
		assert from.equals(PRIMITIVES[idx]) : "mismatch between from and idx";
		// example (from="long", idx=5, useMax=false, ...):
		//	public class X {
		//		public static float foofloat(long v) {
		//			if (v instanceof float) {
		//				float vv = (float) v;
		//				return vv;
		//			}
		//			return -1.0f;
		//		}
		//		public static double foodouble(long v) {
		//			if (v instanceof double) {
		//				double vv = (double) v;
		//				return vv;
		//			}
		//			return -1.0d;
		//		}
		//		public static void main(String[] args) {
		//			long v = 49L;
		//			System.out.print(X.foofloat(v));
		//			System.out.print('|');
		//			System.out.print(X.foodouble(v));
		//			System.out.print('|');
		//	}}

		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static PRIM fooPRIM(FROM v) {
						if (v instanceof PRIM) {
							PRIM vv = (PRIM) v;
							return vv;
						}
						return NEGVAL;
					}
				""";
		String callTmpl =
				"""
						System.out.print(X.fooPRIM(v));
						System.out.print('|');
				""";
		// for all numerical primitive types "greater" than 'from':
		for (int i = idx+1; i < PRIMITIVES.length; i++) {
			if (!IS_NUMERICAL[i]) continue;
			methods.append(fillIn(methodTmpl.replace("FROM", from), i));
			calls.append(fillIn(callTmpl, i));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append("public static void main(String[] args) {\n");
		classX.append(fillInMax("PRIM v = VAL;\n", idx, useMax));
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() }, expectedOut);
	}
	private void testWideningFrom_pattern(String from, int idx, boolean useMax, String expectedOut) {
		assert from.equals(PRIMITIVES[idx]) : "mismatch between from and idx";
		// example (from="long", idx=5, useMax=false, ..):
		//	public class X {
		//		public static float foofloat() {
		//			if (bar() instanceof float vv) {
		//				return vv;
		//			}
		//			return -1.0f;
		//		}
		//		public static double foodouble() {
		//			if (bar() instanceof double vv) {
		//				return vv;
		//			}
		//			return -1.0d;
		//		}
		//		static long bar() {
		//			return 49L;
		//		}
		//		public static void main(String[] args) {
		//			System.out.print(X.foofloat());
		//			System.out.print('|');
		//			System.out.print(X.foodouble());
		//			System.out.print('|');
		//		}
		//	}

		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static PRIM fooPRIM() {
						if (bar() instanceof PRIM vv) {
							return vv;
						}
						return NEGVAL;
					}
				""";
		String methodBar = fillInMax("""
					static PRIM bar() {
						return VAL;
					}
					""",
					idx, useMax);
		String callTmpl =
				"""
						System.out.print(X.fooPRIM());
						System.out.print('|');
				""";
		// for all numerical primitive types "greater" than 'from':
		for (int i = idx+1; i < PRIMITIVES.length; i++) {
			if (!IS_NUMERICAL[i]) continue;
			methods.append(fillIn(methodTmpl, i));
			calls.append(fillIn(callTmpl, i));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append(methodBar);
		classX.append("public static void main(String[] args) {\n");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() }, expectedOut);
	}
	private void testWideningFrom_both(String prim, int idx, boolean useMax, String expectedOut) {
		testWideningFrom(prim, idx, useMax, expectedOut);
		testWideningFrom_pattern(prim, idx, useMax, expectedOut);
	}
	public void testWideningByte() {
		testWideningFrom_both("byte", 1, false, "49|49|49|49.0|49.0|");
		testWideningFrom_both("byte", 1, true, "127|127|127|127.0|127.0|");
	}
	public void testWideningChar() {
		testWideningFrom_both("char", 2, false, "49|49|49|49.0|49.0|"); // '1'
		testWideningFrom_both("char", 2, true, "122|122|122|122.0|122.0|"); // 'z'
	}
	public void testWideningShort() {
		testWideningFrom_both("short", 3, false, "49|49|49.0|49.0|");
		testWideningFrom_both("short", 3, true, "32767|32767|32767.0|32767.0|");
	}
	public void testWideningInt() {
		testWideningFrom_both("int", 4, false, "49|49.0|49.0|");
		// max-int -> float is not exact
		testWideningFrom_both("int", 4, true, "2147483647|-1.0|"+String.valueOf((double) Integer.MAX_VALUE)+'|');
	}
	public void testWideningLong() {
		testWideningFrom_both("long", 5, false, "49.0|49.0|");
		// max-long -> float/double is not exact
		testWideningFrom_both("long", 5, true, "-1.0|-1.0|");
	}
	public void testWideningFloat() {
		testWideningFrom_both("float", 6, false, "49.0|");
		testWideningFrom_both("float", 6, true, String.valueOf((double) Float.MAX_VALUE)+"|");
	}

	private void testNarrowingFrom(String from, int idx, boolean useMax, String expectedOut) {
		assert from.equals(PRIMITIVES[idx]) : "mismatch between from and idx";
		// example (from="short", idx=3, useMax=false):
		//	public class X {
		//		public static byte foobyte(short v) {
		//			if (v instanceof byte) {
		//				byte vv = (byte) v;
		//				return vv;
		//			}
		//			return -1;
		//		}
		//		public static char foochar(short v) {
		//			if (v instanceof char) {
		//				char vv = (char) v;
		//				return vv;
		//			}
		//			return '-';
		//		}
		//		static void print(Object o) {
		//			if (o instanceof Character && ((int)((char) o) > 127))
		//				System.out.print((int)((char) o)); // avoid char encoding issues
		//			else
		//				System.out.print(o);
		//			System.out.print('|');
		//		}
		//		public static void main(String[] args) {
		//			short v = 49;
		//			print(X.foobyte(v));
		//			print(X.foochar(v));
		//		}
		//	}

		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static PRIM fooPRIM(FROM v) {
						if (v instanceof PRIM) {
							PRIM vv = (PRIM) v;
							return vv;
						}
						return NEGVAL;
					}
				""";
		String methodPrint = """
				static void print(Object o) {
					if (o instanceof Character && ((int)((char) o) > 127))
						System.out.print((int)((char) o)); // avoid char encoding issues
					else
						System.out.print(o);
					System.out.print('|');
				}
				""";
		String callTmpl =
				"""
						print(X.fooPRIM(v));
				""";
		// for all primitive types "smaller" than 'from' (except for boolean):
		for (int i = 1; i < idx; i++) {
			methods.append(fillIn(methodTmpl.replace("FROM", from), i));
			calls.append(fillIn(callTmpl, i));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append(methodPrint);
		classX.append("public static void main(String[] args) {\n");
		classX.append(fillInMax("PRIM v = VAL;\n", idx, useMax));
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() }, expectedOut);
	}
	private void testNarrowingFrom_pattern(String from, int idx, boolean useMax, String expectedOut) {
		assert from.equals(PRIMITIVES[idx]) : "mismatch between from and idx";
		// example (from="short", idx=3, useMax=false, ...):
		//	public class X {
		//		public static byte foobyte() {
		//			if (bar() instanceof byte vv) {
		//				return vv;
		//			}
		//			return -1;
		//		}
		//		public static char foochar() {
		//			if (bar() instanceof char vv) {
		//				return vv;
		//			}
		//			return '-';
		//		}
		//		static short bar() {
		//			return 49;
		//		}
		//		static void print(Object o) {
		//			if (o instanceof Character && (int)((char) o) > 127)
		//				System.out.print((int)((char) o)); // avoid char encoding issues
		//			else
		//				System.out.print(o);
		//			System.out.print('|');
		//		}
		//		public static void main(String[] args) {
		//			print(X.foobyte());
		//			print(X.foochar());
		//		}
		//	}

		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static PRIM fooPRIM() {
						if (bar() instanceof PRIM vv) {
							return vv;
						}
						return NEGVAL;
					}
				""";
		String methodBar = fillInMax("""
				static PRIM bar() {
					return VAL;
				}
				""",
				idx, useMax);
		String methodPrint = """
				static void print(Object o) {
					if (o instanceof Character && ((int)((char) o) > 127))
						System.out.print((int)((char) o)); // avoid char encoding issues
					else
						System.out.print(o);
					System.out.print('|');
				}
				""";
		String callTmpl =
				"""
						print(X.fooPRIM());
				""";
		// for all primitive types "smaller" than 'from' (except for boolean):
		for (int i = 1; i < idx; i++) {
			methods.append(fillIn(methodTmpl, i));
			calls.append(fillIn(callTmpl, i));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append(methodBar);
		classX.append(methodPrint);
		classX.append("public static void main(String[] args) {\n");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() }, expectedOut);
	}
	private void testNarrowingFrom_both(String prim, int idx, boolean useMax, String expectedOut) {
		testNarrowingFrom(prim, idx, useMax, expectedOut);
		testNarrowingFrom_pattern(prim, idx, useMax, expectedOut);
	}
	public void testNarrowingDouble() {
		testNarrowingFrom_both("double", 7, false, "49|1|49|49|49|49.0|");
		testNarrowingFrom_both("double", 7, true, "-1|-|-1|-1|-1|-1.0|");
	}
	public void testNarrowingFloat() {
		testNarrowingFrom_both("float", 6, false, "49|1|49|49|49|");
		testNarrowingFrom_both("float", 6, true, "-1|-|-1|-1|-1|");
	}
	public void testNarrowingLong() {
		testNarrowingFrom_both("long", 5, false, "49|1|49|49|");
		testNarrowingFrom_both("long", 5, true, "-1|-|-1|-1|");
	}
	public void testNarrowingInt() {
		testNarrowingFrom_both("int", 4, false, "49|1|49|");
		testNarrowingFrom_both("int", 4, true, "-1|-|-1|");
	}
	public void testNarrowingShort() {
		testNarrowingFrom_both("short", 3, false, "49|1|");
		testNarrowingFrom_both("short", 3, true, "-1|32767|");
	}
	public void testNarrowingChar() {
		testNarrowingFrom_both("char", 2, false, "49|"); // '1'
	}

	public void testNarrowingChar_various() {
		runConformTest(new String[] {
			"X.java",
				"""
				public class X {
					public static char b2c(byte b) {
						if (b instanceof char) {
							return (char) b;
						}
						return '-';
					}
					public static char s2c(short s) {
						if (s instanceof char) {
							return (char) s;
						}
						return '-';
					}
					public static short c2s(char c) {
						if (c instanceof short) {
							return (short) c;
						}
						return -1;
					}
					public static char b2c_pat(byte b) {
						if (b instanceof char v) {
							return v;
						}
						return '-';
					}
					public static char s2c_pat(short s) {
						if (s instanceof char v) {
							return v;
						}
						return '-';
					}
					public static short c2s_pat(char c) {
						if (c instanceof short v) {
							return v;
						}
						return -1;
					}
					public static void main(String[] args) {
						byte b=49, bmax=Byte.MAX_VALUE;
						short s=49, smax=Short.MAX_VALUE;
						char c='1', cmax=Character.MAX_VALUE;
						print(X.b2c(b));
						print(X.s2c(s));
						print(X.c2s(c));
						print(X.b2c(bmax));
						print(X.s2c(smax));
						print(X.c2s(cmax));
						System.out.println();
						print(X.b2c_pat(b));
						print(X.s2c_pat(s));
						print(X.c2s_pat(c));
						print(X.b2c_pat(bmax));
						print(X.s2c_pat(smax));
						print(X.c2s_pat(cmax));
					}
					static void print(Object s) {
						if (s instanceof Character)
							System.out.print((int)((char) s)); // avoid char encoding issues
						else
							System.out.print(s);
						System.out.print('|');
					}
				}
				"""
			},
			"49|49|49|127|32767|-1|\n" +
			"49|49|49|127|32767|-1|");
	}

	public void testBoxing() {
		//	public class X {
		//		public static Boolean boolean2Boolean(boolean v) {
		//			if (v instanceof Boolean) {
		//				return (Boolean) v;
		//			}
		//			return false;
		//		}
		//		public static Byte byte2Byte(byte v) {
		//			if (v instanceof Byte) {
		//				return (Byte) v;
		//			}
		//			return -1;
		//		}
		//		[...]
		//		public static void main(String[] args) {
		//			boolean vboolean = true;
		//			System.out.print(X.boolean2Boolean(vboolean));
		//			System.out.print('|');
		//			byte vbyte = 49;
		//			System.out.print(X.byte2Byte(vbyte));
		//			System.out.print('|');
		//			[...]
		//		}
		//	}

		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static BOX PRIM2BOX(PRIM v) {
						if (v instanceof BOX) {
							return (BOX) v;
						}
						return NEGVAL;
					}
				""";
		String callTmpl =
				"""
						PRIM vPRIM = VAL;
						System.out.print(X.PRIM2BOX(vPRIM));
						System.out.print('|');
				""";
		// for all primitive types:
		for (int i = 0; i < PRIMITIVES.length; i++) {
			methods.append(fillIn(methodTmpl, i));
			calls.append(fillIn(callTmpl, i));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append("public static void main(String[] args) {\n");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() },
				"true|49|1|49|49|49|49.0|49.0|");
	}
	public void testBoxing_pattern() {
		//	public class X {
		//		public static Boolean boolean2Boolean() {
		//			if (barboolean() instanceof Boolean v) {
		//				return v;
		//			}
		//			return false;
		//		}
		//		static boolean barboolean() {
		//			return true;
		//		}
		//		public static Byte byte2Byte() {
		//			if (barbyte() instanceof Byte v) {
		//				return v;
		//			}
		//			return -1;
		//		}
		//		static byte barbyte() {
		//			return 49;
		//		}
		//		[...]
		//		public static void main(String[] args) {
		//			System.out.print(X.boolean2Boolean());
		//			System.out.print('|');
		//			System.out.print(X.byte2Byte());
		//			System.out.print('|');
		//			[...]
		//		}
		//	}

		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static BOX PRIM2BOX() {
						if (barPRIM() instanceof BOX v) {
							return v;
						}
						return NEGVAL;
					}
					static PRIM barPRIM() {
						return VAL;
					}
				""";
		String callTmpl =
				"""
						System.out.print(X.PRIM2BOX());
						System.out.print('|');
				""";
		// for all primitive types:
		for (int i = 0; i < PRIMITIVES.length; i++) {
			methods.append(fillIn(methodTmpl, i));
			calls.append(fillIn(callTmpl, i));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append("public static void main(String[] args) {\n");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() },
				"true|49|1|49|49|49|49.0|49.0|");
	}

	public void testUnboxing() {
		//	public class X {
		//		public static boolean Boolean2boolean(Boolean v) {
		//			if (v instanceof boolean) {
		//				return (boolean) v;
		//			}
		//			return false;
		//		}
		//		public static byte Byte2byte(Byte v) {
		//			if (v instanceof byte) {
		//				return (byte) v;
		//			}
		//			return -1;
		//		}
		//		[...]
		//		public static void main(String[] args) {
		//			Boolean vBoolean = true;
		//			System.out.print(X.Boolean2boolean(vBoolean));
		//			System.out.print('|');
		//			Byte vByte = 49;
		//			System.out.print(X.Byte2byte(vByte));
		//			System.out.print('|');
		//			[...]
		//		}
		//	}

		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static PRIM BOX2PRIM(BOX v) {
						if (v instanceof PRIM) {
							return (PRIM) v;
						}
						return NEGVAL;
					}
				""";
		String callTmpl =
				"""
						BOX vBOX = VAL;
						System.out.print(X.BOX2PRIM(vBOX));
						System.out.print('|');
				""";
		// for all primitive types:
		for (int i = 0; i < PRIMITIVES.length; i++) {
			methods.append(fillIn(methodTmpl, i));
			calls.append(fillIn(callTmpl, i));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append("public static void main(String[] args) {\n");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() },
				"true|49|1|49|49|49|49.0|49.0|");
	}
	public void testUnboxing_pattern() {
		//	public class X {
		//		public static Boolean boolean2Boolean() {
		//			if (barboolean() instanceof Boolean v) {
		//				return v;
		//			}
		//			return false;
		//		}
		//		static boolean barboolean() {
		//			return true;
		//		}
		//		public static Byte byte2Byte() {
		//			if (barbyte() instanceof Byte v) {
		//				return v;
		//			}
		//			return -1;
		//		}
		//		static byte barbyte() {
		//			return 49;
		//		}
		//		[...]
		//		public static void main(String[] args) {
		//			System.out.print(X.boolean2Boolean());
		//			System.out.print('|');
		//			System.out.print(X.byte2Byte());
		//			System.out.print('|');
		//			[...]
		//		}
		//	}

		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static PRIM BOX2PRIM() {
						if (barBOX() instanceof PRIM v) {
							return v;
						}
						return NEGVAL;
					}
					static BOX barBOX() {
						return VAL;
					}
				""";
		String callTmpl =
				"""
						System.out.print(X.BOX2PRIM());
						System.out.print('|');
				""";
		// for all primitive types:
		for (int i = 0; i < PRIMITIVES.length; i++) {
			methods.append(fillIn(methodTmpl, i));
			calls.append(fillIn(callTmpl, i));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append("public static void main(String[] args) {\n");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() },
				"true|49|1|49|49|49|49.0|49.0|");
	}

	// boxing and widening reference conversion

	private void primitive2Comparable(String prim, int idx, String expectedOut) {
		String methodTmpl =
				"""
					@SuppressWarnings("rawtypes")
					public static Comparable foo1(PRIM v) {
						if (v instanceof Comparable r) {
							return r;
						}
						return null;
					}
					@SuppressWarnings({"rawtypes", "unchecked" })
					public static Comparable<BOX> foo2(PRIM v) {
						if (v instanceof Comparable r) {
							return r;
						}
						return null;
					}
					@SuppressWarnings("unchecked")
					public static Comparable<BOX> foo3(PRIM v) {
						if (v instanceof Comparable<BOX> r) {
							return r;
						}
						return null;
					}
				""";
		String callTmpl =
				"""
						PRIM vPRIM = VAL;
						System.out.print(X.foo1(vPRIM));
						System.out.print('|');
						System.out.print(X.foo2(vPRIM));
						System.out.print('|');
						System.out.print(X.foo3(vPRIM));
						System.out.print('|');
				""";
		if (IS_NUMERICAL[idx]) {
			methodTmpl +=
				"""
					public static Number foo4(PRIM v) {
						if (v instanceof Number r) {
							return r;
						}
						return null;
					}
				""";
			callTmpl +=
				"""
					System.out.print(X.foo4(vPRIM));
					System.out.print('|');
				""";
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(fillIn(methodTmpl, idx));
		classX.append("public static void main(String[] args) {\n");
		classX.append(fillIn(callTmpl, idx));
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() }, expectedOut);
	}

	public void test2Comparable_boolean() {
		primitive2Comparable("boolean", 0, "true|true|true|");
	}
	public void test2Comparable_byte() {
		primitive2Comparable("byte", 1, "49|49|49|49|");
	}
	public void test2Comparable_char() {
		primitive2Comparable("char", 2, "1|1|1|"); // '1'
	}
	public void test2Comparable_short() {
		primitive2Comparable("short", 3, "49|49|49|49|");
	}
	public void test2Comparable_int() {
		primitive2Comparable("int", 4, "49|49|49|49|");
	}
	public void test2Comparable_long() {
		primitive2Comparable("long", 5, "49|49|49|49|");
	}
	public void test2Comparable_float() {
		primitive2Comparable("float", 6, "49.0|49.0|49.0|49.0|");
	}
	public void test2Comparable_double() {
		primitive2Comparable("double", 7, "49.0|49.0|49.0|49.0|");
	}
	public void test2Number_NOK() {
		runNegativeTest(new String[] {
			"X.java",
				"""
				public class X {
					public static Number foo1(boolean b) {
						if (b instanceof Number r) {
							return r;
						}
						return null;
					}
					public static Number foo2(char c) {
						if (c instanceof Number r) {
							return r;
						}
						return null;
					}
				}
  			    """
			},
			"""
			----------
			1. ERROR in X.java (at line 3)
				if (b instanceof Number r) {
				    ^^^^^^^^^^^^^^^^^^^^^
			Incompatible conditional operand types boolean and Number
			----------
			2. ERROR in X.java (at line 9)
				if (c instanceof Number r) {
				    ^^^^^^^^^^^^^^^^^^^^^
			Incompatible conditional operand types char and Number
			----------
			""");
	}

	public void testNonPrim001() {
		runConformTest(new String[] {
			"X.java",
				"""
					class Y<T> {
					    public boolean foo(T t) {
					        if (t instanceof T) {
					            return false;
					        }
					        return true;
					    }
					}

					public class X  {
					    public static void main(String argv[]) {
					    	System.out.println(new Y<X>().foo(null));
					    }
					}
 			    """
			},
			"true");
	}

	public void testPrimitivePatternInSwitch() {
		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static PRIM switchPRIM(PRIM in) {
						return switch (in) {
							case MAX -> NEGVAL;
							case PRIM v -> v;
						};
					}
				""";
		String callTmpl =
				"""
						PRIM vPRIM = VAL;
						System.out.print(X.switchPRIM(vPRIM));
						System.out.print('|');
						vPRIM = MAX;
						System.out.print(X.switchPRIM(vPRIM));
						System.out.print('|');
				""";
		// for all primitive types:
		for (int i = 0; i < PRIMITIVES.length; i++) {
			methods.append(fillIn(methodTmpl, i));
			calls.append(fillIn(callTmpl, i));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append("public static void main(String[] args) {\n");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() },
				"false|false|49|-1|1|-|49|-1|49|-1|49|-1|49.0|-1.0|49.0|-1.0|");
	}

	public void testPrimitivePatternInSwitch_unbox() {
		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static PRIM switchPRIM(BOX in) {
						return switch (in) {
							case MAX -> NEGVAL;
							case PRIM v -> v;
						};
					}
				""";
		String callTmpl =
				"""
						BOX vBOX = VAL;
						System.out.print(X.switchPRIM(vBOX));
						System.out.print('|');
						vBOX = MAX;
						System.out.print(X.switchPRIM(vBOX));
						System.out.print('|');
				""";
		// for all primitive types:
		for (int i = 0; i < PRIMITIVES.length; i++) {
			methods.append(fillIn(methodTmpl, i));
			calls.append(fillIn(callTmpl, i));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append("public static void main(String[] args) {\n");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() },
				"false|false|49|-1|1|-|49|-1|49|-1|49|-1|49.0|-1.0|49.0|-1.0|");
	}

	public void testPrimitivePatternInSwitch_narrowConst_NOK() {
		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static PRIM switchPRIM(Object in) {
						return switch (in) {
							case MAX -> NEGVAL;
							default -> MAX;
						};
					}
				""";
		String callTmpl =
				"""
						BOX vBOX = VAL;
						System.out.print(X.switchPRIM(vBOX));
						System.out.print('|');
						vBOX = MAX;
						System.out.print(X.switchPRIM(vBOX));
						System.out.print('|');
				""";
		// for all primitive types:
		for (int i = 0; i < PRIMITIVES.length; i++) {
			methods.append(fillIn(methodTmpl, i));
			calls.append(fillIn(callTmpl, i));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append("public static void main(String[] args) {\n");
		classX.append(calls);
		classX.append("}}\n");
		runNegativeTest(new String[] { "X.java", classX.toString() },
				"""
				----------
				1. ERROR in X.java (at line 4)
					case true -> false;
					     ^^^^
				Case constant of type boolean is incompatible with switch selector type Object
				----------
				2. ERROR in X.java (at line 10)
					case Byte.MAX_VALUE -> -1;
					     ^^^^^^^^^^^^^^
				Case constant of type byte is incompatible with switch selector type Object
				----------
				3. ERROR in X.java (at line 16)
					case 'z' -> '-';
					     ^^^
				Case constant of type char is incompatible with switch selector type Object
				----------
				4. ERROR in X.java (at line 22)
					case Short.MAX_VALUE -> -1;
					     ^^^^^^^^^^^^^^^
				Case constant of type short is incompatible with switch selector type Object
				----------
				5. ERROR in X.java (at line 28)
					case Integer.MAX_VALUE -> -1;
					     ^^^^^^^^^^^^^^^^^
				Case constant of type int is incompatible with switch selector type Object
				----------
				6. ERROR in X.java (at line 34)
					case Long.MAX_VALUE -> -1L;
					     ^^^^^^^^^^^^^^
				Case constant of type long is incompatible with switch selector type Object
				----------
				7. ERROR in X.java (at line 40)
					case Float.MAX_VALUE -> -1.0f;
					     ^^^^^^^^^^^^^^^
				Case constant of type float is incompatible with switch selector type Object
				----------
				8. ERROR in X.java (at line 46)
					case Double.MAX_VALUE -> -1.0d;
					     ^^^^^^^^^^^^^^^^
				Case constant of type double is incompatible with switch selector type Object
				----------
				""");
	}

	public void testPrimitivePatternInSwitch_narrowUnbox() {
		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String methodTmpl =
				"""
					public static PRIM switchPRIM(Object in) {
						return switch (in) {
							case PRIM v -> v;
							default -> NEGVAL;
						};
					}
				""";
		String callTmpl =
				"""
						BOX vBOX = VAL;
						System.out.print(X.switchPRIM(vBOX));
						System.out.print('|');
						System.out.print(X.switchPRIM(new Object()));
						System.out.print('|');
				""";
		// for all primitive types:
		for (int i = 0; i < PRIMITIVES.length; i++) {
			methods.append(fillIn(methodTmpl, i));
			calls.append(fillIn(callTmpl, i));
		}
		StringBuilder classX = new StringBuilder("public class X {\n");
		classX.append(methods.toString());
		classX.append("public static void main(String[] args) {\n");
		classX.append(calls);
		classX.append("}}\n");
		runConformTest(new String[] { "X.java", classX.toString() },
				"true|false|49|-1|1|-|49|-1|49|-1|49|-1|49.0|-1.0|49.0|-1.0|");
	}

	private void testPrimitivePatternInSwitch_from_unboxAndNarrow_NOK(String from, int idx, String expectedError) {
		assert from.equals(BOXES[idx]) : "mismatching from vs idx";
		StringBuilder methods = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		String classTmpl =
				"""
				public class X {
				METHODS
					public static void main(String... args) {
						FROM vFROM= VAL;
				CALLS
					}
				}
				""".replaceAll("FROM", from).replace("VAL", GOODVALUES[idx]);
		String methodTmpl =
				"""
					public static PRIM switchPRIM(FROM in) {
						return switch (in) {
							case PRIM v -> v;
							default -> throw new RuntimeException();
						};
					}
				""".replace("FROM", from);
		String callTmpl =
				"""
						System.out.print(X.switchPRIM(vFROM));
						System.out.print('|');
				""".replaceAll("FROM", from); // leaves only PRIM for replacement in the loop
		// for all smaller numerical primitive types:
		for (int i = 1; i < idx; i++) {
			if (!IS_NUMERICAL[i]) continue;
			methods.append(fillIn(methodTmpl, i));
			calls.append(fillIn(callTmpl, i));
		}
		String classX = classTmpl.replace("METHODS", methods.toString()).replaceAll("CALLS", calls.toString());
		runNegativeTest(new String[] { "X.java", classX },
				expectedError);
	}
	public void testPrimitivePatternInSwitchShort_unboxAndNarrow_NOK() {
		testPrimitivePatternInSwitch_from_unboxAndNarrow_NOK("Short", 3,
				"""
				----------
				1. ERROR in X.java (at line 4)
					case byte v -> v;
					     ^^^^^^
				Type mismatch: cannot convert from Short to byte
				----------
				""");
	}
	public void testPrimitivePatternInSwitchInt_unboxAndNarrow_NOK() {
		testPrimitivePatternInSwitch_from_unboxAndNarrow_NOK("Integer", 4,
				"""
				----------
				1. ERROR in X.java (at line 4)
					case byte v -> v;
					     ^^^^^^
				Type mismatch: cannot convert from Integer to byte
				----------
				2. ERROR in X.java (at line 10)
					case short v -> v;
					     ^^^^^^^
				Type mismatch: cannot convert from Integer to short
				----------
				""");
	}
	public void testPrimitivePatternInSwitchLong_unboxAndNarrow_NOK() {
		testPrimitivePatternInSwitch_from_unboxAndNarrow_NOK("Long", 5,
				"""
				----------
				1. ERROR in X.java (at line 4)
					case byte v -> v;
					     ^^^^^^
				Type mismatch: cannot convert from Long to byte
				----------
				2. ERROR in X.java (at line 10)
					case short v -> v;
					     ^^^^^^^
				Type mismatch: cannot convert from Long to short
				----------
				3. ERROR in X.java (at line 16)
					case int v -> v;
					     ^^^^^
				Type mismatch: cannot convert from Long to int
				----------
				""");
	}
	public void testPrimitivePatternInSwitchFloat_unboxAndNarrow_NOK() {
		testPrimitivePatternInSwitch_from_unboxAndNarrow_NOK("Float", 6,
				"""
				----------
				1. ERROR in X.java (at line 4)
					case byte v -> v;
					     ^^^^^^
				Type mismatch: cannot convert from Float to byte
				----------
				2. ERROR in X.java (at line 10)
					case short v -> v;
					     ^^^^^^^
				Type mismatch: cannot convert from Float to short
				----------
				3. ERROR in X.java (at line 16)
					case int v -> v;
					     ^^^^^
				Type mismatch: cannot convert from Float to int
				----------
				4. ERROR in X.java (at line 22)
					case long v -> v;
					     ^^^^^^
				Type mismatch: cannot convert from Float to long
				----------
				""");
	}
	public void testPrimitivePatternInSwitchDouble_unboxAndNarrow_NOK() {
		testPrimitivePatternInSwitch_from_unboxAndNarrow_NOK("Double", 7,
				"""
				----------
				1. ERROR in X.java (at line 4)
					case byte v -> v;
					     ^^^^^^
				Type mismatch: cannot convert from Double to byte
				----------
				2. ERROR in X.java (at line 10)
					case short v -> v;
					     ^^^^^^^
				Type mismatch: cannot convert from Double to short
				----------
				3. ERROR in X.java (at line 16)
					case int v -> v;
					     ^^^^^
				Type mismatch: cannot convert from Double to int
				----------
				4. ERROR in X.java (at line 22)
					case long v -> v;
					     ^^^^^^
				Type mismatch: cannot convert from Double to long
				----------
				5. ERROR in X.java (at line 28)
					case float v -> v;
					     ^^^^^^^
				Type mismatch: cannot convert from Double to float
				----------
				""");
	}
	public void testPrimitivePatternInSwitch_Character_unboxAndNarrow_NOK() {
		runNegativeTest(new String[] {
				"X.java",
				"""
				public class X {
					static int m1(Character ch) {
						return switch(ch) {
							case byte b -> b;
							case short s -> s;
							case char c -> c;
						};
					}
				}
				"""
			},
			"""
			----------
			1. ERROR in X.java (at line 4)
				case byte b -> b;
				     ^^^^^^
			Type mismatch: cannot convert from Character to byte
			----------
			2. ERROR in X.java (at line 5)
				case short s -> s;
				     ^^^^^^^
			Type mismatch: cannot convert from Character to short
			----------
			""");
	}

	public void testPrimitivePatternInSwitch_Character_unboxAndWiden() {
		runConformTest(new String[] {
				"X.java",
				"""
				public class X {
					static int mint(Character ch) {
						return switch(ch) {
							case int v -> v;
						};
					}
					static long mlong(Character ch) {
						return switch(ch) {
							case long v -> v;
						};
					}
					static float mfloat(Character ch) {
						return switch(ch) {
							case float v -> v;
						};
					}
					static double mdouble(Character ch) {
						return switch(ch) {
							case double v -> v;
						};
					}
					public static void main(String... args) {
						System.out.print(mint('a'));
						System.out.print('|');
						System.out.print(mlong('b'));
						System.out.print('|');
						System.out.print(mfloat('c'));
						System.out.print('|');
						System.out.print(mdouble('d'));
					}
				}
				"""
			},
			"97|98|99.0|100.0");
	}

	public void testPrimitivePatternInSwitch_widenUnbox() {
		runConformTest(new String[] {
				"X.java",
				"""
				import java.util.Optional;
				public class X {
					static <T extends Integer> int mInteger(T in) {
						return switch (in) {
							case int v -> v;
							default -> -1;
						};
					}
					static int mShort(Optional<? extends Short> in) {
						return switch (in.get()) {
							case int v -> v;
							default -> -1;
						};
					}
					public static void main(String... args) {
						System.out.print(mInteger(Integer.valueOf(1)));
						System.out.print(mShort(Optional.of(Short.valueOf((short) 2))));
					}
				}
				"""
			},
			"12");
	}
	void testInstanceof_widenUnbox(String fromBox, int idx, String expectedOuts) {
		// for all numerical types challenge route WIDENING_REFERENCE_AND_UNBOXING_COVERSION_AND_WIDENING_PRIMITIVE_CONVERSION
		//
		// example (from="Integer", idx=4, ...):
		// class X {
		// 		// alternating for type variable (m1*) and wildcard (m2*):
		//		static <T extends Integer> long m1long(T in) {
		//			if (in instanceof long v) return v;
		//			return -1L;
		//		}
		//		static long m2Long(Optional<? extends Integer> in) {
		//			if (in.get() instanceof long v) return v;
		//			return -1L;
		//		}
		//		static <T extends Integer> float m1float(T in) {
		//			if (in instanceof float v) return v;
		//			return -1.0f;
		//		}
		// 		...
		//		public static void main(String... args) {
		//				Integer v = Integer.valueOf((int) 49);
		//				System.out.print(m1long(v));
		//				System.out.print('+');
		//				System.out.print(Optional.of(m2long(v));
		//				System.out.print('|');
		//				System.out.print(m1float(v));
		//				System.out.print('|');
		//				...
		//		}
		// }
		String m1 = """
				static <T extends FROM> PRIM m1PRIM(T in) {
					if (in instanceof PRIM v) return v;
					return NEGVAL;
				}
				""".replace("FROM", fromBox);
		String m2 = """
				static PRIM m2PRIM(Optional<? extends FROM> in) {
					if (in.get() instanceof PRIM v) return v;
					return NEGVAL;
				}
				""".replace("FROM", fromBox);
		StringBuilder clazz = new StringBuilder();
		clazz.append("""
				import java.util.Optional;
				public class X {
				""");
		StringBuilder main = new StringBuilder();
		main.append("public static void main(String... args) {\n");
		main.append("\tFROM v = FROM.valueOf((CAST) VAL);\n"
				.replace("FROM", fromBox)
				.replace("CAST", PRIMITIVES[idx])
				.replace("VAL", GOODVALUES[idx]));
		String call1Tmpl = "\tSystem.out.print(m1PRIM(v));\n".replace("FROM", fromBox);
		String call2Tmpl = "\tSystem.out.print(m2PRIM(Optional.of(v)));\n".replace("FROM", fromBox);
		for (int i=idx+1; i<8; i++) {
			if (!IS_NUMERICAL[i]) continue;
			clazz.append(fillIn(m1, i));
			clazz.append(fillIn(m2, i));
			main.append(fillIn(call1Tmpl, i));
			main.append("\tSystem.out.print('+');\n");
			main.append(fillIn(call2Tmpl, i));
			main.append("\tSystem.out.print('|');\n");
		}
		clazz.append(main);
		clazz.append("""
					}
				}
				""");
		runConformTest(new String[] {"X.java", clazz.toString()}, expectedOuts, getCompilerOptions(true), VMARGS, JavacHasABug.JavacBug8341408);
	}
	public void testInstanceof_widenUnbox_Byte() {
		testInstanceof_widenUnbox("Byte", 1, "49+49|49+49|49+49|49.0+49.0|49.0+49.0|");
	}
	public void testInstanceof_widenUnbox_Short() {
		testInstanceof_widenUnbox("Short", 3, "49+49|49+49|49.0+49.0|49.0+49.0|");
	}
	public void testInstanceof_widenUnbox_Integer() {
		testInstanceof_widenUnbox("Integer", 4, "49+49|49.0+49.0|49.0+49.0|");
	}
	public void testInstanceof_widenUnbox_Long() {
		testInstanceof_widenUnbox("Long", 5, "49.0+49.0|49.0+49.0|");
	}
	public void testInstanceof_widenUnbox_Float() {
		testInstanceof_widenUnbox("Float", 6, "49.0+49.0|");
	}

	public void testInstanceof_genericExpression() { // regression test for a checkCast which we failed to generate earlier
		runConformTest(new String[] {
				"X.java",
				"""
				import java.util.List;
				import java.util.Collections;
				public class X {
					static int mInteger(List<Integer> in) {
						if (in.get(0) instanceof int v) // pattern is total, still the cast to Integer must be generated
							return v;
						return -1;
					}
					public static void main(String... args) {
						System.out.print(mInteger(Collections.singletonList(Integer.valueOf(1))));
					}
				}
				"""
			},
			"1");
	}
	public void testPrimitivePatternInSwitch_more() {
		runConformTest(new String[] {
				"X.java",
				"""
				public class X {
					public static String switchbool(boolean in) {
						// generic test couldn't differentiate cases by output
						return switch (in) {
							case true -> "true";
							case boolean v -> "v="+String.valueOf(v);
						};
					}
					public static String switchfloatMoreCases(float f) {
						return switch (f) {
						    case float v when v == 1.6 -> "v="+String.valueOf(v);
							case 1.0f -> "1.0";
							case 1.5f -> "1.5";
							case float v -> "v="+String.valueOf(v);
						};
					}
					public static char switchByteToChar(byte b) {
						return switch (b) {
							case '1' -> 'A';
							case char c -> c;
							default -> '_';
						};
					}
					public static void main(String... args) {
						System.out.print(switchbool(true));
						System.out.print("|");
						System.out.print(switchbool(false));
						System.out.print("|");
						System.out.print(switchfloatMoreCases(1.0f));
						System.out.print("|");
						System.out.print(switchfloatMoreCases(1.5f));
						System.out.print("|");
						System.out.print(switchfloatMoreCases(1.6f));
						System.out.print("|");
						System.out.print(switchByteToChar((byte) 49));
						System.out.print("|");
						System.out.print(switchByteToChar((byte) 50));
						System.out.print("|");
						System.out.print(switchByteToChar((byte) -1));
					}
				}
				"""},
				"true|v=false|1.0|1.5|v=1.6|A|2|_");
	}

	public void testPrimitivePatternInSwitch_byteToChar_notExhaustive() {
		runNegativeTest(new String[] {
				"X.java",
				"""
				public class X {
					public static char switchByteToChar(byte b) {
						return switch (b) {
							case '1' -> 'A';
							case char c -> c;
						};
					}
				}
				"""},
				"""
				----------
				1. ERROR in X.java (at line 3)
					return switch (b) {
					               ^
				A switch expression should have a default case
				----------
				""");
	}

	private void testNarrowingInSwitchFrom(String from, int idx, String expectedOut) {
		// case statements (constant & type pattern) apply narrowing to each smaller numerical type

		assert from.equals(PRIMITIVES[idx]) : "mismatch between from and idx";
		// example (from="short", idx=3):
		//	public class X {
		//		public static int doswitch(short v) { // return type: at least 'int' or <from>
		//			return switch(v) {
		//				case 1 -> 10;
		//				case byte vv -> 10+vv;
		//				case 300 -> 30;
		//				case short vv -> 30+vv;
		//			}
		//		}
		//		static void print(Object o) {
		//			System.out.print(o);
		//			System.out.print('|');
		//		}
		//		public static void main(String[] args) {
		//			print(X.doswitch((short)1);
		//			print(X.doswitch((short)(1+1));
		//			print(X.doswitch((short)300);
		//			print(X.doswitch((short)(300+300));
		//		}
		//	}

		String classTmpl =
				"""
				public class X {
					public static RET doswitch(FROM v) {
						return switch(v) {
				BODY
						};
					}
					static void print(Object o) {
						System.out.print(o);
						System.out.print('|');
					}
					public static void main(String[] args) {
				CALLS
					}
				}
				""";
		String casesTmpl =
				"""
							case CONST -> VAL;
							case PRIM vv -> VAL+vv;
				""";
		String callsTmpl =
				"""
						print(X.doswitch((FROM)CONST));
						print(X.doswitch((FROM)(CONST+CONST)));
				""";
		// for all numerical primitive types up-to 'from':
		StringBuilder cases = new StringBuilder();
		StringBuilder calls = new StringBuilder();
		for (int i = 0; i <= idx; i++) {
			if (!IS_NUMERICAL[i]) continue;
			String constVal = toConstantOfType(CONSTANTS[i], from);
			String val10 = String.valueOf(i*10);
			cases.append(casesTmpl.replaceAll("PRIM", PRIMITIVES[i]).replace("CONST", constVal).replace("VAL", val10));
			calls.append(callsTmpl.replaceAll("FROM", from).replaceAll("CONST", constVal));
		}
		String retType = idx <= 4 /*int*/ ? "int" : from; // no syntax exists for constants below int
		String classX = classTmpl.replace("FROM", from).replace("RET", retType)
					.replace("BODY", cases.toString())
					.replace("CALLS", calls.toString());
		runConformTest(new String[] { "X.java", classX }, expectedOut);
	}
	public void testNarrowingInSwitchFromShort() {
		testNarrowingInSwitchFrom("short", 3, "10|12|30|630|");
	}
	public void testNarrowingInSwitchFromInt() {
		testNarrowingInSwitchFrom("int", 4, "10|12|30|630|40|80040|");
	}
	public void testNarrowingInSwitchFromLong() {
		testNarrowingInSwitchFrom("long", 5, "10|12|30|630|40|80040|50|10000000050|");
	}
	public void testNarrowingInSwitchFromFloat() {
		testNarrowingInSwitchFrom("float", 6, "10.0|12.0|30.0|630.0|40.0|80040.0|50.0|1.0E10|60.0|1.2E21|");
	}
	public void testNarrowingInSwitchFromDouble() {
		testNarrowingInSwitchFrom("double", 7, "10.0|12.0|30.0|630.0|40.0|80040.0|50.0|1.000000005E10|60.0|1.2E21|70.0|1.4E41|");
	}

	public void testSwitchOn_long_wrongSelector() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public class X {
				int m1(long in) {
					return switch(in) {
						case 1 -> 1;
						case 'a' -> 2;
						case 3L -> 3;
						case 4.0f -> 4;
						case 5.0d -> 5;
						default -> -1;
					};
				}
			}
			"""
		},
		"""
		----------
		1. ERROR in X.java (at line 4)
			case 1 -> 1;
			     ^
		Case constants in a switch on 'long' must have type 'long'
		----------
		2. ERROR in X.java (at line 5)
			case 'a' -> 2;
			     ^^^
		Case constants in a switch on 'long' must have type 'long'
		----------
		3. ERROR in X.java (at line 7)
			case 4.0f -> 4;
			     ^^^^
		Case constants in a switch on 'long' must have type 'long'
		----------
		4. ERROR in X.java (at line 8)
			case 5.0d -> 5;
			     ^^^^
		Case constants in a switch on 'long' must have type 'long'
		----------
		""");
	}
	public void testSwitchOn_Float_wrongSelector() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public class X {
				int m1(Float in) {
					return switch(in) {
						case 1 -> 1;
						case 'a' -> 2;
						case 3L -> 3;
						case 4.0f -> 4;
						case 5.0d -> 5;
						default -> -1;
					};
				}
			}
			"""
		},
		"""
		----------
		1. ERROR in X.java (at line 4)
			case 1 -> 1;
			     ^
		Case constants in a switch on 'Float' must have type 'float'
		----------
		2. ERROR in X.java (at line 5)
			case 'a' -> 2;
			     ^^^
		Case constants in a switch on 'Float' must have type 'float'
		----------
		3. ERROR in X.java (at line 6)
			case 3L -> 3;
			     ^^
		Case constants in a switch on 'Float' must have type 'float'
		----------
		4. ERROR in X.java (at line 8)
			case 5.0d -> 5;
			     ^^^^
		Case constants in a switch on 'Float' must have type 'float'
		----------
		""");
	}
	public void testSwitchOnBoxed_OK() {
		// constant cases for all boxed primitive types except Boolean
		// run as separate tests.
		String classTmpl = """
				public class XBOX {
					static int m1(BOX in) {
						return switch(in) {
							case VAL -> 1;
							case MAX -> 2;
							default -> -2;
						};
					}
					public static void main(String... args) {
				CALLS
					}
				}
				""";
		String callsTmpl =
				"""
						System.out.print(m1((PRIM)VAL));
						System.out.print(m1((PRIM)MAX));
						System.out.print(m1((PRIM)NEGVAL));
				""";
		// for all primitive types other than boolean (boolean would have duplicate cases):
		for (int i = 1; i < PRIMITIVES.length; i++) { // 1
			String calls = fillIn(callsTmpl, i);
			String classX = fillIn(classTmpl, i)
					.replace("CALLS", calls);
			runConformTest(new String[] { "XBOX.java".replace("BOX", BOXES[i]), classX }, "12-2");
		}
	}
	public void testSwitchOn_Boolean_OK() {
		runConformTest(new String[] {
			"X.java",
			"""
			public class X {
				static int m1(Boolean in) {
					return switch(in) {
						case true-> 1;
						default -> -1;
					};
				}
				public static void main(String... args) {
					System.out.print(m1(true));
					System.out.print(m1(false));
				}
			}
			"""
		},
		"1-1");
	}

	public void testDuplicateBoolCase() {
		// saw SOE when executing bogus byte code:
		runNegativeTest(new String[] {
				"XBoolean.java",
				"""
				public class XBoolean {
					static int m1(Boolean in) {
						return switch(in) {
							case true -> 1;
							case true -> 2;
							default -> -1;
						};
					}
					public static void main(String... args) {
						System.out.print(m1(true));
						System.out.print(m1(true));
						System.out.print(m1(false));

					}
				}
				"""
			},
			"""
			----------
			1. ERROR in XBoolean.java (at line 5)
				case true -> 2;
				     ^^^^
			Duplicate case
			----------
			""");
	}
	public void testBooleanSwitchExhaustive_OK() {
		runConformTest(new String[] {
				"X.java",
				"""
				public class X {
					static int m1(boolean b) {
						return switch (b) {
							case true -> 1;
							case false -> 0;
						};
					}
					public static void main(String... args) {
						System.out.print(m1(true));
						System.out.print(m1(false));
					}
				}
				"""
			},
			"10");
	}
	public void testBooleanSwitchExhaustive_NOK_1() {
		runNegativeTest(new String[] {
				"X.java",
				"""
				public class X {
					static int m1(boolean b) {
						return switch (b) {
							case true -> 1;
						};
					}
				}
				"""
			},
			"""
			----------
			1. ERROR in X.java (at line 3)
				return switch (b) {
				               ^
			A switch expression should have a default case
			----------
			""");
	}
	public void testBooleanSwitchExhaustive_NOK_2() {
		runNegativeTest(new String[] {
				"X.java",
				"""
				public class X {
					static int m1(boolean b) {
						return switch (b) {
							case true -> 1;
							case false -> 2;
							default -> 3;
						};
					}
				}
				"""
			},
			"""
			----------
			1. ERROR in X.java (at line 3)
				return switch (b) {
						case true -> 1;
						case false -> 2;
						default -> 3;
					};
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Switch cannot have both boolean values and a default label
			----------
			""");
	}
	public void testBooleanSwitchExhaustive_NOK_3() {
		runNegativeTest(new String[] {
				"X.java",
				"""
				public class X {
					public void foo(Boolean b) {
						final boolean TRUE = true;
						final boolean FALSE = false;
						switch (b) {
							case TRUE -> { break;}
							case FALSE -> { break;}
							default -> { break;}
						}
					}
				}
				"""
			},
			"""
			----------
			1. ERROR in X.java (at line 5)
				switch (b) {
						case TRUE -> { break;}
						case FALSE -> { break;}
						default -> { break;}
					}
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Switch cannot have both boolean values and a default label
			----------
			""");
	}

	// exhaustiveness with identity conversion is already cover testNarrowingInSwitchFrom()

	public void testShortSwitchExhaustive_int_Number_Comparable() {
		runConformTest(new String[] {
				"X.java",
				"""
				public class X {
					static int m1(short s) {
						return switch (s) {
							case 1 -> 0;
							case int v -> v*2;
						};
					}
					static int m2(short s) {
						return switch (s) {
							case 1 -> 0;
							case Number v -> v.intValue()*2;
						};
					}
					static int m3(short s) {
						return switch (s) {
							case 1 -> 0;
							case Comparable<?> v -> humbug(v);
						};
					}
					static int humbug(Comparable<?> v) {
						return 8;
					}
					public static void main(String... args) {
						System.out.print(m1((short) 1));
						System.out.print(m1((short) 4));
						System.out.print(m2((short) 1));
						System.out.print(m2((short) 4));
						System.out.print(m3((short) 1));
						System.out.print(m3((short) 4));
					}
				}
				"""
			},
			"080808");
	}

	public void testIntSwitchExhaustive_NOK() {
		runNegativeTest(new String[] {
				"X.java",
				"""
				public class X {
					static float m1(Integer i) {
						return switch(i) {
							case 1 -> 1.0f;
							case float f -> f;
						};
					}
					static float m2(int i) {
						return switch(i) {
							case 1 -> 1;
							case float f -> f;
						};
					}
				}
				"""
			},
			"""
			----------
			1. ERROR in X.java (at line 3)
				return switch(i) {
				              ^
			A switch expression should have a default case
			----------
			2. ERROR in X.java (at line 9)
				return switch(i) {
				              ^
			A switch expression should have a default case
			----------
			""");
	}

	public void testIntSwitchExhaustive_OK() {
		runConformTest(new String[] {
				"X.java",
				"""
				public class X {
					static double m1(Integer i) {
						return switch(i) {
							case 1 -> 1.0f;
							case double d -> d;
						};
					}
					static double m2(int i) {
						return switch(i) {
							case 1 -> 1;
							case double d -> d;
						};
					}
					public static void main(String... args) {
						System.out.print(m1(1));
						System.out.print('|');
						System.out.print(m1(3));
						System.out.print('|');
						System.out.print(m2(1));
						System.out.print('|');
						System.out.print(m2(3));
					}
				}
				"""
			},
			"1.0|3.0|1.0|3.0");
	}

	public void testLongSwitchExhaustive_NOK() {
		runNegativeTest(new String[] {
				"X.java",
				"""
				public class X {
					static float m1(Long l) {
						return switch(l) {
							case 1L -> 1.0f;
							case float f -> f;
						};
					}
					static double m2(long l) {
						return switch(l) {
							case 1L -> 1.0d;
							case double d -> d;
						};
					}
				}
				"""
			},
			"""
			----------
			1. ERROR in X.java (at line 3)
				return switch(l) {
				              ^
			A switch expression should have a default case
			----------
			2. ERROR in X.java (at line 9)
				return switch(l) {
				              ^
			A switch expression should have a default case
			----------
			""");
	}

	public void testPrimitiveRecordComponent_narrow() {
		runConformTest(new String[] {
				"X.java",
				"""
				record RforRecord(long x) {}
				public class X  {
					public void foo() {
						if (new RforRecord(0L) instanceof RforRecord(int y)) {
							System.out.print("Yay");
						}
						if (new RforRecord(5000000000L) instanceof RforRecord(int y)) {
							System.out.print("Nay");
						} else {
							System.out.print("!");
						}
					}
					public static void main(String... args) {
						new X().foo();
					}
				}
				"""},
				"Yay!");
	}

	public void testPrimitiveRecordComponent_unbox() {
		runConformTest(new String[] {
				"X.java",
				"""
				record RforRecord(Long x) {}
				public class X  {
					public void foo() {
						if (new RforRecord(0L) instanceof RforRecord(long y)) {
							System.out.print("Yay");
						}
					}
					public static void main(String... args) {
						new X().foo();
					}
				}
				"""},
				"Yay");
	}

	public void testPrimitiveRecordComponent_unboxAndWiden() {
		runConformTest(new String[] {
				"X.java",
				"""
				record RforRecord(Integer x) {}
				public class X  {
					public void foo() {
						if (new RforRecord(0) instanceof RforRecord(long y)) {
							System.out.print("Yay");
						}
					}
					public static void main(String... args) {
						new X().foo();
					}
				}
				"""},
				"Yay");
	}

	public void testPrimitiveRecordComponent_narrowingAndUnboxing_nested() {
		runConformTest(new String[] {
				"X.java",
				"""
				record Rec1(Rec2 r) {}
				record Rec2(Number x) {}
				public class X  {
					public void foo() {
						if (new Rec1(new Rec2(Integer.valueOf(1))) instanceof Rec1(Rec2(int y))) {
							System.out.print("Yay"+y);
						}
						if (new Rec1(new Rec2(Short.valueOf((short)1))) instanceof Rec1(Rec2(int y))) {
							System.out.print("Nay");
						} else {
							System.out.print("!");
						}
					}
					public static void main(String... args) {
						new X().foo();
					}
				}
				"""},
				"Yay1!");

	}

	public void testCoversTypePlusDefault() {
		// case int i "covers" type Integer but is not unconditional
		runConformTest(new String[] {
				"X.java",
				"""
				public class X  {
					public static int foo(Integer myInt) {
						return switch (myInt) {
							case int i  -> i;
							default -> 0;
						};
					}

					public static void main(String argv[]) {
						Integer i = 100;
						System.out.println(X.foo(i) == i);
					}
				}
				"""
				},
				"true");
	}

	public void testUnconditionPlusDefault() {
		// case int i "covers" type int and is unconditional
		// various combinations of dominance with/without default
		runNegativeTest(new String[] {
				"X.java",
				"""
				public class X  {
					int foo1(int myInt) {
						return switch (myInt) {
							case int i  -> i;
							default -> 0; // conflict with preceding total pattern (unguarded and unconditional)
						};
					}
					int foo2(int myInt) {
						return switch (myInt) { // swapped order of cases
							default -> 0;
							case int i  -> i; // conflict with preceding default
						};
					}
					int foo3(int myInt) {
						return switch (myInt) {
							default -> 0;
							case int i  -> i; // conflict with preceding default
							case short s -> s; // additionally dominated by int i
						};
					}
					int foo4(int myInt) {
						return switch (myInt) {
							case int i  -> i;
							case short s -> s; // dominated by int i
						};
					}
				}
				"""
				},
				"""
				----------
				1. ERROR in X.java (at line 5)
					default -> 0; // conflict with preceding total pattern (unguarded and unconditional)
					^^^^^^^
				Switch case cannot have both unconditional pattern and default label
				----------
				2. ERROR in X.java (at line 11)
					case int i  -> i; // conflict with preceding default
					     ^^^^^
				This case label is dominated by one of the preceding case labels
				----------
				3. ERROR in X.java (at line 17)
					case int i  -> i; // conflict with preceding default
					     ^^^^^
				This case label is dominated by one of the preceding case labels
				----------
				4. ERROR in X.java (at line 18)
					case short s -> s; // additionally dominated by int i
					     ^^^^^^^
				This case label is dominated by one of the preceding case labels
				----------
				5. ERROR in X.java (at line 24)
					case short s -> s; // dominated by int i
					     ^^^^^^^
				This case label is dominated by one of the preceding case labels
				----------
				""");
	}

	public void testIncompatiblePrimitiveInInstanceof() {
		runNegativeTest(new String[] {
				"X.java",
				"""
				public class X  {
					void foo() {
						if (this instanceof int i)
							return;
					}
				}
				"""
				},
				"""
				----------
				1. ERROR in X.java (at line 3)
					if (this instanceof int i)
					    ^^^^^^^^^^^^^^^^^^^^^
				Incompatible conditional operand types X and int
				----------
				""");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3113
	// [Switch][Record patterns] Unexpected operand error with switch pattern and widening unboxing conversion
	public void testGH3113_ok() {
		runConformTest(new String[] {
				"X.java",
				"""
				record Record<T extends Integer>(T t) {}
				public class X {
					public static <T extends Integer> double convert(Record<T> r) {
						return switch (r) {
						case Record(double d) -> d;
						default -> 2;
						};
					}
					public static void main(String[] args) {
						System.out.print(convert(new Record(2)));
					}
				}
				"""
			},
			"2.0");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3265
	// [Primitive Patterns] Wrong duplicate case error
	public void testIssue3265() {
		runConformTest(new String[] {
				"X.java",
				"""
				public class X {
					public static String switchfloatMoreCases(float f) {
						return switch (f) {
						case 1.0f -> "1.0";
						case 1.5f -> "1.5";
						default -> String.valueOf(f);
						};
					}

					public static void main(String... args) {
						System.out.print(switchfloatMoreCases(1.0f));
						System.out.print("|");
						System.out.print(switchfloatMoreCases(1.5f));
						System.out.print("|");
						System.out.print(switchfloatMoreCases(1.6f));
						System.out.print("|");
					}
				}
				"""},
				"1.0|1.5|1.6|");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3265
	// [Primitive Patterns] Wrong duplicate case error
	public void testIssue3265_2() {
		runNegativeTest(new String[] {
				"X.java",
				"""
				public class X {
					public static String switchfloatMoreCases(float f) {
						return switch (f) {
						case 1.0f -> "1.0";
						case 0.5f + 0.5f -> "1.0";
						default -> String.valueOf(f);
						};
					}

					public static void main(String... args) {
						System.out.print(switchfloatMoreCases(1.0f));
						System.out.print("|");
						System.out.print(switchfloatMoreCases(1.5f));
						System.out.print("|");
						System.out.print(switchfloatMoreCases(1.6f));
						System.out.print("|");
					}
				}
				"""},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	case 0.5f + 0.5f -> \"1.0\";\n" +
				"	     ^^^^^^^^^^^\n" +
				"Duplicate case\n" +
				"----------\n");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3337
	// [Enhanced Switch][Primitive Patterns] ECJ tolerates default case in boolean switch with both true and false cases.
	public void testIssue3337() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public class X {

				public static void main(String[] args) {
					Boolean b = true;
					switch (b) {
						case true -> System.out.println(1);
						case false -> System.out.println(0);
						case null, default -> System.out.println("Error");
					}
				}
			}
			"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\r\n" +
			"	switch (b) {\n" +
			"			case true -> System.out.println(1);\n" +
			"			case false -> System.out.println(0);\n" +
			"			case null, default -> System.out.println(\"Error\");\n" +
			"		}\r\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Switch cannot have both boolean values and a default label\n" +
			"----------\n");
	}

}
