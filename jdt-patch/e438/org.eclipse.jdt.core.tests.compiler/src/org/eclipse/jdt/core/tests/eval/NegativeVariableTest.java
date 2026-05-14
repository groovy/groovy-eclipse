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
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.eval.GlobalVariable;
/**
 * Negative tests for variables. Only compilation problems should be reported in
 * these tests.
 */
@SuppressWarnings({ "rawtypes" })
public class NegativeVariableTest extends EvaluationTest implements ProblemSeverities, ProblemReasons {
/**
 * Creates a new NegativeVariableTest.
 */
public NegativeVariableTest(String name) {
	super(name);
}
public static Test suite() {
	return setupSuite(testClass());
}
public static Class testClass() {
	return NegativeVariableTest.class;
}
/**
 * Test a variable that has a problem in its initializer.
 */
public void testInitializerProblem() {
	// Problem in first variable
	GlobalVariable var = null;
	try {
		var = this.context.newVariable("int".toCharArray(), "i".toCharArray(), buildCharArray(new String[] {
			"(1 + 1) *",
			"(j + 2)"}));
		evaluateWithExpectedProblem(
			var,
			newProblem(IProblem.UnresolvedVariable, Error, 11, 11, 2)); // j cannot be resolved to a variable
	} finally {
		if (var != null) {
			this.context.deleteVariable(var);
		}
	}

	// Problem in second variable
	GlobalVariable var1 = null;
	GlobalVariable var2 = null;
	try {
		var1 = this.context.newVariable("Object".toCharArray(), "o".toCharArray(), "new Object()".toCharArray());
		var2 = this.context.newVariable("int".toCharArray(), "i".toCharArray(), buildCharArray(new String[] {
			"(1 + 1) *",
			"(1 ++ 2)"}));
		evaluateWithExpectedProblem(
			var2,
			newProblem(IProblem.InvalidUnaryExpression, Error, 11, 11, 2)); // Invalid argument to operation ++/--
	} finally {
		if (var1 != null) {
			this.context.deleteVariable(var1);
		}
		if (var2 != null) {
			this.context.deleteVariable(var2);
		}
	}

}
/**
 * Test a variable that has a problem in its name.
 * TODO (david) investigate why changes in enum recovery caused this test to fail
 */
public void _testInvalidName() {
	// Problem in first variable
	GlobalVariable var = null;
	try {
		var = this.context.newVariable("int".toCharArray(), "!@#$%^&*()_".toCharArray(), "1".toCharArray());
		evaluateWithExpectedProblem(
			var,
			newProblem(IProblem.ParsingErrorDeleteTokens, Error, 0, 9, 0)); // Syntax error, delete these tokens
	} finally {
		if (var != null) {
			this.context.deleteVariable(var);
		}
	}

	// Problem in second variable
	GlobalVariable var1 = null;
	GlobalVariable var2 = null;
	try {
		var1 = this.context.newVariable("String".toCharArray(), "foo".toCharArray(), "\"bar\"".toCharArray());
		var2 = this.context.newVariable("int".toCharArray(), "!@#$%^&*()_".toCharArray(), "1".toCharArray());
		evaluateWithExpectedProblem(
			var2,
			newProblem(IProblem.ParsingErrorDeleteTokens, Error, 0, 9, 0)); // Syntax error, delete these tokens
	} finally {
		if (var1 != null) {
			this.context.deleteVariable(var1);
		}
		if (var2 != null) {
			this.context.deleteVariable(var2);
		}
	}
}
/**
 * Test a variable that has a problem in its type declaration.
 */
public void testUnknownType() {
	// Problem in first variable
	GlobalVariable var = null;
	try {
		var = this.context.newVariable("foo.Bar".toCharArray(), "var".toCharArray(), null);
		evaluateWithExpectedProblem(
			var,
			newProblem(IProblem.UndefinedType, Error, 0, 2, -1)); // The type foo is undefined
	} finally {
		if (var != null) {
			this.context.deleteVariable(var);
		}
	}

	// Problem in second variable
	GlobalVariable var1 = null;
	GlobalVariable var2 = null;
	try {
		var1 = this.context.newVariable("int".toCharArray(), "x".toCharArray(), null);
		var2 = this.context.newVariable("foo.Bar".toCharArray(), "var".toCharArray(), null);
		evaluateWithExpectedProblem(
			var2,
			newProblem(IProblem.UndefinedType, Error, 0, 2, -1)); // The type foo is undefined
	} finally {
		if (var1 != null) {
			this.context.deleteVariable(var1);
		}
		if (var2 != null) {
			this.context.deleteVariable(var2);
		}
	}
}
}
