/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contribution for bug 185682 - Increment/decrement operators mark local variables as read
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.JavadocTagConstants;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class JavadocBugsTest extends JavadocTest {

	String docCommentSupport = CompilerOptions.ENABLED;
	String reportInvalidJavadoc = CompilerOptions.ERROR;
	String reportMissingJavadocDescription = CompilerOptions.RETURN_TAG;
	String reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
	String reportMissingJavadocTags = CompilerOptions.ERROR;
	String reportMissingJavadocComments = null;
	String reportMissingJavadocCommentsVisibility = null;
	String reportDeprecation = CompilerOptions.ERROR;
	String reportJavadocDeprecation = null;
	String processAnnotations = null;

public JavadocBugsTest(String name) {
	super(name);
}

public static Class javadocTestClass() {
	return JavadocBugsTest.class;
}

// Use this static initializer to specify subset for tests
// All specified tests which does not belong to the class are skipped...
static {
//		TESTS_PREFIX = "testBug96237";
//		TESTS_NAMES = new String[] { "testBug382606" };
//		TESTS_NUMBERS = new int[] { 129241 };
//		TESTS_RANGE = new int[] { 21, 50 };
}

public static Test suite() {
	return buildAllCompliancesTestSuite(javadocTestClass());
}

@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_DocCommentSupport, this.docCommentSupport);
	options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, this.reportInvalidJavadoc);
	if (!CompilerOptions.IGNORE.equals(this.reportInvalidJavadoc)) {
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, this.reportInvalidJavadocVisibility);
	}
	if (this.reportJavadocDeprecation != null) {
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsDeprecatedRef, this.reportJavadocDeprecation);
	}
	if (this.reportMissingJavadocComments != null) {
		options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportMissingJavadocComments);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocCommentsOverriding, CompilerOptions.ENABLED);
		if (this.reportMissingJavadocCommentsVisibility != null) {
			options.put(CompilerOptions.OPTION_ReportMissingJavadocCommentsVisibility, this.reportMissingJavadocCommentsVisibility);
		}
	} else {
		options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportInvalidJavadoc);
	}
	if (this.reportMissingJavadocTags != null) {
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportMissingJavadocTags);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsOverriding, CompilerOptions.ENABLED);
	} else {
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportInvalidJavadoc);
	}
	if (this.reportMissingJavadocDescription != null) {
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagDescription, this.reportMissingJavadocDescription);
	}
	if (this.processAnnotations != null) {
		options.put(CompilerOptions.OPTION_Process_Annotations, this.processAnnotations);
	}
	options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportDeprecation, this.reportDeprecation);
	options.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
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
	this.reportMissingJavadocTags = CompilerOptions.IGNORE;
	this.reportMissingJavadocComments = CompilerOptions.IGNORE;
	this.reportMissingJavadocCommentsVisibility = CompilerOptions.PUBLIC;
	this.reportDeprecation = CompilerOptions.ERROR;
}

/**
 * Bug 45596.
 * When this bug happened, compiler wrongly complained on missing parameter javadoc
 * entries for method declaration in anonymous class.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45596">45596</a>
 */
public void testBug45596() {
	runConformTest(
		new String[] {
			"test/X.java",
			"package test;\n"
		 	+ "class X {\n"
				+ "	void foo(int x, String str) {}\n"
		  		+ "}\n",
			"test/Y.java",
			"package test;\n"
		   		+ "class Y {\n"
		   		+ "  /** */\n"
		   		+ "  protected X field = new X() {\n"
		   		+ "    void foo(int x, String str) {}\n"
		   		+ "  };\n"
		   		+ "}\n"});
}

/**
 * Additional test for bug 45596.
 * Verify correct complain about missing parameter javadoc entries in anonymous class.
 * Since bug 47132, @param, @return and @throws tags are not resolved in javadoc of anonymous
 * class...
 */
public void testBug45596a() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo(int x, String str) {}\n" +
			"}\n",
			"Y1.java",
			"public class Y1 {\n" +
			"	/** */\n" +
			"	protected X field = new X() {\n" +
			"		/** Invalid javadoc comment in anonymous class */\n" +
			"		void foo(String str) {}\n" +
			"	};\n" +
			"}\n",
			"Y2.java",
			"public class Y2 {\n" +
			"	/** */\n" +
			"	void foo() {\n" +
			"		X x = new X() {\n" +
			"			/** Invalid javadoc comment in anonymous class */\n" +
			"			void foo(String str) {}\n" +
			"		};\n" +
			"		x.foo(0, \"\");\n" +
			"	}\n" +
			"}\n",
			"Y3.java",
			"public class Y3 {\n" +
			"	static X x;\n" +
			"	static {\n" +
			"		x = new X() {\n" +
			"			/** Invalid javadoc comment in anonymous class */\n" +
			"			void foo(String str) {}\n" +
			"		};\n" +
			"	}\n" +
			"}\n" }
		);
}

/**
 * Additional test for bug 45596.
 * Verify no complain about missing parameter javadoc entries.
 */
public void testBug45596b() {
	runConformTest(
		new String[] {
	"X.java",
	"public class X {\n" +
	"	void foo(int x, String str) {}\n" +
	"}\n",
	"Y1.java",
	"public class Y1 {\n" +
	"	/** */\n" +
	"	protected X field = new X() {\n" +
	"		/**\n" +
	"		 * Valid javadoc comment in anonymous class.\n" +
	"		 * @param str String\n" +
	"		 * @return int\n" +
	"		 */\n" +
	"		int bar(String str) {\n" +
	"			return 10;\n" +
	"		}\n" +
	"	};\n" +
	"}\n",
	"Y2.java",
	"public class Y2 {\n" +
	"	/** */\n" +
	"	void foo() {\n" +
	"		X x = new X() {\n" +
	"			/**\n" +
	"			 * Valid javadoc comment in anonymous class.\n" +
	"			 * @param str String\n" +
	"			 * @return int\n" +
	"			 */\n" +
	"			int bar(String str) {\n" +
	"				return 10;\n" +
	"			}\n" +
	"		};\n" +
	"		x.foo(0, \"\");\n" +
	"	}\n" +
	"}\n",
	"Y3.java",
	"public class Y3 {\n" +
	"	static X x;\n" +
	"	static {\n" +
	"		x = new X() {\n" +
	"			/**\n" +
	"			 * Valid javadoc comment in anonymous class.\n" +
	"			 * @param str String\n" +
	"			 * @return int\n" +
	"			 */\n" +
	"			int bar(String str) {\n" +
	"				return 10;\n" +
	"			}\n" +
	"		};\n" +
	"	}\n" +
	"}\n"}
		);
}

/**
 * Bug 45592.
 * When this bug happened, a NullPointerException occured during the compilation.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45592">45592</a>
 */
public void testBug45592() {
	runConformTest(
		new String[] {
			"a/Y.java",
			"package a;\n" +
			"\n" +
			"/** */\n" +
			"public class Y {\n" +
			"	protected boolean bar(Object obj) {\n" +
			"		return obj == null;\n" +
			"	}\n" +
			"}\n",
			"test/X.java",
			"package test;\n" +
			"public class X {\n" +
			"	public static Boolean valueOf(boolean bool) {\n" +
			"		if (bool) {\n" +
			"			return Boolean.TRUE;\n" +
			"		} else {\n" +
			"			return Boolean.FALSE;\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
			"test/YY.java",
			"package test;\n" +
			"\n" +
			"import a.Y;\n" +
			"\n" +
			"/** */\n" +
			"public class YY extends Y {\n" +
			"	/**\n" +
			"	 * Returns a Boolean.\n" +
			"	 * @param key\n" +
			"	 * @return A Boolean telling whether the key is null or not.\n" +
			"	 * @see #bar(Object)\n" +
			"	 */\n" +
			"	protected Boolean foo(Object key) {\n" +
			"		return X.valueOf(bar(key));\n" +
			"	}\n" +
			"}\n"
		}
	);
}

/**
 * Bug 45737.
 * When this bug happened, compiler complains on return type and argument of method bar.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45737">45737</a>
 */
public void testBug45737() {
	runConformTest(
		new String[] {
			"Y.java",
			"class Y {\n" +
			"	void foo() {\n" +
			"		X x = new X() {\n" +
			"			/**\n" +
			"			 * Valid javadoc comment in anonymous class.\n" +
			"			 * @param str String\n" +
			"			 * @return int\n" +
			"			 */\n" +
			"			int bar(String str) {\n" +
			"				return 10;\n" +
			"			}\n" +
			"		};\n" +
			"		x.foo();\n" +
			"	}\n" +
			"}\n",
			"X.java",
			"class X {\n" +
			"	void foo() {}\n" +
			"}\n"
		}
	);
}

/**
 * Bug 45669.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45669">45669</a>
 */
public void testBug45669() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	/**\n" +
			"	 * Valid javadoc comment with tags mixed order\n" +
			"	 * @param str first param\n" +
			"	 * 		@see String\n" +
			"	 * @param dbl second param\n" +
			"	 * 		@see Double\n" +
			"	 * 		also\n" +
			"	 * 		@see \"String ref\"\n" +
			"	 * @return int\n" +
			"	 * @throws InterruptedException\n" +
			"	 * \n" +
			"	 */\n" +
			"	int foo(String str, Double dbl) throws InterruptedException {\n" +
			"		return 0;\n" +
			"	}\n" +
			"}\n"
		}
	);
}
/*
 * Additional test for bug 45669.
 * Verify that compiler complains when @throws tag is between @param tags.
 */
public void testBug45669a() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	/**\n" +
			"	 * Javadoc comment with tags invalid mixed order\n" +
			"	 * @param str first param\n" +
			"	 * 		@see String\n" +
			"	 * @throws InterruptedException\n" +
			"	 * @param dbl second param\n" +
			"	 * 		@see Double\n" +
			"	 * 		also\n" +
			"	 * 		@see \"String ref\"\n" +
			"	 * @return int\n" +
			"	 * \n" +
			"	 */\n" +
			"	public int foo(String str, Double dbl) throws InterruptedException {\n" +
			"		return 0;\n" +
			"	}\n" +
			"}\n"
		},
	"----------\n" +
	"1. ERROR in X.java (at line 7)\n" +
	"	* @param dbl second param\n" +
	"	   ^^^^^\n" +
	"Javadoc: Unexpected tag\n" +
	"----------\n" +
	"2. ERROR in X.java (at line 14)\n" +
	"	public int foo(String str, Double dbl) throws InterruptedException {\n" +
	"	                                  ^^^\n" +
	"Javadoc: Missing tag for parameter dbl\n" +
	"----------\n",
	JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 45958.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45958">45958</a>
 */
public void testBug45958() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	int x;\n" +
			"	public X(int i) {\n" +
			"		x = i;\n" +
			"	}\n" +
			"	/**\n" +
			"	 * @see #X(int)\n" +
			"	 */\n" +
			"	void foo() {\n" +
			"	}\n" +
			"}\n"
		}
	);
}
public void testBug45958a() {
	runNegativeTest(
		new String[] {
		   "X.java",
	   		"public class X {\n" +
	   		"	int x;\n" +
	   		"	public X(int i) {\n" +
	   		"		x = i;\n" +
	   		"	}\n" +
	   		"	/**\n" +
	   		"	 * @see #X(String)\n" +
	   		"	 */\n" +
	   		"	public void foo() {\n" +
	   		"	}\n" +
	   		"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	* @see #X(String)\n" +
		"	        ^^^^^^^^^\n" +
		"Javadoc: The constructor X(String) is undefined\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug45958b() {
	runNegativeTest(
		new String[] {
		   "X.java",
	   		"public class X {\n" +
	   		"	int x;\n" +
	   		"	public X(int i) {\n" +
	   		"		x = i;\n" +
	   		"	}\n" +
	   		"	/**\n" +
	   		"	 * @see #X(int)\n" +
	   		"	 */\n" +
	   		"	public void foo() {\n" +
	   		"	}\n" +
	   		"}\n",
	   		"XX.java",
	   		"public class XX extends X {\n" +
	   		"	/**\n" +
	   		"	 * @param i\n" +
	   		"	 * @see #X(int)\n" +
	   		"	 */\n" +
	   		"	public XX(int i) {\n" +
	   		"		super(i);\n" +
	   		"		x++;\n" +
	   		"	}\n" +
	   		"}\n"
		},
		"----------\n" +
		"1. ERROR in XX.java (at line 4)\n" +
		"	* @see #X(int)\n" +
		"	        ^\n" +
		"Javadoc: The method X(int) is undefined for the type XX\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug45958c() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	int x;\n" +
			"	public X(int i) {\n" +
			"		x = i;\n" +
			"	}\n" +
			"	/**\n" +
			"	 * @see #X(String)\n" +
			"	 */\n" +
			"	void foo() {\n" +
			"	}\n" +
			"	void X(String str) {}\n" +
			"}\n"
		}
	);
}

/**
 * Bug 46901.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=46901">46901</a>
 */
public void testBug46901() {
	runConformTest(
		new String[] {
			"A.java",
			"public abstract class A {\n" +
			"	public A() { super(); }\n" +
			"}\n",
			"X.java",
			"/**\n" +
			" * @see A#A()\n" +
			" */\n" +
			"public class X extends A {\n" +
			"	public X() { super(); }\n" +
			"}\n"
		}
	);
}

/**
 * Bug 47215.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=47215">47215</a>
 */
public void testBug47215() {
	runNegativeTest(
		new String[] {
			"X.java",
			"	/**\n" +
			"	 * @see X\n" +
			"	 * @see X#X(int)\n" +
			"	 * @see X(double)\n" +
			"	 * @see X   (double)\n" +
			"	 * @see X[double]\n" +
			"	 * @see X!=}}\n" +
			"	 * @see foo()\n" +
			"	 * @see foo  ()\n" +
			"	 */\n" +
			"	public class X {\n" +
			"		public X(int i){}\n" +
			"		public void foo() {}\n" +
			"	}\n"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	* @see X(double)\n" +
			"	       ^^^^^^^^^\n" +
			"Javadoc: Missing #: \"X(double)\"\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	* @see X[double]\n" +
			"	       ^^^^^^^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	* @see X!=}}\n" +
			"	       ^^^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 8)\n" +
			"	* @see foo()\n" +
			"	       ^^^^^\n" +
			"Javadoc: Missing #: \"foo()\"\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 9)\n" +
			"	* @see foo  ()\n" +
			"	       ^^^\n" +
			"Javadoc: foo cannot be resolved to a type\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 47341.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=47341">47341</a>
 */
public void testBug47341() {
	runConformTest(
		new String[] {
			"p1/X.java",
			"package p1;\n" +
			"public class X {\n" +
			"	void foo_package() {}\n" +
			"	protected void foo_protected() {}\n" +
			"}\n",
			"p1/Y.java",
			"package p1;\n" +
			"public class Y extends X {\n" +
			"	/**\n" +
			"	 * @see #foo_package()\n" +
			"	 */\n" +
			"	protected void bar() {\n" +
			"		foo_package();\n" +
			"	}\n" +
			"}\n",
			"p2/Y.java",
			"package p2;\n" +
			"import p1.X;\n" +
			"\n" +
			"public class Y extends X {\n" +
			"	/**\n" +
			"	 * @see X#foo_protected()\n" +
			"	 */\n" +
			"	protected void bar() {\n" +
			"		foo_protected();\n" +
			"	}\n" +
			"}\n"
		}
	);
}

/**
 * Bug 47132.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=47132">47132</a>
 */
public void testBug47132() {
	this.reportMissingJavadocComments = CompilerOptions.ERROR;
	runConformTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"  /** */\n" +
			"  public void foo(){\n" +
			"    new Object(){\n" +
			"		public int x;\n" +
			"       public void bar(){}\n" +
			"    };\n" +
			"  }\n" +
			"}\n"
		}
	);
}

/**
 * Bug 47339.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=47339">47339</a>
 */
public void testBug47339() {
	runConformTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X implements Comparable {\n" +
			"	/**\n" +
			"	 * @see java.lang.Comparable#compareTo(java.lang.Object)\n" +
			"	 */\n" +
			"	public int compareTo(Object o) {\n" +
			"		return 0;\n" +
			"	}\n" +
			"	/** @see Object#toString() */\n" +
			"	public String toString(){\n" +
			"		return \"\";\n" +
			"	}\n" +
			"}\n"
		}
	);
}
public void testBug47339a() {
	runConformTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X extends RuntimeException {\n" +
			"	\n" +
			"	/**\n" +
			"	 * @see RuntimeException#RuntimeException(java.lang.String)\n" +
			"	 */\n" +
			"	public X(String message) {\n" +
			"		super(message);\n" +
			"	}\n" +
			"}\n"
		}
	);
}
public void testBug47339b() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X implements Comparable {\n" +
			"	/** */\n" +
			"	public int compareTo(Object o) {\n" +
			"		return 0;\n" +
			"	}\n" +
			"	/** */\n" +
			"	public String toString(){\n" +
			"		return \"\";\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	public int compareTo(Object o) {\n" +
			"	       ^^^\n" +
			"Javadoc: Missing tag for return type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	public int compareTo(Object o) {\n" +
			"	                            ^\n" +
			"Javadoc: Missing tag for parameter o\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 8)\n" +
			"	public String toString(){\n" +
			"	       ^^^^^^\n" +
			"Javadoc: Missing tag for return type\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug47339c() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X extends RuntimeException {\n" +
			"	\n" +
			"	/** */\n" +
			"	public X(String message) {\n" +
			"		super(message);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	public X(String message) {\n" +
		"	                ^^^^^^^\n" +
		"Javadoc: Missing tag for parameter message\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 48064.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=48064">48064</a>
 */
public void testBug48064() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public X(String str) {}\n" +
			"}\n",
			"Y.java",
			"public class Y extends X {\n" +
			"	/**\n" +
			"	 * @see X#X(STRING)\n" +
			"	 */\n" +
			"	public Y(String str) {super(str);}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Y.java (at line 3)\n" +
		"	* @see X#X(STRING)\n" +
		"	           ^^^^^^\n" +
		"Javadoc: STRING cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in Y.java (at line 5)\n" +
		"	public Y(String str) {super(str);}\n" +
		"	                ^^^\n" +
		"Javadoc: Missing tag for parameter str\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug48064a() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void foo(String str) {}\n" +
			"}\n",
			"Y.java",
			"public class Y extends X {\n" +
			"	/**\n" +
			"	 * @see X#foo(STRING)\n" +
			"	 */\n" +
			"	public void foo(String str) {super.foo(str);}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Y.java (at line 3)\n" +
		"	* @see X#foo(STRING)\n" +
		"	             ^^^^^^\n" +
		"Javadoc: STRING cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in Y.java (at line 5)\n" +
		"	public void foo(String str) {super.foo(str);}\n" +
		"	                       ^^^\n" +
		"Javadoc: Missing tag for parameter str\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 48523.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=48523">48523</a>
 */
public void testBug48523() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
				"public class X {\n" +
				"	public void foo() throws IOException {}\n" +
				"}\n",
			"Y.java",
			"import java.io.IOException;\n" +
				"public class Y extends X {\n" +
				"	/**\n" +
				"	 * @throws IOException\n" +
				"	 * @see X#foo()\n" +
				"	 */\n" +
				"	public void foo() throws IOException {}\n" +
				"}\n"
		}
	);
}

/**
 * Bug 48711.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=48711">48711</a>
 */
public void testBug48711() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"\n" +
			"public class X {\n" +
			"	/**\n" +
			"	 * @throws IOException\n" +
			"	 * @throws EOFException\n" +
			"	 * @throws FileNotFoundException\n" +
			"	 */\n" +
			"	public void foo() throws IOException {}\n" +
			"}\n"
		}
	);
}

/**
 * Bug 45782.
 * When this bug happened, compiler wrongly complained on missing parameters declaration
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45782">45782</a>
 */
public void testBug45782() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X implements Comparable {\n" +
				"\n" +
				"	/**\n" +
				"	 * Overridden method with return value and parameters.\n" +
				"	 * {@inheritDoc}\n" +
				"	 */\n" +
				"	public boolean equals(Object obj) {\n" +
				"		return super.equals(obj);\n" +
				"	}\n" +
				"\n" +
				"	/**\n" +
				"	 * Overridden method with return value and thrown exception.\n" +
				"	 * {@inheritDoc}\n" +
				"	 */\n" +
				"	public Object clone() throws CloneNotSupportedException {\n" +
				"		return super.clone();\n" +
				"	}\n" +
				"\n" +
				"	/**\n" +
				"	 * Implemented method (Comparable)  with return value and parameters.\n" +
				"	 * {@inheritDoc}\n" +
				"	 */\n" +
				"	public int compareTo(Object o) { return 0; }\n" +
				"}\n"
		});
}
public void testBug45782a() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**\n" +
				"	 * Unefficient inheritDoc tag on a method which is neither overridden nor implemented...\n" +
				"	 * {@inheritDoc}\n" +
				"	 */\n" +
				"	public int foo(String str) throws IllegalArgumentException { return 0; }\n" +
				"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	* {@inheritDoc}\n" +
		"	    ^^^^^^^^^^\n" +
		"Javadoc: Unexpected tag\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	public int foo(String str) throws IllegalArgumentException { return 0; }\n" +
		"	       ^^^\n" +
		"Javadoc: Missing tag for return type\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 6)\n" +
		"	public int foo(String str) throws IllegalArgumentException { return 0; }\n" +
		"	                      ^^^\n" +
		"Javadoc: Missing tag for parameter str\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 6)\n" +
		"	public int foo(String str) throws IllegalArgumentException { return 0; }\n" +
		"	                                  ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: Missing tag for declared exception IllegalArgumentException\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 49260.
 * When this bug happened, compiler wrongly complained on Invalid parameters declaration
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=49260">49260</a>
 */
public void testBug49260() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.Vector;\n" +
				"public final class X {\n" +
				"	int bar(String str, int var, Vector list, char[] array) throws IllegalAccessException { return 0; }\n" +
				"	/**\n" +
				"	 * Valid method reference on several lines\n" +
				"	 * @see #bar(String str,\n" +
				"	 * 		int var,\n" +
				"	 * 		Vector list,\n" +
				"	 * 		char[] array)\n" +
				"	 */\n" +
				"	void foo() {}\n" +
				"}\n" });
}

/**
 * Bug 48385.
 * When this bug happened, compiler does not complain on CharOperation references in @link tags
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=48385">48385</a>
 */
public void testBug48385() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.Vector;\n" +
				"public class X {\n" +
				"	/**\n" +
				"	 * Method outside javaDoc Comment\n" +
				"	 *  1) {@link String} tag description not empty\n" +
				"	 *  2) {@link CharOperation Label not empty} tag description not empty\n" +
				"	 * @param str\n" +
				"	 * @param var tag description not empty\n" +
				"	 * @param list third param with embedded tag: {@link Vector}\n" +
				"	 * @param array fourth param with several embedded tags on several lines:\n" +
				"	 *  1) {@link String} tag description not empty\n" +
				"	 *  2) {@linkplain CharOperation Label not empty} tag description not empty\n" +
				"	 * @throws IllegalAccessException\n" +
				"	 * @throws NullPointerException tag description not empty\n" +
				"	 * @return an integer\n" +
				"	 * @see String\n" +
				"	 * @see Vector tag description not empty\n" +
				"	 * @see Object tag description includes embedded tags and several lines:\n" +
				"	 *  1) {@link String} tag description not empty\n" +
				"	 *  2) {@link CharOperation Label not empty} tag description not empty\n" +
				"	 */\n" +
				"	int foo(String str, int var, Vector list, char[] array) throws IllegalAccessException { return 0; }\n" +
				"}\n"},
		"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	*  2) {@link CharOperation Label not empty} tag description not empty\n" +
			"	             ^^^^^^^^^^^^^\n" +
			"Javadoc: CharOperation cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 12)\n" +
			"	*  2) {@linkplain CharOperation Label not empty} tag description not empty\n" +
			"	                  ^^^^^^^^^^^^^\n" +
			"Javadoc: CharOperation cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 20)\n" +
			"	*  2) {@link CharOperation Label not empty} tag description not empty\n" +
			"	             ^^^^^^^^^^^^^\n" +
			"Javadoc: CharOperation cannot be resolved to a type\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

public void testBug48385And49620() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.Vector;\n" +
				"public class X {\n" +
				"	/**\n" +
				"	 * Method outside javaDoc Comment\n" +
				"	 *  1) {@link\n" +
				"	 * 				String} tag description not empty\n" +
				"	 *  2) {@link\n" +
				"	 * 				CharOperation Label not empty} tag description not empty\n" +
				"	 * @param\n" +
				"	 * 				str\n" +
				"	 * @param\n" +
				"	 * 				var tag description not empty\n" +
				"	 * @param list third param with embedded tag: {@link\n" +
				"	 * 				Vector} but also on several lines: {@link\n" +
				"	 * 				CharOperation}\n" +
				"	 * @param array fourth param with several embedded tags on several lines:\n" +
				"	 *  1) {@link String} tag description not empty\n" +
				"	 *  2) {@link CharOperation Label not empty} tag description not empty\n" +
				"	 * @throws\n" +
				"	 * 					IllegalAccessException\n" +
				"	 * @throws\n" +
				"	 * 					NullPointerException tag description not empty\n" +
				"	 * @return\n" +
				"	 * 					an integer\n" +
				"	 * @see\n" +
				"	 * 			String\n" +
				"	 * @see\n" +
				"	 * 		Vector\n" +
				"	 * 		tag description not empty\n" +
				"	 * @see Object tag description includes embedded tags and several lines:\n" +
				"	 *  1) {@link String} tag description not empty\n" +
				"	 *  2) {@link CharOperation Label not empty} tag description not empty\n" +
				"	 */\n" +
				"	int foo(String str, int var, Vector list, char[] array) throws IllegalAccessException { return 0; }\n" +
				"}\n"},
		"----------\n" +
			"1. ERROR in X.java (at line 8)\n" +
			"	* 				CharOperation Label not empty} tag description not empty\n" +
			"	  				^^^^^^^^^^^^^\n" +
			"Javadoc: CharOperation cannot be resolved to a type\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 15)\n" +
			"	* 				CharOperation}\n" +
			"	  				^^^^^^^^^^^^^\n" +
			"Javadoc: CharOperation cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 18)\n" +
			"	*  2) {@link CharOperation Label not empty} tag description not empty\n" +
			"	             ^^^^^^^^^^^^^\n" +
			"Javadoc: CharOperation cannot be resolved to a type\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 32)\n" +
			"	*  2) {@link CharOperation Label not empty} tag description not empty\n" +
			"	             ^^^^^^^^^^^^^\n" +
			"Javadoc: CharOperation cannot be resolved to a type\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug48385a() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**\n" +
				"	 * Method outside javaDoc Comment\n" +
				"	 *  1) {@link } Missing reference\n" +
				"	 *  2) {@link Unknown} Cannot be resolved\n" +
				"	 *  3) {@link *} Missing reference\n" +
				"	 *  4) {@link #} Invalid reference\n" +
				"	 *  5) {@link String } } Valid reference\n" +
				"	 *  6) {@link String {} Invalid tag\n" +
				"	 * @return int\n" +
				"	 */\n" +
				"	int foo() {return 0;}\n" +
				"}\n"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	*  1) {@link } Missing reference\n" +
			"	        ^^^^\n" +
			"Javadoc: Missing reference\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	*  2) {@link Unknown} Cannot be resolved\n" +
			"	             ^^^^^^^\n" +
			"Javadoc: Unknown cannot be resolved to a type\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	*  3) {@link *} Missing reference\n" +
			"	        ^^^^\n" +
			"Javadoc: Missing reference\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	*  4) {@link #} Invalid reference\n" +
			"	             ^\n" +
			"Javadoc: Invalid reference\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 9)\n" +
			"	*  6) {@link String {} Invalid tag\n" +
			"	      ^^^^^^^^^^^^^^^\n" +
			"Javadoc: Missing closing brace for inline tag\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 49491.
 * When this bug happened, compiler complained on duplicated throws tag
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=49491">49491</a>
 */
public void testBug49491() {
	runConformTest(
		new String[] {
			"X.java",
			"public final class X {\n" +
				"	/**\n" +
				"	 * Now valid duplicated throws tag\n" +
				"	 * @throws IllegalArgumentException First comment\n" +
				"	 * @throws IllegalArgumentException Second comment\n" +
				"	 * @throws IllegalArgumentException Last comment\n" +
				"	 */\n" +
				"	void foo() throws IllegalArgumentException {}\n" +
				"}\n" });
}
public void testBug49491a() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public final class X {\n" +
				"	/**\n" +
				"	 * Duplicated param tags should be still flagged\n" +
				"	 * @param str First comment\n" +
				"	 * @param str Second comment\n" +
				"	 * @param str Last comment\n" +
				"	 */\n" +
				"	void foo(String str) {}\n" +
				"}\n"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	* @param str Second comment\n" +
			"	         ^^^\n" +
			"Javadoc: Duplicate tag for parameter\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	* @param str Last comment\n" +
			"	         ^^^\n" +
			"Javadoc: Duplicate tag for parameter\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 48376.
 * When this bug happened, compiler complained on duplicated throws tag
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=48376">48376</a>
 */
public void testBug48376() {
	runConformTest(
		new String[] {
			"X.java",
			"/**\n" +
				"	* @see <a href=\"http:/www.ibm.com\">IBM Home Page</a>\n" +
				"	* @see <a href=\"http:/www.ibm.com\">\n" +
				"	*          IBM Home Page</a>\n" +
				"	* @see <a href=\"http:/www.ibm.com\">\n" +
				"	*          IBM Home Page\n" +
				"	* 			</a>\n" +
				"	* @see <a href=\"http:/www.ibm.com\">\n" +
				"	*\n" +
				"	*          IBM\n" +
				"	*\n" +
				"	*          Home Page\n" +
				"	*\n" +
				"	*\n" +
				"	* 			</a>\n" +
				"	* @see Object\n" +
				"	*/\n" +
				"public class X {\n" +
				"}\n"
	 });
}
public void testBug48376a() {
	runNegativeTest(
		new String[] {
			"X.java",
			"/**\n" +
				"	* @see <a href=\"http:/www.ibm.com\">IBM Home Page\n" +
				"	* @see <a href=\"http:/www.ibm.com\">\n" +
				"	*          IBM Home Page\n" +
				"	* @see <a href=\"http:/www.ibm.com\">\n" +
				"	*          IBM Home Page<\n" +
				"	* 			/a>\n" +
				"	* @see <a href=\"http:/www.ibm.com\">\n" +
				"	*\n" +
				"	*          IBM\n" +
				"	*\n" +
				"	*          Home Page\n" +
				"	*\n" +
				"	*\n" +
				"	* 			\n" +
				"	* @see Unknown\n" +
				"	*/\n" +
				"public class X {\n" +
				"}\n"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	* @see <a href=\"http:/www.ibm.com\">IBM Home Page\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	* @see <a href=\"http:/www.ibm.com\">\n" +
			"	*          IBM Home Page\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	*          IBM Home Page<\n" +
			"	                        ^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 8)\n" +
			"	* @see <a href=\"http:/www.ibm.com\">\n" +
			"	*\n" +
			"	*          IBM\n" +
			"	*\n" +
			"	*          Home Page\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 16)\n" +
			"	* @see Unknown\n" +
			"	       ^^^^^^^\n" +
			"Javadoc: Unknown cannot be resolved to a type\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 50644.
 * When this bug happened, compiler complained on duplicated throws tag
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=50644">50644</a>
 */
public void testBug50644() {
	this.reportInvalidJavadoc = CompilerOptions.IGNORE;
	runConformTest(
		new String[] {
			"p1/X.java",
			"package p1;\n" +
				"public class X {\n" +
				"	/**\n" +
				"	 * Should not be @deprecated\n" +
				"	 */\n" +
				"	public void foo() {}\n" +
				"}\n",
			"p2/Y.java",
			"package p2;\n" +
				"import p1.X;\n" +
				"public class Y {\n" +
				"	public void foo() {\n" +
				"		X x = new X();\n" +
				"		x.foo();\n" +
				"	}\n" +
				"}\n"
	 });
}

/**
 * Bug 50695.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=50695">50695</a>
 */
public void testBug50695() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	/**\n" +
			"	 * @see java\n" +
			"	 * @see java.util\n" +
			"	 */\n" +
			"	void foo() {}\n" +
			"}\n"
		 });
}
public void testBug50695b() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**\n" +
				"	 * @see java.unknown\n" +
				"	 */\n" +
				"	void foo() {}\n" +
				"}\n"
		 },
		"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	* @see java.unknown\n" +
			"	       ^^^^^^^^^^^^\n" +
			"Javadoc: java.unknown cannot be resolved to a type\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 51626.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=51626">51626</a>
 */
public void testBug51626() {
	runConformTest(
		new String[] {
			"p1/X.java",
			"package p1;\n" +
				"public class X {\n" +
				"	/**\n" +
				"	 * @see String\n" +
				"	 * toto @deprecated\n" +
				"	 */\n" +
				"	public void foo() {}\n" +
				"}\n",
			"p2/Y.java",
			"package p2;\n" +
				"import p1.*;\n" +
				"public class Y {\n" +
				"	void foo() {\n" +
				"		X x = new X(); \n" +
				"		x.foo();\n" +
				"	}\n" +
				"}\n"
	 });
}

/**
 * Bug 52216.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=52216">52216</a>
 */
public void testBug52216() {
	runConformTest(
		new String[] {
			"X.java",
			"/**\n" +
				" * Valid ref with white spaces at the end\n" +
				"* @see <a href=\"http://www.ietf.org/rfc/rfc2045.txt\">RFC 2045 - Section 6.8</a>		   \n" +
				"*/\n" +
				"public class X {\n" +
				"}\n"
	 });
}
public void testBug52216a() {
	runConformTest(
		new String[] {
			"X.java",
			"/**\n" +
				"* @see \"Valid ref with white spaces at the end\"	   \n" +
				"*/\n" +
				"public class X {\n" +
				"}\n"
	 });
}
public void testBug52216b() {
	runNegativeTest(
		new String[] {
			"X.java",
			"/**\n" +
				"* @see <a href=\"http://www.ietf.org/rfc/rfc2045.txt\">RFC 2045 - Section 6.8</a>		   \n" +
				"* @see <a href=\"http://www.ietf.org/rfc/rfc2045.txt\">RFC 2045 - Section 6.8</a>\n" +
				"* @see <a href=\"http://www.ietf.org/rfc/rfc2045.txt\">RFC 2045 - Section 6.8</a>			,\n" +
				"* @see \"Valid ref with white spaces at the end\"\n" +
				"* @see \"Valid ref with white spaces at the end\"	   \n" +
				"* @see \"Invalid ref\"	   .\n" +
				"*/\n" +
				"public class X {\n" +
				"}\n"
		 },
		"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	* @see <a href=\"http://www.ietf.org/rfc/rfc2045.txt\">RFC 2045 - Section 6.8</a>			,\n" +
			"	                                                                            ^^^^^^^\n" +
			"Javadoc: Unexpected text\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	* @see \"Invalid ref\"	   .\n" +
			"	                    ^^^^^\n" +
			"Javadoc: Unexpected text\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 51529.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=51529">51529</a>
 */
public void testBug51529() {
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.Vector;\n" +
			"public class X {\n" +
			"	/**\n" +
			"	 * @see Vector\n" +
			"	 */\n" +
			"	void foo() {}\n" +
			"}\n"
	 });
}
public void testBug51529a() {
	this.reportInvalidJavadoc = CompilerOptions.IGNORE;
	this.reportMissingJavadocComments = CompilerOptions.IGNORE;
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.Vector;\n" +
			"public class X {\n" +
			"	/**\n" +
			"	 * @see Vector\n" +
			"	 */\n" +
			"	void foo() {}\n" +
			"}\n"
		}
	);
}
public void testBug51529b() {
	this.docCommentSupport = CompilerOptions.DISABLED;
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.Vector;\n" +
			"public class X {\n" +
			"	/**\n" +
			"	 * @see Vector\n" +
			"	 */\n" +
			"	void foo() {}\n" +
			"}\n"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 1)\n" +
			"	import java.util.Vector;\n" +
			"	       ^^^^^^^^^^^^^^^^\n" +
			"The import java.util.Vector is never used\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * bug 51911: [Javadoc] @see method w/out ()
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=51911"
 */
// Conform since bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=191322 has been fixed
public void testBug51911() {
	// Warn an ambiguous method reference
	runConformTest(
		new String[] {
			"X.java",
			"/**\n" +
			" * @see #foo\n" +
			" */\n" +
			"public class X {\n" +
			"	public void foo(int i, float f) {}\n" +
			"	public void foo(String str) {}\n" +
			"}\n"
	 	}
	);
}
public void testBug51911a() {
	// Accept unambiguous method reference
	runConformTest(
		new String[] {
			"X.java",
			"/**\n" +
			" * @see #foo\n" +
			" */\n" +
			"public class X {\n" +
			"	public void foo(String str) {}\n" +
			"}\n"
	 	}
	);
}
public void testBug51911b() {
	// Accept field reference with method name
	runConformTest(
		new String[] {
			"X.java",
			"/**\n" +
			" * @see #foo\n" +
			" */\n" +
			"public class X {\n" +
			"	public int foo;\n" +
			"	public void foo(String str) {}\n" +
			"}\n"
	 	}
	);
}
public void testBug51911c() {
	// Accept field reference with ambiguous method name
	runConformTest(
		new String[] {
			"X.java",
			"/**\n" +
			" * @see #foo\n" +
			" */\n" +
			"public class X {\n" +
			"	public int foo;\n" +
			"	public void foo() {}\n" +
			"	public void foo(String str) {}\n" +
			"}\n"
	 	}
	);
}

/**
 * Bug 53279: [Javadoc] Compiler should complain when inline tag is not terminated
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=53279">53279</a>
 */
public void testBug53279() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**\n" +
				"	 * Unterminated inline tags\n" +
				"	 *  {@link Object\n" +
				"	 */\n" +
				"	void foo() {}\n" +
				"}\n"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	*  {@link Object\n" +
			"	   ^^^^^^^^^^^^^\n" +
			"Javadoc: Missing closing brace for inline tag\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug53279a() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**\n" +
				"	 * Unterminated inline tags\n" +
				"	 *  {@link Object\n" +
				"	 * @return int\n" +
				"	 */\n" +
				"	int foo() {return 0;}\n" +
				"}\n"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	*  {@link Object\n" +
			"	   ^^^^^^^^^^^^^\n" +
			"Javadoc: Missing closing brace for inline tag\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug53279b() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**\n" +
				"	 * Unterminated inline tags\n" +
				"	 *  {@link        \n" +
				"	 */\n" +
				"	void foo() {}\n" +
				"}\n"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	*  {@link        \n" +
			"	   ^^^^^^^^^^^^^^\n" +
			"Javadoc: Missing closing brace for inline tag\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	*  {@link        \n" +
			"	     ^^^^\n" +
			"Javadoc: Missing reference\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug53279c() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**\n" +
				"	 * Unterminated inline tags\n" +
				"	 *  {@link\n" +
				"	 * @return int\n" +
				"	 */\n" +
				"	int foo() {return 0;}\n" +
				"}\n"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	*  {@link\n" +
			"	   ^^^^^^\n" +
			"Javadoc: Missing closing brace for inline tag\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	*  {@link\n" +
			"	     ^^^^\n" +
			"Javadoc: Missing reference\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 53290: [Javadoc] Compiler should complain when tag name is not correct
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=53290">53290</a>
 */
public void testBug53290() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**\n" +
				"	 * See as inline tag\n" +
				"	 *  {@see Object}\n" +
				"	 *  @see Object\n" +
				"	 *  @link Object\n" +
				"	 *  {@link Object}\n" +
				"	 */\n" +
				"	void foo() {}\n" +
				"}\n"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	*  {@see Object}\n" +
			"	     ^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	*  @link Object\n" +
			"	    ^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 62812: Some malformed javadoc tags are not reported as malformed
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=62812">62812</a>
 */
public void testBug62812() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"/**\n" +
				" * @see Object#clone())\n" +
				" * @see Object#equals(Object)}\n" +
				" * @see Object#equals(Object))\n" +
				" * @see Object#equals(Object)xx\n" +
				" */\n" +
				"public class Test {\n" +
				"}\n"
		},
		"----------\n" +
			"1. ERROR in Test.java (at line 2)\n" +
			"	* @see Object#clone())\n" +
			"	                   ^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n" +
			"2. ERROR in Test.java (at line 3)\n" +
			"	* @see Object#equals(Object)}\n" +
			"	                    ^^^^^^^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n" +
			"3. ERROR in Test.java (at line 4)\n" +
			"	* @see Object#equals(Object))\n" +
			"	                    ^^^^^^^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n" +
			"4. ERROR in Test.java (at line 5)\n" +
			"	* @see Object#equals(Object)xx\n" +
			"	                    ^^^^^^^^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug62812a() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"/**\n" +
				" * {@link Object#clone())}\n" +
				" * {@link Object#equals(Object)}\n" +
				" * {@link Object#equals(Object))}\n" +
				" * {@link Object#equals(Object)xx}\n" +
				" */\n" +
				"public class Test {\n" +
				"}\n"
		},
		"----------\n" +
			"1. ERROR in Test.java (at line 2)\n" +
			"	* {@link Object#clone())}\n" +
			"	                     ^^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n" +
			"2. ERROR in Test.java (at line 4)\n" +
			"	* {@link Object#equals(Object))}\n" +
			"	                      ^^^^^^^^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n" +
			"3. ERROR in Test.java (at line 5)\n" +
			"	* {@link Object#equals(Object)xx}\n" +
			"	                      ^^^^^^^^^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 51606: [Javadoc] Compiler should complain when tag name is not correct
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=51606">51606</a>
 */
// Cleaned up this test as part of fix for https://bugs.eclipse.org/bugs/show_bug.cgi?id=247037
// We should not complain about the missing @param tag for Y.foo at all, since the comments are
// automatically inherited.
public void testBug51606() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"  /**\n" +
				"   * @param a aaa\n" +
				"   * @param b bbb\n" +
				"   */\n" +
				"  public void foo(int a, int b) {\n" +
				"  }\n" +
				"}\n",
			"Y.java",
			"public class Y extends X {\n" +
				"  /**\n" +
				"  *  @param a {@inheritDoc}\n" +
				"   */\n" +
				"  public void foo(int a, int b) {\n" +
				"  }\n" +
				"}\n"
		},
		""
	);
}
public void testBug51606a() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"  /**\n" +
				"   * @param a aaa\n" +
				"   * @param b bbb\n" +
				"   */\n" +
				"  public void foo(int a, int b) {\n" +
				"  }\n" +
				"}\n",
			"Y.java",
			"public class Y extends X {\n" +
				"  /**\n" +
				"   * {@inheritDoc}\n" +
				"  *  @param a aaaaa\n" +
				"   */\n" +
				"  public void foo(int a, int b) {\n" +
				"  }\n" +
				"}\n"
		},
		""
	);
}
public void testBug51606b() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"  /**\n" +
				"   * @param a aaa\n" +
				"   * @param b bbb\n" +
				"   */\n" +
				"  public void foo(int a, int b) {\n" +
				"  }\n" +
				"}\n",
			"Y.java",
			"public class Y extends X {\n" +
				"  /**\n" +
				"   * Text before inherit tag\n" +
				"   * {@inheritDoc}\n" +
				"  *  @param a aaaaa\n" +
				"   */\n" +
				"  public void foo(int a, int b) {\n" +
				"  }\n" +
				"}\n"
		}
	);
}
public void testBug51606c() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"  /**\n" +
				"   * @param a aaa\n" +
				"   * @param b bbb\n" +
				"   */\n" +
				"  public void foo(int a, int b) {\n" +
				"  }\n" +
				"}\n",
			"Y.java",
			"public class Y extends X {\n" +
				"  /**\n" +
				"   * Text before inherit tag {@inheritDoc}\n" +
				"  *  @param a aaaaa\n" +
				"   */\n" +
				"  public void foo(int a, int b) {\n" +
				"  }\n" +
				"}\n"
		}
	);
}

/**
 * Bug 65174: Spurious "Javadoc: Missing reference" error
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=65174">65174</a>
 */
public void testBug65174() {
	runConformTest(
		new String[] {
			"Test.java",
			"/**\n" +
				" * Comment with no error: {@link\n" +
				" * Object valid} because it\'s not on first line\n" +
				" */\n" +
				"public class Test {\n" +
				"	/** Comment previously with error: {@link\n" +
				"	 * Object valid} because tag is on comment very first line\n" +
				"	 */\n" +
				"	void foo() {}\n" +
				"}\n"
		}
	);
}
public void testBug65174a() {
	runConformTest(
		new String[] {
			"Test.java",
			"/**\n" +
				" * Comment with no error: {@link    		\n" +
				" * Object valid} because it\'s not on first line\n" +
				" */\n" +
				"public class Test {\n" +
				"	/** Comment previously with error: {@link   		\n" +
				"	 * Object valid} because tag is on comment very first line\n" +
				"	 */\n" +
				"	void foo() {}\n" +
				"}\n"
		}
	);
}
public void testBug65174b() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"/**\n" +
				" * Comment with no error: {@link java.lang.\n" +
				" * Object valid} because it\'s not on first line\n" +
				" */\n" +
				"public class Test {\n" +
				"	/** Comment previously with error: {@link java.lang.\n" +
				"	 * Object valid} because tag is on comment very first line\n" +
				"	 */\n" +
				"	void foo() {}\n" +
				"}\n"
		},
		"----------\n" +
		"1. ERROR in Test.java (at line 2)\r\n" +
		"	* Comment with no error: {@link java.lang.\r\n" +
		"	                               ^^^^^^^^^^^\n" +
		"Javadoc: Invalid reference\n" +
		"----------\n" +
		"2. ERROR in Test.java (at line 6)\r\n" +
		"	/** Comment previously with error: {@link java.lang.\r\n" +
		"	                                         ^^^^^^^^^^^\n" +
		"Javadoc: Invalid reference\n" +
		"----------\n"

	);
}
public void testBug65174c() {
	runConformTest(
		new String[] {
			"Test.java",
			"/**\n" +
				" * Comment with no error: {@link Object\n" +
				" * valid} because it\'s not on first line\n" +
				" */\n" +
				"public class Test {\n" +
				"	/** Comment previously with no error: {@link Object\n" +
				"	 * valid} because tag is on comment very first line\n" +
				"	 */\n" +
				"	void foo() {}\n" +
				"}\n"
		}
	);
}
public void testBug65174d() {
	runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
				"	/** Comment previously with no error: {@link Object valid} comment on one line */\n" +
				"	void foo1() {}\n" +
				"	/** Comment previously with no error: {@link Object valid}       */\n" +
				"	void foo2() {}\n" +
				"	/** Comment previously with no error: {@link Object valid}*/\n" +
				"	void foo3() {}\n" +
				"	/**                    {@link Object valid} comment on one line */\n" +
				"	void foo4() {}\n" +
				"	/**{@link Object valid} comment on one line */\n" +
				"	void foo5() {}\n" +
				"	/**       {@link Object valid} 				*/\n" +
				"	void foo6() {}\n" +
				"	/**{@link Object valid} 				*/\n" +
				"	void foo7() {}\n" +
				"	/**				{@link Object valid}*/\n" +
				"	void foo8() {}\n" +
				"	/**{@link Object valid}*/\n" +
				"	void foo9() {}\n" +
				"}\n"
		}
	);
}

/**
 * bug 65180: Spurious "Javadoc: xxx cannot be resolved or is not a field" error with inner classes
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=65180"
 */
 // Conform since bug "http://bugs.eclipse.org/bugs/show_bug.cgi?id=191322" has been fixed
public void testBug65180() {
	runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"	public class Inner {\n" +
			"		/**\n" +
			"		 * Does something.\n" +
			"		 * \n" +
			"		 * @see #testFunc\n" +
			"		 */\n" +
			"		public void innerFunc() {\n" +
			"			testFunc();\n" +
			"		}\n" +
			"	}\n" +
			"	\n" +
			"	public void testFunc() {}\n" +
			"}\n" +
			"\n"
		}
	);
}
public void testBug65180a() {
	runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"	public class Inner {\n" +
			"		/**\n" +
			"		 * Does something.\n" +
			"		 * \n" +
			"		 * @see #testFunc()\n" +
			"		 */\n" +
			"		public void innerFunc() {\n" +
			"			testFunc();\n" +
			"		}\n" +
			"	}\n" +
			"	\n" +
			"	public void testFunc() {}\n" +
			"}\n"
		}
	);
}
public void testBug65180b() {
	runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"	public class Inner {\n" +
			"		/**\n" +
			"		 * Does something.\n" +
			"		 * \n" +
			"		 * @see Test#testFunc\n" +
			"		 * @see Test#testFunc()\n" +
			"		 */\n" +
			"		public void innerFunc() {\n" +
			"			testFunc();\n" +
			"		}\n" +
			"	}\n" +
			"	\n" +
			"	public void testFunc() {}\n" +
			"}\n"
		}
	);
}
 // Conform since bug "http://bugs.eclipse.org/bugs/show_bug.cgi?id=191322" has been fixed
public void testBug65180c() {
	runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"	public class Inner {\n" +
			"		/**\n" +
			"		 * Does something.\n" +
			"		 * \n" +
			"		 * @see #testFunc\n" +
			"		 */\n" +
			"		public void innerFunc() {\n" +
			"			testFunc();\n" +
			"		}\n" +
			"	}\n" +
			"	\n" +
			"	public void testFunc() {}\n" +
			"	public void testFunc(String str) {}\n" +
			"}\n"
		}
	);
}
public void testBug65180d() {
	runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
				"	int testField;\n" +
			"	public class Inner {\n" +
			"		/**\n" +
			"		 * Does something.\n" +
			"		 * \n" +
			"		 * @see #testField\n" +
			"		 * @see #testFunc(int)\n" +
			"		 */\n" +
			"		public void innerFunc() {\n" +
			"			testFunc(testField);\n" +
			"		}\n" +
			"	}\n" +
			"	\n" +
			"	public void testFunc(int test) {\n" +
			"		testField = test; \n" +
			"	}\n" +
			"}\n"
		}
	);
}
public void testBug65180e() {
	runConformTest(
		new String[] {
			"ITest.java",
			"public interface ITest {\n" +
			"	/**\n" +
			"	 * @see #foo() \n" +
			"	 */\n" +
			"	public static int field = 0;\n" +
			"	/**\n" +
			"	 * @see #field\n" +
			"	 */\n" +
			"	public void foo();\n" +
			"}\n"
		}
	);
}
public void testBug65180f() {
	runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"    static class SuperInner {\n" +
			"    	public int field;\n" +
			"        public void foo() {}\n" +
			"     }\n" +
			"    \n" +
			"	public static class Inner extends SuperInner {\n" +
			"		/**\n" +
			"		 * @see #field\n" +
			"		 */\n" +
			"		public static int f;\n" +
			"		/**\n" +
			"		 * @see #foo()\n" +
			"		 */\n" +
			"		public static void bar() {}\n" +
			"	}\n" +
			"	\n" +
			"	public void foo() {}\n" +
			"}"
		}
	);
}

/**
 * bug 65253: [Javadoc] @@tag is wrongly parsed as @tag
 * test Verify that @@return is not interpreted as a return tag<br>
 * 	Note that since fix for bug 237742, the '@' in a tag name does no longer
 * 	flag it as invalid...
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=65253"
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=237742"
 */
public void testBug65253() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"Test.java",
			"/**\n" +
				" * Comment \n" +
				" * @@@@see Unknown Should not complain on ref\n" +
				" */\n" +
				"public class Test {\n" +
				"	/**\n" +
				"	 * Comment\n" +
				"	 * @@@param xxx Should not complain on param\n" +
				"	 * @@return int\n" +
				"	 */\n" +
				"	int foo() { // should warn on missing tag for return type\n" +
				"		return 0;\n" +
				"	}\n" +
				"}\n"
		},
		"----------\n" +
		"1. ERROR in Test.java (at line 11)\n" +
		"	int foo() { // should warn on missing tag for return type\n" +
		"	^^^\n" +
		"Javadoc: Missing tag for return type\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 66551: Error in org.eclipse.swt project on class PrinterData
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=66551">66551</a>
 */
public void testBug66551() {
	runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
				"    int field;\n" +
				"    /**\n" +
				"     *  @see #field\n" +
				"     */\n" +
				"    void foo(int field) {\n" +
				"    }\n" +
				"\n" +
				"}\n"
		}
	);
}
public void testBug66551a() {
	runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
				"    static int field;\n" +
				"    /**\n" +
				"     *  @see #field\n" +
				"     */\n" +
				"    static void foo(int field) {\n" +
				"    }\n" +
				"\n" +
				"}\n"
		}
	);
}
public void testBug66551b() {
	runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
				"	int field;\n" +
				"	/**\n" +
				"	 * {@link #field}\n" +
				"	 */\n" +
				"	void foo(int field) {\n" +
				"	}\n" +
				"\n" +
				"}\n"
		}
	);
}
public void testBug66551c() {
	runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
				"	static int field;\n" +
				"	/**\n" +
				"	 * {@link #field}\n" +
				"	 */\n" +
				"	static void foo(int field) {\n" +
				"	}\n" +
				"\n" +
				"}\n"
		}
	);
}

/**
 * Bug 66573: Shouldn't bind to local constructs
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=66573">66573</a>
 */
public void testBug66573() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
				"    /**\n" +
				"     * @see Local\n" +
				"     */\n" +
				"    void foo() {\n" +
				"        class Local { \n" +
				"            // shouldn\'t be seen from javadoc\n" +
				"         }\n" +
				"    }\n" +
				"}\n"
		},
		"----------\n" +
			"1. ERROR in Test.java (at line 3)\n" +
			"	* @see Local\n" +
			"	       ^^^^^\n" +
			"Javadoc: Local cannot be resolved to a type\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 68017: Javadoc processing does not detect missing argument to @return
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=68017">68017</a>
 */
public void testBug68017conform() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**@return valid integer*/\n" +
				"	public int foo1() {return 0; }\n" +
				"	/**\n" +
				"	 *	@return #\n" +
				"	 */\n" +
				"	public int foo2() {return 0; }\n" +
				"}\n",
		}
	);
}
public void testBug68017negative() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**@return*/\n" +
				"	public int foo1() {return 0; }\n" +
				"	/**@return        */\n" +
				"	public int foo2() {return 0; }\n" +
				"	/**@return****/\n" +
				"	public int foo3() {return 0; }\n" +
				"	/**\n" +
				"	 *	@return\n" +
				"	 */\n" +
				"	public int foo4() {return 0; }\n" +
				"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	/**@return*/\n" +
		"	    ^^^^^^\n" +
		"Javadoc: Description expected after @return\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	/**@return        */\n" +
		"	    ^^^^^^\n" +
		"Javadoc: Description expected after @return\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 6)\n" +
		"	/**@return****/\n" +
		"	    ^^^^^^\n" +
		"Javadoc: Description expected after @return\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 9)\n" +
		"	*	@return\n" +
		"	 	 ^^^^^^\n" +
		"Javadoc: Description expected after @return\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
// Javadoc issue a warning on following tests
public void testBug68017javadocWarning1() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**\n" +
				"	 *	@return* */\n" +
				"	public int foo1() {return 0; }\n" +
				"	/**@return** **/\n" +
				"	public int foo2() {return 0; }\n" +
				"}\n",
		},
		"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	*	@return* */\n" +
			"	 	 ^^^^^^\n" +
			"Javadoc: Description expected after @return\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	/**@return** **/\n" +
			"	    ^^^^^^\n" +
			"Javadoc: Description expected after @return\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug68017javadocWarning2() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	/**\n" +
			"	 *	@return #\n" +
			"	 */\n" +
			"	public int foo1() {return 0; }\n" +
			"	/**\n" +
			"	 *	@return @\n" +
			"	 */\n" +
			"	public int foo2() {return 0; }\n" +
			"}\n"
		}
	);
}
public void testBug68017javadocWarning3() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**\n" +
				"	 *	@return#\n" +
				"	 *	@return#text\n" +
				"	 */\n" +
				"	public int foo() {return 0; }\n" +
				"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	*	@return#\n" +
		"	 	 ^^^^^^^\n" +
		"Javadoc: Invalid tag\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	*	@return#text\n" +
		"	 	 ^^^^^^^^^^^\n" +
		"Javadoc: Invalid tag\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 68025: Javadoc processing does not detect some wrong links
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=68025">68025</a>
 */
public void testBug68025conform() {
	runConformTest(
		new String[] {
			"Y.java",
			"public class Y {\n" +
				"	public int field;\n" +
				"	public void foo() {}\n" +
				"}\n",
			"Z.java",
			"public class Z {\n" +
				"	/**\n" +
				"	 *	@see Y#field #valid\n" +
				"	 *	@see Y#foo #valid\n" +
				"	 */\n" +
				"	public void foo1() {}\n" +
				"	/**@see Y#field     # valid*/\n" +
				"	public void foo2() {}\n" +
				"	/**@see Y#foo		# valid*/\n" +
				"	public void foo3() {}\n" +
				"	/**@see Y#foo()\n" +
				"	 *# valid*/\n" +
				"	public void foo4() {}\n" +
				"}\n"
		}
	);
}
public void testBug68025negative() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	public int field;\n" +
				"	public void foo() {}\n" +
				"	/**\n" +
				"	 *	@see #field#invalid\n" +
				"	 *	@see #foo#invalid\n" +
				"	 */\n" +
				"	public void foo1() {}\n" +
				"	/**@see Y#field# invalid*/\n" +
				"	public void foo2() {}\n" +
				"	/**@see Y#foo#	invalid*/\n" +
				"	public void foo3() {}\n" +
				"	/**@see Y#foo()#\n" +
				"	 *valid*/\n" +
				"	public void foo4() {}\n" +
				"}\n"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	*	@see #field#invalid\n" +
			"	 	     ^^^^^^^^^^^^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	*	@see #foo#invalid\n" +
			"	 	     ^^^^^^^^^^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 9)\n" +
			"	/**@see Y#field# invalid*/\n" +
			"	         ^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 11)\n" +
			"	/**@see Y#foo#	invalid*/\n" +
			"	         ^^^^^^^^^^^^^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 13)\n" +
			"	/**@see Y#foo()#\n" +
			"	             ^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 68726: [Javadoc] Target attribute in @see link triggers warning
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=68726">68726</a>
 */
public void testBug68726conform1() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**\n" +
				"	 *	@see Object <a href=\"http://www.eclipse.org\" target=\"_top\">Eclipse</a>\n" +
				"	 */\n" +
				"	void foo1() {}\n" +
				"	/**@see Object <a href=\"http://www.eclipse.org\" target=\"_top\" target1=\"_top1\" target2=\"_top2\">Eclipse</a>*/\n" +
				"	void foo2() {}\n" +
				"}\n"
		}
	);
}
public void testBug68726conform2() {
	runConformTest(
		new String[] {
			"X.java",
			"/**\n" +
				"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">IBM Home Page</a>\n" +
				"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">\n" +
				"	*          IBM Home Page</a>\n" +
				"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">\n" +
				"	*          IBM Home Page\n" +
				"	* 			</a>\n" +
				"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">\n" +
				"	*\n" +
				"	*          IBM\n" +
				"	*\n" +
				"	*          Home Page\n" +
				"	*\n" +
				"	*\n" +
				"	* 			</a>\n" +
				"	* @see Object\n" +
				"	*/\n" +
				"public class X {\n" +
				"}\n"
		}
	);
}
public void testBug68726negative1() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**\n" +
				"	 * Invalid URL link references\n" +
				"	 *\n" +
				"	 * @see <a href=\"invalid\" target\n" +
				"	 * @see <a href=\"invalid\" target=\n" +
				"	 * @see <a href=\"invalid\" target=\"\n" +
				"	 * @see <a href=\"invalid\" target=\"_top\n" +
				"	 * @see <a href=\"invalid\" target=\"_top\"\n" +
				"	 * @see <a href=\"invalid\" target=\"_top\">\n" +
				"	 * @see <a href=\"invalid\" target=\"_top\">\n" +
				"	 * @see <a href=\"invalid\" target=\"_top\">invalid\n" +
				"	 * @see <a href=\"invalid\" target=\"_top\">invalid<\n" +
				"	 * @see <a href=\"invalid\" target=\"_top\">invalid</\n" +
				"	 * @see <a href=\"invalid\" target=\"_top\">invalid</a\n" +
				"	 * @see <a href=\"invalid\" target=\"_top\">invalid</a> no text allowed after the href\n" +
				"	 */\n" +
				"	void foo() {}\n" +
				"}\n"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	* @see <a href=\"invalid\" target\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	* @see <a href=\"invalid\" target=\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	* @see <a href=\"invalid\" target=\"\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 8)\n" +
			"	* @see <a href=\"invalid\" target=\"_top\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 9)\n" +
			"	* @see <a href=\"invalid\" target=\"_top\"\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 10)\n" +
			"	* @see <a href=\"invalid\" target=\"_top\">\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 11)\n" +
			"	* @see <a href=\"invalid\" target=\"_top\">\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"8. ERROR in X.java (at line 12)\n" +
			"	* @see <a href=\"invalid\" target=\"_top\">invalid\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"9. ERROR in X.java (at line 13)\n" +
			"	* @see <a href=\"invalid\" target=\"_top\">invalid<\n" +
			"	                                              ^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"10. ERROR in X.java (at line 14)\n" +
			"	* @see <a href=\"invalid\" target=\"_top\">invalid</\n" +
			"	                                              ^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"11. ERROR in X.java (at line 15)\n" +
			"	* @see <a href=\"invalid\" target=\"_top\">invalid</a\n" +
			"	                                              ^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"12. ERROR in X.java (at line 16)\n" +
			"	* @see <a href=\"invalid\" target=\"_top\">invalid</a> no text allowed after the href\n" +
			"	                                               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Unexpected text\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug68726negative2() {
	runNegativeTest(
		new String[] {
			"X.java",
			"/**\n" +
				"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">IBM Home Page\n" +
				"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">\n" +
				"	*          IBM Home Page\n" +
				"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">\n" +
				"	*          IBM Home Page<\n" +
				"	* 			/a>\n" +
				"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">\n" +
				"	*\n" +
				"	*          IBM\n" +
				"	*\n" +
				"	*          Home Page\n" +
				"	*\n" +
				"	*\n" +
				"	* 			\n" +
				"	* @see Unknown\n" +
				"	*/\n" +
				"public class X {\n" +
				"}\n"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">IBM Home Page\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">\n" +
			"	*          IBM Home Page\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	*          IBM Home Page<\n" +
			"	                        ^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 8)\n" +
			"	* @see <a href=\"http:/www.ibm.com\" target=\"_top\">\n" +
			"	*\n" +
			"	*          IBM\n" +
			"	*\n" +
			"	*          Home Page\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 16)\n" +
			"	* @see Unknown\n" +
			"	       ^^^^^^^\n" +
			"Javadoc: Unknown cannot be resolved to a type\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 69272: [Javadoc] Invalid malformed reference (missing separator)
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=69272">69272</a>
 */
public void testBug69272classValid() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**@see Object*/\n" +
				"	public void foo1() {}\n" +
				"	/**@see Object\n" +
				"	*/\n" +
				"	public void foo2() {}\n" +
				"	/**@see Object    */\n" +
				"	public void foo3() {}\n" +
				"	/**@see Object****/\n" +
				"	public void foo4() {}\n" +
				"	/**@see Object		****/\n" +
				"	public void foo5() {}\n" +
				"}\n"
		}
	);
}
public void testBug69272classInvalid() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**@see Object* */\n" +
				"	public void foo1() {}\n" +
				"	/**@see Object*** ***/\n" +
				"	public void foo2() {}\n" +
				"	/**@see Object***\n" +
				"	 */\n" +
				"	public void foo3() {}\n" +
				"}\n"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	/**@see Object* */\n" +
			"	        ^^^^^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	/**@see Object*** ***/\n" +
			"	        ^^^^^^^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	/**@see Object***\n" +
			"	        ^^^^^^^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug69272fieldValid() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	int field;\n" +
				"	/**@see #field*/\n" +
				"	public void foo1() {}\n" +
				"	/**@see #field\n" +
				"	*/\n" +
				"	public void foo2() {}\n" +
				"	/**@see #field    */\n" +
				"	public void foo3() {}\n" +
				"	/**@see #field****/\n" +
				"	public void foo4() {}\n" +
				"	/**@see #field		********/\n" +
				"	public void foo5() {}\n" +
				"}\n"
		}
	);
}
public void testBug69272fieldInvalid() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	int field;\n" +
				"	/**@see #field* */\n" +
				"	public void foo1() {}\n" +
				"	/**@see #field*** ***/\n" +
				"	public void foo2() {}\n" +
				"	/**@see #field***\n" +
				"	 */\n" +
				"	public void foo3() {}\n" +
				"}\n"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	/**@see #field* */\n" +
			"	        ^^^^^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	/**@see #field*** ***/\n" +
			"	        ^^^^^^^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	/**@see #field***\n" +
			"	        ^^^^^^^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug69272methodValid() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**@see Object#wait()*/\n" +
				"	public void foo1() {}\n" +
				"	/**@see Object#wait()\n" +
				"	*/\n" +
				"	public void foo2() {}\n" +
				"	/**@see Object#wait()    */\n" +
				"	public void foo3() {}\n" +
				"	/**@see Object#wait()****/\n" +
				"	public void foo4() {}\n" +
				"	/**@see Object#wait()		****/\n" +
				"	public void foo5() {}\n" +
				"}\n"
		}
	);
}
public void testBug69272methodInvalid() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**@see Object#wait()* */\n" +
				"	public void foo1() {}\n" +
				"	/**@see Object#wait()*** ***/\n" +
				"	public void foo2() {}\n" +
				"	/**@see Object#wait()***\n" +
				"	 */\n" +
				"	public void foo3() {}\n" +
				"}\n"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	/**@see Object#wait()* */\n" +
			"	                   ^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	/**@see Object#wait()*** ***/\n" +
			"	                   ^^^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	/**@see Object#wait()***\n" +
			"	                   ^^^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 69275: [Javadoc] Invalid warning on @see link
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=69275">69275</a>
 */
public void testBug69275conform() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**@see <a href=\"http://www.eclipse.org\">text</a>*/\n" +
				"	void foo1() {}\n" +
				"	/**@see <a href=\"http://www.eclipse.org\">text</a>\n" +
				"	*/\n" +
				"	void foo2() {}\n" +
				"	/**@see <a href=\"http://www.eclipse.org\">text</a>		*/\n" +
				"	void foo3() {}\n" +
				"	/**@see <a href=\"http://www.eclipse.org\">text</a>**/\n" +
				"	void foo4() {}\n" +
				"	/**@see <a href=\"http://www.eclipse.org\">text</a>     *****/\n" +
				"	void foo5() {}\n" +
				"}\n"
		}
	);
}
public void testBug69275negative() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**@see <a href=\"http://www.eclipse.org\">text</a>* */\n" +
				"	void foo1() {}\n" +
				"	/**@see <a href=\"http://www.eclipse.org\">text</a>	** **/\n" +
				"	void foo2() {}\n" +
				"	/**@see <a href=\"http://www.eclipse.org\">text</a>**\n" +
				"	*/\n" +
				"	void foo3() {}\n" +
				"}\n"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	/**@see <a href=\"http://www.eclipse.org\">text</a>* */\n" +
			"	                                              ^^^^^^^\n" +
			"Javadoc: Unexpected text\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	/**@see <a href=\"http://www.eclipse.org\">text</a>	** **/\n" +
			"	                                              ^^^^^^^^^^\n" +
			"Javadoc: Unexpected text\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 69302: [Javadoc] Invalid reference warning inconsistent with javadoc tool
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=69302"
 */
public void testBug69302conform1() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**\n" +
				"	 *	@see Object <a href=\"http://www.eclipse.org\">Eclipse</a>\n" +
				"	 */\n" +
				"	void foo1() {}\n" +
				"	/**\n" +
				"	 *	@see Object \"Valid string reference\"\n" +
				"	 */\n" +
				"	void foo2() {}\n" +
				"}\n"
		}
	);
}
public void testBug69302negative1() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**\n" +
				"	 *	@see Unknown <a href=\"http://www.eclipse.org\">Eclipse</a>\n" +
				"	 */\n" +
				"	void foo1() {}\n" +
				"	/**\n" +
				"	 *	@see Unknown \"Valid string reference\"\n" +
				"	 */\n" +
				"	void foo2() {}\n" +
				"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	*	@see Unknown <a href=\"http://www.eclipse.org\">Eclipse</a>\n" +
		"	 	     ^^^^^^^\n" +
		"Javadoc: Unknown cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	*	@see Unknown \"Valid string reference\"\n" +
		"	 	     ^^^^^^^\n" +
		"Javadoc: Unknown cannot be resolved to a type\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug69302negative2() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**@see Unknown blabla <a href=\"http://www.eclipse.org\">text</a>*/\n" +
				"	void foo1() {}\n" +
				"	/**@see Unknown blabla \"Valid string reference\"*/\n" +
				"	void foo2() {}\n" +
				"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	/**@see Unknown blabla <a href=\"http://www.eclipse.org\">text</a>*/\n" +
		"	        ^^^^^^^\n" +
		"Javadoc: Unknown cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	/**@see Unknown blabla \"Valid string reference\"*/\n" +
		"	        ^^^^^^^\n" +
		"Javadoc: Unknown cannot be resolved to a type\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * bug 70892: [1.5][Javadoc] Compiler should parse reference for inline tag @value
 * test Ensure that reference in tag 'value' is only verified when source level >= 1.5
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=70892"
 */
public void testBug70892a() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	/**\n" +
			"	 * {@value}\n" +
			"	 */\n" +
			"	static int field1;\n" +
			"	/**\n" +
			"	 * {@value }\n" +
			"	 */\n" +
			"	static int field2;\n" +
			"	/**\n" +
			"	 * {@value #field}\n" +
			"	 */\n" +
			"	static int field;\n" +
			"}\n"
		}
	);
}
public void testBug70892b() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	/**\n" +
		"	 * {@value \"invalid\"}\n" +
		"	 */\n" +
		"	final static int field1 = 1;\n" +
		"	/**\n" +
		"	 * {@value <a href=\"invalid\">invalid</a>} invalid\n" +
		"	 */\n" +
		"	final static int field2 = 2;\n" +
		"	/**\n" +
		"	 * {@value #field}\n" +
		"	 */\n" +
		"	final static int field3 = 3;\n" +
		"	/**\n" +
		"	 * {@value #foo}\n" +
		"	 */\n" +
		"	final static int field4 = 4;\n" +
		"	/**\n" +
		"	 * {@value #foo()}\n" +
		"	 */\n" +
		"	final static int field5 = 5;\n" +
		"	void foo() {}\n" +
		"}\n"
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runConformTest(testFiles);
	} else {
		runNegativeTest(testFiles,
			"----------\n" +
			"1. ERROR in X.java (at line 3)\r\n" +
			"	* {@value \"invalid\"}\r\n" +
			"	          ^^^^^^^^^\n" +
			"Javadoc: Only static field reference is allowed for @value tag\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\r\n" +
			"	* {@value <a href=\"invalid\">invalid</a>} invalid\r\n" +
			"	          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Only static field reference is allowed for @value tag\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 11)\r\n" +
			"	* {@value #field}\r\n" +
			"	           ^^^^^\n" +
			"Javadoc: field cannot be resolved or is not a field\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 15)\r\n" +
			"	* {@value #foo}\r\n" +
			"	           ^^^\n" +
			"Javadoc: Only static field reference is allowed for @value tag\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 19)\r\n" +
			"	* {@value #foo()}\r\n" +
			"	           ^^^^^\n" +
			"Javadoc: Only static field reference is allowed for @value tag\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
}

/**
 * Bug 73348: [Javadoc] Missing description for return tag is not always warned
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=73348">73348</a>
 */
public void testBug73348conform() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**\n" +
				"	 *	@return      \n" +
				"	 *	int\n" +
				"	 */\n" +
				"	public int foo1() {return 0; }\n" +
				"	/**\n" +
				"	 *	@return      \n" +
				"	 *	int\n" +
				"	 *	@see Object\n" +
				"	 */\n" +
				"	public int foo2() {return 0; }\n" +
				"}\n",
		}
	);
}
public void testBug73348negative() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**\n" +
				"	 *	@return\n" +
				"	 *	@see Object\n" +
				"	 */\n" +
				"	public int foo1() {return 0; }\n" +
				"	/**\n" +
				"	 *	@return      \n" +
				"	 *	@see Object\n" +
				"	 */\n" +
				"	public int foo2() {return 0; }\n" +
				"}\n",
		},
		"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	*	@return\n" +
			"	 	 ^^^^^^\n" +
			"Javadoc: Description expected after @return\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 8)\n" +
			"	*	@return      \n" +
			"	 	 ^^^^^^\n" +
			"Javadoc: Description expected after @return\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * bug 73352: [Javadoc] Missing description should be warned for all tags
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=73352"
 */
public void testBug73352a() {
	String[] units = new String[] {
		"X.java",
		"/**\n" +
		"* @since\n" +
		"* @author\n" +
		"* @version\n" +
		"*/\n" +
		"public class X {\n" +
		"	/**\n" +
		"	 * @param  aParam\n" +
		"	 * @return\n" +
		"	 * @see\n" +
		"	 * @since\n" +
		"	 * @throws NullPointerException\n" +
		"	 * @exception NullPointerException\n" +
		"	 * @serial\n" +
		"	 * @serialData\n" +
		"	 * @serialField\n" +
		"	 * @deprecated\n" +
		"	 */\n" +
		"	public String foo(String aParam) {\n" +
		"		return new String();\n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runConformTest(
			true,
			units,
			"----------\n" +
			"1. WARNING in X.java (at line 2)\n" +
			"	* @since\n" +
			"	   ^^^^^\n" +
			"Javadoc: Description expected after @since\n" +
			"----------\n" +
			"2. WARNING in X.java (at line 3)\n" +
			"	* @author\n" +
			"	   ^^^^^^\n" +
			"Javadoc: Description expected after @author\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 4)\n" +
			"	* @version\n" +
			"	   ^^^^^^^\n" +
			"Javadoc: Description expected after @version\n" +
			"----------\n" +
			"4. WARNING in X.java (at line 8)\n" +
			"	* @param  aParam\n" +
			"	          ^^^^^^\n" +
			"Javadoc: Description expected after this reference\n" +
			"----------\n" +
			"5. WARNING in X.java (at line 9)\n" +
			"	* @return\n" +
			"	   ^^^^^^\n" +
			"Javadoc: Description expected after @return\n" +
			"----------\n" +
			"6. WARNING in X.java (at line 10)\n" +
			"	* @see\n" +
			"	   ^^^\n" +
			"Javadoc: Missing reference\n" +
			"----------\n" +
			"7. WARNING in X.java (at line 11)\n" +
			"	* @since\n" +
			"	   ^^^^^\n" +
			"Javadoc: Description expected after @since\n" +
			"----------\n" +
			"8. WARNING in X.java (at line 12)\n" +
			"	* @throws NullPointerException\n" +
			"	          ^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Description expected after this reference\n" +
			"----------\n" +
			"9. WARNING in X.java (at line 13)\n" +
			"	* @exception NullPointerException\n" +
			"	             ^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Description expected after this reference\n" +
			"----------\n" +
			"10. WARNING in X.java (at line 14)\n" +
			"	* @serial\n" +
			"	   ^^^^^^\n" +
			"Javadoc: Description expected after @serial\n" +
			"----------\n" +
			"11. WARNING in X.java (at line 15)\n" +
			"	* @serialData\n" +
			"	   ^^^^^^^^^^\n" +
			"Javadoc: Description expected after @serialData\n" +
			"----------\n" +
			"12. WARNING in X.java (at line 16)\n" +
			"	* @serialField\n" +
			"	   ^^^^^^^^^^^\n" +
			"Javadoc: Description expected after @serialField\n" +
			"----------\n" +
			"13. WARNING in X.java (at line 17)\n" +
			"	* @deprecated\n" +
			"	   ^^^^^^^^^^\n" +
			"Javadoc: Description expected after @deprecated\n" +
			"----------\n",
			null,
			null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings
		);
}
public void testBug73352b() {
	String[] units = new String[] {
		"X.java",
		"/**\n" +
		"* @since 1.0\n" +
		"* @author John Doe\n" +
		"* @version 1.1\n" +
		"*/\n" +
		"public class X {\n" +
		"	/**\n" +
		"	 * @param  aParam comment\n" +
		"	 * @return String\n" +
		"	 * @see String\n" +
		"	 * @since 1.1\n" +
		"	 * @throws NullPointerException an exception\n" +
		"	 * @exception NullPointerException an exception\n" +
		"	 * @serial aSerial\n" +
		"	 * @serialData aSerialData\n" +
		"	 * @serialField aSerialField\n" +
		"	 * @deprecated use another method\n" +
		"	 */\n" +
		"	public String foo(String aParam) {\n" +
		"		return new String();\n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runConformTest(units);
}
public void testBug73352c() {
	String[] units = new String[] {
		"X.java",
		"/**\n" +
		"* @since\n" +
		"* @author\n" +
		"* @version\n" +
		"*/\n" +
		"public class X {\n" +
		"	/**\n" +
		"	 * @param  aParam\n" +
		"	 * @return\n" +
		"	 * @see\n" +
		"	 * @since\n" +
		"	 * @throws NullPointerException\n" +
		"	 * @exception NullPointerException\n" +
		"	 * @serial\n" +
		"	 * @serialData\n" +
		"	 * @serialField\n" +
		"	 * @deprecated\n" +
		"	 */\n" +
		"	public String foo(String aParam) {\n" +
		"		return new String();\n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	this.reportMissingJavadocDescription = CompilerOptions.RETURN_TAG;
	runConformTest(
		true,
		units,
		"----------\n" +
		"1. WARNING in X.java (at line 9)\n" +
		"	* @return\n" +
		"	   ^^^^^^\n" +
		"Javadoc: Description expected after @return\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 10)\n" +
		"	* @see\n" +
		"	   ^^^\n" +
		"Javadoc: Missing reference\n" +
		"----------\n",
		null,
		null,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings
		);
}

public void testBug73352d() {
	String[] units = new String[] {
		"X.java",
		"/**\n" +
		"* @since\n" +
		"* @author\n" +
		"* @version\n" +
		"*/\n" +
		"public class X {\n" +
		"	/**\n" +
		"	 * @param  aParam\n" +
		"	 * @return\n" +
		"	 * @see\n" +
		"	 * @since\n" +
		"	 * @throws NullPointerException\n" +
		"	 * @exception NullPointerException\n" +
		"	 * @serial\n" +
		"	 * @serialData\n" +
		"	 * @serialField\n" +
		"	 * @deprecated\n" +
		"	 */\n" +
		"	public String foo(String aParam) {\n" +
		"		return new String();\n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	this.reportMissingJavadocDescription = CompilerOptions.NO_TAG;
	runConformTest(units);
}

/**
 * Bug 73479: [Javadoc] Improve error message for invalid link in @see tags
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=73479">73479</a>
 */
public void testBug73479() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**\n" +
				"	 *	@see <a href=\"spec.html#section\">Java Spec<a>\n" +
				"	 */\n" +
				"	public void foo() {}\n" +
				"}\n",
		},
		"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	*	@see <a href=\"spec.html#section\">Java Spec<a>\n" +
			"	 	                                          ^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 73995: [Javadoc] Wrong warning for missing return type description for &#064;return {&#064;inheritdoc}
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=73995">73995</a>
 */
public void testBug73995() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X extends Base {\n" +
				"	/**\n" +
				"	 *	@return {@link Object}     \n" +
				"	 */\n" +
				"	public int foo1() {return 0; }\n" +
				"	/** @return {@inheritDoc} */\n" +
				"	public int foo2() {return 0; }\n" +
				"	/**\n" +
				"	 *	@return\n" +
				"	 *		{@unknown_tag}\n" +
				"	 */\n" +
				"	public int foo3() {return 0; }\n" +
				"}\n" +
				"class Base {\n" +
				"/** return \"The foo2 value\" */" +
				"public int foo2(){return 0;}\n" +
				"}"
		});
}

/**
 * Bug 74369: [Javadoc] incorrect javadoc in local class
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=74369">74369</a>
 */
public void testBug74369() {
	runConformTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
				"   public void method() {\n" +
				"       /**\n" +
				"        * @see #hsgdfdj\n" +
				"        */\n" +
				"        System.out.println(\"println\");\n" +
				"        class Local {}\n" +
				"    }\n" +
				"}"
		}
	);
}
public void testBug74369deprecated() {
	runNegativeTest(
		new String[] {
			"p/Y.java",
			"package p;\n" +
				"\n" +
				"\n" +
				"public class Y {\n" +
				"   /**\n" +
				"    * @deprecated\n" +
				"    */\n" +
				"   public void bar() {}\n" +
				"}\n",
			"X.java",
			"import p.Y;\n" +
				"\n" +
				"public class X {\n" +
				"	Object obj = new Object() {\n" +
				"		public boolean equals(Object o) {\n" +
				"			/**\n" +
				"			 * @deprecated\n" +
				"			 */\n" +
				"	        System.out.println(\"println\");\n" +
				"	        class Local {\n" +
				"	        	void bar() {\n" +
				"					new Y().bar();\n" +
				"	        	}\n" +
				"	        }\n" +
				"			return super.equals(o);\n" +
				"		}\n" +
				"	};\n" +
				"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	new Y().bar();\n" +
		"	        ^^^^^\n" +
		"The method bar() from the type Y is deprecated\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 76324: [Javadoc] Wrongly reports invalid link format in @see and @link
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=76324">76324</a>
 */
public void testBug76324() {
	runConformTest(
		new String[] {
			"X.java",
			"\n" +
				"/**\n" +
				" * Subclasses perform GUI-related work in a dedicated thread. For instructions\n" +
				" * on using this class, see\n" +
				" * {@link <a  href=\"http://download.oracle.com/javase/tutorial/uiswing/misc/index.html\"> Swing tutorial </a>}\n" +
				" * \n" +
				" * @see <a\n" +
				" *      href=\"http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html\">\n" +
				" *      EDU.oswego.cs.dl.util.concurrent </a>\n" +
				" * @see <a\n" +
				" *      href=\"http://download.oracle.com/javase/6/docs/api/java/util/concurrent/package-summary.html\">\n" +
				" *      JDK 6.0 </a>\n" +
				" * @author {@link <a href=\"http://gee.cs.oswego.edu/dl\">Doug Lea</a>}\n" +
				" * @author {@link <a href=\"http://home.pacbell.net/jfai\">J?rgen Failenschmid</a>}\n" +
				"  *\n" +
				"  * It is assumed that you have read the introductory document\n" +
				"  * {@link <a HREF=\"../../../../../internat/overview.htm\">\n" +
				"  * Internationalization</a>}\n" +
				"  * and are familiar with \n" +
				" */\n" +
				"public class X {\n" +
				"\n" +
				"}\n"
		}
	);
}
// URL Link references
public void testBug76324url() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**\n" +
				"	 * Invalid inline URL link references \n" +
				"	 *\n" +
				"	 * {@link <}\n" +
				"	 * {@link <a}\n" +
				"	 * {@link <a hre}\n" +
				"	 * {@link <a href}\n" +
				"	 * {@link <a href=}\n" +
				"	 * {@link <a href=\"}\n" +
				"	 * {@link <a href=\"invalid}\n" +
				"	 * {@link <a href=\"invalid\"}\n" +
				"	 * {@link <a href=\"invalid\">}\n" +
				"	 * {@link <a href=\"invalid\">invalid}\n" +
				"	 * {@link <a href=\"invalid\">invalid<}\n" +
				"	 * {@link <a href=\"invalid\">invalid</}\n" +
				"	 * {@link <a href=\"invalid\">invalid</a}\n" +
				"	 * {@link <a href=\"invalid\">invalid</a> no text allowed after}\n" +
				"	 */\n" +
				"	public void s_foo() {\n" +
				"	}\n" +
				"}\n"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	* {@link <}\n" +
			"	         ^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	* {@link <a}\n" +
			"	         ^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	* {@link <a hre}\n" +
			"	         ^^^^^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 8)\n" +
			"	* {@link <a href}\n" +
			"	         ^^^^^^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 9)\n" +
			"	* {@link <a href=}\n" +
			"	         ^^^^^^^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 10)\n" +
			"	* {@link <a href=\"}\n" +
			"	         ^^^^^^^^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 11)\n" +
			"	* {@link <a href=\"invalid}\n" +
			"	         ^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"8. ERROR in X.java (at line 12)\n" +
			"	* {@link <a href=\"invalid\"}\n" +
			"	         ^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"9. ERROR in X.java (at line 13)\n" +
			"	* {@link <a href=\"invalid\">}\n" +
			"	         ^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"10. ERROR in X.java (at line 14)\n" +
			"	* {@link <a href=\"invalid\">invalid}\n" +
			"	         ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"11. ERROR in X.java (at line 15)\n" +
			"	* {@link <a href=\"invalid\">invalid<}\n" +
			"	                                  ^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"12. ERROR in X.java (at line 16)\n" +
			"	* {@link <a href=\"invalid\">invalid</}\n" +
			"	                                  ^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"13. ERROR in X.java (at line 17)\n" +
			"	* {@link <a href=\"invalid\">invalid</a}\n" +
			"	                                  ^^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n" +
			"14. ERROR in X.java (at line 18)\n" +
			"	* {@link <a href=\"invalid\">invalid</a> no text allowed after}\n" +
			"	                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Unexpected text\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
// String references
public void testBug76324string() {
	runNegativeTest(
		new String[] {
		"X.java",
		"public class X {\n" +
		"	/**\n" +
		"	 * Inline string references \n" +
		"	 *\n" +
		"	 * {@link \"}\n" +
		"	 * {@link \"unterminated string}\n" +
		"	 * {@link \"invalid string\"\"}\n" +
		"	 * {@link \"valid string\"}\n" +
		"	 * {@link \"invalid\" no text allowed after the string}\n" +
		"	 */\n" +
		"	public void s_foo() {\n" +
		"	}\n" +
		"}\n" },
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	* {@link \"}\n" +
		"	         ^^\n" +
		"Javadoc: Invalid reference\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	* {@link \"unterminated string}\n" +
		"	         ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: Invalid reference\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 7)\n" +
		"	* {@link \"invalid string\"\"}\n" +
		"	                         ^^\n" +
		"Javadoc: Unexpected text\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 9)\n" +
		"	* {@link \"invalid\" no text allowed after the string}\n" +
		"	                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: Unexpected text\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 77510: [javadoc] compiler wrongly report deprecation when option "process javadoc comments" is not set
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=77510">77510</a>
 */
public void testBug77510enabled() {
	runNegativeTest(
		new String[] {
			"A.java",
			"public class A {\n" +
				"	/** \\u0009 @deprecated */\n" +
				"	static int i0009;\n" +
				"	/** \\u000a @deprecated */\n" +
				"	static int i000a;\n" +
				"	/** \\u000b @deprecated */\n" +
				"	static int i000b;\n" +
				"	/** \\u000c @deprecated */\n" +
				"	static int i000c;\n" +
				"	/** \\u001c @deprecated */\n" +
				"	static int i001c;\n" +
				"	/** \\u001d @deprecated */\n" +
				"	static int i001d;\n" +
				"	/** \\u001e @deprecated */\n" +
				"	static int i001e;\n" +
				"	/** \\u001f @deprecated */\n" +
				"	static int i001f;\n" +
				"	/** \\u2007 @deprecated */\n" +
				"	static int i2007;\n" +
				"	/** \\u202f @deprecated */\n" +
				"	static int i202f;\n" +
				"}\n",
			"X.java",
			"public class X {\n" +
				"	int i0 = A.i0009;\n" +
				"	int i1 = A.i000a;\n" +
				"	int i2 = A.i000b;\n" +
				"	int i3 = A.i000c;\n" +
				"	int i4 = A.i001c;\n" +
				"	int i5 = A.i001d;\n" +
				"	int i6 = A.i001e;\n" +
				"	int i7 = A.i001f;\n" +
				"	int i8 = A.i2007;\n" +
				"	int i9 = A.i202f;\n" +
				"}\n" },
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	int i0 = A.i0009;\n" +
		"	           ^^^^^\n" +
		"The field A.i0009 is deprecated\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	int i1 = A.i000a;\n" +
		"	           ^^^^^\n" +
		"The field A.i000a is deprecated\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 5)\n" +
		"	int i3 = A.i000c;\n" +
		"	           ^^^^^\n" +
		"The field A.i000c is deprecated\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug77510disabled() {
	this.docCommentSupport = CompilerOptions.IGNORE;
	runNegativeTest(
		new String[] {
			"A.java",
			"public class A {\n" +
				"	/** \\u0009 @deprecated */\n" +
				"	static int i0009;\n" +
				"	/** \\u000a @deprecated */\n" +
				"	static int i000a;\n" +
				"	/** \\u000b @deprecated */\n" +
				"	static int i000b;\n" +
				"	/** \\u000c @deprecated */\n" +
				"	static int i000c;\n" +
				"	/** \\u001c @deprecated */\n" +
				"	static int i001c;\n" +
				"	/** \\u001d @deprecated */\n" +
				"	static int i001d;\n" +
				"	/** \\u001e @deprecated */\n" +
				"	static int i001e;\n" +
				"	/** \\u001f @deprecated */\n" +
				"	static int i001f;\n" +
				"	/** \\u2007 @deprecated */\n" +
				"	static int i2007;\n" +
				"	/** \\u202f @deprecated */\n" +
				"	static int i202f;\n" +
				"}\n",
			"X.java",
			"public class X {\n" +
				"	int i0 = A.i0009;\n" +
				"	int i1 = A.i000a;\n" +
				"	int i2 = A.i000b;\n" +
				"	int i3 = A.i000c;\n" +
				"	int i4 = A.i001c;\n" +
				"	int i5 = A.i001d;\n" +
				"	int i6 = A.i001e;\n" +
				"	int i7 = A.i001f;\n" +
				"	int i8 = A.i2007;\n" +
				"	int i9 = A.i202f;\n" +
				"}\n" },
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	int i0 = A.i0009;\n" +
		"	           ^^^^^\n" +
		"The field A.i0009 is deprecated\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	int i1 = A.i000a;\n" +
		"	           ^^^^^\n" +
		"The field A.i000a is deprecated\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 5)\n" +
		"	int i3 = A.i000c;\n" +
		"	           ^^^^^\n" +
		"The field A.i000c is deprecated\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Test bug 77260: [Javadoc] deprecation warning should not be reported when @deprecated tag is set
 */
public void testBug77260() {
	runConformTest(
		new String[] {
			"X.java",
			"/** @deprecated */\n" +
				"public class X {\n" +
				"	public int x;\n" +
				"	public void foo() {}\n" +
				"}\n",
			"Y.java",
			"/**\n" +
				" * @see X\n" +
				" * @deprecated\n" +
				" */\n" +
				"public class Y {\n" +
				"	/** @see X#x */\n" +
				"	public int x;\n" +
				"	/** @see X#foo() */\n" +
				"	public void foo() {}\n" +
				"}\n",
			"Z.java",
			"public class Z {\n" +
				"	/** \n" +
				"	 * @see X#x\n" +
				"	 * @deprecated\n" +
				"	 */\n" +
				"	public int x;\n" +
				"	/**\n" +
				"	 * @see X#foo()\n" +
				"	 * @deprecated\n" +
				"	 */\n" +
				"	public void foo() {}\n" +
				"}\n" }
	);
}
public void testBug77260nested() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode, CompilerOptions.ENABLED);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"/** @deprecated */\n" +
				"public class X {\n" +
				"	public int x;\n" +
				"	public void foo() {}\n" +
				"}\n",
			"Y.java",
			"/**\n" +
				" * @see X\n" +
				" * @deprecated\n" +
				" */\n" +
				"public class Y {\n" +
				"	/** @see X#x */\n" +
				"	public int x;\n" +
				"	/** @see X#foo() */\n" +
				"	public void foo() {}\n" +
				"}\n",
			"Z.java",
			"public class Z {\n" +
				"	/** \n" +
				"	 * @see X#x\n" +
				"	 * @deprecated\n" +
				"	 */\n" +
				"	public int x;\n" +
				"	/**\n" +
				"	 * @see X#foo()\n" +
				"	 * @deprecated\n" +
				"	 */\n" +
				"	public void foo() {}\n" +
				"}\n" },
		null,
		options,
		"----------\n" +
			"1. ERROR in Y.java (at line 2)\n" +
			"	* @see X\n" +
			"	       ^\n" +
			"Javadoc: The type X is deprecated\n" +
			"----------\n" +
			"2. ERROR in Y.java (at line 6)\n" +
			"	/** @see X#x */\n" +
			"	         ^\n" +
			"Javadoc: The type X is deprecated\n" +
			"----------\n" +
			"3. ERROR in Y.java (at line 6)\n" +
			"	/** @see X#x */\n" +
			"	           ^\n" +
			"Javadoc: The field X.x is deprecated\n" +
			"----------\n" +
			"4. ERROR in Y.java (at line 8)\n" +
			"	/** @see X#foo() */\n" +
			"	         ^\n" +
			"Javadoc: The type X is deprecated\n" +
			"----------\n" +
			"5. ERROR in Y.java (at line 8)\n" +
			"	/** @see X#foo() */\n" +
			"	           ^^^^^\n" +
			"Javadoc: The method foo() from the type X is deprecated\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in Z.java (at line 3)\n" +
			"	* @see X#x\n" +
			"	       ^\n" +
			"Javadoc: The type X is deprecated\n" +
			"----------\n" +
			"2. ERROR in Z.java (at line 3)\n" +
			"	* @see X#x\n" +
			"	         ^\n" +
			"Javadoc: The field X.x is deprecated\n" +
			"----------\n" +
			"3. ERROR in Z.java (at line 8)\n" +
			"	* @see X#foo()\n" +
			"	       ^\n" +
			"Javadoc: The type X is deprecated\n" +
			"----------\n" +
			"4. ERROR in Z.java (at line 8)\n" +
			"	* @see X#foo()\n" +
			"	         ^^^^^\n" +
			"Javadoc: The method foo() from the type X is deprecated\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug77260nested_disabled() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsDeprecatedRef, CompilerOptions.DISABLED);
	runConformTest(
		new String[] {
			"X.java",
			"/** @deprecated */\n" +
				"public class X {\n" +
				"	public int x;\n" +
				"	public void foo() {}\n" +
				"}\n",
			"Y.java",
			"/**\n" +
				" * @see X\n" +
				" * @deprecated\n" +
				" */\n" +
				"public class Y {\n" +
				"	/** @see X#x */\n" +
				"	public int x;\n" +
				"	/** @see X#foo() */\n" +
				"	public void foo() {}\n" +
				"}\n",
			"Z.java",
			"public class Z {\n" +
				"	/** \n" +
				"	 * @see X#x\n" +
				"	 * @deprecated\n" +
				"	 */\n" +
				"	public int x;\n" +
				"	/**\n" +
				"	 * @see X#foo()\n" +
				"	 * @deprecated\n" +
				"	 */\n" +
				"	public void foo() {}\n" +
				"}\n"
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
}

/**
 * Bug 77602: [javadoc] "Only consider members as visible as" is does not work for syntax error
 */
public void testBug77602() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"  /**\n" +
				"   * @see\n" +
				"   * @see UnknownClass\n" +
				"   */\n" +
				"  protected void foo() {\n" +
				"  }\n" +
			"}\n"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	* @see\n" +
			"	   ^^^\n" +
			"Javadoc: Missing reference\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	* @see UnknownClass\n" +
			"	       ^^^^^^^^^^^^\n" +
			"Javadoc: UnknownClass cannot be resolved to a type\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug77602_Public() {
	this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"  /**\n" +
				"   * @see\n" +
				"   * @see UnknownClass\n" +
				"   */\n" +
				"  protected void foo() {\n" +
				"  }\n" +
			"}\n"
		}
	);
}

/**
 * Bug 78091: [1.5][javadoc] Compiler should accept new 1.5 syntax for @param
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=78091">78091</a>
 */
public void testBug78091() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
				"	/**\n" +
				"	 * Valid type parameter reference\n" +
				"	 * @param xxx.yyy invalid\n" +
				"	 * @param obj(x) invalid\n" +
				"	 */\n" +
				"	public void foo(int xxx, Object obj) {}\n" +
				"}"
		},
		"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	* @param xxx.yyy invalid\n" +
			"	         ^^^^^^^\n" +
			"Javadoc: Invalid param tag name\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	* @param obj(x) invalid\n" +
			"	         ^^^^^^\n" +
			"Javadoc: Invalid param tag name\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	public void foo(int xxx, Object obj) {}\n" +
			"	                    ^^^\n" +
			"Javadoc: Missing tag for parameter xxx\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 7)\n" +
			"	public void foo(int xxx, Object obj) {}\n" +
			"	                                ^^^\n" +
			"Javadoc: Missing tag for parameter obj\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 80910: [javadoc] Invalid missing reference warning on @see or @link tags
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=80910"
 */
public void testBug80910() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"	int field;\n" +
			"\n" +
			"	/**\n" +
			"	 * @param key\'s toto\n" +
			"	 * @see #field\n" +
			"	 */\n" +
			"	public void foo(int x) {\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Test.java (at line 5)\n" +
		"	* @param key\'s toto\n" +
		"	         ^^^^^\n" +
		"Javadoc: Invalid param tag name\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 82088: [search][javadoc] Method parameter types references not found in @see/@link tags
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=82088"
 */
public void testBug82088() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"	int field;\n" +
			"\n" +
			"	/**\n" +
			"	 * @param key\'s toto\n" +
			"	 * @see #field\n" +
			"	 */\n" +
			"	public void foo(int x) {\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Test.java (at line 5)\n" +
		"	* @param key\'s toto\n" +
		"	         ^^^^^\n" +
		"Javadoc: Invalid param tag name\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 83285: [javadoc] Javadoc reference to constructor of secondary type has no binding / not found by search
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=83285"
 */
public void testBug83285a() {
	runConformTest(
		new String[] {
			"p/A.java",
			"package p;\n" +
			"class A { }\n" +
			"class C {\n" +
			"    /**\n" +
			"     * Link {@link #C(String)} was also wrongly warned...\n" +
			"     */\n" +
			"    private String fGerman;\n" +
			"    public C(String german) {\n" +
			"        fGerman = german;\n" +
			"    }\n" +
			"}"
		}
	);
}
public void testBug83285b() {
	runConformTest(
		new String[] {
			"p/A.java",
			"package p;\n" +
			"class A {\n" +
			"	A(char c) {}\n" +
			"}\n" +
			"class B {\n" +
			"	B(Exception ex) {}\n" +
			"	void foo() {} \n" +
			"	class C { \n" +
			"	    /**\n" +
			"	     * Link {@link #B(Exception)} OK\n" +
			"	     * Link {@link #B.C(String)} OK\n" +
			"	     * Link {@link #foo()} OK\n" +
			"	     * Link {@link #bar()} OK\n" +
			"	     */\n" +
			"	    public C(String str) {}\n" +
			"		void bar() {}\n" +
			"	}\n" +
			"}"
		}
	);
}
public void testBug83285c() {
	runNegativeTest(
		new String[] {
			"p/A.java",
			"package p;\n" +
			"class A {\n" +
			"	A(char c) {}\n" +
			"}\n" +
			"class B {\n" +
			"	B(Exception ex) {}\n" +
			"	void foo() {}\n" +
			"	class C { \n" +
			"	    /**\n" +
			"	     * Link {@link #A(char)} KO\n" +
			"	     * Link {@link #B(char)}  KO\n" +
			"	     * Link {@link #C(char)} KO\n" +
			"	     * Link {@link #foo(int)} KO\n" +
			"	     * Link {@link #bar(int)} KO\n" +
			"	     */\n" +
			"	    public C(String str) {}\n" +
			"		void bar() {}\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in p\\A.java (at line 10)\n" +
		"	* Link {@link #A(char)} KO\n" +
		"	               ^\n" +
		"Javadoc: The method A(char) is undefined for the type B.C\n" +
		"----------\n" +
		"2. ERROR in p\\A.java (at line 11)\n" +
		"	* Link {@link #B(char)}  KO\n" +
		"	               ^\n" +
		"Javadoc: The method B(char) is undefined for the type B.C\n" +
		"----------\n" +
		"3. ERROR in p\\A.java (at line 12)\n" +
		"	* Link {@link #C(char)} KO\n" +
		"	               ^^^^^^^\n" +
		"Javadoc: The constructor B.C(char) is undefined\n" +
		"----------\n" +
		"4. ERROR in p\\A.java (at line 13)\n" +
		"	* Link {@link #foo(int)} KO\n" +
		"	               ^^^\n" +
		"Javadoc: The method foo(int) is undefined for the type B.C\n" +
		"----------\n" +
		"5. ERROR in p\\A.java (at line 14)\n" +
		"	* Link {@link #bar(int)} KO\n" +
		"	               ^^^\n" +
		"Javadoc: The method bar() in the type B.C is not applicable for the arguments (int)\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 86769: [javadoc] Warn/Error for 'Missing javadoc comments' doesn't recognize private inner classes
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=86769"
 */
public void testBug86769_Classes1() {
	this.reportMissingJavadocComments = CompilerOptions.ERROR;
	this.reportMissingJavadocCommentsVisibility = CompilerOptions.PROTECTED;
	runNegativeTest(
		new String[] {
			"A.java",
			"/**\n" +
			" * Test bug 86769 \n" +
			" */\n" +
			"public class A {\n" +
			"	private class Level1Private {\n" +
			"		private class Level2_PrivPriv {}\n" +
			"		class Level2_PrivDef {}\n" +
			"		protected class Level2_PrivPro {}\n" +
			"		public class Level2_PrivPub {}\n" +
			"	}\n" +
			"	class Level1Default{\n" +
			"		private class Level2_DefPriv {}\n" +
			"		class Level2_DefDef {}\n" +
			"		protected class Level2_DefPro {}\n" +
			"		public class Level2_DefPub {}\n" +
			"	}\n" +
			"	protected class Level1Protected {\n" +
			"		private class Level2_ProtPriv {}\n" +
			"		class Level2_ProDef {}\n" +
			"		protected class Level2_ProPro {}\n" +
			"		public class Level2_ProPub {} \n" +
			"	}\n" +
			"	public class Level1Public {\n" +
			"		private class Level2_PubPriv {}\n" +
			"		class Level2_PubDef {}\n" +
			"		protected class Level2_PubPro {}\n" +
			"		public class Level2_PubPub {}\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in A.java (at line 17)\n" +
		"	protected class Level1Protected {\n" +
		"	                ^^^^^^^^^^^^^^^\n" +
		"Javadoc: Missing comment for protected declaration\n" +
		"----------\n" +
		"2. ERROR in A.java (at line 20)\n" +
		"	protected class Level2_ProPro {}\n" +
		"	                ^^^^^^^^^^^^^\n" +
		"Javadoc: Missing comment for protected declaration\n" +
		"----------\n" +
		"3. ERROR in A.java (at line 21)\n" +
		"	public class Level2_ProPub {} \n" +
		"	             ^^^^^^^^^^^^^\n" +
		"Javadoc: Missing comment for protected declaration\n" +
		"----------\n" +
		"4. ERROR in A.java (at line 23)\n" +
		"	public class Level1Public {\n" +
		"	             ^^^^^^^^^^^^\n" +
		"Javadoc: Missing comment for public declaration\n" +
		"----------\n" +
		"5. ERROR in A.java (at line 26)\n" +
		"	protected class Level2_PubPro {}\n" +
		"	                ^^^^^^^^^^^^^\n" +
		"Javadoc: Missing comment for protected declaration\n" +
		"----------\n" +
		"6. ERROR in A.java (at line 27)\n" +
		"	public class Level2_PubPub {}\n" +
		"	             ^^^^^^^^^^^^^\n" +
		"Javadoc: Missing comment for public declaration\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug86769_Classes2() {
	this.reportMissingJavadocComments = CompilerOptions.ERROR;
	this.reportMissingJavadocCommentsVisibility = CompilerOptions.DEFAULT;
	runNegativeTest(
		new String[] {
			"B.java",
			"/**\n" +
			" * Test bug 86769\n" +
			" */\n" +
			"public class B {\n" +
			"	class Level0_Default {\n" +
			"		private class Level1Private {\n" +
			"			private class Level2_PrivPriv {}\n" +
			"			class Level2_PrivDef {}\n" +
			"			protected class Level2_PrivPro {}\n" +
			"			public class Level2_PrivPub {}\n" +
			"		}\n" +
			"	}\n" +
			"	public class Level0_Public {\n" +
			"		class Level1Default{\n" +
			"			private class Level2_DefPriv {}\n" +
			"			class Level2_DefDef {}\n" +
			"			protected class Level2_DefPro {}\n" +
			"			public class Level2_DefPub {}\n" +
			"		}\n" +
			"	}\n" +
			"	protected class Level0_Protected {\n" +
			"		protected class Level1Protected {\n" +
			"			private class Level2_ProtPriv {}\n" +
			"			class Level2_ProDef {}\n" +
			"			protected class Level2_ProPro {}\n" +
			"			public class Level2_ProPub {} \n" +
			"		}\n" +
			"	}\n" +
			"	private class Level0_Private {\n" +
			"		public class Level1Public {\n" +
			"			private class Level2_PubPriv {}\n" +
			"			class Level2_PubDef {}\n" +
			"			protected class Level2_PubPro {}\n" +
			"			public class Level2_PubPub {}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in B.java (at line 5)\n" +
		"	class Level0_Default {\n" +
		"	      ^^^^^^^^^^^^^^\n" +
		"Javadoc: Missing comment for default declaration\n" +
		"----------\n" +
		"2. ERROR in B.java (at line 13)\n" +
		"	public class Level0_Public {\n" +
		"	             ^^^^^^^^^^^^^\n" +
		"Javadoc: Missing comment for public declaration\n" +
		"----------\n" +
		"3. ERROR in B.java (at line 14)\n" +
		"	class Level1Default{\n" +
		"	      ^^^^^^^^^^^^^\n" +
		"Javadoc: Missing comment for default declaration\n" +
		"----------\n" +
		"4. ERROR in B.java (at line 16)\n" +
		"	class Level2_DefDef {}\n" +
		"	      ^^^^^^^^^^^^^\n" +
		"Javadoc: Missing comment for default declaration\n" +
		"----------\n" +
		"5. ERROR in B.java (at line 17)\n" +
		"	protected class Level2_DefPro {}\n" +
		"	                ^^^^^^^^^^^^^\n" +
		"Javadoc: Missing comment for default declaration\n" +
		"----------\n" +
		"6. ERROR in B.java (at line 18)\n" +
		"	public class Level2_DefPub {}\n" +
		"	             ^^^^^^^^^^^^^\n" +
		"Javadoc: Missing comment for default declaration\n" +
		"----------\n" +
		"7. ERROR in B.java (at line 21)\n" +
		"	protected class Level0_Protected {\n" +
		"	                ^^^^^^^^^^^^^^^^\n" +
		"Javadoc: Missing comment for protected declaration\n" +
		"----------\n" +
		"8. ERROR in B.java (at line 22)\n" +
		"	protected class Level1Protected {\n" +
		"	                ^^^^^^^^^^^^^^^\n" +
		"Javadoc: Missing comment for protected declaration\n" +
		"----------\n" +
		"9. ERROR in B.java (at line 24)\n" +
		"	class Level2_ProDef {}\n" +
		"	      ^^^^^^^^^^^^^\n" +
		"Javadoc: Missing comment for default declaration\n" +
		"----------\n" +
		"10. ERROR in B.java (at line 25)\n" +
		"	protected class Level2_ProPro {}\n" +
		"	                ^^^^^^^^^^^^^\n" +
		"Javadoc: Missing comment for protected declaration\n" +
		"----------\n" +
		"11. ERROR in B.java (at line 26)\n" +
		"	public class Level2_ProPub {} \n" +
		"	             ^^^^^^^^^^^^^\n" +
		"Javadoc: Missing comment for protected declaration\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug86769_Field1() {
	this.reportMissingJavadocComments = CompilerOptions.ERROR;
	this.reportMissingJavadocCommentsVisibility = CompilerOptions.PUBLIC;
	runNegativeTest(
		new String[] {
			"A.java",
			"/**\n" +
			" * Test bug 86769\n" +
			" */\n" +
			"public class A {\n" +
			"	private class InnerPrivate {\n" +
			"		private int pri_pri;\n" +
			"		int pri_def;\n" +
			"		protected int pri_pro;\n" +
			"		public int pri_pub;\n" +
			"	}\n" +
			"	class InnerDefault{\n" +
			"		private int def_pri;\n" +
			"		int def_def;\n" +
			"		protected int def_pro;\n" +
			"		public int def_pub;\n" +
			"	}\n" +
			"	protected class InnerProtected {\n" +
			"		private int pro_pri;\n" +
			"		int pro_def;\n" +
			"		protected int pro_pro;\n" +
			"		public int pro_pub; \n" +
			"	}\n" +
			"	public class InnerPublic {\n" +
			"		private int pub_pri;\n" +
			"		int pub_def;\n" +
			"		protected int pub_pro;\n" +
			"		public int pub_pub;\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in A.java (at line 23)\n" +
		"	public class InnerPublic {\n" +
		"	             ^^^^^^^^^^^\n" +
		"Javadoc: Missing comment for public declaration\n" +
		"----------\n" +
		"2. ERROR in A.java (at line 27)\n" +
		"	public int pub_pub;\n" +
		"	           ^^^^^^^\n" +
		"Javadoc: Missing comment for public declaration\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug86769_Fields2() {
	this.reportMissingJavadocComments = CompilerOptions.ERROR;
	this.reportMissingJavadocCommentsVisibility = CompilerOptions.PRIVATE;
	runNegativeTest(
		new String[] {
			"B.java",
			"/**\n" +
			" * Test bug 86769\n" +
			" */\n" +
			"public class B {\n" +
			"	private class Level1 {\n" +
			"		private class InnerPrivate {\n" +
			"			private int pri_pri;\n" +
			"			int pri_def;\n" +
			"			protected int pri_pro;\n" +
			"			public int pri_pub;\n" +
			"		}\n" +
			"		class InnerDefault{\n" +
			"			private int def_pri;\n" +
			"			int def_def;\n" +
			"			protected int def_pro;\n" +
			"			public int def_pub;\n" +
			"		}\n" +
			"		protected class InnerProtected {\n" +
			"			private int pro_pri;\n" +
			"			int pro_def;\n" +
			"			protected int pro_pro;\n" +
			"			public int pro_pub; \n" +
			"		}\n" +
			"		public class InnerPublic {\n" +
			"			private int pub_pri;\n" +
			"			int pub_def;\n" +
			"			protected int pub_pro;\n" +
			"			public int pub_pub;\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in B.java (at line 5)\n" +
		"	private class Level1 {\n" +
		"	              ^^^^^^\n" +
		"Javadoc: Missing comment for private declaration\n" +
		"----------\n" +
		"2. ERROR in B.java (at line 6)\n" +
		"	private class InnerPrivate {\n" +
		"	              ^^^^^^^^^^^^\n" +
		"Javadoc: Missing comment for private declaration\n" +
		"----------\n" +
		"3. ERROR in B.java (at line 7)\n" +
		"	private int pri_pri;\n" +
		"	            ^^^^^^^\n" +
		"Javadoc: Missing comment for private declaration\n" +
		"----------\n" +
		"4. ERROR in B.java (at line 8)\n" +
		"	int pri_def;\n" +
		"	    ^^^^^^^\n" +
		"Javadoc: Missing comment for private declaration\n" +
		"----------\n" +
		"5. ERROR in B.java (at line 9)\n" +
		"	protected int pri_pro;\n" +
		"	              ^^^^^^^\n" +
		"Javadoc: Missing comment for private declaration\n" +
		"----------\n" +
		"6. ERROR in B.java (at line 10)\n" +
		"	public int pri_pub;\n" +
		"	           ^^^^^^^\n" +
		"Javadoc: Missing comment for private declaration\n" +
		"----------\n" +
		"7. ERROR in B.java (at line 12)\n" +
		"	class InnerDefault{\n" +
		"	      ^^^^^^^^^^^^\n" +
		"Javadoc: Missing comment for private declaration\n" +
		"----------\n" +
		"8. ERROR in B.java (at line 13)\n" +
		"	private int def_pri;\n" +
		"	            ^^^^^^^\n" +
		"Javadoc: Missing comment for private declaration\n" +
		"----------\n" +
		"9. ERROR in B.java (at line 14)\n" +
		"	int def_def;\n" +
		"	    ^^^^^^^\n" +
		"Javadoc: Missing comment for private declaration\n" +
		"----------\n" +
		"10. ERROR in B.java (at line 15)\n" +
		"	protected int def_pro;\n" +
		"	              ^^^^^^^\n" +
		"Javadoc: Missing comment for private declaration\n" +
		"----------\n" +
		"11. ERROR in B.java (at line 16)\n" +
		"	public int def_pub;\n" +
		"	           ^^^^^^^\n" +
		"Javadoc: Missing comment for private declaration\n" +
		"----------\n" +
		"12. ERROR in B.java (at line 18)\n" +
		"	protected class InnerProtected {\n" +
		"	                ^^^^^^^^^^^^^^\n" +
		"Javadoc: Missing comment for private declaration\n" +
		"----------\n" +
		"13. ERROR in B.java (at line 19)\n" +
		"	private int pro_pri;\n" +
		"	            ^^^^^^^\n" +
		"Javadoc: Missing comment for private declaration\n" +
		"----------\n" +
		"14. ERROR in B.java (at line 20)\n" +
		"	int pro_def;\n" +
		"	    ^^^^^^^\n" +
		"Javadoc: Missing comment for private declaration\n" +
		"----------\n" +
		"15. ERROR in B.java (at line 21)\n" +
		"	protected int pro_pro;\n" +
		"	              ^^^^^^^\n" +
		"Javadoc: Missing comment for private declaration\n" +
		"----------\n" +
		"16. ERROR in B.java (at line 22)\n" +
		"	public int pro_pub; \n" +
		"	           ^^^^^^^\n" +
		"Javadoc: Missing comment for private declaration\n" +
		"----------\n" +
		"17. ERROR in B.java (at line 24)\n" +
		"	public class InnerPublic {\n" +
		"	             ^^^^^^^^^^^\n" +
		"Javadoc: Missing comment for private declaration\n" +
		"----------\n" +
		"18. ERROR in B.java (at line 25)\n" +
		"	private int pub_pri;\n" +
		"	            ^^^^^^^\n" +
		"Javadoc: Missing comment for private declaration\n" +
		"----------\n" +
		"19. ERROR in B.java (at line 26)\n" +
		"	int pub_def;\n" +
		"	    ^^^^^^^\n" +
		"Javadoc: Missing comment for private declaration\n" +
		"----------\n" +
		"20. ERROR in B.java (at line 27)\n" +
		"	protected int pub_pro;\n" +
		"	              ^^^^^^^\n" +
		"Javadoc: Missing comment for private declaration\n" +
		"----------\n" +
		"21. ERROR in B.java (at line 28)\n" +
		"	public int pub_pub;\n" +
		"	           ^^^^^^^\n" +
		"Javadoc: Missing comment for private declaration\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug86769_Metthods1() {
	this.reportMissingJavadocComments = CompilerOptions.ERROR;
	this.reportMissingJavadocCommentsVisibility = CompilerOptions.PUBLIC;
	runNegativeTest(
		new String[] {
			"A.java",
			"/**\n" +
			" * Test bug 86769\n" +
			" */\n" +
			"public class A {\n" +
			"	private class InnerPrivate {\n" +
			"		private void pri_pri() {}\n" +
			"		void pri_def() {}\n" +
			"		protected void pri_pro() {}\n" +
			"		public void pri_pub() {}\n" +
			"	}\n" +
			"	class InnerDefault{\n" +
			"		private void def_pri() {}\n" +
			"		void def_def() {}\n" +
			"		protected void def_pro() {}\n" +
			"		public void def_pub() {}\n" +
			"	}\n" +
			"	protected class InnerProtected {\n" +
			"		private void pro_pri() {}\n" +
			"		void pro_def() {}\n" +
			"		protected void pro_pro() {}\n" +
			"		public void pro_pub() {} \n" +
			"	}\n" +
			"	public class InnerPublic {\n" +
			"		private void pub_pri() {}\n" +
			"		void pub_def() {}\n" +
			"		protected void pub_pro() {}\n" +
			"		public void pub_pub() {}\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in A.java (at line 23)\n" +
		"	public class InnerPublic {\n" +
		"	             ^^^^^^^^^^^\n" +
		"Javadoc: Missing comment for public declaration\n" +
		"----------\n" +
		"2. ERROR in A.java (at line 27)\n" +
		"	public void pub_pub() {}\n" +
		"	            ^^^^^^^^^\n" +
		"Javadoc: Missing comment for public declaration\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug86769_Methods2() {
	this.reportMissingJavadocComments = CompilerOptions.ERROR;
	this.reportMissingJavadocCommentsVisibility = CompilerOptions.PROTECTED;
	runConformTest(
		new String[] {
			"B.java",
			"/**\n" +
			" * Test bug 86769\n" +
			" */\n" +
			"public class B {\n" +
			"	private class Level1 {\n" +
			"		private class InnerPrivate {\n" +
			"			private void pri_pri() {}\n" +
			"			void pri_def() {}\n" +
			"			protected void pri_pro() {}\n" +
			"			public void pri_pub() {}\n" +
			"		}\n" +
			"		class InnerDefault{\n" +
			"			private void def_pri() {}\n" +
			"			void def_def() {}\n" +
			"			protected void def_pro() {}\n" +
			"			public void def_pub() {}\n" +
			"		}\n" +
			"		protected class InnerProtected {\n" +
			"			private void pro_pri() {}\n" +
			"			void pro_def() {}\n" +
			"			protected void pro_pro() {}\n" +
			"			public void pro_pub() {} \n" +
			"		}\n" +
			"		public class InnerPublic {\n" +
			"			private void pub_pri() {}\n" +
			"			void pub_def() {}\n" +
			"			protected void pub_pro() {}\n" +
			"			public void pub_pub() {}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		}
	);
}

/**
 * Bug 87404: [javadoc] Unexpected not defined warning on constructor
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=87404"
 */
public void testBug87404() {
	runConformTest(
		new String[] {
			"p/A.java",
			"package p;\n" +
			"class A {\n" +
			"	A(char c) {}\n" +
			"	class B {\n" +
			"		B(Exception ex) {}\n" +
			"	}\n" +
			"	void foo() {}\n" +
			"    /**\n" +
			"     * Link {@link #A(char)} OK \n" +
			"     * Link {@link #A(String)} OK\n" +
			"     * Link {@link #foo()} OK\n" +
			"     * Link {@link #bar()} OK\n" +
			"     */\n" +
			"    public A(String str) {}\n" +
			"	void bar() {}\n" +
			"}"
		}
	);
}

/**
 * Bug 90302: [javadoc] {&#064;inheritDoc} should be inactive for non-overridden method
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=90302"
 */
public void testBug90302() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"/**\n" +
			" * @see #foo(String)\n" +
			" */\n" +
			"public class X {\n" +
			"	/**\n" +
			"	 * Static method\n" +
			"	 * @param str\n" +
			"	 * @return int\n" +
			"	 * @throws NumberFormatException\n" +
			"	 */\n" +
			"	static int foo(String str) throws NumberFormatException{\n" +
			"		return Integer.parseInt(str);\n" +
			"	}\n" +
			"}\n",
			"Y.java",
			"/**\n" +
			" * @see #foo(String)\n" +
			" */\n" +
			"public class Y extends X { \n" +
			"	/**\n" +
			"	 * Static method: does not override super\n" +
			"	 * {@inheritDoc}\n" +
			"	 */\n" +
			"	static int foo(String str) throws NumberFormatException{\n" +
			"		return Integer.parseInt(str);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Y.java (at line 7)\n" +
		"	* {@inheritDoc}\n" +
		"	    ^^^^^^^^^^\n" +
		"Javadoc: Unexpected tag\n" +
		"----------\n" +
		"2. ERROR in Y.java (at line 9)\n" +
		"	static int foo(String str) throws NumberFormatException{\n" +
		"	       ^^^\n" +
		"Javadoc: Missing tag for return type\n" +
		"----------\n" +
		"3. ERROR in Y.java (at line 9)\n" +
		"	static int foo(String str) throws NumberFormatException{\n" +
		"	                      ^^^\n" +
		"Javadoc: Missing tag for parameter str\n" +
		"----------\n" +
		"4. ERROR in Y.java (at line 9)\n" +
		"	static int foo(String str) throws NumberFormatException{\n" +
		"	                                  ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: Missing tag for declared exception NumberFormatException\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug90302b() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"/** */\n" +
			"public class X {\n" +
			"}\n",
			"Y.java",
			"/**\n" +
			" * @see #foo(String)\n" +
			" */\n" +
			"public class Y extends X { \n" +
			"	/**\n" +
			"	 * Simple method: does not override super\n" +
			"	 * {@inheritDoc}\n" +
			"	 */\n" +
			"	static int foo(String str) throws NumberFormatException{\n" +
			"		return Integer.parseInt(str);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Y.java (at line 7)\n" +
		"	* {@inheritDoc}\n" +
		"	    ^^^^^^^^^^\n" +
		"Javadoc: Unexpected tag\n" +
		"----------\n" +
		"2. ERROR in Y.java (at line 9)\n" +
		"	static int foo(String str) throws NumberFormatException{\n" +
		"	       ^^^\n" +
		"Javadoc: Missing tag for return type\n" +
		"----------\n" +
		"3. ERROR in Y.java (at line 9)\n" +
		"	static int foo(String str) throws NumberFormatException{\n" +
		"	                      ^^^\n" +
		"Javadoc: Missing tag for parameter str\n" +
		"----------\n" +
		"4. ERROR in Y.java (at line 9)\n" +
		"	static int foo(String str) throws NumberFormatException{\n" +
		"	                                  ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: Missing tag for declared exception NumberFormatException\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 103304: [Javadoc] Wrong reference proposal for inner classes.
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=103304"
 */
public void testBug103304a_public() {
	String[] units = new String[] {
			"boden/IAFAState.java",
			"package boden;\n" +
			"public interface IAFAState {\n" +
			"    public class ValidationException extends Exception {\n" +
			"        public ValidationException(String variableName, IAFAState subformula) {\n" +
			"            super(\"Variable \'\"+variableName+\"\' may be unbound in \'\"+subformula+\"\'\");\n" +
			"        }\n" +
			"        public void method() {}\n" +
			"    }\n" +
			"    /**\n" +
			"     * Validates a formula for consistent bindings. Bindings are consistent, when at each point in time,\n" +
			"     * the set of povided variables can be guaranteed to be a superset of the set of required variables.\n" +
			"     * @throws ValidationException Thrown if a variable is unbound. \n" +
			"     * @see ValidationException#IAFAState.ValidationException(String, IAFAState)\n" +
			"     * @see IAFAState.ValidationException#method()\n" +
			"     * @see ValidationException\n" +
			"     * {@link ValidationException}\n" +
			"     */\n" +
			"    public void validate() throws ValidationException;\n" +
			"}\n",
			"boden/TestValid.java",
			"package boden;\n" +
			"import boden.IAFAState.ValidationException;\n" +
			"/**\n" +
			" * @see ValidationException\n" +
			" * @see IAFAState.ValidationException\n" +
			" */\n" +
			"public class TestValid {\n" +
			"	/**  \n" +
			"	 * @see ValidationException#IAFAState.ValidationException(String, IAFAState)\n" +
			"	 */\n" +
			"	IAFAState.ValidationException valid1;\n" +
			"	/**\n" +
			"	 * @see IAFAState.ValidationException#IAFAState.ValidationException(String, IAFAState)\n" +
			"	 */\n" +
			"	IAFAState.ValidationException valid2;\n" +
			"}\n"
		};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(units,
			//boden\TestValid.java:8: warning - Tag @see: reference not found: ValidationException
			"----------\n" +
			"1. ERROR in boden\\TestValid.java (at line 4)\n" +
			"	* @see ValidationException\n" +
			"	       ^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n" +
			"2. ERROR in boden\\TestValid.java (at line 9)\n" +
			"	* @see ValidationException#IAFAState.ValidationException(String, IAFAState)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	} else {
		runConformTest(units);
	}
}

public void testBug103304a_private() {
	String[] units = new String[] {
			"boden/IAFAState.java",
			"package boden;\n" +
			"public interface IAFAState {\n" +
			"    public class ValidationException extends Exception {\n" +
			"        public ValidationException(String variableName, IAFAState subformula) {\n" +
			"            super(\"Variable \'\"+variableName+\"\' may be unbound in \'\"+subformula+\"\'\");\n" +
			"        }\n" +
			"        public void method() {}\n" +
			"    }\n" +
			"    /**\n" +
			"     * Validates a formula for consistent bindings. Bindings are consistent, when at each point in time,\n" +
			"     * the set of povided variables can be guaranteed to be a superset of the set of required variables.\n" +
			"     * @throws ValidationException Thrown if a variable is unbound. \n" +
			"     * @see ValidationException#IAFAState.ValidationException(String, IAFAState)\n" +
			"     * @see IAFAState.ValidationException#method()\n" +
			"     * @see ValidationException\n" +
			"     * {@link ValidationException}\n" +
			"     */\n" +
			"    public void validate() throws ValidationException;\n" +
			"}\n",
			"boden/TestValid.java",
			"package boden;\n" +
			"import boden.IAFAState.ValidationException;\n" +
			"/**\n" +
			" * @see ValidationException\n" +
			" * @see IAFAState.ValidationException\n" +
			" */\n" +
			"public class TestValid {\n" +
			"	/**  \n" +
			"	 * @see ValidationException#IAFAState.ValidationException(String, IAFAState)\n" +
			"	 */\n" +
			"	IAFAState.ValidationException valid1;\n" +
			"	/**\n" +
			"	 * @see IAFAState.ValidationException#IAFAState.ValidationException(String, IAFAState)\n" +
			"	 */\n" +
			"	IAFAState.ValidationException valid2;\n" +
			"}\n"
		};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(units,
			//boden\TestValid.java:8: warning - Tag @see: reference not found: ValidationException
			//boden\TestValid.java:12: warning - Tag @see: reference not found: ValidationException#IAFAState.ValidationException(String, IAFAState)
			"----------\n" +
			"1. ERROR in boden\\TestValid.java (at line 4)\n" +
			"	* @see ValidationException\n" +
			"	       ^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n" +
			"2. ERROR in boden\\TestValid.java (at line 9)\n" +
			"	* @see ValidationException#IAFAState.ValidationException(String, IAFAState)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	} else {
		runConformTest(units);
	}
}

public void testBug103304b() {
	this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
	String[] units = new String[] {
			"boden/IAFAState.java",
			"package boden;\n" +
			"public interface IAFAState {\n" +
			"    public class ValidationException extends Exception {\n" +
			"        public ValidationException(String variableName, IAFAState subformula) {\n" +
			"            super(\"Variable \'\"+variableName+\"\' may be unbound in \'\"+subformula+\"\'\");\n" +
			"        }\n" +
			"        public void method() {}\n" +
			"    }\n" +
			"}\n",
			"boden/TestInvalid1.java",
			"package boden;\n" +
			"import boden.IAFAState.ValidationException;\n" +
			"public class TestInvalid1 {\n" +
			"	/** \n" +
			"	 * @see ValidationException#ValidationException(String, IAFAState)\n" +
			"	 * @see ValidationException#IAFAState.ValidationException(String, IAFAState)\n" +
			"	 */ \n" +
			"	IAFAState.ValidationException invalid;\n" +
			"}\n",
			"boden/TestInvalid2.java",
			"package boden;\n" +
			"public class TestInvalid2 {\n" +
			"	/**\n" +
			"	 * @see IAFAState.ValidationException#ValidationException(String, IAFAState)\n" +
			"	 */\n" +
			"	IAFAState.ValidationException invalid;\n" +
			"}\n",
			"boden/TestInvalid3.java",
			"package boden;\n" +
			"import boden.IAFAState.ValidationException;\n" +
			"public class TestInvalid3 {\n" +
			"	/**\n" +
			"	 * @see IAFAState.ValidationException#IAFA.State.ValidationException(String, IAFAState)\n" +
			"	 */\n" +
			"	IAFAState.ValidationException invalid;\n" +
			"}\n",
			"boden/TestInvalid4.java",
			"package boden;\n" +
			"import boden.IAFAState.ValidationException;\n" +
			"public class TestInvalid4 {\n" +
			"	/**\n" +
			"	 * @see IAFAState.ValidationException#IAFAState .ValidationException(String, IAFAState)\n" +
			"	 */\n" +
			"	IAFAState.ValidationException invalid;\n" +
			"}\n"
		};
	String errors_14 = new String (
			//boden\TestInvalid1.java:7: warning - Tag @see: reference not found: ValidationException#ValidationException(String, IAFAState)
			//boden\TestInvalid1.java:8: warning - Tag @see: reference not found: ValidationException#IAFAState.ValidationException(String, IAFAState)
			//boden\TestInvalid2.java:6: warning - Tag @see: can't find ValidationException(String, IAFAState) in boden.IAFAState.ValidationException => bug ID: 4288720
			//boden\TestInvalid3.java:6: warning - Tag @see: can't find IAFA.State.ValidationException(String, IAFAState) in boden.IAFAState.ValidationException
			//boden\TestInvalid4.java:6: warning - Tag @see: can't find IAFAState in boden.IAFAState.ValidationException
			"----------\n" +
			"1. ERROR in boden\\TestInvalid1.java (at line 5)\n" +
			"	* @see ValidationException#ValidationException(String, IAFAState)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n" +
			"2. ERROR in boden\\TestInvalid1.java (at line 6)\n" +
			"	* @see ValidationException#IAFAState.ValidationException(String, IAFAState)\n" +
			"	       ^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in boden\\TestInvalid3.java (at line 2)\n"+
			"	import boden.IAFAState.ValidationException;\n"+
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The import boden.IAFAState.ValidationException is never used\n"+
			"----------\n"+
			"2. ERROR in boden\\TestInvalid3.java (at line 5)\n" +
			"	* @see IAFAState.ValidationException#IAFA.State.ValidationException(String, IAFAState)\n" +
			"	                                     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in boden\\TestInvalid4.java (at line 2)\n"+
			"	import boden.IAFAState.ValidationException;\n"+
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The import boden.IAFAState.ValidationException is never used\n"+
			"----------\n"+
			"2. ERROR in boden\\TestInvalid4.java (at line 5)\n" +
			"	* @see IAFAState.ValidationException#IAFAState .ValidationException(String, IAFAState)\n" +
			"	                                     ^^^^^^^^^\n" +
			"Javadoc: IAFAState cannot be resolved or is not a field\n" +
			"----------\n"
	);
	String errors_50 = new String (
			//boden\TestInvalid1.java:7: warning - Tag @see: reference not found: ValidationException#ValidationException(String, IAFAState)
			//boden\TestInvalid2.java:6: warning - Tag @see: can't find ValidationException(String, IAFAState) in boden.IAFAState.ValidationException => bug ID: 4288720
			//boden\TestInvalid3.java:6: warning - Tag @see: can't find IAFA.State.ValidationException(String, IAFAState) in boden.IAFAState.ValidationException
			//boden\TestInvalid4.java:6: warning - Tag @see: can't find IAFAState in boden.IAFAState.ValidationException
			"----------\n" +
			"1. ERROR in boden\\TestInvalid3.java (at line 2)\n"+
			"	import boden.IAFAState.ValidationException;\n"+
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The import boden.IAFAState.ValidationException is never used\n"+
			"----------\n"+
			"2. ERROR in boden\\TestInvalid3.java (at line 5)\n" +
			"	* @see IAFAState.ValidationException#IAFA.State.ValidationException(String, IAFAState)\n" +
			"	                                     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in boden\\TestInvalid4.java (at line 2)\n"+
			"	import boden.IAFAState.ValidationException;\n"+
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The import boden.IAFAState.ValidationException is never used\n"+
			"----------\n"+
			"2. ERROR in boden\\TestInvalid4.java (at line 5)\n" +
			"	* @see IAFAState.ValidationException#IAFAState .ValidationException(String, IAFAState)\n" +
			"	                                     ^^^^^^^^^\n" +
			"Javadoc: IAFAState cannot be resolved or is not a field\n" +
			"----------\n"
	);
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(units, errors_14);
	} else {
		runNegativeTest(units, errors_50, JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

}
public void testBug103304c() {
	runConformTest(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"public interface Test {\n" +
			"	public class Level0 {\n" +
			"		public Level0() {}\n" +
			"	}\n" +
			"	public interface Member {\n" +
			"		public class Level1 {\n" +
			"			public Level1() {}\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
			"test/C.java",
			"package test;\n" +
			"public class C {\n" +
			"	/**\n" +
			"	 * @see Test.Level0#Test.Level0()\n" +
			"	 */\n" +
			"	Test.Level0 valid = new Test.Level0();\n" +
			"	/**\n" +
			"	 * @see Test.Level0#Level0()\n" +
			"	 */\n" +
			"	Test.Level0 invalid = new Test.Level0();\n" +
			"}\n"
		}
		//test\C.java:10: warning - Tag @see: can't find Level0() in test.Test.Level0 => bug ID: 4288720
	);
}
public void testBug103304d() {
	runNegativeTest(
		new String[] {
			"test/Test.java",
			"package test;\n" +
			"public interface Test {\n" +
			"	public class Level0 {\n" +
			"		public Level0() {}\n" +
			"	}\n" +
			"	public interface Member {\n" +
			"		public class Level1 {\n" +
			"			public Level1() {}\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
			"test/C2.java",
			"package test;\n" +
			"public class C2 {\n" +
			"	/**\n" +
			"	 * @see Test.Member.Level1#Test.Member.Level1()\n" +
			"	 */\n" +
			"	Test.Member.Level1 valid = new Test.Member.Level1();\n" +
			"	/**\n" +
			"	 * @see Test.Member.Level1#Level1()\n" +
			"	 */\n" +
			"	Test.Member.Level1 invalid = new Test.Member.Level1();\n" +
			"	/**\n" +
			"	 * @see Test.Member.Level1#Test.Level1()\n" +
			"	 */\n" +
			"	Test.Member.Level1 wrong = new Test.Member.Level1();\n" +
			"}\n"
		},
		//test\C2.java:10: warning - Tag @see: can't find Level1() in test.Test.Member.Level1 => Bug ID: 4288720
		//test\C2.java:14: warning - Tag @see: can't find Test.Level1() in test.Test.Member.Level1
		"----------\n" +
		"1. ERROR in test\\C2.java (at line 12)\n" +
		"	* @see Test.Member.Level1#Test.Level1()\n" +
		"	                          ^^^^^^^^^^^^^\n" +
		"Javadoc: Invalid member type qualification\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug103304e() {
	runConformTest(
		new String[] {
			"implicit/Valid.java",
			"package implicit;\n" +
			"public interface Valid {\n" +
			"	public class Level0 {\n" +
			"		/**\n" +
			"		 * @see #Valid.Level0() Valid\n" +
			"		 */\n" +
			"		public Level0() {}\n" +
			"		/**\n" +
			"		 * @see #Valid.Level0(String) Valid\n" +
			"		 */\n" +
			"		public Level0(String str) {}\n" +
			"	}\n" +
			"	public interface Member {\n" +
			"		public class Level1 {\n" +
			"			/**\n" +
			"			 * @see #Valid.Member.Level1() Valid\n" +
			"			 */\n" +
			"			public Level1() {}\n" +
			"			/**\n" +
			"			 * @see #Valid.Member.Level1(int) Valid\n" +
			"			 */\n" +
			"			public Level1(int x) {}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		}
	);
}
public void testBug103304f() {
	runNegativeTest(
		new String[] {
			"implicit/Invalid.java",
			"package implicit;\n" +
			"public interface Invalid {\n" +
			"	public class Level0 {\n" +
			"		/**\n" +
			"		 * @see #Level0() Invalid\n" +
			"		 */\n" +
			"		public Level0() {}\n" +
			"		/**\n" +
			"		 * @see #Level0(String) Invalid\n" +
			"		 */\n" +
			"		public Level0(String str) {}\n" +
			"	}\n" +
			"	public interface Member {\n" +
			"		public class Level1 {\n" +
			"			/**\n" +
			"			 * @see #Level1() Invalid\n" +
			"			 * @see #Member.Level1() Invalid\n" +
			"			 * @see #Invalid.Level1() Invalid\n" +
			"			 */\n" +
			"			public Level1() {}\n" +
			"			/**\n" +
			"			 * @see #Level1(int) Invalid\n" +
			"			 * @see #Invalid.Level1(int) Invalid\n" +
			"			 * @see #Member.Level1(int) Invalid\n" +
			"			 */\n" +
			"			public Level1(int x) {}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		//implicit\Invalid.java:7: warning - Tag @see: can't find Level0() in implicit.Invalid.Level0 => bug ID: 4288720
		//implicit\Invalid.java:11: warning - Tag @see: can't find Level0(String) in implicit.Invalid.Level0 => bug ID: 4288720
		//implicit\Invalid.java:20: warning - Tag @see: can't find Level1() in implicit.Invalid.Member.Level1 => bug ID: 4288720
		//implicit\Invalid.java:20: warning - Tag @see: can't find Member.Level1() in implicit.Invalid.Member.Level1
		//implicit\Invalid.java:20: warning - Tag @see: can't find Invalid.Level1() in implicit.Invalid.Member.Level1
		//implicit\Invalid.java:26: warning - Tag @see: can't find Level1(int) in implicit.Invalid.Member.Level1 => bug ID: 4288720
		//implicit\Invalid.java:26: warning - Tag @see: can't find Invalid.Level1(int) in implicit.Invalid.Member.Level1
		//implicit\Invalid.java:26: warning - Tag @see: can't find Member.Level1(int) in implicit.Invalid.Member.Level1
		"----------\n" +
		"1. ERROR in implicit\\Invalid.java (at line 17)\n" +
		"	* @see #Member.Level1() Invalid\n" +
		"	        ^^^^^^^^^^^^^^^\n" +
		"Javadoc: Invalid member type qualification\n" +
		"----------\n" +
		"2. ERROR in implicit\\Invalid.java (at line 18)\n" +
		"	* @see #Invalid.Level1() Invalid\n" +
		"	        ^^^^^^^^^^^^^^^^\n" +
		"Javadoc: Invalid member type qualification\n" +
		"----------\n" +
		"3. ERROR in implicit\\Invalid.java (at line 23)\n" +
		"	* @see #Invalid.Level1(int) Invalid\n" +
		"	        ^^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: Invalid member type qualification\n" +
		"----------\n" +
		"4. ERROR in implicit\\Invalid.java (at line 24)\n" +
		"	* @see #Member.Level1(int) Invalid\n" +
		"	        ^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: Invalid member type qualification\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 116464: [javadoc] Unicode tag name are not correctly parsed
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=116464"
 */
public void testBug116464() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	/**\n" +
			"	 * @\\u0070\\u0061\\u0072\\u0061\\u006d str xxx\n" +
			"	 */\n" +
			"	void foo(String str) {}\n" +
			"}\n"
		}
	);
}

/**
 * bug 125518: [javadoc] Embedding html in a link placed in a @see JavaDoc tag causes a warning
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=125518"
 */
public void testBug125518a() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X {\n" +
		"	/**\n" +
		"	 * @see <a href=\"ccwww.xyzzy.com/rfc123.html\">invalid></\n" +
		"	 */\n" +
		"	public void foo() { \n" +
		"	 \n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(true, units,
			"----------\n" +
			"1. WARNING in pkg\\X.java (at line 5)\n" +
			"	* @see <a href=\"ccwww.xyzzy.com/rfc123.html\">invalid></\n" +
			"	                                                     ^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

public void testBug125518b() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X {\n" +
		"	/**\n" +
		"	 * @see <a href=\"ccwww.xyzzy.com/rfc123.html\">invalid></a\n" +
		"	 */\n" +
		"	public void foo() { \n" +
		"	 \n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(true, units,
			"----------\n" +
			"1. WARNING in pkg\\X.java (at line 5)\n" +
			"	* @see <a href=\"ccwww.xyzzy.com/rfc123.html\">invalid></a\n" +
			"	                                                     ^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

public void testBug125518c() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X {\n" +
		"	/**\n" +
		"	 * @see <a href=\"ccwww.xyzzy.com/rfc123.html\">invalid></>\n" +
		"	 */\n" +
		"	public void foo() { \n" +
		"	 \n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(true, units,
			"----------\n" +
			"1. WARNING in pkg\\X.java (at line 5)\n" +
			"	* @see <a href=\"ccwww.xyzzy.com/rfc123.html\">invalid></>\n" +
			"	                                                     ^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

public void testBug125518d() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X {\n" +
		"	/**\n" +
		"	 * @see <a href=\"ccwww.xyzzy.com/rfc123.html\">invalid></aa>\n" +
		"	 */\n" +
		"	public void foo() { \n" +
		"	 \n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(true, units,
			"----------\n" +
			"1. WARNING in pkg\\X.java (at line 5)\n" +
			"	* @see <a href=\"ccwww.xyzzy.com/rfc123.html\">invalid></aa>\n" +
			"	                                                     ^^^^^\n" +
			"Javadoc: Malformed link reference\n" +
			"----------\n",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

public void testBug125518e() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X {\n" +
		"	/**\n" +
		"	 * @see <a href=\"http\u003A\u002F\u002Fwww.eclipse.org\"><valid>value</valid></a>\n" +
		"	 */\n" +
		"	public void foo() { \n" +
		"	 \n" +
		"	}\n" +
		"}\n"
		};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(units);
}

/**
 * Bug 125903: [javadoc] Treat whitespace in javadoc tags as invalid tags
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=125903"
 */
public void testBug125903() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"/**\n" +
			" * {@ link java.lang.String}\n" +
			" * @ since 2.1\n" +
			" */\n" +
			"public class X {\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	* {@ link java.lang.String}\n" +
		"	   ^^\n" +
		"Javadoc: Invalid tag\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	* @ since 2.1\n" +
		"	  ^^\n" +
		"Javadoc: Invalid tag\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 128954: Javadoc problems with category CAT_INTERNAL
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=128954"
 */
public void testBug128954() {
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	this.reportDeprecation = CompilerOptions.WARNING;
	runNegativeTest(
		new String[] {
			"X.java", //========================
			"public class X {\n" +
			"	/**\n" +
			"	 * @see p.A#bar()\n" +
			"	 */\n" +
			"	void foo() {\n" +
			"		Zork z;\n" +
			"	}\n" +
			"}\n",
			"p/A.java",  //========================
			"package p;\n" +
			"public class A {\n" +
			"	/** @deprecated */\n" +
			"	public void bar() {\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	* @see p.A#bar()\n" +
		"	           ^^^^^\n" +
		"[@cat:javadoc] [@sup:javadoc] Javadoc: The method bar() from the type A is deprecated\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"[@cat:type] Zork cannot be resolved to a type\n" +
		"----------\n",
		null,
		true,
		null,
		false,
		true,
		true);
}

/**
 * Bug 128954: Javadoc problems with category CAT_INTERNAL - variation
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=128954"
 */
public void testBug128954a() {
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	this.reportDeprecation = CompilerOptions.WARNING;
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	\n" +
			"	/**\n" +
			"	 * @see p.A#bar()\n" +
			"	 */\n" +
			"	void foo() {\n" +
			"		Zork z;\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	* @see p.A#bar()\n" +
		"	       ^^^\n" +
		"[@cat:javadoc] [@sup:javadoc] Javadoc: p cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	Zork z;\n" +
		"	^^^^\n" +
		"[@cat:type] Zork cannot be resolved to a type\n" +
		"----------\n",
		null,
		true,
		null,
		false,
		true,
		true);
}

/**
 * Bug 129241: [Javadoc] deprecation warning wrongly reported when ignoring Malformed Javadoc comments
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=129241"
 */
public void testBug129241a() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	/**\n" +
			"	 * @see p.A#bar\n" +
			"	 */\n" +
			"	void foo() {}\n" +
			"}\n",
			"p/A.java",
			"package p;\n" +
			"/** @deprecated */\n" +
			"public class A {\n" +
			"	void bar() {}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	* @see p.A#bar\n" +
		"	       ^^^\n" +
		"Javadoc: The type A is deprecated\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug129241b() {
	this.reportDeprecation = CompilerOptions.IGNORE;
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	/**\n" +
			"	 * @see p.A#bar\n" +
			"	 */\n" +
			"	void foo() {}\n" +
			"}\n",
			"p/A.java",
			"package p;\n" +
			"/** @deprecated */\n" +
			"public class A {\n" +
			"	void bar() {}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	* @see p.A#bar\n" +
		"	       ^^^\n" +
		"Javadoc: The type A is deprecated\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug129241c() {
	this.reportJavadocDeprecation = CompilerOptions.DISABLED;
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	/**\n" +
			"	 * @see p.A#bar\n" +
			"	 */\n" +
			"	void foo() {}\n" +
			"}\n",
			"p/A.java",
			"package p;\n" +
			"/** @deprecated */\n" +
			"public class A {\n" +
			"	void bar() {}\n" +
			"}\n"
		}
	);
}
public void testBug129241d() {
	this.reportInvalidJavadoc = CompilerOptions.IGNORE;
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	/**\n" +
			"	 * @see p.A#bar\n" +
			"	 */\n" +
			"	void foo() {}\n" +
			"}\n",
			"p/A.java",
			"package p;\n" +
			"/** @deprecated */\n" +
			"public class A {\n" +
			"	void bar() {}\n" +
			"}\n"
		}
	);
}

/**
 * Bug 132813: NPE in Javadoc.resolve(Javadoc.java:196) + log swamped
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=132813"
 */
public void testBug132813() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"public class X { \n" +
			"	/**	 */ \n" +
			"	public Test() {}\n" +
			"	/**	 */\n" +
			"	public test() {}\n" +
			"}\n"			},
		"----------\n" +
		"1. ERROR in Test.java (at line 1)\n" +
		"	public class X { \n" +
		"	             ^\n" +
		"The public type X must be defined in its own file\n" +
		"----------\n" +
		"2. ERROR in Test.java (at line 3)\n" +
		"	public Test() {}\n" +
		"	       ^^^^^^\n" +
		"Return type for the method is missing\n" +
		"----------\n" +
		"3. ERROR in Test.java (at line 5)\n" +
		"	public test() {}\n" +
		"	       ^^^^^^\n" +
		"Return type for the method is missing\n" +
		"----------\n"
	);
}

/**
 * Bug 149013: [javadoc] In latest 3.3 build, there is a javadoc error in org.eclipse.core.resources
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=149013"
 */
public void testBug149013_Private01() {
	this.reportMissingJavadocTags = CompilerOptions.IGNORE;
	runConformTest(
		new String[] {
			"test1/X.java",
			"package test1;\n" +
			"public class X {\n" +
			"	class Inner {\n" +
			"		class Level2 {\n" +
			"			class Level3 {}\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
			"test1/Test.java",
			"package test1;\n" +
			"/**\n" +
			" * @see X.Inner\n" +
			" * @see X.Inner.Level2\n" +
			" * @see X.Inner.Level2.Level3\n" +
			" */\n" +
			"public class Test {}\n",
		}
	);
}
public void testBug149013_Public01() {
	this.reportMissingJavadocTags = CompilerOptions.DISABLED;
	this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
	runNegativeTest(
		new String[] {
			"test1/X.java",
			"package test1;\n" +
			"public class X {\n" +
			"	class Inner {\n" +
			"		class Level2 {\n" +
			"			class Level3 {}\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
			"test1/Test.java",
			"package test1;\n" +
			"/**\n" +
			" * @see X.Inner\n" +
			" * @see X.Inner.Level2\n" +
			" * @see X.Inner.Level2.Level3\n" +
			" */\n" +
			"public class Test {\n" +
			"}\n"
		},
		//test1\Test.java:7: warning - Tag @see: reference not found: X.Inner
		//test1\Test.java:7: warning - Tag @see: reference not found: X.Inner.Level2
		//test1\Test.java:7: warning - Tag @see: reference not found: X.Inner.Level2.Level3
		"----------\n" +
		"1. ERROR in test1\\Test.java (at line 3)\n" +
		"	* @see X.Inner\n" +
		"	       ^^^^^^^\n" +
		"Javadoc: \'public\' visibility for malformed doc comments hides this \'default\' reference\n" +
		"----------\n" +
		"2. ERROR in test1\\Test.java (at line 4)\n" +
		"	* @see X.Inner.Level2\n" +
		"	       ^^^^^^^^^^^^^^\n" +
		"Javadoc: \'public\' visibility for malformed doc comments hides this \'default\' reference\n" +
		"----------\n" +
		"3. ERROR in test1\\Test.java (at line 5)\n" +
		"	* @see X.Inner.Level2.Level3\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: \'public\' visibility for malformed doc comments hides this \'default\' reference\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug149013_Private02() {
	this.reportMissingJavadocTags = CompilerOptions.IGNORE;
	runNegativeTest(
		new String[] {
			"test1/X.java",
			"package test1;\n" +
			"public class X {\n" +
			"	class Inner {\n" +
			"		class Level2 {\n" +
			"			class Level3 {}\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
			"test2/Test.java",
			"package test2;\n" +
			"import test1.X;\n" +
			"/**\n" +
			" * @see X.Inner\n" +
			" * @see X.Inner.Level2\n" +
			" * @see X.Inner.Level2.Level3\n" +
			" */\n" +
			"public class Test {}\n",
		},
		//test2\Test.java:10: warning - Tag @see: reference not found: X.Inner
		//test2\Test.java:10: warning - Tag @see: reference not found: X.Inner.Level2
		//test2\Test.java:10: warning - Tag @see: reference not found: X.Inner.Level2.Level3
		"----------\n" +
		"1. ERROR in test2\\Test.java (at line 4)\r\n" +
		"	* @see X.Inner\r\n" +
		"	       ^^^^^^^\n" +
		"Javadoc: The type X.Inner is not visible\n" +
		"----------\n" +
		"2. ERROR in test2\\Test.java (at line 5)\r\n" +
		"	* @see X.Inner.Level2\r\n" +
		"	       ^^^^^^^^^^^^^^\n" +
		"Javadoc: The type X.Inner is not visible\n" +
		"----------\n" +
		"3. ERROR in test2\\Test.java (at line 6)\r\n" +
		"	* @see X.Inner.Level2.Level3\r\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: The type X.Inner is not visible\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug149013_Public02() {
	this.reportMissingJavadocTags = CompilerOptions.DISABLED;
	this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
	runNegativeTest(
		new String[] {
			"test1/X.java",
			"package test1;\n" +
			"public class X {\n" +
			"	class Inner {\n" +
			"		class Level2 {\n" +
			"			class Level3 {}\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
			"test2/Test.java",
			"package test2;\n" +
			"import test1.X;\n" +
			"/**\n" +
			" * @see X.Inner\n" +
			" * @see X.Inner.Level2\n" +
			" * @see X.Inner.Level2.Level3\n" +
			" */\n" +
			"public class Test {}\n",
		},
		//test2\Test.java:10: warning - Tag @see: reference not found: X.Inner
		//test2\Test.java:10: warning - Tag @see: reference not found: X.Inner.Level2
		//test2\Test.java:10: warning - Tag @see: reference not found: X.Inner.Level2.Level3
		"----------\n" +
		"1. ERROR in test2\\Test.java (at line 4)\r\n" +
		"	* @see X.Inner\r\n" +
		"	       ^^^^^^^\n" +
		"Javadoc: The type X.Inner is not visible\n" +
		"----------\n" +
		"2. ERROR in test2\\Test.java (at line 5)\r\n" +
		"	* @see X.Inner.Level2\r\n" +
		"	       ^^^^^^^^^^^^^^\n" +
		"Javadoc: The type X.Inner is not visible\n" +
		"----------\n" +
		"3. ERROR in test2\\Test.java (at line 6)\r\n" +
		"	* @see X.Inner.Level2.Level3\r\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: The type X.Inner is not visible\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug149013_Private03() {
	this.reportMissingJavadocTags = CompilerOptions.IGNORE;
	runNegativeTest(
		new String[] {
			"test1/X.java",
			"package test1;\n" +
			"public class X {\n" +
			"	class Inner {\n" +
			"		class Level2 {\n" +
			"			class Level3 {}\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
			"test3/Test.java",
			"package test3;\n" +
			"/**\n" +
			" * @see test1.X.Inner\n" +
			" * @see test1.X.Inner.Level2\n" +
			" * @see test1.X.Inner.Level2.Level3\n" +
			" */\n" +
			"public class Test {}\n"
		},
		// no warning
		"----------\n" +
		"1. ERROR in test3\\Test.java (at line 3)\r\n" +
		"	* @see test1.X.Inner\r\n" +
		"	       ^^^^^^^^^^^^^\n" +
		"Javadoc: The type test1.X.Inner is not visible\n" +
		"----------\n" +
		"2. ERROR in test3\\Test.java (at line 4)\r\n" +
		"	* @see test1.X.Inner.Level2\r\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: The type test1.X.Inner is not visible\n" +
		"----------\n" +
		"3. ERROR in test3\\Test.java (at line 5)\r\n" +
		"	* @see test1.X.Inner.Level2.Level3\r\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: The type test1.X.Inner is not visible\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug149013_Public03() {
	this.reportMissingJavadocTags = CompilerOptions.DISABLED;
	this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
	runNegativeTest(
		new String[] {
			"test1/X.java",
			"package test1;\n" +
			"public class X {\n" +
			"	class Inner {\n" +
			"		class Level2 {\n" +
			"			class Level3 {}\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
			"test3/Test.java",
			"package test3;\n" +
			"/**\n" +
			" * @see test1.X.Inner\n" +
			" * @see test1.X.Inner.Level2\n" +
			" * @see test1.X.Inner.Level2.Level3\n" +
			" */\n" +
			"public class Test {}\n"
		},
		// no warning
		"----------\n" +
		"1. ERROR in test3\\Test.java (at line 3)\r\n" +
		"	* @see test1.X.Inner\r\n" +
		"	       ^^^^^^^^^^^^^\n" +
		"Javadoc: The type test1.X.Inner is not visible\n" +
		"----------\n" +
		"2. ERROR in test3\\Test.java (at line 4)\r\n" +
		"	* @see test1.X.Inner.Level2\r\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: The type test1.X.Inner is not visible\n" +
		"----------\n" +
		"3. ERROR in test3\\Test.java (at line 5)\r\n" +
		"	* @see test1.X.Inner.Level2.Level3\r\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: The type test1.X.Inner is not visible\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * bug 153399: [javadoc] JDT Core should warn if the @value tag is not used correctly
 * test Ensure that 'value' tag is well warned when not used correctly
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=153399"
 */
public void testBug153399a() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X { \n" +
		"	/**\n" +
		"	 * {@value #MY_VALUE}\n" +
		"	 */\n" +
		"	public final static int MY_VALUE = 0; \n" +
		"	/**\n" +
		"	 * {@value #MY_VALUE}\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"	/**\n" +
		"	 * {@value #MY_VALUE}\n" +
		"	 */\n" +
		"	class Sub {} \n" +
		"}\n"
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(testFiles,
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	* {@value #MY_VALUE}\n" +
			"	    ^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 11)\n" +
			"	* {@value #MY_VALUE}\n" +
			"	    ^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n"
		);
	} else {
		runConformTest(testFiles);
	}
}
public void testBug153399b() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X { \n" +
		"	/**\n" +
		"	 * {@value}\n" +
		"	 */\n" +
		"	public final static int MY_VALUE = 0; \n" +
		"	/**\n" +
		"	 * {@value}\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"	/**\n" +
		"	 * {@value}\n" +
		"	 */\n" +
		"	class Sub {} \n" +
		"}\n"
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(testFiles,
			"----------\n" +
			"1. ERROR in X.java (at line 7)\n" +
			"	* {@value}\n" +
			"	    ^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 11)\n" +
			"	* {@value}\n" +
			"	    ^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n"
		);
	} else {
		runConformTest(testFiles);
	}
}
public void testBug153399c() {
	String[] testFiles = new String[] {
		"p1/X.java",
		"package p1;\n" +
		"public class X {\n" +
		"	/**\n" +
		"	 * @return a\n" +
		"	 */\n" +
		"	boolean get() {\n" +
		"		return false;\n" +
		"	}\n" +
		"}\n"
	};
	runConformTest(testFiles);
}
public void testBug153399d() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X { \n" +
		"	/**\n" +
		"	 * {@value #MY_VALUE}\n" +
		"	 * {@value}\n" +
		"	 * {@value Invalid}\n" +
		"	 */\n" +
		"	public final static int MY_VALUE = 0; \n" +
		"}\n"
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(testFiles,
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	* {@value #MY_VALUE}\n" +
			"	    ^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	* {@value}\n" +
			"	    ^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n"
		);
	} else {
		runNegativeTest(testFiles,
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	* {@value Invalid}\n" +
			"	          ^^^^^^^^\n" +
			"Javadoc: Invalid reference\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
}
public void testBug153399e() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X { \n" +
		"	/**\n" +
		"	 * {@value Invalid}\n" +
		"	 * {@value #MY_VALUE}\n" +
		"	 */\n" +
		"	public final static int MY_VALUE = 0; \n" +
		"}\n"
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(testFiles,
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	* {@value Invalid}\n" +
			"	    ^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n"
		);
	} else {
		runNegativeTest(testFiles,
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	* {@value Invalid}\n" +
			"	          ^^^^^^^^\n" +
			"Javadoc: Invalid reference\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
}

/**
 * bug 160015: [1.5][javadoc] Missing warning on autoboxing compatible methods
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=160015"
 */
public void testBug160015() {
	runNegativeTest(new String[] {
			"Test.java",
			"/**\n" +
			" * @see #method(Long) Warning!\n" +
			" */\n" +
			"public class Test {\n" +
			"	public void method(long l) {}\n" +
			"	/**\n" +
			"	 * @see #method(Long) Warning!\n" +
			"	 */\n" +
			"	void bar() {}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Test.java (at line 2)\n" +
		"	* @see #method(Long) Warning!\n" +
		"	        ^^^^^^\n" +
		"Javadoc: The method method(long) in the type Test is not applicable for the arguments (Long)\n" +
		"----------\n" +
		"2. ERROR in Test.java (at line 7)\n" +
		"	* @see #method(Long) Warning!\n" +
		"	        ^^^^^^\n" +
		"Javadoc: The method method(long) in the type Test is not applicable for the arguments (Long)\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * bug 163659: [javadoc] Compiler should warn when method parameters are not identical
 * test Ensure that a warning is raised when method parameter types are not identical
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=163659"
 */
public void testBug163659() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"/**\n" +
			" * @see #foo(MyInterface)\n" +
			" * @see #foo(MySubInterface)\n" +
			" */\n" +
			"public class Test {\n" +
			"	public void foo(MyInterface mi) {\n" +
			"	}\n" +
			"}\n" +
			"interface MyInterface {}\n" +
			"interface MySubInterface extends MyInterface {} \n"
		},
		"----------\n" +
		"1. ERROR in Test.java (at line 3)\n" +
		"	* @see #foo(MySubInterface)\n" +
		"	        ^^^\n" +
		"Javadoc: The method foo(MyInterface) in the type Test is not applicable for the arguments (MySubInterface)\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * bug 165794: [javadoc] Should not report ambiguous on method with parameterized types as parameters
 * test Ensure that no warning are raised when ambiguous parameterized methods are present in javadoc comments
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=165794"
 */
public void _testBug165794() {
	String[] testFiles = new String[] {
		"X.java",
		"/**\n" +
		" * No reasonable hint for resolving the {@link #getMax(A)}.\n" +
		" */\n" +
		"public class X {\n" +
		"    /**\n" +
		"     * Extends Number method.\n" +
		"     * @see #getMax(A ipZ)\n" +
		"     */\n" +
		"    public <T extends Y> T getMax(final A<T> ipY) {\n" +
		"        return ipY.t();\n" +
		"    }\n" +
		"    \n" +
		"    /**\n" +
		"     * Extends Exception method.\n" +
		"     * @see #getMax(A ipY)\n" +
		"     */\n" +
		"    public <T extends Z> T getMax(final A<T> ipZ) {\n" +
		"        return ipZ.t();\n" +
		"    }\n" +
		"}\n" +
		"class A<T> {\n" +
		"	T t() { return null; }\n" +
		"}\n" +
		"class Y {}\n" +
		"class Z {}"
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4
			|| this.complianceLevel >= ClassFileConstants.JDK1_7) {
		return;
	}
	runConformTest(testFiles);
}
/**
 * bug 166365: [javadoc] severity level of malformed javadoc comments did not work properly
 * test Ensure that no warning is raised when visibility is lower than the javadoc option one
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=166365"
 */
public void testBug166365() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"    /**\n" +
		"     * @return\n" +
		"     */\n" +
		"    private String getSomePrivate() {\n" +
		"        return \"SomePrivate\";\n" +
		"    }\n" +
		"    /**\n" +
		"     * @return\n" +
		"     */\n" +
		"    protected String getSomeProtected() {\n" +
		"        return \"SomeProtected\";\n" +
		"    }\n" +
		"    /**\n" +
		"     * @return\n" +
		"     */\n" +
		"    String getSomeDefault() {\n" +
		"        return \"SomeDefault\";\n" +
		"    }\n" +
		"    /**\n" +
		"     * @return\n" +
		"     */\n" +
		"    public String getSomePublic() {\n" +
		"        return \"SomePublic\";\n" +
		"    }\n" +
		"}\n"
	};
	this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
	runNegativeTest(testFiles,
		"----------\n" +
		"1. ERROR in X.java (at line 21)\n" +
		"	* @return\n" +
		"	   ^^^^^^\n" +
		"Javadoc: Description expected after @return\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * bug 166436: [javadoc] Potentially wrong javadoc warning for unexpected duplicate tag value
 * test Ensure that no duplicate warning is raised for value tag
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=166436"
 */
public void testBug166436() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	public static final String PUBLIC_CONST = \"public\";\n" +
		"	protected static final String PROTECTED_CONST = \"protected\";\n" +
		"	static final String DEFAULT_CONST = \"default\"; \n" +
		"	private static final String PRIVATE_CONST = \"private\"; \n" +
		"	/**\n" +
		"	 * Values:\n" +
		"	 * <ul>\n" +
		"	 * 	<li>{@value #PUBLIC_CONST}</li>\n" +
		"	 * 	<li>{@value #PROTECTED_CONST}</li>\n" +
		"	 * 	<li>{@value #DEFAULT_CONST}</li>\n" +
		"	 * 	<li>{@value #PRIVATE_CONST}</li>\n" +
		"	 * </ul>\n" +
		"	 */\n" +
		"	public X() {\n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(testFiles,
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	* 	<li>{@value #PUBLIC_CONST}</li>\n" +
			"	  	      ^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 10)\n" +
			"	* 	<li>{@value #PROTECTED_CONST}</li>\n" +
			"	  	      ^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 11)\n" +
			"	* 	<li>{@value #DEFAULT_CONST}</li>\n" +
			"	  	      ^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 12)\n" +
			"	* 	<li>{@value #PRIVATE_CONST}</li>\n" +
			"	  	      ^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n"
		);
	} else {
		runNegativeTest(testFiles,
			"----------\n" +
			"1. ERROR in X.java (at line 10)\n" +
			"	* 	<li>{@value #PROTECTED_CONST}</li>\n" +
			"	  	            ^^^^^^^^^^^^^^^^\n" +
			"Javadoc: \'public\' visibility for malformed doc comments hides this \'protected\' reference\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 11)\n" +
			"	* 	<li>{@value #DEFAULT_CONST}</li>\n" +
			"	  	            ^^^^^^^^^^^^^^\n" +
			"Javadoc: \'public\' visibility for malformed doc comments hides this \'default\' reference\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 12)\n" +
			"	* 	<li>{@value #PRIVATE_CONST}</li>\n" +
			"	  	            ^^^^^^^^^^^^^^\n" +
			"Javadoc: \'public\' visibility for malformed doc comments hides this \'private\' reference\n" +
			"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
}

/**
 * bug 168849: [javadoc] Javadoc warning on @see reference in class level docs.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=168849"
 */
public void testBug168849a() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X {\n" +
		"	/**\n" +
		"	 * @see http://www.eclipse.org/\n" +
		"	 */\n" +
		"	public void foo() { \n" +
		"	 \n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(true, units,
			"----------\n" +
			"1. WARNING in pkg\\X.java (at line 5)\n" +
			"	* @see http://www.eclipse.org/\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Invalid URL reference. Double quote the reference or use the href syntax\n" +
			"----------\n",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

public void testBug168849b() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X {\n" +
		"	/**\n" +
		"	 * @see http://ftp.eclipse.org/\n" +
		"	 */\n" +
		"	public void foo() { \n" +
		"	 \n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(true, units,
			"----------\n" +
			"1. WARNING in pkg\\X.java (at line 5)\n" +
			"	* @see http://ftp.eclipse.org/\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Invalid URL reference. Double quote the reference or use the href syntax\n" +
			"----------\n",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

public void testBug168849c() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X {\n" +
		"	/**\n" +
		"	 * @see ://\n" +
		"	 */\n" +
		"	public void foo() { \n" +
		"	 \n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(
			true, units,
			"----------\n" +
			"1. WARNING in pkg\\X.java (at line 5)\n" +
			"	* @see ://\n" +
			"	   ^^^\n" +
			"Javadoc: Missing reference\n" +
			"----------\n",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

public void testBug168849d() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X {\n" +
		"	/**\n" +
		"	 * @see http\u003A\u002F\u002Fwww.eclipse.org\n" +
		"	 */\n" +
		"	public void foo() { \n" +
		"	 \n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(true, units,
			"----------\n" +
			"1. WARNING in pkg\\X.java (at line 5)\n" +
			"	* @see http://www.eclipse.org\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Invalid URL reference. Double quote the reference or use the href syntax\n" +
			"----------\n",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

public void testBug168849e() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X {\n" +
		"	/**\n" +
		"	 * @see \"http\u003A\u002F\u002Fwww.eclipse.org\"\n" +
		"	 */\n" +
		"	public void foo() { \n" +
		"	 \n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(units);
}

public void testBug168849f() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X {\n" +
		"	/**\n" +
		"	 * @see \"http://www.eclipse.org/\"\n" +
		"	 */\n" +
		"	public void foo() { \n" +
		"	 \n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(units);
}

public void testBug168849g() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X {\n" +
		"	/**\n" +
		"	 * @see http:/ invalid reference\n" +
		"	 */\n" +
		"	public void foo() { \n" +
		"	 \n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(true, units,
			"----------\n" +
			"1. WARNING in pkg\\X.java (at line 5)\n" +
			"	* @see http:/ invalid reference\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

public void testBug168849h() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X {\n" +
		"	/**\n" +
		"	 * @see Object:/ invalid reference\n" +
		"	 */\n" +
		"	public void foo() { \n" +
		"	 \n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(true, units,
			"----------\n" +
			"1. WARNING in pkg\\X.java (at line 5)\n" +
			"	* @see Object:/ invalid reference\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

public void testBug168849i() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X {\n" +
		"	/**\n" +
		"	 * @see http\u003A\u002F invalid reference\n" +
		"	 */\n" +
		"	public void foo() { \n" +
		"	 \n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(true, units,
			"----------\n" +
			"1. WARNING in pkg\\X.java (at line 5)\n" +
			"	* @see http:/ invalid reference\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

public void testBug168849j() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X {\n" +
		"	/**\n" +
		"	 * @see Object\u003A\u002F invalid reference\n" +
		"	 */\n" +
		"	public void foo() { \n" +
		"	 \n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(true, units,
			"----------\n" +
			"1. WARNING in pkg\\X.java (at line 5)\n" +
			"	* @see Object:/ invalid reference\n" +
			"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Javadoc: Malformed reference (missing end space separator)\n" +
			"----------\n",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

/**
 * bug 170637: [javadoc] incorrect warning about missing parameter javadoc when using many links
 * test Verify that javadoc parser is not blown-up when there's a lot of inline tags
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=170637"
 */
public void testBug170637() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runConformTest(
		new String[] {
			"JavaDocTest.java",
			"public interface JavaDocTest\n" +
			"{\n" +
			"  /**\n" +
			"   * This is some stupid test...\n" +
			"   * \n" +
			"   * {@link JavaDocTest}\n" +
			"   * \n" +
			"   * @param bar1 {@link JavaDocTest}\n" +
			"   * @param bar2 {@link JavaDocTest}\n" +
			"   * @param bar3 {@link JavaDocTest}\n" +
			"   * @param bar4 {@link JavaDocTest}\n" +
			"   * @param bar5 {@link JavaDocTest}\n" +
			"   * @param bar6 {@link JavaDocTest}\n" +
			"   * @param bar7 {@link JavaDocTest}\n" +
			"   * @param bar8 {@link JavaDocTest}\n" +
			"   * @param bar9 {@link JavaDocTest}\n" +
			"   * @param bar10 {@link JavaDocTest}\n" +
			"   * @param bar11 {@link JavaDocTest}\n" +
			"   * @param bar12 {@link JavaDocTest}\n" +
			"   * @param bar13 {@link JavaDocTest}\n" +
			"   * \n" +
			"   * @return A string!\n" +
			"   */\n" +
			"  public String foo(String bar1,\n" +
			"      String bar2,\n" +
			"      String bar3,\n" +
			"      String bar4,\n" +
			"      String bar5,\n" +
			"      String bar6,\n" +
			"      String bar7,\n" +
			"      String bar8,\n" +
			"      String bar9,\n" +
			"      String bar10,\n" +
			"      String bar11,\n" +
			"      String bar12,\n" +
			"      String bar13\n" +
			"      );\n" +
			"\n" +
			"  /**\n" +
			"   * This is some more stupid test...\n" +
			"   * \n" +
			"   * {@link JavaDocTest}\n" +
			"   * {@link JavaDocTest}\n" +
			"   * {@link JavaDocTest}\n" +
			"   * {@link JavaDocTest}\n" +
			"   * {@link JavaDocTest}\n" +
			"   * {@link JavaDocTest}\n" +
			"   * {@link JavaDocTest}\n" +
			"   * {@link JavaDocTest}\n" +
			"   * {@link JavaDocTest}\n" +
			"   * {@link JavaDocTest}\n" +
			"   * {@link JavaDocTest}\n" +
			"   * {@link JavaDocTest}\n" +
			"   * {@link JavaDocTest}\n" +
			"   * {@link JavaDocTest}\n" +
			"   * {@link JavaDocTest}\n" +
			"   * {@link JavaDocTest}\n" +
			"   * {@link JavaDocTest}\n" +
			"   * {@link JavaDocTest}\n" +
			"   * {@link JavaDocTest}\n" +
			"   * {@link JavaDocTest}\n" +
			"   * {@link JavaDocTest}\n" +
			"   * \n" +
			"   * @param bar1 \n" +
			"   * @param bar2 \n" +
			"   * @param bar3 \n" +
			"   * @param bar4 \n" +
			"   * @param bar5 \n" +
			"   * @param bar6 \n" +
			"   * @param bar7 \n" +
			"   * @param bar8 \n" +
			"   * @param bar9 \n" +
			"   * @param bar10 \n" +
			"   * @param bar11 \n" +
			"   * @param bar12 \n" +
			"   * @param bar13 \n" +
			"   * \n" +
			"   * @return A string!\n" +
			"   */\n" +
			"  public String foo2(String bar1,\n" +
			"      String bar2,\n" +
			"      String bar3,\n" +
			"      String bar4,\n" +
			"      String bar5,\n" +
			"      String bar6,\n" +
			"      String bar7,\n" +
			"      String bar8,\n" +
			"      String bar9,\n" +
			"      String bar10,\n" +
			"      String bar11,\n" +
			"      String bar12,\n" +
			"      String bar13\n" +
			"      );\n" +
			"}\n"
		}
	);
}
public void testBug170637a() {
	// conform test: verify we can handle a large number of tags
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"public interface X\n" +
		"{\n" +
		"  /**\n" +
		"   * Test for bug {@link \"https://bugs.eclipse.org/bugs/show_bug.cgi?id=170637\"}\n" +
		"   * \n" +
		"   * \n" +
		"   * @param bar1 {@link X}\n" +
		"   * @param bar2 {@link X}\n" +
		"   * @param bar3 {@link X}\n" +
		"   * @param bar4 {@link X}\n" +
		"   * @param bar5 {@link X}\n" +
		"   * @param bar6 {@link X}\n" +
		"   * @param bar7 {@link X}\n" +
		"   * @param bar8 {@link X}\n" +
		"   * @param bar9 {@link X}\n" +
		"   * @param bar10 {@link X}\n" +
		"   * @param bar11 {@link X}\n" +
		"   * @param bar12 {@link X}\n" +
		"   * @param bar13 {@link X}\n" +
		"   * @param bar14 {@link X}\n" +
		"   * @param bar15 {@link X}\n" +
		"   * @param bar16 {@link X}\n" +
		"   * @param bar17 {@link X}\n" +
		"   * @param bar18 {@link X}\n" +
		"   * @param bar19 {@link X}\n" +
		"   * @param bar20 {@link X}\n" +
		"   * @param bar21 {@link X}\n" +
		"   * @param bar22 {@link X}\n" +
		"   * @param bar23 {@link X}\n" +
		"   * @param bar24 {@link X}\n" +
		"   * @param bar25 {@link X}\n" +
		"   * @param bar26 {@link X}\n" +
		"   * @param bar27 {@link X}\n" +
		"   * @param bar28 {@link X}\n" +
		"   * @param bar29 {@link X}\n" +
		"   * @param bar30 {@link X}\n" +
		"   * \n" +
		"   * @return A string\n" +
		"   */\n" +
		"  public String foo(String bar1,\n" +
		"      String bar2,\n" +
		"      String bar3,\n" +
		"      String bar4,\n" +
		"      String bar5,\n" +
		"      String bar6,\n" +
		"      String bar7,\n" +
		"      String bar8,\n" +
		"      String bar9,\n" +
		"      String bar10,\n" +
		"      String bar11,\n" +
		"      String bar12,\n" +
		"      String bar13,\n" +
		"      String bar14,\n" +
		"      String bar15,\n" +
		"      String bar16,\n" +
		"      String bar17,\n" +
		"      String bar18,\n" +
		"      String bar19,\n" +
		"      String bar20,\n" +
		"      String bar21,\n" +
		"      String bar22,\n" +
		"      String bar23,\n" +
		"      String bar24,\n" +
		"      String bar25,\n" +
		"      String bar26,\n" +
		"      String bar27,\n" +
		"      String bar28,\n" +
		"      String bar29,\n" +
		"      String bar30\n" +
		"      );\n" +
		"}\n"
	};
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runConformTest(units);
}
public void testBug170637b() {
	// conform test: verify we are able to raise warnings when dealing with a large number of tags
	String[] units = new String[] {
		"X.java",
		"public interface X\n" +
		"{\n" +
		"  /**\n" +
		"   * Test for bug {@link \"https://bugs.eclipse.org/bugs/show_bug.cgi?id=170637\"}\n" +
		"   * \n" +
		"   * \n" +
		"   * @param bar1 {@link X}\n" +
		"   * @param bar2 {@link X}\n" +
		"   * @param bar3 {@link X}\n" +
		"   * @param bar4 {@link X}\n" +
		"   * @param bar5 {@link X}\n" +
		"   * @param bar6 {@link X}\n" +
		"   * @param bar7 {@link X}\n" +
		"   * @param bar8 {@link X}\n" +
		"   * @param bar9 {@link X}\n" +
		"   * @param bar10 {@link X}\n" +
		"   * @param bar11 {@link X}\n" +
		"   * @param bar12 {@link X}\n" +
		"   * @param bar13 {@link X}\n" +
		"   * @param bar14 {@link X}\n" +
		"   * @param bar15 {@link X}\n" +
		"   * @param bar16 {@link X}\n" +
		"   * @param bar17 {@link X}\n" +
		"   * @param bar18 {@link X}\n" +
		"   * @param bar19 {@link X}\n" +
		"   * @param bar20 {@link X}\n" +
		"   * @param bar21 {@link X}\n" +
		"   * @param bar22 {@link X}\n" +
		"   * @param bar23 {@link X}\n" +
		"   * @param bar24 {@link X}\n" +
		"   * @param bar25 {@link X}\n" +
		"   * @param bar26 {@link X}\n" +
		"   * @param bar27 {@link X}\n" +
		"   * @param bar28 {@link X}\n" +
		"   * @param bar29 {@link X}\n" +
		"   * @param bar30 {@link X}\n" +
		"   * \n" +
		"   * @return A string\n" +
		"   */\n" +
		"  public String foo(String bar1,\n" +
		"      String bar2,\n" +
		"      String bar3,\n" +
		"      String bar4,\n" +
		"      String bar5,\n" +
		"      String bar6,\n" +
		"      String bar7,\n" +
		"      String bar8,\n" +
		"      String bar9,\n" +
		"      String bar10,\n" +
		"      String bar11,\n" +
		"      String bar12,\n" +
		"      String bar13,\n" +
		"      String bar14,\n" +
		"      String bar15,\n" +
		"      String bar16,\n" +
		"      String bar17,\n" +
		"      String bar18,\n" +
		"      String bar19,\n" +
		"      String bar20,\n" +
		"      String bar21,\n" +
		"      String bar22,\n" +
		"      String bar23,\n" +
		"      String bar24,\n" +
		"      String bar25,\n" +
		"      String bar26,\n" +
		"      String bar27,\n" +
		"      String bar28,\n" +
		"      String bar29,\n" +
		"      String bar30,\n" +
		"      String bar31\n" +
		"      );\n" +
		"}\n"
	};
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(units,
		"----------\n" +
		"1. ERROR in X.java (at line 70)\n" +
		"	String bar31\n" +
		"	       ^^^^^\n" +
		"Javadoc: Missing tag for parameter bar31\n" +
		"----------\n");
}

/**
 * Bug 176027: [javadoc] @link to member type handled incorrectly
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=176027"
 */
public void testBug176027a() {
	// case1 class X static class Inner
	String[] units = new String[] {
		"otherpkg/C.java",
		"package otherpkg;\n" +
		"public class C {\n" +
		"        public static class Inner { }\n" +
		"}\n"
		,
		"somepkg/MemberTypeDocTest.java",
		"package somepkg;\n" +
		"import otherpkg.C.Inner;\n" +
		"/**\n" +
		" * {@link Inner} -- error/warning \n" +
		" */\n" +
		"public class MemberTypeDocTest {\n" +
		"      void m() { }\n" +
		"}\n"
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(units,
			//somepkg/MemberTypeDocTest.java:6: warning - Tag @link: reference not found: Inner
			"----------\n" +
			"1. ERROR in somepkg\\MemberTypeDocTest.java (at line 4)\n" +
			"	* {@link Inner} -- error/warning \n" +
			"	         ^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n"
		);
	}
	else {
		runConformTest(units);
	}
}

public void testBug176027b() {
	// case3 class X class Inner
	String[] units = new String[] {
		"otherpkg/C.java",
		"package otherpkg;\n" +
		"public class C {\n" +
		"        public class Inner { }\n" +
		"}\n"
		,
		"somepkg/MemberTypeDocTest.java",
		"package somepkg;\n" +
		"import otherpkg.C.Inner;\n" +
		"/**\n" +
		" * {@link Inner} -- error/warning \n" +
		" */\n" +
		"public class MemberTypeDocTest {\n" +
		"      void m() { }\n" +
		"}\n"
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(units,
			//somepkg/MemberTypeDocTest.java:6: warning - Tag @link: reference not found: Inner
			"----------\n" +
			"1. ERROR in somepkg\\MemberTypeDocTest.java (at line 4)\n" +
			"	* {@link Inner} -- error/warning \n" +
			"	         ^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n"
		);
	}
	else {
		runConformTest(units);
	}
}

public void testBug176027c() {
	// case3 class X interface Inner
	String[] units = new String[] {
		"otherpkg/C.java",
		"package otherpkg;\n" +
		"public class C {\n" +
		"        public interface Inner { }\n" +
		"}\n"
		,
		"somepkg/MemberTypeDocTest.java",
		"package somepkg;\n" +
		"import otherpkg.C.Inner;\n" +
		"/**\n" +
		" * {@link Inner} -- error/warning \n" +
		" */\n" +
		"public class MemberTypeDocTest {\n" +
		"      void m() { }\n" +
		"}\n"
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(units,
			//somepkg/MemberTypeDocTest.java:6: warning - Tag @link: reference not found: Inner
			"----------\n" +
			"1. ERROR in somepkg\\MemberTypeDocTest.java (at line 4)\n" +
			"	* {@link Inner} -- error/warning \n" +
			"	         ^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n"
		);
	}
	else {
		runConformTest(units);
	}
}

public void testBug176027d() {
	// case4 interface X static class Inner
	String[] units = new String[] {
		"otherpkg/C.java",
		"package otherpkg;\n" +
		"public interface C {\n" +
		"        public static class Inner { }\n" +
		"}\n"
		,
		"somepkg/MemberTypeDocTest.java",
		"package somepkg;\n" +
		"import otherpkg.C.Inner;\n" +
		"/**\n" +
		" * {@link Inner} -- error/warning \n" +
		" */\n" +
		"public class MemberTypeDocTest {\n" +
		"      void m() { }\n" +
		"}\n"
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(units,
			//somepkg/MemberTypeDocTest.java:6: warning - Tag @link: reference not found: Inner
			"----------\n" +
			"1. ERROR in somepkg\\MemberTypeDocTest.java (at line 4)\n" +
			"	* {@link Inner} -- error/warning \n" +
			"	         ^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n"
		);
	}
	else {
		runConformTest(units);
	}
}

public void testBug176027f() {
	// case5 interface X class Inner
	String[] units = new String[] {
		"otherpkg/C.java",
		"package otherpkg;\n" +
		"public interface C {\n" +
		"        public class Inner { }\n" +
		"}\n"
		,
		"somepkg/MemberTypeDocTest.java",
		"package somepkg;\n" +
		"import otherpkg.C.Inner;\n" +
		"/**\n" +
		" * {@link Inner} -- error/warning \n" +
		" */\n" +
		"public class MemberTypeDocTest {\n" +
		"      void m() { }\n" +
		"}\n"
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(units,
			//somepkg/MemberTypeDocTest.java:6: warning - Tag @link: reference not found: Inner
			"----------\n" +
			"1. ERROR in somepkg\\MemberTypeDocTest.java (at line 4)\n" +
			"	* {@link Inner} -- error/warning \n" +
			"	         ^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n"
		);
	}
	else {
		runConformTest(units);
	}
}

public void testBug176027g() {
	// case6 interface X interface Inner
	String[] units = new String[] {
		"otherpkg/C.java",
		"package otherpkg;\n" +
		"public interface C {\n" +
		"        public interface Inner { }\n" +
		"}\n"
		,
		"somepkg/MemberTypeDocTest.java",
		"package somepkg;\n" +
		"import otherpkg.C.Inner;\n" +
		"/**\n" +
		" * {@link Inner} -- error/warning \n" +
		" */\n" +
		"public class MemberTypeDocTest {\n" +
		"      void m() { }\n" +
		"}\n"
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(units,
			//somepkg/MemberTypeDocTest.java:6: warning - Tag @link: reference not found: Inner
			"----------\n" +
			"1. ERROR in somepkg\\MemberTypeDocTest.java (at line 4)\n" +
			"	* {@link Inner} -- error/warning \n" +
			"	         ^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n"
		);
	}
	else {
		runConformTest(units);
	}
}

public void testBug176027h_public() {
	// test embedded inner classes
	String[] units = new String[] {
			"mainpkg/Outer.java",
			"package mainpkg;\n" +
			"public class Outer {\n" +
			"        public class Inner {\n" +
			"        	public class MostInner{\n" +
			"    \n" +
			"        	}\n" +
			"        } \n" +
			"}\n"
			,
			"pkg1/Valid1.java",
			"package pkg1; \n" +
			"import mainpkg.Outer.Inner.MostInner;\n" +
			"// valid import - no error in 5.0\n" +
			"\n" +
			"/** \n" +
			" * {@link MostInner}\n" +
			" * \n" +
			" */ \n" +
			"public class Valid1 { \n" +
			"	/** \n" +
			"	 * {@link MostInner} \n" +
			"	 * \n" +
			"	 */ \n" +
			"      void m() { } \n" +
			"}\n"
			,
			"pkg2/Valid2.java",
			"package pkg2; \n" +
			"import mainpkg.Outer.Inner.*;\n" +
			"//valid import - no error in 5.0\n" +
			"\n" +
			"/** \n" +
			" * {@link MostInner}\n" +
			" * \n" +
			" */ \n" +
			"public class Valid2 { \n" +
			"      void m() { } \n" +
			"}\n"
			,
			"pkg3/Invalid3.java",
			"package pkg3; \n" +
			"import mainpkg.Outer.*;\n" +
			"//invalid import: expecting warning / error\n" +
			"\n" +
			"/** \n" +
			" * {@link MostInner} -- error/warning  \n" +
			" * \n" +
			" */ \n" +
			"public class Invalid3 { \n" +
			"      void m() { } \n" +
			"}\n"
	};

	String error14 = new String (
		//pkg1\Valid1.java:12: warning - Tag @link: reference not found: MostInner
		//pkg2\Valid2.java:12: warning - Tag @link: reference not found: MostInner
		//pkg3\Invalid3.java:12: warning - Tag @link: reference not found: MostInner
		"----------\n" +
		"1. ERROR in pkg1\\Valid1.java (at line 6)\n" +
		"	* {@link MostInner}\n" +
		"	         ^^^^^^^^^\n" +
		"Javadoc: Invalid member type qualification\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in pkg2\\Valid2.java (at line 6)\n" +
		"	* {@link MostInner}\n" +
		"	         ^^^^^^^^^\n" +
		"Javadoc: Invalid member type qualification\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in pkg3\\Invalid3.java (at line 2)\n" +
		"	import mainpkg.Outer.*;\n"+
		"	       ^^^^^^^^^^^^^\n"+
		"The import mainpkg.Outer is never used\n" +
		"----------\n" +
		"2. ERROR in pkg3\\Invalid3.java (at line 6)\n" +
		"	* {@link MostInner} -- error/warning  \n" +
		"	         ^^^^^^^^^\n" +
		"Javadoc: MostInner cannot be resolved to a type\n" +
		"----------\n");

	String error50 = new String (
			//pkg3\Invalid3.java:12: warning - Tag @link: reference not found: MostInner
			"----------\n" +
			"1. ERROR in pkg3\\Invalid3.java (at line 2)\n" +
			"	import mainpkg.Outer.*;\n"+
			"	       ^^^^^^^^^^^^^\n"+
			"The import mainpkg.Outer is never used\n" +
			"----------\n" +
			"2. ERROR in pkg3\\Invalid3.java (at line 6)\n" +
			"	* {@link MostInner} -- error/warning  \n" +
			"	         ^^^^^^^^^\n" +
			"Javadoc: MostInner cannot be resolved to a type\n" +
			"----------\n");

	this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(units,error14);
	}
	else {
		runNegativeTest(units,error50, JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

public void testBug176027h_private() {
	// test embedded inner classes
	String[] units = new String[] {
			"mainpkg/Outer.java",
			"package mainpkg;\n" +
			"public class Outer {\n" +
			"        public class Inner {\n" +
			"        	public class MostInner{\n" +
			"    \n" +
			"        	}\n" +
			"        } \n" +
			"}\n"
			,
			"pkg1/Valid1.java",
			"package pkg1; \n" +
			"import mainpkg.Outer.Inner.MostInner;\n" +
			"// valid import - no error in 5.0\n" +
			"\n" +
			"/** \n" +
			" * {@link MostInner}\n" +
			" * \n" +
			" */ \n" +
			"public class Valid1 { \n" +
			"	/** \n" +
			"	 * {@link MostInner} \n" +
			"	 * \n" +
			"	 */ \n" +
			"      void m() { } \n" +
			"}\n"
			,
			"pkg2/Valid2.java",
			"package pkg2; \n" +
			"import mainpkg.Outer.Inner.*;\n" +
			"//valid import - no error in 5.0\n" +
			"\n" +
			"/** \n" +
			" * {@link MostInner}\n" +
			" * \n" +
			" */ \n" +
			"public class Valid2 { \n" +
			"      void m() { } \n" +
			"}\n"
			,
			"pkg3/Invalid3.java",
			"package pkg3; \n" +
			"import mainpkg.Outer.*;\n" +
			"//invalid import: expecting warning / error\n" +
			"\n" +
			"/** \n" +
			" * {@link MostInner} -- error/warning  \n" +
			" * \n" +
			" */ \n" +
			"public class Invalid3 { \n" +
			"      void m() { } \n" +
			"}\n"
	};

	String error14 = new String(
			//pkg1\Valid1.java:12: warning - Tag @link: reference not found: MostInner
			//pkg1\Valid1.java:17: warning - Tag @link: reference not found: MostInner
			//pkg2\Valid2.java:12: warning - Tag @link: reference not found: MostInner
			//pkg3\Invalid3.java:12: warning - Tag @link: reference not found: MostInner
			"----------\n" +
			"1. ERROR in pkg1\\Valid1.java (at line 6)\n" +
			"	* {@link MostInner}\n" +
			"	         ^^^^^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n" +
			"2. ERROR in pkg1\\Valid1.java (at line 11)\n" +
			"	* {@link MostInner} \n" +
			"	         ^^^^^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in pkg2\\Valid2.java (at line 6)\n" +
			"	* {@link MostInner}\n" +
			"	         ^^^^^^^^^\n" +
			"Javadoc: Invalid member type qualification\n" +
			"----------\n" +
			"----------\n" +
			"1. ERROR in pkg3\\Invalid3.java (at line 2)\n" +
			"	import mainpkg.Outer.*;\n"+
			"	       ^^^^^^^^^^^^^\n"+
			"The import mainpkg.Outer is never used\n" +
			"----------\n" +
			"2. ERROR in pkg3\\Invalid3.java (at line 6)\n" +
			"	* {@link MostInner} -- error/warning  \n" +
			"	         ^^^^^^^^^\n" +
			"Javadoc: MostInner cannot be resolved to a type\n" +
	"----------\n");

	String error50 = new String(
			//pkg3\Invalid3.java:12: warning - Tag @link: reference not found: MostInner
			"----------\n" +
			"1. ERROR in pkg3\\Invalid3.java (at line 2)\n" +
			"	import mainpkg.Outer.*;\n"+
			"	       ^^^^^^^^^^^^^\n"+
			"The import mainpkg.Outer is never used\n" +
			"----------\n" +
			"2. ERROR in pkg3\\Invalid3.java (at line 6)\n" +
			"	* {@link MostInner} -- error/warning  \n" +
			"	         ^^^^^^^^^\n" +
			"Javadoc: MostInner cannot be resolved to a type\n" +
			"----------\n");

	this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(units,error14);
	}
	else {
		runNegativeTest(units,error50, JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

/**
 * bug 177009: [javadoc] Missing Javadoc tag not reported
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=177009"
 */
public void testBug177009a() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X {\n" +
		"	public X(String str, int anInt) {\n" +
		"	}\n" +
		"}\n",
		"pkg/Y.java",
		"package pkg;\n" +
		"\n" +
		"public class Y extends X {\n" +
		"	private static int myInt = 0;\n" +
		"	/**\n" +
		"	 * @see X#X(String, int)\n" + // case1 potential AIOOBE
		"	 */\n" +
		"	public Y(String str) {\n" +
		"		super(str, myInt);\n" +
		"	}\n" +
		"}\n"
	};
	this.reportMissingJavadocTags = CompilerOptions.WARNING;
	runConformTest(
			true,
			units,
			"----------\n" +
			"1. WARNING in pkg\\Y.java (at line 8)\n" +
			"	public Y(String str) {\n" +
			"	                ^^^\n" +
			"Javadoc: Missing tag for parameter str\n" +
			"----------\n",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

public void testBug177009b() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X {\n" +
		"	public X(String str, int anInt) {\n" +
		"	}\n" +
		"}\n",
		"pkg/Y.java",
		"package pkg;\n" +
		"\n" +
		"public class Y extends X {\n" +
		"	/**\n" +
		"	 * @param str\n" +
		"	 * @param anInt\n" +
		"	 * @see X#X(String, int)\n" + // case2 find super ref
		"	 */\n" +
		"	public Y(String str, int anInt, int anotherInt) {\n" +
		"		super(str, anInt);\n" +
		"	}\n" +
		"}\n"
	};
	this.reportMissingJavadocTags = CompilerOptions.WARNING;
	runConformTest(true, units,
			"----------\n" +
			"1. WARNING in pkg\\Y.java (at line 9)\n" +
			"	public Y(String str, int anInt, int anotherInt) {\n" +
			"	                                    ^^^^^^^^^^\n" +
			"Javadoc: Missing tag for parameter anotherInt\n" +
			"----------\n",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

/**
 * bug 190970: [javadoc] "field never read locally" analysis should not consider javadoc
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=190970"
 */
public void testBug190970a() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
	this.runConformTest(
		true,
		new String[] {
			"X.java",
			"public class X {\n" +
			"private int unused1;\n" +
			"\n" +
	        "/**\n" +
	        " * Same value as {@link #unused1}\n" +
	        " */\n" +
	        "private int unused2;\n" +
			"}\n",
		},
		null,
		customOptions,
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	private int unused1;\n" +
		"	            ^^^^^^^\n" +
		"The value of the field X.unused1 is not used\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 7)\n" +
		"	private int unused2;\n" +
		"	            ^^^^^^^\n" +
		"The value of the field X.unused2 is not used\n" +
		"----------\n",
		null, null,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings
	);
}
// test unused methods
public void testBug190970b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
	this.runConformTest(
		true,
		new String[] {
			"pkg/X.java",
			"package pkg;\n" +
			"\n" +
			"public class X {\n" +
			"private void unused1() {}\n" +
			"/**\n" +
			" * Same value as {@link #unused1()}\n" +
			" */\n" +
			"private void unused2() {}\n" +
			"}\n",
		},
		null,
		customOptions,
		"----------\n" +
		"1. WARNING in pkg\\X.java (at line 4)\n" +
		"	private void unused1() {}\n" +
		"	             ^^^^^^^^^\n" +
		"The method unused1() from the type X is never used locally\n" +
		"----------\n" +
		"2. WARNING in pkg\\X.java (at line 8)\n" +
		"	private void unused2() {}\n" +
		"	             ^^^^^^^^^\n" +
		"The method unused2() from the type X is never used locally\n" +
		"----------\n",
		null, null,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings
	);
}
// test unused types
public void testBug190970c() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
	runConformTest(
		true,
	new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X {\n" +
		"private class unused1 {}\n" +
		"/**\n" +
		" * {@link X.unused1}\n" +
		" */\n" +
		"private class unused2 {}\n" +
		"}\n",
	},
	null,
	customOptions,
	"----------\n" +
	"1. WARNING in pkg\\X.java (at line 4)\n" +
	"	private class unused1 {}\n" +
	"	              ^^^^^^^\n" +
	"The type X.unused1 is never used locally\n" +
	"----------\n" +
	"2. WARNING in pkg\\X.java (at line 8)\n" +
	"	private class unused2 {}\n" +
	"	              ^^^^^^^\n" +
	"The type X.unused2 is never used locally\n" +
	"----------\n",
	null, null,
	JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings
	);
}

//	static { TESTS_PREFIX = "testBug191322"; }
/**
 * bug 191322: [javadoc] @see or @link reference to method without signature fails to resolve to base class method
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=191322"
 */
public void testBug191322() {
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo() {}\n" +
			"	/**\n" +
			"	 * {@link #foo}.\n" +
			"	 * @see #foo\n" +
			"	 */\n" +
			"	void goo() {}\n" +
			"}\n",
			"Y.java",
			"class Y extends X {\n" +
			"	/**\n" +
			"	 * {@link #foo}\n" +
			"	 * @see #foo\n" +
			"	 */\n" +
			"	void hoo() {}\n" +
			"}"
		}
	);
}
public void testBug191322b() {
	runConformTest(
		new String[] {
			"b/X.java",
			"package b;\n" +
			"public class X {\n" +
			"	void foo() {}\n" +
			"}\n" +
			"class Y extends X {}\n" +
			"class W extends Y {}\n" +
			"class Z extends W {\n" +
			"	/**\n" +
			"	 * {@link #foo}\n" +
			"	 * @see #foo\n" +
			"	 */\n" +
			"	void hoo() {}\n" +
			"}\n"
		}
	);
}
public void testBug191322c() {
	runConformTest(
		new String[] {
			"c/X.java",
			"package c;\n" +
			"public interface X {\n" +
			"	void foo();\n" +
			"}\n" +
			"interface Y extends X {\n" +
			"	/**\n" +
			"	 * {@link #foo}\n" +
			"	 * @see #foo\n" +
			"	 */\n" +
			"	void hoo();\n" +
			"}\n"
		}
	);
}
public void testBug191322d() {
	runConformTest(
		new String[] {
			"d/X.java",
			"package d;\n" +
			"public interface X {\n" +
			"	void foo();\n" +
			"}\n" +
			"interface Y extends X {}\n" +
			"abstract class W implements Y {}\n" +
			"abstract class Z extends W {\n" +
			"	/**\n" +
			"	 * {@link #foo}\n" +
			"	 * @see #foo\n" +
			"	 */\n" +
			"	void hoo() {}\n" +
			"}\n"
		}
	);
}
public void testBug191322e() {
	runConformTest(
		new String[] {
			"e/X.java",
			"package e;\n" +
			"public class X {\n" +
			"	void foo() {}\n" +
			"	class Y {\n" +
			"		/**\n" +
			"		 * {@link #foo}\n" +
			"		 * @see #foo\n" +
			"		 */\n" +
			"		void hoo() {}\n" +
			"	}\n" +
			"}\n"
		}
	);
}
public void testBug191322f() {
	runConformTest(
		new String[] {
			"f/X.java",
			"package f;\n" +
			"public class X {\n" +
			"	void foo() {}\n" +
			"	void foo(String str) {}\n" +
			"}\n" +
			"class Y extends X {\n" +
			"	/**\n" +
			"	 * {@link #foo}\n" +
			"	 * @see #foo\n" +
			"	 */\n" +
			"	void hoo() {}\n" +
			"}\n"
		}
	);
}
public void testBug191322g() {
	runConformTest(
		new String[] {
			"g/X.java",
			"package g;\n" +
			"public class X {\n" +
			"	void foo(String str) {}\n" +
			"	void foo(int x) {}\n" +
			"}\n" +
			"class Y extends X {\n" +
			"	/**\n" +
			"	 * {@link #foo}\n" +
			"	 * @see #foo\n" +
			"	 */\n" +
			"	void hoo() {}\n" +
			"}\n"
		}
	);
}
public void testBug191322h() {
	runConformTest(
		new String[] {
			"h/X.java",
			"package h;\n" +
			"public class X {\n" +
			"	void foo(String str) {}\n" +
			"}\n" +
			"class Y extends X {\n" +
			"	/**\n" +
			"	 * {@link #foo}\n" +
			"	 * @see #foo\n" +
			"	 */\n" +
			"	void hoo() {}\n" +
			"}\n"
		}
	);
}
public void testBug191322i() {
	runConformTest(
		new String[] {
			"i/X.java",
			"package i;\n" +
			"interface X {\n" +
			"	void foo();\n" +
			"}\n" +
			"interface Y {\n" +
			"	void foo(int i);\n" +
			"}\n" +
			"abstract class Z implements X, Y {\n" +
			"	/**\n" +
			"	 * @see #foo\n" +
			"	 */\n" +
			"	void bar() {\n" +
			"	}\n" +
			"}"
		}
	);
}

/**
 * bug 195374: [javadoc] Missing Javadoc warning for required qualification for inner types at 1.4 level
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=195374"
 */
public void testBug195374() {
	String[] units = new String[] {
		"X.java",
		"public class X {\n" +
		"	public static class Param {\n" +
	    "       /**\n" +
	    "         * warning expected when compliance < 1.5 {@link X#setParams(Param[])}\n" +
	    "         * no warning expected {@link X#setParams(X.Param[])}\n" +
	    "         */\n" +
	    "        public int getIndex() {\n" +
	    "                    return 0;\n" +
	    "        }\n" +
	    "    }\n" +
	    "    public void setParams(Param[] params) {\n" +
	    "	}\n" +
		"}\n"
	};

	String error14 = new String(
		// warning - Tag @link: can't find setParams(Param[]) in X
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	* warning expected when compliance < 1.5 {@link X#setParams(Param[])}\n" +
		"	                                                            ^^^^^^^\n" +
		"Javadoc: Invalid member type qualification\n" +
		"----------\n");
	this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(units,error14);
	}
	else {
		runConformTest(units);
	}
}

/**
 * bug 207765: [javadoc] Javadoc warning on @see reference could be improved
 * test Ensure we have different message depending on tag value
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=207765"
 */
public void testBug207765() {
	runNegativeTest(
		new String[] {
			"pkg/X.java",
			"package pkg;\n" +
			"\n" +
			"public class X {\n" +
			"	/**\n" +
			"	 * {@link \"http://www.eclipse.org/}\n" +
			"	 * @see \"http://www.eclipse.org/\n" +
			"	 */\n" +
			"	public void foo() { \n" +
			"	 \n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in pkg\\X.java (at line 5)\n" +
		"	* {@link \"http://www.eclipse.org/}\n" +
		"	         ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: Invalid reference\n" +
		"----------\n" +
		"2. ERROR in pkg\\X.java (at line 6)\n" +
		"	* @see \"http://www.eclipse.org/\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: Invalid URL reference. Double quote the reference or use the href syntax\n" +
		"----------\n"
	);
}

/**
 * bug 222900: [Javadoc] Missing description is warned if valid description is on a new line
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=222900"
 */
public void testBug222900a() {
	String[] units = new String[] {
		"X.java",
		"/**\n" +
		"* @since\n" +
		"* 	description\n" +
		"* @author\n" +
		"* 	description\n" +
		"* @version\n" +
		"* 	description\n" +
		"*/\n" +
		"public class X {\n" +
		"	/**\n" +
		"	 * @param  aParam\n" +
		"	 *         description\n" +
		"	 * @return\n" +
		"	 *         description\n" +
		"	 * @since\n" +
		"	 *         description\n" +
		"	 * @throws NullPointerException\n" +
		"	 *         description\n" +
		"	 * @exception NullPointerException\n" +
		"	 *            description\n" +
		"	 * @serial\n" +
		"	 *         description\n" +
		"	 * @serialData\n" +
		"	 *         description\n" +
		"	 * @serialField\n" +
		"	 *         description\n" +
		"	 * @deprecated\n" +
		"	 *         description\n" +
		"	 */\n" +
		"	public String foo(String aParam) {\n" +
		"		return new String();\n" +
		"	}\n" +
		"}\n"
	};
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runConformTest(units);
}
public void testBug222900b() {
	String[] units = new String[] {
		"X.java",
		"/**\n" +
		" * {@code\n" +
		" *        description}\n" +
		" * {@literal\n" +
		" *        description}\n" +
		"*/\n" +
		"public class X {\n" +
		"}\n"
	};
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runConformTest(units);
}
public void testBug222900c() {
	String[] units = new String[] {
		"X.java",
		"/**\n" +
		" * Test the {@code} missing description\n" +
		" * Test the {@code\n" +
		" * } missing description\n" +
		" * Test the {@code X} with description\n" +
		" * Test the {@code\n" +
		" * public class X} with description\n" +
		"*/\n" +
		"public class X {\n" +
		"}\n"
	};
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runNegativeTest(units,
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	* Test the {@code} missing description\n" +
		"	             ^^^^\n" +
		"Javadoc: Description expected after @code\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	* Test the {@code\n" +
		"	             ^^^^\n" +
		"Javadoc: Description expected after @code\n" +
		"----------\n"
	);
}

/**
 * bug 222902: [Javadoc] Missing description should not be warned in some cases
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=222902"
 */
public void testBug222902() {
	String[] units = new String[] {
		"X.java",
		"/**\n" +
		" * {@code}\n" +
		" * {@literal}\n" +
		" * @author\n" +
		" * @deprecated\n" +
		" * @since\n" +
		" * @version\n" +
		" * @generated\n" + // should not get a warning for missing description on non-standard tag
		" * @code\n" + // should not get a warning for non-inlined tag @code
		" * @literal\n" + // should not get a warning for non-inlined tag @literal
		"*/\n" +
		"public class X {\n" +
		"	/**\n" +
		"	 * @param  aParam\n" +
		"	 * @return\n" +
		"	 * @throws NullPointerException\n" +
		"	 * @exception NullPointerException\n" +
		"	 */\n" +
		"	public String foo(String aParam) {\n" +
		"		return new String();\n" +
		"	}\n" +
		"	/**\n" +
		"	 * @serial\n" +
		"	 * @serialData\n" +
		"	 * @serialField\n" +
		"	 */\n" +
		"	Object field;\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runConformTest(true, units,
		"----------\n" +
		"1. WARNING in X.java (at line 2)\n" +
		"	* {@code}\n" +
		"	    ^^^^\n" +
		"Javadoc: Description expected after @code\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 3)\n" +
		"	* {@literal}\n" +
		"	    ^^^^^^^\n" +
		"Javadoc: Description expected after @literal\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 4)\n" +
		"	* @author\n" +
		"	   ^^^^^^\n" +
		"Javadoc: Description expected after @author\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 5)\n" +
		"	* @deprecated\n" +
		"	   ^^^^^^^^^^\n" +
		"Javadoc: Description expected after @deprecated\n" +
		"----------\n" +
		"5. WARNING in X.java (at line 6)\n" +
		"	* @since\n" +
		"	   ^^^^^\n" +
		"Javadoc: Description expected after @since\n" +
		"----------\n" +
		"6. WARNING in X.java (at line 7)\n" +
		"	* @version\n" +
		"	   ^^^^^^^\n" +
		"Javadoc: Description expected after @version\n" +
		"----------\n" +
		"7. WARNING in X.java (at line 14)\n" +
		"	* @param  aParam\n" +
		"	          ^^^^^^\n" +
		"Javadoc: Description expected after this reference\n" +
		"----------\n" +
		"8. WARNING in X.java (at line 15)\n" +
		"	* @return\n" +
		"	   ^^^^^^\n" +
		"Javadoc: Description expected after @return\n" +
		"----------\n" +
		"9. WARNING in X.java (at line 16)\n" +
		"	* @throws NullPointerException\n" +
		"	          ^^^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: Description expected after this reference\n" +
		"----------\n" +
		"10. WARNING in X.java (at line 17)\n" +
		"	* @exception NullPointerException\n" +
		"	             ^^^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: Description expected after this reference\n" +
		"----------\n" +
		"11. WARNING in X.java (at line 23)\n" +
		"	* @serial\n" +
		"	   ^^^^^^\n" +
		"Javadoc: Description expected after @serial\n" +
		"----------\n" +
		"12. WARNING in X.java (at line 24)\n" +
		"	* @serialData\n" +
		"	   ^^^^^^^^^^\n" +
		"Javadoc: Description expected after @serialData\n" +
		"----------\n" +
		"13. WARNING in X.java (at line 25)\n" +
		"	* @serialField\n" +
		"	   ^^^^^^^^^^^\n" +
		"Javadoc: Description expected after @serialField\n" +
		"----------\n",
		null, null,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings
	);
}

/**
 * bug 227730: [Javadoc] Missing description should not be warned for @inheritDoc
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=227730"
 */
public void testBug227730a() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X extends Object {\n" +
		"	/**\n" +
		"	 * {@inheritDoc}\n" +
		"	 */\n" +
		"	public String toString() { \n" +
		"		return \"foo\";\n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runConformTest(units);
}

public void testBug227730b() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X extends Object {\n" +
		"	/**\n" +
		"	 * {@docRoot}\n" +
		"	 */\n" +
		"	public String toString() { \n" +
		"		return \"foo\";\n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runConformTest(units);
}

/**
 * bug 233187: [javadoc] partially qualified inner types  should be warned
 * test verify that partial inner class qualification are warned as javadoc tools does
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=233187"
 */
public void testBug233187a() {
	String[] units = new String[] {
		"test/a/X.java",
		"package test.a;\n" +
		"\n" +
		"public class X {\n" +
		"   public static class Y {\n" +
		"        public static class Z { \n" +
		"            /**\n" +
		"             * The position in the new method signature depends on\n" +
		"             * the position in the array passed to\n" +
		"             * {@link X.Y#foo(test.a.X.Y.Z[])} OK for javadoc tool\n" +
		"             * {@link X.Y#foo(test.a.X.Y.Z)} KO for javadoc tool\n" +
		"             * {@link X.Y#foo(no_test.a.X.Y.Z[])} KO for javadoc tool\n" +
		"             * {@link X.Y#foo(Y.Z[])} KO for javadoc tool\n" +
		"             * {@link test.a.X.Y#foo(Y.Z[])} KO for javadoc tool\n" +
		"             */\n" +
		"            public int bar() {\n" +
		"                return 0;\n" +
		"            }\n" +
		"        }\n" +
		"\n" +
		"        public void foo(Z[] params) {\n" +
		"        }\n" +
		"    }\n" +
		"}\n"
	};
	runNegativeTest(units,
		// warning - Tag @link: can't find foo(test.a.X.Y.Z) in test.a.X.Y
		// warning - Tag @link: can't find foo(no_test.a.X.Y.Z[]) in test.a.X.Y
		// warning - Tag @link: can't find foo(Y.Z[]) in test.a.X.Y
		// warning - Tag @link: can't find foo(Y.Z[]) in test.a.X.Y
		"----------\n" +
		"1. ERROR in test\\a\\X.java (at line 10)\n" +
		"	* {@link X.Y#foo(test.a.X.Y.Z)} KO for javadoc tool\n" +
		"	             ^^^\n" +
		"Javadoc: The method foo(X.Y.Z[]) in the type X.Y is not applicable for the arguments (X.Y.Z)\n" +
		"----------\n" +
		"2. ERROR in test\\a\\X.java (at line 11)\n" +
		"	* {@link X.Y#foo(no_test.a.X.Y.Z[])} KO for javadoc tool\n" +
		"	                 ^^^^^^^^^^^^^^^\n" +
		"Javadoc: no_test[] cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in test\\a\\X.java (at line 12)\n" +
		"	* {@link X.Y#foo(Y.Z[])} KO for javadoc tool\n" +
		"	                 ^^^\n" +
		"Javadoc: Invalid member type qualification\n" +
		"----------\n" +
		"4. ERROR in test\\a\\X.java (at line 13)\n" +
		"	* {@link test.a.X.Y#foo(Y.Z[])} KO for javadoc tool\n" +
		"	                        ^^^\n" +
		"Javadoc: Invalid member type qualification\n" +
		"----------\n"
	);
}
public void testBug233187b() {
	runNegativeTest(
		new String[] {
			"test/b/X.java",
			"package test.b;\n" +
			"\n" +
			"public class X {\n" +
			"   public static class Y {\n" +
			"        public static class Z { \n" +
			"            /**\n" +
			"             * The position in the new method signature depends on\n" +
			"             * the position in the array passed to\n" +
			"             * {@link X.Y#foo(test.b.X.Y.Z)} OK for javadoc tool\n" +
			"            * {@link X.Y#foo(test.b.X.Y.Z[])} KO for javadoc tool\n" +
			"             * {@link X.Y#foo(no_test.b.X.Y.Z)} KO for javadoc tool\n" +
			"             * {@link X.Y#foo(Y.Z)} KO for javadoc tool\n" +
			"             * {@link test.b.X.Y#foo(Y.Z)} KO for javadoc tool\n" +
			"             */\n" +
			"            public int bar() {\n" +
			"                return 0;\n" +
			"            }\n" +
			"        }\n" +
			"\n" +
			"        public void foo(Z params) {\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		// warning - Tag @link: can't find foo(test.b.X.Y.Z[]) in test.b.X.Y
		// warning - Tag @link: can't find foo(no_test.b.X.Y.Z) in test.b.X.Y
		// warning - Tag @link: can't find foo(Y.Z) in test.b.X.Y
		// warning - Tag @link: can't find foo(Y.Z) in test.b.X.Y
		"----------\n" +
		"1. ERROR in test\\b\\X.java (at line 10)\n" +
		"	* {@link X.Y#foo(test.b.X.Y.Z[])} KO for javadoc tool\n" +
		"	             ^^^\n" +
		"Javadoc: The method foo(X.Y.Z) in the type X.Y is not applicable for the arguments (X.Y.Z[])\n" +
		"----------\n" +
		"2. ERROR in test\\b\\X.java (at line 11)\n" +
		"	* {@link X.Y#foo(no_test.b.X.Y.Z)} KO for javadoc tool\n" +
		"	                 ^^^^^^^^^^^^^^^\n" +
		"Javadoc: no_test cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in test\\b\\X.java (at line 12)\n" +
		"	* {@link X.Y#foo(Y.Z)} KO for javadoc tool\n" +
		"	                 ^^^\n" +
		"Javadoc: Invalid member type qualification\n" +
		"----------\n" +
		"4. ERROR in test\\b\\X.java (at line 13)\n" +
		"	* {@link test.b.X.Y#foo(Y.Z)} KO for javadoc tool\n" +
		"	                        ^^^\n" +
		"Javadoc: Invalid member type qualification\n" +
		"----------\n"
	);
}
public void testBug233187c() {
	runConformTest(
		new String[] {
			"test/c/X.java",
			"package test.c;\n" +
			"\n" +
			"public class X {\n" +
			"	static class Y { \n" +
			"	}\n" +
			"	void foo(Y y) {}\n" +
			"	/**\n" +
			"	 * @see #foo(X.Y)\n" +
			"	 */\n" +
			"	void bar() {}\n" +
			"}\n"
		}
	);
}

/**
 * bug 233887: Build of Eclipse project stop by NullPointerException and will not continue on Eclipse version later than 3.4M7
 * test Ensure that no NPE is raised when a 1.5 param tag syntax is incorrectly used on a fiel with an initializer
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=233887"
 */
public void testBug233887() {
	String expectedError = this.complianceLevel <= ClassFileConstants.JDK1_4 ?
		"----------\n" +
		"1. ERROR in NPETest.java (at line 5)\n" +
		"	* @param <name> <description>\n" +
		"	         ^^^^^^\n" +
		"Javadoc: Invalid param tag name\n" +
		"----------\n"
	:
		"----------\n" +
		"1. ERROR in NPETest.java (at line 5)\n" +
		"	* @param <name> <description>\n" +
		"	   ^^^^^\n" +
		"Javadoc: Unexpected tag\n" +
		"----------\n";
	runNegativeTest(
		new String[] {
			"NPETest.java",
			"public class NPETest {\n" +
			"	public NPETest() {\n" +
			"	}\n" +
			"	/**\n" +
			"	 * @param <name> <description>\n" +
			"	 */\n" +
			"	private static final int MAX = 50;\n" +
			"\n" +
			"}\n"
		},
		expectedError,
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * bug 237937: [javadoc] Wrong "Javadoc: Malformed link reference" if href label contains //
 * test Ensure that no warning is raised when href label contains '//'
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=237937"
 */
public void testBug237937() {
	runConformTest(
		new String[] {
			"Link.java",
			"/**\n" +
			" * @see <a href=\"http://www.eclipse.org/\">http://www.eclipse.org</a>\n" +
			" * @see <a href=\"http://www.eclipse.org/\">//</a>\n" +
			" */\n" +
			"public class Link {}\n"
		}
	);
}

/**
 * bug 246712: [javadoc] Unexpected warning about missing parameter doc in case of @inheritDoc
 * test Ensure inline tag are considered as description
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=246712"
 */
public void testBug246712() {
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"	/**\n" +
			"	 * Do something more.\n" +
			"	 * \n" +
			"	 * @param monitor The monitor\n" +
			"	 * @return {@link String X}\n" +
			"	 */\n" +
			"	String foo(Object monitor) {\n" +
			"		return \"X\";\n" +
			"	}\n" +
			"}\n",
			"Y.java",
			"public class Y extends X {\n" +
			"\n" +
			"	/**\n" +
			"	 * Do something more.\n" +
			"	 * \n" +
			"	 * {@inheritDoc}\n" +
			"	 * \n" +
			"	 * @param monitor {@inheritDoc}\n" +
			"	 * @return {@link String Y}\n" +
			"	 */\n" +
			"	String foo(Object monitor) {\n" +
			"		return \"Y\";\n" +
			"	}\n" +
			"}\n"
		}
	);
}
public void testBug246712b() {
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runConformTest(
		new String[] {
			"X.java",
			"/**\n" +
			" * @author {@link String}\n" +
			" * @since {@link String}\n" +
			" * @version {@link String}\n" +
			" * @deprecated {@link String}\n" +
			"*/\n" +
			"public class X {\n" +
			"	/**\n" +
			"	 * @return {@link String}\n" +
			"	 * @since {@link String}\n" +
			"	 * @throws  Exception {@link String}\n" +
			"	 * @exception Exception {@link String}\n" +
			"	 * @serial {@link String}\n" +
			"	 * @serialData {@link String}\n" +
			"	 * @serialField {@link String}\n" +
			"	 * @deprecated {@link String}\n" +
			"	 */\n" +
			"	public String foo(String aParam) throws Exception {\n" +
			"		return new String();\n" +
			"	}\n" +
			"}"
		}
	);
}
// duplicate
public void testBug246715() {
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"	final static int WAIT_YES = 0;\n" +
			"	final static int WAIT_NO = 1;\n" +
			"	\n" +
			"	/**\n" +
			"	 * Do something more.\n" +
			"	 * \n" +
			"	 * @param waitFlag {@link #WAIT_YES} or {@link #WAIT_NO}\n" +
			"	 */\n" +
			"	String foo(int waitFlag) {\n" +
			"		return \"X\";\n" +
			"	}\n" +
			"}\n"
		}
	);
}

/**
 * bug 254825: [javadoc] compile error when referencing outer param from inner class javadoc
 * test Ensure that local variable reference does not imply missing compiler implementation error
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=254825"
 */
public void testBug254825() {
	runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  public Object foo(Object o) { \n" +
			"    return new Object() {\n" +
			"      /** @see #o */\n" +
			"      public void x() {}\n" +
			"    };\n" +
			"  }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	/** @see #o */\n" +
		"	          ^\n" +
		"Javadoc: o cannot be resolved or is not a field\n" +
		"----------\n"
	);
}
public void testBug254825b() {
	runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  /** @see #o */\n" +
			"  public Object foo(Object o) { return null; }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	/** @see #o */\n" +
		"	          ^\n" +
		"Javadoc: o cannot be resolved or is not a field\n" +
		"----------\n"
	);
}


/**
 * bug 258798: [1.5][compiler] Return type should be erased after unchecked conversion during inference
 * test Fix for this bug had side effects while reporting missing tags in javadoc comments.<br>
 * Following tests have been written to verify that noticed issues have been solved:
 * <ol>
 * <li>missing tags should be reported even when the method/constructor has
 * 	a &#064;see reference on itself</li>
 * <li>missing tag should be reported when superclass constructor has different
 * 	arguments (even if they are compatible)</li>
 * <li>missing tag should not be reported when method arguments are the same
 * 	even when the type argument is not the same</li>
 * </ol>
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=258798"
 */
public void testBug258798_1() {
	this.reportMissingJavadocTags = CompilerOptions.WARNING;
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"/**\n" +
			"* @see #X(int)\n" +
			"*/\n" +
			"X(int i) {\n" +
			"}\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 5)\n" +
		"	X(int i) {\n" +
		"	      ^\n" +
		"Javadoc: Missing tag for parameter i\n" +
		"----------\n"
	);
}
public void testBug258798_2a() {
	this.reportMissingJavadocTags = CompilerOptions.WARNING;
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"X(int i) {}\n" +
			"}\n" +
			"class Y extends X {\n" +
			"/** @see X#X(int) */\n" +
			"Y(double d) { super(0); }\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 6)\n" +
		"	Y(double d) { super(0); }\n" +
		"	         ^\n" +
		"Javadoc: Missing tag for parameter d\n" +
		"----------\n"
	);
}
public void testBug258798_2b() {
	this.reportMissingJavadocTags = CompilerOptions.WARNING;
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"public class X<T> {\n" +
				"X(ArrayList<T> alt) {}\n" +
				"}\n" +
				"class Y<U> extends X<U> {\n" +
				"/** @see X#X(ArrayList) */\n" +
				"Y(List<U> lu) { super(null); }\n" +
				"}\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 7)\n" +
			"	Y(List<U> lu) { super(null); }\n" +
			"	          ^^\n" +
			"Javadoc: Missing tag for parameter lu\n" +
			"----------\n"
		);
	}
}
public void testBug258798_2c() {
	this.reportMissingJavadocTags = CompilerOptions.WARNING;
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"public class X<T> {\n" +
				"X(Object o) {}\n" +
				"}\n" +
				"class Y<U> extends X<U> {\n" +
				"/** @see X#X(Object) */\n" +
				"Y(List<U> lu) { super(lu); }\n" +
				"}\n"
			},
			"----------\n" +
			"1. WARNING in X.java (at line 7)\n" +
			"	Y(List<U> lu) { super(lu); }\n" +
			"	          ^^\n" +
			"Javadoc: Missing tag for parameter lu\n" +
			"----------\n"
		);
	}
}
public void testBug258798_3() {
	this.reportMissingJavadocTags = CompilerOptions.WARNING;
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runConformTest(
			new String[] {
				"X.java",
				"import java.util.*;\n" +
				"public class X<T> {\n" +
				"X(List<T> lt) {}\n" +
				"}\n" +
				"class Y<U> extends X<U> {\n" +
				"/** @see X#X(List) */\n" +
				"Y(List<U> lu) { super(null); }\n" +
				"}\n"
			}
		);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=247037, make sure that we complain when @inheritdoc
// is used where it is outlawed by the specs. This test verifies that we complain when @inheritDoc
// is used with classes and interfaces.
public void testBug247037() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"/**\n" +
			" * {@inheritDoc}\n" +              // error, cannot be applied to a class
			" */\n" +
			"public class X {\n" +
			"}\n" +
			"/**\n" +
			" * {@inheritDoc}\n" +              // error, cannot be applied to interfaces.
			" */" +
			"interface Blah {\n" +
			"    void BlahBlah();\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	* {@inheritDoc}\n" +
		"	    ^^^^^^^^^^\n" +
		"Javadoc: Unexpected tag\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	* {@inheritDoc}\n" +
		"	    ^^^^^^^^^^\n" +
		"Javadoc: Unexpected tag\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=247037, make sure that we complain when @inheritdoc
//is used where it is outlawed by the specs. Here we test that when @inheritDoc is applied to a
// field or constructor, we complain.
public void testBug247037b() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"}\n" +
			"class Y extends X {\n" +
			"    /**\n" +
			"     * {@inheritDoc}\n" +  // error, cannot be applied to a field
			"    */\n" +
			"    public int field = 10;\n" +
			"    /**\n" +
			"     * @param x {@inheritDoc}\n" +  // error, cannot be applied to a constructor
			"    */\n" +
			"    Y(int x) {}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	* {@inheritDoc}\n" +
		"	    ^^^^^^^^^^\n" +
		"Javadoc: Unexpected tag\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	* @param x {@inheritDoc}\n" +
		"	             ^^^^^^^^^^\n" +
		"Javadoc: Unexpected tag\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=247037, make sure that we complain when @inheritdoc
//is used where it is outlawed by the specs. In this test we test the use of @inheritedDoc in some
// block tags.
public void testBug247037c() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    /**\n" +
			"     * @since 1.0\n" +
			"     * @return Blah\n" +
			"     * @param blah Blah Blah\n" +
			"     * @throws Exception When something is wrong\n" +
			"     */\n" +
			"    public int m(int blah) throws Exception {\n" +
			"        return 0;\n" +
			"    }\n" +
			"}\n" +
			"class Y extends X {\n" +
			"    /**\n" +
			"     * @param blah {@inheritDoc}\n" +
			"     * @return {@inheritDoc}\n" +
			"     * @since {@inheritDoc}\n" +  // error, cannot be used in @since
			"     * @author {@inheritDoc}\n" + // error, cannot be used in @author
			"     * @see {@inheritDoc}\n" +    // error, cannot be used in @see
			"     * @throws Exception {@inheritDoc}\n" +
			"     * @exception Exception {@inheritDoc}\n" +
			"     */\n" +
			"    public int m(int blah) throws Exception {\n" +
			"		return 1;\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 16)\n" +
		"	* @since {@inheritDoc}\n" +
		"	           ^^^^^^^^^^\n" +
		"Javadoc: Unexpected tag\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 17)\n" +
		"	* @author {@inheritDoc}\n" +
		"	            ^^^^^^^^^^\n" +
		"Javadoc: Unexpected tag\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 18)\n" +
		"	* @see {@inheritDoc}\n" +
		"	   ^^^\n" +
		"Javadoc: Missing reference\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 18)\n" +
		"	* @see {@inheritDoc}\n" +
		"	         ^^^^^^^^^^\n" +
		"Javadoc: Unexpected tag\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=247037, make sure that we complain when @inheritdoc
// is used where it is outlawed by the specs. Test to verify that every bad use of @inheritDoc triggers
// a message from the compiler
public void testBug247037d() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"}\n" +
			"class Y extends X {\n" +
			"    /**\n" +
			"     * @param blah {@inheritDoc}\n" + // error n() doesn't override anything.
			"     * @return {@inheritDoc}\n" +  // error, n() doesn't override anything
			"     * @author {@inheritDoc}\n" +   // error, cannot be used in @author
			"     */\n" +
			"    public int n(int blah) {\n" +
			"		return 1;\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	* @param blah {@inheritDoc}\n" +
		"	                ^^^^^^^^^^\n" +
		"Javadoc: Unexpected tag\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	* @return {@inheritDoc}\n" +
		"	            ^^^^^^^^^^\n" +
		"Javadoc: Unexpected tag\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 7)\n" +
		"	* @author {@inheritDoc}\n" +
		"	            ^^^^^^^^^^\n" +
		"Javadoc: Unexpected tag\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
/**
 * bug 267833:[javadoc] Custom tags should not be allowed for inline tags
 * test Ensure that a warning is raised when customs tags are used as inline tags
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=267833"
 */
public void testBug267833() {
	runConformTest(
		new String[] {
			"X.java",
			"/**\n" +
			"* Invalid custom tag {@custom \"Invalid\"}   \n" +
			"* @custom \"Valid\"\n" +
			"*/\n" +
			"public class X {\n" +
			"}"
		});
}
/**
 * Additional test for bug 267833
 * test Ensure that the JavadocTagConstants.JAVADOC_TAG_TYPE array is up to date with the other arrays, such as
 *  JavadocTagConstants.TAG_NAMES, JavadocTagConstants.INLINE_TAGS and JavadocTagConstants.BLOCK_TAGS
 */
public void testBug267833_2() {

	assertEquals(JavadocTagConstants.TAG_NAMES.length,JavadocTagConstants.JAVADOC_TAG_TYPE.length);

	int tagsLength = JavadocTagConstants.TAG_NAMES.length;
	nextTag:for (int index=0; index < tagsLength; index++) {
		char[] tagName = JavadocTagConstants.TAG_NAMES[index];
		if (tagName.length > 0) {
			for (int i=0; i < JavadocTagConstants.BLOCK_TAGS_LENGTH; i++) {
				int length = JavadocTagConstants.BLOCK_TAGS[i].length;
				for (int j=0; j < length; j++) {
					if (tagName == JavadocTagConstants.BLOCK_TAGS[i][j]) {
						int tagType = JavadocTagConstants.JAVADOC_TAG_TYPE[index];
						assertTrue((tagType & JavadocTagConstants.TAG_TYPE_BLOCK) != 0);
						continue nextTag;
					}
				}
			}
			for (int i=0; i < JavadocTagConstants.INLINE_TAGS_LENGTH; i++) {
				int length = JavadocTagConstants.INLINE_TAGS[i].length;
				for (int j=0; j < length; j++) {
					if (tagName == JavadocTagConstants.INLINE_TAGS[i][j]) {
						int tagType = JavadocTagConstants.JAVADOC_TAG_TYPE[index];
						assertTrue((tagType & JavadocTagConstants.TAG_TYPE_INLINE) != 0);
						continue nextTag;
					}
				}
			}
			for (int i=0; i < JavadocTagConstants.IN_SNIPPET_TAGS_LENGTH; i++) {
				int length = JavadocTagConstants.IN_SNIPPET_TAGS[i].length;
				for (int j=0; j < length; j++) {
					if (tagName == JavadocTagConstants.IN_SNIPPET_TAGS[i][j]) {
						assertEquals(JavadocTagConstants.JAVADOC_TAG_TYPE[index], JavadocTagConstants.TAG_TYPE_IN_SNIPPET);
						continue nextTag;
					}
				}
			}
		}
		assertEquals(JavadocTagConstants.JAVADOC_TAG_TYPE[index], JavadocTagConstants.TAG_TYPE_NONE);
	}
}
/**
 * Additional test for bug 267833
 * test Ensure that a warning is raised when block tags are used as inline tags.
 */
public void testBug267833_3() {
	if(this.complianceLevel >= ClassFileConstants.JDK16) {
		return;
	}
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"/** \n" +
			"* Description {@see String} , {@return int}, {@since 1.0}, {@param i}, {@throws NullPointerException}\n" +
			"* and more {@author jay}, {@category cat}, {@deprecated}, {@exception NullPointerException}, {@version 1.1}\n" +
			"* and more {@since 1.0}, {@serial 0L}, {@serialData data}, {@serialField field}\n" +
			"* @param i\n" +
			"* @return value\n" +
			"* @throws NullPointerException \n" +
			"*/\n" +
			"public int foo(int i) {\n" +
			"	return 0;\n" +
			"}\n" +
			"}\n" },
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	* Description {@see String} , {@return int}, {@since 1.0}, {@param i}, {@throws NullPointerException}\n" +
			"	                ^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	* Description {@see String} , {@return int}, {@since 1.0}, {@param i}, {@throws NullPointerException}\n" +
			"	                                ^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 3)\n" +
			"	* Description {@see String} , {@return int}, {@since 1.0}, {@param i}, {@throws NullPointerException}\n" +
			"	                                               ^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 3)\n" +
			"	* Description {@see String} , {@return int}, {@since 1.0}, {@param i}, {@throws NullPointerException}\n" +
			"	                                                             ^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"5. ERROR in X.java (at line 3)\n" +
			"	* Description {@see String} , {@return int}, {@since 1.0}, {@param i}, {@throws NullPointerException}\n" +
			"	                                                                         ^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"6. ERROR in X.java (at line 4)\n" +
			"	* and more {@author jay}, {@category cat}, {@deprecated}, {@exception NullPointerException}, {@version 1.1}\n" +
			"	             ^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"7. ERROR in X.java (at line 4)\n" +
			"	* and more {@author jay}, {@category cat}, {@deprecated}, {@exception NullPointerException}, {@version 1.1}\n" +
			"	                            ^^^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"8. ERROR in X.java (at line 4)\n" +
			"	* and more {@author jay}, {@category cat}, {@deprecated}, {@exception NullPointerException}, {@version 1.1}\n" +
			"	                                             ^^^^^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"9. ERROR in X.java (at line 4)\n" +
			"	* and more {@author jay}, {@category cat}, {@deprecated}, {@exception NullPointerException}, {@version 1.1}\n" +
			"	                                                            ^^^^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"10. ERROR in X.java (at line 4)\n" +
			"	* and more {@author jay}, {@category cat}, {@deprecated}, {@exception NullPointerException}, {@version 1.1}\n" +
			"	                                                                                               ^^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"11. ERROR in X.java (at line 5)\n" +
			"	* and more {@since 1.0}, {@serial 0L}, {@serialData data}, {@serialField field}\n" +
			"	             ^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"12. ERROR in X.java (at line 5)\n" +
			"	* and more {@since 1.0}, {@serial 0L}, {@serialData data}, {@serialField field}\n" +
			"	                           ^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"13. ERROR in X.java (at line 5)\n" +
			"	* and more {@since 1.0}, {@serial 0L}, {@serialData data}, {@serialField field}\n" +
			"	                                         ^^^^^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n" +
			"14. ERROR in X.java (at line 5)\n" +
			"	* and more {@since 1.0}, {@serial 0L}, {@serialData data}, {@serialField field}\n" +
			"	                                                             ^^^^^^^^^^^\n" +
			"Javadoc: Unexpected tag\n" +
			"----------\n");
}
/**
 * Additional test for bug 267833 and https://github.com/eclipse-jdt/eclipse.jdt.core/issues/795
 * For java 16+:
 * 1) Ensure that a warning is raised when block tags are used as inline tags.
 * 2) Ensure there is no error reported for return tag used inline
 * 3) TODO: ensure  there is no error reported for duplicated return tag if it is used inline and as block
 */
public void testBug267833_3a() {
	if(this.complianceLevel < ClassFileConstants.JDK16) {
		return;
	}
	runNegativeTest(
			new String[] {
					"X.java",
					"public class X {\n" +
							"/** \n" +
							"* Description {@see String} , {@return int}, {@since 1.0}, {@param i}, {@throws NullPointerException}\n" +
							"* and more {@author jay}, {@category cat}, {@deprecated}, {@exception NullPointerException}, {@version 1.1}\n" +
							"* and more {@since 1.0}, {@serial 0L}, {@serialData data}, {@serialField field}\n" +
							"* @param i\n" +
							"* @return value\n" +
							"* @throws NullPointerException \n" +
							"*/\n" +
							"public int foo(int i) {\n" +
							"	return 0;\n" +
							"}\n" +
			"}\n" },
			"----------\n" +
					"1. ERROR in X.java (at line 3)\n" +
					"	* Description {@see String} , {@return int}, {@since 1.0}, {@param i}, {@throws NullPointerException}\n" +
					"	                ^^^\n" +
					"Javadoc: Unexpected tag\n" +
					"----------\n" +
					"2. ERROR in X.java (at line 3)\n" +
					"	* Description {@see String} , {@return int}, {@since 1.0}, {@param i}, {@throws NullPointerException}\n" +
					"	                                               ^^^^^\n" +
					"Javadoc: Unexpected tag\n" +
					"----------\n" +
					"3. ERROR in X.java (at line 3)\n" +
					"	* Description {@see String} , {@return int}, {@since 1.0}, {@param i}, {@throws NullPointerException}\n" +
					"	                                                             ^^^^^\n" +
					"Javadoc: Unexpected tag\n" +
					"----------\n" +
					"4. ERROR in X.java (at line 3)\n" +
					"	* Description {@see String} , {@return int}, {@since 1.0}, {@param i}, {@throws NullPointerException}\n" +
					"	                                                                         ^^^^^^\n" +
					"Javadoc: Unexpected tag\n" +
					"----------\n" +
					"5. ERROR in X.java (at line 4)\n" +
					"	* and more {@author jay}, {@category cat}, {@deprecated}, {@exception NullPointerException}, {@version 1.1}\n" +
					"	             ^^^^^^\n" +
					"Javadoc: Unexpected tag\n" +
					"----------\n" +
					"6. ERROR in X.java (at line 4)\n" +
					"	* and more {@author jay}, {@category cat}, {@deprecated}, {@exception NullPointerException}, {@version 1.1}\n" +
					"	                            ^^^^^^^^\n" +
					"Javadoc: Unexpected tag\n" +
					"----------\n" +
					"7. ERROR in X.java (at line 4)\n" +
					"	* and more {@author jay}, {@category cat}, {@deprecated}, {@exception NullPointerException}, {@version 1.1}\n" +
					"	                                             ^^^^^^^^^^\n" +
					"Javadoc: Unexpected tag\n" +
					"----------\n" +
					"8. ERROR in X.java (at line 4)\n" +
					"	* and more {@author jay}, {@category cat}, {@deprecated}, {@exception NullPointerException}, {@version 1.1}\n" +
					"	                                                            ^^^^^^^^^\n" +
					"Javadoc: Unexpected tag\n" +
					"----------\n" +
					"9. ERROR in X.java (at line 4)\n" +
					"	* and more {@author jay}, {@category cat}, {@deprecated}, {@exception NullPointerException}, {@version 1.1}\n" +
					"	                                                                                               ^^^^^^^\n" +
					"Javadoc: Unexpected tag\n" +
					"----------\n" +
					"10. ERROR in X.java (at line 5)\n" +
					"	* and more {@since 1.0}, {@serial 0L}, {@serialData data}, {@serialField field}\n" +
					"	             ^^^^^\n" +
					"Javadoc: Unexpected tag\n" +
					"----------\n" +
					"11. ERROR in X.java (at line 5)\n" +
					"	* and more {@since 1.0}, {@serial 0L}, {@serialData data}, {@serialField field}\n" +
					"	                           ^^^^^^\n" +
					"Javadoc: Unexpected tag\n" +
					"----------\n" +
					"12. ERROR in X.java (at line 5)\n" +
					"	* and more {@since 1.0}, {@serial 0L}, {@serialData data}, {@serialField field}\n" +
					"	                                         ^^^^^^^^^^\n" +
					"Javadoc: Unexpected tag\n" +
					"----------\n" +
					"13. ERROR in X.java (at line 5)\n" +
					"	* and more {@since 1.0}, {@serial 0L}, {@serialData data}, {@serialField field}\n" +
					"	                                                             ^^^^^^^^^^^\n" +
					"Javadoc: Unexpected tag\n" +
					"----------\n" +
					"14. ERROR in X.java (at line 7)\n" + // XXX should be WARNING
					"	* @return value\n" +
					"	   ^^^^^^\n" +
					"Javadoc: Duplicate tag for return type\n" + // XXX change to: "Javadoc: @return has already been specified"
			"----------\n");
}

/**
 * bug 281609: [javadoc] "Javadoc: Invalid reference" warning for @link to Java package
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=281609"
 */
public void testBug281609a() {
	runNegativeTest(
		new String[] {
			"pkg/X.java",
			"package pkg;\n" +
			"\n" +
			"public class X {\n" +
			"	/**\n" +
			"	 * @see java\n" +
			"	 * @see java.lang\n" +
			"	 * @see PKG\n" +
			"	 * @see pkg\n" +
			"	 */\n" +
			"	public void foo() { \n" +
			"	 \n" +
			"	}\n" +
			"}\n"
		},
		// warning - Tag @see: reference not found: PKG
		"----------\n" +
		"1. ERROR in pkg\\X.java (at line 7)\n" +
		"	* @see PKG\n" +
		"	       ^^^\n" +
		"Javadoc: PKG cannot be resolved to a type\n" +
		"----------\n"
	);
}
public void testBug281609b() {
	runConformTest(
		new String[] {
			"x/y/z/X.java",
			"package x.y.z;\n" +
			"\n" +
			"public class X {\n" +
			"	/**\n" +
			"	 * @see java\n" +
			"	 * @see java.lang\n" +
			"	 * @see x\n" +
			"	 * @see x.y\n" +
			"	 * @see x.y.z\n" +
			"	 */\n" +
			"	public void foo() { \n" +
			"	 \n" +
			"	}\n" +
			"}\n"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=292510
// Test to verify that partial types are demarcated correctly while
// annotating a deprecated type error in javadoc.
public void testBug292510() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode, CompilerOptions.ENABLED);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"/**  @deprecated */\n" +
				"public class X {\n" +
				"    public class XX {\n" +
				"        public class XXX {\n" +
				"        }\n" +
				"    }\n" +
				"}\n",
			"Y.java",
			"/**\n" +
				" * @see X.XX.XXX\n" +
				" */\n" +
				"public class Y {\n" +
				"}\n"},
		null,
		options,
		"----------\n" +
		"1. ERROR in Y.java (at line 2)\n" +
		"	* @see X.XX.XXX\n" +
		"	       ^\n" +
		"Javadoc: The type X is deprecated\n" +
		"----------\n" +
		"2. ERROR in Y.java (at line 2)\n" +
		"	* @see X.XX.XXX\n" +
		"	       ^^^^\n" +
		"Javadoc: The type X.XX is deprecated\n" +
		"----------\n" +
		"3. ERROR in Y.java (at line 2)\n" +
		"	* @see X.XX.XXX\n" +
		"	       ^^^^^^^^\n" +
		"Javadoc: The type X.XX.XXX is deprecated\n" +
		"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=316782
// Test to verify that turning on process annotations doesn't turn on javadoc check
public void testBug316782() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) {
		return;
	}
	this.processAnnotations = CompilerOptions.ENABLED;
	this.docCommentSupport = CompilerOptions.DISABLED;
	runConformTest(
		new String[] {
			"X.java",
			"/**  @see X.XX.XXX */\n" +
				"public class X {\n" +
				"/**  @see X.XX.XXX */\n" +
				"    public void foo() { }\n" +
				"}\n"
		});
}
/**
 * bug 222188: [javadoc] Incorrect usage of inner type not reported
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=222188"
 */
public void testBug222188a() {
	// case 1: partially qualified reference in another package
	String[] units = new String[] {
		"pack/Test.java",
		"package pack;\n" +
		"public class Test {\n" +
		"        public interface Inner { }\n" +
		"}\n"
		,
		"pack2/X.java",
		"package pack2;\n" +
		"import pack.Test;\n" +
		"public class X {\n" +
		"/**\n" +
		" * See also {@link Test.Inner} -- error/warning \n" +
		" */\n" +
		"     public void m() { }\n" +
		"}\n"
	};
	runNegativeTest(units,
		// warning - Tag @link: reference not found: Test.Inner
		"----------\n" +
		"1. ERROR in pack2\\X.java (at line 5)\n" +
		"	* See also {@link Test.Inner} -- error/warning \n" +
		"	                  ^^^^^^^^^^\n" +
		"Javadoc: Invalid member type qualification\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug222188b() {
	// case 2: fully but invalid qualified reference in another package
	String[] units = new String[] {
		"pack/Test.java",
		"package pack;\n" +
		"public class Test {\n" +
		"        public interface Inner { }\n" +
		"}\n"
		,
		"pack2/X.java",
		"package pack2;\n" +
		"public class X {\n" +
		"/**\n" +
		" * See also {@link pack.Test.Inners} -- error/warning \n" +
		" */\n" +
		"     public void m() { }\n" +
		"}\n"
	};
	runNegativeTest(units,
		// warning - Tag @link: reference not found: Test.Inner
		"----------\n" +
		"1. ERROR in pack2\\X.java (at line 4)\n" +
		"	* See also {@link pack.Test.Inners} -- error/warning \n" +
		"	                  ^^^^^^^^^^^^^^^^\n" +
		"Javadoc: pack.Test.Inners cannot be resolved to a type\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * bug 221539: [javadoc] doesn't detect non visible inner class
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=221539"
 */
public void testBug221539a() {
	// partially qualified reference in the same package
	String[] units = new String[] {
		"p/Test.java",
		"package p;\n" +
		"/**\n" +
		" * {@link Test.Inner} not ok for Javadoc\n" +
		" * {@link Foo.Inner} ok for Javadoc\n" +
		" */\n" +
		"public class Test extends Foo {\n" +
		"}\n"
		,
		"p/Foo.java",
		"package p;\n" +
		"public class Foo {\n" +
		"	static class Inner {}\n" +
		"}\n"
	};
	runNegativeTest(units,
		// warning - Tag @link: reference not found: Test.Inner
		"----------\n" +
		"1. ERROR in p\\Test.java (at line 3)\n" +
		"	* {@link Test.Inner} not ok for Javadoc\n" +
		"	         ^^^^^^^^^^\n" +
		"Javadoc: Invalid member type qualification\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug221539b() {
	// partially qualified reference in different package
	String[] units = new String[] {
		"p1/Test.java",
		"package p1;\n" +
		"import p2.Foo;\n" +
		"/**\n" +
		" * {@link Test.Inner} not ok for Javadoc\n" +
		" * {@link Foo.Inner} not ok Javadoc\n" +
		" * {@link p2.Foo.Inner} ok for Javadoc as fully qualified\n" +
		" */\n" +
		"public class Test extends Foo {\n" +
		"}\n"
		,
		"p2/Foo.java",
		"package p2;\n" +
		"public class Foo {\n" +
		"	public static class Inner {}\n" +
		"}\n"
	};
	runNegativeTest(units,
		// warning - Tag @link: reference not found: Test.Inner
		// warning - Tag @link: reference not found: Foo.Inner
		"----------\n" +
		"1. ERROR in p1\\Test.java (at line 4)\n" +
		"	* {@link Test.Inner} not ok for Javadoc\n" +
		"	         ^^^^^^^^^^\n" +
		"Javadoc: Invalid member type qualification\n" +
		"----------\n" +
		"2. ERROR in p1\\Test.java (at line 5)\n" +
		"	* {@link Foo.Inner} not ok Javadoc\n" +
		"	         ^^^^^^^^^\n" +
		"Javadoc: Invalid member type qualification\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

public void testBug221539c() {
	// case 3: partially qualified references are valid within the same CU
	this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
	runConformTest(
		new String[] {
			"pack/Test.java",
			"package pack;\n" +
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

public void testBug382606() {
	runConformTest(
			new String[] {
				"pack/A.java",
				"package pack;\n" +
				"/**\n"+
				"* @see A\n" +
				"*/\n" +
				"public interface A {\n"+
				"}\n"+
				"/**\n"+
				"* @see #B()\n"+
				"*/\n"+
				"class B {\n"+
				" B() {}\n"+
				"\n"+
				" public void foo(){\n"+
				"     new B();\n"+
				" }\n"+
				"}\n"
			}
		);
}

/**
 * bug 206345: [javadoc] compiler should not interpret contents of {@literal}
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=206345"
 */
public void testBug206345a() {
	// @litteral tags display text without interpreting the text as HTML markup or nested javadoc tags
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X extends Object {\n" +
		"	/**\n" +
		"	 * This is {@literal raw text:\n" +
		"	 * 			{@link BadLink} is just text}\n" +
		"	 * 			{@link expected_error}\n" +
		"	 * }\n" +
		"	 */\n" +
		"	public String toString() { \n" +
		"		return \"foo\";\n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runNegativeTest(units,
		//warning - Tag @link: reference not found: expected_error
		"----------\n" +
		"1. ERROR in pkg\\X.java (at line 7)\r\n" +
		"	* 			{@link expected_error}\r\n" +
		"	  			       ^^^^^^^^^^^^^^\n" +
		"Javadoc: expected_error cannot be resolved to a type\n" +
		"----------\n");
}
public void testBug206345b() {
	// same for @code tags
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X extends Object {\n" +
		"	/**\n" +
		"	 * This is {@code raw text:\n" +
		"	 * 			{@link BadLink} is just text}\n" +
		"	 * 			{@link expected_error}\n" +
		"	 * }\n" +
		"	 */\n" +
		"	public String toString() { \n" +
		"		return \"foo\";\n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runNegativeTest(units,
		// warning - Tag @link: reference not found: expected_error
		"----------\n" +
		"1. ERROR in pkg\\X.java (at line 7)\r\n" +
		"	* 			{@link expected_error}\r\n" +
		"	  			       ^^^^^^^^^^^^^^\n" +
		"Javadoc: expected_error cannot be resolved to a type\n" +
		"----------\n");
}
public void testBug206345c() {
	// verify we still validate other syntax
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X extends Object {\n" +
		"	/**\n" +
		"	 * This is {@link raw text:\n" +
		"	 * 			{@link BadLink} is just text}\n" +
		"	 * 			{@link expected_error}\n" +
		"	 * }\n" +
		"	 */\n" +
		"	public String toString() { \n" +
		"		return \"foo\";\n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runNegativeTest(units,
		// warning - Tag @link: reference not found: raw text: {@link BadLink} is just text
		// warning - Tag @link: reference not found: expected_error
		//
		"----------\n" +
		"1. ERROR in pkg\\X.java (at line 5)\n" +
		"	* This is {@link raw text:\n" +
		"	          ^^^^^^^^^^^^^^^^\n" +
		"Javadoc: Missing closing brace for inline tag\n" +
		"----------\n" +
		"2. ERROR in pkg\\X.java (at line 5)\n" +
		"	* This is {@link raw text:\n" +
		"	                 ^^^\n" +
		"Javadoc: raw cannot be resolved to a type\n" +
		"----------\n" +
		"3. ERROR in pkg\\X.java (at line 6)\n" +
		"	* 			{@link BadLink} is just text}\n" +
		"	  			       ^^^^^^^\n" +
		"Javadoc: BadLink cannot be resolved to a type\n" +
		"----------\n" +
		"4. ERROR in pkg\\X.java (at line 7)\n" +
		"	* 			{@link expected_error}\n" +
		"	  			       ^^^^^^^^^^^^^^\n" +
		"Javadoc: expected_error cannot be resolved to a type\n" +
		"----------\n");
}
public void testBug206345d() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X extends Object {\n" +
		"	/**\n" +
		"	 * This is {@literal raw text:\n" +
		"	 * 			{@link BadLink}}}} is just text}\n" +
		"	 * 			{@link expected_error}\n" +
		"	 * }\n" +
		"	 */\n" +
		"	public String toString() { \n" +
		"		return \"foo\";\n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runNegativeTest(units,
		// warning - Tag @link: reference not found: expected_error
		"----------\n" +
		"1. ERROR in pkg\\X.java (at line 7)\n" +
		"	* 			{@link expected_error}\n" +
		"	  			       ^^^^^^^^^^^^^^\n" +
		"Javadoc: expected_error cannot be resolved to a type\n" +
		"----------\n");
}
public void testBug206345e() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X extends Object {\n" +
		"	/**\n" +
		"	 * This is {@code raw text:\n" +
		"	 * 			{{{{{{@link BadLink}}} is just text}\n" +
		"	 * @since 4.2\n" +
		"	 */\n" +
		"	public String toString() { \n" +
		"		return \"foo\";\n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runNegativeTest(units,
		// warning - End Delimiter } missing for possible See Tag in comment string: "This is {@code raw text: {{{{{{@link BadLink}}} is just text}"
		"----------\n" +
		"1. ERROR in pkg\\X.java (at line 5)\r\n" +
		"	* This is {@code raw text:\n" +
		"	 * 			{{{{{{@link BadLink}}} is just text}\r\n" +
		"	          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: Missing closing brace for inline tag\n" +
		"----------\n");
}
public void testBug206345f() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X extends Object {\n" +
		"	/**\n" +
		"	 * This is {@code raw text:\n" +
		"	 * 			{@link BadLink}\n" +
		"	 * @since 4.2\n" +
		"	 */\n" +
		"	public String toString() { \n" +
		"		return \"foo\";\n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runNegativeTest(units,
		// warning - End Delimiter } missing for possible See Tag in comment string: "This is {@code raw text: {@link BadLink}"
		"----------\n" +
		"1. ERROR in pkg\\X.java (at line 5)\r\n" +
		"	* This is {@code raw text:\n" +
		"	 * 			{@link BadLink}\r\n" +
		"	          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: Missing closing brace for inline tag\n" +
		"----------\n");
	}
public void testBug206345g() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X extends Object {\n" +
		"	/**\n" +
		"	 * This is {@code raw text:\n" +
		"	 * 			{@link BadLink\n" +
		"	 * @since 4.2\n" +
		"	 */\n" +
		"	public String toString() { \n" +
		"		return \"foo\";\n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runNegativeTest(units,
		"----------\n" +
		"1. ERROR in pkg\\X.java (at line 5)\n" +
		"	* This is {@code raw text:\n" +
		"	          ^^^^^^^^^^^^^^^^\n" +
		"Javadoc: Missing closing brace for inline tag\n" +
		"----------\n");
}
public void testBug206345h() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X extends Object {\n" +
		"	/**\n" +
		"	 * This is {@code raw text:\n" +
		"	 * @since 4.2\n" +
		"	 */\n" +
		"	public String toString() { \n" +
		"		return \"foo\";\n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runNegativeTest(units,
		"----------\n" +
		"1. ERROR in pkg\\X.java (at line 5)\r\n" +
		"	* This is {@code raw text:\r\n" +
		"	          ^^^^^^^^^^^^^^^^\n" +
		"Javadoc: Missing closing brace for inline tag\n" +
		"----------\n");
}
public void testBug206345i() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X extends Object {\n" +
		"	/**\n" +
		"	 * This is {@code raw text:\n" +
		"	 */\n" +
		"	public String toString() { \n" +
		"		return \"foo\";\n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runNegativeTest(units,
		"----------\n" +
		"1. ERROR in pkg\\X.java (at line 5)\r\n" +
		"	* This is {@code raw text:\r\n" +
		"	          ^^^^^^^^^^^^^^^^\n" +
		"Javadoc: Missing closing brace for inline tag\n" +
		"----------\n");
}
public void testBug206345j() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X extends Object {\n" +
		"	/**\n" +
		"	 * This is {@literal raw text:\n" +
		"	 * 			{@link BadLink} is just text}\n" +
		"	 */\n" +
		"	public String toString() { \n" +
		"		return \"foo\";\n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runConformReferenceTest(units);
}
public void testBug206345k() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X extends Object {\n" +
		"	/**\n" +
		"	 * This is {@code raw text:\n" +
		"	 * 			{@link BadLink} is just text}\n" +
		"	 * }\n" +
		"	 */\n" +
		"	public String toString() { \n" +
		"		return \"foo\";\n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runConformReferenceTest(units);
}
public void testBug206345l() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X extends Object {\n" +
		"	/**\n" +
		"	 * This is {@literal raw text:\n" +
		"	 * 			{@link BadLink}\n" +
		"	 */\n" +
		"	public String toString() { \n" +
		"		return \"foo\";\n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runNegativeTest(units,
		// warning - End Delimiter } missing for possible See Tag in comment string: "This is {@literal raw text: {@link BadLink}"
		"----------\n" +
		"1. ERROR in pkg\\X.java (at line 5)\n" +
		"	* This is {@literal raw text:\n" +
		"	 * 			{@link BadLink}\n" +
		"	          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: Missing closing brace for inline tag\n" +
		"----------\n");
}
public void testBug206345m() {
	String[] units = new String[] {
		"pkg/X.java",
		"package pkg;\n" +
		"\n" +
		"public class X extends Object {\n" +
		"	/**\n" +
		"	 * This is {@code raw text:\n" +
		"	 * 			{@link BadLink}\n" +
		"	 */\n" +
		"	public String toString() { \n" +
		"		return \"foo\";\n" +
		"	}\n" +
		"}\n"
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runNegativeTest(units,
		// warning - End Delimiter } missing for possible See Tag in comment string: "This is {@code raw text: {@link BadLink}"
		"----------\n" +
		"1. ERROR in pkg\\X.java (at line 5)\n" +
		"	* This is {@code raw text:\n" +
		"	 * 			{@link BadLink}\n" +
		"	          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Javadoc: Missing closing brace for inline tag\n" +
		"----------\n");
}
}

