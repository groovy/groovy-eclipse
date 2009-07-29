/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import junit.framework.Test;

/* See also NullReferenceImplTests for low level, implementation dependent 
 * tests. */
public class NullReferenceTest extends AbstractRegressionTest {

public NullReferenceTest(String name) { 
    super(name);
}

	// Static initializer to specify tests subset using TESTS_* static variables
  	// All specified tests which does not belong to the class are skipped...
  	// Only the highest compliance level is run; add the VM argument
  	// -Dcompliance=1.4 (for example) to lower it if needed
  	static {
//    	TESTS_NAMES = new String[] { "test011" };
//    	TESTS_NUMBERS = new int[] { 561 };   
//    	TESTS_NUMBERS = new int[] { 2999 };   
//    	TESTS_RANGE = new int[] { 2050, -1 }; 
//  	TESTS_RANGE = new int[] { 1, 2049 }; 
//  	TESTS_RANGE = new int[] { 449, 451 }; 
//    	TESTS_RANGE = new int[] { 900, 999 }; 
  	}

public static Test suite() {
    return buildAllCompliancesTestSuite(testClass());
}
  
public static Class testClass() {
    return NullReferenceTest.class;
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
    }
    return defaultOptions;
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
		"1. ERROR in X.java (at line 4)\n" + 
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
		"1. ERROR in X.java (at line 4)\n" + 
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
		"1. ERROR in X.java (at line 4)\n" + 
		"	o.toString();\n" + 
		"	^\n" + 
		"Null pointer access: The variable o can only be null at this location\n" + 
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- conditional expression
// TODO (maxime) fix - may consider simultaneous computation of expression null status
// this case is one of those which raise the need for the simultaneous calculation of
// the null status of an expression and the code analysis of the said expression; this
// case is simplistic: we need a value (here, potentially null), that is *not* carried
// by the current embodiment of the flow info; other cases are less trivial in which
// side effects on variables could introduce errors into after the facts evaluations;
// one possible trick would be to add a slot for this
// other path: use a tainted unknown expression status; does not seem to cope well 
// with o = (o ==  null ? new Object() : o)
// TODO (maxime) https://bugs.eclipse.org/bugs/show_bug.cgi?id=133125
public void _test0034_conditional_expression() {
	this.runNegativeTest(
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
		"1. ERROR in X.java (at line 4)\n" + 
		"	o.toString();\n" + 
		"	^\n" + 
		"The variable o may be null\n" + 
		"----------\n");
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
			"Null pointer access: The variable i can only be null at this location\n" +  
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
			"Null pointer access: The variable i can only be null at this location\n" +  
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
			"Null pointer access: The variable i can only be null at this location\n" + 
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
		"1. ERROR in X.java (at line 13)\n" + 
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
		"2. ERROR in X.java (at line 7)\n" + 
		"	o.toString();\n" + 
		"	^\n" + 
		"Potential null pointer access: The variable o may be null at this location\n" + 
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
			"      o.toString();\n" + // must complain anyway (could be quite distant from the if test)
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
		"2. ERROR in X.java (at line 8)\n" + 
		"	o.toString();\n" + 
		"	^\n" + 
		"Null pointer access: The variable o can only be null at this location\n" + 
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
// TODO (maxime) https://bugs.eclipse.org/bugs/show_bug.cgi?id=129581
// this is a limit of the fix for bug 128014 - calls for a nuance 
// between potential null and tainted null
public void _test0335_if_else() {
	this.runNegativeTest(
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
		"2. ERROR in X.java (at line 8)\n" + 
		"	o.toString();\n" + 
		"	^\n" + 
		"Null pointer access: The variable o can only be null at this location\n" + 
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
	this.runNegativeTest(
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
		"1. ERROR in X.java (at line 11)\r\n" + 
		"	while (o == null) {\r\n" + 
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
public void _test0470_while() {
	this.runConformTest(
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
		"ERROR: o cannot be null on first if");
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
// The whole issue here is whether or not to detect premature exits. We
// follow JLS's conservative approach, which considers that the try
// block may exit before the assignment is completed.
// Note: conversely, without line 1, we would complain about x not being 
//       initialized (for sure) on line 2.
public void test0505_try_finally() {
	this.runConformTest(
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
		"");
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
public void test0525_try_finally_unchecked_exception() {
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
		"1. ERROR in X.java (at line 13)\r\n" + 
		"	o.toString();\r\n" + 
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
	this.runConformTest(
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
		""
		// conservative flow analysis suppresses the warning
//		"----------\n" + 
//		"1. ERROR in X.java (at line 10)\n" + 
//		"	if (o != null) {\n" + 
//		"	    ^\n" + 
//		"Redundant null check: The variable o can only be null at this location\n" + 
//		"----------\n"
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
		"Potential null pointer access: The variable o may be null at this location\n" + 
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
		"Potential null pointer access: The variable o may be null at this location\n" + 
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
//		"Null pointer access: The variable o can only be null at this location\n" + 
		"Potential null pointer access: The variable o may be null at this location\n" +
		// conservative flow analysis softens the error
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
//		"Null pointer access: The variable o can only be null at this location\n" + 
		"Potential null pointer access: The variable o may be null at this location\n" +
		// conservative flow analysis softens the error
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
	this.runNegativeTest(
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
		"----------\n" + 
		"1. ERROR in X.java (at line 8)\n" + 
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
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - try/catch
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=150854
public void test0564_try_catch_unchecked_exception() {
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
		"");
}

// null analysis - try/catch
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=150854
// variant
public void test0565_try_catch_unchecked_exception() {
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
		"");
}

// null analysis - try/catch
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=150854
// variant
public void test0566_try_catch_unchecked_exception() {
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
		"");
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
	this.runNegativeTest(
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
public void _test0746_for_try_catch() {
	runTest(
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
		true /* expectingCompilerErrors */,
		"----------\n" + 
		"1. ERROR in X.java (at line 10)\n" + 
		"	str.charAt(i);\n" + 
		"	^^^\n" + 
		"Potential null pointer access: The variable str may be null at this location\n" + 
		"----------\n" /* expectedCompilerLog */,
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
			"2. ERROR in X.java (at line 5)\n" + 
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
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"  void foo(Object o) {\n" + 
				"    assert(false && o != null);\n" +
				"    if (o == null) { };\n" + 		// quiet
				"  }\n" + 
				"}\n"},
		"");
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
		"3. ERROR in X.java (at line 6)\n" + 
		"	x.foo(null); // 3\n" + 
		"	^\n" + 
		"Null pointer access: The variable x can only be null at this location\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 9)\n" + 
		"	} else if (x != null) { // 6\n" + 
		"	           ^\n" + 
		"Redundant null check: The variable x cannot be null at this location\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 12)\n" + 
		"	x.foo(null); // 8\n" + 
		"	^\n" + 
		"Potential null pointer access: The variable x may be null at this location\n" + 
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
		"2. ERROR in X.java (at line 5)\n" + 
		"	x.foo(this);\n" + 
		"	^\n" + 
		"Null pointer access: The variable x can only be null at this location\n" + 
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test1014() {
	this.runNegativeTest(
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
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	x.foo(null);\n" + 
		"	^\n" + 
		"Potential null pointer access: The variable x may be null at this location\n" + 
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
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
		"1. ERROR in X.java (at line 8)\n" + 
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
		"1. ERROR in X.java (at line 6)\r\n" + 
		"	if (o != null) return;\r\n" + 
		"	    ^\n" + 
		"Null comparison always yields false: The variable o can only be null at this location\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 7)\r\n" + 
		"	o = null;\r\n" + 
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
			"  void foo() {\n" + 
			"    Object o = new Object();\n" + 
			"    while (this != null) {\n" + 
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
			"      } finally {\n" + 
			"        if (o == null) \n" + 
			"          o = new Object();\n" + 
			"        }\n" + 
			"      }\n" + 
			"    if (o == null) return;\n" + 
			"  }\n" + 
			"}\n"},
		"");
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
		"1. ERROR in X.java (at line 7)\n" + 
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
		"1. ERROR in X.java (at line 7)\n" + 
		"	if (a == null) {\n" + 
		"	    ^\n" + 
		"Redundant null check: The variable a can only be null at this location\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 13)\n" + 
		"	if (a == null) {\n" + 
		"	    ^\n" + 
		"Null comparison always yields false: The variable a cannot be null at this location\n" + 
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
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// from AssignmentTest#test034, simplified
public void test1034() {
	this.runConformTest(
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
		"");
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
			"1. WARNING in X.java (at line 8)\n" + 
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
		"1. ERROR in X.java (at line 4)\r\n" + 
		"	if (o != null) {\r\n" + 
		"	    ^\n" + 
		"Null comparison always yields false: The variable o can only be null at this location\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 8)\r\n" + 
		"	o.toString();\r\n" + 
		"	^\n" + 
		"Null pointer access: The variable o can only be null at this location\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 9)\r\n" + 
		"	p.toString();\r\n" + 
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
		"1. ERROR in X.java (at line 8)\r\n" + 
		"	o.toString();\r\n" + 
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
		"1. ERROR in X.java (at line 4)\r\n" + 
		"	if (o != null) {\r\n" + 
		"	    ^\n" + 
		"Null comparison always yields false: The variable o can only be null at this location\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 8)\r\n" + 
		"	o.toString();\r\n" + 
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
		"1. ERROR in X.java (at line 4)\r\n" + 
		"	if (o != null) {\r\n" + 
		"	    ^\n" + 
		"Null comparison always yields false: The variable o can only be null at this location\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 9)\r\n" + 
		"	p.toString();\r\n" + 
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
			"1. ERROR in X.java (at line 9)\r\n" + 
			"	o.toString();\r\n" + 
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
		"1. ERROR in X.java (at line 12)\n" + 
		"	class Inner extends X {\n" + 
		"	      ^^^^^\n" + 
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
		"1. ERROR in X.java (at line 26)\n" + 
		"	class Inner extends X {\n" + 
		"	      ^^^^^\n" + 
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
}