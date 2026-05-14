/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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

import com.sun.jdi.VirtualMachine;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
import org.eclipse.jdt.core.tests.runtime.LocalVMLauncher;
import org.eclipse.jdt.core.tests.runtime.TargetInterface;
import org.eclipse.jdt.core.tests.util.CompilerTestSetup;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.eval.EvaluationResult;
import org.eclipse.jdt.internal.eval.InstallException;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DebugEvaluationTest extends EvaluationTest {
	static {
//		TESTS_NAMES = new String[] { "test069" };
	}
	class DebugRequestor extends Requestor {
		@Override
		public boolean acceptClassFiles(org.eclipse.jdt.internal.compiler.ClassFile[] classFiles, char[] codeSnippetClassName) {
			if (DebugEvaluationTest.this.jdiStackFrame == null) {
				return super.acceptClassFiles(classFiles, codeSnippetClassName);
			}
			// Send but don't run
			super.acceptClassFiles(classFiles, null);

			// Run if needed
			if (codeSnippetClassName != null) {
				boolean success = DebugEvaluationTest.this.jdiStackFrame.run(new String(codeSnippetClassName));
				if (success) {
					TargetInterface.Result result = DebugEvaluationTest.this.target.getResult();
					if (result.displayString == null) {
						acceptResult(new EvaluationResult(null, EvaluationResult.T_CODE_SNIPPET, null, null));
					} else {
						acceptResult(new EvaluationResult(null, EvaluationResult.T_CODE_SNIPPET, result.displayString, result.typeName));
					}
				}
				return success;
			}
			return true;
		}
	}

	protected static final String SOURCE_DIRECTORY = Util.getOutputDirectory() + File.separator + "source";

	public JDIStackFrame jdiStackFrame;
	VirtualMachine jdiVM;

	public DebugEvaluationTest(String name) {
		super(name);
	}
	public static Test setupSuite(Class clazz) {
		ArrayList testClasses = new ArrayList();
		testClasses.add(clazz);
		return buildAllCompliancesTestSuite(clazz, DebugEvaluationSetup.class, testClasses);
	}
	public static Test suite() {
		return setupSuite(testClass());
	}
	public static Class testClass() {
		return DebugEvaluationTest.class;
	}
	public void compileAndDeploy(String source, String className) {
		resetEnv(); // needed to reinitialize the caches
		File directory = new File(SOURCE_DIRECTORY);
		if (!directory.exists()) {
			if (!directory.mkdir()) {
				System.out.println("Could not create " + SOURCE_DIRECTORY);
				return;
			}
		}
		String fileName = SOURCE_DIRECTORY + File.separator + className + ".java";
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			writer.write(source);
			writer.flush();
			writer.close();
		} catch(IOException e) {
			e.printStackTrace();
			return;
		}
		StringBuilder buffer = new StringBuilder();
		buffer
			.append("\"")
			.append(fileName)
			.append("\" -d \"")
			.append(EvaluationSetup.EVAL_DIRECTORY + File.separator + LocalVMLauncher.REGULAR_CLASSPATH_DIRECTORY)
			.append("\" -nowarn -g -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(SOURCE_DIRECTORY)
			.append("\"");
		BatchCompiler.compile(buffer.toString(), new PrintWriter(System.out), new PrintWriter(System.err), null/*progress*/);
	}
	public void compileAndDeploy15(String source, String className) {
		resetEnv(); // needed to reinitialize the caches
		File directory = new File(SOURCE_DIRECTORY);
		if (!directory.exists()) {
			if (!directory.mkdir()) {
				System.out.println("Could not create " + SOURCE_DIRECTORY);
				return;
			}
		}
		String fileName = SOURCE_DIRECTORY + File.separator + className + ".java";
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			writer.write(source);
			writer.flush();
			writer.close();
		} catch(IOException e) {
			e.printStackTrace();
			return;
		}
		StringBuilder buffer = new StringBuilder();
		buffer
			.append("\"")
			.append(fileName)
			.append("\" -d \"")
			.append(EvaluationSetup.EVAL_DIRECTORY + File.separator + LocalVMLauncher.REGULAR_CLASSPATH_DIRECTORY)
			.append("\" -nowarn -" + CompilerOptions.getFirstSupportedJavaVersion() + " -g -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(SOURCE_DIRECTORY)
			.append("\"");
		BatchCompiler.compile(buffer.toString(), new PrintWriter(System.out), new PrintWriter(System.err), null/*progress*/);
	}
	private void evaluate(JDIStackFrame stackFrame, DebugRequestor requestor, char[] snippet) throws InstallException {
		this.context.evaluate(
			snippet,
			stackFrame.localVariableTypeNames(),
			stackFrame.localVariableNames(),
			stackFrame.localVariableModifiers(),
			stackFrame.declaringTypeName(),
			stackFrame.isStatic(),
			stackFrame.isConstructorCall(),
			getEnv(),
			getCompilerOptions(),
			requestor,
			getProblemFactory());
	}
	/**
	 * Generate local variable attribute for these tests.
	 */
	@Override
	public Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_LocalVariableAttribute, CompilerOptions.GENERATE);
		options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.getFirstSupportedJavaVersion());
		options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
		return options;
	}
	@Override
	public void initialize(CompilerTestSetup setUp) {
		super.initialize(setUp);
		this.jdiVM = ((DebugEvaluationSetup)setUp).vm;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		assertNotNull("VM is null, probably VM connection error", this.jdiVM);
	}

	public void removeTempClass(String className) {
		resetEnv(); // needed to reinitialize the caches
		Util.delete(SOURCE_DIRECTORY + File.separator + className + ".java");
		Util.delete(EvaluationSetup.EVAL_DIRECTORY + File.separator + LocalVMLauncher.REGULAR_CLASSPATH_DIRECTORY + File.separator + className + ".class");
	}
	/*public static Test suite(Class evaluationTestClass) {
		junit.framework.TestSuite suite = new junit.framework.TestSuite();
		suite.addTest(new DebugEvaluationTest("test018"));
		return suite;
	}*/
	/**
	 * Sanity test of IEvaluationContext.evaluate(char[], char[][], char[][], int[], char[], boolean, boolean, IRunner, INameEnvironment, ConfigurableOption[], IRequestor , IProblemFactory)
	 */
	public void test001() throws Exception {
		String userCode =
			"";
		JDIStackFrame stackFrame = new JDIStackFrame(
			this.jdiVM,
			this,
			userCode);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return 1;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue("Should get one result but got " + requestor.resultIndex+1, requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "1".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
}
/**
 * Return 'this'.
 */
public void test002() throws Exception {
	try {
		String sourceA002 =
			"public class A002 {\n" +
			"  public int foo() {\n" +
			"    return 2;\n" +
			"  }\n" +
			"  public String toString() {\n" +
			"    return \"hello\";\n" +
			"  }\n" +
			"}";
		compileAndDeploy(sourceA002, "A002");
		String userCode =
			"new A002().foo();";
		JDIStackFrame stackFrame = new JDIStackFrame(
			this.jdiVM,
			this,
			userCode,
			"A002",
			"foo",
			-1);
		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return this;".toCharArray();
		this.context.evaluate(
			snippet,
			null, // local var type names
			null, // local var names
			null, // local modifiers
			stackFrame.declaringTypeName(),
			stackFrame.isStatic(),
			stackFrame.isConstructorCall(),
			getEnv(),
			getCompilerOptions(),
			requestor,
			getProblemFactory());
		assertTrue("Should get one result but got " + requestor.resultIndex+1, requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "hello".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "A002".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A002");
	}
}
/**
 * Return 'this'.
 */
public void test003() throws Exception {
	try {
		String sourceA003 =
			"public class A003 {\n" +
			"  public int foo() {\n" +
			"    return 2;\n" +
			"  }\n" +
			"  public String toString() {\n" +
			"    return \"hello\";\n" +
			"  }\n" +
			"}";
		compileAndDeploy(sourceA003, "A003");
		String userCode =
			"new A003().foo();";
		JDIStackFrame stackFrame = new JDIStackFrame(
			this.jdiVM,
			this,
			userCode,
			"A003",
			"foo",
			-1);
		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return this;".toCharArray();
		this.context.evaluate(
			snippet,
			stackFrame.localVariableTypeNames(),
			stackFrame.localVariableNames(),
			stackFrame.localVariableModifiers(),
			null, // declaring type -- NO DELEGATE THIS
			stackFrame.isStatic(),
			stackFrame.isConstructorCall(),
			getEnv(),
			getCompilerOptions(),
			requestor,
			getProblemFactory());
		assertTrue("Should get one result but got " + requestor.resultIndex+1, requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should have a problem", result.hasProblems()); // 'this' cannot be referenced since there is no declaring type
		assertTrue("Result should not have a value", !result.hasValue());
	} finally {
		removeTempClass("A003");
	}
}
/**
 * Return 'thread'.
 */
public void test004() throws Exception {
	String userCode =
		"java.lang.Thread thread = new Thread() {\n" +
		"  public String toString() {\n" +
		"    return \"my thread\";\n" +
		"  }\n" +
		"};";
	JDIStackFrame stackFrame = new JDIStackFrame(
		this.jdiVM,
		this,
		userCode);

	DebugRequestor requestor = new DebugRequestor();
	char[] snippet = "return thread;".toCharArray();
	evaluate(stackFrame, requestor, snippet);
	assertTrue("Should get one result but got " + requestor.resultIndex+1, requestor.resultIndex == 0);
	EvaluationResult result = requestor.results[0];
	assertTrue("Code snippet should not have problems", !result.hasProblems());
	assertTrue("Result should have a value", result.hasValue());
	assertEquals("Value", "my thread".toCharArray(), result.getValueDisplayString());
	assertEquals("Type", "java.lang.Thread".toCharArray(), result.getValueTypeName());
}
/**
 * Return 'x'.
 */
public void test005() throws Exception {
	try {
		String sourceA005 =
			"public class A005 {\n" +
			"  public int x = 0;\n" +
			"  public int foo() {\n" +
			"    x++;\n" + // workaround pb with JDK 1.4.1 that doesn't stop if only return
			"    return x;\n" +
			"  }\n" +
			"}";
		compileAndDeploy(sourceA005, "A005");
		String userCode =
			"new A005().foo();";
		JDIStackFrame stackFrame = new JDIStackFrame(
			this.jdiVM,
			this,
			userCode,
			"A005",
			"foo",
			-1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return x;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue("Should get one result but got " + requestor.resultIndex+1, requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "0".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A005");
	}
}
/**
 * Return 'x' + new Object(){ int foo(){ return 17; }}.foo();
 */
public void test006() throws Exception {
	try {
		String sourceA006 =
			"public class A006 {\n" +
			"  public int x = 0;\n" +
			"  public int foo() {\n" +
			"    x++;\n" + // workaround pb with JDK 1.4.1 that doesn't stop if only return
			"    return x;\n" +
			"  }\n" +
			"}";
		compileAndDeploy(sourceA006, "A006");
		String userCode =
			"new A006().foo();";
		JDIStackFrame stackFrame = new JDIStackFrame(
			this.jdiVM,
			this,
			userCode,
			"A006",
			"foo",
			-1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return x + new Object(){ int foo(){ return 17; }}.foo();".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue("Should get one result but got " + requestor.resultIndex+1, requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "17".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A006");
	}
}
/**
 * Return a static field.
 */
public void test007() throws Exception {
	try {
		String sourceA007 =
			"public class A007 {\n" +
			"  public static int X = 1;\n" +
			"  public int foo() {\n" +
			"    X++;\n" + // workaround pb with JDK 1.4.1 that doesn't stop if only return
			"    return X;\n" +
			"  }\n" +
			"}";
		compileAndDeploy(sourceA007, "A007");
		String userCode =
			"new A007().foo();";
		JDIStackFrame stackFrame = new JDIStackFrame(
			this.jdiVM,
			this,
			userCode,
			"A007",
			"foo",
			-1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return X;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue("Should get one result but got " + requestor.resultIndex+1, requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "1".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A007");
	}
}
/**
 * Return x + new Object(){ int foo(int x){ return x; }}.foo(14);
 */
public void test008() throws Exception {
	try {
		String sourceA008 =
			"public class A008 {\n" +
			"  public int x = 0;\n" +
			"  public int foo() {\n" +
			"    x++;\n" + // workaround pb with JDK 1.4.1 that doesn't stop if only return
			"    return x;\n" +
			"  }\n" +
			"}";
		compileAndDeploy(sourceA008, "A008");
		String userCode =
			"new A008().foo();";
		JDIStackFrame stackFrame = new JDIStackFrame(
			this.jdiVM,
			this,
			userCode,
			"A008",
			"foo",
			-1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return x + new Object(){ int foo(int x){ return x; }}.foo(14);".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue("Should get one result but got " + requestor.resultIndex+1, requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "14".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A008");
	}
}
/**
 * Free return of local variable 's'.
 */
public void test009() throws Exception {
	String userCode =
		"String s = \"test009\";\n";
	JDIStackFrame stackFrame = new JDIStackFrame(
		this.jdiVM,
		this,
		userCode);

	DebugRequestor requestor = new DebugRequestor();
	char[] snippet = "s".toCharArray();
	evaluate(stackFrame, requestor, snippet);
	assertTrue("Should get one result but got " + requestor.resultIndex+1, requestor.resultIndex == 0);
	EvaluationResult result = requestor.results[0];
	assertTrue("Code snippet should not have problems", !result.hasProblems());
	assertTrue("Result should have a value", result.hasValue());
	assertEquals("Value", "test009".toCharArray(), result.getValueDisplayString());
	assertEquals("Type", "java.lang.String".toCharArray(), result.getValueTypeName());
}
/**
 * Return 'this'.
 */
public void test010() throws Exception {
	try {
		String sourceA010 =
			"public class A010 {\n" +
			"  public int foo() {\n" +
			"    new Object().toString();\n" + // workaround pb with JDK 1.4.1 that doesn't stop if only return
			"    return -1;\n" +
			"  }\n" +
			"}";
		compileAndDeploy(sourceA010, "A010");
		String userCode =
			"A010 a = new A010() {\n" +
			"  public String toString() {\n" +
			"    return \"my object\";\n" +
			"  }\n" +
			"};\n" +
			"a.foo();";
		JDIStackFrame stackFrame = new JDIStackFrame(
			this.jdiVM,
			this,
			userCode,
			"A010",
			"foo",
			-1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return this;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue("Should get one result but got " + requestor.resultIndex+1, requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "my object".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "A010".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A010");
	}
}
/**
 * Return local variable 'v'.
 */
public void test011() throws Exception {
	String userCode =
		"String s = \"s\";\n" +
		"java.util.Vector v = new java.util.Vector();\n" +
		"v.addElement(s);\n";
	JDIStackFrame stackFrame = new JDIStackFrame(
		this.jdiVM,
		this,
		userCode);

	DebugRequestor requestor = new DebugRequestor();
	char[] snippet = "return v;".toCharArray();
	evaluate(stackFrame, requestor, snippet);
	assertTrue("Should get one result but got " + requestor.resultIndex+1, requestor.resultIndex == 0);
	EvaluationResult result = requestor.results[0];
	assertTrue("Code snippet should not have problems", !result.hasProblems());
	assertTrue("Result should have a value", result.hasValue());
	assertEquals("Value", "[s]".toCharArray(), result.getValueDisplayString());
	assertEquals("Type", "java.util.Vector".toCharArray(), result.getValueTypeName());
}
/**
 * Set local variable 'date'.
 */
public void _test012() throws Exception {
	String userCode =
		"java.util.GregorianCalendar cal = new java.util.GregorianCalendar();\n" +
		"java.util.Date date = cal.getGregorianChange();\n" +
		"System.out.println(\"Old date =\t\" + date.toString());";
	JDIStackFrame stackFrame = new JDIStackFrame(
		this.jdiVM,
		this,
		userCode);

	DebugRequestor requestor = new DebugRequestor();
	char[] snippet = "date = new java.util.Date();".toCharArray();
	evaluate(stackFrame, requestor, snippet);
	requestor = new DebugRequestor();
	userCode = "System.out.println(\"new date =\t\" + date.toString());\n" +
				"System.out.println(\"cal.getGregorianChange() =\t\" + cal.getGregorianChange());\n" +
				"return date.after(cal.getGregorianChange());";
	snippet = userCode.toCharArray();
	evaluate(stackFrame, requestor, snippet);
	assertTrue("Should get one result but got " + requestor.resultIndex+1, requestor.resultIndex == 0);
	EvaluationResult result = requestor.results[0];
	assertTrue("Code snippet should not have problems", !result.hasProblems());
	assertTrue("Result should have a value", result.hasValue());
	assertEquals("Value", "true".toCharArray(), result.getValueDisplayString());
	assertEquals("Type", "boolean".toCharArray(), result.getValueTypeName());
}
/**
 * Set local variable 'i'.
 */
// disabled since result has problem: Pb(2) int cannot be resolved to a type
public void _test013() throws Exception {
	String userCode = "int i = 0;";
	JDIStackFrame stackFrame = new JDIStackFrame(
		this.jdiVM,
		this,
		userCode);

	DebugRequestor requestor = new DebugRequestor();
	char[] snippet = "i = -1;".toCharArray();
	evaluate(stackFrame, requestor, snippet);
	requestor = new DebugRequestor();
	snippet = "return i != 0;".toCharArray();
	evaluate(stackFrame, requestor, snippet);
	assertTrue("Should get one result but got " + requestor.resultIndex+1, requestor.resultIndex == 0);
	EvaluationResult result = requestor.results[0];
	assertTrue("Code snippet should not have problems", !result.hasProblems());
	assertTrue("Result should have a value", result.hasValue());
	assertEquals("Value", "true".toCharArray(), result.getValueDisplayString());
	assertEquals("Type", "boolean".toCharArray(), result.getValueTypeName());
}
/**
 * Set local variable 'i'.
 */
// disabled since result has problem: Pb(2) int cannot be resolved to a type
public void _test014() throws Exception {
	String userCode = "int i = 0;";
	JDIStackFrame stackFrame = new JDIStackFrame(
		this.jdiVM,
		this,
		userCode);

	DebugRequestor requestor = new DebugRequestor();
	char[] snippet = "i++;".toCharArray();
	evaluate(stackFrame, requestor, snippet);
	requestor = new DebugRequestor();
	snippet = "return i!= 0;".toCharArray();
	evaluate(stackFrame, requestor, snippet);
	assertTrue("Should get one result but got " + requestor.resultIndex+1, requestor.resultIndex == 0);
	EvaluationResult result = requestor.results[0];
	assertTrue("Code snippet should not have problems", !result.hasProblems());
	assertTrue("Result should have a value", result.hasValue());
	assertEquals("Value", "true".toCharArray(), result.getValueDisplayString());
	assertEquals("Type", "boolean".toCharArray(), result.getValueTypeName());
}
/**
 * Check java.lang.System.out != null
 */
// disabled since result has problem: Pb(2) int cannot be resolved to a type
public void _test015() throws Exception {
	String userCode = "int i = 0;";
	JDIStackFrame stackFrame = new JDIStackFrame(
		this.jdiVM,
		this,
		userCode);

	DebugRequestor requestor = new DebugRequestor();
	char[] snippet = "java.lang.System.setOut(new java.io.PrintStream(new java.io.OutputStream()));".toCharArray();
	evaluate(stackFrame, requestor, snippet);

	requestor = new DebugRequestor();
	snippet = "return java.lang.System.out != null;".toCharArray();
	evaluate(stackFrame, requestor, snippet);
	assertTrue("Should get one result but got " + requestor.resultIndex+1, requestor.resultIndex == 0);
	EvaluationResult result = requestor.results[0];
	assertTrue("Code snippet should not have problems", !result.hasProblems());
	assertTrue("Result should have a value", result.hasValue());
	assertEquals("Value", "true".toCharArray(), result.getValueDisplayString());
	assertEquals("Type", "boolean".toCharArray(), result.getValueTypeName());
}
/**
 * Check java.lang.System.out == null
 */
public void test016() throws Exception {
	String userCode = "";
	JDIStackFrame stackFrame = new JDIStackFrame(
		this.jdiVM,
		this,
		userCode);

	DebugRequestor requestor = new DebugRequestor();
	char[] snippet = "java.lang.System.setOut(null);".toCharArray();
	evaluate(stackFrame, requestor, snippet);
	requestor = new DebugRequestor();
	snippet = "return java.lang.System.out == null;".toCharArray();
	evaluate(stackFrame, requestor, snippet);
	assertTrue("Should get one result but got " + requestor.resultIndex+1, requestor.resultIndex == 0);
	EvaluationResult result = requestor.results[0];
	assertTrue("Code snippet should not have problems", !result.hasProblems());
	assertTrue("Result should have a value", result.hasValue());
	assertEquals("Value", "true".toCharArray(), result.getValueDisplayString());
	assertEquals("Type", "boolean".toCharArray(), result.getValueTypeName());
}
/**
 * Check the third prime number is 5
 */
public void test017() throws Exception {
	String userCode = "";

	JDIStackFrame stackFrame = new JDIStackFrame(this.jdiVM, this, userCode);

	DebugRequestor requestor = new DebugRequestor();
	char[] snippet =
		("class Eratosthenes {\n"
			+ "    int[] primeNumbers;\n"
			+ "\n"
			+ "    public Eratosthenes(int n) {\n"
			+ "        primeNumbers = new int[n + 1];\n"
			+ "\n"
			+ "        for (int i = 2; i <= n; i++) {\n"
			+ "            primeNumbers[i] = i;\n"
			+ "        }\n"
			+ "        int p = 2;\n"
			+ "        while (p * p <= n) {\n"
			+ "            int j = 2 * p;\n"
			+ "            while (j <= n) {\n"
			+ "                primeNumbers[j] = 0;\n"
			+ "                j += p;\n"
			+ "            }\n"
			+ "            do {\n"
			+ "                p++;\n"
			+ "            } while (primeNumbers[p] == 1);\n"
			+ "        }\n"
			+ "    }\n"
			+ "}\n"
			+ "int[] primes = new Eratosthenes(10).primeNumbers;\n"
			+ "int i = 0;\n"
			+ "int max = primes.length;\n"
			+ "int j = 0;\n"
			+ "for (; i < max && j != 3; i++) {\n"
			+ " if (primes[i] != 0) {\n"
			+ "     j++;\n"
			+ " }\n"
			+ "}\n"
			+ "return primes[i-1];").toCharArray();
	evaluate(stackFrame, requestor, snippet);
	assertTrue(
		"Should get one result but got " + (requestor.resultIndex + 1),
		requestor.resultIndex == 0);
	EvaluationResult result = requestor.results[0];
	assertTrue("Code snippet should not have problems", !result.hasProblems());
	assertTrue("Result should have a value", result.hasValue());
	assertEquals("Value", "5".toCharArray(), result.getValueDisplayString());
	assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
}
/**
 * changing the value of a public field
 */
public void test018() throws Exception {
	try {
		String sourceA018 =
			"public class A018 {\n" +
			"  public int x = 1;\n" +
			"  public int foo() {\n" +
			"    x++;\n" + // workaround pb with JDK 1.4.1 that doesn't stop if only return
			"    return x;\n" +
			"  }\n" +
			"}";
		compileAndDeploy(sourceA018, "A018");
		String userCode =
			"new A018().foo();";
		JDIStackFrame stackFrame = new JDIStackFrame(
			this.jdiVM,
			this,
			userCode,
			"A018",
			"foo",
			-1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "x = 5;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		requestor = new DebugRequestor();
		snippet = "return x;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue("Should get one result but got " + requestor.resultIndex+1, requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "5".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A018");
	}
}
/**
 * Access to super reference
 */
// disabled since result has problem: Pb(422) super cannot be used in the code snippet code
public void test019() throws Exception {
  try {
		String sourceA019 =
			"public class A019 {\n" +
			"  public int x = 1;\n" +
			"  public int foo() {\n" +
			"    x++;\n" + // workaround pb with JDK 1.4.1 that doesn't stop if only return
			"    return x;\n" +
			"  }\n" +
			"}";
		compileAndDeploy(sourceA019, "A019");
		String userCode =
			"new A019().foo();";
		JDIStackFrame stackFrame = new JDIStackFrame(
			this.jdiVM,
			this,
			userCode,
			"A019",
			"foo",
			-1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return super.clone().equals(this);".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue("Should get one result but got " + requestor.resultIndex+1, requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should have problems", result.hasProblems());
		assertTrue("Code snippet should have problems", result.hasProblems());
		assertEquals("Wrong size", 1, result.getProblems().length);
		assertEquals("Wrong pb", 422, result.getProblems()[0].getID() & IProblem.IgnoreCategoriesMask);
	} finally {
		removeTempClass("A019");
	}
}
/**
 * Implicit message expression
 */
public void test020() throws Exception {
	try {
		String sourceA =
			"public class A {\n"
				+ "\tObject o = null;\n"
				+ "\tpublic int foo() {\n"
				+ "\t\treturn 2;\n"
				+ "\t}\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "\tpublic Object bar2() {\n"
				+ "\t\treturn new Object();\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA, "A");

		String userCode = "new A().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(
				this.jdiVM,
				this,
				userCode,
				"A",
				"bar",
				-1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return foo();".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "2".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A");
	}
}
/**
 * Implicit message expression
 */
public void test021() throws Exception {
	try {
		String sourceA21 =
			"public class A21 {\n"
				+ "\tObject o = null;\n"
				+ "\tpublic int foo() {\n"
				+ "\t\treturn 2;\n"
				+ "\t}\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "\tpublic Object bar2() {\n"
				+ "\t\treturn \"toto\";\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA21, "A21");

		String userCode = "new A21().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(
				this.jdiVM,
				this,
				userCode,
				"A21",
				"bar",
				-1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "o = bar2();".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		requestor = new DebugRequestor();
		snippet = "return o;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "toto".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "java.lang.Object".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A21");
	}
}
/**
 * Qualified Name Reference: b.s
 */
public void test022() throws Exception {
	try {
		String sourceB22 =
			"public class B22 {\n"
				+ "\tpublic String s = null;\n"
				+ "}";
		compileAndDeploy(sourceB22, "B22");

		String sourceA22 =
			"public class A22 {\n"
				+ "\tpublic B22 b = new B22();\n"
				+ "\tpublic int foo() {\n"
				+ "\t\treturn 2;\n"
				+ "\t}\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "\tpublic Object bar2() {\n"
				+ "\t\treturn \"toto\";\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA22, "A22");

		String userCode = "new A22().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(
				this.jdiVM,
				this,
				userCode,
				"A22",
				"bar",
				-1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "b.s = \"toto\"".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());

		requestor = new DebugRequestor();
		snippet = "return b.s;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "toto".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "java.lang.String".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("B22");
		removeTempClass("A22");
	}
}
/**
 * Qualified Name Reference: b.c.c
 */
public void test023() throws Exception {
	try {
		String sourceC23 =
			"public class C23 {\n"
				+ "\tpublic String c = null;\n"
				+ "}";
		compileAndDeploy(sourceC23, "C23");
		String sourceB23 =
			"public class B23 {\n"
				+ "\tpublic C23 c = new C23();\n"
				+ "}";
		compileAndDeploy(sourceB23, "B23");

		String sourceA23 =
			"public class A23 {\n"
				+ "\tpublic B23 b = new B23();\n"
				+ "\tpublic int foo() {\n"
				+ "\t\treturn 2;\n"
				+ "\t}\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "\tpublic Object bar2() {\n"
				+ "\t\treturn \"toto\";\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA23, "A23");

		String userCode = "new A23().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(
				this.jdiVM,
				this,
				userCode,
				"A23",
				"bar",
				-1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "b.c.c = \"toto\"".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());

		requestor = new DebugRequestor();
		snippet = "return b.c.c;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "toto".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "java.lang.String".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("C23");
		removeTempClass("B23");
		removeTempClass("A23");
	}
}
/**
 * Array Reference
 */
public void test024() throws Exception {
	try {
		String sourceC24 =
			"public class C24 {\n"
				+ "\tpublic int[] tab = {1,2,3,4,5};\n"
				+ "}";
		compileAndDeploy(sourceC24, "C24");


		String sourceB24 =
			"public class B24 {\n"
				+ "\tpublic C24 c = new C24();\n"
				+ "}";
		compileAndDeploy(sourceB24, "B24");

		String sourceA24 =
			"public class A24 {\n"
				+ "\tpublic B24 b = new B24();\n"
				+ "\tpublic int foo() {\n"
				+ "\t\treturn 2;\n"
				+ "\t}\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "\tpublic Object bar2() {\n"
				+ "\t\treturn \"toto\";\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA24, "A24");

		String userCode = "new A24().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(
				this.jdiVM,
				this,
				userCode,
				"A24",
				"bar",
				-1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "b.c.tab[3] = 8".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());

		requestor = new DebugRequestor();
		snippet = "return b.c.tab[3];".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "8".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("C24");
		removeTempClass("B24");
		removeTempClass("A24");
	}
}
/**
 * Array Reference
 */
public void test025() throws Exception {
	try {
		String sourceA25 =
			"public class A25 {\n"
				+ "\tpublic String[] tabString = new String[2];\n"
				+ "\tpublic int foo() {\n"
				+ "\t\treturn 2;\n"
				+ "\t}\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "\tpublic Object bar2() {\n"
				+ "\t\treturn \"toto\";\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA25, "A25");

		String userCode = "new A25().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(
				this.jdiVM,
				this,
				userCode,
				"A25",
				"bar",
				-1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "tabString[1] = \"toto\"".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());

		requestor = new DebugRequestor();
		snippet = "return tabString[1];".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "toto".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "java.lang.String".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A25");
	}
}
/**
 * Array Reference
 */
public void test026() throws Exception {
	try {
		String sourceA26 =
			"public class A26 {\n"
				+ "\tpublic int foo() {\n"
				+ "\t\treturn 2;\n"
				+ "\t}\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA26, "A26");

		String userCode = "new A26().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(
				this.jdiVM,
				this,
				userCode,
				"A26",
				"bar",
				-1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet =
			("int[] tab = new int[1];\n"
			+ "tab[0] = foo();\n"
			+ "tab[0]").toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "2".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A26");
	}
}
/**
 * Array Reference
 */
public void test027() throws Exception {
	try {
		String sourceA27 =
			"public class A27 {\n"
				+ "\tpublic int foo() {\n"
				+ "\t\treturn 2;\n"
				+ "\t}\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "\tpublic int bar2(int i) {\n"
				+ "\t\tif (i == 2) {\n"
				+ "\t\t\treturn 3;\n"
				+ "\t\t} else {\n"
				+ "\t\t\treturn 4;\n"
				+ "\t\t}\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA27, "A27");

		String userCode = "new A27().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(
				this.jdiVM,
				this,
				userCode,
				"A27",
				"bar",
				-1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet =
			("int[] tab = new int[] { 1, 2, 3, 4, 5};\n"
			+ "switch(foo()) {\n"
			+ "case 1 : return -1;\n"
			+ "case 2 : return tab[bar2(foo())];\n"
			+ "default: return -5;}").toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "4".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A27");
	}
}
/**
 * Array Reference
 */
public void test028() throws Exception {
	try {
		String sourceA28 =
			"public class A28 {\n"
				+ "\tpublic int foo() {\n"
				+ "\t\treturn 2;\n"
				+ "\t}\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "\tpublic int bar2(int i) {\n"
				+ "\t\tif (i == 2) {\n"
				+ "\t\t\treturn 3;\n"
				+ "\t\t} else {\n"
				+ "\t\t\treturn 4;\n"
				+ "\t\t}\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA28, "A28");

		String userCode = "new A28().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(
				this.jdiVM,
				this,
				userCode,
				"A28",
				"bar",
				-1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet =
			("int[] tab = new int[] { 1, 2, 3, 4, 5};\n"
			+ "int i =3;\n"
			+ "switch(foo()) {\n"
			+ "case 0 : return -1;\n"
			+ "case 1 : return tab[bar2(foo())];\n"
			+ "}\n"
			+ "return tab[i++];").toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "4".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A28");
	}
}
/**
 * Array Reference
 */
public void test029() throws Exception {
	try {
		String sourceA29 =
			"public class A29 {\n"
				+ "\tpublic int foo() {\n"
				+ "\t\treturn 2;\n"
				+ "\t}\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "\tpublic int bar2(int i) {\n"
				+ "\t\tif (i == 2) {\n"
				+ "\t\t\treturn 3;\n"
				+ "\t\t} else {\n"
				+ "\t\t\treturn 4;\n"
				+ "\t\t}\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA29, "A29");

		String userCode = "new A29().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(
				this.jdiVM,
				this,
				userCode,
				"A29",
				"bar",
				-1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet =
			("int[] tab = new int[] { 1, 2, 3, 4, 5};\n"
			+ "int i =3;\n"
			+ "switch(foo()) {\n"
			+ "case 0 : return -1;\n"
			+ "case 1 : return tab[bar2(foo())];\n"
			+ "}\n"
			+ "return tab[++i];").toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "5".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A29");
	}
}
/**
 * Array Reference: ArrayIndexOutOfBoundException
 */
public void test030() throws Exception {
	try {
		String sourceA30 =
			"public class A30 {\n"
				+ "\tpublic int foo() {\n"
				+ "\t\treturn 2;\n"
				+ "\t}\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "\tpublic int bar2(int i) {\n"
				+ "\t\tif (i == 2) {\n"
				+ "\t\t\treturn 3;\n"
				+ "\t\t} else {\n"
				+ "\t\t\treturn 4;\n"
				+ "\t\t}\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA30, "A30");

		String userCode = "new A30().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(
				this.jdiVM,
				this,
				userCode,
				"A30",
				"bar",
				-1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet =
			("try {\n"
			+ "int[] tab = new int[] { 1, 2, 3, 4};\n"
			+ "int i =3;\n"
			+ "switch(foo()) {\n"
			+ "case 0 : return -1;\n"
			+ "case 1 : return tab[bar2(foo())];\n"
			+ "}\n"
			+ "return tab[++i];"
			+ "} catch(ArrayIndexOutOfBoundsException e) {\n"
			+ "return -2;\n"
			+ "}").toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "-2".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A30");
	}
}
/**
 * Read access to an instance private member of the enclosing class
 */
public void test031() throws Exception {
	try {
		String sourceA31 =
			"public class A31 {\n"
				+ "\tprivate int i = 2;\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA31, "A31");

		String userCode = "new A31().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(
				this.jdiVM,
				this,
				userCode,
				"A31",
				"bar",
				-1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return i;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "2".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A31");
	}
}
/**
 * Read access to a instance private member of the class different from the enclosing class
 */
public void test032() throws Exception {
	try {
		String sourceA32 =
			"public class A32 {\n"
				+ "\tprivate int i = 2;\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA32, "A32");

		String sourceB32 =
			"public class B32 {\n"
				+ "\tprivate int j = 2;\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceB32, "B32");

		String userCode = "new A32().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(
				this.jdiVM,
				this,
				userCode,
				"A32",
				"bar",
				-1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return new B32().j;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", result.hasProblems());
		CategorizedProblem[] problems = result.getProblems();
		StringBuilder buffer = null;
		for (int i = 0, max = problems.length; i < max; i++){
			if (problems[i].isError()){
				if (buffer == null) buffer = new StringBuilder(10);
				buffer.append(problems[i].getMessage());
				buffer.append('|');
			}
		}
		assertEquals("Unexpected errors",
			"The field B32.j is not visible|",
			buffer == null ? "none" : buffer.toString());
	} finally {
		removeTempClass("B32");
		removeTempClass("A32");
	}
}
/**
 * Read access to an instance private member of the enclosing class
 */
public void test033() throws Exception {
	try {
		String sourceA33 =
			"public class A33 {\n"
				+ "\tprivate long l = 2000000L;\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA33, "A33");

		String userCode = "new A33().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(
				this.jdiVM,
				this,
				userCode,
				"A33",
				"bar",
				-1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet =
			("try {\n" +
			"Class c = Class.forName(\"A33\");\n" +
			"java.lang.reflect.Field field = c.getDeclaredField(\"l\");\n" +
			"field.setAccessible(true);\n" +
			"java.lang.reflect.Constructor constr = c.getConstructor(new Class[] {});\n" +
			"Object o = constr.newInstance(new Object[]{});\n" +
			"System.out.println(field.getInt(o));\n" +
			"} catch(Exception e) {}\n" +
			"return l;").toCharArray();
		final Map compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.IGNORE);

		this.context.evaluate(
			snippet,
			stackFrame.localVariableTypeNames(),
			stackFrame.localVariableNames(),
			stackFrame.localVariableModifiers(),
			stackFrame.declaringTypeName(),
			stackFrame.isStatic(),
			stackFrame.isConstructorCall(),
			getEnv(),
			compilerOptions,
			requestor,
			getProblemFactory());
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "2000000".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "long".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A33");
	}
}
/**
 * Write access to an instance private member of the enclosing class
 */
public void test034() throws Exception {
	try {
		String sourceA34 =
			"public class A34 {\n"
				+ "\tprivate long l = 2000000L;\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA34, "A34");

		String userCode = "new A34().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A34", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet =
			("l = 100L;\n" +
			"return l;").toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "100".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "long".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A34");
	}
}
/**
 * Read access to a static private member of the enclosing class
 */
public void test035() throws Exception {
	try {
		String sourceA35 =
			"public class A35 {\n"
				+ "\tstatic private int i = 2;\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA35, "A35");

		String userCode = "new A35().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(
				this.jdiVM,
				this,
				userCode,
				"A35",
				"bar",
				-1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return i;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "2".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A35");
	}
}
/**
 * Coumpound assignement to an instance private member of the enclosing class
 */
public void test036() throws Exception {
	try {
		String sourceA36 =
			"public class A36 {\n"
				+ "\tprivate long l = 2000000L;\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA36, "A36");

		String userCode = "new A36().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A36", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet =
			("l+=4;\n" +
			"return l;").toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "2000004".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "long".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A36");
	}
}
/**
 * Coumpound assignement to an instance private member of the enclosing class
 */
public void test037() throws Exception {
	try {
		String sourceA37 =
			"public class A37 {\n"
				+ "\tprivate long l = 2000000L;\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA37, "A37");

		String userCode = "new A37().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A37", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet =
			("l++;\n" +
			"return l;").toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "2000001".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "long".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A37");
	}
}
/**
 * Coumpound assignement to an instance private member of the enclosing class
 */
public void test038() throws Exception {
	try {
		String sourceA38 =
			"public class A38 {\n"
				+ "\tprivate long l = 2000000L;\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA38, "A38");

		String userCode = "new A38().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A38", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return l++;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "2000000".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "long".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A38");
	}
}
/**
 * Coumpound assignement to an static private member of the enclosing class
 */
public void test039() throws Exception {
	try {
		String sourceA39 =
			"public class A39 {\n"
				+ "\tstatic private int i = 2;\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA39, "A39");

		String userCode = "new A39().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A39", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return A39.i;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "2".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A39");
	}
}
/**
 * Coumpound assignement to an static private member of the enclosing class
 */
public void test040() throws Exception {
	try {
		String sourceA40 =
			"public class A40 {\n"
				+ "\tstatic private int[] tab = new int[] {1, 2};\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA40, "A40");

		String userCode = "new A40().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A40", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return A40.tab.length;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "2".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A40");
	}
}
/**
 * Coumpound assignement to an static private final member of the enclosing class
 */
public void test041() throws Exception {
	try {
		String sourceA41 =
			"public class A41 {\n"
				+ "\tstatic private final int[] tab = new int[] {1, 2};\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA41, "A41");

		String userCode = "new A41().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A41", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return A41.tab.length;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "2".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A41");
	}
}
/**
 * Coumpound assignement to an static private final member of the enclosing class
 */
public void test042() throws Exception {
	try {
		String sourceA42 =
			"public class A42 {\n"
				+ "\tstatic private int Counter = 0;\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA42, "A42");

		String userCode = "new A42().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A42", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return ++A42.Counter;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "1".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A42");
	}
}
/**
 * Coumpound assignement to an static private final member of the enclosing class
 */
public void test043() throws Exception {
	try {
		String sourceA43 =
			"public class A43 {\n"
				+ "\tstatic private int Counter = 0;\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA43, "A43");

		String userCode = "new A43().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A43", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "A43.Counter++; return A43.Counter;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "1".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A43");
	}
}
/**
 * Coumpound assignement to an static private final member of the enclosing class
 */
public void test044() throws Exception {
	try {
		String sourceA44 =
			"public class A44 {\n"
				+ "\tstatic private int Counter = 0;\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA44, "A44");

		String userCode = "new A44().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A44", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "int j = A44.Counter++; return A44.Counter + j;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "1".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A44");
	}
}
/**
 * Coumpound assignement to an static private final member of the enclosing class
 */
public void test045() throws Exception {
	try {
		String sourceA45 =
			"public class A45 {\n"
				+ "\tstatic private int Counter = 0;\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA45, "A45");

		String userCode = "new A45().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A45", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "int j = ++A45.Counter; return A45.Counter + j;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "2".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A45");
	}
}
/**
 * Coumpound assignement to an static protected final member of the enclosing class
 */
public void test046() throws Exception {
	try {
		String sourceA46 =
			"public class A46 {\n"
				+ "\tstatic protected int Counter = 0;\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA46, "A46");

		String userCode = "new A46().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A46", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "int j = ++A46.Counter; return A46.Counter + j;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "2".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A46");
	}
}
/**
 * Return the value of a private static field throught a private static field
 */
public void test047() throws Exception {
	try {
		String sourceA47 =
			"public class A47 {\n"
				+ "\tstatic private A47 instance = new A47();\n"
				+ "\tstatic private int Counter = 2;\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA47, "A47");

		String userCode = "new A47().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A47", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return A47.instance.Counter;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "2".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A47");
	}
}
/**
 * Return the value of a private static field throught a private static field
 * Using private field emulation on a field reference.
 */
public void test048() throws Exception {
	try {
		String sourceA48 =
			"public class A48 {\n"
				+ "\tstatic private A48 instance = new A48();\n"
				+ "\tstatic private int Counter = 2;\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA48, "A48");

		String userCode = "new A48().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A48", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return new A48().instance.Counter;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "2".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A48");
	}
}
/**
 * Compound assignment of a private field.
 * Using private field emulation on a field reference.
 */
public void test049() throws Exception {
	try {
		String sourceA49 =
			"public class A49 {\n"
				+ "\tstatic private A49 instance = new A49();\n"
				+ "\tstatic private int Counter = 2;\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA49, "A49");

		String userCode = "new A49().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A49", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return ++(new A49().Counter);".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "3".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A49");
	}
}
/**
 * Compound assignment of a private field.
 * Using private field emulation on a field reference.
 */
public void test050() throws Exception {
	try {
		String sourceA50 =
			"public class A50 {\n"
				+ "\tstatic private A50 instance = new A50();\n"
				+ "\tstatic private int Counter = 2;\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA50, "A50");

		String userCode = "new A50().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A50", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "A50 a = new A50(); a.Counter = 5; return a.Counter;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "5".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A50");
	}
}
/**
 * Assignment of a private field.
 * Using private field emulation on a field reference.
 */
public void test051() throws Exception {
	try {
		String sourceA51 =
			"public class A51 {\n"
				+ "\tstatic private A51 instance = new A51();\n"
				+ "\tstatic private int Counter = 2;\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA51, "A51");

		String userCode = "new A51().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A51", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "Counter = 5; return Counter;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "5".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A51");
	}
}
/**
 * Assignment of a private field.
 * Using private field emulation on a field reference.
 */
public void test052() throws Exception {
	try {
		String sourceA52 =
			"public class A52 {\n"
				+ "\tstatic private A52 instance = new A52();\n"
				+ "\tstatic private int Counter = 2;\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA52, "A52");

		String userCode = "new A52().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A52", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "this.Counter = 5; return this.Counter;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "5".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A52");
	}
}
/**
 * Post assignement of a private field.
 * Using private field emulation on a field reference.
 */
public void test053() throws Exception {
	try {
		String sourceA53 =
			"public class A53 {\n"
				+ "\tstatic private A53 instance = new A53();\n"
				+ "\tstatic private int Counter = 2;\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA53, "A53");

		String userCode = "new A53().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A53", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "this.Counter++; return this.Counter;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "3".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A53");
	}
}
/**
 * Post assignement of a private field.
 * Using private field emulation on a field reference.
 */
public void test054() throws Exception {
	try {
		String sourceA54 =
			"public class A54 {\n"
				+ "\tstatic private A54 instance = new A54();\n"
				+ "\tstatic private long Counter = 2L;\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA54, "A54");

		String userCode = "new A54().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A54", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "this.Counter++; return this.Counter;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "3".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "long".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A54");
	}
}
/**
 * Read access to a private method.
 */
public void test055() throws Exception {
	try {
		String sourceA55 =
			"public class A55 {\n"
				+ "\tprivate int foo() {;\n"
				+ "\t\treturn 3;\n"
				+ "\t}\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA55, "A55");

		String userCode = "new A55().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A55", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return foo();".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "3".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A55");
	}
}
/**
 * Read access to a private method.
 */
public void test056() throws Exception {
	try {
		String sourceA56 =
			"public class A56 {\n"
				+ "\tprivate Integer foo() {;\n"
				+ "\t\treturn new Integer(3);\n"
				+ "\t}\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA56, "A56");

		String userCode = "new A56().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A56", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return foo().intValue();".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "3".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A56");
	}
}
/**
 * Read access to a private method.
 */
public void test057() throws Exception {
	try {
		String sourceA57 =
			"public class A57 {\n"
				+ "\tprivate Integer foo(int i) {;\n"
				+ "\t\treturn new Integer(i);\n"
				+ "\t}\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA57, "A57");

		String userCode = "new A57().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A57", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return foo(3).intValue();".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "3".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A57");
	}
}
/**
 * Read access to a private method.
 */
public void test058() throws Exception {
	try {
		String sourceA58 =
			"public class A58 {\n"
				+ "\tprivate Integer foo(int i, int[] tab) {;\n"
				+ "\t\treturn new Integer(i + tab.length);\n"
				+ "\t}\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA58, "A58");

		String userCode = "new A58().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A58", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "int[] tab = new int[] {1,2,3};return foo(0, tab).intValue();".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "3".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A58");
	}
}
/**
 * Read access to a private method.
 */
public void test059() throws Exception {
	try {
		String sourceA59 =
			"public class A59 {\n"
				+ "\tprivate Integer foo(int i, Object[][] tab) {;\n"
				+ "\t\treturn new Integer(i + tab.length);\n"
				+ "\t}\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA59, "A59");

		String userCode = "new A59().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A59", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "Object[][] tab = new Object[0][0];return foo(3, tab).intValue();".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "3".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A59");
	}
}
/**
 * Read access to a private method.
 */
public void test060() throws Exception {
	try {
		String sourceA60 =
			"public class A60 {\n"
				+ "\tprivate int i;\n"
				+ "\tpublic A60() {;\n"
				+ "\t}\n"
				+ "\tprivate A60(int i) {;\n"
				+ "\t\tthis.i = i;\n"
				+ "\t}\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA60, "A60");

		String userCode = "new A60().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A60", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return new A60(3).i;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "3".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A60");
	}
}
/**
 * Read access to a private method.
 */
public void test061() throws Exception {
	try {
		String sourceA61 =
			"public class A61 {\n"
				+ "\tprivate int i;\n"
				+ "\tpublic A61() {;\n"
				+ "\t}\n"
				+ "\tprivate A61(int[] tab) {;\n"
				+ "\t\tthis.i = tab.length;\n"
				+ "\t}\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy(sourceA61, "A61");

		String userCode = "new A61().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A61", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return new A61(new int[] {1,2,3}).i;".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "3".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A61");
	}
}
/**
 * Static context with a declaring type.
 */
public void test062() throws Exception {
	try {
		String sourceA62 =
			"public class A62 {\n" +
			"  public static void bar() {\n" +
			"  }\n" +
			"}";
		compileAndDeploy(sourceA62, "A62");

		String userCode = "new A62().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A62", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "1 + 1".toCharArray();
		evaluate(stackFrame, requestor, snippet);
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "2".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A62");
	}
}
/**
 * Return non-static field in static environment.
 */
public void testNegative001() throws InstallException {
	try {
		String sourceANegative001 =
			"public class ANegative001 {\n" +
			"  public int x = 1;\n" +
			"  public int foo() {\n" +
			"    x++;\n" + // workaround pb with JDK 1.4.1 that doesn't stop if only return
			"    return x;\n" +
			"  }\n" +
			"}";
		compileAndDeploy(sourceANegative001, "ANegative001");
		String userCode =
			"new ANegative001().foo();";
		JDIStackFrame stackFrame = new JDIStackFrame(
			this.jdiVM,
			this,
			userCode,
			"ANegative001",
			"foo",
			-1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return this.x;".toCharArray();
		this.context.evaluate(
			snippet,
			stackFrame.localVariableTypeNames(),
			stackFrame.localVariableNames(),
			stackFrame.localVariableModifiers(),
			stackFrame.declaringTypeName(),
			true, // force is static
			stackFrame.isConstructorCall(),
			getEnv(),
			getCompilerOptions(),
			requestor,
			getProblemFactory());
		assertTrue("Got one result", requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		CategorizedProblem[] problems = result.getProblems();
		StringBuilder buffer = null;
		for (int i = 0, max = problems.length; i < max; i++){
			if (problems[i].isError()){
				if (buffer == null) buffer = new StringBuilder(10);
				buffer.append(problems[i].getMessage());
				buffer.append('|');
			}
		}
		assertEquals("Unexpected errors",
			"Cannot use this in a static context|",
			buffer == null ? "none" : buffer.toString());
	} finally {
		removeTempClass("ANegative001");
	}
}
/**
 * Return non-static field in static environment.
 */
public void testNegative002() throws Exception {
	try {
		String sourceANegative002 =
			"public class ANegative002 {\n" +
			"  public int x = 1;\n" +
			"  public int foo() {\n" +
			"    x++;\n" + // workaround pb with JDK 1.4.1 that doesn't stop if only return
			"    return x;\n" +
			"  }\n" +
			"}";
		compileAndDeploy(sourceANegative002, "ANegative002");
		String userCode =
			"new ANegative002().foo();";
		JDIStackFrame stackFrame = new JDIStackFrame(
			this.jdiVM,
			this,
			userCode,
			"ANegative002",
			"foo",
			-1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return x;".toCharArray();
		this.context.evaluate(
			snippet,
			stackFrame.localVariableTypeNames(),
			stackFrame.localVariableNames(),
			stackFrame.localVariableModifiers(),
			stackFrame.declaringTypeName(),
			true, // force is static
			stackFrame.isConstructorCall(),
			getEnv(),
			getCompilerOptions(),
			requestor,
			getProblemFactory());
		assertTrue("Got one result", requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		CategorizedProblem[] problems = result.getProblems();
		StringBuilder buffer = null;
		for (int i = 0, max = problems.length; i < max; i++){
			if (problems[i].isError()){
				if (buffer == null) buffer = new StringBuilder(10);
				buffer.append(problems[i].getMessage());
				buffer.append('|');
			}
		}
		assertEquals("Unexpected errors",
			"Cannot make a static reference to the non-static field x|",
			buffer == null ? "none" : buffer.toString());
	} finally {
		removeTempClass("ANegative002");
	}
}
/**
 * Return inexisting field in static environment.
 */
public void testNegative003() throws InstallException {
	try {
		String sourceANegative003 =
			"public class ANegative003 {\n" +
			"  public int x = 1;\n" +
			"  public int foo() {\n" +
			"    x++;\n" + // workaround pb with JDK 1.4.1 that doesn't stop if only return
			"    return x;\n" +
			"  }\n" +
			"}";
		compileAndDeploy(sourceANegative003, "ANegative003");
		String userCode =
			"new ANegative003().foo();";
		JDIStackFrame stackFrame = new JDIStackFrame(
			this.jdiVM,
			this,
			userCode,
			"ANegative003",
			"foo",
			-1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return zork;".toCharArray();
		this.context.evaluate(
			snippet,
			stackFrame.localVariableTypeNames(),
			stackFrame.localVariableNames(),
			stackFrame.localVariableModifiers(),
			stackFrame.declaringTypeName(),
			true, // force is static
			stackFrame.isConstructorCall(),
			getEnv(),
			getCompilerOptions(),
			requestor,
			getProblemFactory());
		assertTrue("Got one result", requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		CategorizedProblem[] problems = result.getProblems();
		StringBuilder buffer = null;
		for (int i = 0, max = problems.length; i < max; i++){
			if (problems[i].isError()){
				if (buffer == null) buffer = new StringBuilder(10);
				buffer.append(problems[i].getMessage());
				buffer.append('|');
			}
		}
		assertEquals("Unexpected errors",
			"zork cannot be resolved to a variable|",
			buffer == null ? "none" : buffer.toString());
	} finally {
		removeTempClass("ANegative003");
	}
}
/**
 * Check java.lang.System.out = null returns an error
 */
public void testNegative004() throws InstallException {
	String userCode = "";
	JDIStackFrame stackFrame = new JDIStackFrame(
		this.jdiVM,
		this,
		userCode);

	DebugRequestor requestor = new DebugRequestor();
	char[] snippet = "java.lang.System.out = null;".toCharArray();
	evaluate(stackFrame, requestor, snippet);
	assertTrue("Got one result", requestor.resultIndex == 0);
	EvaluationResult result = requestor.results[0];
	CategorizedProblem[] problems = result.getProblems();
	StringBuilder buffer = null;
	for (int i = 0, max = problems.length; i < max; i++){
		if (problems[i].isError()){
			if (buffer == null) buffer = new StringBuilder(10);
			buffer.append(problems[i].getMessage());
			buffer.append('|');
		}
	}
	assertEquals("Unexpected errors",
		"The final field System.out cannot be assigned|",
		buffer == null ? "none" : buffer.toString());
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=102778
 */
public void test063() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	try {
		String sourceA63 =
			"public class A63 {\n" +
			"  public static void bar() {\n" +
			"  }\n" +
			"}";
		compileAndDeploy15(sourceA63, "A63");

		String userCode = "new A63().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A63", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = ("int[] tab = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9 };\n" +
				"int sum = 0;\n" +
				"for (int i : tab) {\n" +
				"	sum += i;\n" +
				"}\n" +
				"sum").toCharArray();
		Map compilerOpts = getCompilerOptions();
		compilerOpts.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.getFirstSupportedJavaVersion());
		compilerOpts.put(CompilerOptions.OPTION_Source, CompilerOptions.getFirstSupportedJavaVersion());
		compilerOpts.put(CompilerOptions.OPTION_Compliance, CompilerOptions.getFirstSupportedJavaVersion());

		this.context.evaluate(
			snippet,
			stackFrame.localVariableTypeNames(),
			stackFrame.localVariableNames(),
			stackFrame.localVariableModifiers(),
			stackFrame.declaringTypeName(),
			stackFrame.isStatic(),
			stackFrame.isConstructorCall(),
			getEnv(),
			compilerOpts,
			requestor,
			getProblemFactory());
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "45".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A62");
	}
}
public void test065() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	try {
		String sourceA65 =
			"public class A65<T> {\n"
				+ "\tprivate int i;\n"
				+ "\tpublic <U>A65() {;\n"
				+ "\t}\n"
				+ "\tprivate <U>A65(int i) {;\n"
				+ "\t\tthis.i = i;\n"
				+ "\t}\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy15(sourceA65, "A65");

		String userCode = "new <Object>A65<Object>().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A65", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return new <Object>A65<Object>(3).i;".toCharArray();
		try {
			this.context.evaluate(
				snippet,
				stackFrame.localVariableTypeNames(),
				stackFrame.localVariableNames(),
				stackFrame.localVariableModifiers(),
				stackFrame.declaringTypeName(),
				stackFrame.isStatic(),
				stackFrame.isConstructorCall(),
				getEnv(),
				getCompilerOptions(),
				requestor,
				getProblemFactory());
		} catch (InstallException e) {
			assertTrue("No targetException " + e.getMessage(), false);
		}
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "3".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A65");
	}
}
public void test066() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	try {
		String sourceA66 =
			"public class A66 {\n"
				+ "\tprivate int i;\n"
				+ "\tpublic A66() {;\n"
				+ "\t}\n"
				+ "\tprivate <U> int foo(int i) {;\n"
				+ "\t\treturn i;\n"
				+ "\t}\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy15(sourceA66, "A66");

		String userCode = "new A66().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A66", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return this.<Object>foo(3);".toCharArray();
		try {
			this.context.evaluate(
				snippet,
				stackFrame.localVariableTypeNames(),
				stackFrame.localVariableNames(),
				stackFrame.localVariableModifiers(),
				stackFrame.declaringTypeName(),
				stackFrame.isStatic(),
				stackFrame.isConstructorCall(),
				getEnv(),
				getCompilerOptions(),
				requestor,
				getProblemFactory());
		} catch (InstallException e) {
			assertTrue("No targetException " + e.getMessage(), false);
		}
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "3".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "int".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A66");
	}
}
public void test067() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	try {
		String sourceA67 =
			"import java.util.List;\n" +
			"public class A67<T> {\n" +
			"	public static String toString(List<?> list) {\n" +
			"		StringBuilder builder = new StringBuilder(\"{\");\n" +
			"		for (Object o : list) {" +
			"			builder.append(o);\n" +
			"		}\n" +
			"		builder.append(\"}\");\n" +
			"		return String.valueOf(builder);\n" +
			"	}\n" +
			"	public void bar() {\n" +
			"	}\n" +
			"}";
		compileAndDeploy15(sourceA67, "A67");

		String userCode = "new A67<Object>().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A67", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = ("java.util.ArrayList<String> list = new java.util.ArrayList<String>();\n" +
				"list.add(\"Test\");\n" +
				"list.add(\"Hello\");\n" +
				"list.add(\"World\");\n" +
				"return A67.toString(list);").toCharArray();
		try {
			this.context.evaluate(
				snippet,
				stackFrame.localVariableTypeNames(),
				stackFrame.localVariableNames(),
				stackFrame.localVariableModifiers(),
				stackFrame.declaringTypeName(),
				stackFrame.isStatic(),
				stackFrame.isConstructorCall(),
				getEnv(),
				getCompilerOptions(),
				requestor,
				getProblemFactory());
		} catch (InstallException e) {
			assertTrue("No targetException " + e.getMessage(), false);
		}
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", !result.hasProblems());
		assertTrue("Result should have a value", result.hasValue());
		assertEquals("Value", "{TestHelloWorld}".toCharArray(), result.getValueDisplayString());
		assertEquals("Type", "java.lang.String".toCharArray(), result.getValueTypeName());
	} finally {
		removeTempClass("A67");
	}
}
public void test068() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	try {
		String sourceSuperA68 =
			"public class SuperA68 {\n"
				+ "\tprivate int i;\n"
				+ "\tpublic SuperA68() {\n"
				+ "\t}\n"
				+ "\tpublic <U> int foo(int i) {;\n"
				+ "\t\treturn i;\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy15(sourceSuperA68, "SuperA68");
		String sourceA68 =
			"public class A68 extends SuperA68 {\n"
				+ "\tprivate int i;\n"
				+ "\tpublic A68() {\n"
				+ "\t}\n"
				+ "\tpublic <U> int foo(int i) {\n"
				+ "\t\treturn i;\n"
				+ "\t}\n"
				+ "\tpublic void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy15(sourceA68, "A68");

		String userCode = "new A68().bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A68", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "return super.<Object>foo(3);".toCharArray();
		try {
			this.context.evaluate(
				snippet,
				stackFrame.localVariableTypeNames(),
				stackFrame.localVariableNames(),
				stackFrame.localVariableModifiers(),
				stackFrame.declaringTypeName(),
				stackFrame.isStatic(),
				stackFrame.isConstructorCall(),
				getEnv(),
				getCompilerOptions(),
				requestor,
				getProblemFactory());
		} catch (InstallException e) {
			assertTrue("No targetException " + e.getMessage(), false);
		}
		assertTrue(
			"Should get one result but got " + (requestor.resultIndex + 1),
			requestor.resultIndex == 0);
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should have problems", result.hasProblems());
		assertEquals("Wrong size", 1, result.getProblems().length);
		assertEquals("Wrong pb", 422, result.getProblems()[0].getID() & IProblem.IgnoreCategoriesMask);
	} finally {
		removeTempClass("A68");
		removeTempClass("SuperA68");
	}
}
public void test069() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	try {
		String sourceA69 =
			"public enum A69 {\n"
				+ "\tA(2), B(1);\n"
				+ "\tprivate int i;\n"
				+ "\tprivate A69(int i) {\n"
				+ "\t\tthis.i = i;\n"
				+ "\t}\n"
				+ "\tpublic String toString() {\n"
				+ "\t\treturn String.valueOf(this.i);\n"
				+ "\t}\n"
				+ "\tpublic static void bar() {\n"
				+ "\t}\n"
				+ "}";
		compileAndDeploy15(sourceA69, "A69");

		String userCode = "A69.bar();";
		JDIStackFrame stackFrame =
			new JDIStackFrame(this.jdiVM, this, userCode, "A69", "bar", -1);

		DebugRequestor requestor = new DebugRequestor();
		char[] snippet = "enum E { C }; return String.toString(E.C.getName());".toCharArray();
		try {
			this.context.evaluate(
				snippet,
				stackFrame.localVariableTypeNames(),
				stackFrame.localVariableNames(),
				stackFrame.localVariableModifiers(),
				stackFrame.declaringTypeName(),
				stackFrame.isStatic(),
				stackFrame.isConstructorCall(),
				getEnv(),
				getCompilerOptions(),
				requestor,
				getProblemFactory());
		} catch (InstallException e) {
			assertTrue("No targetException " + e.getMessage(), false);
		}
		boolean is16Plus = 	this.complianceLevel >= ClassFileConstants.JDK16;
		if (is16Plus) {
			assertTrue(
					"Should get one result but got " + (requestor.resultIndex + 1),
					requestor.resultIndex == 0);
		} else {
			assertTrue(
					"Should get two results but got " + (requestor.resultIndex + 1),
					requestor.resultIndex == 1);
		}
		EvaluationResult result = requestor.results[0];
		assertTrue("Code snippet should not have problems", result.hasProblems());
		assertEquals("Wrong size", 1, result.getProblems().length);
		assertEquals("Wrong pb", is16Plus ? 100 : 31, result.getProblems()[0].getID() & IProblem.IgnoreCategoriesMask);
		if (!is16Plus) {
			result = requestor.results[1];
			assertTrue("Code snippet should not have problems", result.hasProblems());
			assertEquals("Wrong size", 1, result.getProblems().length);
			assertEquals("Wrong pb", 50, result.getProblems()[0].getID() & IProblem.IgnoreCategoriesMask);
		}
	} finally {
		removeTempClass("A69");
	}
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=178861
 */
public void testNegative005() throws InstallException {
	String userCode = "";
	JDIStackFrame stackFrame = new JDIStackFrame(
		this.jdiVM,
		this,
		userCode);

	DebugRequestor requestor = new DebugRequestor();
	char[] snippet = "run()".toCharArray();
	evaluate(stackFrame, requestor, snippet);
	assertTrue("Got one result", requestor.resultIndex == 0);
	EvaluationResult result = requestor.results[0];
	CategorizedProblem[] problems = result.getProblems();
	StringBuilder buffer = null;
	for (int i = 0, max = problems.length; i < max; i++){
		if (problems[i].isError()){
			if (buffer == null) buffer = new StringBuilder(10);
			buffer.append(problems[i].getMessage());
			buffer.append('|');
		}
	}
	assertEquals("Unexpected errors",
		"Cannot use this in a static context|",
		buffer == null ? "none" : buffer.toString());
}
}
