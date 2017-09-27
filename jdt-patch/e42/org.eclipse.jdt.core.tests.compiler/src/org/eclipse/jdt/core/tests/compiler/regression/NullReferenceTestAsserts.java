/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
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
import java.io.IOException;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/* See also NullReferenceTests for general null reference tests */
public class NullReferenceTestAsserts extends AbstractRegressionTest {
	
// class libraries including org.eclipse.equinox.common
String[] assertLib = null;
public NullReferenceTestAsserts(String name) {
	super(name);
}

// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which does not belong to the class are skipped...
// Only the highest compliance level is run; add the VM argument
// -Dcompliance=1.4 (for example) to lower it if needed
static {
//		TESTS_NAMES = new String[] { "testBug373953" };
//		TESTS_NUMBERS = new int[] { 561 };
//		TESTS_RANGE = new int[] { 1, 2049 };
}

public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

public static Class testClass() {
	return NullReferenceTestAsserts.class;
}

protected void setUp() throws Exception {
	super.setUp();
	if (this.assertLib == null) {
		String[] defaultLibs = getDefaultClassPaths();
		int len = defaultLibs.length;
		this.assertLib = new String[len+1];
		System.arraycopy(defaultLibs, 0, this.assertLib, 0, len);
		File bundleFile = FileLocator.getBundleFile(Platform.getBundle("org.eclipse.equinox.common"));
		if (bundleFile.isDirectory())
			this.assertLib[len] = bundleFile.getPath()+"/bin";
		else
			this.assertLib[len] = bundleFile.getPath();
	}
}

// Conditionally augment problem detection settings
static boolean setNullRelatedOptions = true;
protected Map getCompilerOptions() {
    Map defaultOptions = super.getCompilerOptions();
    if (setNullRelatedOptions) {
	    defaultOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	    defaultOptions.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.ERROR);
	    defaultOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
		defaultOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
		defaultOptions.put(CompilerOptions.OPTION_IncludeNullInfoFromAsserts, CompilerOptions.ENABLED);
    }
    return defaultOptions;
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
public void testBug127575a() throws IOException {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o) {\n" +
				"    boolean b = o != null;\n" + // sheds doubts upon o
				"    org.eclipse.core.runtime.Assert.isLegal(o != null);\n" + 	// protection
				"    o.toString();\n" + 		// quiet
				"  }\n" +
				"}\n"},
			"",
			this.assertLib,
			false,
			null);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
public void testBug127575b() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o) {\n" +
				"    org.eclipse.core.runtime.Assert.isLegal(o == null);\n" + 	// forces null
				"    o.toString();\n" + 		// can only be null
				"  }\n" +
				"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
		this.assertLib,
		true);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
public void testBug127575c() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o, boolean b) {\n" +
				"    org.eclipse.core.runtime.Assert.isLegal(o != null || b, \"FAIL\");\n" + // shed doubts
				"    o.toString();\n" + 		// complain
				"  }\n" +
				"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    this.assertLib,
	    true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
public void testBug127575d() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o1, Object o2) {\n" +
				"    org.eclipse.core.runtime.Assert.isLegal(o1 != null && o2 == null);\n" +
				"    if (o1 == null) { };\n" + 		// complain
				"    if (o2 == null) { };\n" + 		// complain
				"  }\n" +
				"}\n"},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	if (o1 == null) { };\n" + 
			"	    ^^\n" + 
			"Null comparison always yields false: The variable o1 cannot be null at this location\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 4)\n" + 
			"	if (o1 == null) { };\n" + 
			"	                ^^^\n" + 
			"Dead code\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	if (o2 == null) { };\n" + 
			"	    ^^\n" + 
			"Redundant null check: The variable o2 can only be null at this location\n" + 
			"----------\n",
		    this.assertLib,
		    true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
public void testBug127575e() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o) {\n" +
				"    org.eclipse.core.runtime.Assert.isLegal(false && o != null);\n" +
				"    if (o == null) { };\n" + 		// quiet
				"  }\n" +
				"}\n"},
				"----------\n" + 
				"1. WARNING in X.java (at line 3)\n" + 
				"	org.eclipse.core.runtime.Assert.isLegal(false && o != null);\n" + 
				"	                                                 ^^^^^^^^^\n" + 
				"Dead code\n" + 
				"----------\n",
				this.assertLib, true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
public void testBug127575e_1() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o) {\n" +
				"	 o = null;\n" +
				"    org.eclipse.core.runtime.Assert.isLegal(false && o != null);\n" +
				"    if (o == null) { };\n" + 		// warn on o because o was null above.
				"  }\n" +
				"}\n"},
				"----------\n" + 
				"1. WARNING in X.java (at line 4)\n" + 
				"	org.eclipse.core.runtime.Assert.isLegal(false && o != null);\n" + 
				"	                                                 ^^^^^^^^^\n" + 
				"Dead code\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	if (o == null) { };\n" + 
				"	    ^\n" + 
				"Redundant null check: The variable o can only be null at this location\n" + 
				"----------\n",
				this.assertLib, true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
public void testBug127575e_2() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o) {\n" +
				"    org.eclipse.core.runtime.Assert.isLegal(true || o != null);\n" +
				"    if (o == null) { };\n" + 		// quiet
				"  }\n" +
				"}\n"},
				"----------\n" + 
				"1. WARNING in X.java (at line 3)\n" + 
				"	org.eclipse.core.runtime.Assert.isLegal(true || o != null);\n" + 
				"	                                                ^^^^^^^^^\n" + 
				"Dead code\n" + 
				"----------\n",
				this.assertLib, true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
public void testBug127575f() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o) {\n" +
				"    org.eclipse.core.runtime.Assert.isLegal(false || o != null);\n" +
				"    if (o == null) { };\n" + 		// complain
				"  }\n" +
				"}\n"},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	if (o == null) { };\n" + 
		"	    ^\n" + 
		"Null comparison always yields false: The variable o cannot be null at this location\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 4)\n" + 
		"	if (o == null) { };\n" + 
		"	               ^^^\n" + 
		"Dead code\n" + 
		"----------\n",
	    this.assertLib, true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// do warn always false comparisons even inside org.eclipse.core.runtime.Assert.isLegal
public void testBug127575g() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    Object o = null;\n" +
				"    org.eclipse.core.runtime.Assert.isLegal(o != null);\n" +    // don't complain
				"    if (o == null) { };\n" +   // complain
				"  }\n" +
				"}\n"},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	org.eclipse.core.runtime.Assert.isLegal(o != null);\n" + 
		"	                                        ^\n" + 
		"Null comparison always yields false: The variable o can only be null at this location\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 5)\n" + 
		"	if (o == null) { };\n" + 
		"	    ^\n" + 
		"Null comparison always yields false: The variable o cannot be null at this location\n" + 
		"----------\n" + 
		"3. WARNING in X.java (at line 5)\n" + 
		"	if (o == null) { };\n" + 
		"	               ^^^\n" + 
		"Dead code\n" + 
		"----------\n",
		this.assertLib, true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// Test to verify that asserts are exempted from redundant null check warnings,
// but this doesn't affect the downstream info.
public void testBug127575h() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void m() {\n" +
				"    X foo = new X();\n" +
				"	 org.eclipse.core.runtime.Assert.isLegal (foo != null);\n" +	// redundant check - don't warn
				"	 if (foo == null) {}\n" +
				"    X foo2 = new X();\n" +
				"	 org.eclipse.core.runtime.Assert.isLegal (foo2 == null);\n" +	// always false check - warn
				"	 if (foo2 == null) {}\n" +
				"    X bar = null;\n" +
				"	 org.eclipse.core.runtime.Assert.isLegal (bar == null);\n" +	// redundant check - don't warn
				"	 if (bar == null) {}\n" +
				"    X bar2 = null;\n" +
				"	 org.eclipse.core.runtime.Assert.isLegal (bar2 != null);\n" +	// always false check - warn
				"	 if (bar2 == null) {}\n" +
				"  }\n" +
				"}\n"},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	if (foo == null) {}\n" + 
		"	    ^^^\n" + 
		"Null comparison always yields false: The variable foo cannot be null at this location\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 5)\n" + 
		"	if (foo == null) {}\n" + 
		"	                 ^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 7)\n" + 
		"	org.eclipse.core.runtime.Assert.isLegal (foo2 == null);\n" + 
		"	                                         ^^^^\n" + 
		"Null comparison always yields false: The variable foo2 cannot be null at this location\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 8)\n" + 
		"	if (foo2 == null) {}\n" + 
		"	    ^^^^\n" + 
		"Redundant null check: The variable foo2 can only be null at this location\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 11)\n" + 
		"	if (bar == null) {}\n" + 
		"	    ^^^\n" + 
		"Redundant null check: The variable bar can only be null at this location\n" + 
		"----------\n" + 
		"6. ERROR in X.java (at line 13)\n" + 
		"	org.eclipse.core.runtime.Assert.isLegal (bar2 != null);\n" + 
		"	                                         ^^^^\n" + 
		"Null comparison always yields false: The variable bar2 can only be null at this location\n" + 
		"----------\n" + 
		"7. ERROR in X.java (at line 14)\n" + 
		"	if (bar2 == null) {}\n" + 
		"	    ^^^^\n" + 
		"Null comparison always yields false: The variable bar2 cannot be null at this location\n" + 
		"----------\n" + 
		"8. WARNING in X.java (at line 14)\n" + 
		"	if (bar2 == null) {}\n" + 
		"	                  ^^\n" + 
		"Dead code\n" + 
		"----------\n",
	    this.assertLib, true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// Test to verify that asserts are exempted from redundant null check warnings,
// but this doesn't affect the downstream info.
public void testBug127575i() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void m() {\n" +
				"	 X bar = null;\n" +
				"    X foo = getX();\n" +
				"    if (foo == null) {\n" +
				"	 	foo = new X();\n" +
				"	 }\n" +
				"	 org.eclipse.core.runtime.Assert.isTrue (foo != null && bar == null);\n" +	// don't warn but do the null analysis
				"	 if (foo != null) {}\n" +		// warn
				"	 if (bar == null) {}\n" +		// warn
				"  }\n" +
				"  public X getX() { return new X();}\n" +
				"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" + 
		"	if (foo != null) {}\n" + 
		"	    ^^^\n" + 
		"Redundant null check: The variable foo cannot be null at this location\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 10)\n" + 
		"	if (bar == null) {}\n" + 
		"	    ^^^\n" + 
		"Redundant null check: The variable bar can only be null at this location\n" + 
		"----------\n",
	    this.assertLib, true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// Test to verify that asserts are exempted from redundant null check warnings in a looping context,
// but this doesn't affect the downstream info.
public void testBug127575j() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void m() {\n" +
				"    X foo = new X();\n" +
				"    X foo2 = new X();\n" +
				"    X bar = null;\n" +
				"    X bar2 = null;\n" +
				"	 while (true) {\n" +
				"	 	org.eclipse.core.runtime.Assert.isLegal (foo != null);\n" +	//don't warn
				"	 	if (foo == null) {}\n" +
				"	 	org.eclipse.core.runtime.Assert.isLegal (foo2 == null);\n" +	//don't warn
				"	 	if (foo2 == null) {}\n" +				
				"	 	org.eclipse.core.runtime.Assert.isLegal (bar == null);\n" +	//don't warn
				"	 	if (bar == null) {}\n" +				
				"	 	org.eclipse.core.runtime.Assert.isLegal (bar2 != null);\n" +	//don't warn
				"	 	if (bar2 == null) {}\n" +
				"	 }\n" +
				"  }\n" +
				"}\n"},
		"----------\n" + 
		"1. ERROR in X.java (at line 9)\n" + 
		"	if (foo == null) {}\n" + 
		"	    ^^^\n" + 
		"Null comparison always yields false: The variable foo cannot be null at this location\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 9)\n" + 
		"	if (foo == null) {}\n" + 
		"	                 ^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 10)\n" + 
		"	org.eclipse.core.runtime.Assert.isLegal (foo2 == null);\n" + 
		"	                                         ^^^^\n" + 
		"Null comparison always yields false: The variable foo2 cannot be null at this location\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 11)\n" + 
		"	if (foo2 == null) {}\n" + 
		"	    ^^^^\n" + 
		"Redundant null check: The variable foo2 can only be null at this location\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 13)\n" + 
		"	if (bar == null) {}\n" + 
		"	    ^^^\n" + 
		"Redundant null check: The variable bar can only be null at this location\n" + 
		"----------\n" + 
		"6. ERROR in X.java (at line 14)\n" + 
		"	org.eclipse.core.runtime.Assert.isLegal (bar2 != null);\n" + 
		"	                                         ^^^^\n" + 
		"Null comparison always yields false: The variable bar2 can only be null at this location\n" + 
		"----------\n" + 
		"7. ERROR in X.java (at line 15)\n" + 
		"	if (bar2 == null) {}\n" + 
		"	    ^^^^\n" + 
		"Null comparison always yields false: The variable bar2 cannot be null at this location\n" + 
		"----------\n" + 
		"8. WARNING in X.java (at line 15)\n" + 
		"	if (bar2 == null) {}\n" + 
		"	                  ^^\n" + 
		"Dead code\n" + 
		"----------\n",
	    this.assertLib, true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// Test to verify that asserts are exempted from redundant null check warnings in a finally context,
// but this doesn't affect the downstream info.
public void testBug127575k() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void m() {\n" +
				"    X foo = new X();\n" +
				"    X foo2 = new X();\n" +
				"    X bar = null;\n" +
				"    X bar2 = null;\n" +
				"	 try {\n" +
				"		System.out.println(\"Inside try\");\n" +
				"	 }\n" +
				"	 finally {\n" +
				"	 	org.eclipse.core.runtime.Assert.isLegal (foo != null);\n" +	//don't warn
				"	 	if (foo == null) {}\n" +
				"	 	org.eclipse.core.runtime.Assert.isLegal (foo2 == null);\n" +	//don't warn
				"	 	if (foo2 == null) {}\n" +				
				"	 	org.eclipse.core.runtime.Assert.isLegal (bar == null);\n" +	//don't warn
				"	 	if (bar == null) {}\n" +				
				"	 	org.eclipse.core.runtime.Assert.isLegal (bar2 != null);\n" +	//don't warn
				"	 	if (bar2 == null) {}\n" +
				"	 }\n" +
				"  }\n" +
				"}\n"},
		"----------\n" + 
		"1. ERROR in X.java (at line 12)\n" + 
		"	if (foo == null) {}\n" + 
		"	    ^^^\n" + 
		"Null comparison always yields false: The variable foo cannot be null at this location\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 12)\n" + 
		"	if (foo == null) {}\n" + 
		"	                 ^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 13)\n" + 
		"	org.eclipse.core.runtime.Assert.isLegal (foo2 == null);\n" + 
		"	                                         ^^^^\n" + 
		"Null comparison always yields false: The variable foo2 cannot be null at this location\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 14)\n" + 
		"	if (foo2 == null) {}\n" + 
		"	    ^^^^\n" + 
		"Redundant null check: The variable foo2 can only be null at this location\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 16)\n" + 
		"	if (bar == null) {}\n" + 
		"	    ^^^\n" + 
		"Redundant null check: The variable bar can only be null at this location\n" + 
		"----------\n" + 
		"6. ERROR in X.java (at line 17)\n" + 
		"	org.eclipse.core.runtime.Assert.isLegal (bar2 != null);\n" + 
		"	                                         ^^^^\n" + 
		"Null comparison always yields false: The variable bar2 can only be null at this location\n" + 
		"----------\n" + 
		"7. ERROR in X.java (at line 18)\n" + 
		"	if (bar2 == null) {}\n" + 
		"	    ^^^^\n" + 
		"Null comparison always yields false: The variable bar2 cannot be null at this location\n" + 
		"----------\n" + 
		"8. WARNING in X.java (at line 18)\n" + 
		"	if (bar2 == null) {}\n" + 
		"	                  ^^\n" + 
		"Dead code\n" + 
		"----------\n",
	    this.assertLib, true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// The condition of org.eclipse.core.runtime.Assert.isLegal is considered always true
// and alters the following analysis suitably.
public void testBug127575l() {
		this.runNegativeTest(
			new String[] {
				"Test.java",
				"public class Test {\n" +
				"	void foo(Object a, Object b, Object c) {\n" +
				"		org.eclipse.core.runtime.Assert.isLegal( a == null);\n " +
				"		if (a!=null) {\n" +
				"			System.out.println(\"a is not null\");\n" +
				"		 } else{\n" +
				"			System.out.println(\"a is null\");\n" +
				"		 }\n" +
				"		a = null;\n" +
				"		if (a== null) {}\n" +
				"		org.eclipse.core.runtime.Assert.isLegal(b != null);\n " +
				"		if (b!=null) {\n" +
				"			System.out.println(\"b is not null\");\n" +
				"		 } else{\n" +
				"			System.out.println(\"b is null\");\n" +
				"		 }\n" +
				"		org.eclipse.core.runtime.Assert.isLegal(c == null);\n" +
				"		if (c.equals(a)) {\n" +
				"			System.out.println(\"\");\n" +
				"		 } else{\n" +
				"			System.out.println(\"\");\n" +
				"		 }\n" +
				"	}\n" +
				"	public static void main(String[] args){\n" +
				"		Test test = new Test();\n" +
				"		test.foo(null,null, null);\n" +
				"	}\n" +
				"}\n"},
			"----------\n" + 
			"1. ERROR in Test.java (at line 4)\n" + 
			"	if (a!=null) {\n" + 
			"	    ^\n" + 
			"Null comparison always yields false: The variable a can only be null at this location\n" + 
			"----------\n" + 
			"2. WARNING in Test.java (at line 4)\n" + 
			"	if (a!=null) {\n" + 
			"			System.out.println(\"a is not null\");\n" + 
			"		 } else{\n" + 
			"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Dead code\n" + 
			"----------\n" + 
			"3. ERROR in Test.java (at line 9)\n" + 
			"	a = null;\n" + 
			"	^\n" + 
			"Redundant assignment: The variable a can only be null at this location\n" + 
			"----------\n" + 
			"4. ERROR in Test.java (at line 10)\n" + 
			"	if (a== null) {}\n" + 
			"	    ^\n" + 
			"Redundant null check: The variable a can only be null at this location\n" + 
			"----------\n" + 
			"5. ERROR in Test.java (at line 12)\n" + 
			"	if (b!=null) {\n" + 
			"	    ^\n" + 
			"Redundant null check: The variable b cannot be null at this location\n" + 
			"----------\n" + 
			"6. WARNING in Test.java (at line 14)\n" + 
			"	} else{\n" + 
			"			System.out.println(\"b is null\");\n" + 
			"		 }\n" + 
			"	      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Dead code\n" + 
			"----------\n" + 
			"7. ERROR in Test.java (at line 18)\n" + 
			"	if (c.equals(a)) {\n" + 
			"	    ^\n" + 
			"Null pointer access: The variable c can only be null at this location\n" + 
			"----------\n",
			this.assertLib, true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// NPE warnings should be given inside org.eclipse.core.runtime.Assert.isLegal too
public void testBug127575m() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"Info.java",
				"public class Info {\n" +
				"	public void test(Info[] infos) {\n" +
				"		for (final Info info : infos) {\n " +
				"			if (info != null) {\n" +
				"				org.eclipse.core.runtime.Assert.isLegal( info.checkSomething());\n" +
				"		 		info.doSomething();\n" +	
				"			}\n" +
				"		 }\n" +
				"		for (final Info info : infos) {\n " +
				"			if (info == null) {\n" +
				"				org.eclipse.core.runtime.Assert.isLegal(info.checkSomething());\n" + // warn NPE
				"		 		info.doSomething();\n" +	
				"			}\n" +
				"		 }\n" +
				"	}\n" +
				"	void doSomething()  {}\n" +
				"	boolean checkSomething() {return true;}\n" +
				"}\n"},
				"----------\n" + 
			"1. ERROR in Info.java (at line 11)\n" + 
			"	org.eclipse.core.runtime.Assert.isLegal(info.checkSomething());\n" + 
			"	                                        ^^^^\n" + 
			"Null pointer access: The variable info can only be null at this location\n" + 
			"----------\n",
			this.assertLib,
			true);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// always false comparison in Assert.isLegal in loop should be warned against
public void testBug127575n() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
		"DoWhileBug.java",
				"public class DoWhileBug {\n" + 
				"	void test(boolean b1) {\n" + 
				"		Object o1 = null;\n" + 
				"		Object o2 = null;\n" + 
				"		do {\n" +
				"           if (b1)\n" + 
				"				o1 = null;\n" +
				"           org.eclipse.core.runtime.Assert.isLegal ((o2 = o1) != null);\n" +
				"		} while (true);\n" + 
				"	}\n" + 
				"}"	
			},
			"----------\n" + 
			"1. ERROR in DoWhileBug.java (at line 7)\n" + 
			"	o1 = null;\n" + 
			"	^^\n" + 
			"Redundant assignment: The variable o1 can only be null at this location\n" + 
			"----------\n" + 
			"2. ERROR in DoWhileBug.java (at line 8)\n" + 
			"	org.eclipse.core.runtime.Assert.isLegal ((o2 = o1) != null);\n" + 
			"	                                         ^^^^^^^^^\n" + 
			"Null comparison always yields false: The variable o2 can only be null at this location\n" + 
			"----------\n",
			this.assertLib,
			true);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// "redundant null check" in Assert.isLegal in loop should not be warned against
public void testBug127575o() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
		"DoWhileBug.java",
				"public class DoWhileBug {\n" + 
				"	void test(boolean b1) {\n" + 
				"		Object o1 = null;\n" + 
				"		Object o2 = null;\n" + 
				"		do {\n" +
				"           if (b1)\n" + 
				"				o1 = null;\n" +
				"           org.eclipse.core.runtime.Assert.isLegal ((o2 = o1) == null);\n" +
				"		} while (true);\n" + 
				"	}\n" + 
				"}"	
			},
			"----------\n" + 
			"1. ERROR in DoWhileBug.java (at line 7)\n" + 
			"	o1 = null;\n" + 
			"	^^\n" + 
			"Redundant assignment: The variable o1 can only be null at this location\n" + 
			"----------\n",
			this.assertLib,
			true);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=373953
public void testBug373953() throws IOException {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o) {\n" +
				"    boolean b = o != null;\n" + // sheds doubts upon o
				"    java.eclipse.core.runtime.Assert.isLegal(o != null);\n" + 	// bogus Assert
				"    o.toString();\n" + 		// warn
				"  }\n" +
				"  void foo1(Object o) {\n" +
				"    boolean b = o != null;\n" + // sheds doubts upon o
				"    org.lang.core.runtime.Assert.isLegal(o != null);\n" + 	// bogus Assert
				"    o.toString();\n" + 		// warn
				"  }\n" +
				"}\n",
				"java.eclipse.core.runtime/Assert.java",
				"package java.eclipse.core.runtime;\n" +
				"public class Assert {\n" +
				"  public static void isLegal(boolean b) {\n" +
				"  }\n" +
				"}\n",
				"org.lang.core.runtime/Assert.java",
				"package org.lang.core.runtime;\n" +
				"public class Assert {\n" +
				"  public static void isLegal(boolean b) {\n" +
				"  }\n" +
				"}\n"},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	o.toString();\n" + 
			"	^\n" + 
			"Potential null pointer access: The variable o may be null at this location\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 10)\n" + 
			"	o.toString();\n" + 
			"	^\n" + 
			"Potential null pointer access: The variable o may be null at this location\n" + 
			"----------\n");
}
}
