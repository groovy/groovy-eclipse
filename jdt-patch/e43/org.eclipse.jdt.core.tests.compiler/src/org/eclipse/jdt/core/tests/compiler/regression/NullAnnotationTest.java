/*******************************************************************************
 * Copyright (c) 2010, 2013 GK Software AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;


import java.io.File;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

// see bug 186342 - [compiler][null] Using annotations for null checking
public class NullAnnotationTest extends AbstractComparableTest {

// class libraries including our default null annotation types:
String[] LIBS;

// names and content of custom annotations used in a few tests:
private static final String CUSTOM_NONNULL_NAME = "org/foo/NonNull.java";
private static final String CUSTOM_NONNULL_CONTENT =
	"package org.foo;\n" +
	"import static java.lang.annotation.ElementType.*;\n" +
	"import java.lang.annotation.*;\n" +
	"@Retention(RetentionPolicy.CLASS)\n" +
	"@Target({METHOD,PARAMETER,LOCAL_VARIABLE})\n" +
	"public @interface NonNull {\n" +
	"}\n";
private static final String CUSTOM_NULLABLE_NAME = "org/foo/Nullable.java";
private static final String CUSTOM_NULLABLE_CONTENT = "package org.foo;\n" +
	"import static java.lang.annotation.ElementType.*;\n" +
	"import java.lang.annotation.*;\n" +
	"@Retention(RetentionPolicy.CLASS)\n" +
	"@Target({METHOD,PARAMETER,LOCAL_VARIABLE})\n" +
	"public @interface Nullable {\n" +
	"}\n";

public NullAnnotationTest(String name) {
	super(name);
}

// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which do not belong to the class are skipped...
static {
//		TESTS_NAMES = new String[] { "testBug412076" };
//		TESTS_NUMBERS = new int[] { 561 };
//		TESTS_RANGE = new int[] { 1, 2049 };
}

public static Test suite() {
	return buildComparableTestSuite(testClass());
}

public static Class testClass() {
	return NullAnnotationTest.class;
}

protected void setUp() throws Exception {
	super.setUp();
	if (this.LIBS == null) {
		this.LIBS = getLibsWithNullAnnotations();
	}
}
// Conditionally augment problem detection settings
static boolean setNullRelatedOptions = true;
protected Map getCompilerOptions() {
    Map defaultOptions = super.getCompilerOptions();
    if (setNullRelatedOptions) {
    	defaultOptions.put(JavaCore.COMPILER_PB_NULL_REFERENCE, JavaCore.ERROR);
	    defaultOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	    defaultOptions.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.ERROR);
		defaultOptions.put(JavaCore.COMPILER_PB_INCLUDE_ASSERTS_IN_NULL_ANALYSIS, JavaCore.ENABLED);

		defaultOptions.put(JavaCore.COMPILER_PB_MISSING_OVERRIDE_ANNOTATION_FOR_INTERFACE_METHOD_IMPLEMENTATION, JavaCore.DISABLED);

		// enable null annotations:
		defaultOptions.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
		// leave other new options at these defaults:
//		defaultOptions.put(CompilerOptions.OPTION_ReportNullContractViolation, JavaCore.ERROR);
//		defaultOptions.put(CompilerOptions.OPTION_ReportPotentialNullContractViolation, JavaCore.ERROR);
//		defaultOptions.put(CompilerOptions.OPTION_ReportNullContractInsufficientInfo, CompilerOptions.WARNING);

//		defaultOptions.put(CompilerOptions.OPTION_NullableAnnotationName, "org.eclipse.jdt.annotation.Nullable");
//		defaultOptions.put(CompilerOptions.OPTION_NonNullAnnotationName, "org.eclipse.jdt.annotation.NonNull");
    }
    return defaultOptions;
}
void runNegativeTestWithLibs(String[] testFiles, String expectedErrorLog) {
	runNegativeTest(
			testFiles,
			expectedErrorLog,
			this.LIBS,
			false /*shouldFlush*/);
}
void runNegativeTestWithLibs(boolean shouldFlushOutputDirectory, String[] testFiles, Map customOptions, String expectedErrorLog) {
	runNegativeTest(
			shouldFlushOutputDirectory,
			testFiles,
			this.LIBS,
			customOptions,
			expectedErrorLog,
			// runtime options
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
void runNegativeTestWithLibs(String[] testFiles, Map customOptions, String expectedErrorLog) {
	runNegativeTestWithLibs(false /* flush output directory */,	testFiles, customOptions, expectedErrorLog);
}
void runConformTestWithLibs(String[] testFiles, Map customOptions, String expectedCompilerLog) {
	runConformTestWithLibs(false /* flush output directory */, testFiles, customOptions, expectedCompilerLog);
}
void runConformTestWithLibs(String[] testFiles, Map customOptions, String expectedCompilerLog, String expectedOutput) {
	runConformTest(
			false, /* flush output directory */
			testFiles,
			this.LIBS,
			customOptions,
			expectedCompilerLog,
			expectedOutput,
			"",/* expected error */
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
void runConformTestWithLibs(boolean shouldFlushOutputDirectory, String[] testFiles, Map customOptions, String expectedCompilerLog) {
	runConformTest(
			shouldFlushOutputDirectory,
			testFiles,
			this.LIBS,
			customOptions,
			expectedCompilerLog,
			"",/* expected output */
			"",/* expected error */
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
void runConformTest(String[] testFiles, Map customOptions, String expectedOutputString) {
	runConformTest(
			testFiles,
			expectedOutputString,
			null /*classLibraries*/,
			true /*shouldFlushOutputDirectory*/,
			null /*vmArguments*/,
			customOptions,
			null /*customRequestor*/);

}
// a nullable argument is dereferenced without a check
public void test_nullable_paramter_001() {
	runNegativeTest(
		new String[] {
			"X.java",
			  "import org.eclipse.jdt.annotation.*;\n" +
			  "public class X {\n" +
			  "    void foo(@Nullable Object o) {\n" +
			  "        System.out.print(o.toString());\n" +
			  "    }\n" +
			  "}\n"},
	    "----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	System.out.print(o.toString());\n" +
		"	                 ^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n",
		this.LIBS,
		true /* shouldFlush*/);
}

// a null value is passed to a nullable argument
public void test_nullable_paramter_002() {
	runConformTest(
		new String[] {
			"X.java",
			  "import org.eclipse.jdt.annotation.*;\n" +
			  "public class X {\n" +
			  "    void foo(@Nullable Object o) {\n" +
			  "        // nop\n" +
			  "    }\n" +
			  "    void bar() {\n" +
			  "        foo(null);\n" +
			  "    }\n" +
			  "}\n"},
	    "",
	    this.LIBS,
	    false/*shouldFlush*/,
	    null/*vmArgs*/);
}

// a non-null argument is checked for null
public void test_nonnull_parameter_001() {
	runNegativeTest(
		new String[] {
			"X.java",
			  "import org.eclipse.jdt.annotation.*;\n" +
			  "public class X {\n" +
			  "    void foo(@NonNull Object o) {\n" +
			  "        if (o != null)\n" +
			  "              System.out.print(o.toString());\n" +
			  "    }\n" +
			  "}\n"},
	    "----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	if (o != null)\n" +
		"	    ^\n" +
		"Redundant null check: The variable o is specified as @NonNull\n" +
		"----------\n",
		this.LIBS,
		true /* shouldFlush*/);
}
// a non-null argument is dereferenced without a check
public void test_nonnull_parameter_002() {
	runConformTest(
		new String[] {
			"X.java",
			  "import org.eclipse.jdt.annotation.*;\n" +
			  "public class X {\n" +
			  "    void foo(@NonNull Object o) {\n" +
			  "        System.out.print(o.toString());\n" +
			  "    }\n" +
			  "    public static void main(String... args) {\n" +
			  "        new X().foo(\"OK\");\n" +
			  "    }\n" +
			  "}\n"},
	    "OK",
	    this.LIBS,
	    false/*shouldFlush*/,
	    null/*vmArgs*/);
}
// passing null to nonnull parameter - many fields in enclosing class
public void test_nonnull_parameter_003() {
	runNegativeTest(
		new String[] {
			"X.java",
			  "import org.eclipse.jdt.annotation.*;\n" +
			  "public class X {\n" +
			  "    int i00, i01, i02, i03, i04, i05, i06, i07, i08, i09;" +
			  "    int i10, i11, i12, i13, i14, i15, i16, i17, i18, i19;" +
			  "    int i20, i21, i22, i23, i24, i25, i26, i27, i28, i29;" +
			  "    int i30, i31, i32, i33, i34, i35, i36, i37, i38, i39;" +
			  "    int i40, i41, i42, i43, i44, i45, i46, i47, i48, i49;" +
			  "    int i50, i51, i52, i53, i54, i55, i56, i57, i58, i59;" +
			  "    int i60, i61, i62, i63, i64, i65, i66, i67, i68, i69;" +
			  "    void foo(@NonNull Object o) {\n" +
			  "        System.out.print(o.toString());\n" +
			  "    }\n" +
			  "    void bar() {\n" +
			  "        foo(null);\n" +
			  "    }\n" +
			  "}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	foo(null);\n" +
		"	    ^^^^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" +
		"----------\n",
		this.LIBS,
		true /* shouldFlush*/);
}
// passing potential null to nonnull parameter - target method is consumed from .class
public void test_nonnull_parameter_004() {
	runConformTestWithLibs(
			new String[] {
				"Lib.java",
					"import org.eclipse.jdt.annotation.*;\n" +
				"public class Lib {\n" +
				"    void setObject(@NonNull Object o) { }\n" +
				"}\n"
			},
			null /*customOptions*/,
			"");
	runNegativeTestWithLibs(
		false, // don't flush
		new String[] {
			"X.java",
			  "public class X {\n" +
			  "    void bar(Lib l, boolean b) {\n" +
			  "        Object o = null;\n" +
			  "        if (b) o = new Object();\n" +
			  "        l.setObject(o);\n" +
			  "    }\n" +
			  "}\n"},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	l.setObject(o);\n" +
		"	            ^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is inferred as @Nullable\n" +
		"----------\n");
}
// passing unknown value to nonnull parameter  - target method is consumed from .class
public void test_nonnull_parameter_005() {
	runConformTestWithLibs(
			new String[] {
				"Lib.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"public class Lib {\n" +
				"    void setObject(@NonNull Object o) { }\n" +
				"}\n"
			},
			null /*customOptions*/,
			"");
	runConformTestWithLibs(
		false, // don't flush
		new String[] {
			"X.java",
			  "public class X {\n" +
			  "    void bar(Lib l, Object o) {\n" +
			  "        l.setObject(o);\n" +
			  "    }\n" +
			  "}\n"},
		null /* options */,
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	l.setObject(o);\n" +
		"	            ^\n" +
		"Null type safety: The expression of type Object needs unchecked conversion to conform to \'@NonNull Object\'\n" +
		"----------\n");
}
// a ternary non-null expression is passed to a nonnull parameter
public void test_nonnull_parameter_006() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
			"X.java",
			  "import org.eclipse.jdt.annotation.*;\n" +
			  "public class X {\n" +
			  "    	void m1(@NonNull String a) {}\n" +
			  "		void m2(@Nullable String b) {\n" +
			  "			m1(b == null ? \"\" : b);\n" +
			  "		}\n" +
			  "}\n"},
		customOptions,
		""  /* compiler output */);
}
// nullable value passed to a non-null parameter in a super-call
public void test_nonnull_parameter_007() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"XSub.java",
			  "import org.eclipse.jdt.annotation.*;\n" +
			  "public class XSub extends XSuper {\n" +
			  "    	XSub(@Nullable String b) {\n" +
			  "			super(b);\n" +
			  "		}\n" +
			  "}\n",
			"XSuper.java",
			  "import org.eclipse.jdt.annotation.*;\n" +
			  "public class XSuper {\n" +
			  "    	XSuper(@NonNull String b) {\n" +
			  "		}\n" +
			  "}\n"
		},
		customOptions,
		"----------\n" +
		"1. ERROR in XSub.java (at line 4)\n" +
		"	super(b);\n" +
		"	      ^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is specified as @Nullable\n" +
		"----------\n");
}
// a nullable value is passed to a non-null parameter in an allocation expression
public void test_nonnull_parameter_008() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			  "import org.eclipse.jdt.annotation.*;\n" +
			  "public class X {\n" +
			  "    	X(@NonNull String a) {}\n" +
			  "		static X create(@Nullable String b) {\n" +
			  "			return new X(b);\n" +
			  "		}\n" +
			  "}\n"},
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	return new X(b);\n" +
		"	             ^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is specified as @Nullable\n" +
		"----------\n"  /* compiler output */);
}
// a nullable value is passed to a non-null parameter in a qualified allocation expression
public void test_nonnull_parameter_009() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			  "import org.eclipse.jdt.annotation.*;\n" +
			  "public class X {\n" +
			  "    class Local {\n" +
			  "    	   Local(@NonNull String a) {}\n" +
			  "    }\n" +
			  "	   Local create(@Nullable String b) {\n" +
			  "	       return this.new Local(b);\n" +
			  "    }\n" +
			  "}\n"},
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	return this.new Local(b);\n" +
		"	                      ^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is specified as @Nullable\n" +
		"----------\n"  /* compiler output */);
}
// null is passed to a non-null parameter in a qualified allocation expression, across CUs
public void test_nonnull_parameter_010() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"ContainingInner2.java",
			"public class ContainingInner2 {\n" + 
			"    public ContainingInner2 (@org.eclipse.jdt.annotation.NonNull Object o) {\n" + 
			"    }\n" + 
			"    public class Inner {\n" + 
			"        public Inner (@org.eclipse.jdt.annotation.NonNull Object o) {\n" + 
			"        }\n" + 
			"    }\n" + 
			"}\n",
			"X.java",
			"public class X {\n" +
			"	 void create() {\n" +
			"          ContainingInner2 container = new ContainingInner2(null);\n" +
			"	       ContainingInner2.Inner inner = container.new Inner(null);\n" +
			"    }\n" +
		  	"}\n"},
		customOptions,
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	ContainingInner2 container = new ContainingInner2(null);\n" + 
		"	                                                  ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 4)\n" + 
		"	ContainingInner2.Inner inner = container.new Inner(null);\n" + 
		"	                                                   ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n"  /* compiler output */);
}
// null is passed to a non-null parameter in a qualified allocation expression, target class read from .class
public void test_nonnull_parameter_011() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runConformTestWithLibs(
			new String[] {
				"ContainingInner2.java",
				"public class ContainingInner2 {\n" + 
				"    public ContainingInner2 (@org.eclipse.jdt.annotation.NonNull Object o) {\n" + 
				"    }\n" + 
				"    public class Inner {\n" + 
				"        public Inner (@org.eclipse.jdt.annotation.NonNull Object o) {\n" + 
				"        }\n" + 
				"    }\n" + 
				"}\n",
			},
			null /*customOptions*/,
			"");
	runNegativeTestWithLibs(
		false, // flush directory
		new String[] {
			"X.java",
			"public class X {\n" +
			"	 void create() {\n" +
			"          ContainingInner2 container = new ContainingInner2(null);\n" +
			"	       ContainingInner2.Inner inner = container.new Inner(null);\n" +
			"    }\n" +
		  	"}\n"},
		customOptions,
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	ContainingInner2 container = new ContainingInner2(null);\n" + 
		"	                                                  ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 4)\n" + 
		"	ContainingInner2.Inner inner = container.new Inner(null);\n" + 
		"	                                                   ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n"  /* compiler output */);
}
// null is passed to a non-null parameter in a qualified allocation expression, generic constructor, target class read from .class
public void test_nonnull_parameter_012() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runConformTestWithLibs(
			new String[] {
				"ContainingInner2.java",
				"public class ContainingInner2 {\n" + 
				"    public ContainingInner2 (@org.eclipse.jdt.annotation.NonNull Object o) {\n" + 
				"    }\n" + 
				"    public class Inner {\n" + 
				"        public <T> Inner (@org.eclipse.jdt.annotation.NonNull T o) {\n" + 
				"        }\n" + 
				"    }\n" + 
				"}\n",
			},
			null /*customOptions*/,
			"");
	runNegativeTestWithLibs(
		false, // flush directory
		new String[] {
			"X.java",
			"public class X {\n" +
			"	 void create() {\n" +
			"          ContainingInner2 container = new ContainingInner2(null);\n" +
			"	       ContainingInner2.Inner inner = container.new Inner(null);\n" +
			"    }\n" +
		  	"}\n"},
		customOptions,
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	ContainingInner2 container = new ContainingInner2(null);\n" + 
		"	                                                  ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 4)\n" + 
		"	ContainingInner2.Inner inner = container.new Inner(null);\n" + 
		"	                                                   ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n"  /* compiler output */);
}
// a method of a local class has a non-null parameter, client passes null
public void test_nonnull_parameter_013() {
	runNegativeTestWithLibs(
		new String[] {
			"B.java",
			"class B {\n" +
			"    void bar () {\n" +
			"        class Local {\n" +
			"            void callMe(@org.eclipse.jdt.annotation.NonNull Object o){\n" +
			"            }\n" +
			"        }\n" +
			"        Local l = new Local();\n" +
			"        l.callMe(null);\n" +
			"    } \n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in B.java (at line 8)\n" + 
		"	l.callMe(null);\n" + 
		"	         ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n");
}
// non-null varargs (message send)
public void test_nonnull_parameter_015() {
	if (this.complianceLevel > ClassFileConstants.JDK1_7) {
		fail("Reminder: should check if JSR 308 mandates a change in handling vararg elements (see bug 365983).");
		return;
	}
	runNegativeTest(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    void foo(@NonNull Object ... o) {\n" +
			"        if (o != null)\n" +
			"              System.out.print(o.toString());\n" +
			"    }\n" +
			"    void foo2(int i, @NonNull Object ... o) {\n" +
			"        if (o.length > 0 && o[0] != null)\n" +
			"              System.out.print(o[0].toString());\n" +
			"    }\n" +
			"    void bar() {\n" +
			"        foo((Object)null);\n" +		// unchecked: single plain argument
			"        Object[] objs = null;\n" +
			"        foo(objs);\n" +				// error
			"        foo(this, null);\n" +			// unchecked: multiple plain arguments
			"        foo2(2, (Object)null);\n" +    // unchecked: single plain argument
			"        foo2(2, null, this);\n" +      // unchecked: multiple plain arguments
			"        foo2(2, null);\n" +  			// error
			"    }\n" +
			"}\n"},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	if (o != null)\n" + 
			"	    ^\n" + 
			"Redundant null check: The variable o is specified as @NonNull\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 14)\n" + 
			"	foo(objs);\n" + 
			"	    ^^^^\n" + 
			"Null type mismatch: required \'@NonNull Object[]\' but the provided value is null\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 18)\n" + 
			"	foo2(2, null);\n" + 
			"	^^^^^^^^^^^^^\n" + 
			"The argument of type null should explicitly be cast to Object[] for the invocation of the varargs method foo2(int, Object...) from type X. It could alternatively be cast to Object for a varargs invocation\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 18)\n" + 
			"	foo2(2, null);\n" + 
			"	        ^^^^\n" + 
			"Null type mismatch: required \'@NonNull Object[]\' but the provided value is null\n" + 
			"----------\n",
		this.LIBS,
		true /* shouldFlush*/);
}
// non-null varargs (allocation and explicit constructor calls)
public void test_nonnull_parameter_016() {
	if (this.complianceLevel > ClassFileConstants.JDK1_7) {
		fail("Reminder: should check if JSR 308 mandates a change in handling vararg elements (see bug 365983).");
		return;
	}
	runNegativeTest(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    X(@NonNull Object ... o) {\n" +
			"        if (o != null)\n" +
			"              System.out.print(o.toString());\n" +
			"    }\n" +
			"    class Y extends X {\n" +
			"        Y(int i, @NonNull Object ... o) {\n" +
			"        	super(i, (Object)null);\n" +
			"        }\n" +
			"        Y(char c, @NonNull Object ... o) {\n" +
			"        	this(1, new Object(), null);\n" +
			"        }\n" +
			"    }\n" +
			"    void bar() {\n" +
			"        new X((Object[])null);\n" +
			"        new X(this, null);\n" +
			"        X x = new X(null, this);\n" +
			"        x.new Y(2, (Object)null);\n" +
			"        this.new Y(2, null, this);\n" +
			"        this.new Y(2, (Object[])null);\n" +
			"    }\n" +
			"}\n"},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	if (o != null)\n" +
			"	    ^\n" +
			"Redundant null check: The variable o is specified as @NonNull\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 16)\n" +
			"	new X((Object[])null);\n" +
			"	      ^^^^^^^^^^^^^^\n" +
			"Null type mismatch: required \'@NonNull Object[]\' but the provided value is null\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 21)\n" +
			"	this.new Y(2, (Object[])null);\n" +
			"	              ^^^^^^^^^^^^^^\n" +
			"Null type mismatch: required \'@NonNull Object[]\' but the provided value is null\n" +
			"----------\n",
		this.LIBS,
		true /* shouldFlush*/);
}
// Bug 367203 - [compiler][null] detect assigning null to nonnull argument
public void test_nonnull_argument_001() {
	runNegativeTestWithLibs(
			new String[] {
				"ShowNPE2.java",
				"import org.eclipse.jdt.annotation.NonNullByDefault;\n" + 
				"@NonNullByDefault\n" + 
				"public class ShowNPE2 {\n" + 
				"     public Object foo(Object o1, final boolean b) {\n" + 
				"         o1 = null;   // expect NPE error\n" + 
				"         System.out.println(o1.toString());   \n" + 
				"         return null;  // expect NPE error\n" + 
				"    }\n" + 
				"}"
			},
			"----------\n" + 
			"1. ERROR in ShowNPE2.java (at line 5)\n" + 
			"	o1 = null;   // expect NPE error\n" + 
			"	     ^^^^\n" + 
			"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
			"----------\n" + 
			"2. ERROR in ShowNPE2.java (at line 7)\n" + 
			"	return null;  // expect NPE error\n" + 
			"	       ^^^^\n" + 
			"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
			"----------\n");
}
// Bug 367203 - [compiler][null] detect assigning null to nonnull argument
public void test_nonnull_argument_002() {
	runNegativeTestWithLibs(
			new String[] {
				"ShowNPE2.java",
				"import org.eclipse.jdt.annotation.NonNullByDefault;\n" + 
				"@NonNullByDefault\n" + 
				"public class ShowNPE2 {\n" + 
				"    public Object foo(Object o1, final boolean b) {\n" + 
				"        bar(o1); // expecting no problem\n" + 
				"        return null;  // expect NPE error\n" + 
				"    }\n" +
				"    void bar(Object o2) {}\n" + 
				"}"
			},
			"----------\n" + 
			"1. ERROR in ShowNPE2.java (at line 6)\n" + 
			"	return null;  // expect NPE error\n" + 
			"	       ^^^^\n" + 
			"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
			"----------\n");
}
// a method of a local class has a non-null parameter, client passes potential null (msg send)
public void test_nonnull_parameter_014() {
	runNegativeTestWithLibs(
		new String[] {
			"B.java",
			"class B {\n" +
			"    void bar () {\n" +
			"        class Local {\n" +
			"            void callMe(@org.eclipse.jdt.annotation.NonNull Object o){\n" +
			"            }\n" +
			"        }\n" +
			"        Local l = new Local();\n" +
			"        l.callMe(getNull());\n" +
			"    }\n" +
			"    @org.eclipse.jdt.annotation.Nullable Object getNull() { return null; }" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in B.java (at line 8)\n" + 
		"	l.callMe(getNull());\n" + 
		"	         ^^^^^^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is inferred as @Nullable\n" + 
		"----------\n");
}
// assigning potential null to a nonnull local variable
public void test_nonnull_local_001() {
	runNegativeTest(
		new String[] {
			"X.java",
			  "import org.eclipse.jdt.annotation.*;\n" +
			  "public class X {\n" +
			  "    void foo(boolean b, Object p) {\n" +
			  "        @NonNull Object o1 = b ? null : new Object();\n" +
			  "        @NonNull String o2 = \"\";\n" +
			  "        o2 = null;\n" +
			  "        @NonNull Object o3 = p;\n" +
			  "    }\n" +
			  "}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	@NonNull Object o1 = b ? null : new Object();\n" +
		"	                     ^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is inferred as @Nullable\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	o2 = null;\n" +
		"	     ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 7)\n" +
		"	@NonNull Object o3 = p;\n" +
		"	                     ^\n" +
		"Null type safety: The expression of type Object needs unchecked conversion to conform to \'@NonNull Object\'\n" +
		"----------\n",
		this.LIBS,
		true /* shouldFlush*/);
}

// assigning potential null to a nonnull local variable - separate decl and assignment
public void test_nonnull_local_002() {
	runNegativeTest(
		new String[] {
			"X.java",
			  "import org.eclipse.jdt.annotation.*;\n" +
			  "public class X {\n" +
			  "    void foo(boolean b, Object p) {\n" +
			  "        @NonNull Object o1;\n" +
			  "        o1 = b ? null : new Object();\n" +
			  "        @NonNull String o2;\n" +
			  "        o2 = \"\";\n" +
			  "        o2 = null;\n" +
			  "        @NonNull Object o3;\n" +
			  "        o3 = p;\n" +
			  "    }\n" +
			  "}\n"},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	o1 = b ? null : new Object();\n" +
		"	     ^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is inferred as @Nullable\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	o2 = null;\n" +
		"	     ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 10)\n" +
		"	o3 = p;\n" +
		"	     ^\n" +
		"Null type safety: The expression of type Object needs unchecked conversion to conform to \'@NonNull Object\'\n" +
		"----------\n",
		this.LIBS,
		true /* shouldFlush*/);
}

// a method tries to tighten the type specification, super declares parameter o as @Nullable
// other parameters: s is redefined from not constrained to @Nullable which is OK
//                   third is redefined from not constrained to @NonNull which is bad, too
public void test_parameter_specification_inheritance_001() {
	runConformTestWithLibs(
		new String[] {
			"Lib.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Lib {\n" +
			"    void foo(String s, @Nullable Object o, Object third) { }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"");
	runNegativeTestWithLibs(
		false, // don't flush
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X extends Lib {\n" +
			"    @Override\n" +
			"    void foo(@Nullable String s, @NonNull Object o, @NonNull Object third) { System.out.print(o.toString()); }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	void foo(@Nullable String s, @NonNull Object o, @NonNull Object third) { System.out.print(o.toString()); }\n" +
		"	                             ^^^^^^^^^^^^^^^\n" +
		"Illegal redefinition of parameter o, inherited method from Lib declares this parameter as @Nullable\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	void foo(@Nullable String s, @NonNull Object o, @NonNull Object third) { System.out.print(o.toString()); }\n" +
		"	                                                ^^^^^^^^^^^^^^^\n" +
		"Illegal redefinition of parameter third, inherited method from Lib does not constrain this parameter\n" +
		"----------\n");
}
// a method body fails to redeclare the inherited null annotation, super declares parameter as @Nullable
public void test_parameter_specification_inheritance_002() {
	runConformTest(
		new String[] {
			"Lib.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Lib {\n" +
			"    void foo(@Nullable Object o) { }\n" +
			"}\n"
		},
		"",
	    this.LIBS,
	    false/*shouldFlush*/,
	    null/*vmArgs*/);
	runNegativeTestWithLibs(
		false, // don't flush
		new String[] {
			"X.java",
			"public class X extends Lib {\n" +
			"    @Override\n" +
			"    void foo(Object o) {\n" +
			"        System.out.print(o.toString());\n" +
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	void foo(Object o) {\n" +
		"	         ^^^^^^\n" +
		"Missing nullable annotation: inherited method from Lib declares this parameter as @Nullable\n" +
		"----------\n");
}
// a method relaxes the parameter null specification, super interface declares parameter o as @NonNull
// other (first) parameter just repeats the inherited @NonNull
public void test_parameter_specification_inheritance_003() {
	runConformTest(
		new String[] {
			"IX.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public interface IX {\n" +
			"    void foo(@NonNull String s, @NonNull Object o);\n" +
			"}\n",
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X implements IX {\n" +
			"    public void foo(@NonNull String s, @Nullable Object o) { ; }\n" +
			"    void bar() { foo(\"OK\", null); }\n" +
			"}\n"
		},
		"",
	    this.LIBS,
	    false/*shouldFlush*/,
	    null/*vmArgs*/);
}
// a method adds a @NonNull annotation, super interface has no null annotation
// changing other from unconstrained to @Nullable is OK
public void test_parameter_specification_inheritance_004() {
	runConformTest(
		new String[] {
			"IX.java",
			"public interface IX {\n" +
			"    void foo(Object o, Object other);\n" +
			"}\n"
		});
	runNegativeTestWithLibs(
		false, // don't flush
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X implements IX {\n" +
			"    public void foo(@NonNull Object o, @Nullable Object other) { System.out.print(o.toString()); }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	public void foo(@NonNull Object o, @Nullable Object other) { System.out.print(o.toString()); }\n" +
		"	                ^^^^^^^^^^^^^^^\n" +
		"Illegal redefinition of parameter o, inherited method from IX does not constrain this parameter\n" +
		"----------\n");
}
// a method tries to relax the null contract, super declares @NonNull return
public void test_parameter_specification_inheritance_005() {
	runConformTestWithLibs(
		new String[] {
			"Lib.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Lib {\n" +
			"    @NonNull Object getObject() { return new Object(); }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"");
	runNegativeTestWithLibs(
		false, //dont' flush
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X extends Lib {\n" +
			"    @Override\n" +
			"    @Nullable Object getObject() { return null; }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	@Nullable Object getObject() { return null; }\n" +
		"	^^^^^^^^^^^^^^^^\n" +
		"The return type is incompatible with the @NonNull return from Lib.getObject()\n" +
		"----------\n");
}

// super has no constraint for return, sub method confirms the null contract as @Nullable
public void test_parameter_specification_inheritance_006() {
	runConformTest(
		new String[] {
			"Lib.java",
			"public class Lib {\n" +
			"    Object getObject() { return null; }\n" +
			"}\n"
		});
	runConformTestWithLibs(
		false, // don't flush
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X extends Lib {\n" +
			"    @Override\n" +
			"    @Nullable Object getObject() { return null; }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"");
}
// a method body violates the inherited null specification, super declares @NonNull return, missing redeclaration
public void test_parameter_specification_inheritance_007() {
	runConformTestWithLibs(
		new String[] {
			"Lib.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Lib {\n" +
			"    @NonNull Object getObject() { return new Object(); }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"");
	runNegativeTestWithLibs(
		false, // don't flush
		new String[] {
			"X.java",
			"public class X extends Lib {\n" +
			"    @Override\n" +
			"    Object getObject() { return null; }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	Object getObject() { return null; }\n" +
		"	^^^^^^\n" +
		"The return type is incompatible with the @NonNull return from Lib.getObject()\n" +
		"----------\n");
}
//a method body violates the @NonNull return specification (repeated from super)
public void test_parameter_specification_inheritance_007a() {
	runConformTestWithLibs(
		new String[] {
			"Lib.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Lib {\n" +
			"    @NonNull Object getObject() { return new Object(); }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"");
	runNegativeTestWithLibs(
		false, // don't flush
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X extends Lib {\n" +
			"    @Override\n" +
			"    @NonNull Object getObject() { return null; }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	@NonNull Object getObject() { return null; }\n" +
		"	                                     ^^^^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" +
		"----------\n");
}
// a client potentially violates the inherited null specification, super interface declares @NonNull parameter
public void test_parameter_specification_inheritance_008() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    void printObject(@NonNull Object o) { System.out.print(o.toString()); }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"");
	runNegativeTestWithLibs(
		false, // don't flush
		new String[] {
			"XSub.java",
			"public class XSub extends X {\n" +
			"    @Override\n" +
			"    public void printObject(Object o) { super.printObject(o); }\n" +
			"}\n",
			"M.java",
			"public class M{\n" +
			"    void foo(X x, Object o) {\n" +
			"        x.printObject(o);\n" +
			"    }\n" +
			"}\n"
		},
		options,
		"----------\n" +
		"1. WARNING in XSub.java (at line 3)\n" + 
		"	public void printObject(Object o) { super.printObject(o); }\n" + 
		"	                        ^^^^^^\n" + 
		"Missing non-null annotation: inherited method from X declares this parameter as @NonNull\n" + 
		"----------\n" + 
		"2. ERROR in XSub.java (at line 3)\n" +
		"	public void printObject(Object o) { super.printObject(o); }\n" +
		"	                                                      ^\n" +
		"Null type safety: The expression of type Object needs unchecked conversion to conform to \'@NonNull Object\'\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in M.java (at line 3)\n" +
		"	x.printObject(o);\n" +
		"	              ^\n" +
		"Null type safety: The expression of type Object needs unchecked conversion to conform to \'@NonNull Object\'\n" +
		"----------\n");
}
// a static method has a more relaxed null contract than a like method in the super class, but no overriding.
public void test_parameter_specification_inheritance_009() {
	runConformTestWithLibs(
		new String[] {
			"Lib.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Lib {\n" +
			"    @NonNull static Object getObject() { return new Object(); }\n" +
			"}\n",
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X extends Lib {\n" +
			"    @Nullable static Object getObject() { return null; }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"");
}
// class default is nonnull, method and its super both use the default
public void test_parameter_specification_inheritance_010() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
	"p1/X.java",
			"package p1;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class X {\n" +
			"    protected String getString(String s) {\n" +
			"        if (Character.isLowerCase(s.charAt(0)))\n" +
			"	        return getString(s);\n" +
			"	     return s;\n" +
			"    }\n" +
			"}\n",
	"p1/Y.java",
			"package p1;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class Y extends X {\n" +
			"    @Override\n" +
			"    protected String getString(String s) {\n" +
			"	     return super.getString(s);\n" +
			"    }\n" +
			"}\n",
		},
		customOptions,
		"");
}
// class default is nonnull, method and its super both use the default, super-call passes null
public void test_parameter_specification_inheritance_011() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
	"p1/X.java",
			"package p1;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class X {\n" +
			"    protected String getString(String s) {\n" +
			"        if (Character.isLowerCase(s.charAt(0)))\n" +
			"	        return getString(s);\n" +
			"	     return s;\n" +
			"    }\n" +
			"}\n",
	"p1/Y.java",
			"package p1;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class Y extends X {\n" +
			"    @Override\n" +
			"    protected String getString(String s) {\n" +
			"	     return super.getString(null);\n" +
			"    }\n" +
			"}\n",
		},
		customOptions,
		"----------\n" +
		"1. ERROR in p1\\Y.java (at line 7)\n" +
		"	return super.getString(null);\n" +
		"	                       ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n");
}
// methods from two super types have different null contracts.
// sub-class merges both using the weakest common contract
public void test_parameter_specification_inheritance_012() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
	"p1/X.java",
			"package p1;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    public @Nullable String getString(String s1, @Nullable String s2, @NonNull String s3) {\n" +
			"	     return s1;\n" +
			"    }\n" +
			"}\n",
	"p1/IY.java",
			"package p1;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public interface IY {\n" +
			"    @NonNull String getString(@NonNull String s1, @NonNull String s2, @Nullable String s3);\n" +
			"}\n",
	"p1/Y.java",
			"package p1;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Y extends X implements IY {\n" +
			"    @Override\n" +
			"    public @NonNull String getString(@Nullable String s1, @Nullable String s2, @Nullable String s3) {\n" +
			"	     return \"\";\n" +
			"    }\n" +
			"}\n",
		},
		customOptions,
		"");
}
// methods from two super types have different null contracts.
// sub-class overrides this method in non-conforming ways
public void test_parameter_specification_inheritance_013() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	customOptions.put(JavaCore.COMPILER_PB_NONNULL_PARAMETER_ANNOTATION_DROPPED, JavaCore.IGNORE);
	runNegativeTestWithLibs(
		new String[] {
	"p1/X.java",
			"package p1;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    public @Nullable String getString(String s1, @Nullable String s2, @NonNull String s3) {\n" +
			"	     return s1;\n" +
			"    }\n" +
			"}\n",
	"p1/IY.java",
			"package p1;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public interface IY {\n" +
			"    @NonNull String getString(@NonNull String s1, @NonNull String s2, @Nullable String s3);\n" +
			"}\n",
	"p1/Y.java",
			"package p1;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Y extends X implements IY {\n" +
			"    @Override\n" +
			"    public @Nullable String getString(String s1, @NonNull String s2, @NonNull String s3) {\n" +
			"	     return \"\";\n" +
			"    }\n" +
			"}\n",
		},
		customOptions,
		"----------\n" +
		"1. ERROR in p1\\Y.java (at line 5)\n" +
		"	public @Nullable String getString(String s1, @NonNull String s2, @NonNull String s3) {\n" +
		"	       ^^^^^^^^^^^^^^^^\n" +
		"The return type is incompatible with the @NonNull return from IY.getString(String, String, String)\n" +
		// no problem regarding s1: widening @NonNull to unannotated
		"----------\n" +
		"2. ERROR in p1\\Y.java (at line 5)\n" +
		"	public @Nullable String getString(String s1, @NonNull String s2, @NonNull String s3) {\n" +
		"	                                             ^^^^^^^^^^^^^^^\n" +
		"Illegal redefinition of parameter s2, inherited method from X declares this parameter as @Nullable\n" +
		"----------\n" +
		"3. ERROR in p1\\Y.java (at line 5)\n" +
		"	public @Nullable String getString(String s1, @NonNull String s2, @NonNull String s3) {\n" +
		"	                                                                 ^^^^^^^^^^^^^^^\n" +
		"Illegal redefinition of parameter s3, inherited method from IY declares this parameter as @Nullable\n" +
		"----------\n");
}
// methods from two super types have different null contracts.
// sub-class does not override, but should to bridge the incompatibility
public void test_parameter_specification_inheritance_014() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
	"p1/IY.java",
			"package p1;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public interface IY {\n" +
			"    public @NonNull String getString1(String s);\n" +
			"    public @NonNull String getString2(String s);\n" +
			"    public String getString3(@Nullable String s);\n" +
			"    public @NonNull String getString4(@Nullable String s);\n" +
			"    public @NonNull String getString5(@Nullable String s);\n" +
			"    public @Nullable String getString6(@NonNull String s);\n" +
			"}\n",
	"p1/X.java",
			"package p1;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    public @Nullable String getString1(String s) {\n" + // incomp. return
			"	     return s;\n" +
			"    }\n" +
			"    public String getString2(String s) {\n" +			 // incomp. return
			"	     return s;\n" +
			"    }\n" +
			"    public String getString3(String s) {\n" +			 // incomp. arg
			"	     return \"\";\n" +
			"    }\n" +
			"    public @NonNull String getString4(@Nullable String s) {\n" +
			"	     return \"\";\n" +
			"    }\n" +
			"    public @NonNull String getString5(@NonNull String s) {\n" + // incomp. arg
			"	     return s;\n" +
			"    }\n" +
			"    public @NonNull String getString6(@Nullable String s) {\n" +
			"	     return \"\";\n" +
			"    }\n" +
			"}\n",
	"p1/Y.java",
			"package p1;\n" +
			"public class Y extends X implements IY {\n" +
			"}\n",
		},
		customOptions,
		"----------\n" +
		"1. ERROR in p1\\Y.java (at line 2)\n" +
		"	public class Y extends X implements IY {\n" +
		"	             ^\n" +
		"The method getString1(String) from X cannot implement the corresponding method from IY due to incompatible nullness constraints\n" +
		"----------\n" +
		"2. ERROR in p1\\Y.java (at line 2)\n" +
		"	public class Y extends X implements IY {\n" +
		"	             ^\n" +
		"The method getString2(String) from X cannot implement the corresponding method from IY due to incompatible nullness constraints\n" +
		"----------\n" +
		"3. ERROR in p1\\Y.java (at line 2)\n" +
		"	public class Y extends X implements IY {\n" +
		"	             ^\n" +
		"The method getString5(String) from X cannot implement the corresponding method from IY due to incompatible nullness constraints\n" +
		"----------\n" +
		"4. ERROR in p1\\Y.java (at line 2)\n" +
		"	public class Y extends X implements IY {\n" +
		"	             ^\n" +
		"The method getString3(String) from X cannot implement the corresponding method from IY due to incompatible nullness constraints\n" +
		"----------\n");
}
// a method relaxes the parameter null specification from @NonNull to un-annotated
// see https://bugs.eclipse.org/381443
public void test_parameter_specification_inheritance_015() {
	runConformTest(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    void foo(@NonNull String s) { System.out.println(s); }\n" +
			"}\n",
			"XSub.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class XSub extends X {\n" +
			"    public void foo(String s) { if (s != null) super.foo(s); }\n" +
			"    void bar() { foo(null); }\n" +
			"}\n"
		},
		"",
	    this.LIBS,
	    false/*shouldFlush*/,
	    null/*vmArgs*/);
}

// a method relaxes the parameter null specification from @NonNull to un-annotated
// see https://bugs.eclipse.org/381443
// issue configured as error
public void test_parameter_specification_inheritance_016() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_NONNULL_PARAMETER_ANNOTATION_DROPPED, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    void foo(@NonNull String s) { System.out.println(s); }\n" +
			"}\n",
			"XSub.java",
			"public class XSub extends X {\n" +
			"    @Override\n" +
			"    public void foo(String s) { if (s != null) super.foo(s); }\n" +
			"    void bar() { foo(null); }\n" +
			"}\n"
		},
		options,
		"----------\n" +
		"1. ERROR in XSub.java (at line 3)\n" +
		"	public void foo(String s) { if (s != null) super.foo(s); }\n" +
		"	                ^^^^^^\n" +
		"Missing non-null annotation: inherited method from X declares this parameter as @NonNull\n" +
		"----------\n");
}

// a class inherits two methods with different spec: one non-null param & one unannotated param
// widening reported as warning by default
// see https://bugs.eclipse.org/381443
public void test_parameter_specification_inheritance_017() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public void foo(String s) { System.out.println(s); }\n" +
			"}\n",
			"IX.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public interface IX {\n" +
			"    void foo(@NonNull String s);\n" +
			"}\n",
			"XSub.java",
			"public class XSub extends X implements IX {\n" +
			"    void bar() { foo(null); }\n" +
			"    static void zork(XSub sub) {\n" +
			"        sub.foo(null);\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in XSub.java (at line 1)\n" + 
		"	public class XSub extends X implements IX {\n" + 
		"	             ^^^^\n" + 
		"Missing non-null annotation: inherited method from IX declares this parameter as @NonNull\n" + 
		"----------\n");
}

// a class inherits two methods with different spec: one non-null param & one unannotated param
// opt to accept this widening
// see https://bugs.eclipse.org/381443
public void test_parameter_specification_inheritance_018() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_NONNULL_PARAMETER_ANNOTATION_DROPPED, JavaCore.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public void foo(String s) { System.out.println(s); }\n" +
			"}\n",
			"IX.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public interface IX {\n" +
			"    void foo(@NonNull String s);\n" +
			"}\n",
			"XSub.java",
			"public class XSub extends X implements IX {\n" +
			"    void bar() { foo(null); }\n" +
			"    static void zork(XSub sub) {\n" +
			"        sub.foo(null);\n" +
			"    }\n" +
			"}\n"
		},
		options,
		"");
}

// a nullable return value is dereferenced without a check
public void test_nullable_return_001() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @Nullable Object getObject() { return null; }\n" +
			"    void foo() {\n" +
			"        Object o = getObject();\n" +
			"        System.out.print(o.toString());\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	System.out.print(o.toString());\n" +
		"	                 ^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n");
}
// a nullable return value is dereferenced without a check, method is read from .class file
public void test_nullable_return_002() {
	runConformTestWithLibs(
		new String[] {
			"Lib.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Lib {\n" +
			"    @Nullable Object getObject() { return null; }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"");
	runNegativeTestWithLibs(
		false, // don't flush
		new String[] {
			"X.java",
			"public class X {\n" +
			"    void foo(Lib l) {\n" +
			"        Object o = l.getObject();\n" +
			"        System.out.print(o.toString());\n" +
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	System.out.print(o.toString());\n" +
		"	                 ^\n" +
		"Potential null pointer access: The variable o may be null at this location\n" +
		"----------\n");
}
// a non-null return value is checked for null, method is read from .class file
public void test_nonnull_return_001() {
	runConformTestWithLibs(
		new String[] {
			"Lib.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Lib {\n" +
			"    @NonNull Object getObject() { return new Object(); }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"");
	runNegativeTestWithLibs(
		false, // don't flush
		new String[] {
			"X.java",
			"public class X {\n" +
			"    void foo(Lib l) {\n" +
			"        Object o = l.getObject();\n" +
			"        if (o != null)\n" +
			"            System.out.print(o.toString());\n" +
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	if (o != null)\n" +
		"	    ^\n" +
		"Redundant null check: The variable o cannot be null at this location\n" +
		"----------\n");
}
// a non-null method returns null
public void test_nonnull_return_003() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @NonNull Object getObject(boolean b) {\n" +
			"        if (b)\n" +
			"            return null;\n" + // definite specification violation despite enclosing "if"
			"        return new Object();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	return null;\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" +
		"----------\n");
}
// a non-null method potentially returns null
public void test_nonnull_return_004() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @NonNull Object getObject(@Nullable Object o) {\n" +
			"        return o;\n" + // 'o' is only potentially null
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	return o;\n" +
		"	       ^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is specified as @Nullable\n" +
		"----------\n");
}
// a non-null method returns its non-null argument
public void test_nonnull_return_005() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @NonNull Object getObject(@NonNull Object o) {\n" +
			"        return o;\n" +
			"    }\n" +
			"}\n"
		},
		null, // options
		"");
}
//a non-null method has insufficient nullness info for its return value
public void test_nonnull_return_006() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @NonNull Object getObject(Object o) {\n" +
			"        return o;\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	return o;\n" +
		"	       ^\n" +
		"Null type safety: The expression of type Object needs unchecked conversion to conform to \'@NonNull Object\'\n" +
		"----------\n");
}
// a result from a nullable method is directly dereferenced
public void test_nonnull_return_007() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @Nullable Object getObject() {\n" +
			"        return null;\n" +
			"    }\n" +
			"    void test() {\n" +
			"        getObject().toString();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	getObject().toString();\n" +
		"	^^^^^^^^^^^\n" +
		"Potential null pointer access: The method getObject() may return null\n" +
		"----------\n");
}
// a result from a nonnull method is directly checked for null: redundant
public void test_nonnull_return_008() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @NonNull Object getObject() {\n" +
			"        return new Object();\n" +
			"    }\n" +
			"    void test() {\n" +
			"        if (getObject() == null)\n" +
			"		     throw new RuntimeException();\n" +
			"    }\n" +
			"}\n"
		},
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	if (getObject() == null)\n" +
		"	    ^^^^^^^^^^^\n" +
		"Null comparison always yields false: The method getObject() cannot return null\n" +
		"----------\n" + 
		"2. WARNING in X.java (at line 8)\n" + 
		"	throw new RuntimeException();\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n");
}
// a result from a nonnull method is directly checked for null (from local): redundant
public void test_nonnull_return_009() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @NonNull Object getObject() {\n" +
			"        return new Object();\n" +
			"    }\n" +
			"    void test() {\n" +
			"        Object left = null;\n" +
			"        if (left != getObject())\n" +
			"		     throw new RuntimeException();\n" +
			"    }\n" +
			"}\n"
		},
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	if (left != getObject())\n" +
		"	    ^^^^\n" +
		"Redundant null check: The variable left can only be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	if (left != getObject())\n" +
		"	            ^^^^^^^^^^^\n" +
		"Redundant null check: The method getObject() cannot return null\n" +
		"----------\n");
}
// a result from a nonnull method is directly checked for null (from local): not redundant due to loop
public void test_nonnull_return_009a() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @NonNull Object getObject() {\n" +
			"        return new Object();\n" +
			"    }\n" +
			"    void test() {\n" +
			"        Object left = null;\n" +
			"        for (int i=0; i<3; i++) {\n" +
			"            if (left != getObject())\n" +
			"	    	     throw new RuntimeException();\n" +
			"            left = new Object();\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		customOptions,
		"");
}
// a result from a nonnull method is directly checked for null (from local): redundant despite loop
// disabled because only one of two desirable errors is raised
// need to integrate @NonNull expressions (MessageSend and more) into deferred analysis by FlowContext
public void _test_nonnull_return_009b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @NonNull Object getObject() {\n" +
			"        return new Object();\n" +
			"    }\n" +
			"    void test() {\n" +
			"        Object left = null;\n" +
			"        for (int i=0; i<3; i++) {\n" +
			"            if (left != getObject())\n" +
			"	    	     throw new RuntimeException();\n" +
			"            // left remains null\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	if (left != getObject())\n" +
		"	    ^^^^\n" +
		"Redundant null check: The variable left can only be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	if (left != getObject())\n" +
		"	            ^^^^^^^^^^^\n" +
		"Redundant null check: The method getObject() cannot return null\n" +
		"----------\n");
}
// a result from a nullable method is assigned and checked for null (from local): not redundant
// see also Bug 336428 - [compiler][null] bogus warning "redundant null check" in condition of do {} while() loop
public void test_nonnull_return_010() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @Nullable X getX() {\n" +
			"        return new X();\n" +
			"    }\n" +
			"    void test() {\n" +
			"        X left = this;\n" +
			"        do {\n" +
			"            if (left == null) \n" +
			"	   	         throw new RuntimeException();\n" +
			"        } while ((left = left.getX()) != null);\n" + // no warning/error here!
			"    }\n" +
			"}\n"
		},
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	if (left == null) \n" +
		"	    ^^^^\n" +
		"Null comparison always yields false: The variable left cannot be null at this location\n" +
		"----------\n");
}
// a non-null method returns a checked-for null value, but that branch is dead code
public void test_nonnull_return_011() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class X {\n" +
			"    Object getObject(Object dubious) {\n" +
			"        if (dubious == null)\n" + // redundant
			"            return dubious;\n" + // definitely null, but not reported inside dead code
			"        return new Object();\n" +
			"    }\n" +
			"}\n"
		},
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	if (dubious == null)\n" +
		"	    ^^^^^^^\n" +
		"Null comparison always yields false: The variable dubious is specified as @NonNull\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 6)\n" +
		"	return dubious;\n" +
		"	^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n");
}
// a non-null method returns a definite null from a conditional expression
// requires the fix for Bug 354554 - [null] conditional with redundant condition yields weak error message
// TODO(SH): ENABLE!
public void _test_nonnull_return_012() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class X {\n" +
			"    Object getObject(Object dubious) {\n" +
			"        return dubious == null ? dubious : null;\n" +
			"    }\n" +
			"}\n"
		},
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	return dubious == null ? dubious : null;\n" +
		"	       ^^^^^^^\n" +
		"Null comparison always yields false: The variable dubious cannot be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	return dubious == null ? dubious : null;\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" +
		"----------\n");
}
// don't apply any default annotations to return void
public void test_nonnull_return_013() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class X {\n" +
			"    void getObject() {}\n" +
			"}\n",
			"Y.java",
			"public class Y extends X {\n" +
			"    @Override\n" +
			"    void getObject() {}\n" + // don't complain, void takes no (default) annotation
			"}\n"
		},
		customOptions,
		"");
}
// bug 365835: [compiler][null] inconsistent error reporting.
public void test_nonnull_return_014() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.NonNull;\n" + 
			"\n" + 
			"public class X {\n" + 
			"	@NonNull\n" + 
			"	public Object foo(Object x, int y) {\n" + 
			"		@NonNull Object local;\n" + 
			"		while (true) {\n" + 
			"			if (y == 4) {\n" + 
			"				local = x;  // error\n" + 
			"				return x;   // only a warning.\n" + 
			"			}\n" + 
			"			x = null;\n" + 
			"		}\n" + 
			"	}\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 9)\n" + 
		"	local = x;  // error\n" + 
		"	        ^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is inferred as @Nullable\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 10)\n" + 
		"	return x;   // only a warning.\n" + 
		"	       ^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is inferred as @Nullable\n" + 
		"----------\n");
}
// suppress an error regarding null-spec violation
public void test_suppress_001() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_SUPPRESS_OPTIONAL_ERRORS, JavaCore.ENABLED);
	runConformTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"public class X {\n" +
				"    @SuppressWarnings(\"null\")\n" +
				"    @NonNull Object getObject(@Nullable Object o) {\n" +
				"        return o;\n" + // 'o' is only potentially null
				"    }\n" +
				"}\n"
			},
			customOptions,
			"");
}
// mixed use of fully qualified name / explicit import
public void test_annotation_import_001() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
	runConformTestWithLibs(
		new String[] {
			CUSTOM_NULLABLE_NAME,
			CUSTOM_NULLABLE_CONTENT,
			CUSTOM_NONNULL_NAME,
			CUSTOM_NONNULL_CONTENT,
			"Lib.java",
			"public class Lib {\n" +
			"    @org.foo.NonNull Object getObject() { return new Object(); }\n" + 	// FQN
			"}\n",
			"X.java",
			"import org.foo.NonNull;\n" +											// explicit import
			"public class X {\n" +
			"    @NonNull Object getObject(@NonNull Lib l) {\n" +
			"        return l.getObject();\n" +
			"    }\n" +
			"}\n"
		},
		customOptions,
		"");
}

// use of explicit imports throughout
public void test_annotation_import_002() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
	runConformTest(
		new String[] {
			CUSTOM_NULLABLE_NAME,
			CUSTOM_NULLABLE_CONTENT,
			CUSTOM_NONNULL_NAME,
			CUSTOM_NONNULL_CONTENT,
			"Lib.java",
			"import org.foo.NonNull;\n" +
			"public class Lib {\n" +
			"    @NonNull Object getObject() { return new Object(); }\n" +
			"}\n",
			"X.java",
			"import org.foo.NonNull;\n" +
			"public class X {\n" +
			"    @NonNull Object getObject(@org.foo.Nullable String dummy, @NonNull Lib l) {\n" +
			"        Object o = l.getObject();" +
			"        return o;\n" +
			"    }\n" +
			"}\n"
		},
		customOptions,
		"");
}
// explicit import of existing annotation types
// using a Lib without null specifications
public void test_annotation_import_005() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.MayBeNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.MustNotBeNull");
	runNegativeTest(
		true/*shouldFlushOutputDirectory*/,
		new String[] {
			"org/foo/MayBeNull.java",
			"package org.foo;\n" +
			"import java.lang.annotation.*;\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"public @interface MayBeNull {}\n",

			"org/foo/MustNotBeNull.java",
			"package org.foo;\n" +
			"import java.lang.annotation.*;\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"public @interface MustNotBeNull {}\n",

			"Lib.java",
			"public class Lib {\n" +
			"    Object getObject() { return new Object(); }\n" +
			"}\n",
			"X.java",
			"import org.foo.*;\n" +
			"public class X {\n" +
			"    @MustNotBeNull Object getObject(@MustNotBeNull Lib l) {\n" +
			"        return l.getObject();\n" +
			"    }\n" +
			"}\n",

		},
		null /*no libs*/,
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	return l.getObject();\n" +
		"	       ^^^^^^^^^^^^^\n" +
		"Null type safety: The expression of type Object needs unchecked conversion to conform to \'@MustNotBeNull Object\'\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// a non-null method returns a value obtained from an unannotated method, missing annotation types
public void test_annotation_import_006() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.MayBeNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.MustNotBeNull");
	runNegativeTest(
		true/*shouldFlushOutputDirectory*/,
		new String[] {
			"Lib.java",
			"public class Lib {\n" +
			"    Object getObject() { return new Object(); }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"    @MustNotBeNull Object getObject(@MustNotBeNull Lib l) {\n" +
			"        return l.getObject();\n" +
			"    }\n" +
			"}\n"
		},
		null /* no libs */,
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	@MustNotBeNull Object getObject(@MustNotBeNull Lib l) {\n" +
		"	 ^^^^^^^^^^^^^\n" +
		"MustNotBeNull cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	@MustNotBeNull Object getObject(@MustNotBeNull Lib l) {\n" +
		"	                                 ^^^^^^^^^^^^^\n" +
		"MustNotBeNull cannot be resolved to a type\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// a null annotation is illegally used on a class:
public void test_illegal_annotation_001() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNull public class X {\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	@NonNull public class X {\n" +
		"	^^^^^^^^\n" +
		"The annotation @NonNull is disallowed for this location\n" +
		"----------\n",
		this.LIBS,
		false/*shouldFlush*/);
}
// this test has been removed:
// setting default to nullable, default applies to a parameter
// public void test_default_nullness_001()

// a null annotation is illegally defined by its simple name
// disabled because specific error is not currently raised
public void _test_illegal_annotation_002() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "NichtNull");
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"public class X {\n" +
			"}\n"
		},
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public class X {\n" +
		"	^\n" +
		"Cannot use the unqualified name \'NichtNull\' as an annotation name for null specification\n" +
		"----------\n");
}

// a null annotation is illegally used on a void method:
public void test_illegal_annotation_003() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"	@NonNull void foo() {}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	@NonNull void foo() {}\n" +
		"	^^^^^^^^^^^^^\n" +
		"The nullness annotation @NonNull is not applicable for the primitive type void\n" +
		"----------\n",
		this.LIBS,
		false/*shouldFlush*/);
}

// a null annotation is illegally used on a primitive type parameter
public void test_illegal_annotation_004() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"	void foo(@Nullable int i) {}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	void foo(@Nullable int i) {}\n" +
		"	         ^^^^^^^^^^^^^\n" +
		"The nullness annotation @Nullable is not applicable for the primitive type int\n" +
		"----------\n",
		this.LIBS,
		false/*shouldFlush*/);
}

// a null annotation is illegally used on a primitive type local var
public void test_illegal_annotation_005() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"	int foo() {\n" +
			"       @Nullable int i = 3;\n" +
			"       return i;\n" +
			"   }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	@Nullable int i = 3;\n" +
		"	^^^^^^^^^^^^^\n" +
		"The nullness annotation @Nullable is not applicable for the primitive type int\n" +
		"----------\n",
		this.LIBS,
		false/*shouldFlush*/);
}

// a configured annotation type does not exist
// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=186342#c133
public void test_illegal_annotation_006() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "nullAnn.Nullable");
	runNegativeTestWithLibs(
		new String[] {
			"p/Test.java",
			"package p;\n" +
			"import nullAnn.*;  // 1 \n" +
			"\n" +
			"public class Test { \n" +
			"\n" +
			"        void foo(@nullAnn.Nullable  Object o) {   // 2\n" +
			"            o.toString();           \n" +
			"        }\n" +
			"}"
		},
		customOptions,
		"----------\n" +
		"1. ERROR in p\\Test.java (at line 2)\n" +
		"	import nullAnn.*;  // 1 \n" +
		"	       ^^^^^^^\n" +
		"The import nullAnn cannot be resolved\n" +
		"----------\n" +
		"2. ERROR in p\\Test.java (at line 6)\n" +
		"	void foo(@nullAnn.Nullable  Object o) {   // 2\n" +
		"	          ^^^^^^^\n" +
		"nullAnn cannot be resolved to a type\n" +
		"----------\n");
}

// a configured annotation type does not exist
// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=186342#c186
public void test_illegal_annotation_007() {
	Map customOptions = getCompilerOptions();
	runNegativeTestWithLibs(
		new String[] {
			"p/Test.java",
			"package p;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"interface TestInt{\n" +
			"	@NonNull Object foo();\n" +
			"}\n" +
			"\n" +
			"public class Test { \n" +
			"	void bar() {" +
			"		new TestInt() {\n" +
			"        	@org public Object foo() {\n" +
			"        	}\n" +
			"		};\n" +
			"	}\n" +
			"}"
		},
		customOptions,
		"----------\n" + 
		"1. ERROR in p\\Test.java (at line 9)\n" + 
		"	@org public Object foo() {\n" + 
		"	 ^^^\n" + 
		"org cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in p\\Test.java (at line 9)\n" + 
		"	@org public Object foo() {\n" + 
		"	            ^^^^^^\n" + 
		"The return type is incompatible with the @NonNull return from TestInt.foo()\n" + 
		"----------\n");
}

public void test_default_nullness_002() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
// This option currently does nothing:
//	customOptions.put(JavaCore.COMPILER_NONNULL_IS_DEFAULT, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class X {\n" +
			"    Object getObject(@Nullable Object o) {\n" +
			"        return new Object();\n" +
			"    }\n" +
			"}\n",
			"Y.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class Y extends X {\n" +
			"    @Override\n" +
			"    @Nullable Object getObject(Object o) {\n" + // complain illegal return redef and inherited annot is not repeated
			"        return o;\n" +
			"    }\n" +
			"}\n",
		},
		customOptions,
		// main error:
		"----------\n" +
		"1. ERROR in Y.java (at line 5)\n" +
		"	@Nullable Object getObject(Object o) {\n" +
		"	^^^^^^^^^^^^^^^^\n" +
		"The return type is incompatible with the @NonNull return from X.getObject(Object)\n" +
		"----------\n" +
		// additional error:
		"2. ERROR in Y.java (at line 5)\n" +
		"	@Nullable Object getObject(Object o) {\n" +
		"	                           ^^^^^^\n" +
		"Illegal redefinition of parameter o, inherited method from X declares this parameter as @Nullable\n" +
		"----------\n");
}
// package default is non-null
public void test_default_nullness_003() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
	"p1/X.java",
			"package p1;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class X {\n" +
			"    protected Object getObject(@Nullable Object o) {\n" +
			"        return new Object();\n" +
			"    }\n" +
			"}\n",
	"p2/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package p2;\n",
	"p2/Y.java",
			"package p2;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Y extends p1.X {\n" +
			"    @Override\n" +
			"    protected @Nullable Object getObject(@Nullable Object o) {\n" +
			"        bar(o);\n" +
			"        return o;\n" +
			"    }\n" +
			"	 void bar(Object o2) { }\n" + // parameter is nonnull per package default
			"}\n"
		},
		customOptions,
		"----------\n" +
		"1. ERROR in p2\\Y.java (at line 5)\n" +
		"	protected @Nullable Object getObject(@Nullable Object o) {\n" +
		"	          ^^^^^^^^^^^^^^^^\n" +
		"The return type is incompatible with the @NonNull return from X.getObject(Object)\n" +
		"----------\n" +
		"2. ERROR in p2\\Y.java (at line 6)\n" +
		"	bar(o);\n" +
		"	    ^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is specified as @Nullable\n" +
		"----------\n");
}
// package level default is consumed from package-info.class, similarly for type level default
public void test_default_nullness_003a() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
	"p1/X.java",
			"package p1;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class X {\n" +
			"    protected Object getObject(@Nullable Object o) {\n" +
			"        return new Object();\n" +
			"    }\n" +
			"	 protected void bar(Object o2) { }\n" + // parameter is nonnull per type default
			"}\n",
	"p2/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package p2;\n",
			},
			customOptions,
			"");
	// check if default is visible from package-info.class.
	runNegativeTestWithLibs(
		false, // don't flush
		new String[] {
	"p2/Y.java",
			"package p2;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Y extends p1.X {\n" +
			"    @Override\n" +
			"    protected @Nullable Object getObject(@Nullable Object o) {\n" + // can't override inherited default nonnull
			"        bar(o);\n" + // parameter is nonnull in super class's .class file
			"        accept(o);\n" +
			"        return o;\n" +
			"    }\n" +
			"    void accept(Object a) {}\n" + // governed by package level default
			"}\n"
		},
		customOptions,
		"----------\n" +
		"1. ERROR in p2\\Y.java (at line 5)\n" +
		"	protected @Nullable Object getObject(@Nullable Object o) {\n" +
		"	          ^^^^^^^^^^^^^^^^\n" +
		"The return type is incompatible with the @NonNull return from X.getObject(Object)\n" +
		"----------\n" +
		"2. ERROR in p2\\Y.java (at line 6)\n" +
		"	bar(o);\n" +
		"	    ^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is specified as @Nullable\n" +
		"----------\n" +
		"3. ERROR in p2\\Y.java (at line 7)\n" +
		"	accept(o);\n" +
		"	       ^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is specified as @Nullable\n" +
		"----------\n");
}
// same as test_default_nullness_003a, but default-induced annotations are combined with explicit ones (not null related)
public void test_default_nullness_003b() {
	Map customOptions = getCompilerOptions();
	runConformTestWithLibs(
		new String[] {
	"p1/Annot.java",
			"package p1;\n" +
			"import static java.lang.annotation.ElementType.*;\n" +
			"import java.lang.annotation.*;\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"@Target({METHOD,PARAMETER})\n" +
			"public @interface Annot {}\n",
	"p1/X.java",
			"package p1;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class X {\n" +
			"    protected @Annot Object getObject(@Annot @Nullable Object o) {\n" +
			"        return new Object();\n" +
			"    }\n" +
			"	 protected @Annot void bar(@Annot Object o2) { }\n" + // parameter is nonnull per type default
			"}\n",
	"p2/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package p2;\n",
			},
			customOptions,
			"");
	// check if default is visible from package-info.class.
	runNegativeTestWithLibs(
		false, // don't flush
		new String[] {
	"p2/Y.java",
			"package p2;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Y extends p1.X {\n" +
			"    @Override\n" +
			"    protected @Nullable Object getObject(@Nullable Object o) {\n" + // can't override inherited default nonnull
			"        bar(o);\n" + // parameter is nonnull in super class's .class file
			"        accept(o);\n" +
			"        return o;\n" +
			"    }\n" +
			"    void accept(@p1.Annot Object a) {}\n" + // governed by package level default
			"}\n"
		},
		customOptions,
		"----------\n" +
		"1. ERROR in p2\\Y.java (at line 5)\n" +
		"	protected @Nullable Object getObject(@Nullable Object o) {\n" +
		"	          ^^^^^^^^^^^^^^^^\n" +
		"The return type is incompatible with the @NonNull return from X.getObject(Object)\n" +
		"----------\n" +
		"2. ERROR in p2\\Y.java (at line 6)\n" +
		"	bar(o);\n" +
		"	    ^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is specified as @Nullable\n" +
		"----------\n" +
		"3. ERROR in p2\\Y.java (at line 7)\n" +
		"	accept(o);\n" +
		"	       ^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is specified as @Nullable\n" +
		"----------\n");
}
// don't apply type-level default to non-reference type
public void test_default_nullness_004() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
	"p1/X.java",
			"package p1;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class X {\n" +
			"    protected Object getObject(boolean o) {\n" +
			"        return new Object();\n" +
			"    }\n" +
			"}\n",
	"p2/Y.java",
			"package p2;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Y extends p1.X {\n" +
			"    @Override\n" +
			"    protected @NonNull Object getObject(boolean o) {\n" +
			"        return o ? this : new Object();\n" +
			"    }\n" +
			"}\n"
		},
		customOptions,
		"");
}
// package default is non-null
// see also Bug 354536 - compiling package-info.java still depends on the order of compilation units
public void test_default_nullness_005() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
	runNegativeTestWithLibs(
		new String[] {
	"p1/X.java",
			"package p1;\n" +
			"public class X {\n" +
			"    class Inner {" +
			"        protected Object getObject(String s) {\n" +
			"            return null;\n" +
			"        }\n" +
			"    }\n" +
			"}\n",
	"p1/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package p1;\n",
	CUSTOM_NONNULL_NAME,
			CUSTOM_NONNULL_CONTENT
		},
		customOptions,
		"----------\n" +
		"1. ERROR in p1\\X.java (at line 4)\n" +
		"	return null;\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" +
		"----------\n");
}
// package default is non-null, package-info.java read before the annotation type
// compile order: beginToCompile(X.Inner) triggers reading of package-info.java before the annotation type was read
public void test_default_nullness_006() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
	runNegativeTestWithLibs(
		new String[] {
	"p1/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package p1;\n",
	"p1/X.java",
			"package p1;\n" +
			"public class X {\n" +
			"    class Inner {" +
			"        protected Object getObject(String s) {\n" +
			"            return null;\n" +
			"        }\n" +
			"    }\n" +
			"}\n",
	CUSTOM_NONNULL_NAME,
			CUSTOM_NONNULL_CONTENT
		},
		customOptions,
		"----------\n" +
		"1. ERROR in p1\\X.java (at line 4)\n" +
		"	return null;\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" +
		"----------\n");
}
// global default nonnull, but return may be null
// DISABLED due to dysfunctional global default after Bug 366063 - Compiler should not add synthetic @NonNull annotations
public void _test_default_nullness_007() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
//	customOptions.put(JavaCore.COMPILER_NONNULL_IS_DEFAULT, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @Nullable Object dangerous() {\n" +
			"        return null;\n" +
			"    }\n" +
			"    Object broken() {\n" +
			"        return dangerous();\n" +
			"    }\n" +
			"}\n",

		},
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	return dangerous();\n" +
		"	       ^^^^^^^^^^^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is inferred as @Nullable\n" +
		"----------\n");
}

// cancel type level default to comply with super specification
public void test_default_nullness_008() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
	"p1/X.java",
			"package p1;\n" +
			"public class X {\n" +
			"    protected Object getObject(Object o) {\n" +
			"        return new Object();\n" +
			"    }\n" +
			"}\n",
	"p2/Y.java",
			"package p2;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class Y extends p1.X {\n" +
			"    @Override\n" +
			"    @NonNullByDefault(false)\n" +
			"    protected Object getObject(Object o) {\n" +
			"        if (o.toString().length() == 0)\n" + // dereference without a warning
			"	        return null;\n" + // return null without a warning
			"        return o.toString();\n" +
			"    }\n" +
			"}\n"
		},
		customOptions,
		"");
}

// cancel outer type level default to comply with super specification
public void test_default_nullness_009() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
	"p1/X.java",
			"package p1;\n" +
			"public class X {\n" +
			"    protected Object getObject(Object o) {\n" +
			"        return new Object();\n" +
			"    }\n" +
			"}\n",
	"p2/Y.java",
			"package p2;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class Y { \n" +
			"    @NonNullByDefault(false)\n" +
			"    static class Z extends p1.X {\n" +
			"        @Override\n" +
			"        protected Object getObject(Object o) {\n" +
			"            if (o.toString().length() == 0) {\n" +
			"                o = null;\n" + // assign null without a warning
			"                bar(o); // error: arg is declared @NonNull\n" +
			"	             return null;\n" +
			"            }\n" +
			"            return o.toString();\n" +
			"        }\n" +
			"        String bar(@NonNull Object o) {\n" +
			"            return getObject(o).toString();" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		customOptions,
		"----------\n" +
		"1. ERROR in p2\\Y.java (at line 11)\n" +
		"	bar(o); // error: arg is declared @NonNull\n" +
		"	    ^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" +
		"----------\n");
}
// non-null declarations are redundant within a default scope.
public void test_default_nullness_010() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
	"p2/Y.java",
			"package p2;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class Y {\n" +
			"    protected @NonNull Object getObject(@NonNull Object o) {\n" +
			"        return o;\n" +
			"    }\n" +
			"}\n"
		},
		customOptions,
		"----------\n" +
		"1. WARNING in p2\\Y.java (at line 5)\n" +
		"	protected @NonNull Object getObject(@NonNull Object o) {\n" +
		"	          ^^^^^^^^^^^^^^^\n" +
		"The nullness annotation is redundant with a default that applies to this location\n" +
		"----------\n" +
		"2. WARNING in p2\\Y.java (at line 5)\n" +
		"	protected @NonNull Object getObject(@NonNull Object o) {\n" +
		"	                                    ^^^^^^^^^^^^^^^^^\n" +
		"The nullness annotation is redundant with a default that applies to this location\n" +
		"----------\n");
}
// package-info declares nonnull-by-default
// special compile order due to import of type from that package
// cf. https://bugs.eclipse.org/bugs/show_bug.cgi?id=186342#add_comment
public void test_default_nullness_011() {
	runNegativeTestWithLibs(
		new String[] {
			"Main.java",
			"import p1.C;\n" +
			"public class Main {\n" +
			"    void test(@org.eclipse.jdt.annotation.NonNull Object o) {\n" +
			"        o = null;\n" +
			"        new C(null);\n" +
			"    }\n" +
			"}\n",
			"p1/C.java",
			"package p1;\n" +
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"public class C {\n" +
			"    public C (Object o) {}\n" +
			"}\n",
			"p1/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package p1;\n"
		},
		"----------\n" + 
		"1. ERROR in Main.java (at line 4)\n" + 
		"	o = null;\n" + 
		"	    ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n" + 
		"2. ERROR in Main.java (at line 5)\n" + 
		"	new C(null);\n" + 
		"	      ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n" + 
		"----------\n" + 
		"1. WARNING in p1\\C.java (at line 2)\n" + 
		"	@org.eclipse.jdt.annotation.NonNullByDefault\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Nullness default is redundant with a default specified for the enclosing package p1\n" + 
		"----------\n");
}
// Bug 365836 - [compiler][null] Incomplete propagation of null defaults.
public void test_default_nullness_012() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" + 
			"import org.eclipse.jdt.annotation.Nullable;\n" + 
			"\n" + 
			"public class X {\n" + 
			"    @NonNullByDefault \n" + 
			"    public void foo(@Nullable String [] args) {\n" + 
			"        class local {\n" + 
			"            void zoo(Object o) {\n" + 
			"            }\n" + 
			"        };\n" + 
			"        new local().zoo(null); // defaults applying from foo\n" + 
			"    }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 11)\n" + 
		"	new local().zoo(null); // defaults applying from foo\n" + 
		"	                ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n");
}
// Bug 365836 - [compiler][null] Incomplete propagation of null defaults.
public void test_default_nullness_013() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" + 
			"import org.eclipse.jdt.annotation.Nullable;\n" + 
			"\n" +
			"@SuppressWarnings(\"unused\")\n" +
			"public class X {\n" + 
			"    @NonNullByDefault \n" + 
			"    public void foo(@Nullable String [] args) {\n" + 
			"        class local {\n" +
			"            class Deeply {\n" + 
			"                Object zoo() {\n" +
			"                    return null; // defaults applying from foo\n" +
			"                }\n" + 
			"            }\n" + 
			"        };\n" + 
			"    }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 11)\n" + 
		"	return null; // defaults applying from foo\n" + 
		"	       ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n");
}
// bug 367154 - [compiler][null] Problem in propagating null defaults.
public void test_default_nullness_014() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" + 
			"import org.eclipse.jdt.annotation.Nullable;\n" + 
			"\n" + 
			"@SuppressWarnings(\"unused\")\n" + 
			"public class X {\n" + 
			"\n" + 
			"    public void foo(@Nullable String [] args) {\n" + 
			"        @NonNullByDefault\n" + 
			"        class local {\n" + 
			"            class Deeply {\n" + 
			"                Object zoo() {\n" + 
			"                    return null;  // expect error here\n" + 
			"                }\n" + 
			"            }\n" + 
			"        };\n" + 
			"    }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 12)\n" + 
		"	return null;  // expect error here\n" + 
		"	       ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n");
}
// bug 367154 - [compiler][null] Problem in propagating null defaults.
// initializer involved
public void test_default_nullness_015() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" + 
			"import org.eclipse.jdt.annotation.Nullable;\n" + 
			"\n" + 
			"@SuppressWarnings(\"unused\")\n" + 
			"@NonNullByDefault\n" + 
			"public class X {\n" + 
			"    {\n" + 
			"        class local {\n" + 
			"            class Deeply {\n" + 
			"                Object zoo() {\n" + 
			"                    return null;  // expect error here\n" + 
			"                }\n" + 
			"            }\n" + 
			"        };\n" + 
			"    }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 11)\n" + 
		"	return null;  // expect error here\n" + 
		"	       ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n");
}

// default nullness applied to fields, class-level:
public void test_default_nullness_016() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class X {\n" +
			"    Object foo;\n" +
			"    void doFoo() {\n" +
			"        foo = null;\n" +
			"    }\n" +
			"    class Inner {\n" +
			"        Object iFoo;\n" +
			"        void diFoo(@Nullable Object arg) {\n" +
			"            iFoo = arg;\n" +
			"        }\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	Object foo;\n" + 
		"	       ^^^\n" + 
		"The @NonNull field foo may not have been initialized\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 6)\n" + 
		"	foo = null;\n" + 
		"	      ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 9)\n" + 
		"	Object iFoo;\n" + 
		"	       ^^^^\n" + 
		"The @NonNull field iFoo may not have been initialized\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 11)\n" + 
		"	iFoo = arg;\n" + 
		"	       ^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is specified as @Nullable\n" + 
		"----------\n");
}

// default nullness applied to fields, method level applied to local class + redundant annotation
public void test_default_nullness_017() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER, JavaCore.IGNORE);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @NonNullByDefault\n" +
			"    Object doFoo() {\n" +
			"        class Local {\n" +
			"            Object foo;\n" +
			"            @NonNull Object goo;\n" +
			"        };" +
			"        return new Local();\n" +
			"    }\n" +
			"}\n",
		},
		options,
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	Object foo;\n" + 
		"	       ^^^\n" + 
		"The @NonNull field foo may not have been initialized\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 7)\n" + 
		"	@NonNull Object goo;\n" + 
		"	^^^^^^^^^^^^^^^\n" + 
		"The nullness annotation is redundant with a default that applies to this location\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 7)\n" + 
		"	@NonNull Object goo;\n" + 
		"	                ^^^\n" + 
		"The @NonNull field goo may not have been initialized\n" + 
		"----------\n");
}

// redundant default annotations - class vs. inner class
public void test_redundant_annotation_01() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
			"p2/Y.java",
			"package p2;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class Y {\n" +
			"    @NonNullByDefault class Inner {\n" +
			"        @NonNullByDefault class DeepInner {}\n" +
			"    }\n" +
			"    class Inner2 {\n" +
			"        @NonNullByDefault class DeepInner2 {\n" +
			"        }\n" +
			"        void foo() {\n" +
			"            @SuppressWarnings(\"unused\") @NonNullByDefault class Local {}\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			"@NonNullByDefault class V {}\n",
			"p3/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault package p3;\n",
			"p3/Z.java",
			"package p3;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class Z {\n" +
			"}\n" +
			"class X {\n" +
			"    @NonNullByDefault class Inner {}\n" +
			"    class Inner2 {\n" +
			"        @NonNullByDefault class DeepInner {}\n" +
			"    }\n" +
			"}\n"
		},
		customOptions,
		"----------\n" + 
		"1. WARNING in p2\\Y.java (at line 5)\n" +
		"	@NonNullByDefault class Inner {\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing type Y\n" +
		"----------\n" +
		"2. WARNING in p2\\Y.java (at line 6)\n" +
		"	@NonNullByDefault class DeepInner {}\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing type Y.Inner\n" +
		"----------\n" +
		"3. WARNING in p2\\Y.java (at line 9)\n" +
		"	@NonNullByDefault class DeepInner2 {\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing type Y\n" +
		"----------\n" +
		"4. WARNING in p2\\Y.java (at line 12)\n" +
		"	@SuppressWarnings(\"unused\") @NonNullByDefault class Local {}\n" +
		"	                            ^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing type Y\n" +
		"----------\n" +
		"----------\n" +
		"1. WARNING in p3\\Z.java (at line 3)\n" +
		"	@NonNullByDefault\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing package p3\n" +
		"----------\n" +
		"2. WARNING in p3\\Z.java (at line 7)\n" +
		"	@NonNullByDefault class Inner {}\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing package p3\n" +
		"----------\n" +
		"3. WARNING in p3\\Z.java (at line 9)\n" +
		"	@NonNullByDefault class DeepInner {}\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing package p3\n" +
		"----------\n");
}

// redundant default annotations - class vs. method
public void test_redundant_annotation_02() {
	Map customOptions = getCompilerOptions();
	runConformTestWithLibs(
		new String[] {
			"p2/Y.java",
			"package p2;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class Y {\n" +
			"    @NonNullByDefault void foo() {}\n" +
			"}\n" +
			"class Z {\n" +
			"    @NonNullByDefault void bar() {\n" +
			"         @NonNullByDefault @SuppressWarnings(\"unused\") class Zork {\n" +
			"             @NonNullByDefault void fubar() {}\n" +
			"         }\n" +
			"    }\n" +
			"    @NonNullByDefault void zink() {\n" +
			"         @SuppressWarnings(\"unused\") class Bork {\n" +
			"             @NonNullByDefault void jubar() {}\n" +
			"         }\n" +
			"    }\n" +
			"}\n"
		},
		customOptions,
		"----------\n" +
		"1. WARNING in p2\\Y.java (at line 5)\n" +
		"	@NonNullByDefault void foo() {}\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing type Y\n" +
		"----------\n" +
		"2. WARNING in p2\\Y.java (at line 9)\n" +
		"	@NonNullByDefault @SuppressWarnings(\"unused\") class Zork {\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing method bar()\n" +
		"----------\n" +
		"3. WARNING in p2\\Y.java (at line 10)\n" +
		"	@NonNullByDefault void fubar() {}\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing type Zork\n" +
		"----------\n" +
		"4. WARNING in p2\\Y.java (at line 15)\n" +
		"	@NonNullByDefault void jubar() {}\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Nullness default is redundant with a default specified for the enclosing method zink()\n" +
		"----------\n");
}

//redundant default annotations - class vs. method - generics
public void test_redundant_annotation_02g() {
	Map customOptions = getCompilerOptions();
	runConformTestWithLibs(
		new String[] {
			"p2/Y.java",
			"package p2;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class Y<TY> {\n" +
			"    @NonNullByDefault <TF> void foo(TF arg) {}\n" +
			"}\n" +
			"class Z {\n" +
			"    @NonNullByDefault <TB> void bar() {\n" +
			"         @NonNullByDefault @SuppressWarnings(\"unused\") class Zork {\n" +
			"             @NonNullByDefault void fubar(TB arg) {}\n" +
			"         }\n" +
			"    }\n" +
			"}\n"
		},
		customOptions,
		"----------\n" + 
		"1. WARNING in p2\\Y.java (at line 5)\n" + 
		"	@NonNullByDefault <TF> void foo(TF arg) {}\n" + 
		"	^^^^^^^^^^^^^^^^^\n" + 
		"Nullness default is redundant with a default specified for the enclosing type Y<TY>\n" + 
		"----------\n" + 
		"2. WARNING in p2\\Y.java (at line 9)\n" + 
		"	@NonNullByDefault @SuppressWarnings(\"unused\") class Zork {\n" + 
		"	^^^^^^^^^^^^^^^^^\n" + 
		"Nullness default is redundant with a default specified for the enclosing method bar()\n" + 
		"----------\n" + 
		"3. WARNING in p2\\Y.java (at line 10)\n" + 
		"	@NonNullByDefault void fubar(TB arg) {}\n" + 
		"	^^^^^^^^^^^^^^^^^\n" + 
		"Nullness default is redundant with a default specified for the enclosing type Zork\n" + 
		"----------\n");
}

// test missing default nullness annotation for types in default package
public void test_missing_default_annotation_01() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_MISSING_NONNULL_BY_DEFAULT_ANNOTATION, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"Lib.java",
			"public class Lib {\n" +
			"    Object getObject() { return new Object(); }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"	 class XInner{}\n" +  // don't warn for inner types
			"    Object getObject(Lib l) {\n" +
			"        return l.getObject();\n" +
			"    }\n" +
			"}\n"
		},
		customOptions,
		"----------\n" + 
		"1. ERROR in Lib.java (at line 1)\n" + 
		"	public class Lib {\n" + 
		"	             ^^^\n" + 
		"A default nullness annotation has not been specified for the type Lib\n" + 
		"----------\n" + 
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X {\n" + 
		"	             ^\n" + 
		"A default nullness annotation has not been specified for the type X\n" + 
		"----------\n");
}

// test missing default nullness annotation for a package with package-info
public void test_missing_default_annotation_02() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_MISSING_NONNULL_BY_DEFAULT_ANNOTATION, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"p2/package-info.java",
			"package p2;\n",
			"p2/Y.java",
			"package p2;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class Y {\n" +
			"   void foo() {}\n" +
			"}\n",
			"p3/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault package p3;\n",
			"p3/Z.java",
			"package p3;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Z {\n" +
			"    @NonNullByDefault void bar() {}\n" +
			"}\n",
		},
		customOptions,
		"----------\n" + 
		"1. ERROR in p2\\package-info.java (at line 1)\n" + 
		"	package p2;\n" + 
		"	        ^^\n" + 
		"A default nullness annotation has not been specified for the package p2\n" + 
		"----------\n" + 
		"----------\n" + 
		"1. WARNING in p3\\Z.java (at line 4)\n" + 
		"	@NonNullByDefault void bar() {}\n" + 
		"	^^^^^^^^^^^^^^^^^\n" + 
		"Nullness default is redundant with a default specified for the enclosing package p3\n" + 
		"----------\n");
}

// redundant default annotations - class vs. inner class
// ensure that disabling null annotations also disables this diagnostic
public void test_redundant_annotation_04() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.DISABLED);
	runConformTestWithLibs(
		new String[] {
			"p2/Y.java",
			"package p2;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class Y {\n" +
			"    @NonNullByDefault class Inner {\n" +
			"        @NonNullByDefault class DeepInner {}\n" +
			"    }\n" +
			"    class Inner2 {\n" +
			"        @NonNullByDefault class DeepInner2 {\n" +
			"        }\n" +
			"        @NonNullByDefault void foo(@Nullable @NonNull Object arg) {\n" +
			"            @NonNullByDefault class Local {}\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			"@NonNullByDefault class V {}\n",
			"p3/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault package p3;\n",
			"p3/Z.java",
			"package p3;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class Z {\n" +
			"}\n" +
			"class X {\n" +
			"    @NonNullByDefault class Inner {}\n" +
			"    class Inner2 {\n" +
			"        @NonNullByDefault class DeepInner {}\n" +
			"    }\n" +
			"}\n"
		},
		customOptions,
		"");
}

// contradictory null annotations
public void test_contradictory_annotations_01() {
	Map customOptions = getCompilerOptions();
	runNegativeTestWithLibs(
		new String[] {
			"p2/Y.java",
			"package p2;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Y {\n" +
			"    void foo(@NonNull @Nullable Object o) {}\n" +
			"    @Nullable @NonNull Object bar() {\n" +
			"        @NonNull @Nullable Object o = null;\n" +
			"        return o;\n" +
			"    }\n" +
			"}\n" +
			"class Z {\n" +
			"    @NonNullByDefault void bar() {}\n" +
			"}\n"
		},
		customOptions,
		"----------\n" + 
		"1. ERROR in p2\\Y.java (at line 4)\n" + 
		"	void foo(@NonNull @Nullable Object o) {}\n" + 
		"	                  ^^^^^^^^^\n" + 
		"Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location\n" + 
		"----------\n" + 
		"2. ERROR in p2\\Y.java (at line 5)\n" + 
		"	@Nullable @NonNull Object bar() {\n" + 
		"	          ^^^^^^^^\n" + 
		"Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location\n" + 
		"----------\n" + 
		"3. ERROR in p2\\Y.java (at line 6)\n" + 
		"	@NonNull @Nullable Object o = null;\n" + 
		"	         ^^^^^^^^^\n" + 
		"Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location\n" + 
		"----------\n");
}

// contradictory null annotations on a field
public void test_contradictory_annotations_02() {
	Map customOptions = getCompilerOptions();
	runNegativeTestWithLibs(
		new String[] {
			"p2/Y.java",
			"package p2;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Y {\n" +
			"    @NonNull @Nullable Object o;\n" +
			"}\n"
		},
		customOptions,
		"----------\n" + 
		"1. ERROR in p2\\Y.java (at line 4)\n" + 
		"	@NonNull @Nullable Object o;\n" + 
		"	         ^^^^^^^^^\n" + 
		"Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location\n" + 
		"----------\n");
}

// a nonnull variable is dereferenced in a loop
public void test_nonnull_var_in_constrol_structure_1() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
// This option currently does nothing:
//	customOptions.put(JavaCore.COMPILER_NONNULL_IS_DEFAULT, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class X {\n" +
			"    void print4(@NonNull String s) {\n" +
			"        for (int i=0; i<4; i++)\n" +
			"             print(s);\n" +
			"    }\n" +
			"    void print5(@Nullable String s) {\n" +
			"        for (int i=0; i<5; i++)\n" +
			"             print(s);\n" +
			"    }\n" +
			"    void print6(boolean b) {\n" +
			"        String s = b ? null : \"\";\n" +
			"        for (int i=0; i<5; i++)\n" +
			"             print(s);\n" +
			"    }\n" +
			"    void print(@NonNull String s) {\n" +
			"        System.out.print(s);\n" +
			"    }\n" +
			"}\n",

		},
		customOptions,
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	void print4(@NonNull String s) {\n" +
		"	            ^^^^^^^^^^^^^^^^^\n" +
		"The nullness annotation is redundant with a default that applies to this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	print(s);\n" +
		"	      ^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is specified as @Nullable\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 15)\n" +
		"	print(s);\n" +
		"	      ^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is inferred as @Nullable\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 17)\n" +
		"	void print(@NonNull String s) {\n" +
		"	           ^^^^^^^^^^^^^^^^^\n" +
		"The nullness annotation is redundant with a default that applies to this location\n" +
		"----------\n");
}
// a nonnull variable is dereferenced in a finally block
public void test_nonnull_var_in_constrol_structure_2() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
// This option currently does nothing:
//	customOptions.put(JavaCore.COMPILER_NONNULL_IS_DEFAULT, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;" +
			"@NonNullByDefault\n" +
			"public class X {\n" +
			"    void print4(String s) {\n" +
			"        try { /*empty*/ } finally {\n" +
			"             print(s);\n" +
			"        }\n" +
			"    }\n" +
			"    void print5(@Nullable String s) {\n" +
			"        try { /*empty*/ } finally {\n" +
			"             print(s);\n" +
			"        }\n" +
			"    }\n" +
			"    void print6(boolean b) {\n" +
			"        String s = b ? null : \"\";\n" +
			"        try { /*empty*/ } finally {\n" +
			"             print(s);\n" +
			"        }\n" +
			"    }\n" +
			"    void print(String s) {\n" +
			"        System.out.print(s);\n" +
			"    }\n" +
			"}\n",

		},
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	print(s);\n" +
		"	      ^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is specified as @Nullable\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 16)\n" +
		"	print(s);\n" +
		"	      ^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is inferred as @Nullable\n" +
		"----------\n");
}
// a nonnull variable is dereferenced in a finally block inside a loop
public void test_nonnull_var_in_constrol_structure_3() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_ANNOTATION, JavaCore.IGNORE);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    void print4(@NonNull String s) {\n" +
			"        for (int i=0; i<4; i++)\n" +
			"            try { /*empty*/ } finally {\n" +
			"                 print(s);\n" +
			"            }\n" +
			"    }\n" +
			"    void print5(@Nullable String s) {\n" +
			"        for (int i=0; i<5; i++)\n" +
			"            try { /*empty*/ } finally {\n" +
			"                 print(s);\n" +
			"            }\n" +
			"    }\n" +
			"    void print6(boolean b) {\n" +
			"        String s = b ? null : \"\";\n" +
			"        for (int i=0; i<4; i++)\n" +
			"            try { /*empty*/ } finally {\n" +
			"                 print(s);\n" +
			"            }\n" +
			"    }\n" +
			"    void print(@NonNull String s) {\n" +
			"        System.out.print(s);\n" +
			"    }\n" +
			"}\n",

		},
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	print(s);\n" +
		"	      ^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is specified as @Nullable\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 19)\n" +
		"	print(s);\n" +
		"	      ^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is inferred as @Nullable\n" +
		"----------\n");
}
// witness for an AIOOBE in FlowContext.recordExpectedType()
public void test_message_send_in_control_structure_01() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.IGNORE);
	customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.WARNING);
	runNegativeTestWithLibs(
		new String[] {
			"p/Scope.java",
			"package p;\n" +
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"public abstract class Scope {\n" +
			"	public ReferenceBinding findMemberType(char[] typeName, ReferenceBinding enclosingType) {\n" +
			"		ReferenceBinding enclosingSourceType = enclosingSourceType();\n" +
			"		PackageBinding currentPackage = getCurrentPackage();\n" +
			"		CompilationUnitScope unitScope = compilationUnitScope();\n" +
			"		ReferenceBinding memberType = enclosingType.getMemberType(typeName);\n" +
			"		ReferenceBinding currentType = enclosingType;\n" +
			"		ReferenceBinding[] interfacesToVisit = null;\n" +
			"		while (true) {\n" +
			"			ReferenceBinding[] itsInterfaces = currentType.superInterfaces();\n" +
			"			if (itsInterfaces != null) {\n" +
			"				if (interfacesToVisit == null) {\n" +
			"					interfacesToVisit = itsInterfaces;\n" +
			"				}\n" +
			"			}\n" +
			"			unitScope.recordReference(currentType, typeName);\n" +
			"			\n" +
			"			if ((memberType = currentType.getMemberType(typeName)) != null) {\n" +
			"				if (enclosingSourceType == null\n" +
			"					? memberType.canBeSeenBy(currentPackage)\n" +
			"					: memberType.canBeSeenBy(enclosingType, enclosingSourceType)) {\n" +
			"						return memberType;\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	private CompilationUnitScope compilationUnitScope() {\n" +
			"		return compilationUnitScope();\n" +
			"	}\n" +
			"	private PackageBinding getCurrentPackage() {\n" +
			"		return getCurrentPackage();\n" +
			"	}\n" +
			"	private ReferenceBinding enclosingSourceType() {\n" +
			"		return enclosingSourceType();\n" +
			"	}\n" +
			"}\n",
			"p/CompilationUnitScope.java",
			"package p;\n" +
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"public class CompilationUnitScope {\n" +
			"    void recordReference(ReferenceBinding rb, char[] name) {}\n" +
			"}\n",
			"p/PackageBinding.java",
			"package p;\n" +
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"public class PackageBinding {\n" +
			"}\n",
			"p/ReferenceBinding.java",
			"package p;\n" +
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"public class ReferenceBinding {\n" +
			"    ReferenceBinding getMemberType(char[] name) { return this; }\n" +
			"    ReferenceBinding[] superInterfaces() { return new ReferenceBinding[0]; }\n" +
			"    boolean canBeSeenBy(PackageBinding ob) { return true; }\n" +
			"    boolean canBeSeenBy(ReferenceBinding rb, ReferenceBinding rb2) { return true; }\n" +
			"}\n"
		},
		customOptions,
		"----------\n" +
		"1. ERROR in p\\Scope.java (at line 13)\n" +
		"	if (itsInterfaces != null) {\n" +
		"	    ^^^^^^^^^^^^^\n" +
		"Redundant null check: The variable itsInterfaces cannot be null at this location\n" +
		"----------\n" +
		"2. ERROR in p\\Scope.java (at line 20)\n" +
		"	if ((memberType = currentType.getMemberType(typeName)) != null) {\n" +
		"	    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Redundant null check: The variable memberType cannot be null at this location\n" +
		"----------\n" +
		"3. ERROR in p\\Scope.java (at line 21)\n" +
		"	if (enclosingSourceType == null\n" +
		"	    ^^^^^^^^^^^^^^^^^^^\n" +
		"Null comparison always yields false: The variable enclosingSourceType cannot be null at this location\n" +
		"----------\n");
}

// Bug 370930 - NonNull annotation not considered for enhanced for loops
public void test_message_send_in_control_structure_02() {
	runNegativeTestWithLibs(
		new String[] {
			"Bug370930.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"import java.util.*;\n" +
			"public class Bug370930 {\n" +
			"	void loop(Collection<String> list) {\n" + 
			"		for(@NonNull String s: list) { // warning here: insufficient info on elements\n" + 
			"			expectNonNull(s); // no warning here\n" + 
			"		}\n" + 
			"	}\n" + 
			"	\n" + 
			"	void expectNonNull(@NonNull String s) {}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in Bug370930.java (at line 5)\n" + 
		"	for(@NonNull String s: list) { // warning here: insufficient info on elements\n" + 
		"	                       ^^^^\n" + 
		"Null type safety: The expression of type String needs unchecked conversion to conform to \'@NonNull String\'\n" + 
		"----------\n");
}
//Bug 370930 - NonNull annotation not considered for enhanced for loops over array
public void test_message_send_in_control_structure_02a() {
	runNegativeTestWithLibs(
		new String[] {
			"Bug370930.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Bug370930 {\n" +
			"	void loop(String[] array) {\n" + 
			"		for(@NonNull String s: array) { // warning here: insufficient info on elements\n" + 
			"			expectNonNull(s); // no warning here\n" + 
			"		}\n" + 
			"	}\n" + 
			"	\n" + 
			"	void expectNonNull(@NonNull String s) {}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in Bug370930.java (at line 4)\n" + 
		"	for(@NonNull String s: array) { // warning here: insufficient info on elements\n" + 
		"	                       ^^^^^\n" + 
		"Null type safety: The expression of type String needs unchecked conversion to conform to \'@NonNull String\'\n" + 
		"----------\n");
}
//Bug 370930 - NonNull annotation not considered for enhanced for loops
public void test_message_send_in_control_structure_03() {
	runNegativeTestWithLibs(
		new String[] {
			"Bug370930.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"import java.util.*;\n" +
			"public class Bug370930 {\n" +
			"	void loop(Collection<String> list) {\n" + 
			"		for(@Nullable String s: list) {\n" + 
			"			expectNonNull(s); // warning here\n" + 
			"		}\n" + 
			"	}\n" + 
			"	\n" + 
			"	void expectNonNull(@NonNull String s) {}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in Bug370930.java (at line 6)\n" + 
		"	expectNonNull(s); // warning here\n" + 
		"	              ^\n" + 
		"Null type mismatch: required \'@NonNull String\' but the provided value is specified as @Nullable\n" + 
		"----------\n");
}
public void test_assignment_expression_1() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"	@Nullable Object foo() {\n" +
			"		Object o = null;\n" +
			"		boolean keepLooking = true;\n" +
			"		while(keepLooking) {\n" +
			"			if ((o=getO()) != null) {\n" +
			"				return o;\n" +
			"			}\n" +
			"		}\n" +
			"		return null;\n" +
			"	}\n" +
			"\n" +
			"	private @Nullable Object getO() {\n" +
			"		return new Object();\n" +
			"	}\n" +
			"}\n",

		},
		customOptions,
		"");	
}
// a nonnull variable is dereferenced in a method of a nested type
public void test_nesting_1() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
// This option currently does nothing:
//	customOptions.put(JavaCore.COMPILER_NONNULL_IS_DEFAULT, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class X {\n" +
			"    void print4(final String s1) {\n" +
			"        for (int i=0; i<3; i++)\n" +
			"            new Runnable() {\n" +
			"                public void run() {\n" +
			"                     print(s1);\n" +
			"                }\n" +
			"            }.run();\n" +
			"    }\n" +
			"    void print8(final @Nullable String s2) {\n" +
			"        for (int i=0; i<3; i++)\n" +
			"            new Runnable() {\n" +
			"                public void run() {\n" +
			"                     print(s2);\n" +
			"                }\n" +
			"            }.run();\n" +
			"    }\n" +
			"    void print16(boolean b) {\n" +
			"        final String s3 = b ? null : \"\";\n" +
			"        for (int i=0; i<3; i++)\n" +
			"            new Runnable() {\n" +
			"                public void run() {\n" +
			"                     @NonNull String s3R = s3;\n" +
			"                }\n" +
			"            }.run();\n" +
			"    }\n" +
			"    void print(String s) {\n" +
			"        System.out.print(s);\n" +
			"    }\n" +
			"}\n",

		},
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 16)\n" +
		"	print(s2);\n" +
		"	      ^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is specified as @Nullable\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 25)\n" +
		"	@NonNull String s3R = s3;\n" +
		"	                      ^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is inferred as @Nullable\n" +
		"----------\n");
}
// Test a regression incurred to the OT/J based implementation
// by the fix in Bug 360328 - [compiler][null] detect null problems in nested code (local class inside a loop)
public void test_constructor_with_nested_class() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    final Object o1;\n" +
			"    final Object o2;\n" +
			"    public X() {\n" +
			"         this.o1 = new Object() {\n" +
			"             public String toString() { return \"O1\"; }\n" +
			"         };\n" +
			"         this.o2 = new Object();" +
			"    }\n" +
			"}\n"
		},
		"");
}
// test analysis disablement, binary type contains annotation
public void test_options_01() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runConformTestWithLibs(
			new String[] {
				"ContainingInner2.java",
				"public class ContainingInner2 {\n" + 
				"    public ContainingInner2 (@org.eclipse.jdt.annotation.NonNull Object o) {\n" + 
				"    }\n" + 
				"    public class Inner {\n" + 
				"        public <T> Inner (@org.eclipse.jdt.annotation.NonNull T o) {\n" + 
				"        }\n" + 
				"    }\n" + 
				"}\n",
			},
			null /*customOptions*/,
			"");
	customOptions.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.DISABLED);
	runConformTestWithLibs(
		false, // flush directory
		new String[] {
			"X.java",
			"public class X {\n" +
			"	 void create() {\n" +
			"          ContainingInner2 container = new ContainingInner2(null);\n" +
			"	       ContainingInner2.Inner inner = container.new Inner(null);\n" +
			"    }\n" +
		  	"}\n"},
		customOptions,
		""  /* compiler output */);
}
// test illegally trying to ignore null spec violations
public void test_options_02() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, JavaCore.IGNORE); // has no effect
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"    public void foo(@org.eclipse.jdt.annotation.NonNull Object o) {\n" +
			"        o = null;\n" +
			"        Object p = o;\n" +
			"        if (p == null)\n" +
			"            p.toString();\n" +
			"    }\n" + 
			"}\n",
		},
		customOptions,
		"----------\n" + 
		"1. ERROR in Test.java (at line 3)\n" + 
		"	o = null;\n" + 
		"	    ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n" + 
		"2. ERROR in Test.java (at line 5)\n" + 
		"	if (p == null)\n" + 
		"	    ^\n" + 
		"Null comparison always yields false: The variable p cannot be null at this location\n" + 
		"----------\n" + 
		"3. WARNING in Test.java (at line 6)\n" + 
		"	p.toString();\n" + 
		"	^^^^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n");
}
// test setting null spec violations to "warning"
public void test_options_03() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, JavaCore.WARNING); // OK
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"    public void foo(@org.eclipse.jdt.annotation.NonNull Object o) {\n" +
			"        o = null;\n" +
			"        Object p = o;\n" +
			"        if (p == null)\n" +
			"            p.toString();\n" +
			"    }\n" + 
			"}\n",
		},
		customOptions,
		"----------\n" + 
		"1. WARNING in Test.java (at line 3)\n" + 
		"	o = null;\n" + 
		"	    ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n" + 
		"2. ERROR in Test.java (at line 5)\n" + 
		"	if (p == null)\n" + 
		"	    ^\n" + 
		"Null comparison always yields false: The variable p cannot be null at this location\n" + 
		"----------\n" + 
		"3. WARNING in Test.java (at line 6)\n" + 
		"	p.toString();\n" + 
		"	^^^^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n");
}
// access to a non-null field
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_1() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @NonNull Object o = new Object();\n" +
			"    public String oString() {\n" +
			"         return o.toString();\n" +
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"");
}

// a non-null field is not properly initialized
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_2() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @NonNull Object o;\n" +
			"    public String oString() {\n" +
			"         return o.toString();\n" +
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	@NonNull Object o;\n" +
		"	                ^\n" +
		"The @NonNull field o may not have been initialized\n" +
		"----------\n");
}

// a non-null field is not properly initialized - explicit constructor
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_2a() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @NonNull Object o;\n" +
			"    X (boolean b) { // only potentially initialized\n" +
			"        if (b)\n" +
			"            o = this;\n" +
			"    }\n" +
			"    X (@NonNull Object other) {\n" + // no problem
			"        o = other;\n" +
			"    }\n" +
			"    public String oString() {\n" +
			"        return o.toString();\n" +
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	X (boolean b) { // only potentially initialized\n" + 
		"	^^^^^^^^^^^^^\n" + 
		"The @NonNull field o may not have been initialized\n" + 
		"----------\n");
}

// a non-null field is not properly initialized - explicit constructor - incomplete switch
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_2b() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"enum Color { BLACK, GREEN }\n" +
			"public class X {\n" +
			"    @NonNull Object o;\n" +
			"    X (Color c) { // only potentially initialized\n" +
			"        switch (c) {\n" +
			"            case BLACK: o = this; break;\n" +
			"            case GREEN: o = new Object(); break;\n" +
			"        }\n" +
			"    }\n" +
			"    public String oString() {\n" +
			"        return o.toString();\n" +
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	X (Color c) { // only potentially initialized\n" + 
		"	^^^^^^^^^^^\n" + 
		"The @NonNull field o may not have been initialized. Note that a problem regarding missing \'default:\' on \'switch\' has been suppressed, which is perhaps related to this problem\n" + 
		"----------\n");
}

// a non-null static field is not properly initialized
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_2c() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    static @NonNull Object o;\n" +
			"    static {\n" +
			"        if (new Object().hashCode() == 42)\n" +
			"            o = new Object();\n" +
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	static @NonNull Object o;\n" + 
		"	                       ^\n" + 
		"The @NonNull field o may not have been initialized\n" + 
		"----------\n");
}

// a non-null static field is properly initialized
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_2d() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    static @NonNull Object o;\n" +
			"    static {\n" +
			"         o = new Object();\n" +
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"");
}

// a non-null field is properly initialized - using this.f reference
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_2e() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X<T> {\n" +
			"    @NonNull Object f;\n" +
			"    {\n" +
			"         this.f = new Object();\n" +
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"");
}

// a non-null field is initialized to null
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_3() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @NonNull Object o = null;\n" +
			"    public String oString() {\n" +
			"         return o.toString();\n" +
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	@NonNull Object o = null;\n" + 
		"	                    ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n");
}
// a non-null field is assigned to null
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_4() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @NonNull Object o = new Object();\n" +
			"    void breakIt1() {\n" +
			"         o = null;\n" +
			"    }\n" +
			"    void breakIt2() {\n" +
			"         this.o = null;\n" +
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	o = null;\n" + 
		"	    ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 8)\n" + 
		"	this.o = null;\n" + 
		"	         ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n");
}
// a non-null field is checked for null
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_5() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @NonNull Object o = new Object();\n" +
			"    boolean checkIt1() {\n" +
			"         return o == null;\n" +
			"    }\n" +
			"    boolean checkIt() {\n" +
			"         return this.o != null;\n" +
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	return o == null;\n" + 
		"	       ^\n" + 
		"Null comparison always yields false: The field o is declared as @NonNull\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 8)\n" + 
		"	return this.o != null;\n" + 
		"	            ^\n" + 
		"Redundant null check: The field o is declared as @NonNull\n" + 
		"----------\n");
}

// a non-null field is checked for null twice - method call inbetween
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_6() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @NonNull Object o = new Object();\n" +
			"    boolean checkIt1() {\n" +
			"         if (o != null)\n" +
			"             System.out.print(\"not null\");\n" +
			"         System.out.print(\"continue\");\n" +
			"         return this.o == null;\n" +
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	if (o != null)\n" + 
		"	    ^\n" + 
		"Redundant null check: The field o is declared as @NonNull\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 8)\n" + 
		"	return this.o == null;\n" + 
		"	            ^\n" + 
		"Null comparison always yields false: The field o is declared as @NonNull\n" + 
		"----------\n");
}

// a non-null field is accessed via a qualified name reference - static field
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_7() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"class Objects {\n" +
			"    static @NonNull Object o = new Object();\n" +
			"}\n" +
			"public class X {\n" +
			"    @NonNull Object getIt1() {\n" +
			"         if (Objects.o != null) // redundant\n" +
			"             System.out.print(\"not null\");\n" +
			"         System.out.print(\"continue\");\n" +
			"         return Objects.o;\n" +
			"    }\n" +
			"    @NonNull Object getIt2() {\n" +
			"         if (null != Objects.o) // redundant\n" +
			"             System.out.print(\"not null\");\n" +
			"         System.out.print(\"continue\");\n" +
			"         return Objects.o;\n" +
			"    }\n" +
			"    String getIt3() {\n" +
			"         return Objects.o.toString();\n" +
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	if (Objects.o != null) // redundant\n" + 
		"	            ^\n" + 
		"Redundant null check: The field o is declared as @NonNull\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 13)\n" + 
		"	if (null != Objects.o) // redundant\n" + 
		"	                    ^\n" + 
		"Redundant null check: The field o is declared as @NonNull\n" + 
		"----------\n");
}

// a non-null field is accessed via a qualified name reference - instance field
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_8() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"class Objects {\n" +
			"    @NonNull Object o = new Object();\n" +
			"}\n" +
			"public class X {\n" +
			"    @NonNull Object getIt1(@NonNull Objects objs) {\n" +
			"         if (objs.o == null) // always false\n" +
			"             System.out.print(\"not null\");\n" +
			"         System.out.print(\"continue\");\n" +
			"         return objs.o;\n" +
			"    }\n" +
			"    String getIt2(@NonNull Objects objs) {\n" +
			"         return objs.o.toString();\n" +
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	if (objs.o == null) // always false\n" + 
		"	         ^\n" + 
		"Null comparison always yields false: The field o is declared as @NonNull\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 8)\n" + 
		"	System.out.print(\"not null\");\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n");
}

// a non-null field is accessed via an indirect field reference - instance field
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_9() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"class Objects {\n" +
			"    @NonNull Object o = new Object();\n" +
			"}\n" +
			"public class X {\n" +
			"    Objects objs = new Objects();\n" +
			"    @NonNull Object getIt1() {\n" +
			"         if (this.objs.o != null) // redundant\n" +
			"             System.out.print(\"not null\");\n" +
			"         System.out.print(\"continue\");\n" +
			"         if (getObjs().o != null) // redundant\n" +
			"             System.out.print(\"not null\");\n" +
			"         return this.objs.o;\n" +
			"    }\n" +
			"    Objects getObjs() { return this.objs; }\n" +
			"    String getIt2() {\n" +
			"         return this.objs.o.toString();\n" +
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" + 
		"1. ERROR in X.java (at line 8)\n" + 
		"	if (this.objs.o != null) // redundant\n" + 
		"	              ^\n" + 
		"Redundant null check: The field o is declared as @NonNull\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 11)\n" + 
		"	if (getObjs().o != null) // redundant\n" + 
		"	              ^\n" + 
		"Redundant null check: The field o is declared as @NonNull\n" + 
		"----------\n");
}

// trying to assign null to a nonnull field via a single / a qualified name reference
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_11() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"class Objects {\n" +
			"    @NonNull Object o = new Object();\n" +
			"    void test0(@Nullable Object x) {\n" +
			"         o = x;\n" +
			"    }\n" +
			"}\n" +
			"public class X {\n" +
			"    void test(@NonNull Objects objs) {\n" +
			"         objs.o = null;\n" +
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	o = x;\n" + 
		"	    ^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is specified as @Nullable\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 10)\n" + 
		"	objs.o = null;\n" + 
		"	         ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n");
}

// @NonNull is applied to a field with primitive type
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_12() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @NonNull int o = 1;\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	@NonNull int o = 1;\n" + 
		"	^^^^^^^^^^^^\n" + 
		"The nullness annotation @NonNull is not applicable for the primitive type int\n" + 
		"----------\n");
}

// A final field is initialized to non-null, treat as effectively @NonNull
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void _test_nonnull_field_13() {
	// withdrawn as of https://bugs.eclipse.org/331649#c75
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    final String s1 = \"\";\n" +
			"    @NonNull String s2;\n" +
			"    X() {\n" +
			"        s2 = s1;\n" +
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"");
}

// A field in a different CU is implicitly @NonNull (by type default) - that class is read from binary
// Assignment to other @NonNull field should not raise a warning
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_14() {
	runConformTestWithLibs(
		new String[] {
			"p1/X.java",
			"package p1;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class X {\n" +
			"    public String s1 = \"\";\n" +
			"}\n",
		},
		null /*customOptions*/,
		"");
	runConformTestWithLibs(
			new String[] {
			"p2/Y.java",
			"package p2;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"import p1.X;\n" +
			"public class Y {\n" +
			"    @NonNull String s2 = \"\";\n" +
			"    void foo(X other) {\n" +
			"        s2 = other.s1;\n" +
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"");
}

// A field in a different CU is implicitly @NonNull (by package default) - that class is read from binary
// Assignment to other @NonNull field should not raise a warning
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_14b() {
	runConformTestWithLibs(
		new String[] {
			"p1/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package p1;\n",
			"p1/X.java",
			"package p1;\n" +
			"public class X {\n" +
			"    public String s1 = \"\";\n" +
			"}\n",
		},
		null /*customOptions*/,
		"");
	runConformTestWithLibs(
			new String[] {
			"p2/Y.java",
			"package p2;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"import p1.X;\n" +
			"public class Y {\n" +
			"    @NonNull String s2 = \"\";\n" +
			"    void foo(X other) {\n" +
			"        s2 = other.s1;\n" +
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"");
}

// A @NonNull field is assumed to be initialized by the injection framework
// [compiler] Null analysis for fields does not take @com.google.inject.Inject into account
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400421
public void test_nonnull_field_15() {
	runConformTestWithLibs(
		new String[] {
			GOOGLE_INJECT_NAME,
			GOOGLE_INJECT_CONTENT,
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"import com.google.inject.Inject;\n" +
			"public class X {\n" +
			"    @NonNull @Inject Object o;\n" +
			"    @NonNullByDefault class Inner {\n" +
			"        @Inject String s;\n" +
			"    }\n" +
			"}\n",
		},
		null /*customOptions*/,
		"");
}

// Injection is optional, don't rely on the framework
// [compiler] Null analysis for fields does not take @com.google.inject.Inject into account
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400421
public void test_nonnull_field_16() {
	runNegativeTestWithLibs(
		new String[] {
			GOOGLE_INJECT_NAME,
			GOOGLE_INJECT_CONTENT,
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"import com.google.inject.Inject;\n" +
			"public class X {\n" +
			"    @Inject(optional=true) @NonNull Object o;\n" +
			"    @NonNullByDefault class Inner {\n" +
			"        @Inject(optional=true) String s;\n" +
			"        @Inject(optional=false) String t;\n" + // don't complain here
			"    }\n" +
			"}\n",
		},
		null /*customOptions*/,
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	@Inject(optional=true) @NonNull Object o;\n" + 
		"	                                       ^\n" + 
		"The @NonNull field o may not have been initialized\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 6)\n" + 
		"	@Inject(optional=true) String s;\n" + 
		"	                              ^\n" + 
		"The @NonNull field s may not have been initialized\n" + 
		"----------\n");
}

// Using javax.inject.Inject, slight variations
// [compiler] Null analysis for fields does not take @com.google.inject.Inject into account
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400421
public void test_nonnull_field_17() {
	runNegativeTestWithLibs(
		new String[] {
			JAVAX_INJECT_NAME,
			JAVAX_INJECT_CONTENT,
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"import javax.inject.Inject;\n" +
			"public class X {\n" +
			"    @NonNull @Inject static String s; // warn since injection of static field is less reliable\n" + // variation: static field
			"    @NonNull @Inject @Deprecated Object o;\n" +
			"    public X() {}\n" + // variation: with explicit constructor
			"}\n",
		},
		null /*customOptions*/,
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	@NonNull @Inject static String s; // warn since injection of static field is less reliable\n" + 
		"	                               ^\n" + 
		"The @NonNull field s may not have been initialized\n" + 
		"----------\n");
}

// access to a nullable field - field reference
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_1() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @Nullable Object o = new Object();\n" +
			"    public String oString() {\n" +
			"         return this.o.toString();\n" +
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	return this.o.toString();\n" + 
		"	            ^\n" + 
		"Potential null pointer access: The field o is declared as @Nullable\n" + 
		"----------\n");
}
// access to a nullable field - single name reference
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_2() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @Nullable Object o = new Object();\n" +
			"    public String oString() {\n" +
			"         return o.toString();\n" +
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	return o.toString();\n" + 
		"	       ^\n" + 
		"Potential null pointer access: The field o is declared as @Nullable\n" + 
		"----------\n");
}
// access to a nullable field - qualified name reference
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_3() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @Nullable Object o = new Object();\n" +
			"    @Nullable X other;\n" +
			"    public String oString() {\n" +
			"         return other.o.toString();\n" +
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	return other.o.toString();\n" +
		"	       ^^^^^\n" +
		"Potential null pointer access: The field other is declared as @Nullable\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	return other.o.toString();\n" +
		"	             ^\n" +
		"Potential null pointer access: The field o is declared as @Nullable\n" +
		"----------\n");
}
// access to a nullable field - qualified name reference - multiple segments
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_3m() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @Nullable Object o = new Object();\n" +
			"    @Nullable X other;\n" +
			"    public String oString() {\n" +
			"         return other.other.o.toString();\n" +
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	return other.other.o.toString();\n" +
		"	       ^^^^^\n" +
		"Potential null pointer access: The field other is declared as @Nullable\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	return other.other.o.toString();\n" +
		"	             ^^^^^\n" +
		"Potential null pointer access: The field other is declared as @Nullable\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 6)\n" +
		"	return other.other.o.toString();\n" +
		"	                   ^\n" +
		"Potential null pointer access: The field o is declared as @Nullable\n" +
		"----------\n");
}
// access to a nullable field - dereference after check
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_4() {
	// currently no flow analysis for fields is implemented,
	// but the direct sequence of null-check + dereference is optionally supported as a special case
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @Nullable Object o = new Object();\n" +
			"    public String oString() {\n" +
			"         if (this.o != null)\n" +
			"             return this.o.toString();\n" + // silent after check
			"         if (o != null)\n" +
			"             return o.toString();\n" + // silent after check
			"         return \"\";\n" +
			"    }\n" +
			"    public String oString2() {\n" +
			"         String local = o.toString();\n" +
			"         if (this.o != null) {\n" +
			"             this.toString();\n" + // method call wipes null info
			"             return this.o.toString(); // warn here\n" +
			"         }\n" +
			"         return \"\";\n" +
			"    }\n" +
			"}\n"
		},
		options /*customOptions*/,
		"----------\n" + 
		"1. ERROR in X.java (at line 12)\n" + 
		"	String local = o.toString();\n" + 
		"	               ^\n" + 
		"Potential null pointer access: The field o is declared as @Nullable\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 15)\n" + 
		"	return this.o.toString(); // warn here\n" + 
		"	            ^\n" + 
		"Potential null pointer access: The field o is declared as @Nullable\n" + 
		"----------\n");
}

// access to a nullable field - intermediate component in a QNR
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_5() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @NonNull Y y = new Y();\n" +
			"    public String oString() {\n" +
			"         return y.z.o.toString(); // pot.NPE on z\n" +
			"    }\n" +
			"}\n",
			"Y.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Y {\n" +
			"    @Nullable Z z = new Z();\n" +
			"}\n",
			"Z.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Z {\n" +
			"    @NonNull Object o = new Object();\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	return y.z.o.toString(); // pot.NPE on z\n" + 
		"	         ^\n" + 
		"Potential null pointer access: The field z is declared as @Nullable\n" + 
		"----------\n");
}

// access to a nullable field - intermediate component in a QNR - inverse of test_nullable_field_5
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_6() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @Nullable Y y = new Y();\n" +
			"    public String oString() {\n" +
			"         return y.z.o.toString(); // pot.NPE on y and o\n" +
			"    }\n" +
			"}\n",
			"Y.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Y {\n" +
			"    @NonNull Z z = new Z();\n" +
			"}\n",
			"Z.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Z {\n" +
			"    Object dummy;\n" + // ensure different interal fieldId
			"    @Nullable Object o = new Object();\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	return y.z.o.toString(); // pot.NPE on y and o\n" + 
		"	       ^\n" + 
		"Potential null pointer access: The field y is declared as @Nullable\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 5)\n" + 
		"	return y.z.o.toString(); // pot.NPE on y and o\n" + 
		"	           ^\n" + 
		"Potential null pointer access: The field o is declared as @Nullable\n" + 
		"----------\n");
}

// access to a nullable field - intermediate component in a double field reference
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_7() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @Nullable Y y = new Y();\n" +
			"    public String oString() {\n" +
			"         return this.y.o.toString(); // pot.NPE on y and o\n" +
			"    }\n" +
			"}\n",
			"Y.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Y {\n" +
			"    @Nullable Object o = new Object();\n" +
			"}\n"
		},
		null /*customOptions*/,
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	return this.y.o.toString(); // pot.NPE on y and o\n" + 
		"	            ^\n" + 
		"Potential null pointer access: The field y is declared as @Nullable\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 5)\n" + 
		"	return this.y.o.toString(); // pot.NPE on y and o\n" + 
		"	              ^\n" + 
		"Potential null pointer access: The field o is declared as @Nullable\n" + 
		"----------\n");
}

// static access to a nullable field - qualified name reference
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_8() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @Nullable static final Object o = null;\n" +
			"    public void foo() {\n" +
			"         if (X.o == null){\n" +
			"				System.out.println(X.o);\n" +
			"		  }\n" +
			"    }\n" +
			"}\n"
		},
		null /*customOptions*/,
		"");
}

// illegal use of @Nullable for a field of primitive type
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_9() {
	runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"public class X {\n" +
				"    @Nullable int i;\n" +
				"}\n"
			},
			null /*customOptions*/,
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	@Nullable int i;\n" + 
			"	^^^^^^^^^^^^^\n" + 
			"The nullness annotation @Nullable is not applicable for the primitive type int\n" + 
			"----------\n");	
}

// protected access to nullable fields - different kinds of references
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_10a() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @Nullable Object o1, o2, o3;\n" +
			"    @NonNull X x = new X();\n" +
			"    public void foo(X other) {\n" +
			"         if (other.o1 != null){\n" +						// qualified reference -> block
			"             System.out.println(other.o1.toString());\n" +
			"         }\n" +
			"         if (this.o2 != null)\n" + 						// field reference -> statement
			"             System.out.println(o2.toString());\n" +
			"         if (this.o2 != null)\n" + 						// identical field references
			"             System.out.println(this.o2.toString());\n" +
			"         System.out.println (null != o3 ? o3.toString() : \"nothing\");\n" + // ternary
			"         if (this.x.o1 != null)\n" +						// nested field reference ...
			"             System.out.println(x.o1.toString());\n" + 	// ... equiv qualified name reference
			"         if (x.o1 != null)\n" +							// qualified name reference ...
			"             System.out.println(this.x.o1.toString());\n" +// ... equiv nested field reference
			"         if (this.x.o1 != null)\n" +						// identical nested field references
			"             System.out.println(this.x.o1.toString());\n" +
			"    }\n" +
			"}\n"
		},
		options,
		"");
}

// protected access to nullable fields - different kinds of references - option not enabled
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_10b() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.DISABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @Nullable Object o1, o2, o3;\n" +
			"    @NonNull X x = new X();\n" +
			"    public void foo(X other) {\n" +
			"         if (other.o1 != null){\n" +						// qualified reference -> block
			"             System.out.println(other.o1.toString());\n" +
			"         }\n" +
			"         if (this.o2 != null)\n" + 						// field reference -> statement
			"             System.out.println(o2.toString());\n" +
			"         if (this.o2 != null)\n" + 						// identical field references
			"             System.out.println(this.o2.toString());\n" +
			"         System.out.println (null != o3 ? o3.toString() : \"nothing\");\n" + // ternary
			"         if (this.x.o1 != null)\n" +						// nested field reference ...
			"             System.out.println(x.o1.toString());\n" + 	// ... equiv qualified name reference
			"         if (x.o1 != null)\n" +							// qualified name reference ...
			"             System.out.println(this.x.o1.toString());\n" +// ... equiv nested field reference
			"         if (this.x.o1 != null)\n" +						// identical nested field references
			"             System.out.println(this.x.o1.toString());\n" +
			"    }\n" +
			"}\n"
		},
		options,
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	System.out.println(other.o1.toString());\n" + 
		"	                         ^^\n" + 
		"Potential null pointer access: The field o1 is declared as @Nullable\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 10)\n" + 
		"	System.out.println(o2.toString());\n" + 
		"	                   ^^\n" + 
		"Potential null pointer access: The field o2 is declared as @Nullable\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 12)\n" + 
		"	System.out.println(this.o2.toString());\n" + 
		"	                        ^^\n" + 
		"Potential null pointer access: The field o2 is declared as @Nullable\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 13)\n" + 
		"	System.out.println (null != o3 ? o3.toString() : \"nothing\");\n" + 
		"	                                 ^^\n" + 
		"Potential null pointer access: The field o3 is declared as @Nullable\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 15)\n" + 
		"	System.out.println(x.o1.toString());\n" + 
		"	                     ^^\n" + 
		"Potential null pointer access: The field o1 is declared as @Nullable\n" + 
		"----------\n" + 
		"6. ERROR in X.java (at line 17)\n" + 
		"	System.out.println(this.x.o1.toString());\n" + 
		"	                          ^^\n" + 
		"Potential null pointer access: The field o1 is declared as @Nullable\n" + 
		"----------\n" + 
		"7. ERROR in X.java (at line 19)\n" + 
		"	System.out.println(this.x.o1.toString());\n" + 
		"	                          ^^\n" + 
		"Potential null pointer access: The field o1 is declared as @Nullable\n" + 
		"----------\n");
}

// protected access to nullable fields - different boolean operators
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_10c() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @Nullable Object o1, o2, o3;\n" +
			"    public void foo(X other) {\n" +
			"         if (o1 != null && o2 != null & o3 != null) \n" + // conjunction: OK
			"             System.out.println(o2.toString());\n" +
			"         if (o1 != null || o2 != null || o3 != null) \n" +
			"             System.out.println(o2.toString()); // warn here: disjunktion is no protection\n" +
			"         if (!(o1 != null)) \n" +
			"             System.out.println(o1.toString()); // warn here: negated inequality is no protection\n" +
			"         if (!(o1 == null || o2 == null)) \n" +
			"             System.out.println(o1.toString()); // don't warn here\n" +
			"         if (!(o1 == null && o2 == null)) \n" +
			"             System.out.println(o2.toString()); // warn here: negated conjunction is no protection\n" +
			"         if (!(!(o1 == null))) \n" +
			"             System.out.println(o1.toString()); // warn here: double negation is no protection\n" +
			"         if (!(!(o1 != null && o2 != null))) \n" +
			"             System.out.println(o1.toString()); // don't warn here\n" +
			"    }\n" +
			"}\n"
		},
		options,
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	System.out.println(o2.toString()); // warn here: disjunktion is no protection\n" +
		"	                   ^^\n" +
		"Potential null pointer access: The field o2 is declared as @Nullable\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	System.out.println(o1.toString()); // warn here: negated inequality is no protection\n" +
		"	                   ^^\n" +
		"Potential null pointer access: The field o1 is declared as @Nullable\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 14)\n" +
		"	System.out.println(o2.toString()); // warn here: negated conjunction is no protection\n" +
		"	                   ^^\n" +
		"Potential null pointer access: The field o2 is declared as @Nullable\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 16)\n" +
		"	System.out.println(o1.toString()); // warn here: double negation is no protection\n" +
		"	                   ^^\n" +
		"Potential null pointer access: The field o1 is declared as @Nullable\n" +
		"----------\n");
}

// protected access to nullable fields - assignment as expression
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_10d() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @Nullable Object o1;\n" +
			"    public void foo(@NonNull X other, X last) {\n" +
			"         o1 = other;\n" +		// reference test case: assignment as statement
			"         if (o1 == last) \n" +	// no expiry
			"             System.out.println(o1.toString());\n" +
			"         if ((o1 = other) == last) \n" + // no expiry
			"             System.out.println(o1.toString());\n" +
			"         if ((o1 = other) == last) {\n" +
			"             o1 = null;\n" + // expire here
			"             System.out.println(o1.toString()); // info is expired\n" +
			"         }\n" +
			"    }\n" +
			"}\n"
		},
		options,
		"----------\n" + 
		"1. ERROR in X.java (at line 12)\n" + 
		"	System.out.println(o1.toString()); // info is expired\n" + 
		"	                   ^^\n" + 
		"Potential null pointer access: The field o1 is declared as @Nullable\n" + 
		"----------\n");
}

// protected access to nullable fields - distinguish local and field
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_10e() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"class Y {\n" +
			"    @Nullable Object o2;\n" +
			"    void bar(Object o2) {\n" +
			"        if (o2 != null)\n" +
			"            System.out.println(this.o2.toString()); // field access is not protected\n" +
			"    }\n" +
			"}\n" +
			"public class X {\n" +
			"    @NonNull Y o1 = new Y();\n" +
			"    public void foo() {\n" +
			"         Y o1 = new Y();\n" +
			"         if (o1.o2 != null) \n" +	// check via local
			"             System.out.println(this.o1.o2.toString()); // field access via other field not protected\n" +
			"         if (this.o1.o2 != null) \n" +	// check via field
			"             System.out.println(o1.o2.toString()); // field access via local not protected\n" +
			"    }\n" +
			"}\n"
		},
		options,
		"----------\n" + 
		"1. WARNING in X.java (at line 4)\n" + 
		"	void bar(Object o2) {\n" + 
		"	                ^^\n" + 
		"The parameter o2 is hiding a field from type Y\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 6)\n" + 
		"	System.out.println(this.o2.toString()); // field access is not protected\n" + 
		"	                        ^^\n" + 
		"Potential null pointer access: The field o2 is declared as @Nullable\n" + 
		"----------\n" + 
		"3. WARNING in X.java (at line 12)\n" + 
		"	Y o1 = new Y();\n" + 
		"	  ^^\n" + 
		"The local variable o1 is hiding a field from type X\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 14)\n" + 
		"	System.out.println(this.o1.o2.toString()); // field access via other field not protected\n" + 
		"	                           ^^\n" + 
		"Potential null pointer access: The field o2 is declared as @Nullable\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 16)\n" + 
		"	System.out.println(o1.o2.toString()); // field access via local not protected\n" + 
		"	                      ^^\n" + 
		"Potential null pointer access: The field o2 is declared as @Nullable\n" + 
		"----------\n");
}

// protected access to nullable fields - duplicate comparison
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_10f() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @Nullable Object o1;\n" +
			"    public void foo(X other) {\n" +
			"         if (o1 != null && o1 != null) // second term is redundant\n" +
			"             System.out.println(o1.toString());\n" +
			"         if (o1 != null)\n" +
			"             if (o1 != null) // this if is redundant\n" +
			"                 System.out.println(o1.toString());\n" +
			"    }\n" +
			"}\n"
		},
		options,
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	if (o1 != null && o1 != null) // second term is redundant\n" + 
		"	                  ^^\n" + 
		"Redundant null check: this expression cannot be null\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 8)\n" + 
		"	if (o1 != null) // this if is redundant\n" + 
		"	    ^^\n" + 
		"Redundant null check: this expression cannot be null\n" + 
		"----------\n");
}

// combined test from comment 20 in https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_11() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runConformTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"class X {\n" + 
				"    @Nullable Object o;\n" + 
				"    public @NonNull Object foo(X x) {\n" + 
				"    	return  x.o != null ? x.o : new Object();\n" + 
				"	 }\n" + 
				"    public void goo(X x) {\n" + 
				"    	if (x.o != null) {\n" + 
				"    		x.o.toString();\n" + 
				"    	}\n" + 
				"    }\n" + 
				"    public void boo(X x) {\n" + 
				"    	if (x.o instanceof String) {\n" + 
				"    		x.o.toString();\n" + 
				"    	}\n" + 
				"    }\n" + 
				"    public void zoo(X x) {\n" + 
				"    	x.o = new Object();\n" + 
				"    	System.out.println(\"hashCode of new Object = \" + x.o.hashCode());\n" + 
				"    }\n" + 
				"    public void doo(X x) {\n" + 
				"    	x.o = foo(x); // foo is guaranteed to return @NonNull Object.\n" + 
				"    	System.out.println(\"hashCode of new Object = \" + x.o.hashCode());\n" + 
				"    }\n" + 
				"}\n"
			},
			options,
			"");
}

// combined test from comment 20 in https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
//  - version with 'this' field references
public void test_nullable_field_11a() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runConformTestWithLibs(
			new String[] {
				"X.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"class X {\n" + 
				"    @Nullable Object o;\n" + 
				"    public @NonNull Object foo() {\n" + 
				"    	return  o != null ? o : new Object();\n" + 
				"    }\n" + 
				"    public void goo() {\n" + 
				"    	if (o != null) {\n" + 
				"    		o.toString();\n" + 
				"    	}\n" + 
				"    }\n" + 
				"    public void boo() {\n" + 
				"    	if (o instanceof String) {\n" + 
				"    		o.toString();\n" + 
				"    	}\n" + 
				"    }\n" + 
				"    public void zoo() {\n" + 
				"    	o = new Object();\n" + 
				"    	System.out.println(\"hashCode of new Object = \" + o.hashCode());\n" + 
				"    }\n" + 
				"    public void doo() {\n" + 
				"    	o = foo(); // foo is guaranteed to return @NonNull Object.\n" + 
				"    	System.out.println(\"hashCode of new Object = \" + o.hashCode());\n" + 
				"    }\n" + 
				"}\n"
			},
			options,
			"");
}

// protected access to nullable field - expiration of information
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_12() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @Nullable Object o1, o2, o3, o4;\n" +
			"    public void foo(X other) {\n" +
			"         if (other.o1 != null){\n" +
			"				System.out.println(goo()+other.o1.toString()); // warn here: expired by call to goo()\n" +
			"		  }\n" +
			"         Object x = o2 != null ? o2 : o1;\n" +
			"         System.out.println(o2.toString()); // warn here: not protected\n" +
			"         if (o3 != null) /*nop*/;\n" +
			"         System.out.println(o3.toString()); // warn here: expired by empty statement\n" +
			"         if (o4 != null && hoo())\n" +
			"             System.out.println(o4.toString()); // warn here: expired by call to hoo()\n" +
			"    }\n" +
			"    String goo() { return \"\"; }\n" +
			"    boolean hoo() { return false; }\n" +
			"}\n"
		},
		options,
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	System.out.println(goo()+other.o1.toString()); // warn here: expired by call to goo()\n" + 
		"	                               ^^\n" + 
		"Potential null pointer access: The field o1 is declared as @Nullable\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 9)\n" + 
		"	System.out.println(o2.toString()); // warn here: not protected\n" +
		"	                   ^^\n" + 
		"Potential null pointer access: The field o2 is declared as @Nullable\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 11)\n" + 
		"	System.out.println(o3.toString()); // warn here: expired by empty statement\n" + 
		"	                   ^^\n" + 
		"Potential null pointer access: The field o3 is declared as @Nullable\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 13)\n" + 
		"	System.out.println(o4.toString()); // warn here: expired by call to hoo()\n" + 
		"	                   ^^\n" + 
		"Potential null pointer access: The field o4 is declared as @Nullable\n" + 
		"----------\n");
}

// example from comment 47
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_13() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @Nullable Object o1;\n" +
			"    @NonNull Object o2 = new Object();\n" +
			"    public void foo(X other) {\n" +
			"         if (other.o1 == null){\n" +
			"				this.o2 = other.o1; // warn here: assign @Nullable to @NonNull\n" +
			"		  }\n" +
			"    }\n" +
			"}\n"
		},
		options,
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	this.o2 = other.o1; // warn here: assign @Nullable to @NonNull\n" + 
		"	          ^^^^^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is specified as @Nullable\n" + 
		"----------\n");
}

// access to a nullable field - protected by check against a @NonNull value
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_14() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @Nullable Object o = new Object();\n" +
			"    public String oString(@NonNull Object a) {\n" +
			"         if (this.o == a)\n" +
			"             return this.o.toString();\n" + // silent after check
			"         return \"\";\n" +
			"    }\n" +
			"}\n"
		},
		options,
		"");
}

// access to a nullable field - not protected by negative check against a @NonNull value
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_14a() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @Nullable Object o = new Object();\n" +
			"    public String oString(@NonNull Object a) {\n" +
			"         if (this.o != a)\n" +
			"             return this.o.toString(); // warn here, check has no effect\n" +
			"         return \"\";\n" +
			"    }\n" +
			"}\n"
		},
		options,
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	return this.o.toString(); // warn here, check has no effect\n" + 
		"	            ^\n" + 
		"Potential null pointer access: The field o is declared as @Nullable\n" + 
		"----------\n");
}

// https://bugs.eclipse.org/401017: [compiler][null] casted reference to @Nullable field lacks a warning
public void test_nullable_field_15() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @Nullable\n" + 
			"    private Object nullable;\n" + 
			"\n" + 
			"    public void test() {\n" + 
			"        if (nullable instanceof Number) {\n" + 
			"            ((Number)nullable).intValue(); // A\n" + 
			"        }\n" + 
			"        if (nullable != null) {\n" + 
			"            nullable.toString(); // B\n" + 
			"        }\n" + 
			"        nullable.toString(); // C\n" + 
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 8)\n" + 
		"	((Number)nullable).intValue(); // A\n" + 
		"	         ^^^^^^^^\n" + 
		"Potential null pointer access: The field nullable is declared as @Nullable\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 11)\n" + 
		"	nullable.toString(); // B\n" + 
		"	^^^^^^^^\n" + 
		"Potential null pointer access: The field nullable is declared as @Nullable\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 13)\n" + 
		"	nullable.toString(); // C\n" + 
		"	^^^^^^^^\n" + 
		"Potential null pointer access: The field nullable is declared as @Nullable\n" + 
		"----------\n");
}
// an enum is declared within the scope of a null-default
// https://bugs.eclipse.org/331649#c61
public void test_enum_field_01() {
	runConformTestWithLibs(
		new String[] {
			"tests/X.java",
			"package tests;\n" +
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"public class X {\n" +
			"    enum A { B }\n" +
			"    public static void main(String ... args) {\n" +
			"         System.out.println(A.B);\n" +
			"    }\n" +
			"}\n"
		},
		null,
		"",
		"B");
}

// Bug 380896 - Enum constants not recognised as being NonNull.
// see also https://bugs.eclipse.org/331649#c61
public void test_enum_field_02() {
	runConformTestWithLibs(
		new String[] {
			"tests/X.java",
			"package tests;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    enum A { B }\n" +
			"    public static void main(String ... args) {\n" +
			"         test(A.B);\n" +
			"    }\n" +
			"    static void test(@NonNull A a) {\n" +
			"        System.out.println(a.ordinal());\n" +
			"    }\n" +
			"}\n"
		},
		null,
		"",
		"0");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=372011
// Test whether @NonNullByDefault on a binary package or an enclosing type is respected from enclosed elements.
public void testBug372011() {
	String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "Test372011.jar";
	String[] libs = new String[this.LIBS.length + 1];
	System.arraycopy(this.LIBS, 0, libs, 0, this.LIBS.length);
	libs[this.LIBS.length] = path;
	runNegativeTest(
		new String[] {
			"X.java",
			  "import p11.T11;\n" +
			  "import p12.T12;\n" + 
			  "import p12.T12a;\n" +
			  "import p12.Public;\n" +
			  "public class X {\n" +
			  "	  void foo() {\n" +
			  "     new T11().t11foo(null);\n" +
			  "     new T12().new T122().foo122(null);\n" +
			  "   }\n" +
			  "	  void trigger1 (Public o){\n" +
			  "			o.bar(null);\n" +
			  "	  }\n" +
			  "	  @org.eclipse.jdt.annotation.NonNull Object foo2() {\n" +
			  "		new T12a().foo12a(new Object());\n" +  // don't complain
			  "     new T12().new T122().new T1222().foo1222(null);\n" +
			  "     return new T11().retSomething();\n" +  // don't complain
			  "   }\n" +
			  "}\n"},
	    "----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	new T11().t11foo(null);\n" + 
		"	                 ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 8)\n" + 
		"	new T12().new T122().foo122(null);\n" + 
		"	                            ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 11)\n" + 
		"	o.bar(null);\n" + 
		"	      ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 15)\n" + 
		"	new T12().new T122().new T1222().foo1222(null);\n" + 
		"	                                         ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n",
		libs,
		true /* shouldFlush*/);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=374129  - more tests for bug 372011
// Test whether @NonNullByDefault on a binary package or an enclosing type is respected from enclosed elements.
public void testBug374129() {
	String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "Test374129.jar";
	/* content of Test372129.jar:
	 	p1bin/package-info.java:
	 		@org.eclipse.jdt.annotation.NonNullByDefault
			package p1bin;
		p1bin/C1bin.java:
			package p1bin;
			import org.eclipse.jdt.annotation.Nullable;
			public class C1bin {
				public String getId(String id, @Nullable String n) {
					return id;
				}
				public static class C1binInner {
					public String getId(String id, @Nullable String n) {
						return id;
					}		
				}
			}
		p2bin/C2bin.java:
			package p2bin;
			import org.eclipse.jdt.annotation.NonNullByDefault;
			import org.eclipse.jdt.annotation.Nullable;
			@NonNullByDefault
			public class C2bin {
				public String getId(String id, @Nullable String n) {
					return id;
				}
				@NonNullByDefault(false)
				public static class C2binInner {
					public String getId(String id, @Nullable String n) {
						return id;
					}		
				}
			}
		p2bin/C3bin.java:
			package p2bin;
			import org.eclipse.jdt.annotation.NonNullByDefault;
			import org.eclipse.jdt.annotation.Nullable;
			public class C3bin {
				@NonNullByDefault public String getId(String id, @Nullable String n) {
					return id;
				}			
			}
	 */
	String[] libs = new String[this.LIBS.length + 1];
	System.arraycopy(this.LIBS, 0, libs, 0, this.LIBS.length);
	libs[this.LIBS.length] = path;
	runNegativeTest(
		new String[] {
			"bug374129/Test.java",
				"package bug374129;\n" + 
				"\n" + 
				"import org.eclipse.jdt.annotation.NonNull;\n" + 
				"import org.eclipse.jdt.annotation.Nullable;\n" + 
				"\n" + 
				"import p1bin.C1bin;\n" + 
				"import p1bin.C1bin.C1binInner;\n" + 
				"import p2bin.C2bin;\n" + 
				"import p2bin.C2bin.C2binInner;\n" + 
				"import p2bin.C3bin;\n" + 
				"\n" + 
				"public class Test {\n" + 
				"	static C1bin c1 = new C1bin();\n" + 
				"	static C1binInner c1i = new C1binInner();\n" + 
				"	static C2bin c2 = new C2bin();\n" + 
				"	static C2binInner c2i = new C2binInner();\n" + 
				"	static C3bin c3 = new C3bin();\n" + 
				"	\n" + 
				"	public static void main(String[] args) {\n" + 
				"		@Nullable String n = getN();\n" + 
				"		@NonNull String s;\n" + 
				"		s = c1.getId(n, n); // error on first arg (package default)\n" + 
				"		s = c1i.getId(n, n); // error on first arg (package default propagated into inner)\n" + 
				"		s = c2.getId(n, n); // error on first arg (type default)\n" + 
				"		s = c2i.getId(n, n); // no arg error (canceled default), return requires unchecked conversion\n" + 
				"		s = c3.getId(n, n); // error on first arg (method default)\n" + 
				"	}\n" + 
				"	static String getN() { return null; }\n" + 
				"}\n" + 
				"\n"},
			"----------\n" + 
			"1. ERROR in bug374129\\Test.java (at line 22)\n" + 
			"	s = c1.getId(n, n); // error on first arg (package default)\n" + 
			"	             ^\n" + 
			"Null type mismatch: required \'@NonNull String\' but the provided value is specified as @Nullable\n" + 
			"----------\n" + 
			"2. ERROR in bug374129\\Test.java (at line 23)\n" + 
			"	s = c1i.getId(n, n); // error on first arg (package default propagated into inner)\n" + 
			"	              ^\n" + 
			"Null type mismatch: required \'@NonNull String\' but the provided value is specified as @Nullable\n" + 
			"----------\n" + 
			"3. ERROR in bug374129\\Test.java (at line 24)\n" + 
			"	s = c2.getId(n, n); // error on first arg (type default)\n" + 
			"	             ^\n" + 
			"Null type mismatch: required \'@NonNull String\' but the provided value is specified as @Nullable\n" + 
			"----------\n" + 
			"4. WARNING in bug374129\\Test.java (at line 25)\n" + 
			"	s = c2i.getId(n, n); // no arg error (canceled default), return requires unchecked conversion\n" + 
			"	    ^^^^^^^^^^^^^^^\n" + 
			"Null type safety: The expression of type String needs unchecked conversion to conform to \'@NonNull String\'\n" + 
			"----------\n" + 
			"5. ERROR in bug374129\\Test.java (at line 26)\n" + 
			"	s = c3.getId(n, n); // error on first arg (method default)\n" + 
			"	             ^\n" + 
			"Null type mismatch: required \'@NonNull String\' but the provided value is specified as @Nullable\n" + 
			"----------\n",
		libs,
		true /* shouldFlush*/);
}

// Bug 385626 - @NonNull fails across loop boundaries
public void testBug385626_1() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    void test() {\n" +
			"        for (Integer i : new ArrayList<Integer>()) {\n" +
			"            if (i != null) {\n" +
			"                for (Integer j : new ArrayList<Integer>()) {\n" +
			"                    if (j != null) {\n" +
			"                        @NonNull Integer j1 = i; // bogus error was here\n" +
			"                    }\n" +
			"                }\n" + 
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		null,//options
		"");
}

// Bug 385626 - @NonNull fails across loop boundaries
public void testBug385626_2() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"import java.util.ArrayList;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    void test(Integer j) {\n" +
			"        for (Integer i : new ArrayList<Integer>()) {\n" +
			"            if (i != null) {\n" +
			"                try {\n" +
			"                    if (j != null) {\n" +
			"                        @NonNull Integer j1 = i;\n" +
			"                    }\n" +
			"                } finally {\n" +
			"                    if (j != null) {\n" +
			"                        @NonNull Integer j1 = i; // bogus error was here\n" +
			"                    }\n" +
			"                }\n" + 
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		null,//options
		"");
}

// Bug 388630 - @NonNull diagnostics at line 0
// synthetic constructor must repeat null annotations of its super
public void testBug388630_1() {
	runConformTestWithLibs(
		new String[] {
			"C0.java",
			"import org.eclipse.jdt.annotation.NonNull;\n" + 
			"public class C0 {\n" + 
			"	C0 (@NonNull Object o) { }\n" + 
			"	void test() { }\n" + 
			"}\n",
			"X.java",
			"public class X {\n" + 
			"	void foo() {\n" + 
			"		new C0(\"\") { }.test();\n" + 
			"	}\n" + 
			"}\n"
		},
		null,
		"");
}

// Bug 388630 - @NonNull diagnostics at line 0
// additionally also references to outer variables must share their nullness
public void testBug388630_2() {
	runNegativeTestWithLibs(
		new String[] {
			"C0.java",
			"import org.eclipse.jdt.annotation.NonNull;\n" + 
			"public class C0 {\n" + 
			"	C0 (@NonNull Object o) { }\n" + 
			"	void test() { }\n" + 
			"}\n",
			"X.java",
			"import org.eclipse.jdt.annotation.Nullable;\n" + 
			"public class X {\n" + 
			"	void foo(final @Nullable Object a) {\n" + 
			"		new C0(\"\") {\n" +
			"           @Override\n" +
			"           void test() {\n" +
			"               System.out.println(a.toString());\n" +
			"               super.test();\n" +
			"           }\n" +
			"       }.test();\n" + 
			"	}\n" + 
			"}\n"
		},
		null,
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	System.out.println(a.toString());\n" + 
		"	                   ^\n" + 
		"Potential null pointer access: The variable a may be null at this location\n" + 
		"----------\n");
}

/* Content of Test388281.jar used in the following tests:

// === package i (explicit annotations): ===
package i;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
public interface I {
    @NonNull Object m1(@Nullable Object a1);
    @Nullable String m2(@NonNull Object a2);
	Object m1(@Nullable Object o1, Object o2);
}

// === package  i2 with package-info.java (default annot, canceled in one type): ===
@org.eclipse.jdt.annotation.NonNullByDefault
package i2;

package i2;
public interface I2 {
    Object m1(Object a1);
    String m2(Object a2);
}

package i2;
public interface II extends i.I {
	String m1(Object o1, Object o2);
}

package i2;
import org.eclipse.jdt.annotation.NonNullByDefault;
@NonNullByDefault(false)
public interface I2A {
    Object m1(Object a1);
    String m2(Object a2);
}

// === package c (no null annotations): ===
package c;
public class C1 implements i.I {
	public Object m1(Object a1) {
		System.out.println(a1.toString()); // (1)
		return null; // (2)
	}
	public String m2(Object a2) {
		System.out.println(a2.toString());
		return null;
	}
	public Object m1(Object o1, Object o2) {
		return null;
	}
}

package c;
public class C2 implements i2.I2 {
	public Object m1(Object a1) {
		return a1;
	}
	public String m2(Object a2) {
		return a2.toString();
	}
}
 */
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388281
// Test whether null annotations from a super interface are respected
// Class and its super interface both read from binary
public void testBug388281_01() {
	String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "Test388281.jar";
	String[] libs = new String[this.LIBS.length + 1];
	System.arraycopy(this.LIBS, 0, libs, 0, this.LIBS.length);
	libs[this.LIBS.length] = path;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_INHERIT_NULL_ANNOTATIONS, JavaCore.ENABLED);
	runNegativeTest(
		new String[] {
			"Client.java",
			"import c.C1;\n" +
			"public class Client {\n" + 
			"    void test(C1 c) {\n" + 
			"         String s = c.m2(null);               // (3)\n" + 
			"         System.out.println(s.toUpperCase()); // (4)\n" + 
			"    }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in Client.java (at line 4)\n" + 
		"	String s = c.m2(null);               // (3)\n" + 
		"	                ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n" + 
		"2. ERROR in Client.java (at line 5)\n" + 
		"	System.out.println(s.toUpperCase()); // (4)\n" + 
		"	                   ^\n" + 
		"Potential null pointer access: The variable s may be null at this location\n" + 
		"----------\n",
		libs,
		true /* shouldFlush*/,
		options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388281
// Test whether null annotations from a super interface are respected
// Class from source, its supers (class + super interface) from binary
public void testBug388281_02() {
	String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "Test388281.jar";
	String[] libs = new String[this.LIBS.length + 1];
	System.arraycopy(this.LIBS, 0, libs, 0, this.LIBS.length);
	libs[this.LIBS.length] = path;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_INHERIT_NULL_ANNOTATIONS, JavaCore.ENABLED);
	runNegativeTest(
		new String[] {
			"ctest/C.java",
			"package ctest;\n" +
			"public class C extends c.C1 {\n" +
			"    @Override\n" +
			"    public Object m1(Object a1) {\n" + 
			"         System.out.println(a1.toString());   // (1)\n" + 
			"         return null;                         // (2)\n" + 
			"    }\n" +
			"    @Override\n" +
			"    public String m2(Object a2) {\n" + 
			"         System.out.println(a2.toString());\n" + 
			"         return null;\n" + 
			"    }\n" +
			"}\n",
			"Client.java",
			"import ctest.C;\n" +
			"public class Client {\n" + 
			"    void test(C c) {\n" + 
			"         String s = c.m2(null);               // (3)\n" + 
			"         System.out.println(s.toUpperCase()); // (4)\n" + 
			"    }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in ctest\\C.java (at line 5)\n" + 
		"	System.out.println(a1.toString());   // (1)\n" + 
		"	                   ^^\n" + 
		"Potential null pointer access: The variable a1 may be null at this location\n" + 
		"----------\n" + 
		"2. ERROR in ctest\\C.java (at line 6)\n" + 
		"	return null;                         // (2)\n" + 
		"	       ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n" + 
		"----------\n" +
		"1. ERROR in Client.java (at line 4)\n" + 
		"	String s = c.m2(null);               // (3)\n" + 
		"	                ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n" + 
		"2. ERROR in Client.java (at line 5)\n" + 
		"	System.out.println(s.toUpperCase()); // (4)\n" + 
		"	                   ^\n" + 
		"Potential null pointer access: The variable s may be null at this location\n" + 
		"----------\n",
		libs,
		true /* shouldFlush*/,
		options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388281
// Test whether null annotations from a super interface trigger an error against the overriding implementation
// Class from source, its super interface from binary
public void testBug388281_03() {
	String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "Test388281.jar";
	String[] libs = new String[this.LIBS.length + 1];
	System.arraycopy(this.LIBS, 0, libs, 0, this.LIBS.length);
	libs[this.LIBS.length] = path;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_INHERIT_NULL_ANNOTATIONS, JavaCore.ENABLED);
	runNegativeTest(
		new String[] {
			"ctest/C.java",
			"package ctest;\n" +
			"public class C implements i.I {\n" +
			"    public Object m1(Object a1) {\n" + 
			"         System.out.println(a1.toString());   // (1)\n" + 
			"         return null;                         // (2)\n" + 
			"    }\n" +
			"    public String m2(Object a2) {\n" + 
			"         System.out.println(a2.toString());\n" + 
			"         return null;\n" + 
			"    }\n" +
			"    public Object m1(Object a1, Object a2) {\n" +
			"        System.out.println(a1.toString());   // (3)\n" +
			"        return null;\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in ctest\\C.java (at line 4)\n" + 
		"	System.out.println(a1.toString());   // (1)\n" + 
		"	                   ^^\n" + 
		"Potential null pointer access: The variable a1 may be null at this location\n" + 
		"----------\n" + 
		"2. ERROR in ctest\\C.java (at line 5)\n" + 
		"	return null;                         // (2)\n" + 
		"	       ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n" + 
		"3. ERROR in ctest\\C.java (at line 12)\n" + 
		"	System.out.println(a1.toString());   // (3)\n" + 
		"	                   ^^\n" + 
		"Potential null pointer access: The variable a1 may be null at this location\n" + 
		"----------\n",
		libs,		
		true /* shouldFlush*/,
		options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388281
// Do inherit even if one parameter/return is annotated
// also features some basic overloading
public void testBug388281_04() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_INHERIT_NULL_ANNOTATIONS, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		true /* shouldFlush*/,
		new String[] {
			"i/I.java",
			"package i;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public interface I {\n" +
			"    @NonNull Object m1(@NonNull Object s1, @Nullable String s2);\n" +
			"    @Nullable Object m1(@Nullable String s1, @NonNull Object s2);\n" +
			"}\n",
			"ctest/C.java",
			"package ctest;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class C implements i.I {\n" +
			"    public Object m1(@Nullable Object o1, String s2) {\n" + 
			"         System.out.println(s2.toString());   // (1)\n" + 
			"         return null;                         // (2)\n" + 
			"    }\n" +
			"    public @NonNull Object m1(String s1, Object o2) {\n" + 
			"         System.out.println(s1.toString());   // (3)\n" + 
			"         return new Object();\n" + 
			"    }\n" +
			"}\n"
		},
		options,
		"----------\n" + 
		"1. ERROR in ctest\\C.java (at line 5)\n" + 
		"	System.out.println(s2.toString());   // (1)\n" + 
		"	                   ^^\n" + 
		"Potential null pointer access: The variable s2 may be null at this location\n" + 
		"----------\n" + 
		"2. ERROR in ctest\\C.java (at line 6)\n" + 
		"	return null;                         // (2)\n" + 
		"	       ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n" + 
		"3. ERROR in ctest\\C.java (at line 9)\n" + 
		"	System.out.println(s1.toString());   // (3)\n" + 
		"	                   ^^\n" + 
		"Potential null pointer access: The variable s1 may be null at this location\n" + 
		"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388281
// Test whether null annotations from a super interface trigger an error against the overriding implementation
// Class from source, its super interface from binary
// Super interface subject to package level @NonNullByDefault
public void testBug388281_05() {
	String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "Test388281.jar";
	String[] libs = new String[this.LIBS.length + 1];
	System.arraycopy(this.LIBS, 0, libs, 0, this.LIBS.length);
	libs[this.LIBS.length] = path;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_INHERIT_NULL_ANNOTATIONS, JavaCore.ENABLED);
	runNegativeTest(
		new String[] {
			"ctest/C.java",
			"package ctest;\n" +
			"public class C implements i2.I2 {\n" +
			"    public Object m1(Object a1) {\n" + 
			"         System.out.println(a1.toString());   // silent\n" + 
			"         return null;                         // (1)\n" + 
			"    }\n" +
			"    public String m2(Object a2) {\n" + 
			"         System.out.println(a2.toString());\n" + 
			"         return null;						   // (2)\n" + 
			"    }\n" +
			"}\n",
			"Client.java",
			"import ctest.C;\n" +
			"public class Client {\n" + 
			"    void test(C c) {\n" + 
			"         String s = c.m2(null);               // (3)\n" + 
			"    }\n" + 
			"}\n"			
		},
		"----------\n" + 
		"1. ERROR in ctest\\C.java (at line 5)\n" + 
		"	return null;                         // (1)\n" + 
		"	       ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n" + 
		"2. ERROR in ctest\\C.java (at line 9)\n" + 
		"	return null;						   // (2)\n" + 
		"	       ^^^^\n" + 
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" + 
		"----------\n" +
		"----------\n" + 
		"1. ERROR in Client.java (at line 4)\n" + 
		"	String s = c.m2(null);               // (3)\n" + 
		"	                ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n",
		libs,		
		true /* shouldFlush*/,
		options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388281
// Conflicting annotations from several indirect super interfaces must be detected
public void testBug388281_06() {
	String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "Test388281.jar";
	String[] libs = new String[this.LIBS.length + 1];
	System.arraycopy(this.LIBS, 0, libs, 0, this.LIBS.length);
	libs[this.LIBS.length] = path;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_INHERIT_NULL_ANNOTATIONS, JavaCore.ENABLED);
	runNegativeTest(
		new String[] {
			"ctest/C.java",
			"package ctest;\n" +
			"public class C extends c.C2 implements i2.I2A {\n" + // neither super has explicit annotations,
																  // but C2 inherits those from the default applicable at its super interface i2.I2
																  // whereas I2A cancels that same default
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in ctest\\C.java (at line 2)\n" + 
		"	public class C extends c.C2 implements i2.I2A {\n" + 
		"	             ^\n" + 
		"The method m2(Object) from C2 cannot implement the corresponding method from I2A due to incompatible nullness constraints\n" + 
		"----------\n" + 
		"2. ERROR in ctest\\C.java (at line 2)\n" + 
		"	public class C extends c.C2 implements i2.I2A {\n" + 
		"	             ^\n" + 
		"The method m1(Object) from C2 cannot implement the corresponding method from I2A due to incompatible nullness constraints\n" + 
		"----------\n",
		libs,		
		true /* shouldFlush*/,
		options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388281
// report conflict between inheritance and default
public void testBug388281_07() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_INHERIT_NULL_ANNOTATIONS, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"p1/Super.java",
			"package p1;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Super {\n" +
			"    public @Nullable Object m(@Nullable Object arg) {\n" +
			"        return null;" +
			"    }\n" +
			"}\n",
			"p2/Sub.java",
			"package p2;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class Sub extends p1.Super {\n" +
			"    @Override\n" +
			"    public Object m(Object arg) { // (a)+(b) conflict at arg and return\n" +
			"        System.out.println(arg.toString()); // (1)\n" +
			"        return null;\n" +
			"    }\n" +
			"}\n",
			"Client.java",
			"public class Client {\n" +
			"    void test(p2.Sub s) {\n" +
			"        Object result = s.m(null);\n" +
			"        System.out.println(result.toString());  // (2)\n" +
			"    }\n" +
			"}\n"
		}, 
		options,
		"----------\n" + 
		"1. ERROR in p2\\Sub.java (at line 6)\n" + 
		"	public Object m(Object arg) { // (a)+(b) conflict at arg and return\n" + 
		"	       ^^^^^^\n" + 
		"The default \'@NonNull\' conflicts with the inherited \'@Nullable\' annotation in the overridden method from Super \n" + 
		"----------\n" + 
		"2. ERROR in p2\\Sub.java (at line 6)\n" + 
		"	public Object m(Object arg) { // (a)+(b) conflict at arg and return\n" + 
		"	                       ^^^\n" + 
		"The default \'@NonNull\' conflicts with the inherited \'@Nullable\' annotation in the overridden method from Super \n" + 
		"----------\n" + 
		"3. ERROR in p2\\Sub.java (at line 7)\n" + 
		"	System.out.println(arg.toString()); // (1)\n" + 
		"	                   ^^^\n" + 
		"Potential null pointer access: The variable arg may be null at this location\n" + 
		"----------\n" + 
		"----------\n" + 
		"1. ERROR in Client.java (at line 4)\n" + 
		"	System.out.println(result.toString());  // (2)\n" + 
		"	                   ^^^^^^\n" + 
		"Potential null pointer access: The variable result may be null at this location\n" + 
		"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388281
// report conflict between inheritance and default - binary types
public void testBug388281_08() {
	String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "Test388281.jar";
	String[] libs = new String[this.LIBS.length + 1];
	System.arraycopy(this.LIBS, 0, libs, 0, this.LIBS.length);
	libs[this.LIBS.length] = path;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_INHERIT_NULL_ANNOTATIONS, JavaCore.ENABLED);
	runNegativeTest(
		new String[] {
			"ctest/Ctest.java",
			"package ctest;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class Ctest implements i2.II {\n" +
			"    public Object m1(@Nullable Object a1) { // silent: conflict at a1 avoided\n" + 
			"		return new Object();\n" + 
			"    }\n" + 
			"    public String m2(Object a2) { // (a) conflict at return\n" + 
			"    	return null;\n" + 
			"    }\n" + 
			"    public String m1(Object o1, Object o2) { // (b) conflict at o1\n" +
			"        System.out.println(o1.toString()); // (1) inherited @Nullable\n" +  
			"        return null; // (2) @NonNullByDefault in i2.II\n" + 
			"    }\n" +
			"}\n",
			"Client.java",
			"public class Client {\n" +
			"    void test(ctest.Ctest c) {\n" +
			"        Object result = c.m1(null, null); // (3) 2nd arg @NonNullByDefault from i2.II\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in ctest\\Ctest.java (at line 8)\n" + 
		"	public String m2(Object a2) { // (a) conflict at return\n" + 
		"	       ^^^^^^\n" + 
		"The default \'@NonNull\' conflicts with the inherited \'@Nullable\' annotation in the overridden method from I \n" + 
		"----------\n" + 
		"2. ERROR in ctest\\Ctest.java (at line 11)\n" + 
		"	public String m1(Object o1, Object o2) { // (b) conflict at o1\n" + 
		"	                        ^^\n" + 
		"The default \'@NonNull\' conflicts with the inherited \'@Nullable\' annotation in the overridden method from II \n" + 
		"----------\n" + 
		"3. ERROR in ctest\\Ctest.java (at line 12)\n" + 
		"	System.out.println(o1.toString()); // (1) inherited @Nullable\n" + 
		"	                   ^^\n" + 
		"Potential null pointer access: The variable o1 may be null at this location\n" + 
		"----------\n" + 
		"4. ERROR in ctest\\Ctest.java (at line 13)\n" + 
		"	return null; // (2) @NonNullByDefault in i2.II\n" + 
		"	       ^^^^\n" + 
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" + 
		"----------\n" + 
		"----------\n" + 
		"1. ERROR in Client.java (at line 3)\n" + 
		"	Object result = c.m1(null, null); // (3) 2nd arg @NonNullByDefault from i2.II\n" + 
		"	                           ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n",
		libs,
		true, // should flush
		options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388281
// difference between inherited abstract & non-abstract methods
public void testBug388281_09() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_INHERIT_NULL_ANNOTATIONS, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"p1/Super.java",
			"package p1;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public abstract class Super {\n" +
			"    public abstract @NonNull Object compatible(@Nullable Object arg);\n" +
			"    public @Nullable Object incompatible(int dummy, @NonNull Object arg) {\n" +
			"        return null;" +
			"    }\n" +
			"}\n",
			"p1/I.java",
			"package p1;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public interface I {\n" +
			"    public @Nullable Object compatible(@NonNull Object arg);\n" +
			"    public @NonNull Object incompatible(int dummy, @Nullable Object arg);\n" +
			"}\n",
			"p2/Sub.java",
			"package p2;\n" +
			"public class Sub extends p1.Super implements p1.I {\n" +
			"    @Override\n" +
			"    public Object compatible(Object arg) {\n" +
			"        return this;\n" +
			"    }\n" +
			"    @Override\n" +
			"    public Object incompatible(int dummy, Object arg) {\n" +
			"        return null;\n" +
			"    }\n" +
			"}\n"
		}, 
		options,
		"----------\n" + 
		"1. ERROR in p2\\Sub.java (at line 4)\n" + 
		"	public Object compatible(Object arg) {\n" + 
		"	       ^^^^^^\n" + 
		"Conflict between inherited null annotations \'@Nullable\' declared in I versus \'@NonNull\' declared in Super \n" + 
		"----------\n" + 
		"2. ERROR in p2\\Sub.java (at line 4)\n" + 
		"	public Object compatible(Object arg) {\n" + 
		"	                         ^^^^^^\n" + 
		"Conflict between inherited null annotations \'@NonNull\' declared in I versus \'@Nullable\' declared in Super \n" + 
		"----------\n" + 
		"3. ERROR in p2\\Sub.java (at line 8)\n" + 
		"	public Object incompatible(int dummy, Object arg) {\n" + 
		"	       ^^^^^^\n" + 
		"Conflict between inherited null annotations \'@NonNull\' declared in I versus \'@Nullable\' declared in Super \n" + 
		"----------\n" + 
		"4. ERROR in p2\\Sub.java (at line 8)\n" + 
		"	public Object incompatible(int dummy, Object arg) {\n" + 
		"	                                      ^^^^^^\n" + 
		"Conflict between inherited null annotations \'@Nullable\' declared in I versus \'@NonNull\' declared in Super \n" + 
		"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388281
// respect inherited @NonNull also inside the method body, see comment 28
public void testBug388281_10() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_INHERIT_NULL_ANNOTATIONS, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"p1/Super.java",
			"package p1;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Super {\n" +
			"    public void m(@NonNull Object arg) {}\n" +
			"}\n",
			"p2/Sub.java",
			"package p2;\n" +
			"public class Sub extends p1.Super  {\n" +
			"    @Override\n" +
			"    public void m(Object arg) {\n" +
			"        arg = null;\n" +
			"    }\n" +
			"}\n"
		}, 
		options,
		"----------\n" + 
		"1. ERROR in p2\\Sub.java (at line 5)\n" + 
		"	arg = null;\n" + 
		"	      ^^^^\n" + 
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" + 
		"----------\n"); 
}

// https://bugs.eclipse.org/382069 - [null] Make the null analysis consider JUnit's assertNotNull similarly to assertions
// junit's assertNull vs. a @NonNull field / expression
public void testBug382069_j() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			NullReferenceTestAsserts.JUNIT_ASSERT_NAME,
			NullReferenceTestAsserts.JUNIT_ASSERT_CONTENT,		
			"X.java",
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"public class X {\n" +
			"  @NonNull String o1 = \"\";\n" +
			"  boolean foo() {\n" +
			"    junit.framework.Assert.assertNull(\"something's wrong\", o1);\n" + // always fails due to @NonNull
			"    return false; // dead code\n" +
			"  }\n" +
			"  void bar() {\n" +
			"      junit.framework.Assert.assertNull(\"\");\n" + // constantly false
			"      return; // dead code\n" +
			"  }\n" +
			"  void zork() {\n" +
			"      junit.framework.Assert.assertNotNull(null);\n" + // constantly false
			"      return; // dead code\n" +
			"  }\n" +
			"}\n"},
			options,
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	return false; // dead code\n" + 
			"	^^^^^^^^^^^^^\n" + 
			"Dead code\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 10)\n" + 
			"	return; // dead code\n" + 
			"	^^^^^^^\n" + 
			"Dead code\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 14)\n" + 
			"	return; // dead code\n" + 
			"	^^^^^^^\n" + 
			"Dead code\n" + 
			"----------\n");
}

// https://bugs.eclipse.org/382069 - [null] Make the null analysis consider JUnit's assertNotNull similarly to assertions
// junit's assertNonNull et al. affecting a @Nullable field using syntactic analysis
public void testBug382069_k() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			NullReferenceTestAsserts.JUNIT_ASSERT_NAME,
			NullReferenceTestAsserts.JUNIT_ASSERT_CONTENT,		
			"X.java",
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"public class X {\n" +
			"  @Nullable String o1;\n" +
			"  int foo() {\n" +
			"    junit.framework.Assert.assertNotNull(\"something's wrong\", o1);\n" +
			"    return o1.length();\n" +
			"  }\n" +
			"  int bar(int i) {\n" +
			"    junit.framework.Assert.assertNotNull(o1);\n" +
			"    i++;\n" + // expire
			"    return o1.length(); // no longer protected\n" +
			"  }\n" +
			"  int garp() {\n" +
			"    junit.framework.Assert.assertFalse(\"something's wrong\", o1 == null);\n" +
			"    return o1.length();\n" +
			"  }\n" +
			"  int zipp() {\n" +
			"    junit.framework.Assert.assertTrue(\"something's wrong\", o1 != null);\n" +
			"    return o1.length();\n" +
			"  }\n" +
			"}\n"},
			options,
			"----------\n" + 
			"1. ERROR in X.java (at line 11)\n" + 
			"	return o1.length(); // no longer protected\n" + 
			"	       ^^\n" + 
			"Potential null pointer access: The field o1 is declared as @Nullable\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/400761: [compiler][null] null may be return as boolean without a diagnostic
public void test_conditional_expression_1() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"	boolean badFunction5(int i) {\n" + 
			"		// expected a potential null problem:\n" + 
			"		return i > 0 ? true : getBoolean();\n" + 
			"	}\n" +
			"	private @Nullable Boolean getBoolean() {\n" + 
			"		return null;\n" + 
			"	}\n" +
			"}\n"},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	return i > 0 ? true : getBoolean();\n" + 
		"	                      ^^^^^^^^^^^^\n" + 
		"Potential null pointer access: This expression of type Boolean may be null but requires auto-unboxing\n" + 
		"----------\n");
}

// Bug 403086 - [compiler][null] include the effect of 'assert' in syntactic null analysis for fields
public void testBug403086_1() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	customOptions.put(JavaCore.COMPILER_PB_INCLUDE_ASSERTS_IN_NULL_ANALYSIS, JavaCore.ENABLED);
	customOptions.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runConformTestWithLibs(
		new String[] {
			NullReferenceTestAsserts.JUNIT_ASSERT_NAME,
			NullReferenceTestAsserts.JUNIT_ASSERT_CONTENT,		
			"Y.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"class Y {\n" +
			"	@Nullable String str;\n" +
			"	int foo(@Nullable String str2) {\n" +
			"		int i;\n" +
			"		junit.framework.Assert.assertNotNull(str);\n" +
			"		i = str.length();\n" +
			"\n" +
			"		assert this.str != null;\n" +
			"		i = str.length();\n" +
			"\n" +
			"		return i;\n" +
			"	}\n" +
			"}\n"
		},
		customOptions,
		"");
}

//Bug 403086 - [compiler][null] include the effect of 'assert' in syntactic null analysis for fields
public void testBug403086_2() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	customOptions.put(JavaCore.COMPILER_PB_INCLUDE_ASSERTS_IN_NULL_ANALYSIS, JavaCore.ENABLED);
	customOptions.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runConformTestWithLibs(
		new String[] {
			NullReferenceTestAsserts.JUNIT_ASSERT_NAME,
			NullReferenceTestAsserts.JUNIT_ASSERT_CONTENT,		
			"Y.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"class Y {\n" +
			"	@Nullable String str;\n" +
			"	int foo(@Nullable String str2) {\n" +
			"		int i;\n" +
			"		junit.framework.Assert.assertNotNull(str);\n" +
			"		i = str.length();\n" +
			"\n" +
			"		assert ! (this.str == null);\n" +
			"		i = str.length();\n" +
			"\n" +
			"		return i;\n" +
			"	}\n" +
			"}\n"
		},
		customOptions,
		"");
}

// https://bugs.eclipse.org/412076 - [compiler] @NonNullByDefault doesn't work for varargs parameter when in generic interface
public void testBug412076() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_MISSING_OVERRIDE_ANNOTATION, JavaCore.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"Foo.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" + 
			"public interface Foo<V> {\n" + 
			"  V bar(String... values);\n" + 
			"  V foo(String value);\n" + 
			"}\n"
		},
		options,
		"");
	runConformTestWithLibs(
		new String[] {
			"FooImpl.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" + 
			"public class FooImpl implements Foo<String> {\n" + 
			"  public String bar(final String... values) {\n" + 
			"    return (\"\");\n" + 
			"  }\n" + 
			"  public String foo(final String value) {\n" + 
			"    return (\"\");\n" + 
			"  }\n" + 
			"}\n"
		},
		options,
		"");	
}

public void testBug413460() {
	runConformTestWithLibs(
		new String[] {
			"Class2.java",
			"\n" + 
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" + 
			"public class Class2 {\n" + 
			"	public class Class3 {\n" + 
			"		public Class3(String nonNullArg) {\n" + 
			"			assert nonNullArg != null;\n" + 
			"		}\n" + 
			"	}\n" + 
			"\n" + 
			"	public Class2(String nonNullArg) {\n" + 
			"		assert nonNullArg != null;\n" + 
			"	}\n" + 
			"\n" + 
			"	public static Class2 create(String nonNullArg) {\n" + 
			"		return new Class2(nonNullArg);\n" + 
			"	}\n" + 
			"}\n"
		}, 
		getCompilerOptions(), 
		"");
	runNegativeTestWithLibs(false,
		new String[] {
			"Class1.java",
			"public class Class1 {\n" + 
			"	public static Class2 works() {\n" + 
			"		return Class2.create(null);\n" + 
			"	}\n" + 
			"\n" + 
			"	public static Class2 bug() {\n" + 
			"		return new Class2(null);\n" + 
			"	}\n" + 
			"\n" + 
			"	public static Class2.Class3 qualifiedbug() {\n" + 
			"		return new Class2(\"\").new Class3(null);\n" + 
			"	}\n" + 
			"}\n"
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in Class1.java (at line 3)\n" + 
		"	return Class2.create(null);\n" + 
		"	                     ^^^^\n" + 
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" + 
		"----------\n" +
		"2. ERROR in Class1.java (at line 7)\n" + 
		"	return new Class2(null);\n" + 
		"	                  ^^^^\n" + 
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" + 
		"----------\n" +
		"3. ERROR in Class1.java (at line 11)\n" + 
		"	return new Class2(\"\").new Class3(null);\n" + 
		"	                                 ^^^^\n" + 
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" + 
		"----------\n");
}
public void testBug416267() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void test() {\n" +
			"		Missing m = new Missing() { };\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	Missing m = new Missing() { };\n" + 
		"	^^^^^^^\n" + 
		"Missing cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	Missing m = new Missing() { };\n" + 
		"	                ^^^^^^^\n" + 
		"Missing cannot be resolved to a type\n" + 
		"----------\n");
}
// duplicate of bug 416267
public void testBug418843() {
	runNegativeTestWithLibs(
		new String[] {
			"TestEnum.java",
			"public enum TestEnum {\n" + 
			"	TestEntry(1){};\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in TestEnum.java (at line 2)\n" + 
		"	TestEntry(1){};\n" + 
		"	^^^^^^^^^\n" + 
		"The constructor TestEnum(int) is undefined\n" + 
		"----------\n");
}
}
