/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.eval;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.junit.extension.StopableTestCase;
import org.eclipse.jdt.core.tests.runtime.LocalVMLauncher;
import org.eclipse.jdt.core.tests.runtime.LocalVirtualMachine;
import org.eclipse.jdt.core.tests.runtime.TargetException;
import org.eclipse.jdt.core.tests.runtime.TargetInterface;
import org.eclipse.jdt.core.tests.util.*;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.eval.EvaluationContext;
import org.eclipse.jdt.internal.eval.EvaluationResult;
import org.eclipse.jdt.internal.eval.GlobalVariable;
import org.eclipse.jdt.internal.eval.IRequestor;
import org.eclipse.jdt.internal.eval.InstallException;

public class EvaluationTest extends AbstractCompilerTest implements StopableTestCase {

	public class Requestor implements IRequestor {
		public int resultIndex = -1;
		public EvaluationResult[] results = new EvaluationResult[5];
		public boolean acceptClassFiles(ClassFile[] classFiles, char[] codeSnippetClassName) {
			try {
				EvaluationTest.this.target.sendClasses(codeSnippetClassName != null, classFiles);
			} catch (TargetException e) {
				return false;
			}
			if (codeSnippetClassName != null) {
				TargetInterface.Result result = EvaluationTest.this.target.getResult();
				if (result.displayString == null) {
					acceptResult(new EvaluationResult(null, EvaluationResult.T_CODE_SNIPPET, null, null));
				} else {
					acceptResult(new EvaluationResult(null, EvaluationResult.T_CODE_SNIPPET, result.displayString, result.typeName));
				}
			} else {
				for (int i = 0, length = classFiles.length; i < length; i++) {
					char[][] compoundName = classFiles[i].getCompoundName();
					if (new String(compoundName[compoundName.length - 1]).startsWith("GlobalVariable")) {
						try {
							IBinaryField[] fields = new ClassFileReader(classFiles[i].getBytes(), null).getFields();
							if (fields != null) {
								for (int j = 0; j < fields.length; j++) {
									IBinaryField field = fields[j];
									if (Modifier.isPublic(field.getModifiers())) {
										TargetInterface.Result result = EvaluationTest.this.target.getResult();
										if (result.displayString == null) {
											acceptResult(new EvaluationResult(field.getName(), EvaluationResult.T_VARIABLE, null, null));
										} else {
											acceptResult(new EvaluationResult(field.getName(), EvaluationResult.T_VARIABLE, result.displayString, result.typeName));
										}
									}
								}
							}
						} catch (ClassFormatException e) {
							e.printStackTrace();
						}
					}
				}
			}
			return true;
		}
		public void acceptProblem(CategorizedProblem problem, char[] fragmentSource, int fragmentKind) {
			acceptResult(new EvaluationResult(fragmentSource, fragmentKind, new CategorizedProblem[]{problem}));
		}
		public void acceptResult(EvaluationResult result) {
			try {
				this.results[++this.resultIndex] = result;
			} catch (ArrayIndexOutOfBoundsException e) {
				int oldResultLength = this.results.length;
				System.arraycopy(this.results, 0, (this.results = new EvaluationResult[oldResultLength * 2]), 0, oldResultLength);
				this.results[this.resultIndex] = result;
			}
		}
	}

	String[] classPath;
	public EvaluationContext context;
	INameEnvironment env;
	LocalVirtualMachine launchedVM;
	TargetInterface target;

	/**
	 * Creates a new EvaluationTest.
	 */
	public EvaluationTest(String name) {
		super(name);
	}

	public static Test setupSuite(Class clazz) {
		ArrayList testClasses = new ArrayList();
		testClasses.add(clazz);
		return buildAllCompliancesTestSuite(clazz, EvaluationSetup.class, testClasses);
	}

	public static Test suite(Class evaluationTestClass) {
		TestSuite suite = new TestSuite(evaluationTestClass);
		return suite;
	}

	/**
	 * Asserts that two char arrays are equal. If they are not an AssertionFailedError is thrown.
	 *
	 * @param message
	 *                 the detail message for this assertion
	 * @param expected
	 *                 the expected value of a char array
	 * @param actual
	 *                 the actual value of a char array
	 */
	public void assertEquals(String message, char[] expected, char[] actual) {
		if (expected == null && actual == null)
			return;
		if (expected != null) {
			if (actual == null) {
				failNotEquals(message, expected, actual);
				return;
			}
			if (expected.length == actual.length) {
				for (int i = 0; i < expected.length; i++) {
					if (expected[i] != actual[i]) {
						failNotEquals(message, expected, actual);
						return;
					}
				}
				return;
			}
		}
		failNotEquals(message, expected, actual);
	}

	/**
	 * Build a char array from the given lines
	 */
	protected char[] buildCharArray(String[] lines) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < lines.length; i++) {
			buffer.append(lines[i]);
			if (i < lines.length - 1) {
				buffer.append("\n");
			}
		}
		int length = buffer.length();
		char[] result = new char[length];
		buffer.getChars(0, length, result, 0);
		return result;
	}

	/**
	 * Returns whether the 2 given problems are equals.
	 */
	public boolean equals(CategorizedProblem pb1, CategorizedProblem pb2) {
		if ((pb1 == null) && (pb2 == null)) {
			return true;
		}
		if ((pb1 == null) || (pb2 == null)) {
			return false;
		}
		return (pb1.getID() == pb2.getID()) && (pb1.isError() == pb2.isError()) && (pb1.getSourceStart() == pb2.getSourceStart()) && (pb1.getSourceEnd() == pb2.getSourceEnd()) && (pb1.getSourceLineNumber() == pb2.getSourceLineNumber());
	}

	/**
	 * Evaluates the given code snippet and makes sure it returns a result with the given display string.
	 */
	public void evaluateWithExpectedDisplayString(Map compilerOptions, char[] codeSnippet, char[] displayString) {
		Requestor requestor = new Requestor();
		try {
			this.context.evaluate(codeSnippet, getEnv(), compilerOptions, requestor, getProblemFactory());
		} catch (InstallException e) {
			assertTrue("Target exception " + e.getMessage(), false);
		}
		if (requestor.resultIndex != 0) {
			for (int i = 0; i < requestor.resultIndex; i++) {
				System.out.println("unexpected result[" + i + "]: " + requestor.results[i]);
			}
		}
		assertTrue("Unexpected result", requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Has problem", !result.hasProblems());
		assertTrue("Empty problem list", result.getProblems().length == 0);
		if (displayString == null) {
			assertTrue("Has value", !result.hasValue());
		} else {
			assertTrue("Has value", result.hasValue());
			assertEquals("Evaluation type", EvaluationResult.T_CODE_SNIPPET, result.getEvaluationType());
			//assertEquals("Evaluation id", codeSnippet, result.getEvaluationID());
			assertEquals("Value display string", displayString, result.getValueDisplayString());
		}
	}

	/**
	 * Evaluates the given code snippet and makes sure it returns a result with the given display string.
	 */
	public void evaluateWithExpectedDisplayString(char[] codeSnippet, char[] displayString) {
		Requestor requestor = new Requestor();
		try {
			this.context.evaluate(codeSnippet, getEnv(), getCompilerOptions(), requestor, getProblemFactory());
		} catch (InstallException e) {
			assertTrue("Target exception " + e.getMessage(), false);
		}
		if (requestor.resultIndex != 0) {
			for (int i = 0; i < requestor.resultIndex; i++) {
				System.out.println("unexpected result[" + i + "]: " + requestor.results[i]);
			}
		}
		assertTrue("Unexpected result", requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Has problem", !result.hasProblems());
		assertTrue("Empty problem list", result.getProblems().length == 0);
		if (displayString == null) {
			assertTrue("Has value", !result.hasValue());
		} else {
			assertTrue("Has value", result.hasValue());
			assertEquals("Evaluation type", EvaluationResult.T_CODE_SNIPPET, result.getEvaluationType());
			//assertEquals("Evaluation id", codeSnippet, result.getEvaluationID());
			assertEquals("Value display string", displayString, result.getValueDisplayString());
		}
	}

	/**
	 * Evaluates the given code snippet and makes sure the evaluation result has at least the given problem on the given import.
	 */
	protected void evaluateWithExpectedImportProblem(char[] codeSnippet, char[] importDeclaration, CategorizedProblem expected) {
		evaluateWithExpectedImportProblem(codeSnippet, importDeclaration, getCompilerOptions(), expected);
	}

	/**
	 * Evaluates the given code snippet and makes sure the evaluation result has at least the given problem on the given import.
	 */
	protected void evaluateWithExpectedImportProblem(char[] codeSnippet, char[] importDeclaration, Map options, CategorizedProblem expected) {
		Requestor requestor = new Requestor();
		try {
			this.context.evaluate(codeSnippet, getEnv(), options, requestor, getProblemFactory());
		} catch (InstallException e) {
			assertTrue("Target exception " + e.getMessage(), false);
		}
		for (int i = 0; i <= requestor.resultIndex; i++) {
			EvaluationResult result = requestor.results[i];
			assertTrue("Has value", !result.hasValue());
			assertTrue("Has problem", result.hasProblems());
			assertEquals("Evaluation type", EvaluationResult.T_IMPORT, result.getEvaluationType());
			assertEquals("Evaluation id", importDeclaration, result.getEvaluationID());
			CategorizedProblem[] problems = result.getProblems();
			if (equals(expected, problems[0])) {
				return;
			}
		}
		assertTrue("Expected problem not found", false);
	}

	/**
	 * Evaluates the given code snippet and makes sure the evaluation result has at least the given problem.
	 */
	protected void evaluateWithExpectedProblem(char[] codeSnippet, CategorizedProblem expected) {
		Requestor requestor = new Requestor();
		try {
			this.context.evaluate(codeSnippet, getEnv(), getCompilerOptions(), requestor, getProblemFactory());
		} catch (InstallException e) {
			assertTrue("Target exception " + e.getMessage(), false);
		}
		for (int i = 0; i <= requestor.resultIndex; i++) {
			EvaluationResult result = requestor.results[i];
			assertTrue("Has value", !result.hasValue());
			assertTrue("Has problem", result.hasProblems());
			assertEquals("Evaluation type", EvaluationResult.T_CODE_SNIPPET, result.getEvaluationType());
			assertEquals("Evaluation id", codeSnippet, result.getEvaluationID());
			CategorizedProblem[] problems = result.getProblems();
			if (equals(expected, problems[0])) {
				return;
			}
		}
		assertTrue("Expected problem not found", false);
	}

	/**
	 * Evaluates the given code snippet and makes sure the evaluation result has at least the given problem.
	 */
	protected void evaluateWithExpectedProblem(char[] codeSnippet, String problemsString) {
		Requestor requestor = new Requestor();
		try {
			this.context.evaluate(codeSnippet, getEnv(), getCompilerOptions(), requestor, getProblemFactory());
		} catch (InstallException e) {
			assertTrue("Target exception " + e.getMessage(), false);
		}
		assertTrue("Got one result", requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Has value", !result.hasValue());
		assertTrue("Has problem", result.hasProblems());
		assertEquals("Evaluation type", EvaluationResult.T_CODE_SNIPPET, result.getEvaluationType());
		assertEquals("Evaluation id", codeSnippet, result.getEvaluationID());
		StringBuffer problemBuffer = new StringBuffer(20);
		CategorizedProblem[] problems = result.getProblems();
		for (int i = 0; i < problems.length; i++) {
			problemBuffer.append(problems[i].getMessage()).append('\n');
		}
		assertEquals("Unexpected problems", problemsString, problemBuffer.toString());
	}

	/**
	 * Evaluates the given variable and makes sure the evaluation result has at least the given problem.
	 */
	protected void evaluateWithExpectedProblem(GlobalVariable var, CategorizedProblem expected) {
		Requestor requestor = new Requestor();
		try {
			this.context.evaluateVariables(getEnv(), getCompilerOptions(), requestor, getProblemFactory());
		} catch (InstallException e) {
			assertTrue("Target exception " + e.getMessage(), false);
		}
		for (int i = 0; i <= requestor.resultIndex; i++) {
			EvaluationResult result = requestor.results[i];
			assertTrue("Has value", !result.hasValue());
			assertTrue("Has problem", result.hasProblems());
			assertEquals("Evaluation type", EvaluationResult.T_VARIABLE, result.getEvaluationType());
			assertEquals("Evaluation id", var.getName(), result.getEvaluationID());
			CategorizedProblem[] problems = result.getProblems();
			if (equals(expected, problems[0])) {
				return;
			}
		}
		assertTrue("Expected problem not found", false);
	}

	/**
	 * Evaluates the given code snippet and makes sure it returns a result with the given type name.
	 */
	protected void evaluateWithExpectedType(char[] codeSnippet, char[] expectedTypeName) {
		Requestor requestor = new Requestor();
		try {
			this.context.evaluate(codeSnippet, getEnv(), getCompilerOptions(), requestor, getProblemFactory());
		} catch (InstallException e) {
			assertTrue("Target exception " + e.getMessage(), false);
		}
		if (requestor.resultIndex != 0) {
			for (int i = 0; i < requestor.resultIndex; i++) {
				System.out.println("unexpected result[" + i + "]: " + requestor.results[i]);
			}
		}
		assertTrue("Got one result", requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		if (expectedTypeName == null) {
			assertTrue("Has value", !result.hasValue());
		} else {
			assertTrue("Has value", result.hasValue());
			assertEquals("Evaluation type", EvaluationResult.T_CODE_SNIPPET, result.getEvaluationType());
			//assertEquals("Evaluation id", codeSnippet, result.getEvaluationID());
			assertEquals("Value type name", expectedTypeName, result.getValueTypeName());
		}
	}

	/**
	 * Evaluates the given code snippet and makes sure it returns a result with the given display string and type name.
	 */
	protected void evaluateWithExpectedValue(char[] codeSnippet, char[] displayString, char[] typeName) {
		Requestor requestor = new Requestor();
		try {
			this.context.evaluate(codeSnippet, getEnv(), getCompilerOptions(), requestor, getProblemFactory());
		} catch (InstallException e) {
			assertTrue("Target exception " + e.getMessage(), false);
		}
		assertTrue("Got one result", requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		if (displayString == null) {
			assertTrue("Missing value", !result.hasValue());
		} else {
			assertTrue("Has value", result.hasValue());
			assertEquals("Evaluation type", EvaluationResult.T_CODE_SNIPPET, result.getEvaluationType());
			//assertEquals("Evaluation id", codeSnippet, result.getEvaluationID());
			assertEquals("Value display string", displayString, result.getValueDisplayString());
			assertEquals("Value type name", typeName, result.getValueTypeName());
		}
	}

	/**
	 * Evaluates the given variable and makes sure it returns a result with the given display string and type name.
	 */
	protected void evaluateWithExpectedValue(GlobalVariable var, char[] displayString, char[] typeName) {
		Requestor requestor = new Requestor();
		try {
			this.context.evaluateVariable(var, getEnv(), getCompilerOptions(), requestor, getProblemFactory());
		} catch (InstallException e) {
			assertTrue("Target exception " + e.getMessage(), false);
		}
		if (requestor.resultIndex != 0) {
			for (int i = 0; i < requestor.resultIndex; i++) {
				System.out.println("unexpected result[" + i + "]: " + requestor.results[i]);
			}
		}
		assertTrue("Unexpected result", requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		if (displayString == null) {
			assertTrue("Has value", !result.hasValue());
		} else {
			assertTrue("Has value", result.hasValue());
			assertEquals("Value display string", displayString, result.getValueDisplayString());
			assertEquals("Value type name", typeName, result.getValueTypeName());
		}
	}

	/**
	 * Evaluates the given code snippet and makes sure an evaluation result has at least the given warning, and that another evaluation result has the given display string.
	 */
	protected void evaluateWithExpectedWarningAndDisplayString(final char[] codeSnippet, final CategorizedProblem[] expected, final char[] displayString) {
		class ResultRequestor extends Requestor {
			ArrayList collectedProblems = new ArrayList();
			boolean gotDisplayString = false;
			public void acceptResult(EvaluationResult result) {
				assertEquals("Evaluation type", EvaluationResult.T_CODE_SNIPPET, result.getEvaluationType());
				//assertEquals("Evaluation id", codeSnippet, result.getEvaluationID());
				if (result.hasValue()) {
					if (CharOperation.equals(result.getValueDisplayString(), displayString)) {
						this.gotDisplayString = true;
					}
				} else {
					assertTrue("Has problem", result.hasProblems());
					CategorizedProblem[] problems = result.getProblems();
					for (int i = 0; i < problems.length; i++) {
						this.collectedProblems.add(problems[i]);
					}
				}
			}
		}
		ResultRequestor requestor = new ResultRequestor();
		try {
			this.context.evaluate(codeSnippet, getEnv(), getCompilerOptions(), requestor, getProblemFactory());
		} catch (InstallException e) {
			assertTrue("Target exception " + e.getMessage(), false);
		}
		if (expected.length == requestor.collectedProblems.size()) {
			for (int i = 0; i < expected.length; i++) {
				assertTrue("Problem mismatch" + requestor.collectedProblems.get(i), this.equals(expected[i], (CategorizedProblem)requestor.collectedProblems.get(i)));
			}
		} else {
			assertTrue("Wrong problem count", false);
		}
		assertTrue("Expected display string", requestor.gotDisplayString);
	}

	private void failNotEquals(String message, char[] expected, char[] actual) {
		String formatted = "";
		if (message != null)
			formatted = message + " ";
		String expectedString = expected == null ? "null" : new String(expected);
		String actualString = actual == null ? "null" : new String(actual);
		fail(formatted + "expected:<" + expectedString + "> but was:<" + actualString + ">");
	}

	public Map getCompilerOptions() {
		Map defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_LocalVariableAttribute, CompilerOptions.DO_NOT_GENERATE);
		defaultOptions.put(CompilerOptions.OPTION_LineNumberAttribute, CompilerOptions.DO_NOT_GENERATE);
		defaultOptions.put(CompilerOptions.OPTION_SourceFileAttribute, CompilerOptions.DO_NOT_GENERATE);
		defaultOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
		defaultOptions.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.IGNORE);
		defaultOptions.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.WARNING);
		defaultOptions.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.WARNING);
		defaultOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
		defaultOptions.put(CompilerOptions.OPTION_ReportPossibleAccidentalBooleanAssignment, CompilerOptions.WARNING);
		return defaultOptions;
	}

	public INameEnvironment getEnv() {
		return this.env;
	}

	public IProblemFactory getProblemFactory() {
		return new DefaultProblemFactory(java.util.Locale.getDefault());
	}

	public void initialize(CompilerTestSetup setUp) {
		super.initialize(setUp);
		EvaluationSetup evalSetUp = (EvaluationSetup)setUp;
		this.context = evalSetUp.context;
		this.target = evalSetUp.target;
		this.launchedVM = evalSetUp.launchedVM;
		this.env = evalSetUp.env;
	}

	/**
	 * Installs all the variables and check that the number of installed variables is the given number.
	 */
	protected void installVariables(final int expectedNumber) {
		class InstallRequestor extends Requestor {
			int count = 0;
			public void acceptResult(EvaluationResult result) {
				assertTrue("Has problems", !result.hasProblems());
				assertTrue("Has value", result.hasValue());
				this.count++;
			}
		}

		InstallRequestor installRequestor = new InstallRequestor();
		try {
			this.context.evaluateVariables(getEnv(), getCompilerOptions(), installRequestor, getProblemFactory());
		} catch (InstallException e) {
			assertTrue("Target exception: " + e.getMessage(), false);
		}
		assertEquals("Number of installed variables", expectedNumber, installRequestor.count);
	}

	public boolean isJRockitVM() {
		final String vmName = System.getProperty("java.vm.name");
		return vmName != null && vmName.indexOf("JRockit") != -1;
	}
	/**
	 * Returns a new problem with the given id, severity, source positions and line number.
	 */
	protected DefaultProblem newProblem(int id, int severity, int startPos, int endPos, int line) {
		return new DefaultProblem(null, null, id, null, severity, startPos, endPos, line, 0);
	}

	public void resetEnv() {
		String encoding = (String)getCompilerOptions().get(CompilerOptions.OPTION_Encoding);
		if ("".equals(encoding))
			encoding = null;
		this.env = new FileSystem(Util.concatWithClassLibs(EvaluationSetup.EVAL_DIRECTORY + File.separator + LocalVMLauncher.REGULAR_CLASSPATH_DIRECTORY, false), new String[0], encoding);
	}

	public void stop() {
		if (this.target != null) {
			this.target.disconnect(); // Close the socket first so that the OS resource has a chance to be freed.
		}
		if (this.launchedVM != null) {
			try {
				int retry = 0;
				while (this.launchedVM.isRunning() && (++retry < 20)) {
					try {
						Thread.sleep(retry * 100);
					} catch (InterruptedException e) {
					}
				}
				if (this.launchedVM.isRunning()) {
					this.launchedVM.shutDown();
				}
			} catch (TargetException e) {
			}
		}
	}
}
