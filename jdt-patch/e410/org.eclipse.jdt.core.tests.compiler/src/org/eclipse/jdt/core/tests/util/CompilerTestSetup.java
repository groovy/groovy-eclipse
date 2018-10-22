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
package org.eclipse.jdt.core.tests.util;

import java.util.Enumeration;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.TestResult;
import junit.framework.TestSuite;

@SuppressWarnings({ "rawtypes" })
public class CompilerTestSetup extends TestSuite {

	long complianceLevel;

	public CompilerTestSetup(long complianceLevel) {
		super(CompilerOptions.versionFromJdkLevel(complianceLevel));
		this.complianceLevel = complianceLevel;
	}

	protected void initTest(Object test) {
		if (test instanceof AbstractCompilerTest) {
			AbstractCompilerTest compilerTest = (AbstractCompilerTest)test;
			compilerTest.initialize(this);
			return;
		}
		if (test instanceof TestSuite) {
			TestSuite testSuite = (TestSuite)test;
			Enumeration evaluationTestClassTests = testSuite.tests();
			while (evaluationTestClassTests.hasMoreElements()) {
				initTest(evaluationTestClassTests.nextElement());
			}
			return;
		}
		if (test instanceof Enumeration) {
			Enumeration evaluationTestClassTests = (Enumeration) test;
			while (evaluationTestClassTests.hasMoreElements()) {
				initTest(evaluationTestClassTests.nextElement());
			}
			return;
		}
	}

	public void run(TestResult result) {
		try {
			setUp();
			super.run(result);
		} finally {
			tearDown();
		}
	}

	protected void setUp() {
		// Init wrapped suite
		initTest(tests());
	}

	protected void tearDown() {
	}
}
