/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
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

import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * Guards against super-linear blow-up of JLS 18 type inference.
 * <p>
 * The test compiles a single generic invocation that takes a large number of generic
 * invocations as its arguments, so that one inference context has to resolve thousands of
 * mutually dependent inference variables. It then asserts that this stays within a time
 * budget. The budget is deliberately very generous and is scaled with the speed of the
 * machine, which is derived from compiling a small variant of the very same construct.
 * </p>
 * <p>
 * Reference measurements (n=500): ~0.4s with the fix for
 * https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5441, ~16s without it. Note that a
 * plain time <em>ratio</em> between two sizes is not usable here, because the constant
 * per-compilation overhead (a few tens of milliseconds) varies with the JIT state and would
 * dominate the small measurement.
 * </p>
 */
@SuppressWarnings({ "rawtypes" })
public class InferenceScalabilityTest extends AbstractRegressionTest {

	/** Number of poly-expression arguments used to calibrate the speed of the machine. */
	private static final int SMALL = 50;
	/** Number of poly-expression arguments under test - the size reported in issue 5441. */
	private static final int LARGE = 500;
	/** Budget for LARGE, relative to SMALL; only relevant on slow machines. */
	private static final long RELATIVE_BUDGET = 60;
	/** Budget for LARGE on a fast machine, in milliseconds. */
	private static final long MINIMUM_BUDGET_MS = 4000;

	public InferenceScalabilityTest(String name) {
		super(name);
	}

	public static Class testClass() {
		return InferenceScalabilityTest.class;
	}

	public static Test suite() {
		// a pure timing guard - running it once is enough, no need to repeat per compliance level
		return buildUniqueComplianceTestSuite(testClass(), CompilerOptions.getFirstSupportedJdkLevel());
	}

	/**
	 * A single generic invocation whose arguments are all generic invocations themselves,
	 * i.e. one inference context holding 2 + 2*count mutually dependent inference variables.
	 * Deliberately uses only self-declared API, so that the test is independent of the JRE it
	 * happens to run on.
	 */
	private String sourceWithNestedGenericInvocations(int count) {
		StringBuilder buf = new StringBuilder(count * 32);
		buf.append("import java.util.Map;\n");
		buf.append("public class X {\n");
		buf.append("	static class Entry<K,V> {}\n");
		buf.append("	static <K,V> Entry<K,V> entry(K k, V v) { return null; }\n");
		buf.append("	@SafeVarargs\n");
		buf.append("	static <K,V> Map<K,V> ofEntries(Entry<? extends K, ? extends V>... entries) { return null; }\n");
		buf.append("	static final Map<String,String> MAP = ofEntries(\n");
		for (int i = 0; i < count; i++) {
			buf.append("		entry(\"k").append(i).append("\", \"v").append(i).append("\")");
			buf.append(i < count - 1 ? ",\n" : "\n");
		}
		buf.append("	);\n");
		buf.append("}\n");
		return buf.toString();
	}

	private long timeCompilationMillis(int count) {
		String[] testFiles = new String[] { "X.java", sourceWithNestedGenericInvocations(count) };
		long start = System.nanoTime();
		runConformTest(testFiles);
		return (System.nanoTime() - start) / 1_000_000L;
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5441
	// inference must not degrade super-linearly with the number of poly arguments
	public void testIssue5441_manyNestedGenericInvocations() {
		if (RUN_JAVAC || RUN_JAVAC_OPT_IN)
			return; // comparing against javac would dominate and distort the measurement

		for (int i = 0; i < 2; i++)
			timeCompilationMillis(SMALL); // warm up JIT, JCL name environment and output directory

		long small = Long.MAX_VALUE;
		for (int i = 0; i < 3; i++)
			small = Math.min(small, timeCompilationMillis(SMALL));
		long budget = Math.max(MINIMUM_BUDGET_MS, RELATIVE_BUDGET * small);

		long large = timeCompilationMillis(LARGE);
		if (large > budget) // the machine may just have been busy, give it one more chance
			large = Math.min(large, timeCompilationMillis(LARGE));

		String measurement = "compiling " + SMALL + " nested generic invocations took " + small
				+ "ms, compiling " + LARGE + " took " + large + "ms, budget was " + budget + "ms";
		System.out.println(testClass().getSimpleName() + ": " + measurement);
		assertTrue("Type inference does not scale: " + measurement, large <= budget);
	}

	public void testIssue5627_nestedCompletableFutureThenCompose() {
		if (RUN_JAVAC || RUN_JAVAC_OPT_IN)
			return;

		String source = """
			import java.util.*;
			import java.util.concurrent.*;
			import java.util.stream.*;

			public class X {
			  record Result(Object v0, Object v1, Object v2, Object v3, Object v4, Object v5, Object v6, Object v7, Object v8, Object v9, Object v10, Object v11) {}

			  static <T> CompletableFuture<T> future(T value) { return CompletableFuture.completedFuture(value); }

			  CompletionStage<Result> build() {
			    final CompletableFuture<List<String>> s0 = future(List.of());
			    final CompletionStage<Map<Long, Set<String>>> s1 = future(Map.of());
			    final CompletableFuture<Optional<Long>> s2 = future(Optional.empty());
			    final CompletionStage<Set<Long>> s3 = future(Set.of());
			    final CompletableFuture<List<String>> s4 = future(List.of());
			    final CompletionStage<Map<Long, Set<String>>> s5 = future(Map.of());
			    final CompletableFuture<Optional<Long>> s6 = future(Optional.empty());
			    final CompletionStage<Set<Long>> s7 = future(Set.of());
			    final CompletableFuture<List<String>> s8 = future(List.of());
			    final CompletionStage<Map<Long, Set<String>>> s9 = future(Map.of());
			    final CompletableFuture<Optional<Long>> s10 = future(Optional.empty());
			    final CompletionStage<Set<Long>> s11 = future(Set.of());
			    return s0.thenCompose(v0 -> s1.thenCompose(v1 -> s2.thenCompose(v2 -> s3.thenCompose(v3 -> s4.thenCompose(v4 -> s5.thenCompose(v5 -> s6.thenCompose(v6 -> s7.thenCompose(v7 -> s8.thenCompose(v8 -> s9.thenCompose(v9 -> s10.thenCompose(v10 -> s11.thenApply(v11 -> {
			        final List<Object> all = Stream.of(v0, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11).map(Object::toString).collect(Collectors.toList());
			        return new Result(v0, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11);
			      }))))))))))));
			  }
			}
			""";

		java.util.Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_17);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_17);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_17);

		final long budgetNanos = java.util.concurrent.TimeUnit.SECONDS.toNanos(15);
		final long start = System.nanoTime();
		long elapsedNanos;
		String measurement;
		try {
			runConformTest(new String[] { "X.java", source }, options);
		} finally {
			elapsedNanos = System.nanoTime() - start;
			measurement = "issue 5627: 12 nested CompletionStage lambdas, elapsed="
					+ java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(elapsedNanos)
					+ "ms, budget="
					+ java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(budgetNanos)
					+ "ms, withinBudget=" + (elapsedNanos <= budgetNanos);
			System.out.println(testClass().getSimpleName() + ": " + measurement);
		}
		assertTrue("Type inference exceeded the 10-second budget: " + measurement,
				elapsedNanos <= budgetNanos);
	}
}