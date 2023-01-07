/*******************************************************************************
 * Copyright (c) 2005, 2019 IBM Corporation and others.
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
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contributions for
 *     						bug 325755 - [compiler] wrong initialization state after conditional expression
 *     						bug 133125 - [compiler][null] need to report the null status of expressions and analyze them simultaneously
 *     						bug 292478 - Report potentially null across variable assignment
 *     						bug 319201 - [null] no warning when unboxing SingleNameReference causes NPE
 *     						bug 320170 - [compiler] [null] Whitebox issues in null analysis
 *     						bug 332637 - Dead Code detection removing code that isn't dead
 *     						bug 338303 - Warning about Redundant assignment conflicts with definite assignment
 *     						bug 336428 - [compiler][null] bogus warning "redundant null check" in condition of do {} while() loop
 * 							bug 324178 - [null] ConditionalExpression.nullStatus(..) doesn't take into account the analysis of condition itself
 * 							bug 354554 - [null] conditional with redundant condition yields weak error message
 * 							bug 358827 - [1.7] exception analysis for t-w-r spoils null analysis
 * 							bug 349326 - [1.7] new warning for missing try-with-resources
 * 							bug 360328 - [compiler][null] detect null problems in nested code (local class inside a loop)
 * 							bug 367879 - Incorrect "Potential null pointer access" warning on statement after try-with-resources within try-finally
 * 							bug 383690 - [compiler] location of error re uninitialized final field should be aligned
 *							bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
 *							bug 376263 - Bogus "Potential null pointer access" warning
 *							bug 331649 - [compiler][null] consider null annotations for fields
 *							bug 382789 - [compiler][null] warn when syntactically-nonnull expression is compared against null
 *							bug 401088 - [compiler][null] Wrong warning "Redundant null check" inside nested try statement
 *							bug 401092 - [compiler][null] Wrong warning "Redundant null check" in outer catch of nested try
 *							bug 400761 - [compiler][null] null may be return as boolean without a diagnostic
 *							bug 402993 - [null] Follow up of bug 401088: Missing warning about redundant null check
 *							bug 403147 - [compiler][null] FUP of bug 400761: consolidate interaction between unboxing, NPE, and deferred checking
 *							bug 384380 - False positive on a "Potential null pointer access" after a continue
 *							bug 406384 - Internal error with I20130413
 *							Bug 364326 - [compiler][null] NullPointerException is not found by compiler. FindBugs finds that one
 *							Bug 453483 - [compiler][null][loop] Improve null analysis for loops
 *							Bug 195638 - [compiler][null][refactoring] Wrong error : "Null pointer access: The variable xxx can only be null at this location " with try..catch in loop
 *							Bug 454031 - [compiler][null][loop] bug in null analysis; wrong "dead code" detection
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/* See also NullReferenceImplTests for low level, implementation dependent
 * tests. */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class NullReferenceTest extends AbstractRegressionTest {

public NullReferenceTest(String name) {
	super(name);
}

	// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which does not belong to the class are skipped...
// Only the highest compliance level is run; add the VM argument
// -Dcompliance=1.4 (for example) to lower it if needed
static {
//		TESTS_NAMES = new String[] { "testBug542707_1" };
//		TESTS_NAMES = new String[] { "testBug384380" };
//		TESTS_NAMES = new String[] { "testBug384380_b" };
//		TESTS_NAMES = new String[] { "testBug321926a2" };
//		TESTS_NAMES = new String[] { "testBug432109" };
//		TESTS_NAMES = new String[] { "testBug418500" };
//		TESTS_NUMBERS = new int[] { 561 };
//		TESTS_RANGE = new int[] { 1, 2049 };
}

public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

public static Class testClass() {
	return NullReferenceTest.class;
}

// Conditionally augment problem detection settings
static boolean setNullRelatedOptions = true;
@Override
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

protected void runNegativeNullTest(String[] testFiles, String expectedCompilerLog) {
	runNegativeTest(testFiles, expectedCompilerLog, JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- simple case for local
public void test0001_simple_local() {
	runNegativeTest(
		new String[] {
			"X.java",
			  "public class X {\n" +
			  "  void foo() {\n" +
			  "    Object o = null;\n" +
			  "    o.toString();\n" +
			  "  }\n" +
			  "}\n"},
	    "----------\n" +
	    "1. ERROR in X.java (at line 4)\n" +
	    "	o.toString();\n" +
	    "	^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
	    "----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- simple case for field
// the current design leaves fields out of the analysis altogether
public void test0002_simple_field() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object o;\n" +
			"  void foo() {\n" +
			"    o = null;\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
	""
//      "----------\n" +
//      "1. ERROR in X.java (at line 5)\n" +
//      "	o.toString();\n" +
//      "	^\n" +
//      "The field o is likely null; it was either set to null or checked for null when last used\n" +
//      "----------\n"
	);
}

// null analysis -- simple case for parameter
public void test0003_simple_parameter() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    o = null;\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- final local
public void test0004_final_local() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    final Object o = null;\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- final local
public void test0005_final_local() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    final Object o;\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"The local variable o may not have been initialized\n" +
			// hides the null related message, but complains once, which is good
		"----------\n");
}

// null analysis -- final local
public void test0006_final_local() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    final Object o = null;\n" +
			"    if (o != null) { /* */ }\n" + // complain
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	if (o != null) { /* */ }\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o can only be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	if (o != null) { /* */ }\n" +
		"	               ^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- local with member
public void test0007_local_with_member() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object m;\n" +
			"  void foo() {\n" +
			"    X x = null;\n" +
			"    x.m.toString();\n" + // complain
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	x.m.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable x can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- local with member
public void test0008_local_with_member() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object m;\n" +
			"  void foo() {\n" +
			"    X x = null;\n" +
			"    System.out.println(x.m);\n" + // complain
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	System.out.println(x.m);\n" +
		"	                   ^\n" +
		"Null pointer access: The variable x can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- local with member
public void test0009_local_with_member() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object m;\n" +
			"  void foo(X x) {\n" +
			"    x.m.toString();\n" + // quiet
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- field
public void test0010_field_with_method_call() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object o;\n" +
			"  void foo() {\n" +
			"    o = null;\n" +
			"    bar();\n" + // defuses null by side effect
			"    o.toString();\n" +
			"  }\n" +
			"  void bar() {\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- field
public void test0011_field_with_method_call() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  static Object o;\n" +
			"  void foo() {\n" +
			"    o = null;\n" +
			"    bar();\n" + // defuses null by side effect
			"    o.toString();\n" +
			"  }\n" +
			"  static void bar() {\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- field
public void test0012_field_with_method_call() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object o;\n" +
			"  void foo() {\n" +
			"    o = null;\n" +
			"    bar();\n" +
			"    o.toString();\n" +
			"  }\n" +
			"  static void bar() {\n" +
			"  }\n" +
			"}\n"},
		"" // still ok because the class may hold a pointer to this
	);
}

// null analysis -- field
public void test0013_field_with_method_call() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  static Object o;\n" +
			"  void foo() {\n" +
			"    o = null;\n" +
			"    bar();\n" +
			"    o.toString();\n" +
			"  }\n" +
			"  void bar() {\n" +
			"  }\n" +
			"}\n"},
		"" // still ok because this may place a static call upon X
	);
}

// null analysis -- field
public void test0014_field_with_explicit_this_access() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object o;\n" +
			"  void foo() {\n" +
			"    o = null;\n" +
			"    this.o.toString();\n" +
			"  }\n" +
			"}\n"},
		""
//      "----------\n" +
//      "1. ERROR in X.java (at line 5)\n" +
//      "	this.o.toString();\n" +
//      "	^^^^^^\n" +
//      "The field o is likely null; it was either set to null or checked for null when last used\n" +
//      "----------\n"
	);
}

// null analysis -- field
public void test0015_field_with_explicit_this_access() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object o;\n" +
			"  void foo() {\n" +
			"    this.o = null;\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
		""
//      "----------\n" +
//      "1. ERROR in X.java (at line 5)\n" +
//      "	o.toString();\n" +
//      "	^\n" +
//      "The field o is likely null; it was either set to null or checked for null when last used\n" +
//      "----------\n"
	);
}

// null analysis -- field
public void test0016_field_of_another_object() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object o;\n" +
			"  void foo() {\n" +
			"    X other = new X();\n" +
			"    other.o = null;\n" +
			"    other.o.toString();\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- field
public void test0017_field_of_another_object() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object o;\n" +
			"  void foo() {\n" +
			"    X other = this;\n" +
			"    o = null;\n" +
			"    other.o.toString();\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- field
public void test0018_field_of_enclosing_object() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object o;\n" +
			"  public class Y {\n" +
			"    void foo() {\n" +
			"      X.this.o = null;\n" +
			"      X.this.o.toString();\n" + // complain
			"    }\n" +
			"  }\n" +
			"}\n"},
		""
//      "----------\n" +
//      "1. ERROR in X.java (at line 6)\n" +
//      "	X.this.o.toString();\n" +
//      "	^^^^^^^^\n" +
//      "The field o is likely null; it was either set to null or checked for null when last used\n" +
//      "----------\n"
	);
}

// null analysis -- fields
// check that fields that are protected against concurrent access
// behave as locals when no call to further methods can affect them
public void test0019_field_synchronized() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object o;\n" +
			"  public synchronized void foo() {\n" +
			"    o = null;\n" +
			"    o.toString();\n" +
			"  }\n" +
			"  void bar() {/* */}\n" +
			"}\n"},
		""
//      "----------\n" +
//      "1. ERROR in X.java (at line 5)\n" +
//      "	o.toString();\n" +
//      "	^\n" +
//      "The field o is likely null; it was either set to null or checked for null when last used\n" +
//      "----------\n"
	);
}

// null analysis -- field
// check that final fields behave as locals despite calls to further
// methods
public void test0020_final_field() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  final Object o = null;\n" +
			"  public synchronized void foo() {\n" +
			"    bar();\n" +
			"    o.toString();\n" +
			"  }\n" +
			"  void bar() {/* */}\n" +
			"}\n"},
		""
//      "----------\n" +
//      "1. ERROR in X.java (at line 5)\n" +
//      "	o.toString();\n" +
//      "	^\n" +
//      "The field o is likely null; it was either set to null or checked for null when last used\n" +
//      "----------\n"
	);
}

// null analysis -- field
public void test0021_final_field() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  final Object o = null;\n" +
			"  X () {\n" +
			"    bar();\n" +
			"    o.toString();\n" +
			"  }\n" +
			"  void bar() {/* */}\n" +
			"}\n"},
		""
//      "----------\n" +
//      "1. ERROR in X.java (at line 5)\n" +
//      "	o.toString();\n" +
//      "	^\n" +
//      "The field o is likely null; it was either set to null or checked for null when last used\n" +
//      "----------\n"
	);
}

// null analysis -- field
public void test0022_final_field() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  final Object o = new Object();\n" +
			"  X () {\n" +
			"    bar();\n" +
			"    if (o == null) { /* empty */ }\n" +
			"  }\n" +
			"  void bar() {/* */}\n" +
			"}\n"},
		""
//      "----------\n" +
//      "1. ERROR in X.java (at line 5)\n" +
//      "	if (o == null) { /* empty */ }\n" +
//      "	    ^\n" +
//      "The field o is likely non null; it was either set to a non-null value or assumed to be non-null when last used\n" +
//      "----------\n"
	);
}

// null analysis -- field
public void test0023_field_assignment() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object m;\n" +
			"  void foo(X x) {\n" +
			"    Object o = x.m;\n" +
			"    if (o == null) { /* */ };\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- field
public void test0024_field_cast_assignment() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object m;\n" +
			"  void foo(Object x) {\n" +
			"    Object o = ((X) x).m;\n" +
			"    if (o == null) { /* */ };\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- parameter
public void test0025_parameter() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    o.toString();\n" + // quiet: parameters have unknown value
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- suppress warnings
public void test0026_suppress_warnings() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.WARNING);
		this.runConformTest(
			new String[] {
				"X.java",
				"@SuppressWarnings(\"null\")\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"    Object o = null;\n" +
				"    o.toString();\n" +
				"  }\n" +
				"}\n"},
		    "", null, true, null, compilerOptions, null);
	}
}

// null analysis -- embedded comparison
public void test0027_embedded_comparison() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    boolean b = o != null;\n" + // shades doubts upon o
			"    if (b) { /* */ }\n" +
			"    o.toString();\n" + 		// complain
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- field
public void test0028_field_as_initializer() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  X f;\n" +
			"  void foo() {\n" +
			"    X x = f;\n" +
			"    if (x == null) { /* */ }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- field
public void test0029_field_assignment() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object m;\n" +
			"  void foo() {\n" +
			"    X x = null;\n" +
			"    x.m = new Object();\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	x.m = new Object();\n" +
		"	^\n" +
		"Null pointer access: The variable x can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- conditional expression
public void test0030_conditional_expression() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = true ? null : null;\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	Object o = true ? null : null;\n" +
			"	                         ^^^^\n" +
			"Dead code\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	o.toString();\n" +
			"	^\n" +
			"Null pointer access: The variable o can only be null at this location\n" +
			"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- conditional expression
public void test0031_conditional_expression() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = true ? null : new Object();\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	Object o = true ? null : new Object();\n" +
			"	                         ^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	o.toString();\n" +
			"	^\n" +
			"Null pointer access: The variable o can only be null at this location\n" +
			"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- conditional expression
public void test0032_conditional_expression() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = false ? null : new Object();\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- conditional expression
public void test0033_conditional_expression() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = (1 == 1) ? null : new Object();\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	Object o = (1 == 1) ? null : new Object();\n" +
			"	           ^^^^^^^^\n" +
			"Comparing identical expressions\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 3)\n" +
			"	Object o = (1 == 1) ? null : new Object();\n" +
			"	                             ^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 4)\n" +
			"	o.toString();\n" +
			"	^\n" +
			"Null pointer access: The variable o can only be null at this location\n" +
			"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- conditional expression
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=133125
public void test0034_conditional_expression() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean b;\n" +
			"  void foo() {\n" +
			"    Object o = b ? null : new Object();\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	o.toString();\n" +
			"	^\n" +
			"Potential null pointer access: The variable o may be null at this location\n" +
			"----------\n");
}

// null analysis -- conditional expression
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=133125
// variant with constant condition
public void test0034_conditional_expression_2() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean b;\n" +
			"  void foo() {\n" +
			"    Object o = false ? null : new Object();\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
			"");
}

// null analysis -- conditional expression
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=133125
public void test0034_conditional_expression_3() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean b;\n" +
			"  void foo(Object a) {\n" +
			" 	 if (a == null) {}\n" +
			"    Object o = b ? a : new Object();\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	o.toString();\n" +
			"	^\n" +
			"Potential null pointer access: The variable o may be null at this location\n" +
			"----------\n");
}

// null analysis -- conditional expression
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=133125
// variant with dependency between condition and expression - LocalDeclaration
// TODO(stephan) cannot analyse this flow dependency
public void _test0034_conditional_expression_4() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean b;\n" +
			"  void foo(Object u) {\n" +
			"    if (u == null) {}\n" + //taint
			"    Object o = (u == null) ? new Object() : u;\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
			"");
}

// null analysis -- conditional expression
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=133125
// variant with dependency between condition and expression - Assignment
// TODO(stephan) cannot analyse this flow dependency
public void _test0034_conditional_expression_5() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean b;\n" +
			"  void foo(Object u) {\n" +
			"    if (u == null) {}\n" + //taint
			"    Object o;\n" +
			"    o = (u == null) ? new Object() : u;\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
			"");
}

// null analysis -- conditional expression
public void test0035_conditional_expression() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean b;\n" +
			"  void foo() {\n" +
			"    Object o = b ? null : new Object();\n" +
			"    if (o == null) { /* */ }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- conditional expression
public void test0036_conditional_expression() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean b;\n" +
			"  void foo() {\n" +
			"    Object o = b ? null : null;\n" +
			"    if (o == null) { /* */ }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	if (o == null) { /* */ }\n" +
		"	    ^\n" +
		"Redundant null check: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// https://bugs.eclipse.org/400761: [compiler][null] null may be return as boolean without a diagnostic
public void test0037_conditional_expression_1() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // needs autoboxing
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	boolean badFunction(int i) {\n" +
			"		return i > 0 ? true : null;\n" +
			"	}\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	return i > 0 ? true : null;\n" +
		"	       ^^^^^^^^^^^^^^^^^^^\n" +
		"Potential null pointer access: This expression of type Boolean may be null but requires auto-unboxing\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/400761: [compiler][null] null may be return as boolean without a diagnostic
public void test0037_conditional_expression_2() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // needs autoboxing
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SUPPRESS_OPTIONAL_ERRORS, JavaCore.ENABLED);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"public class X {\n" +
			"	int badFunction(int i) {\n" +
			"		return i > 0 ? null : Integer.MIN_VALUE;\n" +
			"	}\n" +
			"	@SuppressWarnings(\"null\")\n" +
			"	int silent(int i) {\n" +
			"		return i > 0 ? null : Integer.MIN_VALUE;\n" +
			"	}\n" +
			"}\n"},
		null,
		options,
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	return i > 0 ? null : Integer.MIN_VALUE;\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential null pointer access: This expression of type Integer may be null but requires auto-unboxing\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
//https://bugs.eclipse.org/400761: [compiler][null] null may be return as boolean without a diagnostic
public void test0037_conditional_expression_3() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // needs autoboxing
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"public class X {\n" +
			"	boolean badFunction3(int i) {\n" +
			"		//expected a potential null problem:\n" +
			"		return i > 0 ? true : (Boolean) null;\n" +
			"	}\n" +
			"}\n"},
		null,
		options,
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	return i > 0 ? true : (Boolean) null;\n" +
		"	                      ^^^^^^^^^^^^^^\n" +
		"Null pointer access: This expression of type Boolean is null but requires auto-unboxing\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/400761: [compiler][null] null may be return as boolean without a diagnostic
// if-then-else instead of conditional expression
public void test0037_conditional_expression_4() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // needs autoboxing
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	options.put(JavaCore.COMPILER_PB_UNNECESSARY_ELSE, JavaCore.IGNORE);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"public class X {\n" +
			"	boolean badFunction4(int i) {\n" +
			"	if (i > 0)\n" +
			"		return true;\n" +
			"	else\n" +
			"		// expected a null problem:\n" +
			"		return (Boolean) null;\n" +
			"	}\n" +
			"}\n"},
		null,
		options,
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	return (Boolean) null;\n" +
		"	       ^^^^^^^^^^^^^^\n" +
		"Null pointer access: This expression of type Boolean is null but requires auto-unboxing\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/400761: [compiler][null] null may be return as boolean without a diagnostic
// pot-null cond-expr in receiver position
public void test0037_conditional_expression_5() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"public class X {\n" +
			"	String badFunction3(int i) {\n" +
			"		return (i > 0 ? this : null).toString();\n" +
			"	}\n" +
			"	String badFunction4(int i) {\n" +
			"		Object o = null;\n" +
			"		return (i > 0 ? o : null).toString();\n" +
			"	}\n" +
			"}\n"},
		null,
		options,
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	return (i > 0 ? this : null).toString();\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential null pointer access: This expression may be null\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	return (i > 0 ? o : null).toString();\n" +
		"	       ^^^^^^^^^^^^^^^^^^\n" +
		"Null pointer access: This expression can only be null\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/403147 [compiler][null] FUP of bug 400761: consolidate interaction between unboxing, NPE, and deferred checking
// finally block injects pot-nn into itself via enclosing loop
public void test0037_autounboxing_1() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo1(boolean b) {\n" +
			"       int j = 0;\n" +
			"       Integer i = null;\n" +
			"       while (true) {\n" +
			"           try {\n" +
			"               j = 1;\n" +
			"           } finally {\n" +
			"               j = (b?i:1)+1;\n" +
			"               i = 2;\n" +
			"           }\n" +
			"       }\n" +
			"   }\n" +
			"	void foo2(boolean b) {\n" +
			"       int j = 0;\n" +
			"       Integer i = null;\n" +
			"       try {\n" +
			"           j = 1;\n" +
			"       } finally {\n" +
			"           j = (b?i:1)+1;\n" +
			"           i = 2;\n" +
			"       }\n" +
			"   }\n" +
			"}\n"},
		null,
		options,
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	j = (b?i:1)+1;\n" +
		"	       ^\n" +
		"Potential null pointer access: This expression of type Integer may be null but requires auto-unboxing\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 20)\n" +
		"	j = (b?i:1)+1;\n" +
		"	       ^\n" +
		"Null pointer access: This expression of type Integer is null but requires auto-unboxing\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/403147 [compiler][null] FUP of bug 400761: consolidate interaction between unboxing, NPE, and deferred checking
// inject pot.nn from try into finally
public void test0037_autounboxing_2() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo2(boolean b) {\n" +
			"       int j = 0;\n" +
			"       Integer i = null;\n" +
			"       while (true) {\n" +
			"           try {\n" +
			"               if (b)\n" +
			"                   i = 3;\n" +
			"           } finally {\n" +
			"               j = (b?i:1)+1;\n" +
			"           }\n" +
			"       }\n" +
			"   }\n" +
			"	void foo3(boolean b) {\n" +
			"       int j = 0;\n" +
			"       Integer i = null;\n" +
			"       try {\n" +
			"           if (b)\n" +
			"               i = 3;\n" +
			"       } finally {\n" +
			"           j = (b?i:1)+1;\n" +
			"       }\n" +
			"   }\n" +
			"}\n"},
		null,
		options,
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	j = (b?i:1)+1;\n" +
		"	       ^\n" +
		"Potential null pointer access: This expression of type Integer may be null but requires auto-unboxing\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 21)\n" +
		"	j = (b?i:1)+1;\n" +
		"	       ^\n" +
		"Potential null pointer access: This expression of type Integer may be null but requires auto-unboxing\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/403147 [compiler][null] FUP of bug 400761: consolidate interaction between unboxing, NPE, and deferred checking
// null from try, nn from catch, merge both into finally
public void test0037_autounboxing_3() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo3(Integer i, boolean b) {\n" +
			"       int j = 0;\n" +
			"       while (true) {\n" +
			"           try {\n" +
			"               i = null;\n" +
			"               unsafe();\n" +
			"           } catch (Exception e) {\n" +
			"               i = 3;\n" +
			"           } finally {\n" +
			"               j = (b?i:1)+1;\n" +
			"           }\n" +
			"       }\n" +
			"   }\n" +
			"	void foo4(Integer i, boolean b) {\n" +
			"       int j = 0;\n" +
			"       try {\n" +
			"           i = null;\n" +
			"           unsafe();\n" +
			"       } catch (Exception e) {\n" +
			"           i = 3;\n" +
			"       } finally {\n" +
			"           while (j < 0)\n" +
			"               j = (b?i:1)+1;\n" +
			"       }\n" +
			"   }\n" +
			"\n" +
			"   private void unsafe() throws Exception {\n" +
			"        throw new Exception();\n" +
			"   }\n" +
			"}\n"},
		null,
		options,
		"----------\n" +
		"1. ERROR in X.java (at line 11)\n" +
		"	j = (b?i:1)+1;\n" +
		"	       ^\n" +
		"Potential null pointer access: This expression of type Integer may be null but requires auto-unboxing\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 24)\n" +
		"	j = (b?i:1)+1;\n" +
		"	       ^\n" +
		"Potential null pointer access: This expression of type Integer may be null but requires auto-unboxing\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/403147 [compiler][null] FUP of bug 400761: consolidate interaction between unboxing, NPE, and deferred checking
// effective protection locally within the finally block
public void test0037_autounboxing_4() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo3(Integer i, boolean b) {\n" +
			"       int j = 0;\n" +
			"       while (true) {\n" +
			"           try {\n" +
			"               i = null;\n" +
			"               unsafe();\n" +
			"           } catch (Exception e) {\n" +
			"               i = 3;\n" +
			"           } finally {\n" +
			"				if (i == null) i = 4;\n" +
			"               j = (b?i:1)+1;\n" +
			"           }\n" +
			"       }\n" +
			"   }\n" +
			"	void foo4(Integer i, boolean b) {\n" +
			"       int j = 0;\n" +
			"       try {\n" +
			"           i = null;\n" +
			"           unsafe();\n" +
			"       } catch (Exception e) {\n" +
			"           i = 3;\n" +
			"       } finally {\n" +
			"           while (i == null)\n" +
			"				i = 4;\n" +
			"           while (j < 4)\n" +
			"               j = (b?i:1)+1;\n" +
			"       }\n" +
			"   }\n" +
			"\n" +
			"   private void unsafe() throws Exception {\n" +
			"        throw new Exception();\n" +
			"   }\n" +
			"}\n"},
		options);
}
// https://bugs.eclipse.org/403147 [compiler][null] FUP of bug 400761: consolidate interaction between unboxing, NPE, and deferred checking
// array reference in nested try
public void test0037_autounboxing_5() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"public class X {\n" +
			"		void foo(Object [] o, boolean b, Integer i) {\n" +
			"		int j = 1;\n" +
			"		try {\n" +
			"			if (b) i = null;\n" +
			"		} catch (RuntimeException r) {\n" +
			"			i = 3;\n" +
			"		} finally {\n" +
			"			try {\n" +
			"				System.out.println(o[i]);  \n" +
			"			} finally {\n" +
			"				System.out.println(j);\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"},
		null,
		options,
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	System.out.println(o[i]);  \n" +
		"	                     ^\n" +
		"Potential null pointer access: This expression of type Integer may be null but requires auto-unboxing\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// Bug 406384 - Internal error with I20130413
public void test0037_autounboxing_6() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"public class X {\n" +
			"	void test(List<String> l1, List<String> l2, int i, Object val) {\n" +
			"		for (String s1 : l1) {\n" +
			"			for (String s2 : l2) {\n" +
			"				switch (i) {\n" +
			"				case 1: \n" +
			"					boolean booleanValue = (Boolean)val;\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		});
}

// null analysis -- autoboxing
public void test0040_autoboxing_compound_assignment() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    Integer i = null;\n" +
				"    i += 1;\n" +
				"  }\n" +
				"}\n"},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	i += 1;\n" +
			"	^\n" +
			"Null pointer access: This expression of type Integer is null but requires auto-unboxing\n" +
			"----------\n",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// null analysis -- autoboxing
public void test0041_autoboxing_increment_operator() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    Integer i = null;\n" +
				"    i++;\n" + // complain: this is null
				"    ++i;\n" + // quiet (because previous step guards it)
				"  }\n" +
				"}\n"},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	i++;\n" +
			"	^\n" +
			"Null pointer access: This expression of type Integer is null but requires auto-unboxing\n" +
			"----------\n",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// null analysis -- autoboxing
public void test0042_autoboxing_literal() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    Integer i = 0;\n" +
				"    if (i == null) {};\n" + // complain: this is non null
				"  }\n" +
				"}\n"},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	if (i == null) {};\n" +
			"	    ^\n" +
			"Null comparison always yields false: The variable i cannot be null at this location\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	if (i == null) {};\n" +
			"	               ^^\n" +
			"Dead code\n" +
			"----------\n",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// null analysis -- autoboxing
public void test0043_autoboxing_literal() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    Integer i = null;\n" +
				"    System.out.println(i + 4);\n" + // complain: this is null
				"  }\n" +
				"}\n"},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	System.out.println(i + 4);\n" +
			"	                   ^\n" +
			"Null pointer access: This expression of type Integer is null but requires auto-unboxing\n" +
			"----------\n",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// null analysis -- autoboxing
// origin: AssignmentTest#test020
public void test0044_autoboxing() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    int i = 0;\n" +
			"    boolean b = i < 10;\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- autoboxing
// variant of 42 for
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=165346
public void test0045_autoboxing_operator() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    int j = 5;" +
				"    Integer i = 0 + j;\n" +
				"    if (i == null) {}\n" +
				"  }\n" +
				"}\n"},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	if (i == null) {}\n" +
			"	    ^\n" +
			"Null comparison always yields false: The variable i cannot be null at this location\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	if (i == null) {}\n" +
			"	               ^^\n" +
			"Dead code\n" +
			"----------\n",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// null analysis -- array
public void test0050_array() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String args[]) {\n" +
			"    args = new String[] {\"zero\"};\n" +
			"    args[0] = null;\n" +
			"    if (args[0] == null) {};\n" +
			     // quiet: we don't keep track of all array elements
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- array
public void test0051_array() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String args[]) {\n" +
			"    args = null;\n" +
			"    args[0].toString();\n" + // complain: args is null
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	args[0].toString();\n" +
		"	^^^^\n" +
		"Null pointer access: The variable args can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- array
public void test0052_array() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo(String args[]) {\n" +
			"    String s = args[0];\n" +
			"    if (s == null) {};\n" +
			     // quiet: we don't keep track of all array elements
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- array
public void test0053_array() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo(String args[]) {\n" +
			"    for (int i = 0; i < args.length; i++) { /* */}\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- method call
public void test0061_method_call_guard() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    o.toString();\n" +      // guards o from being null
			"    if (o == null) {};\n" + // complain
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	if (o == null) {};\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	if (o == null) {};\n" +
		"	               ^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - method call
public void test0062_method_call_isolation() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (bar(o = null)) {\n" +
			"      if (o == null) {/* empty */}\n" + // complain
			"    }\n" +
			"  }\n" +
			"  boolean bar(Object o) {\n" +
			"    return true;\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	if (o == null) {/* empty */}\n" +
		"	    ^\n" +
		"Redundant null check: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - method call
public void test0063_method_call_isolation() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (bar(o == null ? new Object() : o)) {\n" +
			"      if (o == null) {/* empty */}\n" + // quiet
			"    }\n" +
			"  }\n" +
			"  boolean bar(Object o) {\n" +
			"    return true;\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis - method call
public void test0064_method_call_isolation() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (bar(o = new Object())) {\n" +
			"      if (o == null) {/* empty */}\n" + // complain
			"    }\n" +
			"  }\n" +
			"  boolean bar(Object o) {\n" +
			"    return true;\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	if (o == null) {/* empty */}\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	if (o == null) {/* empty */}\n" +
		"	               ^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - method call
public void test0065_method_call_invocation_target() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    (o = new Object()).toString();\n" + // quiet
			"  }\n" +
			"}\n"},
		"");
}

// null analysis - method call
public void test0066_method_call_invocation_target() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = new Object();\n" +
			"    (o = null).toString();\n" + // complain
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	(o = null).toString();\n" +
		"	^^^^^^^^^^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - method call
public void test0067_method_call_invocation_target() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    (o = new Object()).toString();\n" + // quiet
			"    if (o == null)  { /* */ }\n" + // complain
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	if (o == null)  { /* */ }\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	if (o == null)  { /* */ }\n" +
		"	                ^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - method call
public void test0068_method_call_assignment() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  X bar() {\n" +
			"    return null;\n" +
			"  }\n" +
			"  void foo(X x) {\n" +
			"    x = x.bar();\n" +
			"    if (x == null)  { /* */ }\n" + // quiet
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- type reference
public void test0070_type_reference() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String args[]) {\n" +
			"    Class c = java.lang.Object.class;\n" +
			"    if (c == null) {};\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	if (c == null) {};\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable c cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	if (c == null) {};\n" +
		"	               ^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test0080_shortcut_boolean_expressions() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o1, Object o2) {\n" +
			"    if (o1 != null && (o2 = o1) != null) { /* */ }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	if (o1 != null && (o2 = o1) != null) { /* */ }\n" +
		"	                  ^^^^^^^^^\n" +
		"Redundant null check: The variable o2 cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test0081_shortcut_boolean_expressions() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o1, Object o2) {\n" +
			"    while (o1 != null && (o2 = o1) != null) { /* */ }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	while (o1 != null && (o2 = o1) != null) { /* */ }\n" +
		"	                     ^^^^^^^^^\n" +
		"Redundant null check: The variable o2 cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - shortcut boolean expression
public void test0082_shortcut_boolean_expression() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (o == null || o == null) {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    if (o == null) { /* */ }\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	if (o == null || o == null) {\n" +
		"	                 ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	if (o == null) { /* */ }\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 6)\n" +
		"	if (o == null) { /* */ }\n" +
		"	               ^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - shortcut boolean expression
public void test0083_shortcut_boolean_expression() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (o == null && o == null) {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    if (o == null) { /* */ }\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	if (o == null && o == null) {\n" +
		"	                 ^\n" +
		"Redundant null check: The variable o can only be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	if (o == null) { /* */ }\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 6)\n" +
		"	if (o == null) { /* */ }\n" +
		"	               ^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - shortcut boolean expression
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=130311
public void test0084_shortcut_boolean_expression() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean foo(Integer i1, Integer i2) {\n" +
			"    return (i1 == null && i2 == null)\n" +
			"      || (i1.byteValue() == i2.byteValue());\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	|| (i1.byteValue() == i2.byteValue());\n" +
		"	    ^^\n" +
		"Potential null pointer access: The variable i1 may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - shortcut boolean expression
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=130311
public void test0085_shortcut_boolean_expression() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean foo(Integer i1, Integer i2) {\n" +
			"    return (i1 == null & i2 == null)\n" +
			"      || (i1.byteValue() == i2.byteValue());\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	|| (i1.byteValue() == i2.byteValue());\n" +
		"	    ^^\n" +
		"Potential null pointer access: The variable i1 may be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	|| (i1.byteValue() == i2.byteValue());\n" +
		"	                      ^^\n" +
		"Potential null pointer access: The variable i2 may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - shortcut boolean expression and correlation
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=195774
public void test0086_shortcut_boolean_expression() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static int foo(Integer i, Integer j) {\n" +
			"    if (i == null && j == null) {\n" +
			"      return 1;\n" +
			"    }\n" +
			"    if (i == null) {\n" +
			"      return j.intValue();\n" +
			"    }\n" +
			"    if (j == null) {\n" +
			"      return i.intValue();\n" +
			"    }\n" +
			"    return 0;\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - shortcut boolean expression and correlation
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=195774
public void _test0087_shortcut_boolean_expression() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static int foo(Integer i, Integer j) {\n" +
			"    if (i == null && j == null) {\n" +
			"      return 1;\n" +
			"    }\n" +
			"    if (j == null) {\n" +
			"      return i.intValue();\n" +
			"    }\n" +
			"    if (i == null) {\n" +
			"      return j.intValue();\n" +
			"    }\n" +
			"    return 0;\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis -- instanceof
// JLS: instanceof returns false if o turns out to be null
public void test0090_instanceof() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  boolean dummy;\n" +
			"  void foo (Object o) {\n" +
			"	if (dummy) {\n" +
			"	  o = null;\n" +
			"	}\n" +
			"	if (o instanceof X) { /* */ }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis -- instanceof
public void test0091_instanceof() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  boolean dummy;\n" +
			"  void foo (Object o) {\n" +
			"	if (dummy) {\n" +
			"	  o = null;\n" +
			"	}\n" +
			"	if (o instanceof X) { /* */ }\n" +
			"	if (o == null) { /* */ }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis -- instanceof
// can only be null always yields false
public void test0092_instanceof() {
	runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  boolean dummy;\n" +
			"  void foo () {\n" +
			"	Object o = null;\n" +
			"	if (o instanceof X) { /* */ }\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	if (o instanceof X) { /* */ }\n" +
		"	    ^\n" +
		"instanceof always yields false: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- instanceof
public void test0093_instanceof() {
	runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(Object x) {\n" +
			"    if (x instanceof X) {\n" +
			"      if (x == null) { /* */ }\n" + // cannot happen
			"    }\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	if (x == null) { /* */ }\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable x cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	if (x == null) { /* */ }\n" +
		"	               ^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- instanceof
public void test0094_instanceof() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(Object x) {\n" +
			"    if (x instanceof X) {\n" +
			"      return;\n" +
			"    }\n" +
			"    if (x != null) { /* */ }\n" +
			// cannot decide: could be null of new Object() for example
			"  }\n" +
			"}"},
		"");
}

// null analysis -- instanceof combined with conditional or
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=145202
public void test0095_instanceof_conditional_or() {
	runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(Object x) {\n" +
			"    if (! (x instanceof String)\n" +
			"         || x == null) {\n" +
			"      return;\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	|| x == null) {\n" +
		"	   ^\n" +
		"Null comparison always yields false: The variable x cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- strings concatenation
// JLS 15.18.1: if one of the operands is null, it is replaced by "null"
// Note: having the diagnostic could come handy when the initialization path
//       is non trivial; to get the diagnostic, simply put in place an
//       extraneous call to toString() -- and remove it before releasing.
public void test0120_strings_concatenation() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  String foo(String s1, String s2) {\n" +
			"    if (s1 == null) { /* */ };\n" +
			"    return s1 + s2;\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- strings concatenation
public void test0121_strings_concatenation() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  String foo(String s1, String s2) {\n" +
			"    if (s1 == null) { /* */ };\n" +
			"    s1 += s2;\n" +
			"    return s1;\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- strings concatenation
public void test0122_strings_concatenation() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  String foo(String s1) {\n" +
			"    if (s1 == null) { /* */ };\n" +
			"    return s1.toString();\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	return s1.toString();\n" +
		"	       ^^\n" +
		"Potential null pointer access: The variable s1 may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- strings concatenation
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127919
// it should suffice that the return type is String to avoid
// errors
public void test0123_strings_concatenation() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  String foo(String s, Object o, Integer i) {\n" +
			"    if (s == null || o == null || i == null) { /* */ };\n" +
			"    if (bar()) {\n" +
			"      return s + i;\n" + // quiet: i replaced by "null" if null
			"    }\n" +
			"    return o + s;\n" + // quiet: o replaced by "null" if null
			"  }\n" +
			"  boolean bar() {\n" +
			"    return false;\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- strings concatenation
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127919
// variant
public void test0124_strings_concatenation() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  String foo(String s, Object o, Integer i) {\n" +
			"    if (s == null || o == null || i == null) { /* */ };\n" +
			"    s += o;\n" + // quiet: o replaced by "null" if null
			"    s += i;\n" + // quiet: i replaced by "null" if null
			"    return s;\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- strings concatenation
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127919
// variant
public void test0125_strings_concatenation() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, Integer i) {\n" +
			"    System.out.println(o + (o == null ? \"\" : o.toString()));\n" + // quiet: o replaced by "null" if null
			"    System.out.println(i + (i == null ? \"\" : i.toString()));\n" + // quiet: o replaced by "null" if null
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- strings concatenation
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=132867
public void test0126_strings_concatenation() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    System.out.println(o + \"\");\n" +
			"    if (o != null) { /* */ };\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- strings concatenation
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=132867
public void test0127_strings_concatenation() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    System.out.println(o + \"\");\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- if/else
// check that obviously unreachable code does not modify the null
// status of a local
// the said code is not marked as unreachable per JLS 14.21 (the rationale
// being the accommodation for the if (constant_flag_evaluating_to_false)
// {code...} volontary code exclusion pattern)
public void test0300_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() {\n" +
			"    Object o = null;\n" +
			"    if (false) {\n" +
			"      o = new Object();\n" + // skipped
			"    }\n" +
			"    if (true) {\n" +
			"      //\n" +
			"    }\n" +
			"    else {\n" +
			"      o = new Object();\n" + // skipped
			"    }\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
			"----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	if (false) {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 10)\n" +
			"	else {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 13)\n" +
			"	o.toString();\n" +
			"	^\n" +
			"Null pointer access: The variable o can only be null at this location\n" +
			"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0301_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = new Object();\n" +
			"    if (o != null) {\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	if (o != null) {\n" +
		"	    ^\n" +
		"Redundant null check: The variable o cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0302_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) throws Exception {\n" +
			"    if (o == null) {\n" +
			"      throw new Exception();\n" +
			"    }\n" +
			"    if (o != null) {\n" + // only get there if o non null
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	if (o != null) {\n" +
		"	    ^\n" +
		"Redundant null check: The variable o cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0303_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (o == null) {\n" +
			"      return;\n" +
			"    }\n" +
			"    if (o != null) {\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	if (o != null) {\n" +
		"	    ^\n" +
		"Redundant null check: The variable o cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0304_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (o == null) {\n" +
			"      o.toString();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0305_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (o == null) {\n" +
			"      // do nothing\n" +
			"    }\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0306_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (o.toString().equals(\"\")) {\n" +
			"      if (o == null) {\n" + // complain: could not get here
			"        // do nothing\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	if (o == null) {\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	if (o == null) {\n" +
		"        // do nothing\n" +
		"      }\n" +
		"	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0307_if_else() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (o ==  null) {\n" +
			"      System.exit(0);\n" +
			"    }\n" +
			"    if (o == null) {\n" +
			  // quiet
			  // a direct call to System.exit() can be recognized as such; yet,
			  // a lot of other methods may have the same property (aka calling
			  // System.exit() themselves.)
			"      // do nothing\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis - if/else
public void test0308_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean b;\n" +
			"  void foo(Object o) {\n" +
			"    if (b) {\n" +
			"      o = null;\n" +
			"    }\n" +
			"    o.toString();\n" + // complain
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0309_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean b1, b2;\n" +
			"  void foo(Object o) {\n" +
			"    if (b1) {\n" +
			"      o = null;\n" +
			"    }\n" +
			"    if (b2) {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    o.toString();\n" + // complain
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0310_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean b1, b2;\n" +
			"  void foo(Object o) {\n" +
			"    if (b1) {\n" +
			"      o = null;\n" +
			"    }\n" +
			"    if (b2) {\n" +
			"      o.toString();\n" + // complain
			"      o.toString();\n" + // silent
			"    }\n" +
			"    o.toString();\n" + // complain
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 11)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0311_if_else() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (o == null)\n" +
			"      o = new Object();\n" +
			"    o.toString();\n" + // quiet
			"  }\n" +
			"}"	},
		"");
}

// null analysis - if/else
public void test0312_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"  void foo() {\n" +
			"    Object o = new Object();\n" +
			"    if (o == null) { /* */ }\n" + // complain
			"    if (o != null) { /* */ }\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	if (o == null) { /* */ }\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 5)\n" +
		"	if (o == null) { /* */ }\n" +
		"	               ^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 6)\n" +
		"	if (o != null) { /* */ }\n" +
		"	    ^\n" +
		"Redundant null check: The variable o cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0313_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (o == null) {\n" + // quiet
			"      o = new Object();\n" +
			"    }\n" +
			"    if (o == null) { /* */ }\n" +
			// complain: o set to non null iff it was null
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	if (o == null) { /* */ }\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 6)\n" +
		"	if (o == null) { /* */ }\n" +
		"	               ^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0314_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (o != null) {\n" + // quiet
			"      o = null;\n" +
			"    }\n" +
			"    if (o == null) { /* */ }\n" + // complain
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	if (o == null) { /* */ }\n" +
		"	    ^\n" +
		"Redundant null check: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0315_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (o != null) {\n" + // quiet
			"      o = null;\n" +
			"    }\n" +
			"    o.toString();\n" + // complain
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0316_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    if (o == null || b) { /* */ }\n" + // quiet
			"    else { /* */ }\n" +
			"    o.toString();\n" + // complain
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0317_if_else_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    if (o != null) {\n" + // quiet
			"      if (b) {\n" + // quiet
			"        o = null;\n" +
			"      }\n" +
			"    }\n" +
			"    if (o == null) { /* */ }\n" + // quiet
			"  }\n" +
			"}"},
		"");
}

// null analysis - if/else
public void test0318_if_else_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    if (o != null) {\n" + // quiet
			"      if (b) {\n" + // quiet
			"        o = null;\n" +
			"      }\n" +
			"      if (o == null) { /* */ }\n" + // quiet
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - if/else
// we do nothing to diagnose the contents of fake reachable code
public void test0319_if_else_dead_branch() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    if (false) {\n" +
			"      o = null;\n" +
			"      if (o == null) { /* */ }\n" + // may have considered complaining here
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - if/else
public void test0320_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    o.toString();\n" +
			"    if (o == null) { /* */ }\n" + // complain
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	if (o == null) { /* */ }\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	if (o == null) { /* */ }\n" +
		"	               ^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0321_if_else() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    Object other = new Object();\n" +
			"    if (b) {\n" +
			"      other = o;\n" +
			"    }\n" +
			"    if (o != null) { /* */ }\n" + // quiet
			"  }\n" +
			"}"},
		"");
}

// null analysis - if/else
public void test0322_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    o.toString();\n" +
			"    if (b) { /* */ }\n" +
			"    if (o == null) { /* */ }\n" + // complain
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	if (o == null) { /* */ }\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 5)\n" +
		"	if (o == null) { /* */ }\n" +
		"	               ^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0323_if_else() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    if (o == null && b) {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    if (o == null) { /* */ }\n" + // quiet
			"  }\n" +
			"}"},
		"");
}

// null analysis - if/else
public void test0324_if_else_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo (boolean b) {\n" +
			"    String s = null;\n" +
			"    if (b) {\n" +
			"      if (b) {\n" +
			"        s = \"1\";\n" +
			"      } \n" +
			"      else {\n" +
			"        s = \"2\";\n" +
			"      }\n" +
			"    } \n" +
			"    else if (b) {\n" +
			"      s = \"3\"; \n" +
			"    } \n" +
			"    else {\n" +
			"      s = \"4\";\n" +
			"    }\n" +
			"    s.toString();\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - if/else
public void test0325_if_else_nested() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo (boolean b) {\n" +
			"    String s = null;\n" +
			"    if (b) {\n" +
			"      if (b) {\n" +
			"        s = \"1\";\n" +
			"      } \n" +
			"      else {\n" +
			"        s = \"2\";\n" +
			"      }\n" +
			"    } \n" +
			"    else if (b) {\n" +
			"      if (b) {\n" +
			"        s = \"3\"; \n" +
			"      }\n" +
			"    } \n" +
			"    else {\n" +
			"      s = \"4\";\n" +
			"    }\n" +
			"    s.toString();\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 20)\n" +
		"	s.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable s may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
// limit: we cannot sync on external factors, even if this is a pattern
// that is quite used
public void test0326_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo (boolean b) {\n" +
			"    String s1 = null;\n" +
			"    if (b) {\n" +
			"      s1 = \"1\";\n" +
			"    }\n" +
			"    s1.toString();\n" + // complain: can't guess if b means anything for s1 init
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	s1.toString();\n" +
		"	^^\n" +
		"Potential null pointer access: The variable s1 may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
// limit: we cannot sync on external factors, even if this is a pattern
// that is quite used
public void test0327_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo (String s1) {\n" +
			"    String s2 = null;\n" +
			"    if (s1 == null) {\n" +
			"      s1 = \"1\";\n" +
			"      s2 = \"2\";\n" +
			"    }\n" +
			"    s1.toString();\n" + // quiet
			"    s2.toString();\n" + // complain: can't guess whether s2 depends on s1 for init
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	s2.toString();\n" +
		"	^^\n" +
		"Potential null pointer access: The variable s2 may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0328_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    if (o != null || b) {\n" +
			"      if (b) {\n" +
			"        o = new Object();\n" +
			"      }\n" +
			"    }\n" + // quiet
			"    else { /* */ }\n" +
			"    o.toString();\n" + // complain
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0329_if_else_nested() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    if (b) {\n" +
			"      if (o != null) { /* */ }\n" + // shade doubts on o
			"    }\n" +
			"    o.toString();\n" + // complain
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0330_if_else_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    if (b) {\n" +
			"      if (o == null) {\n" +
			"        o = new Object();\n" +
			"      }\n" +
			"    }\n" +
			"    o.toString();\n" + // quiet
			"  }\n" +
			"}"},
		"");
}

// null analysis - if/else
public void test0331_if_else_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o1, Object o2) {\n" +
			"    Object o3 = o2;\n" +
			"    if (o1 != null) {\n" +
			"      o3.toString(); // guards o3\n" +
			"    }\n" +
			"    o1 = o3;\n" +
			"    if (o1 != null) { /* */ }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - if/else
public void test0332_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    o = new Object();\n" +
			"    if (b) {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    if (o != null) { /* */ }\n" + // complain
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	if (o != null) { /* */ }\n" +
		"	    ^\n" +
		"Redundant null check: The variable o cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=128014
// invalid analysis when redundant check is done
public void test0333_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    o = new Object();\n" +
			"    if (o != null) {\n" + // complain
			"      o.toString();\n" +
			"    }\n" +
			"    o.toString();\n" + // quiet asked
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	if (o != null) {\n" +
		"	    ^\n" +
		"Redundant null check: The variable o cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=128014
// invalid analysis when redundant check is done - variant
public void test0334_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    o = new Object();\n" +
			"    if (o != null) {\n" + // complain
			"      o.toString();\n" +
			"    }\n" +
			"    else {\n" +
			"      o.toString();\n" +
			"    }\n" +
			"    o.toString();\n" + // quiet
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	if (o != null) {\n" +
		"	    ^\n" +
		"Redundant null check: The variable o cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 7)\n" +
		"	else {\n" +
		"      o.toString();\n" +
		"    }\n" +
		"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=129581
// Test that no false null reference warning is issued for a variable
// that has been wrongly tainted by a redundant null check upstream.
public void test0335_if_else() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (o != null) {\n" +
			"      if (o != null) {\n" + // complain
			"        o.toString();\n" +
			"      }\n" +
			"      o.toString();\n" + // quiet
			"    }\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	if (o != null) {\n" +
		"	    ^\n" +
		"Redundant null check: The variable o cannot be null at this location\n" +
		"----------\n");
}

// null analysis - if/else
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=128014
// invalid analysis when redundant check is done - variant
public void test0336_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (o != null) {\n" +
			"      if (o != null) {\n" + // complain
			"        o.toString();\n" +
			"      }\n" +
			"      else {\n" +
			"        o.toString();\n" + // must complain anyway (could be quite distant from the if test)
			"      }\n" +
			"      o.toString();\n" + // quiet
			"    }\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	if (o != null) {\n" +
		"	    ^\n" +
		"Redundant null check: The variable o cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 7)\n" +
		"	else {\n" +
		"        o.toString();\n" +
		"      }\n" +
		"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}


// null analysis - if/else nested with correlation
// reconsider if we implement correlation
// TODO (maxime) https://bugs.eclipse.org/bugs/show_bug.cgi?id=128861
public void _test0337_if_else_nested_correlation() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public int foo (Object o1, Object o2) {\n" +
			"    int result = 0;\n" +
			"    if (o1 == null && o2 != null) {\n" +
			"      result = -1;\n" +
			"    } else {\n" +
			"      if (o1 == null && o2 == null) {\n" +
			"        result = 0;\n" +
			"      } else {\n" +
			"        if (o1 != null && o2 == null) {\n" +
			"          result = 1;\n" +
			"        } else {\n" +
			"          int lhs = ((Y) o1).foo();  // may be null\n" +
			"          int rhs = ((Y) o2).foo();\n" +
			"          result = lhs - rhs;\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"    return result;\n" +
			"  }\n" +
			"}\n" +
			"abstract class Y {\n" +
			"  abstract int foo();\n" +
			"}\n" +
			"\n"},
		"");
}

// null analysis - if/else nested with correlation
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=128861
// workaround
public void test0338_if_else_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public int foo (Object o1, Object o2) {\n" +
			"    int result = 0;\n" +
			"    if (o1 == null && o2 == null) {\n" +
			"      result = 0;\n" +
			"    } else {\n" +
			"      if (o1 == null) {\n" +
			"        result = -1;\n" +
			"      } else {\n" +
			"        if (o2 == null) {\n" +
			"          result = 1;\n" +
			"        } else {\n" +
			"          int lhs = ((Y) o1).foo();\n" +
			"          int rhs = ((Y) o2).foo();\n" +
			"          result = lhs - rhs;\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"    return result;\n" +
			"  }\n" +
			"}\n" +
			"abstract class Y {\n" +
			"  abstract int foo();\n" +
			"}\n" +
			"\n"},
		"");
}

// null analysis - if/else nested with unknown protection: unknown cannot protect
public void test0339_if_else_nested() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    if (o == null || b) {\n" +
			"      if (bar() == o) {\n" +
			"        o.toString();\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"  Object bar() {\n" +
			"    return new Object();\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else nested
public void test0340_if_else_nested() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (o == null) {\n" +
			"      if (bar() == o) {\n" +
			"        o.toString();\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"  Object bar() {\n" +
			"    return new Object();\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else nested
public void test0341_if_else_nested() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o1, Object o2, boolean b) {\n" +
			"    if (o1 == null || b) {\n" +
			"      if (o1 == o2) {\n" +
			"        o1.toString();\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	o1.toString();\n" +
		"	^^\n" +
		"Potential null pointer access: The variable o1 may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else nested
public void test0342_if_else_nested() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o1, Object o2, boolean b) {\n" +
			"    if (o1 == null || b) {\n" +
			"      if (o2 == o1) {\n" +
			"        o1.toString();\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	o1.toString();\n" +
		"	^^\n" +
		"Potential null pointer access: The variable o1 may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0401_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    while (o.toString() != null) {/* */}\n" +
			      // complain: NPE
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	while (o.toString() != null) {/* */}\n" +
		"	       ^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0402_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    while (o != null) {/* */}\n" +
			  // complain: get o null first time and forever
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	while (o != null) {/* */}\n" +
		"	       ^\n" +
		"Null comparison always yields false: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0403_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    while (o == null) {\n" +
			      // quiet: first iteration is sure to find o null,
			      // but other iterations may change it
			"      o = new Object();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- while
public void test0404_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    while (o == null) {\n" +
			     // quiet: first iteration is sure to find o null,
			     // but other iterations may change it
			"      if (System.currentTimeMillis() > 10L) {\n" +
			"        o = new Object();\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- while
public void test0405_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean bar() {\n" +
			"    return true;\n" +
			"  }\n" +
			"  void foo(Object o) {\n" +
			"    while (bar() && o == null) {\n" +
			"      o.toString();\n" + // complain: NPE
			"      o = new Object();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0406_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo(Object o) {\n" +
			"    o = null;\n" +
			"    while (dummy || o != null) { /* */ }\n" + // o can only be null
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	while (dummy || o != null) { /* */ }\n" +
		"	                ^\n" +
		"Null comparison always yields false: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0407_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    while (dummy) {\n" +
			"      o.toString();\n" +  // complain: NPE on first iteration
			"      o = new Object();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
// this test shows that, as long as we do not explore all possible
// paths, we have to take potential initializations into account
// even in branches that could be pruned in the first passes
// first approximation is to stop pruning code conditioned by
// variables
// second approximation could still rely upon variables that are
// never affected by the looping code (unassigned variables)
// complete solution would call for multiple iterations in the
// null analysis
public void test0408_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null,\n" +
			"           u = new Object(),\n" +
			"           v = new Object();\n" +
			"    while (o == null) {\n" +
			"      if (v == null) {\n" +
			"        o = new Object();\n" +
			"      };\n" +
			"      if (u == null) {\n" +
			"        v = null;\n" +
			"      };\n" +
			"      u = null;\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- while
public void test0409_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    while (dummy || (o = new Object()).equals(o)) {\n" +
			"      o.toString();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0410_while_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    while (dummy) {\n" +
			"      while (o != null) {\n" +
			"        o.toString();\n" +
			"      }\n" +
			"      if (System.currentTimeMillis() > 10L) {\n" +
			"        o = new Object();\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- while
public void test0411_while_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null,\n" +
			"           u = new Object(),\n" +
			"           v = new Object();\n" +
			"    while (o == null) {\n" +
			"      if (v == null) {\n" +
			"        o = new Object();\n" +
			"      };\n" +
			"      while (o == null) {\n" +
			"        if (u == null) {\n" +
			"          v = null;\n" +
			"        };\n" +
			"        u = null;\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- while
public void test0412_while_if_nested() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy, other;\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    while (dummy) {\n" +
			"      if (other) {\n" +
			"        o.toString();\n" +
			"      }\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0413_while_unknown_field() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object o;\n" +
			"  void foo(boolean dummy) {\n" +
			"    while (dummy) {\n" +
			"      o = null;\n" +
			"    }\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- while
public void test0414_while_unknown_parameter() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo(Object o) {\n" +
			"    while (dummy) {\n" +
			"      o = null;\n" + // quiet: first iteration doesn't know
			"    }\n" +
			"    o.toString();\n" + // complain: only get out of the loop with null
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0415_while_unknown_if_else() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    if (dummy) {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    while (dummy) {\n" +
			  // limit of the analysis: we do not correlate if and while conditions
			"      if (o == null) {/* */}\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- while
public void test0416_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    while (dummy) {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0417_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    while (dummy) { /* */ }\n" + // doesn't affect o
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
// origin AssignmentTest.testO22
public void test0418_while_try() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean bool() { return true; }\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    while (bool()) {\n" +
			"      try {\n" +
			"        if (o == null) {\n" +
			"          o = new Object();\n" +
			"        }\n" +
			"      } finally { /* */ }\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis -- while
public void test0419_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean bool;\n" +
			"  void foo(Object o) {\n" +
			"    while (bool) {\n" +
			"      o.toString();" + // complain NPE because of second iteration
			"      o = null;\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	o.toString();      o = null;\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0420_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean bool;\n" +
			"  void foo(Object compare) {\n" +
			"    Object o = new Object();\n" +
			"    while ((o = null) == compare) {\n" +
			"      if (true) {\n" +
			"        break;\n" +
			"      }\n" +
			"    }\n" +
			"    if (o == null) { /* */ }\n" + // complain can only be null
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	if (o == null) { /* */ }\n" +
		"	    ^\n" +
		"Redundant null check: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0421_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean bool;\n" +
			"  void foo(Object compare) {\n" +
			"    Object o = null;\n" +
			"    while (bool) {\n" +
			"      o = new Object();\n" +
			"      o.toString();\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis -- while
public void test0422_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean bool;\n" +
			"  void foo() {\n" +
			"    Object o;\n" +
			"    while (bool) {\n" +
			"      o = new Object();\n" +
			"      if (o == null) { /* */ }\n" +
			"      o = null;\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	if (o == null) { /* */ }\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 7)\n" +
		"	if (o == null) { /* */ }\n" +
		"	               ^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0423_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean bool;\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    while (bool) {\n" +
			"      o = new Object();\n" +
			"      if (o == null) { /* */ }\n" +
			"      o = null;\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	if (o == null) { /* */ }\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 7)\n" +
		"	if (o == null) { /* */ }\n" +
		"	               ^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0424_while_try() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(boolean b) {\n" +
			"    Object o = null;\n" +
			"    while (o == null) {\n" +
			     // quiet: first iteration is sure to find o null,
			     // but other iterations may change it
			"      try { /* */ }\n" +
			"      finally {\n" +
			"        if (b) {\n" +
			"          o = new Object();\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- while
public void test0425_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo(Object u) {\n" +
			"    Object o = null;\n" +
			"    while (dummy) {\n" +
			"      o = u;\n" +
			"    }\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0426_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo(Object o) {\n" +
			"    o.toString();\n" +
			"    while (dummy) { /* */ }\n" +
			"    if (o == null) { /* */ }\n" + // complain
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	if (o == null) { /* */ }\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 6)\n" +
		"	if (o == null) { /* */ }\n" +
		"	               ^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0427_while_return() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    while (dummy) {\n" +
			"      if (o == null) {\n" +
			"        return;\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	if (o == null) {\n" +
		"	    ^\n" +
		"Redundant null check: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - while
public void test0428_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  X bar() {\n" +
			"    return null;\n" +
			"  }\n" +
			"  void foo(X x) {\n" +
			"    x.bar();\n" +
			"    while (x != null) {\n" +
			"      x = x.bar();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis - while
public void test0429_while_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  boolean dummy;\n" +
			"  void foo (X[] xa) {\n" +
			"	while (dummy) {\n" +
			"	  xa = null;\n" +
			"	  if (dummy) {\n" +
			"	    xa = new X[5];\n" +
			"	  }\n" +
			"	  if (xa != null) {\n" +
			"		int i = 0;\n" +
			"		while (dummy) {\n" +
			"		  X x = xa[i++];\n" +
			"		  x.toString();\n" +
			"		}\n" +
			"	  }\n" +
			"	}\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - while
public void test0430_while_for_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  boolean dummy;\n" +
			"  void foo (X[] xa) {\n" +
			"	while (dummy) {\n" +
			"	  xa = null;\n" +
			"	  if (dummy) {\n" +
			"	    xa = new X[5];\n" +
			"	  }\n" +
			"	  if (xa != null) {\n" +
			"		for (int i = 0; i < xa.length; i++) {\n" +
			"		  X x = xa[i];\n" +
			"		  x.toString();\n" +
			"		}\n" +
			"	  }\n" +
			"	}\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - while
public void test0431_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  boolean dummy;\n" +
			"  void foo (X x) {\n" +
			"	x = null;\n" +
			"	while (dummy) {\n" +
			"	  x = bar();\n" +
			"	  x.toString();\n" +
			"	}\n" +
			"  }\n" +
			"  X bar() {\n" +
			"	return null;\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - while
public void test0432_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  boolean dummy;\n" +
			"  void foo (X x) {\n" +
			"	while (dummy) {\n" +
			"	  x = bar();\n" +
			"	  x.toString();\n" +
			"	}\n" +
			"  }\n" +
			"  X bar() {\n" +
			"	return null;\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - while
public void test0433_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  boolean dummy;\n" +
			"  void foo (X x) {\n" +
			"	x = null;\n" +
			"   while (dummy) {\n" +
			"	  x.toString();\n" + // complain and protect
			"	  x.toString();\n" + // quiet
			"	}\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	x.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable x can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - while
// this one shows that we cannot project definitely unknown onto potentially unknown too soon
public void test0434_while_switch_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  Object bar() {\n" +
			"    return new Object();\n" +
			"  }\n" +
			"  void foo(boolean b, int selector) {\n" +
			"    Object o = null;\n" +
			"    while (b) {\n" +
			"      switch (selector) {\n" +
			"      case 0:\n" +
			"        o = bar();\n" +
			"        if (o != null) { \n" +
			"          return;\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - while
public void test0435_while_init() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  int f1;\n" +
			"  X f2;\n" +
			"  void foo(X x1, boolean b) {\n" +
			"    X x2;\n" +
			"    x2 = x1;\n" +
			"    while (b) {\n" +
//			"      if (x2.f1 > 0) { /* */ }\n" +
			"      if (x2.toString().equals(\"\")) { /* */ }\n" +
			"      x2 = x2.f2;\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - while
public void test0436_while_init() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  int f1;\n" +
			"  X f2;\n" +
			"  void foo(X x1, boolean b) {\n" +
			"    X x2 = x1;\n" +
			"    while (b) {\n" +
			"      if (x2.f1 > 0) { /* */ }\n" +
			"      x2 = x2.f2;\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - while
public void test0437_while_exit() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(boolean b) {\n" +
			"    Object o = null;\n" +
			"    while (b) {\n" +
			"      if (b) {\n" +
			"        o = new Object();\n" +
			"      }\n" +
			"      if (o != null) {\n" +
			"        throw new RuntimeException(); \n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}


// null analysis - while
public void test0438_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(Object o) {\n" +
			"    while (o == null) { /* */ }\n" +
			"    o.toString();\n" + // quiet
			"    if (o != null) { /* */ }\n" + // complain
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	if (o != null) { /* */ }\n" +
		"	    ^\n" +
		"Redundant null check: The variable o cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - while
public void test0439_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(Object o) {\n" +
			"    while (o == null) {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    o.toString();\n" + // quiet
			"  }\n" +
			"}"},
		"");
}

// null analysis - while
public void test0440_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(Object o) {\n" +
			"    while (o == null) {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    if (o != null) { /* */ }\n" + // complain
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	if (o != null) { /* */ }\n" +
		"	    ^\n" +
		"Redundant null check: The variable o cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - while
public void test0441_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  X bar() {\n" +
			"    return new X();\n" +
			"  }\n" +
			"  void foo(Object o) {\n" +
			"    while (o == null) {\n" +
			"      o = bar();\n" +
			"    }\n" +
			"    if (o != null) { /* */ }\n" + // complain
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	if (o != null) { /* */ }\n" +
		"	    ^\n" +
		"Redundant null check: The variable o cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - while
public void test0442_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  boolean bar() {\n" +
			"    return true;\n" +
			"  }\n" +
			"  void foo(Object o) {\n" +
			"    while (o == null && bar()) { /* */ }\n" +
			"    o.toString();\n" + // complain
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - while
public void test0443_while_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    ext: for (int i = 0; i < 5 ; i++) {\n" +
			"        if (o != null) {\n" +
			"          break;\n" +
			"        }\n" +
			"        o = new Object();\n" +
			"        int j = 0;\n" +
			"        while (j++ < 2) {\n" +
			"          continue ext;\n" +
			"        }\n" +
			"        return;\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - while
public void test0444_while_deeply_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(boolean b) {\n" +
			"    Object o = null;\n" +
			"    ext: for (int i = 0; i < 5 ; i++) {\n" +
			"        if (o != null) {\n" +
			"          break;\n" +
			"        }\n" +
			"        do {\n" +
			"          o = new Object();\n" +
			"          int j = 0;\n" +
			"          while (j++ < 2) {\n" +
			"            continue ext;\n" +
			"          }\n" +
			"        } while (b);\n" +
			"        return;\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - while
public void test0445_while_deeply_nested() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(boolean b) {\n" +
			"    Object o = null;\n" +
			"    ext: for (int i = 0; i < 5 ; i++) {\n" +
			"        if (o != null) {\n" +
			"          break;\n" +
			"        }\n" +
			"        do {\n" +
			"          // o = new Object();\n" +
			"          int j = 0;\n" +
			"          while (j++ < 2) {\n" +
			"            continue ext;\n" +
			"          }\n" +
			"        } while (b);\n" +
			"        return;\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	if (o != null) {\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - while
public void test0446_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    while (o == null || b) {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    if (o != null) { /* */ }\n" + // complain
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	if (o != null) { /* */ }\n" +
		"	    ^\n" +
		"Redundant null check: The variable o cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - while
public void test0447_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    while (o == null & b) {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    if (o != null) { /* */ }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - while
public void test0448_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(boolean b[]) {\n" +
			"    Object o = null;\n" +
			"    ext: for (int i = 0; i < 5 ; i++) {\n" +
			"        if (o != null) {\n" +
			"          break;\n" +
			"        }\n" +
			"        while (b[1]) {\n" +
			"          continue ext;\n" +
			"        }\n" +
			"        while (b[2]) {\n" +
			"          continue ext;\n" +
			"        }\n" +
			"        while (b[3]) {\n" +
			"          continue ext;\n" +
			"        }\n" +
			"        while (b[4]) {\n" +
			"          continue ext;\n" +
			"        }\n" +
			"        while (b[5]) {\n" +
			"          continue ext;\n" +
			"        }\n" +
			"        while (b[6]) {\n" +
			"          continue ext;\n" +
			"        }\n" +
			"        return;\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	if (o != null) {\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - while
// this series (up to 451) shows that the merge of the states
// potential non null and potential unknown yields damages in
// case of nested loops (unested loops still OK because we can
// carry the definite non null property)
public void test0449_while_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(Object p, boolean b) {\n" +
			"    Object o = new Object();\n" +
			"    while (b) {\n" +
			"      while (b) {\n" +
			"        o = p;\n" + // now o is unknown
			"      }\n" +
			"    }\n" +
			"    if (o != null) { /* */ }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - while
public void test0450_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(boolean b) {\n" +
			"    Object o = new Object();\n" +
			"    while (b) {\n" +
			"      o = new Object();\n" + // o still non null
			"    }\n" +
			"    if (o != null) { /* */ }\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	if (o != null) { /* */ }\n" +
		"	    ^\n" +
		"Redundant null check: The variable o cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=133131
public void test0451_while_nested() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(boolean b) {\n" +
			"    Object o = new Object();\n" +
			"    while (b) {\n" +
			"      while (b) {\n" +
			"        o = new Object();\n" + // o still non null
			"      }\n" +
			"    }\n" +
			"    if (o != null) { /* */ }\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	if (o != null) { /* */ }\n" +
		"	    ^\n" +
		"Redundant null check: The variable o cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=123399
// variant - the bug is not specific to the do while loop
public void _test0452_while() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object doubt) {\n" +
			"    Object o = null;\n" +
			"    while (true) {\n" +
			"      if (o == null) {\n" +
			"        return;\n" +
			"      }\n" +
			"      o = doubt;\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	if (o == null) {\n" +
		"	    ^\n" +
		"Redundant null check: The variable o can only be null at this location\n" +
		"----------\n"
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=123399
// variant - cannot refine the diagnostic without engaging into conditionals
// dedicated flow context
public void _test0453_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object doubt, boolean b) {\n" +
			"    Object o1 = null, o2 = null;\n" +
			"    while (true) {\n" +
			"      if (o1 == null) { /* empty */ }\n" +
			"      if (b) {\n" +
			"        if (o2 == null) {\n" +
			"          return;\n" +
			"        }\n" +
			"      }\n" +
			"      o1 = o2 = doubt;\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"ERROR: complain on line 7, but not on line 5"
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=129122
public void test0454_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object bar() {\n" +
			"    return new Object();\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    while (true) {\n" +
			"      o = bar();\n" +
			"      if (o != null) {\n" +
			"        o = new Object();\n" +
			"      }\n" +
			"      o = null; // quiet pls\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		""
	);
}

// null analysis - while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=133131
// variant
public void test0455_while_nested() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(boolean b) {\n" +
			"    Object o = new Object();\n" +
			"    while (b) {\n" +
			"      o = new Object();\n" + // o still non null
			"    }\n" +
			"    if (o != null) { /* */ }\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	if (o != null) { /* */ }\n" +
		"	    ^\n" +
		"Redundant null check: The variable o cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=134848
// false positive after nested loop with break to explicit label
public void test0456_while_nested_explicit_label() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    while (true) {\n" +
			"      if (o != null) {\n" +
			"        o.toString();\n" +
			"        loop: while (true) {\n" +
			"          break loop;\n" +
			"        }\n" +
			"        o.toString();\n" + // must not complain here
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=154995
public void test0457_while_nested_break() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void test(String p, String q, boolean b) {\n" +
			"    while (b) {\n" +
			"      String e = q;\n" +
			"      e.trim();\n" +
			"      while (true) {\n" +
			"        if (b)\n" +
			"          e = q;\n" +
			"        else\n" +
			"          e = null;\n" +
			"        if (e == null || p != null) {\n" +
			"          if (e != null) {\n" + // should not complain here
			"            // Do something\n" +
			"          }\n" +
			"          break;\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=134848
// variant: no label yields no problem
public void test0458_while_nested_explicit_label() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    while (true) {\n" +
			"      if (o != null) {\n" +
			"        o.toString();\n" +
			"        while (true) {\n" +
			"          break;\n" +
			"        }\n" +
			"        o.toString();\n" + // must not complain here
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis -- while nested hits CAN_ONLY_NON_NULL
public void test0459_while_nested() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(boolean b) {\n" +
			"    Object o = b ? null : new Object(),\n" +
			"           u = new Object(),\n" +
			"           v = new Object();\n" +
			"    while (o != null) {\n" +
			"      while (b) {\n" +
			"        if (v == null) {\n" +
			"          o = new Object();\n" +
			"        };\n" +
			"        while (o == null) {\n" +
			"          if (u == null) {\n" +
			"            v = null;\n" +
			"          };\n" +
			"          u = null;\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 11)\n" +
		"	while (o == null) {\n" +
		"	       ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=176472
// extraneous error in case of a labeled while(true) statement
public void test0460_while_explicit_label() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(int i) {\n" +
			"    Object o = null;\n" +
			"    done: while (true) {\n" +
			"      switch (i) {\n" +
			"        case 0:\n" +
			"          o = new Object();\n" +
			"          break;\n" +
			"        case 1:\n" +
			"          break done;\n" +
			"      }\n" +
			"    }\n" +
			"    if (o == null) {\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis - while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=176472
// extraneous error in case of a labeled while(true) statement
public void test0461_while_explicit_label() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean test() {\n" +
			"    return true;\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    done: while (true) {\n" +
			"      if (test()) {\n" +
			"        break done;\n" +
			"      }\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    if (o == null) {\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis - while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=176472
// variant
public void test0462_while_explicit_label() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean test() {\n" +
			"    return true;\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    done: while (true) {\n" +
			"      try {\n" +
			"        while (true) {\n" +
			"          if (test()) {\n" +
			"            break done;\n" +
			"          }\n" +
			"        }\n" +
			"      }\n" +
			"      finally {\n" +
			"        if (test()) {\n" +
			"          o = new Object();\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"    if (o == null) {\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis - while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184298
// variant
public void test0463_while_infinite() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void test(String[] a) {\n" +
			"    String key = null;\n" +
			"    while(true)\n" +
			"    {\n" +
			"      if (a[0] == null)\n" +
			"        break;\n" +
			"      key = a[0];\n" +
			"    }\n" +
			"    if (key != null) {\n" +
			"      // empty\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184298
// variant
public void test0464_while_infinite() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void test(String[] a) {\n" +
			"    String key = null;\n" +
			"    loop: while(true)\n" +
			"    {\n" +
			"      if (a[0] == null)\n" +
			"        break loop;\n" +
			"      key = a[0];\n" +
			"    }\n" +
			"    if (key != null) {\n" +
			"      // empty\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184298
// variant
public void test0465_while_infinite() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void test(String[] a) {\n" +
			"    String key = null;\n" +
			"    while(true)\n" +
			"    {\n" +
			"      if (a[0] == null)\n" +
			"        break;\n" +
			"      key = \"non null\";\n" +
			"    }\n" +
			"    if (key != null) {\n" +
			"      // empty\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=198955
// dupe of bug 184298 in fact
public void test0466_while_infinite() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    while (true) {\n" +
			"      if (bar()) {\n" +
			"        break;\n" +
			"      }\n" +
			"      if (o == null) {\n" +
			"        o = new Object();\n" +
			"      }\n" +
			"    }\n" +
			"    if (o == null) {}\n" +
			"  }\n" +
			"  boolean bar() {\n" +
			"    return false;\n" +
			"  }\n" +
			"}\n"},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=212283
// (which is a dupe of 184298)
public void test0467_while_break() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    RuntimeException e = null;\n" +
			"    while (e != null || bar()) {\n" +
			"      if (e != null || bar()) {\n" +
			"        break;\n" +  // always breaks out of the loop if e non-null
			"      }\n" +
			"      if (bar()) {\n" +
			"        e = new RuntimeException();\n" +
			"      }\n" +
			"    }\n" +
			"    if (e != null) {\n" +
			"      throw e;\n" +
			"    }\n" +
			"  }\n" +
			"  boolean bar() {\n" +
			"    return false;\n" +
			"  }\n" +
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=212283
// (which is a dupe of 184298)
public void test0468_while_break() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    RuntimeException e = null;\n" +
			"    while (e != null || bar()) {\n" +
			"      if (bar()) {\n" +
			"        break;\n" +
			"      }\n" +
			"      if (bar()) {\n" +
			"        e = new RuntimeException();\n" +
			"      }\n" +
			"    }\n" +
			"    if (e != null) {\n" +
			"      throw e;\n" +
			"    }\n" +
			"  }\n" +
			"  boolean bar() {\n" +
			"    return false;\n" +
			"  }\n" +
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=212283
// (which is a dupe of 184298)
public void test0469_while_break() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    RuntimeException e = null;\n" +
			"    while (e != null || bar()) {\n" +
			"      if (e != null) {\n" +
			"        break;\n" +
			"      }\n" +
			"      if (bar()) {\n" +
			"        e = new RuntimeException();\n" +
			"      }\n" +
			"    }\n" +
			"    if (e != null) {\n" +
			"      throw e;\n" +
			"    }\n" +
			"  }\n" +
			"  boolean bar() {\n" +
			"    return false;\n" +
			"  }\n" +
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=220788
public void test0470_while() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = new Object();\n" +
			"    while (bar()) {\n" +
			"      if (o != null && o.toString().equals(\"o\")) {\n" +
			"      }\n" +
			"    }\n" +
			"    if (o.toString().equals(\"o\")) {\n" +
			"    }\n" +
			"  }\n" +
			"  boolean bar() {\n" +
			"    return false;\n" +
			"  }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	if (o != null && o.toString().equals(\"o\")) {\n" +
		"	    ^\n" +
		"Redundant null check: The variable o cannot be null at this location\n" +
		"----------\n");
}
// null analysis -- try/finally
public void test0500_try_finally() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object m;\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    try { /* */ }\n" +
			"    finally {\n" +
			"      o = m;\n" +
			"    }\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
		"" // because finally assigns to unknown value
	);
}

// null analysis -- try/finally
public void test0501_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = new Object();\n" +
			"    try { /* */ }\n" +
			"    finally {\n" +
			"      o = null;\n" +
			"    }\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n", // because finally assigns to null
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
public void test0502_try_finally() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    try {\n" +
			"      System.out.println();\n" + // might throw a runtime exception
			"      o = new Object();\n" +
			"    }\n" +
			"    finally { /* */ }\n" +
			"    o.toString();\n" +
			    // still OK because in case of exception this code is
			    // not reached
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- try/finally
public void test0503_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(X x) {\n" +
			"    x = null;\n" +
			"    try {\n" +
			"      x = null;\n" +                // complain, already null
			"    } finally { /* */ }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	x = null;\n" +
		"	^\n" +
		"Redundant assignment: The variable x can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
public void test0504_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(X x) {\n" +
			"    x = null;\n" +
			"    try {\n" +
			"    } finally {\n" +
			"      if (x != null) { /* */ }\n" + // complain null
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	if (x != null) { /* */ }\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable x can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
// origin: AssignmentTest#test017
// The whole issue here is whether or not to detect premature exits.
// Previously, we followed JLS's conservative approach, which considers
// that the try block may exit before the assignment is completed.
// As of Bug 345305 this has been changed to a more accurate analysis.
public void test0505_try_finally() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" void foo(X x) {\n" +
			"   x = this;\n" + // 1
			"   try {\n" +
			"     x = null;\n" +
			"   } finally {\n" +
			"     if (x == null) {/* */}\n" + // 2
			"   }\n" +
			" }\n" +
			"}\n"},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	if (x == null) {/* */}\n" +
			"	    ^\n" +
			"Redundant null check: The variable x can only be null at this location\n" +
			"----------\n");
}

// null analysis -- try finally
public void test0506_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    try { /* */ }\n" +
			"    finally {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    if (o == null) { /* */ }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	if (o == null) { /* */ }\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 7)\n" +
		"	if (o == null) { /* */ }\n" +
		"	               ^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try finally
public void test0507_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    try { /* */ }\n" +
			"    finally {\n" +
			"      o.toString();\n" +  // protect
			"    }\n" +
			"    if (o == null) {\n" + // complain
			"      o = new Object();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	if (o == null) {\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 7)\n" +
		"	if (o == null) {\n" +
		"      o = new Object();\n" +
		"    }\n" +
		"	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try finally
public void test0508_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    o = null;" +
			"    try { /* */ }\n" +
			"    finally {\n" +
			"      o.toString();\n" +  // complain and protect
			"      o.toString();\n" +  // quiet
			"    }\n" +
			"    o.toString();\n" +  // quiet
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try finally
public void test0509_try_finally_embedded() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o1) {\n" +
			"    Object o2 = null;" +
			"    while (true) {\n" +
			"      // o2 = o1;\n" +
			"      try { /* */ }\n" +
			"      finally {\n" +
			"        o2.toString();\n" +  // complain and protect
			"        o2.toString();\n" +  // quiet
			"      }\n" +
			"      o2.toString();\n" +  // quiet
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	o2.toString();\n" +
		"	^^\n" +
		"Null pointer access: The variable o2 can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try finally
public void test0510_try_finally() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void bar() throws Exception {\n" +
			"    // empty\n" +
			"  }\n" +
			"  void foo(Object o, boolean b) throws Exception {\n" +
			"    try {\n" +
			"      bar();\n" +
			"      if (b) {\n" +
			"        o.toString();\n" +
			"      }\n" +
			"    }\n" +
			"    finally {\n" +
			"      if (o != null) {\n" +
			"          o.toString();\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- try finally
public void test0511_try_finally() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o1, boolean b) {\n" +
			"    Object o2 = null;\n" +
			"    if (b) {\n" +
			"      o2 = new Object();\n" +
			"    }\n" + 				// 0011
			"    try { /* */ }\n" +
			"    finally {\n" +
			"      o2 = o1;\n" + 		// 1011
			"    }\n" +
			"    o2.toString();\n" + 	// 1011 -- quiet
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- try/finally
public void test0512_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" void foo(X x) {\n" +
			"   x = null;\n" +
			"   try {\n" +
			"     x = new X();\n" +
			"   } finally {\n" +
			"     x.toString();\n" +
			"   }\n" +
			" }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	x.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable x may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=128547
public void test0513_try_finally() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" X bar() {\n" +
			"   return null;\n" +
			" }\n" +
			" Object foo() {\n" +
			"   X x = null;\n" +
			"   try {\n" +
			"     x = bar();\n" +
			"     x.toString();\n" +
			"     return x;\n" +
			"   } finally {\n" +
			"     if (x != null) {\n" +
			"       x.toString();\n" +
			"     }\n" +
			"   }\n" +
			" }\n" +
			"}\n"},
		"");
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=128547
// embedded variant 1
public void test0514_try_finally() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" X bar() {\n" +
			"   return null;\n" +
			" }\n" +
			" Object foo() {\n" +
			"   X x = null;\n" +
			"   try {\n" +
			"     try {\n" +
			"       x = bar();\n" +
			"       x.toString();\n" +
			"       return x;\n" +
			"     }\n" +
			"     finally {\n" +
			"     }\n" +
			"   }\n" +
			"   finally {\n" +
			"     if (x != null) {\n" +
			"       x.toString();\n" +
			"     }\n" +
			"   }\n" +
			" }\n" +
			"}\n"},
		"");
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=128547
// embedded variant 2
public void test0515_try_finally() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" X bar() {\n" +
			"   return null;\n" +
			" }\n" +
			" Object foo() {\n" +
			"   X x = null;\n" +
			"   try {\n" +
			"     try {\n" +
			"       x = bar();\n" +
			"       x.toString();\n" +
			"       return x;\n" +
			"     }\n" +
			"     finally {\n" +
			"       System.out.println();\n" +
			"     }\n" +
			"   }\n" +
			"   finally {\n" +
			"     if (x != null) {\n" +
			"       x.toString();\n" +
			"     }\n" +
			"   }\n" +
			" }\n" +
			"}\n"},
		"");
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=128547
// variant
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184546
// variant
public void test0516_try_finally() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" Object foo() {\n" +
			"   X x = null;\n" +
			"   try {\n" +
			"     x = new X();\n" +
			"     return x;\n" +
			"   }\n" +
			"   finally {\n" +
			"     if (x != null) {\n" +
			"       x.toString();\n" +
			"     }\n" +
			"   }\n" +
			" }\n" +
			"}\n"},
		"");
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=132072
// AIOOBE in null check compiling com.sun.org.apache.xalan.internal.res.XSLTErrorResources from JDK 1.5 source
public void test0517_try_finally() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" Object foo() {\n" +
			"   String s00, s01, s02, s03, s04, s05, s06, s07, s08, s09;\n" +
			"   String s10, s11, s12, s13, s14, s15, s16, s17, s18, s19;\n" +
			"   String s20, s21, s22, s23, s24, s25, s26, s27, s28, s29;\n" +
			"   String s30, s31, s32, s33, s34, s35, s36, s37, s38, s39;\n" +
			"   String s40, s41, s42, s43, s44, s45, s46, s47, s48, s49;\n" +
			"   String s50, s51, s52, s53, s54, s55, s56, s57, s58, s59;\n" +
			"   String s60, s61, s62, s63, s64, s65, s66, s67, s68, s69;\n" +
			"   String s100, s101, s102, s103, s104, s105, s106, s107, s108, s109;\n" +
			"   String s110, s111, s112, s113, s114, s115, s116, s117, s118, s119;\n" +
			"   String s120, s121, s122, s123, s124, s125, s126, s127, s128, s129;\n" +
			"   String s130, s131, s132, s133, s134, s135, s136, s137, s138, s139;\n" +
			"   String s140, s141, s142, s143, s144, s145, s146, s147, s148, s149;\n" +
			"   String s150, s151, s152, s153, s154, s155, s156, s157, s158, s159;\n" +
			"   String s160, s161, s162, s163, s164, s165, s166, s167, s168, s169;\n" +
			"   String s200, s201, s202, s203, s204, s205, s206, s207, s208, s209;\n" +
			"   String s210, s211, s212, s213, s214, s215, s216, s217, s218, s219;\n" +
			"   String s220, s221, s222, s223, s224, s225, s226, s227, s228, s229;\n" +
			"   String s230, s231, s232, s233, s234, s235, s236, s237, s238, s239;\n" +
			"   String s240, s241, s242, s243, s244, s245, s246, s247, s248, s249;\n" +
			"   String s250, s251, s252, s253, s254, s255, s256, s257, s258, s259;\n" +
			"   String s260, s261, s262, s263, s264, s265, s266, s267, s268, s269;\n" +
			"   X x = new X();\n" +
			"   try {\n" +
			"     return x;\n" +
			"   }\n" +
			"   finally {\n" +
			"   }\n" +
			" }\n" +
			"}\n"},
		"");
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=132120
// [compiler][null] NPE batch compiling JDT/Core from HEAD
public void test0518_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" void foo() {\n" +
			"   String s00, s01, s02, s03, s04, s05, s06, s07, s08, s09;\n" +
			"   String s10, s11, s12, s13, s14, s15, s16, s17, s18, s19;\n" +
			"   String s20, s21, s22, s23, s24, s25, s26, s27, s28, s29;\n" +
			"   String s30, s31, s32, s33, s34, s35, s36, s37, s38, s39;\n" +
			"   String s40, s41, s42, s43, s44, s45, s46, s47, s48, s49;\n" +
			"   String s50, s51, s52, s53, s54, s55, s56, s57, s58, s59;\n" +
			"   String s60, s61, s62, s63, s64, s65, s66, s67, s68, s69;\n" +
			"   String s100, s101, s102, s103, s104, s105, s106, s107, s108, s109;\n" +
			"   String s110, s111, s112, s113, s114, s115, s116, s117, s118, s119;\n" +
			"   String s120, s121, s122, s123, s124, s125, s126, s127, s128, s129;\n" +
			"   String s130, s131, s132, s133, s134, s135, s136, s137, s138, s139;\n" +
			"   String s140, s141, s142, s143, s144, s145, s146, s147, s148, s149;\n" +
			"   String s150, s151, s152, s153, s154, s155, s156, s157, s158, s159;\n" +
			"   String s160, s161, s162, s163, s164, s165, s166, s167, s168, s169;\n" +
			"   String s200, s201, s202, s203, s204, s205, s206, s207, s208, s209;\n" +
			"   String s210, s211, s212, s213, s214, s215, s216, s217, s218, s219;\n" +
			"   String s220, s221, s222, s223, s224, s225, s226, s227, s228, s229;\n" +
			"   String s230, s231, s232, s233, s234, s235, s236, s237, s238, s239;\n" +
			"   String s240, s241, s242, s243, s244, s245, s246, s247, s248, s249;\n" +
			"   String s250, s251, s252, s253, s254, s255, s256, s257, s258, s259;\n" +
			"   String s260, s261, s262, s263, s264, s265, s266, s267, s268, s269;\n" +
			"   X x = null;\n" +
			"   try {\n" +
			"     x = new X();\n" +
			"   } finally {\n" +
			"     x.toString();\n" +
			"   }\n" +
			" }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 28)\n" +
		"	x.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable x may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=128962
// incorrect analysis within try finally with a constructor throwing an exception
public void test0519_try_finally_constructor_exc() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo(Y y) throws E {\n" +
			"    try {\n" +
			"      new Y();\n" +
			"      y.toString();\n" + // should be quiet
			"    } finally {\n" +
			"      y = null;\n" +
			"    }\n" +
			"  }\n" +
			"}\n" +
			"class Y {\n" +
			"  Y() throws E {\n" +
			"  }\n" +
			"}\n" +
			"class E extends Exception {\n" +
			"  private static final long serialVersionUID = 1L;\n" +
			"}\n"},
		"");
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=128962
// incorrect analysis within try finally with a constructor throwing an exception
// variant
public void test0520_try_finally_constructor_exc() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo(Y y) throws E { \n" +
			"    try { \n" +
			"      new Y() {\n" +
			"          void bar() {\n" +
			"              // do nothing\n" +
			"          }\n" +
			"      }; \n" +
			"      y.toString();\n" +
			"    } finally { \n" +
			"      y = null; \n" +
			"    } \n" +
			"  } \n" +
			"}\n" +
			"abstract class Y {\n" +
			"  Y() throws E { \n" +
			"  }\n" +
			"  abstract void bar();\n" +
			"} \n" +
			"class E extends Exception {\n" +
			"  private static final long serialVersionUID = 1L;\n" +
			"}"},
		"");
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=149665
// incorrect analysis within try finally with an embedded && expression
public void test0521_try_finally() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X\n" +
			"{\n" +
			"  X m;\n" +
			"  public void foo() {\n" +
			"    for(int j = 0; j < 10; j++) {\n" +
			"      try {\n" +
			"        j++;\n" +
			"      } finally {\n" +
			"        X t = m;\n" +
			"        if( t != null && t.bar()) {\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"  boolean bar() {\n" +
			"    return false;\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=149665
// variant
public void test0522_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X\n" +
			"{\n" +
			"  X m;\n" +
			"  public void foo() {\n" +
			"    for(int j = 0; j < 10; j++) {\n" +
			"      try {\n" +
			"        j++;\n" +
			"      } finally {\n" +
			"        X t = null;\n" +
			"        if(t.bar()) {\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"  boolean bar() {\n" +
			"    return false;\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	if(t.bar()) {\n" +
		"	   ^\n" +
		"Null pointer access: The variable t can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=149665
// variant
public void test0523_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X\n" +
			"{\n" +
			"  X m;\n" +
			"  public void foo() {\n" +
			"    for(int j = 0; j < 10; j++) {\n" +
			"      try {\n" +
			"        j++;\n" +
			"      } finally {\n" +
			"        X t = m;\n" +
			"        if(t == null ? false : (t == null ? false : t.bar())) {\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"  boolean bar() {\n" +
			"    return false;\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	if(t == null ? false : (t == null ? false : t.bar())) {\n" +
		"	                        ^\n" +
		"Null comparison always yields false: The variable t cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=149665
// variant
public void test0524_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X\n" +
			"{\n" +
			"  X m;\n" +
			"  public void foo() {\n" +
			"    for(int j = 0; j < 10; j++) {\n" +
			"      try {\n" +
			"        j++;\n" +
			"      } finally {\n" +
			"        X t = m;\n" +
			"        if(t != null ? false : (t == null ? false : t.bar())) {\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"  boolean bar() {\n" +
			"    return false;\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	if(t != null ? false : (t == null ? false : t.bar())) {\n" +
		"	                        ^\n" +
		"Redundant null check: The variable t can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=150082
public void _test0525_try_finally_unchecked_exception() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X\n" +
			"{\n" +
			"  String foo(Object p) {\n" +
			"    String s = null;\n" +
			"    Object o = null;\n" +
			"    try {\n" +
			"        o = p;\n" +
			"        if (o == null) {\n" +
			"          return null;\n" +
			"        }\n" +
			"        s = o.getClass().getName();\n" +
			"    } catch (RuntimeException e) {\n" +
			"            o.toString();\n" +
			"            s = null;\n" +
			"    } finally {\n" +
			"      if (o != null) {\n" +
			"      }\n" +
			"    }\n" +
			"    return s;\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 13)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=150082
// variant
public void test0526_try_finally_unchecked_exception() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X\n" +
			"{\n" +
			"  String foo(Object p) {\n" +
			"    String s = null;\n" +
			"    Object o = p;\n" +
			"    try {\n" +
			"        if (o == null) {\n" +  // shades doubts upon o
			"          return null;\n" +	// may throw a RuntimeException by spec
			"        }\n" +
			"        s = o.getClass().getName();\n" +
			"    } catch (RuntimeException e) {\n" +
			"            o.toString();\n" +
			"            s = null;\n" +
			"    } finally {\n" +
			"      if (o != null) {\n" +
			"      }\n" +
			"    }\n" +
			"    return s;\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

//null analysis -- try/finally
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=150082
//variant
public void test0527_try_finally_unchecked_exception() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X\n" +
			"{\n" +
			"  String foo(Object p) {\n" +
			"    String s = null;\n" +
			"    Object o = p;\n" +
			"    try {\n" +
			"        if (o == null) {\n" +  // shades doubts upon o
			"          return null;\n" +	// may throw a RuntimeException by spec
			"        }\n" +
			"        s = o.getClass().getName();\n" +
			"    } catch (RuntimeException e) {\n" +
			"            o.toString();\n" +
			"            s = null;\n" +
			"    }\n" +
			"    return s;\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=158000
public void test0528_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(X x) {\n" +
			"    x = null;\n" +
			"    X y = null;\n" +
			"    try {\n" +
			"    } finally {\n" +
			"      if (x != null) { /* */ }\n" + // complain null
			"      if (y != null) { /* */ }\n" + // complain null as well
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	if (x != null) { /* */ }\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable x can only be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	if (y != null) { /* */ }\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable y can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=158000
public void test0529_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    o = null;\n" +
			"    Object o2 = null;\n" +
			"    try { /* */ }\n" +
			"    finally {\n" +
			"      o.toString();\n" +  // complain
			"      o2.toString();\n" + // complain
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	o2.toString();\n" +
		"	^^\n" +
		"Null pointer access: The variable o2 can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=158000
public void test0530_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" void foo(X x) {\n" +
			"   x = null;\n" +
			"   X y = null;\n" +
			"   try {\n" +
			"     x = new X();\n" +
			"   } finally {\n" +
			"     x.toString();\n" +
			"     y.toString();\n" + // complain
			"   }\n" +
			" }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	x.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable x may be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	y.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable y can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=158000
public void test0531_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" void foo() {\n" +
			"   X x = new X();\n" +
			"   X y = null;\n" +
			"   try {\n" +
			"   } finally {\n" +
			"     if (x != null) {\n" +
			"       x.toString();\n" +
			"     }\n" +
			"     y.toString();\n" + // complain
			"   }\n" +
			" }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	if (x != null) {\n" +
		"	    ^\n" +
		"Redundant null check: The variable x cannot be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	y.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable y can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=177863
public void test0532_try_finally() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() {\n" +
			"    Object o = null;\n" +
			"    try {\n" +
			"    } finally {\n" +
			"      o = Object.class.getClass();\n" +
			"      o.getClass();\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184546
public void test0533_try_finally_field() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" static char SHOULD_NOT_MATTER = '?';\n" +
			" Object foo() {\n" +
			"   X x = null;\n" +
			"   try {\n" +
			"     x = new X();\n" +
			"     return x;\n" +
			"   }\n" +
			"   finally {\n" +
			"     if (x != null) {\n" +
			"       x.toString();\n" +
			"     }\n" +
			"   }\n" +
			" }\n" +
			"}\n"},
		"");
}

// null analysis - try finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=198970
public void _test0534_try_finally() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"    String foo = null;\n" +
			"    boolean a = true;\n" +
			"    try {\n" +
			"    }\n" +
			"    catch(Exception e) {\n" +
			"    }\n" +
			"    finally {\n" +
			"      if (a) {\n" +
			"        foo = new String();\n" +
			"      }\n" +
			"      if (foo != null) {\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n"
			},
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		"" /* expectedErrorString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		false /* skipJavac */);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=295260
public void test0535_try_finally() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public void test3(String[] args) {\n" +
				"		while (true) {\n" +
				"			Object a = null;\n" +
				"			try {\n" +
				"				a = new Object();\n" +
				"			} catch (Exception e) {\n" +
				"			} finally {\n" +
				"				if (a != null)\n" +
				"					a = null;\n" +	// quiet
				"			}\n" +
				"		}\n" +
				"	}\n"+
				"}",
			},
			"");
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=320170 -  [compiler] [null] Whitebox issues in null analysis
// trigger nullbits 0111 (pot n|nn|un), don't let "definitely unknown" override previous information
public void test0536_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" X bar () { return null; }\n" +
			" void foo() {\n" +
			"   X x = new X();\n" +
			"   try {\n" +
			"     x = null;\n" +
			"     x = new X();\n" +  // if this throws an exception finally finds x==null
			"     x = bar();\n" +
			"   } finally {\n" +
			"     x.toString();\n" + // complain
			"   }\n" +
			" }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	x.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable x may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=320170 -  [compiler] [null] Whitebox issues in null analysis
// trigger nullbits 0111 (pot n|nn|un), don't let "definitely unknown" override previous information
// multiple variables
public void test0537_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" X bar () { return null; }\n" +
			" void foo() {\n" +
			"   X x1 = new X();\n" +
			"   X x2 = new X();\n" +
			"   X x3 = new X();\n" +
			"   try {\n" +
			"     x1 = null;\n" +
			"     x2 = null;\n" +
			"     x1 = new X();\n" +  // if this throws an exception finally finds x1==null
			"     x2 = new X();\n" +  // if this throws an exception finally finds x2==null
			"     x3 = new X();\n" +  // if this throws an exception finally still finds x3!=null
			"     x1 = bar();\n" +
			"     x2 = bar();\n" +
			"   } finally {\n" +
			"     x1.toString();\n" + // complain
			"     x2.toString();\n" + // complain
			"     x3.toString();\n" + // don't complain
			"   }\n" +
			" }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 16)\n" +
		"	x1.toString();\n" +
		"	^^\n" +
		"Potential null pointer access: The variable x1 may be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 17)\n" +
		"	x2.toString();\n" +
		"	^^\n" +
		"Potential null pointer access: The variable x2 may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/catch
public void test0550_try_catch() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    try {\n" +
			"      System.out.println();\n" +  // might throw a runtime exception
			"      o = new Object();\n" +
			"    }\n" +
			"    catch (Throwable t) {\n" + // catches everything
			"      return;\n" +             // gets out
			"    }\n" +
			"    o.toString();\n" +         // non null
			"  }\n" +
			"}\n"},
		"");
}

// null analysis - try/catch
public void test0551_try_catch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo() {\n" +
			"    Object o = new Object();\n" +
			"    try {\n" +
			"      System.out.println();\n" +
			"      if (dummy) {\n" +
			"        o = null;\n" +
			"        throw new Exception();\n" +
			"      }\n" +
			"    }\n" +
			"    catch (Exception e) {\n" +
			"      o.toString();\n" + // complain
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 13)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - try/catch
public void test0552_try_catch() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo() throws Exception {\n" +
			"    Object o = new Object();\n" +
			"    try {\n" +
			"      if (dummy) {\n" +
			"        o = null;\n" +
			"        throw new Exception();\n" +
			"      }\n" +
			"    }\n" +
			"    catch (Exception e) {\n" +
			"    }\n" +
			"    if (o != null) {\n" +
			  // quiet: get out of try either through normal flow, leaves a new
			  // object, or through Exception, leaves a null
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis - try/catch
public void test0553_try_catch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy, other;\n" +
			"  void foo() {\n" +
			"    Object o = new Object();\n" +
			"    try {\n" +
			"      if (dummy) {\n" +
			"        if (other) {\n" +
			"          throw new LocalException();\n" + // may launch new exception
			"        }\n" +
			"        o = null;\n" +
			"        throw new LocalException();\n" + // must launch new exception
			"      }\n" +
			"    }\n" +
			"    catch (LocalException e) {\n" +
			"      o.toString();\n" + // complain
			"    }\n" +
			"  }\n" +
			"  class LocalException extends Exception {\n" +
			"    private static final long serialVersionUID = 1L;\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 15)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - try/catch
public void test0554_try_catch() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) throws Exception {\n" +
			"    try {\n" +
			"      o = null;\n" +
			"      throwLocalException();\n" +
			"      throw new Exception();\n" +
			"    }\n" +
			"    catch (LocalException e) {\n" +
			"    }\n" +
			"    if (o != null) {\n" +
			  // complain: only way to get out of try and get there is to go
			  // through throwLocalException, after the assignment
			"    }\n" +
			"  }\n" +
			"  class LocalException extends Exception {\n" +
			"    private static final long serialVersionUID = 1L;\n" +
			"  }\n" +
			"  void throwLocalException() throws LocalException {\n" +
			"    throw new LocalException();\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	if (o != null) {\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o can only be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 10)\n" +
		"	if (o != null) {\n" +
		"    }\n" +
		"	               ^^^^^^^\n" +
		"Dead code\n" +
		"----------\n"
	);
}

// null analysis - try/catch
public void test0555_try_catch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = new Object();\n" +
			"    try {\n" +
			"      o = null;\n" +
			"      throwException();\n" +
			"    }\n" +
			"    catch (Exception e) {\n" +
			"      o.toString();\n" + // complain NPE
			"    }\n" +
			"  }\n" +
			"  void throwException() throws Exception {\n" +
			"    throw new Exception();\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - try/catch
public void test0556_try_catch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = new Object();\n" +
			"    try {\n" +
			"      o = null;\n" +
			"      throwException();\n" +
			"    }\n" +
			"    catch (Throwable t) {\n" +
			"      o.toString();\n" + // complain NPE
			"    }\n" +
			"  }\n" +
			"  void throwException() throws Exception {\n" +
			"    throw new Exception();\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - try/catch
public void test0557_try_catch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo() {\n" +
			"    Object o = new Object();\n" +
			"    try {\n" +
			"      if (dummy) {\n" +
			"        o = null;\n" +
			"        throw new Exception();\n" +
			"      }\n" +
			"    }\n" +
			"    catch (Exception e) {\n" +
			"      o.toString();\n" + // complain NPE
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - try/catch
public void test0558_try_catch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo() {\n" +
			"    Object o = new Object();\n" +
			"    try {\n" +
			"      if (dummy) {\n" +
			"        System.out.print(0);\n" + // may thow RuntimeException
			"        o = null;\n" +
			"        throw new LocalException();\n" +
			"      }\n" +
			"    }\n" +
			"    catch (LocalException e) {\n" + // doesn't catch RuntimeException
			"      o.toString();\n" + // complain NPE
			"    }\n" +
			"  }\n" +
			"  class LocalException extends Exception {\n" +
			"    private static final long serialVersionUID = 1L;\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 13)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - try/catch
public void test0559_try_catch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo() {\n" +
			"    Object o = new Object();\n" +
			"    try {\n" +
			"      if (dummy) {\n" +
			"        o = null;\n" +
			"        throw new SubException();\n" +
			"      }\n" +
			"    }\n" +
			"    catch (LocalException e) {\n" + // must catch SubException
			"      o.toString();\n" + // complain NPE
			"    }\n" +
			"  }\n" +
			"  class LocalException extends Exception {\n" +
			"    private static final long serialVersionUID = 1L;\n" +
			"  }\n" +
			"  class SubException extends LocalException {\n" +
			"    private static final long serialVersionUID = 1L;\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - try/catch
public void test0560_try_catch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Class bar(boolean b) throws ClassNotFoundException {\n" +
			"    if (b) {\n" +
			"      throw new ClassNotFoundException();\n" +
			"    }\n" +
			"    return null;\n" +
			"  }\n" +
			"  public Class foo(Class c, boolean b) {\n" +
			"    if (c != null)\n" +
			"      return c;\n" +
			"    if (b) {\n" +
			"      try {\n" +
			"        c = bar(b);\n" +
			"        return c;\n" +
			"      } catch (ClassNotFoundException e) {\n" +
			"      // empty\n" +
			"      }\n" +
			"    }\n" +
			"    if (c == null) { // should complain: c can only be null\n" +
			"    }\n" +
			"    return c;\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 19)\n" +
		"	if (c == null) { // should complain: c can only be null\n" +
		"	    ^\n" +
		"Redundant null check: The variable c can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - try/catch
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=130359
public void test0561_try_catch_unchecked_exception() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    try {\n" +
			"      o = bar();\n" +
			"    } catch (RuntimeException e) {\n" +
			"      o.toString();\n" + // may be null
			"    }\n" +
			"  }\n" +
			"  private Object bar() {\n" +
			"    return new Object();\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - try/catch
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=150854
// (slightly different) variant of 561
public void test0562_try_catch_unchecked_exception() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	this.runNegativeTest(
		true,
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"public class X {\n" +
			"  void foo() {\n" +
			"    LineNumberReader o = null;\n" +
			"    try {\n" +
			"      o = new LineNumberReader(new FileReader(\"dummy\"));\n" +
			"    } catch (NumberFormatException e) {\n" +
			"      o.toString();\n" + // may be null
			"    } catch (IOException e) {\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
			null,
			options,
			"----------\n" +
			"1. WARNING in X.java (at line 6)\n" +
			"	o = new LineNumberReader(new FileReader(\"dummy\"));\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Potential resource leak: \'o\' may not be closed\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 8)\n" +
			"	o.toString();\n" +
			"	^\n" +
			"Potential null pointer access: The variable o may be null at this location\n" +
			"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - try/catch
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=155117
public void test0563_try_catch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo(boolean b) {\n" +
			"    Exception ex = null;\n" +
			"    if (b) {\n" +
			"      try {\n" +
			"        System.out.println();\n" +
			"        return;\n" +
			"      } catch (Exception e) {\n" +
			"        ex = e;\n" +
			"      }\n" +
			"    }\n" +
			"    else {\n" +
			"      try {\n" +
			"        System.out.println();\n" +
			"        return;\n" +
			"      } catch (Exception e) {\n" +
			"        ex = e;\n" +
			"      }\n" +
			"    }\n" +
			"    if (ex == null) {\n" + // complain: ex cannot be null\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 20)\n" +
		"	if (ex == null) {\n" +
		"	    ^^\n" +
		"Null comparison always yields false: The variable ex cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 20)\n" +
		"	if (ex == null) {\n" +
		"    }\n" +
		"	                ^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - try/catch
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=150854
public void test0564_try_catch_unchecked_exception() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static Object foo() {\n" +
			"    Object result = null;\n" +
			"    try {\n" +
			"      result = new Object();\n" +
			"    } catch (Exception e) {\n" +
			"      result = null;\n" +
			"    }\n" +
			"    return result;\n" +
			"  }\n" +
			"}"},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null /* no vm arguments */,
		options,
		null /* no custom requestor*/,
	  	false /* do not skip javac for this peculiar test */);
}

// null analysis - try/catch
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=150854
// variant
public void test0565_try_catch_unchecked_exception() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static Object foo() {\n" +
			"    Object result = null;\n" +
			"    try {\n" +
			"      result = new Object();\n" +
			"      result = new Object();\n" +
			"    } catch (Exception e) {\n" +
			"      result = null;\n" +
			"    }\n" +
			"    return result;\n" +
			"  }\n" +
			"}"},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null /* no vm arguments */,
		options,
		null /* no custom requestor*/,
	  	false /* do not skip javac for this peculiar test */);
}

// null analysis - try/catch
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=150854
// variant
public void test0566_try_catch_unchecked_exception() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static Object foo(Y y) {\n" +
			"    Object result = null;\n" +
			"    try {\n" +
			"      while (y.next()) {\n" +
			"        result = y.getObject();\n" +
			"      }\n" +
			"    } catch (Exception e) {\n" +
			"      result = null;\n" +
			"    }\n" +
			"    return result;\n" +
			"  }\n" +
			"}\n" +
			"class Y {\n" +
			"  boolean next() {\n" +
			"    return false;\n" +
			"  }\n" +
			"  Object getObject() {\n" +
			"    return null;\n" +
			"  }\n" +
			"}"},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null /* no vm arguments */,
		options,
		null /* no custom requestor*/,
	  	false /* do not skip javac for this peculiar test */);
}

// null analysis - try/catch for checked exceptions
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=295260
public void test0567_try_catch_checked_exception() {
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.net.MalformedURLException;\n" +
				"import java.net.URL;\n" +
				"public class X {\n" +
				"	public void test1(String[] args) {\n" +
				"		URL[] urls = null;\n" +
				"		try	{\n" +
				"			urls = new URL[args.length];\n" +
				"			for (int i = 0; i < args.length; i++)\n" +
				"				urls[i] = new URL(\"http\", \"\", -1, args[i]);\n" +
				"		}\n" +
				"		catch (MalformedURLException mfex) {\n" +
				"			urls = null;\n" +	// quiet
				"		}\n" +
				"	}\n" +
				"}",
			},
			"");
}

// null analysis - try/catch for checked exceptions with finally block
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=295260
public void test0568_try_catch_checked_exception() {
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.net.MalformedURLException;\n" +
				"import java.net.URL;\n" +
				"public class X {\n" +
				"	public void test1(String[] args) {\n" +
				"		URL[] urls = null;\n" +
				"		try	{\n" +
				"			urls = new URL[args.length];\n" +
				"			for (int i = 0; i < args.length; i++)\n" +
				"				urls[i] = new URL(\"http\", \"\", -1, args[i]);\n" +
				"		}\n" +
				"		catch (MalformedURLException mfex) {\n" +
				"			urls = null;\n" +	// quiet
				"		}\n" +
				" 		finally{\n"+
				"			System.out.println(\"complete\");\n" +
				"		}\n" +
				"	}\n" +
				"}",
			},
			"");
}
// null analysis -- try/catch
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=302446
public void test0569_try_catch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"	 int i;\n" +
			"	 if (o == null)\n" +	// redundant check
			"	 	i = 0;\n" +
			"    try {\n" +
			"      System.out.println(i);\n" +  // might throw a runtime exception
			"      o = new Object();\n" +
			"	   throw new Exception(\"Exception thrown from try block\");\n" +
			"    }\n" +
			"    catch (Throwable t) {\n" + // catches everything
			"      return;\n" +             // gets out
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	if (o == null)\n" +
		"	    ^\n" +
		"Redundant null check: The variable o can only be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	System.out.println(i);\n" +
		"	                   ^\n" +
		"The local variable i may not have been initialized\n" +
		"----------\n");
}
// null analysis -- try/catch
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=302446
public void test0570_try_catch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"	 int i;\n" +
			"	 if (o == null)\n" +	// redundant check
			"	 	i = 0;\n" +
			"    try {\n" +
			"      System.out.println();\n" +  // might throw a runtime exception
			"      o = new Object();\n" +
			"	   if (o != null)\n" +		// redundant check
			"			i = 1\n;" +
			"		throw new Exception(\"Exception thrown from try block\");\n" +
			"    }\n" +
			"    catch (Exception e) {\n" +
			"      if(i == 0)\n" +
			"			System.out.println(\"o was initialised\");\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	if (o == null)\n" +
		"	    ^\n" +
		"Redundant null check: The variable o can only be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	if (o != null)\n" +
		"	    ^\n" +
		"Redundant null check: The variable o cannot be null at this location\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 15)\n" +
		"	if(i == 0)\n" +
		"	   ^\n" +
		"The local variable i may not have been initialized\n" +
		"----------\n");
}
//null analysis -- try/catch
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=302446
public void test0571_try_catch_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"	 int i;\n" +
			"	 if (o == null)\n" +	// redundant check
			"	 	i = 0;\n" +
			"    try {\n" +
			"      o = new Object();\n" +
			"	   i = 1\n;" +
			"	   throw new Exception(\"Exception thrown from try block\");\n" +
			"    }\n" +
			"    catch (Exception e) {\n" +
			"      if(o == null)\n" +
			"			o = new Object();\n" +
			"	   i = 1;\n" +
			"    }\n" +
			"	 finally {\n" +
			"		if (i==1) {\n" +
			"	 		System.out.println(\"Method ended with o being initialised\");\n" +
			"		System.out.println(o.toString());\n" +	// may be null
			"		} \n" +
			"	 }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	if (o == null)\n" +
		"	    ^\n" +
		"Redundant null check: The variable o can only be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 18)\n" +
		"	if (i==1) {\n" +
		"	    ^\n" +
		"The local variable i may not have been initialized\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 20)\n" +
		"	System.out.println(o.toString());\n" +
		"	                   ^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n");
}
//null analysis -- if statement
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=302446
public void test0572_if_statement() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo() {\n" +
			"		Object o = null;\n" +
			"		int i;\n" +
			"		if (o == null) // redundant check\n" +
			"			i = 0;\n" +
			"		System.out.println(i);\n" +
			"	}\n" +
			"}\n" +
			""},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	if (o == null) // redundant check\n" +
		"	    ^\n" +
		"Redundant null check: The variable o can only be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	System.out.println(i);\n" +
		"	                   ^\n" +
		"The local variable i may not have been initialized\n" +
		"----------\n");
}

// take care for Java7 changes
public void test0573_try_catch_unchecked_and_checked_exception() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    try {\n" +
			"		bar();\n" +
			"		o = new Object();\n" +
			"    } catch (IOException e) {\n" +
			"		o.toString();\n" +
			"    } catch(RuntimeException e) {\n" +
			"       o.toString();\n" + // may be null
			"    }\n" +
			"  }\n" +
			"  private Object bar() throws IOException{\n" +
			"    return new Object();\n" +
			"  }\n" +
			"}\n"},
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	o.toString();\n" +
			"	^\n" +
			"Null pointer access: The variable o can only be null at this location\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 11)\n" +
			"	o.toString();\n" +
			"	^\n" +
			"Potential null pointer access: The variable o may be null at this location\n" +
			"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// similar to test0573 using multi catch parameters
public void test0574_try_multi_catch_unchecked_and_checked_exception() {
	if (this.complianceLevel >=  ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.IOException;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"    Object o = null;\n" +
				"    try {\n" +
				"		bar();\n" +
				"		o = new Object();\n" +
				"    } catch (IOException | RuntimeException e) {\n" +
				"		o.toString();\n" +
				"    }\n" +
				"  }\n" +
				"  private Object bar() throws IOException{\n" +
				"    return new Object();\n" +
				"  }\n" +
				"}\n"},
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	o.toString();\n" +
			"	^\n" +
			"Potential null pointer access: The variable o may be null at this location\n" +
			"----------\n",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}
//multi catch variant of test0561_try_catch_unchecked_exception
public void test0575_try_multi_catch_finally_unchecked_and_checked_exception() {
	if (this.complianceLevel >=  ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.IOException;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"    Object o = null;\n" +
				"    try {\n" +
				"      o = bar();\n" +
				"    } catch (IOException | RuntimeException e) {\n" +
				"      o.toString();\n" + // may be null
				"    } finally {}\n" +
				"  }\n" +
				"  private Object bar() throws IOException{\n" +
				"    return new Object();\n" +
				"  }\n" +
				"}\n"},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	o.toString();\n" +
			"	^\n" +
			"Potential null pointer access: The variable o may be null at this location\n" +
			"----------\n",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// null test for resources inside try with resources statement
public void test0576_try_with_resources() {
	if (this.complianceLevel >=  ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.FileInputStream;\n" +
				"import java.io.IOException;\n" +
				"import java.io.FileNotFoundException;\n" +
				"class MyException extends Exception {}\n" +
				"public class X {\n" +
				"   static void m(int n) throws IllegalArgumentException, MyException {}\n" +
				"   void foo(String name, boolean b) throws FileNotFoundException, IOException{\n" +
				"    FileInputStream fis;\n" +
				"	 if (b) fis = new FileInputStream(\"\");\n" +
				"	 else fis = null;\n" +
				"    try (FileInputStream fis2 = fis; FileInputStream fis3 = fis2; FileInputStream fis4 = null) {\n" +
				"		fis = new FileInputStream(\"\");\n" +
				"		fis2.available();\n" +	// may be null since fis may be null
				"		fis3.close();\n" +
				"		fis4.available();\n" +	// will always be null
				"		m(1);\n" +
				"    } catch (IllegalArgumentException e) {\n" +
				"      fis.available();\n" + // may be null
				"    } catch (MyException e) {\n" +
				"      fis.available();\n" + // cannot be null
				"    } finally {}\n" +
				"  }\n" +
				"}\n"},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	class MyException extends Exception {}\n" +
		"	      ^^^^^^^^^^^\n" +
		"The serializable class MyException does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 13)\n" +
		"	fis2.available();\n" +
		"	^^^^\n" +
		"Potential null pointer access: The variable fis2 may be null at this location\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 14)\n" +
		"	fis3.close();\n" +
		"	^^^^\n" +
		"Potential null pointer access: The variable fis3 may be null at this location\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 15)\n" +
		"	fis4.available();\n" +
		"	^^^^\n" +
		"Null pointer access: The variable fis4 can only be null at this location\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 18)\n" +
		"	fis.available();\n" +
		"	^^^\n" +
		"Potential null pointer access: The variable fis may be null at this location\n" +
		"----------\n",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// null analysis - throw
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=201182
public void test0595_throw() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) throws Throwable {\n" +
			"    Throwable t = null;\n" +
			"    throw t;\n" +
			"  }\n" +
			"}\n"
			},
		true /* expectingCompilerErrors */,
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	throw t;\n" +
		"	      ^\n" +
		"Null pointer access: The variable t can only be null at this location\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		"" /* expectedErrorString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// null analysis - throw
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=201182
// variant - potential NPE
public void test0596_throw() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) throws Throwable {\n" +
			"    Throwable t = null;\n" +
			"    if (args.length > 0) {\n" +
			"      t = new Throwable();\n" +
			"    }\n" +
			"    throw t;\n" +
			"  }\n" +
			"}\n"
			},
		true /* expectingCompilerErrors */,
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	throw t;\n" +
		"	      ^\n" +
		"Potential null pointer access: The variable t may be null at this location\n" +
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		"" /* expectedErrorString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}


// null analysis - throw
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=201182
// variant - unknown
public void test0597_throw() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() throws Throwable {\n" +
			"    throw t();\n" +
			"  }\n" +
			"  Throwable t() {\n" +
			"    return new Throwable();\n" +
			"  }\n" +
			"}\n"
			},
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		"" /* expectedErrorString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// null analysis -- do while
public void test0601_do_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    do {/* */}\n" +
			"    while (o.toString() != null);\n" +
			      // complain: NPE
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	while (o.toString() != null);\n" +
		"	       ^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- do while
public void test0602_do_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    do {/* */}\n" +
			"    while (o != null);\n" +
			  // complain: get o null first time and forever
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	while (o != null);\n" +
		"	       ^\n" +
		"Null comparison always yields false: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- do while
public void test0603_do_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    do {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    while (o == null);\n" +
			      // complain: set it to non null before test, for each iteration
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	while (o == null);\n" +
		"	       ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- do while
public void test0604_do_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    do {\n" +
			"      if (System.currentTimeMillis() > 10L) {\n" +
			"        o = new Object();\n" +
			"      }\n" +
			"    }\n" +
			"    while (o == null);\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- do while
public void test0605_do_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo(Object o) {\n" +
			"    o = null;\n" +
			"    do {\n" +
			"      // do nothing\n" +
			"    }\n" +
			"    while (dummy || o != null);\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	while (dummy || o != null);\n" +
		"	                ^\n" +
		"Null comparison always yields false: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- do while
public void test0606_do_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null,\n" +
			"           u = new Object(),\n" +
			"           v = new Object();\n" +
			"    do {\n" +
			"      if (v == null) {\n" +
			"        o = new Object();\n" +
			"      };\n" +
			"      if (u == null) {\n" +
			"        v = null;\n" +
			"      };\n" +
			"      u = null;\n" +
			"    }\n" +
			"    while (o == null);\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- do while
public void test0607_do_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    do {\n" +
			"      o.toString();\n" +
			         // complain: NPE
			"      o = new Object();\n" +
			"    }\n" +
			"    while (dummy);\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- do while
public void test0608_do_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    do {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    while (dummy);\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- do while
public void test0609_do_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    do { /* */ }\n" +
			"    while (dummy);\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - do while
public void test0610_do_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  X bar() {\n" +
			"    return null;\n" +
			"  }\n" +
			"  void foo(X x) {\n" +
			"    x.bar();\n" +
			"    do {\n" +
			"      x = x.bar();\n" +
			"    } while (x != null);\n" + // quiet
			"  }\n" +
			"}\n"},
		"");
}

// null analysis - do while
public void test0611_do_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  X bar() {\n" +
			"    return new X();\n" +
			"  }\n" +
			"  void foo(Object o) {\n" +
			"    do {\n" +
			"      o = bar();\n" +
			"    } while (o == null);\n" +
			"    if (o != null) { /* */ }\n" + // complain
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	if (o != null) { /* */ }\n" +
		"	    ^\n" +
		"Redundant null check: The variable o cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// the problem here is that a single pass cannot know for the return
// embedded into the if; prior approach did use the upstream flow
// info to catch this, but this is inappropriate in many cases (eg
// test0606)
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=123399
public void _test0612_do_while() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object doubt) {\n" +
			"    Object o = null;\n" +
			"    do {\n" +
			"      if (o == null) {\n" +
			"        return;\n" +
			"      }\n" +
			"      o = doubt;\n" +
			"    } while (true);\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	if (o == null) {\n" +
		"	    ^\n" +
		"Redundant null check: The variable o can only be null at this location\n" +
		"----------\n"
	);
}

// null analysis - do while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=147118
public void test0613_do_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  String f;\n" +
			"  void foo (boolean b) {\n" +
			"    X x = new X();\n" +
			"    do {\n" +
			"      System.out.println(x.f);\n" +
			"      if (b) {\n" +
			"        x = null;\n" +
			"      }\n" +
			"    } while (x != null);\n" +
			"  }\n" +
			"}\n"},
		"");
}


// https://bugs.eclipse.org/bugs/show_bug.cgi?id=123399
// variant
public void _test0614_do_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object doubt) {\n" +
			"    Object o = null;\n" +
			"    exit: do {\n" +
			"      if (o == null) {\n" +
			"        continue exit;\n" +
			"      }\n" +
			"      o = doubt;\n" +
			"    } while (true);\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	if (o == null) {\n" +
		"	    ^\n" +
		"Redundant null check: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=123399
// variant
public void _test0615_do_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object doubt) {\n" +
			"    Object o = null;\n" +
			"    do {\n" +
			"      if (o == null) {\n" +
			"        throw new RuntimeException();\n" +
			"      }\n" +
			"      o = doubt;\n" +
			"    } while (true);\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	if (o == null) {\n" +
		"	    ^\n" +
		"Redundant null check: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - do while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=176472
// variant
public void test0616_do_while_explicit_label() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(int i) {\n" +
			"    Object o = null;\n" +
			"    done: do {\n" +
			"      switch (i) {\n" +
			"        case 0:\n" +
			"          o = new Object();\n" +
			"          break;\n" +
			"        case 1:\n" +
			"          break done;\n" +
			"      }\n" +
			"    } while (true);\n" +
			"    if (o == null) {\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis - do while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=176472
// variant
public void test0617_do_while_explicit_label() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean test() {\n" +
			"    return true;\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    done: do {\n" +
			"      if (test()) {\n" +
			"        break done;\n" +
			"      }\n" +
			"      o = new Object();\n" +
			"    } while (true);\n" +
			"    if (o == null) {\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis - do while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184298
// variant
public void test0618_do_while_infinite() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void test(String[] a) {\n" +
			"    String key = null;\n" +
			"    do {\n" +
			"      if (a[0] == null)\n" +
			"        break;\n" +
			"      key = a[0];\n" +
			"    } while (true);\n" +
			"    if (key != null) {\n" +
			"      // empty\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - do while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184298
// variant
public void test0619_do_while_infinite() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void test(String[] a) {\n" +
			"    String key = null;\n" +
			"    loop: do {\n" +
			"      if (a[0] == null)\n" +
			"        break loop;\n" +
			"      key = a[0];\n" +
			"    } while (true);\n" +
			"    if (key != null) {\n" +
			"      // empty\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis -- for
public void test0701_for() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    for (;o.toString() != null;) {/* */}\n" +
			      // complain: NPE
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	for (;o.toString() != null;) {/* */}\n" +
		"	      ^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- for
public void test0702_for() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    for (;o != null;) {/* */}\n" +
			  // complain: get o null first time and forever
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	for (;o != null;) {/* */}\n" +
		"	      ^\n" +
		"Null comparison always yields false: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- for
public void test0703_for() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    for (;o == null;) {\n" +
			      // quiet: first iteration is sure to find it null,
			      // but other iterations may change it
			"      o = new Object();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- for
public void test0704_for() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    for (;o == null;) {\n" +
			     // quiet: first iteration is sure to find it null,
			     // but other iterations may change it
			"      if (System.currentTimeMillis() > 10L) {\n" +
			"        o = new Object();\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- for
public void test0705_for() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean bar() {\n" +
			"    return true;\n" +
			"  }\n" +
			"  void foo(Object o) {\n" +
			"    for (;bar() && o == null;) {\n" +
			"      o.toString();\n" + // complain: NPE because of condition
			"      o = new Object();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- for
public void test0707_for() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    for (;o == null; o.toString()) {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- for
public void test0708_for() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    for (;o == null; o.toString()) {\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	for (;o == null; o.toString()) {\n" +
		"	                 ^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- for
public void test0709_for() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    for (o.toString(); o == null;) { /* */ }\n" + // complain: protected then unchanged
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	for (o.toString(); o == null;) { /* */ }\n" +
		"	                   ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- for
public void test0710_for() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean bar() {\n" +
			"    return true;\n" +
			"  }\n" +
			"  void foo(Object o) {\n" +
			"    o = null;\n" +
			"    for (o.toString(); bar();) {\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	for (o.toString(); bar();) {\n" +
		"	     ^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- for
public void test0711_for() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    Object t[] = null;\n" +
				"    for (Object o : t) {/* */}\n" +
				      // complain: NPE
				"  }\n" +
				"}\n"},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	for (Object o : t) {/* */}\n" +
			"	                ^\n" +
			"Null pointer access: The variable t can only be null at this location\n" +
			"----------\n",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// null analysis -- for
public void test0712_for() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    Iterable i = null;\n" +
				"    for (Object o : i) {/* */}\n" +
				      // complain: NPE
				"  }\n" +
				"}\n"},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	for (Object o : i) {/* */}\n" +
			"	                ^\n" +
			"Null pointer access: The variable i can only be null at this location\n" +
			"----------\n",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// null analysis -- for
public void test0713_for() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    Object t[] = new Object[1];\n" +
				"    for (Object o : t) {/* */}\n" +
				"  }\n" +
				"}\n"},
			"");
	}
}

// null analysis -- for
public void test0714_for() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    Iterable i = new java.util.Vector<Object>();\n" +
				"    for (Object o : i) {/* */}\n" +
				"  }\n" +
				"}\n"},
			"");
	}
}

// null analysis -- for
public void test0715_for() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    Iterable i = new java.util.Vector<Object>();\n" +
				"    Object flag = null;\n" +
				"    for (Object o : i) {\n" +
				"      flag = new Object();\n" +
				"    }\n" +
				"    flag.toString();\n" +
				// complain: cannot know if at least one iteration got executed
				"  }\n" +
				"}\n"},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	flag.toString();\n" +
			"	^^^^\n" +
			"Potential null pointer access: The variable flag may be null at this location\n" +
			"----------\n",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// null analysis -- for
public void test0716_for() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    Iterable i = new java.util.Vector<Object>();\n" +
				"    Object flag = null;\n" +
				"    for (Object o : i) { /* */ }\n" +
				"    flag.toString();\n" +
				"  }\n" +
				"}\n"},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	flag.toString();\n" +
			"	^^^^\n" +
			"Null pointer access: The variable flag can only be null at this location\n" +
			"----------\n",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// null analysis -- for
public void test0717_for() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(boolean dummy) {\n" +
				"    Object flag = null;\n" +
				"    for (;dummy;) {\n" +
				"      flag = new Object();\n" +
				"    }\n" +
				"    flag.toString();\n" +
				"  }\n" +
				"}\n"},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	flag.toString();\n" +
			"	^^^^\n" +
			"Potential null pointer access: The variable flag may be null at this location\n" +
			"----------\n",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// null analysis -- for
public void test0718_for() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(boolean dummy) {\n" +
			"    Object flag = null;\n" +
			"    for (;dummy;) { /* */ }\n" +
			"    flag.toString();\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	flag.toString();\n" +
		"	^^^^\n" +
		"Null pointer access: The variable flag can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- for
// origin: AssignmentTest#test019
public void test0719_for() {
	this.runConformTest(
		new String[] {
			    "X.java",
			    "public class X {\n" +
			    "  public static final char[] foo(char[] a, char c1, char c2) {\n" +
			    "   char[] r = null;\n" +
			    "   for (int i = 0, length = a.length; i < length; i++) {\n" +
			    "     char c = a[i];\n" +
			    "     if (c == c1) {\n" +
			    "       if (r == null) {\n" +
			    "         r = new char[length];\n" +
			    "       }\n" +
			    "       r[i] = c2;\n" +
			    "     } else if (r != null) {\n" +
			    "       r[i] = c;\n" +
			    "     }\n" +
			    "   }\n" +
			    "   if (r == null) return a;\n" +
			    "   return r;\n" +
			    " }\n" +
			    "}\n"},
		"");
}

// null analysis -- for
public void test0720_for_continue_break() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			  "public class X {\n" +
			  "  void foo() {\n" +
			  "    Object o = new Object();\n" +
			  "    for (int i = 0; i < 10; i++) {\n" +
			  "      if (o == null) {\n" + // complain: o cannot be null
			  "        continue;\n" +
			  "      }\n" +
			  "      o = null;\n" +
			  "      break;\n" +
			  "    }\n" +
			  "  }\n" +
			  "}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	if (o == null) {\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- for
public void test0721_for() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(boolean b) {\n" +
			"    Object o = null;\n" +
			"    for (; b ? (o = new Object()).equals(o) : false ;) {\n" +
			// contrast this with test0238; here the condition shades doubts
			// upon o being null
			"      /* */\n" +
			"    }\n" +
			"    if (o == null) { /* */ }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- for
public void test0722_for_return() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo (boolean b) {\n" +
			"    Object o = null;\n" +
			"    for (int i = 0; i < 25; i++) {\n" +
			"      if (b) {\n" +
			"        if (o == null) {\n" +
			"          o = new Object();\n" + // cleared by return downstream
			"        }\n" +
			"        return;\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	if (o == null) {\n" +
		"	    ^\n" +
		"Redundant null check: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- for
public void test0723_for() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo () {\n" +
			"    Object o[] = new Object[1];\n" +
			"    for (int i = 0; i < 1; i++) {\n" +
			"      if (i < 1) {\n" +
			"        o[i].toString();\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- for
public void test0724_for_with_initialization() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  X field;\n" +
			"  void foo(X x1) {\n" +
			"    // X x2;\n" +
			"    outer: for (int i = 0; i < 30; i++) {\n" +
			"      X x2 = x1;\n" +
			"      do {\n" +
			"        if (x2.equals(x1)) {\n" +
			"          continue outer;\n" +
			"        }\n" +
			"        x2 = x2.field;\n" +
			"      } while (x2 != null);\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- for
public void test0725_for_with_assignment() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  X field;\n" +
			"  void foo(X x1) {\n" +
			"    X x2;\n" +
			"    outer: for (int i = 0; i < 30; i++) {\n" +
			"      x2 = x1;\n" +
			"      do {\n" +
			"        if (x2.equals(x1)) {\n" +
			"          continue outer;\n" +
			"        }\n" +
			"        x2 = x2.field;\n" +
			"      } while (x2 != null);\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- for
// changed with https://bugs.eclipse.org/bugs/show_bug.cgi?id=127570
// we are now able to see that x2 is reinitialized with x1, which is unknown
public void test0726_for() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(X x1) {\n" +
			"    X x2 = null;\n" +
			"    for (int i = 0; i < 5; i++) {\n" +
			"      if (x2 == null) {\n" +
			"        x2 = x1;\n" +
			"      }\n" +
			"      x2.toString();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- for
public void test0727_for() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    for (; true;) { /* */ }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- for
public void test0728_for() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(X x) {\n" +
			"    for (; true; x.toString()) { /* */ }\n" +
			"    if (x == null) { /* */ }\n" + // complain
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	if (x == null) { /* */ }\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Unreachable code\n" +
		"----------\n");
}

// null analysis -- for
public void test0729_for_try_catch_finally() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"class X {\n" +
			"  X f;\n" +
			"  void bar() throws IOException {\n" +
			"    throw new IOException();\n" +
			"  }\n" +
			"  void foo(boolean b) {\n" +
			"    for (int i = 0 ; i < 5 ; i++) {\n" +
			"      X x = this.f;\n" +
			"      if (x == null) { \n" +
			"        continue;\n" +
			"      }\n" +
			"      if (b) {\n" +
			"        try {\n" +
			"          bar();\n" +
			"        } \n" +
			"        catch(IOException e) { /* */ }\n" +
			"        finally {\n" +
			"          x.toString();\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - for
public void test0730_for() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(Object o) {\n" +
			"    for ( ; o == null ; ) {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    if (o != null) { /* */ }\n" + // complain
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	if (o != null) { /* */ }\n" +
		"	    ^\n" +
		"Redundant null check: The variable o cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - for
public void test0731_for() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  X bar() {\n" +
			"    return new X();\n" +
			"  }\n" +
			"  void foo(Object o) {\n" +
			"    for ( ; o == null ; ) {\n" +
			"      o = bar();\n" +
			"    }\n" +
			"    if (o != null) { /* */ }\n" + // complain
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	if (o != null) { /* */ }\n" +
		"	    ^\n" +
		"Redundant null check: The variable o cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - for nested with break
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=129371
public void test0732_for_nested_break() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(String doubt) {\n" +
			"    for(int i = 0; i < 10; i++) {\n" +
			"      String s = doubt;\n" +
			"      if(s != null) {\n" +
			"        for(int j = 0; j < 1; j++) {\n" +
			"          break;\n" +
			"        }\n" +
			"        s.length();\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n" +
			"\n"},
		"");
}

// null analysis - for while with break
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=129371
// variant
public void test0733_for_while_break() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(String doubt, boolean b) {\n" +
			"    for(int i = 0; i < 10; i++) {\n" +
			"      String s = doubt;\n" +
			"      if (s != null) {\n" +
			"        while (b) {\n" +
			"          break;\n" +
			"        }\n" +
			"        s.length();\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n" +
			"\n"},
		"");
}

// null analysis - for while with break
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=129371
// variant
public void test0734_for_while_break() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(String doubt, boolean b) {\n" +
			"    for(int i = 0; i < 10; i++) {\n" +
			"      String s = doubt;\n" +
			"      if (s != null) {\n" +
			"        do {\n" +
			"          break;\n" +
			"        } while (b);\n" +
			"        s.length();\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n" +
			"\n"},
		"");
}

// null analysis - for nested with break
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=129371
// variant
public void test0735_for_nested_break() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object[] a, String doubt) {\n" +
				"    for(int i = 0; i < 10; i++) {\n" +
				"      String s = doubt;\n" +
				"      if(s != null) {\n" +
				"        for(Object o : a) {\n" +
				"          break;\n" +
				"        }\n" +
				"        s.length();\n" +
				"      }\n" +
				"    }\n" +
				"  }\n" +
				"}\n" +
				"\n"},
			"");
	}
}

// null analysis - for
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127570
public void test0736_for_embedded_lazy_init() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  public boolean foo() {\n" +
			"    Boolean b = null;\n" +
			"    for (int i = 0; i < 1; i++) {\n" +
			"      if (b == null) {\n" +
			"        b = Boolean.TRUE;\n" +
			"      }\n" +
			"      if (b.booleanValue()) {\n" + // quiet
			"        return b.booleanValue();\n" +
			"      }\n" +
			"    }\n" +
			"    return false;\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - for with unknown protection: unknown cannot protect anything
// suggested by https://bugs.eclipse.org/bugs/show_bug.cgi?id=127570
public void test0737_for_unknown_protection() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  public boolean foo(Boolean p) {\n" +
			"    Boolean b = null;\n" +
			"    for (int i = 0; i < 1; i++) {\n" +
			"      if (b == p) {\n" + // tells us that p is null as well
			"        // empty\n" +
			"      }\n" +
			"      else {\n" +
			"        continue;\n" +
			"      }\n" +
			"      if (b.booleanValue()) {\n" + // complain b can only be null
			"        return b.booleanValue();\n" +
			"      }\n" +
			"    }\n" +
			"    return false;\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 11)\n" +
		"	if (b.booleanValue()) {\n" +
		"	    ^\n" +
		"Null pointer access: The variable b can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - for with unknown protection
// suggested by https://bugs.eclipse.org/bugs/show_bug.cgi?id=127570
// the issue is that we cannot do less than full aliasing analysis to
// catch this one
// PREMATURE (maxime) reconsider when/if we bring full aliasing in
public void _test0738_for_unknown_protection() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  public boolean foo(Boolean p) {\n" +
			"    Boolean b = null;\n" +
			"    for (int i = 0; i < 1; i++) {\n" +
			"      if (b == p) {\n" +
			"        // empty\n" +
			"      }\n" +
			"      else {\n" +
			"        b = p;\n" +
			"      }\n" +
			"      if (b.booleanValue()) {\n" + // quiet because b is an alias for p, unknown
			"        return b.booleanValue();\n" +
			"      }\n" +
			"    }\n" +
			"    return false;\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis -- for
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=178895
public void test0739_for() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"public class X {\n" +
				"  void foo(List<Object> l, boolean b) {\n" +
				"    for (Object o : l) {\n" +
				"      if (b) {\n" +
				"        if (o != null) {\n" +
				"          return;\n" +
				"        }\n" +
				"      } else {\n" +
				"        o.toString();\n" +
				"      }\n" +
				"    }\n" +
				"  }\n" +
				"}\n"},
			"");
	}
}

// null analysis - for
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=176472
// variant
public void test0740_for_explicit_label() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(int i) {\n" +
			"    Object o = null;\n" +
			"    done: for (;;) {\n" +
			"      switch (i) {\n" +
			"        case 0:\n" +
			"          o = new Object();\n" +
			"          break;\n" +
			"        case 1:\n" +
			"          break done;\n" +
			"      }\n" +
			"    }\n" +
			"    if (o == null) {\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis - for
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=176472
// variant
public void test0741_for_explicit_label() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean test() {\n" +
			"    return true;\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    done: for (;;) {\n" +
			"      if (test()) {\n" +
			"        break done;\n" +
			"      }\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    if (o == null) {\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis - for
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=176472
// variant
public void test0742_for_explicit_label() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" +
				"public class X {\n" +
				"  void foo(int i, List<Object> l) {\n" +
				"    Object o = null;\n" +
				"    done: for (Object j: l) {\n" +
				"      switch (i) {\n" +
				"        case 0:\n" +
				"          o = new Object();\n" +
				"          break;\n" +
				"        case 1:\n" +
				"          break done;\n" +
				"      }\n" +
				"    }\n" +
				"    if (o == null) {\n" +
				"    }\n" +
				"  }\n" +
				"}\n"},
			"");
	}
}

// null analysis - for
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184298
public void test0743_for_infinite() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void test(String[] a) {\n" +
			"    String key = null;\n" +
			"    for( int i = 0; ; i++ )\n" +
			"    {\n" +
			"      if (a[i] == null)\n" +
			"        break;\n" +
			"      key = a[i];\n" +
			"    }\n" +
			"    if (key != null) {\n" +
			"      // empty\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - for
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184298
// variant
public void test0744_for_infinite() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void test(String[] a) {\n" +
			"    String key = null;\n" +
			"    loop: for( int i = 0; ; i++ )\n" +
			"    {\n" +
			"      if (a[i] == null)\n" +
			"        break loop;\n" +
			"      key = a[i];\n" +
			"    }\n" +
			"    if (key != null) {\n" +
			"      // empty\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - for
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=195638
public void test0746_for_try_catch() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    String str = null;\n" +
			"    for (int i = 0; i < 2; i++) {\n" +
			"      try {\n" +
			"        str = new String(\"Test\");\n" +
			"      } catch (Exception ex) {\n" +
			"        ex.printStackTrace();\n" +
			"      }\n" +
			"      str.charAt(i);\n" +
			"      str = null;\n" +
			"    }\n" +
			"  }\n" +
			"}\n"
			},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	str.charAt(i);\n" +
		"	^^^\n" +
		"Potential null pointer access: The variable str may be null at this location\n" +
		"----------\n");
}

// null analysis - for
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=195638
// variant: do not reset to null
public void test0747_for_try_catch() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    String str = null;\n" +
			"    for (int i = 0; i < 2; i++) {\n" +
			"      try {\n" +
			"        str = new String(\"Test\");\n" +
			"      } catch (Exception ex) {\n" +
			"        ex.printStackTrace();\n" +
			"      }\n" +
			"      str.charAt(i);\n" +
			"    }\n" +
			"  }\n" +
			"}\n"
			},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	str.charAt(i);\n" +
		"	^^^\n" +
		"Potential null pointer access: The variable str may be null at this location\n" +
		"----------\n" /* expectedCompilerLog */,
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- switch
public void test0800_switch() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" int k;\n" +
			" void foo() {\n" +
			"   Object o = null;\n" +
			"   switch (k) {\n" +
			"     case 0 :\n" +
			"       o = new Object();\n" +
			"       break;\n" +
			"     case 2 :\n" +
			"       return;\n" +
			"   }\n" +
			"   if(o == null) { /* */ }\n" + // quiet: don't know whether came from 0 or default
			" }\n" +
			"}\n"},
		"");
}

// null analysis -- switch
public void test0801_switch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" int k;\n" +
			" void foo() {\n" +
			"   Object o = null;\n" +
			"   switch (k) {\n" +
			"     case 0 :\n" +
			"       o = new Object();\n" +
			"       break;\n" +
			"     default :\n" +
			"       return;\n" +
			"   }\n" +
			"   if(o == null) { /* */ }\n" + // complain: only get there through 0, o non null
			" }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	if(o == null) { /* */ }\n" +
		"	   ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 12)\n" +
		"	if(o == null) { /* */ }\n" +
		"	              ^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- switch
public void test0802_switch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" int k;\n" +
			" void foo() {\n" +
			"   Object o = null;\n" +
			"   switch (k) {\n" +
			"     case 0 :\n" +
			"       o.toString();\n" + // complain: o can only be null
			"       break;\n" +
			"   }\n" +
			" }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- switch
public void test0803_switch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" int k;\n" +
			" void foo() {\n" +
			"   Object o = null;\n" +
			"   switch (k) {\n" +
			"     case 0 :\n" +
			"       o = new Object();\n" +
			"     case 1 :\n" +
			"       o.toString();\n" + // complain: may come through 0 or 1
			"       break;\n" +
			"   }\n" +
			" }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- switch
public void test0804_switch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo (Object o, int info) {\n" +
			"	 o = null;\n" +
			"	 switch (info) {\n" +
			"	   case 0 :\n" +
			"		 o = new Object();\n" +
			"		 break;\n" +
			"	   case 1 :\n" +
			"		 o = new String();\n" +
			"		 break;\n" +
			"	   default :\n" +
			"		 o = new X();\n" +
			"		 break;\n" +
			"	 }\n" +
			"	 if(o != null) { /* */ }\n" + // complain: all branches allocate a new o
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 15)\n" +
		"	if(o != null) { /* */ }\n" +
		"	   ^\n" +
		"Redundant null check: The variable o cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- switch
public void test0805_switch() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(X p) {\n" +
			"    X x = this;\n" +
			"    for (int i = 0; i < 5; i++) {\n" +
			"      switch (i) {\n" +
			"        case 1:\n" +
			"          x = p;\n" +
			"      }\n" +
			"    }\n" +
			"    if (x != null) { /* */ }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- non null protection tag
public void _test0900_non_null_protection_tag() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    boolean b = o != null;\n" + // shades doubts upon o
			"    o/*NN*/.toString();\n" + 	// protection => do not complain
			"    o.toString();\n" + 		// protected by previous line
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- non null protection tag
public void _test0901_non_null_protection_tag() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    if (b) {\n" +
			"      o = null;\n" +
			"    }\n" +
			"    o/*NN*/.toString();\n" +
			"    if (b) {\n" +
			"      o = null;\n" +
			"    }\n" +
			"    o/*\n" +
			"         NN  comment  */.toString();\n" +
			"    if (b) {\n" +
			"      o = null;\n" +
			"    }\n" +
			"    o/*  NN\n" +
			"               */.toString();\n" +
			"    if (b) {\n" +
			"      o = null;\n" +
			"    }\n" +
			"    o               //  NN   \n" +
			"      .toString();\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- non null protection tag
public void _test0902_non_null_protection_tag() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    if (b) {\n" +
			"      o = null;\n" +
			"    }\n" +
			"    o/*NON-NULL*/.toString();\n" +
			"    if (b) {\n" +
			"      o = null;\n" +
			"    }\n" +
			"    o/*  NON-NULL   comment */.toString();\n" +
			"    if (b) {\n" +
			"      o = null;\n" +
			"    }\n" +
			"    o/*  NON-NULL   \n" +
			"               */.toString();\n" +
			"    if (b) {\n" +
			"      o = null;\n" +
			"    }\n" +
			"    o               //  NON-NULL   \n" +
			"      .toString();\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- non null protection tag
public void test0903_non_null_protection_tag() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    if (b) {\n" +
			"      o = null;\n" +
			"    }\n" +
			"    o/*N N*/.toString();\n" +
			"    if (b) {\n" +
			"      o = null;\n" +
			"    }\n" +
			"    o/*NNa*/.toString();\n" +
			"    if (b) {\n" +
			"      o = null;\n" +
			"    }\n" +
			"    o/*aNN */.toString();\n" +
			"    if (b) {\n" +
			"      o = null;\n" +
			"    }\n" +
			"    o/*NON NULL*/.toString();\n" +
			"    if (b) {\n" +
			"      o = null;\n" +
			"    }\n" +
			"    o/*Non-Null*/.toString();\n" +
			"    if (b) {\n" +
			"      o = null;\n" +
			"    }\n" +
			"    o/*aNON-NULL */.toString();\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	o/*N N*/.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	o/*NNa*/.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 14)\n" +
		"	o/*aNN */.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 18)\n" +
		"	o/*NON NULL*/.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 22)\n" +
		"	o/*Non-Null*/.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 26)\n" +
		"	o/*aNON-NULL */.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}


// null analysis -- non null protection tag
public void test0905_non_null_protection_tag() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    boolean b = o != null;\n" + // shades doubts upon o
			"    o.toString();/*NN*/\n" + 	// too late to protect => complain
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	o.toString();/*NN*/\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- non null protection tag
public void test0906_non_null_protection_tag() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    boolean b = o != null;\n" + // shades doubts upon o
			"    /*NN*/o.toString();\n" + 	// too soon to protect => complain
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	/*NN*/o.toString();\n" +
		"	      ^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127244
// [compiler] Null reference analysis doesn't understand assertions
public void test0950_assert() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_4) {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o) {\n" +
				"    boolean b = o != null;\n" + // shades doubts upon o
				"    assert(o != null);\n" + 	// protection
				"    o.toString();\n" + 		// quiet
				"  }\n" +
				"}\n"},
			"");
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127244
// [compiler] Null reference analysis doesn't understand assertions
public void test0951_assert() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_4) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o) {\n" +
				"    assert(o == null);\n" + 	// forces null
				"    o.toString();\n" + 		// can only be null
				"  }\n" +
				"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127244
// [compiler] Null reference analysis doesn't understand assertions
public void test0952_assert() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_4) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o, boolean b) {\n" +
				"    assert(o != null || b);\n" + // shade doubts
				"    o.toString();\n" + 		// complain
				"  }\n" +
				"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127244
// [compiler] Null reference analysis doesn't understand assertions
public void test0953_assert_combined() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_4) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o1, Object o2) {\n" +
				"    assert(o1 != null && o2 == null);\n" +
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
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127244
// [compiler] Null reference analysis doesn't understand assertions
public void test0954_assert_fake_reachable() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_4) {
		runConformTest(
			true/*flush*/,
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o) {\n" +
				"    assert(false && o != null);\n" +
				"    if (o == null) { };\n" + 		// quiet
				"  }\n" +
				"}\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 3)\n" +
			"	assert(false && o != null);\n" +
			"	                ^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n",
			"",
			"",
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127244
// [compiler] Null reference analysis doesn't understand assertions
public void test0955_assert_combined() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_4) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o) {\n" +
				"    assert(false || o != null);\n" +
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
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127244
// [compiler] Null reference analysis doesn't understand assertions
public void test0956_assert_combined() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_4) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    Object o = null;\n" +
				"    assert(o != null);\n" +    // complain
				"    if (o == null) { };\n" +   // complain
				"  }\n" +
				"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	assert(o != null);\n" +
		"	       ^\n" +
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
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=250056
// Test to verify that asserts are exempted from redundant null check warnings,
// but this doesn't affect the downstream info.
public void test0957_assert() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_4) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void m() {\n" +
				"    X foo = new X();\n" +
				"	 assert (foo != null);\n" +	//don't warn
				"	 if (foo == null) {}\n" +
				"    X foo2 = new X();\n" +
				"	 assert (foo2 == null);\n" +	//don't warn
				"	 if (foo2 == null) {}\n" +
				"    X bar = null;\n" +
				"	 assert (bar == null);\n" +	//don't warn
				"	 if (bar == null) {}\n" +
				"    X bar2 = null;\n" +
				"	 assert (bar2 != null);\n" +	//don't warn
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
		"	assert (foo2 == null);\n" +
		"	        ^^^^\n" +
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
		"	assert (bar2 != null);\n" +
		"	        ^^^^\n" +
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
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=250056
// Test to verify that asserts are exempted from null comparison warnings,
// but this doesn't affect the downstream info.
public void test0958_assert() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.HashMap;\n" +
				"public class X {\n" +
				"  void m() {\n" +
				"    HashMap<Integer,X> map = new HashMap<Integer,X>();\n" +
				"	 X bar = null;\n" +
				"    X foo = map.get(1);\n" +
				"    if (foo == null) {\n" +
				"	 	foo = new X();\n" +
				"		map.put(1, foo);\n" +
				"	 }\n" +
				"	 assert (foo != null && bar == null);\n" +	// don't warn but do the null analysis
				"	 if (foo != null) {}\n" +		// warn
				"	 if (bar == null) {}\n" +		// warn
				"  }\n" +
				"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	if (foo != null) {}\n" +
		"	    ^^^\n" +
		"Redundant null check: The variable foo cannot be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 13)\n" +
		"	if (bar == null) {}\n" +
		"	    ^^^\n" +
		"Redundant null check: The variable bar can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=250056
// Test to verify that asserts are exempted from redundant null check warnings in a looping context,
// but this doesn't affect the downstream info.
public void test0959a_assert_loop() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_4) {
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
				"	 	assert (foo != null);\n" +	//don't warn
				"	 	if (foo == null) {}\n" +
				"	 	assert (foo2 == null);\n" +	//don't warn
				"	 	if (foo2 == null) {}\n" +
				"	 	assert (bar == null);\n" +	//don't warn
				"	 	if (bar == null) {}\n" +
				"	 	assert (bar2 != null);\n" +	//don't warn
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
		"	assert (foo2 == null);\n" +
		"	        ^^^^\n" +
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
		"	assert (bar2 != null);\n" +
		"	        ^^^^\n" +
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
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=250056
// Test to verify that asserts are exempted from redundant null check warnings in a looping context,
// but this doesn't affect the downstream info.
public void test0959b_assert_loop() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_4) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void m() {\n" +
				"	 while (true) {\n" +
				"   	X foo = new X();\n" +
				"	 	assert (foo != null);\n" +	//don't warn
				"	 	if (foo == null) {}\n" +
				"    	X foo2 = new X();\n" +
				"	 	assert (foo2 == null);\n" +	//don't warn
				"	 	if (foo2 == null) {}\n" +
				"    	X bar = null;\n" +
				"	 	assert (bar == null);\n" +	//don't warn
				"	 	if (bar == null) {}\n" +
				"    	X bar2 = null;\n" +
				"	 	assert (bar2 != null);\n" +	//don't warn
				"	 	if (bar2 == null) {}\n" +
				"	 }\n" +
				"  }\n" +
				"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	if (foo == null) {}\n" +
		"	    ^^^\n" +
		"Null comparison always yields false: The variable foo cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 6)\n" +
		"	if (foo == null) {}\n" +
		"	                 ^^\n" +
		"Dead code\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 8)\n" +
		"	assert (foo2 == null);\n" +
		"	        ^^^^\n" +
		"Null comparison always yields false: The variable foo2 cannot be null at this location\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 9)\n" +
		"	if (foo2 == null) {}\n" +
		"	    ^^^^\n" +
		"Redundant null check: The variable foo2 can only be null at this location\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 12)\n" +
		"	if (bar == null) {}\n" +
		"	    ^^^\n" +
		"Redundant null check: The variable bar can only be null at this location\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 14)\n" +
		"	assert (bar2 != null);\n" +
		"	        ^^^^\n" +
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
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=250056
// Test to verify that asserts are exempted from redundant null check warnings in a finally context,
// but this doesn't affect the downstream info.
public void test0960_assert_finally() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_4) {
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
				"	 	assert (foo != null);\n" +	//don't warn
				"	 	if (foo == null) {}\n" +
				"	 	assert (foo2 == null);\n" +	//don't warn
				"	 	if (foo2 == null) {}\n" +
				"	 	assert (bar == null);\n" +	//don't warn
				"	 	if (bar == null) {}\n" +
				"	 	assert (bar2 != null);\n" +	//don't warn
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
		"	assert (foo2 == null);\n" +
		"	        ^^^^\n" +
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
		"	assert (bar2 != null);\n" +
		"	        ^^^^\n" +
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
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// null analysis -- notNull protection tag
public void _test0900_notNull_protection_tag() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(/** @notNull */ Object o) {\n" +
			"    boolean b = o != null;\n" +
			"  }\n" +
			"}\n"},
		"ERR cannot be null");
}

// null analysis -- notNull protection tag
public void _test0901_notNull_protection_tag() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    /** @notNull */ Object l = o;\n" +
			"  }\n" +
			"}\n"},
		"ERR cannot be null... ou pas ?");
}

// null analysis -- notNull protection tag
public void _test0902_notNull_protection_tag() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(/** @nullable */ Object o) {\n" +
			"    /** @notNull */ Object l = o;\n" +
			"  }\n" +
			"}\n"},
		"ERR cannot be null");
}

// null analysis -- notNull protection tag
public void test0903_notNull_protection_tag() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object bar() {\n" +
			"    return null;\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    /** @notNull */ Object l = bar();\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- notNull protection tag
public void _test0904_notNull_protection_tag() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  /** @notNull */\n" +
			"  Object bar() {\n" +
			"    return new Object();\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    Object l = bar();\n" +
			"    if (l == null) { /* empty */ }\n" +
			"  }\n" +
			"}\n"},
		"ERR cannot be null");
}

// null analysis -- notNull protection tag
public void _test0905_notNull_protection_tag() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  /** @notNull */\n" +
			"  Object bar() {\n" +
			"    return null;\n" +
			"  }\n" +
			"}\n"},
		"ERR cannot be null");
}

// null analysis -- nullable tag
public void _test0950_nullable_tag() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(/** @nullable */ Object o) {\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
		"ERR may be null");
}

// null analysis -- nullable tag
public void _test0951_nullable_tag() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(/** @nullable */ Object o) {\n" +
			"    Object l = o;\n" +
			"    l.toString();\n" +
			"  }\n" +
			"}\n"},
		"ERR may be null");
}

// null analysis -- nullable tag
public void _test0952_nullable_tag() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(boolean b) {\n" +
			"    /** @nullable */ Object o;\n" +
			"    if (b) {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
		"ERR may be null");
}

// moved from AssignmentTest
public void test1004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  X foo(X x) {\n" +
			"    x.foo(null); // 0\n" +
			"    if (x != null) { // 1\n" +
			"      if (x == null) { // 2\n" +
			"        x.foo(null); // 3\n" +
			"      } else if (x instanceof X) { // 4\n" +
			"        x.foo(null); // 5 \n" +
			"      } else if (x != null) { // 6\n" +
			"        x.foo(null); // 7\n" +
			"      }\n" +
			"      x.foo(null); // 8\n" +
			"    }\n" +
			"    return this;\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	if (x != null) { // 1\n" +
		"	    ^\n" +
		"Redundant null check: The variable x cannot be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	if (x == null) { // 2\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable x cannot be null at this location\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 5)\n" +
		"	if (x == null) { // 2\n" +
		"        x.foo(null); // 3\n" +
		"      } else if (x instanceof X) { // 4\n" +
		"	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 9)\n" +
		"	} else if (x != null) { // 6\n" +
		"	           ^\n" +
		"Redundant null check: The variable x cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test1005() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Class c) {\n" +
			"    if (c.isArray() ) {\n" +
			"    } else if (c == java.lang.String.class ) {\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

public void test1006() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(X x) {\n" +
			"    if (x == this)\n" +
			"     return;\n" +
			"    x.foo(this);\n" +
			"  }\n" +
			"}\n"},
		"");
}

public void test1007() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(X x, X x2) {\n" +
			"    if (x != null)\n" +
			"      return;\n" +
			"    x = x2;\n" +
			"    if (x == null) {\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

public void test1008() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(X x, X x2) {\n" +
			"    if (x != null)\n" +
			"      return;\n" +
			"    try {\n" +
			"      x = x2;\n" +
			"    } catch(Exception e) {}\n" +
			"    if (x == null) {\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

public void test1009() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"\n" +
			"public class X {\n" +
			"  boolean check(String name) { return true; }\n" +
			"  Class bar(String name) throws ClassNotFoundException { return null; }\n" +
			"  File baz(String name) { return null; }\n" +
			"  \n" +
			"  public Class foo(String name, boolean resolve) throws ClassNotFoundException {\n" +
			"    \n" +
			"    Class c = bar(name);\n" +
			"    if (c != null)\n" +
			"      return c;\n" +
			"    if (check(name)) {\n" +
			"      try {\n" +
			"        c= bar(name);\n" +
			"          return c;\n" +
			"      } catch (ClassNotFoundException e) {\n" +
			"        // keep searching\n" +
			"        // only path to here left c unassigned from try block, means it was assumed to be null\n" +
			"      }\n" +
			"    }\n" +
			"    if (c == null) {// should complain: c can only be null\n" +
			"      File file= baz(name);\n" +
			"      if (file == null)\n" +
			"        throw new ClassNotFoundException();\n" +
			"    }\n" +
			"    return c;\n" +
			"  }\n" +
			"\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 22)\n" +
		"	if (c == null) {// should complain: c can only be null\n" +
		"	    ^\n" +
		"Redundant null check: The variable c can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test1010() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"  X itself() { return this; }\n" +
			"\n" +
			"  void bar() {\n" +
			"    X itself = this.itself();\n" +
			"    if (this == itself) {\n" +
			"      System.out.println(itself.toString()); //1\n" +
			"    } else {\n" +
			"      System.out.println(itself.toString()); //2\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

public void test1011() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"  X itself() { return this; }\n" +
			"\n" +
			"  void bar() {\n" +
			"    X itself = this.itself();\n" +
			"    if (this == itself) {\n" +
			"      X other = (X)itself;\n" +
			"      if (other != null) {\n" +
			"      }\n" +
			"      if (other == null) {\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	if (other != null) {\n" +
		"	    ^^^^^\n" +
		"Redundant null check: The variable other cannot be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 11)\n" +
		"	if (other == null) {\n" +
		"	    ^^^^^\n" +
		"Null comparison always yields false: The variable other cannot be null at this location\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 11)\n" +
		"	if (other == null) {\n" +
		"      }\n" +
		"	                   ^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test1012() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  \n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    do {\n" +
			"      if (o == null) {\n" +
			"        return;\n" +
			"      }\n" +
			"      // o = bar();\n" +
			"    } while (true);\n" +
			"  }\n" +
			"  X bar() { \n" +
			"    return null; \n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	if (o == null) {\n" +
		"	    ^\n" +
		"Redundant null check: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// x cannot equal this then null with no assignment in between
// each diagnostic is locally sound though
public void test1013() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(X x) {\n" +
			"    if (x == this) {\n" +
			"      if (x == null) {\n" +
			"        x.foo(this);\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	if (x == null) {\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable x cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	if (x == null) {\n" +
		"        x.foo(this);\n" +
		"      }\n" +
		"	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test1014() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(X x) {\n" +
			"    x = null;\n" +
			"    try {\n" +
			"      x = this;\n" +
			"    } finally {\n" +
			"      x.foo(null);\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

public void test1015() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    int i = 1;\n" +
			"    switch (i) {\n" +
			"      case 1:\n" +
			"        o = new Object();\n" +
			"        break;\n" +
			"    }\n" +
			"    if (o != null)\n" +
			"      o.toString();\n" +
			"  }\n" +
			"}\n"},
		"");
}

public void test1016() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(X x) {\n" +
			"    x = null;\n" +
			"    try {\n" +
			"      x = null;\n" +
			"    } finally {\n" +
			"      if (x != null) {\n" +
			"        x.foo(null);\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	x = null;\n" +
		"	^\n" +
		"Redundant assignment: The variable x can only be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	if (x != null) {\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable x can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test1017() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(X x) {\n" +
			"    x = this;\n" +
			"    try {\n" +
			"      x = null;\n" +
			"    } finally {\n" +
			"      if (x == null) {\n" +
			"        x.foo(null);\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	if (x == null) {\n" +
		"	    ^\n" +
		"Redundant null check: The variable x can only be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	x.foo(null);\n" +
		"	^\n" +
		"Null pointer access: The variable x can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test1018() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  \n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    do {\n" +
			"      if (o != null) return;\n" +
			"      o = null;\n" +
			"    } while (true);\n" +
			"  }\n" +
			"  X bar() { \n" +
			"    return null; \n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	if (o != null) return;\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o can only be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	o = null;\n" +
		"	^\n" +
		"Redundant assignment: The variable o can only be null at this location\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test1019() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static final char[] replaceOnCopy(\n" +
			"      char[] array,\n" +
			"      char toBeReplaced,\n" +
			"      char replacementChar) {\n" +
			"      \n" +
			"    char[] result = null;\n" +
			"    for (int i = 0, length = array.length; i < length; i++) {\n" +
			"      char c = array[i];\n" +
			"      if (c == toBeReplaced) {\n" +
			"        if (result == null) {\n" +
			"          result = new char[length];\n" +
			"          System.arraycopy(array, 0, result, 0, i);\n" +
			"        }\n" +
			"        result[i] = replacementChar;\n" +
			"      } else if (result != null) {\n" +
			"        result[i] = c;\n" +
			"      }\n" +
			"    }\n" +
			"    if (result == null) return array;\n" +
			"    return result;\n" +
			"  }\n" +
			"}\n"},
		"");
}

public void test1021() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  int kind;\n" +
			"  X parent;\n" +
			"  Object[] foo() { return null; }\n" +
			"  void findTypeParameters(X scope) {\n" +
			"    Object[] typeParameters = null;\n" +
			"    while (scope != null) {\n" +
			"      typeParameters = null;\n" +
			"      switch (scope.kind) {\n" +
			"        case 0 :\n" +
			"          typeParameters = foo();\n" +
			"          break;\n" +
			"        case 1 :\n" +
			"          typeParameters = foo();\n" +
			"          break;\n" +
			"        case 2 :\n" +
			"          return;\n" +
			"      }\n" +
			"      if(typeParameters != null) {\n" +
			"        foo();\n" +
			"      }\n" +
			"      scope = scope.parent;\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

public void test1022() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean bool() { return true; }\n" +
			"  void doSomething() {}\n" +
			"  \n" +
			"  void foo() {\n" +
			"    Object progressJob = null;\n" +
			"    while (bool()) {\n" +
			"      if (bool()) {\n" +
			"        if (progressJob != null)\n" +
			"          progressJob = null;\n" +
			"        doSomething();\n" +
			"      }\n" +
			"      try {\n" +
			"        if (progressJob == null) {\n" +
			"          progressJob = new Object();\n" +
			"        }\n" +
			"      } finally {\n" +
			"        doSomething();\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

public void test1023() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"  void foo(Object that) {\n" +
			"    Object o = new Object();\n" +
			"    while (that != null) {\n" +
			"      try {\n" +
			"        o = null;\n" +
			"        break;\n" +
			"      } finally {\n" +
			"        o = new Object();\n" +
			"      }\n" +
			"    }\n" +
			"    if (o == null) return;\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 13)\n" +
		"	if (o == null) return;\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 13)\n" +
		"	if (o == null) return;\n" +
		"	               ^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test1024() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  \n" +
			"  boolean bool() { return true; }\n" +
			"  void doSomething() {}\n" +
			"  \n" +
			"  void foo() {\n" +
			"    Object progressJob = null;\n" +
			"    while (bool()) {\n" +
			"      if (progressJob != null)\n" +
			"        progressJob = null;\n" +
			"      doSomething();\n" +
			"      try {\n" +
			"        if (progressJob == null) {\n" +
			"          progressJob = new Object();\n" +
			"        }\n" +
			"      } finally {\n" +
			"        doSomething();\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 13)\n" +
		"	if (progressJob == null) {\n" +
		"	    ^^^^^^^^^^^\n" +
		"Redundant null check: The variable progressJob can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test1025() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  \n" +
			"  void foo() {\n" +
			"    Object o;\n" +
			"    try {\n" +
			"      o = null;\n" +
			"    } finally {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    if (o == null) return;\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	if (o == null) return;\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 10)\n" +
		"	if (o == null) return;\n" +
		"	               ^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// TODO (philippe) reenable once fixed
public void _test1026() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  \n" +
			"  public static void main(String[] args) {\n" +
			"    Object o;\n" +
			"    try {\n" +
			"      o = null;\n" +
			"    } finally {\n" +
			"      if (args == null) o = new Object();\n" +
			"    }\n" +
			"    if (o == null) System.out.println(\"SUCCESS\");\n" +
			"  }\n" +
			"}\n"},
		"SUCCESS");
}

public void test1027() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean b;\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    while (b) {\n" +
			"      try {\n" +
			"        o = null;\n" +
			"      } finally {\n" +
			"        if (o == null) \n" +
			"          o = new Object();\n" +
			"        }\n" +
			"      }\n" +
			"    if (o == null) return;\n" +
			"  }\n" +
			"}\n"},
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	if (o == null) \n" +
			"	    ^\n" +
			"Redundant null check: The variable o can only be null at this location\n" +
			"----------\n");
}

// TODO (philippe) reenable once fixed
public void _test1028() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean b;\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    while (b) {\n" +
			"      try {\n" +
			"        o = null;\n" +
			"        break;\n" +
			"      } finally {\n" +
			"        if (o == null) \n" +
			"          o = new Object();\n" +
			"      }\n" +
			"    }\n" +
			"    if (o == null) return;\n" +
			"  }\n" +
			"}\n"},
		"");
}

public void test1029() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"    Object o = null;\n" +
			"    int i = 0;\n" +
			"    while (i++ < 2) {\n" +
			"      try {\n" +
			"        if (i == 2) return;\n" +
			"        o = null;\n" +
			"      } finally {\n" +
			"        if (i == 2) System.out.println(o);\n" +
			"        o = \"SUCCESS\";\n" +
			"      }\n" +
			"    }\n" +
			"    if (o == null) return;\n" +
			"  }\n" +
			"}\n"},
		"SUCCESS");
}

public void test1030() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  \n" +
			"  void foo() {\n" +
			"    Object a = null;\n" +
			"    while (true) {\n" +
			"      a = null;\n" +
			"      if (a == null) {\n" +
			"        System.out.println();\n" +
			"      }\n" +
			"      a = new Object();\n" +
			"      break;\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	a = null;\n" +
		"	^\n" +
		"Redundant assignment: The variable a can only be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	if (a == null) {\n" +
		"	    ^\n" +
		"Redundant null check: The variable a can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test1031() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  \n" +
			"  void foo() {\n" +
			"    Object a = null;\n" +
			"    while (true) {\n" +
			"      a = null;\n" +
			"      if (a == null) {\n" +
			"        System.out.println();\n" +
			"      }\n" +
			"      a = new Object();\n" +
			"      break;\n" +
			"    }\n" +
			"    if (a == null) {\n" +
			"      System.out.println();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	a = null;\n" +
		"	^\n" +
		"Redundant assignment: The variable a can only be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	if (a == null) {\n" +
		"	    ^\n" +
		"Redundant null check: The variable a can only be null at this location\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 13)\n" +
		"	if (a == null) {\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable a cannot be null at this location\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 13)\n" +
		"	if (a == null) {\n" +
		"      System.out.println();\n" +
		"    }\n" +
		"	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test1032() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o1 = this;\n" +
			"    Object o3;\n" +
			"    while (o1 != null && (o3 = o1) != null) {\n" +
			"      o1 = o3;\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	while (o1 != null && (o3 = o1) != null) {\n" +
		"	       ^^\n" +
		"Redundant null check: The variable o1 cannot be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	while (o1 != null && (o3 = o1) != null) {\n" +
		"	                     ^^^^^^^^^\n" +
		"Redundant null check: The variable o3 cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// (simplified to focus on nulls)
public void test1033() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  \n" +
			"  void foo() {\n" +
			"    String a,b;\n" +
			"    do{\n" +
			"      a=\"Hello \";\n" +
			"    }while(a!=null);\n" +
			"    if(a!=null)\n" +
			"      { /* */ }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	}while(a!=null);\n" +
		"	       ^\n" +
		"Redundant null check: The variable a cannot be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	if(a!=null)\n" +
		"	   ^\n" +
		"Null comparison always yields false: The variable a can only be null at this location\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 9)\n" +
		"	{ /* */ }\n" +
		"	^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// from AssignmentTest#test034, simplified
public void test1034() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public final class X \n" +
			"{\n" +
			"	void foo()\n" +
			"	{\n" +
			"		String rs = null;\n" +
			"		try\n" +
			"		{\n" +
			"			rs = \"\";\n" +
			"			return;\n" +
			"		}\n" +
			"		catch (Exception e)\n" +
			"		{\n" +
			"		}\n" +
			"		finally\n" +
			"		{\n" +
			"			if (rs != null)\n" +
			"			{\n" +
			"				try\n" +
			"				{\n" +
			"					rs.toString();\n" +
			"				}\n" +
			"				catch (Exception e)\n" +
			"				{\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"		return;\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 16)\n" +
		"	if (rs != null)\n" +
		"	    ^^\n" +
		"Redundant null check: The variable rs cannot be null at this location\n" +
		"----------\n");
}

public void test1036() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"  void foo() {\n" +
			"    Object o = new Object();\n" +
			"    do {\n" +
			"      o = null;\n" +
			"    } while (o != null);\n" +
			"    if (o == null) {\n" +
			"      // throw new Exception();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	} while (o != null);\n" +
		"	         ^\n" +
		"Null comparison always yields false: The variable o can only be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	if (o == null) {\n" +
		"	    ^\n" +
		"Redundant null check: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170704
// adding distinct options to control null checks in more detail
// default for null options is Ignore
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=192875
// changed default for null access to warning
public void test1050_options_all_default() {
	try {
		setNullRelatedOptions = false;
		runConformTest(
			true, // flush
			new String[] {
				"X.java",
				  "public class X {\n" +
				  "  void foo(Object p) {\n" +
				  "    Object o = null;\n" +
				  "    if (o != null) {\n" +
				  "       o = null;\n" +
				  "    }\n" +
				  "    if (p == null) {}\n" + // taint p
				  "    o.toString();\n" +
				  "    p.toString();\n" +
				  "  }\n" +
				  "}\n"
				  } /* testFiles */,
			"----------\n" +
			"1. WARNING in X.java (at line 4)\n" +
			"	if (o != null) {\n" +
			"       o = null;\n" +
			"    }\n" +
			"	               ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 8)\n" +
			"	o.toString();\n" +
			"	^\n" +
			"Null pointer access: The variable o can only be null at this location\n" +
			"----------\n" /* expectedCompilerLog */,
			"" /* expectedOutputString */,
			"" /* expectedErrorString */,
		    JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
	}
	finally {
		setNullRelatedOptions = true;
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170704
// adding distinct options to control null checks in more detail
// all null options set to Ignore
public void test1051_options_all_ignore() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.IGNORE);
	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.IGNORE);
    customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.IGNORE);
	this.runConformTest(
			new String[] {
				"X.java",
				  "public class X {\n" +
				  "  void foo(Object p) {\n" +
				  "    Object o = null;\n" +
				  "    if (o != null) {\n" +
				  "       o = null;\n" +
				  "    }\n" +
				  "    if (p == null) {}\n" + // taint p
				  "    o.toString();\n" +
				  "    p.toString();\n" +
				  "  }\n" +
				  "}\n"},
			null /* no expected output string */,
			null /* no extra class libraries */,
			true /* flush output directory */,
			null /* no vm arguments */,
			customOptions,
			null /* no custom requestor*/,
		  	false /* do not skip javac for this peculiar test */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170704
// adding distinct options to control null checks in more detail
// all options set to error
public void test1052_options_all_error() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			  "public class X {\n" +
			  "  void foo(Object p) {\n" +
			  "    Object o = null;\n" +
			  "    if (o != null) {\n" +
			  "       o = null;\n" +
			  "    }\n" +
			  "    if (p == null) {}\n" + // taint p
			  "    o.toString();\n" +
			  "    p.toString();\n" +
			  "  }\n" +
			  "}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	if (o != null) {\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o can only be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	if (o != null) {\n" +
		"       o = null;\n" +
		"    }\n" +
		"	               ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 8)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 9)\n" +
		"	p.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable p may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170704
// adding distinct options to control null checks in more detail
// selectively changing error levels
public void test1053_options_mix() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.IGNORE);
	customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.IGNORE);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"  void foo(Object p) {\n" +
			"    Object o = null;\n" +
			"    if (o != null) {\n" +
			"       o = null;\n" +
			"    }\n" +
			"    if (p == null) {}\n" + // taint p
			"    o.toString();\n" +
			"    p.toString();\n" +
			"  }\n" +
			"}\n"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" +  /* expected compiler log */
		"1. WARNING in X.java (at line 4)\n" +
		"	if (o != null) {\n" +
		"       o = null;\n" +
		"    }\n" +
		"	               ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170704
// adding distinct options to control null checks in more detail
// selectively changing error levels
public void test1054_options_mix() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.IGNORE);
	customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"  void foo(Object p) {\n" +
			"    Object o = null;\n" +
			"    if (o != null) {\n" +
			"       o = null;\n" +
			"    }\n" +
			"    if (p == null) {}\n" + // taint p
			"    o.toString();\n" +
			"    p.toString();\n" +
			"  }\n" +
			"}\n"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" +  /* expected compiler log */
		"1. ERROR in X.java (at line 4)\n" +
		"	if (o != null) {\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o can only be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	if (o != null) {\n" +
		"       o = null;\n" +
		"    }\n" +
		"	               ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 8)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170704
// adding distinct options to control null checks in more detail
// selectively changing error levels
public void test1055_options_mix() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.IGNORE);
	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"  void foo(Object p) {\n" +
			"    Object o = null;\n" +
			"    if (o != null) {\n" +
			"       o = null;\n" +
			"    }\n" +
			"    if (p == null) {}\n" + // taint p
			"    o.toString();\n" +
			"    p.toString();\n" +
			"  }\n" +
			"}\n"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" +  /* expected compiler log */
		"1. ERROR in X.java (at line 4)\n" +
		"	if (o != null) {\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o can only be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	if (o != null) {\n" +
		"       o = null;\n" +
		"    }\n" +
		"	               ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 9)\n" +
		"	p.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable p may be null at this location\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170704
// adding distinct options to control null checks in more detail
// selectively changing error levels
public void test1056_options_mix_with_SuppressWarnings() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
		customOptions.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.WARNING);
		customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
		runNegativeTest(
			// test directory preparation
			true /* flush output directory */,
			new String[] { /* test files */
				"X.java",
				"public class X {\n" +
				"@SuppressWarnings(\"null\")\n" +
				"  void foo(Object p) {\n" +
				"    Object o = null;\n" +
				"    if (o != null) {\n" +
				"       o = null;\n" +
				"    }\n" +
				"    if (p == null) {}\n" + // taint p
				"    o.toString();\n" +
				"    p.toString();\n" +
				"  }\n" +
				"}\n"
			},
			// compiler options
			null /* no class libraries */,
			customOptions /* custom options */,
			// compiler results
			"----------\n" +  /* expected compiler log */
			"1. WARNING in X.java (at line 5)\n" +
			"	if (o != null) {\n" +
			"       o = null;\n" +
			"    }\n" +
			"	               ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 9)\n" +
			"	o.toString();\n" +
			"	^\n" +
			"Null pointer access: The variable o can only be null at this location\n" +
			"----------\n",
			// javac options
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170704
// adding distinct options to control null checks in more detail
public void test1057_options_instanceof_is_check() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			  "public class X {\n" +
			  "  void foo(Object p) {\n" +
			  "    Object o = null;\n" +
			  "    if (p == null) {}\n" + // taint p
			  "    if (o instanceof String) {};\n" +
			  "    if (p instanceof String) {};\n" +
			  "  }\n" +
			  "}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	if (o instanceof String) {};\n" +
		"	    ^\n" +
		"instanceof always yields false: The variable o can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170704
// adding distinct options to control null checks in more detail
public void test1058_options_instanceof_is_check() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			  "public class X {\n" +
			  "  void foo(Object p) {\n" +
			  "    Object o = null;\n" +
			  "    if (p == null) {}\n" + // taint p
			  "    if (o instanceof String) {};\n" +
			  "    if (p instanceof String) {};\n" +
			  "  }\n" +
			  "}\n"},
		null /* no expected output string */,
		null /* no extra class libraries */,
		true /* flush output directory */,
		null /* no vm arguments */,
		customOptions,
		null /* no custom requestor*/,
	  	false /* do not skip javac for this peculiar test */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170704
// adding distinct options to control null checks in more detail
public void test1059_options_cannot_be_null_check() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			  "public class X {\n" +
			  "  void foo(Object p) {\n" +
			  "    Object o = new Object();\n" +
			  "    if (o == null) {}\n" +
			  "  }\n" +
			  "}\n"},
		null /* no expected output string */,
		null /* no extra class libraries */,
		true /* flush output directory */,
		null /* no vm arguments */,
		customOptions,
		null /* no custom requestor*/,
	  	false /* do not skip javac for this peculiar test */);
}
// encoding validation
public void test1500() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, int i, boolean b, Object u) {\n" +
			"    o.toString();\n" +
			"    switch (i) {\n" +
			"      case 0:\n" +
			"        if (b) {\n" +
			"          o = u;\n" +
			"        } else {\n" +
			"          o = new Object();\n" +
			"        }\n" +
			"        break;\n" +
			"    }\n" +
			"    if (o == null) { /* empty */ }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// encoding validation
public void test1501() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, int i, boolean b, Object u) {\n" +
			"    if (b) {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    o.toString();\n" +
			"    switch (i) {\n" +
			"      case 0:\n" +
			"        if (b) {\n" +
			"          o = u;\n" +
			"        } else {\n" +
			"          o = new Object();\n" +
			"        }\n" +
			"        break;\n" +
			"    }\n" +
			"    if (o == null) { /* empty */ }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// encoding validation
public void test1502() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, int i, boolean b, Object u) {\n" +
			"    if (b) {\n" +
			"      o = u;\n" +
			"    }\n" +
			"    o.toString();\n" +
			"    switch (i) {\n" +
			"      case 0:\n" +
			"        if (b) {\n" +
			"          o = u;\n" +
			"        } else {\n" +
			"          o = new Object();\n" +
			"        }\n" +
			"        break;\n" +
			"    }\n" +
			"    if (o == null) { /* empty */ }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// encoding validation
public void test1503() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, int i, boolean b, Object u) {\n" +
			"    if (b) {\n" +
			"      o = u;\n" +
			"    } else {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    o.toString();\n" +
			"    switch (i) {\n" +
			"      case 0:\n" +
			"        if (b) {\n" +
			"          o = u;\n" +
			"        } else {\n" +
			"          o = new Object();\n" +
			"        }\n" +
			"        break;\n" +
			"    }\n" +
			"    if (o == null) { /* empty */ }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// flow info low-level validation
public void test2000_flow_info() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"  void foo() {\n" +
			"    Object o0 = new Object(), o1 = o0, o2 = o0, o3 = o0, o4 = o0,\n" +
			"      o5 = o0, o6 = o0, o7 = o0, o8 = o0, o9 = o0,\n" +
			"      o10 = o0, o11 = o0, o12 = o0, o13 = o0, o14 = o0,\n" +
			"      o15 = o0, o16 = o0, o17 = o0, o18 = o0, o19 = o0,\n" +
			"      o20 = o0, o21 = o0, o22 = o0, o23 = o0, o24 = o0,\n" +
			"      o25 = o0, o26 = o0, o27 = o0, o28 = o0, o29 = o0,\n" +
			"      o30 = o0, o31 = o0, o32 = o0, o33 = o0, o34 = o0,\n" +
			"      o35 = o0, o36 = o0, o37 = o0, o38 = o0, o39 = o0,\n" +
			"      o40 = o0, o41 = o0, o42 = o0, o43 = o0, o44 = o0,\n" +
			"      o45 = o0, o46 = o0, o47 = o0, o48 = o0, o49 = o0,\n" +
			"      o50 = o0, o51 = o0, o52 = o0, o53 = o0, o54 = o0,\n" +
			"      o55 = o0, o56 = o0, o57 = o0, o58 = o0, o59 = o0,\n" +
			"      o60 = o0, o61 = o0, o62 = o0, o63 = o0, o64 = o0,\n" +
			"      o65 = o0, o66 = o0, o67 = o0, o68 = o0, o69 = o0;\n" +
			"    if (o65 == null) { /* */ }\n" + // complain
			"    if (o65 != null) { /* */ }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 18)\n" +
		"	if (o65 == null) { /* */ }\n" +
		"	    ^^^\n" +
		"Null comparison always yields false: The variable o65 cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 18)\n" +
		"	if (o65 == null) { /* */ }\n" +
		"	                 ^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 19)\n" +
		"	if (o65 != null) { /* */ }\n" +
		"	    ^^^\n" +
		"Redundant null check: The variable o65 cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test2001_flow_info() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"  void foo(\n" +
			"    Object o0, Object o1, Object o2, Object o3, Object o4,\n" +
			"      Object o5, Object o6, Object o7, Object o8, Object o9,\n" +
			"      Object o10, Object o11, Object o12, Object o13, Object o14,\n" +
			"      Object o15, Object o16, Object o17, Object o18, Object o19,\n" +
			"      Object o20, Object o21, Object o22, Object o23, Object o24,\n" +
			"      Object o25, Object o26, Object o27, Object o28, Object o29,\n" +
			"      Object o30, Object o31, Object o32, Object o33, Object o34,\n" +
			"      Object o35, Object o36, Object o37, Object o38, Object o39,\n" +
			"      Object o40, Object o41, Object o42, Object o43, Object o44,\n" +
			"      Object o45, Object o46, Object o47, Object o48, Object o49,\n" +
			"      Object o50, Object o51, Object o52, Object o53, Object o54,\n" +
			"      Object o55, Object o56, Object o57, Object o58, Object o59,\n" +
			"      Object o60, Object o61, Object o62, Object o63, Object o64,\n" +
			"      Object o65, Object o66, Object o67, Object o68, Object o69) {\n" +
			"    if (o65 == null) { /* */ }\n" +
			"    if (o65 != null) { /* */ }\n" +
			"  }\n" +
			"}\n"},
		"");
}

public void test2002_flow_info() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object m0, m1, m2, m3, m4,\n" +
			"    m5, m6, m7, m8, m9,\n" +
			"    m10, m11, m12, m13, m14,\n" +
			"    m15, m16, m17, m18, m19,\n" +
			"    m20, m21, m22, m23, m24,\n" +
			"    m25, m26, m27, m28, m29,\n" +
			"    m30, m31, m32, m33, m34,\n" +
			"    m35, m36, m37, m38, m39,\n" +
			"    m40, m41, m42, m43, m44,\n" +
			"    m45, m46, m47, m48, m49,\n" +
			"    m50, m51, m52, m53, m54,\n" +
			"    m55, m56, m57, m58, m59,\n" +
			"    m60, m61, m62, m63;\n" +
			"  void foo(Object o) {\n" +
			"    if (o == null) { /* */ }\n" +
			"    if (o != null) { /* */ }\n" +
			"  }\n" +
			"}\n"},
		"");
}

public void test2003_flow_info() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object m0, m1, m2, m3, m4,\n" +
			"    m5, m6, m7, m8, m9,\n" +
			"    m10, m11, m12, m13, m14,\n" +
			"    m15, m16, m17, m18, m19,\n" +
			"    m20, m21, m22, m23, m24,\n" +
			"    m25, m26, m27, m28, m29,\n" +
			"    m30, m31, m32, m33, m34,\n" +
			"    m35, m36, m37, m38, m39,\n" +
			"    m40, m41, m42, m43, m44,\n" +
			"    m45, m46, m47, m48, m49,\n" +
			"    m50, m51, m52, m53, m54,\n" +
			"    m55, m56, m57, m58, m59,\n" +
			"    m60, m61, m62, m63;\n" +
			"  void foo(Object o) {\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
		"");
}

public void test2004_flow_info() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object m0, m1, m2, m3, m4,\n" +
			"    m5, m6, m7, m8, m9,\n" +
			"    m10, m11, m12, m13, m14,\n" +
			"    m15, m16, m17, m18, m19,\n" +
			"    m20, m21, m22, m23, m24,\n" +
			"    m25, m26, m27, m28, m29,\n" +
			"    m30, m31, m32, m33, m34,\n" +
			"    m35, m36, m37, m38, m39,\n" +
			"    m40, m41, m42, m43, m44,\n" +
			"    m45, m46, m47, m48, m49,\n" +
			"    m50, m51, m52, m53, m54,\n" +
			"    m55, m56, m57, m58, m59,\n" +
			"    m60, m61, m62, m63;\n" +
			"  void foo() {\n" +
			"    Object o;\n" +
			"    if (o == null) { /* */ }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 17)\n" +
		"	if (o == null) { /* */ }\n" +
		"	    ^\n" +
		"The local variable o may not have been initialized\n" +
		"----------\n");
}

public void test2005_flow_info() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object m0, m1, m2, m3, m4,\n" +
			"    m5, m6, m7, m8, m9,\n" +
			"    m10, m11, m12, m13, m14,\n" +
			"    m15, m16, m17, m18, m19,\n" +
			"    m20, m21, m22, m23, m24,\n" +
			"    m25, m26, m27, m28, m29,\n" +
			"    m30, m31, m32, m33, m34,\n" +
			"    m35, m36, m37, m38, m39,\n" +
			"    m40, m41, m42, m43, m44,\n" +
			"    m45, m46, m47, m48, m49,\n" +
			"    m50, m51, m52, m53, m54,\n" +
			"    m55, m56, m57, m58, m59,\n" +
			"    m60, m61, m62, m63;\n" +
			"  void foo(Object o) {\n" +
			"    o = null;\n" +
			"  }\n" +
			"}\n"},
		"");
}

public void test2006_flow_info() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object m0, m1, m2, m3, m4,\n" +
			"    m5, m6, m7, m8, m9,\n" +
			"    m10, m11, m12, m13, m14,\n" +
			"    m15, m16, m17, m18, m19,\n" +
			"    m20, m21, m22, m23, m24,\n" +
			"    m25, m26, m27, m28, m29,\n" +
			"    m30, m31, m32, m33, m34,\n" +
			"    m35, m36, m37, m38, m39,\n" +
			"    m40, m41, m42, m43, m44,\n" +
			"    m45, m46, m47, m48, m49,\n" +
			"    m50, m51, m52, m53, m54,\n" +
			"    m55, m56, m57, m58, m59,\n" +
			"    m60, m61, m62, m63;\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"  }\n" +
			"}\n"},
		"");
}

public void test2007_flow_info() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object m0, m1, m2, m3, m4,\n" +
			"    m5, m6, m7, m8, m9,\n" +
			"    m10, m11, m12, m13, m14,\n" +
			"    m15, m16, m17, m18, m19,\n" +
			"    m20, m21, m22, m23, m24,\n" +
			"    m25, m26, m27, m28, m29,\n" +
			"    m30, m31, m32, m33, m34,\n" +
			"    m35, m36, m37, m38, m39,\n" +
			"    m40, m41, m42, m43, m44,\n" +
			"    m45, m46, m47, m48, m49,\n" +
			"    m50, m51, m52, m53, m54,\n" +
			"    m55, m56, m57, m58, m59,\n" +
			"    m60, m61, m62, m63;\n" +
			"  void foo() {\n" +
			"    Object o[] = null;\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- flow info
public void test2008_flow_info() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object m0, m1, m2, m3, m4,\n" +
			"    m5, m6, m7, m8, m9,\n" +
			"    m10, m11, m12, m13, m14,\n" +
			"    m15, m16, m17, m18, m19,\n" +
			"    m20, m21, m22, m23, m24,\n" +
			"    m25, m26, m27, m28, m29,\n" +
			"    m30, m31, m32, m33, m34,\n" +
			"    m35, m36, m37, m38, m39,\n" +
			"    m40, m41, m42, m43, m44,\n" +
			"    m45, m46, m47, m48, m49,\n" +
			"    m50, m51, m52, m53, m54,\n" +
			"    m55, m56, m57, m58, m59,\n" +
			"    m60, m61, m62, m63;\n" +
			"  void foo(boolean b) {\n" +
			"    Object o = null;\n" +
			"    while (o == null) {\n" +
			     // quiet: first iteration is sure to find o null,
			     // but other iterations may change it
			"      try { /* */ }\n" +
			"      finally {\n" +
			"        if (b) {\n" +
			"          o = new Object();\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- flow info
public void test2009_flow_info() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object m0, m1, m2, m3, m4,\n" +
			"    m5, m6, m7, m8, m9,\n" +
			"    m10, m11, m12, m13, m14,\n" +
			"    m15, m16, m17, m18, m19,\n" +
			"    m20, m21, m22, m23, m24,\n" +
			"    m25, m26, m27, m28, m29,\n" +
			"    m30, m31, m32, m33, m34,\n" +
			"    m35, m36, m37, m38, m39,\n" +
			"    m40, m41, m42, m43, m44,\n" +
			"    m45, m46, m47, m48, m49,\n" +
			"    m50, m51, m52, m53, m54,\n" +
			"    m55, m56, m57, m58, m59,\n" +
			"    m60, m61, m62, m63;\n" +
			"  void foo(Object o) {\n" +
			"    try { /* */ }\n" +
			"    finally {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    if (o == null) { /* */ }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 20)\n" +
		"	if (o == null) { /* */ }\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 20)\n" +
		"	if (o == null) { /* */ }\n" +
		"	               ^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- flow info
public void test2010_flow_info() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object m00, m01, m02, m03, m04,\n" +
			"    m05, m06, m07, m08, m09,\n" +
			"    m10, m11, m12, m13, m14,\n" +
			"    m15, m16, m17, m18, m19,\n" +
			"    m20, m21, m22, m23, m24,\n" +
			"    m25, m26, m27, m28, m29,\n" +
			"    m30, m31, m32, m33, m34,\n" +
			"    m35, m36, m37, m38, m39,\n" +
			"    m40, m41, m42, m43, m44,\n" +
			"    m45, m46, m47, m48, m49,\n" +
			"    m50, m51, m52, m53, m54,\n" +
			"    m55, m56, m57, m58, m59,\n" +
			"    m60, m61, m62, m63;\n" +
			"  void foo() {\n" +
			"    Object o;\n" +
			"    try { /* */ }\n" +
			"    finally {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    if (o == null) { /* */ }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 21)\n" +
		"	if (o == null) { /* */ }\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 21)\n" +
		"	if (o == null) { /* */ }\n" +
		"	               ^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- flow info
public void test2011_flow_info() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object m000, m001, m002, m003, m004, m005, m006, m007, m008, m009,\n" +
			"    m010, m011, m012, m013, m014, m015, m016, m017, m018, m019,\n" +
			"    m020, m021, m022, m023, m024, m025, m026, m027, m028, m029,\n" +
			"    m030, m031, m032, m033, m034, m035, m036, m037, m038, m039,\n" +
			"    m040, m041, m042, m043, m044, m045, m046, m047, m048, m049,\n" +
			"    m050, m051, m052, m053, m054, m055, m056, m057, m058, m059,\n" +
			"    m060, m061, m062, m063;\n" +
			"  void foo() {\n" +
			"    Object o000, o001, o002, o003, o004, o005, o006, o007, o008, o009,\n" +
			"      o010, o011, o012, o013, o014, o015, o016, o017, o018, o019,\n" +
			"      o020, o021, o022, o023, o024, o025, o026, o027, o028, o029,\n" +
			"      o030, o031, o032, o033, o034, o035, o036, o037, o038, o039,\n" +
			"      o040, o041, o042, o043, o044, o045, o046, o047, o048, o049,\n" +
			"      o050, o051, o052, o053, o054, o055, o056, o057, o058, o059,\n" +
			"      o060, o061, o062, o063;\n" +
			"    Object o;\n" +
			"    try {\n" +
			"      o000 = new Object();\n" +
			"    }\n" +
			"    finally {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    if (o == null) { /* */ }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 24)\n" +
		"	if (o == null) { /* */ }\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 24)\n" +
		"	if (o == null) { /* */ }\n" +
		"	               ^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- flow info
public void test2012_flow_info() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object m000, m001, m002, m003, m004, m005, m006, m007, m008, m009,\n" +
			"    m010, m011, m012, m013, m014, m015, m016, m017, m018, m019,\n" +
			"    m020, m021, m022, m023, m024, m025, m026, m027, m028, m029,\n" +
			"    m030, m031, m032, m033, m034, m035, m036, m037, m038, m039,\n" +
			"    m040, m041, m042, m043, m044, m045, m046, m047, m048, m049,\n" +
			"    m050, m051, m052, m053, m054, m055, m056, m057, m058, m059,\n" +
			"    m060, m061, m062, m063;\n" +
			"  void foo() {\n" +
			"    Object o000, o001, o002, o003, o004, o005, o006, o007, o008, o009,\n" +
			"      o010, o011, o012, o013, o014, o015, o016, o017, o018, o019,\n" +
			"      o020, o021, o022, o023, o024, o025, o026, o027, o028, o029,\n" +
			"      o030, o031, o032, o033, o034, o035, o036, o037, o038, o039,\n" +
			"      o040, o041, o042, o043, o044, o045, o046, o047, o048, o049,\n" +
			"      o050, o051, o052, o053, o054, o055, o056, o057, o058, o059,\n" +
			"      o060, o061, o062, o063;\n" +
			"    Object o;\n" +
			"    try {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    finally {\n" +
			"      o000 = new Object();\n" +
			"    }\n" +
			"    if (o == null) { /* */ }\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 24)\n" +
		"	if (o == null) { /* */ }\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o cannot be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 24)\n" +
		"	if (o == null) { /* */ }\n" +
		"	               ^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- flow info
public void test2013_flow_info() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  Object m000, m001, m002, m003, m004, m005, m006, m007, m008, m009,\n" +
			"    m010, m011, m012, m013, m014, m015, m016, m017, m018, m019,\n" +
			"    m020, m021, m022, m023, m024, m025, m026, m027, m028, m029,\n" +
			"    m030, m031, m032, m033, m034, m035, m036, m037, m038, m039,\n" +
			"    m040, m041, m042, m043, m044, m045, m046, m047, m048, m049,\n" +
			"    m050, m051, m052, m053, m054, m055, m056, m057, m058, m059,\n" +
			"    m060, m061, m062, m063;\n" +
			"  void foo(Object u) {\n" +
			"    Object o = null;\n" +
			"    while (dummy) {\n" +
			"      o = u;\n" +
			"    }\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 15)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- flow info
public void test2014_flow_info() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  int m000, m001, m002, m003, m004, m005, m006, m007, m008, m009,\n" +
			"    m010, m011, m012, m013, m014, m015, m016, m017, m018, m019,\n" +
			"    m020, m021, m022, m023, m024, m025, m026, m027, m028, m029,\n" +
			"    m030, m031, m032, m033, m034, m035, m036, m037, m038, m039,\n" +
			"    m040, m041, m042, m043, m044, m045, m046, m047, m048, m049,\n" +
			"    m050, m051, m052, m053, m054, m055, m056, m057, m058, m059,\n" +
			"    m060, m061, m062, m063;\n" +
			"  final int m064;\n" +
			"  X() {\n" +
			"    m064 = 10;\n" +
			"    class Inner extends X {\n" +
			"      int m100, m101, m102, m103, m104, m105, m106, m107, m108, m109,\n" +
			"        m110, m111, m112, m113, m114, m115, m116, m117, m118, m119,\n" +
			"        m120, m121, m122, m123, m124, m125, m126, m127, m128, m129,\n" +
			"        m130, m131, m132, m133, m134, m135, m136, m137, m138, m139,\n" +
			"        m140, m141, m142, m143, m144, m145, m146, m147, m148, m149,\n" +
			"        m150, m151, m152, m153, m154, m155, m156, m157, m158, m159,\n" +
			"        m160, m161, m162, m163;\n" +
			"      final int m164;\n" +
			"      int bar() {\n" +
			"        return m100 + m101 + m102 + m103 + m104 +\n" +
			"               m105 + m106 + m107 + m108 + m109 +\n" +
			"               m110 + m111 + m112 + m113 + m114 +\n" +
			"               m115 + m116 + m117 + m118 + m119 +\n" +
			"               m120 + m121 + m122 + m123 + m124 +\n" +
			"               m125 + m126 + m127 + m128 + m129 +\n" +
			"               m130 + m131 + m132 + m133 + m134 +\n" +
			"               m135 + m136 + m137 + m138 + m139 +\n" +
			"               m140 + m141 + m142 + m143 + m144 +\n" +
			"               m145 + m146 + m147 + m148 + m149 +\n" +
			"               m150 + m151 + m152 + m153 + m154 +\n" +
			"               m155 + m156 + m157 + m158 + m159 +\n" +
			"               m160 + m161 + m162 + m163 + m164;\n" +
			"      }\n" +
			"    };\n" +
			"    System.out.println((new Inner()).bar());\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 20)\n" +
		"	final int m164;\n" +
		"	          ^^^^\n" +
		"The blank final field m164 may not have been initialized\n" +
		"----------\n");
}

// null analysis -- flow info
public void test2015_flow_info() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  int m000, m001, m002, m003, m004, m005, m006, m007, m008, m009,\n" +
			"    m010, m011, m012, m013, m014, m015, m016, m017, m018, m019,\n" +
			"    m020, m021, m022, m023, m024, m025, m026, m027, m028, m029,\n" +
			"    m030, m031, m032, m033, m034, m035, m036, m037, m038, m039,\n" +
			"    m040, m041, m042, m043, m044, m045, m046, m047, m048, m049,\n" +
			"    m050, m051, m052, m053, m054, m055, m056, m057, m058, m059,\n" +
			"    m060, m061, m062, m063;\n" +
			"  final int m200;\n" +
			"  int m201, m202, m203, m204, m205, m206, m207, m208, m209,\n" +
			"    m210, m211, m212, m213, m214, m215, m216, m217, m218, m219,\n" +
			"    m220, m221, m222, m223, m224, m225, m226, m227, m228, m229,\n" +
			"    m230, m231, m232, m233, m234, m235, m236, m237, m238, m239,\n" +
			"    m240, m241, m242, m243, m244, m245, m246, m247, m248, m249,\n" +
			"    m250, m251, m252, m253, m254, m255, m256, m257, m258, m259,\n" +
			"    m260, m261, m262, m263;\n" +
			"  int m301, m302, m303, m304, m305, m306, m307, m308, m309,\n" +
			"    m310, m311, m312, m313, m314, m315, m316, m317, m318, m319,\n" +
			"    m320, m321, m322, m323, m324, m325, m326, m327, m328, m329,\n" +
			"    m330, m331, m332, m333, m334, m335, m336, m337, m338, m339,\n" +
			"    m340, m341, m342, m343, m344, m345, m346, m347, m348, m349,\n" +
			"    m350, m351, m352, m353, m354, m355, m356, m357, m358, m359,\n" +
			"    m360, m361, m362, m363;\n" +
			"  X() {\n" +
			"    m200 = 10;\n" +
			"    class Inner extends X {\n" +
			"      int m100, m101, m102, m103, m104, m105, m106, m107, m108, m109,\n" +
			"        m110, m111, m112, m113, m114, m115, m116, m117, m118, m119,\n" +
			"        m120, m121, m122, m123, m124, m125, m126, m127, m128, m129,\n" +
			"        m130, m131, m132, m133, m134, m135, m136, m137, m138, m139,\n" +
			"        m140, m141, m142, m143, m144, m145, m146, m147, m148, m149,\n" +
			"        m150, m151, m152, m153, m154, m155, m156, m157, m158, m159,\n" +
			"        m160, m161, m162, m163;\n" +
			"      final int m164;\n" +
			"      int bar() {\n" +
			"        return m100 + m101 + m102 + m103 + m104 +\n" +
			"               m105 + m106 + m107 + m108 + m109 +\n" +
			"               m110 + m111 + m112 + m113 + m114 +\n" +
			"               m115 + m116 + m117 + m118 + m119 +\n" +
			"               m120 + m121 + m122 + m123 + m124 +\n" +
			"               m125 + m126 + m127 + m128 + m129 +\n" +
			"               m130 + m131 + m132 + m133 + m134 +\n" +
			"               m135 + m136 + m137 + m138 + m139 +\n" +
			"               m140 + m141 + m142 + m143 + m144 +\n" +
			"               m145 + m146 + m147 + m148 + m149 +\n" +
			"               m150 + m151 + m152 + m153 + m154 +\n" +
			"               m155 + m156 + m157 + m158 + m159 +\n" +
			"               m160 + m161 + m162 + m163 + m164;\n" +
			"      }\n" +
			"    };\n" +
			"    System.out.println((new Inner()).bar());\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 34)\n" +
		"	final int m164;\n" +
		"	          ^^^^\n" +
		"The blank final field m164 may not have been initialized\n" +
		"----------\n");
}

// null analysis -- flow info
public void test2016_flow_info() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  int m000, m001, m002, m003, m004, m005, m006, m007, m008, m009,\n" +
			"    m010, m011, m012, m013, m014, m015, m016, m017, m018, m019,\n" +
			"    m020, m021, m022, m023, m024, m025, m026, m027, m028, m029,\n" +
			"    m030, m031, m032, m033, m034, m035, m036, m037, m038, m039,\n" +
			"    m040, m041, m042, m043, m044, m045, m046, m047, m048, m049,\n" +
			"    m050, m051, m052, m053, m054, m055, m056, m057, m058, m059,\n" +
			"    m060, m061;\n" +
			"  final int m062;\n" +
			"  {\n" +
			"    int l063, m201 = 0, m202, m203, m204, m205, m206, m207, m208, m209,\n" +
			"      m210, m211, m212, m213, m214, m215, m216, m217, m218, m219,\n" +
			"      m220, m221, m222, m223, m224, m225, m226, m227, m228, m229,\n" +
			"      m230, m231, m232, m233, m234, m235, m236, m237, m238, m239,\n" +
			"      m240, m241, m242, m243, m244, m245, m246, m247, m248, m249,\n" +
			"      m250, m251, m252, m253, m254, m255, m256, m257, m258, m259,\n" +
			"      m260, m261, m262, m263;\n" +
			"    int m301, m302, m303, m304, m305, m306, m307, m308, m309,\n" +
			"      m310, m311, m312, m313, m314, m315, m316, m317, m318, m319,\n" +
			"      m320, m321, m322, m323, m324, m325, m326, m327, m328, m329,\n" +
			"      m330, m331, m332, m333, m334, m335, m336, m337, m338, m339,\n" +
			"      m340, m341, m342, m343, m344, m345, m346, m347, m348, m349,\n" +
			"      m350, m351, m352, m353, m354, m355, m356, m357, m358, m359,\n" +
			"      m360 = 0, m361 = 0, m362 = 0, m363 = 0;\n" +
			"    m062 = m360;\n" +
			"  }\n" +
			"  X() {\n" +
			"    int l0, l1;\n" +
			"    m000 = l1;\n" +
			"    class Inner extends X {\n" +
			"      int bar() {\n" +
			"        return 0;\n" +
			"      }\n" +
			"    };\n" +
			"    System.out.println((new Inner()).bar());\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 29)\n" +
		"	m000 = l1;\n" +
		"	       ^^\n" +
		"The local variable l1 may not have been initialized\n" +
		"----------\n");
}

public void test2017_flow_info() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  Object m000, m001, m002, m003, m004, m005, m006, m007, m008, m009,\n" +
			"    m010, m011, m012, m013, m014, m015, m016, m017, m018, m019,\n" +
			"    m020, m021, m022, m023, m024, m025, m026, m027, m028, m029,\n" +
			"    m030, m031, m032, m033, m034, m035, m036, m037, m038, m039,\n" +
			"    m040, m041, m042, m043, m044, m045, m046, m047, m048, m049,\n" +
			"    m050, m051, m052, m053, m054, m055, m056, m057, m058, m059,\n" +
			"    m060, m061, m062, m063;\n" +
			"  void foo(Object u) {\n" +
			"    Object o = null;\n" +
			"    while (dummy) {\n" +
			"      if (dummy) {\n" + // uncorrelated
			"        o = u;\n" +
			"        continue;\n" +
			"      }\n" +
			"    }\n" +
			"    if (o != null) { /* */ }\n" +
			"  }\n" +
			"}\n"},
		"");
}

public void test2018_flow_info() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  Object m000, m001, m002, m003, m004, m005, m006, m007, m008, m009,\n" +
			"    m010, m011, m012, m013, m014, m015, m016, m017, m018, m019,\n" +
			"    m020, m021, m022, m023, m024, m025, m026, m027, m028, m029,\n" +
			"    m030, m031, m032, m033, m034, m035, m036, m037, m038, m039,\n" +
			"    m040, m041, m042, m043, m044, m045, m046, m047, m048, m049,\n" +
			"    m050, m051, m052, m053, m054, m055, m056, m057, m058, m059,\n" +
			"    m060, m061, m062, m063;\n" +
			"  void foo() {\n" +
			"    Object o;\n" +
			"    while (dummy) {\n" +
			"      if (dummy) {\n" + // uncorrelated
			"        o = null;\n" +
			"        continue;\n" +
			"      }\n" +
			"    }\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 18)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"The local variable o may not have been initialized\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 18)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n");
}

public void test2019_flow_info() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  Object m000, m001, m002, m003, m004, m005, m006, m007, m008, m009,\n" +
			"    m010, m011, m012, m013, m014, m015, m016, m017, m018, m019,\n" +
			"    m020, m021, m022, m023, m024, m025, m026, m027, m028, m029,\n" +
			"    m030, m031, m032, m033, m034, m035, m036, m037, m038, m039,\n" +
			"    m040, m041, m042, m043, m044, m045, m046, m047, m048, m049,\n" +
			"    m050, m051, m052, m053, m054, m055, m056, m057, m058, m059,\n" +
			"    m060, m061, m062, m063;\n" +
			"  void foo() {\n" +
			"    Object o;\n" +
			"    while (dummy) {\n" +
			"      if (dummy) {\n" + // uncorrelated
			"        continue;\n" +
			"      }\n" +
			"      o = null;\n" +
			"    }\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 18)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"The local variable o may not have been initialized\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 18)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n");
}

public void test2020_flow_info() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  Object m000, m001, m002, m003, m004, m005, m006, m007, m008, m009,\n" +
			"    m010, m011, m012, m013, m014, m015, m016, m017, m018, m019,\n" +
			"    m020, m021, m022, m023, m024, m025, m026, m027, m028, m029,\n" +
			"    m030, m031, m032, m033, m034, m035, m036, m037, m038, m039,\n" +
			"    m040, m041, m042, m043, m044, m045, m046, m047, m048, m049,\n" +
			"    m050, m051, m052, m053, m054, m055, m056, m057, m058, m059,\n" +
			"    m060, m061, m062, m063;\n" +
			"  int m200, m201, m202, m203, m204, m205, m206, m207, m208, m209,\n" +
			"    m210, m211, m212, m213, m214, m215, m216, m217, m218, m219,\n" +
			"    m220, m221, m222, m223, m224, m225, m226, m227, m228, m229,\n" +
			"    m230, m231, m232, m233, m234, m235, m236, m237, m238, m239,\n" +
			"    m240, m241, m242, m243, m244, m245, m246, m247, m248, m249,\n" +
			"    m250, m251, m252, m253, m254, m255, m256, m257, m258, m259,\n" +
			"    m260, m261;\n" +
			"  void foo() {\n" +
			"    Object o0, o1;\n" +
			"    while (dummy) {\n" +
			"      o0 = new Object();\n" +
			"      if (dummy) {\n" + // uncorrelated
			"        o1 = null;\n" +
			"        continue;\n" +
			"      }\n" +
			"    }\n" +
			"    o1.toString();\n" +
			"  }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 26)\n" +
		"	o1.toString();\n" +
		"	^^\n" +
		"The local variable o1 may not have been initialized\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 26)\n" +
		"	o1.toString();\n" +
		"	^^\n" +
		"Potential null pointer access: The variable o1 may be null at this location\n" +
		"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=291418
// Test to verify that redundant null checks are properly reported in all loops
public void testBug291418a() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeNullTest(
				new String[] {
						"X.java",
						"class X {\n" +
						"  void foo(int[] argArray) {\n" +
						"    int[] array = {2};\n" +
						"    int[] collectionVar = {1,2};\n" +
						"	 if(argArray == null) return;\n" +
						"    for(int x:collectionVar) {\n" +
						"        if (collectionVar == null);\n" +	// collectionVar cannot be null here
						"        if (array == null);\n" +				// array is not null here
						"		 if (argArray == null);\n" +		// argArray cannot be null here
						"    }\n" +
						"	 int count = 0;\n" +
						"    do {\n" +
						"		 count++;\n" +
						"        if (array == null);\n" +				// array is not null here
						"		 if (argArray == null);\n" +		// argArray cannot be null here
						"    } while (count<10);\n" +
						"	 array = new int[0];\n" + 			// reset tainting by null check
						"	 if (argArray == null) return;\n" + // reset tainting by null check
						"    for (int i=0; i<2; i++) {\n" +
						"        if (array == null);\n" +				// array is not null here
						"		 if (argArray == null);\n" +		// argArray cannot be null here
						"    }\n" +
						"    while (true) {\n" +
						"        if (array == null);\n" +				// array is not null here
						"		 if (argArray == null);\n" +		// argArray cannot be null here
						"    }\n" +
						"  }\n" +
						"}"},
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	if (collectionVar == null);\n" +
				"	    ^^^^^^^^^^^^^\n" +
				"Null comparison always yields false: The variable collectionVar cannot be null at this location\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 8)\n" +
				"	if (array == null);\n" +
				"	    ^^^^^\n" +
				"Null comparison always yields false: The variable array cannot be null at this location\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 9)\n" +
				"	if (argArray == null);\n" +
				"	    ^^^^^^^^\n" +
				"Null comparison always yields false: The variable argArray cannot be null at this location\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 14)\n" +
				"	if (array == null);\n" +
				"	    ^^^^^\n" +
				"Null comparison always yields false: The variable array cannot be null at this location\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 15)\n" +
				"	if (argArray == null);\n" +
				"	    ^^^^^^^^\n" +
				"Null comparison always yields false: The variable argArray cannot be null at this location\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 20)\n" +
				"	if (array == null);\n" +
				"	    ^^^^^\n" +
				"Null comparison always yields false: The variable array cannot be null at this location\n" +
				"----------\n" +
				"7. ERROR in X.java (at line 21)\n" +
				"	if (argArray == null);\n" +
				"	    ^^^^^^^^\n" +
				"Null comparison always yields false: The variable argArray cannot be null at this location\n" +
				"----------\n" +
				"8. ERROR in X.java (at line 24)\n" +
				"	if (array == null);\n" +
				"	    ^^^^^\n" +
				"Null comparison always yields false: The variable array cannot be null at this location\n" +
				"----------\n" +
				"9. ERROR in X.java (at line 25)\n" +
				"	if (argArray == null);\n" +
				"	    ^^^^^^^^\n" +
				"Null comparison always yields false: The variable argArray cannot be null at this location\n" +
				"----------\n");
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=291418
// Test to verify that redundant null checks are properly reported
// in a loop in case the null status is modified downstream in the loop
public void testBug291418b() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeNullTest(
				new String[] {
						"X.java",
						"class X {\n" +
						"  void foo(int[] argArray) {\n" +
						"    int[] array = {2};\n" +
						"    int[] collectionVar = {1,2};\n" +
						"	 if(argArray == null) return;" +
						"    for(int x:collectionVar) {\n" +
						"        if (collectionVar == null);\n" +	// collectionVar cannot be null here
						"        if (array == null);\n" +		// array is not null in first iteration but assigned null later in the loop. So we keep quiet
						"		 if (argArray == null);\n" +		// argArray cannot be null here
						"		 array = null;\n" +
						"    }\n" +
						"  }\n" +
						"}"},
				"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	if (collectionVar == null);\n" +
				"	    ^^^^^^^^^^^^^\n" +
				"Null comparison always yields false: The variable collectionVar cannot be null at this location\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 8)\n" +
				"	if (argArray == null);\n" +
				"	    ^^^^^^^^\n" +
				"Null comparison always yields false: The variable argArray cannot be null at this location\n" +
				"----------\n");
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=293917
// Test that a redundant null check doesn't affect the null status of
// a variable downstream.
public void testBug293917a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void foo(){\n" +
			"		String x = null, y = null;\n" +
			"		if (x == null) x = \"foo\";\n" +
			"		if (x != null) y = \"bar\";\n" +
			"		x.length();\n" +   // shouldn't warn here
			"		y.length();\n" +   // shouldn't warn here
			"	}\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	if (x == null) x = \"foo\";\n" +
		"	    ^\n" +
		"Redundant null check: The variable x can only be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	if (x != null) y = \"bar\";\n" +
		"	    ^\n" +
		"Redundant null check: The variable x cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=293917
// Test that a redundant null check doesn't affect the null status of
// a variable downstream in a loop.
public void testBug293917b() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void foo(){\n" +
			"		String x = null, y = null;" +
			"		while(true) {\n" +
			"			if (x == null) x = \"foo\";\n" +
			"			if (x != null) y = \"bar\";\n" +
			"			x.length();\n" +   // shouldn't warn here
			"			y.length();\n" +   // shouldn't warn here
			"		}\n" +
			"	}\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	if (x != null) y = \"bar\";\n" +
		"	    ^\n" +
		"Redundant null check: The variable x cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=293917
// Test that a redundant null check doesn't affect the null status of
// a variable downstream in a finally block.
public void testBug293917c() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void foo(){\n" +
			"		String x = null, y = null;" +
			"		try {}\n" +
			"		finally {\n" +
			"			if (x == null) x = \"foo\";\n" +
			"			if (x != null) y = \"bar\";\n" +
			"			x.length();\n" +   // shouldn't warn here
			"			y.length();\n" +   // shouldn't warn here
			"		}\n" +
			"	}\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	if (x == null) x = \"foo\";\n" +
		"	    ^\n" +
		"Redundant null check: The variable x can only be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	if (x != null) y = \"bar\";\n" +
		"	    ^\n" +
		"Redundant null check: The variable x cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=190623
// Test that a redundant null check doesn't affect the null status of
// a variable downstream.
public void testBug190623() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"        Number n = getNumber();\n" +
			"        if (n instanceof Double) {\n" +
			"            Double d= (Double) n;\n" +
			"            if (d != null && d.isNaN()) {\n" +
			"                System.out.println(\"outside loop\");\n" +
			"            }\n" +
			"            for (int i= 0; i < 10; i++) {\n" +
			"                if (d != null && d.isNaN()) {\n" +
			"                    System.out.println(\"inside loop\");\n" +
			"                }\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"    private static Number getNumber() {\n" +
			"        return Double.valueOf(Math.sqrt(-1));\n" +
			"    }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	if (d != null && d.isNaN()) {\n" +
		"	    ^\n" +
		"Redundant null check: The variable d cannot be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	if (d != null && d.isNaN()) {\n" +
		"	    ^\n" +
		"Redundant null check: The variable d cannot be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=299900
//Test to verify that null checks are properly reported for the variable(s)
//in the right expression of an OR condition statement.
public void testBug299900a() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(Object foo, Object bar) {\n" +
			"    if(foo == null || bar == null) {\n" +
			"	 	System.out.println(foo.toString());\n" +
			"	 	System.out.println(bar.toString());\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	System.out.println(foo.toString());\n" +
		"	                   ^^^\n" +
		"Potential null pointer access: The variable foo may be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	System.out.println(bar.toString());\n" +
		"	                   ^^^\n" +
		"Potential null pointer access: The variable bar may be null at this location\n" +
		"----------\n");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=299900
//Test to verify that null checks are properly reported for the variable(s)
//in the right expression of an OR condition statement.
public void testBug299900b() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(Object foo, Object bar) {\n" +
			"    if(foo == null || bar == null) {\n" +
			"    }\n" +
			"	 System.out.println(foo.toString());\n" +
			"	 System.out.println(bar.toString());\n" +
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	System.out.println(foo.toString());\n" +
		"	                   ^^^\n" +
		"Potential null pointer access: The variable foo may be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	System.out.println(bar.toString());\n" +
		"	                   ^^^\n" +
		"Potential null pointer access: The variable bar may be null at this location\n" +
		"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=253896
// Test whether Null pointer access warnings are being reported correctly when auto-unboxing
public void testBug253896a() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeNullTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public void foo() {\n" +
				"    Integer f1 = null;\n" +
				"	 if(f1 == 1)\n" +
				" 	 	System.out.println(\"f1 is 1\");\n" +
				"    Integer f2 = null;\n" +
				"	 int abc = (f2 != 1)? 1 : 0;\n" +
				"    Float f3 = null;\n" +
				"	 if(f3 == null)\n" +
				" 	 	System.out.println(\"f3 is null\");\n" +
				"    Byte f4 = null;\n" +
				"	 if(f4 != null)\n" +
				" 	 	System.out.println(\"f4 is not null\");\n" +
				"  }\n" +
				"}"},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	if(f1 == 1)\n" +
			"	   ^^\n" +
			"Null pointer access: This expression of type Integer is null but requires auto-unboxing\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	int abc = (f2 != 1)? 1 : 0;\n" +
			"	           ^^\n" +
			"Null pointer access: This expression of type Integer is null but requires auto-unboxing\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 9)\n" +
			"	if(f3 == null)\n" +
			"	   ^^\n" +
			"Redundant null check: The variable f3 can only be null at this location\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 12)\n" +
			"	if(f4 != null)\n" +
			"	   ^^\n" +
			"Null comparison always yields false: The variable f4 can only be null at this location\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 13)\n" +
			"	System.out.println(\"f4 is not null\");\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n");
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=253896
// To test whether null pointer access and potential null pointer access warnings are correctly reported when auto-unboxing
public void testBug253896b() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeNullTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public void foo(Integer i1, Integer i2) {\n" +
				"	 if(i1 == null && i2 == null){\n" +
				"		if(i1 == 1)\n" +
				" 	 	System.out.println(i1);}\n" +	//i1 is definitely null here
				"	 else {\n" +
				"		if(i1 == 0) {}\n" +		//i1 may be null here.
				"	 }\n" +
				"  }\n" +
				"}"},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	if(i1 == 1)\n" +
			"	   ^^\n" +
			"Null pointer access: This expression of type Integer is null but requires auto-unboxing\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	if(i1 == 0) {}\n" +
			"	   ^^\n" +
			"Potential null pointer access: This expression of type Integer may be null but requires auto-unboxing\n" +
			"----------\n");
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=253896
// Test whether Null pointer access warnings are being reported correctly when auto-unboxing inside loops
public void testBug253896c() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeNullTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public void foo() {\n" +
				"	 Integer a = null;\n" +
				"	 Integer outer2 = null;\n" +
				"	 while (true) {\n" +
				"    	Integer f1 = null;\n" +
				"	 	if(f1 == 1)\n" +
				" 	 		System.out.println(\"f1 is 1\");\n" +
				"    	Integer f2 = null;\n" +
				"	 	int abc = (f2 != 1)? 1 : 0;\n" +
				"    	Float f3 = null;\n" +
				"	 	if(f3 == null)\n" +
				" 	 		System.out.println(\"f3 is null\");\n" +
				"    	Byte f4 = null;\n" +
				"	 	if(f4 != null)\n" +
				" 	 		System.out.println(\"f4 is not null\");\n" +
				"		if(a == 1) {}\n" +	// warn null reference in deferred check case
				"		if(outer2 == 1) {}\n" +	// warn potential null reference in deferred check case
				"		outer2 = 1;\n" +
				"	 }\n" +
				"  }\n" +
				"}"},
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	if(f1 == 1)\n" +
			"	   ^^\n" +
			"Null pointer access: This expression of type Integer is null but requires auto-unboxing\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 10)\n" +
			"	int abc = (f2 != 1)? 1 : 0;\n" +
			"	           ^^\n" +
			"Null pointer access: This expression of type Integer is null but requires auto-unboxing\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 12)\n" +
			"	if(f3 == null)\n" +
			"	   ^^\n" +
			"Redundant null check: The variable f3 can only be null at this location\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 15)\n" +
			"	if(f4 != null)\n" +
			"	   ^^\n" +
			"Null comparison always yields false: The variable f4 can only be null at this location\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 16)\n" +
			"	System.out.println(\"f4 is not null\");\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 17)\n" +
			"	if(a == 1) {}\n" +
			"	   ^\n" +
			"Null pointer access: This expression of type Integer is null but requires auto-unboxing\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 18)\n" +
			"	if(outer2 == 1) {}\n" +
			"	   ^^^^^^\n" +
			"Potential null pointer access: This expression of type Integer may be null but requires auto-unboxing\n" +
			"----------\n");
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=253896
// Test whether Null pointer access warnings are being reported correctly when auto-unboxing inside finally contexts
public void testBug253896d() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeNullTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public void foo(Integer param) {\n" +
				"	 Integer outer = null;\n" +
				"	 if (param == null) {}\n" +	//tainting param
				"	 try {}\n" +
				"	 finally {\n" +
				"    	Integer f1 = null;\n" +
				"	 	if(f1 == 1)\n" +
				" 	 		System.out.println(\"f1 is 1\");\n" +
				"    	Integer f2 = null;\n" +
				"	 	int abc = (f2 != 1)? 1 : 0;\n" +
				"    	Float f3 = null;\n" +
				"	 	if(f3 == null)\n" +
				" 	 		System.out.println(\"f3 is null\");\n" +
				"    	Byte f4 = null;\n" +
				"	 	if(f4 != null)\n" +
				" 	 		System.out.println(\"f4 is not null\");\n" +
				"		if(outer == 1) {}\n" +  // warn null reference in deferred check case
				"		if(param == 1) {}\n" +
				"	 }\n" +
				"  }\n" +
				"}"},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	if(f1 == 1)\n" +
			"	   ^^\n" +
			"Null pointer access: This expression of type Integer is null but requires auto-unboxing\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 11)\n" +
			"	int abc = (f2 != 1)? 1 : 0;\n" +
			"	           ^^\n" +
			"Null pointer access: This expression of type Integer is null but requires auto-unboxing\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 13)\n" +
			"	if(f3 == null)\n" +
			"	   ^^\n" +
			"Redundant null check: The variable f3 can only be null at this location\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 16)\n" +
			"	if(f4 != null)\n" +
			"	   ^^\n" +
			"Null comparison always yields false: The variable f4 can only be null at this location\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 17)\n" +
			"	System.out.println(\"f4 is not null\");\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 18)\n" +
			"	if(outer == 1) {}\n" +
			"	   ^^^^^\n" +
			"Null pointer access: This expression of type Integer is null but requires auto-unboxing\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 19)\n" +
			"	if(param == 1) {}\n" +
			"	   ^^^^^\n" +
			"Potential null pointer access: This expression of type Integer may be null but requires auto-unboxing\n" +
			"----------\n");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=303448
//To check that code gen is not optimized for an if statement
//where a local variable's definite nullness or otherwise is known because of
//an earlier assert expression (inside finally context)
public void testBug303448a() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	if (this.complianceLevel >= ClassFileConstants.JDK1_4) {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public void foo() {\n" +
				"		Object foo = null;\n" +
				"		Object foo2 = null;\n" +
				"		try {} \n" +
				"		finally {\n" +
				"		assert (foo != null && foo2 != null);\n" +
				"		if (foo != null) {\n" +
				"			System.out.println(\"foo is not null\");\n" +
				"		} else {\n" +
				"			System.out.println(\"foo is null\");\n" +
				"		}\n" +
				"		if (foo2 != null) {\n" +
				"			System.out.println(\"foo2 is not null\");\n" +
				"		} else {\n" +
				"			System.out.println(\"foo2 is null\");\n" +
				"		}\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
			},
			"",
			null,
			true,
			null,
			options,
			null); // custom requestor

		String expectedOutput = this.complianceLevel < ClassFileConstants.JDK1_5?
				"  // Method descriptor #11 ()V\n" +
				"  // Stack: 2, Locals: 3\n" +
				"  public void foo();\n" +
				"     0  aconst_null\n" +
				"     1  astore_1 [foo]\n" +
				"     2  aconst_null\n" +
				"     3  astore_2 [foo2]\n" +
				"     4  getstatic X.$assertionsDisabled : boolean [38]\n" +
				"     7  ifne 26\n" +
				"    10  aload_1 [foo]\n" +
				"    11  ifnull 18\n" +
				"    14  aload_2 [foo2]\n" +
				"    15  ifnonnull 26\n" +
				"    18  new java.lang.AssertionError [49]\n" +
				"    21  dup\n" +
				"    22  invokespecial java.lang.AssertionError() [51]\n" +
				"    25  athrow\n" +
				"    26  aload_1 [foo]\n" +
				"    27  ifnull 41\n" +
				"    30  getstatic java.lang.System.out : java.io.PrintStream [52]\n" +
				"    33  ldc <String \"foo is not null\"> [58]\n" +
				"    35  invokevirtual java.io.PrintStream.println(java.lang.String) : void [60]\n" +
				"    38  goto 49\n" +
				"    41  getstatic java.lang.System.out : java.io.PrintStream [52]\n" +
				"    44  ldc <String \"foo is null\"> [65]\n" +
				"    46  invokevirtual java.io.PrintStream.println(java.lang.String) : void [60]\n" +
				"    49  aload_2 [foo2]\n" +
				"    50  ifnull 64\n" +
				"    53  getstatic java.lang.System.out : java.io.PrintStream [52]\n" +
				"    56  ldc <String \"foo2 is not null\"> [67]\n" +
				"    58  invokevirtual java.io.PrintStream.println(java.lang.String) : void [60]\n" +
				"    61  goto 72\n" +
				"    64  getstatic java.lang.System.out : java.io.PrintStream [52]\n" +
				"    67  ldc <String \"foo2 is null\"> [69]\n" +
				"    69  invokevirtual java.io.PrintStream.println(java.lang.String) : void [60]\n" +
				"    72  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 3]\n" +
				"        [pc: 2, line: 4]\n" +
				"        [pc: 4, line: 7]\n" +
				"        [pc: 26, line: 8]\n" +
				"        [pc: 30, line: 9]\n" +
				"        [pc: 38, line: 10]\n" +
				"        [pc: 41, line: 11]\n" +
				"        [pc: 49, line: 13]\n" +
				"        [pc: 53, line: 14]\n" +
				"        [pc: 61, line: 15]\n" +
				"        [pc: 64, line: 16]\n" +
				"        [pc: 72, line: 19]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 73] local: this index: 0 type: X\n" +
				"        [pc: 2, pc: 73] local: foo index: 1 type: java.lang.Object\n" +
				"        [pc: 4, pc: 73] local: foo2 index: 2 type: java.lang.Object\n"
			: 	this.complianceLevel < ClassFileConstants.JDK1_6?
						"  // Method descriptor #8 ()V\n" +
						"  // Stack: 2, Locals: 3\n" +
						"  public void foo();\n" +
						"     0  aconst_null\n" +
						"     1  astore_1 [foo]\n" +
						"     2  aconst_null\n" +
						"     3  astore_2 [foo2]\n" +
						"     4  getstatic X.$assertionsDisabled : boolean [16]\n" +
						"     7  ifne 26\n" +
						"    10  aload_1 [foo]\n" +
						"    11  ifnull 18\n" +
						"    14  aload_2 [foo2]\n" +
						"    15  ifnonnull 26\n" +
						"    18  new java.lang.AssertionError [26]\n" +
						"    21  dup\n" +
						"    22  invokespecial java.lang.AssertionError() [28]\n" +
						"    25  athrow\n" +
						"    26  aload_1 [foo]\n" +
						"    27  ifnull 41\n" +
						"    30  getstatic java.lang.System.out : java.io.PrintStream [29]\n" +
						"    33  ldc <String \"foo is not null\"> [35]\n" +
						"    35  invokevirtual java.io.PrintStream.println(java.lang.String) : void [37]\n" +
						"    38  goto 49\n" +
						"    41  getstatic java.lang.System.out : java.io.PrintStream [29]\n" +
						"    44  ldc <String \"foo is null\"> [43]\n" +
						"    46  invokevirtual java.io.PrintStream.println(java.lang.String) : void [37]\n" +
						"    49  aload_2 [foo2]\n" +
						"    50  ifnull 64\n" +
						"    53  getstatic java.lang.System.out : java.io.PrintStream [29]\n" +
						"    56  ldc <String \"foo2 is not null\"> [45]\n" +
						"    58  invokevirtual java.io.PrintStream.println(java.lang.String) : void [37]\n" +
						"    61  goto 72\n" +
						"    64  getstatic java.lang.System.out : java.io.PrintStream [29]\n" +
						"    67  ldc <String \"foo2 is null\"> [47]\n" +
						"    69  invokevirtual java.io.PrintStream.println(java.lang.String) : void [37]\n" +
						"    72  return\n" +
						"      Line numbers:\n" +
						"        [pc: 0, line: 3]\n" +
						"        [pc: 2, line: 4]\n" +
						"        [pc: 4, line: 7]\n" +
						"        [pc: 26, line: 8]\n" +
						"        [pc: 30, line: 9]\n" +
						"        [pc: 38, line: 10]\n" +
						"        [pc: 41, line: 11]\n" +
						"        [pc: 49, line: 13]\n" +
						"        [pc: 53, line: 14]\n" +
						"        [pc: 61, line: 15]\n" +
						"        [pc: 64, line: 16]\n" +
						"        [pc: 72, line: 19]\n" +
						"      Local variable table:\n" +
						"        [pc: 0, pc: 73] local: this index: 0 type: X\n" +
						"        [pc: 2, pc: 73] local: foo index: 1 type: java.lang.Object\n" +
						"        [pc: 4, pc: 73] local: foo2 index: 2 type: java.lang.Object\n"
					:	"  // Method descriptor #8 ()V\n" +
						"  // Stack: 2, Locals: 3\n" +
						"  public void foo();\n" +
						"     0  aconst_null\n" +
						"     1  astore_1 [foo]\n" +
						"     2  aconst_null\n" +
						"     3  astore_2 [foo2]\n" +
						"     4  getstatic X.$assertionsDisabled : boolean [16]\n" +
						"     7  ifne 26\n" +
						"    10  aload_1 [foo]\n" +
						"    11  ifnull 18\n" +
						"    14  aload_2 [foo2]\n" +
						"    15  ifnonnull 26\n" +
						"    18  new java.lang.AssertionError [27]\n" +
						"    21  dup\n" +
						"    22  invokespecial java.lang.AssertionError() [29]\n" +
						"    25  athrow\n" +
						"    26  aload_1 [foo]\n" +
						"    27  ifnull 41\n" +
						"    30  getstatic java.lang.System.out : java.io.PrintStream [30]\n" +
						"    33  ldc <String \"foo is not null\"> [36]\n" +
						"    35  invokevirtual java.io.PrintStream.println(java.lang.String) : void [38]\n" +
						"    38  goto 49\n" +
						"    41  getstatic java.lang.System.out : java.io.PrintStream [30]\n" +
						"    44  ldc <String \"foo is null\"> [44]\n" +
						"    46  invokevirtual java.io.PrintStream.println(java.lang.String) : void [38]\n" +
						"    49  aload_2 [foo2]\n" +
						"    50  ifnull 64\n" +
						"    53  getstatic java.lang.System.out : java.io.PrintStream [30]\n" +
						"    56  ldc <String \"foo2 is not null\"> [46]\n" +
						"    58  invokevirtual java.io.PrintStream.println(java.lang.String) : void [38]\n" +
						"    61  goto 72\n" +
						"    64  getstatic java.lang.System.out : java.io.PrintStream [30]\n" +
						"    67  ldc <String \"foo2 is null\"> [48]\n" +
						"    69  invokevirtual java.io.PrintStream.println(java.lang.String) : void [38]\n" +
						"    72  return\n" +
						"      Line numbers:\n" +
						"        [pc: 0, line: 3]\n" +
						"        [pc: 2, line: 4]\n" +
						"        [pc: 4, line: 7]\n" +
						"        [pc: 26, line: 8]\n" +
						"        [pc: 30, line: 9]\n" +
						"        [pc: 38, line: 10]\n" +
						"        [pc: 41, line: 11]\n" +
						"        [pc: 49, line: 13]\n" +
						"        [pc: 53, line: 14]\n" +
						"        [pc: 61, line: 15]\n" +
						"        [pc: 64, line: 16]\n" +
						"        [pc: 72, line: 19]\n" +
						"      Local variable table:\n" +
						"        [pc: 0, pc: 73] local: this index: 0 type: X\n" +
						"        [pc: 2, pc: 73] local: foo index: 1 type: java.lang.Object\n" +
						"        [pc: 4, pc: 73] local: foo2 index: 2 type: java.lang.Object\n" +
						"      Stack map table: number of frames 6\n" +
						"        [pc: 18, append: {java.lang.Object, java.lang.Object}]\n" +
						"        [pc: 26, same]\n" +
						"        [pc: 41, same]\n" +
						"        [pc: 49, same]\n" +
						"        [pc: 64, same]\n" +
						"        [pc: 72, same]\n";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=303448
//To check that code gen is not optimized for an if statement
//where a local variable's definite nullness or otherwise is known because of
//an earlier assert expression (inside finally context)
public void testBug303448b() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	if (this.complianceLevel >= ClassFileConstants.JDK1_4) {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.print(\"start\");\n" +
				"		Object foo = null;\n" +
				"		assert (foo != null);\n" +
				"		if (foo != null) {\n" +
				"			System.out.println(\"foo is not null\");\n" +
				"		}\n" +
				"		System.out.print(\"end\");\n" +
				"	}\n" +
				"}\n",
			},
			"startend",
			null,
			true,
			null,
			options,
			null);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=304416
public void testBug304416() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = null;\n" +
			"		String s2 = null;\n" +
			"		if (s != null && s2 != null) {\n" +
			"			System.out.println(s);\n" +
			"			System.out.println(s2);\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"",
		null,
		true,
		null,
		options,
		null);
	String expectedOutput =
		"  public static void main(java.lang.String[] args);\n" +
		"     0  aconst_null\n" +
		"     1  astore_1 [s]\n" +
		"     2  aconst_null\n" +
		"     3  astore_2 [s2]\n" +
		"     4  aload_1 [s]\n" +
		"     5  ifnull 26\n" +
		"     8  aload_2 [s2]\n" +
		"     9  ifnull 26\n" +
		"    12  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    15  aload_1 [s]\n" +
		"    16  invokevirtual java.io.PrintStream.println(java.lang.String) : void [22]\n" +
		"    19  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    22  aload_2 [s2]\n" +
		"    23  invokevirtual java.io.PrintStream.println(java.lang.String) : void [22]\n" +
		"    26  return\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=305590
// To verify that a "instanceof always yields false" warning is not elicited in the
// case when the expression has been assigned a non null value in the instanceof check.
public void testBug305590() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() {\n" +
			"	 Object str = null;\n" +
			"	 if ((str = \"str\") instanceof String) {}\n" + // shouldn't warn
			"	 str = null;\n" +
			"	 if ((str = \"str\") instanceof Number) {}\n" + // shouldn't warn
			"	 str = null;\n" +
			"	 if (str instanceof String) {}\n" + // should warn
			"  }\n" +
			"}"},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	if (str instanceof String) {}\n" +
		"	    ^^^\n" +
		"instanceof always yields false: The variable str can only be null at this location\n" +
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319201
// unboxing raises an NPE
//   LocalDeclaration
public void testBug319201() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public void foo() {\n" +
				"	 Integer i = null;\n" +
				"	 int j = i;\n" + // should warn
				"  }\n" +
				"}"},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	int j = i;\n" +
			"	        ^\n" +
			"Null pointer access: This expression of type Integer is null but requires auto-unboxing\n" +
			"----------\n",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319201
// unboxing could raise an NPE
//   Assignment
public void testBug319201a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public void foo(Integer i) {\n" +
				"    if (i == null) {};\n" +
				"	 int j;\n" +
				"	 j = i;\n" + // should warn
				"  }\n" +
				"}"},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	j = i;\n" +
			"	    ^\n" +
			"Potential null pointer access: This expression of type Integer may be null but requires auto-unboxing\n" +
			"----------\n",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319201
// unboxing raises an NPE
//   MessageSend
public void testBug319201b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public void foo() {\n" +
				"    Boolean bo = null;;\n" +
				"	 bar(bo);\n" + // should warn
				"  }\n" +
				"  void bar(boolean b) {}\n" +
				"}"},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	bar(bo);\n" +
			"	    ^^\n" +
			"Null pointer access: This expression of type Boolean is null but requires auto-unboxing\n" +
			"----------\n",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319201
// unboxing raises an NPE
// Node types covered (in this order):
//   ExplicitConstructorCall
//   AllocationExpression
//   AND_AND_Expression
//   OR_OR_Expression
//   ArrayAllocationExpression
//   ForStatement
//   DoStatement
//   IfStatement
//   QualifiedAllocationExpression
//   SwitchStatement
//   WhileStatement
//   CastExpression
//   AssertStatement
//   ReturnStatement
public void testBug319201c() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	runNegativeTest(
			new String[] {
              "X.java",
              "class Y { public Y(boolean b1, boolean b2) {} }\n" +
              "public class X extends Y {\n" +
              "  public X(boolean b, Boolean b2) {\n" +
              "      super(b2 == null, b2);\n" +
              "  }\n" +
              "  class Z {\n" +
              "      public Z(boolean b) {}\n" +
              "  }\n" +
              "  boolean fB = (Boolean)null;\n" +
              "  public boolean foo(boolean inB) {\n" +
              "      Boolean b1 = null;\n" +
              "      X x = new X(b1, null);\n" +
              "      Boolean b2 = null;\n" +
              "      boolean dontcare = b2 && inB;\n" +
              "      Boolean b3 = null;\n" +
              "      dontcare = inB || b3;\n" +
              "      Integer dims = null;\n" +
              "      char[] cs = new char[dims];\n" +
              "      Boolean b5 = null;\n" +
              "      do {\n" +
              "          Boolean b4 = null;\n" +
              "          for (int i=0;b4; i++);\n" +
              "      } while (b5);\n" +
              "      Boolean b6 = null;\n" +
              "      if (b6) { }\n" +
              "      Boolean b7 = null;\n" +
              "      Z z = this.new Z(b7);\n" +
              "      Integer sel = null;\n" +
              "      switch(sel) {\n" +
              "          case 1: break;\n" +
              "          default: break;\n" +
              "      }\n" +
              "      Boolean b8 = null;\n" +
              "      while (b8) {}\n" +
              "      Boolean b9 = null;\n" +
              "      dontcare = (boolean)b9;\n" +
              "      Boolean b10 = null;\n" +
              "      assert b10 : \"shouldn't happen, but will\";\n" +
              "      Boolean b11 = null;\n" +
              "      return b11;\n" +
              "  }\n" +
				"}"},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	super(b2 == null, b2);\n" +
			"	                  ^^\n" +
			"Potential null pointer access: This expression of type Boolean may be null but requires auto-unboxing\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 9)\n" +
			"	boolean fB = (Boolean)null;\n" +
			"	             ^^^^^^^^^^^^^\n" +
			"Null pointer access: This expression of type Boolean is null but requires auto-unboxing\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 12)\n" +
			"	X x = new X(b1, null);\n" +
			"	            ^^\n" +
			"Null pointer access: This expression of type Boolean is null but requires auto-unboxing\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 14)\n" +
			"	boolean dontcare = b2 && inB;\n" +
			"	                   ^^\n" +
			"Null pointer access: This expression of type Boolean is null but requires auto-unboxing\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 16)\n" +
			"	dontcare = inB || b3;\n" +
			"	                  ^^\n" +
			"Null pointer access: This expression of type Boolean is null but requires auto-unboxing\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 18)\n" +
			"	char[] cs = new char[dims];\n" +
			"	                     ^^^^\n" +
			"Null pointer access: This expression of type Integer is null but requires auto-unboxing\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 22)\n" +
			"	for (int i=0;b4; i++);\n" +
			"	             ^^\n" +
			"Null pointer access: This expression of type Boolean is null but requires auto-unboxing\n" +
			"----------\n" +
			"8. ERROR in X.java (at line 23)\n" +
			"	} while (b5);\n" +
			"	         ^^\n" +
			"Null pointer access: This expression of type Boolean is null but requires auto-unboxing\n" +
			"----------\n" +
			"9. ERROR in X.java (at line 25)\n" +
			"	if (b6) { }\n" +
			"	    ^^\n" +
			"Null pointer access: This expression of type Boolean is null but requires auto-unboxing\n" +
			"----------\n" +
			"10. ERROR in X.java (at line 27)\n" +
			"	Z z = this.new Z(b7);\n" +
			"	                 ^^\n" +
			"Null pointer access: This expression of type Boolean is null but requires auto-unboxing\n" +
			"----------\n" +
			"11. ERROR in X.java (at line 29)\n" +
			"	switch(sel) {\n" +
			"	       ^^^\n" +
			"Null pointer access: This expression of type Integer is null but requires auto-unboxing\n" +
			"----------\n" +
			"12. ERROR in X.java (at line 34)\n" +
			"	while (b8) {}\n" +
			"	       ^^\n" +
			"Null pointer access: This expression of type Boolean is null but requires auto-unboxing\n" +
			"----------\n" +
			"13. ERROR in X.java (at line 36)\n" +
			"	dontcare = (boolean)b9;\n" +
			"	                    ^^\n" +
			"Null pointer access: This expression of type Boolean is null but requires auto-unboxing\n" +
			"----------\n" +
			"14. ERROR in X.java (at line 38)\n" +
			"	assert b10 : \"shouldn\'t happen, but will\";\n" +
			"	       ^^^\n" +
			"Null pointer access: This expression of type Boolean is null but requires auto-unboxing\n" +
			"----------\n" +
			"15. ERROR in X.java (at line 40)\n" +
			"	return b11;\n" +
			"	       ^^^\n" +
			"Null pointer access: This expression of type Boolean is null but requires auto-unboxing\n" +
			"----------\n",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319201
// unboxing raises an NPE
// DoStatement, variants with assignement and/or continue in the body & empty body
public void testBug319201d() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryElse, CompilerOptions.IGNORE);
	runNegativeTest(
			new String[] {
              "X.java",
              "public class X {\n" +
              "  public void foo(boolean cond, boolean cond2) {\n" +
              "      Boolean b = null;\n" +
              "      do {\n" +
              "          b = false;\n" +
              "          if (cond) continue;\n" +   // shouldn't make a difference
              "      } while (b);\n" + // don't complain, loop body has already assigned b
              "      Boolean b2 = null;\n" +
              "      do {\n" +
              "          if (cond) continue;\n" +
              "          b2 = false;\n" +
              "      } while (b2);\n" + // complain here: potentially null
              "      Boolean b3 = null;\n" +
              "      do {\n" +
              "      } while (b3);\n" + // complain here: definitely null
              "      Boolean b4 = null;\n" +
              "      do {\n" +
              "        if (cond) {\n" +
              "            b4 = true;\n" +
              "            if (cond2) continue;\n" +
              "        }\n" +
              "        b4 = false;\n" +
              "      } while (b4);\n" + // don't complain here: definitely non-null
              "      Boolean b5 = null;\n" +
              "      do {\n" +
              "         b5 = true;\n" +
              "      } while (b5);\n" +  // don't complain
              "      Boolean b6 = null;\n" +
              "      do {\n" +
              "         b6 = true;\n" +
              "         continue;\n" +
              "      } while (b6); \n" + // don't complain
              "      Boolean b7 = null;\n" +
              "      Boolean b8 = null;\n" +
              "      do {\n" +
              "        if (cond) {\n" +
              "            b7 = true;\n" +
              "            continue;\n" +
              "        } else {\n" +
              "            b8 = true;\n" +
              "        }\n" +
              "      } while (b7);\n" + // complain here: after else branch b7 can still be null
              "  }\n" +
			  "}"},
			"----------\n" +
			"1. ERROR in X.java (at line 12)\n" +
			"	} while (b2);\n" +
			"	         ^^\n" +
			"Potential null pointer access: This expression of type Boolean may be null but requires auto-unboxing\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 15)\n" +
			"	} while (b3);\n" +
			"	         ^^\n" +
			"Null pointer access: This expression of type Boolean is null but requires auto-unboxing\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 42)\n" +
			"	} while (b7);\n" +
			"	         ^^\n" +
			"Potential null pointer access: This expression of type Boolean may be null but requires auto-unboxing\n" +
			"----------\n",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions,
			"",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=320414
public void testBug320414() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	static class B {\n" +
			"		public static final int CONST = 16;\n" +
			"		int i;\n" +
			"	}\n" +
			"	B b;\n" +
			"	public static void main(String[] args) {\n" +
			"		new X().foo();\n" +
			"	}\n" +
			"	void foo() {\n" +
			"		B localB = b; \n" +
			"		int i = localB.CONST;\n" +
			"		if (localB != null) {\n" +
			"			i = localB.i;\n" +
			"		}\n" +
			"		System.out.println(i);\n" +
			"	}\n" +
			"}",
		},
		"16",
		null,
		true,
		null,
		options,
		null);
	String expectedOutput =
		"  void foo();\n" +
		"     0  aload_0 [this]\n" +
		"     1  getfield X.b : X.B [24]\n" +
		"     4  astore_1 [localB]\n" +
		"     5  bipush 16\n" +
		"     7  istore_2 [i]\n" +
		"     8  aload_1 [localB]\n" +
		"     9  ifnull 17\n" +
		"    12  aload_1 [localB]\n" +
		"    13  getfield X$B.i : int [26]\n" +
		"    16  istore_2 [i]\n" +
		"    17  getstatic java.lang.System.out : java.io.PrintStream [32]\n" +
		"    20  iload_2 [i]\n" +
		"    21  invokevirtual java.io.PrintStream.println(int) : void [38]\n" +
		"    24  return\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=321926
// To verify that a "redundant null check" warning is NOT elicited for a variable assigned non-null
// in an infinite while loop inside a try catch block and that code generation shows no surprises.
public void testBug321926a() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"		while (true) {\n" +
			"			if (i == 0){\n" +
			"				someVariable = \"not null\";\n" +
			"				i++;\n" +
			"			}\n" +
			"			else\n" +
			"				throw new IOException();\n" +
			"		}\n" +
			"	 } catch (IOException e) {\n" +
			"		// broken from loop, continue on\n" +
			"	 }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler buggy\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler good\");\n" +
			"	 }\n" +
			"  }\n" +
			"}"},
		"Compiler good");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=321926
// need more precise info from the throw location
public void testBug321926a2() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses @SW annotation
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNUSED_WARNING_TOKEN, JavaCore.ERROR);
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"	@SuppressWarnings(\"null\")\n" + // expecting "redundant null check" at "if (someVariable == null)"
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"		while (true) {\n" +
			"			if (i == 0){\n" +
			"				someVariable = \"not null\";\n" +
			"				i++;\n" +
			"			}\n" +
			"			else {\n" +
			"				someVariable = \"value\";\n" +
			"				throw new IOException();\n" +
			"			}\n" +
			"		}\n" +
			"	 } catch (IOException e) {\n" +
			"		// broken from loop, continue on\n" +
			"	 }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler buggy\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler good\");\n" +
			"	 }\n" +
			"  }\n" +
			"}"},
		"Compiler good",
		options);
}
// Test that dead code warning does show up.
public void testBug321926b() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"		while (true) {\n" +
			"			if (i == 0){\n" +
			"				someVariable = \"not null\";\n" +
			"				i++;\n" +
			"			}\n" +
			"			else\n" +
			"				throw new IOException();\n" +
			"		}\n" +
			"       System.out.println(\"This is dead code\");\n" +
			"	 } catch (IOException e) {\n" +
			"		// broken from loop, continue on\n" +
			"	 }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler buggy\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler good\");\n" +
			"	 }\n" +
			"  }\n" +
			"}"},
			"----------\n" +
			"1. ERROR in X.java (at line 15)\n" +
			"	System.out.println(\"This is dead code\");\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Unreachable code\n" +
			"----------\n");
}
// Check nullness in catch block, finally block and downstream code.
public void testBug321926c() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"		while (true) {\n" +
			"			if (i == 0){\n" +
			"				someVariable = \"not null\";\n" +
			"				i++;\n" +
			"			}\n" +
			"			else\n" +
			"				throw new IOException();\n" +
			"		}\n" +
			"	 } catch (IOException e) {\n" +
			"	 	if (someVariable == null) {\n" +
			"    		System.out.println(\"Compiler buggy\");\n" +
			"	 	} else {\n" +
			"			System.out.print(\"Compiler good \");\n" +
			"	 	}\n" +
			"	 } finally {\n" +
			"	 	if (someVariable == null) {\n" +
			"    		System.out.println(\"Compiler buggy\");\n" +
			"	 	} else {\n" +
			"			System.out.print(\"Compiler good \");\n" +
			"	 	}\n" +
            "    }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler buggy\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler good\");\n" +
			"	 }\n" +
			"  }\n" +
			"}"},
		"Compiler good Compiler good Compiler good");
}
// Various nested loops.
public void testBug321926d() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"       while (true) {\n" +
			"           for(;;) { \n" +
			"				while (true) {\n" +
			"					if (i == 0){\n" +
			"						someVariable = \"not null\";\n" +
			"						i++;\n" +
			"					}\n" +
			"					else\n" +
			"						throw new IOException();\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	 } catch (IOException e) {\n" +
			"	 	if (someVariable == null) {\n" +
			"    		System.out.println(\"Compiler buggy\");\n" +
			"	 	} else {\n" +
			"			System.out.print(\"Compiler good \");\n" +
			"	 	}\n" +
			"	 } finally {\n" +
			"	 	if (someVariable == null) {\n" +
			"    		System.out.println(\"Compiler buggy\");\n" +
			"	 	} else {\n" +
			"			System.out.print(\"Compiler good \");\n" +
			"	 	}\n" +
            "    }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler buggy\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler good\");\n" +
			"	 }\n" +
			"  }\n" +
			"}"},
		"Compiler good Compiler good Compiler good");
}
// Test widening catch.
public void testBug321926e() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"		while (true) {\n" +
			"			if (i == 0){\n" +
			"				someVariable = \"not null\";\n" +
			"				i++;\n" +
			"			}\n" +
			"			else\n" +
			"				throw new IOException();\n" +
			"		}\n" +
			"	 } catch (Exception e) {\n" +
			"		// broken from loop, continue on\n" +
			"	 }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler buggy\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler good\");\n" +
			"	 }\n" +
			"  }\n" +
			"}"},
		"Compiler good");
}
// Tested nested try blocks.
public void testBug321926f() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"        String someVariable = null;\n" +
			"        int i = 0;\n" +
			"        try {\n" +
			"        	while (true) {\n" +
			"        		if (i != 0) {\n" +
			"        			try {\n" +
			"        				throw new IOException();\n" +
			"        			} catch (IOException e) {\n" +
			"        				if (someVariable == null) {\n" +
			"        					System.out.println(\"The compiler is buggy\");\n" +
			"        				} else {\n" +
			"        					System.out.print(\"Compiler good \");\n" +
			"        				}\n" +
			"        				throw e;\n" +
			"        			}\n" +
			"        		} else {\n" +
			"        			someVariable = \"not null\";\n" +
			"        			i++;\n" +
			"        		}\n" +
			"        	}\n" +
			"        } catch (Exception e) {\n" +
			"            // having broken from loop, continue on\n" +
			"        }\n" +
			"        if (someVariable == null) {\n" +
			"            System.out.println(\"The compiler is buggy\");\n" +
			"        } else {\n" +
			"            System.out.println(\"Compiler good\");\n" +
			"        }\n" +
			"    }\n" +
			"}\n"},
		"Compiler good Compiler good");
}
// test for loop
public void testBug321926g() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"		for (int j = 0; true; j++) {\n" +
			"			if (i == 0){\n" +
			"				someVariable = \"not null\";\n" +
			"				i++;\n" +
			"			}\n" +
			"			else\n" +
			"				throw new IOException();\n" +
			"		}\n" +
			"	 } catch (IOException e) {\n" +
			"		// broken from loop, continue on\n" +
			"	 }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler buggy\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler good\");\n" +
			"	 }\n" +
			"  }\n" +
			"}"},
		"Compiler good");
}
// test do while loop
public void testBug321926h() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"		do {\n" +
			"			if (i == 0){\n" +
			"				someVariable = \"not null\";\n" +
			"				i++;\n" +
			"			}\n" +
			"			else\n" +
			"				throw new IOException();\n" +
			"		} while(true);\n" +
			"	 } catch (IOException e) {\n" +
			"		// broken from loop, continue on\n" +
			"	 }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler buggy\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler good\");\n" +
			"	 }\n" +
			"  }\n" +
			"}"},
		"Compiler good");
}
// test with while (true) with a break inside. was working already.
public void testBug321926i() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"		while (true) {\n" +
			"			if (i == 0){\n" +
			"				someVariable = \"not null\";\n" +
			"				i++;\n" +
			"               break;\n" +
			"			}\n" +
			"			else\n" +
			"				throw new IOException();\n" +
			"		}\n" +
			"	 } catch (IOException e) {\n" +
			"		// broken from loop, continue on\n" +
			"	 }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler buggy\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler good\");\n" +
			"	 }\n" +
			"  }\n" +
			"}"},
		"Compiler good");
}
// Test with non-explicit throws, i.e call method which throws rather than an inline throw statement.
public void testBug321926j() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"		while (true) {\n" +
			"			if (i == 0){\n" +
			"				someVariable = \"not null\";\n" +
			"				i++;\n" +
			"			}\n" +
			"			else\n" +
			"				invokeSomeMethod();\n" +
			"		}\n" +
			"	 } catch (IOException e) {\n" +
			"		// broken from loop, continue on\n" +
			"	 }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler buggy\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler good\");\n" +
			"	 }\n" +
			"  }\n" +
			"  public static void invokeSomeMethod() throws IOException {\n" +
			"      throw new IOException();\n" +
			"  }\n" +
			"}"},
		"Compiler good");
}
// Variation with nested loops
public void testBug321926k() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"       while (true) {\n" +
			"       	try {\n" +
			"				while (true) {\n" +
			"					if (i == 0){\n" +
			"						someVariable = \"not null\";\n" +
			"						i++;\n" +
			"					}\n" +
			"					else\n" +
			"						throw new IOException();\n" +
			"				}\n" +
			"       	} catch (IOException e) {\n" +
			"           }\n" +
			"	 		if (someVariable == null) {\n" +
			"    			System.out.println(\"Compiler buggy\");\n" +
			"	 		} else {\n" +
			"				System.out.print(\"Compiler good \");\n" +
			"	 		}\n" +
			"           throw new IOException();\n" +
			"       }\n" +
			"	 } catch (IOException e) {\n" +
			"		// broken from loop, continue on\n" +
			"	 }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler buggy\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler good\");\n" +
			"	 }\n" +
			"  }\n" +
			"}"},
		"Compiler good Compiler good");
}
// variation with nested loops.
public void testBug321926l() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.WARNING);

	this.runTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"       while (true) {\n" +
			"           someVariable = null;\n"+
			"       	try {\n" +
			"				while (true) {\n" +
			"					if (i == 0){\n" +
			"						someVariable = \"not null\";\n" +
			"						i++;\n" +
			"					}\n" +
			"					else\n" +
			"						throw new IOException();\n" +
			"				}\n" +
			"       	} catch (IOException e) {\n" +
			"           }\n" +
			"	 		if (someVariable == null) {\n" +
			"    			System.out.println(\"Compiler buggy\");\n" +
			"	 		} else {\n" +
			"				System.out.print(\"Compiler good \");\n" +
			"	 		}\n" +
			"           throw new IOException();\n" +
			"       }\n" +
			"	 } catch (IOException e) {\n" +
			"		// broken from loop, continue on\n" +
			"	 }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler buggy\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler good\");\n" +
			"	 }\n" +
			"  }\n" +
			"}"},
		 false,
		 "----------\n" +
		"1. WARNING in X.java (at line 8)\n" +
		"	someVariable = null;\n" +
		"	^^^^^^^^^^^^\n" +
		"Redundant assignment: The variable someVariable can only be null at this location\n" +
		"----------\n",
		"Compiler good Compiler good",
		"",
		true, // force execution
		null, // classlibs
		true, // flush output,
		null, // vm args
		options,
		null,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}
public void testBug321926m() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"		while (true) {\n" +
			"			if (i == 0){\n" +
			"				someVariable = \"not null\";\n" +
			"				i++;\n" +
			"			}\n" +
			"			else\n" +
			"				throw new IOException();\n" +
			"           if (true) {\n" +
			"               break;\n" +
			"           }\n" +
			"		}\n" +
			"	 } catch (IOException e) {\n" +
			"		// broken from loop, continue on\n" +
			"	 }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler buggy\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler good\");\n" +
			"	 }\n" +
			"  }\n" +
			"}"},
		"Compiler good");
}
public void testBug321926n() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.WARNING);
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"       someVariable = \"not null\";\n" +
			"		while (true) {\n" +
			"			if (i == 0){\n" +
			"				someVariable = \"not null\";\n" +
			"				i++;\n" +
			"			}\n" +
			"			else\n" +
			"				throw new IOException();\n" +
			"		}\n" +
			"	 } catch (IOException e) {\n" +
			"		// broken from loop, continue on\n" +
			"	 }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler buggy\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler good\");\n" +
			"	 }\n" +
			"  }\n" +
			"}"},
		"Compiler good",
		options);
}
public void testBug321926o() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.WARNING);
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"       someVariable = \"not null\";\n" +
			"		for(;;) {\n" +
			"			if (i == 0){\n" +
			"				someVariable = \"not null\";\n" +
			"				i++;\n" +
			"			}\n" +
			"			else\n" +
			"				throw new IOException();\n" +
			"		}\n" +
			"	 } catch (IOException e) {\n" +
			"		// broken from loop, continue on\n" +
			"	 }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler buggy\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler good\");\n" +
			"	 }\n" +
			"  }\n" +
			"}"},
		"Compiler good",
		options);
}
public void testBug321926p() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.WARNING);
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"       someVariable = \"not null\";\n" +
			"		do {\n" +
			"			if (i == 0){\n" +
			"				someVariable = \"not null\";\n" +
			"				i++;\n" +
			"			}\n" +
			"			else\n" +
			"				throw new IOException();\n" +
			"		} while (true);\n" +
			"	 } catch (IOException e) {\n" +
			"		// broken from loop, continue on\n" +
			"	 }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler buggy\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler good\");\n" +
			"	 }\n" +
			"  }\n" +
			"}"},
		"Compiler good",
		options);
}
public void testBug321926q() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"       someVariable = \"not null\";\n" +
			"		do {\n" +
			"			if (i == 0){\n" +
			"				someVariable = \"not null\";\n" +
			"				i++;\n" +
			"			}\n" +
			"			else\n" +
			"				throw new IOException();\n" +
			"		} while ((someVariable = \"not null\") != null);\n" +
			"	 } catch (IOException e) {\n" +
			"		// broken from loop, continue on\n" +
			"	 }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler buggy\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler good\");\n" +
			"	 }\n" +
			"  }\n" +
			"}"},
		"Compiler good", null, true, null, options, null);
}
public void testBug321926r() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"       while ((someVariable = \"not null\") != null) {\n" +
			"			if (i == 0){\n" +
			"				someVariable = \"not null\";\n" +
			"				i++;\n" +
			"			}\n" +
			"			else\n" +
			"				throw new IOException();\n" +
			"		}\n" +
			"	 } catch (IOException e) {\n" +
			"		// broken from loop, continue on\n" +
			"	 }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler buggy\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler good\");\n" +
			"	 }\n" +
			"  }\n" +
			"}"},
		"Compiler good", null, true, null, options, null
		);
}
public void testBug321926s() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"       someVariable = \" not null\";\n" +
			"       while ((someVariable = null) != null) {\n" +
			"			if (i == 0){\n" +
			"				someVariable = \"not null\";\n" +
			"				i++;\n" +
			"			}\n" +
			"			else\n" +
			"				throw new IOException();\n" +
			"		}\n" +
			"	 } catch (IOException e) {\n" +
			"		// broken from loop, continue on\n" +
			"	 }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler good\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler buggy\");\n" +
			"	 }\n" +
			"  }\n" +
			"}"},
		"Compiler good", null, true, null, options, null
		);
}
public void testBug321926t() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"	public static void main(String s[]) {\n" +
			"		String file = \"non null\";\n" +
			"		int i = 0;\n" +
			"       try {\n" +
			"			while (true) {\n" +
			"			    if (i == 0) {\n" +
			"					file = null;\n" +
			"                   i++;\n"+
			"               }\n" +
			"               else \n" +
			"               	throw new IOException();\n" +
			"			}\n" +
			"       } catch (IOException e) {\n" +
			"       }\n" +
			"		if (file == null)\n" +
			"		    System.out.println(\"Compiler good\");\n" +
			"       else \n" +
			"		    System.out.println(\"Compiler bad\");\n" +
			"	}\n" +
			"}\n"},
		"Compiler good");
}
public void testBug321926u() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"	public static void main(String s[]) {\n" +
			"		String file = \"non null\";\n" +
			"		int i = 0;\n" +
			"       try {\n" +
			"			while (true) {\n" +
			"			    if (i == 0) {\n" +
			"					file = null;\n" +
			"                   i++;\n"+
			"               }\n" +
			"               else {\n" +
			"                   file = null;\n" +
			"               	throw new IOException();\n" +
			"               }\n" +
			"			}\n" +
			"       } catch (IOException e) {\n" +
			"       }\n" +
			"		if (file == null)\n" +
			"		    System.out.println(\"Compiler good\");\n" +
			"       else \n" +
			"		    System.out.println(\"Compiler bad\");\n" +
			"	}\n" +
			"}\n"},
		"Compiler good",
		options);
}
public void testBug321926v() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"	public static void main(String s[]) {\n" +
			"		String file = null;\n" +
			"		int i = 0;\n" +
			"       try {\n" +
			"			while (true) {\n" +
			"			    if (i == 0) {\n" +
			"					file = \"non null\";\n" +
			"                   i++;\n"+
			"               }\n" +
			"               else {\n" +
			"                   file = \"non null\";\n" +
			"               	throw new IOException();\n" +
			"               }\n" +
			"			}\n" +
			"       } catch (IOException e) {\n" +
			"       }\n" +
			"		if (file == null)\n" +
			"		    System.out.println(\"Compiler bad\");\n" +
			"       else \n" +
			"		    System.out.println(\"Compiler good\");\n" +
			"	}\n" +
			"}\n"},
		"Compiler good",
		options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317829
public void testBug317829a() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.WARNING);
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"       someVariable = \"not null\";\n" +
			"		while (true) {\n" +
			"			throw new IOException();\n" +
			"		}\n" +
			"	 } catch (IOException e) {\n" +
			"	 	if (someVariable == null) {\n" +
			"    		System.out.println(\"Compiler bad\");\n" +
			"	 	} else {\n" +
			"			System.out.print(\"Compiler good \");\n" +
			"	 	}\n" +
			"	 }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler bad\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler good\");\n" +
			"	 }\n" +
			"  }\n" +
			"}"},
			"Compiler good Compiler good",
			options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317829
// assignment from unknown - not reporting redundant check
public void testBug317829a2() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"       someVariable = getString();\n" +
			"		while (true) {\n" +
			"			throw new IOException();\n" +
			"		}\n" +
			"	 } catch (IOException e) {\n" +
			"	 	if (someVariable == null) {\n" +
			"    		System.out.println(\"Compiler bad\");\n" +
			"	 	} else {\n" +
			"			System.out.print(\"Compiler good \");\n" +
			"	 	}\n" +
			"	 }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler bad\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler good\");\n" +
			"	 }\n" +
			"  }\n" +
			"  static String getString() { return \"\"; }\n" +
			"}"},
			"Compiler good Compiler good");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=317829
public void testBug317829b() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.WARNING);
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"       someVariable = \"not null\";\n" +
			"		while (true) {\n" +
			"			someMethod();\n" +
			"		}\n" +
			"	 } catch (IOException e) {\n" +
			"	 	if (someVariable == null) {\n" +
			"    		System.out.println(\"Compiler bad\");\n" +
			"	 	} else {\n" +
			"			System.out.print(\"Compiler good \");\n" +
			"	 	}\n" +
			"	 }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler bad\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler good\");\n" +
			"	 }\n" +
			"  }\n" +
			"  public static void someMethod() throws IOException {\n" +
			"      throw new IOException();\n" +
			"  }\n" +
			"}"},
			"Compiler good Compiler good",
			options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317829
public void testBug317829c() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.WARNING);
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"       someVariable = \"not null\";\n" +
			"		for (;;) {\n" +
			"			throw new IOException();\n" +
			"		}\n" +
			"	 } catch (IOException e) {\n" +
			"	 	if (someVariable == null) {\n" +
			"    		System.out.println(\"Compiler bad\");\n" +
			"	 	} else {\n" +
			"			System.out.print(\"Compiler good \");\n" +
			"	 	}\n" +
			"	 }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler bad\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler good\");\n" +
			"	 }\n" +
			"  }\n" +
			"}"},
			"Compiler good Compiler good",
			options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=317829
public void testBug317829d() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.WARNING);
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"       someVariable = \"not null\";\n" +
			"		for(;;) {\n" +
			"			someMethod();\n" +
			"		}\n" +
			"	 } catch (IOException e) {\n" +
			"	 	if (someVariable == null) {\n" +
			"    		System.out.println(\"Compiler bad\");\n" +
			"	 	} else {\n" +
			"			System.out.print(\"Compiler good \");\n" +
			"	 	}\n" +
			"	 }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler bad\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler good\");\n" +
			"	 }\n" +
			"  }\n" +
			"  public static void someMethod() throws IOException {\n" +
			"      throw new IOException();\n" +
			"  }\n" +
			"}"},
			"Compiler good Compiler good",
			options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317829
public void testBug317829e() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.WARNING);
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"       someVariable = \"not null\";\n" +
			"		do {\n" +
			"			throw new IOException();\n" +
			"		} while (true);\n" +
			"	 } catch (IOException e) {\n" +
			"	 	if (someVariable == null) {\n" +
			"    		System.out.println(\"Compiler bad\");\n" +
			"	 	} else {\n" +
			"			System.out.print(\"Compiler good \");\n" +
			"	 	}\n" +
			"	 }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler bad\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler good\");\n" +
			"	 }\n" +
			"  }\n" +
			"}"},
			"Compiler good Compiler good",
			options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317829
public void testBug317829f() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.WARNING);
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"       someVariable = \"not null\";\n" +
			"		do {\n" +
			"			someMethod();\n" +
			"		} while (true);\n" +
			"	 } catch (IOException e) {\n" +
			"	 	if (someVariable == null) {\n" +
			"    		System.out.println(\"Compiler bad\");\n" +
			"	 	} else {\n" +
			"			System.out.print(\"Compiler good \");\n" +
			"	 	}\n" +
			"	 }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler bad\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler good\");\n" +
			"	 }\n" +
			"  }\n" +
			"  public static void someMethod() throws IOException {\n" +
			"      throw new IOException();\n" +
			"  }\n" +
			"}"},
			"Compiler good Compiler good",
			options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=292478 -  Report potentially null across variable assignment
// LocalDeclaration
public void testBug292478() {
    this.runNegativeTest(
            new String[] {
                "X.java",
                "public class X {\n" +
                "  void foo(Object o) {\n" +
                "    if (o != null) {/* */}\n" +
                "    Object p = o;\n" +
                "    p.toString();\n" + // complain here
                "  }\n" +
                "}"},
            "----------\n" +
            "1. ERROR in X.java (at line 5)\n" +
            "	p.toString();\n" +
            "	^\n" +
            "Potential null pointer access: The variable p may be null at this location\n" +
            "----------\n",
            JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=292478 -  Report potentially null across variable assignment
// Assignment
public void testBug292478a() {
  this.runNegativeTest(
          new String[] {
              "X.java",
              "public class X {\n" +
              "  void foo(Object o) {\n" +
              "    Object p;" +
              "    if (o != null) {/* */}\n" +
              "    p = o;\n" +
              "    p.toString();\n" + // complain here
              "  }\n" +
              "}"},
          "----------\n" +
          "1. ERROR in X.java (at line 5)\n" +
          "	p.toString();\n" +
          "	^\n" +
          "Potential null pointer access: The variable p may be null at this location\n" +
          "----------\n",
          JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=292478 -  Report potentially null across variable assignment
// Assignment after definite null
public void testBug292478b() {
this.runNegativeTest(
        new String[] {
            "X.java",
            "public class X {\n" +
            "  void foo(Object o) {\n" +
            "    Object p = null;\n" +
            "    if (o != null) {/* */}\n" +
            "    p = o;\n" +
            "    p.toString();\n" + // complain here
            "  }\n" +
            "}"},
        "----------\n" +
        "1. ERROR in X.java (at line 6)\n" +
        "	p.toString();\n" +
        "	^\n" +
        "Potential null pointer access: The variable p may be null at this location\n" +
        "----------\n",
        JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=292478 -  Report potentially null across variable assignment
// Assignment after definite null - many locals
public void testBug292478c() {
this.runNegativeTest(
      new String[] {
          "X.java",
          "public class X {\n" +
          "  void foo(Object o) {\n" +
          "    int i00, i01, i02, i03, i04, i05, i06, i07, i08, i09;\n" +
          "    int i10, i11, i12, i13, i14, i15, i16, i17, i18, i19;\n" +
          "    int i20, i21, i22, i23, i24, i25, i26, i27, i28, i29;\n" +
          "    int i30, i31, i32, i33, i34, i35, i36, i37, i38, i39;\n" +
          "    int i40, i41, i42, i43, i44, i45, i46, i47, i48, i49;\n" +
          "    int i50, i51, i52, i53, i54, i55, i56, i57, i58, i59;\n" +
          "    int i60, i61, i62, i63, i64, i65, i66, i67, i68, i69;\n" +
          "    Object p = null;\n" +
          "    if (o != null) {/* */}\n" +
          "    p = o;\n" +
          "    p.toString();\n" + // complain here
          "  }\n" +
          "}"},
      "----------\n" +
      "1. ERROR in X.java (at line 13)\n" +
      "	p.toString();\n" +
      "	^\n" +
      "Potential null pointer access: The variable p may be null at this location\n" +
      "----------\n",
      JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=292478 -  Report potentially null across variable assignment
// Assignment affects initsOnFinally
public void testBug292478d() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" X bar() {\n" +
			"   return null;\n" +
			" }\n" +
			" Object foo() {\n" +
			"   X x = null;\n" +
			"   X y = new X();\n" +
			"   X u = null;\n" +
			"   try {\n" +
			"     u = bar();\n" +
			"     x = bar();\n" +
			"     if (x==null) { }\n" +
			"     y = x;\n" +				// this makes y potentially null
			"     if (x==null) { y=bar();} else { y=new X(); }\n" +
			"     return x;\n" +
			"   } finally {\n" +
			"     y.toString();\n" +		// must complain against potentially null, although normal exist of tryBlock says differently (unknown or non-null)
			"   }\n" +
			" }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 17)\n" +
		"	y.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable y may be null at this location\n" +
		"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=292478 -  Report potentially null across variable assignment
// test regression reported in comment 8
public void testBug292478e() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"	Object foo(int i, boolean b1, boolean b2) {\n" +
			"		Object o1 = null;\n" +
			"		done : while (true) { \n" +
			"			switch (i) {\n" +
			"				case 1 :\n" +
			"					Object o2 = null;\n" +
			"					if (b2)\n" +
			"						o2 = new Object();\n" +
			"					o1 = o2;\n" +
			"					break;\n" +
			"				case 2 :\n" +
			"					break done;\n" +
			"			}\n" +
			"		}		\n" +
			"		if (o1 != null)\n" +
			"			return o1;\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n"
		});
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=292478 -  Report potentially null across variable assignment
// variant where regression occurred inside the while-switch structure
public void testBug292478f() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"	Object foo(int i, boolean b1, boolean b2) {\n" +
			"		Object o1 = null;\n" +
			"		done : while (true) { \n" +
			"			switch (i) {\n" +
			"				case 1 :\n" +
			"					Object o2 = null;\n" +
			"					if (b2)\n" +
			"						o2 = new Object();\n" +
			"					o1 = o2;\n" +
			"					if (o1 != null)\n" +
			"						return o1;\n" +
			"					break;\n" +
			"				case 2 :\n" +
			"					break done;\n" +
			"			}\n" +
			"		}		\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n"
		});
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=292478 -  Report potentially null across variable assignment
// variant for transfering state potentially unknown
public void testBug292478g() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"	Object foo(int i, boolean b1, boolean b2, Object o2) {\n" +
			"		Object o1 = null;\n" +
			"		done : while (true) { \n" +
			"			switch (i) {\n" +
			"				case 1 :\n" +
			"					if (b2)\n" +
			"						o2 = bar();\n" +
			"					o1 = o2;\n" +
			"					if (o1 != null)\n" +
			"						return o1;\n" +
			"					break;\n" +
			"				case 2 :\n" +
			"					break done;\n" +
			"			}\n" +
			"		}		\n" +
			"		return null;\n" +
			"	}\n" +
			"   Object bar() { return null; }\n" +
			"}\n"
		});
}

// Bug 324762 -  Compiler thinks there is deadcode and removes it!
// regression caused by the fix for bug 133125
// ternary is non-null or null
public void testBug324762() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"	void zork(boolean b1) {\n" +
			"		Object satisfied = null;\n" +
			"		if (b1) {\n" +
			"			String[] s = new String[] { \"a\", \"b\" };\n" +
			"			for (int k = 0; k < s.length && satisfied == null; k++)\n" +
			"				satisfied = s.length > 1 ? new Object() : null;\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		});
}

// Bug 324762 -  Compiler thinks there is deadcode and removes it!
// regression caused by the fix for bug 133125
// ternary is unknown or null
public void testBug324762a() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"	void zork(boolean b1) {\n" +
			"		Object satisfied = null;\n" +
			"		if (b1) {\n" +
			"			String[] s = new String[] { \"a\", \"b\" };\n" +
			"			for (int k = 0; k < s.length && satisfied == null; k++)\n" +
			"				satisfied = s.length > 1 ? bar() : null;\n" +
			"		}\n" +
			"	}\n" +
			"	Object bar() { return null; }\n" +
			"}\n"
		});
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=325229
// instancof expression
public void testBug325229a() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
		this.runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" +
				"	void foo(Object a) {\n" +
				"		assert a instanceof Object;\n " +
				"		if (a!=null) {\n" +
				"			System.out.println(\"a is not null\");\n" +
				"		 } else{\n" +
				"			System.out.println(\"a is null\");\n" +
				"		 }\n" +
				"	}\n" +
				"	public static void main(String[] args){\n" +
				"		Test test = new Test();\n" +
				"		test.foo(null);\n" +
				"	}\n" +
				"}\n"},
			"a is null",
			null,
			true,
			new String[] {"-da"},
			compilerOptions,
			null);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=325229
// MessageSend in assert
public void testBug325229b() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
		this.runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" +
				"	boolean bar() {\n" +
				"		return false;\n" +
				"	}" +
				"	void foo(Test a) {\n" +
				"		assert a.bar();\n " +
				"		if (a!=null) {\n" +
				"			System.out.println(\"a is not null\");\n" +
				"		 } else{\n" +
				"			System.out.println(\"a is null\");\n" +
				"		 }\n" +
				"	}\n" +
				"	public static void main(String[] args){\n" +
				"		Test test = new Test();\n" +
				"		test.foo(null);\n" +
				"	}\n" +
				"}\n"},
			"a is null",
			null,
			true,
			new String[] {"-da"},
			compilerOptions,
			null);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=325229
// QualifiedNameReference in assert
public void testBug325229c() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
		this.runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" +
				"	boolean bar() {\n" +
				"		return false;\n" +
				"	}" +
				"	Test tfield;\n" +
				"	void foo(Test a) {\n" +
				"		assert a.tfield.bar();\n " +
				"		if (a!=null) {\n" +
				"			System.out.println(\"a is not null\");\n" +
				"		 } else{\n" +
				"			System.out.println(\"a is null\");\n" +
				"		 }\n" +
				"	}\n" +
				"	public static void main(String[] args){\n" +
				"		Test test = new Test();\n" +
				"		test.foo(null);\n" +
				"	}\n" +
				"}\n"},
			"a is null",
			null,
			true,
			new String[] {"-da"},
			compilerOptions,
			null);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=325229
// EqualExpression in assert, comparison against non null
public void testBug325229d() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
		this.runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" +
				"	void foo(Object a) {\n" +
				"		Object b = null;" +
				"		assert a == b;\n " +
				"		if (a!=null) {\n" +
				"			System.out.println(\"a is not null\");\n" +
				"		 } else{\n" +
				"			System.out.println(\"a is null\");\n" +
				"		 }\n" +
				"		assert a != b;\n " +
				"		if (a!=null) {\n" +
				"			System.out.println(\"a is not null\");\n" +
				"		 } else{\n" +
				"			System.out.println(\"a is null\");\n" +
				"		 }\n" +
				"	}\n" +
				"	public static void main(String[] args){\n" +
				"		Test test = new Test();\n" +
				"		test.foo(null);\n" +
				"	}\n" +
				"}\n"},
			"a is null\n" +
			"a is null",
			null,
			true,
			new String[] {"-da"},
			compilerOptions,
			null);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=325342
// Null warnings because of assert statements should be suppressed
// when CompilerOptions.OPTION_IncludeNullInfoFromAsserts is disabled.
public void testBug325342a() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_IncludeNullInfoFromAsserts, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"Test.java",
				"public class Test {\n" +
				"	void foo(Object a, Object b, Object c) {\n" +
				"		assert a == null;\n " +
				"		if (a!=null) {\n" +
				"			System.out.println(\"a is not null\");\n" +
				"		 } else{\n" +
				"			System.out.println(\"a is null\");\n" +
				"		 }\n" +
				"		a = null;\n" +
				"		if (a== null) {}\n" +
				"		assert b != null;\n " +
				"		if (b!=null) {\n" +
				"			System.out.println(\"b is not null\");\n" +
				"		 } else{\n" +
				"			System.out.println(\"b is null\");\n" +
				"		 }\n" +
				"		assert c == null;\n" +
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
			"1. ERROR in Test.java (at line 10)\n" +
			"	if (a== null) {}\n" +
			"	    ^\n" +
			"Redundant null check: The variable a can only be null at this location\n" +
			"----------\n",
			null,
			true,
			compilerOptions,
			"",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=325342
// Null warnings because of assert statements should not be suppressed
// when CompilerOptions.OPTION_IncludeNullInfoFromAsserts is enabled.
public void testBug325342b() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_IncludeNullInfoFromAsserts, CompilerOptions.ENABLED);
		this.runNegativeTest(
			new String[] {
				"Test.java",
				"public class Test {\n" +
				"	void foo(Object a, Object b, Object c) {\n" +
				"		assert a == null;\n " +
				"		if (a!=null) {\n" +
				"			System.out.println(\"a is not null\");\n" +
				"		 } else{\n" +
				"			System.out.println(\"a is null\");\n" +
				"		 }\n" +
				"		assert b != null;\n " +
				"		if (b!=null) {\n" +
				"			System.out.println(\"a is not null\");\n" +
				"		 } else{\n" +
				"			System.out.println(\"a is null\");\n" +
				"		 }\n" +
				"		assert c == null;\n" +
				"		if (c.equals(a)) {\n" +
				"			System.out.println(\"\");\n" +
				"		 } else{\n" +
				"			System.out.println(\"\");\n" +
				"		 }\n" +
				"	}\n" +
				"	public static void main(String[] args){\n" +
				"		Test test = new Test();\n" +
				"		test.foo(null,null,null);\n" +
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
			"3. ERROR in Test.java (at line 10)\n" +
			"	if (b!=null) {\n" +
			"	    ^\n" +
			"Redundant null check: The variable b cannot be null at this location\n" +
			"----------\n" +
			"4. WARNING in Test.java (at line 12)\n" +
			"	} else{\n" +
			"			System.out.println(\"a is null\");\n" +
			"		 }\n" +
			"	      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n" +
			"5. ERROR in Test.java (at line 16)\n" +
			"	if (c.equals(a)) {\n" +
			"	    ^\n" +
			"Null pointer access: The variable c can only be null at this location\n" +
			"----------\n",
			null, true, compilerOptions, "",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=325755
// null analysis -- conditional expression
public void testBug325755a() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static Object foo(String s1, String s2) {\n" +
			"		String local1 = s1;\n" +
			"		String local2 = s2;\n" +
			"		\n" +
			"		String local3 = null;\n" +
			"		if (local1 != null && local2 != null)\n" +
			"			local3 = \"\"; //$NON-NLS-1$\n" +
			"		else\n" +
			"			local3 = local1 != null ? local1 : local2;\n" +
			"\n" +
			"		if (local3 != null)\n" +
			"			return new Integer(local3.length());\n" +
			"		return null;\n" +
			"	}\n" +
			"	\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.print(foo(null, null));\n" +
			"		System.out.print(foo(\"p1\", null));\n" +
			"		System.out.print(foo(null, \"p2\"));\n" +
			"		System.out.print(foo(\"p1\", \"p2\"));\n" +
			"	}\n" +
			"}"},
		"null220");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=325755
// null analysis -- conditional expression, many locals
public void testBug325755b() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static Object foo(String s1, String s2) {\n" +
	          "    int i00, i01, i02, i03, i04, i05, i06, i07, i08, i09;\n" +
	          "    int i10, i11, i12, i13, i14, i15, i16, i17, i18, i19;\n" +
	          "    int i20, i21, i22, i23, i24, i25, i26, i27, i28, i29;\n" +
	          "    int i30, i31, i32, i33, i34, i35, i36, i37, i38, i39;\n" +
	          "    int i40, i41, i42, i43, i44, i45, i46, i47, i48, i49;\n" +
	          "    int i50, i51, i52, i53, i54, i55, i56, i57, i58, i59;\n" +
	          "    int i60, i61, i62, i63, i64, i65, i66, i67, i68, i69;\n" +

			"		String local1 = s1;\n" +
			"		String local2 = s2;\n" +
			"		\n" +
			"		String local3 = null;\n" +
			"		if (local1 != null && local2 != null)\n" +
			"			local3 = \"\"; //$NON-NLS-1$\n" +
			"		else\n" +
			"			local3 = local1 != null ? local1 : local2;\n" +
			"\n" +
			"		if (local3 != null)\n" +
			"			return new Integer(local3.length());\n" +
			"		return null;\n" +
			"	}\n" +
			"	\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.print(foo(null, null));\n" +
			"		System.out.print(foo(\"p1\", null));\n" +
			"		System.out.print(foo(null, \"p2\"));\n" +
			"		System.out.print(foo(\"p1\", \"p2\"));\n" +
			"	}\n" +
			"}"},
		"null220");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=332637
// Dead Code detection removing code that isn't dead
public void testBug332637() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	this.runConformTest(
		new String[] {
			"DeadCodeExample.java",
			"public class DeadCodeExample {\n" +
			"\n" +
			"	private class CanceledException extends Exception {\n" +
			"	}\n" +
			"\n" +
			"	private interface ProgressMonitor {\n" +
			"		boolean isCanceled();\n" +
			"	}\n" +
			"\n" +
			"	private void checkForCancellation(ProgressMonitor monitor)\n" +
			"			throws CanceledException {\n" +
			"		if (monitor.isCanceled()) {\n" +
			"			throw new CanceledException();\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	private int run() {\n" +
			"\n" +
			"		ProgressMonitor monitor = new ProgressMonitor() {\n" +
			"			private int i = 0;\n" +
			"\n" +
			"			public boolean isCanceled() {\n" +
			"				return (++i == 5);\n" +
			"			}\n" +
			"		};\n" +
			"\n" +
			"		Integer number = null;\n" +
			"\n" +
			"		try {\n" +
			"			checkForCancellation(monitor);\n" +
			"\n" +
			"			number = Integer.valueOf(0);\n" +
			"\n" +
			"			for (String s : new String[10]) {\n" +
			"				checkForCancellation(monitor);\n" +
			"				number++;\n" +
			"			}\n" +
			"			return 0;\n" +
			"		} catch (CanceledException e) {\n" +
			"			System.out.println(\"Canceled after \" + number\n" +
			"				+ \" times through the loop\");\n" +
			"			if (number != null) {\n" +
			"				System.out.println(\"number = \" + number);\n" +
			"			}\n" +
			"			return -1;\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(new DeadCodeExample().run());\n" +
			"	}\n" +
			"}\n"
		},
		"Canceled after 3 times through the loop\n" +
		"number = 3\n" +
		"-1");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=332637
// Dead Code detection removing code that isn't dead
// variant with a finally block
public void testBug332637b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	this.runConformTest(
		new String[] {
			"DeadCodeExample.java",
			"public class DeadCodeExample {\n" +
			"\n" +
			"	private class CanceledException extends Exception {\n" +
			"	}\n" +
			"\n" +
			"	private interface ProgressMonitor {\n" +
			"		boolean isCanceled();\n" +
			"	}\n" +
			"\n" +
			"	private void checkForCancellation(ProgressMonitor monitor)\n" +
			"			throws CanceledException {\n" +
			"		if (monitor.isCanceled()) {\n" +
			"			throw new CanceledException();\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	private int run() {\n" +
			"\n" +
			"		ProgressMonitor monitor = new ProgressMonitor() {\n" +
			"			private int i = 0;\n" +
			"\n" +
			"			public boolean isCanceled() {\n" +
			"				return (++i == 5);\n" +
			"			}\n" +
			"		};\n" +
			"\n" +
			"		Integer number = null;\n" +
			"\n" +
			"		try {\n" +
			"			checkForCancellation(monitor);\n" +
			"\n" +
			"			number = Integer.valueOf(0);\n" +
			"\n" +
			"			for (String s : new String[10]) {\n" +
			"				checkForCancellation(monitor);\n" +
			"				number++;\n" +
			"			}\n" +
			"			return 0;\n" +
			"		} catch (CanceledException e) {\n" +
			"			System.out.println(\"Canceled after \" + number\n" +
			"				+ \" times through the loop\");\n" +
			"			if (number != null) {\n" +
			"				System.out.println(\"number = \" + number);\n" +
			"			}\n" +
			"			return -1;\n" +
			"		} finally {\n" +
			"			System.out.println(\"Done\");\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(new DeadCodeExample().run());\n" +
			"	}\n" +
			"}\n"
		},
		"Canceled after 3 times through the loop\n" +
		"number = 3\n" +
		"Done\n" +
		"-1");
}

public void testBug406160a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	this.runConformTest(
		new String[] {
			"DeadCodeExample.java",
			"public class DeadCodeExample {\n" +
			"\n" +
			"	class CanceledException extends Exception {\n" +
			"	}\n" +
			"\n" +
			"	private interface ProgressMonitor {\n" +
			"		boolean isCanceled();\n" +
			"	}\n" +
			"\n" +
			"	private void checkForCancellation(ProgressMonitor monitor)\n" +
			"			throws CanceledException {\n" +
			"		if (monitor.isCanceled()) {\n" +
			"			throw new CanceledException();\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	private int run() {\n" +
			"\n" +
			"		ProgressMonitor monitor = new ProgressMonitor() {\n" +
			"			private int i = 0;\n" +
			"\n" +
			"			public boolean isCanceled() {\n" +
			"				return (++i == 5);\n" +
			"			}\n" +
			"		};\n" +
			"\n" +
			"		Integer number = null;\n" +
			"\n" +
			"		for (int j = 0; j < 1; ) {\n" +
			"\n" +
			"			try {\n" +
			"				checkForCancellation(monitor);\n" +
			"\n" +
			"				number = Integer.valueOf(0);\n" +
			"\n" +
			"				for (String s : new String[10]) {\n" +
			"					checkForCancellation(monitor);\n" +
			"					number++;\n" +
			"				}\n" +
			"				return 0;\n" +
			"			} catch (CanceledException e) {\n" +
			"				System.out.println(\"Canceled after \" + number\n" +
			"					+ \" times through the loop\");\n" +
			"				if (number != null) {\n" +
			"					System.out.println(\"number = \" + number);\n" +
			"				}\n" +
			"				return -1;\n" +
			"			}\n" +
			"		}\n" +
			"		return 13;\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(new DeadCodeExample().run());\n" +
			"	}\n" +
			"}\n"
		},
		"Canceled after 3 times through the loop\n" +
		"number = 3\n" +
		"-1");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=333089
// null analysis -- to make sure no AIOOBE or NPE is thrown while calling UnconditionalFlowInfo.markNullStatus(..)
public void testBug333089() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void foo(Object s1) {\n" +
	        "    int i00, i01, i02, i03, i04, i05, i06, i07, i08, i09;\n" +
	        "    int i10, i11, i12, i13, i14, i15, i16, i17, i18, i19;\n" +
	        "    int i20, i21, i22, i23, i24, i25, i26, i27, i28, i29;\n" +
	        "    int i30, i31, i32, i33, i34, i35, i36, i37, i38, i39;\n" +
	        "    int i40, i41, i42, i43, i44, i45, i46, i47, i48, i49;\n" +
	        "    int i50, i51, i52, i53, i54, i55, i56, i57, i58, i59;\n" +
	        "    int i60, i61, i62, i63, i64, i65, i66, i67, i68, i69;\n" +
			"	 Object local1;\n" +
			"	 if (s1 == null){}\n" +
			"	 try {" +
			"		local1 = s1;\n" +
			"	 } finally {\n" +
			"	 }\n" +
			"	}\n" +
			"}"},
		"");
}

//Bug 336428 - [compiler][null] bogus warning "redundant null check" in condition of do {} while() loop
//original issue
public void testBug336428() {
	this.runConformTest(
		new String[] {
	"DoWhileBug.java",
			"public class DoWhileBug {\n" +
			"	void test(boolean b1, Object o1) {\n" +
			"		Object o2 = new Object();\n" +
			"		do {\n" +
			"           if (b1)\n" +
			"				o1 = null;\n" +
			"		} while ((o2 = o1) != null);\n" +
			"	}\n" +
			"}"
		},
		"");
}
//Bug 336428 - [compiler][null] bogus warning "redundant null check" in condition of do {} while() loop
//hitting the same implementation branch from within the loop
//information from unknown o1 is not propagated into the loop, analysis currently believes o2 is def null.
public void _testBug336428a() {
	this.runConformTest(
		new String[] {
	"DoWhileBug.java",
			"public class DoWhileBug {\n" +
			"	void test(boolean b1, Object o1) {\n" +
			"		Object o2 = null;\n" +
			"		do {\n" +
			"           if (b1)\n" +
			"				o1 = null;\n" +
			"           if ((o2 = o1) != null)\n" +
			"               break;\n" +
			"		} while (true);\n" +
			"	}\n" +
			"}"
		},
		"");
}

//Bug 336428 - [compiler][null] bogus warning "redundant null check" in condition of do {} while() loop
//in this variant the analysis believes o2 is def unknown and doesn't even consider raising a warning.
public void _testBug336428b() {
	runNegativeNullTest(
		new String[] {
	"DoWhileBug.java",
			"public class DoWhileBug {\n" +
			"	void test(boolean b1) {\n" +
			"		Object o1 = null;\n" +
			"		Object o2 = null;\n" +
			"		do {\n" +
			"           if ((o2 = o1) == null) break;\n" +
			"		} while (true);\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in DoWhileBug.java (at line 6)\n" +
		"	if ((o2 = o1) == null) break;\n" +
		"	    ^^^^^^^^^\n" +
		"Redundant null check: The variable o2 can only be null at this location\n" +
		"----------\n");
}

//Bug 336428 - [compiler][null] bogus warning "redundant null check" in condition of do {} while() loop
//in this case considering o1 as unknown is correct
public void testBug336428c() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runConformTest(
			new String[] {
		"DoWhileBug.java",
				"public class DoWhileBug {\n" +
				"	void test(boolean b1, Object o1) {\n" +
				"		Object o2 = null;\n" +
				"		do {\n" +
				"           if ((o2 = o1) == null) break;\n" +
				"		} while (true);\n" +
				"	}\n" +
				"}"
			},
			"");
	}
}

//Bug 336428 - [compiler][null] bogus warning "redundant null check" in condition of do {} while() loop
//one more if-statement triggers the expected warnings
public void testBug336428d() {
	runNegativeNullTest(
		new String[] {
	"DoWhileBug.java",
			"public class DoWhileBug {\n" +
			"	void test(boolean b1) {\n" +
			"		Object o1 = null;\n" +
			"		Object o2 = null;\n" +
			"		do {\n" +
			"           if (b1)\n" +
			"				o1 = null;\n" +
			"           if ((o2 = o1) == null) break;\n" +
			"		} while (true);\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in DoWhileBug.java (at line 7)\n" +
		"	o1 = null;\n" +
		"	^^\n" +
		"Redundant assignment: The variable o1 can only be null at this location\n" +
/* In general it's safer *not* to assume that o1 is null on every iteration (see also testBug336428d2):
		"----------\n" +
		"2. ERROR in DoWhileBug.java (at line 8)\n" +
		"	if ((o2 = o1) == null) break;\n" +
		"	    ^^^^^^^^^\n" +
		"Redundant null check: The variable o2 can only be null at this location\n" +
 */
		"----------\n"
		);
}

// Bug 336428 - [compiler][null] bogus warning "redundant null check" in condition of do {} while() loop
// variant after Bug 454031 to demonstrate:
// - previously we would believe that o1 is always null in the assignment to o2 -> bogus warning re redundant null check
// - with improved analysis we don't claim to know the value of o1 in this assignment -> no warning
public void testBug336428d2() {
	this.runConformTest(
		new String[] {
	"DoWhileBug.java",
			"public class DoWhileBug {\n" +
			"	void test(boolean b1) {\n" +
			"		Object o1 = null;\n" +
			"		Object o2 = null;\n" +
			"		do {\n" +
			"           if (b1)\n" +
			"				o1 = null;\n" +
			"           if ((o2 = o1) == null) System.out.println(\"null\");\n" +
			"			o1 = new Object();\n" +
			"		} while (true);\n" +
			"	}\n" +
			"}"
		});
}

//Bug 336428 - [compiler][null] bogus warning "redundant null check" in condition of do {} while() loop
//same analysis, but assert instead of if suppresses the warning
public void testBug336428e() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeNullTest(
			new String[] {
		"DoWhileBug.java",
				"public class DoWhileBug {\n" +
				"	void test(boolean b1) {\n" +
				"		Object o1 = null;\n" +
				"		Object o2 = null;\n" +
				"		do {\n" +
				"           if (b1)\n" +
				"				o1 = null;\n" +
				"           assert (o2 = o1) != null : \"bug\";\n" +
				"		} while (true);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in DoWhileBug.java (at line 7)\n" +
			"	o1 = null;\n" +
			"	^^\n" +
			"Redundant assignment: The variable o1 can only be null at this location\n" +
/* In general it's safer *not* to assume that o1 is null on every iteration:
			"----------\n" +
			"2. ERROR in DoWhileBug.java (at line 8)\n" +
			"	assert (o2 = o1) != null : \"bug\";\n" +
			"	       ^^^^^^^^^\n" +
			"Null comparison always yields false: The variable o2 can only be null at this location\n" +
 */
			"----------\n");
	}
}

// Bug 336428 - [compiler][null] bogus warning "redundant null check" in condition of do {} while() loop
// same analysis, but assert instead of if suppresses the warning
// condition inside assert is redundant null check and hence should not be warned against
public void testBug336428f() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeNullTest(
			new String[] {
		"DoWhileBug.java",
				"public class DoWhileBug {\n" +
				"	void test(boolean b1) {\n" +
				"		Object o1 = null;\n" +
				"		Object o2 = null;\n" +
				"		do {\n" +
				"           if (b1)\n" +
				"				o1 = null;\n" +
				"           assert (o2 = o1) == null : \"bug\";\n" +
				"		} while (true);\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in DoWhileBug.java (at line 7)\n" +
			"	o1 = null;\n" +
			"	^^\n" +
			"Redundant assignment: The variable o1 can only be null at this location\n" +
			"----------\n");
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=332838
// Null info of assert statements should not affect flow info
// when CompilerOptions.OPTION_IncludeNullInfoFromAsserts is disabled.
public void testBug332838() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_IncludeNullInfoFromAsserts, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"Info.java",
				"public class Info {\n" +
				"	public void test(Info[] infos) {\n" +
				"		for (final Info info : infos) {\n " +
				"			if (info != null) {\n" +
				"				assert info.checkSomething();\n" +
				"		 		info.doSomething();\n" +	// no warning
				"			}\n" +
				"		 }\n" +
				"		for (final Info info : infos) {\n " +
				"			if (info == null) {\n" +
				"				assert info.checkSomething();\n" +
				"		 		info.doSomething();\n" +	// warn NPE, not pot. NPE
				"			}\n" +
				"		 }\n" +
				"	}\n" +
				"	void doSomething()  {}\n" +
				"	boolean checkSomething() {return true;}\n" +
				"}\n"},
			"----------\n" +
			"1. ERROR in Info.java (at line 11)\n" +
			"	assert info.checkSomething();\n" +
			"	       ^^^^\n" +
			"Null pointer access: The variable info can only be null at this location\n" +
			"----------\n" +
			"2. ERROR in Info.java (at line 12)\n" +
			"	info.doSomething();\n" +
			"	^^^^\n" +
			"Null pointer access: The variable info can only be null at this location\n" +
			"----------\n",
			null,
			true,
			compilerOptions,
			"",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=336544
public void testBug336544() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Integer i1 = getInt();\n" +
			"		Integer i2 = i1 == null ? null : i1;\n" +
			"		if (i2 != null) {\n" +
			"			System.out.println(\"SUCCESS\");\n" +
			"			return;\n" +
			"		}\n" +
			"		System.out.println(\"FAILURE\");\n" +
			"	}\n" +
			"	private static Integer getInt() {\n" +
			"		return new Integer(0);\n" +
			"	}\n" +
			"}"
		},
		"SUCCESS");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=336544
public void testBug336544_2() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Integer i1 = null;\n" +
			"		Integer i2 = (i1 = getInt()) == null ? null : i1;\n" +
			"		if (i2 != null) {\n" +
			"			System.out.println(\"SUCCESS\");\n" +
			"			return;\n" +
			"		}\n" +
			"		System.out.println(\"FAILURE\");\n" +
			"	}\n" +
			"	private static Integer getInt() {\n" +
			"		return new Integer(0);\n" +
			"	}\n" +
			"}"
		},
		"SUCCESS");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=336544
public void testBug336544_3() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Integer i1 = null;\n" +
			"		Integer i2;\n" +
			"		i2 = (i1 = getInt()) == null ? null : i1;\n" +
			"		if (i2 != null) {\n" +
			"			System.out.println(\"SUCCESS\");\n" +
			"			return;\n" +
			"		}\n" +
			"		System.out.println(\"FAILURE\");\n" +
			"	}\n" +
			"	private static Integer getInt() {\n" +
			"		return new Integer(0);\n" +
			"	}\n" +
			"}"
		},
		"SUCCESS");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=313870
public void testBug313870() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = \"\";\n" +
			"		for (int i = 0; i < 2; i++) {\n" +
            "			if (i != 0) { \n" +
            "    			s = test();\n" +
            "			}\n" +
            "			if (s == null) {\n" +
            "    			System.out.println(\"null\");\n" +
            "			}\n" +
            "		}\n" +
			"	}\n" +
			"	public static String test() {\n" +
            "		return null;\n" +
			"	}\n" +
			"}"
		},
		"null");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=313870
public void testBug313870b() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.BufferedReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"	public void main(BufferedReader bufReader) throws IOException {\n" +
			"		String line = \"\";\n" +
			"		boolean doRead = false;\n" +
			"		while (true) {\n" +
            "			if (doRead) { \n" +
            "    		   line = bufReader.readLine();\n" +
            "			}\n" +
            "			if (line == null) {\n" +
            "    			return;\n" +
            "			}\n" +
            "			doRead = true;\n" +
            "		}\n" +
			"	}\n" +
			"}"
		},
		"");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=313870
public void testBug313870c() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		boolean sometimes = (System.currentTimeMillis() & 1L) != 0L;\n" +
			"		File file = new File(\"myfile\");\n" +
			"		for (int i = 0; i < 2; i++) {\n" +
            "			if (sometimes) { \n" +
            "    		 	file = getNewFile();\n" +
            "			}\n" +
            "			if (file == null) { \n" +
            "    			System.out.println(\"\");\n" +
            "			}\n" +
            "		}\n" +
			"	}\n" +
			"	private static File getNewFile() {\n" +
			"		return null;\n" +
			"	}\n" +
			"}"
		},
		"");
}
// https://bugs.eclipse.org/338303 - Warning about Redundant assignment conflicts with definite assignment
public void testBug338303() {
	this.runConformTest(
		new String[] {
			"Bug338303.java",
			"import java.io.File;\n" +
			"import java.io.IOException;\n" +
			"\n" +
			"public class Bug338303 {\n" +
			"   Object test(Object in, final File f) {\n" +
			"        Object local;\n" +
			"        try {\n" +
			"            local = in;\n" +
			"            if (local == null)\n" +
			"                local = loadEntry(f, false);\n" +
			"        } catch (final IOException e) {\n" +
			"            e.printStackTrace();\n" +
			"            local = null;\n" +
			"        }\n" +
			"        return local;\n" +
			"    }\n" +
			"\n" +
			"    private Object loadEntry(File f, boolean b)  throws IOException {\n" +
			"        throw new IOException();\n" +
			"    }\n" +
			"}\n"
		},
		"");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=338234
public void testBug338234() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"   static int foo() {\n" +
			"        Object o = null;\n" +
			"		 int i = 0;\n" +
			"        label: {\n" +
			"            if (o == null)\n" +
			"                break label;\n" +
			"			 i++;" +
			"        }\n" +
			"         if (i != 0) {\n" +
			"            System.out.println(i);\n" +
			"        }\n" +
			"        return 0;\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	if (o == null)\n" +
		"	    ^\n" +
		"Redundant null check: The variable o can only be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 8)\n" +
		"	i++;        }\n" +
		"	^^^\n" +
		"Dead code\n" +
		"----------\n");
}
// Bug 324178 - [null] ConditionalExpression.nullStatus(..) doesn't take into account the analysis of condition itself
public void testBug324178() {
	this.runConformTest(
		new String[] {
			"Bug324178.java",
			"public class Bug324178 {\n" +
			"    boolean b;\n" +
			"    void foo(Object u) {\n" +
			"    if (u == null) {}\n" +
			"        Object o = (u == null) ? new Object() : u;\n" +
			"        o.toString();   // Incorrect potential NPE\n" +
			"    }\n" +
			"}\n"
		},
		"");
}

// Bug 324178 - [null] ConditionalExpression.nullStatus(..) doesn't take into account the analysis of condition itself
public void testBug324178a() {
	this.runConformTest(
		new String[] {
			"Bug324178.java",
			"public class Bug324178 {\n" +
			"    boolean b;\n" +
			"    void foo(Boolean u) {\n" +
			"    if (u == null) {}\n" +
			"        Boolean o;\n" +
			"        o = (u == null) ? Boolean.TRUE : u;\n" +
			"        o.toString();   // Incorrect potential NPE\n" +
			"    }\n" +
			"}\n"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=326950
public void testBug326950a() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = null;\n" +
			"		if (s == null) {\n" +
			"			System.out.println(\"SUCCESS\");\n" +
			"		} else {\n" +
			"			System.out.println(\"Dead code, but don't optimize me out\");\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"SUCCESS",
		null,
		true,
		null,
		options,
		null);
	String expectedOutput =
		"  public static void main(java.lang.String[] args);\n" +
		"     0  aconst_null\n" +
		"     1  astore_1 [s]\n" +
		"     2  aload_1 [s]\n" +
		"     3  ifnonnull 17\n" +
		"     6  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"     9  ldc <String \"SUCCESS\"> [22]\n" +
		"    11  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
		"    14  goto 25\n" +
		"    17  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
		"    20  ldc <String \"Dead code, but don\'t optimize me out\"> [30]\n" +
		"    22  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" +
		"    25  return\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=326950
// Code marked dead due to if(false), etc. can be optimized out
public void testBug326950b() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		int i = 0;\n" +
			"		if (false) {\n" +
			"			System.out.println(\"Deadcode and you can optimize me out\");\n" +
			"		}\n" +
			"		if (true) {\n" +
			"			i++;\n" +
			"		} else {\n" +
			"			System.out.println(\"Deadcode and you can optimize me out\");\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"",
		null,
		true,
		null,
		options,
		null);
	String expectedOutput =
		"  public static void main(java.lang.String[] args);\n" +
		"    0  iconst_0\n" +
		"    1  istore_1 [i]\n" +
		"    2  iinc 1 1 [i]\n" +
		"    5  return\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=326950
// Free return should be generated for a method even if it ends with dead code
public void testBug326950c() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void foo(String[] args) {\n" +
			"		String s = \"\";\n" +
			"		int i = 0;\n" +
			"		if (s != null) {\n" +
			"			return;\n" +
			"		}\n" +
			"		i++;\n" +
			"	}\n" +
			"}",
		},
		"",
		null,
		true,
		null,
		options,
		null);
	String expectedOutput =
		"  public void foo(java.lang.String[] args);\n" +
		"     0  ldc <String \"\"> [16]\n" +
		"     2  astore_2 [s]\n" +
		"     3  iconst_0\n" +
		"     4  istore_3 [i]\n" +
		"     5  aload_2 [s]\n" +
		"     6  ifnull 10\n" +
		"     9  return\n" +
		"    10  iinc 3 1 [i]\n" +
		"    13  return\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=326950
// Free return should be generated for a constructor even if it ends with dead code
public void testBug326950d() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	X() {\n" +
			"		String s = \"\";\n" +
			"		int i = 0;\n" +
			"		if (s != null) {\n" +
			"			return;\n" +
			"		}\n" +
			"		i++;\n" +
			"	}\n" +
			"}",
		},
		"",
		null,
		true,
		null,
		options,
		null);
	String expectedOutput =
		"  X();\n" +
		"     0  aload_0 [this]\n" +
		"     1  invokespecial java.lang.Object() [8]\n" +
		"     4  ldc <String \"\"> [10]\n" +
		"     6  astore_1 [s]\n" +
		"     7  iconst_0\n" +
		"     8  istore_2 [i]\n" +
		"     9  aload_1 [s]\n" +
		"    10  ifnull 14\n" +
		"    13  return\n" +
		"    14  iinc 2 1 [i]\n" +
		"    17  return\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=339250
// Check code gen
public void testBug339250() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = null;\n" +
			"		s += \"correctly\";\n" +
			"		if (s != null) {\n" + 	// s cannot be null
			"			System.out.println(\"It works \" + s);\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"It works nullcorrectly",
		null,
		true,
		null,
		options,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=339250
// Check that the redundant null check warning is correctly produced
public void testBug339250a() throws Exception {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = null;\n" +
			"		s += \"correctly\";\n" +
			"		if (s != null) {\n" + 	// s cannot be null
			"			System.out.println(\"It works \" + s);\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	if (s != null) {\n" +
		"	    ^\n" +
		"Redundant null check: The variable s cannot be null at this location\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=339250
// Check that the redundant null check warning is correctly produced
public void testBug339250b() throws Exception {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = null;\n" +
			"		s += null;\n" +
			"		if (s != null) {\n" + 	// s is definitely not null
			"			System.out.println(\"It works \" + s);\n" +
			"	    }\n" +
			"		s = null;\n" +
			"		if (s != null) {\n" + 	// s is definitely null
			"			System.out.println(\"Fails \" + s);\n" +
			"	    } else {\n" +
			"			System.out.println(\"Works second time too \" + s);\n" +
			"       }\n" +
			"	}\n" +
			"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	if (s != null) {\n" +
		"	    ^\n" +
		"Redundant null check: The variable s cannot be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	if (s != null) {\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable s can only be null at this location\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 9)\n" +
		"	if (s != null) {\n" +
		"			System.out.println(\"Fails \" + s);\n" +
		"	    } else {\n" +
		"	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=342300
public void testBug342300() throws Exception {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void initPattern(String p, Character escapeChar) {\n" +
				"		int len = p.length();\n" +
				"		for (int i = 0; i < len; i++) {\n" +
				"			char c = p.charAt(i);\n" +
				"			if (escapeChar != null && escapeChar == c) {\n" +	// quiet
				"				c = p.charAt(++i);\n" +
				"			}\n" +
				"	    }\n" +
				"	}\n" +
				"}",
			},
			"");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=342300
// To make sure only the redundant null check is given and not a potential NPE
public void testBug342300b() throws Exception {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeNullTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void initPattern(String p, Character escapeChar) {\n" +
				"		int len = p.length();\n" +
				"		for (int i = 0; i < len; i++) {\n" +
				"			char c = p.charAt(i);\n" +
				"			if (escapeChar != null && escapeChar != null) {\n" +	// look here
				"				c = p.charAt(++i);\n" +
				"			}\n" +
				"	    }\n" +
				"	}\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	if (escapeChar != null && escapeChar != null) {\n" +
			"	                          ^^^^^^^^^^\n" +
			"Redundant null check: The variable escapeChar cannot be null at this location\n" +
			"----------\n");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348379
public void testBug348379a() throws Exception {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
		runNegativeNullTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public void foo() {\n" +
				"		String s = null;\n" +
				"		switch(s) {\n" +
				"		case \"abcd\":\n" +
				"			System.out.println(\"abcd\");\n" +
				"			break;\n" +
				"		default:\n" +
				"			System.out.println(\"oops\");\n" +
				"			break;\n" +
				"	    }\n" +
				"	}\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	switch(s) {\n" +
			"	       ^\n" +
			"Null pointer access: The variable s can only be null at this location\n" +
			"----------\n");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348379
public void testBug348379b() throws Exception {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		String s = \"abcd\";\n" +
				"		switch(s) {\n" +	// no warning since s is not null
				"		case \"abcd\":\n" +
				"			System.out.println(\"abcd\");\n" +
				"			break;\n" +
				"		default:\n" +
				"			System.out.println(\"oops\");\n" +
				"			break;\n" +
				"	    }\n" +
				"	}\n" +
				"}",
			},
			"abcd");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348379
public void testBug348379c() throws Exception {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
		runNegativeNullTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public void foo(String s) {\n" +
				"		if (s == null) {}\n" +		// tainting s
				"		switch(s) {\n" +
				"		case \"abcd\":\n" +
				"			System.out.println(\"abcd\");\n" +
				"			break;\n" +
				"		default:\n" +
				"			System.out.println(\"oops\");\n" +
				"			break;\n" +
				"	    }\n" +
				"	}\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	switch(s) {\n" +
			"	       ^\n" +
			"Potential null pointer access: The variable s may be null at this location\n" +
			"----------\n");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348379
public void testBug348379d() throws Exception {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
		runNegativeNullTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public void foo(String s) {\n" +
				"		if (s != null) {}\n" +		// tainting s
				"		switch(s) {\n" +
				"		case \"abcd\":\n" +
				"			System.out.println(\"abcd\");\n" +
				"			break;\n" +
				"		default:\n" +
				"			System.out.println(\"oops\");\n" +
				"			break;\n" +
				"	    }\n" +
				"	}\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	switch(s) {\n" +
			"	       ^\n" +
			"Potential null pointer access: The variable s may be null at this location\n" +
			"----------\n");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348379
public void testBug348379e() throws Exception {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public void foo(String s) {\n" +
				"		if (s == null) {}\n" +		// tainting s
				"		else\n" +
				"		switch(s) {\n" +   // no warning because we're inside else
				"		case \"abcd\":\n" +
				"			System.out.println(\"abcd\");\n" +
				"			break;\n" +
				"		default:\n" +
				"			System.out.println(\"oops\");\n" +
				"			break;\n" +
				"	    }\n" +
				"	}\n" +
				"}",
			},
			"");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348379
public void testBug348379f() throws Exception {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
		runNegativeNullTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public void foo(String s) {\n" +
				"		s = null;\n" +
				"		switch(s) {\n" +
				"		case \"abcd\":\n" +
				"			System.out.println(\"abcd\");\n" +
				"			break;\n" +
				"		default:\n" +
				"			switch(s) {\n" +	// do not warn again
				"				case \"abcd\":\n" +
				"					System.out.println(\"abcd\");\n" +
				"					break;\n" +
				"				default:\n" +
				"					break;\n" +
				"			}\n" +
				"	    }\n" +
				"	}\n" +
				"}",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	switch(s) {\n" +
			"	       ^\n" +
			"Null pointer access: The variable s can only be null at this location\n" +
			"----------\n");
	}
}
// Bug 354554 - [null] conditional with redundant condition yields weak error message
public void testBug354554() {
	runNegativeNullTest(
		new String[] {
			"Bug354554.java",
			"public class Bug354554{\n" +
			"    void foo() {\n" +
			"        Object u = new Object();\n" +
			"        Object r = (u == null ? u : null);\n" + // condition is always false - should not spoil subsequent null-analysis
			"        System.out.println(r.toString());\n" +  // should strongly complain: r is definitely null
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Bug354554.java (at line 4)\n" +
		"	Object r = (u == null ? u : null);\n" +
		"	            ^\n" +
		"Null comparison always yields false: The variable u cannot be null at this location\n" +
		"----------\n" +
		"2. ERROR in Bug354554.java (at line 5)\n" +
		"	System.out.println(r.toString());\n" +
		"	                   ^\n" +
		"Null pointer access: The variable r can only be null at this location\n" +
		"----------\n");
}
//Bug 354554 - [null] conditional with redundant condition yields weak error message
public void testBug354554b() {
	runNegativeNullTest(
		new String[] {
			"Bug354554.java",
			"public class Bug354554{\n" +
			"    void foo() {\n" +
			"        Object u = new Object();\n" +
			"        Object r = (u != null ? u : null);\n" + // condition is always true - should not spoil subsequent null-analysis
			"        System.out.println(r.toString());\n" +  // don't complain: r is definitely non-null
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Bug354554.java (at line 4)\n" +
		"	Object r = (u != null ? u : null);\n" +
		"	            ^\n" +
		"Redundant null check: The variable u cannot be null at this location\n" +
		"----------\n");
}
// Bug 358827 - [1.7] exception analysis for t-w-r spoils null analysis
public void test358827() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
		runNegativeNullTest(
				new String[] {
					"Bug358827.java",
					"import java.io.FileReader;\n" +
					"public class Bug358827 {\n" +
					"	Object foo2() throws Exception {\n" +
					"		String o = null;\n" +
					"		try (FileReader rf = new FileReader(\"file\")){\n" +
					"			o = o.toUpperCase();\n" +
					"		} finally {\n" +
					"			o = \"OK\";\n" +
					"		}\n" +
					"		return o;\n" +
					"	}\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in Bug358827.java (at line 6)\n" +
				"	o = o.toUpperCase();\n" +
				"	    ^\n" +
				"Null pointer access: The variable o can only be null at this location\n" +
				"----------\n");
	}
}
// Bug 367879 - Incorrect "Potential null pointer access" warning on statement after try-with-resources within try-finally
public void test367879() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
		this.runConformTest(
				new String[] {
					"Bug367879.java",
					"import java.io.IOException;\n" +
					"import java.io.InputStream;\n" +
					"import java.net.HttpURLConnection;\n" +
					"import java.net.URL;\n" +
					"public class Bug367879 {\n" +
					"    public void test() throws IOException {\n" +
					"    HttpURLConnection http = null;\n" +
					"        try {\n" +
					"            http = (HttpURLConnection) new URL(\"http://example.com/\").openConnection();\n" +
					"            try (InputStream in = http.getInputStream()) { /* get input */ }\n" +
					"            http.getURL();\n" + // shouldn't *not* flag as Potential null pointer access
					"        } finally {\n" +
					"            if (http != null) { http.disconnect(); }\n" +
					"        }\n" +
					"    }\n" +
					"}\n"
				},
				"");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=256796
public void testBug256796() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportUnnecessaryElse, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.WARNING);
	compilerOptions.put(CompilerOptions.OPTION_ReportDeadCodeInTrivialIfStatement, CompilerOptions.DISABLED);
	this.runNegativeTest(
			new String[] {
				"Bug.java",
				"public class Bug {\n" +
				"	private static final boolean TRUE = true;\n" +
				"   private static final boolean FALSE = false;\n" +
				"	void foo() throws Exception {\n" +
				"		if (TRUE) return;\n" +
				"		else System.out.println(\"\");\n" +
				"		System.out.println(\"\");\n" + 		// not dead code
				"		if (TRUE) throw new Exception();\n" +
				"		else System.out.println(\"\");\n" +
				"		System.out.println(\"\");\n" + 		// not dead code
				"		if (TRUE) return;\n" +
				"		System.out.println(\"\");\n" + 		// not dead code
				"		if (FALSE) System.out.println(\"\");\n" +
				"		else return;\n" +
				"		System.out.println(\"\");\n" + 		// not dead code
				"		if (FALSE) return;\n" +
				"		System.out.println(\"\");\n" + 		// not dead code
				"		if (false) return;\n" +				// dead code
				"		System.out.println(\"\");\n" +
				"		if (true) return;\n" +
				"		System.out.println(\"\");\n" + 		// dead code
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. WARNING in Bug.java (at line 18)\n" +
			"	if (false) return;\n" +
			"	           ^^^^^^^\n" +
			"Dead code\n" +
			"----------\n" +
			"2. WARNING in Bug.java (at line 21)\n" +
			"	System.out.println(\"\");\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n",
			null,
			true,
			compilerOptions,
			null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=256796
public void testBug256796a() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportUnnecessaryElse, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.WARNING);
	compilerOptions.put(CompilerOptions.OPTION_ReportDeadCodeInTrivialIfStatement, CompilerOptions.ENABLED);
	this.runNegativeTest(
			new String[] {
				"Bug.java",
				"public class Bug {\n" +
				"	private static final boolean TRUE = true;\n" +
				"   private static final boolean FALSE = false;\n" +
				"	void foo() throws Exception {\n" +
				"		if (TRUE) return;\n" +
				"		else System.out.println(\"\");\n" +
				"		System.out.println(\"\");\n" + 		// dead code
				"	}\n" +
				"   void foo2() {\n" +
				"		if (TRUE) return;\n" +
				"		System.out.println(\"\");\n" + 		// dead code
				"	}\n" +
				"	void foo3() throws Exception {\n" +
				"		if (TRUE) throw new Exception();\n" +
				"		else System.out.println(\"\");\n" + // dead code
				"		System.out.println(\"\");\n" + 		// dead code
				"	}\n" +
				"	void foo4() throws Exception {\n" +
				"		if (FALSE) System.out.println(\"\");\n" +
				"		else return;\n" +
				"		System.out.println(\"\");\n" + 		// dead code
				"	}\n" +
				"	void foo5() throws Exception {\n" +
				"		if (FALSE) return;\n" +				// dead code
				"		System.out.println(\"\");\n" +
				"	}\n" +
				"	void foo6() throws Exception {\n" +
				"		if (false) return;\n" +				// dead code
				"		System.out.println(\"\");\n" +
				"		if (true) return;\n" +
				"		System.out.println(\"\");\n" + 		// dead code
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. WARNING in Bug.java (at line 6)\n" +
			"	else System.out.println(\"\");\n" +
			"	     ^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n" +
			"2. WARNING in Bug.java (at line 7)\n" +
			"	System.out.println(\"\");\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n" +
			"3. WARNING in Bug.java (at line 11)\n" +
			"	System.out.println(\"\");\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n" +
			"4. WARNING in Bug.java (at line 15)\n" +
			"	else System.out.println(\"\");\n" +
			"	     ^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n" +
			"5. WARNING in Bug.java (at line 16)\n" +
			"	System.out.println(\"\");\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n" +
			"6. WARNING in Bug.java (at line 19)\n" +
			"	if (FALSE) System.out.println(\"\");\n" +
			"	           ^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n" +
			"7. WARNING in Bug.java (at line 21)\n" +
			"	System.out.println(\"\");\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n" +
			"8. WARNING in Bug.java (at line 24)\n" +
			"	if (FALSE) return;\n" +
			"	           ^^^^^^^\n" +
			"Dead code\n" +
			"----------\n" +
			"9. WARNING in Bug.java (at line 28)\n" +
			"	if (false) return;\n" +
			"	           ^^^^^^^\n" +
			"Dead code\n" +
			"----------\n" +
			"10. WARNING in Bug.java (at line 31)\n" +
			"	System.out.println(\"\");\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n",
			null,
			true,
			compilerOptions,
			"",
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}
// Bug 360328 - [compiler][null] detect null problems in nested code (local class inside a loop)
public void testBug360328() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
	runNegativeTest(
		true, /* flushOutputDir */
		new String[] {
			"X.java",
			"public class X {\n" +
			"    void print4() {\n" +
			"        final String s1 = \"\";\n" +
			"        for (int i=0; i<4; i++)\n" +
			"            new Runnable() {\n" +
			"                public void run() {\n" +
			"                     if (s1 != null)\n" +
			"                         s1.toString();\n" +
			"                }\n" +
			"            }.run();\n" +
			"    }\n" +
			"    void print16(boolean b) {\n" +
			"        final String s3 = b ? null : \"\";\n" +
			"        for (int i=0; i<16; i++)\n" +
			"            new Runnable() {\n" +
			"                public void run() {\n" +
			"                     s3.toString();\n" +
			"                }\n" +
			"            }.run();\n" +
			"    }\n" +
			"    void print23() {\n" +
			"        final String s23 = null;\n" +
			"        for (int i=0; i<23; i++)\n" +
			"            new Runnable() {\n" +
			"                public void run() {\n" +
			"                     s23.toString();\n" +
			"                }\n" +
			"            }.run();\n" +
			"    }\n" +
			"}\n",

		},
		null, /* classLibs */
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	if (s1 != null)\n" +
		"	    ^^\n" +
		"Redundant null check: The variable s1 cannot be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 17)\n" +
		"	s3.toString();\n" +
		"	^^\n" +
		"Potential null pointer access: The variable s3 may be null at this location\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 26)\n" +
		"	s23.toString();\n" +
		"	^^^\n" +
		"Null pointer access: The variable s23 can only be null at this location\n" +
		"----------\n",
		"",/* expected output */
		"",/* expected error */
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// Bug 360328 - [compiler][null] detect null problems in nested code (local class inside a loop)
// constructors
public void testBug360328b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
	runNegativeTest(
		true, /* flushOutputDir */
		new String[] {
			"X.java",
			"public class X {\n" +
			"    void print4() {\n" +
			"        final String s1 = \"\";\n" +
			"        for (int i=0; i<4; i++) {\n" +
			"            class R {\n" +
			"                public R() {\n" +
			"                     if (s1 != null)\n" +
			"                         s1.toString();\n" +
			"                }\n" +
			"            };\n" +
			"            new R();\n" +
			"        }\n" +
			"    }\n" +
			"    void print16(boolean b) {\n" +
			"        final String s3 = b ? null : \"\";\n" +
			"        int i=0; while (i++<16) {\n" +
			"            class R {\n" +
			"                public R() {\n" +
			"                     s3.toString();\n" +
			"                }\n" +
			"            };\n" +
			"            new R();\n" +
			"        };\n" +
			"    }\n" +
			"    void print23() {\n" +
			"        final String s23 = null;\n" +
			"        for (int i=0; i<23; i++) {\n" +
			"            class R {\n" +
			"                public R() {\n" +
			"                     s23.toString();\n" +
			"                }\n" +
			"            };\n" +
			"            new R();\n" +
			"        };\n" +
			"    }\n" +
			"}\n",

		},
		null, /* classLibs */
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	if (s1 != null)\n" +
		"	    ^^\n" +
		"Redundant null check: The variable s1 cannot be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 19)\n" +
		"	s3.toString();\n" +
		"	^^\n" +
		"Potential null pointer access: The variable s3 may be null at this location\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 30)\n" +
		"	s23.toString();\n" +
		"	^^^\n" +
		"Null pointer access: The variable s23 can only be null at this location\n" +
		"----------\n",
		"",/* expected output */
		"",/* expected error */
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// Bug 360328 - [compiler][null] detect null problems in nested code (local class inside a loop)
// initializers
public void testBug360328c() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runNegativeTest(
		true, /* flushOutputDir */
		new String[] {
			"X.java",
			"public class X {\n" +
			"    void print4() {\n" +
			"        final String s1 = \"\";\n" +
			"        for (int i=0; i<4; i++) {\n" +
			"            class R {\n" +
			"                String s1R;\n" +
			"                {\n" +
			"                    if (s1 != null)\n" +
			"                         s1R = s1;\n" +
			"                }\n" +
			"            };\n" +
			"            new R();\n" +
			"        }\n" +
			"    }\n" +
			"    void print16(boolean b) {\n" +
			"        final String s3 = b ? null : \"\";\n" +
			"        for (int i=0; i<16; i++) {\n" +
			"            class R {\n" +
			"                String s3R = s3.toString();\n" +
			"            };\n" +
			"            new R();\n" +
			"        };\n" +
			"    }\n" +
			"    void print23() {\n" +
			"        final String s23 = null;\n" +
			"        for (int i=0; i<23; i++) {\n" +
			"            class R {\n" +
			"                String s23R;\n" +
			"                {\n" +
			"                     s23R = s23.toString();\n" +
			"                }\n" +
			"            };\n" +
			"            new R();\n" +
			"        };\n" +
			"    }\n" +
			"}\n",

		},
		null, /* classLibs */
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	if (s1 != null)\n" +
		"	    ^^\n" +
		"Redundant null check: The variable s1 cannot be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 19)\n" +
		"	String s3R = s3.toString();\n" +
		"	             ^^\n" +
		"Potential null pointer access: The variable s3 may be null at this location\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 30)\n" +
		"	s23R = s23.toString();\n" +
		"	       ^^^\n" +
		"Null pointer access: The variable s23 can only be null at this location\n" +
		"----------\n",
		"",/* expected output */
		"",/* expected error */
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// Bug 360328 - [compiler][null] detect null problems in nested code (local class inside a loop)
// try-finally instead of loop
public void testBug360328d() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
	runNegativeTest(
		true, /* flushOutputDir */
		new String[] {
			"X.java",
			"public class X {\n" +
			"    void print4() {\n" +
			"        final String s1 = \"\";\n" +
			"        try { } finally {\n" +
			"            new Runnable() {\n" +
			"                public void run() {\n" +
			"                     if (s1 != null)\n" +
			"                         s1.toString();\n" +
			"                }\n" +
			"            }.run();\n" +
			"        }\n" +
			"    }\n" +
			"    void print16(boolean b) {\n" +
			"        final String s3 = b ? null : \"\";\n" +
			"        try { } finally {\n" +
			"            new Runnable() {\n" +
			"                public void run() {\n" +
			"                     s3.toString();\n" +
			"                }\n" +
			"            }.run();\n" +
			"        }\n" +
			"    }\n" +
			"    void print23() {\n" +
			"        final String s23 = null;\n" +
			"        try { } finally {\n" +
			"            new Runnable() {\n" +
			"                public void run() {\n" +
			"                     s23.toString();\n" +
			"                }\n" +
			"            }.run();\n" +
			"        }\n" +
			"    }\n" +
			"}\n",

		},
		null, /* classLibs */
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	if (s1 != null)\n" +
		"	    ^^\n" +
		"Redundant null check: The variable s1 cannot be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 18)\n" +
		"	s3.toString();\n" +
		"	^^\n" +
		"Potential null pointer access: The variable s3 may be null at this location\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 28)\n" +
		"	s23.toString();\n" +
		"	^^^\n" +
		"Null pointer access: The variable s23 can only be null at this location\n" +
		"----------\n",
		"",/* expected output */
		"",/* expected error */
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// Bug 384380 - False positive on a "Potential null pointer access" after a continue
// original test case
public void testBug384380() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" +
				"	public static class Container{\n" +
				"		public int property;\n" +
				"	}\n" +
				"	public static class CustomException extends Exception {\n" +
				"		private static final long	 serialVersionUID	= 1L;\n" +
				"	}\n" +
				"	public static void anotherMethod() throws CustomException {}\n" +
				"\n" +
				"	public static void method(final java.util.List<Container> list) {\n" +
				"		for (final Container c : list) {\n" +
				"			if(c == null)\n" +
				"				continue; // return or break, are fine though\n" +
				"\n" +
				"			// without this try-catch+for+exception block it does not fails\n" +
				"			try {\n" +
				"				for(int i = 0; i < 10 ; i++) // needs a loop here (a 'while' or a 'for') to fail\n" +
				"					anotherMethod(); // throwing directly CustomException make it fails too\n" +
				"			} catch (final CustomException e) {\n" +
				"				// it fails even if catch is empty\n" +
				"			}\n" +
				"			c.property += 1; // \"Potential null pointer access: The variable c may be null at this location\"\n" +
				"		}\n" +
				"\n" +
				"	}\n" +
				"}\n"
			},
			"");
	}
}
// Bug 384380 - False positive on a "Potential null pointer access" after a continue
// variant with a finally block
public void testBug384380_a() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" +
				"	public static class Container{\n" +
				"		public int property;\n" +
				"	}\n" +
				"	public static class CustomException extends Exception {\n" +
				"		private static final long	 serialVersionUID	= 1L;\n" +
				"	}\n" +
				"	public static void anotherMethod() throws CustomException {}\n" +
				"\n" +
				"	public static void method(final java.util.List<Container> list) {\n" +
				"		for (final Container c : list) {\n" +
				"			if(c == null)\n" +
				"				continue; // return or break, are fine though\n" +
				"\n" +
				"			// without this try-catch+for+exception block it does not fails\n" +
				"			try {\n" +
				"				for(int i = 0; i < 10 ; i++) // needs a loop here (a 'while' or a 'for') to fail\n" +
				"					anotherMethod(); // throwing directly CustomException make it fails too\n" +
				"			} catch (final CustomException e) {\n" +
				"				// it fails even if catch is empty\n" +
				"			} finally {\n" +
				"				System.out.print(3);\n" +
				"			}\n" +
				"			c.property += 1; // \"Potential null pointer access: The variable c may be null at this location\"\n" +
				"		}\n" +
				"\n" +
				"	}\n" +
				"}\n"
			},
			"");
	}
}
// Bug 384380 - False positive on a "Potential null pointer access" after a continue
// while & foreach loops
public void testBug384380_b() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" +
				"	public static class Container{\n" +
				"		public int property;\n" +
				"	}\n" +
				"	public static class CustomException extends Exception {\n" +
				"		private static final long	 serialVersionUID	= 1L;\n" +
				"	}\n" +
				"	public static void anotherMethod() throws CustomException {}\n" +
				"\n" +
				"	public static void method(final java.util.List<Container> list) {\n" +
				"		java.util.Iterator<Container> it = list.iterator();\n" +
				"		while (it.hasNext()) {\n" +
				"			final Container c = it.next();\n" +
				"			if(c == null)\n" +
				"				continue; // return or break, are fine though\n" +
				"\n" +
				"			// without this try-catch+for+exception block it does not fails\n" +
				"			try {\n" +
				"				for(Container c1 : list) // needs a loop here (a 'while' or a 'for') to fail\n" +
				"					anotherMethod(); // throwing directly CustomException make it fails too\n" +
				"			} catch (final CustomException e) {\n" +
				"				// it fails even if catch is empty\n" +
				"			}\n" +
				"			c.property += 1; // \"Potential null pointer access: The variable c may be null at this location\"\n" +
				"		}\n" +
				"\n" +
				"	}\n" +
				"}\n"
			},
			"");
	}
}
public void testBug376263() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"    private int x;\n" +
			"\n" +
			"    static void test(Test[] array) {\n" +
			"        Test elem = null;\n" +
			"        int i = 0;\n" +
			"        while (i < array.length) {\n" +
			"            if (i == 0) {\n" +
			"                elem = array[0];\n" +
			"            }\n" +
			"            if (elem != null) {\n" +
			"                while (true) {\n" +
			"                    if (elem.x >= 0 || i >= array.length) { // should not warn here\n" +
			"                        break;\n" +
			"                    }\n" +
			"                    elem = array[i++];\n" +
			"                }\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"}"
		},
		"",
		null/*classLibraries*/,
		true/*shouldFlush*/,
		null/*vmArgs*/,
		customOptions,
		null/*requestor*/);
}
//object/array allocation
public void testExpressions01() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	 void foo() {\n" +
			"		if (new Object() == null)\n" +
			"           System.out.println(\"null\");\n" +
			"    }\n" +
			"	 void goo() {\n" +
			"		if (null != this.new I())\n" +
			"           System.out.println(\"nonnull\");\n" +
			"    }\n" +
			"    void hoo() {\n" +
			"		if (null != new Object[3])\n" +
			"           System.out.println(\"nonnull\");\n" +
			"    }\n" +
			"    class I {}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	if (new Object() == null)\n" +
		"	    ^^^^^^^^^^^^\n" +
		"Null comparison always yields false: this expression cannot be null\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	System.out.println(\"null\");\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 7)\n" +
		"	if (null != this.new I())\n" +
		"	            ^^^^^^^^^^^^\n" +
		"Redundant null check: this expression cannot be null\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 11)\n" +
		"	if (null != new Object[3])\n" +
		"	            ^^^^^^^^^^^^^\n" +
		"Redundant null check: this expression cannot be null\n" +
		"----------\n"
	);
}
//'this' expressions (incl. qualif.)
public void testExpressions02() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	 void foo() {\n" +
			"		if (this == null)\n" +
			"           System.out.println(\"null\");\n" +
			"    }\n" +
			"    class I {\n" +
			"        void goo() {\n" +
			"		     if (null != X.this)\n" +
			"                System.out.println(\"nonnull\");\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	if (this == null)\n" +
		"	    ^^^^\n" +
		"Null comparison always yields false: this expression cannot be null\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	System.out.println(\"null\");\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 8)\n" +
		"	if (null != X.this)\n" +
		"	            ^^^^^^\n" +
		"Redundant null check: this expression cannot be null\n" +
		"----------\n"
	);
}
//various non-null expressions: class-literal, string-literal, casted 'this'
public void testExpressions03() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	 void foo() {\n" +
			"		if (X.class == null)\n" +
			"           System.out.println(\"null\");\n" +
			"    }\n" +
			"    void goo() {\n" +
			"        if (null != \"STRING\")\n" +
			"            System.out.println(\"nonnull\");\n" +
			"        if (null == (Object)this)\n" +
			"            System.out.println(\"I'm null\");\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	if (X.class == null)\n" +
		"	    ^^^^^^^\n" +
		"Null comparison always yields false: this expression cannot be null\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	System.out.println(\"null\");\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 7)\n" +
		"	if (null != \"STRING\")\n" +
		"	            ^^^^^^^^\n" +
		"Redundant null check: this expression cannot be null\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 9)\n" +
		"	if (null == (Object)this)\n" +
		"	            ^^^^^^^^^^^^\n" +
		"Null comparison always yields false: this expression cannot be null\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 10)\n" +
		"	System.out.println(\"I\'m null\");\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n"
	);
}

//a non-null ternary expression
public void testExpressions04() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    void foo(boolean b) {\n" +
			"		Object o1 = new Object();\n" +
			"		Object o2 = new Object();\n" +
			"		if ((b ? o1 : o2) != null)\n" +
			"			System.out.println(\"null\");\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	if ((b ? o1 : o2) != null)\n" +
		"	    ^^^^^^^^^^^^^\n" +
		"Redundant null check: this expression cannot be null\n" +
		"----------\n"
	);
}

// Bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
// simplified: only try-finally involved
public void testBug345305_1() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    void foo() {\n" +
			"        String s = null;\n" +
			"        try {\n" +
			"            s = \"hi\";\n" +
			"        } finally {\n" +
			"            s.length();\n" +
			"            s = null;\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		});
}

// Bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
// original test case
public void testBug345305_2() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    void foo() {\n" +
			"        String s = null;\n" +
			"        while (true) {\n" +
			"            try {\n" +
			"                s = \"hi\";\n" +
			"            }\n" +
			"            finally {\n" +
			"                s.length();\n" +
			"                s = null;\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		});
}

// Bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
// assignment in method argument position
public void testBug345305_3() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    void foo() {\n" +
			"        String s = null;\n" +
			"        while (true) {\n" +
			"            try {\n" +
			"                check(s = \"hi\");\n" +
			"            }\n" +
			"            finally {\n" +
			"                s.length();\n" +
			"                s = null;\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"    void check(String s) {}\n" +
			"}\n"
		});
}

// Bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
// analysis of second local variable must not interfere
public void testBug345305_4() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    void foo() {\n" +
			"        String s = \"\";\n" +
			"        String s2 = null;\n" +
			"        while (true) {\n" +
			"            try {\n" +
			"                s = null;\n" +
			"                bar();\n" +
			"                s2 = \"world\";\n" +
			"            }\n" +
			"            finally {\n" +
			"                s.length();\n" +
			"                s = null;\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"    void bar() {}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	s.length();\n" +
		"	^\n" +
		"Null pointer access: The variable s can only be null at this location\n" +
		"----------\n");
}

// Bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
// block-less if involved - info about pot.nn. was lost when checking against loop's info (deferred check)
public void testBug345305_6() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    void foo(boolean b) {\n" +
			"        String s = null;\n" +
			"        while (true) {\n" +
			"            try {\n" +
			"                if (b)\n" +
			"                    s = \"hi\";\n" +
			"            }\n" +
			"            finally {\n" +
			"                s.length();\n" +
			"                s = null;\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	s.length();\n" +
		"	^\n" +
		"Potential null pointer access: The variable s may be null at this location\n" +
		"----------\n");
}

// Bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
// block-less if involved
public void testBug345305_7() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    void foo(boolean b) {\n" +
			"        while (true) {\n" +
			"            String s = null;\n" +
			"            try {\n" +
			"                if (b)\n" +
			"                    s = \"hi\";\n" +
			"            }\n" +
			"            finally {\n" +
			"                s.length();\n" +
			"                s = null;\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	s.length();\n" +
		"	^\n" +
		"Potential null pointer access: The variable s may be null at this location\n" +
		"----------\n");
}

// Bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
// consider exception thrown from cast expression
public void testBug345305_8() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    void foo(Object o) {\n" +
			"        while (true) {\n" +
			"            String s = null;\n" +
			"            try {\n" +
			"                 s = (String) o;\n" +
			"            }\n" +
			"            finally {\n" +
			"                s.length();\n" +
			"                s = null;\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	s.length();\n" +
		"	^\n" +
		"Potential null pointer access: The variable s may be null at this location\n" +
		"----------\n");
}

// Bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
// consider exception thrown from binary expression
public void testBug345305_9() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    void foo(int i, int j) {\n" +
			"        while (true) {\n" +
			"            String s = null;\n" +
			"            try {\n" +
			"                 s = ((i / j) == 3) ? \"3\" : \"not-3\";\n" +
			"            }\n" +
			"            finally {\n" +
			"                s.length();\n" +
			"                s = null;\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	s.length();\n" +
		"	^\n" +
		"Potential null pointer access: The variable s may be null at this location\n" +
		"----------\n");
}

// Bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
// inner labeled block with break
public void testBug345305_10() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    void foo(int j) {\n" +
			"        while (true) {\n" +
			"            String s = null;\n" +
			"            try {\n" +
			"                int i=0;\n" +
			"                block: {\n" +
			"                    if (i++ == j)\n" +
			"                         break block;\n" +
			"                    s = \"\";\n" +
			"                    return;\n" +
			"                }\n" +
			"            }\n" +
			"            finally {\n" +
			"                s.length();\n" +
			"                s = null;\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 15)\n" +
		"	s.length();\n" +
		"	^\n" +
		"Potential null pointer access: The variable s may be null at this location\n" +
		"----------\n");
}

// Bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
// switch statement
public void testBug345305_11() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    void foo(int j) {\n" +
			"        while (true) {\n" +
			"            String s = null;\n" +
			"            try {\n" +
			"                switch (j) {\n" +
			"                    case 3:\n" +
			"                        s = \"\";\n" +
			"                        return;\n" +
			"                    default: return;\n" +
			"                }\n" +
			"            }\n" +
			"            finally {\n" +
			"                s.length();\n" +
			"                s = null;\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 14)\n" +
		"	s.length();\n" +
		"	^\n" +
		"Potential null pointer access: The variable s may be null at this location\n" +
		"----------\n");
}

// Bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
// assignment inside conditional expression
public void testBug345305_12() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    String foo(boolean b) {\n" +
			"        while (true) {\n" +
			"            String s = null;\n" +
			"            try {\n" +
			"                 return b ? (s = \"be\") : \"be not\";\n" +
			"            }\n" +
			"            finally {\n" +
			"                s.length();\n" +
			"                s = null;\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	s.length();\n" +
		"	^\n" +
		"Potential null pointer access: The variable s may be null at this location\n" +
		"----------\n");
}

// Bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
// explicit throw
public void testBug345305_13() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    String foo(boolean b) {\n" +
			"        while (true) {\n" +
			"            String s = null;\n" +
			"            RuntimeException ex = new RuntimeException();\n" +
			"            try {\n" +
			"                 if (b)\n" +
			"                     throw ex;\n" +
			"                 s = \"be\";\n" +
			"            }\n" +
			"            finally {\n" +
			"                s.length();\n" +
			"                s = null;\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	s.length();\n" +
		"	^\n" +
		"Potential null pointer access: The variable s may be null at this location\n" +
		"----------\n");
}

// Bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
// do-while
public void testBug345305_14() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    void foo1(boolean b) {\n" +
			"        while (true) {\n" +
			"            String s = null;\n" +
			"            try {\n" +
			"                 do {\n" +
			"                     s = \"be\";\n" +
			"                     if (b)\n" +
			"                         return;\n" +
			"                 } while (true);\n" +
			"            }\n" +
			"            finally {\n" +
			"                s.length(); // don't complain here\n" +
			"                s = null;\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"    void foo2(boolean b) {\n" +
			"        while (true) {\n" +
			"            String s = null;\n" +
			"            try {\n" +
			"                 do {\n" +
			"                     if (b)\n" +
			"                         continue;\n" +
			"                     s = \"be\";\n" +
			"                     b = !b;\n" +
			"                 } while (b);\n" +
			"            }\n" +
			"            finally {\n" +
			"                s.length();\n" +
			"                s = null;\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 30)\n" +
		"	s.length();\n" +
		"	^\n" +
		"Potential null pointer access: The variable s may be null at this location\n" +
		"----------\n");
}

// Bug 364326 - [compiler][null] NullPointerException is not found by compiler. FindBugs finds that one
public void testBug364326() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // employs auto-unboxing
	runNegativeNullTest(
		new String[] {
			"NPE_OnBoxing.java",
			"\n" +
			"public class NPE_OnBoxing\n" +
			"{\n" +
			"    private interface IValue\n" +
			"    {\n" +
			"        boolean isSomething();\n" +
			"    }\n" +
			"\n" +
			"    private final IValue m_Value;\n" +
			"\n" +
			"    public NPE_OnBoxing()\n" +
			"    {\n" +
			"        m_Value = null;\n" +
			"    }\n" +
			"\n" +
			"    public boolean isSomething()\n" +
			"    {\n" +
			"        return m_Value != null ? m_Value.isSomething() : null;\n" +
			"    }\n" +
			"\n" +
			"    public static void main(final String [] args)\n" +
			"    {\n" +
			"        new NPE_OnBoxing().isSomething();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in NPE_OnBoxing.java (at line 18)\n" +
		"	return m_Value != null ? m_Value.isSomething() : null;\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential null pointer access: This expression of type Boolean may be null but requires auto-unboxing\n" +
		"----------\n");
}

// Bug 401088 - [compiler][null] Wrong warning "Redundant null check" inside nested try statement
public void testBug401088() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"	private static void occasionallyThrowException() throws Exception {\n" +
			"		throw new Exception();\n" +
			"	}\n" +
			"\n" +
			"	private static void open() throws Exception {\n" +
			"		occasionallyThrowException();\n" +
			"	}\n" +
			"\n" +
			"	private static void close() throws Exception {\n" +
			"		occasionallyThrowException();\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String s[]) {\n" +
			"		Exception exc = null;\n" +
			"		try {\n" +
			"			open();\n" +
			"			// do more things\n" +
			"		}\n" +
			"		catch (Exception e) {\n" +
			"			exc = e;\n" +
			"		}\n" +
			"		finally {\n" +
			"			try {\n" +
			"				close();\n" +
			"			}\n" +
			"			catch (Exception e) {\n" +
			"				if (exc == null) // should not warn on this line\n" +
			"					exc = e;\n" +
			"			}\n" +
			"		}\n" +
			"		if (exc != null)\n" +
			"			System.out.println(exc);\n" +
			"	}\n" +
			"}\n"
		},
		"java.lang.Exception");
}
// Bug 401088 - [compiler][null] Wrong warning "Redundant null check" inside nested try statement
public void testBug401088a() {
 runConformTest(
     new String[] {
         "X.java",
         "public class X {\n" +
         "\n" +
         "   private static void occasionallyThrowException() throws Exception {\n" +
         "       throw new Exception();\n" +
         "   }\n" +
         "\n" +
         "   private static void open() throws Exception {\n" +
         "       occasionallyThrowException();\n" +
         "   }\n" +
         "\n" +
         "   private static void close() throws Exception {\n" +
         "       occasionallyThrowException();\n" +
         "   }\n" +
         "\n" +
         "   public static void main(String s[]) {\n" +
         "       Exception exc = null;\n" +
         "       try {\n" +
         "           open();\n" +
         "           // do more things\n" +
         "       }\n" +
         "       catch (Exception e) {\n" +
         "           exc = e;\n" +
         "       }\n" +
         "       finally {\n" +
         "           try {\n" +
         "               close();\n" +
         "           }\n" +
         "           catch (Exception e) {\n" +
         "               if (exc == null) // should not warn on this line\n" +
         "                   exc = e;\n" +
         "           }\n" +
         "           finally { System.out.print(1); }\n" +
         "       }\n" +
         "       if (exc != null)\n" +
         "           System.out.println(exc);\n" +
         "   }\n" +
         "}\n"
     },
     "1java.lang.Exception");
}
// Bug 401092 - [compiler][null] Wrong warning "Redundant null check" in outer catch of nested try
public void test401092() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.Date;\n" +
			"\n" +
			"public class X {\n" +
			"\n" +
			"    private static void occasionallyThrowException() throws Exception {\n" +
			"        throw new Exception();\n" +
			"    }\n" +
			"\n" +
			"    private static Date createDate() throws Exception {\n" +
			"        occasionallyThrowException();\n" +
			"        return new Date();\n" +
			"    }\n" +
			"\n" +
			"    public static void main(String s[]) {\n" +
			"        Date d = null;\n" +
			"        try {\n" +
			"            d = createDate();\n" +
			"            System.out.println(d.toString());\n" +
			"            try {\n" +
			"                occasionallyThrowException();\n" +
			"            }\n" +
			"            catch (Exception exc) {\n" +
			"            }\n" +
			"        }\n" +
			"        catch (Exception exc) {\n" +
			"            if (d != null) // should not warn in this line\n" +
			"                System.out.println(d.toString());\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		});
}
// Bug 401092 - [compiler][null] Wrong warning "Redundant null check" in outer catch of nested try
public void test401092a() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.Date;\n" +
			"\n" +
			"public class X {\n" +
			"\n" +
			"    private static void occasionallyThrowException() throws Exception {\n" +
			"        throw new Exception();\n" +
			"    }\n" +
			"\n" +
			"    private static Date createDate() throws Exception {\n" +
			"        occasionallyThrowException();\n" +
			"        return new Date();\n" +
			"    }\n" +
			"\n" +
			"    public static void main(String s[]) {\n" +
			"        Date d = null;\n" +
			"        try {\n" +
			"            d = createDate();\n" +
			"            System.out.println(d.toString());\n" +
			"            try {\n" +
			"                occasionallyThrowException();\n" +
			"            }\n" +
			"            catch (Exception exc) {\n" +
			"            }\n" +
			"            finally { System.out.println(1); }\n" +
			"        }\n" +
			"        catch (Exception exc) {\n" +
			"            if (d != null) // should not warn in this line\n" +
			"                System.out.println(d.toString());\n" +
			"        }\n" +
			"        finally { System.out.println(2); }\n" +
			"    }\n" +
			"}\n"
		});
}
// Bug 402993 - [null] Follow up of bug 401088: Missing warning about redundant null check
public void testBug402993() {
	runNegativeNullTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"\n" +
			"	private static void occasionallyThrowException() throws Exception {\n" +
			"		if ((System.currentTimeMillis() & 1L) != 0L)\n" +
			"			throw new Exception();\n" +
			"	}\n" +
			"\n" +
			"	private static void open() throws Exception {\n" +
			"		occasionallyThrowException();\n" +
			"	}\n" +
			"\n" +
			"	private static void close() throws Exception {\n" +
			"		occasionallyThrowException();\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String s[]) {\n" +
			"		Exception exc = null;\n" +
			"		try {\n" +
			"			open();\n" +
			"			// do more things\n" +
			"		}\n" +
			"		catch (Exception e) {\n" +
			"			if (exc == null) // no warning here ??\n" +
			"				;\n" +
			"		}\n" +
			"		finally {\n" +
			"			try {\n" +
			"				close();\n" +
			"			}\n" +
			"			catch (Exception e) {\n" +
			"				if (exc == null) // No warning here ??\n" +
			"					exc = e;\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Test.java (at line 23)\n" +
		"	if (exc == null) // no warning here ??\n" +
		"	    ^^^\n" +
		"Redundant null check: The variable exc can only be null at this location\n" +
		"----------\n" +
		"2. ERROR in Test.java (at line 31)\n" +
		"	if (exc == null) // No warning here ??\n" +
		"	    ^^^\n" +
		"Redundant null check: The variable exc can only be null at this location\n" +
		"----------\n");
}
// Bug 402993 - [null] Follow up of bug 401088: Missing warning about redundant null check
// variant with finally block in inner try
public void testBug402993a() {
	runNegativeNullTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"\n" +
			"	private static void occasionallyThrowException() throws Exception {\n" +
			"		if ((System.currentTimeMillis() & 1L) != 0L)\n" +
			"			throw new Exception();\n" +
			"	}\n" +
			"\n" +
			"	private static void open() throws Exception {\n" +
			"		occasionallyThrowException();\n" +
			"	}\n" +
			"\n" +
			"	private static void close() throws Exception {\n" +
			"		occasionallyThrowException();\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String s[]) {\n" +
			"		Exception exc = null;\n" +
			"		try {\n" +
			"			open();\n" +
			"			// do more things\n" +
			"		}\n" +
			"		catch (Exception e) {\n" +
			"			if (exc == null) // no warning here ??\n" +
			"				;\n" +
			"		}\n" +
			"		finally {\n" +
			"			try {\n" +
			"				close();\n" +
			"			}\n" +
			"			catch (Exception e) {\n" +
			"				if (exc == null) // No warning here ??\n" +
			"					exc = e;\n" +
			"			} finally {\n" +
			"				System.out.print(1);\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Test.java (at line 23)\n" +
		"	if (exc == null) // no warning here ??\n" +
		"	    ^^^\n" +
		"Redundant null check: The variable exc can only be null at this location\n" +
		"----------\n" +
		"2. ERROR in Test.java (at line 31)\n" +
		"	if (exc == null) // No warning here ??\n" +
		"	    ^^^\n" +
		"Redundant null check: The variable exc can only be null at this location\n" +
		"----------\n");
}
public void testBug453305() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses foreach loop
	runConformTest(
		new String[] {
			"NullTest.java",
			"import java.util.*;\n" +
			"public class NullTest {\n" +
			"    class SomeOtherClass {\n" +
			"\n" +
			"        public SomeOtherClass m() {\n" +
			"            return new SomeOtherClass();\n" +
			"        }\n" +
			"\n" +
			"        public void doSomething() {\n" +
			"        }\n" +
			"    }\n" +
			"\n" +
			"    public Object m1() {\n" +
			"        SomeOtherClass result = null;\n" +
			"        List<Object> list = new ArrayList<Object>();\n" +
			"        for (Object next : list) {\n" +
			"            System.out.println(next);\n" +
			"            boolean bool = false;\n" +
			"            if (bool) {\n" +
			"                SomeOtherClass something = new SomeOtherClass();\n" +
			"                result = something.m();\n" +
			"            } else {\n" +
			"                result = new SomeOtherClass();\n" +
			"            }\n" +
			"            result.doSomething(); // warning is here\n" +
			"            break;\n" +
			"        }\n" +
			"        return null;\n" +
			"    }\n" +
			"}\n"
		});
}
public void testBug431016() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses foreach loop
	runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"  void test(Object[] values) {\n" +
			"    Object first = null;\n" +
			"    for (Object current : values) {\n" +
			"        if (first == null) {\n" +
			"            first = current;\n" +
			"        }\n" +
			"\n" +
			"        if (current.hashCode() > 0) {\n" +
			"            System.out.println(first.hashCode());\n" +
			"        }\n" +
			"\n" +
			"        System.out.println(first.hashCode());\n" +
			"      }\n" +
			"  }\n" +
			"}\n"
		});
}
// originally created for documentation purpose, see https://bugs.eclipse.org/453483#c9
public void testBug431016_simplified() {
	runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"  void test(Object input, boolean b) {\n" +
			"    Object o = null;\n" +
			"    while (true) {\n" +
			"      if (o == null)\n" +
			"        o = input;\n" +
			"      if (b)\n" +
			"        o.toString();\n" +
			"      o.toString();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"
		});
}
public void testBug432109() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses generics & foreach loop
	runConformTest(
		new String[] {
			"Test.java",
			"import java.util.Collection;\n" +
			"public class Test {\n" +
			"  public void test(Collection <Object> values)\n" +
			"  {\n" +
			"      boolean condition = false;\n" +
			"      \n" +
			"      for(Object value : values)\n" +
			"      {\n" +
			"                  \n" +
			"          if(value == null)\n" +
			"          {\n" +
			"              if( condition )\n" +
			"              {\n" +
			"                  // without this continue statement, \n" +
			"                  // there is no warning below\n" +
			"                  continue; \n" +
			"              }\n" +
			"              \n" +
			"              value = getDefaultValue();\n" +
			"          }\n" +
			"          \n" +
			"          // IDE complains here about potential null pointer access\n" +
			"          value.toString();\n" +
			"      }\n" +
			"  }\n" +
			"\n" +
			"  public String getDefaultValue() { return \"<empty>\"; }\n" +
			"}\n"
		});
}
public void testBug435528_orig() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"Test.java",
			"public class Test\n" +
			"{\n" +
			"   static final String a = \"A\";\n" +
			"\n" +
			"   static void main(String args[])\n" +
			"   {\n" +
			"      String x = null;\n" +
			"      while (true) {\n" +
			"         x = Math.random() < 0.5 ? a : \"BB\";\n" +
			"         if (a != null) {\n" +
			"            System.out.println(\"s2 value: \" + x);\n" +
			"         }\n" +
			"         if (x.equals(\"A\")) {\n" +
			"            break;\n" +
			"         } else {\n" +
			"            x = null;\n" +
			"         }\n" +
			"      }\n" +
			"   }\n" +
			"}\n"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in Test.java (at line 10)\n" +
		"	if (a != null) {\n" +
		"	    ^\n" +
		"Redundant null check: The field a is a nonnull constant\n" +
		"----------\n" +
		"2. WARNING in Test.java (at line 15)\n" +
		"	} else {\n" +
		"            x = null;\n" +
		"         }\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally\n" +
		"----------\n";
	runner.customOptions = getCompilerOptions();
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
public void testBug435528_notaconstant() {
	runConformTest(
		true/*flush*/,
		new String[] {
			"Test.java",
			"public class Test\n" +
			"{\n" +
			"   static String a	;\n" +
			"\n" +
			"   static void main(String args[])\n" +
			"   {\n" +
			"      String x = null;\n" +
			"      while (true) {\n" +
			"         x = Math.random() < 0.5 ? a : \"BB\";\n" +
			"         if (a != null) {\n" +
			"            System.out.println(\"s2 value: \" + x);\n" +
			"         }\n" +
			"         if (x.equals(\"A\")) {\n" +
			"            break;\n" +
			"         } else {\n" +
			"            x = null;\n" +
			"         }\n" +
			"      }\n" +
			"   }\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in Test.java (at line 15)\n" +
		"	} else {\n" +
		"            x = null;\n" +
		"         }\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally\n" +
		"----------\n",
		"",
		"",
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}
public void testBug418500() {
	runConformTest(
		new String[] {
			"Test.java",
			"import java.util.*;\n" +
			"public class Test {\n" +
			(this.complianceLevel < ClassFileConstants.JDK1_5 ? "\n" : "  @SuppressWarnings(\"unchecked\")\n" ) +
			"  void method() {\n" +
			"    Map topMap = new HashMap();\n" +
			"    List targets = null;\n" +
			"    \n" +
			"    for (int idx = 1; idx < 100; idx++) {\n" +
			"      String[] targetArray = (String[]) topMap.get(\"a\");\n" +
			"      if (targetArray != null) {\n" +
			"        targets = Arrays.asList(targetArray);\n" +
			"      } else {\n" +
			"        targets = new ArrayList(64);\n" +
			"      }\n" +
			"      if (targets.size() > 0) {\n" +
			"        topMap.put(\"b\", targets.toArray(new String[1]));\n" +
			"      } else {\n" +
			"        topMap.remove(\"b\");\n" +
			"      }\n" +
			"\n" +
			"      // BUG - this statement causes null analysis to\n" +
			"      // report that at the targets.size() statement above\n" +
			"      // targets must be null. Commenting this line eliminates the error.\n" +
			"      targets = null;\n" +
			"    }\n" +
			"  }\n" +
			"}\n"
		});
}
public void testBug441737() {
	runConformTest(
		new String[] {
			"Bogus.java",
			"public class Bogus {\n" +
			"    static boolean ok = true;\n" +
			"    static int count = 0;\n" +
			"    public static void main(String[] args) {\n" +
			"        Thing x = new Thing();\n" +
			"        // if y is left uninitialized here, the warning below disappears\n" +
			"        Thing y = null;\n" +
			"        do {\n" +
			"            y = x;\n" +
			"            if (ok) {\n" +
			"                // if this assignment is moved out of the if statement\n" +
			"                // or commented out, the warning below disappears\n" +
			"                x = y.resolve();\n" +
			"            }\n" +
			"            // a warning about y being potentially null occurs here:\n" +
			"            x = y.resolve();\n" +
			"        } while (x != y);\n" +
			"    }\n" +
			"\n" +
			"    static class Thing {\n" +
			"        public Thing resolve() {\n" +
			"            return count++ > 2 ? this : new Thing();\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		});
}
// fixed in 3.6.2, likely via bug 332637.
public void testBug195638_comment3() {
	runConformTest(
		new String[] {
			"Test.java",
			"import java.sql.Connection;\n" +
			"import java.sql.SQLException;\n" +
			"public class Test {\n" +
			"  void m() throws SQLException\n" +
			"  {\n" +
			"    Connection conn = null;\n" +
			"    try\n" +
			"    {\n" +
			"      conn = createConnection();\n" +
			"\n" +
			"      for (; ; )\n" +
			"      {\n" +
			"        throwSomething();\n" +
			"      }\n" +
			"    }\n" +
			"    catch (MyException e)\n" +
			"    {\n" +
			"      conn.rollback(); //The variable can never be null here...\n" +
			"    }\n" +
			"  }\n" +
			"\n" +
			"  private void throwSomething() throws MyException\n" +
			"  {\n" +
			"    throw new MyException();\n" +
			"  }\n" +
			"\n" +
			"  class MyException extends Exception\n" +
			"  {\n" +
			"\n" +
			"  }\n" +
			"\n" +
			"  private Connection createConnection()\n" +
			"  {\n" +
			"    return null;\n" +
			"  }\n" +
			"}\n"
		});
}
public void testBug195638_comment6() {
	runNegativeNullTest(
		new String[] {
			"CanOnlyBeNullShouldBeMayBeNull.java",
			"public class CanOnlyBeNullShouldBeMayBeNull {\n" +
			"\n" +
			"	private void method() {\n" +
			"		String tblVarRpl = null;\n" +
			"		while (true) {\n" +
			"			boolean isOpenVariableMortageRateProduct = true;\n" +
			"			boolean tblVarRplAllElementAddedIndicator = false;\n" +
			"			if (isOpenVariableMortageRateProduct) {\n" +
			"				if (tblVarRplAllElementAddedIndicator == false)\n" +
			"					tblVarRpl = \"\";\n" +
			"				tblVarRpl.substring(1);	//Can only be null???\n" +
			"				return; \n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in CanOnlyBeNullShouldBeMayBeNull.java (at line 3)\n" +
		"	private void method() {\n" +
		"	             ^^^^^^^^\n" +
		"The method method() from the type CanOnlyBeNullShouldBeMayBeNull is never used locally\n" +
		"----------\n" +
		"2. ERROR in CanOnlyBeNullShouldBeMayBeNull.java (at line 11)\n" +
		"	tblVarRpl.substring(1);	//Can only be null???\n" +
		"	^^^^^^^^^\n" +
		"Potential null pointer access: The variable tblVarRpl may be null at this location\n" +
		"----------\n");
}
public void testBug195638_comment14() {
	runNegativeNullTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"    private void test() {\n" +
			"        boolean x = true;\n" +
			"        Object o = null;\n" +
			"        \n" +
			"        for (;;) {\n" +
			"            if (x) o = new Object();\n" +
			"            \n" +
			"            o.toString(); // warning here\n" + // bug was: Null pointer access: The variable o can only be null at this location
			"            \n" +
			"            o = null;\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in Test.java (at line 2)\n" +
		"	private void test() {\n" +
		"	             ^^^^^^\n" +
		"The method test() from the type Test is never used locally\n" +
		"----------\n" +
		"2. ERROR in Test.java (at line 9)\n" +
		"	o.toString(); // warning here\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n");
}
public void testBug195638_comment19() {
	runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"    public void testIt() {\n" +
			"      Object aRole = null;\n" +
			"      for (;;) {\n" +
			"        aRole = new Object();\n" +
			"        if (aRole.toString() == null) {\n" +
			"          aRole = getObject(); // changing to \"new Object()\" makes warning disappear.\n" +
			"        }\n" +
			"        aRole.toString();\n" +
			"        // above line gets: \"Null pointer access: The variable aRole can only be null at this location\"\n" +
			"        break;\n" +
			"      }\n" +
			"    }\n" +
			"    private Object getObject() {\n" +
			"      return new Object();\n" +
			"    }\n" +
			"}\n"
		});
}
public void testBug454031() {
	runNegativeNullTest(
		new String[] {
			"xy/Try.java",
			"package xy;\n" +
			"\n" +
			"public class Try {\n" +
			"    public static void main(String[] args) {\n" +
			"        foo(new Node());\n" +
			"    }\n" +
			"    static void foo(Node n) {\n" +
			"        Node selectedNode= n;\n" +
			"        if (selectedNode == null) {\n" +
			"            return;\n" +
			"        }\n" +
			"        while (selectedNode != null && !(selectedNode instanceof Cloneable)) {\n" +
			"            selectedNode= selectedNode.getParent();\n" +
			"        }\n" +
			"        if (selectedNode == null) { //wrong problem: Null comparison always yields false: The variable selectedNode cannot be null at this location\n" +
			"            // wrong problem: dead code\n" +
			"            System.out.println(selectedNode.hashCode());\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			"\n" +
			"class Node {\n" +
			"    Node getParent() {\n" +
			"        return null;\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in xy\\Try.java (at line 17)\n" +
		"	System.out.println(selectedNode.hashCode());\n" +
		"	                   ^^^^^^^^^^^^\n" +
		"Null pointer access: The variable selectedNode can only be null at this location\n" +
		"----------\n");
}
// switch with fall-through nested in for:
public void testBug451660() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"        public static void main(String[] args)\n" +
			"    {\n" +
			"        String s = null;\n" +
			"        for(; true;) // ok with \"while(true)\"\n" +
			"        {\n" +
			"            switch(0)\n" +
			"            {\n" +
			"            default:\n" +
			"                s = \"Hello!\";\n" +
			"            case 1:\n" +
			"                System.out.println(s.toString());\n" +
			"            }\n" +
			"            return;\n" +
			"        }\n" +
			"    }\n" +
			"\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	System.out.println(s.toString());\n" +
		"	                   ^\n" +
		"Potential null pointer access: The variable s may be null at this location\n" +
		"----------\n");
}
public void testBug486912KnownNullInLoop() {
	runNegativeNullTest(
		new String[] {
			"test/KnownNullInLoop.java",
			"package test;\n" +
			"\n" +
			"public class KnownNullInLoop {\n" +
			"	public void testDoWhile() {\n" +
			"		Object o1 = null;\n" +
			"		do {\n" +
			"			o1.hashCode(); // ERROR1: known null, but no problem reported.\n" +
			"		} while (false);\n" +
			"	}\n" +
			"\n" +
			"	public void testWhileWithBreak() {\n" +
			"		Object o1 = null;\n" +
			"		while (true) {\n" +
			"			o1.hashCode(); // ERROR2: known null, but no problem reported.\n" +
			"			break;\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		"----------\n" +
		"1. ERROR in test\\KnownNullInLoop.java (at line 7)\n" +
		"	o1.hashCode(); // ERROR1: known null, but no problem reported.\n" +
		"	^^\n" +
		"Null pointer access: The variable o1 can only be null at this location\n" +
		"----------\n" +
		"2. ERROR in test\\KnownNullInLoop.java (at line 14)\n" +
		"	o1.hashCode(); // ERROR2: known null, but no problem reported.\n" +
		"	^^\n" +
		"Null pointer access: The variable o1 can only be null at this location\n" +
		"----------\n"
	);
}
public void testBug486912PotNullInLoop_orig() {
	runNegativeNullTest(
		new String[] {
			"test/PotNullInLoop.java",
			"package test;\n" +
			"\n" +
			"public class PotNullInLoop {\n" +
			"	boolean b;\n" +
			"\n" +
			"	public void testDoWhile1() {\n" +
			"		Object o1 = null;\n" +
			"		Object o2 = new Object();\n" +
			"		Object potNonNull = b ? o2 : o2;\n" + // actually: def nn
			"		Object potNull = b ? o1 : o1;\n" +	  // actually: def n
			"		Object ponNullOrNonNull = b ? potNull : potNonNull;\n" +
			"		do {\n" +
			"			potNonNull.hashCode(); // OK\n" +
			"			potNull.hashCode(); // ERROR 1: pot null, but nothing reported\n" +
			"			ponNullOrNonNull.hashCode(); // ERROR 2: pot null, but nothing reported\n" +
			"		} while (false);\n" +
			"	}\n" +
			"\n" +
			"	public void testWhileWithBreak() {\n" +
			"		Object o1 = null;\n" +
			"		Object o2 = new Object();\n" +
			"		Object potNonNull = b ? o2 : o2;\n" +
			"		Object potNull = b ? o1 : o1;\n" +
			"		Object ponNullOrNonNull = b ? potNull : potNonNull;\n" +
			"		while (b) {\n" +
			"			potNonNull.hashCode(); // OK\n" +
			"			potNull.hashCode(); // ERROR 3 : pot null, but nothing reported\n" +
			"			ponNullOrNonNull.hashCode(); // ERROR 4: pot null, but nothing reported\n" +
			"			break;\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public void testWhile() {\n" +
			"		Object o1 = null;\n" +
			"		Object o2 = new Object();\n" +
			"		Object potNonNull = b ? o2 : o2;\n" +
			"		Object potNull = b ? o1 : o1;\n" +
			"		Object ponNullOrNonNull = b ? potNull : potNonNull;\n" +
			"		while (b) {\n" +
			"			potNonNull.hashCode(); // OK\n" +
			"			potNull.hashCode(); // OK: pot null, is reported\n" +
			"			ponNullOrNonNull.hashCode(); // OK: pot null, is reported\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public void testFor() {\n" +
			"		Object o1 = null;\n" +
			"		Object o2 = new Object();\n" +
			"		Object potNonNull = b ? o2 : o2;\n" +
			"		Object potNull = b ? o1 : o1;\n" +
			"		Object ponNullOrNonNull = b ? potNull : potNonNull;\n" +
			"		for (int i = 0; i < 1; i++) {\n" +
			"			potNonNull.hashCode(); // OK\n" +
			"			potNull.hashCode(); // OK: pot null, is reported\n" +
			"			ponNullOrNonNull.hashCode(); // OK: pot null, is reported\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public void testForEach() {\n" +
			"		Object o1 = null;\n" +
			"		Object o2 = new Object();\n" +
			"		Object potNonNull = b ? o2 : o2;\n" +
			"		Object potNull = b ? o1 : o1;\n" +
			"		Object ponNullOrNonNull = b ? potNull : potNonNull;\n" +
			"		for (int i = 0; i < 1; i++) {\n" +
			"			potNonNull.hashCode(); // OK\n" +
			"			potNull.hashCode(); // OK: pot null, is reported\n" +
			"			ponNullOrNonNull.hashCode(); // OK: pot null, is reported\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"",
		},
		"----------\n" +
		"1. ERROR in test\\PotNullInLoop.java (at line 14)\n" +
		"	potNull.hashCode(); // ERROR 1: pot null, but nothing reported\n" +
		"	^^^^^^^\n" +
		"Null pointer access: The variable potNull can only be null at this location\n" +
		"----------\n" +
		"2. ERROR in test\\PotNullInLoop.java (at line 15)\n" +
		"	ponNullOrNonNull.hashCode(); // ERROR 2: pot null, but nothing reported\n" +
		"	^^^^^^^^^^^^^^^^\n" +
		"Potential null pointer access: The variable ponNullOrNonNull may be null at this location\n" +
		"----------\n" +
		"3. ERROR in test\\PotNullInLoop.java (at line 27)\n" +
		"	potNull.hashCode(); // ERROR 3 : pot null, but nothing reported\n" +
		"	^^^^^^^\n" +
		"Null pointer access: The variable potNull can only be null at this location\n" +
		"----------\n" +
		"4. ERROR in test\\PotNullInLoop.java (at line 28)\n" +
		"	ponNullOrNonNull.hashCode(); // ERROR 4: pot null, but nothing reported\n" +
		"	^^^^^^^^^^^^^^^^\n" +
		"Potential null pointer access: The variable ponNullOrNonNull may be null at this location\n" +
		"----------\n" +
		"5. ERROR in test\\PotNullInLoop.java (at line 41)\n" +
		"	potNull.hashCode(); // OK: pot null, is reported\n" +
		"	^^^^^^^\n" +
		"Null pointer access: The variable potNull can only be null at this location\n" +
		"----------\n" +
		"6. ERROR in test\\PotNullInLoop.java (at line 42)\n" +
		"	ponNullOrNonNull.hashCode(); // OK: pot null, is reported\n" +
		"	^^^^^^^^^^^^^^^^\n" +
		"Potential null pointer access: The variable ponNullOrNonNull may be null at this location\n" +
		"----------\n" +
		"7. ERROR in test\\PotNullInLoop.java (at line 54)\n" +
		"	potNull.hashCode(); // OK: pot null, is reported\n" +
		"	^^^^^^^\n" +
		"Null pointer access: The variable potNull can only be null at this location\n" +
		"----------\n" +
		"8. ERROR in test\\PotNullInLoop.java (at line 55)\n" +
		"	ponNullOrNonNull.hashCode(); // OK: pot null, is reported\n" +
		"	^^^^^^^^^^^^^^^^\n" +
		"Potential null pointer access: The variable ponNullOrNonNull may be null at this location\n" +
		"----------\n" +
		"9. ERROR in test\\PotNullInLoop.java (at line 67)\n" +
		"	potNull.hashCode(); // OK: pot null, is reported\n" +
		"	^^^^^^^\n" +
		"Null pointer access: The variable potNull can only be null at this location\n" +
		"----------\n" +
		"10. ERROR in test\\PotNullInLoop.java (at line 68)\n" +
		"	ponNullOrNonNull.hashCode(); // OK: pot null, is reported\n" +
		"	^^^^^^^^^^^^^^^^\n" +
		"Potential null pointer access: The variable ponNullOrNonNull may be null at this location\n" +
		"----------\n"
	);
}
// variant of testBug486912PotNullInLoop_orig spiced up with potentiality from an 'unknown' o0:
public void testBug486912PotNullInLoop() {
	runNegativeNullTest(
		new String[] {
			"test/PotNullInLoop.java",
			"package test;\n" +
			"\n" +
			"public class PotNullInLoop {\n" +
			"	boolean b;\n" +
			"\n" +
			"	public void testDoWhile1(Object o0) {\n" +
			"		Object o1 = null;\n" +
			"		Object o2 = new Object();\n" +
			"		Object potNonNull = b ? o2 : o0;\n" +
			"		Object potNull = b ? o1 : o0;\n" +
			"		do {\n" +
			"			potNonNull.hashCode(); // OK\n" +
			"			potNull.hashCode(); // ERROR 1: pot null, but nothing reported\n" +
			"		} while (false);\n" +
			"	}\n" +
			"\n" +
			"	public void testWhileWithBreak(Object o0) {\n" +
			"		Object o1 = null;\n" +
			"		Object o2 = new Object();\n" +
			"		Object potNonNull = b ? o2 : o0;\n" +
			"		Object potNull = b ? o1 : o0;\n" +
			"		while (b) {\n" +
			"			potNonNull.hashCode(); // OK\n" +
			"			potNull.hashCode(); // ERROR 3 : pot null, but nothing reported\n" +
			"			break;\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public void testWhile(Object o0) {\n" +
			"		Object o1 = null;\n" +
			"		Object o2 = new Object();\n" +
			"		Object potNonNull = b ? o2 : o0;\n" +
			"		Object potNull = b ? o1 : o0;\n" +
			"		while (b) {\n" +
			"			potNonNull.hashCode(); // OK\n" +
			"			potNull.hashCode(); // OK: pot null, is reported\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public void testFor(Object o0) {\n" +
			"		Object o1 = null;\n" +
			"		Object o2 = new Object();\n" +
			"		Object potNonNull = b ? o2 : o0;\n" +
			"		Object potNull = b ? o1 : o0;\n" +
			"		for (int i = 0; i < 1; i++) {\n" +
			"			potNonNull.hashCode(); // OK\n" +
			"			potNull.hashCode(); // OK: pot null, is reported\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public void testForEach(Object o0) {\n" +
			"		Object o1 = null;\n" +
			"		Object o2 = new Object();\n" +
			"		Object potNonNull = b ? o2 : o0;\n" +
			"		Object potNull = b ? o1 : o0;\n" +
			"		for (int i = 0; i < 1; i++) {\n" +
			"			potNonNull.hashCode(); // OK\n" +
			"			potNull.hashCode(); // OK: pot null, is reported\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"",
		},
		"----------\n" +
		"1. ERROR in test\\PotNullInLoop.java (at line 13)\n" +
		"	potNull.hashCode(); // ERROR 1: pot null, but nothing reported\n" +
		"	^^^^^^^\n" +
		"Potential null pointer access: The variable potNull may be null at this location\n" +
		"----------\n" +
		"2. ERROR in test\\PotNullInLoop.java (at line 24)\n" +
		"	potNull.hashCode(); // ERROR 3 : pot null, but nothing reported\n" +
		"	^^^^^^^\n" +
		"Potential null pointer access: The variable potNull may be null at this location\n" +
		"----------\n" +
		"3. ERROR in test\\PotNullInLoop.java (at line 36)\n" +
		"	potNull.hashCode(); // OK: pot null, is reported\n" +
		"	^^^^^^^\n" +
		"Potential null pointer access: The variable potNull may be null at this location\n" +
		"----------\n" +
		"4. ERROR in test\\PotNullInLoop.java (at line 47)\n" +
		"	potNull.hashCode(); // OK: pot null, is reported\n" +
		"	^^^^^^^\n" +
		"Potential null pointer access: The variable potNull may be null at this location\n" +
		"----------\n" +
		"5. ERROR in test\\PotNullInLoop.java (at line 58)\n" +
		"	potNull.hashCode(); // OK: pot null, is reported\n" +
		"	^^^^^^^\n" +
		"Potential null pointer access: The variable potNull may be null at this location\n" +
		"----------\n"
	);
}
public void testBug447695() {
	runConformTest(
		new String[] {
		"test/Test447695.java",
			"package test;\n" +
			"\n" +
			"public class Test447695 {\n" +
			"	public static void f() {\n" +
			"		int[] array = null;\n" +
			"		(array = new int[1])[0] = 42;\n" +
			"	}\n" +
			"	public static int g() {\n" +
			"		int[] array = null;\n" +
			"		return (array = new int[1])[0];\n" +
			"	}\n" +
			"}\n"
		}
	);
}
public void testBug447695b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses foreach
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"public class X {\n" +
			"	void test(String[] ss) {\n" +
			"		List<String> strings = null;\n" +
			"		for (String s : (strings = Arrays.asList(ss)))\n" +
			"			System.out.println(s);\n" +
			"	}\n" +
			"}\n"
		});
}
public void testBug447695c() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses autoboxing
	runConformTest(
		new String[] {
			"test/Test447695.java",
			"package test;\n" +
			"\n" +
			"public class Test447695 {\n" +
			"	void f() {\n" +
			"		Integer l1 = null;\n" +
			"		Integer l2 = null;\n" +
			"		int b = (l1 = new Integer(2)) + (l2 = new Integer(1));\n" +
			"	}\n" +
			"}\n"
		}
	);
}
public void testBug447695d() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // uses reference expression
	runConformTest(
		new String[] {
			"test/Test447695.java",
			"package test;\n" +
			"\n" +
			"import java.util.function.Supplier;\n" +
			"\n" +
			"public class Test447695 {\n" +
			"	void f() {\n" +
			"		String s = null;\n" +
			"		Supplier<String> l = (s = \"\")::toString;\n" +
			"	}\n" +
			"}\n"
		}
	);
}
public void testBug447695e() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses autoboxing
	runConformTest(
		new String[] {
			"test/Test447695.java",
			"package test;\n" +
			"\n" +
			"public class Test447695 {\n" +
			"	void f() {\n" +
			"		Integer i = null;\n" +
			"		int j = -(i = new Integer(1));\n" +
			"		Boolean b1 = null;\n" +
			"		boolean b = !(b1 = new Boolean(false));\n" +
			"	}\n" +
			"}\n"
		}
	);
}
public void testBug447695f() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses autoboxing
	runConformTest(
		new String[] {
			"test/Test447695.java",
			"package test;\n" +
			"\n" +
			"public class Test447695 {\n" +
			"	void f() {\n" +
			"		int i = 0;\n" +
			"		Integer i1 = null;\n" +
			"		Integer i2 = null;\n" +
			"		Integer i3 = null;\n" +
			"		int j = (i1 = new Integer(1)) \n" +
			"				+ (i2 = new Integer(1)) \n" +
			"				+ i + i + i + i + i + i + i + i + i + i + i + i + i + i + i + i + i + i + i \n" +
			"				+ (i3 = new Integer(2)) + i;\n" +
			"	}\n" +
			"}\n"
		}
	);
}
public void testBug447695g() {
	runNegativeNullTest(
		new String[] {
			"test/Test447695.java",
			"package test;\n" +
			"\n" +
			"class X {\n" +
			"	int i;\n" +
			"}\n" +
			"\n" +
			"public class Test447695 {\n" +
			"	void f() {\n" +
			"		X x1 = null;\n" +
			"		X x2 = null;\n" +
			"		X x3 = null;\n" +
			"		X x4 = null;\n" +
			"		X x5 = null;\n" +
			"		X x6 = null;\n" +
			"		X x7 = null;\n" +
			"		X x8 = null;\n" +
			"		X x9 = null;\n" +
			"		X x10 = null;\n" +
			"		X x11 = null;\n" +
			"		x1.i = 1; // error 1 expected\n" +
			"		x2.i += 1; // error 2 expected\n" +
			"		(x3).i = 1; // error 3 expected\n" +
			"		(x4).i += 1; // error 4 expected\n" +
			"		(x5 = new X()).i = (x6 = new X()).i;\n" +
			"		(x7 = new X()).i += (x8 = new X()).i;\n" +
			"		int i1 = x9.i; // error 5 expected\n" +
			"		int i2 = (x10).i; // error 6 expected\n" +
			"		int i3 = (x11 = new X()).i;\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in test\\Test447695.java (at line 20)\n" +
		"	x1.i = 1; // error 1 expected\n" +
		"	^^\n" +
		"Null pointer access: The variable x1 can only be null at this location\n" +
		"----------\n" +
		"2. ERROR in test\\Test447695.java (at line 21)\n" +
		"	x2.i += 1; // error 2 expected\n" +
		"	^^\n" +
		"Null pointer access: The variable x2 can only be null at this location\n" +
		"----------\n" +
		"3. ERROR in test\\Test447695.java (at line 22)\n" +
		"	(x3).i = 1; // error 3 expected\n" +
		"	^^^^\n" +
		"Null pointer access: The variable x3 can only be null at this location\n" +
		"----------\n" +
		"4. ERROR in test\\Test447695.java (at line 23)\n" +
		"	(x4).i += 1; // error 4 expected\n" +
		"	^^^^\n" +
		"Null pointer access: The variable x4 can only be null at this location\n" +
		"----------\n" +
		"5. ERROR in test\\Test447695.java (at line 26)\n" +
		"	int i1 = x9.i; // error 5 expected\n" +
		"	         ^^\n" +
		"Null pointer access: The variable x9 can only be null at this location\n" +
		"----------\n" +
		"6. ERROR in test\\Test447695.java (at line 27)\n" +
		"	int i2 = (x10).i; // error 6 expected\n" +
		"	         ^^^^^\n" +
		"Null pointer access: The variable x10 can only be null at this location\n" +
		"----------\n"
	);
}
public void testBug509188() {
	runConformTest(
		new String[] {
			"test/Bug509188.java",
			"package test;\n" +
			"\n" +
			"public class Bug509188 {\n" +
			"	public static class QuinamidCell {\n" +
			"		public QuinamidCell next;\n" +
			"	}\n" +
			"\n" +
			"	public static void drawBoardElements() {\n" +
			"		Object _a, _b, _c, _d, _e, _f, _g, _h, _i, _j, _k, _l, _m, _n, _o, _p, _q, _r, _s, _t, _u, _v, _w, _x, _y, _z,\n" +
			"				_a1, _b1, _c1, _d1, _e1, _f1, _g1, _h1, _i1, _j1, _k1, _l1, _m1, _n1, _o1, _p1, _q1, _r1, _s1, _t1, _u1,\n" +
			"				_v1, _w1, _x1, _y1, _z_1, _a2, _b2, _c2, _d2, _e2, _f2, _g2, _h2, _i2, _j2, _k2;\n" +
			"\n" +
			"		QuinamidCell hitCell = null;\n" +
			"\n" +
			"		int level = 0; while (level < 1) {\n" +
			"			for (QuinamidCell c = new QuinamidCell(); c != null; c = c.next) {\n" +
			"				hitCell = c;\n" +
			"			} level++;\n" +
			"		}\n" +
			"		if (hitCell != null) {\n" +
			"			System.out.println(\"not dead\");\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		drawBoardElements();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		"not dead"
	);
}
public void testBug536408() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return; // uses auto unboxing
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"        Long s1 = null;\n" +
			"        long t = 0;\n" +
			"        t += s1;\n" +
			"		 Long s2 = t > 0 ? 1l : null;\n" +
			"		 t += s2;\n" +
			"    }\n" +
			"}\n"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	t += s1;\n" +
		"	     ^^\n" +
		"Null pointer access: This expression of type Long is null but requires auto-unboxing\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	t += s2;\n" +
		"	     ^^\n" +
		"Potential null pointer access: This expression of type Long may be null but requires auto-unboxing\n" +
		"----------\n";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
public void testBug542707_1() {
	if (!checkPreviewAllowed()) return; // switch expression
	Runner runner = new Runner();
	runner.customOptions = new HashMap<>();
	runner.customOptions.put(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
	runner.customOptions.put(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
	runner.testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void test(int i) {\n" +
		"		String s = switch (i) {\n" +
		"			case 1 -> \"one\";\n" +
		"			default -> null;\n" +
		"		};\n" +
		"		System.out.println(s.toLowerCase());\n" +
		"	}\n" +
		"}\n"
	};
	runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	System.out.println(s.toLowerCase());\n" +
			"	                   ^\n" +
			"Potential null pointer access: The variable s may be null at this location\n" +
			"----------\n";
	runner.runNegativeTest();
}
public void testBug544872() {
	runNegativeNullTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"    static void f(String string) {\n" +
			"        if (string != null)\n" +
			"            string.hashCode();\n" +
			"        synchronized (string) {\n" +
			"            string.hashCode();\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			""
		},
		"----------\n" +
		"1. ERROR in Test.java (at line 5)\n" +
		"	synchronized (string) {\n" +
		"	              ^^^^^^\n" +
		"Potential null pointer access: The variable string may be null at this location\n" +
		"----------\n"
	);
}
public void testBug551012() {
	runNegativeNullTest(
		new String[] {
			"NullConstants.java",
			"public class NullConstants {\n" +
			"	protected static final String FOO = null;\n" +
			"\n" +
			"	protected String foo = FOO;\n" +
			"\n" +
			"	protected static final String BAR = \"\";\n" +
			"\n" +
			"	protected String bar = BAR;\n" +
			"\n" +
			"	public boolean notAProblemButWhyNot() {\n" +
			"		return FOO == null ? foo != null : !FOO.equals(foo);\n" +
			"	}\n" +
			"\n" +
			"	public boolean alsoNotAProblemButThisWillAlwaysNPE() {\n" +
			"		return FOO != null ? foo != null : !FOO.equals(foo);\n" +
			"	}\n" +
			"\n" +
			"	public boolean aProblemButHowToAvoid() {\n" +
			"		return BAR == null ? bar != null : !BAR.equals(bar);\n" +
			"	}\n" +
			"\n" +
			"	public boolean wrongpProblemMessage() {\n" +
			"		return BAR != null ? !BAR.equals(bar) : bar != null;\n" +
			"	}\n" +
			"\n" +
			"	public boolean howAboutThis() {\n" +
			"		return bar == null ? BAR != null : bar.equals(BAR);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in NullConstants.java (at line 19)\n" +
		"	return BAR == null ? bar != null : !BAR.equals(bar);\n" +
		"	       ^^^\n" +
		"Null comparison always yields false: The field BAR is a nonnull constant\n" +
		"----------\n" +
		"2. ERROR in NullConstants.java (at line 23)\n" +
		"	return BAR != null ? !BAR.equals(bar) : bar != null;\n" +
		"	       ^^^\n" +
		"Redundant null check: The field BAR is a nonnull constant\n" +
		"----------\n" +
		"3. ERROR in NullConstants.java (at line 27)\n" +
		"	return bar == null ? BAR != null : bar.equals(BAR);\n" +
		"	                     ^^^\n" +
		"Redundant null check: The field BAR is a nonnull constant\n" +
		"----------\n");
}
public void testBug561280() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses generics
	runConformTest(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"\n" +
			"import java.util.List;\n" +
			"import java.util.Set;\n" +
			"\n" +
			"public class Test\n" +
			"{\n" +
			"  protected static final String ERROR_TYPE = \"error\";\n" +
			"  protected static final String OBJECT_TYPE = \"object\";\n" +
			"  protected static final String UNKNOWN_FEATURE_TYPE = \"unknownFeature\";\n" +
			"  protected static final String DOCUMENT_ROOT_TYPE = \"documentRoot\";\n" +
			"\n" +
			"  protected final static String TYPE_ATTRIB = \"\";\n" +
			"  protected final static String NIL_ATTRIB = \"\";\n" +
			"  protected final static String SCHEMA_LOCATION_ATTRIB = \"\";\n" +
			"  protected final static String NO_NAMESPACE_SCHEMA_LOCATION_ATTRIB = \"\";\n" +
			"\n" +
			"  protected final static boolean DEBUG_DEMANDED_PACKAGES = false;\n" +
			"\n" +
			"\n" +
			"  protected Object xmlResource;\n" +
			"  protected Object helper;\n" +
			"  protected Object elements;\n" +
			"  protected Object objects;\n" +
			"  protected Object types;\n" +
			"  protected Object mixedTargets;\n" +
			"  protected Object prefixesToFactories;\n" +
			"  protected Object urisToLocations;\n" +
			"  protected Object externalURIToLocations;\n" +
			"  protected boolean processSchemaLocations;\n" +
			"  protected Object extent;\n" +
			"  protected Object deferredExtent;\n" +
			"  protected Object resourceSet;\n" +
			"  protected Object packageRegistry;\n" +
			"  protected Object resourceURI;\n" +
			"  protected boolean resolve;\n" +
			"  protected boolean oldStyleProxyURIs;\n" +
			"  protected boolean disableNotify;\n" +
			"  protected StringBuffer text;\n" +
			"  protected boolean isIDREF;\n" +
			"  protected boolean isSimpleFeature;\n" +
			"  protected Object sameDocumentProxies;\n" +
			"  protected Object[] identifiers;\n" +
			"  protected int[] positions;\n" +
			"  protected static final int ARRAY_SIZE = 64;\n" +
			"  protected static final int REFERENCE_THRESHOLD = 5;\n" +
			"  protected int capacity;\n" +
			"  protected Set<String> notFeatures;\n" +
			"  protected String idAttribute;\n" +
			"  protected String hrefAttribute;\n" +
			"  protected Object xmlMap;\n" +
			"  protected Object extendedMetaData;\n" +
			"  protected Object anyType;\n" +
			"  protected Object anySimpleType;\n" +
			"  protected boolean recordUnknownFeature;\n" +
			"  protected boolean useNewMethods;\n" +
			"  protected boolean recordAnyTypeNSDecls;\n" +
			"  protected Object eObjectToExtensionMap;\n" +
			"  protected Object contextFeature;\n" +
			"  protected Object xmlSchemaTypePackage = null;\n" +
			"  protected boolean deferIDREFResolution;\n" +
			"  protected boolean processAnyXML;\n" +
			"  protected Object ecoreBuilder;\n" +
			"  protected boolean isRoot;\n" +
			"  protected Object locator;\n" +
			"  protected Object attribs;\n" +
			"  protected boolean useConfigurationCache;\n" +
			"  protected boolean needsPushContext;\n" +
			"  protected Object resourceEntityHandler;\n" +
			"  protected Object uriHandler;\n" +
			"  protected Object documentRoot;\n" +
			"  protected boolean usedNullNamespacePackage;\n" +
			"  protected boolean isNamespaceAware;\n" +
			"  protected boolean suppressDocumentRoot;\n" +
			"  protected boolean laxWildcardProcessing;\n" +
			"\n" +
			"  protected static void processObjectx(Object object)\n" +
			"  {\n" +
			"    if (object instanceof List)\n" +
			"    {\n" +
			"      List<?> list = ((List<?>)object);\n" +
			"      list.size();\n" +
			"    }\n" +
			"\n" +
			"    if (object != null)\n" +
			"    {\n" +
			"      object.hashCode();\n" +
			"    }\n" +
			"    else\n" +
			"    {\n" +
			"      System.err.println(\"#\");\n" +
			"    }\n" +
			"  }\n" +
			"}\n"
		});
}
public void testBug380786() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return; // uses foreach
	runNegativeTest(
		new String[] {
			"PNA.java",
			"public class PNA {\n" +
			"  void missedPNA(String s) {\n" +
			"    if (s != null)\n" +
			"      s = \"1,2\";\n" +
			"    final String[] sa = s.split(\",\");\n" +
			"    for (final String ss : sa)\n" +
			"      System.out.println(ss);\n" +
			"  }\n" +
			"\n" +
			"  void detectedPNA(final String ps) {\n" +
			"    String s = ps;\n" +
			"    if (s != null)\n" +
			"      s = \"1,2\";\n" +
			"    final String[] sa = s.split(\",\");\n" +
			"    for (final String ss : sa)\n" +
			"      System.out.println(ss);\n" +
			"  }\n" +
			"  \n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in PNA.java (at line 5)\n" +
		"	final String[] sa = s.split(\",\");\n" +
		"	                    ^\n" +
		"Potential null pointer access: The variable s may be null at this location\n" +
		"----------\n" +
		"2. ERROR in PNA.java (at line 14)\n" +
		"	final String[] sa = s.split(\",\");\n" +
		"	                    ^\n" +
		"Potential null pointer access: The variable s may be null at this location\n" +
		"----------\n"
			);
}
}