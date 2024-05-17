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

import junit.framework.Test;

/**
 * <b>README:</b> this class captures the actual outcome of examples where we doubt if the outcome is correct,
 * (e.g., because javac behaves differently). Still the current outcome is expected in tests, in order
 * to alert us when their behavior changes due to some other fix. If such change occurs, we should decide:
 * <ol>
 * <li>if the new outcome is worse, try to improve the code change
 * <li>if the new outcome is equally dubious, just change the test expectation
 * <li>if the new outcome is good, thus removing the doubt, then move the test to a 'regular' suite class
 * </ol>
 */
public class DubiousOutcomeTest extends AbstractRegressionTest {

	static {
//		TESTS_NAMES = new String[] { "testGH1591" };
//		TESTS_NUMBERS = new int[] { 40, 41, 43, 45, 63, 64 };
//		TESTS_RANGE = new int[] { 11, -1 };
	}

	public DubiousOutcomeTest(String name) {
		super(name);
	}
	public static Class<?> testClass() {
		return DubiousOutcomeTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}

	public void testGH1591() {
		// javac accepts
		runNegativeTest(
			new String[] {
				"Outer.java",
				"""
				import java.io.Serializable;
				import java.util.List;
				import java.util.function.Supplier;

				public class Outer {
					public void test() {
						Supplier<? extends List<? extends Serializable>> supplier = () -> null;
						error(supplier.get(), "");
					}

					public <T, V extends Serializable> void error(List<V> v2, T t) {}

					}
				"""
			},
			"----------\n" +
			"1. ERROR in Outer.java (at line 8)\n" +
			"	error(supplier.get(), \"\");\n" +
			"	^^^^^\n" +
			"The method error(List<V>, T) in the type Outer is not applicable for the arguments (capture#1-of ? extends List<? extends Serializable>, String)\n" +
			"----------\n");
	}
}
