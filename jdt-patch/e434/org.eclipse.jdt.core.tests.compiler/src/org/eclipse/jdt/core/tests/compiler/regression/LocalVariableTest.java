/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
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
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contribution for bug 185682 - Increment/decrement operators mark local variables as read
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class LocalVariableTest extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "testBug537033" };
}
public LocalVariableTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

public void test001() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"public class X {\n" +
		"        int foo(){\n" +
		"                int i;\n" +
		"                return 1;\n" +
		"        }\n" +
		"}\n",
	});
}
public void test002() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"public class X {\n" +
		"  void foo() {\n" +
		"    String temp;\n" +
		"    try {\n" +
		"      return;\n" +
		"    }\n" +
		"    catch (Exception e){\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
	});
}
public void test003() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"public class X {\n" +
		"  void foo() {\n" +
		"    String temp;\n" +
		"    try {\n" +
		"      return;\n" +
		"    }\n" +
		"    catch (Exception e) {\n" +
		"    }\n" +
		"  }\n" +
		"}\n",
	});
}
public void test004() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"public class X {\n" +
		"  {\n" +
		"     int i = 1;\n" +
		"    System.out.println(i);\n" +
		"  }\n" +
		"  X(int j){\n" +
		"  }\n" +
		"}\n",
	});
}
public void test005() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"public class X {\n" +
		"  int j;\n" +
		"  void f1() {\n" +
		"    int l;\n" +
		"    switch (j) {\n" +
		"      case 0 :\n" +
		"        l = 10;\n" +
		"		 l++;\n" + // at least one read usage
		"        break;\n" +
		"      case 1 :\n" +
		"        l = 20;\n" +
		"        break;\n" +
		"      case 2 :\n" +
		"        l = 30;\n" +
		"        break;\n" +
		"      default :\n" +
		"        l = 10;\n" +
		"        break;\n" +
		"    }\n" +
		"  }\n" +
		"  public static void main(String args[]) {\n" +
		"  }\n" +
		"}\n",
	});
}

public void test006() {
	this.runConformTest(new String[] {
		"p/Truc.java",
		"package p;\n" +
		"public class Truc{\n" +
		"   void foo(){\n" +
		"      final int i; \n" +
		"	   i = 1;\n" +
		"      if (false) i = 2;\n" +
		"   } \n" +
		"	public static void main(java.lang.String[] args) {\n" +
		"  		System.out.println(\"SUCCESS\"); \n" +
		"	}	\n" +
		"}",
	},
	"SUCCESS");
}

public void test007() {
	this.runConformTest(new String[] {
		"p/A.java",
		"package p;\n" +
		"import p.helper.Y;\n" +
		"class A extends Y {\n" +
		"  class Y {\n" +
		"    int j = i;// i is a protected member inherited from Y\n" +
		"  }\n" +
		"}",

		"p/helper/Y.java",
		"package p.helper;\n" +
		"public class Y {\n" +
		"  protected int i = 10;\n" +
		"  public inner in = new inner();\n" +
		"    \n" +
		"  protected class inner {\n" +
		"    public int  f() {\n" +
		"      return 20;\n" +
		"    }\n" +
		"  }\n" +
		"}",

	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127078
public void test008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"	class Y {\n" +
			"		Y innerY;\n" +
			"\n" +
			"		int longMemberName;\n" +
			"	}\n" +
			"\n" +
			"	static public void main(String args[]) {\n" +
			"		Y y;\n" +
			"		System.out.println(y.innerY.longMemberName);\n" +
			"	}\n" +
			"}"},
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	System.out.println(y.innerY.longMemberName);\n" +
			"	                   ^\n" +
			"The local variable y may not have been initialized\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127078
public void test009() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"	class Y {\n" +
			"		int longMemberName;\n" +
			"	}\n" +
			"\n" +
			"	static public void main(String args[]) {\n" +
			"		Y y;\n" +
			"		System.out.println(y.longMemberName);\n" +
			"	}\n" +
			"}"},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	System.out.println(y.longMemberName);\n" +
			"	                   ^\n" +
			"The local variable y may not have been initialized\n" +
			"----------\n");
}
public void test010() {
	Map options = getCompilerOptions();
	options.put(
		CompilerOptions.OPTION_DocCommentSupport,
		CompilerOptions.ENABLED);
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" +
		"/**\n" +
		" * @see Y\n" +
		" */\n" +
		"public class X {\n" +
		"}",
		"p/Y.java",
		"package p;\n" +
		"class Z {\n" +
		"}",
	},
	"",
	null,
	true,
	null,
	options,
	null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=144426
public void test011() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"        public static void main(String[] args) {\n" +
				"                int x = 2;\n" +
				"                if (true) {\n" +
				"                        int x = 4;\n" +
				"                }\n" +
				"        }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	int x = 4;\n" +
			"	    ^\n" +
			"Duplicate local variable x\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=144858
public void test012() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"        public static void main(String[] args) {\n" +
				"                int x = x = 0;\n" +
				"                if (true) {\n" +
				"                        int x = x = 1;\n" +
				"                }\n" +
				"        }\n" +
				"}\n",
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	int x = x = 0;\n" +
			"	    ^^^^^^^^^\n" +
			"The assignment to variable x has no effect\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	int x = x = 1;\n" +
			"	    ^\n" +
			"Duplicate local variable x\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 5)\n" +
			"	int x = x = 1;\n" +
			"	    ^^^^^^^^^\n" +
			"The assignment to variable x has no effect\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=144858 - variation
//check variable collision resiliance (catch argument)
// variable collision should not interfere with exception collision
public void test013() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"        public static void main(String[] args) {\n" +
				"                int x = 2;\n" +
				"                try {\n" +
				"                	\n" +
				"                } catch(Exception x) {\n" +
				"                } catch(Exception e) {\n" +
				"                }\n" +
				"        }\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	} catch(Exception x) {\n" +
			"	                  ^\n" +
			"Duplicate parameter x\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	} catch(Exception e) {\n" +
			"	        ^^^^^^^^^\n" +
			"Unreachable catch block for Exception. It is already handled by the catch block for Exception\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=144858 - variation
public void test014() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void foo(){\n" +
				"		int x = 0;\n" +
				"		String x = \"\";\n" +
				"		x.toString();\n" +
				"	  }\n" +
				"	}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	String x = \"\";\n" +
			"	       ^\n" +
			"Duplicate local variable x\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157379
public void test015() {
	Map options = getCompilerOptions();
	if (this.complianceLevel == ClassFileConstants.JDK1_3) return;
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"        public static boolean test() {\n" +
				"                boolean b = false;\n" +
				"                assert b = true;\n" +
				"                return false;\n" +
				"        }\n" +
				"        public static void main(String[] args) {\n" +
				"                test();\n" +
				"                System.out.println(\"SUCCESS\");\n" +
				"        }\n" +
				"}\n",
			},
			"SUCCESS",
			null,
			true,
			null,
			options,
			null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=118217
public void test016() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterIncludeDocCommentReference, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterWhenOverridingConcrete, CompilerOptions.DISABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterWhenImplementingAbstract, CompilerOptions.DISABLED);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X extends Parent implements Doable {\n" +
			"	/**\n" +
			"	 * @param value\n" +
			"	 */\n" +
			"	void foo(int value) { // X#foo(...)\n" +
			"	}\n" +
			"	void bar(int value) { // X#bar(...)\n" +
			"	}\n" +
			"\n" +
			"	void top(int value) { /* X#top(...)*/}\n" +
			"	void parent(int value) { /* X#parent(...) */}\n" +
			"	public void doit(int value) { /* X#doit(...) */}\n" +
			"}\n" +
			"abstract class Top {\n" +
			"	/**\n" +
			"	 * @param value\n" +
			"	 */\n" +
			"	abstract void top(int value); // Top#top(...)\n" +
			"}\n" +
			"abstract class Parent extends Top {\n" +
			"	/**\n" +
			"	 * @param value\n" +
			"	 */\n" +
			"	void parent(int value) { /* Parent#parent(...) */}\n" +
			"}\n" +
			"interface Doable {\n" +
			"	/**\n" +
			"	 * @param value\n" +
			"	 */\n" +
			"	void doit (int value); // Doable#doit(...)\n" +
			"}", // =================
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 7)\n" +
		"	void bar(int value) { // X#bar(...)\n" +
		"	             ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=118217 - variation
public void test017() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.DISABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterIncludeDocCommentReference, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterWhenOverridingConcrete, CompilerOptions.DISABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterWhenImplementingAbstract, CompilerOptions.DISABLED);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X extends Parent implements Doable {\n" +
			"	/**\n" +
			"	 * @param value\n" +
			"	 */\n" +
			"	void foo(int value) { // X#foo(...)\n" +
			"	}\n" +
			"	void bar(int value) { // X#bar(...)\n" +
			"	}\n" +
			"\n" +
			"	void top(int value) { /* X#top(...)*/}\n" +
			"	void parent(int value) { /* X#parent(...) */}\n" +
			"	public void doit(int value) { /* X#doit(...) */}\n" +
			"}\n" +
			"abstract class Top {\n" +
			"	/**\n" +
			"	 * @param value\n" +
			"	 */\n" +
			"	abstract void top(int value); // Top#top(...)\n" +
			"}\n" +
			"abstract class Parent extends Top {\n" +
			"	/**\n" +
			"	 * @param value\n" +
			"	 */\n" +
			"	void parent(int value) { /* Parent#parent(...) */}\n" +
			"}\n" +
			"interface Doable {\n" +
			"	/**\n" +
			"	 * @param value\n" +
			"	 */\n" +
			"	void doit (int value); // Doable#doit(...)\n" +
			"}", // =================
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 5)\n" +
		"	void foo(int value) { // X#foo(...)\n" +
		"	             ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	void bar(int value) { // X#bar(...)\n" +
		"	             ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 24)\n" +
		"	void parent(int value) { /* Parent#parent(...) */}\n" +
		"	                ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=118217 - variation
public void test018() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterIncludeDocCommentReference, CompilerOptions.DISABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterWhenOverridingConcrete, CompilerOptions.DISABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterWhenImplementingAbstract, CompilerOptions.DISABLED);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X extends Parent implements Doable {\n" +
			"	/**\n" +
			"	 * @param value\n" +
			"	 */\n" +
			"	void foo(int value) { // X#foo(...)\n" +
			"	}\n" +
			"	void bar(int value) { // X#bar(...)\n" +
			"	}\n" +
			"\n" +
			"	void top(int value) { /* X#top(...)*/}\n" +
			"	void parent(int value) { /* X#parent(...) */}\n" +
			"	public void doit(int value) { /* X#doit(...) */}\n" +
			"}\n" +
			"abstract class Top {\n" +
			"	/**\n" +
			"	 * @param value\n" +
			"	 */\n" +
			"	abstract void top(int value); // Top#top(...)\n" +
			"}\n" +
			"abstract class Parent extends Top {\n" +
			"	/**\n" +
			"	 * @param value\n" +
			"	 */\n" +
			"	void parent(int value) { /* Parent#parent(...) */}\n" +
			"}\n" +
			"interface Doable {\n" +
			"	/**\n" +
			"	 * @param value\n" +
			"	 */\n" +
			"	void doit (int value); // Doable#doit(...)\n" +
			"}", // =================
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 5)\n" +
		"	void foo(int value) { // X#foo(...)\n" +
		"	             ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	void bar(int value) { // X#bar(...)\n" +
		"	             ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 24)\n" +
		"	void parent(int value) { /* Parent#parent(...) */}\n" +
		"	                ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=118217 - variation
public void test019() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterIncludeDocCommentReference, CompilerOptions.DISABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterWhenOverridingConcrete, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterWhenImplementingAbstract, CompilerOptions.ENABLED);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X extends Parent implements Doable {\n" +
			"	/**\n" +
			"	 * @param value\n" +
			"	 */\n" +
			"	void foo(int value) { // X#foo(...)\n" +
			"	}\n" +
			"	void bar(int value) { // X#bar(...)\n" +
			"	}\n" +
			"\n" +
			"	void top(int value) { /* X#top(...)*/}\n" +
			"	void parent(int value) { /* X#parent(...) */}\n" +
			"	public void doit(int value) { /* X#doit(...) */}\n" +
			"}\n" +
			"abstract class Top {\n" +
			"	/**\n" +
			"	 * @param value\n" +
			"	 */\n" +
			"	abstract void top(int value); // Top#top(...)\n" +
			"}\n" +
			"abstract class Parent extends Top {\n" +
			"	/**\n" +
			"	 * @param value\n" +
			"	 */\n" +
			"	void parent(int value) { /* Parent#parent(...) */}\n" +
			"}\n" +
			"interface Doable {\n" +
			"	/**\n" +
			"	 * @param value\n" +
			"	 */\n" +
			"	void doit (int value); // Doable#doit(...)\n" +
			"}", // =================
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 5)\n" +
		"	void foo(int value) { // X#foo(...)\n" +
		"	             ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	void bar(int value) { // X#bar(...)\n" +
		"	             ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 10)\n" +
		"	void top(int value) { /* X#top(...)*/}\n" +
		"	             ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 11)\n" +
		"	void parent(int value) { /* X#parent(...) */}\n" +
		"	                ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 12)\n" +
		"	public void doit(int value) { /* X#doit(...) */}\n" +
		"	                     ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 24)\n" +
		"	void parent(int value) { /* Parent#parent(...) */}\n" +
		"	                ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=118217 - variation
public void test020() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterIncludeDocCommentReference, CompilerOptions.DISABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterWhenOverridingConcrete, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedParameterWhenImplementingAbstract, CompilerOptions.ENABLED);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X extends Parent implements Doable {\n" +
			"	/** @param value */\n" +
			"	void foo(int value) { // X#foo(...)\n" +
			"	}\n" +
			"	void bar(int value) { // X#bar(...)\n" +
			"	}\n" +
			"\n" +
			"	/** @param value */\n" +
			"	void top(int value) { /* X#top(...)*/}\n" +
			"	/** @param value */\n" +
			"	void parent(int value) { /* X#parent(...) */}\n" +
			"	/** @param value */\n" +
			"	public void doit(int value) { /* X#doit(...) */}\n" +
			"}\n" +
			"abstract class Top {\n" +
			"	/** @param value */\n" +
			"	abstract void top(int value); // Top#top(...)\n" +
			"}\n" +
			"abstract class Parent extends Top {\n" +
			"	/** @param value */\n" +
			"	void parent(int value) { /* Parent#parent(...) */}\n" +
			"}\n" +
			"interface Doable {\n" +
			"	/** @param value */\n" +
			"	void doit (int value); // Doable#doit(...)\n" +
			"}", // =================
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 3)\n" +
		"	void foo(int value) { // X#foo(...)\n" +
		"	             ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	void bar(int value) { // X#bar(...)\n" +
		"	             ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 9)\n" +
		"	void top(int value) { /* X#top(...)*/}\n" +
		"	             ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 11)\n" +
		"	void parent(int value) { /* X#parent(...) */}\n" +
		"	                ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 13)\n" +
		"	public void doit(int value) { /* X#doit(...) */}\n" +
		"	                     ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 21)\n" +
		"	void parent(int value) { /* Parent#parent(...) */}\n" +
		"	                ^^^^^\n" +
		"The value of the parameter value is not used\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=412119, Optional warning for unused throwable variable in catch block
//No error message for exception parameter not being used.
public void test412119a() {
	runConformTest(new String[] {
			"p/X.java",
			"package p;\n" +
			"class X {\n" +
			"	\n" +
			"	void somethingDangerous() {}\n" +
			"	void foo() {\n" +
			"		try {\n" +
			"			somethingDangerous();\n" +
			"		} catch(Exception e) {\n" +
			"			throw new RuntimeException();\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		});
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=412119, Optional warning for unused throwable variable in catch block
//Error message for exception parameter not being used.
public void test412119b() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedExceptionParameter, CompilerOptions.ERROR);
	runner.testFiles =
			new String[] {
				"p/X.java",
				"package p;\n" +
				"class X {\n" +
				"	void somethingDangerous() {}\n" +
				"	void foo() {\n" +
				"		try {\n" +
				"			somethingDangerous();\n" +
				"		} catch(Exception e) {\n" +
				"				throw new RuntimeException();\n" +
				"		}\n" +
				"		try {\n" +
				"			somethingDangerous();\n" +

				// Exception thrown under a true boolean expression
				"		} catch(Exception e1) {\n" +
				"				if (true)\n" +
				"					throw new RuntimeException(e1);\n" +
				"		}\n" +

				// Catch clause parameter used.
				"		try {\n" +
				"			somethingDangerous();\n" +
				"		} catch(Exception e2) {\n" +
				"			throw new RuntimeException(e2);\n" +
				"		}\n" +
				"    }\n" +
				"}\n",
			};
	runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in p\\X.java (at line 7)\n" +
			"	} catch(Exception e) {\n" +
			"	                  ^\n" +
			"The value of the exception parameter e is not used\n" +
			"----------\n";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=412119, Optional warning for unused throwable variable in catch block
//Multi-catch parameters.
public void test412119c() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedExceptionParameter, CompilerOptions.ERROR);
	runner.testFiles =
			new String[] {
				"p/X.java",
				"package p;\n" +
				"class X {\n" +
				"class Z2 extends Exception {\n" +
				"	private static final long serialVersionUID = 1L;}\n" +
				"class Z1 extends Exception {\n" +
				"	private static final long serialVersionUID = 1L;}\n" +
				"	void somethingDangerous(int x, int y) throws Z1, Z2 {\n" +
				"		if (x < 1)\n" +
				"			throw new Z1();\n" +
				"		if (y > 1) \n" +
				"			throw new Z2();\n" +
				"	}\n" +
				"	void foo(int x, int y) {\n" +
				"		try {\n" +
				"			somethingDangerous(x, y);\n" +
				"		} catch(Z2|Z1 z) {\n" +
				"			throw new RuntimeException();\n" +
				"		}\n" +
				"		try {\n" +
				"			somethingDangerous(x, y);\n" +
				"		} catch(Z2|Z1 z2) {\n" +
				"			throw new RuntimeException(z2);\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
			};
	runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in p\\X.java (at line 16)\n" +
			"	} catch(Z2|Z1 z) {\n" +
			"	              ^\n" +
			"The value of the exception parameter z is not used\n" +
			"----------\n";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=412119, Optional warning for unused throwable variable in catch block
//Suppress Warnings.
public void test412119d() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportUnusedExceptionParameter, CompilerOptions.WARNING);
	this.runNegativeTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"class X {\n" +
				"	@SuppressWarnings(\"unused\")\n" +
				"	void foo(int x) {}\n" +
				"	void somethingDangerous() {}\n" +
				"	@SuppressWarnings(\"unused\")\n" +
				"	void foo3() {\n" +
				"		try {\n" +
				"			somethingDangerous();\n" +
				"		} catch(Exception e) {\n" +
				"			throw new RuntimeException();\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"" ,
			null,
			true,
			options);
}
public void testBug537033() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	runNegativeTest(
		new String[] {
			"ShowBug.java",
			"import java.util.concurrent.Callable;\n" +
			"\n" +
			"public class ShowBug {\n" +
			"    private static abstract class X {\n" +
			"        abstract void x(int val);\n" +
			"    }\n" +
			"\n" +
			"    public ShowBug() {\n" +
			"        final X x = new X() {\n" +
			"            void x(int val) {\n" +
			"                if (val > 0) {\n" +
			"                    // (1) The local variable x may not have been initialized\n" +
			"                    x.x(val - 1);\n" +
			"                }\n" +
			"            }\n" +
			"        };\n" +
			"\n" +
			"        new Callable<Void>() {\n" +
			"            public Void call() {\n" +
			"                // (2) Missing code implementation in the compiler\n" +
			"                x.x(10);          \n" +
			"                return null;\n" +
			"            }\n" +
			"        }.call();\n" +
			"    }\n" +
			"}\n"
		},
		this.complianceLevel < ClassFileConstants.JDK11
		?
		"----------\n" +
		"1. WARNING in ShowBug.java (at line 9)\n" +
		"	final X x = new X() {\n" +
		"	                ^^^\n" +
		"Access to enclosing constructor ShowBug.X() is emulated by a synthetic accessor method\n" +
		"----------\n" +
		"2. ERROR in ShowBug.java (at line 13)\n" +
		"	x.x(val - 1);\n" +
		"	^\n" +
		"The local variable x may not have been initialized\n" +
		"----------\n"
		:
		"----------\n" +
		"1. ERROR in ShowBug.java (at line 13)\n" +
		"	x.x(val - 1);\n" +
		"	^\n" +
		"The local variable x may not have been initialized\n" +
		"----------\n");
}
public static Class testClass() {
	return LocalVariableTest.class;
}
}
