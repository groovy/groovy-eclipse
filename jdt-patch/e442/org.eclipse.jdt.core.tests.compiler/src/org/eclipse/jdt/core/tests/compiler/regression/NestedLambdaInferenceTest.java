/*******************************************************************************
 * Copyright (c) 2026 François Martin and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     François Martin - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

public class NestedLambdaInferenceTest extends AbstractRegressionTest {
	private static final int NESTING_DEPTH = 24;

	public NestedLambdaInferenceTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(NestedLambdaInferenceTest.class, F_1_8);
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5206
	public void testIssue5206GenericRouteChain() {
		runNestedLambdaTest("GenericRouteChain", """
			<V extends Red> V route(Red marker, Work<V> work) { return null; }
			<V extends Blue> V route(Blue marker, Work<V> work) { return null; }
			<V extends Green> V route(Green marker, Work<V> work) { return null; }
			<V extends Gold> V route(Gold marker, Work<V> work) { return null; }
			<V> V route(Object marker, Work<V> work) { return null; }

			<V extends Red> V select(Red marker, Work<V> work) { return null; }
			<V extends Blue> V select(Blue marker, Work<V> work) { return null; }
			<V extends Green> V select(Green marker, Work<V> work) { return null; }
			<V extends Gold> V select(Gold marker, Work<V> work) { return null; }
			<V> V select(Object marker, Work<V> work) { return null; }
			""");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5206
	public void testIssue5206ConcreteRouteChain() {
		runNestedLambdaTest("ConcreteRouteChain", """
			String route(Red marker, Work<Red> work) { return ""; }
			String route(Blue marker, Work<Blue> work) { return ""; }
			String route(Green marker, Work<Green> work) { return ""; }
			String route(Gold marker, Work<Gold> work) { return ""; }
			String route(Object marker, Work<String> work) { return ""; }

			String select(Red marker, Work<Red> work) { return ""; }
			String select(Blue marker, Work<Blue> work) { return ""; }
			String select(Green marker, Work<Green> work) { return ""; }
			String select(Gold marker, Work<Gold> work) { return ""; }
			String select(Object marker, Work<String> work) { return ""; }
			""");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5206
	public void testIssue5206InterleavedParameterizedLambdas() {
		this.runConformTest(new String[] {
			"InterleavedLambdas.java",
			"""
			public class InterleavedLambdas {
			    interface Producer<T> {
			        T produce();
			    }
			    interface Mapper<T, R> {
			        R map(T value);
			    }

			    static <T> T produce(Producer<T> producer) {
			        return producer.produce();
			    }
			    static <T, R> R map(T value, Mapper<T, R> mapper) {
			        return mapper.map(value);
			    }

			    public static void main(String[] args) {
			        String result = produce(() ->
			            map("left", left ->
			                produce(() ->
			                    map(7, number -> left + number)))
			            + produce(() -> "!"));
			        System.out.print(result);
			    }
			}
			"""
		},
		"left7!");
	}

	private void runNestedLambdaTest(String className, String overloads) {
		this.runConformTest(new String[] {
			className + ".java",
			"""
			class %s {
			    interface Work<V> {
			        V perform();
			    }
			    static final Work<String> TERMINAL = null;

			    void test() {
			        %s;
			    }
			    static class Red { }
			    static class Blue { }
			    static class Green { }
			    static class Gold { }
			%s
			}
			""".formatted(className, createNestedInvocation(), overloads.indent(4).stripTrailing())
		});
	}

	private static String createNestedInvocation() {
		String invocation = "route(null, TERMINAL)";
		for (int level = 1; level < NESTING_DEPTH; level++) {
			String selector = level % 2 == 0 ? "route" : "select";
			invocation = selector + "(null, () -> " + invocation + ")";
		}
		return invocation;
	}
}
