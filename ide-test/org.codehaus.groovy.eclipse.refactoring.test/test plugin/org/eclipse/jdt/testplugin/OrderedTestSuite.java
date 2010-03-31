/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.testplugin;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test suite with user-specified test order.
 * Fails if not all test methods are listed.
 */
public class OrderedTestSuite extends TestSuite {

	public OrderedTestSuite(final Class testClass, String[] testMethods) {
		super(testClass.getName());

		Set existingMethods= new HashSet();
		Method[] methods= testClass.getMethods(); // just public member methods
		for (int i= 0; i < methods.length; i++) {
			Method method= methods[i];
			existingMethods.add(method.getName());
		}

		for (int i= 0; i < testMethods.length; i++) {
			final String testMethod= testMethods[i];
			if (existingMethods.remove(testMethod)) {
				addTest(createTest(testClass, testMethod));
			} else {
				addTest(error(testMethod, new IllegalArgumentException(
						"Class '" + testClass.getName() + " misses test method '" + testMethod
						+ "'.")));
			}
		}

		for (Iterator iter= existingMethods.iterator(); iter.hasNext();) {
			String existingMethod= (String) iter.next();
			if (existingMethod.startsWith("test")) {
				addTest(error(existingMethod, new IllegalArgumentException(
						"Test method '" + existingMethod + "' not listed in OrderedTestSuite of class '"
						+ testClass.getName() + "'.")));
			}
		}

	}

	public static Test error(String testMethod, Exception exception) {
		final Throwable e2= exception.fillInStackTrace();
		return new TestCase(testMethod) {
			protected void runTest() throws Throwable {
				throw e2;
			}
		};
	}
}
