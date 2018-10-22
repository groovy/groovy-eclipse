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
package org.eclipse.jdt.core.tests.eval;

import java.util.ArrayList;

import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;

import junit.framework.Test;
/**
 * Run all tests defined in this package.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TestAll extends EvaluationTest {
public TestAll(String name) {
	super(name);
}
public static Test suite() {
	ArrayList testClasses = new ArrayList();
	testClasses.add(SanityTestEvaluationContext.class);
	testClasses.add(SanityTestEvaluationResult.class);
	testClasses.add(VariableTest.class);
	testClasses.add(CodeSnippetTest.class);
	testClasses.add(NegativeCodeSnippetTest.class);
	testClasses.add(NegativeVariableTest.class);
	testClasses.add(DebugEvaluationTest.class);

	return AbstractCompilerTest.buildAllCompliancesTestSuite(TestAll.class, DebugEvaluationSetup.class, testClasses);
}
}
