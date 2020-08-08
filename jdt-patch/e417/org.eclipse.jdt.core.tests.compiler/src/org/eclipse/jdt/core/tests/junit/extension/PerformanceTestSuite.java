/*******************************************************************************
 * Copyright (c) 2004, 2020 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.junit.extension;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

@SuppressWarnings({ "rawtypes" })
public class PerformanceTestSuite extends TestSuite {

	/**
	 * Constructs a TestSuite from the given class. Adds all the methods
	 * starting with "testPerf" as test cases to the suite.
	 */
	 public PerformanceTestSuite(final Class theClass) {
		setName(theClass.getName());
		try {
			getTestConstructor(theClass); // Avoid generating multiple error messages
		} catch (NoSuchMethodException e) {
			addTest(addWarningTest("Class "+theClass.getName()+" has no public constructor TestCase(String name) or TestCase()"));
			return;
		}

		if (!Modifier.isPublic(theClass.getModifiers())) {
			addTest(addWarningTest("Class "+theClass.getName()+" is not public"));
			return;
		}

		Class superClass= theClass;
		List<String> names= new ArrayList<>();
		while (Test.class.isAssignableFrom(superClass)) {
			Method[] methods= superClass.getDeclaredMethods();
			for (int i= 0; i < methods.length; i++) {
				addTestMethod(methods[i], names, theClass);
			}
			superClass= superClass.getSuperclass();
		}
		if (countTestCases() == 0)
			addTest(addWarningTest("No tests found in "+theClass.getName()));
	}

	public PerformanceTestSuite(String name) {
		setName(name);
	}

	private void addTestMethod(Method m, List<String> names, Class theClass) {
		String name= m.getName();
		if (names.contains(name))
			return;
		if (! isPublicTestMethod(m)) {
			if (isTestMethod(m))
				addTest(addWarningTest("Test method isn't public: "+m.getName()));
			return;
		}
		names.add(name);
		addTest(createTest(theClass, name));
	}

	public void addTestSuite(Class theClass) {
		addTest(new PerformanceTestSuite(theClass));
	}

	private boolean isPublicTestMethod(Method m) {
		return isTestMethod(m) && Modifier.isPublic(m.getModifiers());
	 }

	private boolean isTestMethod(Method m) {
		String name= m.getName();
		Class[] parameters= m.getParameterTypes();
		Class returnType= m.getReturnType();
		return parameters.length == 0 && name.startsWith("testPerf") && returnType.equals(Void.TYPE);
	 }

	/**
	 * Returns a test which will fail and log a warning message.
	 */
	private static Test addWarningTest(final String message) {
		return new TestCase("warning") {
			protected void runTest() {
				fail(message);
			}
		};
	}
}
