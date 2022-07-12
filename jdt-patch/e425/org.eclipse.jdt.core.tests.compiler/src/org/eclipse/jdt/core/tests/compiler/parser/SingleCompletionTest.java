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
package org.eclipse.jdt.core.tests.compiler.parser;

import java.lang.reflect.InvocationTargetException;

import junit.framework.TestCase;

/**
 * Only 1 test should be in this class
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class SingleCompletionTest extends AbstractCompletionTest {
/**
 * SingleCompletionTest constructor comment.
 * @param testName java.lang.String
 */
public SingleCompletionTest(String testName) {
	super(testName);
}
private void run(Class testClass, String methodName) {
	try {
		java.lang.reflect.Constructor constructor = testClass.getDeclaredConstructor(new Class[] {String.class});
		TestCase test = (TestCase)constructor.newInstance(new Object[] {"single completion test"});
		java.lang.reflect.Method method = testClass.getDeclaredMethod(methodName, new Class[] {});
		method.invoke(test, new Object[] {});
	} catch (InstantiationException e) {
		e.printStackTrace();
	} catch (IllegalAccessException e) {
		e.printStackTrace();
	} catch (NoSuchMethodException e) {
		e.printStackTrace();
	} catch (InvocationTargetException e) {
		Throwable target = e.getTargetException();
		if (target instanceof RuntimeException) {
			throw (RuntimeException)target;
		}
		if (target instanceof Error) {
			throw (Error)target;
		}
		throw new Error(target.getMessage());
	}
}
/*
 * The test.
 */
public void test() {
	run(NameReferenceCompletionTest.class, "testMethodInvocationAnonymousInnerClass2");
}
}
