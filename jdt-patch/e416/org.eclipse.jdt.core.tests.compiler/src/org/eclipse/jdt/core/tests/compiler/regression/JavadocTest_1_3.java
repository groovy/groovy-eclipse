/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
public class JavadocTest_1_3 extends JavadocTest {

	String docCommentSupport = CompilerOptions.ENABLED;
	String reportInvalidJavadoc = CompilerOptions.ERROR;
	String reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
	String reportMissingJavadocTags = CompilerOptions.ERROR;
	String reportMissingJavadocComments = null;
	String reportMissingJavadocCommentsVisibility = null;

	public JavadocTest_1_3(String name) {
		super(name);
	}

	public static Class testClass() {
		return JavadocTest_1_3.class;
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] {
//			"testBug70892conform1", "testBug70892conform2"
//		};
//		TESTS_NUMBERS = new int[] { 101283 };
//		TESTS_RANGE = new int[] { 21, 50 };
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_3);
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
		if (this.reportMissingJavadocTags != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportMissingJavadocTags);
		else
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportInvalidJavadoc);
		options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.ERROR);
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
		this.reportMissingJavadocComments = CompilerOptions.IGNORE;
	}


	/**
	 * Test fix for bug 70891: [1.5][javadoc] Compiler should accept new 1.5 syntax for @param
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=70891">70891</a>
	 * These two tests fail for 1.5 source level but should pass for 1.3 or 1.4
	 * @see JavadocTest_1_3
	 */
	/* (non-Javadoc)
	 * Test @param for generic class type parameter
	 */
	public void test003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" +
					"  * Valid type parameter reference\n" +
					"  * @param <E> Type\n" +
					"  */\n" +
					" public class X<E> {}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 3)\r\n" +
				"	* @param <E> Type\r\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\r\n" +
				"	public class X<E> {}\r\n" +
				"	               ^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n"
		);
	}
	public void test004() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" +
					"  * Valid type parameter reference\n" +
					"  * @param <E> Type extends RuntimeException\n" +
					"  */\n" +
					" public class X<E extends RuntimeException> {}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 3)\r\n" +
				"	* @param <E> Type extends RuntimeException\r\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\r\n" +
				"	public class X<E extends RuntimeException> {}\r\n" +
				"	               ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n"

		);
	}
	public void test005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" +
					"  * Valid type parameter reference\n" +
					"  * @param <T> Type parameter 1\n" +
					"  * @param <U> Type parameter 2\n" +
					"  * @param <V> Type parameter 3\n" +
					"  */\n" +
					" public class X<T, U, V> {}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 3)\r\n" +
				"	* @param <T> Type parameter 1\r\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\r\n" +
				"	* @param <U> Type parameter 2\r\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\r\n" +
				"	* @param <V> Type parameter 3\r\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 7)\r\n" +
				"	public class X<T, U, V> {}\r\n" +
				"	               ^^^^^^^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n"
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
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n"
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
				"1. ERROR in X.java (at line 3)\n" +
				"	* @param <E> Type parameter\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	public class X<E, F> {}\n" +
				"	               ^^^^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n"
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
				"1. ERROR in X.java (at line 3)\n" +
				"	* @param <T> Type parameter 1\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	* @param <U> Type parameter 2\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	* @param <V> Type parameter 3\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 7)\n" +
				"	public class X<T> {}\n" +
				"	               ^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n"
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
				"1. ERROR in X.java (at line 3)\n" +
				"	* @param <T> Type parameter 1\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	* @param <X> Type parameter 2\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	* @param <U> Type parameter 2\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 6)\n" +
				"	* @param <E> Type parameter 2\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 7)\n" +
				"	* @param <V> Type parameter 3\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 9)\n" +
				"	public class X<T, U, V> {}\n" +
				"	               ^^^^^^^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n"
		);
	}
	public void test010() {
		this.runNegativeTest(
			new String[] {
				"X.java",
					" /**\n" +
					"  * Valid type parameter reference\n" +
					"  * @param <V> Type parameter 3\n" +
					"  * @param <U> Type parameter 2\n" +
					"  * @param <T> Type parameter 1\n" +
					"  */\n" +
					" public class X<T, U, V> {}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 3)\r\n" +
				"	* @param <V> Type parameter 3\r\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\r\n" +
				"	* @param <U> Type parameter 2\r\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\r\n" +
				"	* @param <T> Type parameter 1\r\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 7)\r\n" +
				"	public class X<T, U, V> {}\r\n" +
				"	               ^^^^^^^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n"
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
				"1. ERROR in X.java (at line 3)\n" +
				"	* @param <U> Type parameter 1\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	* @param <E> Type parameter 2\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	* @param <V> Type parameter 2\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 6)\n" +
				"	* @param <U> Type parameter 2\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 7)\n" +
				"	* @param <T> Type parameter 3\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 9)\n" +
				"	public class X<T, U, V> {}\n" +
				"	               ^^^^^^^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n"
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
				"	               ^^^^^^^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n"
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
				"1. ERROR in X.java (at line 3)\n" +
				"	* @param <T> Type parameter 3\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	public class X<T, U, V> {}\n" +
				"	               ^^^^^^^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n"
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
				"1. ERROR in X.java (at line 3)\n" +
				"	* @param <U> Type parameter 3\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	public class X<T, U, V> {}\n" +
				"	               ^^^^^^^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n"
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
				"1. ERROR in X.java (at line 3)\n" +
				"	* @param <U> Type parameter 3\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	* @param <V> Type parameter 3\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 6)\n" +
				"	public class X<T, U, V> {}\n" +
				"	               ^^^^^^^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n"
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
				"1. ERROR in X.java (at line 3)\n" +
				"	* @param <T> Type parameter 3\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	* @param <V> Type parameter 3\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 6)\n" +
				"	public class X<T, U, V> {}\n" +
				"	               ^^^^^^^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n"
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
				"1. ERROR in X.java (at line 3)\n" +
				"	* @param <T> Type parameter 3\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	* @param <U> Type parameter 3\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 6)\n" +
				"	public class X<T, U, V> {}\n" +
				"	               ^^^^^^^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n"
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
				"1. ERROR in X.java (at line 3)\n" +
				"	* @param <V> Type parameter 3\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	public class X<T, U, V> {}\n" +
				"	               ^^^^^^^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n"
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
				"1. ERROR in X.java (at line 3)\n" +
				"	* @param <V> Type parameter 2\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	* @param <X> Type parameter 2\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	* @param <U> Type parameter 1\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 6)\n" +
				"	* @param <E> Type parameter 2\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 7)\n" +
				"	* @param <U> Type parameter 2\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 9)\n" +
				"	public class X<T, U, V> {}\n" +
				"	               ^^^^^^^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n"
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
				"1. ERROR in X.java (at line 3)\n" +
				"	* @param <V> Type parameter 2\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	* @param\n" +
				"	   ^^^^^\n" +
				"Javadoc: Missing parameter name\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	* @param <U> Type parameter 1\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 7)\n" +
				"	public class X<T, U, V> {}\n" +
				"	               ^^^^^^^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n"
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
				"1. ERROR in X.java (at line 3)\n" +
				"	* @param <T> Type parameter 2\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	* @param <V> Type parameter 2\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	* @param <U> Type parameter 1\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 7)\n" +
				"	public class X<T, , V> {}\n" +
				"	              ^^\n" +
				"Syntax error on tokens, delete these tokens\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 7)\n" +
				"	public class X<T, , V> {}\n" +
				"	               ^\n" +
				"Syntax error, insert \"ClassBody\" to complete CompilationUnit\n" +
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
				"1. ERROR in X.java (at line 3)\n" +
				"	* @param <T> Type parameter 2\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	* @param <V> Type parameter 2\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	* @param <U> Type parameter 1\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 7)\n" +
				"	public class X<T, U, V extend Exception> {}\n" +
				"	              ^^^^^^\n" +
				"Syntax error on tokens, delete these tokens\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 7)\n" +
				"	public class X<T, U, V extend Exception> {}\n" +
				"	                   ^\n" +
				"Syntax error, insert \"ClassBody\" to complete CompilationUnit\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 7)\n" +
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
		this.runNegativeTest(
			new String[] {
				"X.java",
				" public class X {\n" +
					"	/**\n" +
					"	 * Valid type parameter reference\n" +
					"	 * @param <E> Type\n" +
					"	 */\n" +
					"	public <E> void foo() {}\n" +
					"}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 4)\r\n" +
				"	* @param <E> Type\r\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\r\n" +
				"	public <E> void foo() {}\r\n" +
				"	        ^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n"
		);
	}
	public void test024() {
		this.runNegativeTest(
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
			},
			"----------\n" +
				"1. ERROR in X.java (at line 4)\r\n" +
				"	* @param <E> Type extends RuntimeException\r\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 8)\r\n" +
				"	public <E extends RuntimeException> void foo(int val, Object obj) {}\r\n" +
				"	        ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n"
		);
	}
	public void test025() {
		this.runNegativeTest(
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
			},
			"----------\n" +
				"1. ERROR in X.java (at line 6)\r\n" +
				"	* @param <T> Type parameter 1\r\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 7)\r\n" +
				"	* @param <U> Type parameter 2\r\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 8)\r\n" +
				"	* @param <V> Type parameter 3\r\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 10)\r\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\r\n" +
				"	        ^^^^^^^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n"
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
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n"
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
				"1. ERROR in X.java (at line 4)\n" +
				"	* @param <E> Type parameter\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	public <E, F> void foo(int val, Object obj) {}\n" +
				"	        ^^^^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 6)\n" +
				"	public <E, F> void foo(int val, Object obj) {}\n" +
				"	                           ^^^\n" +
				"Javadoc: Missing tag for parameter val\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 6)\n" +
				"	public <E, F> void foo(int val, Object obj) {}\n" +
				"	                                       ^^^\n" +
				"Javadoc: Missing tag for parameter obj\n" +
				"----------\n"
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
				"1. ERROR in X.java (at line 4)\n" +
				"	* @param <T> Type parameter 1\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	* @param <U> Type parameter 2\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 6)\n" +
				"	* @param <V> Type parameter 3\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 7)\n" +
				"	* @param xxx int\n" +
				"	         ^^^\n" +
				"Javadoc: Parameter xxx is not declared\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 8)\n" +
				"	* @param Obj Object\n" +
				"	         ^^^\n" +
				"Javadoc: Parameter Obj is not declared\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 10)\n" +
				"	public <T> void foo(int val, Object obj) {}\n" +
				"	        ^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n" +
				"7. ERROR in X.java (at line 10)\n" +
				"	public <T> void foo(int val, Object obj) {}\n" +
				"	                        ^^^\n" +
				"Javadoc: Missing tag for parameter val\n" +
				"----------\n" +
				"8. ERROR in X.java (at line 10)\n" +
				"	public <T> void foo(int val, Object obj) {}\n" +
				"	                                    ^^^\n" +
				"Javadoc: Missing tag for parameter obj\n" +
				"----------\n"
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
				"1. ERROR in X.java (at line 4)\n" +
				"	* @param <T> Type parameter 1\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	* @param <X> Type parameter 2\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 7)\n" +
				"	* @param <U> Type parameter 2\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 8)\n" +
				"	* @param <E> Type parameter 2\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 10)\n" +
				"	* @param <V> Type parameter 3\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 12)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	        ^^^^^^^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n"
		);
	}
	public void test030() {
		this.runNegativeTest(
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
			},
			"----------\n" +
				"1. ERROR in X.java (at line 4)\r\n" +
				"	* @param <V> Type parameter 3\r\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\r\n" +
				"	* @param <U> Type parameter 2\r\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 8)\r\n" +
				"	* @param <T> Type parameter 1\r\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 10)\r\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\r\n" +
				"	        ^^^^^^^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n"
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
				"	        ^^^^^^^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	                              ^^^\n" +
				"Javadoc: Missing tag for parameter val\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	                                          ^^^\n" +
				"Javadoc: Missing tag for parameter obj\n" +
				"----------\n"
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
				"1. ERROR in X.java (at line 4)\n" +
				"	* @param <T> Type parameter 3\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 7)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	        ^^^^^^^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 7)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	                                          ^^^\n" +
				"Javadoc: Missing tag for parameter obj\n" +
				"----------\n"
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
				"1. ERROR in X.java (at line 5)\n" +
				"	* @param <U> Type parameter 3\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	* @param <V> Type parameter 3\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 8)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	        ^^^^^^^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 8)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	                              ^^^\n" +
				"Javadoc: Missing tag for parameter val\n" +
				"----------\n"
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
				"1. ERROR in X.java (at line 5)\n" +
				"	* @param <V> Type parameter 2\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 6)\n" +
				"	* @param <X> Type parameter 2\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 7)\n" +
				"	* @param <U> Type parameter 1\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 8)\n" +
				"	* @param Object obj\n" +
				"	         ^^^^^^\n" +
				"Javadoc: Parameter Object is not declared\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 9)\n" +
				"	* @param <E> Type parameter 2\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 10)\n" +
				"	* @param <U> Type parameter 2\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"7. ERROR in X.java (at line 11)\n" +
				"	* @param val int\n" +
				"	         ^^^\n" +
				"Javadoc: Duplicate tag for parameter\n" +
				"----------\n" +
				"8. ERROR in X.java (at line 13)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	        ^^^^^^^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n" +
				"9. ERROR in X.java (at line 13)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	                                          ^^^\n" +
				"Javadoc: Missing tag for parameter obj\n" +
				"----------\n"
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
				"1. ERROR in X.java (at line 4)\n" +
				"	* @param <V> Type parameter 2\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	* @param\n" +
				"	   ^^^^^\n" +
				"Javadoc: Missing parameter name\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 6)\n" +
				"	* @param <U> Type parameter 1\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 8)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	        ^^^^^^^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 8)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	                              ^^^\n" +
				"Javadoc: Missing tag for parameter val\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 8)\n" +
				"	public <T, U, V> void foo(int val, Object obj) {}\n" +
				"	                                          ^^^\n" +
				"Javadoc: Missing tag for parameter obj\n" +
				"----------\n"
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
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 5)\n" +
				"	* @param <V> Type parameter 2\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 6)\n" +
				"	* @param <U> Type parameter 1\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 10)\n" +
				"	public <T, , V> void foo(int val, Object obj) {}\n" +
				"	       ^^^^^^^^\n" +
				"Syntax error on tokens, delete these tokens\n" +
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
			"1. ERROR in X.java (at line 4)\n" +
			"	* @param <T> Type parameter 2\n" +
			"	         ^^^\n" +
			"Javadoc: Invalid param tag name\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	* @param <V> Type parameter 2\n" +
			"	         ^^^\n" +
			"Javadoc: Invalid param tag name\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	* @param <U> Type parameter 1\n" +
			"	         ^^^\n" +
			"Javadoc: Invalid param tag name\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 10)\n" +
			"	public <T, U, V extends Exceptions> void foo(int val, Object obj) {}\n" +
			"	        ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 10)\n" +
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
				"	         ^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	* @param < Type for parameterization\n" +
				"	         ^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	* @param <> Type\n" +
				"	         ^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 6)\n" +
				"	* @param <?> Type\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 7)\n" +
				"	* @param <*> Type\n" +
				"	         ^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 9)\n" +
				"	public class X<E> {}\n" +
				"	               ^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n"
		);
	}
	public void test039() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				" /**\n" +
					"  * Invalid type parameter reference\n" +
					"  * @param <E Type parameter of class X\n" +
					"  * @param E> Type\n" +
					"  * @param <<E> Type\n" +
					"  * @param <<<E> Type\n" +
					"  * @param <E>> Type\n" +
					"  */\n" +
					" public class X<E> {}"
			},
			"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	* @param <E Type parameter of class X\n" +
				"	         ^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	* @param E> Type\n" +
				"	         ^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 5)\n" +
				"	* @param <<E> Type\n" +
				"	         ^^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 6)\n" +
				"	* @param <<<E> Type\n" +
				"	         ^^^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 7)\n" +
				"	* @param <E>> Type\n" +
				"	         ^^^^\n" +
				"Javadoc: Invalid param tag name\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 9)\n" +
				"	public class X<E> {}\n" +
				"	               ^\n" +
				"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
				"----------\n"
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
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	public class X extends G<Exception> {\n" +
			"	                         ^^^^^^^^^\n" +
			"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 8)\n" +
			"	class G<E extends Exception> {\n" +
			"	        ^^^^^^^^^^^^^^^^^^^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n"
		);
	}

	/**
	 * Test fix for bug 82514: [1.5][javadoc] Problem with generics in javadoc
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=82514"
	 */
	public void testBug82514() {
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
			"1. ERROR in X.java (at line 2)\n" +
			"	public static <T extends Comparable< ? super T>> int compareTo(final Object first, final Object firstPrime,  final Class<T> type) throws ClassCastException\n" +
			"	               ^^^^^^^^^^^^^^^^^^^^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 2)\n" +
			"	public static <T extends Comparable< ? super T>> int compareTo(final Object first, final Object firstPrime,  final Class<T> type) throws ClassCastException\n" +
			"	                                                                                                                         ^\n" +
			"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	public static <X extends Comparable< ? super X>> int compareTo(final X first, final X firstPrime)\n" +
			"	               ^^^^^^^^^^^^^^^^^^^^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 6)\n" +
			"	public static <X extends Comparable< ? super X>> int compareTo(final X first, final X firstPrime)\n" +
			"	               ^\n" +
			"The type parameter X is hiding the type X\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 14)\n" +
			"	*  {@link ComparableUtils#compareTo(Object, Object)}.\n" +
			"	                          ^^^^^^^^^\n" +
			"Javadoc: The method compareTo(Object, Object, Class<T>) in the type ComparableUtils is not applicable for the arguments (Object, Object)\n" +
		    "----------\n");
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
			"----------\n" +
			"5. ERROR in Test.java (at line 11)\n" +
			"	public class Test<T> {\n" +
			"	                  ^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"6. ERROR in Test.java (at line 18)\n" +
			"	class Sub<E extends Number> extends Test<E> {\n" +
			"	          ^^^^^^^^^^^^^^^^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"7. ERROR in Test.java (at line 18)\n" +
			"	class Sub<E extends Number> extends Test<E> {\n" +
			"	                                         ^\n" +
			"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
			"----------\n");
	}
	public void testBug83127b() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"Test.java",
				"/** \n" +
				" * @see Sub#add(T)\n" +
				" * @see Sub#Sub(T)\n" +
				" *   - warning = \"The method add(Object) in the type Test is not applicable for\n" +
				" *                the arguments (T)\"\n" +
				" *   - method binding = Test.add(Object)\n" +
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
			"Javadoc: The method add(Object) in the type Test is not applicable for the arguments (T)\n" +
			"----------\n" +
			"2. ERROR in Test.java (at line 3)\n" +
			"	* @see Sub#Sub(T)\n" +
			"	           ^^^^^^\n" +
			"Javadoc: The constructor Sub(T) is undefined\n" +
			"----------\n" +
			"3. ERROR in Test.java (at line 11)\n" +
			"	public class Test<T>{\n" +
			"	                  ^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"4. ERROR in Test.java (at line 18)\n" +
			"	class Sub<E extends Number> extends Test<E> {\n" +
			"	          ^^^^^^^^^^^^^^^^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"5. ERROR in Test.java (at line 18)\n" +
			"	class Sub<E extends Number> extends Test<E> {\n" +
			"	                                         ^\n" +
			"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
			"----------\n");
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
			"----------\n" +
			"3. ERROR in Test.java (at line 8)\n" +
			"	public class Test<T>{\n" +
			"	                  ^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"4. ERROR in Test.java (at line 15)\n" +
			"	class Sub<E extends Number> extends Test<E> {\n" +
			"	          ^^^^^^^^^^^^^^^^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"5. ERROR in Test.java (at line 15)\n" +
			"	class Sub<E extends Number> extends Test<E> {\n" +
			"	                                         ^\n" +
			"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
			"----------\n");
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
			"1. ERROR in Unrelated1.java (at line 1)\n" +
			"	public class Unrelated1<E extends Number> {\n" +
			"	                        ^^^^^^^^^^^^^^^^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
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
			"----------\n" +
			"3. ERROR in Test.java (at line 8)\n" +
			"	public class Test<T>{\n" +
			"	                  ^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"4. ERROR in Test.java (at line 15)\n" +
			"	class Sub<E extends Number> extends Test<E> {\n" +
			"	          ^^^^^^^^^^^^^^^^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"5. ERROR in Test.java (at line 15)\n" +
			"	class Sub<E extends Number> extends Test<E> {\n" +
			"	                                         ^\n" +
			"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
			"----------\n");
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
			"1. ERROR in Unrelated1.java (at line 1)\n" +
			"	public class Unrelated1<E extends Number> {\n" +
			"	                        ^^^^^^^^^^^^^^^^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
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
			"----------\n" +
			"3. ERROR in Test.java (at line 9)\n" +
			"	public class Test<T>{\n" +
			"	                  ^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"4. ERROR in Test.java (at line 15)\n" +
			"	class Sub<E extends Number> extends Test<E> {\n" +
			"	          ^^^^^^^^^^^^^^^^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"5. ERROR in Test.java (at line 15)\n" +
			"	class Sub<E extends Number> extends Test<E> {\n" +
			"	                                         ^\n" +
			"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
			"----------\n");
	}
	public void testBug83127f() {
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
			},
			"----------\n" +
			"1. ERROR in Unrelated1.java (at line 1)\n" +
			"	public class Unrelated1<E extends Number> {\n" +
			"	                        ^^^^^^^^^^^^^^^^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in Test.java (at line 8)\n" +
			"	public class Test<T>{\n" +
			"	                  ^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"2. ERROR in Test.java (at line 14)\n" +
			"	class Sub<E extends Number> extends Test<E> {\n" +
			"	          ^^^^^^^^^^^^^^^^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"3. ERROR in Test.java (at line 14)\n" +
			"	class Sub<E extends Number> extends Test<E> {\n" +
			"	                                         ^\n" +
			"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
			"----------\n"
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
				"    public boolean add(E e) {\n" +
				"        if (e.doubleValue() > 0)\n" +
				"            return false;\n" +
				"        return super.add(e);\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in Unrelated1.java (at line 1)\n" +
			"	public class Unrelated1<E extends Number> {\n" +
			"	                        ^^^^^^^^^^^^^^^^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
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
			"----------\n" +
			"3. ERROR in Test.java (at line 9)\n" +
			"	public class Test<T>{\n" +
			"	                  ^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"4. ERROR in Test.java (at line 16)\n" +
			"	class Sub<E extends Number> extends Test<E> {\n" +
			"	          ^^^^^^^^^^^^^^^^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"5. ERROR in Test.java (at line 16)\n" +
			"	class Sub<E extends Number> extends Test<E> {\n" +
			"	                                         ^\n" +
			"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
			"----------\n"
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
			"1. ERROR in Unrelated2.java (at line 1)\n" +
			"	public interface Unrelated2<E> {\n" +
			"	                            ^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in Test.java (at line 2)\n" +
			"	* @see Unrelated2#add(T)\n" +
			"	                  ^^^\n" +
			"Javadoc: The method add(Object) in the type Unrelated2 is not applicable for the arguments (T)\n" +
			"----------\n" +
			"2. ERROR in Test.java (at line 10)\n" +
			"	public class Test<T>{\n" +
			"	                  ^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"3. ERROR in Test.java (at line 17)\n" +
			"	class Sub<E extends Number> extends Test<E> {\n" +
			"	          ^^^^^^^^^^^^^^^^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"4. ERROR in Test.java (at line 17)\n" +
			"	class Sub<E extends Number> extends Test<E> {\n" +
			"	                                         ^\n" +
			"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
			"----------\n");
	}

	/**
	 * Bug 83393: [1.5][javadoc] reference to vararg method also considers non-array type as correct
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=83393"
	 */
	public void testBug83393a() {
		runNegativeTest(
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
			},
			"----------\n" +
			"1. ERROR in Test.java (at line 3)\r\n" +
			"	public void foo(int a, int... args) {}\r\n" +
			"	                       ^^^^^^^^^^^\n" +
			"Syntax error, varargs are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"2. ERROR in Test.java (at line 4)\r\n" +
			"	public void foo(String... args) {}\r\n" +
			"	                ^^^^^^^^^^^^^^\n" +
			"Syntax error, varargs are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"3. ERROR in Test.java (at line 5)\r\n" +
			"	public void foo(Exception str, boolean... args) {}\r\n" +
			"	                               ^^^^^^^^^^^^^^^\n" +
			"Syntax error, varargs are only available if source level is 1.5 or greater\n" +
			"----------\n"
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
			"1. ERROR in Test.java (at line 3)\n" +
			"	public void foo(int a, int... args) {}\n" +
			"	                       ^^^^^^^^^^^\n" +
			"Syntax error, varargs are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"2. ERROR in Test.java (at line 4)\n" +
			"	public void foo(String... args) {}\n" +
			"	                ^^^^^^^^^^^^^^\n" +
			"Syntax error, varargs are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"3. ERROR in Test.java (at line 5)\n" +
			"	public void foo(Exception str, boolean... args) {}\n" +
			"	                               ^^^^^^^^^^^^^^^\n" +
			"Syntax error, varargs are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in Invalid.java (at line 2)\n" +
			"	* @see Test#foo(int)\n" +
			"	            ^^^\n" +
			"Javadoc: The method foo(int, int) in the type Test is not applicable for the arguments (int)\n" +
			"----------\n" +
			"2. ERROR in Invalid.java (at line 3)\n" +
			"	* @see Test#foo(int, int, int)\n" +
			"	            ^^^\n" +
			"Javadoc: The method foo(int, int) in the type Test is not applicable for the arguments (int, int, int)\n" +
			"----------\n" +
			"3. ERROR in Invalid.java (at line 4)\n" +
			"	* @see Test#foo()\n" +
			"	            ^^^\n" +
			"Javadoc: The method foo(String[]) in the type Test is not applicable for the arguments ()\n" +
			"----------\n" +
			"4. ERROR in Invalid.java (at line 5)\n" +
			"	* @see Test#foo(String)\n" +
			"	            ^^^\n" +
			"Javadoc: The method foo(String[]) in the type Test is not applicable for the arguments (String)\n" +
			"----------\n" +
			"5. ERROR in Invalid.java (at line 6)\n" +
			"	* @see Test#foo(String, String)\n" +
			"	            ^^^\n" +
			"Javadoc: The method foo(int, int) in the type Test is not applicable for the arguments (String, String)\n" +
			"----------\n" +
			"6. ERROR in Invalid.java (at line 7)\n" +
			"	* @see Test#foo(Exception)\n" +
			"	            ^^^\n" +
			"Javadoc: The method foo(Exception, boolean[]) in the type Test is not applicable for the arguments (Exception)\n" +
			"----------\n" +
			"7. ERROR in Invalid.java (at line 8)\n" +
			"	* @see Test#foo(Exception, boolean)\n" +
			"	            ^^^\n" +
			"Javadoc: The method foo(Exception, boolean[]) in the type Test is not applicable for the arguments (Exception, boolean)\n" +
			"----------\n" +
			"8. ERROR in Invalid.java (at line 9)\n" +
			"	* @see Test#foo(Exception, boolean, boolean)\n" +
			"	            ^^^\n" +
			"Javadoc: The method foo(Exception, boolean[]) in the type Test is not applicable for the arguments (Exception, boolean, boolean)\n" +
			"----------\n"
		);
	}

	/**
	 * Bug 86769: [javadoc] Warn/Error for 'Missing javadoc comments' doesn't recognize private inner classes
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=86769"
	 */
	public void testBug86769() {

		/* Deleted a completely meaningless test that could only serve as a torture test for the parser.
		   The test is still run in 1.5+ modes where it makes sense to run it - The test here was a nuisance
		   failing every time there is some serious grammar change that alters the semi-random behavior in
		   Diagnose Parser.
		 */
		return;
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
				" * @see Test\n" +
				" * @see Unknown\n" +
				" * @see Test#foo()\n" +
				" * @see Test#unknown()\n" +
				" * @see Test#field\n" +
				" * @see Test#unknown\n" +
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
			""
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
			//comment6\Invalid.java:6: warning - Tag @see: reference not found: Inner
			"----------\n" +
			"1. ERROR in comment6\\Invalid.java (at line 4)\r\n" +
			"	* @see Inner\r\n" +
			"	       ^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n"
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
			//comment6a\def\Test.java:6: warning - Tag @see: reference not found: Inner
			//comment6a\test\Invalid.java:8: warning - Tag @link: reference not found: Inner
			//comment6a\test\Invalid2.java:8: warning - Tag @see: reference not found: Test.Inner => bug ID: 4464323
			"----------\n" +
			"1. ERROR in comment6a\\def\\Test.java (at line 4)\n" +
			"	* @see Inner\n" +
			"	       ^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n" +
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
			"----------\n"
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
			//comment6b\Test.java:6: warning - Tag @see: reference not found: Inner
			//comment6b\Invalid.java:6: warning - Tag @see: reference not found: Inner
			"----------\n" +
			"1. ERROR in comment6b\\Invalid.java (at line 4)\n" +
			"	* @see Inner\n" +
			"	       ^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in comment6b\\Test.java (at line 4)\n" +
			"	* @see Inner\n" +
			"	       ^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n"
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
			"----------\n"
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
			"----------\n"
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
			"----------\n"
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
			"----------\n"
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
			//comment6\Invalid.java:6: warning - Tag @see: reference not found: Inner
			//comment6\Invalid.java:11: warning - Tag @link: reference not found: Inner
			"----------\n" +
			"1. ERROR in comment6\\Invalid.java (at line 4)\n" +
			"	* @see Inner\n" +
			"	       ^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n" +
			"2. ERROR in comment6\\Invalid.java (at line 9)\n" +
			"	* See also {@link Inner} \n" +
			"	                  ^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n"
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
			//comment6a\def\Test.java:6: warning - Tag @see: reference not found: Inner
			//comment6a\test\Invalid.java:8: warning - Tag @link: reference not found: Inner
			//comment6a\test\Invalid2.java:8: warning - Tag @see: reference not found: Test.Inner => bug ID: 4464323
			"----------\n" +
			"1. ERROR in comment6a\\def\\Test.java (at line 4)\n" +
			"	* @see Inner\n" +
			"	       ^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n" +
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
			"----------\n"
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
			//comment6b\Test.java:6: warning - Tag @see: reference not found: Inner
			"----------\n" +
			"1. ERROR in comment6b\\Invalid.java (at line 4)\n" +
			"	* @see Inner\n" +
			"	       ^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in comment6b\\Test.java (at line 4)\n" +
			"	* @see Inner\n" +
			"	       ^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n"
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
		runNegativeTest(
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
			},
			"----------\n" +
			"1. ERROR in test\\X.java (at line 8)\n" +
			"	public <T> G<T> foo(Class<T> stuffClass) {\n" +
			"	        ^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"2. ERROR in test\\X.java (at line 8)\n" +
			"	public <T> G<T> foo(Class<T> stuffClass) {\n" +
			"	             ^\n" +
			"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"3. ERROR in test\\X.java (at line 8)\n" +
			"	public <T> G<T> foo(Class<T> stuffClass) {\n" +
			"	                          ^\n" +
			"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"4. ERROR in test\\X.java (at line 15)\n" +
			"	* @param <T>\n" +
			"	         ^^^\n" +
			"Javadoc: Invalid param tag name\n" +
			"----------\n" +
			"5. ERROR in test\\X.java (at line 19)\n" +
			"	public <T extends Object> G<T> foo(Class<T> stuffClass);\n" +
			"	        ^^^^^^^^^^^^^^^^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"6. ERROR in test\\X.java (at line 19)\n" +
			"	public <T extends Object> G<T> foo(Class<T> stuffClass);\n" +
			"	                            ^\n" +
			"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"7. ERROR in test\\X.java (at line 19)\n" +
			"	public <T extends Object> G<T> foo(Class<T> stuffClass);\n" +
			"	                                         ^\n" +
			"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"8. ERROR in test\\X.java (at line 22)\n" +
			"	* @param <T>\n" +
			"	         ^^^\n" +
			"Javadoc: Invalid param tag name\n" +
			"----------\n" +
			"9. ERROR in test\\X.java (at line 24)\n" +
			"	class G<T> {}\n" +
			"	        ^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n");
	}
	public void testBug95521b() {
		runNegativeTest(
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
			},
			"----------\n" +
			"1. ERROR in test\\X.java (at line 6)\n" +
			"	* @param <T>\n" +
			"	         ^^^\n" +
			"Javadoc: Invalid param tag name\n" +
			"----------\n" +
			"2. ERROR in test\\X.java (at line 9)\n" +
			"	public <T> X(Class<T> classT) {\n" +
			"	        ^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"3. ERROR in test\\X.java (at line 9)\n" +
			"	public <T> X(Class<T> classT) {\n" +
			"	                   ^\n" +
			"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"4. ERROR in test\\X.java (at line 12)\n" +
			"	* @param <T>\n" +
			"	         ^^^\n" +
			"Javadoc: Invalid param tag name\n" +
			"----------\n" +
			"5. ERROR in test\\X.java (at line 16)\n" +
			"	public <T> Class<T> foo(Class<T> classT) {\n" +
			"	        ^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"6. ERROR in test\\X.java (at line 16)\n" +
			"	public <T> Class<T> foo(Class<T> classT) {\n" +
			"	                 ^\n" +
			"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"7. ERROR in test\\X.java (at line 16)\n" +
			"	public <T> Class<T> foo(Class<T> classT) {\n" +
			"	                              ^\n" +
			"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"8. ERROR in test\\X.java (at line 25)\n" +
			"	public <T> Y(Class<T> classT) {\n" +
			"	        ^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"9. ERROR in test\\X.java (at line 25)\n" +
			"	public <T> Y(Class<T> classT) {\n" +
			"	                   ^\n" +
			"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"10. ERROR in test\\X.java (at line 25)\n" +
			"	public <T> Y(Class<T> classT) {\n" +
			"	                      ^^^^^^\n" +
			"Javadoc: Missing tag for parameter classT\n" +
			"----------\n" +
			"11. ERROR in test\\X.java (at line 32)\n" +
			"	public <T extends Object> Class<T> foo(Class<T> stuffClass) {\n" +
			"	        ^^^^^^^^^^^^^^^^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"12. ERROR in test\\X.java (at line 32)\n" +
			"	public <T extends Object> Class<T> foo(Class<T> stuffClass) {\n" +
			"	                                ^\n" +
			"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"13. ERROR in test\\X.java (at line 32)\n" +
			"	public <T extends Object> Class<T> foo(Class<T> stuffClass) {\n" +
			"	                                             ^\n" +
			"Syntax error, parameterized types are only available if source level is 1.5 or greater\n" +
			"----------\n");
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
			"1. ERROR in X.java (at line 1)\n" +
			"	public class X<T, F> {\n" +
			"	               ^^^^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	* @param <T>  \n" +
			"	         ^^^\n" +
			"Javadoc: Invalid param tag name\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	* @param <F>\n" +
			"	         ^^^\n" +
			"Javadoc: Invalid param tag name\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	static class Entry<L, R> {\n" +
			"	                   ^^^^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n"
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
			"1. ERROR in X.java (at line 1)\n" +
			"	public class X<T, F> {\n" +
			"	               ^^^^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	* @see T Variable \n" +
			"	       ^\n" +
			"Javadoc: Invalid reference\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	* @see F Variable\n" +
			"	       ^\n" +
			"Javadoc: Invalid reference\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	static class Entry<L, R> {\n" +
			"	                   ^^^^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n"
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
			"1. ERROR in X.java (at line 1)\n" +
			"	public class X<T, F> {\n" +
			"	               ^^^^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	* @param <T>  \n" +
			"	         ^^^\n" +
			"Javadoc: Invalid param tag name\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	* @param <F>\n" +
			"	         ^^^\n" +
			"Javadoc: Invalid param tag name\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	class Entry<L, R> {\n" +
			"	            ^^^^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n"
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
			"1. ERROR in X.java (at line 1)\n" +
			"	public class X<T, F> {\n" +
			"	               ^^^^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	* @see T Variable \n" +
			"	       ^\n" +
			"Javadoc: Invalid reference\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 5)\n" +
			"	* @see F Variable\n" +
			"	       ^\n" +
			"Javadoc: Invalid reference\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	class Entry<L, R> {\n" +
			"	            ^^^^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n"
		);
	}
	public void testBug101283e() {
		runNegativeTest(
			new String[] {
				"X.java",
				"/**\n" +
				" * @see #foo()\n" +
				" */\n" +
				"public interface X<T> {\n" +
				"\n" +
				"	public T foo();\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	public interface X<T> {\n" +
			"	                   ^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n"
		);
	}
	// Verify that ProblemReasons.InheritedNameHidesEnclosingName is not reported as Javadoc error
	public void testBug101283g() {
		this.reportMissingJavadocTags = CompilerOptions.DISABLED;
		runNegativeTest(
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
			},
			"----------\n" +
			"1. ERROR in test\\X.java (at line 6)\r\n" +
			"	return foo();\r\n" +
			"	       ^^^\n" +
			"The method foo is defined in an inherited type and an enclosing scope\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in test\\Y.java (at line 9)\r\n" +
			"	return foo;\r\n" +
			"	       ^^^\n" +
			"The field foo is defined in an inherited type and an enclosing scope \n" +
			"----------\n"
		);
	}

	/**
	 * Bug 112346: [javadoc] {&#064;inheritedDoc} should be inactive for non-overridden method
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=112346"
	 */
	public void testBug112346() {
		runNegativeTest(
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
			},
			"----------\n" +
			"1. ERROR in Test.java (at line 6)\n" +
			"	public class Test<T> {\n" +
			"	                  ^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n");
	}

	/**
	 * Bug 119857: [javadoc] Some inner class references should be flagged as unresolved
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=119857"
	 */
	public void testBug119857() {
		runNegativeTest(
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
			},
			"----------\n" +
			"1. ERROR in DefaultInformationControl.java (at line 14)\n" +
			"	* @deprecated As of 3.2, replaced by {@link IInformationPresenterExtension#updatePresentation(String, int, int)}\n" +
			"	                                            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n" +
			"2. ERROR in DefaultInformationControl.java (at line 34)\n" +
			"	* Replaces {@link IInformationPresenter#updatePresentation(String, int, int)}\n" +
			"	                  ^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n"
		);
	}
	public void testBug119857_Fields() {
		runNegativeTest(
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
			},
			"----------\n" +
			"1. ERROR in TestFields.java (at line 6)\n" +
			"	* @see MyInnerClass#foo\n" +
			"	       ^^^^^^^^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n"
		);
	}
	public void testBug119857_Methods() {
		runNegativeTest(
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
			},
			"----------\n" +
			"1. ERROR in TestMethods.java (at line 7)\n" +
			"	* @see MyInnerInterface#foo()\n" +
			"	       ^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n" +
			"2. ERROR in TestMethods.java (at line 13)\n" +
			"	* @see MyInnerClass#foo()\n" +
			"	       ^^^^^^^^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n"
		);
	}
	public void testBug119857_Types() {
		runNegativeTest(
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
			},
			"----------\n" +
			"1. ERROR in TestTypes.java (at line 7)\n" +
			"	* @see MyInnerInterface\n" +
			"	       ^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n" +
			"2. ERROR in TestTypes.java (at line 13)\n" +
			"	* @see MyInnerClass\n" +
			"	       ^^^^^^^^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n"
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
			"----------\n"
		);
	}
	public void testBug119857_Private02() {
		this.reportMissingJavadocTags = CompilerOptions.IGNORE;
		runNegativeTest(
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
			},
			//pack\Test2.java:11: warning - Tag @see: reference not found: Inner1#foo()
			"----------\n" +
			"1. ERROR in test\\Test.java (at line 9)\r\n" +
			"	* @see Inner1#foo()\r\n" +
			"	       ^^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n"
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
			"----------\n"
		);
	}

	/**
	 * Bug 145007: [1.5][javadoc] Generics + Inner Class -> Javadoc "missing @throws" warning
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=145007"
	 */
	public void testBug145007() {
		runNegativeTest(
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
				"\n" +
				"    static class MyException extends Exception {\n" +
				"        private static final long serialVersionUID = 1L;\n" +
				"    }\n" +
				"}"
			},
			"----------\n" +
			"1. ERROR in TestClass.java (at line 1)\r\n" +
			"	class TestClass<T> {\r\n" +
			"	                ^\n" +
			"Syntax error, type parameters are only available if source level is 1.5 or greater\n" +
			"----------\n"
		);
	}
}
