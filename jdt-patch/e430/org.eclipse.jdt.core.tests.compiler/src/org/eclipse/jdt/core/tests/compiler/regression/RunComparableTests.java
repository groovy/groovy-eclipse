/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.eclipse.jdt.core.tests.junit.extension.TestCase;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Run all compiler regression tests
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class RunComparableTests extends junit.framework.TestCase {

	public static ArrayList ALL_CLASSES = null;
	static {
		ALL_CLASSES = new ArrayList();
		ALL_CLASSES.add(AmbiguousMethodTest.class);
		ALL_CLASSES.add(AutoBoxingTest.class);
		ALL_CLASSES.add(SuppressWarningsTest.class);
		ALL_CLASSES.add(Compliance_1_5.class);
		ALL_CLASSES.add(GenericTypeTest.class);
		ALL_CLASSES.add(GenericsRegressionTest.class);
		ALL_CLASSES.add(ForeachStatementTest.class);
		ALL_CLASSES.add(StaticImportTest.class);
		ALL_CLASSES.add(VarargsTest.class);
		ALL_CLASSES.add(EnumTest.class);
		ALL_CLASSES.add(MethodVerifyTest.class);
		ALL_CLASSES.add(AnnotationTest.class);
		ALL_CLASSES.add(EnclosingMethodAttributeTest.class);
		// Reset forgotten subsets tests
		TestCase.TESTS_PREFIX = null;
		TestCase.TESTS_NAMES = null;
		TestCase.TESTS_NUMBERS= null;
		TestCase.TESTS_RANGE = null;
		TestCase.RUN_ONLY_ID = null;
	}

	public RunComparableTests(String testName) {
		super(testName);
	}

	public static Test suite() {
		TestSuite ts = new TestSuite(RunComparableTests.class.getName());
		for (int i = 0, size=ALL_CLASSES.size(); i < size; i++) {
			Class testClass = (Class) ALL_CLASSES.get(i);
			try {
				Method suiteMethod = testClass.getDeclaredMethod("suite", new Class[0]); //$NON-NLS-1$
				Test suite = (Test)suiteMethod.invoke(null, new Object[0]);
				ts.addTest(suite);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.getTargetException().printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		return ts;
	}
}
