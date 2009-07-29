/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class Deprecated15Test extends AbstractRegressionTest {
public Deprecated15Test(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_5);
}
public void test001() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.WARNING);
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"package p;\n" +
			"/**\n" + 
			" * @deprecated\n" + 
			" */\n" + 
			"public class X<T> {\n" + 
			"}\n",
			"Y.java",
			"import p.X;\n" +
			"public class Y {\n" + 
			"  Zork z;\n" +
			"  void foo() {\n" + 
			"    X x;\n" + 
			"    X[] xs = { x };\n" + 
			"  }\n" + 
			"  void bar() {\n" + 
			"    p.X x;\n" + 
			"    p.X[] xs = { x };\n" + 
			"  }\n" + 
			"}\n",
		}, 
		"----------\n" + 
		"1. WARNING in Y.java (at line 1)\n" + 
		"	import p.X;\n" + 
		"	       ^^^\n" + 
		"The type X<T> is deprecated\n" + 
		"----------\n" + 
		"2. ERROR in Y.java (at line 3)\n" + 
		"	Zork z;\n" + 
		"	^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n" + 
		"3. WARNING in Y.java (at line 5)\n" + 
		"	X x;\n" + 
		"	^\n" + 
		"The type X<T> is deprecated\n" + 
		"----------\n" + 
		"4. WARNING in Y.java (at line 5)\n" + 
		"	X x;\n" + 
		"	^\n" + 
		"X is a raw type. References to generic type X<T> should be parameterized\n" + 
		"----------\n" + 
		"5. WARNING in Y.java (at line 6)\n" + 
		"	X[] xs = { x };\n" + 
		"	^\n" + 
		"The type X<T> is deprecated\n" + 
		"----------\n" + 
		"6. WARNING in Y.java (at line 6)\n" + 
		"	X[] xs = { x };\n" + 
		"	^\n" + 
		"X is a raw type. References to generic type X<T> should be parameterized\n" + 
		"----------\n" + 
		"7. WARNING in Y.java (at line 9)\n" + 
		"	p.X x;\n" + 
		"	^^^\n" + 
		"The type X is deprecated\n" + 
		"----------\n" + 
		"8. WARNING in Y.java (at line 9)\n" + 
		"	p.X x;\n" + 
		"	^^^\n" + 
		"X is a raw type. References to generic type X<T> should be parameterized\n" + 
		"----------\n" + 
		"9. WARNING in Y.java (at line 10)\n" + 
		"	p.X[] xs = { x };\n" + 
		"	^^^\n" + 
		"The type X is deprecated\n" + 
		"----------\n" + 
		"10. WARNING in Y.java (at line 10)\n" + 
		"	p.X[] xs = { x };\n" + 
		"	^^^\n" + 
		"X is a raw type. References to generic type X<T> should be parameterized\n" + 
		"----------\n",
		null,
		true,
		options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159709
// guard variant for DeprecatedTest#test015 using an annotation 
public void test002() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	this.runNegativeTest(
		true,
		new String[] {
			"p/M1.java",
			"package p;\n" +
			"public class M1 {\n" +
			"  void bar() {\n" +
			"    a.N1.N2.N3 m = null;\n" +
			"    m.foo();\n" +
			"  }\n" + 
			"}\n",
			"a/N1.java",
			"package a;\n" +
			"public class N1 {\n" +
			"  @Deprecated\n" + 
			"  public class N2 {" +
			"    public void foo() {}" +
			"    public class N3 {" +
			"      public void foo() {}" +
			"    }" +
			"  }" +
			"}\n",
		}, 
		null, customOptions,
		"----------\n" + 
		"1. ERROR in p\\M1.java (at line 4)\n" + 
		"	a.N1.N2.N3 m = null;\n" + 
		"	^^^^^^^^^^\n" + 
		"The type N1.N2 is deprecated\n" + 
		"----------\n" + 
		"2. ERROR in p\\M1.java (at line 4)\n" + 
		"	a.N1.N2.N3 m = null;\n" + 
		"	^^^^^^^^^^\n" + 
		"The type N1.N2.N3 is deprecated\n" + 
		"----------\n" + 
		"3. ERROR in p\\M1.java (at line 5)\n" + 
		"	m.foo();\n" + 
		"	^^^^^^^\n" + 
		"The method foo() from the type N1.N2.N3 is deprecated\n" + 
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=161214
// shows that Member2 is properly tagged as deprecated (use the debugger, since
// we do not report deprecation in the unit where the deprecated type is 
// declared anyway)
public void test003() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" + 
			"  void foo() {\n" + 
			"    class Local {\n" + 
			"      class Member1 {\n" + 
			"        void bar() {\n" + 
			"          Member2 m2; // Member2 is deprecated\n" + 
			"        }\n" + 
			"      }\n" + 
			"      @Deprecated\n" + 
			"      class Member2 {\n" + 
			"      }\n" + 
			"    }\n" + 
			"  }\n" + 
			"}\n"
		}, 
		"",
		null,
		true,
		null,
		customOptions,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191909
public void test004() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	this.runNegativeTest(
		true,
		new String[] {
			"test1/E01.java",
			"package test1;\n" + 
			"public class E01 {\n" + 
			"	@Deprecated\n" + 
			"	public static int x = 5, y= 10;\n" + 
			"}",
			"test1/E02.java",
			"package test1;\n" + 
			"public class E02 {\n" + 
			"	public void foo() {\n" + 
			"		System.out.println(E01.x);\n" + 
			"		System.out.println(E01.y);\n" + 
			"	}\n" + 
			"}"
		}, 
		null, customOptions,
		"----------\n" + 
		"1. ERROR in test1\\E02.java (at line 4)\n" + 
		"	System.out.println(E01.x);\n" + 
		"	                       ^\n" + 
		"The field E01.x is deprecated\n" + 
		"----------\n" + 
		"2. ERROR in test1\\E02.java (at line 5)\n" + 
		"	System.out.println(E01.y);\n" + 
		"	                       ^\n" + 
		"The field E01.y is deprecated\n" + 
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
public static Class testClass() {
	return Deprecated15Test.class;
}
}
