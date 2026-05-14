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
import org.eclipse.jdt.internal.eval.EvaluationResult;
import org.eclipse.jdt.internal.eval.GlobalVariable;
import org.eclipse.jdt.internal.eval.IRequestor;
import org.eclipse.jdt.internal.eval.InstallException;
/**
 * Sanity test the IEvaluationContext interface.
 * For in depth tests, see VariableTest or CodeSnippetTest.
 */
@SuppressWarnings({ "rawtypes" })
public class SanityTestEvaluationContext extends EvaluationTest {
/**
 * Creates a new SanityEvaluationContextTest.
 */
public SanityTestEvaluationContext(String name) {
	super(name);
}
public static Test suite() {
	return setupSuite(testClass());
}
/**
 * Sanity test of IEvaluationContext.allVariables()
 */
public void testAllVariables() {
	// No variables defined yet
	GlobalVariable[] vars = this.context.allVariables();
	assertEquals("No variables should be defined", 0, vars.length);

	// Define 3 variables
	this.context.newVariable("int".toCharArray(), "foo".toCharArray(), "1".toCharArray());
	this.context.newVariable("Object".toCharArray(), "bar".toCharArray(), null);
	this.context.newVariable("String".toCharArray(), "zip".toCharArray(), "\"abcdefg\"".toCharArray());
	vars = this.context.allVariables();
	assertEquals("3 variables should be defined", 3, vars.length);
	assertEquals("1st variable", "foo".toCharArray(), vars[0].getName());
	assertEquals("2nd variable", "bar".toCharArray(), vars[1].getName());
	assertEquals("3rd variable", "zip".toCharArray(), vars[2].getName());

	// Remove 2nd variable
	this.context.deleteVariable(vars[1]);
	vars = this.context.allVariables();
	assertEquals("2 variables should be defined", 2, vars.length);
	assertEquals("1st variable", "foo".toCharArray(), vars[0].getName());
	assertEquals("2nd variable", "zip".toCharArray(), vars[1].getName());

	// Remove last variable
	this.context.deleteVariable(vars[1]);
	vars = this.context.allVariables();
	assertEquals("1 variable should be defined", 1, vars.length);
	assertEquals("1st variable", "foo".toCharArray(), vars[0].getName());

	// Remove 1st variable
	this.context.deleteVariable(vars[0]);
	vars = this.context.allVariables();
	assertEquals("No variables should be defined", 0, vars.length);
}
public static Class testClass() {
	return SanityTestEvaluationContext.class;
}
/**
 * Sanity test of IEvaluationContext.evaluate(char[], INameEnvironment, ConfigurableOption[], IRequestor , IProblemFactory)
 */
public void testEvaluate() {
	Requestor requestor = new Requestor();
	char[] snippet = "return 1;".toCharArray();
	try {
		this.context.evaluate(snippet, getEnv(), getCompilerOptions(), requestor, getProblemFactory());
	} catch (InstallException e) {
		assertTrue("No targetException " + e.getMessage(), false);
	}
	assertTrue("Got one result", requestor.resultIndex == 0);
	EvaluationResult result = requestor.results[0];
	assertTrue("No problems with the code snippet", !result.hasProblems());
	assertTrue("Result has a value", result.hasValue());
	assertEquals("Value", "1".toCharArray(), result.getValueDisplayString());
	assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
}
/**
 * Sanity test of IEvaluationContext.evaluateImports(INameEnvironment, IRequestor , IProblemFactory)
 */
public void testEvaluateImports() {
	try {
		// Define imports
		this.context.setImports(new char[][] {"java.util.*".toCharArray(), "java.lang.reflect.Method".toCharArray()});

		// Evaluate them
		IRequestor requestor = new Requestor() {
			@Override
			public void acceptResult(EvaluationResult result) {
				assertTrue("No problems with the imports", !result.hasProblems());
			}
		};
		this.context.evaluateImports(getEnv(), requestor, getProblemFactory());
	} finally {
		// Clean up
		this.context.setImports(new char[0][]);
	}
}
/**
 * Sanity test of IEvaluationContext.evaluateVariable(IGlobalVariable, IRequestor)
 */
public void testEvaluateVariable() {
	GlobalVariable var = null;
	try {
		// Create the variable
		var = this.context.newVariable("int".toCharArray(), "foo".toCharArray(), "1".toCharArray());

		// Install it
		class NoPbRequestor extends Requestor {
			@Override
			public void acceptResult(EvaluationResult result) {
				assertTrue("No problems with the variable", !result.hasProblems());
			}
		}
		try {
			this.context.evaluateVariables(getEnv(), getCompilerOptions(), new NoPbRequestor(), getProblemFactory());
		} catch (InstallException e) {
			assertTrue("No targetException " + e.getMessage(), false);
		}

		// Get its value
		Requestor requestor = new Requestor();
		try {
			this.context.evaluateVariable(var, getEnv(), getCompilerOptions(), requestor, getProblemFactory());
		} catch (InstallException e) {
			assertTrue("No targetException " + e.getMessage(), false);
		}
		assertTrue("Got one result", requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Result has value", result.hasValue());
		assertEquals("Value", "1".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		// Clean up
		if (var != null) {
			this.context.deleteVariable(var);
		}
	}
}
/**
 * Sanity test of IEvaluationContext.evaluateVariables(INameEnvironment, ConfigurableOption[], IRequestor, IProblemFactory)
 */
public void testEvaluateVariables() {
	GlobalVariable var = null;
	try {
		// Create 1 variable
		var = this.context.newVariable("int".toCharArray(), "foo".toCharArray(), "1".toCharArray());

		// Install it and get its value
		Requestor requestor = new Requestor();
		try {
			this.context.evaluateVariables(getEnv(), getCompilerOptions(), requestor, getProblemFactory());
		} catch (InstallException e) {
			assertTrue("No targetException " + e.getMessage(), false);
		}
		assertTrue("Got one result", requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("No problems with the variable", !result.hasProblems());
		assertTrue("Result has value", result.hasValue());
		assertEquals("Value", "1".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		// Clean up
		if (var != null) {
			this.context.deleteVariable(var);
		}
	}
}
/**
 * Sanity test of IEvaluationContext.getImports() and IEvaluationContext.setImports(char[][])
 */
public void testGetSetImports() {
	try {
		// No imports
		assertTrue("No imports defined", this.context.getImports().length == 0);

		// Define some imports
		char[][] imports = new char[][] {"java.util".toCharArray(), "java.lang.reflect.Method".toCharArray()};
		this.context.setImports(imports);
		char[][] storedImports = this.context.getImports();
		assertEquals("Same length", imports.length, storedImports.length);
		for (int i = 0; i < imports.length; i++){
			assertEquals("Import #" + i, imports[i], storedImports[i]);
		}
	} finally {
		// Clean up
		this.context.setImports(new char[0][]);
	}
}
/**
 * Sanity test of IEvaluationContext.getPackageName() and IEvaluationContext.setPackageName(char[])
 */
public void testGetSetPackageName() {
	try {
		// Default package
		assertTrue("Default package", this.context.getPackageName().length == 0);

		// Define a package
		char[] packageName = "x.y.z".toCharArray();
		this.context.setPackageName(packageName);
		char[] storedPackageName = this.context.getPackageName();
		assertEquals("Same package name", packageName, storedPackageName);
	} finally {
		// Clean up
		this.context.setPackageName(new char[0]);
	}
}
/**
 * Sanity test of IEvaluationContext.newVariable(char[], char[], char[]) and
 * IEvaluationContext.deleteVariable(IGlobalVariable)
 */
public void testNewDeleteVariable() {
	// Define 1 variable
	GlobalVariable var = this.context.newVariable("int".toCharArray(), "deleted".toCharArray(), null);

	// Delete it
	this.context.deleteVariable(var);
	GlobalVariable[] vars = this.context.allVariables();
	for (int i = 0; i < vars.length; i++) {
		assertTrue("Variable should not exist", !var.getName().equals(vars[i].getName()));
	}
}
}
