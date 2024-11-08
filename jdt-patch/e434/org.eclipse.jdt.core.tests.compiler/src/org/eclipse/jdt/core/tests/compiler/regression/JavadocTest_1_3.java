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
		return buildMinimalComplianceTestSuite(testClass(), FIRST_SUPPORTED_JAVA_VERSION);
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
			"1. ERROR in X.java (at line 4)\n" +
			"	* @param\n" +
			"	   ^^^^^\n" +
			"Javadoc: Missing parameter name\n" +
			"""
			----------
			2. ERROR in X.java (at line 7)
				public class X<T, U, V> {}
				               ^
			Javadoc: Missing tag for parameter T
			----------
			"""
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
			"""
			----------
			1. ERROR in X.java (at line 4)
				* @param <V> Type parameter 2
				          ^
			Javadoc: V cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 5)
				* @param <U> Type parameter 1
				          ^
			Javadoc: U cannot be resolved to a type
			----------
			3. ERROR in X.java (at line 7)
				public class X<T, , V> {}
				                  ^
			Syntax error on token ",", delete this token
			----------
			"""
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
			"""
			----------
			1. ERROR in X.java (at line 7)
				public class X<T, U, V extend Exception> {}
				                       ^^^^^^
			Syntax error on token "extend", extends expected
			----------
			2. ERROR in X.java (at line 7)
				public class X<T, U, V extend Exception> {}
				                       ^^^^^^
			extend cannot be resolved to a type
			----------
			"""
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
	 * Bug 83804: [1.5][javadoc] Missing Javadoc node for package declaration
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=83804"
	 */
	public void _2551_testBug83804() {
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
			""
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
			"1. ERROR in comment6\\Invalid.java (at line 9)\n" +
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
			""
		);
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
			""
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
			""
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
			""
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
			""
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
			""
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

}
