/*******************************************************************************
 * Copyright (c) 2013, 2020 Jesper S Moller and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jesper S Moller - initial API and implementation
 *     					Bug 412151 - [1.8][compiler] Check repeating annotation's collection type
 *     					Bug 412149 - [1.8][compiler] Emit repeated annotations into the designated container
 *     					Bug 419209 - [1.8] Repeating container annotations should be rejected in the presence of annotation it contains
 *		Stephan Herrmann - Contribution for
 *						Bug 419209 - [1.8] Repeating container annotations should be rejected in the presence of annotation it contains
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import junit.framework.Test;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.impl.IntConstant;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;

@SuppressWarnings({ "rawtypes" })
public class RepeatableAnnotationTest extends AbstractComparableTest {

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which do not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test006" };
//		TESTS_NUMBERS = new int[] { 297 };
//		TESTS_RANGE = new int[] { 294, -1 };
	}
	boolean isJRE14 = false;
	public RepeatableAnnotationTest(String name) {
		super(name);
		String javaVersion = System.getProperty("java.version");
		int index = javaVersion.indexOf('.');
		if (index != -1) {
			javaVersion = javaVersion.substring(0, index);
		} else {
			index = javaVersion.indexOf('-');
			if (index != -1)
				javaVersion = javaVersion.substring(0, index);
		}
		this.isJRE14 = Integer.parseInt(javaVersion) >= 14;
	}
	private String normalizeAnnotationString(String s) {
		if (!this.isJRE14) return s;
		if (s.indexOf("value=") != -1) {
			s = s.replace("value=[", "{");
			s = s.replace("value=", "");
		}
		return s;
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}

	public static Class testClass() {
		return RepeatableAnnotationTest.class;
	}

	// check repeated occurrence of non-repeatable annotation
	public void test001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public @Foo @Foo class X {\n" +
				"}\n" +
				"\n",
				"Foo.java",
				"public @interface Foo {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public @Foo @Foo class X {\n" +
			"	       ^^^^\n" +
			"Duplicate annotation of non-repeatable type @Foo. Only annotation types marked @Repeatable can be used multiple times at one target.\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	public @Foo @Foo class X {\n" +
			"	            ^^^^\n" +
			"Duplicate annotation of non-repeatable type @Foo. Only annotation types marked @Repeatable can be used multiple times at one target.\n" +
			"----------\n");
	}

	public void test002() {
		this.runConformTest(
				new String[] {
						"X.java",
						"@Foo @Foo public class X {\n" +
								"}\n" +
								"\n",
								"Foo.java",
								"@java.lang.annotation.Repeatable(FooContainer.class) public @interface Foo {\n" +
										"}\n",
										"FooContainer.java",
										"public @interface FooContainer {\n" +
												"	Foo[] value();\n" +
												"}\n"
				},
				"");
	}

	// check repeated occurrence of annotation where annotation container is not valid for the target
	public void test003() {
		this.runNegativeTest(
			new String[] {
				"FooContainer.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"@Target({ElementType.METHOD, ElementType.FIELD}) public @interface FooContainer {\n" +

				"	Foo[] value();\n" +
				"}\n",
				"Foo.java",
				"@java.lang.annotation.Repeatable(FooContainer.class) public @interface Foo {\n" +
				"}\n",
				"X.java",
				"@Foo @Foo public class X { /* Problem */\n" +
				"  @Foo @Foo void okHere() { /* No problem */\n" +
				"    @Foo @Foo int local = 0; /* Problem! */\n" +
				"  }\n" +
				"  @Foo @Foo int alsoFoo = 0; /* No problem */\n" +
				"  @Foo class Y {} /* No problem since not repeated */\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	@Foo @Foo public class X { /* Problem */\n" +
			"	^^^^\n" +
			"The annotation @Foo cannot be repeated at this location since its container annotation type @FooContainer is disallowed at this location\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	@Foo @Foo int local = 0; /* Problem! */\n" +
			"	^^^^\n" +
			"The annotation @Foo cannot be repeated at this location since its container annotation type @FooContainer is disallowed at this location\n" +
			"----------\n");
	}

	// This is the same test as test003, only where the annotation info for Foo is from a class file, not from the compiler
	public void test004() {
		this.runConformTest(
			new String[] {
					"FooContainer.java",
					"import java.lang.annotation.ElementType;\n" +
					"import java.lang.annotation.Target;\n" +
					"@Target({ElementType.METHOD, ElementType.FIELD}) public @interface FooContainer {\n" +
					"	Foo[] value();\n" +
					"}\n",
					"Foo.java",
					"@java.lang.annotation.Repeatable(FooContainer.class) public @interface Foo {\n" +
					"}\n"
				},
				"");
		Runner runner = new Runner();
		runner.testFiles =
			new String[] {
				"X.java",
				"@Foo @Foo public class X { /* Problem */\n" +
				"}\n"
			};
		runner.expectedCompilerLog =
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	@Foo @Foo public class X { /* Problem */\n" +
			"	^^^^\n" +
			"The annotation @Foo cannot be repeated at this location since its container annotation type @FooContainer is disallowed at this location\n" +
			"----------\n";
		runner.shouldFlushOutputDirectory = false;
		runner.javacTestOptions = JavacTestOptions.JavacHasABug.JavacBug8044196;
		runner.runNegativeTest();
	}

	// Test that a single, repeatable annotation can exist just fine an occurrence of its container annotation
	public void test005() {
		this.runConformTest(
			new String[] {
				"X.java",
				"@java.lang.annotation.Repeatable(FooContainer.class) @interface Foo {}\n" +
				"@interface FooContainer { Foo[] value(); }\n" +
				"@Foo @FooContainer({@Foo, @Foo}) public class X { /* Not a problem */ }\n"
			},
			"");
	}

	// Test that an repeated annotation can't occur together with its container annotation
	public void test006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface FooContainer { Foo[] value(); }\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class) @interface Foo {}\n" +
				"@Foo @Foo @FooContainer({@Foo, @Foo}) public class X { /* A problem */ }\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	@Foo @Foo @FooContainer({@Foo, @Foo}) public class X { /* A problem */ }\n" +
			"	^^^^\n" +
			"The repeatable annotation @Foo may not be repeated where its container annotation type @FooContainer is also used directly\n" +
			"----------\n");
	}

	// Test that an repeated annotation can't occur together with its container annotation, even if it itself is repeatable.
	public void test007() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface FooContainerContainer { FooContainer[] value(); }\n" +
				"@java.lang.annotation.Repeatable(FooContainerContainer.class) @interface FooContainer { Foo[] value(); }\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class) @interface Foo {}\n" +
				"@Foo @Foo @FooContainer({@Foo, @Foo}) public class X { /* Still a problem */ }\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	@Foo @Foo @FooContainer({@Foo, @Foo}) public class X { /* Still a problem */ }\n" +
			"	^^^^\n" +
			"The repeatable annotation @Foo may not be repeated where its container annotation type @FooContainer is also used directly\n" +
			"----------\n");
	}

	// Test that an repeated annotation can't occur together with its container annotation, even if it itself is repeatable.
	public void test007a() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface FooContainerContainer { FooContainer[] value(); }\n" +
				"@java.lang.annotation.Repeatable(FooContainerContainer.class) @interface FooContainer { Foo[] value(); }\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class) @interface Foo {}\n" +
				"@interface Bar {}\n" +
				"@Foo @Foo @Bar @Bar @FooContainer({@Foo, @Foo}) public class X { /* Still a problem */ }\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	@Foo @Foo @Bar @Bar @FooContainer({@Foo, @Foo}) public class X { /* Still a problem */ }\n" +
			"	^^^^\n" +
			"The repeatable annotation @Foo may not be repeated where its container annotation type @FooContainer is also used directly\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	@Foo @Foo @Bar @Bar @FooContainer({@Foo, @Foo}) public class X { /* Still a problem */ }\n" +
			"	          ^^^^\n" +
			"Duplicate annotation of non-repeatable type @Bar. Only annotation types marked @Repeatable can be used multiple times at one target.\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	@Foo @Foo @Bar @Bar @FooContainer({@Foo, @Foo}) public class X { /* Still a problem */ }\n" +
			"	               ^^^^\n" +
			"Duplicate annotation of non-repeatable type @Bar. Only annotation types marked @Repeatable can be used multiple times at one target.\n" +
			"----------\n");
	}

	// Test that repeated annotations should be contiguous (raises a warning if not) -- not yet in BETA_JAVA8
	public void _test008() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@interface Bar {}\n" +
				"@interface Baz {}\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class) @interface Foo {}\n" +
				"@interface FooContainer { Foo[] value(); }\n" +
				"@Foo @Bar @Foo /* just lexical */ @Foo public class X { /* Gives a warning */ }\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 5)\n" +
			"	@Foo @Bar @Foo /* just lexical */ @Foo public class X { /* Gives a warning */ }\n" +
			"	          ^^^^\n" +
			"Repeated @Foo annotations are not grouped together\n" +
			"----------\n");
	}
	// Test that deprecation of container annotation is reflected in the repeated annotation (disabled until specification clarification is available)
	public void _test009() {
		this.runConformTest(
			new String[] {
				"Y.java",
				"@java.lang.annotation.Repeatable(FooContainer.class) @interface Foo { int value(); }\n" +
				"@Deprecated @interface FooContainer { Foo[] value(); }\n" +
				"@Foo(0) class X { /* Gives a warning */ }\n" +
				"@Foo(1) @Foo(2) public class Y { /* Gives a warning */ }\n"
			},
			new ASTVisitor() {
				public boolean visit(
						TypeDeclaration typeDeclaration,
						CompilationUnitScope scope) {
						if (new String(typeDeclaration.name).equals("X")) {
							assertFalse("Foo on X should NOT be deprecated!", typeDeclaration.annotations[0].getCompilerAnnotation().getAnnotationType().isDeprecated());
						}
						if (new String(typeDeclaration.name).equals("Y")) {
							assertEquals("Find Foo(1) on Y",  IntConstant.fromValue(1), typeDeclaration.annotations[0].getCompilerAnnotation().getElementValuePairs()[0].value);
							assertTrue("1st Foo on Y should be deprecated!", typeDeclaration.annotations[0].getCompilerAnnotation().getAnnotationType().isDeprecated());
							assertEquals("Find Foo(2) on Y",  IntConstant.fromValue(2), typeDeclaration.annotations[1].getCompilerAnnotation().getElementValuePairs()[0].value);
							assertTrue("2nd Foo on Y should be deprecated!", typeDeclaration.annotations[1].getCompilerAnnotation().getAnnotationType().isDeprecated());
						}
						return true; // do nothing by default, keep traversing
					}
			});
	}
	// Bug 412151: [1.8][compiler] Check repeating annotation's collection type
	// 412151: The collections type's (TC) declaration must have a array of Ts as its value() - with Foo and FooContainer in same compilation round
	public void test010() {
		this.runNegativeTest(
			new String[] {
			"Foo.java",
			"@interface FooContainer {\n" +
			"}\n" +
			"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
			"@interface Foo {}\n"
			},
		"----------\n" +
		"1. ERROR in Foo.java (at line 3)\n" +
		"	@java.lang.annotation.Repeatable(FooContainer.class)\n" +
		"	                                 ^^^^^^^^^^^^^^^^^^\n" +
		"The container annotation type @FooContainer must declare a member value()\n" +
		"----------\n");
	}
	// 412151: The collections type's (TC) declaration must have a array of Ts as its value() - with Foo and FooContainer in same compilation round
	public void test011() {
		this.runNegativeTest(
			new String[] {
			"Foo.java",
			"@interface FooContainer {\n" +
			"    int[] value();\n" +
			"}\n" +
			"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
			"@interface Foo {}\n"
			},
		"----------\n" +
		"1. ERROR in Foo.java (at line 4)\n" +
		"	@java.lang.annotation.Repeatable(FooContainer.class)\n" +
		"	                                 ^^^^^^^^^^^^^^^^^^\n" +
		"The value method in the container annotation type @FooContainer must be of type Foo[] but is int[]\n" +
		"----------\n");
	}
	// 412151: The collections type's (TC) declaration must have a array of Ts as its value() - with Foo and FooContainer in same compilation round
	public void test012() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"@interface FooContainer {\n" +
				"    Foo[][] value();\n" +
				"}\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@interface Foo {}\n"
			},
			"----------\n" +
			"1. ERROR in Foo.java (at line 2)\n" +
			"	Foo[][] value();\n" +
			"	^^^^^^^\n" +
			"Invalid type Foo[][] for the annotation attribute FooContainer.value; only primitive type, String, Class, annotation, enumeration are permitted or 1-dimensional arrays thereof\n" +
			"----------\n" +
			"2. ERROR in Foo.java (at line 4)\n" +
			"	@java.lang.annotation.Repeatable(FooContainer.class)\n" +
			"	                                 ^^^^^^^^^^^^^^^^^^\n" +
			"The value method in the container annotation type @FooContainer must be of type Foo[] but is Foo[][]\n" +
			"----------\n"
		);
	}
	// 412151: Any methods declared by TC other than value() have a default value (JLS 9.6.2).
	public void test013() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"@interface FooContainer {\n" +
				"    Foo[] value();\n" +
				"    int hasDefaultValue() default 1337;\n" +
				"    int doesntHaveDefaultValue();\n" +
				"}\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@interface Foo {}\n"
			},
		"----------\n" +
		"1. ERROR in Foo.java (at line 6)\n" +
		"	@java.lang.annotation.Repeatable(FooContainer.class)\n" +
		"	                                 ^^^^^^^^^^^^^^^^^^\n" +
		"The container annotation type @FooContainer must declare a default value for the annotation attribute \'doesntHaveDefaultValue\'\n" +
		"----------\n");
	}
	// 412151: The @Retention meta-annotation of TC must at least include the retention of T ()
	public void test014() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@interface FooContainer {\n" +
				"    Foo[] value();\n" +
				"}\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@Retention(RetentionPolicy.CLASS)\n" +
				"@interface Foo {\n" +
				"}\n"
			},
		"");
	}

	//
	public void test015() {
		// These are fine:
		this.runConformTest(
			new String[] {
					"FooContainer.java",
					"public @interface FooContainer {\n" +
					"	Foo[] value();\n" +
					"}\n",
					"Foo.java",
					"@java.lang.annotation.Repeatable(FooContainer.class) public @interface Foo {\n" +
					"}\n"
				},
				"");
		// This changes FooContainer without re-checking Foo
		this.runConformTest(
				new String[] {
						"FooContainer.java",
						"public @interface FooContainer {\n" +
						"	int[] value();\n" +
						"}\n"
					},
					"",
					null,
					false,
					null);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"@Foo @Foo public class X { /* Problem since Foo now uses FooContainer which doesn't work anymore*/\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	@Foo @Foo public class X { /* Problem since Foo now uses FooContainer which doesn\'t work anymore*/\n" +
			"	^^^^\n" +
			"The value method in the container annotation type @FooContainer must be of type Foo[] but is int[]\n" +
			"----------\n",
			null, false /* don't flush*/);
	}

	// 412151: The @Retention meta-annotation of TC must at least include the retention of T ()
	// Base example, both targets are specified
	public void test016() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"@Retention(RetentionPolicy.SOURCE)\n" +
				"@interface FooContainer { Foo[] value(); }\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface Foo { }\n"
			},
		"----------\n" +
		"1. ERROR in Foo.java (at line 5)\n" +
		"	@java.lang.annotation.Repeatable(FooContainer.class)\n" +
		"	                                 ^^^^^^^^^^^^^^^^^^\n" +
		"Retention \'RUNTIME\' of @Foo is longer than the retention of its container annotation type @FooContainer, which is \'SOURCE\'\n" +
		"----------\n");
	}

	// 412151: The @Retention meta-annotation of TC must at least include the retention of T ()
	// Only specified on FooContainer
	public void test017() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"@Retention(RetentionPolicy.SOURCE)\n" +
				"@interface FooContainer { Foo[] value(); }\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@interface Foo { }\n"
			},
		"----------\n" +
		"1. ERROR in Foo.java (at line 5)\n" +
		"	@java.lang.annotation.Repeatable(FooContainer.class)\n" +
		"	                                 ^^^^^^^^^^^^^^^^^^\n" +
		"Retention \'CLASS\' of @Foo is longer than the retention of its container annotation type @FooContainer, which is \'SOURCE\'\n" +
		"----------\n");
	}

	// 412151: The @Retention meta-annotation of TC must at least include the retention of T ()
	// Only specified on Foo
	public void test018() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"@interface FooContainer { Foo[] value(); }\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface Foo { }\n"
			},
		"----------\n" +
		"1. ERROR in Foo.java (at line 4)\n" +
		"	@java.lang.annotation.Repeatable(FooContainer.class)\n" +
		"	                                 ^^^^^^^^^^^^^^^^^^\n" +
		"Retention \'RUNTIME\' of @Foo is longer than the retention of its container annotation type @FooContainer, which is \'CLASS\'\n" +
		"----------\n");
	}

	// 412151: The @Retention meta-annotation of TC must at least include the retention of T ()
	// Only specified on Foo - but positive
	public void test019() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"@interface FooContainer { Foo[] value(); }\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@Retention(RetentionPolicy.SOURCE)\n" +
				"@interface Foo { }\n"
			});
	}

	// 412151: The @Retention meta-annotation of TC must at least include the retention of T
	// Only specified on FooContainer, separate compilation
	public void test020() {
		this.runConformTest(
			new String[] {
					"FooContainer.java",
					"import java.lang.annotation.Retention;\n" +
					"import java.lang.annotation.RetentionPolicy;\n" +
					"@Retention(RetentionPolicy.SOURCE)\n" +
					"public @interface FooContainer { Foo[] value(); }\n",
					"Foo.java",
					"import java.lang.annotation.Retention;\n" +
					"import java.lang.annotation.RetentionPolicy;\n" +
					"@Retention(RetentionPolicy.SOURCE)\n" +
					"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
					"public @interface Foo { }\n"
				});
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"public @interface Foo { } // If omitted, retention is class\n"
			},
		"----------\n" +
		"1. ERROR in Foo.java (at line 1)\n" +
		"	@java.lang.annotation.Repeatable(FooContainer.class)\n" +
		"	                                 ^^^^^^^^^^^^^^^^^^\n" +
		"Retention \'CLASS\' of @Foo is longer than the retention of its container annotation type @FooContainer, which is \'SOURCE\'\n" +
		"----------\n",
		null, false /* don't flush*/);
	}

	// 412151: The @Retention meta-annotation of TC must at least include the retention of T ()
	// Only specified on Foo, separate compilation
	public void test021() {
		this.runConformTest(
			new String[] {
				"FooContainer.java",
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"public @interface FooContainer { Foo[] value(); }\n",
				"Foo.java",
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"public @interface Foo { }\n"
			});
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@Retention(RetentionPolicy.RUNTIME)\n" +
				"@interface Foo { }\n"
			},
		"----------\n" +
		"1. ERROR in Foo.java (at line 3)\n" +
		"	@java.lang.annotation.Repeatable(FooContainer.class)\n" +
		"	                                 ^^^^^^^^^^^^^^^^^^\n" +
		"Retention \'RUNTIME\' of @Foo is longer than the retention of its container annotation type @FooContainer, which is \'CLASS\'\n" +
		"----------\n",
		null, false /* don't flush*/);
	}

	// 412151: TC's @Targets, if specified, must be a subset or the same as T's @Targets
	// TC's @Targets, if specified, must be a subset or the same as T's @Targets. Simple test
	public void test022() {
		this.runNegativeTest(
			new String[] {
				"FooContainer.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"public @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})\n" +
				"@interface FooContainer { Foo[] value(); }\n",
				"Foo.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"public @java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@Target({ElementType.FIELD})\n" +
				"@interface Foo { }\n"
			},
		"----------\n" +
		"1. ERROR in Foo.java (at line 3)\n" +
		"	public @java.lang.annotation.Repeatable(FooContainer.class)\n" +
		"	                                        ^^^^^^^^^^^^^^^^^^\n" +
		"The container annotation type @FooContainer is allowed at targets where the repeatable annotation type @Foo is not: TYPE, METHOD\n" +
		"----------\n");
	}

	// 412151: TC's @Targets, if specified, must be a subset or the same as T's @Targets
	// TC's @Targets, if specified, must be a subset or the same as T's @Targets. Test this as a separate pass, so that
	// FooContainer is loaded from binary.
	public void test023() {
		this.runConformTest(
			new String[] {
				"FooContainer.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"public @Target({ElementType.METHOD})\n" +
				"@interface FooContainer { Foo[] value(); }\n",
				"Foo.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"public @Target({ElementType.METHOD})\n" +
				"@interface Foo { }\n"
			});
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"public @java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@java.lang.annotation.Target({ElementType.FIELD})\n" +
				"@interface Foo { }\n"
			},
		"----------\n" +
		"1. ERROR in Foo.java (at line 3)\n" +
		"	public @java.lang.annotation.Repeatable(FooContainer.class)\n" +
		"	                                        ^^^^^^^^^^^^^^^^^^\n" +
		"The container annotation type @FooContainer is allowed at targets where the repeatable annotation type @Foo is not: METHOD\n" +
		"----------\n",
		null, false /* don't flush*/);
	}

	// 412151: TC's @Targets, if specified, must be a subset or the same as T's @Targets
	// TC's may target ANNOTATION_TYPE but that should match TYPE for T, since it's a superset
	public void test024() {
		this.runConformTest(
			new String[] {
				"FooContainer.java",
				"import java.lang.annotation.ElementType;\n" +
				"@java.lang.annotation.Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})\n" +
				"@interface FooContainer { Foo[] value(); }\n",
				"Foo.java",
				"import java.lang.annotation.ElementType;\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@java.lang.annotation.Target({ElementType.METHOD, ElementType.TYPE})\n" +
				"@interface Foo { }\n"
			});
	}

	// 412151: TC's @Targets, if specified, must be a subset or the same as T's @Targets
	// Test that all ElementTypes can be reported
	public void test025() {
		this.runNegativeTest(
			new String[] {
				"FooContainer.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"public @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.CONSTRUCTOR, ElementType.LOCAL_VARIABLE, ElementType.ANNOTATION_TYPE, ElementType.PACKAGE, ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})\n" +
				"@interface FooContainer { Foo[] value(); }\n",
				"Foo.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"public @java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@Target({})\n" +
				"@interface Foo { }\n"
			},
		"----------\n" +
		"1. ERROR in Foo.java (at line 3)\n" +
		"	public @java.lang.annotation.Repeatable(FooContainer.class)\n" +
		"	                                        ^^^^^^^^^^^^^^^^^^\n" +
		"The container annotation type @FooContainer is allowed at targets where the repeatable annotation type @Foo is not: TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, ANNOTATION_TYPE, PACKAGE, TYPE_PARAMETER, TYPE_USE\n" +
		"----------\n");
	}

	// 412151: TC's @Targets, if specified, must be a subset or the same as T's @Targets
	// TC's has no @Targets (=every declaration location), but @Foo has, then complain.
	public void test026() {
		this.runConformTest(
			new String[] {
				"FooContainer.java",
				"@interface FooContainer { Foo[] value(); }\n",
				"Foo.java",
				"@interface Foo { }\n"
			});
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@java.lang.annotation.Target({ElementType.FIELD})\n" +
				"@interface Foo { }\n"
			},
			"----------\n" +
			"1. ERROR in Foo.java (at line 3)\n" +
			"	@java.lang.annotation.Repeatable(FooContainer.class)\n" +
			"	                                 ^^^^^^^^^^^^^^^^^^\n" +
			"The container annotation type @FooContainer is allowed at targets where the repeatable annotation type @Foo is not: TYPE, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, ANNOTATION_TYPE, PACKAGE, TYPE_PARAMETER, MODULE, RECORD_COMPONENT\n" +
			"----------\n",
		null, false /* don't flush*/);
	}

	// 412151: If T is @Documented, then TC should also be Documented
	public void test027() {
		this.runConformTest(
			new String[] {
				"FooContainer.java",
				"@java.lang.annotation.Documented @interface FooContainer { Foo[] value(); }\n",
				"Foo.java",
				"@java.lang.annotation.Documented @interface Foo { }\n"});
	}

	// 412151: If T is @Documented, then TC should also be Documented, OK for TC to be documented while T is not
	public void test028() {
		this.runConformTest(
			new String[] {
				"FooContainer.java",
				"@java.lang.annotation.Documented @interface FooContainer { Foo[] value(); }\n",
				"Foo.java",
				"@interface Foo { }\n"});
	}

	// 412151: If T is @Documented, then TC should also be Documented
	public void test029() {
		this.runNegativeTest(
			new String[] {
				"FooContainer.java",
				"@interface FooContainer { Foo[] value(); }\n",
				"Foo.java",
				"@java.lang.annotation.Repeatable(FooContainer.class) @java.lang.annotation.Documented\n" +
				"@interface Foo { }\n"
			},
			"----------\n" +
			"1. ERROR in Foo.java (at line 1)\n" +
			"	@java.lang.annotation.Repeatable(FooContainer.class) @java.lang.annotation.Documented\n" +
			"	                                 ^^^^^^^^^^^^^^^^^^\n" +
			"The repeatable annotation type @Foo is marked @Documented, but its container annotation type @FooContainer is not\n" +
			"----------\n");
	}

	// 412151: If T is @Documented, then TC should also be Documented - check from previous compilation
	public void test030() {
		this.runConformTest(
				new String[] {
					"FooContainer.java",
					"@java.lang.annotation.Documented @interface FooContainer { Foo[] value(); }\n",
					"Foo.java",
					"@java.lang.annotation.Documented @interface Foo { }\n"
				});
			this.runConformTest(
				new String[] {
					"Foo.java",
					"public @java.lang.annotation.Documented @java.lang.annotation.Repeatable(FooContainer.class)\n" +
					"@interface Foo { }\n"
				},
				"",
				null,
				false,
				null);
	}

	// 412151: If T is @Inherited, then TC should also be Inherited
	public void test031() {
		this.runConformTest(
			new String[] {
				"FooContainer.java",
				"@java.lang.annotation.Inherited @interface FooContainer { Foo[] value(); }\n",
				"Foo.java",
				"@java.lang.annotation.Inherited @interface Foo { }\n"});
	}

	// 412151: If T is @Inherited, then TC should also be Inherited, OK for TC to be inherited while T is not.
	public void test032() {
		this.runConformTest(
			new String[] {
				"FooContainer.java",
				"@java.lang.annotation.Inherited @interface FooContainer { Foo[] value(); }\n",
				"Foo.java",
				"@interface Foo { }\n"});
	}
	// 412151: If T is @Inherited, then TC should also be Inherited
	public void test033() {
		this.runNegativeTest(
			new String[] {
				"FooContainer.java",
				"@interface FooContainer { Foo[] value(); }\n",
				"Foo.java",
				"@java.lang.annotation.Repeatable(FooContainer.class) @java.lang.annotation.Inherited\n" +
				"@interface Foo { }\n"
			},
			"----------\n" +
			"1. ERROR in Foo.java (at line 1)\n" +
			"	@java.lang.annotation.Repeatable(FooContainer.class) @java.lang.annotation.Inherited\n" +
			"	                                 ^^^^^^^^^^^^^^^^^^\n" +
			"The repeatable annotation type @Foo is marked @Inherited, but its container annotation type @FooContainer is not\n" +
			"----------\n");
	}

	// 412151: If T is @Inherited, then TC should also be Inherited - check from previous compilation
	public void test034() {
		this.runConformTest(
				new String[] {
					"FooContainer.java",
					"@java.lang.annotation.Inherited @interface FooContainer { Foo[] value(); }\n",
					"Foo.java",
					"@java.lang.annotation.Inherited @interface Foo { }\n"
				});
			this.runConformTest(
				new String[] {
					"Foo.java",
					"public @java.lang.annotation.Inherited @java.lang.annotation.Repeatable(FooContainer.class)\n" +
					"@interface Foo { }\n"
				},
				"",
				null,
				false,
				null);
	}
	// 412151: Ensure no double reporting for bad target.
	public void test035() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Repeatable;\n" +
				"import java.lang.annotation.Target;\n" +
				"@Target(ElementType.FIELD)\n" +
				"@interface TC {\n" +
				"	T [] value();\n" +
				"}\n" +
				"@Target(ElementType.TYPE)\n" +
				"@Repeatable(TC.class)\n" +
				"@interface T {\n" +
				"}\n" +
				"@T @T // we used to double report here.\n" +
				"public class X { \n" +
				"	X f;\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	@Repeatable(TC.class)\n" +
			"	            ^^^^^^^^\n" +
			"The container annotation type @TC is allowed at targets where the repeatable annotation type @T is not: FIELD\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 12)\n" +
			"	@T @T // we used to double report here.\n" +
			"	^^\n" +
			"The annotation @T cannot be repeated at this location since its container annotation type @TC is disallowed at this location\n" +
			"----------\n");
	}
	// 412149: [1.8][compiler] Emit repeated annotations into the designated container
	public void test036() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Repeatable;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"\n" +
				"@Retention(RUNTIME)\n" +
				"@interface AttrContainer {\n" +
				"  public Attr[] value();\n" +
				"}\n" +
				"@Retention(RUNTIME)\n" +
				"@Repeatable(AttrContainer.class)\n" +
				"@interface Attr {\n" +
				"  public int value() default -1;\n" +
				"}\n" +
				"\n" +
				"@Attr(1) @Attr(2)\n" +
				"public class X {\n" +
				"  public static void main(String args[]) {\n" +
				"  	Object e[] = X.class.getAnnotationsByType(Attr.class);\n" +
				"  	for (int i=0; i<e.length;++i) System.out.print(e[i] + \" \");\n" +
				"  }\n" +
				"}"
			},
			normalizeAnnotationString("@Attr(value=1) @Attr(value=2)"));

	}
	// 412149: [1.8][compiler] Emit repeated annotations into the designated container
	// Test that only repetitions go into the container
	public void test037() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Annotation;\n" +
				"import java.lang.annotation.Repeatable;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"\n" +
				"@Retention(RUNTIME)\n" +
				"@interface AttrContainer {\n" +
				"  public Attr[] value();\n" +
				"}\n" +
				"@Retention(RUNTIME)\n" +
				"@Repeatable(AttrContainer.class)\n" +
				"@interface Attr {\n" +
				"  public int value() default -1;\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"  @Attr(1) class Y1 {}\n" +
				"  @Attr(1) @Attr(2) class Y2 {} \n" +
				"  public static void main(String args[]) {\n" +
				"  	System.out.print(\"Y1: \" + normalizeAnnotation(Y1.class.getAnnotation(Attr.class)) + \"\\n\");\n" +
				"  	System.out.print(\"Y2: \" + normalizeAnnotation(Y2.class.getAnnotation(Attr.class)) + \"\\n\");\n" +
				"  	System.out.print(\"Y1: \" + normalizeAnnotation(Y1.class.getAnnotation(AttrContainer.class)) + \"\\n\");\n" +
				"  	System.out.print(\"Y2: \" + normalizeAnnotation(Y2.class.getAnnotation(AttrContainer.class)) + \"\\n\");\n" +
				"  }\n" +
				"  static String normalizeAnnotation(Annotation a) {\n" +
				" 		if (a == null) return null;\n" +
				"	  String str = a.toString();\n" +
				"	  str = str.replace(\"value={@\", \"value=[@\");\n" +
				"	  str = str.replace(\")}\", \")]\");\n" +
				"	  return str;\n" +
				"  }\n" +
				"}"
			},
			normalizeAnnotationString("Y1: @Attr(value=1)\n" +
			"Y2: null\n" +
			"Y1: null\n" +
			"Y2: @AttrContainer(value=[@Attr(value=1), @Attr(value=2)])"));

	}
	// 412149: [1.8][compiler] Emit repeated annotations into the designated container
	// Test that the retention from the containing annotation is used
	public void test038() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Repeatable;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"\n" +
				"@Retention(RUNTIME)\n" +
				"@interface AttrContainer {\n" +
				"  public Attr[] value();\n" +
				"}\n" +
				"@Retention(SOURCE)\n" +
				"@Repeatable(AttrContainer.class)\n" +
				"@interface Attr {\n" +
				"  public int value() default -1;\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"  @Attr(1) class Y1 {}\n" +
				"  @Attr(1) @Attr(2) class Y2 {} \n" +
				"  public static void main(String args[]) {\n" +
				"  	System.out.println(\"Y1 has \" + Y1.class.getAnnotationsByType(Attr.class).length);\n" +
				"  	System.out.println(\"Y2 has \" + Y2.class.getAnnotationsByType(Attr.class).length);\n" +
				"  }\n" +
				"}"
			},
			"Y1 has 0\n" +
			"Y2 has 2");

	}
	// 412149: [1.8][compiler] Emit repeated annotations into the designated container
	// Test that repeated annotations can appear at package targets
	public void test039() throws Exception {
		String[] testFiles = {
				"repeatable/Main.java",
				"package repeatable;\n" +
				"public class Main {\n" +
				"    public static void main (String[] argv) {\n" +
				"    };\n" +
				"}",

			"repeatable/FooContainer.java",
			"package repeatable;\n" +
			"@java.lang.annotation.Target(java.lang.annotation.ElementType.PACKAGE)\n" +
			"@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)\n" +
			"public @interface FooContainer {\n" +
			"	Foo[] value();\n" +
			"}\n",

			"repeatable/Foo.java",
			"package repeatable;\n" +
			"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
			"public @interface Foo {}\n",

			"repeatable/package-info.java",
			"@Foo @Foo\n" +
			"package repeatable;\n" +
			"import repeatable.Foo;",
		};
		runConformTest(testFiles, "");
		String expectedOutout =
				"  RuntimeVisibleAnnotations: \n" +
				"    #8 @repeatable.FooContainer(\n" +
				"      #9 value=[\n" +
				"        annotation value =\n" +
				"            #10 @repeatable.Foo(\n" +
				"            )\n" +
				"        annotation value =\n" +
				"            #10 @repeatable.Foo(\n" +
				"            )\n" +
				"        ]\n" +
				"    )\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "repeatable" + File.separator + "package-info.class", "package-info", expectedOutout, ClassFileBytesDisassembler.SYSTEM);
	}
	// 412149: [1.8][compiler] Emit repeated annotations into the designated container
	// Test that repeated annotations show up on fields, methods, and parameters
	public void test040() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.reflect.Field;\n" +
				"import java.lang.reflect.Method;\n" +
				"import java.lang.reflect.Parameter;\n" +
				"import java.lang.annotation.Repeatable;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"\n" +
				"@Retention(RUNTIME)\n" +
				"@interface AttrContainer {\n" +
				"  public Attr[] value();\n" +
				"}\n" +
				"@Retention(RUNTIME)\n" +
				"@Repeatable(AttrContainer.class)\n" +
				"@interface Attr {\n" +
				"  public int value() default -1;\n" +
				"}\n" +
				"\n" +
				"public class X {\n" +
				"   @Attr(1) @Attr(2) public int field;\n" +
				"\n" +
				"   @Attr(3) @Attr(4)\n" +
				"   public static void main(@Attr(5) @Attr(6) String args[]) throws Exception {\n" +
				"    Field fieldField = X.class.getField(\"field\");\n" +
				"    dump(fieldField.getAnnotationsByType(Attr.class));\n" +
				"    Method mainMethod = X.class.getMethod(\"main\", (new String[0]).getClass());\n" +
				"    dump(mainMethod.getAnnotationsByType(Attr.class));\n" +
				"    Parameter argvParameter = mainMethod.getParameters()[0];\n" +
				"    dump(argvParameter.getAnnotationsByType(Attr.class));\n" +
				"   }\n" +
				"   static void dump(Attr[] attrs) {\n" +
				"    for (int i=0; i<attrs.length;++i) System.out.print(attrs[i] + \" \");\n" +
				"   }\n" +
				"}"
			},
			normalizeAnnotationString(
					"@Attr(value=1) @Attr(value=2) @Attr(value=3) @Attr(value=4) @Attr(value=5) @Attr(value=6)"));
	}
	// Test that repeated annotations show up type parameters properly.
	public void testTypeParameters() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Annotation;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Repeatable;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.Target;\n" +
				"import java.lang.reflect.AnnotatedElement;\n" +
				"import java.lang.reflect.AnnotatedType;\n" +
				"import java.lang.reflect.Field;\n" +
				"import java.lang.reflect.Method;\n" +
				"import java.lang.reflect.Type;\n" +
				"import java.lang.reflect.TypeVariable;\n" +
				"\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"\n" +
				"@Retention(RUNTIME)\n" +
				"@Target({ElementType.TYPE_USE, ElementType.TYPE, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER,})\n" +
				"@interface TC {\n" +
				"  public T[] value();\n" +
				"}\n" +
				"@Retention(RUNTIME)\n" +
				"@Repeatable(TC.class)\n" +
				"@Target({ElementType.TYPE_USE, ElementType.TYPE, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.METHOD})\n" +
				"@interface T {\n" +
				"  public int value() default -1;\n" +
				"}\n" +
				"\n" +
				"interface I<@T(1) @T(2) K extends @T(3) @T(4) Object & java.lang.@T(5) @T(6) Comparable<?>> {\n" +
				"}\n" +
				"\n" +
				"\n" +
				"public class X {\n" +
				"  public static void main(String args[]) {\n" +
				"	Class<I> ci = I.class;  \n" +
				"  	printAnnotations(\"I.class\", ci);\n" +
				"  	TypeVariable<Class<I>>[] typeParameters = ci.getTypeParameters();\n" +
				"  	for (TypeVariable<?> t: typeParameters) {\n" +
				"  		printAnnotations(t.getName(), t);\n" +
				"  		AnnotatedType[] bounds = t.getAnnotatedBounds();\n" +
				"  		for (AnnotatedType bound : bounds) {\n" +
				"  			printAnnotations(bound.getType().getTypeName(), bound);\n" +
				"  		}\n" +
				"  	}\n" +
				"  }\n" +
				"  \n" +
				"  static void printAnnotations(String name, AnnotatedElement element) {\n" +
				"	  int [] iterations = { 0, 1 };\n" +
				"	  for (int i : iterations) {\n" +
				"		  Class<? extends Annotation> annotation = i == 0 ? T.class : TC.class;\n" +
				"		  for (int j: iterations) {\n" +
				"			  Annotation [] annotations = j == 0 ? new Annotation [] { element.getAnnotation(annotation) } : element.getAnnotationsByType(annotation);\n" +
				"			  if (annotations.length == 0 || (annotations.length == 1 && annotations[0] == null)) continue;\n" +
				"			  System.out.print(name + (j == 0 ? \".getAnnotation(\" : \".getAnnotationByType(\") + annotation.getName() + \".class): \");\n" +
				"			  for (Annotation a : annotations) {\n" +
				"				  System.out.print(normalizeAnnotation(a) + \" \");\n" +
				"			  }\n" +
				"			  System.out.print(\"\\n\");\n" +
				"		  }\n" +
				"	  }\n" +
				"  }\n" +
				"  static String normalizeAnnotation(Annotation a) {\n" +
				" 		if (a == null) return null;\n" +
				"	  String str = a.toString();\n" +
				"	  str = str.replace(\"value={@\", \"value=[@\");\n" +
				"	  str = str.replace(\")}\", \")]\");\n" +
				"	  return str;\n" +
				"  }\n" +
				"}\n"

			},
			normalizeAnnotationString("K.getAnnotationByType(T.class): @T(value=1) @T(value=2) \n" +
			"K.getAnnotation(TC.class): @TC(value=[@T(value=1), @T(value=2)]) \n" +
			"K.getAnnotationByType(TC.class): @TC(value=[@T(value=1), @T(value=2)]) \n" +
			"java.lang.Object.getAnnotationByType(T.class): @T(value=3) @T(value=4) \n" +
			"java.lang.Object.getAnnotation(TC.class): @TC(value=[@T(value=3), @T(value=4)]) \n" +
			"java.lang.Object.getAnnotationByType(TC.class): @TC(value=[@T(value=3), @T(value=4)]) \n" +
			"java.lang.Comparable<?>.getAnnotationByType(T.class): @T(value=5) @T(value=6) \n" +
			"java.lang.Comparable<?>.getAnnotation(TC.class): @TC(value=[@T(value=5), @T(value=6)]) \n" +
			"java.lang.Comparable<?>.getAnnotationByType(TC.class): @TC(value=[@T(value=5), @T(value=6)])"),
			null,
			true,
			new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
	}
	// Test that repeated annotations show up at various sites, both type use and declaration.
	public void testVariousSites() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Annotation;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Repeatable;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.Target;\n" +
				"import java.lang.reflect.AnnotatedArrayType;\n" +
				"import java.lang.reflect.AnnotatedElement;\n" +
				"import java.lang.reflect.AnnotatedParameterizedType;\n" +
				"import java.lang.reflect.AnnotatedType;\n" +
				"import java.lang.reflect.Constructor;\n" +
				"import java.lang.reflect.Field;\n" +
				"import java.lang.reflect.Method;\n" +
				"import java.lang.reflect.TypeVariable;\n" +
				"\n" +
				"import static java.lang.annotation.RetentionPolicy.*;\n" +
				"\n" +
				"@Retention(RUNTIME)\n" +
				"@Target({ElementType.TYPE_USE, ElementType.TYPE, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.METHOD})\n" +
				"@interface TC {\n" +
				"  public T[] value();\n" +
				"}\n" +
				"@Retention(RUNTIME)\n" +
				"@Repeatable(TC.class)\n" +
				"@Target({ElementType.TYPE_USE, ElementType.TYPE, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.METHOD})\n" +
				"@interface T {\n" +
				"  public int value() default -1;\n" +
				"}\n" +
				"\n" +
				"interface I {\n" +
				"}\n" +
				"\n" +
				"@T(1) @T(2)\n" +
				"public class X<@T(3) @T(4) K extends @T(5) @T(6) Object & java.lang.@T(7) @T(8) Comparable<?>, @T(9) @T(10) V> extends @T(11) @T(12) Object implements @T(13) @T(14) I {\n" +
				"  public @T(15) @T(16) X<@T(17) @T(18) String, @T(19) @T(20) Integer> field;\n" +
				"  @T(21) @T(22)\n" +
				"  public <@T(23) @T(24) Q> X @T(25) @T(26) [] method(@T(27) @T(28) X<K, V> this, \n" +
				"		                                             @T(29) @T(30) X<@T(31) @T(32) String, String> that) throws @T(33) @T(34) NullPointerException {\n" +
				"	  return null;\n" +
				"  }\n" +
				"  @T(35) @T(36)\n" +
				"  public X() {\n" +
				"	  \n" +
				"  }\n" +
				"  @T(37) @T(48)\n" +
				"  public class MemberType {\n" +
				"	  \n" +
				"  }\n" +
				"  \n" +
				"  public static void main(String args[]) {\n" +
				"	Class<X> xc = X.class;  \n" +
				"  	printAnnotations(\"Class: \" + \"X.class\", xc);\n" +
				"  	TypeVariable<Class<X>>[] typeParameters = xc.getTypeParameters();\n" +
				"  	for (TypeVariable<?> t: typeParameters) {\n" +
				"  		printAnnotations(\"Type Parameter: \" + t.getName(), t);\n" +
				"  		AnnotatedType[] bounds = t.getAnnotatedBounds();\n" +
				"  		for (AnnotatedType bound : bounds) {\n" +
				"  			printAnnotations(\"Type parameter bound: \" + bound.getType().getTypeName(), bound);\n" +
				"  		}\n" +
				"  	}\n" +
				"  	AnnotatedType annotatedSuperclass = xc.getAnnotatedSuperclass();\n" +
				"  	printAnnotations(\"Superclass: \" + annotatedSuperclass.getType().getTypeName(), annotatedSuperclass);\n" +
				"  	\n" +
				"  	AnnotatedType [] annotatedSuperInterfaces = xc.getAnnotatedInterfaces();\n" +
				"  	printAnnotations(\"Superinterface: \" + annotatedSuperInterfaces[0].getType().getTypeName(), annotatedSuperInterfaces[0]);\n" +
				"  	\n" +
				"  	for (Field field: xc.getFields()) {\n" +
				"  		printAnnotations(\"Field: \" + field.getName(), field);\n" +
				"  		AnnotatedParameterizedType fType = (AnnotatedParameterizedType) field.getAnnotatedType();\n" +
				"  		for (AnnotatedType typeArgumentType : fType.getAnnotatedActualTypeArguments())\n" +
				"  			printAnnotations(\"Field Type argument: \" + typeArgumentType.getType().getTypeName(), typeArgumentType);\n" +
				"  			\n" +
				"  	}\n" +
				"  	for (Method method: xc.getMethods()) {\n" +
				"  		switch (method.getName()) {\n" +
				"  		case \"method\"  :\n" +
				"  			printAnnotations(method.getName(), method);\n" +
				"  			AnnotatedArrayType mType = (AnnotatedArrayType) method.getAnnotatedReturnType();\n" +
				"  			printAnnotations(\"Method return type: \" + mType.getType().getTypeName(), mType);\n" +
				"  			AnnotatedType mTypeEtype = mType.getAnnotatedGenericComponentType();\n" +
				"  			printAnnotations(\"Method return type, element type: \" + mTypeEtype.getType().getTypeName(), mTypeEtype);\n" +
				"  			TypeVariable<Method>[] typeParameters2 = method.getTypeParameters();\n" +
				"  		  	for (TypeVariable<?> t: typeParameters2) {\n" +
				"  		  		printAnnotations(\"Method Type Parameter: \" + t.getName(), t);\n" +
				"  		  	}\n" +
				"  		  	AnnotatedType annotatedReceiverType = method.getAnnotatedReceiverType();\n" +
				"  		  	printAnnotations(\"Receiver: \", annotatedReceiverType);\n" +
				"  		  	AnnotatedType[] annotatedParameterTypes = method.getAnnotatedParameterTypes();\n" +
				"  		  	for (AnnotatedType annotatedParameterType : annotatedParameterTypes) {\n" +
				"  		  		printAnnotations(\"Parameter: \", annotatedParameterType);\n" +
				"  		  	}\n" +
				"  		  	AnnotatedType[] annotatedExceptionTypes = method.getAnnotatedExceptionTypes();\n" +
				"  		  	for (AnnotatedType annotatedType : annotatedExceptionTypes) {\n" +
				"				printAnnotations(\"Exception type: \", annotatedType);\n" +
				"			}\n" +
				"  			break;\n" +
				"  		}\n" +
				"  	}\n" +
				"  	for (Constructor<?> constructor : xc.getConstructors()) {\n" +
				"  		printAnnotations(\"Constructor: \", constructor);\n" +
				"  	}\n" +
				"  	// don't know how to get member classes.\n" +
				"  }\n" +
				"  \n" +
				"  static void printAnnotations(String name, AnnotatedElement element) {\n" +
				"	  int [] iterations = { 0, 1 };\n" +
				"	  for (int i : iterations) {\n" +
				"		  Class<? extends Annotation> annotation = i == 0 ? T.class : TC.class;\n" +
				"		  for (int j: iterations) {\n" +
				"			  Annotation [] annotations = j == 0 ? new Annotation [] { element.getAnnotation(annotation) } : element.getAnnotationsByType(annotation);\n" +
				"			  if (annotations.length == 0 || (annotations.length == 1 && annotations[0] == null)) continue;\n" +
				"			  System.out.print(name + (j == 0 ? \".getAnnotation(\" : \".getAnnotationByType(\") + annotation.getName() + \".class): \");\n" +
				"			  for (Annotation a : annotations) {\n" +
				"				  System.out.print(normalizeAnnotation(a) + \" \");\n" +
				"			  }\n" +
				"			  System.out.print(\"\\n\");\n" +
				"		  }\n" +
				"	  }\n" +
				"  }\n" +
				"  static String normalizeAnnotation(Annotation a) {\n" +
				" 		if (a == null) return null;\n" +
				"	  String str = a.toString();\n" +
				"	  str = str.replace(\"value={@\", \"value=[@\");\n" +
				"	  str = str.replace(\")}\", \")]\");\n" +
				"	  return str;\n" +
				"  }\n" +
				"}\n"

			},
			normalizeAnnotationString("Class: X.class.getAnnotationByType(T.class): @T(value=1) @T(value=2) \n" +
			"Class: X.class.getAnnotation(TC.class): @TC(value=[@T(value=1), @T(value=2)]) \n" +
			"Class: X.class.getAnnotationByType(TC.class): @TC(value=[@T(value=1), @T(value=2)]) \n" +
			"Type Parameter: K.getAnnotationByType(T.class): @T(value=3) @T(value=4) \n" +
			"Type Parameter: K.getAnnotation(TC.class): @TC(value=[@T(value=3), @T(value=4)]) \n" +
			"Type Parameter: K.getAnnotationByType(TC.class): @TC(value=[@T(value=3), @T(value=4)]) \n" +
			"Type parameter bound: java.lang.Object.getAnnotationByType(T.class): @T(value=5) @T(value=6) \n" +
			"Type parameter bound: java.lang.Object.getAnnotation(TC.class): @TC(value=[@T(value=5), @T(value=6)]) \n" +
			"Type parameter bound: java.lang.Object.getAnnotationByType(TC.class): @TC(value=[@T(value=5), @T(value=6)]) \n" +
			"Type parameter bound: java.lang.Comparable<?>.getAnnotationByType(T.class): @T(value=7) @T(value=8) \n" +
			"Type parameter bound: java.lang.Comparable<?>.getAnnotation(TC.class): @TC(value=[@T(value=7), @T(value=8)]) \n" +
			"Type parameter bound: java.lang.Comparable<?>.getAnnotationByType(TC.class): @TC(value=[@T(value=7), @T(value=8)]) \n" +
			"Type Parameter: V.getAnnotationByType(T.class): @T(value=9) @T(value=10) \n" +
			"Type Parameter: V.getAnnotation(TC.class): @TC(value=[@T(value=9), @T(value=10)]) \n" +
			"Type Parameter: V.getAnnotationByType(TC.class): @TC(value=[@T(value=9), @T(value=10)]) \n" +
			"Superclass: java.lang.Object.getAnnotationByType(T.class): @T(value=11) @T(value=12) \n" +
			"Superclass: java.lang.Object.getAnnotation(TC.class): @TC(value=[@T(value=11), @T(value=12)]) \n" +
			"Superclass: java.lang.Object.getAnnotationByType(TC.class): @TC(value=[@T(value=11), @T(value=12)]) \n" +
			"Superinterface: I.getAnnotationByType(T.class): @T(value=13) @T(value=14) \n" +
			"Superinterface: I.getAnnotation(TC.class): @TC(value=[@T(value=13), @T(value=14)]) \n" +
			"Superinterface: I.getAnnotationByType(TC.class): @TC(value=[@T(value=13), @T(value=14)]) \n" +
			"Field: field.getAnnotationByType(T.class): @T(value=15) @T(value=16) \n" +
			"Field: field.getAnnotation(TC.class): @TC(value=[@T(value=15), @T(value=16)]) \n" +
			"Field: field.getAnnotationByType(TC.class): @TC(value=[@T(value=15), @T(value=16)]) \n" +
			"Field Type argument: java.lang.String.getAnnotationByType(T.class): @T(value=17) @T(value=18) \n" +
			"Field Type argument: java.lang.String.getAnnotation(TC.class): @TC(value=[@T(value=17), @T(value=18)]) \n" +
			"Field Type argument: java.lang.String.getAnnotationByType(TC.class): @TC(value=[@T(value=17), @T(value=18)]) \n" +
			"Field Type argument: java.lang.Integer.getAnnotationByType(T.class): @T(value=19) @T(value=20) \n" +
			"Field Type argument: java.lang.Integer.getAnnotation(TC.class): @TC(value=[@T(value=19), @T(value=20)]) \n" +
			"Field Type argument: java.lang.Integer.getAnnotationByType(TC.class): @TC(value=[@T(value=19), @T(value=20)]) \n" +
			"method.getAnnotationByType(T.class): @T(value=21) @T(value=22) \n" +
			"method.getAnnotation(TC.class): @TC(value=[@T(value=21), @T(value=22)]) \n" +
			"method.getAnnotationByType(TC.class): @TC(value=[@T(value=21), @T(value=22)]) \n" +
			"Method return type: X[].getAnnotationByType(T.class): @T(value=25) @T(value=26) \n" +
			"Method return type: X[].getAnnotation(TC.class): @TC(value=[@T(value=25), @T(value=26)]) \n" +
			"Method return type: X[].getAnnotationByType(TC.class): @TC(value=[@T(value=25), @T(value=26)]) \n" +
			"Method return type, element type: X.getAnnotationByType(T.class): @T(value=21) @T(value=22) \n" +
			"Method return type, element type: X.getAnnotation(TC.class): @TC(value=[@T(value=21), @T(value=22)]) \n" +
			"Method return type, element type: X.getAnnotationByType(TC.class): @TC(value=[@T(value=21), @T(value=22)]) \n" +
			"Method Type Parameter: Q.getAnnotationByType(T.class): @T(value=23) @T(value=24) \n" +
			"Method Type Parameter: Q.getAnnotation(TC.class): @TC(value=[@T(value=23), @T(value=24)]) \n" +
			"Method Type Parameter: Q.getAnnotationByType(TC.class): @TC(value=[@T(value=23), @T(value=24)]) \n" +
			"Receiver: .getAnnotationByType(T.class): @T(value=27) @T(value=28) \n" +
			"Receiver: .getAnnotation(TC.class): @TC(value=[@T(value=27), @T(value=28)]) \n" +
			"Receiver: .getAnnotationByType(TC.class): @TC(value=[@T(value=27), @T(value=28)]) \n" +
			"Parameter: .getAnnotationByType(T.class): @T(value=29) @T(value=30) \n" +
			"Parameter: .getAnnotation(TC.class): @TC(value=[@T(value=29), @T(value=30)]) \n" +
			"Parameter: .getAnnotationByType(TC.class): @TC(value=[@T(value=29), @T(value=30)]) \n" +
			"Exception type: .getAnnotationByType(T.class): @T(value=33) @T(value=34) \n" +
			"Exception type: .getAnnotation(TC.class): @TC(value=[@T(value=33), @T(value=34)]) \n" +
			"Exception type: .getAnnotationByType(TC.class): @TC(value=[@T(value=33), @T(value=34)]) \n" +
			"Constructor: .getAnnotationByType(T.class): @T(value=35) @T(value=36) \n" +
			"Constructor: .getAnnotation(TC.class): @TC(value=[@T(value=35), @T(value=36)]) \n" +
			"Constructor: .getAnnotationByType(TC.class): @TC(value=[@T(value=35), @T(value=36)])"),
			null,
			true,
			new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
	}

	// Test that bad container specifications are handled properly.
	public void testBadContainerType() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.Repeatable;\n" +
				"@Repeatable(X.class)\n" +
				"@interface T {\n" +
				"  public int value() default -1;\n" +
				"}\n" +
				"public class X {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	@Repeatable(X.class)\n" +
			"	            ^^^^^^^\n" +
			"Type mismatch: cannot convert from Class<X> to Class<? extends Annotation>\n" +
			"----------\n");
	}
	// Test unspecified target.
	public void testUnspecifiedTarget() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Repeatable;\n" +
				"import java.lang.annotation.Target;\n" +
				"\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface TC {\n" +
				"	T [] value();\n" +
				"}\n" +
				"\n" +
				"@Repeatable(TC.class)\n" +
				"@interface T {\n" +
				"}\n" +
				"\n" +
				"@T @T\n" +
				"public class X { \n" +
				"	X f;\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	@Repeatable(TC.class)\n" +
			"	            ^^^^^^^^\n" +
			"The container annotation type @TC is allowed at targets where the repeatable annotation type @T is not: TYPE_USE\n" +
			"----------\n");
	}
	// Test unspecified target.
	public void testUnspecifiedTarget2() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Repeatable;\n" +
				"import java.lang.annotation.Target;\n" +
				"\n" +
				"@Target(ElementType.TYPE_PARAMETER)\n" +
				"@interface TC {\n" +
				"	T [] value();\n" +
				"}\n" +
				"\n" +
				"@Repeatable(TC.class)\n" +
				"@interface T {\n" + // no target, so all declaration targets allowed including TYPE_PARAMETER
				"}\n" +
				"\n" +
				"@T @T\n" +
				"public class X { \n" +
				"	X f;\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 14)\n" +
			"	@T @T\n" +
			"	^^\n" +
			"The annotation @T cannot be repeated at this location since its container annotation type @TC is disallowed at this location\n" +
			"----------\n");
	}
	public void testDeprecation() {
		this.runNegativeTest(
			new String[] {
				"TC.java",
				"@Deprecated\n" +
				"public @interface TC {\n" +
				"  public T[] value();\n" +
				"}\n",
				"T.java",
				"@java.lang.annotation.Repeatable(TC.class)\n" +
				"@interface T {\n" +
				"  public int value() default -1;\n" +
				"}\n" +
				"interface I<@T(1) @T(2) K> {\n" +
				"}\n"
			},
			"----------\n" +
			"1. WARNING in T.java (at line 1)\n" +
			"	@java.lang.annotation.Repeatable(TC.class)\n" +
			"	                                 ^^\n" +
			"The type TC is deprecated\n" +
			"----------\n" +
			"2. WARNING in T.java (at line 5)\n" +
			"	interface I<@T(1) @T(2) K> {\n" +
			"	            ^^\n" +
			"The type TC is deprecated\n");
	}
	public void testDeprecation2() { // verify that deprecation warning does not show up when the deprecated element is used in the same file defining it.
		this.runNegativeTest(
			new String[] {
				"T.java",
				"@Deprecated\n" +
				"@interface TC {\n" +
				"  public T[] value();\n" +
				"}\n" +
				"@java.lang.annotation.Repeatable(TC.class)\n" +
				"@interface T {\n" +
				"  public int value() default -1;\n" +
				"}\n" +
				"interface I extends @T(1) Runnable {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in T.java (at line 9)\n" +
			"	interface I extends @T(1) Runnable {\n" +
			"	                    ^^\n" +
			"Annotation types that do not specify explicit target element types cannot be applied here\n" +
			"----------\n");
	}

	// 419209: [1.8] Repeating container annotations should be rejected in the presence of annotation it contains
	public void testRepeatableWithContaining1() {
		this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
				"A.java",
				"@interface FooContainerContainer {\n" +
				"  public FooContainer[] value();\n" +
				"}\n" +
				"@java.lang.annotation.Repeatable(FooContainerContainer.class)\n" +
				"@interface FooContainer {\n" +
				"  public Foo[] value();\n" +
				"}\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@interface Foo {\n" +
				"  public int value() default -1;\n" +
				"}\n" +
				"@FooContainer({@Foo(1)}) @FooContainer({@Foo(2)}) @Foo(3) class A {}\n"
			},
			"----------\n" +
			"1. WARNING in A.java (at line 12)\n" +
			"	@FooContainer({@Foo(1)}) @FooContainer({@Foo(2)}) @Foo(3) class A {}\n" +
			"	                                                  ^^^^\n" +
			"The repeatable annotation @Foo may not be present where its container annotation type @FooContainer is repeated\n" +
			"----------\n");
	}
	// 419209: [1.8] Repeating container annotations should be rejected in the presence of annotation it contains
	public void testRepeatableWithContaining2() {
		this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
				"A.java",
				"@interface FooContainerContainer {\n" +
				"  public FooContainer[] value();\n" +
				"}\n" +
				"@java.lang.annotation.Repeatable(FooContainerContainer.class)\n" +
				"@interface FooContainer {\n" +
				"  public Foo[] value();\n" +
				"}\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@interface Foo {\n" +
				"  public int value() default -1;\n" +
				"}\n" +
				"@Foo(1) @FooContainer({@Foo(2)}) @FooContainer({@Foo(3)}) class A {}\n"
			},
			"----------\n" +
			"1. WARNING in A.java (at line 12)\n" +
			"	@Foo(1) @FooContainer({@Foo(2)}) @FooContainer({@Foo(3)}) class A {}\n" +
			"	^^^^\n" +
			"The repeatable annotation @Foo may not be present where its container annotation type @FooContainer is repeated\n" +
			"----------\n");
	}
	// 419209: [1.8] Repeating container annotations should be rejected in the presence of annotation it contains
	public void testRepeatableWithContaining3() {
		this.runNegativeTest(
			false /* skipJavac */,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
			new String[] {
				"A.java",
				"@interface FooContainerContainer {\n" +
				"  public FooContainer[] value();\n" +
				"}\n" +
				"@java.lang.annotation.Repeatable(FooContainerContainer.class)\n" +
				"@interface FooContainer {\n" +
				"  public Foo[] value();\n" +
				"}\n" +
				"@java.lang.annotation.Repeatable(FooContainer.class)\n" +
				"@interface Foo {\n" +
				"  public int value() default -1;\n" +
				"}\n" +
				"@FooContainer({@Foo(2)}) @Foo(1) @FooContainer({@Foo(3)}) class A {}\n"
			},
			"----------\n" +
			"1. WARNING in A.java (at line 12)\n" +
			"	@FooContainer({@Foo(2)}) @Foo(1) @FooContainer({@Foo(3)}) class A {}\n" +
			"	                         ^^^^\n" +
			"The repeatable annotation @Foo may not be present where its container annotation type @FooContainer is repeated\n" +
			"----------\n");
	}
	// check repeated occurrence of annotation where annotation container is not valid for the target
	public void testRepeatingAnnotationsWithoutTarget() {
		this.runNegativeTest(
			new String[] {
				"FooContainer.java",
				"public @interface FooContainer {\n" +
				"	Foo[] value();\n" +
				"}\n",
				"Foo.java",
				"@java.lang.annotation.Repeatable(FooContainer.class) public @interface Foo {\n" +
				"}\n",
				"X.java",
				"public class X<@Foo @Foo T> extends @Foo @Foo Object {\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	public class X<@Foo @Foo T> extends @Foo @Foo Object {\n" +
			"	                                    ^^^^\n" +
			"Annotation types that do not specify explicit target element types cannot be applied here\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	public class X<@Foo @Foo T> extends @Foo @Foo Object {\n" +
			"	                                    ^^^^\n" +
			"The annotation @Foo cannot be repeated at this location since its container annotation type @FooContainer is disallowed at this location\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 1)\n" +
			"	public class X<@Foo @Foo T> extends @Foo @Foo Object {\n" +
			"	                                         ^^^^\n" +
			"Annotation types that do not specify explicit target element types cannot be applied here\n" +
			"----------\n");
	}
}
