/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann  - Contribution for bug 295551
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contribution for bug 185682 - Increment/decrement operators mark local variables as read
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

public class AnnotationTest extends AbstractComparableTest {

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which do not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test293" };
//		TESTS_NUMBERS = new int[] { 297 };
//		TESTS_RANGE = new int[] { 294, -1 };
	}

	String reportMissingJavadocComments = null;

	public AnnotationTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return AnnotationTest.class;
	}

	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, CompilerOptions.PRIVATE);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsVisibility, CompilerOptions.PRIVATE);
		if (this.reportMissingJavadocComments != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportMissingJavadocComments);
		return options;
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		this.reportMissingJavadocComments = null;
	}

	public void test001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public @interface X { \n" +
				"	String value(); \n" +
				"}"
			},
			"");
	}

	// check invalid annotation
	public void test002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public @Foo class X {\n" +
				"}\n" +
				"\n" +
				"@interface Foo {\n" +
				"	String value();\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public @Foo class X {\n" +
			"	       ^^^^\n" +
			"The annotation @Foo must define the attribute value\n" +
			"----------\n");
	}

	// check annotation method cannot indirectly return annotation type (circular ref)
	public void test003() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" +
				"	Bar value();\n" +
				"}\n" +
				"\n" +
				"@interface Bar {\n" +
				"	Foo value();\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in Foo.java (at line 2)\n" +
			"	Bar value();\n" +
			"	^^^\n" +
			"Cycle detected: a cycle exists between annotation attributes of Foo and Bar\n" +
			"----------\n" +
			"2. ERROR in Foo.java (at line 6)\n" +
			"	Foo value();\n" +
			"	^^^\n" +
			"Cycle detected: a cycle exists between annotation attributes of Bar and Foo\n" +
			"----------\n");
	    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=85538
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface Nested {\n" +
				"	String name() default \"Hans\";\n" +
				"	N2 nest();\n" +
				"}\n" +
				"@interface N2 {\n" +
				"	Nested n2() default @Nested(name=\"Haus\", nest= @N2);\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	N2 nest();\n" +
			"	^^\n" +
			"Cycle detected: a cycle exists between annotation attributes of Nested and N2\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	Nested n2() default @Nested(name=\"Haus\", nest= @N2);\n" +
			"	^^^^^^\n" +
			"Cycle detected: a cycle exists between annotation attributes of N2 and Nested\n" +
			"----------\n");
	}

	// check annotation method cannot directly return annotation type
	public void test004() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" +
				"	Foo value();\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in Foo.java (at line 2)\n" +
			"	Foo value();\n" +
			"	^^^\n" +
			"Cycle detected: the annotation type Foo cannot contain attributes of the annotation type itself\n" +
			"----------\n");
	}

	// check annotation type cannot have superclass
	public void test005() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"public @interface Foo extends Object {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in Foo.java (at line 1)\n" +
			"	public @interface Foo extends Object {\n" +
			"	                  ^^^\n" +
			"Annotation type declaration cannot have an explicit superclass\n" +
			"----------\n");
	}

	// check annotation type cannot have superinterfaces
	public void test006() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"public @interface Foo implements Cloneable {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in Foo.java (at line 1)\n" +
			"	public @interface Foo implements Cloneable {\n" +
			"	                  ^^^\n" +
			"Annotation type declaration cannot have explicit superinterfaces\n" +
			"----------\n");
	}

	// check annotation method cannot be specified parameters
	// TODO (olivier) unoptimal syntax error -> no parameter for annotation method?
	public void test007() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" +
				"	String value(int i);\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in Foo.java (at line 2)\n" +
			"	String value(int i);\n" +
			"	       ^^^^^^^^^^^^\n" +
			"Annotation attributes cannot have parameters\n" +
			"----------\n");
	}

	// annotation method cannot be generic?
	public void test008() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" +
				"	<T> T value();\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in Foo.java (at line 2)\n" +
			"	<T> T value();\n" +
			"	    ^\n" +
			"Invalid type T for the annotation attribute Foo.value; only primitive type, String, Class, annotation, enumeration are permitted or 1-dimensional arrays thereof\n" +
			"----------\n" +
			"2. ERROR in Foo.java (at line 2)\n" +
			"	<T> T value();\n" +
			"	      ^^^^^^^\n" +
			"Annotation attributes cannot be generic\n" +
			"----------\n");
	}

	// check annotation method return type
	public void test009() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public @interface X {\n" +
				"	\n" +
				"	Runnable value();\n" +
				"}\n"
			},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	Runnable value();\n" +
		"	^^^^^^^^\n" +
		"Invalid type Runnable for the annotation attribute X.value; only primitive type, String, Class, annotation, enumeration are permitted or 1-dimensional arrays thereof\n" +
		"----------\n");
	}

	// check annotation method missing return type
	// TODO (olivier) we should get rid of syntax error here (tolerate invalid constructor scenario)
	public void test010() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public @interface X {\n" +
				"	\n" +
				"	value();\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	value();\n" +
			"	^^^^^^^\n" +
			"Return type for the method is missing\n" +
			"----------\n");
	}

	// check annotation denotes annotation type
	public void test011() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@Object\n" +
				"public class X {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	@Object\n" +
			"	 ^^^^^^\n" +
			"Type mismatch: cannot convert from Object to Annotation\n" +
			"----------\n");
	}

	// check for duplicate annotations
	public void test012() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@Foo @Foo\n" +
				"public class X {\n" +
				"}\n" +
				"@interface Foo {}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	@Foo @Foo\n" +
			"	^^^^\n" +
			"Duplicate annotation @Foo\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	@Foo @Foo\n" +
			"	     ^^^^\n" +
			"Duplicate annotation @Foo\n" +
			"----------\n");
	}

	// check single member annotation - no need to specify value if member has default value
	public void test013() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@Foo(\"hello\") public class X {\n" +
				"}\n" +
				"\n" +
				"@interface Foo {\n" +
				"	String id() default \"\";\n" +
				"	String value() default \"\";\n" +
				"}\n"
			},
			"");
	}

	// check single member annotation -  need to speficy value if member has no default value
	public void test014() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@Foo(\"hello\") public class X {\n" +
				"}\n" +
				"\n" +
				"@interface Foo {\n" +
				"	String id() default \"\";\n" +
				"	String value() default \"\";\n" +
				"	String foo();\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	@Foo(\"hello\") public class X {\n" +
			"	^^^^\n" +
			"The annotation @Foo must define the attribute foo\n" +
			"----------\n");
	}

	// check normal annotation -  need to speficy value if member has no default value
	public void test015() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@Foo(\n" +
				"		id = \"hello\") public class X {\n" +
				"}\n" +
				"\n" +
				"@interface Foo {\n" +
				"	String id() default \"\";\n" +
				"	String value() default \"\";\n" +
				"	String foo();\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	@Foo(\n" +
			"	^^^^\n" +
			"The annotation @Foo must define the attribute foo\n" +
			"----------\n");
	}

	// check normal annotation - if single member, no need to be named 'value'
	public void test016() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface Name {\n" +
				"	String first();\n" +
				"	String last();\n" +
				"}\n" +
				"@interface Author {\n" +
				"	Name name();\n" +
				"}\n" +
				"public class X {\n" +
				"	\n" +
				"	@Author(name = @Name(first=\"Bill\", last=\"Yboy\")) \n" +
				"	void foo() {\n" +
				"	}\n" +
				"}\n"
			},
			"");
	}

	// check single member annotation can only refer to 'value' member
	public void test017() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface Name {\n" +
				"	String first();\n" +
				"	String last();\n" +
				"}\n" +
				"@interface Author {\n" +
				"	Name name();\n" +
				"}\n" +
				"@Author(@Name(first=\"Joe\",last=\"Hacker\")) \n" +
				"public class X {\n" +
				"	\n" +
				"	@Author(name = @Name(first=\"Bill\", last=\"Yboy\")) \n" +
				"	void foo() {\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	@Author(@Name(first=\"Joe\",last=\"Hacker\")) \n" +
			"	^^^^^^^\n" +
			"The annotation @Author must define the attribute name\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 8)\n" +
			"	@Author(@Name(first=\"Joe\",last=\"Hacker\")) \n" +
			"	        ^^^^^\n" +
			"The attribute value is undefined for the annotation type Author\n" +
			"----------\n");
	}

	// check for duplicate member value pairs
	public void test018() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface Name {\n" +
				"	String first();\n" +
				"	String last();\n" +
				"}\n" +
				"@interface Author {\n" +
				"	Name name();\n" +
				"}\n" +
				"public class X {\n" +
				"	\n" +
				"	@Author(name = @Name(first=\"Bill\", last=\"Yboy\", last=\"dup\")) \n" +
				"	void foo() {\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	@Author(name = @Name(first=\"Bill\", last=\"Yboy\", last=\"dup\")) \n" +
			"	                                   ^^^^\n" +
			"Duplicate attribute last in annotation @Name\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 10)\n" +
			"	@Author(name = @Name(first=\"Bill\", last=\"Yboy\", last=\"dup\")) \n" +
			"	                                                ^^^^\n" +
			"Duplicate attribute last in annotation @Name\n" +
			"----------\n",
			JavacTestOptions.EclipseJustification.EclipseJustification0001);
	}

	// check for duplicate member value pairs - simplified to check javac
	public void test018b() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface Name {\n" +
				"	String first();\n" +
				"	String last();\n" +
				"}\n" +
				"public class X {\n" +
				"	@Name(first=\"Bill\", last=\"Yboy\", last=\"dup\")\n" +
				"	void foo() {\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	@Name(first=\"Bill\", last=\"Yboy\", last=\"dup\")\n" +
			"	                    ^^^^\n" +
			"Duplicate attribute last in annotation @Name\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	@Name(first=\"Bill\", last=\"Yboy\", last=\"dup\")\n" +
			"	                                 ^^^^\n" +
			"Duplicate attribute last in annotation @Name\n" +
			"----------\n");
	}
	// check class annotation member value must be a class literal
	public void test019() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface Foo {\n" +
				"	Class value() default X.clazz();\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"	@Foo( clazz() )\n" +
				"	void foo() {}\n" +
				"	static Class clazz() { return X.class; }\n" +
				"}\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 2)\n" +
			"	Class value() default X.clazz();\n" +
			"	^^^^^\n" +
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 2)\n" +
			"	Class value() default X.clazz();\n" +
			"	                      ^^^^^^^^^\n" +
			"The value for annotation attribute Foo.value must be a class literal\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	@Foo( clazz() )\n" +
			"	      ^^^^^^^\n" +
			"The value for annotation attribute Foo.value must be a class literal\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 8)\n" +
			"	static Class clazz() { return X.class; }\n" +
			"	       ^^^^^\n" +
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
			"----------\n");
	}

	// check primitive annotation member value must be a constant
	public void test020() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface Foo {\n" +
				"	int value() default X.val();\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"	@Foo( val() )\n" +
				"	void foo() {}\n" +
				"	static int val() { return 0; }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	int value() default X.val();\n" +
			"	                    ^^^^^^^\n" +
			"The value for annotation attribute Foo.value must be a constant expression\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	@Foo( val() )\n" +
			"	      ^^^^^\n" +
			"The value for annotation attribute Foo.value must be a constant expression\n" +
			"----------\n");
	}

	// check String annotation member value must be a constant
	public void test021() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface Foo {\n" +
				"	String value() default X.val();\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"	@Foo( val() )\n" +
				"	void foo() {}\n" +
				"	static String val() { return \"\"; }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	String value() default X.val();\n" +
			"	                       ^^^^^^^\n" +
			"The value for annotation attribute Foo.value must be a constant expression\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	@Foo( val() )\n" +
			"	      ^^^^^\n" +
			"The value for annotation attribute Foo.value must be a constant expression\n" +
			"----------\n");
	}
	// check String annotation member value must be a constant
	public void test022() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface Foo {\n" +
				"	String[] value() default null;\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"	@Foo( null )\n" +
				"	void foo() {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	String[] value() default null;\n" +
			"	                         ^^^^\n" +
			"The value for annotation attribute Foo.value must be a constant expression\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	@Foo( null )\n" +
			"	      ^^^^\n" +
			"The value for annotation attribute Foo.value must be a constant expression\n" +
			"----------\n");
	}

	// check use of array initializer
	public void test023() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface Foo {\n" +
				"	String[] value() default {};\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"	@Foo( {} )\n" +
				"	void foo() {}\n" +
				"}\n"
			},
			"");
	}

	// check use of binary annotation - check referencing binary annotation
	public void test024() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" +
				"	String[] value() default {};\n" +
				"}\n"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	@Foo({})\n" +
				"	void foo() {}\n" +
				"}\n"
			},
			"",
			null,
			false,
			null);
	}

	// check use of binary annotation - check default value presence
	public void test025() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" +
				"	String[] value() default {};\n" +
				"}\n"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	@Foo()\n" +
				"	void foo() {}\n" +
				"}\n"
			},
			"",
			null,
			false,
			null);
	}

	// check use of binary annotation - check default value presence
	public void test026() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" +
				"		int value() default 8;\n" +
				"}\n"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	@Foo()\n" +
				"	void foo() {}\n" +
				"}\n"
			},
			"",
			null,
			false,
			null);
	}

	// check use of binary annotation - check default value presence
	public void test027() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" +
				"		byte value() default (byte)255;\n" +
				"}\n"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	@Foo()\n" +
				"	void foo() {}\n" +
				"}\n"
			},
			"",
			null,
			false,
			null);
	}

	// check use of binary annotation - check default value presence
	public void test028() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" +
				"		boolean value() default true;\n" +
				"}\n"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	@Foo()\n" +
				"	void foo() {}\n" +
				"}\n"
			},
			"",
			null,
			false,
			null);
	}

	// check use of binary annotation - check default value presence
	public void test029() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" +
				"		char value() default ' ';\n" +
				"}\n"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	@Foo()\n" +
				"	void foo() {}\n" +
				"}\n"
			},
			"",
			null,
			false,
			null);
	}

	// check use of binary annotation - check default value presence
	public void test030() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" +
				"		short value() default (short)1024;\n" +
				"}\n"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	@Foo()\n" +
				"	void foo() {}\n" +
				"}\n"
			},
			"",
			null,
			false,
			null);
	}

	// check use of binary annotation - check default value presence
	public void test031() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" +
				"		double value() default 0.0;\n" +
				"}\n"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	@Foo()\n" +
				"	void foo() {}\n" +
				"}\n"
			},
			"",
			null,
			false,
			null);
	}

	// check use of binary annotation - check default value presence
	public void test032() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" +
				"		float value() default -0.0f;\n" +
				"}\n"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	@Foo()\n" +
				"	void foo() {}\n" +
				"}\n"
			},
			"",
			null,
			false,
			null);
	}

	// check use of binary annotation - check default value presence
	public void test033() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" +
				"		long value() default 1234567890L;\n" +
				"}\n"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	@Foo()\n" +
				"	void foo() {}\n" +
				"}\n"
			},
			"",
			null,
			false,
			null);
	}

	// check use of binary annotation - check default value presence
	public void test034() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"public @interface Foo {\n" +
				"		String value() default \"Hello, World\";\n" +
				"}\n"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	@Foo()\n" +
				"	void foo() {}\n" +
				"}\n"
			},
			"",
			null,
			false,
			null);
	}

	// check use of binary annotation - check default value presence
	public void test035() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"enum E {\n" +
				"	CONST1\n" +
				"}\n" +
				"@interface Foo {\n" +
				"	E value() default E.CONST1;\n" +
				"}"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	@Foo()\n" +
				"	void foo() {}\n" +
				"}\n"
			},
			"",
			null,
			false,
			null);
	}

	// check use of binary annotation - check default value presence
	public void test036() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"@interface Foo {\n" +
				"	Class value() default Object.class;\n" +
				"}"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	@Foo()\n" +
				"	void foo() {}\n" +
				"}\n"
			},
			"",
			null,
			false,
			null);
	}

	// check use of binary annotation - check default value presence
	public void test037() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"@interface Y {\n" +
				"	int id() default 8;\n" +
				"	Class type();\n" +
				"}\n" +
				"public @interface Foo {\n" +
				"	Y value() default @Y(id=10,type=Object.class);\n" +
				"}"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	@Foo()\n" +
				"	void foo() {}\n" +
				"}\n"
			},
			"",
			null,
			false,
			null);
	}

	// check use of binary annotation - check default value presence
	public void test038() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"@interface Foo {\n" +
				"	int id() default 8;\n" +
				"	Class type();\n" +
				"}"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"@Foo(type=String.class) public class X {\n" +
				"}"
			},
			"",
			null,
			false,
			null);
	}

	// check annotation member modifiers
	public void test039() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public @interface X {\n" +
				"	native int id() default 0;\n" +
				"}"
			},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	native int id() default 0;\n" +
		"	           ^^^^\n" +
		"Illegal modifier for the annotation attribute X.id; only public & abstract are permitted\n" +
		"----------\n");
	}

	// check annotation array field initializer
	public void test040() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public @interface X {\n" +
				"	int[] tab;\n" +
				"	int[] value();\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	int[] tab;\n" +
			"	      ^^^\n" +
			"The blank final field tab may not have been initialized\n" +
			"----------\n");
	}

	// check annotation array field initializer
	public void test041() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public @interface X {\n" +
				"	int[] tab = value();\n" +
				"	int[] value();\n" +
				"}\n"
			},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	int[] tab = value();\n" +
		"	            ^^^^^\n" +
		"Cannot make a static reference to the non-static method value() from the type X\n" +
		"----------\n");
	}

	// check annotation array field initializer
	public void test042() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public @interface X {\n" +
				"	int[] tab = { 0 , \"aaa\".length() };\n" +
				"}\n"
			},
		"");
	}

	// check annotation field initializer
	public void test043() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public @interface X {\n" +
				"	int value;\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	int value;\n" +
			"	    ^^^^^\n" +
			"The blank final field value may not have been initialized\n" +
			"----------\n");
	}

	// check annotation field initializer
	public void test044() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public @interface X {\n" +
				"	protected int value = 0;\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	protected int value = 0;\n" +
			"	              ^^^^^\n" +
			"Illegal modifier for the annotation field X.value; only public, static & final are permitted\n" +
			"----------\n");
	}

	// check incompatible default values
	public void test045() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface X {\n" +
				"    int id () default 10L; \n" +
				"    int[] ids() default { 10L };\n" +
				"    Class cls() default new Object();\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	int id () default 10L; \n" +
			"	                  ^^^\n" +
			"Type mismatch: cannot convert from long to int\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	int[] ids() default { 10L };\n" +
			"	                      ^^^\n" +
			"Type mismatch: cannot convert from long to int\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 4)\n" +
			"	Class cls() default new Object();\n" +
			"	^^^^^\n" +
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 4)\n" +
			"	Class cls() default new Object();\n" +
			"	                    ^^^^^^^^^^^^\n" +
			"Type mismatch: cannot convert from Object to Class\n" +
			"----------\n");
	}

	// check need for constant pair value
	public void test046() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface I {\n" +
				"    boolean val() default true;\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"	boolean bar() {\n" +
				"		return false;\n" +
				"	}\n" +
				"    @I(val = bar()) void foo() {\n" +
				"    }\n" +
				"}\n"
			},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	@I(val = bar()) void foo() {\n" +
		"	         ^^^^^\n" +
		"The value for annotation attribute I.val must be a constant expression\n" +
		"----------\n");
	}

	// check array handling of singleton
	public void test047() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" +
				"    boolean[] val() default {true};\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    @I(val = false) void foo() {\n" +
				"    }\n" +
				"}\n"
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 0, Locals: 1\n" +
			"  @I(val={false})\n" +
			"  void foo();";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);

		ClassFileReader fileReader = ClassFileReader.read(new File(OUTPUT_DIR + File.separator  +"I.class"));
		assertEquals("Not an annotation type declaration", TypeDeclaration.ANNOTATION_TYPE_DECL, TypeDeclaration.kind(fileReader.getModifiers()));
	}

	// check invalid constant in array initializer
	public void test048() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface I {\n" +
				"     boolean[] value();\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"     @I(value={false, X.class != null }) void foo() {\n" +
				"     }\n" +
				"}\n"
			},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	@I(value={false, X.class != null }) void foo() {\n" +
		"	                 ^^^^^^^^^^^^^^^\n" +
		"The value for annotation attribute I.value must be a constant expression\n" +
		"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=79349
	public void test049() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"\n" +
				"@Documented\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@Target(ElementType.TYPE)\n" +
				"@interface MyAnn {\n" +
				"  String value() default \"Default Message\";\n" +
				"}\n" +
				"\n" +
				"@MyAnn\n" +
				"public class X {\n" +
				"	public @MyAnn void something() { }	\n" +
				"}\n"
			},
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	public @MyAnn void something() { }	\n" +
		"	       ^^^^^^\n" +
		"The annotation @MyAnn is disallowed for this location\n" +
		"----------\n");
	}

	// check array handling of singleton
	public void test050() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" +
				"    String[] value();\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    @I(\"Hello\") void foo() {\n" +
				"    }\n" +
				"}\n"
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 0, Locals: 1\n" +
			"  @I(value={\"Hello\"})\n" +
			"  void foo();";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}

	public void test051() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" +
				"    String value() default \"Hello\";\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    @I(\"Hi\") void foo() {\n" +
				"    }\n" +
				"}\n"
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 0, Locals: 1\n" +
			"  @I(value=\"Hi\")\n" +
			"  void foo();";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}

	public void test052() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" +
				"    int value() default 0;\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    @I(2) void foo() {\n" +
				"    }\n" +
				"}\n"
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 0, Locals: 1\n" +
			"  @I(value=(int) 2)\n" +
			"  void foo();";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	public void test053() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" +
				"    byte value() default 0;\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    @I(2) void foo() {\n" +
				"    }\n" +
				"}\n"
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 0, Locals: 1\n" +
			"  @I(value=(byte) 2)\n" +
			"  void foo();";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	public void test054() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" +
				"    short value() default 0;\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    @I(2) void foo() {\n" +
				"    }\n" +
				"}\n"
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 0, Locals: 1\n" +
			"  @I(value=(short) 2)\n" +
			"  void foo();";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	public void test055() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" +
				"    char value() default ' ';\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    @I('@') void foo() {\n" +
				"    }\n" +
				"}\n"
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 0, Locals: 1\n" +
			"  @I(value=\'@\')\n" +
			"  void foo();";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	public void test056() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" +
				"    long value() default 6;\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    @I(Long.MAX_VALUE) void foo() {\n" +
				"    }\n" +
				"}\n"
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 0, Locals: 1\n" +
			"  @I(value=9223372036854775807L)\n" +
			"  void foo();";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	public void test057() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" +
				"    float value();\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    @I(-0.0f) void foo() {\n" +
				"    }\n" +
				"}\n"
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 0, Locals: 1\n" +
			"  @I(value=-0.0f)\n" +
			"  void foo();";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	public void test058() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" +
				"    double value();\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    @I(-0.0) void foo() {\n" +
				"    }\n" +
				"}\n"
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 0, Locals: 1\n" +
			"  @I(value=-0.0)\n" +
			"  void foo();";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}

	public void test059() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface Foo {\n" +
				"    double value() default 0.0;\n" +
				"    int id();\n" +
				"}\n" +
				"@interface I {\n" +
				"    Foo value();\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    @I(@Foo(id=5)) void foo() {\n" +
				"    }\n" +
				"}\n"
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 0, Locals: 1\n" +
			"  @I(value=@Foo(id=(int) 5))\n" +
			"  void foo();";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}

	public void test060() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"enum Color {" +
				"	BLUE, RED, GREEN\n" +
				"}\n" +
				"@interface I {\n" +
				"    Color value() default Color.GREEN;\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    @I(Color.RED) void foo() {\n" +
				"    }\n" +
				"}\n"
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 0, Locals: 1\n" +
			"  @I(value=Color.RED)\n" +
			"  void foo();";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	public void test061() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"enum Color {" +
				"	BLUE, RED, GREEN\n" +
				"}\n" +
				"@interface I {\n" +
				"    Color[] value() default { Color.GREEN };\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    @I(Color.RED) void foo() {\n" +
				"    }\n" +
				"}\n"
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 0, Locals: 1\n" +
			"  @I(value={Color.RED})\n" +
			"  void foo();";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	public void test062() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface Foo {\n" +
				"    double value() default 0.0;\n" +
				"    int id() default 0;\n" +
				"}\n" +
				"enum Color {" +
				"	BLUE, RED, GREEN\n" +
				"}\n" +
				"@interface I {\n" +
				"    Color[] enums() default { Color.GREEN };\n" +
				"    Foo[] annotations() default { @Foo() };\n" +
				"    int[] ints() default { 0, 1, 2, 3 };\n" +
				"    byte[] bytes() default { 0 };\n" +
				"    short[] shorts() default { 0 };\n" +
				"    long[] longs() default { Long.MIN_VALUE, Long.MAX_VALUE };\n" +
				"    String[] strings() default { \"\" };\n" +
				"    boolean[] booleans() default { true, false };\n" +
				"    float[] floats() default { Float.MAX_VALUE };\n" +
				"    double[] doubles() default { Double.MAX_VALUE };\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    @I(enums=Color.RED,\n" +
				"		annotations=@Foo(),\n" +
				"		ints=2,\n" +
				"		bytes=1,\n" +
				"		shorts=5,\n" +
				"		longs=Long.MIN_VALUE,\n" +
				"		strings=\"Hi\",\n" +
				"		booleans=true,\n" +
				"		floats=0.0f,\n" +
				"		doubles=-0.0) void foo() {\n" +
				"    }\n" +
				"}\n"
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  @I(enums={Color.RED},\n" +
			"    annotations={@Foo},\n" +
			"    ints={(int) 2},\n" +
			"    bytes={(byte) 1},\n" +
			"    shorts={(short) 5},\n" +
			"    longs={-9223372036854775808L},\n" +
			"    strings={\"Hi\"},\n" +
			"    booleans={true},\n" +
			"    floats={0.0f},\n" +
			"    doubles={-0.0})\n" +
			"  void foo();";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 3));
		}
		if (index == -1) {
			assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
		}
	}
	public void test063() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface Foo {\n" +
				"    double value() default 0.0;\n" +
				"    int id() default 0;\n" +
				"}\n" +
				"enum Color {" +
				"	BLUE, RED, GREEN\n" +
				"}\n" +
				"@interface I {\n" +
				"    Color enums() default Color.GREEN;\n" +
				"    Foo annotations() default @Foo();\n" +
				"    int ints() default 0;\n" +
				"    byte bytes() default 0;\n" +
				"    short shorts() default 0;\n" +
				"    long longs() default Long.MIN_VALUE;\n" +
				"    String strings() default \"\";\n" +
				"    boolean booleans() default true;\n" +
				"    float floats() default Float.MAX_VALUE;\n" +
				"    double doubles() default Double.MAX_VALUE;\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    @I(enums=Color.RED,\n" +
				"		annotations=@Foo(),\n" +
				"		ints=2,\n" +
				"		bytes=1,\n" +
				"		shorts=5,\n" +
				"		longs=Long.MIN_VALUE,\n" +
				"		strings=\"Hi\",\n" +
				"		booleans=true,\n" +
				"		floats=0.0f,\n" +
				"		doubles=-0.0) void foo() {\n" +
				"    }\n" +
				"}\n"
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  @I(enums=Color.RED,\n" +
			"    annotations=@Foo,\n" +
			"    ints=(int) 2,\n" +
			"    bytes=(byte) 1,\n" +
			"    shorts=(short) 5,\n" +
			"    longs=-9223372036854775808L,\n" +
			"    strings=\"Hi\",\n" +
			"    booleans=true,\n" +
			"    floats=0.0f,\n" +
			"    doubles=-0.0)\n" +
			"  void foo();";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 3));
		}
		if (index == -1) {
			assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
		}
	}

	public void test064() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" +
				"    String[] names();\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    @I(names={\"Hello\"}) void foo() {\n" +
				"    }\n" +
				"}\n"
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 0, Locals: 1\n" +
			"  @I(names={\"Hello\"})\n" +
			"  void foo();";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=79848
	public void test065() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" +
				"    Class[] classes();\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    @I(classes = {X.class, I.class}) public void foo(){\n" +
				"    }\n" +
				"}\n"
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"  // Method descriptor #6 ()V\n" +
			"  // Stack: 0, Locals: 1\n" +
			"  @I(classes={X,I})\n" +
			"  public void foo();";

		if (actualOutput.indexOf(expectedOutput) == -1) {
			System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actualOutput, 2));
		}
		assertTrue("unexpected bytecode sequence", actualOutput.indexOf(expectedOutput) != -1);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=79844
	public void test066() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" +
				"    short value() default 0;\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    @I(2) void foo() {\n" +
				"    }\n" +
				"}\n" +
				"\n"
			},
		"");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=79844 - variation
	public void test067() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface I {\n" +
				"    int value() default 0L;\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    @I(2) void foo() {\n" +
				"    }\n" +
				"}\n" +
				"\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	int value() default 0L;\n" +
			"	                    ^^\n" +
			"Type mismatch: cannot convert from long to int\n" +
			"----------\n");
	}
	// 79844 - variation
	public void test068() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" +
				"    short[] value() default 2;\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    @I(2) void foo() {\n" +
				"    }\n" +
				"}\n"
			},
		"");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=79844 - variation
	public void test069() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface I {\n" +
				"    short[] value() default { 2 };\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    @I(2) void foo() {\n" +
				"    }\n" +
				"}\n"
			},
		"");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=79847
	public void test070() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface I {\n" +
				"    int[][] ids();\n" +
				"    Object[][] obs();\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"    @I(ids = {{1 , 2}, { 3 }}) public void foo(){\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	int[][] ids();\n" +
			"	^^^^^^^\n" +
			"Invalid type int[][] for the annotation attribute I.ids; only primitive type, String, Class, annotation, enumeration are permitted or 1-dimensional arrays thereof\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Object[][] obs();\n" +
			"	^^^^^^^^^^\n" +
			"Invalid type Object[][] for the annotation attribute I.obs; only primitive type, String, Class, annotation, enumeration are permitted or 1-dimensional arrays thereof\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 8)\n" +
			"	@I(ids = {{1 , 2}, { 3 }}) public void foo(){\n" +
			"	^^\n" +
			"The annotation @I must define the attribute obs\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 8)\n" +
			"	@I(ids = {{1 , 2}, { 3 }}) public void foo(){\n" +
			"	          ^^^^^^^\n" +
			"The value for annotation attribute I.ids must be a constant expression\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 8)\n" +
			"	@I(ids = {{1 , 2}, { 3 }}) public void foo(){\n" +
			"	                   ^^^^^\n" +
			"The value for annotation attribute I.ids must be a constant expression\n" +
			"----------\n");
	}

	public void test071() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface I {\n" +
				"	int hashCode();\n" +
				"	Object clone();\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    @I(hashCode = 0) public void foo(){\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	int hashCode();\n" +
			"	    ^^^^^^^^^^\n" +
			"The annotation type I cannot override the method Annotation.hashCode()\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	Object clone();\n" +
			"	^^^^^^\n" +
			"Invalid type Object for the annotation attribute I.clone; only primitive type, String, Class, annotation, enumeration are permitted or 1-dimensional arrays thereof\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 3)\n" +
			"	Object clone();\n" +
			"	       ^^^^^^^\n" +
			"The annotation type I cannot override the method Object.clone()\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	@I(hashCode = 0) public void foo(){\n" +
			"	^^\n" +
			"The annotation @I must define the attribute clone\n" +
			"----------\n");
	}

	// check annotation cannot refer to inherited methods as attributes
	public void test072() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface I {\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    @I(hashCode = 0) public void foo(){\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	@I(hashCode = 0) public void foo(){\n" +
			"	   ^^^^^^^^\n" +
			"The attribute hashCode is undefined for the annotation type I\n" +
			"----------\n");
	}

	// check code generation of annotation default attribute (autowrapping)
	public void test073() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface Foo {\n" +
				"    double value() default 0.0;\n" +
				"    int id() default 0;\n" +
				"}\n" +
				"enum Color {" +
				"	BLUE, RED, GREEN\n" +
				"}\n" +
				"@interface I {\n" +
				"    Color[] enums() default Color.GREEN;\n" +
				"    Foo[] annotations() default @Foo();\n" +
				"    int[] ints() default 0;\n" +
				"    byte[] bytes() default 1;\n" +
				"    short[] shorts() default 3;\n" +
				"    long[] longs() default Long.MIN_VALUE;\n" +
				"    String[] strings() default \"\";\n" +
				"    boolean[] booleans() default true;\n" +
				"    float[] floats() default Float.MAX_VALUE;\n" +
				"    double[] doubles() default Double.MAX_VALUE;\n" +
				"    Class[] classes() default I.class;\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"    @I(enums=Color.RED,\n" +
				"		annotations=@Foo(),\n" +
				"		ints=2,\n" +
				"		bytes=1,\n" +
				"		shorts=5,\n" +
				"		longs=Long.MIN_VALUE,\n" +
				"		strings=\"Hi\",\n" +
				"		booleans=true,\n" +
				"		floats=0.0f,\n" +
				"		doubles=-0.0) void foo() {\n" +
				"    }\n" +
				"}\n"
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"I.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"abstract @interface I extends java.lang.annotation.Annotation {\n" +
			"  \n" +
			"  // Method descriptor #8 ()[LColor;\n" +
			"  public abstract Color[] enums() default {Color.GREEN};\n" +
			"  \n" +
			"  // Method descriptor #13 ()[LFoo;\n" +
			"  public abstract Foo[] annotations() default {@Foo};\n" +
			"  \n" +
			"  // Method descriptor #16 ()[I\n" +
			"  public abstract int[] ints() default {(int) 0};\n" +
			"  \n" +
			"  // Method descriptor #19 ()[B\n" +
			"  public abstract byte[] bytes() default {(byte) 1};\n" +
			"  \n" +
			"  // Method descriptor #22 ()[S\n" +
			"  public abstract short[] shorts() default {(short) 3};\n" +
			"  \n" +
			"  // Method descriptor #25 ()[J\n" +
			"  public abstract long[] longs() default {-9223372036854775808L};\n" +
			"  \n" +
			"  // Method descriptor #29 ()[Ljava/lang/String;\n" +
			"  public abstract java.lang.String[] strings() default {\"\"};\n" +
			"  \n" +
			"  // Method descriptor #32 ()[Z\n" +
			"  public abstract boolean[] booleans() default {true};\n" +
			"  \n" +
			"  // Method descriptor #34 ()[F\n" +
			"  public abstract float[] floats() default {3.4028235E38f};\n" +
			"  \n" +
			"  // Method descriptor #37 ()[D\n" +
			"  public abstract double[] doubles() default {1.7976931348623157E308};\n" +
			"  \n" +
			"  // Method descriptor #41 ()[Ljava/lang/Class;\n" +
			"  public abstract java.lang.Class[] classes() default {I};\n";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 3));
		}
		if (index == -1) {
			assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
		}
	}
	// check code generation of annotation default attribute non array types
	public void test074() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface Foo {\n" +
				"    double value() default 0.0;\n" +
				"    int id() default 0;\n" +
				"}\n" +
				"enum Color {" +
				"	BLUE, RED, GREEN\n" +
				"}\n" +
				"@interface I {\n" +
				"    Color _enum() default Color.GREEN;\n" +
				"    Foo _annotation() default @Foo;\n" +
				"    int _int() default 0;\n" +
				"    byte _byte() default 1;\n" +
				"    short _short() default 3;\n" +
				"    long _long() default Long.MIN_VALUE;\n" +
				"    String _string() default \"\";\n" +
				"    boolean _boolean() default true;\n" +
				"    float _float() default Float.MAX_VALUE;\n" +
				"    double _double() default Double.MAX_VALUE;\n" +
				"    Class _class() default I.class;\n" +
				"}\n" +
				"public class X {\n" +
				"    @I(_enum=Color.RED,\n" +
				"		_annotation=@Foo(),\n" +
				"		_int=2,\n" +
				"		_byte=1,\n" +
				"		_short=5,\n" +
				"		_long=Long.MIN_VALUE,\n" +
				"		_string=\"Hi\",\n" +
				"		_boolean=true,\n" +
				"		_float=0.0f,\n" +
				"		_double=-0.0) void foo() {\n" +
				"    }\n" +
				"}\n"
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"I.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		String expectedOutput =
			"abstract @interface I extends java.lang.annotation.Annotation {\n" +
			"  \n" +
			"  // Method descriptor #8 ()LColor;\n" +
			"  public abstract Color _enum() default Color.GREEN;\n" +
			"  \n" +
			"  // Method descriptor #13 ()LFoo;\n" +
			"  public abstract Foo _annotation() default @Foo;\n" +
			"  \n" +
			"  // Method descriptor #16 ()I\n" +
			"  public abstract int _int() default (int) 0;\n" +
			"  \n" +
			"  // Method descriptor #19 ()B\n" +
			"  public abstract byte _byte() default (byte) 1;\n" +
			"  \n" +
			"  // Method descriptor #22 ()S\n" +
			"  public abstract short _short() default (short) 3;\n" +
			"  \n" +
			"  // Method descriptor #25 ()J\n" +
			"  public abstract long _long() default -9223372036854775808L;\n" +
			"  \n" +
			"  // Method descriptor #29 ()Ljava/lang/String;\n" +
			"  public abstract java.lang.String _string() default \"\";\n" +
			"  \n" +
			"  // Method descriptor #32 ()Z\n" +
			"  public abstract boolean _boolean() default true;\n" +
			"  \n" +
			"  // Method descriptor #34 ()F\n" +
			"  public abstract float _float() default 3.4028235E38f;\n" +
			"  \n" +
			"  // Method descriptor #37 ()D\n" +
			"  public abstract double _double() default 1.7976931348623157E308;\n" +
			"  \n" +
			"  // Method descriptor #41 ()Ljava/lang/Class;\n" +
			"  public abstract java.lang.Class _class() default I;\n";

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 3));
		}
		if (index == -1) {
			assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
		}
	}
	// check detection of duplicate target element specification
	public void test075() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"\n" +
				"@Target ({FIELD, FIELD})\n" +
				"@interface Tgt {\n" +
				"	E[] foo();\n" +
				"	int[] bar();\n" +
				"}\n" +
				"enum E {\n" +
				"	BLEU, BLANC, ROUGE\n" +
				"}\n" +
				"\n" +
				"@Tgt( foo = { E.BLEU, E.BLEU}, bar = { 0, 0} )\n" +
				"public class X {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	@Target ({FIELD, FIELD})\n" +
			"	                 ^^^^^\n" +
			"Duplicate element FIELD specified in annotation @Target\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 13)\n" +
			"	@Tgt( foo = { E.BLEU, E.BLEU}, bar = { 0, 0} )\n" +
			"	^^^^\n" +
			"The annotation @Tgt is disallowed for this location\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77463
	public void test076() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"private @interface TestAnnot {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	private @interface TestAnnot {\n" +
			"	                   ^^^^^^^^^\n" +
			"Illegal modifier for the annotation type TestAnnot; only public & abstract are permitted\n" +
			"----------\n");
	}
	// check @Override annotation - strictly for superclasses (overrides) and not interfaces (implements)
	public void test077() {
		String expectedOutput = new CompilerOptions(getCompilerOptions()).sourceLevel < ClassFileConstants.JDK1_6
			?	"----------\n" +
				"1. ERROR in X.java (at line 14)\n" +
				"	void foo() {}\n" +
				"	     ^^^^^\n" +
				"The method foo() of type X must override a superclass method\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 18)\n" +
				"	public void baz() {}\n" +
				"	            ^^^^^\n" +
				"The method baz() of type X must override a superclass method\n" +
				"----------\n"
			:	"----------\n" +
				"1. ERROR in X.java (at line 14)\n" +
				"	void foo() {}\n" +
				"	     ^^^^^\n" +
				"The method foo() of type X must override or implement a supertype method\n" +
				"----------\n";
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class Further {\n" +
				"	void bar() {}\n" +
				"}\n" +
				"\n" +
				"class Other extends Further {\n" +
				"}\n" +
				"\n" +
				"interface Baz {\n" +
				"	void baz();\n" +
				"}\n" +
				"\n" +
				"public class X extends Other implements Baz {\n" +
				"	@Override\n" +
				"	void foo() {}\n" +
				"	@Override\n" +
				"	void bar() {}\n" +
				"	@Override\n" +
				"	public void baz() {}\n" +
				"}\n"
			},
			expectedOutput);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80114
	public void test078() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public @interface X {\n" +
				"	X() {}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	X() {}\n" +
			"	^^^\n" +
			"Annotation type declaration cannot have a constructor\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80014
	public void test079() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"\n" +
				"@Retention(RUNTIME)\n" +
				"@Target({TYPE})\n" +
				"@interface Attr {\n" +
				"  public int tst() default -1;\n" +
				"}\n" +
				"\n" +
				"@Attr \n" +
				"public class X {\n" +
				"  public static void main(String args[]) {\n" +
				"  	Object e = X.class.getAnnotation(Attr.class);\n" +
				"  	System.out.print(e);\n" +
				"  }\n" +
				"}"
			},
			"@Attr(tst=-1)");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80014
	public void test080() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"\n" +
				"@Retention(CLASS)\n" +
				"@Target({TYPE})\n" +
				"@interface Attr {\n" +
				"  public int tst() default -1;\n" +
				"}\n" +
				"\n" +
				"@Attr \n" +
				"public class X {\n" +
				"  public static void main(String args[]) {\n" +
				"  	Object e = X.class.getAnnotation(Attr.class);\n" +
				"  	System.out.print(e);\n" +
				"  }\n" +
				"}"
			},
			"null");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80014
	public void test081() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"\n" +
				"@Retention(SOURCE)\n" +
				"@Target({TYPE})\n" +
				"@interface Attr {\n" +
				"  public int tst() default -1;\n" +
				"}\n" +
				"\n" +
				"@Attr \n" +
				"public class X {\n" +
				"  public static void main(String args[]) {\n" +
				"  	Object e = X.class.getAnnotation(Attr.class);\n" +
				"  	System.out.print(e);\n" +
				"  }\n" +
				"}"
			},
			"null");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80014
	public void test082() {
		this.runConformTest(
			new String[] {
				"Attr.java",
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"\n" +
				"@Retention(SOURCE)\n" +
				"@Target({TYPE})\n" +
				"@interface Attr {\n" +
				"  public int tst() default -1;\n" +
				"}",
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"@Attr \n" +
				"public class X {\n" +
				"  public static void main(String args[]) {\n" +
				"  	Object e = X.class.getAnnotation(Attr.class);\n" +
				"  	System.out.print(e);\n" +
				"  }\n" +
				"}"
			},
			"null",
			null,
			false,
			null);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80014
	public void test083() {
		this.runConformTest(
			new String[] {
				"Attr.java",
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"\n" +
				"@Retention(CLASS)\n" +
				"@Target({TYPE})\n" +
				"@interface Attr {\n" +
				"  public int tst() default -1;\n" +
				"}",
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"@Attr \n" +
				"public class X {\n" +
				"  public static void main(String args[]) {\n" +
				"  	Object e = X.class.getAnnotation(Attr.class);\n" +
				"  	System.out.print(e);\n" +
				"  }\n" +
				"}"
			},
			"null",
			null,
			false,
			null);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80014
	public void test084() {
		this.runConformTest(
			new String[] {
				"Attr.java",
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"\n" +
				"@Retention(RUNTIME)\n" +
				"@Target({TYPE})\n" +
				"@interface Attr {\n" +
				"  public int tst() default -1;\n" +
				"}",
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"@Attr \n" +
				"public class X {\n" +
				"  public static void main(String args[]) {\n" +
				"  	Object e = X.class.getAnnotation(Attr.class);\n" +
				"  	System.out.print(e);\n" +
				"  }\n" +
				"}"
			},
			"@Attr(tst=-1)",
			null,
			false,
			null);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=76751
	public void test085() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.Retention;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"  @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) @interface\n" +
				"TestAnnotation {\n" +
				"\n" +
				"    String testAttribute();\n" +
				"\n" +
				"  }\n" +
				"  @TestAnnotation(testAttribute = \"test\") class A {\n" +
				"  }\n" +
				"\n" +
				"  public static void main(String[] args) {\n" +
				"    System.out.print(A.class.isAnnotationPresent(TestAnnotation.class));\n" +
				"  }\n" +
				"\n" +
				"}"
			},
			"true");
	}
	// check handling of empty array initializer
	public void test086() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"\n" +
				"@Target({}) @interface I {}\n" +
				"@I public class X {}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	@I public class X {}\n" +
			"	^^\n" +
			"The annotation @I is disallowed for this location\n" +
			"----------\n");
	}

	// check type targeting annotation also allowed for annotation type
	public void test087() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"\n" +
				"@Target(TYPE)\n" +
				"@interface Annot {\n" +
				"}\n" +
				"\n" +
				"@Annot\n" +
				"public @interface X {\n" +
				"}\n"
			},
			"");
	}

	// check parameter/local target for annotation
	public void test088() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"import static java.lang.annotation.ElementType.*;\n" +
				"\n" +
				"@Target(LOCAL_VARIABLE)\n" +
				"@interface Annot {\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"	void foo(@Annot int i) {\n" +
				"		@Annot int j;\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	void foo(@Annot int i) {\n" +
			"	         ^^^^^^\n" +
			"The annotation @Annot is disallowed for this location\n" +
			"----------\n");
	}

	// Add check for parameter
	public void test089() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"    @Target(ElementType.PARAMETER) @interface I {}\n" +
				"    \n" +
				"    void m(@I int i){\n" +
				"    }\n" +
				"}"
			},
			"");
	}
	// Add check that type includes annotation type
	public void test090() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"    @Target(ElementType.TYPE) @interface Annot1 {}\n" +
				"    \n" +
				"    @Annot1 @interface Annot2 {}\n" +
				"}"
			},
			"");
	}
	// Add check that a field cannot have an annotation targetting TYPE
	public void test091() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"    @Target(ElementType.TYPE) @interface Marker {}\n" +
				"    \n" +
				"    @Marker static int i = 123;\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	@Marker static int i = 123;\n" +
			"	^^^^^^^\n" +
			"The annotation @X.Marker is disallowed for this location\n" +
			"----------\n");
	}
	// Add check that a field cannot have an annotation targetting FIELD
	public void test092() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"\n" +
				"public class X {\n" +
				"\n" +
				"    @Target(ElementType.FIELD) @interface Marker {}\n" +
				"    \n" +
				"    @Marker static int i = 123;\n" +
				"}"
			},
			"");
	}
	// @Inherited can only be used on annotation types
	public void test093() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Inherited;\n" +
				"\n" +
				"@Deprecated\n" +
				"@Inherited\n" +
				"class A {\n" +
				"}\n" +
				"\n" +
				"class B extends A {\n" +
				"}\n" +
				"\n" +
				"class C extends B {\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"	C c;\n" +
				"}\n"
			},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	@Inherited\n" +
		"	^^^^^^^^^^\n" +
		"The annotation @Inherited is disallowed for this location\n" +
		"----------\n");
	}
	// check handling of empty array initializer (binary check)
	public void test094() {
		this.runConformTest(
			new String[] {
				"I.java",
				"import java.lang.annotation.Target;\n" +
				"\n" +
				"@Target({}) @interface I {}",
			},
			"");
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@I public class X {}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	@I public class X {}\n" +
			"	^^\n" +
			"The annotation @I is disallowed for this location\n" +
			"----------\n",
			null,
			false,
			null);
	}

	// check no interaction between Retention and Target (switch fall-thru)
	public void test095() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface Ann {}\n" +
				"\n" +
				"public class X {\n" +
				"	@Ann\n" +
				"	void foo() {}\n" +
				"}\n",
			},
			"");
	}

	// check attributes for parameters
	public void test096() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.Annotation;\n" +
				"import java.lang.reflect.Method;\n" +
				"\n" +
				"@Retention(CLASS) @interface Attr {\n" +
				"}\n" +
				"\n" +
				"@Retention(RUNTIME) @interface Foo {\n" +
				"	int id() default 0;\n" +
				"}\n" +
				"@Foo(id=5) @Attr public class X {\n" +
				"	public void foo(@Foo(id=5) @Attr final int j, @Attr final int k, int n) {\n" +
				"	}\n" +
				"	\n" +
				"	public static void main(String[] args) {\n" +
				"		try {\n" +
				"			Class c = X.class;\n" +
				"			Annotation[] annots = c.getAnnotations();\n" +
				"			System.out.print(annots.length);\n" +
				"			Method method = c.getMethod(\"foo\", Integer.TYPE, Integer.TYPE, Integer.TYPE);\n" +
				"			Annotation[][] annotations = method.getParameterAnnotations();\n" +
				"			final int length = annotations.length;\n" +
				"			System.out.print(length);\n" +
				"			if (length == 3) {\n" +
				"				System.out.print(annotations[0].length);\n" +
				"				System.out.print(annotations[1].length);\n" +
				"				System.out.print(annotations[2].length);\n" +
				"			}\n" +
				"		} catch(NoSuchMethodException e) {\n" +
				"		}\n" +
				"	}\n" +
				"}",
			},
			"13100");
	}

	public void test097() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface I {\n" +
				"	int id default 0;\n" +
				"}\n" +
				"\n" +
				"@I() public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.println(X.class.getAnnotation(I.class));\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	int id default 0;\n" +
			"	       ^^^^^^^\n" +
			"Syntax error on token \"default\", = expected\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80328
	public void test098() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface I {\n" +
				"	int id default 0;\n" +
				"}\n" +
				"\n" +
				"@I() public class X {\n" +
				"	public static void main(String[] s) {\n" +
				"		System.out.println(X.class.getAnnotation(I.class));\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	int id default 0;\n" +
			"	       ^^^^^^^\n" +
			"Syntax error on token \"default\", = expected\n" +
			"----------\n");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80780
	public void test099() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.*;\n" +
				"import java.lang.reflect.Method;\n" +
				"\n" +
				"public class X {\n" +
				"    public static void main(String[] args) {\n" +
				"        Object o = new X();\n" +
				"        for (Method m : o.getClass().getMethods()) {\n" +
				"            if (m.isAnnotationPresent(MyAnon.class)) {\n" +
				"                System.out.println(m.getAnnotation(MyAnon.class).c());\n" +
				"            }\n" +
				"        }\n" +
				"    }\n" +
				"    @MyAnon(c = X.class) \n" +
				"    public void foo() {}\n" +
				"\n" +
				"    @Retention(RetentionPolicy.RUNTIME) \n" +
				"    public @interface MyAnon {\n" +
				"        Class c();\n" +
				"    }\n" +
				"    public interface I {\n" +
				"    }\n" +
				"}"
			},
			"class X");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		String actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);

		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		String expectedOutput = null;
		if (options.targetJDK == ClassFileConstants.JDK1_5) {
			expectedOutput =
				"  Inner classes:\n" +
				"    [inner class info: #66 X$I, outer class info: #1 X\n" +
				"     inner name: #68 I, accessflags: 1545 public abstract static],\n" +
				"    [inner class info: #27 X$MyAnon, outer class info: #1 X\n" +
				"     inner name: #69 MyAnon, accessflags: 9737 public abstract static]\n";
		} else if (options.targetJDK == ClassFileConstants.JDK1_6) {
			expectedOutput =
				"  Inner classes:\n" +
				"    [inner class info: #70 X$I, outer class info: #1 X\n" +
				"     inner name: #72 I, accessflags: 1545 public abstract static],\n" +
				"    [inner class info: #27 X$MyAnon, outer class info: #1 X\n" +
				"     inner name: #73 MyAnon, accessflags: 9737 public abstract static]\n";
		} else {
			return;
		}

		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 3));
		}
		if (index == -1) {
			assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80544
	public void test100() {
		this.runConformTest(
			new String[] {
				"X.java",
				"abstract class Foo {\n" +
				"	abstract protected boolean accept(Object o);\n" +
				"}\n" +
				"\n" +
				"public class X extends Foo {\n" +
				"	@Override \n" +
				"	protected boolean accept(Object o) { return false; }\n" +
				"}\n",
			},
			"");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81148
	public void test101() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Target;\n" +
				"\n" +
				"@Target(Element)\n" +
				"public @interface X {\n" +
				"	\n" +
				"	boolean UML() default false;\n" +
				"	boolean platformDependent() default true;\n" +
				"	boolean OSDependent() default true;\n" +
				"}\n",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	@Target(Element)\n" +
			"	        ^^^^^^^\n" +
			"Element cannot be resolved to a variable\n" +
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80964
	public void test102() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  @TestAnnotation(testAttribute = \"test\") class A {\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    System.out.print(A.class.isAnnotationPresent(TestAnnotation.class));\n" +
				"  }\n" +
				"}",
				"TestAnnotation.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.Retention;\n" +
				"@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) public @interface\n" +
				"TestAnnotation {\n" +
				"    String testAttribute();\n" +
				"}\n"
			},
			"true");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80964
	public void test103() {
		this.runConformTest(
			new String[] {
				"TestAnnotation.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.Retention;\n" +
				"@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) public @interface\n" +
				"TestAnnotation {\n" +
				"    String testAttribute();\n" +
				"}\n"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  @TestAnnotation(testAttribute = \"test\") class A {\n" +
				"  }\n" +
				"  public static void main(String[] args) {\n" +
				"    System.out.print(A.class.isAnnotationPresent(TestAnnotation.class));\n" +
				"  }\n" +
				"}",
			},
			"true",
			null,
			false,
			null);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81825
	public void test104() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@interface ValuesAnnotation {\n" +
				"	byte[] byteArrayValue();\n" +
				"	char[] charArrayValue();\n" +
				"	boolean[] booleanArrayValue();\n" +
				"	int[] intArrayValue();\n" +
				"	short[] shortArrayValue();\n" +
				"	long[] longArrayValue();\n" +
				"	float[] floatArrayValue();\n" +
				"	double[] doubleArrayValue();\n" +
				"	String[] stringArrayValue();\n" +
				"	ValuesEnum[] enumArrayValue();\n" +
				"	ValueAttrAnnotation[] annotationArrayValue();\n" +
				"	Class[] classArrayValue();\n" +
				"	byte byteValue();\n" +
				"	char charValue();\n" +
				"	boolean booleanValue();\n" +
				"	int intValue();\n" +
				"	short shortValue();\n" +
				"	long longValue();\n" +
				"	float floatValue();\n" +
				"	double doubleValue();\n" +
				"	String stringValue();\n" +
				"	ValuesEnum enumValue();\n" +
				"	ValueAttrAnnotation annotationValue();\n" +
				"	Class classValue();\n" +
				"}\n" +
				"enum ValuesEnum {\n" +
				"	ONE, TWO;\n" +
				"}\n" +
				"\n" +
				"@interface ValueAttrAnnotation {\n" +
				"	String value() default \"\";\n" +
				"}\n" +
				"@interface ValueAttrAnnotation1 {\n" +
				"	String value();\n" +
				"}\n" +
				"@interface ValueAttrAnnotation2 {\n" +
				"	String value();\n" +
				"}\n" +
				"@ValuesAnnotation(\n" +
				"  byteValue = 1,\n" +
				"  charValue = \'A\',\n" +
				"  booleanValue = true,\n" +
				"  intValue = 1,\n" +
				"  shortValue = 1,\n" +
				"  longValue = 1L,\n" +
				"  floatValue = 1.0f,\n" +
				"  doubleValue = 1.0d,\n" +
				"  stringValue = \"A\",\n" +
				"\n" +
				"  enumValue = ValuesEnum.ONE,\n" +
				"  annotationValue = @ValueAttrAnnotation( \"annotation\"),\n" +
				"  classValue = X.class,\n" +
				"\n" +
				"  byteArrayValue = { 1, -1},\n" +
				"  charArrayValue = { \'c\', \'b\', (char)-1},\n" +
				"  booleanArrayValue = {true, false},\n" +
				"  intArrayValue = { 1, -1},\n" +
				"  shortArrayValue = { (short)1, (short)-1},\n" +
				"  longArrayValue = { 1L, -1L},\n" +
				"  floatArrayValue = { 1.0f, -1.0f},\n" +
				"  doubleArrayValue = { 1.0d, -1.0d},\n" +
				"  stringArrayValue = { \"aa\", \"bb\"},\n" +
				"\n" +
				"  enumArrayValue = {ValuesEnum.ONE, ValuesEnum.TWO},\n" +
				"  annotationArrayValue = {@ValueAttrAnnotation( \"annotation1\"),\n" +
				"@ValueAttrAnnotation( \"annotation2\")},\n" +
				"  classArrayValue = {X.class, X.class}\n" +
				")\n" +
				"@ValueAttrAnnotation1( \"classAnnotation1\")\n" +
				"@ValueAttrAnnotation2( \"classAnnotation2\")\n" +
				"public class X {\n" +
				"\n" +
				"  @ValueAttrAnnotation1( \"fieldAnnotation1\")\n" +
				"  @ValueAttrAnnotation2( \"fieldAnnotation2\")\n" +
				"  public String testfield = \"test\";\n" +
				"\n" +
				"  @ValueAttrAnnotation1( \"methodAnnotation1\")\n" +
				"  @ValueAttrAnnotation2( \"methodAnnotation2\")\n" +
				"  @ValueAttrAnnotation()\n" +
				"  public void testMethod( \n" +
				"      @ValueAttrAnnotation1( \"param1Annotation1\") \n" +
				"      @ValueAttrAnnotation2( \"param1Annotation2\") String param1, \n" +
				"      @ValueAttrAnnotation1( \"param2Annotation1\") \n" +
				"      @ValueAttrAnnotation2( \"param2Annotation2\") int param2) {\n" +
				"    // @ValueAttrAnnotation( \"codeAnnotation\")\n" +
				"  }\n" +
				"}\n"
			},
			"");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82136
	public void test105() {
		this.runConformTest(
			new String[] {
				"Property.java",
				"import java.lang.annotation.Documented;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"\n" +
				"@Documented\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"public @interface Property\n" +
				"{\n" +
				"  String property();\n" +
				"  String identifier() default \"\";\n" +
				"}",
				"Properties.java",
				"import java.lang.annotation.Documented;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"\n" +
				"@Documented\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"public @interface Properties {\n" +
				"  Property[] value();\n" +
				"}",
				"X.java",
				"@Properties({\n" +
				"  @Property(property = \"prop\", identifier = \"someIdentifier\"),\n" +
				"  @Property(property = \"type\")\n" +
				"})\n" +
				"public interface X {\n" +
				"  void setName();\n" +
				"  String getName();\n" +
				"}"
			},
			"");
			try {
				byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
				new ClassFileReader(classFileBytes, "X.java".toCharArray(), true);
			} catch (ClassFormatException e) {
				assertTrue("ClassFormatException", false);
			} catch (IOException e) {
				assertTrue("IOException", false);
			}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83939
    public void test106() {
        this.runNegativeTest(
            new String[] {
                "X.java",
                "public @interface X {\n" +
                "    int[] bar() default null;\n" +
                "}",
            },
            "----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	int[] bar() default null;\n" +
			"	                    ^^^^\n" +
			"The value for annotation attribute X.bar must be a constant expression\n" +
			"----------\n");
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=83939
    public void test107() {
        this.runNegativeTest(
            new String[] {
                "X.java",
                "@interface Ann {\n" +
                "    int[] bar();\n" +
                "}\n" +
                "@Ann(bar=null) class X {}",
            },
            "----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	@Ann(bar=null) class X {}\n" +
			"	         ^^^^\n" +
			"The value for annotation attribute Ann.bar must be a constant expression\n" +
			"----------\n");
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=83939 - variation
    public void test108() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@interface Foo {}\n" +
				"\n" +
				"@interface Bar {\n" +
				"    Foo[] foo() default null;\n" +
				"}\n" +
				"\n" +
				"@Bar(foo=null)\n" +
				"public class X { \n" +
				"}\n",
            },
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	Foo[] foo() default null;\n" +
			"	                    ^^^^\n" +
			"The value for annotation attribute Bar.foo must be some @Foo annotation \n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	@Bar(foo=null)\n" +
			"	         ^^^^\n" +
			"The value for annotation attribute Bar.foo must be some @Foo annotation \n" +
			"----------\n");
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=83939 - variation
    public void test109() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@interface Foo {}\n" +
				"\n" +
				"@interface Bar {\n" +
				"    Foo[] foo() default \"\";\n" +
				"}\n" +
				"\n" +
				"@Bar(foo=\"\")\n" +
				"public class X { \n" +
				"}\n",
            },
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	Foo[] foo() default \"\";\n" +
			"	                    ^^\n" +
			"The value for annotation attribute Bar.foo must be some @Foo annotation \n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	@Bar(foo=\"\")\n" +
			"	         ^^\n" +
			"The value for annotation attribute Bar.foo must be some @Foo annotation \n" +
			"----------\n");
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84791
    public void test110() {
        this.runConformTest(
            new String[] {
                "X.java",
				"import java.lang.annotation.Annotation;\n" +
				"import java.util.Arrays;\n" +
				"\n" +
				"@interface Ann {\n" +
				"}\n" +
				"\n" +
				"interface Iface extends Ann {\n" +
				"}\n" +
				"\n" +
				"abstract class Klass implements Ann {\n" +
				"}\n" +
				"\n" +
				"class SubKlass extends Klass {\n" +
				"	public Class<? extends Annotation> annotationType() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		Class c = SubKlass.class;\n" +
				"		System.out.print(\"Classes:\");\n" +
				"		while (c != Object.class) {\n" +
				"			System.out.print(\"-> \" + c.getName());\n" +
				"			c = c.getSuperclass();\n" +
				"		}\n" +
				"\n" +
				"		System.out.print(\", Interfaces:\");\n" +
				"		c = SubKlass.class;\n" +
				"		while (c != Object.class) {\n" +
				"			Class[] i = c.getInterfaces();\n" +
				"			System.out.print(\"-> \" + Arrays.asList(i));\n" +
				"			c = c.getSuperclass();\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
            },
			"Classes:-> SubKlass-> Klass, Interfaces:-> []-> [interface Ann]");
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=84791 - variation
    public void test111() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(
    			CompilerOptions.OPTION_ReportMissingOverrideAnnotation,
    			CompilerOptions.ERROR);
    	customOptions.put(
    			CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation,
    			CompilerOptions.DISABLED);

    	String expectedOutput =
    		"----------\n" + 
    		"1. WARNING in X.java (at line 8)\n" + 
    		"	interface Iface extends Ann {\n" + 
    		"	                        ^^^\n" + 
    		"The annotation type Ann should not be used as a superinterface for Iface\n" + 
    		"----------\n" + 
    		"2. WARNING in X.java (at line 11)\n" + 
    		"	abstract class Klass implements Ann {\n" + 
    		"	                                ^^^\n" + 
    		"The annotation type Ann should not be used as a superinterface for Klass\n" + 
    		"----------\n" + 
    		"3. ERROR in X.java (at line 14)\n" + 
    		"	class SubKlass extends Klass {\n" + 
    		"	      ^^^^^^^^\n" + 
    		"The type SubKlass must implement the inherited abstract method Ann.foo()\n" + 
    		"----------\n" + 
    		"4. WARNING in X.java (at line 20)\n" + 
    		"	class AnnImpl implements Ann {\n" + 
    		"	                         ^^^\n" + 
    		"The annotation type Ann should not be used as a superinterface for AnnImpl\n" + 
    		"----------\n" + 
    		"5. ERROR in X.java (at line 21)\n" + 
    		"	public boolean equals(Object obj) { return false; }\n" + 
    		"	               ^^^^^^^^^^^^^^^^^^\n" + 
    		"The method equals(Object) of type AnnImpl should be tagged with @Override since it actually overrides a superclass method\n" + 
    		"----------\n" + 
    		"6. ERROR in X.java (at line 22)\n" + 
    		"	public int hashCode() { return 0; }\n" + 
    		"	           ^^^^^^^^^^\n" + 
    		"The method hashCode() of type AnnImpl should be tagged with @Override since it actually overrides a superclass method\n" + 
    		"----------\n" + 
    		"7. ERROR in X.java (at line 23)\n" + 
    		"	public String toString() { return null; }\n" + 
    		"	              ^^^^^^^^^^\n" + 
    		"The method toString() of type AnnImpl should be tagged with @Override since it actually overrides a superclass method\n" + 
    		"----------\n" + 
    		"8. WARNING in X.java (at line 30)\n" + 
    		"	Class c = SubKlass.class;\n" + 
    		"	^^^^^\n" + 
    		"Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
    		"----------\n" + 
    		"9. WARNING in X.java (at line 41)\n" + 
    		"	Class[] i = c.getInterfaces();\n" + 
    		"	^^^^^\n" + 
    		"Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
    		"----------\n";

		this.runNegativeTest(
				true,
	    		new String[] {
						"X.java",
						"import java.lang.annotation.Annotation;\n" +
						"import java.util.Arrays;\n" +
						"\n" +
						"@interface Ann {\n" +
						"	int foo();\n" +
						"}\n" +
						"\n" +
						"interface Iface extends Ann {\n" +
						"}\n" +
						"\n" +
						"abstract class Klass implements Ann {\n" +
						"}\n" +
						"\n" +
						"class SubKlass extends Klass {\n" +
						"	public Class<? extends Annotation> annotationType() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}\n" +
						"\n" +
						"class AnnImpl implements Ann {\n" +
						"    public boolean equals(Object obj) { return false; }\n" +
						"    public int hashCode() { return 0; }\n" +
						"    public String toString() { return null; }\n" +
						"    public Class<? extends Annotation> annotationType() { return null; }\n" +
						"    public int foo() { return 0; }\n" +
						"}\n" +
						"\n" +
						"public class X {\n" +
						"	public static void main(String[] args) {\n" +
						"		Class c = SubKlass.class;\n" +
						"		System.out.println(\"Classes:\");\n" +
						"		while (c != Object.class) {\n" +
						"			System.out.println(\"-> \" + c.getName());\n" +
						"			c = c.getSuperclass();\n" +
						"		}\n" +
						"\n" +
						"		System.out.println();\n" +
						"		System.out.println(\"Interfaces:\");\n" +
						"		c = SubKlass.class;\n" +
						"		while (c != Object.class) {\n" +
						"			Class[] i = c.getInterfaces();\n" +
						"			System.out.println(\"-> \" + Arrays.asList(i));\n" +
						"			c = c.getSuperclass();\n" +
						"		}\n" +
						"	}\n" +
						"}\n",
		            },
		null, customOptions,
		expectedOutput,
		JavacTestOptions.SKIP);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=86291
    public void test112() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@interface Annot {\n" +
				"  String foo1() default \"\";\n" +
				"}\n" +
				"@Annot(foo1=zzz)\n" +
				"public class X {\n" +
				"  static final String zzz =  \"\";\n" +
				"}\n",
            },
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	@Annot(foo1=zzz)\n" +
			"	            ^^^\n" +
			"zzz cannot be resolved to a variable\n" +
			"----------\n");
    }
    public void test113() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@interface Annot {\n" +
				"	String foo();\n" +
				"}\n" +
				"@Annot( foo = new String(){} )\n" +
				"public class X {\n" +
				"	\n" +
				"	\n" +
				"}\n",
            },
    		"----------\n" +
    		"1. ERROR in X.java (at line 4)\n" +
    		"	@Annot( foo = new String(){} )\n" +
    		"	                  ^^^^^^\n" +
    		"An anonymous class cannot subclass the final class String\n" +
    		"----------\n");
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=86291 - variation
    public void test114() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@interface Annot {\n" +
				"	Class foo();\n" +
				"}\n" +
				"@Annot( foo = M.class )\n" +
				"public class X {\n" +
				"	class M {}\n" +
				"	\n" +
				"}\n",
            },
            "----------\n" +
    		"1. WARNING in X.java (at line 2)\n" +
    		"	Class foo();\n" +
    		"	^^^^^\n" +
    		"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
    		"----------\n" +
    		"2. ERROR in X.java (at line 4)\n" +
    		"	@Annot( foo = M.class )\n" +
    		"	              ^\n" +
    		"M cannot be resolved to a type\n" +
    		"----------\n");
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=86291 - variation
    public void test115() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@interface Annot {\n" +
				"	Class foo();\n" +
				"	String bar() default \"\";\n" +
				"}\n" +
				"@Annot(foo = M.class, bar = baz()+s)\n" +
				"public class X {\n" +
				"	class M {\n" +
				"	}\n" +
				"	final static String s = \"\";\n" +
				"	String baz() { return null; }\n" +
				"	@Annot(foo = T.class, bar = s)\n" +
				"	<T> T foo(T t, String s) {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n",
            },
            "----------\n" +
    		"1. WARNING in X.java (at line 2)\n" +
    		"	Class foo();\n" +
    		"	^^^^^\n" +
    		"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
    		"----------\n" +
    		"2. ERROR in X.java (at line 5)\n" +
    		"	@Annot(foo = M.class, bar = baz()+s)\n" +
    		"	             ^\n" +
    		"M cannot be resolved to a type\n" +
    		"----------\n" +
    		"3. ERROR in X.java (at line 5)\n" +
    		"	@Annot(foo = M.class, bar = baz()+s)\n" +
    		"	                            ^^^\n" +
    		"The method baz() is undefined for the type X\n" +
    		"----------\n" +
    		"4. ERROR in X.java (at line 5)\n" +
    		"	@Annot(foo = M.class, bar = baz()+s)\n" +
    		"	                                  ^\n" +
    		"s cannot be resolved to a variable\n" +
    		"----------\n" +
    		"5. ERROR in X.java (at line 11)\n" +
    		"	@Annot(foo = T.class, bar = s)\n" +
    		"	             ^^^^^^^\n" +
    		"Illegal class literal for the type parameter T\n" +
    		"----------\n" +
    		"6. WARNING in X.java (at line 12)\n" +
    		"	<T> T foo(T t, String s) {\n" +
    		"	                      ^\n" +
    		"The parameter s is hiding a field from type X\n" +
    		"----------\n");
    }
    // check @Deprecated support
    public void test116() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"/** @deprecated */\n" +
				"@Deprecated\n" +
				"public class X {\n" +
				"}\n",
                "Y.java",
				"public class Y {\n" +
				"	X x;\n" +
				"	Zork z;\n" +
				"}\n",
            },
			"----------\n" +
			"1. WARNING in Y.java (at line 2)\n" +
			"	X x;\n" +
			"	^\n" +
			"The type X is deprecated\n" +
			"----------\n" +
			"2. ERROR in Y.java (at line 3)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
    }
    // check @Deprecated support
    public void test117() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@Deprecated\n" +
				"public class X {\n" +
				"}\n",
                "Y.java",
				"public class Y {\n" +
				"	X x;\n" +
				"	Zork z;\n" +
				"}\n",
            },
			"----------\n" +
			"1. WARNING in Y.java (at line 2)\n" +
			"	X x;\n" +
			"	^\n" +
			"The type X is deprecated\n" +
			"----------\n" +
			"2. ERROR in Y.java (at line 3)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
    }
    // check @Deprecated support
    public void test118() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@interface Deprecated {}\n" +
				"\n" +
				"@Deprecated // not the real @Deprecated interface\n" +
				"public class X {\n" +
				"}\n",
                "Y.java",
				"public class Y {\n" +
				"	X x;\n" +
				"	Zork z;\n" +
				"}\n",
            },
			"----------\n" +
			"1. ERROR in Y.java (at line 3)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
    }
    // check @Deprecated support
    public void test119() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@Deprecated\n" +
				"public class X {\n" +
				"	void foo(){}\n" +
				"}\n",
                "Y.java",
				"public class Y extends X {\n" +
				"	void foo(){ super.foo(); }\n" +
				"	Zork z;\n" +
				"}\n",
            },
			"----------\n" +
			"1. WARNING in Y.java (at line 1)\n" +
			"	public class Y extends X {\n" +
			"	                       ^\n" +
			"The type X is deprecated\n" +
			"----------\n" +
			"2. WARNING in Y.java (at line 2)\n" +
			"	void foo(){ super.foo(); }\n" +
			"	     ^^^^^\n" +
			"The method foo() of type Y should be tagged with @Override since it actually overrides a superclass method\n" +
			"----------\n" +
			"3. WARNING in Y.java (at line 2)\n" +
			"	void foo(){ super.foo(); }\n" +
			"	                  ^^^^^\n" +
			"The method foo() from the type X is deprecated\n" +
			"----------\n" +
			"4. ERROR in Y.java (at line 3)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
    }
    // check @Deprecated support
    public void test120() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@Deprecated\n" +
				"public class X {\n" +
				"	void foo(){}\n" +
				"}\n",
                "Y.java",
				"public class Y extends X {\n" +
				"	void foo(){ super.foo(); }\n" +
				"	Zork z;\n" +
				"}\n",
            },
			"----------\n" +
			"1. WARNING in Y.java (at line 1)\n" +
			"	public class Y extends X {\n" +
			"	                       ^\n" +
			"The type X is deprecated\n" +
			"----------\n" +
			"2. WARNING in Y.java (at line 2)\n" +
			"	void foo(){ super.foo(); }\n" +
			"	     ^^^^^\n" +
			"The method foo() of type Y should be tagged with @Override since it actually overrides a superclass method\n" +
			"----------\n" +
			"3. WARNING in Y.java (at line 2)\n" +
			"	void foo(){ super.foo(); }\n" +
			"	                  ^^^^^\n" +
			"The method foo() from the type X is deprecated\n" +
			"----------\n" +
			"4. ERROR in Y.java (at line 3)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
    }
    // check missing @Deprecated detection
    public void test121() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"/** @deprecated */\n" +
				"public class X {\n" +
				"	/** @deprecated */\n" +
				"	public static class Y {\n" +
				"	}\n" +
				"	/** @deprecated */\n" +
				"	int i;\n" +
				"	/** @deprecated */\n" +
				"	public void flag() {}\n" +
				"	void doNotFlag() {}\n" +
				"  Zork z;\n" +
				"} \n",
            },
			"----------\n" +
			"1. WARNING in X.java (at line 2)\n" +
			"	public class X {\n" +
			"	             ^\n" +
			"The deprecated type X should be annotated with @Deprecated\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 4)\n" +
			"	public static class Y {\n" +
			"	                    ^\n" +
			"The deprecated type X.Y should be annotated with @Deprecated\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 7)\n" +
			"	int i;\n" +
			"	    ^\n" +
			"The deprecated field X.i should be annotated with @Deprecated\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 9)\n" +
			"	public void flag() {}\n" +
			"	            ^^^^^^\n" +
			"The deprecated method flag() of type X should be annotated with @Deprecated\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 11)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=88446
    public void test122() {
        this.runConformTest(
            new String[] {
                "X.java",
                "import java.lang.annotation.Annotation;\n" +
                "import java.lang.reflect.Method;\n" +
                "import java.lang.annotation.ElementType;\n" +
                "import java.lang.annotation.Retention;\n" +
                "import java.lang.annotation.RetentionPolicy;\n" +
                "import java.lang.annotation.Target;\n" +
                "class GenericWithInnerAnnotation<T> {\n" +
                "    @Retention(RetentionPolicy.RUNTIME)\n" +
                "    @Target(ElementType.METHOD)\n" +
                "    public @interface MyAnnotation {\n" +
                "    }\n" +
                "}\n" +
                "public class X extends GenericWithInnerAnnotation<Integer> {\n" +
                "    @MyAnnotation\n" +
                "    public void aMethod() {\n" +
                "    }\n" +
                "    \n" +
                "    public static void main(String[] args) {\n" +
                "       try {\n" +
                "           Method method = X.class.getDeclaredMethod(\"aMethod\", new Class[]{});\n" +
                "           System.out.print(method.getName());\n" +
                "           Annotation[] annotations = method.getAnnotations();\n" +
                "           System.out.println(annotations.length);\n" +
                "       } catch(NoSuchMethodException e) {\n" +
                "       }\n" +
                "    }\n" +
                "}",
            },
            "aMethod1");
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=90110
    public void test123() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"class SuperX {\n" +
				"\n" +
				"    static void notOverridden() {\n" +
				"        return;\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"public class X extends SuperX {\n" +
				"\n" +
				"    static void notOverridden() {\n" +
				"        return;\n" +
				"    }\n" +
				"  Zork z;\n" +
				"} \n",
            },
			"----------\n" +
			"1. ERROR in X.java (at line 13)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=90110 - variation
    public void test124() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"class SuperX {\n" +
				"\n" +
				"    void notOverridden() {\n" +
				"        return;\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"public class X extends SuperX {\n" +
				"\n" +
				"    void notOverridden() {\n" +
				"        return;\n" +
				"    }\n" +
				"  Zork z;\n" +
				"} \n",
            },
			"----------\n" +
			"1. WARNING in X.java (at line 10)\n" +
			"	void notOverridden() {\n" +
			"	     ^^^^^^^^^^^^^^^\n" +
			"The method notOverridden() of type X should be tagged with @Override since it actually overrides a superclass method\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 13)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
    }
    public void test125() {
        this.runConformTest(
            new String[] {
                "X.java",
				"import java.lang.annotation.*;\n" +
				"\n" +
				"public class X implements Ann {\n" +
				"	\n" +
				"	Ann ann = new X();\n" +
				"	public Class<? extends Annotation>  annotationType() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n" +
				"\n" +
				"@interface Ann {}\n" +
				"\n",
            },
			"");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=90484 - check no missing @Override warning
    public void test126() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"public interface X {\n" +
				"   Zork z;\n" +
				"   X clone();\n" +
				"}\n",
            },
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
    }
    // check @SuppressWarning support
    public void test127() {
        this.runNegativeTest(
            new String[] {
                "X.java",
                "@Deprecated\n" +
                "public class X {\n" +
                "   void foo(){}\n" +
                "}\n",
                "Y.java",
                "public class Y extends X {\n" +
                "  @SuppressWarnings(\"all\")\n" +
                "   void foo(){ super.foo(); }\n" +
                "   Zork z;\n" +
                "}\n",
            },
			"----------\n" +
			"1. WARNING in Y.java (at line 1)\n" +
			"	public class Y extends X {\n" +
			"	                       ^\n" +
			"The type X is deprecated\n" +
			"----------\n" +
			"2. ERROR in Y.java (at line 4)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
    }
    // check @SuppressWarning support
    public void test128() {
        this.runNegativeTest(
            new String[] {
                "X.java",
                "import java.util.List;\n" +
                "\n" +
                "public class X {\n" +
                "    void foo(List list) {\n" +
                "        List<String> ls1 = list;\n" +
                "    }\n" +
                "    @SuppressWarnings({\"unchecked\", \"rawtypes\"})\n" +
                "    void bar(List list) {\n" +
                "        List<String> ls2 = list;\n" +
                "    }\n" +
                "   Zork z;\n" +
                "}\n",
            },
            "----------\n" +
    		"1. WARNING in X.java (at line 4)\n" +
    		"	void foo(List list) {\n" +
    		"	         ^^^^\n" +
    		"List is a raw type. References to generic type List<E> should be parameterized\n" +
    		"----------\n" +
    		"2. WARNING in X.java (at line 5)\n" +
    		"	List<String> ls1 = list;\n" +
    		"	                   ^^^^\n" +
    		"Type safety: The expression of type List needs unchecked conversion to conform to List<String>\n" +
    		"----------\n" +
    		"3. ERROR in X.java (at line 11)\n" +
    		"	Zork z;\n" +
    		"	^^^^\n" +
    		"Zork cannot be resolved to a type\n" +
    		"----------\n");
    }
    // check @SuppressWarning support
    public void test129() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"import java.util.*;\n" +
				"@SuppressWarnings(\"unchecked\")\n" +
				"public class X {\n" +
				"	void foo() {\n" +
				"		Map<String, String>[] map = new HashMap[10];\n" +
				"	}\n" +
                "   Zork z;\n" +
				"}\n",
            },
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
    }
    // check @SuppressWarning support
    public void test130() {
    	Map customOptions = new Hashtable();
		String[] warnings = CompilerOptions.warningOptionNames();
		for (int i = 0, ceil = warnings.length; i < ceil; i++) {
			customOptions.put(warnings[i], CompilerOptions.WARNING);
		}
        this.runConformTest(
        	true,
            new String[] {
                "X.java",
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"  }\n" +
				"}\n",
            },
            null,
            customOptions,
    		"----------\n" +
    		"1. WARNING in X.java (at line 1)\n" +
    		"	public class X {\n" +
    		"	             ^\n" +
    		"Javadoc: Missing comment for public declaration\n" +
    		"----------\n" +
    		"2. WARNING in X.java (at line 2)\n" +
    		"	public static void main(String[] args) {\n" +
    		"	                   ^^^^^^^^^^^^^^^^^^^\n" +
    		"Javadoc: Missing comment for public declaration\n" +
    		"----------\n" +
    		"3. WARNING in X.java (at line 2)\n" +
    		"	public static void main(String[] args) {\n" +
    		"  }\n" +
    		"	                                       ^^^^^\n" +
    		"Empty block should be documented\n" +
    		"----------\n",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
    }
    // check @SuppressWarning support
    public void test131() {
        this.runNegativeTest(
            new String[] {
                "X.java",
                "@SuppressWarnings(\"all\")\n" +
                "public class X {\n" +
		        "  public static void main(String[] args) {\n" +
		        "    Zork z;\n" +
		        "  }\n" +
		        "}\n",
            },
            "----------\n" +
            "1. ERROR in X.java (at line 4)\n" +
            "	Zork z;\n" +
            "	^^^^\n" +
            "Zork cannot be resolved to a type\n" +
            "----------\n");
    }
    // check @SuppressWarning support
    public void test132() {
        this.runNegativeTest(
            new String[] {
                "X.java",
    			"import java.io.Serializable;\n" +
    			"import java.util.List;\n" +
    			"import java.util.Vector;\n" +
    			"\n" +
    			"public class X {\n" +
    			"	public static void main(String[] args) {\n" +
    			"		W.deprecated();\n" +
    			"		List<X> l = new Vector();\n" +
    			"		l.size();\n" +
    			"		try {\n" +
    			"			// do nothing\n" +
    			"		} finally {\n" +
    			"			throw new Error();\n" +
    			"		}\n" +
    			"		// Zork z;\n" +
    			"	}\n" +
    			"\n" +
    			"	class S implements Serializable {\n" +
    			"		String dummy;\n" +
    			"	}\n" +
    			"}",
    			"W.java",
    			"public class W {\n" +
    			"	// @deprecated\n" +
    			"	@Deprecated\n" +
    			"	static void deprecated() {\n" +
    			"		// do nothing\n" +
    			"	}\n" +
    			"}\n"
            },
            "----------\n" +
    		"1. WARNING in X.java (at line 7)\n" +
    		"	W.deprecated();\n" +
    		"	  ^^^^^^^^^^^^\n" +
    		"The method deprecated() from the type W is deprecated\n" +
    		"----------\n" +
    		"2. WARNING in X.java (at line 8)\n" +
    		"	List<X> l = new Vector();\n" +
    		"	            ^^^^^^^^^^^^\n" +
    		"Type safety: The expression of type Vector needs unchecked conversion to conform to List<X>\n" +
    		"----------\n" +
    		"3. WARNING in X.java (at line 8)\n" +
    		"	List<X> l = new Vector();\n" +
    		"	                ^^^^^^\n" +
    		"Vector is a raw type. References to generic type Vector<E> should be parameterized\n" +
    		"----------\n" +
    		"4. WARNING in X.java (at line 12)\n" +
    		"	} finally {\n" +
    		"			throw new Error();\n" +
    		"		}\n" +
    		"	          ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
    		"finally block does not complete normally\n" +
    		"----------\n" +
    		"5. WARNING in X.java (at line 18)\n" +
    		"	class S implements Serializable {\n" +
    		"	      ^\n" +
    		"The serializable class S does not declare a static final serialVersionUID field of type long\n" +
    		"----------\n",
    		null,
    		true,
    		null,
    		"java.lang.Error");
    }
    // check @SuppressWarning support
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=89436
    public void test133() {
        this.runNegativeTest(
            new String[] {
                "X.java",
    			"import java.io.Serializable;\n" +
    			"import java.util.List;\n" +
    			"import java.util.Vector;\n" +
    			"\n" +
    			"@SuppressWarnings( { \"deprecation\",//$NON-NLS-1$\n" +
    			"		\"finally\",//$NON-NLS-1$\n" +
    			"		\"rawtypes\",//$NON-NLS-1$\n" +
    			"		\"serial\",//$NON-NLS-1$\n" +
    			"		\"unchecked\"//$NON-NLS-1$\n" +
    			"})\n" +
    			"public class X {\n" +
    			"	public static void main(String[] args) {\n" +
    			"		W.deprecated();\n" +
    			"		List<X> l = new Vector();\n" +
    			"		l.size();\n" +
    			"		try {\n" +
    			"			// do nothing\n" +
    			"		} finally {\n" +
    			"			throw new Error();\n" +
    			"		}\n" +
    			"	}\n" +
    			"\n" +
    			"	class S implements Serializable {\n" +
    			"		Zork dummy;\n" +
    			"	}\n" +
    			"}",
    			"W.java",
    			"public class W {\n" +
    			"	// @deprecated\n" +
    			"	@Deprecated\n" +
    			"	static void deprecated() {\n" +
    			"		// do nothing\n" +
    			"	}\n" +
    			"}\n"
            },
    		"----------\n" +
    		"1. ERROR in X.java (at line 24)\n" +
    		"	Zork dummy;\n" +
    		"	^^^^\n" +
    		"Zork cannot be resolved to a type\n" +
    		"----------\n");
    }
    // check @SuppressWarning support
    public void test134() {
        this.runNegativeTest(
            new String[] {
                "X.java",
    			"import java.io.Serializable;\n" +
    			"import java.util.List;\n" +
    			"import java.util.Vector;\n" +
    			"\n" +
    			"public class X {\n" +
    			"	@SuppressWarnings( { \"deprecation\",//$NON-NLS-1$\n" +
    			"			\"finally\",//$NON-NLS-1$\n" +
    			"			\"rawtypes\",//$NON-NLS-1$\n" +
    			"			\"unchecked\"//$NON-NLS-1$\n" +
    			"	})\n" +
    			"	public static void main(String[] args) {\n" +
    			"		W.deprecated();\n" +
    			"		List<X> l = new Vector();\n" +
    			"		l.size();\n" +
    			"		try {\n" +
    			"			// do nothing\n" +
    			"		} finally {\n" +
    			"			throw new Error();\n" +
    			"		}\n" +
    			"	}\n" +
    			"\n" +
    			"	@SuppressWarnings({\"unchecked\", \"rawtypes\"}//$NON-NLS-1$//$NON-NLS-2$\n" +
    			"	)\n" +
    			"	List<X> l = new Vector();\n" +
    			"\n" +
    			"	@SuppressWarnings(\"serial\"//$NON-NLS-1$\n" +
    			"	)\n" +
    			"	class S implements Serializable {\n" +
    			"		Zork dummy;\n" +
    			"	}\n" +
    			"}",
    			"W.java",
    			"public class W {\n" +
    			"	// @deprecated\n" +
    			"	@Deprecated\n" +
    			"	static void deprecated() {\n" +
    			"		// do nothing\n" +
    			"	}\n" +
    			"}\n"
            },
    		"----------\n" +
    		"1. ERROR in X.java (at line 29)\n" +
    		"	Zork dummy;\n" +
    		"	^^^^\n" +
    		"Zork cannot be resolved to a type\n" +
    		"----------\n");
    }
    // check @SuppressWarning support
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=69505 -- NOT READY YET: "all" only so far, no file support --
    //                                                        hence no import support
    public void test135() {
        this.runNegativeTest(
            new String[] {
                "X.java",
    			"@SuppressWarnings(\"all\")//$NON-NLS-1$\n" +
    			"import java.util.List;\n" +
    			"\n" +
    			"public class X {\n" +
    			"	public static void main(String[] args) {\n" +
    			"		if (false) {\n" +
    			"			;\n" +
    			"		} else {\n" +
    			"		}\n" +
    			"		Zork z;\n" +
    			"	}\n" +
    			"}"
            },
    		"----------\n" + 
    		"1. ERROR in X.java (at line 2)\n" + 
    		"	import java.util.List;\n" + 
    		"	^^^^^^\n" + 
    		"Syntax error on token \"import\", package expected\n" + 
    		"----------\n" + 
    		"2. ERROR in X.java (at line 10)\n" + 
    		"	Zork z;\n" + 
    		"	^^^^\n" + 
    		"Zork cannot be resolved to a type\n" + 
    		"----------\n");
    }
    // check @SuppressWarning support
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=71968
    public void test136() {
        this.runNegativeTest(
            new String[] {
                "X.java",
    			"public class X {\n" +
    			"	@SuppressWarnings(\"unused\"//$NON-NLS-1$\n" +
    			"	)\n" +
    			"	private static final String marker = \"never used mark\"; //$NON-NLS-1$\n" +
    			"\n" +
    			"	public static void main(String[] args) {\n" +
    			"		Zork z;\n" +
    			"	}\n" +
    			"}"
            },
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n");
    }
    // check @SuppressWarning support
    public void test137() {
    	Map customOptions = new Hashtable();
		String[] warnings = CompilerOptions.warningOptionNames();
		for (int i = 0, ceil = warnings.length; i < ceil; i++) {
			customOptions.put(warnings[i], CompilerOptions.WARNING);
		}
		customOptions.put(CompilerOptions.OPTION_SuppressWarnings, CompilerOptions.DISABLED);
        this.runNegativeTest(
            new String[] {
                "X.java",
    			"import java.io.Serializable;\n" +
    			"import java.util.List;\n" +
    			"import java.util.Vector;\n" +
    			"\n" +
    			"@SuppressWarnings(\"all\")\n" +
    			"public class X {\n" +
    			"	public static void main(String[] args) {\n" +
    			"		W.deprecated();\n" +
    			"		List<X> l = new Vector();\n" +
    			"		l.size();\n" +
    			"		try {\n" +
    			"			// do nothing\n" +
    			"		} finally {\n" +
    			"			throw new Error();\n" +
    			"		}\n" +
    			"		Zork z;\n" +
    			"	}\n" +
    			"\n" +
    			"	class S implements Serializable {\n" +
    			"		String dummy;\n" +
    			"	}\n" +
    			"}",
    			"W.java",
    			"public class W {\n" +
    			"	// @deprecated\n" +
    			"	@Deprecated\n" +
    			"	static void deprecated() {\n" +
    			"		// do nothing\n" +
    			"	}\n" +
    			"}\n"
            },
            "----------\n" +
    		"1. WARNING in X.java (at line 6)\n" +
    		"	public class X {\n" +
    		"	             ^\n" +
    		"Javadoc: Missing comment for public declaration\n" +
    		"----------\n" +
    		"2. WARNING in X.java (at line 7)\n" +
    		"	public static void main(String[] args) {\n" +
    		"	                   ^^^^^^^^^^^^^^^^^^^\n" +
    		"Javadoc: Missing comment for public declaration\n" +
    		"----------\n" +
    		"3. WARNING in X.java (at line 8)\n" +
    		"	W.deprecated();\n" +
    		"	  ^^^^^^^^^^^^\n" +
    		"The method deprecated() from the type W is deprecated\n" +
    		"----------\n" +
    		"4. WARNING in X.java (at line 9)\n" +
    		"	List<X> l = new Vector();\n" +
    		"	            ^^^^^^^^^^^^\n" +
    		"Type safety: The expression of type Vector needs unchecked conversion to conform to List<X>\n" +
    		"----------\n" +
    		"5. WARNING in X.java (at line 9)\n" +
    		"	List<X> l = new Vector();\n" +
    		"	                ^^^^^^\n" +
    		"Vector is a raw type. References to generic type Vector<E> should be parameterized\n" +
    		"----------\n" +
    		"6. ERROR in X.java (at line 16)\n" +
    		"	Zork z;\n" +
    		"	^^^^\n" +
    		"Zork cannot be resolved to a type\n" +
    		"----------\n" +
    		"7. WARNING in X.java (at line 19)\n" +
    		"	class S implements Serializable {\n" +
    		"	      ^\n" +
    		"The serializable class S does not declare a static final serialVersionUID field of type long\n" +
    		"----------\n" +
    		"----------\n" +
    		"1. WARNING in W.java (at line 1)\n" +
    		"	public class W {\n" +
    		"	             ^\n" +
    		"Javadoc: Missing comment for public declaration\n" +
    		"----------\n",
			null, true, customOptions);
    }
    // check @SuppressWarning support
    public void test138() {
    	Map customOptions = new Hashtable();
    	customOptions.put(CompilerOptions.OPTION_ReportUnhandledWarningToken, CompilerOptions.WARNING);
        this.runNegativeTest(

            new String[] {
                "X.java",
    			"@SuppressWarnings(\"zork\")//$NON-NLS-1$\n" +
    			"public class X {\n" +
    			"	Zork z;\n" +
    			"}\n"
            },
    		"----------\n" +
    		"1. WARNING in X.java (at line 1)\n" +
    		"	@SuppressWarnings(\"zork\")//$NON-NLS-1$\n" +
    		"	                  ^^^^^^\n" +
    		"Unsupported @SuppressWarnings(\"zork\")\n" +
    		"----------\n" +
    		"2. ERROR in X.java (at line 3)\n" +
    		"	Zork z;\n" +
    		"	^^^^\n" +
    		"Zork cannot be resolved to a type\n" +
    		"----------\n",
			null, true, customOptions);
    }
    // check @SuppressWarning support
    public void test139() {
    	Map customOptions = new Hashtable();
    	customOptions.put(CompilerOptions.OPTION_ReportUnhandledWarningToken, CompilerOptions.WARNING);
        this.runNegativeTest(

            new String[] {
                "X.java",
    			"@SuppressWarnings({\"zork\", \"warningToken\"})//$NON-NLS-1$//$NON-NLS-2$\n" +
    			"public class X {\n" +
    			"	Zork z;\n" +
    			"}\n"
            },
    		"----------\n" +
    		"1. WARNING in X.java (at line 1)\n" +
    		"	@SuppressWarnings({\"zork\", \"warningToken\"})//$NON-NLS-1$//$NON-NLS-2$\n" +
    		"	                   ^^^^^^\n" +
    		"Unsupported @SuppressWarnings(\"zork\")\n" +
    		"----------\n" +
    		"2. WARNING in X.java (at line 1)\n" +
    		"	@SuppressWarnings({\"zork\", \"warningToken\"})//$NON-NLS-1$//$NON-NLS-2$\n" +
    		"	                           ^^^^^^^^^^^^^^\n" +
    		"Unsupported @SuppressWarnings(\"warningToken\")\n" +
    		"----------\n" +
    		"3. ERROR in X.java (at line 3)\n" +
    		"	Zork z;\n" +
    		"	^^^^\n" +
    		"Zork cannot be resolved to a type\n" +
    		"----------\n",
			null, true, customOptions);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=90111 - variation
    public void test140() {
    	String expectedOutput = new CompilerOptions(getCompilerOptions()).sourceLevel < ClassFileConstants.JDK1_6
		?	"----------\n" +
    		"1. ERROR in X.java (at line 6)\n" +
    		"	static void foo(){}	\n" +
    		"	            ^^^^^\n" +
    		"The method foo() of type Bar must override a superclass method\n" +
    		"----------\n"
		:	"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	static void foo(){}	\n" +
			"	            ^^^^^\n" +
			"The method foo() of type Bar must override or implement a supertype method\n" +
			"----------\n";
        this.runNegativeTest(
            new String[] {
                "X.java",
				"public class X {\n" +
				"  static void foo(){}\n" +
				"}\n" +
				"class Bar extends X {\n" +
				"  @Override\n" +
				"  static void foo(){}	\n" +
				"}\n" +
				"\n"
            },
            expectedOutput,
            JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=94867
    public void test141() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@interface X1 {\n" +
				"	Class<? extends Throwable>[] expected1() default {};\n" +
				"	Class<? super Throwable>[] expected2() default {};\n" +
				"	Class<?>[] expected3() default {};\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"	@X1(expected1=Throwable.class, expected2={})\n" +
				"	public static void main(String[] args) {\n" +
				"		\n" +
				"	}\n" +
				"	void foo() {\n" +
				"		Class<? extends Throwable>[] c1 = {};\n" +
				"		Class<? super Throwable>[] c2 = {};\n" +
				"		Class<?>[] c3 = {};\n" +
				"	}\n" +
				"}\n"
            },
			"----------\n" +
			"1. ERROR in X.java (at line 13)\n" +
			"	Class<? extends Throwable>[] c1 = {};\n" +
			"	                                  ^^\n" +
			"Cannot create a generic array of Class<? extends Throwable>\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 14)\n" +
			"	Class<? super Throwable>[] c2 = {};\n" +
			"	                                ^^\n" +
			"Cannot create a generic array of Class<? super Throwable>\n" +
			"----------\n");
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=94308
    public void test142() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@SuppressWarnings(\"deprecation\")\n" +
				"public class X extends p.OldStuff {\n" +
				"	/**\n" +
				"	 * @see p.OldStuff#foo()\n" +
				"	 */\n" +
				"	@Override\n" +
				"	public void foo() {\n" +
				"		super.foo();\n" +
				"	}\n" +
				"}\n",
                "p/OldStuff.java",
                "package p;\n" +
                "@Deprecated\n" +
				"public class OldStuff {\n" +
				"	public void foo() {\n" +
				"	}	\n" +
				"  Zork z;\n" +
				"}\n",
            },
			"----------\n" +
			"1. ERROR in p\\OldStuff.java (at line 6)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n",
			null,
			true,
			null);
    }
    public void test142b() {
		Map raiseInvalidJavadocSeverity =
			new HashMap(2);
		raiseInvalidJavadocSeverity.put(
				CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
		// admittingly, when these are errors, SuppressWarnings is not enough to
		// filter them out *but* the deprecation level being WARNING, we get them
		// out anyway
	    this.runNegativeTest(
	        new String[] {
	            "X.java",
				"@SuppressWarnings(\"deprecation\")\n" +
				"public class X extends p.OldStuff {\n" +
				"	/**\n" +
				"	 * @see p.OldStuff#foo()\n" +
				"	 */\n" +
				"	@Override\n" +
				"	public void foo() {\n" +
				"		super.foo();\n" +
				"	}\n" +
				"}\n",
	            "p/OldStuff.java",
	            "package p;\n" +
	            "@Deprecated\n" +
				"public class OldStuff {\n" +
				"	public void foo() {\n" +
				"	}	\n" +
				"  Zork z;\n" +
				"}\n",
	        },
			"----------\n" +
			"1. ERROR in p\\OldStuff.java (at line 6)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n",
			null,
			true,
			raiseInvalidJavadocSeverity);
	}
// check that @SuppressWarning is reported as unused when corresponding warning is moved from
// warning to error
public void test142c() {
	Map raiseDeprecationReduceInvalidJavadocSeverity =
		new HashMap(2);
	raiseDeprecationReduceInvalidJavadocSeverity.put(
			CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	raiseDeprecationReduceInvalidJavadocSeverity.put(
			CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.WARNING);
    this.runNegativeTest(
    	true,
        new String[] {
            "X.java",
			"@SuppressWarnings(\"deprecation\")\n" +
			"public class X extends p.OldStuff {\n" +
			"	/**\n" +
			"	 * @see p.OldStuff#foo()\n" +
			"	 */\n" +
			"	@Override\n" +
			"	public void foo() {\n" +
			"		super.foo();\n" +
			"	}\n" +
			"}\n",
            "p/OldStuff.java",
            "package p;\n" +
            "@Deprecated\n" +
			"public class OldStuff {\n" +
			"	public void foo() {\n" +
			"	}	\n" +
			"}\n",
        },
        null,
        raiseDeprecationReduceInvalidJavadocSeverity,
        "----------\n" + 
		"1. WARNING in X.java (at line 1)\n" + 
		"	@SuppressWarnings(\"deprecation\")\n" + 
		"	                  ^^^^^^^^^^^^^\n" + 
		"Unnecessary @SuppressWarnings(\"deprecation\")\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	public class X extends p.OldStuff {\n" + 
		"	                         ^^^^^^^^\n" + 
		"The type OldStuff is deprecated\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 8)\n" + 
		"	super.foo();\n" + 
		"	      ^^^^^\n" + 
		"The method foo() from the type OldStuff is deprecated\n" + 
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
public void test143() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"public class X extends p.OldStuff {\n" +
			"	@SuppressWarnings(\"all\")\n" +
			"	public void foo() {\n" +
			"		super.foo();\n" +
			"	}\n" +
			"}\n",
            "p/OldStuff.java",
            "package p;\n" +
            "@Deprecated\n" +
			"public class OldStuff {\n" +
			"	public void foo() {\n" +
			"	}	\n" +
			"  Zork z;\n" +
			"}\n",
        },
		"----------\n" +
		"1. WARNING in X.java (at line 1)\n" +
		"	public class X extends p.OldStuff {\n" +
		"	                         ^^^^^^^^\n" +
		"The type OldStuff is deprecated\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in p\\OldStuff.java (at line 6)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}
    public void test144() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"import java.util.*;\n" +
				"public class X {\n" +
				"	Zork z;\n" +
				"	@SuppressWarnings(\"all\")  \n" +
				"	public static class EverythingWrong {\n" +
				"		private EverythingWrong() {}\n" +
				"		@BeforeClass public void notStaticBC() {}\n" +
				"		@BeforeClass static void notPublicBC() {}\n" +
				"		@BeforeClass public static int nonVoidBC() { return 0; }\n" +
				"		@BeforeClass public static void argumentsBC(int i) {}\n" +
				"		@BeforeClass public static void fineBC() {}\n" +
				"		@AfterClass public void notStaticAC() {}\n" +
				"		@AfterClass static void notPublicAC() {}\n" +
				"		@AfterClass public static int nonVoidAC() { return 0; }\n" +
				"		@AfterClass public static void argumentsAC(int i) {}\n" +
				"		@AfterClass public static void fineAC() {}\n" +
				"		@After public static void staticA() {}\n" +
				"		@After void notPublicA() {}\n" +
				"		@After public int nonVoidA() { return 0; }\n" +
				"		@After public void argumentsA(int i) {}\n" +
				"		@After public void fineA() {}\n" +
				"		@Before public static void staticB() {}\n" +
				"		@Before void notPublicB() {}\n" +
				"		@Before public int nonVoidB() { return 0; }\n" +
				"		@Before public void argumentsB(int i) {}\n" +
				"		@Before public void fineB() {}\n" +
				"		@Test public static void staticT() {}\n" +
				"		@Test void notPublicT() {}\n" +
				"		@Test public int nonVoidT() { return 0; }\n" +
				"		@Test public void argumentsT(int i) {}\n" +
				"		@Test public void fineT() {}\n" +
				"	}\n" +
				"	@Test public void testFailures() throws Exception {\n" +
				"		List<Exception> problems= new TestIntrospector(EverythingWrong.class).validateTestMethods();\n" +
				"		int errorCount= 1 + 4 * 5; // missing constructor plus four invalid methods for each annotation */\n" +
				"		assertEquals(errorCount, problems.size());\n" +
				"	}\n" +
				"	public static junit.framework.Test suite() {\n" +
				"		return null; // new JUnit4TestAdapter(TestMethodTest.class);\n" +
				"	}\n" +
				"	void assertEquals(int i, int j) {\n" +
				"	}\n" +
				"}\n" +
				"@interface BeforeClass {}\n" +
				"@interface AfterClass {}\n" +
				"@interface Test {}\n" +
				"@interface After {}\n" +
				"@interface Before {}\n" +
				"class TestIntrospector {\n" +
				"	TestIntrospector(Class c) {}\n" +
				"	List validateTestMethods() { return null; }\n" +
				"}\n",
            },
            "----------\n" +
    		"1. ERROR in X.java (at line 3)\n" +
    		"	Zork z;\n" +
    		"	^^^^\n" +
    		"Zork cannot be resolved to a type\n" +
    		"----------\n" +
    		"2. WARNING in X.java (at line 34)\n" +
    		"	List<Exception> problems= new TestIntrospector(EverythingWrong.class).validateTestMethods();\n" +
    		"	                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
    		"Type safety: The expression of type List needs unchecked conversion to conform to List<Exception>\n" +
    		"----------\n" +
    		"3. ERROR in X.java (at line 38)\n" +
    		"	public static junit.framework.Test suite() {\n" +
    		"	              ^^^^^\n" +
    		"junit cannot be resolved to a type\n" +
    		"----------\n" +
    		"4. WARNING in X.java (at line 50)\n" +
    		"	TestIntrospector(Class c) {}\n" +
    		"	                 ^^^^^\n" +
    		"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
    		"----------\n" +
    		"5. WARNING in X.java (at line 51)\n" +
    		"	List validateTestMethods() { return null; }\n" +
    		"	^^^^\n" +
    		"List is a raw type. References to generic type List<E> should be parameterized\n" +
    		"----------\n");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=89937
    public void test145() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@interface Annot {\n" +
				"  int foo();\n" +
				"  int bar();\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"  static final int yyy = 0;\n" +
				"  @Annot(foo=zzz, bar = yyy)\n" +
				"  static final int zzz = 0;\n" +
				"}\n" +
				"\n",
            },
			"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	@Annot(foo=zzz, bar = yyy)\n" +
			"	           ^^^\n" +
			"Cannot reference a field before it is defined\n" +
			"----------\n");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=96631
    public void test146() {
        this.runConformTest(
            new String[] {
                "X.java",
				"@SuppressWarnings(value={})\n" +
				"public class X {\n" +
				"}\n",
            },
			"");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=96974
    public void test147() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@SuppressWarnings({\"nls\"})\n" +
				"public class X<T> {\n" +
				"	 String test= \"\";\n" +
				"}",
            },
			"",
			null,
			true,
			options
		);
    }

    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=97466
    public void test148() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"public class X {\n" +
				"	private static void foo() {\n" +
				"		 @interface Bar {\n" +
				"			public String bar = \"BUG\";\n" +
				"		}\n" +
				"	}\n" +
				"}",
            },
            "----------\n" +
    		"1. ERROR in X.java (at line 3)\n" +
    		"	@interface Bar {\n" +
    		"	           ^^^\n" +
    		"The member annotation Bar can only be defined inside a top-level class or interface\n" +
    		"----------\n");
    }

    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=96991
    public void test149() {
        this.runNegativeTest(
            new String[] {
                "X.java",
				"public class X {\n" +
				"	void bar() {\n" +
				"		@Annot(foo = zzz)\n" +
				"		final int zzz = 0;\n" +
				"\n" +
				"		@Annot(foo = kkk)\n" +
				"		int kkk = 1;\n" +
				"\n" +
				"	}\n" +
				"	@Annot(foo = fff)\n" +
				"	final int fff = 0;\n" +
				"	\n" +
				"	@Annot(foo = Member.ttt)\n" +
				"	static class Member {\n" +
				"		final static int ttt = 2;\n" +
				"	}\n" +
				"}\n" +
				"@interface Annot {\n" +
				"	int foo();\n" +
				"}\n",
            },
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	@Annot(foo = kkk)\n" +
			"	             ^^^\n" +
			"The value for annotation attribute Annot.foo must be a constant expression\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 10)\n" +
			"	@Annot(foo = fff)\n" +
			"	             ^^^\n" +
			"Cannot reference a field before it is defined\n" +
			"----------\n");
    }

    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=98091
    public void test150() {
        this.runConformTest(
        	true,
            new String[] {
                "X.java",
				"@SuppressWarnings(\"assertIdentifier\")\n" +
				"class X {}",
            },
    		"----------\n" +
    		"1. WARNING in X.java (at line 1)\n" +
    		"	@SuppressWarnings(\"assertIdentifier\")\n" +
    		"	                  ^^^^^^^^^^^^^^^^^^\n" +
    		"Unsupported @SuppressWarnings(\"assertIdentifier\")\n" +
    		"----------\n",
    		null, null,
    		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test151() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportAutoboxing, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@SuppressWarnings({\"boxing\"})\n" +
				"public class X {\n" +
				"	 static void foo(int i) {}\n" +
				"	 public static void main(String[] args) {\n" +
				"		foo(new Integer(0));\n" +
				"	 }\n" +
				"}",
            },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test152() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportAutoboxing, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@SuppressWarnings({\"boxing\"})\n" +
				"public class X {\n" +
				"	 static void foo(Integer i) {}\n" +
				"	 public static void main(String[] args) {\n" +
				"		foo(0);\n" +
				"	 }\n" +
				"}",
            },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test153() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportIncompleteEnumSwitch, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
                "enum E { A, B, C }\n" +
				"public class X {\n" +
				"    @SuppressWarnings({\"incomplete-switch\"})\n" +
				"	 public static void main(String[] args) {\n" +
				"		for (E e : E.values()) {\n" +
				"			switch(e) {\n" +
				"				case A :\n" +
				"					System.out.println(e);\n" +
				"				break;\n" +
				"			}\n" +
				"		}\n" +
				"	 }\n" +
				"}",
            },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test154() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
				"public class X {\n" +
				"	 static int i;\n" +
				"    @SuppressWarnings({\"hiding\"})\n" +
				"	 public static void main(String[] args) {\n" +
				"		for (int i = 0, max = args.length; i < max; i++) {\n" +
				"			System.out.println(args[i]);\n" +
				"		}\n" +
				"	 }\n" +
				"}",
            },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test155() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@SuppressWarnings({\"hiding\"})\n" +
	   			"public class X {	\n"+
    			"	{ int x = \n"+
    			"		new Object() { 	\n"+
    			"			int foo() {	\n"+
    			"				int x = 0;\n" +
    			"				return x;	\n"+
    			"			}	\n"+
    			"		}.foo();	\n"+
    			"	}	\n"+
    			"}\n",
           },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test156() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportTypeParameterHiding, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
	   			"class T {}\n" +
				"@SuppressWarnings({\"hiding\"})\n" +
	   			"public class X<T> {\n"+
    			"}\n",
           },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test157() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportHiddenCatchBlock, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
    			"public class X {\n" +
				"   @SuppressWarnings({\"hiding\"})\n" +
    			"	public static void main(String[] args) {\n" +
    			"		try {\n" +
    			"			throw new BX();\n" +
    			"		} catch(BX e) {\n" +
    			"		} catch(AX e) {\n" +
    			"		}\n" +
    			"	}\n" +
    			"} \n" +
				"@SuppressWarnings({\"serial\"})\n" +
	   			"class AX extends Exception {}\n" +
				"@SuppressWarnings({\"serial\"})\n" +
    			"class BX extends AX {}\n"
            },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test158() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportFinallyBlockNotCompletingNormally, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
    			"public class X {\n" +
				"   @SuppressWarnings({\"finally\"})\n" +
    			"	public static void main(String[] args) {\n" +
    			"		try {\n" +
    			"			throw new AX();\n" +
    			"		} finally {\n" +
    			"			return;\n" +
    			"		}\n" +
    			"	}\n" +
    			"} \n" +
				"@SuppressWarnings({\"serial\"})\n" +
	   			"class AX extends Exception {}"
            },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test159() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportIndirectStaticAccess, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@SuppressWarnings({\"static-access\"})\n" +
	   			"public class X extends XZ {\n" +
    			"	\n" +
    			"	void foo() {\n" +
    			"		int j = X.S;\n" +
    			"		int k = super.S;\n" +
    			"		int l = XZ.S;\n" +
    			"		int m = XY.S;\n" +
    			"		\n" +
    			"		bar();\n" +
    			"		X.bar();\n" +
    			"		XY.bar();\n" +
    			"		XZ.bar();\n" +
    			"	}\n" +
    			"}\n" +
    			"class XY {\n" +
    			"	static int S = 10;\n" +
    			"	static void bar(){}\n" +
    			"}\n" +
    			"class XZ extends XY {\n" +
    			"}"
            },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test160() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
				"@SuppressWarnings(\"static-access\")\n" +
	   			"public class X {\n" +
    			"	void foo() {\n" +
    			"		int m = new XY().S;\n" +
    			"	}\n" +
    			"}\n" +
    			"class XY {\n" +
    			"	static int S = 10;\n" +
    			"}"
            },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test161() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportUnqualifiedFieldAccess, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
                "@SuppressWarnings(\"unqualified-field-access\")\n" +
	   			"public class X {\n" +
	   			"	int i;\n" +
    			"	int foo() {\n" +
    			"		return i;\n" +
    			"	}\n" +
    			"}"
            },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test162() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
                "@SuppressWarnings({\"unchecked\", \"rawtypes\"})\n" +
				"public class X<T> {\n" +
				"    \n" +
				"    public static void main(String[] args) {\n" +
				"        AX ax = new AX();\n" +
				"        AX ax2 = ax.p;\n" +
				"        ax.p = new AX<String>();\n" +
				"        ax.q = new AX<String>();\n" +
				"        ax.r = new AX<Object>();\n" +
				"        System.out.println(ax2);\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"class AX <P> {\n" +
				"    AX<P> p;\n" +
				"    AX<Object> q;\n" +
				"    AX<String> r;\n" +
				"    BX<String> s;\n" +
				"}\n" +
				"\n" +
				"class BX<Q> {\n" +
				"}\n",
            },
			"",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test163() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownException, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
                "import java.io.*;\n" +
                "@SuppressWarnings(\"unused\")\n" +
				"public class X<T> {\n" +
				"    \n" +
				"    public void foo(int i) throws java.io.IOException {\n" +
				"       int j = 0;\n" +
				"		class C {\n" +
				"			private void bar() {}\n" +
				"		}\n" +
				"    }\n" +
				"}",
				"Y.java", // =================
				"public class Y extends Zork {\n" +
				"}\n", // =================
			},
			"----------\n" +
			"1. ERROR in Y.java (at line 1)\n" +
			"	public class Y extends Zork {\n" +
			"	                       ^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n",
			null,
			true,
			options
		);
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test164() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.WARNING);
        this.runNegativeTest(
            new String[] {
                "X.java",
                "@SuppressWarnings({\"synthetic-access\", \"unused\"})\n" +
				"public class X {\n" +
				"    private int i;\n" +
				"	 private void bar() {}\n" +
				"    public void foo() {\n" +
				"       class C {\n" +
				"			private void bar() {\n" +
				"				System.out.println(i);\n" +
				"				i = 0;\n" +
				"				bar();\n" +
				"			}\n" +
				"		};\n" +
				"		new C().bar();\n" +
				"    }\n" +
				"}"
            },
            "",
			null,
			true,
			options
		);
    }

    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=99009
    public void test165() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportMissingDeprecatedAnnotation, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsDeprecatedRef, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotation, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, CompilerOptions.PRIVATE);
	    this.runConformTest(
	    	true,
            new String[] {
                "X.java",
				"/**\n" +
				" * @see Y\n" +
				" */\n" +
                "@SuppressWarnings(\"deprecation\")\n" +
				"public class X extends Y {\n" +
				"	 /**\n" +
				"	  * @see Y#foo()\n" +
				"	  * @see Y#j\n" +
				"	  */\n" +
				"    public void foo() {\n" +
				"		super.foo();\n" +
				"    }\n" +
				"}",
				"Y.java",
				"/**\n" +
				" * @deprecated\n" +
				" */\n" +
				"public class Y {\n" +
				"	/**\n" +
				"	 * @deprecated\n" +
				"	 */\n" +
				"	public void foo() {}\n" +
				"	/**\n" +
				"	 * @deprecated\n" +
				"	 */\n" +
				"	public int j;\n" +
				"}"
            },
            null,
            options,
            "",
			null, null,
			JavacTestOptions.SKIP /* suppressed deprecation related warnings */
		);
    }

    // check array handling of singleton
	public void test166() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"import java.lang.annotation.Inherited;\n" +
				"\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@Inherited()\n" +
				"@interface ParameterAnnotation {\n" +
				"	String value() default \"Default\";\n" +
				"}\n"+
				"@interface ClassAnnotation {\n" +
				"	String value() default \"Default\";\n" +
				"}\n" +
				"\n" +
				"enum EnumClass{\n" +
				"	Value1, Value2, Value3\n" +
				"}\n" +
				"\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@Inherited()\n" +
				"@interface ValueAnnotation {\n" +
				"	String value() default \"Default\";\n" +
				"	boolean booleanValue() default true;\n" +
				"	char charValue() default \'q\';\n" +
				"	byte byteValue() default 123;\n" +
				"	short shortValue() default 12345;\n" +
				"	int intValue() default 1234567890;\n" +
				"	float floatValue() default 12345.6789f;\n" +
				"	double doubleValue() default 12345.6789;\n" +
				"	long longValue() default 1234567890123456789l;\n" +
				"	String stringValue() default \"stringValue\";\n" +
				"	EnumClass enumValue() default EnumClass.Value1;\n" +
				"	Class classValue() default EnumClass.class;\n" +
				"	ClassAnnotation annotationValue() default @ClassAnnotation();\n" +
				"	boolean[] booleanArrayValue() default {true, false};\n" +
				"	char[] charArrayValue() default {\'q\', \'m\'};\n" +
				"	byte[] byteArrayValue() default {123, -123};\n" +
				"	short[] shortArrayValue() default {12345, -12345};\n" +
				"	int[] intArrayValue() default {1234567890, -1234567890};\n" +
				"	float[] floatArrayValue() default {12345.6789f, -12345.6789f};\n" +
				"	double[] doubleArrayValue() default {12345.6789, -12345.6789};\n" +
				"	long[] longArrayValue() default {1234567890123456789l, -1234567890123456789l};\n" +
				"	String[] stringArrayValue() default {\"stringValue\", \"valueString\"};\n" +
				"	EnumClass[] enumArrayValue() default {EnumClass.Value1, EnumClass.Value2};\n" +
				"	Class[] classArrayValue() default {X.class, EnumClass.class};\n" +
				"	ClassAnnotation[] annotationArrayValue() default {@ClassAnnotation(), @ClassAnnotation()};\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"	@ValueAnnotation(\n" +
				"		value=\"ValueAnnotation\",\n" +
				"		booleanValue=true,\n" +
				"		charValue=\'m\',\n" +
				"		byteValue=-123,\n" +
				"		shortValue=-12345,\n" +
				"		intValue=-1234567890,\n" +
				"		floatValue=-12345.6789f,\n" +
				"		doubleValue=-12345.6789,\n" +
				"		longValue=-1234567890123456789l,\n" +
				"		stringValue=\"valueString\",\n" +
				"		enumValue=EnumClass.Value3,\n" +
				"		classValue=X.class,\n" +
				"		annotationValue=@ClassAnnotation(value=\"ClassAnnotation\"),\n" +
				"		booleanArrayValue={\n" +
				"			false,\n" +
				"			true\n" +
				"		},\n" +
				"		charArrayValue={\n" +
				"			\'m\',\n" +
				"			\'q\'\n" +
				"		},\n" +
				"		byteArrayValue={\n" +
				"			-123,\n" +
				"			123\n" +
				"		},\n" +
				"		shortArrayValue={\n" +
				"			-12345,\n" +
				"			12345\n" +
				"		},\n" +
				"		intArrayValue={\n" +
				"			-1234567890,\n" +
				"			1234567890\n" +
				"		},\n" +
				"		floatArrayValue={\n" +
				"			-12345.6789f,\n" +
				"			12345.6789f\n" +
				"		},\n" +
				"		doubleArrayValue={\n" +
				"			-12345.6789,\n" +
				"			12345.6789\n" +
				"		},\n" +
				"		longArrayValue={\n" +
				"			-1234567890123456789l,\n" +
				"			1234567890123456789l\n" +
				"		},\n" +
				"		stringArrayValue={\n" +
				"			\"valueString\",\n" +
				"			\"stringValue\"\n" +
				"		},\n" +
				"		enumArrayValue={\n" +
				"			EnumClass.Value2,\n" +
				"			EnumClass.Value1\n" +
				"		},\n" +
				"		classArrayValue={\n" +
				"			EnumClass.class,\n" +
				"			X.class\n" +
				"		},\n" +
				"		annotationArrayValue={\n" +
				"			@ClassAnnotation(value=\"ClassAnnotation1\"),\n" +
				"			@ClassAnnotation(value=\"ClassAnnotation2\")\n" +
				"		})\n" +
				"	public String field;\n" +
				"	@ValueAnnotation(\n" +
				"		value=\"ValueAnnotation\",\n" +
				"		booleanValue=true,\n" +
				"		charValue=\'m\',\n" +
				"		byteValue=-123,\n" +
				"		shortValue=-12345,\n" +
				"		intValue=-1234567890,\n" +
				"		floatValue=-12345.6789f,\n" +
				"		doubleValue=-12345.6789,\n" +
				"		longValue=-1234567890123456789l,\n" +
				"		stringValue=\"valueString\",\n" +
				"		enumValue=EnumClass.Value3,\n" +
				"		classValue=X.class,\n" +
				"		annotationValue=@ClassAnnotation(value=\"ClassAnnotation\"),\n" +
				"		booleanArrayValue={\n" +
				"			false,\n" +
				"			true\n" +
				"		},\n" +
				"		charArrayValue={\n" +
				"			\'m\',\n" +
				"			\'q\'\n" +
				"		},\n" +
				"		byteArrayValue={\n" +
				"			-123,\n" +
				"			123\n" +
				"		},\n" +
				"		shortArrayValue={\n" +
				"			-12345,\n" +
				"			12345\n" +
				"		},\n" +
				"		intArrayValue={\n" +
				"			-1234567890,\n" +
				"			1234567890\n" +
				"		},\n" +
				"		floatArrayValue={\n" +
				"			-12345.6789f,\n" +
				"			12345.6789f\n" +
				"		},\n" +
				"		doubleArrayValue={\n" +
				"			-12345.6789,\n" +
				"			12345.6789\n" +
				"		},\n" +
				"		longArrayValue={\n" +
				"			-1234567890123456789l,\n" +
				"			1234567890123456789l\n" +
				"		},\n" +
				"		stringArrayValue={\n" +
				"			\"valueString\",\n" +
				"			\"stringValue\"\n" +
				"		},\n" +
				"		enumArrayValue={\n" +
				"			EnumClass.Value2,\n" +
				"			EnumClass.Value1\n" +
				"		},\n" +
				"		classArrayValue={\n" +
				"			EnumClass.class,\n" +
				"			X.class\n" +
				"		},\n" +
				"		annotationArrayValue={\n" +
				"			@ClassAnnotation(value=\"ClassAnnotation1\"),\n" +
				"			@ClassAnnotation(value=\"ClassAnnotation2\")\n" +
				"		})\n" +
				"	public X(@ParameterAnnotation(value=\"ParameterAnnotation\") @Deprecated() String param1, @ParameterAnnotation(value=\"ParameterAnnotation\") String param2) {\n" +
				"	}\n" +
				"	@ValueAnnotation(\n" +
				"		value=\"ValueAnnotation\",\n" +
				"		booleanValue=true,\n" +
				"		charValue=\'m\',\n" +
				"		byteValue=-123,\n" +
				"		shortValue=-12345,\n" +
				"		intValue=-1234567890,\n" +
				"		floatValue=-12345.6789f,\n" +
				"		doubleValue=-12345.6789,\n" +
				"		longValue=-1234567890123456789l,\n" +
				"		stringValue=\"valueString\",\n" +
				"		enumValue=EnumClass.Value3,\n" +
				"		classValue=X.class,\n" +
				"		annotationValue=@ClassAnnotation(value=\"ClassAnnotation\"),\n" +
				"		booleanArrayValue={\n" +
				"			false,\n" +
				"			true\n" +
				"		},\n" +
				"		charArrayValue={\n" +
				"			\'m\',\n" +
				"			\'q\'\n" +
				"		},\n" +
				"		byteArrayValue={\n" +
				"			-123,\n" +
				"			123\n" +
				"		},\n" +
				"		shortArrayValue={\n" +
				"			-12345,\n" +
				"			12345\n" +
				"		},\n" +
				"		intArrayValue={\n" +
				"			-1234567890,\n" +
				"			1234567890\n" +
				"		},\n" +
				"		floatArrayValue={\n" +
				"			-12345.6789f,\n" +
				"			12345.6789f\n" +
				"		},\n" +
				"		doubleArrayValue={\n" +
				"			-12345.6789,\n" +
				"			12345.6789\n" +
				"		},\n" +
				"		longArrayValue={\n" +
				"			-1234567890123456789l,\n" +
				"			1234567890123456789l\n" +
				"		},\n" +
				"		stringArrayValue={\n" +
				"			\"valueString\",\n" +
				"			\"stringValue\"\n" +
				"		},\n" +
				"		enumArrayValue={\n" +
				"			EnumClass.Value2,\n" +
				"			EnumClass.Value1\n" +
				"		},\n" +
				"		classArrayValue={\n" +
				"			EnumClass.class,\n" +
				"			X.class\n" +
				"		},\n" +
				"		annotationArrayValue={\n" +
				"			@ClassAnnotation(value=\"ClassAnnotation1\"),\n" +
				"			@ClassAnnotation(value=\"ClassAnnotation2\")\n" +
				"		})\n" +
				"	public void method(@ParameterAnnotation(value=\"ParameterAnnotation\") @Deprecated() String param1, @ParameterAnnotation(value=\"ParameterAnnotation\") String param2){\n" +
				"	}\n" +
				"}"
			},
		"");

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		final byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		new ClassFileReader(classFileBytes, "X.java".toCharArray(), true);
		disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=99469
	public void test167() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public foo(@Deprecated() String s) {\n" +
				"	}\n" +
				"}\n",
			},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public foo(@Deprecated() String s) {\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Return type for the method is missing\n" +
		"----------\n");
    }
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=94759
    public void test168() {
    	String expectedOutput = new CompilerOptions(getCompilerOptions()).sourceLevel < ClassFileConstants.JDK1_6
			?	"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	@Override I clone();\n" +
				"	            ^^^^^^^\n" +
				"The method clone() of type I must override a superclass method\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 7)\n" +
				"	@Override void foo();\n" +
				"	               ^^^^^\n" +
				"The method foo() of type J must override a superclass method\n" +
				"----------\n"
			:	"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	@Override I clone();\n" +
				"	            ^^^^^^^\n" +
				"The method clone() of type I must override or implement a supertype method\n" +
				"----------\n";
    	this.runNegativeTest(
            new String[] {
                "X.java",
				"interface I {\n" +
				"	@Override I clone();\n" +
				"	void foo();\n" +
				"}\n" +
				"\n" +
				"interface J extends I {\n" +
				"	@Override void foo();\n" +
				"}\n",
           },
           expectedOutput);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=97220
    public void test169() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        this.runConformTest(
        	true,
            new String[] {
                "X.java",
    			"@SuppressWarnings(\"serial\")\n" +
    			"public class X extends Exception {\n" +
    			"	String s = \"Hello\"; \n" +
    			"}"
            },
            null,
            customOptions,
            "----------\n" +
    		"1. WARNING in X.java (at line 3)\n" +
    		"	String s = \"Hello\"; \n" +
    		"	           ^^^^^^^\n" +
    		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" +
    		"----------\n",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=97220 - variation
    public void test170() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        this.runConformTest(
            new String[] {
                "X.java",
    			"public class X extends Exception {\n" +
    			"   @SuppressWarnings(\"nls\")\n" +
    			"	String s = \"Hello\"; \n" +
    			"}"
            },
    		"",
			null, true, null, customOptions, null);
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=97220 - variation
    public void test171() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        this.runConformTest(
        	true,
            new String[] {
                "X.java",
    			"public class X extends Exception {\n" +
    			"   @SuppressWarnings(\"nls\")\n" +
    			"	String s = \"Hello\"; \n" +
    			"   @SuppressWarnings(\"serial\")\n" +
    			"	String s2 = \"Hello2\"; \n" +
    			"}"
            },
            null, customOptions,
    		"----------\n" +
    		"1. WARNING in X.java (at line 1)\n" +
    		"	public class X extends Exception {\n" +
    		"	             ^\n" +
    		"The serializable class X does not declare a static final serialVersionUID field of type long\n" +
    		"----------\n" +
    		"2. WARNING in X.java (at line 4)\n" +
    		"	@SuppressWarnings(\"serial\")\n" +
    		"	                  ^^^^^^^^\n" +
    		"Unnecessary @SuppressWarnings(\"serial\")\n" +
    		"----------\n" +
    		"3. WARNING in X.java (at line 5)\n" +
    		"	String s2 = \"Hello2\"; \n" +
    		"	            ^^^^^^^^\n" +
    		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" +
    		"----------\n",
    		null, null, JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=97220 - variation
    public void test172() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        this.runConformTest(
        	true,
        	new String[] {
                "X.java",
    			"@SuppressWarnings(\"serial\")\n" +
    			"public class X extends Exception {\n" +
    			"   @SuppressWarnings(\"nls\")\n" +
    			"	String s = \"Hello\"; \n" +
    			"   @SuppressWarnings(\"serial\")\n" +
    			"	String s2 = \"Hello2\"; \n" +
    			"}"
            },
            null, customOptions,
    		"----------\n" +
    		"1. WARNING in X.java (at line 5)\n" +
    		"	@SuppressWarnings(\"serial\")\n" +
    		"	                  ^^^^^^^^\n" +
    		"Unnecessary @SuppressWarnings(\"serial\")\n" +
    		"----------\n" +
    		"2. WARNING in X.java (at line 6)\n" +
    		"	String s2 = \"Hello2\"; \n" +
    		"	            ^^^^^^^^\n" +
    		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" +
    		"----------\n",
			null, null, JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=97220 - variation
    public void test173() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        this.runConformTest(
        	true,
            new String[] {
                "X.java",
    			"@interface Annot {\n" +
    			"    String value() default \"NONE\";\n" +
    			"}\n" +
    			"@Annot(\"serial\")\n" +
    			"public class X extends Exception {\n" +
    			"   @SuppressWarnings(\"nls\")\n" +
    			"	String s = \"Hello\"; \n" +
    			"   @SuppressWarnings(\"serial\")\n" +
    			"	String s2 = \"Hello2\"; \n" +
    			"}"
            },
            null,
            customOptions,
    		"----------\n" +
    		"1. WARNING in X.java (at line 5)\n" +
    		"	public class X extends Exception {\n" +
    		"	             ^\n" +
    		"The serializable class X does not declare a static final serialVersionUID field of type long\n" +
    		"----------\n" +
    		"2. WARNING in X.java (at line 8)\n" +
    		"	@SuppressWarnings(\"serial\")\n" +
    		"	                  ^^^^^^^^\n" +
    		"Unnecessary @SuppressWarnings(\"serial\")\n" +
    		"----------\n" +
    		"3. WARNING in X.java (at line 9)\n" +
    		"	String s2 = \"Hello2\"; \n" +
    		"	            ^^^^^^^^\n" +
    		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" +
    		"----------\n",
			null, null, JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=97220 - variation
    public void test174() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        final String source = "@interface Annot {\n" +
    			"    int value() default 0;\n" +
    			"}\n" +
    			"@interface Annot2 {\n" +
    			"    String value();\n" +
    			"}\n" +
    			"@Annot(value=5)\n" +
    			"public class X {\n" +
    			"   @Annot2(value=\"nls\")\n" +
    			"	String s = null; \n" +
    			"   @SuppressWarnings(\"serial\")\n" +
    			"	String s2 = \"Hello2\"; \n" +
    			"}";
		this.runConformTest(
			true,
            new String[] {
                "X.java",
    			source
            },
            null, customOptions,
    		"----------\n" +
    		"1. WARNING in X.java (at line 11)\n" +
    		"	@SuppressWarnings(\"serial\")\n" +
    		"	                  ^^^^^^^^\n" +
    		"Unnecessary @SuppressWarnings(\"serial\")\n" +
    		"----------\n" +
    		"2. WARNING in X.java (at line 12)\n" +
    		"	String s2 = \"Hello2\"; \n" +
    		"	            ^^^^^^^^\n" +
    		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" +
    		"----------\n",
			null, null, JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=97220 - variation
    public void test175() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        final String source = "@interface Annot {\n" +
    			"    int value() default 0;\n" +
    			"}\n" +
    			"@interface Annot2 {\n" +
    			"    String value();\n" +
    			"}\n" +
    			"@Annot(value=5)\n" +
    			"public class X {\n" +
    			"   @Annot2(value=\"nls\") String s = \"value\"; \n" +
    			"   @SuppressWarnings(\"serial\")\n" +
    			"	String s2 = \"Hello2\"; \n" +
    			"}";
		this.runConformTest(
			true,
            new String[] {
                "X.java",
    			source
            },
            null, customOptions,
    		"----------\n" +
    		"1. WARNING in X.java (at line 9)\n" +
    		"	@Annot2(value=\"nls\") String s = \"value\"; \n" +
    		"	                                ^^^^^^^\n" +
    		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" +
    		"----------\n" +
    		"2. WARNING in X.java (at line 10)\n" +
    		"	@SuppressWarnings(\"serial\")\n" +
    		"	                  ^^^^^^^^\n" +
    		"Unnecessary @SuppressWarnings(\"serial\")\n" +
    		"----------\n" +
    		"3. WARNING in X.java (at line 11)\n" +
    		"	String s2 = \"Hello2\"; \n" +
    		"	            ^^^^^^^^\n" +
    		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" +
    		"----------\n",
			null, null, JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=97220 - variation
    public void test176() {
    	Map customOptions = getCompilerOptions();
    	customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.WARNING);
        final String source = "@interface Annot {\n" +
    			"    int value() default 0;\n" +
    			"}\n" +
    			"@interface Annot2 {\n" +
    			"    String value();\n" +
    			"}\n" +
    			"@Annot(value=5)\n" +
    			"public class X {\n" +
    			"   @Annot2(value=\"nls\") String s = \"value\"; \n" +
    			"   @SuppressWarnings({\"serial\", \"nls\"})\n" +
    			"	String s2 = \"Hello2\"; \n" +
    			"	@Annot(value=5) void foo() {}\n" +
    			"}";
		this.runConformTest(
			true,
            new String[] {
                "X.java",
    			source
            },
            null, customOptions,
    		"----------\n" +
    		"1. WARNING in X.java (at line 9)\n" +
    		"	@Annot2(value=\"nls\") String s = \"value\"; \n" +
    		"	                                ^^^^^^^\n" +
    		"Non-externalized string literal; it should be followed by //$NON-NLS-<n>$\n" +
    		"----------\n" +
    		"2. WARNING in X.java (at line 10)\n" +
    		"	@SuppressWarnings({\"serial\", \"nls\"})\n" +
    		"	                   ^^^^^^^^\n" +
    		"Unnecessary @SuppressWarnings(\"serial\")\n" +
    		"----------\n",
			null, null, JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=108263
    public void test177() {
        this.runConformTest(
            new String[] {
                "X.java",
				"public @interface X {\n" +
				"  public static final Integer foo = B.zzz; \n" +
				"  public static final int foo3 = B.zzz2; \n" +
				"}\n" +
				"class B {\n" +
				"  public static final Integer zzz = new Integer(0);\n" +
				"  public static final int zzz2 = 0;\n" +
				"}\n",
           },
		"");
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=111076
    public void test178() {
        runConformTest(
        	true,
            new String[] {
                "X.java",
    			"import java.util.*;\n" +
    			"public class X {\n" +
    			"	private void testme(boolean check) {\n" +
    			"		ArrayList<Integer> aList = new ArrayList<Integer>();\n" +
    			"		for (@SuppressWarnings(\"unusedLocal\")\n" +
    			"		Integer i : aList) {\n" +
    			"			System.out.println(\"checking\");\n" +
    			"		}\n" +
    			"	}\n" +
    			"}\n",
           },
        null,
		"",
		null,
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=112433
    public void test179() {
    	this.runConformTest(
    		true,
    		new String[] {
    			"X.java",
    			"import static java.lang.annotation.ElementType.*;\n" +
    			"import static java.lang.annotation.RetentionPolicy.*;\n" +
    			"import java.lang.annotation.Retention;\n" +
    			"import java.lang.annotation.Target;\n" +
    			"@Target({TYPE, FIELD, METHOD,\n" +
    			"         PARAMETER, CONSTRUCTOR,\n" +
    			"         LOCAL_VARIABLE, PACKAGE,})\n" +
    			"@Retention(CLASS)\n" +
    			"public @interface X {}"
    		},
    		"",
    		"",
    		null,
    		JavacTestOptions.EclipseJustification.EclipseBug112433);
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=116028
    public void test180() {
    	this.runConformTest(
    		new String[] {
    			"X.java",
    			"import java.lang.reflect.Field;\n" +
    			"\n" +
    			"public class X {\n" +
    			"  @Deprecated public static Object x, y, z;\n" +
    			"\n" +
    			"  public static void main(String[] args) {\n" +
    			"    Class c = X.class;\n" +
    			"    int counter = 0;\n" +
    			"    for (Field f : c.getFields()) {\n" +
    			"      counter += f.getDeclaredAnnotations().length;\n" +
    			"    }\n" +
    			"    System.out.print(counter);\n" +
    			"  }\n" +
    			"}"
    		},
    		"3");
    }
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=116028
    public void test181() {
    	this.runConformTest(
    		new String[] {
    			"X.java",
    			"import java.lang.reflect.Field;\n" +
    			"\n" +
    			"public class X {\n" +
    			"  public static Object x, y, z;\n" +
    			"\n" +
    			"  public static void main(String[] args) {\n" +
    			"    Class c = X.class;\n" +
    			"    int counter = 0;\n" +
    			"    for (Field f : c.getFields()) {\n" +
    			"      counter += f.getDeclaredAnnotations().length;\n" +
    			"    }\n" +
    			"    System.out.print(counter);\n" +
    			"  }\n" +
    			"}"
    		},
    		"0");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=110593
    public void test182() {
    	this.runNegativeTest(
    		new String[] {
    				"X.java", // =================
    				"public class X {\n" +
    				"	void foo(Y y) {\n" +
    				"		y.initialize(null, null, null);\n" +
    				"	}\n" +
    				"}\n" +
    				"\n" +
    				"\n", // =================
    				"Y.java", // =================
    				"public class Y {\n" +
    				"\n" +
    				"	/**\n" +
    				"	 * @deprecated\n" +
    				"	 */\n" +
    				"	public void initialize(Zork z, String s) {\n" +
    				"	}\n" +
    				"\n" +
    				"	public void initialize(Zork z, String s, Thread t) {\n" +
    				"	}\n" +
    				"}\n" +
    				"\n" +
    				"\n", // =================
    		},
    		"----------\n" +
    		"1. ERROR in X.java (at line 3)\n" +
    		"	y.initialize(null, null, null);\n" +
    		"	  ^^^^^^^^^^\n" +
    		"The method initialize(Zork, String, Thread) from the type Y refers to the missing type Zork\n" +
    		"----------\n" +
    		"----------\n" +
    		"1. WARNING in Y.java (at line 6)\n" +
    		"	public void initialize(Zork z, String s) {\n" +
    		"	            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
    		"The deprecated method initialize(Zork, String) of type Y should be annotated with @Deprecated\n" +
    		"----------\n" +
    		"2. ERROR in Y.java (at line 6)\n" +
    		"	public void initialize(Zork z, String s) {\n" +
    		"	                       ^^^^\n" +
    		"Zork cannot be resolved to a type\n" +
    		"----------\n" +
    		"3. ERROR in Y.java (at line 6)\n" +
    		"	public void initialize(Zork z, String s) {\n" +
    		"	                            ^\n" +
    		"Javadoc: Missing tag for parameter z\n" +
    		"----------\n" +
    		"4. ERROR in Y.java (at line 6)\n" +
    		"	public void initialize(Zork z, String s) {\n" +
    		"	                                      ^\n" +
    		"Javadoc: Missing tag for parameter s\n" +
    		"----------\n" +
    		"5. ERROR in Y.java (at line 9)\n" +
    		"	public void initialize(Zork z, String s, Thread t) {\n" +
    		"	                       ^^^^\n" +
    		"Zork cannot be resolved to a type\n" +
    		"----------\n");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=110593 - variation
    public void test183() {
    	this.runNegativeTest(
    		new String[] {
    				"X.java", // =================
    				"public class X {\n" +
    				"	void foo(Y y) {\n" +
    				"		int i = y.initialize;\n" +
    				"	}\n" +
    				"}\n" +
    				"\n", // =================
    				"Y.java", // =================
    				"public class Y {\n" +
    				"\n" +
    				"	/**\n" +
    				"	 * @deprecated\n" +
    				"	 */\n" +
    				"	public Zork initialize;\n" +
    				"}\n" +
    				"\n", // =================
    		},
    		"----------\n" +
    		"1. ERROR in X.java (at line 3)\n" +
    		"	int i = y.initialize;\n" +
    		"	        ^^^^^^^^^^^^\n" +
    		"Zork cannot be resolved to a type\n" +
    		"----------\n" +
    		"2. WARNING in X.java (at line 3)\n" +
    		"	int i = y.initialize;\n" +
    		"	          ^^^^^^^^^^\n" +
    		"The field Y.initialize is deprecated\n" +
    		"----------\n" +
    		"----------\n" +
    		"1. ERROR in Y.java (at line 6)\n" +
    		"	public Zork initialize;\n" +
    		"	       ^^^^\n" +
    		"Zork cannot be resolved to a type\n" +
    		"----------\n" +
    		"2. WARNING in Y.java (at line 6)\n" +
    		"	public Zork initialize;\n" +
    		"	            ^^^^^^^^^^\n" +
    		"The deprecated field Y.initialize should be annotated with @Deprecated\n" +
    		"----------\n");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=110593 - variation
    public void test184() {
    	this.runNegativeTest(
    		new String[] {
    				"X.java", // =================
    				"public class X {\n" +
    				"	void foo() {\n" +
    				"		Y.initialize i;\n" +
    				"	}\n" +
    				"}\n" +
    				"\n" +
    				"\n", // =================
    				"Y.java", // =================
    				"public class Y {\n" +
    				"\n" +
    				"	/**\n" +
    				"	 * @deprecated\n" +
    				"	 */\n" +
    				"	public class initialize extends Zork {\n" +
    				"	}\n" +
    				"}\n" +
    				"\n" +
    				"\n", // =================
    		},
    		"----------\n" +
    		"1. WARNING in X.java (at line 3)\n" +
    		"	Y.initialize i;\n" +
    		"	  ^^^^^^^^^^\n" +
    		"The type Y.initialize is deprecated\n" +
    		"----------\n" +
    		"----------\n" +
    		"1. WARNING in Y.java (at line 6)\n" +
    		"	public class initialize extends Zork {\n" +
    		"	             ^^^^^^^^^^\n" +
    		"The deprecated type Y.initialize should be annotated with @Deprecated\n" +
    		"----------\n" +
    		"2. ERROR in Y.java (at line 6)\n" +
    		"	public class initialize extends Zork {\n" +
    		"	                                ^^^^\n" +
    		"Zork cannot be resolved to a type\n" +
    		"----------\n");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=123522
    public void test185() {
    	this.runNegativeTest(
    		new String[] {
    				"X.java", // =================
    				"import p.A;\n" +
    				"@SuppressWarnings(\"all\")\n" +
    				"public class X {\n" +
    				"	void foo(A a) {\n" +
    				"		Zork z;\n" +
    				"	}\n" +
    				"}\n" +
    				"\n" +
    				"class Y {\n" +
    				"	A a;\n" +
    				"}\n", // =================
    				"p/A.java", // =================
    				"package p;\n" +
    				"@Deprecated\n" +
    				"public class A {\n" +
    				"}\n", // =================
    		},
    		"----------\n" +
    		"1. ERROR in X.java (at line 5)\n" +
    		"	Zork z;\n" +
    		"	^^^^\n" +
    		"Zork cannot be resolved to a type\n" +
    		"----------\n" +
    		"2. WARNING in X.java (at line 10)\n" +
    		"	A a;\n" +
    		"	^\n" +
    		"The type A is deprecated\n" +
    		"----------\n");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=124346
    public void test186() {
    	this.runNegativeTest(
    		new String[] {
    				"p1/X.java", // =================
    				"package p1;\n" +
    				"public class X {\n" +
    				"	@Deprecated\n" +
    				"	class Y implements p2.I {\n" +
    				"		Zork z;\n" +
    				"	}\n" +
    				"}\n", // =================
    				"p2/I.java", // =================
    				"package p2;\n" +
    				"@Deprecated\n" +
    				"public interface I {\n" +
    				"}\n", // =================
    		},
    		"----------\n" +
    		"1. ERROR in p1\\X.java (at line 5)\n" +
    		"	Zork z;\n" +
    		"	^^^^\n" +
    		"Zork cannot be resolved to a type\n" +
    		"----------\n");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=124346 - variation
    public void test187() {
    	this.runNegativeTest(
    		new String[] {
    				"p1/X.java", // =================
    				"package p1;\n" +
    				"import p2.I;\n" +
    				"@Deprecated\n" +
    				"public class X {\n" +
    				"	Zork z;\n" +
    				"}\n", // =================
    				"p2/I.java", // =================
    				"package p2;\n" +
    				"@Deprecated\n" +
    				"public interface I {\n" +
    				"}\n", // =================
    		},
    		"----------\n" +
    		"1. ERROR in p1\\X.java (at line 5)\n" +
    		"	Zork z;\n" +
    		"	^^^^\n" +
    		"Zork cannot be resolved to a type\n" +
    		"----------\n");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=126332
    public void test188() {
    	this.runNegativeTest(
    		new String[] {
				"X.java",
				"@interface A1 {\n" +
				"	int[] values();\n" +
				"}\n" +
				"@A1(values = new int[] { 1, 2 })\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"	}\n" +
				"}",
    		},
    		"----------\n" +
    		"1. ERROR in X.java (at line 4)\n" +
    		"	@A1(values = new int[] { 1, 2 })\n" +
    		"	             ^^^^^^^^^^^^^^^^^^\n" +
    		"The value for annotation attribute A1.values must be an array initializer\n" +
    		"----------\n");
    }
    // partial recompile - keep a binary
	public void test189() {
		this.runConformTest(
			true,
			new String[] {
				"A1.java",
				"@A2(@A1(m1 = \"u\"))\n" +
				"public @interface A1 {\n" +
				"  String m1();\n" +
				"  String m2() default \"v\";\n" +
				"}\n",
				"A2.java",
				"@A2(@A1(m1 = \"u\", m2 = \"w\"))\n" +
				"public @interface A2 {\n" +
				"  A1[] value();\n" +
				"}\n",
			},
			"",
			"",
			null,
			JavacTestOptions.DEFAULT);
		// keep A2 binary, recompile A1 with a name change
		this.runConformTest(
			false, // do not flush A2.class
			new String[] {
				"A1.java",
				"@A2(@A1(m1 = \"u\"))\n" +
				"public @interface A1 {\n" +
				"  String m1();\n" +
				"  String m3() default \"v\";\n" +
				"}\n",
			},
			null,
			"",
			null,
			JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
	}
// transitive closure on binary types does not need to include annotations
public void test190() {
	this.runConformTest(
		new String[] {
			"A.java",
			"public @interface A {\n" +
			"  int value();\n" +
			"}\n"
			},
		"");
	String binName1 = OUTPUT_DIR + File.separator + "bin1";
	File bin1 = new File(binName1);
	bin1.mkdir();
	String [] javaClassLibs = Util.getJavaClassLibs();
	int javaClassLibsLength;
	String [] xClassLibs = new String[(javaClassLibsLength = javaClassLibs.length) + 2];
	System.arraycopy(javaClassLibs, 0, xClassLibs, 0, javaClassLibsLength);
	xClassLibs[javaClassLibsLength] = OUTPUT_DIR;
	xClassLibs[javaClassLibsLength + 1] = binName1;
	(new File(OUTPUT_DIR + File.separator + "A.class")).renameTo(new File(binName1 + File.separator + "A.class"));
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    @A(0)\n" +
			"    void foo() {\n" +
			"        System.out.println(\"SUCCESS\");\n" +
			"    }\n" +
			"    public static void main(String args[]) {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}",
		},
		"SUCCESS",
		xClassLibs,
		false, // do not flush
		null);
	String binName2 = OUTPUT_DIR + File.separator + "bin2";
	File bin2 = new File(binName2);
	bin2.mkdir();
	(new File(OUTPUT_DIR + File.separator + "X.class")).renameTo(new File(binName2 + File.separator + "X.class"));
	String [] yClassLibs = new String[javaClassLibsLength + 2];
	System.arraycopy(javaClassLibs, 0, yClassLibs, 0, javaClassLibsLength);
	yClassLibs[javaClassLibsLength] = OUTPUT_DIR;
	yClassLibs[javaClassLibsLength + 1] = binName2;
	// Y compiles despite the fact that A is not on the classpath
	this.runConformTest(
		new String[] {
			"Y.java",
			"public class Y {\n" +
			"    public static void main(String args[]) {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}",
		},
		"SUCCESS",
		yClassLibs,
		false, // do not flush
		null);
}

// transitive closure on binary types does not need to include annotations - variant
public void test191() {
	this.runConformTest(
		new String[] {
			"A.java",
			"@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)\n" +
			"public @interface A {\n" +
			"  int value();\n" +
			"}\n"
			},
		"");
	String binName1 = OUTPUT_DIR + File.separator + "bin1";
	File bin1 = new File(binName1);
	bin1.mkdir();
	String [] javaClassLibs = Util.getJavaClassLibs();
	int javaClassLibsLength;
	String [] xClassLibs = new String[(javaClassLibsLength = javaClassLibs.length) + 2];
	System.arraycopy(javaClassLibs, 0, xClassLibs, 0, javaClassLibsLength);
	xClassLibs[javaClassLibsLength] = OUTPUT_DIR;
	xClassLibs[javaClassLibsLength + 1] = binName1;
	(new File(OUTPUT_DIR + File.separator + "A.class")).renameTo(new File(binName1 + File.separator + "A.class"));
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    @A(0)\n" +
			"    void foo() {\n" +
			"        System.out.println(\"SUCCESS\");\n" +
			"    }\n" +
			"    public static void main(String args[]) {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}",
		},
		"SUCCESS",
		xClassLibs,
		false, // do not flush
		null);
	String binName2 = OUTPUT_DIR + File.separator + "bin2";
	File bin2 = new File(binName2);
	bin2.mkdir();
	(new File(OUTPUT_DIR + File.separator + "X.class")).renameTo(new File(binName2 + File.separator + "X.class"));
	String [] yClassLibs = new String[javaClassLibsLength + 2];
	System.arraycopy(javaClassLibs, 0, yClassLibs, 0, javaClassLibsLength);
	yClassLibs[javaClassLibsLength] = OUTPUT_DIR;
	yClassLibs[javaClassLibsLength + 1] = binName2;
	// Y compiles despite the fact that A is not on the classpath
	this.runConformTest(
		new String[] {
			"Y.java",
			"public class Y {\n" +
			"    public static void main(String args[]) {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}",
		},
		"SUCCESS",
		yClassLibs,
		false, // do not flush
		null);
}

public void test192() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	@ATest(groups={\"a\",\"b\"})\n" +
			"	void foo(){\n" +
			"	}\n" +
			"	@ATest(groups=\"c\")\n" +
			"	void bar(){\n" +
			"	}\n" +
			"}\n" +
			"@interface ATest {\n" +
			"	String[] groups();\n" +
			"}\n"
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=102160
public void test193() {
	this.runNegativeTest(
		new String[] {
			"A.java",
			"public @interface A {\n" +
			"	A circular1();\n" +
			"}\n" +
			"@interface B {\n" +
			"	A circular2();\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in A.java (at line 2)\n" +
		"	A circular1();\n" +
		"	^\n" +
		"Cycle detected: the annotation type A cannot contain attributes of the annotation type itself\n" +
		"----------\n"
	);
	this.runNegativeTest(
		new String[] {
			"A.java",
			"public @interface A {\n" +
			"	B circular2();\n" +
			"	A circular1();\n" +
			"}\n" +
			"@interface B {\n" +
			"	A circular();\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in A.java (at line 2)\n" +
		"	B circular2();\n" +
		"	^\n" +
		"Cycle detected: a cycle exists between annotation attributes of A and B\n" +
		"----------\n" +
		"2. ERROR in A.java (at line 3)\n" +
		"	A circular1();\n" +
		"	^\n" +
		"Cycle detected: the annotation type A cannot contain attributes of the annotation type itself\n" +
		"----------\n" +
		"3. ERROR in A.java (at line 6)\n" +
		"	A circular();\n" +
		"	^\n" +
		"Cycle detected: a cycle exists between annotation attributes of B and A\n" +
		"----------\n"
	);
	this.runNegativeTest(
		new String[] {
			"A.java",
			"public @interface A {\n" +
			"	A circular1();\n" +
			"	B circular2();\n" +
			"}\n" +
			"@interface B {\n" +
			"	A circular();\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in A.java (at line 2)\n" +
		"	A circular1();\n" +
		"	^\n" +
		"Cycle detected: the annotation type A cannot contain attributes of the annotation type itself\n" +
		"----------\n" +
		"2. ERROR in A.java (at line 3)\n" +
		"	B circular2();\n" +
		"	^\n" +
		"Cycle detected: a cycle exists between annotation attributes of A and B\n" +
		"----------\n" +
		"3. ERROR in A.java (at line 6)\n" +
		"	A circular();\n" +
		"	^\n" +
		"Cycle detected: a cycle exists between annotation attributes of B and A\n" +
		"----------\n"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130017
public void test194() {
	String expectedOutput = new CompilerOptions(getCompilerOptions()).sourceLevel < ClassFileConstants.JDK1_6
	?	"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	@Override\n" +
		"	^^^^^^^^^\n" +
		"The annotation @Override is disallowed for this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	public static void foo() {}\n" +
		"	                   ^^^^^\n" +
		"The method foo() of type X must override a superclass method\n" +
		"----------\n"
	:	"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	@Override\n" +
		"	^^^^^^^^^\n" +
		"The annotation @Override is disallowed for this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	public static void foo() {}\n" +
		"	                   ^^^^^\n" +
		"The method foo() of type X must override or implement a supertype method\n" +
		"----------\n";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class Base {\n" +
			"    public static void foo() {}\n" +
			"}\n" +
			"public class X extends Base {\n" +
			"	@Override\n" +
			"	X(){}\n" +
			"	\n" +
			"    @Override\n" +
			"    public static void foo() {}\n" +
			"}\n"
		},
		expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130516
public void test195() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	@SuppressWarnings(\"cast\")\n" +
			"	void foo() {\n" +
			"		String s = (String) \"hello\";\n" +
			"	}\n" +
			"	void bar() {\n" +
			"		String s = (String) \"hello\";\n" +
			"	}\n" +
			"	Zork z;\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 7)\n" +
		"	String s = (String) \"hello\";\n" +
		"	           ^^^^^^^^^^^^^^^^\n" +
		"Unnecessary cast from String to String\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=133440
public void test196() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public @interface X {\n" +
			"    enum MyEnum {\n" +
			"        VAL_1, VAL_2\n" +
			"    }\n" +
			"    public MyEnum theValue() default null;\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	public MyEnum theValue() default null;\n" +
		"	                                 ^^^^\n" +
		"The value for annotation attribute X.theValue must be an enum constant expression\n" +
		"----------\n");
}
// no override between package private methods
public void test197() {
	String expectedOutput = new CompilerOptions(getCompilerOptions()).sourceLevel < ClassFileConstants.JDK1_6
	?	"----------\n" +
		"1. WARNING in p\\X.java (at line 4)\n" +
		"	void foo() {\n" +
		"	     ^^^^^\n" +
		"The method X.foo() does not override the inherited method from OldStuff since it is private to a different package\n" +
		"----------\n" +
		"2. ERROR in p\\X.java (at line 4)\n" +
		"	void foo() {\n" +
		"	     ^^^^^\n" +
		"The method foo() of type X must override a superclass method\n" +
		"----------\n"
	:	"----------\n" +
		"1. WARNING in p\\X.java (at line 4)\n" +
		"	void foo() {\n" +
		"	     ^^^^^\n" +
		"The method X.foo() does not override the inherited method from OldStuff since it is private to a different package\n" +
		"----------\n" +
		"2. ERROR in p\\X.java (at line 4)\n" +
		"	void foo() {\n" +
		"	     ^^^^^\n" +
		"The method foo() of type X must override or implement a supertype method\n" +
		"----------\n";
    this.runNegativeTest(
        new String[] {
            "p/X.java",
            "package p;\n" +
			"public class X extends q.OldStuff {\n" +
			"	@Override\n" +
			"	void foo() {\n" +
			"	}\n" +
			"}\n",
            "q/OldStuff.java",
            "package q;\n" +
			"public class OldStuff {\n" +
			"	void foo() {\n" +
			"	}	\n" +
			"}\n",
        },
        expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=134129
public void test198() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"@interface Anno {\n" +
			"        boolean b() default false;\n" +
			"        String[] c() default \"\";\n" +
			"}\n" +
			"@Anno(b = {})\n" +
			"public class X {\n" +
			"	@Anno(c = { 0 })\n" +
			"	void foo(){}\n" +
			"}\n",
        },
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	@Anno(b = {})\n" +
		"	          ^^\n" +
		"Type mismatch: cannot convert from Object[] to boolean\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	@Anno(c = { 0 })\n" +
		"	            ^\n" +
		"Type mismatch: cannot convert from int to String\n" +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=138443
public void test199() {
	this.runConformTest(
		true,
		new String[] {
			"X.java",
			"@interface AttributeOverrides {\n" +
			"	AttributeOverride[] value();\n" +
			"}\n" +
			"@interface AttributeOverride {\n" +
			"	String name();\n" +
			"	Column column();\n" +
			"}\n" +
			"@interface Column {\n" +
			"	String name();\n" +
			"}\n" +
			"@AttributeOverrides({\n" +
			"    @AttributeOverride( name=\"city\", column=@Column( name=\"DIAB99C_TXCTY\" )),\n" +
			"    @AttributeOverride( name=\"state\", column=@Column( name=\"DIAB99C_TXSTAT\" )),\n" +
			"    @AttributeOverride( name=\"zipCode\", column=@Column( name=\"DIAB99C_TXZIP\")),\n" +
			"}) public class X {}"
		},
		"",
		"",
		null,
		JavacTestOptions.EclipseJustification.EclipseBug112433);
}
// JLS 3 - 9.6: cannot override Object's methods
public void test200() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"@interface X {\n" +
			"  int clone();\n" +
			"  String finalize();\n" +
			"  boolean getClass();\n" +
			"  long notify();\n" +
			"  double notifyAll();\n" +
			"  float wait();\n" +
			"}\n",
        },
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	int clone();\n" +
		"	    ^^^^^^^\n" +
		"The annotation type X cannot override the method Object.clone()\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	String finalize();\n" +
		"	       ^^^^^^^^^^\n" +
		"The annotation type X cannot override the method Object.finalize()\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	boolean getClass();\n" +
		"	        ^^^^^^^^^^\n" +
		"The annotation type X cannot override the method Object.getClass()\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 5)\n" +
		"	long notify();\n" +
		"	     ^^^^^^^^\n" +
		"The annotation type X cannot override the method Object.notify()\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 6)\n" +
		"	double notifyAll();\n" +
		"	       ^^^^^^^^^^^\n" +
		"The annotation type X cannot override the method Object.notifyAll()\n" +
		"----------\n" +
		"6. ERROR in X.java (at line 7)\n" +
		"	float wait();\n" +
		"	      ^^^^^^\n" +
		"The annotation type X cannot override the method Object.wait()\n" +
		"----------\n");
}
//JLS 3 - 9.6: cannot override Annotation's methods
public void test201() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"@interface X {\n" +
			"  char hashCode();\n" +
			"  int annotationType();\n" +
			"  Class toString();\n" +
			"}\n",
        },
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	char hashCode();\n" +
		"	     ^^^^^^^^^^\n" +
		"The annotation type X cannot override the method Annotation.hashCode()\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	int annotationType();\n" +
		"	    ^^^^^^^^^^^^^^^^\n" +
		"The annotation type X cannot override the method Annotation.annotationType()\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 4)\n" +
		"	Class toString();\n" +
		"	^^^^^\n" +
		"Class is a raw type. References to generic type Class<T> should be parameterized\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 4)\n" +
		"	Class toString();\n" +
		"	      ^^^^^^^^^^\n" +
		"The annotation type X cannot override the method Annotation.toString()\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=143259
public void test202() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			" public class X {\n" +
			" @Ann(m=Object)\n" +
			" private int foo;\n" +
			" private NonExisting bar;\n" +
			" }\n" +
			" @interface Ann {\n" +
			" String m();\n" +
			" }\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	@Ann(m=Object)\n" +
		"	       ^^^^^^\n" +
		"Object cannot be resolved to a variable\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	private NonExisting bar;\n" +
		"	        ^^^^^^^^^^^\n" +
		"NonExisting cannot be resolved to a type\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=143259 - variation
public void test203() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	@Ann(m=Object())\n" +
			"	private void foo(){}\n" +
			"	private NonExisting bar(){}\n" +
			"}\n" +
			"@interface Ann {\n" +
			"    String m();\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	@Ann(m=Object())\n" +
		"	       ^^^^^^\n" +
		"The method Object() is undefined for the type X\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	private NonExisting bar(){}\n" +
		"	        ^^^^^^^^^^^\n" +
		"NonExisting cannot be resolved to a type\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=143259 - variation
public void test204() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	@Ann(m=bar(null))\n" +
			"	private void foo(){}\n" +
			"	private NonExisting bar(NonExisting ne){}\n" +
			"}\n" +
			"@interface Ann {\n" +
			"    String m();\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	@Ann(m=bar(null))\n" +
		"	       ^^^\n" +
		"The method bar(NonExisting) from the type X refers to the missing type NonExisting\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	private NonExisting bar(NonExisting ne){}\n" +
		"	        ^^^^^^^^^^^\n" +
		"NonExisting cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	private NonExisting bar(NonExisting ne){}\n" +
		"	                        ^^^^^^^^^^^\n" +
		"NonExisting cannot be resolved to a type\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=143259 - variation
public void test205() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	@Ann(m=foo())\n" +
			"	private void foo(){}\n" +
			"	private NonExisting bar(NonExisting ne){}\n" +
			"}\n" +
			"@interface Ann {\n" +
			"    String m();\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	@Ann(m=foo())\n" +
		"	       ^^^^^\n" +
		"Type mismatch: cannot convert from void to String\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	private NonExisting bar(NonExisting ne){}\n" +
		"	        ^^^^^^^^^^^\n" +
		"NonExisting cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	private NonExisting bar(NonExisting ne){}\n" +
		"	                        ^^^^^^^^^^^\n" +
		"NonExisting cannot be resolved to a type\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=143259 - variation
public void test206() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	@Ann(m=bar())\n" +
			"	private void foo(){}\n" +
			"	private NonExisting bar(){}\n" +
			"}\n" +
			"@interface Ann {\n" +
			"    String m();\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	@Ann(m=bar())\n" +
		"	       ^^^\n" +
		"The method bar() from the type X refers to the missing type NonExisting\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	private NonExisting bar(){}\n" +
		"	        ^^^^^^^^^^^\n" +
		"NonExisting cannot be resolved to a type\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=143259 - variation
public void test207() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			" public class X {\n" +
			"@Ann(m=foo)\n" +
			" private NonExisting foo;\n" +
			" private NonExisting bar;\n" +
			" }\n" +
			" @interface Ann {\n" +
			" String m();\n" +
			" }\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	@Ann(m=foo)\n" +
		"	       ^^^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	@Ann(m=foo)\n" +
		"	       ^^^\n" +
		"NonExisting cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 3)\n" +
		"	private NonExisting foo;\n" +
		"	        ^^^^^^^^^^^\n" +
		"NonExisting cannot be resolved to a type\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 4)\n" +
		"	private NonExisting bar;\n" +
		"	        ^^^^^^^^^^^\n" +
		"NonExisting cannot be resolved to a type\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=149751
public void test208() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"import java.lang.annotation.Retention;\n" +
			"import static java.lang.annotation.RetentionPolicy.RUNTIME;\n" +
			"@Retention(RUNTIME) @interface MyAnnotation {\n" +
			"    public MyEnum value();\n" +
			"}\n" +
			"enum MyEnum {\n" +
			"    ONE, TWO, THREE\n" +
			"}\n" +
			"@MyAnnotation(X.FOO) class MyClass {\n" +
			"}\n" +
			"public class X {\n" +
			"    public static final MyEnum FOO = MyEnum.TWO;\n" +
			"    public static void main(String[] args) {\n" +
			"        MyAnnotation annotation =\n" +
			"                MyClass.class.getAnnotation(MyAnnotation.class);\n" +
			"        System.out.println(annotation.value().toString());\n" +
			"    }\n" +
			"}",
        },
        "----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	@MyAnnotation(X.FOO) class MyClass {\n" +
		"	              ^^^^^\n" +
		"The value for annotation attribute MyAnnotation.value must be an enum constant expression\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=149751
public void test209() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"import java.lang.annotation.Retention;\n" +
			"import static java.lang.annotation.RetentionPolicy.RUNTIME;\n" +
			"@Retention(RUNTIME) @interface MyAnnotation {\n" +
			"    public MyEnum value();\n" +
			"}\n" +
			"enum MyEnum {\n" +
			"    ONE, TWO, THREE\n" +
			"}\n" +
			"@MyAnnotation(value=X.FOO) class MyClass {\n" +
			"}\n" +
			"public class X {\n" +
			"    public static final MyEnum FOO = MyEnum.TWO;\n" +
			"    public static void main(String[] args) {\n" +
			"        MyAnnotation annotation =\n" +
			"                MyClass.class.getAnnotation(MyAnnotation.class);\n" +
			"        System.out.println(annotation.value().toString());\n" +
			"    }\n" +
			"}",
        },
        "----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	@MyAnnotation(value=X.FOO) class MyClass {\n" +
		"	                    ^^^^^\n" +
		"The value for annotation attribute MyAnnotation.value must be an enum constant expression\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=149751
public void test210() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"import java.lang.annotation.Retention;\n" +
			"import static java.lang.annotation.RetentionPolicy.RUNTIME;\n" +
			"@Retention(RUNTIME) @interface MyAnnotation {\n" +
			"    public MyEnum[] value();\n" +
			"}\n" +
			"enum MyEnum {\n" +
			"    ONE, TWO, THREE\n" +
			"}\n" +
			"@MyAnnotation(value= { X.FOO }) class MyClass {\n" +
			"}\n" +
			"public class X {\n" +
			"    public static final MyEnum FOO = MyEnum.TWO;\n" +
			"    public static void main(String[] args) {\n" +
			"        MyAnnotation annotation =\n" +
			"                MyClass.class.getAnnotation(MyAnnotation.class);\n" +
			"        System.out.println(annotation.value().toString());\n" +
			"    }\n" +
			"}",
        },
        "----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	@MyAnnotation(value= { X.FOO }) class MyClass {\n" +
		"	                       ^^^^^\n" +
		"The value for annotation attribute MyAnnotation.value must be an enum constant expression\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=149751
public void test211() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"import java.lang.annotation.Retention;\n" +
			"import static java.lang.annotation.RetentionPolicy.RUNTIME;\n" +
			"@Retention(RUNTIME) @interface MyAnnotation {\n" +
			"    public MyEnum[] value();\n" +
			"}\n" +
			"enum MyEnum {\n" +
			"    ONE, TWO, THREE\n" +
			"}\n" +
			"@MyAnnotation(value= { null }) class MyClass {\n" +
			"}\n" +
			"public class X {\n" +
			"    public static final MyEnum FOO = MyEnum.TWO;\n" +
			"    public static void main(String[] args) {\n" +
			"        MyAnnotation annotation =\n" +
			"                MyClass.class.getAnnotation(MyAnnotation.class);\n" +
			"        System.out.println(annotation.value().toString());\n" +
			"    }\n" +
			"}",
        },
        "----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	@MyAnnotation(value= { null }) class MyClass {\n" +
		"	                       ^^^^\n" +
		"The value for annotation attribute MyAnnotation.value must be an enum constant expression\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156891
public void test212() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"import java.lang.annotation.Retention;\n" +
			"import static java.lang.annotation.RetentionPolicy.RUNTIME;\n" +
			"@Retention(RUNTIME) @interface MyAnnotation {\n" +
			"    public MyEnum[] values();\n" +
			"}\n" +
			"enum MyEnum {\n" +
			"    ONE, TWO, THREE\n" +
			"}\n" +
			"public class X {\n" +
			"\n" +
			"		private static final MyEnum[] myValues = { MyEnum.ONE, MyEnum.TWO };\n" +
			"       @MyAnnotation(values=myValues) \n" +
			"       public void dothetrick(){} \n" +
			"\n" +
			"        public static void main(String[] args)throws Exception {\n" +
			"                MyAnnotation sluck = X.class.getMethod(\"dothetrick\", new Class[0]).getAnnotation(MyAnnotation.class);\n" +
			"                System.out.println(sluck.values().length);\n" +
			"        }\n" +
			"}",
        },
        "----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	@MyAnnotation(values=myValues) \n" +
		"	                     ^^^^^^^^\n" +
		"The value for annotation attribute MyAnnotation.values must be an array initializer\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156891
public void test213() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"import java.lang.annotation.Retention;\n" +
			"import static java.lang.annotation.RetentionPolicy.RUNTIME;\n" +
			"@Retention(RUNTIME) @interface MyAnnotation {\n" +
			"    public int[] values();\n" +
			"}\n" +
			"public class X {\n" +
			"\n" +
			"		private static final int[] myValues = { 1, 2, 3 };\n" +
			"       @MyAnnotation(values=myValues) \n" +
			"       public void dothetrick(){} \n" +
			"\n" +
			"        public static void main(String[] args)throws Exception {\n" +
			"                MyAnnotation sluck = X.class.getMethod(\"dothetrick\", new Class[0]).getAnnotation(MyAnnotation.class);\n" +
			"                System.out.println(sluck.values().length);\n" +
			"        }\n" +
			"}",
        },
        "----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	@MyAnnotation(values=myValues) \n" +
		"	                     ^^^^^^^^\n" +
		"The value for annotation attribute MyAnnotation.values must be an array initializer\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141931
public void test214() {
	Map customOptions = getCompilerOptions();
	customOptions.put(
			CompilerOptions.OPTION_ReportMissingOverrideAnnotation,
			CompilerOptions.ERROR);
	customOptions.put(
			CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation,
			CompilerOptions.DISABLED);

	String expectedOutput = new CompilerOptions(getCompilerOptions()).sourceLevel < ClassFileConstants.JDK1_6
		?	"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	void foo();\n" +
			"	     ^^^^^\n" +
			"The method foo() of type I must override a superclass method\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 8)\n" +
			"	public void foo() {}\n" +
			"	            ^^^^^\n" +
			"The method foo() of type X must override a superclass method\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 13)\n" +
			"	void foo();\n" +
			"	     ^^^^^\n" +
			"The method foo() of type J must override a superclass method\n" +
			"----------\n"
		:	"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	void foo();\n" +
			"	     ^^^^^\n" +
			"The method foo() of type I must override or implement a supertype method\n" +
			"----------\n";
    this.runNegativeTest(
    	true,
        new String[] {
            "X.java",
			"interface I {\n" +
			"  @Override\n" +
			"  void foo();\n" +
			"  void bar();\n" +
			"}\n" +
			"public class X implements I {\n" +
			"  @Override\n" +
			"  public void foo() {}\n" +
			"  public void bar() {}\n" +
			"}\n" +
			"interface J extends I {\n" +
			"	@Override\n" +
			"	void foo();\n" +
			"}\n",
        },
        null,
        customOptions,
        expectedOutput,
        JavacTestOptions.SKIP);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=141931
// variant
public void test215() {
	String sources[] = new String[] {
		"I.java",
		"public interface I {\n" +
		"  void foo();\n" +
		"}\n",
		"X.java",
		"abstract class X implements I {\n" +
		"}\n",
		"Y.java",
		"class Y extends X {\n" +
		"  @Override\n" +
		"  public void foo() {}\n" +
		"}\n"};
	if (new CompilerOptions(getCompilerOptions()).sourceLevel < ClassFileConstants.JDK1_6) {
		this.runNegativeTest(sources,
			"----------\n" +
			"1. ERROR in Y.java (at line 3)\n" +
			"	public void foo() {}\n" +
			"	            ^^^^^\n" +
			"The method foo() of type Y must override a superclass method\n" +
			"----------\n");
	} else {
		this.runConformTest(sources,
			"");
	}
}
// extending java.lang.annotation.Annotation
public void test216() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"import java.lang.annotation.Annotation;\n" +
			"public class X {\n" +
			"  void bar(MyConstructor constructor, Class<Ann> ann) {\n" +
			"    constructor.getAnnotation(ann).message();\n" +
			"  }\n" +
			"}\n" +
			"@interface Ann {\n" +
			"  String message();\n" +
			"}\n" +
			"class MyConstructor<V> {\n" +
			"  public <T extends Annotation> T getAnnotation(Class<T> c) { return null; }\n" +
			"}\n",
        },
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	void bar(MyConstructor constructor, Class<Ann> ann) {\n" +
		"	         ^^^^^^^^^^^^^\n" +
		"MyConstructor is a raw type. References to generic type MyConstructor<V> should be parameterized\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	constructor.getAnnotation(ann).message();\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: The method getAnnotation(Class) belongs to the raw type MyConstructor. References to generic type MyConstructor<V> should be parameterized\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	constructor.getAnnotation(ann).message();\n" +
		"	                               ^^^^^^^\n" +
		"The method message() is undefined for the type Annotation\n" +
		"----------\n");
}
// extending java.lang.annotation.Annotation
public void test217() {
	Map customOptions = getCompilerOptions();
	customOptions.put(
			CompilerOptions.OPTION_ReportMissingOverrideAnnotation,
			CompilerOptions.ERROR);
	customOptions.put(
			CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation,
			CompilerOptions.DISABLED);
	String expectedOutput =
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	void bar(MyConstructor constructor, Class<Ann> ann) {\n" +
		"	         ^^^^^^^^^^^^^\n" +
		"MyConstructor is a raw type. References to generic type MyConstructor<V> should be parameterized\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	constructor.getAnnotation(ann).message();\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: The method getAnnotation(Class) belongs to the raw type MyConstructor. References to generic type MyConstructor<V> should be parameterized\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	constructor.getAnnotation(ann).message();\n" +
		"	                               ^^^^^^^\n" +
		"The method message() is undefined for the type Annotation\n" +
		"----------\n";
    this.runNegativeTest(
    	true,
        new String[] {
            "X.java",
			"import java.lang.annotation.Annotation;\n" +
			"public class X {\n" +
			"  void bar(MyConstructor constructor, Class<Ann> ann) {\n" +
			"    constructor.getAnnotation(ann).message();\n" +
			"  }\n" +
			"}\n" +
			"@interface Ann {\n" +
			"  String message();\n" +
			"}\n" +
			"interface Z {\n" +
			"  <T extends Annotation> T getAnnotation(Class<T> c);\n" +
			"}\n" +
			"class MyAccessibleObject implements Z {\n" +
			"  public <T extends Annotation> T getAnnotation(Class<T> c) { return null; }\n" +
			"}\n" +
			"class MyConstructor<V> {\n" +
			"  public <T extends Annotation> T getAnnotation(Class<T> c) { return null; }\n" +
			"}\n",
        },
        null,
        customOptions,
        expectedOutput,
        JavacTestOptions.SKIP);
}
// extending java.lang.annotation.Annotation
public void test218() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"import java.lang.annotation.Annotation;\n" +
			"import java.lang.reflect.Constructor;\n" +
			"public class X {\n" +
			"  void bar(Constructor constructor, Class<Ann> ann) {\n" +
			"    constructor.getAnnotation(ann).message();\n" +
			"  }\n" +
			"}\n" +
			"@interface Ann {\n" +
			"  String message();\n" +
			"}\n",
        },
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	void bar(Constructor constructor, Class<Ann> ann) {\n" +
		"	         ^^^^^^^^^^^\n" +
		"Constructor is a raw type. References to generic type Constructor<T> should be parameterized\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 5)\n" +
		"	constructor.getAnnotation(ann).message();\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Type safety: The method getAnnotation(Class) belongs to the raw type Constructor. References to generic type Constructor<T> should be parameterized\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 5)\n" +
		"	constructor.getAnnotation(ann).message();\n" +
		"	                               ^^^^^^^\n" +
		"The method message() is undefined for the type Annotation\n" +
		"----------\n",
		JavacTestOptions.JavacHasABug.JavacBug6400189);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=167217
public void test219() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"        @MyB1(MyA2.XX)\n" +
			"        public void foo(){}\n" +
			"}",
			"MyA1.java",
			"import static java.lang.annotation.ElementType.TYPE;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target( {\n" +
			"	TYPE\n" +
			"})\n" +
			"public @interface MyA1 {\n" +
			"}",
			"MyA2.java",
			"import static java.lang.annotation.ElementType.TYPE;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target( {\n" +
			"	TYPE\n" +
			"})\n" +
			"public @interface MyA2 {\n" +
			"        public static final MyA1 XX = null;\n" +
			"}",
			"MyB1.java",
			"import static java.lang.annotation.ElementType.*;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target( {\n" +
			"	TYPE, METHOD\n" +
			"})\n" +
			"public @interface MyB1 {\n" +
			"        MyA1 value();\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	@MyB1(MyA2.XX)\n" +
		"	      ^^^^^^^\n" +
		"The value for annotation attribute MyB1.value must be some @MyA1 annotation \n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=167217
public void test220() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"        @MyB1({MyA2.XX})\n" +
			"        public void foo(){}\n" +
			"}",
			"MyA1.java",
			"import static java.lang.annotation.ElementType.TYPE;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target( {\n" +
			"	TYPE\n" +
			"})\n" +
			"public @interface MyA1 {\n" +
			"}",
			"MyA2.java",
			"import static java.lang.annotation.ElementType.TYPE;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target( {\n" +
			"	TYPE\n" +
			"})\n" +
			"public @interface MyA2 {\n" +
			"        public static final MyA1 XX = null;\n" +
			"}",
			"MyB1.java",
			"import static java.lang.annotation.ElementType.*;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target( {\n" +
			"	TYPE, METHOD\n" +
			"})\n" +
			"public @interface MyB1 {\n" +
			"        MyA1[] value();\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	@MyB1({MyA2.XX})\n" +
		"	       ^^^^^^^\n" +
		"The value for annotation attribute MyB1.value must be some @MyA1 annotation \n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=167217
public void test221() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"        @MyB1(null)\n" +
			"        public void foo(){}\n" +
			"}",
			"MyA1.java",
			"import static java.lang.annotation.ElementType.TYPE;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target( {\n" +
			"	TYPE\n" +
			"})\n" +
			"public @interface MyA1 {\n" +
			"}",
			"MyA2.java",
			"import static java.lang.annotation.ElementType.TYPE;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target( {\n" +
			"	TYPE\n" +
			"})\n" +
			"public @interface MyA2 {\n" +
			"        public static final MyA1 XX = null;\n" +
			"}",
			"MyB1.java",
			"import static java.lang.annotation.ElementType.*;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target( {\n" +
			"	TYPE, METHOD\n" +
			"})\n" +
			"public @interface MyB1 {\n" +
			"        MyA1 value();\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	@MyB1(null)\n" +
		"	      ^^^^\n" +
		"The value for annotation attribute MyB1.value must be some @MyA1 annotation \n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=167217
public void test222() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"        @MyB1({null})\n" +
			"        public void foo(){}\n" +
			"}",
			"MyA1.java",
			"import static java.lang.annotation.ElementType.TYPE;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target( {\n" +
			"	TYPE\n" +
			"})\n" +
			"public @interface MyA1 {\n" +
			"}",
			"MyA2.java",
			"import static java.lang.annotation.ElementType.TYPE;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target( {\n" +
			"	TYPE\n" +
			"})\n" +
			"public @interface MyA2 {\n" +
			"        public static final MyA1 XX = null;\n" +
			"}",
			"MyB1.java",
			"import static java.lang.annotation.ElementType.*;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target( {\n" +
			"	TYPE, METHOD\n" +
			"})\n" +
			"public @interface MyB1 {\n" +
			"        MyA1[] value();\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	@MyB1({null})\n" +
		"	       ^^^^\n" +
		"The value for annotation attribute MyB1.value must be some @MyA1 annotation \n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=167217
public void test223() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"        @MyB1(@MyA1())\n" +
			"        public void foo(){}\n" +
			"}",
			"MyA1.java",
			"import static java.lang.annotation.ElementType.TYPE;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target( {\n" +
			"	TYPE\n" +
			"})\n" +
			"public @interface MyA1 {\n" +
			"}",
			"MyA2.java",
			"import static java.lang.annotation.ElementType.TYPE;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target( {\n" +
			"	TYPE\n" +
			"})\n" +
			"public @interface MyA2 {\n" +
			"        public static final MyA1 XX = null;\n" +
			"}",
			"MyB1.java",
			"import static java.lang.annotation.ElementType.*;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target( {\n" +
			"	TYPE, METHOD\n" +
			"})\n" +
			"public @interface MyB1 {\n" +
			"        MyA1 value();\n" +
			"}"
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=167217
public void test224() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"        @MyB1({@MyA1(), @MyA1})\n" +
			"        public void foo(){}\n" +
			"}",
			"MyA1.java",
			"import static java.lang.annotation.ElementType.TYPE;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target( {\n" +
			"	TYPE\n" +
			"})\n" +
			"public @interface MyA1 {\n" +
			"}",
			"MyA2.java",
			"import static java.lang.annotation.ElementType.TYPE;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target( {\n" +
			"	TYPE\n" +
			"})\n" +
			"public @interface MyA2 {\n" +
			"        public static final MyA1 XX = null;\n" +
			"}",
			"MyB1.java",
			"import static java.lang.annotation.ElementType.*;\n" +
			"import java.lang.annotation.Target;\n" +
			"\n" +
			"@Target( {\n" +
			"	TYPE, METHOD\n" +
			"})\n" +
			"public @interface MyB1 {\n" +
			"        MyA1[] value();\n" +
			"}"
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=XXXXX
public void test225() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n"+
			"  public void myMethod() {\n"+
			"    @MyAnnot1()\n"+
			"  }\n"+
			"}\n"+
			"@interface MyAnnot1 {\n"+
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	@MyAnnot1()\n" +
		"	          ^\n" +
		"Syntax error, insert \"enum Identifier\" to complete EnumHeader\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	@MyAnnot1()\n" +
		"	          ^\n" +
		"Syntax error, insert \"EnumBody\" to complete BlockStatements\n" +
		"----------\n",
		null,
		true,
		null /* no custom options */,
		false /* do not generate output */,
		false /* do not show category */,
		false /* do not show warning token */,
		false  /* do not skip javac for this peculiar test */,
		true  /* do not perform statements recovery */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179477 - variation
public void test226() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public @interface Annot {\n" +
			"        float[] value();\n" +
			"        Class<X>[] classe();\n" +
			"    }\n" +
			"    @Annot(value={x}, classe={Zork.class,zork})\n" +
			"    class Inner {\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	@Annot(value={x}, classe={Zork.class,zork})\n" +
		"	              ^\n" +
		"x cannot be resolved to a variable\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	@Annot(value={x}, classe={Zork.class,zork})\n" +
		"	                          ^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 6)\n" +
		"	@Annot(value={x}, classe={Zork.class,zork})\n" +
		"	                          ^^^^^^^^^^\n" +
		"Class<Zork> cannot be resolved to a type\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 6)\n" +
		"	@Annot(value={x}, classe={Zork.class,zork})\n" +
		"	                                     ^^^^\n" +
		"zork cannot be resolved to a variable\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 6)\n" +
		"	@Annot(value={x}, classe={Zork.class,zork})\n" +
		"	                                     ^^^^\n" +
		"The value for annotation attribute X.Annot.classe must be a class literal\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=186822
public void test227() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public @interface X<T> {}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	public @interface X<T> {}\n" +
		"	                    ^\n" +
		"Syntax error, annotation declaration cannot have type parameters\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127533
public void test228() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"	@SuppressWarnings(\"unchecked\") //unused\n" +
				"	void doNoEvil(){\n" +
				"	}\n" +
				"}\n",
				"Y.java",
				"public class Y {\n" +
				"	Zork z;\n" +
				"}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	@SuppressWarnings(\"unchecked\") //unused\n" +
		"	                  ^^^^^^^^^^^\n" +
		"Unnecessary @SuppressWarnings(\"unchecked\")\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in Y.java (at line 2)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127533
public void test229() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"	@SuppressWarnings({\"unchecked\",\"all\"})\n" +
				"	void doNoEvil(){\n" +
				"	}\n" +
				"	Zork z;\n" +
				"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127533 - variation
public void test230() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	this.runNegativeTest(
		true,
		new String[] {
				"X.java",
				"public class X {\n" +
				"	@SuppressWarnings({\"zork\", \"unused\" })\n" +
				"	void foo() {}\n" +
				"}\n" +
				"@SuppressWarnings({\"all\"})\n" +
				"class X2 {\n" +
				"	@SuppressWarnings({\"zork\", \"unused\" })\n" +
				"	void foo() {}\n" +
				"}\n",
		},
		null, options,
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	@SuppressWarnings({\"zork\", \"unused\" })\n" +
		"	                   ^^^^^^\n" +
		"Unsupported @SuppressWarnings(\"zork\")\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	@SuppressWarnings({\"zork\", \"unused\" })\n" +
		"	                           ^^^^^^^^\n" +
		"Unnecessary @SuppressWarnings(\"unused\")\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 7)\n" +
		"	@SuppressWarnings({\"zork\", \"unused\" })\n" +
		"	                           ^^^^^^^^\n" +
		"Unnecessary @SuppressWarnings(\"unused\")\n" +
		"----------\n",
		null, null, JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127533 - variation
public void test231() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	this.runNegativeTest(
		true,
		new String[] {
				"X.java",
				"public class X {\n" +
				"	@SuppressWarnings({\"zork\", \"unused\",\"all\"})\n" +
				"	void foo() {}\n" +
				"}\n" +
				"\n" +
				"@SuppressWarnings({\"all\"})\n" +
				"class X2 {\n" +
				"	@SuppressWarnings(\"unused\")\n" +
				"	void foo() {}\n" +
				"	Object z;\n" +
				"}\n",
		},
		null, options,
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	@SuppressWarnings({\"zork\", \"unused\",\"all\"})\n" +
		"	                           ^^^^^^^^\n" +
		"Unnecessary @SuppressWarnings(\"unused\")\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	@SuppressWarnings(\"unused\")\n" +
		"	                  ^^^^^^^^\n" +
		"Unnecessary @SuppressWarnings(\"unused\")\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127533 - variation
public void test232() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	this.runNegativeTest(
		true,
		new String[] {
				"X.java",
				"public class X {\n" +
				"    @SuppressWarnings({\"finally\",\"finally\"})\n" +
				"    public int test(int p) {\n" +
				"    	try {\n" +
				"		return 1;\n" +
				"	} finally {\n" +
				"		return 2;\n" +
				"	}\n" +
				"    }\n" +
				"}\n" +
				"class Y {}",
		},
		null, options,
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	@SuppressWarnings({\"finally\",\"finally\"})\n" +
		"	                             ^^^^^^^^^\n" +
		"Unnecessary @SuppressWarnings(\"finally\")\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127533 - variation
public void test233() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"    @SuppressWarnings({\"finally\",\"finally\"})\n" +
				"    public int test(int p) {\n" +
				"    	try {\n" +
				"		return Zork;\n" +
				"	} finally {\n" +
				"		return 2;\n" +
				"	}\n" +
				"    }\n" +
				"}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	@SuppressWarnings({\"finally\",\"finally\"})\n" +
		"	                             ^^^^^^^^^\n" +
		"Unnecessary @SuppressWarnings(\"finally\")\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	return Zork;\n" +
		"	       ^^^^\n" +
		"Zork cannot be resolved to a variable\n" +
		"----------\n");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127533 - variation
public void test234() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"    @SuppressWarnings(\"finally\")\n" + // unused but no complaint since an error is nested (can't tell for sure)
				"    public int test(int p) {\n" +
				"		return Zork;\n" +
				"    }\n" +
				"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	return Zork;\n" +
		"	       ^^^^\n" +
		"Zork cannot be resolved to a variable\n" +
		"----------\n");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=207758
public void test235() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"import java.util.List;\n" +
				"public class X {\n" +
				"        void foo() {\n" +
				"                ArrayList al = null;\n" +
				"                @SuppressWarnings(\"unchecked\")\n" +
				"                List<String> ls = al;\n" +
				"        }\n" +
				"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	ArrayList al = null;\n" +
		"	^^^^^^^^^\n" +
		"ArrayList cannot be resolved to a type\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=207758 - variation
public void test236() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"import java.util.List;\n" +
				"public class X {\n" +
				"	void foo() {\n" +
				"		@SuppressWarnings(\"unchecked\")\n" +
				"		List<String> ls = bar();\n" +
				"	}\n" +
				"	ArrayList bar() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	List<String> ls = bar();\n" +
		"	                  ^^^\n" +
		"The method bar() from the type X refers to the missing type ArrayList\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	ArrayList bar() {\n" +
		"	^^^^^^^^^\n" +
		"ArrayList cannot be resolved to a type\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=207758 - variation
public void test237() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"import java.util.List;\n" +
				"public class X<B extends ArrayList> {\n" +
				"	B get() { return null; }\n" +
				"	void foo() {\n" +
				"		@SuppressWarnings(\"unchecked\")\n" +
				"		List<String> ls = get();\n" +
				"	}\n" +
				"}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public class X<B extends ArrayList> {\n" +
		"	                         ^^^^^^^^^\n" +
		"ArrayList cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	List<String> ls = get();\n" +
		"	                  ^^^^^\n" +
		"Type mismatch: cannot convert from B to List<String>\n" +
		"----------\n");
}
public void test238() {
	// check that if promoted to ERROR, unhandled warning token shouldn't be suppressed by @SuppressWarnings("all")
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnhandledWarningToken, CompilerOptions.ERROR);
	this.runNegativeTest(
		true,
		new String[] {
				"X.java",
				"public class X {\n" +
				"	@SuppressWarnings({\"zork\",\"all\"})\n" +
				"	void foo() {}\n" +
				"}\n",
		},
		null, options,
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	@SuppressWarnings({\"zork\",\"all\"})\n" +
		"	                   ^^^^^^\n" +
		"Unsupported @SuppressWarnings(\"zork\")\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=77918
public void test239() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantSuperinterface, CompilerOptions.WARNING);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"class X implements I {}\n" +
				"@SuppressWarnings(\"unused\")\n" +
				"class Y extends X implements I {\n" +
				"	Zork z;\n" +
				"}\n" +
				"class Z extends X implements I {}\n" +
				"interface I {}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 6)\n" +
		"	class Z extends X implements I {}\n" +
		"	                             ^\n" +
		"Redundant superinterface I for the type Z, already defined by X\n" +
		"----------\n",
		null,
		false,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=207411
public void test240() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"@Deprecated @Zork\n" +
				"public class X {\n" +
				"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	@Deprecated @Zork\n" +
		"	             ^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=210213
public void test241() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"\n" +
				"	@SuppressWarnings(\"unchecked\")\n" +
				"	public static <T> T asClassUnchecked(Object object, T requiredClassObject) {\n" +
				"		return (T) object;\n" +
				"	}\n" +
				"	public static void main(String... args) {\n" +
				"		try {\n" +
				"			X[] xs = X.asClassUnchecked(\"abc\", (X[])null);\n" +
				"			System.out.println(xs.length);\n" +
				"		} catch(ClassCastException e) {\n" +
				"			System.out.println(\"SUCCESS\");\n" +
				"		}\n" +
				"	}\n" +
				"}\n",
		},
		"SUCCESS",
		null,
		false,
		null,
		options,
		null);
}
///https://bugs.eclipse.org/bugs/show_bug.cgi?id=210422 - variation
public void test242() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"import java.io.Serializable;\n" +
				"public final class X implements Serializable {\n" +
				"    class SMember extends String {}  \n" +
				"    @Annot(value = new SMember())\n" +
				"     void bar() {}\n" +
				"    @Annot(value = \n" +
				"            new X(){\n" +
				"                    ZorkAnonymous1 z;\n" +
				"                    void foo() {\n" +
				"                            this.bar();\n" +
				"                            Zork2 z;\n" +
				"                    }\n" +
				"            })\n" +
				"	void foo() {}\n" +
				"}\n" +
				"@interface Annot {\n" +
				"        String value();\n" +
				"}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	public final class X implements Serializable {\n" +
		"	                   ^\n" +
		"The serializable class X does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	class SMember extends String {}  \n" +
		"	                      ^^^^^^\n" +
		"The type SMember cannot subclass the final class String\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 4)\n" +
		"	@Annot(value = new SMember())\n" +
		"	               ^^^^^^^^^^^^^\n" +
		"Type mismatch: cannot convert from X.SMember to String\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 7)\n" +
		"	new X(){\n" +
		"	    ^\n" +
		"An anonymous class cannot subclass the final class X\n" +
		"----------\n" +
		"5. ERROR in X.java (at line 8)\n" +
		"	ZorkAnonymous1 z;\n" +
		"	^^^^^^^^^^^^^^\n" +
		"ZorkAnonymous1 cannot be resolved to a type\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=210213 - variation
public void test243() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"	\n" +
				"	@SuppressWarnings(\"unchecked\")\n" +
				"	void foo() {\n" +
				"		\n" +
				"	}\n" +
				"}	\n",
		},
		"",
		null,
		false,
		null,
		options,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=210213 - variation
public void test244() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.WARNING);
	this.runNegativeTest(
		true,
		new String[] {
				"X.java",
				"public class X {\n" +
				"	\n" +
				"	@SuppressWarnings(\"unchecked\")\n" +
				"	void foo() {\n" +
				"		\n" +
				"	}\n" +
				"}	\n",
		},
		null, options,
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	@SuppressWarnings(\"unchecked\")\n" +
		"	                  ^^^^^^^^^^^\n" +
		"Unnecessary @SuppressWarnings(\"unchecked\")\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=210213 - variation
public void test245() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.WARNING);
	this.runNegativeTest(
		true,
		new String[] {
				"X.java",
				"public class X {\n" +
				"	\n" +
				"	@SuppressWarnings({\"unchecked\",\"unused\"})\n" +
				"	void foo() {\n" +
				"		\n" +
				"	}\n" +
				"}	\n",
		},
		null, options,
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	@SuppressWarnings({\"unchecked\",\"unused\"})\n" +
		"	                               ^^^^^^^^\n" +
		"Unnecessary @SuppressWarnings(\"unused\")\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=210213 - variation
public void test246() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"	\n" +
				"	@SuppressWarnings(\"all\")\n" +
				"	void foo() {\n" +
				"		\n" +
				"	}\n" +
				"}	\n",
		},
		"",
		null,
		false,
		null,
		options,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=211609
public void test247() {
	if (this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	// only enable in 1.6 mode
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	this.runConformTest(
			new String[] {
				"TestAnnotation.java",
				"public @interface TestAnnotation {\n" +
				"	Class targetItem() default void.class;\n" +
				"}"
			},
			"");
	this.runConformTest(
		new String[] {
				"X.java",
				"public class X {\n" +
				"	@TestAnnotation\n" +
				"	private String foo;\n" +
				"}",
		},
		"",
		null,
		false,
		null,
		options,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=211609
public void test248() {
	this.runNegativeTest(
			new String[] {
				"TestAnnotation.java",
				"public @interface TestAnnotation {\n" +
				"	String targetItem() default void.class;\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in TestAnnotation.java (at line 2)\n" +
			"	String targetItem() default void.class;\n" +
			"	                            ^^^^^^^^^^\n" +
			"Type mismatch: cannot convert from Class<Void> to String\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191090
public void test249() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", //-----------------------------------------------------------------------
			"@Zork\n" +
			"public class X {}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 1)\n" +
		"	@Zork\n" +
		"	 ^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n",
		null,
		true, // flush output
		null,
		true, // generate output
		false,
		false);
	String expectedOutput = "public class X {";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191090
public void test250() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", //-----------------------------------------------------------------------
			"@Deprecated\n" +
			"@Zork\n" +
			"@Annot(1)\n" +
			"public class X {}",
			"Annot.java",
			"import java.lang.annotation.Retention;\n" +
			"import static java.lang.annotation.RetentionPolicy.*;\n" +
			"@Retention(RUNTIME)\n" +
			"@interface Annot {\n" +
			"	int value() default -1;\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	@Zork\n" +
		"	 ^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n",
		null,
		true, // flush output
		null,
		true, // generate output
		false,
		false);
	String expectedOutput =
		"@java.lang.Deprecated\n" +
		"@Annot(value=(int) 1)\n" +
		"public class X {";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191090
public void test251() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", //-----------------------------------------------------------------------
			"@Deprecated\n" +
			"@Zork\n" +
			"@Annot(1)\n" +
			"public class X {}",
			"Annot.java",
			"import java.lang.annotation.Retention;\n" +
			"import static java.lang.annotation.RetentionPolicy.*;\n" +
			"@Retention(CLASS)\n" +
			"@interface Annot {\n" +
			"	int value() default -1;\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	@Zork\n" +
		"	 ^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n",
		null,
		true, // flush output
		null,
		true, // generate output
		false,
		false);
	String expectedOutput =
		"@Annot(value=(int) 1)\n" +
		"@java.lang.Deprecated\n" +
		"public class X {";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191090
public void test252() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", //-----------------------------------------------------------------------
			"public class X {\n" +
			"	public void foo(@Deprecated @Zork @Annot(2) int i) {}\n" +
			"}",
			"Annot.java",
			"import java.lang.annotation.Retention;\n" +
			"import static java.lang.annotation.RetentionPolicy.*;\n" +
			"@Retention(CLASS)\n" +
			"@interface Annot {\n" +
			"	int value() default -1;\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public void foo(@Deprecated @Zork @Annot(2) int i) {}\n" +
		"	                             ^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n",
		null,
		true, // flush output
		null,
		true, // generate output
		false,
		false);
	String expectedOutput =
		"    RuntimeVisibleParameterAnnotations: \n" +
		"      Number of annotations for parameter 0: 1\n" +
		"        #22 @java.lang.Deprecated(\n" +
		"        )\n" +
		"    RuntimeInvisibleParameterAnnotations: \n" +
		"      Number of annotations for parameter 0: 2\n" +
		"        #17 @Zork(\n" +
		"        )\n" +
		"        #18 @Annot(\n" +
		"          #19 value=(int) 2 (constant type)\n" +
		"        )\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191090
public void test253() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", //-----------------------------------------------------------------------
			"public class X {\n" +
			"	public void foo(@Deprecated @Zork @Annot(2) int i) {}\n" +
			"}",
			"Annot.java",
			"import java.lang.annotation.Retention;\n" +
			"import static java.lang.annotation.RetentionPolicy.*;\n" +
			"@Retention(RUNTIME)\n" +
			"@interface Annot {\n" +
			"	int value() default -1;\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public void foo(@Deprecated @Zork @Annot(2) int i) {}\n" +
		"	                             ^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n",
		null,
		true, // flush output
		null,
		true, // generate output
		false,
		false);
	String expectedOutput =
		"    RuntimeVisibleParameterAnnotations: \n" +
		"      Number of annotations for parameter 0: 2\n" +
		"        #19 @java.lang.Deprecated(\n" +
		"        )\n" +
		"        #20 @Annot(\n" +
		"          #21 value=(int) 2 (constant type)\n" +
		"        )\n" +
		"    RuntimeInvisibleParameterAnnotations: \n" +
		"      Number of annotations for parameter 0: 1\n" +
		"        #17 @Zork(\n" +
		"        )\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191090
public void test254() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", //-----------------------------------------------------------------------
			"public class X {\n" +
			"	public void foo(@Deprecated int j, @Zork @Annot(3) int i) {}\n" +
			"}",
			"Annot.java",
			"import java.lang.annotation.Retention;\n" +
			"import static java.lang.annotation.RetentionPolicy.*;\n" +
			"@Retention(RUNTIME)\n" +
			"@interface Annot {\n" +
			"	int value() default -1;\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public void foo(@Deprecated int j, @Zork @Annot(3) int i) {}\n" +
		"	                                    ^^^^\n" +
		"Zork cannot be resolved to a type\n" +
		"----------\n",
		null,
		true, // flush output
		null,
		true, // generate output
		false,
		false);
	String expectedOutput =
		"    RuntimeVisibleParameterAnnotations: \n" +
		"      Number of annotations for parameter 0: 1\n" +
		"        #19 @java.lang.Deprecated(\n" +
		"        )\n" +
		"      Number of annotations for parameter 1: 1\n" +
		"        #20 @Annot(\n" +
		"          #21 value=(int) 3 (constant type)\n" +
		"        )\n" +
		"    RuntimeInvisibleParameterAnnotations: \n" +
		"      Number of annotations for parameter 0: 0\n" +
		"      Number of annotations for parameter 1: 1\n" +
		"        #17 @Zork(\n" +
		"        )\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191090
public void test255() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", //-----------------------------------------------------------------------
			"public class X {\n" +
			"	public void foo(@Deprecated int j, @Annot(\"\") @Deprecated int i) {}\n" +
			"}",
			"Annot.java",
			"import java.lang.annotation.Retention;\n" +
			"import static java.lang.annotation.RetentionPolicy.*;\n" +
			"@Retention(RUNTIME)\n" +
			"@interface Annot {\n" +
			"	int value() default -1;\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public void foo(@Deprecated int j, @Annot(\"\") @Deprecated int i) {}\n" +
		"	                                          ^^\n" +
		"Type mismatch: cannot convert from String to int\n" +
		"----------\n",
		null,
		true, // flush output
		null,
		true, // generate output
		false,
		false);
	String expectedOutput =
		"    RuntimeVisibleParameterAnnotations: \n" +
		"      Number of annotations for parameter 0: 1\n" +
		"        #17 @java.lang.Deprecated(\n" +
		"        )\n" +
		"      Number of annotations for parameter 1: 1\n" +
		"        #17 @java.lang.Deprecated(\n" +
		"        )\n";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191090
public void test256() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", //-----------------------------------------------------------------------
			"public class X {\n" +
			"	public void foo(@Deprecated int j, @Annot(\"\") @Deprecated int i) {}\n" +
			"}",
			"Annot.java",
			"import java.lang.annotation.Retention;\n" +
			"import static java.lang.annotation.RetentionPolicy.*;\n" +
			"@Retention(CLASS)\n" +
			"@interface Annot {\n" +
			"	int value() default -1;\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	public void foo(@Deprecated int j, @Annot(\"\") @Deprecated int i) {}\n" +
		"	                                          ^^\n" +
		"Type mismatch: cannot convert from String to int\n" +
		"----------\n",
		null,
		true, // flush output
		null,
		true, // generate output
		false,
		false);
	String expectedOutput =
		"    RuntimeVisibleParameterAnnotations: \n" +
		"      Number of annotations for parameter 0: 1\n" +
		"        #20 @java.lang.Deprecated(\n" +
		"        )\n" +
		"      Number of annotations for parameter 1: 1\n" +
		"        #20 @java.lang.Deprecated(\n" +
		"        )";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=216570
public void test257() {
	if (new CompilerOptions(getCompilerOptions()).sourceLevel < ClassFileConstants.JDK1_6) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    static interface IFoo {\n" +
					"        public boolean eval(String s);\n" +
					"    }\n" +
					"    static class Foo implements IFoo {\n" +
					"        @Override\n" +
					"        public boolean eval(String s) {\n" +
					"            return true;\n" +
					"        }\n" +
					"    }\n" +
					"}\n"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	public boolean eval(String s) {\n" +
				"	               ^^^^^^^^^^^^^^\n" +
				"The method eval(String) of type X.Foo must override a superclass method\n" +
				"----------\n");
		return;
	}
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    static interface IFoo {\n" +
				"        public boolean eval(String s);\n" +
				"    }\n" +
				"    static class Foo implements IFoo {\n" +
				"        @Override\n" +
				"        public boolean eval(String s) {\n" +
				"            return true;\n" +
				"        }\n" +
				"    }\n" +
				"}\n"
			},
			"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=167262
public void test258() {
	String expectedOutput = new CompilerOptions(getCompilerOptions()).sourceLevel < ClassFileConstants.JDK1_6
	?	"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	void bar();//3\n" +
		"	     ^^^^^\n" +
		"The method bar() of type Bar must override a superclass method\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 13)\n" +
		"	public void bar() {}//4\n" +
		"	            ^^^^^\n" +
		"The method bar() of type BarImpl must override a superclass method\n" +
		"----------\n"
	:	"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	void bar();//3\n" +
		"	     ^^^^^\n" +
		"The method bar() of type Bar must override or implement a supertype method\n" +
		"----------\n";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"interface Foo {\n" +
			"	@Override\n" +
			"	String toString();//1\n" +
			"}\n" +
			"interface Bar extends Foo {\n" +
			"	@Override\n" +
			"	String toString();//2\n" +
			"	@Override\n" +
			"	void bar();//3\n" +
			"}\n" +
			"class BarImpl implements Bar {\n" +
			"	@Override\n" +
			"	public void bar() {}//4\n" +
			"}\n"
		},
		expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239273
public void test259() {
	this.runConformTest(
		new String[] {
			"Jpf.java",
			"public class Jpf {\n" + 
			"	@interface Action {\n" + 
			"		Forward[] forwards();\n" + 
			"	}\n" + 
			"	@interface Forward {\n" + 
			"		String name();\n" + 
			"		String path();\n" + 
			"		ActionOutput[] actionOutputs();\n" + 
			"	}\n" + 
			"	@interface ActionOutput {\n" + 
			"		String name();\n" + 
			"		Class type();\n" + 
			"	}\n" + 
			"	@Jpf.Action( \n" + 
			"			forwards = { \n" + 
			"					@Jpf.Forward(\n" + 
			"							name = \"success\", \n" + 
			"							path = \"results.jsp\", \n" + 
			"							actionOutputs = { \n" + 
			"									@Jpf.ActionOutput(\n" + 
			"											name = \"mybeanmethodResult\", \n" + 
			"											type = java.lang.String[].class) }) })\n" + 
			"	public Forward mybeanmethod() {\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"}\n"
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=245435
public void test260() {
	this.runConformTest(
		new String[] {
			"X.java",
			"@X.StringAnnotation(X.CONSTANT_EXPRESSION)\n" + 
			"public class X {\n" + 
			"  public @interface StringAnnotation {\n" + 
			"    String value();\n" + 
			"  }\n" + 
			"  public final static String CONSTANT = \"Constant\";\n" + 
			"  public final static String CONSTANT_EXPRESSION = CONSTANT + \"Expression\";\n" + 
			"}\n"
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239273
public void test261() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	this.runConformTest(
		new String[] {
			"X.java",//=====================
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new Other().foo();\n" + 
			"	}\n" + 
			"}\n",
			"Annot.java",//=====================
			"public @interface Annot {\n" + 
			"	Class value();\n" + 
			"}\n",
			"Other.java",//=====================
			"public class Other {\n" + 
			"	@Annot(value = Other[].class)\n" + 
			"	void foo() {\n" + 
			"		System.out.println(\"SUCCESS\");\n" + 
			"	}\n" + 
			"}\n"
		},
		"SUCCESS",
		null,
		true,
		null,
		options,
		null);
	this.runConformTest(
			new String[] {
				"X.java",//=====================
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new Other().foo();\n" + 
				"	}\n" + 
				"}\n",
			},
			"SUCCESS",
			null,
			false,
			null,
			options,
			null);	
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239273 - variation
public void test262() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	this.runConformTest(
		new String[] {
			"X.java",//=====================
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new Other().foo();\n" + 
			"	}\n" + 
			"}\n",
			"Annot.java",//=====================
			"public @interface Annot {\n" + 
			"	String[] values();\n" + 
			"}\n",
			"Other.java",//=====================
			"public class Other {\n" + 
			"	@Annot(values = {\"foo\",\"bar\"})\n" + 
			"	void foo() {\n" + 
			"		System.out.println(\"SUCCESS\");\n" + 
			"	}\n" + 
			"}\n"
		},
		"SUCCESS",
		null,
		true,
		null,
		options,
		null);
	this.runConformTest(
			new String[] {
				"X.java",//=====================
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new Other().foo();\n" + 
				"	}\n" + 
				"}\n",
			},
			"SUCCESS",
			null,
			false,
			null,
			options,
			null);	
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239273 - variation
public void test263() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	this.runConformTest(
		new String[] {
			"X.java",//=====================
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new Other().foo();\n" + 
			"	}\n" + 
			"}\n",
			"Annot.java",//=====================
			"public @interface Annot {\n" + 
			"	String[] values();\n" + 
			"}\n",
			"Other.java",//=====================
			"public class Other {\n" + 
			"	@Annot(values = {\"foo\",\"bar\"})\n" + 
			"	void foo() {\n" + 
			"		System.out.println(\"SUCCESS\");\n" + 
			"	}\n" + 
			"}\n"
		},
		"SUCCESS",
		null,
		true,
		null,
		options,
		null);
	this.runConformTest(
			new String[] {
				"X.java",//=====================
				"public class X {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		new Other().foo();\n" + 
				"	}\n" + 
				"}\n",
			},
			"SUCCESS",
			null,
			false,
			null,
			options,
			null);	
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=256035
public void test264() {
	this.runConformTest(
		new String[] {
			"X.java",
			"@interface Anno {\n" + 
			"	String value();\n" + 
			"}\n" + 
			"\n" + 
			"@Anno(X.B)\n" + 
			"public class X {\n" + 
			"	public static final String A = \"a\";\n" + 
			"	public static final String B = A + \"b\";\n" + 
			"}\n"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=258906 
public void test265() {
	if (new CompilerOptions(getCompilerOptions()).complianceLevel < ClassFileConstants.JDK1_6) return;

	INameEnvironment nameEnvironment = new FileSystem(Util.getJavaClassLibs(), new String[] {}, null);
	IErrorHandlingPolicy errorHandlingPolicy = new IErrorHandlingPolicy() {
		public boolean proceedOnErrors() { return true; }
		public boolean stopOnFirstError() { return false; }
	};
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	CompilerOptions compilerOptions = new CompilerOptions(options);
	compilerOptions.performMethodsFullRecovery = false;
	compilerOptions.performStatementsRecovery = false;
	Requestor requestor = new Requestor(false, null /*no custom requestor*/, false, /* show category */ false /* show warning token*/);
	requestor.outputPath = "bin/";
	IProblemFactory problemFactory = new DefaultProblemFactory(Locale.getDefault());

	Compiler compiler = new Compiler(nameEnvironment, errorHandlingPolicy, compilerOptions, requestor, problemFactory);
	compiler.options.produceReferenceInfo = true;

	String code = "@javax.xml.bind.annotation.XmlSchema(namespace = \"test\")\npackage testpack;\n";
	ICompilationUnit source = new CompilationUnit(code.toCharArray(), "testpack/package-info.java", null);

	// don't call compile as would be normally expected since that wipes out the lookup environment
	// before we could query it. Use internal API resolve instead which can run a subset of the
	// compilation steps for us.

	compiler.resolve (source,
		true, // verifyMethods,
		true, // boolean analyzeCode,
		false // generateCode
	);
	char [][] compoundName = new char [][] { "testpack".toCharArray(), "package-info".toCharArray()};
	ReferenceBinding type = compiler.lookupEnvironment.getType(compoundName);
	AnnotationBinding[] annotations = null;
	if (type != null && type.isValidBinding()) {
		annotations = type.getAnnotations();
	}
	assertTrue ("Annotations missing on package-info interface", annotations != null && annotations.length == 1);
	assertEquals("Wrong annotation on package-info interface ", "@XmlSchema{ namespace = (String)\"test\"}", annotations[0].toString());
	nameEnvironment.cleanup();
	if (requestor.hasErrors)
		System.err.print(requestor.problemLog); // problem log empty if no problems
	compiler = null;
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=220311
public void test266() {
	this.runNegativeTest(
		new String[] {
			"p/package-info.java",
			"@Deprecated\n" + 
			"@Deprecated\n" + 
			"package p;"
		},
		"----------\n" + 
		"1. ERROR in p\\package-info.java (at line 1)\n" + 
		"	@Deprecated\n" + 
		"	^^^^^^^^^^^\n" + 
		"Duplicate annotation @Deprecated\n" + 
		"----------\n" + 
		"2. ERROR in p\\package-info.java (at line 2)\n" + 
		"	@Deprecated\n" + 
		"	^^^^^^^^^^^\n" + 
		"Duplicate annotation @Deprecated\n" + 
		"----------\n"
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=261323.
// Test to make sure that the use of a static import as an annotation value counts as a use
// (and consequently that there is no unused static import warning)
public void test267() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_SuppressWarnings, CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);

	runNegativeTest(
		true,
		new String[] {
				"com/SomeTest.java",
				"package com;\n" + 
				"import static com.SomeTest.UNCHECKED;\n" + 
				"@SuppressWarnings(UNCHECKED)\n" + 
				"public class SomeTest {\n" + 
				"    public static final String UNCHECKED = \"unchecked\";\n" + 
				"}\n"
		}, 
		null, customOptions,
		"----------\n" +
		"1. ERROR in com\\SomeTest.java (at line 3)\n" +
		"	@SuppressWarnings(UNCHECKED)\n" +
		"	                  ^^^^^^^^^\n" +
		"Unnecessary @SuppressWarnings(\"unchecked\")\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=262304
public void test268() {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"public class X {\n" + 
			"	protected enum E {\n" + 
			"		E1, E2\n" + 
			"	}\n" + 
			"	protected @interface Anno1 { E value(); }\n" + 
			"	protected @interface Anno2 { E value(); }\n" + 
			"	protected @interface Anno3 { E value(); }\n" + 
			"	@Anno1(true ? E.E1 : E.E2)\n" + 
			"	@Anno2(bar())\n" + 
			"	@Anno3(((E.E1)))\n" + 
			"	public void foo() {\n" + 
			"	}\n" + 
			"	public E bar() { return E.E1; }\n" + 
			"}\n", // =================
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 8)\n" + 
		"	@Anno1(true ? E.E1 : E.E2)\n" + 
		"	       ^^^^^^^^^^^^^^^^^^\n" + 
		"The value for annotation attribute X.Anno1.value must be an enum constant expression\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 9)\n" + 
		"	@Anno2(bar())\n" + 
		"	       ^^^^^\n" + 
		"The value for annotation attribute X.Anno2.value must be an enum constant expression\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=274917
public void test269() {
	Map customOptions = new Hashtable();
	String[] warnings = CompilerOptions.warningOptionNames();
	for (int i = 0, ceil = warnings.length; i < ceil; i++) {
		customOptions.put(warnings[i], CompilerOptions.WARNING);
	}
	this.runConformTest(
			true,
			new String[] {
					"X.java",
					"@interface X {}",
			},
			null,
			customOptions,
			"----------\n" + 
			"1. WARNING in X.java (at line 1)\n" + 
			"	@interface X {}\n" + 
			"	             ^^\n" + 
			"Empty block should be documented\n" + 
			"----------\n",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=287009
public void test270() {
	this.runNegativeTest(
		new String[] {
			"Test.java", 
			"public class Test<T> {\n" + 
			"	@interface Anno {\n" + 
			"		Anno value();\n" + 
			"	}\n" + 
			"}\n", 
		},
		"----------\n" +
		"1. ERROR in Test.java (at line 3)\n" +
		"	Anno value();\n" +
		"	^^^^\n" +
		"Cycle detected: the annotation type Test<T>.Anno cannot contain attributes of the annotation type itself\n" +
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=289576
public void test271() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"@interface A {}\n" + 
			"public class X {\n" + 
			"	@SuppressWarnings(\"unused\")\n" + 
			"	private void foo(@A Object o) {}\n" + 
			"}"
		},
	"");

	String expectedOutput =
		"  // Method descriptor #15 (Ljava/lang/Object;)V\n" + 
		"  // Stack: 0, Locals: 2\n" + 
		"  private void foo(@A java.lang.Object o);\n";

	checkDisassembledClassFile(OUTPUT_DIR + File.separator  +"X.class", "X", expectedOutput, ClassFileBytesDisassembler.DETAILED);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=289516
public void test272() throws Exception {
	if (this.complianceLevel != ClassFileConstants.JDK1_5) {
		return;
	}
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_4);
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	this.runConformTest(
		new String[] {
			"X.java",
			"@interface A {}\n" + 
			"public class X {\n" + 
			"	@SuppressWarnings(\"unused\")\n" + 
			"	private void foo(@A Object o) {}\n" + 
			"}"
		},
		"",
		null,
		true,
		null,
		options,
		null,
		true);

	String expectedOutput =
		"  // Method descriptor #15 (Ljava/lang/Object;)V\n" + 
		"  // Stack: 0, Locals: 2\n" + 
		"  private void foo(@A java.lang.Object o);\n";

	checkDisassembledClassFile(OUTPUT_DIR + File.separator  +"X.class", "X", expectedOutput, ClassFileBytesDisassembler.DETAILED);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=289576
public void test273() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"@interface A {}\n" + 
			"public class X {\n" + 
			"	@SuppressWarnings(\"unused\")\n" + 
			"	private X(@A Object o) {}\n" + 
			"}"
		},
		"");

	String expectedOutput =
		"  // Method descriptor #6 (Ljava/lang/Object;)V\n" + 
		"  // Stack: 1, Locals: 2\n" + 
		"  private X(@A java.lang.Object o);\n";

	checkDisassembledClassFile(OUTPUT_DIR + File.separator  +"X.class", "X", expectedOutput, ClassFileBytesDisassembler.DETAILED);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=163194
// To check Missing override annotation error when a method implements
// and also overrides a method in a superclass
public void test274a() {
	String testString [] = new String[] {
			"T.java",
			"public interface T {\n" +
			"        void m();\n" +
			"}\n" + 
			"abstract class A implements T {\n" +
			"}\n" +
			"class B extends A {\n" +
			"        public void m() {}\n" +
			"}\n"
			};
	Map customOptions = getCompilerOptions();
	customOptions.put(
			CompilerOptions.OPTION_ReportMissingOverrideAnnotation,
			CompilerOptions.ERROR);
	customOptions.put(
			CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation,
			CompilerOptions.ENABLED);
	if (new CompilerOptions(customOptions).sourceLevel >= ClassFileConstants.JDK1_6) {
		String expectedOutput =
				"----------\n" +
				"1. ERROR in T.java (at line 7)\n" +
				"	public void m() {}\n" +
				"	            ^^^\n" +
				"The method m() of type B should be tagged with @Override since it actually overrides a superinterface method\n" +
				"----------\n";
		this.runNegativeTest(
				true,
				testString,
				null, customOptions,
				expectedOutput, 
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	} else {
		this.runConformTest(
				true, testString,
				null,
				customOptions,
				null,
				null, null, 
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=163194
// To check Missing override annotation error when a method implements but
// doesn't overrides
public void test274b() {
	String testString [] = new String[] {
			"Over.java",
			"interface I {\n" +
			"        void m();\n" +
			"}\n" +
			"public class Over implements I {\n" +
			"        public void m() {}\n" +
			"}\n"
			};
	Map customOptions = getCompilerOptions();
	customOptions.put(
			CompilerOptions.OPTION_ReportMissingOverrideAnnotation,
			CompilerOptions.ERROR);
	customOptions.put(
			CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation,
			CompilerOptions.ENABLED);
	if (new CompilerOptions(customOptions).sourceLevel >= ClassFileConstants.JDK1_6) {
		String expectedOutput =
			"----------\n" +
			"1. ERROR in Over.java (at line 5)\n" +
			"	public void m() {}\n" +
			"	            ^^^\n" +
			"The method m() of type Over should be tagged with @Override since it actually overrides a superinterface method\n" +
			"----------\n";
		this.runNegativeTest(
				true,
				testString,
				null, customOptions,
				expectedOutput,
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	} else {
		this.runConformTest(
				true, testString,
				null,
				customOptions,
				null,
				null, null,
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=163194
// To check Missing override annotation error when a method simply overrides
public void test274c() {
	String testString [] = new String[] {
			"B.java",
			"interface A {\n" +
			"        void m();\n" +
			"}\n" +
			"public interface B extends A {\n" +
			"        void m();\n" +
			"}\n"
			};
	Map customOptions = getCompilerOptions();
	customOptions.put(
			CompilerOptions.OPTION_ReportMissingOverrideAnnotation,
			CompilerOptions.ERROR);
	customOptions.put(
			CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation,
			CompilerOptions.ENABLED);
	if (new CompilerOptions(customOptions).sourceLevel >= ClassFileConstants.JDK1_6) {
		String expectedOutput =
				"----------\n" +
				"1. ERROR in B.java (at line 5)\n" +
				"	void m();\n" +
				"	     ^^^\n" +
				"The method m() of type B should be tagged with @Override since it actually overrides a superinterface method\n" +
				"----------\n";
		this.runNegativeTest(
				true,
				testString,
				null, customOptions,
				expectedOutput,
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	} else {
		this.runConformTest(
				true, testString,
				null,
				customOptions,
				null,
				null, null,
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=163194
// To check missing override annotation if the method has a signature
// that is override-equivalent to that of any public method declared in Object.
public void test274d() {
	String testString [] = new String[] {
			"A.java",
			"public interface A {\n" +
			"        String toString();\n" +
			"}\n"
			};
	Map customOptions = getCompilerOptions();
	customOptions.put(
			CompilerOptions.OPTION_ReportMissingOverrideAnnotation,
			CompilerOptions.ERROR);
	customOptions.put(
			CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation,
			CompilerOptions.ENABLED);
	if (new CompilerOptions(customOptions).sourceLevel >= ClassFileConstants.JDK1_6) {
		String expectedOutput =
			"----------\n" +
			"1. ERROR in A.java (at line 2)\n" +
			"	String toString();\n" +
			"	       ^^^^^^^^^^\n" +
			"The method toString() of type A should be tagged with @Override since it actually overrides a superinterface method\n" +
			"----------\n";
		this.runNegativeTest(
				true,
				testString,
				null, customOptions,
				expectedOutput,
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	} else {
		this.runConformTest(
				true, testString,
				null,
				customOptions,
				null,
				null, null,
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=282770.
public void test275() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportDeadCodeInTrivialIfStatement, CompilerOptions.ENABLED);

	runConformTest(
		true,
		new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static final boolean DEBUG = false;\n" + 
				"//	@SuppressWarnings(\"unused\")\n" + 
				"	public void foo() {\n" + 
				"		if (DEBUG)\n" +
				"			System.out.println(\"true\");\n" +
				"		else\n" +
				"			System.out.println(\"false\");\n" +
				"		\n" +
				"	}\n" + 
				"}\n"
		},
		null,
		customOptions,
		"----------\n" + 
		"1. WARNING in X.java (at line 6)\n" + 
		"	System.out.println(\"true\");\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n",
		"",
		"",
		JavacTestOptions.SKIP);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=282770.
public void test276() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportDeadCodeInTrivialIfStatement, CompilerOptions.ENABLED);

	runConformTest(
		true,
		new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static final boolean DEBUG = false;\n" + 
				"	@SuppressWarnings(\"unused\")\n" + 
				"	public void foo() {\n" + 
				"		if (DEBUG)\n" +
				"			System.out.println(\"true\");\n" +
				"		else\n" +
				"			System.out.println(\"false\");\n" +
				"		\n" +
				"	}\n" + 
				"}\n"
		},
		null,
		customOptions,
		"",
		"",
		"",
		JavacTestOptions.SKIP);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=282770.
public void test277() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportDeadCodeInTrivialIfStatement, CompilerOptions.DISABLED);

	runConformTest(
		true,
		new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static final boolean DEBUG = false;\n" + 
				"	@SuppressWarnings(\"unused\")\n" + 
				"	public void foo() {\n" + 
				"		if (0 < 1)\n" +
				"			System.out.println(\"true\");\n" +
				"		else\n" +
				"			System.out.println(\"false\");\n" +
				"		\n" +
				"	}\n" + 
				"}\n"
		},
		null,
		customOptions,
		"",
		"",
		"",
		JavacTestOptions.SKIP);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=293777
// To verify that a misleading warning against @Override annotation is not
// issued in case the method signature has not been resolved properly.
public void test278() {
	String testString [] = new String[] {
			"A.java",
			"import javax.swing.JComponent;\n" +
			"public class A extends JComponent {\n" +
			"   @Override\n" +
			"	protected void paintComponent(Graphics g) {" +
			"   }\n" +
			"}\n"
			};
	String expectedOutput =
		"----------\n" +
		"1. WARNING in A.java (at line 2)\n" +
		"	public class A extends JComponent {\n" +
		"	             ^\n" +
		"The serializable class A does not declare a static final serialVersionUID field of type long\n" +
		"----------\n" +
		"2. ERROR in A.java (at line 4)\n" +
		"	protected void paintComponent(Graphics g) {   }\n" +
		"	                              ^^^^^^^^\n" +
		"Graphics cannot be resolved to a type\n" +
		"----------\n";
	this.runNegativeTest(
			testString,
			expectedOutput,
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=301683
public void test279() {
	String testString [] = new String[] {
			"A.java",
			"public class A {\n" +
			"    public @interface Inline {\n" +
			"        String value();\n" +
			"    }\n" +
			"    @Inline(\"foo\")\n" +
			"    public Zork test;\n" +
			"    public native void method();\n" +
			"}"
			};
	String expectedOutput =
		"----------\n" + 
		"1. ERROR in A.java (at line 6)\n" + 
		"	public Zork test;\n" + 
		"	       ^^^^\n" + 
		"Zork cannot be resolved to a type\n" +
		"----------\n";
	this.runNegativeTest(
			testString,
			expectedOutput,
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=295551
public void test280() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	String testFiles [] = new String[] {
			"A.java",
			"public class A {\n" +
			"	@SuppressWarnings(\"unused\")\n" +
			"	private int i;\n" + // problem configured as warning but still suppressed
			"}\n"
			};
	runConformTest(
			testFiles,
			null,
			null,
			true,
			null,
			customOptions,
			null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=295551
public void test281() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_SuppressWarnings, CompilerOptions.DISABLED); // this option overrides the next
	customOptions.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	String testFiles [] = new String[] {
			"A.java",
			"public class A {\n" +
			"	@SuppressWarnings(\"unused\")\n" +
			"	private int i;\n" +
			"}\n"
			};
	String expectedErrorString = 
			"----------\n" + 
			"1. ERROR in A.java (at line 3)\n" + 
			"	private int i;\n" + 
			"	            ^\n" + 
			"The value of the field A.i is not used\n" + 
			"----------\n";
	runNegativeTest(
			true,
			testFiles,
			null, 
			customOptions,
			expectedErrorString,
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=295551
public void test282() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	String testFiles [] = new String[] {
			"A.java",
			"import java.util.Map;\n" +
			"public class A {\n" +
			"	@SuppressWarnings({\"rawtypes\", \"unused\"})\n" + //suppress a warning and an error
			"	private Map i;\n" + 
			"}\n"
			};
	runConformTest(
			testFiles,
			null,
			null,
			true,
			null,
			customOptions,
			null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=295551
public void test283() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	String testFiles [] = new String[] {
			"A.java",
			"public class A {\n" +
			"	@SuppressWarnings(\"all\")\n" +
			"	private void i;\n" + // cannot suppress mandatory error
			"}\n"
			};
	String expectedErrorString = 
			"----------\n" + 
			"1. ERROR in A.java (at line 3)\n" + 
			"	private void i;\n" + 
			"	             ^\n" + 
			"void is an invalid type for the variable i\n" + 
			"----------\n";
	runNegativeTest(
			true,
			testFiles,
			null, 
			customOptions,
			expectedErrorString,
			JavacTestOptions.SKIP);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=304031
public void test284() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	String testFiles [] = new String[] {
			"X.java",
			"public class X {\n" + 
			"    void m() {\n" + 
			"        @SuppressWarnings(\"cast\")\n" + 
			"        int i= (int) 0;\n" + 
			"        @SuppressWarnings(\"cast\")\n" + 
			"        byte b= (byte) i;\n" + 
			"        System.out.println(b);\n" + 
			"    }\n" + 
			"}"
	};
	String expectedErrorString = 
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	@SuppressWarnings(\"cast\")\n" + 
		"	                  ^^^^^^\n" + 
		"Unnecessary @SuppressWarnings(\"cast\")\n" + 
		"----------\n";
	runNegativeTest(
			true,
			testFiles,
			null, 
			customOptions,
			expectedErrorString,
			JavacTestOptions.SKIP);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=304031
public void test285() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	String testFiles [] = new String[] {
			"X.java",
			"public class X {\n" + 
			"    void m() {\n" + 
			"        @SuppressWarnings(\"cast\")\n" + 
			"        int i= (int) 0;\n" + 
			"        @SuppressWarnings(\"cast\")\n" + 
			"        byte b= (byte) i;\n" + 
			"        System.out.println(b);\n" + 
			"    }\n" + 
			"}"
	};
	String expectedErrorString = 
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	@SuppressWarnings(\"cast\")\n" + 
		"	                  ^^^^^^\n" + 
		"Unnecessary @SuppressWarnings(\"cast\")\n" + 
		"----------\n";
	runNegativeTest(
			true,
			testFiles,
			null, 
			customOptions,
			expectedErrorString,
			JavacTestOptions.SKIP);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=304031
public void test286() {
	Map raiseDeprecationReduceInvalidJavadocSeverity =
		new HashMap(2);
	raiseDeprecationReduceInvalidJavadocSeverity.put(
			CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	raiseDeprecationReduceInvalidJavadocSeverity.put(
			CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	raiseDeprecationReduceInvalidJavadocSeverity.put(
			CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
				"X.java",
				"@SuppressWarnings(\"deprecation\")\n" +
				"public class X extends p.OldStuff {\n" +
				"	/**\n" +
				"	 * @see p.OldStuff#foo()\n" +
				"	 */\n" +
				"	@Override\n" +
				"	public void foo() {\n" +
				"		super.foo();\n" +
				"	}\n" +
				"}\n",
				"p/OldStuff.java",
				"package p;\n" +
				"@Deprecated\n" +
				"public class OldStuff {\n" +
				"	public void foo() {\n" +
				"	}	\n" +
				"}\n",
		},
		"",
		null,
		true,
		null,
		raiseDeprecationReduceInvalidJavadocSeverity,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=311849
public void test287() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	this.runConformTest(
		new String[] {
				"X.java",
				"import java.util.ArrayList;\n" + 
				"\n" + 
				"public class X {\n" + 
				"	@SuppressWarnings(\"rawtypes\")\n" + 
				"	void foo(ArrayList arg) {\n" + 
				"		for (\n" + 
				"			@SuppressWarnings(\"unchecked\")\n" +
				"			boolean a= arg.add(1), b= arg.add(1);\n" + 
				"			Boolean.FALSE;\n" + 
				"		) {\n" + 
				"			System.out.println(a && b);\n" + 
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
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=311849
public void test288() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	this.runConformTest(
		new String[] {
				"X.java",
				"import java.util.ArrayList;\n" + 
				"\n" + 
				"public class X {\n" + 
				"	@SuppressWarnings(\"rawtypes\")\n" + 
				"	ArrayList arg;\n" + 
				"	@SuppressWarnings(\"unchecked\")\n" + 
				"	boolean a= arg.add(1), b= arg.add(1);\n" + 
				"}",
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=311849
public void test289() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	this.runConformTest(
		new String[] {
				"X.java",
				"import java.util.ArrayList;\n" + 
				"\n" + 
				"public class X {\n" + 
				"	void foo(ArrayList arg) {\n" + 
				"		for (\n" + 
				"			@Deprecated\n" + 
				"			@Other\n" + 
				"			@SuppressWarnings(\"unchecked\")\n" +
				"			boolean a= arg.add(1), b= arg.add(1);\n" + 
				"			Boolean.FALSE;\n" + 
				"		) {\n" + 
				"			System.out.println(a && b);\n" + 
				"		}\n" + 
				"	}\n" + 
				"}",
				"Other.java",
				"@interface Other {}"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=313109
public void test290() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
				"X.java",
				"import java.util.ArrayList;\n" + 
				"class X {\n" + 
				"	@SuppressWarnings(\"rawtypes\")\n" + 
				"	void foo(ArrayList arg) {\n" + 
				"		@SuppressWarnings(\"unchecked\")\n" + 
				"		boolean aa = arg.add(1), bb = arg.add(1);\n" + 
				"		if (bb)\n" + 
				"			System.out.println(\"hi\");\n" + 
				"	}\n" + 
				"}"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=313109
public void test291() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
				"X.java",
				"import java.util.ArrayList;\n" + 
				"class X {\n" + 
				"	@SuppressWarnings(\"rawtypes\")\n" + 
				"	void foo(ArrayList arg) {\n" + 
				"		@SuppressWarnings(\"unchecked\")\n" + 
				"		boolean aa = arg.add(1), bb = arg.add(1);\n" + 
				"		if (aa)\n" + 
				"			System.out.println(\"hi\");\n" + 
				"	}\n" + 
				"}"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=313109
public void test292() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportUnusedWarningToken, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
				"X.java",
				"import java.util.ArrayList;\n" + 
				"class X {\n" + 
				"	@SuppressWarnings(\"rawtypes\")\n" + 
				"	void foo(ArrayList arg) {\n" + 
				"		@SuppressWarnings(\"unchecked\")\n" + 
				"		boolean aa = arg.add(1), bb = arg.add(1);\n" + 
				"	}\n" + 
				"}"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=316456
public void test293() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"@A(name = X.QUERY_NAME, query = X.QUERY)\n" +
				"public class X {\n" +
				"    public static final String QUERY_NAME = \"client.query.name\";\n" +
				"    private static final String QUERY = \"from Client\";\n" +
				"}\n" +
				"@interface A{\n" +
				"    String name();\n" +
				"    String query();\n" +
				"}\n" 
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	@A(name = X.QUERY_NAME, query = X.QUERY)\n" + 
		"	                                  ^^^^^\n" + 
		"The field X.QUERY is not visible\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179566
public void test294() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportMissingJavadocTags, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportMissingJavadocComments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	String testFiles [] = new String[] {
			"A.java",
			"/** */\n" +
			"public class A {\n" +
			"	@SuppressWarnings(\"javadoc\")\n" +
			"	public int foo(int i) { return 0; }\n" +
			"}\n"
			};
	runConformTest(
			testFiles,
			null,
			null,
			true,
			null,
			customOptions,
			null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179566
public void test295() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportMissingJavadocTags, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportMissingJavadocComments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	String testFiles [] = new String[] {
			"A.java",
			"/** */\n" +
			"public class A {\n" +
			"	/**\n" +
			"	 * @param j the given param/\n" +
			"	 */\n" +
			"	@SuppressWarnings(\"javadoc\")\n" +
			"	public int foo(int i) { return 0; }\n" +
			"}\n"
			};
	runConformTest(
			testFiles,
			null,
			null,
			true,
			null,
			customOptions,
			null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179566
public void test296() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportMissingJavadocTags, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportMissingJavadocComments, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	String testFiles [] = new String[] {
			"A.java",
			"/** */\n" +
			"public class A {\n" +
			"	/**\n" +
			"	 * @param i/\n" +
			"	 */\n" +
			"	@SuppressWarnings(\"javadoc\")\n" +
			"	public int foo(int i) { return 0; }\n" +
			"}\n"
			};
	runConformTest(
			testFiles,
			null,
			null,
			true,
			null,
			customOptions,
			null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=343621
public void test297() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_SuppressWarnings, CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnhandledWarningToken, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_ReportComparingIdentical, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.ERROR);
	
	String expectedErrors = 
		"----------\n" + 
		"1. ERROR in A.java (at line 15)\n" + 
		"	return i == i;\n" + 
		"	       ^^^^^^\n" + 
		"Comparing identical expressions\n" + 
		"----------\n";

	if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
		expectedErrors =
			"----------\n" + 
			"1. ERROR in A.java (at line 10)\n" + 
			"	public final Object build(Class<? super Object>... objects) {\n" + 
			"	                                                   ^^^^^^^\n" + 
			"Type safety: Potential heap pollution via varargs parameter objects\n" + 
			"----------\n" + 
			"2. ERROR in A.java (at line 15)\n" + 
			"	return i == i;\n" + 
			"	       ^^^^^^\n" + 
			"Comparing identical expressions\n" + 
			"----------\n";
	}
	String testFiles [] = new String[] {
			"A.java",
			"public class A {\n" + 
			"	public void one() {\n" + 
			"		@SuppressWarnings(\"unused\")\n" + 
			"		Object object = new Object();\n" + 
			"	}\n" + 
			"	public void two() {\n" + 
			"		@SuppressWarnings({ \"unchecked\", \"unused\" })\n" + 
			"		Object object = build();\n" + 
			"	}\n" + 
			"	public final Object build(Class<? super Object>... objects) {\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"	public boolean bar() {\n" +
			"		int i = 0;\n" +
			"		return i == i;\n" + 
			"	}\n" + 
			"}"
	};
	runNegativeTest(
			testFiles,
			expectedErrors,
			null,
			true,
			customOptions);
}
}
