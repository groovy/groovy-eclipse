/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class JavadocTest_1_5 extends JavadocTest {

	String docCommentSupport = CompilerOptions.ENABLED;
	String reportInvalidJavadoc = CompilerOptions.ERROR;
	String reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
	String reportMissingJavadocTags = CompilerOptions.ERROR;
	String reportMissingJavadocTagsOverriding = CompilerOptions.ENABLED;
	String reportMissingJavadocComments = null;
	String reportMissingJavadocCommentsVisibility = null;

	public JavadocTest_1_5(String name) {
		super(name);
	}

	public static Class testClass() {
		return JavadocTest_1_5.class;
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_PREFIX = "testBug95521";
//		TESTS_NAMES = new String[] { "testBug331872d" };
//		TESTS_NUMBERS = new int[] { 101283 };
//		TESTS_RANGE = new int[] { 23, -1 };
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_5);
	}

	@Override
	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_DocCommentSupport, this.docCommentSupport);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, this.reportInvalidJavadoc);
		if (!CompilerOptions.IGNORE.equals(this.reportInvalidJavadoc)) {
			options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, this.reportInvalidJavadocVisibility);
		}
		if (this.reportMissingJavadocComments != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportMissingJavadocComments);
		else
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportInvalidJavadoc);
		if (this.reportMissingJavadocCommentsVisibility != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocCommentsVisibility, this.reportMissingJavadocCommentsVisibility);
		if (this.reportMissingJavadocTags != null)  {
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportMissingJavadocTags);
			if (this.reportMissingJavadocTagsOverriding != null) {
				options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsOverriding, this.reportMissingJavadocTagsOverriding);
			}
		} else {
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportInvalidJavadoc);
		}
		options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsMethodTypeParameters, CompilerOptions.ENABLED);
		return options;
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.docCommentSupport = CompilerOptions.ENABLED;
		this.reportInvalidJavadoc = CompilerOptions.ERROR;
		this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
		this.reportMissingJavadocTags = CompilerOptions.ERROR;
		this.reportMissingJavadocTagsOverriding = CompilerOptions.ENABLED;
		this.reportMissingJavadocComments = CompilerOptions.IGNORE;
	}

	/**
	 * Test fix for bug 70891: [1.5][javadoc] Compiler should accept new 1.5 syntax for @param
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=70891">70891</a>
	 * These two tests fail for 1.5 source level but should pass for 1.3 or 1.4
	 * @see JavadocTest_1_4
	 */
	/* (non-Javadoc)
	 * Test @param for generic class type parameter
	 */
	public void test003() {
		this.runConformTest(
			new String[] {
				"X.java",
					" /**\n" +
					"  * Valid type parameter reference\n" +
					"  * @param <E> Type\n" +
					"  */\n" +
					" public class X<E> {}"
			}
		);
	}
	public void test004() {
		this.runConformTest(
			new String[] {
				"X.java",
					" /**\n" +
					"  * Valid type parameter reference\n" +
					"  * @param <E> Type extends RuntimeException\n" +
					"  */\n" +
					" public class X<E extends RuntimeException> {}"
			}
		);
	}
	public void test005() {
		this.runConformTest(
			new String[] {
				"X.java",
					" /**\n" +
					"  * Valid type parameter reference\n" +
					"  * @param <T> Type parameter 1\n" +
					"  * @param <U> Type parameter 2\n" +
					"  * @param <V> Type parameter 3\n" +
					"  */\n" +
					" public class X<T, U, V> {}"
			}
		);
	}
	public void test006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" +
					"  * Invalid type parameter reference\n" +
					"  * @param <E> Type parameter\n" +
					"  */\n" +
					" public class X {}",
			},
			"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	* @param <E> Type parameter\n" +
				"	   ^^^^^\n" +
				"Javadoc: Unexpected tag\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void test007() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" +
					"  * Invalid type parameter reference\n" +
					"  * @param <E> Type parameter\n" +
					"  */\n" +
					" public class X<E, F> {}",
			},
			"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	public class X<E, F> {}\n" +
				"	                  ^\n" +
				"Javadoc: Missing tag for parameter F\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void test008() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" +
					"  * Invalid type parameter reference\n" +
					"  * @param <T> Type parameter 1\n" +
					"  * @param <U> Type parameter 2\n" +
					"  * @param <V> Type parameter 3\n" +
					"  */\n" +
					" public class X<T> {}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	* @param <U> Type parameter 2\n" +
				"	          ^\n" +
				"Javadoc: U cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	* @param <V> Type parameter 3\n" +
				"	          ^\n" +
				"Javadoc: V cannot be resolved to a type\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void test009() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" +
					"  * Invalid type parameter reference\n" +
					"  * @param <T> Type parameter 1\n" +
					"  * @param <X> Type parameter 2\n" +
					"  * @param <U> Type parameter 2\n" +
					"  * @param <E> Type parameter 2\n" +
					"  * @param <V> Type parameter 3\n" +
					"  */\n" +
					" public class X<T, U, V> {}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	* @param <X> Type parameter 2\n" +
				"	          ^\n" +
				"Javadoc: Parameter X is not declared\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	* @param <E> Type parameter 2\n" +
				"	          ^\n" +
				"Javadoc: E cannot be resolved to a type\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void test010() {
		this.runConformTest(
			new String[] {
				"X.java",
					" /**\n" +
					"  * Valid type parameter reference\n" +
					"  * @param <V> Type parameter 3\n" +
					"  * @param <U> Type parameter 2\n" +
					"  * @param <T> Type parameter 1\n" +
					"  */\n" +
					" public class X<T, U, V> {}"
			}
		);
	}
	public void test011() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" +
					"  * Invalid type parameter reference\n" +
					"  * @param <U> Type parameter 1\n" +
					"  * @param <E> Type parameter 2\n" +
					"  * @param <V> Type parameter 2\n" +
					"  * @param <U> Type parameter 2\n" +
					"  * @param <T> Type parameter 3\n" +
					"  */\n" +
					" public class X<T, U, V> {}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	* @param <E> Type parameter 2\n" +
				"	          ^\n" +
				"Javadoc: E cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	* @param <U> Type parameter 2\n" +
				"	          ^\n" +
				"Javadoc: Duplicate tag for parameter\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void test012() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" +
					"  * Invalid type parameter reference\n" +
					"  */\n" +
					" public class X<T, U, V> {}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	public class X<T, U, V> {}\n" +
				"	               ^\n" +
				"Javadoc: Missing tag for parameter T\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	public class X<T, U, V> {}\n" +
				"	                  ^\n" +
				"Javadoc: Missing tag for parameter U\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 4)\n" +
				"	public class X<T, U, V> {}\n" +
				"	                     ^\n" +
				"Javadoc: Missing tag for parameter V\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void test013() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" +
					"  * Invalid type parameter reference\n" +
					"  * @param <T> Type parameter 3\n" +
					"  */\n" +
					" public class X<T, U, V> {}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	public class X<T, U, V> {}\n" +
				"	                  ^\n" +
				"Javadoc: Missing tag for parameter U\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	public class X<T, U, V> {}\n" +
				"	                     ^\n" +
				"Javadoc: Missing tag for parameter V\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void test014() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" +
					"  * Invalid type parameter reference\n" +
					"  * @param <U> Type parameter 3\n" +
					"  */\n" +
					" public class X<T, U, V> {}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	public class X<T, U, V> {}\n" +
				"	               ^\n" +
				"Javadoc: Missing tag for parameter T\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	public class X<T, U, V> {}\n" +
				"	                     ^\n" +
				"Javadoc: Missing tag for parameter V\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void test015() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" +
					"  * Invalid type parameter reference\n" +
					"  * @param <U> Type parameter 3\n" +
					"  * @param <V> Type parameter 3\n" +
					"  */\n" +
					" public class X<T, U, V> {}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	public class X<T, U, V> {}\n" +
				"	               ^\n" +
				"Javadoc: Missing tag for parameter T\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void test016() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" +
					"  * Invalid type parameter reference\n" +
					"  * @param <T> Type parameter 3\n" +
					"  * @param <V> Type parameter 3\n" +
					"  */\n" +
					" public class X<T, U, V> {}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	public class X<T, U, V> {}\n" +
				"	                  ^\n" +
				"Javadoc: Missing tag for parameter U\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void test017() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" +
					"  * Invalid type parameter reference\n" +
					"  * @param <T> Type parameter 3\n" +
					"  * @param <U> Type parameter 3\n" +
					"  */\n" +
					" public class X<T, U, V> {}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	public class X<T, U, V> {}\n" +
				"	                     ^\n" +
				"Javadoc: Missing tag for parameter V\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void test018() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" +
					"  * Invalid type parameter reference\n" +
					"  * @param <V> Type parameter 3\n" +
					"  */\n" +
					" public class X<T, U, V> {}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	public class X<T, U, V> {}\n" +
				"	               ^\n" +
				"Javadoc: Missing tag for parameter T\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	public class X<T, U, V> {}\n" +
				"	                  ^\n" +
				"Javadoc: Missing tag for parameter U\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void test019() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" +
					"  * Invalid type parameter reference\n" +
					"  * @param <V> Type parameter 2\n" +
					"  * @param <X> Type parameter 2\n" +
					"  * @param <U> Type parameter 1\n" +
					"  * @param <E> Type parameter 2\n" +
					"  * @param <U> Type parameter 2\n" +
					"  */\n" +
					" public class X<T, U, V> {}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	* @param <X> Type parameter 2\n" +
				"	          ^\n" +
				"Javadoc: Parameter X is not declared\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	* @param <E> Type parameter 2\n" +
				"	          ^\n" +
				"Javadoc: E cannot be resolved to a type\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 7)\n" +
				"	* @param <U> Type parameter 2\n" +
				"	          ^\n" +
				"Javadoc: Duplicate tag for parameter\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 9)\n" +
				"	public class X<T, U, V> {}\n" +
				"	               ^\n" +
				"Javadoc: Missing tag for parameter T\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void test020() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" +
					"  * Invalid type parameter reference\n" +
					"  * @param <V> Type parameter 2\n" +
					"  * @param\n" +
					"  * @param <U> Type parameter 1\n" +
					"  */\n" +
					" public class X<T, U, V> {}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	* @param\n" +
				"	   ^^^^^\n" +
				"Javadoc: Missing parameter name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 7)\n" +
				"	public class X<T, U, V> {}\n" +
				"	               ^\n" +
				"Javadoc: Missing tag for parameter T\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void test021() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" +
					"  * Invalid type parameter reference: compile error\n" +
					"  * @param <T> Type parameter 2\n" +
					"  * @param <V> Type parameter 2\n" +
					"  * @param <U> Type parameter 1\n" +
					"  */\n" +
					" public class X<T, , V> {}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	* @param <V> Type parameter 2\n" +
				"	          ^\n" +
				"Javadoc: V cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	* @param <U> Type parameter 1\n" +
				"	          ^\n" +
				"Javadoc: U cannot be resolved to a type\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 7)\n" +
				"	public class X<T, , V> {}\n" +
				"	                  ^\n" +
				"Syntax error on token \",\", delete this token\n" +
				"----------\n"
		);
	}
	public void test022() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" +
					"  * Invalid type parameter reference: compile error\n" +
					"  * @param <T> Type parameter 2\n" +
					"  * @param <V> Type parameter 2\n" +
					"  * @param <U> Type parameter 1\n" +
					"  */\n" +
					" public class X<T, U, V extend Exception> {}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	public class X<T, U, V extend Exception> {}\n" +
				"	                       ^^^^^^\n" +
				"Syntax error on token \"extend\", extends expected\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 7)\n" +
				"	public class X<T, U, V extend Exception> {}\n" +
				"	                       ^^^^^^\n" +
				"extend cannot be resolved to a type\n" +
				"----------\n"
		);
	}

	/* (non-Javadoc)
	 * Test @param for generic method type parameter
	 */
	public void test023() {
		this.runConformTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" +
					"	 * Valid type parameter reference\n" +
					"	 * @param <E> Type\n" +
					"	 */\n" +
					"	public <E> void foo() {}\n" +
					"}"
			}
		);
	}
	public void test024() {
		this.runConformTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" +
					"	 * Valid type parameter reference\n" +
					"	 * @param <E> Type extends RuntimeException\n" +
					"	 * @param val int\n" +
					"	 * @param obj Object\n" +
					"	 */\n" +
					"	public <E extends RuntimeException> void foo(int val, Object obj) {}\n" +
					"}"
			}
		);
	}
	public void test025() {
		this.runConformTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" +
					"	 * Valid type parameter reference\n" +
					"	 * @param val int\n" +
					"	 * @param obj Object\n" +
					"	 * @param <T> Type parameter 1\n" +
					"	 * @param <U> Type parameter 2\n" +
					"	 * @param <V> Type parameter 3\n" +
					"	 */\n" +
					"	public <T, U, V> void foo(int val, Object obj) {}\n" +
					"}"
			}
		);
	}
	public void test026() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" +
					"	 * Invalid type parameter reference\n" +
					"	 * @param val int\n" +
					"	 * @param <E> Type parameter\n" +
					"	 * @param obj Object\n" +
					"	 */\n" +
					"	public void foo(int val, Object obj) {}\n" +
					"}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	* @param <E> Type parameter\n" +
				"	   ^^^^^\n" +
				"Javadoc: Unexpected tag\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void test027() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" +
					"	 * Invalid type parameter reference\n" +
					"	 * @param <E> Type parameter\n" +
					"	 */\n" +
					"	public <E, F> void foo(int val, Object obj) {}\n" +
					"}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	public <E, F> void foo(int val, Object obj) {}\n" +
				"	           ^\n" +
				"Javadoc: Missing tag for parameter F\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	public <E, F> void foo(int val, Object obj) {}\n" +
				"	                           ^^^\n" +
				"Javadoc: Missing tag for parameter val\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 6)\n" +
				"	public <E, F> void foo(int val, Object obj) {}\n" +
				"	                                       ^^^\n" +
				"Javadoc: Missing tag for parameter obj\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void test028() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" +
					"	 * Invalid type parameter reference\n" +
					"	 * @param <T> Type parameter 1\n" +
					"	 * @param <U> Type parameter 2\n" +
					"	 * @param <V> Type parameter 3\n" +
					"	 * @param xxx int\n" +
					"	 * @param Obj Object\n" +
					"	 */\n" +
					"	public <T> void foo(int val, Object obj) {}\n" +
					"}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	* @param <U> Type parameter 2\n" +
				"	          ^\n" +
				"Javadoc: U cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	* @param <V> Type parameter 3\n" +
				"	          ^\n" +
				"Javadoc: V cannot be resolved to a type\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 7)\n" +
				"	* @param xxx int\n" +
				"	         ^^^\n" +
				"Javadoc: Parameter xxx is not declared\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 8)\n" +
				"	* @param Obj Object\n" +
				"	         ^^^\n" +
				"Javadoc: Parameter Obj is not declared\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 10)\n" +
				"	public <T> void foo(int val, Object obj) {}\n" +
				"	                        ^^^\n" +
				"Javadoc: Missing tag for parameter val\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 10)\n" +
				"	public <T> void foo(int val, Object obj) {}\n" +
				"	                                    ^^^\n" +
				"Javadoc: Missing tag for parameter obj\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void test029() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" +
					"	 * Invalid type parameter reference\n" +
					"	 * @param <T> Type parameter 1\n" +
					"	 * @param <X> Type parameter 2\n" +
					"	 * @param val int\n" +
					"	 * @param <U> Type parameter 2\n" +
					"	 * @param <E> Type parameter 2\n" +
					"	 * @param obj Object\n" +
					"	 * @param <V> Type parameter 3\n" +
					"	 */\n" +
					"	public <T, U, V> void foo(int val, Object obj) {}\n" +
					"}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	* @param <X> Type parameter 2\n" +
				"	          ^\n" +
				"Javadoc: Parameter X is not declared\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 8)\n" +
				"	* @param <E> Type parameter 2\n" +
				"	          ^\n" +
				"Javadoc: E cannot be resolved to a type\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void test030() {
		this.runConformTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" +
					"	 * Valid type parameter reference\n" +
					"	 * @param <V> Type parameter 3\n" +
					"	 * @param obj Object\n" +
					"	 * @param <U> Type parameter 2\n" +
					"	 * @param val int\n" +
					"	 * @param <T> Type parameter 1\n" +
					"	 */\n" +
					"	public <T, U, V> void foo(int val, Object obj) {}\n" +
					"}"
			}
		);
	}
	public void test031() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" +
					"	 * Invalid type parameter reference\n" +
					"	 */\n" +
					"	public <T, U, V> void foo(int val, Object obj) {}\n" +
					"}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	        ^\n" +
				"Javadoc: Missing tag for parameter T\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	           ^\n" +
				"Javadoc: Missing tag for parameter U\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	              ^\n" +
				"Javadoc: Missing tag for parameter V\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 5)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	                              ^^^\n" +
				"Javadoc: Missing tag for parameter val\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 5)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	                                          ^^^\n" +
				"Javadoc: Missing tag for parameter obj\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void test032() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" +
					"	 * Invalid type parameter reference\n" +
					"	 * @param <T> Type parameter 3\n" +
					"	 * @param val int\n" +
					"	 */\n" +
					"	public <T, U, V> void foo(int val, Object obj) {}\n" +
					"}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 7)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	           ^\n" +
				"Javadoc: Missing tag for parameter U\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 7)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	              ^\n" +
				"Javadoc: Missing tag for parameter V\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 7)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	                                          ^^^\n" +
				"Javadoc: Missing tag for parameter obj\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void test033() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" +
					"	 * Invalid type parameter reference\n" +
					"	 * @param obj Object\n" +
					"	 * @param <U> Type parameter 3\n" +
					"	 * @param <V> Type parameter 3\n" +
					"	 */\n" +
					"	public <T, U, V> void foo(int val, Object obj) {}\n" +
					"}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	        ^\n" +
				"Javadoc: Missing tag for parameter T\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 8)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	                              ^^^\n" +
				"Javadoc: Missing tag for parameter val\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void test034() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" +
					"	 * Invalid type parameter reference\n" +
					"	 * @param val int\n" +
					"	 * @param <V> Type parameter 2\n" +
					"	 * @param <X> Type parameter 2\n" +
					"	 * @param <U> Type parameter 1\n" +
					"	 * @param Object obj\n" +
					"	 * @param <E> Type parameter 2\n" +
					"	 * @param <U> Type parameter 2\n" +
					"	 * @param val int\n" +
					"	 */\n" +
					"	public <T, U, V> void foo(int val, Object obj) {}\n" +
					"}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	* @param <X> Type parameter 2\n" +
				"	          ^\n" +
				"Javadoc: Parameter X is not declared\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 8)\n" +
				"	* @param Object obj\n" +
				"	         ^^^^^^\n" +
				"Javadoc: Parameter Object is not declared\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 9)\n" +
				"	* @param <E> Type parameter 2\n" +
				"	          ^\n" +
				"Javadoc: E cannot be resolved to a type\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 10)\n" +
				"	* @param <U> Type parameter 2\n" +
				"	          ^\n" +
				"Javadoc: Duplicate tag for parameter\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 11)\n" +
				"	* @param val int\n" +
				"	         ^^^\n" +
				"Javadoc: Duplicate tag for parameter\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 13)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	        ^\n" +
				"Javadoc: Missing tag for parameter T\n" +
				"----------\n" +
				"7. ERROR in X.java (at line 13)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	                                          ^^^\n" +
				"Javadoc: Missing tag for parameter obj\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void test035() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" +
					"	 * Invalid type parameter reference\n" +
					"	 * @param <V> Type parameter 2\n" +
					"	 * @param\n" +
					"	 * @param <U> Type parameter 1\n" +
					"	 */\n" +
					"	public <T, U, V> void foo(int val, Object obj) {}\n" +
					"}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	* @param\n" +
				"	   ^^^^^\n" +
				"Javadoc: Missing parameter name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 8)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	        ^\n" +
				"Javadoc: Missing tag for parameter T\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 8)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	                              ^^^\n" +
				"Javadoc: Missing tag for parameter val\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 8)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	                                          ^^^\n" +
				"Javadoc: Missing tag for parameter obj\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	// TODO (david) recovery seems not to work properly here:
	// we should have type parameters in method declaration.
	public void test036() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" +
					"	 * Invalid type parameter reference: compile error\n" +
					"	 * @param <T> Type parameter 2\n" +
					"	 * @param <V> Type parameter 2\n" +
					"	 * @param <U> Type parameter 1\n" +
					"	 * @param val int\n" +
					"	 * @param obj Object\n" +
					"	 */\n" +
					"	public <T, , V> void foo(int val, Object obj) {}\n" +
					"}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	* @param <T> Type parameter 2\n" +
				"	   ^^^^^\n" +
				"Javadoc: Unexpected tag\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	* @param <V> Type parameter 2\n" +
				"	   ^^^^^\n" +
				"Javadoc: Unexpected tag\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 6)\n" +
				"	* @param <U> Type parameter 1\n" +
				"	   ^^^^^\n" +
				"Javadoc: Unexpected tag\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 10)\n" +
				"	public <T, , V> void foo(int val, Object obj) {}\n" +
				"	           ^\n" +
				"Syntax error on token \",\", delete this token\n" +
				"----------\n"
		);
	}
	public void test037() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" +
					"	 * Invalid type parameter reference: compile error\n" +
					"	 * @param <T> Type parameter 2\n" +
					"	 * @param <V> Type parameter 2\n" +
					"	 * @param <U> Type parameter 1\n" +
					"	 * @param val int\n" +
					"	 * @param obj Object\n" +
					"	 */\n" +
					"	public <T, U, V extends Exceptions> void foo(int val, Object obj) {}\n" +
					"}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 10)\n" +
				"	public <T, U, V extends Exceptions> void foo(int val, Object obj) {}\n" +
				"	                        ^^^^^^^^^^\n" +
				"Exceptions cannot be resolved to a type\n" +
				"----------\n"
		);
	}
	public void test038() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" /**\n" +
					"  * Invalid type parameter reference\n" +
					"  * @param < Type\n" +
					"  * @param < Type for parameterization\n" +
					"  * @param <> Type\n" +
					"  * @param <?> Type\n" +
					"  * @param <*> Type\n" +
					"  */\n" +
					" public class X<E> {}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	* @param < Type\n" +
				"	         ^^^^^^\n" +
				"Javadoc: Invalid param tag type parameter name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	* @param < Type for parameterization\n" +
				"	         ^^^^^^\n" +
				"Javadoc: Invalid param tag type parameter name\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	* @param <> Type\n" +
				"	         ^^\n" +
				"Javadoc: Invalid param tag type parameter name\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 6)\n" +
				"	* @param <?> Type\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag type parameter name\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 7)\n" +
				"	* @param <*> Type\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag type parameter name\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 9)\n" +
				"	public class X<E> {}\n" +
				"	               ^\n" +
				"Javadoc: Missing tag for parameter E\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void test039() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" /**\n" +
					"  * Invalid type parameter reference\n" +
					"  * @param <E Type\n" +
					"  * @param E> Type\n" +
					"  * @param <<E> Type\n" +
					"  * @param <<<E> Type\n" +
					"  * @param <E>> Type\n" +
					"  */\n" +
					" public class X<E> {}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	* @param <E Type\n" +
				"	         ^^\n" +
				"Javadoc: Invalid param tag type parameter name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	* @param E> Type\n" +
				"	         ^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	* @param <<E> Type\n" +
				"	         ^^^^\n" +
				"Javadoc: Invalid param tag type parameter name\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 6)\n" +
				"	* @param <<<E> Type\n" +
				"	         ^^^^^\n" +
				"Javadoc: Invalid param tag type parameter name\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 7)\n" +
				"	* @param <E>> Type\n" +
				"	         ^^^^\n" +
				"Javadoc: Invalid param tag type parameter name\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 9)\n" +
				"	public class X<E> {}\n" +
				"	               ^\n" +
				"Javadoc: Missing tag for parameter E\n" +
				"----------\n",
				JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void test040() {
		runConformReferenceTest(
			new String[] {
				"X.java",
				"/**\n" +
				" * @category\n" +
				" */\n" +
				"public class X {\n" +
				"}\n"
			}
		);
	}

	/**
	 * Test fix for bug 80257: [javadoc] Invalid missing reference warning on @see or @link tags
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=80257"
	 */
	public void testBug80257() {
		runNegativeTest(
			new String[] {
				"X.java",
				"/**\n" +
				" * @see G#G(Object)\n" +
				" * @see G#G(Exception)\n" +
				" */\n" +
				"public class X extends G<Exception> {\n" +
				"	X(Exception exc) { super(exc);}\n" +
				"}\n" +
				"class G<E extends Exception> {\n" +
				"	G(E e) {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	* @see G#G(Object)\n" +
			"	         ^^^^^^^^^\n" +
			"Javadoc: The constructor G(Object) is undefined\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	/**
	 * Test fix for bug 82514: [1.5][javadoc] Problem with generics in javadoc
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=82514"
	 */
	// FAIL ERRMSG
	public void _testBug82514() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class ComparableUtils {\n" +
				"   public static <T extends Comparable< ? super T>> int compareTo(final Object first, final Object firstPrime,  final Class<T> type) throws ClassCastException\n" +
				"    {\n" +
				"        return 0;\n" +
				"    }\n" +
				"    public static <X extends Comparable< ? super X>> int compareTo(final X first, final X firstPrime)\n" +
				"        throws ClassCastException\n" +
				"    {\n" +
				"        return 0;\n" +
				"    }\n" +
				"}\n" +
				"public final class X {  \n" +
				"	/** Tests the method{@link ComparableUtils#compareTo(Object, Object, Class)} and\n" +
				"	 *  {@link ComparableUtils#compareTo(Object, Object)}.\n" +
				"	 */\n" +
				"    public void testCompareTo() {}\n" +
				"}"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 6)\n" +
			"	public static <X extends Comparable< ? super X>> int compareTo(final X first, final X firstPrime)\n" +
			"	               ^\n" +
			"The type parameter X is hiding the type X\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 14)\n" +
			"	*  {@link ComparableUtils#compareTo(Object, Object)}.\n" +
			"	                          ^^^^^^^^^\n" +
			"Javadoc: Bound mismatch: The generic method compareTo(X, X) of type ComparableUtils is not applicable for the arguments (Object, Object). The inferred type Object is not a valid substitute for the bounded parameter <X extends Comparable<? super X>>\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

	/**
	 * Test fix for bug 83127: [1.5][javadoc][dom] Wrong / strange bindings for references in javadoc to methods with type variables as parameter types
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=83127"
	 */
	public void testBug83127a() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Test.java",
				"/** \n" +
				" * @see Test#add(T) \n" +
				" * @see #add(T)\n" +
				" * @see Test#Test(T)\n" +
				" * @see #Test(T)\n" +
				" *   - warning = \"The method add(Object) in the type Test is not applicable for\n" +
				" *                the arguments (T)\"\n" +
				" *   - method binding = Test.add(Object)\n" +
				" *   - parameter binding = T of A\n" +
				" */\n" +
				"public class Test<T> {\n" +
				"	Test(T t) {}\n" +
				"    public boolean add(T t) {\n" +
				"        return true;\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"class Sub<E extends Number> extends Test<E> {\n" +
				"	Sub (E e) {super(null);}\n" +
				"    public boolean add(E e) {\n" +
				"        if (e.doubleValue() > 0)\n" +
				"            return false;\n" +
				"        return super.add(e);\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in Test.java (at line 2)\n" +
			"	* @see Test#add(T) \n" +
			"	            ^^^\n" +
			"Javadoc: The method add(Object) in the type Test is not applicable for the arguments (T)\n" +
			"----------\n" +
			"2. ERROR in Test.java (at line 3)\n" +
			"	* @see #add(T)\n" +
			"	        ^^^\n" +
			"Javadoc: The method add(Object) in the type Test is not applicable for the arguments (T)\n" +
			"----------\n" +
			"3. ERROR in Test.java (at line 4)\n" +
			"	* @see Test#Test(T)\n" +
			"	            ^^^^^^^\n" +
			"Javadoc: The constructor Test(T) is undefined\n" +
			"----------\n" +
			"4. ERROR in Test.java (at line 5)\n" +
			"	* @see #Test(T)\n" +
			"	        ^^^^^^^\n" +
			"Javadoc: The constructor Test(T) is undefined\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void testBug83127b() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Test.java",
				"/** \n" +
				" * @see Sub#add(T)\n" +
				" * @see Sub#Sub(T)\n" +
				" *   - warning = \"The method add(Number) in the type Sub is not applicable for\n" +
				" *                the arguments (T)\"\n" +
				" *   - method binding = Sub.add(Number)\n" +
				" *   - parameter binding = T of A\n" +
				" *     -> Do we need to change this as T natually resolved to TypeVariable?\n" +
				" *        As compiler raises a warning, it\'s perhaps not a problem now...\n" +
				" */\n" +
				"public class Test<T>{\n" +
				"	Test(T t) {}\n" +
				"    public boolean add(T t) {\n" +
				"        return true;\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"class Sub<E extends Number> extends Test<E> {\n" +
				"	Sub (E e) {super(null);}\n" +
				"    public boolean add(E e) {\n" +
				"        if (e.doubleValue() > 0)\n" +
				"            return false;\n" +
				"        return super.add(e);\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in Test.java (at line 2)\n" +
			"	* @see Sub#add(T)\n" +
			"	           ^^^\n" +
			"Javadoc: The method add(Number) in the type Sub is not applicable for the arguments (T)\n" +
			"----------\n" +
			"2. ERROR in Test.java (at line 3)\n" +
			"	* @see Sub#Sub(T)\n" +
			"	           ^^^^^^\n" +
			"Javadoc: The constructor Sub(T) is undefined\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void testBug83127c() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Test.java",
				"/** \n" +
				" * @see Sub#add(E) \n" +
				" * @see Sub#Sub(E)\n" +
				" *   - warning = \"E cannot be resolved to a type\"\n" +
				" *   - method binding = null\n" +
				" *   - parameter binding = null\n" +
				" */\n" +
				"public class Test<T>{\n" +
				"	Test(T t) {}\n" +
				"    public boolean add(T t) {\n" +
				"        return true;\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"class Sub<E extends Number> extends Test<E> {\n" +
				"	Sub (E e) {super(null);}\n" +
				"    public boolean add(E e) {\n" +
				"        if (e.doubleValue() > 0)\n" +
				"            return false;\n" +
				"        return super.add(e);\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in Test.java (at line 2)\n" +
			"	* @see Sub#add(E) \n" +
			"	               ^\n" +
			"Javadoc: E cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in Test.java (at line 3)\n" +
			"	* @see Sub#Sub(E)\n" +
			"	               ^\n" +
			"Javadoc: E cannot be resolved to a type\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void testBug83127d() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Unrelated1.java",
				"public class Unrelated1<E extends Number> {\n" +
				"	public Unrelated1(E e) {}\n" +
				"	public boolean add(E e) { return false; }\n" +
				"}\n",
				"Test.java",
				"/** \n" +
				" * @see Unrelated1#add(E)\n" +
				" * @see Unrelated1#Unrelated1(E)\n" +
				" *   - warning = \"E cannot be resolved to a type\"\n" +
				" *   - method binding = null\n" +
				" *   - parameter binding = null\n" +
				" */\n" +
				"public class Test<T>{\n" +
				"	Test(T t) {}\n" +
				"    public boolean add(T t) {\n" +
				"        return true;\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"class Sub<E extends Number> extends Test<E> {\n" +
				"	Sub (E e) {super(null);}\n" +
				"    public boolean add(E e) {\n" +
				"        if (e.doubleValue() > 0)\n" +
				"            return false;\n" +
				"        return super.add(e);\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in Test.java (at line 2)\n" +
			"	* @see Unrelated1#add(E)\n" +
			"	                      ^\n" +
			"Javadoc: E cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in Test.java (at line 3)\n" +
			"	* @see Unrelated1#Unrelated1(E)\n" +
			"	                             ^\n" +
			"Javadoc: E cannot be resolved to a type\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void testBug83127e() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Unrelated1.java",
				"public class Unrelated1<E extends Number> {\n" +
				"	public Unrelated1(E e) {}\n" +
				"	public boolean add(E e) { return false; }\n" +
				"}\n",
				"Test.java",
				"/** \n" +
				" * @see Unrelated1#add(Object)\n" +
				" * @see Unrelated1#Unrelated1(Object)\n" +
				" *   - warning = \"The method add(Object) in the type Test is not applicable for\n" +
				" *                the arguments (Object)\"\n" +
				" *   - method binding = Unrelated1.add(Number)\n" +
				" *   - parameter binding = java.lang.Object\n" +
				" */\n" +
				"public class Test<T>{\n" +
				"	Test(T t) {}\n" +
				"    public boolean add(T t) {\n" +
				"        return true;\n" +
				"    }\n" +
				"}\n" +
				"class Sub<E extends Number> extends Test<E> {\n" +
				"	Sub (E e) {super(null);}\n" +
				"    public boolean add(E e) {\n" +
				"        if (e.doubleValue() > 0)\n" +
				"            return false;\n" +
				"        return super.add(e);\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in Test.java (at line 2)\n" +
			"	* @see Unrelated1#add(Object)\n" +
			"	                  ^^^\n" +
			"Javadoc: The method add(Number) in the type Unrelated1 is not applicable for the arguments (Object)\n" +
			"----------\n" +
			"2. ERROR in Test.java (at line 3)\n" +
			"	* @see Unrelated1#Unrelated1(Object)\n" +
			"	                  ^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The constructor Unrelated1(Object) is undefined\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void testBug83127f() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"Unrelated1.java",
				"public class Unrelated1<E extends Number> {\n" +
				"	public Unrelated1(E e) {}\n" +
				"	public boolean add(E e) { return false; }\n" +
				"}\n",
				"Test.java",
				"/** \n" +
				" * @see Unrelated1#add(Number)\n" +
				" * @see Unrelated1#Unrelated1(Number)\n" +
				" *   - no warning\n" +
				" *   - method binding = Unrelated1.add(Number)\n" +
				" *   - parameter binding = java.lang.Number\n" +
				" */\n" +
				"public class Test<T>{\n" +
				"	Test(T t) {}\n" +
				"    public boolean add(T t) {\n" +
				"        return true;\n" +
				"    }\n" +
				"}\n" +
				"class Sub<E extends Number> extends Test<E> {\n" +
				"	Sub (E e) {super(null);}\n" +
				"    public boolean add(E e) {\n" +
				"        if (e.doubleValue() > 0)\n" +
				"            return false;\n" +
				"        return super.add(e);\n" +
				"    }\n" +
				"}\n"
			}
		);
	}
	public void testBug83127g() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Unrelated1.java",
				"public class Unrelated1<E extends Number> {\n" +
				"	public Unrelated1(E e) {}\n" +
				"	public boolean add(E e) { return false; }\n" +
				"}\n",
				"Test.java",
				"/** \n" +
				" * @see Unrelated1#add(Integer)\n" +
				" * @see Unrelated1#Unrelated1(Integer)\n" +
				" *   - warning = \"The method add(Object) in the type Test is not applicable for\n" +
				" *                the arguments (Integer)\"\n" +
				" *   - method binding = Unrelated1.add(Number)\n" +
				" *   - parameter binding = java.lang.Integer\n" +
				" */\n" +
				"public class Test<T>{\n" +
				"	Test(T t) {}\n" +
				"    public boolean add(T t) {\n" +
				"        return true;\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"class Sub<E extends Number> extends Test<E> {\n" +
				"	Sub (E e) {super(null);}\n" +
				"	public boolean add(E e) {\n" +
				"        if (e.doubleValue() > 0)\n" +
				"            return false;\n" +
				"        return super.add(e);\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in Test.java (at line 2)\n" +
			"	* @see Unrelated1#add(Integer)\n" +
			"	                  ^^^\n" +
			"Javadoc: The method add(Number) in the type Unrelated1 is not applicable for the arguments (Integer)\n" +
			"----------\n" +
			"2. ERROR in Test.java (at line 3)\n" +
			"	* @see Unrelated1#Unrelated1(Integer)\n" +
			"	                  ^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: The constructor Unrelated1(Integer) is undefined\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void testBug83127h() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Unrelated2.java",
				"public interface Unrelated2<E> {\n" +
				"	boolean add(E e);\n" +
				"}\n",
				"Test.java",
				"/** \n" +
				" * @see Unrelated2#add(T)\n" +
				" *   - warning = \"The method add(Object) in the type Test is not applicable for\n" +
				" *                the arguments (T)\"\n" +
				" *   - method binding = Unrelated2.add(Object)\n" +
				" *   - parameter binding = T of A\n" +
				" *     -> Do we need to change this as T natually resolved to TypeVariable?\n" +
				" *        As compiler raises a warning, it\'s perhaps not a problem now...\n" +
				" */\n" +
				"public class Test<T>{\n" +
				"	Test(T t) {}\n" +
				"    public boolean add(T t) {\n" +
				"        return true;\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"class Sub<E extends Number> extends Test<E> {\n" +
				"	Sub (E e) {super(null);}\n" +
				"    public boolean add(E e) {\n" +
				"        if (e.doubleValue() > 0)\n" +
				"            return false;\n" +
				"        return super.add(e);\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in Test.java (at line 2)\n" +
			"	* @see Unrelated2#add(T)\n" +
			"	                  ^^^\n" +
			"Javadoc: The method add(Object) in the type Unrelated2 is not applicable for the arguments (T)\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	/**
	 * Bug 83393: [1.5][javadoc] reference to vararg method also considers non-array type as correct
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=83393"
	 */
	public void testBug83393a() {
		runConformTest(
			new String[] {
				"Test.java",
				"public class Test {\n" +
				"	public void foo(int a, int b) {} \n" +
				"	public void foo(int a, int... args) {}\n" +
				"	public void foo(String... args) {}\n" +
				"	public void foo(Exception str, boolean... args) {}\n" +
				"}\n",
				"Valid.java",
				"/**\n" +
				" * @see Test#foo(int, int)\n" +
				" * @see Test#foo(int, int[])\n" +
				" * @see Test#foo(int, int...)\n" +
				" * @see Test#foo(String[])\n" +
				" * @see Test#foo(String...)\n" +
				" * @see Test#foo(Exception, boolean[])\n" +
				" * @see Test#foo(Exception, boolean...)\n" +
				" */\n" +
				"public class Valid {}\n"
			}
		);
	}
	public void testBug83393b() {
		runNegativeTest(
			new String[] {
				"Test.java",
				"public class Test {\n" +
				"	public void foo(int a, int b) {} \n" +
				"	public void foo(int a, int... args) {}\n" +
				"	public void foo(String... args) {}\n" +
				"	public void foo(Exception str, boolean... args) {}\n" +
				"}\n",
				"Invalid.java",
				"/**\n" +
				" * @see Test#foo(int)\n" +
				" * @see Test#foo(int, int, int)\n" +
				" * @see Test#foo()\n" +
				" * @see Test#foo(String)\n" +
				" * @see Test#foo(String, String)\n" +
				" * @see Test#foo(Exception)\n" +
				" * @see Test#foo(Exception, boolean)\n" +
				" * @see Test#foo(Exception, boolean, boolean)\n" +
				" */\n" +
				"public class Invalid {}\n"
			},
			"----------\n" +
			"1. ERROR in Invalid.java (at line 2)\n" +
			"	* @see Test#foo(int)\n" +
			"	            ^^^\n" +
			"Javadoc: The method foo(int, int...) in the type Test is not applicable for the arguments (int)\n" +
			"----------\n" +
			"2. ERROR in Invalid.java (at line 3)\n" +
			"	* @see Test#foo(int, int, int)\n" +
			"	            ^^^\n" +
			"Javadoc: The method foo(int, int...) in the type Test is not applicable for the arguments (int, int, int)\n" +
			"----------\n" +
			"3. ERROR in Invalid.java (at line 4)\n" +
			"	* @see Test#foo()\n" +
			"	            ^^^\n" +
			"Javadoc: The method foo(String...) in the type Test is not applicable for the arguments ()\n" +
			"----------\n" +
			"4. ERROR in Invalid.java (at line 5)\n" +
			"	* @see Test#foo(String)\n" +
			"	            ^^^\n" +
			"Javadoc: The method foo(String...) in the type Test is not applicable for the arguments (String)\n" +
			"----------\n" +
			"5. ERROR in Invalid.java (at line 6)\n" +
			"	* @see Test#foo(String, String)\n" +
			"	            ^^^\n" +
			"Javadoc: The method foo(String...) in the type Test is not applicable for the arguments (String, String)\n" +
			"----------\n" +
			"6. ERROR in Invalid.java (at line 7)\n" +
			"	* @see Test#foo(Exception)\n" +
			"	            ^^^\n" +
			"Javadoc: The method foo(Exception, boolean...) in the type Test is not applicable for the arguments (Exception)\n" +
			"----------\n" +
			"7. ERROR in Invalid.java (at line 8)\n" +
			"	* @see Test#foo(Exception, boolean)\n" +
			"	            ^^^\n" +
			"Javadoc: The method foo(Exception, boolean...) in the type Test is not applicable for the arguments (Exception, boolean)\n" +
			"----------\n" +
			"8. ERROR in Invalid.java (at line 9)\n" +
			"	* @see Test#foo(Exception, boolean, boolean)\n" +
			"	            ^^^\n" +
			"Javadoc: The method foo(Exception, boolean...) in the type Test is not applicable for the arguments (Exception, boolean, boolean)\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	/**
	 * Bug 83804: [1.5][javadoc] Missing Javadoc node for package declaration
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=83804"
	 */
	public void testBug83804() {
		runNegativeTest(
			new String[] {
				"pack/package-info.java",
				"/**\n" +
				" * Valid javadoc.\n" +
				" * @see pack.Test\n" +
				" * @see Unknown\n" +
				" * @see pack.Test#foo()\n" +
				" * @see pack.Test#unknown()\n" +
				" * @see pack.Test#field\n" +
				" * @see pack.Test#unknown\n" +
				" * @param unexpected\n" +
				" * @throws unexpected\n" +
				" * @return unexpected \n" +
				" * @deprecated accepted by javadoc.exe although javadoc 1.5 spec does not say that's a valid tag\n" +
				" * @other-tags are valid\n" +
				" */\n" +
				"package pack;\n",
				"pack/Test.java",
				"/**\n" +
				" * Invalid javadoc\n" +
				" */\n" +
				"package pack;\n" +
				"public class Test {\n" +
				"	public int field;\n" +
				"	public void foo() {}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in pack\\package-info.java (at line 4)\n" +
			"	* @see Unknown\n" +
			"	       ^^^^^^^\n" +
			"Javadoc: Unknown cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in pack\\package-info.java (at line 6)\n" +
			"	* @see pack.Test#unknown()\n" +
			"	                 ^^^^^^^\n" +
			"Javadoc: The method unknown() is undefined for the type Test\n" +
			"----------\n" +
			"3. ERROR in pack\\package-info.java (at line 8)\n" +
			"	* @see pack.Test#unknown\n" +
			"	                 ^^^^^^^\n" +
			"Javadoc: unknown cannot be resolved or is not a field\n" +
			"----------\n" +
			"4. ERROR in pack\\package-info.java (at line 9)\n" +
			"	* @param unexpected\n" +
			"	   ^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"5. ERROR in pack\\package-info.java (at line 10)\n" +
			"	* @throws unexpected\n" +
			"	   ^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"6. ERROR in pack\\package-info.java (at line 11)\n" +
			"	* @return unexpected \n" +
			"	   ^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	/**
	 * Bug 86769: [javadoc] Warn/Error for 'Missing javadoc comments' doesn't recognize private inner classes
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=86769"
	 */
	public void _testBug86769() {
		this.reportMissingJavadocComments = CompilerOptions.ERROR;
		runNegativeTest(
			new String[] {
				"E.java",
				"public enum E {\n" +
				"	A,\n" +
				"	DC{\n" +
				"		public void foo() {}\n" +
				"	};\n" +
				"	E() {}\n" +
				"	public void foo() {}\n" +
				"	private enum Epriv {\n" +
				"		Apriv,\n" +
				"		Cpriv {\n" +
				"			public void foo() {}\n" +
				"		};\n" +
				"		Epriv() {}\n" +
				"		public void foo() {}\n" +
				"	}\n" +
				"	enum Edef {\n" +
				"		Adef,\n" +
				"		Cdef {\n" +
				"			public void foo() {}\n" +
				"		};\n" +
				"		Edef() {}\n" +
				"		public void foo() {}\n" +
				"	}\n" +
				"	protected enum Epro {\n" +
				"		Apro,\n" +
				"		Cpro {\n" +
				"			public void foo() {}\n" +
				"		};\n" +
				"		Epro() {}\n" +
				"		public void foo() {}\n" +
				"	}\n" +
				"	public enum Epub {\n" +
				"		Apub,\n" +
				"		Cpub {\n" +
				"			public void foo() {}\n" +
				"		};\n" +
				"		Epub() {}\n" +
				"		public void foo() {}\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in E.java (at line 1)\n" +
			"	public enum E {\n" +
			"	            ^\n" +
			"Javadoc: Missing comment for public declaration\n" +
			"----------\n" +
			"2. ERROR in E.java (at line 2)\n" +
			"	A,\n" +
			"	^\n" +
			"Javadoc: Missing comment for public declaration\n" +
			"----------\n" +
			"3. ERROR in E.java (at line 3)\n" +
			"	DC{\n" +
			"	^^\n" +
			"Javadoc: Missing comment for public declaration\n" +
			"----------\n" +
			"4. ERROR in E.java (at line 7)\n" +
			"	public void foo() {}\n" +
			"	            ^^^^^\n" +
			"Javadoc: Missing comment for public declaration\n" +
			"----------\n" +
			"5. ERROR in E.java (at line 32)\n" +
			"	public enum Epub {\n" +
			"	            ^^^^\n" +
			"Javadoc: Missing comment for public declaration\n" +
			"----------\n" +
			"6. ERROR in E.java (at line 33)\n" +
			"	Apub,\n" +
			"	^^^^\n" +
			"Javadoc: Missing comment for public declaration\n" +
			"----------\n" +
			"7. ERROR in E.java (at line 34)\n" +
			"	Cpub {\n" +
			"	^^^^\n" +
			"Javadoc: Missing comment for public declaration\n" +
			"----------\n" +
			"8. ERROR in E.java (at line 38)\n" +
			"	public void foo() {}\n" +
			"	            ^^^^^\n" +
			"Javadoc: Missing comment for public declaration\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	/**
	 * Bug 95286: [1.5][javadoc] package-info.java incorrectly flags "Missing comment for public declaration"
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=95286"
	 */
	public void testBug95286_Default() {
		this.reportMissingJavadocComments = CompilerOptions.ERROR;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.DEFAULT;
		runConformTest(
			new String[] {
				"test/package-info.java",
				"/**\n" +
				" * Javadoc for all package \n" +
				" */\n" +
				"package test;\n"
			}
		);
	}
	public void testBug95286_Private() {
		this.reportMissingJavadocComments = CompilerOptions.ERROR;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.PRIVATE;
		runConformTest(
			new String[] {
				"test/package-info.java",
				"/**\n" +
				" * Javadoc for all package \n" +
				" */\n" +
				"package test;\n"
			}
		);
	}

	/**
	 * Bug 95521: [1.5][javadoc] validation with @see tag not working for generic method
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=95521"
	 */
	public void testBug95521() {
		runConformTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
				"\n" +
				"/** Test */\n" +
				"public class X implements I {\n" +
				"	/**\n" +
				"	 * @see test.I#foo(java.lang.Class)\n" +
				"	 */\n" +
				"	public <T> G<T> foo(Class<T> stuffClass) {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n" +
				"/** Interface */\n" +
				"interface I {\n" +
				"    /**\n" +
				"     * @param <T>\n" +
				"     * @param stuffClass \n" +
				"     * @return stuff\n" +
				"     */\n" +
				"    public <T extends Object> G<T> foo(Class<T> stuffClass);\n" +
				"}\n" +
				"/** \n" +
				" * @param <T>\n" +
				" */\n" +
				"class G<T> {}\n"
			}
		);
	}
	public void testBug95521b() {
		runConformTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
				"\n" +
				"/** Test */\n" +
				"public class X {\n" +
				"    /**\n" +
				"     * @param <T>\n" +
				"     * @param classT \n" +
				"     */\n" +
				"	public <T> X(Class<T> classT) {\n" +
				"	}\n" +
				"    /**\n" +
				"     * @param <T>\n" +
				"     * @param classT\n" +
				"     * @return classT\n" +
				"     */\n" +
				"	public <T> Class<T> foo(Class<T> classT) {\n" +
				"		return classT;\n" +
				"	}\n" +
				"}\n" +
				"/** Super class */\n" +
				"class Y extends X {\n" +
				"	/**\n" +
				"	 * @see X#X(java.lang.Class)\n" +
				"	 */\n" +
				"	public <T> Y(Class<T> classT) {\n" +
				"		super(classT);\n" +
				"	}\n" +
				"\n" +
				"	/**\n" +
				"	 * @see X#foo(java.lang.Class)\n" +
				"	 */\n" +
				"    public <T extends Object> Class<T> foo(Class<T> stuffClass) {\n" +
				"    	return null;\n" +
				"    }\n" +
				"}\n"
			}
		);
	}

	/**
	 * Bug 96237: [javadoc] Inner types must be qualified
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=96237"
	 */
	public void testBug96237_Public01() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
		runConformTest(
			new String[] {
				"comment6/Valid.java",
				"package comment6;\n" +
				"public class Valid {\n" +
				"    /**\n" +
				"     * @see Valid.Inner\n" +
				"     */\n" +
				"    public class Inner { }\n" +
				"}\n" +
				"/**\n" +
				" * See also {@link Valid.Inner}\n" +
				" */\n" +
				"class Sub2 extends Valid { }"
			}
		);
	}
	public void testBug96237_Public02() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
		runConformTest(
			new String[] {
				"comment6/Invalid.java",
				"package comment6;\n" +
				"public class Invalid {\n" +
				"    /**\n" +
				"     * @see Inner\n" +
				"     */\n" +
				"    public class Inner { }\n" +
				"}\n" +
				"/**\n" +
				" * See also {@link Inner} \n" +
				" */\n" +
				"class Sub1 extends Invalid { }\n"
			}
		);
	}
	public void testBug96237_Public03() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(
			new String[] {
				"comment6a/def/Test.java",
				"package comment6a.def;\n" +
				"public class Test {\n" +
				"    /**\n" +
				"     * @see Inner\n" +
				"     */\n" +
				"    public class Inner { }\n" +
				"}\n",
				"comment6a/test/Invalid.java",
				"package comment6a.test;\n" +
				"import comment6a.def.Test;\n" +
				"/**\n" +
				" * See also {@link Inner}\n" +
				" */\n" +
				"public class Invalid extends Test { \n" +
				"}",
				"comment6a/test/Invalid2.java",
				"package comment6a.test;\n" +
				"import comment6a.def.Test;\n" +
				"/**\n" +
				" * @see Test.Inner\n" +
				" */\n" +
				"public class Invalid2 extends Test { \n" +
				"}",
				"comment6a/test/Valid.java",
				"package comment6a.test;\n" +
				"import comment6a.def.Test;\n" +
				"/**\n" +
				" * @see comment6a.def.Test.Inner\n" +
				" */\n" +
				"public class Valid extends Test { \n" +
				"}"
			},
			//comment6a\test\Invalid.java:8: warning - Tag @link: reference not found: Inner
			//comment6a\test\Invalid2.java:8: warning - Tag @see: reference not found: Test.Inner => bug ID: 4464323
			"----------\n" +
			"1. ERROR in comment6a\\test\\Invalid.java (at line 4)\n" +
			"	* See also {@link Inner}\n" +
			"	                  ^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in comment6a\\test\\Invalid2.java (at line 4)\n" +
			"	* @see Test.Inner\n" +
			"	       ^^^^^^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void testBug96237_Public04() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(
			new String[] {
				"comment6b/Invalid.java",
				"package comment6b;\n" +
				"\n" +
				"/**\n" +
				" * @see Inner\n" +
				" */\n" +
				"public class Invalid implements Test { \n" +
				"}",
				"comment6b/Test.java",
				"package comment6b;\n" +
				"public interface Test {\n" +
				"    /**\n" +
				"     * @see Inner\n" +
				"     */\n" +
				"    public class Inner { }\n" +
				"}\n",
				"comment6b/Valid.java",
				"package comment6b;\n" +
				"\n" +
				"/**\n" +
				" * @see Test.Inner\n" +
				" */\n" +
				"public class Valid implements Test { \n" +
				"}"
			},
			//comment6b\Invalid.java:6: warning - Tag @see: reference not found: Inner
			"----------\n" +
			"1. ERROR in comment6b\\Invalid.java (at line 4)\n" +
			"	* @see Inner\n" +
			"	       ^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void testBug96237_Public05() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(
			new String[] {
				"test/a/Test.java",
				"package test.a;\n" +
				"/**\n" +
				" * @see Inner\n" +
				" * @see Test.Inner\n" +
				" */\n" +
				"public class Test {\n" +
				"	class Inner {}\n" +
				"}\n"
			},
			//test\a\Test.java:6: warning - Tag @see: reference not found: Inner
			//test\a\Test.java:6: warning - Tag @see: reference not found: Test.Inner
			"----------\n" +
			"1. ERROR in test\\a\\Test.java (at line 3)\n" +
			"	* @see Inner\n" +
			"	       ^^^^^\n" +
			"Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference\n" +
			"----------\n" +
			"2. ERROR in test\\a\\Test.java (at line 4)\n" +
			"	* @see Test.Inner\n" +
			"	       ^^^^^^^^^^\n" +
			"Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void testBug96237_Public06() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(
			new String[] {
				"test/b/Test.java",
				"package test.b;\n" +
				"/** \n" +
				" * @see Inner.Level2\n" +
				" * @see Test.Inner.Level2\n" +
				" */\n" +
				"public class Test {\n" +
				"	/** \n" +
				"	 * @see Level2\n" +
				"	 * @see Test.Inner.Level2\n" +
				"	 */\n" +
				"	public class Inner {\n" +
				"		class Level2 {}\n" +
				"	}\n" +
				"}\n"
			},
			//test\b\Test.java:6: warning - Tag @see: reference not found: Inner.Level2
			//test\b\Test.java:6: warning - Tag @see: reference not found: Test.Inner.Level2
			//test\b\Test.java:11: warning - Tag @see: reference not found: Level2
			//test\b\Test.java:11: warning - Tag @see: reference not found: Test.Inner.Level2
			"----------\n" +
			"1. ERROR in test\\b\\Test.java (at line 3)\n" +
			"	* @see Inner.Level2\n" +
			"	       ^^^^^^^^^^^^\n" +
			"Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference\n" +
			"----------\n" +
			"2. ERROR in test\\b\\Test.java (at line 4)\n" +
			"	* @see Test.Inner.Level2\n" +
			"	       ^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference\n" +
			"----------\n" +
			"3. ERROR in test\\b\\Test.java (at line 8)\n" +
			"	* @see Level2\n" +
			"	       ^^^^^^\n" +
			"Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference\n" +
			"----------\n" +
			"4. ERROR in test\\b\\Test.java (at line 9)\n" +
			"	* @see Test.Inner.Level2\n" +
			"	       ^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void testBug96237_Public07() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(
			new String[] {
				"test/c/Test.java",
				"package test.c;\n" +
				"/**\n" +
				" * @see Inner.Level2.Level3\n" +
				" * @see Test.Inner.Level2.Level3\n" +
				" */\n" +
				"public class Test {\n" +
				"	public class Inner {\n" +
				"		/**\n" +
				"		 * @see Level3\n" +
				"		 * @see Level2.Level3\n" +
				"		 * @see Inner.Level2.Level3\n" +
				"		 * @see Test.Inner.Level2.Level3\n" +
				"		 */\n" +
				"		public class Level2 {\n" +
				"			class Level3 {\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
			},
			//test\c\Test.java:6: warning - Tag @see: reference not found: Inner.Level2.Level3
			//test\c\Test.java:6: warning - Tag @see: reference not found: Test.Inner.Level2.Level3
			//test\c\Test.java:14: warning - Tag @see: reference not found: Level3
			//test\c\Test.java:14: warning - Tag @see: reference not found: Level2.Level3
			//test\c\Test.java:14: warning - Tag @see: reference not found: Inner.Level2.Level3
			//test\c\Test.java:14: warning - Tag @see: reference not found: Test.Inner.Level2.Level3
			"----------\n" +
			"1. ERROR in test\\c\\Test.java (at line 3)\n" +
			"	* @see Inner.Level2.Level3\n" +
			"	       ^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference\n" +
			"----------\n" +
			"2. ERROR in test\\c\\Test.java (at line 4)\n" +
			"	* @see Test.Inner.Level2.Level3\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference\n" +
			"----------\n" +
			"3. ERROR in test\\c\\Test.java (at line 9)\n" +
			"	* @see Level3\n" +
			"	       ^^^^^^\n" +
			"Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference\n" +
			"----------\n" +
			"4. ERROR in test\\c\\Test.java (at line 10)\n" +
			"	* @see Level2.Level3\n" +
			"	       ^^^^^^^^^^^^^\n" +
			"Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference\n" +
			"----------\n" +
			"5. ERROR in test\\c\\Test.java (at line 11)\n" +
			"	* @see Inner.Level2.Level3\n" +
			"	       ^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference\n" +
			"----------\n" +
			"6. ERROR in test\\c\\Test.java (at line 12)\n" +
			"	* @see Test.Inner.Level2.Level3\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void testBug96237_Public08() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(
			new String[] {
				"test/d/Reference.java",
				"package test.d;\n" +
				"class Reference {\n" +
				"}\n",
				"test/d/Test.java",
				"package test.d;\n" +
				"/**\n" +
				" * @see Secondary\n" +
				" * @see Reference\n" +
				" */\n" +
				"public class Test {\n" +
				"}\n" +
				"class Secondary {}"
			},
			"----------\n" +
			"1. ERROR in test\\d\\Test.java (at line 3)\n" +
			"	* @see Secondary\n" +
			"	       ^^^^^^^^^\n" +
			"Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference\n" +
			"----------\n" +
			"2. ERROR in test\\d\\Test.java (at line 4)\n" +
			"	* @see Reference\n" +
			"	       ^^^^^^^^^\n" +
			"Javadoc: 'public' visibility for malformed doc comments hides this 'default' reference\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void testBug96237_Private01() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
		runConformTest(
			new String[] {
				"comment6/Valid.java",
				"package comment6;\n" +
				"public class Valid {\n" +
				"    /**\n" +
				"     * @see Valid.Inner\n" +
				"     */\n" +
				"    public class Inner { }\n" +
				"}\n" +
				"/**\n" +
				" * See also {@link Valid.Inner}\n" +
				" */\n" +
				"class Sub2 extends Valid { }"
			}
		);
	}
	public void testBug96237_Private02() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
		runNegativeTest(
			new String[] {
				"comment6/Invalid.java",
				"package comment6;\n" +
				"public class Invalid {\n" +
				"    /**\n" +
				"     * @see Inner\n" +
				"     */\n" +
				"    public class Inner { }\n" +
				"}\n" +
				"/**\n" +
				" * See also {@link Inner} \n" +
				" */\n" +
				"class Sub1 extends Invalid { }\n"
			},
			//comment6\Invalid.java:11: warning - Tag @link: reference not found: Inner
			"----------\n" +
			"1. ERROR in comment6\\Invalid.java (at line 9)\n" +
			"	* See also {@link Inner} \n" +
			"	                  ^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void testBug96237_Private03() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
		runNegativeTest(
			new String[] {
				"comment6a/def/Test.java",
				"package comment6a.def;\n" +
				"public class Test {\n" +
				"    /**\n" +
				"     * @see Inner\n" +
				"     */\n" +
				"    public class Inner { }\n" +
				"}\n",
				"comment6a/test/Invalid.java",
				"package comment6a.test;\n" +
				"import comment6a.def.Test;\n" +
				"/**\n" +
				" * See also {@link Inner}\n" +
				" */\n" +
				"public class Invalid extends Test { \n" +
				"}",
				"comment6a/test/Invalid2.java",
				"package comment6a.test;\n" +
				"import comment6a.def.Test;\n" +
				"/**\n" +
				" * @see Test.Inner\n" +
				" */\n" +
				"public class Invalid2 extends Test { \n" +
				"}",
				"comment6a/test/Valid.java",
				"package comment6a.test;\n" +
				"import comment6a.def.Test;\n" +
				"/**\n" +
				" * @see comment6a.def.Test.Inner\n" +
				" */\n" +
				"public class Valid extends Test { \n" +
				"}"
			},
			//comment6a\test\Invalid.java:8: warning - Tag @link: reference not found: Inner
			//comment6a\test\Invalid2.java:8: warning - Tag @see: reference not found: Test.Inner => bug ID: 4464323
			"----------\n" +
			"1. ERROR in comment6a\\test\\Invalid.java (at line 4)\n" +
			"	* See also {@link Inner}\n" +
			"	                  ^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in comment6a\\test\\Invalid2.java (at line 4)\n" +
			"	* @see Test.Inner\n" +
			"	       ^^^^^^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void testBug96237_Private04() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
		runNegativeTest(
			new String[] {
				"comment6b/Invalid.java",
				"package comment6b;\n" +
				"\n" +
				"/**\n" +
				" * @see Inner\n" +
				" */\n" +
				"public class Invalid implements Test { \n" +
				"}",
				"comment6b/Test.java",
				"package comment6b;\n" +
				"public interface Test {\n" +
				"    /**\n" +
				"     * @see Inner\n" +
				"     */\n" +
				"    public class Inner { }\n" +
				"}\n",
				"comment6b/Valid.java",
				"package comment6b;\n" +
				"\n" +
				"/**\n" +
				" * @see Test.Inner\n" +
				" */\n" +
				"public class Valid implements Test { \n" +
				"}"
			},
			//comment6b\Invalid.java:6: warning - Tag @see: reference not found: Inner
			"----------\n" +
			"1. ERROR in comment6b\\Invalid.java (at line 4)\n" +
			"	* @see Inner\n" +
			"	       ^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void testBug96237_Private05() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
		runConformTest(
			new String[] {
				"test/a/Test.java",
				"package test.a;\n" +
				"/**\n" +
				" * @see Inner\n" +
				" * @see Test.Inner\n" +
				" */\n" +
				"public class Test {\n" +
				"	class Inner {}\n" +
				"}\n"
			}
		);
	}
	public void testBug96237_Private06() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
		runConformTest(
			new String[] {
				"test/b/Test.java",
				"package test.b;\n" +
				"/** \n" +
				" * @see Inner.Level2\n" +
				" * @see Test.Inner.Level2\n" +
				" */\n" +
				"public class Test {\n" +
				"	/** \n" +
				"	 * @see Level2\n" +
				"	 * @see Test.Inner.Level2\n" +
				"	 */\n" +
				"	public class Inner {\n" +
				"		class Level2 {}\n" +
				"	}\n" +
				"}\n"
			}
		);
	}
	public void testBug96237_Private07() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
		runConformTest(
			new String[] {
				"test/c/Test.java",
				"package test.c;\n" +
				"/**\n" +
				" * @see Inner.Level2.Level3\n" +
				" * @see Test.Inner.Level2.Level3\n" +
				" */\n" +
				"public class Test {\n" +
				"	public class Inner {\n" +
				"		/**\n" +
				"		 * @see Level3\n" +
				"		 * @see Level2.Level3\n" +
				"		 * @see Inner.Level2.Level3\n" +
				"		 * @see Test.Inner.Level2.Level3\n" +
				"		 */\n" +
				"		public class Level2 {\n" +
				"			class Level3 {\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
			}
		);
	}
	public void testBug96237_Private08() {
		this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
		runConformTest(
			new String[] {
				"test/d/Reference.java",
				"package test.d;\n" +
				"class Reference {\n" +
				"}\n",
				"test/d/Test.java",
				"package test.d;\n" +
				"/**\n" +
				" * @see Secondary\n" +
				" * @see Reference\n" +
				" */\n" +
				"public class Test {\n" +
				"}\n" +
				"class Secondary {}"
			}
		);
	}

	/**
	 * Bug 101283: [1.5][javadoc] Javadoc validation raises missing implementation in compiler
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=101283"
	 */
	public void testBug101283a() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T, F> {\n" +
				"\n" +
				"	/**\n" +
				"	 * @param <T>  \n" +
				"	 * @param <F>\n" +
				"	 */\n" +
				"	static class Entry<L, R> {\n" +
				"		// empty\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	* @param <T>  \n" +
			"	          ^\n" +
			"Javadoc: Parameter T is not declared\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	* @param <F>\n" +
			"	          ^\n" +
			"Javadoc: Parameter F is not declared\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	static class Entry<L, R> {\n" +
			"	                   ^\n" +
			"Javadoc: Missing tag for parameter L\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	static class Entry<L, R> {\n" +
			"	                      ^\n" +
			"Javadoc: Missing tag for parameter R\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void testBug101283b() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T, F> {\n" +
				"\n" +
				"	/**\n" +
				"	 * @see T Variable \n" +
				"	 * @see F Variable\n" +
				"	 */\n" +
				"	static class Entry<L, R> {\n" +
				"		// empty\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	* @see T Variable \n" +
			"	       ^\n" +
			"Javadoc: Invalid reference\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	* @see F Variable\n" +
			"	       ^\n" +
			"Javadoc: Invalid reference\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	static class Entry<L, R> {\n" +
			"	                   ^\n" +
			"Javadoc: Missing tag for parameter L\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	static class Entry<L, R> {\n" +
			"	                      ^\n" +
			"Javadoc: Missing tag for parameter R\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void testBug101283c() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T, F> {\n" +
				"\n" +
				"	/**\n" +
				"	 * @param <T>  \n" +
				"	 * @param <F>\n" +
				"	 */\n" +
				"	class Entry<L, R> {\n" +
				"		// empty\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	* @param <T>  \n" +
			"	          ^\n" +
			"Javadoc: Parameter T is not declared\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	* @param <F>\n" +
			"	          ^\n" +
			"Javadoc: Parameter F is not declared\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	class Entry<L, R> {\n" +
			"	            ^\n" +
			"Javadoc: Missing tag for parameter L\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	class Entry<L, R> {\n" +
			"	               ^\n" +
			"Javadoc: Missing tag for parameter R\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void testBug101283d() {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T, F> {\n" +
				"\n" +
				"	/**\n" +
				"	 * @see T Variable \n" +
				"	 * @see F Variable\n" +
				"	 */\n" +
				"	class Entry<L, R> {\n" +
				"		// empty\n" +
				"	}\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	* @see T Variable \n" +
			"	       ^\n" +
			"Javadoc: Invalid reference\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	* @see F Variable\n" +
			"	       ^\n" +
			"Javadoc: Invalid reference\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	class Entry<L, R> {\n" +
			"	            ^\n" +
			"Javadoc: Missing tag for parameter L\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	class Entry<L, R> {\n" +
			"	               ^\n" +
			"Javadoc: Missing tag for parameter R\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	// Verify duplicate test case: bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=102735
	public void testBug101283e() {
		this.reportMissingJavadocTags = CompilerOptions.DISABLED;
		runNegativeTest(
			new String[] {
				"Test.java",
				"public interface Test<V, R extends Component<?>, C extends\n" +
				"Test<V, R, C>> extends Control<SelectModel<V>, C>\n" +
				"{\n" +
				"	public interface ValueRepresentationStrategy<VV, RR extends Component<?>> extends ComponentFactory<VV, RR>\n" +
				"	{\n" +
				"		/**This value must be equal to the ID of the component returned by the {@link\n" +
				"		ComponentFactory#createComponent(V)} method.*/\n" +
				"		public String getID(final VV value);\n" +
				"	}\n" +
				"}\n" +
				"class Component<T> {}\n" +
				"interface Control<U, V> {}\n" +
				"class SelectModel<V> {}\n" +
				"interface ComponentFactory <U, V> {\n" +
				"	public void createComponent(V v);\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in Test.java (at line 7)\n" +
			"	ComponentFactory#createComponent(V)} method.*/\n" +
			"	                                 ^\n" +
			"Javadoc: Cannot make a static reference to the non-static type variable V\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void testBug101283f() {
		this.reportMissingJavadocTags = CompilerOptions.DISABLED;
		runConformTest(
			new String[] {
				"Test.java",
				"public interface Test<V, R extends Component<?>, C extends\n" +
				"Test<V, R, C>> extends Control<SelectModel<V>, C>\n" +
				"{\n" +
				"	public interface ValueRepresentationStrategy<VV, RR extends Component<?>> extends ComponentFactory<VV, RR>\n" +
				"	{\n" +
				"		/**This value must be equal to the ID of the component returned by the {@link\n" +
				"		ComponentFactory#createComponent(Object)} method.*/\n" +
				"		public String getID(final VV value);\n" +
				"	}\n" +
				"}\n" +
				"class Component<T> {}\n" +
				"interface Control<U, V> {}\n" +
				"class SelectModel<V> {}\n" +
				"interface ComponentFactory <U, V> {\n" +
				"	public void createComponent(V v);\n" +
				"}\n"
			}
		);
	}
	// Verify that ProblemReasons.InheritedNameHidesEnclosingName is not reported as Javadoc error
	public void testBug101283g() {
		this.reportMissingJavadocTags = CompilerOptions.DISABLED;
		runConformTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
				"public class X {\n" +
				"	int foo() { return 0; }\n" +
				"	class XX extends X2 {\n" +
				"		int bar() {\n" +
				"			return foo();\n" +
				"		}\n" +
				"	}\n" +
				"}\n" +
				"class X2 {\n" +
				"	int foo() {\n" +
				"		return 0;\n" +
				"	}\n" +
				"}\n",
				"test/Y.java",
				"package test;\n" +
				"public class Y {\n" +
				"	int foo;\n" +
				"	class YY extends Y2 {\n" +
				"	/**\n" +
				"	 *  @see #foo\n" +
				"	 */\n" +
				"		int bar() {\n" +
				"			return foo;\n" +
				"		}\n" +
				"	}\n" +
				"}\n" +
				"class Y2 {\n" +
				"	int foo;\n" +
				"}\n"
			}
		);
	}

	/**
	 * Bug 112346: [javadoc] {&#064;inheritedDoc} should be inactive for non-overridden method
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=112346"
	 */
	public void testBug112346() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"Test.java",
				"/**\n" +
				" * Test references\n" +
				" * @see Test#field\n" +
				" * @see Test#foo()\n" +
				" */\n" +
				"public class Test<T> {\n" +
				"	T field;\n" +
				"	T foo() { return null; }\n" +
				"}\n"
			}
		);
	}

	/**
	 * Bug 119857: [javadoc] Some inner class references should be flagged as unresolved
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=119857"
	 */
	public void testBug119857() {
		runConformTest(
			new String[] {
				"DefaultInformationControl.java",
				"public class DefaultInformationControl {\n" +
				"	public interface IInformationPresenter {\n" +
				"		/**\n" +
				"		 * Updates the given presentation of the given information and\n" +
				"		 * thereby may manipulate the information to be displayed. The manipulation\n" +
				"		 * could be the extraction of textual encoded style information etc. Returns the\n" +
				"		 * manipulated information.\n" +
				"		 *\n" +
				"		 * @param hoverInfo the information to be presented\n" +
				"		 * @param maxWidth the maximal width in pixels\n" +
				"		 * @param maxHeight the maximal height in pixels\n" +
				"		 *\n" +
				"		 * @return the manipulated information\n" +
				"		 * @deprecated As of 3.2, replaced by {@link IInformationPresenterExtension#updatePresentation(String, int, int)}\n" +
				"		 * 				see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=38528 for details.\n" +
				"		 */\n" +
				"		String updatePresentation(String hoverInfo, int maxWidth, int maxHeight);\n" +
				"	}\n" +
				"	/**\n" +
				"	 * An information presenter determines the style presentation\n" +
				"	 * of information displayed in the default information control.\n" +
				"	 * The interface can be implemented by clients.\n" +
				"	 * \n" +
				"	 * @since 3.2\n" +
				"	 */\n" +
				"	public interface IInformationPresenterExtension {\n" +
				"		\n" +
				"		/**\n" +
				"		 * Updates the given presentation of the given information and\n" +
				"		 * thereby may manipulate the information to be displayed. The manipulation\n" +
				"		 * could be the extraction of textual encoded style information etc. Returns the\n" +
				"		 * manipulated information.\n" +
				"		 * <p>\n" +
				"		 * Replaces {@link IInformationPresenter#updatePresentation(String, int, int)}\n" +
				"		 * <em>Make sure that you do not pass in a <code>Display</code></em> until\n" +
				"		 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=38528 is fixed.\n" +
				"		 * </p>\n" +
				"		 *\n" +
				"		 * @param hoverInfo the information to be presented\n" +
				"		 * @param maxWidth the maximal width in pixels\n" +
				"		 * @param maxHeight the maximal height in pixels\n" +
				"		 *\n" +
				"		 * @return the manipulated information\n" +
				"		 */\n" +
				"		String updatePresentation(String hoverInfo, int maxWidth, int maxHeight);\n" +
				"	}\n" +
				"}\n"
			}
		);
	}
	public void testBug119857_Fields() {
		runConformTest(
			new String[] {
				"TestFields.java",
				"/**\n" +
				" * @see MyInnerClass#foo\n" +
				" */\n" +
				"public class TestFields {\n" +
				"    /**\n" +
				"     * @see MyInnerClass#foo\n" +
				"     */\n" +
				"    public class MyInnerClass {\n" +
				"            Object foo;\n" +
				"    }\n" +
				"}"
			}
		);
	}
	public void testBug119857_Methods() {
		runConformTest(
			new String[] {
				"TestMethods.java",
				"/**\n" +
				" * @see MyInnerClass#foo()\n" +
				" * @see MyInnerInterface#foo()\n" +
				" */\n" +
				"public class TestMethods {\n" +
				"    /**\n" +
				"     * @see MyInnerInterface#foo()\n" +
				"     */\n" +
				"    public class MyInnerClass {\n" +
				"            public void foo() {}\n" +
				"    }\n" +
				"    /**\n" +
				"     * @see MyInnerClass#foo()\n" +
				"     */\n" +
				"    public interface MyInnerInterface {\n" +
				"            public void foo();\n" +
				"    }\n" +
				"}"
			}
		);
	}
	public void testBug119857_Types() {
		runConformTest(
			new String[] {
				"TestTypes.java",
				"/**\n" +
				" * @see MyInnerClass\n" +
				" * @see MyInnerInterface\n" +
				" */\n" +
				"public class TestTypes {\n" +
				"	/**\n" +
				"	 * @see MyInnerInterface\n" +
				"	 */\n" +
				"	public class MyInnerClass {\n" +
				"	        public void foo() {}\n" +
				"	}\n" +
				"	/**\n" +
				"	 * @see MyInnerClass\n" +
				"	 */\n" +
				"	public interface MyInnerInterface {\n" +
				"	        public void foo();\n" +
				"	}\n" +
				"}"
			}
		);
	}
	public void testBug119857_Private01() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"pack/Test.java",
				"package pack;\n" +
				"public class Test {\n" +
				"	static class Inner {\n" +
				"		public Object foo() { return null; }\n" +
				"	}\n" +
				"	public Inner field;\n" +
				"	/** \n" +
				"	 * @see Inner#foo()\n" +
				"	 */\n" +
				"	public Object foo() {\n" +
				"		return field.foo();\n" +
				"	}\n" +
				"}\n"
			}
		);
	}
	public void testBug119857_Public01() {
		this.reportMissingJavadocTags = CompilerOptions.DISABLED;
		this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(
			new String[] {
				"pack/Test.java",
				"package pack;\n" +
				"public class Test {\n" +
				"	static class Inner {\n" +
				"		public Object foo() { return null; }\n" +
				"	}\n" +
				"	public Inner field;\n" +
				"	/** \n" +
				"	 * @see Inner#foo()\n" +
				"	 */\n" +
				"	public Object foo() {\n" +
				"		return field.foo();\n" +
				"	}\n" +
				"}\n"
			},
			//pack/Test.java:13: warning - Tag @see: reference not found: Inner1#foo()
			"----------\n" +
			"1. ERROR in pack\\Test.java (at line 8)\r\n" +
			"	* @see Inner#foo()\r\n" +
			"	       ^^^^^\n" +
			"Javadoc: \'public\' visibility for malformed doc comments hides this \'default\' reference\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	public void testBug119857_Private02() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"test/Test.java",
				"package test;\n" +
				"public class Test {\n" +
				"	static class Inner1 {\n" +
				"		public Object foo() { return null; }\n" +
				"	}\n" +
				"	static class Inner2 {\n" +
				"		public Inner1 field;\n" +
				"		/** \n" +
				"		 * @see Inner1#foo()\n" +
				"		 */\n" +
				"		public Object foo() {\n" +
				"			return field.foo();\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
			}
		);
	}
	public void testBug119857_Public02() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
		runConformTest(
			new String[] {
				"test/Test.java",
				"package test;\n" +
				"public class Test {\n" +
				"	static class Inner1 {\n" +
				"		public Object foo() { return null; }\n" +
				"	}\n" +
				"	static class Inner2 {\n" +
				"		public Inner1 field;\n" +
				"		/** \n" +
				"		 * @see Inner1#foo()\n" +
				"		 */\n" +
				"		public Object foo() {\n" +
				"			return field.foo();\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
			}
		);
	}
	public void testBug119857_Public03() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
		runNegativeTest(
			new String[] {
				"pack/Test.java",
				"package pack;\n" +
				"public class Test {\n" +
				"	static class Inner1 {\n" +
				"		public Object foo() { return null; }\n" +
				"	}\n" +
				"	public static class Inner2 {\n" +
				"		public Inner1 field;\n" +
				"		/** \n" +
				"		 * @see Inner1#foo()\n" +
				"		 */\n" +
				"		public Object foo() {\n" +
				"			return field.foo();\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
			},
			//pack/Test.java:13: warning - Tag @see: reference not found: Inner1#foo()
			"----------\n" +
			"1. ERROR in pack\\Test.java (at line 9)\n" +
			"	* @see Inner1#foo()\n" +
			"	       ^^^^^^\n" +
			"Javadoc: \'public\' visibility for malformed doc comments hides this \'default\' reference\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	/**
	 * Bug 132430: [1.5][javadoc] Unwanted missing tag warning for overridden method with parameter containing type variable
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=132430"
	 */
	public void testBug132430() {
		runConformTest(
			new String[] {
				"A.java",
				"public class A<E> {\n" +
				"    /**\n" +
				"     * @param object\n" +
				"     */\n" +
				"    public void aMethod(E object) {}\n" +
				"}",
				"B.java",
				"public class B<E> extends A<E> {\n" +
				"	/**\n" +
				"	 * @see A#aMethod(java.lang.Object)\n" +
				"	 */\n" +
				"	@Override\n" +
				"	public void aMethod(E object) {\n" +
				"		super.aMethod(object);\n" +
				"	}\n" +
				"}\n"
			}
		);
	}
	public void testBug132430b() {
		runConformTest(
			new String[] {
				"A.java",
				"public class A<E> {\n" +
				"    /**\n" +
				"     * @param object\n" +
				"     */\n" +
				"    public void aMethod(E object) {}\n" +
				"}",
				"B.java",
				"public class B<E> extends A<E> {\n" +
				"	/**\n" +
				"	 * @see A#aMethod(java.lang.Object)\n" +
				"	 */\n" +
				"	public void aMethod(E object) {\n" +
				"		super.aMethod(object);\n" +
				"	}\n" +
				"}\n"
			}
		);
	}
	public void testBug132430c() {
		runConformTest(
			new String[] {
				"A.java",
				"public class A<E> {\n" +
				"    /**\n" +
				"     * @param object\n" +
				"     */\n" +
				"    public void aMethod(E object) {}\n" +
				"}",
				"B.java",
				"public class B<E> extends A<E> {\n" +
				"	/**\n" +
				"	 * Empty comment\n" +
				"	 */\n" +
				"	@Override\n" +
				"	public void aMethod(E object) {\n" +
				"		super.aMethod(object);\n" +
				"	}\n" +
				"}\n"
			}
		);
	}

	/**
	 * Bug 145007: [1.5][javadoc] Generics + Inner Class -> Javadoc "missing @throws" warning
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=145007"
	 */
	public void testBug145007() {
		runConformTest(
			new String[] {
				"TestClass.java",
				"class TestClass<T> {\n" +
				"    static class Test1 {\n" +
				"        /**\n" +
				"         * A simple method that demonstrates tag problems\n" +
				"         * \n" +
				"         * @return a string\n" +
				"         * @throws MyException\n" +
				"         *             if something goes wrong\n" +
				"         */\n" +
				"        public String getString() throws MyException {\n" +
				"            throw new MyException();\n" +
				"        }\n" +
				"    }\n" +
				"    static class MyException extends Exception {\n" +
				"        private static final long serialVersionUID = 1L;\n" +
				"    }\n" +
				"}"
			}
		);
	}


	/**
	 * Bug 87500: [1.5][javadoc][options] Add a 'Consider enum values' option to warn/error on 'Missing javadoc comments'.
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=87500"
	 */
	public void testBug87500a() {
		this.reportMissingJavadocComments = CompilerOptions.ERROR;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.DEFAULT;
		runNegativeTest(
			new String[] {
				"A.java",
				"enum A {\n" +
				"	clubs,\n" +
				"	diamonds,\n" +
				"	hearts,\n" +
				"	spades\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in A.java (at line 1)\n" +
			"	enum A {\n" +
			"	     ^\n" +
			"Javadoc: Missing comment for default declaration\n" +
			"----------\n" +
			"2. ERROR in A.java (at line 2)\n" +
			"	clubs,\n" +
			"	^^^^^\n" +
			"Javadoc: Missing comment for default declaration\n" +
			"----------\n" +
			"3. ERROR in A.java (at line 3)\n" +
			"	diamonds,\n" +
			"	^^^^^^^^\n" +
			"Javadoc: Missing comment for default declaration\n" +
			"----------\n" +
			"4. ERROR in A.java (at line 4)\n" +
			"	hearts,\n" +
			"	^^^^^^\n" +
			"Javadoc: Missing comment for default declaration\n" +
			"----------\n" +
			"5. ERROR in A.java (at line 5)\n" +
			"	spades\n" +
			"	^^^^^^\n" +
			"Javadoc: Missing comment for default declaration\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void testBug87500b() {
		this.reportMissingJavadocComments = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"A.java",
				"enum A {\n" +
				"	clubs,\n" +
				"	diamonds,\n" +
				"	hearts,\n" +
				"	spades\n" +
				"}\n"
			});
	}

	/**
	 * Bug 204749  [1.5][javadoc] NPE in JavadocQualifiedTypeReference
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=204749"
	 */
	public void testBug204749a() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportMissingJavadocComments = CompilerOptions.IGNORE;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
			    "    /** @see T.R */\n" +
			    "    void foo() {}\n" +
				"}"
				},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	/** @see T.R */\n" +
			"	         ^^^\n" +
			"Javadoc: Invalid reference\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void testBug204749b() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportMissingJavadocComments = CompilerOptions.IGNORE;
		this.reportInvalidJavadoc = CompilerOptions.IGNORE;
		runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" +
			    "    /** @see T.R */\n" +
			    "    void foo() {}\n" +
				"}"
			}
		);
	}

	/**
	 * Bug 209936  Missing code implementation in the compiler on inner classes
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=209936"
	 */
	public void testBug209936a() {
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public abstract class X extends Y {\n" +
				"	protected class A extends Member {\n" +
				"		/**\n" +
				"		 * @see Member#foo(Object, Object)\n" +
				"		 */\n" +
				"		public void foo(Object source, Object data) {}\n" +
				"	}\n" +
				"}",
				"p/Y.java",
				"package p;\n" +
				"import p1.Z;\n" +
				"public abstract class Y extends Z<Object> {\n" +
				"}",
				"p1/Z.java",
				"package p1;\n" +
				"public abstract class Z<T> {\n" +
				"	protected class Member {\n" +
				"		protected void foo(Object source, Object data) {\n" +
				"		}\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in p\\X.java (at line 5)\n" +
			"	* @see Member#foo(Object, Object)\n" +
			"	       ^^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void testBug209936b() {
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public abstract class X extends Y {\n" +
				"	protected class A extends Member {\n" +
				"		/**\n" +
				"		 * @see Member#foo(Object, Object)\n" +
				"		 */\n" +
				"		public void foo(Object source, Object data) {}\n" +
				"	}\n" +
				"}",
				"p/Y.java",
				"package p;\n" +
				"\n" +
				"import p1.Z;\n" +
				"public abstract class Y extends Z<Object> {}",
				"p1/Z.java",
				"package p1;\n" +
				"public abstract class Z<T> {\n" +
				"	protected class Member {\n" +
				"		protected void foo(Object source, Object data) {}\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in p\\X.java (at line 5)\n" +
			"	* @see Member#foo(Object, Object)\n" +
			"	       ^^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void testBug209936_GenericMemberImplicitReference() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"p1/A.java",
				"package p1;\n" +
				"public class A<R> {\n" +
				"	public class A1<S> {\n" +
				"		public class A2<T> {\n" +
				"			public class A3<U> {\n" +
				"				public class A4<V> {\n" +
				"					public void foo(V v) {}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}",
				"p2/X.java",
				"package p2;\n" +
				"import p1.A;\n" +
				"public class X<R> extends A<R> {\n" +
				"	public class X1<S> extends A1<S> {\n" +
				"		public class X2<T> extends A2<T> {\n" +
				"			public class X3<U> extends A3<U> {\n" +
				"				public class X4<V> extends A4<V> {\n" +
				"					/**\n" +
									// implicit type reference
				"			 		 * @see #foo(Object)\n" +
				"			 		 * @see #foo(V)\n" +
				"					 */\n" +
				"					public void foo(V v) {}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in p2\\X.java (at line 10)\n" +
			"	* @see #foo(V)\n" +
			"	        ^^^\n" +
			"Javadoc: The method foo(Object) in the type X.X1.X2.X3.X4 is not applicable for the arguments (V)\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void testBug209936_GenericMemberSingleReference() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"p1/A.java",
				"package p1;\n" +
				"public class A<R> {\n" +
				"	public class A1<S> {\n" +
				"		public class A2<T> {\n" +
				"			public class A3<U> {\n" +
				"				public class A4<V> {\n" +
				"					public void foo(V v) {}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}",
				"p2/X.java",
				"package p2;\n" +
				"import p1.A;\n" +
				"public class X<R> extends A<R> {\n" +
				"	public class X1<S> extends A1<S> {\n" +
				"		public class X2<T> extends A2<T> {\n" +
				"			public class X3<U> extends A3<U> {\n" +
				"				public class X4<V> extends A4<V> {\n" +
				"					/**\n" +
									// single type reference
				"			 		 * @see A4#foo(V)\n" +
				"			 		 * @see A4#foo(Object)\n" +
				"					 */\n" +
				"					public void myFoo(V v) {}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in p2\\X.java (at line 9)\n" +
			"	* @see A4#foo(V)\n" +
			"	          ^^^\n" +
			"Javadoc: The method foo(Object) in the type A.A1.A2.A3.A4 is not applicable for the arguments (V)\n" +
			"----------\n" +
			"2. ERROR in p2\\X.java (at line 10)\n" +
			"	* @see A4#foo(Object)\n" +
			"	       ^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void testBug209936_GenericMemberQualifiedSingleReference() {
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"p1/A.java",
				"package p1;\n" +
				"public class A<R> {\n" +
				"	public class A1<S> {\n" +
				"		public class A2<T> {\n" +
				"			public class A3<U> {\n" +
				"				public class A4<V> {\n" +
				"					public void foo(V v) {}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}",
				"p2/X.java",
				"package p2;\n" +
				"import p1.A;\n" +
				"public class X<R> extends A<R> {\n" +
				"	public class X1<S> extends A1<S> {\n" +
				"		public class X2<T> extends A2<T> {\n" +
				"			public class X3<U> extends A3<U> {\n" +
				"				public class X4<V> extends A4<V> {\n" +
				"					/**\n" +
									// qualified single type reference
				"			 		 * @see A3.A4#foo(V)\n" +
				"			 		 * @see p1.A.A1.A2.A3.A4#foo(Object)\n" +
				"					 */\n" +
				"					public void foo(V v) {}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in p2\\X.java (at line 9)\n" +
			"	* @see A3.A4#foo(V)\n" +
			"	             ^^^\n" +
			"Javadoc: The method foo(Object) in the type A.A1.A2.A3.A4 is not applicable for the arguments (V)\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void testBug209936_GenericMemberFullyQualifiedSingleReference() {
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"p1/A.java",
				"package p1;\n" +
				"public class A<R> {\n" +
				"	public class A1<S> {\n" +
				"		public class A2<T> {\n" +
				"			public class A3<U> {\n" +
				"				public class A4<V> {\n" +
				"					public void foo(V v) {}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}",
				"p2/X.java",
				"package p2;\n" +
				"import p1.A;\n" +
				"public class X<R> extends A<R> {\n" +
				"	public class X1<S> extends A1<S> {\n" +
				"		public class X2<T> extends A2<T> {\n" +
				"			public class X3<U> extends A3<U> {\n" +
				"				public class X4<V> extends A4<V> {\n" +
				"					/**\n" +
									// fully qualified type reference
				"			 		 * @see p1.A.A1.A2.A3.A4#foo(V)\n" +
				"			 		 * @see p1.A.A1.A2.A3.A4#foo(Object)\n" +
				"					 */\n" +
				"					public void foo(V v) {}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in p2\\X.java (at line 9)\n" +
			"	* @see p1.A.A1.A2.A3.A4#foo(V)\n" +
			"	                        ^^^\n" +
			"Javadoc: The method foo(Object) in the type A.A1.A2.A3.A4 is not applicable for the arguments (V)\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void testBug209936_MemberImplicitReference() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"p1/A.java",
				"package p1;\n" +
				"public class A<R> {\n" +
				"	public class A1<S> {\n" +
				"		public class A2<T> {\n" +
				"			public class A3<U> {\n" +
				"				public class A4 {\n" +
				"					public void foo(U u) {}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}",
				"p2/X.java",
				"package p2;\n" +
				"import p1.A;\n" +
				"public class X<R> extends A<R> {\n" +
				"	public class X1<S> extends A1<S> {\n" +
				"		public class X2<T> extends A2<T> {\n" +
				"			public class X3<U> extends A3<U> {\n" +
				"				public class X4 extends A4 {\n" +
				"					/**\n" +
									// implicit reference
				"			 		 * @see #foo(Object)\n" +
				"			 		 * @see #foo(U u)\n" +
				"					 */\n" +
				"					public void foo(U u) {}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in p2\\X.java (at line 10)\r\n" +
			"	* @see #foo(U u)\r\n" +
			"	        ^^^\n" +
			"Javadoc: The method foo(Object) in the type X.X1.X2.X3.X4 is not applicable for the arguments (U)\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void testBug209936_MemberSingleReference1(){
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"p1/A.java",
				"package p1;\n" +
				"public class A<R> {\n" +
				"	public class A1<S> {\n" +
				"		public class A2<T> {\n" +
				"			public class A3 {\n" +
				"				public class A4 {\n" +
				"					public void foo(T t) {}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}",
				"p2/X.java",
				"package p2;\n" +
				"import p1.A;\n" +
				"public class X<R> extends A<R> {\n" +
				"	public class X1<S> extends A1<S> {\n" +
				"		public class X2<T> extends A2<T> {\n" +
				"			public class X3 extends A3 {\n" +
				"				public class X4 extends A4 {\n" +
				"					/**\n" +
									// single type reference
				"			 		 * @see A4#foo(Object)\n" +
				"			 		 * @see A4#foo(T)\n" +
				"					 */\n" +
				"					public void foo(T t) {}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in p2\\X.java (at line 9)\n" +
			"	* @see A4#foo(Object)\n" +
			"	       ^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n" +
			"2. ERROR in p2\\X.java (at line 10)\n" +
			"	* @see A4#foo(T)\n" +
			"	          ^^^\n" +
			"Javadoc: The method foo(Object) in the type A.A1.A2.A3.A4 is not applicable for the arguments (T)\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void testBug209936_MemberSingleReference2(){
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"p1/A.java",
				"package p1;\n" +
				"public class A<R> {\n" +
				"	public class A1<S> {\n" +
				"		public class A2 {\n" +
				"			public class A3 {\n" +
				"				public class A4 {\n" +
				"					public void foo(S s) {}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}",
				"p2/X.java",
				"package p2;\n" +
				"import p1.A;\n" +
				"public class X<R> extends A<R> {\n" +
				"	public class X1<S> extends A1<S> {\n" +
				"		public class X2 extends A2 {\n" +
				"			public class X3 extends A3 {\n" +
				"				public class X4 extends A4 {\n" +
				"					/**\n" +
									// single type reference
				"			 		 * @see A4#foo(Object)\n" +
				"			 		 * @see A4#foo(S)\n" +
				"					 */\n" +
				"					public void foo(S s) {}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in p2\\X.java (at line 9)\n" +
			"	* @see A4#foo(Object)\n" +
			"	       ^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n" +
			"2. ERROR in p2\\X.java (at line 10)\n" +
			"	* @see A4#foo(S)\n" +
			"	          ^^^\n" +
			"Javadoc: The method foo(Object) in the type A.A1.A2.A3.A4 is not applicable for the arguments (S)\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void testBug209936_MemberSingleReference3(){
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"p1/A.java",
				"package p1;\n" +
				"public class A {\n" +
				"	public class A1 {\n" +
				"		public class A2<T> {\n" +
				"			public class A3 {\n" +
				"				public class A4 {\n" +
				"					public void foo(T t) {}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}",
				"p2/X.java",
				"package p2;\n" +
				"import p1.A;\n" +
				"public class X extends A {\n" +
				"	public class X1 extends A1 {\n" +
				"		public class X2<T> extends A2<T> {\n" +
				"			public class X3 extends A3 {\n" +
				"				public class X4 extends A4 {\n" +
				"					/**\n" +
									// single type reference
				"			 		 * @see A4#foo(Object)\n" +
				"			 		 * @see A4#foo(T)\n" +
				"					 */\n" +
				"					public void foo(T t) {}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in p2\\X.java (at line 9)\n" +
			"	* @see A4#foo(Object)\n" +
			"	       ^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n" +
			"2. ERROR in p2\\X.java (at line 10)\n" +
			"	* @see A4#foo(T)\n" +
			"	          ^^^\n" +
			"Javadoc: The method foo(Object) in the type A.A1.A2.A3.A4 is not applicable for the arguments (T)\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void testBug209936_MemberSingleReference4(){
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"p1/A.java",
				"package p1;\n" +
				"public class A {\n" +
				"	public class A1 {\n" +
				"		public class A2<T> {\n" +
				"			public class A3 {\n" +
				"				public class A4 {\n" +
				"					public void foo(T t) {}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}",
				"p2/X.java",
				"package p2;\n" +
				"import p1.A;\n" +
				"public class X extends A {\n" +
				"	public class X1 extends A1 {\n" +
				"		public class X2<T> extends A2<T> {\n" +
				"			public class X3 extends A3 {\n" +
				"				public class X4 extends A4 {\n" +
				"					/**\n" +
				// single type reference
				"			 		 * @see A4#foo(Object)\n" +
				"			 		 * @see A4#foo(T)\n" +
				"					 */\n" +
				"					public void foo(T t) {}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in p2\\X.java (at line 9)\n" +
			"	* @see A4#foo(Object)\n" +
			"	       ^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n" +
			"2. ERROR in p2\\X.java (at line 10)\n" +
			"	* @see A4#foo(T)\n" +
			"	          ^^^\n" +
			"Javadoc: The method foo(Object) in the type A.A1.A2.A3.A4 is not applicable for the arguments (T)\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}

	public void testBug209936_MemberQualifiedSingleReference1() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runConformTest(
			new String[] {
				"p1/A.java",
				"package p1;\n" +
				"\n" +
				"public class A<R> {\n" +
				"	public class A1<S> {\n" +
				"		public class A2 {\n" +
				"			public class A3 {\n" +
				"				public class A4 {\n" +
				"					public void foo(S s) {}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}",
				"p2/X.java",
				"package p2;\n" +
				"import p1.A;\n" +
				"public class X<R> extends A<R> {\n" +
				"	public class X1<S> extends A1<S> {\n" +
				"		public class X2 extends A2 {\n" +
				"			public class X3 extends A3 {\n" +
				"				public class X4 extends A4 {\n" +
				"					/**\n" +
									// qualified single type reference
				"			 		 * @see p1.A.A1.A2.A3.A4#foo(Object)\n" +
				"					 */\n" +
				"					public void foo(S s) {}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
			}
		);
	}

	public void testBug209936_MemberQualifiedSingleReference2() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runConformTest(
			new String[] {
				"p1/A.java",
				"package p1;\n" +
				"\n" +
				"public class A<R> {\n" +
				"	public class A1<S> {\n" +
				"		public class A2 {\n" +
				"			public class A3 {\n" +
				"				public class A4 {\n" +
				"					public class A5 {\n" +
				"						public class A6 {\n" +
				"							public void foo(S s) {}\n" +
				"						}\n" +
				"					}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}",
				"p2/X.java",
				"package p2;\n" +
				"import p1.A;\n" +
				"public class X<R> extends A<R> {\n" +
				"	public class X1<S> extends A1<S> {\n" +
				"		public class X2 extends A2 {\n" +
				"			public class X3 extends A3 {\n" +
				"				public class X4 extends A4 {\n" +
				"					public class X5 extends A5 {\n" +
				"						public class X6 extends A6 {\n" +
				"							/**\n" +
											// qualified single type reference
				"			 				 * @see p1.A.A1.A2.A3.A4.A5.A6#foo(Object)\n" +
				"							 */\n" +
				"							public void foo(S s) {}\n" +
				"						}\n" +
				"					}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
			}
		);
	}

	public void testBug209936_MemberFullyQualifiedSingleReference() {
		this.reportMissingJavadocCommentsVisibility = CompilerOptions.WARNING;
		runNegativeTest(
			new String[] {
				"p1/A.java",
				"package p1;\n" +
				"\n" +
				"public class A<R> {\n" +
				"	public class A1 {\n" +
				"		public class A2 {\n" +
				"			public class A3 {\n" +
				"				public class A4 {\n" +
				"					public void foo(R r) {}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}",
				"p2/X.java",
				"package p2;\n" +
				"import p1.A;\n" +
				"public class X<R> extends A<R> {\n" +
				"	public class X1 extends A1 {\n" +
				"		public class X2 extends A2 {\n" +
				"			public class X3 extends A3 {\n" +
				"				public class X4 extends A4 {\n" +
				"					/**\n" +
									// fully qualified type reference
				"			 		 * @see p1.A.A1.A2.A3.A4#foo(Object)\n" +
				"			 		 * @see p1.A.A1.A2.A3.A4#foo(R)\n" +
				"					 */\n" +
				"					public void foo(R r) {}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in p2\\X.java (at line 10)\r\n" +
			"	* @see p1.A.A1.A2.A3.A4#foo(R)\r\n" +
			"	                        ^^^\n" +
			"Javadoc: The method foo(Object) in the type A.A1.A2.A3.A4 is not applicable for the arguments (R)\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=247037, verify that we complain about @inheritDoc
	// being used in package level javadoc.
	public void testBug247037a() {
		runNegativeTest(
			new String[] {
				"pack/package-info.java",
				"/**\n" +
				" * {@inheritDoc}\n" +
				" * @since {@inheritDoc}\n" +
				" * @blah {@inheritDoc}\n" +
				" */\n" +
				"package pack;\n"
			},
			"----------\n" +
			"1. ERROR in pack\\package-info.java (at line 2)\n" +
			"	* {@inheritDoc}\n" +
			"	    ^^^^^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"2. ERROR in pack\\package-info.java (at line 3)\n" +
			"	* @since {@inheritDoc}\n" +
			"	           ^^^^^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"3. ERROR in pack\\package-info.java (at line 4)\n" +
			"	* @blah {@inheritDoc}\n" +
			"	          ^^^^^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n"
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=247037, verify that we complain about @inheritDoc
	// being used in package level javadoc (variation)
	public void testBug247037b() {
		runNegativeTest(
			new String[] {
				"pack/package-info.java",
				"/**\n" +
				" * @return {@inheritDoc}\n" +
				" * @param blah {@inheritDoc}\n" +
				" */\n" +
				"package pack;\n"
			},
			"----------\n" +
			"1. ERROR in pack\\package-info.java (at line 2)\n" +
			"	* @return {@inheritDoc}\n" +
			"	   ^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"2. ERROR in pack\\package-info.java (at line 2)\n" +
			"	* @return {@inheritDoc}\n" +
			"	            ^^^^^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"3. ERROR in pack\\package-info.java (at line 3)\n" +
			"	* @param blah {@inheritDoc}\n" +
			"	   ^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"4. ERROR in pack\\package-info.java (at line 3)\n" +
			"	* @param blah {@inheritDoc}\n" +
			"	                ^^^^^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n"
		);
	}
	/**
	 * @bug 286918:[javadoc] Compiler should warn when @see and @link tag references in package-info.java don't have fully qualified names
	 * @test that in a package-info.java file
	 * 				1. References to valid packages are ACCEPTED without any warnings or errors
	 * 				2. References to valid Java elements (including the ones in the same package) without qualified names are REPORTED as errors
	 * 				3. References to valid Java elements with qualified names are ACCEPTED
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=286918"
	 */
	public void testBug284333() {
		runNegativeTest(new String[]{
				"goo/bar/package-info.java",
				"/**\n" +
				"*/\n" +
				"package goo.bar;\n",
				"foo/bar/ClassInSamePackage.java",
				"package foo.bar;\n" +
				"public class ClassInSamePackage {\n" +
				"	public static int SOME_FIELD; \n" +
				"}\n",
				"foo/bar/goo/ClassInSubPackage.java",
				"package foo.bar.goo;\n" +
				"public class ClassInSubPackage {\n" +
				"	public static void foo() { \n" +
				"   }\n" +
				"}\n",
				"foo/ClassInEnclosingPackage.java",
				"package foo;\n" +
				"public class ClassInEnclosingPackage {\n" +
				"	public static int SOME_FIELD; \n" +
				"}\n",
				"foo/bar/package-info.java",
				"/**\n" +
				" * @see ClassInSamePackage#SOME_FIELD\n" +
				" * @see foo.bar.ClassInSamePackage#SOME_FIELD\n" +
				" * @see ClassInSamePackage#SOME_FIELD\n" +
				" * @see ClassInSubPackage#foo\n" +
				" * @see foo.bar.goo.ClassInSubPackage#foo\n" +
				" * @see ClassInSubPackage#foo\n" +
				" * @see ClassInEnclosingPackage\n" +
				" * @see foo.ClassInEnclosingPackage\n" +
				" * @see ClassInEnclosingPackage\n" +
				" * @see foo.bar\n" +
				" * @see goo.bar\n" +
				" * @see foo.bar.goo\n" +
				" */\n" +
				"package foo.bar;\n"
		},
		"----------\n" +
		"1. ERROR in foo\\bar\\package-info.java (at line 2)\n" +
		"	* @see ClassInSamePackage#SOME_FIELD\n" +
		"	       ^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: Invalid reference\n" +
		"----------\n" +
		"2. ERROR in foo\\bar\\package-info.java (at line 4)\n" +
		"	* @see ClassInSamePackage#SOME_FIELD\n" +
		"	       ^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: Invalid reference\n" +
		"----------\n" +
		"3. ERROR in foo\\bar\\package-info.java (at line 5)\n" +
		"	* @see ClassInSubPackage#foo\n" +
		"	       ^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: ClassInSubPackage cannot be resolved to a type\n" +
		"----------\n" +
		"4. ERROR in foo\\bar\\package-info.java (at line 7)\n" +
		"	* @see ClassInSubPackage#foo\n" +
		"	       ^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: ClassInSubPackage cannot be resolved to a type\n" +
		"----------\n" +
		"5. ERROR in foo\\bar\\package-info.java (at line 8)\n" +
		"	* @see ClassInEnclosingPackage\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: ClassInEnclosingPackage cannot be resolved to a type\n" +
		"----------\n" +
		"6. ERROR in foo\\bar\\package-info.java (at line 10)\n" +
		"	* @see ClassInEnclosingPackage\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: ClassInEnclosingPackage cannot be resolved to a type\n" +
		"----------\n");
	}

	/**
	 * Additional tests for "https://bugs.eclipse.org/bugs/show_bug.cgi?id=286918"
	 * @test that in a non package-info.java file
	 * 				2. References without qualified names to valid Java elements in the same package are ACCEPTED
	 * 	 			2. References without qualified names to valid Java elements in other packages are REPORTED
	 * 				3. References with qualified names to valid Java elements are accepted
	 */
	public void testBug284333a() {
		runNegativeTest(new String[]{
				"goo/bar/package-info.java",
				"/**\n" +
				"*/\n" +
				"package goo.bar;\n",
				"foo/bar/ClassInSamePackage.java",
				"package foo.bar;\n" +
				"public class ClassInSamePackage {\n" +
				"	public static int SOME_FIELD; \n" +
				"}\n",
				"foo/bar/goo/ClassInSubPackage.java",
				"package foo.bar.goo;\n" +
				"public class ClassInSubPackage {\n" +
				"	public static void foo() { \n" +
				"   }\n" +
				"}\n",
				"foo/ClassInEnclosingPackage.java",
				"package foo;\n" +
				"public class ClassInEnclosingPackage {\n" +
				"	public static int SOME_FIELD; \n" +
				"}\n",
				"foo/bar/NotAPackageInfo.java",
				"package foo.bar;\n" +
				"/**\n" +
				" * @see ClassInSamePackage#SOME_FIELD\n" +
				" * @see foo.bar.ClassInSamePackage#SOME_FIELD\n" +
				" * @see ClassInSamePackage#SOME_FIELD\n" +
				" */\n" +
				" public class NotAPackageInfo {\n" +
				"/**\n" +
				" * @see ClassInSubPackage#foo\n" +
				" * @see foo.bar.goo.ClassInSubPackage#foo\n" +
				" * @see ClassInSubPackage#foo\n" +
				" */\n" +
				"	public static int SOME_FIELD = 0;\n" +
				"/**\n" +
				" * @see ClassInEnclosingPackage\n" +
				" * @see foo.ClassInEnclosingPackage\n" +
				" * @see ClassInEnclosingPackage\n" +
				" */\n" +
				" 	public static void foo() {\n" +
				"	}\n" +
				"	" +
				" }\n"
		},
		"----------\n" +
		"1. ERROR in foo\\bar\\NotAPackageInfo.java (at line 9)\n" +
		"	* @see ClassInSubPackage#foo\n" +
		"	       ^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: ClassInSubPackage cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in foo\\bar\\NotAPackageInfo.java (at line 11)\n" +
		"	* @see ClassInSubPackage#foo\n" +
		"	       ^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: ClassInSubPackage cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in foo\\bar\\NotAPackageInfo.java (at line 15)\n" +
		"	* @see ClassInEnclosingPackage\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: ClassInEnclosingPackage cannot be resolved to a type\n" +
		"----------\n" +
		"4. ERROR in foo\\bar\\NotAPackageInfo.java (at line 17)\n" +
		"	* @see ClassInEnclosingPackage\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: ClassInEnclosingPackage cannot be resolved to a type\n" +
		"----------\n");
	}
	/**
	 * Additional tests for "https://bugs.eclipse.org/bugs/show_bug.cgi?id=284333"
	 * @test that in a non package-info.java file
	 * 	 			2. References without qualified names to imported Java elements in other packages are ACCEPTED
	 * 				3. References with qualified names to valid Java elements are ACCEPTED
	 */
	public void testBug284333b() {
		runConformTest(new String[] {
				"goo/bar/package-info.java",
				"/**\n" +
				"*/\n" +
				"package goo.bar;\n",
				"foo/bar/ClassInSamePackage.java",
				"package foo.bar;\n" +
				"public class ClassInSamePackage {\n" +
				"	public static int SOME_FIELD; \n" +
				"}\n",
				"foo/bar/goo/ClassInSubPackage.java",
				"package foo.bar.goo;\n" +
				"public class ClassInSubPackage {\n" +
				"	public static void foo() { \n" +
				"   }\n" +
				"}\n",
				"foo/ClassInEnclosingPackage.java",
				"package foo;\n" +
				"public class ClassInEnclosingPackage {\n" +
				"	public static int SOME_FIELD; \n" +
				"}\n",
				"foo/bar/NotAPackageInfo.java",
				"package foo.bar;\n" +
				"import foo.*;\n" +
				"import foo.bar.goo.*;\n" +
				"/**\n" +
				" * @see ClassInSamePackage#SOME_FIELD\n" +
				" * @see foo.bar.ClassInSamePackage#SOME_FIELD\n" +
				" * @see ClassInSamePackage#SOME_FIELD\n" +
				" * @see goo.bar\n" +
				" */\n" +
				" public class NotAPackageInfo {\n" +
				"/**\n" +
				" * @see ClassInSubPackage#foo\n" +
				" * @see foo.bar.goo.ClassInSubPackage#foo\n" +
				" * @see ClassInSubPackage#foo\n" +
				" * @see goo.bar\n" +
				" */\n" +
				"	public static int SOME_FIELD = 0;\n" +
				"/**\n" +
				" * @see ClassInEnclosingPackage\n" +
				" * @see foo.ClassInEnclosingPackage\n" +
				" * @see ClassInEnclosingPackage\n" +
				" * @see goo.bar\n" +
				" */\n" +
				" 	public static void foo() {\n" +
				"	}\n" +
				"	" +
				" }\n"
		});
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322581
	// To test the javadoc option CompilerOptions.OPTION_ReportMissingJavadocTagsMethodTypeParameters
	public void testBug322581a() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsMethodTypeParameters, CompilerOptions.DISABLED);
		this.runNegativeTest(
			true,
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" +
					"	 * javadoc\n" +
					"	 */\n" +
					"	public <T, U, V> void foo(int val, Object obj) {}\n" +
					"}"
			},
			null,
			options,
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	public <T, U, V> void foo(int val, Object obj) {}\n" +
			"	                              ^^^\n" +
			"Javadoc: Missing tag for parameter val\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	public <T, U, V> void foo(int val, Object obj) {}\n" +
			"	                                          ^^^\n" +
			"Javadoc: Missing tag for parameter obj\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322581
	// To test the javadoc option CompilerOptions.OPTION_ReportMissingJavadocTagsMethodTypeParameters
	public void testBug322581b() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsMethodTypeParameters, CompilerOptions.DISABLED);
		this.runNegativeTest(
			true,
			new String[] {
				"ListCallable.java",
				" import java.util.Collections;\n" +
				" import java.util.List;\n" +
				" import java.util.concurrent.Callable;\n" +
				"/**\n" +
				" * Callable that returns a list.\n" +
				" */\n" +
				"public abstract class ListCallable<V> implements Callable<List<V>> { // good warning\n" +
				"	public abstract List<V> call() throws Exception;\n" +
				"    /**\n" +
				"	 * Returns a {@link ListCallable} that wraps the result from calling <code>callable</code>.\n" +
				"    * @param callable the {@link Callable} to wrap\n" +
				"	 * @return the wrapper\n" +
				"    */\n" +
				"	public static <T> ListCallable<T> from(final Callable<T> callable) { // don't warn\n" +
				"		return new ListCallable<T>() {\n" +
				"			@Override\n" +
				"			public List<T> call() throws Exception {\n" +
				"				return Collections.singletonList(callable.call());\n" +
				"			}\n" +
				"		};\n" +
				"	}\n" +
				"}"
			},
			null,
			options,
			"----------\n" +
			"1. ERROR in ListCallable.java (at line 7)\n" +
			"	public abstract class ListCallable<V> implements Callable<List<V>> { // good warning\n" +
			"	                                   ^\n" +
			"Javadoc: Missing tag for parameter V\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331872
	public void testBug331872() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsMethodTypeParameters, CompilerOptions.DISABLED);
		this.runNegativeTest(
			true,
			new String[] {
				"X.java",
				"/**\n" +
				" * @param <p> the given type parameter\n" +
				" */\n" +
				"public class X<p> {\n" +
				"	/**\n" +
				"	 * @param o the given object\n" +
				"	 * @see #foo(p.O[])\n" +
				"	 */\n" +
				"	public void foo(Object o) {\n" +
				"	}\n" +
				"}"
			},
			null,
			options,
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	* @see #foo(p.O[])\n" +
			"	            ^^^\n" +
			"Illegal qualified access from the type parameter p\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331872
	public void testBug331872b() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsMethodTypeParameters, CompilerOptions.DISABLED);
		this.runNegativeTest(
			true,
			new String[] {
				"X.java",
				"/**\n" +
				" * @param <p> the given type parameter\n" +
				" */\n" +
				"public class X<p> {\n" +
				"	/**\n" +
				"	 * @param o the given object\n" +
				"	 * @see #foo(O[])\n" +
				"	 */\n" +
				"	public void foo(Object o) {\n" +
				"	}\n" +
				"}"
			},
			null,
			options,
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	* @see #foo(O[])\n" +
			"	            ^\n" +
			"Javadoc: O[] cannot be resolved to a type\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331872
	public void testBug331872c() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsMethodTypeParameters, CompilerOptions.DISABLED);
		this.runNegativeTest(
			true,
			new String[] {
				"X.java",
				"/**\n" +
				" * @param <p> the given type parameter\n" +
				" */\n" +
				"public class X<p> {\n" +
				"	/**\n" +
				"	 * @param o the given object\n" +
				"	 * @see #foo(test.O[])\n" +
				"	 */\n" +
				"	public void foo(Object o) {\n" +
				"	}\n" +
				"}"
			},
			null,
			options,
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	* @see #foo(test.O[])\n" +
			"	            ^^^^^^\n" +
			"Javadoc: test[] cannot be resolved to a type\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331872
	public void testBug331872d() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsMethodTypeParameters, CompilerOptions.DISABLED);
		this.runNegativeTest(
			true,
			new String[] {
				"X.java",
				"/**\n" +
				" * @param <p> the given type parameter\n" +
				" */\n" +
				"public class X<p> {\n" +
				"	/**\n" +
				"	 * @param o the given object\n" +
				"	 * @see #foo(test.O)\n" +
				"	 */\n" +
				"	public void foo(Object o) {\n" +
				"	}\n" +
				"}"
			},
			null,
			options,
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	* @see #foo(test.O)\n" +
			"	            ^^^^^^\n" +
			"Javadoc: test cannot be resolved to a type\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
}
