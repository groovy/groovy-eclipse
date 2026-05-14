/*******************************************************************************
 * Copyright (c) 2013, 2018 Jesper Steen Moeller and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jesper Steen Moeller - initial API and implementation
 *			bug 527554 - [18.3] Compiler support for JEP 286 Local-Variable Type
 *
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class MethodParametersAttributeTest extends AbstractRegressionTest {
	String versionString = null;
	public MethodParametersAttributeTest(String name) {
		super(name);
	}
	// No need for a tearDown()
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.versionString = AbstractCompilerTest.getVersionString(this.complianceLevel);
	}
	@SuppressWarnings("rawtypes")
	public static Class testClass() {
		return MethodParametersAttributeTest.class;
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_PREFIX = "test012";
//		TESTS_NAMES = new String[] { "testBug359495" };
//		TESTS_NUMBERS = new int[] { 53 };
//		TESTS_RANGE = new int[] { 23 -1,};
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}

	String originalSource =
		"import java.util.concurrent.Callable;\n" +
		"\n" +
		"public class ParameterNames {\n" +
		"	\n" +
		"	public void someMethod(int simple, final double complex) {\n" +
		"	}\n" +
		"	\n" +
		"	public Callable<String> makeInnerWithCapture(final String finalMessage, String mutableMessage) {\n" +
		"		return new Callable<String>()  {\n" +
		"			public String call() throws Exception {\n" +
		"				return finalMessage;\n" +
		"			}\n" +
		"		};\n" +
		"	}\n" +
		"\n" +
		"	public int localMath(final String finalMessage, String mutableMessage) {\n" +
		"		int capturedB = 42;\n" +
		"		\n" +
		"		class Local {\n" +
		"			int fieldA;\n" +
		"			Local(int a) {\n" +
		"				this.fieldA = a;\n" +
		"			}\n" +
		"			int calculate(final int parameterC) {\n" +
		"				return  this.fieldA + capturedB + parameterC;\n" +
		"			}\n" +
		"		}\n" +
		"		\n" +
		"		return new Local(2).calculate(3);\n" +
		"	}\n" +
		"\n" +
		"}\n" +
		"";

	public void test001() throws Exception {

			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "ParameterNames.class";
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(path));
			String actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);

			String expectedOutput =
					"// Compiled from ParameterNames.java (version 1.8 : 52.0, super bit)\n" +
					"public class ParameterNames {\n" +
					"  \n" +
					"  // Method descriptor #12 ()V\n" +
					"  // Stack: 1, Locals: 1\n" +
					"  public ParameterNames();\n" +
					"    0  aload_0 [this]\n" +
					"    1  invokespecial java.lang.Object() [1]\n" +
					"    4  return\n" +
					"      Line numbers:\n" +
					"        [pc: 0, line: 3]\n" +
					"  \n" +
					"  // Method descriptor #16 (ID)V\n" +
					"  // Stack: 0, Locals: 4\n" +
					"  public void someMethod(int simple, double complex);\n" +
					"    0  return\n" +
					"      Line numbers:\n" +
					"        [pc: 0, line: 6]\n" +
					"      Method Parameters:\n" +
					"        simple\n" +
					"        final complex\n" +
					"  \n" +
					"  // Method descriptor #21 (Ljava/lang/String;Ljava/lang/String;)Ljava/util/concurrent/Callable;\n" +
					"  // Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/util/concurrent/Callable<Ljava/lang/String;>;\n" +
					"  // Stack: 4, Locals: 3\n" +
					"  public java.util.concurrent.Callable makeInnerWithCapture(java.lang.String finalMessage, java.lang.String mutableMessage);\n" +
					"     0  new ParameterNames$1 [2]\n" +
					"     3  dup\n" +
					"     4  aload_0 [this]\n" +
					"     5  aload_1 [finalMessage]\n" +
					"     6  invokespecial ParameterNames$1(ParameterNames, java.lang.String) [3]\n" +
					"     9  areturn\n" +
					"      Line numbers:\n" +
					"        [pc: 0, line: 9]\n" +
					"      Method Parameters:\n" +
					"        final finalMessage\n" +
					"        mutableMessage\n" +
					"  \n" +
					"  // Method descriptor #27 (Ljava/lang/String;Ljava/lang/String;)I\n" +
					"  // Stack: 5, Locals: 4\n" +
					"  public int localMath(java.lang.String finalMessage, java.lang.String mutableMessage);\n" +
					"     0  bipush 42\n" +
					"     2  istore_3\n" +
					"     3  new ParameterNames$1Local [4]\n" +
					"     6  dup\n" +
					"     7  aload_0 [this]\n" +
					"     8  iconst_2\n" +
					"     9  iload_3\n" +
					"    10  invokespecial ParameterNames$1Local(ParameterNames, int, int) [5]\n" +
					"    13  iconst_3\n" +
					"    14  invokevirtual ParameterNames$1Local.calculate(int) : int [6]\n" +
					"    17  ireturn\n" +
					"      Line numbers:\n" +
					"        [pc: 0, line: 17]\n" +
					"        [pc: 3, line: 29]\n" +
					"      Method Parameters:\n" +
					"        final finalMessage\n" +
					"        mutableMessage\n" +
					"\n" +
					"  Inner classes:\n" +
					"    [inner class info: #4 ParameterNames$1Local, outer class info: #0\n" +
					"     inner name: #9 Local, accessflags: 0 default],\n" +
					"    [inner class info: #2 ParameterNames$1, outer class info: #0\n" +
					"     inner name: #0, accessflags: 0 default]\n" +
					"}";


			assertSubstring(actualOutput, expectedOutput);
	}
	public void test002() throws Exception {

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "ParameterNames$1.class";
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(path));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"// Compiled from ParameterNames.java (version 1.8 : 52.0, super bit)\n" +
			"// Signature: Ljava/lang/Object;Ljava/util/concurrent/Callable<Ljava/lang/String;>;\n" +
			"class ParameterNames$1 implements java.util.concurrent.Callable {\n" +
			"  \n" +
			"  // Field descriptor #9 Ljava/lang/String;\n" +
			"  final synthetic java.lang.String val$finalMessage;\n" +
			"  \n" +
			"  // Field descriptor #11 LParameterNames;\n" +
			"  final synthetic ParameterNames this$0;\n" +
			"  \n" +
			"  // Method descriptor #13 (LParameterNames;Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 3\n" +
			"  ParameterNames$1(ParameterNames this$0, java.lang.String val$finalMessage);\n" +
			"     0  aload_0 [this]\n" +
			"     1  aload_1 [this$0]\n" +
			"     2  putfield ParameterNames$1.this$0 : ParameterNames [1]\n" +
			"     5  aload_0 [this]\n" +
			"     6  aload_2 [val$finalMessage]\n" +
			"     7  putfield ParameterNames$1.val$finalMessage : java.lang.String [2]\n" +
			"    10  aload_0 [this]\n" +
			"    11  invokespecial java.lang.Object() [3]\n" +
			"    14  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 9]\n" +
			"      Method Parameters:\n" +
			"        final mandated this$0\n" +
			"        final synthetic val$finalMessage\n" +
			"  \n" +
			"  // Method descriptor #18 ()Ljava/lang/String;\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  public java.lang.String call() throws java.lang.Exception;\n" +
			"    0  aload_0 [this]\n" +
			"    1  getfield ParameterNames$1.val$finalMessage : java.lang.String [2]\n" +
			"    4  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 11]\n" +
			"  \n" +
			"  // Method descriptor #21 ()Ljava/lang/Object;\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  public bridge synthetic java.lang.Object call() throws java.lang.Exception;\n" +
			"    0  aload_0 [this]\n" +
			"    1  invokevirtual ParameterNames$1.call() : java.lang.String [4]\n" +
			"    4  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 9]\n" +
			"\n" +
			"  Inner classes:\n" +
			"    [inner class info: #5 ParameterNames$1, outer class info: #0\n" +
			"     inner name: #0, accessflags: 0 default]\n" +
			"  Enclosing Method: #27  #28 ParameterNames.makeInnerWithCapture(Ljava/lang/String;Ljava/lang/String;)Ljava/util/concurrent/Callable;\n" +
			"}";

		assertSubstring(actualOutput, expectedOutput);
	}

	public void test003() throws Exception {

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "ParameterNames$1Local.class";
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(path));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"// Compiled from ParameterNames.java (version 1.8 : 52.0, super bit)\n" +
			"class ParameterNames$1Local {\n" +
			"  \n" +
			"  // Field descriptor #8 I\n" +
			"  int fieldA;\n" +
			"  \n" +
			"  // Field descriptor #8 I\n" +
			"  final synthetic int val$capturedB;\n" +
			"  \n" +
			"  // Field descriptor #11 LParameterNames;\n" +
			"  final synthetic ParameterNames this$0;\n" +
			"  \n" +
			"  // Method descriptor #13 (LParameterNames;II)V\n" +
			"  // Signature: (I)V\n" +
			"  // Stack: 2, Locals: 4\n" +
			"  ParameterNames$1Local(ParameterNames this$0, int val$capturedB, int a);\n" +
			"     0  aload_0 [this]\n" +
			"     1  aload_1 [this$0]\n" +
			"     2  putfield ParameterNames$1Local.this$0 : ParameterNames [1]\n" +
			"     5  aload_0 [this]\n" +
			"     6  iload_3 [a]\n" +
			"     7  putfield ParameterNames$1Local.val$capturedB : int [2]\n" +
			"    10  aload_0 [this]\n" +
			"    11  invokespecial java.lang.Object() [3]\n" +
			"    14  aload_0 [this]\n" +
			"    15  iload_2 [val$capturedB]\n" +
			"    16  putfield ParameterNames$1Local.fieldA : int [4]\n" +
			"    19  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 21]\n" +
			"        [pc: 14, line: 22]\n" +
			"        [pc: 19, line: 23]\n" +
			"      Method Parameters:\n" +
			"        final mandated this$0\n" +
			"        final synthetic val$capturedB\n" +
			"        a\n" +
			"  \n" +
			"  // Method descriptor #21 (I)I\n" +
			"  // Stack: 2, Locals: 2\n" +
			"  int calculate(int parameterC);\n" +
			"     0  aload_0 [this]\n" +
			"     1  getfield ParameterNames$1Local.fieldA : int [4]\n" +
			"     4  aload_0 [this]\n" +
			"     5  getfield ParameterNames$1Local.val$capturedB : int [2]\n" +
			"     8  iadd\n" +
			"     9  iload_1 [parameterC]\n" +
			"    10  iadd\n" +
			"    11  ireturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 25]\n" +
			"      Method Parameters:\n" +
			"        final parameterC\n" +
			"\n" +
			"  Inner classes:\n" +
			"    [inner class info: #5 ParameterNames$1Local, outer class info: #0\n" +
			"     inner name: #33 Local, accessflags: 0 default]\n" +
			"  Enclosing Method: #26  #27 ParameterNames.localMath(Ljava/lang/String;Ljava/lang/String;)I\n" +
			"}";

		assertSubstring(actualOutput, expectedOutput);
	}

	public void test004() throws Exception {

		// Test the results of the ClassFileReader
		String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "ParameterNames.class";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = ClassFileReader.read(path);
		IBinaryMethod[] methodInfos = classFileReader.getMethods();
		assertNotNull("No method infos", methodInfos);
		int length = methodInfos.length;
		assertEquals("Must have four methods", 4, length);
		assertEquals("finalMessage", new String(methodInfos[2].getArgumentNames()[0]));
		assertEquals("mutableMessage", new String(methodInfos[2].getArgumentNames()[1]));
	}

	public void test005() throws Exception {
		// Test the results of the ClassFileReader where some of the paramers are synthetic and/or mandated
		String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "ParameterNames$1Local.class";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = ClassFileReader.read(path);
		IBinaryMethod[] methodInfos = classFileReader.getMethods();
		assertNotNull("No method infos", methodInfos);
		int length = methodInfos.length;
		assertEquals("Must have two methods", 2, length);
		assertEquals("this$0", new String(methodInfos[0].getArgumentNames()[0]));
		assertEquals("val$capturedB", new String(methodInfos[0].getArgumentNames()[1]));
	}
	public void test006() throws Exception {
		// Test that the code generator can emit the names, so the ClassFileReader may read them back

		this.runParameterNameTest(
			"X.java",
			"public class X {\n" +
			"    X(int wholeNumber) {\n" +
			"    }\n" +
			"    void foo(final float pluggedTheHoles, boolean yesItFloats) {\n" +
			"    }\n" +
			"}");

		try {
			ClassFileReader classFileReader = ClassFileReader.read(OUTPUT_DIR + File.separator + "X.class");
			IBinaryMethod[] methods = classFileReader.getMethods();
			assertNotNull("No methods", methods);
			assertEquals("Wrong size", 2, methods.length);
			assertEquals("Wrong name", "<init>", new String(methods[0].getSelector()));
			char[][] argumentNames = methods[0].getArgumentNames();
			assertEquals("<init> should have 1 parameter", 1, argumentNames.length);
			assertEquals("wholeNumber", new String(argumentNames[0]));
			assertEquals("Wrong name", "foo", new String(methods[1].getSelector()));
			assertEquals("pluggedTheHoles", new String(methods[1].getArgumentNames()[0]));
			assertEquals("yesItFloats", new String(methods[1].getArgumentNames()[1]));
		} catch (ClassFormatException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}
	}

	public void test007() throws Exception {
		// Test that the code generator can emit the names, so the disassembler may read them back (same source as was compiled with javac)

		this.runParameterNameTest(
			"ParameterNames.java",
			this.originalSource);

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String path = OUTPUT_DIR + File.separator + "ParameterNames.class";
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(path));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String nestMembers = "";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		if (options.complianceLevel >= ClassFileConstants.JDK11) {
			nestMembers = "\n" +
					"Nest Members:\n" +
					"   #20 ParameterNames$1,\n" +
					"   #29 ParameterNames$1Local\n";
		}
		String expectedOutput =
				"// Compiled from ParameterNames.java (" + this.versionString + ", super bit)\n" +
				"public class ParameterNames {\n" +
				"  \n" +
				"  // Method descriptor #6 ()V\n" +
				"  // Stack: 1, Locals: 1\n" +
				"  public ParameterNames();\n" +
				"    0  aload_0 [this]\n" +
				"    1  invokespecial java.lang.Object() [8]\n" +
				"    4  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 3]\n" +
				"  \n" +
				"  // Method descriptor #12 (ID)V\n" +
				"  // Stack: 0, Locals: 4\n" +
				"  public void someMethod(int simple, double complex);\n" +
				"    0  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 6]\n" +
				"      Method Parameters:\n" +
				"        simple\n" +
				"        final complex\n" +
				"  \n" +
				"  // Method descriptor #17 (Ljava/lang/String;Ljava/lang/String;)Ljava/util/concurrent/Callable;\n" +
				"  // Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/util/concurrent/Callable<Ljava/lang/String;>;\n" +
				"  // Stack: 4, Locals: 3\n" +
				"  public java.util.concurrent.Callable makeInnerWithCapture(java.lang.String finalMessage, java.lang.String mutableMessage);\n" +
				"     0  new ParameterNames$1 [20]\n" +
				"     3  dup\n" +
				"     4  aload_0 [this]\n" +
				"     5  aload_1 [finalMessage]\n" +
				"     6  invokespecial ParameterNames$1(ParameterNames, java.lang.String) [22]\n" +
				"     9  areturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 9]\n" +
				"      Method Parameters:\n" +
				"        final finalMessage\n" +
				"        mutableMessage\n" +
				"  \n" +
				"  // Method descriptor #28 (Ljava/lang/String;Ljava/lang/String;)I\n" +
				"  // Stack: 5, Locals: 4\n" +
				"  public int localMath(java.lang.String finalMessage, java.lang.String mutableMessage);\n" +
				"     0  bipush 42\n" +
				"     2  istore_3\n" +
				"     3  new ParameterNames$1Local [29]\n" +
				"     6  dup\n" +
				"     7  aload_0 [this]\n" +
				"     8  iconst_2\n" +
				"     9  iload_3\n" +
				"    10  invokespecial ParameterNames$1Local(ParameterNames, int, int) [31]\n" +
				"    13  iconst_3\n" +
				"    14  invokevirtual ParameterNames$1Local.calculate(int) : int [34]\n" +
				"    17  ireturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 17]\n" +
				"        [pc: 3, line: 29]\n" +
				"      Method Parameters:\n" +
				"        final finalMessage\n" +
				"        mutableMessage\n" +
				"\n" +
				"  Inner classes:\n" +
				"    [inner class info: #20 ParameterNames$1, outer class info: #0\n" +
				"     inner name: #0, accessflags: 0 default],\n" +
				"    [inner class info: #29 ParameterNames$1Local, outer class info: #0\n" +
				"     inner name: #41 Local, accessflags: 0 default]\n" +
				nestMembers +
				"}";

		assertSubstring(actualOutput, expectedOutput);
	}

	public void test008() throws Exception {
		// Test that the code generator can emit synthetic and mandated names, just to match javac as closely as possibly

		this.runParameterNameTest(
			"ParameterNames.java",
			this.originalSource);

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String path = OUTPUT_DIR + File.separator + "ParameterNames$1.class";
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(path));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String nestHost = "";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		if (options.complianceLevel >= ClassFileConstants.JDK11) {
			nestHost = "\n" +
		               "Nest Host: #36 ParameterNames\n";
		}
		String expectedOutput =
			"// Compiled from ParameterNames.java (" + this.versionString + ", super bit)\n" +
			"// Signature: Ljava/lang/Object;Ljava/util/concurrent/Callable<Ljava/lang/String;>;\n" +
			"class ParameterNames$1 implements java.util.concurrent.Callable {\n" +
			"  \n" +
			"  // Field descriptor #8 LParameterNames;\n" +
			"  final synthetic ParameterNames this$0;\n" +
			"  \n" +
			"  // Field descriptor #10 Ljava/lang/String;\n" +
			"  private final synthetic java.lang.String val$finalMessage;\n" +
			"  \n" +
			"  // Method descriptor #12 (LParameterNames;Ljava/lang/String;)V\n" +
			"  // Stack: 2, Locals: 3\n" +
			"  ParameterNames$1(ParameterNames this$0, java.lang.String val$finalMessage);\n" +
			"     0  aload_0 [this]\n" +
			"     1  aload_1 [this$0]\n" +
			"     2  putfield ParameterNames$1.this$0 : ParameterNames [14]\n" +
			"     5  aload_0 [this]\n" +
			"     6  aload_2 [val$finalMessage]\n" +
			"     7  putfield ParameterNames$1.val$finalMessage : java.lang.String [16]\n" +
			"    10  aload_0 [this]\n" +
			"    11  invokespecial java.lang.Object() [18]\n" +
			"    14  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 9]\n" +
			"      Method Parameters:\n" +
			"        final mandated this$0\n" +
			"        final synthetic val$finalMessage\n" +
			"  \n" +
			"  // Method descriptor #24 ()Ljava/lang/String;\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  public java.lang.String call() throws java.lang.Exception;\n" +
			"    0  aload_0 [this]\n" +
			"    1  getfield ParameterNames$1.val$finalMessage : java.lang.String [16]\n" +
			"    4  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 11]\n" +
			"  \n" +
			"  // Method descriptor #28 ()Ljava/lang/Object;\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  public bridge synthetic java.lang.Object call() throws java.lang.Exception;\n" +
			"    0  aload_0 [this]\n" +
			"    1  invokevirtual ParameterNames$1.call() : java.lang.String [29]\n" +
			"    4  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"\n" +
			"  Inner classes:\n" +
			"    [inner class info: #1 ParameterNames$1, outer class info: #0\n" +
			"     inner name: #0, accessflags: 0 default]\n" +
			"  Enclosing Method: #36  #38 ParameterNames.makeInnerWithCapture(Ljava/lang/String;Ljava/lang/String;)Ljava/util/concurrent/Callable;\n" +
			nestHost +
			"}"	;

		assertSubstring(actualOutput, expectedOutput);
	}

	public void test009() throws Exception {
		// Test that the code generator can emit synthetic and mandated names, just to match javac as closely as possibly

		this.runParameterNameTest(
			"FancyEnum.java",
			"\n" +
			"public enum FancyEnum {\n" +
			"	ONE(1), TWO(2);\n" +
			"	\n" +
			"	private FancyEnum(final int v) { this.var = v; }\n" +
			"	int var;\n" +
			"}\n" +
			"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String path = OUTPUT_DIR + File.separator + "FancyEnum.class";
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(path));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"// Compiled from FancyEnum.java (" + this.versionString + ", super bit)\n" +
			"// Signature: Ljava/lang/Enum<LFancyEnum;>;\n" +
			"public final enum FancyEnum {\n" +
			"  \n" +
			"  // Field descriptor #6 LFancyEnum;\n" +
			"  public static final enum FancyEnum ONE;\n" +
			"  \n" +
			"  // Field descriptor #6 LFancyEnum;\n" +
			"  public static final enum FancyEnum TWO;\n" +
			"  \n" +
			"  // Field descriptor #9 I\n" +
			"  int var;\n" +
			"  \n" +
			"  // Field descriptor #11 [LFancyEnum;\n" +
			"  private static final synthetic FancyEnum[] ENUM$VALUES;\n" +
			"  \n" +
			"  // Method descriptor #13 ()V\n" +
			"  // Stack: 5, Locals: 0\n" +
			"  static {};\n" +
			"     0  new FancyEnum [1]\n" +
			"     3  dup\n" +
			"     4  ldc <String \"ONE\"> [15]\n" +
			"     6  iconst_0\n" +
			"     7  iconst_1\n" +
			"     8  invokespecial FancyEnum(java.lang.String, int, int) [16]\n" +
			"    11  putstatic FancyEnum.ONE : FancyEnum [20]\n" +
			"    14  new FancyEnum [1]\n" +
			"    17  dup\n" +
			"    18  ldc <String \"TWO\"> [22]\n" +
			"    20  iconst_1\n" +
			"    21  iconst_2\n" +
			"    22  invokespecial FancyEnum(java.lang.String, int, int) [16]\n" +
			"    25  putstatic FancyEnum.TWO : FancyEnum [23]\n" +
			"    28  iconst_2\n" +
			"    29  anewarray FancyEnum [1]\n" +
			"    32  dup\n" +
			"    33  iconst_0\n" +
			"    34  getstatic FancyEnum.ONE : FancyEnum [20]\n" +
			"    37  aastore\n" +
			"    38  dup\n" +
			"    39  iconst_1\n" +
			"    40  getstatic FancyEnum.TWO : FancyEnum [23]\n" +
			"    43  aastore\n" +
			"    44  putstatic FancyEnum.ENUM$VALUES : FancyEnum[] [25]\n" +
			"    47  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 3]\n" +
			"        [pc: 28, line: 2]\n" +
			"  \n" +
			"  // Method descriptor #19 (Ljava/lang/String;II)V\n" +
			"  // Stack: 3, Locals: 4\n" +
			"  private FancyEnum(java.lang.String $enum$name, int $enum$ordinal, int v);\n" +
			"     0  aload_0 [this]\n" +
			"     1  aload_1 [$enum$name]\n" +
			"     2  iload_2 [$enum$ordinal]\n" +
			"     3  invokespecial java.lang.Enum(java.lang.String, int) [28]\n" +
			"     6  aload_0 [this]\n" +
			"     7  iload_3 [v]\n" +
			"     8  putfield FancyEnum.var : int [31]\n" +
			"    11  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 5]\n" +
			"      Method Parameters:\n" +
			"        synthetic $enum$name\n" +
			"        synthetic $enum$ordinal\n" +
			"        final v\n" +
			"  \n" +
			"  // Method descriptor #38 ()[LFancyEnum;\n" +
			"  // Stack: 5, Locals: 3\n" +
			"  public static FancyEnum[] values();\n" +
			"     0  getstatic FancyEnum.ENUM$VALUES : FancyEnum[] [25]\n" +
			"     3  dup\n" +
			"     4  astore_0\n" +
			"     5  iconst_0\n" +
			"     6  aload_0\n" +
			"     7  arraylength\n" +
			"     8  dup\n" +
			"     9  istore_1\n" +
			"    10  anewarray FancyEnum [1]\n" +
			"    13  dup\n" +
			"    14  astore_2\n" +
			"    15  iconst_0\n" +
			"    16  iload_1\n" +
			"    17  invokestatic java.lang.System.arraycopy(java.lang.Object, int, java.lang.Object, int, int) : void [39]\n" +
			"    20  aload_2\n" +
			"    21  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"  \n" +
			"  // Method descriptor #46 (Ljava/lang/String;)LFancyEnum;\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  public static FancyEnum valueOf(java.lang.String name);\n" +
			"     0  ldc <Class FancyEnum> [1]\n" +
			"     2  aload_0 [name]\n" +
			"     3  invokestatic java.lang.Enum.valueOf(java.lang.Class, java.lang.String) : java.lang.Enum [47]\n" +
			"     6  checkcast FancyEnum [1]\n" +
			"     9  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 1]\n" +
			"      Method Parameters:\n" +
			"        mandated name\n" +
			"}";

		assertSubstring(actualOutput, expectedOutput);
	}

	public void test010() throws Exception {
		// Test that the non private inner class gets a mandated enclosing instance parameter.

		this.runParameterNameTest(
			"X.java",
			"public class X {\n" +
			"    class Y {}\n" +
			"}\n"
		);

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String path = OUTPUT_DIR + File.separator + "X$Y.class";
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(path));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
				"  X$Y(X this$0);\n" +
				"     0  aload_0 [this]\n" +
				"     1  aload_1 [this$0]\n" +
				"     2  putfield X$Y.this$0 : X [10]\n" +
				"     5  aload_0 [this]\n" +
				"     6  invokespecial java.lang.Object() [12]\n" +
				"     9  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 2]\n" +
				"      Method Parameters:\n" +
				"        final mandated this$0\n" +
				"\n";

		assertSubstring(actualOutput, expectedOutput);
	}

	public void test011() throws Exception {
		// Test that a private inner class does not get a mandated enclosing instance parameter.

		this.runParameterNameTest(
			"X.java",
			"public class X {\n" +
			"    private class Y {}\n" +
			"}\n"
		);

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String path = OUTPUT_DIR + File.separator + "X$Y.class";
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(path));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
				"  private X$Y(X this$0);\n" +
				"     0  aload_0 [this]\n" +
				"     1  aload_1 [this$0]\n" +
				"     2  putfield X$Y.this$0 : X [10]\n" +
				"     5  aload_0 [this]\n" +
				"     6  invokespecial java.lang.Object() [12]\n" +
				"     9  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 2]\n" +
				"      Method Parameters:\n" +
				"        final synthetic this$0\n" +
				"\n";

		assertSubstring(actualOutput, expectedOutput);
	}

	public void test012() throws Exception {

		this.runParameterNameTest(
			"X.java",
			"public class X {\n" +
			"    void foo() {\n" +
			"        new Y().new Z() {\n" +
			"        };\n" +
			"    }\n" +
			"}\n" +
			"class Y {\n" +
			"    class Z {}\n" +
			"}\n"
		);

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String path = OUTPUT_DIR + File.separator + "X$1.class";
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(path));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
				"  X$1(X this$0, Y this$1);\n" +
				"     0  aload_0 [this]\n" +
				"     1  aload_1 [this$0]\n" +
				"     2  putfield X$1.this$0 : X [10]\n" +
				"     5  aload_0 [this]\n" +
				"     6  aload_2 [this$1]\n" +
				"     7  invokespecial Y$Z(Y) [12]\n" +
				"    10  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 3]\n" +
				"      Method Parameters:\n" +
				"        final synthetic this$0\n" +
				"        final mandated this$1\n" +
				"\n";

		assertSubstring(actualOutput, expectedOutput);
	}

	public void test013() throws Exception {
		// Test that synthesized enum constructor arguments show up as synthetic

		this.runParameterNameTest(
			"FancyEnum.java",
			"\n" +
			"public enum FancyEnum {\n" +
			"	ONE, TWO;\n" +
			"}\n" +
			"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String path = OUTPUT_DIR + File.separator + "FancyEnum.class";
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(path));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
				"  private FancyEnum(java.lang.String $enum$name, int $enum$ordinal);\n" +
				"    0  aload_0 [this]\n" +
				"    1  aload_1 [$enum$name]\n" +
				"    2  iload_2 [$enum$ordinal]\n" +
				"    3  invokespecial java.lang.Enum(java.lang.String, int) [26]\n" +
				"    6  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 2]\n" +
				"      Method Parameters:\n" +
				"        synthetic $enum$name\n" +
				"        synthetic $enum$ordinal\n" +
				"  \n";

		assertSubstring(actualOutput, expectedOutput);
	}

	public void test014() throws Exception {
		// Test that the name argument of enum valueOf shows up as mandated

		this.runParameterNameTest(
			"FancyEnum.java",
			"\n" +
			"public enum FancyEnum {\n" +
			"	ONE, TWO;\n" +
			"}\n" +
			"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String path = OUTPUT_DIR + File.separator + "FancyEnum.class";
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(path));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
				"  public static FancyEnum valueOf(java.lang.String name);\n" +
						"     0  ldc <Class FancyEnum> [1]\n" +
						"     2  aload_0 [name]\n" +
						"     3  invokestatic java.lang.Enum.valueOf(java.lang.Class, java.lang.String) : java.lang.Enum [40]\n" +
						"     6  checkcast FancyEnum [1]\n" +
						"     9  areturn\n" +
						"      Line numbers:\n" +
						"        [pc: 0, line: 1]\n" +
						"      Method Parameters:\n" +
						"        mandated name\n";
		assertSubstring(actualOutput, expectedOutput);
	}

	public void test015() throws Exception {
		// Test that the name argument of enum valueOf shows up as mandated

		this.runParameterNameTest(
			"InnerLocalClassTest.java",
			"public class InnerLocalClassTest {\n" +
			"   void test() {\n" +
			"     class Local { }\n" +
			"     new Local() { };\n" +
			"   } \n" +
			"}\n");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String path = OUTPUT_DIR + File.separator + "InnerLocalClassTest$1.class";
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(path));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  InnerLocalClassTest$1(InnerLocalClassTest this$0, InnerLocalClassTest this$1);\n" +
			"     0  aload_0 [this]\n" +
			"     1  aload_2 [this$1]\n" +
			"     2  putfield InnerLocalClassTest$1.this$0 : InnerLocalClassTest [10]\n" +
			"     5  aload_0 [this]\n" +
			"     6  aload_1 [this$0]\n" +
			"     7  invokespecial InnerLocalClassTest$1Local(InnerLocalClassTest) [12]\n" +
			"    10  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 4]\n" +
			"      Method Parameters:\n" +
			"        final synthetic this$0\n" +
			"        final synthetic this$1\n";
		assertSubstring(actualOutput, expectedOutput);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=476528
	public void test016() throws Exception {
		// Test that the name argument of enum valueOf shows up as mandated

		this.runParameterNameTest(
			"Foo.java",
			"public enum Foo {\n" +
			"	BAR;\n" +
			"   public static Foo valueOf(int intParameter) {\n" +
			"		return BAR;\n" +
			"   }\n" +
			"   public static Foo valueOf2(int intParameter) {\n" +
			"		return BAR;\n" +
			"   } \n" +
			"}\n");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String path = OUTPUT_DIR + File.separator + "Foo.class";
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(path));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  // Stack: 1, Locals: 1\n" +
			"  public static Foo valueOf(int intParameter);\n" +
			"    0  getstatic Foo.BAR : Foo [17]\n" +
			"    3  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 4]\n" +
			"      Method Parameters:\n" +
			"        intParameter\n" +
			"  \n" +
			"  // Method descriptor #27 (I)LFoo;\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  public static Foo valueOf2(int intParameter);\n" +
			"    0  getstatic Foo.BAR : Foo [17]\n" +
			"    3  areturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 7]\n" +
			"      Method Parameters:\n" +
			"        intParameter\n";
		assertSubstring(actualOutput, expectedOutput);
		// Test that synthetic method gets the right parameter names
		expectedOutput =
				"  public static Foo valueOf(java.lang.String name);\n" +
				"     0  ldc <Class Foo> [1]\n" +
				"     2  aload_0 [name]\n" +
				"     3  invokestatic java.lang.Enum.valueOf(java.lang.Class, java.lang.String) : java.lang.Enum [39]\n" +
				"     6  checkcast Foo [1]\n" +
				"     9  areturn\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 1]\n" +
				"      Method Parameters:\n" +
				"        mandated name\n";
			assertSubstring(actualOutput, expectedOutput);
	}

	private void runParameterNameTest(String fileName, String body) {
		Map<String, String> compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_LocalVariableAttribute, CompilerOptions.DO_NOT_GENERATE);
		compilerOptions.put(CompilerOptions.OPTION_MethodParametersAttribute, CompilerOptions.GENERATE);
		this.runConformTest(
			new String[] {
				fileName,
				body
			},
			compilerOptions /* custom options */
		);
	}

	private void assertSubstring(String actualOutput, String expectedOutput) {
		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}
}
