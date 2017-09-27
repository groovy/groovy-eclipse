/*******************************************************************************
 * Copyright (c) 2010, 2012 GK Software AG and others.
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

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
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
//		TESTS_NAMES = new String[] { "testBug385626" };
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
		String[] defaultLibs = getDefaultClassPaths();
		int len = defaultLibs.length;
		this.LIBS = new String[len+1];
		System.arraycopy(defaultLibs, 0, this.LIBS, 0, len);
		File bundleFile = FileLocator.getBundleFile(Platform.getBundle("org.eclipse.jdt.annotation"));
		if (bundleFile.isDirectory())
			this.LIBS[len] = bundleFile.getPath()+"/bin";
		else
			this.LIBS[len] = bundleFile.getPath();
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
			"IX.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public interface IX {\n" +
			"    void printObject(@NonNull Object o);\n" +
			"}\n"
		},
		null /*customOptions*/,
		"");
	runNegativeTestWithLibs(
		false, // don't flush
		new String[] {
			"X.java",
			"public class X implements IX {\n" +
			"    public void printObject(Object o) { System.out.print(o.toString()); }\n" +
			"}\n",
			"M.java",
			"public class M{\n" +
			"    void foo(IX x, Object o) {\n" +
			"        x.printObject(o);\n" +
			"    }\n" +
			"}\n"
		},
		options,
		"----------\n" +
		// additional error:
		"1. ERROR in X.java (at line 2)\n" +
		"	public void printObject(Object o) { System.out.print(o.toString()); }\n" +
		"	                        ^^^^^^\n" +
		"Missing non-null annotation: inherited method from IX declares this parameter as @NonNull\n" +
		"----------\n" +
		// main error:
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
		"----------\n" +
		"2. ERROR in p1\\Y.java (at line 5)\n" +
		"	public @Nullable String getString(String s1, @NonNull String s2, @NonNull String s3) {\n" +
		"	                                  ^^^^^^\n" +
		"Missing non-null annotation: inherited method from IY declares this parameter as @NonNull\n" +
		"----------\n" +
		"3. ERROR in p1\\Y.java (at line 5)\n" +
		"	public @Nullable String getString(String s1, @NonNull String s2, @NonNull String s3) {\n" +
		"	                                             ^^^^^^^^^^^^^^^\n" +
		"Illegal redefinition of parameter s2, inherited method from X declares this parameter as @Nullable\n" +
		"----------\n" +
		"4. ERROR in p1\\Y.java (at line 5)\n" +
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
		"Redundant null check: The method getObject() cannot return null\n" +
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
}
