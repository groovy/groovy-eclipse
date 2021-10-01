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

import junit.framework.Test;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.eval.EvaluationResult;
import org.eclipse.jdt.internal.eval.IRequestor;
import org.eclipse.jdt.internal.eval.InstallException;
/**
 * Sanity test the IEvaluationResult interface.
 * For in depth tests, see VariableTest or CodeSnippetTest.
 */
@SuppressWarnings({ "rawtypes" })
public class SanityTestEvaluationResult extends EvaluationTest {
	EvaluationResult result;
/**
 * Creates a new SanityEvaluationResultTest.
 */
public SanityTestEvaluationResult(String name) {
	super(name);
}
/**
 * Initializes this test with an evaluation result coming from the
 * evaluation of the following code snippet: "return 1;".
 */
@Override
protected void setUp() throws Exception {
	super.setUp();
	IRequestor requestor = new Requestor() {
		@Override
		public void acceptResult(EvaluationResult evalResult) {
			SanityTestEvaluationResult.this.result = evalResult;
		}
	};
	try {
		this.context.evaluate("return 1;".toCharArray(), getEnv(), getCompilerOptions(), requestor, getProblemFactory());
	} catch (InstallException e) {
		throw new Error(e.getMessage());
	}
}
public static Test suite() {
	return setupSuite(testClass());
}
public static Class testClass() {
	return SanityTestEvaluationResult.class;
}
/**
 * Sanity test of IEvaluationResult.getEvaluationType()
 */
public void testGetEvaluationType() {
	int evaluationType = this.result.getEvaluationType();
	assertEquals("Evaluation type", EvaluationResult.T_CODE_SNIPPET, evaluationType);
}
/**
 * Sanity test of IEvaluationResult.getProblems()
 */
public void testGetProblems() {
	CategorizedProblem[] problems = this.result.getProblems();
	assertTrue("Problems", problems == null || problems.length == 0);
}
/**
 * Sanity test of IEvaluationResult.getValue()
 */
public void testGetValue() {
	// TBD: Not implemented yet
}
/**
 * Sanity test of IEvaluationResult.getValueDisplayString()
 */
public void testGetValueDisplayString() {
	char[] displayString = this.result.getValueDisplayString();
	assertEquals("Value display string", "1".toCharArray(), displayString);
}
/**
 * Sanity test of IEvaluationResult.getValueTypeName()
 */
public void testGetValueTypeName() {
	char[] typeName = this.result.getValueTypeName();
	assertEquals("Value type name", "int".toCharArray(), typeName);
}
/**
 * Sanity test of IEvaluationResult.hasErrors()
 */
public void testHasErrors() {
	assertTrue("Result has no errors", !this.result.hasErrors());
}
/**
 * Sanity test of IEvaluationResult.hasProblems()
 */
public void testHasProblems() {
	assertTrue("Result has no problems", !this.result.hasProblems());
}
/**
 * Sanity test of IEvaluationResult.hasValue()
 */
public void testHasValue() {
	assertTrue("Result has a value", this.result.hasValue());
}
/**
 * Sanity test of IEvaluationResult.hasWarnings()
 */
public void testHasWarnings() {
	assertTrue("Result has no warnings", !this.result.hasWarnings());
}
}
