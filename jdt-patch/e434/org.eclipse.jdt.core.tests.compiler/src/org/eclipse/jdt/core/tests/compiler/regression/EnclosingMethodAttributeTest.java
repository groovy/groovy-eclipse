/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

@SuppressWarnings({ "rawtypes" })
public class EnclosingMethodAttributeTest extends AbstractComparableTest {
	public EnclosingMethodAttributeTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test127" };
//		TESTS_NUMBERS = new int[] { 4 };
//		TESTS_RANGE = new int[] { 169, 180 };
	}

	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return EnclosingMethodAttributeTest.class;
	}

	public void test001() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"public static void main(String[] args) throws Exception  {\n" +
				"	class MyLocal$A {\n" +
				"		class Member {\n" +
				"		}\n" +
				"	};\n" +
				"	System.out.print(MyLocal$A.Member.class.getEnclosingMethod() != null);\n" +
				"	System.out.print(MyLocal$A.Member.class.getEnclosingConstructor() != null);\n" +
				"\n" +
				"	System.out.print(MyLocal$A.class.getEnclosingMethod()!= null);\n" +
				"	System.out.print(MyLocal$A.class.getEnclosingConstructor() != null);	\n" +
				"	\n" +
				"	System.out.print(X.class.getEnclosingMethod() != null);\n" +
				"	System.out.print(X.class.getEnclosingConstructor() != null);	\n" +
				"}\n" +
				"public Object foo() {\n" +
				"	return new Object() {};\n" +
				"}\n" +
				"}"
			},
			"falsefalsetruefalsefalsefalse");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X$1.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput = "  Enclosing Method: #22  #24 X.foo()Ljava/lang/Object;\n";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}

	public void test002() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"public static void main(String[] args) throws Exception  {\n" +
				"	class MyLocal$A {\n" +
				"		class Member {\n" +
				"			public Object foo() {\n" +
				"				return new Object() {};\n" +
				"			}\n" +
				"		}\n" +
				"	};\n" +
				"	System.out.print(MyLocal$A.Member.class.getEnclosingMethod() != null);\n" +
				"	System.out.print(MyLocal$A.Member.class.getEnclosingConstructor() != null);\n" +
				"\n" +
				"	System.out.print(MyLocal$A.class.getEnclosingMethod()!= null);\n" +
				"	System.out.print(MyLocal$A.class.getEnclosingConstructor() != null);	\n" +
				"	\n" +
				"	System.out.print(X.class.getEnclosingMethod() != null);\n" +
				"	System.out.print(X.class.getEnclosingConstructor() != null);	\n" +
				"}\n" +
				"}"
			},
			"falsefalsetruefalsefalsefalse");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  + "X$1MyLocal$A$Member$1.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput = "  Enclosing Method: #22  #24 X$1MyLocal$A$Member.foo()Ljava/lang/Object;\n";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=162356
	public void test003() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.reflect.*;\n" +
				"public class X {\n" +
				"        public void test() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {\n" +
				"                class LocalClass {\n" +
				"                        public void method() {\n" +
				"                        }\n" +
				"                };\n" +
				"                LocalClass localClass = new LocalClass();\n" +
				"                Class cc = localClass.getClass();\n" +
				"                System.out.println(\"enclosing class = \" + cc.getEnclosingClass());\n" +
				"                System.out.println(\"enclosing method = \" + cc.getEnclosingMethod());\n" +
				"        }\n" +
				"        public static void main(String args[]) {\n" +
				"                X t = new X();\n" +
				"                try {\n" +
				"                        t.test();\n" +
				"                } catch (Exception e) {\n" +
				"                        e.printStackTrace();\n" +
				"                }\n" +
				"        }\n" +
				"}"
			},
			"enclosing class = class X\n" +
			"enclosing method = public void X.test() throws java.lang.NoSuchMethodException,java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException");

		INameEnvironment nameEnvironment = getNameEnvironment(new String[]{}, null);
		nameEnvironment.findType(new char[][] {new char[0], "X$1LocalClass".toCharArray()});
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  + "X$1LocalClass.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  Inner classes:\n" +
			"    [inner class info: #1 X$1LocalClass, outer class info: #0\n" +
			"     inner name: #28 LocalClass, accessflags: 0 default]\n";

		// check inner classes info
		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}

		expectedOutput =
			"  Enclosing Method: #23  #25 X.test()V\n";

		// check enclosing method
		index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=288920
	public void test004() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.reflect.*;\n" +
				"interface I<E> {\n" +
				"	public String run();\n" +
				"}\n" +
				"public class X {\n" +
				"	public Object test(String s, int i) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {\n" +
				"		return (new I<String>() {" +
				"			public String run() {\n" +
				"				return \"SUCCESS\";\n" +
				"			}\n" +
				"		}).run();\n" +
				"	}\n" +
				"	public static void main(String args[]) {\n" +
				"		X t = new X();\n" +
				"		try {\n" +
				"			System.out.println(t.test(\"\", 0));\n" +
				"		} catch (Exception e) {\n" +
				"			e.printStackTrace();\n" +
				"		}\n" +
				"	}\n" +
				"}"
			},
			"SUCCESS");

		Requestor requestor =
					new Requestor(
						true,
						null,
						false, /* show category */
						false /* show warning token*/);
		requestor.outputPath = OUTPUT_DIR.endsWith(File.separator) ? OUTPUT_DIR : OUTPUT_DIR + File.separator;
				// WORK should not have to test a constant?

		Map<String, String> options = getCompilerOptions();
		CompilerOptions compilerOptions = new CompilerOptions(options);
		compilerOptions.performMethodsFullRecovery = true;
		compilerOptions.performStatementsRecovery = true;
		INameEnvironment nameEnvironment = getNameEnvironment(new String[]{}, null);
		Compiler batchCompiler =
			new Compiler(
				nameEnvironment,
				getErrorHandlingPolicy(),
				compilerOptions,
				requestor,
				getProblemFactory());
		ReferenceBinding binaryType = batchCompiler.lookupEnvironment.askForType(new char[][] {new char[0], "X$1".toCharArray()}, batchCompiler.lookupEnvironment.UnNamedModule);
		assertNotNull("Should not be null", binaryType);
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1905
	// ECJ writes incorrect enclosing method for doubly-nested anonymous class
	public void testGH1905() throws Exception {
		if (this.complianceLevel < ClassFileConstants.JDK10)
			return;

		this.runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {
					static Object o = new Object () {};
					public static void main(String[] args) {
						var outer = new Object() {
							Object inner = new Object() {
								//
							};
						};
						System.out.println(o.getClass().getEnclosingMethod());
						System.out.println(outer.getClass().getEnclosingMethod());
						System.out.println(outer.inner.getClass().getEnclosingMethod());
					}
				}
				"""
			},
			"null\n" +
			"public static void X.main(java.lang.String[])\n" +
			"null");
	}
}
