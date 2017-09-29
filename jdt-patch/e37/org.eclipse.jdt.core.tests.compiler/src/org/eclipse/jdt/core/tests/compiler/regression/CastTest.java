/*******************************************************************************
 * Copyright (c) 2003, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class CastTest extends AbstractRegressionTest {

public CastTest(String name) {
	super(name);
}
protected Map getCompilerOptions() {
	Map defaultOptions = super.getCompilerOptions();
	defaultOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.WARNING);
	defaultOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	return defaultOptions;
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

/*
 * check extra checkcast (interface->same interface)
 */
public void test001() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n" +
			"    public static void main(String[] args) {	\n" +
			"       Cloneable c1 = new int[0]; \n"+
			"		Cloneable c2 = (Cloneable)c1; \n" +
			"		System.out.print(\"SUCCESS\");	\n" +
			"    }	\n" +
			"}	\n",
		},
		"SUCCESS");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 3\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  iconst_0\n" +
		"     1  newarray int [10]\n" +
		"     3  astore_1 [c1]\n" +
		"     4  aload_1 [c1]\n" +
		"     5  astore_2 [c2]\n" +
		"     6  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"     9  ldc <String \"SUCCESS\"> [22]\n" +
		"    11  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]\n" +
		"    14  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 3]\n" +
		"        [pc: 4, line: 4]\n" +
		"        [pc: 6, line: 5]\n" +
		"        [pc: 14, line: 6]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 4, pc: 15] local: c1 index: 1 type: java.lang.Cloneable\n" +
		"        [pc: 6, pc: 15] local: c2 index: 2 type: java.lang.Cloneable\n";
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 2));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}

public void test002() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		// standard expressions\n" +
			"		String s = (String) null;	// UNnecessary\n" +
			"		String t = (String) \"hello\";	// UNnecessary\n" +
			"		float f = (float) 12;			// UNnecessary\n" +
			"		int i = (int)12.0;				//   necessary\n" +
			"	}\n" +
			"}\n"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 4)\n" +
		"	String s = (String) null;	// UNnecessary\n" +
		"	           ^^^^^^^^^^^^^\n" +
		"Unnecessary cast from null to String\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	String t = (String) \"hello\";	// UNnecessary\n" +
		"	           ^^^^^^^^^^^^^^^^\n" +
		"Unnecessary cast from String to String\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 6)\n" +
		"	float f = (float) 12;			// UNnecessary\n" +
		"	          ^^^^^^^^^^\n" +
		"Unnecessary cast from int to float\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void test003() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		// message sends		\n" +
			"		foo((Object) \"hello\");		//   necessary\n" +
			"		foo((String) \"hello\");			// UNnecessary\n" +
			"		foo((Object) null);			//   necessary\n" +
			"		foo((String) null);				// UNnecessary but keep as useful documentation \n" +
			"	}\n" +
			"	static void foo(String s) {\n" +
			"		System.out.println(\"foo(String):\"+s);\n" +
			"	}\n" +
			"	static void foo(Object o) {\n" +
			"		System.out.println(\"foo(Object):\"+o);\n" +
			"	}\n" +
			"}\n"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 5)\n" +
		"	foo((String) \"hello\");			// UNnecessary\n" +
		"	    ^^^^^^^^^^^^^^^^\n" +
		"Unnecessary cast from String to String\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void test004() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		// constructors\n" +
			"		new X((Object) \"hello\");	//   necessary\n" +
			"		new X((String) \"hello\");	// UNnecessary\n" +
			"		new X((Object) null);		//   necessary\n" +
			"		new X((String) null);		// UNnecessary but keep as useful documentation\n" +
			"	}\n" +
			"	X(){}\n" +
			"	X(String s){\n" +
			"		System.out.println(\"new X(String):\"+s);\n" +
			"	}\n" +
			"	X(Object o){\n" +
			"		System.out.println(\"new X(Object):\"+o);\n" +
			"	}\n" +
			"}\n"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 5)\n" +
		"	new X((String) \"hello\");	// UNnecessary\n" +
		"	      ^^^^^^^^^^^^^^^^\n" +
		"Unnecessary cast from String to String\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void test005() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		// qualified allocations\n" +
			"		new X().new XM3((Object) \"hello\");	//   necessary\n" +
			"		new X().new XM3((String) \"hello\");	// UNnecessary\n" +
			"		new X().new XM3((Object) null);		//   necessary\n" +
			"		new X().new XM3((String) null);		// UNnecessary but keep as useful documentation\n" +
			"		new X().new XM3((Object) \"hello\"){};	//   necessary\n" +
			"		new X().new XM3((String) \"hello\"){};	// UNnecessary\n" +
			"		new X().new XM3((Object) null){};		//   necessary\n" +
			"		new X().new XM3((String) null){};		// UNnecessary but keep as useful documentation\n" +
			"	}\n" +
			"	X(){}\n" +
			"	static class XM1 extends X {}\n" +
			"	static class XM2 extends X {}\n" +
			"	class XM3 {\n" +
			"		XM3(String s){\n" +
			"			System.out.println(\"new XM3(String):\"+s);\n" +
			"		}\n" +
			"		XM3(Object o){\n" +
			"			System.out.println(\"new XM3(Object):\"+o);\n" +
			"		}\n" +
			"	}	\n" +
			"}\n"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
			"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 5)\n" +
		"	new X().new XM3((String) \"hello\");	// UNnecessary\n" +
		"	                ^^^^^^^^^^^^^^^^\n" +
		"Unnecessary cast from String to String\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	new X().new XM3((String) \"hello\"){};	// UNnecessary\n" +
		"	                ^^^^^^^^^^^^^^^^\n" +
		"Unnecessary cast from String to String\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void _test006() { // TODO (philippe) add support to conditional expression for unnecessary cast
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		// ternary operator\n" +
			"		String s = null, t = null;	\n" +
			"		X x0 = s == t\n" +
			"			? (X)new XM1()			// UNnecessary\n" +
			"			: new X();\n" +
			"		X x1 = s == t \n" +
			"			? (X)new XM1()			//   necessary\n" +
			"			: new XM2();\n" +
			"		X x2 = s == t \n" +
			"			? new XM1()\n" +
			"			: (X)new XM2();			//   necessary\n" +
			"		X x3 = s == t \n" +
			"			? (X)new XM1()			//   necessary\n" +
			"			: (X)new XM2();			//   necessary\n" +
			"	}\n" +
			"	X(){}\n" +
			"	static class XM1 extends X {}\n" +
			"	static class XM2 extends X {}\n" +
			"}\n"
		},
		"x",
		null,
		true,
		customOptions);
}

public void test007() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"	X(){}\n" +
			"	class XM3 {\n" +
			"		XM3(String s){\n" +
			"			System.out.println(\"new XM3(String):\"+s);\n" +
			"		}\n" +
			"		XM3(Object o){\n" +
			"			System.out.println(\"new XM3(Object):\"+o);\n" +
			"		}\n" +
			"	}	\n" +
			"	\n" +
			"	class XM4 extends XM3 {\n" +
			"		XM4(String s){\n" +
			"			super((Object) s); // necessary\n" +
			"			System.out.println(\"new XM4(String):\"+s);\n" +
			"		}\n" +
			"		XM4(Object o){\n" +
			"			super((String) o); // necessary\n" +
			"			System.out.println(\"new XM4(Object):\"+o);\n" +
			"		}\n" +
			"		XM4(Thread t){\n" +
			"			super((Object) t); // UNnecessary\n" +
			"			System.out.println(\"new XM4(Thread):\"+t);\n" +
			"		}\n" +
			"		XM4(){\n" +
			"			super((String)null); // UNnecessary but keep as useful documentation\n" +
			"			System.out.println(\"new XM4():\");\n" +
			"		}\n" +
			"		XM4(int i){\n" +
			"			super((Object)null); // necessary\n" +
			"			System.out.println(\"new XM4():\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 22)\n" +
		"	super((Object) t); // UNnecessary\n" +
		"	      ^^^^^^^^^^\n" +
		"Unnecessary cast from Thread to Object\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void test008() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b1 = new XM1() instanceof X; // UNnecessary\n" +
			"		boolean b2 = new X() instanceof XM1; // necessary\n" +
			"		boolean b3 = null instanceof X;\n" +
			"	}\n" +
			"	static class XM1 extends X {}\n" +
			"}\n"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 3)\n" +
		"	boolean b1 = new XM1() instanceof X; // UNnecessary\n" +
		"	             ^^^^^^^^^^^^^^^^^^^^^^\n" +
		"The expression of type X.XM1 is already an instance of type X\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void test009() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean b1 = ((X) new XM1()) == new X(); // UNnecessary\n" +
			"		boolean b2 = ((X) new XM1()) == new XM2(); // necessary\n" +
			"		boolean b3 = ((X) null) == new X(); // UNnecessary\n" +
			"	}\n" +
			"	static class XM1 extends X {}\n" +
			"	static class XM2 extends X {}\n" +
			"}\n"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */		"1. ERROR in X.java (at line 3)\n" +
		"	boolean b1 = ((X) new XM1()) == new X(); // UNnecessary\n" +
		"	             ^^^^^^^^^^^^^^^\n" +
		"Unnecessary cast from X.XM1 to X\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	boolean b3 = ((X) null) == new X(); // UNnecessary\n" +
		"	             ^^^^^^^^^^\n" +
		"Unnecessary cast from null to X\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void test010() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		long l1 = ((long) 1) + 2L; // UNnecessary\n" +
			"		long l2 = ((long)1) + 2; // necessary\n" +
			"		long l3 = 0;" +
			"		l3 += (long)12; // UNnecessary\n" +
			"	}\n" +
			"}\n"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 3)\n" +
		"	long l1 = ((long) 1) + 2L; // UNnecessary\n" +
		"	          ^^^^^^^^^^\n" +
		"Unnecessary cast from int to long\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	long l3 = 0;		l3 += (long)12; // UNnecessary\n" +
		"	            		      ^^^^^^^^\n" +
		"Unnecessary cast from int to long\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void test011() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		String s1 = ((long) 1) + \"hello\"; // necessary\n" +
			"		String s2 = ((String)\"hello\") + 2; // UNnecessary\n" +
			"		String s3 = ((String)null) + null; // necessary\n" +
			"		String s4 = ((int) (byte)1) + \"hello\"; // necessary\n" +
			"	}\n" +
			"}\n"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 4)\n" +
		"	String s2 = ((String)\"hello\") + 2; // UNnecessary\n" +
		"	            ^^^^^^^^^^^^^^^^^\n" +
		"Unnecessary cast from String to String\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void test012() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		// message sends		\n" +
			"		X x = new YM1();	\n" +
			"		foo((X) x);			// UNnecessary\n" +
			"		foo((XM1) x);	// UNnecessary\n" +
			"		foo((YM1) x);	// necessary \n" +
			"	}\n" +
			"	static void foo(X x) {}\n" +
			"	static void foo(YM1 ym1) {}\n" +
			"  static class XM1 extends X {}\n" +
			"  static class YM1 extends XM1 {}\n" +
			"}\n"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 5)\n" +
		"	foo((X) x);			// UNnecessary\n" +
		"	    ^^^^^\n" +
		"Unnecessary cast from X to X\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	foo((XM1) x);	// UNnecessary\n" +
		"	    ^^^^^^^\n" +
		"Unnecessary cast from X to X.XM1\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=42289
public void test013() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"	\n" +
			"	public static void main(String[] args) {\n" +
			"		int a = 0, b = 1;\n" +
			"		long d;\n" +
			"		d = (long)a; 				// unnecessary\n" +
			"		d = (long)a + b; 		// necessary \n" +
			"		d = d + a + (long)b; 	// unnecessary\n" +
			"	}\n" +
			"}\n" +
			"\n",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 6)\n" +
		"	d = (long)a; 				// unnecessary\n" +
		"	    ^^^^^^^\n" +
		"Unnecessary cast from int to long\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	d = d + a + (long)b; 	// unnecessary\n" +
		"	            ^^^^^^^\n" +
		"Unnecessary cast from int to long\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// 39925 - Unnecessary instanceof checking leads to a NullPointerException
public void test014() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"	boolean b = new Cloneable() {} instanceof Cloneable;\n" +
			"}"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 2)\n" +
		"	boolean b = new Cloneable() {} instanceof Cloneable;\n" +
		"	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"The expression of type new Cloneable(){} is already an instance of type Cloneable\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// narrowing cast on base types may change value, thus necessary
public void test015() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"	void foo() {	\n" +
			"    int lineCount = 10; \n" +
			"    long time = 1000; \n" +
			"    double linePerSeconds1 = ((int) (lineCount * 10000.0 / time)) / 10.0; // necessary \n" +
			"    double linePerSeconds2 = ((double) (lineCount * 10000.0 / time)) / 10.0; // UNnecessary \n" +
			"  } \n" +
			"}"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 6)\n" +
		"	double linePerSeconds2 = ((double) (lineCount * 10000.0 / time)) / 10.0; // UNnecessary \n" +
		"	                         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Unnecessary cast from double to double\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// narrowing cast on base types may change value, thus necessary
public void test016() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"	void foo() {	\n" +
			"    int lineCount = 10; \n" +
			"    long time = 1000; \n" +
			"    print((int) (lineCount * 10000.0 / time)); // necessary \n" +
			"    print((double) (lineCount * 10000.0 / time)); // UNnecessary \n" +
			"  } \n" +
			"  void print(double d) {}  \n" +
			"}"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 6)\n" +
		"	print((double) (lineCount * 10000.0 / time)); // UNnecessary \n" +
		"	      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Unnecessary cast from double to double\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//fault tolerance (40288)
public void test017() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void bar() {\n" +
			"		foo((X) this);\n" +
			"		foo((X) zork());\n" + // unbound #zork() should not cause NPE
			"	}\n" +
			"	void foo(X x) {\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	foo((X) this);\n" +
		"	    ^^^^^^^^\n" +
		"Unnecessary cast from X to X\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	foo((X) zork());\n" +
		"	        ^^^^\n" +
		"The method zork() is undefined for the type X\n" +
		"----------\n",
		null,
		true,
		customOptions);
}
//fault tolerance (40423)
public void test018() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class Y {\n" +
			"	static Y[] foo(int[] tab) {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n" +
			"public class X extends Y {\n" +
			"	Y[] bar() {\n" +
			"		return (Y[]) Y.foo(new double[] {});\n" + // no cast warning until method is applicable
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	return (Y[]) Y.foo(new double[] {});\n" +
		"	               ^^^\n" +
		"The method foo(int[]) in the type Y is not applicable for the arguments (double[])\n" +
		"----------\n",
		null,
		true,
		customOptions);
}
//fault tolerance (40288)
public void tes019() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void bar() {\n" +
			"		X x1 =(X) this;\n" +
			"		X x2 = (X) zork();\n" + // unbound #zork() should not cause NPE
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	X x1 =(X) this;\n" +
		"	      ^^^^^^^^\n" +
		"Unnecessary cast to type X for expression of type X\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	X x2 = (X) zork();\n" +
		"	           ^^^^\n" +
		"The method zork() is undefined for the type X\n" +
		"----------\n",
		null,
		true,
		customOptions);
}
//fault tolerance
public void test020() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void bar() {\n" +
			"		long l = (long)zork() + 2;\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	long l = (long)zork() + 2;\n" +
		"	               ^^^^\n" +
		"The method zork() is undefined for the type X\n" +
		"----------\n",
		null,
		true,
		customOptions);
}

// unnecessary cast diagnosis should also consider receiver type (40572)
public void test021() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"p1/A.java",
			"package p1;\n" +
			"public class A {\n" +
			"	public class Member1 {}\n" +
			"	public class Member2 {}\n" +
			"	class Member3 {}\n" +
			"   public static class Member4 {\n" +
			"	   public static class M4Member {}\n" +
			"   }\n" +
			"}\n",
			"p2/B.java",
			"package p2;\n" +
			"import p1.A;\n" +
			"public class B extends A {\n" +
			"	public class Member1 {}\n" +
			"}\n",
			"p1/C.java",
			"package p1;\n" +
			"import p2.B;\n" +
			"public class C extends B {\n" +
			"	void baz(B b) {\n" +
			"		((A)b).new Member1(); // necessary since would bind to B.Member instead\n" +
			"		((A)b).new Member2(); // UNnecessary\n" +
			"		((A)b).new Member3(); // necessary since visibility issue\n" +
			"		((A)b).new Member4().new M4Member(); // fault tolerance\n" +
			"		((A)zork()).new Member1(); // fault-tolerance\n" +
			"		// anonymous\n"+
			"		((A)b).new Member1(){}; // necessary since would bind to B.Member instead\n" +
			"		((A)b).new Member2(){}; // UNnecessary\n" +
			"		((A)b).new Member3(){}; // necessary since visibility issue\n" +
			"		((A)b).new Member4().new M4Member(){}; // fault tolerance\n" +
			"		((A)zork()).new Member1(){}; // fault-tolerance\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in p1\\C.java (at line 6)\n" +
		"	((A)b).new Member2(); // UNnecessary\n" +
		"	^^^^^^\n" +
		"Unnecessary cast from B to A\n" +
		"----------\n" +
		"2. ERROR in p1\\C.java (at line 8)\n" +
		"	((A)b).new Member4().new M4Member(); // fault tolerance\n" +
		"	^^^^^^\n" +
		"Unnecessary cast from B to A\n" +
		"----------\n" +
		"3. ERROR in p1\\C.java (at line 9)\n" +
		"	((A)zork()).new Member1(); // fault-tolerance\n" +
		"	    ^^^^\n" +
		"The method zork() is undefined for the type C\n" +
		"----------\n" +
		"4. ERROR in p1\\C.java (at line 12)\n" +
		"	((A)b).new Member2(){}; // UNnecessary\n" +
		"	^^^^^^\n" +
		"Unnecessary cast from B to A\n" +
		"----------\n" +
		"5. ERROR in p1\\C.java (at line 14)\n" +
		"	((A)b).new Member4().new M4Member(){}; // fault tolerance\n" +
		"	^^^^^^\n" +
		"Unnecessary cast from B to A\n" +
		"----------\n" +
		"6. ERROR in p1\\C.java (at line 15)\n" +
		"	((A)zork()).new Member1(){}; // fault-tolerance\n" +
		"	    ^^^^\n" +
		"The method zork() is undefined for the type C\n" +
		"----------\n",
		null,
		true,
		customOptions);
}
// unnecessary cast diagnosis should tolerate array receiver type (40752)
public void test022() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.IGNORE);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {	\n" +
			"  void foo(java.util.Map map){ \n" +
			"    int[] fillPattern = new int[0]; \n" +
			"    if (fillPattern.equals((int[])map.get(\"x\"))) { \n" +
			"    }  \n" +
			"  } \n"+
			"} \n",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 4)\n" +
		"	if (fillPattern.equals((int[])map.get(\"x\"))) { \n" +
		"	                       ^^^^^^^^^^^^^^^^^^^\n" +
		"Unnecessary cast from Object to int[]\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}

// unnecessary cast diagnosis should tolerate array receiver type (40752)
public void test023() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		final long lgLow32BitMask1 = ~(~((long) 0) << 32);		// necessary\n" +
			"		final long lgLow32BitMask2 = ~(~0 << 32);					// necessary\n" +
			"		final long lgLow32BitMask3 = ~(~((long) 0L) << 32);	// unnecessary\n" +
			"		final long lgLow32BitMask4 = ~(~((int) 0L) << 32);		// necessary\n" +
			"		System.out.println(\"lgLow32BitMask1: \"+lgLow32BitMask1);\n" +
			"		System.out.println(\"lgLow32BitMask2: \"+lgLow32BitMask2);\n" +
			"	}\n" +
			"}",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 6)\n" +
		"	final long lgLow32BitMask3 = ~(~((long) 0L) << 32);	// unnecessary\n" +
		"	                                ^^^^^^^^^^^\n" +
		"Unnecessary cast from long to long\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}

// unnecessary cast diagnosis for message receiver (44400)
public void test024() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"	public void foo(Object bar) {\n" +
			"		System.out.println(((Object) bar).toString());\n" +
			"	}\n" +
			"}",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 3)\n" +
		"	System.out.println(((Object) bar).toString());\n" +
		"	                   ^^^^^^^^^^^^^^\n" +
		"Unnecessary cast from Object to Object\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}

// unnecessary cast diagnosis for message receiver (44400)
// variation with field access
public void test025() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"	int i;\n" +
			"	public void foo(X bar) {\n" +
			"		System.out.println(((X) bar).i);\n" +
			"	}\n" +
			"}",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 4)\n" +
		"	System.out.println(((X) bar).i);\n" +
		"	                   ^^^^^^^^^\n" +
		"Unnecessary cast from X to X\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47074
 */
public void test026() {

	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel < ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    A a = null;\n" +
				"    B b = (B) a;\n" +
				"  }\n" +
				"}\n" +
				"interface A {\n" +
				"  void doSomething();\n" +
				"}\n" +
				"interface B {\n" +
				"  int doSomething();\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	B b = (B) a;\n" +
			"	      ^^^^^\n" +
			"Cannot cast from A to B\n" +
			"----------\n");
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"    A a = null;\n" +
			"    B b = (B) a;\n" +
			"  }\n" +
			"}\n" +
			"interface A {\n" +
			"  void doSomething();\n" +
			"}\n" +
			"interface B {\n" +
			"  int doSomething();\n" +
			"}",
		},
		"");

}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47074
 */
public void test027() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel < ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    A a = null;\n" +
				"    boolean b = a instanceof B;\n" +
				"  }\n" +
				"}\n" +
				"interface A {\n" +
				"  void doSomething();\n" +
				"}\n" +
				"interface B {\n" +
				"  int doSomething();\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	boolean b = a instanceof B;\n" +
			"	            ^^^^^^^^^^^^^^\n" +
			"Incompatible conditional operand types A and B\n" +
			"----------\n");
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"    A a = null;\n" +
			"    boolean b = a instanceof B;\n" +
			"  }\n" +
			"}\n" +
			"interface A {\n" +
			"  void doSomething();\n" +
			"}\n" +
			"interface B {\n" +
			"  int doSomething();\n" +
			"}",
		},
		"");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47074
 */
public void test028() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel < ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    A a = null;\n" +
				"    B b = null;\n" +
				"    boolean c = a == b;\n" +
				"  }\n" +
				"}\n" +
				"interface A {\n" +
				"  void doSomething();\n" +
				"}\n" +
				"interface B {\n" +
				"  int doSomething();\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	boolean c = a == b;\n" +
			"	            ^^^^^^\n" +
			"Incompatible operand types A and B\n" +
			"----------\n");
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"    A a = null;\n" +
			"    B b = null;\n" +
			"    boolean c = a == b;\n" +
			"  }\n" +
			"}\n" +
			"interface A {\n" +
			"  void doSomething();\n" +
			"}\n" +
			"interface B {\n" +
			"  int doSomething();\n" +
			"}",
		},
		"");

}

/*
 * verify error when assigning null to array
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26903
 */
public void test029() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n" +
			"    public static void main(String[] args) {	\n" +
			"		try {	\n" +
			"		    char[][] qName;	\n" +
			"			qName = null;	\n" +
			"			qName[0] = new char[1];	\n" +
			"		} catch(Exception e){	\n" +
			"		}	\n" +
			"		try {	\n" +
			"		    char[][] qName;	\n" +
			"			qName = (char[][])null;	\n" +
			"			qName[0] = new char[1];	\n" +
			"		} catch(Exception e){	\n" +
			"		}	\n" +
			"		try {	\n" +
			"		    char[][] qName;	\n" +
			"			qName = (char[][])(char[][])(char[][])null;	\n" +
			"			qName[0] = new char[2];	\n" +
			"		} catch(Exception e){	\n" +
			"		}	\n" +
			"		try {	\n" +
			"		    char[][] qName;	\n" +
			"			qName = args.length > 1 ? new char[1][2] : null;	\n" +
			"			qName[0] = new char[3];	\n" +
			"		} catch(Exception e){	\n" +
			"		}	\n" +
			"		System.out.println(\"SUCCESS\");	\n"+
			"	}	\n" +
			"}	\n",
		},
	"SUCCESS");
}

/*
 * verify error when assigning null to array
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26903
 */
public void test030() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n" +
			"    public static void main(String[] args) {	\n" +
			"		try {	\n" +
			"			char[][] qName = null;	\n" +
			"			qName[0] = new char[1];	\n" +
			"		} catch(Exception e){	\n" +
			"		}	\n" +
			"		try {	\n" +
			"			char[][] qName = (char[][])null;	\n" +
			"			qName[0] = new char[1];	\n" +
			"		} catch(Exception e){	\n" +
			"		}	\n" +
			"		try {	\n" +
			"			char[][] qName = (char[][])(char[][])(char[][])null;	\n" +
			"			qName[0] = new char[2];	\n" +
			"		} catch(Exception e){	\n" +
			"		}	\n" +
			"		try {	\n" +
			"			char[][] qName = args.length > 1 ? new char[1][2] : null;	\n" +
			"			qName[0] = new char[3];	\n" +
			"		} catch(Exception e){	\n" +
			"		}	\n" +
			"		System.out.println(\"SUCCESS\");	\n"+
			"	}	\n" +
			"}	\n",
		},
	"SUCCESS");
}

/*
 * verify error when assigning null to array
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26903
 */
public void test031() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n" +
			"    public static void main(String[] args) {	\n" +
			"		try {	\n" +
			"			char[][] qName = null;	\n" +
			"			setName(qName[0]);	\n" +
			"		} catch(Exception e){	\n" +
			"		}	\n" +
			"		try {	\n" +
			"			char[][] qName = (char[][])null;	\n" +
			"			setName(qName[0]);	\n" +
			"		} catch(Exception e){	\n" +
			"		}	\n" +
			"		try {	\n" +
			"			char[][] qName = (char[][])(char[][])(char[][])null;	\n" +
			"			setName(qName[0]);	\n" +
			"		} catch(Exception e){	\n" +
			"		}	\n" +
			"		try {	\n" +
			"			char[][] qName = args.length > 1 ? new char[1][2] : null;	\n" +
			"			setName(qName[0]);	\n" +
			"		} catch(Exception e){	\n" +
			"		}	\n" +
			"		System.out.println(\"SUCCESS\");	\n"+
			"	}	\n" +
			"	static void setName(char[] name) {	\n"+
			"	}	\n" +
			"}	\n",
		},
	"SUCCESS");
}
/*
 * verify error when assigning null to array
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26903
 */
public void test032() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"			try {\n" +
			"				((int[]) null)[0] = 0;\n" +
			"				((int[]) null)[0] += 1;\n" +
			"				((int[]) null)[0] ++;\n" +
			"			} catch (NullPointerException e) {\n" +
			"				System.out.print(\"SUCCESS\");\n" +
			"			}\n" +
			"	}\n" +
			"}\n",
		},
	"SUCCESS");
}

/*
 * unused cast diagnosis
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=54763
 */
public void test033() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"import java.util.ArrayList;\n" +
			"import java.util.List;\n" +
			"\n" +
			"public class X {\n" +
			"    public static void main(String [] args) {\n" +
			"        List list = (List) new ArrayList();\n" +
			"        list = (List) new ArrayList();\n" +
			"        \n" +
			"        String s = (String) \"hello\";\n" +
			"        s += (List) new ArrayList();\n" +
			"        \n" +
			"        ArrayList alist = new ArrayList();\n" +
			"        List list2 = (List) alist;\n" +
			"        list2 = (List) alist;\n" +
			"        \n" +
			"        String s2 = (String) \"hello\";\n" +
			"        s2 += (List) alist;\n" +
			"    }\n" +
			"}\n"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 6)\n" +
		"	List list = (List) new ArrayList();\n" +
		"	            ^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Unnecessary cast from ArrayList to List\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	list = (List) new ArrayList();\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Unnecessary cast from ArrayList to List\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 9)\n" +
		"	String s = (String) \"hello\";\n" +
		"	           ^^^^^^^^^^^^^^^^\n" +
		"Unnecessary cast from String to String\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 10)\n" +
		"	s += (List) new ArrayList();\n" +
		"	     ^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Unnecessary cast from ArrayList to List\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 13)\n" +
		"	List list2 = (List) alist;\n" +
		"	             ^^^^^^^^^^^^\n" +
		"Unnecessary cast from ArrayList to List\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 14)\n" +
		"	list2 = (List) alist;\n" +
		"	        ^^^^^^^^^^^^\n" +
		"Unnecessary cast from ArrayList to List\n" +
		"----------\n" +
		"7. ERROR in X.java (at line 16)\n" +
		"	String s2 = (String) \"hello\";\n" +
		"	            ^^^^^^^^^^^^^^^^\n" +
		"Unnecessary cast from String to String\n" +
		"----------\n" +
		"8. ERROR in X.java (at line 17)\n" +
		"	s2 += (List) alist;\n" +
		"	      ^^^^^^^^^^^^\n" +
		"Unnecessary cast from ArrayList to List\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
/*
 * check non insertion of checkcast for unnecessary cast to interfaces
 * (same test case as test033)
 */
public void test034() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"import java.util.List;\n" +
			"\n" +
			"public class X {\n" +
			"    public static void main(String [] args) {\n" +
			"        List list = (List) new ArrayList();\n" +
			"        list = (List) new ArrayList();\n" +
			"        \n" +
			"        ArrayList alist = new ArrayList();\n" +
			"        List list2 = (List) alist;\n" +
			"        list2 = (List) alist;\n" +
			"        \n" +
			"       System.out.println(\"SUCCESS\");\n" +
			"    }\n" +
			"}\n",
		},
		"SUCCESS");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" +
		"  // Stack: 2, Locals: 4\n" +
		"  public static void main(java.lang.String[] args);\n" +
		"     0  new java.util.ArrayList [16]\n" +
		"     3  dup\n" +
		"     4  invokespecial java.util.ArrayList() [18]\n" +
		"     7  astore_1 [list]\n" +
		"     8  new java.util.ArrayList [16]\n" +
		"    11  dup\n" +
		"    12  invokespecial java.util.ArrayList() [18]\n" +
		"    15  astore_1 [list]\n" +
		"    16  new java.util.ArrayList [16]\n" +
		"    19  dup\n" +
		"    20  invokespecial java.util.ArrayList() [18]\n" +
		"    23  astore_2 [alist]\n" +
		"    24  aload_2 [alist]\n" +
		"    25  astore_3 [list2]\n" +
		"    26  aload_2 [alist]\n" +
		"    27  astore_3 [list2]\n" +
		"    28  getstatic java.lang.System.out : java.io.PrintStream [19]\n" +
		"    31  ldc <String \"SUCCESS\"> [25]\n" +
		"    33  invokevirtual java.io.PrintStream.println(java.lang.String) : void [27]\n" +
		"    36  return\n" +
		"      Line numbers:\n" +
		"        [pc: 0, line: 6]\n" +
		"        [pc: 8, line: 7]\n" +
		"        [pc: 16, line: 9]\n" +
		"        [pc: 24, line: 10]\n" +
		"        [pc: 26, line: 11]\n" +
		"        [pc: 28, line: 13]\n" +
		"        [pc: 36, line: 14]\n" +
		"      Local variable table:\n" +
		"        [pc: 0, pc: 37] local: args index: 0 type: java.lang.String[]\n" +
		"        [pc: 8, pc: 37] local: list index: 1 type: java.util.List\n" +
		"        [pc: 24, pc: 37] local: alist index: 2 type: java.util.ArrayList\n" +
		"        [pc: 26, pc: 37] local: list2 index: 3 type: java.util.List\n";
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 2));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}
// javac incorrectly accepts it
public void test035() {
	this.runNegativeTest(
		new String[] {
			"Test231.java",
			"public class Test231 implements Test231i\n" +
			"{\n" +
			"	void	foo()\n" +
			"	{\n" +
			"		new Object()\n" +
			"		{\n" +
			"			Test231i	bar()\n" +
			"			{\n" +
			"				return	(Test231i)this;\n" +
			"			}\n" +
			"		};\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"\n" +
			"interface Test231i\n" +
			"{\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in Test231.java (at line 9)\n" +
		"	return	(Test231i)this;\n" +
		"	      	^^^^^^^^^^^^^^\n" +
		"Cannot cast from new Object(){} to Test231i\n" +
		"----------\n",
		// javac options
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10 /* javac test options */);
}
public void test036() {
	runConformTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public final class X {\n" +
			"	private static final boolean DO_BUG = true;\n" +
			"\n" +
			"	// Workaround: cast null to Base\n" +
			"	private static Base base = DO_BUG ?\n" +
			"	// (Base)null\n" +
			"			null : new Base() {\n" +
			"				public final String test() {\n" +
			"					return (\"anonymous\");\n" +
			"				}\n" +
			"			};\n" +
			"\n" +
			"	private X() {\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] argv) {\n" +
			"		if (base == null)\n" +
			"			System.out.println(\"no base\");\n" +
			"		else\n" +
			"			System.out.println(base.test());\n" +
			"	}\n" +
			"\n" +
			"	private static abstract class Base {\n" +
			"		public Base() {\n" +
			"		}\n" +
			"\n" +
			"		public abstract String test();\n" +
			"	}\n" +
			"}\n"
		},
		// compiler results
		"", /* expected compiler log */
		// runtime results
		"no base" /* expected output string */,
		"" /* expected error string */,
		// javac options
		JavacTestOptions.JavacHasABug.JavacGeneratesByteCodeUponWhichJavaThrowsAnException /* javac test options */);
}
public void test037() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Integer[] integers = {};\n" +
			"		int[] ints = (int[]) integers;\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	int[] ints = (int[]) integers;\n" +
		"	             ^^^^^^^^^^^^^^^^\n" +
		"Cannot cast from Integer[] to int[]\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101208
public void test038() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo() {\n" +
			"		System.out.println(null instanceof Object);\n" +
			"      Zork z;\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\r\n" +
		"	Zork z;\r\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}
//unnecessary cast warnings in assignment (Object o = (String) something).
public void test039() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"public class X {\n" +
			"	Object fo = (String) new Object();\n" +
			"	void foo(ArrayList al) {\n" +
			"		List l = (List) al;\n" +
			"		Object o;\n" +
			"		o = (ArrayList) al;\n" +
			"		Object o2 = (ArrayList) al;\n" +
			"		o = (ArrayList) l;\n" +
			"		Object o3 = (ArrayList) l;\n" +
			"		Zork z;\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	Object fo = (String) new Object();\n" +
		"	            ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Unnecessary cast from Object to String\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 5)\n" +
		"	List l = (List) al;\n" +
		"	         ^^^^^^^^^\n" +
		"Unnecessary cast from ArrayList to List\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 7)\n" +
		"	o = (ArrayList) al;\n" +
		"	    ^^^^^^^^^^^^^^\n" +
		"Unnecessary cast from ArrayList to ArrayList\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 8)\n" +
		"	Object o2 = (ArrayList) al;\n" +
		"	            ^^^^^^^^^^^^^^\n" +
		"Unnecessary cast from ArrayList to ArrayList\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 9)\n" +
		"	o = (ArrayList) l;\n" +
		"	    ^^^^^^^^^^^^^\n" +
		"Unnecessary cast from List to ArrayList\n" +
		"----------\n" +
		"6. WARNING in X.java (at line 10)\n" +
		"	Object o3 = (ArrayList) l;\n" +
		"	            ^^^^^^^^^^^^^\n" +
		"Unnecessary cast from List to ArrayList\n" +
		"----------\n" +
		"7. ERROR in X.java (at line 11)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}
//http://bugs.eclipse.org/bugs/show_bug.cgi?id=116647
public void test040() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	{\n" +
			"		int i = 12;\n" +
			"		int j = (byte) i;\n" +
			"		float f = (float) i;\n" +
			"		Zork z;\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 5)\n" +
		"	float f = (float) i;\n" +
		"	          ^^^^^^^^^\n" +
		"Unnecessary cast from int to float\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158855
public void test041() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public abstract class X {\n" +
			"    class A extends X {\n" +
			"        public void callMe() {\n" +
			"        }\n" +
			"    }\n" +
			"    public abstract void callMe();\n" +
			"    class B {\n" +
			"        public void callSite() {\n" +
			"            // expect warning not there:\n" +
			"            ((A) this.getAA()).callMe();\n" +
			"            Integer max = new Integer(1);\n" +
			"            // execpted warning there:\n" +
			"            Integer other = (Integer) max;\n" +
			"        }\n" +
			"        public X getAA() {\n" +
			"            Zork z;\n" +
			"            return null;\n" +
			"        }\n" +
			"    }\n" +
			"}", // =================
		},
		"----------\n" +
		"1. WARNING in X.java (at line 13)\n" +
		"	Integer other = (Integer) max;\n" +
		"	                ^^^^^^^^^^^^^\n" +
		"Unnecessary cast from Integer to Integer\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 16)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=159654
public void test042() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.print(\"SUCCESS\");	\n" +
			"	}\n" +
			"	\n" +
			"	public static void foo(boolean b, List l) {\n" +
			"		if (b) {\n" +
			"			String s = (String) l.get(0);\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=159654
public void test043() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.print(\"SUCCESS\");	\n" +
			"	}\n" +
			"	\n" +
			"	public static void foo(boolean b, List l) {\n" +
			"		if (b) {\n" +
			"			Object o = (Object) l.get(0);\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=159822
public void test044() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static String s;\n" +
			"    public static void main(String[] args) throws Throwable {\n" +
			"      if (args.length == 0) {\n" +
			"        Class c = Class.forName(\"X\");\n" +
			"        String s = ((X) c.newInstance()).s;\n" +
			"        System.out.println(s);\n" +
			"      }\n" +
			"      System.out.println();\n" +
			"    }\n" +
			"}",
		},
		"null");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239305
public void test045() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		System.out.println(castLongToInt(3));\n" + 
			"	}\n" + 
			"	private static int castLongToInt(long longVal) {\n" + 
			"		return (int)((long)longVal);\n" + 
			"	}\n" + 
			"}\n",
		},
		"3");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=282869
public void test046() {
	this.runConformTest(
		true,
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		char a = 65;\n" + 
			"		String b = \"\" + a; // -> \"A\"\n" + 
			"		String c = \"\" + (int) a;\n" + 
			"		System.out.print(b);\n" + 
			"		System.out.print(c);\n" + 
			"		\n" + 
			"		String logText = \" second case \";\n" + 
			"		char firstChar = 65;\n" + 
			"		logText += (int) firstChar;\n" + 
			"		System.out.println(logText);\n" + 
			"	}\n" + 
			"}",
		},
		"",
		"A65 second case 65",
		"",
		JavacTestOptions.SKIP);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=287676
// Test to make sure that an unnecessary cast warning is produced in case of
// wrapper types like Integer, Character, Short, Byte, etc.
public void test047() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel < ClassFileConstants.JDK1_5) return;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"public class X{\n" +
			"	void test() {" +
			"		Integer a = 1;\n" +
			"		ArrayList<Character> aList = new ArrayList<Character>(1);\n" +
			"		a = (Integer)a + (Integer)2;\n" +
			"		if ((Character)aList.get(0) == 'c')\n" +
			"			System.out.println();\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 5)\n" +
		"	a = (Integer)a + (Integer)2;\n" +
		"	    ^^^^^^^^^^\n" +
		"Unnecessary cast from Integer to Integer\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 5)\n" +
		"	a = (Integer)a + (Integer)2;\n" +
		"	                 ^^^^^^^^^^\n" +
		"Unnecessary cast from int to Integer\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 6)\n" +
		"	if ((Character)aList.get(0) == 'c')\n" +
		"	    ^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Unnecessary cast from Character to Character\n" +
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=302919
public void test048() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel < ClassFileConstants.JDK1_5) return;
	this.runNegativeTest(
		new String[] {
			"A.java",
			"public class A<T> extends D<T> {\n" +
			"    public class A1 extends D1 {\n" +
			"    }\n" +
			"    void m1(A<T> tree) {\n" +
			"        A.A1 v = ((A.A1) tree.root);\n" +
			"    }\n" +
			"    Zork z;\n" +
			"}\n" +
			"class D<T> {\n" +
			"    protected D1 root;\n" +
			"    protected class D1 {\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in A.java (at line 7)\n" + 
		"	Zork z;\n" + 
		"	^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=302919
public void test049() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel < ClassFileConstants.JDK1_5) return;
	this.runNegativeTest(
		new String[] {
			"A.java",
			"public class A {\n" +
			"	void foo(Other2<?>.Member2<?> om2) {\n" +
			"		Other<?>.Member m = (Other<?>.Member) om2;\n" +
			"		m = om2;\n" +
			"	}\n" +
			"}\n" +
			"class Other<T> {\n" +
			"	class Member {}\n" +
			"}\n" +
			"class Other2<T> extends Other<T> {\n" +
			"	class Member2<U> extends Other<U>.Member {\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in A.java (at line 3)\n" + 
		"	Other<?>.Member m = (Other<?>.Member) om2;\n" + 
		"	                    ^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Unnecessary cast from Other2<?>.Member2<capture#1-of ?> to Other<?>.Member\n" + 
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=302919
public void test050() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel < ClassFileConstants.JDK1_5) return;
	this.runNegativeTest(
		new String[] {
			"A.java",
			"public class A<T> extends D<T> {\n" +
			"    public class A1 extends D.D1 {\n" +
			"    }\n" +
			"    void m1(A<T> tree) {\n" +
			"        A.A1 v = ((A.A1) tree.root);\n" +
			"    }\n" +
			"    Zork z;\n" +
			"}\n" +
			"class D<T> {\n" +
			"    protected D1 root;\n" +
			"    protected class D1 {\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in A.java (at line 7)\n" + 
		"	Zork z;\n" + 
		"	^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353085
public void test051() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	String source =
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		Object x = foo();\n" + 
			"		boolean y = (boolean) x;\n" + 
			"		System.out.println(y);\n" + 
			"	}\n" + 
			"	public static Object foo() {\n" + 
			"		return Boolean.TRUE;\n" + 
			"	}\n" + 
			"}";
	if (options.sourceLevel < ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					source
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	boolean y = (boolean) x;\n" + 
				"	            ^^^^^^^^^^^\n" + 
				"Cannot cast from Object to boolean\n" + 
				"----------\n"
			);
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					source
				},
				"true"
			);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353085
public void test052() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	String source =
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		Object x = foo();\n" + 
			"		byte y = (byte) x;\n" + 
			"		System.out.println(y);\n" + 
			"	}\n" + 
			"	public static Object foo() {\n" + 
			"		return new Byte((byte)1);\n" + 
			"	}\n" + 
			"}";
	if (options.sourceLevel < ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					source
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	byte y = (byte) x;\n" + 
				"	         ^^^^^^^^\n" + 
				"Cannot cast from Object to byte\n" + 
				"----------\n"
			);
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					source
				},
				"1"
			);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353085
public void test053() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	String source =
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		Object x = foo();\n" + 
			"		char y = (char) x;\n" + 
			"		System.out.println(y);\n" + 
			"	}\n" + 
			"	public static Object foo() {\n" + 
			"		return new Character('d');\n" + 
			"	}\n" + 
			"}";
	if (options.sourceLevel < ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					source
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	char y = (char) x;\n" + 
				"	         ^^^^^^^^\n" + 
				"Cannot cast from Object to char\n" + 
				"----------\n"
			);
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					source
				},
				"d"
			);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353085
// Also confirm that a check cast and unboxing conversion are generated.
public void test054() throws Exception {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	String source =
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		Object x = foo();\n" + 
			"		int y = (int) x;\n" + 
			"		System.out.println(y);\n" + 
			"	}\n" + 
			"	public static Object foo() {\n" + 
			"		return new Integer(1);\n" + 
			"	}\n" + 
			"}";
	if (options.sourceLevel < ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					source
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	int y = (int) x;\n" + 
				"	        ^^^^^^^\n" + 
				"Cannot cast from Object to int\n" + 
				"----------\n"
			);
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					source
				},
				"1"
			);
		String expectedOutput = 
				"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
				"  // Stack: 2, Locals: 3\n" + 
				"  public static void main(java.lang.String[] args);\n" + 
				"     0  invokestatic X.foo() : java.lang.Object [16]\n" + 
				"     3  astore_1 [x]\n" + 
				"     4  aload_1 [x]\n" + 
				"     5  checkcast java.lang.Integer [20]\n" + 
				"     8  invokevirtual java.lang.Integer.intValue() : int [22]\n" + 
				"    11  istore_2 [y]\n" + 
				"    12  getstatic java.lang.System.out : java.io.PrintStream [26]\n" + 
				"    15  iload_2 [y]\n" + 
				"    16  invokevirtual java.io.PrintStream.println(int) : void [32]\n" + 
				"    19  return\n" + 
				"      Line numbers:\n" + 
				"        [pc: 0, line: 3]\n" + 
				"        [pc: 4, line: 4]\n" + 
				"        [pc: 12, line: 5]\n" + 
				"        [pc: 19, line: 6]\n" + 
				"      Local variable table:\n" + 
				"        [pc: 0, pc: 20] local: args index: 0 type: java.lang.String[]\n" + 
				"        [pc: 4, pc: 20] local: x index: 1 type: java.lang.Object\n" + 
				"        [pc: 12, pc: 20] local: y index: 2 type: int\n" + 
				"  \n";
		File f = new File(OUTPUT_DIR + File.separator + "X.class");
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
		int index = result.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(result, 3));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, result);
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353085
public void test055() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	String source =
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		Object x = foo();\n" + 
			"		long y = (long) x;\n" + 
			"		System.out.println(y);\n" + 
			"	}\n" + 
			"	public static Object foo() {\n" + 
			"		return new Long(Long.MAX_VALUE);\n" + 
			"	}\n" + 
			"}";
	if (options.sourceLevel < ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					source
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	long y = (long) x;\n" + 
				"	         ^^^^^^^^\n" + 
				"Cannot cast from Object to long\n" + 
				"----------\n"
			);
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					source
				},
				"9223372036854775807"
			);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353085
public void test056() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	String source =
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		Object x = foo();\n" + 
			"		short y = (short) x;\n" + 
			"		System.out.println(y);\n" + 
			"	}\n" + 
			"	public static Object foo() {\n" + 
			"		return new Short((short) 1);\n" + 
			"	}\n" + 
			"}";
	if (options.sourceLevel < ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					source
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	short y = (short) x;\n" + 
				"	          ^^^^^^^^^\n" + 
				"Cannot cast from Object to short\n" + 
				"----------\n"
			);
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					source
				},
				"1"
			);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353085
public void test057() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	String source =
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		Object x = foo();\n" + 
			"		double y = (double) x;\n" + 
			"		System.out.println(y);\n" + 
			"	}\n" + 
			"	public static Object foo() {\n" + 
			"		return new Double(1.0);\n" + 
			"	}\n" + 
			"}";
	if (options.sourceLevel < ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					source
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	double y = (double) x;\n" + 
				"	           ^^^^^^^^^^\n" + 
				"Cannot cast from Object to double\n" + 
				"----------\n"
			);
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					source
				},
				"1.0"
			);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353085
public void test058() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	String source =
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		Object x = foo();\n" + 
			"		float y = (float) x;\n" + 
			"		System.out.println(y);\n" + 
			"	}\n" + 
			"	public static Object foo() {\n" + 
			"		return new Float(1.0f);\n" + 
			"	}\n" + 
			"}";
	if (options.sourceLevel < ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					source
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	float y = (float) x;\n" + 
				"	          ^^^^^^^^^\n" + 
				"Cannot cast from Object to float\n" + 
				"----------\n"
			);
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					source
				},
				"1.0"
			);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353085
public void test059() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	String source =
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		Object x = foo();\n" +
			"		try {\n" + 
			"			int y = (int) x;\n" +
			"		} catch (ClassCastException e) {\n" +
			"			System.out.println(\"SUCCESS\");\n" +
			"			return;\n" +
			"		}\n" + 
			"		System.out.println(\"FAIL\");\n" + 
			"	}\n" + 
			"	public static Object foo() {\n" + 
			"		return new Float(1.0f);\n" + 
			"	}\n" + 
			"}";
	if (options.sourceLevel < ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					source
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	int y = (int) x;\n" + 
				"	        ^^^^^^^\n" + 
				"Cannot cast from Object to int\n" + 
				"----------\n"
			);
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					source
				},
				"SUCCESS"
			);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353085
public void test059b() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	String source =
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		Object x = foo();\n" + 
			"		try {\n" + 
			"			int y = (int) x;\n" +
			"		} catch (ClassCastException e) {\n" +
			"			System.out.println(\"SUCCESS\");\n" +
			"			return;\n" +
			"		}\n" + 
			"		System.out.println(\"FAIL\");\n" +
			"	}\n" + 
			"	public static Object foo() {\n" + 
			"		return Boolean.TRUE;\n" + 
			"	}\n" + 
			"}";
	if (options.sourceLevel < ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					source
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	int y = (int) x;\n" + 
				"	        ^^^^^^^\n" + 
				"Cannot cast from Object to int\n" + 
				"----------\n"
			);
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					source
				},
				"SUCCESS"
			);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353085
public void test059c() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	String source =
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		Object x = foo();\n" + 
			"		try {\n" + 
			"			char y = (char) x;\n" +
			"		} catch (ClassCastException e) {\n" +
			"			System.out.println(\"SUCCESS\");\n" +
			"			return;\n" +
			"		}\n" + 
			"		System.out.println(\"FAIL\");\n" +
			"	}\n" + 
			"	public static Object foo() {\n" + 
			"		return Boolean.TRUE;\n" + 
			"	}\n" + 
			"}";
	if (options.sourceLevel < ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					source
				},
				"----------\n" + 
				"1. ERROR in X.java (at line 5)\n" + 
				"	char y = (char) x;\n" + 
				"	         ^^^^^^^^\n" + 
				"Cannot cast from Object to char\n" + 
				"----------\n"
			);
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					source
				},
				"SUCCESS"
			);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353085
public void test060() {
	String source =
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		Object x = foo();\n" + 
			"		Boolean y = (Boolean) x;\n" + 
			"		System.out.println(y);\n" + 
			"	}\n" + 
			"	public static Object foo() {\n" + 
			"		return Boolean.TRUE;\n" + 
			"	}\n" + 
			"}";
	this.runConformTest(
			new String[] {
				"X.java",
				source
			},
			"true"
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353085
public void test061() {
	String source =
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		Object x = foo();\n" + 
			"		try {\n" + 
			"			Float y = (Float) x;\n" +
			"		} catch (ClassCastException e) {\n" +
			"			System.out.println(\"SUCCESS\");\n" +
			"			return;\n" +
			"		}\n" + 
			"		System.out.println(\"FAIL\");\n" +
			"	}\n" + 
			"	public static Object foo() {\n" + 
			"		return Boolean.TRUE;\n" + 
			"	}\n" + 
			"}";
	this.runConformTest(
			new String[] {
				"X.java",
				source
			},
			"SUCCESS"
		);
}
public static Class testClass() {
	return CastTest.class;
}
}
