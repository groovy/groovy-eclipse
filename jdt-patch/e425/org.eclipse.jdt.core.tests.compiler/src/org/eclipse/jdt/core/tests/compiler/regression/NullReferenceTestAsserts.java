/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *		IBM Corporation - initial API and implementation
 *		Stephan Herrmann - Contribution for
 *								bug 382069 - [null] Make the null analysis consider JUnit's assertNotNull similarly to assertions
 *								Bug 454031 - [compiler][null][loop] bug in null analysis; wrong "dead code" detection
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
@SuppressWarnings({ "unchecked", "rawtypes" })
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
//		TESTS_NAMES = new String[] { "testBug382069" };
//		TESTS_NUMBERS = new int[] { 561 };
//		TESTS_RANGE = new int[] { 1, 2049 };
}

static final String JUNIT_ASSERT_NAME = "junit/framework/Assert.java";
static final String JUNIT_ASSERT_CONTENT = "package junit.framework;\n" +
		"public class Assert {\n" +
		"    static public void assertNull(Object object) {}\n" +
		"    static public void assertNull(String message, Object object) {}\n" +
		"    static public void assertNotNull(Object object) {}\n" +
		"    static public void assertNotNull(String message, Object object) {}\n" +
		"    static public void assertTrue(boolean expression) {}\n" +
		"    static public void assertTrue(String message, boolean expression) {}\n" +
		"    static public void assertFalse(boolean expression) {}\n" +
		"    static public void assertFalse(String message, boolean expression) {}\n" +
		"}\n";

static final String ORG_JUNIT_ASSERT_NAME = "org/junit/Assert.java";
static final String ORG_JUNIT_ASSERT_CONTENT = "package org.junit;\n" +
		"public class Assert {\n" +
		"    static public void assertNull(Object object) {}\n" +
		"    static public void assertNull(String message, Object object) {}\n" +
		"    static public void assertNotNull(Object object) {}\n" +
		"    static public void assertNotNull(String message, Object object) {}\n" +
		"    static public void assertTrue(boolean expression) {}\n" +
		"    static public void assertTrue(String message, boolean expression) {}\n" +
		"    static public void assertFalse(boolean expression) {}\n" +
		"    static public void assertFalse(String message, boolean expression) {}\n" +
		"}\n";
static final String ORG_JUNIT_JUPITER_API_ASSERTIONS_NAME = "org/junit/jupiter/api/Assertions.java";
static final String ORG_JUNIT_JUPITER_API_ASSERTIONS_CONTENT = "package org.junit.jupiter.api;\n" +
		"import java.util.function.Supplier;\n" +
		"import java.util.function.BooleanSupplier;\n" +
		"public class Assertions {\n" +
		"    static public void assertNull(Object object) {}\n" +
		"    static public void assertNull(Object object, String message) {}\n" +
		"    static public void assertNull(Object object, Supplier<String> messageSupplier) {}\n" +
		"    static public void assertNotNull(Object object) {}\n" +
		"    static public void assertNotNull(Object object, String message) {}\n" +
		"    static public void assertNotNull(Object object, Supplier<String> messageSupplier) {}\n" +
		"    static public void assertTrue(boolean expression) {}\n" +
		"    static public void assertTrue(boolean expression, String message) {}\n" +
		"    static public void assertTrue(boolean expression, Supplier<String> messageSupplier) {}\n" +
		"    static public void assertTrue(BooleanSupplier booleanSupplier) {}\n" +
		"    static public void assertTrue(BooleanSupplier booleanSupplier, String message) {}\n" +
		"    static public void assertTrue(BooleanSupplier booleanSupplier, Supplier<String> messageSupplier) {}\n" +
		"    static public void assertFalse(boolean expression) {}\n" +
		"    static public void assertFalse(boolean expression, String message) {}\n" +
		"    static public void assertFalse(boolean expression, Supplier<String> messageSupplier) {}\n" +
		"    static public void assertFalse(BooleanSupplier booleanSupplier) {}\n" +
		"    static public void assertFalse(BooleanSupplier booleanSupplier, String message) {}\n" +
		"    static public void assertFalse(BooleanSupplier booleanSupplier, Supplier<String> messageSupplier) {}\n" +
		"}\n";

static final String APACHE_VALIDATE_NAME = "org/apache/commons/lang/Validate.java";
static final String APACHE_VALIDATE_CONTENT = "package org.apache.commons.lang;\n" +
		"public class Validate {\n" +
		"    static public void notNull(Object object) {}\n" +
		"    static public void notNull(Object object, String message) {}\n" +
		"    static public void isTrue(boolean expression) {}\n" +
		"    static public void isTrue(boolean expression, String message) {}\n" +
		"    static public void isTrue(boolean expression, String message, double value) {}\n" +
		"    static public void isTrue(boolean expression, String message, long value) {}\n" +
		"    static public void isTrue(boolean expression, String message, Object value) {}\n" +
		"}\n";

static final String APACHE_3_VALIDATE_NAME = "org/apache/commons/lang3/Validate.java";
static final String APACHE_3_VALIDATE_CONTENT = "package org.apache.commons.lang3;\n" +
		"public class Validate {\n" +
		"    static public <T> T notNull(T object) { return object; }\n" +
		"    static public <T> T notNull(T object, String message, Object... values) { return object; }\n" +
		"    static public void isTrue(boolean expression) {}\n" +
		"    static public void isTrue(boolean expression, String message, double value) {}\n" +
		"    static public void isTrue(boolean expression, String message, long value) {}\n" +
		"    static public void isTrue(boolean expression, String message, Object value) {}\n" +
		"}\n";

static final String GOOGLE_PRECONDITIONS_NAME = "com/google/common/base/Preconditions.java";
static final String GOOGLE_PRECONDITIONS_CONTENT = "package com.google.common.base;\n" +
		"public class Preconditions {\n" +
		"    static public <T> T checkNotNull(T object) { return object; }\n" +
		"    static public <T> T checkNotNull(T object, Object message) { return object; }\n" +
		"    static public <T> T checkNotNull(T object, String message, Object... values) { return object; }\n" +
		"    static public void checkArgument(boolean expression) {}\n" +
		"    static public void checkArgument(boolean expression, Object message) {}\n" +
		"    static public void checkArgument(boolean expression, String msgTmpl, Object... messageArgs) {}\n" +
		"    static public void checkState(boolean expression) {}\n" +
		"    static public void checkState(boolean expression, Object message) {}\n" +
		"    static public void checkState(boolean expression, String msgTmpl, Object... messageArgs) {}\n" +
		"}\n";

public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

public static Class testClass() {
	return NullReferenceTestAsserts.class;
}

@Override
protected void setUp() throws Exception {
	super.setUp();
	if (this.assertLib == null) {
		String[] defaultLibs = getDefaultClassPaths();
		int len = defaultLibs.length;
		this.assertLib = new String[len+1];
		System.arraycopy(defaultLibs, 0, this.assertLib, 0, len);
		File bundleFile = FileLocator.getBundleFileLocation(Platform.getBundle("org.eclipse.equinox.common")).get();
		if (bundleFile.isDirectory())
			this.assertLib[len] = bundleFile.getPath()+"/bin";
		else
			this.assertLib[len] = bundleFile.getPath();
	}
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

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
public void testBug127575a() throws IOException {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
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
			true,
			null);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
public void testBug127575b() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o) {\n" +
				"    org.eclipse.core.runtime.Assert.isLegal(o == null);\n" + 	// forces null
				"    o.toString();\n" + 		// can only be null
				"  }\n" +
				"}\n"};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n";
	runner.classLibraries =
		this.assertLib;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
public void testBug127575c() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o, boolean b) {\n" +
				"    org.eclipse.core.runtime.Assert.isLegal(o != null || b, \"FAIL\");\n" + // shed doubts
				"    o.toString();\n" + 		// complain
				"  }\n" +
				"}\n"};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n";
	runner.classLibraries =
	    this.assertLib;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
public void testBug127575d() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o1, Object o2) {\n" +
				"    org.eclipse.core.runtime.Assert.isLegal(o1 != null && o2 == null);\n" +
				"    if (o1 == null) { };\n" + 		// complain
				"    if (o2 == null) { };\n" + 		// complain
				"  }\n" +
				"}\n"};
	runner.expectedCompilerLog =
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
			"----------\n";
	runner.classLibraries =
		    this.assertLib;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
public void testBug127575e() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o) {\n" +
				"    org.eclipse.core.runtime.Assert.isLegal(false && o != null);\n" +
				"    if (o == null) { };\n" + 		// quiet
				"  }\n" +
				"}\n"};
	runner.expectedCompilerLog =
				"----------\n" +
				"1. WARNING in X.java (at line 3)\n" +
				"	org.eclipse.core.runtime.Assert.isLegal(false && o != null);\n" +
				"	                                                 ^^^^^^^^^\n" +
				"Dead code\n" +
				"----------\n";
	runner.classLibraries =
				this.assertLib;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runConformTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
public void testBug127575e_1() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o) {\n" +
				"	 o = null;\n" +
				"    org.eclipse.core.runtime.Assert.isLegal(false && o != null);\n" +
				"    if (o == null) { };\n" + 		// warn on o because o was null above.
				"  }\n" +
				"}\n"};
	runner.expectedCompilerLog =
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
				"----------\n";
	runner.classLibraries =
				this.assertLib;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
public void testBug127575e_2() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o) {\n" +
				"    org.eclipse.core.runtime.Assert.isLegal(true || o != null);\n" +
				"    if (o == null) { };\n" + 		// quiet
				"  }\n" +
				"}\n"};
	runner.expectedCompilerLog =
				"----------\n" +
				"1. WARNING in X.java (at line 3)\n" +
				"	org.eclipse.core.runtime.Assert.isLegal(true || o != null);\n" +
				"	                                                ^^^^^^^^^\n" +
				"Dead code\n" +
				"----------\n";
	runner.classLibraries =
				this.assertLib;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runConformTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
public void testBug127575f() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o) {\n" +
				"    org.eclipse.core.runtime.Assert.isLegal(false || o != null);\n" +
				"    if (o == null) { };\n" + 		// complain
				"  }\n" +
				"}\n"};
	runner.expectedCompilerLog =
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
		"----------\n";
	runner.classLibraries =
	    this.assertLib;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// do warn always false comparisons even inside org.eclipse.core.runtime.Assert.isLegal
public void testBug127575g() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	Runner runner = new Runner();
	runner.testFiles =
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    Object o = null;\n" +
				"    org.eclipse.core.runtime.Assert.isLegal(o != null);\n" +    // don't complain
				"    if (o == null) { };\n" +   // complain
				"  }\n" +
				"}\n"};
	runner.expectedCompilerLog =
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
		"----------\n";
	runner.classLibraries =
		this.assertLib;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// Test to verify that asserts are exempted from redundant null check warnings,
// but this doesn't affect the downstream info.
public void testBug127575h() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	Runner runner = new Runner();
	runner.testFiles =
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
				"}\n"};
	runner.expectedCompilerLog =
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
		"----------\n";
	runner.classLibraries =
	    this.assertLib;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// Test to verify that asserts are exempted from redundant null check warnings,
// but this doesn't affect the downstream info.
public void testBug127575i() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	Runner runner = new Runner();
	runner.testFiles =
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
				"}\n"};
	runner.expectedCompilerLog =
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
		"----------\n";
	runner.classLibraries =
	    this.assertLib;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// Test to verify that asserts are exempted from redundant null check warnings in a looping context,
// but this doesn't affect the downstream info.
public void testBug127575j() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	Runner runner = new Runner();
	runner.testFiles =
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
				"}\n"};
	runner.expectedCompilerLog =
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
		"----------\n";
	runner.classLibraries =
	    this.assertLib;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// Test to verify that asserts are exempted from redundant null check warnings in a finally context,
// but this doesn't affect the downstream info.
public void testBug127575k() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	Runner runner = new Runner();
	runner.testFiles =
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
				"}\n"};
	runner.expectedCompilerLog =
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
		"----------\n";
	runner.classLibraries =
	    this.assertLib;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// The condition of org.eclipse.core.runtime.Assert.isLegal is considered always true
// and alters the following analysis suitably.
public void testBug127575l() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	Runner runner = new Runner();
	runner.testFiles =
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
				"}\n"};
	runner.expectedCompilerLog =
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
			"----------\n";
	runner.classLibraries =
			this.assertLib;
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// NPE warnings should be given inside org.eclipse.core.runtime.Assert.isLegal too
public void testBug127575m() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Runner runner = new Runner();
		runner.testFiles =
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
				"}\n"};
		runner.expectedCompilerLog =
				"----------\n" +
			"1. ERROR in Info.java (at line 11)\n" +
			"	org.eclipse.core.runtime.Assert.isLegal(info.checkSomething());\n" +
			"	                                        ^^^^\n" +
			"Null pointer access: The variable info can only be null at this location\n" +
			"----------\n";
		runner.classLibraries =
			this.assertLib;
		runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
		runner.runNegativeTest();
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// always false comparison in Assert.isLegal in loop should be warned against
public void testBug127575n() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Runner runner = new Runner();
		runner.testFiles =
			new String[] {
		"DoWhileBug.java",
				"public class DoWhileBug {\n" +
				"	void test(boolean b1) {\n" +
				"		Object o1 = null;\n" +
				"		Object o2 = null;\n" +
				"		do {\n" +
				"           if (b1)\n" +
				"				o1 = null;\n" +
				"           org.eclipse.core.runtime.Assert.isLegal (o1 != null);\n" +
				"		} while (true);\n" +
				"	}\n" +
				"}"
			};
		runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in DoWhileBug.java (at line 7)\n" +
			"	o1 = null;\n" +
			"	^^\n" +
			"Redundant assignment: The variable o1 can only be null at this location\n" +
			"----------\n" +
			"2. ERROR in DoWhileBug.java (at line 8)\n" +
			"	org.eclipse.core.runtime.Assert.isLegal (o1 != null);\n" +
			"	                                         ^^\n" +
			"Null comparison always yields false: The variable o1 can only be null at this location\n" +
			"----------\n";
		runner.classLibraries =
			this.assertLib;
		runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
		runner.runNegativeTest();
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127575
// "redundant null check" in Assert.isLegal in loop should not be warned against
public void testBug127575o() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Runner runner = new Runner();
		runner.testFiles =
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
			};
		runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in DoWhileBug.java (at line 7)\n" +
			"	o1 = null;\n" +
			"	^^\n" +
			"Redundant assignment: The variable o1 can only be null at this location\n" +
			"----------\n";
		runner.classLibraries =
			this.assertLib;
		runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
		runner.runNegativeTest();
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=373953
public void testBug373953() throws IOException {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Runner runner = new Runner();
		runner.testFiles =
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
				"}\n"};
		runner.expectedCompilerLog =
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
			"----------\n";
		runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
		runner.runNegativeTest();
	}
}

// https://bugs.eclipse.org/382069 - [null] Make the null analysis consider JUnit's assertNotNull similarly to assertions
// junit's assertNotNull
public void testBug382069a() throws IOException {
	this.runConformTest(
		new String[] {
			JUNIT_ASSERT_NAME,
			JUNIT_ASSERT_CONTENT,
			"X.java",
			"public class X {\n" +
			"  void foo(Object o1, String o2) {\n" +
			"    boolean b = o1 != null;\n" + // sheds doubts upon o1
			"    junit.framework.Assert.assertNotNull(o1);\n" + 	// protection
			"    o1.toString();\n" + 		// quiet
			"    b = o2 != null;\n" + // sheds doubts upon o2
			"    junit.framework.Assert.assertNotNull(\"msg\", o2);\n" + 	// protection
			"    o2.toString();\n" + 		// quiet
			"  }\n" +
			"}\n"},
		"");
}

// https://bugs.eclipse.org/382069 - [null] Make the null analysis consider JUnit's assertNotNull similarly to assertions
// org.eclipse.core.runtime.Assert.isNotNull
public void testBug382069b() {
	if (RUN_JAVAC && this.complianceLevel < ClassFileConstants.JDK11)
		return; // Assert has class file format JDK 11
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runConformTest(
			new String[] {
		"X.java",
				"public class X {\n" +
				"  void foo(Object o1, String o2) {\n" +
				"    boolean b = o1 != null;\n" + // sheds doubts upon o1
				"    org.eclipse.core.runtime.Assert.isNotNull(o1);\n" + 	// protection
				"    o1.toString();\n" + 		// quiet
				"    b = o2 != null;\n" + // sheds doubts upon o2
				"    org.eclipse.core.runtime.Assert.isNotNull(o2, \"msg\");\n" + 	// protection
				"    o2.toString();\n" + 		// quiet
				"  }\n" +
				"}"
			},
			"",
			this.assertLib,
			true,
			null);
	}
}

// https://bugs.eclipse.org/382069 - [null] Make the null analysis consider JUnit's assertNotNull similarly to assertions
// junit's assertNull and dead code analysis
public void testBug382069c() throws IOException {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			JUNIT_ASSERT_NAME,
			JUNIT_ASSERT_CONTENT,
			"X.java",
			"public class X {\n" +
			"  boolean foo(String o1, String o2) {\n" +
			"    junit.framework.Assert.assertNull(\"something's wrong\", o1);\n" + 	// establish nullness
			"    if (o2 == null)\n" +
			"        return o1 != null;\n" +
			"    junit.framework.Assert.assertNull(o2);\n" + // will always throw
			"    return false; // dead code\n" +
			"  }\n" +
			"  void bar(X x) {\n" +
			"    if (x == null) {\n" +
			"      junit.framework.Assert.assertNotNull(x);\n" +
			"      return; // dead code\n" +
			"    }\n" +
			"  }\n" +
			"}\n"};
	runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	return o1 != null;\n" +
			"	       ^^\n" +
			"Null comparison always yields false: The variable o1 can only be null at this location\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 7)\n" +
			"	return false; // dead code\n" +
			"	^^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 12)\n" +
			"	return; // dead code\n" +
			"	^^^^^^^\n" +
			"Dead code\n" +
			"----------\n";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
// https://bugs.eclipse.org/382069 - [null] Make the null analysis consider JUnit's assertNotNull similarly to assertions
// various asserts from org.apache.commons.lang.Validate
public void testBug382069d() throws IOException {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			APACHE_VALIDATE_NAME,
			APACHE_VALIDATE_CONTENT,
			"X.java",
			"import org.apache.commons.lang.Validate;\n" +
			"public class X {\n" +
			"  void foo(Object o1, String o2, X x) {\n" +
			"    boolean b = o1 != null;\n" + // sheds doubts upon o1
			"    Validate.notNull(o1);\n" + 	// protection
			"    o1.toString();\n" + 		// quiet
			"    b = o2 != null;\n" + // sheds doubts upon o2
			"    Validate.notNull(o2, \"msg\");\n" + 	// protection
			"    o2.toString();\n" + 		// quiet
			"    Validate.isTrue(x == null, \"ups\", x);\n" +
			"    x.foo(null, null, null); // definite NPE\n" +
			"  }\n" +
			"}\n"};
	runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in X.java (at line 11)\n" +
			"	x.foo(null, null, null); // definite NPE\n" +
			"	^\n" +
			"Null pointer access: The variable x can only be null at this location\n" +
			"----------\n";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
// https://bugs.eclipse.org/382069 - [null] Make the null analysis consider JUnit's assertNotNull similarly to assertions
// various asserts from org.apache.commons.lang3Validate
public void testBug382069e() throws IOException {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Runner runner = new Runner();
		runner.testFiles =
			new String[] {
				APACHE_3_VALIDATE_NAME,
				APACHE_3_VALIDATE_CONTENT,
				"X.java",
				"import org.apache.commons.lang3.Validate;\n" +
				"public class X {\n" +
				"  void foo(Object o1, String o2, X x) {\n" +
				"    boolean b = o1 != null;\n" + // sheds doubts upon o1
				"    Validate.notNull(o1);\n" + 	// protection
				"    o1.toString();\n" + 		// quiet
				"    b = o2 != null;\n" + // sheds doubts upon o2
				"    Validate.notNull(o2, \"msg\");\n" + 	// protection
				"    o2.toString();\n" + 		// quiet
				"    Validate.isTrue(x == null, \"ups\", x);\n" +
				"    x.foo(null, null, null); // definite NPE\n" +
				"  }\n" +
				"}\n"};
		runner.expectedCompilerLog =
				"----------\n" +
				"1. ERROR in X.java (at line 11)\n" +
				"	x.foo(null, null, null); // definite NPE\n" +
				"	^\n" +
				"Null pointer access: The variable x can only be null at this location\n" +
				"----------\n";
		runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
		runner.runNegativeTest();
	}
}
// https://bugs.eclipse.org/382069 - [null] Make the null analysis consider JUnit's assertNotNull similarly to assertions
// various asserts from com.google.common.base.Preconditions
public void testBug382069f() throws IOException {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Runner runner = new Runner();
		runner.testFiles =
			new String[] {
				GOOGLE_PRECONDITIONS_NAME,
				GOOGLE_PRECONDITIONS_CONTENT,
				"X.java",
				"import com.google.common.base.Preconditions;\n" +
				"public class X {\n" +
				"  void foo(Object o1, String o2, X x) {\n" +
				"    boolean b = o1 != null;\n" + // sheds doubts upon o1
				"    Preconditions.checkNotNull(o1);\n" + 	// protection
				"    o1.toString();\n" + 		// quiet
				"    b = o2 != null;\n" + // sheds doubts upon o2
				"    Preconditions.checkNotNull(o2, \"msg {0}.{1}\", o1, o2);\n" + 	// protection
				"    o2.toString();\n" + 		// quiet
				"    Preconditions.checkArgument(x == null, \"ups\");\n" +
				"    x.foo(null, null, null); // definite NPE\n" +
				"  }\n" +
				"}\n"};
		runner.expectedCompilerLog =
				"----------\n" +
				"1. ERROR in X.java (at line 11)\n" +
				"	x.foo(null, null, null); // definite NPE\n" +
				"	^\n" +
				"Null pointer access: The variable x can only be null at this location\n" +
				"----------\n";
		runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
		runner.runNegativeTest();
	}
}
// https://bugs.eclipse.org/382069 - [null] Make the null analysis consider JUnit's assertNotNull similarly to assertions
// java.util.Objects#requireNonNull
public void testBug382069g() throws IOException {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
		this.runConformTest(
			new String[] {
				"X.java",
				"import static java.util.Objects.requireNonNull;\n" +
				"public class X {\n" +
				"  void foo(Object o1, String o2, X x) {\n" +
				"    boolean b = o1 != null;\n" + // sheds doubts upon o1
				"    requireNonNull(o1);\n" + 	// protection
				"    o1.toString();\n" + 		// quiet
				"    b = o2 != null;\n" + // sheds doubts upon o2
				"    requireNonNull(o2, \"msg\");\n" + 	// protection
				"    o2.toString();\n" + 		// quiet
				"  }\n" +
				"}\n"},
				"");
	}
}

// https://bugs.eclipse.org/382069 - [null] Make the null analysis consider JUnit's assertNotNull similarly to assertions
// junit's assertTrue / assertFalse
public void testBug382069h() throws IOException {
	this.runConformTest(
		new String[] {
			JUNIT_ASSERT_NAME,
			JUNIT_ASSERT_CONTENT,
			"X.java",
			"public class X {\n" +
			"  void foo(Object o1, String o2) {\n" +
			"    boolean b = o1 != null;\n" + // sheds doubts upon o1
			"    junit.framework.Assert.assertTrue(o1 != null);\n" + 	// protection
			"    o1.toString();\n" + 		// quiet
			"    b = o2 != null;\n" + // sheds doubts upon o2
			"    junit.framework.Assert.assertFalse(\"msg\", o2 == null);\n" + 	// protection
			"    o2.toString();\n" + 		// quiet
			"  }\n" +
			"}\n"},
		"");
}
// Bug 401159 - [null] Respect org.junit.Assert for control flow
// various asserts from org.junit.Assert
public void testBug401159() throws IOException {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			ORG_JUNIT_ASSERT_NAME,
			ORG_JUNIT_ASSERT_CONTENT,
			"X.java",
			"import org.junit.Assert;\n" +
			"public class X {\n" +
			"  void foo(Object o1, String o2, X x) {\n" +
			"    boolean b = o1 != null;\n" + // sheds doubts upon o1
			"    Assert.assertNotNull(o1);\n" + 	// protection
			"    o1.toString();\n" + 		// quiet
			"    b = o2 != null;\n" + // sheds doubts upon o2
			"    Assert.assertNotNull(\"msg\", o2);\n" + 	// protection
			"    o2.toString();\n" + 		// quiet
			"    Assert.assertTrue(\"ups\", x == null);\n" +
			"    x.foo(null, null, null); // definite NPE\n" +
			"  }\n" +
			"}\n"};
	runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in X.java (at line 11)\n" +
			"	x.foo(null, null, null); // definite NPE\n" +
			"	^\n" +
			"Null pointer access: The variable x can only be null at this location\n" +
			"----------\n";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
}

// https://bugs.eclipse.org/472618 - [compiler][null] assertNotNull vs. Assert.assertNotNull
// junit's assertNotNull
public void testBug472618() throws IOException {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses auto-unboxing
	this.runConformTest(
		new String[] {
			JUNIT_ASSERT_NAME,
			JUNIT_ASSERT_CONTENT,
			"AssertionTest.java",
			"import junit.framework.Assert;\n" +
			"\n" +
			"public class AssertionTest extends Assert\n" +
			"{\n" +
			"    void test()\n" +
			"    {\n" +
			"        Long test = null;\n" +
			"\n" +
			"        if(Boolean.TRUE)\n" +
			"        {\n" +
			"            test = 0L;\n" +
			"        }\n" +
			"\n" +
			"        assertNotNull(test);\n" +
			"\n" +
			"        test.longValue();  // <- potential null pointer access\n" +
			"    }\n" +
			"}\n"},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=568542
// junit 5's assertNotNull
public void testBug568542a() throws IOException {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // uses Supplier
	this.runConformTest(
		new String[] {
			ORG_JUNIT_JUPITER_API_ASSERTIONS_NAME,
			ORG_JUNIT_JUPITER_API_ASSERTIONS_CONTENT,
			"X.java",
			"import static org.junit.jupiter.api.Assertions.assertNotNull;\n" +
			"public class X {\n" +
			"    void test(Long test1, Long test2, Long test3) {\n" +
			"        boolean b = (test1 != null | test2 != null | test3 != null);\n" +
			"        assertNotNull(test1);\n" +
			"        test1.longValue();\n" +
			"        assertNotNull(test2, \"message\");\n" +
			"        test2.longValue();\n" +
			"        assertNotNull(test3, () -> \"message\");\n" +
			"        test3.longValue();\n" +
			"    }\n" +
			"}\n"},
		"");
}
// junit 5's assertNull
public void testBug568542b() throws IOException {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // uses Supplier
	runNegativeTest(
		new String[] {
			ORG_JUNIT_JUPITER_API_ASSERTIONS_NAME,
			ORG_JUNIT_JUPITER_API_ASSERTIONS_CONTENT,
			"X.java",
			"import static org.junit.jupiter.api.Assertions.assertNull;\n" +
			"public class X {\n" +
			"    void test(Long test1, Long test2, Long test3) {\n" +
			"        boolean b = (test1 != null | test2 != null | test3 != null);\n" +
			"        assertNull(test1);\n" +
			"        test1.longValue();\n" +
			"        assertNull(test2, \"message\");\n" +
			"        test2.longValue();\n" +
			"        assertNull(test3, () -> \"message\");\n" +
			"        test3.longValue();\n" +
			"    }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	test1.longValue();\n" +
		"	^^^^^\n" +
		"Null pointer access: The variable test1 can only be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	test2.longValue();\n" +
		"	^^^^^\n" +
		"Null pointer access: The variable test2 can only be null at this location\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 10)\n" +
		"	test3.longValue();\n" +
		"	^^^^^\n" +
		"Null pointer access: The variable test3 can only be null at this location\n" +
		"----------\n"
	);
}
// junit 5's assertTrue
public void testBug568542c() throws IOException {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // uses Supplier
	this.runConformTest(
		new String[] {
			ORG_JUNIT_JUPITER_API_ASSERTIONS_NAME,
			ORG_JUNIT_JUPITER_API_ASSERTIONS_CONTENT,
			"X.java",
			"import static org.junit.jupiter.api.Assertions.assertTrue;\n" +
			"public class X {\n" +
			"    void test(Long test1, Long test2, Long test3) {\n" +
			"        boolean b = (test1 != null | test2 != null | test3 != null);\n" +
			"        assertTrue(test1 != null);\n" +
			"        test1.longValue();\n" +
			"        assertTrue(test2 != null, \"message\");\n" +
			"        test2.longValue();\n" +
			"        assertTrue(test3 != null, () -> \"message\");\n" +
			"        test3.longValue();\n" +
			"    }\n" +
			"}\n"},
		"");
}
// junit 5's assertFalse
public void testBug568542d() throws IOException {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // uses Supplier
	runNegativeTest(
		new String[] {
			ORG_JUNIT_JUPITER_API_ASSERTIONS_NAME,
			ORG_JUNIT_JUPITER_API_ASSERTIONS_CONTENT,
			"X.java",
			"import static org.junit.jupiter.api.Assertions.assertFalse;\n" +
			"public class X {\n" +
			"    void test(Long test1, Long test2, Long test3) {\n" +
			"        boolean b = (test1 != null | test2 != null | test3 != null);\n" +
			"        assertFalse(test1 != null);\n" +
			"        test1.longValue();\n" +
			"        assertFalse(test2 != null, \"message\");\n" +
			"        test2.longValue();\n" +
			"        assertFalse(test3 != null, () -> \"message\");\n" +
			"        test3.longValue();\n" +
			"    }\n" +
			"}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	test1.longValue();\n" +
		"	^^^^^\n" +
		"Null pointer access: The variable test1 can only be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	test2.longValue();\n" +
		"	^^^^^\n" +
		"Null pointer access: The variable test2 can only be null at this location\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 10)\n" +
		"	test3.longValue();\n" +
		"	^^^^^\n" +
		"Null pointer access: The variable test3 can only be null at this location\n" +
		"----------\n"
	);
}
}
